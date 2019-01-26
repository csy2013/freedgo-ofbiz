package com.yuaoq.yabiz.product.service;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.ofbiz.base.util.ArrayUtil;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by changsy on 2018/8/23.
 */
public class ProductFeatureServices {
    
    public static final String module = ProductFeatureServices.class.getName();
    public static final String resource = "ProductUiLabels";
    
    public static Map<String, Object> getProductCategoryFeatureList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> ret = ServiceUtil.returnSuccess();
        String productCategoryId = (String) context.get("productCategoryId");
        Delegator delegator = dctx.getDelegator();
        try {
            List<GenericValue> productFeatureCategoryAppl = EntityUtil.filterByDate(delegator.findByAnd("ProductFeatureCategoryAppl", UtilMisc.toMap("productCategoryId", productCategoryId), UtilMisc.toList("fromDate")));
            
            ret.put("productCategoryFeatures", EntityUtil.getRelated("ProductFeatureType", productFeatureCategoryAppl));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    
    public static Map<String, Object> getProductFeatureList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> ret = ServiceUtil.returnSuccess();
        
        Delegator delegator = dctx.getDelegator();
        try {
            List<GenericValue> featureTypes = delegator.findByAnd("ProductFeatureType", null, UtilMisc.toList("-productFeatureTypeId"));
            
            ret.put("productFeatueTypes", featureTypes);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    public static Map<String, Object> findFeaturesByFeatureTypeId(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> ret = ServiceUtil.returnSuccess();
        String featureTypeId = (String) context.get("featureTypeId");
        Delegator delegator = dctx.getDelegator();
        try {
            List<GenericValue> features = delegator.findByAnd("ProductFeatureAndDataSourceAppl", UtilMisc.toMap("productFeatureTypeId", featureTypeId), UtilMisc.toList("defaultSequenceNum"));
            
            ret.put("features", features);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return ret;
    }
    
    /**
     * variantFeatures: List<FeatureType
     * variantProducts : product，库存
     *
     * @param dctx
     * @param context
     * @return
     */
    
    public static Map<String, Object> getVariantProductsInfo(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> ret = ServiceUtil.returnSuccess();
        String productId = (String) context.get("productId");
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {
            List<GenericValue> variantProducts = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", productId,"isDel","N"));
            
            Map<String, Set<String>> variantFeatureIds = FastMap.newInstance();
            Set<String> variantFeatureTypes = FastSet.newInstance();
            Map<String, Set<String>> variantFeatureNames = FastMap.newInstance();
            List<Map> varaintProductInfo = FastList.newInstance();
//            skuGroup2Load('10003', '10007,10006,10008', '绿色,黄色,红色', '绿色,黄色,红色')
            if (UtilValidate.isNotEmpty(variantProducts)) {
                for (int i = 0; i < variantProducts.size(); i++) {
                    GenericValue variantProduct = variantProducts.get(i);
                    String variantProductId = (String) variantProduct.get("productId");
                    Map<String, Object> productInventory = dispatcher.runSync("getProductInventoryAvailable", UtilMisc.toMap("productId", variantProductId, "userLogin", userLogin));
                    if (ServiceUtil.isError(productInventory)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(productInventory));
                    }
                    Map<String, Object> productPrices = dispatcher.runSync("calculateProductPrice", UtilMisc.toMap("product", variantProduct, "userLogin", userLogin));
                    if (ServiceUtil.isError(productPrices)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(productInventory));
                    }
                    
                    Map variantProductMap = variantProduct.toMap();
                    variantProductMap.put("quantityOnHandTotal", productInventory.get("quantityOnHandTotal"));
                    variantProductMap.put("defaultPrice",productPrices.get("defaultPrice"));
                    variantProductMap.put("costPrice",productPrices.get("costPrice"));
                    varaintProductInfo.add(variantProductMap);
                    String featureProductId = variantProduct.getString("featureProductId");
                    String[] featureArray = featureProductId.split("\\|");
                    for (int j = 0; j < featureArray.length; j++) {
                        String featureId = featureArray[j];
                        GenericValue feature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", featureId));
                        String featureTypeId = feature.getString("productFeatureTypeId");
                        String featureName = feature.getString("productFeatureName");
                        if (UtilValidate.isNotEmpty(variantFeatureIds.get(featureTypeId))) {
                            Set featureIds = variantFeatureIds.get(featureTypeId);
                            featureIds.add(featureId);
                        } else {
                            Set featureIds = FastSet.newInstance();
                            featureIds.add(featureId);
                            variantFeatureIds.put(featureTypeId, featureIds);
                        }
                        
                        if (UtilValidate.isNotEmpty(variantFeatureNames.get(featureTypeId))) {
                            Set featureNames = variantFeatureNames.get(featureTypeId);
                            featureNames.add(featureName);
                        } else {
                            Set featureNames = FastSet.newInstance();
                            featureNames.add(featureName);
                            variantFeatureNames.put(featureTypeId, featureNames);
                        }
                        variantFeatureTypes.add(featureTypeId);
                        
                    }
                }
                
                Map<String, List<String>> allFeatureMaps = new HashMap<>();
                List<String> featureTypes = FastList.newInstance();
               
                Map<String, String> variantFeatureIdStrMap = FastMap.newInstance();
                
                Map<String, String> variantFeatureNamesMap = FastMap.newInstance();
                
                if (UtilValidate.isNotEmpty(variantFeatureTypes)) {
                    Iterator typeIter = variantFeatureTypes.iterator();
                    while (typeIter.hasNext()) {
                        String typeId = (String) typeIter.next();
                        List<GenericValue> features = delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureTypeId", typeId));
                        List<String> featureNames = FastList.newInstance();
                        if (UtilValidate.isNotEmpty(features)) {
                            for (int i = 0; i < features.size(); i++) {
                                GenericValue fear = features.get(i);
                                featureNames.add(fear.getString("productFeatureName"));
                            }
                            allFeatureMaps.put(typeId, featureNames);
                        }
                        featureTypes.add(typeId);
                        Set<String> ids = variantFeatureIds.get(typeId);
                        String featureIds = StringUtil.join(CollectionUtils.arrayToList(ids.toArray()),",");
                        Set<String> names = variantFeatureNames.get(typeId);
                        String featureNamArray = StringUtil.join(CollectionUtils.arrayToList(names.toArray()),",");
                        variantFeatureIdStrMap.put(typeId,featureIds);
                        variantFeatureNamesMap.put(typeId,featureNamArray);
                    }
                }
                ret.put("allFeatures", allFeatureMaps);
                ret.put("featureTypes", featureTypes);
                ret.put("variantFeatureNames", variantFeatureNamesMap);
                ret.put("variantFeatureIds", variantFeatureIdStrMap);
                ret.put("variantProducts", varaintProductInfo);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return ret;
    }
    
}
