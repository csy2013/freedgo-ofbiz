package com.yuaoq.yabiz.app.mobile.microservice.order.api.v1;

import com.google.gson.Gson;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.order.shoppingcart.CheckOutEvents;
import com.yuaoq.yabiz.mobile.order.shoppingcart.ShippingEvents;
import com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCart;
import com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCartEvents;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.order.shoppingcart.product.ProductPromoWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by changsy on 2018/3/24.
 */

@RestController
@RequestMapping(path = "/api/order/v1")
public class OrderControllerV1 {
    
    public static final String module = OrderControllerV1.class.getName();
    
    /**
     * 创建订单
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/createOrderFromCart", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createOrder(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        boolean success = true;
        List<String> orders = FastList.newInstance();
        //获取ShoppingCart
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        String orderGroupType = "";
        String orderGroupBusId = "";
        Map<String, Object> result = createOrderBase(request, loginName, delegator, resultData, dispatcher, success, orders, cart, orderGroupType, orderGroupBusId, "shoppingCart");
        
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
        
    }
    
    private Map<String, Object> createOrderBase(HttpServletRequest request, String loginName, Delegator delegator, Map<String, Object> resultData, LocalDispatcher dispatcher, boolean success, List<String> orders, ShoppingCart cart, String orderGroupType, String orderGroupBusId, String orderType) {
        //做一下促销
        ProductPromoWorker.doPromotions(cart, dispatcher);
        //获取登录用户信息
        //渠道ID
        String channelId = request.getParameter("channelId");
        //发票信息
        String needInvoice = request.getParameter("needInvoice");
        String invoiceType = request.getParameter("invoiceType");
        String taxNo = request.getParameter("taxNo");
        String invoiceTitle = request.getParameter("invoiceTitle");
        String invoiceContentTypeId = request.getParameter("invoiceContentTypeId");
        //用户的Crm信息
        String member_id = request.getParameter("member_id");
        String mall_id = request.getParameter("mall_id");
        //用户的省份证信息
        String partyCardNo = request.getParameter("partyNo");
        //优惠劵
        String couponCode = request.getParameter("couponCode");
        String productPromoCodeId = request.getParameter("productPromoCodeId");
        //积分
        String useIntegral = request.getParameter("useIntegral");
        String integralDiscount = request.getParameter("integralDiscount");
        String activityId = request.getParameter("activityId");
        
        //配送方式 String shippingMethod = typeId+"@"+shipPartyId;
        String shipmentMethod = request.getParameter("shipmentMethod");
        //支付方式 EXT_PING
        String payMethod = request.getParameter("payMethod");
        String webSiteId = request.getHeader("client");
        //收货地址
        String token = request.getParameter("token");
        //虚拟商品购买ticketId
        String ticketId = request.getParameter("ticketId");
        String shippingContactMechId = request.getParameter("shippingContactMechId");
        
        
        if ((cart == null) || UtilValidate.isEmpty(cart.items())) {
            String errMsg = UtilProperties.getMessage("OrderErrorUiLabels", "checkevents.cart_empty",
                    (cart != null ? cart.getLocale() : UtilHttp.getLocale(request)));
            resultData.put("retCode", "0");
            resultData.put("message", errMsg);
        }
        
        Map<String, BigDecimal> useIntegralStoreMap = FastMap.newInstance();
        Map<String, BigDecimal> integralDiscountStoreMap = FastMap.newInstance();
        
        //积分分摊
        scoreAssignForCart(delegator, cart, useIntegral, integralDiscount, useIntegralStoreMap, integralDiscountStoreMap);
        
        
        //获取没有参与促销的订单项总金额
        BigDecimal toalAmountUnPromo = getAllCartTotalAmountUnPromo(cart);
        Map<String, BigDecimal> tatalAmountUnPromoByStoreId = getAllCartTotalAmountUnPromoByStoreId(cart);
        //计算出没有参与促销的店铺总金额
        Map<String, String> storeCouponMap = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(productPromoCodeId)) {
            String[] codeIds = productPromoCodeId.split(",");
            for (int i = 0; i < codeIds.length; i++) {
                String codeId = codeIds[i];
                try {
                    GenericValue codeAndParty = EntityUtil.getFirst(EntityUtil.filterByDate(delegator.findByAnd("PromoCouponCodeAndParty", UtilMisc.toMap("productPromoCodeId", codeId))));
                    //判断优惠劵的使用有效期
                    if (UtilValidate.isEmpty(codeAndParty)) {
                        resultData.put("retCode", "0");
                        resultData.put("message", "优惠劵超出有效使用时间");
                        return resultData;
                    }
                    String couponCodeStr = codeAndParty.getString("couponCode");
                    List<GenericValue> coupon = delegator.findByAnd("ProductStorePromoAndCoupon", UtilMisc.toMap("couponCode", couponCodeStr));
                    GenericValue couponVal = coupon.get(0);
                    String applyScope = couponVal.getString("applyScope");
                    String couponProductType = couponVal.getString("couponProductType");
                    
                    String promoCodeStatus = codeAndParty.getString("promoCodeStatus");
                    if ("D".equals(promoCodeStatus) || "U".equals(promoCodeStatus)) {
                        resultData.put("retCode", "0");
                        resultData.put("message", "优惠劵已经被使用或者过期");
                        return resultData;
                    }
                    String useWithScore = couponVal.getString("useWithScore");
                    String productStoreId = couponVal.getString("productStoreId");
                    if (UtilValidate.isNotEmpty(useWithScore) && "N".equals(useWithScore)) {
                        if (UtilValidate.isNotEmpty(useIntegral)) {
                            resultData.put("retCode", "0");
                            resultData.put("message", "优惠劵不能和积分一起使用");
                            return resultData;
                        }
                    }
                    BigDecimal payReduce = couponVal.getBigDecimal("payReduce");
                    BigDecimal payFill = new BigDecimal(couponVal.getLong("payFill"));
                    if (UtilValidate.isNotEmpty(applyScope) && "A".equals(applyScope) && "COUPON_PRT_ALL".equals(couponProductType) ) {
                        //全渠道的优惠劵
                        if (payFill.compareTo(toalAmountUnPromo) > 0) {
                            resultData.put("retCode", "0");
                            resultData.put("message", "订单金额小于优惠劵优惠条件");
                            return resultData;
                        }
                        //计算全渠道的优惠劵分摊
                        for (ShoppingCartItem cartItem : cart.items()) {
                            createCouponOrderAdjustment(delegator, cart, productPromoCodeId, couponVal, payReduce, toalAmountUnPromo, cartItem);
                            
                        }
                        
                        
                    } else if ("COUPON_PRT_ALL".equals(couponProductType) && "S".equals(applyScope)) {
                        //店铺通用
                        BigDecimal toatlStoreCouponAmount = tatalAmountUnPromoByStoreId.get(productStoreId);
                        if (payFill.compareTo(toatlStoreCouponAmount) > 0) {
                            resultData.put("retCode", "0");
                            resultData.put("message", "订单金额小于优惠劵优惠条件");
                            return resultData;
                        }
                        
                        //计算店铺的优惠劵分摊
                        for (ShoppingCartItem cartItem : cart.items()) {
                            createCouponOrderAdjustment(delegator, cart, productPromoCodeId, couponVal, productStoreId, payReduce, toatlStoreCouponAmount, cartItem);
//                            System.out.println("cartItem adjustments = " + cartItem.getAdjustments());
                        }
                        
                    } else {
                        //指定商品
                        BigDecimal toatlProductsCouponAmount = BigDecimal.ZERO;
                        List<GenericValue> couponProducts = delegator.findByAnd("ProductCouponProduct", UtilMisc.toMap("couponCode", couponVal.getString("couponCode")));
                        //获取指定商品的总金额,比较指定的商品和购物车商品
                        for (ShoppingCartItem cartItem : cart.items()) {
                            if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo()))) {
                                for (GenericValue couponProduct : couponProducts) {
                                    if (cartItem.getProductId().equals(couponProduct.getString("productId"))) {
                                        BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                                        toatlProductsCouponAmount = toatlProductsCouponAmount.add(itemAmount);
                                    }
                                }
                                
                            }
                        }
                        
                        if (payFill.compareTo(toatlProductsCouponAmount) > 0) {
                            resultData.put("retCode", "0");
                            resultData.put("message", "订单金额小于优惠劵优惠条件");
                            return resultData;
                        }
                        
                        //计算优惠劵分摊
                        for (ShoppingCartItem cartItem : cart.items()) {
                            for (GenericValue couponProduct : couponProducts) {
                                if (cartItem.getProductId().equals(couponProduct.getString("productId"))) {
                                    createCouponOrderAdjustment(delegator, cart, productPromoCodeId, couponVal, payReduce, toatlProductsCouponAmount, cartItem);
                                }
                            }
                        }
                    }
                    
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }catch (Exception e){
                    e.printStackTrace();
                }
                
            }
        }
        
        
        //查询ticket可以适用的店铺
        Map<String, String> storeTicketMap = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(ticketId)) {
            String[] ticketIds = ticketId.split(",");
            for (int i = 0; i < ticketIds.length; i++) {
                String ticketId1 = ticketIds[i];
                try {
                    List<GenericValue> ticketAndStore = delegator.findByAnd("TicketAndProductAndStore", UtilMisc.toMap("ticketId", ticketId1));
                    if (UtilValidate.isNotEmpty(ticketAndStore)) {
                        String productStoreId = ticketAndStore.get(0).getString("productStoreId");
                        GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
                        String partyId = productStore.getString("ownerPartyId");
                        GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", partyId));
                        String isInner = partyGroup.getString("isInner");
                        String productId = ticketAndStore.get(0).getString("productId");
                        BigDecimal voucherAmount = ticketAndStore.get(0).getBigDecimal("voucherAmount");
                        List<GenericValue> products = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_CONF"));
                        if ("Y".equals(isInner)) {
                            if (UtilValidate.isEmpty(products)) {
                                //代表是全场通用劵
                                for (ShoppingCartItem cartItem : cart.items()) {
                                    createTicketOrderAdjustment(delegator, cart, ticketAndStore.get(0), voucherAmount, toalAmountUnPromo, cartItem);
                                }
                            } else {
                                //指定商品
                                //获取指定商品的总金额,比较指定的商品和购物车商品
                                BigDecimal toatlProductsTicketAmount = BigDecimal.ZERO;
                                for (ShoppingCartItem cartItem : cart.items()) {
                                    if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo()))) {
                                        for (GenericValue ticketProduct : products) {
                                            if (cartItem.getProductId().equals(ticketProduct.getString("productIdTo"))) {
                                                BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                                                toatlProductsTicketAmount = toatlProductsTicketAmount.add(itemAmount);
                                            }
                                        }
                                        
                                    }
                                }
                                //计算全渠道的优惠劵分摊
                                for (ShoppingCartItem cartItem : cart.items()) {
                                    for (GenericValue ticketProduct : products) {
                                        if (cartItem.getProductId().equals(ticketProduct.getString("productIdTo"))) {
                                            createTicketOrderAdjustment(delegator, cart, ticketAndStore.get(0), voucherAmount, toatlProductsTicketAmount, cartItem);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (UtilValidate.isEmpty(products)) {
                                BigDecimal toatlProductsTicketAmount = BigDecimal.ZERO;
                                for (ShoppingCartItem cartItem : cart.items()) {
                                    if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo())) && cartItem.getProductStoreId().equals(productStoreId)) {
                                        BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                                        toatlProductsTicketAmount = toatlProductsTicketAmount.add(itemAmount);
                                    }
                                }
                                //代表是店铺通用劵
                                for (ShoppingCartItem cartItem : cart.items()) {
                                    if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo())) && cartItem.getProductStoreId().equals(productStoreId)) {
                                        createTicketOrderAdjustment(delegator, cart, ticketAndStore.get(0), productStoreId, voucherAmount, toatlProductsTicketAmount, cartItem);
                                    }
                                }
                                
                                
                            } else {
                                //指定商品
                                //获取指定商品的总金额,比较指定的商品和购物车商品
                                BigDecimal toatlProductsTicketAmount = BigDecimal.ZERO;
                                for (ShoppingCartItem cartItem : cart.items()) {
                                    if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo())) && cartItem.getProductStoreId().equals(productStoreId)) {
                                        for (GenericValue ticketProduct : products) {
                                            if (cartItem.getProductId().equals(ticketProduct.getString("productIdTo"))) {
                                                BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                                                toatlProductsTicketAmount = toatlProductsTicketAmount.add(itemAmount);
                                            }
                                        }
                                        
                                    }
                                }
                                for (ShoppingCartItem cartItem : cart.items()) {
                                    for (GenericValue ticketProduct : products) {
                                        if (cartItem.getProductId().equals(ticketProduct.getString("productIdTo"))) {
                                            createTicketOrderAdjustment(delegator, cart, ticketAndStore.get(0), productStoreId, voucherAmount, toatlProductsTicketAmount, cartItem);
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (UtilValidate.isNotEmpty(storeTicketMap.get(productStoreId))) {
                            String tickets = storeTicketMap.get(productStoreId);
                            tickets += "," + tickets;
                            storeTicketMap.put(productStoreId, tickets);
                        } else {
                            storeTicketMap.put(productStoreId, ticketId1);
                        }
                    }
                    
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                
            }
        }
        
        //按照店铺拆单
        Map<String, List<ShoppingCartItem>> storeCarts = FastMap.newInstance();
        for (ShoppingCartItem cartItem : cart.items()) {
            if ("Y".equals(cartItem.getIsChoose())) {
                String productStoreId = cartItem.getProductStoreId();
                if (storeCarts.get(productStoreId) != null) {
                    List<ShoppingCartItem> cartItems = storeCarts.get(productStoreId);
                    cartItems.add(cartItem);
                } else {
                    List<ShoppingCartItem> cartItems = FastList.newInstance();
                    cartItems.add(cartItem);
                    storeCarts.put(productStoreId, cartItems);
                }
            }
        }
        List<String> errorMsgs = FastList.newInstance();
        for (String storeId : storeCarts.keySet()) {
            //根据店铺循环创建订单
            List<String> singleUsePayments = FastList.newInstance();
            singleUsePayments.add(payMethod);
            Map<String, Map<String, Object>> selectedPaymentMethods = FastMap.newInstance();
            selectedPaymentMethods.put("EXT_PING", FastMap.newInstance());
            String result = CheckOutEvents.setCheckOutOptions(storeId, cart, delegator, dispatcher, request, shipmentMethod, shippingContactMechId, selectedPaymentMethods);
            if ("error".equals(result)) {
                resultData.put("retCode", "0");
                resultData.put("message", "订单创建失败");
                success = false;
            }
            if (success) {
                result = ShippingEvents.getShipEstimate(storeId, cart, dispatcher, delegator);
                if ("error".equals(result)) {
                    resultData.put("retCode", "0");
                    resultData.put("message", "订单创建失败");
                    success = false;
                }
                if (success) {
                    result = CheckOutEvents.checkPaymentMethods(cart, dispatcher, delegator);
                    if ("error".equals(result)) {
                        resultData.put("retCode", "0");
                        resultData.put("message", "订单创建失败");
                        success = false;
                    }
                    if (success) {
                        cart.setNeedInvoice(needInvoice);
                        cart.setInvoiceTitle(invoiceTitle);
                        cart.setInvoiceContentTypeId(invoiceContentTypeId);
                        cart.setInvoiceType(invoiceType);
                        cart.setTaxNo(taxNo);
                        cart.setPartyCardNo(partyCardNo);
                        //crm
                        cart.setMember_id(member_id);
                        cart.setMall_id(mall_id);
                        cart.setChannelId(channelId);
                        //优惠劵
//                        cart.setCouponCode(couponCode);
                        //按照店铺来区分
                        if (UtilValidate.isNotEmpty(storeCouponMap)) {
                            if (UtilValidate.isNotEmpty(storeCouponMap.get(storeId))) {
                                cart.setProductPromoCodeId(storeCouponMap.get(storeId));
                            } else {
                                cart.setProductPromoCodeId(null);
                            }
                        } else {
                            cart.setProductPromoCodeId(null);
                        }
                        //按照店铺区分ticket
                        if (UtilValidate.isNotEmpty(storeTicketMap)) {
                            if (UtilValidate.isNotEmpty(storeTicketMap.get(storeId))) {
                                cart.setTicketId(storeTicketMap.get(storeId));
                            } else {
                                cart.setTicketId(null);
                            }
                        } else {
                            cart.setTicketId(null);
                        }
                        
                        cart.setActivityId(activityId);
                        cart.setWebSiteId(webSiteId);
                        
                        cart.setToken(token);
                        cart.setOrderType(orderType);
                     
                        //处理供应商拆单
                        GenericValue productStore = null;
                        Map<String,Boolean> dealProvider = FastMap.newInstance();
                        try {
                            productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", storeId));
                            String ownerPartyId = productStore.getString("ownerPartyId");
                            GenericValue partyGroup = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", ownerPartyId));
                            String isInner = partyGroup.getString("isInner");
                            if ("Y".equals(isInner)) {
                                for(ShoppingCartItem orderItem: cart.items()){
                                    if(orderItem.getProductStoreId().equals(storeId) && "Y".equals(orderItem.getIsChoose())) {
                                        String productId = orderItem.getProductId();
                                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                                        String providerId = product.getString("providerId") == null ? "isInner" : product.getString("providerId");
                                        if (UtilValidate.isNotEmpty(providerId)) {
                                            if (UtilValidate.isEmpty(dealProvider.get(providerId))) {
                                                String orderId = CheckOutEvents.createOrder(storeId, cart, dispatcher, delegator, loginName, request, providerId);
                                                dealProvider.put(providerId, true);
                                                if (orderId.equals("")) {
                                                    resultData.put("retCode", "0");
                                                    errorMsgs.add((String) request.getAttribute("_ERROR_MESSAGE_"));
                                                    success = false;
                                                } else {
                                                    orders.add(orderId);
                                                }
                                            }
        
                                        }
                                    }
                                }
                            }else{
                                String orderId = CheckOutEvents.createOrder(storeId, cart, dispatcher, delegator, loginName, request);
                                if (orderId.equals("")) {
                                    resultData.put("retCode", "0");
                                    errorMsgs.add((String) request.getAttribute("_ERROR_MESSAGE_"));
                                    success = false;
                                } else {
                                    orders.add(orderId);
                                }
                            }
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        if (success && UtilValidate.isNotEmpty(orders)) {
            //优惠劵设置已经使用？虚拟商品代金劵的状态？
            if (UtilValidate.isNotEmpty(productPromoCodeId)) {
                String[] codeIds = productPromoCodeId.split(",");
                for (int i = 0; i < codeIds.length; i++) {
                    String codeId = codeIds[i];
                    GenericValue productPromoCode = null;
                    try {
                        productPromoCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId", codeId));
                        productPromoCode.set("promoCodeStatus", "U");
                        delegator.store(productPromoCode);
                        GenericValue productPromoCodeParty  = delegator.findByPrimaryKey("ProductPromoCodeParty", UtilMisc.toMap("productPromoCodeId", codeId,"partyId",cart.getPartyId()));
                        
                        productPromoCodeParty.set("useDate", UtilDateTime.nowTimestamp());
                        delegator.store(productPromoCodeParty);
    
                        GenericValue productPromoCoupon = productPromoCode.getRelatedOne("ProductPromoCoupon");
//                        System.out.println("productPromoCoupon = " + productPromoCoupon);
                        //优惠劵Code标注已使用、优惠劵更新order数
                        Long orderCount = productPromoCoupon.getLong("orderCount") == null ? 0L : productPromoCoupon.getLong("orderCount");
                        productPromoCoupon.set("orderCount", orderCount + 1);
                        delegator.store(productPromoCoupon);
    
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
            if (UtilValidate.isNotEmpty(ticketId)) {
                String[] ticketIds = ticketId.split(",");
                for (int i = 0; i < ticketIds.length; i++) {
                    String tId = ticketIds[i];
                    GenericValue ticket = null;
                    try {
                        ticket = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId", tId));
                        ticket.set("ticketStatus", "hasUsed");
                        ticket.set("useDate", UtilDateTime.nowTimestamp());
                        delegator.store(ticket);
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
            if (UtilValidate.isNotEmpty(useIntegral) && new BigDecimal(useIntegral).compareTo(BigDecimal.ZERO) > 0) {
                try {
                    if (UtilValidate.isEmpty(token)) {
                        token = "";
                    }
                    Map<String, Object> scoreRes = dispatcher.runSync("kaide-consumeIntegral", UtilMisc.toMap("member_id", member_id, "mall_id", mall_id, "integral", useIntegral.toString(), "description", "用户通过星积分下单", "merchant_id", mall_id, "token", token));
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
                        
                        GenericValue partyAccountDetail = delegator.makeValue("PartyAccountDetail");
                        partyAccountDetail.set("detailId", delegator.getNextSeqId("PartyAccountDetail"));
                        partyAccountDetail.set("partyId", cart.getPartyId());
                        partyAccountDetail.set("amount", new BigDecimal(useIntegral));
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        partyAccountDetail.set("createDate", Timestamp.valueOf(sdf.format(date)));
                        partyAccountDetail.set("description", "订单支付，订单号：" + orders);
                        partyAccountDetail.set("operator", cart.getPartyId());
                    
                    }
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                    Debug.log("yabiz商城积分变更服务调用错误！");
                    return ServiceUtil.returnError("yabiz商城积分变更服务调用错误");
                }
            }
            
            
            GenericValue orderGroup = delegator.makeValue("OrderGroup");
            orderGroup.setNextSeqId();
            orderGroup.put("groupName", cart.getOrderName());
            orderGroup.put("orderGroupType", orderGroupType);
            orderGroup.put("orderGroupBusId", orderGroupBusId);
            try {
                orderGroup.create();
                if (UtilValidate.isNotEmpty(orders)) {
                    for (String orderId : orders) {
                        GenericValue orderRel = delegator.makeValue("OrderGroupOrderRel");
                        orderRel.put("orderId", orderId);
                        orderRel.put("orderGroupId", orderGroup.get("orderGroupId"));
                        orderRel.create();
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
                resultData.put("retCode", "0");
                resultData.put("message", "订单创建失败");
                success = false;
            }
            if (success) {
                resultData.put("orderGroupId", orderGroup.get("orderGroupId"));
                resultData.put("orders", orders);
                resultData.put("retCode", "1");
                resultData.put("message", "订单创建成功");
            }
        } else {
            String errorMsgsStr = "";
            resultData.put("retCode", "0");
            if (UtilValidate.isNotEmpty(errorMsgs)) {
                for (int i = 0; i < errorMsgs.size(); i++) {
                    String error = errorMsgs.get(i);
                    if (i == errorMsgs.size() - 1) {
                        errorMsgsStr += error;
                    } else {
                        errorMsgsStr += error + ",";
                    }
                }
            }
            resultData.put("message", errorMsgsStr);
        }
//        cart.clear();
        Debug.logInfo("创建订单结果:"+resultData,module);
        return resultData;
    }
    
    private void createCouponOrderAdjustment(Delegator delegator, ShoppingCart cart, String productPromoCodeId, GenericValue couponVal,  BigDecimal payReduce, BigDecimal toatlStoreCouponAmount, ShoppingCartItem cartItem) {
        if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo()))) {
            BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
            BigDecimal couponItemPrice = itemAmount.divide(toatlStoreCouponAmount, 12, RoundingMode.HALF_UP).multiply(payReduce).setScale(2, RoundingMode.HALF_UP);
            //增加订单项
            GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
            orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
            orderAdjustment.set("sourceReferenceId", productPromoCodeId);
            if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                orderAdjustment.set("orderItemSeqId", cartItem.getOrderItemSeqId());
            }
            if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                orderAdjustment.set("shipGroupSeqId", cartItem.getOrderItemSeqId());
            }
            orderAdjustment.set("description", "优惠劵抵扣订单金额");
            orderAdjustment.set("orderAdjustmentTypeId", "COUPON_ADJUESTMENT");
            orderAdjustment.set("amount", couponItemPrice.multiply(new BigDecimal(-1)));
            //优惠劵名称
            orderAdjustment.set("comments", couponVal.get("couponName"));
            orderAdjustment.set("correspondingProductId", cartItem.getProductId());
            orderAdjustment.set("productStoreId", cartItem.getProductStoreId());
            cartItem.addAdjustment(orderAdjustment);
            
            BigDecimal totalRealAmount = cartItem.getRecurringBasePrice() == null ? BigDecimal.ZERO : cartItem.getRecurringBasePrice();
            cartItem.setRecurringBasePrice(totalRealAmount.add(couponItemPrice));
        }
    }
    
    private void createCouponOrderAdjustment(Delegator delegator, ShoppingCart cart, String productPromoCodeId, GenericValue couponVal, String productStoreId, BigDecimal payReduce, BigDecimal toatlStoreCouponAmount, ShoppingCartItem cartItem) {
        if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo())) && cartItem.getProductStoreId().equals(productStoreId)) {
            BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
            BigDecimal couponItemPrice = itemAmount.divide(toatlStoreCouponAmount, 12, RoundingMode.HALF_UP).multiply(payReduce).setScale(2, RoundingMode.HALF_UP);
            //增加订单项
            GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
            orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
            orderAdjustment.set("sourceReferenceId", productPromoCodeId);
            if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                orderAdjustment.set("orderItemSeqId", cartItem.getOrderItemSeqId());
            }
            if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                orderAdjustment.set("shipGroupSeqId", cartItem.getOrderItemSeqId());
            }
            orderAdjustment.set("description", "优惠劵抵扣订单金额");
            orderAdjustment.set("orderAdjustmentTypeId", "COUPON_ADJUESTMENT");
            orderAdjustment.set("amount", couponItemPrice.multiply(new BigDecimal(-1)));
            //优惠劵名称
            orderAdjustment.set("comments", couponVal.get("couponName"));
            orderAdjustment.set("correspondingProductId", cartItem.getProductId());
            orderAdjustment.set("productStoreId", cartItem.getProductStoreId());
            cartItem.addAdjustment(orderAdjustment);
//            System.out.println("createCouponOrderAdjustment for product store = " + orderAdjustment);
            BigDecimal totalRealAmount = cartItem.getRecurringBasePrice() == null ? BigDecimal.ZERO : cartItem.getRecurringBasePrice();
            cartItem.setRecurringBasePrice(totalRealAmount.add(couponItemPrice));
        }
    }
    
    private void createTicketOrderAdjustment(Delegator delegator, ShoppingCart cart, GenericValue ticket, BigDecimal voucherAmount, BigDecimal toatlTicketnAmount, ShoppingCartItem cartItem) {
        if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo()))) {
            BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
            BigDecimal ticketItemPrice = BigDecimal.ZERO;
            //如果代金劵面值大于应用到商品总价之和，则取商品的总价分摊
            if(voucherAmount.compareTo(toatlTicketnAmount)>0){
                  ticketItemPrice = itemAmount.divide(toatlTicketnAmount, 12, RoundingMode.HALF_UP).multiply(toatlTicketnAmount).setScale(2, RoundingMode.HALF_UP);
            }else{
                  ticketItemPrice = itemAmount.divide(toatlTicketnAmount, 12, RoundingMode.HALF_UP).multiply(voucherAmount).setScale(2, RoundingMode.HALF_UP);
            }
           
            GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
            
            orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
            orderAdjustment.set("sourceReferenceId", ticket.getString("ticketId"));
            if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                orderAdjustment.set("orderItemSeqId", cartItem.getOrderItemSeqId());
            }
            if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                orderAdjustment.set("shipGroupSeqId", cartItem.getOrderItemSeqId());
            }
            orderAdjustment.set("description", "代金劵抵扣订单金额");
            orderAdjustment.set("orderAdjustmentTypeId", "TICKET_ADJUESTMENT");
            orderAdjustment.set("comments", ticket.getString("ticketName"));
            orderAdjustment.set("amount", ticketItemPrice.multiply(new BigDecimal(-1)));
            orderAdjustment.set("correspondingProductId", cartItem.getProductId());
            orderAdjustment.set("productStoreId", cartItem.getProductStoreId());
            cartItem.addAdjustment(orderAdjustment);
            BigDecimal totalRealAmount = cartItem.getRecurringBasePrice() == null ? BigDecimal.ZERO : cartItem.getRecurringBasePrice();
            cartItem.setRecurringBasePrice(totalRealAmount.add(ticketItemPrice));
        }
    }
    private void createTicketOrderAdjustment(Delegator delegator, ShoppingCart cart, GenericValue ticket, String productStoreId, BigDecimal voucherAmount, BigDecimal toatlTicketnAmount, ShoppingCartItem cartItem) {
        if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!(cartItem.getIsPromo())) && cartItem.getProductStoreId().equals(productStoreId)) {
            BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
            BigDecimal ticketItemPrice = itemAmount.divide(toatlTicketnAmount, 12, RoundingMode.HALF_UP).multiply(voucherAmount).setScale(2, RoundingMode.HALF_UP);
            GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
            
            orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
            orderAdjustment.set("sourceReferenceId", ticket.getString("ticketId"));
            if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                orderAdjustment.set("orderItemSeqId", cartItem.getOrderItemSeqId());
            }
            if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                orderAdjustment.set("shipGroupSeqId", cartItem.getOrderItemSeqId());
            }
            orderAdjustment.set("description", "代金劵抵扣订单金额");
            orderAdjustment.set("orderAdjustmentTypeId", "TICKET_ADJUESTMENT");
            orderAdjustment.set("comments", ticket.getString("ticketName"));
            orderAdjustment.set("amount", ticketItemPrice.multiply(new BigDecimal(-1)));
            orderAdjustment.set("correspondingProductId", cartItem.getProductId());
            orderAdjustment.set("productStoreId", cartItem.getProductStoreId());
            cartItem.addAdjustment(orderAdjustment);
            
            
            BigDecimal totalRealAmount = cartItem.getRecurringBasePrice() == null ? BigDecimal.ZERO : cartItem.getRecurringBasePrice();
            cartItem.setRecurringBasePrice(totalRealAmount.add(ticketItemPrice));
        }
    }
    
    private BigDecimal getAllCartTotalAmountUnPromo(ShoppingCart cart) {
        BigDecimal allCartTotalAmount = BigDecimal.ZERO;
        for (ShoppingCartItem cartItem : cart.items()) {
            if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!cartItem.getIsPromo())) {
                allCartTotalAmount = allCartTotalAmount.add(cartItem.getBasePrice().multiply(cartItem.getQuantity()));
            }
        }
        return allCartTotalAmount;
    }
    
    private Map<String, BigDecimal> getAllCartTotalAmountUnPromoByStoreId(ShoppingCart cart) {
        Map<String, BigDecimal> totalAmountByStore = FastMap.newInstance();
        
        for (ShoppingCartItem cartItem : cart.items()) {
            if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose()) && (!cartItem.getIsPromo())) {
                String productStoreId = cartItem.getProductStoreId();
                if (UtilValidate.isNotEmpty(totalAmountByStore.get(productStoreId))) {
                    BigDecimal itemAmount = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                    totalAmountByStore.put(productStoreId, itemAmount.add(totalAmountByStore.get(productStoreId)));
                } else {
                    totalAmountByStore.put(productStoreId, cartItem.getBasePrice().multiply(cartItem.getQuantity()));
                }
                
            }
        }
        return totalAmountByStore;
    }
    
    private void scoreAssignForCart(Delegator delegator, ShoppingCart cart, String useIntegral, String integralDiscount, Map<String, BigDecimal> useIntegralStoreMap, Map<String, BigDecimal> integralDiscountStoreMap) {
        if (UtilValidate.isNotEmpty(useIntegral) && UtilValidate.isNotEmpty(integralDiscount)) {
            // 做订单积分扣减拆分
            //积分兑奖比例
            GenericValue integralPerMoney = null;//积分抵现规则表
            try {
                integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            /*1.不可使用积分2:百分比抵扣3:固定金额抵扣*/
            Long integralValue = integralPerMoney.getLong("integralValue");
            BigDecimal totalScoreUpper = BigDecimal.ZERO;
            for (ShoppingCartItem cartItem : cart.items()) {
                if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose())) {
                    GenericValue product = cartItem.getProduct();
                    String integralDeductionType = product.getString("integralDeductionType");
                    BigDecimal integralDeductionUpper = product.getBigDecimal("integralDeductionUpper");
                    if (integralDeductionType.equalsIgnoreCase("2")) {
                        BigDecimal scorePrice = cartItem.getBasePrice().multiply(integralDeductionUpper).divide(new BigDecimal(100));
                        BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice).multiply(cartItem.getQuantity());
                        totalScoreUpper = totalScoreUpper.add(score);
                    } else if (integralDeductionType.equalsIgnoreCase("3")) {
                        BigDecimal score = integralDeductionUpper.multiply(new BigDecimal(integralValue)).multiply(cartItem.getQuantity());//需要的积分
                        totalScoreUpper = totalScoreUpper.add(score);
                    }
                }
            }
            //计算每个产品的积分扣减orderAdjustment
            for (ShoppingCartItem cartItem : cart.items()) {
                if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose())) {
                    GenericValue product = cartItem.getProduct();
                    String integralDeductionType = product.getString("integralDeductionType");
                    BigDecimal integralDeductionUpper = product.getBigDecimal("integralDeductionUpper");
                    BigDecimal cartItemScore = BigDecimal.ZERO;
                    if ("2".equalsIgnoreCase(integralDeductionType)) {
                        BigDecimal scorePrice = cartItem.getBasePrice().multiply(integralDeductionUpper).divide(new BigDecimal(100));
                        BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice).multiply(cartItem.getQuantity());
                        //计算出订单项的扣减积分
                        cartItemScore = score.divide(totalScoreUpper, 12, RoundingMode.HALF_UP).multiply(new BigDecimal(useIntegral));
                        
                    } else if ("3".equalsIgnoreCase(integralDeductionType)) {
                        BigDecimal scorePrice = integralDeductionUpper.setScale(2, BigDecimal.ROUND_HALF_UP);//积分抵扣的金额
                        BigDecimal score = integralDeductionUpper.multiply(new BigDecimal(integralValue)).multiply(cartItem.getQuantity());//需要的积分
                        //计算出订单项的扣减积分
                        cartItemScore = score.divide(totalScoreUpper, 12, RoundingMode.HALF_UP).multiply(new BigDecimal(useIntegral));
                        
                    }
                    if (cartItemScore.compareTo(BigDecimal.ZERO) > 0) {
                        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment");
                        orderAdjustment.set("createdDate", UtilDateTime.nowTimestamp());
                        if (UtilValidate.isEmpty(orderAdjustment.get("orderItemSeqId"))) {
                            orderAdjustment.set("orderItemSeqId", cartItem.getOrderItemSeqId());
                        }
                        if (UtilValidate.isEmpty(orderAdjustment.get("shipGroupSeqId"))) {
                            orderAdjustment.set("shipGroupSeqId", cartItem.getOrderItemSeqId());
                        }
                        orderAdjustment.set("description", "星积分抵扣订单金额");
                        orderAdjustment.set("orderAdjustmentTypeId", "INTEGRAL_ADJUESTMENT");
                        orderAdjustment.set("correspondingProductId", cartItem.getProductId());
                        BigDecimal recurringBasePrice = cartItemScore.divide(new BigDecimal(integralValue), 2, BigDecimal.ROUND_HALF_UP);
                        orderAdjustment.set("amount", recurringBasePrice.multiply(new BigDecimal(-1)));
                        //增加积分兑换金额
                        orderAdjustment.set("recurringAmount", cartItemScore);
                        orderAdjustment.set("productStoreId", cartItem.getProductStoreId());
                        cartItem.addAdjustment(orderAdjustment);
                        //订单项扣减增加积分扣减
                        BigDecimal totalRealAmount = cartItem.getRecurringBasePrice() == null ? BigDecimal.ZERO : cartItem.getRecurringBasePrice();
                        cartItem.setRecurringBasePrice(totalRealAmount.add(recurringBasePrice));
//                        System.out.println("get cartItem recurringBasePrice1= "+cartItem.getRecurringBasePrice());
                        String productStoreId = cartItem.getProductStoreId();
                        if (UtilValidate.isEmpty(useIntegralStoreMap.get(productStoreId))) {
                            useIntegralStoreMap.put(productStoreId, cartItemScore);
                        } else {
                            useIntegralStoreMap.put(productStoreId, useIntegralStoreMap.get(productStoreId).add(cartItemScore));
                        }
                        
                        if (UtilValidate.isEmpty(integralDiscountStoreMap.get(productStoreId))) {
                            integralDiscountStoreMap.put(productStoreId, recurringBasePrice);
                        } else {
                            integralDiscountStoreMap.put(productStoreId, integralDiscountStoreMap.get(productStoreId).add(recurringBasePrice));
                        }
                        
                    }
                }
            }
            
        }
    }

    private Map<String, Object> createGroupOrSeckillOrderBase(HttpServletRequest request, String loginName, Delegator delegator, Map<String, Object> resultData, LocalDispatcher dispatcher, boolean success, List<String> orders, String orderGroupType, String orderGroupBusId, String orderType) {
        //获取登录用户信息
        //渠道ID
        String channelId = request.getParameter("channelId");
        //发票信息
        String needInvoice = request.getParameter("needInvoice");
        String invoiceType = request.getParameter("invoiceType");
        String taxNo = request.getParameter("taxNo");
        String invoiceTitle = request.getParameter("invoiceTitle");
        String invoiceContentTypeId = request.getParameter("invoiceContentTypeId");
        //用户的Crm信息
        String member_id = request.getParameter("member_id");
        String mall_id = request.getParameter("mall_id");
        //用户的省份证信息
        String partyCardNo = request.getParameter("partyNo");
        //优惠劵
        String couponCode = request.getParameter("couponCode");
        String productPromoCodeId = request.getParameter("productPromoCodeId");
        //积分
        String useIntegral = request.getParameter("useIntegral");
        String integralDiscount = request.getParameter("integralDiscount");
        String activityId = request.getParameter("activityId");
        
        //配送方式 String shippingMethod = typeId+"@"+shipPartyId;
        String shipmentMethod = request.getParameter("shipmentMethod");
        //支付方式 EXT_PING
        String payMethod = request.getParameter("payMethod");
        String webSiteId = request.getHeader("client");
        //收货地址
        String token = request.getParameter("token");
        //虚拟商品购买ticketId
        String ticketId = request.getParameter("ticketId");
        String shippingContactMechId = request.getParameter("shippingContactMechId");
        
        String productId = request.getParameter("productId");
        GenericValue userLogin = null;
        String productStoreId = null;
        GenericValue product = null;
        BigDecimal defaultPrice = BigDecimal.ZERO;
        BigDecimal listPirce = BigDecimal.ZERO;
        try {
            userLogin = delegator.findByPrimaryKeyCache("UserLogin", UtilMisc.toMap("userLoginId", loginName));
//            productStoreId = EntityUtil.getFirst(delegator.findByAnd("ProductStoreProduct", UtilMisc.toMap("productId", productId))).getString("productStoreId");
    
            productStoreId = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId)).getString("productStoreId");
            
            List<GenericValue> productPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId));
            //
            for (int i = 0; i < productPrices.size(); i++) {
                GenericValue productPrice = productPrices.get(i);
                if ("DEFAULT_PRICE".equals(productPrice.getString("productPriceTypeId"))) {
                    defaultPrice = productPrice.getBigDecimal("price");
                } else if ("MARKET_PRICE".equals(productPrice.getString("productPriceTypeId"))) {
                    listPirce = productPrice.getBigDecimal("price");
                }
            }
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        BigDecimal activityPrice = getActivityPrice(delegator, productId, activityId, productStoreId);
        
        String itemName = product.getString("productName");
        BigDecimal unitPrice = BigDecimal.ZERO;

        String partyId = userLogin.getString("partyId");
        String[] shipmentMethods = shipmentMethod.split("@");
        String shipmentMethodTypeId = shipmentMethods[0];
        String carrierPartyId = shipmentMethods[1];
        
        //计算运费
        Map<String, Object> context = FastMap.newInstance();
        BigDecimal shipEstimate = ShippingEvents.getShipGroupEstimateByProduct(delegator, product, BigDecimal.ONE, shippingContactMechId, productStoreId);
        
        if (shipEstimate.compareTo(BigDecimal.ZERO) != 0) {
            GenericValue shipAdj = delegator.makeValue("OrderAdjustment");
            shipAdj.set("orderAdjustmentTypeId", "SHIPPING_CHARGES");
            shipAdj.set("amount", shipEstimate);
            shipAdj.set("shipGroupSeqId", "00001");
            List<GenericValue> values = FastList.newInstance();
            values.add(shipAdj);
            context.put("orderAdjustments", values);
        }

        context.put("partyId", partyId);
        context.put("productStoreId", productStoreId);
        context.put("saleOrderTypeId", orderType);
        context.put("webSiteId", webSiteId);
        context.put("orderTypeId", "SALES_ORDER");
        context.put("orderItems", makeOrderItems(delegator, productId, activityId, unitPrice, listPirce, itemName, loginName, activityPrice));
        context.put("orderItemShipGroupInfo", makeItemShipGroupAndAssoc(delegator, shippingContactMechId, shipmentMethodTypeId, carrierPartyId, "CARRIER"));
        
        context.put("orderPaymentInfo", makeOrderPaymentInfo(delegator, payMethod, ""));
        context.put("needInvoice", needInvoice);
        context.put("invoiceType", invoiceType);
        context.put("invoiceTitle", invoiceTitle);
        context.put("invoiceContentTypeId", invoiceContentTypeId);
        context.put("taxNo", taxNo);
        context.put("partyNo", partyCardNo);
        context.put("currencyUom", "CNY");
        
        context.put("orderContactMechs", makeOrderContactMechs(delegator, shippingContactMechId));
        context.put("grandTotal", activityPrice);
        context.put("orderDate", UtilDateTime.nowTimestamp());
        context.put("useIntegral", useIntegral);
        context.put("integralDiscount", integralDiscount);
        context.put("couponCode", couponCode);
        context.put("productPromoCodeId", productPromoCodeId);
        context.put("ticketId", ticketId);
        context.put("member_id", member_id);
        context.put("mall_id", mall_id);
        context.put("channelId", channelId);
        context.put("cartType", orderType);
        context.put("activityId", activityId);
        context.put("token", token);
        context.put("userLogin", userLogin);
        
        Map<String, Object> storeResult = null;
        try {
            storeResult = dispatcher.runSync("storeOrder", context);
            String orderId = (String) storeResult.get("orderId");
            orders.add(orderId);
        } catch (GenericServiceException e) {
            String service = e.getMessage();
            
            return ServiceUtil.returnError(service);
        }
        
        List<String> errorMsgs = FastList.newInstance();
        
        if (success && UtilValidate.isNotEmpty(orders)) {
            GenericValue orderGroup = delegator.makeValue("OrderGroup");
            orderGroup.setNextSeqId();
            orderGroup.put("groupName", product.getString("productName"));
            orderGroup.put("orderGroupType", orderGroupType);
            orderGroup.put("orderGroupBusId", orderGroupBusId);
            try {
                orderGroup.create();
                if (UtilValidate.isNotEmpty(orders)) {
                    for (String orderId : orders) {
                        GenericValue orderRel = delegator.makeValue("OrderGroupOrderRel");
                        orderRel.put("orderId", orderId);
                        orderRel.put("orderGroupId", orderGroup.get("orderGroupId"));
                        orderRel.create();
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
                resultData.put("retCode", "0");
                resultData.put("message", "订单创建失败");
                success = false;
            }
            if (success) {
                resultData.put("orderGroupId", orderGroup.get("orderGroupId"));
                resultData.put("orders", orders);
                resultData.put("retCode", "1");
                resultData.put("message", "订单创建成功");
            }
        } else {
            String errorMsgsStr = "";
            resultData.put("retCode", "0");
            if (UtilValidate.isNotEmpty(errorMsgs)) {
                for (int i = 0; i < errorMsgs.size(); i++) {
                    String error = errorMsgs.get(i);
                    if (i == errorMsgs.size() - 1) {
                        errorMsgsStr += error;
                    } else {
                        errorMsgsStr += error + ",";
                    }
                }
            }
            resultData.put("message", errorMsgsStr);
        }
//        cart.clear();
        return resultData;
    }

    private BigDecimal getActivityPrice(Delegator delegator, String productId, String activityId, String productStoreId) {
        List<GenericValue> pactivityInfo = null;
        try {
            pactivityInfo = delegator.findByAnd("ProductActivityInfo", UtilMisc.toMap("activityAuditStatus", "ACTY_AUDIT_PASS", "productStoreId", productStoreId, "productId", productId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        pactivityInfo = EntityUtil.filterByDate(pactivityInfo, UtilDateTime.nowTimestamp(), "activityStartDate", "activityEndDate", true);
        if (UtilValidate.isNotEmpty(pactivityInfo)) {
            for (int t = 0; t < pactivityInfo.size(); t++) {
                GenericValue pactivity = pactivityInfo.get(t);
                if (pactivity.getString("productId").equals(productId)) {
                    
                    return pactivity.getBigDecimal("activityPrice");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 创建团购订单
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/createGroupOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGroupOrder(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        //获取登录用户信息
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //获取活动编号
        String activityId = request.getParameter("activityId");
        //拼团编号
        String togetherId = request.getParameter("togetherId");
        String productId = request.getParameter("productId");
        
        Map<String, Object> resultData = FastMap.newInstance();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        boolean success = true;
        List<String> orders = FastList.newInstance();
        //获取ShoppingCart
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        
        String orderGroupType = "ORDER_GROUP";
        String orderGroupBusId = "";
        BigDecimal grandTotal = BigDecimal.ZERO;
        String productName = "";
        Map<String, Object> result = null;
        try {
            if (UtilValidate.isNotEmpty(togetherId)) {
                
                GenericValue createForOrder = delegator.findByPrimaryKey("TogetherGroup", UtilMisc.toMap("togetherId", togetherId));
                BigDecimal currentNum = new BigDecimal(createForOrder.getString("currentNum"));
                BigDecimal limitUserNum = new BigDecimal(createForOrder.getString("limitUserNum"));
                int personNum = currentNum.compareTo(limitUserNum);
                if (personNum == 0) {
                    resultData.put("retCode", 0);
                    resultData.put("message", "该团人数已满！");
                    
                } else if (personNum < 0) {
                    result = createOrderBase(request, loginName, delegator, resultData, dispatcher, success, orders, cart, orderGroupType, orderGroupBusId, "togetherCart");
                }
            }
            
            if (UtilValidate.isEmpty(togetherId)) {
                result = createOrderBase(request, loginName, delegator, resultData, dispatcher, success, orders, cart, orderGroupType, orderGroupBusId, "togetherCart");
            }
            
        } catch (GeneralException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        if (UtilValidate.isNotEmpty(result)) {
            if (!"1".equals(result.get("retCode"))) {
                success = false;
            }
            
            if (success && UtilValidate.isNotEmpty(orders)) {
                String orderId = orders.get(0);
                 //如果togetherId有值 ---》参与者
                if (UtilValidate.isNotEmpty(togetherId)) {
                    
                    try {
                        List<GenericValue> toGroupOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));
                        GenericValue newUserLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                        String partyId = "";
                        if (UtilValidate.isNotEmpty(newUserLogin)) {
                            partyId = newUserLogin.getString("partyId");
                        }
                        if (UtilValidate.isNotEmpty(toGroupOrders)) {
                            for (int t = 0; t < toGroupOrders.size(); t++) {
                                String toOrderUserId = toGroupOrders.get(t).getString("orderUserId");
                                if (toOrderUserId.equals(partyId)) {
                                    resultData.put("retCode", 0);
                                    resultData.put("message", "您已经参加过此次团购");
                                    resultData.put("isInGroup", "Y");
                                } else {
                                    //参团
                                    GenericValue togetherGroup = delegator.findByPrimaryKey("TogetherGroup", UtilMisc.toMap("togetherId", togetherId));
                                    //创建拼团的用户id
                                    String createUserId = togetherGroup.getString("createUserId");
                                    BigDecimal currentNum = new BigDecimal(togetherGroup.getString("currentNum"));
                                    BigDecimal limitUserNum = new BigDecimal(togetherGroup.getString("limitUserNum"));
                                    int groupPersonNum = Integer.parseInt(togetherGroup.getString("limitUserNum"));
                                    Timestamp createDate = togetherGroup.getTimestamp("createDate");
                                    //personNum -1小于 0 等于
                                    int personNum = currentNum.compareTo(limitUserNum);
                                    String status = "TOGETHER_NOTPAY";//拼团订单未支付
                                    if (personNum < 0) {
                                        GenericValue groupRelOrder = delegator.makeValidValue("TogetherGroupRelOrder");
                                        groupRelOrder.set("togetherId", togetherId);
                                        groupRelOrder.set("orderId", orderId);
                                        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                                        if (UtilValidate.isNotEmpty(userLogin)) {
                                            groupRelOrder.set("orderUserId", userLogin.getString("partyId"));
                                            GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.getString("partyId")));
                                            if (UtilValidate.isNotEmpty(person)) {
                                                String headImgUrl = person.getString("headphoto");
                                                groupRelOrder.set("headImgUrl", headImgUrl);
                                            }
                                        }
                                        groupRelOrder.set("createDate", createDate);
                                        groupRelOrder.create();
                                        GenericValue togroup = delegator.makeValidValue("TogetherGroup");
                                        togroup.set("togetherId", togetherId);
                                        togroup.set("createUserId", createUserId);
                                        if (UtilValidate.isNotEmpty(productId)) {
                                            togroup.set("productId", productId);
                                        }
                                        togroup.set("createDate", createDate);
                                        String num = togetherGroup.getString("currentNum");
                                        int current = Integer.parseInt(num);
                                        current = current + 1;
                                        BigDecimal personTo = new BigDecimal(current);
                                        int compartNum = personTo.compareTo(limitUserNum);
                                        Long limitNum = togetherGroup.getLong("limitUserNum");
                                        togroup.set("limitUserNum", limitNum);
                                        if (UtilValidate.isNotEmpty(activityId)) {
                                            togroup.set("activityId", activityId);
                                        }
                                        togroup.set("currentNum", new Long(current));
                                        togetherGroup.set("status", status);
                                        togroup.store();
                                        
                                    } else if (personNum == 0) {
                                        togetherGroup.set("togetherId", togetherId);
                                        togetherGroup.set("createUserId", createUserId);
                                        togetherGroup.set("status", status);
                                        Long current = togetherGroup.getLong("currentNum");
                                        togetherGroup.set("currentNum", current);
                                        togetherGroup.set("activityId", activityId);
                                        togetherGroup.set("productId", productId);
                                        togetherGroup.set("createDate", createDate);
                                        Long limit = togetherGroup.getLong("limitUserNum");
                                        togetherGroup.set("limitUserNum", limit);
                                        togetherGroup.store();
                                        
                                        GenericValue groupRelOrder = delegator.makeValidValue("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId, "orderId", orderId));
                                        groupRelOrder.set("togetherId", togetherId);
                                        groupRelOrder.set("orderId", orderId);
                                        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                                        if (UtilValidate.isNotEmpty(userLogin)) {
                                            groupRelOrder.set("orderUserId", userLogin.getString("partyId"));
                                            GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.getString("partyId")));
                                            if (UtilValidate.isNotEmpty(person)) {
                                                String headImgUrl = person.getString("headphoto");
                                                groupRelOrder.set("headImgUrl", headImgUrl);
                                            }
                                        }
                                        groupRelOrder.set("createDate", createDate);
                                        groupRelOrder.store();
                                        
                                    }
                                    
                                }
                            }
                        }
                        
                    } catch (GeneralException e) {
                        e.printStackTrace();
                        resultData.put("retCode", 0);
                        resultData.put("message", e.getMessage());
                    }
                    
                } else {
                    //发起  创建
                    togetherId = delegator.getNextSeqId("TogetherGroup");
                    String status = "TOGETHER_NOTPAY";//拼团订单未支付
                    try {
                        
                        GenericValue togetherGroup = delegator.makeValidValue("TogetherGroup");
                        togetherGroup.set("togetherId", togetherId);
                        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                        if (UtilValidate.isNotEmpty(userLogin)) {
                            togetherGroup.set("createUserId", userLogin.getString("partyId"));
                        }
                        togetherGroup.set("status", status);
                        if (UtilValidate.isNotEmpty(productId)) {
                            togetherGroup.set("productId", productId);
                        }
                        if (UtilValidate.isNotEmpty(activityId)) {
                            togetherGroup.set("activityId", activityId);
                        }
                        
                        //当前人数
                        Integer currentNum = 1;
                        togetherGroup.set("currentNum", new Long(currentNum));
                        if (UtilValidate.isNotEmpty(activityId) && UtilValidate.isNotEmpty(productId)) {
                            GenericValue activityGood = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("activityId", activityId, "productId", productId));
                            String quantity = "0";
                            if (UtilValidate.isNotEmpty(activityGood)) {
                                quantity = activityGood.getString("activityQuantity");
                            }
                            int activityQuantity = Integer.parseInt(quantity);
                            //多少人成团
                            togetherGroup.set("limitUserNum", new Long(activityQuantity));
                            Timestamp createDate = UtilDateTime.nowTimestamp();
                            togetherGroup.set("createDate", createDate);
                            togetherGroup.create();
                        }
                        
                        GenericValue togetherGroupRelOrder = delegator.makeValidValue("TogetherGroupRelOrder");
                        togetherGroupRelOrder.set("togetherId", togetherId);
                        togetherGroupRelOrder.set("orderId", orderId);
                        togetherGroupRelOrder.set("orderUserId", userLogin.getString("partyId"));
                        Timestamp createDate = UtilDateTime.nowTimestamp();
                        togetherGroupRelOrder.set("createDate", createDate);
                        GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.getString("partyId")));
                        if (UtilValidate.isNotEmpty(person)) {
                            String headImgUrl = person.getString("headphoto");
                            if (UtilValidate.isNotEmpty(headImgUrl)) {
                                togetherGroupRelOrder.set("headImgUrl", headImgUrl);
                            }
                        }
                        togetherGroupRelOrder.create();

                    } catch (GeneralException e) {
                        e.printStackTrace();
                        resultData.put("retCode", 0);
                        resultData.put("message", e.getMessage());
                    }
                    
                }
                
                if (success) {
                    resultData.put("togetherId", togetherId);
                    resultData.put("isInGroup", "Y");
                    resultData.put("retCode", "1");
                    resultData.put("message", "订单创建成功");
                }
                
            }
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 创建礼品订单
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/createGiftOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGiftOrder(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException {
        Map<String, Object> resultData = FastMap.newInstance();
        
        String presentCardId = request.getParameter("presentCardId");
        if (UtilValidate.isEmpty(presentCardId)) {
            resultData.put("retCode", "0");
            resultData.put("message", "presentCardId卡片信息不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        boolean success = true;
        List<String> orders = FastList.newInstance();
        //获取ShoppingCart
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        
        String orderGroupType = "ORDER_GIFT";
        String orderGroupBusId = "";
        Map<String, Object> result = createOrderBase(request, loginName, delegator, resultData, dispatcher, success, orders, cart, orderGroupType, orderGroupBusId, "giftCart");
        
        if (!"1".equals(result.get("retCode"))) {
            success = false;
        }
        
        if (success && UtilValidate.isNotEmpty(orders)) {
            String partyId = CommonUtils.getPartyId(delegator, loginName);
            String orderGroupId = (String) result.get("orderGroupId");
            //礼品处理
            GenericValue partyOrderRelPresent = delegator.makeValue("PartyOrderRelPresent");
            String giftId = delegator.getNextSeqId("PartyOrderRelPresent");
            partyOrderRelPresent.put("giftId", giftId);
            partyOrderRelPresent.put("orderGroupId", orderGroupId);
            partyOrderRelPresent.put("sendPartyId", partyId);
            partyOrderRelPresent.put("presentCardId", presentCardId);
            //待支付
            partyOrderRelPresent.put("status", "GIFT_WAIT_PAY");
            partyOrderRelPresent.put("sendDate", new Timestamp(System.currentTimeMillis()));
            partyOrderRelPresent.create();
            
            resultData.put("retCode", "1");
            resultData.put("orderGroupId", orderGroupId);
            resultData.put("giftId", giftId);
            resultData.put("message", "订单创建成功");
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 创建心愿单订单
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/createWishOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createWishOrder(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException {
        Map<String, Object> resultData = FastMap.newInstance();
        String wishId = request.getParameter("wishId");
        if (UtilValidate.isEmpty(wishId)) {
            resultData.put("retCode", "0");
            resultData.put("message", "wishId心愿单id不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue orderWish = delegator.findByPrimaryKey("PartyOrderWish", UtilMisc.toMap("wishId", wishId));
        if (orderWish == null) {
            resultData.put("retCode", "0");
            resultData.put("message", "查找不到该心愿单");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        boolean success = true;
        List<String> orders = FastList.newInstance();
        //获取ShoppingCart
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        
        String orderGroupType = "ORDER_WISH";
        String orderGroupBusId = "";
        Map<String, Object> result = createOrderBase(request, loginName, delegator, resultData, dispatcher, success, orders, cart, orderGroupType, orderGroupBusId, "wishCart");
        
        if (!"1".equals(result.get("retCode"))) {
            success = false;
        }
        
        if (success && UtilValidate.isNotEmpty(orders)) {
            String partyId = CommonUtils.getPartyId(delegator, loginName);
            String orderGroupId = (String) result.get("orderGroupId");
            //礼品处理
            orderWish.put("orderGroupId", orderGroupId);
            orderWish.put("achievePartyId", partyId);
            orderWish.put("status", "WISH_WAIT_PAY");//代付款状态
            orderWish.store();
            resultData.put("retCode", "1");
            resultData.put("orderGroupId", orderGroupId);
            resultData.put("message", "订单创建成功");
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 创建秒杀订单
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/createSecKillOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createSecKillOrder(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        boolean success = true;
        List<String> orders = FastList.newInstance();
        //获取ShoppingCart
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        
        String orderGroupType = "ORDER_SECKILL";
        String orderGroupBusId = "";
        Map<String, Object> result = createOrderBase(request, loginName, delegator, resultData, dispatcher, success, orders, cart, orderGroupType, orderGroupBusId, "seckillCart");
//        Map<String, Object> result =  createGroupOrSeckillOrderBase(request, loginName, delegator, resultData, dispatcher, success, orders, orderGroupType, orderGroupBusId, "seckillCart");
        
        
        if (success && UtilValidate.isNotEmpty(orders)) {
            GenericValue orderGroup = delegator.makeValue("OrderGroup");
            orderGroup.setNextSeqId();
            orderGroup.put("groupName", cart.getOrderName());
            try {
                orderGroup.create();
                if (UtilValidate.isNotEmpty(orders)) {
                    for (String orderId : orders) {
                        GenericValue orderRel = delegator.makeValue("OrderGroupOrderRel");
                        orderRel.put("orderId", orderId);
                        orderRel.put("orderGroupId", orderGroup.get("orderGroupId"));
                        orderRel.create();
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
                resultData.put("retCode", "0");
                resultData.put("message", "订单创建失败");
                success = false;
            }
            if (success) {
                resultData.put("retCode", "1");
                resultData.put("message", "订单创建成功");
            }
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 待付款
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    @RequestMapping(value = "/waitingPay", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> waitingPay(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        //获取登录用户信息
        
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        
        List<String> fieldsToSelect = FastList.newInstance();
        
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            
            beganTransaction = TransactionUtil.begin();
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
            
            if (UtilValidate.isNotEmpty(userLogin)) {
                String partyId = userLogin.getString("partyId");
                
                /** 定义订单动态视图 */
                DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
                dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
                dynamicViewEntity.addAliasAll("OH", "", null);
                dynamicViewEntity.addAlias("OH", "orderId");
                dynamicViewEntity.addAlias("OH", "statusId");

                dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
                //会员ID
                dynamicViewEntity.addAlias("ORE", "partyId");
                //角色类型ID
                dynamicViewEntity.addAlias("ORE", "roleTypeId");
                dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
                
                fieldsToSelect.add("orderId");
                fieldsToSelect.add("partyId");
                fieldsToSelect.add("statusId");
                dynamicViewEntity.setGroupBy(fieldsToSelect);
                
                /** 定义查询条件集合 */
                EntityCondition mainCond = EntityCondition.makeCondition(
                        UtilMisc.toList(
                                /*orderRole*/
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
                                EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"),
                                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITPAY")
                        )
                        , EntityOperator.AND);
                
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);

                eli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
                
                Integer size = eli.getResultsSizeAfterPartialList();
                
                resultData.put("retCode", "1");
                resultData.put("message", "待付款数量查询成功");
                resultData.put("size", size);
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (eli != null) {
                try {
                    eli.close();
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
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 待收货
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    @RequestMapping(value = "/waitingShip", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> waitingShip(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        //获取登录用户信息
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        
        List<String> fieldsToSelect = FastList.newInstance();
        
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        
        try {
            beganTransaction = TransactionUtil.begin();
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
            
            if (UtilValidate.isNotEmpty(userLogin)) {
                String partyId = userLogin.getString("partyId");
                
                /** 定义订单动态视图 */
                DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
                dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
                dynamicViewEntity.addAliasAll("OH", "", null);
                dynamicViewEntity.addAlias("OH", "orderId");
                dynamicViewEntity.addAlias("OH", "statusId");
                /*add by gss*/
                dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
                //会员ID
                dynamicViewEntity.addAlias("ORE", "partyId");
                //角色类型ID
                dynamicViewEntity.addAlias("ORE", "roleTypeId");
                dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
                
                fieldsToSelect.add("orderId");
                fieldsToSelect.add("partyId");
                fieldsToSelect.add("statusId");
                dynamicViewEntity.setGroupBy(fieldsToSelect);
                List<String> statusList = FastList.newInstance();
                statusList.add("ORDER_HAVEPAY");
                statusList.add("ORDER_WAITSHIP");
                statusList.add("ORDER_WAITRECEIVE");
                
                /** 定义查询条件集合 */
                EntityCondition mainCond = EntityCondition.makeCondition(
                        UtilMisc.toList(
                                /*orderRole*/
                                EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
                                EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"),
                                EntityCondition.makeCondition("statusId", EntityOperator.IN, statusList)
                        )
                        , EntityOperator.AND);
                
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
                
                eli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
                
                Integer size = eli.getResultsSizeAfterPartialList();
                
                resultData.put("retCode", "1");
                resultData.put("message", "待收货数量查询成功");
                resultData.put("size", size);
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (eli != null) {
                try {
                    eli.close();
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

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    
    /**
     * 取消订单 删除订单
     * 根据orderId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/cancelOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> cancelOrder(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String orderId = request.getParameter("orderId");
        String operateReason = request.getParameter("operateReason");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
            resultData = dispatcher.runSync("cancelOrder", UtilMisc.toMap("orderId", orderId, "operateReason", operateReason, "userLogin", userLogin));
            resultData.put("retCode", 1);
            resultData.put("message", "操作成功");
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 订单取消原因
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/orderCancelReason", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> orderCancelReason(HttpServletRequest request) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> enum_list = null;
        try {
            enum_list = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "FIELD_CANCEL", "enumCode", "N"), UtilMisc.toList("sequenceId"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        List<Map> resultList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(enum_list)) {
            for (GenericValue enumeration : enum_list) {
                Map map = FastMap.newInstance();
                map.put("enumId", enumeration.get("enumId"));
                map.put("description", enumeration.get("description"));
                resultList.add(map);
            }
        }
        resultData.put("reasons", resultList);
        resultData.put("retCode", 1);
        resultData.put("message", "查询成功");
        return Optional.ofNullable(resultList).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 确认收货
     * 根据orderId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/confirmReceipt", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> changeOrderStatusValuate(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String orderId = request.getParameter("orderId");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            
            resultData = dispatcher.runSync("changeOrderStatusValuate", UtilMisc.toMap("orderId", orderId, "userLoginId", loginName));
            resultData.put("retCode", 1);
            resultData.put("message", "操作成功");
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 删除订单
     * 根据orderId 进行操作
     * 置订单显示状态isShow ： N
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/deleteOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> deleteOrder(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        String orderId = request.getParameter("orderId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            orderHeader.set("isShow", "N");
            orderHeader.store();
            resultData.put("retCode", 1);
            resultData.put("message", "操作成功");
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 申请退换货列表
     * userLoginId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/party/returnList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> returnList(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String viewIndex = request.getParameter("viewIndex");
        String viewSize = request.getParameter("viewSize");
        try {
            resultData = dispatcher.runSync("getReturnList", UtilMisc.toMap("userLoginId", loginName, "viewSize", viewSize, "viewIndex", viewIndex));
            if (ServiceUtil.isError(resultData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 申请退换货
     * 根据orderId 进行操作
     * 接口已废不能用
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/returnOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> returnOrder(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String orderId = request.getParameter("orderId");
        String productIds = request.getParameter("productIds");
        String quantitys = request.getParameter("quantitys");
        String enumId = request.getParameter("enumId");
        String returnReason = request.getParameter("returnReason");
        String applyMoney = request.getParameter("applyMoney");
        String contentIds = request.getParameter("contentIds");
        
        try {
            
            Map<String, Object> returnData = dispatcher.runSync("applyReturn", UtilMisc.toMap("userLoginId", loginName, "orderId", orderId, "productIds", productIds, "quantitys", quantitys, "enumId", enumId, "returnReason", returnReason, "applyMoney", new BigDecimal(applyMoney), "contentIds", contentIds));
            if (ServiceUtil.isError(returnData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 实物申请退换货
     * 根据orderId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/physicalRefund", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> physicalRefund(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String refundType = request.getParameter("refundType");
        String orderId = request.getParameter("orderId");
        String refundMoney = request.getParameter("refundMoney");
        String refundReason = request.getParameter("refundReason");
        String contentIds = request.getParameter("contentIds");
        String enumId = request.getParameter("enumId");
        String productId = request.getParameter("productId");
        try {
            BigDecimal refundMo = new BigDecimal(refundMoney);
            Map<String, Object> returnData = dispatcher.runSync("physicalRefund", UtilMisc.toMap("userLoginId", loginName, "orderId", orderId, "refundType", refundType, "refundMoney", refundMo, "contentIds", contentIds, "refundReason", refundReason, "enumId", enumId, "productId", productId));
            if (ServiceUtil.isError(returnData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("orderId", orderId);
                resultData.put("returnId", returnData.get("returnId"));
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 退货申请 提交物流单号
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/submitOrderDelivery", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> submitOrderDelivery(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        String orderId = request.getParameter("orderId");
        String deliveryCompany = request.getParameter("deliveryCompany");//DELIVERY_COMPANY
        String logisticsNumber = request.getParameter("logisticsNumber1");
        try {
            String id = delegator.getNextSeqId("id");
            GenericValue orderDelivery = delegator.makeValidValue("OrderDelivery");
            orderDelivery.set("id", id);
            orderDelivery.set("orderId", orderId);
            orderDelivery.set("deliveryCompany", deliveryCompany);
            orderDelivery.set("logisticsNumber1", logisticsNumber);
            orderDelivery.create();
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 实物退款/退货详情
     * 根据orderId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/physicalRefundDetail", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> physicalRefundDetail(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String orderId = request.getParameter("orderId");
        try {
            
            Map<String, Object> returnData = dispatcher.runSync("physicalRefundDetail", UtilMisc.toMap("userLoginId", loginName, "orderId", orderId));
            if (ServiceUtil.isError(returnData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                
                Map<String, Object> returnItem = (Map<String, Object>) returnData.get("resultData");
                if (UtilValidate.isNotEmpty(returnItem)) {
                    String refundType = (String) returnItem.get("refundType");
                    String productRefundId = (String) returnItem.get("productRefundId");
                    String status = (String) returnItem.get("status");
                    String refundTime = (String) returnItem.get("refundTime");
                    resultData.put("refundType", refundType);
                    resultData.put("returnId", productRefundId);
                    resultData.put("status", status);
                    resultData.put("refundTime", refundTime);
                    resultData.put("refundType", refundType);
                }
                
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 虚拟申请退换货
     * 根据orderId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/virtualRefund", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> virtualRefund(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String refundType = request.getParameter("refundType");
        String orderId = request.getParameter("orderId");
        String ticketIds = request.getParameter("ticketIds");
        String refundMoney = request.getParameter("refundMoney");
        String refundReason = request.getParameter("refundReason");
        String contentIds = request.getParameter("contentIds");
        try {
            
            Map<String, Object> returnData = dispatcher.runSync("virtualRefund", UtilMisc.toMap("userLoginId", loginName, "orderId", orderId, "refundType", refundType, "refundMoney", refundMoney, "applyMoney", new BigDecimal(refundMoney), "contentIds", contentIds, "refundReason", refundReason, "ticketIds", ticketIds));
            if (ServiceUtil.isError(returnData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 虚拟申请退换货/退货详情
     * 根据orderId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/virtualRefundDetail", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> virtualRefundDetail(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String orderId = request.getParameter("orderId");
        String ticketId = request.getParameter("ticketId");
        try {
            
            Map<String, Object> returnData = dispatcher.runSync("virtualRefundDetail", UtilMisc.toMap("userLoginId", loginName, "orderId", orderId, "ticketId", ticketId));
            if (ServiceUtil.isError(returnData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 退换货/退货日志查询
     * 根据orderId 进行操作
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/refundLog", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> refundLog(HttpServletRequest request, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String returnId = request.getParameter("returnId");
        try {
            
            Map<String, Object> returnData = dispatcher.runSync("refundLog", UtilMisc.toMap("returnId", returnId, "userLoginId", loginName));//"userLoginId",loginName
            if (ServiceUtil.isError(returnData)) {
                resultData.put("retCode", 0);
                resultData.put("message", ServiceUtil.getErrorMessage(resultData));
            } else {
                
                Map<String, Object> returnItem = (Map<String, Object>) returnData.get("resultData");
                List<GenericValue> results = (List<GenericValue>) returnItem.get("returns");
                if (UtilValidate.isNotEmpty(results)) {
                    List returnList = FastList.newInstance();
                    for (int i = 0; i < results.size(); i++) {
                        Map<String, Object> result = results.get(i);
                        Map<String, Object> returnMap = FastMap.newInstance();
                        returnMap.put("operateTime", result.get("operateTime"));
                        returnMap.put("operator", result.get("operator"));
                        returnMap.put("operateType", result.get("operateType"));
                        returnList.add(returnMap);
                        
                    }
                    resultData.put("returnList", returnList);
                }
                
                resultData.put("retCode", 1);
                resultData.put("message", "操作成功");
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 获取退款原因
     *
     * @param request
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getReturnOrderReason", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getReturnOrderReason(HttpServletRequest request, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize, String productId, String orderId) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        int limit = pageSize;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            DynamicViewEntity dynamicView = new DynamicViewEntity();//product_category
            dynamicView.addMemberEntity("E", "Enumeration");
            dynamicView.addAlias("E", "enumId");
            dynamicView.addAlias("E", "enumTypeId");
            dynamicView.addAlias("E", "description");
            dynamicView.addAlias("E", "createdStamp");
            
            List<String> fieldToSel = FastList.newInstance();
            fieldToSel.add("enumId");
            fieldToSel.add("enumTypeId");
            fieldToSel.add("description");
            fieldToSel.add("createdStamp");
            
            dynamicView.setGroupBy(fieldToSel);
            orderBy.add("-createdStamp");

            andExprs.add(EntityCondition.makeCondition("enumTypeId", EntityOperator.EQUALS, "FIELD_RETURN"));//FIELD_RETURN
            andExprs.add(EntityCondition.makeCondition("enumId", EntityOperator.EQUALS, "SERVICE_SUPP_7DAYS"));//FIELD_RETURN
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.OR);
            }
            
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldToSel, null, findOpts);
            List<GenericValue> orderReturnReasons = eli.getPartialList(lowIndex, viewSize);
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();
            
            List<Map<String, Object>> reasonList = FastList.newInstance();

            if (UtilValidate.isNotEmpty(orderReturnReasons)) {
                for (GenericValue orderReturnReason : orderReturnReasons) {
                    Map<String, Object> reasonMap = FastMap.newInstance();
                    
                    if (UtilValidate.isNotEmpty(productId)) {
                        List<GenericValue> serviceAssoc = delegator.findByAnd("ProductSupportServiceAssoc", UtilMisc.toMap("productId", productId, "enumId", "SERVICE_SUPP_7DAYS"));
                    }
                    
                    String enumId = orderReturnReason.getString("enumId");
                    String description = orderReturnReason.getString("description");
                    if (enumId.equals("SERVICE_SUPP_7DAYS")) {
                        reasonMap.put("enumId", enumId);
                        reasonMap.put("description", "七日无理由退换货");
                    }
                    reasonMap.put("enumId", enumId);
                    reasonMap.put("description", description);
                    reasonList.add(reasonMap);
                }
                
            }
            
            if (UtilValidate.isNotEmpty(orderId)) {
                GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(orderHeader)) {
                    //应付金额
                    BigDecimal grandTotal = orderHeader.getBigDecimal("grandTotal").setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId, "productId", productId));
                if (UtilValidate.isNotEmpty(orderItems)) {
                    GenericValue orderItem = orderItems.get(0);
                    //订单项应付的钱 TOTAL_REAL_AMOUNT
                    BigDecimal grandTotal = orderItem.getBigDecimal("totalRealAmount").setScale(2, BigDecimal.ROUND_HALF_UP);
                    resultData.put("grandTotal", grandTotal);
                    
                }

            }
            
            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            
            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
            resultData.put("reasonList", reasonList);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (eli != null) {
                try {
                    eli.close();
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
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    public List<GenericValue> makeItemShipGroupAndAssoc(Delegator delegator, String contactMechId, String shipmentMethodTypeId, String carrierPartyId, String carrierRoleTypeId) {
        String shipGroupSeqId = UtilFormatOut.formatPaddedNumber(1, 5);
        List<GenericValue> values = new LinkedList<GenericValue>();
        
        // create order contact mech for shipping address
        
        GenericValue orderCm = delegator.makeValue("OrderContactMech");
        orderCm.set("contactMechPurposeTypeId", "SHIPPING_LOCATION");
        orderCm.set("contactMechId", contactMechId);
        values.add(orderCm);
        
        
        // create the ship group
        GenericValue shipGroup = delegator.makeValue("OrderItemShipGroup");
        shipGroup.set("shipmentMethodTypeId", shipmentMethodTypeId);
        shipGroup.set("carrierRoleTypeId", carrierRoleTypeId);
        shipGroup.set("carrierPartyId", carrierPartyId);
        shipGroup.set("supplierPartyId", "");
        shipGroup.set("shippingInstructions", "");
        shipGroup.set("giftMessage", "");
        shipGroup.set("contactMechId", contactMechId);
        shipGroup.set("telecomContactMechId", "");
        shipGroup.set("maySplit", "N");
        shipGroup.set("isGift", "N");
        shipGroup.set("shipGroupSeqId", shipGroupSeqId);
        shipGroup.set("vendorPartyId", "");
        shipGroup.set("facilityId", "");
        
        // use the cart's default ship before and after dates here
        
        values.add(shipGroup);
        
        
        GenericValue assoc = delegator.makeValue("OrderItemShipGroupAssoc");
        assoc.set("orderItemSeqId", "00001");
        assoc.set("shipGroupSeqId", shipGroupSeqId);
        assoc.set("quantity", BigDecimal.ONE);
        values.add(assoc);
        
        return values;
    }
    
    
    private List<GenericValue> makeOrderPaymentInfo(Delegator delegator, String paymentMethodTypeId, String paymentMethodId) {
        List<GenericValue> values = new LinkedList<GenericValue>();
        GenericValue opp = delegator.makeValue("OrderPaymentPreference");
        opp.set("paymentMethodTypeId", paymentMethodTypeId);
        opp.set("presentFlag", "N");
        opp.set("swipedFlag", "N");
        opp.set("overflowFlag", "N");
        opp.set("paymentMethodId", paymentMethodId);
        values.add(opp);
        return values;
        
        
    }
    
    public List<GenericValue> makeOrderItems(Delegator delegator, String productId, String activityId, BigDecimal unitPrice, BigDecimal listPrice, String itemName, String userName, BigDecimal totalRealAmount) {
        
        List<GenericValue> result = FastList.newInstance();
        
        
        GenericValue orderItem = delegator.makeValue("OrderItem");
        orderItem.set("orderItemSeqId", "00001");
        
        orderItem.set("orderItemTypeId", "PRODUCT_ORDER_ITEM");
        
        orderItem.set("productId", productId);
        
        
        orderItem.set("quantity", BigDecimal.ONE);
        
        orderItem.set("unitPrice", unitPrice);
        orderItem.set("unitListPrice", listPrice);
        orderItem.set("isModifiedPrice", "Y");
        orderItem.set("isPromo", "N");
        
        orderItem.set("shoppingListId", "");
        orderItem.set("shoppingListItemSeqId", "00001");
        
        orderItem.set("itemDescription", itemName);
        
        orderItem.set("statusId", "ITEM_CREATED");
        
        orderItem.set("totalRealAmount", totalRealAmount);
        orderItem.set("changeByUserLoginId", userName);
        
        
        result.add(orderItem);
        // don't do anything with adjustments here, those will be added below in makeAllAdjustments
        
        
        return result;
    }
    
    private List<GenericValue> makeOrderContactMechs(Delegator delegator, String shippingContactMechId) {
        
        List<GenericValue> allOrderContactMechs = new LinkedList<GenericValue>();
        
        
        GenericValue orderContactMech = delegator.makeValue("OrderContactMech");
        orderContactMech.set("contactMechPurposeTypeId", "SHIPPING_LOCATION");
        orderContactMech.set("contactMechId", shippingContactMechId);
        allOrderContactMechs.add(orderContactMech);
        
        
        return allOrderContactMechs;
        
    }
    
}
