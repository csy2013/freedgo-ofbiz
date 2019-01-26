package com.yuaoq.yabiz.app.mobile.microservice.question.api.v1;

import com.yuaoq.yabiz.app.mobile.microservice.redpackage.api.v1.RedPackageV1Controller;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.CouponUtils;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("all")
@RestController
@RequestMapping(path = "/api/question/v1")
public class QuestionV1Controller {
    public static final String module = RedPackageV1Controller.class.getName();
    private long scoreNums05=5L;//每答对一题送多少积分
    private long scoreNums10=10L;//连续答对固定题目送多少积分
    /**
     * 获取一个问题，如果带了groupId说明连续答题，否则是新发起的答题，需要返回groupId
     * @return  status 0今日已经答错  1正常获取一个题目 2异常
     */
    @RequestMapping(value = "/getQuestion", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getQuestion(HttpServletRequest request, String groupId,HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) request.getAttribute("userLogin");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        List<GenericValue> questionSettings = delegator.findByAnd("QuestionSetting",UtilMisc.toMap("status","1"));
        if(questionSettings==null||questionSettings.size()==0){
            resultData.put("retCode", 0);
            resultData.put("message", "答题模板暂未设置");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        //查看答题设置
        GenericValue questionSetting =questionSettings.get(0);
        //每答对多少题送10积分
        long scoreQuestNums= questionSetting.getLong("scoreQuestNums");
        //送多少积分
//        long scoreNums= scoreNums;
        //没答对一题得多少分，写死5分
//        long everyQuestionNums  =scoreQuestNums;

        String partyId = CommonUtils.getPartyId(delegator, loginName);

        List<GenericValue> partyQuestionList = delegator.findByAnd("Question");
        if(partyQuestionList==null||partyQuestionList.size()==0){
            resultData.put("retCode", 0);
            resultData.put("message", "查询不到答题信息！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        //查询今天用户是否可以答题了
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PQRR", "PartyQuestionRefuseRecord");
        dynamicView.addAlias("PQRR", "partyId");
        dynamicView.addAlias("PQRR", "date");

        List<EntityCondition> filedExprs = FastList.newInstance();
        //一天前的时间
        Timestamp startTime = new Timestamp(CommonUtils.getStartTime().getTime());
        Timestamp endTime = new Timestamp(CommonUtils.getEndTime().getTime());
        filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("date"), EntityOperator.GREATER_THAN, startTime));
        filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("date"), EntityOperator.LESS_THAN, endTime));
        filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyId"), EntityOperator.EQUALS, partyId));
        EntityCondition mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);

        boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator pli=null;
        List<GenericValue> refuseRecordList = null;
        try {
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, null);
            refuseRecordList = pli.getCompleteList();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        } finally {
            if (pli != null) {
                try {
                    pli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {

            }
        }

        //获取用户总积分
        String sql="SELECT sum(SCORE_VALUE) scoreSum from PARTY_SCORE_DETAIL WHERE PARTY_ID='"+partyId+"' and SCORE_TYPE='SCORE_TYPE_QUESTION'";
        SQLProcessor sqlP = null;

        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            if (rs.next()) {
                long scoreSum= rs.getLong("scoreSum");
                resultData.put("scoreSum",scoreSum);
            }

            //用户总答对了多少题
            sql="SELECT count(*) rightNum from PARTY_QUESTION_ANSWER where PARTY_ID='"+partyId+"' and IS_RIGHT='Y'";
            sqlP.executeQuery(sql);
            rs = sqlP.getResultSet();
            if (rs.next()) {
                long rightNum= rs.getLong("rightNum");
                resultData.put("rightNum",rightNum);
            }

            sql="SELECT count(*) rightNum from PARTY_QUESTION_ANSWER where PARTY_ID='"+partyId+"' and IS_RIGHT='Y' and GROUP_ID='"+groupId+"'";
            sqlP.executeQuery(sql);
            rs = sqlP.getResultSet();
            if (rs.next()) {
                //当前一组中答对了多少分
                long rightNum = rs.getLong("rightNum");
                //预测答对下一题会得多少分
                long nextRightNum = 0;
                if( (rightNum+1)%scoreQuestNums==0){
                    nextRightNum=scoreNums10;
                }else{
                    nextRightNum=scoreNums05;
                }
                resultData.put("nextRightNum",nextRightNum);
            }

        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());

            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }finally {
            sqlP.close();
        }

        if(refuseRecordList!=null && refuseRecordList.size()>0){
            resultData.put("retCode", 1);
            resultData.put("status", "0");
            resultData.put("message", "用户已经答错题，今日不可回答，或者转发到微信朋友圈重新获取机会");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        //查看用户今日答题次数
        String todayStartDate = CommonUtils.getStringDate(CommonUtils.getStartTime());
        sql ="select count(*) num from PARTY_QUESTION_ANSWER where PARTY_ID='"+partyId+"' and CREATED_TX_STAMP>'"+todayStartDate+"' AND ANSWER_ID is not null";
        int todayAnswerCount=0;//今日答题次数
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            if (rs.next()) {
                todayAnswerCount=rs.getInt("num");
            }
        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }finally {
            sqlP.close();
        }

        if(todayAnswerCount>=5){
            resultData.put("retCode", 1);
            resultData.put("status", "3");
            resultData.put("message", "今日用户已经答题五次，不可再次答题！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }


        boolean isFirst=false;
        //获取一个题目
        if(groupId!=null && !"".equals(groupId)){
            //说明正在答题中
            //查看该题目是否已经结束
            List<GenericValue> partyQuestions = delegator.findByAnd("PartyQuestion", UtilMisc.toMap("groupId",groupId));
            if(partyQuestions==null||partyQuestions.size()==0){
                resultData.put("retCode", 0);
                resultData.put("message", "查询不到该批次答题信息！");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }
            //获取一个题目。
            GenericValue partyQuestion = partyQuestions.get(0);
//            String isEnd =partyQuestion.getString("isEnd");
//            if("Y".equals(isEnd)){
//                resultData.put("retCode", 0);
//                resultData.put("message", "该批次答题已经结束，请重新开始答题！");
//                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//            }

        }else{
            isFirst=true;
            groupId = delegator.getNextSeqId("PartyQuestion");
            GenericValue partyQuestion = delegator.makeValue("PartyQuestion",UtilMisc.toMap("groupId",groupId,"isEnd","N"));
            Timestamp createDate = new Timestamp(System.currentTimeMillis());
            partyQuestion.put("createDate",createDate);
            partyQuestion.create();
        }

        //根据已有的groupId获取一道新的题目

        sql ="SELECT TOP 1 * FROM QUESTION where STATUS='Y' and QUESTION_ID NOT IN (SELECT QUESTION_ID FROM PARTY_QUESTION_ANSWER pqa where pqa.GROUP_ID='"+groupId+"') ORDER BY NEWID()";

        //获得数据库的连接
        Map questionMap= null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            questionMap = null;
            if (rs.next()) {
                String questionId = rs.getString("QUESTION_ID");
                String question = rs.getString("QUESTION");
                String questionType = rs.getString("QUESTION_TYPE");
                questionMap = FastMap.newInstance();
                questionMap.put("questionId",questionId);
                questionMap.put("question",question);
                questionMap.put("questionType",questionType);
                resultData.put("question",questionMap);

            }else{
                resultData.put("retCode", 0);
                resultData.put("message", "题库为空！");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }
        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }finally {
            sqlP.close();
        }

        //获取问题的回答，并且插入一条用户答题记录
        String questionId = (String) questionMap.get("questionId");
        List<GenericValue> answerList =delegator.findByAnd("Answer",UtilMisc.toMap("questionId",questionId));
        questionMap.put("answers",answerList);


        //插入一条数据，之前先获取用户之前的答题的序列号。
        long sequenceId=0L;
        if(isFirst){
            sequenceId=1L;
        }else{
            //查询最大序列号
            sql="select max(SEQUENCE_ID) maxSequenceId from PARTY_QUESTION_ANSWER where GROUP_ID='"+groupId+"'";
            try {
                GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
                sqlP = new SQLProcessor(helperInfo);
                sqlP.executeQuery(sql);

                ResultSet rs = sqlP.getResultSet();
                if (rs.next()) {
                    int maxSequenceId= rs.getInt("maxSequenceId");
                    sequenceId=maxSequenceId+1;
                }else{
                    sequenceId=1L;
                }
            } catch (Exception e) {
                Debug.logError(e, "源生sql异常.", module);
                resultData.put("retCode", 0);
                resultData.put("message", e.getMessage());
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

            }finally {
                sqlP.close();
            }
        }
        GenericValue partyQuestionAnswer = delegator.makeValue("PartyQuestionAnswer");
        partyQuestionAnswer.put("userQuestionId",delegator.getNextSeqId("PartyQuestionAnswer"));
        partyQuestionAnswer.put("partyId",partyId);
        partyQuestionAnswer.put("groupId",groupId);
        partyQuestionAnswer.put("questionId",questionId);
//        partyQuestionAnswer.put("createDate",new Timestamp(System.currentTimeMillis()));
        partyQuestionAnswer.put("sequenceId",sequenceId);
        partyQuestionAnswer.create();


        resultData.put("groupId",groupId);
        resultData.put("status","1");


        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 回答问题
     * @param request
     * @param answerId 问题的答案，多个答案用,分割
     * @param response
     * @param token
     * @return  status 0 回答错误 1回答正确  2回答正确有积分  3回答正确有代金券  4异常 5同时送代金券和积分
     * @throws GenericEntityException
     * @throws SQLException
     */
    @RequestMapping(value = "/answerQuestion", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> answerQuestion(HttpServletRequest request, String groupId,String memberId,String mallId,String questionId,String answerId,HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException, GenericServiceException {

        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getAttribute("userLogin");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(memberId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "memberId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(mallId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "mallId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(answerId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "答案不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String partyId = CommonUtils.getPartyId(delegator, loginName);

        if(groupId==null){
            resultData.put("retCode", 0);
            resultData.put("message", "groupId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }else if(questionId==null){
            resultData.put("retCode", 0);
            resultData.put("message", "questionId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }else if(answerId==null){
            resultData.put("retCode", 0);
            resultData.put("message", "answerId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        //查询这题
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> questionList = delegator.findByAnd("Question",UtilMisc.toMap("questionId",questionId));
        TransactionUtil.commit(beganTransaction);
        if(questionList==null || questionList.size()==0){
            resultData.put("retCode", 0);
            resultData.put("message", "找不到这题！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        beganTransaction = TransactionUtil.begin();
        List<GenericValue> questionAnswerList = delegator.findByAnd("PartyQuestionAnswer",UtilMisc.toMap("questionId",questionId,"partyId",partyId,"groupId",groupId));
        TransactionUtil.commit(beganTransaction);
        if(questionAnswerList==null || questionAnswerList.size()==0){
            resultData.put("retCode", 0);
            resultData.put("message", "找不到这题答题记录！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        GenericValue question  = questionList.get(0);
        String questionType = question.getString("questionType");
        boolean isRight=false;
        if("1".equals(questionType)){
            //画像，所有答案都对
            isRight=true;
            //TODO 打用户标签
            String [] answerIds = answerId.split(",");
            List<String> answerIdList = Arrays.asList(answerIds);
            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("answerId", EntityOperator.IN, answerIdList)
                    )
                    , EntityOperator.AND);
            List<GenericValue> answerList = delegator.findList("Answer",mainCond,null,null,null,false);
            List<GenericValue> partyLabelList = FastList.newInstance();
            for (GenericValue answer:answerList){
                String tagId = answer.getString("tagId");
                GenericValue partyLabel = delegator.makeValue("PartyLabel");
                GenericValue tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", tagId));
                String tagName = tag.get("tagName").toString();
                partyLabel.put("partyLabelId",delegator.getNextSeqId("PartyLabel"));
                partyLabel.put("partyId",partyId);
                partyLabel.put("label",tagName);
                partyLabelList.add(partyLabel);
            }
            delegator.storeAll(partyLabelList);

        }else{
            String questionAnswer = question.getString("result");
            if(questionAnswer.trim().equals(answerId.trim())){
                isRight=true;
            }
        }
        GenericValue questionAnswer = questionAnswerList.get(0);

        if(!isRight){
            beganTransaction = TransactionUtil.begin();
            //说明答案不正确
            //修改状态
            questionAnswer.put("isRight","N");
            questionAnswer.put("answerId",answerId);
            questionAnswer.put("createDate",new Timestamp(System.currentTimeMillis()));
            questionAnswer.store();
            //设置今日不可答题
            delegator.makeValue("PartyQuestionRefuseRecord",UtilMisc.toMap("pqrrId",delegator.getNextSeqId("PartyQuestionRefuseRecord"),"partyId",partyId,"date",new Timestamp(System.currentTimeMillis()))).create();
            TransactionUtil.commit(beganTransaction);
            resultData.put("retCode", 1);
            resultData.put("status", 0);
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        beganTransaction = TransactionUtil.begin();
        //走到这里说明答案正确。
        questionAnswer.put("isRight","Y");
        questionAnswer.put("answerId",answerId);
        questionAnswer.put("createDate",new Timestamp(System.currentTimeMillis()));
        questionAnswer.store();
        //查看答题设置
        GenericValue questionSetting = delegator.findByAnd("QuestionSetting",UtilMisc.toMap("status","1")).get(0);
        TransactionUtil.commit(beganTransaction);
        //每答对多少题送10积分,其他送5积分
        long scoreQuestNums= questionSetting.getLong("scoreQuestNums");
        //送多少积分
//        long scoreNums= 10L;//questionSetting.getLong("scoreNums");
        //每答对多少题送代金券
        long couponQuestNums= questionSetting.getLong("couponQuestNums");
        String questionSettingId = questionSetting.getString("questionId");
        String status="1";
        //查看现在答对了多少题
        SQLProcessor sqlP = null;
        try {
            String sql="SELECT count(*) rightNum from PARTY_QUESTION_ANSWER where PARTY_ID='"+partyId+"' and IS_RIGHT='Y' and GROUP_ID='"+groupId+"'";
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();
            if (rs.next()) {
                long rightNum= rs.getLong("rightNum");
                GenericValue partyScoreDetail = delegator.makeValue("PartyScoreDetail");
                String scoreDetailId=delegator.getNextSeqId("PartyScoreDetail");
                partyScoreDetail.put("scoreDetailId",scoreDetailId);
                partyScoreDetail.put("partyId",partyId);
                partyScoreDetail.put("scoreType","SCORE_TYPE_QUESTION");
                partyScoreDetail.put("valueId",groupId);
                partyScoreDetail.put("operate","A");

                String integralCode= "";
                if(rightNum!=0 && rightNum%scoreQuestNums==0){
                    //送积分了。
                    status="2";
                    partyScoreDetail.put("scoreValue",scoreNums10);
                    resultData.put("scoreNums",scoreNums10);

                    GenericValue integralCodeGV = delegator.findByPrimaryKey("IntegralCode",UtilMisc.toMap("integralCodeId","10001"));
                    if(integralCodeGV!=null){
//                        integralCode="HQ180424050";
                        integralCode=integralCodeGV.getString("integralCodeNo");
                    }
                }else{
                    //每题送积分
                    partyScoreDetail.put("scoreValue",scoreNums05);
                    GenericValue integralCodeGV = delegator.findByPrimaryKey("IntegralCode",UtilMisc.toMap("integralCodeId","10000"));
                    if(integralCodeGV!=null){
//                        integralCode="HQ18042405";
                        integralCode=integralCodeGV.getString("integralCodeNo");
                    }
                }
                partyScoreDetail.create();

                //同步crm
                //测试数据
                String locationId = "";

                //locationId="LHQS";
                //integralCode="HQ-180418-01";

                GenericValue kdMallInfo = delegator.findByPrimaryKey("KdMallInfo",UtilMisc.toMap("mallId",mallId));
                if(kdMallInfo==null){
                    partyScoreDetail.put("syncResult","F");
                    partyScoreDetail.put("syncMessage","查询不到kdmalllinfo表中对应的locationId");
                    partyScoreDetail.store();
                }else{
                    locationId=kdMallInfo.getString("locationId");
                    Map res = dispatcher.runSync("kaide-userAddScore",UtilMisc.toMap("member_id",memberId,"integralCode",integralCode,"locationCode",locationId));
                    if (res == null || res.get("result") == null) {
                        partyScoreDetail.put("syncResult","F");
                        partyScoreDetail.put("syncMessage","接口返回null");
                        partyScoreDetail.store();
                    }else{
                        String resString = (String) res.get("result");
                        JSONObject jsonRes = JSONObject.fromObject(resString);

                        String result = jsonRes.getString("result");
                        if ("7001".equals(result)) {
                            partyScoreDetail.put("syncResult","S");
                            partyScoreDetail.put("syncMessage","成功");
                            partyScoreDetail.store();
                        }else{
                            String errormsg = jsonRes.getString("msg");
                            partyScoreDetail.put("syncResult","F");
                            partyScoreDetail.put("syncMessage",errormsg);
                            partyScoreDetail.store();
                        }
                    }
                }

                //送代金券
                if(rightNum!=0 && rightNum%couponQuestNums==0){
                    if("2".equals(status)){
                        status="5";
                    }else{
                        status="3";
                    }
                    //查找代金券

                    List<GenericValue>  questionCouponSettingList = delegator.findByAnd("QuestionCouponSetting",UtilMisc.toMap("questionId",questionSettingId));
                    List<String> couponIds = FastList.newInstance();
                    for(GenericValue questionCouponSetting: questionCouponSettingList){
                        String couponId = questionCouponSetting.getString("couponId");
                        couponIds.add(couponId);
                    }
                    //查询出所有的优惠券。
                    DynamicViewEntity dynamicView = new DynamicViewEntity();
                    dynamicView.addMemberEntity("PPC","ProductPromoCoupon");
                    dynamicView.addAlias("PPC","couponCode");
                    dynamicView.addAlias("PPC","couponQuantity");
                    dynamicView.addAlias("PPC","userCount");

                    List<EntityCondition> filedExprs = FastList.newInstance();
                    filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("couponCode"), EntityOperator.IN, couponIds));
                    EntityCondition mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
                    // 显示字段集合
                    List<String> fieldsToSelect = FastList.newInstance();
                    fieldsToSelect.add("couponCode");
                    fieldsToSelect.add("couponQuantity");
                    fieldsToSelect.add("userCount");

                    beganTransaction = TransactionUtil.begin();
                    EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, null);
                    List<GenericValue> productCouponList = pli.getCompleteList();
                    TransactionUtil.commit(beganTransaction);
                    pli.close();

                    //从代金券列表中随机选择一条代金券
                    long sum =0;
                    for(GenericValue coupon:productCouponList){
                        long couponQuantity =coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("couponQuantity");
                        sum+=couponQuantity;
                    }
                    if(sum<=0){
                        resultData.put("retCode", 1);
                        resultData.put("status", "4");
                        resultData.put("message", "代金券数量不够！");
                        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
                    }

                    Map coupon = CouponUtils.getUsefulCoupon(productCouponList,delegator,dispatcher,userLogin);
                    if(coupon==null){
                        resultData.put("retCode", 1);
                        resultData.put("status", "4");
                        resultData.put("message", "代金券数量不够！");
                        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
                    }
                    String productPromoCodeId = (String) coupon.get("productPromoCodeId");
                    long payReduce= (long) coupon.get("payReduce");
                    List<GenericValue> productPromoCodeList = delegator.findByAnd("ProductPromoCode",UtilMisc.toMap("productPromoCodeId",productPromoCodeId));
                    GenericValue productPromoCode = productPromoCodeList.get(0);
                    GenericValue productPromoCoupon = productPromoCode.getRelatedOne("ProductPromoCoupon");

                    Map couponInfo = FastMap.newInstance();
                    couponInfo.put("useBeginDate",CommonUtils.getStringDate(productPromoCode.getTimestamp("fromDate")));
                    couponInfo.put("useEndDate",CommonUtils.getStringDate(productPromoCode.getTimestamp("thruDate")));
                    couponInfo.put("couponName",productPromoCoupon.getString("couponName"));
                    couponInfo.put("couponType",productPromoCoupon.getString("couponType"));
                    couponInfo.put("payFill",productPromoCoupon.getString("payFill"));
                    couponInfo.put("payReduce",productPromoCoupon.getString("payReduce"));
                    couponInfo.put("applyScope",productPromoCoupon.getString("applyScope"));
                    couponInfo.put("couponProductType",productPromoCoupon.getString("couponProductType"));
                    couponInfo.put("productPromoCodeId",productPromoCodeId);
                    resultData.put("couponInfo",couponInfo);

                    //添加用户和代金券的关联
                    GenericValue productPromoCodeParty = delegator.makeValue("ProductPromoCodeParty");
                    productPromoCodeParty.put("productPromoCodeId",productPromoCodeId);
                    productPromoCodeParty.put("sourceTypeId","SOURCE_TYPE_REDPACKAGE");
                    productPromoCodeParty.put("partyId",partyId);
                    productPromoCodeParty.put("getDate",new Timestamp(System.currentTimeMillis()));
                    productPromoCodeParty.create();

                    //本次答题结束
                   /* Map<String, Object> updateFields = FastMap.newInstance();
                    updateFields.put("isEnd", "Y");
                    EntityCondition UpdateCon = EntityCondition.makeCondition("groupId", EntityComparisonOperator.EQUALS, groupId);
                    delegator.storeByCondition("PartyQuestion", updateFields, UpdateCon);*/

                }
            }

        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());

            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }finally {
            sqlP.close();
        }

        resultData.put("retCode", 1);
        resultData.put("status", status);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }



    /**
     * 领取代金券
     * @param productPromoCodeId 代金券code
     * @param response status 0获取失败或者该代金券已经被领取 1领取成功
     * @param token
     * @return
     * @throws GenericEntityException
     * @throws SQLException
     */
    @RequestMapping(value = "/acceptCoupon", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> acceptCoupon(HttpServletRequest request, String productPromoCodeId ,HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        List<GenericValue> productPromoCodes = delegator.findByAnd("ProductPromoCode", UtilMisc.toMap("productPromoCodeId",productPromoCodeId,"promoCodeStatus","C"));
        if(productPromoCodes==null || productPromoCodes.size()==0){
            resultData.put("retCode","1");
            resultData.put("status","0");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }
        GenericValue productPromoCode = productPromoCodes.get(0);
        productPromoCode.put("promoCodeStatus","G");
        productPromoCode.store();

        resultData.put("retCode","1");
        resultData.put("status","1");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    /**
     * 当用户做错题目时候需要转发朋友圈后调用此接口才可以继续获得回答机会
     * @param request
     * @param response
     * @param token
     * @return status 1成功
     * @throws GenericEntityException
     * @throws SQLException
     */
    @RequestMapping(value = "/relive", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> relive(HttpServletRequest request ,HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException {

        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        String partyId = CommonUtils.getPartyId(delegator, loginName);
        delegator.removeByAnd("PartyQuestionRefuseRecord","partyId",partyId);

        resultData.put("retCode","1");
        resultData.put("status","1");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }



}
