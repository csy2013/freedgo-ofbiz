/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.qihua.ofbiz.customer;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * customer service
 *
 * @author AlexYao 2016/03/30
 */
public class CustomerServices {
    public static final String module = CustomerServices.class.getName();
    public static final String resource = "ProductUiLabels";


    /**
     * 咨询列表数据:查询条件(用户昵称,商品名称,咨询类型,是否回复,商家,内容)
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findConsult(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Locale locale = (Locale) context.get("locale");

        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));


        List<Map> consultList = FastList.newInstance();
        int consultListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("CON", "Consult");
        dynamicView.addAlias("CON", "consultId");
        dynamicView.addAlias("CON", "productId");
        dynamicView.addAlias("CON", "createPartyId");
        dynamicView.addAlias("CON", "replyPartyId");
        dynamicView.addAlias("CON", "consultType");
        dynamicView.addAlias("CON", "replyDate");
        dynamicView.addAlias("CON", "consultContent");
        dynamicView.addAlias("CON", "isShow");
        dynamicView.addAlias("CON", "createDate");
        dynamicView.addAlias("CON", "replyContent");
        dynamicView.addAlias("CON", "isShowReply");

        dynamicView.addMemberEntity("PER", "Person");
        dynamicView.addAlias("PER", "nickname");

        dynamicView.addMemberEntity("PRO", "Product");
        dynamicView.addAlias("PRO", "productName");

        dynamicView.addMemberEntity("PARB", "PartyBusiness");
        dynamicView.addAlias("PARB", "businessName");

        dynamicView.addViewLink("CON", "PER", Boolean.FALSE, ModelKeyMap.makeKeyMapList("createPartyId", "partyId"));
        dynamicView.addViewLink("CON", "PRO", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicView.addViewLink("PRO", "PARB", Boolean.FALSE, ModelKeyMap.makeKeyMapList("businessPartyId", "partyId"));

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("consultId");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("consultType");
        fieldsToSelect.add("createPartyId");
        fieldsToSelect.add("nickname");
        fieldsToSelect.add("consultContent");
        fieldsToSelect.add("isShow");
        fieldsToSelect.add("createDate");
        fieldsToSelect.add("businessName");
        fieldsToSelect.add("replyPartyId");
        fieldsToSelect.add("replyDate");
        fieldsToSelect.add("replyContent");
        fieldsToSelect.add("isShowReply");

        String sortField = "consultId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        //根据昵称模糊查询
        if (UtilValidate.isNotEmpty(context.get("nickname"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("nickname"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("nickname") + "%")));
        }

        //根据商品名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("productName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("productName") + "%")));
        }

        //根据咨询类型查询
        if (UtilValidate.isNotEmpty(context.get("consultType"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("consultType"), EntityOperator.EQUALS, context.get("consultType")));
        }
        //根据是否回复查询
        if (UtilValidate.isNotEmpty(context.get("isReply")) && "Y".equals(context.get("isReply"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("replyDate"), EntityOperator.NOT_EQUAL, null));
        }

        //根据店铺名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("businessName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("businessName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("businessName") + "%")));
        }

        //根据咨询内容模糊查询
        if (UtilValidate.isNotEmpty(context.get("consultContent"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("consultContent"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("consultContent") + "%")));
        }

        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

            int i = 0;
            //遍历查询结果集
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map record = FastMap.newInstance();
                record.put("index", i++);
                String consultId = gv.getString("consultId");
                String productId = gv.getString("productId");
                Map map = null;
                try {
                    map = dispatcher.runSync("getProductContentAsText", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                }
                String imageUrl = (String) map.get("imgUrl");
                String productName = gv.getString("productName");
                String consultType = gv.getString("consultType");
                String createPartyId = gv.getString("createPartyId");
                String nickname = gv.getString("nickname");
                String consultContent = gv.getString("consultContent");
                String isShow = gv.getString("isShow");
                String businessName = gv.getString("businessName");
                String replyPartyId = gv.getString("replyPartyId");
                String replyContent = gv.getString("replyContent");
                String isShowReply = gv.getString("isShowReply");

                record.put("consultId", consultId);
                if (UtilValidate.isNotEmpty(imageUrl)) {
                    record.put("imgUrl", imageUrl);
                } else {
                    record.put("imgUrl", "");
                }
                if (UtilValidate.isNotEmpty(productName)) {
                    record.put("productName", productName);
                } else {
                    record.put("productName", "");
                }
                record.put("consultType", consultType);
                record.put("createPartyId", createPartyId);
                if (UtilValidate.isNotEmpty(nickname)) {
                    record.put("nickname", nickname);
                } else {
                    record.put("nickname", "");
                }
                if (UtilValidate.isNotEmpty(consultContent)) {
                    record.put("consultContent", consultContent);
                } else {
                    record.put("consultContent", "");
                }
                if (UtilValidate.isNotEmpty(isShow)) {
                    record.put("isShow", isShow);
                } else {
                    record.put("isShow", "");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (UtilValidate.isNotEmpty(gv.get("createDate"))) {
                    record.put("createDate", sdf.format(gv.get("createDate")));
                } else {
                    record.put("createDate", "");
                }
                if (UtilValidate.isNotEmpty(businessName)) {
                    record.put("businessName", businessName);
                } else {
                    record.put("businessName", "");
                }
                if (UtilValidate.isNotEmpty(gv.get("replyDate"))) {
                    record.put("isReply", "Y");
                    record.put("replyDate", sdf.format(gv.get("replyDate")));
                } else {
                    record.put("isReply", "N");
                    record.put("replyDate", "");
                }
                if (UtilValidate.isNotEmpty(replyContent)) {
                    record.put("replyContent", replyContent);
                } else {
                    record.put("replyContent", "");
                }
                if (UtilValidate.isNotEmpty(isShowReply)) {
                    record.put("isShowReply", isShowReply);
                } else {
                    record.put("isShowReply", "");
                }
                if (UtilValidate.isNotEmpty(replyPartyId)) {
                    GenericValue person = null;
                    person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", replyPartyId));
                    if (UtilValidate.isNotEmpty(person) && UtilValidate.isNotEmpty(person.get("nickname"))) {
                        record.put("replyName", person.get("nickname"));
                    } else {
                        record.put("replyName", "客服");
                    }
                } else {
                    record.put("replyName", "客服");
                }

                consultList.add(record);
            }

            //总记录数
            consultListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > consultListSize) {
                highIndex = consultListSize;
            }

            //关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in consult find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        result.put("recordsList", consultList);
        result.put("totalSize", Integer.valueOf(consultListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 显示或隐藏咨询信息
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editConsultIsShow(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String consultIds = (String) context.get("consultIds");
        String isShow = (String) context.get("isShow");
        Map map = new HashMap();
        if (UtilValidate.isNotEmpty(consultIds) && UtilValidate.isNotEmpty(isShow)) {
            String[] consultIdList = consultIds.split(",");
            EntityListIterator consults = null;
            try {
                consults = delegator.find("Consult", EntityCondition.makeCondition("consultId", EntityOperator.IN, Arrays.asList(consultIdList)), null, null, null, null);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(consults)) {
                List<GenericValue> toBeStore = new ArrayList<GenericValue>();
                GenericValue consult = null;
                while ((consult = consults.next()) != null) {
                    consult.set("isShow", isShow);
                    toBeStore.add(consult);
                }
                try {
                    delegator.storeAll(toBeStore);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                map.put("status", true);
            }
            try {
                consults.close();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }

        result.put("result", map);

        return result;
    }

    /**
     * 新增或修改咨询回复  Add By AlexYao 2016-3-31 17:16:54
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> replyConsult(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String consultId = (String) context.get("consultId");
        String isShow = (String) context.get("isShow");
        String replyContent = (String) context.get("replyContent");
        String isShowReply = (String) context.get("isShowReply");
        if (UtilValidate.isNotEmpty(consultId) && UtilValidate.isNotEmpty(isShow) && UtilValidate.isNotEmpty(replyContent) && UtilValidate.isNotEmpty(isShowReply)) {
            GenericValue consult = null;
            try {
                consult = delegator.findByPrimaryKey("Consult", UtilMisc.toMap("consultId", consultId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(consult)) {
                consult.set("isShow", isShow);
                consult.set("replyContent", replyContent);
                consult.set("isShowReply", isShowReply);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                consult.set("replyDate", Timestamp.valueOf(sdf.format(new Date())));
                consult.set("replyPartyId", userLogin.get("partyId"));
                try {
                    delegator.store(consult);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 反馈意见数据:查询条件(用户昵称,反馈类型,是否回复,内容)
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findFeedback(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Locale locale = (Locale) context.get("locale");

        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));


        List<Map> feedbackList = FastList.newInstance();
        int feedbackListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("FEE", "Feedback");
        dynamicView.addAlias("FEE", "feedbackId");
        dynamicView.addAlias("FEE", "createPartyId");
        dynamicView.addAlias("FEE", "replyPartyId");
        dynamicView.addAlias("FEE", "feedbackType");
        dynamicView.addAlias("FEE", "replyDate");
        dynamicView.addAlias("FEE", "feedbackContent");
        dynamicView.addAlias("FEE", "contactMethod");
        dynamicView.addAlias("FEE", "createDate");
        dynamicView.addAlias("FEE", "replyContent");
        dynamicView.addAlias("FEE", "isShowReply");

        dynamicView.addMemberEntity("PER", "Person");
        dynamicView.addAlias("PER", "nickname");

        dynamicView.addViewLink("FEE", "PER", Boolean.FALSE, ModelKeyMap.makeKeyMapList("createPartyId", "partyId"));

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("feedbackId");
        fieldsToSelect.add("feedbackType");
        fieldsToSelect.add("createPartyId");
        fieldsToSelect.add("nickname");
        fieldsToSelect.add("feedbackContent");
        fieldsToSelect.add("contactMethod");
        fieldsToSelect.add("createDate");
        fieldsToSelect.add("replyPartyId");
        fieldsToSelect.add("replyDate");
        fieldsToSelect.add("replyContent");
        fieldsToSelect.add("isShowReply");

        String sortField = "-createDate";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        //根据昵称模糊查询
        if (UtilValidate.isNotEmpty(context.get("nickname"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("nickname"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("nickname") + "%")));
        }

        //根据咨询类型查询
        if (UtilValidate.isNotEmpty(context.get("feedbackType"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("feedbackType"), EntityOperator.EQUALS, context.get("feedbackType")));
        }
        //根据是否回复查询
        if (UtilValidate.isNotEmpty(context.get("isReply")) && "Y".equals(context.get("isReply"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("replyDate"), EntityOperator.NOT_EQUAL, null));
        } else if (UtilValidate.isNotEmpty(context.get("isReply")) && "N".equals(context.get("isReply"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("replyDate"), EntityOperator.EQUALS, null));
        }

        //根据咨询内容模糊查询
        if (UtilValidate.isNotEmpty(context.get("feedbackContent"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("feedbackContent"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("feedbackContent") + "%")));
        }

        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

            int i = 0;
            //遍历查询结果集
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map record = FastMap.newInstance();
                record.put("index", i++);
                String feedbackId = gv.getString("feedbackId");
                String feedbackType = gv.getString("feedbackType");
                String createPartyId = gv.getString("createPartyId");
                String nickname = gv.getString("nickname");
                String feedbackContent = gv.getString("feedbackContent");
                String contactMethod = gv.getString("contactMethod");
                String replyPartyId = gv.getString("replyPartyId");
                String replyContent = gv.getString("replyContent");
                String isShowReply = gv.getString("isShowReply");

                record.put("feedbackId", feedbackId);
                record.put("feedbackType", feedbackType);
                record.put("createPartyId", createPartyId);
                if (UtilValidate.isNotEmpty(nickname)) {
                    record.put("nickname", nickname);
                } else {
                    record.put("nickname", "");
                }
                if (UtilValidate.isNotEmpty(feedbackContent)) {
                    record.put("feedbackContent", feedbackContent);
                } else {
                    record.put("feedbackContent", "");
                }
                if (UtilValidate.isNotEmpty(contactMethod)) {
                    record.put("contactMethod", contactMethod);
                } else {
                    record.put("contactMethod", "");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (UtilValidate.isNotEmpty(gv.get("createDate"))) {
                    record.put("createDate", sdf.format(gv.get("createDate")));
                } else {
                    record.put("createDate", "");
                }
                if (UtilValidate.isNotEmpty(gv.get("replyDate"))) {
                    record.put("isReply", "Y");
                    record.put("replyDate", sdf.format(gv.get("replyDate")));
                } else {
                    record.put("isReply", "N");
                    record.put("replyDate", "");
                }
                if (UtilValidate.isNotEmpty(replyContent)) {
                    record.put("replyContent", replyContent);
                } else {
                    record.put("replyContent", "");
                }
                if (UtilValidate.isNotEmpty(isShowReply)) {
                    record.put("isShowReply", isShowReply);
                } else {
                    record.put("isShowReply", "");
                }
                if (UtilValidate.isNotEmpty(replyPartyId)) {
                    GenericValue person = null;
                    person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", replyPartyId));
                    if (UtilValidate.isNotEmpty(person) && UtilValidate.isNotEmpty(person.get("nickname"))) {
                        record.put("replyName", person.get("nickname"));
                    } else {
                        record.put("replyName", "客服");
                    }
                } else {
                    record.put("replyName", "客服");
                }
                List<GenericValue> feedbackContents = delegator.findByAnd("FeedbackContent", UtilMisc.toMap("feedbackId", feedbackId));
                List<String> contentIds = FastList.newInstance();
                for (GenericValue content : feedbackContents) {
                    contentIds.add(content.getString("contentId"));
                }
                record.put("contentIds", contentIds);
                feedbackList.add(record);
            }

            //总记录数
            feedbackListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > feedbackListSize) {
                highIndex = feedbackListSize;
            }

            //关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in feedback find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        result.put("recordsList", feedbackList);
        result.put("totalSize", Integer.valueOf(feedbackListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 新增或修改反馈回复  Add By AlexYao 2016-4-1 10:38:54
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> replyFeedback(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String feedbackId = (String) context.get("feedbackId");
        String replyContent = (String) context.get("replyContent");
        String isShowReply = (String) context.get("isShowReply");
        if (UtilValidate.isNotEmpty(feedbackId) && UtilValidate.isNotEmpty(replyContent) && UtilValidate.isNotEmpty(isShowReply)) {
            GenericValue feedback = null;
            try {
                feedback = delegator.findByPrimaryKey("Feedback", UtilMisc.toMap("feedbackId", feedbackId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(feedback)) {
                feedback.set("replyContent", replyContent);
                feedback.set("isShowReply", isShowReply);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                feedback.set("replyDate", Timestamp.valueOf(sdf.format(new Date())));
                feedback.set("replyPartyId", userLogin.get("partyId"));
                try {
                    delegator.store(feedback);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

}
