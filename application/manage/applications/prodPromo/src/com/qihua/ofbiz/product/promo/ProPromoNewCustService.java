package com.qihua.ofbiz.product.promo;

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
import org.ofbiz.entity.model.ModelViewEntity.ComplexAlias;
import org.ofbiz.entity.model.ModelViewEntity.ComplexAliasField;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by nf on 16/1/9.
 */
public class ProPromoNewCustService {
    public static final String module = ProPromoNewCustService.class.getName();
    /**
     * 会员首次促销查询并回显
     * @param dtx
     * @param context
     * @return
     */
    public Map<String, Object> findPromoNewCustService(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dtx.getDelegator();
        try {
            List<GenericValue> productPromos= delegator.findByAnd("ProductPromo", UtilMisc.toMap("promoType", "PROMO_NEW_CUST"));
            if(productPromos==null||productPromos.size()==0){
                return result;
            }
            GenericValue productPromo=productPromos.get(0);
            productPromoId = productPromo.getString("productPromoId");

            result.put("productPromo", productPromo);
            List<GenericValue> productStorePromoAppls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
                GenericValue productStore_PromoAppls = EntityUtil.getFirst(productStorePromoAppls);
                result.put("productStorePromoAppls", productStore_PromoAppls);
            }
            if (UtilValidate.isNotEmpty(productPromo)) {
                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                if (UtilValidate.isNotEmpty(productPromoRules)) {
                    GenericValue productPromoRule = productPromoRules.get(0);
                    result.put("productPromoRule", productPromoRule);
                    //cond
                    String ruleId = (String) productPromoRule.get("productPromoRuleId");
                    List<EntityCondition> andExprs = FastList.newInstance();
                    andExprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));
                    andExprs.add(EntityCondition.makeCondition("productPromoRuleId", EntityOperator.EQUALS, ruleId));
                    andExprs.add(EntityCondition.makeCondition("inputParamEnumId", EntityOperator.NOT_EQUAL, "PPIP_PARTY_LEVEL"));
                    List<String> orderBy = FastList.newInstance();
                    orderBy.add("productPromoCondSeqId");

                    List<GenericValue> productPromoConds = delegator.findList("ProductPromoCond", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, orderBy, null, false);
                    result.put("productPromoConds", productPromoConds);
                    List<GenericValue> productPromoPartyLevels = delegator.findByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId",
                            productPromoId, "productPromoRuleId", ruleId, "inputParamEnumId", "PPIP_PARTY_LEVEL"));
                    List<GenericValue> partyLevelTypes = FastList.newInstance();
                    for (GenericValue productPromoPartyLevel : productPromoPartyLevels) {
                        partyLevelTypes.add(delegator.findByPrimaryKey("PartyLevelType", UtilMisc.toMap("levelId", productPromoPartyLevel.get("condValue"))));
                    }
                    result.put("productPromoPartyLevels", partyLevelTypes);

                    //action
                    List<GenericValue> productPromoActions = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoActions", productPromoActions);


                    String actionEnumId = productPromoActions.get(0).getString("productPromoActionEnumId");

                    if("PROMO_GWP".equals(actionEnumId)){
                        //赠送商品
                        //查询赠送商品的集合
                        List<Map<String, Object>> productGiftList = FastList.newInstance();
                        if (UtilValidate.isNotEmpty(productPromoActions)) {
                            for (GenericValue product_Promo : productPromoActions) {
                                String productId = (String)product_Promo.get("productId");
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
                                productGiftList.add(map);
                            }
                        }
                        result.put("productList", productGiftList);
                    }

                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 新增会员首次促销
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> addPromoNewCustService(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException, GenericServiceException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoCode ="";
        String promoName = "会员首次促销";
        String promoText = "";
        String productStoreId= (String) context.get("productStoreId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String productIds = (String) context.get("productIds");
        String actionEnumId=(String) context.get("paramEnumId");
        String amount =(String) context.get("Amount");
        String promoStatus =(String) context.get("promoStatus");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();
        List<String> productids = StringUtil.split(productIds, ",");
        String promoProductType = "";
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }
        String productPromoId="";
        Map serviceIn=null;
        Map<String, Object> ret = null;
        //保存之前先删除之前保存的。首次促销只有一条数据
        //先从表中关联查询到当前店铺对应的首次促销信息
        List<GenericValue>  promoList = delegator.findByAnd("ProductStorePromoAndAppl", UtilMisc.toMap("productStoreId",productStoreId,"promoType","PROMO_NEW_CUST"));
        if(promoList!=null&&promoList.size()==1){
            GenericValue value =promoList.get(0);
            //先删除之前的
            productPromoId = (String) value.get("productPromoId");
            Map<String, Object> deleteret =dispatcher.runSync("deleteProductPromoWithoutMainPromo",UtilMisc.toMap("productPromoId",productPromoId));
            if (ServiceUtil.isError(deleteret)) {
                Debug.logError(ServiceUtil.getErrorMessage(deleteret), module);
                return deleteret;
            }
            //保存修改
            GenericValue storeValue = delegator.makeValue("ProductPromo");
            storeValue.set("productPromoId",productPromoId);
            storeValue.set("promoProductType",promoProductType);
            storeValue.set("promoStatus",promoStatus);
            storeValue.store();
        }else{
            //新增
            String promoType = "PROMO_NEW_CUST";
            //创建促销主表
            serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoText", promoText, "promoProductType",
                    promoProductType, "promoType", promoType, "promoStatus", promoStatus, "userLogin", userLogin);
            serviceIn.put("useLimitPerOrder", 1L);
            serviceIn.put("useLimitPerCustomer", 1L);
            serviceIn.put("useLimitPerPromotion", 1L);
            serviceIn.put("requireCode", "N");
            serviceIn.put("userEntered", "Y");
            serviceIn.put("showToCustomer", "Y");
            ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            //获得促销主表的id
            productPromoId = (String) ret.get("productPromoId");
            //修改自动生成promoCode
            promoCode="CX_"+productPromoId;
            GenericValue updateValue = delegator.makeValue("ProductPromo",UtilMisc.toMap("productPromoId",productPromoId,"promoCode",promoCode));
            updateValue.store();


        }
        try {
            // 创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "促销规则", "userLogin", userLogin);
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            //获得促销规则主表id
            productPromoRuleId = (String) ret.get("productPromoRuleId");

            //创建促销条件
            String operatorEnumId = "PPC_EQ";
            serviceIn=UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", "PPIP_CUST_ORDER_NUM",
                    "operatorEnumId", operatorEnumId, "condValue", "0", "userLogin", userLogin);
            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
            //获得促销条件表id
            String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            String quantity = "1";
            //价格优惠或者包邮
            if("PROMO_ORDER_AMOUNT".equals(actionEnumId)||"PROMO_FREE_SHIPPING".equals(actionEnumId)){
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                        "productPromoActionEnumId", actionEnumId, "quantity", new BigDecimal(quantity), "amount", new BigDecimal(amount),"productId","", "userLogin", userLogin);
                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }
            }else{//赠送商品
                String[] productidsStr = productIds.split(",");
                for(String curPrudId:productidsStr){
                    //调用创建促销动作
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", actionEnumId, "quantity", new BigDecimal(quantity), "amount", new BigDecimal(amount),"productId",curPrudId, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                }
            }
            //根据当前登陆人店铺进行关联
            try {
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                        "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }
            } catch (Exception e) {
                return ServiceUtil.returnError(e.getMessage());
            }


        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 删除促销的所有子表，不删除主表
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> deleteProductPromoWithoutMainPromo(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dcx.getDelegator();
        try {
            //删除cond,action,product,store
            delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
//            delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    /**
     * 查询会员首次促销
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPromosNewCust(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");
        String promoStatus = (String) context.get("promoStatus");
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);


        List<GenericValue> promoList = FastList.newInstance();
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
        fieldsToSelect.add("promoCode");
        fieldsToSelect.add("promoName");
        fieldsToSelect.add("promoType");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("thruDate");
        fieldsToSelect.add("promoStatus");
        fieldsToSelect.add("productPromoId");

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("productPromoId" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(promoCode)) {
            paramList = paramList + "&promoCode=" + promoCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoCode + "%")));
        }

        if (UtilValidate.isNotEmpty(promoName)) {
            paramList = paramList + "&promoName=" + promoName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoName + "%")));
        }

        if (UtilValidate.isNotEmpty(fromDate)) {
            paramList = paramList + "&fromDate=" + fromDate;
            Object beginDate = null;
            try {
                beginDate = ObjectType.simpleTypeConvert(fromDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN_EQUAL_TO, beginDate));
        }
        if (UtilValidate.isNotEmpty(thruDate)) {
            paramList = paramList + "&thruDate=" + thruDate;
            Object endDate = null;
            try {
                endDate = ObjectType.simpleTypeConvert(thruDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.LESS_THAN_EQUAL_TO, endDate));
        }
        /*满减状态*/
        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.EQUALS, "PROMO_NEW_CUST"));

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
                EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

                // get the partial list for this page
                promoList = pli.getPartialList(lowIndex, viewSize);

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
        result.put("promoList", promoList);
        result.put("promoCode", promoCode);
        result.put("promoName", promoName);
        result.put("fromDate", fromDate);
        result.put("thruDate", thruDate);
        result.put("promoStatus", promoStatus);

        result.put("promoListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


}
