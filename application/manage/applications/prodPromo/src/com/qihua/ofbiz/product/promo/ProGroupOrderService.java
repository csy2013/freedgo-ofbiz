package com.qihua.ofbiz.product.promo;

import com.google.common.base.Joiner;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.product.product.ProductServices;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

public class ProGroupOrderService {

    public Map<String, Object> addGroupOrder(DispatchContext dcx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String activityName = (String) context.get("activityName");
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        Long limitQuantity = (Long) context.get("limitQuantity");
        String activityDesc = (String) context.get("activityDesc");

        String isAnyReturn = (String) context.get("isAnyReturn");
        String isSupportOverTimeReturn = (String) context.get("isSupportOverTimeReturn");
        String isSupportScore = (String) context.get("isSupportScore");
        String isSupportReturnScore = (String) context.get("isSupportReturnScore");
        String isShowIndex = (String) context.get("isShowIndex");
        String isPostageFree = (String) context.get("isPostageFree");

        String productLineList = (String) context.get("productLineList");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        //创建productActivity

        try {
            //查看是否有其他全场促销
            boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator,productStoreId,activityStartDate,activityEndDate);
            if(isPromoAllExist){
                return ServiceUtil.returnFailure("当前时间已存在全场促销");
            }

            List<GenericValue> allStore = new LinkedList<GenericValue>();
            GenericValue productActivity = delegator.makeValue("ProductActivity");
            productActivity.set("activityName", activityName);
            productActivity.set("activityStartDate", activityStartDate);
            productActivity.set("activityEndDate", activityEndDate);
            productActivity.set("limitQuantity", limitQuantity);
            productActivity.set("activityType", "GROUP_ORDER");
            productActivity.set("activityAuditStatus", "ACTY_AUDIT_INIT");
            productActivity.set("activityDesc", activityDesc);

            productActivity.set("isAnyReturn", isAnyReturn);
            productActivity.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
            productActivity.set("isSupportScore", isSupportScore);
            productActivity.set("isSupportReturnScore", isSupportReturnScore);
            productActivity.set("isShowIndex", isShowIndex);
            productActivity.set("isPostageFree", isPostageFree);

            productActivity.set("hasGroup", "N");
            productActivity.set("hasBuyQuantity", 0L);
            productActivity.set("occupiedQuantityTotal", 0L);
            productActivity.set("isUpdatePrice", "Y");
            String promoId = "";
            String activityId = delegator.getNextSeqId("ProductActivity");
            String activityCode = "";
            productActivity.set("activityId", activityId);
            productActivity.create();
            //修改团购code
            activityCode = "TG_" + activityId;
            GenericValue updateValue = delegator.makeValue("ProductActivity", UtilMisc.toMap("activityId", activityId, "activityCode", activityCode));
            updateValue.store();

            //遍历团购商品，生成对应的规则。
            List<String> productLines = StringUtil.split(productLineList, "|");
            int i = 0;
            for (String prodLine : productLines) {
                String[] lineStrs = prodLine.split(",");
                String productId = lineStrs[0];
//                String activityQuantity= lineStrs[1];
                String activityPersonNum = lineStrs[1];
                String activityPrice = lineStrs[2];

                GenericValue productActivityGoods = delegator.makeValue("ProductActivityGoods");
                productActivityGoods.set("productId", productId);
                productActivityGoods.set("isAnyReturn", isAnyReturn);
                productActivityGoods.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
                productActivityGoods.set("isSupportScore", isSupportScore);
                productActivityGoods.set("isSupportReturnScore", isSupportReturnScore);
                productActivityGoods.set("isShowIndex", isShowIndex);
                productActivityGoods.set("isPostageFree", isPostageFree);
                productActivityGoods.set("activityId", activityId);
                productActivityGoods.set("activityQuantity", new Long(activityPersonNum));
                productActivityGoods.set("activityPrice", new BigDecimal(activityPrice));
//                productActivityGoods.set("activityQuantity", Long.parseLong(activityQuantity));
                allStore.add(productActivityGoods);

                //创建团购促销
                String promoName = activityName + ":促销" + (i + 1);
                String promoText = promoName;
                String userEntered = "Y";
                String showToCustomer = "Y";
                String requireCode = "N";
                Long useLimitPerOrder = 1L;
                Long useLimitPerCustomer = 1L;
                Map inputParams = FastMap.newInstance();
                inputParams.put("promoName", promoName);
                inputParams.put("promoText", promoText);
                inputParams.put("userEntered", userEntered);
                inputParams.put("showToCustomer", showToCustomer);
                inputParams.put("requireCode", requireCode);
                inputParams.put("useLimitPerOrder", useLimitPerOrder);
                inputParams.put("useLimitPerCustomer", useLimitPerCustomer);
                inputParams.put("userLogin", userLogin);
                Map<String, Object> callback = null;
                try {
                    callback = dispatcher.runSync("createProductPromo", inputParams);
                    String productPromoId = (String) callback.get("productPromoId");
                    //创建促销规则
                    String ruleName = activityName + ":促销条件" + (i + 1);
                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoId", productPromoId);
                    inputParams.put("ruleName", ruleName);
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoRule", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }
                    String productPromoRuleId = (String) callback.get("productPromoRuleId");
                    //创建createProductPromoCond
                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoRuleId", productPromoRuleId);
                    inputParams.put("productPromoId", productPromoId);

                    inputParams.put("inputParamEnumId", "PPIP_GRPODR_TOTAL");
                    inputParams.put("operatorEnumId", "PPC_GTE");
                    inputParams.put("condValue", activityPersonNum);
//                    inputParams.put("otherValue", RQuantity);
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoCond", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }
                    String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");

                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoRuleId", productPromoRuleId);
                    inputParams.put("productPromoId", productPromoId);
                    inputParams.put("productPromoCondSeqId", productPromoCondSeqId);
                    inputParams.put("productPromoActionSeqId", "_NA_");
                    inputParams.put("productId", productId);
                    inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }
                    //创建createProductPromoAction
                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoCondSeqId", productPromoCondSeqId);
                    inputParams.put("productPromoRuleId", productPromoRuleId);
                    inputParams.put("productPromoId", productPromoId);
                    inputParams.put("productPromoActionEnumId", "PROMO_PROD_ASGPC");
                    inputParams.put("orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT");
                    inputParams.put("quantity", BigDecimal.ONE);
                    inputParams.put("amount", new BigDecimal(activityPrice));
                    inputParams.put("useCartQuantity", "N");
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoAction", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }
                    //创建产品团购规则对应每个促销
                    GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                    productGroupOrderRule.set("activityId", activityId);
                    productGroupOrderRule.set("seqId", delegator.getNextSeqId("ProductGroupOrderRule"));
//                    productGroupOrderRule.set("orderQuantity", lQuantity);
//                    productGroupOrderRule.set("orderPrice", price);
                    productGroupOrderRule.set("productPromoId", productPromoId);
                    allStore.add(productGroupOrderRule);


                } catch (GenericServiceException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError(e.getMessage());
                }
                if (ServiceUtil.isError(callback)) {
                    return result;
                }

                i++;
            }

            delegator.storeAll(allStore);
            //创建团购和店铺的关联
            GenericValue storeValue = delegator.makeValue("ProductStoreProductActAppl", UtilMisc.toMap("productStoreId", productStoreId, "activityId", activityId
                    , "fromDate", activityStartDate, "thruDate", activityEndDate));
            storeValue.create();


        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    public Map<String, Object> updateGroupOrder(DispatchContext dcx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityId = (String) context.get("activityId");
        String productStoreId = (String) context.get("productStoreId");
        String activityName = (String) context.get("activityName");
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        Long limitQuantity = (Long) context.get("limitQuantity");
        String activityDesc = (String) context.get("activityDesc");

        String isAnyReturn = (String) context.get("isAnyReturn");
        String isSupportOverTimeReturn = (String) context.get("isSupportOverTimeReturn");
        String isSupportScore = (String) context.get("isSupportScore");
        String isSupportReturnScore = (String) context.get("isSupportReturnScore");
        String isShowIndex = (String) context.get("isShowIndex");
        String isPostageFree = (String) context.get("isPostageFree");

        String productLineList = (String) context.get("productLineList");
        String productIds=(String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dcx.getDelegator();
        Map ret = null;
        GenericValue productActivityForUpdate=null;
        List<String> productIdList=StringUtil.split(productIds, ",");// 商品编码列表
        // 根据团购活动编码取得秒杀信息
        try {
            productActivityForUpdate = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
            // 验证状态
            String chkFlg = "Y";
            if (UtilValidate.isNotEmpty(productActivityForUpdate)) {
                if ("ACTY_AUDIT_PASS".equalsIgnoreCase(productActivityForUpdate.getString("activityAuditStatus"))) {
                    //此刻是已完成状态，需要判断新增商品互斥
                    List<GenericValue> products = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
                    List<String> addProductIds = FastList.newInstance();
                    List<String> oldProductIds = FastList.newInstance();
                    if (products != null && products.size() > 0) {
                        for (GenericValue product : products) {
                            if (UtilValidate.isNotEmpty(product.get("productId"))) {
                                String productId = product.getString("productId");
                                oldProductIds.add(productId);
                            }
                        }
                    }
                    for (String newProductId : productIdList) {
                        if (!oldProductIds.contains(newProductId)) {
                            addProductIds.add(newProductId);
                        }
                    }
                    if (UtilValidate.isNotEmpty(addProductIds)) {
                        //check新增的商品存不存在其他促销活动中
                        List<String> existProductIds = ProPromoService.checkProductPromoExist(delegator, productStoreId, addProductIds, activityStartDate, activityEndDate);
                        if (UtilValidate.isNotEmpty(existProductIds)) {
                            chkFlg = "N";
                            result.put("chkFlg", chkFlg);
                            result.put("errorMsg", "商品id为：" + Joiner.on(",").join(existProductIds) + "的商品该时间段内已经在其他活动中！");
                            return result;
                        }
                    }
                }
            }
        }catch (GenericEntityException e){

            return ServiceUtil.returnError(e.getMessage());
        }


        //删除团购信息除了主表
        try {
            ret = dispatcher.runSync("deleteGroupOrderWithoutMain", UtilMisc.toMap("activityId", activityId));
        } catch (GenericServiceException e) {
            if (ServiceUtil.isError(ret)) {
                return ret;
            }
        }

        //修改productActivity
        List<GenericValue> allStore = new LinkedList<GenericValue>();
        GenericValue productActivity = delegator.makeValue("ProductActivity");
        productActivity.set("activityName", activityName);
        productActivity.set("activityStartDate", activityStartDate);
        productActivity.set("activityEndDate", activityEndDate);
        productActivity.set("limitQuantity", limitQuantity);
        if(!"ACTY_AUDIT_PASS".equalsIgnoreCase(productActivityForUpdate.getString("activityAuditStatus"))){
            productActivity.set("activityAuditStatus", "ACTY_AUDIT_INIT");
        }
        productActivity.set("activityDesc", activityDesc);

        productActivity.set("isAnyReturn", isAnyReturn);
        productActivity.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
        productActivity.set("isSupportScore", isSupportScore);
        productActivity.set("isSupportReturnScore", isSupportReturnScore);
        productActivity.set("isShowIndex", isShowIndex);
        productActivity.set("isPostageFree", isPostageFree);
        try {
            productActivity.set("activityId", activityId);
            productActivity.store();
            //遍历团购商品，生成对应的规则。
            List<String> productLines = StringUtil.split(productLineList, "|");
            int i = 0;
            for (String prodLine : productLines) {
                String[] lineStrs = prodLine.split(",");
                String productId = lineStrs[0];
//                String activityQuantity= lineStrs[1];
                String activityPersonNum = lineStrs[1];
                String activityPrice = lineStrs[2];

                GenericValue productActivityGoods = delegator.makeValue("ProductActivityGoods");
                productActivityGoods.set("productId", productId);
                productActivityGoods.set("isAnyReturn", isAnyReturn);
                productActivityGoods.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
                productActivityGoods.set("isSupportScore", isSupportScore);
                productActivityGoods.set("isSupportReturnScore", isSupportReturnScore);
                productActivityGoods.set("isShowIndex", isShowIndex);
                productActivityGoods.set("isPostageFree", isPostageFree);
                productActivityGoods.set("activityId", activityId);
                productActivityGoods.set("activityQuantity", new Long(activityPersonNum));
                productActivityGoods.set("activityPrice", new BigDecimal(activityPrice));
                allStore.add(productActivityGoods);

                //创建团购促销
                String promoName = activityName + ":促销" + (i + 1);
                String promoText = promoName;
                String userEntered = "Y";
                String showToCustomer = "Y";
                String requireCode = "N";
                Long useLimitPerOrder = 1L;
                Long useLimitPerCustomer = 1L;
                Map inputParams = FastMap.newInstance();
                inputParams.put("promoName", promoName);
                inputParams.put("promoText", promoText);
                inputParams.put("userEntered", userEntered);
                inputParams.put("showToCustomer", showToCustomer);
                inputParams.put("requireCode", requireCode);
                inputParams.put("useLimitPerOrder", useLimitPerOrder);
                inputParams.put("useLimitPerCustomer", useLimitPerCustomer);
                inputParams.put("userLogin", userLogin);
                Map<String, Object> callback = null;
                try {
                    callback = dispatcher.runSync("createProductPromo", inputParams);
                    String productPromoId = (String) callback.get("productPromoId");
                    //创建促销规则
                    String ruleName = activityName + ":促销条件" + (i + 1);
                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoId", productPromoId);
                    inputParams.put("ruleName", ruleName);
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoRule", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }
                    String productPromoRuleId = (String) callback.get("productPromoRuleId");
                    //创建createProductPromoCond
                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoRuleId", productPromoRuleId);
                    inputParams.put("productPromoId", productPromoId);

                    inputParams.put("inputParamEnumId", "PPIP_GRPODR_TOTAL");
                    inputParams.put("operatorEnumId", "PPC_GTE");
                    inputParams.put("condValue", activityPersonNum);
//                    inputParams.put("otherValue", RQuantity);
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoCond", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }
                    String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");

                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoRuleId", productPromoRuleId);
                    inputParams.put("productPromoId", productPromoId);
                    inputParams.put("productPromoCondSeqId", productPromoCondSeqId);
                    inputParams.put("productPromoActionSeqId", "_NA_");
                    inputParams.put("productId", productId);
                    inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }
                    //创建createProductPromoAction
                    inputParams = FastMap.newInstance();
                    inputParams.put("productPromoCondSeqId", productPromoCondSeqId);
                    inputParams.put("productPromoRuleId", productPromoRuleId);
                    inputParams.put("productPromoId", productPromoId);
                    inputParams.put("productPromoActionEnumId", "PROMO_PROD_ASGPC");
                    inputParams.put("orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT");
                    inputParams.put("quantity", BigDecimal.ONE);
                    inputParams.put("amount", new BigDecimal(activityPrice));
                    inputParams.put("useCartQuantity", "N");
                    inputParams.put("userLogin", userLogin);
                    callback = dispatcher.runSync("createProductPromoAction", inputParams);
                    if (ServiceUtil.isError(callback)) {
                        return result;
                    }

                    //创建产品团购规则对应每个促销
                    GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                    productGroupOrderRule.set("activityId", activityId);
                    productGroupOrderRule.set("seqId", delegator.getNextSeqId("ProductGroupOrderRule"));
//                    productGroupOrderRule.set("orderQuantity", lQuantity);
//                    productGroupOrderRule.set("orderPrice", price);
                    productGroupOrderRule.set("productPromoId", productPromoId);
                    allStore.add(productGroupOrderRule);


                } catch (GenericServiceException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError(e.getMessage());
                }
                if (ServiceUtil.isError(callback)) {
                    return result;
                }

                i++;
            }

            delegator.storeAll(allStore);
            //创建团购和店铺的关联
            GenericValue storeValue = delegator.makeValue("ProductStoreProductActAppl", UtilMisc.toMap("productStoreId", productStoreId, "activityId", activityId
                    , "fromDate", activityStartDate, "thruDate", activityEndDate));
            storeValue.create();


        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 查找活动
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findActivities(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String activityCode = (String) context.get("activityCode");
        String activityName = (String) context.get("activityName");
        String activityStartDate = (String) context.get("activityStartDate");
        String activityEndDate = (String) context.get("activityEndDate");
        String activityType = (String) context.get("activityType");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        Delegator delegator = dcx.getDelegator();

        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");


        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        List<GenericValue> activityList = FastList.newInstance();
        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;

        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 10;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 10;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PA", "ProductActivity");
        dynamicView.addAlias("PA", "activityId");
        dynamicView.addAlias("PA", "activityCode");
        dynamicView.addAlias("PA", "activityName");
        dynamicView.addAlias("PA", "activityStartDate");
        dynamicView.addAlias("PA", "activityEndDate");
        dynamicView.addAlias("PA", "activityAuditStatus");
        dynamicView.addAlias("PA", "activityType");
        dynamicView.addAlias("PA", "hasGroup");
        dynamicView.addAlias("PA", "leaveQuantity");
        dynamicView.addAlias("PA", "hasBuyQuantity");
        dynamicView.addAlias("PA", "publishDate");
        dynamicView.addAlias("PA", "endDate");
        dynamicView.addAlias("PA", "createdStamp");


        dynamicView.addMemberEntity("PSPA", "ProductStoreProductActAppl");
        dynamicView.addAlias("PSPA", "activityId");
        dynamicView.addAlias("PSPA", "productStoreId");
        dynamicView.addViewLink("PA", "PSPA", false, ModelKeyMap.makeKeyMapList("activityId", "activityId"));

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("activityId");
        fieldsToSelect.add("activityCode");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("activityType");
        fieldsToSelect.add("hasGroup");
        fieldsToSelect.add("leaveQuantity");
        fieldsToSelect.add("hasBuyQuantity");
        fieldsToSelect.add("publishDate");
        fieldsToSelect.add("endDate");

        List<String> orderBy = FastList.newInstance();
        orderBy.add("-createdStamp");

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(activityCode)) {
            paramList = paramList + "&activityCode=" + activityCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + activityCode + "%")));
        }
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityType"), EntityOperator.EQUALS, activityType));
        if (UtilValidate.isNotEmpty(activityName)) {
            paramList = paramList + "&activityName=" + activityName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + activityName + "%")));
        }
        if (UtilValidate.isNotEmpty(activityAuditStatus)) {
            paramList = paramList + "&activityAuditStatus=" + activityAuditStatus;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, activityAuditStatus));
        }

        List<GenericValue> activities = null;
        //关联店铺
        andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            boolean beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            activityList = pli.getPartialList(lowIndex, viewSize);
            listSize = pli.getResultsSizeAfterPartialList();
            TransactionUtil.commit(beganTransaction);
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg);
            return ServiceUtil.returnError(e.getMessage());
        }

        if (highIndex > listSize) {
            highIndex = listSize;
        }
        result.put("groupList", activityList);
        result.put("activityCode", activityCode);
        result.put("activityName", activityName);
        result.put("activityStartDate", activityStartDate);
        result.put("activityEndDate", activityEndDate);
        result.put("activityAuditStatus", activityAuditStatus);

        result.put("groupListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }


    /**
     * 查询活动
     * d_productStoreName
     * d_activityTypeName
     * d_activityAuditStatusName
     * d_activityCode
     * d_activityName
     * d_publicDate
     * d_endDate
     * d_activityStartDate
     * d_activityEndDate
     * d_limitQuantity
     * d_activityQuantity
     * d_productName
     * d_shipmentTypeName
     * d_scoreValue
     * d_activityPayTypeName
     * d_productPrice
     * d_virtualProductStartDate
     * d_isAnyReturn
     * d_isSupportOverTimeReturn
     * d_isSupportScore
     * d_isSupportReturnScore
     * d_isShowIndex
     * d_activityDesc
     * d_productGroupOrderRules
     * d_productActivityPartyLevels
     * d_productActivityAreas
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> productActivityDetail(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        String productActivityId = (String) context.get("activityId");

        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        String d_activityTypeName;

        String d_activityAuditStatusName = "";
        String d_activityCode;
        String d_activityName;
        String d_activityStartDate;
        String d_activityEndDate;
        String d_limitQuantity;
        String d_shipmentTypeName = null;
        String d_isAnyReturn = null;
        String d_isSupportOverTimeReturn = null;
        String d_isSupportScore = null;
        String d_isSupportReturnScore = null;
        String d_isShowIndex = null;
        String d_isPostageFree = null;
        String d_activityDesc = null;
        try {
            GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", productActivityId));
//            String activityType = (String) productActivity.get("activityType");
//            GenericValue activityEnum = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", activityType));
//            d_activityTypeName = (String) activityEnum.get("description", locale);//团购
            d_activityTypeName = "拼团";
            String activityAuditStatus = (String) productActivity.get("activityAuditStatus");//当前团购状态
            Timestamp nowDate = UtilDateTime.nowTimestamp();
            if ("ACTY_AUDIT_PASS".equals(activityAuditStatus) && (nowDate.before((Timestamp) productActivity.get("activityStartDate")))) {
                d_activityAuditStatusName = "待发布";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.before((Timestamp) productActivity.get("activityStartDate")))) {
                d_activityAuditStatusName = "未开始";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.after((Timestamp) productActivity.get("activityStartDate"))) && (nowDate.before((Timestamp) productActivity.get("activityEndDate")))) {
                d_activityAuditStatusName = "进行中";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.after((Timestamp) productActivity.get("activityEndDate"))) && (nowDate.before((Timestamp) productActivity.get("activityEndDate")))) {
                d_activityAuditStatusName = "已结束";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.after((Timestamp) productActivity.get("activityEndDate")))) {
                d_activityAuditStatusName = "已下架";
            } else {
                GenericValue activityAuditEnum = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", activityAuditStatus));
                d_activityAuditStatusName = (String) activityAuditEnum.get("description", locale);
            }
            d_activityCode = (String) productActivity.get("activityCode");
            d_activityName = (String) productActivity.get("activityName");
            d_activityStartDate = UtilDateTime.timeStampToString((Timestamp) productActivity.get("activityStartDate"), "yyyy-MM-dd HH:mm", TimeZone.getDefault(), locale);
            d_activityEndDate = UtilDateTime.timeStampToString((Timestamp) productActivity.get("activityEndDate"), "yyyy-MM-dd HH:mm", TimeZone.getDefault(), locale);
            d_limitQuantity = productActivity.get("limitQuantity") == null ? "" : productActivity.get("limitQuantity").toString();
            d_activityDesc = productActivity.get("activityDesc") == null ? "无" : (String) productActivity.get("activityDesc");
            //对应产品
            d_isAnyReturn = (String) productActivity.get("isAnyReturn");
            d_isSupportOverTimeReturn = (String) productActivity.get("isSupportOverTimeReturn");
            d_isSupportScore = (String) productActivity.get("isSupportScore");
            d_isSupportReturnScore = (String) productActivity.get("isSupportReturnScore");
            d_isShowIndex = (String) productActivity.get("isShowIndex");
            d_isPostageFree = (String) productActivity.get("isPostageFree");

            List<Map> productList = new ArrayList<Map>();
            List<GenericValue> groupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", productActivityId));
            for (GenericValue orderRule : groupOrderRules) {
                String productPromoId = orderRule.getString("productPromoId");
                //查找对应的促销主表
                GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                GenericValue productPromoRule = productPromoRules.get(0);
                //查找条件里面的团购人数
                String ruleId = (String) productPromoRule.get("productPromoRuleId");
                GenericValue promoCond = delegator.findByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId)).get(0);
                String activityPersonNum = promoCond.getString("condValue");//团购人数

                String productPromoCondSeqId = (String) promoCond.get("productPromoCondSeqId");
                GenericValue promoAction = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId)).get(0);
                BigDecimal activityPrice = promoAction.getBigDecimal("amount");//团购金额


                //查找当前促销对应的产品id
                GenericValue productPromoProduct = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId)).get(0);
                String productId = productPromoProduct.getString("productId");
                Map ret = dispatcher.runSync("getProductGoodById", UtilMisc.toMap("id", productId));
                ret.put("activityPersonNum", activityPersonNum);
                ret.put("activityPrice", activityPrice);
                //根据产品找到对应的活动数量
//                GenericValue activityGood = delegator.findByAnd("ProductActivityGoods",UtilMisc.toMap("activityId",productActivityId,"productId",productId)).get(0);
                GenericValue activityGood = delegator.findByPrimaryKey("ProductActivityGoods", UtilMisc.toMap("activityId", productActivityId, "productId", productId));
                Long activityQuantity = Long.parseLong("0");
                if (UtilValidate.isNotEmpty(activityGood)) {
                    if (UtilValidate.isNotEmpty(activityGood.get("activityQuantity"))) {
                        activityQuantity = activityGood.getLong("activityQuantity");
                    }
                }
                ret.put("activityQuantity", activityQuantity);

                productList.add(ret);
            }
            result.put("productList", productList);
        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        result.put("d_activityTypeName", d_activityTypeName);
        result.put("d_activityAuditStatusName", d_activityAuditStatusName);
        result.put("d_activityCode", d_activityCode);
        result.put("d_activityName", d_activityName);
        result.put("d_activityStartDate", d_activityStartDate);
        result.put("d_activityEndDate", d_activityEndDate);
        result.put("d_limitQuantity", d_limitQuantity);
        result.put("d_isAnyReturn", d_isAnyReturn);
        result.put("d_isSupportOverTimeReturn", d_isSupportOverTimeReturn);
        result.put("d_isSupportScore", d_isSupportScore);
        result.put("d_isSupportReturnScore", d_isSupportReturnScore);
        result.put("d_isShowIndex", d_isShowIndex);
        result.put("d_isPostageFree", d_isPostageFree);
        result.put("d_activityDesc", d_activityDesc);

        return result;
    }

    /**
     * 删除团购
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteGroupOrder(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityId = (String) context.get("activityId");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        //删除存在的促销ProductPromo
        //删除存在的促销ProductPromoRule
        //删除存在的促销ProductPromoRule
        //删除存在的促销ProductPromoCond
        //删除存在的促销ProductPromoAction
        //删除存在的促销ProductStorePromoAppl
        //删除存在的促销ProductGroupOrderRule

        List<GenericValue> o_productGroupOrderRules = null;
        try {
            o_productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId));

            if (UtilValidate.isNotEmpty(o_productGroupOrderRules)) {
                for (GenericValue productOrderRlue : o_productGroupOrderRules) {
                    String o_promoId = (String) productOrderRlue.get("productPromoId");
                    delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductGroupOrderRule", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", o_promoId));
                }
            }
            // 取得团购的产品列表信息
            List<GenericValue> promoProducts=delegator.findByAnd("ProductActivityGoods",UtilMisc.toMap("activityId",activityId));
            List<String>  productIdRedisList=FastList.newInstance();
            if(UtilValidate.isNotEmpty(promoProducts)){
                for(GenericValue gv: promoProducts){
                    if(UtilValidate.isNotEmpty(gv)){
                        String curProductId=gv.getString("productId");
                        if(UtilValidate.isNotEmpty(curProductId)){
                            productIdRedisList.add(curProductId);
                        }
                        // Redis 信息的删除处理
//                        if(UtilRedis.exists(curProductId+"_summary")){
//                            UtilRedis.del(curProductId+"_summary");// 产品缓存
//                        }
//                        if(UtilRedis.exists(curProductId+"_downPromo")){
//                            UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                        }
//                        if(UtilRedis.exists(curProductId+"_groupOrder")){
//                            UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                        }
//                        if(UtilRedis.exists(curProductId+"_seckill")) {
//                            UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                        }

                    }
                }
            }
            if(UtilValidate.isNotEmpty(productIdRedisList)){
                String redisProductStr = Joiner.on(",").join(productIdRedisList);
                Map<String, Object> reParams = FastMap.newInstance();
                reParams.put("productIds", redisProductStr);
                reParams.put("isDel", "Y");
                Map<String, Object> resultMap = FastMap.newInstance();
                try {
                    // 调用服务 productInfoRedisPro
                    resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                } catch (GenericServiceException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
            }

            delegator.removeByAnd("ProductStoreProductActAppl", UtilMisc.toMap("activityId", activityId));
            delegator.removeByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
//            delegator.removeByAnd("ProductActivityPartyLevel", UtilMisc.toMap("activityId", activityId));
//            delegator.removeByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
            delegator.removeByAnd("ProductActivity", UtilMisc.toMap("activityId", activityId));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 删除团购
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteGroupOrderWithoutMain(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityId = (String) context.get("activityId");
        Delegator delegator = dcx.getDelegator();
        //删除存在的促销ProductPromo
        //删除存在的促销ProductPromoRule
        //删除存在的促销ProductPromoRule
        //删除存在的促销ProductPromoCond
        //删除存在的促销ProductPromoAction
        //删除存在的促销ProductStorePromoAppl
        //删除存在的促销ProductGroupOrderRule

        List<GenericValue> o_productGroupOrderRules = null;
        try {
            o_productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId));

            if (UtilValidate.isNotEmpty(o_productGroupOrderRules)) {
                for (GenericValue productOrderRlue : o_productGroupOrderRules) {
                    String o_promoId = (String) productOrderRlue.get("productPromoId");
                    delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductGroupOrderRule", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", o_promoId));
                }
            }
            delegator.removeByAnd("ProductStoreProductActAppl", UtilMisc.toMap("activityId", activityId));
            delegator.removeByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
//            delegator.removeByAnd("ProductActivityPartyLevel", UtilMisc.toMap("activityId", activityId));
//            delegator.removeByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
//            delegator.removeByAnd("ProductActivity", UtilMisc.toMap("activityId", activityId));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    public Map<String, Object> editTogetherEndDate(DispatchContext dct, Map<String, ? extends Object> context) throws GenericEntityException {
        Delegator delegator = dct.getDelegator();
        LocalDispatcher dispatcher = dct.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();

        String activityId = (String) context.get("activityId");
        String productStoreId = (String) context.get("productStoreId");
        Timestamp endDate = (Timestamp) context.get("activityEndDate");

        List<GenericValue> promos = delegator.findByAnd("ProductStorePromoAndAct", UtilMisc.toMap("activityId", activityId));
        if (UtilValidate.isEmpty(promos)) {
            result.put("chkFlg", "N");
            result.put("errorMsg", "查找不到该团购促销");
            return result;
        }
        GenericValue promo = promos.get(0);
        Timestamp activityEndDate = promo.getTimestamp("activityEndDate");
        Timestamp activityStartDate = promo.getTimestamp("activityStartDate");
        List<String>  productIdRedisList=FastList.newInstance();
        // 取得团购产品列表信息
        List<GenericValue> promoProducts=delegator.findByAnd("ProductActivityGoods",UtilMisc.toMap("activityId",activityId));
        if (endDate.getTime() < activityEndDate.getTime()) {
            //说明时间未过期可以直接修改
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("activityEndDate", endDate);
            EntityCondition UpdateCon = EntityCondition.makeCondition("activityId", EntityComparisonOperator.EQUALS, activityId);
            delegator.storeByCondition("ProductActivity", updateFields, UpdateCon);

            if(UtilValidate.isNotEmpty(promoProducts)){

                for(GenericValue gv: promoProducts){
                    if(UtilValidate.isNotEmpty(gv)){
                        String curProductId=gv.getString("productId");
                        if(UtilValidate.isNotEmpty(curProductId)){
                            productIdRedisList.add(curProductId);
                        }
                        // Redis 信息的删除处理
//                        if(UtilRedis.exists(curProductId+"_summary")){
//                            UtilRedis.del(curProductId+"_summary");// 产品缓存
//                        }
//                        if(UtilRedis.exists(curProductId+"_downPromo")){
//                            UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                        }
//                        if(UtilRedis.exists(curProductId+"_groupOrder")){
//                            UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                        }
//                        if(UtilRedis.exists(curProductId+"_seckill")) {
//                            UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                        }
                    }
                }
                if(UtilValidate.isNotEmpty(productIdRedisList)){
                    String redisProductStr = Joiner.on(",").join(productIdRedisList);
                    Map<String, Object> reParams = FastMap.newInstance();
                    reParams.put("productIds", redisProductStr);
                    reParams.put("isDel", "N");
                    Map<String, Object> resultMap = FastMap.newInstance();
                    try {
                        // 调用服务 productInfoRedisPro
                        resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                    } catch (GenericServiceException e) {
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }
            }
            result.put("chkFlg", "Y");
            return result;
        }

        //判断有没有商品在活动中
        List<GenericValue> products = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
        List<String> productIds = FastList.newInstance();
        if (products != null && products.size() > 0) {
            for (GenericValue product : products) {
                if (UtilValidate.isNotEmpty(product.get("productId"))) {
                    String productId = product.getString("productId");
                    productIds.add(productId);
                }
            }
        }
        List<String> unUserdProductIdInfos = ProductServices.getUnUsedProductIds(delegator, productStoreId,activityStartDate,endDate);
        String chkFlg = "Y";
        List<String> productIdExists = FastList.newInstance();
        if (UtilValidate.isNotEmpty(productIds)) {
            for (String curProcutId : productIds) {
                if (!unUserdProductIdInfos.contains(curProcutId)) {
                    continue;
                } else {
                    chkFlg = "N";
                    productIdExists.add(curProcutId);
                }
            }
        }else{
            if(UtilValidate.isNotEmpty(unUserdProductIdInfos)){
                chkFlg = "N";
                productIdExists.addAll(unUserdProductIdInfos);
            }
        }
//        String existProductStr = Joiner.on(",").join(productIdExists);

        result.put("chkFlg", chkFlg);

        if ("N".equalsIgnoreCase(chkFlg)) {
            result.put("errorMsg", "当前时间已存在指定商品的促销!");
        }

        if("Y".equalsIgnoreCase(chkFlg)){
            boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator,productStoreId,activityStartDate,endDate);
            if(isPromoAllExist){
                chkFlg = "N";
                result.put("chkFlg", chkFlg);
                result.put("errorMsg", "当前时间已存在全场促销");
            }
        }

        if ("Y".equalsIgnoreCase(chkFlg)) {
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("activityEndDate", endDate);
            EntityCondition UpdateCon = EntityCondition.makeCondition("activityId", EntityComparisonOperator.EQUALS, activityId);
            delegator.storeByCondition("ProductActivity", updateFields, UpdateCon);
            // 删除产品Redis信息
            if(UtilValidate.isNotEmpty(promoProducts)){
                productIdRedisList=FastList.newInstance();
                for(GenericValue gv: promoProducts){
                    if(UtilValidate.isNotEmpty(gv)){
                        String curProductId=gv.getString("productId");
                        if(UtilValidate.isNotEmpty(curProductId)){
                            productIdRedisList.add(curProductId);
                        }
                        // Redis 信息的删除处理
//                        if(UtilRedis.exists(curProductId+"_summary")){
//                            UtilRedis.del(curProductId+"_summary");// 产品缓存
//                        }
//                        if(UtilRedis.exists(curProductId+"_downPromo")){
//                            UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                        }
//                        if(UtilRedis.exists(curProductId+"_groupOrder")){
//                            UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                        }
//                        if(UtilRedis.exists(curProductId+"_seckill")) {
//                            UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                        }
                    }
                }
                if(UtilValidate.isNotEmpty(productIdRedisList)){
                    String redisProductStr = Joiner.on(",").join(productIdRedisList);
                    Map<String, Object> reParams = FastMap.newInstance();
                    reParams.put("productIds", redisProductStr);
                    reParams.put("isDel", "N");
                    Map<String, Object> resultMap = FastMap.newInstance();
                    try {
                        // 调用服务 productInfoRedisPro
                        resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                    } catch (GenericServiceException e) {
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }
            }

        }

        return result;
    }
}
