package org.ofbiz.order.order;

import javolution.util.FastList;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by changsy on 2018/4/21.
 */
public class CouponServices {
    
    
    /**
     * 检查优惠劵可领取状态
     *
     * @param couponCode
     * @param productStoreId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> validateCouponCodeGetStatus(String partyId, String couponCode, String productStoreId, Delegator delegator) throws GenericEntityException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        //检查优惠劵是否有效
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        //优惠劵
        //包括审批通过 ++ （本店铺内 or 全渠道使用）+ (当前产品或没有选择任何产品)
        List andExprs = FastList.newInstance();
        andExprs.add(EntityCondition.makeCondition("couponStatus", "ACTY_AUDIT_PASS"));
        andExprs.add(EntityCondition.makeCondition("result", "ACTY_AUDIT_PASS"));
        
        //(当前产品或没有选择任何产品)
        List orExprs = FastList.newInstance();
        
        orExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_PART_IN"));
        orExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_ALL"));
        
        //（本店铺内 or 全渠道使用）
        List<EntityCondition> sExp = FastList.newInstance();
        List<EntityCondition> aExp = FastList.newInstance();
        //本店铺
        if (UtilValidate.isNotEmpty(productStoreId)) {
            andExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            
        }
        andExprs.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
        andExprs.add(EntityCondition.makeCondition("couponCode", couponCode));
        
        List<GenericValue> coupons = delegator.findList("ProductPromoCouponInfo", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, findOpts, false);
        if (UtilValidate.isNotEmpty(coupons)) {
            //（有效时间内 or 有效天数内）
            coupons = EntityUtil.filterByDate(coupons, UtilDateTime.nowTimestamp(), "startDate", "endDate", true);
            if (UtilValidate.isEmpty(coupons)) {
                resultData = ServiceUtil.returnError("优惠劵失效");
            } else {
                GenericValue coupon = coupons.get(0);
                //优惠劵限制领取条件
                //每人限制条件
                Long limit = coupon.getLong("couponPreCustomer");
                List<GenericValue> partyCoupons = delegator.findByAnd("PromoCouponCodeAndParty", UtilMisc.toMap("partyId", partyId, "couponCode", couponCode));
                if (UtilValidate.isNotEmpty(partyCoupons) && UtilValidate.isNotEmpty(limit)) {
                    int paryCouponCount = partyCoupons.size();
                    if (paryCouponCount >= limit) {
                        resultData = ServiceUtil.returnError("用户已经达到领取优惠劵数量上限");
                    }
                }
                //每日限制领取条件
                Long couponPerDay = coupon.getLong("couponPerDay");
                List<EntityCondition> exps = FastList.newInstance();
                exps.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.nowTimestamp())));
                exps.add(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp())));
                exps.add(EntityCondition.makeCondition("couponCode", couponCode));
                List<GenericValue> dayCoupons = delegator.findList("PromoCouponCodeAndParty", EntityCondition.makeCondition(exps, EntityOperator.AND), null, null, findOpts, true);
                if (UtilValidate.isNotEmpty(dayCoupons)) {
                    int dayCouponsCount = dayCoupons.size();
                    if (dayCouponsCount >= couponPerDay) {
                        resultData = ServiceUtil.returnError("已经达到每日领取优惠劵数量上限");
                    }
                }
                //优惠劵数量和用户数量比较
                Long couponQuantity = coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("couponQuantity");
                Long userCount = coupon.getLong("userCount");
                if (UtilValidate.isNotEmpty(couponQuantity) && UtilValidate.isNotEmpty(userCount) && userCount >= couponQuantity) {
                    resultData = ServiceUtil.returnError("优惠劵数量不足");
                }
                resultData.put("coupon", coupon);
            }
            
        } else {
            resultData = ServiceUtil.returnError("优惠劵失效");
            
        }
        return resultData;
    }
    
    
    /**
     * 检查优惠劵可使用状态
     * @param productStoreId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static Map<String, Object> validateCouponCodeUseStatus(List<GenericValue> orderItems, BigDecimal orderAmount,Boolean userScore,String productPromoCodeId, String productStoreId, Delegator delegator) throws GenericEntityException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        
        GenericValue codeAndParty = EntityUtil.getFirst(EntityUtil.filterByDate(delegator.findByAnd("PromoCouponCodeAndParty",UtilMisc.toMap("productPromoCodeId",productPromoCodeId))));
        //判断优惠劵的使用有效期
         if(UtilValidate.isEmpty(codeAndParty)){
             return ServiceUtil.returnError("优惠劵超出有效使用时间");
         }else {
             String couponCode = codeAndParty.getString("couponCode");
             EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
             //优惠劵
             //包括审批通过 ++ （本店铺内 or 全渠道使用）+ (当前产品或没有选择任何产品)
             List andExprs = FastList.newInstance();
             andExprs.add(EntityCondition.makeCondition("couponStatus", "ACTY_AUDIT_PASS"));
             andExprs.add(EntityCondition.makeCondition("result", "ACTY_AUDIT_PASS"));
    
             //(当前产品或没有选择任何产品)
             List<String> products = FastList.newInstance();
             for (GenericValue orderItem : orderItems) {
                 products.add(orderItem.getString("productId"));
             }
             List orExprs = FastList.newInstance();
             List productExprs = FastList.newInstance();
             productExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_PART_IN"));
             productExprs.add(EntityCondition.makeCondition("productId", EntityOperator.IN, products));
             orExprs.add(EntityCondition.makeCondition(productExprs, EntityOperator.AND));
             orExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_ALL"));
    
             //（本店铺内 or 全渠道使用）
             List<EntityCondition> sExp = FastList.newInstance();
             List<EntityCondition> aExp = FastList.newInstance();
             //本店铺
             sExp.add(EntityCondition.makeCondition("applyScope", "S"));
             sExp.add(EntityCondition.makeCondition("productStoreId", productStoreId));
    
             //全渠道
             aExp.add(EntityCondition.makeCondition("applyScope", "A"));
             aExp.add(EntityCondition.makeCondition(sExp, EntityOperator.AND));
    
             andExprs.add(EntityCondition.makeCondition(aExp, EntityOperator.OR));
             andExprs.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
             andExprs.add(EntityCondition.makeCondition("couponCode", couponCode));
             List<GenericValue> coupons = delegator.findList("ProductPromoCouponInfo", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, findOpts, false);
             if (UtilValidate.isNotEmpty(coupons)) {
                 //（有效时间内 or 有效天数内）
                 GenericValue coupon = coupons.get(0);
                 BigDecimal payFill = new BigDecimal(coupon.getLong("payFill"));
                 if (payFill.compareTo(orderAmount) > 0) {
                     return ServiceUtil.returnError("订单金额小于优惠劵优惠条件");
                 }
                 String useWithScore = coupon.getString("useWithScore");
                 if (UtilValidate.isNotEmpty(useWithScore) && "N".equals(useWithScore)) {
                     if (userScore) {
                         return ServiceUtil.returnError("优惠劵不能和积分一起使用");
                     }
                 }
                 //把应用到的商品ID加进去
                 List<String> realProductIds = FastList.newInstance();
                 if(coupon.getString("couponProductType").equals("COUPON_PRT_ALL")){
                     for (int i = 0; i < products.size(); i++) {
                         for (int j = 0; j < orderItems.size(); j++) {
                             GenericValue orderItem =  orderItems.get(j);
                             if(orderItem.getString("productId").equals(products.get(i))){
                                 realProductIds.add(products.get(i));
                             }
                         }
                     }
                     resultData.put("productIds",realProductIds);
                 }else{
                     
                     for (int i = 0; i < coupons.size(); i++) {
                         GenericValue couponInfo = coupons.get(i);
                         for (int j = 0; j < orderItems.size(); j++) {
                             GenericValue orderItem =  orderItems.get(j);
                             if(orderItem.getString("productId").equals(couponInfo.getString("productId"))){
                                 realProductIds.add(couponInfo.getString("productId"));
                             }
                         }
                     }
                     resultData.put("productIds",realProductIds);
                 }
        
        
             } else {
                 return ServiceUtil.returnError("该优惠劵状态无效");
             }
         }
        return resultData;
    }
}
