package com.yuaoq.yabiz.app.mobile.microservice.product.api.v1;

import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.Paginate;
import com.yuaoq.yabiz.product.service.ProductServices;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by changsy on 2018/4/7.
 */
@RestController
@RequestMapping(value = "/api/product/v1")
public class ProductControllerV1 {
    
    @Value("${image.base.url}")
    String baseImgUrl;
    public static final String module = ProductControllerV1.class.getName();
    
    public static UtilCache<String, Map> productDetailCache = UtilCache.createUtilCache("product.content.detail", true);
    
    @RequestMapping(value = "/product/{productId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> productDetail(HttpServletRequest request, @PathVariable(name = "productId") String productId, JwtAuthenticationToken token) {
    
        String webSiteId = request.getHeader("client");
        Map<String, Object> restData = FastMap.newInstance();
        if (UtilValidate.isEmpty(webSiteId)) {
            restData.put("retCode", 0);
            restData.put("message", "站点编号不能为空");
        }
        /*if (productDetailCache.get(productId) != null) {
            restData  = (productDetailCache.get(productId));
            return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
        }*/
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        Locale locale = request.getLocale();
        
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        BigDecimal finalPrice = BigDecimal.ZERO;
        
        
        Map<String, Object> prouductData = null;
        GenericValue userLogin = null;
        String partyId = null;
        try {
            userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", loginName)).get(0);
            partyId = userLogin.getString("partyId");
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        try {
            
            prouductData = dispatcher.runSync("productDetail", UtilMisc.toMap("productId", productId));
            if (ServiceUtil.isError(prouductData)) {
                restData.put("message", ServiceUtil.getErrorMessage(prouductData));
                restData.put("retCode", 0);
                if(productDetailCache!=null){
                    productDetailCache.put(productId,restData);
                }
                return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
                
            } else {
                if (UtilValidate.isNotEmpty(prouductData)) {
                    prouductData = (Map) prouductData.get("resultData");
                    
                    restData.put("priceResult", prouductData.get("priceResult"));
                    finalPrice = (BigDecimal) ((Map) prouductData.get("priceResult")).get("basePrice");
                    restData.put("productCalculatedInfo", prouductData.get("productCalculatedInfo"));
                    restData.put("accountingQuantityTotal", prouductData.get("accountingQuantityTotal"));
                    restData.put("additionalImages", prouductData.get("additionalImages"));
                    restData.put("description", prouductData.get("description"));
                    restData.put("productId", prouductData.get("productId"));
                    restData.put("productTypeId", prouductData.get("productTypeId"));
                    restData.put("mediumImageUrl", prouductData.get("mediumImageUrl"));
                    restData.put("smallImageUrl", prouductData.get("smallImageUrl"));
                    restData.put("longDescription", prouductData.get("longDescription"));
                    restData.put("productName", prouductData.get("productName"));
                    restData.put("isVirtual", prouductData.get("isVirtual"));
                    restData.put("largeImageUrl", prouductData.get("largeImageUrl"));
                    restData.put("originalImageUrl", prouductData.get("originalImageUrl"));
                    restData.put("wrapProductId", prouductData.get("wrapProductId"));
                    restData.put("variantTree", prouductData.get("variantTree"));
                    restData.put("variantTreeChoose", prouductData.get("variantTreeChoose"));
                    restData.put("featuresInfo", prouductData.get("featuresInfo"));
                    restData.put("productStoreId", prouductData.get("productStoreId"));
                    restData.put("serviceTag", prouductData.get("serviceTag"));
                    restData.put("purchaseLimitationQuantity", prouductData.get("purchaseLimitationQuantity"));
                    //需要的积分
                    restData.put("introductionDate", prouductData.get("introductionDate"));
                    restData.put("salesDiscontinuationDate", prouductData.get("salesDiscontinuationDate"));
                    restData.put("subTitle", prouductData.get("internalName"));
                    restData.put("weight", prouductData.get("weight"));
                    restData.put("isBondedGoods", prouductData.get("isBondedGoods"));//保税
                    restData.put("integralDeductionType", prouductData.get("integralDeductionType"));//1.不可使用积分2:百分比抵扣3:固定金额抵扣
                    restData.put("integralDeductionUpper", prouductData.get("integralDeductionUpper")); //积分抵扣数
                    restData.put("voucherAmount", prouductData.get("voucherAmount"));//代金劵面值
                    restData.put("isInner", prouductData.get("isInner"));//是否自营
                    restData.put("isSku", prouductData.get("isVirtual"));
                    restData.put("scorePrice", prouductData.get("scorePrice"));
                    restData.put("scoreValue", prouductData.get("scoreValue"));
                    restData.put("tags", prouductData.get("tags"));
                    restData.put("storeName", prouductData.get("storeName"));
                    restData.put("productParameters", prouductData.get("productParameters"));
                    //计算商品的最大购买次数是否超出
                    BigDecimal purchaseLimitationQuantity = (BigDecimal) prouductData.get("purchaseLimitationQuantity");
                    //计算用户商品的订单数
                    if (UtilValidate.isNotEmpty(purchaseLimitationQuantity)) {

                    //  查找之前的下单记录数
                        List<EntityCondition> mainExps = FastList.newInstance();
                        mainExps.add(EntityCondition.makeCondition("partyId", partyId));
                        mainExps.add(EntityCondition.makeCondition("roleTypeId", "PLACING_CUSTOMER"));
                        mainExps.add(EntityCondition.makeCondition("productId", productId));
                        mainExps.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ORDER_CANCELLED"));
                        List<GenericValue> oldOrders = delegator.findList("OrderHeaderItemAndRoles", EntityCondition.makeCondition(mainExps, EntityOperator.AND), UtilMisc.toSet("orderId", "productId", "quantity", "itemDescription"), null, null, false);
                        BigDecimal totalOrder = BigDecimal.ZERO;
                        if (UtilValidate.isNotEmpty(oldOrders)) {
                            for (int i = 0; i < oldOrders.size(); i++) {
                                GenericValue oldOrder = oldOrders.get(i);
                                totalOrder = totalOrder.add(oldOrder.getBigDecimal("quantity"));
                            }
                        }
                        restData.put("purchaseLimitationQuantity",purchaseLimitationQuantity.subtract(totalOrder));
                        if (totalOrder.compareTo(purchaseLimitationQuantity) >= 0) {
                            restData.put("hasLimitBuy", "Y");
                        }
                    }
                }
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(prouductData.get("productTypeId")) && prouductData.get("productTypeId").equals("FINISHED_GOOD")) {
            try {
                //获取产品的应用的促销信息，直降，团购、秒杀、优惠劵等等
                Map<String, Object> result = dispatcher.runSync("getProductPromoInfoByProductId", UtilMisc.toMap("productId", productId));
                if (ServiceUtil.isError(result)) {
                    restData.put("message", ServiceUtil.getErrorMessage(result));
                    restData.put("retCode", 0);
                    if(productDetailCache!=null){
                        productDetailCache.put(productId,restData);
                    }
                    return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
                    
                } else {
                    
                    Map<String, Object> priceDownInfo = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(result.get("priceDownInfo"))) {
                        Map downInfo = (Map) result.get("priceDownInfo");
                        priceDownInfo.put("productPromoId", downInfo.get("productPromoId"));
                        priceDownInfo.put("promoText", downInfo.get("promoText"));
                        priceDownInfo.put("promoCode", downInfo.get("promoCode"));
                        priceDownInfo.put("productStoreId", downInfo.get("productStoreId"));
                        priceDownInfo.put("amount", downInfo.get("amount"));
                        finalPrice = (BigDecimal) downInfo.get("amount");
                        priceDownInfo.put("productId", downInfo.get("productId"));
                        priceDownInfo.put("promoType", downInfo.get("promoType"));
                        priceDownInfo.put("thruDate", downInfo.get("thruDate"));
                        priceDownInfo.put("fromDate", downInfo.get("fromDate"));
                        priceDownInfo.put("promoName", downInfo.get("promoName"));
                        restData.put("priceDownInfo", priceDownInfo);
                        
                    }
                    Map<String, Object> orderGroupInfo = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(result.get("orderGroupInfo"))) {
                        Map orderGroupMap = (Map) result.get("orderGroupInfo");
                        orderGroupInfo.put("activityDesc", orderGroupMap.get("activityDesc"));
                        orderGroupInfo.put("activityId", orderGroupMap.get("activityId"));
                        orderGroupInfo.put("activityCode", orderGroupMap.get("activityCode"));
                        
                        orderGroupInfo.put("activityStartDate", orderGroupMap.get("activityStartDate"));
                        orderGroupInfo.put("activityEndDate", orderGroupMap.get("activityEndDate"));
                        
                        orderGroupInfo.put("activityQuantity", orderGroupMap.get("activityQuantity"));
                        orderGroupInfo.put("orderPrice", result.get("orderGroupPrice"));//团购价
                        finalPrice = (BigDecimal) result.get("orderGroupPrice");
                        orderGroupInfo.put("hasGroup", orderGroupMap.get("hasGroup"));
                        orderGroupInfo.put("leaveQuantity", orderGroupMap.get("leaveQuantity"));
                        orderGroupInfo.put("averageCustomerRating", orderGroupMap.get("averageCustomerRating"));
                        orderGroupInfo.put("hasBuyQuantity", orderGroupMap.get("hasBuyQuantity"));
                        
                        orderGroupInfo.put("productStoreId", orderGroupMap.get("productStoreId"));
                        orderGroupInfo.put("activityPayType", orderGroupMap.get("activityPayType"));
                        orderGroupInfo.put("scoreValue", orderGroupMap.get("scoreValue"));
                        orderGroupInfo.put("activityName", orderGroupMap.get("activityName"));
                        
                        //当前用户可以购物的次数
                        List<GenericValue> orderSize = delegator.findByAnd("OrderHeaderAndRole", UtilMisc.toMap("partyId", userLogin.getString("partyId"), "roleTypeId", "PLACING_CUSTOMER", "activityId", orderGroupMap.get("activityId")));
                        orderGroupInfo.put("limitQuantity", orderGroupMap.get("activityQuantity"));
                        Integer size = orderSize.size();
                        int quantity = ((Long) orderGroupMap.get("limitQuantity")).intValue();
                        orderGroupInfo.put("limitQuantity", quantity - size);
                        //成团人员、人头像
                        
                        restData.put("orderGroupInfo", orderGroupInfo);
                    }
                    if (UtilValidate.isNotEmpty(result.get("groupInfo"))) {
                        List<Map<String,Object>> groupInfos = (List<Map<String, Object>>) result.get("groupInfo");
                        for (int i = 0; i < groupInfos.size(); i++) {
                            Map<String, Object> groupMap = groupInfos.get(i);
                            List<String> users = (List<String>) groupMap.get("users");
                            if(UtilValidate.isNotEmpty(users)){
                                for (int j = 0; j < users.size(); j++) {
                                    String parId = users.get(j);
                                    if(parId.equals(partyId)){
                                        groupMap.put("hasOrder","Y");
                                    }
                                }
                            }
                        }
                        restData.put("groupInfo", result.get("groupInfo"));
                    }
                    Map<String, Object> secKillInfo = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(result.get("secKillInfo"))) {
                        Map secKillMap = (Map) result.get("secKillInfo");
                        secKillInfo.put("activityDesc", secKillMap.get("activityDesc"));
                        secKillInfo.put("activityId", secKillMap.get("activityId"));
                        secKillInfo.put("activityCode", secKillMap.get("activityCode"));
                        //当前用户可以购物的次数
                        List<GenericValue> orderSize = delegator.findByAnd("OrderHeaderAndRole", UtilMisc.toMap("partyId", userLogin.getString("partyId"), "roleTypeId", "PLACING_CUSTOMER", "activityId", secKillMap.get("activityId")));
                        Integer size = orderSize.size();
                        int quantity = ((Long) secKillMap.get("limitQuantity")).intValue();
                        secKillInfo.put("limitQuantity", quantity - size);
                        secKillInfo.put("activityStartDate", secKillMap.get("activityStartDate"));
                        secKillInfo.put("activityEndDate", secKillMap.get("activityEndDate"));
                        
                        secKillInfo.put("activityQuantity", secKillMap.get("activityQuantity"));
                        secKillInfo.put("hasBuyQuantity", secKillMap.get("hasBuyQuantity"));
                        secKillInfo.put("orderPrice", result.get("secKillPrice"));//团购价
                        finalPrice = (BigDecimal) result.get("secKillPrice");
                        secKillInfo.put("hasGroup", secKillMap.get("hasGroup"));
                        secKillInfo.put("leaveQuantity", secKillMap.get("leaveQuantity"));
                        secKillInfo.put("averageCustomerRating", secKillMap.get("averageCustomerRating"));
                        
                        secKillInfo.put("productStoreId", secKillMap.get("productStoreId"));
                        secKillInfo.put("activityPayType", secKillMap.get("activityPayType"));
                        secKillInfo.put("scoreValue", secKillMap.get("scoreValue"));
                        secKillInfo.put("activityName", secKillMap.get("activityName"));
                        //判断秒杀活动数量和发生的数量比较，判断是否可以继续秒杀
                        Long activityQuantity = (Long) secKillMap.get("activityQuantity")==null? 0L :(Long)secKillMap.get("activityQuantity");
                        Long occuQuantity = (Long) secKillMap.get("occupiedQuantityTotal")==null? 0L :(Long)secKillMap.get("occupiedQuantityTotal");
                        if(activityQuantity>occuQuantity){
                            secKillInfo.put("canBuy","Y");
                        }else{
                            secKillInfo.put("canBuy","N");
                        }
                        restData.put("secKillInfo", secKillInfo);
                    }
                    
                }
            } catch (GenericServiceException e) {
                e.printStackTrace();
                restData.put("message", "获取产品对应的促销活动信息错误");
                restData.put("retCode", 0);
//                if(productDetailCache!=null){
//                    productDetailCache.put(productId,restData);
//                }
                return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            //获取产品对应的店铺促销信息
            try {
                //获取产品的应用的促销信息，直降，团购、秒杀、优惠劵等等
                Map<String, Object> result = dispatcher.runSync("getStorePromoInfoByStoreId", UtilMisc.toMap("productId", productId));
                if (ServiceUtil.isError(result)) {
                    restData.put("message", ServiceUtil.getErrorMessage(result));
                    restData.put("retCode", 0);
                    if(productDetailCache!=null){
                        productDetailCache.put(productId,restData);
                    }
                    return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
                    
                } else {
                    
                    List<Map<String, Object>> promos = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(result.get("promos"))) {
                        List<GenericValue> promoList = (List<GenericValue>) result.get("promos");
                        if (UtilValidate.isNotEmpty(promoList)) {
                            for (GenericValue promo : promoList) {
                                Map<String, Object> promoMap = FastMap.newInstance();
                                promoMap.put("productPromoId", promo.getString("productPromoId"));
                                promoMap.put("useLimitPerCustomer", promo.getString("useLimitPerCustomer"));
                                promoMap.put("useLimitPerOrder", promo.getString("useLimitPerOrder"));
                                promoMap.put("promoText", promo.getString("promoText"));
                                promoMap.put("promoCode", promo.getString("promoCode"));
                                promoMap.put("productStoreId", promo.getString("productStoreId"));
                                promoMap.put("productId", promo.getString("productId"));
                                promoMap.put("promoType", promo.getString("promoType"));
                                promoMap.put("requireCode", promo.getString("requireCode"));
                                promoMap.put("thruDate", promo.getTimestamp("thruDate"));
                                promoMap.put("fromDate", promo.getTimestamp("fromDate"));
                                promoMap.put("promoName", promo.getString("promoName"));
                                promos.add(promoMap);
                            }
                        }
                        restData.put("promos", promos);
                        
                    }
                    
                    List<Map<String, Object>> coupons = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(result.get("coupons"))) {
                        List<GenericValue> couponsList = (List<GenericValue>) result.get("coupons");
                        if (UtilValidate.isNotEmpty(couponsList)) {
                            for (GenericValue coupon : couponsList) {
                                Map<String, Object> couponMap = FastMap.newInstance();
                                couponMap.put("couponName", coupon.getString("couponName"));
                                couponMap.put("startDate", coupon.getString("startDate"));//发放时间
                                couponMap.put("endDate", coupon.getString("endDate"));
                                couponMap.put("couponQuantity", coupon.getString("couponQuantity"));
                                couponMap.put("validitDays", coupon.getString("validitDays"));
                                couponMap.put("useIntegral", coupon.getString("useIntegral"));//积分
                                couponMap.put("arrivedAmount", coupon.getString("arrivedAmount"));//
                                couponMap.put("couponType", coupon.getString("couponType"));
                                couponMap.put("couponDesc", coupon.getString("couponDesc"));
                                couponMap.put("productStoreId", coupon.getString("productStoreId"));
                                couponMap.put("applyScope", coupon.getString("applyScope"));
                                couponMap.put("useBeginDate", coupon.getTimestamp("useBeginDate"));//使用时期
                                couponMap.put("useEndDate", coupon.getTimestamp("useEndDate"));
                                couponMap.put("useWithScore", coupon.getString("useWithScore"));
                                couponMap.put("payReduce", coupon.getString("payReduce"));//代金劵金额
                                couponMap.put("couponPerDay", coupon.getString("couponPerDay"));
                                couponMap.put("couponCode", coupon.getString("couponCode"));
                                couponMap.put("useCount", coupon.getString("useCount"));//领取数
                                couponMap.put("orderCount", coupon.getString("orderCount"));//使用数
                                
                                List<EntityCondition> exps = FastList.newInstance();
                                exps.add(EntityCondition.makeCondition("partyId", partyId));
                                exps.add(EntityCondition.makeCondition("couponCode", coupon.getString("couponCode")));
                                Long count = delegator.findCountByCondition("PromoCouponCodeAndParty", EntityCondition.makeCondition(exps, EntityOperator.AND), null, null);
                                couponMap.put("count",count);
                                couponMap.put("couponPreCustomer",coupon.getLong("couponPreCustomer"));
                                if (count.intValue() >= (coupon.getLong("couponPreCustomer").intValue())) {
                                    couponMap.put("canGet", "N");
                                } else {
                                    couponMap.put("canGet", "Y");
                                }
                                coupons.add(couponMap);
                            }
                        }
                        restData.put("coupons", coupons);
                        
                    }
                    restData.put("productGrps", result.get("productGrps"));
                }
                
                
            } catch (GenericServiceException e) {
                e.printStackTrace();
                restData.put("message", "获取店铺对应的促销活动信息错误");
                restData.put("retCode", 0);
                if(productDetailCache!=null){
                    productDetailCache.put(productId,restData);
                }
                return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
                
                
            } catch (GenericEntityException e) {
                restData.put("message", "获取店铺对应的促销活动信息错误");
                restData.put("retCode", 0);
                e.printStackTrace();
                if(productDetailCache!=null){
                    productDetailCache.put(productId,restData);
                }
                return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
            }
        }
        //积分兑奖比例
        GenericValue integralPerMoney = null;//积分抵现规则表
        try {
            integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
            restData.put("message", "获取店铺对应的促销活动信息错误");
            restData.put("retCode", 0);
            if(productDetailCache!=null){
                productDetailCache.put(productId,restData);
            }
            return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
        }
        /*1.不可使用积分2:百分比抵扣3:固定金额抵扣*/
        Long integralValue = integralPerMoney.getLong("integralValue");
        String integralDeductionType = (String) prouductData.get("integralDeductionType");
        BigDecimal integralDeductionUpper = (BigDecimal) prouductData.get("integralDeductionUpper");
        if (UtilValidate.isNotEmpty(integralDeductionType)) {
            if (integralDeductionType.equalsIgnoreCase("2")) {
                BigDecimal scorePrice = finalPrice.multiply(integralDeductionUpper).divide(new BigDecimal(100));
                BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
                restData.put("scorePrice", scorePrice.setScale(2, BigDecimal.ROUND_HALF_UP)); //积分抵扣的金额
                restData.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
            } else if (integralDeductionType.equalsIgnoreCase("3")) {
                restData.put("scorePrice", integralDeductionUpper.setScale(2, BigDecimal.ROUND_HALF_UP));//积分抵扣的金额
                restData.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
            }
        }
        if(productDetailCache!=null){
            productDetailCache.put(productId,restData);
        }
        return Optional.ofNullable(restData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据联想关键字搜索
     *
     * @param request
     * @param name
     * @param paginate
     * @return
     */
    @RequestMapping(value = "/keyword/{name}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> keywordSearch(HttpServletRequest request, @PathVariable(name = "name") String name, @RequestBody Paginate paginate) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        if (UtilValidate.isEmpty(name)) {
            resultData.put("retCode", 0);
            resultData.put("message", "请输入联想关键字");
        }
        
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //记录集合
        List<Map> recordsList = FastList.newInstance();
        
        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 20;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        boolean beganTransaction;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            EntityListIterator pli = delegator.find("ProductKeyword", EntityCondition.makeCondition("keyword", EntityOperator.LIKE, "%" + name + "%"), null, UtilMisc.toSet("keyword"), null, findOpts);
            
            List<GenericValue> keywords = pli.getPartialList(lowIndex, viewSize);
            List<String> words = FastList.newInstance();
            if (UtilValidate.isNotEmpty(keywords)) {
                for (int i = 0; i < keywords.size(); i++) {
                    GenericValue keyword = keywords.get(i);
                    words.add(keyword.getString("keyword"));
                }
            }
            // 获取总记录数
            size = pli.getResultsSizeAfterPartialList();
            
            //关闭 iterator
            pli.close();
            
            TransactionUtil.commit(beganTransaction);
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
            resultData.put("lowIndex", lowIndex);
            resultData.put("highIndex", highIndex);
            resultData.put("size", size);
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("keywords", words);
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据关键字搜索： SEARCH_STRING
     * 排序 时间： sortOrder=SortProductField:introductionDate
     * totalPartyFavorite|totalQuantityOrdered|totalPartyFavorite|totalTimesViewed|averageCustomerRating
     * 人气：sortOrder=SortProductField:totalTimesViewed
     * 价格：sortOrder=SortProductPrice:DEFAULT_PRICE
     * 销量：sortOrder=SortProductField:totalQuantityOrdered
     * 升降序: sortAscending=N
     * 根据品牌：SEARCH_BRAND_NAME=苹果
     * 根据分类：SEARCH_CATEGORY_ID=10002
     * 价格区间 LIST_PRICE_LOW_LIST_PRICE_HIGHT
     * SKU：SEARCH_FEAS=10007|100111
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> searchProduct(HttpServletRequest request, HttpServletResponse response) {
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        ProductServices.keywordSearch(request, response);
        resultData = (Map<String, Object>) request.getAttribute("resultData");
        boolean beganTransaction;
        try {
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //获取商品对应的品牌
            if (UtilValidate.isNotEmpty(resultData.get("productIds"))) {
                List<String> productIds = (List<String>) resultData.get("productIds");
                //获取商品sumaary
                if (UtilValidate.isNotEmpty(productIds)) {
                    String productStr = "";
                    for (int i = 0; i < productIds.size(); i++) {
                        productStr += productIds.get(i) + ",";
                    }
                    try {
                        Map<String, Object> prouductData = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productStr));
                        if (ServiceUtil.isError(prouductData)) {
                            resultData.put("message", ServiceUtil.getErrorMessage(prouductData));
                            resultData.put("retCode", 0);
                            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
                            
                        } else {
                            List prouductDatas = (List) prouductData.get("resultData");
                            resultData.put("products", prouductDatas);
                        }
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            List<String> allProductIds = (List<String>) resultData.get("allProductIds");
            List<GenericValue> brands = delegator.findList("ProductAndBrand", EntityCondition.makeCondition("productId", EntityOperator.IN, allProductIds), UtilMisc.toSet("brandName", "productBrandId"), null, findOpts, false);
            resultData.put("brands", brands);
            
            //获取商品对应的SKU
            List<GenericValue> features = delegator.findList("ProductFeatureAssoc", EntityCondition.makeCondition("productId", EntityOperator.IN, allProductIds), UtilMisc.toSet("productFeatureId"), null, findOpts, false);
            List skuList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(features)) {
                List<GenericValue> productFeatures = EntityUtil.getRelated("ProductFeature", features);
                if (UtilValidate.isNotEmpty(productFeatures)) {
                    List<GenericValue> productFeatureTypes = EntityUtil.getRelated("ProductFeatureType", productFeatures);
                    if (UtilValidate.isNotEmpty(productFeatureTypes)) {
                        Map typeMap = FastMap.newInstance();
                        for (GenericValue type : productFeatureTypes) {
                            
                            String typeId = type.getString("productFeatureTypeId");
                            String typeName = type.getString("productFeatureTypeName");
                            List<Map> featureList = FastList.newInstance();
                            for (GenericValue feature : productFeatures) {
                                if (typeId.equals(feature.getString("productFeatureTypeId"))) {
                                    Map<String, String> featureMap = FastMap.newInstance();
                                    String featureName = feature.getString("productFeatureName");
                                    String featureId = feature.getString("productFeatureId");
                                    featureMap.put("featureName", featureName);
                                    featureMap.put("featureId", featureId);
                                    featureList.add(featureMap);
                                }
                            }
                            typeMap.put(typeName, featureList);
                            
                            
                        }
                        resultData.put("features", typeMap);
                    }
                }
            }
            
            resultData.remove("allProductIds");
            TransactionUtil.commit(beganTransaction);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据分类搜索产品
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/searchByCategory/{categoryId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> searchProductByCategory(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "categoryId") String
            categoryId, @RequestBody Paginate paginate) {
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        
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
        
        int viewSize = 20;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 20;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        lowIndex = viewIndex * viewSize + 1;
        highIndex = (viewIndex + 1) * viewSize;
        boolean beganTransaction;
        try {
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);//获取分类下的产品
            List<EntityExpr> exps = FastList.newInstance();
            //增加审批通过，并且在商品有效时间范围内的
            exps.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            exps.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            exps.add(EntityCondition.makeCondition("isDel", EntityOperator.NOT_EQUAL, "Y"));
            exps.add(EntityCondition.makeCondition("isVerify", "Y"));
            exps.add(EntityCondition.makeCondition("isVariant", "N"));
            exps.add(EntityCondition.makeCondition("primaryProductCategoryId", categoryId));
            EntityCondition whereCondition = EntityCondition.makeCondition(exps, EntityOperator.AND);
            EntityListIterator eli = delegator.find("Product", whereCondition, null, UtilMisc.toSet("productId"), null, findOpts);
            List<GenericValue> products = eli.getPartialList(lowIndex, highIndex);
            
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();
            //获取商品对应的品牌
            if (UtilValidate.isNotEmpty(products)) {
                String productStr = "";
                for (GenericValue product : products) {
                    productStr += product.getString("productId") + ",";
                }
                try {
                    Map<String, Object> prouductData = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productStr));
                    if (ServiceUtil.isError(prouductData)) {
                        resultData.put("message", ServiceUtil.getErrorMessage(prouductData));
                        resultData.put("retCode", 0);
                        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
                        
                    } else {
                        List prouductDatas = (List) prouductData.get("resultData");
                        resultData.put("products", prouductDatas);
                    }
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                }
            }
            eli.close();
            TransactionUtil.commit(beganTransaction);
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
            resultData.put("paginate", pMap);
            
            
            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
        } catch (Exception e) {
            try {
                TransactionUtil.rollback();
            } catch (GenericTransactionException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据featureIds和productId获取多样化产品
     *
     * @param request
     * @param response
     * @param productId
     * @return
     */
    @RequestMapping(value = "/variant/{productId}", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getVariantInfo(HttpServletRequest request, HttpServletResponse response, @PathVariable(name = "productId") String productId) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String selectedFeatureStr = request.getParameter("selectedFeatures");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        String varProductId = null;
        try {
            String[] selectedFeatures = selectedFeatureStr.split(",");
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            List<GenericValue> varProducts = FastList.newInstance();
            if (product.getString("isVariant").equals("Y")) {
                productId = product.getString("mainProductId");
                varProducts = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", productId));
            } else {
                varProducts = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", productId));
            }
            
            if (UtilValidate.isNotEmpty(varProducts)) {
                for (int i = 0; i < varProducts.size(); i++) {
                    GenericValue varProduct = varProducts.get(i);
                    int k = 0;
                    for (int j = 0; j < selectedFeatures.length; j++) {
                        String featureId = selectedFeatures[j];
                        List<GenericValue> features = delegator.findByAnd("ProductFeatureAssoc", UtilMisc.toMap("productId", varProduct.getString("productId"), "productFeatureId", featureId));
                        if (UtilValidate.isNotEmpty(features)) {
                            k++;
                        }
                    }
                    if (k == selectedFeatures.length) {
                        varProductId = varProduct.getString("productId");
                        break;
                    }
                    
                }
            }
            if (UtilValidate.isNotEmpty(varProductId)) {
                resultData.put("productId", varProductId);
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
        
    }
    
    /**
     * 拼团推荐 列表展示
     *
     * @param request
     * @param page
     * @param pageSize
     * @param productCategoryId
     * @return
     */
    @RequestMapping(value = "/productGroupList", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> productGroupList(HttpServletRequest request, @RequestParam(defaultValue = "0") Integer
            page, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(defaultValue = "") String productCategoryId) {
        
        int limit = pageSize;
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        List<Map> recordsList = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        
        //总记录数
        int size = 0;
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
            
            List<String> productIds = FastList.newInstance();
            
            if (UtilValidate.isNotEmpty(productCategoryId)) {//platformClassId 平台分类id
                List<GenericValue> secondProductCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", productCategoryId));
                if (UtilValidate.isNotEmpty(secondProductCategorys)) {
                    for (int i = 0; i < secondProductCategorys.size(); i++) {
                        String secondLevel = secondProductCategorys.get(i).getString("productCategoryLevel");
                        String secondProductCategoryId = secondProductCategorys.get(i).getString("productCategoryId");
                        if (secondLevel.equals("2")) {
                            List<GenericValue> thirdProductCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", secondProductCategoryId));//PRIMARY_PARENT_CATEGORY_ID
                            if (UtilValidate.isNotEmpty(thirdProductCategorys)) {
                                for (int j = 0; j < thirdProductCategorys.size(); j++) {
                                    String thirdProductCategoryId = thirdProductCategorys.get(j).getString("productCategoryId");
                                    if (UtilValidate.isNotEmpty(thirdProductCategoryId)) {
                                        List<GenericValue> products = delegator.findByAnd("Product", UtilMisc.toMap("platformClassId", thirdProductCategoryId));
                                        if (UtilValidate.isNotEmpty(products)) {
                                            for (int k = 0; k < products.size(); k++) {
                                                String productId = products.get(k).getString("productId");
                                                productIds.add(productId);
                                            }
                                        }
                                        
                                    }
                                    
                                }
                                
                            }
                            
                        }
                        
                    }
                    
                }
                
            }
            
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            List<String> fieldsToSelect = FastList.newInstance();
            List<EntityCondition> andExprs = FastList.newInstance();
            EntityCondition mainCond = null;
            
            dynamicView.addMemberEntity("PA", "ProductActivity");
            dynamicView.addAlias("PA", "activityId");
            dynamicView.addAlias("PA", "activityName");
            dynamicView.addAlias("PA", "activityAuditStatus");
            dynamicView.addAlias("PA", "activityType");
            dynamicView.addAlias("PA", "createdStamp");
            dynamicView.addAlias("PA", "activityStartDate");
            dynamicView.addAlias("PA", "activityEndDate");
            
            dynamicView.addMemberEntity("E", "Enumeration");
            dynamicView.addAlias("E", "enumId");
            dynamicView.addAlias("E", "activityTypeName", "description", null, false, null, null);
            dynamicView.addViewLink("PA", "E", false, ModelKeyMap.makeKeyMapList("activityType", "enumId"));
            
            dynamicView.addMemberEntity("PAG", "ProductActivityGoods");
            dynamicView.addAlias("PAG", "productId");
            dynamicView.addAlias("PAG", "occupiedQuantityTotal");
            dynamicView.addAlias("PAG", "activityPrice");
            dynamicView.addViewLink("PA", "PAG", false, ModelKeyMap.makeKeyMapList("activityId", "activityId"));

            dynamicView.addMemberEntity("P","Product");
            //是否上架
            dynamicView.addAlias("P","isOnline");
            dynamicView.addViewLink("PAG", "P", false, ModelKeyMap.makeKeyMapList("productId", "productId"));

            fieldsToSelect.add("activityId");
            fieldsToSelect.add("activityName");
            fieldsToSelect.add("activityAuditStatus");
            fieldsToSelect.add("activityStartDate");
            fieldsToSelect.add("activityEndDate");
            fieldsToSelect.add("activityType");
            fieldsToSelect.add("activityTypeName");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("activityPrice");
            fieldsToSelect.add("productId");
            fieldsToSelect.add("occupiedQuantityTotal");
            fieldsToSelect.add("isOnline");

            dynamicView.setGroupBy(fieldsToSelect);
            
            orderBy.add("-createdStamp");
            
            //按促销类型查询
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
            //拼团活动的时间限制
            andExprs.add(EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            andExprs.add(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp()));
            
            List<EntityCondition> defaultExprs2 = FastList.newInstance();
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                if (UtilValidate.isNotEmpty(productIds)) {
                    defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.IN, productIds));
                    andExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.AND));
                }
            }
            
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            
            for (GenericValue gv : eli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String activityId = gv.getString("activityId");
                String activityName = gv.getString("activityName");
                String activityType = gv.getString("activityType");
                String activityTypeName = gv.getString("activityTypeName");
                Timestamp activityStartDate = gv.getTimestamp("activityStartDate");
                Timestamp activityEndDate = gv.getTimestamp("activityEndDate");
                String productId = gv.getString("productId");
                String occupiedQuantityTotal = gv.getString("occupiedQuantityTotal");//occupiedQuantityTotal
                String activityPrice = gv.getBigDecimal("activityPrice").setScale(2, BigDecimal.ROUND_HALF_UP).toString();

                map.put("activityId", activityId);
                map.put("activityType", activityType);
                map.put("activityTypeName", activityTypeName);
                map.put("activityName", activityName);
                map.put("activityStartDate", activityStartDate);
                map.put("activityEndDate", activityEndDate);

                if (UtilValidate.isNotEmpty(productId)) {
                    Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productId));
                    List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                    List productList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(products)) {
                        for (int j = 0; j < products.size(); j++) {
                            Map<String, Object> product = products.get(j);
                            Map<String, Object> productMap = FastMap.newInstance();
                            productMap.put("productId", product.get("productId"));
                            BigDecimal price = (BigDecimal) product.get("price");
                            if (UtilValidate.isEmpty(price)) {
                                price = BigDecimal.ZERO;
                            }
                            productMap.put("price", price);
                            productMap.put("productName", product.get("productName"));
                            productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                            if (UtilValidate.isEmpty(activityPrice)) {
                                activityPrice = "0";
                            }
                            if (UtilValidate.isEmpty(occupiedQuantityTotal)) {
                                occupiedQuantityTotal = "0";
                            }
                            if (UtilValidate.isNotEmpty(productId)) {
                                List<GenericValue> toGroups = delegator.findByAnd("TogetherGroup", UtilMisc.toMap("productId", productId), UtilMisc.toList("-createDate"));
                                List<String> personList = FastList.newInstance();
                                Set<String> userPartyId = FastSet.newInstance();
                                if (UtilValidate.isNotEmpty(toGroups)) {
                                    for (int i = 0; i < toGroups.size(); i++) {
                                        String togetherId = toGroups.get(i).getString("togetherId");
                                        if (UtilValidate.isNotEmpty(togetherId)) {
                                            List<GenericValue> relOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));
                                            if (UtilValidate.isNotEmpty(relOrders)) {

                                                for (int r = 0; r < relOrders.size(); r++) {

                                                    String orderId = relOrders.get(r).getString("orderId");
                                                    String orderUserId = relOrders.get(r).getString("orderUserId");
                                                    List<GenericValue> orderPayments = delegator.findByAnd("OrderPaymentPreference",UtilMisc.toMap("orderId",orderId));
                                                    if (UtilValidate.isNotEmpty(orderPayments)){
                                                        GenericValue orderPayment = orderPayments.get(0);
                                                        String statusId = orderPayment.getString("statusId");
                                                        if ("PAYMENT_RECEIVED".equals(statusId)){
                                                            userPartyId.add(orderUserId);
                                                        }
                                                    }

                                                }

                                            }

                                        }
                                    }

                                    for (String partyId : userPartyId ){
                                        GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
                                        if (UtilValidate.isNotEmpty(person)){
                                            String headphoto = person.getString("headphoto");
                                            personList.add(headphoto);
                                        }
                                    }

                                    //头像展示 大于5个则展示5个
                                    if (personList.size() > 5) {
                                        personList = personList.subList(0, 5);
                                    }
                                    productMap.put("personHead", personList);

                                }
                            }
                            productMap.put("activityPrice", activityPrice);
                            productMap.put("activityId", activityId);
                            productMap.put("occupiedQuantityTotal", occupiedQuantityTotal);
                            productList.add(productMap);
                        }
                    }
                    map.put("products", productList);
                }

                recordsList.add(map);
            }
            
            // 获取总记录数
            size = eli.getResultsSizeAfterPartialList();
            
            //关闭 iterator
            resultData.put("highIndex", highIndex);
            resultData.put("lowIndex", lowIndex);
            resultData.put("size", size);
            
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
            resultData.put("paginate", pMap);
            resultData.put("recordsList", recordsList);
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
    
    /**
     * 获取商品标签
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getProductTags", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getProductTags(HttpServletRequest request) {
        Map<String, Object> resultData = FastMap.newInstance();
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        
        try {
            
            beganTransaction = TransactionUtil.begin();
            dynamicView.addMemberEntity("T", "Tag");
            dynamicView.addAlias("T", "tagId");
            dynamicView.addAlias("T", "tagName");
            dynamicView.addAlias("T", "tagTypeId");
            
            fieldsToSelect.add("tagId");
            fieldsToSelect.add("tagName");
            fieldsToSelect.add("tagTypeId");
            
            dynamicView.setGroupBy(fieldsToSelect);
            
            //根据tagTypeId为ProdutTypeTag来筛选
            andExprs.add(EntityCondition.makeCondition("tagTypeId", EntityOperator.EQUALS, "ProdutTypeTag"));
            
            //添加where条件
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            //查询的数据Iterator
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), null, null);
            
            List<Map> tagList = FastList.newInstance();
            for (GenericValue gv : eli.getCompleteList()) {
                Map map = FastMap.newInstance();
                String tagId = gv.getString("tagId");
                String tagName = gv.getString("tagName");
                map.put("tagId", tagId);
                map.put("name", tagName);
                tagList.add(map);
            }
            
            resultData.put("tagList", tagList);
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

        //返回的参数
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 商品推荐列表展示
     *
     * @param request
     * @param productCategoryId
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/productRecomentList", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> productRecomentList(HttpServletRequest request, @RequestParam(defaultValue = "") String
            productCategoryId, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) {
        int limit = pageSize;
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        List<String> fieldsToSelect = FastList.newInstance();
        String webSiteId = request.getHeader("client");
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        
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
        
        int viewSize = 4;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 4;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            List<String> pIds = FastList.newInstance();
            
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                //platformClassId 平台分类id
                List<GenericValue> secondProductCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", productCategoryId));
                if (UtilValidate.isNotEmpty(secondProductCategorys)) {
                    for (int i = 0; i < secondProductCategorys.size(); i++) {
                        String secondLevel = secondProductCategorys.get(i).getString("productCategoryLevel");
                        String secondProductCategoryId = secondProductCategorys.get(i).getString("productCategoryId");
                        if (secondLevel.equals("2")) {
                            List<GenericValue> thirdProductCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", secondProductCategoryId));//PRIMARY_PARENT_CATEGORY_ID
                            if (UtilValidate.isNotEmpty(thirdProductCategorys)) {
                                for (int j = 0; j < thirdProductCategorys.size(); j++) {
                                    String thirdProductCategoryId = thirdProductCategorys.get(j).getString("productCategoryId");
                                    if (UtilValidate.isNotEmpty(thirdProductCategoryId)) {
                                        List<GenericValue> products = delegator.findByAnd("Product", UtilMisc.toMap("platformClassId", thirdProductCategoryId));
                                        if (UtilValidate.isNotEmpty(products)) {
                                            for (int k = 0; k < products.size(); k++) {
                                                String productId = products.get(k).getString("productId");
                                                pIds.add(productId);
                                            }
                                        }
                                        
                                    }
                                    
                                }
                                
                            }
                            
                        }
                        
                    }
                    
                }
                
            }
            
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PRD", "ProductRecommend");
            dynamicView.addAlias("PRD", "recommendId");
            dynamicView.addAlias("PRD", "productId");
            dynamicView.addAlias("PRD", "isAllWebSite");
            dynamicView.addAlias("PRD", "status");
            dynamicView.addAlias("PRD", "sequenceId");
            dynamicView.addAlias("PRD", "createdStamp");
            
            dynamicView.addMemberEntity("PRWS", "ProductRecommendWebSite");
            dynamicView.addAlias("PRWS", "webSiteId");
            dynamicView.addViewLink("PRD", "PRWS", true, ModelKeyMap.makeKeyMapList("recommendId", "recommendId"));

            dynamicView.addMemberEntity("P","Product");
            dynamicView.addAlias("P","isOnline");
            dynamicView.addViewLink("PRD", "P", true, ModelKeyMap.makeKeyMapList("productId", "productId"));

            fieldsToSelect.add("recommendId");
            fieldsToSelect.add("productId");
            fieldsToSelect.add("isAllWebSite");
            fieldsToSelect.add("webSiteId");
            fieldsToSelect.add("status");
            fieldsToSelect.add("sequenceId");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("isOnline");

            dynamicView.setGroupBy(fieldsToSelect);
            
            orderBy.add("sequenceId");
            
            andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, "Y"));
            andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));

            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            andExprs.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.IN, pIds));
            }

            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();
            
            resultData.put("highIndex", highIndex);
            resultData.put("lowIndex", lowIndex);
            resultData.put("size", size);
            List<GenericValue> productRecommends = eli.getPartialList(lowIndex, highIndex);
            
            StringBuffer productIds = new StringBuffer();
            if (UtilValidate.isNotEmpty(productRecommends)) {
                for (GenericValue recommend : productRecommends) {
                    productIds.append(recommend.getString("productId"));
                    productIds.append(",");
                }

                String prodStr = productIds.toString();
                if (prodStr.endsWith(",")) {
                    prodStr = prodStr.substring(0, prodStr.length() - 1);
                }
                    
                List productList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(prodStr)) {

                    Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr));
                    List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                    if (UtilValidate.isNotEmpty(products)) {
                        for (int i = 0; i < products.size(); i++) {
                            Map<String, Object> productMap = FastMap.newInstance();
                            Map<String, Object> product = products.get(i);
                            productMap.put("productId", product.get("productId"));//isOnline
                            productMap.put("productName", product.get("productName"));
                            //是否使用积分进行判断
                            if (UtilValidate.areEqual("2", product.get("integralDeductionType")) || UtilValidate.areEqual("3", product.get("integralDeductionType"))) {
                                //所需要的积分数
                                productMap.put("scoreValue", product.get("scoreValue"));
                                //差价
                                productMap.put("diffPrice", product.get("diffPrice"));
                            }
                            productMap.put("price", product.get("price"));
                            productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                            productList.add(productMap);
                        }
                    }
                    resultData.put("products", productList);
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
            resultData.put("paginate", pMap);
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

}
