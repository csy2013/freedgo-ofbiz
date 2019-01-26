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
package org.ofbiz.order.order;

import com.ibm.icu.util.Calendar;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.common.DataModelConstants;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericDAO;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.kuaidi100.PostOrder;
import org.ofbiz.order.shoppingcart.*;
import org.ofbiz.order.shoppingcart.product.ProductPromoWorker;
import org.ofbiz.order.shoppingcart.shipping.ShippingEvents;
import org.ofbiz.party.contact.ContactHelper;
import org.ofbiz.party.party.PartyWorker;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.security.Security;
import org.ofbiz.service.*;
import org.ofbiz.service.calendar.RecurrenceRule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transaction;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;


/**
 * Order Processing Services
 */

public class OrderServices {
    
    public static final String module = OrderServices.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";
    public static final String resourceProduct = "ProductUiLabels";
    public static final String FINISHED_GOOD = "FINISHED_GOOD"; //实物商品
    public static final String VIRTUAL_GOOD = "VIRTUAL_GOOD";   //虚拟商品
    public static final String SMZT = "SMZT"; //上门自提
    public static final MathContext generalRounding = new MathContext(10);
    
    public static Map<String, String> salesAttributeRoleMap = FastMap.newInstance();
    public static Map<String, String> purchaseAttributeRoleMap = FastMap.newInstance();
    
    static {
        salesAttributeRoleMap.put("placingCustomerPartyId", "PLACING_CUSTOMER");
        salesAttributeRoleMap.put("billToCustomerPartyId", "BILL_TO_CUSTOMER");
        salesAttributeRoleMap.put("billFromVendorPartyId", "BILL_FROM_VENDOR");
        salesAttributeRoleMap.put("shipToCustomerPartyId", "SHIP_TO_CUSTOMER");
        salesAttributeRoleMap.put("endUserCustomerPartyId", "END_USER_CUSTOMER");
        
        purchaseAttributeRoleMap.put("billToCustomerPartyId", "BILL_TO_CUSTOMER");
        purchaseAttributeRoleMap.put("billFromVendorPartyId", "BILL_FROM_VENDOR");
        purchaseAttributeRoleMap.put("shipFromVendorPartyId", "SHIP_FROM_VENDOR");
        purchaseAttributeRoleMap.put("supplierAgentPartyId", "SUPPLIER_AGENT");
    }
    
    public static final int taxDecimals = UtilNumber.getBigDecimalScale("salestax.calc.decimals");
    public static final int taxRounding = UtilNumber.getBigDecimalRoundingMode("salestax.rounding");
    public static final int orderDecimals = UtilNumber.getBigDecimalScale("order.decimals");
    public static final int orderRounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(taxDecimals, taxRounding);
    
    
    public static boolean hasPermission(String orderId, GenericValue userLogin, String action, Security security, Delegator delegator) {
        OrderReadHelper orh = new OrderReadHelper(delegator, orderId);
        String orderTypeId = orh.getOrderTypeId();
        String partyId = null;
        GenericValue orderParty = orh.getEndUserParty();
        if (UtilValidate.isEmpty(orderParty)) {
            orderParty = orh.getPlacingParty();
        }
        if (UtilValidate.isNotEmpty(orderParty)) {
            partyId = orderParty.getString("partyId");
        }
        boolean hasPermission = hasPermission(orderTypeId, partyId, userLogin, action, security);
        if (!hasPermission) {
            GenericValue placingCustomer = null;
            try {
                Map<String, Object> placingCustomerFields = UtilMisc.<String, Object>toMap("orderId", orderId, "partyId", userLogin.getString("partyId"), "roleTypeId", "PLACING_CUSTOMER");
                placingCustomer = delegator.findByPrimaryKey("OrderRole", placingCustomerFields);
            } catch (GenericEntityException e) {
                Debug.logError("Could not select OrderRoles for order " + orderId + " due to " + e.getMessage(), module);
            }
            hasPermission = (placingCustomer != null);
        }
        return hasPermission;
    }
    
    public static boolean hasPermission(String orderTypeId, String partyId, GenericValue userLogin, String action, Security security) {
        boolean hasPermission = security.hasEntityPermission("ORDERMGR", "_" + action, userLogin);
        if (!hasPermission) {
            if ("SALES_ORDER".equals(orderTypeId)) {
                if (security.hasEntityPermission("ORDERMGR", "_SALES_" + action, userLogin)) {
                    hasPermission = true;
                } else {
                    // check sales agent/customer relationship
                    List<GenericValue> repsCustomers = new LinkedList<GenericValue>();
                    try {
                        repsCustomers = EntityUtil.filterByDate(userLogin.getRelatedOne("Party").getRelatedByAnd("FromPartyRelationship",
                                UtilMisc.toMap("roleTypeIdFrom", "AGENT", "roleTypeIdTo", "CUSTOMER", "partyIdTo", partyId)));
                    } catch (GenericEntityException ex) {
                        Debug.logError("Could not determine if " + partyId + " is a customer of user " + userLogin.getString("userLoginId") + " due to " + ex.getMessage(), module);
                    }
                    if ((repsCustomers != null) && (repsCustomers.size() > 0) && (security.hasEntityPermission("ORDERMGR", "_ROLE_" + action, userLogin))) {
                        hasPermission = true;
                    }
                    if (!hasPermission) {
                        // check sales sales rep/customer relationship
                        try {
                            repsCustomers = EntityUtil.filterByDate(userLogin.getRelatedOne("Party").getRelatedByAnd("FromPartyRelationship",
                                    UtilMisc.toMap("roleTypeIdFrom", "SALES_REP", "roleTypeIdTo", "CUSTOMER", "partyIdTo", partyId)));
                        } catch (GenericEntityException ex) {
                            Debug.logError("Could not determine if " + partyId + " is a customer of user " + userLogin.getString("userLoginId") + " due to " + ex.getMessage(), module);
                        }
                        if ((repsCustomers != null) && (repsCustomers.size() > 0) && (security.hasEntityPermission("ORDERMGR", "_ROLE_" + action, userLogin))) {
                            hasPermission = true;
                        }
                    }
                }
            } else if (("PURCHASE_ORDER".equals(orderTypeId) && (security.hasEntityPermission("ORDERMGR", "_PURCHASE_" + action, userLogin)))) {
                hasPermission = true;
            }
        }
        return hasPermission;
    }
    
    
    /**
     * Service for creating a new order
     */
    public static Map<String, Object> createOrder(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Security security = ctx.getSecurity();
        List<GenericValue> toBeStored = new LinkedList<GenericValue>();
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        // get the order type
        String orderTypeId = (String) context.get("orderTypeId");
        String partyId = (String) context.get("partyId");
        
        //订单金额
        BigDecimal grandTotal = (BigDecimal) context.get("grandTotal");
        
        //新增字段
        String mall_id = (String) context.get("mall_id");
        String member_id = (String) context.get("member_id");
        String channelId = (String) context.get("channelId");
        
        //团购、秒杀
        String activityId = (String) context.get("activityId");
//        String cartType = (String) context.get("cartType");
        //具体价格等，保存在orderHeader
        
        Object remarks = context.get("remarks");
        String invoiceType = (String) context.get("invoiceType");           //发票类型
        String invoiceTitle = (String) context.get("invoiceTitle");         //发票抬头
        String invoiceContentTypeId = (String) context.get("invoiceContentTypeId");         //发票抬头
        String needInvoice = (String) context.get("needInvoice");     //是否需要发票
        String taxNo = (String) context.get("taxNo"); //纳税人识别号
        String token = (String) context.get("token"); //用户token
        String saleOrderTypeId = (String) context.get("saleOrderTypeId");
        // get the product store for the order, but it is required only for sales orders
        String productStoreId = (String) context.get("productStoreId");
        String providerId = (String)context.get("providerId");
        
        // create the order object
        String orderId = (String) context.get("orderId");
        if (UtilValidate.isEmpty(orderId)) {
            // for purchase orders or when other orderId generation fails, a product store id should not be required to make an order
            orderId = delegator.getNextSeqId("OrderHeader");
        }
        
        // check to make sure we have something to order
        List<GenericValue> orderItems = UtilGenerics.checkList(context.get("orderItems"));
        if (orderItems.size() < 1) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error, "items.none", locale));
        }
        /*订单调整项*/
        List<GenericValue> orderAdjustments = UtilGenerics.checkList(context.get("orderAdjustments"));
        if (UtilValidate.isEmpty(orderAdjustments)) {
            orderAdjustments = FastList.newInstance();
        }
        
        //检查商品
        // check inventory and other things for each item
        List<String> errorMessages = FastList.newInstance();
        Map<String, BigDecimal> normalizedItemQuantities = FastMap.newInstance();
        Map<String, String> normalizedItemNames = FastMap.newInstance();
        Map<String, GenericValue> itemValuesBySeqId = FastMap.newInstance();
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        
        for (GenericValue orderItem : orderItems) {
            //处理商品库存
            GenericValue inventoryItem = null;
            if (dealInventoryItem(delegator, orderItem, inventoryItem)) {
                return ServiceUtil.returnError("商品库存不足");
            }
            
            // start by putting it in the itemValuesById Map
            itemValuesBySeqId.put(orderItem.getString("orderItemSeqId"), orderItem);
            String currentProductId = orderItem.getString("productId");
            /*GenericValue product = null;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", currentProductId));
                //增加购买数量限制
                BigDecimal purchaseLimitationQuantity = product.getBigDecimal("purchaseLimitationQuantity");
                if (UtilValidate.isNotEmpty(purchaseLimitationQuantity)) {
//                    查找之前的下单记录数
                    List<EntityCondition> mainExps = FastList.newInstance();
                    mainExps.add(EntityCondition.makeCondition("partyId", partyId));
                    mainExps.add(EntityCondition.makeCondition("roleTypeId", "PLACING_CUSTOMER"));
                    mainExps.add(EntityCondition.makeCondition("productId", currentProductId));
                    mainExps.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"));
                    List<GenericValue> oldOrders = delegator.findList("OrderHeaderItemAndRoles", EntityCondition.makeCondition(mainExps, EntityOperator.AND), UtilMisc.toSet("orderId", "productId", "quantity", "itemDescription"), null, null, false);
                    BigDecimal totalOrder = BigDecimal.ZERO;
                    String itemDescription = "";
                    if (UtilValidate.isNotEmpty(oldOrders)) {
                        for (int i = 0; i < oldOrders.size(); i++) {
                            GenericValue oldOrder = oldOrders.get(i);
                            totalOrder = totalOrder.add(oldOrder.getBigDecimal("quantity"));
                            itemDescription = oldOrder.getString("itemDescription");
                        }
                    }
                    if (totalOrder.compareTo(purchaseLimitationQuantity) >= 0) {
                        String productName = product.getString("productName");
                        return ServiceUtil.returnError(productName + "的购买数量超过该产品购买数量限制");
                    }
                }
            } catch (GenericEntityException e) {
                String errMsg = UtilProperties.getMessage(resource_error, "product.not_found", new Object[]{currentProductId}, locale);
                Debug.logError(e, errMsg, module);
                errorMessages.add(errMsg);
                continue;
            }*/
            if (currentProductId != null) {
                // only normalize items with a product associated (ignore non-product items)
                if (normalizedItemQuantities.get(currentProductId) == null) {
                    normalizedItemQuantities.put(currentProductId, orderItem.getBigDecimal("quantity"));
                    normalizedItemNames.put(currentProductId, orderItem.getString("itemDescription"));
                } else {
                    BigDecimal currentQuantity = normalizedItemQuantities.get(currentProductId);
                    normalizedItemQuantities.put(currentProductId, currentQuantity.add(orderItem.getBigDecimal("quantity")));
                }
                try {
                    // count product ordered quantities
                    // run this synchronously so it will run in the same transaction
                    dispatcher.runSync("countProductQuantityOrdered", UtilMisc.<String, Object>toMap("productId", currentProductId, "quantity", orderItem.getBigDecimal("quantity"), "userLogin", userLogin));
                    /*if(UtilRedis.exists(currentProductId+"_summary")) {
                        UtilRedis.del(currentProductId + "_summary");
                    }
                    if(UtilRedis.exists(currentProductId+"_detail")) {
                        UtilRedis.del(currentProductId + "_detail");
                    }*/
                } catch (GenericServiceException e1) {
                    Debug.logError(e1, "Error calling countProductQuantityOrdered service", module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCallingCountProductQuantityOrderedService", locale) + e1.toString());
                }
                
            }
        }
        if (errorMessages.size() > 0) {
            return ServiceUtil.returnError(errorMessages);
        }
        
        //使用积分
        BigDecimal useIntegral = (BigDecimal) context.get("useIntegral");
        BigDecimal integralDiscount = (BigDecimal) context.get("integralDiscount");
        Boolean userScore = false;
        //如果是团购、秒杀活动
        if ("seckillCart".equals(saleOrderTypeId) || "togetherCart".equals(saleOrderTypeId)) {
            if (UtilValidate.isEmpty(orderItems) || orderItems.size() != 1) {
                return ServiceUtil.returnError("团购或秒杀商品不存在或超出一种商品");
            }
            GenericValue orderItem = orderItems.get(0);
            if ("seckillCart".equals(saleOrderTypeId)) {
                try {
                    Map<String, Object> togetherData = ProductActivityServices.validateSeckillStatus(partyId, activityId, orderItem, productStoreId, delegator);
                    if (ServiceUtil.isError(togetherData)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(togetherData));
                    }
//                    grandTotal = (BigDecimal) togetherData.get("totalPrice");
//                    BigDecimal unitPrice = (BigDecimal) togetherData.get("activityPrice");
//                    orderItem.set("unitPrice", unitPrice);
//                    orderItem.set("selectedAmount", grandTotal);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError(e.getMessage());
                }
            } else if ("togetherCart".equals(saleOrderTypeId)) {
                try {
                    Map<String, Object> togetherData = ProductActivityServices.validateTogetherStatus(partyId, activityId, orderItem, productStoreId, delegator);
                    if (ServiceUtil.isError(togetherData)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(togetherData));
                    }
//                    grandTotal = (BigDecimal) togetherData.get("totalPrice");
//                    BigDecimal unitPrice = (BigDecimal) togetherData.get("activityPrice");
//                    orderItem.set("unitPrice", unitPrice);
//                    orderItem.set("selectedAmount", grandTotal);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        
        /*如果使用了星积分*/
//        BigDecimal totalStartScoreAmount = BigDecimal.ZERO;
        /*if (UtilValidate.isNotEmpty(useIntegral) && useIntegral.compareTo(BigDecimal.ZERO) != 0) {
//            userScore = true;
            //检查商品是否可以使用积分
            *//*List<String> productIds = FastList.newInstance();
            for (GenericValue product : orderItems) {
                productIds.add(product.getString("productId"));
            }
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            List<EntityCondition> exps = FastList.newInstance();
            exps.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productIds));
            exps.add(EntityCondition.makeCondition("integralDeductionType", EntityOperator.IN, UtilMisc.toList("2", "3")));
            List<GenericValue> validateProds = null;
            try {
                validateProds = delegator.findList("Product", EntityCondition.makeCondition(exps, EntityOperator.AND), UtilMisc.toSet("productId"), null, findOpts, false);
                if (UtilValidate.isEmpty(validateProds) || validateProds.size() == 0) {
                    return ServiceUtil.returnError("不存在可以用积分兑换的商品");
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
                
            }
            *//*
            String description = (String) context.get("description");
            if (UtilValidate.isEmpty(description)) {
                description = "用户通过星积分下单";
            }
            try {
                if (UtilValidate.isEmpty(token)) {
                    token = "";
                }
                Map<String, Object> scoreRes = dispatcher.runSync("kaide-consumeIntegral", UtilMisc.toMap("member_id", member_id, "mall_id", mall_id, "integral", useIntegral.toString(), "description", description, "merchant_id", mall_id, "token", token));
                Map retData = new Gson().fromJson((String) scoreRes.get("result"), Map.class);
                if (UtilValidate.isNotEmpty(retData)) {
                    int result = ((Double) retData.get("result")).intValue();
                    if (result != 1) {
                        return ServiceUtil.returnError("积分扣减失败：member_id:" + member_id + "金额：" + useIntegral);
                    }
                } else {
                    return ServiceUtil.returnError("积分扣减失败：member_id:" + member_id + "金额：" + useIntegral );
                }
                if (ServiceUtil.isError(scoreRes)) {
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(scoreRes));
                } else {
                    //判断积分返回信息是否成功
                    //扣减订单金额，通过生成订单调整信息
                   *//* GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
                    orderAdjustment.set("orderId", orderId);
                    orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
                    orderAdjustment.set("createdByUserLogin", userLogin.getString("userLoginId"));
                    
                    if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                        orderAdjustment.set("orderItemSeqId", DataModelConstants.SEQ_ID_NA);
                    }
                    if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                        orderAdjustment.set("shipGroupSeqId", DataModelConstants.SEQ_ID_NA);
                    }
                    orderAdjustment.set("description", "星积分抵扣订单金额");
                    orderAdjustment.set("orderAdjustmentTypeId", "INTEGRAL_ADJUESTMENT");
                    
                    orderAdjustment.set("amount", integralDiscount.multiply(new BigDecimal(-1)));
                    //增加积分兑换金额
                    orderAdjustment.set("recurringAmount", useIntegral);
                    orderAdjustments.add(orderAdjustment);*//*
            
//                    grandTotal = grandTotal.subtract(integralDiscount);
                    GenericValue partyAccountDetail = delegator.makeValue("PartyAccountDetail");
                    partyAccountDetail.set("detailId", delegator.getNextSeqId("PartyAccountDetail"));
                    partyAccountDetail.set("partyId", partyId);
                    partyAccountDetail.set("amount", useIntegral);
                    Date date = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    partyAccountDetail.set("createDate", Timestamp.valueOf(sdf.format(date)));
                    partyAccountDetail.set("description", "订单支付，订单号：" + orderId);
                    partyAccountDetail.set("operator", partyId);
                    
                   *//* //计算订单项分摊
                    BigDecimal totalScoreAmount = BigDecimal.ZERO;
                    for (int i = 0; i < validateProds.size(); i++) {
                        for (GenericValue orderItem : orderItems) {
                            if (validateProds.get(i).getString("productId").equals(orderItem.getString("productId"))) {
                                totalScoreAmount = totalScoreAmount.add(orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity")));
                            }
                            
                        }
                    }
                    
                    for (int i = 0; i < validateProds.size(); i++) {
                        for (GenericValue orderItem : orderItems) {
                            if (validateProds.get(i).getString("productId").equals(orderItem.getString("productId"))) {
                                BigDecimal itemTotalPrice = orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity"));
                                BigDecimal scoreItemPrice = itemTotalPrice.divide(totalScoreAmount, 12, RoundingMode.HALF_UP).multiply(integralDiscount).setScale(2, RoundingMode.HALF_UP);
                                BigDecimal totalRealAmount = orderItem.getBigDecimal("totalRealAmount") == null ? BigDecimal.ZERO : orderItem.getBigDecimal("totalRealAmount");
                                orderItem.set("totalRealAmount", totalRealAmount.add(scoreItemPrice));
                            }
                            
                        }
                    }*//*
                }
            } catch (GenericServiceException e) {
                e.printStackTrace();
                Debug.log("yabiz商城积分变更服务调用错误！");
                return ServiceUtil.returnError("yabiz商城积分变更服务调用错误");
            }
        }*/
        
        //如果使用了优惠劵
        /*BigDecimal totalCouponAmount = BigDecimal.ZERO;
        String productPromoCodeId = (String) context.get("productPromoCodeId");
        if (UtilValidate.isNotEmpty(productPromoCodeId)) {
            String[] codeIds = productPromoCodeId.split(",");
            for (int i = 0; i < codeIds.length; i++) {
                String codeId = codeIds[i];
                //检查优惠劵是否有效
                //检查优惠劵code是否有效
                try {
                    GenericValue productPromoCodeParty = delegator.findByPrimaryKey("ProductPromoCodeParty", UtilMisc.toMap("productPromoCodeId", codeId, "partyId", partyId));
                    if (UtilValidate.isNotEmpty(productPromoCodeParty)) {
                        GenericValue productPromoCode = productPromoCodeParty.getRelatedOne("ProductPromoCode");
                        if (UtilValidate.isNotEmpty(productPromoCode)) {
                            String promoCodeStatus = productPromoCode.getString("promoCodeStatus");
                            if ("D".equals(promoCodeStatus) || "U".equals(promoCodeStatus)) {
                                return ServiceUtil.returnError("优惠劵已经被使用或者过期");
                            }
                        }
                        Map<String, Object> restData = CouponServices.validateCouponCodeUseStatus(orderItems, grandTotal, userScore, codeId, productStoreId, delegator);
                        if (ServiceUtil.isError(restData)) {
                            return ServiceUtil.returnError("优惠劵有效性检查不通过");
                        }    else {
                            productPromoCode.set("promoCodeStatus", "U");
                            delegator.store(productPromoCode);
                            GenericValue productPromoCoupon = productPromoCode.getRelatedOne("ProductPromoCoupon");
                            //优惠劵Code标注已使用、优惠劵更新order数
//                            Long couponQuantity = productPromoCoupon.getLong("couponQuantity") == null ? 0l : productPromoCoupon.getLong("couponQuantity");
//                            productPromoCoupon.set("couponQuantity", couponQuantity - 1);
                            Long orderCount = productPromoCoupon.getLong("orderCount") == null ? 0L : productPromoCoupon.getLong("orderCount");
                            productPromoCoupon.set("orderCount", orderCount + 1);
                            delegator.store(productPromoCoupon);
                            //做订单金额调整
                            //获取优惠劵的面值
                            BigDecimal payReduce = productPromoCoupon.getBigDecimal("payReduce");
                            totalCouponAmount = totalCouponAmount.add(payReduce);
                            grandTotal = grandTotal.subtract(payReduce);
                            //增加订单项
                            GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
                            orderAdjustment.set("orderId", orderId);
                            orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
                            orderAdjustment.set("createdByUserLogin", userLogin.getString("userLoginId"));
                            orderAdjustment.set("sourceReferenceId", productPromoCodeId);
                            if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                                orderAdjustment.set("orderItemSeqId", DataModelConstants.SEQ_ID_NA);
                            }
                            if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                                orderAdjustment.set("shipGroupSeqId", DataModelConstants.SEQ_ID_NA);
                            }
                            orderAdjustment.set("description", "优惠劵抵扣订单金额");
                            orderAdjustment.set("orderAdjustmentTypeId", "COUPON_ADJUESTMENT");
                            orderAdjustment.set("amount", payReduce.multiply(new BigDecimal(-1)));
                            //优惠劵名称
                            orderAdjustment.set("comments", productPromoCoupon.get("couponName"));
                            orderAdjustments.add(orderAdjustment);
                            
                            //
                            //计算订单项分摊
                            BigDecimal totalCouponRealAmount = BigDecimal.ZERO;
                            List<String> validateProds = (List<String>) restData.get("productIds");
                            for (int j = 0; j < validateProds.size(); j++) {
                                for (GenericValue orderItem : orderItems) {
                                    if (validateProds.get(j).equals(orderItem.getString("productId"))) {
                                        totalCouponRealAmount = totalCouponRealAmount.add(orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity")));
                                    }
            
                                }
                            }
    
                            for (int k = 0; k < validateProds.size(); k++) {
                                for (GenericValue orderItem : orderItems) {
                                    if (validateProds.get(k).equals(orderItem.getString("productId"))) {
                                        BigDecimal itemTotalPrice = orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity"));
                                        BigDecimal couponItemPrice = itemTotalPrice.divide(totalCouponRealAmount, 12, RoundingMode.HALF_UP).multiply(payReduce).setScale(2, RoundingMode.HALF_UP);
                                        BigDecimal totalRealAmount = orderItem.getBigDecimal("totalRealAmount") == null ? BigDecimal.ZERO : orderItem.getBigDecimal("totalRealAmount");
                                        orderItem.set("totalRealAmount", totalRealAmount.add(couponItemPrice));
                                    }
            
                                }
                            }
                        }
                    }
                    
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }*/
        
        
        //  如果使用了代金劵
       /* String ticketIdStr = (String) context.get("ticketId");
        BigDecimal totalTicketAmount = BigDecimal.ZERO;
        if (UtilValidate.isNotEmpty(ticketIdStr)) {
            String[] ticketIds = ticketIdStr.split(",");
            for (int i = 0; i < ticketIds.length; i++) {
                String ticketId = ticketIds[i];
                //检查优惠劵是否有效
                //检查优惠劵code是否有效
                try {
                    List<GenericValue> tickets = delegator.findByAnd("Ticket", UtilMisc.toMap("ticketId", ticketId));
                    if (UtilValidate.isNotEmpty(tickets)) {
                        GenericValue ticket = tickets.get(0);
                        if (UtilValidate.isNotEmpty(ticket)) {
                            String status = ticket.getString("ticketStatus");
                            if (!"notUsed".equals(status)) {
                                return ServiceUtil.returnError("代金劵已经被使用或者过期");
                            }
                            if (!ticket.getString("partyId").equals(partyId)) {
                                return ServiceUtil.returnError("代金劵不是本人领取的");
                            }
                        }
                        
                        ticket.set("ticketStatus", "hasUsed");
                        ticket.set("useDate", UtilDateTime.nowTimestamp());
                        delegator.store(ticket);
                        String productId = ticket.getString("productId");
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                        //做订单金额调整
                        //获取优惠劵的面值
                        BigDecimal amount = product.getBigDecimal("voucherAmount");
                        totalTicketAmount = totalTicketAmount.add(amount);
                        grandTotal = grandTotal.subtract(amount);
                        //增加订单项
                        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
                        orderAdjustment.set("orderId", orderId);
                        orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
                        orderAdjustment.set("createdByUserLogin", userLogin.getString("userLoginId"));
                        orderAdjustment.set("sourceReferenceId", ticket.getString("ticketId"));
                        if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                            orderAdjustment.set("orderItemSeqId", DataModelConstants.SEQ_ID_NA);
                        }
                        if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                            orderAdjustment.set("shipGroupSeqId", DataModelConstants.SEQ_ID_NA);
                        }
                        orderAdjustment.set("description", "代金劵抵扣订单金额");
                        orderAdjustment.set("orderAdjustmentTypeId", "TICKET_ADJUESTMENT");
                        orderAdjustment.set("comments", ticket.getString("ticketName"));
                        orderAdjustment.set("amount", amount.multiply(new BigDecimal(-1)));
                        orderAdjustments.add(orderAdjustment);
                        //计算订单项分摊
                        List<String> validateProds = FastList.newInstance();
                        List<GenericValue> products = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_CONF"));
                        if(UtilValidate.isNotEmpty(products)){
                            for (int j = 0; j < products.size(); j++) {
                                GenericValue productObj = products.get(j);
                                validateProds.add(productObj.getString("productId"));
                            }
                        }else{
                            for (GenericValue orderItem : orderItems) {
                                validateProds.add(orderItem.getString("productId"));
                            }
                        }
                        BigDecimal totalCouponRealAmount = BigDecimal.ZERO;
                       
                        for (int j = 0; j < validateProds.size(); j++) {
                            for (GenericValue orderItem : orderItems) {
                                if (validateProds.get(j).equals(orderItem.getString("productId"))) {
                                    totalCouponRealAmount = totalCouponRealAmount.add(orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity")));
                                }
            
                            }
                        }
    
                        for (int k = 0; k < validateProds.size(); k++) {
                            for (GenericValue orderItem : orderItems) {
                                if (validateProds.get(k).equals(orderItem.getString("productId"))) {
                                    BigDecimal itemTotalPrice = orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity"));
                                    BigDecimal couponItemPrice = itemTotalPrice.divide(totalCouponRealAmount, 12, RoundingMode.HALF_UP).multiply(amount).setScale(2, RoundingMode.HALF_UP);
                                    BigDecimal totalRealAmount = orderItem.getBigDecimal("totalRealAmount") == null ? BigDecimal.ZERO : orderItem.getBigDecimal("totalRealAmount");
                                    orderItem.set("totalRealAmount", totalRealAmount.add(couponItemPrice));
                                }
            
                            }
                        }
                    }
                    
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }*/
        
        
        String ownerPartyId = "";
        GenericValue productStore = null;
        if (("SALES_ORDER".equals(orderTypeId)) && (UtilValidate.isNotEmpty(productStoreId))) {
            try {
                productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
                ownerPartyId = productStore.getString("ownerPartyId");
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderErrorCouldNotFindProductStoreWithID", UtilMisc.toMap("productStoreId", productStoreId), locale) + e.toString());
            }
        }
        
        
        successResult.put("orderTypeId", orderTypeId);
        List<GenericValue> orderItemShipGroupInfo = UtilGenerics.checkList(context.get("orderItemShipGroupInfo"));
        Timestamp orderDate = (Timestamp) context.get("orderDate");

//        检查是否超出商品的购买限制
        // set the order payment info 支付信息
        List<GenericValue> orderPaymentInfos = UtilGenerics.checkList(context.get("orderPaymentInfo"));
        
        //得到支付方式
        String paymentMethodTypeId = "";
        if (UtilValidate.isNotEmpty(orderPaymentInfos)) {
            for (GenericValue valueObj : orderPaymentInfos) {
                paymentMethodTypeId = valueObj.getString("paymentMethodTypeId");
            }
        }
        
        List<GenericValue> products = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderItems)) {
            for (GenericValue orderItem : orderItems) {
                GenericValue product = null;
                try {
                    product = delegator.getRelatedOne("Product", orderItem);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                products.add(product);
            }
        }
        //如果包含了实物商品和虚拟商品的情况
        
        String initialStatus = "ORDER_WAITPAY";
        successResult.put("statusId", initialStatus);
        
        
        if (orderDate == null) {
            orderDate = nowTimestamp;
        }
        
        Map<String, Object> orderHeaderMap = UtilMisc.<String, Object>toMap("orderId", orderId, "orderTypeId", orderTypeId, "orderDate", orderDate, "entryDate", nowTimestamp, "statusId", initialStatus);
        orderHeaderMap.put("orderName", context.get("orderName"));
        orderHeaderMap.put("invoicePerShipment", context.get("invoicePerShipment"));
        
        GenericValue orderHeader = delegator.makeValue("OrderHeader", orderHeaderMap);
        /*订单类型:普通（SALE_ORDER_CART）、团购(SALE_ORDER_GROUP)、秒杀(SALE_ORDER_SECKILL)、礼品(SALE_ORDER_GIFT)、SALE_ORDER_WISH 心愿单*/
        if ("shoppingCart".equals(saleOrderTypeId)) {
            orderHeader.put("saleOrderTypeId", "SALE_ORDER_CART");
        } else if ("togetherCart".equals(saleOrderTypeId)) {
            orderHeader.put("saleOrderTypeId", "SALE_ORDER_GROUP");
        } else if ("wishCart".equals(saleOrderTypeId)) {
            orderHeader.put("saleOrderTypeId", "SALE_ORDER_WISH");
        } else if ("giftCart".equals(saleOrderTypeId)) {
            orderHeader.put("saleOrderTypeId", "SALE_ORDER_GIFT");
        } else if ("seckillCart".equals(saleOrderTypeId)) {
            orderHeader.put("saleOrderTypeId", "SALE_ORDER_SECKILL");
        }
        orderHeader.put("activityId", activityId);
        orderHeader.set("salesChannelEnumId", channelId);
        if (context.get("currencyUom") != null) {
            orderHeader.set("currencyUom", context.get("currencyUom"));
        }
        orderHeader.set("grandTotal", grandTotal);
        if (UtilValidate.isNotEmpty(context.get("visitId"))) {
            orderHeader.set("visitId", context.get("visitId"));
        }
        if (UtilValidate.isNotEmpty(context.get("originFacilityId"))) {
            orderHeader.set("originFacilityId", context.get("originFacilityId"));
        }
        
        if (UtilValidate.isNotEmpty(context.get("productStoreId"))) {
            orderHeader.set("productStoreId", context.get("productStoreId"));
        }
        
        if (UtilValidate.isNotEmpty(context.get("providerId"))){
            orderHeader.set("providerId",context.get("providerId"));
        }
        
        if (UtilValidate.isNotEmpty(context.get("autoOrderShoppingListId"))) {
            orderHeader.set("autoOrderShoppingListId", context.get("autoOrderShoppingListId"));
        }
        
        if (UtilValidate.isNotEmpty(context.get("webSiteId"))) {
            orderHeader.set("webSiteId", context.get("webSiteId"));
        }
        
        if (userLogin != null && userLogin.get("userLoginId") != null) {
            orderHeader.set("createdBy", userLogin.getString("userLoginId"));
        }
        
        String invoicePerShipment = UtilProperties.getPropertyValue("AccountingConfig", "create.invoice.per.shipment");
        if (UtilValidate.isNotEmpty(invoicePerShipment)) {
            orderHeader.set("invoicePerShipment", invoicePerShipment);
        }
        
        orderHeader.set("saleOrderTypeId", saleOrderTypeId);
        // first try to create the OrderHeader; if this does not fail, continue.
        try {
            delegator.create(orderHeader);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot create OrderHeader entity; problems with insert", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderOrderCreationFailedPleaseNotifyCustomerService", locale));
        }

//        System.out.println(Thread.currentThread().getName() + "+++++++++++++++++++++++++ 1715 :" + sdf1.format(new java.util.Date()));
        // create the order status record
        String orderStatusSeqId = delegator.getNextSeqId("OrderStatus");
        GenericValue orderStatus = delegator.makeValue("OrderStatus", UtilMisc.toMap("orderStatusId", orderStatusSeqId));
        orderStatus.set("orderId", orderId);
        orderStatus.set("statusId", orderHeader.getString("statusId"));
        orderStatus.set("statusDatetime", nowTimestamp);
        orderStatus.set("statusUserLogin", userLogin.getString("userLoginId"));
        toBeStored.add(orderStatus);
        
        // before processing orderItems process orderItemGroups so that they'll be in place for the foreign keys and what not
        List<GenericValue> orderItemGroups = UtilGenerics.checkList(context.get("orderItemGroups"));
        if (UtilValidate.isNotEmpty(orderItemGroups)) {
            for (GenericValue orderItemGroup : orderItemGroups) {
                orderItemGroup.set("orderId", orderId);
                toBeStored.add(orderItemGroup);
            }
        }
        //创建用户订单role
        
        toBeStored.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", partyId, "roleTypeId", "PLACING_CUSTOMER")));
        toBeStored.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", partyId, "roleTypeId", "BILL_TO_CUSTOMER")));
        toBeStored.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", ownerPartyId, "roleTypeId", "BILL_FROM_VENDOR")));
        
        // set the order items
        //生成订单项,计算每个订单项的实付单价、实付金额
        BigDecimal itemSubTotal = OrderReadHelper.getOrderItemsSubTotal1(orderItems, null);
        BigDecimal adjustmentSubTotal = BigDecimal.ZERO;
        if (UtilValidate.isNotEmpty(orderAdjustments)) {
            for (int i = 0; i < orderAdjustments.size(); i++) {
                GenericValue adjustment = orderAdjustments.get(i);
                if (!"SHIPPING_CHARGES".equals(adjustment.getString("orderAdjustmentTypeId"))) {
                    adjustmentSubTotal = adjustmentSubTotal.add(adjustment.getBigDecimal("amount"));
                }
            }
        }
        
        for (GenericValue orderItem : orderItems) {
            orderItem.set("orderId", orderId);
            GenericValue product = null;
            try {
                product = orderItem.getRelatedOne("Product");
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if ("FINISHED_GOOD".equals(product.get("productTypeId"))) {
                /*将状态改为待支付 EXT_COD: 货到付款 */
                if ("EXT_COD".equals(paymentMethodTypeId)) {
                    //货到付款状态直接变为待发货
                    orderItem.set("statusId", "ITEM_WAITSHIP");
                } else {
                    orderItem.set("statusId", "ITEM_WAITPAY");
                }
            } else if ("VIRTUAL_GOOD".equals(product.get("productTypeId"))) {
                orderItem.set("statusId", "ITEM_WAITPAY");
            }
            //促销上实际优惠的金额
            BigDecimal totalRealAmount = orderItem.getBigDecimal("totalRealAmount") == null ? BigDecimal.ZERO : orderItem.getBigDecimal("totalRealAmount");
            //代金劵
//            totalRealAmount = totalRealAmount.add(totalTicketAmount);
            //优惠劵
//            totalRealAmount = totalRealAmount.add(totalCouponAmount);
            //积分
//            totalRealAmount = totalRealAmount.add(totalStartScoreAmount);
            
            BigDecimal itemSubAmount = orderItem.getBigDecimal("unitPrice").multiply(orderItem.getBigDecimal("quantity"));
            
            //每个订单项分摊的优惠金额： 订单项金额/整个订单金额 * 优惠金额
            BigDecimal totalRealAmount1 = itemSubAmount.subtract(totalRealAmount);
            //实际该订单项分摊的支付金额
            orderItem.set("totalRealAmount", totalRealAmount1);
            //实际分摊的退货商品单价，用于退货时的商品价格
            orderItem.set("realPrice", totalRealAmount1.divide(orderItem.getBigDecimal("quantity"), 2, RoundingMode.HALF_UP));
            
            //商品的售价
            BigDecimal lastUnitPrice = orderItem.getBigDecimal("unitPrice");
            try {
                List<GenericValue> productPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product.getString("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                productPrices = EntityUtil.filterByDate(productPrices);
                if (UtilValidate.isNotEmpty(productPrices)) {
                    GenericValue productPrice = EntityUtil.getFirst(productPrices);
                    lastUnitPrice = productPrice.getBigDecimal("price");
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            }
            orderItem.set("lastUnitPrice", lastUnitPrice);
            toBeStored.add(orderItem);
            // create the item status record
            String itemStatusId = delegator.getNextSeqId("OrderStatus");
            GenericValue itemStatus = delegator.makeValue("OrderStatus", UtilMisc.toMap("orderStatusId", itemStatusId));
            itemStatus.put("statusId", orderItem.get("statusId"));
            itemStatus.put("orderId", orderId);
            itemStatus.put("orderItemSeqId", orderItem.get("orderItemSeqId"));
            itemStatus.put("statusDatetime", nowTimestamp);
            itemStatus.set("statusUserLogin", userLogin.getString("userLoginId"));
            toBeStored.add(itemStatus);
            
            
        }
//        System.out.println(Thread.currentThread().getName() + "+++++++++++++++++++++++++ 1774 :" + sdf1.format(new java.util.Date()));
        // set the order attributes
        List<GenericValue> orderAttributes = UtilGenerics.checkList(context.get("orderAttributes"));
        //add by dongxiao 2016.2.15 保存订单发票相关内容
        
        if (UtilValidate.isNotEmpty(needInvoice) && "Y".equals(needInvoice)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "needInvoice", "attrValue", "Y")));
            //需要发票，需保存发票抬头和发票内容
            if (UtilValidate.isNotEmpty(invoiceType)) {
                
                orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "invoiceType", "attrValue", invoiceType)));
            }
            if (UtilValidate.isNotEmpty(invoiceTitle)) {
                orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "invoiceTitle", "attrValue", invoiceTitle)));
            }
        } else {
            
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "needInvoice", "attrValue", "N")));
        }
        if (UtilValidate.isNotEmpty(taxNo)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "taxNo", "attrValue", taxNo)));
        }
        
        if (UtilValidate.isNotEmpty(mall_id)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "mall_id", "attrValue", mall_id)));
        }
        if (UtilValidate.isNotEmpty(member_id)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "member_id", "attrValue", member_id)));
        }
        if (UtilValidate.isNotEmpty(invoiceContentTypeId)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "invoiceContentTypeId", "attrValue", invoiceContentTypeId)));
        }
        String partyNo = (String) context.get("partyNo");
        if (UtilValidate.isNotEmpty(partyNo)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "partyNo", "attrValue", partyNo)));
        }
        
        
        if (UtilValidate.isNotEmpty(useIntegral)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "useIntegral", "attrValue", useIntegral.toString())));
        }
        //add By AlexYao 2016/01/30 积分抵扣金额
        if (UtilValidate.isNotEmpty(integralDiscount) && UtilValidate.isNotEmpty(useIntegral)) {
            orderAttributes.add(delegator.makeValue("OrderAttribute", UtilMisc.toMap("attrName", "integralDiscount", "attrValue", integralDiscount.toString())));
        }
        
        
        if (UtilValidate.isNotEmpty(orderAttributes)) {
            for (GenericValue oatt : orderAttributes) {
                oatt.set("orderId", orderId);
                toBeStored.add(oatt);
            }
        }
        
        // set the order item attributes
        List<GenericValue> orderItemAttributes = UtilGenerics.checkList(context.get("orderItemAttributes"));
        if (UtilValidate.isNotEmpty(orderItemAttributes)) {
            for (GenericValue oiatt : orderItemAttributes) {
                oiatt.set("orderId", orderId);
                toBeStored.add(oiatt);
            }
        }
        
        
        // create the order public notes
        List<String> orderNotes = UtilGenerics.checkList(context.get("orderNotes"));
        if (UtilValidate.isNotEmpty(orderNotes)) {
            for (String orderNote : orderNotes) {
                try {
                    Map<String, Object> noteOutputMap = dispatcher.runSync("createOrderNote", UtilMisc.<String, Object>toMap("orderId", orderId,
                            "internalNote", "N",
                            "note", orderNote,
                            "userLogin", userLogin));
                    if (ServiceUtil.isError(noteOutputMap)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                "OrderOrderNoteCannotBeCreated", UtilMisc.toMap("errorString", ""), locale),
                                null, null, noteOutputMap);
                    }
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Error creating notes while creating order: " + e.toString(), module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                            "OrderOrderNoteCannotBeCreated", UtilMisc.toMap("errorString", e.toString()), locale));
                }
            }
        }
        
        if (errorMessages.size() > 0) {
            return ServiceUtil.returnError(errorMessages);
        }
        
        
        // set the orderId on all adjustments; this list will include order and
        // item adjustments...
        if (UtilValidate.isNotEmpty(orderAdjustments)) {
            for (GenericValue orderAdjustment : orderAdjustments) {
                try {
                    orderAdjustment.set("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment"));
                } catch (IllegalArgumentException e) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCouldNotGetNextSequenceIdForOrderAdjustmentCannotCreateOrder", locale));
                }
                
                orderAdjustment.set("orderId", orderId);
                orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
                orderAdjustment.set("createdByUserLogin", userLogin.getString("userLoginId"));
                
                if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                    orderAdjustment.set("orderItemSeqId", DataModelConstants.SEQ_ID_NA);
                }
                if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                    orderAdjustment.set("shipGroupSeqId", DataModelConstants.SEQ_ID_NA);
                }
                toBeStored.add(orderAdjustment);
            }
        }
        
        // set the order contact mechs
        List<GenericValue> orderContactMechs = UtilGenerics.checkList(context.get("orderContactMechs"));
        if (UtilValidate.isNotEmpty(orderContactMechs)) {
            for (GenericValue ocm : orderContactMechs) {
                ocm.set("orderId", orderId);
                toBeStored.add(ocm);
            }
        }
        
        // set the order item contact mechs
        List<GenericValue> orderItemContactMechs = UtilGenerics.checkList(context.get("orderItemContactMechs"));
        if (UtilValidate.isNotEmpty(orderItemContactMechs)) {
            for (GenericValue oicm : orderItemContactMechs) {
                oicm.set("orderId", orderId);
                toBeStored.add(oicm);
            }
        }
        
        // set the order item ship groups
        List<String> dropShipGroupIds = FastList.newInstance(); // this list will contain the ids of all the ship groups for drop shipments (no reservations)
        String shipmentMethodTypeId = "";
        //上门自提二期做
        if (UtilValidate.isNotEmpty(orderItemShipGroupInfo)) {
            for (GenericValue valueObj : orderItemShipGroupInfo) {
                shipmentMethodTypeId = valueObj.getString("shipmentMethodTypeId");
                valueObj.set("orderId", orderId);
                if ("OrderItemShipGroup".equals(valueObj.getEntityName())) {
                    // ship group
                    if (valueObj.get("carrierRoleTypeId") == null) {
                        valueObj.set("carrierRoleTypeId", "CARRIER");
                    }
                    
                } else if ("OrderAdjustment".equals(valueObj.getEntityName())) {
                    // shipping / tax adjustment(s)
                    if (UtilValidate.isEmpty(valueObj.get("orderItemSeqId"))) {
                        valueObj.set("orderItemSeqId", DataModelConstants.SEQ_ID_NA);
                    }
                    valueObj.set("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment"));
                    valueObj.set("createdDate", UtilDateTime.nowTimestamp());
                    valueObj.set("createdByUserLogin", userLogin.getString("userLoginId"));
                }
                toBeStored.add(valueObj);
            }
        }
        
        
        // set the order payment info
        
        if (UtilValidate.isNotEmpty(orderPaymentInfos)) {
            for (GenericValue valueObj : orderPaymentInfos) {
                paymentMethodTypeId = valueObj.getString("paymentMethodTypeId");
                valueObj.set("orderId", orderId);
                valueObj.set("maxAmount", context.get("grandTotal"));
                //将支付金额改为订单里存储的总价 Add By AlexYao
                if ("OrderPaymentPreference".equals(valueObj.getEntityName())) {
                    if (valueObj.get("orderPaymentPreferenceId") == null) {
                        valueObj.set("orderPaymentPreferenceId", delegator.getNextSeqId("OrderPaymentPreference"));
                        valueObj.set("createdDate", UtilDateTime.nowTimestamp());
                        valueObj.set("createdByUserLogin", userLogin.getString("userLoginId"));
                    }
                    if (valueObj.get("statusId") == null) {
                        valueObj.set("statusId", "PAYMENT_NOT_RECEIVED");
                    }
                    valueObj.set("maxAmount",grandTotal);
                }
                toBeStored.add(valueObj);
            }
        }
        //为用户打标签
        for (int i = 0; i < products.size(); i++) {
            GenericValue product = products.get(i);
            List<GenericValue> tagAssoc = null;
            try {
                tagAssoc = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", product.getString("productId")));
                if (UtilValidate.isNotEmpty(tagAssoc)) {
                    List<GenericValue> tags = EntityUtil.getRelated("Tag", tagAssoc);
                    if (UtilValidate.isNotEmpty(tags)) {
                        for (GenericValue tag : tags) {
                            GenericValue partyTag = delegator.makeValue("PartyTag");
                            partyTag.put("tagId", tag.getString("tagId"));
                            partyTag.put("tagTypeId", tag.getString("tagTypeId"));
                            partyTag.setNextSeqId();
                            partyTag.put("partyId", partyId);
                            toBeStored.add(partyTag);
                        }
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        try {
            // store line items, etc so that they will be there for the foreign key checks
            
            List<String> resErrorMessages = new LinkedList<String>();
            // add a product service to inventory
            
           
           
           /* //EXT_COD 扣减库存  by AlexYao 2016/02/01
            if ("EXT_COD".equals(paymentMethodTypeId)) {
                for (GenericValue item : orderItems) {
                    //货到付款状态直接扣减库存
                    List<GenericValue> inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", item.getString("productId")));
                    if (UtilValidate.isNotEmpty(inventoryItems)) {
                        GenericValue inventoryItem = inventoryItems.get(0);
                        BigDecimal accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("accountingQuantityTotal");
                        BigDecimal quantity = orderItems.get(0).getBigDecimal("quantity");
                        inventoryItem.set("accountingQuantityTotal", accountingQuantityTotal.add(quantity));
                        toBeStored.add(inventoryItem);
                        
                    }
                }
                
            }*/
            
            
            if (UtilValidate.isNotEmpty(toBeStored)) {
                delegator.storeAll(toBeStored);
            }
            
            if (resErrorMessages.size() > 0) {
                return ServiceUtil.returnError(resErrorMessages);
            }
            // END inventory reservation
            
            successResult.put("orderId", orderId);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem with order storage or reservations", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCouldNotCreateOrderWriteError", locale) + e.getMessage() + ").");
        }
        //设置自动取消订单
        
        //订单规则
        GenericValue orderRule = null;
        try {
            orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        int time = 5 * 60 * 1000;
        if (UtilValidate.isNotEmpty(orderRule)) {
            GenericValue productActivity = null;
            try {
                productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (productActivity != null) {
                if ("SEC_KILL".equals(productActivity.get("activityType"))) {
                    if (UtilValidate.isNotEmpty(orderRule.get("seckillCancelStamp")) && UtilValidate.isNotEmpty(orderRule.get("seckillCancelUom"))) {
                        if ("d".equals(orderRule.get("seckillCancelUom"))) {
                            time = orderRule.getLong("seckillCancelStamp").intValue() * 24 * 60 * 60 * 1000;
                        } else if ("h".equals(orderRule.get("seckillCancelUom"))) {
                            time = orderRule.getLong("seckillCancelStamp").intValue() * 60 * 60 * 1000;
                        } else if ("min".equals(orderRule.get("seckillCancelUom"))) {
                            time = orderRule.getLong("seckillCancelStamp").intValue() * 60 * 1000;
                        }
                    }
                } else if ("GROUP_ORDER".equals(productActivity.get("activityType"))) {
                    if (UtilValidate.isNotEmpty(orderRule.get("groupCancelStamp")) && UtilValidate.isNotEmpty(orderRule.get("groupCancelUom"))) {
                        if ("d".equals(orderRule.get("groupCancelUom"))) {
                            time = orderRule.getLong("groupCancelStamp").intValue() * 24 * 60 * 60 * 1000;
                        } else if ("h".equals(orderRule.get("groupCancelUom"))) {
                            time = orderRule.getLong("groupCancelStamp").intValue() * 60 * 60 * 1000;
                        } else if ("min".equals(orderRule.get("groupCancelUom"))) {
                            time = orderRule.getLong("groupCancelStamp").intValue() * 60 * 1000;
                        }
                    }
                }
            } else {
                if (UtilValidate.isNotEmpty(orderRule.get("ordinaryCancelStamp")) && UtilValidate.isNotEmpty(orderRule.get("ordinaryCancelUom"))) {
                    if ("d".equals(orderRule.get("ordinaryCancelUom"))) {
                        time = orderRule.getLong("ordinaryCancelStamp").intValue() * 24 * 60 * 60 * 1000;
                    } else if ("h".equals(orderRule.get("ordinaryCancelUom"))) {
                        time = orderRule.getLong("ordinaryCancelStamp").intValue() * 60 * 60 * 1000;
                    } else if ("min".equals(orderRule.get("ordinaryCancelUom"))) {
                        time = orderRule.getLong("ordinaryCancelStamp").intValue() * 60 * 1000;
                    }
                }
            }
        }
        
        
        Map orderContext = UtilMisc.toMap("orderId", orderId);
        try {
            dispatcher.schedule("autoCancelOrder", "pool", "autoCancelOrder", orderContext,
                    System.currentTimeMillis() + time, RecurrenceRule.HOURLY, 1, 1, System.currentTimeMillis() + time + 5 * 60 * 1000, -1);
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        List<String> resErrorMessages = new LinkedList<String>();
        // START inventory reservation
      /*  try {
            //锁定库存
            reserveInventory(delegator, dispatcher, userLogin, locale, orderItemShipGroupInfo, dropShipGroupIds, itemValuesBySeqId,
                    orderTypeId, productStoreId, resErrorMessages);
        } catch (GeneralException e) {
            return ServiceUtil.returnError(e.getMessage());
        }*/
        
        if (resErrorMessages.size() > 0) {
            return ServiceUtil.returnError(resErrorMessages);
        }
        // END inventory reservation
            return successResult;
    }
    
    private static synchronized boolean dealInventoryItem(Delegator delegator, GenericValue orderItem, GenericValue inventoryItem) {
        BigDecimal quantity = orderItem.getBigDecimal("quantity");
        try {
            inventoryItem = EntityUtil.getFirst(delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", orderItem.getString("productId"))));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(inventoryItem)) {
            BigDecimal accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("accountingQuantityTotal");
            ;
            BigDecimal lockQuantityTotal = inventoryItem.getBigDecimal("lockQuantityTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("lockQuantityTotal");
            if (lockQuantityTotal.add(quantity).compareTo(accountingQuantityTotal) > 0) {
                return true;
            } else {
                inventoryItem.set("lockQuantityTotal", lockQuantityTotal.add(quantity));
                try {
                    inventoryItem.store();
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    public static void reserveInventory(Delegator delegator, LocalDispatcher dispatcher, GenericValue userLogin, Locale locale, List<GenericValue> orderItemShipGroupInfo, List<String> dropShipGroupIds, Map<String, GenericValue> itemValuesBySeqId, String orderTypeId, String productStoreId, List<String> resErrorMessages) throws GeneralException {
        boolean isImmediatelyFulfilled = false;
        GenericValue productStore = null;
        if (UtilValidate.isNotEmpty(productStoreId)) {
            try {
                productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
            } catch (GenericEntityException e) {
                throw new GeneralException(UtilProperties.getMessage(resource_error,
                        "OrderErrorCouldNotFindProductStoreWithID",
                        UtilMisc.toMap("productStoreId", productStoreId), locale) + e.toString());
            }
        }
        // START inventory reservation
        // decrement inventory available for each OrderItemShipGroupAssoc, within the same transaction
        if (UtilValidate.isNotEmpty(orderItemShipGroupInfo)) {
            for (GenericValue orderItemShipGroupAssoc : orderItemShipGroupInfo) {
                if ("OrderItemShipGroupAssoc".equals(orderItemShipGroupAssoc.getEntityName())) {
                    GenericValue orderItem = itemValuesBySeqId.get(orderItemShipGroupAssoc.get("orderItemSeqId"));
                    GenericValue orderItemShipGroup = orderItemShipGroupAssoc.getRelatedOne("OrderItemShipGroup");
                    String shipGroupFacilityId = orderItemShipGroup.getString("facilityId");
                    String itemStatus = orderItem.getString("statusId");
                    if ("ITEM_REJECTED".equals(itemStatus) || "ITEM_CANCELLED".equals(itemStatus) || "ITEM_COMPLETED".equals(itemStatus)) {
                        Debug.logInfo("Order item [" + orderItem.getString("orderId") + " / " + orderItem.getString("orderItemSeqId") + "] is not in a proper status for reservation", module);
                        continue;
                    }
                    if (UtilValidate.isNotEmpty(orderItem.getString("productId"))) {  // ignore for rental
                        try {
                            // get the product of the order item
                            GenericValue product = orderItem.getRelatedOne("Product");
                            if (product == null) {
                                Debug.logError("Error when looking up product in reserveInventory service", module);
                                resErrorMessages.add("Error when looking up product in reserveInventory service");
                                continue;
                            }
                            
                            // reserve the product
                            Map<String, Object> reserveInput = new HashMap<String, Object>();
                            
                            
                                reserveInput.put("productStoreId", orderItem.getRelatedOne("Product").get("productStoreId"));
                            
                            reserveInput.put("productId", orderItem.getString("productId"));
                            reserveInput.put("orderId", orderItem.getString("orderId"));
                            reserveInput.put("orderItemSeqId", orderItem.getString("orderItemSeqId"));
                            reserveInput.put("shipGroupSeqId", orderItemShipGroupAssoc.getString("shipGroupSeqId"));
                            reserveInput.put("facilityId", shipGroupFacilityId);
                            // use the quantity from the orderItemShipGroupAssoc, NOT the orderItem, these are reserved by item-group assoc
                            reserveInput.put("quantity", orderItemShipGroupAssoc.getBigDecimal("quantity"));
                            reserveInput.put("userLogin", userLogin);
                            Map<String, Object> reserveResult = dispatcher.runSync("reserveStoreInventory", reserveInput);
                            if (ServiceUtil.isError(reserveResult)) {
                                String invErrMsg = "产品 ";
                                if (product != null) {
                                    invErrMsg += getProductName(product, orderItem);
                                }
                                invErrMsg += "库存不足.";
                                resErrorMessages.add(invErrMsg);
                            }
                            
                        } catch (GenericServiceException e) {
                            String errMsg = "Fatal error calling reserveStoreInventory service: " + e.toString();
                            Debug.logError(e, errMsg, module);
                            resErrorMessages.add(errMsg);
                        }
                    }
                    
                }
            }
        }
    }
    
    public static String getProductName(GenericValue product, GenericValue orderItem) {
        if (UtilValidate.isNotEmpty(product.getString("productName"))) {
            return product.getString("productName");
        } else {
            return orderItem.getString("itemDescription");
        }
    }
    
    public static String getProductName(GenericValue product, String orderItemName) {
        if (UtilValidate.isNotEmpty(product.getString("productName"))) {
            return product.getString("productName");
        } else {
            return orderItemName;
        }
    }
    
    public static String determineSingleFacilityFromOrder(GenericValue orderHeader) {
        if (orderHeader != null) {
            String productStoreId = orderHeader.getString("productStoreId");
            if (productStoreId != null) {
                return ProductStoreWorker.determineSingleFacilityForStore(orderHeader.getDelegator(), productStoreId);
            }
        }
        return null;
    }
    
    /**
     * Service for resetting the OrderHeader grandTotal
     */
    public static Map<String, Object> resetGrandTotal(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        //appears to not be used: GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            String errMsg = "ERROR: Could not set grantTotal on OrderHeader entity: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        
        if (orderHeader != null) {
            OrderReadHelper orh = new OrderReadHelper(orderHeader);
            BigDecimal currentTotal = orderHeader.getBigDecimal("grandTotal");
            BigDecimal currentSubTotal = orderHeader.getBigDecimal("remainingSubTotal");
            
            // get the new grand total
            BigDecimal updatedTotal = orh.getOrderGrandTotal();
            
            String productStoreId = orderHeader.getString("productStoreId");
            String showPricesWithVatTax = null;
            if (UtilValidate.isNotEmpty(productStoreId)) {
                GenericValue productStore = null;
                try {
                    productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
                } catch (GenericEntityException e) {
                    String errorMessage = UtilProperties.getMessage(resource_error,
                            "OrderErrorCouldNotFindProductStoreWithID",
                            UtilMisc.toMap("productStoreId", productStoreId), (Locale) context.get("locale")) + e.toString();
                    Debug.logError(e, errorMessage, module);
                    return ServiceUtil.returnError(errorMessage + e.getMessage() + ").");
                }
                showPricesWithVatTax = productStore.getString("showPricesWithVatTax");
            }
            BigDecimal remainingSubTotal = ZERO;
            if (UtilValidate.isNotEmpty(productStoreId) && "Y".equalsIgnoreCase(showPricesWithVatTax)) {
                // calculate subTotal as grandTotal + taxes - (returnsTotal + shipping of all items)
                remainingSubTotal = updatedTotal.subtract(orh.getOrderReturnedTotal()).subtract(orh.getShippingTotal());
            } else {
                // calculate subTotal as grandTotal - returnsTotal - (tax + shipping of items not returned)
                remainingSubTotal = updatedTotal.subtract(orh.getOrderReturnedTotal()).subtract(orh.getOrderNonReturnedTaxAndShipping());
            }
            
            if (currentTotal == null || currentSubTotal == null || updatedTotal.compareTo(currentTotal) != 0 ||
                    remainingSubTotal.compareTo(currentSubTotal) != 0) {
                orderHeader.set("grandTotal", updatedTotal);
                orderHeader.set("remainingSubTotal", remainingSubTotal);
                try {
                    orderHeader.store();
                } catch (GenericEntityException e) {
                    String errMsg = "ERROR: Could not set grandTotal on OrderHeader entity: " + e.toString();
                    Debug.logError(e, errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }
            }
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Service for setting the OrderHeader grandTotal for all OrderHeaders with no grandTotal
     */
    public static Map<String, Object> setEmptyGrandTotals(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Boolean forceAll = (Boolean) context.get("forceAll");
        Locale locale = (Locale) context.get("locale");
        if (forceAll == null) {
            forceAll = Boolean.FALSE;
        }
        
        EntityCondition cond = null;
        if (!forceAll.booleanValue()) {
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("grandTotal", EntityOperator.EQUALS, null),
                    EntityCondition.makeCondition("remainingSubTotal", EntityOperator.EQUALS, null));
            cond = EntityCondition.makeCondition(exprs, EntityOperator.OR);
        }
        Set<String> fields = UtilMisc.toSet("orderId");
        
        EntityListIterator eli = null;
        try {
            eli = delegator.find("OrderHeader", cond, null, fields, null, null);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        if (eli != null) {
            // reset each order
            GenericValue orderHeader = null;
            while ((orderHeader = eli.next()) != null) {
                String orderId = orderHeader.getString("orderId");
                Map<String, Object> resetResult = null;
                try {
                    resetResult = dispatcher.runSync("resetGrandTotal", UtilMisc.<String, Object>toMap("orderId", orderId, "userLogin", userLogin));
                } catch (GenericServiceException e) {
                    Debug.logError(e, "ERROR: Cannot reset order totals - " + orderId, module);
                }
                
                if (resetResult != null && ServiceUtil.isError(resetResult)) {
                    Debug.logWarning(UtilProperties.getMessage(resource_error,
                            "OrderErrorCannotResetOrderTotals",
                            UtilMisc.toMap("orderId", orderId, "resetResult", ServiceUtil.getErrorMessage(resetResult)), locale), module);
                }
            }
            
            // close the ELI
            try {
                eli.close();
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        } else {
            Debug.logInfo("No orders found for reset processing", module);
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Service for checking and re-calc the shipping amount
     */
    public static Map<String, Object> recalcOrderShipping(DispatchContext ctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Delegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        
        // check and make sure we have permission to change the order
        Security security = ctx.getSecurity();
        boolean hasPermission = OrderServices.hasPermission(orderId, userLogin, "UPDATE", security, delegator);
        if (!hasPermission) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderYouDoNotHavePermissionToChangeThisOrdersStatus", locale));
        }
        
        // get the order header
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCannotGetOrderHeaderEntity", locale) + e.getMessage());
        }
        
        if (orderHeader == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorNoValidOrderHeaderFoundForOrderId", UtilMisc.toMap("orderId", orderId), locale));
        }
        
        OrderReadHelper orh = new OrderReadHelper(orderHeader);
        List<GenericValue> shipGroups = orh.getOrderItemShipGroups();
        if (shipGroups != null) {
            for (GenericValue shipGroup : shipGroups) {
                String shipGroupSeqId = shipGroup.getString("shipGroupSeqId");
                
                if (shipGroup.get("contactMechId") == null || shipGroup.get("shipmentMethodTypeId") == null) {
                    // not shipped (face-to-face order)
                    continue;
                }
                
                Map<String, Object> shippingEstMap = ShippingEvents.getShipEstimate(dispatcher, delegator, orh, shipGroupSeqId);
                BigDecimal shippingTotal = null;
                if (UtilValidate.isEmpty(orh.getValidOrderItems(shipGroupSeqId))) {
                    shippingTotal = ZERO;
                    Debug.logInfo("No valid order items found - " + shippingTotal, module);
                } else {
                    shippingTotal = UtilValidate.isEmpty(shippingEstMap.get("shippingTotal")) ? ZERO : (BigDecimal) shippingEstMap.get("shippingTotal");
                    shippingTotal = shippingTotal.setScale(orderDecimals, orderRounding);
                    Debug.logInfo("Got new shipping estimate - " + shippingTotal, module);
                }
                if (Debug.infoOn()) {
                    Debug.logInfo("New Shipping Total [" + orderId + " / " + shipGroupSeqId + "] : " + shippingTotal, module);
                }
                
                BigDecimal currentShipping = OrderReadHelper.getAllOrderItemsAdjustmentsTotal(orh.getOrderItemAndShipGroupAssoc(shipGroupSeqId), orh.getAdjustments(), false, false, true);
                currentShipping = currentShipping.add(OrderReadHelper.calcOrderAdjustments(orh.getOrderHeaderAdjustments(shipGroupSeqId), orh.getOrderItemsSubTotal(), false, false, true));
                
                if (Debug.infoOn()) {
                    Debug.logInfo("Old Shipping Total [" + orderId + " / " + shipGroupSeqId + "] : " + currentShipping, module);
                }
                
                List<String> errorMessageList = UtilGenerics.checkList(shippingEstMap.get(ModelService.ERROR_MESSAGE_LIST));
                if (errorMessageList != null) {
                    Debug.logWarning("Problem finding shipping estimates for [" + orderId + "/ " + shipGroupSeqId + "] = " + errorMessageList, module);
                    continue;
                }
                
                if ((shippingTotal != null) && (shippingTotal.compareTo(currentShipping) != 0)) {
                    // place the difference as a new shipping adjustment
                    BigDecimal adjustmentAmount = shippingTotal.subtract(currentShipping);
                    String adjSeqId = delegator.getNextSeqId("OrderAdjustment");
                    GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment", UtilMisc.toMap("orderAdjustmentId", adjSeqId));
                    orderAdjustment.set("orderAdjustmentTypeId", "SHIPPING_CHARGES");
                    orderAdjustment.set("amount", adjustmentAmount);
                    orderAdjustment.set("orderId", orh.getOrderId());
                    orderAdjustment.set("shipGroupSeqId", shipGroupSeqId);
                    orderAdjustment.set("orderItemSeqId", DataModelConstants.SEQ_ID_NA);
                    orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
                    orderAdjustment.set("createdByUserLogin", userLogin.getString("userLoginId"));
                    //orderAdjustment.set("comments", "Shipping Re-Calc Adjustment");
                    try {
                        orderAdjustment.create();
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Problem creating shipping re-calc adjustment : " + orderAdjustment, module);
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                                "OrderErrorCannotCreateAdjustment", locale));
                    }
                }
                
                // TODO: re-balance free shipping adjustment
            }
        }
        
        return ServiceUtil.returnSuccess();
        
    }
    
    /**
     * Service for checking to see if an order is fully completed or canceled
     */
    public static Map<String, Object> checkItemStatus(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Locale locale = (Locale) context.get("locale");
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        
        // check and make sure we have permission to change the order
        Security security = ctx.getSecurity();
        boolean hasPermission = OrderServices.hasPermission(orderId, userLogin, "UPDATE", security, delegator);
        if (!hasPermission) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderYouDoNotHavePermissionToChangeThisOrdersStatus", locale));
        }
        
        // get the order header
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get OrderHeader record", module);
        }
        if (orderHeader == null) {
            Debug.logError("OrderHeader came back as null", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderCannotUpdateNullOrderHeader", UtilMisc.toMap("orderId", orderId), locale));
        }
        
        // get the order items
        List<GenericValue> orderItems = null;
        try {
            orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get OrderItem records", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderProblemGettingOrderItemRecords", locale));
        }
        
        String orderHeaderStatusId = orderHeader.getString("statusId");
        String orderTypeId = orderHeader.getString("orderTypeId");
        
        boolean allCanceled = true;
        boolean allComplete = true;
        boolean allApproved = true;
        if (orderItems != null) {
            for (GenericValue item : orderItems) {
                String statusId = item.getString("statusId");
                //Debug.logInfo("Item Status: " + statusId, module);
                if (!"ITEM_CANCELLED".equals(statusId)) {
                    //Debug.logInfo("Not set to cancel", module);
                    allCanceled = false;
                    if (!"ITEM_COMPLETED".equals(statusId)) {
                        //Debug.logInfo("Not set to complete", module);
                        allComplete = false;
                        if (!"ITEM_APPROVED".equals(statusId)) {
                            //Debug.logInfo("Not set to approve", module);
                            allApproved = false;
                            break;
                        }
                    }
                }
            }
            
            // find the next status to set to (if any)
            String newStatus = null;
            if (allCanceled) {
                if (!"PURCHASE_ORDER".equals(orderTypeId)) {
                    newStatus = "ORDER_CANCELLED";
                }
            } else if (allComplete) {
                newStatus = "ORDER_COMPLETED";
            } else if (allApproved) {
                boolean changeToApprove = true;
                
                // NOTE DEJ20070805 I'm not sure why we would want to auto-approve the header... adding at least this one exeption so that we don't have to add processing, held, etc statuses to the item status list
                // NOTE2 related to the above: appears this was a weird way to set the order header status by setting all order item statuses... changing that to be less weird and more direct
                // this is a bit of a pain: if the current statusId = ProductStore.headerApprovedStatus and we don't have that status in the history then we don't want to change it on approving the items
                if (UtilValidate.isNotEmpty(orderHeader.getString("productStoreId"))) {
                    try {
                        GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", orderHeader.getString("productStoreId")));
                        if (productStore != null) {
                            String headerApprovedStatus = productStore.getString("headerApprovedStatus");
                            if (UtilValidate.isNotEmpty(headerApprovedStatus)) {
                                if (headerApprovedStatus.equals(orderHeaderStatusId)) {
                                    Map<String, Object> orderStatusCheckMap = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", headerApprovedStatus, "orderItemSeqId", null);
                                    List<GenericValue> orderStatusList = delegator.findByAnd("OrderStatus", orderStatusCheckMap);
                                    // should be 1 in the history, but just in case accept 0 too
                                    if (orderStatusList.size() <= 1) {
                                        changeToApprove = false;
                                    }
                                }
                            }
                        }
                    } catch (GenericEntityException e) {
                        String errMsg = "Database error checking if we should change order header status to approved: " + e.toString();
                        Debug.logError(e, errMsg, module);
                        return ServiceUtil.returnError(errMsg);
                    }
                }
                
                if ("ORDER_SENT".equals(orderHeaderStatusId)) {
                    changeToApprove = false;
                }
                if ("ORDER_COMPLETED".equals(orderHeaderStatusId)) {
                    if ("SALES_ORDER".equals(orderTypeId)) {
                        changeToApprove = false;
                    }
                }
                if ("ORDER_CANCELLED".equals(orderHeaderStatusId)) {
                    changeToApprove = false;
                }
                
                if (changeToApprove) {
                    newStatus = "ORDER_APPROVED";
                }
            }
            
            // now set the new order status
            if (newStatus != null && !newStatus.equals(orderHeaderStatusId)) {
                Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", newStatus, "userLogin", userLogin);
                Map<String, Object> newSttsResult = null;
                try {
                    newSttsResult = dispatcher.runSync("changeOrderStatus", serviceContext);
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Problem calling the changeOrderStatus service", module);
                }
                if (ServiceUtil.isError(newSttsResult)) {
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(newSttsResult));
                }
            }
        } else {
            Debug.logWarning(UtilProperties.getMessage(resource_error,
                    "OrderReceivedNullForOrderItemRecordsOrderId", UtilMisc.toMap("orderId", orderId), locale), module);
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Service to cancel an order item quantity
     */
    public static Map<String, Object> cancelOrderItem(DispatchContext ctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        BigDecimal cancelQuantity = (BigDecimal) context.get("cancelQuantity");
        String orderId = (String) context.get("orderId");
        String orderItemSeqId = (String) context.get("orderItemSeqId");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        Map<String, String> itemReasonMap = UtilGenerics.checkMap(context.get("itemReasonMap"));
        Map<String, String> itemCommentMap = UtilGenerics.checkMap(context.get("itemCommentMap"));
        
        // debugging message info
        String itemMsgInfo = orderId + " / " + orderItemSeqId + " / " + shipGroupSeqId;
        
        // check and make sure we have permission to change the order
        Security security = ctx.getSecurity();
        
        boolean hasPermission = OrderServices.hasPermission(orderId, userLogin, "UPDATE", security, delegator);
        if (!hasPermission) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderYouDoNotHavePermissionToChangeThisOrdersStatus", locale));
        }
        
        Map<String, String> fields = UtilMisc.<String, String>toMap("orderId", orderId);
        if (orderItemSeqId != null) {
            fields.put("orderItemSeqId", orderItemSeqId);
        }
        if (shipGroupSeqId != null) {
            fields.put("shipGroupSeqId", shipGroupSeqId);
        }
        
        List<GenericValue> orderItemShipGroupAssocs = null;
        try {
            orderItemShipGroupAssocs = delegator.findByAnd("OrderItemShipGroupAssoc", fields);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCannotGetOrderItemAssocEntity", UtilMisc.toMap("itemMsgInfo", itemMsgInfo), locale));
        }
        
        if (orderItemShipGroupAssocs != null) {
            for (GenericValue orderItemShipGroupAssoc : orderItemShipGroupAssocs) {
                GenericValue orderItem = null;
                try {
                    orderItem = orderItemShipGroupAssoc.getRelatedOne("OrderItem");
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
                
                if (orderItem == null) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCannotCancelItemItemNotFound", UtilMisc.toMap("itemMsgInfo", itemMsgInfo), locale));
                }
                
                BigDecimal aisgaCancelQuantity = orderItemShipGroupAssoc.getBigDecimal("cancelQuantity");
                if (aisgaCancelQuantity == null) {
                    aisgaCancelQuantity = BigDecimal.ZERO;
                }
                BigDecimal availableQuantity = orderItemShipGroupAssoc.getBigDecimal("quantity").subtract(aisgaCancelQuantity);
                
                BigDecimal itemCancelQuantity = orderItem.getBigDecimal("cancelQuantity");
                if (itemCancelQuantity == null) {
                    itemCancelQuantity = BigDecimal.ZERO;
                }
                BigDecimal itemQuantity = orderItem.getBigDecimal("quantity").subtract(itemCancelQuantity);
                if (availableQuantity == null) {
                    availableQuantity = BigDecimal.ZERO;
                }
                if (itemQuantity == null) {
                    itemQuantity = BigDecimal.ZERO;
                }
                
                BigDecimal thisCancelQty = null;
                if (cancelQuantity != null) {
                    thisCancelQty = cancelQuantity;
                } else {
                    thisCancelQty = availableQuantity;
                }
                
                if (availableQuantity.compareTo(thisCancelQty) >= 0) {
                    if (availableQuantity.compareTo(BigDecimal.ZERO) == 0) {
                        continue;  //OrderItemShipGroupAssoc already cancelled
                    }
                    orderItem.set("cancelQuantity", itemCancelQuantity.add(thisCancelQty));
                    orderItemShipGroupAssoc.set("cancelQuantity", aisgaCancelQuantity.add(thisCancelQty));
                    
                    try {
                        List<GenericValue> toStore = UtilMisc.toList(orderItem, orderItemShipGroupAssoc);
                        delegator.storeAll(toStore);
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                                "OrderUnableToSetCancelQuantity", UtilMisc.toMap("itemMsgInfo", itemMsgInfo), locale));
                    }
                    
                    //  create order item change record
                    if (!"Y".equals(orderItem.getString("isPromo"))) {
                        String reasonEnumId = null;
                        String changeComments = null;
                        if (UtilValidate.isNotEmpty(itemReasonMap)) {
                            reasonEnumId = itemReasonMap.get(orderItem.getString("orderItemSeqId"));
                        }
                        if (UtilValidate.isNotEmpty(itemCommentMap)) {
                            changeComments = itemCommentMap.get(orderItem.getString("orderItemSeqId"));
                        }
                        
                        Map<String, Object> serviceCtx = FastMap.newInstance();
                        serviceCtx.put("orderId", orderItem.getString("orderId"));
                        serviceCtx.put("orderItemSeqId", orderItem.getString("orderItemSeqId"));
                        serviceCtx.put("cancelQuantity", thisCancelQty);
                        serviceCtx.put("changeTypeEnumId", "ODR_ITM_CANCEL");
                        serviceCtx.put("reasonEnumId", reasonEnumId);
                        serviceCtx.put("changeComments", changeComments);
                        serviceCtx.put("userLogin", userLogin);
                        Map<String, Object> resp = null;
                        try {
                            resp = dispatcher.runSync("createOrderItemChange", serviceCtx);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }
                        if (ServiceUtil.isError(resp)) {
                            return ServiceUtil.returnError((String) resp.get(ModelService.ERROR_MESSAGE));
                        }
                    }
                    
                    // log an order note
                    try {
                        BigDecimal quantity = thisCancelQty.setScale(1, orderRounding);
                        String cancelledItemToOrder = UtilProperties.getMessage(resource, "OrderCancelledItemToOrder", locale);
                        dispatcher.runSync("createOrderNote", UtilMisc.<String, Object>toMap("orderId", orderId, "note", cancelledItemToOrder +
                                orderItem.getString("productId") + " (" + quantity + ")", "internalNote", "Y", "userLogin", userLogin));
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                    }
                    
                    if (thisCancelQty.compareTo(itemQuantity) >= 0) {
                        // all items are cancelled -- mark the item as cancelled
                        Map<String, Object> statusCtx = UtilMisc.<String, Object>toMap("orderId", orderId, "orderItemSeqId", orderItem.getString("orderItemSeqId"), "statusId", "ITEM_CANCELLED", "userLogin", userLogin);
                        try {
                            dispatcher.runSyncIgnore("changeOrderItemStatus", statusCtx);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                                    "OrderUnableToCancelOrderLine", UtilMisc.toMap("itemMsgInfo", itemMsgInfo), locale));
                        }
                    } else {
                        // reverse the inventory reservation
                        Map<String, Object> invCtx = UtilMisc.<String, Object>toMap("orderId", orderId, "orderItemSeqId", orderItem.getString("orderItemSeqId"), "shipGroupSeqId",
                                shipGroupSeqId, "cancelQuantity", thisCancelQty, "userLogin", userLogin);
                        try {
                            dispatcher.runSyncIgnore("cancelOrderItemInvResQty", invCtx);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                                    "OrderUnableToUpdateInventoryReservations", UtilMisc.toMap("itemMsgInfo", itemMsgInfo), locale));
                        }
                    }
                } else {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderInvalidCancelQuantityCannotCancel", UtilMisc.toMap("thisCancelQty", thisCancelQty), locale));
                }
            }
        } else {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCannotCancelItemItemNotFound", UtilMisc.toMap("itemMsgInfo", itemMsgInfo), locale));
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Service for changing the status on order item(s)
     */
    public static Map<String, Object> setItemStatus(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        String orderItemSeqId = (String) context.get("orderItemSeqId");
        String fromStatusId = (String) context.get("fromStatusId");
        String statusId = (String) context.get("statusId");
        Timestamp statusDateTime = (Timestamp) context.get("statusDateTime");
        Locale locale = (Locale) context.get("locale");
        
        // check and make sure we have permission to change the order
        Security security = ctx.getSecurity();
        /*boolean hasPermission = OrderServices.hasPermission(orderId, userLogin, "UPDATE", security, delegator);
        if (!hasPermission) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderYouDoNotHavePermissionToChangeThisOrdersStatus", locale));
        }*/
        
        List<EntityExpr> exprs = new ArrayList<EntityExpr>();
        exprs.add(EntityCondition.makeCondition("orderId", orderId));
        if (orderItemSeqId != null) {
            exprs.add(EntityCondition.makeCondition("orderItemSeqId", orderItemSeqId));
        }
        if (fromStatusId != null) {
            exprs.add(EntityCondition.makeCondition("statusId", fromStatusId));
        } else {
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, UtilMisc.toList("ITEM_COMPLETED", "ITEM_CANCELLED")));
        }
        
        List<GenericValue> orderItems = null;
        try {
            orderItems = delegator.findList("OrderItem", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCannotGetOrderItemEntity", locale) + e.getMessage());
        }
        
        if (UtilValidate.isNotEmpty(orderItems)) {
            List<GenericValue> toBeStored = new ArrayList<GenericValue>();
            for (GenericValue orderItem : orderItems) {
                if (orderItem == null) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCannotChangeItemStatusItemNotFound", locale));
                }
                if (Debug.verboseOn()) {
                    Debug.logVerbose("[OrderServices.setItemStatus] : Status Change: [" + orderId + "] (" + orderItem.getString("orderItemSeqId"), module);
                }
                if (Debug.verboseOn()) {
                    Debug.logVerbose("[OrderServices.setItemStatus] : From Status : " + orderItem.getString("statusId"), module);
                }
                if (Debug.verboseOn()) {
                    Debug.logVerbose("[OrderServices.setOrderStatus] : To Status : " + statusId, module);
                }
                
                if (orderItem.getString("statusId").equals(statusId)) {
                    continue;
                }
                
                try {
                    Map<String, String> statusFields = UtilMisc.<String, String>toMap("statusId", orderItem.getString("statusId"), "statusIdTo", statusId);
                    GenericValue statusChange = delegator.findByPrimaryKeyCache("StatusValidChange", statusFields);
                    
                    if (statusChange == null) {
                        Debug.logWarning(UtilProperties.getMessage(resource_error,
                                "OrderItemStatusNotChangedIsNotAValidChange", UtilMisc.toMap("orderStatusId", orderItem.getString("statusId"), "statusId", statusId), locale), module);
                        continue;
                    }
                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCouldNotChangeItemStatus", locale) + e.getMessage());
                }
                
                orderItem.set("statusId", statusId);
                toBeStored.add(orderItem);
                if (statusDateTime == null) {
                    statusDateTime = UtilDateTime.nowTimestamp();
                }
                // now create a status change
                Map<String, Object> changeFields = new HashMap<String, Object>();
                changeFields.put("orderStatusId", delegator.getNextSeqId("OrderStatus"));
                changeFields.put("statusId", statusId);
                changeFields.put("orderId", orderId);
                changeFields.put("orderItemSeqId", orderItem.getString("orderItemSeqId"));
                changeFields.put("statusDatetime", statusDateTime);
                changeFields.put("statusUserLogin", userLogin.getString("userLoginId"));
                GenericValue orderStatus = delegator.makeValue("OrderStatus", changeFields);
                toBeStored.add(orderStatus);
            }
            
            // store the changes
            if (toBeStored.size() > 0) {
                try {
                    delegator.storeAll(toBeStored);
                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCannotStoreStatusChanges", locale) + e.getMessage());
                }
            }
            
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    
    /**
     * Service for changing the status on an order header
     */
    public static Map<String, Object> setOrderStatus(DispatchContext ctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Delegator delegator = ctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        String statusId = (String) context.get("statusId");
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        
        // check and make sure we have permission to change the order
        Security security = ctx.getSecurity();
        /*boolean hasPermission = OrderServices.hasPermission(orderId, userLogin, "UPDATE", security, delegator);
        if (!hasPermission) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderYouDoNotHavePermissionToChangeThisOrdersStatus", locale));
        }*/
        
        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            
            if (orderHeader == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderErrorCouldNotChangeOrderStatusOrderCannotBeFound", locale));
            }
            // first save off the old status
            successResult.put("oldStatusId", orderHeader.get("statusId"));
            successResult.put("orderTypeId", orderHeader.get("orderTypeId"));
            
            if (Debug.verboseOn()) {
                Debug.logVerbose("[OrderServices.setOrderStatus] : From Status : " + orderHeader.getString("statusId"), module);
            }
            if (Debug.verboseOn()) {
                Debug.logVerbose("[OrderServices.setOrderStatus] : To Status : " + statusId, module);
            }
            
            if (orderHeader.getString("statusId").equals(statusId)) {
                Debug.logWarning(UtilProperties.getMessage(resource_error,
                        "OrderTriedToSetOrderStatusWithTheSameStatusIdforOrderWithId", UtilMisc.toMap("statusId", statusId, "orderId", orderId), locale), module);
                return successResult;
            }
            try {
                Map<String, String> statusFields = UtilMisc.<String, String>toMap("statusId", orderHeader.getString("statusId"), "statusIdTo", statusId);
                GenericValue statusChange = delegator.findByPrimaryKeyCache("StatusValidChange", statusFields);
                if (statusChange == null) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCouldNotChangeOrderStatusStatusIsNotAValidChange", locale) + ": [" + statusFields.get("statusId") + "] -> [" + statusFields.get("statusIdTo") + "]");
                }
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderErrorCouldNotChangeOrderStatus", locale) + e.getMessage() + ").");
            }
            
            // update the current status
            orderHeader.set("statusId", statusId);
            
            // now create a status change
            GenericValue orderStatus = delegator.makeValue("OrderStatus");
            orderStatus.put("orderStatusId", delegator.getNextSeqId("OrderStatus"));
            orderStatus.put("statusId", statusId);
            orderStatus.put("orderId", orderId);
            orderStatus.put("statusDatetime", UtilDateTime.nowTimestamp());
            orderStatus.put("statusUserLogin", userLogin.getString("userLoginId"));
            
            orderHeader.store();
            orderStatus.create();
            
            successResult.put("needsInventoryIssuance", orderHeader.get("needsInventoryIssuance"));
            successResult.put("grandTotal", orderHeader.get("grandTotal"));
            //Debug.logInfo("For setOrderStatus orderHeader is " + orderHeader, module);
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCouldNotChangeOrderStatus", locale) + e.getMessage() + ").");
        }
        
        // release the inital hold if we are cancelled or approved
        if ("ORDER_CANCELLED".equals(statusId) || "ORDER_APPROVED".equals(statusId)) {
            OrderChangeHelper.releaseInitialOrderHold(ctx.getDispatcher(), orderId);
            
            // cancel any order processing if we are cancelled
            if ("ORDER_CANCELLED".equals(statusId)) {
                OrderChangeHelper.abortOrderProcessing(ctx.getDispatcher(), orderId);
            }
        }
        
        if ("Y".equals(context.get("setItemStatus"))) {
            String newItemStatusId = null;
            if ("ORDER_APPROVED".equals(statusId)) {
                newItemStatusId = "ITEM_APPROVED";
            } else if ("ORDER_COMPLETED".equals(statusId)) {
                newItemStatusId = "ITEM_COMPLETED";
            } else if ("ORDER_CANCELLED".equals(statusId)) {
                newItemStatusId = "ITEM_CANCELLED";
            } else if ("ORDER_WAITEVALUATE".equals(statusId)) {
                newItemStatusId = "ITEM_WAITEVALUATE";
            } else if ("ORDER_WAITPAY".equals(statusId)) {
                newItemStatusId = "ITEM_WAITPAY";
            } else if ("ORDER_WAITRECEIVE".equals(statusId)) {
                newItemStatusId = "ITEM_WAITRECEIVE";
            } else if ("ORDER_WAITSHIP".equals(statusId)) {
                newItemStatusId = "ITEM_WAITSHIP";
            } else if ("ORDER_RETURNED".equals(statusId)) {
                newItemStatusId = "ITEM_RETURNED";
            }
            
            if (newItemStatusId != null) {
                try {
                    Map<String, Object> resp = dispatcher.runSync("changeOrderItemStatus", UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", newItemStatusId, "userLogin", userLogin));
                    if (ServiceUtil.isError(resp)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                                "OrderErrorCouldNotChangeItemStatus", locale) + newItemStatusId, null, null, resp);
                    }
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Error changing item status to " + newItemStatusId + ": " + e.toString(), module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCouldNotChangeItemStatus", locale) + newItemStatusId + ": " + e.toString());
                }
            }
        }
        
        successResult.put("orderStatusId", statusId);
        //Debug.logInfo("For setOrderStatus successResult is " + successResult, module);
        return successResult;
    }
    
    /**
     * Service to update the order tracking number
     */
    public static Map<String, Object> updateTrackingNumber(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = new HashMap<String, Object>();
        Delegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        String trackingNumber = (String) context.get("trackingNumber");
        //Locale locale = (Locale) context.get("locale");
        
        try {
            GenericValue shipGroup = delegator.findByPrimaryKey("OrderItemShipGroup", UtilMisc.toMap("orderId", orderId, "shipGroupSeqId", shipGroupSeqId));
            
            if (shipGroup == null) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: No order shipment preference found!");
            } else {
                shipGroup.set("trackingNumber", trackingNumber);
                shipGroup.store();
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not set tracking number (" + e.getMessage() + ").");
        }
        return result;
    }
    
    /**
     * Service to add a role type to an order
     */
    public static Map<String, Object> addRoleType(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = new HashMap<String, Object>();
        Delegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String partyId = (String) context.get("partyId");
        String roleTypeId = (String) context.get("roleTypeId");
        Boolean removeOld = (Boolean) context.get("removeOld");
        //Locale locale = (Locale) context.get("locale");
        
        if (removeOld != null && removeOld.booleanValue()) {
            try {
                delegator.removeByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", roleTypeId));
            } catch (GenericEntityException e) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not remove old roles (" + e.getMessage() + ").");
                return result;
            }
        }
        
        Map<String, String> fields = UtilMisc.<String, String>toMap("orderId", orderId, "partyId", partyId, "roleTypeId", roleTypeId);
        
        try {
            // first check and see if we are already there; if so, just return success
            GenericValue testValue = delegator.findByPrimaryKey("OrderRole", fields);
            if (testValue != null) {
                ServiceUtil.returnSuccess();
            } else {
                GenericValue value = delegator.makeValue("OrderRole", fields);
                delegator.create(value);
            }
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not add role to order (" + e.getMessage() + ").");
            return result;
        }
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }
    
    /**
     * Service to remove a role type from an order
     */
    public static Map<String, Object> removeRoleType(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = new HashMap<String, Object>();
        Delegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String partyId = (String) context.get("partyId");
        String roleTypeId = (String) context.get("roleTypeId");
        Map<String, String> fields = UtilMisc.<String, String>toMap("orderId", orderId, "partyId", partyId, "roleTypeId", roleTypeId);
        //Locale locale = (Locale) context.get("locale");
        
        GenericValue testValue = null;
        
        try {
            testValue = delegator.findByPrimaryKey("OrderRole", fields);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not add role to order (" + e.getMessage() + ").");
            return result;
        }
        
        if (testValue == null) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
            return result;
        }
        
        try {
            GenericValue value = delegator.findByPrimaryKey("OrderRole", fields);
            
            value.remove();
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not remove role from order (" + e.getMessage() + ").");
            return result;
        }
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }
    
    /**
     * Service to email a customer with initial order confirmation
     */
    public static Map<String, Object> sendOrderConfirmNotification(DispatchContext ctx, Map<String, ? extends Object> context) {
        return sendOrderNotificationScreen(ctx, context, "PRDS_ODR_CONFIRM");
    }
    
    /**
     * Service to email a customer with order changes
     */
    public static Map<String, Object> sendOrderCompleteNotification(DispatchContext ctx, Map<String, ? extends Object> context) {
        return sendOrderNotificationScreen(ctx, context, "PRDS_ODR_COMPLETE");
    }
    
    /**
     * Service to email a customer with order changes
     */
    public static Map<String, Object> sendOrderBackorderNotification(DispatchContext ctx, Map<String, ? extends Object> context) {
        return sendOrderNotificationScreen(ctx, context, "PRDS_ODR_BACKORDER");
    }
    
    /**
     * Service to email a customer with order changes
     */
    public static Map<String, Object> sendOrderChangeNotification(DispatchContext ctx, Map<String, ? extends Object> context) {
        return sendOrderNotificationScreen(ctx, context, "PRDS_ODR_CHANGE");
    }
    
    /**
     * Service to email a customer with order payment retry results
     */
    public static Map<String, Object> sendOrderPayRetryNotification(DispatchContext ctx, Map<String, ? extends Object> context) {
        return sendOrderNotificationScreen(ctx, context, "PRDS_ODR_PAYRETRY");
    }
    
    protected static Map<String, Object> sendOrderNotificationScreen(DispatchContext dctx, Map<String, ? extends Object> context, String emailType) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        String orderItemSeqId = (String) context.get("orderItemSeqId");
        String sendTo = (String) context.get("sendTo");
        String sendCc = (String) context.get("sendCc");
        String note = (String) context.get("note");
        String screenUri = (String) context.get("screenUri");
        GenericValue temporaryAnonymousUserLogin = (GenericValue) context.get("temporaryAnonymousUserLogin");
        Locale localePar = (Locale) context.get("locale");
        if (userLogin == null) {
            // this may happen during anonymous checkout, try to the special case user
            userLogin = temporaryAnonymousUserLogin;
        }
        
        // prepare the order information
        Map<String, Object> sendMap = FastMap.newInstance();
        
        // get the order header and store
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting OrderHeader", module);
        }
        
        if (orderHeader == null) {
            return ServiceUtil.returnFailure(UtilProperties.getMessage(resource,
                    "OrderOrderNotFound", UtilMisc.toMap("orderId", orderId), localePar));
        }
        
        if (orderHeader.get("webSiteId") == null) {
            return ServiceUtil.returnFailure(UtilProperties.getMessage(resource,
                    "OrderOrderWithoutWebSite", UtilMisc.toMap("orderId", orderId), localePar));
        }
        
        GenericValue productStoreEmail = null;
        try {
            productStoreEmail = delegator.findByPrimaryKey("ProductStoreEmailSetting", UtilMisc.toMap("productStoreId", orderHeader.get("productStoreId"), "emailType", emailType));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting the ProductStoreEmailSetting for productStoreId=" + orderHeader.get("productStoreId") + " and emailType=" + emailType, module);
        }
        if (productStoreEmail == null) {
            return ServiceUtil.returnFailure(UtilProperties.getMessage(resourceProduct,
                    "ProductProductStoreEmailSettingsNotValid",
                    UtilMisc.toMap("productStoreId", orderHeader.get("productStoreId"),
                            "emailType", emailType), localePar));
        }
        
        // the override screenUri
        if (UtilValidate.isEmpty(screenUri)) {
            String bodyScreenLocation = productStoreEmail.getString("bodyScreenLocation");
            if (UtilValidate.isEmpty(bodyScreenLocation)) {
                bodyScreenLocation = ProductStoreWorker.getDefaultProductStoreEmailScreenLocation(emailType);
            }
            sendMap.put("bodyScreenUri", bodyScreenLocation);
            String xslfoAttachScreenLocation = productStoreEmail.getString("xslfoAttachScreenLocation");
            sendMap.put("xslfoAttachScreenLocation", xslfoAttachScreenLocation);
        } else {
            sendMap.put("bodyScreenUri", screenUri);
        }
        
        // website
        sendMap.put("webSiteId", orderHeader.get("webSiteId"));
        
        OrderReadHelper orh = new OrderReadHelper(orderHeader);
        String emailString = orh.getOrderEmailString();
        if (UtilValidate.isEmpty(emailString)) {
            Debug.logInfo("Customer is not setup to receive emails; no address(s) found [" + orderId + "]", module);
            return ServiceUtil.returnFailure(UtilProperties.getMessage(resource,
                    "OrderOrderWithoutEmailAddress", UtilMisc.toMap("orderId", orderId), localePar));
        }
        
        // where to get the locale... from PLACING_CUSTOMER's UserLogin.lastLocale,
        // or if not available then from ProductStore.defaultLocaleString
        // or if not available then the system Locale
        Locale locale = null;
        GenericValue placingParty = orh.getPlacingParty();
        GenericValue placingUserLogin = placingParty == null ? null : PartyWorker.findPartyLatestUserLogin(placingParty.getString("partyId"), delegator);
        if (locale == null && placingParty != null) {
            locale = PartyWorker.findPartyLastLocale(placingParty.getString("partyId"), delegator);
        }
        
        // for anonymous orders, use the temporaryAnonymousUserLogin as the placingUserLogin will be null
        if (placingUserLogin == null) {
            placingUserLogin = temporaryAnonymousUserLogin;
        }
        
        GenericValue productStore = OrderReadHelper.getProductStoreFromOrder(orderHeader);
        if (locale == null && productStore != null) {
            String localeString = productStore.getString("defaultLocaleString");
            if (UtilValidate.isNotEmpty(localeString)) {
                locale = UtilMisc.parseLocale(localeString);
            }
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }
        
        Map<String, Object> bodyParameters = UtilMisc.<String, Object>toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "userLogin", placingUserLogin, "locale", locale);
        if (placingParty != null) {
            bodyParameters.put("partyId", placingParty.get("partyId"));
        }
        bodyParameters.put("note", note);
        sendMap.put("bodyParameters", bodyParameters);
        sendMap.put("userLogin", userLogin);
        
        String subjectString = productStoreEmail.getString("subject");
        sendMap.put("subject", subjectString);
        
        sendMap.put("contentType", productStoreEmail.get("contentType"));
        sendMap.put("sendFrom", productStoreEmail.get("fromAddress"));
        sendMap.put("sendCc", productStoreEmail.get("ccAddress"));
        sendMap.put("sendBcc", productStoreEmail.get("bccAddress"));
        if ((sendTo != null) && UtilValidate.isEmail(sendTo)) {
            sendMap.put("sendTo", sendTo);
        } else {
            sendMap.put("sendTo", emailString);
        }
        if ((sendCc != null) && UtilValidate.isEmail(sendCc)) {
            sendMap.put("sendCc", sendCc);
        } else {
            sendMap.put("sendCc", productStoreEmail.get("ccAddress"));
        }
        
        // send the notification
        Map<String, Object> sendResp = null;
        try {
            sendResp = dispatcher.runSync("sendMailFromScreen", sendMap);
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderServiceExceptionSeeLogs", locale));
        }
        
        // check for errors
        if (sendResp != null && !ServiceUtil.isError(sendResp)) {
            sendResp.put("emailType", emailType);
        }
        if (UtilValidate.isNotEmpty(orderId)) {
            sendResp.put("orderId", orderId);
        }
        return sendResp;
    }
    
    /**
     * Service to email order notifications for pending actions
     */
    public static Map<String, Object> sendProcessNotification(DispatchContext ctx, Map<String, ? extends Object> context) {
        //appears to not be used: Map result = new HashMap();
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        String adminEmailList = (String) context.get("adminEmailList");
        String assignedToUser = (String) context.get("assignedPartyId");
        //appears to not be used: String assignedToRole = (String) context.get("assignedRoleTypeId");
        String workEffortId = (String) context.get("workEffortId");
        Locale locale = (Locale) context.get("locale");
        
        GenericValue workEffort = null;
        GenericValue orderHeader = null;
        //appears to not be used: String assignedEmail = null;
        
        // get the order/workflow info
        try {
            workEffort = delegator.findByPrimaryKey("WorkEffort", UtilMisc.toMap("workEffortId", workEffortId));
            String sourceReferenceId = workEffort.getString("sourceReferenceId");
            if (sourceReferenceId != null) {
                orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", sourceReferenceId));
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderProblemWithEntityLookup", locale));
        }
        
        // find the assigned user's email address(s)
        GenericValue party = null;
        Collection<GenericValue> assignedToEmails = null;
        try {
            party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", assignedToUser));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderProblemWithEntityLookup", locale));
        }
        if (party != null) {
            assignedToEmails = ContactHelper.getContactMechByPurpose(party, "PRIMARY_EMAIL", false);
        }
        
        Map<String, Object> templateData = new HashMap<String, Object>(context);
        templateData.putAll(orderHeader);
        templateData.putAll(workEffort);

        /* NOTE DEJ20080609 commenting out this code because the old OFBiz Workflow Engine is being deprecated and this was only for that
        String omgStatusId = WfUtil.getOMGStatus(workEffort.getString("currentStatusId"));
        templateData.put("omgStatusId", omgStatusId);
        */
        templateData.put("omgStatusId", workEffort.getString("currentStatusId"));
        
        // get the assignments
        List<GenericValue> assignments = null;
        if (workEffort != null) {
            try {
                assignments = workEffort.getRelated("WorkEffortPartyAssignment");
            } catch (GenericEntityException e1) {
                Debug.logError(e1, "Problems getting assignements", module);
            }
        }
        templateData.put("assignments", assignments);
        
        StringBuilder emailList = new StringBuilder();
        if (assignedToEmails != null) {
            for (GenericValue ct : assignedToEmails) {
                if (ct != null && ct.get("infoString") != null) {
                    if (emailList.length() > 1) {
                        emailList.append(",");
                    }
                    emailList.append(ct.getString("infoString"));
                }
            }
        }
        if (adminEmailList != null) {
            if (emailList.length() > 1) {
                emailList.append(",");
            }
            emailList.append(adminEmailList);
        }
        
        // prepare the mail info
        String ofbizHome = System.getProperty("ofbiz.home");
        String templateName = ofbizHome + "/applications/order/email/default/emailprocessnotify.ftl";
        
        Map<String, Object> sendMailContext = new HashMap<String, Object>();
        sendMailContext.put("sendTo", emailList.toString());
        sendMailContext.put("sendFrom", "workflow@ofbiz.org"); // fixme
        sendMailContext.put("subject", "Workflow Notification");
        sendMailContext.put("templateName", templateName);
        sendMailContext.put("templateData", templateData);
        
        try {
            dispatcher.runAsync("sendGenericNotificationEmail", sendMailContext);
        } catch (GenericServiceException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderSendMailServiceFailed", locale) + e.getMessage());
        }
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Service to create an order payment preference
     */
    public static Map<String, Object> createPaymentPreference(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = new HashMap<String, Object>();
        Delegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String statusId = (String) context.get("statusId");
        String paymentMethodTypeId = (String) context.get("paymentMethodTypeId");
        String paymentMethodId = (String) context.get("paymentMethodId");
        BigDecimal maxAmount = (BigDecimal) context.get("maxAmount");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        
        String prefId = null;
        
        try {
            prefId = delegator.getNextSeqId("OrderPaymentPreference");
        } catch (IllegalArgumentException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCouldNotCreateOrderPaymentPreferenceIdGenerationFailure", locale));
        }
        
        Map<String, Object> fields = UtilMisc.<String, Object>toMap("orderPaymentPreferenceId", prefId, "orderId", orderId, "paymentMethodTypeId",
                paymentMethodTypeId, "paymentMethodId", paymentMethodId, "maxAmount", maxAmount);
        
        if (statusId != null) {
            fields.put("statusId", statusId);
        }
        
        try {
            GenericValue v = delegator.makeValue("OrderPaymentPreference", fields);
            v.set("createdDate", UtilDateTime.nowTimestamp());
            if (userLogin != null) {
                v.set("createdByUserLogin", userLogin.getString("userLoginId"));
            }
            delegator.create(v);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage(resource,
                    "OrderOrderPaymentPreferencesCannotBeCreated", UtilMisc.toMap("errorString", e.getMessage()), locale));
            return ServiceUtil.returnFailure();
        }
        result.put("orderPaymentPreferenceId", prefId);
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }
    
    /**
     * Service to get order header information as standard results.
     */
    public static Map<String, Object> getOrderHeaderInformation(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        Locale locale = (Locale) context.get("locale");
        
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting order header detial", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderCannotGetOrderHeader", locale) + e.getMessage());
        }
        if (orderHeader != null) {
            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.putAll(orderHeader);
            return result;
        }
        return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                "OrderErrorGettingOrderHeaderInformationNull", locale));
    }
    
    /**
     * Service to get the total shipping for an order.
     */
    public static Map<String, Object> getOrderShippingAmount(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        Locale locale = (Locale) context.get("locale");
        
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCouldNotGetOrderInformation", locale) + e.getMessage() + ").");
        }
        
        Map<String, Object> result = null;
        if (orderHeader != null) {
            OrderReadHelper orh = new OrderReadHelper(orderHeader);
            List<GenericValue> orderItems = orh.getValidOrderItems();
            List<GenericValue> orderAdjustments = orh.getAdjustments();
            List<GenericValue> orderHeaderAdjustments = orh.getOrderHeaderAdjustments();
            BigDecimal orderSubTotal = orh.getOrderItemsSubTotal();
            
            BigDecimal shippingAmount = OrderReadHelper.getAllOrderItemsAdjustmentsTotal(orderItems, orderAdjustments, false, false, true);
            shippingAmount = shippingAmount.add(OrderReadHelper.calcOrderAdjustments(orderHeaderAdjustments, orderSubTotal, false, false, true));
            
            result = ServiceUtil.returnSuccess();
            result.put("shippingAmount", shippingAmount);
        } else {
            result = ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderUnableToFindOrderHeaderCannotGetShippingAmount", locale));
        }
        return result;
    }
    
    /**
     * Service to get an order contact mech.
     */
    public static Map<String, Object> getOrderAddress(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = new HashMap<String, Object>();
        Delegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        Locale locale = (Locale) context.get("locale");
        //appears to not be used: GenericValue v = null;
        String[] purpose = {"BILLING_LOCATION", "SHIPPING_LOCATION"};
        String[] outKey = {"billingAddress", "shippingAddress"};
        GenericValue orderHeader = null;
        //Locale locale = (Locale) context.get("locale");
        
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (orderHeader != null) {
                result.put("orderHeader", orderHeader);
            }
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage(resource,
                    "OrderOrderNotFound", UtilMisc.toMap("orderId", orderId), locale));
            return result;
        }
        if (orderHeader == null) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage(resource,
                    "OrderOrderNotFound", UtilMisc.toMap("orderId", orderId), locale));
            return result;
        }
        for (int i = 0; i < purpose.length; i++) {
            try {
                GenericValue orderContactMech = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderContactMech",
                        UtilMisc.toMap("contactMechPurposeTypeId", purpose[i])));
                GenericValue contactMech = orderContactMech.getRelatedOne("ContactMech");
                
                if (contactMech != null) {
                    result.put(outKey[i], contactMech.getRelatedOne("PostalAddress"));
                }
            } catch (GenericEntityException e) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage(resource,
                        "OrderOrderContachMechNotFound", UtilMisc.toMap("errorString", e.getMessage()), locale));
                return result;
            }
        }
        
        result.put("orderId", orderId);
        return result;
    }
    
    /**
     * Service to create a order header note.
     */
    public static Map<String, Object> createOrderNote(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String noteString = (String) context.get("note");
        String noteName = (String) context.get("noteName");
        String orderId = (String) context.get("orderId");
        String internalNote = (String) context.get("internalNote");
        Map<String, Object> noteCtx = UtilMisc.<String, Object>toMap("note", noteString, "userLogin", userLogin, "noteName", noteName);
        Locale locale = (Locale) context.get("locale");
        
        try {
            // Store the note.
            Map<String, Object> noteRes = dispatcher.runSync("createNote", noteCtx);
            
            if (ServiceUtil.isError(noteRes)) {
                return noteRes;
            }
            
            String noteId = (String) noteRes.get("noteId");
            
            if (UtilValidate.isEmpty(noteId)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderProblemCreatingTheNoteNoNoteIdReturned", locale));
            }
            
            // Set the order info
            Map<String, String> fields = UtilMisc.<String, String>toMap("orderId", orderId, "noteId", noteId, "internalNote", internalNote);
            GenericValue v = delegator.makeValue("OrderHeaderNote", fields);
            
            delegator.create(v);
        } catch (GenericEntityException ee) {
            Debug.logError(ee, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "OrderOrderNoteCannotBeCreated", UtilMisc.toMap("errorString", ee.getMessage()), locale));
        } catch (GenericServiceException se) {
            Debug.logError(se, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "OrderOrderNoteCannotBeCreated", UtilMisc.toMap("errorString", se.getMessage()), locale));
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> allowOrderSplit(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        Locale locale = (Locale) context.get("locale");
        
        // check and make sure we have permission to change the order
        Security security = ctx.getSecurity();
        if (!security.hasEntityPermission("ORDERMGR", "_UPDATE", userLogin)) {
            GenericValue placingCustomer = null;
            try {
                Map<String, Object> placingCustomerFields = UtilMisc.<String, Object>toMap("orderId", orderId, "partyId", userLogin.getString("partyId"), "roleTypeId", "PLACING_CUSTOMER");
                placingCustomer = delegator.findByPrimaryKey("OrderRole", placingCustomerFields);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderErrorCannotGetOrderRoleEntity", locale) + e.getMessage());
            }
            if (placingCustomer == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderYouDoNotHavePermissionToChangeThisOrdersStatus", locale));
            }
        }
        
        GenericValue shipGroup = null;
        try {
            Map<String, String> fields = UtilMisc.<String, String>toMap("orderId", orderId, "shipGroupSeqId", shipGroupSeqId);
            shipGroup = delegator.findByPrimaryKey("OrderItemShipGroup", fields);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problems getting OrderItemShipGroup for : " + orderId + " / " + shipGroupSeqId, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderCannotUpdateProblemGettingOrderShipmentPreference", locale));
        }
        
        if (shipGroup != null) {
            shipGroup.set("maySplit", "Y");
            try {
                shipGroup.store();
            } catch (GenericEntityException e) {
                Debug.logError("Problem saving OrderItemShipGroup for : " + orderId + " / " + shipGroupSeqId, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderCannotUpdateProblemSettingOrderShipmentPreference", locale));
            }
        } else {
            Debug.logError("ERROR: Got a NULL OrderItemShipGroup", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderCannotUpdateNoAvailableGroupsToChange", locale));
        }
        return ServiceUtil.returnSuccess();
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object> cancelFlaggedSalesOrders(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //Locale locale = (Locale) context.get("locale");
        
        List<GenericValue> ordersToCheck = null;
        
        // create the query expressions
        List<EntityExpr> exprs = UtilMisc.toList(
                EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_COMPLETED"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_REJECTED")
        );
        EntityConditionList<EntityExpr> ecl = EntityCondition.makeCondition(exprs, EntityOperator.AND);
        
        // get the orders
        try {
            ordersToCheck = delegator.findList("OrderHeader", ecl, null, UtilMisc.toList("orderDate"), null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting order headers", module);
        }
        
        if (UtilValidate.isEmpty(ordersToCheck)) {
            Debug.logInfo("No orders to check, finished", module);
            return ServiceUtil.returnSuccess();
        }
        
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        for (GenericValue orderHeader : ordersToCheck) {
            String orderId = orderHeader.getString("orderId");
            String orderStatus = orderHeader.getString("statusId");
            
            if ("ORDER_CREATED".equals(orderStatus)) {
                // first check for un-paid orders
                Timestamp orderDate = orderHeader.getTimestamp("entryDate");
                
                // need the store for the order
                GenericValue productStore = null;
                try {
                    productStore = orderHeader.getRelatedOne("ProductStore");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
                }
                
                // default days to cancel
                int daysTillCancel = 30;
                
                // get the value from the store
                if (productStore != null && productStore.get("daysToCancelNonPay") != null) {
                    daysTillCancel = productStore.getLong("daysToCancelNonPay").intValue();
                }
                
                if (daysTillCancel > 0) {
                    // 0 days means do not auto-cancel
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(orderDate.getTime());
                    cal.add(Calendar.DAY_OF_YEAR, daysTillCancel);
                    Date cancelDate = cal.getTime();
                    Date nowDate = new Date();
                    //Debug.logInfo("Cancel Date : " + cancelDate, module);
                    //Debug.logInfo("Current Date : " + nowDate, module);
                    if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
                        // cancel the order item(s)
                        Map<String, Object> svcCtx = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ITEM_CANCELLED", "userLogin", userLogin);
                        try {
                            // TODO: looks like result is ignored here, but we should be looking for errors
                            dispatcher.runSync("changeOrderItemStatus", svcCtx);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, "Problem calling change item status service : " + svcCtx, module);
                        }
                    }
                }
            } else {
                // check for auto-cancel items
                List itemsExprs = new ArrayList();
                
                // create the query expressions
                itemsExprs.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
                itemsExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_CREATED"),
                        EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_APPROVED")), EntityOperator.OR));
                itemsExprs.add(EntityCondition.makeCondition("dontCancelSetUserLogin", EntityOperator.EQUALS, GenericEntity.NULL_FIELD));
                itemsExprs.add(EntityCondition.makeCondition("dontCancelSetDate", EntityOperator.EQUALS, GenericEntity.NULL_FIELD));
                itemsExprs.add(EntityCondition.makeCondition("autoCancelDate", EntityOperator.NOT_EQUAL, GenericEntity.NULL_FIELD));
                
                ecl = EntityCondition.makeCondition(itemsExprs);
                
                List<GenericValue> orderItems = null;
                try {
                    orderItems = delegator.findList("OrderItem", ecl, null, null, null, false);
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Problem getting order item records", module);
                }
                if (UtilValidate.isNotEmpty(orderItems)) {
                    for (GenericValue orderItem : orderItems) {
                        String orderItemSeqId = orderItem.getString("orderItemSeqId");
                        Timestamp autoCancelDate = orderItem.getTimestamp("autoCancelDate");
                        
                        if (autoCancelDate != null) {
                            if (nowTimestamp.equals(autoCancelDate) || nowTimestamp.after(autoCancelDate)) {
                                // cancel the order item
                                Map<String, Object> svcCtx = UtilMisc.<String, Object>toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId, "statusId", "ITEM_CANCELLED", "userLogin", userLogin);
                                try {
                                    // TODO: check service result for an error return
                                    dispatcher.runSync("changeOrderItemStatus", svcCtx);
                                } catch (GenericServiceException e) {
                                    Debug.logError(e, "Problem calling change item status service : " + svcCtx, module);
                                }
                            }
                        }
                    }
                }
            }
        }
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> checkDigitalItemFulfillment(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        Locale locale = (Locale) context.get("locale");
        
        // need the order header
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "ERROR: Unable to get OrderHeader for orderId : " + orderId, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorUnableToGetOrderHeaderForOrderId", UtilMisc.toMap("orderId", orderId), locale));
        }
        
        // get all the items for the order
        List<GenericValue> orderItems = null;
        if (orderHeader != null) {
            try {
                orderItems = orderHeader.getRelated("OrderItem");
            } catch (GenericEntityException e) {
                Debug.logError(e, "ERROR: Unable to get OrderItem list for orderId : " + orderId, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderErrorUnableToGetOrderItemListForOrderId", UtilMisc.toMap("orderId", orderId), locale));
            }
        }
        
        // find any digital or non-product items
        List<GenericValue> nonProductItems = new ArrayList<GenericValue>();
        List<GenericValue> digitalItems = new ArrayList<GenericValue>();
        Map<GenericValue, GenericValue> digitalProducts = new HashMap<GenericValue, GenericValue>();
        
        if (UtilValidate.isNotEmpty(orderItems)) {
            for (GenericValue item : orderItems) {
                GenericValue product = null;
                try {
                    product = item.getRelatedOne("Product");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "ERROR: Unable to get Product from OrderItem", module);
                }
                if (product != null) {
                    GenericValue productType = null;
                    try {
                        productType = product.getRelatedOne("ProductType");
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "ERROR: Unable to get ProductType from Product", module);
                    }
                    
                    if (productType != null) {
                        String isPhysical = productType.getString("isPhysical");
                        String isDigital = productType.getString("isDigital");
                        
                        // check for digital and finished/digital goods
                        if (isDigital != null && "Y".equalsIgnoreCase(isDigital)) {
                            // we only invoice APPROVED items
                            if ("ITEM_APPROVED".equals(item.getString("statusId"))) {
                                digitalItems.add(item);
                            }
                            if (isPhysical == null || !"Y".equalsIgnoreCase(isPhysical)) {
                                // 100% digital goods need status change
                                digitalProducts.put(item, product);
                            }
                        }
                    }
                } else {
                    String itemType = item.getString("orderItemTypeId");
                    if (!"PRODUCT_ORDER_ITEM".equals(itemType)) {
                        nonProductItems.add(item);
                    }
                }
            }
        }
        
        // now process the digital items
        if (digitalItems.size() > 0 || nonProductItems.size() > 0) {
            GenericValue productStore = OrderReadHelper.getProductStoreFromOrder(dispatcher.getDelegator(), orderId);
            boolean invoiceItems = true;
            if (productStore != null && productStore.get("autoInvoiceDigitalItems") != null) {
                invoiceItems = "Y".equalsIgnoreCase(productStore.getString("autoInvoiceDigitalItems"));
            }
            
            // single list with all invoice items
            List<GenericValue> itemsToInvoice = FastList.newInstance();
            itemsToInvoice.addAll(nonProductItems);
            itemsToInvoice.addAll(digitalItems);
            
            if (invoiceItems) {
                // invoice all APPROVED digital/non-product goods
                
                // do something tricky here: run as a different user that can actually create an invoice, post transaction, etc
                Map<String, Object> invoiceResult = null;
                try {
                    GenericValue permUserLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "system"));
                    Map<String, Object> invoiceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "billItems", itemsToInvoice, "userLogin", permUserLogin);
                    invoiceResult = dispatcher.runSync("createInvoiceForOrder", invoiceContext);
                } catch (GenericEntityException e) {
                    Debug.logError(e, "ERROR: Unable to invoice digital items", module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderProblemWithInvoiceCreationDigitalItemsNotFulfilled", locale));
                } catch (GenericServiceException e) {
                    Debug.logError(e, "ERROR: Unable to invoice digital items", module);
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderProblemWithInvoiceCreationDigitalItemsNotFulfilled", locale));
                }
                if (ModelService.RESPOND_ERROR.equals(invoiceResult.get(ModelService.RESPONSE_MESSAGE))) {
                    return ServiceUtil.returnError((String) invoiceResult.get(ModelService.ERROR_MESSAGE));
                }
                
                // update the status of digital goods to COMPLETED; leave physical/digital as APPROVED for pick/ship
                for (GenericValue item : itemsToInvoice) {
                    GenericValue productType = null;
                    GenericValue product = digitalProducts.get(item);
                    boolean markComplete = false;
                    
                    if (product != null) {
                        try {
                            productType = product.getRelatedOne("ProductType");
                        } catch (GenericEntityException e) {
                            Debug.logError(e, "ERROR: Unable to get ProductType from Product", module);
                        }
                    } else {
                        String itemType = item.getString("orderItemTypeId");
                        if (!"PRODUCT_ORDER_ITEM".equals(itemType)) {
                            markComplete = true;
                        }
                    }
                    
                    if (product != null && productType != null) {
                        String isPhysical = productType.getString("isPhysical");
                        String isDigital = productType.getString("isDigital");
                        
                        // we were set as a digital good; one more check and change status
                        if ((isDigital != null && "Y".equalsIgnoreCase(isDigital)) &&
                                (isPhysical == null || !"Y".equalsIgnoreCase(isPhysical))) {
                            markComplete = true;
                        }
                    }
                    
                    if (markComplete) {
                        Map<String, Object> statusCtx = new HashMap<String, Object>();
                        statusCtx.put("orderId", item.getString("orderId"));
                        statusCtx.put("orderItemSeqId", item.getString("orderItemSeqId"));
                        statusCtx.put("statusId", "ITEM_COMPLETED");
                        statusCtx.put("userLogin", userLogin);
                        try {
                            dispatcher.runSyncIgnore("changeOrderItemStatus", statusCtx);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, "ERROR: Problem setting the status to COMPLETED : " + item, module);
                        }
                    }
                }
            }
            
            // fulfill the digital goods
            Map<String, Object> fulfillContext = UtilMisc.<String, Object>toMap("orderId", orderId, "orderItems", digitalItems, "userLogin", userLogin);
            Map<String, Object> fulfillResult = null;
            try {
                // will be running in an isolated transaction to prevent rollbacks
                fulfillResult = dispatcher.runSync("fulfillDigitalItems", fulfillContext, 300, true);
            } catch (GenericServiceException e) {
                Debug.logError(e, "ERROR: Unable to fulfill digital items", module);
            }
            if (ModelService.RESPOND_ERROR.equals(fulfillResult.get(ModelService.RESPONSE_MESSAGE))) {
                // this service cannot return error at this point or we will roll back the invoice
                // since payments are already captured; errors should have been logged already.
                // the response message here will be passed as an error to the user.
                return ServiceUtil.returnSuccess((String) fulfillResult.get(ModelService.ERROR_MESSAGE));
            }
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> fulfillDigitalItems(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        //appears to not be used: String orderId = (String) context.get("orderId");
        List<GenericValue> orderItems = UtilGenerics.checkList(context.get("orderItems"));
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        
        if (UtilValidate.isNotEmpty(orderItems)) {
            // loop through the digital items to fulfill
            for (GenericValue orderItem : orderItems) {
                // make sure we have a valid item
                if (orderItem == null) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCannotCheckForFulfillmentItemNotFound", locale));
                }
                
                // locate the Product & ProductContent records
                GenericValue product = null;
                List<GenericValue> productContent = null;
                try {
                    product = orderItem.getRelatedOne("Product");
                    if (product == null) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                                "OrderErrorCannotCheckForFulfillmentProductNotFound", locale));
                    }
                    
                    List<GenericValue> allProductContent = product.getRelated("ProductContent");
                    
                    // try looking up the parent product if the product has no content and is a variant
                    if (UtilValidate.isEmpty(allProductContent) && ("Y".equals(product.getString("isVariant")))) {
                        GenericValue parentProduct = ProductWorker.getParentProduct(product.getString("productId"), delegator);
                        if (allProductContent == null) {
                            allProductContent = FastList.newInstance();
                        }
                        if (parentProduct != null) {
                            allProductContent.addAll(parentProduct.getRelated("ProductContent"));
                        }
                    }
                    
                    if (UtilValidate.isNotEmpty(allProductContent)) {
                        // only keep ones with valid dates
                        productContent = EntityUtil.filterByDate(allProductContent, UtilDateTime.nowTimestamp(), "fromDate", "thruDate", true);
                        Debug.logInfo("Product has " + allProductContent.size() + " associations, " +
                                (productContent == null ? "0" : "" + productContent.size()) + " has valid from/thru dates", module);
                    }
                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                            "OrderErrorCannotGetProductEntity", locale) + e.getMessage());
                }
                
                // now use the ProductContent to fulfill the item
                if (UtilValidate.isNotEmpty(productContent)) {
                    for (GenericValue productContentItem : productContent) {
                        GenericValue content = null;
                        try {
                            content = productContentItem.getRelatedOne("Content");
                        } catch (GenericEntityException e) {
                            Debug.logError(e, "ERROR: Cannot get Content entity: " + e.getMessage(), module);
                            continue;
                        }
                        
                        String fulfillmentType = productContentItem.getString("productContentTypeId");
                        if ("FULFILLMENT_EXTASYNC".equals(fulfillmentType) || "FULFILLMENT_EXTSYNC".equals(fulfillmentType)) {
                            // enternal service fulfillment
                            String fulfillmentService = (String) content.get("serviceName");
                            if (fulfillmentService == null) {
                                Debug.logError("ProductContent of type FULFILLMENT_EXTERNAL had Content with empty serviceName, can not run fulfillment", module);
                            }
                            Map<String, Object> serviceCtx = UtilMisc.<String, Object>toMap("userLogin", userLogin, "orderItem", orderItem);
                            serviceCtx.putAll(productContentItem.getPrimaryKey());
                            try {
                                Debug.logInfo("Running external fulfillment '" + fulfillmentService + "'", module);
                                if ("FULFILLMENT_EXTASYNC".equals(fulfillmentType)) {
                                    dispatcher.runAsync(fulfillmentService, serviceCtx, true);
                                } else if ("FULFILLMENT_EXTSYNC".equals(fulfillmentType)) {
                                    Map<String, Object> resp = dispatcher.runSync(fulfillmentService, serviceCtx);
                                    if (ServiceUtil.isError(resp)) {
                                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                                "OrderOrderExternalFulfillmentError", locale), null, null, resp);
                                    }
                                }
                            } catch (GenericServiceException e) {
                                Debug.logError(e, "ERROR: Could not run external fulfillment service '" + fulfillmentService + "'; " + e.getMessage(), module);
                            }
                        } else if ("FULFILLMENT_EMAIL".equals(fulfillmentType)) {
                            // digital email fulfillment
                            // TODO: Add support for fulfillment email
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                                    "OrderEmailFulfillmentTypeNotYetImplemented", locale));
                        } else if ("DIGITAL_DOWNLOAD".equals(fulfillmentType)) {
                            // digital download fulfillment
                            
                            // Nothing to do for here. Downloads are made available to the user
                            // though a query of OrderItems with related ProductContent.
                        } else {
                            Debug.logError("Invalid fulfillment type : " + fulfillmentType + " not supported.", module);
                        }
                    }
                }
            }
        }
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Service to invoice service items from order
     */
    public static Map<String, Object> invoiceServiceItems(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        Locale locale = (Locale) context.get("locale");
        
        OrderReadHelper orh = null;
        try {
            orh = new OrderReadHelper(delegator, orderId);
        } catch (IllegalArgumentException e) {
            Debug.logError(e, "ERROR: Unable to get OrderHeader for orderId : " + orderId, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorUnableToGetOrderHeaderForOrderId", UtilMisc.toMap("orderId", orderId), locale));
        }
        
        // get all the approved items for the order
        List<GenericValue> orderItems = null;
        orderItems = orh.getOrderItemsByCondition(EntityCondition.makeCondition("statusId", "ITEM_APPROVED"));
        
        // find any service items
        List<GenericValue> serviceItems = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderItems)) {
            for (GenericValue item : orderItems) {
                GenericValue product = null;
                try {
                    product = item.getRelatedOne("Product");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "ERROR: Unable to get Product from OrderItem", module);
                }
                if (product != null) {
                    // check for service goods
                    if ("SERVICE".equals(product.get("productTypeId"))) {
                        serviceItems.add(item);
                    }
                }
            }
        }
        
        // now process the service items
        if (UtilValidate.isNotEmpty(serviceItems)) {
            // Make sure there is actually something needing invoicing because createInvoiceForOrder doesn't check
            List<GenericValue> billItems = FastList.newInstance();
            for (GenericValue item : serviceItems) {
                BigDecimal orderQuantity = OrderReadHelper.getOrderItemQuantity(item);
                BigDecimal invoiceQuantity = OrderReadHelper.getOrderItemInvoicedQuantity(item);
                BigDecimal outstandingQuantity = orderQuantity.subtract(invoiceQuantity);
                if (outstandingQuantity.compareTo(ZERO) > 0) {
                    billItems.add(item);
                }
            }
            // do something tricky here: run as a different user that can actually create an invoice, post transaction, etc
            Map<String, Object> invoiceResult = null;
            try {
                GenericValue permUserLogin = ServiceUtil.getUserLogin(dctx, context, "system");
                Map<String, Object> invoiceContext = UtilMisc.toMap("orderId", orderId, "billItems", billItems, "userLogin", permUserLogin);
                invoiceResult = dispatcher.runSync("createInvoiceForOrder", invoiceContext);
            } catch (GenericServiceException e) {
                Debug.logError(e, "ERROR: Unable to invoice service items", module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderProblemWithInvoiceCreationServiceItems", locale));
            }
            if (ModelService.RESPOND_ERROR.equals(invoiceResult.get(ModelService.RESPONSE_MESSAGE))) {
                return ServiceUtil.returnError((String) invoiceResult.get(ModelService.ERROR_MESSAGE));
            }
            
            // update the status of service goods to COMPLETED;
            for (GenericValue item : serviceItems) {
                Map<String, Object> statusCtx = FastMap.newInstance();
                statusCtx.put("orderId", item.getString("orderId"));
                statusCtx.put("orderItemSeqId", item.getString("orderItemSeqId"));
                statusCtx.put("statusId", "ITEM_COMPLETED");
                statusCtx.put("userLogin", userLogin);
                try {
                    dispatcher.runSyncIgnore("changeOrderItemStatus", statusCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, "ERROR: Problem setting the status to COMPLETED : " + item, module);
                }
            }
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> addItemToApprovedOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        String orderId = (String) context.get("orderId");
        String productId = (String) context.get("productId");
        String prodCatalogId = (String) context.get("prodCatalogId");
        BigDecimal basePrice = (BigDecimal) context.get("basePrice");
        BigDecimal quantity = (BigDecimal) context.get("quantity");
        BigDecimal amount = (BigDecimal) context.get("amount");
        Timestamp itemDesiredDeliveryDate = (Timestamp) context.get("itemDesiredDeliveryDate");
        String overridePrice = (String) context.get("overridePrice");
        String reasonEnumId = (String) context.get("reasonEnumId");
        String orderItemTypeId = (String) context.get("reasonEnumId");
        String changeComments = (String) context.get("changeComments");
        Boolean calcTax = (Boolean) context.get("calcTax");
        String productStoreId = (String) context.get("productStoreId");
        if (calcTax == null) {
            calcTax = Boolean.TRUE;
        }
        
        if (amount == null) {
            amount = BigDecimal.ZERO;
        }
        
        int shipGroupIdx = -1;
        try {
            shipGroupIdx = Integer.parseInt(shipGroupSeqId);
            shipGroupIdx--;
        } catch (NumberFormatException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        if (shipGroupIdx < 0) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "OrderShipGroupSeqIdInvalid", UtilMisc.toMap("shipGroupSeqId", shipGroupSeqId), locale));
        }
        if (quantity.compareTo(BigDecimal.ONE) < 0) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "OrderItemQtyMustBePositive", locale));
        }
        
        // obtain a shopping cart object for updating
        ShoppingCart cart = null;
        try {
            cart = loadCartForUpdate(dispatcher, delegator, userLogin, orderId);
        } catch (GeneralException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        if (cart == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "OrderShoppingCartEmpty", locale));
        }
        
        // add in the new product
        try {
            if ("PURCHASE_ORDER".equals(cart.getOrderType())) {
                GenericValue supplierProduct = cart.getSupplierProduct(productId, quantity, dispatcher);
                ShoppingCartItem item = null;
                if (supplierProduct != null) {
                    item = ShoppingCartItem.makePurchaseOrderItem(null, productId, null, quantity, null, null, prodCatalogId, null, orderItemTypeId, null, dispatcher, cart, supplierProduct, itemDesiredDeliveryDate, itemDesiredDeliveryDate, null);
                    cart.addItem(0, item);
                } else {
                    throw new CartItemModifyException("No supplier information found for product [" + productId + "] and quantity quantity [" + quantity + "], cannot add to cart.");
                }
                
                if (basePrice != null) {
                    item.setBasePrice(basePrice);
                    item.setIsModifiedPrice(true);
                }
                
                cart.setItemShipGroupQty(item, item.getQuantity(), shipGroupIdx);
            } else {
                ShoppingCartItem item = ShoppingCartItem.makeItem(null, productId, null, quantity, null, null, null, null, null, null, null, null, prodCatalogId, null, null, null, dispatcher, cart, null, Boolean.FALSE, null, Boolean.FALSE, Boolean.FALSE, "Y");
                if (basePrice != null && overridePrice != null) {
                    item.setBasePrice(basePrice);
                    // special hack to make sure we re-calc the promos after a price change
                    item.setQuantity(quantity.add(BigDecimal.ONE), dispatcher, cart, false);
                    item.setQuantity(quantity, dispatcher, cart, false);
                    item.setBasePrice(basePrice);
                    item.setIsModifiedPrice(true);
                }
                
                // set the item in the selected ship group
                item.setDesiredDeliveryDate(itemDesiredDeliveryDate);
                cart.clearItemShipInfo(item);
                cart.setItemShipGroupQty(item, item.getQuantity(), shipGroupIdx);
            }
        } catch (CartItemModifyException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (ItemNotFoundException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        Map<String, Object> changeMap = UtilMisc.<String, Object>toMap("itemReasonMap", UtilMisc.<String, Object>toMap("reasonEnumId", reasonEnumId),
                "itemCommentMap", UtilMisc.<String, Object>toMap("changeComments", changeComments));
        // save all the updated information
        try {
            saveUpdatedCartToOrder(productStoreId, dispatcher, delegator, cart, locale, userLogin, orderId, changeMap, calcTax, false);
        } catch (GeneralException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        
        // log an order note
        try {
            String addedItemToOrder = UtilProperties.getMessage(resource, "OrderAddedItemToOrder", locale);
            dispatcher.runSync("createOrderNote", UtilMisc.<String, Object>toMap("orderId", orderId, "note", addedItemToOrder +
                    productId + " (" + quantity + ")", "internalNote", "Y", "userLogin", userLogin));
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        result.put("orderId", orderId);
        return result;
    }
    
    public static Map<String, Object> updateApprovedOrderItems(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        String orderId = (String) context.get("orderId");
        Map<String, String> overridePriceMap = UtilGenerics.checkMap(context.get("overridePriceMap"));
        Map<String, String> itemDescriptionMap = UtilGenerics.checkMap(context.get("itemDescriptionMap"));
        Map<String, String> itemPriceMap = UtilGenerics.checkMap(context.get("itemPriceMap"));
        Map<String, String> itemQtyMap = UtilGenerics.checkMap(context.get("itemQtyMap"));
        Map<String, String> itemReasonMap = UtilGenerics.checkMap(context.get("itemReasonMap"));
        Map<String, String> itemCommentMap = UtilGenerics.checkMap(context.get("itemCommentMap"));
        Map<String, String> itemAttributesMap = UtilGenerics.checkMap(context.get("itemAttributesMap"));
        Map<String, String> itemEstimatedShipDateMap = UtilGenerics.checkMap(context.get("itemShipDateMap"));
        Map<String, String> itemEstimatedDeliveryDateMap = UtilGenerics.checkMap(context.get("itemDeliveryDateMap"));
        Boolean calcTax = (Boolean) context.get("calcTax");
        if (calcTax == null) {
            calcTax = Boolean.TRUE;
        }
        
        // obtain a shopping cart object for updating
        ShoppingCart cart = null;
        try {
            cart = loadCartForUpdate(dispatcher, delegator, userLogin, orderId);
        } catch (GeneralException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        if (cart == null) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "OrderShoppingCartEmpty", locale));
        }
        
        // go through the item map and obtain the totals per item
        Map<String, BigDecimal> itemTotals = new HashMap<String, BigDecimal>();
        for (String key : itemQtyMap.keySet()) {
            String quantityStr = itemQtyMap.get(key);
            BigDecimal groupQty = BigDecimal.ZERO;
            try {
                groupQty = (BigDecimal) ObjectType.simpleTypeConvert(quantityStr, "BigDecimal", null, locale);
            } catch (GeneralException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            if (groupQty.compareTo(BigDecimal.ONE) < 0) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "OrderItemQtyMustBePositive", locale));
            }
            
            String[] itemInfo = key.split(":");
            BigDecimal tally = itemTotals.get(itemInfo[0]);
            if (tally == null) {
                tally = groupQty;
            } else {
                tally = tally.add(groupQty);
            }
            itemTotals.put(itemInfo[0], tally);
        }
        
        // set the items amount/price
        for (String itemSeqId : itemTotals.keySet()) {
            ShoppingCartItem cartItem = cart.findCartItem(itemSeqId);
            
            if (cartItem != null) {
                BigDecimal qty = itemTotals.get(itemSeqId);
                BigDecimal priceSave = cartItem.getBasePrice();
                
                // set quantity
                try {
                    cartItem.setQuantity(qty, dispatcher, cart, false, false); // trigger external ops, don't reset ship groups (and update prices for both PO and SO items)
                } catch (CartItemModifyException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                Debug.logInfo("Set item quantity: [" + itemSeqId + "] " + qty, module);
                
                if (cartItem.getIsModifiedPrice()) // set price
                {
                    cartItem.setBasePrice(priceSave);
                }
                
                if (overridePriceMap.containsKey(itemSeqId)) {
                    String priceStr = itemPriceMap.get(itemSeqId);
                    if (UtilValidate.isNotEmpty(priceStr)) {
                        BigDecimal price = null;
                        try {
                            price = (BigDecimal) ObjectType.simpleTypeConvert(priceStr, "BigDecimal", null, locale);
                        } catch (GeneralException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }
                        price = price.setScale(orderDecimals, orderRounding);
                        cartItem.setBasePrice(price);
                        cartItem.setIsModifiedPrice(true);
                        Debug.logInfo("Set item price: [" + itemSeqId + "] " + price, module);
                    }
                    
                }
                
                // Update the item description
                if (itemDescriptionMap != null && itemDescriptionMap.containsKey(itemSeqId)) {
                    String description = itemDescriptionMap.get(itemSeqId);
                    if (UtilValidate.isNotEmpty(description)) {
                        cartItem.setName(description);
                        Debug.logInfo("Set item description: [" + itemSeqId + "] " + description, module);
                    } else {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                "OrderItemDescriptionCannotBeEmpty", locale));
                    }
                }
                
                // update the order item attributes
                if (itemAttributesMap != null) {
                    // go through the item attributes map once to get a list of key names
                    Set<String> attributeNames = FastSet.newInstance();
                    Set<String> keys = itemAttributesMap.keySet();
                    for (String key : keys) {
                        String[] attributeInfo = key.split(":");
                        attributeNames.add(attributeInfo[0]);
                    }
                    
                    String attrValue = null;
                    for (String attrName : attributeNames) {
                        attrValue = itemAttributesMap.get(attrName + ":" + itemSeqId);
                        if (UtilValidate.isNotEmpty(attrName)) {
                            cartItem.setOrderItemAttribute(attrName, attrValue);
                            Debug.logInfo("Set item attribute Name: [" + itemSeqId + "] " + attrName + " , Value:" + attrValue, module);
                        }
                    }
                }
                
            } else {
                Debug.logInfo("Unable to locate shopping cart item for seqId #" + itemSeqId, module);
            }
        }
        // Create Estimated Delivery dates
        if (null != itemEstimatedDeliveryDateMap) {
            for (Map.Entry<String, String> entry : itemEstimatedDeliveryDateMap.entrySet()) {
                String itemSeqId = entry.getKey();
                
                // ignore internationalised variant of dates
                if (!itemSeqId.endsWith("_i18n")) {
                    String estimatedDeliveryDate = entry.getValue();
                    if (UtilValidate.isNotEmpty(estimatedDeliveryDate)) {
                        Timestamp deliveryDate = Timestamp.valueOf(estimatedDeliveryDate);
                        ShoppingCartItem cartItem = cart.findCartItem(itemSeqId);
                        cartItem.setDesiredDeliveryDate(deliveryDate);
                    }
                }
            }
        }
        
        // Create Estimated ship dates
        if (null != itemEstimatedShipDateMap) {
            for (Map.Entry<String, String> entry : itemEstimatedShipDateMap.entrySet()) {
                String itemSeqId = entry.getKey();
                
                // ignore internationalised variant of dates
                if (!itemSeqId.endsWith("_i18n")) {
                    String estimatedShipDate = entry.getValue();
                    if (UtilValidate.isNotEmpty(estimatedShipDate)) {
                        Timestamp shipDate = Timestamp.valueOf(estimatedShipDate);
                        ShoppingCartItem cartItem = cart.findCartItem(itemSeqId);
                        cartItem.setEstimatedShipDate(shipDate);
                    }
                }
            }
        }
        
        // update the group amounts
        for (String key : itemQtyMap.keySet()) {
            String quantityStr = itemQtyMap.get(key);
            BigDecimal groupQty = BigDecimal.ZERO;
            try {
                groupQty = (BigDecimal) ObjectType.simpleTypeConvert(quantityStr, "BigDecimal", null, locale);
            } catch (GeneralException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            String[] itemInfo = key.split(":");
            @SuppressWarnings("unused")
            int groupIdx = -1;
            try {
                groupIdx = Integer.parseInt(itemInfo[1]);
            } catch (NumberFormatException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            // set the group qty
            ShoppingCartItem cartItem = cart.findCartItem(itemInfo[0]);
            if (cartItem != null) {
                Debug.logInfo("Shipping info (before) for group #" + (groupIdx - 1) + " [" + cart.getShipmentMethodTypeId(groupIdx - 1) + " / " + cart.getCarrierPartyId(groupIdx - 1) + "]", module);
                cart.setItemShipGroupQty(cartItem, groupQty, groupIdx - 1);
                Debug.logInfo("Set ship group qty: [" + itemInfo[0] + " / " + itemInfo[1] + " (" + (groupIdx - 1) + ")] " + groupQty, module);
                Debug.logInfo("Shipping info (after) for group #" + (groupIdx - 1) + " [" + cart.getShipmentMethodTypeId(groupIdx - 1) + " / " + cart.getCarrierPartyId(groupIdx - 1) + "]", module);
            }
        }
        String productStoreId = null;
        
        // save all the updated information
        try {
            saveUpdatedCartToOrder(productStoreId, dispatcher, delegator, cart, locale, userLogin, orderId, UtilMisc.<String, Object>toMap("itemReasonMap", itemReasonMap, "itemCommentMap", itemCommentMap), calcTax, false);
        } catch (GeneralException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        
        // run promotions to handle all changes in the cart
        ProductPromoWorker.doPromotions(cart, dispatcher);
        
        // log an order note
        try {
            dispatcher.runSync("createOrderNote", UtilMisc.<String, Object>toMap("orderId", orderId, "note", "Updated order.", "internalNote", "Y", "userLogin", userLogin));
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        result.put("orderId", orderId);
        return result;
    }
    
    public static Map<String, Object> loadCartForUpdate(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        
        String orderId = (String) context.get("orderId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        
        ShoppingCart cart = null;
        Map<String, Object> result = null;
        try {
            cart = loadCartForUpdate(dispatcher, delegator, userLogin, orderId);
            result = ServiceUtil.returnSuccess();
            result.put("shoppingCart", cart);
        } catch (GeneralException e) {
            Debug.logError(e, module);
            result = ServiceUtil.returnError(e.getMessage());
        }
        
        result.put("orderId", orderId);
        return result;
    }
    
    /*
     *  Warning: loadCartForUpdate(...) and saveUpdatedCartToOrder(...) must always
     *           be used together in this sequence.
     *           In fact loadCartForUpdate(...) will remove or cancel data associated to the order,
     *           before returning the ShoppingCart object; for this reason, the cart
     *           must be stored back using the method saveUpdatedCartToOrder(...),
     *           because that method will recreate the data.
     */
    private static ShoppingCart loadCartForUpdate(LocalDispatcher dispatcher, Delegator delegator, GenericValue userLogin, String orderId) throws GeneralException {
        // load the order into a shopping cart
        Map<String, Object> loadCartResp = null;
        try {
            loadCartResp = dispatcher.runSync("loadCartFromOrder", UtilMisc.<String, Object>toMap("orderId", orderId,
                    "skipInventoryChecks", Boolean.TRUE, // the items are already reserved, no need to check again
                    "skipProductChecks", Boolean.TRUE, // the products are already in the order, no need to check their validity now
                    "userLogin", userLogin));
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        if (ServiceUtil.isError(loadCartResp)) {
            throw new GeneralException(ServiceUtil.getErrorMessage(loadCartResp));
        }
        
        ShoppingCart cart = (ShoppingCart) loadCartResp.get("shoppingCart");
        if (cart == null) {
            throw new GeneralException("Error loading shopping cart from order [" + orderId + "]");
        } else {
            cart.setOrderId(orderId);
        }
        
        // Now that the cart is loaded, all the data that will be re-created
        // when the method saveUpdatedCartToOrder(...) will be called, are
        // removed and cancelled:
        // - inventory reservations are cancelled
        // - promotional items are cancelled
        // - order payments are released (cancelled)
        // - offline non received payments are cancelled
        // - promotional, shipping and tax adjustments are removed
        
        // Inventory reservations
        // find ship group associations
        List<GenericValue> shipGroupAssocs = null;
        try {
            shipGroupAssocs = delegator.findByAnd("OrderItemShipGroupAssoc", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        // cancel existing inventory reservations
        if (shipGroupAssocs != null) {
            for (GenericValue shipGroupAssoc : shipGroupAssocs) {
                String orderItemSeqId = shipGroupAssoc.getString("orderItemSeqId");
                String shipGroupSeqId = shipGroupAssoc.getString("shipGroupSeqId");
                
                Map<String, Object> cancelCtx = UtilMisc.<String, Object>toMap("userLogin", userLogin, "orderId", orderId);
                cancelCtx.put("orderItemSeqId", orderItemSeqId);
                cancelCtx.put("shipGroupSeqId", shipGroupSeqId);
                
                Map<String, Object> cancelResp = null;
                try {
                    cancelResp = dispatcher.runSync("cancelOrderInventoryReservation", cancelCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    throw new GeneralException(e.getMessage());
                }
                if (ServiceUtil.isError(cancelResp)) {
                    throw new GeneralException(ServiceUtil.getErrorMessage(cancelResp));
                }
            }
        }
        
        // cancel promo items -- if the promo still qualifies it will be added by the cart
        List<GenericValue> promoItems = null;
        try {
            promoItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId, "isPromo", "Y"));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        if (promoItems != null) {
            for (GenericValue promoItem : promoItems) {
                // Skip if the promo is already cancelled
                if ("ITEM_CANCELLED".equals(promoItem.get("statusId"))) {
                    continue;
                }
                Map<String, Object> cancelPromoCtx = UtilMisc.<String, Object>toMap("orderId", orderId);
                cancelPromoCtx.put("orderItemSeqId", promoItem.getString("orderItemSeqId"));
                cancelPromoCtx.put("userLogin", userLogin);
                Map<String, Object> cancelResp = null;
                try {
                    cancelResp = dispatcher.runSync("cancelOrderItemNoActions", cancelPromoCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    throw new GeneralException(e.getMessage());
                }
                if (ServiceUtil.isError(cancelResp)) {
                    throw new GeneralException(ServiceUtil.getErrorMessage(cancelResp));
                }
            }
        }
        
        // cancel exiting authorizations
        Map<String, Object> releaseResp = null;
        try {
            releaseResp = dispatcher.runSync("releaseOrderPayments", UtilMisc.<String, Object>toMap("orderId", orderId, "userLogin", userLogin));
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        if (ServiceUtil.isError(releaseResp)) {
            throw new GeneralException(ServiceUtil.getErrorMessage(releaseResp));
        }
        
        // cancel other (non-completed and non-cancelled) payments
        List<GenericValue> paymentPrefsToCancel = null;
        try {
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_RECEIVED"));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_CANCELLED"));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_DECLINED"));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_SETTLED"));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_REFUNDED"));
            EntityCondition cond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
            paymentPrefsToCancel = delegator.findList("OrderPaymentPreference", cond, null, null, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        if (paymentPrefsToCancel != null) {
            for (GenericValue opp : paymentPrefsToCancel) {
                try {
                    opp.set("statusId", "PAYMENT_CANCELLED");
                    opp.store();
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    throw new GeneralException(e.getMessage());
                }
            }
        }
        
        // remove the adjustments
        try {
            List<EntityCondition> adjExprs = new LinkedList<EntityCondition>();
            adjExprs.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
            List<EntityCondition> exprs = new LinkedList<EntityCondition>();
            exprs.add(EntityCondition.makeCondition("orderAdjustmentTypeId", EntityOperator.EQUALS, "PROMOTION_ADJUSTMENT"));
            exprs.add(EntityCondition.makeCondition("orderAdjustmentTypeId", EntityOperator.EQUALS, "SHIPPING_CHARGES"));
            exprs.add(EntityCondition.makeCondition("orderAdjustmentTypeId", EntityOperator.EQUALS, "SALES_TAX"));
            exprs.add(EntityCondition.makeCondition("orderAdjustmentTypeId", EntityOperator.EQUALS, "VAT_TAX"));
            exprs.add(EntityCondition.makeCondition("orderAdjustmentTypeId", EntityOperator.EQUALS, "VAT_PRICE_CORRECT"));
            adjExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));
            EntityCondition cond = EntityCondition.makeCondition(adjExprs, EntityOperator.AND);
            delegator.removeByCondition("OrderAdjustment", cond);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        
        return cart;
    }
    
    public static Map<String, Object> saveUpdatedCartToOrder(DispatchContext dctx, Map<String, ? extends Object> context) throws GeneralException {
        
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        
        String orderId = (String) context.get("orderId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
        Map<String, Object> changeMap = UtilGenerics.checkMap(context.get("changeMap"));
        Locale locale = (Locale) context.get("locale");
        Boolean deleteItems = (Boolean) context.get("deleteItems");
        Boolean calcTax = (Boolean) context.get("calcTax");
        if (calcTax == null) {
            calcTax = Boolean.TRUE;
        }
        
        Map<String, Object> result = null;
        try {
            saveUpdatedCartToOrder(null, dispatcher, delegator, cart, locale, userLogin, orderId, changeMap, calcTax, deleteItems);
            result = ServiceUtil.returnSuccess();
            //result.put("shoppingCart", cart);
        } catch (GeneralException e) {
            Debug.logError(e, module);
            result = ServiceUtil.returnError(e.getMessage());
        }
        
        result.put("orderId", orderId);
        return result;
    }
    
    private static void saveUpdatedCartToOrder(String productStoreId, LocalDispatcher dispatcher, Delegator delegator, ShoppingCart cart,
                                               Locale locale, GenericValue userLogin, String orderId, Map<String, Object> changeMap, boolean calcTax,
                                               boolean deleteItems) throws GeneralException {
        // get/set the shipping estimates.  if it's a SALES ORDER, then return an error if there are no ship estimates
        int shipGroups = cart.getShipGroupSize();
        for (int gi = 0; gi < shipGroups; gi++) {
            String shipmentMethodTypeId = cart.getShipmentMethodTypeId(gi);
            String carrierPartyId = cart.getCarrierPartyId(gi);
            Debug.logInfo("Getting ship estimate for group #" + gi + " [" + shipmentMethodTypeId + " / " + carrierPartyId + "]", module);
            Map<String, Object> result = ShippingEvents.getShipGroupEstimate(dispatcher, delegator, cart, productStoreId);
            if (("SALES_ORDER".equals(cart.getOrderType())) && (ServiceUtil.isError(result))) {
                Debug.logError(ServiceUtil.getErrorMessage(result), module);
                throw new GeneralException(ServiceUtil.getErrorMessage(result));
            }
            
            BigDecimal shippingTotal = (BigDecimal) result.get("shippingTotal");
            if (shippingTotal == null) {
                shippingTotal = BigDecimal.ZERO;
            }
            cart.setItemShipGroupEstimate(shippingTotal, gi);
        }
        
        // calc the sales tax
        CheckOutHelper coh = new CheckOutHelper(dispatcher, delegator, cart);
        
        
        // get the new orderItems, adjustments, shipping info, payments and order item attributes from the cart
        List<Map<String, Object>> modifiedItems = FastList.newInstance();
        List<GenericValue> toStore = new LinkedList<GenericValue>();
        List<GenericValue> toAddList = new ArrayList<GenericValue>();
        toAddList.addAll(cart.makeAllAdjustments(productStoreId));
        cart.clearAllPromotionAdjustments();
        ProductPromoWorker.doPromotions(cart, dispatcher);
        
        // validate the payment methods
        Map<String, Object> validateResp = coh.validatePaymentMethods();
        if (ServiceUtil.isError(validateResp)) {
            throw new GeneralException(ServiceUtil.getErrorMessage(validateResp));
        }
        
        // handle OrderHeader fields
        String billingAccountId = cart.getBillingAccountId();
        if (UtilValidate.isNotEmpty(billingAccountId)) {
            try {
                GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
                orderHeader.set("billingAccountId", billingAccountId);
                toStore.add(orderHeader);
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                throw new GeneralException(e.getMessage());
            }
        }
        
        toStore.addAll(cart.makeOrderItems());
        toStore.addAll(cart.makeAllAdjustments(productStoreId));
        
        String shipGroupSeqId = null;
        long groupIndex = cart.getShipInfoSize();
        if (!deleteItems) {
            for (long itr = 1; itr <= groupIndex; itr++) {
                shipGroupSeqId = UtilFormatOut.formatPaddedNumber(itr, 5);
                List<GenericValue> removeList = new ArrayList<GenericValue>();
                for (GenericValue stored : toStore) {
                    if ("OrderAdjustment".equals(stored.getEntityName())) {
                        if (("SHIPPING_CHARGES".equals(stored.get("orderAdjustmentTypeId")) ||
                                "SALES_TAX".equals(stored.get("orderAdjustmentTypeId"))) &&
                                stored.get("orderId").equals(orderId) &&
                                stored.get("shipGroupSeqId").equals(shipGroupSeqId)) {
                            // Removing objects from toStore list for old Shipping and Handling Charges Adjustment and Sales Tax Adjustment.
                            removeList.add(stored);
                        }
                        if (stored.get("comments") != null && ((String) stored.get("comments")).startsWith("Added manually by")) {
                            // Removing objects from toStore list for Manually added Adjustment.
                            removeList.add(stored);
                        }
                    }
                }
                toStore.removeAll(removeList);
            }
            for (GenericValue toAdd : toAddList) {
                if ("OrderAdjustment".equals(toAdd.getEntityName())) {
                    if (toAdd.get("comments") != null && ((String) toAdd.get("comments")).startsWith("Added manually by") && (("PROMOTION_ADJUSTMENT".equals(toAdd.get("orderAdjustmentTypeId"))) ||
                            ("SHIPPING_CHARGES".equals(toAdd.get("orderAdjustmentTypeId"))) || ("SALES_TAX".equals(toAdd.get("orderAdjustmentTypeId"))))) {
                        toStore.add(toAdd);
                    }
                }
            }
        } else {
            // add all the cart adjustments
            toStore.addAll(toAddList);
        }
        
        // Creating objects for New Shipping and Handling Charges Adjustment and Sales Tax Adjustment
        toStore.addAll(cart.makeAllShipGroupInfos(productStoreId));
        toStore.addAll(cart.makeAllOrderPaymentInfos(dispatcher));
        toStore.addAll(cart.makeAllOrderItemAttributes(orderId, ShoppingCart.FILLED_ONLY));
        
        
        List<GenericValue> toRemove = FastList.newInstance();
        if (deleteItems) {
            // flag to delete existing order items and adjustments
            try {
                toRemove.addAll(delegator.findByAnd("OrderItemShipGroupAssoc", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderItemContactMech", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderItemPriceInfo", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderItemAttribute", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderItemBilling", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderItemRole", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderItemChange", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderAdjustment", "orderId", orderId));
                toRemove.addAll(delegator.findByAnd("OrderItem", "orderId", orderId));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        } else {
            // get the empty order item atrributes from the cart and remove them
            toRemove.addAll(cart.makeAllOrderItemAttributes(orderId, ShoppingCart.EMPTY_ONLY));
        }
        
        // get the promo uses and codes
        for (String promoCodeEntered : cart.getProductPromoCodesEntered()) {
            GenericValue orderProductPromoCode = delegator.makeValue("OrderProductPromoCode");
            orderProductPromoCode.set("orderId", orderId);
            orderProductPromoCode.set("productPromoCodeId", promoCodeEntered);
            toStore.add(orderProductPromoCode);
        }
        for (GenericValue promoUse : cart.makeProductPromoUses()) {
            promoUse.set("orderId", orderId);
            toStore.add(promoUse);
        }
        
        List<GenericValue> existingPromoCodes = null;
        List<GenericValue> existingPromoUses = null;
        try {
            existingPromoCodes = delegator.findByAnd("OrderProductPromoCode", UtilMisc.toMap("orderId", orderId));
            existingPromoUses = delegator.findByAnd("ProductPromoUse", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        toRemove.addAll(existingPromoCodes);
        toRemove.addAll(existingPromoUses);
        
        // set the orderId & other information on all new value objects
        List<String> dropShipGroupIds = FastList.newInstance(); // this list will contain the ids of all the ship groups for drop shipments (no reservations)
        for (GenericValue valueObj : toStore) {
            valueObj.set("orderId", orderId);
            if ("OrderItemShipGroup".equals(valueObj.getEntityName())) {
                // ship group
                if (valueObj.get("carrierRoleTypeId") == null) {
                    valueObj.set("carrierRoleTypeId", "CARRIER");
                }
                
            } else if ("OrderAdjustment".equals(valueObj.getEntityName())) {
                // shipping / tax adjustment(s)
                if (UtilValidate.isEmpty(valueObj.get("orderItemSeqId"))) {
                    valueObj.set("orderItemSeqId", DataModelConstants.SEQ_ID_NA);
                }
                // in order to avoid duplicate adjustments don't set orderAdjustmentId (which is the pk) if there is already one
                if (UtilValidate.isEmpty(valueObj.getString("orderAdjustmentId"))) {
                    valueObj.set("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment"));
                }
                valueObj.set("createdDate", UtilDateTime.nowTimestamp());
                valueObj.set("createdByUserLogin", userLogin.getString("userLoginId"));
            } else if ("OrderPaymentPreference".equals(valueObj.getEntityName())) {
                if (valueObj.get("orderPaymentPreferenceId") == null) {
                    valueObj.set("orderPaymentPreferenceId", delegator.getNextSeqId("OrderPaymentPreference"));
                    valueObj.set("createdDate", UtilDateTime.nowTimestamp());
                    valueObj.set("createdByUserLogin", userLogin.getString("userLoginId"));
                }
                if (valueObj.get("statusId") == null) {
                    valueObj.set("statusId", "PAYMENT_NOT_RECEIVED");
                }
            } else if ("OrderItem".equals(valueObj.getEntityName()) && !deleteItems) {
                
                //  ignore promotion items. They are added/canceled automatically
                if ("Y".equals(valueObj.getString("isPromo"))) {
                    continue;
                }
                GenericValue oldOrderItem = null;
                try {
                    oldOrderItem = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", valueObj.getString("orderId"), "orderItemSeqId", valueObj.getString("orderItemSeqId")));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    throw new GeneralException(e.getMessage());
                }
                if (UtilValidate.isNotEmpty(oldOrderItem)) {
                    
                    //  Existing order item found. Check for modifications and store if any
                    String oldItemDescription = oldOrderItem.getString("itemDescription") != null ? oldOrderItem.getString("itemDescription") : "";
                    BigDecimal oldQuantity = oldOrderItem.getBigDecimal("quantity") != null ? oldOrderItem.getBigDecimal("quantity") : BigDecimal.ZERO;
                    BigDecimal oldUnitPrice = oldOrderItem.getBigDecimal("unitPrice") != null ? oldOrderItem.getBigDecimal("unitPrice") : BigDecimal.ZERO;
                    
                    boolean changeFound = false;
                    Map<String, Object> modifiedItem = FastMap.newInstance();
                    if (!oldItemDescription.equals(valueObj.getString("itemDescription"))) {
                        modifiedItem.put("itemDescription", oldItemDescription);
                        changeFound = true;
                    }
                    
                    BigDecimal quantityDif = valueObj.getBigDecimal("quantity").subtract(oldQuantity);
                    BigDecimal unitPriceDif = valueObj.getBigDecimal("unitPrice").subtract(oldUnitPrice);
                    if (quantityDif.compareTo(BigDecimal.ZERO) != 0) {
                        modifiedItem.put("quantity", quantityDif);
                        changeFound = true;
                    }
                    if (unitPriceDif.compareTo(BigDecimal.ZERO) != 0) {
                        modifiedItem.put("unitPrice", unitPriceDif);
                        changeFound = true;
                    }
                    if (changeFound) {
                        
                        //  found changes to store
                        Map<String, String> itemReasonMap = UtilGenerics.checkMap(changeMap.get("itemReasonMap"));
                        Map<String, String> itemCommentMap = UtilGenerics.checkMap(changeMap.get("itemCommentMap"));
                        if (UtilValidate.isNotEmpty(itemReasonMap)) {
                            String changeReasonId = itemReasonMap.get(valueObj.getString("orderItemSeqId"));
                            modifiedItem.put("reasonEnumId", changeReasonId);
                        }
                        if (UtilValidate.isNotEmpty(itemCommentMap)) {
                            String changeComments = itemCommentMap.get(valueObj.getString("orderItemSeqId"));
                            modifiedItem.put("changeComments", changeComments);
                        }
                        
                        modifiedItem.put("orderId", valueObj.getString("orderId"));
                        modifiedItem.put("orderItemSeqId", valueObj.getString("orderItemSeqId"));
                        modifiedItem.put("changeTypeEnumId", "ODR_ITM_UPDATE");
                        modifiedItems.add(modifiedItem);
                    }
                } else {
                    
                    //  this is a new item appended to the order
                    Map<String, String> itemReasonMap = UtilGenerics.checkMap(changeMap.get("itemReasonMap"));
                    Map<String, String> itemCommentMap = UtilGenerics.checkMap(changeMap.get("itemCommentMap"));
                    Map<String, Object> appendedItem = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(itemReasonMap)) {
                        String changeReasonId = itemReasonMap.get("reasonEnumId");
                        appendedItem.put("reasonEnumId", changeReasonId);
                    }
                    if (UtilValidate.isNotEmpty(itemCommentMap)) {
                        String changeComments = itemCommentMap.get("changeComments");
                        appendedItem.put("changeComments", changeComments);
                    }
                    
                    appendedItem.put("orderId", valueObj.getString("orderId"));
                    appendedItem.put("orderItemSeqId", valueObj.getString("orderItemSeqId"));
                    appendedItem.put("quantity", valueObj.getBigDecimal("quantity"));
                    appendedItem.put("changeTypeEnumId", "ODR_ITM_APPEND");
                    modifiedItems.add(appendedItem);
                }
            }
        }
        
        if (Debug.verboseOn()) {
            Debug.logVerbose("To Store Contains: " + toStore, module);
        }
        
        // remove any order item attributes that were set to empty
        try {
            delegator.removeAll(toRemove, true);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        
        // store the new items/adjustments/order item attributes
        try {
            delegator.storeAll(toStore);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        
        //  store the OrderItemChange
        if (UtilValidate.isNotEmpty(modifiedItems)) {
            for (Map<String, Object> modifiendItem : modifiedItems) {
                Map<String, Object> serviceCtx = FastMap.newInstance();
                serviceCtx.put("orderId", modifiendItem.get("orderId"));
                serviceCtx.put("orderItemSeqId", modifiendItem.get("orderItemSeqId"));
                serviceCtx.put("itemDescription", modifiendItem.get("itemDescription"));
                serviceCtx.put("quantity", modifiendItem.get("quantity"));
                serviceCtx.put("unitPrice", modifiendItem.get("unitPrice"));
                serviceCtx.put("changeTypeEnumId", modifiendItem.get("changeTypeEnumId"));
                serviceCtx.put("reasonEnumId", modifiendItem.get("reasonEnumId"));
                serviceCtx.put("changeComments", modifiendItem.get("changeComments"));
                serviceCtx.put("userLogin", userLogin);
                Map<String, Object> resp = null;
                try {
                    resp = dispatcher.runSync("createOrderItemChange", serviceCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    throw new GeneralException(e.getMessage());
                }
                if (ServiceUtil.isError(resp)) {
                    throw new GeneralException((String) resp.get(ModelService.ERROR_MESSAGE));
                }
            }
        }
        
        // make the order item object map & the ship group assoc list
        List<GenericValue> orderItemShipGroupAssoc = new LinkedList<GenericValue>();
        Map<String, GenericValue> itemValuesBySeqId = new HashMap<String, GenericValue>();
        for (GenericValue v : toStore) {
            if ("OrderItem".equals(v.getEntityName())) {
                itemValuesBySeqId.put(v.getString("orderItemSeqId"), v);
            } else if ("OrderItemShipGroupAssoc".equals(v.getEntityName())) {
                orderItemShipGroupAssoc.add(v);
            }
        }
        
        
        String orderTypeId = cart.getOrderType();
        List<String> resErrorMessages = new LinkedList<String>();
        try {
            Debug.logInfo("Calling reserve inventory...", module);
            reserveInventory(delegator, dispatcher, userLogin, locale, orderItemShipGroupAssoc, dropShipGroupIds, itemValuesBySeqId,
                    orderTypeId, productStoreId, resErrorMessages);
        } catch (GeneralException e) {
            Debug.logError(e, module);
            throw new GeneralException(e.getMessage());
        }
        
        if (resErrorMessages.size() > 0) {
            throw new GeneralException(ServiceUtil.getErrorMessage(ServiceUtil.returnError(resErrorMessages)));
        }
    }
    
    public static Map<String, Object> processOrderPayments(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        Locale locale = (Locale) context.get("locale");
        
        OrderReadHelper orh = new OrderReadHelper(delegator, orderId);
        String productStoreId = orh.getProductStoreId();
        
        // check if order was already cancelled / rejected
        GenericValue orderHeader = orh.getOrderHeader();
        String orderStatus = orderHeader.getString("statusId");
        if ("ORDER_CANCELLED".equals(orderStatus) || "ORDER_REJECTED".equals(orderStatus)) {
            return ServiceUtil.returnFailure(UtilProperties.getMessage(resource,
                    "OrderProcessOrderPaymentsStatusInvalid", locale) + orderStatus);
        }
        
        // process the payments
        if (!"PURCHASE_ORDER".equals(orh.getOrderTypeId())) {
            GenericValue productStore = ProductStoreWorker.getProductStore(productStoreId, delegator);
            Map<String, Object> paymentResp = null;
            try {
                Debug.logInfo("Calling process payments...", module);
                //Debug.set(Debug.VERBOSE, true);
                paymentResp = CheckOutHelper.processPayment(orderId, orh.getOrderGrandTotal(), orh.getCurrency(), productStore, userLogin, false, false, dispatcher, delegator);
                //Debug.set(Debug.VERBOSE, false);
            } catch (GeneralException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            } catch (GeneralRuntimeException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            if (ServiceUtil.isError(paymentResp)) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "OrderProcessOrderPayments", locale), null, null, paymentResp);
            }
        }
        return ServiceUtil.returnSuccess();
    }
    
    // sample test services
    public static Map<String, Object> shoppingCartTest(DispatchContext dctx, Map<String, ? extends Object> context) {
        Locale locale = (Locale) context.get("locale");
        ShoppingCart cart = new ShoppingCart(dctx.getDelegator(), "9000", "webStore", locale, "USD");
        try {
            cart.addOrIncreaseItem("GZ-1005", null, BigDecimal.ONE, null, null, null, null, null, null, null, "DemoCatalog", null, null, null, null, dctx.getDispatcher(), "Y", null);
        } catch (CartItemModifyException e) {
            Debug.logError(e, module);
        } catch (ItemNotFoundException e) {
            Debug.logError(e, module);
        }
        
        try {
            dctx.getDispatcher().runAsync("shoppingCartRemoteTest", UtilMisc.toMap("cart", cart), true);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> shoppingCartRemoteTest(DispatchContext dctx, Map<String, ? extends Object> context) {
        ShoppingCart cart = (ShoppingCart) context.get("cart");
        Debug.logInfo("Product ID : " + cart.findCartItem(0).getProductId(), module);
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Service to create a payment using an order payment preference.
     *
     * @return Map
     */
    public static Map<String, Object> createPaymentFromPreference(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderPaymentPreferenceId = (String) context.get("orderPaymentPreferenceId");
        String paymentRefNum = (String) context.get("paymentRefNum");
        String paymentFromId = (String) context.get("paymentFromId");
        String comments = (String) context.get("comments");
        Timestamp eventDate = (Timestamp) context.get("eventDate");
        Locale locale = (Locale) context.get("locale");
        if (UtilValidate.isEmpty(eventDate)) {
            eventDate = UtilDateTime.nowTimestamp();
        }
        try {
            // get the order payment preference
            GenericValue orderPaymentPreference = delegator.findByPrimaryKey("OrderPaymentPreference", UtilMisc.toMap("orderPaymentPreferenceId", orderPaymentPreferenceId));
            if (orderPaymentPreference == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "OrderOrderPaymentCannotBeCreated",
                        UtilMisc.toMap("orderPaymentPreferenceId", "orderPaymentPreferenceId"), locale));
            }
            
            // get the order header
            GenericValue orderHeader = orderPaymentPreference.getRelatedOne("OrderHeader");
            if (orderHeader == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "OrderOrderPaymentCannotBeCreatedWithRelatedOrderHeader", locale));
            }
            
            // get the store for the order.  It will be used to set the currency
            GenericValue productStore = orderHeader.getRelatedOne("ProductStore");
            if (productStore == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "OrderOrderPaymentCannotBeCreatedWithRelatedProductStore", locale));
            }
            
            // get the partyId billed to
            if (paymentFromId == null) {
                OrderReadHelper orh = new OrderReadHelper(orderHeader);
                GenericValue billToParty = orh.getBillToParty();
                if (billToParty != null) {
                    paymentFromId = billToParty.getString("partyId");
                } else {
                    paymentFromId = "_NA_";
                }
            }
            
            // set the payToPartyId
            String payToPartyId = productStore.getString("ownerPartyId");
            if (payToPartyId == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "OrderOrderPaymentCannotBeCreatedPayToPartyIdNotSet", locale));
            }
            
            // create the payment
            Map<String, Object> paymentParams = new HashMap<String, Object>();
            BigDecimal maxAmount = orderPaymentPreference.getBigDecimal("maxAmount");
            //if (maxAmount > 0.0) {
            paymentParams.put("paymentTypeId", "CUSTOMER_PAYMENT");
            paymentParams.put("paymentMethodTypeId", orderPaymentPreference.getString("paymentMethodTypeId"));
            paymentParams.put("paymentPreferenceId", orderPaymentPreference.getString("orderPaymentPreferenceId"));
            paymentParams.put("amount", maxAmount);
            paymentParams.put("statusId", "PMNT_RECEIVED");
            paymentParams.put("effectiveDate", eventDate);
            paymentParams.put("partyIdFrom", paymentFromId);
            paymentParams.put("currencyUomId", productStore.getString("defaultCurrencyUomId"));
            paymentParams.put("partyIdTo", payToPartyId);
            /*}
            else {
                paymentParams.put("paymentTypeId", "CUSTOMER_REFUND"); // JLR 17/7/4 from a suggestion of Si cf. https://issues.apache.org/jira/browse/OFBIZ-828#action_12483045
                paymentParams.put("paymentMethodTypeId", orderPaymentPreference.getString("paymentMethodTypeId")); // JLR 20/7/4 Finally reverted for now, I prefer to see an amount in payment, even negative
                paymentParams.put("paymentPreferenceId", orderPaymentPreference.getString("orderPaymentPreferenceId"));
                paymentParams.put("amount", Double.valueOf(Math.abs(maxAmount)));
                paymentParams.put("statusId", "PMNT_RECEIVED");
                paymentParams.put("effectiveDate", UtilDateTime.nowTimestamp());
                paymentParams.put("partyIdFrom", payToPartyId);
                paymentParams.put("currencyUomId", productStore.getString("defaultCurrencyUomId"));
                paymentParams.put("partyIdTo", billToParty.getString("partyId"));
            }*/
            if (paymentRefNum != null) {
                paymentParams.put("paymentRefNum", paymentRefNum);
            }
            if (comments != null) {
                paymentParams.put("comments", comments);
            }
            paymentParams.put("userLogin", userLogin);
            
            return dispatcher.runSync("createPayment", paymentParams);
            
        } catch (GenericEntityException ex) {
            Debug.logError(ex, "Unable to create payment using payment preference.", module);
            return (ServiceUtil.returnError(ex.getMessage()));
        } catch (GenericServiceException ex) {
            Debug.logError(ex, "Unable to create payment using payment preference.", module);
            return (ServiceUtil.returnError(ex.getMessage()));
        }
    }
    
    
    public static Map<String, Object> checkCreateDropShipPurchaseOrders(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        // TODO (use the "system" user)
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        Locale locale = (Locale) context.get("locale");
        OrderReadHelper orh = new OrderReadHelper(delegator, orderId);
        // TODO: skip this if there is already a purchase order associated with the sales order (ship group)
        
        try {
            // if sales order
            if ("SALES_ORDER".equals(orh.getOrderTypeId())) {
                // get the order's ship groups
                for (GenericValue shipGroup : orh.getOrderItemShipGroups()) {
                    if (!UtilValidate.isEmpty(shipGroup.getString("supplierPartyId"))) {
                        // This ship group is a drop shipment: we create a purchase order for it
                        String supplierPartyId = shipGroup.getString("supplierPartyId");
                        // create the cart
                        ShoppingCart cart = new ShoppingCart(delegator, orh.getProductStoreId(), null, orh.getCurrency());
                        cart.setOrderType("PURCHASE_ORDER");
                        cart.setBillToCustomerPartyId(cart.getBillFromVendorPartyId()); //Company
                        cart.setBillFromVendorPartyId(supplierPartyId);
                        cart.setOrderPartyId(supplierPartyId);
                        // Get the items associated to it and create po
                        List<GenericValue> items = orh.getValidOrderItems(shipGroup.getString("shipGroupSeqId"));
                        if (!UtilValidate.isEmpty(items)) {
                            for (GenericValue item : items) {
                                try {
                                    int itemIndex = cart.addOrIncreaseItem(item.getString("productId"),
                                            null, // amount
                                            item.getBigDecimal("quantity"),
                                            null, null, null, // reserv
                                            item.getTimestamp("shipBeforeDate"),
                                            item.getTimestamp("shipAfterDate"),
                                            null, null, null,
                                            null, null, null,
                                            null, dispatcher, "Y", null);
                                    ShoppingCartItem sci = cart.findCartItem(itemIndex);
                                    sci.setAssociatedOrderId(orderId);
                                    sci.setAssociatedOrderItemSeqId(item.getString("orderItemSeqId"));
                                    sci.setOrderItemAssocTypeId("DROP_SHIPMENT");
                                } catch (Exception e) {
                                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                            "OrderOrderCreatingDropShipmentsError",
                                            UtilMisc.toMap("orderId", orderId, "errorString", e.getMessage()),
                                            locale));
                                }
                            }
                        }
                        
                        // If there are indeed items to drop ship, then create the purchase order
                        if (!UtilValidate.isEmpty(cart.items())) {
                            // set checkout options
                            cart.setDefaultCheckoutOptions(dispatcher);
                            // the shipping address is the one of the customer
                            cart.setShippingContactMechId(shipGroup.getString("contactMechId"));
                            // associate ship groups of sales and purchase orders
                            ShoppingCart.CartShipInfo cartShipInfo = cart.getShipGroups().get(0);
                            cartShipInfo.setAssociatedShipGroupSeqId(shipGroup.getString("shipGroupSeqId"));
                            // create the order
                            CheckOutHelper coh = new CheckOutHelper(dispatcher, delegator, cart);
                            coh.createOrder(null, userLogin);
                        } else {
                            // if there are no items to drop ship, then clear out the supplier partyId
                            Debug.logWarning("No drop ship items found for order [" + shipGroup.getString("orderId") + "] and ship group [" + shipGroup.getString("shipGroupSeqId") + "] and supplier party [" + shipGroup.getString("supplierPartyId") + "].  Supplier party information will be cleared for this ship group", module);
                            shipGroup.set("supplierPartyId", null);
                            shipGroup.store();
                            
                        }
                    }
                }
            }
        } catch (Exception exc) {
            // TODO: imporve error handling
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "OrderOrderCreatingDropShipmentsError",
                    UtilMisc.toMap("orderId", orderId, "errorString", exc.getMessage()),
                    locale));
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> updateOrderPaymentPreference(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        String orderPaymentPreferenceId = (String) context.get("orderPaymentPreferenceId");
        String checkOutPaymentId = (String) context.get("checkOutPaymentId");
        String statusId = (String) context.get("statusId");
        
        try {
            GenericValue opp = delegator.findByPrimaryKey("OrderPaymentPreference", UtilMisc.toMap("orderPaymentPreferenceId", orderPaymentPreferenceId));
            String paymentMethodId = null;
            String paymentMethodTypeId = null;
            
            // The checkOutPaymentId is either a paymentMethodId or paymentMethodTypeId
            // the original method did a "\d+" regexp to decide which is the case, this version is more explicit with its lookup of PaymentMethodType
            if (checkOutPaymentId != null) {
                List<GenericValue> paymentMethodTypes = delegator.findList("PaymentMethodType", null, null, null, null, true);
                for (GenericValue type : paymentMethodTypes) {
                    if (type.get("paymentMethodTypeId").equals(checkOutPaymentId)) {
                        paymentMethodTypeId = (String) type.get("paymentMethodTypeId");
                        break;
                    }
                }
                if (paymentMethodTypeId == null) {
                    GenericValue method = delegator.findByPrimaryKey("PaymentMethod", UtilMisc.toMap("paymentMethodTypeId", paymentMethodTypeId));
                    paymentMethodId = checkOutPaymentId;
                    paymentMethodTypeId = (String) method.get("paymentMethodTypeId");
                }
            }
            
            Map<String, Object> results = ServiceUtil.returnSuccess();
            if (UtilValidate.isNotEmpty(statusId) && "PAYMENT_CANCELLED".equalsIgnoreCase(statusId)) {
                opp.set("statusId", "PAYMENT_CANCELLED");
                opp.store();
                results.put("orderPaymentPreferenceId", opp.get("orderPaymentPreferenceId"));
            } else {
                GenericValue newOpp = (GenericValue) opp.clone();
                opp.set("statusId", "PAYMENT_CANCELLED");
                opp.store();
                
                newOpp.set("orderPaymentPreferenceId", delegator.getNextSeqId("OrderPaymentPreference"));
                newOpp.set("paymentMethodId", paymentMethodId);
                newOpp.set("paymentMethodTypeId", paymentMethodTypeId);
                newOpp.setNonPKFields(context);
                newOpp.create();
                results.put("orderPaymentPreferenceId", newOpp.get("orderPaymentPreferenceId"));
            }
            
            return results;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }
    
    /**
     * Generates a product requirement for the total cancelled quantity over all order items for each product
     *
     * @param dctx    the dispatch context
     * @param context the context
     * @return the result of the service execution
     */
    public static Map<String, Object> generateReqsFromCancelledPOItems(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        
        String orderId = (String) context.get("orderId");
        String facilityId = (String) context.get("facilityId");
        
        try {
            
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            
            if (UtilValidate.isEmpty(orderHeader)) {
                String errorMessage = UtilProperties.getMessage(resource_error,
                        "OrderErrorOrderIdNotFound", UtilMisc.toMap("orderId", orderId), locale);
                Debug.logError(errorMessage, module);
                return ServiceUtil.returnError(errorMessage);
            }
            
            if (!"PURCHASE_ORDER".equals(orderHeader.getString("orderTypeId"))) {
                String errorMessage = UtilProperties.getMessage(resource_error,
                        "ProductErrorOrderNotPurchaseOrder", UtilMisc.toMap("orderId", orderId), locale);
                Debug.logError(errorMessage, module);
                return ServiceUtil.returnError(errorMessage);
            }
            
            // Build a map of productId -> quantity cancelled over all order items
            Map<String, Object> productRequirementQuantities = new HashMap<String, Object>();
            List<GenericValue> orderItems = orderHeader.getRelated("OrderItem");
            for (GenericValue orderItem : orderItems) {
                if (!"PRODUCT_ORDER_ITEM".equals(orderItem.getString("orderItemTypeId"))) {
                    continue;
                }
                
                // Get the cancelled quantity for the item
                BigDecimal orderItemCancelQuantity = BigDecimal.ZERO;
                if (!UtilValidate.isEmpty(orderItem.get("cancelQuantity"))) {
                    orderItemCancelQuantity = orderItem.getBigDecimal("cancelQuantity");
                }
                
                if (orderItemCancelQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                String productId = orderItem.getString("productId");
                if (productRequirementQuantities.containsKey(productId)) {
                    orderItemCancelQuantity = orderItemCancelQuantity.add((BigDecimal) productRequirementQuantities.get(productId));
                }
                productRequirementQuantities.put(productId, orderItemCancelQuantity);
                
            }
            
            // Generate requirements for each of the product quantities
            for (String productId : productRequirementQuantities.keySet()) {
                BigDecimal requiredQuantity = (BigDecimal) productRequirementQuantities.get(productId);
                Map<String, Object> createRequirementResult = dispatcher.runSync("createRequirement", UtilMisc.<String, Object>toMap("requirementTypeId", "PRODUCT_REQUIREMENT", "facilityId", facilityId, "productId", productId, "quantity", requiredQuantity, "userLogin", userLogin));
                if (ServiceUtil.isError(createRequirementResult)) {
                    return createRequirementResult;
                }
            }
            
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException se) {
            Debug.logError(se, module);
            return ServiceUtil.returnError(se.getMessage());
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * Cancels remaining (unreceived) quantities for items of an order. Does not consider received-but-rejected quantities.
     *
     * @param dctx    the dispatch context
     * @param context the context
     * @return cancels remaining (unreceived) quantities for items of an order
     */
    public static Map<String, Object> cancelRemainingPurchaseOrderItems(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        
        String orderId = (String) context.get("orderId");
        
        try {
            
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            
            if (UtilValidate.isEmpty(orderHeader)) {
                String errorMessage = UtilProperties.getMessage(resource_error,
                        "OrderErrorOrderIdNotFound", UtilMisc.toMap("orderId", orderId), locale);
                Debug.logError(errorMessage, module);
                return ServiceUtil.returnError(errorMessage);
            }
            
            if (!"PURCHASE_ORDER".equals(orderHeader.getString("orderTypeId"))) {
                String errorMessage = UtilProperties.getMessage(resource_error,
                        "OrderErrorOrderNotPurchaseOrder", UtilMisc.toMap("orderId", orderId), locale);
                Debug.logError(errorMessage, module);
                return ServiceUtil.returnError(errorMessage);
            }
            
            List<GenericValue> orderItems = orderHeader.getRelated("OrderItem");
            for (GenericValue orderItem : orderItems) {
                if (!"PRODUCT_ORDER_ITEM".equals(orderItem.getString("orderItemTypeId"))) {
                    continue;
                }
                
                // Get the ordered quantity for the item
                BigDecimal orderItemQuantity = BigDecimal.ZERO;
                if (!UtilValidate.isEmpty(orderItem.get("quantity"))) {
                    orderItemQuantity = orderItem.getBigDecimal("quantity");
                }
                BigDecimal orderItemCancelQuantity = BigDecimal.ZERO;
                if (!UtilValidate.isEmpty(orderItem.get("cancelQuantity"))) {
                    orderItemCancelQuantity = orderItem.getBigDecimal("cancelQuantity");
                }
                
                // Get the received quantity for the order item - ignore the quantityRejected, since rejected items should be reordered
                List<GenericValue> shipmentReceipts = orderItem.getRelated("ShipmentReceipt");
                BigDecimal receivedQuantity = BigDecimal.ZERO;
                for (GenericValue shipmentReceipt : shipmentReceipts) {
                    if (!UtilValidate.isEmpty(shipmentReceipt.get("quantityAccepted"))) {
                        receivedQuantity = receivedQuantity.add(shipmentReceipt.getBigDecimal("quantityAccepted"));
                    }
                }
                
                BigDecimal quantityToCancel = orderItemQuantity.subtract(orderItemCancelQuantity).subtract(receivedQuantity);
                if (quantityToCancel.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> cancelOrderItemResult = dispatcher.runSync("cancelOrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItem.get("orderItemSeqId"), "cancelQuantity", quantityToCancel, "userLogin", userLogin));
                    if (ServiceUtil.isError(cancelOrderItemResult)) {
                        return cancelOrderItemResult;
                    }
                }
                
                // If there's nothing to cancel, the item should be set to completed, if it isn't already
                orderItem.refresh();
                if ("ITEM_APPROVED".equals(orderItem.getString("statusId"))) {
                    Map<String, Object> changeOrderItemStatusResult = dispatcher.runSync("changeOrderItemStatus", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItem.get("orderItemSeqId"), "statusId", "ITEM_COMPLETED", "userLogin", userLogin));
                    if (ServiceUtil.isError(changeOrderItemStatusResult)) {
                        return changeOrderItemStatusResult;
                    }
                }
            }
            
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException se) {
            Debug.logError(se, module);
            return ServiceUtil.returnError(se.getMessage());
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    // create simple non-product order
    public static Map<String, Object> createSimpleNonProductSalesOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        
        String paymentMethodId = (String) context.get("paymentMethodId");
        String productStoreId = (String) context.get("productStoreId");
        String currency = (String) context.get("currency");
        String partyId = (String) context.get("partyId");
        Map<String, BigDecimal> itemMap = UtilGenerics.checkMap(context.get("itemMap"));
        
        ShoppingCart cart = new ShoppingCart(delegator, productStoreId, null, locale, currency);
        try {
            cart.setUserLogin(userLogin, dispatcher);
        } catch (CartItemModifyException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        cart.setOrderType("SALES_ORDER");
        cart.setOrderPartyId(partyId);
        
        for (String item : itemMap.keySet()) {
            BigDecimal price = itemMap.get(item);
            try {
                cart.addNonProductItem("BULK_ORDER_ITEM", item, null, price, BigDecimal.ONE, null, null, null, dispatcher);
            } catch (CartItemModifyException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        
        // set the payment method
        try {
            cart.addPayment(paymentMethodId);
        } catch (IllegalArgumentException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        
        // save the order (new tx)
        Map<String, Object> createResp;
        try {
            createResp = dispatcher.runSync("createOrderFromShoppingCart", UtilMisc.toMap("shoppingCart", cart), 90, true);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        if (ServiceUtil.isError(createResp)) {
            return createResp;
        }
        
        // auth the order (new tx)
        Map<String, Object> authResp;
        try {
            authResp = dispatcher.runSync("callProcessOrderPayments", UtilMisc.toMap("shoppingCart", cart), 180, true);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        if (ServiceUtil.isError(authResp)) {
            return authResp;
        }
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("orderId", createResp.get("orderId"));
        return result;
    }
    
    // generic method for creating an order from a shopping cart
    public static Map<String, Object> createOrderFromShoppingCart(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        
        ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
        GenericValue userLogin = cart.getUserLogin();
        
        CheckOutHelper coh = new CheckOutHelper(dispatcher, delegator, cart);
        Map<String, Object> createOrder = coh.createOrder(null, userLogin);
        if (ServiceUtil.isError(createOrder)) {
            return createOrder;
        }
        String orderId = (String) createOrder.get("orderId");
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        result.put("orderId", orderId);
        return result;
    }
    
    // generic method for processing an order's payment(s)
    public static Map<String, Object> callProcessOrderPayments(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        
        Transaction trans = null;
        try {
            // disable transaction procesing
            trans = TransactionUtil.suspend();
            
            // get the cart
            ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
            GenericValue userLogin = cart.getUserLogin();
            Boolean manualHold = (Boolean) context.get("manualHold");
            if (manualHold == null) {
                manualHold = Boolean.FALSE;
            }
            
            if (!"PURCHASE_ORDER".equals(cart.getOrderType())) {
                String productStoreId = null;
                GenericValue productStore = ProductStoreWorker.getProductStore(productStoreId, delegator);
                CheckOutHelper coh = new CheckOutHelper(dispatcher, delegator, cart);
                
                // process payment
                Map<String, Object> payResp;
                try {
                    payResp = coh.processPayment(productStore, userLogin, false, manualHold.booleanValue());
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                if (ServiceUtil.isError(payResp)) {
                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                            "OrderProcessOrderPayments", locale), null, null, payResp);
                }
            }
            
            return ServiceUtil.returnSuccess();
        } catch (GenericTransactionException e) {
            return ServiceUtil.returnError(e.getMessage());
        } finally {
            // resume transaction
            try {
                TransactionUtil.resume(trans);
            } catch (GenericTransactionException e) {
                Debug.logWarning(e, e.getMessage(), module);
            }
        }
    }
    
    /**
     * Determines the total amount invoiced for a given order item over all invoices by totalling the item subtotal (via OrderItemBilling),
     * any adjustments for that item (via OrderAdjustmentBilling), and the item's share of any order-level adjustments (that calculated
     * by applying the percentage of the items total that the item represents to the order-level adjustments total (also via
     * OrderAdjustmentBilling). Also returns the quantity invoiced for the item over all invoices, to aid in prorating.
     *
     * @param dctx    DispatchContext
     * @param context Map
     * @return Map
     */
    public static Map<String, Object> getOrderItemInvoicedAmountAndQuantity(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        
        String orderId = (String) context.get("orderId");
        String orderItemSeqId = (String) context.get("orderItemSeqId");
        
        GenericValue orderHeader = null;
        GenericValue orderItemToCheck = null;
        BigDecimal orderItemTotalValue = ZERO;
        BigDecimal invoicedQuantity = ZERO; // Quantity invoiced for the target order item
        try {
            
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (UtilValidate.isEmpty(orderHeader)) {
                String errorMessage = UtilProperties.getMessage(resource_error,
                        "OrderErrorOrderIdNotFound", context, locale);
                Debug.logError(errorMessage, module);
                return ServiceUtil.returnError(errorMessage);
            }
            orderItemToCheck = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId));
            if (UtilValidate.isEmpty(orderItemToCheck)) {
                String errorMessage = UtilProperties.getMessage(resource_error,
                        "OrderErrorOrderItemNotFound", context, locale);
                Debug.logError(errorMessage, module);
                return ServiceUtil.returnError(errorMessage);
            }
            
            BigDecimal orderItemsSubtotal = ZERO; // Aggregated value of order items, non-tax and non-shipping item-level adjustments
            BigDecimal invoicedTotal = ZERO; // Amount invoiced for the target order item
            BigDecimal itemAdjustments = ZERO; // Item-level tax- and shipping-adjustments
            
            // Aggregate the order items subtotal
            List<GenericValue> orderItems = orderHeader.getRelated("OrderItem", UtilMisc.toList("orderItemSeqId"));
            for (GenericValue orderItem : orderItems) {
                // Look at the orderItemBillings to discover the amount and quantity ever invoiced for this order item
                List<GenericValue> orderItemBillings = delegator.findByAnd("OrderItemBilling", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItem.get("orderItemSeqId")));
                for (GenericValue orderItemBilling : orderItemBillings) {
                    BigDecimal quantity = orderItemBilling.getBigDecimal("quantity");
                    BigDecimal amount = orderItemBilling.getBigDecimal("amount").setScale(orderDecimals, orderRounding);
                    if (UtilValidate.isEmpty(invoicedQuantity) || UtilValidate.isEmpty(amount)) {
                        continue;
                    }
                    
                    // Add the item base amount to the subtotal
                    orderItemsSubtotal = orderItemsSubtotal.add(quantity.multiply(amount));
                    
                    // If the item is the target order item, add the invoiced quantity and amount to their respective totals
                    if (orderItemSeqId.equals(orderItem.get("orderItemSeqId"))) {
                        invoicedQuantity = invoicedQuantity.add(quantity);
                        invoicedTotal = invoicedTotal.add(quantity.multiply(amount));
                    }
                }
                
                // Retrieve the adjustments for this item
                List<GenericValue> orderAdjustments = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItem.get("orderItemSeqId")));
                for (GenericValue orderAdjustment : orderAdjustments) {
                    String orderAdjustmentTypeId = orderAdjustment.getString("orderAdjustmentTypeId");
                    
                    // Look at the orderAdjustmentBillings to discove the amount ever invoiced for this order adjustment
                    List<GenericValue> orderAdjustmentBillings = delegator.findByAnd("OrderAdjustmentBilling", UtilMisc.toMap("orderAdjustmentId", orderAdjustment.get("orderAdjustmentId")));
                    for (GenericValue orderAjustmentBilling : orderAdjustmentBillings) {
                        BigDecimal amount = orderAjustmentBilling.getBigDecimal("amount").setScale(orderDecimals, orderRounding);
                        if (UtilValidate.isEmpty(amount)) {
                            continue;
                        }
                        
                        if ("SALES_TAX".equals(orderAdjustmentTypeId) || "SHIPPING_CHARGES".equals(orderAdjustmentTypeId)) {
                            if (orderItemSeqId.equals(orderItem.get("orderItemSeqId"))) {
                                
                                // Add tax- and shipping-adjustment amounts to the total adjustments for the target order item
                                itemAdjustments = itemAdjustments.add(amount);
                            }
                        } else {
                            
                            // Add non-tax and non-shipping adjustment amounts to the order items subtotal
                            orderItemsSubtotal = orderItemsSubtotal.add(amount);
                            if (orderItemSeqId.equals(orderItem.get("orderItemSeqId"))) {
                                
                                // If the item is the target order item, add non-tax and non-shipping adjustment amounts to the invoiced total
                                invoicedTotal = invoicedTotal.add(amount);
                            }
                        }
                    }
                }
            }
            
            // Total the order-header-level adjustments for the order
            BigDecimal orderHeaderAdjustmentsTotalValue = ZERO;
            List<GenericValue> orderHeaderAdjustments = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", "_NA_"));
            for (GenericValue orderHeaderAdjustment : orderHeaderAdjustments) {
                List<GenericValue> orderHeaderAdjustmentBillings = delegator.findByAnd("OrderAdjustmentBilling", UtilMisc.toMap("orderAdjustmentId", orderHeaderAdjustment.get("orderAdjustmentId")));
                for (GenericValue orderHeaderAdjustmentBilling : orderHeaderAdjustmentBillings) {
                    BigDecimal amount = orderHeaderAdjustmentBilling.getBigDecimal("amount").setScale(orderDecimals, orderRounding);
                    if (UtilValidate.isEmpty(amount)) {
                        continue;
                    }
                    orderHeaderAdjustmentsTotalValue = orderHeaderAdjustmentsTotalValue.add(amount);
                }
            }
            
            // How much of the order-level adjustments total does the target order item represent? The assumption is: the same
            //  proportion of the adjustments as of the invoiced total for the item to the invoiced total for all items. These
            //  figures don't take tax- and shipping- adjustments into account, so as to be in accordance with the code in InvoiceServices
            BigDecimal invoicedAmountProportion = ZERO;
            if (orderItemsSubtotal.signum() != 0) {
                invoicedAmountProportion = invoicedTotal.divide(orderItemsSubtotal, 5, orderRounding);
            }
            BigDecimal orderItemHeaderAjustmentAmount = orderHeaderAdjustmentsTotalValue.multiply(invoicedAmountProportion);
            orderItemTotalValue = invoicedTotal.add(orderItemHeaderAjustmentAmount);
            
            // Add back the tax- and shipping- item-level adjustments for the order item
            orderItemTotalValue = orderItemTotalValue.add(itemAdjustments);
            
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("invoicedAmount", orderItemTotalValue.setScale(orderDecimals, orderRounding));
        result.put("invoicedQuantity", invoicedQuantity.setScale(orderDecimals, orderRounding));
        return result;
    }
    
    public static Map<String, Object> setOrderPaymentStatus(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        String orderPaymentPreferenceId = (String) context.get("orderPaymentPreferenceId");
        String changeReason = (String) context.get("changeReason");
        Locale locale = (Locale) context.get("locale");
        try {
            GenericValue orderPaymentPreference = delegator.findByPrimaryKey("OrderPaymentPreference", UtilMisc.toMap("orderPaymentPreferenceId", orderPaymentPreferenceId));
            String orderId = orderPaymentPreference.getString("orderId");
            String statusUserLogin = orderPaymentPreference.getString("createdByUserLogin");
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (orderHeader == null) {
                return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                        "OrderErrorCouldNotChangeOrderStatusOrderCannotBeFound", locale));
            }
            String statusId = orderPaymentPreference.getString("statusId");
            if (Debug.verboseOn()) {
                Debug.logVerbose("[OrderServices.setOrderPaymentStatus] : Setting Order Payment Status to : " + statusId, module);
            }
            // create a order payment status
            GenericValue orderStatus = delegator.makeValue("OrderStatus");
            orderStatus.put("statusId", statusId);
            orderStatus.put("orderId", orderId);
            orderStatus.put("orderPaymentPreferenceId", orderPaymentPreferenceId);
            orderStatus.put("statusUserLogin", statusUserLogin);
            orderStatus.put("changeReason", changeReason);
            
            // Check that the status has actually changed before creating a new record
            List<GenericValue> previousStatusList = delegator.findByAnd("OrderStatus", UtilMisc.toMap("orderId", orderId, "orderPaymentPreferenceId", orderPaymentPreferenceId), UtilMisc.toList("-statusDatetime"));
            GenericValue previousStatus = EntityUtil.getFirst(previousStatusList);
            if (previousStatus != null) {
                // Temporarily set some values on the new status so that we can do an equals() check
                orderStatus.put("orderStatusId", previousStatus.get("orderStatusId"));
                orderStatus.put("statusDatetime", previousStatus.get("statusDatetime"));
                if (orderStatus.equals(previousStatus)) {
                    // Status is the same, return without creating
                    return ServiceUtil.returnSuccess();
                }
            }
            orderStatus.put("orderStatusId", delegator.getNextSeqId("OrderStatus"));
            orderStatus.put("statusDatetime", UtilDateTime.nowTimestamp());
            orderStatus.create();
            
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    "OrderErrorCouldNotChangeOrderStatus", locale) + e.getMessage() + ").");
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> runSubscriptionAutoReorders(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        int count = 0;
        Map<String, Object> result = null;
        
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
            
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("automaticExtend", EntityOperator.EQUALS, "Y"),
                    EntityCondition.makeCondition("orderId", EntityOperator.NOT_EQUAL, null),
                    EntityCondition.makeCondition("productId", EntityOperator.NOT_EQUAL, null));
            EntityCondition cond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
            EntityListIterator eli = null;
            eli = delegator.find("Subscription", cond, null, null, null, null);
            
            if (eli != null) {
                GenericValue subscription;
                while (((subscription = eli.next()) != null)) {
                    
                    Calendar endDate = Calendar.getInstance();
                    endDate.setTime(UtilDateTime.nowTimestamp());
                    //check if the thruedate - cancel period (if provided) is earlier than todays date
                    int field = Calendar.MONTH;
                    if (subscription.get("canclAutmExtTime") != null && subscription.get("canclAutmExtTimeUomId") != null) {
                        if ("TF_day".equals(subscription.getString("canclAutmExtTimeUomId"))) {
                            field = Calendar.DAY_OF_YEAR;
                        } else if ("TF_wk".equals(subscription.getString("canclAutmExtTimeUomId"))) {
                            field = Calendar.WEEK_OF_YEAR;
                        } else if ("TF_mon".equals(subscription.getString("canclAutmExtTimeUomId"))) {
                            field = Calendar.MONTH;
                        } else if ("TF_yr".equals(subscription.getString("canclAutmExtTimeUomId"))) {
                            field = Calendar.YEAR;
                        } else {
                            Debug.logWarning("Don't know anything about useTimeUomId [" + subscription.getString("canclAutmExtTimeUomId") + "], defaulting to month", module);
                        }
                        
                        endDate.add(field, Integer.valueOf(subscription.getString("canclAutmExtTime")).intValue());
                    }
                    
                    Calendar endDateSubscription = Calendar.getInstance();
                    endDateSubscription.setTime(subscription.getTimestamp("thruDate"));
                    
                    if (endDate.before(endDateSubscription)) {
                        // nor expired yet.....
                        continue;
                    }
                    
                    result = dispatcher.runSync("loadCartFromOrder", UtilMisc.toMap("orderId", subscription.get("orderId"), "userLogin", userLogin));
                    ShoppingCart cart = (ShoppingCart) result.get("shoppingCart");
                    
                    // remove former orderId from cart (would cause duplicate entry).
                    // orderId is set by order-creation services (including store-specific prefixes, e.g.)
                    cart.setOrderId(null);
                    
                    // only keep the orderitem with the related product.
                    List<ShoppingCartItem> cartItems = cart.items();
                    for (ShoppingCartItem shoppingCartItem : cartItems) {
                        if (!subscription.get("productId").equals(shoppingCartItem.getProductId())) {
                            cart.removeCartItem(shoppingCartItem, dispatcher);
                        }
                    }
                    
                    CheckOutHelper helper = new CheckOutHelper(dispatcher, delegator, cart);
                    
                    // store the order
                    Map<String, Object> createResp = helper.createOrder(null, userLogin);
                    if (createResp != null && ServiceUtil.isError(createResp)) {
                        Debug.logError("Cannot create order for shopping list - " + subscription, module);
                    } else {
                        String orderId = (String) createResp.get("orderId");
                        
                        // authorize the payments
                        Map<String, Object> payRes = null;
                        try {
                            payRes = helper.processPayment(ProductStoreWorker.getProductStore(null, delegator), userLogin);
                        } catch (GeneralException e) {
                            Debug.logError(e, module);
                        }
                        
                        if (payRes != null && ServiceUtil.isError(payRes)) {
                            Debug.logError("Payment processing problems with shopping list - " + subscription, module);
                        }
                        
                        // remove the automatic extension flag
                        subscription.put("automaticExtend", "N");
                        subscription.store();
                        
                        // send notification
                        dispatcher.runAsync("sendOrderPayRetryNotification", UtilMisc.toMap("orderId", orderId));
                        count++;
                    }
                }
                eli.close();
            }
            
        } catch (GenericServiceException e) {
            Debug.logError("Could call service to create cart", module);
            return ServiceUtil.returnError(e.toString());
        } catch (CartItemModifyException e) {
            Debug.logError("Could not modify cart: " + e.toString(), module);
            return ServiceUtil.returnError(e.toString());
        } catch (GenericEntityException e) {
            try {
                // only rollback the transaction if we started one...
                TransactionUtil.rollback(beganTransaction, "Error creating subscription auto-reorders", e);
            } catch (GenericEntityException e2) {
                Debug.logError(e2, "[Delegator] Could not rollback transaction: " + e2.toString(), module);
            }
            Debug.logError(e, "Error while creating new shopping list based automatic reorder" + e.toString(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "OrderShoppingListCreationError", UtilMisc.toMap("errorString", e.toString()), locale));
        } finally {
            try {
                // only commit the transaction if we started one... this will throw an exception if it fails
                TransactionUtil.commit(beganTransaction);
            } catch (GenericEntityException e) {
                Debug.logError(e, "Could not commit transaction for creating new shopping list based automatic reorder", module);
            }
        }
        return ServiceUtil.returnSuccess(UtilProperties.getMessage(resource,
                "OrderRunSubscriptionAutoReorders", UtilMisc.toMap("count", count), locale));
    }
    
    public static Map<String, Object> setShippingInstructions(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        String shippingInstructions = (String) context.get("shippingInstructions");
        try {
            GenericValue orderItemShipGroup = EntityUtil.getFirst(delegator.findByAnd("OrderItemShipGroup", UtilMisc.toMap("orderId", orderId, "shipGroupSeqId", shipGroupSeqId)));
            orderItemShipGroup.set("shippingInstructions", shippingInstructions);
            orderItemShipGroup.store();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> setGiftMessage(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String shipGroupSeqId = (String) context.get("shipGroupSeqId");
        String giftMessage = (String) context.get("giftMessage");
        try {
            GenericValue orderItemShipGroup = EntityUtil.getFirst(delegator.findByAnd("OrderItemShipGroup", UtilMisc.toMap("orderId", orderId, "shipGroupSeqId", shipGroupSeqId)));
            orderItemShipGroup.set("giftMessage", giftMessage);
            orderItemShipGroup.set("isGift", "Y");
            orderItemShipGroup.store();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> createAlsoBoughtProductAssocs(DispatchContext dctx, Map<String, ? extends Object> context) {
        final Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // All orders with an entryDate > orderEntryFromDateTime will be processed
        Timestamp orderEntryFromDateTime = (Timestamp) context.get("orderEntryFromDateTime");
        // If true all orders ever created will be processed and any pre-existing ALSO_BOUGHT ProductAssocs will be expired
        boolean processAllOrders = context.get("processAllOrders") == null ? false : (Boolean) context.get("processAllOrders");
        if (orderEntryFromDateTime == null && !processAllOrders) {
            // No from date supplied, check to see when this service last ran and use the startDateTime
            EntityCondition cond = EntityCondition.makeCondition(UtilMisc.toMap("statusId", "SERVICE_FINISHED", "serviceName", "createAlsoBoughtProductAssocs"));
            EntityFindOptions efo = new EntityFindOptions();
            efo.setMaxRows(1);
            try {
                GenericValue lastRunJobSandbox = EntityUtil.getFirst(delegator.findList("JobSandbox", cond, null, UtilMisc.toList("startDateTime DESC"), efo, false));
                if (lastRunJobSandbox != null) {
                    orderEntryFromDateTime = lastRunJobSandbox.getTimestamp("startDateTime");
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
            if (orderEntryFromDateTime == null) {
                // Still null, process all orders
                processAllOrders = true;
            }
        }
        if (processAllOrders) {
            // Expire any pre-existing ALSO_BOUGHT ProductAssocs in preparation for reprocessing
            EntityCondition cond = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition("productAssocTypeId", "ALSO_BOUGHT"),
                    EntityCondition.makeConditionDate("fromDate", "thruDate")
            ));
            try {
                delegator.storeByCondition("ProductAssoc", UtilMisc.toMap("thruDate", UtilDateTime.nowTimestamp()), cond);
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        List<EntityExpr> orderCondList = UtilMisc.toList(EntityCondition.makeCondition("orderTypeId", "SALES_ORDER"));
        if (!processAllOrders && orderEntryFromDateTime != null) {
            orderCondList.add(EntityCondition.makeCondition("entryDate", EntityOperator.GREATER_THAN, orderEntryFromDateTime));
        }
        final EntityCondition cond = EntityCondition.makeCondition(orderCondList);
        List<String> orderIds;
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
            orderIds = TransactionUtil.doNewTransaction(new Callable<List<String>>() {
                public List<String> call() throws Exception {
                    List<String> orderIds = new LinkedList<String>();
                    EntityListIterator eli = null;
                    try {
                        eli = delegator.find("OrderHeader", cond, null, UtilMisc.toSet("orderId"), UtilMisc.toList("entryDate ASC"), null);
                        GenericValue orderHeader;
                        while ((orderHeader = eli.next()) != null) {
                            orderIds.add(orderHeader.getString("orderId"));
                        }
                    } finally {
                        if (eli != null) {
                            eli.close();
                        }
                    }
                    return orderIds;
                }
            }, "getSalesOrderIds", 0, true);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (GenericTransactionException e2) {
                Debug.logError(e2, "Unable to rollback transaction", module);
            }
            return ServiceUtil.returnError(e.getMessage());
        } finally {
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (GenericTransactionException e) {
                Debug.logError(e, "Unable to commit transaction", module);
            }
        }
        
        for (String orderId : orderIds) {
            Map<String, Object> svcIn = FastMap.newInstance();
            svcIn.put("userLogin", context.get("userLogin"));
            svcIn.put("orderId", orderId);
            try {
                dispatcher.runSync("createAlsoBoughtProductAssocsForOrder", svcIn);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
            }
        }
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> createAlsoBoughtProductAssocsForOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        OrderReadHelper orh = new OrderReadHelper(delegator, orderId);
        List<GenericValue> orderItems = orh.getOrderItems();
        // In order to improve efficiency a little bit, we will always create the ProductAssoc records
        // with productId < productIdTo when the two are compared.  This way when checking for an existing
        // record we don't have to check both possible combinations of productIds
        TreeSet<String> productIdSet = new TreeSet<String>();
        if (orderItems != null) {
            for (GenericValue orderItem : orderItems) {
                String productId = orderItem.getString("productId");
                if (productId != null) {
                    GenericValue parentProduct = ProductWorker.getParentProduct(productId, delegator);
                    if (parentProduct != null) {
                        productId = parentProduct.getString("productId");
                    }
                    productIdSet.add(productId);
                }
            }
        }
        TreeSet<String> productIdToSet = new TreeSet<String>(productIdSet);
        for (String productId : productIdSet) {
            productIdToSet.remove(productId);
            for (String productIdTo : productIdToSet) {
                EntityCondition cond = EntityCondition.makeCondition(
                        UtilMisc.toList(
                                EntityCondition.makeCondition("productId", productId),
                                EntityCondition.makeCondition("productIdTo", productIdTo),
                                EntityCondition.makeCondition("productAssocTypeId", "ALSO_BOUGHT"),
                                EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()),
                                EntityCondition.makeCondition("thruDate", null)
                        )
                );
                GenericValue existingProductAssoc = null;
                try {
                    // No point in using the cache because of the filterByDateExpr
                    existingProductAssoc = EntityUtil.getFirst(delegator.findList("ProductAssoc", cond, null, UtilMisc.toList("fromDate DESC"), null, false));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
                try {
                    if (existingProductAssoc != null) {
                        BigDecimal newQuantity = existingProductAssoc.getBigDecimal("quantity");
                        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                            newQuantity = BigDecimal.ZERO;
                        }
                        newQuantity = newQuantity.add(BigDecimal.ONE);
                        ModelService updateProductAssoc = dctx.getModelService("updateProductAssoc");
                        Map<String, Object> updateCtx = updateProductAssoc.makeValid(context, ModelService.IN_PARAM, true, null);
                        updateCtx.putAll(updateProductAssoc.makeValid(existingProductAssoc, ModelService.IN_PARAM));
                        updateCtx.put("quantity", newQuantity);
                        dispatcher.runSync("updateProductAssoc", updateCtx);
                    } else {
                        Map<String, Object> createCtx = FastMap.newInstance();
                        createCtx.put("userLogin", context.get("userLogin"));
                        createCtx.put("productId", productId);
                        createCtx.put("productIdTo", productIdTo);
                        createCtx.put("productAssocTypeId", "ALSO_BOUGHT");
                        createCtx.put("fromDate", UtilDateTime.nowTimestamp());
                        createCtx.put("quantity", BigDecimal.ONE);
                        dispatcher.runSync("createProductAssoc", createCtx);
                    }
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                }
            }
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    /**
     * 系统时间戳 add by gss
     *
     * @return
     */
    public static String serverTime(HttpServletRequest request, HttpServletResponse response) {
        /** 返回结果集 */
        Map<String, Object> resultData = FastMap.newInstance();
        resultData.put("serverTime", System.currentTimeMillis() / 1000);
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    /**
     * 同步用户信息 add by Wcy
     *
     * @return
     */
    public static String userOrderInfo(HttpServletRequest request, HttpServletResponse response) {
        /** 获取托管 */
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        /** 获取参数 */
        String userLoginId = (String) request.getParameter("userLoginId");
        String token = (String) request.getParameter("token");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            List<GenericValue> communitys = delegator.findByAnd("Community", (Object[]) null);
            List communityServerList = new ArrayList();
            if (UtilValidate.isNotEmpty(communitys)) {
                for (GenericValue community : communitys) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", community.get("communityId"));
                    jsonObject.put("name", community.get("name"));
                    communityServerList.add(jsonObject);
                }
                request.setAttribute("communityServerList", communityServerList);
            }
            if (null != userLogin) {
                Map<String, Object> paramContext = FastMap.newInstance();
                paramContext.put("token", token);
                paramContext.put("phone", userLoginId);
                paramContext.put("sign", "12345");
                try {
                    Map<String, Object> Result = dispatcher.runSync("loginSync", paramContext);
                    String status = (String) Result.get("status");
                    if ("false".equals(status)) {
                        request.setAttribute("info", Result.get("info"));
                        return "success";
                    }
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                }
                Map<String, Object> addressList = getPartyPostalAddresses(request, response);
                if (null != addressList) {
                    resultData.put("defaultAddressId", addressList.get("defaultId"));
                    resultData.put("addressList", addressList.get("list"));
                }
                //TODO 需改动，直接在登录方法处获取默认密码。不需传递
                String defaultPassword = UtilProperties.getPropertyValue("member.properties", "defaultPassword");
                // resultData.put("serverTime", UtilDateTime.nowDateString("yyyyMMddHHmmss"));
                request.setAttribute("resultData", resultData);
                request.setAttribute("USERNAME", userLoginId);
                request.setAttribute("PASSWORD", defaultPassword);
                return "success";
            } else {
                Map<String, Object> paramContext = FastMap.newInstance();
                paramContext.put("token", token);
                paramContext.put("phone", userLoginId);
                paramContext.put("sign", "12345");
                try {
                    Map<String, Object> Result = dispatcher.runSync("loginSync", paramContext);
                    String status = (String) Result.get("status");
                    if ("false".equals(status)) {
                        request.setAttribute("info", Result.get("info"));
                        return "success";
                    }
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                }
                Map<String, Object> addressList = getPartyPostalAddresses(request, response);
                if (null != addressList) {
                    resultData.put("defaultAddressId", addressList.get("defaultId"));
                    resultData.put("addressList", addressList.get("list"));
                }
                //TODO 需改动，直接在登录方法处获取默认密码。不需传递
                String defaultPassword = UtilProperties.getPropertyValue("member.properties", "defaultPassword");
                // resultData.put("serverTime", UtilDateTime.nowDateString("yyyyMMddHHmmss"));
                request.setAttribute("resultData", resultData);
                request.setAttribute("USERNAME", userLoginId);
                request.setAttribute("PASSWORD", defaultPassword);
                return "success";
            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    /**
     * Add by Wcy 用户订单信息 at 2016.01.11
     * 返回：待使用订单数量、待收货订单数量、待评价订单数量、退款/售后订单数量
     * 待使用订单数量：统计该用户下的【虚拟商品】的订单，再筛选存在【“未使用”】券的订单
     * 待收货：统计该用户下的【实物商品】的订单，再筛选【未确认收货】的订单
     * 待评价：统计该用户所有已确认收货的订单以及存在“已使用”商品的订单，再从中筛选没有进行过评价的订单
     * 退款/售后：统计该用户下已发起退款/退货，且退款/退货未完成的所有订单
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> userOrderNum(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        Delegator delegator = dctx.getDelegator();
        /** 待使用订单数量 */
        Map<String, Object> notUsedNumResult = notUsedNum(dctx, context);
        /** 待收货订单数量 */
        Map<String, Object> notReceivedResult = notReceivedNum(dctx, context);
        /** 待评价订单数量 */
        Map<String, Object> notReviewedResult = notReviewedNum(dctx, context);
        /** 获取登录信息 */
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (null != userLogin) {
                String defaultPassword = UtilProperties.getPropertyValue("member.properties", "defaultPassword");
                result.put("userLoginId", userLoginId);
                result.put("notUsedNum", notUsedNumResult.get("notUsedNum")); //待使用订单数量
                result.put("notReceivedNum", notReceivedResult.get("notReceivedNum")); //待收货订单数量
                result.put("notReviewedNum", notReviewedResult.get("notReviewedNum")); //待评价订单数量
                return result;
            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        
        return result;
    }
    
    
    /**
     * 待使用订单数量：统计该用户下的【虚拟商品】的订单，再筛选存在【“未使用”】券的订单
     * add by wcy
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> notUsedNum(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 定义查询字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        
        /** 获取会员信息 */
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("notUsedNum", 0L); //待使用订单数量
            return result;
        }
        
        if (null == userLogin) {
            result.put("notUsedNum", 0L); //待使用订单数量
            return result;
        }
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        /*add by gss*/
        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
        //会员ID
        dynamicViewEntity.addAlias("ORE", "partyId");
        //角色类型ID
        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        dynamicViewEntity.addAlias("OH", "orderId", "orderId", null, true, null, null);
        
        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
        
        /** 定义商品动态视图 */
        dynamicViewEntity.addMemberEntity("PR", "Product");
        dynamicViewEntity.addAlias("PR", "productId");
        dynamicViewEntity.addAlias("PR", "productTypeId");
        
        /** 定义生活券动态视图*/
        dynamicViewEntity.addMemberEntity("PGN", "Ticket");
        dynamicViewEntity.addAlias("PGN", "orderId");
        dynamicViewEntity.addAlias("PGN", "ticketStatus");
        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
        dynamicViewEntity.addViewLink("OI", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));
        dynamicViewEntity.addViewLink("OI", "PGN", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
        /**orderOrle 与orderItem*/
        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        /** 查询字段 */
        fieldsToSelect.add("orderId");
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("notUsedNum", 0L); //待使用订单数量
            return result;
        }
        /** 定义查询条件集合 */
        EntityCondition mainCond = EntityCondition.makeCondition(
                UtilMisc.toList(
                        /*orderRole*/
                        EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
                        EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"),
                        
                        EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "VIRTUAL_GOOD"),
                        EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, "notUsed"), EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, "rejectApplication")), EntityOperator.OR)
                )
                , EntityOperator.AND);
        
        /** 获取结果集 */
        List<GenericValue> resultList = null;
        try {
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
            //获取结果集
            resultList = pli.getCompleteList();
            pli.close();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        
        /** 结果集判断 */
        if (null != resultList && resultList.size() > 0) {
            Long notUsedNum = Long.valueOf(resultList.size());
            result.put("notUsedNum", notUsedNum); //待使用订单数量
        } else {
            result.put("notUsedNum", 0L); //待使用订单数量
        }
        return result;
    }
    
    /**
     * 统计该用户下的状态为“待发货”与“待收货”的实物商品订单数量
     * add by wcy
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> notReceivedNum(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 定义查询字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        
        /** 获取会员信息 */
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("notReceivedNum", 0L); //待收货订单数量
            return result;
        }
        if (null == userLogin) {
            result.put("notReceivedNum", 0L); //待收货订单数量
            return result;
        }
        
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        /*add by gss*/
        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
        //会员ID
        dynamicViewEntity.addAlias("ORE", "partyId");
        //角色类型ID
        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        dynamicViewEntity.addAlias("OH", "statusId");
        dynamicViewEntity.addAlias("OH", "countOrderNum", "orderId", null, null, null, "count");
        
        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
        
        /** 定义商品动态视图 */
        dynamicViewEntity.addMemberEntity("PR", "Product");
        dynamicViewEntity.addAlias("PR", "productId");
        dynamicViewEntity.addAlias("PR", "productTypeId");
        
        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
        dynamicViewEntity.addViewLink("OI", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));
        /**orderOrle 与orderItem*/
        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        /** 查询字段 */
        fieldsToSelect.add("countOrderNum");
        
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("notReceivedNum", 0L); //待收货订单数量
            return result;
        }
        
        /** 定义查询条件集合 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        /*orderRole*/
        andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
        andExprs.add(EntityCondition.makeCondition(
                EntityCondition.makeCondition(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITSHIP")),
                EntityOperator.OR,
                EntityCondition.makeCondition(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITRECEIVE")))
        );
        
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        /** 获取结果集 */
        List<GenericValue> resultList = null;
        try {
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
            //获取结果集
            resultList = pli.getCompleteList();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        
        /** 结果集判断 */
        if (null != resultList && resultList.size() > 0) {
            Long notUsedNum = UtilValidate.isEmpty(resultList.get(0).getLong("countOrderNum")) ? 0L : resultList.get(0).getLong("countOrderNum");
            result.put("notReceivedNum", notUsedNum); //待收货订单数量
        } else {
            result.put("notReceivedNum", 0L); //待收货订单数量
        }
        return result;
    }
    
    /**
     * 待评价数据
     * 统计该用户状态为“待评价”的实物订单，以及存在“已使用”券但未评价的虚拟订单数量之和
     * add by wcy
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> notReviewedNum(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 定义查询字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        
        /** 获取会员信息 */
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("notReviewedNum", 0L); //待评价订单数量
            return result;
        }
        if (null == userLogin) {
            result.put("notReviewedNum", 0L); //待评价订单数量
            return result;
        }
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("notReviewedNum", 0L); //待评价订单数量
            return result;
        }
        
        
        /** sql 语句*/
        //条件1：已确认收货的订单
        //条件2：已使用的订单
        //条件3：未评价的订单
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        
        String sql = "select * " +
                "  from(" +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,c.product_id as productId " +
                "  from order_header a, order_item b, product c, ticket d,order_role r" +
                " where a.order_id= b.order_id" +
                "   and b.product_id= c.product_id " +
                "   and c.product_id= d.product_id " +
                "   and b.order_id= d.order_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and a.status_id= 'ORDER_WAITEVALUATE' " +
                "   and c.product_type_id= 'VIRTUAL_GOOD' " +
                "   and d.ticket_status='hasUsed' " +
                " union all " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,c.product_id as productId" +
                "  from order_header a, order_item b, product c,order_role r " +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and a.status_id= 'ORDER_WAITEVALUATE' " +
                "   and c.product_type_id= 'FINISHED_GOOD') as temptb " +
                " order by orderDate desc,productType ";
       /* String sql = "select * " +
                "  from(" +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl " +
                "  from order_header a, order_item b, product c, ticket d,order_role r" +
                " where a.order_id= b.order_id" +
                "   and b.product_id= c.product_id " +
                "   and c.product_id= d.product_id " +
                "   and b.order_id= d.order_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and a.created_by= '" + partyId + "' " +
                "   and a.status_id= 'ORDER_WAITEVALUATE' " +
                "   and c.product_type_id= 'VIRTUAL_GOOD' " +
                "   and d.ticket_status='hasUsed' " +
                " union all " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl " +
                "  from order_header a, order_item b, product c ,order_role r" +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and a.status_id= 'ORDER_WAITEVALUATE' " +
                "   and c.product_type_id= 'FINISHED_GOOD') as temptb " +
                " order by orderDate,productType ";*/
        String countSql = "select count(*) as number from (" + sql + ") as tb";
        
        Long number = 0L;
        try {
            sqlP.prepareStatement(countSql);
            ResultSet rs = sqlP.executeQuery();
            while (rs.next()) {
                number = rs.getLong("number");
            }
            rs.close();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        result.put("notReviewedNum", number);
        return result;
    }
    
    /**
     * 统计该用户下已发起退款/退货，且退款/退货未完成的所有订单
     * add by wcy
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> refundNum(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("refundNum", 0L); //售后/评价订单数量
            return result;
        }
        
        if (null == userLogin) {
            result.put("refundNum", 0L); //售后/评价订单数量
            return result;
        }
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("refundNum", 0L); //待评价订单数量
            return result;
        }
        
        /** sql 语句*/
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        String sql = "select *  " +
                "  from( " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_id as productId, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,null as returnType,ticket_status as ticketStatus " +
                "  from order_header a, order_item b, product c, ticket d,order_role r " +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and c.product_id= d.product_id " +
                "   and b.order_id= d.order_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and c.product_type_id= 'VIRTUAL_GOOD' " +
                "   and (d.ticket_status='notAudited' or d.ticket_status='notRefunded' or d.ticket_status='hasRefuned' or d.ticket_status='rejectApplication') " +
                " union all " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_id as productId, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,return_header_type_id as returnType,null as ticketStatus " +
                "  from order_header a, order_item b, product c,return_header d,return_item e ,order_role r" +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and c.product_type_id= 'FINISHED_GOOD' " +
//                "   and d.from_party_id = '" + partyId + "' " +
                "   and e.return_id = d.return_id " +
                "   and e.order_id = a.order_id  " +
                ") as temptb " +
                " order by orderDate desc,ticketStatus ";
        
        String countSql = "select count(*) as number from (" + sql + ") as tb";
        Long number = 0L;
        try {
            sqlP.prepareStatement(countSql);
            ResultSet rs = sqlP.executeQuery();
            while (rs.next()) {
                number = rs.getLong("number");
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            number = 0L;
        } catch (SQLException e) {
            Debug.logError(e, module);
            number = 0L;
        }
        result.put("refundNum", number);
        return result;
    }
    
    /**
     * 根据订单状态查询所有订单列表  add by wcy
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findAllOrderList(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 定义查询字段 & 排序字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        /*add by gss*/
        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
        //会员ID
        dynamicViewEntity.addAlias("ORE", "partyId");
        //角色类型ID
        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        
        dynamicViewEntity.addAlias("OH", "orderId");
        dynamicViewEntity.addAlias("OH", "orderStatus", "statusId", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "createdBy");
        dynamicViewEntity.addAlias("OH", "total", "grandTotal", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "orderDate");
        dynamicViewEntity.addAlias("OH", "distributionMethod");
        
        
        /** 定义订单元素动态视图 */
        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
        dynamicViewEntity.addAlias("OI", "number", "quantity", null, null, null, null);
        
        /** 定义产品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        dynamicViewEntity.addAlias("PD", "productName");
        dynamicViewEntity.addAlias("PD", "productType", "productTypeId", null, null, null, null);
        dynamicViewEntity.addAlias("PD", "imgUrl", "mediumImageUrl", null, null, null, null);
        
        /** 定义表的关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        dynamicViewEntity.addViewLink("OI", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        /**orderOrle 与orderItem*/
        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        /** 查询字段 & 排序字段 */
        fieldsToSelect.add("orderId");//订单编号
        fieldsToSelect.add("productId");//产品编号
        fieldsToSelect.add("orderStatus");//订单状态
        fieldsToSelect.add("productName");//产品名称
        fieldsToSelect.add("orderDate");//下单日期
        fieldsToSelect.add("productType");//产品类型[FINISHED_GOOD:实物产品,VIRTUAL_GOOD:虚拟产品]
        fieldsToSelect.add("imgUrl");//产品小图
        fieldsToSelect.add("number");   //下单数量
        fieldsToSelect.add("total");//订单总金额
        fieldsToSelect.add("distributionMethod");//订单配送方式
        
        
        /** 按照下单时间倒序排序，时间重复的实物订单优先放到虚拟订单上方 */
        orderBy.add("-orderDate");
        orderBy.add("productType");
        
        /** 定义查询条件集合 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        /** 查询当前用户 */
        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        andExprs.add(EntityCondition.makeCondition("orderStatus", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"));//取消订单不查询
        andExprs.add(EntityCondition.makeCondition("orderStatus", EntityOperator.NOT_EQUAL, "ORDER_WAITPAY"));  //待支付订单不查询
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        /** 查询结束条数*/
        int highIndex = 0;
        /** 查询结果集*/
        List<GenericValue> orderList = null;
        int orderListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
//            lowIndex = viewIndex * viewSize + 1;
//            highIndex = (viewIndex + 1) * viewSize;
            lowIndex = viewIndex + 1;
            highIndex = viewIndex + viewSize;
            
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            findOpts.setDistinct(true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取分页结果集
            orderList = pli.getPartialList(lowIndex, viewSize);
            //获取记录条数
            orderListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > orderListSize) {
                highIndex = orderListSize;
            }
            
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        
        if (null == orderList) {
            orderList = FastList.newInstance();
        }
        List<Map<String, Object>> jsonArray = null;
        try {
            jsonArray = changeOrderListFormGenericValue(delegator, orderList, timeZone, locale, dispatcher);
//            System.out.println(jsonArray);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        /** 订单列表 */
        //List<Map> orderList = FastList.newInstance();
        //Map<String,Object> orderMap = FastMap.newInstance();
        //orderMap.put("orderId","WSOD100000"); //订单号
        //orderMap.put("productName","yabiz商城");//产品名称
        //orderMap.put("productType","虚拟");//产品类型
        //orderMap.put("orderStatus","待收货");//订单状态： 待使用、待收货、待评价、待退款、退款中、已退款
        //orderMap.put("imgUrl", "/image/product/manage/10000/first.png");//产品图片地址
        //orderMap.put("validityDate","2016-02-01"); //过期日期
        //orderMap.put("number",12);//下单数量
        //orderMap.put("total",BigDecimal.TEN); //订单总金额
        //orderMap.put("rLiftButton",true);//是否有“yabiz商城券”按钮
        //orderMap.put("logisticsButton",true);//是否有“查看物流”按钮
        //orderMap.put("receiveButton",true);//是否有“确认收货”按钮
        //orderMap.put("evaluateButton",true); //是否有“去评价”按钮
        //orderMap.put("refundButton",true); //是否有“退款详情”按钮
        //
        //Map<String, Object> orderMap1 = FastMap.newInstance();
        //orderMap1.put("orderId", "WSOD100001"); //订单号
        //orderMap1.put("productName", "yabiz商城");//产品名称
        //orderMap1.put("productType", "虚拟");//产品类型
        //orderMap1.put("orderStatus", "待收货");//订单状态： 待使用、待收货、待评价、待退款、退款中、已退款
        //orderMap1.put("imgUrl", "/image/product/manage/10000/first.png");//产品图片地址
        //orderMap1.put("validityDate", "2016-02-01"); //过期日期
        //orderMap1.put("number", 12);//下单数量
        //orderMap1.put("total", BigDecimal.TEN); //订单总金额
        //orderMap1.put("rLiftButton", true);//是否有“yabiz商城券”按钮
        //orderMap1.put("logisticsButton", true);//是否有“查看物流”按钮
        //orderMap1.put("receiveButton", true);//是否有“确认收货”按钮
        //orderMap1.put("evaluateButton", true); //是否有“去评价”按钮
        //orderMap1.put("refundButton", true); //是否有“退款详情”按钮
        //orderList.add(orderMap);
        //orderList.add(orderMap1);
        
        resultData.put("max", orderListSize);
        resultData.put("orderList", jsonArray);
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 结果集转换为List  add by wcy
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> changeOrderListFormGenericValue(Delegator delegator, List<GenericValue> valueList, TimeZone timeZone, Locale locale, LocalDispatcher dispatcher) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, String> productType = FastMap.newInstance();
        List<Map<String, Object>> tickets = FastList.newInstance();
        productType.put(FINISHED_GOOD, "physical");//实物
        productType.put(VIRTUAL_GOOD, "virtual");//虚拟
        Map orderStatus = new HashMap();
        
        for (GenericValue v : valueList) {
            Map<String, Object> map = FastMap.newInstance();
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
            String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            
            map.put("orderId", v.get("orderId"));//订单号
            map.put("productName", v.get("productName"));//产品名称
            map.put("imgUrl", imageUrl);//商品图片
            map.put("number", v.get("number"));//下单数量
            map.put("total", v.get("total")); //订单总金额
            List<GenericValue> returnItems = delegator.findList("ReturnItem", EntityCondition.makeCondition("orderId", v.get("orderId")), null, UtilMisc.toList("-applyTime"), null, false);
            //产品类型[FINISHED_GOOD:实物产品,VIRTUAL_GOOD:虚拟产品]
            if (FINISHED_GOOD.equals(v.get("productType"))) {
                map.put("productType", productType.get(FINISHED_GOOD));//商品类型
                //订单状态： 待发货、待收货、待评价、已完成、已退单
                orderStatus = getOrderStatus(delegator, v.getString("orderId"), v.getString("orderStatus"));
                map.put("orderStatus", orderStatus.get("status"));//订单状态
                map.put("orderStatusId", orderStatus.get("statusId"));//订单状态ID
                map.put("validityDate", "");
                //是否有“yabiz商城券”按钮 & 是否有“查看物流”按钮 &  是否有“确认收货”按钮 & 是否有“去评价”按钮 & 是否有“退款详情”按钮
                map.put("rLiftButton", false);
                //订单状态 ：(自提订单不显示物流) && (有物流信息)
                List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", v.get("orderId")));
                if (!SMZT.equals(v.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                    map.put("logisticsButton", true);
                } else {
                    map.put("logisticsButton", false);
                }
                Boolean mark = true;
                if (UtilValidate.isNotEmpty(returnItems)) {
                    for (GenericValue returnItem : returnItems) {
                        if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                            Timestamp applyTime = returnItem.getTimestamp("applyTime");
                            //订单规则
                            GenericValue orderRule = null;
                            try {
                                orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            Long time = 5 * 60 * 1000L;
                            if (UtilValidate.isNotEmpty(orderRule)) {
                                if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                    time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                }
                                Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                map.put("refundTime", sdf.format(refundTime));
                            }
                            mark = false;
                            break;
                        }
                    }
                }
                //订单状态为“待收货”时有：确认收货按钮
                if ("ORDER_WAITRECEIVE".equals(v.getString("orderStatus"))) {
                    map.put("receiveButton", true);
                    if (mark) {
                        Timestamp deliveryTime = null;
                        GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", v.get("orderId")));
                        if (!SMZT.equals(v.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                            deliveryTime = orderHeader.getTimestamp("orderDate");
                        } else {
                            if (UtilValidate.isNotEmpty(orderDelivery)) {
                                deliveryTime = orderDelivery.get(0).getTimestamp("createdStamp");
                            } else {
                                deliveryTime = orderHeader.getTimestamp("orderDate");
                            }
                        }
                        //订单规则
                        GenericValue orderRule = null;
                        try {
                            orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        Long time = 5 * 60 * 1000L;
                        if (UtilValidate.isNotEmpty(orderRule)) {
                            if (UtilValidate.isNotEmpty(orderRule.get("confirmOrderStamp"))) {
                                time = orderRule.getLong("confirmOrderStamp") * 24 * 60 * 60 * 1000;
                            }
                            Timestamp receiveTime = new Timestamp(deliveryTime.getTime() + time);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            map.put("receiveTime", sdf.format(receiveTime));
                        }
                    }
                } else {
                    map.put("receiveButton", false);
                }
                
                //待评价有“去评价”按钮
                if ("ORDER_WAITEVALUATE".equals(v.getString("orderStatus"))) {
                    map.put("evaluateButton", true);
                } else {
                    map.put("evaluateButton", false);
                }
                //是否有退款详情按钮
                if ("notAudited".equals(orderStatus.get("statusId")) || "notRefunded".equals(orderStatus.get("statusId")) || "hasRefuned".equals(orderStatus.get("statusId")) || "rejectApplication".equals(orderStatus.get("statusId"))) {
                    map.put("viewRefundButton ", true);
                } else {
                    map.put("viewRefundButton", false);
                }
                if ("notAudited".equals(orderStatus.get("statusId")) || "notRefunded".equals(orderStatus.get("statusId"))) {
                    Timestamp applyTime = returnItems.get(0).getTimestamp("applyTime");
                    //订单规则
                    GenericValue orderRule = null;
                    try {
                        orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    Long time = 5 * 60 * 1000L;
                    if (UtilValidate.isNotEmpty(orderRule)) {
                        if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                            time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                        }
                        Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        map.put("refundTime", sdf.format(refundTime));
                    }
                }
                
            } else if (VIRTUAL_GOOD.equals(v.get("productType"))) {
                map.put("productType", productType.get(VIRTUAL_GOOD));
                //券状态：待使用、待评价、已使用、待审核、待退款、已退款、已过期
                Map<String, Object> statusMap = getVirtualOrderStatus(delegator, v);
                map.put("orderStatus", statusMap.get("status"));
                map.put("orderStatusId", statusMap.get("statusId"));
                List<GenericValue> orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", v.get("orderId")));
                //过期日期:虚拟商品有效期至
                GenericValue productActivityGoods = EntityUtil.getFirst(delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", orderItem.get(0).get("activityId"), "productId", v.get("productId"))));
                if (null != productActivityGoods) {
                    map.put("validityDate", UtilDateTime.timeStampToString(productActivityGoods.getTimestamp("virtualProductEndDate"), "yyyy-MM-dd", timeZone, locale));
                } else {
                    map.put("validityDate", "");
                }
                Map<String, Object> ticketResult = getrLifeTicketByStatus(delegator, v.getString("productId"), v.getString("orderId"));
                tickets = (List<Map<String, Object>>) ticketResult.get("ticketList");
                //是否有“yabiz商城券”按钮 & 是否有“查看物流”按钮 &  是否有“确认收货”按钮 & 是否有“去评价”按钮 & 是否有“退款详情”按钮
                map.put("rLiftButton", true);
                map.put("logisticsButton", false);
                map.put("receiveButton", false);
                map.put("viewRefundButton", false);
                map.put("evaluateButton", statusMap.get("evaluateButton"));
                if (UtilValidate.isNotEmpty(returnItems)) {
                    Timestamp applyTime = null;
                    for (GenericValue returnItem : returnItems) {
                        if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                            applyTime = returnItem.getTimestamp("applyTime");
                            //订单规则
                            GenericValue orderRule = null;
                            try {
                                orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            Long time = 5 * 60 * 1000L;
                            if (UtilValidate.isNotEmpty(orderRule)) {
                                if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                    time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                }
                                Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                map.put("refundTime", sdf.format(refundTime));
                            }
                            break;
                        }
                    }
                }
            }
            map.put("rLifeTickets", tickets);
            if (UtilValidate.isNotEmpty(returnItems)) {
                for (GenericValue returnItem : returnItems) {
                    if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId"))) {
                        map.put("returnStatus", true);
                        break;
                    } else if ("RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                        map.put("returnStatus", true);
                        break;
                    }
                }
            }
            list.add(map);
//            list.add(v.getAllFields());
        }
        return list;
    }
    
    /**
     * 待使用订单结果集转换为List  add by wcy
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> noUsedOrderListFormGenericValue(Delegator delegator, List<GenericValue> valueList, TimeZone timeZone, Locale locale, LocalDispatcher dispatcher) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> tickets = new ArrayList<Map<String, Object>>();
        
        for (GenericValue v : valueList) {
            Map<String, Object> map = FastMap.newInstance();
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
            String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            
            map.put("orderId", v.get("orderId"));//订单号
            map.put("productName", v.get("productName"));//产品名称
            map.put("imgUrl", imageUrl);//商品图片
            map.put("number", v.get("number"));//下单数量
            map.put("total", v.get("total")); //订单总金额
            map.put("productType", "virtual");  //虚拟
            map.put("orderStatus", "待使用");
            map.put("orderStatusId", "notUsed");
            List<GenericValue> orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", v.get("orderId")));
            //过期日期:虚拟商品有效期至
            GenericValue productActivityGoods = EntityUtil.getFirst(delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", orderItem.get(0).get("activityId"), "productId", v.get("productId"))));
            if (null != productActivityGoods) {
                map.put("validityDate", UtilDateTime.timeStampToString(productActivityGoods.getTimestamp("virtualProductEndDate"), "yyyy-MM-dd", timeZone, locale));
            } else {
                map.put("validityDate", "");
            }
            //获取未使用生活券
            Map<String, Object> ticketResult = getrLifeTicketByStatus(delegator, v.getString("productId"), v.getString("orderId"));//"notUsed"
            tickets = (List<Map<String, Object>>) ticketResult.get("ticketList");
            map.put("rLiftButton", true);
            map.put("logisticsButton", false);
            map.put("receiveButton", false);
            map.put("evaluateButton", false);
            map.put("viewRefundButton", false);
            map.put("rLifeTickets", tickets);
            List<GenericValue> returnItems = delegator.findList("ReturnItem", EntityCondition.makeCondition("orderId", v.get("orderId")), null, UtilMisc.toList("-applyTime"), null, false);
            if (UtilValidate.isNotEmpty(returnItems)) {
                Timestamp applyTime = null;
                for (GenericValue returnItem : returnItems) {
                    if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                        applyTime = returnItem.getTimestamp("applyTime");
                        //订单规则
                        GenericValue orderRule = null;
                        try {
                            orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        Long time = 5 * 60 * 1000L;
                        if (UtilValidate.isNotEmpty(orderRule)) {
                            if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                            }
                            Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            map.put("refundTime", sdf.format(refundTime));
                        }
                        map.put("returnStatus", true);
                        break;
                    }
                }
            }
//            list.add(v.getAllFields());
            list.add(map);
        }
        return list;
    }
    
    /**
     * 待收货订单结果集转换为List  add by wcy
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> notReceivedOrderListFormGenericValue(Delegator delegator, List<GenericValue> valueList, TimeZone timeZone, Locale locale, LocalDispatcher dispatcher) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> tickets = new ArrayList<Map<String, Object>>();
        String orderStatus = "";
        
        for (GenericValue v : valueList) {
            Map<String, Object> map = FastMap.newInstance();
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
            String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            
            map.put("orderId", v.get("orderId"));//订单号
            map.put("productName", v.get("productName"));//产品名称
            map.put("imgUrl", imageUrl);//商品图片
            map.put("number", v.get("number"));//下单数量
            map.put("total", v.get("total")); //订单总金额
            map.put("productType", "physical");//商品类型
            map.put("validityDate", "");
            
            List<GenericValue> returnItems = delegator.findList("ReturnItem", EntityCondition.makeCondition("orderId", v.get("orderId")), null, UtilMisc.toList("-applyTime"), null, false);
            //待发货
            if ("ORDER_WAITSHIP".equals(v.get("orderStatus"))) {
                orderStatus = "待发货";
                map.put("orderStatusId", "notShipped");
                map.put("logisticsButton", false);
                map.put("receiveButton", false);
                if (UtilValidate.isNotEmpty(returnItems)) {
                    for (GenericValue returnItem : returnItems) {
                        if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                            Timestamp applyTime = returnItem.getTimestamp("applyTime");
                            //订单规则
                            GenericValue orderRule = null;
                            try {
                                orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            Long time = 5 * 60 * 1000L;
                            if (UtilValidate.isNotEmpty(orderRule)) {
                                if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                    time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                }
                                Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                map.put("refundTime", sdf.format(refundTime));
                            }
                            break;
                        }
                    }
                }
            } else if ("ORDER_WAITRECEIVE".equals(v.get("orderStatus"))) {
                orderStatus = "待收货";
                map.put("orderStatusId", "notReceived");
                map.put("receiveButton", true);
                Boolean mark = true;
                if (UtilValidate.isNotEmpty(returnItems)) {
                    for (GenericValue returnItem : returnItems) {
                        if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                            Timestamp applyTime = returnItem.getTimestamp("applyTime");
                            //订单规则
                            GenericValue orderRule = null;
                            try {
                                orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            Long time = 5 * 60 * 1000L;
                            if (UtilValidate.isNotEmpty(orderRule)) {
                                if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                    time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                }
                                Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                map.put("refundTime", sdf.format(refundTime));
                            }
                            mark = false;
                            break;
                        }
                    }
                }
                if (mark) {
                    Timestamp deliveryTime = null;
                    List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", v.get("orderId")));
                    GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", v.get("orderId")));
                    if (!SMZT.equals(v.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                        deliveryTime = orderHeader.getTimestamp("orderDate");
                    } else {
                        if (UtilValidate.isNotEmpty(orderDelivery)) {
                            deliveryTime = orderDelivery.get(0).getTimestamp("createdStamp");
                        } else {
                            deliveryTime = orderHeader.getTimestamp("orderDate");
                        }
                    }
                    //订单规则
                    GenericValue orderRule = null;
                    try {
                        orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    Long time = 5 * 60 * 1000L;
                    if (UtilValidate.isNotEmpty(orderRule)) {
                        if (UtilValidate.isNotEmpty(orderRule.get("confirmOrderStamp"))) {
                            time = orderRule.getLong("confirmOrderStamp") * 24 * 60 * 60 * 1000;
                        }
                        Timestamp receiveTime = new Timestamp(deliveryTime.getTime() + time);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        map.put("receiveTime", sdf.format(receiveTime));
                    }
                }
            }
            map.put("orderStatus", orderStatus);
            map.put("rLiftButton", false);
            map.put("evaluateButton", false);
            map.put("viewRefundButton", false);
            map.put("rLifeTickets", tickets);
            if (UtilValidate.isNotEmpty(returnItems)) {
                for (GenericValue returnItem : returnItems) {
                    if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId"))) {
                        map.put("returnStatus", true);
                        break;
                    } else if ("RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                        map.put("returnStatus", true);
                        break;
                    }
                }
            }
//            list.add(v.getAllFields());
            list.add(map);
        }
        
        return list;
    }
    
    /**
     * 待评价订单结果集转换为List
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> notReviewedOrderListFormGenericValue(Delegator delegator, List<GenericValue> valueList, TimeZone timeZone, Locale locale, LocalDispatcher dispatcher) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> tickets = new ArrayList<Map<String, Object>>();
        Map<String, String> productType = FastMap.newInstance();
        productType.put(FINISHED_GOOD, "实物");
        productType.put(VIRTUAL_GOOD, "虚拟");
        String orderStatus = "待评价";
        
        for (GenericValue v : valueList) {
            //过期日期:实物商品和虚拟商品都有参与活动
            GenericValue productActivityGoods = EntityUtil.getFirst(delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("productId", v.get("productId"))));
            if (null != productActivityGoods) {
                v.put("validityDate", UtilDateTime.timeStampToString(productActivityGoods.getTimestamp("virtualProductEndDate"), "yyyy-MM-dd", timeZone, locale));
            } else {
                v.put("validityDate", "");
            }
            
            List<GenericValue> returnItems = delegator.findList("ReturnItem", EntityCondition.makeCondition("orderId", v.get("orderId")), null, UtilMisc.toList("-applyTime"), null, false);
            //虚拟订单
            if (VIRTUAL_GOOD.equals(v.getString("productType"))) {
                //yabiz商城券”与“去评价”按钮
                v.put("rLiftButton", true);
                if (UtilValidate.isNotEmpty(returnItems)) {
                    Timestamp applyTime = null;
                    for (GenericValue returnItem : returnItems) {
                        if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                            applyTime = returnItem.getTimestamp("applyTime");
                            //订单规则
                            GenericValue orderRule = null;
                            try {
                                orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            Long time = 5 * 60 * 1000L;
                            if (UtilValidate.isNotEmpty(orderRule)) {
                                if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                    time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                }
                                Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                v.put("refundTime", sdf.format(refundTime));
                            }
                            break;
                        }
                    }
                }
            } else if (FINISHED_GOOD.equals(v.getString("productType"))) {
                v.put("rLiftButton", false);
                List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", v.get("orderId")));
                if (!SMZT.equals(v.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                    v.put("logisticsButton", true);
                } else {
                    v.put("logisticsButton", false);
                }
                if (UtilValidate.isNotEmpty(returnItems)) {
                    for (GenericValue returnItem : returnItems) {
                        if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                            Timestamp applyTime = returnItem.getTimestamp("applyTime");
                            //订单规则
                            GenericValue orderRule = null;
                            try {
                                orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            Long time = 5 * 60 * 1000L;
                            if (UtilValidate.isNotEmpty(orderRule)) {
                                if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                    time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                }
                                Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                v.put("refundTime", sdf.format(refundTime));
                            }
                            break;
                        }
                    }
                }
            }
            v.put("orderStatusId", "notReviewed");
            v.put("receiveButton", false);
            v.put("productType", productType.get(v.getString("productType")));
            v.put("orderStatus", orderStatus);
            v.put("evaluateButton", true);
            v.put("viewRefundButton", false);
            v.put("rLifeTickets", tickets);
            if (UtilValidate.isNotEmpty(returnItems)) {
                for (GenericValue returnItem : returnItems) {
                    if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId"))) {
                        v.put("returnStatus", true);
                        break;
                    } else if ("RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                        v.put("returnStatus", true);
                        break;
                    }
                }
            }
            list.add(v.getAllFields());
            
        }
        
        return list;
    }
    
    
    /**
     * 获取订单状态
     * 待发货、待收货、待评价, “待审核”、“待退款”、“已退款”、“拒绝申请”
     *
     * @param delegator
     * @param orderId
     * @param orderStatus
     * @return
     */
    public static Map getOrderStatus(Delegator delegator, String orderId, String orderStatus) {
        
        Map map = new HashMap();
        List<GenericValue> returnItems = null;
        try {
            returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if ("ORDER_WAITSHIP".equals(orderStatus)) {
            map.put("status", "待发货");
            map.put("statusId", "notShipped");
        } else if ("ORDER_WAITRECEIVE".equals(orderStatus)) {
            map.put("status", "待收货");
            map.put("statusId", "notReceived");
        } else if ("ORDER_WAITEVALUATE".equals(orderStatus)) {
            map.put("status", "待评价");
            map.put("statusId", "notReviewed");
        } else if ("ORDER_REFUNDED".equals(orderStatus)) {
            map.put("status", "已退款");
            map.put("statusId", "hasRefuned");
        } else if ("ORDER_REJECTED".equals(orderStatus)) {
            map.put("status", "拒绝申请");
            map.put("statusId", "rejectApplication");
        } else if ("ORDER_COMPLETED".equals(orderStatus)) {
            map.put("status", "已完成");
            map.put("statusId", "completed");
        } else if (UtilValidate.isNotEmpty(returnItems)) {
            for (GenericValue returnItem : returnItems) {
                if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId"))) {
                    map.put("status", "待审核");
                    map.put("statusId", "notAudited");
                    break;
                } else if ("RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                    map.put("status", "待退款");
                    map.put("statusId", "notRefunded");
                    break;
                } else if ("RETURN_COMPLETED".equals(returnItem.get("statusId"))) {
                    map.put("status", "已退款");
                    map.put("statusId", "hasRefuned");
                    break;
                } else if ("RETURN_REJECTAPPLY".equals(returnItem.get("statusId"))) {
                    map.put("status", "拒绝申请");
                    map.put("statusId", "rejectApplication");
                    break;
                }
            }
        }
        return map;
    }
    
    /**
     * 获取虚拟订单状态
     * 展示在“全部”列表中的状态优先级为：待评价>待审核>待退款>已过期>待使用>已完成>已退款
     *
     * @param delegator
     * @param order
     * @return
     */
    public static Map<String, Object> getVirtualOrderStatus(Delegator delegator, GenericValue order) {
        Map<String, Object> orderStatus = FastMap.newInstance();
        //一个订单对应多个券
        Map<String, Object> ticketResult = getrLifeTicketStatus(delegator, order.getString("productId"), order.getString("orderId"));
        List<String> ticketList = (List<String>) ticketResult.get("ticketList");
        if ("ORDER_WAITEVALUATE".equals(order.getString("orderStatus")) && ticketList.toString().contains("hasUsed")) {
            orderStatus.put("statusId", "notReviewed");
            orderStatus.put("status", "待评价");
            orderStatus.put("evaluateButton", true);
            return orderStatus;
        }
        if (ticketList.toString().contains("notAudited")) {
            orderStatus.put("statusId", "notAudited");
            orderStatus.put("status", "待审核");
        } else if (ticketList.toString().contains("notRefunded")) {
            orderStatus.put("statusId", "notRefunded");
            orderStatus.put("status", "待退款");
        } else if (ticketList.toString().contains("expired")) {
            orderStatus.put("statusId", "expired");
            orderStatus.put("status", "已过期");
        } else if (ticketList.toString().contains("notUsed") || ticketList.toString().contains("rejectApplication")) {
            orderStatus.put("statusId", "notUsed");
            orderStatus.put("status", "待使用");
        } else if ("ORDER_COMPLETED".equals(order.getString("orderStatus"))) {
            orderStatus.put("statusId", "completed");
            orderStatus.put("status", "已完成");
        } else if (ticketList.toString().contains("hasRefuned")) {
            orderStatus.put("statusId", "hasRefuned");
            orderStatus.put("status", "已退款");
        }
        orderStatus.put("evaluateButton", false);
        
        return orderStatus;
    }
    
    /**
     * 判断订单状态： 待收货 & 待评价 & 已完成
     * 是否有“查看物流”
     *
     * @param delegator
     * @param statusId
     * @return
     */
    public static Boolean hasLogistics(Delegator delegator, String statusId) {
        List<String> logisticsStatus = FastList.newInstance();
        logisticsStatus.add("ORDER_WAITRECEIVE");
        logisticsStatus.add("ORDER_WAITEVALUATE");
        logisticsStatus.add("ORDER_COMPLETED");
        return logisticsStatus.contains(statusId);
    }
    
    
    /**
     * 待使用 add by Wcy
     * 该用户下的【虚拟商品】的订单，再筛选存在【“未使用”】券的订单
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findUnUsedVirtualList(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 定义查询字段 & 排序字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        dynamicViewEntity.addAlias("OH", "orderId");
        dynamicViewEntity.addAlias("OH", "orderStatus", "statusId", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "createdBy");
        dynamicViewEntity.addAlias("OH", "total", "grandTotal", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "orderDate");
        dynamicViewEntity.addAlias("OH", "distributionMethod");
        
        /** 定义订单role */
        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
        dynamicViewEntity.addAlias("ORE", "partyId");
        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        
        /** 定义订单元素动态视图 */
        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
        dynamicViewEntity.addAlias("OI", "number", "quantity", null, null, null, null);
        
        /** 定义产品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        dynamicViewEntity.addAlias("PD", "productName");
        dynamicViewEntity.addAlias("PD", "productType", "productTypeId", null, null, null, null);
        dynamicViewEntity.addAlias("PD", "imgUrl", "smallImageUrl", null, null, null, null);
        
        /** 定义团购券动态视图 */
        dynamicViewEntity.addMemberEntity("PG", "Ticket");
        dynamicViewEntity.addAlias("PG", "orderId");
        dynamicViewEntity.addAlias("PG", "productId");
        dynamicViewEntity.addAlias("PG", "partyId");
        dynamicViewEntity.addAlias("PG", "ticketStatus");
        
        /** 定义表的关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        dynamicViewEntity.addViewLink("OI", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewEntity.addViewLink("OH", "PG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        /**orderOrle 与orderHeader*/
        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        /** 查询字段 & 排序字段 */
        fieldsToSelect.add("orderId");//订单编号
        fieldsToSelect.add("productId");//产品编号
        fieldsToSelect.add("orderStatus");//订单状态
        fieldsToSelect.add("productName");//产品名称
        fieldsToSelect.add("orderDate");//下单日期
        fieldsToSelect.add("productType");//产品类型[FINISHED_GOOD:实物产品,VIRTUAL_GOOD:虚拟产品]
        fieldsToSelect.add("imgUrl");//产品小图
        fieldsToSelect.add("number");   //下单数量
        fieldsToSelect.add("total");//订单总金额
        fieldsToSelect.add("distributionMethod");//订单配送方式
        fieldsToSelect.add("ticketStatus");//订单配送方式
        
        
        /** 按照下单时间倒序排序 */
        orderBy.add("-orderDate");
        
        /** 定义查询条件集合 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        /** 查询当前用户 */
        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        /** 虚拟商品 */
        andExprs.add(EntityCondition.makeCondition("productType", EntityOperator.EQUALS, VIRTUAL_GOOD));
        /** yabiz商城券为未使用 */
        andExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, "notUsed"), EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, "rejectApplication")), EntityOperator.OR));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        /** 查询结束条数*/
        int highIndex = 0;
        /** 查询结果集*/
        List<GenericValue> orderList = null;
        int orderListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
//            lowIndex = viewIndex * viewSize + 1;
//            highIndex = (viewIndex + 1) * viewSize;
            lowIndex = viewIndex + 1;
            highIndex = viewIndex + viewSize;
            
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            findOpts.setDistinct(true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取分页结果集
            orderList = pli.getPartialList(lowIndex, viewSize);
            //获取记录条数
            orderListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > orderListSize) {
                highIndex = orderListSize;
            }
            
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        
        if (null == orderList) {
            orderList = FastList.newInstance();
        }
        List<Map<String, Object>> jsonArray = null;
        try {
            jsonArray = noUsedOrderListFormGenericValue(delegator, orderList, timeZone, locale, dispatcher);
//            System.out.println(jsonArray);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        //获取订单总数量
//        Long noUserNum = 0L;
//        Map<String, Object> notUsedNumResult = notUsedNum(dctx, context);
//        if (UtilValidate.isNotEmpty(notUsedNumResult) && UtilValidate.isNotEmpty(notUsedNumResult.get("notUsedNum")))
//            noUserNum = (Long) notUsedNumResult.get("notUsedNum");
        
        resultData.put("max", orderListSize);
        resultData.put("orderList", jsonArray);
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 待收货
     * 该用户下的【实物商品】的订单，再筛选【待发货、待收货】的订单
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findDealGoodsList(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 定义查询字段 & 排序字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        dynamicViewEntity.addAlias("OH", "orderId");
        dynamicViewEntity.addAlias("OH", "orderStatus", "statusId", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "createdBy");
        dynamicViewEntity.addAlias("OH", "total", "grandTotal", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "orderDate");
        dynamicViewEntity.addAlias("OH", "distributionMethod");
        
        /** 定义订单role */
        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
        dynamicViewEntity.addAlias("ORE", "partyId");
        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        
        /** 定义订单元素动态视图 */
        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
        dynamicViewEntity.addAlias("OI", "number", "quantity", null, null, null, null);
        
        /** 定义产品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        dynamicViewEntity.addAlias("PD", "productName");
        dynamicViewEntity.addAlias("PD", "productType", "productTypeId", null, null, null, null);
        dynamicViewEntity.addAlias("PD", "imgUrl", "smallImageUrl", null, null, null, null);
        
        
        /** 定义表的关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        dynamicViewEntity.addViewLink("OI", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        /**orderOrle 与orderHeader*/
        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        /** 查询字段 & 排序字段 */
        fieldsToSelect.add("orderId");//订单编号
        fieldsToSelect.add("productId");//产品编号
        fieldsToSelect.add("orderStatus");//订单状态
        fieldsToSelect.add("productName");//产品名称
        fieldsToSelect.add("orderDate");//下单日期
        fieldsToSelect.add("productType");//产品类型[FINISHED_GOOD:实物产品,VIRTUAL_GOOD:虚拟产品]
        fieldsToSelect.add("imgUrl");//产品小图
        fieldsToSelect.add("number");   //下单数量
        fieldsToSelect.add("total");//订单总金额
        fieldsToSelect.add("distributionMethod");//订单配送方式
        
        
        /** 按照下单时间倒序排序 */
        orderBy.add("-orderDate");
        
        /** 定义查询条件集合 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        /** 查询当前用户 */
        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        /** 实物商品 */
        andExprs.add(EntityCondition.makeCondition("productType", EntityOperator.EQUALS, FINISHED_GOOD));
        /** 订单状态：待发货或待收货*/
        andExprs.add(EntityCondition.makeCondition(
                EntityCondition.makeCondition(EntityCondition.makeCondition("orderStatus", EntityOperator.EQUALS, "ORDER_WAITSHIP")),
                EntityOperator.OR,
                EntityCondition.makeCondition(EntityCondition.makeCondition("orderStatus", EntityOperator.EQUALS, "ORDER_WAITRECEIVE")))
        );
        
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        /** 查询结束条数*/
        int highIndex = 0;
        /** 查询结果集*/
        List<GenericValue> orderList = null;
        int orderListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
//            lowIndex = viewIndex * viewSize + 1;
//            highIndex = (viewIndex + 1) * viewSize;
            lowIndex = viewIndex + 1;
            highIndex = viewIndex + viewSize;
            
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            findOpts.setDistinct(true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取分页结果集
            orderList = pli.getPartialList(lowIndex, viewSize);
            //获取记录条数
            orderListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > orderListSize) {
                highIndex = orderListSize;
            }
            
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        
        if (null == orderList) {
            orderList = FastList.newInstance();
        }
        List<Map<String, Object>> jsonArray = null;
        try {
            jsonArray = notReceivedOrderListFormGenericValue(delegator, orderList, timeZone, locale, dispatcher);
//            System.out.println(jsonArray);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        //获取订单总数量
//        Long notReceivedNum = 0L;
//        Map<String, Object> notReceivedNumResult = notReceivedNum(dctx, context);
//        if (UtilValidate.isNotEmpty(notReceivedNumResult) && UtilValidate.isNotEmpty(notReceivedNumResult.get("notReceivedNum")))
//            notReceivedNum = (Long) notReceivedNumResult.get("notReceivedNum");

//        resultData.put("max", notReceivedNum);
        resultData.put("max", orderListSize);
        resultData.put("orderList", jsonArray);
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 退款/售后
     * 该用户下已发起退款/退货，且退款/退货未完成的所有订单
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findAfterSalesList(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        //计算开始分页值 & 计算分页结束值
        lowIndex = viewIndex;
        /** sql 语句*/
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        String limitSql = "limit " + lowIndex + "," + viewSize;
        String sql = "select *  " +
                "  from( " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_id as productId, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,null as returnType,ticket_status as ticketStatus " +
                "  from order_header a, order_item b, product c, ticket d,order_role r " +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and c.product_id= d.product_id " +
                "   and b.order_id= d.order_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and c.product_type_id= 'VIRTUAL_GOOD' " +
                "   and (d.ticket_status='notAudited' or d.ticket_status='notRefunded' or d.ticket_status='hasRefuned' or d.ticket_status='rejectApplication') " +
                " union all " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_id as productId, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,return_header_type_id as returnType,null as ticketStatus " +
                "  from order_header a, order_item b, product c,return_header d,return_item e ,order_role r" +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and c.product_type_id= 'FINISHED_GOOD' " +
//                "   and d.from_party_id = '" + partyId + "' " +
                "   and e.return_id = d.return_id " +
                "   and e.order_id = a.order_id  " +
                ") as temptb " +
                " order by orderDate desc,ticketStatus ";
        
        List<Map<String, Object>> jsonArray = null;
        List<Map> resultArray = new ArrayList<Map>();
        try {
            sqlP.prepareStatement(sql + limitSql);
            ResultSet rs = sqlP.executeQuery();
            jsonArray = getListFromResultSet(rs);
            rs.close();
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    List<Map<String, Object>> tickets = FastList.newInstance();
                    Map<String, Object> orderMap = jsonArray.get(i);
                    Map<String, Object> map = FastMap.newInstance();
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderMap.get("productId")));
                    String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                    
                    map.put("orderId", orderMap.get("orderId"));//订单号
                    map.put("productName", orderMap.get("productName"));//产品名称
                    map.put("imgUrl", imageUrl);//商品图片
                    map.put("number", orderMap.get("number"));//下单数量
                    map.put("total", orderMap.get("total")); //订单总金额
                    if (FINISHED_GOOD.equals(String.valueOf(orderMap.get("productType")))) {
                        List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderMap.get("orderId")));
                        if ("ORDER_REFUNDED".equals(orderMap.get("orderStatus"))) {
                            map.put("orderStatus", "已退款");
                            map.put("orderStatusId", "hasRefuned");
                        } else if ("ORDER_REJECTED".equals(orderMap.get("orderStatus"))) {
                            map.put("orderStatus", "拒绝申请");
                            map.put("orderStatusId", "rejectApplication");
                        } else if (UtilValidate.isNotEmpty(returnItems)) {
                            for (GenericValue returnItem : returnItems) {
                                if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId"))) {
                                    map.put("orderStatus", "待审核");
                                    map.put("orderStatusId", "notAudited");
                                } else if ("RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                                    map.put("orderStatus", "待退款");
                                    map.put("orderStatusId", "notRefunded");
                                } else if ("RETURN_COMPLETED".equals(returnItem.get("statusId"))) {
                                    map.put("orderStatus", "已退款");
                                    map.put("orderStatusId", "hasRefuned");
                                    break;
                                } else if ("RETURN_REJECTAPPLY".equals(returnItem.get("statusId"))) {
                                    map.put("orderStatus", "拒绝申请");
                                    map.put("orderStatusId", "rejectApplication");
                                    break;
                                }
                                if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                                    Timestamp applyTime = returnItem.getTimestamp("applyTime");
                                    //订单规则
                                    GenericValue orderRule = null;
                                    try {
                                        orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                                    } catch (GenericEntityException e) {
                                        e.printStackTrace();
                                    }
                                    Long time = 5 * 60 * 1000L;
                                    if (UtilValidate.isNotEmpty(orderRule)) {
                                        if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                            time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                        }
                                        Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        map.put("refundTime", sdf.format(refundTime));
                                    }
                                    break;
                                }
                            }
                        }
                        map.put("validityDate", "");
                        map.put("productType", "physical");//实物
                        map.put("rLiftButton", false);
                        //退款 || 退货
                        if ("CUSTOMER_RETURN".equals(orderMap.get("returnType")) || "VENDOR_RETURN".equals(orderMap.get("returnType"))) {
                            map.put("logisticsButton", true);
                        } else {
                            map.put("logisticsButton", false);
                        }
                    } else {
                        //一个订单对应多个券
                        Map<String, Object> ticketStatusResult = getrLifeTicketStatus(delegator, (String) orderMap.get("productId"), (String) orderMap.get("orderId"));
                        List<String> ticketList = (List<String>) ticketStatusResult.get("ticketList");
                        if (ticketList.toString().contains("rejectApplication")) {
                            map.put("orderStatusId", "rejectApplication");
                            map.put("orderStatus", "拒绝申请");
                        } else if (ticketList.toString().contains("notAudited")) {
                            map.put("orderStatusId", "notAudited");
                            map.put("orderStatus", "待审核");
                        } else if (ticketList.toString().contains("notRefunded")) {
                            map.put("orderStatusId", "notRefunded");
                            map.put("orderStatus", "待退款");
                        } else if (ticketList.toString().contains("hasRefuned")) {
                            map.put("orderStatusId", "hasRefuned");
                            map.put("orderStatus", "已退款");
                        }
                        List<GenericValue> orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderMap.get("orderId")));
                        //过期日期:虚拟商品有效期至
                        GenericValue productActivityGoods = EntityUtil.getFirst(delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", orderItem.get(0).get("activityId"), "productId", orderMap.get("productId"))));
                        if (null != productActivityGoods) {
                            map.put("validityDate", UtilDateTime.timeStampToString(productActivityGoods.getTimestamp("virtualProductEndDate"), "yyyy-MM-dd", timeZone, locale));
                        } else {
                            map.put("validityDate", "");
                        }
                        map.put("productType", "virtual"); //虚拟
                        map.put("rLiftButton", true);
                        map.put("logisticsButton", false);
                        Map<String, Object> ticketResult = getrLifeTicketByStatus(delegator, (String) orderMap.get("productId"), (String) orderMap.get("orderId"));
                        tickets = (List<Map<String, Object>>) ticketResult.get("ticketList");
                        List<GenericValue> returnItems = delegator.findList("ReturnItem", EntityCondition.makeCondition("orderId", orderMap.get("orderId")), null, UtilMisc.toList("-applyTime"), null, false);
                        if (UtilValidate.isNotEmpty(returnItems)) {
                            Timestamp applyTime = null;
                            for (GenericValue returnItem : returnItems) {
                                if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId")) || "RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                                    applyTime = returnItem.getTimestamp("applyTime");
                                    //订单规则
                                    GenericValue orderRule = null;
                                    try {
                                        orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
                                    } catch (GenericEntityException e) {
                                        e.printStackTrace();
                                    }
                                    Long time = 5 * 60 * 1000L;
                                    if (UtilValidate.isNotEmpty(orderRule)) {
                                        if (UtilValidate.isNotEmpty(orderRule.get("expectedRefundStamp"))) {
                                            time = orderRule.getLong("expectedRefundStamp") * 24 * 60 * 60 * 1000;
                                        }
                                        Timestamp refundTime = new Timestamp(applyTime.getTime() + time);
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        map.put("refundTime", sdf.format(refundTime));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    map.put("rLifeTickets", tickets);
                    map.put("viewRefundButton", false);
                    map.put("evaluateButton", false);
                    map.put("receiveButton", false);
                    resultArray.add(map);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        if (null == jsonArray) {
            jsonArray = FastList.newInstance();
        }
        
        Long refundNum = 0L;
        Map<String, Object> refundNumResult = refundNum(dctx, context);
        if (UtilValidate.isNotEmpty(refundNumResult) && UtilValidate.isNotEmpty(refundNumResult.get("refundNum"))) {
            refundNum = (Long) refundNumResult.get("refundNum");
        }
        
        resultData.put("max", refundNum);
        resultData.put("orderList", resultArray);
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 待评价:包含虚拟订单和实物订单
     * 虚拟商品待评价： 存在状态为“已使用”的yabiz商城券，且订单未评价
     * 实物商品待评价： 展示中台状态为“待评价”订单
     * 查询排序：按照下单时间倒序排序，时间重复的实物订单优先放到虚拟订单上方
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findUnReviewList(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        String partyId = userLogin.getString("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        //计算开始分页值 & 计算分页结束值
        lowIndex = viewIndex;
        /** sql 语句*/
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        String limitSql = "limit " + lowIndex + "," + viewSize;
        String sql = "select * " +
                "  from(" +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,c.product_id as productId " +
                "  from order_header a, order_item b, product c, ticket d,order_role r" +
                " where a.order_id= b.order_id" +
                "   and b.product_id= c.product_id " +
                "   and c.product_id= d.product_id " +
                "   and b.order_id= d.order_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and a.status_id= 'ORDER_WAITEVALUATE' " +
                "   and c.product_type_id= 'VIRTUAL_GOOD' " +
                "   and d.ticket_status='hasUsed' " +
                " union all " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,c.product_id as productId" +
                "  from order_header a, order_item b, product c,order_role r " +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and r.order_id= a.order_id " +
                "   and r.party_id= '" + partyId + "' " +
                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and a.status_id= 'ORDER_WAITEVALUATE' " +
                "   and c.product_type_id= 'FINISHED_GOOD') as temptb " +
                " order by orderDate desc,productType ";
        
        List<Map<String, Object>> jsonArray = null;
        List<Map> resultArray = new ArrayList<Map>();
        try {
            sqlP.prepareStatement(sql + limitSql);
            ResultSet rs = sqlP.executeQuery();
            //ResultSet转成List<Map<String,Object>>
            jsonArray = getListFromResultSet(rs);
            rs.close();
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    List<Map<String, Object>> tickets = FastList.newInstance();
                    Map<String, Object> orderMap = jsonArray.get(i);
                    Map<String, Object> map = FastMap.newInstance();
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderMap.get("productId")));
                    String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                    
                    map.put("orderId", orderMap.get("orderId"));//订单号
                    map.put("productName", orderMap.get("productName"));//产品名称
                    map.put("imgUrl", imageUrl);//商品图片
                    map.put("number", orderMap.get("number"));//下单数量
                    map.put("total", orderMap.get("total")); //订单总金额
                    map.put("orderStatus", "待评价");
                    map.put("orderStatusId", "notReviewed");
                    if (FINISHED_GOOD.equals(String.valueOf(orderMap.get("productType")))) {
                        map.put("productType", "physical");//实物
                        map.put("validityDate", "");
                        map.put("rLiftButton", false);
                        //是否上门自提
                        List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderMap.get("orderId")));
                        if (!SMZT.equals(orderMap.get("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                            map.put("logisticsButton", true);
                        } else {
                            map.put("logisticsButton", false);
                        }
                    } else {
                        map.put("productType", "virtual");//虚拟
                        List<GenericValue> orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderMap.get("orderId")));
                        //过期日期:虚拟商品有效期至
                        GenericValue productActivityGoods = EntityUtil.getFirst(delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", orderItem.get(0).get("activityId"), "productId", orderMap.get("productId"))));
                        if (null != productActivityGoods) {
                            map.put("validityDate", UtilDateTime.timeStampToString(productActivityGoods.getTimestamp("virtualProductEndDate"), "yyyy-MM-dd", timeZone, locale));
                        } else {
                            map.put("validityDate", "");
                        }
                        map.put("rLiftButton", true);
                        map.put("logisticsButton", false);
                        Map<String, Object> ticketResult = getrLifeTicketByStatus(delegator, (String) orderMap.get("productId"), (String) orderMap.get("orderId"));
                        tickets = (List<Map<String, Object>>) ticketResult.get("ticketList");
                        
                    }
                    map.put("rLifeTickets", tickets);
                    map.put("evaluateButton", true);
                    map.put("viewRefundButton", false);
                    map.put("receiveButton", false);
                    resultArray.add(map);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        if (null == jsonArray) {
            jsonArray = FastList.newInstance();
        }
        
        //获取订单总数量
        Long notReviewedNum = 0L;
        Map<String, Object> notReviewedNumResult = notReviewedNum(dctx, context);
        if (UtilValidate.isNotEmpty(notReviewedNumResult) && UtilValidate.isNotEmpty(notReviewedNumResult.get("notReviewedNum"))) {
            notReviewedNum = (Long) notReviewedNumResult.get("notReviewedNum");
        }
        
        resultData.put("max", notReviewedNum);
        resultData.put("orderList", resultArray);
        result.put("resultData", resultData);
        return result;
    }
    
    
    /**
     * 确认收货
     * “确认收货”时，判断该订单是否存在退货或退款
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> changeOrderStatusValuate(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        // String userLoginId = "18912979995";
        String orderId = (String) context.get("orderId");
        //String orderId ="WSOD10626";
        /** 返回提示消息*/
        String tipMsg = "";
        
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == userLogin) {
            result.put("resultData", false);
            return result;
        }
        
        /** 查询订单 */
        String[] orderStatus = new String[]{"RETURN_CANCELLED", "RETURN_COMPLETED", "RETURN_REJECTAPPLY", "RETURN_REJECTRECEIVE"};
        //EntityCondition condition = EntityCondition.makeCondition("orderId", EntityOperator.IN,Arrays.asList(orderStatus));
        //add by gss
        EntityCondition condition = EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId), EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, Arrays.asList(orderStatus))), EntityOperator.AND);
        Set<String> fieldsToSelect = FastSet.newInstance();
        fieldsToSelect.add("returnId");
        try {
            List<GenericValue> orderReturn = delegator.findList("ReturnItem", condition, fieldsToSelect, null, null, false);
            if (UtilValidate.isEmpty(orderReturn)) {
                Map<String, Object> ctx = FastMap.newInstance();
                ctx.put("statusId", "ORDER_WAITEVALUATE");
                ctx.put("orderId", orderId);
                ctx.put("setItemStatus", "Y");
                ctx.put("userLogin", userLogin);
                Map<String, Object> resp = null;
                try {
                    resp = dispatcher.runSync("changeOrderStatus", ctx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    result.put("resultData", false);
                    return result;
                }
                if (ServiceUtil.isError(resp)) {
                    result.put("resultData", false);
                    return result;
                    //return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,
                    //        "OrderErrorCouldNotChangeOrderStatus", locale), null, null, resp);
                }
                
                result.put("resultData", true);
                return result;
                
            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", false);
            return result;
        }
        //{
        //    //如果订单已发起退款 / 退货，且退款 / 退货单状态为“待审核、待退款”时,提示如下信息：订单存在进行中的退款/退货记录,无法确认收货!
        //    tipMsg = UtilProperties.getMessage(resource_error,
        //            "OrderExistReturnItemError", locale);
        //    result.put("info", tipMsg);
        //    result.put("resultData", false);
        //    return result;
        //}
        
        //tipMsg = UtilProperties.getMessage(resource,
        //        "OrderStatusValuate", locale);
        //result.put("info", tipMsg);
        result.put("resultData", false);
        return result;
    }
    
    
    /**
     * 订单详情 add by wcy
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> orderDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String orderStatusId = (String) context.get("orderStatusId");
        Map<String, String> productType = FastMap.newInstance();
        productType.put(FINISHED_GOOD, "physical");
        productType.put(VIRTUAL_GOOD, "virtual");
        
        Map<String, String> orderStatusMap = FastMap.newInstance();
        orderStatusMap.put("notUsed", "待使用");
        orderStatusMap.put("hasUsed", "已使用");
        orderStatusMap.put("notShipped", "待发货");
        orderStatusMap.put("notReceived", "待收货");
        orderStatusMap.put("notReviewed", "待评价");
        orderStatusMap.put("notAudited", "待审核");
        orderStatusMap.put("notRefunded", "待退款");
        orderStatusMap.put("hasRefuned", "已退款");
        orderStatusMap.put("rejectApplication", "拒绝申请");
        orderStatusMap.put("expired", "已过期");
        orderStatusMap.put("completed", "已完成");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        
        String partyId = userLogin.getString("partyId");
        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (null == orderHeader) {
                result.put("resultData", resultData);
                return result;
            }
            GenericValue orderItem = EntityUtil.getFirst(delegator.getRelated("OrderItem", null, null, orderHeader));
            if (null == orderItem) {
                result.put("resultData", resultData);
                return result;
            }
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get("productId")));
            if (null == product) {
                result.put("resultData", resultData);
                return result;
            }
            GenericValue orderAttribute = EntityUtil.getFirst(delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderItem.getString("orderId"), "attrName", "telPhone")));
            GenericValue integralDiscount = EntityUtil.getFirst(delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderItem.getString("orderId"), "attrName", "integralDiscount")));
            String telPhone = orderAttribute == null ? "" : orderAttribute.getString("attrValue");
            String discount = integralDiscount == null ? "" : integralDiscount.getString("attrValue");
            //产品活动
            String activityId = orderItem.getString("activityId");
            GenericValue activity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
            GenericValue productActivity = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", product.getString("productId")));
            String activityDesc = "";
            Long soldNum = 0L;
            if (null != productActivity) {
                activityDesc = activity == null ? "" : activity.getString("activityDesc");
                
                //虚拟券有效期
                if (VIRTUAL_GOOD.equals(product.get("productTypeId"))) {
                    resultData.put("validityDate", UtilDateTime.timeStampToString(productActivity.getTimestamp("virtualProductEndDate"), "yyyy-MM-dd", timeZone, locale));
                } else {
                    resultData.put("validityDate", "");
                }
                //已售数量
                soldNum = activity.getLong("hasBuyQuantity");
                resultData.put("soldNum", soldNum);//已售数量
            } else {
                resultData.put("validityDate", "");
                resultData.put("soldNum", soldNum);
            }
            //阶梯价
            List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
            try {
                productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId), UtilMisc.toList("orderQuantity"));//阶梯价规则表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            List<Map> priceList = new ArrayList<Map>();
            if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                for (GenericValue productGroupOrderRule : productGroupOrderRules) {
                    Map price = new HashMap();
                    price.put("people", productGroupOrderRule.get("orderQuantity"));
                    price.put("price", productGroupOrderRule.get("orderPrice"));
                    priceList.add(price);
                }
            }
            resultData.put("priceList", priceList);//阶梯价
            
            //活动标签
            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
            Boolean anytimeRefund = false;
            Boolean overtimeRefund = false;
            if ("Y".equals(productActivity.getString("isAnyReturn"))) {
                list.add(UtilMisc.toMap("labelName", "随时退"));
                resultData.put("anytimeRefund", true); // 是否有随时退标签
                anytimeRefund = true;
            } else {
                resultData.put("anytimeRefund", false); // 是否有随时退标签
            }
            if ("Y".equals(productActivity.getString("isSupportOverTimeReturn"))) {
                list.add(UtilMisc.toMap("labelName", "支持过期退"));
                overtimeRefund = true;
            }
            if ("Y".equals(productActivity.getString("isSupportScore"))) {
                list.add(UtilMisc.toMap("labelName", "活动可积分"));
            }
            if ("Y".equals(productActivity.getString("isSupportReturnScore"))) {
                list.add(UtilMisc.toMap("labelName", "退货返回积分"));
            }
            //add by gss
            if ("Y".equals(productActivity.getString("isPostageFree"))) {
                list.add(UtilMisc.toMap("labelName", "包邮"));
            }
//            if ("Y".equals(productActivity.getString("isShowIndex"))) {
//                list.add(UtilMisc.toMap("labelName", "推荐到首页"));
//            }
            //生活券
            Boolean refund = false;
            List<Map<String, Object>> ticketList = new ArrayList<Map<String, Object>>();
            if (VIRTUAL_GOOD.equals(product.getString("productTypeId"))) {
                resultData.put("QRCodeButton", true); //是否有“二维码”按钮
                //虚拟商品获取生活券
                List<GenericValue> tickets = delegator.findByAnd("Ticket", UtilMisc.toMap("orderId", orderItem.getString("orderId")));
                for (GenericValue v : tickets) {
                    //TODO：判断是否退款
                    Map<String, Object> ticketMap = new HashMap<String, Object>();
                    ticketMap.put("index", v.getString("ticketIndex"));
                    ticketMap.put("ticketId", v.getString("ticketId"));
                    ticketMap.put("ticketNo", v.getString("ticketNo"));
                    ticketMap.put("statusId", v.getString("ticketStatus"));
                    ticketMap.put("status", orderStatusMap.get(v.getString("ticketStatus")));     //券的状态
                    ticketList.add(ticketMap);
                    if ("notUsed".equals(v.getString("ticketStatus"))) {
                        refund = true;
                    }
                }
            } else {
                resultData.put("QRCodeButton", false); //是否有“二维码”按钮
            }
            if (UtilValidate.isNotEmpty(orderHeader.get("distributionMethod")) && "SMZT".equals(orderHeader.get("distributionMethod"))) {
                resultData.put("receivePerson", ""); //收货人姓名
                resultData.put("receivePhone", "");    //收货人电话
                resultData.put("receiveAddress", ""); //收货人地址
            } else {
                //收货人
                OrderReadHelper orderReadHelper = new OrderReadHelper(orderHeader);
                GenericValue shippingLocations = EntityUtil.getFirst(orderReadHelper.getShippingLocations());
                String province = "";
                String city = "";
                String country = "";
                String address = "";
                
                if (null != shippingLocations) {
                    province = getGeoName(delegator, "PROVINCE", shippingLocations.getString("stateProvinceGeoId"));
                    city = getGeoName(delegator, "CITY", shippingLocations.getString("city"));
                    country = getGeoName(delegator, "COUNTY", shippingLocations.getString("countryGeoId"));
                    address = province + city + country + shippingLocations.getString("address1");
                    resultData.put("receivePerson", shippingLocations.getString("toName")); //收货人姓名
                    resultData.put("receivePhone", shippingLocations.getString("mobilePhone"));
                    resultData.put("receiveAddress", address);    //省市区+地址
                } else {
                    resultData.put("receivePerson", ""); //收货人姓名
                    resultData.put("receivePhone", "");    //收货人电话
                    resultData.put("receiveAddress", ""); //收货人地址
                }
            }
            resultData.put("orderId", orderId);
            resultData.put("activityId", activityId);//活动编号
            resultData.put("productName", product.getString("productName"));//产品名称
            resultData.put("productType", productType.get(product.getString("productTypeId")));
            //订单状态【待使用、待收货、待评价、待审核、待退款、已退款、拒绝申请、已过期、已完成】
            resultData.put("orderStatus", orderStatusMap.get(orderStatusId));
            resultData.put("orderStatusId", orderStatusId);
            String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            resultData.put("imgUrl", imageUrl);//产品图片
            resultData.put("number", orderItem.get("quantity"));//下单数量
            //判断订单是否有更改价格  add by gss   begin 
            BigDecimal unitPrice = BigDecimal.ZERO;
            if (UtilValidate.isNotEmpty(orderItem.getBigDecimal("lastUnitPrice"))) {
                unitPrice = orderItem.getBigDecimal("lastUnitPrice");
            } else {
                unitPrice = orderItem.getBigDecimal("unitPrice");
            }
            resultData.put("total", unitPrice.multiply(orderItem.getBigDecimal("quantity")));//订单总金额
            //判断订单是否有更改价格  add by gss   end
            //resultData.put("total", orderHeader.getBigDecimal("grandTotal"));//订单总金额
            resultData.put("activityLabels", list);  //活动标签
            resultData.put("rLifeTickets", ticketList); //生活券
            if (UtilValidate.isNotEmpty(activityDesc)) {
                resultData.put("buyingTips", activityDesc);  //购买须知
            } else {
                resultData.put("buyingTips", "");
            }
            
            Boolean ladderGroup = false;
            if (UtilValidate.isNotEmpty(priceList) && priceList.size() > 1) {
                resultData.put("ladderGroup", true);// 是否阶梯团购
                ladderGroup = true;
            } else {
                resultData.put("ladderGroup", false);
            }
            if ("ORDER_WAITEVALUATE".equals(orderHeader.get("statusId")) && "notReviewed".equals(orderStatusId)) {
                resultData.put("evaluateButton", true);  // 是否有“去评价”按钮
            } else {
                resultData.put("evaluateButton", false);
            }
            //评价信息
            List<GenericValue> productReview = delegator.findByAnd("ProductReview", UtilMisc.toMap("orderId", orderId));
            if (UtilValidate.isNotEmpty(productReview)) {
                resultData.put("viewEvaluateButton", true); // 是否有“查看评价”按钮
            } else {
                resultData.put("viewEvaluateButton", false);
            }
            //订单规则
            GenericValue orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
            if (VIRTUAL_GOOD.equals(product.getString("productTypeId"))) {
                resultData.put("receiveButton", false);
                resultData.put("logisticsButton", false);
                resultData.put("viewRefundButton", false);
                if (UtilValidate.isNotEmpty(orderRule) && "N".equals(orderRule.get("isReturn"))) {
                    resultData.put("refundButton", false);
                } else {
//                    if ("notUsed".equals(orderStatusId) && refund) {
//                        if (ladderGroup) {
//                            resultData.put("refundButton", false);//是否有“申请退款”按钮
//                        } else {
//                            resultData.put("refundButton", true);
//                        }
//                    } else
                    if ("expired".equals(orderStatusId)) {
                        if (ladderGroup || !overtimeRefund) {
                            resultData.put("refundButton", false);
                        } else {
                            resultData.put("refundButton", true);
                        }
                    } else {
                        if (refund) {
                            if (ladderGroup) {
                                resultData.put("refundButton", false);//是否有“申请退款”按钮
                            } else {
                                resultData.put("refundButton", true);
                            }
                        } else {
                            resultData.put("refundButton", false);
                        }
                    }
                }
            } else {
                //查看是否有退款/退货记录
                String[] orderStatus = new String[]{"RETURN_CANCELLED", "RETURN_COMPLETED", "RETURN_REJECTAPPLY", "RETURN_REJECTRECEIVE"};
                EntityCondition condition = EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId), EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, Arrays.asList(orderStatus))), EntityOperator.AND);
                Set<String> fieldsToSelect = FastSet.newInstance();
                fieldsToSelect.add("returnId");
                List<GenericValue> orderReturn = delegator.findList("ReturnItem", condition, fieldsToSelect, null, null, false);
                if ("ORDER_WAITRECEIVE".equals(orderHeader.get("statusId")) && UtilValidate.isEmpty(orderReturn)) {
                    resultData.put("receiveButton", true); // 是否有“确认收货”按钮
                } else {
                    resultData.put("receiveButton", false);
                }
                //订单状态 ：(自提订单不显示物流) && (有物流信息)
                List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderId));
                if (!SMZT.equals(orderHeader.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                    resultData.put("logisticsButton", true); // 是否有“查看物流”按钮
                } else {
                    resultData.put("logisticsButton", false);
                }
                
                List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(returnItems) && ("notAudited".equals(orderStatusId) || "notRefunded".equals(orderStatusId) || "hasRefuned".equals(orderStatusId) || "rejectApplication".equals(orderStatusId))) {
                    resultData.put("viewRefundButton", true);// 是否有“退款详情”按钮
                } else {
                    resultData.put("viewRefundButton", false);
                }
                if (UtilValidate.isNotEmpty(orderRule) && "N".equals(orderRule.get("isReturn"))) {
                    resultData.put("refundButton", false);
                } else {
                    if (UtilValidate.isNotEmpty(returnItems)) {
                        resultData.put("refundButton", false);//是否有“申请退款”按钮
                    } else {
                        if ("notShipped".equals(orderStatusId) || "notReceived".equals(orderStatusId)) {
                            if (ladderGroup) {
                                resultData.put("refundButton", false);
                            } else {
                                resultData.put("refundButton", true);
                            }
                        } else if ("notReviewed".equals(orderStatusId) || "completed".equals(orderStatusId)) {
//                            if ("0".equals(orderHeader.get("isAllowReturn"))) {
//                                resultData.put("refundButton", false);
//                            }else{
//                                resultData.put("refundButton", true);
//                            }
                            GenericValue orderStatusChange = EntityUtil.getFirst(delegator.findByAnd("OrderStatus", UtilMisc.toMap("statusId", "ORDER_WAITEVALUATE", "orderId", orderId)));
                            Timestamp statusDatetime = orderStatusChange.getTimestamp("statusDatetime");
                            Calendar c = Calendar.getInstance();
                            c.setTime(statusDatetime);
                            Long time = 24 * 60 * 60 * 1000L;
                            if (UtilValidate.isNotEmpty(orderRule) && UtilValidate.isNotEmpty(orderRule.get("returnCommitStamp"))) {
                                time = orderRule.getLong("returnCommitStamp");
                            }
                            c.add(Calendar.DATE, time.intValue());
                            Date returnCommitStamp = c.getTime();//可提交退款的时间
                            if (ladderGroup || returnCommitStamp.compareTo(new Date()) < 0) {
                                resultData.put("refundButton", false);
                            } else {
                                resultData.put("refundButton", true);
                            }
                        } else {
                            resultData.put("refundButton", false);
                        }
                    }
                }
            }
            
            resultData.put("orderTime", UtilDateTime.timeStampToString(orderHeader.getTimestamp("orderDate"), "yyyy-MM-dd HH:mm:ss", timeZone, locale)); //  下单时间 “2015-11-11 10:52:52
            resultData.put("receiveTicketPhone", telPhone); //  接收消费券手机号
            resultData.put("integralDeduction", discount); //积分抵扣
//            resultData.put("balancePayment", orderHeader.get("balance"));// 余额支付
            resultData.put("remarks", orderHeader.get("remarks"));// 备注
            BigDecimal realPayment = BigDecimal.ZERO;
            List<GenericValue> orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId, "statusId", "PAYMENT_RECEIVED"));
            if (UtilValidate.isNotEmpty(orderPaymentPreferences)) {
                for (GenericValue orderPaymentPreference : orderPaymentPreferences) {
                    realPayment = realPayment.add(orderPaymentPreference.getBigDecimal("maxAmount"));
                }
            }
            resultData.put("realPayment", realPayment); // 实际支付
            
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        
        
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 结果集转换为List add by wcy
     *
     * @param rs
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> getListFromResultSet(ResultSet rs) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> rowData = new HashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
    }
    
    public static String getGeoName(Delegator delegator, String type, String id) {
        try {
            GenericValue geo = EntityUtil.getFirst(delegator.findByAnd("Geo", UtilMisc.toMap("geoId", id, "geoTypeId", type)));
            if (null != geo) {
                return geo.getString("geoName");
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return "";
    }
    
    /**
     * 结果集转换为List add by wcy
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> changeListFormGenericValue(List<GenericValue> valueList) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (GenericValue v : valueList) {
            list.add(v.getAllFields());
        }
        return list;
    }
    
    
    /**
     * add by dx 2016.01.13
     * 更新快递
     *
     * @param request
     * @param response
     * @return
     */
    public static String updateDelivery(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        String orderId = (String) request.getParameter("orderId");
        String id = delegator.getNextSeqId("OrderDelivery");
        String orderStatus = (String) request.getParameter("orderStatus");
        orderStatus = UtilValidate.isNotEmpty(orderStatus) ? orderStatus : "";
        delegator.create("OrderDelivery", UtilMisc.toMap("id", id, "orderId", orderId, "deliveryCompany", request.getParameter("deliveryCompany"),
                "logisticsNumber1", request.getParameter("logisticsNumber1"), "logisticsNumber2", request.getParameter("logisticsNumber2"), "logisticsNumber3", request.getParameter("logisticsNumber3")));
        Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_WAITRECEIVE", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
        setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
        
        String mobile = "";
        if (UtilValidate.isNotEmpty(delegator.findByPrimaryKey("OrderAttribute", UtilMisc.toMap("orderId", orderId, "attrName", "telPhone")))) {
            mobile = delegator.findByPrimaryKey("OrderAttribute", UtilMisc.toMap("orderId", orderId, "attrName", "telPhone")).getString("attrValue");
        } else {
            Map<String, Object> info = OrderReadHelper.getOrderPurchaseInfo(delegator, orderId);
            mobile = (String) info.get("fromMobile");
        }
        String businessName = org.ofbiz.order.order.OrderReadHelper.getOrderBusinessName(delegator, orderId);
        GenericValue product = org.ofbiz.order.order.OrderReadHelper.getOrderProducts(delegator, orderId).get(0);
        //向快递100发送订阅请求
        PostOrder.postOrder(request.getParameter("deliveryCompany"), request.getParameter("logisticsNumber1"), null, null, mobile, businessName, product.getString("productName"));
        //保存订单操作日志
        delegator.create("OrderOperateLog", UtilMisc.toMap("id", delegator.getNextSeqId("OrderOperateLog"), "orderId", orderId, "operateType", "发货动作", "operator", userLogin.get("userLoginId"),
                "operateTime", UtilDateTime.nowTimestamp()));
        //发送短信
//        mobile = "18851050925";
       /* if (!"".equals(mobile)) {
            Map context = FastMap.newInstance();
            context.put("mobile", mobile);
            List<GenericValue> products = OrderReadHelper.getOrderProducts(delegator, orderId);
            String productName = "";
            if (UtilValidate.isNotEmpty(products)) {
                productName = products.get(0).getString("productName");
            }
            String content = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("smsContent", "physical.order.delivered"), UtilMisc.toMap("productName", productName));
            context.put("content", content);
            try {
                Map<String, Object> resultMap = dispatcher.runSync("smsSend", context);
                String code = (String) resultMap.get("result");
                if (UtilValidate.isEmpty(code)) {
                    Debug.logError("审核退货或退款单发送短信错误，错误代码：" + code, module);
                } else if (code.startsWith("-")) {
                    Debug.logError("审核退货或退款单发送短信错误，错误代码：" + code, module);
                }
            } catch (GenericServiceException e) {
                e.printStackTrace();
                Debug.logError(e, module);
            }
        }*/
        return "success";
    }
    
    /**
     * add by dx 2016.01.13
     * 取消订单
     *
     * @return
     */
    public static Map<String, Object> cancelOrder(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        
        String orderId = (String) context.get("orderId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String operateReason = (String) context.get("operateReason");
        
        Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_CANCELLED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
        setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
        //保存订单操作日志
        try {
            delegator.create("OrderOperateLog", UtilMisc.toMap("id", delegator.getNextSeqId("OrderOperateLog"), "orderId", orderId, "operateType", "已取消", "operator", userLogin.get("userLoginId"),
                    "operateTime", UtilDateTime.nowTimestamp(), "operateReason", operateReason));
            
            //如果该订单为拼团订单时 取消订单 则在拼团订单关联表中删除该订单
            List<GenericValue> togtherGroupOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("orderId", orderId));
            
            if (UtilValidate.isNotEmpty(togtherGroupOrders)) {
                GenericValue togtherGroupOrder = togtherGroupOrders.get(0);
                String togetherId = togtherGroupOrder.getString("togetherId");
                delegator.removeByAnd("TogetherGroupRelOrder", UtilMisc.toMap("orderId", orderId, "togetherId", togetherId));
                
                delegator.findByPrimaryKey("TogetherGroup", UtilMisc.toMap("togetherId", togetherId));
                
            }
            
            List<GenericValue> orderItems = null;
            GenericValue productActivity = null;
            List<GenericValue> orderRoles = null;
            List<GenericValue> orderAttributes = null;
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            try {
                orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(orderItems)) {
                    productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", orderHeader.get("activityId")));
                }
                orderRoles = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", "PLACING_CUSTOMER"));
                orderAttributes = delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            
            //恢复活动剩余  by AlexYao 2016/02/01
            if (UtilValidate.isNotEmpty(productActivity)) {
                Long occupiedQuantityTotal = productActivity.getLong("occupiedQuantityTotal") == null ? 0L : productActivity.getLong("occupiedQuantityTotal");
                Long quantity = orderItems.get(0).getBigDecimal("quantity") == null ? 0L : orderItems.get(0).getBigDecimal("quantity").longValue();
                productActivity.set("occupiedQuantityTotal", occupiedQuantityTotal - quantity);
                try {
                    delegator.store(productActivity);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }

            }
            
            //恢复库存  by AlexYao 2016/02/01
            if (UtilValidate.isNotEmpty(orderItems)) {
                for (int i = 0; i < orderItems.size(); i++) {
                    GenericValue orderItem = orderItems.get(i);
                    List<GenericValue> inventoryItems = null;
                    try {
                        inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", orderItem.get("productId")));//库存明细
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(inventoryItems)) {
                        //锁定库存减少
                        BigDecimal lockQuantityTotal = (inventoryItems.get(0).getBigDecimal("lockQuantityTotal")) == null ? BigDecimal.ZERO : inventoryItems.get(0).getBigDecimal("lockQuantityTotal");
                        if (lockQuantityTotal.compareTo(BigDecimal.ZERO) > 0) {
                            inventoryItems.get(0).set("lockQuantityTotal", lockQuantityTotal.subtract(orderItem.getBigDecimal("quantity")));
                            try {
                                inventoryItems.get(0).store();
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            
            //恢复代金劵
            try {
                List<GenericValue> couponList = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId, "orderAdjustmentTypeId", "TICKET_ADJUESTMENT"));
                if (UtilValidate.isNotEmpty(couponList)) {
                    for (int i = 0; i < couponList.size(); i++) {
                        GenericValue couponAdjust = couponList.get(i);
                        String ticketId = couponAdjust.getString("sourceReferenceId");
                        GenericValue ticket = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId", ticketId));
                        ticket.set("ticketStatus", "notUsed");
                        delegator.store(ticket);
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            //恢复优惠劵
            try {
                List<GenericValue> couponList = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId, "orderAdjustmentTypeId", "COUPON_ADJUESTMENT"));
                if (UtilValidate.isNotEmpty(couponList)) {
                    for (int i = 0; i < couponList.size(); i++) {
                        GenericValue couponAdjust = couponList.get(i);
                        String coponCodeId = couponAdjust.getString("sourceReferenceId");
                        GenericValue couponCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId", coponCodeId));
                        couponCode.set("promoCodeStatus", "G");
                        delegator.store(couponCode);
                        GenericValue productPromoCoupon = couponCode.getRelatedOne("ProductPromoCoupon");
                        //优惠劵Code标注已使用、优惠劵更新order数
                        if (UtilValidate.isNotEmpty(productPromoCoupon)) {
                            
                            Long orderCount = productPromoCoupon.getLong("orderCount") == null ? 0L : productPromoCoupon.getLong("orderCount");
                            if (orderCount > 0L) {
                                productPromoCoupon.set("orderCount", orderCount - 1);
                            }
                            delegator.store(productPromoCoupon);
                        }
                        
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return resultData;
    }
    
    /**
     * add by dx 2016.01.14
     * 更新订单优惠价格
     *
     * @param request
     * @param response
     * @return
     */
    public static String updateOrderDiscountMoney(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        String orderId = (String) request.getParameter("orderId");
        BigDecimal discountMoney = new BigDecimal(request.getParameter("discountMoney"));
        GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        BigDecimal shouldPayMoney = UtilValidate.isNotEmpty(orderHeader.getBigDecimal("shouldPayMoney")) ? orderHeader.getBigDecimal("shouldPayMoney") : BigDecimal.ZERO;
        BigDecimal notPayMoney = UtilValidate.isNotEmpty(orderHeader.getBigDecimal("notPayMoney")) ? orderHeader.getBigDecimal("notPayMoney") : BigDecimal.ZERO;
        shouldPayMoney = shouldPayMoney.subtract(discountMoney);
        orderHeader.set("discountMoney", discountMoney);
        orderHeader.set("shouldPayMoney", shouldPayMoney);
        orderHeader.set("notPayMoney", notPayMoney.subtract(discountMoney));
        orderHeader.store();
        //保存订单操作日志
        delegator.create("OrderOperateLog", UtilMisc.toMap("id", delegator.getNextSeqId("OrderOperateLog"), "orderId", orderId, "operateType", "修改金额", "operator", userLogin.get("userLoginId"),
                "operateTime", UtilDateTime.nowTimestamp(), "operateReason", request.getParameter("operateReason")));
        return "success";
    }
    
    
    public static String getProductReviewImages(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productReviewId = request.getParameter("productReviewId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map<String, Object>> images = ProductWorker.getProductReviewContent(delegator, productReviewId);
        request.setAttribute("images", images);
        return "success";
    }
    
    public static String changeIsShow(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productReviewId = request.getParameter("productReviewId");
        String isShow = request.getParameter("isShow");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue genericValue = delegator.findByPrimaryKey("ProductReview", UtilMisc.toMap("productReviewId", productReviewId));
        genericValue.set("isShow", isShow);
        genericValue.store();
        return "success";
    }
    
    /**
     * add by dx 2016.01.21
     * 更新退款单信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String updateReturn(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        String returnId =  request.getParameter("returnId");
        String newStatusId = "";
        List<GenericValue> toBeSaved = new LinkedList<GenericValue>();
        String operateType = (String) request.getParameter("operateType");
        BigDecimal actualPaymentMoney = null;
        String returnType = (String) request.getParameter("returnType");
        if (UtilValidate.isNotEmpty(operateType)) {
            /*if(operateType.equals("同意退货")){           // ico
                newStatusId = "RETURN_WAITSHIP";
            }else if (operateType.equals("拒绝退货")){
                newStatusId = "RETURN_REJECTAPPLY";
            }else if (operateType.equals("同意收货")){
                newStatusId = "RETURN_WAITFEFUND";
            }else if (operateType.equals("拒绝收货")){
                newStatusId = "RETURN_REJECTRECEIVE";
            }*/
            if ("同意退货".equals(operateType) || "同意退款".equals(operateType)) {
                // 弘阳 待审核直接到待退款
                newStatusId = "RETURN_WAITFEFUND";
            } else if ("拒绝退货".equals(operateType) || "拒绝退款".equals(operateType)) {
                newStatusId = "RETURN_REJECTAPPLY";

            }

            GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader", UtilMisc.toMap("returnId", returnId));
            String userId = returnHeader.getString("createdBy");
            GenericValue user = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userId));
            String pId = user.getString("partyId");
            GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", pId));
            String openId = person.getString("wxAppOpenId");

            List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("returnId", returnId));
            BigDecimal retPay = BigDecimal.ZERO;
            String productInfo = "";
            if (UtilValidate.isNotEmpty(returnItems)) {
                for (int i = 0; i < returnItems.size(); i++) {
                    GenericValue returnItem = returnItems.get(i);
                    retPay = retPay.add(returnItem.getBigDecimal("applyMoney"));
                    productInfo += returnItem.getRelatedOne("Product").getString("productName") + " ";
                }
            }
            String orderId =  returnHeader.getString("orderId");
            String template_id="";
            String template_Type="";
            Map<String, Object> daMap = FastMap.newInstance();
            daMap.put("keyword1",returnId);
            daMap.put("keyword3", retPay.toString());
            daMap.put("keyword4", productInfo);
            if("同意退货".equals(operateType)){
                GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "REFUND_GOOD_NOTIFY")));

                template_id = config.getString("wxLiteTemplateId");
                template_Type=config.getString("templateType");
                //发送小程序消息
                daMap.put("keyword2", "商家同意了您的退货申请");

            }else if("同意退款".equals(operateType)){
                GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "REFUND_APPLY_NOTIFY")));

                template_id = config.getString("wxLiteTemplateId");
                template_Type=config.getString("templateType");

                //发送小程序消息
                daMap.put("keyword2","商家同意了您的退款申请");

            }else if("拒绝退货".equals(operateType)){
                GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "REFUND_GOOD_BACK")));

                template_id = config.getString("wxLiteTemplateId");
                template_Type=config.getString("templateType");

                //发送小程序消息
                daMap.put("keyword2", "商家驳回了您的退货申请");


            }else if("拒绝退款".equals(operateType)){
                GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "REFUND_APPLY_BACK")));

                template_id = config.getString("wxLiteTemplateId");
                template_Type=config.getString("templateType");

                //发送小程序消息
                daMap.put("keyword2","商家驳回了您的退款申请");
            }

            try {
                String page ="/pages/webview/index?path=reviewProgress&id=" +orderId+"&returnId"+returnId;
                Map<String, Object> order =dispatcher.runSync("xgro-sendTemplateMsg2", UtilMisc.toMap("page",page,"templateSendType", template_Type, "touser", openId, "data", daMap, "partyId", userLogin.getString("partyId"), "objectValueId", returnHeader.get("returnId")));
            } catch (Exception e) {
                e.printStackTrace();
                Debug.log(e.getMessage());
            }

        } else {
            actualPaymentMoney = new BigDecimal((String) request.getParameter("actualPaymentMoney"));
            newStatusId = "RETURN_COMPLETED";
        }
        GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader", UtilMisc.toMap("returnId", returnId));
        returnHeader.set("statusId", newStatusId);
        GenericValue returnItem = delegator.findByAnd("ReturnItem", UtilMisc.toMap("returnId", returnId)).get(0);
        returnItem.set("statusId", newStatusId);
        String orderId = returnItem.getString("orderId");
        GenericValue orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId)).get(0);
        GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        String activityId = orderHeader.getString("activityId");
        if (UtilValidate.isEmpty(operateType)) {
            returnItem.set("actualPaymentMoney", actualPaymentMoney);
            operateType = "退款" + actualPaymentMoney + "元";
            //更新会员余额
            GenericValue orderRole = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", returnItem.getString("orderId"), "roleTypeId", "BILL_TO_CUSTOMER")).get(0);
            String partyId = orderRole.getString("partyId");
            
            List<GenericValue> orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
            if (UtilValidate.isNotEmpty(orderPaymentPreferences)) {
                GenericValue orderPaymentPreference = EntityUtil.getFirst(orderPaymentPreferences);
                String paymentMethodTypeId = orderPaymentPreference.getString("paymentMethodTypeId");
                if (paymentMethodTypeId != null && "EXT_QB".equalsIgnoreCase(paymentMethodTypeId)) {
                    
                    GenericValue partyAccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", partyId));
                    if (partyAccount == null) {
                        partyAccount = delegator.makeValidValue("PartyAccount", UtilMisc.toMap("partyId", partyId, "amount", actualPaymentMoney, "createDate", UtilDateTime.nowTimestamp()));
                    } else {
                        partyAccount.set("amount", partyAccount.getBigDecimal("amount").add(actualPaymentMoney));
                    }
                    delegator.createOrStore(partyAccount);      //保存总余额
                    delegator.create("PartyAccountDetail", UtilMisc.toMap("detailId", delegator.getNextSeqId("PartyAccountDetail"), "partyId", partyId,
                            "amount", actualPaymentMoney, "description", "订单退款，退单号：" + returnId, "operator", userLogin.get("userLoginId"), "createDate", UtilDateTime.nowTimestamp()));
                    
                    
                } else if (paymentMethodTypeId != null && "EXT_PING".equalsIgnoreCase(paymentMethodTypeId)) {
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    //调用PING++退款
                    try {
                        dispatcher.runSync("pingRefundRequest", UtilMisc.toMap("orderId", orderId, "chargeId", orderPaymentPreference.get("chargeId"), "amount", actualPaymentMoney, "returnHeader", returnHeader));
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                    }
                    /*try {
                        resultMap = dispatcher.runSync("refundWeixin", UtilMisc.toMap("returnId", returnId, "orderId", orderId, "refundfee", actualPaymentMoney.toString()));
                        request.setAttribute("status", resultMap.get("status"));
                        request.setAttribute("msg", resultMap.get("msg"));
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                        return "error";
                    }*/
                }
            }
        }
        returnItem.set("statusId", newStatusId);
        GenericValue returnStatus = delegator.makeValue("ReturnStatus", UtilMisc.toMap("returnStatusId", delegator.getNextSeqId("ReturnStatus"),
                "statusId", newStatusId, "returnId", returnId, "returnItemSeqId", returnItem.get("returnItemSeqId"), "changeByUserLoginId", userLogin.get("userLoginId"),
                "statusDatetime", UtilDateTime.nowTimestamp()));
        
        
        if ("RETURN_WAITFEFUND".equals(newStatusId)) {
            returnItem.set("examinePassTime", UtilDateTime.nowTimestamp());
            
        } else if ("RETURN_COMPLETED".equals(newStatusId)) {   //退款完成
            returnItem.set("completeTime", UtilDateTime.nowTimestamp());
            
            //库存操作
            if ("0".equals(returnItem.get("returnType"))) {        //退货单只有实物商品需要退库存
                String productId = returnItem.getString("productId");
                GenericValue inventoryItem = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId)).get(0);
                inventoryItem.set("accountingQuantityTotal", inventoryItem.getBigDecimal("accountingQuantityTotal").add(returnItem.getBigDecimal("returnQuantity")));
                toBeSaved.add(inventoryItem);
                //判断商品是否参加了活动
                if (UtilValidate.isNotEmpty(activityId)) {
                    GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
                    productActivity.set("hasBuyQuantity", productActivity.getLong("hasBuyQuantity") - returnItem.getBigDecimal("returnQuantity").longValue());
                    toBeSaved.add(productActivity);
                }
            } else if ("1".equals(returnItem.get("returnType"))) {
                //退款单只退虚拟商品的库存
                String productId = returnItem.getString("productId");
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                if ("VIRTUAL_GOOD".equals(product.getString("productTypeId"))) {
                    GenericValue inventoryItem = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId)).get(0);
                    inventoryItem.set("accountingQuantityTotal", inventoryItem.getBigDecimal("accountingQuantityTotal").add(returnItem.getBigDecimal("returnQuantity")));
                    toBeSaved.add(inventoryItem);
                    if (UtilValidate.isNotEmpty(activityId)) {
                        GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
                        productActivity.set("hasBuyQuantity", productActivity.getLong("hasBuyQuantity") - returnItem.getBigDecimal("returnQuantity").longValue());
                        toBeSaved.add(productActivity);
                    }
                } else {      //实物商品
                    
                    if ("ORDER_WAITSHIP".equals(orderHeader.getString("statusId"))) {
                        GenericValue inventoryItem = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId)).get(0);
                        inventoryItem.set("accountingQuantityTotal", inventoryItem.getBigDecimal("accountingQuantityTotal").add(returnItem.getBigDecimal("returnQuantity")));
                        toBeSaved.add(inventoryItem);
                        if (UtilValidate.isNotEmpty(activityId)) {
                            GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
                            productActivity.set("hasBuyQuantity", productActivity.getLong("hasBuyQuantity") - returnItem.getBigDecimal("returnQuantity").longValue());
                            toBeSaved.add(productActivity);
                        }
                    }
                }
            }
            
        } else if ("RETURN_REJECTAPPLY".equals(newStatusId)) {
            //更新‘是否允许退货’为“是”
            orderHeader.set("isAllowReturn", "1");
            delegator.store(orderHeader);
        }
        
        toBeSaved.add(returnHeader);
        toBeSaved.add(returnItem);
        toBeSaved.add(returnStatus);
        delegator.storeAll(toBeSaved);
        //修改虚拟订单券中的状态
        List<GenericValue> returnTickets = delegator.findByAnd("ReturnTicket", UtilMisc.toMap("returnId", returnId));
        if (UtilValidate.isNotEmpty(returnTickets) && returnTickets.size() > 0) {
            for (GenericValue rt : returnTickets) {
                GenericValue t = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId", rt.getString("ticketId")));
                if ("RETURN_WAITFEFUND".equals(newStatusId)) {
                    t.set("ticketStatus", "notRefunded");
                } else if ("RETURN_COMPLETED".equals(newStatusId)) {
                    t.set("ticketStatus", "hasRefuned");
                } else if ("RETURN_REJECTAPPLY".equals(newStatusId)) {
                    t.set("ticketStatus", "rejectApplication");
                }
                t.store();
            }
        }
        //保存订单操作日志
        delegator.create("ReturnOperateLog", UtilMisc.toMap("id", delegator.getNextSeqId("ReturnOperateLog"), "returnId", returnId, "operateType", operateType, "operator", userLogin.get("userLoginId"),
                "operateTime", UtilDateTime.nowTimestamp(), "operateReason", request.getParameter("operateReason")));
        //更新用户积分
        String productId = orderItem.getString("productId");
        if ("RETURN_COMPLETED".equals(newStatusId) && UtilValidate.isNotEmpty(activityId)) {           //参加了活动
            GenericValue productActivityGoods = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", productId));
            if ("Y".equals(productActivityGoods.getString("isSupportReturnScore"))) {         //是否允许退积分
                String billPartyId = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId)).get(0).getString("partyId");
                BigDecimal useIntegral = orderHeader.getBigDecimal("useIntegral");
                BigDecimal integral = useIntegral.divide(orderItem.getBigDecimal("quantity"), 2, BigDecimal.ROUND_HALF_UP);
                BigDecimal shouldReturnIntegral = (integral.multiply(returnItem.getBigDecimal("returnQuantity"))).setScale(0, BigDecimal.ROUND_HALF_UP);
                String token = UtilValidate.isNotEmpty(delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", billPartyId))) ? delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", billPartyId)).get(0).getString("token") : null;
                GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", billPartyId));
                if (!shouldReturnIntegral.equals(BigDecimal.ZERO)) {
                    try {
                        //调用yabiz积分改变接口
                        Map context = FastMap.newInstance();
                        context.put("token", token);
                        context.put("changedir", "plus");
                        context.put("point", shouldReturnIntegral.intValue());
                        context.put("type", "3");
                        dispatcher.runSync("partyScoreChange", context);
                        long oldScoreValue = partyScore.getLong("scoreValue");
                        long value = shouldReturnIntegral.longValue();
                        partyScore.set("scoreValue", oldScoreValue + value);
                        partyScore.store();
                        delegator.create("PartyIntegralHistory", UtilMisc.toMap("partyIntegralHistoryId", delegator.getNextSeqId("PartyIntegralHistory"),
                                "partyId", billPartyId, "integralValue", value, "getWay", "退货或退款", "description", "退货或退款增加积分，订单号：【" + orderId + "】，退单号：【" + returnId + "】，增加积分：" + shouldReturnIntegral.intValue()));
                        /*Map context = FastMap.newInstance();
                        context.put("partyId", billPartyId);
                        context.put("changedir", "plus");
                        context.put("integralValue", shouldReturnIntegral);
                        context.put("type", "3");
                        context.put("getWay", "退货或退款");
                        context.put("orderId", orderId);
                        context.put("description", "退货或退款增加积分，订单号：【"+orderId+"】，退单号：【"+returnId+"】，增加积分："+ shouldReturnIntegral.intValue());
                        dispatcher.runSync("partyIntegralChange",context);*/
                    } catch (Exception e) {
                        e.printStackTrace();
                        Debug.logError("退款调用积分同步接口出错", module);
                    }
                }
            }
        }
        boolean isAllReturn = OrderReadHelper.isAllReturn(delegator, orderId);
        
        if (isAllReturn) {
            //全部退货或退款，需要退使用积分
            Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_RETURNED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
            setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
        }
        
        if (UtilValidate.isNotEmpty(returnType) && "1".equals(returnType)) {
            return "orderRefund";
        } else {
            return "orderReturn";
        }
    }
    
    /*
    public static String exportOrder(HttpServletRequest request, HttpServletResponse response) throws IOException, GenericEntityException {
        String orderIds = request.getParameter("ids");
        String fileName = "订单" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
        String name = new String(fileName.getBytes("gbk"), "iso8859-1");
        Locale locale = UtilHttp.getLocale(request);
        if (UtilValidate.isNotEmpty(orderIds)) {
            Delegator delegator = (Delegator) request.getAttribute("delegator");
            List<String> orderProdctIdsList = UtilMisc.toListArray(orderIds.split(","));
            EntityCondition condition = EntityCondition.makeCondition("orderId", EntityOperator.IN, orderProdctIdsList);
            List<GenericValue> orderHeaders = null;
            try {
                orderHeaders = delegator.findList("OrderHeader", condition, null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            //设置response相应属性，设置为下载
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment;filename=" + name);
            //获得response中的输出流
            OutputStream out = null;
            out = response.getOutputStream();
            //excel表格第一行标题，本例中不采用poi或jxl，只对每个单元属性后面添//加\t，每行结束加\n。这是excel文件的基本属性。
            String head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t实付金额\t支付方式\t订单状态\t商家\n";
            int id = 0;
            out.write(head.getBytes("gbk"));
            if (UtilValidate.isNotEmpty(orderHeaders)) {
                for (GenericValue orderHeader : orderHeaders) {
                    id++;
                    String orderId = orderHeader.getString("orderId");
                    //商品
                    List<GenericValue> products = OrderReadHelper.getOrderProducts(delegator, orderHeader.getString("orderId"));
                    //商品数量
                    BigDecimal num = OrderReadHelper.getOrderProductsNum(delegator, orderHeader.getString("orderId"));
                    //购买人信息
                    Map<String, Object> userInfo = OrderReadHelper.getOrderUserInfo(delegator, orderHeader.getString("orderId"));
                    Map<String, Object> info = OrderReadHelper.getToCustomerInfo(delegator, orderHeader.getString("orderId"));
                    //商家
                    String businessName = OrderReadHelper.getOrderBusinessName(delegator, orderHeader.getString("orderId"));
                    //支付方式
                    List<GenericValue> orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                    String paymentMethodName = "";
                    if (UtilValidate.isNotEmpty(orderPaymentPreferences)) {
                        GenericValue orderPaymentPreference = orderPaymentPreferences.get(0);
                        GenericValue paymentMethod = delegator.findByPrimaryKey("PaymentMethodType", UtilMisc.toMap("paymentMethodTypeId", orderPaymentPreference.get("paymentMethodTypeId")));
                        paymentMethodName = (String) paymentMethod.get("description", locale);
                    }
                    String orderStatus = (String) orderHeader.getRelatedOneCache("StatusItem").get("description", locale);
                    String productName = "";
                    for (GenericValue p : products) {
                        productName += p.get("productName") + ",";
                    }
                    productName = (productName != "") ? productName.substring(0, productName.length() - 1) : "";
                    StringBuffer conBuffer = new StringBuffer();
                    conBuffer.append(id + "\t");
                    conBuffer.append(orderId + "\t");
                    conBuffer.append(outPutString(productName) + "\t");
                    conBuffer.append(outPutString(orderHeader.getBigDecimal("shouldPayMoney") + "") + "\t" );
                    conBuffer.append( + num.intValue()  + "\t");
                    conBuffer.append(outPutString(sdf.format(orderHeader.getTimestamp("orderDate"))) + "\t");
                    conBuffer.append(outPutString(userInfo.get("name") + "") + "\t");
                    conBuffer.append(outPutString(info.get("toName")+"") + "\t");
                    conBuffer.append(outPutString(info.get("mobilePhone") + "") + "\t");
                    conBuffer.append(orderHeader.get("grandTotal") + "\t");
                    conBuffer.append(paymentMethodName + "\t");
                    conBuffer.append(orderStatus + "\t");
                    conBuffer.append(businessName + "\n");
                    String content = conBuffer.toString();
                    out.write(content.getBytes("gbk"));
                }
                out.flush();
                out.close();
            }
        }
        return "success";
    }*/
    
    
    public static String exportAllOrder(LocalDispatcher dispatcher, String searchFunction, Map<String, Object> paramMap) {
        //导出根据件查询的结果集
        Map<String, Object> tmpResult = null;
        String resultList = "orderList";
        try {
            tmpResult = dispatcher.runSync(searchFunction, paramMap);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            tmpResult = new HashMap<String, Object>();
        }
        if (searchFunction.contains("Return")) {
            resultList = "returnList";
        }
        List<String> orderIdList = new ArrayList<String>();
        String orderIds = "";
        if (tmpResult != null) {
            
            List<GenericValue> orderList = (List<GenericValue>) tmpResult.get(resultList);
            if (orderList != null && orderList.size() > 0) {
                for (GenericValue order : orderList) {
                    if (searchFunction.contains("Return")) {
                        orderIdList.add(order.getString("returnId") + "-" + order.getString("returnItemSeqId"));
                    } else {
                        orderIdList.add(order.getString("orderId"));
                    }
                }
                orderIds = StringUtil.join(orderIdList, ",");
            }
        }
        
        return orderIds;
    }
//
//    public static String exportOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
//        String orderIds = request.getParameter("ids");
//        String type = UtilValidate.isNotEmpty(request.getParameter("type")) ? request.getParameter("type") : "";
//        String fileName = "订单" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
//        String name = new String(fileName.getBytes("gbk"), "iso8859-1");
//        Locale locale = UtilHttp.getLocale(request);
//        List<Map<String, Object>> orderHeaders = null;
//        String whereSql = " ";
//
//        if (UtilValidate.isEmpty(orderIds)) {
//            HttpSession session = request.getSession();
//            GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
//            String searchFunction = request.getParameter("searchfunction");
//            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
//            Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
//            paramMap.put("VIEW_SIZE", "-1");
//            paramMap.put("userLogin", userLogin);
//            paramMap.remove("type");
//            paramMap.remove("searchfunction");
//            orderIds = exportAllOrder(dispatcher, searchFunction, paramMap);
//        }
//
//        if (UtilValidate.isNotEmpty(orderIds)) {
//            String orderIdsInStr = OrderReadHelper.toSqlInStr(orderIds);
//            Delegator delegator = (Delegator) request.getAttribute("delegator");
//            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
//            SQLProcessor sqlP = new SQLProcessor(helperInfo);
//            whereSql = "where order_id in (" + orderIdsInStr + ") ";
//            String sql = "select * from (\n" +
//                    "select\n" +
//                    "\torderr.orderid 订单号,\n" +
//                    "\t商品名称,\n" +
//                    "\t商品数量,\n" +
//                    "\t单价,\n" +
//                    "\t下单时间,\n" +
//                    "\t配送方式,\n" +
//                    "\torderr.userid,\n" +
//                    "\t(\n" +
//                    "\t\tselect\n" +
//                    "\t\t\tname\n" +
//                    "\t\tfrom\n" +
//                    "\t\t\tperson p\n" +
//                    "\t\twhere\n" +
//                    "\t\t\tp.party_id = orderr.userid\n" +
//                    "\t) 用户,\n" +
//                    "\t(\n" +
//                    "\t\tselect\n" +
//                    "\t\t\tmobile\n" +
//                    "\t\tfrom\n" +
//                    "\t\t\tperson p\n" +
//                    "\t\twhere\n" +
//                    "\t\t\tp.party_id = orderr.userid\n" +
//                    "\t) 用户手机号码,\n" +
//                    "\t(\n" +
//                    "\t\tselect\n" +
//                    "\t\t\tamount\n" +
//                    "\t\tfrom\n" +
//                    "\t\t\tparty_account pa\n" +
//                    "\t\twhere\n" +
//                    "\t\t\tpa.party_id = orderr.userid\n" +
//                    "\t) 用户余额,\n" +
//                    "\t(\n" +
//                    "\t\tselect\n" +
//                    "\t\t\tname\n" +
//                    "\t\tfrom\n" +
//                    "\t\t\tcommunity\n" +
//                    "\t\twhere\n" +
//                    "\t\t\tcommunity_id = orderr.communityid\n" +
//                    "\t) 社区,\n" +
//                    "\t收货人名称,\n" +
//                    "\t收货人联系电话,\n" +
//                    "\t收货人地址,\n" +
//                    "\t(\n" +
//                    "\t\tselect\n" +
//                    "\t\t\tattr_value\n" +
//                    "\t\tfrom\n" +
//                    "\t\t\torder_attribute oa\n" +
//                    "\t\twhere\n" +
//                    "\t\t\toa.attr_name = 'integraldiscount'\n" +
//                    "\t\tand oa.order_id = orderr.orderid\n" +
//                    "\t) 积分抵扣,\n" +
//                    "\t(\n" +
//                    "\t\tcase 配送时间\n" +
//                    "\t\twhen 'smzt' then\n" +
//                    "\t\t\t'上门自提'\n" +
//                    "\t\twhen 'zmps' then\n" +
//                    "\t\t\t'周末配送'\n" +
//                    "\t\twhen 'gzrps' then\n" +
//                    "\t\t\t'工作日配送'\n" +
//                    "\t\tend\n" +
//                    "\t) 配送时间,\n" +
//                    "\t(\n" +
//                    "\t\tcase orderstatus\n" +
//                    "\t\twhen 'order_completed' then\n" +
//                    "\t\t\t'已完成'\n" +
//                    "\t\twhen 'order_waitpay' then\n" +
//                    "\t\t\t'待支付'\n" +
//                    "\t\twhen 'order_waitship' then\n" +
//                    "\t\t\t'待发货'\n" +
//                    "\t\twhen 'order_waitreceive' then\n" +
//                    "\t\t\t'待收货'\n" +
//                    "\t\twhen 'order_waitevaluate' then\n" +
//                    "\t\t\t'待评价'\n" +
//                    "\t\twhen 'order_cancelled' then\n" +
//                    "\t\t\t'已取消'\n" +
//                    "\t\twhen 'order_returned' then\n" +
//                    "\t\t\t'退单'\n" +
//                    "\t\tend\n" +
//                    "\t) 订单状态,\n" +
//                    "\t支付宝支付,\n" +
//                    "\t微信支付,\n" +
//                    "\t钱包支付,\n" +
//                    "\t商家,\n" +
//                    "\t支付流水号\n" +
//                    "from\n" +
//                    "\t(\n" +
//                    "\t\tselect\n" +
//                    "\t\t\toi.order_id orderid,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tproduct_name\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\tproduct p\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tp.product_id = oi.product_id\n" +
//                    "\t\t\t) 商品名称,\n" +
//                    "\t\t\tquantity 商品数量,\n" +
//                    "\t\t\tunit_price 单价,\n" +
//                    "\t\t\tconcat(\n" +
//                    "\t\t\t\t' ',\n" +
//                    "\t\t\t\t(\n" +
//                    "\t\t\t\t\tselect\n" +
//                    "\t\t\t\t\t\torder_date\n" +
//                    "\t\t\t\t\tfrom\n" +
//                    "\t\t\t\t\t\torder_header oh\n" +
//                    "\t\t\t\t\twhere\n" +
//                    "\t\t\t\t\t\toh.order_id = oi.order_id\n" +
//                    "\t\t\t\t)\n" +
//                    "\t\t\t) 下单时间,\n" +
//                    "\t\t\t(select\n" +
//                    "\t\t\t\t\tdistribution_method\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\torder_header oh\n" +
//                    "\t\t\t\t\twhere\n" +
//                    "\t\t\t\t\toh.order_id = oi.order_id\n" +
//                    "\t\t) 配送方式,\n" +
//                    "\t\t\t(select\n" +
//                    "\t\t\t\t\tbalance\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\torder_header oh\n" +
//                    "\t\t\t\t\twhere\n" +
//                    "\t\t\t\t\toh.order_id = oi.order_id\n" +
//                    "\t\t) 钱包支付,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tparty_id\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_role ot\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tot.order_id = oi.order_id\n" +
//                    "\t\t\t\tand ot.role_type_id = 'bill_to_customer'\n" +
//                    "\t\t\t) userid,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tpa.community_id\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_contact_mech ocm\n" +
//                    "\t\t\t\tjoin postal_address pa on ocm.contact_mech_id = pa.contact_mech_id\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tocm.order_id = oi.order_id\n" +
//                    "\t\t\t) communityid,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tpa.to_name\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_contact_mech ocm\n" +
//                    "\t\t\t\tjoin postal_address pa on ocm.contact_mech_id = pa.contact_mech_id\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tocm.order_id = oi.order_id\n" +
//                    "\t\t\t) 收货人名称,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tpa.mobile_phone\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_contact_mech ocm\n" +
//                    "\t\t\t\tjoin postal_address pa on ocm.contact_mech_id = pa.contact_mech_id\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tocm.order_id = oi.order_id\n" +
//                    "\t\t\t) 收货人联系电话,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tpa.address1\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_contact_mech ocm\n" +
//                    "\t\t\t\tjoin postal_address pa on ocm.contact_mech_id = pa.contact_mech_id\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tocm.order_id = oi.order_id\n" +
//                    "\t\t\t) 收货人地址,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tstatus_id\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_header oh\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\toh.order_id = oi.order_id\n" +
//                    "\t\t\t) orderstatus,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tmax_amount\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_payment_preference opp\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tpayment_method_type_id = 'ext_alipay'\n" +
//                    "\t\t\t\tand status_id = 'payment_received'\n" +
//                    "\t\t\t\tand opp.order_id = oi.order_id\n" +
//                    "\t\t\t) 支付宝支付,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tmax_amount\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_payment_preference opp\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tpayment_method_type_id = 'ext_weixin'\n" +
//                    "\t\t\t\tand status_id = 'payment_received'\n" +
//                    "\t\t\t\tand opp.order_id = oi.order_id\n" +
//                    "\t\t\t) 微信支付,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tbusiness_name\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\tproduct p\n" +
//                    "\t\t\t\tleft join party_business pb on p.merchant_name = pb.party_id\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\tp.product_id = oi.product_id\n" +
//                    "\t\t\t) 商家,\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\tdistribution_method\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_header oh\n" +
//                    "\t\t\t\twhere\n" +
//                    "\t\t\t\t\toh.order_id = oi.order_id\n" +
//                    "\t\t\t) 配送时间\n" +
//                    "\t\tfrom\n" +
//                    "\t\t\t(\n" +
//                    "\t\t\t\tselect\n" +
//                    "\t\t\t\t\torder_id,\n" +
//                    "\t\t\t\t\tproduct_id,\n" +
//                    "\t\t\t\t\tquantity,\n" +
//                    "\t\t\t\t\tunit_price\n" +
//                    "\t\t\t\tfrom\n" +
//                    "\t\t\t\t\torder_item\n" +
//                    "\t\t\t) oi " + whereSql +
//                    "\t) orderr\n" +
//                    "left join (\n" +
//                    "\tselect\n" +
//                    "\t\torder_id orderid,\n" +
//                    "\t\tconcat(\n" +
//                    "\t\t\t'流水号：',\n" +
//                    "\t\t\treference_num\n" +
//                    "\t\t) 支付流水号\n" +
//                    "\tfrom\n" +
//                    "\t\torder_payment_preference opp\n" +
//                    "\tjoin payment_gateway_response pgr on opp.order_payment_preference_id = pgr.order_payment_preference_id\n" +
//                    ") reference on orderr.orderid = reference.orderid\n" +
//                    ") a ;";
//
//            sqlP.prepareStatement(sql);
//            ResultSet rs = sqlP.executeQuery();
//            orderHeaders = getListFromResultSet(rs);
//            rs.close();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
//            //设置response相应属性，设置为下载
//            response.setContentType("application/x-msdownload");
//            response.setHeader("Content-Disposition", "attachment;filename=" + name);
//            //获得response中的输出流
//            OutputStream out = null;
//            out = response.getOutputStream();
//            //excel表格第一行标题，本例中不采用poi或jxl，只对每个单元属性后面添//加\t，每行结束加\n。这是excel文件的基本属性。
//            String head = "序号\t订单号\t商品名称\t商品数量\t单价\t下单时间\t用户\t用户手机号码\t社区\t收货人名称\t收货人联系电话\t收货人地址\t积分抵扣\t配送时间\t订单状态\t支付宝支付\t微信支付\t钱包支付\t商家\t支付流水号\n";
//            int id = 0;
//            out.write(head.getBytes("GBK"));
//            if (UtilValidate.isNotEmpty(orderHeaders)) {
//                for (Map<String, Object> orderHeader : orderHeaders) {
//                    id++;
//                    StringBuffer conBuffer = new StringBuffer();
//                    conBuffer.append(id + "\t");
//                    conBuffer.append(orderHeader.get("订单号") + "\t");
//                    conBuffer.append(orderHeader.get("商品名称") + "\t");
//                    conBuffer.append(orderHeader.get("商品数量") + "\t");
//                    conBuffer.append(orderHeader.get("单价") + "\t");
//                    conBuffer.append(orderHeader.get("下单时间") + "\t");
//                    conBuffer.append(orderHeader.get("用户") + "\t");
//                    conBuffer.append(orderHeader.get("用户手机号码") + "\t");
//                    //edit by 20160504:配送方式-->自提：根据用户绑定的社区信息，获取社区名称，可能会有多个社区名称的情况
//                    //配送方式 -->快递配送(分为周末配送，工作日配送)：根据订单社区信息获取社区名称
//                    if ("ZMPS".equals(orderHeader.get("配送方式")) || "GZRPS".equals(orderHeader.get("配送方式"))) {
//                        conBuffer.append(((orderHeader.get("社区") != null) ? orderHeader.get("社区").toString().replace("･", "·") : "") + "\t");
//                    } else if ("SMZT".equals(orderHeader.get("配送方式"))) {
//                        List<String> partyCommunitys = EntityUtil.getFieldListFromEntityList(delegator.findList("PartyCommunity",
//                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, orderHeader.get("userid")),
//                                UtilMisc.toSet("communityId"), null, null, false), "communityId", true);
//                        List<String> communityNames = EntityUtil.getFieldListFromEntityList(delegator.findList("Community",
//                                EntityCondition.makeCondition(EntityCondition.makeCondition("communityId", EntityOperator.IN, partyCommunitys)),
//                                UtilMisc.toSet("name"), null,
//                                null, false), "name", true);
//
//                        String communityName = UtilValidate.isNotEmpty(communityNames) ? StringUtil.join(communityNames, ",").replace("･", "·") : "";
//                        conBuffer.append(communityName + "\t");
//                    } else {
//                        conBuffer.append("\t");
//                    }
//                    if (!type.equals("virtual")) {
//                        conBuffer.append(((orderHeader.get("收货人名称") != null) ? orderHeader.get("收货人名称") : "") + "\t");
//                    } else {
//                        conBuffer.append("\t");
//                    }
//                    if (!type.equals("virtual")) {
//                        conBuffer.append(((orderHeader.get("收货人联系电话") != null) ? orderHeader.get("收货人联系电话") : "") + "\t");
//                    } else {
//                        conBuffer.append("\t");
//                    }
//                    if (!type.equals("virtual")) {
//                        conBuffer.append(((orderHeader.get("收货人地址") != null) ? orderHeader.get("收货人地址") : "") + "\t");
//                    } else {
//                        conBuffer.append("\t");
//                    }
//                    conBuffer.append(((orderHeader.get("积分抵扣") != null) ? orderHeader.get("积分抵扣") : "0") + "\t");
//                    conBuffer.append(((orderHeader.get("配送时间") != null) ? orderHeader.get("配送时间") : "") + "\t");
//                    conBuffer.append(orderHeader.get("订单状态") + "\t");
//                    conBuffer.append(((orderHeader.get("支付宝支付") != null) ? orderHeader.get("支付宝支付") : "0") + "\t");
//                    conBuffer.append(((orderHeader.get("微信支付") != null) ? orderHeader.get("微信支付") : "0") + "\t");
//                    conBuffer.append(((orderHeader.get("钱包支付") != null) ? orderHeader.get("钱包支付") : "0") + "\t");
//                    conBuffer.append(((orderHeader.get("商家") != null) ? orderHeader.get("商家") : "") + "\t");
//                    conBuffer.append(((orderHeader.get("支付流水号") != null) ? orderHeader.get("支付流水号") : "") + "\n");
//                    String content = conBuffer.toString();
//                    out.write(content.getBytes("GBK"));
//                }
//                out.flush();
//                out.close();
//            }
//        }
//        return "success";
//    }
    
    public static String exportReturn(HttpServletRequest request, HttpServletResponse response) throws IOException, GenericEntityException {
        String returnIds = request.getParameter("ids");
        String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".xls";
        String name = new String(fileName.getBytes("gbk"), "iso8859-1");
        Locale locale = UtilHttp.getLocale(request);
        
        if (UtilValidate.isEmpty(returnIds)) {
            HttpSession session = request.getSession();
            GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
            String searchFunction = request.getParameter("searchfunction");
            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
            paramMap.put("VIEW_SIZE", "-1");
            paramMap.put("userLogin", userLogin);
            paramMap.remove("searchfunction");
            returnIds = exportAllOrder(dispatcher, searchFunction, paramMap);
        }
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> returnMapIdsList = UtilMisc.toListArray(returnIds.split(","));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-Disposition", "attachment;filename=" + name);
        OutputStream out = null;
        
        out = response.getOutputStream();
        String head = "序号\t商品\t实付金额\t数量\t退单时间\t用户\t收货人\t电话\t申请退款金额\t实退金额\t退单状态\t商家\n";
        int id = 0;
        out.write(head.getBytes("gbk"));
        GenericValue orderReturn = null;
        GenericValue returnHeader = null;
        if (UtilValidate.isNotEmpty(returnMapIdsList)) {
            for (String returnMap : returnMapIdsList) {
                id++;
                String[] temp = returnMap.split("-");
                String returnId = temp[0];
                String returnItemSeqId = temp[1];
                orderReturn = delegator.findOne("ReturnItem", UtilMisc.toMap("returnId", returnId, "returnItemSeqId", returnItemSeqId), false);
                returnHeader = delegator.findOne("ReturnHeader", UtilMisc.toMap("returnId", returnId), false);
                String orderId = orderReturn.getString("orderId");
                //商品
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderReturn.getString("productId")));
                //实退金额
                BigDecimal refundMoney = orderReturn.getBigDecimal("actualPaymentMoney") == null ? BigDecimal.ZERO : orderReturn.getBigDecimal("actualPaymentMoney");
                GenericValue orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId)).get(0);
                //实付金额
                BigDecimal orderPaymentMoney = orderItem.getBigDecimal("unitPrice").multiply(orderReturn.getBigDecimal("returnQuantity"));
                //数量
                BigDecimal actualPaymentNum = orderReturn.getBigDecimal("returnQuantity");
                //购买人信息
                Map<String, Object> purchaseInfo = OrderReadHelper.getOrderPurchaseInfo(delegator, orderId);
                //退单状态
                String returnStatus = (String) orderReturn.getRelatedOneCache("StatusItem").get("description", locale);
                //商家
                String businessName = OrderReadHelper.getOrderBusinessName(delegator, orderId);
                StringBuffer conBuffer = new StringBuffer();
                conBuffer.append(id + "\t");
                conBuffer.append(outPutString(product.getString("productName")) + "\t");
                conBuffer.append(outPutString(orderPaymentMoney + "") + "\t");
                conBuffer.append(outPutString(actualPaymentNum + "") + "\t");
                conBuffer.append(outPutString(sdf.format(orderReturn.getTimestamp("createdStamp"))) + "\t");
                conBuffer.append(outPutString(purchaseInfo.get("name") + "") + "\t");
                conBuffer.append(outPutString(purchaseInfo.get("toName") + "") + "\t");
                conBuffer.append(outPutString(purchaseInfo.get("mobilePhone") + "") + "\t");
                conBuffer.append(orderReturn.getBigDecimal("applyMoney").intValue() + "\t");
                conBuffer.append(refundMoney + "\t");
                conBuffer.append(returnStatus + "\t");
                conBuffer.append(businessName + "\n");
                String content = conBuffer.toString();
                out.write(content.getBytes("gbk"));
            }
            out.flush();
            out.close();
        }
        
        return "success";
    }
    
    /*去掉特殊字符*/
    public static String outPutString(String str) {
        StringBuffer sbf = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '\t' && str.charAt(i) != '\n' && str.charAt(i) != '\r') {
                sbf.append(str.charAt(i));
            }
        }
        return sbf.toString();
    }
    
    /**
     * 申请退款/退货 （实物） Add By AlexYao 2016/01/25
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> physicalRefund(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        List<GenericValue> toBeStore = new ArrayList<GenericValue>();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String refundType = (String) context.get("refundType");
        BigDecimal refundMoney = (BigDecimal) context.get("refundMoney");
        String refundReason = (String) context.get("refundReason");
        String contentIds = (String) context.get("contentIds");
        String enumId = (String) context.get("enumId");
        String productId = (String) context.get("productId");
        
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        orderHeader.set("isAllowReturn", "1");
        toBeStore.add(orderHeader);

        GenericValue userLogin = null;
        List<GenericValue> orderItems = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
            List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderId, "productId", productId));
            if (UtilValidate.isNotEmpty(returnItems)) {
                GenericValue checkReturnItem = returnItems.get(0);
                String returnItmeStatus = checkReturnItem.getString("statusId");
                //如果退款/退货订单审核通过 则需要将订单状态修改
                if ("RETURN_WAITFEFUND".equals(returnItmeStatus)) {
                    List<GenericValue> updateOrderItmes = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId, "productId", productId));
                    if (UtilValidate.isNotEmpty(updateOrderItmes)) {
                        GenericValue updateOrderItem = updateOrderItmes.get(0);
                        updateOrderItem.set("statusId", "ORDER_RETURNED");//STATUS_ID
                        updateOrderItem.store();
                    }
                }
                
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        //判断当前订单是否是本人
        String partyId = userLogin.getString("partyId");
        try {
            GenericValue orderRole = delegator.findByPrimaryKey("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", partyId, "roleTypeId", "PLACING_CUSTOMER"));
            if (UtilValidate.isEmpty(orderRole)) {
                return ServiceUtil.returnError("当前订单不是申请人发起");
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        if (UtilValidate.isNotEmpty(userLogin) && UtilValidate.isNotEmpty(orderItems)) {
            String returnId = delegator.getNextSeqId("ReturnHeader");
            GenericValue returnHeader = delegator.makeValue("ReturnHeader");
            returnHeader.set("returnId", returnId);
            returnHeader.set("statusId", "RETURN_WAITEXAMINE");
            returnHeader.set("createdBy", userLoginId);
            returnHeader.set("fromPartyId", userLogin.get("partyId"));
            returnHeader.set("orderId", orderId);
            
            returnHeader.set("entryDate", new Timestamp(System.currentTimeMillis()));
            toBeStore.add(returnHeader);
            GenericValue returnItem = delegator.makeValue("ReturnItem");
            returnItem.set("returnId", returnId);
            returnItem.set("returnItemSeqId", "00001");
            returnItem.set("orderId", orderId);
            returnItem.set("productId", productId);
            returnItem.set("statusId", "RETURN_WAITEXAMINE");
            returnItem.set("returnQuantity", orderItems.get(0).get("quantity"));
            returnItem.set("returnPrice", refundMoney);
            returnItem.set("applyMoney", refundMoney);
            returnItem.set("returnReasonId", enumId);
            returnItem.set("returnReason", refundReason);
            returnItem.set("applyTime", new Timestamp(System.currentTimeMillis()));
            if ("product".equals(refundType)) {
                returnItem.set("returnType", "0");
                
            } else {
                returnItem.set("returnType", "1");
            }
            toBeStore.add(returnItem);
            if (UtilValidate.isNotEmpty(contentIds)) {
                int index = 1;
                String[] contentIdList = contentIds.split(",");
                for (String contentId : contentIdList) {
                    GenericValue returnContent = delegator.makeValue("ReturnContent");
                    returnContent.set("returnId", returnId);
                    returnContent.set("returnContentSeqId", "0000" + String.valueOf(index++));
                    returnContent.set("contentId", contentId);
                    toBeStore.add(returnContent);
                }
            }
            GenericValue returnOperateLog = delegator.makeValue("ReturnOperateLog");
            returnOperateLog.set("id", delegator.getNextSeqId("ReturnOperateLog"));
            returnOperateLog.set("returnId", returnId);
            if ("product".equals(refundType)) {
                returnOperateLog.set("operateType", "申请退货");
            } else {
                returnOperateLog.set("operateType", "申请退款");
            }
            returnOperateLog.set("operator", userLoginId);
            returnOperateLog.set("operateTime", new Timestamp(System.currentTimeMillis()));
            toBeStore.add(returnOperateLog);
            GenericValue returnStatus = delegator.makeValue("ReturnStatus");
            returnStatus.set("returnStatusId", delegator.getNextSeqId("ReturnStatus"));
            returnStatus.set("statusId", "RETURN_WAITEXAMINE");
            returnStatus.set("returnId", returnId);
            returnStatus.set("returnItemSeqId", "00001");
            returnStatus.set("changeByUserLoginId", userLoginId);
            returnStatus.set("statusDatetime", new Timestamp(System.currentTimeMillis()));
            toBeStore.add(returnStatus);
            try {
                delegator.storeAll(toBeStore);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            successResult.put("resultData", true);
            successResult.put("returnId", returnId);
        } else {
            successResult.put("resultData", false);
        }
        return successResult;
    }
    
    /**
     * 实物退款/退货详情 Add By AlexYao 2016/01/25
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> physicalRefundDetail(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        Map resultData = new HashMap();
        List<GenericValue> returnItem = null;
        List<GenericValue> orderItem = null;
        try {
            returnItem = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderId));
            orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(returnItem)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resultData.put("productRefundId", returnItem.get(0).get("returnId"));
            resultData.put("refundTime", sdf.format(returnItem.get(0).get("createdStamp")));
            if ("RETURN_WAITEXAMINE".equals(returnItem.get(0).get("statusId"))) {
                resultData.put("statusId", "notAudited");
                resultData.put("status", "待审核");
            } else if ("RETURN_WAITFEFUND".equals(returnItem.get(0).get("statusId"))) {
                resultData.put("statusId", "notRefunded");
                resultData.put("status", "待退款");
            } else if ("RETURN_COMPLETED".equals(returnItem.get(0).get("statusId"))) {
                resultData.put("statusId", "hasRefuned");
                resultData.put("status", "已完成");
            } else if ("RETURN_REJECTAPPLY".equals(returnItem.get(0).get("statusId"))) {
                resultData.put("statusId", "rejectApplication");
                resultData.put("status", "拒绝申请");
            }
            if ("1".equals(returnItem.get(0).get("returnType"))) {
                resultData.put("refundType", "money");
            } else {
                resultData.put("refundType", "product");
            }
            resultData.put("productName", orderItem.get(0).get("itemDescription"));
            resultData.put("number", returnItem.get(0).get("returnQuantity"));
            resultData.put("refundReason", returnItem.get(0).get("returnReason"));
            resultData.put("refundMoney", returnItem.get(0).get("applyMoney"));
            List<GenericValue> returnContents = null;
            try {
                returnContents = delegator.findByAnd("ReturnContent", UtilMisc.toMap("returnId", returnItem.get(0).get("returnId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            List list = new ArrayList();
            if (UtilValidate.isNotEmpty(returnContents)) {
                for (GenericValue returnContent : returnContents) {
                    GenericValue content = null;
                    try {
                        content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", returnContent.get("contentId")));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(content)) {
                        GenericValue dataResource = null;
                        try {
                            dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        if (UtilValidate.isNotEmpty(dataResource) && UtilValidate.isNotEmpty(dataResource.get("objectInfo"))) {
                            list.add(dataResource.get("objectInfo"));
                        }
                    }
                }
            }
            resultData.put("refundImages", list);
        }
        successResult.put("resultData", resultData);
        return successResult;
    }
    
    /**
     * 申请退款 （虚拟） Add By AlexYao 2016/01/26
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> virtualRefund(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        List<GenericValue> toBeStore = new ArrayList<GenericValue>();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String ticketIds = (String) context.get("ticketIds");
        String refundReason = (String) context.get("refundReason");
        BigDecimal refundMoney = (BigDecimal) context.get("refundMoney");
        GenericValue userLogin = null;
        List<GenericValue> orderItems = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin) && UtilValidate.isNotEmpty(orderItems)) {
            String returnId = delegator.getNextSeqId("ReturnHeader");
            GenericValue returnHeader = delegator.makeValue("ReturnHeader");
            returnHeader.set("returnId", returnId);
            returnHeader.set("statusId", "RETURN_WAITEXAMINE");
            returnHeader.set("createdBy", userLoginId);
            returnHeader.set("entryDate", new Timestamp(System.currentTimeMillis()));
            toBeStore.add(returnHeader);
            GenericValue returnItem = delegator.makeValue("ReturnItem");
            returnItem.set("returnId", returnId);
            returnItem.set("returnItemSeqId", "00001");
            returnItem.set("productId", orderItems.get(0).get("productId"));
            returnItem.set("orderId", orderId);
            returnItem.set("statusId", "RETURN_WAITEXAMINE");
            String[] ticketIdList = ticketIds.split(",");
            returnItem.set("returnQuantity", new BigDecimal(ticketIdList.length));
            returnItem.set("returnPrice", refundMoney);
            returnItem.set("applyMoney", refundMoney);
            returnItem.set("returnReason", refundReason);
            returnItem.set("applyTime", new Timestamp(System.currentTimeMillis()));
            returnItem.set("returnType", "1");
            toBeStore.add(returnItem);
            if (UtilValidate.isNotEmpty(ticketIds)) {
                int index = 1;
                for (String ticketId : ticketIdList) {
                    GenericValue returnTicket = delegator.makeValue("ReturnTicket");
                    returnTicket.set("returnId", returnId);
                    returnTicket.set("returnTicketSeqId", "0000" + String.valueOf(index++));
                    returnTicket.set("ticketId", ticketId);
                    toBeStore.add(returnTicket);
                    GenericValue ticket = null;
                    try {
                        ticket = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId", ticketId));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(ticket)) {
                        ticket.set("ticketStatus", "notAudited");
                        toBeStore.add(ticket);
                    }
                }
            }
            GenericValue returnOperateLog = delegator.makeValue("ReturnOperateLog");
            returnOperateLog.set("id", delegator.getNextSeqId("ReturnOperateLog"));
            returnOperateLog.set("returnId", returnId);
            returnOperateLog.set("operateType", "申请退款");
            returnOperateLog.set("operator", userLoginId);
            returnOperateLog.set("operateTime", new Timestamp(System.currentTimeMillis()));
            toBeStore.add(returnOperateLog);
            GenericValue returnStatus = delegator.makeValue("ReturnStatus");
            returnStatus.set("returnStatusId", delegator.getNextSeqId("ReturnStatus"));
            returnStatus.set("statusId", "RETURN_WAITEXAMINE");
            returnStatus.set("returnId", returnId);
            returnStatus.set("returnItemSeqId", "00001");
            returnStatus.set("changeByUserLoginId", userLoginId);
            returnStatus.set("statusDatetime", new Timestamp(System.currentTimeMillis()));
            toBeStore.add(returnStatus);
            try {
                delegator.storeAll(toBeStore);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            successResult.put("resultData", true);
        } else {
            successResult.put("resultData", false);
        }
        return successResult;
    }
    
    /**
     * 虚拟退款详情 Add By AlexYao 2016/01/26
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> virtualRefundDetail(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String ticketId = (String) context.get("ticketId");
        Map resultData = new HashMap();
        List<GenericValue> returnTicket = null;
        List<GenericValue> orderItem = null;
        try {
            returnTicket = delegator.findByAnd("ReturnTicket", UtilMisc.toMap("ticketId", ticketId));
            orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(returnTicket) && UtilValidate.isNotEmpty(orderItem)) {
            List<GenericValue> returnItems = null;
            try {
                returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("returnId", returnTicket.get(0).get("returnId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(returnItems)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                resultData.put("productRefundId", returnTicket.get(0).get("returnId"));
                GenericValue product = null;
                List<GenericValue> productActivityGoods = null;
                try {
                    product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get(0).get("productId")));
                    productActivityGoods = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("productId", orderItem.get(0).get("productId"), "activityId", orderItem.get(0).get("activityId")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                String smallImageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                resultData.put("imgUrl", smallImageUrl);
                resultData.put("productName", orderItem.get(0).get("itemDescription"));
                resultData.put("validityDate", sdf.format(productActivityGoods.get(0).get("virtualProductEndDate")));
                List rLifeTickets = new ArrayList();
                List<GenericValue> returnTickets = new ArrayList<GenericValue>();
                try {
                    returnTickets = delegator.findByAnd("ReturnTicket", UtilMisc.toMap("returnId", returnTicket.get(0).get("returnId")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                for (GenericValue returnTicket1 : returnTickets) {
                    GenericValue ticket = null;
                    try {
                        ticket = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId", returnTicket1.get("ticketId")));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    Map rLifeTicket = new HashMap();
                    if ("RETURN_WAITEXAMINE".equals(returnItems.get(0).get("statusId"))) {
                        rLifeTicket.put("statusId", "notAudited");
                        rLifeTicket.put("status", "待审核");
                    } else if ("RETURN_WAITFEFUND".equals(returnItems.get(0).get("statusId"))) {
                        rLifeTicket.put("statusId", "notRefunded");
                        rLifeTicket.put("status", "待退款");
                    } else if ("RETURN_COMPLETED".equals(returnItems.get(0).get("statusId"))) {
                        rLifeTicket.put("statusId", "hasRefuned");
                        rLifeTicket.put("status", "已退款");
                    } else if ("RETURN_REJECTAPPLY".equals(returnItems.get(0).get("statusId"))) {
                        rLifeTicket.put("statusId", "rejectApplication");
                        rLifeTicket.put("status", "拒绝申请");
                    }
                    rLifeTicket.put("index", ticket.get("ticketIndex"));
                    rLifeTicket.put("ticketId", ticketId);
                    rLifeTicket.put("ticketNo", ticket.get("ticketNo"));
                    rLifeTickets.add(rLifeTicket);
                }
                resultData.put("rLifeTickets", rLifeTickets);
                if ("1".equals(returnItems.get(0).get("returnType"))) {
                    resultData.put("refundType", "money");
                } else {
                    resultData.put("refundType", "product");
                }
                resultData.put("refundReason", returnItems.get(0).get("returnReason"));
                resultData.put("refundMoney", returnItems.get(0).get("applyMoney"));
            }
        }
        successResult.put("resultData", resultData);
        return successResult;
    }
    
    /**
     * yabiz商城券 Add By AlexYao 2016/01/26
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> rLifeTicket(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        Map resultData = new HashMap();
        List<GenericValue> orderItem = null;
        List<GenericValue> tickets = null;
        try {
            orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
            tickets = delegator.findByAnd("Ticket", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(tickets)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            GenericValue product = null;
            List<GenericValue> productActivityGoods = null;
            GenericValue productActivity = null;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get(0).get("productId")));
                productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", orderItem.get(0).get("activityId")));
                productActivityGoods = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("productId", orderItem.get(0).get("productId"), "activityId", orderItem.get(0).get("activityId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            String smallImageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            resultData.put("imgUrl", smallImageUrl);
            resultData.put("productName", orderItem.get(0).get("itemDescription"));
            resultData.put("validityDate", sdf.format(productActivityGoods.get(0).get("virtualProductEndDate")));
            List<Map> rLifeTickets = new ArrayList<Map>();
            for (GenericValue ticket : tickets) {
                Map map = new HashMap();
                map.put("index", ticket.get("ticketIndex"));
                map.put("ticketId", ticket.get("ticketId"));
                map.put("ticketNo", ticket.get("ticketNo"));
                if ("notUsed".equals(ticket.get("ticketStatus"))) {
                    map.put("statusId", "notUsed");
                    map.put("status", "未使用");
                } else if ("notAudited".equals(ticket.get("ticketStatus"))) {
                    map.put("statusId", "notAudited");
                    map.put("status", "待审核");
                } else if ("notRefunded".equals(ticket.get("ticketStatus"))) {
                    map.put("statusId", "notRefunded");
                    map.put("status", "待退款");
                } else if ("hasRefuned".equals(ticket.get("ticketStatus"))) {
                    map.put("statusId", "hasRefuned");
                    map.put("status", "已退款");
                } else if ("rejectApplication".equals(ticket.get("ticketStatus"))) {
                    map.put("statusId", "rejectApplication");
                    map.put("status", "拒绝申请");
                } else if ("hasUsed".equals(ticket.get("ticketStatus"))) {
                    map.put("statusId", "hasUsed");
                    map.put("status", "已使用");
                } else if ("expired".equals(ticket.get("ticketStatus"))) {
                    map.put("statusId", "expired");
                    map.put("status", "已过期");
                }
                if (UtilValidate.isNotEmpty(ticket.get("contentId"))) {
                    GenericValue content = null;
                    try {
                        content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", ticket.get("contentId")));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(content)) {
                        GenericValue dataResource = null;
                        try {
                            dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        if (UtilValidate.isNotEmpty(dataResource) && UtilValidate.isNotEmpty(dataResource.get("objectInfo"))) {
                            map.put("imgUrl", dataResource.get("objectInfo"));
                        }
                    }
                }
                rLifeTickets.add(map);
            }
            resultData.put("rLifeTickets", rLifeTickets);
            resultData.put("useTips", productActivity.get("activityDesc"));
        }
        successResult.put("resultData", resultData);
        return successResult;
    }
    
    /**
     * 查看物流 Add By AlexYao 2016/01/26
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> viewLogistics(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        Map resultData = new HashMap();
        List<GenericValue> orderItem = null;
        List<GenericValue> orderDelivery = null;
        try {
            orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
            orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(orderItem) && UtilValidate.isNotEmpty(orderDelivery)) {
            GenericValue product = null;
            List<GenericValue> logisticsCompanys = null;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get(0).get("productId")));
                logisticsCompanys = delegator.findByAnd("LogisticsCompany", UtilMisc.toMap("companyId", orderDelivery.get(0).get("deliveryCompany")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            String smallImageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            resultData.put("imgUrl", smallImageUrl);
            resultData.put("logisticCompany", logisticsCompanys.get(0).get("companyName"));
            resultData.put("logisticId", orderDelivery.get(0).get("logisticsNumber1"));
            if ("0".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "在途中");
            } else if ("1".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "已揽收");
            } else if ("2".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "疑难");
            } else if ("3".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "已签收");
            } else if ("4".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "退签");
            } else if ("5".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "同城派送中");
            } else if ("6".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "退回");
            } else if ("7".equals(orderDelivery.get(0).get("state"))) {
                resultData.put("status", "转单");
            } else {
                resultData.put("status", "待揽收");
            }
            List<GenericValue> deliveryItems = null;
            try {
                deliveryItems = delegator.findByAnd("DeliveryItem", UtilMisc.toMap("companyId", orderDelivery.get(0).get("deliveryCompany"), "logisticsNumber", orderDelivery.get(0).get("logisticsNumber1")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(deliveryItems)) {
                List list = new ArrayList();
                for (GenericValue deliveryItem : deliveryItems) {
                    list.add(UtilMisc.toMap("logisticInfo", UtilDateTime.timeStampToString(deliveryItem.getTimestamp("dateTime"), "yyyy-MM-dd HH:mm:ss", timeZone, locale) + " " + deliveryItem.get("description")));
                }
                resultData.put("logistics", list);
            }
        }
        successResult.put("resultData", resultData);
        return successResult;
    }
    
    /**
     * 根据状态获取生活券 add by Wcy 2016.01.27
     *
     * @return
     */
    public static Map<String, Object> getrLifeTicketByStatus(Delegator delegator, String productId, String orderId) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        Map<String, Object> ticketStatus = FastMap.newInstance();
        ticketStatus.put("notUsed", "待使用");
        ticketStatus.put("hasUsed", "已使用");
        ticketStatus.put("notAudited", "待审核");
        ticketStatus.put("notRefunded", "待退款");
        ticketStatus.put("hasRefuned", "已退款");
        ticketStatus.put("rejectApplication", "拒绝申请");
        ticketStatus.put("expired", "已过期");
        /** 定义团购券动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("TI", "Ticket");
        dynamicViewEntity.addAlias("TI", "index", "ticketIndex", null, null, null, null);
        dynamicViewEntity.addAlias("TI", "ticketId");
        dynamicViewEntity.addAlias("TI", "ticketNo");
        dynamicViewEntity.addAlias("TI", "statusId", "ticketStatus", null, null, null, null);
        dynamicViewEntity.addAlias("TI", "productId");
        dynamicViewEntity.addAlias("TI", "orderId");
        
        /** 查询字段 & 排序字段*/
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        
        fieldsToSelect.add("index");
        fieldsToSelect.add("ticketId");
        fieldsToSelect.add("ticketNo");
        fieldsToSelect.add("statusId");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("orderId");
        orderBy.add("index");
        
        try {
            /** 查询条件 */
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
            exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
            // exprs.add(EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, status));
            EntityCondition cond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
            
            //填充查询条件,查询字段，排序字段
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, cond, null, fieldsToSelect, orderBy, findOpts);
            //获取结果集
            List<GenericValue> tickets = pli.getCompleteList();
            List<Map<String, Object>> ticketList = FastList.newInstance();
            for (GenericValue v : tickets) {
                Map<String, Object> map = FastMap.newInstance();
                map.put("status", ticketStatus.get(v.getString("statusId")));
                map.put("index", v.get("index"));
                map.put("ticketId", v.get("ticketId"));
                map.put("ticketNo", v.get("ticketNo"));
                map.put("statusId", v.get("statusId"));
                ticketList.add(map);
            }
            result.put("ticketList", tickets);
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取生活券的所有状态 add by Wcy 2016.01.27
     *
     * @return
     */
    public static Map<String, Object> getrLifeTicketStatus(Delegator delegator, String productId, String orderId) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        try {
            /** 查询条件 */
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
            exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
            EntityCondition cond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
            
            /** 查询字段 & 排序字段 */
            Set<String> fieldsToSelect = FastSet.newInstance();
            List<String> orderBy = FastList.newInstance();
            fieldsToSelect.add("ticketStatus");
            orderBy.add("ticketIndex");
            List<GenericValue> tickets = delegator.findList("Ticket", cond, fieldsToSelect, orderBy, null, false);
            List<String> ticketList = FastList.newInstance();
            for (GenericValue v : tickets) {
                ticketList.add(v.getString("ticketStatus"));
            }
            result.put("ticketList", tickets);
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取抢购数据 Add By AlexYao 2016/01/27
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> orderProduct(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String userLoginId = (String) context.get("userLoginId");
        String activityId = (String) context.get("activityId");
        Map resultData = new HashMap();
        List<GenericValue> productActivityGoods = new ArrayList<GenericValue>();
        GenericValue productActivity = null;
        GenericValue userLogin = null;
        GenericValue integralPerMoney = null;
        GenericValue person = null;
        try {
            productActivityGoods = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));//活动与产品关联表
            productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));//活动主表
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));//积分抵现规则表
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(productActivityGoods) && UtilValidate.isNotEmpty(productActivity) && UtilValidate.isNotEmpty(userLogin)) {
            try {
                person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(person)) {
                resultData.put("telphone", person.get("mobile"));
            }
            resultData.put("partyId", userLogin.get("partyId"));
            String productId = productActivityGoods.get(0).getString("productId");
            GenericValue product = null;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));//产品主表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(product)) {
                //产品类型
                if (FINISHED_GOOD.equals(product.get("productTypeId"))) {
                    resultData.put("productType", "esse");
                } else if (VIRTUAL_GOOD.equals(product.get("productTypeId"))) {
                    resultData.put("productType", "fictitious");
                }
                //活动状态
                if (productActivity.getLong("activityQuantity") - productActivity.getLong("hasBuyQuantity") <= 0L) {
                    resultData.put("activityState", "clearout");
                } else if ("ACTY_AUDIT_PASS".equals(productActivity.get("activityAuditStatus"))) {
                    Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
                    if (nowTimestamp.after(productActivity.getTimestamp("activityStartDate")) && nowTimestamp.before(productActivity.getTimestamp("activityEndDate"))) {
                        resultData.put("activityState", "ongoing");
                    } else if (nowTimestamp.after(productActivity.getTimestamp("activityEndDate"))) {
                        resultData.put("activityState", "finish");
                    }
                }
                //产品id
                resultData.put("productId", product.get("productId"));
                //产品名称
                resultData.put("productTitle", product.get("productName"));
                //收货地址
                /*Map addressMap = new HashMap();
                GenericValue shipToParty = null;
                try {
                    shipToParty = delegator.findByPrimaryKeyCache("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                List<GenericValue> profiledefs = null;
                try {
                    profiledefs = delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", userLogin.get("partyId")));//默认地址表
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                List<GenericValue> shippingContactMechList = (List<GenericValue>) ContactHelper.getContactMech(shipToParty, "SHIPPING_LOCATION", "POSTAL_ADDRESS", false);//获取联系地址
                if (UtilValidate.isNotEmpty(shippingContactMechList)) {
                    if (UtilValidate.isNotEmpty(profiledefs)) {
                        for (GenericValue shippingContactMech : shippingContactMechList) {
                            if (profiledefs.get(0).get("defaultShipAddr").equals(shippingContactMech.get("contactMechId"))) {
                                addressMap.put("id", shippingContactMech.get("contactMechId"));
                                addressMap.put("name", shippingContactMech.get("toName"));
                                GenericValue stateProvince = null;
                                GenericValue city = null;
                                GenericValue county = null;
                                try {
                                    stateProvince = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", shippingContactMech.get("stateProvinceGeoId")));//省
                                    city = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", shippingContactMech.get("city")));//市
                                    county = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", shippingContactMech.get("countyGeoId")));//区
                                } catch (GenericEntityException e) {
                                    e.printStackTrace();
                                }
                                addressMap.put("location", (String) stateProvince.get("geoName", locale) + (String) city.get("geoName", locale) + (String) county.get("geoName", locale) + shippingContactMech.get("address1"));
                                addressMap.put("telphone", shippingContactMech.get("mobilePhone"));
                            }
                        }
                    } else {
                        addressMap.put("id", shippingContactMechList.get(0).get("contactMechId"));
                        addressMap.put("name", shippingContactMechList.get(0).get("toName"));
                        GenericValue stateProvince = null;
                        GenericValue city = null;
                        GenericValue county = null;
                        try {
                            stateProvince = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", shippingContactMechList.get(0).get("stateProvinceGeoId")));//省
                            city = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", shippingContactMechList.get(0).get("city")));//市
                            county = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", shippingContactMechList.get(0).get("countyGeoId")));//区
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        addressMap.put("location", (String) stateProvince.get("geoName", locale) + (String) city.get("geoName", locale) + (String) county.get("geoName", locale) + shippingContactMechList.get(0).get("address1"));
                        addressMap.put("telphone", shippingContactMechList.get(0).get("mobilePhone"));
                    }
                }
                resultData.put("address",addressMap);*/
                //是否自提
                if ("SHIPMENT_OWN".equals(productActivityGoods.get(0).get("shipmentType"))) {
                    resultData.put("isPickedUp", true);
                } else {
                    resultData.put("isPickedUp", false);
                }
                if ("SEC_KILL".equals(productActivity.get("activityType"))) {
                    //秒杀价
                    resultData.put("curPrice", productActivity.get("productPrice"));
                } else if ("GROUP_ORDER".equals(productActivity.get("activityType"))) {
                    //阶梯价
                    List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
                    try {
                        productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId), UtilMisc.toList("orderQuantity"));//阶梯价规则表
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    List<Map> priceList = new ArrayList<Map>();
                    if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                        for (GenericValue productGroupOrderRule : productGroupOrderRules) {
                            Map price = new HashMap();
                            price.put("people", productGroupOrderRule.get("orderQuantity"));
                            price.put("price", productGroupOrderRule.get("orderPrice"));
                            priceList.add(price);
                        }
                    }
                    resultData.put("priceList", priceList);
                }
                //查询该活动已下单数量
                Long buyNum = 0L;
                if (FINISHED_GOOD.equals(product.get("productTypeId"))) {
                    DynamicViewEntity dve = new DynamicViewEntity();
                    dve.addMemberEntity("OHR", "OrderHeader");
                    dve.addMemberEntity("ORE", "OrderRole");
                    dve.addMemberEntity("OIM", "OrderItem");
                    dve.addViewLink("OHR", "ORE", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
                    dve.addViewLink("OHR", "OIM", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
                    dve.addAlias("OHR", "orderId", "orderId", null, true, true, null);
                    dve.addAlias("OHR", "statusId");
                    dve.addAlias("ORE", "partyId");
                    dve.addAlias("ORE", "roleTypeId");
                    dve.addAlias("OIM", "activityId");
                    dve.addAlias("OIM", "quantity");
                    List<EntityCondition> entityConditionList = FastList.newInstance();
                    entityConditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, UtilMisc.toList("ORDER_WAITPAY", "ORDER_CANCELLED", "ORDER_RETURNED")));
                    entityConditionList.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
                    entityConditionList.add(EntityCondition.makeCondition("roleTypeId", "PLACING_CUSTOMER"));
                    entityConditionList.add(EntityCondition.makeCondition("activityId", activityId));
                    EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
                    EntityListIterator eli = null;
                    // do the lookup
                    try {
                        eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, findOpts);
                        List<GenericValue> resultList = eli.getCompleteList();
                        eli.close();
                        if (UtilValidate.isNotEmpty(resultList)) {
                            for (GenericValue result : resultList) {
                                buyNum += result.getBigDecimal("quantity").longValue();
                            }
                        }
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                } else if (VIRTUAL_GOOD.equals(product.get("productTypeId"))) {
                    List<GenericValue> tickets = new ArrayList<GenericValue>();
                    List<EntityCondition> ticketConditiona = FastList.newInstance();
                    ticketConditiona.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
                    ticketConditiona.add(EntityCondition.makeCondition("activityId", activityId));
                    ticketConditiona.add(EntityCondition.makeCondition("ticketStatus", EntityOperator.IN, UtilMisc.toList("notUsed", "hasUsed", "notAudited", "notRefunded", "rejectApplication", "expired")));
                    try {
                        tickets = delegator.findList("Ticket", EntityCondition.makeCondition(ticketConditiona, EntityOperator.AND), null, null, null, false);
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(tickets)) {
                        buyNum += Long.valueOf(tickets.size());
                    }
                }
                //已售数量
                Long soldNum = productActivity.getLong("hasBuyQuantity");
                resultData.put("soldNum", soldNum);
                //限制购买数量
                resultData.put("limitNumber", productActivity.getLong("limitQuantity") - buyNum);
                //最多使用积分
                resultData.put("maxUseScore", productActivity.get("scoreValue"));
                //积分抵现规则
                resultData.put("scoreRatio", integralPerMoney.get("integralValue"));
                GenericValue partyScore = null;
                GenericValue partyAccount = null;
                try {
                    partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", userLogin.get("partyId")));//会员积分表
                    partyAccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", userLogin.get("partyId")));//会员余额表
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                Long score = 0L;
                BigDecimal balance = BigDecimal.ZERO;
                if (UtilValidate.isNotEmpty(partyScore) && UtilValidate.isNotEmpty(partyScore.get("scoreValue"))) {
                    score = partyScore.getLong("scoreValue");
                }
                if (UtilValidate.isNotEmpty(partyAccount) && UtilValidate.isNotEmpty(partyAccount.get("amount"))) {
                    balance = partyAccount.getBigDecimal("amount");
                }
                //剩余积分
                resultData.put("score", score);
                //余额
                resultData.put("balance", balance);
                //产品库存
                List<GenericValue> productFacilitys = new ArrayList<GenericValue>();
                try {
                    productFacilitys = delegator.findByAnd("ProductFacility", UtilMisc.toMap("productId", productId));//产品库存场所
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                int productSurplusNumber = 0;
                String productStoreId = "10000";
                if (UtilValidate.isNotEmpty(productFacilitys)) {
                    List<GenericValue> inventoryItems = new ArrayList<GenericValue>();
                    List<GenericValue> productStoreFacilitys = new ArrayList<GenericValue>();
                    try {
                        inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId, "facilityId", productFacilitys.get(0).get("facilityId")));//库存明细
                        productStoreFacilitys = delegator.findByAnd("ProductStoreFacility", UtilMisc.toMap("facilityId", productFacilitys.get(0).get("facilityId")));//库存场所与店铺的关联
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(inventoryItems)) {
//                        BigDecimal occupiedQuantityTotal = BigDecimal.ZERO;
//                        if(UtilValidate.isNotEmpty(inventoryItems.get(0).getBigDecimal("occupiedQuantityTotal"))){
//                            occupiedQuantityTotal = inventoryItems.get(0).getBigDecimal("occupiedQuantityTotal");
//                        }
                        productSurplusNumber = inventoryItems.get(0).getBigDecimal("accountingQuantityTotal").intValue();
                    }
                    if (UtilValidate.isNotEmpty(productStoreFacilitys)) {
                        productStoreId = productStoreFacilitys.get(0).getString("productStoreId");
                    }
                }
                resultData.put("productSurplusNumber", productSurplusNumber);
                resultData.put("productStoreId", productStoreId);
                //活动剩余
                Long occupiedQuantityTotal = 0L;
                if (UtilValidate.isNotEmpty(productActivity.getLong("occupiedQuantityTotal"))) {
                    occupiedQuantityTotal = productActivity.getLong("occupiedQuantityTotal");
                }
                resultData.put("activitySurplusNumber", productActivity.getLong("activityQuantity") - productActivity.getLong("hasBuyQuantity") - occupiedQuantityTotal);
            }
        }
        successResult.put("resultData", resultData);
        return successResult;
    }
    
    /**
     * 取消订单  Add By AlexYao 2016/02/01
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> autoCancelOrder(DispatchContext ctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Delegator delegator = ctx.getDelegator();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        
        // get the stored order id from the session
        String orderId = (String) context.get("orderId");
        if (UtilValidate.isNotEmpty(orderId)) {
            GenericValue orderHeader = null;
            try {
                orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            //排除没有stuatsID的情况
            if (UtilValidate.isNotEmpty(orderHeader) && UtilValidate.isNotEmpty(orderHeader.getString("statusId"))) {
                if (!"ORDER_WAITPAY".equals(orderHeader.getString("statusId"))) {
                    successResult.put("resultData", true);
                    return successResult;
                }
            }
            // attempt to start a transaction
            boolean beganTransaction = false;
            try {
                beganTransaction = TransactionUtil.begin();
            } catch (GenericTransactionException gte) {
                Debug.logError(gte, "Unable to begin transaction", module);
            }
            List<GenericValue> orderItems = null;
            GenericValue productActivity = null;
            List<GenericValue> orderRoles = null;
            List<GenericValue> orderAttributes = null;
            try {
                orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(orderItems)) {
                    productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", orderHeader.get("activityId")));
                }
                orderRoles = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", "PLACING_CUSTOMER"));
                orderAttributes = delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            //恢复活动剩余  by AlexYao 2016/02/01
            if (UtilValidate.isNotEmpty(productActivity)) {
                Long occupiedQuantityTotal = productActivity.getLong("occupiedQuantityTotal") == null ? 0L : productActivity.getLong("occupiedQuantityTotal");
                Long quantity = orderItems.get(0).getBigDecimal("quantity") == null ? 0L : orderItems.get(0).getBigDecimal("quantity").longValue();
                productActivity.set("occupiedQuantityTotal", occupiedQuantityTotal - quantity);
                try {
                    delegator.store(productActivity);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                //拼团订单的处理，orderId, activityId、productId
                try {
                    List<GenericValue> togetherGroupOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("orderId", orderId));
                    if (UtilValidate.isNotEmpty(togetherGroupOrders)) {
                        GenericValue togetherGroupOrder = togetherGroupOrders.get(0);
                        String togetherId = togetherGroupOrder.getString("togetherId");
                        delegator.removeByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId, "orderId", orderId));
                    }
                    
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                
            }
            //恢复积分
      /*  try {
            List<GenericValue> orderAdjustments  = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId,"orderAdjustmentTypeId","INTEGRAL_ADJUESTMENT"));
            if(UtilValidate.isNotEmpty(orderAdjustments)){
                BigDecimal amount = BigDecimal.ZERO;
                for (int i = 0; i < orderAdjustments.size(); i++) {
                    GenericValue orderAdjustment = orderAdjustments.get(i);
                    amount = amount.add(orderAdjustment.getBigDecimal("recurringAmount"));
                }
                String member_id = "";
                String mall_id = "";
                if(UtilValidate.isNotEmpty(orderAttributes)){
                    for (int i = 0; i < orderAttributes.size(); i++) {
                        GenericValue orderAttrs = orderAttributes.get(i);
                        if(orderAttrs.getString("attrName").equals("member_id")){
                            member_id =   orderAttrs.getString("attrValue");
                        }
                        if(orderAttrs.getString("attrName").equals("mall_id")){
                            mall_id =   orderAttrs.getString("attrValue");
                        }
                    }
                }
                if(amount.compareTo(BigDecimal.ZERO)>0){
                    String amoutStr = "-"+amount.intValue();
                    Map<String, Object> scoreRes = dispatcher.runSync("kaide-consumeIntegral", UtilMisc.toMap("member_id", member_id, "mall_id", mall_id, "integral", amoutStr, "description", "订单取消积分返还，订单号:"+orderId, "merchant_id", mall_id, "token", ""));
                    Map retData = new Gson().fromJson((String) scoreRes.get("result"), Map.class);
                    if (UtilValidate.isNotEmpty(retData)) {
                        int result = ((Double) retData.get("result")).intValue();
                        if (result != 1) {
                            return ServiceUtil.returnError("积分扣减失败：member_id:" + member_id + "金额：" + amount );
                        }
                    } else {
                        return ServiceUtil.returnError("积分扣减失败：member_id:" + member_id + "金额：" + amount);
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }*/
            
            //恢复库存  by AlexYao 2016/02/01
            if (UtilValidate.isNotEmpty(orderItems)) {
                for (int i = 0; i < orderItems.size(); i++) {
                    GenericValue orderItem = orderItems.get(i);
                    List<GenericValue> inventoryItems = null;
                    try {
                        inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", orderItem.get("productId")));//库存明细
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(inventoryItems)) {
                        //锁定库存减少
                        BigDecimal lockQuantityTotal = (inventoryItems.get(0).getBigDecimal("lockQuantityTotal")) == null ? BigDecimal.ZERO : inventoryItems.get(0).getBigDecimal("lockQuantityTotal");
                        if (lockQuantityTotal.compareTo(BigDecimal.ZERO) > 0) {
                            inventoryItems.get(0).set("lockQuantityTotal", lockQuantityTotal.subtract(orderItem.getBigDecimal("quantity")));
                            try {
                                inventoryItems.get(0).store();
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            
            //恢复代金劵
            try {
                List<GenericValue> couponList = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId, "orderAdjustmentTypeId", "TICKET_ADJUESTMENT"));
                if (UtilValidate.isNotEmpty(couponList)) {
                    for (int i = 0; i < couponList.size(); i++) {
                        GenericValue couponAdjust = couponList.get(i);
                        String ticketId = couponAdjust.getString("sourceReferenceId");
                        GenericValue ticket = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId", ticketId));
                        ticket.set("ticketStatus", "notUsed");
                        delegator.store(ticket);
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            //恢复优惠劵
            try {
                List<GenericValue> couponList = delegator.findByAnd("OrderAdjustment", UtilMisc.toMap("orderId", orderId, "orderAdjustmentTypeId", "COUPON_ADJUESTMENT"));
                if (UtilValidate.isNotEmpty(couponList)) {
                    for (int i = 0; i < couponList.size(); i++) {
                        GenericValue couponAdjust = couponList.get(i);
                        String coponCodeId = couponAdjust.getString("sourceReferenceId");
                        GenericValue couponCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId", coponCodeId));
                        couponCode.set("promoCodeStatus", "G");
                        delegator.store(couponCode);
                        GenericValue productPromoCoupon = couponCode.getRelatedOne("ProductPromoCoupon");
                        //优惠劵Code标注已使用、优惠劵更新order数
                        if (UtilValidate.isNotEmpty(productPromoCoupon)) {
                            
                            Long orderCount = productPromoCoupon.getLong("orderCount") == null ? 0L : productPromoCoupon.getLong("orderCount");
                            if (orderCount > 0L) {
                                productPromoCoupon.set("orderCount", orderCount - 1);
                            }
                            delegator.store(productPromoCoupon);
                        }
                        
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            if (UtilValidate.isNotEmpty(orderRoles)) {
                List<GenericValue> userLogin = null;
                try {
                    userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", orderRoles.get(0).get("partyId")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(userLogin)) {
                    // cancel the order
                    boolean okay = OrderChangeHelper.cancelOrder(dispatcher, userLogin.get(0), orderId);
                    
                    if (okay) {
                        try {
                            TransactionUtil.commit(beganTransaction);
                        } catch (GenericTransactionException gte) {
                            Debug.logError(gte, "Unable to commit transaction", module);
                        }
                    } else {
                        try {
                            TransactionUtil.rollback(beganTransaction, "Failure in processing Pay cancel callback", null);
                        } catch (GenericTransactionException gte) {
                            Debug.logError(gte, "Unable to rollback transaction", module);
                        }
                    }
                    
                    // attempt to release the offline hold on the order (workflow)
                    if (okay) {
                        OrderChangeHelper.releaseInitialOrderHold(dispatcher, orderId);
                    }
                }
            }
        }
        successResult.put("resultData", true);
        return successResult;
    }
    
    /**
     * 获取收货地址 add by Wcy at 2016.01.28
     *
     * @param request
     * @param response
     * @return
     */
    public static Map<String, Object> getPartyPostalAddresses(HttpServletRequest request, HttpServletResponse response) {
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        /** 获取参数 */
        String userLoginId = (String) request.getParameter("userLoginId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map<String, Object>> resultData = FastList.newInstance();
        
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == userLogin) {
            successResult.put("resultData", resultData);
            return successResult;
        }
        String partyId = userLogin.getString("partyId");
        //默认地址
        GenericValue profiledefs;
        try {
            profiledefs = EntityUtil.getFirst(delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId)));
            if (UtilValidate.isNotEmpty(profiledefs)) {
                successResult.put("defaultId", profiledefs.get("defaultShipAddr"));
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        //end add by gss
        try {
            GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            List<GenericValue> shippingContactMechList = (List<GenericValue>) ContactHelper.getContactMechByType(party, "POSTAL_ADDRESS", false);
            if (UtilValidate.isNotEmpty(shippingContactMechList)) {
                for (int i = 0; i < shippingContactMechList.size(); i++) {
                    GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", shippingContactMechList.get(i).get("contactMechId")));
//                    System.out.println("MMMMMMMMMMM_________IIIIIIIII" + postalAddress);
                    Map<String, Object> map = FastMap.newInstance();
                    map.put("id", postalAddress.get("contactMechId"));
                    map.put("name", postalAddress.get("toName"));
                    map.put("telphone", postalAddress.get("mobilePhone"));
                    Map<String, Object> stateProvincemap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(postalAddress.getString("stateProvinceGeoId"))) {
                        GenericValue stateProvinceGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("stateProvinceGeoId")));
                        stateProvincemap.put("id", postalAddress.getString("stateProvinceGeoId"));
                        stateProvincemap.put("title", stateProvinceGeoId.getString("geoName"));
                        map.put("stateProvinceGeoId", stateProvincemap);
                    }
                    Map<String, Object> citymap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(postalAddress.getString("city"))) {
                        GenericValue cityGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("city")));
                        citymap.put("id", postalAddress.getString("city"));
                        citymap.put("title", cityGeoId.getString("geoName"));
                        map.put("city", citymap);
                    }
                    Map<String, Object> countymap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(postalAddress.getString("countyGeoId"))) {
                        GenericValue countyGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("countyGeoId")));
                        countymap.put("id", postalAddress.getString("countyGeoId"));
                        countymap.put("title", countyGeoId.getString("geoName"));
                        map.put("countyGeoId", countymap);
                    }
                    //map.put("region",address);
                    
                    map.put("location", postalAddress.get("address1"));
                    
                    map.put("chooseCommunity", postalAddress.get("communityId"));
                    resultData.add(map);
                }
            }
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        successResult.put("list", resultData);
        return successResult;
    }
    
    
    public static String updateOrderComment(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String replyComment = request.getParameter("replyComment");
        String isShow = request.getParameter("isShow");
        String seeType = request.getParameter("seeType");
        String commentId = request.getParameter("commentId");
        GenericValue orderComment = delegator.findByPrimaryKey("ProductReview", UtilMisc.toMap("productReviewId", commentId));
        if (UtilValidate.isNotEmpty(replyComment)) {
            orderComment.set("replyComment", replyComment);
            orderComment.set("isReply", "1");
        }
        orderComment.set("isShow", isShow);
        orderComment.set("seeType", seeType);
        orderComment.store();
        return (String) successResult.get("responseMessage");
    }
    
    public static String batchShowOrHidden(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String ids = request.getParameter("ids");
        String isShow = request.getParameter("isShow");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            GenericValue oc = delegator.findByPrimaryKey("ProductReview", UtilMisc.toMap("productReviewId", id));
            oc.set("isShow", isShow);
            oc.store();
        }
        return (String) successResult.get("responseMessage");
    }
    
    /**
     * 商家待发货  Add By AlexYao  2016-2-27 17:14:44
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> busNotShippedOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 定义查询字段 & 排序字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String orderStatus = (String) context.get("orderStatus");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        dynamicViewEntity.addAlias("OH", "orderId");
        dynamicViewEntity.addAlias("OH", "orderStatus", "statusId", null, null, null, null);
//        dynamicViewEntity.addAlias("OH", "total", "grandTotal", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "orderDate");
        dynamicViewEntity.addAlias("OH", "distributionMethod");
        
        /** 定义订单role */
//        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
//        dynamicViewEntity.addAlias("ORE", "partyId");
//        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        
        /** 定义订单元素动态视图 */
        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
//        dynamicViewEntity.addAlias("OI", "number", "quantity", null, null, null, null);
        
        /** 定义产品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        dynamicViewEntity.addAlias("PD", "productName");
        dynamicViewEntity.addAlias("PD", "productType", "productTypeId", null, null, null, null);
        dynamicViewEntity.addAlias("PD", "imgUrl", "smallImageUrl", null, null, null, null);
        dynamicViewEntity.addAlias("PD", "businessPartyId");
        
        
        /** 定义表的关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        dynamicViewEntity.addViewLink("OI", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        /**orderOrle 与orderHeader*/
//        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        /** 查询字段 & 排序字段 */
        fieldsToSelect.add("orderId");//订单编号
        fieldsToSelect.add("productId");//产品编号
        fieldsToSelect.add("orderStatus");//订单状态
        fieldsToSelect.add("productName");//产品名称
        fieldsToSelect.add("orderDate");//下单日期
        fieldsToSelect.add("productType");//产品类型[FINISHED_GOOD:实物产品,VIRTUAL_GOOD:虚拟产品]
        fieldsToSelect.add("imgUrl");//产品小图
//        fieldsToSelect.add("number");   //下单数量
//        fieldsToSelect.add("total");//订单总金额
        fieldsToSelect.add("distributionMethod");//订单配送方式
        
        
        /** 按照下单时间倒序排序 */
        orderBy.add("-orderDate");
        
        /** 定义查询条件集合 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<GenericValue> partyRelationship = null;
        try {
            partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        String partyId = partyRelationship.get(0).getString("partyIdFrom");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        /** 查询当前用户 */
//        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("businessPartyId", EntityOperator.EQUALS, partyId));
//        andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        /** 实物商品 */
        andExprs.add(EntityCondition.makeCondition("productType", EntityOperator.EQUALS, FINISHED_GOOD));
        /** 订单状态：待发货或待收货*/
        andExprs.add(EntityCondition.makeCondition(EntityCondition.makeCondition("orderStatus", EntityOperator.EQUALS, orderStatus)));
        
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        /** 查询结束条数*/
        int highIndex = 0;
        /** 查询结果集*/
        List<GenericValue> orderList = null;
        int orderListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
            lowIndex = viewIndex + 1;
            highIndex = viewIndex + viewSize;
            
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            findOpts.setDistinct(true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取分页结果集
            orderList = pli.getPartialList(lowIndex, viewSize);
            //获取记录条数
            orderListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > orderListSize) {
                highIndex = orderListSize;
            }
            
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        
        if (null == orderList) {
            orderList = FastList.newInstance();
        }
        List<Map<String, Object>> jsonArray = null;
        try {
            jsonArray = notShippedAndNotReceivedOrder(delegator, orderList, locale, dispatcher);
//            System.out.println(jsonArray);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        resultData.put("max", orderListSize);
        resultData.put("orderList", jsonArray);
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 商家待发货、待收货订单结果集转换为List  add by AlexYao
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> notShippedAndNotReceivedOrder(Delegator delegator, List<GenericValue> valueList, Locale locale, LocalDispatcher dispatcher) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (GenericValue v : valueList) {
            Map<String, Object> map = FastMap.newInstance();
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
            String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            
            map.put("orderId", v.get("orderId"));//订单号
            map.put("orderDate", sdf.format(v.getTimestamp("orderDate")));//下单日期
            map.put("productName", v.get("productName"));//产品名称
//            map.put("productType", "physical");//商品类型
            //待发货
            if ("ORDER_WAITSHIP".equals(v.get("orderStatus"))) {
                map.put("orderStatus", "待发货");
                map.put("orderStatusId", "notShipped");
                map.put("logisticsButton", false);
                if (!SMZT.equals(v.getString("distributionMethod"))) {
                    map.put("deliveryButton", true);
                } else {
                    map.put("deliveryButton", false);
                }
            } else if ("ORDER_WAITRECEIVE".equals(v.get("orderStatus"))) {
                map.put("orderStatus", "待收货");
                map.put("orderStatusId", "notReceived");
                map.put("deliveryButton", false);
                //订单状态 ：(自提订单不显示物流) && (有物流信息)
                List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", v.get("orderId")));
                if (!SMZT.equals(v.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                    map.put("logisticsButton", true);
                } else {
                    map.put("logisticsButton", false);
                }
            } else if ("ORDER_WAITEVALUATE".equals(v.get("orderStatus"))) {
                map.put("orderStatus", "待评价");
                map.put("orderStatusId", "notReviewed");
                map.put("deliveryButton", false);
                //订单状态 ：(自提订单不显示物流) && (有物流信息)
                List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", v.get("orderId")));
                if (!SMZT.equals(v.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                    map.put("logisticsButton", true);
                } else {
                    map.put("logisticsButton", false);
                }
            } else if ("ORDER_COMPLETED".equals(v.get("orderStatus"))) {
                map.put("orderStatus", "已完成");
                map.put("orderStatusId", "completed");
                map.put("deliveryButton", false);
                //订单状态 ：(自提订单不显示物流) && (有物流信息)
                List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", v.get("orderId")));
                if (!SMZT.equals(v.getString("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                    map.put("logisticsButton", true);
                } else {
                    map.put("logisticsButton", false);
                }
            }
            map.put("imgUrl", imageUrl);//商品图片
            String receivePerson = "";
            String receivePhone = "";
            if (UtilValidate.isNotEmpty(v.get("distributionMethod")) || !"SMZT".equals(v.get("distributionMethod"))) {
                GenericValue orderContactMech = EntityUtil.getFirst(delegator.findByAnd("OrderContactMech", UtilMisc.toMap("orderId", v.get("orderId"), "contactMechPurposeTypeId", "SHIPPING_LOCATION")));
                if (UtilValidate.isNotEmpty(orderContactMech)) {
                    GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", orderContactMech.get("contactMechId")));
                    if (UtilValidate.isNotEmpty(postalAddress)) {
                        receivePerson = postalAddress.getString("toName");
                        receivePhone = postalAddress.getString("mobilePhone");
                    }
                }
            }
            map.put("receivePerson", receivePerson);//收货人
            map.put("receivePhone", receivePhone);//电话
//            map.put("number", v.get("number"));//下单数量
//            map.put("total", v.get("total")); //订单总金额
            map.put("viewRefundButton", false);
            list.add(map);
        }
        
        return list;
    }
    
    /**
     * 验证是否发起退款 Add By AlexYao 2016-2-29 10:10:42
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> deliveryProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        List<EntityCondition> conditions = new ArrayList<EntityCondition>();
        List<EntityCondition> statusConditions = new ArrayList<EntityCondition>();
        conditions.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
        statusConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_WAITEXAMINE"));
        statusConditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_WAITFEFUND"));
        conditions.add(EntityCondition.makeCondition(statusConditions, EntityOperator.OR));
        List<GenericValue> returnItems = null;
        try {
            returnItems = delegator.findList("ReturnItem", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(returnItems)) {
            result.put("resultData", false);
        } else {
            result.put("resultData", true);
        }
        return result;
    }
    
    /**
     * 商家待评价、已完成  Add By AlexYao  2016-2-29 10:23:39
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> busCompletedOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 定义查询字段 & 排序字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        dynamicViewEntity.addAlias("OH", "orderId");
        dynamicViewEntity.addAlias("OH", "orderStatus", "statusId", null, null, null, null);
//        dynamicViewEntity.addAlias("OH", "total", "grandTotal", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "orderDate");
        dynamicViewEntity.addAlias("OH", "distributionMethod");
        
        /** 定义订单role */
//        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
//        dynamicViewEntity.addAlias("ORE", "partyId");
//        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        
        /** 定义订单元素动态视图 */
        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
//        dynamicViewEntity.addAlias("OI", "number", "quantity", null, null, null, null);
        
        /** 定义产品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        dynamicViewEntity.addAlias("PD", "productName");
        dynamicViewEntity.addAlias("PD", "productType", "productTypeId", null, null, null, null);
        dynamicViewEntity.addAlias("PD", "imgUrl", "smallImageUrl", null, null, null, null);
        dynamicViewEntity.addAlias("PD", "businessPartyId");
        
        
        /** 定义表的关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        dynamicViewEntity.addViewLink("OI", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        /**orderOrle 与orderHeader*/
//        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        /** 查询字段 & 排序字段 */
        fieldsToSelect.add("orderId");//订单编号
        fieldsToSelect.add("productId");//产品编号
        fieldsToSelect.add("orderStatus");//订单状态
        fieldsToSelect.add("productName");//产品名称
        fieldsToSelect.add("orderDate");//下单日期
        fieldsToSelect.add("productType");//产品类型[FINISHED_GOOD:实物产品,VIRTUAL_GOOD:虚拟产品]
        fieldsToSelect.add("imgUrl");//产品小图
//        fieldsToSelect.add("number");   //下单数量
//        fieldsToSelect.add("total");//订单总金额
        fieldsToSelect.add("distributionMethod");//订单配送方式
        
        
        /** 按照下单时间倒序排序 */
        orderBy.add("-orderDate");
        
        /** 定义查询条件集合 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<GenericValue> partyRelationship = null;
        try {
            partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        String partyId = partyRelationship.get(0).getString("partyIdFrom");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        /** 查询当前用户 */
//        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("businessPartyId", EntityOperator.EQUALS, partyId));
//        andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        /** 实物商品 */
        andExprs.add(EntityCondition.makeCondition("productType", EntityOperator.EQUALS, FINISHED_GOOD));
        /** 订单状态：待评价或已完成*/
        andExprs.add(EntityCondition.makeCondition(
                EntityCondition.makeCondition(EntityCondition.makeCondition("orderStatus", EntityOperator.EQUALS, "ORDER_WAITEVALUATE")),
                EntityOperator.OR,
                EntityCondition.makeCondition(EntityCondition.makeCondition("orderStatus", EntityOperator.EQUALS, "ORDER_COMPLETED"))));
        
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        /** 查询结束条数*/
        int highIndex = 0;
        /** 查询结果集*/
        List<GenericValue> orderList = null;
        int orderListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
            lowIndex = viewIndex + 1;
            highIndex = viewIndex + viewSize;
            
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            findOpts.setDistinct(true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取分页结果集
            orderList = pli.getPartialList(lowIndex, viewSize);
            //获取记录条数
            orderListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > orderListSize) {
                highIndex = orderListSize;
            }
            
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        
        if (null == orderList) {
            orderList = FastList.newInstance();
        }
        List<Map<String, Object>> jsonArray = null;
        try {
            jsonArray = notShippedAndNotReceivedOrder(delegator, orderList, locale, dispatcher);
//            System.out.println(jsonArray);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        resultData.put("max", orderListSize);
        resultData.put("orderList", jsonArray);
        result.put("resultData", resultData);
        return result;
        
    }
    
    /**
     * 商家退款/售后订单 Add By AlexYao 2016-2-29 10:39:17
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> busRefundedOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        List<GenericValue> partyRelationship = null;
        try {
            partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        String partyId = partyRelationship.get(0).getString("partyIdFrom");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        
        /** 查询开始条数*/
        int lowIndex = 0;
        //计算开始分页值 & 计算分页结束值
        lowIndex = viewIndex;
        /** sql 语句*/
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        String limitSql = "limit " + lowIndex + "," + viewSize;
        String sql = "select *  " +
                "  from( " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_id as productId, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,return_header_type_id as returnType,null as ticketStatus " +
                "  from order_header a, order_item b, product c,return_header d,return_item e" +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and c.merchant_name= '" + partyId + "' " +
//                "   and r.order_id= a.order_id " +
//                "   and r.party_id= '" + partyId + "' " +
//                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and c.product_type_id= 'FINISHED_GOOD' " +
                "   and e.return_id = d.return_id " +
                "   and e.order_id = a.order_id  " +
                ") as temptb " +
                " order by orderDate desc ";
        
        List<Map<String, Object>> jsonArray = null;
        List<Map> resultArray = new ArrayList<Map>();
        try {
            sqlP.prepareStatement(sql + limitSql);
            ResultSet rs = sqlP.executeQuery();
            jsonArray = getListFromResultSet(rs);
            rs.close();
            if (jsonArray != null && jsonArray.size() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                for (int i = 0; i < jsonArray.size(); i++) {
                    Map<String, Object> orderMap = jsonArray.get(i);
                    Map<String, Object> map = FastMap.newInstance();
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderMap.get("productId")));
                    String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                    
                    map.put("orderId", orderMap.get("orderId"));//订单号
                    map.put("orderDate", sdf.format(orderMap.get("orderDate")));//下单日期
                    map.put("productName", orderMap.get("productName"));//产品名称
//                    map.put("productType", "physical");//商品类型
                    List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderMap.get("orderId")));
                    if ("ORDER_REFUNDED".equals(orderMap.get("orderStatus"))) {
                        map.put("orderStatus", "已退款");
                        map.put("orderStatusId", "hasRefuned");
                    } else if ("ORDER_REJECTED".equals(orderMap.get("orderStatus"))) {
                        map.put("orderStatus", "拒绝申请");
                        map.put("orderStatusId", "rejectApplication");
                    } else if (UtilValidate.isNotEmpty(returnItems)) {
                        for (GenericValue returnItem : returnItems) {
                            if ("RETURN_WAITEXAMINE".equals(returnItem.get("statusId"))) {
                                map.put("orderStatus", "待审核");
                                map.put("orderStatusId", "notAudited");
                                break;
                            } else if ("RETURN_WAITFEFUND".equals(returnItem.get("statusId"))) {
                                map.put("orderStatus", "待退款");
                                map.put("orderStatusId", "notRefunded");
                                break;
                            } else if ("RETURN_COMPLETED".equals(returnItem.get("statusId"))) {
                                map.put("orderStatus", "已退款");
                                map.put("orderStatusId", "hasRefuned");
                                break;
                            } else if ("RETURN_REJECTAPPLY".equals(returnItem.get("statusId"))) {
                                map.put("orderStatus", "拒绝申请");
                                map.put("orderStatusId", "rejectApplication");
                                break;
                            }
                        }
                    }
                    map.put("deliveryButton", false);
                    //订单状态 ：(自提订单不显示物流) && (有物流信息)
                    List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderMap.get("orderId")));
                    if (!SMZT.equals(orderMap.get("distributionMethod")) && UtilValidate.isNotEmpty(orderDelivery)) {
                        map.put("logisticsButton", true);
                    } else {
                        map.put("logisticsButton", false);
                    }
                    map.put("imgUrl", imageUrl);//商品图片
                    String receivePerson = "";
                    String receivePhone = "";
                    if (UtilValidate.isNotEmpty(orderMap.get("distributionMethod")) || !"SMZT".equals(orderMap.get("distributionMethod"))) {
                        GenericValue orderContactMech = EntityUtil.getFirst(delegator.findByAnd("OrderContactMech", UtilMisc.toMap("orderId", orderMap.get("orderId"), "contactMechPurposeTypeId", "SHIPPING_LOCATION")));
                        if (UtilValidate.isNotEmpty(orderContactMech)) {
                            GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", orderContactMech.get("contactMechId")));
                            if (UtilValidate.isNotEmpty(postalAddress)) {
                                receivePerson = postalAddress.getString("toName");
                                receivePhone = postalAddress.getString("mobilePhone");
                            }
                        }
                    }
                    map.put("receivePerson", receivePerson);//收货人
                    map.put("receivePhone", receivePhone);//电话
//                    map.put("number", orderMap.get("number"));//下单数量
//                    map.put("total", orderMap.get("total")); //订单总金额
                    map.put("viewRefundButton", true);
                    
                    resultArray.add(map);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        
        if (null == jsonArray) {
            jsonArray = FastList.newInstance();
        }
        
        Long refundNum = 0L;
        Map<String, Object> refundNumResult = busRefundNum(dctx, context);
        if (UtilValidate.isNotEmpty(refundNumResult) && UtilValidate.isNotEmpty(refundNumResult.get("refundNum"))) {
            refundNum = (Long) refundNumResult.get("refundNum");
        }
        
        resultData.put("max", refundNum);
        resultData.put("orderList", resultArray);
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 统计该用户下已发起退款/退货，且退款/退货未完成的所有订单 Add By AlexYao 2016-2-29 11:56:32
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> busRefundNum(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 获取会员信息 */
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("refundNum", 0L); //售后/评价订单数量
            return result;
        }
        
        if (null == userLogin) {
            result.put("refundNum", 0L); //售后/评价订单数量
            return result;
        }
        List<GenericValue> partyRelationship = null;
        try {
            partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        String partyId = partyRelationship.get(0).getString("partyIdFrom");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("refundNum", 0L); //待评价订单数量
            return result;
        }
        
        /** sql 语句*/
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        String sql = "select *  " +
                "  from( " +
                "select distinct a.order_id as orderId, a.status_id as orderStatus, a.created_by as createdBy, a.grand_total as total, a.order_date as orderDate, a.distribution_method as distributionMethod, b.quantity as number, c.product_id as productId, c.product_name as productName, c.product_type_id as productType, c.small_image_url as smallImageUrl,return_header_type_id as returnType,null as ticketStatus " +
                "  from order_header a, order_item b, product c,return_header d,return_item e" +
                " where a.order_id= b.order_id " +
                "   and b.product_id= c.product_id " +
                "   and c.merchant_name= '" + partyId + "' " +
//                "   and r.order_id= a.order_id " +
//                "   and r.party_id= '" + partyId + "' " +
//                "   and r.role_type_id= 'PLACING_CUSTOMER' " +
                "   and c.product_type_id= 'FINISHED_GOOD' " +
                "   and e.return_id = d.return_id " +
                "   and e.order_id = a.order_id  " +
                ") as temptb " +
                " order by orderDate desc ";
        
        String countSql = "select count(*) as number from (" + sql + ") as tb";
        Long number = 0L;
        try {
            sqlP.prepareStatement(countSql);
            ResultSet rs = sqlP.executeQuery();
            while (rs.next()) {
                number = rs.getLong("number");
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            number = 0L;
        } catch (SQLException e) {
            Debug.logError(e, module);
            number = 0L;
        }
        result.put("refundNum", number);
        return result;
    }
    
    /**
     * 商家订单详情  Add By AlexYao 2016-2-29 11:56:39
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> busOrderDetail(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String orderStatusId = (String) context.get("orderStatusId");
        Map<String, String> productType = FastMap.newInstance();
        productType.put(FINISHED_GOOD, "physical");
        
        Map<String, String> orderStatusMap = FastMap.newInstance();
        orderStatusMap.put("notShipped", "待发货");
        orderStatusMap.put("notReceived", "待收货");
        orderStatusMap.put("notReviewed", "待评价");
        orderStatusMap.put("notAudited", "待审核");
        orderStatusMap.put("notRefunded", "待退款");
        orderStatusMap.put("hasRefuned", "已退款");
        orderStatusMap.put("rejectApplication", "拒绝申请");
        orderStatusMap.put("completed", "已完成");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put("resultData", resultData);
            return result;
        }
        
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        
        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (null == orderHeader) {
                result.put("resultData", resultData);
                return result;
            }
            GenericValue orderItem = EntityUtil.getFirst(delegator.getRelated("OrderItem", null, null, orderHeader));
            if (null == orderItem) {
                result.put("resultData", resultData);
                return result;
            }
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get("productId")));
            if (null == product) {
                result.put("resultData", resultData);
                return result;
            }
            //产品活动
            String activityId = orderItem.getString("activityId");
            GenericValue activity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
            GenericValue productActivity = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", product.getString("productId")));
            
            resultData.put("orderId", orderId);
            resultData.put("orderDate", UtilDateTime.timeStampToString(orderHeader.getTimestamp("orderDate"), "yyyy-MM-dd HH:mm:ss", timeZone, locale)); //  下单时间 “2015-11-11 10:52:52
            String activityDesc = "";
            Long soldNum = 0L;
            if (null != productActivity) {
                activityDesc = activity == null ? "" : activity.getString("activityDesc");
                //已售数量
                soldNum = activity.getLong("hasBuyQuantity");
                resultData.put("soldNum", soldNum);//已售数量
            } else {
                resultData.put("soldNum", soldNum);
            }
            //阶梯价
            List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
            try {
                productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId), UtilMisc.toList("orderQuantity"));//阶梯价规则表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            List<Map> priceList = new ArrayList<Map>();
            if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                for (GenericValue productGroupOrderRule : productGroupOrderRules) {
                    Map price = new HashMap();
                    price.put("people", productGroupOrderRule.get("orderQuantity"));
                    price.put("price", productGroupOrderRule.get("orderPrice"));
                    priceList.add(price);
                }
            }
            resultData.put("priceList", priceList);//阶梯价
            if (!SMZT.equals(orderHeader.getString("distributionMethod"))) {
                //收货人
                OrderReadHelper orderReadHelper = new OrderReadHelper(orderHeader);
                GenericValue shippingLocations = EntityUtil.getFirst(orderReadHelper.getShippingLocations());
                String province = "";
                String city = "";
                String country = "";
                String address = "";
                
                if (null != shippingLocations) {
                    province = getGeoName(delegator, "PROVINCE", shippingLocations.getString("stateProvinceGeoId"));
                    city = getGeoName(delegator, "CITY", shippingLocations.getString("city"));
                    country = getGeoName(delegator, "COUNTY", shippingLocations.getString("countryGeoId"));
                    address = province + city + country + shippingLocations.getString("address1");
                    resultData.put("receivePerson", shippingLocations.getString("toName")); //收货人姓名
                    resultData.put("receivePhone", shippingLocations.getString("mobilePhone"));
                    resultData.put("receiveAddress", address);    //省市区+地址
                } else {
                    resultData.put("receivePerson", ""); //收货人姓名
                    resultData.put("receivePhone", "");    //收货人电话
                    resultData.put("receiveAddress", ""); //收货人地址
                }
            } else {
                List<GenericValue> orderRoles = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", "PLACING_CUSTOMER"));
                GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", orderRoles.get(0).get("partyId")));
                List<GenericValue> login = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", orderRoles.get(0).get("partyId")));
                if (UtilValidate.isNotEmpty(person)) {
                    resultData.put("receivePerson", person.get("nickname")); //收货人姓名
                } else {
                    resultData.put("receivePerson", "");
                }
                resultData.put("receivePhone", login.get(0).get("userLoginId"));    //收货人电话
                resultData.put("receiveAddress", ""); //收货人地址
            }
            resultData.put("buyingTips", activityDesc);  //购买须知
            String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
            resultData.put("imgUrl", imageUrl);//产品图片
            resultData.put("productName", product.getString("productName"));//产品名称
            resultData.put("number", orderItem.get("quantity"));//下单数量
            resultData.put("total", orderHeader.getBigDecimal("grandTotal"));//订单总金额
            //订单状态【待使用、待收货、待评价、待审核、待退款、已退款、拒绝申请、已过期、已完成】
            resultData.put("orderStatus", orderStatusMap.get(orderStatusId));
            resultData.put("orderStatusId", orderStatusId);
            resultData.put("remarks", orderHeader.get("remarks"));// 备注
            
            //评价信息
            List<GenericValue> productReview = delegator.findByAnd("ProductReview", UtilMisc.toMap("orderId", orderId));
            if (UtilValidate.isNotEmpty(productReview)) {
                resultData.put("viewEvaluateButton", true); // 是否有“查看评价”按钮
            } else {
                resultData.put("viewEvaluateButton", false);
            }
            
            //查看是否有退款/退货记录
            String[] orderStatus = new String[]{"RETURN_CANCELLED", "RETURN_COMPLETED", "RETURN_REJECTAPPLY", "RETURN_REJECTRECEIVE"};
            EntityCondition condition = EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId), EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN, Arrays.asList(orderStatus))), EntityOperator.AND);
            Set<String> fieldsToSelect = FastSet.newInstance();
            fieldsToSelect.add("returnId");
            //订单状态 ：(自提订单不显示物流) && (有物流信息)
            List<GenericValue> orderDelivery = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderId));
            if (!SMZT.equals(orderHeader.getString("distributionMethod"))) {
                if (UtilValidate.isNotEmpty(orderDelivery)) {
                    resultData.put("logisticsButton", true); // 是否有“查看物流”按钮
                }
                resultData.put("distributionMethod", "快递配送");
                if ("GZRPS".equals(orderHeader.get("distributionMethod"))) {
                    resultData.put("distributionTime", "只在工作日送货");
                } else {
                    resultData.put("distributionTime", "只在双休日，节假日送货");
                }
            } else {
                resultData.put("logisticsButton", false);
                resultData.put("distributionMethod", "自提订单");
                resultData.put("distributionTime", "");
            }
            if (!SMZT.equals(orderHeader.getString("distributionMethod"))) {
                if ("ORDER_WAITSHIP".equals(orderHeader.get("statusId")) && UtilValidate.isEmpty(orderDelivery)) {
                    resultData.put("deliveryButton", true); // 是否有“确认发货”按钮
                } else {
                    resultData.put("deliveryButton", false);
                }
            } else {
                resultData.put("deliveryButton", false);
            }
            List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderId));
            if (UtilValidate.isNotEmpty(returnItems) && ("notAudited".equals(orderStatusId) || "notRefunded".equals(orderStatusId) || "hasRefuned".equals(orderStatusId) || "rejectApplication".equals(orderStatusId))) {
                resultData.put("viewRefundButton", true);// 是否有“退款详情”按钮
            } else {
                resultData.put("viewRefundButton", false);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        //物流公司
        List deliveryCompanys = FastList.newInstance();
        List<GenericValue> logisticsCompanys = null;
        try {
            logisticsCompanys = delegator.findList("LogisticsCompany", EntityCondition.makeCondition("companyId", EntityOperator.NOT_EQUAL, null), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(logisticsCompanys)) {
            for (GenericValue logisticsCompany : logisticsCompanys) {
                Map map = new HashMap();
                map.put("companyId", logisticsCompany.get("companyId"));
                map.put("companyName", logisticsCompany.get("companyName"));
                deliveryCompanys.add(map);
            }
        }
        resultData.put("deliveryCompanys", deliveryCompanys);
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 商家信息  Add By AlexYao 2016-2-29 16:42:10
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessData(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            List<GenericValue> partyRelationship = null;
            try {
                partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue partyBusiness = null;
            if (UtilValidate.isNotEmpty(partyRelationship)) {
                try {
                    partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", partyRelationship.get(0).get("partyIdFrom")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            if (UtilValidate.isNotEmpty(partyBusiness)) {
                resultData.put("businessName", partyBusiness.get("businessName"));
            }
        }
        
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 验证yabiz商城券  Add By AlexYao 2016-2-29 17:02:34
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> checkTicket(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String ticketNo = (String) context.get("ticketNo");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            List<GenericValue> partyRelationship = null;
            List<GenericValue> tickets = null;
            try {
                partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
                tickets = delegator.findByAnd("Ticket", UtilMisc.toMap("ticketNo", ticketNo));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(tickets)) {
                GenericValue product = null;
                try {
                    product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", tickets.get(0).get("productId")));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(product) && product.get("businessPartyId").equals(partyRelationship.get(0).get("partyIdFrom"))) {
                    if ("notUsed".equals(tickets.get(0).get("ticketStatus")) || "rejectApplication".equals(tickets.get(0).get("ticketStatus"))) {
                        tickets.get(0).set("ticketStatus", "hasUsed");
                        List<GenericValue> orderItems = new ArrayList<GenericValue>();
                        try {
                            orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", tickets.get(0).get("orderId")));
                            delegator.storeAll(tickets);
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        try {
                            Map map = dispatcher.runSync("saveVirtualOrderIntegral", UtilMisc.toMap("orderId", tickets.get(0).get("orderId"), "ticketList", UtilMisc.toList(tickets.get(0).get("ticketId"))));
                        } catch (GenericServiceException e) {
                            e.printStackTrace();
                        }
                        resultData.put("status", true);
                        resultData.put("info", "购买商品：" + product.get("productName") + "，单价：￥" + orderItems.get(0).getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                    } else if ("notAudited".equals(tickets.get(0).get("ticketStatus"))) {
                        resultData.put("status", false);
                        resultData.put("info", "该券号退款审核中！");
                    } else if ("notRefunded".equals(tickets.get(0).get("ticketStatus"))) {
                        resultData.put("status", false);
                        resultData.put("info", "该券号退款审核通过，等待退款中！");
                    } else if ("hasRefuned".equals(tickets.get(0).get("ticketStatus"))) {
                        resultData.put("status", false);
                        resultData.put("info", "该券号已退款！");
                    } else if ("expired".equals(tickets.get(0).get("ticketStatus"))) {
                        resultData.put("status", false);
                        resultData.put("info", "该券号已过期！");
                    } else if ("hasUsed".equals(tickets.get(0).get("ticketStatus"))) {
                        resultData.put("status", false);
                        resultData.put("info", "该券号已使用！");
                    }
                } else {
                    resultData.put("status", false);
                    resultData.put("info", "该券号是其它商家的券号！");
                }
            } else {
                resultData.put("status", false);
                resultData.put("info", "该券号不存在！");
            }
        } else {
            resultData.put("status", false);
            resultData.put("info", "该用户不存在！");
        }
        
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 商家申请  Add By AlexYao 2016-2-29 18:30:30
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessApply(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String businessName = (String) context.get("businessName");
        String description = (String) context.get("description");
        String province = (String) context.get("province");
        String city = (String) context.get("city");
        String county = (String) context.get("county");
        String address = (String) context.get("address");
        String legalPersonName = (String) context.get("legalPersonName");
        String tel = (String) context.get("tel");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            List<GenericValue> partyRelationship = null;
            List<GenericValue> partyBusiness = null;
            try {
                partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(partyRelationship)) {
                try {
                    partyBusiness = delegator.findByAnd("PartyBusiness", UtilMisc.toMap("partyId", partyRelationship.get(0).get("partyIdFrom"), "auditStatus", "2"));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            if (UtilValidate.isEmpty(partyRelationship) || UtilValidate.isNotEmpty(partyBusiness)) {
                Map businessMap = FastMap.newInstance();
                businessMap.put("partyId", userLogin.get("partyId"));
                businessMap.put("businessName", businessName);
                businessMap.put("description", description);
                businessMap.put("province", province);
                businessMap.put("city", city);
                businessMap.put("county", county);
                businessMap.put("address", address);
                businessMap.put("legalPersonName", legalPersonName);
                businessMap.put("tel", tel);
                businessMap.put("auditStatus", "0");
                try {
                    dispatcher.runSync("businessAdd", businessMap);
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                }
                resultData.put("status", true);
                resultData.put("info", "申请成功！");
            } else {
                resultData.put("status", false);
                resultData.put("info", "您已申请，请勿重复申请！");
            }
        } else {
            resultData.put("status", false);
            resultData.put("info", "该用户不存在！");
        }
        
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 物流公司  Add By AlexYao 2016-3-1 10:00:56
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> businessCompany(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        List resultData = FastList.newInstance();
        List<GenericValue> logisticsCompanys = null;
        try {
            logisticsCompanys = delegator.findList("LogisticsCompany", EntityCondition.makeCondition("companyId", EntityOperator.NOT_EQUAL, null), null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(logisticsCompanys)) {
            for (GenericValue logisticsCompany : logisticsCompanys) {
                Map map = new HashMap();
                map.put("companyId", logisticsCompany.get("companyId"));
                map.put("companyName", logisticsCompany.get("companyName"));
                resultData.add(map);
            }
        }
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 订单发货  Add By AlexYao 2016-3-1 10:00:56
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> deliveryOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String deliveryCompany = (String) context.get("deliveryCompany");
        String logisticsNumber = (String) context.get("logisticsNumber");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Boolean resultData = false;
        List<GenericValue> orderDeliverys = null;
        GenericValue userLogin = null;
        List<GenericValue> orderItem = null;
        List<GenericValue> orderRoles = null;
        List<GenericValue> customerLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            orderDeliverys = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderId));
            orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
            orderRoles = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", "PLACING_CUSTOMER"));
            customerLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", orderRoles.get(0).get("partyId")));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(orderDeliverys)) {
            List<GenericValue> partyRelationship = null;
            try {
                partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue partyBusiness = null;
            try {
                partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", partyRelationship.get(0).get("partyIdFrom")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue orderDelivery = delegator.makeValue("OrderDelivery");
            orderDelivery.set("id", delegator.getNextSeqId("OrderDelivery"));
            orderDelivery.set("orderId", orderId);
            orderDelivery.set("deliveryCompany", deliveryCompany);
            orderDelivery.set("logisticsNumber1", logisticsNumber);
            try {
                delegator.create(orderDelivery);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            PostOrder.postOrder(deliveryCompany, logisticsNumber, null, null, customerLogin.get(0).getString("userLoginId"), partyBusiness.getString("businessName"), orderItem.get(0).getString("itemDescription"));
            Map serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_WAITRECEIVE", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
            Map map = setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
//            System.out.println(map);
            resultData = true;
            
            Map paraMap = FastMap.newInstance();
            paraMap.put("productName", orderItem.get(0).get("itemDescription"));
            String content = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("smsContent", "physical.order.delivered"), paraMap);
            try {
                dispatcher.runAsync("smsSend", UtilMisc.toMap("mobile", customerLogin.get(0).get("userLoginId"), "content", content));
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
            //保存订单操作日志
            try {
                delegator.create("OrderOperateLog", UtilMisc.toMap("id", delegator.getNextSeqId("OrderOperateLog"), "orderId", orderId, "operateType", "发货动作", "operator", userLogin.get("userLoginId"),
                        "operateTime", UtilDateTime.nowTimestamp()));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        
        
        result.put("resultData", resultData);
        return result;
    }
    
    /**
     * 已验证列表  Add By AlexYao  2016-3-1 10:40:41
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> validatedData(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException {
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String userLoginId = (String) context.get("userLoginId");
        Map<String, Object> resultData = FastMap.newInstance();
        
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        if (UtilValidate.isEmpty(userLogin)) {
            result.put("resultData", resultData);
            return result;
        }
        
        List<GenericValue> partyRelationship = null;
        try {
            partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        List<GenericValue> orderList = null;
        int max = 0;
        int lowIndex = 0;
        int highIndex = 0;
        
        //  double maxpage = 0;
        lowIndex = viewIndex;
        highIndex = viewIndex + viewSize;
        
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("statusDatetime");
        // set distinct on so we only get one row per order
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        
        JSONArray jsonArray = new JSONArray();
        
        try {
            String limitSql = " limit " + lowIndex + "," + viewSize;
            String sql = "SELECT DATE_FORMAT(TI.LAST_UPDATED_STAMP,'%Y-%m-%d') as date,count(TI.TICKET_ID) as number FROM TICKET TI INNER JOIN PRODUCT PD ON TI.PRODUCT_ID = PD.PRODUCT_ID " +
                    " WHERE PD.MERCHANT_NAME = '" + partyRelationship.get(0).get("partyIdFrom") + "' AND TI.TICKET_STATUS = 'hasUsed' group by DATE_FORMAT(TI.LAST_UPDATED_STAMP,'%Y-%m-%d') order by date desc ";
            
            String countsql = "SELECT COUNT(date) as ticketNum FROM (" + sql + ") tempdate";
            
            
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            SQLProcessor sqlP = new SQLProcessor(helperInfo);
            sqlP.prepareStatement(sql + limitSql, findOpts.getSpecifyTypeAndConcur(), findOpts.getResultSetType(),
                    findOpts.getResultSetConcurrency(), findOpts.getFetchSize(), findOpts.getMaxRows());
            long queryStartTime = 0;
            if (Debug.timingOn()) {
                queryStartTime = System.currentTimeMillis();
            }
            ResultSet rs = sqlP.executeQuery();
            if (Debug.timingOn()) {
                long queryEndTime = System.currentTimeMillis();
                long queryTotalTime = queryEndTime - queryStartTime;
                if (queryTotalTime > 150) {
                    Debug.logTiming("Ran query in " + queryTotalTime + " milli-seconds: " + " EntityName:  Sql: " + sql + " where clause:", module);
                }
            }
            
            EntityListIterator pli = new EntityListIterator(sqlP, null, null, null, GenericDAO.getGenericDAO(helperInfo), null, null, findOpts.getDistinct());
            int numberOfColumns = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                for (int i = 1; i <= numberOfColumns; i++) {
                    jsonObject.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                }
                jsonArray.add(jsonObject);
            }
            
            sqlP.prepareStatement(countsql, findOpts.getSpecifyTypeAndConcur(), findOpts.getResultSetType(),
                    findOpts.getResultSetConcurrency(), findOpts.getFetchSize(), findOpts.getMaxRows());
            ResultSet resultSizeSet = sqlP.executeQuery();
            
            while (resultSizeSet.next()) {
                max = resultSizeSet.getInt("ticketNum");
            }
            
            pli.close();
            
            
            result.put("max", max); //总页数
            result.put("ticketList", jsonArray);
            
        } catch (SQLException e) {
            Debug.log(e.getMessage());
        }
        
        resultData.put("resultData", result);
        
        return resultData;
    }
    
    /**
     * 已验证详情  Add By AlexYao 2016-3-1 11:27:46
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> validatedDetailData(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String date = (String) context.get("date");
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        List resultData = new ArrayList();
        
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(userLogin)) {
            result.put("resultData", resultData);
            return result;
        }
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("TI", "Ticket");
        dynamicViewEntity.addAlias("TI", "ticketId");
        dynamicViewEntity.addAlias("TI", "ticketNo");
        dynamicViewEntity.addAlias("TI", "ticketStatus");
        dynamicViewEntity.addAlias("TI", "orderId");
        dynamicViewEntity.addAlias("TI", "productId");
        dynamicViewEntity.addAlias("TI", "lastUpdatedStamp");
        
        /** 定义产品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        dynamicViewEntity.addAlias("PD", "businessPartyId");
        dynamicViewEntity.addAlias("PD", "productName");
        
        /** 定义表的关联关系 */
        dynamicViewEntity.addViewLink("TI", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        /** 查询字段 & 排序字段 */
        fieldsToSelect.add("ticketId");
        fieldsToSelect.add("ticketNo");//券号
        fieldsToSelect.add("ticketStatus");
        fieldsToSelect.add("orderId");//订单号
        fieldsToSelect.add("productName");//产品名称
        fieldsToSelect.add("lastUpdatedStamp");//验证日期
        
        
        /** 按照下单时间倒序排序 */
        orderBy.add("-lastUpdatedStamp");
        
        /** 定义查询条件集合 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<GenericValue> partyRelationship = null;
        try {
            partyRelationship = delegator.findByAnd("PartyRelationship", UtilMisc.toMap("partyIdTo", userLogin.get("partyId"), "roleTypeIdFrom", "SUPPLIER", "roleTypeIdTo", "EMPLOYEE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        String partyId = partyRelationship.get(0).getString("partyIdFrom");
        if (UtilValidate.isEmpty(partyId)) {
            result.put("resultData", resultData);
            return result;
        }
        /** 查询当前用户 */
        andExprs.add(EntityCondition.makeCondition("ticketStatus", EntityOperator.EQUALS, "hasUsed"));
        andExprs.add(EntityCondition.makeCondition("businessPartyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("lastUpdatedStamp", EntityOperator.GREATER_THAN_EQUAL_TO, Timestamp.valueOf(date + " 00:00:00")));
        andExprs.add(EntityCondition.makeCondition("lastUpdatedStamp", EntityOperator.LESS_THAN_EQUAL_TO, Timestamp.valueOf(date + " 23:59:59")));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        
        EntityListIterator eli = null;
        try {
            eli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        List<GenericValue> tickets = null;
        try {
            tickets = eli.getCompleteList();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(tickets)) {
            for (GenericValue ticket : tickets) {
                Map map = new HashMap();
                map.put("ticketNo", ticket.get("ticketNo"));
                map.put("orderId", ticket.get("orderId"));
                map.put("productName", ticket.get("productName"));
                map.put("dateTime", UtilDateTime.timeStampToString(ticket.getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                resultData.add(map);
            }
        }
        
        try {
            eli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("resultData", resultData);
        return result;
    }


//    /**
//     * 增加用户积分 Add By AlexYao 2016-3-8 17:19:55
//     * @param dctx
//     * @param context
//     * @return
//     */
//    public static Map<String, Object> addCostomerScore(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException{
//        Delegator delegator = dctx.getDelegator();
//        LocalDispatcher dispatcher = dctx.getDispatcher();
//        /** 获取参数 */
//        String orderId = (String) context.get("orderId");
//        BigDecimal maxAmount = (BigDecimal) context.get("maxAmount");
//        Map<String, Object> result = ServiceUtil.returnSuccess();
//
//        BigDecimal getIntegral = BigDecimal.ZERO;
//        GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
//        List<GenericValue> orderItems = delegator.findByAnd("OrderItem",UtilMisc.toMap("orderId",orderId));
//        List<GenericValue> orderRole = delegator.findByAnd("OrderRole",UtilMisc.toMap("orderId",orderId,"roleTypeId","PLACING_CUSTOMER"));
//        GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",orderItems.get(0).get("productId")));
//        GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity",UtilMisc.toMap("activityId",orderItems.get(0).get("activityId")));
//        if ("Y".equals(productActivity.getString("isSupportScore"))) {
//            List<GenericValue> nonProduct = delegator.findByAnd("PartyIntegral",UtilMisc.toMap("productId",product.get("productId"),"partyIntegralType","NONPRODUCT"));
//            if (UtilValidate.isEmpty(nonProduct)){
//                List<GenericValue> nonCategory = delegator.findByAnd("PartyIntegral",UtilMisc.toMap("productCategoryId",product.get("primaryProductCategoryId"),"partyIntegralType","NONCATEGORY"));
//                if (UtilValidate.isEmpty(nonCategory)){
//                    List<GenericValue> partyLevel = delegator.findByAnd("PartyLevel",UtilMisc.toMap("partyId",orderRole.get(0).get("partyId")));
//                    if(UtilValidate.isNotEmpty(partyLevel)){
//                        List<GenericValue> specialProduct = delegator.findByAnd("PartyIntegral",UtilMisc.toMap("levelId",partyLevel.get(0).get("levelId"),"productId",product.get("productId"),"partyIntegralType","SPECIALPRODUCT"));
//                        if(UtilValidate.isNotEmpty(specialProduct)){
//                            getIntegral = maxAmount.divide(new BigDecimal(specialProduct.get(0).getLong("integralValue"))).setScale(2,BigDecimal.ROUND_HALF_UP);
//                        }else{
//                            List<GenericValue> specialCategory = delegator.findByAnd("PartyIntegral",UtilMisc.toMap("levelId",partyLevel.get(0).get("levelId"),"productCategoryId",product.get("primaryProductCategoryId"),"partyIntegralType","SPECIALCATEGORY"));
//                            if(UtilValidate.isNotEmpty(specialCategory)){
//                                getIntegral = maxAmount.divide(new BigDecimal(specialCategory.get(0).getLong("integralValue"))).setScale(2,BigDecimal.ROUND_HALF_UP);
//                            }else{
//                                List<GenericValue> normal = delegator.findByAnd("PartyIntegral",UtilMisc.toMap("levelId",partyLevel.get(0).get("levelId"),"partyIntegralType","NORMAL"));
//                                if(UtilValidate.isNotEmpty(normal)){
//                                    getIntegral = maxAmount.divide(new BigDecimal(normal.get(0).getLong("integralValue"))).setScale(2,BigDecimal.ROUND_HALF_UP);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        if(getIntegral.compareTo(BigDecimal.ZERO) != 0){
//            GenericValue partyScore = null;
//            List<GenericValue> userLogin = null;
//            List<GenericValue> toBeStore = new ArrayList<GenericValue>();
//            partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", orderRole.get(0).get("partyId")));
//            userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", orderRole.get(0).get("partyId")));
//            orderHeader.set("getIntegral",getIntegral);
//            toBeStore.add(orderHeader);
//            if(getIntegral.compareTo(BigDecimal.ZERO) != 0) {
//                partyScore.set("scoreValue", partyScore.getLong("scoreValue") + getIntegral.longValue());
//                toBeStore.add(partyScore);
//            }
//            delegator.storeAll(toBeStore);
//            try {
//                dispatcher.runAsync("partyScoreChange",UtilMisc.toMap("token",userLogin.get(0).get("token"),"changedir","plus","point",getIntegral.intValue(),"vouchercode",orderId,"type","1"));
//            } catch (GenericServiceException e) {
//                e.printStackTrace();
//            }
//        }
//        result.put("resultData",true);
//        return result;
//    }
    
    /**
     * 创建评价记录 Add by Wcy 2016.01.14
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> createProductReviewForWap(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 响应结果 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        Integer productRating = (Integer) context.get("productRating");
        String productReview = (String) context.get("productReview");
        String postAnonymous = (String) context.get("postAnonymous");
        String contentId = (String) context.get("contentIds");
        String[] contentIds = null;
        if (UtilValidate.isNotEmpty(contentId)) {
            contentIds = contentId.split(",");
        }
        /** 获取登录信息 */
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            result.put("resultData", false);
            return result;
        }
        
        /** 获取评价产品 & 对应的店铺 */
        if (UtilValidate.isEmpty(orderId)) {
            result.put("resultData", false);
            return result;
        }
        String productId = "";
        String productStoreId = "";
        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (null != orderHeader) {
                productStoreId = orderHeader.getString("productStoreId");
            }
            GenericValue orderItems = EntityUtil.getFirst(orderHeader.getRelated("OrderItem", UtilMisc.toList("orderItemSeqId")));
            if (null != orderItems) {
                productId = orderItems.getString("productId");
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        
        if (UtilValidate.isEmpty(productId)) {
            result.put("resultData", false);
            return result;
        }
        
        Map<String, Object> paramContext = FastMap.newInstance();
        paramContext.put("userLogin", userLogin);
        paramContext.put("productStoreId", productStoreId);
        paramContext.put("productId", productId);
        paramContext.put("productRating", new BigDecimal(productRating));
        paramContext.put("productReview", productReview);
        paramContext.put("postAnonymous", postAnonymous);
        paramContext.put("orderId", orderId);
        
        try {
            Map<String, Object> resultContext = dispatcher.runSync("createProductReview", paramContext);
            String productReviewID = (String) resultContext.get("productReviewId");
            if (UtilValidate.isNotEmpty(contentIds)) {
                for (int i = 0; i < contentIds.length; i++) {
                    GenericValue productReviewContent = delegator.makeValue("ProductReviewContent");
                    productReviewContent.set("productReviewId", productReviewID);
                    productReviewContent.set("contentId", contentIds[i]);
                    productReviewContent.create();
                }
            }
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            result.put("resultData", false);
            return result;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put("resultData", false);
            return result;
        }
        //更改订单状态为已完成
        Map<String, Object> ctx = FastMap.newInstance();
        ctx.put("statusId", "ORDER_COMPLETED");
        ctx.put("orderId", orderId);
        ctx.put("setItemStatus", "Y");
        ctx.put("userLogin", userLogin);
        ctx.put("locale", Locale.CHINESE);
        Map<String, Object> resp = null;
        
        try {
            OrderServices.setOrderStatus(dispatcher.getDispatchContext(), ctx);
        } catch (Exception e) {
            Debug.logError(e, "Problem calling change item status service : " + ctx, module);
        }
        
        BigDecimal product_Ratings = BigDecimal.ZERO;
        try {
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            Map<String, String> reviewByAnd = UtilMisc.toMap("statusId", "PRR_APPROVED");
            List<GenericValue> reviews = product.getRelatedCache("ProductReview", reviewByAnd, UtilMisc.toList("-postedDateTime"));
            // tally the average
            BigDecimal ratingTally = BigDecimal.ZERO;
            BigDecimal numRatings = BigDecimal.ZERO;
            if (reviews != null) {
                for (GenericValue productReviews : reviews) {
                    BigDecimal rating = productReviews.getBigDecimal("productRating");
                    if (rating != null) {
                        ratingTally = ratingTally.add(rating);
                        numRatings = numRatings.add(BigDecimal.ONE);
                    }
                }
            }
            if (ratingTally.compareTo(BigDecimal.ZERO) > 0 && numRatings.compareTo(BigDecimal.ZERO) > 0) {
                product_Ratings = ratingTally.divide(numRatings, generalRounding);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        //商品评分
        // BigDecimal product_Rating= ProductWorker.getAverageProductRating(delegator,productId);
        List<GenericValue> activityList = null;
        /** 定义团购动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
        dynamicViewEntity.addAlias("PA", "activityId");
        //平均客户评价
        dynamicViewEntity.addAlias("PA", "averageCustomerRating");
        
        dynamicViewEntity.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicViewEntity.addAlias("PAG", "productId");
        dynamicViewEntity.addViewLink("PA", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("activityId");
        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        //填充查询条件,查询字段，排序字段
        try {
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
            activityList = pli.getCompleteList();
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(activityList)) {
            for (GenericValue activityLists : activityList) {
                try {
                    GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityLists.get("activityId")));
                    if (UtilValidate.isNotEmpty(productActivity)) {
                        productActivity.set("averageCustomerRating", product_Ratings);
                        productActivity.store();
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        }
        
        result.put("resultData", true);
        return result;
    }
    
    
    // Add by zhajh at 20180426 根据编码取得订单信息（凯德项目） Begin
    
    /**
     * 根据编码取得订单信息（凯德项目）
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getOrderInfoById(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        //参数
        String orderId = (String) context.get("orderId");
        
        Map<String, Object> productInfoMap = FastMap.newInstance();
        List<GenericValue> orderItems = FastList.newInstance();
        try {
            //根据订单编码取得一条订单信息
            GenericValue orderInfo = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            
            // 初始化
            // 合并支付订单号（js-grouporderId）
            String groupOrderId = "";
            // 下单时间 （js-orderDate）
            String orderDate = "";
            // 用户（js-userName）
            String userName = "";
            // 原始金额：（js-originalPrice）
            String originalPrice = "";
            // 运费：（js-shipCost）
            String shipCost = "";
            // 促销类型：（js-promoType）
            String promoType = "";
            // 促销优惠：(js-promoSale)
            String promoSale = "";
            // 代金券名称：（js-couponName）
            String couponName = "";
            // 代金券优惠：（js-couponSale）
            String couponSale = "";
            // 订单使用积分：（js-orderUseIntegral）
            String orderUseIntegral = "";
            // 积分优惠：（js-integralDiscount）
            String integralDiscount = "";
            // 应付金额：(js-shouldPayMoney)
            String shouldPayMoney = "";
            // 实付金额：(js-actualPayMoney)
            String actualPayMoney = "";
            // 支付方式：(js-payMoney)
            String payMoney = "";
            // 支付流水号：(js-referenceNum)
            String referenceNum = "";
            // 获得积分：(js-getIntegral)
            String getIntegral = "";
            // 店铺：(js-store)
            String store = "";
            // 配送方式：(js-delivery)
            String delivery = "";
            // 快递公司：（js-expressCompany）
            String expressCompany = "";
            // 快递单号：（js-expressNumber）
            String expressNumber = "";
            // 运费：（js-carriage）
            String carriage = "";
            // 收货地址：（js-receiveAddress）
            String receiveAddress = "";
            // 详细地址：（js-detailAddress）
            String detailAddress = "";
            // 收货人：（js-receiver）
            String receiver = "";
            // 电话：（js-tel）
            String tel = "";
            // 客户留言：（js-customerMessage）
            String customerMessage = "";
            // 发票类型：(js-invoiceType)
            String invoiceType = "";
            
            if (UtilValidate.isNotEmpty(orderInfo)) {
                // 标题
                // 订单号
                result.put("orderId", orderInfo.get("orderId"));
                // 合并支付订单号（js-orderGroupId）
                List<GenericValue> orderGroupInfos = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(orderGroupInfos)) {
                    result.put("orderGroupId", orderGroupInfos.get(0).getString("orderGroupId"));
                }
                // 下单时间 （js-orderDate）
                result.put("orderDate", orderInfo.getString("orderDate"));
                // 用户（js-userName）
                userName = OrderReadHelper.getCustomerName(delegator, orderId);
                result.put("userName", userName);
                
                // 取得支付信息
                // 原始金额：（js-originalPrice）
                orderItems = OrderReadHelper.getOrderItems(delegator, orderId);
                BigDecimal bOriginalPrice = BigDecimal.valueOf(0);
                if (UtilValidate.isNotEmpty(orderItems)) {
                    for (GenericValue oi : orderItems) {
                        bOriginalPrice = bOriginalPrice.add(oi.getBigDecimal("unitPrice").multiply(oi.getBigDecimal("quantity")));
                    }
                }
                result.put("originalPrice", bOriginalPrice);
                // 运费：（js-shipCost）
                // 促销类型：（js-promoType）
                // 促销优惠：(js-promoSale)
                // 代金券名称：（js-couponName）
                // 代金券优惠：（js-couponSale）
                // 订单使用积分：（js-orderUseIntegral）
                // 积分优惠：（js-integralDiscount）
                
                // 应付金额：(js-shouldPayMoney)
                result.put("shouldPayMoney", orderInfo.getBigDecimal("shouldPayMoney"));
                // 实付金额：(js-actualPayMoney)
                result.put("shouldPayMoney", orderInfo.getBigDecimal("actualPayMoney"));
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
                if (UtilValidate.isNotEmpty(orderItems)) {
                    for (GenericValue oi : orderItems) {
                        
                        //                         BigDecimal originalPrice = OrderReadHelper.getOrderProductsPrice(delegator,oi.getString("orderId"),oi.getString("orderItemSeqId"));
                        //                         String productSettingName = OrderReadHelper.getProductSettingName(delegator,oi.getString("productId"),oi.getString("orderId"),oi.getString("orderItemSeqId"));
                        //                         GenericValue p =  delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId", oi.getString("productId")));
                        //                         GenericValue activity = delegator.findByPrimaryKey("ProductActivity",UtilMisc.toMap("activityId", oi.getString("activityId")));
                        //                         String returnType = OrderReadHelper.getOrderReturnType(delegator,orderId,oi.getString("productId"));
                        List<GenericValue> fs = OrderReadHelper.getProductFeature(delegator, oi.getString("productId"));
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
                    
                    }
                }
                
                result.put("productInfo", productInfoMap);
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
    // Add by zhajh at 20180426 根据编码取得订单信息（凯德项目） End
    
    
    /**
     * 查看退款日志
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> refundLog(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        Delegator delegator = dctx.getDelegator();
        
        String returnId = (String) context.get("returnId");
        String userLoginId = (String) context.get("userLoginId");
        try {
            List<GenericValue> returnList = delegator.findByAnd("ReturnOperateLog", UtilMisc.toMap("returnId", returnId), UtilMisc.toList("-operateTime"));
            List<Map> newReturnList = FastList.newInstance();
            for (int r = 0; r < returnList.size(); r++) {
                Map<String, Object> returnMap = FastMap.newInstance();
                returnMap.put("operateTime", returnList.get(r).get("operateTime"));
                String operateType = returnList.get(r).getString("operateType");
                String operateTypeDetail = "";
                List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("returnId", returnId));
                BigDecimal actualPaymentMoney = BigDecimal.ZERO;
                if (UtilValidate.isNotEmpty(returnItems)){
                    GenericValue returnItem = returnItems.get(0);
                    String statusId = returnItem.getString("statusId");
                    if ("RETURN_COMPLETED".equals(statusId)){//已经完成
                        actualPaymentMoney = returnItem.getBigDecimal("actualPaymentMoney").setScale(0,BigDecimal.ROUND_DOWN);
                    }
                }
                String operType1 = "退款" + actualPaymentMoney + "元";

                //退款 退货申请状态捕捉
                if ("申请退货".equals(operateType)) {
                    operateTypeDetail = "您的服务单已申请成功，待售后审核中。";
                } else if ("同意退款".equals(operateType)) {
                    operateTypeDetail = "您的服务申请已通过，退款处理中。";
                } else if ("申请退款".equals(operateType)) {
                    operateTypeDetail = "您的服务单已申请成功，待售后审核中。";
                } else if (operType1.equals(operateType)) {
                    operateTypeDetail = "您的服务单已退款，将于15个工作日之内退款到您的支付账户中，请注意查收。";
                } else if ("同意退货".equals(operateType)) {
                    operateTypeDetail = "您的服务申请已通过，退款处理中。";
                }
                
                String operator = returnList.get(r).getString("operator");
                if (userLoginId.equals(operator)){
                    returnMap.put("operator", "系统");
                }else {
                    GenericValue userLogin = delegator.findByPrimaryKey("UserLogin",UtilMisc.toMap("userLoginId",operator));
                    String partyId = "";
                    if (UtilValidate.isNotEmpty(userLogin)){
                        partyId = userLogin.getString("partyId");
                    }
                    GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
                    if (UtilValidate.isNotEmpty(person)){
                        String name = UtilValidate.isNotEmpty(person.getString("name"))?person.getString("name"):null;
                        returnMap.put("operator", name);
                    }
                }
                returnMap.put("operateType", operateTypeDetail);
                newReturnList.add(returnMap);
            }
            resultData.put("returns", newReturnList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        successResult.put("resultData", resultData);
        return successResult;
    }
    
    
    public static String exportOrder(HttpServletRequest request, HttpServletResponse response) throws IOException, GenericEntityException {
        String orderIds = request.getParameter("ids");
        String paymentMethodTypeInfo = request.getParameter("paymentMethodType");// 支付方法
        String communityStoreIdInfo = request.getParameter("communityStoreId");// 门店信息
        String businessNameInfo = request.getParameter("businessName");// 商家信息
        String salesOrderType = request.getParameter("salesOrderType");// 销售订单类型
        
        String fileName = "订单" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        if (UtilValidate.isNotEmpty(salesOrderType)) {
            if ("FINISHED".equals(salesOrderType)) {
                fileName = "实物" + fileName;
            } else if ("VIRTUAL".equals(salesOrderType)) {
                fileName = "虚拟" + fileName;
            }
        }
        String name = new String(fileName.getBytes("utf-8"), "utf-8");
        Locale locale = UtilHttp.getLocale(request);
        if (UtilValidate.isNotEmpty(orderIds)) {
            Delegator delegator = (Delegator) request.getAttribute("delegator");
            List<String> orderProdctIdsList = UtilMisc.toListArray(orderIds.split(","));
            EntityCondition condition = EntityCondition.makeCondition("orderId", EntityOperator.IN, orderProdctIdsList);
            List<GenericValue> orderHeaders = null;
            try {
                orderHeaders = delegator.findList("OrderHeader", condition, null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
            //设置response相应属性，设置为下载

            String utf = "UTF-8";
            String headStr = "attachment; filename=\"" + URLEncoder.encode(fileName, utf) + ".xls"+"\"";
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", headStr);

//            response.setContentType("application/x-msdownload");
//            response.setHeader("Content-Disposition", "attachment;filename=" + name);
            //获得response中的输出流
            OutputStream out = null;
            out = response.getOutputStream();
            //excel表格第一行标题，本例中不采用poi或jxl，只对每个单元属性后面添//加\t，每行结束加\n。这是excel文件的基本属性。
//            String head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t实付金额\t支付方式\t订单状态\t商家\n";
//            String head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t实付金额\t支付方式\t订单状态\n";
            String headEnd = "\n";
            String head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t实付金额\t支付方式\t订单状态";
            if ("FINISHED".equals(salesOrderType)) {
                head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t收货地址\t实付金额\t支付方式\t订单状态";
            }
            head += headEnd;
            int id = 0;
            out.write(head.getBytes("gbk"));
            if (UtilValidate.isNotEmpty(orderHeaders)) {
                for (GenericValue orderHeader : orderHeaders) {
                    id++;
                    String orderId = orderHeader.getString("orderId");
                    //商品
                    List<GenericValue> products = OrderReadHelper.getOrderProducts(delegator, orderHeader.getString("orderId"));
                    //商品数量
                    BigDecimal num = OrderReadHelper.getOrderProductsNum(delegator, orderHeader.getString("orderId"));
                    //购买人信息
                    Map<String, Object> purchaseInfo = OrderReadHelper.getToCustomerInfo(delegator, orderHeader.getString("orderId"));
                    // 客户信息
                    String customerName = OrderReadHelper.getCustomerName(delegator, orderHeader.getString("orderId"));
                    //支付方式
                    String paymentMethodName = "";
                    List<GenericValue> orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                    if (UtilValidate.isNotEmpty(orderPaymentPreferences)) {
                        GenericValue orderPaymentPreference = orderPaymentPreferences.get(0);
                        GenericValue paymentMethod = delegator.findByPrimaryKey("PaymentMethodType", UtilMisc.toMap("paymentMethodTypeId", orderPaymentPreference.get("paymentMethodTypeId")));
                        if (UtilValidate.isNotEmpty(paymentMethodTypeInfo)) {
                            if (orderPaymentPreference.get("paymentMethodTypeId").equals(paymentMethodTypeInfo)) {
                                paymentMethodName = (String) paymentMethod.get("description", locale);
                            }
                        } else {
                            paymentMethodName = (String) paymentMethod.get("description", locale) + "++";
                        }
                    }
                    String orderStatus = (String) orderHeader.getRelatedOneCache("StatusItem").get("description", locale);
                    String productName = "";
                    for (GenericValue p : products) {
                        productName += p.get("productName") + ",";
                    }
                    productName = (productName != "") ? productName.substring(0, productName.length() - 1) : "";
                    StringBuffer conBuffer = new StringBuffer();
                    conBuffer.append(id + "\t");
                    conBuffer.append(orderId + "\t");
                    conBuffer.append(outPutString(productName) + "\t");
                    conBuffer.append(outPutString(orderHeader.getBigDecimal("grandTotal") + "") + "\t");
                    conBuffer.append(+num.intValue() + "\t");
                    conBuffer.append(outPutString(sdf.format(orderHeader.getTimestamp("orderDate"))) + "\t");
                    conBuffer.append(outPutString(UtilValidate.isEmpty(customerName) ? "" : customerName + "") + "\t");
                    conBuffer.append(outPutString(UtilValidate.isEmpty(purchaseInfo.get("toName")) ? "" : purchaseInfo.get("toName") + "") + "\t");
                    conBuffer.append(outPutString(UtilValidate.isEmpty(purchaseInfo.get("mobilePhone")) ? "" : purchaseInfo.get("mobilePhone") + "") + "\t");
                    if ("FINISHED".equals(salesOrderType)) {
                        conBuffer.append(outPutString(UtilValidate.isEmpty(purchaseInfo.get("address")) ? "" : purchaseInfo.get("address") + " ") +
                                outPutString(UtilValidate.isEmpty(purchaseInfo.get("detailAddress")) ? "" : purchaseInfo.get("detailAddress") + "") + "\t");
                    }
                    conBuffer.append((UtilValidate.isEmpty(orderHeader.get("grandTotal")) ? 0 : orderHeader.get("grandTotal")) + "\t");
                    conBuffer.append(paymentMethodName + "\t");
                    conBuffer.append(orderStatus + "\n");
                    String content = conBuffer.toString();
                    out.write(content.getBytes("gbk"));
                }
                out.flush();
                out.close();
            }
        }
        return "success";
    }
    
    // Add by zhajh at 20160628 订单导出（根据条件批量） Begin
    
    /**
     * 订单导出（根据条件批量）
     *
     * @param request
     * @param response
     * @return
     */
    public static String exportOrderForAll(HttpServletRequest request, HttpServletResponse response) throws IOException, GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String salesOrderType = request.getParameter("salesOrderType");// 销售订单类型
        
        String fileName = "订单_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        if (UtilValidate.isNotEmpty(salesOrderType)) {
            if ("FINISHED".equals(salesOrderType)) {
                fileName = "实物" + fileName;
            } else if ("VIRTUAL".equals(salesOrderType)) {
                fileName = "虚拟" + fileName;
            }
        }
        String name = new String(fileName.getBytes("utf-8"), "utf-8");
        Locale locale = UtilHttp.getLocale(request);
        
        HttpSession session = request.getSession();

//        String orderId = request.getParameter("orderId");
//        String receivePartyName = request.getParameter("receivePartyName");
//        String partyPhone = request.getParameter("partyPhone");
//        String productName = request.getParameter("productName");
//        String billToName = request.getParameter("billToName");
//        String orderType = request.getParameter("orderType");
//        String startDate = request.getParameter("startDate");
//        String endDate = request.getParameter("endDate");
//        String logisticsNumber1 = request.getParameter("logisticsNumber1");

//        String isHasPaymentMethodType = request.getParameter("isHasPaymentMethodType");// 是否有支付选项
//        String isHasCommunityStore = request.getParameter("isHasCommunityStore");// 是否有门店选项
//        String isHasBusinessName = request.getParameter("isHasBusinessName");// 是否有商家选项
        String paymentMethodTypeInfo = request.getParameter("paymentMethodType");// 支付方法
//        String communityStoreIdInfo = request.getParameter("communityStoreId");// 门店信息
//        String businessNameInfo = request.getParameter("businessName");// 商家信息
        
        
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        String orderStatus = request.getParameter("orderStatus");
        String sortField = "-orderDate";
        // 选择字段List
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("orderId");
        fieldsToSelect.add("orderDate");
        fieldsToSelect.add("grandTotal");
        fieldsToSelect.add("statusId");
//        fieldsToSelect.add("discountMoney");
//        fieldsToSelect.add("shouldPayMoney");
//        fieldsToSelect.add("actualPayMoney");
//        fieldsToSelect.add("notPayMoney");
//        fieldsToSelect.add("distributionMethod");
//        fieldsToSelect.add("getIntegral");
//        fieldsToSelect.add("deliveryDate");
        fieldsToSelect.add("originFacilityId");
        fieldsToSelect.add("orderTypeId");
//        fieldsToSelect.add("complainId");
//        fieldsToSelect.add("distributeMoney");
        fieldsToSelect.add("remarks");
//        fieldsToSelect.add("freeFee");
//        fieldsToSelect.add("salesOrderType");
//        fieldsToSelect.add("commStoreId");
//        fieldsToSelect.add("buyerTelphone");
//        fieldsToSelect.add("appointmentName");
//        fieldsToSelect.add("servicePersonName");
        
        // list to hold the parameters
        List<String> paramList = FastList.newInstance();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        // list of conditions
        List<EntityCondition> conditions = FastList.newInstance();
        // dynamic view entity
        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("OH", "OrderHeader");
        dve.addAliasAll("OH", "", null); // no prefix
        dve.addRelation("one-nofk", "", "OrderType", UtilMisc.toList(new ModelKeyMap("orderTypeId", "orderTypeId")));
        dve.addRelation("one-nofk", "", "StatusItem", UtilMisc.toList(new ModelKeyMap("statusId", "statusId")));
        dve.addMemberEntity("OI", "OrderItem");
        dve.addMemberEntity("P", "Product");
        dve.addAlias("P", "productId");
        dve.addAlias("P", "productTypeId");
        dve.addAlias("P", "productName");
        dve.addAlias("P", "businessPartyId");
        dve.addAliasAll("OI", "", null);
        dve.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
        dve.addViewLink("OI", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));
        dve.addMemberEntity("OD", "OrderDelivery");
        dve.addAliasAll("OD", "", null);
        dve.addViewLink("OH", "OD", Boolean.TRUE, ModelKeyMap.makeKeyMapList("orderId"));

//        if (UtilValidate.isNotEmpty(salesOrderType)) {
//            conditions.add(EntityCondition.makeCondition("salesOrderType", EntityOperator.EQUALS, salesOrderType));
//        }
        if (UtilValidate.isNotEmpty(salesOrderType)) {
            if ("FINISHED".equals(salesOrderType)) {
                conditions.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
            } else if ("VIRTUAL".equals(salesOrderType)) {
                conditions.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "VIRTUAL_GOOD"));
            }
        }
        String isInner = request.getParameter("isInner"); // 是否自营 Y: 自营 N：非自营
        String productStoreId = request.getParameter("productStoreId");// 商家编码
        if("N".equals(isInner)) {
            if (UtilValidate.isNotEmpty(productStoreId)) {
                conditions.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
            }
        }
//        conditions.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
//        String userCategory = "";
//        try {
//            GenericValue userLoginParty = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
//            userCategory = UtilValidate.isNotEmpty(userLoginParty.getString("partyCategory")) ? userLoginParty.getString("partyCategory") : "";
//        } catch (GenericEntityException e) {
//            e.printStackTrace();
//        }
//        if (userCategory.equals("BUSINESS")) {               //登录用户为商家
//            conditions.add(EntityCondition.makeCondition("businessPartyId", EntityOperator.EQUALS, userLogin.getString("partyId")));
//        }
//
//
//        if (UtilValidate.isNotEmpty(orderStatus)) {
//            paramList.add("orderStatus=" + orderStatus);
//            paramMap.put("orderStatus", orderStatus);
//            if (orderStatus.equals("ORDER_RETURNED")) {       //退单
//                dve.addMemberEntity("RI", "ReturnItem");
//                dve.addAlias("RI", "returnStatusId", "statusId", null, null, null, null);
//                dve.addAliasAll("RI", "", UtilMisc.toSet("statusId"));
//                dve.addViewLink("OH", "RI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
//                conditions.add(EntityCondition.makeCondition("returnStatusId", EntityOperator.NOT_EQUAL, "RETURN_REJECTAPPLY"));
//            } else if (orderStatus.equals("AlreadyShip")) {//已发货，出库列表需要
//                conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_WAITRECEIVE", "ORDER_WAITEVALUATE", "ORDER_COMPLETED", "ORDER_CANCELLED")));
//                conditions.add(EntityCondition.makeCondition("logisticsNumber1", EntityOperator.NOT_EQUAL, null));
//            } else {
//                conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, orderStatus));
//            }
//            if (orderStatus.equals("ORDER_WAITSHIP")) {
//                sortField = "orderId";
//            }
//        }
//
//        // 订单编号
//        if (UtilValidate.isNotEmpty(orderId)) {
//            paramList.add("orderId=" + orderId);
//            paramMap.put("orderId", orderId);
//            conditions.add(EntityCondition.makeCondition("orderId", EntityOperator.LIKE, "%" + orderId + "%"));
//        }
//
//        // 订单类型
//        if (UtilValidate.isNotEmpty(orderType)) {
//            paramList.add("orderType=" + orderType);
//            paramMap.put("orderType", orderType);
//            conditions.add(EntityCondition.makeCondition("orderTypeId", orderType));
//        }
//        if (UtilValidate.isNotEmpty(logisticsNumber1)) {
//            paramList.add("logisticsNumber1=" + logisticsNumber1);
//            paramMap.put("logisticsNumber1", logisticsNumber1);
//            conditions.add(EntityCondition.makeCondition("logisticsNumber1", EntityOperator.LIKE, "%" + logisticsNumber1 + "%"));
//        }
//
////        if (UtilValidate.isNotEmpty(receivePartyName) || UtilValidate.isNotEmpty(partyPhone)) {
////            dve.addMemberEntity("OCM", "OrderContactMech");
////            dve.addMemberEntity("PA", "PostalAddress");
////            dve.addAliasAll("OCM", "", null);
////            dve.addAliasAll("PA", "", null);
////            dve.addViewLink("OH", "OCM", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
////            dve.addViewLink("OCM", "PA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("contactMechId"));
////
////        }
//
//        if (UtilValidate.isNotEmpty(receivePartyName)) {
//            paramList.add("receivePartyName=" + receivePartyName);
//            paramMap.put("receivePartyName", receivePartyName);
//            if (UtilValidate.isNotEmpty(salesOrderType)) {
//                if (salesOrderType.equals("REPAIR_ORDER")) {
//                    conditions.add(EntityCondition.makeCondition("servicePersonName", EntityOperator.LIKE, "%" + receivePartyName + "%"));
//                } else if (salesOrderType.equals("INSURANCE_ORDER")) {
//                    conditions.add(EntityCondition.makeCondition("appointmentName", EntityOperator.LIKE, "%" + receivePartyName + "%"));
//                } else {
//                    conditions.add(EntityCondition.makeCondition("buyerName", EntityOperator.LIKE, "%" + receivePartyName + "%"));
//                }
//            }
//        }
//
//
//        if (UtilValidate.isNotEmpty(partyPhone)) {
//            paramList.add("partyPhone=" + partyPhone);
//            paramMap.put("partyPhone", partyPhone);
//            conditions.add(EntityCondition.makeCondition("buyerTelphone", EntityOperator.LIKE, "%" + partyPhone + "%"));
//        }
//
//        if (UtilValidate.isNotEmpty(productName)) {
//            paramList.add("productName=" + productName);
//            paramMap.put("productName", productName);
//            conditions.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + productName + "%"));
//        }
//
//
//        if (UtilValidate.isNotEmpty(billToName)) {
//            paramList.add("billToName=" + billToName);
//            paramMap.put("billToName", billToName);
//            dve.addMemberEntity("OT", "OrderRole");
//            dve.addAliasAll("OT", "", null);
//            dve.addMemberEntity("PP", "PartyAndPerson");
//            dve.addAlias("PP", "partyId");
//            dve.addAlias("PP", "name");
//            dve.addViewLink("OH", "OT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
//            dve.addViewLink("OT", "PP", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));
//            conditions.add(EntityCondition.makeCondition("name", EntityOperator.LIKE, "%" + billToName + "%"));
//            conditions.add(EntityCondition.makeCondition("roleTypeId", "BILL_TO_CUSTOMER"));
//        }
//
//
//        if (UtilValidate.isNotEmpty(startDate) && startDate.length() > 8) {
//            paramList.add("startDate=" + startDate);
//            paramMap.put("startDate", startDate);
//            startDate = startDate.trim();
//            if (startDate.length() < 14) startDate = startDate + " " + "00:00:00.000";
//            try {
//                Object converted = ObjectType.simpleTypeConvert(startDate, "Timestamp", null, null);
//                if (converted != null) {
//                    conditions.add(EntityCondition.makeCondition("orderDate", EntityOperator.GREATER_THAN_EQUAL_TO, converted));
//                }
//            } catch (GeneralException e) {
//                Debug.logWarning(e.getMessage(), module);
//            }
//        }
//
//        if (UtilValidate.isNotEmpty(endDate) && endDate.length() > 8) {
//            paramList.add("endDate=" + endDate);
//            paramMap.put("endDate", endDate);
//            endDate = endDate.trim();
//            if (endDate.length() < 14) endDate = endDate + " " + "23:59:59.999";
//            try {
//                Object converted = ObjectType.simpleTypeConvert(endDate, "Timestamp", null, null);
//                if (converted != null) {
//                    conditions.add(EntityCondition.makeCondition("orderDate", EntityOperator.LESS_THAN_EQUAL_TO, converted));
//                }
//            } catch (GeneralException e) {
//                Debug.logWarning(e.getMessage(), module);
//            }
//        }
        
        
        List<GenericValue> orderList = FastList.newInstance();
        
        // set distinct on so we only get one row per order
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        // create the main condition
        EntityCondition cond = null;
        if (conditions.size() > 0) {
            cond = EntityCondition.makeCondition(conditions, EntityOperator.AND);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        //设置response相应属性，设置为下载
        String utf = "UTF-8";
        String headStr = "attachment; filename=\"" + URLEncoder.encode(fileName, utf) + ".xls"+"\"";
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setHeader("Content-Disposition", headStr);


//        response.setContentType("application/x-msdownload");
//        response.setHeader("Content-Disposition", "attachment;filename=" + name);
        //获得response中的输出流
        OutputStream out = null;
        out = response.getOutputStream();
        //excel表格第一行标题，本例中不采用poi或jxl，只对每个单元属性后面添//加\t，每行结束加\n。这是excel文件的基本属性。
//         String head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t实付金额\t支付方式\t订单状态\t商家\n";
        String headEnd = "\n";
        String head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t实付金额\t支付方式\t订单状态";
        if ("FINISHED".equals(salesOrderType)) {
            head = "序号\t订单号\t商品\t总价\t数量\t下单时间\t用户\t收货人\t电话\t收货地址\t实付金额\t支付方式\t订单状态";
        }
        head += headEnd;
        int id = 0;
        out.write(head.getBytes("gbk"));
        
        EntityListIterator eli = null;
        try {
            // do the lookup
            eli = delegator.findListIteratorByCondition(dve, cond, null, fieldsToSelect, UtilMisc.toList(sortField), findOpts);
            // get the partial list for this page
            eli.beforeFirst();
            orderList = eli.getCompleteList();
            // 查询的数据Iterator
            for (GenericValue gv : orderList) {
                String curOrderId = gv.getString("orderId");
                GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", curOrderId));
                id++;
                //商品
                List<GenericValue> products = OrderReadHelper.getOrderProducts(delegator, orderHeader.getString("orderId"));
                //商品数量
                BigDecimal num = OrderReadHelper.getOrderProductsNum(delegator, orderHeader.getString("orderId"));
                //购买人信息
                Map<String, Object> purchaseInfo = OrderReadHelper.getToCustomerInfo(delegator, orderHeader.getString("orderId"));
                // 客户信息
                String customerName = OrderReadHelper.getCustomerName(delegator, orderHeader.getString("orderId"));
                //支付方式
                String paymentMethodName = "";
                List<GenericValue> orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                if (UtilValidate.isNotEmpty(orderPaymentPreferences)) {
                    GenericValue orderPaymentPreference = orderPaymentPreferences.get(0);
                    GenericValue paymentMethod = delegator.findByPrimaryKey("PaymentMethodType", UtilMisc.toMap("paymentMethodTypeId", orderPaymentPreference.get("paymentMethodTypeId")));
                    if (UtilValidate.isNotEmpty(paymentMethodTypeInfo)) {
                        if (orderPaymentPreference.get("paymentMethodTypeId").equals(paymentMethodTypeInfo)) {
                            paymentMethodName = (String) paymentMethod.get("description", locale);
                        }
                    } else {
                        paymentMethodName = (String) paymentMethod.get("description", locale) + "++";
                    }
                }
                String orderStatusForFile = (String) orderHeader.getRelatedOneCache("StatusItem").get("description", locale);
                String productNameForFile = "";
                for (GenericValue p : products) {
                    productNameForFile += p.get("productName") + ",";
                }
                productNameForFile = (productNameForFile != "") ? productNameForFile.substring(0, productNameForFile.length() - 1) : "";
                StringBuffer conBuffer = new StringBuffer();
                conBuffer.append(id + "\t");
                conBuffer.append(curOrderId + "\t");
                conBuffer.append(outPutString(productNameForFile) + "\t");
                conBuffer.append(outPutString(orderHeader.getBigDecimal("grandTotal") + "") + "\t");
                conBuffer.append(+num.intValue() + "\t");
                conBuffer.append(outPutString(sdf.format(orderHeader.getTimestamp("orderDate"))) + "\t");
                conBuffer.append(outPutString(UtilValidate.isEmpty(customerName) ? "" : customerName + "") + "\t");
                conBuffer.append(outPutString(UtilValidate.isEmpty(purchaseInfo.get("toName")) ? "" : purchaseInfo.get("toName") + "") + "\t");
                conBuffer.append(outPutString(UtilValidate.isEmpty(purchaseInfo.get("mobilePhone")) ? "" : purchaseInfo.get("mobilePhone") + "") + "\t");
                if ("FINISHED".equals(salesOrderType)) {
                    conBuffer.append(outPutString(UtilValidate.isEmpty(purchaseInfo.get("address")) ? "" : purchaseInfo.get("address") + " ") +
                            outPutString(UtilValidate.isEmpty(purchaseInfo.get("detailAddress")) ? "" : purchaseInfo.get("detailAddress") + "") + "\t");
                }
                conBuffer.append((UtilValidate.isEmpty(orderHeader.get("grandTotal")) ? 0 : orderHeader.get("grandTotal")) + "\t");
                conBuffer.append(paymentMethodName + "\t");
                conBuffer.append(orderStatusForFile + "\n");
                String content = conBuffer.toString();
                out.write(content.getBytes("gbk"));
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return "error";
        } finally {
            if (eli != null) {
                try {
                    eli.close();
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, e.getMessage(), module);
//                    return "error";
                }
            }
        }
        
        return "success";
    }
    // Add by zhajh at 20160628 订单导出（根据条件批量） End
}

