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
package com.yuaoq.yabiz.mobile.services;

import com.google.gson.Gson;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import com.yuaoq.yabiz.mobile.services.kdmall.KdRetData;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.common.util.JsonMapper;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDataSourceException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.OrderChangeHelper;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.TicketEvents;
import org.ofbiz.service.*;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Order Processing Services
 */

public class OrderService {

    @Value("${image.base.url}")
    public static String baseImgUrl;

    public static final String module = OrderService.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";
    public static final String resourceProduct = "ProductUiLabels";
    public static final String FINISHED_GOOD = "FINISHED_GOOD"; //实物商品
    public static final String VIRTUAL_GOOD = "VIRTUAL_GOOD";   //虚拟商品
    public static final String SMZT = "SMZT"; //上门自提
    public static final MathContext generalRounding = new MathContext(10);


    /**
     * 向凯德发送订单信息
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> sendOrderInfo2Kd(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dctx.getDispatcher();

        /** 获取参数 */
        Delegator delegator = dctx.getDelegator();
        // 订单信息参数
        Map<String, Object> orderInfoParams = FastMap.newInstance();
        Map<String, Object> userInfoParams = FastMap.newInstance();
        Map<String, Object> resultMap = FastMap.newInstance();
        Map<String, Object> result03Map = FastMap.newInstance();


        String unionId = ""; //用户的unionid

        String token = ""; // token
        String member_id = ""; // 会员编号
        String mall_id = "";// 商场编号
        String amount = "";// 交易金额单位元
        String storeCode = "";// 商户代码（CRM提供）
        String receiptNo = "";// 订单号
        String locationId = "";// mall_id 的地址编码

        try {
            // 取得当前系统时间
            Date nowTime = new Date(System.currentTimeMillis());
            SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd");
            String strNowTime = sdFormatter.format(nowTime);
            // 获取订单信息
            List<GenericValue> orderInfos = delegator.findByAnd("OrderHeaderByStatusId", UtilMisc.toMap("statusId", "ORDER_WAITRECEIVE"));


            List<EntityCondition> orderConditions = FastList.newInstance();


            // 订单为待收货，待评价，已完成状态的场合
            orderConditions.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITRECEIVE"),
                                                                                  EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITEVALUATE"),
                                                                                  EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_COMPLETED")), EntityOperator.OR));
            List<String> orderBy=FastList.newInstance();
            orderBy.add("statusDatetime");
            List<GenericValue> orderInfoForAllStatus = delegator.findList("OrderHeaderByStatusId", EntityCondition.makeCondition(orderConditions, EntityOperator.AND), null, orderBy, null, false);


            List<GenericValue> orderForKdList = FastList.newInstance();
            // 取得需要传递的订单信息（订单状态为"待收货",距离待收货的日期有10日）
            if (UtilValidate.isNotEmpty(orderInfos)) {
                for (GenericValue curOrderInfo : orderInfos) {
                    int days = 0;
                    if (UtilValidate.isNotEmpty(curOrderInfo)) {
                        Timestamp statusDatetime = curOrderInfo.getTimestamp("statusDatetime");
                        String strStatusDatetime = "";
                        if (UtilValidate.isNotEmpty(statusDatetime)) {
                            strStatusDatetime = sdFormatter.format(statusDatetime);
                        }
                        if (UtilValidate.isNotEmpty(strStatusDatetime)) {
                            days = UtilDateTime.daysBetween(strStatusDatetime, strNowTime);
                            if (days == 10) {
                                List<GenericValue> orderInfoByIds = FastList.newInstance();
                                if (UtilValidate.isNotEmpty(orderInfoForAllStatus)) {
                                    for (GenericValue orderInfo : orderInfoForAllStatus) {
                                        if (UtilValidate.isNotEmpty(curOrderInfo.getString("orderId")) &&
                                                UtilValidate.isNotEmpty(curOrderInfo.getString("orderId"))) {
                                            if (orderInfo.getString("orderId").equals(curOrderInfo.getString("orderId"))) {
                                                orderInfoByIds.add(orderInfo);
                                            }
                                        }
                                    }
                                    if (UtilValidate.isNotEmpty(orderInfoByIds)) {
                                        GenericValue gvOrderInfo = orderInfoByIds.get(orderInfoByIds.size() - 1);
                                        if (UtilValidate.isNotEmpty(gvOrderInfo)) {
                                            orderForKdList.add(gvOrderInfo);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (UtilValidate.isNotEmpty(orderForKdList)) {
                for (GenericValue orderInfo : orderForKdList) {
                    // 初始化
                    unionId = ""; //用户的unionid
                    token = ""; // token
                    member_id = ""; // 会员编号
                    mall_id = "";// 商场编号
                    amount = "";// 交易金额单位元
                    storeCode = "";// 商户代码（CRM提供）
                    receiptNo = "";// 订单号
                    locationId = "";// mall_id 的地址编码

                    // 取得下单人信息unionid信息
                    if (UtilValidate.isNotEmpty(orderInfo)) {
                        List<GenericValue> orderRoleInfos = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderInfo.getString("orderId"), "roleTypeId", "PLACING_CUSTOMER"));
                        if (UtilValidate.isNotEmpty(orderRoleInfos)) {
                            String curPartyId = EntityUtil.getFirst(orderRoleInfos).getString("partyId");
                            GenericValue personInfo = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", curPartyId));
                            if (UtilValidate.isNotEmpty(personInfo)) {
                                unionId = personInfo.getString("unionId");
                            }
                        }
                    }
                    // 取得用户的token 和 memberId 信息
                    if (UtilValidate.isNotEmpty(unionId)) {
                        result03Map = FastMap.newInstance();
                        userInfoParams.put("unionid", unionId);

                        Map<String, Object> result03 = dispatcher.runSync("kaide-getMemberIdByUnionId", UtilMisc.toMap("unionid", unionId));
                        if (ServiceUtil.isError(result03)) {
                            result03Map.put("retCode", 0);
                            result03Map.put("message", "根据unionid获取获取token和memberID信息错误");
                        }
                        KdRetData retData = new Gson().fromJson((String) result03.get("result"), KdRetData.class);
                        if (retData.getResult().equalsIgnoreCase("22001")) {
                            //获取成功
                            // 取得token 和 member_id的值
                            member_id = (String) retData.getData().get("memberID");
                            token = (String) retData.getData().get("token");
                        }

                        if (UtilValidate.isNotEmpty(member_id) && UtilValidate.isNotEmpty(token)) {
                            // 订单金额
                            BigDecimal curGrandTotal = orderInfo.getBigDecimal("grandTotal");
                            // 商场编码（保存在订单表的mall_id 中）
                            List<GenericValue> orderAttributeInfos = delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderInfo.getString("orderId"), "attrName", "mall_Id"));
                            if (UtilValidate.isNotEmpty(orderAttributeInfos)) {
                                mall_id = EntityUtil.getFirst(orderAttributeInfos).getString("attrValue");// 商场编号
                            }
//                                mall_id="10";
                            // 交易金额单位元
                            amount = curGrandTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                            // 商户编码
                            GenericValue kdMallInfo = delegator.findByPrimaryKey("KdMallInfo", UtilMisc.toMap("mallId", mall_id));
                            if (UtilValidate.isNotEmpty(kdMallInfo)) {
                                storeCode = kdMallInfo.getString("locationId");
                                locationId = kdMallInfo.getString("locationId");
                            }
                            storeCode = "LEMALL";// 指定storeCode的值 固定值：LEMALL
                            // 订单编码
                            String curOrderId = orderInfo.getString("orderId");// 订单号
                            String nowDate = UtilDateTime.nowDateString("yyyyMMddHHmmssSSS");
                            if (UtilValidate.isNotEmpty(curOrderId)) {
                                receiptNo = curOrderId + nowDate;// 订单号
                            }
                            if (UtilValidate.isNotEmpty(token)) {
                                orderInfoParams.put("token", token);
                            } else {
                                continue;
                            }
                            if (UtilValidate.isNotEmpty(member_id)) {
                                orderInfoParams.put("member_id", member_id);
                            } else {
                                continue;
                            }
                            if (UtilValidate.isNotEmpty(mall_id)) {
                                orderInfoParams.put("mall_id", mall_id);
                            } else {
                                continue;
                            }
                            if (UtilValidate.isNotEmpty(amount)) {
                                orderInfoParams.put("amount", amount);
                            } else {
                                continue;
                            }
                            if (UtilValidate.isNotEmpty(storeCode)) {
                                orderInfoParams.put("storeCode", storeCode);
                            } else {
                                continue;
                            }
                            if (UtilValidate.isNotEmpty(receiptNo)) {
                                orderInfoParams.put("receiptNo", receiptNo);
                            } else {
                                continue;
                            }
//                                if (UtilValidate.isNotEmpty(locationId)) {
//                                     orderInfoParams.put("locationId", locationId);
//                                }else{
//                                    continue;
//                                }
                            String msg = "";
                            String resultValue = "";
                            if (UtilValidate.isNotEmpty(orderInfoParams)) {
                                resultMap = FastMap.newInstance();
                                resultMap = dispatcher.runSync("kaide-sendOrderInfo", orderInfoParams);
                                KdRetData retDataKd = new Gson().fromJson((String) resultMap.get("result"), KdRetData.class);

                                msg = retDataKd.getMsg();
                                resultValue = retDataKd.getResult();
                                result.put("msg", msg);
                                result.put("result", resultValue);
                            }
                        }
                    }
                }
            }

        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        } catch (GenericServiceException e) {
            return ServiceUtil.returnError(e.getMessage());
        } catch (ParseException e) {
            return ServiceUtil.returnError(e.getMessage());
        }


        return result;
    }

    public static Map<String, Object> processPaymentCallback(DispatchContext dct, Map<String, ? extends Object> context) {

        Delegator delegator = dct.getDelegator();
        LocalDispatcher dispatcher = dct.getDispatcher();
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String orderId = (String) context.get("orderId");
        String transactionId = (String) context.get("transactionId");
        Integer totalFee = (Integer) context.get("totalFee");
        Integer discount = (Integer) context.get("discount");
        String status = (String) context.get("status");
        String chargeId = (String) context.get("charegeId");
        // get the system user
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "system"));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get UserLogin for: system; cannot continue", module);
            return ServiceUtil.returnError(e.getMessage());

        }

        GenericValue orderHeader = null;
        if (UtilValidate.isNotEmpty(orderId)) {
            try {
                orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        } else {
            Debug.logError("WeiXin did not callback with a valid orderId!", module);
            return ServiceUtil.returnError("没有获取orderId");
        }

        if (orderHeader == null) {
            Debug.logError("Cannot get the order header for order: " + orderId, module);
            return ServiceUtil.returnError("orderId对应订单不存在");
        }
        boolean okay = false;
        boolean beganTransaction = false;

        try {
            beganTransaction = TransactionUtil.begin();
            List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
            String activityId = "";
            if (UtilValidate.isNotEmpty(orderHeader.getString("activityId"))) {
                GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", orderHeader.get("activityId")));
                activityId = productActivity.getString("activityId");
            }

            //判断该笔订单是否在商户网站中已经做过处理
            //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
            //如果有做过处理，不执行商户的业务程序
            boolean hasGoodsType = false;
            if (UtilValidate.isNotEmpty(orderItems)) {
                for (int i = 0; i < orderItems.size(); i++) {
                    GenericValue orderItem = orderItems.get(i);
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.getString("productId")));
                    if ("FINISHED_GOOD".equals(product.get("productTypeId"))) {
                        hasGoodsType = true;
                    } else if ("VIRTUAL_GOOD".equals(product.get("productTypeId"))) {
                        okay = OrderChangeHelper.evaluateOrder(dispatcher, userLogin, orderId);
                        //生成代金券
                        TicketEvents.createTickets(dispatcher, delegator, orderItem.getString("productId"), activityId, orderId, orderItem);
                    }

                }

            }
    
            //增加活动已售  by AlexYao 2016/02/01
            if (UtilValidate.isNotEmpty(activityId)) {
                //如果是上门自提
                String productId = orderItems.get(0).getString("productId");
                
                //下单记录发生的数据
                    GenericValue productActivityGoods = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", productId));
                    if (UtilValidate.isNotEmpty(productActivityGoods)) {
                        Long occupiedQuantityTotal = productActivityGoods.getLong("occupiedQuantityTotal") == null ? 0L : productActivityGoods.getLong("occupiedQuantityTotal");
                        Long orderNums = orderItems.get(0).getBigDecimal("quantity").longValue();
                        productActivityGoods.set("occupiedQuantityTotal", occupiedQuantityTotal + orderNums);
                        productActivityGoods.store();
                    }
                 
            }
            
            if (hasGoodsType) {
                okay = OrderChangeHelper.shipOrder(dispatcher, userLogin, orderId);
            } else {
                okay = OrderChangeHelper.evaluateOrder(dispatcher, userLogin, orderId);
            }

            //注意：
            //该种交易状态只在两种情况下出现
            //1、开通了普通即时到账，买家付款成功后。
            //2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
            if (okay) {
                // set the payment preference
                okay = setPaymentPreferences(delegator, dispatcher, userLogin, orderId, transactionId, totalFee, discount, status, chargeId);
            }

            //
        } catch (Exception e) {
            String errMsg = "Error handling AliPay notification";
            Debug.logError(e, errMsg, module);
            try {
                TransactionUtil.rollback(beganTransaction, errMsg, e);
            } catch (GenericTransactionException gte2) {
                Debug.logError(gte2, "Unable to rollback transaction", module);
            }
        } finally {
            if (!okay) {
                try {
                    TransactionUtil.rollback(beganTransaction, "Failure in processing AliPay callback", null);
                } catch (GenericTransactionException gte) {
                    Debug.logError(gte, "Unable to rollback transaction", module);
                }
            } else {
                try {
                    TransactionUtil.commit(beganTransaction);
                } catch (GenericTransactionException gte) {
                    Debug.logError(gte, "Unable to commit transaction", module);
                }
            }
        }
        return resultData;
    }


    private static boolean setPaymentPreferences(Delegator delegator, LocalDispatcher dispatcher, GenericValue userLogin, String orderId, String transactionId, Integer totalFee, Integer discount, String status, String chargeId) {
        Debug.logVerbose("Setting payment prefrences..", module);
        List<GenericValue> paymentPrefs = null;
        try {
            Map<String, String> paymentFields = UtilMisc.toMap("orderId", orderId, "statusId", "PAYMENT_NOT_RECEIVED");
            paymentPrefs = delegator.findByAnd("OrderPaymentPreference", paymentFields);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get payment preferences for order #" + orderId, module);
            return false;
        }
        if (paymentPrefs.size() > 0) {
            for (GenericValue pref : paymentPrefs) {
                boolean okay = setPaymentPreference(dispatcher, userLogin, pref, orderId, transactionId, totalFee, discount, status, chargeId);
                if (!okay)
                    return false;
            }
        }
        return true;
    }

    private static boolean setPaymentPreference(LocalDispatcher dispatcher, GenericValue userLogin, GenericValue paymentPreference, String orderId, String transactionId, Integer totalFee, Integer discount, String status, String chargeId) {

        List<GenericValue> toStore = new LinkedList<GenericValue>();

        // WeiXin returns the timestamp in the format 'hh:mm:ss Jan 1, 2000 PST'
        // Parse this into a valid Timestamp Object
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss MMM d, yyyy z");
        java.sql.Timestamp authDate = UtilDateTime.nowTimestamp();

//        paymentPreference.set("maxAmount", new BigDecimal(((Double) (new BigDecimal(totalFee).doubleValue() / 100)).toString()));

        if (status.equalsIgnoreCase("success")) {
            paymentPreference.set("statusId", "PAYMENT_RECEIVED");
        } else if (status.equals("Pending")) {
            paymentPreference.set("statusId", "PAYMENT_NOT_RECEIVED");
        } else {
            paymentPreference.set("statusId", "PAYMENT_RECEIVED");
        }
        paymentPreference.set("paymentMethodTypeId", "EXT_PING");
        paymentPreference.set("chargeId", chargeId);
        toStore.add(paymentPreference);


        Delegator delegator = paymentPreference.getDelegator();

        // create the PaymentGatewayResponse
        String responseId = delegator.getNextSeqId("PaymentGatewayResponse");
        GenericValue response = delegator.makeValue("PaymentGatewayResponse");
        response.set("paymentGatewayResponseId", responseId);
        response.set("paymentServiceTypeEnumId", "PRDS_PAY_EXTERNAL");
        response.set("orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"));
        response.set("paymentMethodTypeId", paymentPreference.get("paymentMethodTypeId"));
        response.set("paymentMethodId", paymentPreference.get("paymentMethodId"));

        // set the auth info
        response.set("amount", new BigDecimal(((Double) (new BigDecimal(totalFee).doubleValue() / 100)).toString()));
        response.set("referenceNum", transactionId);
        response.set("gatewayCode", status);
        response.set("gatewayFlag", status.substring(0, 1));
        response.set("gatewayMessage", status);
        response.set("transactionDate", authDate);
        toStore.add(response);

        try {
            delegator.storeAll(toStore);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot set payment preference/payment info", module);
            return false;
        }

        // create a payment record too
        Map<String, Object> results = null;
        try {
            String comment = "AccountingPaymentReceiveViaPing++";
            results = dispatcher.runSync("createPaymentFromPreference", UtilMisc.toMap("userLogin", userLogin,
                    "orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"), "comments", comment));
        } catch (GenericServiceException e) {
            Debug.logError(e, "Failed to execute service createPaymentFromPreference", module);
            return false;
        }

        if ((results == null) || (results.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
            Debug.logError((String) results.get(ModelService.ERROR_MESSAGE), module);
            return false;
        }

        return true;
    }

    /**
     * 申请退款
     *
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> applyReturn(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dcx.getDispatcher();

        Delegator delegator = dcx.getDelegator();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String productIds = (String) context.get("productIds");
        //退款数量
        String quantitys = (String) context.get("quantitys");
        //退款原因编号
        String enumId = (String) context.get("enumId");
        //退款原因
        String returnReason = (String) context.get("returnReason");
        //退款金额
        BigDecimal applyMoney = (BigDecimal) context.get("applyMoney");
        //图片地址
        String contentIds = (String) context.get("contentIds");
        GenericValue orderRole = null;
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            orderRole = delegator.findByPrimaryKey("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", userLogin.get("partyId"), "roleTypeId", "PLACING_CUSTOMER"));

            if (UtilValidate.isEmpty(orderRole)) {
                return ServiceUtil.returnError("该订单不属于当前用户");
            }
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));

            List<GenericValue> tobeStored = FastList.newInstance();
            GenericValue returnHeader = delegator.makeValue("ReturnHeader");
            String returnId = delegator.getNextSeqId("ReturnHeader"); // 主键
            returnHeader.set("returnId", returnId);
            returnHeader.set("applyTotal", applyMoney);
            returnHeader.set("statusId", "RETURN_WAITEXAMINE");
            returnHeader.set("fromPartyId", userLogin.get("partyId"));
            returnHeader.set("createdBy", userLogin.getString("userLoginId"));
            returnHeader.set("orderId", orderId);
            returnHeader.set("entryDate", UtilDateTime.nowTimestamp());
            returnHeader.set("returnReason", returnReason);
            returnHeader.set("returnReasonId", enumId);
            BigDecimal totalQuantity = BigDecimal.ZERO;
            BigDecimal returnPrice = BigDecimal.ZERO;
            String[] productIdList = productIds.split(",");
            String[] quantityList = quantitys.split(",");

            for (int i = 0; i < productIdList.length; i++) {
                String productId = productIdList[i];
                String quantity = quantityList[i];
                totalQuantity = totalQuantity.add(new BigDecimal(quantity));
                GenericValue orderItem = EntityUtil.getFirst(delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId, "productId", productId)));
                if (UtilValidate.isNotEmpty(orderItem) && UtilValidate.areEqual("N", orderItem.get("isReturn"))) {
                    return ServiceUtil.returnError("该商品已退款或退款中");
                }
                if (new BigDecimal(quantity).compareTo(orderItem.getBigDecimal("quantity")) > 0) {
                    return ServiceUtil.returnError("最多可退" + orderItem.getBigDecimal("quantity").toString() + "件" + orderItem.getString("itemDescription"));
                }
                returnPrice = returnPrice.add(orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity")));

                orderItem.set("isReturn", "N");//已退款
                tobeStored.add(orderItem);
                // 退款单行项目
                GenericValue returnItem = delegator.makeValue("ReturnItem");
                returnItem.set("returnId", returnId);
                String returnItemId = delegator.getNextSeqId("ReturnItem"); // 主键
                returnItem.set("returnItemSeqId", returnItemId);
                returnItem.set("productId", orderItem.get("productId"));
                returnItem.set("description", orderItem.get("itemDescription"));
                returnItem.set("orderId", orderId);
                returnItem.set("orderItemSeqId", orderItem.get("orderItemSeqId"));
                returnItem.set("returnQuantity", new BigDecimal(quantity));

                returnItem.set("returnPrice", new BigDecimal(Double.parseDouble(orderItem.get("unitPrice").toString())));
                returnItem.set("returnType", "1");
                returnItem.set("statusId", "RETURN_WAITEXAMINE");
                returnItem.set("applyTime", UtilDateTime.nowTimestamp());
                tobeStored.add(returnItem);
            }

            if (UtilValidate.isNotEmpty(contentIds)) {
                String[] contentList = contentIds.split(",");
                for (int i = 0; i < contentList.length; i++) {
                    String contentId = contentList[i];
                    // 退款单行项目
                    GenericValue returnContent = delegator.makeValue("OrderReturnContent");
                    returnContent.set("contentId", contentId);
                    returnContent.set("returnId", returnId);
                    tobeStored.add(returnContent);
                }

            }
            returnHeader.set("totalQuantity", totalQuantity);

            if (applyMoney.compareTo(returnPrice) > 0) {
                return ServiceUtil.returnError("最多可退" + returnPrice.toString() + "元");
            }
            returnPrice = orderHeader.getBigDecimal("grandTotal");
            if (applyMoney.compareTo(returnPrice) > 0) {
                return ServiceUtil.returnError("最多可退" + returnPrice.toString() + "元");

            }
            tobeStored.add(returnHeader);
            // 添加操作日志
            GenericValue returnOperateLog = delegator.makeValue("ReturnOperateLog");
            String id = delegator.getNextSeqId("ReturnOperateLog");
            returnOperateLog.setPKFields(UtilMisc.toMap("id", id));
            returnOperateLog.set("returnId", returnId);
            returnOperateLog.set("operateType", "申请退款"); // 申请退款
            GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            returnOperateLog.set("operator", person.getString("nickname"));
            returnOperateLog.set("operatorTel", person.getString("mobile"));
            returnOperateLog.set("operateTime", UtilDateTime.nowTimestamp());
            returnOperateLog.set("operateReason", returnReason);
            returnOperateLog.set("createdStamp", UtilDateTime.nowTimestamp());
            tobeStored.add(returnOperateLog);
            delegator.storeAll(tobeStored);
            //增加用户退货电子邮件发送
            dispatcher.runAsync("applyReturnEmailSender", UtilMisc.toMap("userId", userLoginId, "orderId", orderId, "returnId", returnId));

            OrderChangeHelper.orderStatusChanges(dispatcher, userLogin, orderId, "ORDER_WAITEVALUATE", null, "ORDER_RETURNED", null);
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return resultData;
    }


    /**
     * 退款单列表
     *
     * @return
     */
    public static Map<String, Object> getReturnList(DispatchContext dcx, Map<String, ? extends Object> context) {
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty((String) context.get("viewIndex"))) {
            viewIndex = Integer.valueOf((String) context.get("viewIndex"));
        } else {
            viewIndex = 0;
        }

        Integer viewSize = null;
        if (UtilValidate.isNotEmpty((String) context.get("viewSize"))) {
            viewSize = Integer.valueOf((String) context.get("viewSize"));
        } else {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("RH", "ReturnHeader");
        dev.addAlias("RH", "returnId");
        dev.addAlias("RH", "statusId");
        dev.addAlias("RH", "fromPartyId");
        dev.addAlias("RH", "applyTotal");
        dev.addAlias("RH", "entryDate");
        dev.addAlias("RH", "orderId");
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("fromPartyId", userLogin.get("partyId")));
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dev, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-entryDate"), findOpts);
            List<GenericValue> returnHeaders = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> returnList = FastList.newInstance();
            for (GenericValue returnHeader : returnHeaders) {
                Map map = FastMap.newInstance();
                map.put("orderId", returnHeader.get("orderId"));
                map.put("returnId", returnHeader.get("returnId"));
                map.put("statusId", returnHeader.get("statusId"));
                map.put("applyTotal", returnHeader.get("applyTotal"));
                //退单时间
                map.put("entryDate", returnHeader.get("entryDate"));
                List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("returnId", returnHeader.get("returnId")));
                List<Map> products = FastList.newInstance();
                for (GenericValue returnItem : returnItems) {
                    Map productMap = FastMap.newInstance();
                    String productId = (String) returnItem.get("productId");
                    productMap.put("productId", returnItem.get("productId"));
                    if (UtilValidate.isNotEmpty(productId)) {
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                        if (UtilValidate.isNotEmpty(product)) {
                            productMap.put("productName", product.get("productName"));
                            ProductContentWrapper miniProductContentWrapper = new ProductContentWrapper(dispatcher, product, locale, "text/html");
                            String mediumImageUrl = miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM").toString();
                            String baseImg = UtilProperties.getMessage("application.properties", "image.base.url", locale);
                            String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                            if (uploadType.equals("FTP")) {
                                baseImg = "";
                            }
                            productMap.put("mediumImageUrl", baseImg + mediumImageUrl);
                        }
                    }
                    productMap.put("returnQuantity", returnItem.get("returnQuantity"));
                    products.add(productMap);
                }
                map.put("products", products);
                returnList.add(map);
            }

            resultData.put("returnList", returnList);
            resultData.put("max", resultSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultData;
    }

    /**
     * 拼团自动完成任务
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> togetherOrderAutoComplete(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        List<EntityCondition> exps = FastList.newInstance();
        exps.add(EntityCondition.makeCondition("status", "TOGETHER_RUNING"));
//        拼团的创建时间在系统当前时间的24小时之前，再加3小时
        Timestamp endDate = UtilDateTime.addDaysToTimestamp(UtilDateTime.nowTimestamp(), -1);
        endDate = new Timestamp(endDate.getTime() + 3L * 60L * 60L * 1000L);

        Timestamp startDate = new Timestamp(System.currentTimeMillis() - 24L * 60L * 60L * 1000L);
        exps.add(EntityCondition.makeCondition("createDate", EntityOperator.LESS_THAN, endDate));
        exps.add(EntityCondition.makeCondition("createDate", EntityOperator.GREATER_THAN, startDate));

        try {
            List<GenericValue> togetherGroups = delegator.findList("TogetherGroup", EntityCondition.makeCondition(exps, EntityOperator.AND), null, null, null, false);
            if (UtilValidate.isEmpty(togetherGroups)) {
                return resultData;
            }
            for (int i = 0; i < togetherGroups.size(); i++) {
                GenericValue togetherGroup = togetherGroups.get(i);
                Long limitUserNum = togetherGroup.getLong("limitUserNum");
                Long currentNum = togetherGroup.getLong("currentNum");
                String togetherId = togetherGroup.getString("togetherId");
                int leavePersonNums = (int) (limitUserNum - currentNum);//剩余团购数量
                if (leavePersonNums <= 0L) {
                    continue;
                }
                //随时获取leavePersonNums用户
                List<Map> list = CommonUtils.getVirtualPerson(delegator, leavePersonNums);
                if (list.size() != leavePersonNums) {
                    Debug.log("拼团定时器失败，找不到虚拟用户！");
                    continue;
                }
                //真实参与的组团用户
                List<GenericValue> realTogetherGroupRels = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));
//                List<GenericValue> toStoredList = FastList.newInstance();
                for (int j = 0; j < list.size(); j++) {
                    Map personMap = list.get(j);
                    delegator.makeValue("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId, "orderUserId", personMap.get("personId"), "orderId", "00000" + j, "headImgUrl", personMap.get("headphoto"), "createDate", UtilDateTime.nowTimestamp())).create();
                }
//                delegator.storeAll(toStoredList);

                togetherGroup.set("currentNum", limitUserNum);
                togetherGroup.set("status", "TOGETHER_DONE");
                togetherGroup.store();

                //通知微信通知模版消息
                GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "TOGETHER_SUCCESS_NOTIFY")));
                if (UtilValidate.isEmpty(config)) {
                    continue;
                }
                String template_id = config.getString("wxLiteTemplateId");
                String activityId = togetherGroup.getString("activityId");
                String productId = togetherGroup.getString("productId");
                for (GenericValue togetherGroupRel : realTogetherGroupRels) {
                    String partyId = togetherGroupRel.getString("orderUserId");
                    String orderId = togetherGroupRel.getString("orderId");
                    String sendwxAppOpenId = CommonUtils.getWxAppOpenId(delegator, partyId);

                    double price = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", productId)).get(0).getBigDecimal("activityPrice").doubleValue();
                    Map<String, Object> daMap = FastMap.newInstance();
                    daMap.put("keyword1", new Message.Data(orderId + "", ""));
                    daMap.put("keyword2", new Message.Data(ProductUtils.getProductName(productId, delegator), ""));
                    daMap.put("keyword3", new Message.Data(price + "", ""));
                    daMap.put("keyword4", new Message.Data(limitUserNum + "", ""));
                    String daJson = JsonMapper.defaultMapper().toJson(daMap);
                    try {
                        resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", sendwxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=orderDetail&orderId=" + orderId, "form_id", "", "data", daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", sendwxAppOpenId, "objectValueId", togetherId));
                    } catch (Exception e) {
                        Debug.log(e.getMessage());
                    }

                }


            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

        }
        return resultData;
    }


    public Map<String, Object> sendScore2Kd(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException, GenericServiceException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();

        Timestamp tenAgo = new Timestamp(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000);
        String tenAgoStr = CommonUtils.getStringDate(tenAgo);
        String sql = "select os.STATUS_ID,og.ORDER_GROUP_ID orderGroupId,os.STATUS_DATETIME deliverTime ,oh.ORDER_ID orderId  from ORDER_HEADER oh INNER JOIN ORDER_STATUS os on oh.ORDER_ID=os.ORDER_ID \n" +
                "INNER JOIN ORDER_GROUP_ORDER_REL ogor on ogor.ORDER_ID=oh.ORDER_ID INNER JOIN ORDER_GROUP og on ogor.ORDER_GROUP_ID = og.ORDER_GROUP_ID \n" +
                "where os.STATUS_ID = 'ORDER_WAITRECEIVE' and (og.IS_SEND_SCORE2_KD is null OR og.IS_SEND_SCORE2_KD='N') and os.STATUS_DATETIME <'"+tenAgoStr+"'";

        List<Map<String,String>> orderList =FastList.newInstance();
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();
            while (rs.next()) {
                String orderGroupId = rs.getString("orderGroupId");
                String orderId = rs.getString("orderId");
                Map<String,String> order = FastMap.newInstance();
                order.put("orderGroupId",orderGroupId);
                order.put("orderId",orderId);
                orderList.add(order);
            }
        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);
        } finally {
            try {
                sqlP.close();
            } catch (GenericDataSourceException e) {
                e.printStackTrace();
            }
        }
        if(UtilValidate.isEmpty(orderList)){
            return resultData;
        }
//        delegator.findByAnd("OrderIntegralRule",)

        for(Map<String,String>curOrder:orderList){
            String curOrderGroupId = curOrder.get("orderGroupId");
            String orderId = curOrder.get("orderId");
            GenericValue curOrderGroup = delegator.findByPrimaryKey("OrderGroup",UtilMisc.toMap("orderGroupId",curOrderGroupId));
            String isSendScore2Kd= curOrderGroup.getString("isSendScore2Kd");
            if("Y".equalsIgnoreCase(isSendScore2Kd)){
                continue;
            }
            //找到这个订单生成时间点的对应的规则区间。
            GenericValue orderStatus =delegator.findByAnd("OrderStatus",UtilMisc.toMap("orderId",orderId,"statusId","ORDER_WAITPAY")).get(0);
            Timestamp orderCreatedTime = orderStatus.getTimestamp("statusDatetime");
            //查找规则
            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("startTime", EntityOperator.LESS_THAN, orderCreatedTime),
                            EntityCondition.makeCondition("endTime", EntityOperator.GREATER_THAN, orderCreatedTime)
                    )
                    , EntityOperator.AND);
            List<GenericValue> integralRules = delegator.findList("OrderIntegralRule",mainCond,null,null,null,false);
            if(UtilValidate.isEmpty(integralRules)){
                mainCond = EntityCondition.makeCondition(
                        UtilMisc.toList(
                                EntityCondition.makeCondition("startTime", EntityOperator.LESS_THAN, orderCreatedTime),
                                EntityCondition.makeCondition("status", EntityOperator.EQUALS, "1")
                        )
                        , EntityOperator.AND);
                integralRules = delegator.findList("OrderIntegralRule",mainCond,null,null,null,false);
            }
            if(UtilValidate.isEmpty(integralRules)){
                //找不到规则，设置该订单状态结束
                Map<String, Object> updateFields = FastMap.newInstance();
                updateFields.put("isSendScore2Kd", "Y");
                EntityCondition UpdateCon = EntityCondition.makeCondition("orderGroupId", EntityComparisonOperator.EQUALS, curOrderGroupId);
                delegator.storeByCondition("OrderGroup", updateFields, UpdateCon);
            }
            //该订单所属的规则
            GenericValue integralRule = integralRules.get(0);
            String partyIntegralGiftId =integralRule.getString("partyIntegralGiftId");
            //满金额赠送积分是否开启
            String isFullOpen = integralRule.getString("isFullOpen");
            //订单满足多少钱赠送
            BigDecimal orderMoneyCond = integralRule.getBigDecimal("orderMoney");
            //满足指定商品赠送积分开关是否开启
            String isAssignOpen = integralRule.getString("isAssignOpen");
            //查询该订单的总金额
            BigDecimal orderTotalMoney = getOrderMoneyByGroupId(curOrderGroupId,delegator);

            List<GenericValue> orderRoleInfos =delegator.findByAnd("OrderRole",UtilMisc.toMap("orderId",orderId, "roleTypeId", "PLACING_CUSTOMER"));
            String curPartyId = EntityUtil.getFirst(orderRoleInfos).getString("partyId");
            GenericValue personInfo = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", curPartyId));
            String  unionId = personInfo.getString("unionId");
            String member_id="";
            String token="";
            Map<String, Object> result03 = dispatcher.runSync("kaide-getMemberIdByUnionId", UtilMisc.toMap("unionid", unionId));
            if (ServiceUtil.isError(result03)) {
                //失败，记录日志 TODO
                continue;
            }
            KdRetData retData = new Gson().fromJson((String) result03.get("result"), KdRetData.class);
            if (retData.getResult().equalsIgnoreCase("22001")) {
                //获取成功
                // 取得token 和 member_id的值
                member_id = (String) retData.getData().get("memberID");
                token = (String) retData.getData().get("token");
            }else{
                //失败，记录日志 TODO
                continue;
            }

            if("Y".equalsIgnoreCase(isFullOpen)&&orderTotalMoney.compareTo(orderMoneyCond)>=0){
                //满足条件，赠送积分 TODO

            }
            if("Y".equalsIgnoreCase(isAssignOpen)){
                List<GenericValue> integralRuleProds =delegator.findByAnd("OrderIntegralRuleProd",UtilMisc.toMap("partyIntegralGiftId",partyIntegralGiftId));
                //查找该订单下的所有商品id
                List<String> productIds = getOrderProducts(curOrderGroupId,delegator);
//                System.out.println(productIds);
                for(String productId:productIds){
                    for(GenericValue integralRuleProd:integralRuleProds){
                        String ruleProd =integralRuleProd.getString("partyGiftProdId");
                        if(productId.equalsIgnoreCase(ruleProd)){
                            //该商品需要赠送积分
                            String integralCodeNo= integralRuleProd.getString("integralCodeNo");
                            List<GenericValue> orderAttributeInfos = delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId",orderId, "attrName", "mall_Id"));

                            if (UtilValidate.isEmpty(orderAttributeInfos)) {
                                continue;
                            }
                            String mall_id = EntityUtil.getFirst(orderAttributeInfos).getString("attrValue");// 商场编号
                            GenericValue kdMallInfo = delegator.findByPrimaryKey("KdMallInfo", UtilMisc.toMap("mallId", mall_id));
                            if (UtilValidate.isNotEmpty(kdMallInfo)) {
                                String locationId = kdMallInfo.getString("locationId");
                                Map res = dispatcher.runSync("kaide-userAddScore",UtilMisc.toMap("member_id",member_id,"integralCode",integralCodeNo,"locationCode",locationId));
                            }
                            break;
                        }
                    }
                }

            }
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("isSendScore2Kd", "Y");
            EntityCondition UpdateCon = EntityCondition.makeCondition("orderGroupId", EntityComparisonOperator.EQUALS, curOrderGroupId);
            delegator.storeByCondition("OrderGroup", updateFields, UpdateCon);

        }


        return resultData;
    }

    private List<String> getOrderProducts(String curOrderGroupId, Delegator delegator) throws GenericEntityException {
        List<String> productIds = FastList.newInstance();
        List<GenericValue> orderRels = delegator.findByAnd("OrderGroupOrderRel",UtilMisc.toMap("orderGroupId",curOrderGroupId));
        for(GenericValue orderRel:orderRels) {
            String orderId = orderRel.getString("orderId");
            List<GenericValue> orderItems =delegator.findByAnd("OrderItem",UtilMisc.toMap("orderId",orderId));
            for(GenericValue orderItem:orderItems){
                String productId = orderItem.getString("productId");
                productIds.add(productId);
            }
        }
        return productIds;
    }

    private BigDecimal getOrderMoneyByGroupId(String curOrderGroupId, Delegator delegator) throws GenericEntityException {
        List<GenericValue> orderRels = delegator.findByAnd("OrderGroupOrderRel",UtilMisc.toMap("orderGroupId",curOrderGroupId));
        BigDecimal totalMoney =new BigDecimal(0);
        for(GenericValue orderRel:orderRels){
            String orderId = orderRel.getString("orderId");
            GenericValue order = delegator.findByPrimaryKey("OrderHeader",UtilMisc.toMap("orderId",orderId));
            BigDecimal orderPrice = order.getBigDecimal("grandTotal");
            totalMoney=totalMoney.add(orderPrice);
        }
        return totalMoney;

    }


}

