package com.yuaoq.yabiz.app.mobile.microservice.index.api.v1;

import com.yuaoq.yabiz.app.mobile.microservice.article.api.v1.ArticleV1Controller;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.CouponUtils;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDataSourceException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("all")
@RestController
@RequestMapping(value = "/api/dynamicIndex/v1")
public class DynamicIndexControllerV1 {
    public static final String module = DynamicIndexControllerV1.class.getName();

    @Value("${image.base.url}")
    String baseImgUrl;

    /**
     * 用户行为
     * 团购，随机购物信息，用户拆红包
     */
    String[] userBehavior = {"togetherGroup", "shoppingInfo", "userRedpackage"};
    /**
     * 功能模块
     * 答题，活动推荐，券购买，秒杀，热销商品
     */
    String[] funcModules = {"question", "activity", "couponProduct", "secKill", "hotSaleGoods"};


    //首页打开显示
    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> index(HttpServletRequest request) {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        //已经存在的团购，购物信息，用户拆红包信息，保证不重复显示
        List<String> existTogetherGroup = FastList.newInstance();
        List<String> existShoppingInfo = FastList.newInstance();
        List<String> existUserRedpackage = FastList.newInstance();
        //获取一个推荐活动
        try {
            Map activity = getActivity(delegator, dispatcher);
            //为这个推荐随机获取两个用户行为
            activity.put("behaviors", getRandomBehavior(delegator, dispatcher, existTogetherGroup, existShoppingInfo, existUserRedpackage));

            Map question = getQuestionModule(delegator, dispatcher);
            question.put("behaviors", getRandomBehavior(delegator, dispatcher, existTogetherGroup, existShoppingInfo, existUserRedpackage));
            //购买券
            Map couponProduct = getCouponByModule(delegator, dispatcher);
            couponProduct.put("behaviors", getRandomBehavior(delegator, dispatcher, existTogetherGroup, existShoppingInfo, existUserRedpackage));

            Map secKill = getSeckillModule(delegator, dispatcher);
            secKill.put("behaviors", getRandomBehavior(delegator, dispatcher, existTogetherGroup, existShoppingInfo, existUserRedpackage));

            Map hotSaleGoods = getHotSaleGoodsModule(delegator, dispatcher);
            hotSaleGoods.put("behaviors", getRandomBehavior(delegator, dispatcher, existTogetherGroup, existShoppingInfo, existUserRedpackage));

            resultData.put("activity", activity);
            resultData.put("question", question);
            resultData.put("couponProduct", couponProduct);
            resultData.put("secKill", secKill);
            resultData.put("hotSaleGoods", hotSaleGoods);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (GeneralException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    @RequestMapping(value = "/answerQuestion", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> answerQuestion(HttpServletRequest request, String questionId, String answerId, String isFirst, String memberId, String mallId, JwtAuthenticationToken token) throws IOException, SQLException, GeneralException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(questionId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "questionId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(answerId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "answerId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(isFirst)) {
            resultData.put("retCode", 0);
            resultData.put("message", "isFirst不能为空");
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
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> questionList = delegator.findByAnd("Question", UtilMisc.toMap("questionId", questionId));
        TransactionUtil.commit(beganTransaction);
        if (questionList == null || questionList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "找不到这题！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String partyId = CommonUtils.getPartyId(delegator, loginName);
        if ("Y".equalsIgnoreCase(isFirst)) {
            //送积分
            GenericValue partyScoreDetail = delegator.makeValue("PartyScoreDetail");
            String scoreDetailId = delegator.getNextSeqId("PartyScoreDetail");
            partyScoreDetail.put("scoreDetailId", scoreDetailId);
            partyScoreDetail.put("partyId", partyId);
            partyScoreDetail.put("scoreType", "SCORE_TYPE_QUESTION");
            partyScoreDetail.put("valueId", "");
            partyScoreDetail.put("operate", "A");
            partyScoreDetail.put("scoreValue", 5L);
            partyScoreDetail.create();

            //
            String locationId = "";
            String integralCode = "HQ-180418-01";

            GenericValue kdMallInfo = delegator.findByPrimaryKey("KdMallInfo", UtilMisc.toMap("mallId", mallId));
            if (kdMallInfo == null) {
                partyScoreDetail.put("syncResult", "F");
                partyScoreDetail.put("syncMessage", "查询不到kdmalllinfo表中对应的locationId");
                partyScoreDetail.store();
            } else {
                locationId = kdMallInfo.getString("locationId");
                Map res = dispatcher.runSync("kaide-userAddScore", UtilMisc.toMap("member_id", memberId, "integralCode", integralCode, "locationCode", locationId));
                if (res == null || res.get("result") == null) {
                    partyScoreDetail.put("syncResult", "F");
                    partyScoreDetail.put("syncMessage", "接口返回null");
                    partyScoreDetail.store();
                } else {
                    String resString = (String) res.get("result");
                    JSONObject jsonRes = JSONObject.fromObject(resString);

                    String result = jsonRes.getString("result");
                    if ("7001".equals(result)) {
                        partyScoreDetail.put("syncResult", "S");
                        partyScoreDetail.put("syncMessage", "成功");
                        partyScoreDetail.store();
                    } else {
                        String errormsg = jsonRes.getString("msg");
                        partyScoreDetail.put("syncResult", "F");
                        partyScoreDetail.put("syncMessage", errormsg);
                        partyScoreDetail.store();
                    }
                }
            }

        }
        resultData.put("retCode", 1);


        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    @RequestMapping(value = "/flush", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> flush(HttpServletRequest request) throws IOException, SQLException, GeneralException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        //已经存在的团购，购物信息，用户拆红包信息，保证不重复显示
        List<String> existTogetherGroup = FastList.newInstance();
        List<String> existShoppingInfo = FastList.newInstance();
        List<String> existUserRedpackage = FastList.newInstance();

        String func = funcModules[new Random().nextInt(funcModules.length)];

        Map module = null;
        switch (func) {
            case "question"://团购

                module = getQuestionModule(delegator, dispatcher);
                resultData.put("question", module);
                break;
            case "activity"://团购
                module = getActivity(delegator, dispatcher);
                resultData.put("activity", module);
                break;
            case "couponProduct"://团购
                module = getCouponByModule(delegator, dispatcher);
                resultData.put("couponProduct", module);
                break;
            case "secKill"://团购
                module = getSeckillModule(delegator, dispatcher);
                resultData.put("secKill", module);
                break;
            case "hotSaleGoods"://团购
                module = getHotSaleGoodsModule(delegator, dispatcher);
                resultData.put("hotSaleGoods", module);
                break;
        }

        //获取四个用户行为
        List<Map> behaviors = FastList.newInstance();

        Map map1 = FastMap.newInstance();
        map1.put("behavior", "togetherGroup");
        map1.put("detail", getTogetherGroup(delegator, dispatcher, existTogetherGroup));
        behaviors.add(map1);

        Map map2 = FastMap.newInstance();
        map2.put("behavior", "userRedpackage");
        map2.put("detail", getRepackage(delegator, dispatcher, existUserRedpackage));
        behaviors.add(map2);


        Map map3 = FastMap.newInstance();
        map3.put("behavior", "shoppingInfo");
        map3.put("detail", getShoppingInfo(delegator, dispatcher, existUserRedpackage));
        behaviors.add(map3);

        Map map4 = FastMap.newInstance();
        map4.put("behavior", "togetherGroup");
        map4.put("detail", getTogetherGroup(delegator, dispatcher, existTogetherGroup));
        behaviors.add(map4);

        module.put("behaviors", behaviors);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


    private Map getHotSaleGoodsModule(Delegator delegator, LocalDispatcher dispatcher) {
        String sql = "select TOP 6 PRODUCT_ID productId from PRODUCT_RECOMMEND where status='Y' ORDER BY NEWID()";
        Map resMap = FastMap.newInstance();
        List<Map> hotSaleGoods = null;
        List<String> productIdList = null;
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            resMap.put("exist", "N");
            hotSaleGoods = FastList.newInstance();
            productIdList = FastList.newInstance();
            ResultSet rs = sqlP.getResultSet();
            while (rs.next()) {
                resMap.put("exist", "Y");
                String productId = rs.getString("productId");
                productIdList.add(productId);
            }

        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);
            try {
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
            return null;
        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }

            }
        }
        hotSaleGoods = ProductUtils.getProductsWithOutFeature(productIdList, delegator, dispatcher, baseImgUrl);
        resMap.put("hotSaleGoods", hotSaleGoods);
        return resMap;
    }

    /**
     * 秒杀模块
     *
     * @param delegator
     * @param dispatcher
     * @return
     */
    private Map getSeckillModule(Delegator delegator, LocalDispatcher dispatcher) throws IOException, GeneralException {
        Map resMap = FastMap.newInstance();
        String sql = "SELECT TOP 6\n" +
                "\tpa.ACTIVITY_ID,pa.ACTIVITY_NAME,pa.ACTIVITY_CODE,pa.ACTIVITY_START_DATE,pa.ACTIVITY_END_DATE,pa.LIMIT_QUANTITY,pag.ACTIVITY_QUANTITY,pag.HAS_BUY_QUANTITY,pag.PRODUCT_ID,pag.ACTIVITY_PRICE\n" +
                "FROM\n" +
                "\tPRODUCT_ACTIVITY pa\n" +
                "\tLEFT JOIN PRODUCT_ACTIVITY_GOODS pag on pa.ACTIVITY_ID = pag.ACTIVITY_ID\n" +
                "WHERE\n" +
                "\tpa.ACTIVITY_TYPE = 'SEC_KILL'\n" +
                "AND pa.ACTIVITY_AUDIT_STATUS = 'ACTY_AUDIT_PASS'\n" +
                "AND GETDATE() > pa.ACTIVITY_START_DATE\n" +
                "AND GETDATE() < pa.ACTIVITY_END_DATE\n" +
                "ORDER BY pa.CREATED_STAMP DESC";
        SQLProcessor sqlP = null;
        List<Map> secKillList = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            resMap.put("exist", "N");
            secKillList = FastList.newInstance();
            ResultSet rs = sqlP.getResultSet();
            while (rs.next()) {
                resMap.put("exist", "Y");
                String activityId = rs.getString("ACTIVITY_ID");
                String activityName = rs.getString("ACTIVITY_NAME");
                String activityCode = rs.getString("ACTIVITY_CODE");
                Timestamp activityStartDate = rs.getTimestamp("ACTIVITY_START_DATE");
                Timestamp activityEndDate = rs.getTimestamp("ACTIVITY_END_DATE");
                int limitQuantity = rs.getInt("LIMIT_QUANTITY");
                int activityQuantity = rs.getInt("ACTIVITY_QUANTITY");
                String productId = rs.getString("PRODUCT_ID");
                BigDecimal activityPrice = rs.getBigDecimal("ACTIVITY_PRICE");
                int hasBuyQuantity = rs.getInt("HAS_BUY_QUANTITY");

                Map map = FastMap.newInstance();
                map.put("activityId", activityId);
                map.put("activityName", activityName);
                map.put("activityCode", activityCode);
                map.put("activityStartDate", CommonUtils.getStringDate(activityStartDate));
                map.put("activityEndDate", CommonUtils.getStringDate(activityEndDate));
                map.put("limitQuantity", limitQuantity);//每人限购数量
                map.put("activityQuantity", activityQuantity);
                map.put("activityPrice", activityPrice.doubleValue());
                map.put("hasBuyQuantity", hasBuyQuantity);
                map.put("productId", productId);
//                map.put("productInfo", ProductUtils.getOneProductsWithOutFeature(productId, delegator, dispatcher, baseImgUrl));
                secKillList.add(map);
            }


        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }

            }
        }

        for (Map skillInfo : secKillList) {
            String productId = (String) skillInfo.get("productId");
            skillInfo.put("productInfo", ProductUtils.getOneProductsWithOutFeature(productId, delegator, dispatcher, baseImgUrl));
        }

        resMap.put("secKillList", secKillList);
        return resMap;
    }

    private Map getCouponByModule(Delegator delegator, LocalDispatcher dispatcher) throws IOException, GeneralException {
        Map resMap = FastMap.newInstance();
        String sql = "SELECT  TOP 6 * FROM PRODUCT  where IS_ONLINE='Y' AND IS_DEL='N' AND PRODUCT_TYPE_ID='VIRTUAL_GOOD' ORDER BY INTRODUCTION_DATE DESC";
        List<Map> couponProducts = null;
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();
            resMap.put("exist", "N");
            couponProducts = FastList.newInstance();
            while (rs.next()) {
                resMap.put("exist", "Y");
                Map map = FastMap.newInstance();
                String productId = rs.getString("PRODUCT_ID");
                String productName = rs.getString("PRODUCT_NAME");
                double voucherAmount = rs.getDouble("VOUCHER_AMOUNT");
//                Map productInfo = ProductUtils.getOneProductsWithOutFeature(productId, delegator, dispatcher, baseImgUrl);
//                map.put("productInfo", productInfo);
                map.put("productId", productId);
                map.put("productName", productName);
                map.put("voucherAmount", voucherAmount);
                couponProducts.add(map);
            }

        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Map couponProduct : couponProducts) {
            String productId = (String) couponProduct.get("productId");
            couponProduct.put("productInfo", ProductUtils.getOneProductsWithOutFeature(productId, delegator, dispatcher, baseImgUrl));
        }

        resMap.put("couponProducts", couponProducts);

        return resMap;
    }

    /**
     * 获取一条用户画像题目
     *
     * @param delegator
     * @param dispatcher
     * @return
     */
    private Map getQuestionModule(Delegator delegator, LocalDispatcher dispatcher) {
        String sql = "SELECT TOP 1 * FROM QUESTION where STATUS='Y' AND QUESTION_TYPE='1'  ORDER BY NEWID()";
        Map questionMap = null;
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();
            questionMap = FastMap.newInstance();
            if (rs.next()) {
                questionMap.put("exist", "Y");
                String questionId = rs.getString("QUESTION_ID");
                String question = rs.getString("QUESTION");
                String questionType = rs.getString("QUESTION_TYPE");
                questionMap.put("questionId", questionId);
                questionMap.put("question", question);
                questionMap.put("questionType", questionType);
                List<GenericValue> answerList = delegator.findByAnd("Answer", UtilMisc.toMap("questionId", questionId));
                questionMap.put("answers", answerList);

            } else {
                questionMap.put("exist", "N");
            }
        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);
        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }
            }
        }

        return questionMap;

    }

    /**
     * 随机获取两个特征
     *
     * @param delegator
     * @param dispatcher
     * @param existTogetherGroup
     * @param existShoppingInfo
     * @param existUserRedpackage
     * @return
     */
    private List<Map> getRandomBehavior(Delegator delegator, LocalDispatcher dispatcher, List<String> existTogetherGroup, List<String> existShoppingInfo, List<String> existUserRedpackage) throws SQLException, IOException, GeneralException {
        //随机获取获取两个不同行为
        List<String> exist = FastList.newInstance();
        String b1 = getUserBehavior(null);

        exist.add(b1);
        String b2 = getUserBehavior(exist);

        List<Map> list = FastList.newInstance();
        Map behaviorMap1 = getBehavior(b1, delegator, dispatcher, existTogetherGroup, existShoppingInfo, existUserRedpackage);
        Map behaviorMap2 = getBehavior(b2, delegator, dispatcher, existTogetherGroup, existShoppingInfo, existUserRedpackage);

        list.add(behaviorMap1);
        list.add(behaviorMap2);

        return list;
    }

    //获取一个行为
    public Map getBehavior(String behavior, Delegator delegator, LocalDispatcher dispatcher, List<String> existTogetherGroup, List<String> existShoppingInfo, List<String> existUserRedpackage) throws IOException, SQLException, GeneralException {
        Map map = FastMap.newInstance();
        map.put("behavior", behavior);

        switch (behavior) {
            case "togetherGroup"://团购
                Map res = getTogetherGroup(delegator, dispatcher, existTogetherGroup);
                map.put("detail", res);
                break;
            case "shoppingInfo"://随机购物信息
                Map shoppingRes = getShoppingInfo(delegator, dispatcher, existShoppingInfo);
                map.put("detail", shoppingRes);
                break;
            case "userRedpackage"://用户拆红包信息
                Map redPackageRes = getRepackage(delegator, dispatcher, existUserRedpackage);
                map.put("detail", redPackageRes);
                break;
        }

        return map;
    }

    /**
     * 获取一条用户拆红包信息
     *
     * @param delegator
     * @param dispatcher
     * @param existUserRedpackage
     * @return
     */
    private Map getRepackage(Delegator delegator, LocalDispatcher dispatcher, List<String> existUserRedpackage) {
        String existStr = getSplit(existUserRedpackage);
        String lastDay = CommonUtils.getStringDate(new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000));

        String inSql = "";
        if (UtilValidate.isNotEmpty(existStr)) {
            inSql = " AND USER_PACKAGE_ID NOT IN(" + existStr + ")";
        }
        String sql = "SELECT TOP 1 * FROM USER_RED_PACKAGE where CREATE_DATE >'" + lastDay + "' " + inSql + "  AND GET_NUMS<COUPON_NUMS AND GET_NUMS>1 ORDER BY NEWID()";


        EntityListIterator pli = null;

        SQLProcessor sqlP = null;

        Map resMap = FastMap.newInstance();
        try {

            String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();
            if (rs.next()) {
                resMap.put("exist", "Y");
                resMap.put("isReal", "Y");
                String userPackageId = rs.getString("USER_PACKAGE_ID");
                Timestamp createDate = rs.getTimestamp("CREATE_DATE");
                resMap.put("userPackageId", userPackageId);
                resMap.put("createDate", CommonUtils.getStringDate(createDate));
                existUserRedpackage.add(userPackageId);


            } else {
                resMap.put("exist", "N");

            /*resMap.put("isReal","N");
            //模拟一个红包
            //随机查找2个拆红包模板的代金券金额
            sql="select top 2 * from RED_PACKAGE_COUPON_SETTING where PACKAGE_ID in(select PACKAGE_ID from RED_PACKAGE_SETTING where IS_USED='Y' and STATUS='1') ORDER BY NEWID()";
            conn = ConnectionFactory.getConnection(groupHelperName);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            resMap.put("exist","N");
            List<Map> redpackageDetailList = FastList.newInstance();
            String couponId="";
            while (rs.next()){
                resMap.put("exist","Y");

                couponId = rs.getString("COUPON_ID");
                Map couponInfo = getOneRandomVirtualPerson(delegator);
                Map couponDetail = CouponUtils.getCouponByCouponId(couponId,delegator);
                couponInfo.put("couponInfo",couponDetail);
                redpackageDetailList.add(couponInfo);
            }
            if(redpackageDetailList.size()==1){
                Map couponInfo = getOneRandomVirtualPerson(delegator);
                Map couponDetail = CouponUtils.getCouponByCouponId(couponId,delegator);
                couponInfo.put("couponInfo",couponDetail);
                redpackageDetailList.add(couponInfo);
            }
            resMap.put("createDate",getLatelyTimeStr());
            resMap.put("redpackageDetail",redpackageDetailList);

            stmt.close();
            rs.close();
            conn.close();*/

            }
        } catch (Exception e) {
            Debug.logError(e, "异常.", module);

            return null;
        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }

            }
        }

        if ("Y".equalsIgnoreCase((String) resMap.get("exist"))) {
            boolean beganTransaction = false;
            try {
                beganTransaction = TransactionUtil.begin();
                String userPackageId = (String) resMap.get("userPackageId");
                //获取红包领取详情
                DynamicViewEntity dynamicView = new DynamicViewEntity();
                dynamicView.addMemberEntity("URPD", "UserRedPackageDetail");
                dynamicView.addAlias("URPD", "partyId");
                dynamicView.addAlias("URPD", "userPackageId");
                dynamicView.addAlias("URPD", "userPackageDetailId");
                dynamicView.addAlias("URPD", "couponId");

                EntityCondition mainCond = EntityCondition.makeCondition(
                        UtilMisc.toList(
                                EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL, null),
                                EntityCondition.makeCondition("userPackageId", EntityOperator.EQUALS, userPackageId)
                        )
                        , EntityOperator.AND);

                List<GenericValue> userRedPackageDetailList = null;
                pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, null);
                userRedPackageDetailList = pli.getCompleteList();

                List<Map> redpackageDetailList = FastList.newInstance();
                for (GenericValue redPackageDetail : userRedPackageDetailList) {
                    String couponId = redPackageDetail.getString("couponId");
                    String partyId = redPackageDetail.getString("partyId");

                    Map userInfo = CommonUtils.getUserInfo(delegator, partyId);

                    Map couponInfo = FastMap.newInstance();
                    couponInfo.put("nickname", userInfo.get("nickname"));
                    couponInfo.put("headphoto", userInfo.get("headphoto"));

                    Map couponDetail = CouponUtils.getCouponByCouponId(couponId, delegator);
                    couponInfo.put("couponInfo", couponDetail);
                    redpackageDetailList.add(couponInfo);
                }
                resMap.put("redpackageDetail", redpackageDetailList);
            } catch (GenericEntityException e) {
                e.printStackTrace();
                try {
                    TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
                } catch (Exception e1) {
                    Debug.logError(e1, module);
                }
            } finally {
                try {
                    TransactionUtil.commit(beganTransaction);
                } catch (Exception e) {
                }
            }
        }

        return resMap;

    }

    /**
     * 获取一条24小时内随机购物信息
     *
     * @param delegator
     * @param dispatcher
     * @param existShoppingInfo
     * @return
     */
    private Map getShoppingInfo(Delegator delegator, LocalDispatcher dispatcher, List<String> existShoppingInfo) {
        String existStr = getSplit(existShoppingInfo);
        String lastDay = CommonUtils.getStringDate(new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000));

        String inSql = "";
        if (UtilValidate.isNotEmpty(existStr)) {
            inSql = " AND ORDER_ID NOT IN(" + existStr + ")";
        }
        String sql = "SELECT TOP 1 * FROM ORDER_HEADER  where ORDER_DATE>'" + lastDay + "' " + inSql + "  and STATUS_ID in ('ORDER_HAVEPAY','ORDER_WAITSHIP','ORDER_WAITRECEIVE','ORDER_WAITEVALUATE')  ORDER BY NEWID()";
        Map resMap = null;
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();

            resMap = FastMap.newInstance();

            if (rs.next()) {
                String orderId = rs.getString("ORDER_ID");
                Timestamp date = rs.getTimestamp("ORDER_DATE");
                //找该订单的创建人
                String createPartyId = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId)).get(0).getString("partyId");
                Map userInfo = CommonUtils.getUserInfo(delegator, createPartyId);
                resMap.put("nickname", userInfo.get("nickname"));
                resMap.put("headphoto", userInfo.get("headphoto"));
                BigDecimal orderPrice = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId)).get(0).getBigDecimal("maxAmount");
                //订单价格
                resMap.put("orderPrice", orderPrice.doubleValue());
                //订单使用了多少积分
                List<GenericValue> orderAttributes = delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderId, "attrName", "useIntegral"));
                if (orderAttributes != null && orderAttributes.size() > 0) {
                    resMap.put("scoreValue", orderAttributes.get(0).getString("attrValue"));
                }
                //订单使用了多少钱的优惠券
                List<GenericValue> orderAdjustments = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId, "orderAdjustmentTypeId", "COUPON_ADJUESTMENT"));
                if (orderAdjustments != null && orderAdjustments.size() > 0) {
                    resMap.put("couponValue", orderAdjustments.get(0).getBigDecimal("amount").doubleValue());
                }
                //查找商品明细
                List<GenericValue> orderItemList = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
                List<String> productIdList = FastList.newInstance();
                for (GenericValue orderItem : orderItemList) {
                    productIdList.add(orderItem.getString("productId"));
                }
                List<Map> productList = ProductUtils.getProductsWithOutFeature(productIdList, delegator, dispatcher, baseImgUrl);
                resMap.put("productList", productList);
                resMap.put("createDate", CommonUtils.getStringDate(date));
                resMap.put("isReal", "Y");


                //添加到集合中，避免下次重复
                existShoppingInfo.add(orderId);
            } else {
                //造一个购物行为
                resMap.put("isReal", "N");
                String productId = getRandomSaleProduct(delegator);
                if (productId == null) {
                    resMap.put("exist", "N");
                    return resMap;
                }
                Map person = getOneRandomVirtualPerson(delegator);
                resMap.putAll(person);
                resMap.put("createDate", getLatelyTimeStr());

                Map productInfo = ProductUtils.getOneProductsWithOutFeature(productId, delegator, dispatcher, baseImgUrl);
                if (productInfo != null) {
                    resMap.put("orderPrice", productInfo.get("diffPrice"));
                    resMap.put("price", productInfo.get("price"));
                    resMap.put("scoreValue", productInfo.get("scoreValue"));
                }
                List<Map> productList = FastList.newInstance();
                productList.add(productInfo);
                resMap.put("productList", productList);

            }
        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }

            }
        }
        return resMap;
    }


    /**
     * 随机获取一个正在销售商品
     */
    private String getRandomSaleProduct(Delegator delegator) {
        String sql = "select top 1 PRODUCT_ID from PRODUCT WHERE IS_ONLINE='Y' and IS_DEL='N' ORDER BY NEWID()";
        SQLProcessor sqlP = null;
        try {
            String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            Map resMap = FastMap.newInstance();
            if (rs.next()) {
                String productId = rs.getString("PRODUCT_ID");
                return productId;
            }
        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 获取一个团购信息
     *
     * @param delegator
     * @param dispatcher
     * @param existTogetherGroup
     * @return
     */
    private Map getTogetherGroup(Delegator delegator, LocalDispatcher dispatcher, List<String> existTogetherGroup) {
        String existStr = getSplit(existTogetherGroup);
        String lastDay = CommonUtils.getStringDate(new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000));

        String inSql = "";
        if (UtilValidate.isNotEmpty(existStr)) {
            inSql = " AND TOGETHER_ID NOT IN(" + existStr + ")";
        }
        String sql = "SELECT TOP 1 * FROM TOGETHER_GROUP  where CREATE_DATE>'" + lastDay + "' " + inSql + " and STATUS='TOGETHER_RUNING' and CURRENT_NUM<LIMIT_USER_NUM  ORDER BY NEWID() ";
        SQLProcessor sqlP = null;
        Map resMap = null;

        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();

            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();

            resMap = FastMap.newInstance();

            if (rs.next()) {
                String togetherId = rs.getString("TOGETHER_ID");
                String productId = rs.getString("PRODUCT_ID");
                String createUser = rs.getString("CREATE_USER_ID");
                String createDate = CommonUtils.getStringDate(rs.getTimestamp("CREATE_DATE"));
                Map productInfo = ProductUtils.getOneProductsWithOutFeature(productId, delegator, dispatcher, baseImgUrl);

                String curNum = rs.getString("CURRENT_NUM");
                String limitUser = rs.getString("LIMIT_USER_NUM");
                Map userInfo = CommonUtils.getUserInfo(delegator, createUser);
                resMap.put("isReal", "Y");
                resMap.put("nickname", userInfo.get("nickname"));
                resMap.put("headphoto", userInfo.get("headphoto"));
                resMap.put("createDate", createDate);
                resMap.put("togetherId", togetherId);
                //还剩几人成团
                resMap.put("leaveNum", Integer.parseInt(limitUser) - Integer.parseInt(curNum));

                resMap.put("productInfo", productInfo);
                //获取当前参与人头像信息

                List<GenericValue> togetherGroupRelList = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));
                List<Map> participants = FastList.newInstance();
                for (GenericValue togetherGroupRel : togetherGroupRelList) {
                    String partyId = togetherGroupRel.getString("orderUserId");
                    Map userInfoMap = CommonUtils.getUserInfo(delegator, partyId);
                    Map userInfoTemp = FastMap.newInstance();
                    userInfoTemp.put("nickname", userInfoMap.get("nickname"));
                    userInfoTemp.put("headphoto", userInfoMap.get("headphoto"));
                    participants.add(userInfoTemp);
                }
                resMap.put("participants", participants);

                //添加到集合中，避免下次重复
                existTogetherGroup.add(togetherId);
            } else {
                resMap.put("isReal", "N");

                //获取一个随机的团购商品。
                Map product = getRandomTogetherProduct(delegator);
                if (product == null) {
                    //代表没有团购商品
                    resMap.put("exist", "N");
                    return resMap;
                }
                resMap.put("leaveNum", new Random().nextInt(3) + 1);
                resMap.put("createDate", getLatelyTimeStr());
                String productId = (String) product.get("productId");
                double price = (double) product.get("price");
                Map productInfo = ProductUtils.getOneTogetherProductsWithOutFeature(productId, price, delegator, dispatcher, baseImgUrl);
                Map person = getOneRandomVirtualPerson(delegator);
                resMap.putAll(person);
                resMap.put("productInfo", productInfo);
                //虚拟的参与人
                resMap.put("participants", getRandomVirtualPerson(delegator));

            }
            TransactionUtil.commit(beganTransaction);
        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        } finally {
            if (sqlP != null) {
                try {
                    sqlP.close();
                } catch (GenericDataSourceException e) {
                    e.printStackTrace();
                }

            }
        }
        return resMap;
    }

    //得到一个24小时内的随机时间
    public String getLatelyTimeStr() {
        int hours = new Random().nextInt(10) + 1;
        String date = CommonUtils.getStringDate(new Timestamp(System.currentTimeMillis() - hours * 60 * 60 * 1000));
        return date;
    }

    /**
     * 将集合里面每个字符串用逗号分割拼接
     *
     * @param list
     * @return
     */
    public String getSplit(List<String> list) {
        if (list == null || list.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                sb.append(list.get(i));
            } else {
                sb.append(",").append(list.get(i));
            }
        }
        return sb.toString();
    }

    /**
     * 获取一个用户行为
     *
     * @param exist 已经存在的行为，保证不重复
     * @return
     */
    public String getUserBehavior(List<String> exist) {
        int behaviorSize = userBehavior.length;
        if (exist == null || exist.size() == 0) {
            //随机返回一个
            return userBehavior[new Random().nextInt(behaviorSize)];
        }
        String behavior = userBehavior[new Random().nextInt(behaviorSize)];
        if (exist.contains(behavior)) {
            return getUserBehavior(exist);
        }
        return behavior;
    }

    /**
     * 获取一个活动
     *
     * @return
     */
    public Map getActivity(Delegator delegator, LocalDispatcher dispatcher) throws SQLException, GeneralException, IOException {
        //目前先随机获取一个活动，后期改成获取微信小程序站点的活动 TODO
        String sql = "SELECT TOP 1 * FROM PRODUCT_TOPIC_ACTIVITY WHERE IS_USE='0'  ORDER BY NEWID() ";
        Map resMap = null;


        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            resMap = FastMap.newInstance();

            if (rs.next()) {
                String imgId = rs.getString("BIG_IMG");
                String activityName = rs.getString("TOPIC_ACTIVITY_NAME");
                //链接类型，FLT_SPLJ 商品链接  FLT_WZLJ 文章链接
                String linkType = rs.getString("LINK_TYPE");
                String linkId = rs.getString("LINK_ID");

                String imgUrl = CommonUtils.getImgUrl(delegator, dispatcher, imgId, baseImgUrl);

                resMap.put("imgUrl", imgUrl);
                resMap.put("activityName", activityName);
                resMap.put("linkType", linkType);
                resMap.put("linkId", linkId);
                resMap.put("exist", "Y");
            } else {
                resMap.put("exist", "N");
            }


        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        } finally {
            sqlP.close();
        }

        return resMap;
    }

    /**
     * 获取一个随机的在有效期内的团购商品
     *
     * @param delegator
     * @return
     */
    public Map getRandomTogetherProduct(Delegator delegator) throws GenericEntityException {

        List<Map> productList = FastList.newInstance();
        //在规定的时间内
        Timestamp nowTime = UtilDateTime.nowTimestamp();

        List<EntityCondition> andExprs2 = FastList.newInstance();
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityType"), EntityOperator.EQUALS, "GROUP_ORDER"));
        EntityCondition mainCond2 = EntityCondition.makeCondition(andExprs2, EntityOperator.AND);
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> actList = null;
        EntityListIterator pli2 = null;
        try {
            pli2 = delegator.find("ProductStorePromoAndAct", mainCond2, null, null, null, null);
            actList = pli2.getCompleteList();
        } catch (Exception e) {
            return null;
        } finally {
            TransactionUtil.commit(beganTransaction);
            pli2.close();
        }
        if (actList != null && actList.size() > 0) {
            for (GenericValue act : actList) {
                String activityId = act.getString("activityId");
                List<GenericValue> acts = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
                if (acts != null && acts.size() > 0) {
                    for (GenericValue act1 : acts) {
                        String productId = act1.getString("productId");
                        BigDecimal price = act1.getBigDecimal("activityPrice");
                        Map map = FastMap.newInstance();
                        map.put("productId", productId);
                        map.put("price", price == null ? 0 : price.doubleValue());
                        productList.add(map);
                    }
                }
            }
        }

        if (productList.size() == 0) {
            return null;
        }
        //随机选择一个团购商品返回
        return productList.get(new Random().nextInt(productList.size()));
    }

    /**
     * 随机获取虚拟用户
     *
     * @param delegator
     * @return
     */
    public List<Map> getRandomVirtualPerson(Delegator delegator) throws SQLException, GenericEntityException {

        int personNum = new Random().nextInt(3) + 2;
        String sql = "select top " + personNum + " * from VITUAL_PERSON ORDER BY NEWID()";

        List<Map> list = null;
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();

            Map resMap = FastMap.newInstance();
            list = FastList.newInstance();
            while (rs.next()) {
                String nickname = rs.getString("NICK_NAME");
                String headphoto = rs.getString("IMG_URL");
                Map map = FastMap.newInstance();
                map.put("nickname", nickname);
                map.put("headphoto", headphoto);
                list.add(map);
            }

        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        }finally {
            sqlP.close();
        }

        return list;
    }

    public Map getOneRandomVirtualPerson(Delegator delegator) throws SQLException, GenericEntityException {

        String sql = "select top 1 * from VITUAL_PERSON ORDER BY NEWID()";

        Map map = null;
        SQLProcessor sqlP = null;

        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            Map resMap = FastMap.newInstance();
            map = FastMap.newInstance();
            if (rs.next()) {
                String nickname = rs.getString("NICK_NAME");
                String headphoto = rs.getString("IMG_URL");
                map.put("nickname", nickname);
                map.put("headphoto", headphoto);
            }

        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        }finally {
            sqlP.close();
        }


        return map;
    }

}
