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
package com.qihua.ofbiz.give;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
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
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

//import org.ofbiz.order.order.OrderReadHelper;
//import org.ofbiz.product.product.ProductWorker;

/**
 * GivePresent service
 * @author zhajh 2018/04/23
 *
 */
public class GivePresentServices {
    public static final String module = GivePresentServices.class.getName();
    public static final String resource = "ProductUiLabels";

    /*************************************赠送礼品***********************************/
    /**
     * 赠送礼品列表查询
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getGivePresentList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> givePresentList = FastList.newInstance();
        //总记录数
        int givePresentListSize = 0;
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

        String sender = (String) context.get("sender"); // 赠送人
        String startDate = (String) context.get("startDate");// 增送时间（begin）
        String endDate = (String) context.get("endDate");// 增送时间（end）
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();


        dynamicView.addMemberEntity("PORP","PartyOrderRelPresent");

        dynamicView.addAlias("PORP","giftId");
        dynamicView.addAlias("PORP","orderGroupId");
        dynamicView.addAlias("PORP","sendDate");
        dynamicView.addAlias("PORP","receiveDate");
        dynamicView.addAlias("PORP","sendPartyId");
        dynamicView.addAlias("PORP","receivePartyId");
        dynamicView.addAlias("PORP","status");

        dynamicView.addMemberEntity("OGOR","OrderGroupOrderRel");
        dynamicView.addAlias("OGOR","orderGroupId");
        dynamicView.addAlias("OGOR","orderId");
        dynamicView.addViewLink("PORP","OGOR", Boolean.TRUE,ModelKeyMap.makeKeyMapList("orderGroupId", "orderGroupId"));

        dynamicView.addMemberEntity("SP","Person");
        dynamicView.addAlias("SP", "sPartyId", "partyId", null, null, null, null);
        dynamicView.addAlias("SP", "sMobile", "mobile", null, null, null, null);
        dynamicView.addViewLink("PORP","SP", Boolean.TRUE,ModelKeyMap.makeKeyMapList("sendPartyId", "partyId"));


        dynamicView.addMemberEntity("RP","Person");
        dynamicView.addAlias("RP", "rPartyId", "partyId", null, null, null, null);
        dynamicView.addAlias("RP", "rMobile", "mobile", null, null, null, null);
        dynamicView.addViewLink("PORP","RP", Boolean.TRUE,ModelKeyMap.makeKeyMapList("receivePartyId", "partyId"));


//        dynamicView.addMemberEntity("PRNO","getProductNameByOrderId");
//        dynamicView.addAlias("PRNO", "orderId");
//        dynamicView.addAlias("PRNO", "productNames");
//        dynamicView.addViewLink("OGOR","PRNO", Boolean.TRUE,ModelKeyMap.makeKeyMapList("orderId", "orderId"));

        fieldsToSelect.add("giftId");
        fieldsToSelect.add("orderGroupId");
        fieldsToSelect.add("orderId");
        fieldsToSelect.add("sendDate");
        fieldsToSelect.add("receiveDate");
        fieldsToSelect.add("rPartyId");
        fieldsToSelect.add("sPartyId");
        fieldsToSelect.add("rMobile");
        fieldsToSelect.add("sMobile");
//        fieldsToSelect.add("productNames");
        fieldsToSelect.add("status");

        // 赠送人
        if(UtilValidate.isNotEmpty(sender)){
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("sMobile"), EntityOperator.LIKE, EntityFunction.UPPER("%"+sender+"%")));
        }


        if (UtilValidate.isNotEmpty(startDate) && startDate.length() > 8) {
            startDate = startDate.trim();
            if (startDate.length() < 14) {
                startDate = startDate + " " + "00:00:00.000";
            }
            try {
                Object converted = ObjectType.simpleTypeConvert(startDate, "Timestamp", null, null);
                if (converted != null) {
                    filedExprs.add(EntityCondition.makeCondition("sendDate", EntityOperator.GREATER_THAN_EQUAL_TO, converted));
                }
            } catch (GeneralException e) {
                Debug.logWarning(e.getMessage(), module);
            }
        }

        if (UtilValidate.isNotEmpty(endDate) && endDate.length() > 8) {
            endDate = endDate.trim();
            if (endDate.length() < 14) {
                endDate = endDate + " " + "23:59:59.999";
            }
            try {
                Object converted = ObjectType.simpleTypeConvert(endDate, "Timestamp", null, null);
                if (converted != null) {
                    filedExprs.add(EntityCondition.makeCondition("sendDate", EntityOperator.LESS_THAN_EQUAL_TO, converted));
                }
            } catch (GeneralException e) {
                Debug.logWarning(e.getMessage(), module);
            }
        }

        //排序字段名称
        String sortField = "-giftId";
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
            List <GenericValue> curGivePresentList=FastList.newInstance();
            curGivePresentList=pli.getPartialList(lowIndex, viewSize);
            for(GenericValue gv : curGivePresentList){
                String giftId = gv.getString("giftId");
                String orderGroupId = gv.getString("orderGroupId");
                String sendDate = gv.getString("sendDate");
                String receiveDate = gv.getString("receiveDate");
                String rPartyId = gv.getString("rPartyId");
                String sPartyId = gv.getString("sPartyId");
                String rMobile = gv.getString("rMobile");
                String sMobile = gv.getString("sMobile");

                String status = gv.getString("status");
                String orderId= gv.getString("orderId");

                //  取得订单商品信息
                String productNames ="";
                List<GenericValue> orderItemInfos=delegator.findByAnd("OrderItem",UtilMisc.toMap("orderId",orderId));
                if(UtilValidate.isNotEmpty((orderItemInfos))){
                    for(GenericValue orderItemInfo:orderItemInfos){
                        String curProductId=orderItemInfo.getString("productId");
                        GenericValue curProductInfo=delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",curProductId));
                        if(UtilValidate.isNotEmpty(curProductInfo)){
                            if(UtilValidate.isEmpty(productNames)){
                                productNames=curProductInfo.getString("productName");
                            }else{
                                productNames=productNames+","+curProductInfo.getString("productName");
                            }
                        }
                    }
                }


                Map map = FastMap.newInstance();
                map.put("giftId", giftId);
                map.put("orderGroupId", orderGroupId);
                map.put("sendDate",sendDate);
                map.put("receiveDate", receiveDate);
                map.put("rPartyId",rPartyId);
                map.put("sPartyId", sPartyId);
                map.put("rMobile",rMobile);
                map.put("sMobile", sMobile);
                map.put("productNames", productNames);
                map.put("status", status);
                map.put("orderId", orderId);

                givePresentList.add(map);
            }

            // 获取总记录数
            givePresentListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > givePresentListSize) {
                highIndex = givePresentListSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }


        //返回的参数
        result.put("givePresentList",givePresentList);
        result.put("totalSize", Integer.valueOf(givePresentListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }


    /**
     * 根据编码取得订单信息
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getOrderInfoById(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        //参数
        String orderId = (String)context.get("orderId");

        Map<String,Object> productInfoMap=FastMap.newInstance();
        List<GenericValue> orderItems= FastList.newInstance();
        try {
            //根据订单编码取得一条订单信息
            GenericValue orderInfo = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId",orderId));

            // 初始化
            // 合并支付订单号（js-grouporderId）
            String groupOrderId="";
            // 下单时间 （js-orderDate）
            String orderDate="";
            // 用户（js-userName）
            String userName="";
            // 原始金额：（js-originalPrice）
            String originalPrice="";
            // 运费：（js-shipCost）
            String shipCost="";
            // 促销类型：（js-promoType）
            String promoType="";
            // 促销优惠：(js-promoSale)
            String promoSale="";
            // 代金券名称：（js-couponName）
            String couponName="";
            // 代金券优惠：（js-couponSale）
            String couponSale="";
            // 订单使用积分：（js-orderUseIntegral）
            String orderUseIntegral="";
            // 积分优惠：（js-integralDiscount）
            String integralDiscount="";
            // 应付金额：(js-shouldPayMoney)
            String shouldPayMoney="";
            // 实付金额：(js-actualPayMoney)
            String actualPayMoney="";
            // 支付方式：(js-payMoney)
            String payMoney="";
            // 支付流水号：(js-referenceNum)
            String referenceNum="";
            // 获得积分：(js-getIntegral)
            String getIntegral="";
            // 店铺：(js-store)
            String store="";
            // 配送方式：(js-delivery)
            String delivery="";
            // 快递公司：（js-expressCompany）
            String expressCompany="";
            // 快递单号：（js-expressNumber）
            String expressNumber="";
            // 运费：（js-carriage）
            String carriage="";
            // 收货地址：（js-receiveAddress）
            String receiveAddress="";
            // 详细地址：（js-detailAddress）
            String detailAddress="";
            // 收货人：（js-receiver）
            String receiver="";
            // 电话：（js-tel）
            String tel="";
            // 客户留言：（js-customerMessage）
            String customerMessage="";
            // 发票类型：(js-invoiceType)
            String invoiceType="";

            if(UtilValidate.isNotEmpty(orderInfo)){
//                // 标题
//                   // 订单号
//                   result.put("orderId",orderInfo.get("orderId"));
//                   // 合并支付订单号（js-orderGroupId）
//                   List<GenericValue> orderGroupInfos = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderId",orderId));
//                   if(UtilValidate.isNotEmpty(orderGroupInfos)){
//                       result.put("orderGroupId",orderGroupInfos.get(0).getString("orderGroupId"));
//                   }
//                   // 下单时间 （js-orderDate）
//                   result.put("orderDate",orderInfo.getString("orderDate"));
//                   // 用户（js-userName）
//                   userName =OrderReadHelper.getCustomerName(delegator,orderId);
//                   result.put("userName",userName);
//
//                   // 取得支付信息
//                    // 原始金额：（js-originalPrice）
//                   orderItems= OrderReadHelper.getOrderItems(delegator,orderId);
//                   BigDecimal bOriginalPrice=BigDecimal.valueOf(0);
//                   if(UtilValidate.isNotEmpty(orderItems)){
//                       for(GenericValue oi:orderItems){
//                           bOriginalPrice=bOriginalPrice.add(oi.getBigDecimal("unitPrice").multiply(oi.getBigDecimal("quantity")));
//                       }
//                   }
//                   result.put("originalPrice",bOriginalPrice);
                    // 运费：（js-shipCost）
                    // 促销类型：（js-promoType）
                    // 促销优惠：(js-promoSale)
                    // 代金券名称：（js-couponName）
                    // 代金券优惠：（js-couponSale）
                    // 订单使用积分：（js-orderUseIntegral）
                    // 积分优惠：（js-integralDiscount）

                    // 应付金额：(js-shouldPayMoney)
                    result.put("shouldPayMoney",orderInfo.getBigDecimal("shouldPayMoney"));
                    // 实付金额：(js-actualPayMoney)
                    result.put("shouldPayMoney",orderInfo.getBigDecimal("actualPayMoney"));
                    // 支付方式：(js-payMoney)
                    // 支付流水号：(js-referenceNum)
                    // 获得积分：(js-getIntegral)
                    // 店铺：(js-store)

                // 物流信息
                    // 配送方式：(js-delivery)
                    // 快递公司：（js-expressCompany）
                    // 快递单号：（js-expressNumber）
                    // 运费：（js-carriage）
                    // 收货地址：（js-receiveAddress）
                    // 详细地址：（js-detailAddress）
                    // 收货人：（js-receiver）
                    // 电话：（js-tel）
                    // 客户留言：（js-customerMessage）
                // 发票信息
                    // 发票类型：(js-invoiceType)
                // 商品信息
//                if(UtilValidate.isNotEmpty(orderItems)){
//                    for(GenericValue oi:orderItems){
//
//                         BigDecimal originalPrice = OrderReadHelper.getOrderProductsPrice(delegator,oi.getString("orderId"),oi.getString("orderItemSeqId"));
//                         String productSettingName = OrderReadHelper.getProductSettingName(delegator,oi.getString("productId"),oi.getString("orderId"),oi.getString("orderItemSeqId"));
//                         GenericValue p =  delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId", oi.getString("productId")));
//                         GenericValue activity = delegator.findByPrimaryKey("ProductActivity",UtilMisc.toMap("activityId", oi.getString("activityId")));
//                         String returnType = OrderReadHelper.getOrderReturnType(delegator,orderId,oi.getString("productId"));
//                         String fs = OrderReadHelper.getProductFeature(delegator,oi.getString("productId")) ;
//                        // 商品名称
//                        if(UtilValidate.isNotEmpty(p)){
//                            productInfoMap.put("productName",p.getString("productName"));
//                        }
//                        // 原始价格
//                        if(UtilValidate.isNotEmpty(originalPrice)){
//                            productInfoMap.put("originalPrice",originalPrice);
//                        }
//                        // 商品规格
//                         // 取得商品特征属性
//                        String productFeatures = ProductWorker.getProductFeatureInfos(delegator,oi.getString("productId"));
//                        if(UtilValidate.isNotEmpty(productFeatures)){
//                            productInfoMap.put("productFeatures",productFeatures);
//                        }
//                        // 数量
//                        if(UtilValidate.isNotEmpty(p)){
//                            productInfoMap.put("quantity",p.get("quantity"));
//                        }
//                        // 惠后价格
//                        if(UtilValidate.isNotEmpty(p)){
//                            productInfoMap.put("unitPrice",p.get("unitPrice"));
//                        }
//                        // 商品总价
//                        if(UtilValidate.isNotEmpty(p)){
//                            productInfoMap.put("totalPrice",p.getBigDecimal("unitPrice").multiply(p.getBigDecimal("quantity")));
//                        }
//                        // 状态
//                         GenericValue status = delegator.findByPrimaryKey("StatusItem",UtilMisc.toMap("statusId",oi.getString("statusId")));
//                         if(UtilValidate.isNotEmpty(status)){
//                             productInfoMap.put("orderStatusName",status);
//                         }
//
//                    }
//                }

                result.put("productInfo",productInfoMap);
            }

//            result.put("questionId",question_gv.get("questionId"));
//            result.put("question",question_gv.get("question"));
//            result.put("answerResult",question_gv.get("result"));
//            result.put("questionType", question_gv.get("questionType"));
//
//            //答案信息
//            String _arr="";
//            List<GenericValue> answer_list = FastList.newInstance();
//            answer_list = delegator.findByAnd("Answer",UtilMisc.toMap("questionId", questionId));
//            result.put("answerList", answer_list);

            result.put("orderId", "1222");


        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

}
