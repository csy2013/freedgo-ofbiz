package com.yuaoq.yabiz.mobile.services;

/**
 * Created by changsy on 2017/11/1.
 */
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.bouncycastle.util.Times;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.config.ProductConfigWorker;
import org.ofbiz.product.config.ProductConfigWrapper;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by changsy on 2017/7/17.
 */
@SuppressWarnings("all")
public class ProductService {

    private static String module = ProductService.class.getName();
    //增加product的cache
    public static UtilCache<String, Map> productSummaryCache = UtilCache.createUtilCache("product.content.summary", true);

    public static Map<String, Object> recommendProducts1(DispatchContext dcx, Map<String, ? extends Object> context) {
        String productStoreId = (String) context.get("productStoreId");
        Delegator delegator = dcx.getDelegator();
        Set<String> productIds = FastSet.newInstance();
        Map<String, Object> retMap = ServiceUtil.returnSuccess();
        //找到店铺目录
        try {
            List<GenericValue> prodCatalogStores = EntityUtil.filterByDate(delegator.findByAndCache("ProdCatalogStore", UtilMisc.toMap("productStoreId", productStoreId, "showInShop", "Y"), UtilMisc.toList("sequenceNum", "prodCatalogId")), true);
            if (UtilValidate.isNotEmpty(prodCatalogStores)) {
                GenericValue prodCatalog = prodCatalogStores.get(0);
                String catalogId = (String) prodCatalog.get("prodCatalogId");
                //获取目录下推荐分类
                GenericValue prodCatalogCategory = EntityUtil.getFirst(EntityUtil.filterByDate(delegator.findByAndCache("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", catalogId, "prodCatalogCategoryTypeId", "PCCT_WHATS_NEW"), UtilMisc.toList("sequenceNum", "productCategoryId")), true));
                if (UtilValidate.isNotEmpty(prodCatalogCategory)) {
                    String categoryId = (String) prodCatalogCategory.get("productCategoryId");
                    List<GenericValue> categoryMembers = EntityUtil.filterByDate(delegator.findByAndCache("ProductCategoryMember", UtilMisc.toMap("productCategoryId", categoryId), UtilMisc.toList("sequenceNum")), true);
                    if (UtilValidate.isNotEmpty(categoryMembers)) {
                        for (int i = 0; i < categoryMembers.size(); i++) {
                            GenericValue member = categoryMembers.get(i);
                            productIds.add((String) member.get("productId"));

                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        retMap.put("productIds", productIds);

        return retMap;

    }

    public static Map<String, Object> productsSummary(DispatchContext dcx, Map<String, ? extends Object> context) {

        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        String productIdStrs = (String) context.get("productIds");
        String[] productIds = null;
        if (UtilValidate.isNotEmpty(productIdStrs)) {
            productIds = productIdStrs.split(",");
        }
        String webSiteId = (String) context.get("webSiteId");
        String prodCatalogId = (String) context.get("prodCatalogId");
        String productStoreId = (String) context.get("productStoreId");
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();

        Map<String, Object> retMap = ServiceUtil.returnSuccess();
        List retList = FastList.newInstance();
        GenericValue productCalculatedInfo = null;
        String defaultCurrency = "CNY";
        for (int i = 0; i < productIds.length; i++) {
            String productId = productIds[i];
            /*if (productSummaryCache.get(productId) != null) {
                retList.add(productSummaryCache.get(productId));
                continue;
            }*/

//            Person person = new Person(100, "alan");
//            UtilRedis.set("person:100", person);
//            person = new Person(101, "bruce");
//            UtilRedis.set("person:101", person);
//
//            System.out.println(UtilRedis.get("person:100").toString());
//            System.out.println(UtilRedis.get("person:101").toString());

            Map<String, Object> result = FastMap.newInstance();
           /* boolean productInfoExist = UtilRedis.exists(productId + "_summary");
            if (productInfoExist) {
                result = (Map<String, Object>) UtilRedis.get(productId + "_summary");
            } else {*/
                //商品缓存过期时间
                Timestamp expireTime = new Timestamp(System.currentTimeMillis());
                BigDecimal finalPrice = BigDecimal.ZERO;
                GenericValue miniProduct = null;
                try {
                    miniProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    //商品销售时间
                    expireTime=miniProduct.getTimestamp("salesDiscontinuationDate");
                    productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", productId));
                    //标签
                    List<GenericValue> tagAssoc = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", productId));
                    List<Map> tagList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(tagAssoc)) {
                        List<GenericValue> tags = EntityUtil.getRelated("Tag", tagAssoc);
                        if (UtilValidate.isNotEmpty(tags)) {
                            for (GenericValue tag : tags) {
                                Map<String, String> tagMap = FastMap.newInstance();
                                tagMap.put("tagId", tag.getString("tagId"));
                                tagMap.put("tagName", tag.getString("tagName"));
                                tagList.add(tagMap);
                            }
                        }

                    }
                    if (miniProduct != null) {

                        result.put("tags", tagList);
                        // calculate the "your" price
                        Map<String, Object> priceResult = null;
                        try {
                            priceResult = dispatcher.runSync("calculateProductPrice",
                                    UtilMisc.<String, Object>toMap("product", miniProduct, "prodCatalogId", prodCatalogId, "webSiteId", webSiteId, "currencyUomId", defaultCurrency, "autoUserLogin", userLogin, "productStoreId", productStoreId));
                            if (ServiceUtil.isError(priceResult)) {

                            } else {
                                result.put("price", priceResult.get("defaultPrice"));
                                result.put("basePrice", priceResult.get("basePrice"));
//                            finalPrice = (BigDecimal) priceResult.get("basePrice");
                                result.put("marketPrice", priceResult.get("marketPrice"));
                            }
                        } catch (GenericServiceException e) {
                            Debug.logError(e, "Error changing item status to " + "ITEM_COMPLETED" + ": " + e.toString(), module);
                        }

                        //积分兑奖比例
                        // get aggregated product totalPrice
                        if ("AGGREGATED".equals(miniProduct.get("productTypeId")) || "AGGREGATED_SERVICE".equals(miniProduct.get("productTypeId"))) {
                            ProductConfigWrapper configWrapper = ProductConfigWorker.getProductConfigWrapper(productId, defaultCurrency, prodCatalogId, webSiteId, productStoreId, userLogin, dispatcher, delegator, locale);
                            if (configWrapper != null) {
                                configWrapper.setDefaultConfig();
                                // Check if Config Price has to be displayed with tax
                                result.put("totalPrice", df.format(configWrapper.getTotalPrice()));
                            }
                        }
                        //分类id
                        result.put("primaryProductCategoryId", miniProduct.getString("primaryProductCategoryId"));
                        //积分抵扣类型
                        result.put("integralDeductionType", miniProduct.getString("integralDeductionType"));
                        //积分抵扣上限
                        result.put("integralDeductionUpper", miniProduct.getString("integralDeductionUpper"));
                        result.put("nowTimeLong", UtilDateTime.nowTimestamp());
                        // make the miniProductContentWrapper
                        ProductContentWrapper miniProductContentWrapper = new ProductContentWrapper(dispatcher, miniProduct, locale, "text/html");

                        String mediumImageUrl = miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM").toString();
                        String largeImageUrl = miniProductContentWrapper.get("XTRA_IMG_1_LARGE").toString();
                        String originalImageUrl = miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString();
                        String smallImageUrl = miniProductContentWrapper.get("XTRA_IMG_1_SMALL").toString();

                        if (UtilValidate.isEmpty(mediumImageUrl)) {
                            mediumImageUrl = miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString();
                        }
                        if (UtilValidate.isEmpty(largeImageUrl)) {
                            largeImageUrl = miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString();
                        }
                        if (UtilValidate.isEmpty(smallImageUrl)) {
                            smallImageUrl = miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString();
                        }
                        if (UtilValidate.isEmpty(originalImageUrl)) {
                            originalImageUrl = miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString();
                        }

                        String productName = miniProductContentWrapper.get("PRODUCT_NAME").toString();
                        String isVirtual = miniProductContentWrapper.get("IS_VIRTUAL").toString();
                        String wrapProductId = miniProductContentWrapper.get("PRODUCT_ID").toString();
                        //增加虚拟产品对应的信息featureTypeId,featureId
                        //增加虚拟产品对应的信息featureTypeId,featureId
                        String isVariant = miniProduct.getString("isVariant");
                        String boolVirtual = miniProduct.getString("isVirtual");

                        if (UtilValidate.isNotEmpty(isVariant) && UtilValidate.isNotEmpty(boolVirtual)) {
                            if (isVariant.equals("Y") || boolVirtual.equalsIgnoreCase("Y")) {
                                Map<String, Object> variantTreeMap = null;
                                try {
                                    //获取商品对应的特征值
                                    variantTreeMap = dispatcher.runSync("getProductVariantSet", UtilMisc.toMap("productId", productId));
                                    if (ServiceUtil.isError(variantTreeMap)) {
                                        return ServiceUtil.returnError("获取产品特征错误");
                                    } else {
                                        if (!boolVirtual.equalsIgnoreCase("Y")) {
                                            result.put("variantTreeChoose", variantTreeMap.get("variantTreeChoose"));
                                        }
                                        result.put("variantTree", variantTreeMap.get("variantTree"));
                                    }
                                } catch (GenericServiceException e) {
                                    e.printStackTrace();
                                }

                            }
                            if (boolVirtual.equalsIgnoreCase("Y") || isVariant.equalsIgnoreCase("Y")) {
                                //获取对应的子产品及特征值
                                List<GenericValue> featureProducts = FastList.newInstance();
                                if (isVariant.equalsIgnoreCase("Y")) {
                                    featureProducts = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", miniProduct.getString("mainProductId")));
                                } else if (boolVirtual.equalsIgnoreCase("Y")) {
                                    featureProducts = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", productId));
                                }
                                List<Map<String, String>> featureInfo = FastList.newInstance();
                                if (UtilValidate.isNotEmpty(featureProducts)) {
                                    for (int j = 0; j < featureProducts.size(); j++) {
                                        GenericValue featureProduct = featureProducts.get(j);
                                        String featureProductId = featureProduct.getString("productId");
                                        String featureId = featureProduct.getString("featureProductId");
                                        /** 活动商品可用库存 */
                                        List<GenericValue> finventoryItem = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", featureProductId));
                                        if (UtilValidate.isNotEmpty(finventoryItem)) {
                                            GenericValue inventoryItem = EntityUtil.getFirst(finventoryItem);
                                            BigDecimal accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal");
                                            if (accountingQuantityTotal.compareTo(BigDecimal.ONE) >= 0) {
                                                Map<String, String> featureObj = FastMap.newInstance();
                                                featureObj.put("productId", featureProductId);
                                                featureObj.put("featureIds", featureId);
                                                featureInfo.add(featureObj);
                                            }
                                        }

                                    }
                                }
                                if (UtilValidate.isNotEmpty(featureInfo)) {
                                    result.put("featuresInfo", featureInfo);
                                }
                            }
                        }

                        String baseImg = UtilProperties.getMessage("application.properties", "image.base.url", locale);
                        String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                        if (uploadType.equals("FTP")) {
                            baseImg = "";
                        }
                        List<GenericValue> serviceAssoc = delegator.findByAnd("ProductSupportServiceAssoc", UtilMisc.toMap("productId", productId));
                        if (UtilValidate.isNotEmpty(serviceAssoc)) {
                            List<String> serviceTags = FastList.newInstance();
                            for (int j = 0; j < serviceAssoc.size(); j++) {
                                GenericValue assoc = serviceAssoc.get(j);
                                serviceTags.add(assoc.getString("enumId"));
                            }
                            result.put("serviceTag", serviceTags);
                        }

                        result.put("productId", productId);
                        result.put("catalogId", prodCatalogId);
                        result.put("mediumImageUrl", baseImg + mediumImageUrl);
                        result.put("smallImageUrl", baseImg + smallImageUrl);
                        //result.put("longDescription",longDescription);
                        result.put("productName", productName);
                        result.put("isVirtual", isVirtual);
                        result.put("largeImageUrl", baseImg + largeImageUrl);
                        result.put("originalImageUrl", baseImg + originalImageUrl);
                        result.put("productTypeId", miniProduct.get("productTypeId"));
                        result.put("wrapProductId", wrapProductId);
                        result.put("productCalculatedInfo", productCalculatedInfo==null?null:productCalculatedInfo.toMap());
                        result.put("introductionDate", miniProduct.get("introductionDate"));
                        result.put("salesDiscontinuationDate", miniProduct.get("salesDiscontinuationDate"));
                        result.put("subTitle", miniProduct.get("internalName"));
                        result.put("weight", miniProduct.get("weight"));
                        result.put("isBondedGoods", miniProduct.get("isBondedGoods"));//保税
                        result.put("integralDeductionType", miniProduct.get("integralDeductionType"));//1.不可使用积分2:百分比抵扣3:固定金额抵扣
                        result.put("integralDeductionUpper", miniProduct.get("integralDeductionUpper")); //积分抵扣数
                        result.put("voucherAmount", miniProduct.get("voucherAmount"));//代金劵面值
                        result.put("isInner", miniProduct.get("isInner"));//是否自营
                        result.put("isSku", miniProduct.get("isVirtual"));
                        result.put("isVariant", isVariant);


                    }
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Problems reading order header from datasource.", module);

                }
                //获取产品的应用的促销信息，直降，团购、秒杀、优惠劵等等
                finalPrice = (BigDecimal) result.get("basePrice");
                if (result.get("productTypeId").equals("FINISHED_GOOD")) {
                    try {
                        //查找缓存

                        //获取产品的应用的促销信息，直降，团购、秒杀、优惠劵等等
                        Map<String, Object> result1 = dispatcher.runSync("getProductPromoInfoByProductId", UtilMisc.toMap("productId", productId));
                        if (ServiceUtil.isError(result1)) {
                            result.put("message", ServiceUtil.getErrorMessage(result));
                            result.put("retCode", 0);

                        } else {

                            Map<String, Object> priceDownInfo = FastMap.newInstance();
                            if (UtilValidate.isNotEmpty(result1.get("priceDownInfo"))) {
                                Map downInfo = (Map) result1.get("priceDownInfo");
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
                                result.put("priceDownInfo", priceDownInfo);
                                //查找这个直降的结束日期,如果促销结束时间比商品销售时间小，则将缓存结束时间设置为促销结束时间
                                GenericValue promoAppl = delegator.findByAnd("ProductStorePromoAppl",UtilMisc.toMap("productPromoId", downInfo.get("productPromoId"))).get(0);
                                Timestamp promoApplEndTime =promoAppl.getTimestamp("thruDate");
                                if(expireTime==null){
                                    expireTime=promoApplEndTime;
                                }else{
                                    if(expireTime.getTime()>promoApplEndTime.getTime()){
                                        expireTime =promoApplEndTime;
                                    }
                                }

                            }
                            Map<String, Object> orderGroupInfo = FastMap.newInstance();
                            if (UtilValidate.isNotEmpty(result1.get("orderGroupInfo"))) {
                                Map orderGroupMap = (Map) result1.get("orderGroupInfo");
                                orderGroupInfo.put("activityDesc", orderGroupMap.get("activityDesc"));
                                orderGroupInfo.put("activityId", orderGroupMap.get("activityId"));
                                orderGroupInfo.put("activityCode", orderGroupMap.get("activityCode"));

                                orderGroupInfo.put("activityStartDate", orderGroupMap.get("activityStartDate"));
                                orderGroupInfo.put("activityEndDate", orderGroupMap.get("activityEndDate"));

                                orderGroupInfo.put("activityQuantity", orderGroupMap.get("activityQuantity"));
                                orderGroupInfo.put("orderPrice", result1.get("orderGroupPrice"));//团购价
                                finalPrice = (BigDecimal) result1.get("orderGroupPrice");
                                orderGroupInfo.put("hasGroup", orderGroupMap.get("hasGroup"));
                                orderGroupInfo.put("leaveQuantity", orderGroupMap.get("leaveQuantity"));
                                orderGroupInfo.put("averageCustomerRating", orderGroupMap.get("averageCustomerRating"));
                                orderGroupInfo.put("hasBuyQuantity", orderGroupMap.get("hasBuyQuantity"));

                                orderGroupInfo.put("productStoreId", orderGroupMap.get("productStoreId"));
                                orderGroupInfo.put("activityPayType", orderGroupMap.get("activityPayType"));
                                orderGroupInfo.put("scoreValue", orderGroupMap.get("scoreValue"));
                                orderGroupInfo.put("activityName", orderGroupMap.get("activityName"));
                                orderGroupInfo.put("limitQuantity", orderGroupMap.get("activityQuantity"));
                                int quantity = ((Long) orderGroupMap.get("limitQuantity")).intValue();
                                orderGroupInfo.put("limitQuantity", quantity);
                                //成团人员、人头像
                                result.put("orderGroupInfo", orderGroupInfo);

                                Timestamp activityEnde = (Timestamp) orderGroupMap.get("activityEndDate");
                                if(expireTime==null){
                                    expireTime=activityEnde;
                                }else{
                                    if(expireTime.getTime()>activityEnde.getTime()){
                                        expireTime =activityEnde;
                                    }
                                }


                            }
                            if (UtilValidate.isNotEmpty(result1.get("groupInfo"))) {
                                result.put("groupInfo", result1.get("groupInfo"));
                            }

                            Map<String, Object> secKillInfo = FastMap.newInstance();
                            if (UtilValidate.isNotEmpty(result1.get("secKillInfo"))) {
                                Map secKillMap = (Map) result1.get("secKillInfo");
                                secKillInfo.put("activityDesc", secKillMap.get("activityDesc"));
                                secKillInfo.put("activityId", secKillMap.get("activityId"));
                                secKillInfo.put("activityCode", secKillMap.get("activityCode"));
                                //当前用户可以购物的次数
                                int quantity = ((Long) secKillMap.get("limitQuantity")).intValue();
                                secKillInfo.put("limitQuantity", quantity);
                                secKillInfo.put("activityStartDate", secKillMap.get("activityStartDate"));
                                secKillInfo.put("activityEndDate", secKillMap.get("activityEndDate"));

                                secKillInfo.put("activityQuantity", secKillMap.get("activityQuantity"));

                                secKillInfo.put("hasBuyQuantity", secKillMap.get("hasBuyQuantity"));
                                secKillInfo.put("orderPrice", result1.get("secKillPrice"));//团购价
                                finalPrice = (BigDecimal) result1.get("secKillPrice");
                                secKillInfo.put("hasGroup", secKillMap.get("hasGroup"));
                                secKillInfo.put("leaveQuantity", secKillMap.get("leaveQuantity"));
                                secKillInfo.put("averageCustomerRating", secKillMap.get("averageCustomerRating"));

                                secKillInfo.put("productStoreId", secKillMap.get("productStoreId"));
                                secKillInfo.put("activityPayType", secKillMap.get("activityPayType"));
                                secKillInfo.put("scoreValue", secKillMap.get("scoreValue"));
                                secKillInfo.put("activityName", secKillMap.get("activityName"));
                                result.put("secKillInfo", secKillInfo);
                                Timestamp activityEnde = (Timestamp) secKillMap.get("activityEndDate");
                                if(expireTime==null){
                                    expireTime=activityEnde;
                                }else{
                                    if(expireTime.getTime()>activityEnde.getTime()){
                                        expireTime =activityEnde;
                                    }
                                }
                            }
                        }

                        //积分兑奖比例
                        GenericValue integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));//积分抵现规则表
                            /*1.不可使用积分2:百分比抵扣3:固定金额抵扣*/
                        Long integralValue = integralPerMoney.getLong("integralValue");
                        String integralDeductionType = (String) result.get("integralDeductionType");
                        BigDecimal integralDeductionUpper = (BigDecimal) result.get("integralDeductionUpper");
                        if (UtilValidate.isNotEmpty(integralDeductionType)) {
                            if (integralDeductionType.equalsIgnoreCase("2")) {
                                BigDecimal scorePrice = finalPrice.multiply(integralDeductionUpper).divide(new BigDecimal(100));
                                BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
                                BigDecimal diffPrice = finalPrice.subtract(scorePrice).setScale(2, BigDecimal.ROUND_HALF_UP);
                                result.put("scorePrice", scorePrice.setScale(2, BigDecimal.ROUND_HALF_UP)); //积分抵扣的金额
                                result.put("scoreValue", new Double(Math.floor(score.doubleValue())).intValue());//需要的积分
                                result.put("diffPrice", diffPrice);//价格+积分
                            } else if (integralDeductionType.equalsIgnoreCase("3")) {
                                BigDecimal diffPrice = finalPrice.subtract(integralDeductionUpper).setScale(2, BigDecimal.ROUND_HALF_UP);
                                result.put("scorePrice", integralDeductionUpper.setScale(2, BigDecimal.ROUND_HALF_UP));//积分抵扣的金额
                                result.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)).setScale(0, BigDecimal.ROUND_DOWN));//需要的积分
                                result.put("diffPrice", diffPrice);
                            }
                        }

                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                        result.put("message", "获取产品对应的促销活动信息错误");
                        result.put("retCode", 0);

                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                }
                //设置产品缓存
                /*if(expireTime==null){
                    UtilRedis.set(productId + "_summary", result);
                }else{
                    long expireLongTime =expireTime.getTime()-System.currentTimeMillis();
                    if(expireLongTime>0L){
                        UtilRedis.set(productId + "_summary", result,expireLongTime);
                    }
                }*/

//            }

            /*if (productSummaryCache != null) {
                productSummaryCache.put(productId, result);
            }*/
            retList.add(result);

        }
        retMap.put("resultData", retList);
        return retMap;
    }


    public static Map<String, Object> productDetail(DispatchContext dcx, Map<String, ? extends Object> context) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        String productId = (String) context.get("productId");
        String webSiteId = (String) context.get("webSiteId");

        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();

        Map<String, Object> retMap = ServiceUtil.returnSuccess();

        /*boolean productInfoExist = UtilRedis.exists(productId + "_detail");
        if (productInfoExist) {
            retMap = (Map<String, Object>) UtilRedis.get(productId + "_detail");
        } else {*/
            Map<String, Object> restData = FastMap.newInstance();
            String defaultCurrency = "CNY";
            GenericValue miniProduct = null;
            String prodCatalogId = null;
            GenericValue productCalculatedInfo = null;
            String productStoreId = null;
            Timestamp expireTime=null;
            try {
                miniProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                expireTime=miniProduct.getTimestamp("salesDiscontinuationDate");
                
                productStoreId = (String) miniProduct.get("productStoreId");
                restData.put("productStoreId", productStoreId);
                productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", productId));
                //标签
                List<GenericValue> tagAssoc = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", productId));
                List<Map> tagList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(tagAssoc)) {
                    List<GenericValue> tags = EntityUtil.getRelated("Tag", tagAssoc);
                    if (UtilValidate.isNotEmpty(tags)) {
                        for (GenericValue tag : tags) {
                            Map<String, String> tagMap = FastMap.newInstance();
                            tagMap.put("tagId", tag.getString("tagId"));
                            tagMap.put("tagName", tag.getString("tagName"));
                            tagList.add(tagMap);
                        }
                    }
                }
                restData.put("tags", tagList);
                //产品参数
                List<GenericValue> productParameters = delegator.findByAnd("ProductParameter", UtilMisc.toMap("productId", productId));
                List<Map> paramList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(productParameters)) {
                    for (GenericValue para : productParameters) {
                        Map<String, String> pMap = FastMap.newInstance();
                        pMap.put("productParameterId", para.getString("productParameterId"));
                        pMap.put("productParameterName", para.getString("parameterName"));
                        pMap.put("parameterDescription", para.getString("parameterDescription"));
                        paramList.add(pMap);
                    }
                }
                restData.put("productParameters", paramList);
                if (miniProduct != null) {
                    // calculate the "your" price
                    Map<String, Object> priceResult = null;
                    try {
                        priceResult = dispatcher.runSync("calculateProductPrice",
                                UtilMisc.<String, Object>toMap("product", miniProduct, "prodCatalogId", prodCatalogId, "webSiteId", webSiteId, "currencyUomId", defaultCurrency, "autoUserLogin", userLogin, "productStoreId", productStoreId));
                        if (ServiceUtil.isError(priceResult)) {

                        } else {
                            restData.put("price", priceResult.get("defaultPrice"));
                            restData.put("marketPrice", priceResult.get("marketPrice"));
                        }
                    } catch (GenericServiceException e) {
                        Debug.logError(e, "Error changing item status to " + "ITEM_COMPLETED" + ": " + e.toString(), module);
                    }

                    //如果是sku主商品，库存为子商品库存之和
                    BigDecimal accountingQuantityTotal = BigDecimal.ZERO;

                    /** 活动商品可用库存 */
                    List<GenericValue> inventory_item = null;
                    try {
                        inventory_item = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId));
                    } catch (GenericEntityException e) {
                        Debug.log(e.getMessage());
                    }

                    if (UtilValidate.isNotEmpty(inventory_item)) {
                        GenericValue inventoryItem = EntityUtil.getFirst(inventory_item);
                        accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("accountingQuantityTotal");
                        ;
                        BigDecimal lockQuantityTotal = inventoryItem.getBigDecimal("lockQuantityTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("lockQuantityTotal");
                        restData.put("accountingQuantityTotal", accountingQuantityTotal.subtract(lockQuantityTotal));
                    }

                    // returns: basePrice listPrice ,COMPETITIVE_PRICE,AVERAGE_COST
                    restData.put("priceResult", priceResult);
                    BigDecimal defaultPrice = (BigDecimal) priceResult.get("defaultPrice");
                    //积分兑奖比例
                    GenericValue integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));//积分抵现规则表
                /*1.不可使用积分2:百分比抵扣3:固定金额抵扣*/
                    Long integralValue = integralPerMoney.getLong("integralValue");
                    String integralDeductionType = miniProduct.getString("integralDeductionType");
                    BigDecimal integralDeductionUpper = miniProduct.getBigDecimal("integralDeductionUpper");
                    if (integralDeductionType.equalsIgnoreCase("2")) {
                        BigDecimal scorePrice = defaultPrice.multiply(integralDeductionUpper).divide(new BigDecimal(100));
                        BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
                        restData.put("scorePrice", scorePrice.setScale(2, BigDecimal.ROUND_HALF_UP)); //积分抵扣的金额
                        restData.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
                    } else if (integralDeductionType.equalsIgnoreCase("3")) {
                        restData.put("scorePrice", integralDeductionUpper.setScale(2, BigDecimal.ROUND_HALF_UP));//积分抵扣的金额
                        restData.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
                    }

                    // get aggregated product totalPrice
                    if ("AGGREGATED".equals(miniProduct.get("productTypeId")) || "AGGREGATED_SERVICE".equals(miniProduct.get("productTypeId"))) {
                        ProductConfigWrapper configWrapper = ProductConfigWorker.getProductConfigWrapper(productId, defaultCurrency, prodCatalogId, webSiteId, productStoreId, userLogin, dispatcher, delegator, locale);
                        if (configWrapper != null) {
                            configWrapper.setDefaultConfig();
                            // Check if Config Price has to be displayed with tax
                            restData.put("totalPrice", df.format(configWrapper.getTotalPrice()));
                        }
                    }
                    restData.put("nowTimeLong", UtilDateTime.nowTimestamp());
                    // make the miniProductContentWrapper
                    ProductContentWrapper miniProductContentWrapper = new ProductContentWrapper(dispatcher, miniProduct, locale, "text/html");
//                String mediumImageUrl = miniProductContentWrapper.get("MEDIUM_IMAGE_URL").toString();
                    String description = miniProduct.getString("mobileDetails");
                    String productName = miniProductContentWrapper.get("PRODUCT_NAME").toString();
                    String isVirtual = miniProductContentWrapper.get("IS_VIRTUAL").toString();
                    String wrapProductId = miniProductContentWrapper.get("PRODUCT_ID").toString();
                    //增加虚拟产品对应的信息featureTypeId,featureId
                    String isVariant = miniProduct.getString("isVariant");
                    String boolVirtual = miniProduct.getString("isVirtual");

                    List<GenericValue> serviceAssoc = delegator.findByAnd("ProductSupportServiceAssoc", UtilMisc.toMap("productId", productId));
                    if (UtilValidate.isNotEmpty(serviceAssoc)) {
                        List<String> serviceTags = FastList.newInstance();
                        for (int j = 0; j < serviceAssoc.size(); j++) {
                            GenericValue assoc = serviceAssoc.get(j);
                            serviceTags.add(assoc.getString("enumId"));
                        }
                        restData.put("serviceTag", serviceTags);
                    }

                    if (UtilValidate.isNotEmpty(isVariant) && UtilValidate.isNotEmpty(boolVirtual)) {
                        if (isVariant.equals("Y") || boolVirtual.equalsIgnoreCase("Y")) {
                            Map<String, Object> variantTreeMap = null;
                            try {
                                //获取商品对应的特征值
                                variantTreeMap = dispatcher.runSync("getProductVariantSet", UtilMisc.toMap("productId", productId));
                                if (ServiceUtil.isError(variantTreeMap)) {
                                    return ServiceUtil.returnError("获取产品特征错误");
                                } else {
                                    if (!boolVirtual.equalsIgnoreCase("Y")) {
                                        restData.put("variantTreeChoose", variantTreeMap.get("variantTreeChoose"));
                                    }
                                    restData.put("variantTree", variantTreeMap.get("variantTree"));
                                }
                            } catch (GenericServiceException e) {
                                e.printStackTrace();
                            }

                        }
                        BigDecimal mainProductAccountingQuantityTotal = BigDecimal.ZERO;
                        if (boolVirtual.equalsIgnoreCase("Y") || isVariant.equalsIgnoreCase("Y")) {
                            //获取对应的子产品及特征值
                            List<GenericValue> featureProducts = FastList.newInstance();
                            if (isVariant.equalsIgnoreCase("Y")) {
                                featureProducts = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", miniProduct.getString("mainProductId")));
                            } else if (boolVirtual.equalsIgnoreCase("Y")) {
                                featureProducts = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", productId));
                            }
                            List<Map<String, Object>> featureInfo = FastList.newInstance();
                            if (UtilValidate.isNotEmpty(featureProducts)) {
                                for (int i = 0; i < featureProducts.size(); i++) {
                                    GenericValue featureProduct = featureProducts.get(i);
                                    String featureProductId = featureProduct.getString("productId");
                                    String featureId = featureProduct.getString("featureProductId");
                                    /** 活动商品可用库存 */
                                    List<GenericValue> finventoryItem = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", featureProductId));
                                    if (UtilValidate.isNotEmpty(finventoryItem)) {
                                        GenericValue inventoryItem = EntityUtil.getFirst(finventoryItem);
                                        accountingQuantityTotal = inventoryItem.getBigDecimal("accountingQuantityTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("accountingQuantityTotal");
                                        BigDecimal lockQuantityTotal = inventoryItem.getBigDecimal("lockQuantityTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("lockQuantityTotal");
                                        if (accountingQuantityTotal.compareTo(BigDecimal.ONE) >= 0) {
                                            Map<String, Object> featureObj = FastMap.newInstance();
                                            featureObj.put("productId", featureProductId);
                                            featureObj.put("featureIds", featureId);
                                            featureObj.put("accountingQuantityTotal", accountingQuantityTotal.subtract(lockQuantityTotal));
                                            featureInfo.add(featureObj);
                                        }
                                        mainProductAccountingQuantityTotal = mainProductAccountingQuantityTotal.add(accountingQuantityTotal.subtract(lockQuantityTotal));

                                    }
                                }
                            }
                            if (UtilValidate.isNotEmpty(featureInfo)) {
                                restData.put("featuresInfo", featureInfo);
                            }
                        }
                        if (boolVirtual.equalsIgnoreCase("Y")) {
                            restData.put("accountingQuantityTotal", mainProductAccountingQuantityTotal);
                        }
                    }


                    String baseImg = UtilProperties.getMessage("application.properties", "image.base.url", locale);
                    String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                    if (uploadType.equals("FTP")) {
                        baseImg = "";
                    }
                    String additionalImage1Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_1_DETAIL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_1_DETAIL").toString();
                    String additionalImage1Dedium = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM").toString();
                    String additionalImage1Large = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_1_LARGE").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_1_LARGE").toString();
                    String additionalImage1Orginal = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_1_ORIGINAL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_1_ORIGINAL").toString();
                    String additionalImage1Small = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_1_SMALL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_1_SMALL").toString();
                    if (UtilValidate.isEmpty(additionalImage1Dedium)) {
                        additionalImage1Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString()) ? "" : baseImg + miniProductContentWrapper.get("ADDITIONAL_IMAGE_1").toString();
                        additionalImage1Dedium = additionalImage1Detail;
                        additionalImage1Large = additionalImage1Detail;
                        additionalImage1Orginal = additionalImage1Detail;
                        additionalImage1Small = additionalImage1Detail;
                    }
                    List additionalImage1 = FastList.newInstance();
                    if (!additionalImage1Detail.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDetail", additionalImage1Detail);
                        additionalImage1.add(imageMap);
                    }
                    if (!additionalImage1Dedium.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDedium", additionalImage1Dedium);
                        additionalImage1.add(imageMap);
                    }
                    if (!additionalImage1Large.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageLarge", additionalImage1Large);
                        additionalImage1.add(imageMap);
                    }
                    if (!additionalImage1Orginal.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageOrginal", additionalImage1Orginal);
                        additionalImage1.add(imageMap);
                    }
                    if (!additionalImage1Small.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageSmall", additionalImage1Small);
                        additionalImage1.add(imageMap);
                    }


                    String additionalImage2Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_2_DETAIL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_2_DETAIL").toString();
                    String additionalImage2Dedium = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_2_MEDIUM").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_2_MEDIUM").toString();
                    String additionalImage2Large = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_2_LARGE").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_2_LARGE").toString();
                    String additionalImage2Orginal = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_2_ORIGINAL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_2_ORIGINAL").toString();
                    String additionalImage2Small = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_2_SMALL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_2_SMALL").toString();
                    if (UtilValidate.isEmpty(additionalImage2Dedium)) {
                        additionalImage1Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("ADDITIONAL_IMAGE_2").toString()) ? "" : baseImg + miniProductContentWrapper.get("ADDITIONAL_IMAGE_2").toString();
                        additionalImage2Detail = additionalImage1Detail;
                        additionalImage2Dedium = additionalImage1Detail;
                        additionalImage2Large = additionalImage1Detail;
                        additionalImage2Orginal = additionalImage1Detail;
                        additionalImage2Small = additionalImage1Detail;
                    }
                    List additionalImage2 = FastList.newInstance();
                    if (!additionalImage2Detail.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDetail", additionalImage2Detail);
                        additionalImage2.add(imageMap);
                    }
                    if (!additionalImage2Dedium.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDedium", additionalImage2Dedium);
                        additionalImage2.add(imageMap);
                    }
                    if (!additionalImage2Large.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageLarge", additionalImage2Large);
                        additionalImage2.add(imageMap);
                    }
                    if (!additionalImage2Orginal.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageOrginal", additionalImage2Orginal);
                        additionalImage2.add(imageMap);
                    }
                    if (!additionalImage2Small.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageSmall", additionalImage2Small);
                        additionalImage2.add(imageMap);
                    }


                    String additionalImage3Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_3_DETAIL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_3_DETAIL").toString();
                    String additionalImage3Dedium = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_3_MEDIUM").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_3_MEDIUM").toString();
                    String additionalImage3Large = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_3_LARGE").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_3_LARGE").toString();
                    String additionalImage3Orginal = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_3_ORIGINAL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_3_ORIGINAL").toString();
                    String additionalImage3Small = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_3_SMALL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_3_SMALL").toString();
                    if (UtilValidate.isEmpty(additionalImage3Dedium)) {
                        additionalImage1Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("ADDITIONAL_IMAGE_3").toString()) ? "" : baseImg + miniProductContentWrapper.get("ADDITIONAL_IMAGE_3").toString();
                        additionalImage3Detail = additionalImage1Detail;
                        additionalImage3Dedium = additionalImage1Detail;
                        additionalImage3Large = additionalImage1Detail;
                        additionalImage3Orginal = additionalImage1Detail;
                        additionalImage3Small = additionalImage1Detail;
                    }

                    List additionalImage3 = FastList.newInstance();
                    if (!additionalImage3Detail.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDetail", additionalImage3Detail);
                        additionalImage3.add(imageMap);
                    }
                    if (!additionalImage3Dedium.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDedium", additionalImage3Dedium);
                        additionalImage3.add(imageMap);
                    }
                    if (!additionalImage3Large.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageLarge", additionalImage3Large);
                        additionalImage3.add(imageMap);
                    }
                    if (!additionalImage3Orginal.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageOrginal", additionalImage3Orginal);
                        additionalImage3.add(imageMap);
                    }
                    if (!additionalImage3Small.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageSmall", additionalImage3Small);
                        additionalImage3.add(imageMap);
                    }

                    String additionalImage4Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_4_DETAIL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_4_DETAIL").toString();
                    String additionalImage4Dedium = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_4_MEDIUM").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_4_MEDIUM").toString();
                    String additionalImage4Large = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_4_LARGE").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_4_LARGE").toString();
                    String additionalImage4Orginal = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_4_ORIGINAL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_4_ORIGINAL").toString();
                    String additionalImage4Small = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_4_SMALL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_4_SMALL").toString();
                    if (UtilValidate.isEmpty(additionalImage4Dedium)) {
                        additionalImage1Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("ADDITIONAL_IMAGE_4").toString()) ? "" : baseImg + miniProductContentWrapper.get("ADDITIONAL_IMAGE_4").toString();
                        additionalImage4Detail = additionalImage1Detail;
                        additionalImage4Dedium = additionalImage1Detail;
                        additionalImage4Large = additionalImage1Detail;
                        additionalImage4Orginal = additionalImage1Detail;
                        additionalImage4Small = additionalImage1Detail;
                    }

                    List additionalImage4 = FastList.newInstance();
                    if (!additionalImage4Detail.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDetail", additionalImage4Detail);
                        additionalImage4.add(imageMap);
                    }
                    if (!additionalImage4Dedium.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDedium", additionalImage4Dedium);
                        additionalImage4.add(imageMap);
                    }
                    if (!additionalImage4Large.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageLarge", additionalImage4Large);
                        additionalImage4.add(imageMap);
                    }
                    if (!additionalImage4Orginal.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageOrginal", additionalImage4Orginal);
                        additionalImage4.add(imageMap);
                    }
                    if (!additionalImage4Small.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageSmall", additionalImage4Small);
                        additionalImage4.add(imageMap);
                    }


                    String additionalImage5Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_5_DETAIL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_5_DETAIL").toString();
                    String additionalImage5Dedium = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_5_MEDIUM").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_5_MEDIUM").toString();
                    String additionalImage5Large = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_5_LARGE").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_5_LARGE").toString();
                    String additionalImage5Orginal = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_5_ORIGINAL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_5_ORIGINAL").toString();
                    String additionalImage5Small = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_5_SMALL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_5_SMALL").toString();
                    if (UtilValidate.isEmpty(additionalImage5Dedium)) {
                        additionalImage1Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("ADDITIONAL_IMAGE_5").toString()) ? "" : baseImg + miniProductContentWrapper.get("ADDITIONAL_IMAGE_5").toString();
                        additionalImage5Detail = additionalImage1Detail;
                        additionalImage5Dedium = additionalImage1Detail;
                        additionalImage5Large = additionalImage1Detail;
                        additionalImage5Orginal = additionalImage1Detail;
                        additionalImage5Small = additionalImage1Detail;
                    }
                    List additionalImage5 = FastList.newInstance();
                    if (!additionalImage5Detail.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDetail", additionalImage5Detail);
                        additionalImage5.add(imageMap);
                    }
                    if (!additionalImage5Dedium.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDedium", additionalImage5Dedium);
                        additionalImage5.add(imageMap);
                    }
                    if (!additionalImage5Large.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageLarge", additionalImage5Large);
                        additionalImage5.add(imageMap);
                    }
                    if (!additionalImage5Orginal.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageOrginal", additionalImage5Orginal);
                        additionalImage5.add(imageMap);
                    }
                    if (!additionalImage5Small.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageSmall", additionalImage5Small);
                        additionalImage5.add(imageMap);
                    }

                    String additionalImage6Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_6_DETAIL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_6_DETAIL").toString();
                    String additionalImage6Dedium = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_6_MEDIUM").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_6_MEDIUM").toString();
                    String additionalImage6Large = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_6_LARGE").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_6_LARGE").toString();
                    String additionalImage6Orginal = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_6_ORIGINAL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_6_ORIGINAL").toString();
                    String additionalImage6Small = UtilValidate.isEmpty(miniProductContentWrapper.get("XTRA_IMG_6_SMALL").toString()) ? "" : baseImg + miniProductContentWrapper.get("XTRA_IMG_6_SMALL").toString();
                    if (UtilValidate.isEmpty(additionalImage6Dedium)) {
                        additionalImage1Detail = UtilValidate.isEmpty(miniProductContentWrapper.get("ADDITIONAL_IMAGE_6").toString()) ? "" : baseImg + miniProductContentWrapper.get("ADDITIONAL_IMAGE_6").toString();
                        additionalImage6Detail = additionalImage1Detail;
                        additionalImage6Dedium = additionalImage1Detail;
                        additionalImage6Large = additionalImage1Detail;
                        additionalImage6Orginal = additionalImage1Detail;
                        additionalImage6Small = additionalImage1Detail;
                    }
                    List additionalImage6 = FastList.newInstance();
                    if (!additionalImage6Detail.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageDetail", additionalImage6Detail);
                        additionalImage6.add(imageMap);
                    }
                    if (!additionalImage6Dedium.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImage6Dedium", additionalImage6Dedium);
                        additionalImage6.add(imageMap);
                    }
                    if (!additionalImage6Large.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageLarge", additionalImage6Large);
                        additionalImage6.add(imageMap);
                    }
                    if (!additionalImage6Orginal.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageOrginal", additionalImage6Orginal);
                        additionalImage6.add(imageMap);
                    }
                    if (!additionalImage6Small.equals("")) {
                        Map imageMap = new HashMap();
                        imageMap.put("additionalImageSmall", additionalImage6Small);
                        additionalImage6.add(imageMap);
                    }
                    Map<String, List> imgsMap = UtilMisc.toMap("additionalImage1", additionalImage1, "additionalImage2", additionalImage2, "additionalImage3", additionalImage3, "additionalImage4", additionalImage4, "additionalImage5", additionalImage5, "additionalImage6", additionalImage6);
                    restData.put("additionalImages", imgsMap);
                    restData.put("productCalculatedInfo", productCalculatedInfo==null?null:productCalculatedInfo.toMap());
                    restData.put("description", description);
                    restData.put("productId", productId);
                    restData.put("catalogId", prodCatalogId);
                    restData.put("productName", productName);
                    restData.put("isVirtual", isVirtual);
                    restData.put("isVariant", isVariant);
                    restData.put("productTypeId", miniProduct.get("productTypeId"));
                    restData.put("purchaseLimitationQuantity", miniProduct.get("purchaseLimitationQuantity"));
                    restData.put("wrapProductId", wrapProductId);
                    restData.put("introductionDate", miniProduct.get("introductionDate"));
                    restData.put("salesDiscontinuationDate", miniProduct.get("salesDiscontinuationDate"));
                    restData.put("subTitle", miniProduct.get("internalName"));
                    restData.put("weight", miniProduct.get("weight"));
                    restData.put("isBondedGoods", miniProduct.get("isBondedGoods"));//保税
                    restData.put("integralDeductionType", miniProduct.get("integralDeductionType"));//1.不可使用积分2:百分比抵扣3:固定金额抵扣
                    restData.put("integralDeductionUpper", miniProduct.get("integralDeductionUpper")); //积分抵扣数
                    restData.put("voucherAmount", miniProduct.get("voucherAmount"));//代金劵面值
                    restData.put("isInner", miniProduct.get("isInner"));//是否自营
                    restData.put("isSku", miniProduct.get("isVirtual"));

                    GenericValue store = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
                    restData.put("storeName", store.getString("storeName"));
                    retMap.put("resultData", restData);

                    //查找商品过期时间
                   /* if(expireTime==null){
                        UtilRedis.set(productId + "_detail", retMap);
                    }else{
                        long expireLongTime =expireTime.getTime()-System.currentTimeMillis();
                        if(expireLongTime>0L){
                            UtilRedis.set(productId + "_detail", retMap,expireLongTime);
                        }
                    }*/
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problems reading order header from datasource.", module);

            }
//        }

        return retMap;
    }


    private static void recursionCate(Delegator delegator, Set<String> productIds, String categoryId) throws GenericEntityException {
        List<GenericValue> categoryMembers = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap("productCategoryId", categoryId)));
        if (UtilValidate.isNotEmpty(categoryMembers)) {
            for (int j = 0; j < categoryMembers.size(); j++) {
                GenericValue member = categoryMembers.get(j);
                productIds.add((String) member.get("productId"));
            }
        }
        List<GenericValue> subMembers = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryRollup", UtilMisc.toMap("parentProductCategoryId", categoryId)));
        if (UtilValidate.isNotEmpty(subMembers)) {
            for (int j = 0; j < subMembers.size(); j++) {
                GenericValue categoryRollup = subMembers.get(j);
                String cateId = (String) categoryRollup.get("productCategoryId");
                recursionCate(delegator, productIds, cateId);
            }
        }
    }

    public static Map<String, Object> productCategoryGoods(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> retMap = ServiceUtil.returnSuccess();
        Map resultDataMap = FastMap.newInstance();
        String categoryId = (String) context.get("categoryId");
        String webSiteId = (String) context.get("webSiteId");
        String productStoreId = (String) context.get("productStoreId");
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher localDispatcher = dcx.getDispatcher();
        boolean hasNext = true;
        boolean hasPrev = true;
        int next = 1;
        int page = 1;
        int perPage = 10;
        int prev = 1;
        int pages = 1;
        int total = 1;

        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("page"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultDataMap.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("limit"));
        } catch (Exception e) {
            viewSize = 20;
        }
        page = viewIndex;
        perPage = viewSize;
        next = viewIndex + 1;
        if (viewIndex == 0) {
            prev = 0;
        } else {
            prev = viewIndex - 1;
        }

        resultDataMap.put("viewSize", Integer.valueOf(viewSize));
        Set<String> productIds = FastSet.newInstance();
        List resultData = FastList.newInstance();
        //根据catalog获取对应的分类根
        try {
            GenericValue catalog = EntityUtil.getFirst(EntityUtil.filterByDate(delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("productStoreId", productStoreId))));
            recursionCate(delegator, productIds, categoryId);
            total = productIds.size();
            pages = total % viewSize == 0 ? total / viewSize : total / viewSize + 1;
            if (UtilValidate.isNotEmpty(productIds)) {
                List<String> prodIds = FastList.newInstance();
                prodIds.addAll(productIds);
                lowIndex = viewIndex * viewSize;
                highIndex = (viewIndex + 1) * viewSize;
                if (highIndex > prodIds.size()) {
                    highIndex = prodIds.size();
                    hasNext = false;
                }
                if (lowIndex == 0) {
                    hasPrev = false;
                }


                prodIds = prodIds.subList(lowIndex, highIndex);
                String productStr = "";
                if (UtilValidate.isNotEmpty(prodIds)) {
                    for (int i = 0; i < prodIds.size(); i++) {
                        String productId = prodIds.get(i);
                        if (i < prodIds.size() - 1) {
                            productStr += productId + ",";
                        } else {
                            productStr += productId;
                        }
                    }
                }


                Map<String, Object> resultMap = localDispatcher.runSync("wx_productssummary", UtilMisc.toMap("productIds", productStr, "productStoreId", productStoreId, "webSiteId", webSiteId, "prodCatalogId", catalog.get("prodCatalogId")));
                if (ServiceUtil.isSuccess(resultMap)) {
                    resultData = (List) resultMap.get("resultData");
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        Map<String, Object> pMap = FastMap.newInstance();
        pMap.put("hasNext", hasNext);
        pMap.put("hasPrev", hasPrev);
        pMap.put("next", next);
        pMap.put("page", page);
        pMap.put("pages", pages);
        pMap.put("perPage", perPage);
        pMap.put("prev", prev);
        pMap.put("total", total);
        resultDataMap.put("paginate", pMap);
        resultDataMap.put("total", total);
        resultDataMap.put("highIndex", Integer.valueOf(highIndex));
        resultDataMap.put("lowIndex", Integer.valueOf(lowIndex));
        resultDataMap.put("products", resultData);

        retMap.put("resultData", resultDataMap);
        return retMap;
    }

    public static Map<String, Object> recommendProduct(DispatchContext dcx, Map<String, ? extends Object> context) {

        Delegator delegator = dcx.getDelegator();
        Set<String> productIds = FastSet.newInstance();
        Map<String, Object> retMap = ServiceUtil.returnSuccess();
        //找到店铺目录
        try {
            List<GenericValue> recommends = delegator.findByAndCache("WebSiteRecommendProduct", UtilMisc.toMap("webSiteId", "app"), UtilMisc.toList("sequenceNum"));
            List<GenericValue> recommendProducts = EntityUtil.getRelated("Product", recommends);

            retMap.put("resultData", recommendProducts);

        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }


        return retMap;

    }


    /**
     * Builds a variant feature tree.
     */
    public static Map<String, Object> getProductVariantSet(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String productId = (String) context.get("productId");
        //获取商品对应的SKU
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        List<GenericValue> features = null;
        try {
            features = delegator.findList("ProductFeatureAssoc", EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId), UtilMisc.toSet("productFeatureId"), null, findOpts, false);

            Set skuList = FastSet.newInstance();
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
                            //获取所有特征
                            List allFeatureList = FastList.newInstance();
                            Map allFeatureMap = FastMap.newInstance();
                            allFeatureMap.put("featureTypeId", typeId);
                            allFeatureMap.put("featureTypeName", typeName);
                            List<GenericValue> pfs = delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureTypeId", typeId));
                            if (UtilValidate.isNotEmpty(pfs)) {
                                for (int i = 0; i < pfs.size(); i++) {
                                    GenericValue pf = pfs.get(i);
                                    Map<String, String> featureMap = FastMap.newInstance();
                                    String featureName = pf.getString("productFeatureName");
                                    String featureId = pf.getString("productFeatureId");
                                    featureMap.put("featureName", featureName);
                                    featureMap.put("featureId", featureId);
                                    allFeatureList.add(featureMap);
                                }
                            }
                            allFeatureMap.put("features", allFeatureList);
                            skuList.add(allFeatureMap);


                        }
                        resultData.put("variantTreeChoose", typeMap);
                        resultData.put("variantTree", skuList);
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return resultData;
    }


}
