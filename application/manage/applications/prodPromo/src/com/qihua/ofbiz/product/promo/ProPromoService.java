package com.qihua.ofbiz.product.promo;

import com.google.common.base.Joiner;
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
import org.ofbiz.product.product.ProductServices;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by changsy on 16/1/9.
 */
public class ProPromoService {

    public static final String module = ProPromoService.class.getName();


    public Map<String, Object> findSecKill(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        return result;
    }

    public Map<String, Object> addGroupOrder(DispatchContext dcx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreIds = (String) context.get("productStoreIds");
        String activityCode = (String) context.get("activityCode");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        String activityType = (String) context.get("activityType");
        String activityName = (String) context.get("activityName");
        Timestamp publishDate = (Timestamp) context.get("publishDate");
        Timestamp endDate = (Timestamp) context.get("endDate");
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        Long limitQuantity = (Long) context.get("limitQuantity");
        Long activityQuantity = (Long) context.get("activityQuantity");
        Long scoreValue = (Long) context.get("scoreValue");
        String activityPayType = (String) context.get("activityPayType");
        String activityDesc = (String) context.get("activityDesc");
        BigDecimal productPrice = (BigDecimal) context.get("productPrice");

        String productId = (String) context.get("productId");
        String shipmentType = (String) context.get("shipmentType");
        Timestamp virtualProductStartDate = (Timestamp) context.get("virtualProductStartDate");
        Timestamp virtualProductEndDate = (Timestamp) context.get("virtualProductEndDate");
        String isAnyReturn = (String) context.get("isAnyReturn");
        String isSupportOverTimeReturn = (String) context.get("isSupportOverTimeReturn");
        String isSupportScore = (String) context.get("isSupportScore");
        String isSupportReturnScore = (String) context.get("isSupportReturnScore");
        String isShowIndex = (String) context.get("isShowIndex");
        // Add by zhajh at 20160318 包邮 Begin
        String isPostageFree = (String) context.get("isPostageFree");
        // Add by zhajh at 20160318 包邮 End
        String productGroupOrderRules = (String) context.get("productGroupOrderRules");
        String productActivityPartyLevels = (String) context.get("productActivityPartyLevels");
        String productActivityAreas = (String) context.get("productActivityAreas");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        //创建productActivity
        Delegator delegator = dcx.getDelegator();

        List<GenericValue> allStore = new LinkedList<GenericValue>();
        GenericValue productActivity = delegator.makeValue("ProductActivity");
        productActivity.set("activityCode", activityCode);
        productActivity.set("activityAuditStatus", activityAuditStatus);
        productActivity.set("activityName", activityName);
        productActivity.set("activityType", activityType);
        productActivity.set("publishDate", publishDate);
        productActivity.set("endDate", endDate);
        productActivity.set("activityStartDate", activityStartDate);
        productActivity.set("activityEndDate", activityEndDate);
        productActivity.set("limitQuantity", limitQuantity);
        productActivity.set("activityQuantity", activityQuantity);
        productActivity.set("leaveQuantity", activityQuantity);
        productActivity.set("hasGroup", "N");
        productActivity.set("scoreValue", scoreValue);
        productActivity.set("productPrice", productPrice);
        productActivity.set("activityPayType", activityPayType);
        productActivity.set("activityDesc", activityDesc);
        productActivity.set("hasBuyQuantity", 0L);
        productActivity.set("occupiedQuantityTotal", 0L);
        // Add by zhajh at 20160317 更新价格标记 Begin
        productActivity.set("isUpdatePrice", "Y");
        // Add by zhajh at 20160317 更新价格标记 End
        String promoId = "";
        String activityId = delegator.getNextSeqId("ProductActivity");

        try {
            productActivity.set("activityId", activityId);
            productActivity.create();

            //创建对应商品
            GenericValue productActivityGoods = delegator.makeValue("ProductActivityGoods");
            productActivityGoods.set("productId", productId);
            productActivityGoods.set("shipmentType", shipmentType);
            productActivityGoods.set("isAnyReturn", isAnyReturn);
            productActivityGoods.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
            productActivityGoods.set("isSupportScore", isSupportScore);
            productActivityGoods.set("isSupportReturnScore", isSupportReturnScore);
            productActivityGoods.set("isShowIndex", isShowIndex);
            // Add by zhajh at 20160318 包邮 Begin
            productActivityGoods.set("isPostageFree", isPostageFree);
            // Add by zhajh at 20160318 包邮 End
            productActivityGoods.set("virtualProductStartDate", virtualProductStartDate);
            productActivityGoods.set("virtualProductEndDate", virtualProductEndDate);
            productActivityGoods.set("activityId", activityId);
            allStore.add(productActivityGoods);
            //创建对应ProductGroupOrderRule
            if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                List<String> rules = StringUtil.split(productGroupOrderRules, ",");
                if (UtilValidate.isNotEmpty(rules)) {
                    for (int i = 0; i < rules.size(); i++) {
                        String rule = rules.get(i);
                        if (UtilValidate.isNotEmpty(rule)) {
                            List<String> ruleObj = StringUtil.split(rule, ":");
                            if (UtilValidate.isNotEmpty(ruleObj)) {
                                String seq = ruleObj.get(0);
                                String quantity = ruleObj.get(1);
                                Long lQuantity = Long.parseLong(quantity);
                                BigDecimal price = new BigDecimal(ruleObj.get(2));

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

                                try {
                                    Map<String, Object> callback = dispatcher.runSync("createProductPromo", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }

                                    String productPromoId = (String) callback.get("productPromoId");
                                    //创建createProductPromoRule
                                    String ruleName = activityName + ":促销条件" + (i + 1);
                                    inputParams = FastMap.newInstance();
                                    inputParams.put("productPromoId", productPromoId);
                                    inputParams.put("ruleName", ruleName);
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoRule", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    String RQuantity = null;
                                    String productPromoRuleId = (String) callback.get("productPromoRuleId");
//                                    获取阶梯价的next值
                                    if (i < (rules.size() - 1)) {
                                        if (UtilValidate.isNotEmpty(rules.get(i + 1))) {
                                            String nextRule = rules.get(i + 1);
                                            List<String> nextRuleObj = StringUtil.split(nextRule, ":");
                                            if (UtilValidate.isNotEmpty(nextRuleObj)) {
                                                RQuantity = nextRuleObj.get(1);

                                            }
                                        }
                                    }
                                    //创建createProductPromoCond
                                    inputParams = FastMap.newInstance();
                                    inputParams.put("productPromoRuleId", productPromoRuleId);
                                    inputParams.put("productPromoId", productPromoId);

                                    inputParams.put("inputParamEnumId", "PPIP_GRPODR_TOTAL");
                                    inputParams.put("operatorEnumId", "PPC_BTW");
                                    inputParams.put("condValue", lQuantity.toString());
                                    inputParams.put("otherValue", RQuantity);
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoCond", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");
                                    //创建条件产品
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
                                    inputParams.put("amount", price);
                                    inputParams.put("useCartQuantity", "N");
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoAction", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    //创建ProductPromoProduct
                                    String productPromoActionSeqId = (String) callback.get("productPromoActionSeqId");
                                    inputParams = FastMap.newInstance();
                                    inputParams.put("productPromoRuleId", productPromoRuleId);
                                    inputParams.put("productPromoId", productPromoId);
                                    inputParams.put("productPromoCondSeqId", "_NA_");
                                    inputParams.put("productPromoActionSeqId", productPromoActionSeqId);
                                    inputParams.put("productId", productId);
                                    inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    //创建促销对应店铺createProductStorePromoAppl

                                    if (UtilValidate.isNotEmpty(productStoreIds)) {
                                        List storeIds = StringUtil.split(productStoreIds, ",");
                                        for (int j = 0; j < storeIds.size(); j++) {
                                            String productStoreId = (String) storeIds.get(j);
                                            inputParams = FastMap.newInstance();
                                            inputParams.put("productStoreId", productStoreId);
                                            inputParams.put("productPromoId", productPromoId);
                                            inputParams.put("fromDate", UtilDateTime.nowTimestamp());
                                            inputParams.put("userLogin", userLogin);
                                            callback = dispatcher.runSync("createProductStorePromoAppl", inputParams);
                                            if (ServiceUtil.isError(callback)) {
                                                return result;
                                            }

                                        }

                                    }

                                    //创建产品团购规则对应每个促销
                                    GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                                    productGroupOrderRule.set("activityId", activityId);
                                    productGroupOrderRule.set("seqId", seq);
                                    productGroupOrderRule.set("orderQuantity", lQuantity);
                                    productGroupOrderRule.set("orderPrice", price);
                                    productGroupOrderRule.set("productPromoId", productPromoId);
                                    allStore.add(productGroupOrderRule);

                                } catch (GenericServiceException e) {
                                    return ServiceUtil.returnError(e.getMessage());
                                }


                            }

                        }
                    }
                }
            }
            //创建对应ProductPartyLevel
            if (UtilValidate.isNotEmpty(productActivityPartyLevels)) {
                List<String> levels = StringUtil.split(productActivityPartyLevels, ",");
                if (UtilValidate.isNotEmpty(levels)) {
                    for (int i = 0; i < levels.size(); i++) {
                        String level = levels.get(i);
                        List<String> levelObj = StringUtil.split(level, ":");
                        if (UtilValidate.isNotEmpty(levelObj)) {
                            String levelId = levelObj.get(0);
                            String levelName = levelObj.get(1);
                            GenericValue activityPartyLevel = delegator.makeValue("ProductActivityPartyLevel");
                            activityPartyLevel.set("levelId", levelId);
                            activityPartyLevel.set("levelName", levelName);
                            activityPartyLevel.set("activityId", activityId);
                            allStore.add(activityPartyLevel);
                        }
                    }
                }
            }
            //productActivityAreas
            if (UtilValidate.isNotEmpty(productActivityAreas)) {
                List<String> areas = StringUtil.split(productActivityAreas, ",");
                if (UtilValidate.isNotEmpty(areas)) {
                    for (int i = 0; i < areas.size(); i++) {
                        String area = areas.get(i);
                        List<String> areaObj = StringUtil.split(area, ":");
                        if (UtilValidate.isNotEmpty(areaObj)) {
                            String geoId = "";
                            String geoName = "";
                            if (areaObj.size() > 1) {
                                geoId = areaObj.get(0);
                                geoName = areaObj.get(1);
                            } else {
                                geoId = areaObj.get(0);
                                geoName = "";
                            }
                            GenericValue productActivityArea = delegator.makeValue("ProductActivityArea");
                            productActivityArea.set("communityId", geoId);
                            productActivityArea.set("communityName", geoName);
                            productActivityArea.set("activityId", activityId);
                            allStore.add(productActivityArea);
                        }
                    }
                }
            }

            //

            delegator.storeAll(allStore);

        } catch (GenericEntityException e) {

            return ServiceUtil.returnError(e.getMessage());
        }
        result.put("activityId", activityId);

        return result;
    }


    public Map<String, Object> updateGroupOrder(DispatchContext dcx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityId = (String) context.get("activityId");
        String productStoreIds = (String) context.get("productStoreIds");
        String activityCode = (String) context.get("activityCode");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        String activityType = (String) context.get("activityType");
        String activityName = (String) context.get("activityName");
        Timestamp publishDate = (Timestamp) context.get("publishDate");
        Timestamp endDate = (Timestamp) context.get("endDate");
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        Long limitQuantity = (Long) context.get("limitQuantity");
        Long activityQuantity = (Long) context.get("activityQuantity");
        Long scoreValue = (Long) context.get("scoreValue");
        String activityPayType = (String) context.get("activityPayType");
        String activityDesc = (String) context.get("activityDesc");
        BigDecimal productPrice = (BigDecimal) context.get("productPrice");

        String productId = (String) context.get("productId");
        String shipmentType = (String) context.get("shipmentType");
        Timestamp virtualProductStartDate = (Timestamp) context.get("virtualProductStartDate");
        Timestamp virtualProductEndDate = (Timestamp) context.get("virtualProductEndDate");
        String isAnyReturn = (String) context.get("isAnyReturn");
        String isSupportOverTimeReturn = (String) context.get("isSupportOverTimeReturn");
        String isSupportScore = (String) context.get("isSupportScore");
        String isSupportReturnScore = (String) context.get("isSupportReturnScore");
        String isShowIndex = (String) context.get("isShowIndex");
        // Add by zhajh at 20160318 包邮 Begin
        String isPostageFree = (String) context.get("isPostageFree");
        // Add by zhajh at 20160318 包邮 End
        String productGroupOrderRules = (String) context.get("productGroupOrderRules");
        String productActivityPartyLevels = (String) context.get("productActivityPartyLevels");
        String productActivityAreas = (String) context.get("productActivityAreas");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        // Add by zhajh at 20160317  更新价格标识 Begin
        String isUpdatePrice = (String) context.get("isUpdatePrice");
        // Add by zhajh at 20160317  更新价格标识 End
        //创建productActivity
        Delegator delegator = dcx.getDelegator();
        try {
            //        若将活动总数量减至小于已购买数量，建议给予提示，‘不能小于已购数量'
            GenericValue pActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
            Long db_hasBuyQuantity = (Long) pActivity.get("hasBuyQuantity");
            if (activityQuantity.compareTo(db_hasBuyQuantity) <= 0) {
                return ServiceUtil.returnError("活动数量不能小于已购数量");
            }

            List<GenericValue> allStore = new LinkedList<GenericValue>();
            GenericValue productActivity = delegator.makeValue("ProductActivity");
            productActivity.set("activityCode", activityCode);
            productActivity.set("activityAuditStatus", "ACTY_AUDIT_INIT");
            productActivity.set("activityName", activityName);
            productActivity.set("activityType", activityType);
            productActivity.set("publishDate", publishDate);
            productActivity.set("endDate", endDate);
            productActivity.set("activityStartDate", activityStartDate);
            productActivity.set("activityEndDate", activityEndDate);
            productActivity.set("limitQuantity", limitQuantity);
            productActivity.set("activityQuantity", activityQuantity);
            productActivity.set("scoreValue", scoreValue);
            productActivity.set("productPrice", productPrice);
            productActivity.set("activityPayType", activityPayType);
            productActivity.set("activityDesc", activityDesc);
            // Add by zhajh at 20160317  更新价格标识 Begin
            if (UtilValidate.isNotEmpty(isUpdatePrice)) {
                productActivity.set("isUpdatePrice", isUpdatePrice);
            }
            // Add by zhajh at 20160317  更新价格标识 End
            productActivity.set("activityId", activityId);

            allStore.add(productActivity);

            //修改对应商品
            delegator.removeByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));

            GenericValue productActivityGoods = delegator.makeValue("ProductActivityGoods");
            productActivityGoods.set("productId", productId);
            productActivityGoods.set("shipmentType", shipmentType);
            productActivityGoods.set("isAnyReturn", isAnyReturn);
            productActivityGoods.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
            productActivityGoods.set("isSupportScore", isSupportScore);
            productActivityGoods.set("isSupportReturnScore", isSupportReturnScore);
            productActivityGoods.set("isShowIndex", isShowIndex);
            // Add by zhajh at 20160318 包邮 Begin
            productActivityGoods.set("isPostageFree", isPostageFree);
            // Add by zhajh at 20160318 包邮 End
            productActivityGoods.set("virtualProductStartDate", virtualProductStartDate);
            productActivityGoods.set("virtualProductEndDate", virtualProductEndDate);
            productActivityGoods.set("activityId", activityId);
            allStore.add(productActivityGoods);


            //删除存在的促销ProductPromo
            //删除存在的促销ProductPromoRule
            //删除存在的促销ProductPromoRule
            //删除存在的促销ProductPromoCond
            //删除存在的促销ProductPromoAction
            //删除存在的促销ProductStorePromoAppl
            //删除存在的促销ProductGroupOrderRule

            List<GenericValue> o_productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId));
            if (UtilValidate.isNotEmpty(o_productGroupOrderRules)) {
                for (GenericValue productOrderRlue : o_productGroupOrderRules) {
                    String o_promoId = (String) productOrderRlue.get("productPromoId");

                    delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", o_promoId));
                    List<GenericValue> appls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", o_promoId));
                    if (UtilValidate.isNotEmpty(appls)) {
                        for (int i = 0; i < appls.size(); i++) {
                            GenericValue genericValue = appls.get(i);
                            delegator.removeValue(genericValue);
                        }
                    }
                    delegator.removeByAnd("ProductGroupOrderRule", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", o_promoId));
                }
            }

            //创建对应ProductGroupOrderRule
            if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                List<String> rules = StringUtil.split(productGroupOrderRules, ",");
                if (UtilValidate.isNotEmpty(rules)) {
                    for (int i = 0; i < rules.size(); i++) {
                        String rule = rules.get(i);
                        if (UtilValidate.isNotEmpty(rule)) {
                            List<String> ruleObj = StringUtil.split(rule, ":");
                            if (UtilValidate.isNotEmpty(ruleObj)) {
                                String seq = ruleObj.get(0);
                                String quantity = ruleObj.get(1);
                                Long lQuantity = Long.parseLong(quantity);
                                BigDecimal price = new BigDecimal(ruleObj.get(2));

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

                                try {
                                    Map<String, Object> callback = dispatcher.runSync("createProductPromo", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    String productPromoId = (String) callback.get("productPromoId");
                                    //创建createProductPromoRule
                                    String ruleName = activityName + ":促销条件" + (i + 1);
                                    inputParams = FastMap.newInstance();
                                    inputParams.put("productPromoId", productPromoId);
                                    inputParams.put("ruleName", ruleName);
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoRule", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    String RQuantity = null;
                                    String productPromoRuleId = (String) callback.get("productPromoRuleId");
//                                    获取阶梯价的next值
                                    if (i < (rules.size() - 1)) {
                                        if (UtilValidate.isNotEmpty(rules.get(i + 1))) {
                                            String nextRule = rules.get(i + 1);
                                            List<String> nextRuleObj = StringUtil.split(nextRule, ":");
                                            if (UtilValidate.isNotEmpty(nextRuleObj)) {
                                                RQuantity = nextRuleObj.get(1);

                                            }
                                        }
                                    }
                                    //创建createProductPromoCond
                                    inputParams = FastMap.newInstance();
                                    inputParams.put("productPromoRuleId", productPromoRuleId);
                                    inputParams.put("productPromoId", productPromoId);

                                    inputParams.put("inputParamEnumId", "PPIP_GRPODR_TOTAL");
                                    inputParams.put("operatorEnumId", "PPC_BTW");
                                    inputParams.put("condValue", lQuantity.toString());
                                    inputParams.put("otherValue", RQuantity);
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoCond", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");
                                    //创建条件产品
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
                                    inputParams.put("amount", price);
                                    inputParams.put("useCartQuantity", "N");
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoAction", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    //创建ProductPromoProduct
                                    String productPromoActionSeqId = (String) callback.get("productPromoActionSeqId");
                                    inputParams = FastMap.newInstance();
                                    inputParams.put("productPromoRuleId", productPromoRuleId);
                                    inputParams.put("productPromoId", productPromoId);
                                    inputParams.put("productPromoCondSeqId", "_NA_");
                                    inputParams.put("productPromoActionSeqId", productPromoActionSeqId);
                                    inputParams.put("productId", productId);
                                    inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                                    inputParams.put("userLogin", userLogin);
                                    callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                                    if (ServiceUtil.isError(callback)) {
                                        return result;
                                    }
                                    //创建促销对应店铺createProductStorePromoAppl

                                    if (UtilValidate.isNotEmpty(productStoreIds)) {
                                        List storeIds = StringUtil.split(productStoreIds, ",");
                                        for (int j = 0; j < storeIds.size(); j++) {
                                            String productStoreId = (String) storeIds.get(j);
                                            inputParams = FastMap.newInstance();
                                            inputParams.put("productStoreId", productStoreId);
                                            inputParams.put("productPromoId", productPromoId);
                                            inputParams.put("fromDate", UtilDateTime.nowTimestamp());
                                            inputParams.put("userLogin", userLogin);
                                            callback = dispatcher.runSync("createProductStorePromoAppl", inputParams);
                                            if (ServiceUtil.isError(callback)) {
                                                return result;
                                            }

                                        }

                                    }
                                    //创建促销与活动关系
                                    GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                                    productGroupOrderRule.set("activityId", activityId);
                                    productGroupOrderRule.set("seqId", seq);
                                    productGroupOrderRule.set("orderQuantity", lQuantity);
                                    productGroupOrderRule.set("orderPrice", price);
                                    productGroupOrderRule.set("productPromoId", productPromoId);
                                    allStore.add(productGroupOrderRule);

                                } catch (GenericServiceException e) {
                                    return ServiceUtil.returnError(e.getMessage());
                                }
                            }

                        }
                    }
                }
            }
            delegator.removeByAnd("ProductActivityPartyLevel", UtilMisc.toMap("activityId", activityId));
            //创建对应ProductPartyLevel
            if (UtilValidate.isNotEmpty(productActivityPartyLevels)) {
                List<String> levels = StringUtil.split(productActivityPartyLevels, ",");
                if (UtilValidate.isNotEmpty(levels)) {
                    for (int i = 0; i < levels.size(); i++) {
                        String level = levels.get(i);
                        List<String> levelObj = StringUtil.split(level, ":");
                        if (UtilValidate.isNotEmpty(levelObj)) {
                            String levelId = levelObj.get(0);
                            String levelName = levelObj.get(1);
                            GenericValue activityPartyLevel = delegator.makeValue("ProductActivityPartyLevel");
                            activityPartyLevel.set("levelId", levelId);
                            activityPartyLevel.set("levelName", levelName);
                            activityPartyLevel.set("activityId", activityId);
                            allStore.add(activityPartyLevel);
                        }
                    }
                }
            }

            delegator.removeByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
            //productActivityAreas
            if (UtilValidate.isNotEmpty(productActivityAreas)) {
                List<String> areas = StringUtil.split(productActivityAreas, ",");
                if (UtilValidate.isNotEmpty(areas)) {
                    for (int i = 0; i < areas.size(); i++) {
                        String area = areas.get(i);
                        List<String> areaObj = StringUtil.split(area, ":");
                        if (UtilValidate.isNotEmpty(areaObj)) {
                            String geoId = areaObj.get(0);
                            String geoName = areaObj.get(1);
                            GenericValue productActivityArea = delegator.makeValue("ProductActivityArea");
                            productActivityArea.set("communityId", geoId);
                            productActivityArea.set("communityName", geoName);
                            productActivityArea.set("activityId", activityId);
                            allStore.add(productActivityArea);
                        }
                    }
                }
            }

            //
            delegator.storeAll(allStore);

        } catch (GenericEntityException e) {

            return ServiceUtil.returnError(e.getMessage());
        }
        result.put("activityId", activityId);

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
        String activityCode = (String) context.get("activityCode");
        String activityName = (String) context.get("activityName");
        String activityStartDate = (String) context.get("activityStartDate");
        String activityEndDate = (String) context.get("activityEndDate");
        String activityType = (String) context.get("activityType");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
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


        List<GenericValue> activityList = FastList.newInstance();
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
        // Add by zhajh at 20160317 更新价格标记 Begin
        fieldsToSelect.add("isUpdatePrice");
        // Add by zhajh at 20160317 更新价格标记 End

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("createdStamp" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(activityCode)) {
            paramList = paramList + "&activityCode=" + activityCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + activityCode + "%")));
        }
        if (UtilValidate.isNotEmpty(activityType)) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityType"), EntityOperator.EQUALS, activityType));
        }
        if (UtilValidate.isNotEmpty(activityName)) {
            paramList = paramList + "&activityName=" + activityName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + activityName + "%")));
        }
        if (UtilValidate.isNotEmpty(activityAuditStatus)) {
            paramList = paramList + "&activityAuditStatus=" + activityAuditStatus;
            Timestamp noTime = UtilDateTime.nowTimestamp();
            if ("ACTY_AUDIT_PUBING".equals(activityAuditStatus)) {
                //待发布（auditStatus为审批通过并且系统当前时间小于发布时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("publishDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_UNBEGIN".equals(activityAuditStatus)) {
                //未开始（auditStatus为审批通过并且系统当前时间小于销售开始时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("publishDate"), EntityOperator.LESS_THAN, noTime));
            } else if ("ACTY_AUDIT_DOING".equals(activityAuditStatus)) {
                //进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.LESS_THAN, noTime));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_END".equals(activityAuditStatus)) {
                //已结束（auditStatus为审批通过并且系统当前时间大于等于销售结束时间小于下架时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.LESS_THAN, noTime));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_OFF".equals(activityAuditStatus)) {
                //已下架（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.LESS_THAN, noTime));
            } else {
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, activityAuditStatus));
            }
        }
        if (UtilValidate.isNotEmpty(activityStartDate)) {
            paramList = paramList + "&activityStartDate=" + activityStartDate;
            Object startDate = null;
            try {
                startDate = ObjectType.simpleTypeConvert(activityStartDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.GREATER_THAN_EQUAL_TO, startDate));
        }
        if (UtilValidate.isNotEmpty(activityEndDate)) {
            paramList = paramList + "&activityEndDate=" + activityEndDate;
            Object endDate = null;
            try {
                endDate = ObjectType.simpleTypeConvert(activityEndDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.LESS_THAN_EQUAL_TO, endDate));
        }
        List<GenericValue> activities = null;
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
                EntityListIterator pli = delegator.find("ProductActivity", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

                // get the partial list for this page
                activityList = pli.getPartialList(lowIndex, viewSize);

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

    public static Map<String, Object> updateGroupOrderPrice(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityId = (String) context.get("activityId");
        Delegator delegator = dcx.getDelegator();

        if (UtilValidate.isNotEmpty(activityId)) {
            try {
                GenericValue activity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
                Timestamp activityEndDate = (Timestamp) activity.get("activityEndDate");
                Timestamp endDate = (Timestamp) activity.get("endDate");
                Timestamp nowTime = UtilDateTime.nowTimestamp();
                if (activityEndDate.before(nowTime) || endDate.before(nowTime)) {
                    Long hasBuyQuantity = (Long) activity.get("hasBuyQuantity");
                    List<GenericValue> orderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId), UtilMisc.toList("seqId"));
                    if (UtilValidate.isNotEmpty(orderRules)) {
                        BigDecimal lastPrice = BigDecimal.ZERO;
                        for (int i = 0; i < orderRules.size(); i++) {
                            GenericValue orderRule = orderRules.get(i);
                            Long ruleQuantity = (Long) orderRule.get("orderQuantity");
                            BigDecimal rulePrice = (BigDecimal) orderRule.get("orderPrice");
                            if (i == 0) {
                                lastPrice = rulePrice;
                            } else {
                                if (hasBuyQuantity.compareTo(ruleQuantity) >= 0) {
                                    lastPrice = rulePrice;
                                }
                            }
                        }

                        if (lastPrice.compareTo(BigDecimal.ZERO) > 0) {
                            //查询出所有order状态不为取消的订单(ITEM_CANCELLED)，lastPriceUnit 改成对应的lastPrice
                            List<EntityCondition> conditions = FastList.newInstance();
                            conditions.add(EntityCondition.makeCondition("activityId", EntityOperator.EQUALS, activityId));
                            conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ITEM_CANCELLED"));
                            EntityFindOptions options = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
                            List<GenericValue> orderItems = delegator.findList("OrderItem", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, options, false);
                            if (UtilValidate.isNotEmpty(orderItems)) {
                                // Mod by zhajh at 20160317 2033 团购列表中点击“更新价格”后，订单不更新价格，余额也没有增加 Begin
//                                for(GenericValue orderItem : orderItems){
//                                    orderItem.set("lastUnitPrice",lastPrice);
//                                }
//                                delegator.storeAll(orderItems);
//
//                                //更新会员金额，add by dongxiao 2016.3.11
//                                String orderId = orderItems.get(0).getString("orderId");
//                                GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
//                                BigDecimal actualPayMoney = UtilValidate.isNotEmpty(orderHeader.getBigDecimal("actualPayMoney"))? orderHeader.getBigDecimal("actualPayMoney") : BigDecimal.ZERO ;    //实付金额
//                                BigDecimal shouldPayMoney = orderHeader.getBigDecimal("shouldPayMoney");    //应付金额
//                                if (actualPayMoney.compareTo(shouldPayMoney) > 0){
//                                    BigDecimal needReturnMoney =  actualPayMoney.subtract(shouldPayMoney);
//                                    //更新会员余额
//                                    GenericValue orderRole = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", "BILL_TO_CUSTOMER")).get(0);
//                                    String partyId = orderRole.getString("partyId");
//                                    GenericValue partyAccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", partyId));
//                                    if (partyAccount == null) {
//                                        partyAccount = delegator.makeValidValue("PartyAccount", UtilMisc.toMap("partyId", partyId, "amount", needReturnMoney, "createDate", UtilDateTime.nowTimestamp()));
//                                    } else {
//                                        partyAccount.set("amount", partyAccount.getBigDecimal("amount").add(needReturnMoney));
//                                    }
//                                    //保存总余额
//                                    delegator.createOrStore(partyAccount);
//                                    //保存明细
//                                    String message = "活动编码："+ activity.getString("activityCode") + "，订单号：" + orderId + "阶梯退款：" + needReturnMoney + "元";
//                                    GenericValue userLogin = (GenericValue) context.get("userLogin");
//                                    delegator.create("PartyAccountDetail", UtilMisc.toMap("detailId", delegator.getNextSeqId("PartyAccountDetail"), "partyId", partyId,
//                                            "amount", needReturnMoney, "description", message, "operator", userLogin.get("userLoginId"), "createDate", UtilDateTime.nowTimestamp()));
//                                }
                                //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                for (GenericValue orderItem : orderItems) {
                                    orderItem.set("lastUnitPrice", lastPrice);
                                    orderItem.store();
                                    //更新会员金额，add by dongxiao 2016.3.11
                                    String orderId = orderItem.getString("orderId");
                                    GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
                                    BigDecimal actualPayMoney = UtilValidate.isNotEmpty(orderHeader.getBigDecimal("actualPayMoney")) ? orderHeader.getBigDecimal("actualPayMoney") : BigDecimal.ZERO;    //实付金额
                                    BigDecimal shouldPayMoney = orderItem.getBigDecimal("quantity").multiply(lastPrice);  //应付金额
                                    if (actualPayMoney.compareTo(shouldPayMoney) > 0) {
                                        BigDecimal needReturnMoney = actualPayMoney.subtract(shouldPayMoney);
                                        //更新会员余额
                                        GenericValue orderRole = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", "BILL_TO_CUSTOMER")).get(0);
                                        String partyId = orderRole.getString("partyId");
                                        GenericValue partyAccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", partyId));
                                        if (partyAccount == null) {
                                            partyAccount = delegator.makeValidValue("PartyAccount", UtilMisc.toMap("partyId", partyId, "amount", needReturnMoney, "createDate", UtilDateTime.nowTimestamp()));
                                        } else {
                                            partyAccount.set("amount", partyAccount.getBigDecimal("amount").add(needReturnMoney));
                                        }
                                        //保存总余额
                                        delegator.createOrStore(partyAccount);
                                        //保存明细
                                        String message = "活动编码：" + activity.getString("activityCode") + "，订单号：" + orderId + "阶梯退款：" + needReturnMoney + "元";
                                        GenericValue userLogin = (GenericValue) context.get("userLogin");
                                        delegator.create("PartyAccountDetail", UtilMisc.toMap("detailId", delegator.getNextSeqId("PartyAccountDetail"), "partyId", partyId,
                                                "amount", needReturnMoney, "description", message, "operator", userLogin.get("userLoginId"), "createDate", UtilDateTime.nowTimestamp()));
                                    }

                                }
                                // Mod by zhajh at 20160317 2033 团购列表中点击“更新价格”后，订单不更新价格，余额也没有增加 End


                            }
                        }
                        // Add by zhajh at 20160317 Begin
                        // 更新团购活动价格变更标记
                        activity.set("isUpdatePrice", "N");
                        activity.store();
                        // Add by zhajh at 20160317 End
                    }
                } else {
                    return ServiceUtil.returnError("当前活动未结束或活动未下架");
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            }
        }

        return result;
    }

    /**
     * 删除活动
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteGroupOrder(DispatchContext dcx, Map<String, ? extends Object> context) {

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
                    List<GenericValue> appls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", o_promoId));
                    if (UtilValidate.isNotEmpty(appls)) {
                        for (int i = 0; i < appls.size(); i++) {
                            GenericValue genericValue = appls.get(i);
                            delegator.removeValue(genericValue);
                        }
                    }
                    delegator.removeByAnd("ProductGroupOrderRule", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", o_promoId));
                }
            }
            delegator.removeByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
            delegator.removeByAnd("ProductActivityPartyLevel", UtilMisc.toMap("activityId", activityId));
            delegator.removeByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
            delegator.removeByAnd("ProductActivity", UtilMisc.toMap("activityId", activityId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据活动ID活动有效的活动订单数
     *
     * @param dcx
     * @param context
     * @return活动订单数
     */
    public static int orderNum = 0;


    public static Map<String, Object> getAvailableGroupOrderQuantity(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> returnRet = ServiceUtil.returnSuccess();

        Delegator delegator = dcx.getDelegator();
        String productId = (String) context.get("productId");
        String quantity = (String) context.get("quantity");
        String activityId = (String) context.get("groupOrderId");
        String partyId = (String) context.get("partyId");
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        //查询该活动已下单数量
        BigDecimal buyNum = new BigDecimal(quantity);
        String productType = (String) product.get("productTypeId");
        if ("FINISHED_GOOD".equals(productType)) {
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
            entityConditionList.add(EntityCondition.makeCondition("partyId", partyId));
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
                        buyNum = buyNum.add(result.getBigDecimal("quantity"));
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        } else if ("VIRTUAL_GOOD".equals(productType)) {
            List<GenericValue> tickets = new ArrayList<GenericValue>();
            List<EntityCondition> ticketConditiona = FastList.newInstance();
            ticketConditiona.add(EntityCondition.makeCondition("partyId", partyId));
            ticketConditiona.add(EntityCondition.makeCondition("activityId", activityId));
            ticketConditiona.add(EntityCondition.makeCondition("ticketStatus", EntityOperator.IN, UtilMisc.toList("notUsed", "hasUsed", "notAudited", "notRefunded", "rejectApplication", "expired")));
            try {
                tickets = delegator.findList("Ticket", EntityCondition.makeCondition(ticketConditiona, EntityOperator.AND), null, null, null, false);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(tickets)) {
                buyNum = buyNum.add(new BigDecimal(tickets.size()));
            }
        }
        returnRet.put("quantities", buyNum);
        return returnRet;
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
        TimeZone timezone = (TimeZone) context.get("timeZone");
        Delegator delegator = dcx.getDelegator();
        String d_productStoreName;
        String d_activityTypeName;
        String activityType;
        String d_activityAuditStatusName = "";
        String d_activityCode;
        String d_activityName;
        String d_publishDate;
        String d_endDate;
        String d_activityStartDate;
        String d_activityEndDate;
        String d_limitQuantity;
        String d_activityQuantity;
        String d_productName = null;
        String d_shipmentTypeName = null;
        String d_scoreValue;
        String d_activityPayTypeName;
        String d_productPrice;
        String d_virtualProductStartDate = null;
        String d_virtualProductEndDate = null;
        String d_isAnyReturn = null;
        String d_isSupportOverTimeReturn = null;
        String d_isSupportScore = null;
        String d_isSupportReturnScore = null;
        String d_isShowIndex = null;
        // Add by zhajh at 20160318 包邮 Begin
        String d_isPostageFree = null;
        // Add by zhajh at 20160318 包邮 End
        String d_activityDesc;
        String d_productType = null;
        String activityAuditStatus;
        String shipmentType = null;
        String activityPayType = null;
        // Add by zhajh at 20160317 是否更新标记 Begin
        String d_isUpdatePrice = null;
        // Add by zhajh at 20160317 是否更新标记 End
        List<GenericValue> d_productGroupOrderRules = null;
        List<GenericValue> d_productActivityPartyLevels = null;
        List<GenericValue> d_productActivityAreas = null;
        List<String> stores = new ArrayList<String>();
        String productId = null;
        try {
            GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", productActivityId));
            activityType = (String) productActivity.get("activityType");
            GenericValue activityEnum = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", activityType));
            d_activityTypeName = (String) activityEnum.get("description", locale);

            activityAuditStatus = (String) productActivity.get("activityAuditStatus");

            Timestamp nowDate = UtilDateTime.nowTimestamp();
            if ("ACTY_AUDIT_PASS".equals(activityAuditStatus) && (nowDate.before((Timestamp) productActivity.get("publishDate")))) {
                d_activityAuditStatusName = "待发布";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.before((Timestamp) productActivity.get("activityStartDate")))) {
                d_activityAuditStatusName = "未开始";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.after((Timestamp) productActivity.get("activityStartDate"))) && (nowDate.before((Timestamp) productActivity.get("activityEndDate")))) {
                d_activityAuditStatusName = "进行中";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.after((Timestamp) productActivity.get("activityEndDate"))) && (nowDate.before((Timestamp) productActivity.get("endDate")))) {
                d_activityAuditStatusName = "已结束";
            } else if (("ACTY_AUDIT_PASS".equals(activityAuditStatus)) && (nowDate.after((Timestamp) productActivity.get("endDate")))) {
                d_activityAuditStatusName = "已下架";
            } else {
                GenericValue activityAuditEnum = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", activityAuditStatus));
                d_activityAuditStatusName = (String) activityAuditEnum.get("description", locale);
            }

            d_activityCode = (String) productActivity.get("activityCode");
            d_activityName = (String) productActivity.get("activityName");
            d_publishDate = UtilDateTime.timeStampToString((Timestamp) productActivity.get("publishDate"), "yyyy-MM-dd HH:mm", timezone, locale);
            d_endDate = UtilDateTime.timeStampToString((Timestamp) productActivity.get("endDate"), "yyyy-MM-dd HH:mm", timezone, locale);
            d_activityStartDate = UtilDateTime.timeStampToString((Timestamp) productActivity.get("activityStartDate"), "yyyy-MM-dd HH:mm", timezone, locale);
            d_activityEndDate = UtilDateTime.timeStampToString((Timestamp) productActivity.get("activityEndDate"), "yyyy-MM-dd HH:mm", timezone, locale);
            d_limitQuantity = productActivity.get("limitQuantity") == null ? "" : productActivity.get("limitQuantity").toString();
            d_activityQuantity = productActivity.get("activityQuantity") == null ? "" : productActivity.get("activityQuantity").toString();
            d_scoreValue = productActivity.get("scoreValue") == null ? "" : productActivity.get("scoreValue").toString();

            activityPayType = (String) productActivity.get("activityPayType");
            GenericValue payEnum = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", activityPayType));
            d_activityPayTypeName = (String) payEnum.get("description", locale);

            d_productPrice = productActivity.get("productPrice") == null ? "" : productActivity.get("productPrice").toString();
            if (UtilValidate.isNotEmpty(d_productPrice)) {
                d_productPrice = d_productPrice.substring(0, d_productPrice.length() - 1);
            }
            d_activityDesc = productActivity.get("activityDesc") == null ? "无" : (String) productActivity.get("activityDesc");
            // Add by zhajh at 20160317 是否更新标记 Begin
            d_isUpdatePrice = (String) productActivity.get("isUpdatePrice"); //是否可以价格更新标记
            // Add by zhajh at 20160317 是否更新标记 End
            //对应产品

            List<GenericValue> goods = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", productActivityId));
            if (UtilValidate.isNotEmpty(goods)) {
                for (GenericValue good : goods) {
                    productId = (String) good.get("productId");
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    d_productName = (String) product.get("productName");
                    shipmentType = (String) good.get("shipmentType");
                    GenericValue shipEnum = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", shipmentType));
                    if (UtilValidate.isNotEmpty(shipEnum)) {
                        d_shipmentTypeName = (String) shipEnum.get("description", locale);
                    }
                    if (UtilValidate.isNotEmpty(good.get("virtualProductStartDate"))) {
                        d_virtualProductStartDate = UtilDateTime.timeStampToString((Timestamp) good.get("virtualProductStartDate"), "yyyy-MM-dd HH:mm", timezone, locale);
                    }
                    if (UtilValidate.isNotEmpty(good.get("virtualProductEndDate"))) {
                        d_virtualProductEndDate = UtilDateTime.timeStampToString((Timestamp) good.get("virtualProductEndDate"), "yyyy-MM-dd HH:mm", timezone, locale);
                    }
                    d_isAnyReturn = (String) good.get("isAnyReturn");
                    d_isSupportOverTimeReturn = (String) good.get("isSupportOverTimeReturn");
                    d_isSupportScore = (String) good.get("isSupportScore");
                    d_isSupportReturnScore = (String) good.get("isSupportReturnScore");
                    d_isShowIndex = (String) good.get("isShowIndex");
                    // Add by zhajh at 20160318 包邮 Begin
                    d_isPostageFree = (String) good.get("isPostageFree");
                    // Add by zhajh at 20160318 包邮 End
                    d_productType = (String) product.get("productTypeId");

                }
            }

            //对应店铺
//            productActivity.getRelated()
            List<GenericValue> promos = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", productActivityId));

            d_productStoreName = "";
            if (UtilValidate.isNotEmpty(promos)) {
                for (GenericValue promo : promos) {
                    String productPromoId = (String) promo.get("productPromoId");
                    List<GenericValue> appls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
                    appls = EntityUtil.filterByDate(appls);
                    if (UtilValidate.isNotEmpty(appls)) {
                        for (int i = 0; i < appls.size(); i++) {
                            GenericValue app = appls.get(i);
                            GenericValue store = app.getRelatedOne("ProductStore");
                            d_productStoreName = store.get("storeName") + ",";
                            stores.add((String) store.get("productStoreId"));
                        }
                    }
                }
            }

            //d_productGroupOrderRules
            d_productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", productActivityId));

            //d_productActivityPartyLevels
            d_productActivityPartyLevels = delegator.findByAnd("ProductActivityPartyLevel", UtilMisc.toMap("activityId", productActivityId));
            //d_productActivityAreas
            d_productActivityAreas = delegator.findByAnd("ProductActivityArea", UtilMisc.toMap("activityId", productActivityId));

        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        result.put("d_productStoreName", d_productStoreName);
        result.put("d_activityTypeName", d_activityTypeName);
        result.put("activityType", activityType);
        result.put("d_activityAuditStatusName", d_activityAuditStatusName);
        result.put("d_activityCode", d_activityCode);
        result.put("d_activityName", d_activityName);
        result.put("d_publishDate", d_publishDate);
        result.put("d_endDate", d_endDate);
        result.put("d_activityStartDate", d_activityStartDate);
        result.put("d_activityEndDate", d_activityEndDate);
        result.put("d_limitQuantity", d_limitQuantity);
        result.put("d_activityQuantity", d_activityQuantity);
        result.put("d_productName", d_productName);
        result.put("d_shipmentTypeName", d_shipmentTypeName);
        result.put("shipmentType", shipmentType);
        result.put("d_scoreValue", d_scoreValue);
        result.put("d_activityPayTypeName", d_activityPayTypeName);
        result.put("d_productPrice", d_productPrice);
        result.put("d_virtualProductStartDate", d_virtualProductStartDate);
        result.put("d_isAnyReturn", d_isAnyReturn);
        result.put("d_isSupportOverTimeReturn", d_isSupportOverTimeReturn);
        result.put("d_isSupportScore", d_isSupportScore);
        result.put("d_isSupportReturnScore", d_isSupportReturnScore);
        result.put("d_isShowIndex", d_isShowIndex);
        // Add by zhajh at 20160318 包邮 Begin
        result.put("d_isPostageFree", d_isPostageFree);
        // Add by zhajh at 20160318 包邮 End
        result.put("d_activityDesc", d_activityDesc);
        result.put("d_virtualProductEndDate", d_virtualProductEndDate);
        result.put("activityAuditStatus", activityAuditStatus);
        result.put("d_productGroupOrderRules", d_productGroupOrderRules);
        result.put("d_productActivityPartyLevels", d_productActivityPartyLevels);
        result.put("d_productActivityAreas", d_productActivityAreas);

        result.put("productStoreIds", stores);
        result.put("productId", productId);
        result.put("activityPayType", activityPayType);
        result.put("d_productType", d_productType);
        // Add by zhajh at 20160317 是否更新标记 Begin
        result.put("d_isUpdatePrice", d_isUpdatePrice);
        // Add by zhajh at 20160317 是否更新标记 End

        return result;
    }

    /**
     * 审批活动
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> auditGroupOrder(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityId = (String) context.get("activityId");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        String message = (String) context.get("auditMessage");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String userLoginId = userLogin.getString("userLoginId");
        try {
            GenericValue auditLog = delegator.makeValue("PromoAuditLog");
            auditLog.setNextSeqId();
            String businessId = "go_" + activityId;
            auditLog.put("businessId", businessId);
            auditLog.put("auditType", "ACTY_AUDIT_PASS".equals(activityAuditStatus) ? "通过" : "驳回");
            auditLog.put("auditPerson", userLoginId);
            auditLog.put("auditMessage", message);
            auditLog.put("createDate", new Timestamp(System.currentTimeMillis()));
            auditLog.create();

            GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));

            if (UtilValidate.isNotEmpty(productActivity)) {
                productActivity.set("activityAuditStatus", activityAuditStatus);
                if (UtilValidate.isNotEmpty(message)) {
                    productActivity.set("auditMessage", message);
                } else {
                    productActivity.set("auditMessage", message);
                }
                productActivity.store();

                // 取得团购的产品列表信息
                List<GenericValue> promoProducts = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
                List<String> productIdRedisList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(promoProducts)) {
                    for (GenericValue gv : promoProducts) {
                        if (UtilValidate.isNotEmpty(gv)) {
                            String curProductId = gv.getString("productId");
                            if (UtilValidate.isNotEmpty(curProductId)) {
                                productIdRedisList.add(curProductId);
                            }

                            // Redis 信息的删除处理
//                            if(UtilRedis.exists(curProductId+"_summary")){
//                                UtilRedis.del(curProductId+"_summary");// 产品缓存
//                            }
//                            if(UtilRedis.exists(curProductId+"_downPromo")){
//                                UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                            }
//                            if(UtilRedis.exists(curProductId+"_groupOrder")){
//                                UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                            }
//                            if(UtilRedis.exists(curProductId+"_seckill")) {
//                                UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                            }

                        }
                    }

                    if (UtilValidate.isNotEmpty(productIdRedisList)) {
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

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 秒杀活动 add by wcy
     * 取“推荐到首页”为“是”且“活动状态”为“未开始”“进行中”的秒杀活动
     * 排序逻辑为：“活动状态”+“发布时间”，“未开始”高于“进行中”显示，发布时间根据倒序排序
     */
    public static Map<String, Object> getPromoByCommunity(DispatchContext dcx, Map<String, ? extends Object> context) {
        /** 获取delegator */
        Delegator delegator = dcx.getDelegator();
        /** 响应结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取参数 */
        List<String> communityId = (List<String>) context.get("communityId");
        List<GenericValue> activityList = null;

        /** 定义活动动态视图*/
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
        dynamicViewEntity.addAlias("PA", "activityId");
        dynamicViewEntity.addAlias("PA", "activityAuditStatus");
        dynamicViewEntity.addAlias("PA", "publishDate");
        dynamicViewEntity.addAlias("PA", "activityName");
        dynamicViewEntity.addAlias("PA", "activityStartDate");
        dynamicViewEntity.addAlias("PA", "activityEndDate");
        dynamicViewEntity.addAlias("PA", "productPrice");
        dynamicViewEntity.addAlias("PA", "activityType");

        /** 定义活动商品动态视图 */
        dynamicViewEntity.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicViewEntity.addAlias("PAG", "activityId");
        dynamicViewEntity.addAlias("PAG", "productId");
        //add  by gss
        dynamicViewEntity.addAlias("PAG", "isShowIndex");

        /** 定义商品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");

        /** 定义商品价格动态视图*/
        dynamicViewEntity.addMemberEntity("PR", "ProductPrice");
        dynamicViewEntity.addAlias("PR", "price");
        dynamicViewEntity.addAlias("PR", "productPriceTypeId");

        /** 定义活动区域动态视图 */
        dynamicViewEntity.addMemberEntity("PAA", "ProductActivityArea");
        dynamicViewEntity.addAlias("PAA", "activityId");
        dynamicViewEntity.addAlias("PAA", "communityId");

        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("PA", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        dynamicViewEntity.addViewLink("PAG", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewEntity.addViewLink("PD", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewEntity.addViewLink("PA", "PAA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));


        /** 查询字段 & 排序字段 */
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("activityId");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("publishDate");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("price");
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("productPrice");
        fieldsToSelect.add("productId");

        orderBy.add("-activityAuditStatus");
        orderBy.add("-publishDate");

        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //如果社区编号为空，则查询所有，查询社区对应的活动
        if (UtilValidate.isNotEmpty(communityId)) {
            andExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
        }
        /*  andExprs.add(EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_UNBEGIN"),
                        EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS,"ACTY_AUDIT_DOING")
                ),EntityOperator.OR)
        );*/

        //“活动状态”为 "通过审批",根据时间判断是否“未开始”还是在“进行中”的秒杀活动
        andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        //推荐到首页”为“是”
        andExprs.add(EntityCondition.makeCondition("isShowIndex", EntityOperator.EQUALS, "Y"));
        //活动开始时间和结束时间 判断是否为“进行中”还是 “未开始”

        // andExprs.add(EntityCondition.makeCondition("productPriceTypeId",EntityOperator.EQUALS,"SALE_PRICE"));
        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"));
        andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
            //获取结果集
            activityList = pli.getCompleteList();
            pli.close();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }

        if (activityList == null) {
            activityList = FastList.newInstance();
        }
        result.put("activityList", activityList);
        return result;
    }

    /**
     * 取“推荐到首页”为是的团购
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> getPromoShowIndex(DispatchContext dcx, Map<String, ? extends Object> context) {
        /** 获取delegator */
        Delegator delegator = dcx.getDelegator();
        /** 响应结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        List<GenericValue> activityList = null;
        /** 获取参数 */
        List<String> communityId = (List<String>) context.get("communityId");
        /** 定义活动动态视图*/
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
        dynamicViewEntity.addAlias("PA", "activityId");
        dynamicViewEntity.addAlias("PA", "activityName");
        dynamicViewEntity.addAlias("PA", "productPrice");
        dynamicViewEntity.addAlias("PA", "activityAuditStatus");
        dynamicViewEntity.addAlias("PA", "publishDate");
        dynamicViewEntity.addAlias("PA", "activityStartDate");
        dynamicViewEntity.addAlias("PA", "activityEndDate");
        dynamicViewEntity.addAlias("PA", "activityType");
        //活动总数量
        dynamicViewEntity.addAlias("PA", "activityQuantity");
        //剩余数量
        dynamicViewEntity.addAlias("PA", "leaveQuantity");
        //销售数量
        dynamicViewEntity.addAlias("PA", "hasBuyQuantity");
        /** 定义活动商品动态视图 */
        dynamicViewEntity.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicViewEntity.addAlias("PAG", "productId");
        dynamicViewEntity.addAlias("PAG", "activityId");
        dynamicViewEntity.addAlias("PAG", "isAnyReturn");
        dynamicViewEntity.addAlias("PAG", "isSupportOverTimeReturn");
        dynamicViewEntity.addAlias("PAG", "isSupportScore");
        dynamicViewEntity.addAlias("PAG", "isShowIndex");
        dynamicViewEntity.addAlias("PAG", "isPostageFree");


        /** 定义商品动态视图 */
        dynamicViewEntity.addMemberEntity("PR", "Product");
        dynamicViewEntity.addAlias("PR", "smallImageUrl");
        dynamicViewEntity.addAlias("PR", "productId");
        
        /* *//** 定义商品评价动态视图 *//*
        dynamicViewEntity.addMemberEntity("PRV","ProductReview");
        dynamicViewEntity.addAlias("PRV","productId");
        dynamicViewEntity.addAlias("PRV","score","productRating",null,null,null,"sum");
        dynamicViewEntity.addAlias("PRV","commentNum","productReviewId",null,null,null,"count");*/

        /** 定义活动区域动态视图  add  by gss*/
        dynamicViewEntity.addMemberEntity("PAA", "ProductActivityArea");
        dynamicViewEntity.addAlias("PAA", "activityId");
        dynamicViewEntity.addAlias("PAA", "communityId");

        /** 定义表关联关系 */
        dynamicViewEntity.addViewLink("PA", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        //add
        dynamicViewEntity.addViewLink("PAG", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        //dynamicViewEntity.addViewLink("PR","PRV",Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId","productId"));
        // add by gss
        dynamicViewEntity.addViewLink("PA", "PAA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        /** 查询字段 & 排序字段 */
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("activityId");
        fieldsToSelect.add("publishDate");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("productPrice");
        fieldsToSelect.add("smallImageUrl");
        // fieldsToSelect.add("score");
        //fieldsToSelect.add("commentNum");
        fieldsToSelect.add("productId");
        // add by gss begin
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("isAnyReturn");
        fieldsToSelect.add("isSupportOverTimeReturn");
        fieldsToSelect.add("isSupportScore");
        fieldsToSelect.add("isShowIndex");
        fieldsToSelect.add("activityQuantity");
        fieldsToSelect.add("leaveQuantity");
        fieldsToSelect.add("hasBuyQuantity");
        fieldsToSelect.add("isPostageFree");
        // add by gss end
        orderBy.add("-publishDate");

        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        Date now = new Date();

        //“活动状态”为"审批通过"的团购活动   add  by gss
        andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        //取“推荐到首页”为“是”
        andExprs.add(EntityCondition.makeCondition("isShowIndex", EntityOperator.EQUALS, "Y"));
        //团购
        andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
        // 开始时间小于当前时间 并且结束时间大于当前时间
        andExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()), EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())), EntityOperator.AND));
        //如果社区编号为空，则查询所有，查询社区对应的活动
        if (UtilValidate.isNotEmpty(communityId)) {
            andExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
        }

        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
            //获取结果集
            activityList = pli.getCompleteList();
            pli.close();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (activityList == null) {
            activityList = FastList.newInstance();
        }

        result.put("activityList", activityList);
        return result;
    }

    //    begin秒杀
    public Map<String, Object> addSecKill(DispatchContext dcx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreIds = (String) context.get("productStoreIds");
        String activityCode = (String) context.get("activityCode");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        String activityType = (String) context.get("activityType");
        String activityName = (String) context.get("activityName");
        Timestamp publishDate = (Timestamp) context.get("publishDate");
        Timestamp endDate = (Timestamp) context.get("endDate");
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        Long limitQuantity = (Long) context.get("limitQuantity");
        Long activityQuantity = (Long) context.get("activityQuantity");
        Long scoreValue = (Long) context.get("scoreValue");
        String activityPayType = (String) context.get("activityPayType");
        String activityDesc = (String) context.get("activityDesc");
        BigDecimal productPrice = (BigDecimal) context.get("productPrice");

        String productId = (String) context.get("productId");
        String shipmentType = (String) context.get("shipmentType");
        Timestamp virtualProductStartDate = (Timestamp) context.get("virtualProductStartDate");
        Timestamp virtualProductEndDate = (Timestamp) context.get("virtualProductEndDate");
        String isAnyReturn = (String) context.get("isAnyReturn");
        String isSupportOverTimeReturn = (String) context.get("isSupportOverTimeReturn");
        String isSupportScore = (String) context.get("isSupportScore");
        String isSupportReturnScore = (String) context.get("isSupportReturnScore");
        String isShowIndex = (String) context.get("isShowIndex");
        // Add by zhajh at 20160318 包邮 Begin
        String isPostageFree = (String) context.get("isPostageFree");
        // Add by zhajh at 20160318 包邮 End
        String productGroupOrderRules = (String) context.get("productGroupOrderRules");
        String productActivityPartyLevels = (String) context.get("productActivityPartyLevels");
        String productActivityAreas = (String) context.get("productActivityAreas");
        GenericValue userLogin = (GenericValue) context.get("userLogin");


        try {
            //创建productActivity
            Delegator delegator = dcx.getDelegator();
            //查看是否有其他全场促销
            boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreIds, activityStartDate, activityEndDate);
            if (isPromoAllExist) {
                return ServiceUtil.returnFailure("当前时间已存在全场促销");
            }
            List<GenericValue> allStore = new LinkedList<GenericValue>();
            GenericValue productActivity = delegator.makeValue("ProductActivity");
            productActivity.set("activityCode", activityCode);
            productActivity.set("activityAuditStatus", activityAuditStatus);
            productActivity.set("activityName", activityName);
            productActivity.set("activityType", activityType);
            productActivity.set("publishDate", publishDate);
            productActivity.set("endDate", endDate);
            productActivity.set("activityStartDate", activityStartDate);
            productActivity.set("activityEndDate", activityEndDate);
            productActivity.set("limitQuantity", limitQuantity);
            productActivity.set("activityQuantity", activityQuantity);
            productActivity.set("leaveQuantity", activityQuantity);
            productActivity.set("hasGroup", "N");
            productActivity.set("scoreValue", scoreValue);
            productActivity.set("productPrice", productPrice);
            productActivity.set("activityPayType", activityPayType);
            productActivity.set("activityDesc", activityDesc);
            productActivity.set("hasBuyQuantity", 0L);
            productActivity.set("occupiedQuantityTotal", 0L);
            String activityId = delegator.getNextSeqId("ProductActivity");
            productActivity.set("activityId", activityId);
            productActivity.create();

            //创建对应商品
            GenericValue productActivityGoods = delegator.makeValue("ProductActivityGoods");
            productActivityGoods.set("productId", productId);
            productActivityGoods.set("shipmentType", shipmentType);
            productActivityGoods.set("isAnyReturn", isAnyReturn);
            productActivityGoods.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
            productActivityGoods.set("isSupportScore", isSupportScore);
            productActivityGoods.set("isSupportReturnScore", isSupportReturnScore);
            productActivityGoods.set("isShowIndex", isShowIndex);
            // Add by zhajh at 20160318 包邮 Begin
            productActivityGoods.set("isPostageFree", isPostageFree);
            // Add by zhajh at 20160318 包邮 End
            productActivityGoods.set("virtualProductStartDate", virtualProductStartDate);
            productActivityGoods.set("virtualProductEndDate", virtualProductEndDate);
            productActivityGoods.set("activityId", activityId);
            productActivityGoods.set("activityPrice", productPrice);
            allStore.add(productActivityGoods);
            //创建对应ProductGroupOrderRule

            //创建团购促销
            String promoName = activityName + ":促销";
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
            try {
                Map<String, Object> callback = dispatcher.runSync("createProductPromo", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                String productPromoId = (String) callback.get("productPromoId");
                //创建createProductPromoRule
                String ruleName = activityName + ":促销条件";
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

                inputParams.put("inputParamEnumId", "SALE_TIME_BTW");
                inputParams.put("operatorEnumId", "PPC_BTW");
                inputParams.put("condValue", activityStartDate.toString());
                inputParams.put("otherValue", activityEndDate.toString());
                inputParams.put("userLogin", userLogin);
                callback = dispatcher.runSync("createProductPromoCond", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");
                //创建条件产品
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
                inputParams.put("amount", productPrice);
                inputParams.put("useCartQuantity", "N");
                inputParams.put("userLogin", userLogin);
                callback = dispatcher.runSync("createProductPromoAction", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                //创建ProductPromoProduct
                String productPromoActionSeqId = (String) callback.get("productPromoActionSeqId");
                inputParams = FastMap.newInstance();
                inputParams.put("productPromoRuleId", productPromoRuleId);
                inputParams.put("productPromoId", productPromoId);
                inputParams.put("productPromoCondSeqId", "_NA_");
                inputParams.put("productPromoActionSeqId", productPromoActionSeqId);
                inputParams.put("productId", productId);
                inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                inputParams.put("userLogin", userLogin);
                callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                //创建促销对应店铺createProductStorePromoAppl

                if (UtilValidate.isNotEmpty(productStoreIds)) {
                    List storeIds = StringUtil.split(productStoreIds, ",");
                    for (int j = 0; j < storeIds.size(); j++) {
                        String productStoreId = (String) storeIds.get(j);
                        inputParams = FastMap.newInstance();
                        inputParams.put("productStoreId", productStoreId);
                        inputParams.put("productPromoId", productPromoId);
                        inputParams.put("fromDate", UtilDateTime.nowTimestamp());
                        inputParams.put("userLogin", userLogin);
                        callback = dispatcher.runSync("createProductStorePromoAppl", inputParams);
                        if (ServiceUtil.isError(callback)) {
                            return result;
                        }

                    }

                }

                //创建产品团购规则对应每个促销
                GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                productGroupOrderRule.set("activityId", activityId);
                productGroupOrderRule.set("seqId", "001");
                productGroupOrderRule.set("orderQuantity", 1L);
                productGroupOrderRule.set("orderPrice", productPrice);
                productGroupOrderRule.set("productPromoId", productPromoId);
                allStore.add(productGroupOrderRule);

            } catch (GenericServiceException e) {
                return ServiceUtil.returnError(e.getMessage());
            }


            //创建对应ProductPartyLevel
            if (UtilValidate.isNotEmpty(productActivityPartyLevels)) {
                List<String> levels = StringUtil.split(productActivityPartyLevels, ",");
                if (UtilValidate.isNotEmpty(levels)) {
                    for (int i = 0; i < levels.size(); i++) {
                        String level = levels.get(i);
                        List<String> levelObj = StringUtil.split(level, ":");
                        if (UtilValidate.isNotEmpty(levelObj)) {
                            String levelId = levelObj.get(0);
                            String levelName = levelObj.get(1);
                            GenericValue activityPartyLevel = delegator.makeValue("ProductActivityPartyLevel");
                            activityPartyLevel.set("levelId", levelId);
                            activityPartyLevel.set("levelName", levelName);
                            activityPartyLevel.set("activityId", activityId);
                            allStore.add(activityPartyLevel);
                        }
                    }
                }
            }
            //productActivityAreas
            if (UtilValidate.isNotEmpty(productActivityAreas)) {
                List<String> areas = StringUtil.split(productActivityAreas, ",");
                if (UtilValidate.isNotEmpty(areas)) {
                    for (int i = 0; i < areas.size(); i++) {
                        String area = areas.get(i);
                        List<String> areaObj = StringUtil.split(area, ":");
                        if (UtilValidate.isNotEmpty(areaObj)) {
                            String geoId = areaObj.get(0);
                            String geoName = areaObj.get(1);
                            GenericValue productActivityArea = delegator.makeValue("ProductActivityArea");
                            productActivityArea.set("communityId", geoId);
                            productActivityArea.set("communityName", geoName);
                            productActivityArea.set("activityId", activityId);
                            allStore.add(productActivityArea);
                        }
                    }
                }
            }

            //

            delegator.storeAll(allStore);
            result.put("activityId", activityId);
        } catch (GenericEntityException e) {

            return ServiceUtil.returnError(e.getMessage());
        }


        return result;
    }


    public Map<String, Object> updateSecKill(DispatchContext dcx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityId = (String) context.get("activityId");
        String productStoreIds = (String) context.get("productStoreIds");
        String activityCode = (String) context.get("activityCode");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        String activityType = (String) context.get("activityType");
        String activityName = (String) context.get("activityName");
        Timestamp publishDate = (Timestamp) context.get("publishDate");
        Timestamp endDate = (Timestamp) context.get("endDate");
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        Long limitQuantity = (Long) context.get("limitQuantity");
        Long activityQuantity = (Long) context.get("activityQuantity");
        Long scoreValue = (Long) context.get("scoreValue");
        String activityPayType = (String) context.get("activityPayType");
        String activityDesc = (String) context.get("activityDesc");
        BigDecimal productPrice = (BigDecimal) context.get("productPrice");

        String productId = (String) context.get("productId");
        String shipmentType = (String) context.get("shipmentType");
        Timestamp virtualProductStartDate = (Timestamp) context.get("virtualProductStartDate");
        Timestamp virtualProductEndDate = (Timestamp) context.get("virtualProductEndDate");
        String isAnyReturn = (String) context.get("isAnyReturn");
        String isSupportOverTimeReturn = (String) context.get("isSupportOverTimeReturn");
        String isSupportScore = (String) context.get("isSupportScore");
        String isSupportReturnScore = (String) context.get("isSupportReturnScore");
        String isShowIndex = (String) context.get("isShowIndex");
        // Add by zhajh at 20160318 包邮 Begin
        String isPostageFree = (String) context.get("isPostageFree");
        // Add by zhajh at 20160318 包邮 End
        String productGroupOrderRules = (String) context.get("productGroupOrderRules");
        String productActivityPartyLevels = (String) context.get("productActivityPartyLevels");
        String productActivityAreas = (String) context.get("productActivityAreas");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        //创建productActivity
        Delegator delegator = dcx.getDelegator();

        List<GenericValue> allStore = new LinkedList<GenericValue>();
        GenericValue productActivity = delegator.makeValue("ProductActivity");
        productActivity.set("activityCode", activityCode);
        productActivity.set("activityAuditStatus", "ACTY_AUDIT_INIT");
        productActivity.set("activityName", activityName);
        productActivity.set("activityType", activityType);
        productActivity.set("publishDate", publishDate);
        productActivity.set("endDate", endDate);
        productActivity.set("activityStartDate", activityStartDate);
        productActivity.set("activityEndDate", activityEndDate);
        productActivity.set("limitQuantity", limitQuantity);
        productActivity.set("activityQuantity", activityQuantity);
        productActivity.set("scoreValue", scoreValue);
        productActivity.set("productPrice", productPrice);
        productActivity.set("activityPayType", activityPayType);
        productActivity.set("activityDesc", activityDesc);

        try {
            productActivity.set("activityId", activityId);
            allStore.add(productActivity);

            //修改对应商品
            delegator.removeByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));

            GenericValue productActivityGoods = delegator.makeValue("ProductActivityGoods");
            productActivityGoods.set("productId", productId);
            productActivityGoods.set("shipmentType", shipmentType);
            productActivityGoods.set("isAnyReturn", isAnyReturn);
            productActivityGoods.set("isSupportOverTimeReturn", isSupportOverTimeReturn);
            productActivityGoods.set("isSupportScore", isSupportScore);
            productActivityGoods.set("isSupportReturnScore", isSupportReturnScore);
            productActivityGoods.set("isShowIndex", isShowIndex);
            // Add by zhajh at 20160318 包邮 Begin
            productActivityGoods.set("isPostageFree", isPostageFree);
            // Add by zhajh at 20160318 包邮 End
            productActivityGoods.set("virtualProductStartDate", virtualProductStartDate);
            productActivityGoods.set("virtualProductEndDate", virtualProductEndDate);
            productActivityGoods.set("activityId", activityId);

            allStore.add(productActivityGoods);


            //删除存在的促销ProductPromo
            //删除存在的促销ProductPromoRule
            //删除存在的促销ProductPromoRule
            //删除存在的促销ProductPromoCond
            //删除存在的促销ProductPromoAction
            //删除存在的促销ProductStorePromoAppl
            //删除存在的促销ProductGroupOrderRule

            List<GenericValue> o_productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId));
            if (UtilValidate.isNotEmpty(o_productGroupOrderRules)) {
                for (GenericValue productOrderRlue : o_productGroupOrderRules) {
                    String o_promoId = (String) productOrderRlue.get("productPromoId");

                    delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", o_promoId));
                    List<GenericValue> appls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", o_promoId));
                    if (UtilValidate.isNotEmpty(appls)) {
                        for (int i = 0; i < appls.size(); i++) {
                            GenericValue genericValue = appls.get(i);
                            delegator.removeValue(genericValue);
                        }
                    }
                    delegator.removeByAnd("ProductGroupOrderRule", UtilMisc.toMap("productPromoId", o_promoId));
                    delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", o_promoId));
                }
            }

            //创建团购促销
            String promoName = activityName + ":促销";
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
            try {
                Map<String, Object> callback = dispatcher.runSync("createProductPromo", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                String productPromoId = (String) callback.get("productPromoId");
                //创建createProductPromoRule
                String ruleName = activityName + ":促销条件";
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

                inputParams.put("inputParamEnumId", "SALE_TIME_BTW");
                inputParams.put("operatorEnumId", "PPC_BTW");
                inputParams.put("condValue", activityStartDate.toString());
                inputParams.put("otherValue", activityEndDate.toString());
                inputParams.put("userLogin", userLogin);
                callback = dispatcher.runSync("createProductPromoCond", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");
                //创建条件产品
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
                inputParams.put("amount", productPrice);
                inputParams.put("useCartQuantity", "N");
                inputParams.put("userLogin", userLogin);
                callback = dispatcher.runSync("createProductPromoAction", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                //创建ProductPromoProduct
                String productPromoActionSeqId = (String) callback.get("productPromoActionSeqId");
                inputParams = FastMap.newInstance();
                inputParams.put("productPromoRuleId", productPromoRuleId);
                inputParams.put("productPromoId", productPromoId);
                inputParams.put("productPromoCondSeqId", "_NA_");
                inputParams.put("productPromoActionSeqId", productPromoActionSeqId);
                inputParams.put("productId", productId);
                inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                inputParams.put("userLogin", userLogin);
                callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                if (ServiceUtil.isError(callback)) {
                    return result;
                }
                //创建促销对应店铺createProductStorePromoAppl

                if (UtilValidate.isNotEmpty(productStoreIds)) {
                    List storeIds = StringUtil.split(productStoreIds, ",");
                    for (int j = 0; j < storeIds.size(); j++) {
                        String productStoreId = (String) storeIds.get(j);
                        inputParams = FastMap.newInstance();
                        inputParams.put("productStoreId", productStoreId);
                        inputParams.put("productPromoId", productPromoId);
                        inputParams.put("fromDate", UtilDateTime.nowTimestamp());
                        inputParams.put("userLogin", userLogin);
                        callback = dispatcher.runSync("createProductStorePromoAppl", inputParams);
                        if (ServiceUtil.isError(callback)) {
                            return result;
                        }

                    }

                }

                //创建产品团购规则对应每个促销
                GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                productGroupOrderRule.set("activityId", activityId);
                productGroupOrderRule.set("seqId", "001");
                productGroupOrderRule.set("orderQuantity", 1L);
                productGroupOrderRule.set("orderPrice", productPrice);
                productGroupOrderRule.set("productPromoId", productPromoId);
                allStore.add(productGroupOrderRule);

            } catch (GenericServiceException e) {
                return ServiceUtil.returnError(e.getMessage());
            }

            delegator.removeByAnd("ProductActivityPartyLevel", UtilMisc.toMap("activityId", activityId));
            //创建对应ProductPartyLevel
            if (UtilValidate.isNotEmpty(productActivityPartyLevels)) {
                List<String> levels = StringUtil.split(productActivityPartyLevels, ",");
                if (UtilValidate.isNotEmpty(levels)) {
                    for (int i = 0; i < levels.size(); i++) {
                        String level = levels.get(i);
                        List<String> levelObj = StringUtil.split(level, ":");
                        if (UtilValidate.isNotEmpty(levelObj)) {
                            String levelId = levelObj.get(0);
                            String levelName = levelObj.get(1);
                            GenericValue activityPartyLevel = delegator.makeValue("ProductActivityPartyLevel");
                            activityPartyLevel.set("levelId", levelId);
                            activityPartyLevel.set("levelName", levelName);
                            activityPartyLevel.set("activityId", activityId);
                            allStore.add(activityPartyLevel);
                        }
                    }
                }
            }

            delegator.removeByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
            //productActivityAreas
            if (UtilValidate.isNotEmpty(productActivityAreas)) {
                List<String> areas = StringUtil.split(productActivityAreas, ",");
                if (UtilValidate.isNotEmpty(areas)) {
                    for (int i = 0; i < areas.size(); i++) {
                        String area = areas.get(i);
                        List<String> areaObj = StringUtil.split(area, ":");
                        if (UtilValidate.isNotEmpty(areaObj)) {
                            String geoId = areaObj.get(0);
                            String geoName = areaObj.get(1);
                            GenericValue productActivityArea = delegator.makeValue("ProductActivityArea");
                            productActivityArea.set("communityId", geoId);
                            productActivityArea.set("communityName", geoName);
                            productActivityArea.set("activityId", activityId);
                            allStore.add(productActivityArea);
                        }
                    }
                }
            }

            //
            delegator.storeAll(allStore);

        } catch (GenericEntityException e) {

            return ServiceUtil.returnError(e.getMessage());
        }
        result.put("activityId", activityId);

        return result;
    }

    /**
     * 删除秒杀
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteSecKill(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String curActivityId = (String) context.get("activityId");
        LocalDispatcher dispatcher = dcx.getDispatcher();
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
            if (UtilValidate.isNotEmpty(curActivityId)) {
                String[] arrActivityIds = curActivityId.split(",");
                if (UtilValidate.isNotEmpty(arrActivityIds)) {
                    for (int j = 0; j < arrActivityIds.length; j++) {
                        String activityId = arrActivityIds[j];
                        o_productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId));
                        if (UtilValidate.isNotEmpty(o_productGroupOrderRules)) {
                            for (GenericValue productOrderRlue : o_productGroupOrderRules) {
                                String o_promoId = (String) productOrderRlue.get("productPromoId");

                                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", o_promoId));
                                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", o_promoId));
                                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", o_promoId));
                                delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", o_promoId));
                                List<GenericValue> appls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", o_promoId));
                                if (UtilValidate.isNotEmpty(appls)) {
                                    for (int i = 0; i < appls.size(); i++) {
                                        GenericValue genericValue = appls.get(i);
                                        delegator.removeValue(genericValue);
                                    }
                                }
                                delegator.removeByAnd("ProductGroupOrderRule", UtilMisc.toMap("productPromoId", o_promoId));
                                delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", o_promoId));
                            }
                        }

                        // 取得秒杀的产品列表信息
                        List<GenericValue> promoProducts = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
                        List<String> productIdRedisList = FastList.newInstance();
                        if (UtilValidate.isNotEmpty(promoProducts)) {
                            for (GenericValue gv : promoProducts) {
                                if (UtilValidate.isNotEmpty(gv)) {
                                    String curProductId = gv.getString("productId");

                                    if (UtilValidate.isNotEmpty(curProductId)) {
                                        productIdRedisList.add(curProductId);
                                    }
                                    // Redis 信息的删除处理
//                                    if(UtilRedis.exists(curProductId+"_summary")){
//                                        UtilRedis.del(curProductId+"_summary");// 产品缓存
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_downPromo")){
//                                        UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_groupOrder")){
//                                        UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_seckill")) {
//                                        UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                                    }

                                }
                            }

                            if (UtilValidate.isNotEmpty(productIdRedisList)) {
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

                        }

                        delegator.removeByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
                        delegator.removeByAnd("ProductActivityPartyLevel", UtilMisc.toMap("activityId", activityId));
                        delegator.removeByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
                        delegator.removeByAnd("ProductActivity", UtilMisc.toMap("activityId", activityId));
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 查询促销活动列表，弹出框使用   add by qianjin 2016/01/20
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getPromoListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> recordsList = FastList.newInstance();

        //总记录数
        int recordsListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PA", "ProductActivity");
        dynamicView.addAlias("PA", "activityId");
        dynamicView.addAlias("PA", "activityName");
        dynamicView.addAlias("PA", "activityStartDate");
        dynamicView.addAlias("PA", "activityEndDate");
        dynamicView.addAlias("PA", "activityAuditStatus");
        dynamicView.addAlias("PA", "activityType");

        dynamicView.addMemberEntity("E", "Enumeration");
        dynamicView.addAlias("E", "enumId");
        dynamicView.addAlias("E", "activityTypeName", "description", null, false, null, null);
        dynamicView.addViewLink("PA", "E", false, ModelKeyMap.makeKeyMapList("activityType", "enumId"));

        dynamicView.addMemberEntity("PAA", "ProductActivityArea");
        dynamicView.addAlias("PAA", "paId", "activityId", null, false, null, null);
        dynamicView.addAlias("PAA", "communityId");
        dynamicView.addAlias("PAA", "communityName");
        dynamicView.addViewLink("PA", "PAA", Boolean.TRUE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));

        fieldsToSelect.add("activityId");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("activityType");
        fieldsToSelect.add("activityTypeName");

        //排序字段名称
        String sortField = "activityId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        //按促销类型查询
        if (UtilValidate.isNotEmpty(context.get("activityType"))) {
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, context.get("activityType")));
        }
        //按促销名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("activityName"))) {
            andExprs.add(EntityCondition.makeCondition("activityName", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("activityName") + "%")));
        }

        //按社区查询
        if (UtilValidate.isNotEmpty(context.get("activityArea"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("communityName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("activityArea") + "%")));
        }

        //默认条件
        List<EntityCondition> defaultExprs = FastList.newInstance();
        List<EntityCondition> defaultExprs1 = FastList.newInstance();
        List<EntityCondition> defaultExprs2 = FastList.newInstance();
        //未开始（auditStatus为审批通过并且系统当前时间小于销售开始时间）
        defaultExprs1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        defaultExprs1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        defaultExprs.add(EntityCondition.makeCondition(defaultExprs1, EntityOperator.AND));
        //进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
        defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp()));
        defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        defaultExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.AND));
        andExprs.add(EntityCondition.makeCondition(defaultExprs, EntityOperator.OR));
        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String activityId = gv.getString("activityId");
                String activityName = gv.getString("activityName");
                Timestamp activityStartDate = gv.getTimestamp("activityStartDate");
                Timestamp activityEndDate = gv.getTimestamp("activityEndDate");
                String activityAuditStatus = gv.getString("activityAuditStatus");
                String activityType = gv.getString("activityType");
                String activityTypeName = gv.getString("activityTypeName");

                map.put("activityId", activityId);
                map.put("activityType", activityType);
                map.put("activityTypeName", activityTypeName);
                map.put("activityName", activityName);
                String audityStatus = "";
                if ("ACTY_AUDIT_PASS".equals(activityAuditStatus) && activityStartDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "未开始";
                } else if ("ACTY_AUDIT_PASS".equals(activityAuditStatus) && activityStartDate.before(UtilDateTime.nowTimestamp()) && activityEndDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "进行中";
                }
                map.put("audityStatus", audityStatus);
                //获取社区名称
                String communityName = "";
                List<GenericValue> areaList = delegator.findByAnd("ProductActivityArea", UtilMisc.toMap("activityId", activityId));
                if (UtilValidate.isNotEmpty(areaList)) {
                    for (int i = 0; i < areaList.size(); i++) {
                        String name = areaList.get(i).getString("communityName");
                        if (i == 0) {
                            communityName += name;
                        } else {
                            communityName += "，" + name;
                        }
                    }
                } else {
                    communityName = "全部社区";
                }
                map.put("communityName", communityName);
                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }


        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 获取团购数据  add by gss
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getPromoForWap(DispatchContext dct,
                                                     Map<String, ? extends Object> context) {

        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //进行中的团购
        List<GenericValue> ongoingActivityList = null;
        //未开始的团购
        List<GenericValue> nostartActivityList = null;
        //已售罄的团购
        List<GenericValue> clearoutActivityList = null;
        //结束的团购
        List<GenericValue> finishActivityList = null;

        //默认智能排序  进行中--未开始 -已售--已结束
        List<GenericValue> activityList = FastList.newInstance();

        Map<String, Object> resultData = FastMap.newInstance();
        List<GenericValue> activityLists = FastList.newInstance();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        List<String> communityId = UtilGenerics.cast(context.get("communityId"));
        String classify = (String) context.get("classify");   //根据分类查询产品 然后查询产品对应团购活动用的
        String keyword1 = (String) context.get("keyword");//关键字
        String sort = (String) context.get("sort");

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

        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        //如果分类不为空
        List<String> classiIds = new ArrayList<String>();
        if (UtilValidate.isNotEmpty(classify) && !"all".equals(classify)) {
            classiIds.add(classify);
            GenericValue ProductCategory = null;
            try {
                ProductCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", classify));
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            if ((Long) ProductCategory.get("productCategoryLevel") == 2) {
                try {
                    List<GenericValue> productCategorylist = delegator
                            .findByAnd("ProductCategory", UtilMisc.toMap(
                                    "primaryParentCategoryId",
                                    ProductCategory.get("productCategoryId")));
                    if (UtilValidate.isNotEmpty(productCategorylist)) {
                        for (GenericValue productCategorylists : productCategorylist) {
                            classiIds.add((String) productCategorylists
                                    .get("productCategoryId"));
                        }
                    } else {
                        classiIds.add(classify);
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            if (UtilValidate.isEmpty(classiIds)) {
                resultData.put("max", 0);
                resultData.put("groBuyList", classiIds);
                result.put("resultData", resultData);
                return result;
            }
        }

        List<String> activityIds = new ArrayList<String>();
        if (UtilValidate.isNotEmpty(keyword1)) {
            String keyword = keyword1.replaceAll(" +", "");
            try {
                DynamicViewEntity dev = new DynamicViewEntity();
                //活动商品关联表
                dev.addMemberEntity("PA", "ProductActivity");
                dev.addAlias("PA", "activityType");
                dev.addMemberEntity("PAG", "ProductActivityGoods");
                dev.addAlias("PAG", "activityId");
                dev.addAlias("PAG", "productId");
                /** 定义商品动态视图 */
                dev.addMemberEntity("PD", "Product");
                dev.addAlias("PD", "productId");
                dev.addAlias("PD", "productName");
                dev.addAlias("PD", "internalName");
                dev.addAlias("PD", "productSubheadName");
                dev.addViewLink("PAG", "PA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
                dev.addViewLink("PAG", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
                /** 查询字段 & 排序字段 */
                List<String> fieldsToSelect = FastList.newInstance();
                fieldsToSelect.add("activityId");
                List<EntityCondition> andExprs = FastList.newInstance();
                EntityCondition mainCond = null;
                andExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("productName", EntityOperator.LIKE,
                        "%" + keyword + "%"), EntityCondition.makeCondition("productSubheadName", EntityOperator.LIKE,
                        "%" + keyword + "%")), EntityOperator.OR));
                andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS,
                        "GROUP_ORDER"));//活动类型为团购
                if (andExprs.size() > 0) {
                    mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                }
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
                //获取结果集
                EntityListIterator pli = delegator.findListIteratorByCondition(dev, mainCond, null, fieldsToSelect, null, findOpts);
                List<GenericValue> activityIdLists = pli.getCompleteList();
                pli.close();
                //获取所有符合关键字的团购活动
                if (UtilValidate.isNotEmpty(activityIdLists)) {
                    for (GenericValue activityId : activityIdLists) {
                        if (!activityIds.contains((String) activityId.get("activityId"))) //如果list数组不包括keyword[i]中的值的话，就返回true
                        {
                            activityIds.add((String) activityId.get("activityId")); //在list数组中加入keyword[i]的值。已经过滤过。
                        }
                    }
                }
                //关键字在活动中
                List exprs = FastList.newInstance();
                exprs.add(EntityCondition.makeCondition("activityName", EntityOperator.LIKE,
                        "%" + keyword + "%"));//活动名称
                exprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS,
                        "GROUP_ORDER"));//活动类型为团购
                List<GenericValue> Keywordactivity = delegator.findList("ProductActivity",
                        EntityCondition.makeCondition(exprs, EntityOperator.AND),
                        null, null, null, false);
                if (UtilValidate.isNotEmpty(Keywordactivity)) {
                    for (GenericValue Keywordactivitys : Keywordactivity) {
                        if (!activityIds.contains((String) Keywordactivitys.get("activityId"))) //如果list数组不包括keyword[i]中的值的话，就返回true
                        {
                            activityIds.add((String) Keywordactivitys.get("activityId")); //在list数组中加入keyword[i]的值。已经过滤过。
                        }
                    }
                }
            } catch (GenericEntityException e1) {
                e1.printStackTrace();
            }
            if (UtilValidate.isEmpty(activityIds)) {
                resultData.put("max", 0);
                resultData.put("groBuyList", activityIds);
                result.put("resultData", resultData);
                return result;
            }
        }


        /** 定义团购动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
        dynamicViewEntity.addAlias("PA", "id", "activityId", null, null, null, null);
        dynamicViewEntity.addAlias("PA", "title", "activityName", null, null, null, null);
        dynamicViewEntity.addAlias("PA", "price", "productPrice", null, null, null, null);
        dynamicViewEntity.addAlias("PA", "publishDate");
        dynamicViewEntity.addAlias("PA", "activityAuditStatus");
        dynamicViewEntity.addAlias("PA", "activityType");
        dynamicViewEntity.addAlias("PA", "endDate");
        //活动总数量
        dynamicViewEntity.addAlias("PA", "activityQuantity");
        //剩余数量
        dynamicViewEntity.addAlias("PA", "leaveQuantity");
        dynamicViewEntity.addAlias("PA", "activityStartDate");
        dynamicViewEntity.addAlias("PA", "activityEndDate");
        //平均客户评价
        dynamicViewEntity.addAlias("PA", "averageCustomerRating");
        //销售数量
        dynamicViewEntity.addAlias("PA", "hasBuyQuantity");

        ComplexAlias relevancyComplexAlias = new ComplexAlias("-");
        relevancyComplexAlias.addComplexAliasMember(new ComplexAliasField("PA", "activityQuantity", null, null));
        relevancyComplexAlias.addComplexAliasMember(new ComplexAliasField("PA", "hasBuyQuantity", null, null));
        //剩余数量
        dynamicViewEntity.addAlias(null, "residualQuantity", null, null, null, null, null, relevancyComplexAlias);

        dynamicViewEntity.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicViewEntity.addAlias("PAG", "isAnyReturn");
        dynamicViewEntity.addAlias("PAG", "isSupportOverTimeReturn");
        dynamicViewEntity.addAlias("PAG", "isSupportScore");
        dynamicViewEntity.addAlias("PAG", "isSupportReturnScore");
        dynamicViewEntity.addAlias("PAG", "isShowIndex");
        dynamicViewEntity.addAlias("PAG", "activityId");
        dynamicViewEntity.addAlias("PAG", "productId");

        dynamicViewEntity.addMemberEntity("PAA", "ProductActivityArea");
        dynamicViewEntity.addAlias("PAA", "activityId");
        dynamicViewEntity.addAlias("PAA", "communityId");

        /** 定义商品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        //add by gss 产品分类
        dynamicViewEntity.addAlias("PD", "primaryProductCategoryId");
        /** 定义表关联关系 */
        dynamicViewEntity.addViewLink("PA", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        dynamicViewEntity.addViewLink("PAG", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewEntity.addViewLink("PA", "PAA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        /** 查询字段 & 排序字段 */
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("publishDate");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("title");
        fieldsToSelect.add("price");
        fieldsToSelect.add("isSupportOverTimeReturn");
        fieldsToSelect.add("isSupportScore");
        fieldsToSelect.add("isSupportReturnScore");
        fieldsToSelect.add("isShowIndex");
        fieldsToSelect.add("isAnyReturn");
        fieldsToSelect.add("activityType");
        //fieldsToSelect.add("residualQuantity");
        fieldsToSelect.add("hasBuyQuantity");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("activityQuantity");
        fieldsToSelect.add("leaveQuantity");
        //智能排序
        /*正在进行中团购条数*/
        int ongoingListSize = 0;
        /*已售完团购条数*/
        int clearoutListSize = 0;
        /*正在进行中团购条数*/
        int finishListSize = 0;
        /*未开始的团购条数*/
        int nostartListSize = 0;
        
        /*正在进行中团购总条数*/
        int ongoingAllSize = 0;
        /*已售完团购总条数*/
        int clearoutAllSize = 0;
        /*正在进行中团购总条数*/
        int finishAllSize = 0;
        /*未开始的团购总条数*/
        int nostartAllSize = 0;
        int allsize = 0;

        if ("default".equals(sort)) {
            orderBy.add("-publishDate");//发布时间
            /** 查询条件   正在进行中的 团购 */
            List<EntityCondition> andExprs = FastList.newInstance();
            EntityCondition mainCond = null;
            if (UtilValidate.isNotEmpty(activityIds)) {
                andExprs.add(EntityCondition.makeCondition("id", EntityOperator.IN, activityIds));
            }
            //判断是否有分类并且不是全部
            if (!"all".equals(classify) && UtilValidate.isNotEmpty(classiIds)) {
                //商品分类
                andExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, classiIds));
            }
            andExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            //“活动状态”为"审批通过"的团购活动   add  by gss
            andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            // 团购正在进行中---开始时间小于当前时间 并且结束时间大于当前时间
            andExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()), EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())), EntityOperator.AND));
            //发布时间小于当前时间
            andExprs.add(EntityCondition.makeCondition("publishDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //下架时间大于当前时间
            andExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //剩余数量大于0
            andExprs.add(EntityCondition.makeCondition("residualQuantity", EntityOperator.GREATER_THAN, new Long(0)));
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            EntityFindOptions commonfindOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator ongoing_pli;
            try {
                ongoing_pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, commonfindOpts);
                ongoingActivityList = ongoing_pli.getCompleteList();
                ongoingAllSize = ongoing_pli.getResultsSizeAfterPartialList();
            } catch (GenericEntityException e2) {
                e2.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(ongoingActivityList)) {
                for (GenericValue v : ongoingActivityList) {
                    activityList.add(v);
                }
            }
            
            /*  *//** 查询开始条数*//*
            int lowIndex = 0;
            *//** 查询结束条数*//*
            int highIndex = 0;
            try {
                //计算开始分页值 & 计算分页结束值
            	lowIndex = viewIndex  + 1;
                highIndex = viewIndex+ viewSize;
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                //填充查询条件,查询字段，排序字段
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
                //获取正在进行中的团购结果集
                ongoingActivityList = pli.getPartialList(lowIndex, viewSize);
                // add by gss  有多少条正在进行的团购
                ongoingListSize = ongoingActivityList.size();
                pli.close();
                if(UtilValidate.isNotEmpty(ongoingActivityList)){
                    for (GenericValue v : ongoingActivityList) {
                    	 activityList.add(v);
                    }
                }
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }*/

            /**正在进行的活动 数量小于 查询数量时， 添加正在进行的活动  */
            List<EntityCondition> nostartExprs = FastList.newInstance();
            EntityCondition nostartCond = null;
            //是否有关键字
            if (UtilValidate.isNotEmpty(activityIds)) {
                nostartExprs.add(EntityCondition.makeCondition("activityId", EntityOperator.IN, activityIds));
            }
            nostartExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
            nostartExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            //“活动状态”为"审批通过"的团购活动   add  by gss
            nostartExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            // 团购未开始---开始时间大于当前时间
            nostartExprs.add(EntityCondition.makeCondition(EntityCondition.makeCondition("activityStartDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())));
            //发布时间小于当前时间
            nostartExprs.add(EntityCondition.makeCondition("publishDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //下架时间大于当前时间
            nostartExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //判断是否有分类并且不是全部
            if (!"all".equals(classify) && UtilValidate.isNotEmpty(classiIds)) {
                //商品分类
                nostartExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, classiIds));
            }
            if (nostartExprs.size() > 0) {
                nostartCond = EntityCondition.makeCondition(nostartExprs, EntityOperator.AND);
            }
            //填充查询条件,查询字段，排序字段
            EntityListIterator nostart_pli;
            try {
                nostart_pli = delegator.findListIteratorByCondition(dynamicViewEntity, nostartCond, null, fieldsToSelect, orderBy, commonfindOpts);
                nostartAllSize = nostart_pli.getResultsSizeAfterPartialList();
                nostartActivityList = nostart_pli.getCompleteList();
                nostart_pli.close();
            } catch (GenericEntityException e2) {
                e2.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(nostartActivityList)) {
                for (GenericValue v : nostartActivityList) {
                    activityList.add(v);
                }
            }
           /* //正在进行的活动 数量小于 查询数量时， 添加 未开始的活动
            if(ongoingListSize<viewSize){
                try {
                	viewIndex=10;
                    //计算开始分页值 & 计算分页结束值
                    lowIndex = 1;
                    highIndex = viewSize;
                    EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                    //填充查询条件,查询字段，排序字段
                    EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, nostartCond, null, fieldsToSelect, orderBy, findOpts);
                    //获取未开始的团购结果集
                    nostartActivityList = pli.getPartialList(lowIndex, viewSize-ongoingActivityList.size());
                    pli.close();
                    //add by gss  未开始的团购数量
                    nostartListSize = nostartActivityList.size();
                    if(UtilValidate.isNotEmpty(nostartActivityList)){
                        for (GenericValue v : nostartActivityList) {
                        	 activityList.add(v);
                        }
                    }
                } catch (GenericEntityException e) {
                    Debug.log(e.getMessage());
                }
            }*/

            /** 已售完查询条件  */
            List<EntityCondition> clearoutExprs = FastList.newInstance();
            EntityCondition clearoutCond = null;
            //是否有关键字
            if (UtilValidate.isNotEmpty(activityIds)) {
                clearoutExprs.add(EntityCondition.makeCondition("activityId", EntityOperator.IN, activityIds));
            }
            clearoutExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
            clearoutExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            //“活动状态”为"审批通过"的团购活动   add  by gss
            clearoutExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            // 团购正在进行中---开始时间小于当前时间 并且结束时间大于当前时间
            clearoutExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()), EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())), EntityOperator.AND));
            //发布时间小于当前时间
            clearoutExprs.add(EntityCondition.makeCondition("publishDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //下架时间大于当前时间
            clearoutExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //剩余数量等于0
            clearoutExprs.add(EntityCondition.makeCondition("residualQuantity", new Long(0)));
            //判断是否有分类并且不是全部
            if (!"all".equals(classify) && UtilValidate.isNotEmpty(classiIds)) {
                //商品分类
                clearoutExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, classiIds));
            }
            if (clearoutExprs.size() > 0) {
                clearoutCond = EntityCondition.makeCondition(clearoutExprs, EntityOperator.AND);
            }
            //填充查询条件,查询字段，排序字段
            EntityListIterator clearout_pli;
            try {
                clearout_pli = delegator.findListIteratorByCondition(dynamicViewEntity, clearoutCond, null, fieldsToSelect, orderBy, commonfindOpts);
                clearoutActivityList = clearout_pli.getCompleteList();
                clearoutAllSize = clearout_pli.getResultsSizeAfterPartialList();
            } catch (GenericEntityException e2) {
                e2.printStackTrace();
            }

            if (UtilValidate.isNotEmpty(clearoutActivityList)) {
                for (GenericValue v : clearoutActivityList) {
                    activityList.add(v);
                }
            }
               /* if(ongoingListSize+nostartListSize<viewSize){
                    try {
                        //计算开始分页值 & 计算分页结束值
                        lowIndex = 1;
                        highIndex = viewSize;
                        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                        //填充查询条件,查询字段，排序字段
                        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, clearoutCond, null, fieldsToSelect, orderBy, findOpts);
                        //获取正在进行中的团购结果集
                        clearoutActivityList = pli.getPartialList(lowIndex, viewSize-ongoingListSize-nostartListSize);
                         pli.close();
                        //add by gss  有多少条已售完的团购
                         clearoutListSize = clearoutActivityList.size();
                        if(UtilValidate.isNotEmpty(clearoutActivityList)){
                            for (GenericValue v : clearoutActivityList) {
                            	 activityList.add(v);
                            }
                        }
                    } catch (GenericEntityException e) {
                        Debug.log(e.getMessage());
                    }
                }*/
            /** 查询已结束  */
            List<EntityCondition> finishExprs = FastList.newInstance();
            EntityCondition finishCond = null;
            //是否有关键字
            if (UtilValidate.isNotEmpty(activityIds)) {
                finishExprs.add(EntityCondition.makeCondition("activityId", EntityOperator.IN, activityIds));
            }
            finishExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
            finishExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            //“活动状态”为"审批通过"的团购活动   add  by gss
            finishExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            // 团购已结束---结束时间小于当前时间
            finishExprs.add(EntityCondition.makeCondition("activityEndDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            // 下架时间大于当前时间
            finishExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //判断是否有分类并且不是全部
            if (!"all".equals(classify) && UtilValidate.isNotEmpty(classiIds)) {
                //商品分类
                finishExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, classiIds));
            }
            if (finishExprs.size() > 0) {
                finishCond = EntityCondition.makeCondition(finishExprs, EntityOperator.AND);
            }
            //填充查询条件,查询字段，排序字段
            EntityListIterator finish_pli;
            try {
                finish_pli = delegator.findListIteratorByCondition(dynamicViewEntity, finishCond, null, fieldsToSelect, orderBy, commonfindOpts);
                finishActivityList = finish_pli.getCompleteList();
                finishAllSize = finish_pli.getResultsSizeAfterPartialList();
            } catch (GenericEntityException e1) {
                e1.printStackTrace();
            }

            if (UtilValidate.isNotEmpty(finishActivityList)) {
                for (GenericValue v : finishActivityList) {
                    activityList.add(v);
                }
            }

            /*    if(ongoingListSize+nostartListSize+clearoutListSize<viewSize)
                {
                    try {
                        //计算开始分页值 & 计算分页结束值
                        lowIndex = 1;
                        highIndex = viewSize;
                        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                        //填充查询条件,查询字段，排序字段
                        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, finishCond, null, fieldsToSelect, orderBy, findOpts);
                        //获取已结束的团购结果集
                        finishActivityList = pli.getPartialList(lowIndex, viewSize-ongoingListSize-nostartListSize-clearoutListSize);
                        pli.close();
                        finishListSize = finishActivityList.size();
                        if(UtilValidate.isNotEmpty(finishActivityList)){
                            for (GenericValue v : finishActivityList) {
                            	 activityList.add(v);
                            }
                        }
                    } catch (GenericEntityException e) {
                        Debug.log(e.getMessage());
                    }
                }*/

            allsize = ongoingAllSize + clearoutAllSize + finishAllSize + nostartAllSize;
            int viewindex = 0;
            if (viewIndex == 0) {
                viewindex = 10;
            } else {
                viewindex = viewIndex + viewSize;
            }
            if (activityList.size() >= viewindex) {
                for (int i = viewIndex; i < viewindex; i++) {
                    activityLists.add(activityList.get(i));
                }
            } else if (activityList.size() <= viewindex && activityList.size() > viewIndex) {
                for (int i = viewIndex; i < activityList.size(); i++) {
                    activityLists.add(activityList.get(i));
                }
            }

        } else {
            Map<String, BigDecimal> map = new TreeMap<String, BigDecimal>();
            List<String> activity_List = new FastList<String>();
            if ("sales".equals(sort)) {
                //活动销售数量
                orderBy.add("-hasBuyQuantity");
            } else if ("comment".equals(sort)) { //好评优先
                orderBy.add("-averageCustomerRating");
            }
            /** 查询条件 */
            List<EntityCondition> andExprs = FastList.newInstance();
            EntityCondition mainCond = null;
            //是否有关键字
            if (UtilValidate.isNotEmpty(activityIds)) {
                andExprs.add(EntityCondition.makeCondition("id", EntityOperator.IN, activityIds));
            }
            andExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            //“活动状态”为"审批通过"的团购活动   add  by gss
            andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            //发布时间小于当前时间
            andExprs.add(EntityCondition.makeCondition("publishDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //下架时间大于当前时间
            andExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            //判断是否有分类并且不是全部
            if (!"all".equals(classify) && UtilValidate.isNotEmpty(classiIds)) {
                //商品分类
                andExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, classiIds));
            }
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);

            if ("priceDec".equals(sort) || "priceAsc".equals(sort)) {
                //填充查询条件,查询字段，排序字段
                try {
                    EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
                    ongoingActivityList = pli.getCompleteList();
                    allsize = pli.getResultsSizeAfterPartialList();
                    pli.close();
                    if (UtilValidate.isNotEmpty(ongoingActivityList)) {
                        for (GenericValue activtys : ongoingActivityList) {
                            //已售数量
                            Long hasBuyQuantity = activtys
                                    .getLong("hasBuyQuantity");
                            //团购价格
                            List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
                            try {
                                productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activtys.get("id")), UtilMisc.toList("orderQuantity"));//阶梯价规则表
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            BigDecimal salePrice = null;
                            salePrice = productGroupOrderRules.get(0).getBigDecimal("orderPrice");
                            for (int i = 0; i < productGroupOrderRules.size(); i++) {
                                if (productGroupOrderRules.get(i).getLong("orderQuantity").compareTo(Long.valueOf(hasBuyQuantity)) <= 0) {
                                    salePrice = productGroupOrderRules.get(i).getBigDecimal("orderPrice");
                                } else {
                                    break;
                                }
                            }

                            map.put((String) activtys.get("id"), salePrice);
                        }
                    }
                    //这里将map.entrySet()转换成list
                    List<Map.Entry<String, BigDecimal>> list = new ArrayList<Map.Entry<String, BigDecimal>>(map.entrySet());
                    //然后通过比较器来实现排序
                    if ("priceDec".equals(sort)) {//价格从高到低
                        Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() {
                            //降序排序
                            public int compare(Map.Entry<String, BigDecimal> o1,
                                               Map.Entry<String, BigDecimal> o2) {
                                return o2.getValue().compareTo(o1.getValue());
                            }
                        });
                        for (Map.Entry<String, BigDecimal> mapping : list) {
                            //System.out.println(mapping.getKey()+":"+mapping.getValue());
                            activity_List.add(mapping.getKey());
                        }
                    } else if ("priceAsc".equals(sort)) {//价格从低到高
                        Collections.sort(list, new Comparator<Map.Entry<String, BigDecimal>>() {
                            //升序排序
                            public int compare(Map.Entry<String, BigDecimal> o1,
                                               Map.Entry<String, BigDecimal> o2) {
                                return o1.getValue().compareTo(o2.getValue());
                            }
                        });
                        for (Map.Entry<String, BigDecimal> mapping : list) {
                            //System.out.println(mapping.getKey()+":"+mapping.getValue());
                            activity_List.add(mapping.getKey());
                        }
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            //判断是排序过后的活动ID是否为空
            if (UtilValidate.isNotEmpty(activity_List)) {
                List<String> resultaAtivity = new ArrayList<String>();
                int from = viewIndex;
                int to = viewIndex + viewSize;
                if (to < activity_List.size()) {
                    for (int i = from; i < to; i++) {
                        resultaAtivity.add(activity_List.get(i));
                    }
                } else if (from < activity_List.size() && to >= activity_List.size()) {
                    for (int i = from; i < activity_List.size(); i++) {
                        resultaAtivity.add(activity_List.get(i));
                    }
                } else {
                    resultaAtivity.clear();
                }
                for (int i = 0; i < resultaAtivity.size(); i++) {
                    /** 查询条件 */
                    List<EntityCondition> andExprs1 = FastList.newInstance();
                    //排序过后的activityID
                    andExprs1.add(EntityCondition.makeCondition("id", EntityOperator.EQUALS, resultaAtivity.get(i)));
                    EntityCondition mainCond1 = null;
                    if (andExprs.size() > 0) {
                        mainCond1 = EntityCondition.makeCondition(andExprs1, EntityOperator.AND);
                    }
                    try {
                        EntityFindOptions findOpt = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
                        //填充查询条件,查询字段，排序字段
                        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond1, null, fieldsToSelect, orderBy, findOpt);
                        //获取正在进行中的团购结果集
                        List<GenericValue> ongoingActivityList1 = pli.getCompleteList();
                        pli.close();
                        if (UtilValidate.isNotEmpty(ongoingActivityList1)) {
                            for (GenericValue v : ongoingActivityList1) {
                                activityLists.add(v);
                            }
                        }
                    } catch (GenericEntityException e) {
                        Debug.log(e.getMessage());
                    }
                }
            } else {
                /** 查询开始条数*/
                int lowIndex = 0;
                /** 查询结束条数*/
                int highIndex = 0;
                try {
                    //计算开始分页值 & 计算分页结束值
                    lowIndex = viewIndex + 1;
                    highIndex = viewIndex + viewSize;
                    EntityFindOptions findOpt = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                    //填充查询条件,查询字段，排序字段
                    EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpt);
                    //获取正在进行中的团购结果集
                    ongoingActivityList = pli.getPartialList(lowIndex, viewSize);
                    // add by gss  有多少条团购
                    allsize = pli.getResultsSizeAfterPartialList();
                    pli.close();
                    if (UtilValidate.isNotEmpty(ongoingActivityList)) {
                        for (GenericValue v : ongoingActivityList) {
                            activityLists.add(v);
                        }
                    }
                } catch (GenericEntityException e) {
                    Debug.log(e.getMessage());
                }
            }
        }
        if (activityList == null) {
            activityList = FastList.newInstance();
        }
        List<Map<String, Object>> list = FastList.newInstance();
        for (GenericValue v : activityLists) {
            Map<String, Object> map = FastMap.newInstance();
            //add  by gss
            map.put("id", v.get("id"));//团购活动ID
            map.put("title", v.get("title"));//团购活动名称
            map.put("soldNum", v.getLong("hasBuyQuantity"));//团购已售数量
            GenericValue product;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
                String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                map.put("picSrc", imageUrl);//图片路径
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            //已售数量
            Long hasBuyQuantity = v.getLong("hasBuyQuantity");
            //团购价格
            List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
            try {
                productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", v.get("id")), UtilMisc.toList("orderQuantity"));//阶梯价规则表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            BigDecimal salePrice = null;
            if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                salePrice = productGroupOrderRules.get(0).getBigDecimal("orderPrice");
                for (int i = 0; i < productGroupOrderRules.size(); i++) {
                    if (productGroupOrderRules.get(i).getLong("orderQuantity").compareTo(Long.valueOf(hasBuyQuantity)) <= 0) {
                        salePrice = productGroupOrderRules.get(i).getBigDecimal("orderPrice");
                    } else {
                        break;
                    }
                }
            }
            map.put("price", salePrice);//团购价格
            List<String> marks = FastList.newInstance();
            if ("Y".equals(v.getString("isAnyReturn"))) {
                marks.add("随时退");
            }
            if ("Y".equals(v.getString("isSupportOverTimeReturn"))) {
                marks.add("过期退");
            }
            /*if("Y".equals(v.getString("isSupportScore"))) marks.add("活动可积分");
            if("Y".equals(v.getString("isSupportReturnScore"))) marks.add("退货返回积分");
            if("Y".equals(v.getString("isShowIndex"))) marks.add("推荐到首页");*/
            map.put("marks", marks);
            Long num = 0L;
            BigDecimal score = BigDecimal.ZERO;
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PR", "ProductReview");
            dynamicView.addAlias("PR", "productId");
            dynamicView.addAlias("PR", "sumProductRating", "productRating", null, null, null, "sum");
            dynamicView.addAlias("PR", "num", "productReviewId", null, null, null, "count");
            List<EntityCondition> reviewExprs = FastList.newInstance();
            reviewExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, v.get("productId")));
            EntityCondition reviewCond = EntityCondition.makeCondition(reviewExprs, EntityOperator.AND);
            EntityFindOptions findOpts1 = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
            // using list iterator
            EntityListIterator productReviewEli;
            try {
                productReviewEli = delegator.findListIteratorByCondition(dynamicView,
                        reviewCond
                        , null, null, null, findOpts1);
                List<GenericValue> prods = productReviewEli.getCompleteList();
                Iterator<GenericValue> prodsIt = prods.iterator();
                while (prodsIt.hasNext()) {
                    GenericValue oneProd = prodsIt.next();
                    num = oneProd.getLong("num");
                    score = oneProd.getBigDecimal("sumProductRating");
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
                score = new BigDecimal(10);
            } else {
                score = score.divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(2));
            }
            map.put("score", score);//商品评分，1-10分别对应0.5-5星
            map.put("commentNum", num);//评价数据量
            list.add(map);
        }

        resultData.put("max", allsize);  //团购总数
        resultData.put("groBuyList", list);
        result.put("resultData", resultData);
        return result;
    }


    /**
     * 获取抢购数据 add by Wcy 2016.01.26
     * edit by gss
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getSecKillForWap(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = FastMap.newInstance();
        List<GenericValue> activityList = FastList.newInstance();
        //进行中的秒杀
        List<GenericValue> ongoingActivityList = null;
        //未开始的秒杀
        List<GenericValue> nostartActivityList = null;
        //结束的秒杀
        List<GenericValue> finishActivityList = null;
        //抢光的
        List<GenericValue> overActivityList = null;
        Map<String, Object> resultData = FastMap.newInstance();
        Map<String, Object> resultDatas = FastMap.newInstance();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        List<String> communityId = UtilGenerics.cast(context.get("communityId"));
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
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }

        /** 定义活动动态视图*/
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
        dynamicViewEntity.addAlias("PA", "activityId");
        dynamicViewEntity.addAlias("PA", "activityAuditStatus");
        dynamicViewEntity.addAlias("PA", "publishDate");
        dynamicViewEntity.addAlias("PA", "activityName");
        dynamicViewEntity.addAlias("PA", "activityStartDate");
        dynamicViewEntity.addAlias("PA", "activityEndDate");
        dynamicViewEntity.addAlias("PA", "productPrice");
        dynamicViewEntity.addAlias("PA", "activityType");
        //活动总数量
        dynamicViewEntity.addAlias("PA", "activityQuantity");
        //剩余数量
        dynamicViewEntity.addAlias("PA", "leaveQuantity");
        //已售数量
        dynamicViewEntity.addAlias("PA", "hasBuyQuantity");
        dynamicViewEntity.addAlias("PA", "endDate");

        ComplexAlias relevancyComplexAlias = new ComplexAlias("-");
        relevancyComplexAlias.addComplexAliasMember(new ComplexAliasField("PA", "activityQuantity", null, null));
        relevancyComplexAlias.addComplexAliasMember(new ComplexAliasField("PA", "hasBuyQuantity", null, null));
        //剩余数量
        dynamicViewEntity.addAlias(null, "residualQuantity", null, null, null, null, null, relevancyComplexAlias);
        /** 定义活动商品动态视图 */
        dynamicViewEntity.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicViewEntity.addAlias("PAG", "activityId");
        dynamicViewEntity.addAlias("PAG", "productId");
        //add  by gss
        dynamicViewEntity.addAlias("PAG", "isShowIndex");

        /** 定义商品动态视图 */
        dynamicViewEntity.addMemberEntity("PD", "Product");
        dynamicViewEntity.addAlias("PD", "productId");
        dynamicViewEntity.addAlias("PD", "smallImageUrl");

        /** 定义商品价格动态视图*/
        dynamicViewEntity.addMemberEntity("PR", "ProductPrice");
        dynamicViewEntity.addAlias("PR", "price");
        dynamicViewEntity.addAlias("PR", "productPriceTypeId");

        /** 定义活动区域动态视图 */
        dynamicViewEntity.addMemberEntity("PAA", "ProductActivityArea");
        dynamicViewEntity.addAlias("PAA", "activityId");
        dynamicViewEntity.addAlias("PAA", "communityId");

        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("PA", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        dynamicViewEntity.addViewLink("PAG", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewEntity.addViewLink("PD", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewEntity.addViewLink("PA", "PAA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        /** 查询字段 & 排序字段 */
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("activityId");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("publishDate");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("price");
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("productPrice");
        fieldsToSelect.add("activityQuantity");
        fieldsToSelect.add("leaveQuantity");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("hasBuyQuantity");
        fieldsToSelect.add("residualQuantity");
        orderBy.add("-publishDate");
        int ongoingSize = 0;
        int nostartSize = 0;
        int overSize = 0;
        int finishSize = 0;
        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        andExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
        // begin add by gss 默认价格
        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"));
        //活动类型 为秒杀
        andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
        //活动剩余数量大于0
        andExprs.add(EntityCondition.makeCondition("residualQuantity", EntityOperator.GREATER_THAN, new Long(0)));
        //活动状态 审批通过
        andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        // 秒杀正在进行中---开始时间小于当前时间 并且结束时间大于当前时间
        andExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()), EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())), EntityOperator.AND));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        EntityFindOptions ongoing_findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        //填充查询条件,查询字段，排序字段
        EntityListIterator ongoing_plis;
        try {
            ongoing_plis = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, ongoing_findOpts);
            ongoingSize = ongoing_plis.getResultsSizeAfterPartialList();
            ongoingActivityList = ongoing_plis.getCompleteList();
            ongoing_plis.close();
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        for (GenericValue v : ongoingActivityList) {
            activityList.add(v);
        }
        
        
        /* *//** 查询开始条数*//*
        int lowIndex = 0;
        *//** 查询结束条数*//*
        int highIndex = 0;
        正在进行中秒杀总条数
        int ongoingkillListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
            lowIndex = viewIndex  + 1;
            highIndex = viewIndex + viewSize;
            EntityFindOptions ongoingfindOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, ongoingfindOpts);
            //获取结果集
            ongoingActivityList = pli.getPartialList(lowIndex, viewSize);
            // add by gss  有多少条正在进行的秒杀抢购
            ongoingkillListSize = ongoingActivityList.size();
            pli.close();
            if(UtilValidate.isNotEmpty(ongoingActivityList)){
            for (GenericValue v : ongoingActivityList) {
            	 activityList.add(v);
            }
            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }*/

        /** 查询条件 */
        List<EntityCondition> nostartExprs = FastList.newInstance();
        EntityCondition nostartCond = null;
        nostartExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
        // begin add by gss 默认价格
        nostartExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"));
        //活动类型 为秒杀
        nostartExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
        //活动状态 审批通过
        nostartExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        // 秒杀正未开始---开始时间大于当前时间 并且发布时间小于当前时间
        nostartExprs.add(EntityCondition.makeCondition(EntityCondition.makeCondition("activityStartDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())));
        nostartExprs.add(EntityCondition.makeCondition(EntityCondition.makeCondition("publishDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())));
        if (nostartExprs.size() > 0) {
            nostartCond = EntityCondition.makeCondition(nostartExprs, EntityOperator.AND);
        }
        EntityFindOptions nostart_findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        //填充查询条件,查询字段，排序字段
        EntityListIterator nostart_plis;
        try {
            nostart_plis = delegator.findListIteratorByCondition(dynamicViewEntity, nostartCond, null, fieldsToSelect, orderBy, ongoing_findOpts);
            nostartSize = nostart_plis.getResultsSizeAfterPartialList();
            nostartActivityList = nostart_plis.getCompleteList();
            nostart_plis.close();
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        for (GenericValue v : nostartActivityList) {
            activityList.add(v);
        }

    	/*未开始秒杀总条数
        int nostartkillListSize = 0;
        //判断正在进行中的秒杀数量是否够，不够在查询未开始的秒杀
        if(ongoingkillListSize<viewSize){
        	try {
        		//计算开始分页值 & 计算分页结束值
        		lowIndex = 1;
        		highIndex = viewSize;
        		EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        		//填充查询条件,查询字段，排序字段
        		EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, nostartCond, null, fieldsToSelect, orderBy, findOpts);
        		//获取结果集
        		nostartActivityList = pli.getPartialList(lowIndex, viewSize-ongoingkillListSize);
        		// add by gss  有多少条未开始的秒杀抢购
        		nostartkillListSize = nostartActivityList.size();
        		pli.close();
        		if(UtilValidate.isNotEmpty(nostartActivityList)){
        			for (GenericValue v : nostartActivityList) {
        				activityList.add(v);
        			}
        		}
        	} catch (GenericEntityException e) {
        		Debug.log(e.getMessage());
        	}
        }*/

        /** 查询条件 */
        List<EntityCondition> overExprs = FastList.newInstance();
        EntityCondition overCond = null;
        overExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
        // begin add by gss 默认价格
        overExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"));
        //活动类型 为秒杀
        overExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
        //活动剩余数量大于0
        overExprs.add(EntityCondition.makeCondition("residualQuantity", EntityOperator.EQUALS, new Long(0)));
        //活动状态 审批通过
        overExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        // 秒杀正在进行中---开始时间小于当前时间 并且结束时间大于当前时间
        overExprs.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()), EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())), EntityOperator.AND));
        if (overExprs.size() > 0) {
            overCond = EntityCondition.makeCondition(overExprs, EntityOperator.AND);
        }
        //填充查询条件,查询字段，排序字段
        EntityListIterator over_plis;
        try {
            over_plis = delegator.findListIteratorByCondition(dynamicViewEntity, overCond, null, fieldsToSelect, orderBy, ongoing_findOpts);
            overSize = over_plis.getResultsSizeAfterPartialList();
            overActivityList = over_plis.getCompleteList();
            over_plis.close();
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        for (GenericValue v : overActivityList) {
            activityList.add(v);
        }
        /*已抢光总条数
        int overkillListSize = 0;
        //判断正在进行中的秒杀数量是否够，不够在查询已抢光的秒杀
        if(ongoingkillListSize+nostartkillListSize<viewSize){
        	try {
        		//计算开始分页值 & 计算分页结束值
        		lowIndex = 1;
        		highIndex = viewSize;
        		EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        		//填充查询条件,查询字段，排序字段
        		EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, overCond, null, fieldsToSelect, orderBy, findOpts);
        		//获取结果集
        		overActivityList = pli.getPartialList(lowIndex, viewSize-ongoingkillListSize-nostartkillListSize);
        		// add by gss  有多少条已抢光的秒杀抢购
        		overkillListSize = overActivityList.size();
        		pli.close();
        		if(UtilValidate.isNotEmpty(overActivityList)){
        			for (GenericValue v : overActivityList) {
        				activityList.add(v);
        			}
        		}
        	} catch (GenericEntityException e) {
        		Debug.log(e.getMessage());
        	}
        }*/
        /** 已结束秒杀总条数 */
        List<EntityCondition> finishExprs = FastList.newInstance();
        EntityCondition finishCond = null;
        finishExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
        // begin add by gss 默认价格
        finishExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"));
        //活动类型 为秒杀
        finishExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
        //活动状态 审批通过
        finishExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        // 秒杀正已过期---结束时间小于当前时间
        finishExprs.add(EntityCondition.makeCondition(EntityCondition.makeCondition("activityEndDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())));
        finishExprs.add(EntityCondition.makeCondition(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())));
        if (finishExprs.size() > 0) {
            finishCond = EntityCondition.makeCondition(finishExprs, EntityOperator.AND);
        }
        //填充查询条件,查询字段，排序字段
        EntityListIterator finish_plis;
        try {
            finish_plis = delegator.findListIteratorByCondition(dynamicViewEntity, finishCond, null, fieldsToSelect, orderBy, ongoing_findOpts);
            finishSize = finish_plis.getResultsSizeAfterPartialList();
            finishActivityList = finish_plis.getCompleteList();
            finish_plis.close();
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        for (GenericValue v : finishActivityList) {
            activityList.add(v);
        }

       /* 已结束秒杀总条数
        int finishkillListSize = 0;
         //判断正在进行中的秒杀数量是否够，不够在查询结束的秒杀
        if(ongoingkillListSize+nostartkillListSize+overkillListSize<viewSize){
            try {
                //计算开始分页值 & 计算分页结束值
                lowIndex = 1;
                highIndex = viewSize;
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                //填充查询条件,查询字段，排序字段
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, finishCond, null, fieldsToSelect, orderBy, findOpts);
                //获取结果集
                finishActivityList = pli.getPartialList(lowIndex, viewSize-ongoingkillListSize-nostartkillListSize-overkillListSize);
                // add by gss  有多少条已结束的秒杀抢购
                finishkillListSize = finishActivityList.size();
                pli.close();
                if(UtilValidate.isNotEmpty(finishActivityList)){
                    for (GenericValue v : finishActivityList) {
                    	 activityList.add(v);
                    }
                    }
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
        }*/
        int viewindex = 0;
        if (viewIndex == 0) {
            viewindex = 10;
        } else {
            viewindex = viewIndex + viewSize;
        }
        List<GenericValue> activityLists = FastList.newInstance();
        if (activityList.size() >= viewindex) {
            for (int i = viewIndex; i < viewindex; i++) {
                activityLists.add(activityList.get(i));
            }
        } else if (activityList.size() <= viewindex && activityList.size() > viewIndex) {
            for (int i = viewIndex; i < activityList.size(); i++) {
                activityLists.add(activityList.get(i));
            }
        }
        if (activityList == null) {
            activityList = FastList.newInstance();
        }

        Date now = new Date();
        List<Map<String, Object>> list = FastList.newInstance();
        List lists = FastList.newInstance();
        for (GenericValue v : activityLists) {
            Map<String, Object> map = FastMap.newInstance();
            //未开始：preparation，进行中：ongoing，已结束：finish，已售罄：clearout
            //根据开始时间和结束时间来判断活动状态  add by gss
            Date activityStartDate = v.getTimestamp("activityStartDate");
            Date activityEndDate = v.getTimestamp("activityEndDate");
            //已售数量
            Long hasBuyQuantity = v.getLong("hasBuyQuantity");
            //总数量
            Long activityQuantity = v.getLong("activityQuantity");
            if (hasBuyQuantity == null) {
                hasBuyQuantity = 0L;
            }
            //剩余数量
            Long soldQuantity = activityQuantity - hasBuyQuantity;
            //秒杀活动是否已结束
            if (UtilValidate.isNotEmpty(activityEndDate) && now.before(activityEndDate)) {
                //当前时间大于开始时间 活动进行中
                if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && soldQuantity == 0) {
                    map.put("state", "clearout");
                } else if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && (soldQuantity > 0)) {
                    map.put("state", "ongoing");
                } else if (UtilValidate.isNotEmpty(activityStartDate) && now.before(activityStartDate)) {
                    map.put("state", "preparation");
                }
            } else {
                map.put("state", "finish");
            }
            //add  by gss
            map.put("id", v.get("activityId"));//秒杀活动ID
            map.put("title", v.get("activityName"));//秒杀活动ID
            GenericValue product;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
                String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                map.put("picSrc", imageUrl);//图片路径

            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            map.put("orgPrice", v.get("price"));//原价
            map.put("curPrice", v.get("productPrice"));//当前价格
            map.put("soldNum", hasBuyQuantity);//已售数量
            map.put("startTimestamp", v.getTimestamp("activityStartDate").getTime() / 1000);
            map.put("endTimestamp", v.getTimestamp("activityEndDate").getTime() / 1000);
            list.add(map);
        }
        resultData.put("max", ongoingSize + nostartSize + overSize + finishSize);  //总数
        resultData.put("secKillList", list);
        result.put("resultData", resultData);
        return result;
    }

    /**
     * 获取详情 add by Wcy 2016.01.26
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getActivityDetail(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数  活动ID */
        String activityId = (String) context.get("activityId");
        String userLoginId = (String) context.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        String partyId = userLogin.getString("partyId");
        GenericValue activityGoods = null;
        GenericValue activity = null;
        String productId = "";
        try {
            activity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
            activityGoods = EntityUtil.getFirst(delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId)));
            productId = activityGoods.getString("productId");
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == activity) {
            result.put("resultData", resultData);
            return result;
        }

        /** 商品 */
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == product) {
            result.put("resultData", resultData);
            return result;
        }
        /** 图片列表 */
        List<String> banner = FastList.newInstance();
       /* String image1 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_1_LARGE", locale, dispatcher);
        String image2 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_2_LARGE", locale, dispatcher);
        String image3 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_3_LARGE", locale, dispatcher);
        String image4 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_4_LARGE", locale, dispatcher);
        String image5 = ProductContentWrapper.getProductContentAsText(product, "XTRA_IMG_5_LARGE", locale, dispatcher);*/
        String imageUrl1 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
        String imageUrl2 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_2", locale, dispatcher);
        String imageUrl3 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_3", locale, dispatcher);
        String imageUrl4 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_4", locale, dispatcher);
        String imageUrl5 = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_5", locale, dispatcher);
        if (UtilValidate.isNotEmpty(imageUrl1)) {
            banner.add(imageUrl1);
        }
        if (UtilValidate.isNotEmpty(imageUrl2)) {
            banner.add(imageUrl2);
        }
        if (UtilValidate.isNotEmpty(imageUrl3)) {
            banner.add(imageUrl3);
        }
        if (UtilValidate.isNotEmpty(imageUrl4)) {
            banner.add(imageUrl4);
        }
        if (UtilValidate.isNotEmpty(imageUrl5)) {
            banner.add(imageUrl5);
        }
        /** 活动标签 */
        List<String> marks = FastList.newInstance();
        if (null != activityGoods) {
            if ("Y".equals(activityGoods.getString("isAnyReturn"))) {
                marks.add("随时退");
            }
            if ("Y".equals(activityGoods.getString("isSupportOverTimeReturn"))) {
                marks.add("过期退");
            }
            if ("Y".equals(activityGoods.getString("isSupportScore"))) {
                marks.add("可积分");
            }
            if ("Y".equals(activityGoods.getString("isSupportReturnScore"))) {
                marks.add("退货返回积分");
            }
            if ("Y".equals(activityGoods.getString("isPostageFree"))) {
                marks.add("包邮");
            }
            // if ("Y".equals(activityGoods.getString("isShowIndex"))) marks.add("推荐到首页");
        }

        /** 团购阶梯列表 */
        List<GenericValue> productGroupOrderRule = null;
        try {
            productGroupOrderRule = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", activityId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        List<Map<String, Object>> priceList = FastList.newInstance();
        for (GenericValue groupRule : productGroupOrderRule) {
            Map<String, Object> map = FastMap.newInstance();
            map.put("people", groupRule.get("orderQuantity"));
            map.put("price", groupRule.get("orderPrice"));
            priceList.add(map);
        }

        /** 评价列表 */
        List<Map<String, Object>> reviewList = null;
        Map<String, Object> parameContext = FastMap.newInstance();
        parameContext.put("productId", productId);
        //parameContext.put("userLoginId",userLoginId);
        parameContext.put("viewSize", "3");
        Map<String, Object> resultData1 = FastMap.newInstance();
        try {
            Map<String, Object> reviewResult = dispatcher.runSync("findProductReview", parameContext);
            //reviewList = (List<Map<String, Object>>) reviewResult.get("resultData");
            resultData1 = (Map<String, Object>) reviewResult.get("resultData");
            reviewList = (List<Map<String, Object>>) resultData1.get("reviewList");
        } catch (GenericServiceException e) {
            Debug.log(e.getMessage());
        }

        /** 活动是否被收藏 */
        List<GenericValue> productActivityCollection = null;
        try {
            productActivityCollection = delegator.findByAnd("ProductActivityCollection", UtilMisc.toMap("activityId", activityId, "partyId", partyId));
            //productActivityCollection = delegator.findByPrimaryKey("ProductActivityCollection",UtilMisc.toMap("activityId",activityId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        Boolean collection;
        if (UtilValidate.isNotEmpty(productActivityCollection)) {
            collection = true;
        } else {
            collection = false;
        }
        //Boolean collection = !(productActivityCollection == null);

        resultData.put("productId", productId);//商品ID
        if ("GROUP_ORDER".equals(activity.getString("activityType"))) {
            resultData.put("type", "group"); //活动类型  团购：group，秒杀：secKill
            resultData.put("priceList", priceList); //团购阶梯列表
        } else if ("SEC_KILL".equals(activity.getString("activityType"))) {
            resultData.put("type", "secKill"); //活动类型  团购：group，秒杀：secKill
            resultData.put("startTimestamp", activity.getTimestamp("activityStartDate").getTime() / 1000); //抢购开始时间戳
            resultData.put("endTimestamp", activity.getTimestamp("activityEndDate").getTime() / 1000); //抢购结束时间戳
            resultData.put("curPrice", activity.getBigDecimal("productPrice")); //当前价格
            try {
                List<GenericValue> productDefaultPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productPriceTypeId", "DEFAULT_PRICE", "productId", productId)));
                if (UtilValidate.isNotEmpty(productDefaultPrices)) {
                    GenericValue productFeaturePrice = productDefaultPrices.get(0);
                    if (UtilValidate.isNotEmpty(productFeaturePrice.get("price"))) {
                        resultData.put("orgPrice", productFeaturePrice.getBigDecimal("price"));
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        //活动状态
        Date now = new Date();
        //根据开始时间和结束时间来判断活动状态  add by gss
        Date activityStartDate = activity.getTimestamp("activityStartDate");
        Date activityEndDate = activity.getTimestamp("activityEndDate");
        //已售数量
        Long hasBuyQuantity = activity.getLong("hasBuyQuantity");
        //单个ID限购数量
        Long limitQuantity = activity.getLong("limitQuantity");
        //活动数量
        Long activityQuantity = activity.getLong("activityQuantity");
        if (hasBuyQuantity == null) {
            hasBuyQuantity = 0L;
        }
        //剩余数量
        Long soldQuantity = activityQuantity - hasBuyQuantity;

        /** 活动商品可用库存 */
        BigDecimal accountingQuantityTotal = BigDecimal.ZERO;
        List<GenericValue> inventory_item = null;
        try {
            inventory_item = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (UtilValidate.isNotEmpty(inventory_item)) {
            GenericValue inventoryItem = EntityUtil.getFirst(inventory_item);
            accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal");
        }
        //查询该活动已下单数量
        Long buyNum = 0L;
        if ("FINISHED_GOOD".equals(product.get("productTypeId"))) {
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
                    for (GenericValue results : resultList) {
                        buyNum += results.getBigDecimal("quantity").longValue();
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        } else if ("VIRTUAL_GOOD".equals(product.get("productTypeId"))) {
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
        //可购买的数量
        long needbuyNum = limitQuantity - buyNum;
        //活动结束时间大于当前时间
        if (UtilValidate.isNotEmpty(activityEndDate) && now.before(activityEndDate)) {
            //当前时间大于开始时间 活动进行中
            if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && soldQuantity == 0 || accountingQuantityTotal.compareTo(BigDecimal.ZERO) == 0) {
                resultData.put("state", "clearout");
            } else if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && (soldQuantity > 0) && needbuyNum > 0) {
                resultData.put("state", "ongoing");
            } else if (UtilValidate.isNotEmpty(activityStartDate) && now.before(activityStartDate)) {
                resultData.put("state", "preparation");
            } else if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && (needbuyNum <= 0)) {
                resultData.put("state", "purchase");
            }
        } else {
            resultData.put("state", "finish");
        }
        // resultData.put("state",activityType.get(activity.getString("activityAuditStatus")));//活动状态  未开始：preparation，进行中：ongoing，已结束：finish，已售罄：clearout
        resultData.put("collection", collection); //是否被收藏
        resultData.put("title", product.getString("productName"));  //活动标题
        resultData.put("subTitle", product.getString("productSubheadName"));   //活动副标题
        resultData.put("banner", banner); //图片列表
        resultData.put("mark", marks); //活动标签
        //resultData.put("soldNum",10); //已售数量  //TODO 数据从哪里来？
        //add by gss
        resultData.put("soldNum", hasBuyQuantity); //已售数量
        // resultData.put("orgPrice",10); //商品原价 //TODO 数据从哪里来？
        resultData.put("content", product.getString("mobileDetails") == null ? "" : product.getString("mobileDetails"));//商品详情
        resultData.put("inform", activity.getString("activityDesc") == null ? "" : activity.getString("activityDesc")); //购买须知
        BigDecimal score = BigDecimal.ZERO;
        Long num = 0L;
        Map<String, Object> reviewDatas = FastMap.newInstance();
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PR", "ProductReview");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "isShow");
        dynamicView.addAlias("PR", "sumProductRating", "productRating", null, null, null, "sum");
        dynamicView.addAlias("PR", "num", "productReviewId", null, null, null, "count");
        List<EntityCondition> andExprs = FastList.newInstance();
        andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, product.get("productId")));
        andExprs.add(EntityCondition.makeCondition("isShow", EntityOperator.EQUALS, "1"));
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        EntityFindOptions findOpts1 = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
        // using list iterator
        EntityListIterator productReviewEli;
        try {
            productReviewEli = delegator.findListIteratorByCondition(dynamicView,
                    mainCond
                    , null, null, null, findOpts1);
            List<GenericValue> prods = productReviewEli.getCompleteList();
            Iterator<GenericValue> prodsIt = prods.iterator();
            while (prodsIt.hasNext()) {
                GenericValue oneProd = prodsIt.next();
                score = oneProd.getBigDecimal("sumProductRating");
                num = oneProd.getLong("num");
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
            score = new BigDecimal(10);
        } else {
            score = score.divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(2));
        }
        reviewDatas.put("score", score);
        reviewDatas.put("number", num);
        reviewDatas.put("list", reviewList);

        resultData.put("review", reviewDatas); //评价列表
        result.put("resultData", resultData);
        return result;
    }

    /**
     * 收藏请求 add by Wcy 2016.01.28
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> saveCollect(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数 */
        String activityId = (String) context.get("activityId");
        String userLoginId = (String) context.get("userLoginId");

        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        String partyId = userLogin.getString("partyId");
        GenericValue activity = null;
        try {
            activity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == activity) {
            result.put("resultData", resultData);
            return result;
        }
        //判断是否已收藏  add by gss
        try {
            List<GenericValue> activityCollect = delegator.findByAnd("ProductActivityCollection", UtilMisc.toMap("activityId", activityId, "partyId", partyId));
            if (UtilValidate.isNotEmpty(activityCollect)) {
                delegator.removeAll(activityCollect);
                resultData.put("status", true);
                resultData.put("info", "收藏已取消");
                result.put("resultData", resultData);
            } else {
                GenericValue collect = delegator.makeValue("ProductActivityCollection");
                collect.set("activityId", activityId);
                collect.set("partyId", partyId);
                collect.set("activityType", activity.getString("activityType"));
                try {
                    collect.create();
                    resultData.put("status", true);
                    resultData.put("info", "收藏成功");
                    result.put("resultData", resultData);
                } catch (GenericEntityException e) {
                    Debug.log(e.getMessage());
                    resultData.put("status", false);
                    resultData.put("info", "收藏失败");
                    result.put("resultData", resultData);
                }
            }
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    /**
     * 我的收藏：分为团购收藏和秒杀收藏  add by Wcy 2016.01.28
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> myCollect(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数 */
        String type = (String) context.get("type");
        String userLoginId = (String) context.get("userLoginId");

        /** 分页：当前页 */
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }

        /** 分页：每页记录数 */
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
        } catch (Exception e) {
            viewSize = 20;
        }

        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        String partyId = userLogin.getString("partyId");

        /** 定义活动动态视图*/
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();

        dynamicViewEntity.addMemberEntity("PAC", "ProductActivityCollection");
        dynamicViewEntity.addAlias("PAC", "activityId");
        dynamicViewEntity.addAlias("PAC", "partyId");
        dynamicViewEntity.addAlias("PAC", "activityType");
        dynamicViewEntity.addAlias("PAC", "createdStamp");//创建时间

        dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
        dynamicViewEntity.addAlias("PA", "activityId");
        dynamicViewEntity.addAlias("PA", "activityAuditStatus");
        dynamicViewEntity.addAlias("PA", "publishDate");
        dynamicViewEntity.addAlias("PA", "activityName");
        dynamicViewEntity.addAlias("PA", "activityStartDate");
        dynamicViewEntity.addAlias("PA", "activityEndDate");
        dynamicViewEntity.addAlias("PA", "hasBuyQuantity");
        //下架时间
        dynamicViewEntity.addAlias("PA", "endDate");
        dynamicViewEntity.addAlias("PA", "productPrice");
        dynamicViewEntity.addAlias("PA", "activityType");
        //活动总数量
        dynamicViewEntity.addAlias("PA", "activityQuantity");
        //剩余数量
        dynamicViewEntity.addAlias("PA", "leaveQuantity");
        dynamicViewEntity.addAlias("PA", "limitQuantity");


        /** 定义活动商品动态视图 */
        dynamicViewEntity.addMemberEntity("PAG", "ProductActivityGoods");
        dynamicViewEntity.addAlias("PAG", "activityId");
        dynamicViewEntity.addAlias("PAG", "productId");
        //add  by gss
        dynamicViewEntity.addAlias("PAG", "isShowIndex");
        dynamicViewEntity.addAlias("PAG", "isAnyReturn");
        dynamicViewEntity.addAlias("PAG", "isSupportOverTimeReturn");
        dynamicViewEntity.addAlias("PAG", "isSupportScore");
        dynamicViewEntity.addAlias("PAG", "isSupportReturnScore");
        dynamicViewEntity.addAlias("PAG", "isPostageFree");


        /** 定义商品价格动态视图*/
        dynamicViewEntity.addMemberEntity("PR", "ProductPrice");
        dynamicViewEntity.addAlias("PR", "price");
        dynamicViewEntity.addAlias("PR", "productPriceTypeId");

        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("PA", "PAC", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        dynamicViewEntity.addViewLink("PA", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
        dynamicViewEntity.addViewLink("PAG", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));

        /** 查询字段 & 排序字段 */
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("activityId");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("publishDate");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("price");
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("productPrice");
        fieldsToSelect.add("activityQuantity");
        fieldsToSelect.add("leaveQuantity");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("hasBuyQuantity");
        fieldsToSelect.add("limitQuantity");
        fieldsToSelect.add("isAnyReturn");
        fieldsToSelect.add("isSupportOverTimeReturn");
        fieldsToSelect.add("isSupportScore");
        fieldsToSelect.add("isSupportReturnScore");
        fieldsToSelect.add("isPostageFree");
        orderBy.add("-createdStamp");
        //TODO 排序字段哪一位，请站出来
        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // begin add by gss 默认价格
        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"));
        //活动状态 审批通过
        andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        //下架时间大于当前时间
        andExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        //秒杀收藏列表
        if ("0".equals(type)) {
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
            //团购收藏列表
        } else if ("1".equals(type)) {
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
        }
        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        /** 查询开始条数*/
        int lowIndex = 0;
        /** 查询结束条数*/
        int highIndex = 0;
        List<GenericValue> activityList = null;
        int activityListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
            lowIndex = viewIndex + 1;
            highIndex = viewIndex + viewSize;
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取分页结果集
            activityList = pli.getPartialList(lowIndex, viewSize);
            //获取记录条数
            activityListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > activityListSize) {
                highIndex = activityListSize;
            }
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (activityList == null) {
            activityList = FastList.newInstance();
        }
        Date now = new Date();
        List<Map<String, Object>> list = FastList.newInstance();
        for (GenericValue v : activityList) {
            Map<String, Object> map = FastMap.newInstance();
            //add  by gss
            map.put("id", v.get("activityId"));//秒杀活动ID 团购活动ID
            map.put("title", v.get("activityName"));//秒杀名称 团购活动名称
            GenericValue product = null;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
                String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                map.put("picSrc", imageUrl);//图片路径
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            //已售数量
            Long hasBuyQuantity = v.getLong("hasBuyQuantity");
            //单个ID限购数量
            Long limitQuantity = v.getLong("limitQuantity");
            //活动数量
            Long activityQuantity = v.getLong("activityQuantity");
            if (hasBuyQuantity == null) {
                hasBuyQuantity = 0L;
            }
            //剩余数量
            Long soldQuantity = activityQuantity - hasBuyQuantity;
            /** 活动商品可用库存 */
            BigDecimal accountingQuantityTotal = BigDecimal.ZERO;
            List<GenericValue> inventory_item = null;
            try {
                inventory_item = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", product.get("productId")));
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            if (UtilValidate.isNotEmpty(inventory_item)) {
                GenericValue inventoryItem = EntityUtil.getFirst(inventory_item);
                accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal");
            }
            map.put("soldNum", hasBuyQuantity);//已售数量  团购已售数量
            //查询该活动已下单数量
            Long buyNum = 0L;
            if ("FINISHED_GOOD".equals(product.get("productTypeId"))) {
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
                entityConditionList.add(EntityCondition.makeCondition("activityId", v.get("activityId")));
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
                EntityListIterator eli = null;
                // do the lookup
                try {
                    eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, findOpts);
                    List<GenericValue> resultList = eli.getCompleteList();
                    eli.close();
                    if (UtilValidate.isNotEmpty(resultList)) {
                        for (GenericValue results : resultList) {
                            buyNum += results.getBigDecimal("quantity").longValue();
                        }
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            } else if ("VIRTUAL_GOOD".equals(product.get("productTypeId"))) {
                List<GenericValue> tickets = new ArrayList<GenericValue>();
                List<EntityCondition> ticketConditiona = FastList.newInstance();
                ticketConditiona.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
                ticketConditiona.add(EntityCondition.makeCondition("activityId", v.get("activityId")));
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
            //可购买的数量
            long needbuyNum = limitQuantity - buyNum;
            if ("0".equals(type)) {
                //未开始：preparation，进行中：ongoing，已结束：finish，已售罄：clearout
                //根据开始时间和结束时间来判断活动状态  add by gss
                Date activityStartDate = v.getTimestamp("activityStartDate");
                Date activityEndDate = v.getTimestamp("activityEndDate");
                //秒杀活动是否已结束
                if (UtilValidate.isNotEmpty(activityEndDate) && now.before(activityEndDate)) {
                    //当前时间大于开始时间 活动进行中
                    if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && soldQuantity == 0 || accountingQuantityTotal.compareTo(BigDecimal.ZERO) == 0) {
                        map.put("state", "clearout");
                    } else if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && (soldQuantity > 0)) {
                        map.put("state", "ongoing");
                    } else if (UtilValidate.isNotEmpty(activityStartDate) && now.before(activityStartDate)) {
                        map.put("state", "preparation");
                    }
                } else {
                    map.put("state", "finish");
                }
                map.put("orgPrice", v.get("price"));//原价
                map.put("curPrice", v.get("productPrice"));//当前价格
                map.put("startTimestamp", v.getTimestamp("activityStartDate").getTime() / 1000);
                map.put("endTimestamp", v.getTimestamp("activityEndDate").getTime() / 1000);
            }
            if ("1".equals(type)) {
                //团购价格
                List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
                try {
                    productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", v.get("activityId")), UtilMisc.toList("orderQuantity"));//阶梯价规则表
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                BigDecimal salePrice = null;
                if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                    salePrice = productGroupOrderRules.get(0).getBigDecimal("orderPrice");
                    for (int i = 0; i < productGroupOrderRules.size(); i++) {
                        if (productGroupOrderRules.get(i).getLong("orderQuantity").compareTo(Long.valueOf(hasBuyQuantity)) <= 0) {
                            salePrice = productGroupOrderRules.get(i).getBigDecimal("orderPrice");
                        } else {
                            break;
                        }
                    }
                }
                map.put("price", salePrice);//团购价格
                List<String> marks = FastList.newInstance();
                if ("Y".equals(v.getString("isAnyReturn"))) {
                    marks.add("随时退");
                }
                if ("Y".equals(v.getString("isSupportOverTimeReturn"))) {
                    marks.add("过期退");
                }
                //if("Y".equals(v.getString("isSupportScore"))) marks.add("可积分");
                //if("Y".equals(v.getString("isSupportReturnScore"))) marks.add("退货返回积分");
                if ("Y".equals(v.getString("isPostageFree"))) {
                    marks.add("包邮");
                }
                //if("Y".equals(v.getString("isShowIndex"))) marks.add("推荐到首页");
                map.put("marks", marks);
                Long num = 0L;
                BigDecimal score = BigDecimal.ZERO;
                DynamicViewEntity dynamicView = new DynamicViewEntity();
                dynamicView.addMemberEntity("PR", "ProductReview");
                dynamicView.addAlias("PR", "productId");
                dynamicView.addAlias("PR", "sumProductRating", "productRating", null, null, null, "sum");
                dynamicView.addAlias("PR", "num", "productReviewId", null, null, null, "count");
                List<EntityCondition> reviewExprs = FastList.newInstance();
                reviewExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, v.get("productId")));
                EntityCondition reviewCond = EntityCondition.makeCondition(reviewExprs, EntityOperator.AND);
                EntityFindOptions findOpts1 = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
                // using list iterator
                EntityListIterator productReviewEli;
                try {
                    productReviewEli = delegator.findListIteratorByCondition(dynamicView,
                            reviewCond
                            , null, null, null, findOpts1);
                    List<GenericValue> prods = productReviewEli.getCompleteList();
                    Iterator<GenericValue> prodsIt = prods.iterator();
                    while (prodsIt.hasNext()) {
                        GenericValue oneProd = prodsIt.next();
                        num = oneProd.getLong("num");
                        score = oneProd.getBigDecimal("sumProductRating");
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
                    score = new BigDecimal(10);
                } else {
                    score = score.divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(2));
                }
                map.put("score", score);//商品评分，1-10分别对应0.5-5星
                map.put("commentNum", num);//评价数据量
            }
            list.add(map);
        }
        resultData.put("max", activityListSize);  //总数
        resultData.put("list", list);
        result.put("resultData", resultData);
        return result;
    }

    /**
     * 删除收藏 add by Wcy 2016.01.28
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> removeMyCollect(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String activityId = (String) context.get("activityId");

        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }

        if (null == userLogin) {
            resultData.put("status", false);
            resultData.put("info", "删除失败");
            result.put("resultData", resultData);
            return result;
        }
        String partyId = userLogin.getString("partyId");
        try {
            GenericValue activityCollect = delegator.findByPrimaryKey("ProductActivityCollection", UtilMisc.toMap("activityId", activityId, "partyId", partyId));
            if (null != activityCollect) {
                activityCollect.remove();
                resultData.put("status", true);
                resultData.put("info", "删除成功");
            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            resultData.put("status", false);
            resultData.put("info", "删除失败");
        }
        result.put("resultData", resultData);
        return result;
    }


    /**
     * 获取专题活动
     * add by gss
     */
    public static Map<String, Object> getProductTopicActivity(DispatchContext dcx, Map<String, ? extends Object> context) {
        /** 获取delegator */
        Delegator delegator = dcx.getDelegator();
        /** 响应结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取参数 */
        List<String> communityId = (List<String>) context.get("communityId");
        List<GenericValue> activityList = null;
        /** 定义活动专题动态视图*/
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PA", "ProductTopicActivity");
        dynamicViewEntity.addAlias("PA", "productTopicActivityId");
        dynamicViewEntity.addAlias("PA", "topicActivityName");
        dynamicViewEntity.addAlias("PA", "smallImg");
        dynamicViewEntity.addAlias("PA", "bigImg");
        dynamicViewEntity.addAlias("PA", "isAllCommunity");
        dynamicViewEntity.addAlias("PA", "sequenceId");
        dynamicViewEntity.addAlias("PA", "isUse");
        dynamicViewEntity.addAlias("PA", "linkUrl");
        dynamicViewEntity.addAlias("PA", "linkType");
        dynamicViewEntity.addAlias("PA", "linkId");

        /** 定义活动专题社区动态视图 */
        dynamicViewEntity.addMemberEntity("PTA", "ProductTopicActivityCommunity");
        dynamicViewEntity.addAlias("PTA", "productTopicActivityCommunityId");
        dynamicViewEntity.addAlias("PTA", "productTopicActivityId");
        dynamicViewEntity.addAlias("PTA", "communityId");

        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("PA", "PTA", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productTopicActivityId", "productTopicActivityId"));
        /** 查询字段 & 排序字段 */
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("linkUrl");
        fieldsToSelect.add("smallImg");
        fieldsToSelect.add("bigImg");
        fieldsToSelect.add("linkType");
        fieldsToSelect.add("linkId");
        fieldsToSelect.add("productTopicActivityId");
        orderBy.add("sequenceId");
        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //如果社区编号为空，则查询所有，查询社区对应的活动
        if (UtilValidate.isNotEmpty(communityId)) {
            List<EntityCondition> exprs_list1 = FastList.newInstance();
            exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllCommunity"), EntityOperator.EQUALS, "1"));
            exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("communityId"), EntityOperator.IN, communityId));

            List<EntityCondition> exprs_list2 = FastList.newInstance();
            exprs_list2.add(EntityCondition.makeCondition(exprs_list1, EntityOperator.AND));
            exprs_list2.add(EntityCondition.makeCondition(UtilMisc.toList(EntityCondition.makeCondition("isAllCommunity", EntityOperator.EQUALS, "0"), EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, "0")), EntityOperator.AND));
            andExprs.add(EntityCondition.makeCondition(exprs_list2, EntityOperator.OR));
        }
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取结果集
            activityList = pli.getCompleteList();
            pli.close();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (activityList == null) {
            activityList = FastList.newInstance();
        }
        List<Map<String, Object>> list = FastList.newInstance();
        for (int i = 0; i < activityList.size(); i++) {

        }
        for (int i = 0; i < activityList.size(); i++) {
            GenericValue v = activityList.get(i);
            Map<String, Object> map = FastMap.newInstance();
            //专题活动类型为促销
            if (UtilValidate.isNotEmpty(v.get("linkType")) && "CX".equals(v.get("linkType"))) {
                String url = "productDetail?activityId=" + v.get("linkId");
                map.put("src", url);//跳转路径
            } else if (UtilValidate.isNotEmpty(v.get("linkType")) && "HD".equals(v.get("linkType"))) {
                try {
                    GenericValue productActivityManager = delegator.findByPrimaryKey("ProductActivityManager", UtilMisc.toMap("productActivityManagerId", v.get("linkId")));
                    if (UtilValidate.isNotEmpty(productActivityManager)) {
                        map.put("src", "1".equals(productActivityManager.get("templateId")) ? "specialList?topicId=" + v.get("linkId") : "specialItem?topicId=" + v.get("linkId"));//跳转路径
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            //偶数个专题
            String contentId = null;
            if (activityList.size() % 2 != 0) {
                contentId = v.getString("smallImg");
            } else {
                if (i == activityList.size() - 1) {
                    contentId = v.getString("bigImg");
                } else {
                    contentId = v.getString("smallImg");
                }
            }
            GenericValue Content;
            try {
                Content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", v.get("smallImg")));
                if (UtilValidate.isNotEmpty(Content)) {
                    GenericValue DataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId")));
                    if (UtilValidate.isNotEmpty(DataResource)) {
                        String imgurl = (String) DataResource.get("objectInfo");
                        String images = imgurl.substring(imgurl.indexOf("/images/datasource"));
                        map.put("picSrc", images);//展示图片路径,
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            list.add(map);
        }
        result.put("activityList", list);
        return result;
    }

    /**
     * 获取专题活动详情接口 add by gss
     */
    public static Map<String, Object> getProductTopicTitle(DispatchContext dcx,
                                                           Map<String, ? extends Object> context) {
        /** 获取delegator */
        Delegator delegator = dcx.getDelegator();
        /** 响应结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数 */
        /** 专题Id */
        String topicId = (String) context.get("topicId");

        GenericValue productActivityManager = null;
        try {
            productActivityManager = delegator.findByPrimaryKey(
                    "ProductActivityManager", UtilMisc.toMap(
                            "productActivityManagerId",
                            topicId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        Map<String, Object> map = FastMap.newInstance();
        map.put("title",
                productActivityManager.getString("activityManagerName"));// 活动标题
        if (UtilValidate.isNotEmpty(productActivityManager
                .getString("activityManagerText"))) {
            GenericValue Content = null;
            try {
                Content = delegator.findByPrimaryKey("Content",
                        UtilMisc.toMap("contentId", productActivityManager
                                .getString("activityManagerText")));
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            if (UtilValidate.isNotEmpty(Content)) {
                GenericValue Contenttext;
                try {
                    Contenttext = delegator.findByPrimaryKey(
                            "ElectronicText",
                            UtilMisc.toMap("dataResourceId",
                                    Content.get("dataResourceId")));
                    if (UtilValidate.isNotEmpty(Contenttext)) {
                        map.put("html", Contenttext.getString("textData"));
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            map.put("html", null);
        }
        result.put("resultData", map);
        return result;
    }

    /**
     * 获取详情 add by GSS 2016.03.1
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getActivityTopicDetail(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取参数  活动ID    类型 HD 活动 CX 促销*/
        String topicId = (String) context.get("topicId");
        //String topicId = "10000";
        String userLoginId = (String) context.get("userLoginId");
        //String userLoginId = "18912979995";
        List<String> communityId = UtilGenerics.cast(context.get("communityId"));

        List<GenericValue> activityList = null;
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
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }

        try {
            /**专题为活动类型*/
            GenericValue productActivityManager = delegator.findByPrimaryKey("ProductActivityManager", UtilMisc.toMap("productActivityManagerId", topicId));
            try {
                if (UtilValidate.isNotEmpty(productActivityManager) && "1".equals(productActivityManager.get("templateId"))) {
                    /** 定义活动专题动态视图*/
                    DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
                    dynamicViewEntity.addMemberEntity("PAM", "ProductActivityManager");
                    dynamicViewEntity.addAlias("PAM", "productActivityManagerId");
                    //活动名称
                    dynamicViewEntity.addAlias("PAM", "activityManagerName");
                    //使用模版类型 1,2
                    dynamicViewEntity.addAlias("PAM", "templateId");
                    //活动正文 contentId
                    dynamicViewEntity.addAlias("PAM", "activityManagerText");

                    /** 定义活动专题关联动态视图 */
                    dynamicViewEntity.addMemberEntity("PAA", "ProductActivityManagerAssoc");
                    dynamicViewEntity.addAlias("PAA", "productActivityManagerId");
                    dynamicViewEntity.addAlias("PAA", "productActivityId");
                    dynamicViewEntity.addAlias("PAA", "imgContentId");
                    dynamicViewEntity.addAlias("PAA", "sequenceId");

                    /** 定义活动表动态视图 */
                    dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
                    dynamicViewEntity.addAlias("PA", "activityId");
                    dynamicViewEntity.addAlias("PA", "activityName");
                    dynamicViewEntity.addAlias("PA", "productPrice");
                    dynamicViewEntity.addAlias("PA", "publishDate");
                    dynamicViewEntity.addAlias("PA", "activityAuditStatus");
                    dynamicViewEntity.addAlias("PA", "activityType");
                    dynamicViewEntity.addAlias("PA", "endDate");
                    //活动总数量
                    dynamicViewEntity.addAlias("PA", "activityQuantity");
                    //剩余数量
                    dynamicViewEntity.addAlias("PA", "leaveQuantity");
                    dynamicViewEntity.addAlias("PA", "activityStartDate");
                    dynamicViewEntity.addAlias("PA", "activityEndDate");
                    dynamicViewEntity.addAlias("PA", "hasBuyQuantity");
                    /** 定义活动商品表动态视图 */
                    dynamicViewEntity.addMemberEntity("PAG", "ProductActivityGoods");
                    dynamicViewEntity.addAlias("PAG", "isAnyReturn");
                    dynamicViewEntity.addAlias("PAG", "isSupportOverTimeReturn");
                    dynamicViewEntity.addAlias("PAG", "isSupportScore");
                    dynamicViewEntity.addAlias("PAG", "isSupportReturnScore");
                    dynamicViewEntity.addAlias("PAG", "isPostageFree");
                    dynamicViewEntity.addAlias("PAG", "isShowIndex");
                    dynamicViewEntity.addAlias("PAG", "activityId");
                    dynamicViewEntity.addAlias("PAG", "productId");

                    /** 定义活动区域动态视图 */
                    dynamicViewEntity.addMemberEntity("PAE", "ProductActivityArea");
                    dynamicViewEntity.addAlias("PAE", "activityId");
                    dynamicViewEntity.addAlias("PAE", "communityId");

                    /** 定义商品价格动态视图*/
                    dynamicViewEntity.addMemberEntity("PR", "ProductPrice");
                    dynamicViewEntity.addAlias("PR", "price");
                    dynamicViewEntity.addAlias("PR", "productPriceTypeId");

                    /** 定义表关联关系 */
                    /*活动与活动关联表*/
                    dynamicViewEntity.addViewLink("PAM", "PAA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productActivityManagerId", "productActivityManagerId"));
                    /*活动关联表与商品活动管理*/
                    dynamicViewEntity.addViewLink("PAA", "PA", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productActivityId", "activityId"));
                    dynamicViewEntity.addViewLink("PA", "PAG", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
                    //dynamicViewEntity.addViewLink("PAG", "PD", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
                    dynamicViewEntity.addViewLink("PA", "PAE", Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
                    dynamicViewEntity.addViewLink("PAG", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
                    /** 查询字段 & 排序字段 */
                    List<String> orderBy = FastList.newInstance();
                    List<String> fieldsToSelect = FastList.newInstance();
                    //活动名称
                    fieldsToSelect.add("activityManagerName");
                    //活动模版
                    fieldsToSelect.add("templateId");
                    //活动正文
                    fieldsToSelect.add("activityManagerText");
                    fieldsToSelect.add("activityId");
                    fieldsToSelect.add("imgContentId");
                    fieldsToSelect.add("activityManagerName");
                    fieldsToSelect.add("publishDate");
                    fieldsToSelect.add("activityName");
                    fieldsToSelect.add("productPrice");
                    fieldsToSelect.add("activityStartDate");
                    fieldsToSelect.add("activityEndDate");
                    fieldsToSelect.add("activityQuantity");
                    fieldsToSelect.add("activityType");
                    fieldsToSelect.add("leaveQuantity");
                    fieldsToSelect.add("productId");
                    fieldsToSelect.add("productActivityId");
                    fieldsToSelect.add("hasBuyQuantity");
                    fieldsToSelect.add("isPostageFree");
                    fieldsToSelect.add("isAnyReturn");
                    fieldsToSelect.add("isSupportOverTimeReturn");
                    fieldsToSelect.add("isSupportScore");
                    fieldsToSelect.add("isSupportReturnScore");

                    orderBy.add("-publishDate");
                    orderBy.add("sequenceId");
                    /** 查询条件 */
                    List<EntityCondition> andExprs = FastList.newInstance();
                    EntityCondition mainCond = null;
                    //社区Id
                    // andExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, Arrays.asList(communityId)));
                    andExprs.add(EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId));
                    //活动ID
                    andExprs.add(EntityCondition.makeCondition("productActivityManagerId", EntityOperator.EQUALS, topicId));
                    //默认价格
                    andExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS, "DEFAULT_PRICE"));
                    //活动状态 审批通过
                    andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                    //下架时间大于当前时间
                    andExprs.add(EntityCondition.makeCondition("endDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                    if (andExprs.size() > 0) {
                        mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                    }
                    /** 查询开始条数*/
                    int lowIndex = 0;
                    /** 查询结束条数*/
                    int highIndex = 0;
                    /*活动中的秒杀或团购总条数*/
                    int activityListSize = 0;
                    try {
                        //计算开始分页值 & 计算分页结束值
                        lowIndex = viewIndex + 1;
                        highIndex = viewIndex + viewSize;
                        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                        //填充查询条件,查询字段，排序字段
                        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
                        //获取结果集
                        activityList = pli.getPartialList(lowIndex, viewSize);
                        activityListSize = pli.getCompleteList().size();
                        pli.close();
                        Date now = new Date();
                        List<Map<String, Object>> list = FastList.newInstance();
                        for (GenericValue v : activityList) {
                            Map<String, Object> map = FastMap.newInstance();
                            //add  by gss
                            map.put("id", v.get("activityId"));//秒杀活动ID 团购活动ID
                            map.put("title", v.get("activityName"));//秒杀名称 团购活动名称
                            GenericValue product;
                            try {
                                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
                                String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                                map.put("picSrc", imageUrl);//图片路径
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            //已售数量
                            Long hasBuyQuantity = v.getLong("hasBuyQuantity");
                            //剩余数量
                            Long leaveQuantity = v.getLong("leaveQuantity");
                            Long activityQuantity = v.getLong("activityQuantity");
                            if (hasBuyQuantity == null) {
                                hasBuyQuantity = 0L;
                            }
                            //Long soldQuantity = activityQuantity-leaveQuantity;
                            map.put("soldNum", hasBuyQuantity);//已售数量  团购已售数量
                            if ("SEC_KILL".equals(v.get("activityType"))) {
                                //未开始：preparation，进行中：ongoing，已结束：finish，已售罄：clearout
                                //根据开始时间和结束时间来判断活动状态  add by gss
                                Date activityStartDate = v.getTimestamp("activityStartDate");
                                Date activityEndDate = v.getTimestamp("activityEndDate");
                                //秒杀活动是否已结束
                                if (UtilValidate.isNotEmpty(activityEndDate) && now.before(activityEndDate)) {
                                    //当前时间大于开始时间 活动进行中
                                    if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && leaveQuantity == 0) {
                                        map.put("state", "clearout");
                                    } else if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate) && (leaveQuantity > 0 || leaveQuantity == null)) {
                                        map.put("state", "ongoing");
                                    } else if (UtilValidate.isNotEmpty(activityStartDate) && now.before(activityStartDate)) {
                                        map.put("state", "preparation");
                                    }
                                } else {
                                    map.put("state", "finish");
                                }
                                map.put("orgPrice", v.get("price"));//原价
                                map.put("curPrice", v.get("productPrice"));//当前价格
                                map.put("startTimestamp", v.getTimestamp("activityStartDate").getTime() / 1000);
                                map.put("endTimestamp", v.getTimestamp("activityEndDate").getTime() / 1000);
                                resultData.put("type", false);  //类型
                            }
                            if ("GROUP_ORDER".equals(v.get("activityType"))) {

                                //团购价格
                                List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
                                try {
                                    productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", v.get("id")), UtilMisc.toList("orderQuantity"));//阶梯价规则表
                                } catch (GenericEntityException e) {
                                    e.printStackTrace();
                                }
                                BigDecimal salePrice = null;
                                if (UtilValidate.isNotEmpty(productGroupOrderRules)) {
                                    salePrice = productGroupOrderRules.get(0).getBigDecimal("orderPrice");
                                    for (int i = 0; i < productGroupOrderRules.size(); i++) {
                                        if (productGroupOrderRules.get(i).getLong("orderQuantity").compareTo(Long.valueOf(hasBuyQuantity)) <= 0) {
                                            salePrice = productGroupOrderRules.get(i).getBigDecimal("orderPrice");
                                        } else {
                                            break;
                                        }
                                    }
                                }
                                map.put("price", salePrice);//团购价格
                                List<String> marks = FastList.newInstance();
                                if ("Y".equals(v.getString("isAnyReturn"))) {
                                    marks.add("随时退");
                                }
                                if ("Y".equals(v.getString("isSupportOverTimeReturn"))) {
                                    marks.add("过期退");
                                }
                                if ("Y".equals(v.getString("isSupportScore"))) {
                                    marks.add("可积分");
                                }
                                if ("Y".equals(v.getString("isSupportReturnScore"))) {
                                    marks.add("退货返回积分");
                                }
                                if ("Y".equals(v.getString("isPostageFree"))) {
                                    marks.add("包邮");
                                }
                                // if("Y".equals(v.getString("isShowIndex"))) marks.add("推荐到首页");
                                map.put("marks", marks);
                                Long num = 0L;
                                BigDecimal score = BigDecimal.ZERO;
                                DynamicViewEntity dynamicView = new DynamicViewEntity();
                                dynamicView.addMemberEntity("PR", "ProductReview");
                                dynamicView.addAlias("PR", "productId");
                                dynamicView.addAlias("PR", "isShow");
                                dynamicView.addAlias("PR", "sumProductRating", "productRating", null, null, null, "sum");
                                dynamicView.addAlias("PR", "num", "productReviewId", null, null, null, "count");
                                List<EntityCondition> reviewExprs = FastList.newInstance();
                                reviewExprs.add(EntityCondition.makeCondition("isShow", EntityOperator.EQUALS, "1"));
                                reviewExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, v.get("productId")));
                                EntityCondition reviewCond = EntityCondition.makeCondition(reviewExprs, EntityOperator.AND);
                                EntityFindOptions findOpts1 = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
                                // using list iterator
                                EntityListIterator productReviewEli;
                                try {
                                    productReviewEli = delegator.findListIteratorByCondition(dynamicView,
                                            reviewCond
                                            , null, null, null, findOpts1);
                                    List<GenericValue> prods = productReviewEli.getCompleteList();
                                    Iterator<GenericValue> prodsIt = prods.iterator();
                                    while (prodsIt.hasNext()) {
                                        GenericValue oneProd = prodsIt.next();
                                        num = oneProd.getLong("num");
                                        score = oneProd.getBigDecimal("sumProductRating");
                                    }
                                } catch (GenericEntityException e) {
                                    e.printStackTrace();
                                }
                                if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
                                    score = new BigDecimal(10);
                                } else {
                                    score = score.divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(2));
                                }
                                map.put("score", score);//商品评分，1-10分别对应0.5-5星
                                map.put("commentNum", num);//评价数据量
                                resultData.put("type", true);  //类型
                            }
                            list.add(map);
                        }
                        resultData.put("max", activityListSize);  //总数
                        resultData.put("list", list);
                    } catch (GenericEntityException e) {
                        Debug.log(e.getMessage());
                    }
                } else {
                    /** 定义活动专题动态视图 */
                    DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
                    dynamicViewEntity.addMemberEntity("PAM", "ProductActivityManager");
                    dynamicViewEntity.addAlias("PAM", "productActivityManagerId");
                    //活动名称
                    dynamicViewEntity.addAlias("PAM", "activityManagerName");
                    //使用模版类型 1,2
                    dynamicViewEntity.addAlias("PAM", "templateId");
                    //活动正文 contentId
                    dynamicViewEntity.addAlias("PAM", "activityManagerText");

                    /** 定义活动专题关联动态视图 */
                    dynamicViewEntity.addMemberEntity("PAA", "ProductActivityManagerAssoc");
                    dynamicViewEntity.addAlias("PAA", "productActivityManagerId");
                    dynamicViewEntity.addAlias("PAA", "productActivityId");
                    dynamicViewEntity.addAlias("PAA", "imgContentId");
                    dynamicViewEntity.addAlias("PAA", "sequenceId");

                    /** 定义活动表动态视图 */
                    dynamicViewEntity.addMemberEntity("PA", "ProductActivity");
                    dynamicViewEntity.addAlias("PA", "activityId");
                    dynamicViewEntity.addAlias("PA", "activityName");
                    dynamicViewEntity.addAlias("PA", "productPrice");
                    dynamicViewEntity.addAlias("PA", "publishDate");
                    dynamicViewEntity.addAlias("PA", "activityAuditStatus");
                    dynamicViewEntity.addAlias("PA", "activityType");
                    dynamicViewEntity.addAlias("PA", "endDate");
                    //活动总数量
                    dynamicViewEntity.addAlias("PA", "activityQuantity");
                    //剩余数量
                    dynamicViewEntity.addAlias("PA", "leaveQuantity");
                    dynamicViewEntity.addAlias("PA", "activityStartDate");
                    dynamicViewEntity.addAlias("PA", "activityEndDate");
                    //** 定义活动区域动态视图 *//*
                    dynamicViewEntity.addMemberEntity("PAE", "ProductActivityArea");
                    dynamicViewEntity.addAlias("PAE", "activityId");
                    dynamicViewEntity.addAlias("PAE", "communityId");

                    /** 定义表关联关系 */
                    dynamicViewEntity.addViewLink("PAM", "PAA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productActivityManagerId", "productActivityManagerId"));
                    /** 查询字段 & 排序字段 */
                    List<String> orderBy = FastList.newInstance();
                    List<String> fieldsToSelect = FastList.newInstance();
                    // 活动名称
                    fieldsToSelect.add("activityManagerName");
                    // 活动正文
                    fieldsToSelect.add("activityManagerText");
                    fieldsToSelect.add("imgContentId");
                    fieldsToSelect.add("activityManagerName");
                    fieldsToSelect.add("productActivityId");
                    orderBy.add("sequenceId");
                    /** 查询条件 */
                    List<EntityCondition> andExprs = FastList.newInstance();
                    EntityCondition mainCond = null;
                    List<EntityCondition> exprs_list1 = FastList
                            .newInstance();

                    exprs_list1.add(EntityCondition.makeCondition(
                            "communityId", EntityOperator.IN, communityId));
                    // 活动ID
                    exprs_list1.add(EntityCondition.makeCondition(
                            "productActivityManagerId",
                            EntityOperator.EQUALS,
                            topicId));
                    // 活动状态 审批通过
                    exprs_list1.add(EntityCondition.makeCondition(
                            "activityAuditStatus", EntityOperator.EQUALS,
                            "ACTY_AUDIT_PASS"));
                    // 下架时间大于当前时间
                    exprs_list1.add(EntityCondition.makeCondition(
                            "endDate",
                            EntityOperator.GREATER_THAN_EQUAL_TO,
                            UtilDateTime.nowTimestamp()));
                    List<EntityCondition> exprs_list2 = FastList
                            .newInstance();
                    exprs_list2.add(EntityCondition.makeCondition(
                            exprs_list1, EntityOperator.AND));
                    exprs_list2.add(EntityCondition.makeCondition(UtilMisc
                                    .toList(EntityCondition.makeCondition(
                                            "productActivityId",
                                            EntityOperator.EQUALS, null),
                                            EntityCondition.makeCondition(
                                                    "productActivityManagerId",
                                                    EntityOperator.EQUALS,
                                                    topicId)),
                            EntityOperator.AND));
                    andExprs.add(EntityCondition.makeCondition(exprs_list2,
                            EntityOperator.OR));
                    if (andExprs.size() > 0) {
                        mainCond = EntityCondition.makeCondition(andExprs,
                                EntityOperator.AND);
                    }
                    /** 查询开始条数 */
                    int lowIndex = 0;
                    /** 查询结束条数 */
                    int highIndex = 0;
                    /* 活动中的秒杀或团购总条数 */
                    int activityListSize = 0;
                    try {
                        // 计算开始分页值 & 计算分页结束值
                        lowIndex = viewIndex + 1;
                        highIndex = viewIndex + viewSize;
                        EntityFindOptions findOpts = new EntityFindOptions(
                                true,
                                EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                                EntityFindOptions.CONCUR_READ_ONLY, -1,
                                highIndex, true);
                        // 填充查询条件,查询字段，排序字段
                        EntityListIterator pli = delegator
                                .findListIteratorByCondition(
                                        dynamicViewEntity, mainCond, null,
                                        fieldsToSelect, orderBy, findOpts);
                        // 获取结果集
                        activityList = pli.getPartialList(lowIndex,
                                viewSize);
                        activityListSize = pli
                                .getCompleteList().size();
                        if (highIndex > activityListSize) {
                            highIndex = activityListSize;
                        }
                        pli.close();
                        Date now = new Date();
                        List<Map<String, Object>> list = FastList
                                .newInstance();
                        for (GenericValue v : activityList) {
                            Map<String, Object> map = FastMap.newInstance();
                            String images = null;
                            GenericValue Content;
                            try {
                                Content = delegator
                                        .findByPrimaryKey(
                                                "Content",
                                                UtilMisc.toMap(
                                                        "contentId",
                                                        v.getString("imgContentId")));
                                if (UtilValidate.isNotEmpty(Content)) {
                                    GenericValue DataResource = delegator
                                            .findByPrimaryKey(
                                                    "DataResource",
                                                    UtilMisc.toMap(
                                                            "dataResourceId",
                                                            Content.get("dataResourceId")));
                                    if (UtilValidate
                                            .isNotEmpty(DataResource)) {
                                        String imgurl = (String) DataResource
                                                .get("objectInfo");
                                        images = imgurl
                                                .substring(imgurl
                                                        .indexOf("/images/datasource"));
                                    }
                                }
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            }
                            map.put("picSrc", images); // 促销对应图片
                            map.put("title", v.get("activityManagerName")); // title促销标题
                            map.put("activityId",
                                    v.get("productActivityId") == null ? null
                                            : v.get("productActivityId"));// 促销对应ID
                            list.add(map);
                        }
                        resultData.put("max", activityListSize); // 总数
                        resultData.put("list", list);
                    } catch (GenericEntityException e) {
                        Debug.log(e.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("resultData", resultData);
        return result;
    }


    /**
     * 获取热门搜索
     * add by gss
     */
    public static Map<String, Object> getHotSearch(DispatchContext dcx, Map<String, ? extends Object> context) {
        /** 获取delegator */
        Delegator delegator = dcx.getDelegator();
        /** 响应结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取参数 */
        List<String> communityId = (List<String>) context.get("communityId");
        List<GenericValue> hotSearchList = null;
        /** 定义活动专题动态视图*/

        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("CHS", "ContentHotSearch");
        dynamicViewEntity.addAlias("CHS", "contentHotSearchId");
        dynamicViewEntity.addAlias("CHS", "hotSearchKeyName");
        dynamicViewEntity.addAlias("CHS", "sequenceNum");
        dynamicViewEntity.addAlias("CHS", "isAllCommunity");

        /** 定义活动专题社区动态视图 */
        dynamicViewEntity.addMemberEntity("HCA", "HotsearchCommunityAssoc");
        dynamicViewEntity.addAlias("HCA", "contentHotSearchId");
        dynamicViewEntity.addAlias("HCA", "communityId");

        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("CHS", "HCA", Boolean.TRUE, ModelKeyMap.makeKeyMapList("contentHotSearchId", "contentHotSearchId"));
        /** 查询字段 & 排序字段 */
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("hotSearchKeyName");
        orderBy.add("-sequenceNum");
        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //如果社区编号为空，则查询所有，查询社区对应的热门搜索
        if (UtilValidate.isNotEmpty(communityId)) {
            List<EntityCondition> exprs_list1 = FastList.newInstance();
            exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllCommunity"), EntityOperator.EQUALS, "1"));
            exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("communityId"), EntityOperator.IN, communityId));
            //exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("communityId"), EntityOperator.IN, Arrays.asList(communityId)));

            List<EntityCondition> exprs_list2 = FastList.newInstance();
            exprs_list2.add(EntityCondition.makeCondition(exprs_list1, EntityOperator.AND));
            exprs_list2.add(EntityCondition.makeCondition("isAllCommunity", EntityOperator.EQUALS, "0"));
            andExprs.add(EntityCondition.makeCondition(exprs_list2, EntityOperator.OR));
        }
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, null, findOpts);
            //获取结果集
            hotSearchList = pli.getPartialList(1, 9);
            pli.close();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (hotSearchList == null) {
            hotSearchList = FastList.newInstance();
        }
        List<String> list = FastList.newInstance();
        for (GenericValue v : hotSearchList) {
            list.add(v.getString("hotSearchKeyName"));
        }
        result.put("resultData", list);
        return result;
    }

    /**
     * 查询满减
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPromos(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");
        /*满减状态*/
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
        //TODO andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.EQUALS, "PROMO_SUBTRACT"));
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.IN, UtilMisc.toList("PROMO_REDUCE", "PROMO_PRE_REDUCE")));
        //关联店铺
        andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
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
                // using list iterator  UtilMisc.makeSetWritable(fieldsToSelect)
                EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

                // get the partial list for this page
                promoList = pli.getPartialList(lowIndex, viewSize);
                // List<GenericValue> actionList = EntityUtil.getRelated("ProductPromoAction",promoList) ;
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

    /**
     * 查询折扣列表
     *
     * @param dcx
     * @param context
     * @return
     */

    public static Map<String, Object> findDiscount(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");
        String productStoreId = (String) context.get("productStoreId");

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

        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.EQUALS, "PROMO_DISCOUNT"));
        //关联店铺
        andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));

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


    /**
     * 查询直降列表
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findStraightDown(DispatchContext dcx, Map<String, ? extends Object> context) {

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
            viewIndex = Integer.parseInt((String) context.get("viewIndex"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("viewSize"));
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

        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.EQUALS, "PROMO_SPE_PRICE"));

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


    /**
     * 查询包邮列表
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPackage(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");
        String productStoreId = (String) context.get("productStoreId");

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
        /*包邮状态*/
        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.IN, UtilMisc.toList("PROMO_FREE_SHIPPING", "PROMO_PRE_FREE_SHIPPING")));

        //关联店铺
        andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));

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

    /**
     * 查询满赠
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPromosGift(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String fromDate = (String) context.get("fromDate");
        String thruDate = (String) context.get("thruDate");
        /*满赠状态*/
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
        andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.IN, UtilMisc.toList("PROMO_GIFT", "PROMO_PRE_GIFT")));
        //关联店铺
        andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
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

    /**
     * 新增折扣 addDiscount
     *
     * @param dtx
     * @param context
     * @return
     */

    public static Map<String, Object> addDiscount(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoName = (String) context.get("promoName");
        String promoCode = (String) context.get("promoCode");
        String promoTypeId = (String) context.get("promoType");
        String promoType = "PROMO_DISCOUNT";
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String productStoreId = (String) context.get("productStoreId");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dtx.getDelegator();
        String promoProductType = "";
        List<String> productids = StringUtil.split(productIds, ",");
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
            List<String> unUsedProductIds = ProductServices.getUnUsedProductIds(delegator, productStoreId, fromDate, thruDate);
            if (UtilValidate.isNotEmpty(unUsedProductIds)) {
                return ServiceUtil.returnError("当前时间已存在指定商品的促销，无法创建全场促销");
            }
        }
        //查看是否有其他全场促销
        boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreId, fromDate, thruDate);
        if (isPromoAllExist) {
            return ServiceUtil.returnError("当前时间已存在全场促销");
        }
        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

//        String promoCode = delegator.getNextSeqId("ProductPromo");
        Map serviceIn = UtilMisc.toMap("promoName", promoName, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
//        serviceIn.put("promoCode", "CX_"+promoCode);

        try {
            String productPromoId = "";
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoId = (String) ret.get("productPromoId");

            promoCode = "CX_" + productPromoId;
            GenericValue updateValue = delegator.makeValue("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId, "promoCode", promoCode));
            updateValue.store();

            List<String> promos = StringUtil.split(promoCondActions, "|");
            List<String> proIds = StringUtil.split(productIds, ",");
            if (UtilValidate.isEmpty(promos)) {
                return ServiceUtil.returnError("配置错误");
            } else {
                for (int i = 0; i < promos.size(); i++) {
                    String condActions = promos.get(i);
                    List<String> condAction = StringUtil.split(condActions, ",");
                    if (UtilValidate.isNotEmpty(condAction)) {
                        //创建促销规则
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "折扣规则", "userLogin", userLogin);
                        String productPromoRuleId = "";
                        ret = dispatcher.runSync("createProductPromoRule", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                        productPromoRuleId = (String) ret.get("productPromoRuleId");

                        String cond = condAction.get(0);
                        String action = condAction.get(1);
                        // 调用创建cond
                        String inputParamEnumId = promoTypeId;
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }

                        // 调用创建action
                        String productPromoActionEnumId = "PROMO_PROD_DISC";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }


                    }
                }
            }

            //促销对应商品
            if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                String productPromoActionSeqId = "_NA_";
                String productPromoRuleId = "_NA_";
                String productPromoCondSeqId = "_NA_";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            }

            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
//                    for (int i = 0; i < productStores.size(); i++) {
//                        GenericValue productStore = productStores.get(i);
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                            "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                }
//                }

            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
//
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 新增包邮
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> addPackage(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoName = (String) context.get("promoName");
        String promoCode = (String) context.get("promoCode");
        String productStoreId = (String) context.get("productStoreId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");

        String promoCondActions = (String) context.get("promoCondActions");
        //全部产品参与
        String promoProductType = "PROMO_PRT_ALL";

        String promoType = "PROMO_FREE_SHIPPING";
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();
//        String promoCode = delegator.getNextSeqId("ProductPromo");

        Map serviceIn = UtilMisc.toMap("promoType", promoType, "promoName", promoName, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
        //自动生成促销编码
//        serviceIn.put("promoCode", "CX_"+promoCode);

        try {
            String productPromoId = "";
            //调用创建促销
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoId = (String) ret.get("productPromoId");
            promoCode = "CX_" + productPromoId;
            GenericValue updateValue = delegator.makeValue("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId, "promoCode", promoCode));
            updateValue.store();
//
            //创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "包邮规则", "userLogin", userLogin);
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            productPromoRuleId = (String) ret.get("productPromoRuleId");
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            if ("PROMO_FREE_SHIPPING".equals(promoType)) {
                String[] condStr = promoCondActions.split(",");
                if (condStr == null || condStr.length != 2) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    //获取页面的值
                    String condValue = condStr[0];

                    // 调用创建cond 促销条件
                    String inputParamEnumId = condStr[1];
                    String operatorEnumId = "PPC_GTE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                            "operatorEnumId", operatorEnumId, "condValue", condValue, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }

                    //调用创建action
                    String productPromoActionEnumId = "PROMO_SHIP_CHARGE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(10), "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }

                }
            }
            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
//                    for (int i = 0; i < productStores.size(); i++) {
//                        GenericValue productStore = productStores.get(i);
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                            "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
//                    }
                }

            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            //会员等级关系

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 新增满减促销
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> addPromoReduce(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String promoText = (String) context.get("promoText");
        String promoType = (String) context.get("promoType");//每满/满
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");

        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        String levelIds = (String) context.get("levelIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();
        //start by nf  2018/03/05
        String promoProductType = "";
        List<String> productids = StringUtil.split(productIds, ",");
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
            List<String> unUsedProductIds = ProductServices.getUnUsedProductIds(delegator, productStoreId, fromDate, thruDate);
            if (UtilValidate.isNotEmpty(unUsedProductIds)) {
                return ServiceUtil.returnError("当前时间已存在指定商品的促销，无法创建全场促销");
            }

        }
        //查看是否有其他全场促销
        boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreId, fromDate, thruDate);
        if (isPromoAllExist) {
            return ServiceUtil.returnError("当前时间已存在全场促销");
        }
        String promoType1 = "PROMO_SUBTRACT";
        //end
        //调用创建促销


        Map serviceIn = UtilMisc.toMap("promoCode", "", "promoName", promoName, "promoText", promoText, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");

        try {
            String productPromoId = "";
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoId = (String) ret.get("productPromoId");
            promoCode = "CX_" + productPromoId;
            GenericValue updateValue = delegator.makeValue("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId, "promoCode", promoCode));
            updateValue.store();
            if ("PROMO_PRE_REDUCE".equals(promoType)) {//每满减
                //创建促销规则
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "促销规则", "userLogin", userLogin);
                ret = dispatcher.runSync("createProductPromoRule", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }
                String productPromoRuleId = (String) ret.get("productPromoRuleId");

                List<String> promos = StringUtil.split(promoCondActions, ",");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    String condition = promos.get(0);
                    String action = promos.get(1);
                    // 调用创建cond
                    String inputParamEnumId = "PPIP_ORDER_TOTAL";
                    String operatorEnumId = "PPC_GTE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                            "operatorEnumId", operatorEnumId, "condValue", condition, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                    //调用创建action
                    String productPromoActionEnumId = "PROMO_PRO_PER_AMDISC";
                    String quantity = "1";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(quantity), "amount", new BigDecimal(action), "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                }

            } else if ("PROMO_REDUCE".equals(promoType)) {
                List<String> promos = StringUtil.split(promoCondActions, "|");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    for (int i = 0; i < promos.size(); i++) {
                        String condActions = promos.get(i);
                        List<String> condAction = StringUtil.split(condActions, ",");
                        if (UtilValidate.isEmpty(condAction)) {
                            return ServiceUtil.returnError("配置错误");
                        } else {
                            //创建促销规则
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "促销规则", "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                            String productPromoRuleId = (String) ret.get("productPromoRuleId");

                            String cond = condAction.get(0);
                            String action = condAction.get(1);
                            // 调用创建cond
                            String inputParamEnumId = "PPIP_ORDER_TOTAL";
                            String operatorEnumId = "PPC_GTE";
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                    "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                            String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                            // 调用创建action
                            String productPromoActionEnumId = "PROMO_PROD_AMDISC";
                            BigDecimal quantity = BigDecimal.ONE;
                            BigDecimal amount = new BigDecimal(action);
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                    "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }
                }

            }

            //促销对应商品
            if ("PROMO_PRT_ALL".equals(promoProductType)) {
                //所有产品生效，
            } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        String productPromoRuleId = "_NA_";
                        String productPromoActionSeqId = "_NA_";
                        String productPromoCondSeqId = "_NA_";
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
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

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 修改促销
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updatePromoReduce(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String promoText = (String) context.get("promoText");
        String promoType = (String) context.get("promoType");//每满/满
        String productStoreId = (String) context.get("productStoreId");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Map<String, Object> ret = null;
        Delegator delegator = dtx.getDelegator();
        LocalDispatcher dispatcher = dtx.getDispatcher();
        List<String> productids = StringUtil.split(productIds, ",");
        //判断该活动的状态
        String chkFlg="Y";
        result.put("chkFlg", chkFlg);
        GenericValue auditInfo =EntityUtil.getFirst(delegator.findByAnd("ProductPromo",UtilMisc.toMap("productPromoId",productPromoId))) ;
        if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
            //此刻是已完成状态，需要判断新增商品互斥
            List<String> addProductIds  = getAddProductIds(delegator,productPromoId,productids);
            if(UtilValidate.isNotEmpty(addProductIds)){
                //check新增的商品存不存在其他促销活动中
                List<String> existProductIds = checkProductPromoExist(delegator,productStoreId,addProductIds,fromDate,thruDate);
                if(UtilValidate.isNotEmpty(existProductIds)){
                    chkFlg="N";
                    result.put("chkFlg", chkFlg);
                    result.put("errorMsg","商品id为：" +Joiner.on(",").join(existProductIds)+"的商品该时间段内已经在其他活动中！");
                    return result;
                }
            }
        }

        String promoProductType = "";

        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }


        try {
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
            //删除之前的数据，然后重新插入
            for (GenericValue promoRule : productPromoRules) {
                String productPromoRuleId = (String) promoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
            }
            delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));

            String promoStatus="ACTY_AUDIT_INIT";
            if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
                promoStatus="ACTY_AUDIT_PASS";
            }

            Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoName", promoName, "promoText", promoText, "promoProductType",
                    promoProductType, "productPromoId", productPromoId, "promoStatus", promoStatus, "promoType", promoType);
            GenericValue updateValue = delegator.makeValue("ProductPromo", serviceIn);
            updateValue.store();

            if ("PROMO_PRE_REDUCE".equals(promoType)) {//每满减
                //创建促销规则
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "促销规则", "userLogin", userLogin);
                ret = dispatcher.runSync("createProductPromoRule", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }
                String productPromoRuleId = (String) ret.get("productPromoRuleId");

                List<String> promos = StringUtil.split(promoCondActions, ",");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    String condition = promos.get(0);
                    String action = promos.get(1);
                    // 调用创建cond
                    String inputParamEnumId = "PPIP_ORDER_TOTAL";
                    String operatorEnumId = "PPC_GTE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                            "operatorEnumId", operatorEnumId, "condValue", condition, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                    //调用创建action
                    String productPromoActionEnumId = "PROMO_PRO_PER_AMDISC";
                    String quantity = "1";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(quantity), "amount", new BigDecimal(action), "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                }

            } else if ("PROMO_REDUCE".equals(promoType)) {
                List<String> promos = StringUtil.split(promoCondActions, "|");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {
                    for (int i = 0; i < promos.size(); i++) {
                        String condActions = promos.get(i);
                        List<String> condAction = StringUtil.split(condActions, ",");
                        if (UtilValidate.isEmpty(condAction)) {
                            return ServiceUtil.returnError("配置错误");
                        } else {
                            //创建促销规则
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "促销规则", "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                            String productPromoRuleId = (String) ret.get("productPromoRuleId");

                            String cond = condAction.get(0);
                            String action = condAction.get(1);
                            // 调用创建cond
                            String inputParamEnumId = "PPIP_ORDER_TOTAL";
                            String operatorEnumId = "PPC_GTE";
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                    "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                            String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                            // 调用创建action
                            String productPromoActionEnumId = "PROMO_PROD_AMDISC";
                            BigDecimal quantity = BigDecimal.ONE;
                            BigDecimal amount = new BigDecimal(action);
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                    "productPromoActionEnumId", productPromoActionEnumId, "quantity", quantity, "amount", amount, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }
                }

            }

            //促销对应商品
            if ("PROMO_PRT_ALL".equals(promoProductType)) {
                //所有产品生效，
            } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        String productPromoRuleId = "_NA_";
                        String productPromoActionSeqId = "_NA_";
                        String productPromoCondSeqId = "_NA_";
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
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

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    public static List<String> checkProductPromoExist(Delegator delegator, String productStoreId, List<String> addProductIds, Timestamp fromDate, Timestamp thruDate) throws GenericEntityException {
        List<String> usedProductIds = ProductServices.getUnUsedProductIds(delegator,productStoreId,fromDate,thruDate);
        List<String> existProductIds = FastList.newInstance();
        for(String addProductId:addProductIds){
            if(usedProductIds.contains(addProductId)){
                existProductIds.add(addProductId);
            }
        }
        return existProductIds;
    }

    public static List<String> getAddProductIds(Delegator delegator, String productPromoId,List<String> newProductIds) throws GenericEntityException {

        List<String> returnProductIds = FastList.newInstance();
        if(UtilValidate.isEmpty(newProductIds)){
            return returnProductIds;
        }

        //未修改之前的产品id
        List<String> oldProductIds = FastList.newInstance();
        List<GenericValue> products = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
        if (products != null && products.size() > 0) {
            for (GenericValue product : products) {
                String productId ="";
                if(UtilValidate.isNotEmpty(product.get("productId"))){
                    productId=product.getString("productId");
                    oldProductIds.add(productId);
                }
            }
        }

        for(String newProductId:newProductIds){
            if(!oldProductIds.contains(newProductId)){
                returnProductIds.add(newProductId);
            }

        }
        return returnProductIds;

    }


    /**
     * 新增直降促销
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> addStraightDown(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoName = (String) context.get("promoName");
        String promoCode = (String) context.get("promoCode");
        String promoTypeId = (String) context.get("promoType");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        Delegator delegator = dtx.getDelegator();
        String promoType = "PROMO_SPE_PRICE";
        String productStoreId = (String) context.get("productStoreId");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String promoProductType = "PROMO_PRT_PART_IN";
//        List<String> productids = StringUtil.split(productIds, ",");
//        if (productids != null && productids.size() > 0) {
//            //有产品id传进来代表部分产品参与
//            promoProductType = "PROMO_PRT_PART_IN";
//        } else {
//            //全部产品参与
//            promoProductType = "PROMO_PRT_ALL";
//
//        }

        //查看是否有其他全场促销
        boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator,productStoreId,fromDate,thruDate);
        if(isPromoAllExist){
            return ServiceUtil.returnFailure("当前时间已存在全场促销");
        }
        //end
        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();
//        String promoCode = delegator.getNextSeqId("ProductPromo");
        Map serviceIn = UtilMisc.toMap("promoName", promoName, "promoType",
                promoType, "promoProductType", promoProductType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
//        serviceIn.put("promoCode", "CX_" + promoCode);

        try {
            String productPromoId = "";
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoId = (String) ret.get("productPromoId");
            promoCode = "CX_" + productPromoId;
            GenericValue updateValue = delegator.makeValue("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId, "promoCode", promoCode));
            updateValue.store();

//            创建促销规则
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "userLogin", userLogin, "ruleName", promoCode + "直降规则");
            String productPromoRuleId = "";
            ret = dispatcher.runSync("createProductPromoRule", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            productPromoRuleId = (String) ret.get("productPromoRuleId");

            List<String> promos = StringUtil.split(promoCondActions, "|");
            List<String> proIds = StringUtil.split(productIds, ",");
            if (UtilValidate.isEmpty(promos)) {
                return ServiceUtil.returnError("配置错误");
            } else if (UtilValidate.isEmpty(proIds)) {
                return ServiceUtil.returnError("配置错误");
            } else {

                for (int i = 0; i < promos.size(); i++) {
                    String action = promos.get(i);
                    String product = proIds.get(i);
                    // 调用创建cond
                    String inputParamEnumId = "PPIP_PRODUCT_TOTAL";
                    String operatorEnumId = "PPC_GTE";
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                            "operatorEnumId", operatorEnumId, "condValue", "1", "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                    String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                    //调用创建action
                    String productPromoActionEnumId = "PROMO_PROD_PRICE";

                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                            "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "productId", product, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                }

            }

            //促销对应商品
//            if (promoProductType.equals("PROMO_PRT_ALL")) {
//                //所有产品生效，
//
//            } else
            if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                String productPromoActionSeqId = "_NA_";
                String productPromoCondSeqId = "_NA_";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            }

            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
//                    for (int i = 0; i < productStores.size(); i++) {
//                        GenericValue productStore = productStores.get(i);
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                            "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
//                    }
                }

            } catch (GenericEntityException e) {
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
     * 新增满赠
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> addPromoGiftService(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String promoText = (String) context.get("promoText");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        String productIds1 = (String) context.get("productIds1");//赠送的商品 ,id的字符串集合
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String promoType = "PROMO_GIFT";
        String promoProductType = "";
        List<String> productids = StringUtil.split(productIds, ",");
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
            List<String> unUsedProductIds = ProductServices.getUnUsedProductIds(delegator, productStoreId, fromDate, thruDate);
            if (UtilValidate.isNotEmpty(unUsedProductIds)) {
                return ServiceUtil.returnError("当前时间已存在指定商品的促销，无法创建全场促销");
            }

        }
        //查看是否有其他全场促销
        boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreId, fromDate, thruDate);
        if (isPromoAllExist) {
            return ServiceUtil.returnError("当前时间已存在全场促销");
        }
        //创建促销主表
        Map serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoText", promoText, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);
        serviceIn.put("useLimitPerOrder", 1L);
        serviceIn.put("useLimitPerCustomer", 1L);
        serviceIn.put("useLimitPerPromotion", 1L);
        serviceIn.put("requireCode", "N");
        serviceIn.put("userEntered", "Y");
        serviceIn.put("showToCustomer", "Y");
        try {
            String productPromoId = "";
            String productPromoCondSeqId = "";
            String productPromoActionSeqId = "";
            Map<String, Object> ret = dispatcher.runSync("createProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            //获得促销主表的id
            productPromoId = (String) ret.get("productPromoId");
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
            promoCode = "CX_" + productPromoId;
            GenericValue updateValue = delegator.makeValue("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId, "promoCode", promoCode));
            updateValue.store();
            //创建促销条件
            String[] condStr = promoCondActions.split(",");
            if (condStr == null || condStr.length != 2) {
                return ServiceUtil.returnError("配置错误");
            }
            String operatorEnumId = "PPC_GTE";
            String inputParamEnumId = condStr[0];
            String condition = condStr[1];
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                    "operatorEnumId", operatorEnumId, "condValue", condition, "userLogin", userLogin);

            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
            //获得促销条件表id
            productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            String productPromoActionEnumId = "PROMO_GWP";
            String quantity = "1";
            String[] productids1Str = productIds1.split(",");
            for (String curPrudId : productids1Str) {
                //调用创建促销动作
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(quantity), "amount", new BigDecimal(1), "productId", curPrudId, "userLogin", userLogin);
                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                productPromoActionSeqId = (String) ret.get("productPromoActionSeqId");
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }
            }
            //促销对应商品
            if ("PROMO_PRT_ALL".equals(promoProductType)) {
                //所有产品生效，
            } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
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
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 修改满赠
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updatePromoGiftService(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String productPromoId = (String) context.get("productPromoId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String promoText = (String) context.get("promoText");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        String productIds1 = (String) context.get("productIds1");//赠送的商品 ,id的字符串集合
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String promoType = "PROMO_GIFT";
        String promoProductType = "";
        List<String> productids = StringUtil.split(productIds, ",");
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();

        String chkFlg="Y";
        result.put("chkFlg", chkFlg);
        GenericValue auditInfo =EntityUtil.getFirst(delegator.findByAnd("ProductPromo",UtilMisc.toMap("productPromoId",productPromoId))) ;
        if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
            //此刻是已完成状态，需要判断新增商品互斥
            List<String> addProductIds  = getAddProductIds(delegator,productPromoId,productids);
            if(UtilValidate.isNotEmpty(addProductIds)){
                //check新增的商品存不存在其他促销活动中
                List<String> existProductIds = checkProductPromoExist(delegator,productStoreId,addProductIds,fromDate,thruDate);
                if(UtilValidate.isNotEmpty(existProductIds)){
                    chkFlg="N";
                    result.put("chkFlg", chkFlg);
                    result.put("errorMsg","商品id为：" +Joiner.on(",").join(existProductIds)+"的商品该时间段内已经在其他活动中！");
                    return result;
                }
            }
        }


        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }

        String promoStatus="ACTY_AUDIT_INIT";
        if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
            promoStatus="ACTY_AUDIT_PASS";
        }

        //创建促销主表
        Map serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "promoName", promoName, "promoText", promoText, "promoProductType",
                promoProductType, "promoType", promoType, "promoStatus", promoStatus);

        try {
            String productPromoCondSeqId = "";
            String productPromoActionSeqId = "";
            GenericValue proPromoGV = delegator.makeValue("ProductPromo", serviceIn);
            proPromoGV.store();
            Map<String, Object> ret = null;
//            删除副表
            Map<String, Object> deleteret = dispatcher.runSync("deleteProductPromoWithoutMainPromo", UtilMisc.toMap("productPromoId", productPromoId));
            if (ServiceUtil.isError(deleteret)) {
                Debug.logError(ServiceUtil.getErrorMessage(deleteret), module);
                return deleteret;
            }
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
//            GenericValue tore();
            //创建促销条件
            String[] condStr = promoCondActions.split(",");
            if (condStr == null || condStr.length != 2) {
                return ServiceUtil.returnError("配置错误");
            }
            String operatorEnumId = "PPC_GTE";
            String inputParamEnumId = condStr[0];
            String condition = condStr[1];
            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                    "operatorEnumId", operatorEnumId, "condValue", condition, "userLogin", userLogin);

            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
            //获得促销条件表id
            productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            String productPromoActionEnumId = "PROMO_GWP";
            String quantity = "1";
            String[] productids1Str = productIds1.split(",");
            for (String curPrudId : productids1Str) {
                //调用创建促销动作
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(quantity), "amount", new BigDecimal(1), "productId", curPrudId, "userLogin", userLogin);
                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                productPromoActionSeqId = (String) ret.get("productPromoActionSeqId");
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }
            }
            //促销对应商品
            if ("PROMO_PRT_ALL".equals(promoProductType)) {
                //所有产品生效，
            } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
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
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 修改包邮 updatePackage
     *
     * @param dtx
     * @param context
     * @return
     */

    public static Map<String, Object> updatePackage(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String) context.get("productStoreId");
        String productPromoId = (String) context.get("productPromoId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");

        String promoCondActions = (String) context.get("promoCondActions");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String promoType = "PROMO_FREE_SHIPPING";
        String promoProductType = (String) context.get("promoProductType");

        String productIds = (String) context.get("productIds");
//        List<String> productids = StringUtil.split(productIds, ",");
//        if(productids!=null&&productids.size()>0){
//            //有产品id传进来代表部分产品参与
//            promoProductType = "PROMO_PRT_PART_IN";
//        }else{
//            //全部产品参与
//            promoProductType="PROMO_PRT_ALL";
//        }

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);

        try {

            Map<String, Object> ret = dispatcher.runSync("updateProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            Delegator delegator = dtx.getDelegator();

            // 创建促销规则
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));

            if (UtilValidate.isNotEmpty(productPromoRules)) {
                GenericValue productPromoRule = productPromoRules.get(0);
                String productPromoRuleId = (String) productPromoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));

                //创建促销条件
                String[] condStr = promoCondActions.split(",");
                if (condStr == null || condStr.length != 2) {
                    return ServiceUtil.returnError("配置错误");
                }
                String operatorEnumId = "PPC_GTE";
                String inputParamEnumId = condStr[1];
                String condition = condStr[0];
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                        "operatorEnumId", operatorEnumId, "condValue", condition, "userLogin", userLogin);

                ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                //获得促销条件表id
                String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }

                String productPromoActionEnumId = "PROMO_SHIP_CHARGE";
                //调用创建促销动作
                serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                        "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(10), "userLogin", userLogin);
                ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                if (ServiceUtil.isError(ret)) {
                    Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                    return ret;
                }

                //调用促销对应店铺
//            查找所有的店铺

                try {
                    List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                    if (UtilValidate.isNotEmpty(productStores)) {
//                        for (int i = 0; i < productStores.size(); i++) {
//                            GenericValue productStore = productStores.get(i);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId, "fromDate", fromDate,
                                "thruDate", thruDate, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
//                        }
                    }

                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
                //会员等级关系
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 修改直降
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updateStraightDown(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
//        String promoProductType = (String) context.get("promoProductType");
        String promoProductType = "";
        String promoType = (String) context.get("promoType");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        List<String> productids = StringUtil.split(productIds, ",");
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);

        try {

            Map<String, Object> ret = dispatcher.runSync("updateProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            Delegator delegator = dtx.getDelegator();

            // 创建促销规则
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));

            if (UtilValidate.isNotEmpty(productPromoRules)) {
                GenericValue productPromoRule = productPromoRules.get(0);
                String productPromoRuleId = (String) productPromoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));

                List<String> promos = StringUtil.split(promoCondActions, "|");
                List<String> proIds = StringUtil.split(productIds, ",");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {

                    for (int i = 0; i < promos.size(); i++) {
                        String action = promos.get(i);
                        String product = proIds.get(i);
                        // 调用创建cond
                        String inputParamEnumId = "PPIP_PRODUCT_TOTAL";
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", "1", "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                        //调用创建action
                        String productPromoActionEnumId = "PROMO_PROD_AMDISC";

                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "productId", product, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

                //促销对应商品
                if ("PROMO_PRT_ALL".equals(promoProductType)) {
                    //所有产品生效，

                } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_INCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }


                } else if ("PROMO_PRT_PART_EX".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_EXCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }
                }

                //调用促销对应店铺
//            查找所有的店铺

                try {
                    List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                    if (UtilValidate.isNotEmpty(productStores)) {
//                        for (int i = 0; i < productStores.size(); i++) {
//                            GenericValue productStore = productStores.get(i);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId, "fromDate", fromDate,
                                "thruDate", thruDate, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
//                        }
                    }

                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
                //会员等级关系
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 修改折扣
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updateDiscount(DispatchContext dtx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String promoName = (String) context.get("promoName");
        String promoCode = (String) context.get("promoCode");
        String promoTypeId = (String) context.get("promoType");
        String promoType = "PROMO_DISCOUNT";
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String productStoreId = (String) context.get("productStoreId");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dtx.getDelegator();
        List<String> productids = StringUtil.split(productIds, ",");


        String chkFlg="Y";
        result.put("chkFlg", chkFlg);
        GenericValue auditInfo =EntityUtil.getFirst(delegator.findByAnd("ProductPromo",UtilMisc.toMap("productPromoId",productPromoId))) ;
        if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
            //此刻是已完成状态，需要判断新增商品互斥
            List<String> addProductIds  = getAddProductIds(delegator,productPromoId,productids);
            if(UtilValidate.isNotEmpty(addProductIds)){
                //check新增的商品存不存在其他促销活动中
                List<String> existProductIds = checkProductPromoExist(delegator,productStoreId,addProductIds,fromDate,thruDate);
                if(UtilValidate.isNotEmpty(existProductIds)){
                    chkFlg="N";
                    result.put("chkFlg", chkFlg);
                    result.put("errorMsg","商品id为：" +Joiner.on(",").join(existProductIds)+"的商品该时间段内已经在其他活动中！");
                    return result;
                }
            }
        }


        String promoProductType = "";
        if (productids != null && productids.size() > 0) {
            //有产品id传进来代表部分产品参与
            promoProductType = "PROMO_PRT_PART_IN";
        } else {
            //全部产品参与
            promoProductType = "PROMO_PRT_ALL";
        }

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();
        String promoStatus="ACTY_AUDIT_INIT";
        if(auditInfo!=null&&"ACTY_AUDIT_PASS".equalsIgnoreCase(auditInfo.getString("promoStatus"))){
            promoStatus="ACTY_AUDIT_PASS";
        }
//        String promoCode = delegator.getNextSeqId("ProductPromo");
        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", promoStatus, "userLogin", userLogin);


        try {
            Map<String, Object> ret = dispatcher.runSync("updateProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }

            GenericValue updateValue = delegator.makeValue("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId, "promoCode", promoCode));
            updateValue.store();

            delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));

            List<String> promos = StringUtil.split(promoCondActions, "|");
            List<String> proIds = StringUtil.split(productIds, ",");
            if (UtilValidate.isEmpty(promos)) {
                return ServiceUtil.returnError("配置错误");
            } else {
                for (int i = 0; i < promos.size(); i++) {
                    String condActions = promos.get(i);
                    List<String> condAction = StringUtil.split(condActions, ",");
                    if (UtilValidate.isNotEmpty(condAction)) {
                        //创建促销规则
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "ruleName", promoCode + "折扣规则", "userLogin", userLogin);
                        String productPromoRuleId = "";
                        ret = dispatcher.runSync("createProductPromoRule", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                        productPromoRuleId = (String) ret.get("productPromoRuleId");

                        String cond = condAction.get(0);
                        String action = condAction.get(1);
                        // 调用创建cond
                        String inputParamEnumId = promoTypeId;
                        String operatorEnumId = "PPC_GTE";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                        String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }

                        // 调用创建action
                        String productPromoActionEnumId = "PROMO_PROD_DISC";
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }


                    }
                }
            }

            //促销对应商品
            if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                List<String> productIdList = StringUtil.split(productIds, ",");
                String productPromoApplEnumId = "PPPA_INCLUDE";
                String productPromoActionSeqId = "_NA_";
                String productPromoRuleId = "_NA_";
                String productPromoCondSeqId = "_NA_";
                if (UtilValidate.isNotEmpty(productIdList)) {
                    for (int i = 0; i < productIdList.size(); i++) {
                        String productId = productIdList.get(i);
                        serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
                    }
                }

            }

            //调用促销对应店铺
//            查找所有的店铺
            try {
                List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                if (UtilValidate.isNotEmpty(productStores)) {
//                    for (int i = 0; i < productStores.size(); i++) {
//                        GenericValue productStore = productStores.get(i);
                    serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId,
                            "fromDate", fromDate, "thruDate", thruDate, "userLogin", userLogin);
                    ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                    if (ServiceUtil.isError(ret)) {
                        Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                        return ret;
                    }
                }
//                }

            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
//
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    /**
     * 修改折扣
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updateDiscount1(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        String productStoreId = (String) context.get("productStoreId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String promoProductType = (String) context.get("promoProductType");
        String paramEnumId = (String) context.get("promoType");
        String promoCondActions = (String) context.get("promoCondActions");
        String productIds = (String) context.get("productIds");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        //调用创建促销
        LocalDispatcher dispatcher = dtx.getDispatcher();

        Map<String, ? extends Object> serviceIn = UtilMisc.toMap("promoCode", promoCode, "promoName", promoName, "promoProductType",
                promoProductType, "productPromoId", productPromoId, "promoStatus", "ACTY_AUDIT_INIT", "userLogin", userLogin);

        try {

            Map<String, Object> ret = dispatcher.runSync("updateProductPromo", serviceIn);
            if (ServiceUtil.isError(ret)) {
                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                return ret;
            }
            Delegator delegator = dtx.getDelegator();

            // 创建促销规则
            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));

            if (UtilValidate.isNotEmpty(productPromoRules)) {
                GenericValue productPromoRule = productPromoRules.get(0);
                String productPromoRuleId = (String) productPromoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));

                List<String> promos = StringUtil.split(promoCondActions, "|");
                if (UtilValidate.isEmpty(promos)) {
                    return ServiceUtil.returnError("配置错误");
                } else {

                    for (int i = 0; i < promos.size(); i++) {
                        String condActions = promos.get(i);
                        List<String> condAction = StringUtil.split(condActions, ",");
//                            if (UtilValidate.isEmpty(condAction)) {
//                                return ServiceUtil.returnError("配置错误");
//                            }else
                        if (UtilValidate.isNotEmpty(condAction)) {
                            String cond = condAction.get(0);
                            String action = condAction.get(1);
                            // 调用创建cond
                            String inputParamEnumId = paramEnumId;
                            String operatorEnumId = "PPC_GTE";
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "inputParamEnumId", inputParamEnumId,
                                    "operatorEnumId", operatorEnumId, "condValue", cond, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoCond", serviceIn);
                            String productPromoCondSeqId = (String) ret.get("productPromoCondSeqId");
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }

                            // 调用创建action
                            String productPromoActionEnumId = "PROMO_PROD_DISC";
                            serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId, "productPromoCondSeqId", productPromoCondSeqId,
                                    "productPromoActionEnumId", productPromoActionEnumId, "quantity", new BigDecimal(1), "amount", new BigDecimal(action), "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoAction", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }

                        }
                    }
                }

                //促销对应商品
                if ("PROMO_PRT_ALL".equals(promoProductType)) {
                    //所有产品生效，

                } else if ("PROMO_PRT_PART_IN".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_INCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }

                } else if ("PROMO_PRT_PART_EX".equals(promoProductType)) {
                    List<String> productIdList = StringUtil.split(productIds, ",");
                    String productPromoApplEnumId = "PPPA_EXCLUDE";
                    String productPromoActionSeqId = "_NA_";
                    String productPromoCondSeqId = "_NA_";
                    if (UtilValidate.isNotEmpty(productIdList)) {
                        for (int i = 0; i < productIdList.size(); i++) {
                            String productId = productIdList.get(i);
                            serviceIn = UtilMisc.toMap("productId", productId, "productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId,
                                    "productPromoApplEnumId", productPromoApplEnumId, "productPromoActionSeqId", productPromoActionSeqId,
                                    "productPromoCondSeqId", productPromoCondSeqId, "userLogin", userLogin);
                            ret = dispatcher.runSync("createProductPromoProduct", serviceIn);
                            if (ServiceUtil.isError(ret)) {
                                Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                                return ret;
                            }
                        }
                    }
                }

                //调用促销对应店铺
//            查找所有的店铺

                try {
                    List<GenericValue> productStores = delegator.findList("ProductStore", null, null, null, null, false);
                    if (UtilValidate.isNotEmpty(productStores)) {
//                        for (int i = 0; i < productStores.size(); i++) {
//                            GenericValue productStore = productStores.get(i);
                        serviceIn = UtilMisc.toMap("productPromoId", productPromoId, "productStoreId", productStoreId, "fromDate", fromDate,
                                "thruDate", thruDate, "userLogin", userLogin);
                        ret = dispatcher.runSync("createProductStorePromoAppl", serviceIn);
                        if (ServiceUtil.isError(ret)) {
                            Debug.logError(ServiceUtil.getErrorMessage(ret), module);
                            return ret;
                        }
//                        }
                    }

                } catch (GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }

            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    public Map<String, Object> productPromoGiftDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
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

                    //查询赠送商品的集合
                    List<Map<String, Object>> productGiftList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(productPromoActions)) {
                        for (GenericValue product_Promo : productPromoActions) {
                            String productId = (String) product_Promo.get("productId");
                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
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
                    result.put("productList1", productGiftList);


                    //product
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoProducts", productPromoProducts);
                    List<Map<String, Object>> productList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue product_Promo : productPromoProducts) {
                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product_Promo.get("productId")));
                            Map<String, Object> map = FastMap.newInstance();
                            map.put("productName", product.get("productName"));
                            GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "DEFAULT_PRICE")));
                            GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "MARKET_PRICE")));
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
                            productList.add(map);
                        }
                    }
                    result.put("productList", productList);
                }
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 查看信息回显 需要遍历 ruleId
     *
     * @param dispatchContext
     * @param context
     * @return productPromoDiscountDetail
     */
    //测试用 测试通过
    public Map<String, Object> productPromoDiscountDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
            result.put("productPromo", productPromo);
            List<GenericValue> productStorePromoAppls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
                GenericValue productStore_PromoAppls = EntityUtil.getFirst(productStorePromoAppls);
                result.put("productStorePromoAppls", productStore_PromoAppls);
            }

            if (UtilValidate.isNotEmpty(productPromo)) {
                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                if (UtilValidate.isNotEmpty(productPromoRules)) {
                    List<Map> condActList = new ArrayList<Map>();
                    for (GenericValue productPromoRule : productPromoRules) {
                        String ruleId = (String) productPromoRule.get("productPromoRuleId");
                        //cond
                        List<EntityCondition> andExprs = FastList.newInstance();
                        andExprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));
                        andExprs.add(EntityCondition.makeCondition("productPromoRuleId", EntityOperator.EQUALS, ruleId));
                        andExprs.add(EntityCondition.makeCondition("inputParamEnumId", EntityOperator.NOT_EQUAL, "PPIP_PARTY_LEVEL"));
                        List<String> orderBy = FastList.newInstance();
                        orderBy.add("productPromoCondSeqId");
                        List<GenericValue> productPromoCondList = delegator.findList("ProductPromoCond", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, orderBy, null, false);
                        String condValue = productPromoCondList.get(0).getString("condValue");
                        String inputParamEnumId = productPromoCondList.get(0).getString("inputParamEnumId");
                        //action
                        List<GenericValue> productPromoActionList = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                        BigDecimal amount = productPromoActionList.get(0).getBigDecimal("amount");

                        Map condActMap = UtilMisc.toMap("condValue", condValue, "inputParamEnumId", inputParamEnumId, "amount", amount);
                        condActList.add(condActMap);
                    }
                    result.put("condActList", condActList);
                }
                //product
                List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
                result.put("productPromoProducts", productPromoProducts);
                List<Map<String, Object>> productList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(productPromoProducts)) {
                    for (GenericValue product_Promo : productPromoProducts) {
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product_Promo.get("productId")));
                        Map<String, Object> map = FastMap.newInstance();
                        map.put("productName", product.get("productName"));
                        GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "DEFAULT_PRICE")));
                        GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "MARKET_PRICE")));
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
                        productList.add(map);
                    }
                }
                result.put("productList", productList);
            }

        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    //之前的代码
    public Map<String, Object> productPromoStraightDownDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        EntityListIterator pli =null;
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
            result.put("productPromo", productPromo);
            List<GenericValue> productStorePromoAppls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
                GenericValue productStore_PromoAppls = EntityUtil.getFirst(productStorePromoAppls);
                result.put("productStorePromoAppls", productStore_PromoAppls);
            }

            if (UtilValidate.isNotEmpty(productPromo)) {

                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                if (UtilValidate.isNotEmpty(productPromoRules)) {
                    List<Map<String, Object>> productList = FastList.newInstance();

                    DynamicViewEntity dynamicView = new DynamicViewEntity();
                    dynamicView.addMemberEntity("PPP", "ProductPromoProduct");
                    dynamicView.addAlias("PPP", "productId");
                    dynamicView.addAlias("PPP", "productPromoId");

                    dynamicView.addMemberEntity("PPA", "ProductPromoAction");
                    dynamicView.addAlias("PPA", "productId");
                    dynamicView.addAlias("PPA", "productPromoId");
                    dynamicView.addAlias("PPA", "amount");
                    dynamicView.addAlias("PPA", "productPromoCondSeqId");
                    dynamicView.addViewLink("PPP", "PPA", false, ModelKeyMap.makeKeyMapList("productId", "productId","productPromoId","productPromoId"));

                    List<EntityCondition> andExprs = FastList.newInstance();
                    andExprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));

                    List<String> orderBy = FastList.newInstance();
                    orderBy.add("productPromoCondSeqId");

                    pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, null, null);
                    List<GenericValue> productPromoProducts = pli.getCompleteList();

                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue product_Promo : productPromoProducts) {
                            Map map = product_Promo.toMap();
                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product_Promo.get("productId")));
                            map.put("productName", product.get("productName"));
                            GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "DEFAULT_PRICE")));
                            GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "MARKET_PRICE")));
                            map.put("productName", product.get("productName"));
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
                            productList.add(map);
                        }
                    }
                    /*GenericValue productPromoRule = productPromoRules.get(0);
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

                    //action
                    List<GenericValue> productPromoActions = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoActions", productPromoActions);
                    //product
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoProducts", productPromoProducts);
                    List<Map<String, Object>> productList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue product_Promo : productPromoProducts) {
                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product_Promo.get("productId")));
                            Map<String, Object> map = FastMap.newInstance();
                            map.put("productName", product.get("productName"));
                            GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "DEFAULT_PRICE")));
                            GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "MARKET_PRICE")));
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
                            productList.add(map);
                        }
                    }*/
                    result.put("productList", productList);
                }
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }finally {
            if(pli!=null){
                pli.close();
            }
        }
        return result;
    }

    public Map<String, Object> productPromoDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
            result.put("productPromo", productPromo);
            List<GenericValue> productStorePromoAppls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
                GenericValue productStore_PromoAppls = EntityUtil.getFirst(productStorePromoAppls);
                result.put("productStorePromoAppls", productStore_PromoAppls);
            }

            if (UtilValidate.isNotEmpty(productPromo)) {
                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                if (UtilValidate.isNotEmpty(productPromoRules)) {
                    List<Map> condActList = new ArrayList<Map>();
                    for (GenericValue productPromoRule : productPromoRules) {
                        String ruleId = (String) productPromoRule.get("productPromoRuleId");
                        //cond
                        List<EntityCondition> andExprs = FastList.newInstance();
                        andExprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));
                        andExprs.add(EntityCondition.makeCondition("productPromoRuleId", EntityOperator.EQUALS, ruleId));
                        andExprs.add(EntityCondition.makeCondition("inputParamEnumId", EntityOperator.NOT_EQUAL, "PPIP_PARTY_LEVEL"));
                        List<String> orderBy = FastList.newInstance();
                        orderBy.add("productPromoCondSeqId");
                        List<GenericValue> productPromoCondList = delegator.findList("ProductPromoCond", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, orderBy, null, false);
                        String condValue = productPromoCondList.get(0).getString("condValue");
                        //action
                        List<GenericValue> productPromoActionList = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                        BigDecimal amount = productPromoActionList.get(0).getBigDecimal("amount");

                        Map condActMap = UtilMisc.toMap("condValue", condValue, "amount", amount);
                        condActList.add(condActMap);
                    }
                    result.put("condActList", condActList);
                    //product
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
                    result.put("productPromoProducts", productPromoProducts);
                    List<Map<String, Object>> productList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue product_Promo : productPromoProducts) {
                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product_Promo.get("productId")));
                            Map<String, Object> map = FastMap.newInstance();
                            map.put("productName", product.get("productName"));
                            GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "DEFAULT_PRICE")));
                            GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_Promo.get("productId"), "productPriceTypeId", "MARKET_PRICE")));
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
                            productList.add(map);
                        }
                    }
                    result.put("productList", productList);
                }
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    public Map<String, Object> editPromoGiftDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
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

                    result.put("productPromoCond", productPromoConds.get(0));
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
                    String productIds1 = "";
                    //赠送商品的id的集合
                    for (GenericValue actionGV : productPromoActions) {
                        String productId = (String) actionGV.get("productId");
                        productIds1 = productIds1 + productId + ",";
                    }
                    productIds1 = productIds1.substring(0, productIds1.length() - 1);
                    result.put("productIds1", productIds1);
                    //product
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoProducts", productPromoProducts);
                    List<Map<String, Object>> productList = FastList.newInstance();
                    String productIds = "";
                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue coupponPrud : productPromoProducts) {
                            String productId = (String) coupponPrud.get("productId");
                            productIds = productIds + productId + ",";

                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
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
                            productList.add(map);
                        }
                        productIds = productIds.substring(0, productIds.length() - 1);

                    }
                    result.put("productIds", productIds);
                    result.put("productList", productList);
                }
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    public Map<String, Object> editPromoReduceDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
            result.put("productPromo", productPromo);
            List<GenericValue> productStorePromoAppls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
                GenericValue productStore_PromoAppls = EntityUtil.getFirst(productStorePromoAppls);
                result.put("productStorePromoAppls", productStore_PromoAppls);
            }
            List<Map> condActList = new ArrayList<Map>();
            if (UtilValidate.isNotEmpty(productPromo)) {
                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                for (GenericValue productPromoRule : productPromoRules) {
                    String ruleId = (String) productPromoRule.get("productPromoRuleId");
                    //cond
                    List<EntityCondition> andExprs = FastList.newInstance();
                    andExprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));
                    andExprs.add(EntityCondition.makeCondition("productPromoRuleId", EntityOperator.EQUALS, ruleId));
                    andExprs.add(EntityCondition.makeCondition("inputParamEnumId", EntityOperator.NOT_EQUAL, "PPIP_PARTY_LEVEL"));
                    List<String> orderBy = FastList.newInstance();
                    orderBy.add("productPromoCondSeqId");
                    List<GenericValue> productPromoCondList = delegator.findList("ProductPromoCond", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, orderBy, null, false);
                    String condValue = productPromoCondList.get(0).getString("condValue");
                    //action
                    List<GenericValue> productPromoActionList = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    BigDecimal amount = productPromoActionList.get(0).getBigDecimal("amount");

                    Map condActMap = UtilMisc.toMap("condValue", condValue, "amount", amount);
                    condActList.add(condActMap);
                }
                result.put("condActList", condActList);
                //product
                List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
                List<Map<String, Object>> productList = FastList.newInstance();
                String productIds = "";
                String productNames = "";
                if (UtilValidate.isNotEmpty(productPromoProducts)) {
                    for (GenericValue coupponPrud : productPromoProducts) {
                        String productId = (String) coupponPrud.get("productId");
                        productIds = productIds + productId + ",";
                    }
                    productIds = productIds.substring(0, productIds.length() - 1);
                    productNames = "";
                }
                result.put("productIds", productIds);
                result.put("productNames", productNames);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 包邮回显数据
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> editPackageDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
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

                    //action
                    List<GenericValue> productPromoActions = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoActions", productPromoActions);

                    //product
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoProducts", productPromoProducts);
                    List<Map<String, Object>> productList = FastList.newInstance();
                    String productIds = "";
                    String productNames = "";
                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue coupponPrud : productPromoProducts) {
                            String productId = (String) coupponPrud.get("productId");
                            productIds = productIds + productId + ",";

                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                            Map<String, Object> map = FastMap.newInstance();
                            map.put("productName", product.get("productName"));
                            productNames = productNames + product.get("productName") + ",";

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
                            productList.add(map);
                        }
                        productIds = productIds.substring(0, productIds.length() - 1);
                        productNames = productNames.substring(0, productNames.length() - 1);
                    }
                    result.put("productIds", productIds);
                    result.put("productNames", productNames);
                    result.put("productList", productList);
                }
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 直降回显数据
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> editStraightDownDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
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

                    //action
                    List<GenericValue> productPromoActions = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoActions", productPromoActions);

                    //product
                    List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                    result.put("productPromoProducts", productPromoProducts);
                    List<Map<String, Object>> productList = FastList.newInstance();
                    String productIds = "";
                    String productNames = "";
                    if (UtilValidate.isNotEmpty(productPromoProducts)) {
                        for (GenericValue coupponPrud : productPromoProducts) {
                            String productId = (String) coupponPrud.get("productId");
                            productIds = productIds + productId + ",";

                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                            Map<String, Object> map = FastMap.newInstance();
                            map.put("productName", product.get("productName"));
                            productNames = productNames + product.get("productName") + ",";

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
                            productList.add(map);
                        }
                        productIds = productIds.substring(0, productIds.length() - 1);
                        productNames = productNames.substring(0, productNames.length() - 1);
                    }
                    result.put("productIds", productIds);
                    result.put("productNames", productNames);
                    result.put("productList", productList);
                }
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 折扣回显示数据
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> editDiscountDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            GenericValue productPromo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
            result.put("productPromo", productPromo);
            List<GenericValue> productStorePromoAppls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isNotEmpty(productStorePromoAppls)) {
                GenericValue productStore_PromoAppls = EntityUtil.getFirst(productStorePromoAppls);
                result.put("productStorePromoAppls", productStore_PromoAppls);
            }

            if (UtilValidate.isNotEmpty(productPromo)) {
                List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
                if (UtilValidate.isNotEmpty(productPromoRules)) {
                    List<Map> condActList = new ArrayList<Map>();
                    for (GenericValue productPromoRule : productPromoRules) {
                        String ruleId = (String) productPromoRule.get("productPromoRuleId");
                        //cond
                        List<EntityCondition> andExprs = FastList.newInstance();
                        andExprs.add(EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId));
                        andExprs.add(EntityCondition.makeCondition("productPromoRuleId", EntityOperator.EQUALS, ruleId));
                        andExprs.add(EntityCondition.makeCondition("inputParamEnumId", EntityOperator.NOT_EQUAL, "PPIP_PARTY_LEVEL"));
                        List<String> orderBy = FastList.newInstance();
                        orderBy.add("productPromoCondSeqId");
                        List<GenericValue> productPromoCondList = delegator.findList("ProductPromoCond", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, orderBy, null, false);
                        String condValue = productPromoCondList.get(0).getString("condValue");
                        String inputParamEnumId = productPromoCondList.get(0).getString("inputParamEnumId");
                        //action
                        List<GenericValue> productPromoActionList = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", ruleId));
                        BigDecimal amount = productPromoActionList.get(0).getBigDecimal("amount");

                        Map condActMap = UtilMisc.toMap("condValue", condValue, "inputParamEnumId", inputParamEnumId, "amount", amount);
                        condActList.add(condActMap);
                    }
                    result.put("condActList", condActList);
                }

                //product
                List<GenericValue> productPromoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
                result.put("productPromoProducts", productPromoProducts);
                List<Map<String, Object>> productList = FastList.newInstance();
                String productIds = "";
                String productNames = "";
                if (UtilValidate.isNotEmpty(productPromoProducts)) {
                    for (GenericValue coupponPrud : productPromoProducts) {
                        String productId = (String) coupponPrud.get("productId");
                        productIds = productIds + productId + ",";

                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                        Map<String, Object> map = FastMap.newInstance();
                        map.put("productName", product.get("productName"));
                        productNames = productNames + product.get("productName") + ",";

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
                        productList.add(map);
                    }
                    productIds = productIds.substring(0, productIds.length() - 1);
                    productNames = productNames.substring(0, productNames.length() - 1);
                }
                result.put("productIds", productIds);
                result.put("productNames", productNames);
                result.put("productList", productList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /*获取市场价格和销售价格
     *
     * add by gss
     * */
    public Map<String, Object> findPrice(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productId = (String) context.get("productId");
        Delegator delegator = dispatchContext.getDelegator();
        List<Map<String, Object>> productList = FastList.newInstance();
        try {
            Map<String, Object> map = FastMap.newInstance();
            GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")));
            GenericValue marketPrice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE")));
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
            productList.add(map);
            result.put("productList", productList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 设置促销在店铺的结束时间
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> endPromoReduce(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Timestamp endDate = (Timestamp) context.get("endDate");
        String productPromoId = (String) context.get("productPromoId");
        String productStoreId = (String) context.get("productStoreId");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();

        try {
            List<GenericValue> promos = delegator.findByAnd("ProductStorePromoAndAppl", UtilMisc.toMap("productPromoId", productPromoId));
            if (UtilValidate.isEmpty(promos)) {
                result.put("chkFlg", "N");
                result.put("errorMsg", "查找不到该促销");
                return result;
            }
            GenericValue promo = promos.get(0);
            Timestamp thruDate = promo.getTimestamp("thruDate");
            Timestamp fromDate = promo.getTimestamp("fromDate");

            // 取得直降的产品列表信息
            List<GenericValue> promoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));

            List<String> productIdRedisList = FastList.newInstance();
            if (endDate.getTime() < thruDate.getTime()) {
                //说明时间未过期可以直接修改
                promo.set("thruDate", endDate);
                delegator.store(promo);
                result.put("chkFlg", "Y");

                if (UtilValidate.isNotEmpty(promoProducts)) {
                    for (GenericValue gv : promoProducts) {
                        if (UtilValidate.isNotEmpty(gv)) {
                            String curProductId = gv.getString("productId");
                            if (UtilValidate.isNotEmpty(curProductId)) {
                                productIdRedisList.add(curProductId);
                            }
                            // Redis 信息的删除处理
//                            if(UtilRedis.exists(curProductId+"_summary")){
//                                UtilRedis.del(curProductId+"_summary");// 产品缓存
//                            }
//                            if(UtilRedis.exists(curProductId+"_downPromo")){
//                                UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                            }
//                            if(UtilRedis.exists(curProductId+"_groupOrder")){
//                                UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                            }
//                            if(UtilRedis.exists(curProductId+"_seckill")) {
//                                UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                            }

                        }
                    }

                    if (UtilValidate.isNotEmpty(productIdRedisList)) {
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

                return result;
            }
            //判断有没有商品在活动中
            List<GenericValue> products = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
            List<String> productIds = FastList.newInstance();
            if (products != null && products.size() > 0) {
                for (GenericValue product : products) {
                    if (UtilValidate.isNotEmpty(product.get("productId"))) {
                        String productId = product.getString("productId");
                        productIds.add(productId);
                    }
                }
            }
            List<String> unUserdProductIdInfos = ProductServices.getUnUsedProductIds(delegator, productStoreId, fromDate, endDate);
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
//            String existProductStr = Joiner.on(",").join(productIdExists);

            result.put("chkFlg", chkFlg);
            if ("N".equalsIgnoreCase(chkFlg)) {
                result.put("errorMsg", "当前时间已存在指定商品的促销!");
            }
            if ("Y".equalsIgnoreCase(chkFlg)) {
                boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreId, fromDate, endDate);
                if (isPromoAllExist) {
                    chkFlg = "N";
                    result.put("chkFlg", chkFlg);
                    result.put("errorMsg", "当前时间已存在全场促销");
                }
            }


            if ("Y".equalsIgnoreCase(chkFlg)) {
                promo.set("thruDate", endDate);
                delegator.store(promo);
                productIdRedisList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(promoProducts)) {
                    for (GenericValue gv : promoProducts) {
                        if (UtilValidate.isNotEmpty(gv)) {
                            String curProductId = gv.getString("productId");
                            if (UtilValidate.isNotEmpty(curProductId)) {
                                productIdRedisList.add(curProductId);
                            }
                            // Redis 信息的删除处理
//                            if(UtilRedis.exists(curProductId+"_summary")){
//                                UtilRedis.del(curProductId+"_summary");// 产品缓存
//                            }
//                            if(UtilRedis.exists(curProductId+"_downPromo")){
//                                UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                            }
//                            if(UtilRedis.exists(curProductId+"_groupOrder")){
//                                UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                            }
//                            if(UtilRedis.exists(curProductId+"_seckill")) {
//                                UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                            }

                        }
                    }
                    if (UtilValidate.isNotEmpty(productIdRedisList)) {
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

        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 删除促销
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> deleteProductPromo(DispatchContext dcx, Map<String, ? extends Object> context) {
        //OrderAdjustment，QuoteAdjustment


        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        try {
           /* long orderProCount = delegator.findCountByCondition("OrderAdjustment", EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId), null, null);
            if (orderProCount > 0l) {
                return ServiceUtil.returnError("订单已经使用了促销，不能删除");
            }
            long quoteProCount = delegator.findCountByCondition("QuoteAdjustment", EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId), null, null);
            if (quoteProCount > 0l) {
                return ServiceUtil.returnError("订单已经使用了促销，不能删除");
            }*/

            List<GenericValue> productPromoRules = delegator.findByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
            //删除之前的数据，然后重新插入
            for (GenericValue promoRule : productPromoRules) {
                String productPromoRuleId = (String) promoRule.get("productPromoRuleId");
                //删除cond,action,product,store
                delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId, "productPromoRuleId", productPromoRuleId));
                delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", productPromoId));
            }

            // 取得直降的产品列表信息
            List<GenericValue> promoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
            List<String> productIdRedisList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(promoProducts)) {
                for (GenericValue gv : promoProducts) {
                    if (UtilValidate.isNotEmpty(gv)) {
                        String curProductId = gv.getString("productId");
                        if (UtilValidate.isNotEmpty(curProductId)) {
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
                if (UtilValidate.isNotEmpty(productIdRedisList)) {
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
            }

            delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", productPromoId));
            delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));


        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    public Map<String, Object> auditPromoReduce(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoId = (String) context.get("productPromoId");
        String message = (String) context.get("auditMessage");
        String status = (String) context.get("promoStatus");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();

        //添加审批日志
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String partyId = userLogin.getString("partyId");

        try {
            GenericValue auditLog = delegator.makeValue("PromoAuditLog");
            auditLog.setNextSeqId();
            String businessId = "promo_" + promoId;
            auditLog.put("businessId", businessId);
            auditLog.put("auditType", "ACTY_AUDIT_PASS".equals(status) ? "通过" : "驳回");
            auditLog.put("auditPerson", partyId);
            auditLog.put("auditMessage", message);
            auditLog.put("createDate", new Timestamp(System.currentTimeMillis()));
            auditLog.create();

            GenericValue audit = delegator.makeValue("ProductPromoAudit");
            String promoAuditId = delegator.getNextSeqId("ProductPromoAudit");
            audit.set("promoAuditId", promoAuditId);
            audit.set("productPromoId", promoId);
            audit.set("auditMessage", message);
            audit.set("result", status);
            delegator.create(audit);
            GenericValue promo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", promoId));
            promo.set("promoStatus", status);
            delegator.store(promo);

            // 取得直降的产品列表信息
            List<GenericValue> promoProducts = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", promoId));
            List<String> productIdRedisList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(promoProducts)) {
                for (GenericValue gv : promoProducts) {
                    if (UtilValidate.isNotEmpty(gv)) {
                        String curProductId = gv.getString("productId");
                        if (UtilValidate.isNotEmpty(curProductId)) {
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

                if (UtilValidate.isNotEmpty(productIdRedisList)) {
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
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /*批量审批
     *
     * add by gss
     * */
    public Map<String, Object> batchAuditPromoReduce(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String obj = (String) context.get("obj");
        String status = (String) context.get("promoStatus");
        // 截取字符串
        String[] m = obj.split(",");
        String opinion = null;
        // 满减促销Id
        String productPromoId = "";
        // 审核意见
        String auditMessage = "";

        for (int i = 0; i < m.length; i++) {
            opinion = m[i];
            String[] n = opinion.split(":");
            if (n.length > 1) {
                productPromoId = n[0];
                auditMessage = n[1];
            } else {
                productPromoId = n[0];
            }

            try {
                Delegator delegator = dcx.getDelegator();
                GenericValue audit = delegator.makeValue("ProductPromoAudit");
                String promoAuditId = delegator.getNextSeqId("ProductPromoAudit");
                audit.set("promoAuditId", promoAuditId);
                audit.set("productPromoId", productPromoId);
                audit.set("auditMessage", auditMessage);
                audit.set("result", status);
                delegator.create(audit);
                GenericValue promo = delegator.findByPrimaryKey("ProductPromo", UtilMisc.toMap("productPromoId", productPromoId));
                promo.set("promoStatus", status);
                delegator.store(promo);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return result;
    }

    /* 查看满减驳回原因
     * add  2016-5-5
     * add by gss
     *
     * */
    public Map<String, Object> findAuditMessage(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dcx.getDelegator();
        List<GenericValue> ProductPromoAuditList;
        try {
            ProductPromoAuditList = delegator.findByAnd("ProductPromoAudit", UtilMisc.toMap("productPromoId", productPromoId), UtilMisc.toList("createdStamp"));
            GenericValue ProductPromoAudit = EntityUtil.getFirst(ProductPromoAuditList);
            result.put("ProductPromoAudit", ProductPromoAudit);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据组合商品编码取得组合商品信息
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> getProductGroupingProductIdsById(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productGrpId = (String) context.get("productGrpId");
        Delegator delegator = dispatchContext.getDelegator();
        List<String> productList = FastList.newInstance();
        try {
            // 取得组合商品信息
            GenericValue productGroupPromo = delegator.findByPrimaryKey("ProductGroupPromo", UtilMisc.toMap("productGrpId", productGrpId));
            String startDate = "";
            String endDate = "";
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 取得组合商品和商品的关联信息
            if (UtilValidate.isNotEmpty(productGroupPromo)) {
                if (UtilValidate.isNotEmpty(productGroupPromo.getTimestamp("fromDate")) && UtilValidate.isNotEmpty(productGroupPromo.getTimestamp("thruDate"))) {
                    startDate = df.format(productGroupPromo.getTimestamp("fromDate"));
                    endDate = df.format(productGroupPromo.getTimestamp("thruDate"));
                }
                List<GenericValue> proGrpPromoProducts = delegator.findByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", productGrpId));
                if (UtilValidate.isNotEmpty(proGrpPromoProducts)) {
                    for (GenericValue proGrpPromoProductInfo : proGrpPromoProducts) {
                        String productId = proGrpPromoProductInfo.getString("productId");
                        if (UtilValidate.isNotEmpty(productId)) {
                            productList.add(productId);
                        }
                    }
                }
                result.put("productList", productList);
            }
            result.put("startDate", startDate);
            result.put("endDate", endDate);
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    // Add by zhajh at 20180313 商品组合 Begin

    /**
     * 查询商品组合列表
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findProductGrouping(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        /*商品组合状态*/
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


        List<GenericValue> productGroupList = FastList.newInstance();
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
        fieldsToSelect.add("promoPrice");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("thruDate");
        fieldsToSelect.add("promoStatus");
        fieldsToSelect.add("productGrpId");
        fieldsToSelect.add("createdStamp");
        List<String> orderBy = FastList.newInstance();
        orderBy.add("-createdStamp");

        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(promoName)) {
            paramList = paramList + "&promoName=" + promoName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoName + "%")));
        }
        if (UtilValidate.isNotEmpty(promoCode)) {
            paramList = paramList + "&promoCode=" + promoCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + promoCode + "%")));
        }
        
        /*组合商品状态*/
        if (UtilValidate.isNotEmpty(promoStatus)) {
            andExprs.add(EntityCondition.makeCondition("promoStatus",
                    EntityOperator.EQUALS, promoStatus));
        }

        // 店铺编码
        String productStoreId = (String) context.get("productStoreId");
        if(UtilValidate.isNotEmpty(productStoreId)) {
            andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
        }
        // build the main condition
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        if ("Y".equals(lookupFlag)) {
            try {
                // get the indexes for the partial list
                lowIndex = viewIndex * viewSize + 1;
                highIndex = (viewIndex + 1) * viewSize;

                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                //EntityListIterator pli = delegator.find("ProductGroupPromo", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
                EntityListIterator pli = delegator.find("ProductStorePromoAndGrp", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

                // get the partial list for this page
                productGroupList = pli.getPartialList(lowIndex, viewSize);

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
        result.put("productGroupList", productGroupList);
        result.put("promoCode", promoCode);
        result.put("promoName", promoName);
        result.put("promoStatus", promoStatus);

        result.put("productGroupListSize", Integer.valueOf(listSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 商品组合的编辑
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updateProductGroupingService(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productGrpId = (String) context.get("productGrpId");
        String promoCode = (String) context.get("promoCode");
        String promoName = (String) context.get("promoName");
        String promoPrice = (String) context.get("promoPrice");
        Timestamp fromDate = (Timestamp) context.get("fromDate");
        Timestamp thruDate = (Timestamp) context.get("thruDate");
        String productGroupInfos = (String) context.get("productGroupInfos"); // 组合商品编码信息
        String productStoreId = (String) context.get("productStoreId"); // 商品店铺信息
        String productIds = (String) context.get("productIds"); // 商品信息
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();

        try {

            if (UtilValidate.isEmpty(productGrpId)) {
                //查看是否有其他全场促销
                boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreId, fromDate, thruDate);
                if (isPromoAllExist) {
                    return ServiceUtil.returnError("当前时间已存在全场促销");
                }

                // 新增的场合
                //创建商品组合促销主表
                GenericValue productGroupPromo = null;
                String productGroupPromoIdCreate = delegator.getNextSeqId("ProductGroupPromo");
                productGroupPromo = delegator.makeValue("ProductGroupPromo", UtilMisc.toMap("productGrpId", productGroupPromoIdCreate));
                // 组合名称
                if (UtilValidate.isNotEmpty(promoName)) {
                    productGroupPromo.set("promoName", promoName);
                }
                // 组合编码
                if (UtilValidate.isNotEmpty(productGroupPromoIdCreate)) {
                    productGroupPromo.set("promoCode", "CX_" + productGroupPromoIdCreate);
                }
                // 优惠金额
                if (UtilValidate.isNotEmpty(promoPrice)) {
                    productGroupPromo.set("promoPrice", new BigDecimal(promoPrice));
                }
                // 状态
                productGroupPromo.set("promoStatus", "ACTY_AUDIT_INIT");

                // 开始时间
                if (UtilValidate.isNotEmpty(fromDate)) {
                    productGroupPromo.set("fromDate", fromDate);
                }
                // 结束时间
                if (UtilValidate.isNotEmpty(thruDate)) {
                    productGroupPromo.set("thruDate", thruDate);
                }
                // 创建表
                productGroupPromo.create();

                // 建立组合商品和商品之间的关系
                if (UtilValidate.isNotEmpty(productGroupInfos)) {
                    String[] pgInfos = productGroupInfos.split(",");
                    if (UtilValidate.isNotEmpty(pgInfos)) {
                        for (String pgInfo : pgInfos) {
                            String[] attrInfos = pgInfo.split("\\|");
                            String productId = attrInfos[0]; // 商品编码
                            String quantity = attrInfos[1]; // 商品数量

                            // 创建产品配置明细
                            GenericValue proGrpPromoProduct = null;
                            Map<String, Object> proGrpPromoProductMap = FastMap.newInstance();
                            // 组合商品ID
                            proGrpPromoProductMap.put("productGrpId", productGroupPromoIdCreate);
                            // 商品ID
                            proGrpPromoProductMap.put("productId", productId);
                            // 商品数量
                            proGrpPromoProductMap.put("quantity", Long.parseLong(quantity));
                            proGrpPromoProduct = delegator.makeValue("ProGrpPromoProduct", proGrpPromoProductMap);
                            // 创建表
                            proGrpPromoProduct.create();

                        }
                    }
                }
                // 建立商品关联店铺
                if (UtilValidate.isNotEmpty(productStoreId)) {
                    GenericValue productStoreProductGrpAppl = null;
                    Map<String, Object> productStoreProductGrpApplMap = FastMap.newInstance();
                    // 商品店铺编码
                    productStoreProductGrpApplMap.put("productStoreId", productStoreId);
                    // 组合商品编码
                    productStoreProductGrpApplMap.put("productGrpId", productGroupPromoIdCreate);
                    // 促销开始时间
                    productStoreProductGrpApplMap.put("fromDate", fromDate);
                    // 促销结束时间
                    productStoreProductGrpApplMap.put("thruDate", thruDate);
                    productStoreProductGrpAppl = delegator.makeValue("ProductStoreProductGrpAppl", productStoreProductGrpApplMap);
                    // 创建表
                    productStoreProductGrpAppl.create();
                }
            } else {
                // 更新的场合
                // 根据组合商品编码取得组合商品信息
                GenericValue productGroupPromoForUpdate = delegator.findByPrimaryKey("ProductGroupPromo", UtilMisc.toMap("productGrpId", productGrpId));
                List<String> productIdList = StringUtil.split(productIds, ",");
                //判断该活动的状态
                String chkFlg="Y";
                result.put("chkFlg", chkFlg);
                if(UtilValidate.isNotEmpty(productGroupPromoForUpdate)){
                    if("ACTY_AUDIT_PASS".equalsIgnoreCase(productGroupPromoForUpdate.getString("promoStatus"))){
                        //此刻是已完成状态，需要判断新增商品互斥
                        //未修改之前的产品id
                        List<String> oldProductIds = FastList.newInstance();
                        List<String> returnProductIds = FastList.newInstance();
                        List<GenericValue> proGrpPromoProducts = delegator.findByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", productGrpId));

                        if (UtilValidate.isNotEmpty(proGrpPromoProducts)) {
                            for (GenericValue proGrpPromoProductInfo : proGrpPromoProducts) {
                                String productId = proGrpPromoProductInfo.getString("productId");
                                if (UtilValidate.isNotEmpty(productId)) {
                                    oldProductIds.add(productId);
                                }
                            }
                        }


                        for(String newProductId:productIdList){
                            if(!oldProductIds.contains(newProductId)){
                                returnProductIds.add(newProductId);
                            }

                        }
                        if(UtilValidate.isNotEmpty(returnProductIds)){
                            //check新增的商品存不存在其他促销活动中
                            List<String> existProductIds = checkProductPromoExist(delegator,productStoreId,returnProductIds,fromDate,thruDate);
                            if(UtilValidate.isNotEmpty(existProductIds)){
                                chkFlg="N";
                                result.put("chkFlg", chkFlg);
                                result.put("errorMsg","商品id为：" +Joiner.on(",").join(existProductIds)+"的商品该时间段内已经在其他活动中！");
                                return result;
                            }
                        }
                    }
                }



                if (UtilValidate.isNotEmpty(productGroupPromoForUpdate)) {

                    // 组合名称
                    if (UtilValidate.isNotEmpty(promoName)) {
                        productGroupPromoForUpdate.set("promoName", promoName);
                    }
                    // 组合编码

                    // 优惠金额
                    if (UtilValidate.isNotEmpty(promoPrice)) {
                        productGroupPromoForUpdate.set("promoPrice", new BigDecimal(promoPrice));
                    }
                    // 状态
                    if(!("ACTY_AUDIT_PASS").equals(productGroupPromoForUpdate.getString("promoStatus"))){
                        productGroupPromoForUpdate.set("promoStatus", "ACTY_AUDIT_INIT");
                    }
                    // 开始时间
                    if (UtilValidate.isNotEmpty(fromDate)) {
                        productGroupPromoForUpdate.set("fromDate", fromDate);
                    }
                    // 结束时间
                    if (UtilValidate.isNotEmpty(thruDate)) {
                        productGroupPromoForUpdate.set("thruDate", thruDate);
                    }
                    // 更新表
                    productGroupPromoForUpdate.store();
                }

                // 建立组合商品和商品之间的关系
                List<GenericValue> proGrpPromoProductList = delegator.findByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", productGrpId));
                if (UtilValidate.isNotEmpty(productGroupInfos)) {
                    // 删除与商品的关联关系
                    if (UtilValidate.isNotEmpty(proGrpPromoProductList)) {
                        delegator.removeAll(proGrpPromoProductList);
                    }
                    String[] pgInfos = productGroupInfos.split(",");
                    if (UtilValidate.isNotEmpty(pgInfos)) {
                        for (String pgInfo : pgInfos) {
                            String[] attrInfos = pgInfo.split("\\|");
                            String productId = attrInfos[0]; // 商品编码
                            String quantity = attrInfos[1]; // 商品数量

                            // 创建产品配置明细
                            GenericValue proGrpPromoProduct = null;
                            Map<String, Object> proGrpPromoProductMap = FastMap.newInstance();
                            // 组合商品ID
                            proGrpPromoProductMap.put("productGrpId", productGrpId);
                            // 商品ID
                            proGrpPromoProductMap.put("productId", productId);
                            // 商品数量
                            proGrpPromoProductMap.put("quantity", Long.parseLong(quantity));
                            proGrpPromoProduct = delegator.makeValue("ProGrpPromoProduct", proGrpPromoProductMap);
                            // 创建表
                            proGrpPromoProduct.create();
                        }
                    }
                } else {
                    if (UtilValidate.isNotEmpty(proGrpPromoProductList)) {
                        delegator.removeAll(proGrpPromoProductList);
                    }
                }
                // 建立商品关联店铺
                List<GenericValue> productStoreProductGrpApplList = delegator.findByAnd("ProductStoreProductGrpAppl", UtilMisc.toMap("productGrpId", productGrpId));
                if (UtilValidate.isNotEmpty(productStoreId)) {
                    // 删除与店铺的关联关系
                    if (UtilValidate.isNotEmpty(productStoreProductGrpApplList)) {
                        delegator.removeAll(productStoreProductGrpApplList);
                    }
                    GenericValue productStoreProductGrpAppl = null;
                    Map<String, Object> productStoreProductGrpApplMap = FastMap.newInstance();
                    // 商品店铺编码
                    productStoreProductGrpApplMap.put("productStoreId", productStoreId);
                    // 组合商品编码
                    productStoreProductGrpApplMap.put("productGrpId", productGrpId);
                    // 促销开始时间
                    productStoreProductGrpApplMap.put("fromDate", fromDate);
                    // 促销结束时间
                    productStoreProductGrpApplMap.put("thruDate", thruDate);
                    productStoreProductGrpAppl = delegator.makeValue("ProductStoreProductGrpAppl", productStoreProductGrpApplMap);
                    // 创建表
                    productStoreProductGrpAppl.create();
                } else {
                    // 删除与店铺的关联关系
                    if (UtilValidate.isNotEmpty(productStoreProductGrpApplList)) {
                        delegator.removeAll(productStoreProductGrpApplList);
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
     * 取得组合商品详情
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> productGroupingDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productGrpId = (String) context.get("productGrpId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            // 取得组合商品信息
            GenericValue productGroupPromo = delegator.findByPrimaryKey("ProductGroupPromo", UtilMisc.toMap("productGrpId", productGrpId));
            result.put("productGroupPromo", productGroupPromo);

            // 取得组合商品和店铺的关联信息
            List<GenericValue> productStoreProductGrpAppls = delegator.findByAnd("ProductStoreProductGrpAppl", UtilMisc.toMap("productGrpId", productGrpId));
            if (UtilValidate.isNotEmpty(productStoreProductGrpAppls)) {
                GenericValue productStoreProductGrpAppl = EntityUtil.getFirst(productStoreProductGrpAppls);
                result.put("productStoreProductGrpAppl", productStoreProductGrpAppl);
            }
            // 取得组合商品和商品的关联信息
            if (UtilValidate.isNotEmpty(productGroupPromo)) {
                List<GenericValue> proGrpPromoProducts = delegator.findByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", productGrpId));

                List<Map<String, Object>> productGroupList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(proGrpPromoProducts)) {
                    for (GenericValue proGrpPromoProductInfo : proGrpPromoProducts) {
                        String productId = proGrpPromoProductInfo.getString("productId");
                        String quantity = proGrpPromoProductInfo.getLong("quantity").toString();
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                        Map<String, Object> map = FastMap.newInstance();
                        map.put("productId", productId);// 商品编码
                        map.put("productName", product.get("productName"));// 商品名称
                        map.put("quantity", quantity);// 数量
                        GenericValue defaultprice = EntityUtil.getFirst(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE")));
                        if (UtilValidate.isNotEmpty(defaultprice)) {
                            map.put("defaultprice", defaultprice.get("price")); // 销售价格
                        } else {
                            map.put("defaultprice", 0);
                        }
                        // 商品规格的取得
                        String featureInfo = "";// 商品规格
                        if (UtilValidate.isNotEmpty(productId)) {
                            if (UtilValidate.isNotEmpty(product)) {
                                if (UtilValidate.isNotEmpty(product.getString("mainProductId"))) {
                                    if (UtilValidate.isNotEmpty(product.getString("featureaProductId"))) {
                                        GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", product.getString("featureaProductId")));
                                        if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                            GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                                            if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                                String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                                String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                                featureInfo = productFeatureTypeName + ":" + productFeatureName;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        map.put("featureInfo", featureInfo);// 特征

                        // 取得商品图片
                        String imgUrl = "";// 商品图片
                        // 根据商品ID获取商品图片url
                        String productAdditionalImage1 = "";
                        List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                                UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                        if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                            imgUrl = "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId");
                        }
                        map.put("imgUrl", imgUrl);
                        productGroupList.add(map);
                    }
                }
                result.put("productGroupList", productGroupList);
                result.put("proGrpPromoProducts", proGrpPromoProducts);
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 根据编码删除商品组合
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> deleteProductGroupingByIds(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productGrpIds = (String) context.get("productGrpIds");
        Delegator delegator = dcx.getDelegator();
        try {
            if (UtilValidate.isNotEmpty(productGrpIds)) {
                String[] arrProductGrpIds = productGrpIds.split(",");
                if (UtilValidate.isNotEmpty(arrProductGrpIds)) {
                    for (int i = 0; i < arrProductGrpIds.length; i++) {
                        String curProductGrpId = arrProductGrpIds[i];
                        if (UtilValidate.isNotEmpty(curProductGrpId)) {
                            //删除
                            delegator.removeByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", curProductGrpId));
                            delegator.removeByAnd("ProductStoreProductGrpAppl", UtilMisc.toMap("productGrpId", curProductGrpId));
                            delegator.removeByAnd("ProductGroupPromo", UtilMisc.toMap("productGrpId", curProductGrpId));
                            delegator.removeByAnd("ProductGrpPromoAudit", UtilMisc.toMap("productGrpId", curProductGrpId));
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 组合商品审批
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> auditProductGrouping(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productGrpId = (String) context.get("productGrpId");
        String message = (String) context.get("auditMessage");
        String status = (String) context.get("promoStatus");
        Delegator delegator = dcx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String userLoginId = userLogin.getString("userLoginId");


        try {
            GenericValue auditLog = delegator.makeValue("PromoAuditLog");
            auditLog.setNextSeqId();
            String businessId = "grp_" + productGrpId;
            auditLog.put("businessId", businessId);
            auditLog.put("auditType", "ACTY_AUDIT_PASS".equals(status) ? "通过" : "驳回");
            auditLog.put("auditPerson", userLoginId);
            auditLog.put("auditMessage", message);
            auditLog.put("createDate", new Timestamp(System.currentTimeMillis()));
            auditLog.create();

            GenericValue audit = delegator.makeValue("ProductGrpPromoAudit");
            String promoAuditId = delegator.getNextSeqId("ProductGrpPromoAudit");
            audit.set("promoAuditId", promoAuditId);
            audit.set("productGrpId", productGrpId);
            audit.set("auditMessage", message);
            audit.set("result", status);
            delegator.create(audit);
            GenericValue productGroupPromo = delegator.findByPrimaryKey("ProductGroupPromo", UtilMisc.toMap("productGrpId", productGrpId));
            productGroupPromo.set("promoStatus", status);
            delegator.store(productGroupPromo);
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 查看商品组合驳回原因
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> findProductGroupingAuditMessage(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productGrpId = (String) context.get("productGrpId");
        Delegator delegator = dcx.getDelegator();
        List<GenericValue> productGrpPromoAuditList = FastList.newInstance();
        try {
            productGrpPromoAuditList = delegator.findByAnd("ProductGrpPromoAudit", UtilMisc.toMap("productGrpId", productGrpId), UtilMisc.toList("createdStamp"));
            GenericValue productGrpPromoAudit = EntityUtil.getFirst(productGrpPromoAuditList);
            result.put("productGrpPromoAudit", productGrpPromoAudit);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 设置组合商品的结束时间
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> editEndDatePromoProductGrouping(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Timestamp endDate = (Timestamp) context.get("endDate");
        String productGrpId = (String) context.get("productGrpId");
        String productStoreId = (String) context.get("productStoreId");
        Delegator delegator = dcx.getDelegator();
        try {
            // 取得组合商品信息
            GenericValue productGroupPromo = delegator.findByPrimaryKey("ProductGroupPromo", UtilMisc.toMap("productGrpId", productGrpId));
            // 取得组合商品关联店铺信息
            List<GenericValue> productStoreProductGrpAppls = delegator.findByAnd("ProductStoreProductGrpAppl", UtilMisc.toMap("productGrpId", productGrpId));
            String chkFlg = "Y";
            String existProductStr = "";
            Timestamp thruDate = null;
            Timestamp fromDate = null;
            if (UtilValidate.isNotEmpty(productGroupPromo)) {
                thruDate = productGroupPromo.getTimestamp("thruDate");
                fromDate = productGroupPromo.getTimestamp("fromDate");
            }

            if (UtilValidate.isNotEmpty(endDate) && UtilValidate.isNotEmpty(thruDate)) {
                if (endDate.getTime() < thruDate.getTime()) {
                    //说明时间未过期可以直接修改
                    if (UtilValidate.isNotEmpty(productGroupPromo)) {
                        productGroupPromo.set("thruDate", endDate);
                        productGroupPromo.store();
                    }

                    if (UtilValidate.isNotEmpty(productStoreProductGrpAppls)) {
                        for (int i = 0; i < productStoreProductGrpAppls.size(); i++) {
                            GenericValue productStoreProductGrpAppl = productStoreProductGrpAppls.get(i);
                            productStoreProductGrpAppl.set("thruDate", endDate);
                            productStoreProductGrpAppl.store();
                        }
                    }
                } else {
                    //判断有没有商品在活动中
                    List<GenericValue> products = delegator.findByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", productGrpId));
                    List<String> productIds = FastList.newInstance();
                    if (products != null && products.size() > 0) {
                        for (GenericValue product : products) {
                            if (UtilValidate.isNotEmpty(product.get("productId"))) {
                                String productId = product.getString("productId");
                                productIds.add(productId);
                            }
                        }
                    }
                    List<String> unUserdProductIdInfos = ProductServices.getUnUsedProductIds(delegator, productStoreId, fromDate, endDate);

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

//                    existProductStr = Joiner.on(",").join(productIdExists);
                    result.put("chkFlg", chkFlg);
                    if ("N".equalsIgnoreCase(chkFlg)) {
                        result.put("errorMsg", "当前时间已存在指定商品的促销!");
                    }
//                    result.put("errorMsg", "商品id为" + existProductStr + "的商品已经参加过其他促销活动正在活动中！");

                    if ("Y".equalsIgnoreCase(chkFlg)) {
                        boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreId, fromDate, endDate);
                        if (isPromoAllExist) {
                            chkFlg = "N";
                            result.put("chkFlg", chkFlg);
                            result.put("errorMsg", "当前时间已存在全场促销");
                        }
                    }

                    if (chkFlg == "Y") {
                        //说明时间未过期可以直接修改
                        if (UtilValidate.isNotEmpty(productGroupPromo)) {
                            productGroupPromo.set("thruDate", endDate);
                            productGroupPromo.store();
                        }

                        if (UtilValidate.isNotEmpty(productStoreProductGrpAppls)) {
                            for (int i = 0; i < productStoreProductGrpAppls.size(); i++) {
                                GenericValue productStoreProductGrpAppl = productStoreProductGrpAppls.get(i);
                                productStoreProductGrpAppl.set("thruDate", endDate);
                                productStoreProductGrpAppl.store();
                            }
                        }
                    }


                }
            }


        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 组合商品批量审批处理
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> batchAuditProductGrouping(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String obj = (String) context.get("obj");
        String status = (String) context.get("promoStatus");
        Delegator delegator = dcx.getDelegator();
        // 截取字符串
        String[] m = obj.split(",");
        String opinion = null;
        // 商品组合编码
        String productGrpId = "";
        // 审核意见
        String auditMessage = "";

        for (int i = 0; i < m.length; i++) {
            productGrpId = "";
            auditMessage = "";
            opinion = m[i];
            String[] n = opinion.split(":");
            if (n.length > 1) {
                productGrpId = n[0];
                auditMessage = n[1];
            } else {
                productGrpId = n[0];
            }
            try {
                // 创建组合商品审核表
                GenericValue audit = delegator.makeValue("ProductGrpPromoAudit");
                String promoAuditId = delegator.getNextSeqId("ProductGrpPromoAudit");
                audit.set("promoAuditId", promoAuditId);
                audit.set("productGrpId", productGrpId);
                audit.set("auditMessage", auditMessage);
                audit.set("result", status);
                delegator.create(audit);
                GenericValue productGroupPromo = delegator.findByPrimaryKey("ProductGroupPromo", UtilMisc.toMap("productGrpId", productGrpId));
                productGroupPromo.set("promoStatus", status);
                delegator.store(productGroupPromo);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return result;
    }
    // Add by zhajh at 20180313 商品组合 End


    // Add by zhajh at 20180319 秒杀 Start
    //   秒杀信息的编辑
    public Map<String, Object> updateSecKillInfo(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productSkId = (String) context.get("productSkId");
        String activityCode = (String) context.get("activityCode");
//        String activityAuditStatus = (String) context.get("activityAuditStatus");
//        String activityType = (String) context.get("activityType");
        String activityName = (String) context.get("activityName");
        Timestamp activityStartDate = (Timestamp) context.get("activityStartDate");
        Timestamp activityEndDate = (Timestamp) context.get("activityEndDate");
        Long limitQuantity = (Long) context.get("limitQuantity");
        String activityDesc = (String) context.get("activityDesc");
        String productSkInfos = (String) context.get("productSkInfos"); // 秒杀商品编码信息
        String productStoreId = (String) context.get("productStoreId"); // 商品店铺信息
        String productIds = (String) context.get("productIds"); // 商品店铺信息

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();

        try {

            // 取得秒杀商品信息
            List<Map<String, Object>> productSkInfoMapList = FastList.newInstance();
            Map<String, Object> curProductSkMap = FastMap.newInstance();
            if (UtilValidate.isNotEmpty(productSkInfos)) {
                String[] pgInfos = productSkInfos.split(",");
                if (UtilValidate.isNotEmpty(pgInfos)) {
                    for (String pgInfo : pgInfos) {
                        curProductSkMap = FastMap.newInstance();
                        String[] attrInfos = pgInfo.split("\\|");
                        String productId = attrInfos[0]; // 商品编码
                        String quantity = attrInfos[1]; // 活动数量
                        String skPrice = attrInfos[2]; // 秒杀金额

                        curProductSkMap.put("productId", productId);
                        curProductSkMap.put("quantity", quantity);
                        curProductSkMap.put("skPrice", skPrice);
                        productSkInfoMapList.add(curProductSkMap);
                    }
                }
            }

            if (UtilValidate.isEmpty(productSkId)) {
                //查看是否有其他全场促销
                boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator, productStoreId, activityStartDate, activityEndDate);
                if (isPromoAllExist) {
                    return ServiceUtil.returnFailure("当前时间已存在全场促销");
                }
                // 新增的场合
                // 创建秒杀活动信息(productActivity)
                List<GenericValue> allStore = new LinkedList<GenericValue>();

                GenericValue productActivity = null;
                String productActivityIdCreate = delegator.getNextSeqId("ProductActivity");
                productActivity = delegator.makeValue("ProductActivity", UtilMisc.toMap("activityId", productActivityIdCreate));
                // 活动编码
                productActivity.set("activityCode", "SK_" + productActivityIdCreate);
                // 促销名称
                if (UtilValidate.isNotEmpty(activityName)) {
                    productActivity.set("activityName", activityName);
                }
                // 活动类型
                productActivity.set("activityType", "SEC_KILL");
                // 活动状态
                productActivity.set("activityAuditStatus", "ACTY_AUDIT_INIT");
                // 开始时间
                if (UtilValidate.isNotEmpty(activityStartDate)) {
                    productActivity.set("activityStartDate", activityStartDate);
                }
                // 结束时间
                if (UtilValidate.isNotEmpty(activityEndDate)) {
                    productActivity.set("activityEndDate", activityEndDate);
                }
                // 单个ID限购数量
                if (UtilValidate.isNotEmpty(limitQuantity)) {
                    productActivity.set("limitQuantity", limitQuantity);
                }
                // 活动描述
                if (UtilValidate.isNotEmpty(activityDesc)) {
                    productActivity.set("activityDesc", activityDesc);
                }
                // 创建表
                productActivity.create();

                int curSeqId = 1;
                if (UtilValidate.isNotEmpty(productSkInfoMapList)) {
                    for (Map<String, Object> curSkInfo : productSkInfoMapList) {
                        if (UtilValidate.isNotEmpty(curSkInfo)) {
                            String activityId = productActivityIdCreate;
                            String quantity = "0";
                            if (UtilValidate.isNotEmpty(curSkInfo.get("quantity"))) {
                                quantity = (String) curSkInfo.get("quantity");
                            }
                            String skPrice = "0";
                            if (UtilValidate.isNotEmpty(curSkInfo.get("skPrice"))) {
                                skPrice = (String) curSkInfo.get("skPrice");
                            }
                            String productId = (String) curSkInfo.get("productId");
                            // 建立秒杀活动和商品之间的关系（productActivityGoods）
                            GenericValue productActivityGoods = null;
                            Map<String, Object> productActivityGoodsMap = FastMap.newInstance();
                            // 活动ID
                            productActivityGoodsMap.put("activityId", activityId);
                            // 商品ID
                            productActivityGoodsMap.put("productId", productId);
                            // 活动数量
                            productActivityGoodsMap.put("activityQuantity", Long.parseLong(quantity));
                            // 活动价格
                            productActivityGoodsMap.put("activityPrice", new BigDecimal(skPrice));
                            productActivityGoods = delegator.makeValue("ProductActivityGoods", productActivityGoodsMap);
                            // 创建表
                            productActivityGoods.create();


                            //创建团购促销
                            String promoName = activityName + ":促销";
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

                            Map<String, Object> callback = dispatcher.runSync("createProductPromo", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }
                            String productPromoId = (String) callback.get("productPromoId");


                            //创建 createProductPromoRule
                            String ruleName = activityName + ":促销条件";
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
                            inputParams.put("inputParamEnumId", "SALE_TIME_BTW");
                            inputParams.put("operatorEnumId", "PPC_BTW");
                            inputParams.put("condValue", activityStartDate.toString());
                            inputParams.put("otherValue", activityEndDate.toString());
                            inputParams.put("userLogin", userLogin);
                            callback = dispatcher.runSync("createProductPromoCond", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }
                            String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");


                            //创建条件产品
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
                            inputParams.put("quantity", new BigDecimal(quantity));
                            inputParams.put("amount", new BigDecimal(skPrice));
                            inputParams.put("useCartQuantity", "N");
                            inputParams.put("userLogin", userLogin);
                            callback = dispatcher.runSync("createProductPromoAction", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }

                            //创建ProductPromoProduct
                            String productPromoActionSeqId = (String) callback.get("productPromoActionSeqId");
                            inputParams = FastMap.newInstance();
                            inputParams.put("productPromoRuleId", productPromoRuleId);
                            inputParams.put("productPromoId", productPromoId);
                            inputParams.put("productPromoCondSeqId", productPromoCondSeqId);
                            inputParams.put("productPromoActionSeqId", productPromoActionSeqId);
                            inputParams.put("productId", productId);
                            inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                            inputParams.put("userLogin", userLogin);
                            callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }

                            //创建产品团购规则对应每个促销
                            GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                            productGroupOrderRule.set("activityId", activityId);
                            productGroupOrderRule.set("seqId", curSeqId + "");
                            productGroupOrderRule.set("orderQuantity", Long.parseLong(quantity));
                            productGroupOrderRule.set("orderPrice", new BigDecimal(skPrice));
                            productGroupOrderRule.set("productPromoId", productPromoId);
                            // 创建表
                            productGroupOrderRule.create();
                            curSeqId++;

                        }
                    }
                    // 建立商品关联店铺
                    if (UtilValidate.isNotEmpty(productStoreId)) {
                        GenericValue productStoreProductActAppl = null;
                        Map<String, Object> productStoreProductActApplMap = FastMap.newInstance();
                        // 商品店铺编码
                        productStoreProductActApplMap.put("productStoreId", productStoreId);
                        // 组合商品编码
                        productStoreProductActApplMap.put("activityId", productActivityIdCreate);
                        // 促销开始时间
                        productStoreProductActApplMap.put("fromDate", activityStartDate);
                        // 促销结束时间
                        productStoreProductActApplMap.put("thruDate", activityEndDate);
                        productStoreProductActAppl = delegator.makeValue("ProductStoreProductActAppl", productStoreProductActApplMap);
                        // 创建表
                        productStoreProductActAppl.create();
                    }
                }
                result.put("activityId", productActivityIdCreate);
            } else {
                // 更新的场合
                // 根据秒杀活动编码取得秒杀信息
                GenericValue productActivityForUpdate = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", productSkId));
                List<String> productIdList=StringUtil.split(productIds, ",");// 商品编码列表




                // 验证状态
                String chkFlg="Y";
                if(UtilValidate.isNotEmpty(productActivityForUpdate)){
                    if("ACTY_AUDIT_PASS".equalsIgnoreCase(productActivityForUpdate.getString("activityAuditStatus"))){
                        //此刻是已完成状态，需要判断新增商品互斥
                        List<GenericValue> products = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", productSkId));
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
                        for(String newProductId:productIdList){
                            if(!oldProductIds.contains(newProductId)){
                                addProductIds.add(newProductId);
                            }
                        }
                        if(UtilValidate.isNotEmpty(addProductIds)){
                            //check新增的商品存不存在其他促销活动中
                            List<String> existProductIds = checkProductPromoExist(delegator,productStoreId,addProductIds,activityStartDate,activityEndDate);
                            if(UtilValidate.isNotEmpty(existProductIds)){
                                chkFlg="N";
                                result.put("chkFlg", chkFlg);
                                result.put("errorMsg","商品id为：" +Joiner.on(",").join(existProductIds)+"的商品该时间段内已经在其他活动中！");
                                return result;
                            }
                        }
                    }
                }

                List<GenericValue> allStore = new LinkedList<GenericValue>();
                if (UtilValidate.isNotEmpty(productActivityForUpdate)) {
                    // 促销名称
                    if (UtilValidate.isNotEmpty(activityName)) {
                        productActivityForUpdate.set("activityName", activityName);
                    }
                    // 状态
                    if(!"ACTY_AUDIT_PASS".equalsIgnoreCase(productActivityForUpdate.getString("activityAuditStatus"))) {
                        productActivityForUpdate.set("activityAuditStatus", "ACTY_AUDIT_INIT");
                    }
                    // 开始时间
                    if (UtilValidate.isNotEmpty(activityStartDate)) {
                        productActivityForUpdate.set("activityStartDate", activityStartDate);
                    }
                    // 结束时间
                    if (UtilValidate.isNotEmpty(activityEndDate)) {
                        productActivityForUpdate.set("activityEndDate", activityEndDate);
                    }
                    // 单个ID限购数量
                    if (UtilValidate.isNotEmpty(limitQuantity)) {
                        productActivityForUpdate.set("limitQuantity", limitQuantity);
                    }
                    // 活动描述
                    if (UtilValidate.isNotEmpty(activityDesc)) {
                        productActivityForUpdate.set("activityDesc", activityDesc);
                    }
                    // 更新表
                    allStore.add(productActivityForUpdate);
//                    productActivityForUpdate.store();
                }
                result.put("activityId", productSkId);

                //删除存在的促销ProductPromo
                //删除存在的促销ProductPromoRule
                //删除存在的促销ProductPromoRule
                //删除存在的促销ProductPromoCond
                //删除存在的促销ProductPromoAction
                //删除存在的促销ProductStorePromoAppl
                //删除存在的促销ProductGroupOrderRule

                List<GenericValue> o_productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule", UtilMisc.toMap("activityId", productSkId));
                if (UtilValidate.isNotEmpty(o_productGroupOrderRules)) {
                    for (GenericValue productOrderRlue : o_productGroupOrderRules) {
                        String o_promoId = (String) productOrderRlue.get("productPromoId");

                        delegator.removeByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", o_promoId));
                        delegator.removeByAnd("ProductPromoCond", UtilMisc.toMap("productPromoId", o_promoId));
                        delegator.removeByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", o_promoId));
                        delegator.removeByAnd("ProductPromoRule", UtilMisc.toMap("productPromoId", o_promoId));
                        List<GenericValue> appls = delegator.findByAnd("ProductStorePromoAppl", UtilMisc.toMap("productPromoId", o_promoId));
                        if (UtilValidate.isNotEmpty(appls)) {
                            for (int i = 0; i < appls.size(); i++) {
                                GenericValue genericValue = appls.get(i);
                                delegator.removeValue(genericValue);
                            }
                        }
                        delegator.removeByAnd("ProductGroupOrderRule", UtilMisc.toMap("productPromoId", o_promoId));
                        delegator.removeByAnd("ProductPromo", UtilMisc.toMap("productPromoId", o_promoId));
                    }
                }

                int curSeqId = 1;
                if (UtilValidate.isNotEmpty(productSkInfoMapList)) {
                    for (Map<String, Object> curSkInfo : productSkInfoMapList) {
                        if (UtilValidate.isNotEmpty(curSkInfo)) {
                            String activityId = productSkId;
                            String quantity = "0";
                            if (UtilValidate.isNotEmpty(curSkInfo.get("quantity"))) {
                                quantity = (String) curSkInfo.get("quantity");
                            }
                            String skPrice = "0";
                            if (UtilValidate.isNotEmpty(curSkInfo.get("skPrice"))) {
                                skPrice = (String) curSkInfo.get("skPrice");
                            }
                            String productId = (String) curSkInfo.get("productId");

                            // 删除关系
                            delegator.removeByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", productSkId));
                            // 建立秒杀活动和商品之间的关系（productActivityGoods）
                            GenericValue productActivityGoods = null;
                            Map<String, Object> productActivityGoodsMap = FastMap.newInstance();
                            // 活动ID
                            productActivityGoodsMap.put("activityId", activityId);
                            // 商品ID
                            productActivityGoodsMap.put("productId", productId);
                            // 活动数量
                            productActivityGoodsMap.put("activityQuantity", Long.parseLong(quantity));
                            // 活动价格
                            productActivityGoodsMap.put("activityPrice", new BigDecimal(skPrice));
                            productActivityGoods = delegator.makeValue("ProductActivityGoods", productActivityGoodsMap);
                            // 创建表
//                            productActivityGoods.create();
                            allStore.add(productActivityGoods);

                            //创建团购促销
                            String promoName = activityName + ":促销";
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

                            Map<String, Object> callback = dispatcher.runSync("createProductPromo", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }
                            String productPromoId = (String) callback.get("productPromoId");


                            //创建 createProductPromoRule
                            String ruleName = activityName + ":促销条件";
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
                            inputParams.put("inputParamEnumId", "SALE_TIME_BTW");
                            inputParams.put("operatorEnumId", "PPC_BTW");
                            inputParams.put("condValue", activityStartDate.toString());
                            inputParams.put("otherValue", activityEndDate.toString());
                            inputParams.put("userLogin", userLogin);
                            callback = dispatcher.runSync("createProductPromoCond", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }
                            String productPromoCondSeqId = (String) callback.get("productPromoCondSeqId");


                            //创建条件产品
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
                            inputParams.put("quantity", new BigDecimal(quantity));
                            inputParams.put("amount", new BigDecimal(skPrice));
                            inputParams.put("useCartQuantity", "N");
                            inputParams.put("userLogin", userLogin);
                            callback = dispatcher.runSync("createProductPromoAction", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }

                            //创建ProductPromoProduct
                            String productPromoActionSeqId = (String) callback.get("productPromoActionSeqId");
                            inputParams = FastMap.newInstance();
                            inputParams.put("productPromoRuleId", productPromoRuleId);
                            inputParams.put("productPromoId", productPromoId);
                            inputParams.put("productPromoCondSeqId", productPromoCondSeqId);
                            inputParams.put("productPromoActionSeqId", productPromoActionSeqId);
                            inputParams.put("productId", productId);
                            inputParams.put("productPromoApplEnumId", "PPPA_INCLUDE");
                            inputParams.put("userLogin", userLogin);
                            callback = dispatcher.runSync("createProductPromoProduct", inputParams);
                            if (ServiceUtil.isError(callback)) {
                                return result;
                            }

                            //创建产品团购规则对应每个促销
                            GenericValue productGroupOrderRule = delegator.makeValue("ProductGroupOrderRule");
                            productGroupOrderRule.set("activityId", activityId);
                            productGroupOrderRule.set("seqId", curSeqId + "");
                            productGroupOrderRule.set("orderQuantity", Long.parseLong(quantity));
                            productGroupOrderRule.set("orderPrice", new BigDecimal(skPrice));
                            productGroupOrderRule.set("productPromoId", productPromoId);
                            // 创建表
//                            productGroupOrderRule.create();
                            allStore.add(productGroupOrderRule);
                            curSeqId++;

                        }
                    }
                    delegator.storeAll(allStore);
                    // 建立秒杀关联店铺
                    List<GenericValue> productStoreProductActApplList = delegator.findByAnd("ProductStoreProductActAppl", UtilMisc.toMap("activityId", productSkId));
                    if (UtilValidate.isNotEmpty(productStoreId)) {
                        // 删除与店铺的关联关系
                        if (UtilValidate.isNotEmpty(productStoreProductActApplList)) {
                            delegator.removeAll(productStoreProductActApplList);
                        }
                        GenericValue productStoreProductActAppl = null;
                        Map<String, Object> productStoreProductActApplMap = FastMap.newInstance();
                        // 商品店铺编码
                        productStoreProductActApplMap.put("productStoreId", productStoreId);
                        // 秒杀商品编码
                        productStoreProductActApplMap.put("activityId", productSkId);
                        // 活动开始时间
                        productStoreProductActApplMap.put("fromDate", activityStartDate);
                        // 活动结束时间
                        productStoreProductActApplMap.put("thruDate", activityEndDate);
                        productStoreProductActAppl = delegator.makeValue("ProductStoreProductActAppl", productStoreProductActApplMap);
                        // 创建表
                        productStoreProductActAppl.create();
                    }
                }
            }


        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 取得秒杀活动详情
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> productSkDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productSkId = (String) context.get("productSkId");
        Delegator delegator = dispatchContext.getDelegator();
        List<Map<String, Object>> productSkProductInfoList = FastList.newInstance();
        try {
            // 取得秒杀商品信息
            GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", productSkId));
            result.put("productActivity", productActivity);


            // 秒杀商品和商品的关联信息
            if (UtilValidate.isNotEmpty(productActivity)) {
                List<GenericValue> productActivityGoods = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", productSkId));
                Map<String, Object> map = FastMap.newInstance();
                if (UtilValidate.isNotEmpty(productActivityGoods)) {
                    for (GenericValue productActivityGoodsInfo : productActivityGoods) {
                        map = FastMap.newInstance();
                        String productId = productActivityGoodsInfo.getString("productId");
                        Long activityQuantity = Long.parseLong("0");
                        BigDecimal activityPrice = new BigDecimal("0");

                        if (UtilValidate.isNotEmpty(productActivityGoodsInfo.getLong("activityQuantity"))) {
                            activityQuantity = productActivityGoodsInfo.getLong("activityQuantity");
                        }
                        if (UtilValidate.isNotEmpty(productActivityGoodsInfo.getBigDecimal("activityPrice"))) {
                            activityPrice = productActivityGoodsInfo.getBigDecimal("activityPrice");
                        }

                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                        map.put("activityQuantity", activityQuantity.toString());// 活动数量
                        map.put("activityPrice", activityPrice.toString());// 活动价格
                        map.put("productInfo", product);// 商品信息


                        // 商品规格的取得
                        String featureInfo = "";// 商品规格
                        if (UtilValidate.isNotEmpty(productId)) {
                            if (UtilValidate.isNotEmpty(product)) {
                                if (UtilValidate.isNotEmpty(product.getString("mainProductId"))) {
                                    if (UtilValidate.isNotEmpty(product.getString("featureaProductId"))) {
                                        GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", product.getString("featureaProductId")));
                                        if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                            GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                                            if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                                String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                                String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                                featureInfo = productFeatureTypeName + ":" + productFeatureName;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        map.put("featureInfo", featureInfo);// 特征
                        // 取得商品图片
                        String imgUrl = "";// 商品图片
                        // 根据商品ID获取商品图片url
                        String productAdditionalImage1 = "";
                        List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                                UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                        if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                            imgUrl = "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId");
                        }
                        map.put("imgUrl", imgUrl);

                        productSkProductInfoList.add(map);
                    }
                }
                result.put("productSkProductInfoList", productSkProductInfoList);
                result.put("productActivity", productActivity);
            }
        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 查找秒杀活动
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findSkActivities(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        String activityCode = (String) context.get("activityCode");
        String activityName = (String) context.get("activityName");
        String activityStartDate = (String) context.get("activityStartDate");
        String activityEndDate = (String) context.get("activityEndDate");
        String activityType = (String) context.get("activityType");
        String activityAuditStatus = (String) context.get("activityAuditStatus");
        String productStoreId = (String) context.get("productStoreId"); // 店铺编码
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


        List<GenericValue> activityList = FastList.newInstance();
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
        fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("createdStamp");

//        // Add by zhajh at 20160317 更新价格标记 Begin
//        fieldsToSelect.add("isUpdatePrice");
//        // Add by zhajh at 20160317 更新价格标记 End

        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("createdStamp" + " " + "DESC");
        }
        // blank param list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        String paramList = "";
        if (UtilValidate.isNotEmpty(activityCode)) {
            paramList = paramList + "&activityCode=" + activityCode;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityCode"), EntityOperator.LIKE, EntityFunction.UPPER("%" + activityCode + "%")));
        }
        if (UtilValidate.isNotEmpty(activityType)) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityType"), EntityOperator.EQUALS, activityType));
        }
        if (UtilValidate.isNotEmpty(activityName)) {
            paramList = paramList + "&activityName=" + activityName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + activityName + "%")));
        }
        if (UtilValidate.isNotEmpty(activityAuditStatus)) {
            paramList = paramList + "&activityAuditStatus=" + activityAuditStatus;
            Timestamp noTime = UtilDateTime.nowTimestamp();
            if ("ACTY_AUDIT_PUBING".equals(activityAuditStatus)) {
                //待发布（auditStatus为审批通过并且系统当前时间小于发布时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("publishDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_UNBEGIN".equals(activityAuditStatus)) {
                //未开始（auditStatus为审批通过并且系统当前时间小于销售开始时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("publishDate"), EntityOperator.LESS_THAN, noTime));
            } else if ("ACTY_AUDIT_DOING".equals(activityAuditStatus)) {
                //进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.LESS_THAN, noTime));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_END".equals(activityAuditStatus)) {
                //已结束（auditStatus为审批通过并且系统当前时间大于等于销售结束时间小于下架时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.LESS_THAN, noTime));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_OFF".equals(activityAuditStatus)) {
                //已下架（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.LESS_THAN, noTime));
            } else {
                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, activityAuditStatus));
            }
        }
        if (UtilValidate.isNotEmpty(activityStartDate)) {
            paramList = paramList + "&activityStartDate=" + activityStartDate;
            Object startDate = null;
            try {
                startDate = ObjectType.simpleTypeConvert(activityStartDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.GREATER_THAN_EQUAL_TO, startDate));
        }
        if (UtilValidate.isNotEmpty(activityEndDate)) {
            paramList = paramList + "&activityEndDate=" + activityEndDate;
            Object endDate = null;
            try {
                endDate = ObjectType.simpleTypeConvert(activityEndDate, "Timestamp", null, (TimeZone) context.get("timeZone"), (Locale) context.get("locale"), true);
            } catch (GeneralException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.LESS_THAN_EQUAL_TO, endDate));
        }
        //关联店铺
        if (UtilValidate.isNotEmpty(productStoreId)) {
            andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
        }

        List<GenericValue> activities = null;
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
//                EntityListIterator pli = delegator.find("ProductActivity", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
                // get the partial list for this page
                activityList = pli.getPartialList(lowIndex, viewSize);

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

    // Add by zhajh at 20180319 秒杀 End


    // Add by zhajh at 20180402 取得代金券的信息(弹框用) Begin

    /**
     * 查询促销优惠券列表，弹出框使用
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getPromoCouponListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> recordsList = FastList.newInstance();

        //总记录数
        int recordsListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PC", "ProductPromoCoupon");
        dynamicView.addAlias("PC", "couponCode");
        dynamicView.addAlias("PC", "couponName");
        dynamicView.addAlias("PC", "payReduce");
        dynamicView.addAlias("PC", "payFill");
        dynamicView.addAlias("PC", "publishType");

        dynamicView.addAlias("PC", "couponQuantity");
        dynamicView.addAlias("PC", "couponStatus");
        dynamicView.addAlias("PC", "useBeginDate");
        dynamicView.addAlias("PC", "useEndDate");
        dynamicView.addAlias("PC", "applyScope");
        dynamicView.addAlias("PC", "validitDays");
        dynamicView.addAlias("PC", "useWithScore");

        dynamicView.addAlias("PC", "isDel");


        dynamicView.addMemberEntity("PSC", "ProductStoreCouponAppl");
        dynamicView.addAlias("PSC", "couponCode");
        dynamicView.addAlias("PSC", "productStoreId");
        dynamicView.addViewLink("PC", "PSC", Boolean.TRUE, ModelKeyMap.makeKeyMapList("couponCode", "couponCode"));


        fieldsToSelect.add("couponCode");
        fieldsToSelect.add("couponName");
        fieldsToSelect.add("payReduce");
        fieldsToSelect.add("payFill");
        fieldsToSelect.add("couponQuantity");
        fieldsToSelect.add("couponStatus");
        fieldsToSelect.add("useBeginDate");
        fieldsToSelect.add("useEndDate");
        fieldsToSelect.add("applyScope");
        fieldsToSelect.add("validitDays");
        fieldsToSelect.add("useWithScore");
        fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("isDel");


        //排序字段名称
        String sortField = "couponCode";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);


        //按促销代金券名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("couponName"))) {
            andExprs.add(EntityCondition.makeCondition("couponName", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("couponName") + "%")));
        }

        // 代金券金额
        String payReduceStart = (String) context.get("payReduceStart");
        String payReduceEnd = (String) context.get("payReduceEnd");
        if (!UtilValidate.isEmpty(payReduceStart)) {
            andExprs.add(EntityCondition.makeCondition("payReduce", EntityOperator.GREATER_THAN_EQUAL_TO,
                    new BigDecimal(payReduceStart)));
        }
        if (!UtilValidate.isEmpty(payReduceEnd)) {
            andExprs.add(EntityCondition.makeCondition("payReduce", EntityOperator.LESS_THAN_EQUAL_TO,
                    new BigDecimal(payReduceEnd)));
        }
        String isRedPackage = (String) context.get("isRedPackage");
        if (UtilValidate.isNotEmpty(isRedPackage)) {
            if ("Y".equals(isRedPackage)) {
                andExprs.add(EntityCondition.makeCondition("publishType", EntityOperator.EQUALS, "COUPON_ACT_DIR"));
            }
        }

        //默认条件
        //进行中（auditStatus为审批通过并且系统当前时间大于等于发放开始时间小于发放结束时间）
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("couponStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        String productStoreId = (String) context.get("productStoreId"); // 店铺编码
        if (UtilValidate.isNotEmpty(productStoreId)) {
            andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
        }
        andExprs.add(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("useBeginDate"), EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp()));
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("useEndDate"), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));

        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            // 获取分页所需的记录集合
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String couponCode = gv.getString("couponCode");
                String couponName = gv.getString("couponName");
                String payReduce = UtilMisc.doubleTrans(gv.getBigDecimal("payReduce"));
                String payFill = gv.getString("payFill");
                String applyScope = gv.getString("applyScope");
                String couponQuantity = gv.getString("couponQuantity");
                String validitDays = gv.getString("validitDays");

                String useEndDate = "";
                if (UtilValidate.isNotEmpty(gv.getTimestamp("useEndDate"))) {
                    useEndDate = sdf.format(gv.getTimestamp("useEndDate"));
                }
                String useBeginDate = "";
                if (UtilValidate.isNotEmpty(gv.getTimestamp("useBeginDate"))) {
                    useBeginDate = sdf.format(gv.getTimestamp("useBeginDate"));
                }
                String useWithScore = gv.getString("useWithScore");


                map.put("couponCode", couponCode);
                map.put("couponName", couponName);
                map.put("payReduce", payReduce);
                map.put("couponQuantity", couponQuantity);
                map.put("validitDays", validitDays);
                map.put("useBeginDate", useBeginDate);
                map.put("useEndDate", useEndDate);
                map.put("useWithScore", useWithScore);


                String strPayFill = "";
                if (UtilValidate.isNotEmpty(payFill)) {
                    strPayFill = "满" + payFill + "元使用";
                }
                map.put("payFill", strPayFill);
                String strApplyScope = "";
                if (UtilValidate.isNotEmpty(applyScope)) {
                    if ("S".equals(applyScope)) {
                        strApplyScope = "自营";
                    } else if ("A".equals(applyScope)) {
                        strApplyScope = "全渠道";
                    }
                }
                map.put("applyScope", strApplyScope);

                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }


        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    // Add by zhajh at 20180402 取得代金券的信息(弹框用) End


    /**
     * 根据代金券编码取得代金券信息
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> getPromoCouponListByIds(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String ids = (String) context.get("ids");
        Delegator delegator = dispatchContext.getDelegator();
        List<Map<String, Object>> promoCouponInfoList = FastList.newInstance();
        List<String> promoCouponIdList = FastList.newInstance();
        Map<String, Object> map = FastMap.newInstance();
        try {

            // 取得代金券Id信息列表
            if (UtilValidate.isNotEmpty(ids)) {
                String[] tPromoCouponArray = ids.split(",");
                for (String promoCouponId : tPromoCouponArray) {
                    promoCouponIdList.add(promoCouponId);
                }
            }
            // 取得代金券信息
            if (promoCouponIdList.size() > 0) {

                for (String promoCouponId : promoCouponIdList) {
                    GenericValue productPromoCouponInfo = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", promoCouponId));

                    map = FastMap.newInstance();
                    map.put("productPromoCouponInfo", productPromoCouponInfo);
                    if (UtilValidate.isNotEmpty(productPromoCouponInfo)) {

                        map.put("couponCode", productPromoCouponInfo.getString("couponCode"));
                        map.put("couponName", productPromoCouponInfo.getString("couponName"));
                        if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("payReduce"))) {
                            map.put("payReduce", UtilMisc.doubleTrans(productPromoCouponInfo.getBigDecimal("payReduce")));
                        }

                        String strPayFill = "";
                        if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("payFill"))) {
                            strPayFill = "满" + productPromoCouponInfo.getString("payFill") + "元使用";
                        }
                        map.put("payFill", strPayFill);
                        String strValiditDays = "";
                        if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("validitDays"))) {
                            strValiditDays = productPromoCouponInfo.getString("validitDays") + "天";
                        }
                        map.put("validitDays", strValiditDays);

                        String strApplyScope = "";
                        if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("applyScope"))) {
                            if ("S".equals(productPromoCouponInfo.getString("applyScope"))) {
                                strApplyScope = "自营";
                            } else if ("A".equals(productPromoCouponInfo.getString("applyScope"))) {
                                strApplyScope = "全渠道";
                            }
                        }
                        map.put("applyScope", strApplyScope);
                        String strCouponQuantity = "0";
                        Long iCouponQuantity = Long.valueOf(0);
                        if (UtilValidate.isNotEmpty(productPromoCouponInfo.get("couponQuantity"))) {
                            iCouponQuantity = productPromoCouponInfo.getLong("couponQuantity");
                        }
                        Long iUserCount = Long.valueOf(0);
                        if (UtilValidate.isNotEmpty(productPromoCouponInfo.get("userCount"))) {
                            iUserCount = productPromoCouponInfo.getLong("userCount");
                        }
                        Long accessCount = iCouponQuantity - iUserCount;

                        if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("couponQuantity"))) {
                            strCouponQuantity = productPromoCouponInfo.getString("couponQuantity");
                        }
                        map.put("couponQuantity", strCouponQuantity);
                        map.put("accessCount", accessCount.toString());

                        promoCouponInfoList.add(map);
                    }
                }

            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        //返回的参数
        result.put("promoCouponInfoList", promoCouponInfoList);
        return result;
    }

    // Add by zhajh at 20180403 红包设置的编辑处理 Begin

    /**
     * 红包设置的编辑
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updatePromoRedPacketSettingService(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String hourRange = (String) context.get("hourRange");
        String nums = (String) context.get("nums");
        String isUsed = (String) context.get("isUsed");
        String promoCouponIds = (String) context.get("promoCouponIds"); // 代金券编码信息
        String packageId = (String) context.get("packageId"); // 红包编码
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();

        try {
            if (UtilValidate.isEmpty(packageId)) {
                // 新增的场合
                // 新增一条红包设置信息
                createPromoRedPacketInfo(delegator, hourRange, nums, isUsed, promoCouponIds);
            } else {
                // 更新的场合
                // 取得红包信息
                List<GenericValue> redPackageCouponSettingList = delegator.findByAnd("RedPackageSetting");
                if (UtilValidate.isNotEmpty(redPackageCouponSettingList)) {
                    if (redPackageCouponSettingList.size() > 0) {
                        for (GenericValue redPackageCouponSettingInfo : redPackageCouponSettingList) {
                            redPackageCouponSettingInfo.set("status", "0");
                            redPackageCouponSettingInfo.store();
                        }
                    }
                }
                // 新增一条红包设置信息
                createPromoRedPacketInfo(delegator, hourRange, nums, isUsed, promoCouponIds);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 新增红包设置信息
     */
    private static void createPromoRedPacketInfo(Delegator delegator, String hourRange, String nums, String isUsed, String promoCouponIds) {
        try {
            // 新增的场合
            //创建红包设置主表
            GenericValue redPackageSetting = null;
            String redPackageSettingIdCreate = delegator.getNextSeqId("RedPackageSetting");
            redPackageSetting = delegator.makeValue("RedPackageSetting", UtilMisc.toMap("packageId", redPackageSettingIdCreate));
            // 活动有效期
            if (UtilValidate.isNotEmpty(hourRange)) {
                redPackageSetting.set("hourRange", Long.valueOf(hourRange));
            }
            // 参与人数
            if (UtilValidate.isNotEmpty(nums)) {
                redPackageSetting.set("nums", Long.valueOf(nums));
            }
            // 是否启用
            if (UtilValidate.isNotEmpty(isUsed)) {
                redPackageSetting.set("isUsed", isUsed);
            }
            // 状态
            redPackageSetting.set("status", "1");
            // 创建表
            redPackageSetting.create();

            // 建立红包和代金券之间的关系
            if (UtilValidate.isNotEmpty(promoCouponIds)) {
                String[] pcInfos = promoCouponIds.split(",");
                if (UtilValidate.isNotEmpty(pcInfos)) {
                    for (String couponId : pcInfos) {
                        // 创建红包与代金券关系表
                        GenericValue redPackageCouponSetting = null;
                        Map<String, Object> redPackageCouponSettingMap = FastMap.newInstance();
                        // 红包ID
                        redPackageCouponSettingMap.put("packageId", redPackageSettingIdCreate);
                        // 代金券ID
                        redPackageCouponSettingMap.put("couponId", couponId);
                        redPackageCouponSetting = delegator.makeValue("RedPackageCouponSetting", redPackageCouponSettingMap);
                        // 创建表
                        redPackageCouponSetting.create();

                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得红包设置详情
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> promoRedPacketDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String packageId = (String) context.get("packageId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            // 取得红包设置信息
            List<GenericValue> redPackageSettingInfos = delegator.findByAnd("RedPackageSetting", UtilMisc.toMap("status", "1"));

            GenericValue redPackageSetting = null;
            List<Map<String, Object>> promoCouponList = FastList.newInstance();
            // 取得红包和代金券的关联信息
            if (UtilValidate.isNotEmpty(redPackageSettingInfos)) {
                redPackageSetting = EntityUtil.getFirst(redPackageSettingInfos);
                result.put("redPackageSetting", redPackageSetting);
                if (UtilValidate.isNotEmpty(redPackageSetting)) {
                    String curPackageId = redPackageSetting.getString("packageId");
                    List<GenericValue> redPackageCouponSettingInfos = delegator.findByAnd("RedPackageCouponSetting", UtilMisc.toMap("packageId", curPackageId));
                    if (UtilValidate.isNotEmpty(redPackageCouponSettingInfos)) {

                        for (GenericValue curRedPackageCouponSetting : redPackageCouponSettingInfos) {
                            String couponCode = curRedPackageCouponSetting.getString("couponId");

                            GenericValue productPromoCouponInfo = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
                            if (UtilValidate.isNotEmpty(productPromoCouponInfo)) {
                                Map<String, Object> map = FastMap.newInstance();

                                map.put("couponCode", productPromoCouponInfo.getString("couponCode"));
                                map.put("couponName", productPromoCouponInfo.getString("couponName"));
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("payReduce"))) {
                                    map.put("payReduce", UtilMisc.doubleTrans(productPromoCouponInfo.getBigDecimal("payReduce")));
                                }

                                String strPayFill = "";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("payFill"))) {
                                    strPayFill = "满" + productPromoCouponInfo.getString("payFill") + "元使用";
                                }
                                map.put("payFill", strPayFill);
                                String strValiditDays = "";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("validitDays"))) {
                                    strValiditDays = productPromoCouponInfo.getString("validitDays") + "天";
                                }
                                map.put("validitDays", strValiditDays);

                                String strApplyScope = "";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("applyScope"))) {
                                    if ("S".equals(productPromoCouponInfo.getString("applyScope"))) {
                                        strApplyScope = "自营";
                                    } else if ("A".equals(productPromoCouponInfo.getString("applyScope"))) {
                                        strApplyScope = "全渠道";
                                    }
                                }
                                map.put("applyScope", strApplyScope);
                                String strCouponQuantity = "0";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("couponQuantity"))) {
                                    strCouponQuantity = productPromoCouponInfo.getString("couponQuantity");
                                }
                                Long iCouponQuantity = Long.valueOf(0);
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.get("couponQuantity"))) {
                                    iCouponQuantity = productPromoCouponInfo.getLong("couponQuantity");
                                }
                                Long iUserCount = Long.valueOf(0);
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.get("userCount"))) {
                                    iUserCount = productPromoCouponInfo.getLong("userCount");
                                }
                                Long accessCount = iCouponQuantity - iUserCount;

                                map.put("couponQuantity", strCouponQuantity);
                                map.put("accessCount", accessCount.toString());

                                promoCouponList.add(map);
                            }
                        }
                        result.put("redPackageSetting", redPackageSetting);
                        result.put("promoCouponList", promoCouponList);
                    }
                }
            } else {
                result.put("redPackageSetting", redPackageSetting);
                result.put("promoCouponList", promoCouponList);
            }

        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    // Add by zhajh at 20180403 红包设置的编辑处理 End


    // Add by zhajh at 20180403 问题设置的编辑处理 Begin

    /**
     * 问题设置的编辑
     *
     * @param dtx
     * @param context
     * @return
     */
    public static Map<String, Object> updateQuestionSettingService(DispatchContext dtx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String scoreQuestNums = (String) context.get("scoreQuestNums");
        String scoreNums = (String) context.get("scoreNums");
        String couponQuestNums = (String) context.get("couponQuestNums");
        String isUsed = (String) context.get("isUsed");
        String promoCouponIds = (String) context.get("promoCouponIds"); // 代金券编码信息
        String questionId = (String) context.get("questionId"); // 问题编码
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dtx.getDispatcher();
        Delegator delegator = dtx.getDelegator();

        try {
            if (UtilValidate.isEmpty(questionId)) {
                // 新增的场合
                // 新增一条问题设置信息
                createQuestionSettingInfo(delegator, scoreQuestNums, scoreNums, couponQuestNums, isUsed, promoCouponIds);
            } else {
                // 更新的场合
                // 取得问题信息
                List<GenericValue> questionSettingList = delegator.findByAnd("QuestionSetting");
                if (UtilValidate.isNotEmpty(questionSettingList)) {
                    if (questionSettingList.size() > 0) {
                        for (GenericValue questionSettingInfo : questionSettingList) {
                            questionSettingInfo.set("status", "0");
                            questionSettingInfo.store();
                        }
                    }
                }
                // 新增一条问题设置信息
                createQuestionSettingInfo(delegator, scoreQuestNums, scoreNums, couponQuestNums, isUsed, promoCouponIds);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }


    /**
     * 新增问题设置信息
     */
    private static void createQuestionSettingInfo(Delegator delegator, String scoreQuestNums, String scoreNums, String couponQuestNums, String isUsed, String promoCouponIds) {
        try {
            // 新增的场合
            //创建问题设置主表
            GenericValue questionSetting = null;
            String questionSettingIdCreate = delegator.getNextSeqId("QuestionSetting");
            questionSetting = delegator.makeValue("QuestionSetting", UtilMisc.toMap("questionId", questionSettingIdCreate));
            // 积分设置：每答对几题
            if (UtilValidate.isNotEmpty(scoreQuestNums)) {
                questionSetting.set("scoreQuestNums", Long.valueOf(scoreQuestNums));
            }
            // 赠送积分数
            if (UtilValidate.isNotEmpty(scoreNums)) {
                questionSetting.set("scoreNums", Long.valueOf(scoreNums));
            }
            // 代金劵设置:每答对几题
            if (UtilValidate.isNotEmpty(couponQuestNums)) {
                questionSetting.set("couponQuestNums", Long.valueOf(couponQuestNums));
            }
            // 是否启用
            if (UtilValidate.isNotEmpty(isUsed)) {
                questionSetting.set("isUsed", isUsed);
            }
            // 状态
            questionSetting.set("status", "1");
            // 创建表
            questionSetting.create();

            // 建立问题和代金券之间的关系
            if (UtilValidate.isNotEmpty(promoCouponIds)) {
                String[] pcInfos = promoCouponIds.split(",");
                if (UtilValidate.isNotEmpty(pcInfos)) {
                    for (String couponId : pcInfos) {
                        // 创建问题与代金券关系表
                        GenericValue questionCouponSetting = null;
                        Map<String, Object> questionCouponSettingMap = FastMap.newInstance();
                        // 问题ID
                        questionCouponSettingMap.put("questionId", questionSettingIdCreate);
                        // 代金券ID
                        questionCouponSettingMap.put("couponId", couponId);
                        questionCouponSetting = delegator.makeValue("QuestionCouponSetting", questionCouponSettingMap);
                        // 创建表
                        questionCouponSetting.create();

                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得问题设置详情
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> questionSettingDetail(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String questionId = (String) context.get("questionId");
        Delegator delegator = dispatchContext.getDelegator();
        try {
            // 取得问题设置信息
            List<GenericValue> questionSettingInfos = delegator.findByAnd("QuestionSetting", UtilMisc.toMap("status", "1"));

            GenericValue questionSetting = null;
            List<Map<String, Object>> promoCouponList = FastList.newInstance();
            // 取得问题和代金券的关联信息
            if (UtilValidate.isNotEmpty(questionSettingInfos)) {
                questionSetting = EntityUtil.getFirst(questionSettingInfos);
                result.put("questionSetting", questionSetting);
                if (UtilValidate.isNotEmpty(questionSetting)) {
                    String curQuestionId = questionSetting.getString("questionId");
                    List<GenericValue> questionCouponSettingInfos = delegator.findByAnd("QuestionCouponSetting", UtilMisc.toMap("questionId", curQuestionId));
                    if (UtilValidate.isNotEmpty(questionCouponSettingInfos)) {

                        for (GenericValue curQuestionCouponSetting : questionCouponSettingInfos) {
                            String couponCode = curQuestionCouponSetting.getString("couponId");

                            GenericValue productPromoCouponInfo = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", couponCode));
                            if (UtilValidate.isNotEmpty(productPromoCouponInfo)) {
                                Map<String, Object> map = FastMap.newInstance();

                                map.put("couponCode", productPromoCouponInfo.getString("couponCode"));
                                map.put("couponName", productPromoCouponInfo.getString("couponName"));
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("payReduce"))) {
                                    map.put("payReduce", UtilMisc.doubleTrans(productPromoCouponInfo.getBigDecimal("payReduce")));
                                }

                                String strPayFill = "";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("payFill"))) {
                                    strPayFill = "满" + productPromoCouponInfo.getString("payFill") + "元使用";
                                }
                                map.put("payFill", strPayFill);
                                String strValiditDays = "";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("validitDays"))) {
                                    strValiditDays = productPromoCouponInfo.getString("validitDays") + "天";
                                }
                                map.put("validitDays", strValiditDays);

                                String strApplyScope = "";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("applyScope"))) {
                                    if ("S".equals(productPromoCouponInfo.getString("applyScope"))) {
                                        strApplyScope = "自营";
                                    } else if ("A".equals(productPromoCouponInfo.getString("applyScope"))) {
                                        strApplyScope = "全渠道";
                                    }
                                }
                                map.put("applyScope", strApplyScope);
                                String strCouponQuantity = "0";
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.getString("couponQuantity"))) {
                                    strCouponQuantity = productPromoCouponInfo.getString("couponQuantity");
                                }
                                Long iCouponQuantity = Long.valueOf(0);
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.get("couponQuantity"))) {
                                    iCouponQuantity = productPromoCouponInfo.getLong("couponQuantity");
                                }
                                Long iUserCount = Long.valueOf(0);
                                if (UtilValidate.isNotEmpty(productPromoCouponInfo.get("userCount"))) {
                                    iUserCount = productPromoCouponInfo.getLong("userCount");
                                }
                                Long accessCount = iCouponQuantity - iUserCount;
                                map.put("couponQuantity", strCouponQuantity);
                                map.put("accessCount", accessCount.toString());

                                promoCouponList.add(map);
                            }
                        }
                        result.put("questionSetting", questionSetting);
                        result.put("promoCouponList", promoCouponList);
                    }
                }
            } else {
                result.put("questionSetting", questionSetting);
                result.put("promoCouponList", promoCouponList);
            }

        } catch (GenericEntityException e) {

            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    // Add by zhajh at 20180404 问题设置的编辑处理 End

    /**
     * 查询产品的促销信息，包括直降、团购、秒杀
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getProductPromoInfoByProductInfo(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        String productId = (String) context.get("productId");
        Delegator delegator = dct.getDelegator();

        try {
            GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            if (UtilValidate.isEmpty(productInfo)) {
                return ServiceUtil.returnError("产品没有店铺");
            }
            //直降
            List<GenericValue> priceLowPromos = delegator.findByAnd("ProductSpecialPricePromo", UtilMisc.toMap("promoStatus", "ACTY_AUDIT_PASS", "promoProductType", "PROMO_PRT_PART_IN", "productId", productId, "promoType", "PROMO_SPE_PRICE"));
            priceLowPromos = EntityUtil.filterByDate(priceLowPromos, UtilDateTime.nowTimestamp(), "fromDate", "thruDate", true);
            if (UtilValidate.isNotEmpty(priceLowPromos)) {
                for (int i = 0; i < priceLowPromos.size(); i++) {
                    GenericValue priceLowPromo = priceLowPromos.get(i);
                    if (priceLowPromo.get("productId").equals(productId)) {
                        result.put("priceDownInfo", priceLowPromo);
                    }
                }

            }
            //团购
            List<GenericValue> pactivityInfo = delegator.findByAnd("ProductActivityInfo", UtilMisc.toMap("activityType", "GROUP_ORDER", "activityAuditStatus", "ACTY_AUDIT_PASS", "productStoreId", productInfo.get("productStoreId"), "productId", productId));
            pactivityInfo = EntityUtil.filterByDate(pactivityInfo, UtilDateTime.nowTimestamp(), "activityStartDate", "activityEndDate", true);
            if (UtilValidate.isNotEmpty(pactivityInfo)) {
                for (int t = 0; t < pactivityInfo.size(); t++) {
                    GenericValue pactivity = pactivityInfo.get(t);
                    if (pactivity.getString("productId").equals(productId)) {

                        result.put("orderGroupInfo", pactivity);
                        Long activityQuantity = pactivity.getLong("activityQuantity");
                        result.put("orderGroupPrice", pactivity.get("activityPrice"));
                        List<GenericValue> togetherGroups = delegator.findByAnd("TogetherGroup", UtilMisc.toMap("productId", productId, "status", "TOGETHER_RUNING"), UtilMisc.toList("-createdStamp"));
                        List<Map<String, Object>> groupInfo = FastList.newInstance();
                        if (UtilValidate.isNotEmpty(togetherGroups)) {
                            int size = togetherGroups.size();
                            if (size > 2) {
                                size = 2;
                            }
                            for (int i = 0; i < size; i++) {
                                GenericValue together = togetherGroups.get(i);
                                String createUserId = together.getString("createUserId");
                                GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", createUserId));
                                Map<String, Object> pinfo = FastMap.newInstance();
                                List headImgs = FastList.newInstance();
                                if (UtilValidate.isNotEmpty(person)) {
                                    String personName = person.getString("nickname");
                                    String headUrl = person.getString("headphoto");
                                    headImgs.add(headUrl);
                                    pinfo.put("groupName", personName);
                                }
                                pinfo.put("togetherId", together.getString("togetherId"));
                                pinfo.put("createDate", together.getTimestamp("createDate"));
                                List<GenericValue> rels = together.getRelated("TogetherGroupRelOrder");
                                if (UtilValidate.isNotEmpty(rels)) {
                                    int groupSize = rels.size();
                                    if (UtilValidate.isNotEmpty(activityQuantity)) {
                                        Long lostCount = groupSize - activityQuantity;
                                        pinfo.put("lostCount", lostCount);
                                    }
                                    List<String> grpParties = FastList.newInstance();
                                    for (int j = 0; j < rels.size(); j++) {
                                        GenericValue grpRel = rels.get(j);
                                        String orderUserId = grpRel.getString("orderUserId");
                                        grpParties.add(orderUserId);
                                        GenericValue person1 = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", orderUserId));
                                        if (UtilValidate.isNotEmpty(person1)) {
                                            String personName1 = person1.getString("nickname");
                                            String headUrl1 = person1.getString("headphoto");
                                            headImgs.add(headUrl1);
                                        }

                                    }
                                    pinfo.put("users", grpParties);
                                    pinfo.put("userImgs", headImgs);
                                }
                                groupInfo.add(pinfo);
                            }
                        }
                        if (UtilValidate.isNotEmpty(groupInfo)) {
                            result.put("groupInfo", groupInfo);
                        }
                    }
                }
            }

            //秒杀
            List<GenericValue> seckillInfo = delegator.findByAnd("ProductActivityInfo", UtilMisc.toMap("activityType", "SEC_KILL", "activityAuditStatus", "ACTY_AUDIT_PASS", "productStoreId", productInfo.get("productStoreId"), "productId", productId));
            seckillInfo = EntityUtil.filterByDate(seckillInfo, UtilDateTime.nowTimestamp(), "activityStartDate", "activityEndDate", true);
            if (UtilValidate.isNotEmpty(seckillInfo)) {
                for (int i = 0; i < seckillInfo.size(); i++) {
                    GenericValue pactivity = seckillInfo.get(i);
                    if (pactivity.getString("productId").equals(productId)) {
                        result.put("secKillInfo", pactivity);
                        result.put("secKillPrice", pactivity.get("activityPrice"));
                    }
                }


            }


        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    public static Map<String, Object> getStorePromoInfoByStoreId(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productId = (String) context.get("productId");
        //店铺促销：满减、满赠、、包邮、会员首次促销、折扣
        Delegator delegator = dct.getDelegator();
        //满减
        List<GenericValue> promos = null;
        try {
            GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            if (UtilValidate.isEmpty(productInfo)) {
                return ServiceUtil.returnError("产品没有店铺");
            }
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            List<EntityCondition> andExprs = FastList.newInstance();
            andExprs.add(EntityCondition.makeCondition("promoStatus", "ACTY_AUDIT_PASS"));
            List<EntityCondition> orExprs = FastList.newInstance();
            List<EntityCondition> productExprs = FastList.newInstance();

            productExprs.add(EntityCondition.makeCondition("promoProductType", "PROMO_PRT_PART_IN"));
            productExprs.add(EntityCondition.makeCondition("productId", productId));
            orExprs.add(EntityCondition.makeCondition(productExprs, EntityOperator.AND));
            orExprs.add(EntityCondition.makeCondition("promoProductType", "PROMO_PRT_ALL"));
            andExprs.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));

            andExprs.add(EntityCondition.makeCondition("promoType", EntityOperator.IN, UtilMisc.toList("PROMO_PRE_REDUCE", "PROMO_REDUCE", "PROMO_GIFT", "PROMO_FREE_SHIPPING", "PROMO_NEW_CUST", "PROMO_DISCOUNT")));
            andExprs.add(EntityCondition.makeCondition("productStoreId", productInfo.getString("productStoreId")));
            promos = delegator.findList("ProductPromoAndProduct", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, findOpts, false);

            promos = EntityUtil.filterByDate(promos, UtilDateTime.nowTimestamp());
            if (UtilValidate.isNotEmpty(promos)) {
                result.put("promos", promos);
            }
            //商户组合
            List<GenericValue> proGrpInfos = delegator.findByAnd("ProductGroupPromoInfo", UtilMisc.toMap("productStoreId", productInfo.getString("productStoreId"), "productId", productId, "result", "ACTY_AUDIT_PASS"));
            if (UtilValidate.isNotEmpty(proGrpInfos)) {
                proGrpInfos = EntityUtil.filterByDate(proGrpInfos, UtilDateTime.nowTimestamp());
                result.put("productGrps", proGrpInfos);
            }

            //优惠劵
            //包括审批通过 ++ （本店铺内 or 全渠道使用）+ (当前产品或没有选择任何产品)
            andExprs = FastList.newInstance();
            andExprs.add(EntityCondition.makeCondition("couponStatus", "ACTY_AUDIT_PASS"));
            andExprs.add(EntityCondition.makeCondition("result", "ACTY_AUDIT_PASS"));
            //(当前产品或没有选择任何产品)

            orExprs = FastList.newInstance();
            productExprs = FastList.newInstance();
            productExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_PART_IN"));
            productExprs.add(EntityCondition.makeCondition("productId", productId));
            orExprs.add(EntityCondition.makeCondition(productExprs, EntityOperator.AND));
            orExprs.add(EntityCondition.makeCondition("couponProductType", "COUPON_PRT_ALL"));

            //（本店铺内 or 全渠道使用）
            List<EntityCondition> productStoreExp = FastList.newInstance();
            List<EntityCondition> sExp = FastList.newInstance();
            List<EntityCondition> aExp = FastList.newInstance();
            //本店铺
            sExp.add(EntityCondition.makeCondition("applyScope", "S"));
            sExp.add(EntityCondition.makeCondition("productStoreId", productInfo.getString("productStoreId")));
            //全渠道
            aExp.add(EntityCondition.makeCondition("applyScope", "A"));
            aExp.add(EntityCondition.makeCondition(sExp, EntityOperator.AND));

            andExprs.add(EntityCondition.makeCondition(aExp, EntityOperator.OR));
            andExprs.add(EntityCondition.makeCondition(orExprs, EntityOperator.OR));
            andExprs.add(EntityCondition.makeCondition("isDel", "N"));
            andExprs.add(EntityCondition.makeCondition("publishType", "COUPON_PRDE_DIR"));
            List<GenericValue> coupons = delegator.findList("ProductPromoCouponInfo", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, findOpts, false);
            if (UtilValidate.isNotEmpty(coupons)) {
                //（有效时间内 or 有效天数内）
                coupons = EntityUtil.filterByDate(coupons, UtilDateTime.nowTimestamp(), "startDate", "endDate", true);
                result.put("coupons", coupons);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;

        //优惠劵


    }


    // Add by zhajh at 20180412 取得品牌的信息(弹框用) Begin

    /**
     * 查询品牌列表，弹出框使用
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBrandListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> recordsList = FastList.newInstance();

        //总记录数
        int recordsListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addAlias("PB", "brandNameAlias");
        dynamicView.addAlias("PB", "brandDesc");
        dynamicView.addAlias("PB", "isDel");

        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("brandDesc");
        fieldsToSelect.add("isDel");


        //排序字段名称
        String sortField = "productBrandId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);


        //按品牌名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("brandName"))) {
            andExprs.add(EntityCondition.makeCondition("brandName", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("brandName") + "%")));
        }


        //默认条件
        // 未删除的
        andExprs.add(EntityCondition.makeCondition("isDel", EntityOperator.NOT_EQUAL, "Y"));
        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String productBrandId = gv.getString("productBrandId");
                String brandName = gv.getString("brandName");
                String contentId = gv.getString("contentId");
                String brandNameAlias = gv.getString("brandNameAlias");
                String brandDesc = gv.getString("brandDesc");

                String imgUrl = "";
                if (UtilValidate.isNotEmpty(contentId)) {
                    imgUrl = "/content/control/getImage?contentId=" + contentId;
                }
                map.put("productBrandId", productBrandId);
                map.put("brandName", brandName);
                map.put("contentId", contentId);
                map.put("brandNameAlias", brandNameAlias);
                map.put("brandDesc", brandDesc);
                map.put("imgUrl", imgUrl);

                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }


        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    // Add by zhajh at 20180412 取得品牌的信息(弹框用) End

    /**
     * 根据品牌编码取得品牌信息
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> getBrandListByIds(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String ids = (String) context.get("ids");
        Delegator delegator = dispatchContext.getDelegator();
        List<Map<String, Object>> brandInfoList = FastList.newInstance();
        List<String> brandIdList = FastList.newInstance();
        Map<String, Object> map = FastMap.newInstance();
        try {

            // 取得品牌Id信息列表
            if (UtilValidate.isNotEmpty(ids)) {
                String[] tBrandArray = ids.split(",");
                for (String curBrandId : tBrandArray) {
                    brandIdList.add(curBrandId);
                }
            }
            // 取得代金券信息
            if (brandIdList.size() > 0) {

                for (String brandId : brandIdList) {
                    GenericValue brandInfo = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", brandId));
                    map = FastMap.newInstance();
                    map.put("brandInfo", brandInfo);
                    if (UtilValidate.isNotEmpty(brandInfo)) {
                        map.put("productBrandId", brandInfo.getString("productBrandId"));
                        map.put("brandName", brandInfo.getString("brandName"));
                        map.put("brandNameAlias", brandInfo.getString("brandNameAlias"));
                        map.put("contentId", brandInfo.getString("contentId"));
                        String imgUrl = "";
                        if (UtilValidate.isNotEmpty(brandInfo.getString("contentId"))) {
                            imgUrl = "/content/control/getImage?contentId=" + brandInfo.getString("contentId");
                        }
                        map.put("imgUrl", imgUrl);
                        map.put("brandDesc", brandInfo.getString("brandDesc"));
                        brandInfoList.add(map);
                    }
                }

            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        //返回的参数
        result.put("brandInfoList", brandInfoList);
        return result;
    }


    // Add by zhajh at 20180413 取得店铺的信息(弹框用) Begin

    /**
     * 查询店铺列表，弹出框使用
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getStoreListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> recordsList = FastList.newInstance();

        //总记录数
        int recordsListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PS", "ProductStore");
        dynamicView.addAlias("PS", "productStoreId");

        dynamicView.addMemberEntity("PG", "PartyGroup");
        dynamicView.addAlias("PG", "partyId");
        dynamicView.addAlias("PG", "partyName");
        dynamicView.addAlias("PG", "productStoreId");
        dynamicView.addViewLink("PS", "PG", Boolean.TRUE, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));

        dynamicView.addMemberEntity("PB", "PartyBusiness");
        dynamicView.addAlias("PB", "description");
        dynamicView.addAlias("PB", "logoImg");
        dynamicView.addViewLink("PG", "PB", Boolean.TRUE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));


        fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("logoImg");
        fieldsToSelect.add("partyName");

        //排序字段名称
        String sortField = "partyId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);


        //按店铺名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("storeName"))) {
            andExprs.add(EntityCondition.makeCondition("partyName", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("storeName") + "%")));
        }

        if (UtilValidate.isNotEmpty(context.get("partyId"))) {
            andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("partyId") + "%")));
        }

        //默认条件
        andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL, "10000"));
        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String productStoreId = gv.getString("productStoreId");
                String storeName = gv.getString("partyName");
                String logoImg = gv.getString("logoImg");
                String partyId = gv.getString("partyId");

                String imgUrl = "";
                if (UtilValidate.isNotEmpty(logoImg)) {
                    imgUrl = "/content/control/getImage?contentId=" + logoImg;
                }
                map.put("productStoreId", productStoreId);
                map.put("storeName", storeName);
                map.put("logoImg", logoImg);
                map.put("partyId", partyId);
                map.put("imgUrl", imgUrl);

                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }


        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    // Add by zhajh at 20180413 取得店铺的信息(弹框用) End


    /**
     * 根据店铺编码取得店铺信息
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public Map<String, Object> getStoreListByIds(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String ids = (String) context.get("ids");
        Delegator delegator = dispatchContext.getDelegator();
        List<Map<String, Object>> storeInfoList = FastList.newInstance();
        List<String> storeIdList = FastList.newInstance();
        Map<String, Object> map = FastMap.newInstance();
        try {

            // 取得品牌Id信息列表
            if (UtilValidate.isNotEmpty(ids)) {
                String[] tStoreArray = ids.split(",");
                for (String curStoreId : tStoreArray) {
                    storeIdList.add(curStoreId);
                }
            }
            // 取得店铺信息
            if (storeIdList.size() > 0) {

                for (String storeId : storeIdList) {

                    map = FastMap.newInstance();
//                    List<GenericValue> storeInfos = delegator.findByAnd("GetPartyBusinnessInfos", UtilMisc.toMap("productStoreId", storeId));
                    List<GenericValue> storeInfos = delegator.findByAnd("PartyGroup", UtilMisc.toMap("productStoreId", storeId));
                    GenericValue storeInfo = null;
                    if (UtilValidate.isNotEmpty(storeInfos)) {
                        storeInfo = storeInfos.get(0);
                    }
                    if (UtilValidate.isNotEmpty(storeInfo)) {
                        map.put("storeInfo", storeInfo);
                        map.put("productStoreId", storeInfo.getString("productStoreId"));
                        map.put("partyName", storeInfo.getString("partyName"));
                        map.put("partyId", storeInfo.getString("partyId"));
                        String imgUrl = "";
                        GenericValue productBuinessInfo = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", storeInfo.getString("partyId")));
                        if (UtilValidate.isNotEmpty(productBuinessInfo)) {

                            if (UtilValidate.isNotEmpty(productBuinessInfo.getString("logoImg"))) {
                                imgUrl = "/content/control/getImage?contentId=" + productBuinessInfo.getString("logoImg");
                                map.put("logoImg", productBuinessInfo.getString("logoImg"));
                            }
                        }
                        map.put("imgUrl", imgUrl);
                        storeInfoList.add(map);
                    }
                }

            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        //返回的参数
        result.put("storeInfoList", storeInfoList);
        return result;
    }


    /**
     * 设置秒杀商品的结束时间
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> editEndDateSecKill(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Timestamp endDate = (Timestamp) context.get("endDate");
        String productSkId = (String) context.get("productSkId");
        String productStoreId = (String) context.get("productStoreId");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        try {

            // 取得组合商品信息
            GenericValue productSeckill = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", productSkId));
            // 取得秒杀商品关联店铺信息
            List<GenericValue> productStoreProductActAppls = delegator.findByAnd("ProductStoreProductActAppl", UtilMisc.toMap("activityId", productSkId));

            String chkFlg = "Y";
            String existProductStr = "";
            Timestamp thruDate = null;
            Timestamp fromDate = null;
            if (UtilValidate.isNotEmpty(productSeckill)) {
                thruDate = productSeckill.getTimestamp("activityEndDate");
                fromDate = productSeckill.getTimestamp("activityStartDate");
            }
            // 取得秒杀的产品列表信息
            List<GenericValue> promoProducts = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", productSkId));
            List<String> productIdRedisList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(endDate) && UtilValidate.isNotEmpty(thruDate)) {
                if (endDate.getTime() < thruDate.getTime()) {
                    //说明时间未过期可以直接修改
                    if (UtilValidate.isNotEmpty(productSeckill)) {
                        productSeckill.set("activityEndDate", endDate);
                        productSeckill.store();

                        if (UtilValidate.isNotEmpty(promoProducts)) {
                            for (GenericValue gv : promoProducts) {
                                if (UtilValidate.isNotEmpty(gv)) {
                                    String curProductId = gv.getString("productId");
                                    if (UtilValidate.isNotEmpty(curProductId)) {
                                        productIdRedisList.add(curProductId);
                                    }
                                    // Redis 信息的删除处理
//                                    if(UtilRedis.exists(curProductId+"_summary")){
//                                        UtilRedis.del(curProductId+"_summary");// 产品缓存
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_downPromo")){
//                                        UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_groupOrder")){
//                                        UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_seckill")) {
//                                        UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                                    }
                                }
                            }
                        }
                        if (UtilValidate.isNotEmpty(productIdRedisList)) {
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

                    if (UtilValidate.isNotEmpty(productStoreProductActAppls)) {
                        for (int i = 0; i < productStoreProductActAppls.size(); i++) {
                            GenericValue productStoreProductActAppl = productStoreProductActAppls.get(i);
                            productStoreProductActAppl.set("thruDate", endDate);
                            productStoreProductActAppl.store();
                        }
                    }
                    result.put("chkFlg", "Y");
                    return result;
                } else {
                    //判断有没有商品在活动中
                    List<GenericValue> products = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", productSkId));
                    List<String> productIds = FastList.newInstance();
                    if (products != null && products.size() > 0) {
                        for (GenericValue product : products) {
                            if (UtilValidate.isNotEmpty(product.get("productId"))) {
                                String productId = product.getString("productId");
                                productIds.add(productId);
                            }
                        }
                    }
//                    List<String> unUserdProductIdInfos = ProductServices.getUnUsedProductIds(delegator, productStoreId);
                    List<String> unUserdProductIdInfos = ProductServices.getUnUsedProductIds(delegator, productStoreId, fromDate, endDate);

                    chkFlg = "Y";
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
//                    existProductStr = Joiner.on(",").join(productIdExists);
                    result.put("chkFlg", chkFlg);

                    if ("N".equalsIgnoreCase(chkFlg)) {
                        result.put("errorMsg", "当前时间已存在指定商品的促销!");
                    }
                    if("Y".equalsIgnoreCase(chkFlg)){
                        boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator,productStoreId,fromDate,endDate);
                        if(isPromoAllExist){
                            chkFlg = "N";
                            result.put("chkFlg", chkFlg);
                            result.put("errorMsg", "当前时间已存在全场促销");
                        }
                    }

                    if ("Y".equalsIgnoreCase(chkFlg)) {
                        if (UtilValidate.isNotEmpty(productSeckill)) {
                            productSeckill.set("activityEndDate", endDate);
                            productSeckill.store();
                        }

                        if (UtilValidate.isNotEmpty(productStoreProductActAppls)) {
                            for (int i = 0; i < productStoreProductActAppls.size(); i++) {
                                GenericValue productStoreProductActAppl = productStoreProductActAppls.get(i);
                                productStoreProductActAppl.set("thruDate", endDate);
                                productStoreProductActAppl.store();
                            }
                        }
                        productIdRedisList = FastList.newInstance();
                        if (UtilValidate.isNotEmpty(promoProducts)) {
                            for (GenericValue gv : promoProducts) {
                                if (UtilValidate.isNotEmpty(gv)) {
                                    String curProductId = gv.getString("productId");
                                    if (UtilValidate.isNotEmpty(curProductId)) {
                                        productIdRedisList.add(curProductId);
                                    }
                                    // Redis 信息的删除处理
//                                    if(UtilRedis.exists(curProductId+"_summary")){
//                                        UtilRedis.del(curProductId+"_summary");// 产品缓存
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_downPromo")){
//                                        UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_groupOrder")){
//                                        UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                                    }
//                                    if(UtilRedis.exists(curProductId+"_seckill")) {
//                                        UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                                    }

                                }
                            }
                            if (UtilValidate.isNotEmpty(productIdRedisList)) {
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
                }
            }

        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    /**
     * 查看商品秒杀驳回原因
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> findProductSkAuditMessage(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productSkId = (String) context.get("productSkId");
        Delegator delegator = dcx.getDelegator();
        List<GenericValue> productSkAuditList = FastList.newInstance();
        try {
            productSkAuditList = delegator.findByAnd("ProductActivity", UtilMisc.toMap("activityId", productSkId));
            GenericValue productSkAudit = EntityUtil.getFirst(productSkAuditList);
            String curAuditMessage = "没有输入内容";
            if (UtilValidate.isNotEmpty(productSkAudit)) {
                curAuditMessage = productSkAudit.getString("auditMessage");
            }
            result.put("curAuditMessage", curAuditMessage);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Map<String, Object> findProductIdsByPromoId(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        String productPromoId = (String) context.get("productPromoId");
        List<GenericValue> products = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
        List<String> productIds = FastList.newInstance();
        if (products != null && products.size() > 0) {
            for (GenericValue product : products) {
                if (UtilValidate.isNotEmpty(product.get("productId"))) {
                    String productId = product.getString("productId");
                    productIds.add(productId);
                }
            }
        }

        String startDate = "";
        String endDate = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<GenericValue> promos = delegator.findList("ProductStorePromoAndAppl", EntityCondition.makeCondition("productPromoId", EntityOperator.EQUALS, productPromoId), null, null, null, false);
        // 取得组合商品和商品的关联信息
        if (UtilValidate.isNotEmpty(promos)) {
            GenericValue promo = promos.get(0);
            if (UtilValidate.isNotEmpty(promo.getTimestamp("fromDate")) && UtilValidate.isNotEmpty(promo.getTimestamp("thruDate"))) {
                startDate = df.format(promo.getTimestamp("fromDate"));
                endDate = df.format(promo.getTimestamp("thruDate"));
            }
        }
        String productIdStr = Joiner.on(",").join(productIds);
        result.put("productIds", productIdStr);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        return result;
    }

    public Map<String, Object> findProductIdsByActivityId(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        String activityId = (String) context.get("activityId");
        String startDate = "";
        String endDate = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 取得组合商品信息
        GenericValue productActivity = delegator.findByPrimaryKey("ProductActivity", UtilMisc.toMap("activityId", activityId));
        if (UtilValidate.isNotEmpty(productActivity)) {
            startDate = df.format(productActivity.getTimestamp("activityStartDate"));
            endDate = df.format(productActivity.getTimestamp("activityEndDate"));
        }


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
        String productIdStr = Joiner.on(",").join(productIds);
        result.put("productIds", productIdStr);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        return result;
    }



    /*public Map<String, Object> checkEndDate(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productPromoId = (String) context.get("productPromoId");
        Delegator delegator = dcx.getDelegator();
        //将过期时间和现在对比

        result.put("chkFlag","Y");
        return result;
    }*/

}
