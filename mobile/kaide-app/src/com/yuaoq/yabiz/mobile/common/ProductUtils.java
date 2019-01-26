package com.yuaoq.yabiz.mobile.common;

import com.yuaoq.yabiz.app.mobile.microservice.wish.api.v1.WishV1Controller;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericDataSourceException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class ProductUtils {
    public static final String module = ProductUtils.class.getName();

    /**
     * 得到商品特征
     *
     * @param productId
     * @param delegator
     * @return
     * @throws SQLException
     * @throws GenericEntityException
     */
    public static List<Map> getProductFeature(String productId, Delegator delegator) {
        String sql = "select pf.product_feature_name,pft.product_feature_type_name from product_feature_assoc pfa INNER JOIN product_feature pf on pfa.PRODUCT_FEATURE_ID = pf.PRODUCT_FEATURE_ID\n" +
                "INNER JOIN product_feature_type pft on pf.product_feature_type_id = pft.product_feature_type_id\n" +
                "where pfa.PRODUCT_ID='" + productId + "'";
        List<Map> featureList = FastList.newInstance();

        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            Map questionMap = null;
            while (rs.next()) {
                Map map = FastMap.newInstance();
                map.put("productFeatureTypeName", rs.getString("product_feature_type_name"));
                map.put("productFeatureName", rs.getString("product_feature_name"));
                featureList.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        } finally {
            try {
                sqlP.close();
            } catch (GenericDataSourceException e) {
                e.printStackTrace();
            }
        }

        return featureList;
    }

    /**
     * 得到所有不带商品特征的商品详情
     *
     * @param productIds
     * @param delegator
     * @param dispatcher
     * @return
     * @throws GenericServiceException
     */
    public static List<Map> getProductsWithOutFeature(List<String> productIds, Delegator delegator, LocalDispatcher dispatcher, String baseImgUrl) {
        List productList = FastList.newInstance();
        if (productIds == null || productIds.size() == 0) {
            return productList;
        }


        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("P", "Product");
        dynamicView.addAlias("P", "productId");
        dynamicView.addAlias("P", "productName");
        dynamicView.addAlias("P", "integralDeductionType");
        dynamicView.addAlias("P", "integralDeductionUpper");

        EntityCondition mainCond = EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition("productId", EntityOperator.IN, productIds)
                )
                , EntityOperator.AND);
        boolean beganTransaction =false;
        EntityListIterator pli=null;
        List<GenericValue> productResList=null;
        try{
            beganTransaction = TransactionUtil.begin();
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, null);
            productResList = pli.getCompleteList();

            GenericValue integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));//积分抵现规则表

            Long integralValue = integralPerMoney.getLong("integralValue");


            if (productResList != null && productResList.size() > 0) {
                for (GenericValue product : productResList) {
                    Map resMap = FastMap.newInstance();
                    //获取商品图片地址
                    String productId = product.getString("productId");
                    resMap.put("imgUrl", getProductImgUrl(productId, delegator, dispatcher, baseImgUrl));
                    //获取商品几种价格
                    List<GenericValue> priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
                    BigDecimal defaultPrice = null;
                    if (priceList != null && priceList.size() > 0) {
                        defaultPrice = priceList.get(0).getBigDecimal("price");
                    }
                    resMap.put("price", defaultPrice.doubleValue());

                    //市场价
                    List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE"));
                    double marketPrice = 0;
                    if (marketPriceList != null && marketPriceList.size() > 0) {
                        marketPrice = marketPriceList.get(0).getBigDecimal("price").doubleValue();
                    }
                    resMap.put("marketPrice", marketPrice);
                    resMap.put("productName", product.getString("productName"));
                    resMap.put("productId", product.getString("productId"));
                    GenericValue productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", productId));
                    int qty = 0;
                    if (productCalculatedInfo != null) {
                        qty = productCalculatedInfo.getBigDecimal("totalQuantityOrdered").intValue();
                    }
                    resMap.put("qty", qty);
                    //1.不可使用积分2:百分比抵扣3:固定金额抵扣
                    String integralDeductionType = product.getString("integralDeductionType");
                    BigDecimal integralDeductionUpper = product.getBigDecimal("integralDeductionUpper");
                    if (integralDeductionType != null && integralDeductionType.equalsIgnoreCase("2")) {
                        BigDecimal scorePrice = defaultPrice.multiply(integralDeductionUpper).divide(new BigDecimal(100));
                        BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
                        BigDecimal diffPrice = defaultPrice.subtract(scorePrice);
                        resMap.put("diffPrice", diffPrice);//价格+积分
                        resMap.put("scorePrice", scorePrice); //积分抵扣的金额
                        resMap.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
                    } else if (integralDeductionType != null && integralDeductionType.equalsIgnoreCase("3")) {
                        BigDecimal diffPrice = defaultPrice.subtract(integralDeductionUpper);
                        resMap.put("diffPrice", diffPrice);
                        resMap.put("scorePrice", integralDeductionUpper);//积分抵扣的金额
                        resMap.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
                    }

                    productList.add(resMap);
                }
            }

        }catch (Exception e){
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);

        }finally {
            if (pli != null) {
                try {
                    pli.close();
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

        return productList;
    }

    public static String getProductName(String productId, Delegator delegator) throws GenericEntityException {
        if(UtilValidate.isEmpty(productId)){
            return "";
        }
        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        if(product==null){
            return "";
        }
        return product.getString("productName");
    }

    public static Map getOneProductsWithOutFeature(String productId, Delegator delegator, LocalDispatcher dispatcher, String baseImgUrl) throws GeneralException, IOException {
        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));

        Map resMap = FastMap.newInstance();
        resMap.put("imgUrl", getProductImgUrl(productId, delegator, dispatcher, baseImgUrl));
        //获取商品几种价格
        boolean beganTransaction = TransactionUtil.begin();

        List<GenericValue> priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
        BigDecimal defaultPrice = null;
        if (priceList != null && priceList.size() > 0) {
            defaultPrice = priceList.get(0).getBigDecimal("price");
        }
        resMap.put("price", defaultPrice.doubleValue());

        //市场价
        List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE"));
        double marketPrice = 0;
        if (marketPriceList != null && marketPriceList.size() > 0) {
            marketPrice = marketPriceList.get(0).getBigDecimal("price").doubleValue();
        }
        resMap.put("marketPrice", marketPrice);
        resMap.put("productName", product.getString("productName"));
        resMap.put("productId", product.getString("productId"));
        //如果integralDeductionType等于2或者等于3，则代表可以通过积分购买
        resMap.put("integralDeductionType", product.getString("integralDeductionType"));
        resMap.put("description", product.getString("description"));

        //商品销售数量
        GenericValue productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", productId));
        int qty = 0;
        if (productCalculatedInfo != null) {
            qty = productCalculatedInfo.getBigDecimal("totalQuantityOrdered").intValue();
        }
        resMap.put("qty", qty);

        GenericValue integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));//积分抵现规则表
        Long integralValue = integralPerMoney.getLong("integralValue");

        //1.不可使用积分2:百分比抵扣3:固定金额抵扣
        String integralDeductionType = product.getString("integralDeductionType");
        BigDecimal integralDeductionUpper = product.getBigDecimal("integralDeductionUpper");
        if (integralDeductionType != null && integralDeductionType.equalsIgnoreCase("2")) {
            BigDecimal scorePrice = defaultPrice.multiply(integralDeductionUpper).divide(new BigDecimal(100));
            BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
            BigDecimal diffPrice = defaultPrice.subtract(scorePrice);
            resMap.put("diffPrice", diffPrice);//价格+积分
            resMap.put("scorePrice", scorePrice); //积分抵扣的金额
            resMap.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
        } else if (integralDeductionType != null && integralDeductionType.equalsIgnoreCase("3")) {
            BigDecimal diffPrice = defaultPrice.subtract(integralDeductionUpper);
            resMap.put("diffPrice", diffPrice);
            resMap.put("scorePrice", integralDeductionUpper);//积分抵扣的金额
            resMap.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
        }
        TransactionUtil.commit(beganTransaction);

        return resMap;
    }

    public static double getProductDefaultPrice(String productId, Delegator delegator, LocalDispatcher dispatcher) throws GenericEntityException {
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
        TransactionUtil.commit(beganTransaction);

        BigDecimal defaultPrice = new BigDecimal(0);
        if (priceList != null && priceList.size() > 0) {
            defaultPrice = priceList.get(0).getBigDecimal("price");
        }
        return defaultPrice.doubleValue();
    }

    public static Map getOneTogetherProductsWithOutFeature(String productId, double price, Delegator delegator, LocalDispatcher dispatcher, String baseImgUrl) throws GeneralException, IOException {
        boolean beganTransaction = TransactionUtil.begin();
        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));

        Map resMap = FastMap.newInstance();
        resMap.put("imgUrl", getProductImgUrl(productId, delegator, dispatcher, baseImgUrl));
        //获取商品几种价格
        List<GenericValue> priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
        BigDecimal defaultPrice = new BigDecimal(price);
        resMap.put("price", defaultPrice.doubleValue());

        //市场价
        List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE"));
        double marketPrice = 0;
        if (marketPriceList != null && marketPriceList.size() > 0) {
            marketPrice = marketPriceList.get(0).getBigDecimal("price").doubleValue();
        }
        resMap.put("marketPrice", marketPrice);
        resMap.put("productName", product.getString("productName"));
        resMap.put("productId", product.getString("productId"));
        //如果integralDeductionType等于2或者等于3，则代表可以通过积分购买
        resMap.put("integralDeductionType", product.getString("integralDeductionType"));
        resMap.put("description", product.getString("description"));


        GenericValue integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));//积分抵现规则表
        Long integralValue = integralPerMoney.getLong("integralValue");

        //1.不可使用积分2:百分比抵扣3:固定金额抵扣
        String integralDeductionType = product.getString("integralDeductionType");
        BigDecimal integralDeductionUpper = product.getBigDecimal("integralDeductionUpper");
        if (integralDeductionType.equalsIgnoreCase("2")) {
            BigDecimal scorePrice = defaultPrice.multiply(integralDeductionUpper).divide(new BigDecimal(100));
            BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
            BigDecimal diffPrice = defaultPrice.subtract(scorePrice);
            resMap.put("diffPrice", diffPrice);//价格+积分
            resMap.put("scorePrice", scorePrice); //积分抵扣的金额
            resMap.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
        } else if (integralDeductionType.equalsIgnoreCase("3")) {
            BigDecimal diffPrice = defaultPrice.subtract(integralDeductionUpper);
            resMap.put("diffPrice", diffPrice);
            resMap.put("scorePrice", integralDeductionUpper);//积分抵扣的金额
            resMap.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
        }
        TransactionUtil.commit(beganTransaction);

        return resMap;
    }


    public static String getProductImgUrl(String productId, Delegator delegator, LocalDispatcher dispatcher, String baseImgUrl) throws GeneralException, IOException {
        boolean beganTransaction = TransactionUtil.begin();

        List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
        String imgUrl = "";
        if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
            String contentId = (String) curProductAdditionalImage1.get(0).get("contentId");
            if (UtilValidate.isNotEmpty(contentId)) {
                imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
            }
        }
        TransactionUtil.commit(beganTransaction);
        return imgUrl;
    }

    public static BigDecimal getProductActivityPrice(String productId,LocalDispatcher dispatcher) throws GenericServiceException {

        Map promoInfo = dispatcher.runSync("getProductPromoInfoByProductId",UtilMisc.toMap("productId",productId));
        BigDecimal activityPrice = BigDecimal.ZERO;
        Map<String, Object> orderGroupInfos = (Map<String, Object>) promoInfo.get("orderGroupInfo");
        Map<String, Object> secKillInfo = (Map<String, Object>) promoInfo.get("secKillInfo");
        Map<String, Object> priceDownInfo = (Map<String, Object>) promoInfo.get("priceDownInfo");
        if (UtilValidate.isNotEmpty(orderGroupInfos)&&orderGroupInfos.size() > 0){
            activityPrice = ((BigDecimal) orderGroupInfos.get("activityPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
        }else if (UtilValidate.isNotEmpty(secKillInfo)&&secKillInfo.size() > 0) {
            activityPrice = ((BigDecimal) secKillInfo.get("activityPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else if (UtilValidate.isNotEmpty(priceDownInfo)&&priceDownInfo.size() > 0) {
            activityPrice = ((BigDecimal) priceDownInfo.get("amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else if (UtilValidate.isEmpty(orderGroupInfos)&&UtilValidate.isEmpty(secKillInfo)&&UtilValidate.isEmpty(priceDownInfo) ) {
        }
        return activityPrice;
    }

}
