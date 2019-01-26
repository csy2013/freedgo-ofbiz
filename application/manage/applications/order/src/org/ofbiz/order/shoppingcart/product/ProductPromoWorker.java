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
package org.ofbiz.order.shoppingcart.product;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.ProductSearch;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

/**
 * ProductPromoWorker - Worker class for catalog/product promotion related functionality
 */
public class ProductPromoWorker {
    
    public static final String module = ProductPromoWorker.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";
    
    public static final int decimals = UtilNumber.getBigDecimalScale("order.decimals");
    public static final int rounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");
    
    public static final MathContext generalRounding = new MathContext(10);
    
    public static List<GenericValue> getStoreProductPromos(Delegator delegator, String productStoreId, LocalDispatcher dispatcher, ServletRequest request) {
        List<GenericValue> productPromos = FastList.newInstance();
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        
        // get the ShoppingCart out of the session.
        HttpServletRequest req = null;
        ShoppingCart cart = null;
        try {
            req = (HttpServletRequest) request;
            cart = ShoppingCartEvents.getCartObject(req);
        } catch (ClassCastException cce) {
            Debug.logError("Not a HttpServletRequest, no shopping cart found.", module);
            return null;
        } catch (IllegalArgumentException e) {
            Debug.logError(e, module);
            return null;
        }
        
        boolean condResult = true;
        
        try {
            
            GenericValue productStore = null;
            try {
                productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Error looking up store with id " + productStoreId, module);
            }
            if (productStore == null) {
                Debug.logWarning(UtilProperties.getMessage(resource_error, "OrderNoStoreFoundWithIdNotDoingPromotions", UtilMisc.toMap("productStoreId", productStoreId), cart.getLocale()), module);
                return productPromos;
            }
            
            if (productStore != null) {
                Iterator<GenericValue> productStorePromoAppls = UtilMisc.toIterator(EntityUtil.filterByDate(productStore.getRelatedCache("ProductStorePromoAppl", UtilMisc.toMap("productStoreId", productStoreId), UtilMisc.toList("sequenceNum")), true));
                while (productStorePromoAppls != null && productStorePromoAppls.hasNext()) {
                    GenericValue productStorePromoAppl = productStorePromoAppls.next();
                    
                    if (UtilValidate.isNotEmpty(productStorePromoAppl.getString("manualOnly")) && "Y".equals(productStorePromoAppl.getString("manualOnly"))) {
                        // manual only promotions are not automatically evaluated (they must be explicitly selected by the user)
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("Skipping promotion with id [" + productStorePromoAppl.getString("productPromoId") + "] because it is applied to the store with ID " + productStoreId + " as a manual only promotion.", module);
                        }
                        continue;
                    }
                    GenericValue productPromo = productStorePromoAppl.getRelatedOneCache("ProductPromo");
                    List<GenericValue> productPromoRules = productPromo.getRelatedCache("ProductPromoRule", null, null);
                    
                    
                    if (productPromoRules != null) {
                        Iterator<GenericValue> promoRulesItr = productPromoRules.iterator();
                        
                        while (condResult && promoRulesItr != null && promoRulesItr.hasNext()) {
                            GenericValue promoRule = promoRulesItr.next();
                            Iterator<GenericValue> productPromoConds = UtilMisc.toIterator(promoRule.getRelatedCache("ProductPromoCond", null, UtilMisc.toList("productPromoCondSeqId")));
                            
                            while (condResult && productPromoConds != null && productPromoConds.hasNext()) {
                                GenericValue productPromoCond = productPromoConds.next();
                                
                                // evaluate the party related conditions; so we don't show the promo if it doesn't apply.
                                if ("PPIP_PARTY_ID".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, productStoreId, cart, delegator, dispatcher, nowTimestamp);
                                } else if ("PPIP_PARTY_GRP_MEM".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, productStoreId, cart, delegator, dispatcher, nowTimestamp);
                                } else if ("PPIP_PARTY_CLASS".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, productStoreId, cart, delegator, dispatcher, nowTimestamp);
                                } else if ("PPIP_ROLE_TYPE".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, productStoreId, cart, delegator, dispatcher, nowTimestamp);
                                }
                            }
                        }
                        if (!condResult) {
                            productPromo = null;
                        }
                    }
                    if (productPromo != null) {
                        productPromos.add(productPromo);
                    }
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return productPromos;
    }
    
    public static Set<String> getStoreProductPromoCodes(ShoppingCart cart, String productStoreId) {
        Set<String> promoCodes = FastSet.newInstance();
        Delegator delegator = cart.getDelegator();
        GenericValue productStore = null;
        try {
            productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error looking up store with id " + productStoreId, module);
        }
        if (productStore == null) {
            Debug.logWarning(UtilProperties.getMessage(resource_error, "OrderNoStoreFoundWithIdNotDoingPromotions", UtilMisc.toMap("productStoreId", productStoreId), cart.getLocale()), module);
            return promoCodes;
        }
        try {
            Iterator<GenericValue> productStorePromoAppls = UtilMisc.toIterator(EntityUtil.filterByDate(productStore.getRelatedCache("ProductStorePromoAppl", UtilMisc.toMap("productStoreId", productStoreId), UtilMisc.toList("sequenceNum")), true));
            while (productStorePromoAppls != null && productStorePromoAppls.hasNext()) {
                GenericValue productStorePromoAppl = productStorePromoAppls.next();
                if (UtilValidate.isNotEmpty(productStorePromoAppl.getString("manualOnly")) && "Y".equals(productStorePromoAppl.getString("manualOnly"))) {
                    // manual only promotions are not automatically evaluated (they must be explicitly selected by the user)
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("Skipping promotion with id [" + productStorePromoAppl.getString("productPromoId") + "] because it is applied to the store with ID " + productStoreId + " as a manual only promotion.", module);
                    }
                    continue;
                }
                GenericValue productPromo = productStorePromoAppl.getRelatedOneCache("ProductPromo");
                Iterator<GenericValue> productPromoCodesIter = UtilMisc.toIterator(productPromo.getRelatedCache("ProductPromoCode", null, null));
                while (productPromoCodesIter != null && productPromoCodesIter.hasNext()) {
                    GenericValue productPromoCode = productPromoCodesIter.next();
                    promoCodes.add(productPromoCode.getString("productPromoCodeId"));
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return promoCodes;
    }
    
    /**
     * add by changsy 活动所有店铺的促销信息，根据shoppingCart 里面对应店铺
     *
     * @param cartItems
     * @param nowTimestamp
     * @param dispatcher
     * @return
     */
    public static List<GenericValue> getProductStorePromotions(List<ShoppingCartItem> cartItems, Timestamp nowTimestamp, LocalDispatcher dispatcher) {
        List<GenericValue> productPromoList = FastList.newInstance();
        //第一步取对应店铺
        Set<String> productStoreIds = FastSet.newInstance();
        Delegator delegator = null;
        for (ShoppingCartItem cartItem : cartItems) {
            delegator = cartItem.getDelegator();
            String productStoreId = cartItem.getProductStoreId();
            productStoreIds.add(productStoreId);
        }
         if(UtilValidate.isNotEmpty(productStoreIds) ){
             Iterator productStoreIter = productStoreIds.iterator();
             while (productStoreIter.hasNext()) {
                 String productStoreId = (String)productStoreIter.next();
                 GenericValue productStore = null;
                 try {
                     productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
                 } catch (GenericEntityException e) {
                     Debug.logError(e, "Error looking up store with id " + productStoreId, module);
                 }
                 if (productStore == null) {
                     Debug.logWarning(UtilProperties.getMessage(resource_error, "OrderNoStoreFoundWithIdNotDoingPromotions", UtilMisc.toMap("productStoreId", productStoreId), Locale.CHINA), module);
                     return productPromoList;
                 }
                 try {
                     // loop through promotions and get a list of all of the rules...
                     //根据规则找到所有指定商品的促销
                     List<GenericValue> productStorePromoApplsList = delegator.findByAnd("ProductStorePromoAndAppl", UtilMisc.toMap("promoStatus","ACTY_AUDIT_PASS","productStoreId", productStoreId,"promoProductType","PROMO_PRT_PART_IN"),UtilMisc.toList("fromDate"));
                     productStorePromoApplsList = EntityUtil.filterByDate(productStorePromoApplsList, nowTimestamp);
                     /* 按优先级从高到低取店铺内的满减、折扣、满赠、包邮；*/
                     List<String> promoTypes = UtilMisc.toList("PROMO_SPE_PRICE", "PROMO_PRE_REDUCE", "PROMO_REDUCE", "PROMO_DISCOUNT", "PROMO_GIFT");
                     promoTypes.add("PROMO_FREE_SHIPPING");
                     promoTypes.add("PROMO_SHIP_CHARGE");
                    
                     List<GenericValue> appsList = FastList.newInstance();
                     for (int i = 0; i < promoTypes.size(); i++) {
                         String promoType = promoTypes.get(i);
                         for (int j = 0; j < productStorePromoApplsList.size(); j++) {
                             GenericValue productStorePromoAppl = productStorePromoApplsList.get(j);
                             if (productStorePromoAppl.get("promoType").equals(promoType)) {
                                 appsList.add(productStorePromoAppl);
                             }
                         }
                     }
    
                     //根据规则找到全场通用促销,包括最后的新用户
                     List<GenericValue> productStorePromoApplsList1 = delegator.findByAnd("ProductStorePromoAndAppl", UtilMisc.toMap("promoStatus","ACTY_AUDIT_PASS","productStoreId", productStoreId,"promoProductType","PROMO_PRT_ALL"),UtilMisc.toList("fromDate"));
                     productStorePromoApplsList1 = EntityUtil.filterByDate(productStorePromoApplsList1, nowTimestamp);
                     /* 按优先级从高到低取店铺内的满减、折扣、满赠、包邮；*/
                     promoTypes.add("PROMO_NEW_CUST");
                     for (int i = 0; i < promoTypes.size(); i++) {
                         String promoType = promoTypes.get(i);
                         for (int j = 0; j < productStorePromoApplsList1.size(); j++) {
                             GenericValue productStorePromoAppl = productStorePromoApplsList1.get(j);
                             if (productStorePromoAppl.get("promoType").equals(promoType)) {
                                 appsList.add(productStorePromoAppl);
                             }
                         }
                     }
                     
                     
                     Iterator<GenericValue> prodCatalogPromoAppls = UtilMisc.toIterator(appsList);
                     while (prodCatalogPromoAppls != null && prodCatalogPromoAppls.hasNext()) {
                         GenericValue prodCatalogPromoAppl = prodCatalogPromoAppls.next();
            
                         GenericValue productPromo = prodCatalogPromoAppl.getRelatedOneCache("ProductPromo");
                         //判断促销是否正式启用 add by changsy 20180321
                         if (productPromo.get("promoStatus") != null && "ACTY_AUDIT_PASS".equalsIgnoreCase(productPromo.getString("promoStatus"))) {
                             String productPromoId = productPromo.getString("productPromoId");
                             boolean hasIn = false;
                             for (int i = 0; i < productPromoList.size(); i++) {
                                 GenericValue proPromo = productPromoList.get(i);
                                 if (proPromo.getString("productPromoId").equals(productPromoId)) {
                                     hasIn = true;
                                 }
                             }
                             if (!hasIn) {
                                 productPromoList.add(productPromo);
                             }
                         }
            
                     }
                 } catch (GenericEntityException e) {
                     Debug.logError(e, "Error looking up promotion data while doing promotions", module);
                 }
             }
        }/*满减、折扣、满赠、包邮*/
        return productPromoList;
    }
    
    public static List<GenericValue> getProductStorePromotions(ShoppingCart cart, String productStoreId, Timestamp nowTimestamp, LocalDispatcher dispatcher) {
        List<GenericValue> productPromoList = FastList.newInstance();
        
        Delegator delegator = cart.getDelegator();
        
        GenericValue productStore = null;
        try {
            productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error looking up store with id " + productStoreId, module);
        }
        if (productStore == null) {
            Debug.logWarning(UtilProperties.getMessage(resource_error, "OrderNoStoreFoundWithIdNotDoingPromotions", UtilMisc.toMap("productStoreId", productStoreId), cart.getLocale()), module);
            return productPromoList;
        }
        
        try {
            // loop through promotions and get a list of all of the rules...
            List<GenericValue> productStorePromoApplsList = productStore.getRelatedCache("ProductStorePromoAppl", null, UtilMisc.toList("sequenceNum"));
            productStorePromoApplsList = EntityUtil.filterByDate(productStorePromoApplsList, nowTimestamp);
            
            if (UtilValidate.isEmpty(productStorePromoApplsList)) {
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Not doing promotions, none applied to store with ID " + productStoreId, module);
                }
            }
            
            Iterator<GenericValue> prodCatalogPromoAppls = UtilMisc.toIterator(productStorePromoApplsList);
            while (prodCatalogPromoAppls != null && prodCatalogPromoAppls.hasNext()) {
                GenericValue prodCatalogPromoAppl = prodCatalogPromoAppls.next();
                if (UtilValidate.isNotEmpty(prodCatalogPromoAppl.getString("manualOnly")) && "Y".equals(prodCatalogPromoAppl.getString("manualOnly"))) {
                    // manual only promotions are not automatically evaluated (they must be explicitly selected by the user)
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("Skipping promotion with id [" + prodCatalogPromoAppl.getString("productPromoId") + "] because it is applied to the store with ID " + productStoreId + " as a manual only promotion.", module);
                    }
                    continue;
                }
                GenericValue productPromo = prodCatalogPromoAppl.getRelatedOneCache("ProductPromo");
                productPromoList.add(productPromo);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error looking up promotion data while doing promotions", module);
        }
        return productPromoList;
    }
    
    
    public static void doPromotions(ShoppingCart cart, LocalDispatcher dispatcher) {
        ProductPromoWorker.doPromotions(cart, null, dispatcher);
    }
    
    public static void doPromotions(ShoppingCart cart, List<GenericValue> productPromoList, LocalDispatcher dispatcher) {
        // this is called when a user logs in so that per customer limits are honored, called by cart when new userlogin is set
        // there is code to store ProductPromoUse information when an order is placed
        // ProductPromoUses are ignored if the corresponding order is cancelled
        // limits sub total for promos to not use gift cards (products with a don't use in promo indicator), also exclude gift cards from all other promotion considerations including subTotals for discounts, etc
        // TODO: (not done, delay, still considering...) add code to check ProductPromoUse limits per promo (customer, promo), and per code (customer, code) to avoid use of promos or codes getting through due to multiple carts getting promos applied at the same time, possibly on totally different servers
        
        if (!cart.getDoPromotions()) {
            return;
        }
        Delegator delegator = cart.getDelegator();
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        
        // start out by clearing all existing promotions, then we can just add all that apply
        cart.clearAllPromotionInformation();
        
        // there will be a ton of db access, so just do a big catch entity exception block
        try {
            //增加商品组合的优惠,获取组合商品的id和购物车的productId包含比较
            //获取组合销售
            List<GenericValue> groupPromos = EntityUtil.filterByDate(delegator.findByAnd("ProductGroupPromoInfo1", UtilMisc.toMap("result", "ACTY_AUDIT_PASS")));
            //产品数量
            List<Map<String, Long>> promoProductIds = FastList.newInstance();
            if (UtilValidate.isNotEmpty(groupPromos)) {
                
                for (int i = 0; i < groupPromos.size(); i++) {
                    
                    GenericValue groupPromo = groupPromos.get(i);
                    List<GenericValue> groupPromoProducts = groupPromo.getRelated("ProGrpPromoProduct");
                    
                    if (UtilValidate.isNotEmpty(groupPromoProducts)) {
                        
                        int productSize = groupPromoProducts.size();
                        
                        Map<String, BigDecimal> perProductQuantity = FastMap.newInstance();
                        //获取组合商品的可以使用数量
                        for (int j = 0; j < groupPromoProducts.size(); j++) {
                            GenericValue groupPromoProduct = groupPromoProducts.get(j);
                            List<ShoppingCartItem> shoppingCartItems = cart.items();
                            if (UtilValidate.isNotEmpty(shoppingCartItems)) {
                                for (int k = 0; k < shoppingCartItems.size(); k++) {
                                    ShoppingCartItem cartItem = shoppingCartItems.get(k);
                                    if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose())) {
                                        String cartProductId = cartItem.getProductId();
                                        String promoProductId = groupPromoProduct.getString("productId");
                                        Debug.log(cartProductId + "," + promoProductId);
                                        if (cartItem.getProductId().equals(groupPromoProduct.getString("productId")) && cartItem.getQuantity().compareTo(new BigDecimal(groupPromoProduct.getLong("quantity"))) >= 0) {
                                            perProductQuantity.put(promoProductId, cartItem.getQuantity());
                                        }
                                    }
                                }
                            }
                        }
                        //判断过滤出来的产品和产品组合的产品数量一致
                        while (perProductQuantity.size() == groupPromoProducts.size()) {
                            int count = 0;
                            BigDecimal totalItemPrices = BigDecimal.ZERO;
                            for (int j = 0; j < groupPromoProducts.size(); j++) {
                                GenericValue groupPromoProduct = groupPromoProducts.get(j);
                                List<ShoppingCartItem> shoppingCartItems = cart.items();
                                if (UtilValidate.isNotEmpty(shoppingCartItems)) {
                                    for (int k = 0; k < shoppingCartItems.size(); k++) {
                                        ShoppingCartItem cartItem = shoppingCartItems.get(k);
                                        if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose())) {
                                            String cartProductId = cartItem.getProductId();
                                            String promoProductId = groupPromoProduct.getString("productId");
                                            Debug.log(cartProductId + "," + promoProductId);
                                            //增加可用使用的数量
                                            if (cartItem.getProductId().equals(groupPromoProduct.getString("productId")) && perProductQuantity.get(promoProductId).compareTo(new BigDecimal(groupPromoProduct.getLong("quantity"))) >= 0) {
                                                count++;
                                                BigDecimal leftQuantity = perProductQuantity.get(promoProductId).subtract(new BigDecimal(groupPromoProduct.getLong("quantity")));
                                                BigDecimal productPromoQuantity = new BigDecimal(groupPromoProduct.getLong("quantity"));
                                                if (leftQuantity.compareTo(productPromoQuantity) >= 0) {
                                                    perProductQuantity.put(promoProductId, perProductQuantity.get(promoProductId).subtract(new BigDecimal(groupPromoProduct.getLong("quantity"))));
                                                } else {
                                                    perProductQuantity.remove(promoProductId);
                                                }
                                                
                                                totalItemPrices = totalItemPrices.add(cartItem.getBasePrice().multiply(productPromoQuantity));
                                            }
                                        }
                                    }
                                }
                            }
                            if (count == productSize) {
                                BigDecimal promoPrice = totalItemPrices.subtract(groupPromo.getBigDecimal("promoPrice")).negate();
                                //计算组合商品的总价
                                BigDecimal groupItemSubTotal = BigDecimal.ZERO;
                                if (promoPrice.compareTo(BigDecimal.ZERO) != 0) {
                                    //设置cartItem里面对应的产品为组合促销产品
                                    for (int j = 0; j < groupPromoProducts.size(); j++) {
                                        GenericValue groupProduct = groupPromoProducts.get(j);
                                        List<ShoppingCartItem> shoppingCartItems = cart.items();
                                        for (int k = 0; k < shoppingCartItems.size(); k++) {
                                            ShoppingCartItem item = shoppingCartItems.get(k);
                                            if (UtilValidate.isNotEmpty(item.getIsChoose()) && "Y".equals(item.getIsChoose())) {
                                                if (item.getProductId().equals(groupProduct.getString("productId"))) {
                                                    groupItemSubTotal = groupItemSubTotal .add (item.getBasePrice() .multiply(new BigDecimal(groupProduct.getLong("quantity"))));
                                                }
                                            }
                                        }
                                    }
                                    //总优惠金额
                                    BigDecimal discount = groupItemSubTotal.subtract(groupPromo.getBigDecimal("promoPrice"));
                                    //计算每个商品的实退金额
                                    for (int j = 0; j < groupPromoProducts.size(); j++) {
                                        GenericValue groupProduct = groupPromoProducts.get(j);
                                        List<ShoppingCartItem> shoppingCartItems = cart.items();
                                        for (int k = 0; k < shoppingCartItems.size(); k++) {
                                            ShoppingCartItem item = shoppingCartItems.get(k);
                                            if (UtilValidate.isNotEmpty(item.getIsChoose()) && "Y".equals(item.getIsChoose())) {
                                                if (item.getProductId().equals(groupProduct.getString("productId"))) {
                                                    item.setAttribute("isGroupProduct", "Y");
                                                    item.setIsPromo(true);
                                                    //计算商品的实退金额，可以有多次
                                                    BigDecimal itemSubTotal = item.getBasePrice().multiply(new BigDecimal(groupProduct.getLong("quantity")));
                                                    BigDecimal cartItemRealPrice = itemSubTotal.divide(groupItemSubTotal, 12, RoundingMode.HALF_UP).multiply(discount).setScale(2, RoundingMode.HALF_UP);
                                                    BigDecimal recurringBasePrice = item.getRecurringBasePrice()==null?BigDecimal.ZERO:item.getRecurringBasePrice();
                                                    item.setRecurringBasePrice(recurringBasePrice.add(cartItemRealPrice));
                                                    item.setIsPromo(true);
                                                    GenericValue productPromoAction = delegator.makeValue("ProductPromoAction", UtilMisc.toMap("productPromoId", groupPromo.getString("productGrpId"), "orderAdjustmentTypeId", "GROUP_PROMO_TYPE", "quantity", BigDecimal.ONE, "amount", groupPromo.getBigDecimal("promoPrice")));
                                                    doOrderItemPromoAction(productPromoAction, item, cartItemRealPrice.negate(), "amount", delegator, item.getProductStoreId(), "GROUP_PROMO_TYPE",groupPromo.getString("promoName"));
    
                                                }
                                            }
                                        }
                                    }
//                                    GenericValue productPromoAction = delegator.makeValue("ProductPromoAction", UtilMisc.toMap("productPromoId", groupPromo.getString("productGrpId"), "orderAdjustmentTypeId", "GROUP_PROMO_TYPE", "quantity", BigDecimal.ONE, "amount", groupPromo.getBigDecimal("promoPrice")));
//                                    doOrderPromoAction(productPromoAction, cart, promoPrice, "amount", delegator, groupPromo.getString("productStoreId"), "GROUP_PROMO_TYPE", groupPromo.getString("promoName"), "TOGETHER_TYPE");
                                    
                                }
                            }
                        }
                        
                    }
                }
            }
            //获取购物车的productId
            
            if (productPromoList == null) {
                /* 获取所有的促销活动*/
                List<ShoppingCartItem> cartItems = cart.items();
                List<ShoppingCartItem> chooseCartItems = FastList.newInstance();
                if (UtilValidate.isNotEmpty(cartItems)) {
                    for (int i = 0; i < cartItems.size(); i++) {
                        ShoppingCartItem cartItem = cartItems.get(i);
                        if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose())) {
                            chooseCartItems.add(cartItem);
                        }
                    }
                }
                productPromoList = ProductPromoWorker.getProductStorePromotions(chooseCartItems, nowTimestamp, dispatcher);
                
            }
            // do a calculate only run through the promotions, then order by descending totalDiscountAmount for each promotion
            // NOTE: on this run, with isolatedTestRun passed as false it should not apply any adjustments
            //  or track which cart items are used for which promotions, but it will track ProductPromoUseInfo and
            //  useLimits; we are basicly just trying to run each promo "independently" to see how much each is worth
//            runProductPromos(productPromoList, cart, delegator, dispatcher, nowTimestamp, true);
            
            // NOTE: after that first pass we could remove any that have a 0 totalDiscountAmount from the run list, but we won't because by the time they are run the cart may have changed enough to get them to go; also, certain actions like free shipping should always be run even though we won't know what the totalDiscountAmount is at the time the promotion is run
            // each ProductPromoUseInfo on the shopping cart will contain it's total value, so add up all totals for each promoId and put them in a List of Maps
            // create a List of Maps with productPromo and totalDiscountAmount, use the Map sorter to sort them descending by totalDiscountAmount
            
            // before sorting split into two lists and sort each list; one list for promos that have a order total condition, and the other list for all promos that don't; then we'll always run the ones that have no condition on the order total first
            List<Map<Object, Object>> productPromoDiscountMapList = FastList.newInstance();
            List<Map<Object, Object>> productPromoDiscountMapListOrderTotal = FastList.newInstance();
            for (GenericValue productPromo : productPromoList) {
                Map<Object, Object> productPromoDiscountMap = UtilGenerics.checkMap(UtilMisc.toMap("productPromo", productPromo, "totalDiscountAmount", cart.getProductPromoUseTotalDiscount(productPromo.getString("productPromoId"))));
                if (hasOrderTotalCondition(productPromo, delegator)) {
                    productPromoDiscountMapListOrderTotal.add(productPromoDiscountMap);
                } else {
                    productPromoDiscountMapList.add(productPromoDiscountMap);
                }
            }
            // sort the Map List, do it ascending because the discount amounts will be negative, so the lowest number is really the highest discount
            productPromoDiscountMapList = UtilMisc.sortMaps(productPromoDiscountMapList, UtilMisc.toList("+totalDiscountAmount"));
            productPromoDiscountMapListOrderTotal = UtilMisc.sortMaps(productPromoDiscountMapListOrderTotal, UtilMisc.toList("+totalDiscountAmount"));
            productPromoDiscountMapList.addAll(productPromoDiscountMapListOrderTotal);
            
            List<GenericValue> sortedProductPromoList = new ArrayList<GenericValue>(productPromoDiscountMapList.size());
            Iterator<Map<Object, Object>> productPromoDiscountMapIter = productPromoDiscountMapList.iterator();
            while (productPromoDiscountMapIter.hasNext()) {
                Map<Object, Object> productPromoDiscountMap = UtilGenerics.checkMap(productPromoDiscountMapIter.next());
                GenericValue productPromo = (GenericValue) productPromoDiscountMap.get("productPromo");
                sortedProductPromoList.add(productPromo);
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Sorted Promo [" + productPromo.getString("productPromoId") + "] with total discount: " + productPromoDiscountMap.get("totalDiscountAmount"), module);
                }
            }
            
            // okay, all ready, do the real run, clearing the temporary result first...
//            cart.clearAllPromotionInformation();
            runProductPromos(sortedProductPromoList, cart, delegator, dispatcher, nowTimestamp, false);
        } catch (NumberFormatException e) {
            Debug.logError(e, "Number not formatted correctly in promotion rules, not completed...", module);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error looking up promotion data while doing promotions", module);
        } catch (Exception e) {
            Debug.logError(e, "Error running promotions, will ignore: " + e.toString(), module);
        }
    }
    
    protected static boolean hasOrderTotalCondition(GenericValue productPromo, Delegator delegator) throws GenericEntityException {
        boolean hasOtCond = false;
        List<GenericValue> productPromoConds = delegator.findByAndCache("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromo.get("productPromoId")), UtilMisc.toList("productPromoCondSeqId"));
        for (GenericValue productPromoCond : productPromoConds) {
            String inputParamEnumId = productPromoCond.getString("inputParamEnumId");
            if ("PPIP_ORDER_TOTAL".equals(inputParamEnumId)) {
                hasOtCond = true;
                break;
            }
        }
        return hasOtCond;
    }
    
    protected static void runProductPromos(List<GenericValue> productPromoList, ShoppingCart cart, Delegator delegator, LocalDispatcher dispatcher, Timestamp nowTimestamp, boolean isolatedTestRun) throws GeneralException {
        String partyId = cart.getPartyId();
        
        // this is our safety net; we should never need to loop through the rules more than a certain number of times, this is that number and may have to be changed for insanely large promo sets...
        long maxIterations = 1000;
        // part of the safety net to avoid infinite iteration
        long numberOfIterations = 0;
        
        // set a max limit on how many times each promo can be run, for cases where there is no use limit this will be the use limit
        //default to 2 times the number of items in the cart
        long maxUseLimit = cart.getTotalQuantity().multiply(BigDecimal.valueOf(50)).setScale(0, BigDecimal.ROUND_CEILING).longValue();
        
        try {
            // repeat until no more rules to run: either all rules are run, or no changes to the cart in a loop
            boolean cartChanged = true;
            
            for (GenericValue productPromo : productPromoList) {
                String productPromoId = productPromo.getString("productPromoId");
                List<GenericValue> productPromoRules = productPromo.getRelatedCache("ProductPromoRule", null, null);
                if (UtilValidate.isNotEmpty(productPromoRules)) {
                    // always have a useLimit to avoid unlimited looping, default to 1 if no other is specified
                    Long candidateUseLimit = getProductPromoUseLimit(productPromo, partyId, delegator);
                    Long useLimit = candidateUseLimit;
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("Running promotion [" + productPromoId + "], useLimit=" + useLimit + ", # of rules=" + productPromoRules.size(), module);
                    }
                    try {
                        if (runProductPromoRules(cart, cartChanged, useLimit, false, null, null, maxUseLimit, productPromo, productPromoRules, dispatcher, delegator, nowTimestamp)) {
                            cartChanged = true;
                        }
                    } catch (RuntimeException e) {
                        throw new GeneralException("Error running promotion with ID [" + productPromoId + "]", e);
                    }
                    
                }
                
                // if this is an isolatedTestRun clear out adjustments and cart item promo use info
                if (isolatedTestRun) {
                    cart.clearAllPromotionAdjustments();
                    cart.clearCartItemUseInPromoInfo();
                }
            }
            
            
        } catch (UseLimitException e) {
            Debug.logError(e, e.toString(), module);
        }
    }
    
    /**
     * calculate low use limit for this promo for the current "order", check per order, customer, promo
     */
    public static Long getProductPromoUseLimit(GenericValue productPromo, String partyId, Delegator delegator) throws GenericEntityException {
        String productPromoId = productPromo.getString("productPromoId");
        Long candidateUseLimit = null;
        
        Long useLimitPerOrder = productPromo.getLong("useLimitPerOrder");
        if (useLimitPerOrder != null) {
            if (candidateUseLimit == null || candidateUseLimit.longValue() > useLimitPerOrder.longValue()) {
                candidateUseLimit = useLimitPerOrder;
            }
        }
        
        // Debug.logInfo("Promo [" + productPromoId + "] use limit after per order check: " + candidateUseLimit, module);
        
        Long useLimitPerCustomer = productPromo.getLong("useLimitPerCustomer");
        // check this whether or not there is a party right now
        if (useLimitPerCustomer != null) {
            // if partyId is not empty check previous usage
            long productPromoCustomerUseSize = 0;
            if (UtilValidate.isNotEmpty(partyId)) {
                // check to see how many times this has been used for other orders for this customer, the remainder is the limit for this order
                EntityCondition checkCondition = EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId),
                        EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
                        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_REJECTED"),
                        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED")), EntityOperator.AND);
                productPromoCustomerUseSize = delegator.findCountByCondition("ProductPromoUseCheck", checkCondition, null, null);
            }
            long perCustomerThisOrder = useLimitPerCustomer.longValue() - productPromoCustomerUseSize;
            if (candidateUseLimit == null || candidateUseLimit.longValue() > perCustomerThisOrder) {
                candidateUseLimit = Long.valueOf(perCustomerThisOrder);
            }
        }
        
        // Debug.logInfo("Promo [" + productPromoId + "] use limit after per customer check: " + candidateUseLimit, module);
        
        Long useLimitPerPromotion = productPromo.getLong("useLimitPerPromotion");
        if (useLimitPerPromotion != null) {
            // check to see how many times this has been used for other orders for this customer, the remainder is the limit for this order
            EntityCondition checkCondition = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId),
                    EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_REJECTED"),
                    EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED")), EntityOperator.AND);
            long productPromoUseSize = delegator.findCountByCondition("ProductPromoUseCheck", checkCondition, null, null);
            long perPromotionThisOrder = useLimitPerPromotion.longValue() - productPromoUseSize;
            if (candidateUseLimit == null || candidateUseLimit.longValue() > perPromotionThisOrder) {
                candidateUseLimit = Long.valueOf(perPromotionThisOrder);
            }
        }
        
        // Debug.logInfo("Promo [" + productPromoId + "] use limit after per promotion check: " + candidateUseLimit, module);
        
        return candidateUseLimit;
    }
    
    public static Long getProductPromoCodeUseLimit(GenericValue productPromoCode, String partyId, Delegator delegator) throws GenericEntityException {
        String productPromoCodeId = productPromoCode.getString("productPromoCodeId");
        Long codeUseLimit = null;
        
        // check promo code use limits, per customer, code
        Long codeUseLimitPerCustomer = productPromoCode.getLong("useLimitPerCustomer");
        if (codeUseLimitPerCustomer != null) {
            long productPromoCustomerUseSize = 0;
            if (UtilValidate.isNotEmpty(partyId)) {
                // check to see how many times this has been used for other orders for this customer, the remainder is the limit for this order
                EntityCondition checkCondition = EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition("productPromoCodeId", EntityOperator.EQUALS, productPromoCodeId),
                        EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId),
                        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_REJECTED"),
                        EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED")), EntityOperator.AND);
                productPromoCustomerUseSize = delegator.findCountByCondition("ProductPromoUseCheck", checkCondition, null, null);
            }
            long perCustomerThisOrder = codeUseLimitPerCustomer.longValue() - productPromoCustomerUseSize;
            if (codeUseLimit == null || codeUseLimit.longValue() > perCustomerThisOrder) {
                codeUseLimit = Long.valueOf(perCustomerThisOrder);
            }
        }
        
        Long codeUseLimitPerCode = productPromoCode.getLong("useLimitPerCode");
        if (codeUseLimitPerCode != null) {
            // check to see how many times this has been used for other orders for this customer, the remainder is the limit for this order
            EntityCondition checkCondition = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition("productPromoCodeId", EntityOperator.EQUALS, productPromoCodeId),
                    EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_REJECTED"),
                    EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED")), EntityOperator.AND);
            long productPromoCodeUseSize = delegator.findCountByCondition("ProductPromoUseCheck", checkCondition, null, null);
            long perCodeThisOrder = codeUseLimitPerCode.longValue() - productPromoCodeUseSize;
            if (codeUseLimit == null || codeUseLimit.longValue() > perCodeThisOrder) {
                codeUseLimit = Long.valueOf(perCodeThisOrder);
            }
        }
        
        return codeUseLimit;
    }
    
    public static String checkCanUsePromoCode(String productPromoCodeId, String partyId, Delegator delegator, Locale locale) {
        return checkCanUsePromoCode(productPromoCodeId, partyId, delegator, null, locale);
    }
    
    public static String checkCanUsePromoCode(String productPromoCodeId, String partyId, Delegator delegator, ShoppingCart cart, Locale locale) {
        try {
            GenericValue productPromoCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId", productPromoCodeId));
            if (productPromoCode == null) {
                return UtilProperties.getMessage(resource_error, "productpromoworker.promotion_code_not_valid", UtilMisc.toMap("productPromoCodeId", productPromoCodeId), locale);
            }
            //活动productPromoCodeId对应的店铺
            GenericValue productPromoCoupon = productPromoCode.getRelatedOne("ProductPromoCoupon");
            if (UtilValidate.isNotEmpty(productPromoCoupon)) {
                GenericValue productStoreCouponAppl = productPromoCoupon.getRelatedOne("ProductStoreCouponAppl");
                String productStoreId = (String) productStoreCouponAppl.get("productStoreId");
                if (cart != null && productStoreId == null) {
                    Set<String> promoCodes = ProductPromoWorker.getStoreProductPromoCodes(cart, productStoreId);
                    if (UtilValidate.isEmpty(promoCodes) || !promoCodes.contains(productPromoCodeId)) {
                        return UtilProperties.getMessage(resource_error, "productpromoworker.promotion_code_not_valid", UtilMisc.toMap("productPromoCodeId", productPromoCodeId), locale);
                    }
                }
            }
            Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
            Timestamp thruDate = productPromoCode.getTimestamp("thruDate");
            if (thruDate != null) {
                if (nowTimestamp.after(thruDate)) {
                    return UtilProperties.getMessage(resource_error, "productpromoworker.promotion_code_is_expired_at", UtilMisc.toMap("productPromoCodeId", productPromoCodeId, "thruDate", thruDate), locale);
                }
            }
            Timestamp fromDate = productPromoCode.getTimestamp("fromDate");
            if (fromDate != null) {
                if (nowTimestamp.before(fromDate)) {
                    return UtilProperties.getMessage(resource_error, "productpromoworker.promotion_code_will_be_activated_at", UtilMisc.toMap("productPromoCodeId", productPromoCodeId, "fromDate", fromDate), locale);
                }
            }
            
            if ("Y".equals(productPromoCode.getString("requireEmailOrParty"))) {
                boolean hasEmailOrParty = false;
                
                // check partyId
                if (UtilValidate.isNotEmpty(partyId)) {
                    if (delegator.findByPrimaryKey("ProductPromoCodeParty", UtilMisc.toMap("productPromoCodeId", productPromoCodeId, "partyId", partyId)) != null) {
                        // found party associated with the code, looks good...
                        return null;
                    }
                    
                    // check email address in ProductPromoCodeEmail
                    List<EntityCondition> validEmailCondList = FastList.newInstance();
                    validEmailCondList.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
                    validEmailCondList.add(EntityCondition.makeCondition("productPromoCodeId", EntityOperator.EQUALS, productPromoCodeId));
                    validEmailCondList.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, nowTimestamp));
                    validEmailCondList.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN_EQUAL_TO, nowTimestamp),
                            EntityOperator.OR, EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null)));
                    EntityCondition validEmailCondition = EntityCondition.makeCondition(validEmailCondList, EntityOperator.AND);
                    long validEmailCount = delegator.findCountByCondition("ProductPromoCodeEmailParty", validEmailCondition, null, null);
                    if (validEmailCount > 0) {
                        // there was an email in the list, looks good...
                        return null;
                    }
                }
                
                if (!hasEmailOrParty) {
                    return UtilProperties.getMessage(resource_error, "productpromoworker.promotion_code_no_account_or_email", UtilMisc.toMap("productPromoCodeId", productPromoCodeId), locale);
                }
            }
            
            // check per customer and per promotion code use limits
            Long useLimit = getProductPromoCodeUseLimit(productPromoCode, partyId, delegator);
            if (useLimit != null && useLimit.longValue() <= 0) {
                return UtilProperties.getMessage(resource_error, "productpromoworker.promotion_code_maximum_limit", UtilMisc.toMap("productPromoCodeId", productPromoCodeId), locale);
            }
            
            return null;
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error looking up ProductPromoCode", module);
            return UtilProperties.getMessage(resource_error, "productpromoworker.promotion_code_error_lookup", UtilMisc.toMap("productPromoCodeId", productPromoCodeId, "errorMsg", e.toString()), locale);
        }
    }
    
    public static String makeAutoDescription(GenericValue productPromo, Delegator delegator, Locale locale) throws GenericEntityException {
        if (productPromo == null) {
            return "";
        }
        StringBuilder promoDescBuf = new StringBuilder();
        List<GenericValue> productPromoRules = productPromo.getRelatedCache("ProductPromoRule", null, null);
        Iterator<GenericValue> promoRulesIter = productPromoRules.iterator();
        while (promoRulesIter != null && promoRulesIter.hasNext()) {
            GenericValue productPromoRule = promoRulesIter.next();
            
            List<GenericValue> productPromoConds = delegator.findByAndCache("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromo.get("productPromoId")), UtilMisc.toList("productPromoCondSeqId"));
            productPromoConds = EntityUtil.filterByAnd(productPromoConds, UtilMisc.toMap("productPromoRuleId", productPromoRule.get("productPromoRuleId")));
            // using the other method to consolodate cache entries because the same cache is used elsewhere: List productPromoConds = productPromoRule.getRelatedCache("ProductPromoCond", null, UtilMisc.toList("productPromoCondSeqId"));
            Iterator<GenericValue> productPromoCondIter = UtilMisc.toIterator(productPromoConds);
            while (productPromoCondIter != null && productPromoCondIter.hasNext()) {
                GenericValue productPromoCond = productPromoCondIter.next();
                
                String equalityOperator = UtilProperties.getMessage("promotext", "operator.equality." + productPromoCond.getString("operatorEnumId"), locale);
                String quantityOperator = UtilProperties.getMessage("promotext", "operator.quantity." + productPromoCond.getString("operatorEnumId"), locale);
                
                String condValue = "invalid";
                if (UtilValidate.isNotEmpty(productPromoCond.getString("condValue"))) {
                    condValue = productPromoCond.getString("condValue");
                }
                
                Map<String, Object> messageContext = UtilMisc.<String, Object>toMap("condValue", condValue, "equalityOperator", equalityOperator, "quantityOperator", quantityOperator);
                String msgProp = UtilProperties.getMessage("promotext", "condition." + productPromoCond.getString("inputParamEnumId"), messageContext, locale);
                promoDescBuf.append(msgProp);
                promoDescBuf.append(" ");
                
                if (promoRulesIter.hasNext()) {
                    promoDescBuf.append(" and ");
                }
            }
            
            List<GenericValue> productPromoActions = productPromoRule.getRelatedCache("ProductPromoAction", null, UtilMisc.toList("productPromoActionSeqId"));
            Iterator<GenericValue> productPromoActionIter = UtilMisc.toIterator(productPromoActions);
            while (productPromoActionIter != null && productPromoActionIter.hasNext()) {
                GenericValue productPromoAction = productPromoActionIter.next();
                
                String productId = productPromoAction.getString("productId");
                
                Map<String, Object> messageContext = UtilMisc.<String, Object>toMap("quantity", productPromoAction.get("quantity"), "amount", productPromoAction.get("amount"), "productId", productId, "partyId", productPromoAction.get("partyId"));
                
                if (UtilValidate.isEmpty(messageContext.get("productId"))) {
                    messageContext.put("productId", "any");
                }
                if (UtilValidate.isEmpty(messageContext.get("partyId"))) {
                    messageContext.put("partyId", "any");
                }
                GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
                if (product != null) {
                    messageContext.put("productName", ProductContentWrapper.getProductContentAsText(product, "PRODUCT_NAME", locale, null));
                }
                
                String msgProp = UtilProperties.getMessage("promotext", "action." + productPromoAction.getString("productPromoActionEnumId"), messageContext, locale);
                promoDescBuf.append(msgProp);
                promoDescBuf.append(" ");
                
                if (promoRulesIter.hasNext()) {
                    promoDescBuf.append(" and ");
                }
            }
            
            if (promoRulesIter.hasNext()) {
                promoDescBuf.append(" or ");
            }
        }
        
        if (promoDescBuf.length() > 0) {
            // remove any trailing space
            if (promoDescBuf.charAt(promoDescBuf.length() - 1) == ' ') {
                promoDescBuf.deleteCharAt(promoDescBuf.length() - 1);
            }
            // add a period
            promoDescBuf.append(". ");
            // capitalize the first letter
            promoDescBuf.setCharAt(0, Character.toUpperCase(promoDescBuf.charAt(0)));
        }
        
        if ("Y".equals(productPromo.getString("requireCode"))) {
            promoDescBuf.append(UtilProperties.getMessage(resource, "OrderRequiresCodeToUse", locale));
        }
        if (productPromo.getLong("useLimitPerOrder") != null) {
            promoDescBuf.append(UtilProperties.getMessage(resource, "OrderLimitPerOrder",
                    UtilMisc.toMap("limit", productPromo.getLong("useLimitPerOrder")), locale));
        }
        if (productPromo.getLong("useLimitPerCustomer") != null) {
            promoDescBuf.append(UtilProperties.getMessage(resource, "OrderLimitPerCustomer",
                    UtilMisc.toMap("limit", productPromo.getLong("useLimitPerCustomer")), locale));
        }
        if (productPromo.getLong("useLimitPerPromotion") != null) {
            promoDescBuf.append(UtilProperties.getMessage(resource, "OrderLimitPerPromotion",
                    UtilMisc.toMap("limit", productPromo.getLong("useLimitPerPromotion")), locale));
        }
        
        return promoDescBuf.toString();
    }
    
    /**
     * 执行当前促销活动的rule规则
     *
     * @param cart
     * @param cartChanged
     * @param useLimit
     * @param requireCode
     * @param productPromoCodeId
     * @param codeUseLimit
     * @param maxUseLimit
     * @param productPromo
     * @param productPromoRules
     * @param dispatcher
     * @param delegator
     * @param nowTimestamp
     * @return
     * @throws GenericEntityException
     * @throws UseLimitException
     */
    protected static boolean runProductPromoRules(ShoppingCart cart, boolean cartChanged, Long useLimit, boolean requireCode, String productPromoCodeId, Long codeUseLimit, long maxUseLimit, GenericValue productPromo, List<GenericValue> productPromoRules, LocalDispatcher dispatcher, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException, UseLimitException {
        String productPromoId = productPromo.getString("productPromoId");
        List<GenericValue> productStorePromoAppls = productPromo.getRelated("ProductStorePromoAppl");
        if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
            GenericValue productStorePromoAppl = productStorePromoAppls.get(0);
            String productStoreId = (String) productStorePromoAppl.get("productStoreId");
            boolean promoUsed = false;
            BigDecimal totalDiscountAmount = BigDecimal.ZERO;
            BigDecimal quantityLeftInActions = BigDecimal.ZERO;
            Iterator<GenericValue> promoRulesIter = productPromoRules.iterator();
            //当前促销的所有规则rule执行
            List<ActionResultInfo> actionResultInfos = FastList.newInstance();
            while (promoRulesIter != null && promoRulesIter.hasNext()) {
                GenericValue productPromoRule = promoRulesIter.next();
                // if apply then performActions when no conditions are false, so default to true
                boolean performActions = true;
                // loop through conditions for rule, if any false, set allConditionsTrue to false
                List<GenericValue> productPromoConds = delegator.findByAndCache("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromo.get("productPromoId")), UtilMisc.toList("productPromoCondSeqId"));
                productPromoConds = EntityUtil.filterByAnd(productPromoConds, UtilMisc.toMap("productPromoRuleId", productPromoRule.get("productPromoRuleId")));
                
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Checking " + productPromoConds.size() + " conditions for rule " + productPromoRule, module);
                }
                Iterator<GenericValue> productPromoCondIter = UtilMisc.toIterator(productPromoConds);
                /*循环当前rule对应的cond，如果有一个不满足条件退出*/
                while (productPromoCondIter != null && productPromoCondIter.hasNext()) {
                    GenericValue productPromoCond = productPromoCondIter.next();
                    boolean condResult = checkCondition(productPromoCond, productStoreId, cart, delegator, dispatcher, nowTimestamp);
                    // any false condition will cause it to NOT perform the action
                    if (condResult == false) {
                        performActions = false;
                        break;
                    }
                }
                
                if (performActions) {
                    // perform all actions, either apply or unapply
                    List<GenericValue> productPromoActions = productPromoRule.getRelatedCache("ProductPromoAction", null, UtilMisc.toList("productPromoActionSeqId"));
                    Iterator<GenericValue> productPromoActionIter = UtilMisc.toIterator(productPromoActions);
                    while (productPromoActionIter != null && productPromoActionIter.hasNext()) {
                        GenericValue productPromoAction = productPromoActionIter.next();
                        try {
                            ActionResultInfo actionResultInfo = performAction(productPromoAction, productStoreId, cart, delegator, dispatcher, nowTimestamp, productPromo.getString("promoType"));
                            actionResultInfos.add(actionResultInfo);
                            
                        } catch (CartItemModifyException e) {
                            Debug.logError(e, "Error modifying the cart while performing promotion action [" + productPromoAction.getPrimaryKey() + "]", module);
                        }
                    }
                }
            }
            //记录当前促销的优惠信息
            //多级满减、多级折扣 的情况使用 最多的一次
            /*如果是多级满减Type:PROMO_REDUCE,或者 PROMO_DISCOUNT 取优惠劵最多一条*/
            if (UtilValidate.isNotEmpty(actionResultInfos)) {
                String promoType = productPromo.getString("promoType");
                if ("PROMO_REDUCE".equals(promoType) || "PROMO_DISCOUNT".equals(promoType)) {
                    Collections.sort(actionResultInfos, new Comparator<ActionResultInfo>() {
                        @Override
                        public int compare(ActionResultInfo o1, ActionResultInfo o2) {
                            return o1.totalDiscountAmount.compareTo(o2.totalDiscountAmount.negate());
                        }
                    });
                    totalDiscountAmount = totalDiscountAmount.add(actionResultInfos.get(0).totalDiscountAmount);
                    boolean actionChangedCart = actionResultInfos.get(0).ranAction;
                    GenericValue action = actionResultInfos.get(0).action;
                    List<ShoppingCartItem> items = actionResultInfos.get(0).actionCartItems;
                    /*比较设置当前促销cartItem为isPromo，并且每个Item的优惠金额*/
                    List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
                    Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                    while (lineOrderedByBasePriceIter.hasNext()) {
                        ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                        for (int i = 0; i < items.size(); i++) {
                            ShoppingCartItem item = items.get(i);
                            if (cartItem.getProductId().equals(item.getProductId())) {
                                cartItem.setRecurringBasePrice(item.getRecurringBasePrice());
                                cartItem.setIsPromo(true);
                                doOrderItemPromoAction(action, cartItem, item.getRecurringBasePrice().negate(), "amount", delegator, productStoreId, promoType);
                            }
                        }
                    }
                    /*设置订单的促销优惠信息*/
//                    doOrderPromoAction(action, cart, totalDiscountAmount, "amount", delegator, productStoreId, promoType);
                    if (actionResultInfos.size() > 1) {
                        for (int i = 1; i < actionResultInfos.size(); i++) {
                            ActionResultInfo actionResultInfo = actionResultInfos.get(i);
                            cart.removeAdjustmentByPromoAction(actionResultInfo.action.getString("productPromoActionSeqId"), productPromoId, actionResultInfo.ruleId);
                        }
                    }
                    //按照订单项占比计算商品分摊的adjustment
                    if (actionChangedCart) {
                        promoUsed = true;
                        cartChanged = true;
                    }
                    if (promoUsed) {
                        cart.addProductPromoUse(productPromo.getString("productPromoId"), productPromoCodeId, totalDiscountAmount, BigDecimal.ZERO);
                    } else {
                        // the promotion was not used, don't try again until we finish a full pass and come back to see the promo conditions are now satisfied based on changes to the cart
                    }
                } else {
                    for (int i = 0; i < actionResultInfos.size(); i++) {
                        ActionResultInfo actionResultInfo = actionResultInfos.get(i);
                        //按照订单项占比计算商品分摊的adjustment
                        totalDiscountAmount = totalDiscountAmount.add(actionResultInfo.totalDiscountAmount);
                        // only set if true, don't set back to false: implements OR logic (ie if ANY actions change content, redo loop)
                        boolean actionChangedCart = actionResultInfo.ranAction;
                        List<ShoppingCartItem> items = actionResultInfo.actionCartItems;
                        if (UtilValidate.isNotEmpty(items)) {
                            for (int j = 0; j < items.size(); j++) {
                                ShoppingCartItem cartItem = items.get(j);
                                cartItem.setIsPromo(true);
                            }
                        }
                        if (actionChangedCart) {
                            promoUsed = true;
                            cartChanged = true;
                        }
                        if (promoUsed) {
                            cart.addProductPromoUse(productPromo.getString("productPromoId"), productPromoCodeId, totalDiscountAmount, BigDecimal.ZERO);
                        } else {
                            // the promotion was not used, don't try again until we finish a full pass and come back to see the promo conditions are now satisfied based on changes to the cart
                        }
                    }
                }
            }
            
            
            if (cart.getProductPromoUseCount(productPromoId) > maxUseLimit) {
                throw new UseLimitException("ERROR: While calculating promotions the promotion [" + productPromoId + "] action was applied more than " + maxUseLimit + " times, so the calculation has been ended. This should generally never happen unless you have bad rule definitions.");
            }
        }
        
        
        return cartChanged;
    }
    
    protected static boolean checkCondition(GenericValue productPromoCond, String productStoreId, ShoppingCart cart, Delegator delegator, LocalDispatcher dispatcher, Timestamp nowTimestamp) throws GenericEntityException {
        String condValue = productPromoCond.getString("condValue");
        String otherValue = productPromoCond.getString("otherValue");
        String inputParamEnumId = productPromoCond.getString("inputParamEnumId");
        String operatorEnumId = productPromoCond.getString("operatorEnumId");
        String shippingMethod = "";
        String carrierPartyId = "";
        if (otherValue != null && otherValue.contains("@")) {
            carrierPartyId = otherValue.substring(0, otherValue.indexOf("@"));
            shippingMethod = otherValue.substring(otherValue.indexOf("@") + 1);
            otherValue = "";
        }
        String partyId = cart.getPartyId();
        GenericValue userLogin = cart.getUserLogin();
        if (userLogin == null) {
            userLogin = cart.getAutoUserLogin();
        }
        
        if (Debug.verboseOn()) {
            Debug.logVerbose("Checking promotion condition: " + productPromoCond, module);
        }
        Integer compareBase = null;
        
        if ("PPIP_PRODUCT_AMOUNT".equals(inputParamEnumId)) {
            // for this type of promo force the operatorEnumId = PPC_EQ, effectively ignore that setting because the comparison is implied in the code
            operatorEnumId = "PPC_EQ";
            
            // this type of condition requires items involved to not be involved in any other quantity consuming cond/action, and does not pro-rate the price, just uses the base price
            BigDecimal amountNeeded = BigDecimal.ZERO;
            if (UtilValidate.isNotEmpty(condValue)) {
                amountNeeded = new BigDecimal(condValue);
            }
            
            // Debug.logInfo("Doing Amount Cond with Value: " + amountNeeded, module);
            
            Set<String> productIds = ProductPromoWorker.getPromoRuleCondProductIds(productPromoCond, delegator, nowTimestamp);
            List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
            Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
            while (amountNeeded.compareTo(BigDecimal.ZERO) > 0 && lineOrderedByBasePriceIter.hasNext()) {
                ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                // only include if it is in the productId Set for this check and if it is not a Promo (GWP) item
                GenericValue product = cartItem.getProduct();
                String parentProductId = cartItem.getParentProductId();
                boolean passedItemConds = checkConditionsForItem(productPromoCond, cart, cartItem, delegator, dispatcher, nowTimestamp);
                if (passedItemConds && !cartItem.getIsPromo() &&
                        (productIds.contains(cartItem.getProductId()) || (parentProductId != null && productIds.contains(parentProductId))) &&
                        (product == null || !"N".equals(product.getString("includeInPromotions")))) {
                    
                    BigDecimal basePrice = cartItem.getBasePrice();
                    // get a rough price, round it up to an integer
                    BigDecimal quantityNeeded = amountNeeded.divide(basePrice, generalRounding).setScale(0, BigDecimal.ROUND_CEILING);
                    
                    // reduce amount still needed to qualify for promo (amountNeeded)
                    BigDecimal quantity = cartItem.addPromoQuantityCandidateUse(quantityNeeded, productPromoCond, false);
                    // get pro-rated amount based on discount
                    amountNeeded = amountNeeded.subtract(quantity.multiply(basePrice));
                }
            }
            
            // Debug.logInfo("Doing Amount Cond with Value after finding applicable cart lines: " + amountNeeded, module);
            
            // if amountNeeded > 0 then the promo condition failed, so remove candidate promo uses and increment the promoQuantityUsed to restore it
            if (amountNeeded.compareTo(BigDecimal.ZERO) > 0) {
                // failed, reset the entire rule, ie including all other conditions that might have been done before
                cart.resetPromoRuleUse(productPromoCond.getString("productPromoId"), productPromoCond.getString("productPromoRuleId"));
                compareBase = Integer.valueOf(-1);
            } else {
                // we got it, the conditions are in place...
                compareBase = Integer.valueOf(0);
                // NOTE: don't confirm promo rule use here, wait until actions are complete for the rule to do that
            }
        } else if ("PPIP_PRODUCT_TOTAL".equals(inputParamEnumId)) {
            // this type of condition allows items involved to be involved in other quantity consuming cond/action, and does pro-rate the price
            if (UtilValidate.isNotEmpty(condValue)) {
                BigDecimal amountNeeded = new BigDecimal(condValue);
                BigDecimal amountAvailable = BigDecimal.ZERO;
                
                //获取action中productIds
                Set<String> productIds = ProductPromoWorker.getPromoProductIdsOfAction(productPromoCond, delegator, nowTimestamp);
                List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
                for (ShoppingCartItem cartItem : lineOrderedByBasePriceList) {
                    // only include if it is in the productId Set for this check and if it is not a Promo (GWP) item
                    GenericValue product = cartItem.getProduct();
                    String parentProductId = cartItem.getParentProductId();
                    boolean passedItemConds = checkConditionsForItem(productPromoCond, cart, cartItem, delegator, dispatcher, nowTimestamp);
                    if (passedItemConds && !cartItem.getIsPromo() &&
                            (productIds.contains(cartItem.getProductId()) || (parentProductId != null && productIds.contains(parentProductId))) &&
                            (product == null || !"N".equals(product.getString("includeInPromotions")))) {
                        
                        // just count the entire sub-total of the item
                        amountAvailable = amountAvailable.add(cartItem.getQuantity());
                    }
                }
                
                // Debug.logInfo("Doing Amount Not Counted Cond with Value after finding applicable cart lines: " + amountNeeded, module);
                
                compareBase = Integer.valueOf(amountAvailable.compareTo(amountNeeded));
            }
        } else if ("PPIP_PRODUCT_QUANT".equals(inputParamEnumId)) {
            // for this type of promo force the operatorEnumId = PPC_EQ, effectively ignore that setting because the comparison is implied in the code
            operatorEnumId = "PPC_EQ";
            
            BigDecimal quantityNeeded = BigDecimal.ONE;
            if (UtilValidate.isNotEmpty(condValue)) {
                quantityNeeded = new BigDecimal(condValue);
            }
            
            Set<String> productIds = ProductPromoWorker.getPromoRuleCondProductIds(productPromoCond, delegator, nowTimestamp);
            List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
            BigDecimal totalCount = BigDecimal.ZERO;
            Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
            //判断商品数量是否大于等于设置数量
            while (lineOrderedByBasePriceIter.hasNext()) {
                ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                if ( !cartItem.getIsPromo() && (productIds.contains(cartItem.getProductId()) )) {
                    totalCount = totalCount.add(cartItem.getQuantity());
                }
            }
            
            if (quantityNeeded.compareTo(totalCount) > 0) {
                // failed, reset the entire rule, ie including all other conditions that might have been done before
                cart.resetPromoRuleUse(productPromoCond.getString("productPromoId"), productPromoCond.getString("productPromoRuleId"));
                compareBase = Integer.valueOf(-1);
            } else {
                // we got it, the conditions are in place...
                compareBase = Integer.valueOf(0);
                // NOTE: don't confirm rpomo rule use here, wait until actions are complete for the rule to do that
            }
        } else if ("PPIP_NEW_ACCT".equals(inputParamEnumId)) {
            if (UtilValidate.isNotEmpty(condValue)) {
                BigDecimal acctDays = cart.getPartyDaysSinceCreated(nowTimestamp);
                if (acctDays == null) {
                    // condition always fails if we don't know how many days since account created
                    return false;
                }
                compareBase = acctDays.compareTo(new BigDecimal(condValue));
            }
        } else if ("PPIP_PARTY_ID".equals(inputParamEnumId)) {
            if (partyId != null && UtilValidate.isNotEmpty(condValue)) {
                compareBase = Integer.valueOf(partyId.compareTo(condValue));
            } else {
                compareBase = Integer.valueOf(1);
            }
        } else if ("PPIP_PARTY_GRP_MEM".equals(inputParamEnumId)) {
            if (UtilValidate.isEmpty(partyId) || UtilValidate.isEmpty(condValue)) {
                compareBase = Integer.valueOf(1);
            } else {
                String groupPartyId = condValue;
                if (partyId.equals(groupPartyId)) {
                    compareBase = Integer.valueOf(0);
                } else {
                    // look for PartyRelationship with partyRelationshipTypeId=GROUP_ROLLUP, the partyIdTo is the group member, so the partyIdFrom is the groupPartyId
                    List<GenericValue> partyRelationshipList = delegator.findByAndCache("PartyRelationship", UtilMisc.toMap("partyIdFrom", groupPartyId, "partyIdTo", partyId, "partyRelationshipTypeId", "GROUP_ROLLUP"));
                    // and from/thru date within range
                    partyRelationshipList = EntityUtil.filterByDate(partyRelationshipList, true);
                    
                    if (UtilValidate.isNotEmpty(partyRelationshipList)) {
                        compareBase = Integer.valueOf(0);
                    } else {
                        compareBase = Integer.valueOf(checkConditionPartyHierarchy(delegator, nowTimestamp, groupPartyId, partyId));
                    }
                }
            }
        } else if ("PPIP_PARTY_CLASS".equals(inputParamEnumId)) {
            if (UtilValidate.isEmpty(partyId) || UtilValidate.isEmpty(condValue)) {
                compareBase = Integer.valueOf(1);
            } else {
                String partyClassificationGroupId = condValue;
                // find any PartyClassification
                List<GenericValue> partyClassificationList = delegator.findByAndCache("PartyClassification", UtilMisc.toMap("partyId", partyId, "partyClassificationGroupId", partyClassificationGroupId));
                // and from/thru date within range
                partyClassificationList = EntityUtil.filterByDate(partyClassificationList, true);
                // then 0 (equals), otherwise 1 (not equals)
                if (UtilValidate.isNotEmpty(partyClassificationList)) {
                    compareBase = Integer.valueOf(0);
                } else {
                    compareBase = Integer.valueOf(1);
                }
            }
        } else if ("PPIP_ROLE_TYPE".equals(inputParamEnumId)) {
            if (partyId != null && UtilValidate.isNotEmpty(condValue)) {
                // if a PartyRole exists for this partyId and the specified roleTypeId
                GenericValue partyRole = delegator.findByPrimaryKeyCache("PartyRole",
                        UtilMisc.toMap("partyId", partyId, "roleTypeId", condValue));
                
                // then 0 (equals), otherwise 1 (not equals)
                if (partyRole != null) {
                    compareBase = Integer.valueOf(0);
                } else {
                    compareBase = Integer.valueOf(1);
                }
            } else {
                compareBase = Integer.valueOf(1);
            }
        } else if ("PPIP_ORDER_TOTAL".equals(inputParamEnumId)) {
            if (UtilValidate.isNotEmpty(condValue)) {
                // 根据订单里面的商品计算总金额，如果没有商品默认订单金额
                BigDecimal orderSubTotal = ProductPromoWorker.getPromoRuleActionProductsTotalAmount(productPromoCond, delegator, cart, productStoreId);
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Doing order total compare: orderSubTotal=" + orderSubTotal, module);
                }
                compareBase = Integer.valueOf(orderSubTotal.compareTo(new BigDecimal(condValue)));
            }
        } else if ("PPIP_ORST_HIST".equals(inputParamEnumId)) {
            // description="Order sub-total X in last Y Months"
            if (partyId != null && userLogin != null && UtilValidate.isNotEmpty(condValue)) {
                // call the getOrderedSummaryInformation service to get the sub-total
                int monthsToInclude = 12;
                if (otherValue != null) {
                    monthsToInclude = Integer.parseInt(otherValue);
                }
                Map<String, Object> serviceIn = UtilMisc.<String, Object>toMap("partyId", partyId, "roleTypeId", "PLACING_CUSTOMER", "orderTypeId", "SALES_ORDER", "statusId", "ORDER_COMPLETED", "monthsToInclude", Integer.valueOf(monthsToInclude), "userLogin", userLogin);
                try {
                    Map<String, Object> result = dispatcher.runSync("getOrderedSummaryInformation", serviceIn);
                    if (ServiceUtil.isError(result)) {
                        Debug.logError("Error calling getOrderedSummaryInformation service for the PPIP_ORST_HIST ProductPromo condition input value: " + ServiceUtil.getErrorMessage(result), module);
                        return false;
                    } else {
                        BigDecimal orderSubTotal = (BigDecimal) result.get("totalSubRemainingAmount");
                        BigDecimal orderSubTotalAndCartSubTotal = orderSubTotal.add(cart.getSubTotal());
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("Doing order history sub-total compare: orderSubTotal=" + orderSubTotal + ", for the last " + monthsToInclude + " months.", module);
                        }
                        compareBase = Integer.valueOf(orderSubTotalAndCartSubTotal.compareTo(new BigDecimal(condValue)));
                    }
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Error getting order history sub-total in the getOrderedSummaryInformation service, evaluating condition to false.", module);
                    return false;
                }
            } else {
                return false;
            }
        } else if ("PPIP_CUST_ORDER_NUM".equals(inputParamEnumId)) {
            //用户订单数量
            if (partyId != null && userLogin != null && UtilValidate.isNotEmpty(condValue)) {
                Map<String, Object> serviceIn = UtilMisc.<String, Object>toMap("partyId", partyId, "roleTypeId", "PLACING_CUSTOMER", "orderTypeId", "SALES_ORDER", "userLogin", userLogin);
                try {
                    Map<String, Object> result = dispatcher.runSync("queryCustOrderedSummaryInfo", serviceIn);
                    if (ServiceUtil.isError(result)) {
                        Debug.logError("Error calling queryCustOrderedSummaryInfo service for the PPIP_CUST_ORDER_NUM ProductPromo condition input value: " + ServiceUtil.getErrorMessage(result), module);
                        return false;
                    } else {
                        Long orderNum = (Long) result.get("totalOrders");
                        if (Debug.verboseOn()) {
                            Debug.logVerbose("Doing order num compare: orderNum=" + orderNum, module);
                        }
                        compareBase = Integer.valueOf(orderNum.compareTo(new Long(condValue)));
                    }
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Error getting order history sub-total in the getOrderedSummaryInformation service, evaluating condition to false.", module);
                    return false;
                }
            } else {
                return false;
            }
        } else if ("PPIP_ORDER_SHIPTOTAL".equals(inputParamEnumId) && shippingMethod.equals(cart.getShipmentMethodTypeId()) && carrierPartyId.equals(cart.getCarrierPartyId())) {
            if (UtilValidate.isNotEmpty(condValue)) {
                BigDecimal orderTotalShipping = cart.getTotalShipping();
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Doing order total Shipping compare: ordertotalShipping=" + orderTotalShipping, module);
                }
                compareBase = orderTotalShipping.compareTo(new BigDecimal(condValue));
            }
        } else if ("PPIP_LPMUP_AMT".equals(inputParamEnumId)) {
            // does nothing on order level, only checked on item level, so ignore by always considering passed
            return true;
        } else if ("PPIP_LPMUP_PER".equals(inputParamEnumId)) {
            // does nothing on order level, only checked on item level, so ignore by always considering passed
            return true;
        }
        //团购（判断当有效订单数是否达到设定值）
        else if ("PPIP_GRPODR_TOTAL".equals(inputParamEnumId)) {
//            compareBase = Integer.valueOf(-1);
//            BigDecimal orderSubTotal = BigDecimal.ZERO;
//            if(UtilValidate.isNotEmpty(condValue)){
//                Set<String> productIds = ProductPromoWorker.getPromoRuleCondProductIds(productPromoCond, delegator, nowTimestamp);
//                if(UtilValidate.isNotEmpty(productIds)){
//                    //本次团购的产品唯一
//                    String productId = (String) productIds.toArray()[0];
//                    //找到参加本次团购的所有有效订单数量
//                    String groupOrderId = "0";  //团购号
//                    String productPromoId = (String) productPromoCond.get("productPromoId");
//                    List<GenericValue> groupOrderRules = delegator.findByAnd("ProductGroupOrderRule",UtilMisc.toMap("productPromoId",productPromoId));
//                    if(UtilValidate.isNotEmpty(groupOrderRules)){
//                        GenericValue groupOrderRule = groupOrderRules.get(0);
//                        groupOrderId = (String)groupOrderRule.get("activityId");
//                    }
//
//                    try{
//                    Map<String,Object> result = dispatcher.runSync("getAvailableGroupOrderQuantity",UtilMisc.toMap("productId",productId,"activityId",groupOrderId,"partyId",cart.getPartyId(),"userLogin",userLogin));
//                    orderSubTotal = (BigDecimal) result.get("quantities");
//                    if (ServiceUtil.isError(result)) {
//                        Debug.logError("Error calling getGroupOrderQuantities service for the PPIP_GLOBAL_ORDER_TOTAL ProductPromo condition input value: " + ServiceUtil.getErrorMessage(result), module);
//                        return false;
//                    }
//                } catch (GenericServiceException e) {
//                    Debug.logError(e, "Error getting order history sub-total in the getOrderedSummaryInformation service, evaluating condition to false.", module);
//                    return false;
//                }
//                }
//                orderSubTotal =  orderSubTotal.add(cart.getSubTotal());
//            }
//            compareBase = Integer.valueOf(orderSubTotal.compareTo(new BigDecimal(condValue)));
            compareBase = 1;
        }
        //秒杀
        else {
            Debug.logWarning(UtilProperties.getMessage(resource_error, "OrderAnUnSupportedProductPromoCondInputParameterLhs", UtilMisc.toMap("inputParamEnumId", productPromoCond.getString("inputParamEnumId")), cart.getLocale()), module);
            return false;
        }
        
        if (Debug.verboseOn()) {
            Debug.logVerbose("Condition compare done, compareBase=" + compareBase, module);
        }
        
        if (compareBase != null) {
            int compare = compareBase.intValue();
            if ("PPC_EQ".equals(operatorEnumId)) {
                if (compare == 0) {
                    return true;
                }
            } else if ("PPC_NEQ".equals(operatorEnumId)) {
                if (compare != 0) {
                    return true;
                }
            } else if ("PPC_LT".equals(operatorEnumId)) {
                if (compare < 0) {
                    return true;
                }
            } else if ("PPC_LTE".equals(operatorEnumId)) {
                if (compare <= 0) {
                    return true;
                }
            } else if ("PPC_GT".equals(operatorEnumId)) {
                if (compare > 0) {
                    return true;
                }
            } else if ("PPC_GTE".equals(operatorEnumId)) {
                if (compare >= 0) {
                    return true;
                }
            } else {
                Debug.logWarning(UtilProperties.getMessage(resource_error, "OrderAnUnSupportedProductPromoCondCondition", UtilMisc.toMap("operatorEnumId", operatorEnumId), cart.getLocale()), module);
                return false;
            }
        }
        // default to not meeting the condition
        return false;
    }
    
    /**
     * 获取促销条件下执行动作的商品的总金额，如果没商品取店铺总的金额
     *
     * @param productPromoCond
     * @param delegator
     * @return
     */
    private static BigDecimal getPromoRuleActionProductsTotalAmount(GenericValue productPromoCond, Delegator delegator, ShoppingCart cart, String productStoreId) {
        BigDecimal toalAmount = BigDecimal.ZERO;
        if (UtilValidate.isNotEmpty(productPromoCond)) {
            try {
                GenericValue productPromo = productPromoCond.getRelatedOne("ProductPromo");
                String promoProductType = productPromo.getString("promoProductType");
                if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                    Set<String> productIds = ProductPromoWorker.getPromoRuleCondProductIds(productPromoCond, delegator, UtilDateTime.nowTimestamp());
                    List<ShoppingCartItem> items = ProductPromoWorker.findCartItemsForPromoProductIds(cart, productIds);
                    if (UtilValidate.isNotEmpty(items)) {
                        for (int i = 0; i < items.size(); i++) {
                            ShoppingCartItem cartItem = items.get(i);
                            toalAmount = toalAmount.add(cartItem.getBasePrice().multiply(cartItem.getQuantity()));
                        }
                    }
                    
                } else if ("PROMO_PRT_ALL".equals(promoProductType)) {
                    return cart.getSubTotalForPromo(productStoreId);
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return toalAmount;
    }
    
    /**
     * 根据productIds找到对应的订单项
     *
     * @param cart
     * @param productIds
     * @return
     */
    private static List<ShoppingCartItem> findCartItemsForPromoProductIds(ShoppingCart cart, Set<String> productIds) {
        List<ShoppingCartItem> cartItems = cart.items();
        List<ShoppingCartItem> newItems = FastList.newInstance();
        if (UtilValidate.isNotEmpty(cartItems)) {
            for (int i = 0; i < cartItems.size(); i++) {
                ShoppingCartItem cartItem = cartItems.get(i);
                if (UtilValidate.isNotEmpty(cartItem) && "Y".equals(cartItem.getIsChoose()) && (!cartItem.getIsPromo())) {
                    String productId = cartItem.getProductId();
                    if (productIds.contains(productId)) {
                        newItems.add(cartItem);
                    }
                }
            }
        }
        return newItems;
    }
    
    protected static boolean checkConditionsForItem(GenericValue productPromoActionOrCond, ShoppingCart cart, ShoppingCartItem cartItem, Delegator delegator, LocalDispatcher dispatcher, Timestamp nowTimestamp) throws GenericEntityException {
        GenericValue productPromoRule = productPromoActionOrCond.getRelatedOneCache("ProductPromoRule");
        
        List<GenericValue> productPromoConds = delegator.findByAndCache("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoRule.get("productPromoId")), UtilMisc.toList("productPromoCondSeqId"));
        productPromoConds = EntityUtil.filterByAnd(productPromoConds, UtilMisc.toMap("productPromoRuleId", productPromoRule.get("productPromoRuleId")));
        for (GenericValue productPromoCond : productPromoConds) {
            boolean passed = checkConditionForItem(productPromoCond, cart, cartItem, delegator, dispatcher, nowTimestamp);
            if (!passed) {
                return false;
            }
        }
        return true;
    }
    
    protected static boolean checkConditionForItem(GenericValue productPromoCond, ShoppingCart cart, ShoppingCartItem cartItem, Delegator delegator, LocalDispatcher dispatcher, Timestamp nowTimestamp) throws GenericEntityException {
        String condValue = productPromoCond.getString("condValue");
        // String otherValue = productPromoCond.getString("otherValue");
        String inputParamEnumId = productPromoCond.getString("inputParamEnumId");
        String operatorEnumId = productPromoCond.getString("operatorEnumId");
        
        // don't get list price from cart because it may have tax included whereas the base price does not: BigDecimal listPrice = cartItem.getListPrice();
        Map<String, String> priceFindMap = UtilMisc.toMap("productId", cartItem.getProductId(),
                "productPriceTypeId", "LIST_PRICE", "productPricePurposeId", "PURCHASE");
        List<GenericValue> listProductPriceList = delegator.findByAnd("ProductPrice", priceFindMap, UtilMisc.toList("-fromDate"));
        listProductPriceList = EntityUtil.filterByDate(listProductPriceList, true);
        GenericValue listProductPrice = (listProductPriceList != null && listProductPriceList.size() > 0) ? listProductPriceList.get(0) : null;
        BigDecimal listPrice = (listProductPrice != null) ? listProductPrice.getBigDecimal("price") : null;
        
        if (listPrice == null) {
            // can't find a list price so this condition is meaningless, consider it passed
            return true;
        }
        
        BigDecimal basePrice = cartItem.getBasePrice();
        BigDecimal amountOff = listPrice.subtract(basePrice);
        BigDecimal percentOff = amountOff.divide(listPrice, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100L));
        
        Integer compareBase = null;
        
        if ("PPIP_LPMUP_AMT".equals(inputParamEnumId)) {
            // NOTE: only check this after we know it's this type of cond, otherwise condValue may not be a number
            BigDecimal condValueBigDecimal = new BigDecimal(condValue);
            compareBase = Integer.valueOf(amountOff.compareTo(condValueBigDecimal));
        } else if ("PPIP_LPMUP_PER".equals(inputParamEnumId)) {
            // NOTE: only check this after we know it's this type of cond, otherwise condValue may not be a number
            BigDecimal condValueBigDecimal = new BigDecimal(condValue);
            compareBase = Integer.valueOf(percentOff.compareTo(condValueBigDecimal));
        } else {
            // condition doesn't apply to individual item, always passes
            return true;
        }
        
        Debug.logInfo("Checking condition for item productId=" + cartItem.getProductId() + ", listPrice=" + listPrice + ", basePrice=" + basePrice + ", amountOff=" + amountOff + ", percentOff=" + percentOff + ", condValue=" + condValue + ", compareBase=" + compareBase + ", productPromoCond=" + productPromoCond, module);
        
        if (compareBase != null) {
            int compare = compareBase.intValue();
            if ("PPC_EQ".equals(operatorEnumId)) {
                if (compare == 0) {
                    return true;
                }
            } else if ("PPC_NEQ".equals(operatorEnumId)) {
                if (compare != 0) {
                    return true;
                }
            } else if ("PPC_LT".equals(operatorEnumId)) {
                if (compare < 0) {
                    return true;
                }
            } else if ("PPC_LTE".equals(operatorEnumId)) {
                if (compare <= 0) {
                    return true;
                }
            } else if ("PPC_GT".equals(operatorEnumId)) {
                if (compare > 0) {
                    return true;
                }
            } else if ("PPC_GTE".equals(operatorEnumId)) {
                if (compare >= 0) {
                    return true;
                }
            } else {
                Debug.logWarning(UtilProperties.getMessage(resource_error, "OrderAnUnSupportedProductPromoCondCondition", UtilMisc.toMap("operatorEnumId", operatorEnumId), cart.getLocale()), module);
                return false;
            }
            // was a compareBase and nothing returned above, so condition didn't pass, return false
            return false;
        }
        
        // no compareBase, this condition doesn't apply
        return true;
    }
    
    private static int checkConditionPartyHierarchy(Delegator delegator, Timestamp nowTimestamp, String groupPartyId, String partyId) throws GenericEntityException {
        List<GenericValue> partyRelationshipList = delegator.findByAndCache("PartyRelationship", UtilMisc.toMap("partyIdTo", partyId, "partyRelationshipTypeId", "GROUP_ROLLUP"));
        partyRelationshipList = EntityUtil.filterByDate(partyRelationshipList, nowTimestamp, null, null, true);
        for (GenericValue genericValue : partyRelationshipList) {
            String partyIdFrom = (String) genericValue.get("partyIdFrom");
            if (partyIdFrom.equals(groupPartyId)) {
                return 0;
            }
            if (0 == checkConditionPartyHierarchy(delegator, nowTimestamp, groupPartyId, partyIdFrom)) {
                return 0;
            }
        }
        return 1;
    }
    
    public static class ActionResultInfo {
        public boolean ranAction = false;
        public BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        public BigDecimal quantityLeftInAction = BigDecimal.ZERO;
        public GenericValue action = null;
        public String ruleId = null;
        public List<ShoppingCartItem> actionCartItems = FastList.newInstance();
    }
    
    /**
     * returns true if the cart was changed and rules need to be re-evaluted
     */
    protected static ActionResultInfo performAction(GenericValue productPromoAction, String productStoreId, ShoppingCart cart, Delegator delegator, LocalDispatcher dispatcher, Timestamp nowTimestamp, String promoType) throws GenericEntityException, CartItemModifyException {
        ActionResultInfo actionResultInfo = new ActionResultInfo();
        performAction(actionResultInfo, productStoreId, productPromoAction, cart, delegator, dispatcher, nowTimestamp, promoType);
        return actionResultInfo;
    }
    
    public static void performAction(ActionResultInfo actionResultInfo, String productStoreId, GenericValue productPromoAction, ShoppingCart cart, Delegator delegator, LocalDispatcher dispatcher, Timestamp nowTimestamp, String promoType) throws GenericEntityException, CartItemModifyException {
        
        String productPromoActionEnumId = productPromoAction.getString("productPromoActionEnumId");
        
        if ("PROMO_SERVICE".equals(productPromoActionEnumId)) {
            Map<String, Object> serviceCtx = UtilMisc.<String, Object>toMap("productPromoAction", productPromoAction, "shoppingCart", cart, "nowTimestamp", nowTimestamp, "actionResultInfo", actionResultInfo);
            String serviceName = productPromoAction.getString("serviceName");
            Map<String, Object> actionResult;
            try {
                actionResult = dispatcher.runSync(serviceName, serviceCtx);
            } catch (GenericServiceException e) {
                Debug.logError("Error calling promo action service [" + serviceName + "]", module);
                throw new CartItemModifyException("Error calling promo action service [" + serviceName + "]", e);
            }
            if (ServiceUtil.isError(actionResult)) {
                Debug.logError("Error calling promo action service [" + serviceName + "], result is: " + actionResult, module);
                throw new CartItemModifyException((String) actionResult.get(ModelService.ERROR_MESSAGE));
            }
            CartItemModifyException cartItemModifyException = (CartItemModifyException) actionResult.get("cartItemModifyException");
            if (cartItemModifyException != null) {
                throw cartItemModifyException;
            }
        } else if ("PROMO_GWP".equals(productPromoActionEnumId)) {
            // the code was in there for this, so even though I don't think we want to restrict this, just adding this flag to make it easy to change; could make option dynamic, but now implied by the use limit
            boolean allowMultipleGwp = true;
            GenericValue productPromo = productPromoAction.getRelatedOne("ProductPromo");
            String promoProductType = productPromo.getString("promoProductType");
            Integer itemLoc = findPromoItem(productPromoAction, cart);
            if (!allowMultipleGwp && itemLoc != null) {
                if (Debug.verboseOn()) {
                    Debug.logVerbose("Not adding promo item, already there; action: " + productPromoAction, module);
                }
                actionResultInfo.ranAction = false;
            } else {
                BigDecimal quantity;
                if (productPromoAction.get("quantity") != null) {
                    quantity = productPromoAction.getBigDecimal("quantity");
                } else {
                    if ("Y".equals(productPromoAction.get("useCartQuantity"))) {
                        quantity = BigDecimal.ZERO;
                        List<ShoppingCartItem> used = getCartItemsUsed(cart, productPromoAction);
                        for (ShoppingCartItem item : used) {
                            BigDecimal available = item.getPromoQuantityAvailable();
                            quantity = quantity.add(available).add(item.getPromoQuantityCandidateUseActionAndAllConds(productPromoAction));
                            item.addPromoQuantityCandidateUse(available, productPromoAction, false);
                        }
                    } else {
                        quantity = BigDecimal.ZERO;
                    }
                }
                
                List<String> optionProductIds = FastList.newInstance();
                //赠送商品
                String productId = productPromoAction.getString("productId");
                BigDecimal quantityAlreadyInCart = BigDecimal.ZERO;
                GenericValue product = null;
                if (UtilValidate.isNotEmpty(productId)) {
                    product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
                    if (product == null) {
                        String errMsg = "GWP Product not found with ID [" + productId + "] for ProductPromoAction [" + productPromoAction.get("productPromoId") + ":" + productPromoAction.get("productPromoRuleId") + ":" + productPromoAction.get("productPromoActionSeqId") + "]";
                        Debug.logError(errMsg, module);
                        throw new CartItemModifyException(errMsg);
                    }
                    
                    if (cart != null) {
                        List<ShoppingCartItem> matchingItems = cart.findAllCartItems(productId);
                        for (ShoppingCartItem item : matchingItems) {
                            if (UtilValidate.isNotEmpty(item.getIsChoose()) && "Y".equals(item.getIsChoose())) {
                                quantityAlreadyInCart = quantityAlreadyInCart.add(item.getQuantity());
                            }
                        }
                    }
                    
                }
                // support multiple gift options if products are attached to the action, or if the productId on the action is a virtual product
                boolean condTrue = false;
                Set<String> productIds = ProductPromoWorker.getPromoRuleActionProductIds(productPromoAction, delegator, nowTimestamp);
                //在有满足条件的商品情况下
                List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
                
                //cond条件中已经满足
                
                if (UtilValidate.isNotEmpty(product) || ((UtilValidate.isNotEmpty(promoProductType) && "PROMO_PRT_ALL".equals(promoProductType)))) {
                    // pass null for cartLocation to add to end of cart, pass false for doPromotions to avoid infinite recursion
                    ShoppingCartItem gwpItem = null;
                    try {
                        // just leave the prodCatalogId null, this line won't be associated with a catalog
                        String prodCatalogId = null;
                        gwpItem = ShoppingCartItem.makeItem(null, product, null, quantity, null, null, null, null, null, null, null, UtilMisc.toMap("isGift", "Y"), prodCatalogId, null, null, null, dispatcher, cart, Boolean.FALSE, Boolean.FALSE, null, Boolean.FALSE, Boolean.FALSE, "Y");
                        if (optionProductIds.size() > 0) {
                            gwpItem.setAlternativeOptionProductIds(optionProductIds);
                        } else {
                            gwpItem.setAlternativeOptionProductIds(null);
                        }
                    } catch (CartItemModifyException e) {
                        int gwpItemIndex = cart.getItemIndex(gwpItem);
                        cart.removeCartItem(gwpItemIndex, dispatcher);
                        throw e;
                    }
                    //设置条件商品item 为isPromo
                    List<ShoppingCartItem> items = FastList.newInstance();
                    lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
                    if (UtilValidate.isNotEmpty(promoProductType) && "PROMO_PRT_ALL".equals(promoProductType)) {
                        items = lineOrderedByBasePriceList;
                        
                    } else {
                        Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                        while (lineOrderedByBasePriceIter.hasNext()) {
                            ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                            if (productIds.contains(cartItem.getProductId())) {
                                items.add(cartItem);
                            }
                        }
                    }

//                    BigDecimal discountAmount = quantity.multiply(gwpItem.getBasePrice()).negate();
                    BigDecimal discountAmount = BigDecimal.ZERO;
                    //满赠是属于订单
                    doOrderPromoAction(productPromoAction, cart, discountAmount, "amount", delegator, productStoreId, promoType, product.getString("productId"));
                    // set promo after create; note that to setQuantity we must clear this flag, setQuantity, then re-set the flag
                    gwpItem.setIsPromo(true);
                    if (Debug.verboseOn()) {
                        Debug.logVerbose("gwpItem adjustments: " + gwpItem.getAdjustments(), module);
                    }
                    
                    actionResultInfo.ranAction = true;
                    actionResultInfo.totalDiscountAmount = discountAmount;
                    actionResultInfo.actionCartItems = items;
                }
                
            }
        } else if ("PROMO_FREE_SHIPPING".equals(productPromoActionEnumId) || "PROMO_SHIP_CHARGE".equals(productPromoActionEnumId)) {
            // this may look a bit funny: on each pass all rules that do free shipping will set their own rule id for it,
            // and on unapply if the promo and rule ids are the same then it will clear it; essentially on any pass
            // through the promos and rules if any free shipping should be there, it will be there
            cart.addFreeShippingProductPromoAction(productPromoAction);
            //运费设置为0
            ShoppingCart.CartShipInfo shipInfo = cart.getShipGroupByProductStoreId(productStoreId);
            shipInfo.setShipEstimate(BigDecimal.ZERO);
            /*List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
            Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
            while (lineOrderedByBasePriceIter.hasNext()) {
                ShoppingCartItem item = lineOrderedByBasePriceIter.next();
                if(!item.getIsPromo()) {
                    //没有使用促销的商品包邮
                    item.setFreeShip(true);
                    
                }
            }*/
            
            // don't consider this as a cart change?
            actionResultInfo.ranAction = true;
            // should probably set the totalDiscountAmount to something, but we have no idea what it will be, so leave at 0, will still get run
        } else if ("PROMO_PROD_DISC".equals(productPromoActionEnumId)) {
            //折扣使用
            GenericValue productPromo = productPromoAction.getRelatedOne("ProductPromo");
            String promoProductType = productPromo.getString("promoProductType");
            
            Set<String> productIds = ProductPromoWorker.getPromoRuleActionProductIds(productPromoAction, delegator, nowTimestamp);
            List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
            
            GenericValue productPromoCond = productPromoAction.getRelatedOne("ProductPromoCond");
            BigDecimal orderSubTotal = ProductPromoWorker.getPromoRuleActionProductsTotalAmount(productPromoCond, delegator, cart, productStoreId);
            
            Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
            List<ShoppingCartItem> cartItems = FastList.newInstance();
            if (UtilValidate.isNotEmpty(promoProductType) && "PROMO_PRT_ALL".equals(promoProductType)) {
                //如果没有指定商品则购物车中所有商品折扣
                
                BigDecimal discountAmountTotal = BigDecimal.ZERO;
                //折扣比
                BigDecimal percentModifier = productPromoAction.get("amount") == null ? BigDecimal.ZERO : productPromoAction.getBigDecimal("amount").movePointLeft(1);
                //获取订单项的总的金额
                BigDecimal discountAmount = orderSubTotal.subtract(orderSubTotal.multiply(percentModifier));
                discountAmountTotal = discountAmountTotal.add(discountAmount);
                while (lineOrderedByBasePriceIter.hasNext()) {
                    ShoppingCartItem item = lineOrderedByBasePriceIter.next();
                    BigDecimal cartItemSubTotal = item.getBasePrice().multiply(item.getQuantity());
                    BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(discountAmountTotal).setScale(2, RoundingMode.HALF_UP);
                    item.setRecurringBasePrice(cartItemRealPrice);
                    cartItems.add(item);
                    
                }
                
                actionResultInfo.ranAction = true;
                actionResultInfo.totalDiscountAmount = discountAmountTotal.negate();
                actionResultInfo.action = productPromoAction;
                actionResultInfo.actionCartItems = cartItems;
                actionResultInfo.ruleId = productPromoAction.getString("productPromoRuleId");
                
                
            } else {
                
                BigDecimal discountAmountTotal = BigDecimal.ZERO;
                //折扣比
                BigDecimal percentModifier = productPromoAction.get("amount") == null ? BigDecimal.ZERO : productPromoAction.getBigDecimal("amount").movePointLeft(1);
                //获取订单项的总的金额
                BigDecimal discountAmount = orderSubTotal.subtract(orderSubTotal.multiply(percentModifier));
                discountAmountTotal = discountAmountTotal.add(discountAmount);
                
                while (lineOrderedByBasePriceIter.hasNext()) {
                    ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                    if (!cartItem.getIsPromo() && (productIds.contains(cartItem.getProductId()))) {
                        
                        BigDecimal cartItemSubTotal = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                        BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(discountAmountTotal).setScale(2, RoundingMode.HALF_UP);
                        cartItem.setRecurringBasePrice(cartItemRealPrice);
                        cartItems.add(cartItem);
                    }
                }
                
                //
                actionResultInfo.ranAction = true;
                actionResultInfo.totalDiscountAmount = discountAmountTotal.negate();
                actionResultInfo.action = productPromoAction;
                actionResultInfo.actionCartItems = cartItems;
                actionResultInfo.ruleId = productPromoAction.getString("productPromoRuleId");
            }
        } else if ("PROMO_PRO_PER_AMDISC".equals(productPromoActionEnumId)) {
            //每满减：如果是PROMO_PRT_ALL 则折扣应用在每个商品，如果是指定商品根据商品占比打折
            BigDecimal discountAmountTotal = BigDecimal.ZERO;
            List<ShoppingCartItem> cartItems = FastList.newInstance();
            Set<String> productIds = ProductPromoWorker.getPromoRuleActionProductIds(productPromoAction, delegator, nowTimestamp);
            GenericValue productPromo = productPromoAction.getRelatedOne("ProductPromo");
            String promoProductType = productPromo.getString("promoProductType");
            List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
            GenericValue productPromoCond = productPromoAction.getRelatedOne("ProductPromoCond");
            BigDecimal orderSubTotal = ProductPromoWorker.getPromoRuleActionProductsTotalAmount(productPromoCond, delegator, cart, productStoreId);
            
            // create an adjustment and add it to the cartItem that implements the promotion action
            BigDecimal discount = productPromoAction.get("amount") == null ? BigDecimal.ZERO : productPromoAction.getBigDecimal("amount");
            //每满减代表对整个订单产品满足条件的做满减
            
            GenericValue cond = productPromoAction.getRelatedOne("ProductPromoCond");
            String condValue = cond.getString("condValue");
            //需要HALF_DOWN
            BigDecimal amount = orderSubTotal.divide(new BigDecimal(condValue), 0, RoundingMode.DOWN);
            
            discount = discount.multiply(amount);
            BigDecimal discountAmount = (discount).negate();
            discountAmountTotal = discountAmountTotal.add(discountAmount);
//            doOrderPromoAction(productPromoAction, cart, discountAmountTotal, "amount", delegator, productStoreId, promoType);
            if (UtilValidate.isNotEmpty(promoProductType) && "PROMO_PRT_ALL".equals(promoProductType)) {
                Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                while (lineOrderedByBasePriceIter.hasNext()) {
                    ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                    if(!cartItem.getIsPromo()) {
                        BigDecimal cartItemSubTotal = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                        BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(discount).setScale(2, RoundingMode.HALF_UP);
                        cartItem.setRecurringBasePrice(cartItemRealPrice);
                        cartItem.setIsPromo(true);
    
                        doOrderItemPromoAction(productPromoAction, cartItem, cartItemRealPrice.negate(), "amount", delegator, productStoreId, promoType);
                    }
                }
                
            } else {
                Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                while (lineOrderedByBasePriceIter.hasNext()) {
                    ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                    if (!cartItem.getIsPromo() && (productIds.contains(cartItem.getProductId()))) {
                        BigDecimal cartItemSubTotal = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                        BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(discount).setScale(2, RoundingMode.HALF_UP);
                        cartItem.setIsPromo(true);
                        cartItem.setRecurringBasePrice(cartItemRealPrice);
                        doOrderItemPromoAction(productPromoAction, cartItem, cartItemRealPrice.negate(), "amount", delegator, productStoreId, promoType);
                    }
                    
                }
            }
            
            actionResultInfo.ranAction = true;
            actionResultInfo.totalDiscountAmount = discountAmountTotal;
            actionResultInfo.actionCartItems = cartItems;
            actionResultInfo.action = productPromoAction;
            actionResultInfo.ruleId = productPromoAction.getString("productPromoRuleId");
            
        } else if ("PROMO_PROD_AMDISC".equals(productPromoActionEnumId)) {
            //多级满减中的一次
            BigDecimal discountAmountTotal = BigDecimal.ZERO;
            BigDecimal discount = productPromoAction.get("amount") == null ? BigDecimal.ZERO : productPromoAction.getBigDecimal("amount");
            discountAmountTotal = discount.negate();
            GenericValue productPromo = productPromoAction.getRelatedOne("ProductPromo");
            String promoProductType = productPromo.getString("promoProductType");
            List<ShoppingCartItem> cartItems = FastList.newInstance();
            List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
            
            GenericValue productPromoCond = productPromoAction.getRelatedOne("ProductPromoCond");
            BigDecimal orderSubTotal = ProductPromoWorker.getPromoRuleActionProductsTotalAmount(productPromoCond, delegator, cart, productStoreId);
            
            if (UtilValidate.isNotEmpty(promoProductType) && "PROMO_PRT_ALL".equals(promoProductType)) {
                Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                while (lineOrderedByBasePriceIter.hasNext()) {
                    //多级满减不设置cartItem.setIsPromo
                    ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                    if(!cartItem.getIsPromo()) {
                        BigDecimal cartItemSubTotal = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                        BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(discount).setScale(2, RoundingMode.HALF_UP);
                        cartItem.setRecurringBasePrice(cartItemRealPrice);
                        cartItems.add(cartItem);
                    }
                }
                
            } else {
                //如果是多个商品参与的满减，需要分摊到每个item下
                
                Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                Set<String> productIds = ProductPromoWorker.getPromoRuleActionProductIds(productPromoAction, delegator, nowTimestamp);
                while (lineOrderedByBasePriceIter.hasNext()) {
                    ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                    if (!cartItem.getIsPromo() && (productIds.contains(cartItem.getProductId()))) {
                        //多级满减不设置cartItem.setIsPromo¶
                        cartItems.add(cartItem);
                        BigDecimal cartItemSubTotal = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                        BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(discount).setScale(2, RoundingMode.HALF_UP);
                        cartItem.setRecurringBasePrice(cartItemRealPrice);
                    }
                }
            }
            
            //总的优惠
            actionResultInfo.ranAction = true;
            actionResultInfo.totalDiscountAmount = discountAmountTotal;
            actionResultInfo.action = productPromoAction;
            actionResultInfo.actionCartItems = cartItems;
            
            
        } else if ("PROMO_PROD_PRICE".equals(productPromoActionEnumId)) {
            // with this we want the set of used items to be one price, so total the price for all used items, subtract the amount we want them to cost, and create an adjustment for what is left
            
            BigDecimal desiredAmount = productPromoAction.get("amount") == null ? BigDecimal.ZERO : productPromoAction.getBigDecimal("amount");
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            Set<String> productIds = ProductPromoWorker.getPromoProductOfAction(productPromoAction, delegator, nowTimestamp);
            List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
            Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
            
            while (lineOrderedByBasePriceIter.hasNext()) {
                ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                // only include if it is in the productId Set for this check and if it is not a Promo (GWP) item
                boolean passedItemConds = checkConditionsForItem(productPromoAction, cart, cartItem, delegator, dispatcher, nowTimestamp);
                if (passedItemConds && !cartItem.getIsPromo() && (productIds.contains(cartItem.getProductId()))) {
                    List<GenericValue> prices = delegator.findByAnd("ProductPrice",UtilMisc.toMap("productId",cartItem.getProductId()));
                    BigDecimal basePrice = BigDecimal.ZERO;
                    if(UtilValidate.isNotEmpty(prices)){
                        for (int i = 0; i < prices.size(); i++) {
                            GenericValue price = prices.get(i);
                            if("DEFAULT_PRICE".equals(price.getString("productPriceTypeId"))){
                                basePrice = price.getBigDecimal("price");
                            }
                        }
                    }
                    desiredAmount = (desiredAmount.multiply(cartItem.getQuantity()));
                    BigDecimal itemSubTotal = basePrice.multiply(cartItem.getQuantity());
                    BigDecimal discountAmount = itemSubTotal.subtract(desiredAmount);
                    doOrderItemPromoAction(productPromoAction, cartItem, discountAmount.negate(), "amount", delegator, productStoreId, promoType);
                    cartItem.setIsPromo(true);
                    cartItem.setRecurringBasePrice(discountAmount);
                    actionResultInfo.ranAction = true;
                    actionResultInfo.totalDiscountAmount = discountAmount;
                }
            }
            
            
            
            
            
        } else if ("PROMO_ORDER_PERCENT".equals(productPromoActionEnumId)) {
            //暂时没有用到
            BigDecimal percentage = (productPromoAction.get("amount") == null ? BigDecimal.ZERO : (productPromoAction.getBigDecimal("amount").movePointLeft(2))).negate();
            BigDecimal amount = cart.getSubTotalForPromotions().multiply(percentage);
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
//                doOrderPromoAction(productPromoAction, cart, amount, "amount", delegator, productStoreId, promoType);
                actionResultInfo.ranAction = true;
                actionResultInfo.totalDiscountAmount = amount;
            }
        } else if ("PROMO_ORDER_AMOUNT".equals(productPromoActionEnumId)) {
            BigDecimal amount = productPromoAction.get("amount") == null ? BigDecimal.ZERO : productPromoAction.getBigDecimal("amount");
            
            GenericValue productPromo = productPromoAction.getRelatedOne("ProductPromo");
            String promoProductType = productPromo.getString("promoProductType");
            List<ShoppingCartItem> lineOrderedByBasePriceList = cart.getProductStoreLineListOrderedByBasePrice(false, productStoreId);
    
            GenericValue productPromoCond = productPromoAction.getRelatedOne("ProductPromoCond");
            BigDecimal orderSubTotal = ProductPromoWorker.getPromoRuleActionProductsTotalAmount(productPromoCond, delegator, cart, productStoreId);
            /*减少金额不能大于下单的金额*/
            if(amount.compareTo(orderSubTotal)<=0) {
    
                if (UtilValidate.isNotEmpty(promoProductType) && "PROMO_PRT_ALL".equals(promoProductType)) {
        
                    Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                    while (lineOrderedByBasePriceIter.hasNext()) {
                        //多级满减不设置cartItem.setIsPromo
                        ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                        BigDecimal cartItemSubTotal = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                        BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(amount).setScale(2, RoundingMode.HALF_UP);
                        cartItem.setRecurringBasePrice(cartItemRealPrice);
                        cartItem.setIsPromo(true);
                        doOrderItemPromoAction(productPromoAction, cartItem, cartItemRealPrice.negate(), "amount", delegator, productStoreId, promoType);
            
                    }
        
                } else {
                    //如果是多个商品参与的满减，需要分摊到每个item下
        
                    Iterator<ShoppingCartItem> lineOrderedByBasePriceIter = lineOrderedByBasePriceList.iterator();
                    Set<String> productIds = ProductPromoWorker.getPromoRuleActionProductIds(productPromoAction, delegator, nowTimestamp);
                    while (lineOrderedByBasePriceIter.hasNext()) {
                        ShoppingCartItem cartItem = lineOrderedByBasePriceIter.next();
                        if (!cartItem.getIsPromo() && (productIds.contains(cartItem.getProductId()))) {
                            //多级满减不设置cartItem.setIsPromo
                            BigDecimal cartItemSubTotal = cartItem.getBasePrice().multiply(cartItem.getQuantity());
                            BigDecimal cartItemRealPrice = cartItemSubTotal.divide(orderSubTotal, 12, RoundingMode.HALF_UP).multiply(amount).setScale(2, RoundingMode.HALF_UP);
                            cartItem.setRecurringBasePrice(cartItemRealPrice);
                            cartItem.setIsPromo(true);
                            doOrderItemPromoAction(productPromoAction, cartItem, cartItemRealPrice.negate(), "amount", delegator, productStoreId, promoType);
                        }
                    }
                }
    
                if (amount.compareTo(BigDecimal.ZERO) != 0) {
//                    doOrderPromoAction(productPromoAction, cart, amount, "amount", delegator, productStoreId, promoType);
                    actionResultInfo.ranAction = true;
                    actionResultInfo.totalDiscountAmount = amount;
                }
            }
        } else if ("PROMO_PROD_SPPRC".equals(productPromoActionEnumId)) {
            // if there are productIds associated with the action then restrict to those productIds, otherwise apply for all products
            Set<String> productIds = ProductPromoWorker.getPromoRuleActionProductIds(productPromoAction, delegator, nowTimestamp);
            
            // go through the cart items and for each product that has a specialPromoPrice use that price
            for (ShoppingCartItem cartItem : cart.items()) {
                if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose())) {
                    String itemProductId = cartItem.getProductId();
                    if (UtilValidate.isEmpty(itemProductId)) {
                        continue;
                    }
                    
                    if (productIds.size() > 0 && !productIds.contains(itemProductId)) {
                        continue;
                    }
                    
                    if (cartItem.getSpecialPromoPrice() == null) {
                        continue;
                    }
                    
                    // get difference between basePrice and specialPromoPrice and adjust for that
                    BigDecimal difference = cartItem.getBasePrice().multiply(cartItem.getRentalAdjustment()).subtract(cartItem.getSpecialPromoPrice()).negate();
                    
                    if (difference.compareTo(BigDecimal.ZERO) != 0) {
                        BigDecimal quantityUsed = cartItem.addPromoQuantityCandidateUse(cartItem.getQuantity(), productPromoAction, false);
                        if (quantityUsed.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal amount = difference.multiply(quantityUsed);
                            doOrderItemPromoAction(productPromoAction, cartItem, amount, "amount", delegator, productStoreId, promoType);
                            actionResultInfo.ranAction = true;
                            actionResultInfo.totalDiscountAmount = amount;
                        }
                    }
                }
            }
        } else if ("PROMO_SHIP_CHARGE".equals(productPromoActionEnumId)) {
            //暂时没有用到
            BigDecimal percentage = (productPromoAction.get("amount") == null ? BigDecimal.ZERO : (productPromoAction.getBigDecimal("amount").movePointLeft(2))).negate();
            BigDecimal amount = cart.getTotalShipping().multiply(percentage);
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
                int existingOrderPromoIndex = cart.getAdjustmentPromoIndex(productPromoAction.getString("productPromoId"));
                if (existingOrderPromoIndex != -1 && cart.getAdjustment(existingOrderPromoIndex).getBigDecimal("amount").compareTo(amount) == 0) {
                    actionResultInfo.ranAction = false;  // already ran, no need to repeat
                } else {
                    if (existingOrderPromoIndex != -1 && cart.getAdjustment(existingOrderPromoIndex).getBigDecimal("amount").compareTo(amount) != 0) {
                        cart.removeAdjustment(existingOrderPromoIndex);
                    }
//                    doOrderPromoAction(productPromoAction, cart, amount, "amount", delegator, productStoreId, promoType);
                    actionResultInfo.ranAction = true;
                    actionResultInfo.totalDiscountAmount = amount;
                }
            }
        } else if ("PROMO_TAX_PERCENT".equals(productPromoActionEnumId)) {
            //暂时没有用到
            BigDecimal percentage = (productPromoAction.get("amount") == null ? BigDecimal.ZERO : (productPromoAction.getBigDecimal("amount").movePointLeft(2))).negate();
            BigDecimal amount = cart.getTotalSalesTax().multiply(percentage);
            if (amount.compareTo(BigDecimal.ZERO) != 0) {
//                doOrderPromoAction(productPromoAction, cart, amount, "amount", delegator, productStoreId, promoType);
                actionResultInfo.ranAction = true;
                actionResultInfo.totalDiscountAmount = amount;
            }
        } else {
            Debug.logError("An un-supported productPromoActionType was used: " + productPromoActionEnumId + ", not performing any action", module);
            actionResultInfo.ranAction = false;
        }
        
        if (actionResultInfo.ranAction) {
            // in action, if doesn't have enough quantity to use the promo at all, remove candidate promo uses and increment promoQuantityUsed; this should go for all actions, if any action runs we confirm
            cart.confirmPromoRuleUse(productPromoAction.getString("productPromoId"), productPromoAction.getString("productPromoRuleId"));
        } else {
            cart.resetPromoRuleUse(productPromoAction.getString("productPromoId"), productPromoAction.getString("productPromoRuleId"));
        }
    }
    
    protected static List<ShoppingCartItem> getCartItemsUsed(ShoppingCart cart, GenericValue productPromoAction) {
        List<ShoppingCartItem> cartItemsUsed = FastList.newInstance();
        for (ShoppingCartItem cartItem : cart) {
            if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equalsIgnoreCase(cartItem.getIsChoose())) {
                BigDecimal quantityUsed = cartItem.getPromoQuantityCandidateUseActionAndAllConds(productPromoAction);
                if (quantityUsed.compareTo(BigDecimal.ZERO) > 0) {
                    cartItemsUsed.add(cartItem);
                }
            }
        }
        return cartItemsUsed;
    }
    
    protected static BigDecimal getCartItemsUsedTotalAmount(ShoppingCart cart, GenericValue productPromoAction) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCartItem cartItem : cart.items()) {
            if (UtilValidate.isNotEmpty(cartItem.getIsChoose()) && "Y".equals(cartItem.getIsChoose())) {
                BigDecimal quantityUsed = cartItem.getPromoQuantityCandidateUseActionAndAllConds(productPromoAction);
                if (quantityUsed.compareTo(BigDecimal.ZERO) > 0) {
                    totalAmount = totalAmount.add(quantityUsed.multiply(cartItem.getBasePrice()));
                }
            }
        }
        return totalAmount;
    }
    
    /*protected static void distributeDiscountAmount(BigDecimal discountAmountTotal, BigDecimal totalAmount, List<ShoppingCartItem> cartItemsUsed, GenericValue productPromoAction, Delegator delegator) {
        BigDecimal discountAmount = discountAmountTotal;
        // distribute the discount evenly weighted according to price over the order items that the individual quantities came from; avoids a number of issues with tax/shipping calc, inclusion in the sub-total for other promotions, etc
        Iterator<ShoppingCartItem> cartItemsUsedIter = cartItemsUsed.iterator();
        while (cartItemsUsedIter.hasNext()) {
            ShoppingCartItem cartItem = cartItemsUsedIter.next();
            // to minimize rounding issues use the remaining total for the last one, otherwise use a calculated value
            if (cartItemsUsedIter.hasNext()) {
                BigDecimal quantityUsed = cartItem.getPromoQuantityCandidateUseActionAndAllConds(productPromoAction);
                BigDecimal ratioOfTotal = quantityUsed.multiply(cartItem.getBasePrice()).divide(totalAmount, generalRounding);
                BigDecimal weightedAmount = ratioOfTotal.multiply(discountAmountTotal);
                // round the weightedAmount to 3 decimal places, we don't want an exact number cents/whatever because this will be added up as part of a subtotal which will be rounded to 2 decimal places
                weightedAmount = weightedAmount.setScale(3, BigDecimal.ROUND_HALF_UP);
                discountAmount = discountAmount.subtract(weightedAmount);
                doOrderItemPromoAction(productPromoAction, cartItem, weightedAmount, "amount", delegator);
            } else {
                // last one, just use discountAmount
                doOrderItemPromoAction(productPromoAction, cartItem, discountAmount, "amount", delegator);
            }
        }
        // this is the old way that causes problems: doOrderPromoAction(productPromoAction, cart, discountAmount, "amount", delegator);
    }*/
    
    /**
     * 计算每个item的促销金额 = 根据本次促销的总的金额/参与促销的item数
     *
     * @param discountAmountTotal
     * @param totalAmount
     * @param cartItemsUsed
     * @param productPromoAction
     * @param delegator
     */
    protected static void distributeDiscountAmount(BigDecimal discountAmountTotal, BigDecimal totalAmount, List<ShoppingCartItem> cartItemsUsed, GenericValue productPromoAction, Delegator delegator, String productStoreId, String promoType) {
        BigDecimal discountAmount = discountAmountTotal;
        // distribute the discount evenly weighted according to price over the order items that the individual quantities came from; avoids a number of issues with tax/shipping calc, inclusion in the sub-total for other promotions, etc
        discountAmount = discountAmount.divide(new BigDecimal(cartItemsUsed.size()));
        Iterator<ShoppingCartItem> cartItemsUsedIter = cartItemsUsed.iterator();
        while (cartItemsUsedIter.hasNext()) {
            ShoppingCartItem cartItem = cartItemsUsedIter.next();
            // to minimize rounding issues use the remaining total for the last one, otherwise use a calculated value
            doOrderItemPromoAction(productPromoAction, cartItem, discountAmount, "amount", delegator, productStoreId, promoType);
        }
        // this is the old way that causes problems: doOrderPromoAction(productPromoAction, cart, discountAmount, "amount", delegator);
    }
    
    
    protected static Integer findPromoItem(GenericValue productPromoAction, ShoppingCart cart) {
        List<ShoppingCartItem> cartItems = cart.items();
        
        for (int i = 0; i < cartItems.size(); i++) {
            ShoppingCartItem checkItem = cartItems.get(i);
            if (UtilValidate.isNotEmpty(checkItem.getIsChoose()) && "Y".equals(checkItem.getIsChoose()) && checkItem.getIsPromo()) {
                // found a promo item, see if it has a matching adjustment on it
                Iterator<GenericValue> checkOrderAdjustments = UtilMisc.toIterator(checkItem.getAdjustments());
                while (checkOrderAdjustments != null && checkOrderAdjustments.hasNext()) {
                    GenericValue checkOrderAdjustment = checkOrderAdjustments.next();
                    if (productPromoAction.getString("productPromoId").equals(checkOrderAdjustment.get("productPromoId")) &&
                            productPromoAction.getString("productPromoRuleId").equals(checkOrderAdjustment.get("productPromoRuleId")) &&
                            productPromoAction.getString("productPromoActionSeqId").equals(checkOrderAdjustment.get("productPromoActionSeqId"))) {
                        return Integer.valueOf(i);
                    }
                }
            }
        }
        return null;
    }
    
    public static void doOrderItemPromoAction(GenericValue productPromoAction, ShoppingCartItem cartItem, BigDecimal amount, String amountField, Delegator delegator, String productStoreId, String promoType) {
        // round the amount before setting to make sure we don't get funny numbers in there
        // only round to 3 places, we need more specific amounts in adjustments so that they add up cleaner as part of the item subtotal, which will then be rounded
        amount = amount.setScale(3, rounding);
        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment",
                UtilMisc.toMap("comments", promoType, "productStoreId", productStoreId, "orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", amountField, amount,
                        "productPromoId", productPromoAction.get("productPromoId"),
                        "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                        "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId"),
                        "correspondingProductId", cartItem.getProductId(),
                        "description", getProductPromoDescription((String) productPromoAction.get("productPromoId"), delegator)));
        
        // if an orderAdjustmentTypeId was included, override the default
        if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
            orderAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
        }
        
        cartItem.addAdjustment(orderAdjustment);
    }
    
    
    public static void doOrderItemPromoAction(GenericValue productPromoAction, ShoppingCartItem cartItem, BigDecimal amount, String amountField, Delegator delegator, String productStoreId, String promoType,String description) {
        // round the amount before setting to make sure we don't get funny numbers in there
        // only round to 3 places, we need more specific amounts in adjustments so that they add up cleaner as part of the item subtotal, which will then be rounded
        amount = amount.setScale(3, rounding);
        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment",
                UtilMisc.toMap("comments", promoType, "productStoreId", productStoreId, "orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", amountField, amount,
                        "productPromoId", productPromoAction.get("productPromoId"),
                        "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                        "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId"),
                        "correspondingProductId", cartItem.getProductId(),
                        "description", description));
        
        // if an orderAdjustmentTypeId was included, override the default
        if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
            orderAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
        }
        
        cartItem.addAdjustment(orderAdjustment);
    }
    
    public static void doOrderPromoAction(GenericValue productPromoAction, ShoppingCart cart, BigDecimal amount, String amountField, Delegator delegator, String productStoreId, String promoType) {
        // round the amount before setting to make sure we don't get funny numbers in there
        amount = amount.setScale(decimals, rounding);
        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment",
                UtilMisc.toMap("comments", promoType, "productStoreId", productStoreId, "orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", amountField, amount,
                        "productPromoId", productPromoAction.get("productPromoId"),
                        "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                        "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId"),
                        "description", getProductPromoDescription((String) productPromoAction.get("productPromoId"), delegator)));
        
        // if an orderAdjustmentTypeId was included, override the default
        if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
            orderAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
        }
        cart.addAdjustment(orderAdjustment);
    }
    
    
    public static void doOrderPromoAction(GenericValue productPromoAction, ShoppingCart cart, BigDecimal amount, String amountField, Delegator delegator, String productStoreId, String promoType, String groupName, String groupType) {
        // round the amount before setting to make sure we don't get funny numbers in there
        amount = amount.setScale(decimals, rounding);
        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment",
                UtilMisc.toMap("comments", promoType, "productStoreId", productStoreId, "orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", amountField, amount,
                        "productPromoId", productPromoAction.get("productPromoId"),
                        "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                        "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId"),
                        "description", groupName));
        
        // if an orderAdjustmentTypeId was included, override the default
        if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
            orderAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
        }
        
        cart.addAdjustment(orderAdjustment);
    }
    
    public static void doOrderPromoAction(GenericValue productPromoAction, ShoppingCart cart, BigDecimal amount, String amountField, Delegator delegator, String productStoreId, String promoType, String productId) {
        // round the amount before setting to make sure we don't get funny numbers in there
        amount = amount.setScale(decimals, rounding);
        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment",
                UtilMisc.toMap("correspondingProductId", productId, "comments", promoType, "productStoreId", productStoreId, "orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", amountField, amount,
                        "productPromoId", productPromoAction.get("productPromoId"),
                        "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                        "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId"),
                        "description", getProductPromoDescription((String) productPromoAction.get("productPromoId"), delegator)));
        
        // if an orderAdjustmentTypeId was included, override the default
        if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
            orderAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
        }
        
        cart.addAdjustment(orderAdjustment);
    }
    
    private static String getProductPromoDescription(String prodPromoId, Delegator delegator) {
        // get the promoText / promoName to set as a descr of the orderAdj
        GenericValue prodPromo;
        try {
            prodPromo = delegator.findByPrimaryKeyCache("ProductPromo", UtilMisc.toMap("productPromoId", prodPromoId));
            if (UtilValidate.isEmpty(prodPromo)) {
                return "";
            }
            if (UtilValidate.isNotEmpty(prodPromo.get("promoText"))) {
                return (String) prodPromo.get("promoText");
            }
            return (String) prodPromo.get("promoName");
            
        } catch (GenericEntityException e) {
            Debug.logWarning("Error getting ProductPromo for Id " + prodPromoId, module);
        }
        
        return null;
    }
    
    protected static Integer findAdjustment(GenericValue productPromoAction, List<GenericValue> adjustments) {
        for (int i = 0; i < adjustments.size(); i++) {
            GenericValue checkOrderAdjustment = adjustments.get(i);
            
            if (productPromoAction.getString("productPromoId").equals(checkOrderAdjustment.get("productPromoId")) &&
                    productPromoAction.getString("productPromoRuleId").equals(checkOrderAdjustment.get("productPromoRuleId")) &&
                    productPromoAction.getString("productPromoActionSeqId").equals(checkOrderAdjustment.get("productPromoActionSeqId"))) {
                return Integer.valueOf(i);
            }
        }
        return null;
    }
    
    public static Set<String> getPromoProductIdsOfAction(GenericValue productPromoCond, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        GenericValue productPromoRule = productPromoCond.getRelatedOne("ProductPromoRule");
        List<GenericValue> productPromoActions = productPromoRule.getRelatedCache("ProductPromoAction", null, UtilMisc.toList("productPromoActionSeqId"));
        Set<String> productIds = FastSet.newInstance();
        for (GenericValue action : productPromoActions) {
            productIds.add(action.getString("productId"));
        }
        return productIds;
    }
    
    public static Set<String> getPromoProductOfAction(GenericValue productPromoAction, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        Set<String> productIds = FastSet.newInstance();
        productIds.add(productPromoAction.getString("productId"));
        return productIds;
    }
    
    public static Set<String> getPromoRuleCondProductIds(GenericValue productPromoCond, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        // get a cached list for the whole promo and filter it as needed, this for better efficiency in caching
        List<GenericValue> productPromoProductsAll = delegator.findByAndCache("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoCond.get("productPromoId")));
        List<GenericValue> productPromoProducts = EntityUtil.filterByAnd(productPromoProductsAll, UtilMisc.toMap("productPromoRuleId", "_NA_", "productPromoCondSeqId", "_NA_"));
        productPromoProducts.addAll(EntityUtil.filterByAnd(productPromoProductsAll, UtilMisc.toMap("productPromoRuleId", productPromoCond.get("productPromoRuleId"), "productPromoCondSeqId", productPromoCond.get("productPromoCondSeqId"))));
        
        Set<String> productIds = FastSet.newInstance();
        makeProductPromoIdSet(productIds, productPromoProducts, delegator, nowTimestamp, false);
        return productIds;
    }
    
    public static Set<String> getPromoRuleActionProductIds(GenericValue productPromoAction, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        // get a cached list for the whole promo and filter it as needed, this for better efficiency in caching
        List<GenericValue> productPromoProductsAll = delegator.findByAndCache("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoAction.get("productPromoId")));
        List<GenericValue> productPromoProducts = EntityUtil.filterByAnd(productPromoProductsAll, UtilMisc.toMap("productPromoRuleId", "_NA_", "productPromoActionSeqId", "_NA_"));
        productPromoProducts.addAll(EntityUtil.filterByAnd(productPromoProductsAll, UtilMisc.toMap("productPromoRuleId", productPromoAction.get("productPromoRuleId"), "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId"))));
        
        Set<String> productIds = FastSet.newInstance();
        makeProductPromoIdSet(productIds, productPromoProducts, delegator, nowTimestamp, false);
        return productIds;
    }
    
    public static void makeProductPromoIdSet(Set<String> productIds, List<GenericValue> productPromoProducts, Delegator delegator, Timestamp nowTimestamp, boolean filterOldProducts) throws GenericEntityException {
        // do the includes
        handleProductPromoProducts(productIds, productPromoProducts, "PPPA_INCLUDE");
        
    }
    
    public static void makeProductPromoCondActionIdSets(String productPromoId, Set<String> productIdsCond, Set<String> productIdsAction, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        makeProductPromoCondActionIdSets(productPromoId, productIdsCond, productIdsAction, delegator, nowTimestamp, false);
    }
    
    public static void makeProductPromoCondActionIdSets(String productPromoId, Set<String> productIdsCond, Set<String> productIdsAction, Delegator delegator, Timestamp nowTimestamp, boolean filterOldProducts) throws GenericEntityException {
        if (nowTimestamp == null) {
            nowTimestamp = UtilDateTime.nowTimestamp();
        }
        
        List<GenericValue> productPromoCategoriesAll = delegator.findByAndCache("ProductPromoCategory", UtilMisc.toMap("productPromoId", productPromoId));
        List<GenericValue> productPromoProductsAll = delegator.findByAndCache("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
        
        List<GenericValue> productPromoProductsCond = FastList.newInstance();
        List<GenericValue> productPromoCategoriesCond = FastList.newInstance();
        List<GenericValue> productPromoProductsAction = FastList.newInstance();
        List<GenericValue> productPromoCategoriesAction = FastList.newInstance();
        
        for (GenericValue productPromoProduct : productPromoProductsAll) {
            // if the rule id is null then this is a global promo one, so always include
            if (!"_NA_".equals(productPromoProduct.getString("productPromoCondSeqId")) || "_NA_".equals(productPromoProduct.getString("productPromoRuleId"))) {
                productPromoProductsCond.add(productPromoProduct);
            }
            if (!"_NA_".equals(productPromoProduct.getString("productPromoActionSeqId")) || "_NA_".equals(productPromoProduct.getString("productPromoRuleId"))) {
                productPromoProductsAction.add(productPromoProduct);
            }
        }
        for (GenericValue productPromoCategory : productPromoCategoriesAll) {
            if (!"_NA_".equals(productPromoCategory.getString("productPromoCondSeqId")) || "_NA_".equals(productPromoCategory.getString("productPromoRuleId"))) {
                productPromoCategoriesCond.add(productPromoCategory);
            }
            if (!"_NA_".equals(productPromoCategory.getString("productPromoActionSeqId")) || "_NA_".equals(productPromoCategory.getString("productPromoRuleId"))) {
                productPromoCategoriesAction.add(productPromoCategory);
            }
        }
        
        makeProductPromoIdSet(productIdsCond, productPromoProductsCond, delegator, nowTimestamp, filterOldProducts);
        makeProductPromoIdSet(productIdsAction, productPromoProductsAction, delegator, nowTimestamp, filterOldProducts);
        
        // last of all filterOldProducts, done here to make sure no product gets looked up twice
        if (filterOldProducts) {
            Iterator<String> productIdsCondIter = productIdsCond.iterator();
            while (productIdsCondIter.hasNext()) {
                String productId = productIdsCondIter.next();
                if (isProductOld(productId, delegator, nowTimestamp)) {
                    productIdsCondIter.remove();
                }
            }
            Iterator<String> productIdsActionIter = productIdsAction.iterator();
            while (productIdsActionIter.hasNext()) {
                String productId = productIdsActionIter.next();
                if (isProductOld(productId, delegator, nowTimestamp)) {
                    productIdsActionIter.remove();
                }
            }
        }
    }
    
    protected static boolean isProductOld(String productId, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
        if (product != null) {
            Timestamp salesDiscontinuationDate = product.getTimestamp("salesDiscontinuationDate");
            if (salesDiscontinuationDate != null && salesDiscontinuationDate.before(nowTimestamp)) {
                return true;
            }
        }
        return false;
    }
    
    protected static void handleProductPromoCategories(Set<String> productIds, List<GenericValue> productPromoCategories, String productPromoApplEnumId, Delegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        boolean include = !"PPPA_EXCLUDE".equals(productPromoApplEnumId);
        Set<String> productCategoryIds = FastSet.newInstance();
        Map<String, List<Set<String>>> productCategoryGroupSetListMap = FastMap.newInstance();
        
        for (GenericValue productPromoCategory : productPromoCategories) {
            if (productPromoApplEnumId.equals(productPromoCategory.getString("productPromoApplEnumId"))) {
                Set<String> tempCatIdSet = FastSet.newInstance();
                if ("Y".equals(productPromoCategory.getString("includeSubCategories"))) {
                    ProductSearch.getAllSubCategoryIds(productPromoCategory.getString("productCategoryId"), tempCatIdSet, delegator, nowTimestamp);
                } else {
                    tempCatIdSet.add(productPromoCategory.getString("productCategoryId"));
                }
                
                String andGroupId = productPromoCategory.getString("andGroupId");
                if ("_NA_".equals(andGroupId)) {
                    productCategoryIds.addAll(tempCatIdSet);
                } else {
                    List<Set<String>> catIdSetList = productCategoryGroupSetListMap.get(andGroupId);
                    if (catIdSetList == null) {
                        catIdSetList = FastList.newInstance();
                    }
                    catIdSetList.add(tempCatIdSet);
                }
            }
        }
        
        // for the ones with andGroupIds, if there is only one category move it to the productCategoryIds Set
        // also remove all empty SetLists and Sets
        Iterator<Map.Entry<String, List<Set<String>>>> pcgslmeIter = productCategoryGroupSetListMap.entrySet().iterator();
        while (pcgslmeIter.hasNext()) {
            Map.Entry<String, List<Set<String>>> entry = pcgslmeIter.next();
            List<Set<String>> catIdSetList = entry.getValue();
            if (catIdSetList.size() == 0) {
                pcgslmeIter.remove();
            } else if (catIdSetList.size() == 1) {
                Set<String> catIdSet = catIdSetList.iterator().next();
                if (catIdSet.size() == 0) {
                    pcgslmeIter.remove();
                } else {
                    // if there is only one set in the list since the set will be or'ed anyway, just add them all to the productCategoryIds Set
                    productCategoryIds.addAll(catIdSet);
                    pcgslmeIter.remove();
                }
            }
        }
        
        // now that the category Set and Map are setup, take care of the productCategoryIds Set first
        getAllProductIds(productCategoryIds, productIds, delegator, nowTimestamp, include);
        
        // now handle the productCategoryGroupSetListMap
        // if a set has more than one category (because of an include sub-cats) then do an or
        // all lists will have more than category because of the pre-pass that was done, so and them together
        for (Map.Entry<String, List<Set<String>>> entry : productCategoryGroupSetListMap.entrySet()) {
            List<Set<String>> catIdSetList = entry.getValue();
            // get all productIds for this catIdSetList
            List<Set<String>> productIdSetList = FastList.newInstance();
            
            for (Set<String> catIdSet : catIdSetList) {
                // make a Set of productIds including all ids from all categories
                Set<String> groupProductIdSet = FastSet.newInstance();
                getAllProductIds(catIdSet, groupProductIdSet, delegator, nowTimestamp, true);
                productIdSetList.add(groupProductIdSet);
            }
            
            // now go through all productId sets and only include IDs that are in all sets
            // by definition if each id must be in all categories, then it must be in the first, so go through the first and drop each one that is not in all others
            Set<String> firstProductIdSet = productIdSetList.remove(0);
            for (Set<String> productIdSet : productIdSetList) {
                firstProductIdSet.retainAll(productIdSet);
            }

            /* the old way of doing it, not as efficient, recoded above using the retainAll operation, pretty handy
            Iterator firstProductIdIter = firstProductIdSet.iterator();
            while (firstProductIdIter.hasNext()) {
                String curProductId = (String) firstProductIdIter.next();

                boolean allContainProductId = true;
                Iterator productIdSetIter = productIdSetList.iterator();
                while (productIdSetIter.hasNext()) {
                    Set productIdSet = (Set) productIdSetIter.next();
                    if (!productIdSet.contains(curProductId)) {
                        allContainProductId = false;
                        break;
                    }
                }

                if (!allContainProductId) {
                    firstProductIdIter.remove();
                }
            }
             */
            
            if (firstProductIdSet.size() >= 0) {
                if (include) {
                    productIds.addAll(firstProductIdSet);
                } else {
                    productIds.removeAll(firstProductIdSet);
                }
            }
        }
    }
    
    protected static void getAllProductIds(Set<String> productCategoryIdSet, Set<String> productIdSet, Delegator delegator, Timestamp nowTimestamp, boolean include) throws GenericEntityException {
        for (String productCategoryId : productCategoryIdSet) {
            // get all product category memebers, filter by date
            List<GenericValue> productCategoryMembers = delegator.findByAndCache("ProductCategoryMember", UtilMisc.toMap("productCategoryId", productCategoryId));
            productCategoryMembers = EntityUtil.filterByDate(productCategoryMembers, nowTimestamp);
            for (GenericValue productCategoryMember : productCategoryMembers) {
                String productId = productCategoryMember.getString("productId");
                if (include) {
                    productIdSet.add(productId);
                } else {
                    productIdSet.remove(productId);
                }
            }
        }
    }
    
    protected static void handleProductPromoProducts(Set<String> productIds, List<GenericValue> productPromoProducts, String productPromoApplEnumId) throws GenericEntityException {
        boolean include = !"PPPA_EXCLUDE".equals(productPromoApplEnumId);
        for (GenericValue productPromoProduct : productPromoProducts) {
            if (productPromoApplEnumId.equals(productPromoProduct.getString("productPromoApplEnumId"))) {
                String productId = productPromoProduct.getString("productId");
                if (include) {
                    productIds.add(productId);
                } else {
                    productIds.remove(productId);
                }
            }
        }
    }
    
    @SuppressWarnings("serial")
    protected static class UseLimitException extends Exception {
        public UseLimitException(String str) {
            super(str);
        }
    }
}
