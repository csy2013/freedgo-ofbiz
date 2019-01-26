/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.qihua.ofbiz.question;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;


/**
 * QuestionBank service
 * @author zhajh 2018/04/13
 *
 */
public class QuestionBankServices {
    public static final String module = QuestionBankServices.class.getName();
    public static final String resource = "ProductUiLabels";

    /*************************************题库管理***********************************/
    /**
     * 题库管理列表查询
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getQuestionBankList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> questionBankList = FastList.newInstance();
        //总记录数
        int questionBankListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        String questionId = (String) context.get("questionId"); // 题目编码
        String questionType = (String) context.get("questionType"); // 题库类型
        String question = (String) context.get("question"); // 题干
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("Q","Question");
        dynamicView.addAlias("Q","questionId");
        dynamicView.addAlias("Q","question");
        dynamicView.addAlias("Q","result");
        dynamicView.addAlias("Q","questionType");
        dynamicView.addAlias("Q","status");

        fieldsToSelect.add("questionId");
        fieldsToSelect.add("question");
        fieldsToSelect.add("result");
        fieldsToSelect.add("questionType");
        fieldsToSelect.add("status");

        // 题库编码
        if(UtilValidate.isNotEmpty(questionId)){
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("questionId"), EntityOperator.LIKE, EntityFunction.UPPER("%"+questionId+"%")));
        }
        // 题目类型
        if(UtilValidate.isNotEmpty(questionType)){
            filedExprs.add(EntityCondition.makeCondition("questionType", questionType));
        }
        // 题干
        if(UtilValidate.isNotEmpty(question)){
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("question"), EntityOperator.LIKE, EntityFunction.UPPER("%"+question+"%")));
        }
        filedExprs.add(EntityCondition.makeCondition("status", "Y"));

        //排序字段名称
        String sortField = "questionId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String)context.get("sortType");
        }
        orderBy.add(sortType+sortField);

        //添加where条件
        if (filedExprs.size() > 0){
            mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            List <GenericValue> curStoreRecomendList=FastList.newInstance();
            curStoreRecomendList=pli.getPartialList(lowIndex, viewSize);
            for(GenericValue gv : curStoreRecomendList){

                String curQuestionId = gv.getString("questionId");
                String curQuestion = gv.getString("question");
                String curPuestionType = gv.getString("questionType");
                String answerResult = gv.getString("result");
                String status = gv.getString("status");

                Map map = FastMap.newInstance();
                map.put("questionId", curQuestionId);
                map.put("question", curQuestion);
                map.put("questionType",curPuestionType);
                map.put("result", answerResult);
                map.put("status", status);

                questionBankList.add(map);
            }

            // 获取总记录数
            questionBankListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > questionBankListSize) {
                highIndex = questionBankListSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }


        //返回的参数
        result.put("questionBankList",questionBankList);
        result.put("totalSize", Integer.valueOf(questionBankListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }


    /**
     * 题库管理新增
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> questionBankAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String question = (String)context.get("question");
        String questionType = (String)context.get("questionType");
        String answerInfos = (String)context.get("answerInfos");
        String answerResult=(String)context.get("answerResult");

        String[] curAnswerInfos = null;// 答题选项内容数组

        List<String> answerResultInfos=FastList.newInstance(); // 选择答案序号列表
        List<Map<String, Object>> answerInfoList = FastList.newInstance();//答题信息列表

        // 确取得答题信息
        if (UtilValidate.isNotEmpty(answerInfos)) {
            String[] tAnswerInfosArray = answerInfos.split(",");
            for (String answerInfo : tAnswerInfosArray) {
                String[] attrInfos = answerInfo.split("\\^");
                String answerSeqId = attrInfos[0];//答题序号
                String answerContent = attrInfos[1];//答题内容
                String answerTagId="";//标签编码
                if("1".equals(questionType)) {
                    answerTagId=attrInfos[2];
                }

                Map<String, Object> mapTemp=null;
                if (UtilValidate.isNotEmpty(answerSeqId) && UtilValidate.isNotEmpty(answerContent)) {
                    mapTemp = FastMap.newInstance();
                    mapTemp.put("answerSeqId", answerSeqId);
                    mapTemp.put("answerContent", answerContent);
                    mapTemp.put("answerTagId",answerTagId);
                    answerInfoList.add(mapTemp);
                }
            }
        }

        // 取得答案序号列表
        if(UtilValidate.isNotEmpty(answerResult)){
            for(String answerResultInfo : answerResult.split(",")){
                answerResultInfos.add(answerResultInfo);
            }
        }

        List<GenericValue> allStore = new LinkedList<GenericValue>();
        EntityCondition mainCond = null;
        try {

            // 新增一条题库信息
            String createQuestionId = delegator.getNextSeqId("Question");
            //新增一条题库记录
            GenericValue questionBank_gv = delegator.makeValue("Question", UtilMisc.toMap("questionId",createQuestionId));
            questionBank_gv.setString("question",question);
            questionBank_gv.setString("questionType", questionType);
            questionBank_gv.setString("result","");
            questionBank_gv.setString("status", "Y");
//            questionBank_gv.create();
            List<String> answerIdList=FastList.newInstance();
            if(UtilValidate.isNotEmpty(answerInfoList)){
                Map<String,Object> curAnswerInfo=FastMap.newInstance();
                for(int i=0;i<answerInfoList.size();i++){
                    curAnswerInfo=FastMap.newInstance();
                    curAnswerInfo=answerInfoList.get(i);
                    String createAnsewerId = delegator.getNextSeqId("Answer");
                    answerIdList.add(createAnsewerId);
                    //新增一条答案记录
                    GenericValue answer_gv = delegator.makeValue("Answer", UtilMisc.toMap("answerId",createAnsewerId));
                    answer_gv.setString("questionId",createQuestionId);
                    answer_gv.setString("sequenceId",(String)curAnswerInfo.get("answerSeqId"));
                    answer_gv.setString("answer", (String)curAnswerInfo.get("answerContent"));
                    // 标签编码
                    String curTagId=(String)curAnswerInfo.get("answerTagId");
                    if("1".equals(questionType)) {
                        GenericValue tagInfo = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", curTagId));
                        String tagId = "";
                        if (UtilValidate.isEmpty(tagInfo)) {
                            tagId = delegator.getNextSeqId("Tag");
                            GenericValue Tag = delegator.makeValue("Tag");
                            Tag.set("tagId", tagId);
                            Tag.set("tagTypeId", "QuestionTag");
                            Tag.set("tagRemark", "题库中添加");
                            Tag.set("tagName", curTagId);
                            Tag.set("isDel", "N");
                            Tag.create();
                        } else {
                            tagId = curTagId;
                        }
                        answer_gv.setString("tagId", tagId);
                    }
//                    answer_gv.create();
                    allStore.add(answer_gv);
                }
            }
            String answerIds="";
            if(UtilValidate.isNotEmpty(answerResultInfos)){
                for(String cur_seq : answerResultInfos){
                    for(int i=0;i<answerIdList.size();i++){
                        String curIndex=(i+1)+"";
                        if(cur_seq.equals(curIndex)){
                            if(UtilValidate.isEmpty(answerIds)){
                                answerIds+=answerIdList.get(i);
                            }else{
                                answerIds+=","+answerIdList.get(i);
                            }
                        }
                    }
                }
            }
            questionBank_gv.setString("result",answerIds);
            allStore.add(questionBank_gv);
            delegator.storeAll(allStore);

//            GenericValue question_gv = delegator.findByPrimaryKey("Question", UtilMisc.toMap("questionId",createQuestionId));
//            if(UtilValidate.isNotEmpty(question_gv)){
//                question_gv.setString("result",answerIds);
//                question_gv.store();
//            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据ID删除题库管理信息可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> questionBankDel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String)context.get("ids");
        //转换成list
        List idList = FastList.newInstance();
        for(String id : ids.split(",")){
            idList.add(id);
        }
        //编辑where条件
        EntityCondition mainCond = EntityCondition.makeCondition("questionId", EntityOperator.IN,idList);
        try {

            EntityListIterator pli=delegator.find("Question",mainCond,null,null,null,null);
            List<GenericValue> questionBankList = pli.getCompleteList();
            for(GenericValue questionBankInfo:questionBankList){
                if(UtilValidate.isNotEmpty(questionBankInfo)){
                    questionBankInfo.setString("status","N");
                    questionBankInfo.store();
                }
            }
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据编码取得题库管理信息
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> questionBankEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String questionId = (String)context.get("questionId");
        try {
            //根据题库编码取得一条question信息
            GenericValue question_gv = delegator.findByPrimaryKey("Question", UtilMisc.toMap("questionId",questionId));

            result.put("questionId",question_gv.get("questionId"));
            result.put("question",question_gv.get("question"));
            result.put("answerResult",question_gv.get("result"));
            result.put("questionType", question_gv.get("questionType"));

            //答案信息
            String _arr="";
            List<GenericValue> answer_list = FastList.newInstance();
            answer_list = delegator.findByAnd("Answer",UtilMisc.toMap("questionId", questionId));
            result.put("answerList", answer_list);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 题库管理编辑处理
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> questionBankEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String questionId = (String)context.get("questionId");// 题库编码
        String question = (String)context.get("question");// 题干
        String questionType = (String)context.get("questionType"); // 题库类型
        String answerInfos = (String)context.get("answerInfos"); // 答题选项
        String answerResult = (String)context.get("answerResult");// 答案

        String[] curAnswerInfos = null;// 答题选项内容数组

        List<String> answerResultInfos=FastList.newInstance(); // 选择答案序号列表
        List<Map<String, Object>> answerInfoList = FastList.newInstance();//答题信息列表


        // 确取得答题信息
        if (UtilValidate.isNotEmpty(answerInfos)) {
            String[] tAnswerInfosArray = answerInfos.split(",");
            for (String answerInfo : tAnswerInfosArray) {
                String[] attrInfos = answerInfo.split("\\^");
                String answerSeqId = attrInfos[0];//答题序号
                String answerContent = attrInfos[1];//答题内容
                String answerTagId="";//标签编码
                if("1".equals(questionType)) {
                    answerTagId=attrInfos[2];
                }
                Map<String, Object> mapTemp=null;
                if (UtilValidate.isNotEmpty(answerSeqId) && UtilValidate.isNotEmpty(answerContent)) {
                    mapTemp = FastMap.newInstance();
                    mapTemp.put("answerSeqId", answerSeqId);
                    mapTemp.put("answerContent", answerContent);
                    mapTemp.put("answerTagId",answerTagId);
                    answerInfoList.add(mapTemp);
                }
            }
        }

        // 取得答案序号列表
        if(UtilValidate.isNotEmpty(answerResult)){
            for(String answerResultInfo : answerResult.split(",")){
                answerResultInfos.add(answerResultInfo);
            }
        }
        try {
            //取得一条题库管理记录
            GenericValue questionBank_gv = delegator.findByPrimaryKey("Question", UtilMisc.toMap("questionId",questionId));

            if(UtilValidate.isNotEmpty(questionBank_gv)){
                questionBank_gv.setString("question",question);
                questionBank_gv.setString("questionType",questionType);
                // 更新答题选项内容
                Long curSequenceNum=Long.valueOf("1");
                List<String> answerIdList=FastList.newInstance();
                if(UtilValidate.isNotEmpty(answerInfoList)){
                    //删除该题干的所有答题选项
                    delegator.removeByAnd("Answer", UtilMisc.toMap("questionId",questionId));
                    Map<String,Object> curAnswerInfo=FastMap.newInstance();
                    for(int i=0;i<answerInfoList.size();i++){
                        curSequenceNum=curSequenceNum+i;
                        curAnswerInfo=FastMap.newInstance();
                        curAnswerInfo=answerInfoList.get(i);
                        String createAnsewerId = delegator.getNextSeqId("Answer");
                        answerIdList.add(createAnsewerId);
                        //新增一条答案记录
                        GenericValue answer_gv = delegator.makeValue("Answer", UtilMisc.toMap("answerId",createAnsewerId));
                        answer_gv.setString("questionId",questionId);
                        answer_gv.setString("sequenceId", (String)curAnswerInfo.get("answerSeqId"));
                        answer_gv.setString("answer", (String)curAnswerInfo.get("answerContent"));
                        // 标签编码
                        String curTagId=(String)curAnswerInfo.get("answerTagId");
                        if("1".equals(questionType)) {
                            GenericValue tagInfo = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", curTagId));
                            String tagId = "";
                            if (UtilValidate.isEmpty(tagInfo)) {
                                tagId = delegator.getNextSeqId("Tag");
                                GenericValue Tag = delegator.makeValue("Tag");
                                Tag.set("tagId", tagId);
                                Tag.set("tagTypeId", "QuestionTag");
                                Tag.set("tagRemark", "题库中添加");
                                Tag.set("tagName", curTagId);
                                Tag.set("isDel", "N");
                                Tag.create();
                            } else {
                                tagId = curTagId;
                            }
                            answer_gv.setString("tagId", tagId);
                        }
                        answer_gv.create();
                    }
                }

                String answerIds="";
                if(UtilValidate.isNotEmpty(answerResultInfos)){
                    for(String cur_seq : answerResultInfos){
                        for(int i=0;i<answerIdList.size();i++){
                            String curIndex=(i+1)+"";
                            if(cur_seq.equals(curIndex)){
                                if(UtilValidate.isEmpty(answerIds)){
                                    answerIds+=answerIdList.get(i);
                                }else{
                                    answerIds+=","+answerIdList.get(i);
                                }
                            }
                        }
                    }
                }
                questionBank_gv.setString("result",answerIds);
                questionBank_gv.store();
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 题库导入
     *
     * @param dctx
     * @param context
     * @return
     */
    public static void questionImport(HttpServletRequest request, HttpServletResponse response) {
        //返回信息，json格式
        String returnJson = "";
        //当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        //Delegator对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            //调用Excel导入方法
            Map rs = dispatcher.runSync("excelImport", UtilMisc.toMap(
                    "request", request,
                    "xmlUrl", "src/com/qihua/ofbiz/question/QuestionValidate.xml",
                    "validateCellData", "questionValidateCell"));
            //获取导入的信息
            returnJson = rs.get("msg").toString();
            //获取导入的数据list
            List<Map> listDatas = (List<Map>) rs.get("listDatas");
            //遍历list，进行新增或修改操作

            for (Map record : listDatas) {

                String question ="";   // 题干
                String questionType="";// 类型
                String answerResult="";// 答案

                String answerItemA="";// 选项A
                String tagA="";       // 标签A
                String answerItemB="";// 选项B
                String tagB="";       // 标签B
                String answerItemC="";// 选项C
                String tagC="";       // 标签C
                String answerItemD="";// 选项D
                String tagD="";       // 标签D
                String answerItemE="";// 选项E
                String tagE="";       // 标签E
                String answerItemF="";// 选项F
                String tagF="";       // 标签F
                String answerItemG="";// 选项G
                String tagG="";       // 标签G
                String answerItemH="";// 选项H
                String tagH="";       // 标签H

                question = (String) record.get("question");
                questionType ="0";
                if(UtilValidate.isNotEmpty(record.get("questionType"))) {
                    if ("画像".equals((String) record.get("questionType"))) {
                        questionType = "1";
                    }
                }
                answerResult = (String) record.get("result");
                answerItemA = (String) record.get("answerItemA");
                tagA = (String) record.get("tagA");
                answerItemB = (String) record.get("answerItemB");
                tagB = (String) record.get("tagB");
                answerItemC = (String) record.get("answerItemC");
                tagC = (String) record.get("tagC");
                answerItemD = (String) record.get("answerItemD");
                tagD = (String) record.get("tagD");
                answerItemE = (String) record.get("answerItemE");
                tagE = (String) record.get("tagE");
                answerItemF = (String) record.get("answerItemF");
                tagF = (String) record.get("tagF");
                answerItemG = (String) record.get("answerItemG");
                tagG = (String) record.get("tagG");
                answerItemH = (String) record.get("answerItemH");
                tagH = (String) record.get("tagH");


                List<Map<String, Object>> answerInfoList = FastList.newInstance();//答题信息列表
                Map<String, Object> mapTemp=FastMap.newInstance();

                // 确取得答题信息
                if(UtilValidate.isNotEmpty(answerItemA)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemA);
                    if(UtilValidate.isNotEmpty(tagA)){
                        mapTemp.put("answerTagId", tagA);
                    }
                    answerInfoList.add(mapTemp);
                }
                if(UtilValidate.isNotEmpty(answerItemB)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemB);
                    if(UtilValidate.isNotEmpty(tagB)){
                        mapTemp.put("answerTagId", tagB);
                    }
                    answerInfoList.add(mapTemp);
                }
                if(UtilValidate.isNotEmpty(answerItemC)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemC);
                    if(UtilValidate.isNotEmpty(tagC)){
                        mapTemp.put("answerTagId", tagC);
                    }
                    answerInfoList.add(mapTemp);
                }
                if(UtilValidate.isNotEmpty(answerItemD)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemD);
                    if(UtilValidate.isNotEmpty(tagD)){
                        mapTemp.put("answerTagId", tagD);
                    }
                    answerInfoList.add(mapTemp);
                }
                if(UtilValidate.isNotEmpty(answerItemE)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemE);
                    if(UtilValidate.isNotEmpty(tagE)){
                        mapTemp.put("answerTagId", tagE);
                    }
                    answerInfoList.add(mapTemp);
                }
                if(UtilValidate.isNotEmpty(answerItemF)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemF);
                    if(UtilValidate.isNotEmpty(tagF)){
                        mapTemp.put("answerTagId", tagF);
                    }
                    answerInfoList.add(mapTemp);
                }
                if(UtilValidate.isNotEmpty(answerItemG)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemG);
                    if(UtilValidate.isNotEmpty(tagG)){
                        mapTemp.put("answerTagId", tagG);
                    }
                    answerInfoList.add(mapTemp);
                }
                if(UtilValidate.isNotEmpty(answerItemH)){
                    mapTemp=FastMap.newInstance();
                    mapTemp.put("answerContent", answerItemH);
                    if(UtilValidate.isNotEmpty(tagH)){
                        mapTemp.put("answerTagId", tagH);
                    }
                    answerInfoList.add(mapTemp);
                }

                if(UtilValidate.isNotEmpty(answerInfoList)){
                    for(int i=0;i<answerInfoList.size();i++){
                        if(UtilValidate.isNotEmpty(answerInfoList.get(i))){
                            answerInfoList.get(i).put("answerSeqId",(i+1)+"");
                        }
                    }
                }
                List<String> answerResultInfos=FastList.newInstance(); // 选择答案序号列表
                // 取得答案序号列表
                if(UtilValidate.isNotEmpty(answerResult)){
                    for(String answerResultInfo : answerResult.split(",")){
                        answerResultInfos.add(getAnswerSeqIdByCode(answerResultInfo));
                    }
                }

                List<GenericValue> allStore = new LinkedList<GenericValue>();
                List<String> curIdList = FastList.newInstance();// 插入用编码列表
                List<String> curErrIdList = FastList.newInstance();// 重复的编码列表
                Boolean chk=true;
                List<EntityCondition> andExprs = FastList.newInstance();
                EntityCondition mainCond = null;

                // 新增一条题库信息
                String createQuestionId = delegator.getNextSeqId("Question");
                //新增一条题库记录
                GenericValue questionBank_gv = delegator.makeValue("Question", UtilMisc.toMap("questionId",createQuestionId));

                questionBank_gv.setString("question",question);
                questionBank_gv.setString("questionType", questionType);
                questionBank_gv.setString("result","");
                questionBank_gv.setString("status", "Y");

                List<String> answerIdList=FastList.newInstance();
                if(UtilValidate.isNotEmpty(answerInfoList)){
                    Map<String,Object> curAnswerInfo=FastMap.newInstance();
                    for(int i=0;i<answerInfoList.size();i++){
                        curAnswerInfo=FastMap.newInstance();
                        curAnswerInfo=answerInfoList.get(i);
                        String createAnsewerId = delegator.getNextSeqId("Answer");
                        answerIdList.add(createAnsewerId);
                        //新增一条答案记录
                        GenericValue answer_gv = delegator.makeValue("Answer", UtilMisc.toMap("answerId",createAnsewerId));
                        answer_gv.setString("questionId",createQuestionId);
                        answer_gv.setString("sequenceId",(String)curAnswerInfo.get("answerSeqId"));
                        answer_gv.setString("answer", (String)curAnswerInfo.get("answerContent"));
                        // 标签编码
                        String curTagId=(String)curAnswerInfo.get("answerTagId");
                        if("1".equals(questionType)) {
                            GenericValue tagInfo = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", curTagId));
                            String tagId = "";
                            if (UtilValidate.isEmpty(tagInfo)) {
                                tagId = delegator.getNextSeqId("Tag");
                                GenericValue Tag = delegator.makeValue("Tag");
                                Tag.set("tagId", tagId);
                                Tag.set("tagTypeId", "QuestionTag");
                                Tag.set("tagRemark", "题库中添加");
                                Tag.set("tagName", curTagId);
                                Tag.set("isDel", "N");
                                Tag.create();
                            } else {
                                tagId = curTagId;
                            }
                            answer_gv.setString("tagId", tagId);
                        }
                        allStore.add(answer_gv);
                    }
                }
                String answerIds="";
                if(UtilValidate.isNotEmpty(answerResultInfos)){
                    for(String cur_seq : answerResultInfos){
                        for(int i=0;i<answerIdList.size();i++){
                            String curIndex=(i+1)+"";
                            if(cur_seq.equals(curIndex)){
                                if(UtilValidate.isEmpty(answerIds)){
                                    answerIds+=answerIdList.get(i);
                                }else{
                                    answerIds+=","+answerIdList.get(i);
                                }
                            }
                        }
                    }
                }
                questionBank_gv.setString("result",answerIds);
                allStore.add(questionBank_gv);
                delegator.storeAll(allStore);
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.write(returnJson);
            out.flush();
            out.close();
        }
    }


    /**
     * 根据Code取得答案序号
     * @param code
     * @return
     */
    private static String getAnswerSeqIdByCode(String code){
        String resultSeqId="";
        switch(code){
            case "A":
                resultSeqId="1";
                break;
            case "B":
                resultSeqId="2";
                break;
            case "C":
                resultSeqId="3";
                break;
            case "D":
                resultSeqId="4";
                break;
            case "E":
                resultSeqId="5";
                break;
            case "F":
                resultSeqId="6";
                break;
            case "G":
                resultSeqId="7";
                break;
            case "H":
                resultSeqId="8";
                break;
            default:
                break;
        }
        return resultSeqId;
    }
    // Add by zhajh at 20180417 题库试题导入 End
}
