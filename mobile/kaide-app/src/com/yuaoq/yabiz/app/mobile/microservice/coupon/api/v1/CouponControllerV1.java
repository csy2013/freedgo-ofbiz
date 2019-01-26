package com.yuaoq.yabiz.app.mobile.microservice.coupon.api.v1;

import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import javolution.util.FastList;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.ServiceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by changsy on 2018/4/21.
 */
@RestController
@RequestMapping(path = "/api/coupon/v1")
public class CouponControllerV1 {
    public static final String module = CouponControllerV1.class.getName();
    /**
     * 店铺可以领取的优惠劵
     * @param request
     * @param response
     * @param token
     * @return
     */
    @RequestMapping(value = "/coupons", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> partyCouponGet(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        GenericValue userLogin = null;
        String partyId = null;
        try {
            userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", loginName)).get(0);
            partyId = userLogin.getString("partyId");
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        //不传的话默认全场通用的
        String productStoreId = request.getParameter("productStoreId");
        
        //不传默认是店铺通用的优惠劵，传为针对这个商品的优惠劵
        String productId = request.getParameter("productId");
        boolean beganTransaction = false;
        
        try {
            beganTransaction = TransactionUtil.begin();
            GenericValue  productStore = null;
                    //检查优惠劵是否有效
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //优惠劵
            //包括审批通过 ++ （本店铺内 or 全渠道使用）+ (当前产品或没有选择任何产品)
            List<EntityCondition> andExprs = FastList.newInstance();
            andExprs.add(EntityCondition.makeCondition("couponStatus", "ACTY_AUDIT_PASS"));
            andExprs.add(EntityCondition.makeCondition("result", "ACTY_AUDIT_PASS"));
            andExprs.add(EntityCondition.makeCondition("publishType","COUPON_PRDE_DIR"));
            andExprs.add(EntityCondition.makeCondition("isDel","N"));
            //(当前产品或没有选择任何产品)
            if (UtilValidate.isNotEmpty(productId)) {
                andExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_PART_IN"));
                andExprs.add(EntityCondition.makeCondition("productId", productId));
            }
            //本店铺
            if (UtilValidate.isNotEmpty(productStoreId)) {
                andExprs.add(EntityCondition.makeCondition("applyScope", "S"));
                andExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                productStore = delegator.findByPrimaryKey("ProductStore",UtilMisc.toMap("productStoreId",productStoreId));
            } else {
            //全渠道
                andExprs.add(EntityCondition.makeCondition("applyScope", "A"));
            }
            
            List<GenericValue> coupons = null;
           
            coupons = delegator.findList("ProductPromoCouponInfo", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, findOpts, false);
            
            if (UtilValidate.isNotEmpty(coupons)) {
                //（有效时间内 or 有效天数内）
                coupons = EntityUtil.filterByDate(coupons, UtilDateTime.nowTimestamp(), "startDate", "endDate", true);
                if (UtilValidate.isEmpty(coupons)) {
                
                } else {
                    List<GenericValue> newCoupons = FastList.newInstance();
                    List<GenericValue> products = FastList.newInstance();
                    for (int i = 0; i < coupons.size(); i++) {
                        boolean validate = true;
                        GenericValue coupon = coupons.get(i);
                        //优惠劵限制领取条件
                        //每日限制领取条件
                        Long couponPerDay = coupon.getLong("couponPerDay");
                        List<EntityCondition> exps = FastList.newInstance();
                        exps.add(EntityCondition.makeCondition("couponCode", coupon.getString("couponCode")));
                        exps.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.nowTimestamp())));
                        exps.add(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp())));
                        
                        List<GenericValue> dayCoupons = delegator.findList("PromoCouponCodeAndParty", EntityCondition.makeCondition(exps, EntityOperator.AND), null, null, findOpts, true);
                        if (UtilValidate.isNotEmpty(dayCoupons)) {
                            int dayCouponsCount = dayCoupons.size();
                            if (dayCouponsCount >= couponPerDay) {
                                validate = false;
                            }
                        }
                        //优惠劵数量和用户数量比较
                        Long couponQuantity = coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("couponQuantity");
                        Long userCount = coupon.getLong("userCount") == null ? 0L : coupon.getLong("userCount");
                        if (userCount >= couponQuantity) {
                            validate = false;
                        }
                        if (validate) {
                            if (coupon.get("couponProductType").equals("COUPON_PRT_PART_IN")) {
                                List<GenericValue> pros = coupon.getRelated("ProductCouponProduct");
                                products.addAll(pros);
                            }
                            String couponId = coupon.getString("couponCode");
                            boolean isExist = false;
                            for (int j = 0; j < newCoupons.size(); j++) {
                                GenericValue newCoupon = newCoupons.get(j);
                                if(newCoupon.getString("couponCode").equals(couponId)){
                                    isExist = true;
                                    break;
                                }
                            }
                            if(!isExist) {
                                newCoupons.add(coupon);
                            }
                        }
                    }
                    if (UtilValidate.isNotEmpty(products)) {
                        resultData.put("products", products);
                    }
                    List<Map> couponList = FastList.newInstance();
                    if(UtilValidate.isNotEmpty(newCoupons)){
                        for (int i = 0; i < newCoupons.size(); i++) {
                            GenericValue coupon = newCoupons.get(i);
                            Map couponMap = coupon.toMap();
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
                            couponList.add(couponMap);
                        }
                    }
                    resultData.put("coupons", couponList);
                    if(UtilValidate.isNotEmpty(productStore)) {
                        resultData.put("productStoreName", productStore.getString("storeName"));
                    }
                }
                
            } else {
            
                
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
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    
    /**
     *
     * @param request
     * @param response
     * @param token
     * @return
     */
    @RequestMapping(value = "/isHaveCoupons", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> hasPartyCouponGet(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //不传的话默认全场通用的
        String productStoreId = request.getParameter("productStoreId");
        
        //不传默认是店铺通用的优惠劵，传为针对这个商品的优惠劵
        String productId = request.getParameter("productId");
        boolean beganTransaction = false;
    
        try {
            beganTransaction = TransactionUtil.begin();
            GenericValue  productStore = null;
            //检查优惠劵是否有效
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //优惠劵
            //包括审批通过 ++ （本店铺内 or 全渠道使用）+ (当前产品或没有选择任何产品)
            List<EntityCondition> andExprs = FastList.newInstance();
            andExprs.add(EntityCondition.makeCondition("couponStatus", "ACTY_AUDIT_PASS"));
            andExprs.add(EntityCondition.makeCondition("publishType","COUPON_PRDE_DIR"));
            andExprs.add(EntityCondition.makeCondition("result", "ACTY_AUDIT_PASS"));
            //(当前产品或没有选择任何产品)
            if (UtilValidate.isNotEmpty(productId)) {
                andExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_PART_IN"));
                andExprs.add(EntityCondition.makeCondition("productId", productId));
            }
            //本店铺
            if (UtilValidate.isNotEmpty(productStoreId)) {
                andExprs.add(EntityCondition.makeCondition("applyScope", "S"));
                andExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                productStore = delegator.findByPrimaryKey("ProductStore",UtilMisc.toMap("productStoreId",productStoreId));
            } else {
                //全渠道
                andExprs.add(EntityCondition.makeCondition("applyScope", "A"));
            }
            
            List<GenericValue> coupons = null;
            
            coupons = delegator.findList("ProductPromoCouponInfo", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, findOpts, false);
            
            if (UtilValidate.isNotEmpty(coupons)) {
                //（有效时间内 or 有效天数内）
                coupons = EntityUtil.filterByDate(coupons, UtilDateTime.nowTimestamp(), "startDate", "endDate", true);
                if (UtilValidate.isEmpty(coupons)) {
                
                } else {
                    List<GenericValue> newCoupons = FastList.newInstance();
                    List<GenericValue> products = FastList.newInstance();
                    for (int i = 0; i < coupons.size(); i++) {
                        boolean validate = true;
                        GenericValue coupon = coupons.get(i);
                        //优惠劵限制领取条件
                        //每日限制领取条件
                        Long couponPerDay = coupon.getLong("couponPerDay");
                        List<EntityCondition> exps = FastList.newInstance();
                        exps.add(EntityCondition.makeCondition("couponCode", coupon.getString("couponCode")));
                        exps.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.nowTimestamp())));
                        exps.add(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayEnd(UtilDateTime.nowTimestamp())));
                        
                        List<GenericValue> dayCoupons = delegator.findList("PromoCouponCodeAndParty", EntityCondition.makeCondition(exps, EntityOperator.AND), null, null, findOpts, true);
                        if (UtilValidate.isNotEmpty(dayCoupons)) {
                            int dayCouponsCount = dayCoupons.size();
                            if (dayCouponsCount >= couponPerDay) {
                                validate = false;
                            }
                        }
                        //优惠劵数量和用户数量比较
                        Long couponQuantity = coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("couponQuantity");
                        Long userCount = coupon.getLong("userCount") == null ? 0L : coupon.getLong("userCount");
                        if (userCount >= couponQuantity) {
                            validate = false;
                        }
                        if (validate) {
                            if (coupon.get("couponProductType").equals("COUPON_PRT_PART_IN")) {
                                List<GenericValue> pros = coupon.getRelated("ProductCouponProduct");
                                products.addAll(pros);
                            }
                            newCoupons.add(coupon);
                            
                        }
                    }
                   
                    resultData.put("haveCoupons", newCoupons.size()>0?"Y":"N");
                    return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
                }
                
            } else {
            
            
            }
        }  catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
        resultData.put("haveCoupons","N");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
}
