package com.qihua.ofbiz.product.promo;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 16/4/20.
 */
public class ProductCouponService {

    public static final String module = ProductCouponService.class.getName();

    /**
     * 查询优惠劵
     *
     * @param dcx
     * @param context
     * @return couponList 中包括自定义的字段 userCount,OrderCount.
     */
    public Map<String, Object> findCoupons(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");

        String couponCode = (String) context.get("couponCode");
        String couponName = (String) context.get("couponName");
        String couponType = (String) context.get("couponType");
        String publishType = (String) context.get("publishType");
        String couponRange = (String) context.get("couponRange");
        String couponStatus = (String) context.get("couponStatus");
        Delegator delegator = dcx.getDelegator();
        List<Map> coupon_List = FastList.newInstance();

        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

       /* result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);*/

        List<GenericValue> couponList = FastList.newInstance();
        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
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
        List<String> fieldsToSelect = FastList.newInstance();

        fieldsToSelect.add("couponCode");
        fieldsToSelect.add("couponName");
        fieldsToSelect.add("couponType");
        fieldsToSelect.add("publishType");
        fieldsToSelect.add("couponRange");
        fieldsToSelect.add("couponQuantity");
        fieldsToSelect.add("couponStatus");
        //发放结束时间 add by gss
        fieldsToSelect.add("endDate");
        fieldsToSelect.add("useEndDate");
        fieldsToSelect.add("orderCount");
        fieldsToSelect.add("userCount");
        fieldsToSelect.add("validitType");
        fieldsToSelect.add("partyName");

        List<String> orderBy = FastList.newInstance();


        //排序字段名称
        String sortField = "couponCode";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "-";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }

        orderBy.add(sortType + sortField);
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(couponCode)) {
            paramList = paramList + "&couponCode=" + couponCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("couponCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + couponCode + "%")));
        }

        if (UtilValidate.isNotEmpty(couponName)) {
            paramList = paramList + "&couponName=" + couponName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("couponName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + couponName + "%")));
        }

        if (UtilValidate.isNotEmpty(publishType)) {
            paramList = paramList + "&publishType=" + publishType;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("publishType"), EntityOperator.EQUALS, publishType));

        }
        if (UtilValidate.isNotEmpty(couponType)) {
            paramList = paramList + "&couponType=" + couponType;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("couponType"), EntityOperator.EQUALS, couponType));

        }
        if (UtilValidate.isNotEmpty(couponRange)) {
            paramList = paramList + "&couponRange=" + couponRange;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("couponRange"), EntityOperator.EQUALS, couponRange));

        }
        /*优惠券状态*/
        if (UtilValidate.isNotEmpty(couponStatus)) {
            paramList = paramList + "&couponStatus=" + couponStatus;
            /*判断优惠券是否已结束*/
            /*if (couponStatus.equals("ACTY_AUDIT_PASS")) {
                //发放结束时间大于当前时间
                andExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            }*/
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("couponStatus"), EntityOperator.EQUALS, couponStatus));
        }

        //关联店铺
        if(!"10000".equals(productStoreId)) {
            andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
        }
        //查找未删除的
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isDel"), EntityOperator.EQUALS, "N"));

        // build the main condition
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                EntityListIterator pli = delegator.find("ProductStorePromoAndCoupon", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

                // get the partial list for this page
                couponList = pli.getPartialList(lowIndex, viewSize);
                Date now = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (UtilValidate.isNotEmpty(couponList)) {
                    for (int i = 0; i < couponList.size(); i++) {
                        Map<String, Object> record = FastMap.newInstance();
                        GenericValue coupon = couponList.get(i);
                        String code = (String) coupon.get("couponCode");
//                        7、对应优惠劵码对应用户表:ProductPromoCodeParty
//                        long userCount = delegator.findCountByCondition("PromoCouponCodeParty", EntityCondition.makeCondition("couponCode", EntityOperator.EQUALS, code), null, null);
                        //已经消费的优惠劵
//                        long orderCount = delegator.findCountByCondition("PromoCouponCodeOrder", EntityCondition.makeCondition("couponCode", EntityOperator.EQUALS, code), null, null);
                       
                        record.put("couponCode", coupon.get("couponCode"));
                        record.put("couponName", coupon.get("couponName"));
                        record.put("couponType", coupon.get("couponType"));
                        record.put("publishType", coupon.get("publishType"));
                        record.put("couponRange", coupon.get("couponRange"));
                        record.put("couponQuantity", coupon.get("couponQuantity"));
                        record.put("couponStatus", coupon.get("couponStatus"));
                        record.put("endDate", df.format(coupon.getTimestamp("endDate")));
                        record.put("useEndDate",coupon.get("useEndDate")==null?"": df.format(coupon.getTimestamp("useEndDate")));
                        record.put("validitType",coupon.get("validitType"));
                        record.put("userCount", coupon.get("userCount"));
                        record.put("orderCount", coupon.get("orderCount"));
                        record.put("nowDate", df.format(now));
                        record.put("partyName", coupon.get("partyName"));
                        //coupon.set("userCount",userCount);
                        //coupon.set("orderCount",orderCount);
                        // coupon.set("createdStamp",now);
                        coupon_List.add(record);
                    }
                }

                // attempt to get the full size
                listSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > listSize) {
                    highIndex = listSize;
                }

                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg);
                return ServiceUtil.returnError(e.getMessage());
            }
        } else {
            listSize = 0;
        }
        result.put("couponList", coupon_List);
        result.put("couponStatus", couponStatus);
        result.put("couponCode", couponCode);
        result.put("couponName", couponName);
        result.put("publishType", publishType);
        result.put("couponRange", couponRange);
        result.put("couponType", couponType);


        result.put("couponListSize", Integer.valueOf(listSize));
        result.put("totalSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 优惠劵明细
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> couponCouponDetail(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String couponCode = (String) context.get("couponCode");
        Delegator delegator = dct.getDelegator();
        try {
            GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
            result.put("productPromoCoupon", productPromoCoupon);
            //查看代金券对应的条件商品信息
            List<GenericValue> couponProductList = delegator.findByAnd("ProductCouponProduct",UtilMisc.toMap("couponCode", couponCode));
            List<Map<String, Object>> productCouponProducts = FastList.newInstance();
            String productIds ="";
            if(couponProductList!=null&&couponProductList.size()>0){
                for(GenericValue coupponPrud:couponProductList){
                    String productId = (String)coupponPrud.get("productId");
                    productIds=productIds+productId+",";
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId",productId));
                    Map<String, Object> map = FastMap.newInstance();
                    map.put("productName", product.get("productName"));
                    GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")));
                    GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE")));
                    map.put("productName", product.get("productName"));
                    map.put("productId", product.get("productId"));
                    if (UtilValidate.isNotEmpty(defaultprice)) {
                        map.put("defaultprice", defaultprice.get("price"));
                    } else {
                        map.put("defaultprice", 0);
                    }
                    if (UtilValidate.isNotEmpty(marketPrice)) {
                        map.put("marketprice", marketPrice.get("price"));
                    } else {
                        map.put("marketprice", 0);
                    }
                    productCouponProducts.add(map);
                }
                productIds = productIds.substring(0,productIds.length()-1);
            }
            result.put("productIds", productIds);


            result.put("productCouponProducts", productCouponProducts);

           /* if (UtilValidate.isNotEmpty(productPromoCoupon)) {
                String range = (String) productPromoCoupon.get("couponRange");

                if (range.equals("COUPON_RANGE_SINGLE")) {
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("couponCode", couponCode));
                    result.put("productPromoProducts", productPromoProducts);
                }
                if (range.equals("COUPON_TYPE_CATE")) {
                    List<GenericValue> productPromoCategorys = delegator.findByAnd("ProductPromoCategory", UtilMisc.toMap("couponCode", couponCode));
                    result.put("productPromoCategorys", productPromoCategorys);
                }
                if (range.equals("COUPON_TYPE_BRAND")) {
                    List<GenericValue> productPromoBrands = delegator.findByAnd("ProductPromoBrand", UtilMisc.toMap("couponCode", couponCode));
                    result.put("productPromoBrands", productPromoBrands);
                }

                List<GenericValue> productCouponPartyLevel = delegator.findByAnd("ProductCouponPartyLevel", UtilMisc.toMap("couponCode", couponCode));
                List<String> levels = FastList.newInstance();
                if (UtilValidate.isNotEmpty(productCouponPartyLevel)) {
                    for (GenericValue v : productCouponPartyLevel) {
                        GenericValue partyLevel = delegator.findByPrimaryKey("PartyLevelType", UtilMisc.toMap("levelId", v.get("partyLevel")));
                        if (UtilValidate.isNotEmpty(partyLevel)) {
                            levels.add(partyLevel.getString("levelName"));
                        }
                    }

                }
                result.put("productCouponPartyLevel", levels);
                result.put("productCouponPartyLevels", productCouponPartyLevel);
            }
*/
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 优惠劵代码查询
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> couponCodeDetail(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();

        String couponCode = (String) context.get("couponCode");
        String couponStatus = (String) context.get("couponStatus");
        String partyId = (String) context.get("partyId");
        Delegator delegator = dct.getDelegator();
        try {
            GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
            result.put("productPromoCoupon", productPromoCoupon);
            if (UtilValidate.isNotEmpty(productPromoCoupon)) {
                List<EntityExpr> Exps = FastList.newInstance();
                Exps.add(EntityCondition.makeCondition("couponCode", EntityOperator.EQUALS, couponCode));
                if (UtilValidate.isNotEmpty(couponStatus)) {
                    Exps.add(EntityCondition.makeCondition("couponStatus", EntityOperator.EQUALS, couponStatus));
                }
                if (UtilValidate.isNotEmpty(partyId)) {
                    Exps.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
                }

                List<GenericValue> productPromoCodes = delegator.findList("PromoCouponCodePartyOrder", EntityCondition.makeCondition(Exps, EntityOperator.AND),
                        UtilMisc.toSet("productPromoCodeId", "codeStatus", "partyId", "useDate", "orderDate"), null, null, false);
                result.put("productPromoCodes", productPromoCodes);
            }

        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 新增优惠劵
     *
     * @param dtx
     * @param context
     * @return
     */
    public Map<String, Object> addCoupon(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String couponCode = "";
        String productStoreId = (String) context.get("productStoreId");
        Timestamp startDate = (Timestamp) context.get("startDate");
        Timestamp endDate = (Timestamp) context.get("endDate");
        String productIds = (String) context.get("productIds");


        Delegator delegator = dtx.getDelegator();
        GenericValue productPromoCoupon = delegator.makeValue("ProductPromoCoupon");
        productPromoCoupon.setPKFields(context);
        productPromoCoupon.setNonPKFields(context);
        String couponProductType="";
        if(productIds==null||"".equals(productIds)){
            couponProductType="COUPON_PRT_ALL";//全部产品参与
        }else{
            couponProductType="COUPON_PRT_PART_IN";//部分产品参与
        }
        productPromoCoupon.put("couponProductType",couponProductType);

        String validitType = (String) context.get("validitType");
        if("ROLL".equals(validitType)){
            //滚动方式，删除时间
            productPromoCoupon.set("useBeginDate",null);
            productPromoCoupon.set("useEndDate",null);
        }else{
            productPromoCoupon.set("validitDays",null);
        }
        productPromoCoupon.set("userCount",0L);
        productPromoCoupon.set("orderCount",0L);
        productPromoCoupon.set("isDel","N");

        try {
            couponCode = delegator.getNextSeqId("ProductPromoCoupon");
            productPromoCoupon.set("couponCode", couponCode);
            productPromoCoupon.set("couponStatus", "ACTY_AUDIT_INIT");
            delegator.create(productPromoCoupon);
            //生成所有对应的参与商品的数据
            if(productIds!=null&&!"".equals(productIds)){
                List<GenericValue> values = FastList.newInstance();
                List<String> productIdList = StringUtil.split(productIds, ",");
                for(String prudId:productIdList){
                    GenericValue tempValue = delegator.makeValue("ProductCouponProduct");
                    tempValue.put("couponCode",couponCode);
                    tempValue.put("productId",prudId);
                    values.add(tempValue);
                }
                delegator.storeAll(values);
            }
            //添加优惠券和店铺的关联
            GenericValue gvret = delegator.makeValue("ProductStoreCouponAppl",UtilMisc.toMap("couponCode",couponCode,"productStoreId",productStoreId,"fromDate",startDate,"thruDate",endDate));
            gvret.create();

        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 更新优惠劵
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> updateCoupon(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String couponCode = (String) context.get("couponCode");
        String couponName = (String) context.get("couponName");
        String couponDesc = (String) context.get("couponDesc");
        String couponType = (String) context.get("couponType");
        Timestamp startDate = (Timestamp) context.get("startDate");
        Timestamp endDate = (Timestamp) context.get("endDate");
        Timestamp useBeginDate = (Timestamp) context.get("useBeginDate");
        Timestamp useEndDate = (Timestamp) context.get("useEndDate");
        Long couponPreCustomer = (Long) context.get("couponPreCustomer");
        Long couponPerDay = (Long) context.get("couponPerDay");
        Long couponQuantity = (Long) context.get("couponQuantity");
        String publishType = (String) context.get("publishType");
        String useWithScore = (String) context.get("useWithScore");//使用限制

        Long payFill = (Long) context.get("payFill");//满多少
        BigDecimal payReduce = (BigDecimal) context.get("payReduce");//减少多少
        Long useIntegral = 0L;//使用多少积分，现在默认是""空，直接发放
        String productIds = (String) context.get("productIds");

        GenericValue userLogin = (GenericValue) context.get("userLogin");

        Delegator delegator = dct.getDelegator();
        GenericValue productPromoCoupon = delegator.makeValue("ProductPromoCoupon");
        productPromoCoupon.setPKFields(context);
        productPromoCoupon.setNonPKFields(context);

        String couponProductType="";
        if(productIds==null||"".equals(productIds)){
            couponProductType="COUPON_PRT_ALL";//全部产品参与
        }else{
            couponProductType="COUPON_PRT_PART_IN";//部分产品参与
        }
        productPromoCoupon.put("couponProductType",couponProductType);

        //每次修改都要变成初始化状态进行重新审核
        productPromoCoupon.set("couponStatus","ACTY_AUDIT_INIT");

        String validitType = (String) context.get("validitType");
        if("ROLL".equals(validitType)){
            //滚动方式，删除时间
            productPromoCoupon.set("useBeginDate",null);
            productPromoCoupon.set("useEndDate",null);
        }else{
            productPromoCoupon.set("validitDays",null);
        }

        try {
            delegator.store(productPromoCoupon);
            //删除对应的产品信息
            delegator.removeByAnd("ProductCouponProduct",UtilMisc.toMap("couponCode", couponCode));

            //添加对应的产品
            if(productIds!=null&&!"".equals(productIds)){
                List<GenericValue> values = FastList.newInstance();
                List<String> productIdList = StringUtil.split(productIds, ",");
                for(String prudId:productIdList){
                    GenericValue tempValue = delegator.makeValue("ProductCouponProduct");
                    tempValue.put("couponCode",couponCode);
                    tempValue.put("productId",prudId);
                    values.add(tempValue);
                }
                delegator.storeAll(values);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 下架优惠劵 置endDate当前系统时间
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> endCoupon(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Timestamp endDate = UtilDateTime.nowTimestamp();
        String couponCode = (String) context.get("couponCode");
        Delegator delegator = dct.getDelegator();
        try {
            GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
            productPromoCoupon.set("endDate", endDate);
            delegator.store(productPromoCoupon);

        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 审批优惠劵
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> auditCoupon(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String couponCode = (String) context.get("couponCode");
        String message = (String) context.get("auditMessage");
        String status = (String) context.get("couponStatus");
        LocalDispatcher dispatcher = dct.getDispatcher();
        Delegator delegator = dct.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String userLoginId = userLogin.getString("userLoginId");
        try {

            GenericValue auditLog = delegator.makeValue("PromoAuditLog");
            auditLog.setNextSeqId();
            String businessId ="coupon_"+couponCode;
            auditLog.put("businessId",businessId);
            auditLog.put("auditType","ACTY_AUDIT_PASS".equals(status)?"通过":"驳回");
            auditLog.put("auditPerson",userLoginId);
            auditLog.put("auditMessage",message);
            auditLog.put("createDate",new Timestamp(System.currentTimeMillis()));
            auditLog.create();

            GenericValue audit = delegator.makeValue("ProductCouponAudit");
            audit.set("couponAuditId", delegator.getNextSeqId("ProductCouponAudit"));
            audit.set("couponCode", couponCode);
            audit.set("auditMessage", message);
            audit.set("result", status);
            delegator.create(audit);
            GenericValue coupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
            if (UtilValidate.isNotEmpty(coupon)) {
                /*if (status.equals("ACTY_AUDIT_PASS")) {
                    Map serviceIn = FastMap.newInstance();
                    serviceIn.put("couponCode", couponCode);
                    serviceIn.put("useLimitPerCode", 1l);
                    serviceIn.put("useLimitPerCustomer", 1l);
                    serviceIn.put("quantity", coupon.getLong("couponQuantity"));
                    serviceIn.put("codeLength", 8);
                    serviceIn.put("promoCodeLayout", "smart");
                    serviceIn.put("userEntered", "N");
                    serviceIn.put("requireEmailOrParty", "N");
                    serviceIn.put("userLogin", userLogin);
                    serviceIn.put("codeStatus", "COUPON_CODE_ENABLE");
                    try {
                        dispatcher.runAsync("createProductPromoCodeSet", serviceIn);
                    } catch (ServiceAuthException e) {
                        e.printStackTrace();
                    } catch (ServiceValidationException e) {
                        e.printStackTrace();
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                    }
                }*/
                coupon.set("couponStatus", status);
                delegator.store(coupon);
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 删除优惠劵（暂无）
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> deleteCoupon(DispatchContext dct, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String couponCode = (String) context.get("couponCode");
        Delegator delegator = dct.getDelegator();
        try {
            //修改删除状态
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("isDel", "Y");
            EntityCondition UpdateCon = EntityCondition.makeCondition("couponCode", EntityComparisonOperator.EQUALS, couponCode);
            delegator.storeByCondition("ProductPromoCoupon", updateFields, UpdateCon);

            //删除对应的拆红包模板和答题模板关联的数据
            delegator.removeByAnd("RedPackageCouponSetting",UtilMisc.toMap("couponId",couponCode));
            delegator.removeByAnd("QuestionCouponSetting",UtilMisc.toMap("couponId",couponCode));

            //先删除对应的商品关联信息
//            delegator.removeByAnd("ProductCouponProduct",UtilMisc.toMap("couponCode", couponCode));
            //删除对应的活动
//            delegator.removeByAnd("ProductPromoCoupon",UtilMisc.toMap("couponCode", couponCode));
        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        //删除
        return result;

    }
    /* 查看优惠券驳回原因
    * add  2016-5-5
    * add by gss
    * 
    * */
    public Map<String, Object> findAuditMessage(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String couponCode = (String) context.get("couponCode");
        Delegator delegator = dcx.getDelegator();
        List<GenericValue> ProductCouponAuditList;
        try {
            ProductCouponAuditList = delegator.findByAnd("ProductCouponAudit", UtilMisc.toMap("couponCode", couponCode), UtilMisc.toList("createdStamp"));
            GenericValue ProductCouponAudit = EntityUtil.getFirst(ProductCouponAuditList);
            result.put("ProductCouponAudit", ProductCouponAudit);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*批量审批
     *
     * add by gss
     * */
    public Map<String, Object> batchAuditPromoCoupon(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String obj = (String) context.get("obj");
        String status = (String) context.get("couponStatus");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        // 截取字符串
        String[] m = obj.split(",");
        String opinion = null;
        // 优惠券促销Id
        String couponCode = "";
        // 审核意见
        String auditMessage = "";

        for (int i = 0; i < m.length; i++) {
            opinion = m[i];
            String[] n = opinion.split(":");
            if (n.length > 1) {
                couponCode = n[0];
                auditMessage = n[1];
            } else {
                couponCode = n[0];
            }
            try {
                Delegator delegator = dcx.getDelegator();
                GenericValue audit = delegator.makeValue("ProductCouponAudit");
                audit.set("couponAuditId", delegator.getNextSeqId("ProductCouponAudit"));
                audit.set("couponCode", couponCode);
                audit.set("auditMessage", auditMessage);
                audit.set("result", status);
                delegator.create(audit);
                GenericValue coupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
                if (UtilValidate.isNotEmpty(coupon)) {
                    if ("ACTY_AUDIT_PASS".equals(status)) {
                        Map serviceIn = FastMap.newInstance();
                        serviceIn.put("couponCode", couponCode);
                        serviceIn.put("useLimitPerCode", 1L);
                        serviceIn.put("useLimitPerCustomer", 1L);
                        serviceIn.put("quantity", coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("couponQuantity"));
                        serviceIn.put("codeLength", 8);
                        serviceIn.put("promoCodeLayout", "smart");
                        serviceIn.put("userEntered", "N");
                        serviceIn.put("requireEmailOrParty", "N");
                        serviceIn.put("userLogin", userLogin);
                        serviceIn.put("codeStatus", "COUPON_CODE_ENABLE");
                        try {
                            dispatcher.runAsync("createProductPromoCodeSet", serviceIn);
                        } catch (ServiceAuthException e) {
                            e.printStackTrace();
                        } catch (ServiceValidationException e) {
                            e.printStackTrace();
                        } catch (GenericServiceException e) {
                            e.printStackTrace();
                        }
                    }
                    coupon.set("couponStatus", status);
                    delegator.store(coupon);
                }
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return result;
    }

    /*
     * 优惠券列表
     * add by gss
     * */
    public Map<String, Object> getCouponCodeDetail(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String couponCode = (String) context.get("couponCode");
        String codeStatus = (String) context.get("codeStatus");
        String userLoginId = (String) context.get("userLoginId");
        Delegator delegator = dcx.getDelegator();
        List<Map> recordsList = FastList.newInstance();
        int couponListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));
        result.put("viewSize", Integer.valueOf(viewSize));
        try {
            GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
            result.put("couponName", productPromoCoupon.get("couponName"));
            result.put("couponQuantity", productPromoCoupon.get("couponQuantity"));
            //已领取的优惠券
            long userCount = delegator.findCountByCondition("PromoCouponCodeAndParty", EntityCondition.makeCondition("couponCode", EntityOperator.EQUALS, couponCode), null, null);
            //已经消费的优惠劵
            long orderCount = delegator.findCountByCondition("PromoCouponCodeOrder", EntityCondition.makeCondition("couponCode", EntityOperator.EQUALS, couponCode), null, null);
            result.put("userCount", userCount);
            result.put("orderCount", orderCount);
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }

        result.put("viewSize", Integer.valueOf(viewSize));
        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("UL", "UserLogin");
        dve.addAlias("UL", "partyId");
        dve.addAlias("UL", "userLoginId");

        /** 定义实体动态视图：会员信息 */
        dve.addMemberEntity("PT", "Party");
        dve.addAlias("PT", "partyId");

        dve.addMemberEntity("PPC", "ProductPromoCode");
        dve.addAlias("PPC", "productPromoCodeId");// 券号
        dve.addAlias("PPC", "couponCode");// 券号
        dve.addAlias("PPC", "codeStatus");// 优惠券状态 COUPON_CODE_ENABLE
        // 有效默认，COUPON_CODE_DISABLE 失效==已过期
        // ，COUPON_CODE_GET 已领取==未使用
        // ，COUPON_CODE_USED 已使用
        dve.addMemberEntity("PPCP", "ProductPromoCodeParty");
        dve.addAlias("PPCP", "useDate");// 领取时间 使用时间 orderHeader 中 orderDate
        dve.addMemberEntity("OPCP", "OrderProductPromoCode");
        dve.addAlias("OPCP", "orderId");//订单号

        //dve.addViewLink("U", "PT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));
        dve.addViewLink("PPC", "OPCP", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productPromoCodeId", "productPromoCodeId"));
        dve.addViewLink("PPC", "PPCP", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productPromoCodeId", "productPromoCodeId"));
        dve.addViewLink("PPCP", "PT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
        dve.addViewLink("PT", "UL", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));


        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("productPromoCodeId");//券号
        fieldsToSelect.add("userLoginId");//领取人
        fieldsToSelect.add("codeStatus");//优惠券状态
        fieldsToSelect.add("useDate");//领取时间
        fieldsToSelect.add("orderId");//使用订单号
        List<String> orderBy = FastList.newInstance();
        orderBy.add("productPromoCodeId");//券号排序
        List<EntityExpr> Exps = FastList.newInstance();
        EntityCondition mainCond = null;
        Exps.add(EntityCondition.makeCondition("couponCode",
                EntityOperator.EQUALS, couponCode));
        if (UtilValidate.isNotEmpty(codeStatus)) {
            Exps.add(EntityCondition.makeCondition("codeStatus",
                    EntityOperator.EQUALS, codeStatus));
        } else {
            Exps.add(EntityCondition.makeCondition("codeStatus",
                    EntityOperator.IN, UtilMisc.toList("COUPON_CODE_DISABLE",
                            "COUPON_CODE_GET", "COUPON_CODE_USED")));
        }
        if (UtilValidate.isNotEmpty(userLoginId)) {
            Exps.add(EntityCondition.makeCondition("userLoginId",
                    EntityOperator.LIKE, "%" + userLoginId + "%"));
        }
        if (Exps.size() > 0) {
            mainCond = EntityCondition.makeCondition(Exps, EntityOperator.AND);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dve,
                    mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                FastMap<Object, Object> map = FastMap.newInstance();
                map.put("productPromoCodeId", gv.get("productPromoCodeId"));
                map.put("userLoginId", gv.get("userLoginId"));
                map.put("codeStatus", gv.get("codeStatus"));
                map.put("useDate", df.format(gv.getTimestamp("useDate")));
                if (UtilValidate.isNotEmpty(gv.get("orderId"))) {
                    GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", gv.get("orderId")));
                    if (UtilValidate.isNotEmpty(orderHeader)) {
                        map.put("endDate", df.format(gv.getTimestamp("orderDate")));
                    } else {
                        map.put("endDate", "");
                    }
                } else {
                    map.put("endDate", "");
                }
                recordsList.add(map);
            }

            //couponList = pli.getPartialList(lowIndex, viewSize);
            // attempt to get the full size
            couponListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > couponListSize) {
                highIndex = couponListSize;
            }
            // close the list iterator
            pli.close();
            result.put("recordsList", recordsList);
            result.put("totalSize", Integer.valueOf(couponListSize));
            result.put("highIndex", Integer.valueOf(highIndex));
            result.put("lowIndex", Integer.valueOf(lowIndex));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 当前系统时间
     * 优惠码自动失效
     * add by gss
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> disableCouponCode(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Timestamp nowDate = UtilDateTime.nowTimestamp();
        String couponCode = (String) context.get("couponCode");
        Delegator delegator = dct.getDelegator();
        try {
            GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
            Timestamp useEndDate = productPromoCoupon.getTimestamp("useEndDate");
            List<EntityExpr> Exps = FastList.newInstance();
            Exps.add(EntityCondition.makeCondition("couponCode", EntityOperator.EQUALS, couponCode));
            Exps.add(EntityCondition.makeCondition("codeStatus", EntityOperator.NOT_EQUAL, "COUPON_CODE_USED"));
            if (useEndDate.before(nowDate)) {
                List<GenericValue> productPromoCodesList = delegator.findList("ProductPromoCode", EntityCondition.makeCondition(Exps, EntityOperator.AND),
                        null, null, null, false);
                for (GenericValue gv : productPromoCodesList) {
                    gv.set("codeStatus", "COUPON_CODE_DISABLE");
                    gv.store();
                }
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 校验 支付后领取 金额是否重复
     * add by gss
     *
     * @param dct
     * @param context
     * @return
     */
    public Map<String, Object> checkAfterPayNum(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String afterPayNum = (String) context.get("afterPayNum");
        String afterPayUom = (String) context.get("afterPayUom");
        Delegator delegator = dct.getDelegator();
        try {
            List<EntityExpr> Exps = FastList.newInstance();
            //发放结束时间大于当前时间
            Exps.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //订单满多少
            Exps.add(EntityCondition.makeCondition("afterPayNum", EntityOperator.EQUALS, new Long(afterPayNum)));
            //件 /元
            Exps.add(EntityCondition.makeCondition("afterPayUom", EntityOperator.EQUALS, afterPayUom));
            //已通过 待审核的
            Exps.add(EntityCondition.makeCondition("couponStatus",
                    EntityOperator.IN, UtilMisc.toList("ACTY_AUDIT_INIT",
                            "ACTY_AUDIT_PASS")));
            List<GenericValue> productPromoCouponList = delegator.findList("ProductPromoCoupon", EntityCondition.makeCondition(Exps, EntityOperator.AND),
                    null, null, null, false);

            if (UtilValidate.isNotEmpty(productPromoCouponList)) {
                result.put("status", true);
            } else {
                result.put("status", false);
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    public Map<String, Object> editCouponEndTime(DispatchContext dct, Map<String, ? extends Object> context) throws GenericEntityException {
        Delegator delegator = dct.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();

        String couponCode = (String) context.get("couponCode");
        Timestamp endDate = (Timestamp) context.get("endDate");
        Timestamp useEndDate = context.get("useEndDate")==null?null:(Timestamp)context.get("useEndDate");
        String validitType = (String) context.get("validitType");

        Map<String, Object> updateFields = FastMap.newInstance();
        updateFields.put("endDate",endDate);
        updateFields.put("useEndDate",useEndDate);
        EntityCondition UpdateCon = EntityCondition.makeCondition("couponCode", EntityComparisonOperator.EQUALS, couponCode);
        delegator.storeByCondition("ProductPromoCoupon", updateFields, UpdateCon);

        return result;
    }
    
    
    
   

}
