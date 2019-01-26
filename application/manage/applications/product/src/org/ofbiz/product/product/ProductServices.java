/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.product.product;

import com.google.common.base.Joiner;
import com.qihua.ofbiz.excel.ExcelExport;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.jdom.JDOMException;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.category.CategoryWorker;
import org.ofbiz.product.config.ProductConfigWorker;
import org.ofbiz.product.config.ProductConfigWrapper;
import org.ofbiz.product.image.ScaleImage;
import org.ofbiz.security.Security;
import org.ofbiz.service.*;
import org.ofbiz.service.calendar.RecurrenceRule;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Product Services
 */
public class ProductServices {

    public static final String module = ProductServices.class.getName();
    public static final String resource = "ProductUiLabels";
    public static final String resourceError = "ProductErrorUiLabels";
    public static final MathContext generalRounding = new MathContext(10);

    /**
     * Creates a Collection of product entities which are variant products from the specified product ID.
     */
    public static Map<String, Object> prodFindAllVariants(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Parent (virtual) product ID
        Map<String, Object> subContext = UtilMisc.makeMapWritable(context);
        subContext.put("type", "PRODUCT_VARIANT");
        return prodFindAssociatedByType(dctx, subContext);
    }

    /**
     * Finds a specific product or products which contain the selected features.
     */
    public static Map<String, Object> prodFindSelectedVariant(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Parent (virtual) product ID
        // * Map selectedFeatures  -- Selected features
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        Map<String, String> selectedFeatures = UtilGenerics.checkMap(context.get("selectedFeatures"));
        List<GenericValue> products = FastList.newInstance();
        // All the variants for this products are retrieved
        Map<String, Object> resVariants = prodFindAllVariants(dctx, context);
        List<GenericValue> variants = UtilGenerics.checkList(resVariants.get("assocProducts"));
        for (GenericValue oneVariant : variants) {
            // For every variant, all the standard features are retrieved
            Map<String, String> feaContext = FastMap.newInstance();
            feaContext.put("productId", oneVariant.getString("productIdTo"));
            feaContext.put("type", "STANDARD_FEATURE");
            Map<String, Object> resFeatures = prodGetFeatures(dctx, feaContext);
            List<GenericValue> features = UtilGenerics.checkList(resFeatures.get("productFeatures"));
            boolean variantFound = true;
            // The variant is discarded if at least one of its standard features
            // has the same type of one of the selected features but a different feature id.
            // Example:
            // Input: (COLOR, Black), (SIZE, Small)
            // Variant1: (COLOR, Black), (SIZE, Large) --> nok
            // Variant2: (COLOR, Black), (SIZE, Small) --> ok
            // Variant3: (COLOR, Black), (SIZE, Small), (IMAGE, SkyLine) --> ok
            // Variant4: (COLOR, Black), (IMAGE, SkyLine) --> ok
            for (GenericValue oneFeature : features) {
                if (selectedFeatures.containsKey(oneFeature.getString("productFeatureTypeId"))) {
                    if (!selectedFeatures.containsValue(oneFeature.getString("productFeatureId"))) {
                        variantFound = false;
                        break;
                    }
                }
            }
            if (variantFound) {
                try {
                    products.add(delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", oneVariant.getString("productIdTo"))));
                } catch (GenericEntityException e) {
                    Map<String, String> messageMap = UtilMisc.toMap("errProductFeatures", e.toString());
                    String errMsg = UtilProperties.getMessage(resourceError, "productservices.problem_reading_product_features_errors", messageMap, locale);
                    Debug.logError(e, errMsg, module);
                    return ServiceUtil.returnError(errMsg);
                }
            }
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("products", products);
        return result;
    }

    /**
     * Finds product variants based on a product ID and a distinct feature.
     */
    public static Map<String, Object> prodFindDistinctVariants(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Parent (virtual) product ID
        // * String feature        -- Distinct feature name
        //TODO This service has not yet been implemented.
        return ServiceUtil.returnFailure();
    }

    /**
     * Finds a Set of feature types in sequence.
     */
    public static Map<String, Object> prodFindFeatureTypes(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Product ID to look up feature types
        Delegator delegator = dctx.getDelegator();
        String productId = (String) context.get("productId");
        String productFeatureApplTypeId = (String) context.get("productFeatureApplTypeId");
        if (UtilValidate.isEmpty(productFeatureApplTypeId)) {
            productFeatureApplTypeId = "SELECTABLE_FEATURE";
        }
        Locale locale = (Locale) context.get("locale");
        String errMsg = null;
        Set<String> featureSet = new LinkedHashSet<String>();

        try {
            Map<String, String> fields = UtilMisc.toMap("productId", productId, "productFeatureApplTypeId", productFeatureApplTypeId);
            List<String> order = UtilMisc.toList("sequenceNum", "productFeatureTypeId");
            List<GenericValue> features = delegator.findByAndCache("ProductFeatureAndAppl", fields, order);
            for (GenericValue v : features) {
                featureSet.add(v.getString("productFeatureTypeId"));
            }
            //if (Debug.infoOn()) Debug.logInfo("" + featureSet, module);
        } catch (GenericEntityException e) {
            Map<String, String> messageMap = UtilMisc.toMap("errProductFeatures", e.toString());
            errMsg = UtilProperties.getMessage(resourceError, "productservices.problem_reading_product_features_errors", messageMap, locale);
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }

        if (featureSet.size() == 0) {
            errMsg = UtilProperties.getMessage(resourceError, "productservices.problem_reading_product_features", locale);
            // ToDo DO 2004-02-23 Where should the errMsg go?
            Debug.logWarning(errMsg + " for product " + productId, module);
            //return ServiceUtil.returnError(errMsg);
        }
        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("featureSet", featureSet);
        return result;
    }

    /**
     * Builds a variant feature tree.
     */
    public static Map<String, Object> prodMakeFeatureTree(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Parent (virtual) product ID
        // * List featureOrder     -- Order of features
        // * Boolean checkInventory-- To calculate available inventory.
        // * String productStoreId -- Product Store ID for Inventory
        String productStoreId = (String) context.get("productStoreId");
        Locale locale = (Locale) context.get("locale");

        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = FastMap.newInstance();
        List<String> featureOrder = UtilMisc.makeListWritable(UtilGenerics.<String>checkCollection(context.get("featureOrder")));

        if (UtilValidate.isEmpty(featureOrder)) {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "ProductFeatureTreeCannotFindFeaturesList", locale));
        }

        List<GenericValue> variants = UtilGenerics.checkList(prodFindAllVariants(dctx, context).get("assocProducts"));
        List<String> virtualVariant = FastList.newInstance();

        if (UtilValidate.isEmpty(variants)) {
            return ServiceUtil.returnSuccess();
        }
        List<String> items = FastList.newInstance();
        List<GenericValue> outOfStockItems = FastList.newInstance();

        for (GenericValue variant : variants) {
            String productIdTo = variant.getString("productIdTo");

            // first check to see if intro and discontinue dates are within range
            GenericValue productTo = null;

            try {
                productTo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productIdTo));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                Map<String, String> messageMap = UtilMisc.toMap("productIdTo", productIdTo, "errMessage", e.toString());
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError,
                        "productservices.error_finding_associated_variant_with_ID_error", messageMap, locale));
            }
            if (productTo == null) {
                Debug.logWarning("Could not find associated variant with ID " + productIdTo + ", not showing in list", module);
                continue;
            }

            java.sql.Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

            // check to see if introductionDate hasn't passed yet
            if (productTo.get("introductionDate") != null && nowTimestamp.before(productTo.getTimestamp("introductionDate"))) {
                if (Debug.verboseOn()) {
                    String excMsg = "Tried to view the Product " + productTo.getString("productName") +
                            " (productId: " + productTo.getString("productId") + ") as a variant. This product has not yet been made available for sale, so not adding for view.";

                    Debug.logVerbose(excMsg, module);
                }
                continue;
            }

            // check to see if salesDiscontinuationDate has passed
            if (productTo.get("salesDiscontinuationDate") != null && nowTimestamp.after(productTo.getTimestamp("salesDiscontinuationDate"))) {
                if (Debug.verboseOn()) {
                    String excMsg = "Tried to view the Product " + productTo.getString("productName") +
                            " (productId: " + productTo.getString("productId") + ") as a variant. This product is no longer available for sale, so not adding for view.";

                    Debug.logVerbose(excMsg, module);
                }
                continue;
            }

            // next check inventory for each item: if inventory is not required or is available
            Boolean checkInventory = (Boolean) context.get("checkInventory");
            try {
                if (checkInventory) {
                    Map<String, Object> invReqResult = dispatcher.runSync("isStoreInventoryAvailableOrNotRequired", UtilMisc.<String, Object>toMap("productStoreId", productStoreId, "productId", productIdTo, "quantity", BigDecimal.ONE));
                    if (ServiceUtil.isError(invReqResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                "ProductFeatureTreeCannotCallIsStoreInventoryRequired", locale), null, null, invReqResult);
                    } else if ("Y".equals(invReqResult.get("availableOrNotRequired"))) {
                        items.add(productIdTo);
                        if (productTo.getString("isVirtual") != null && "Y".equals(productTo.getString("isVirtual"))) {
                            virtualVariant.add(productIdTo);
                        }
                    } else {
                        outOfStockItems.add(productTo);
                    }
                } else {
                    items.add(productIdTo);
                    if (productTo.getString("isVirtual") != null && "Y".equals(productTo.getString("isVirtual"))) {
                        virtualVariant.add(productIdTo);
                    }
                }
            } catch (GenericServiceException e) {
                Debug.logError(e, "Error calling the isStoreInventoryRequired when building the variant product tree: " + e.toString(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductFeatureTreeCannotCallIsStoreInventoryRequired", locale));
            }
        }

        String productId = (String) context.get("productId");

        // Make the selectable feature list
        List<GenericValue> selectableFeatures = null;
        try {
            Map<String, String> fields = UtilMisc.toMap("productId", productId, "productFeatureApplTypeId", "SELECTABLE_FEATURE");
            List<String> sort = UtilMisc.toList("sequenceNum");

            selectableFeatures = delegator.findByAndCache("ProductFeatureAndAppl", fields, sort);
            selectableFeatures = EntityUtil.filterByDate(selectableFeatures, true);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resourceError,
                    "productservices.empty_list_of_selectable_features_found", locale));
        }
        Map<String, List<String>> features = FastMap.newInstance();
        for (GenericValue v : selectableFeatures) {
            String featureType = v.getString("productFeatureTypeId");
            String feature = v.getString("description");

            if (!features.containsKey(featureType)) {
                List<String> featureList = FastList.newInstance();
                featureList.add(feature);
                features.put(featureType, featureList);
            } else {
                List<String> featureList = features.get(featureType);
                featureList.add(feature);
                features.put(featureType, featureList);
            }
        }

        Map<String, Object> tree = null;
        try {
            tree = makeGroup(delegator, features, items, featureOrder, 0);
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        if (UtilValidate.isEmpty(tree)) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage(resourceError,
                    "productservices.feature_grouping_came_back_empty", locale));
        } else {
            result.put("variantTree", tree);
            result.put("virtualVariant", virtualVariant);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        }

        //add by changsy 2015-9-29 增加变形选择

        Map<String, List<GenericValue>> treeMap = null;
        try {
            treeMap = makeGroupMap(delegator, items);
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        if (UtilValidate.isEmpty(treeMap)) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, UtilProperties.getMessage(resourceError,
                    "productservices.feature_grouping_came_back_empty", locale));
        } else {
            result.put("variantTreeChoose", treeMap);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        }


        //add by changsy 2015-9-29 增加变形选择

        Map<String, GenericValue> sample = null;
        try {
            sample = makeVariantSample(dctx.getDelegator(), features, items, featureOrder.get(0));
        } catch (Exception e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        if (outOfStockItems.size() > 0) {
            result.put("unavailableVariants", outOfStockItems);
        }
        result.put("variantSample", sample);
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);

        return result;
    }


    /**
     * Builds a variant feature tree.
     */
    public static Map<String, Object> findVariantProductByFeatureList(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Parent (virtual) product ID
        // * Boolean checkInventory-- To calculate available inventory.
        // * String productStoreId -- Product Store ID for Inventory
        String productStoreId = (String) context.get("productStoreId");
        Locale locale = (Locale) context.get("locale");
        String productId = (String) context.get("productId");
        Map<String, String> selectFeatures = (Map<String, String>) context.get("selectFeatures");

        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Map<String, Object> result = FastMap.newInstance();
        //根据虚拟产品查询出对应的变形产品
        List<GenericValue> variants = UtilGenerics.checkList(prodFindAllVariants(dctx, context).get("assocProducts"));
        List<String> virtualVariant = FastList.newInstance();

        if (UtilValidate.isEmpty(variants)) {
            return ServiceUtil.returnSuccess();
        }
        List<String> items = FastList.newInstance();
        List<GenericValue> outOfStockItems = FastList.newInstance();


        //查找每个变形产品是否有效。
        for (GenericValue variant : variants) {
            String productIdTo = variant.getString("productIdTo");

            // first check to see if intro and discontinue dates are within range
            GenericValue productTo = null;

            try {
                productTo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productIdTo));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                Map<String, String> messageMap = UtilMisc.toMap("productIdTo", productIdTo, "errMessage", e.toString());
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError,
                        "productservices.error_finding_associated_variant_with_ID_error", messageMap, locale));
            }
            if (productTo == null) {
                Debug.logWarning("Could not find associated variant with ID " + productIdTo + ", not showing in list", module);
                continue;
            }

            java.sql.Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

            // check to see if introductionDate hasn't passed yet
            if (productTo.get("introductionDate") != null && nowTimestamp.before(productTo.getTimestamp("introductionDate"))) {
                if (Debug.verboseOn()) {
                    String excMsg = "Tried to view the Product " + productTo.getString("productName") +
                            " (productId: " + productTo.getString("productId") + ") as a variant. This product has not yet been made available for sale, so not adding for view.";

                    Debug.logVerbose(excMsg, module);
                }
                continue;
            }

            // check to see if salesDiscontinuationDate has passed
            if (productTo.get("salesDiscontinuationDate") != null && nowTimestamp.after(productTo.getTimestamp("salesDiscontinuationDate"))) {
                if (Debug.verboseOn()) {
                    String excMsg = "Tried to view the Product " + productTo.getString("productName") +
                            " (productId: " + productTo.getString("productId") + ") as a variant. This product is no longer available for sale, so not adding for view.";

                    Debug.logVerbose(excMsg, module);
                }
                continue;
            }

            // next check inventory for each item: if inventory is not required or is available
            Boolean checkInventory = (Boolean) context.get("checkInventory");
            try {
                if (checkInventory) {
                    Map<String, Object> invReqResult = dispatcher.runSync("isStoreInventoryAvailableOrNotRequired", UtilMisc.<String, Object>toMap("productStoreId", productStoreId, "productId", productIdTo, "quantity", BigDecimal.ONE));
                    if (ServiceUtil.isError(invReqResult)) {
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                "ProductFeatureTreeCannotCallIsStoreInventoryRequired", locale), null, null, invReqResult);
                    } else if ("Y".equals(invReqResult.get("availableOrNotRequired"))) {
                        items.add(productIdTo);
                        if (productTo.getString("isVirtual") != null && "Y".equals(productTo.getString("isVirtual"))) {
                            virtualVariant.add(productIdTo);
                        }
                    } else {
                        outOfStockItems.add(productTo);
                    }
                } else {
                    items.add(productIdTo);
                    if (productTo.getString("isVirtual") != null && "Y".equals(productTo.getString("isVirtual"))) {
                        virtualVariant.add(productIdTo);
                    }
                }
            } catch (GenericServiceException e) {
                Debug.logError(e, "Error calling the isStoreInventoryRequired when building the variant product tree: " + e.toString(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductFeatureTreeCannotCallIsStoreInventoryRequired", locale));
            }
        }
        //根据变形产品查询出来对应的featureTypeId
        if (!items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                String asscoProductId = items.get(i);
                List<GenericValue> features = null;

                try {
                    Map<String, String> fields = UtilMisc.toMap("productId", asscoProductId, "productFeatureApplTypeId", "STANDARD_FEATURE");
                    List<String> sort = UtilMisc.toList("sequenceNum");

                    // get the features and filter out expired dates
                    features = delegator.findByAndCache("ProductFeatureAndAppl", fields, sort);
                    features = EntityUtil.filterByDate(features, true);
                    boolean correct = true;
                    if (!features.isEmpty()) {
                        for (int j = 0; j < features.size(); j++) {
                            GenericValue feature = features.get(j);
                            if (selectFeatures.containsKey(feature.get("productFeatureTypeId"))) {
                                if (!selectFeatures.get(feature.get("productFeatureTypeId")).equals(feature.get("productFeatureId"))) {
                                    correct = false;
                                }
                            } else {
                                correct = false;
                            }
                        }
                    }

                    if (correct) {
                        GenericValue genericValue = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", asscoProductId));
                        result.put("variantProduct", genericValue);
                        break;
                    }

                } catch (GenericEntityException e) {
                    throw new IllegalStateException("Problem reading relation: " + e.getMessage());
                }

            }
        }


        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);

        return result;
    }

    /**
     * Gets the product features of a product.
     */
    public static Map<String, Object> prodGetFeatures(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Product ID to find
        // * String type           -- Type of feature (STANDARD_FEATURE, SELECTABLE_FEATURE)
        // * String distinct       -- Distinct feature (SIZE, COLOR)
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        String productId = (String) context.get("productId");
        String distinct = (String) context.get("distinct");
        String type = (String) context.get("type");
        Locale locale = (Locale) context.get("locale");
        String errMsg = null;
        List<GenericValue> features = null;

        try {
            Map<String, String> fields = UtilMisc.toMap("productId", productId);
            List<String> order = UtilMisc.toList("sequenceNum", "productFeatureTypeId");

            if (distinct != null) {
                fields.put("productFeatureTypeId", distinct);
            }
            if (type != null) {
                fields.put("productFeatureApplTypeId", type);
            }
            features = delegator.findByAndCache("ProductFeatureAndAppl", fields, order);
            result.put("productFeatures", features);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        } catch (GenericEntityException e) {
            Map<String, String> messageMap = UtilMisc.toMap("errMessage", e.toString());
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.problem_reading_product_feature_entity", messageMap, locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
        }
        return result;
    }

    /**
     * Finds a product by product ID.
     */
    public static Map<String, Object> prodFindProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Product ID to find
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        String productId = (String) context.get("productId");
        Locale locale = (Locale) context.get("locale");
        String errMsg = null;

        if (UtilValidate.isEmpty(productId)) {
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.invalid_productId_passed", locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }

        try {
            GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
            GenericValue mainProduct = product;

            if (product.get("isVariant") != null && "Y".equalsIgnoreCase(product.getString("isVariant"))) {
                List<GenericValue> c = product.getRelatedByAndCache("AssocProductAssoc",
                        UtilMisc.toMap("productAssocTypeId", "PRODUCT_VARIANT"));
                //if (Debug.infoOn()) Debug.logInfo("Found related: " + c, module);
                c = EntityUtil.filterByDate(c);
                //if (Debug.infoOn()) Debug.logInfo("Found Filtered related: " + c, module);
                if (c.size() > 0) {
                    GenericValue asV = c.iterator().next();

                    //if (Debug.infoOn()) Debug.logInfo("ASV: " + asV, module);
                    mainProduct = asV.getRelatedOneCache("MainProduct");
                    //if (Debug.infoOn()) Debug.logInfo("Main product = " + mainProduct, module);
                }
            }
            result.put("product", mainProduct);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            Map<String, String> messageMap = UtilMisc.toMap("errMessage", e.getMessage());
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.problems_reading_product_entity", messageMap, locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
        }

        return result;
    }

    /**
     * Finds associated products by product ID and association ID.
     */
    public static Map<String, Object> prodFindAssociatedByType(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Current Product ID
        // * String type           -- Type of association (ie PRODUCT_UPGRADE, PRODUCT_COMPLEMENT, PRODUCT_VARIANT)
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        String productId = (String) context.get("productId");
        String productIdTo = (String) context.get("productIdTo");
        String type = (String) context.get("type");
        Locale locale = (Locale) context.get("locale");
        String errMsg = null;

        Boolean cvaBool = (Boolean) context.get("checkViewAllow");
        boolean checkViewAllow = (cvaBool == null ? false : cvaBool);
        String prodCatalogId = (String) context.get("prodCatalogId");
        Boolean bidirectional = (Boolean) context.get("bidirectional");
        bidirectional = bidirectional == null ? false : bidirectional;
        Boolean sortDescending = (Boolean) context.get("sortDescending");
        sortDescending = sortDescending == null ? false : sortDescending;

        if (productId == null && productIdTo == null) {
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.both_productId_and_productIdTo_cannot_be_null", locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }

        if (productId != null && productIdTo != null) {
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.both_productId_and_productIdTo_cannot_be_defined", locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }

        productId = productId == null ? productIdTo : productId;
        GenericValue product = null;

        try {
            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Map<String, String> messageMap = UtilMisc.toMap("errMessage", e.getMessage());
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.problems_reading_product_entity", messageMap, locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }

        if (product == null) {
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.problems_getting_product_entity", locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }

        try {
            List<GenericValue> productAssocs = null;

            List<String> orderBy = FastList.newInstance();
            if (sortDescending) {
                orderBy.add("sequenceNum DESC");
            } else {
                orderBy.add("sequenceNum");
            }

            if (bidirectional) {
                EntityCondition cond = EntityCondition.makeCondition(
                        UtilMisc.toList(
                                EntityCondition.makeCondition("productId", productId),
                                EntityCondition.makeCondition("productIdTo", productId)
                        ), EntityJoinOperator.OR);
                cond = EntityCondition.makeCondition(cond, EntityCondition.makeCondition("productAssocTypeId", type));
                productAssocs = delegator.findList("ProductAssoc", cond, null, orderBy, null, true);
            } else {
                if (productIdTo == null) {
                    productAssocs = product.getRelatedCache("MainProductAssoc", UtilMisc.toMap("productAssocTypeId", type), orderBy);
                } else {
                    productAssocs = product.getRelatedCache("AssocProductAssoc", UtilMisc.toMap("productAssocTypeId", type), orderBy);
                }
            }
            // filter the list by date
            productAssocs = EntityUtil.filterByDate(productAssocs);
            // first check to see if there is a view allow category and if these products are in it...
            if (checkViewAllow && prodCatalogId != null && UtilValidate.isNotEmpty(productAssocs)) {
                String viewProductCategoryId = CatalogWorker.getCatalogViewAllowCategoryId(delegator, prodCatalogId);
                if (viewProductCategoryId != null) {
                    if (productIdTo == null) {
                        productAssocs = CategoryWorker.filterProductsInCategory(delegator, productAssocs, viewProductCategoryId, "productIdTo");
                    } else {
                        productAssocs = CategoryWorker.filterProductsInCategory(delegator, productAssocs, viewProductCategoryId, "productId");
                    }
                }
            }


            result.put("assocProducts", productAssocs);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        } catch (GenericEntityException e) {
            Map<String, String> messageMap = UtilMisc.toMap("errMessage", e.getMessage());
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.problems_product_association_relation_error", messageMap, locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }

        return result;
    }

    // Builds a product feature tree
    private static Map<String, Object> makeGroup(Delegator delegator, Map<String, List<String>> featureList, List<String> items, List<String> order, int index)
            throws IllegalArgumentException, IllegalStateException {
        //List featureKey = FastList.newInstance();
        Map<String, List<String>> tempGroup = FastMap.newInstance();
        Map<String, Object> group = new LinkedHashMap<String, Object>();
        String orderKey = order.get(index);

        if (featureList == null) {
            throw new IllegalArgumentException("Cannot build feature tree: featureList is null");
        }

        if (index < 0) {
            throw new IllegalArgumentException("Invalid index '" + index + "' min index '0'");
        }
        if (index + 1 > order.size()) {
            throw new IllegalArgumentException("Invalid index '" + index + "' max index '" + (order.size() - 1) + "'");
        }

        // loop through items and make the lists
        for (String thisItem : items) {
            // -------------------------------
            // Gather the necessary data
            // -------------------------------

            if (Debug.verboseOn()) {
                Debug.logVerbose("ThisItem: " + thisItem, module);
            }
            List<GenericValue> features = null;

            try {
                Map<String, String> fields = UtilMisc.toMap("productId", thisItem, "productFeatureTypeId", orderKey,
                        "productFeatureApplTypeId", "STANDARD_FEATURE");
                List<String> sort = UtilMisc.toList("sequenceNum");

                // get the features and filter out expired dates
                features = delegator.findByAndCache("ProductFeatureAndAppl", fields, sort);
                features = EntityUtil.filterByDate(features, true);
            } catch (GenericEntityException e) {
                throw new IllegalStateException("Problem reading relation: " + e.getMessage());
            }
            if (Debug.verboseOn()) {
                Debug.logVerbose("Features: " + features, module);
            }

            // -------------------------------
            for (GenericValue item : features) {
                String itemKey = item.getString("description");

                if (tempGroup.containsKey(itemKey)) {
                    List<String> itemList = tempGroup.get(itemKey);

                    if (!itemList.contains(thisItem)) {
                        itemList.add(thisItem);
                    }
                } else {
                    List<String> itemList = UtilMisc.toList(thisItem);

                    tempGroup.put(itemKey, itemList);
                }
            }
        }
        if (Debug.verboseOn()) {
            Debug.logVerbose("TempGroup: " + tempGroup, module);
        }

        // Loop through the feature list and order the keys in the tempGroup
        List<String> orderFeatureList = featureList.get(orderKey);

        if (orderFeatureList == null) {
            throw new IllegalArgumentException("Cannot build feature tree: orderFeatureList is null for orderKey=" + orderKey);
        }

        for (String featureStr : orderFeatureList) {
            if (tempGroup.containsKey(featureStr)) {
                group.put(featureStr, tempGroup.get(featureStr));
            }
        }

        if (Debug.verboseOn()) {
            Debug.logVerbose("Group: " + group, module);
        }

        // no groups; no tree
        if (group.size() == 0) {
            return group;
            //throw new IllegalStateException("Cannot create tree from group list; error on '" + orderKey + "'");
        }

        if (index + 1 == order.size()) {
            return group;
        }

        // loop through the keysets and get the sub-groups
        for (String key : group.keySet()) {
            List<String> itemList = UtilGenerics.checkList(group.get(key));

            if (UtilValidate.isNotEmpty(itemList)) {
                Map<String, Object> subGroup = makeGroup(delegator, featureList, itemList, order, index + 1);
                group.put(key, subGroup);
            } else {
                // do nothing, ie put nothing in the Map
                //throw new IllegalStateException("Cannot create tree from an empty list; error on '" + key + "'");
            }
        }
        return group;
    }


    // 变形产品属性
    private static Map<String, List<GenericValue>> makeGroupMap(Delegator delegator, List<String> items)
            throws IllegalArgumentException, IllegalStateException {
        //List featureKey = FastList.newInstance();
        Map<String, List<GenericValue>> group = new LinkedHashMap<String, List<GenericValue>>();

        // loop through items and make the lists
        for (String thisItem : items) {
            // -------------------------------
            // Gather the necessary data
            // -------------------------------

            List<GenericValue> features = null;

            try {
                Map<String, String> fields = UtilMisc.toMap("productId", thisItem,
                        "productFeatureApplTypeId", "STANDARD_FEATURE");
                List<String> sort = UtilMisc.toList("sequenceNum");

                // get the features and filter out expired dates
                features = delegator.findByAndCache("ProductFeatureAndAppl", fields, sort);
                features = EntityUtil.filterByDate(features, true);
            } catch (GenericEntityException e) {
                throw new IllegalStateException("Problem reading relation: " + e.getMessage());
            }
            if (Debug.verboseOn()) {
                Debug.logVerbose("Features: " + features, module);
            }

            // -------------------------------
            for (GenericValue feature : features) {
                //String itemKey = item.getString("description");
                try {
                    GenericValue featureType = feature.getRelatedOne("ProductFeatureType");
                    String desc = (String) featureType.get("description");
                    if (group.containsKey(desc)) {
                        List<GenericValue> itemList = group.get(desc);
                        boolean hasFeature = false;
                        for (int i = 0; i < itemList.size(); i++) {
                            GenericValue genericValue = itemList.get(i);
                            if (genericValue.get("productFeatureId").equals(feature.get("productFeatureId"))) {
                                hasFeature = true;
                            }
                        }
                        if (!hasFeature) {
                            itemList.add(feature);
                        }
                    } else {
                        List<GenericValue> itemList = UtilMisc.toList(feature);
                        group.put(desc, itemList);
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }

            }
        }

        return group;
    }


    // builds a variant sample (a single sku for a featureType)
    private static Map<String, GenericValue> makeVariantSample(Delegator delegator, Map<String, List<String>> featureList, List<String> items, String feature) {
        Map<String, GenericValue> tempSample = FastMap.newInstance();
        Map<String, GenericValue> sample = new LinkedHashMap<String, GenericValue>();
        for (String productId : items) {
            List<GenericValue> features = null;

            try {
                Map<String, String> fields = UtilMisc.toMap("productId", productId, "productFeatureTypeId", feature,
                        "productFeatureApplTypeId", "STANDARD_FEATURE");
                List<String> sort = UtilMisc.toList("sequenceNum", "description");

                // get the features and filter out expired dates
                features = delegator.findByAndCache("ProductFeatureAndAppl", fields, sort);
                features = EntityUtil.filterByDate(features, true);
            } catch (GenericEntityException e) {
                throw new IllegalStateException("Problem reading relation: " + e.getMessage());
            }
            for (GenericValue featureAppl : features) {
                try {
                    GenericValue product = delegator.findByPrimaryKeyCache("Product",
                            UtilMisc.toMap("productId", productId));

                    tempSample.put(featureAppl.getString("description"), product);
                } catch (GenericEntityException e) {
                    throw new RuntimeException("Cannot get product entity: " + e.getMessage());
                }
            }
        }

        // Sort the sample based on the feature list.
        List<String> features = featureList.get(feature);
        for (String f : features) {
            if (tempSample.containsKey(f)) {
                sample.put(f, tempSample.get(f));
            }
        }

        return sample;
    }

    

    /**
     * This will create a virtual product and return its ID, and associate all of the variants with it.
     * It will not put the selectable features on the virtual or standard features on the variant.
     */
    public static Map<String, Object> quickCreateVirtualWithVariants(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

        // get the various IN attributes
        String variantProductIdsBag = (String) context.get("variantProductIdsBag");
        String productFeatureIdOne = (String) context.get("productFeatureIdOne");
        String productFeatureIdTwo = (String) context.get("productFeatureIdTwo");
        String productFeatureIdThree = (String) context.get("productFeatureIdThree");
        Locale locale = (Locale) context.get("locale");

        Map<String, Object> successResult = ServiceUtil.returnSuccess();

        try {
            // Generate new virtual productId, prefix with "VP", put in successResult
            String productId = (String) context.get("productId");

            if (UtilValidate.isEmpty(productId)) {
                productId = "VP" + delegator.getNextSeqId("Product");
                // Create new virtual product...
                GenericValue product = delegator.makeValue("Product");
                product.set("productId", productId);
                // set: isVirtual=Y, isVariant=N, productTypeId=FINISHED_GOOD, introductionDate=now
                product.set("isVirtual", "Y");
                product.set("isVariant", "N");
                product.set("productTypeId", "FINISHED_GOOD");
                product.set("introductionDate", nowTimestamp);
                // set all to Y: returnable, taxable, chargeShipping, autoCreateKeywords, includeInPromotions
                product.set("returnable", "Y");
                product.set("taxable", "Y");
                product.set("chargeShipping", "Y");
                product.set("autoCreateKeywords", "Y");
                product.set("includeInPromotions", "Y");
                // in it goes!
                product.create();
            }
            successResult.put("productId", productId);

            // separate variantProductIdsBag into a Set of variantProductIds
            //note: can be comma, tab, or white-space delimited
            Set<String> prelimVariantProductIds = FastSet.newInstance();
            List<String> splitIds = Arrays.asList(variantProductIdsBag.split("[,\\p{Space}]"));
            Debug.logInfo("Variants: bag=" + variantProductIdsBag, module);
            Debug.logInfo("Variants: split=" + splitIds, module);
            prelimVariantProductIds.addAll(splitIds);
            //note: should support both direct productIds and GoodIdentification entries (what to do if more than one GoodID? Add all?

            Map<String, GenericValue> variantProductsById = FastMap.newInstance();
            for (String variantProductId : prelimVariantProductIds) {
                if (UtilValidate.isEmpty(variantProductId)) {
                    // not sure why this happens, but seems to from time to time with the split method
                    continue;
                }
                // is a Product.productId?
                GenericValue variantProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", variantProductId));
                if (variantProduct != null) {
                    variantProductsById.put(variantProductId, variantProduct);
                } else {
                    // is a GoodIdentification.idValue?
                    List<GenericValue> goodIdentificationList = delegator.findByAnd("GoodIdentification", UtilMisc.toMap("idValue", variantProductId));
                    if (UtilValidate.isEmpty(goodIdentificationList)) {
                        // whoops, nothing found... return error
                        return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                "ProductVirtualVariantCreation", UtilMisc.toMap("variantProductId", variantProductId), locale));
                    }

                    if (goodIdentificationList.size() > 1) {
                        // what to do here? for now just log a warning and add all of them as variants; they can always be dissociated later
                        Debug.logWarning("Warning creating a virtual with variants: the ID [" + variantProductId + "] was not a productId and resulted in [" + goodIdentificationList.size() + "] GoodIdentification records: " + goodIdentificationList, module);
                    }

                    for (GenericValue goodIdentification : goodIdentificationList) {
                        GenericValue giProduct = goodIdentification.getRelatedOne("Product");
                        if (giProduct != null) {
                            variantProductsById.put(giProduct.getString("productId"), giProduct);
                        }
                    }
                }
            }

            // Attach productFeatureIdOne, Two, Three to the new virtual and all variant products as a standard feature
            Set<String> featureProductIds = FastSet.newInstance();
            featureProductIds.add(productId);
            featureProductIds.addAll(variantProductsById.keySet());
            Set<String> productFeatureIds = new HashSet<String>();
            productFeatureIds.add(productFeatureIdOne);
            productFeatureIds.add(productFeatureIdTwo);
            productFeatureIds.add(productFeatureIdThree);

            for (String featureProductId : featureProductIds) {
                for (String productFeatureId : productFeatureIds) {
                    if (UtilValidate.isNotEmpty(productFeatureId)) {
                        GenericValue productFeatureAppl = delegator.makeValue("ProductFeatureAppl",
                                UtilMisc.toMap("productId", featureProductId, "productFeatureId", productFeatureId,
                                        "productFeatureApplTypeId", "STANDARD_FEATURE", "fromDate", nowTimestamp));
                        productFeatureAppl.create();
                    }
                }
            }

            for (GenericValue variantProduct : variantProductsById.values()) {
                // for each variant product set: isVirtual=N, isVariant=Y, introductionDate=now
                variantProduct.set("isVirtual", "N");
                variantProduct.set("isVariant", "Y");
                variantProduct.set("introductionDate", nowTimestamp);
                variantProduct.store();

                // for each variant product create associate with the new virtual as a PRODUCT_VARIANT
                GenericValue productAssoc = delegator.makeValue("ProductAssoc",
                        UtilMisc.toMap("productId", productId, "productIdTo", variantProduct.get("productId"),
                                "productAssocTypeId", "PRODUCT_VARIANT", "fromDate", nowTimestamp));
                productAssoc.create();
            }
        } catch (GenericEntityException e) {
            String errMsg = "Error creating new virtual product from variant products: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        }
        return successResult;
    }

    public static Map<String, Object> updateProductIfAvailableFromShipment(DispatchContext dctx, Map<String, ? extends Object> context) {
        if ("Y".equals(UtilProperties.getPropertyValue("catalog.properties", "reactivate.product.from.receipt", "N"))) {
            LocalDispatcher dispatcher = dctx.getDispatcher();
            Delegator delegator = dctx.getDelegator();
            GenericValue userLogin = (GenericValue) context.get("userLogin");
            String inventoryItemId = (String) context.get("inventoryItemId");

            GenericValue inventoryItem = null;
            try {
                inventoryItem = delegator.findByPrimaryKeyCache("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemId));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }

            if (inventoryItem != null) {
                String productId = inventoryItem.getString("productId");
                GenericValue product = null;
                try {
                    product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }

                if (product != null) {
                    Timestamp salesDiscontinuationDate = product.getTimestamp("salesDiscontinuationDate");
                    if (salesDiscontinuationDate != null && salesDiscontinuationDate.before(UtilDateTime.nowTimestamp())) {
                        Map<String, Object> invRes = null;
                        try {
                            invRes = dispatcher.runSync("getProductInventoryAvailable", UtilMisc.<String, Object>toMap("productId", productId, "userLogin", userLogin));
                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }

                        BigDecimal availableToPromiseTotal = (BigDecimal) invRes.get("availableToPromiseTotal");
                        if (availableToPromiseTotal != null && availableToPromiseTotal.compareTo(BigDecimal.ZERO) > 0) {
                            // refresh the product so we can update it
                            GenericValue productToUpdate = null;
                            try {
                                productToUpdate = delegator.findByPrimaryKey("Product", product.getPrimaryKey());
                            } catch (GenericEntityException e) {
                                Debug.logError(e, module);
                                return ServiceUtil.returnError(e.getMessage());
                            }

                            // set and save
                            productToUpdate.set("salesDiscontinuationDate", null);
                            try {
                                delegator.store(productToUpdate);
                            } catch (GenericEntityException e) {
                                Debug.logError(e, module);
                                return ServiceUtil.returnError(e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        return ServiceUtil.returnSuccess();
    }

 
    

    /**
     * Finds productId(s) corresponding to a product reference, productId or a GoodIdentification idValue
     *
     * @param ctx     the dispatch context
     * @param context productId use to search with productId or goodIdentification.idValue
     * @return a GenericValue with a productId and a List of complementary productId found
     */
    public static Map<String, Object> findProductById(DispatchContext ctx, Map<String, Object> context) {
        Delegator delegator = ctx.getDelegator();
        String idToFind = (String) context.get("idToFind");
        String goodIdentificationTypeId = (String) context.get("goodIdentificationTypeId");
        String searchProductFirstContext = (String) context.get("searchProductFirst");
        String searchAllIdContext = (String) context.get("searchAllId");

        boolean searchProductFirst = UtilValidate.isNotEmpty(searchProductFirstContext) && "N".equals(searchProductFirstContext) ? false : true;
        boolean searchAllId = UtilValidate.isNotEmpty(searchAllIdContext) && "Y".equals(searchAllIdContext) ? true : false;

        GenericValue product = null;
        List<GenericValue> productsFound = null;
        try {
            productsFound = ProductWorker.findProductsById(delegator, idToFind, goodIdentificationTypeId, searchProductFirst, searchAllId);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        if (UtilValidate.isNotEmpty(productsFound)) {
            // gets the first productId of the List
            product = EntityUtil.getFirst(productsFound);
            // remove this productId
            productsFound.remove(0);
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("product", product);
        result.put("productsList", productsFound);

        return result;
    }

    public static Map<String, Object> addImageForProductPromo(DispatchContext dctx, Map<String, ? extends Object> context)
            throws IOException, JDOMException {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productPromoId = (String) context.get("productPromoId");
        String productPromoContentTypeId = (String) context.get("productPromoContentTypeId");
        ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
        String contentId = (String) context.get("contentId");
        Locale locale = (Locale) context.get("locale");

        if (UtilValidate.isNotEmpty(context.get("_uploadedFile_fileName"))) {
            String imageFilenameFormat = UtilProperties.getPropertyValue("catalog", "image.filename.format");
            String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.server.path"), context);
            while (imageServerPath.endsWith("/")) {
                imageServerPath = imageServerPath.substring(0, imageServerPath.length() - 1);
            }
            String imageUrlPrefix = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.url.prefix"), context);
            if (imageUrlPrefix.endsWith("/")) {
                imageUrlPrefix = imageUrlPrefix.substring(0, imageUrlPrefix.length() - 1);
            }
            FlexibleStringExpander filenameExpander = FlexibleStringExpander.getInstance(imageFilenameFormat);
            String id = productPromoId + "_Image_" + productPromoContentTypeId.charAt(productPromoContentTypeId.length() - 1);
            String fileLocation = filenameExpander.expandString(UtilMisc.toMap("location", "products", "type", "promo", "id", id));
            String filePathPrefix = "";
            String filenameToUse = fileLocation;
            if (fileLocation.lastIndexOf("/") != -1) {
                filePathPrefix = fileLocation.substring(0, fileLocation.lastIndexOf("/") + 1); // adding 1 to include the trailing slash
                filenameToUse = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
            }

            List<GenericValue> fileExtension = FastList.newInstance();
            try {
                fileExtension = delegator.findList("FileExtension", EntityCondition.makeCondition("mimeTypeId", EntityOperator.EQUALS, context.get("_uploadedFile_contentType")), null, null, null, false);
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }

            GenericValue extension = EntityUtil.getFirst(fileExtension);
            if (extension != null) {
                filenameToUse += "." + extension.getString("fileExtensionId");
            }

            File makeResourceDirectory = new File(imageServerPath + "/" + filePathPrefix);
            if (!makeResourceDirectory.exists()) {
                makeResourceDirectory.mkdirs();
            }

            File file = new File(imageServerPath + "/" + filePathPrefix + filenameToUse);

            try {
                RandomAccessFile out = new RandomAccessFile(file, "rw");
                out.write(imageData.array());
                out.close();
            } catch (FileNotFoundException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductImageViewUnableWriteFile", UtilMisc.toMap("fileName", file.getAbsolutePath()), locale));
            } catch (IOException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductImageViewUnableWriteBinaryData", UtilMisc.toMap("fileName", file.getAbsolutePath()), locale));
            }

            String imageUrl = imageUrlPrefix + "/" + filePathPrefix + filenameToUse;

            if (UtilValidate.isNotEmpty(imageUrl) && imageUrl.length() > 0) {
                Map<String, Object> dataResourceCtx = FastMap.newInstance();
                dataResourceCtx.put("objectInfo", imageUrl);
                dataResourceCtx.put("dataResourceName", context.get("_uploadedFile_fileName"));
                dataResourceCtx.put("userLogin", userLogin);

                Map<String, Object> productPromoContentCtx = FastMap.newInstance();
                productPromoContentCtx.put("productPromoId", productPromoId);
                productPromoContentCtx.put("productPromoContentTypeId", productPromoContentTypeId);
                productPromoContentCtx.put("fromDate", context.get("fromDate"));
                productPromoContentCtx.put("thruDate", context.get("thruDate"));
                productPromoContentCtx.put("userLogin", userLogin);

                if (UtilValidate.isNotEmpty(contentId)) {
                    GenericValue content = null;
                    try {
                        content = delegator.findOne("Content", UtilMisc.toMap("contentId", contentId), false);
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }

                    if (UtilValidate.isNotEmpty(content)) {
                        GenericValue dataResource = null;
                        try {
                            dataResource = content.getRelatedOne("DataResource");
                        } catch (GenericEntityException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }

                        if (UtilValidate.isNotEmpty(dataResource)) {
                            dataResourceCtx.put("dataResourceId", dataResource.getString("dataResourceId"));
                            try {
                                dispatcher.runSync("updateDataResource", dataResourceCtx);
                            } catch (GenericServiceException e) {
                                Debug.logError(e, module);
                                return ServiceUtil.returnError(e.getMessage());
                            }
                        } else {
                            dataResourceCtx.put("dataResourceTypeId", "SHORT_TEXT");
                            dataResourceCtx.put("mimeTypeId", "text/html");
                            Map<String, Object> dataResourceResult = FastMap.newInstance();
                            try {
                                dataResourceResult = dispatcher.runSync("createDataResource", dataResourceCtx);
                            } catch (GenericServiceException e) {
                                Debug.logError(e, module);
                                return ServiceUtil.returnError(e.getMessage());
                            }

                            Map<String, Object> contentCtx = FastMap.newInstance();
                            contentCtx.put("contentId", contentId);
                            contentCtx.put("dataResourceId", dataResourceResult.get("dataResourceId"));
                            contentCtx.put("userLogin", userLogin);
                            try {
                                dispatcher.runSync("updateContent", contentCtx);
                            } catch (GenericServiceException e) {
                                Debug.logError(e, module);
                                return ServiceUtil.returnError(e.getMessage());
                            }
                        }

                        productPromoContentCtx.put("contentId", contentId);
                        try {
                            dispatcher.runSync("updateProductPromoContent", productPromoContentCtx);
                        } catch (GenericServiceException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }
                    }
                } else {
                    dataResourceCtx.put("dataResourceTypeId", "SHORT_TEXT");
                    dataResourceCtx.put("mimeTypeId", "text/html");
                    Map<String, Object> dataResourceResult = FastMap.newInstance();
                    try {
                        dataResourceResult = dispatcher.runSync("createDataResource", dataResourceCtx);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }

                    Map<String, Object> contentCtx = FastMap.newInstance();
                    contentCtx.put("contentTypeId", "DOCUMENT");
                    contentCtx.put("dataResourceId", dataResourceResult.get("dataResourceId"));
                    contentCtx.put("userLogin", userLogin);
                    Map<String, Object> contentResult = FastMap.newInstance();
                    try {
                        contentResult = dispatcher.runSync("createContent", contentCtx);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }

                    productPromoContentCtx.put("contentId", contentResult.get("contentId"));
                    try {
                        dispatcher.runSync("createProductPromoContent", productPromoContentCtx);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }
            }
        } else {
            Map<String, Object> productPromoContentCtx = FastMap.newInstance();
            productPromoContentCtx.put("productPromoId", productPromoId);
            productPromoContentCtx.put("productPromoContentTypeId", productPromoContentTypeId);
            productPromoContentCtx.put("contentId", contentId);
            productPromoContentCtx.put("fromDate", context.get("fromDate"));
            productPromoContentCtx.put("thruDate", context.get("thruDate"));
            productPromoContentCtx.put("userLogin", userLogin);
            try {
                dispatcher.runSync("updateProductPromoContent", productPromoContentCtx);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> productSummary(DispatchContext ctx, Map<String, Object> context) throws GenericEntityException {

        String productId = (String) context.get("productId");
        String webSiteId = (String) context.get("webSiteId");
        String prodCatalogId = (String) context.get("catalogId");
        String productStoreId = (String) context.get("productStoreId");
        GenericValue autoUserLogin = (GenericValue) context.get("UserLogin");

        Delegator delegator = ctx.getDelegator();
        GenericValue productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
        String defaultCurrency = (String) productStore.get("defaultCurrencyUomId");

        Locale locale = (Locale) context.get("locale");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue miniProduct = null;
        try {

            miniProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problems reading order header from datasource.", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(e.getMessage(), "productSummary", locale));

        }
        LocalDispatcher dispatcher = ctx.getDispatcher();
        BigDecimal finalPrice = BigDecimal.ZERO;
        if (miniProduct != null && productStoreId != null && prodCatalogId != null) {
            // calculate the "your" price
            Map<String, Object> priceResult = null;
            try {
                priceResult = dispatcher.runSync("calculateProductPrice",
                        UtilMisc.<String, Object>toMap("product", miniProduct, "prodCatalogId", prodCatalogId, "webSiteId", webSiteId, "currencyUomId", defaultCurrency, "autoUserLogin", autoUserLogin, "productStoreId", productStoreId));
                if (ServiceUtil.isError(priceResult)) {
                    Debug.logError(UtilProperties.getMessage(resourceError,
                            "ProductSummary", locale) + "priceResult error", null, null, priceResult);
                }
            } catch (GenericServiceException e) {
                Debug.logError(e, "Error changing item status to " + "ITEM_COMPLETED" + ": " + e.toString(), module);
                return ServiceUtil.returnError(e.getMessage());
            }


            // returns: basePrice listPrice ,COMPETITIVE_PRICE,AVERAGE_COST
            result.put("priceResult", priceResult);
            finalPrice = (BigDecimal) priceResult.get("basePrice");
            // get aggregated product totalPrice
            if ("AGGREGATED".equals(miniProduct.get("productTypeId")) || "AGGREGATED_SERVICE".equals(miniProduct.get("productTypeId"))) {
                //ProductConfigWrapper configWrapper = ProductConfigWorker.getProductConfigWrapper(productId, defaultCurrency, prodCatalogId,webSiteId,productStoreId,autoUserLogin,dispatcher,delegator,locale);
                ProductConfigWrapper configWrapper = ProductConfigWorker.getProductConfigWrapper(productId, defaultCurrency, prodCatalogId, webSiteId, productStoreId, autoUserLogin, dispatcher, delegator, locale);
                if (configWrapper != null) {
                    configWrapper.setDefaultConfig();
                    // Check if Config Price has to be displayed with tax
                    if ("Y".equals(productStore.get("showPricesWithVatTax"))) {
                        BigDecimal totalPriceNoTax = configWrapper.getTotalPrice();
                        Map<String, Object> totalPriceMap = null;
                        try {
                            totalPriceMap = dispatcher.runSync("calcTaxForDisplay", UtilMisc.toMap("basePrice", totalPriceNoTax, "locale", locale, "productId", productId, "productStoreId", productStoreId));
                        } catch (GenericServiceException e) {
                            e.printStackTrace();
                        }
                        result.put("totalPrice", totalPriceMap.get("priceWithTax"));
                    } else {
                        result.put("totalPrice", configWrapper.getTotalPrice());
                    }
                }
            }


            result.put("nowTimeLong", UtilDateTime.nowTimestamp());


            // make the miniProductContentWrapper
            ProductContentWrapper miniProductContentWrapper = new ProductContentWrapper(dispatcher, miniProduct, locale, "text/html");
            String mediumImageUrl = miniProductContentWrapper.get("MEDIUM_IMAGE_URL").toString();
            String description = miniProductContentWrapper.get("DESCRIPTION").toString();
            String productName = miniProductContentWrapper.get("PRODUCT_NAME").toString();
            String isVirtual = miniProductContentWrapper.get("IS_VIRTUAL").toString();
            String largeImageUrl = miniProductContentWrapper.get("LARGE_IMAGE_URL").toString();
            String originalImageUrl = miniProductContentWrapper.get("ORIGINAL_IMAGE_URL").toString();
            String smallImageUrl = miniProductContentWrapper.get("SMALL_IMAGE_URL").toString();
            String wrapProductId = miniProductContentWrapper.get("PRODUCT_ID").toString();


            String additionalImage1Detail = miniProductContentWrapper.get("XTRA_IMG_1_DETAIL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_1_DETAIL").toString();
            String additionalImage1Dedium = miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_1_MEDIUM").toString();
            String additionalImage1Large = miniProductContentWrapper.get("XTRA_IMG_1_LARGE") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_1_LARGE").toString();
            String additionalImage1Orginal = miniProductContentWrapper.get("XTRA_IMG_1_ORIGINAL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_1_ORIGINAL").toString();
            String additionalImage1Small = miniProductContentWrapper.get("XTRA_IMG_1_SMALL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_1_SMALL").toString();
            List additionalImage1 = FastList.newInstance();
            if (!"".equals(additionalImage1Detail)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDetail", additionalImage1Detail);
                additionalImage1.add(imageMap);
            }
            if (!"".equals(additionalImage1Dedium)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDedium", additionalImage1Dedium);
                additionalImage1.add(imageMap);
            }
            if (!"".equals(additionalImage1Large)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageLarge", additionalImage1Large);
                additionalImage1.add(imageMap);
            }
            if (!"".equals(additionalImage1Orginal)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageOrginal", additionalImage1Orginal);
                additionalImage1.add(imageMap);
            }
            if (!"".equals(additionalImage1Small)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageSmall", additionalImage1Small);
                additionalImage1.add(imageMap);
            }


            String additionalImage2Detail = miniProductContentWrapper.get("XTRA_IMG_2_DETAIL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_2_DETAIL").toString();
            String additionalImage2Dedium = miniProductContentWrapper.get("XTRA_IMG_2_MEDIUM") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_2_MEDIUM").toString();
            String additionalImage2Large = miniProductContentWrapper.get("XTRA_IMG_2_LARGE") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_2_LARGE").toString();
            String additionalImage2Orginal = miniProductContentWrapper.get("XTRA_IMG_2_ORIGINAL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_2_ORIGINAL").toString();
            String additionalImage2Small = miniProductContentWrapper.get("XTRA_IMG_2_SMALL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_2_SMALL").toString();

            List additionalImage2 = FastList.newInstance();
            if (!"".equals(additionalImage2Detail)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDetail", additionalImage2Detail);
                additionalImage2.add(imageMap);
            }
            if (!"".equals(additionalImage2Dedium)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDedium", additionalImage2Dedium);
                additionalImage2.add(imageMap);
            }
            if (!"".equals(additionalImage2Large)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageLarge", additionalImage2Large);
                additionalImage2.add(imageMap);
            }
            if (!"".equals(additionalImage2Orginal)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageOrginal", additionalImage2Orginal);
                additionalImage2.add(imageMap);
            }
            if (!"".equals(additionalImage2Small)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageSmall", additionalImage2Small);
                additionalImage2.add(imageMap);
            }


            String additionalImage3Detail = miniProductContentWrapper.get("XTRA_IMG_3_DETAIL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_3_DETAIL").toString();
            String additionalImage3Dedium = miniProductContentWrapper.get("XTRA_IMG_3_MEDIUM") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_3_MEDIUM").toString();
            String additionalImage3Large = miniProductContentWrapper.get("XTRA_IMG_3_LARGE") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_3_LARGE").toString();
            String additionalImage3Orginal = miniProductContentWrapper.get("XTRA_IMG_3_ORIGINAL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_3_ORIGINAL").toString();
            String additionalImage3Small = miniProductContentWrapper.get("XTRA_IMG_3_SMALL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_3_SMALL").toString();


            List additionalImage3 = FastList.newInstance();
            if (!"".equals(additionalImage3Detail)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDetail", additionalImage3Detail);
                additionalImage3.add(imageMap);
            }
            if (!"".equals(additionalImage3Dedium)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDedium", additionalImage3Dedium);
                additionalImage3.add(imageMap);
            }
            if (!"".equals(additionalImage3Large)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageLarge", additionalImage3Large);
                additionalImage3.add(imageMap);
            }
            if (!"".equals(additionalImage3Orginal)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageOrginal", additionalImage3Orginal);
                additionalImage3.add(imageMap);
            }
            if (!"".equals(additionalImage3Small)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageSmall", additionalImage3Small);
                additionalImage3.add(imageMap);
            }

            String additionalImage4Detail = miniProductContentWrapper.get("XTRA_IMG_4_DETAIL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_4_DETAIL").toString();
            String additionalImage4Dedium = miniProductContentWrapper.get("XTRA_IMG_4_MEDIUM") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_4_MEDIUM").toString();
            String additionalImage4Large = miniProductContentWrapper.get("XTRA_IMG_4_LARGE") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_4_LARGE").toString();
            String additionalImage4Orginal = miniProductContentWrapper.get("XTRA_IMG_4_ORIGINAL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_4_ORIGINAL").toString();
            String additionalImage4Small = miniProductContentWrapper.get("XTRA_IMG_4_SMALL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_4_SMALL").toString();


            List additionalImage4 = FastList.newInstance();
            if (!"".equals(additionalImage4Detail)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDetail", additionalImage4Detail);
                additionalImage4.add(imageMap);
            }
            if (!"".equals(additionalImage4Dedium)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDedium", additionalImage4Dedium);
                additionalImage4.add(imageMap);
            }
            if (!"".equals(additionalImage4Large)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageLarge", additionalImage4Large);
                additionalImage4.add(imageMap);
            }
            if (!"".equals(additionalImage4Orginal)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageOrginal", additionalImage4Orginal);
                additionalImage4.add(imageMap);
            }
            if (!"".equals(additionalImage4Small)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageSmall", additionalImage4Small);
                additionalImage4.add(imageMap);
            }


            String additionalImage5Detail = miniProductContentWrapper.get("XTRA_IMG_5_DETAIL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_5_DETAIL").toString();
            String additionalImage5Dedium = miniProductContentWrapper.get("XTRA_IMG_5_MEDIUM") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_5_MEDIUM").toString();
            String additionalImage5Large = miniProductContentWrapper.get("XTRA_IMG_5_LARGE") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_5_LARGE").toString();
            String additionalImage5Orginal = miniProductContentWrapper.get("XTRA_IMG_5_ORIGINAL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_5_ORIGINAL").toString();
            String additionalImage5Small = miniProductContentWrapper.get("XTRA_IMG_5_SMALL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_5_SMALL").toString();

            List additionalImage5 = FastList.newInstance();
            if (!"".equals(additionalImage5Detail)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDetail", additionalImage5Detail);
                additionalImage5.add(imageMap);
            }
            if (!"".equals(additionalImage5Dedium)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDedium", additionalImage5Dedium);
                additionalImage5.add(imageMap);
            }
            if (!"".equals(additionalImage5Large)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageLarge", additionalImage5Large);
                additionalImage5.add(imageMap);
            }
            if (!"".equals(additionalImage5Orginal)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageOrginal", additionalImage5Orginal);
                additionalImage5.add(imageMap);
            }
            if (!"".equals(additionalImage5Small)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageSmall", additionalImage5Small);
                additionalImage5.add(imageMap);
            }

            String additionalImage6Detail = miniProductContentWrapper.get("XTRA_IMG_6_DETAIL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_6_DETAIL").toString();
            String additionalImage6Dedium = miniProductContentWrapper.get("XTRA_IMG_6_MEDIUM") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_6_MEDIUM").toString();
            String additionalImage6Large = miniProductContentWrapper.get("XTRA_IMG_6_LARGE") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_6_LARGE").toString();
            String additionalImage6Orginal = miniProductContentWrapper.get("XTRA_IMG_6_ORIGINAL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_6_ORIGINAL").toString();
            String additionalImage6Small = miniProductContentWrapper.get("XTRA_IMG_6_SMALL") == null ? "" : miniProductContentWrapper.get("XTRA_IMG_6_SMALL").toString();
            List additionalImage6 = FastList.newInstance();
            if (!"".equals(additionalImage6Detail)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageDetail", additionalImage6Detail);
                additionalImage6.add(imageMap);
            }
            if (!"".equals(additionalImage6Dedium)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImage6Dedium", additionalImage6Dedium);
                additionalImage6.add(imageMap);
            }
            if (!"".equals(additionalImage6Large)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageLarge", additionalImage6Large);
                additionalImage6.add(imageMap);
            }
            if (!"".equals(additionalImage6Orginal)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageOrginal", additionalImage6Orginal);
                additionalImage6.add(imageMap);
            }
            if (!"".equals(additionalImage6Small)) {
                Map imageMap = new HashMap();
                imageMap.put("additionalImageSmall", additionalImage6Small);
                additionalImage6.add(imageMap);
            }

            //增加虚拟产品对应的信息featureTypeId,featureId

            if ("Y".equals(isVirtual)) {

                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                List<List<Map<String, String>>> featureLists = ProductWorker.getSelectableProductFeaturesByTypesAndSeq(product);
                result.put("featureList", featureLists);
                Map<String, Object> featureMap = null;
                try {
                    featureMap = dispatcher.runSync("getProductFeatureSet", UtilMisc.toMap("productId", productId));
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                }
                Set featureSet = (Set) featureMap.get("featureSet");
                if (featureSet != null) {
                    Map<String, Object> variantTreeMap = null;
                    try {
                        variantTreeMap = dispatcher.runSync("getProductVariantTree", UtilMisc.toMap("productId", productId, "featureOrder", featureSet, "productStoreId", productStoreId));
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                    }
                    Map<String, Object> variantsRes = null;
                    // make a list of variant sku with requireAmount
                    try {
                        variantsRes = dispatcher.runSync("getAssociatedProducts", UtilMisc.toMap("productId", productId, "type", "PRODUCT_VARIANT",
                                "checkViewAllow", true, "prodCatalogId", prodCatalogId));
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                    }
                    if (variantTreeMap != null && variantTreeMap.get("variantTreeChoose") != null) {
                        result.put("variantTreeChoose", variantTreeMap.get("variantTreeChoose"));
                    }
                    //    result.put("variantsRes", variantsRes);
                    List<GenericValue> assocProducts = (List<GenericValue>) variantsRes.get("assocProducts");
                }

            }
            List<Map> imgsList = FastList.newInstance();
            Map<String, List> imgsMap1 = FastMap.newInstance();
            if (UtilValidate.isNotEmpty(additionalImage1)) {
                imgsMap1.put("additionalImage1", additionalImage1);
            }
            imgsList.add(imgsMap1);

            Map<String, List> imgsMap2 = FastMap.newInstance();
            if (UtilValidate.isNotEmpty(additionalImage2)) {
                imgsMap2.put("additionalImage2", additionalImage2);
            }
            imgsList.add(imgsMap2);

            Map<String, List> imgsMap3 = FastMap.newInstance();
            if (UtilValidate.isNotEmpty(additionalImage3)) {
                imgsMap3.put("additionalImage3", additionalImage3);
            }
            imgsList.add(imgsMap3);

            Map<String, List> imgsMap4 = FastMap.newInstance();
            if (UtilValidate.isNotEmpty(additionalImage4)) {
                imgsMap4.put("additionalImage4", additionalImage4);
            }
            imgsList.add(imgsMap4);

            Map<String, List> imgsMap5 = FastMap.newInstance();
            if (UtilValidate.isNotEmpty(additionalImage5)) {
                imgsMap5.put("additionalImage5", additionalImage5);
            }
            imgsList.add(imgsMap5);


            Map<String, List> imgsMap6 = FastMap.newInstance();
            if (UtilValidate.isNotEmpty(additionalImage6)) {
                imgsMap6.put("additionalImage6", additionalImage6);
            }
            imgsList.add(imgsMap6);

            List<GenericValue> productTags = delegator.findByAnd("ProductTag", UtilMisc.toMap("productId", productId));
            List<String> tagNames = FastList.newInstance();
            productTags = EntityUtil.filterByDate(productTags);
            if (UtilValidate.isNotEmpty(productTags)) {
                for (int i = 0; i < productTags.size(); i++) {
                    GenericValue productTag = productTags.get(i);
                    tagNames.add((String) productTag.get("tagName"));
                }
            }


            if("FINISHED_GOOD".equals(miniProduct.get("productTypeId"))) {
                try {
                    //获取产品的应用的促销信息，直降，团购、秒杀、优惠劵等等
                    result = dispatcher.runSync("getProductPromoInfoByProductId", UtilMisc.toMap("productId", productId));
                    if (ServiceUtil.isError(result)) {
                        result.put("message", ServiceUtil.getErrorMessage(result));
                        result.put("retCode", 0);


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
                            result.put("priceDownInfo", priceDownInfo);

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
                            orderGroupInfo.put("limitQuantity", orderGroupMap.get("activityQuantity"));

                            int quantity = ((Long) orderGroupMap.get("limitQuantity")).intValue();
                            orderGroupInfo.put("limitQuantity", quantity);
                            //成团人员、人头像

                            result.put("orderGroupInfo", orderGroupInfo);
                        }
                        if (UtilValidate.isNotEmpty(result.get("groupInfo"))) {
                            result.put("groupInfo", result.get("groupInfo"));
                        }


                        Map<String, Object> secKillInfo = FastMap.newInstance();
                        if (UtilValidate.isNotEmpty(result.get("secKillInfo"))) {
                            Map secKillMap = (Map) result.get("secKillInfo");
                            secKillInfo.put("activityDesc", secKillMap.get("activityDesc"));
                            secKillInfo.put("activityId", secKillMap.get("activityId"));
                            secKillInfo.put("activityCode", secKillMap.get("activityCode"));
                            //当前用户可以购物的次数
                            int quantity = ((Long) secKillMap.get("limitQuantity")).intValue();
                            secKillInfo.put("limitQuantity", quantity );
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
                            result.put("secKillInfo", secKillInfo);
                        }
                    }

                    //积分兑奖比例
                    GenericValue integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", "PARTY_INTEGRAL_SET"));//积分抵现规则表
                    /*1.不可使用积分2:百分比抵扣3:固定金额抵扣*/
                    Long integralValue = integralPerMoney.getLong("integralValue");
                    String integralDeductionType = (String) miniProduct.get("integralDeductionType");
                    BigDecimal integralDeductionUpper = (BigDecimal) miniProduct.get("integralDeductionUpper");
                    if (UtilValidate.isNotEmpty(integralDeductionType)) {
                        if ("2".equalsIgnoreCase(integralDeductionType)) {
                            BigDecimal scorePrice = finalPrice.multiply(integralDeductionUpper).divide(new BigDecimal(100));
                            BigDecimal score = new BigDecimal(integralValue).multiply(scorePrice);
                            result.put("scorePrice", scorePrice.setScale(2, BigDecimal.ROUND_HALF_UP)); //积分抵扣的金额
                            result.put("scoreValue", new Double(Math.ceil(score.doubleValue())).intValue());//需要的积分
                        } else if ("3".equalsIgnoreCase(integralDeductionType)) {
                            result.put("scorePrice", integralDeductionUpper.setScale(2, BigDecimal.ROUND_HALF_UP));//积分抵扣的金额
                            result.put("scoreValue", integralDeductionUpper.multiply(new BigDecimal(integralValue)));//需要的积分
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

            result.put("productTags", tagNames);
            result.put("additionalImages", imgsList);
            result.put("description", description);
            result.put("productId", productId);
            result.put("catalogId", prodCatalogId);
            result.put("mediumImageUrl", mediumImageUrl);
            result.put("smallImageUrl", smallImageUrl);
//            result.put("longDescription",longDescription);
            result.put("productName", productName);
            result.put("isVirtual", isVirtual);
            result.put("largeImageUrl", largeImageUrl);
            result.put("originalImageUrl", originalImageUrl);
            result.put("productTypeId", miniProduct.get("productTypeId"));
            result.put("wrapProductId", wrapProductId);
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
            return result;
        }
        return ServiceUtil.returnError(UtilProperties.getMessage(resourceError, "productevents.product_with_id_not_found", new Object[]{productId}, locale));
    }

    public static Map<String, Object> productContents(DispatchContext dctx, Map<String, ? extends Object> context) {
        Locale locale = (Locale) context.get("locale");
        Delegator delegator = dctx.getDelegator();
        String productId = (String) context.get("productId");
        String contentTypeId = (String) context.get("contentTypeId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map resultData = FastMap.newInstance();
        GenericValue miniProduct = null;
        try {
            miniProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        ProductContentWrapper miniProductContentWrapper = new ProductContentWrapper(dctx.getDispatcher(), miniProduct, locale, "text/html");
        if (contentTypeId == null) {

            try {
                List<GenericValue> contentTypes = delegator.findList("ProductContentType", null, null, null, null, true);
                if (contentTypes.size() > 0) {
                    for (int i = 0; i < contentTypes.size(); i++) {
                        GenericValue productContentType = contentTypes.get(i);
                        String contType = (String) productContentType.get("productContentTypeId");
                        String content = miniProductContentWrapper.get(contType).toString();
                        if(UtilValidate.isNotEmpty(content)) {
                            resultData.put(contType, content);
                        }
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
                ServiceUtil.returnFailure(e.getMessage());
            }
        } else {

            // make the miniProductContentWrapper
            String content = miniProductContentWrapper.get(contentTypeId).toString();
            resultData.put(contentTypeId, content);
        }
        result.put("productContents", resultData);
        return result;
    }


    /**
     * 产品评论
     *

     * @return
     */
    public Map<String, Object> productReview(DispatchContext dctx, Map<String, ? extends Object> context) {
        String productId = (String) context.get("productId");
        String productStoreId = (String) context.get("productStoreId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map resultData = FastMap.newInstance();
        // get all product review in case of Purchase Order.
        try {
            GenericValue product = dctx.getDelegator().findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            List<GenericValue> reviews = product.getRelatedCache("ProductReview", UtilMisc.toMap("statusId", "PRR_APPROVED", "productStoreId", productStoreId), UtilMisc.toList("-postedDateTime"));
            result.put("reviews", reviews);
            // get the average rating
            if (reviews != null && !reviews.isEmpty()) {
                List<GenericValue> ratingReviews = EntityUtil.filterByAnd(reviews, UtilMisc.toList(EntityCondition.makeCondition("productRating", EntityOperator.NOT_EQUAL, null)));
                if (result != null && (!ratingReviews.isEmpty())) {
                    result.put("averageRating", ProductWorker.getAverageProductRating(product, reviews, productStoreId));
                    result.put("numRatings", ratingReviews.size());
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnFailure(e.getMessage());
        }
        return result;
    }


    /**
     * 获取所有的目录下得分类， 目录下浏览根目录
     *

     * @return
     */
    public Map<String, Object> productCategoryList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> catalogs = null;
        List cateList = FastList.newInstance();
        Map resultData = FastMap.newInstance();
        try {
            catalogs = delegator.findList("ProdCatalog", null, null, null, null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnFailure(e.getMessage());
        }
        if (!catalogs.isEmpty()) {
            for (int i = 0; i < catalogs.size(); i++) {
                GenericValue catalog = catalogs.get(i);
                String catalogId = (String) catalog.get("prodCatalogId");
                try {
                    List<GenericValue> categories = EntityUtil.filterByDate(delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", catalogId)));
                    if (!categories.isEmpty()) {
                        for (int j = 0; j < categories.size(); j++) {
                            GenericValue prodCategory = categories.get(j);
                            List<GenericValue> pcategories = prodCategory.getRelated("ProductCategory");
                            cateList.add(pcategories);
                        }
                    }

                } catch (GenericEntityException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnFailure(e.getMessage());
                }

            }
        }
        result.put("categories", cateList);
        return result;
    }


    /**
     * id:当前节点,
     * type:当前节点类型
     * level:当前节点level
     *
     * @param dcx
     * @param context
     * @return
     */
    public static final Map<String, Object> getCatalogCateTree(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String id = (String) context.get("id");
        String type = (String) context.get("type");
        Integer level = (Integer) context.get("level");
        String scope = (String) context.get("scope");
        Delegator delegator = dcx.getDelegator();
        List<Map> gProdCatalogList = FastList.newInstance();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        try {
            List partyGroups = delegator.findByAnd("PartyGroup", UtilMisc.toMap("isInner", "Y"));
            String partyId = "0";
            if (UtilValidate.isNotEmpty(partyGroups)) {
                GenericValue party = (GenericValue) partyGroups.get(0);
                partyId = (String) party.get("partyId");
            }

            if ("0".equals(id) && "catalog".equals(type)) {
                //第一次加载

                List<GenericValue> proStoreCatas = delegator.findByAnd("ProdCatalogStore", null, UtilMisc.toList("+sequenceNum"));
                if (UtilValidate.isNotEmpty(proStoreCatas)) {
                    for (int i = 0; i < proStoreCatas.size(); i++) {
                        GenericValue prodCata = proStoreCatas.get(i);
                        Map gProdCatalogMap = FastMap.newInstance();
                        gProdCatalogMap.put("id", prodCata.getString("prodCatalogId") + "_catalog");
                        gProdCatalogMap.put("name", UtilProperties.getMessage(resource, "ProductCatalogs", (Locale) context.get("locale")) + "-" + prodCata.getString("catalogName"));
                        gProdCatalogMap.put("parent", "0");
                        gProdCatalogMap.put("type", "catalog");
                        gProdCatalogMap.put("level", level + 1);
                        gProdCatalogMap.put("isCatalog", "true");
                        gProdCatalogMap.put("isCategoryType", "false");
                        if (UtilValidate.isNotEmpty(proStoreCatas)) {
                            gProdCatalogMap.put("seq", prodCata.get("sequenceNum"));
                        } else {
                            gProdCatalogMap.put("seq", "1");
                        }
                        gProdCatalogList.add(gProdCatalogMap);
                    }

                }
            } else if (!"0".equals(id) && ("catalog".equals(type))) {
                id = id.substring(0, id.indexOf("_catalog"));
                List<GenericValue> prodCatalogCategories = EntityUtil.filterByDate(delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", id), UtilMisc.toList("+sequenceNum")));
                if (UtilValidate.isNotEmpty(prodCatalogCategories)) {
                    for (int j = 0; j < prodCatalogCategories.size(); j++) {
                        Map gCategoryMap = FastMap.newInstance();
                        GenericValue cataCategory = prodCatalogCategories.get(j);
                        GenericValue productCategory = cataCategory.getRelatedOne("ProductCategory");
                        gCategoryMap.put("id", productCategory.getString("productCategoryId") + "_category");
                        gCategoryMap.put("name", UtilProperties.getMessage(resource, "ProductCategories", (Locale) context.get("locale")) + "-" + productCategory.getString("categoryName"));
                        gCategoryMap.put("parent", id);
                        gCategoryMap.put("type", "category");
                        gCategoryMap.put("level", level + 1);
                        gCategoryMap.put("isCatalog", "false");
                        gCategoryMap.put("isCategoryType", "true");
                        if (UtilValidate.isNotEmpty(cataCategory.get("sequenceNum"))) {
                            gCategoryMap.put("seq", cataCategory.get("sequenceNum"));
                        } else {
                            gCategoryMap.put("seq", "1");
                        }
                        gProdCatalogList.add(gCategoryMap);
                    }
                }


            } else if (!"0".equals(id) && ("category".equals(type))) {
                id = id.substring(0, id.indexOf("_category"));
                List<GenericValue> childOfCats = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryRollupAndChild", UtilMisc.toMap(
                        "parentProductCategoryId", id), UtilMisc.toList("+sequenceNum")));
                if (UtilValidate.isNotEmpty(childOfCats)) {
                    for (int i = 0; i < childOfCats.size(); i++) {
                        Map gCategoryMap = FastMap.newInstance();
                        GenericValue cateCategory = childOfCats.get(i);
                        gCategoryMap.put("id", cateCategory.getString("productCategoryId") + "_category");
                        gCategoryMap.put("name", UtilProperties.getMessage(resource, "ProductCategories", (Locale) context.get("locale")) + "-" + cateCategory.getString("categoryName"));
                        gCategoryMap.put("parent", id);
                        gCategoryMap.put("type", "category");
                        gCategoryMap.put("level", level + 1);
                        gCategoryMap.put("isCatalog", "false");
                        gCategoryMap.put("isCategoryType", "true");
                        if (UtilValidate.isNotEmpty(cateCategory.get("sequenceNum"))) {
                            gCategoryMap.put("seq", cateCategory.get("sequenceNum"));
                        } else {
                            gCategoryMap.put("seq", "1");
                        }
                        gProdCatalogList.add(gCategoryMap);
                    }
                }
                //获取分类下的产品
                Map paramInMap = FastMap.newInstance();
                paramInMap.put("productCategoryId", id);

                paramInMap.put("limitView", false);
                paramInMap.put("useCacheForMembers", true);
                paramInMap.put("defaultViewSize", 25);
                // Returns: viewIndex, viewSize, lowIndex, highIndex, listSize, productCategory, productCategoryMembers
                if (UtilValidate.isEmpty(scope) || (!"category".equals(scope))) {
                    Map result1 = dispatcher.runSync("getProductCategoryAndLimitedMembers", paramInMap);
                    if (ServiceUtil.isError(result1)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result1));
                    }
                    if (result1.get("productCategoryMembers") != null) {
                        List<GenericValue> productCategoryMembers = (List<GenericValue>) result1.get("productCategoryMembers");
                        if (UtilValidate.isNotEmpty(productCategoryMembers)) {
                            for (int i = 0; i < productCategoryMembers.size(); i++) {
                                GenericValue categoryMember = productCategoryMembers.get(i);
                                Map gCategoryMap = FastMap.newInstance();
                                gCategoryMap.put("id", categoryMember.getString("productId") + "_productId");
                                GenericValue product = categoryMember.getRelatedOne("Product");
                                gCategoryMap.put("name", UtilProperties.getMessage(resource, "ProductProducts", (Locale) context.get("locale")) + "-" + product.getString("productName"));
                                gCategoryMap.put("parent", id);
                                gCategoryMap.put("type", "product");
                                gCategoryMap.put("level", level + 1);
                                gCategoryMap.put("isCatalog", "false");
                                gCategoryMap.put("isCategoryType", "false");
                                if (UtilValidate.isNotEmpty(categoryMember.get("sequenceNum"))) {
                                    gCategoryMap.put("seq", categoryMember.get("sequenceNum"));
                                } else {
                                    gCategoryMap.put("seq", "1");
                                }
                                gProdCatalogList.add(gCategoryMap);
                            }
                        }
                    }
                }

            } else if ("0".equals(id) && ("category".equals(type))) {
                List<GenericValue> prodCatalogCategories = EntityUtil.filterByDate(delegator.findByAnd("ProdCatalogCategory", null, UtilMisc.toList("+sequenceNum")));
                if (UtilValidate.isNotEmpty(prodCatalogCategories)) {
                    for (int j = 0; j < prodCatalogCategories.size(); j++) {
                        Map gCategoryMap = FastMap.newInstance();
                        GenericValue cataCategory = prodCatalogCategories.get(j);
                        GenericValue productCategory = cataCategory.getRelatedOne("ProductCategory");
                        gCategoryMap.put("id", productCategory.getString("productCategoryId") + "_category");
                        gCategoryMap.put("name", UtilProperties.getMessage(resource, "ProductCategories", (Locale) context.get("locale")) + "-" + productCategory.getString("categoryName"));
                        gCategoryMap.put("parent", id);
                        gCategoryMap.put("type", "category");
                        gCategoryMap.put("level", level + 1);
                        gCategoryMap.put("isCatalog", "false");
                        gCategoryMap.put("isCategoryType", "true");
                        if (UtilValidate.isNotEmpty(cataCategory.get("sequenceNum"))) {
                            gCategoryMap.put("seq", cataCategory.get("sequenceNum"));
                        } else {
                            gCategoryMap.put("seq", "1");
                        }
                        gProdCatalogList.add(gCategoryMap);
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        result.put("nodes", gProdCatalogList);
        return result;
    }

    /**
     * @param dcx
     * @param context
     * @return
     */
    public static final Map<String, Object> catalogNodeCreate(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String parent = (String) context.get("parent");
        String name = (String) context.get("name");
        //before after firstChild lastChild
        String position = (String) context.get("position");
        String related = (String) context.get("related");
        Delegator delegator = dcx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String parentType = (String) context.get("parentType");
        String relatedType = (String) context.get("relatedType");
        String action = (String) context.get("action");
        String targetId = (String) context.get("targetId");
        LocalDispatcher localDispatcher = dcx.getDispatcher();
        try {
            List<GenericValue> updateList = FastList.newInstance();
            if ("catalog".equals(parentType)) {
                String pCatalogId = "0";
                if (parent.indexOf("_catalog") != -1) {
                    pCatalogId = parent.substring(0, parent.indexOf("_catalog"));
                }
                String categoryId = "";
                Long num = 1L;
                if ("before".equals(position) || "after".equals(position)) {
                    if ("catalog".equals(relatedType)) {
                        related = related.substring(0, related.indexOf("_catalog"));
                        GenericValue currentCatalog = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("prodCatalogId", related))).get(0);
                        num = currentCatalog.get("sequenceNum") == null ? 1L : (Long) currentCatalog.get("sequenceNum");
                        List proStoreCatalogs = delegator.findList("ProductStoreCatalog", null, null, UtilMisc.toList("+sequenceNum"), null, false);
                        String productStoreId = null;
                        if (position.endsWith("before")) {
                            //当前catalog及之后的catalog num+1;
                            for (int i = 0; i < proStoreCatalogs.size(); i++) {
                                GenericValue proStoreCatalog = (GenericValue) proStoreCatalogs.get(i);
                                Long currentNum = proStoreCatalog.get("sequenceNum") == null ? 1L : (Long) proStoreCatalog.get("sequenceNum");
                                if (currentNum >= num) {
                                    proStoreCatalog.put("sequenceNum", currentNum + 1);
                                    updateList.add(proStoreCatalog);
                                }
                                productStoreId = (String) proStoreCatalog.get("productStoreId");
                            }
                        } else if ("after".equals(position)) {
                            //当前不变,之后之后的category num+1;
                            for (int i = 0; i < proStoreCatalogs.size(); i++) {
                                GenericValue proStoreCatalog = (GenericValue) proStoreCatalogs.get(i);
                                Long currentNum = proStoreCatalog.get("sequenceNum") == null ? 1L : (Long) proStoreCatalog.get("sequenceNum");
                                if (currentNum > num) {
                                    proStoreCatalog.put("sequenceNum", currentNum + 1);
                                    updateList.add(proStoreCatalog);
                                }
                                productStoreId = (String) proStoreCatalog.get("productStoreId");
                            }
                            num = num + 1;
                        }
                        Map<String, Object> result1 = localDispatcher.runSync("createProdCatalog", UtilMisc.toMap("catalogName", name, "productStoreId", productStoreId, "fromDate", UtilDateTime.nowTimestamp(), "sequenceNum", num, "userLogin", userLogin));
                        if (ServiceUtil.isError(result1)) {
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result1));
                        }
                        result.put("id", result1.get("prodCatalogId") + "_catalog");
                        result.put("name", UtilProperties.getMessage(resource, "ProductCatalogs", (Locale) context.get("locale")) + "-" + name);
                        result.put("level", 1);
                        result.put("type", "catalog");
                        result.put("seq", num.intValue());
                        result.put("parent", pCatalogId);

                    } else {
                        if ("category".equals(relatedType)) {
                            related = related.substring(0, related.indexOf("_category"));
                            //目录下创建分类
                            categoryId = related;
                            List<GenericValue> curCategories = delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", pCatalogId, "productCategoryId", categoryId));
                            curCategories = EntityUtil.filterByDate(curCategories);
                            if (UtilValidate.isNotEmpty(curCategories)) {
                                num = curCategories.get(0).get("sequenceNum") == null ? 1L : (Long) curCategories.get(0).get("sequenceNum");
                            }

                            List<GenericValue> pCatalogCategories = delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", pCatalogId), UtilMisc.toList("+sequenceNum"));
                            if (position.endsWith("before")) {
                                //当前category及之后的category num+1;
                                for (int i = 0; i < pCatalogCategories.size(); i++) {
                                    GenericValue pCatalogCate = pCatalogCategories.get(i);
                                    Long currentNum = pCatalogCate.get("sequenceNum") == null ? 1L : (Long) pCatalogCate.get("sequenceNum");
                                    if (currentNum >= num) {
                                        pCatalogCate.put("sequenceNum", currentNum + 1);
                                        updateList.add(pCatalogCate);
                                    }
                                }
                            } else if ("after".equals(position)) {
                                //当前不变,之后之后的category num+1;
                                for (int i = 0; i < pCatalogCategories.size(); i++) {
                                    GenericValue pCatalogCate = pCatalogCategories.get(i);
                                    Long currentNum = pCatalogCate.get("sequenceNum") == null ? 1L : (Long) pCatalogCate.get("sequenceNum");
                                    if (currentNum > num) {
                                        pCatalogCate.put("sequenceNum", currentNum + 1);
                                        updateList.add(pCatalogCate);
                                    }
                                }
                                num = num + 1;
                            }


                        }
                    }
                }
                if ("firstChild".equals(position) || "lastChild".equals(position)) {
                    List<GenericValue> iterator = delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", pCatalogId), UtilMisc.toList("+sequenceNum"));
                    if (UtilValidate.isNotEmpty(iterator)) {
                        for (int i = 0; i < iterator.size(); i++) {
                            GenericValue catalogCate = iterator.get(i);
                            if ("firstChild".equals(position)) {
                                //第一个 则 所有+1
                                catalogCate.put("sequenceNum", catalogCate.get("sequenceNum") == null ? 1L : (Long) catalogCate.get("sequenceNum") + 1);
                                updateList.add(catalogCate);
                                num = 1L;
                            }
                            if ("lastChild".equals(position)) {
                                if (i == iterator.size() - 1) {
                                    //取最后一个的sequenceNum
                                    num = catalogCate.get("sequenceNum") == null ? 1L : (Long) catalogCate.get("sequenceNum") + 1;
                                }
                            }
                        }

                    }
                    String id = targetId;
                    if ("1".equals(action)) {

                    } else {
                        Map<String, Object> result1 = localDispatcher.runSync("createProductCategory", UtilMisc.toMap("userLogin", userLogin, "productCategoryTypeId", "CATALOG_CATEGORY", "categoryName", name, "description", name));
                        if (ServiceUtil.isError(result1)) {
                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result1));
                        }
                        id = (String) result1.get("productCategoryId");
                    }
                    Map<String, Object> result2 = localDispatcher.runSync("addProductCategoryToProdCatalog", UtilMisc.toMap("userLogin", userLogin, "productCategoryId", id, "prodCatalogId", pCatalogId,
                            "prodCatalogCategoryTypeId", "PCCT_BROWSE_ROOT", "fromDate", UtilDateTime.nowTimestamp(), "sequenceNum", num));

                    if (ServiceUtil.isError(result2)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result2));
                    }
                    delegator.storeAll(updateList);
                    //{"id":8273,"name":"ddsssaaassss","level":2,"type":"default"}
                    result.put("id", id + "_category");
                    result.put("name", UtilProperties.getMessage(resource, "ProductCategories", (Locale) context.get("locale")) + "-" + name);
                    result.put("level", 2);
                    result.put("type", "category");
                    result.put("seq", num.intValue());
                    result.put("parent", pCatalogId);
                }

            } else if ("category".equals(parentType)) {
                parent = parent.substring(0, parent.indexOf("_category"));
                Long num = 1L;
                String pCategory = parent;
                if ("before".equals(position) || "after".equals(position)) {
                    if ("category".equals(relatedType)) {
                        //目录下创建分类
                        related = related.substring(0, related.indexOf("_category"));
                        String categoryId = related;
                        List<GenericValue> childOfCats = delegator.findByAnd("ProductCategoryRollupAndChild", UtilMisc.toMap(
                                "parentProductCategoryId", pCategory, "productCategoryId", categoryId));

                        List<GenericValue> allChildOfCats = delegator.findByAnd("ProductCategoryRollup", UtilMisc.toMap(
                                "parentProductCategoryId", pCategory));
                        if (UtilValidate.isNotEmpty(childOfCats)) {
                            Long curNum = childOfCats.get(0).get("sequenceNum") == null ? 1L : (Long) childOfCats.get(0).get("sequenceNum");
                            for (int i = 0; i < allChildOfCats.size(); i++) {
                                GenericValue allCate = allChildOfCats.get(i);
                                Long allNum = allCate.get("sequenceNum") == null ? 1L : (Long) allCate.get("sequenceNum");
                                if ("before".equals(position)) {
                                    //新增的为当前的
                                    if (allNum >= curNum) {
                                        allCate.put("sequenceNum", curNum + 1);
                                        updateList.add(allCate);
                                    }

                                } else if ("after".equals(position)) {
                                    if (allNum > curNum) {
                                        allCate.put("sequenceNum", allNum + 1);
                                        updateList.add(allCate);
                                    }
                                }
                            }
                            if ("before".equals(position)) {
                                num = curNum;
                            } else if ("after".equals(position)) {
                                num = curNum + 1;
                            }
                        }
                    }

                } else if ("firstChild".equals(position) || "lastChild".equals(position)) {
                    List<GenericValue> iterator = delegator.findByAnd("ProductCategoryRollup", UtilMisc.toMap("parentProductCategoryId", pCategory), UtilMisc.toList("+sequenceNum"));
                    if (UtilValidate.isNotEmpty(iterator)) {
                        for (int i = 0; i < iterator.size(); i++) {
                            GenericValue catalogCate = iterator.get(i);
                            if ("firstChild".equals(position)) {
                                //第一个 则 所有+1
                                catalogCate.put("sequenceNum", catalogCate.get("sequenceNum") == null ? 1L : (Long) catalogCate.get("sequenceNum") + 1);
                                updateList.add(catalogCate);
                            }
                            if ("lastChild".equals(position)) {
                                if (i == iterator.size() - 1) {
                                    //取最后一个的sequenceNum
                                    num = catalogCate.get("sequenceNum") == null ? 1L : (Long) catalogCate.get("sequenceNum") + 1;
                                }
                            }
                        }
                        if ("firstChild".equals(position)) {
                            num = 1L;
                        }
                    }
                }
                String id = null;
                if ("1".equals(action)) {
                    id = targetId;
                } else {
                    Map<String, Object> result1 = localDispatcher.runSync("createProductCategory", UtilMisc.toMap("userLogin", userLogin, "productCategoryTypeId", "CATALOG_CATEGORY", "categoryName", name, "description", name));
                    if (ServiceUtil.isError(result1)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result1));
                    }

                    id = (String) result1.get("productCategoryId");
                }

                Map<String, Object> result2 = localDispatcher.runSync("addProductCategoryToCategory", UtilMisc.toMap("userLogin", userLogin, "productCategoryId", id, "parentProductCategoryId", pCategory,
                        "fromDate", UtilDateTime.nowTimestamp(), "sequenceNum", num));

                if (ServiceUtil.isError(result2)) {
                    return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result2));
                }
                delegator.storeAll(updateList);
                result.put("id", id + "_category");
                result.put("name", UtilProperties.getMessage(resource, "ProductCategories", (Locale) context.get("locale")) + "-" + name);
                result.put("level", 3);
                result.put("type", "category");
                result.put("parent", pCategory);
                result.put("seq", num.intValue());
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    public static final Map<String, Object> catalogNodeDelete(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String id = (String) context.get("id");
        String type = (String) context.get("type");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        try {
            if (UtilValidate.isNotEmpty(type)) {
                if ("catalog".equals(type)) {
                    id = id.substring(0, id.indexOf("_catalog"));
                    result = dispatcher.runSync("deleteProdCatalog", UtilMisc.toMap("prodCatalogId", id, "userLogin", userLogin));
                    if (ServiceUtil.isError(result)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }

                } else if ("category".equals(type)) {
                    id = id.substring(0, id.indexOf("_category"));
                    result = dispatcher.runSync("deleteCategory", UtilMisc.toMap("productCategoryId", id, "userLogin", userLogin));
                    if (ServiceUtil.isError(result)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                } else if ("product".equals(type)) {
                    id = id.substring(0, id.indexOf("_product"));
                    result = dispatcher.runSync("removeProduct", UtilMisc.toMap("productId", id, "userLogin", userLogin));
                    if (ServiceUtil.isError(result)) {
                        return ServiceUtil.returnError(ServiceUtil.getErrorMessage(result));
                    }
                }
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * @param dcx
     * @param context
     * @return
     */
    public static final Map<String, Object> catalogNodeUpdate(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();

        String name = (String) context.get("name");
        String id = (String) context.get("id");
        String type = (String) context.get("type");

        String parent = (String) context.get("parent");
        //before after firstChild lastChild
        String position = (String) context.get("position");
        String related = (String) context.get("related");
        String parentType = (String) context.get("parentType");
        String relatedType = (String) context.get("relatedType");
        Integer level = (Integer) context.get("level");
        Integer seq = (Integer) context.get("seq");

        Delegator delegator = dcx.getDelegator();
        try {
            if (UtilValidate.isNotEmpty(type)) {
                if ("catalog".equals(type)) {
                    id = id.substring(0, id.indexOf("_catalog"));
                    String prefix = UtilProperties.getMessage(resource, "ProductCatalogs", (Locale) context.get("locale")) + "-";
                    if (name.startsWith(prefix)) {
                        name = name.substring(prefix.length());
                    }
                    GenericValue catalog = delegator.findByPrimaryKey("ProdCatalog", UtilMisc.toMap("prodCatalogId", id));
                    catalog.put("catalogName", name);
                    delegator.store(catalog);
                    result.put("id", id + "_catalog");
                    result.put("name", UtilProperties.getMessage(resource, "ProductCatalogs", (Locale) context.get("locale")) + "-" + name);
                    result.put("level", level);
                    result.put("type", "catalog");
                    result.put("parent", parent);
                    result.put("seq", seq);

                } else if ("category".equals(type)) {
                    id = id.substring(0, id.indexOf("_category"));
                    String prefix = UtilProperties.getMessage(resource, "ProductCategories", (Locale) context.get("locale")) + "-";
                    if (name.startsWith(prefix)) {
                        name = name.substring(prefix.length());
                    }
                    GenericValue category = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", id));
                    category.put("categoryName", name);
                    delegator.store(category);
                    result.put("id", id + "_category");
                    result.put("name", UtilProperties.getMessage(resource, "ProductCategories", (Locale) context.get("locale")) + "-" + name);
                    result.put("level", level);
                    result.put("type", "category");
                    result.put("parent", parent);
                    result.put("seq", seq);

                } else if ("product".equals(type)) {
                    id = id.substring(0, id.indexOf("_product"));
                    String prefix = UtilProperties.getMessage(resource, "ProductProducts", (Locale) context.get("locale")) + "-";
                    if (name.startsWith(prefix)) {
                        name = name.substring(prefix.length());
                    }
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", id));
                    product.put("productName", name);
                    delegator.store(product);
                    result.put("id", id + "_product");
                    result.put("name", UtilProperties.getMessage(resource, "ProductProducts", (Locale) context.get("locale")) + "-" + name);
                    result.put("level", level);
                    result.put("type", "product");
                    result.put("parent", parent);
                    result.put("seq", seq);
                }
            }


        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        return result;
    }

    public static Map<String, Object> productReviews(DispatchContext dcx, Map<String, ? extends Object> context) {
        String productId = (String) context.get("productId");
        String productStoreId = (String) context.get("productStroeId");
        Delegator delegator = dcx.getDelegator();
        GenericValue product = null;
        Map<String, Object> result = ServiceUtil.returnSuccess();
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));

            List<GenericValue> reviews = product.getRelatedCache("ProductReview", UtilMisc.toMap("statusId", "PRR_APPROVED", "productStroeId", productStoreId), UtilMisc.toList("-postedDateTime"));
            result.put("reviews", reviews);
            // get the average rating
            if (UtilValidate.isNotEmpty(reviews)) {
                List<GenericValue> ratingReviews = EntityUtil.filterByAnd(reviews, UtilMisc.toList(EntityCondition.makeCondition("productRating", EntityOperator.NOT_EQUAL, null)));
                if (UtilValidate.isNotEmpty(ratingReviews)) {
                    BigDecimal averageRating = ProductWorker.getAverageProductRating(product, reviews, productStoreId);
                    result.put("averageRating", averageRating);
                    Integer numRatings = ratingReviews.size();
                    result.put("numRatings", numRatings);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }


    public static Map<String, Object> preCreateProduct(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        GenericValue product = delegator.makeValue("Product");
        product.setNextSeqId();
        result.put("product", product);
        return result;

    }

   
    /**
     * 新增用户对产品的收藏
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> addProductFavorite(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        String productId = (String) context.get("productId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        GenericValue favorite = delegator.makeValue("PartyFavoriteProduct");
        favorite.set("partyId", userLogin.get("partyId"));
        favorite.set("productId", productId);
        favorite.set("fromDate", UtilDateTime.nowTimestamp());


        try {

            GenericValue productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", productId));
            if (UtilValidate.isEmpty(productCalculatedInfo)) {
                productCalculatedInfo = delegator.makeValue("ProductCalculatedInfo");
                productCalculatedInfo.put("productId", productId);
                productCalculatedInfo.put("totalQuantityOrdered", new BigDecimal(0));
                productCalculatedInfo.put("totalPartyFavorite", new Long(1));
                productCalculatedInfo.put("totalTimesViewed", new Long(1));
                productCalculatedInfo.put("averageCustomerRating", new BigDecimal(0));
                productCalculatedInfo.create();

            } else {
                productCalculatedInfo.put("totalPartyFavorite", productCalculatedInfo.get("totalPartyFavorite") == null ? 1 : ((Long) productCalculatedInfo.get("totalPartyFavorite") + 1));
                productCalculatedInfo.store();
            }

            //增加虚拟产品的收藏
            GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
            String virtualProductId = ProductWorker.getVariantVirtualId(product);
            if (UtilValidate.isNotEmpty(virtualProductId)) {

                GenericValue pFavorite = delegator.makeValue("PartyFavoriteProduct");
                pFavorite.set("partyId", userLogin.get("partyId"));
                pFavorite.set("productId", virtualProductId);
                pFavorite.set("fromDate", UtilDateTime.nowTimestamp());
                delegator.create(pFavorite);
                productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", virtualProductId));
                if (UtilValidate.isEmpty(productCalculatedInfo)) {
                    productCalculatedInfo = delegator.makeValue("ProductCalculatedInfo");
                    productCalculatedInfo.put("productId", virtualProductId);
                    productCalculatedInfo.put("totalQuantityOrdered", new BigDecimal(0));
                    productCalculatedInfo.put("totalPartyFavorite", new Long(1));
                    productCalculatedInfo.put("totalTimesViewed", new Long(1));
                    productCalculatedInfo.put("averageCustomerRating", new BigDecimal(0));
                    productCalculatedInfo.create();

                } else {
                    productCalculatedInfo.put("totalPartyFavorite", productCalculatedInfo.get("totalPartyFavorite") == null ? 1 : ((Long) productCalculatedInfo.get("totalPartyFavorite") + 1));
                    productCalculatedInfo.store();
                }
            }

            favorite = delegator.create(favorite);
            result.put("resultData", favorite);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 查看收藏
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> viewProductFavorite(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        String productId = (String) context.get("productId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {
            List<GenericValue> favorites = EntityUtil.filterByDate(delegator.findByAnd("PartyFavoriteProduct", UtilMisc.toMap("productId", productId, "partyId", userLogin.get("partyId"))));
            result.put("resultData", favorites);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    /**
     * 取消收藏
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> disProductFavorite(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = ctx.getDelegator();
        String productId = (String) context.get("productId");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {
            delegator.removeByAnd("PartyFavoriteProduct", UtilMisc.toMap("productId", productId, "partyId", userLogin.get("partyId")));
            result.put("resultData", true);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }

    // Add by zhajh at 20151231 商品类型列表处理 Begin

    /**
     * 商品分类列表
     *

     * @return
     */
    public static Map<String, Object> getProductCategoryList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        List<GenericValue> productCategoryList = FastList.newInstance();
        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "id", "productCategoryId", null, false, false, null);
        dynamicView.addAlias("PC", "name", "categoryName", null, false, false, null);
        dynamicView.addAlias("PC", "pId", "primaryParentCategoryId", null, false, false, null);
        dynamicView.addAlias("PC", "isDel");

        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("name");
        fieldsToSelect.add("pId");
        fieldsToSelect.add("isDel");
        orderBy.add("pId");

        List<EntityCondition> entityConditionList = FastList.newInstance();
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));// 非删除的商品

        try {
            // 查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView,
                    EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, fieldsToSelect,
                    orderBy, findOpts);
            productCategoryList = pli.getCompleteList();
            // 关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
        }

        result.put("productCategoryList", productCategoryList);
        return result;
    }

    /**
     * 查询商品列表，弹出框使用 add by qianjin 2016/01/19
     *

     * @return
     */
    public static Map<String, Object> getProductListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 记录集合
        List<Map> recordsList = FastList.newInstance();

        // 总记录数
        int recordsListSize = 0;
        // 查询开始条数
        int lowIndex = 0;
        // 查询结束条数
        int highIndex = 0;

        // 跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        // 每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // 店铺编码
        String productStoreId = (String) context.get("productStoreId");

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // 查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // 排序字段集合
        List<String> orderBy = FastList.newInstance();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PR", "Product");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "productName");
        dynamicView.addAlias("PR", "isOnline");
        dynamicView.addAlias("PR", "primaryProductCategoryId");
        dynamicView.addAlias("PR", "productTypeId");
        dynamicView.addAlias("PR", "isVerify");
        dynamicView.addAlias("PR", "salesDiscontinuationDate");
        dynamicView.addAlias("PR", "isDel");
        dynamicView.addAlias("PR", "isVirtual");
        dynamicView.addAlias("PR", "mainProductId");


        dynamicView.addMemberEntity("PT", "ProductType");
        dynamicView.addAlias("PT", "productTypeId");
        dynamicView.addAlias("PT", "productTypeName", "description", null, false, null, null);
        dynamicView.addViewLink("PR", "PT", false, ModelKeyMap.makeKeyMapList("productTypeId", "productTypeId"));

        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "productCategoryId");
        dynamicView.addAlias("PC", "categoryName");
        dynamicView.addViewLink("PR", "PC", false,
                ModelKeyMap.makeKeyMapList("primaryProductCategoryId", "productCategoryId"));

        // 添加价格检索条件
        dynamicView.addMemberEntity("PP", "ProductPrice");
        dynamicView.addAlias("PP", "productPriceTypeId");
        dynamicView.addAlias("PP", "price");
        dynamicView.addViewLink("PR", "PP", false, ModelKeyMap.makeKeyMapList("productId", "productId"));

 
        fieldsToSelect.add("productId");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("primaryProductCategoryId");
        fieldsToSelect.add("productTypeName");
        fieldsToSelect.add("categoryName");
        fieldsToSelect.add("productTypeId");

        // 排序字段名称
        String sortField = "productId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        // 排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        // 添加必要查询条件
        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));

        // 按产品编号模糊查询
        if (UtilValidate.isNotEmpty(context.get("productId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productId") + "%")));
        }
        // 按商品名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("productName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productName") + "%")));
        }

        // 商品类型，商品类别 查询
        String productTypeId = (String) context.get("productTypeId");

        if (!UtilValidate.isEmpty(productTypeId)) {
            EntityCondition cond = EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, productTypeId);
            andExprs.add(cond);
        }

        String productCategoryId = (String) context.get("primaryProductCategoryId");
        if (!UtilValidate.isEmpty(productCategoryId)) {
            EntityCondition cond = EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.EQUALS,
                    productTypeId);
            andExprs.add(cond);
        }

        // 商品价格
        String productPriceStart = (String) context.get("productPriceStart");
        String productPriceEnd = (String) context.get("productPriceEnd");
        if (!UtilValidate.isEmpty(productPriceStart)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO,
                    new BigDecimal(productPriceStart)));
        }
        if (!UtilValidate.isEmpty(productPriceEnd)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO,
                    new BigDecimal(productPriceEnd)));
        }

        // 默认条件
        andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
        andExprs.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "Y"));
        // 平台的场合可以查看所有商品 （包括商家建的商品）
        if(UtilValidate.isNotEmpty(productStoreId)){
            if(!"10000".equals(productStoreId)){
                andExprs.add(EntityCondition.makeCondition("productStoreId",EntityOperator.EQUALS,productStoreId));
            }
        }

        // 销售结束日期大于等于今天，或者销售结束日期为空
        andExprs.add(EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO,
                                UtilDateTime.nowTimestamp()),
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null)),
                EntityOperator.OR));
        // 删除标识为Y，或则为空
        andExprs.add(
                EntityCondition.makeCondition(
                        UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.NOT_EQUAL, "Y"),
                                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null)),
                        EntityOperator.OR));

        // 非sku商品或sku商品（不包含sku的主商品）
        // Add by zhajh at 20180316 凯德项目 Begin
//        andExprs.add(
//                EntityCondition.makeCondition(
//                        UtilMisc.toList(EntityCondition.makeCondition("isSku", EntityOperator.EQUALS, null),
//                                EntityCondition.makeCondition("mainProductId", EntityOperator.NOT_EQUAL, null)),
//                        EntityOperator.OR));
        // Add by zhajh at 20180316 凯德项目 End

        // 添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // 去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // 查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
                    orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();

                String productId = gv.getString("productId");
                String productName = gv.getString("productName");
                String primaryProductCategoryId = gv.getString("primaryProductCategoryId");
                String categoryName = gv.getString("categoryName");

                String productTypeName = gv.getString("productTypeName");
                GenericValue productTypeInfo = delegator.findByPrimaryKey("ProductType",
                        UtilMisc.toMap("productTypeId", gv.getString("productTypeId")));
                if (UtilValidate.isNotEmpty(productTypeInfo)) {
                    productTypeName = (String) productTypeInfo.get("description", locale);
                }

                map.put("productId", productId);
                map.put("productName", productName);
                map.put("productTypeName", productTypeName);
                map.put("categoryName", categoryName);
                // 商品价格
                List<GenericValue> pp_list = delegator.findByAnd("ProductPrice",
                        UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
                String price = "";
                if (UtilValidate.isNotEmpty(pp_list)) {
                    GenericValue pp_gv = EntityUtil.getFirst(pp_list);
                    price = UtilMisc.doubleTrans(pp_gv.getBigDecimal("price"));
                }
                map.put("price", price);

                map.put("imgUrl", "");
                // 根据商品ID获取商品图片url，？图片类型？
                String productAdditionalImage1 = "";
                List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                        UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                    map.put("imgUrl", "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId"));
                }

                // 商品特征
                String productGoodFeature = "";
                String  curFeatureNames="";
                GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                if (UtilValidate.isNotEmpty(productInfo)) {
                    if("Y".equals(productInfo.getString("isVirtual"))|| UtilValidate.isNotEmpty(productInfo.getString("mainProductId"))) {
                        if (UtilValidate.isNotEmpty(productInfo.getString("mainProductId"))) {
                            if (UtilValidate.isNotEmpty(productInfo.getString("featureProductId"))) {

                                String[] curFeatureIds = productInfo.getString("featureProductId").split("\\|");
                                if (UtilValidate.isNotEmpty(curFeatureIds)) {
                                    for (String featureId : curFeatureIds) {
                                        GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", featureId));
                                        if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                            String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                            if (UtilValidate.isEmpty(curFeatureNames)) {
                                                curFeatureNames = productFeatureName;
                                            } else {
                                                curFeatureNames = curFeatureNames + ";" + productFeatureName;
                                            }
                                        }
                                    }
                                }

//                            GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productInfo.getString("featureProductId")));
//                            if (UtilValidate.isNotEmpty(productFeatureInfo)) {
//                                GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
//                                if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
//                                    String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
//                                    String productFeatureName = productFeatureInfo.getString("productFeatureName");
//                                    productGoodFeature = productFeatureTypeName + ":" + productFeatureName;
//                                }
//                            }
                            }
                        } else {
                            curFeatureNames = "特征主商品";
                        }
                    }
                }
                map.put("productGoodFeature", curFeatureNames);
                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }

            // 关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    public static Map<String, Object> getProductWithoutVirtualListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 记录集合
        List<Map> recordsList = FastList.newInstance();

        // 总记录数
        int recordsListSize = 0;
        // 查询开始条数
        int lowIndex = 0;
        // 查询结束条数
        int highIndex = 0;

        // 跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        // 每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // 店铺编码
        String productStoreId = (String) context.get("productStoreId");

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // 查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // 排序字段集合
        List<String> orderBy = FastList.newInstance();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PR", "Product");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "productName");
        dynamicView.addAlias("PR", "isOnline");
        dynamicView.addAlias("PR", "primaryProductCategoryId");
        dynamicView.addAlias("PR", "productTypeId");
        dynamicView.addAlias("PR", "isVerify");
        dynamicView.addAlias("PR", "salesDiscontinuationDate");
        dynamicView.addAlias("PR", "isDel");
        dynamicView.addAlias("PR", "isVirtual");
        dynamicView.addAlias("PR", "mainProductId");


        dynamicView.addMemberEntity("PT", "ProductType");
        dynamicView.addAlias("PT", "productTypeId");
        dynamicView.addAlias("PT", "productTypeName", "description", null, false, null, null);
        dynamicView.addViewLink("PR", "PT", false, ModelKeyMap.makeKeyMapList("productTypeId", "productTypeId"));

        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "productCategoryId");
        dynamicView.addAlias("PC", "categoryName");
        dynamicView.addViewLink("PR", "PC", false,
                ModelKeyMap.makeKeyMapList("primaryProductCategoryId", "productCategoryId"));

        // 添加价格检索条件
        dynamicView.addMemberEntity("PP", "ProductPrice");
        dynamicView.addAlias("PP", "productPriceTypeId");
        dynamicView.addAlias("PP", "price");
        dynamicView.addViewLink("PR", "PP", false, ModelKeyMap.makeKeyMapList("productId", "productId"));
 
        fieldsToSelect.add("productId");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("primaryProductCategoryId");
        fieldsToSelect.add("productTypeName");
        fieldsToSelect.add("categoryName");
        fieldsToSelect.add("productTypeId");

        // 排序字段名称
        String sortField = "productId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        // 排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        // 添加必要查询条件
        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));
        andExprs.add(EntityCondition.makeCondition("productTypeId", "FINISHED_GOOD"));
        andExprs.add(EntityCondition.makeCondition("productStoreId",EntityOperator.EQUALS,productStoreId));

        // 按产品编号模糊查询
        if (UtilValidate.isNotEmpty(context.get("productId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productId") + "%")));
        }
        // 按商品名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("productName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productName") + "%")));
        }

        // 商品类型，商品类别 查询
        String productTypeId = (String) context.get("productTypeId");

        if (!UtilValidate.isEmpty(productTypeId)) {
            EntityCondition cond = EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, productTypeId);
            andExprs.add(cond);
        }

        String productCategoryId = (String) context.get("primaryProductCategoryId");
        if (!UtilValidate.isEmpty(productCategoryId)) {
            EntityCondition cond = EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.EQUALS,
                    productTypeId);
            andExprs.add(cond);
        }

        // 商品价格
        String productPriceStart = (String) context.get("productPriceStart");
        String productPriceEnd = (String) context.get("productPriceEnd");
        if (!UtilValidate.isEmpty(productPriceStart)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO,
                    new BigDecimal(productPriceStart)));
        }
        if (!UtilValidate.isEmpty(productPriceEnd)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO,
                    new BigDecimal(productPriceEnd)));
        }

        // 默认条件
        andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
        andExprs.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "Y"));
        // 销售结束日期大于等于今天，或者销售结束日期为空
        andExprs.add(EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO,
                                UtilDateTime.nowTimestamp()),
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null)),
                EntityOperator.OR));
        // 删除标识为Y，或则为空
        andExprs.add(
                EntityCondition.makeCondition(
                        UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.NOT_EQUAL, "Y"),
                                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null)),
                        EntityOperator.OR));

        // 非sku商品或sku商品（不包含sku的主商品）
        // Add by zhajh at 20180316 凯德项目 Begin
        andExprs.add(
                EntityCondition.makeCondition(
                        UtilMisc.toList(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, null),
                                EntityCondition.makeCondition("mainProductId", EntityOperator.NOT_EQUAL, null)),
                        EntityOperator.OR));
        // Add by zhajh at 20180316 凯德项目 End

        // 添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // 去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // 查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
                    orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();

                String productId = gv.getString("productId");
                String productName = gv.getString("productName");
                String primaryProductCategoryId = gv.getString("primaryProductCategoryId");
                String categoryName = gv.getString("categoryName");

                String productTypeName = gv.getString("productTypeName");
                GenericValue productTypeInfo = delegator.findByPrimaryKey("ProductType",
                        UtilMisc.toMap("productTypeId", gv.getString("productTypeId")));
                if (UtilValidate.isNotEmpty(productTypeInfo)) {
                    productTypeName = (String) productTypeInfo.get("description", locale);
                }

                map.put("productId", productId);
                map.put("productName", productName);
                map.put("productTypeName", productTypeName);
                map.put("categoryName", categoryName);
                // 商品价格
                List<GenericValue> pp_list = delegator.findByAnd("ProductPrice",
                        UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
                String price = "";
                if (UtilValidate.isNotEmpty(pp_list)) {
                    GenericValue pp_gv = EntityUtil.getFirst(pp_list);
                    price = UtilMisc.doubleTrans(pp_gv.getBigDecimal("price"));
                }
                map.put("price", price);

                map.put("imgUrl", "");
                // 根据商品ID获取商品图片url，？图片类型？
                String productAdditionalImage1 = "";
                List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                        UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                    map.put("imgUrl", "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId"));
                }

                // 商品特征
                String productGoodFeature = "";
                GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));

                if (UtilValidate.isNotEmpty(productInfo)) {
                    if (UtilValidate.isNotEmpty(productInfo.getString("mainProductId"))) {
                        if (UtilValidate.isNotEmpty(productInfo.getString("featureProductId"))) {
                            GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productInfo.getString("featureProductId")));
                            if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                                if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                    String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                    String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                    productGoodFeature = productFeatureTypeName + ":" + productFeatureName;
                                }
                            }
                        }
                    }
                }
                map.put("productGoodFeature", productGoodFeature);
                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }

            // 关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 查询商品列表，弹出框使用 add by qianjin 2016/01/19
     *

     * @return
     */
    public static Map<String, Object> getUnUsedProductListForModal(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 记录集合
        List<Map> recordsList = FastList.newInstance();

        String productStoreId = (String) context.get("productStoreId");
        // 总记录数
        int recordsListSize = 0;
        // 查询开始条数
        int lowIndex = 0;
        // 查询结束条数
        int highIndex = 0;

        // 跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        // 每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // 查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // 排序字段集合
        List<String> orderBy = FastList.newInstance();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PR", "Product");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "productName");
        dynamicView.addAlias("PR", "isOnline");
        dynamicView.addAlias("PR", "primaryProductCategoryId");
        dynamicView.addAlias("PR", "productTypeId");
        dynamicView.addAlias("PR", "isVerify");
        dynamicView.addAlias("PR", "salesDiscontinuationDate");
        dynamicView.addAlias("PR", "isDel");
        dynamicView.addAlias("PR", "isVirtual");
        dynamicView.addAlias("PR", "mainProductId");
        dynamicView.addAlias("PR","productStoreId");

        dynamicView.addMemberEntity("PT", "ProductType");
        dynamicView.addAlias("PT", "productTypeId");
        dynamicView.addAlias("PT", "productTypeName", "description", null, false, null, null);
        dynamicView.addViewLink("PR", "PT", false, ModelKeyMap.makeKeyMapList("productTypeId", "productTypeId"));

        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "productCategoryId");
        dynamicView.addAlias("PC", "categoryName");
        dynamicView.addViewLink("PR", "PC", false,
                ModelKeyMap.makeKeyMapList("primaryProductCategoryId", "productCategoryId"));

        // 添加价格检索条件
        dynamicView.addMemberEntity("PP", "ProductPrice");
        dynamicView.addAlias("PP", "productPriceTypeId");
        dynamicView.addAlias("PP", "price");
        dynamicView.addViewLink("PR", "PP", false, ModelKeyMap.makeKeyMapList("productId", "productId"));
 
        fieldsToSelect.add("productId");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("primaryProductCategoryId");
        fieldsToSelect.add("productTypeName");
        fieldsToSelect.add("categoryName");
        fieldsToSelect.add("productTypeId");

        // 排序字段名称
        String sortField = "productId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        // 排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);
        //已经参加团购，秒杀或者直降的产品id集合，不显示出来
        String startDate = (String) context.get("startDate"); //开始时间
        String endDate = (String) context.get("endDate"); //结束时间
        Timestamp tStartDate=Timestamp.valueOf(startDate);
        Timestamp tEndDate=Timestamp.valueOf(endDate);
        List<String> productIds = getUnUsedProductIds(delegator, productStoreId,tStartDate,tEndDate);
        if (productIds.size() > 0) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.NOT_IN, productIds));
        }

        // 添加必要查询条件
        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));

        // 按产品编号模糊查询
        if (UtilValidate.isNotEmpty(context.get("productId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productId") + "%")));
        }
        // 按商品名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("productName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productName") + "%")));
        }

        // 商品类型，商品类别 查询
        String productTypeId = (String) context.get("productTypeId");

        if (!UtilValidate.isEmpty(productTypeId)) {
            EntityCondition cond = EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, productTypeId);
            andExprs.add(cond);
        }

        String productCategoryId = (String) context.get("primaryProductCategoryId");
        if (!UtilValidate.isEmpty(productCategoryId)) {
            EntityCondition cond = EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.EQUALS,
                    productTypeId);
            andExprs.add(cond);
        }

        // 商品价格
        String productPriceStart = (String) context.get("productPriceStart");
        String productPriceEnd = (String) context.get("productPriceEnd");
        if (!UtilValidate.isEmpty(productPriceStart)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO,
                    new BigDecimal(productPriceStart)));
        }
        if (!UtilValidate.isEmpty(productPriceEnd)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO,
                    new BigDecimal(productPriceEnd)));
        }

        // 默认条件
        andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
        andExprs.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "Y"));
        andExprs.add(EntityCondition.makeCondition("productStoreId",EntityOperator.EQUALS,productStoreId));
        // 销售结束日期大于等于今天，或者销售结束日期为空
        andExprs.add(EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO,
                                UtilDateTime.nowTimestamp()),
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null)),
                EntityOperator.OR));
        // 删除标识为Y，或则为空
        andExprs.add(
                EntityCondition.makeCondition(
                        UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.NOT_EQUAL, "Y"),
                                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null)),
                        EntityOperator.OR));

        // 非sku商品或sku商品（不包含sku的主商品）
        // Add by zhajh at 20180316 凯德项目 Begin
        andExprs.add(
                EntityCondition.makeCondition(
                        UtilMisc.toList(EntityCondition.makeCondition("isVirtual", EntityOperator.EQUALS, null),
                                EntityCondition.makeCondition("mainProductId", EntityOperator.NOT_EQUAL, null)),
                        EntityOperator.OR));
        // Add by zhajh at 20180316 凯德项目 End

        // 添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // 去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // 查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
                    orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();

                String productId = gv.getString("productId");
                String productName = gv.getString("productName");
                String primaryProductCategoryId = gv.getString("primaryProductCategoryId");
                String categoryName = gv.getString("categoryName");

                String productTypeName = gv.getString("productTypeName");
                GenericValue productTypeInfo = delegator.findByPrimaryKey("ProductType",
                        UtilMisc.toMap("productTypeId", gv.getString("productTypeId")));
                if (UtilValidate.isNotEmpty(productTypeInfo)) {
                    productTypeName = (String) productTypeInfo.get("description", locale);
                }

                map.put("productId", productId);
                map.put("productName", productName);
                map.put("productTypeName", productTypeName);
                map.put("categoryName", categoryName);
                // 商品价格
                List<GenericValue> pp_list = delegator.findByAnd("ProductPrice",
                        UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
                String price = "";
                if (UtilValidate.isNotEmpty(pp_list)) {
                    GenericValue pp_gv = EntityUtil.getFirst(pp_list);
                    price = UtilMisc.doubleTrans(pp_gv.getBigDecimal("price"));
                }
                map.put("price", price);

                map.put("imgUrl", "");
                // 根据商品ID获取商品图片url，？图片类型？
                String productAdditionalImage1 = "";
                List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                        UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                    map.put("imgUrl", "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId"));
                }

                // 商品特征
                String productGoodFeature = "";
                GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));

                if (UtilValidate.isNotEmpty(productInfo)) {
                    if (UtilValidate.isNotEmpty(productInfo.getString("mainProductId"))) {
                        if (UtilValidate.isNotEmpty(productInfo.getString("featureProductId"))) {
                            GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productInfo.getString("featureProductId")));
                            if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                                if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                    String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                    String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                    productGoodFeature = productFeatureTypeName + ":" + productFeatureName;
                                }
                            }
                        }
                    }
                }
                map.put("productGoodFeature", productGoodFeature);
                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }

            // 关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    public static List<String> getUnUsedProductIds(Delegator delegator, String productStoreId) throws GenericEntityException {

        List<String> productIds = FastList.newInstance();
        //校验在直降，秒杀，团购中是否已经出现。（有效期内）
        //在规定的时间内
        Timestamp nowTime = UtilDateTime.nowTimestamp();
        //直降
        // 满减、满赠、直降、折扣中的商品
        List<String> promoTypeList=FastList.newInstance();// 促销类型列表
        promoTypeList.add("PROMO_REDUCE"); // 满减
        promoTypeList.add("PROMO_PRE_REDUCE");// 满减
        promoTypeList.add("PROMO_GIFT");// 满赠
        promoTypeList.add("PROMO_SPE_PRICE");// 直降
        promoTypeList.add("PROMO_DISCOUNT");// 折扣

        List andExprs = FastList.newInstance();
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoType"), EntityOperator.EQUALS, "PROMO_SPE_PRICE"));//类型为直降
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoType"), EntityOperator.IN, promoTypeList));//
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));//

        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);

        EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, null, null, null);
        List<GenericValue> promoList = pli.getCompleteList();
        pli.close();
        if (promoList != null && promoList.size() > 0) {
            for (GenericValue promo : promoList) {
                String productPromoId = promo.getString("productPromoId");
//                List<GenericValue> actions = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId));
//                if (actions != null && actions.size() > 0) {
//                    for (GenericValue act : actions) {
//                        String productId = act.getString("productId");
//                        productIds.add(productId);
//                    }
//                }

                List<GenericValue> products = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
                if (products != null && products.size() > 0) {
                    for (GenericValue product : products) {
                        String productId ="";
                        if(UtilValidate.isNotEmpty(product.get("productId"))){
                            productId=product.getString("productId");
                            productIds.add(productId);
                        }
                    }
                }

            }
        }

        //检查是否被其他团购或者秒杀使用  ProductStorePromoAndAct
        List<EntityCondition> andExprs2 = FastList.newInstance();
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.LESS_THAN, nowTime));
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.GREATER_THAN, nowTime));
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        EntityCondition mainCond2 = EntityCondition.makeCondition(andExprs2, EntityOperator.AND);

        EntityListIterator pli2 = delegator.find("ProductStorePromoAndAct", mainCond2, null, null, null, null);
        List<GenericValue> actList = pli2.getCompleteList();
        pli2.close();
        if (actList != null && actList.size() > 0) {
            for (GenericValue act : actList) {
                String activityId = act.getString("activityId");
                List<GenericValue> acts = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
                if (acts != null && acts.size() > 0) {
                    for (GenericValue act1 : acts) {
                        String productId = "";
                        if(UtilValidate.isNotEmpty(act1.get("productId"))){
                            productId = act1.getString("productId");
                            productIds.add(productId);
                        }
                    }
                }
            }
        }


        // 检查是否被组合商品使用
        List<EntityCondition> andExprs3 = FastList.newInstance();
        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//
        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        EntityCondition mainCond3 = EntityCondition.makeCondition(andExprs3, EntityOperator.AND);

        EntityListIterator pli3 = delegator.find("ProductStorePromoAndGrp", mainCond3, null, null, null, null);
        List<GenericValue> grpList = pli3.getCompleteList();
        pli3.close();
        if (grpList != null && grpList.size() > 0) {
            for (GenericValue grp : grpList) {
                String productGrpId = grp.getString("productGrpId");
                List<GenericValue> grpProductInfos = delegator.findByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", productGrpId));
                if (grpProductInfos != null && grpProductInfos.size() > 0) {
                    for (GenericValue grpProductInfo : grpProductInfos) {
                        String productId = "";
                        if(UtilValidate.isNotEmpty(grpProductInfo.get("productId"))){
                            productId=grpProductInfo.getString("productId");
                            productIds.add(productId);
                        }

                    }
                }
            }
        }
        return productIds;
    }

    /**
     * 看看这个时间段是否有全场通用的促销
     * @param delegator
     * @param productStoreId
     * @param startDate
     * @param endDate
     * @return
     * @throws GenericEntityException
     */
    public static boolean isPromoAllExist(Delegator delegator, String productStoreId,Timestamp startDate,Timestamp endDate) throws GenericEntityException {
        Timestamp nowTime = UtilDateTime.nowTimestamp();

        // 满减、满赠、折扣中的商品
        List<String> promoTypeList=FastList.newInstance();// 促销类型列表
        promoTypeList.add("PROMO_REDUCE"); // 满减
        promoTypeList.add("PROMO_PRE_REDUCE");// 满减
        promoTypeList.add("PROMO_DISCOUNT");// 折扣
        promoTypeList.add("PROMO_GIFT");// 满赠

        List andExprs = FastList.newInstance();
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoType"), EntityOperator.EQUALS, "PROMO_SPE_PRICE"));//类型为直降
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoType"), EntityOperator.IN, promoTypeList));//
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));//
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoProductType"), EntityOperator.EQUALS, "PROMO_PRT_ALL"));//

        EntityCondition andCond1=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, startDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, startDate)),
                EntityOperator.AND);

        EntityCondition andCond2=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, endDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, endDate)),
                EntityOperator.AND);
        EntityCondition andCond7=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN, startDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.LESS_THAN, endDate)),
                EntityOperator.AND);

        EntityCondition orCond=EntityCondition.makeCondition(
                UtilMisc.toList(andCond1, andCond2,andCond7), EntityOperator.OR);
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        andExprs.add(orCond);
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);

        EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, null, null, null);
        List<GenericValue> promoList = pli.getCompleteList();
        pli.close();
        if (promoList != null && promoList.size() > 0) {
            return true;
        }

        return false;

    }
    public static List<String> getUnUsedProductIds(Delegator delegator, String productStoreId,Timestamp startDate,Timestamp endDate) throws GenericEntityException {

        List<String> productIds = FastList.newInstance();
        //校验在直降，秒杀，团购中是否已经出现。（有效期内）
        //在规定的时间内
        Timestamp nowTime = UtilDateTime.nowTimestamp();
        //直降
        // 满减、满赠、直降、折扣中的商品
        List<String> promoTypeList=FastList.newInstance();// 促销类型列表
        promoTypeList.add("PROMO_REDUCE"); // 满减
        promoTypeList.add("PROMO_PRE_REDUCE");// 满减
        promoTypeList.add("PROMO_GIFT");// 满赠
        promoTypeList.add("PROMO_SPE_PRICE");// 直降
        promoTypeList.add("PROMO_DISCOUNT");// 折扣

        List andExprs = FastList.newInstance();
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoType"), EntityOperator.EQUALS, "PROMO_SPE_PRICE"));//类型为直降
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoType"), EntityOperator.IN, promoTypeList));//
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//
        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));//

        EntityCondition andCond1=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, startDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, startDate)),
                EntityOperator.AND);

        EntityCondition andCond2=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, endDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, endDate)),
                EntityOperator.AND);
        EntityCondition andCond7=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN, startDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.LESS_THAN, endDate)),
                EntityOperator.AND);


        EntityCondition orCond=EntityCondition.makeCondition(
                UtilMisc.toList(andCond1, andCond2,andCond7), EntityOperator.OR);
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
//        andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        andExprs.add(orCond);
        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);

        EntityListIterator pli = delegator.find("ProductStorePromoAndAppl", mainCond, null, null, null, null);
        List<GenericValue> promoList = pli.getCompleteList();
        pli.close();
        if (promoList != null && promoList.size() > 0) {
            for (GenericValue promo : promoList) {
                String productPromoId = promo.getString("productPromoId");
//                List<GenericValue> actions = delegator.findByAnd("ProductPromoAction", UtilMisc.toMap("productPromoId", productPromoId));
//                if (actions != null && actions.size() > 0) {
//                    for (GenericValue act : actions) {
//                        String productId = act.getString("productId");
//                        productIds.add(productId);
//                    }
//                }

                List<GenericValue> products = delegator.findByAnd("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoId));
                if (products != null && products.size() > 0) {
                    for (GenericValue product : products) {
                        String productId ="";
                        if(UtilValidate.isNotEmpty(product.get("productId"))){
                            productId=product.getString("productId");
                            productIds.add(productId);
                        }
                    }
                }

            }
        }

        //检查是否被其他团购或者秒杀使用  ProductStorePromoAndAct
        List<EntityCondition> andExprs2 = FastList.newInstance();
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//


        EntityCondition andCond3=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN, startDate),
                        EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN, startDate)),
                EntityOperator.AND);

        EntityCondition andCond4=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN, endDate),
                        EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN, endDate)),
                EntityOperator.AND);

        EntityCondition andCond8=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("activityStartDate", EntityOperator.GREATER_THAN, startDate),
                        EntityCondition.makeCondition("activityEndDate", EntityOperator.LESS_THAN, endDate)),
                EntityOperator.AND);

        EntityCondition orCond2=EntityCondition.makeCondition(
                UtilMisc.toList(andCond3, andCond4,andCond8), EntityOperator.OR);
        andExprs2.add(orCond2);
//        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.LESS_THAN, nowTime));
//        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.GREATER_THAN, nowTime));
        andExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        EntityCondition mainCond2 = EntityCondition.makeCondition(andExprs2, EntityOperator.AND);

        EntityListIterator pli2 = delegator.find("ProductStorePromoAndAct", mainCond2, null, null, null, null);
        List<GenericValue> actList = pli2.getCompleteList();
        pli2.close();
        if (actList != null && actList.size() > 0) {
            for (GenericValue act : actList) {
                String activityId = act.getString("activityId");
                List<GenericValue> acts = delegator.findByAnd("ProductActivityGoods", UtilMisc.toMap("activityId", activityId));
                if (acts != null && acts.size() > 0) {
                    for (GenericValue act1 : acts) {
                        String productId = "";
                        if(UtilValidate.isNotEmpty(act1.get("productId"))){
                            productId = act1.getString("productId");
                            productIds.add(productId);
                        }
                    }
                }
            }
        }


        // 检查是否被组合商品使用
        List<EntityCondition> andExprs3 = FastList.newInstance();
        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productStoreId"), EntityOperator.EQUALS, productStoreId));//
        EntityCondition andCond5=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, startDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, startDate)),
                EntityOperator.AND);

        EntityCondition andCond6=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, endDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, endDate)),
                EntityOperator.AND);
        EntityCondition andCond9=EntityCondition.makeCondition(
                UtilMisc.toList(EntityCondition.makeCondition("fromDate", EntityOperator.GREATER_THAN, startDate),
                        EntityCondition.makeCondition("thruDate", EntityOperator.LESS_THAN, endDate)),
                EntityOperator.AND);

        EntityCondition orCond3=EntityCondition.makeCondition(
                UtilMisc.toList(andCond5, andCond6,andCond9), EntityOperator.OR);
        andExprs3.add(orCond3);

//        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN, nowTime));
//        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("thruDate"), EntityOperator.GREATER_THAN, nowTime));
        andExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("promoStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        EntityCondition mainCond3 = EntityCondition.makeCondition(andExprs3, EntityOperator.AND);

        EntityListIterator pli3 = delegator.find("ProductStorePromoAndGrp", mainCond3, null, null, null, null);
        List<GenericValue> grpList = pli3.getCompleteList();
        pli3.close();
        if (grpList != null && grpList.size() > 0) {
            for (GenericValue grp : grpList) {
                String productGrpId = grp.getString("productGrpId");
                List<GenericValue> grpProductInfos = delegator.findByAnd("ProGrpPromoProduct", UtilMisc.toMap("productGrpId", productGrpId));
                if (grpProductInfos != null && grpProductInfos.size() > 0) {
                    for (GenericValue grpProductInfo : grpProductInfos) {
                        String productId = "";
                        if(UtilValidate.isNotEmpty(grpProductInfo.get("productId"))){
                            productId=grpProductInfo.getString("productId");
                            productIds.add(productId);
                        }

                    }
                }
            }
        }
        return productIds;
    }


    /**
     * 查询直降促销商品弹出框 add by changchen 20180305
     *

     * @return getProductPromoListForModal
     */
    public static Map<String, Object> getProductPromoListForModal(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        // LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        // 记录集合
        List<Map> recordsList = FastList.newInstance();

        // 总记录数
        int recordsListSize = 0;
        // 查询开始条数
        int lowIndex = 0;
        // 查询结束条数
        int highIndex = 0;

        // 跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        // 每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // 动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // 查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // 排序字段集合
        List<String> orderBy = FastList.newInstance();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PR", "Product");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "productName");
        dynamicView.addAlias("PR", "isOnline");
        dynamicView.addAlias("PR", "primaryProductCategoryId");
        dynamicView.addAlias("PR", "productTypeId");
        dynamicView.addAlias("PR", "isVerify");
        dynamicView.addAlias("PR", "salesDiscontinuationDate");
        dynamicView.addAlias("PR", "isDel");

        dynamicView.addMemberEntity("PT", "ProductType");
        dynamicView.addAlias("PT", "productTypeId");
        dynamicView.addAlias("PT", "productTypeName", "description", null, false, null, null);
        dynamicView.addViewLink("PR", "PT", false, ModelKeyMap.makeKeyMapList("productTypeId", "productTypeId"));

        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "productCategoryId");
        dynamicView.addAlias("PC", "categoryName");
        dynamicView.addViewLink("PR", "PC", false,
                ModelKeyMap.makeKeyMapList("primaryProductCategoryId", "productCategoryId"));

        // 添加价格检索条件
        dynamicView.addMemberEntity("PP", "ProductPrice");
        dynamicView.addAlias("PP", "productPriceTypeId");
        dynamicView.addAlias("PP", "price");
        dynamicView.addViewLink("PR", "PP", false, ModelKeyMap.makeKeyMapList("productId", "productId"));

        fieldsToSelect.add("productId");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("primaryProductCategoryId");
        fieldsToSelect.add("productTypeName");
        fieldsToSelect.add("categoryName");
        fieldsToSelect.add("productTypeId");

        // 排序字段名称
        String sortField = "productId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        // 排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        // 添加必要查询条件
        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));

        // 按产品编号模糊查询
        if (UtilValidate.isNotEmpty(context.get("productId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productId") + "%")));
        }
        // 按商品名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("productName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("productName") + "%")));
        }

        // 商品类型，商品类别 查询
        String productTypeId = (String) context.get("productTypeId");

        if (!UtilValidate.isEmpty(productTypeId)) {
            EntityCondition cond = EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, productTypeId);
            andExprs.add(cond);
        }

        String productCategoryId = (String) context.get("primaryProductCategoryId");
        if (!UtilValidate.isEmpty(productCategoryId)) {
            EntityCondition cond = EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.EQUALS,
                    productTypeId);
            andExprs.add(cond);
        }

        // 商品价格
        String productPriceStart = (String) context.get("productPriceStart");
        String productPriceEnd = (String) context.get("productPriceEnd");
        if (!UtilValidate.isEmpty(productPriceStart)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO,
                    new BigDecimal(productPriceStart)));
        }
        if (!UtilValidate.isEmpty(productPriceEnd)) {
            andExprs.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO,
                    new BigDecimal(productPriceEnd)));
        }

        // 默认条件
        andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
        andExprs.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "Y"));
        // 销售结束日期大于等于今天，或者销售结束日期为空
        andExprs.add(EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO,
                                UtilDateTime.nowTimestamp()),
                        EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null)),
                EntityOperator.OR));
        // 删除标识为Y，或则为空
        andExprs.add(
                EntityCondition.makeCondition(
                        UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.NOT_EQUAL, "Y"),
                                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null)),
                        EntityOperator.OR));

        // 添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            // 去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // 查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
                    orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();

                String productId = gv.getString("productId");
                String productName = gv.getString("productName");
                String primaryProductCategoryId = gv.getString("primaryProductCategoryId");
                String categoryName = gv.getString("categoryName");

                String productTypeName = gv.getString("productTypeName");
                GenericValue productTypeInfo = delegator.findByPrimaryKey("ProductType",
                        UtilMisc.toMap("productTypeId", gv.getString("productTypeId")));
                if (UtilValidate.isNotEmpty(productTypeInfo)) {
                    productTypeName = (String) productTypeInfo.get("description", locale);
                }

                map.put("productId", productId);
                map.put("productName", productName);
                map.put("productTypeName", productTypeName);
                map.put("categoryName", categoryName);
                // 商品价格
                List<GenericValue> pp_list = delegator.findByAnd("ProductPrice",
                        UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
                String price = "";
                if (UtilValidate.isNotEmpty(pp_list)) {
                    GenericValue pp_gv = EntityUtil.getFirst(pp_list);
                    price = UtilMisc.doubleTrans(pp_gv.getBigDecimal("price"));
                }
                map.put("price", price);

                map.put("imgUrl", "");
                // 根据商品ID获取商品图片url，？图片类型？
                String productAdditionalImage1 = "";
                List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                        UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                    map.put("imgUrl", "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId"));
                }
                recordsList.add(map);
            }

            // 获取总记录数
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }

            // 关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }

        // 返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", Integer.valueOf(recordsListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    // Add by zhajh at 20151222 查找商品品牌 Begin
    public static Map<String, Object> findProductBrands(DispatchContext dct, Map<String, ? extends Object> context) {
        Delegator delegator = dct.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }

//        String orderFiled = (String) context.get("ORDER_FILED");
//        String orderFiledBy = (String) context.get("ORDER_BY");

        String orderFiled = (String) context.get("sortField");
        String orderFiledBy = (String) context.get("sortType");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        String brandName = (String) context.get("brandName");// 品牌名称
        String brandNameAlias = (String) context.get("brandNameAlias");// 品牌别名

        List<GenericValue> productBrandList = FastList.newInstance();
        int productBrandListSize = 0;
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
        // blank param list
        String paramList = "";

        DynamicViewEntity dynamicView = new DynamicViewEntity();

        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();

        // default view settings
        dynamicView.addMemberEntity("PB", "ProductBrand");
        dynamicView.addAliasAll("PB", "", null);
        dynamicView.addAlias("PB", "productBrandId");
        dynamicView.addAlias("PB", "brandName");
        dynamicView.addAlias("PB", "brandNameAlias");
        dynamicView.addAlias("PB", "isUsed");
        dynamicView.addAlias("PB", "contentId");
        dynamicView.addAlias("PB", "createdStamp");
        fieldsToSelect.add("productBrandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("isUsed");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("createdStamp");

        // mod 排序问题的修改 at 20160222
        if (UtilValidate.isNotEmpty(orderFiled)) {
//            orderBy.add(orderFiled + " " + orderFiledBy);
            if ("desc".equals(orderFiledBy)) {
                orderBy.add("-" + orderFiled);
            } else {
            }
        } else {
            orderBy.add("-createdStamp");
        }

        //查询
        andExprs.add(EntityCondition.makeCondition("auditStatus", "1"));
        // 品牌名称
        if (UtilValidate.isNotEmpty(brandName)) {
            paramList = paramList + "&brandName=" + brandName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + brandName + "%")));
        }
        // 品牌别名
        if (UtilValidate.isNotEmpty(brandNameAlias)) {
            paramList = paramList + "&brandNameAlias=" + brandNameAlias;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandNameAlias"), EntityOperator.LIKE, EntityFunction.UPPER("%" + brandNameAlias + "%")));
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
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

                // get the partial list for this page
                productBrandList = pli.getPartialList(lowIndex, viewSize);

                // attempt to get the full size
                productBrandListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > productBrandListSize) {
                    highIndex = productBrandListSize;
                }

                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in productBrand find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            productBrandListSize = 0;
        }
        result.put("productBrandList", productBrandList);
        result.put("productBrandListSize", Integer.valueOf(productBrandListSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }


    /**
     * 商品品牌的查询处理
     *

     * @return
     */
    public static Map<String, Object> findProductBrands1(DispatchContext dct, Map<String, ? extends Object> context) {
        Delegator delegator = dct.getDelegator();
        DynamicViewEntity dve = new DynamicViewEntity();
        String paramString = "";
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
        int productBrandCount = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // get the index for the partial list
        lowIndex = viewIndex * viewSize + 1;
        highIndex = (viewIndex + 1) * viewSize;
        dve.addMemberEntity("PB", "ProductBrand");
        dve.addAliasAll("PB", "", null);
        List<String> orderBy = FastList.newInstance();
        orderBy.add("productBrandId");
        List<EntityCondition> entityConditionList = FastList.newInstance();
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
        List<GenericValue> productBrandList = FastList.newInstance();
        EntityListIterator eli = null;
        String brandName = (String) context.get("brandName");// 品牌名称
        String brandNameAlias = (String) context.get("brandNameAlias");// 品牌别名
        entityConditionList.add(EntityCondition.makeCondition("isUsed", "Y"));
        //查询
        // 品牌名称
        if (UtilValidate.isNotEmpty(brandName)) {
            entityConditionList.add(EntityCondition.makeCondition("brandName", brandName));
        }
        // 品牌别名
        if (UtilValidate.isNotEmpty(brandNameAlias)) {
            entityConditionList.add(EntityCondition.makeCondition("brandNameAlias", brandNameAlias));
        }
        // do the lookup
        try {
            eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, orderBy, findOpts);
            productBrandCount = eli.getResultsSizeAfterPartialList();
            if (productBrandCount > viewSize) {
                productBrandList = eli.getPartialList(lowIndex, viewSize);
            } else if (productBrandCount > 0) {
                productBrandList = eli.getCompleteList();
            }
            if (highIndex > productBrandCount) {
                highIndex = productBrandCount;
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        result.put("viewIndex", viewIndex);
        result.put("viewSize", viewSize);
        result.put("productBrandList", productBrandList);
        result.put("productBrandListSize", productBrandCount);
        result.put("paramList", (paramString == null) ? "" : paramString);
        result.put("viewIndexFirst", 0);
        return result;
    }
    // Add by zhajh at 20151222 查找商品品牌 End


    // Add by zhajh at 20151222 商品品牌的更新处理 Begin
    public static Map<String, Object> updatePr(DispatchContext dctx, Map<String, ? extends Object> context) {
        // * String productId      -- Product ID to find
        // * String type           -- Type of feature (STANDARD_FEATURE, SELECTABLE_FEATURE)
        // * String distinct       -- Distinct feature (SIZE, COLOR)
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        String productId = (String) context.get("productId");
        String distinct = (String) context.get("distinct");
        String type = (String) context.get("type");
        Locale locale = (Locale) context.get("locale");
        String errMsg = null;
        List<GenericValue> features = null;

        try {
            Map<String, String> fields = UtilMisc.toMap("productId", productId);
            List<String> order = UtilMisc.toList("sequenceNum", "productFeatureTypeId");

            if (distinct != null) {
                fields.put("productFeatureTypeId", distinct);
            }
            if (type != null) {
                fields.put("productFeatureApplTypeId", type);
            }
            features = delegator.findByAndCache("ProductFeatureAndAppl", fields, order);
            result.put("productFeatures", features);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        } catch (GenericEntityException e) {
            Map<String, String> messageMap = UtilMisc.toMap("errMessage", e.toString());
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.problem_reading_product_feature_entity", messageMap, locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
        }
        return result;
    }


    /**
     * 商品品牌的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static String updateProductBrand(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productBrandId = request.getParameter("productBrandId");
        String brandName = request.getParameter("brandName");
        String brandNameAlias = request.getParameter("brandNameAlias");
        String contentId = request.getParameter("contentId");
        String operateType = request.getParameter("operateType");
        String productCategoryIds = request.getParameter("productCategoryIds");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        if ("create".equals(operateType) || "update".equals(operateType)) {
            // 品牌名称输入验证
            if (UtilValidate.isEmpty(brandName)) {
                String errMsg = "品牌名称不能为空";
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                return "error";
            }
//		    // 品牌logo输入验证
//		    if(UtilValidate.isEmpty(contentId)){
//	    	   String errMsg = "品牌logo不能为空";
//	           request.setAttribute("_ERROR_MESSAGE_", errMsg);
//	           return "error";
//	        }
        }
        if (UtilValidate.isEmpty(productBrandId) && "create".equals(operateType)) {
            String productBrandIdCreate = delegator.getNextSeqId("ProductBrand");
            GenericValue productBrand = null;
            productBrand = delegator.makeValue("ProductBrand", UtilMisc.toMap("productBrandId", productBrandIdCreate));
            // 品牌名称
            productBrand.set("brandName", brandName);

            // 品牌别名
            if (UtilValidate.isNotEmpty(brandNameAlias)) {
                productBrand.set("brandNameAlias", brandNameAlias);
            }
            // 品牌Logo
            if (UtilValidate.isNotEmpty(contentId)) {
                productBrand.set("contentId", contentId);
            }
            // 是否启用标识
            productBrand.set("isUsed", "Y");
            // 审核标识
            productBrand.set("auditStatus", "1");
            // 创建表
            productBrand.create();
            // 品牌分类关系表
            if (UtilValidate.isNotEmpty(productCategoryIds)) {
                GenericValue productBrandCategory = null;
                String productBrandCategoryCreate = "";
                String[] productCategoryIdsArry = productCategoryIds.split(",");
                for (String productCategoryId : productCategoryIdsArry) {
                    productBrandCategoryCreate = delegator.getNextSeqId("ProductBrandCategory");
                    // 品牌和分类的关系表
                    productBrandCategory = delegator.makeValue("ProductBrandCategory", UtilMisc.toMap("productBrandCategoryId", productBrandCategoryCreate));
                    // 品牌Id
                    productBrandCategory.set("productBrandId", productBrandIdCreate);
                    // 分类Id
                    productBrandCategory.set("productCategoryId", productCategoryId);
                    // 创建表
                    productBrandCategory.create();
                }
            }

        } else {
            if (UtilValidate.isNotEmpty(productBrandId)) {
                GenericValue productBrandUpdate = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", productBrandId));
                if ("update".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                        // 更新商品品牌
                        if (UtilValidate.isNotEmpty(brandName)) {
                            productBrandUpdate.set("brandName", brandName);
                        }
                        if (UtilValidate.isNotEmpty(brandNameAlias)) {
                            productBrandUpdate.set("brandNameAlias", brandNameAlias);
                        }
                        if (UtilValidate.isNotEmpty(contentId)) {
                            productBrandUpdate.set("contentId", contentId);
                        }
                        productBrandUpdate.store();

                        // 品牌分类关系表的更新
                        if (UtilValidate.isNotEmpty(productCategoryIds)) {
                            List<GenericValue> productBrandCategoryList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productBrandId", productBrandId));
                            if (productBrandCategoryList.size() > 0) {
                                delegator.removeAll(productBrandCategoryList);
                            }
                            GenericValue productBrandCategory = null;
                            String productBrandCategoryCreate = "";
                            String[] productCategoryIdsArry = productCategoryIds.split(",");
                            for (String productCategoryId : productCategoryIdsArry) {
                                productBrandCategoryCreate = delegator.getNextSeqId("ProductBrandCategory");
                                // 品牌和分类的关系表
                                productBrandCategory = delegator.makeValue("ProductBrandCategory", UtilMisc.toMap("productBrandCategoryId", productBrandCategoryCreate));
                                // 品牌Id
                                productBrandCategory.set("productBrandId", productBrandId);
                                // 分类Id
                                productBrandCategory.set("productCategoryId", productCategoryId);
                                // 创建表
                                productBrandCategory.create();
                            }
                        }
                    }
                } else if ("delete".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                        //删除品牌和分类的关系表
                        List<GenericValue> productBrandCategoryList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productBrandId", productBrandId));
                        if (productBrandCategoryList.size() > 0) {
                            delegator.removeAll(productBrandCategoryList);
                        }
                        // 删除品牌表
                        productBrandUpdate.remove();
                    }
                } else if ("disable".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                        productBrandUpdate.set("isUsed", "N");
                        productBrandUpdate.store();
                    }
                } else if ("enabled".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                        productBrandUpdate.set("isUsed", "Y");
                        productBrandUpdate.store();
                    }
                }
            }
        }
        return "success";
    }
    // Add by zhajh at 20151222 商品品牌的更新处理  End


    /**
     * 商品分类的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> updateProductBrandIcoPro(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String productBrandId = (String) context.get("productBrandId");//商品品牌ID
        String brandName = (String) context.get("brandName"); //品牌名称
        String brandNameAlias = (String) context.get("brandNameAlias"); //品牌别名
//        String contentId = (String)context.get("contentId");//log图片
        String operateType = (String) context.get("operateType");//操作类型
        String productCategoryIds = (String) context.get("productCategoryIds");//商品分类信息

        String contentId = (String) context.get("contentId");//商品分类信息
        String fileName = (String) context.get("_uploadedFile_fileName");
        String contentType = (String) context.get("_uploadedFile_contentType");
        ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
        String productContentTypeId = (String) context.get("productContentTypeId");
        String productId = (String) context.get("productId");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        if ("create".equals(operateType) || "update".equals(operateType)) {
            // 品牌名称输入验证
            if (UtilValidate.isEmpty(brandName)) {
                String errMsg = "品牌名称不能为空";
                return ServiceUtil.returnError(errMsg);
            }
            // 品牌logo输入验证

//		    if (UtilValidate.isNotEmpty(fileName)){
//	            Map<String, Object> passedParams = FastMap.newInstance();
//	            passedParams.put("userLogin", userLogin);
//	            passedParams.put("_uploadedFile_fileName", fileName);
//	            passedParams.put("_uploadedFile_contentType", contentType);
//	            passedParams.put("uploadedFile", imageData);
//	            passedParams.put("productId", productId);
//	            passedParams.put("productContentTypeId", productContentTypeId);
//	            Map<String, Object> resultMap = FastMap.newInstance();
//	            try {
//	            	resultMap = dispatcher.runSync("addProductCategoryPicUpload", passedParams);
//	            } catch (GenericServiceException e) {
//					return ServiceUtil.returnError(e.getMessage());
//				}
//	            contentId = (String) resultMap.get("contentId");
//	        }else{
//	        	// String errMsg = "品牌logo不能为空";
//	        	// return ServiceUtil.returnError(errMsg);
//	        }

        }
        if (UtilValidate.isEmpty(productBrandId) && "create".equals(operateType)) {
            try {

                String productBrandIdCreate = delegator.getNextSeqId("ProductBrand");
                GenericValue productBrand = null;
                productBrand = delegator.makeValue("ProductBrand", UtilMisc.toMap("productBrandId", productBrandIdCreate));
                // 品牌名称
                productBrand.set("brandName", brandName);

                // 品牌别名
                if (UtilValidate.isNotEmpty(brandNameAlias)) {
                    productBrand.set("brandNameAlias", brandNameAlias);
                }
                // 品牌Logo
                if (UtilValidate.isNotEmpty(contentId)) {
                    productBrand.set("contentId", contentId);
                }
                // 是否启用标识
                productBrand.set("isUsed", "Y");
                productBrand.set("isDel", "N");
                productBrand.set("auditStatus","1");
                // 创建表
                productBrand.create();
                // 品牌分类关系表
                if (UtilValidate.isNotEmpty(productCategoryIds)) {
                    GenericValue productBrandCategory = null;
                    String productBrandCategoryCreate = "";
                    String[] productCategoryIdsArry = productCategoryIds.split(",");
                    for (String productCategoryId : productCategoryIdsArry) {
                        productBrandCategoryCreate = delegator.getNextSeqId("ProductBrandCategory");
                        // 品牌和分类的关系表
                        productBrandCategory = delegator.makeValue("ProductBrandCategory", UtilMisc.toMap("productBrandCategoryId", productBrandCategoryCreate));
                        // 品牌Id
                        productBrandCategory.set("productBrandId", productBrandIdCreate);
                        // 分类Id
                        productBrandCategory.set("productCategoryId", productCategoryId);
                        // 创建表
                        productBrandCategory.create();
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }


        } else {
            if (UtilValidate.isNotEmpty(productBrandId)) {
                try {
                    GenericValue productBrandUpdate = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", productBrandId));
                    if ("update".equals(operateType)) {
                        if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                            // 更新商品品牌
                            if (UtilValidate.isNotEmpty(brandName)) {
                                productBrandUpdate.set("brandName", brandName);
                            }
                            if (UtilValidate.isNotEmpty(brandNameAlias)) {
                                productBrandUpdate.set("brandNameAlias", brandNameAlias);
                            }
                            if (UtilValidate.isNotEmpty(contentId)) {
                                productBrandUpdate.set("contentId", contentId);
                            }
                            productBrandUpdate.store();

                            // 品牌分类关系表的更新
                            if (UtilValidate.isNotEmpty(productCategoryIds)) {
                                List<GenericValue> productBrandCategoryList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productBrandId", productBrandId));
                                if (productBrandCategoryList.size() > 0) {
                                    delegator.removeAll(productBrandCategoryList);
                                }
                                GenericValue productBrandCategory = null;
                                String productBrandCategoryCreate = "";
                                String[] productCategoryIdsArry = productCategoryIds.split(",");
                                for (String productCategoryId : productCategoryIdsArry) {
                                    productBrandCategoryCreate = delegator.getNextSeqId("ProductBrandCategory");
                                    // 品牌和分类的关系表
                                    productBrandCategory = delegator.makeValue("ProductBrandCategory", UtilMisc.toMap("productBrandCategoryId", productBrandCategoryCreate));
                                    // 品牌Id
                                    productBrandCategory.set("productBrandId", productBrandId);
                                    // 分类Id
                                    productBrandCategory.set("productCategoryId", productCategoryId);
                                    // 创建表
                                    productBrandCategory.create();
                                }
                            }
                        }
                    } else if ("delete".equals(operateType)) {
                        if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                            //删除品牌和分类的关系表
                            List<GenericValue> productBrandCategoryList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productBrandId", productBrandId));
                            if (productBrandCategoryList.size() > 0) {
                                delegator.removeAll(productBrandCategoryList);
                            }
                            // 删除品牌表
                            productBrandUpdate.remove();
                        }
                    } else if ("disable".equals(operateType)) {
                        if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                            productBrandUpdate.set("isUsed", "N");
                            productBrandUpdate.store();
                        }
                    } else if ("enabled".equals(operateType)) {
                        if (UtilValidate.isNotEmpty(productBrandUpdate)) {
                            productBrandUpdate.set("isUsed", "Y");
                            productBrandUpdate.store();
                        }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }

            }
        }
        result = ServiceUtil.returnSuccess();
        return result;
    }
    // Add by zhajh at 20151222 商品分类的更新处理  End


    /**
     * 商品品牌的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static String getProductBrandCategoryList(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productBrandId = request.getParameter("productBrandId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");


        List<GenericValue> productBrandCategoryList = FastList.newInstance();
        productBrandCategoryList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productBrandId", productBrandId));

        // 保存成功
        request.setAttribute("productBrandCategoryList", productBrandCategoryList);
        request.setAttribute("success", true);
        return "success";
    }


    /**
     * 根据品牌ID获取分类信息
     *

     * @return
     */
    public static Map<String, Object> getProductBrandCategoryList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Map<String, List> paramMap = FastMap.newInstance();
        //参数仓库ID
        String productBrandId = (String) context.get("productBrandId");

        try {
            //根据品牌ID获取该品牌的分类信息
            List<GenericValue> productBrandCategoryList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productBrandId", productBrandId), null);
            paramMap.put("productBrandCategoryList", productBrandCategoryList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("paramMap", paramMap);
        return result;
    }
    // Add by zhajh at 20151222 商品品牌对应的分类取得处理  End


    // Add by zhajh at 20151225 商品品牌的删除处理 Begin

    /**
     * 商品品牌的删除处理ByIds
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductBrandByIds(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品品牌记录 ids
        String ids = request.getParameter("checkedIds");
        String brandIds = ""; // 已选择的品牌ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                brandIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品品牌的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> brandIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(brandIds)) {
            brandIdsList = UtilMisc.toListArray(brandIds.split(","));
        }
        // 根据条件取得商品品牌的数据
        List<GenericValue> productBrandInfoList = null;
        EntityCondition condition = EntityCondition.makeCondition("productBrandId", EntityOperator.IN, brandIdsList);
        try {
            productBrandInfoList = delegator.findList("ProductBrand", condition, null, null, null, false);
            // 删除处理
            if (UtilValidate.isNotEmpty(productBrandInfoList)) {
                for (GenericValue productBrandInfo : productBrandInfoList) {
                    productBrandInfo.remove();
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20151225 商品品牌的删除处理 End


    // Add by zhajh at 20151225 品牌列表的导出 Begin

    /**
     * 品牌列表的导出处理
     *
     * @param delegator
     * @param payOnlineIds
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> productBrandListReport(Delegator delegator, String ids) throws GenericEntityException {
        Map<String, Object> mapTemp = new HashMap<String, Object>();
        List<Map<String, Object>> productBrandList = new LinkedList<Map<String, Object>>();
        Map<String, Object> mapResult = new HashMap<String, Object>();
        List<GenericValue> productBrandInfoList = null;
        String brandIds = ""; // 已选择的品牌ID
        EntityCondition condition = null;
        List<String> brandIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(ids)) {
            // 选择商品品牌记录 ids
            if (UtilValidate.isNotEmpty(ids)) {
                String[] idsArray = ids.split(",");
                String sessionIds = "";
                for (String id : idsArray) {
                    if (!sessionIds.contains(id)) {
                        sessionIds = sessionIds + id + ",";
                    }
                }
                if (UtilValidate.isNotEmpty(sessionIds)) {
                    brandIds = sessionIds.substring(0, sessionIds.length() - 1);
                }
            }
            if (UtilValidate.isNotEmpty(brandIds)) {
                brandIdsList = UtilMisc.toListArray(brandIds.split(","));
            }
            condition = EntityCondition.makeCondition("productBrandId", EntityOperator.IN, brandIdsList);
        }
        try {
            productBrandInfoList = delegator.findList("ProductBrand", condition, null, null, null, false);
            // 删除处理
            if (UtilValidate.isNotEmpty(productBrandInfoList)) {
                for (GenericValue productBrandInfo : productBrandInfoList) {
                    mapTemp = new HashMap<String, Object>();
                    mapTemp.put("brandName", productBrandInfo.getString("brandName"));
                    mapTemp.put("brandNameAlias", productBrandInfo.getString("brandNameAlias"));
                    if (UtilValidate.isNotEmpty(productBrandInfo.getString("isUsed"))) {
                        if ("Y".equals(productBrandInfo.getString("isUsed"))) {
                            mapTemp.put("isUsed", "是");
                        } else {
                            mapTemp.put("isUsed", "否");
                        }
                    }
                    productBrandList.add(mapTemp);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        mapResult.put("productBrandList", productBrandList);
        return mapResult;
    }
    // Add by zhajh at 20151225 菜品列表的导出 End

    // Add by zhajh at 20151222 商品品牌的导入处理 Begin

    /**
     * 商品品牌的导入处理
     *
     * @param request
     * @param response
     * @return
     * @throws InvalidFormatException
     * @throws IOException
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static void importProductBrand2(HttpServletRequest request, HttpServletResponse response) throws InvalidFormatException, GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        int maxPostSize = 1000 * 1024 * 1024; //最大缓存
        FileInputStream fis = null;
        InputStream in = null;
        Workbook workBook = null;
        HSSFWorkbook workBook2 = null;
        Sheet sheet = null;
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(maxPostSize);
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(-1);
        try {
            //获取所有文件列表
            List<FileItem> items;
//			items = upload.parseRequest(request);
            items = upload.parseRequest(request);

            for (FileItem item : items) {
                if (!item.isFormField()) {
                    if (item.getInputStream() instanceof FileInputStream) {
                        fis = (FileInputStream) item.getInputStream();
                    } else if (item.getInputStream() instanceof InputStream) {
                        in = item.getInputStream();
                    } else {
                    
                    }
//	    			workBook = WorkbookFactory.create(in);
                    workBook2 = new HSSFWorkbook(in);
                    // 读取导入文件的插入数据
//	    			readExcelData(workBook,sheet);
                    List<Map<String, Object>> brandList = readXls(workBook2);

                    // 取得品牌列表的数据
                    //List<GenericValue> productBrandlist = delegator.findList("ProductBrand",null , null, null, null, false);
                    // 数据重复信息check
                    // 将插入数据保存到DB
                    if (brandList.size() > 0) {
                        saveBrandImportData(delegator, brandList);
                    }

                }
            }

        } catch (FileUploadException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }
    }


    /**
     * 读取xls文件内容
     *
     * @return List<XlsDto>对象
     * @throws IOException 输入/输出(i/o)异常
     */
    private static List<Map<String, Object>> readXls(HSSFWorkbook hssfWorkbook) throws IOException {
        List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
        Map<String, Object> mapTemp = new HashMap<String, Object>();
        // 循环工作表Sheet
        for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);
            if (hssfSheet == null) {
                continue;
            }
            // 循环行Row
            for (int rowNum = 2; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                HSSFRow hssfRow = hssfSheet.getRow(rowNum);
                mapTemp = new HashMap<String, Object>();
                if (hssfRow == null) {
                    continue;
                }
                // 循环列Cell
                // 0品牌名称 1品牌别名
                // for (int cellNum = 0; cellNum <=4; cellNum++) {
                HSSFCell brandName = hssfRow.getCell(0);
                if (brandName == null) {
                    break;
                } else {
                    mapTemp.put("brandName", brandName.toString());
                }
                HSSFCell barandNameAlias = hssfRow.getCell(1);
                if (barandNameAlias == null) {
                    continue;
                } else {
                    mapTemp.put("brandNameAlias", barandNameAlias.toString());
                }
//	                HSSFCell isUsed = hssfRow.getCell(2);
//	                if (isUsed == null) {
//	                    continue;
//	                }
                list.add(mapTemp);
                // }
            }
        }
        return list;
    }

    /**
     * 保存上传数据
     *
     * @param delegator
     * @param importBrands
     * @return
     * @throws GenericEntityException
     */
    private static boolean saveBrandImportData(Delegator delegator, List<Map<String, Object>> importBrands) throws GenericEntityException {
        for (Map<String, Object> brandInfo : importBrands) {
            GenericValue productBrand = null;
            String productBrandIdCreate = delegator.getNextSeqId("ProductBrand");
            productBrand = delegator.makeValue("ProductBrand", UtilMisc.toMap("productBrandId", productBrandIdCreate));
            // 品牌名称
            productBrand.set("brandName", brandInfo.get("brandName"));
            // 品牌别名
            String brandNameAlias = (String) brandInfo.get("brandNameAlias");
            if (UtilValidate.isNotEmpty(brandNameAlias)) {
                productBrand.set("brandNameAlias", brandNameAlias);
            }
            // 是否启用标识
            productBrand.set("isUsed", "Y");
            // 创建表
            productBrand.create();
        }
        return true;

    }
    // Add by zhajh at 20151222 商品品牌的导入处理 End
    // Add by zhajh at 20151231 商品分类的更新处理 Begin

    /**
     * 商品分类的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static String updateProductCategoryIco(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productCategoryId = request.getParameter("productCategoryId");//分类ID
        String categoryName = request.getParameter("categoryName"); //分类名称
        String primaryParentCategoryId = request.getParameter("primaryParentCategoryId"); //分类父级ID
        String sequenceNum = request.getParameter("sequenceNum");//排序号
        String isHasExtendAttr = request.getParameter("isHasExtendAttr");//是否有扩展属性
        String operateType = request.getParameter("operateType");//操作类型
        String productCategoryLevel = request.getParameter("productCategoryLevel");//分类级别
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String extendAttrInfos = request.getParameter("extendAttrInfos"); //商品分类的扩展属性
        String productStoreId = (String)request.getAttribute("productStoreId");
        
        List<Map<String, Object>> productCategoryAttrList = FastList.newInstance();
        List<Map<String, Object>> productCategoryAttrOptionList = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        if ("create".equals(operateType) || "update".equals(operateType)) {
            // 取得商品分类扩展属性的值
            if (UtilValidate.isNotEmpty(extendAttrInfos)) {
                String[] tExtendAttrInfosArray = extendAttrInfos.split(",");
                for (String ExtendAttrInfo : tExtendAttrInfosArray) {
                    String[] attrInfos = ExtendAttrInfo.split("\\|");
                    String attrName = attrInfos[0];//属性名
                    String isRequired = attrInfos[1];//是否必填
                    String extendOptions = attrInfos[2];//可选项
                    Map<String, Object> mapTemp;
                    if (UtilValidate.isNotEmpty(attrName) && UtilValidate.isNotEmpty(isRequired)) {
                        mapTemp = FastMap.newInstance();
                        mapTemp.put("attrName", attrName);
                        mapTemp.put("isRequired", isRequired);
                        mapTemp.put("extendOptions", extendOptions);
                        productCategoryAttrList.add(mapTemp);
                    }
                }
            }
            // 品牌分类名称输入验证
            if (UtilValidate.isEmpty(categoryName)) {
                String errMsg = "品牌分类名称不能为空";
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                return "error";
            }
        }
        if (UtilValidate.isEmpty(productCategoryId) && "create".equals(operateType)) {
            String productCategoryIdCreate = delegator.getNextSeqId("ProductCategory");
            GenericValue productCategory = null;
            productCategory = delegator.makeValue("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryIdCreate));
            // 品牌分类名称
            productCategory.set("categoryName", categoryName);
            // 品牌分类上级分类
            if (UtilValidate.isNotEmpty(primaryParentCategoryId)) {
                productCategory.set("primaryParentCategoryId", primaryParentCategoryId);
            }
            // 排序号
            if (UtilValidate.isNotEmpty(sequenceNum)) {
                productCategory.set("sequenceNum", Long.parseLong(sequenceNum));
            }
            // 是否有附加属性
            if (UtilValidate.isNotEmpty(isHasExtendAttr)) {
                productCategory.set("isHasExtendAttr", isHasExtendAttr);
            }
            // 级别
            productCategory.set("productCategoryLevel", Long.parseLong(productCategoryLevel));
            productCategory.set("productStoreId",productStoreId);
            
            // 创建表
            productCategory.create();


            if ("Y".equals(isHasExtendAttr)) {

                // 商品分类扩展属性值数据的登陆
                if (productCategoryAttrList.size() > 0) {
                    for (Map<String, Object> map : productCategoryAttrList) {
                        GenericValue productCategoryAttribute = null;
                        productCategoryAttribute = delegator.makeValue("ProductCategoryAttribute");
                        // 产品分类ID
                        productCategoryAttribute.set("productCategoryId", productCategoryIdCreate);
                        // 产品分类属性名称
                        productCategoryAttribute.set("attrName", (String) map.get("attrName"));
                        // 是否必填
                        productCategoryAttribute.set("isRequired", (String) map.get("isRequired"));
                        // 创建表
                        productCategoryAttribute.create();

                        // 扩展属性的可选项
                        String extendOptions = (String) map.get("extendOptions");
                        String[] options = extendOptions.split("\\^");
                        for (int i = 0; i < options.length; i++) {
                            String productOptionIdCreate = delegator.getNextSeqId("ProductOption");
                            GenericValue productOption = null;
                            productOption = delegator.makeValue("ProductOption", UtilMisc.toMap("productOptionId", productOptionIdCreate));
                            // 产品分类ID
                            productOption.set("productCategoryId", productCategoryIdCreate);
                            // 产品分类属性名称
                            productOption.set("attrName", (String) map.get("attrName"));
                            // 选项名称
                            productOption.set("optionName", options[i]);
                            // 创建表
                            productOption.create();
                        }
                    }
                }
            }
        } else {
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                GenericValue productCategoryUpdate = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryId));
                if ("update".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productCategoryUpdate)) {
                        // 更新商品分类
                        if (UtilValidate.isNotEmpty(categoryName)) {
                            productCategoryUpdate.set("categoryName", categoryName);
                        }
                        if (UtilValidate.isNotEmpty(primaryParentCategoryId)) {
                            productCategoryUpdate.set("primaryParentCategoryId", primaryParentCategoryId);
                        }
                        if (UtilValidate.isNotEmpty(sequenceNum)) {
                            productCategoryUpdate.set("sequenceNum", Long.parseLong(sequenceNum));
                        }
                        // 是否有附加属性
                        if (UtilValidate.isNotEmpty(isHasExtendAttr)) {
                            productCategoryUpdate.set("isHasExtendAttr", isHasExtendAttr);
                        }
                        productCategoryUpdate.set("productStoreId",productStoreId);
                        productCategoryUpdate.store();

                        if ("Y".equals(isHasExtendAttr)) {

                            List<GenericValue> productCategoryAttributeDelList = null;
                            List<GenericValue> productOptionDelList = null;
                            productCategoryAttributeDelList = delegator.findList("ProductCategoryAttribute", EntityCondition.makeCondition("productCategoryId", productCategoryId), null, null, null, true);
                            productOptionDelList = delegator.findList("ProductOption", EntityCondition.makeCondition("productCategoryId", productCategoryId), null, null, null, true);
                            if (UtilValidate.isNotEmpty(productOptionDelList)) {
                                delegator.removeAll(productOptionDelList);
                            }
                            if (UtilValidate.isNotEmpty(productCategoryAttributeDelList)) {
                                delegator.removeAll(productCategoryAttributeDelList);
                            }

                            // 商品分类的扩展属性数据的登陆
                            if (productCategoryAttrList.size() > 0) {
                                for (Map<String, Object> map : productCategoryAttrList) {
                                    GenericValue productCategoryAttribute = null;
                                    productCategoryAttribute = delegator.makeValue("ProductCategoryAttribute");
                                    // 产品分类ID
                                    productCategoryAttribute.set("productCategoryId", productCategoryId);
                                    // 产品分类属性名称
                                    productCategoryAttribute.set("attrName", (String) map.get("attrName"));
                                    // 是否必填
                                    productCategoryAttribute.set("isRequired", (String) map.get("isRequired"));
                                    // 创建表
                                    productCategoryAttribute.create();

                                    // 扩展属性的可选项
                                    String extendOptions = (String) map.get("extendOptions");
                                    String[] options = extendOptions.split("\\^");
                                    for (int i = 0; i < options.length; i++) {
                                        String productOptionIdCreate = delegator.getNextSeqId("ProductOption");
                                        GenericValue productOption = null;
                                        productOption = delegator.makeValue("ProductOption", UtilMisc.toMap("productOptionId", productOptionIdCreate));
                                        // 产品分类ID
                                        productOption.set("productCategoryId", productCategoryId);
                                        // 产品分类属性名称
                                        productOption.set("attrName", (String) map.get("attrName"));
                                        // 选项名称
                                        productOption.set("optionName", options[i]);
                                        // 创建表
                                        productOption.create();
                                    }
                                }
                            }
                        }
                    }
                } else if ("delete".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productCategoryId)) {
                        List<GenericValue> productCategoryAttributeDelList = null;
                        List<GenericValue> productOptionDelList = null;
                        productCategoryAttributeDelList = delegator.findList("ProductCategoryAttribute", EntityCondition.makeCondition("productCategoryId", productCategoryId), null, null, null, true);
                        productOptionDelList = delegator.findList("ProductOption", EntityCondition.makeCondition("productCategoryId", productCategoryId), null, null, null, true);
                        if (UtilValidate.isNotEmpty(productOptionDelList)) {
                            delegator.removeAll(productOptionDelList);
                        }
                        if (UtilValidate.isNotEmpty(productCategoryAttributeDelList)) {
                            delegator.removeAll(productCategoryAttributeDelList);
                        }
                        productCategoryUpdate.remove();
                    }
                }
            }
        }
        return "success";
    }


    
    // Add by zhajh at 20151222 商品分类的更新处理  End


    // Add by zhajh at 20151231 商品类型列表处理 Begin

    /**
     * 商品等级分类列表的取得
     *

     * @return
     */
    public static Map<String, Object> getProductCategoryByLevel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("PC", "ProductCategory");
        dve.addAliasAll("PC", "", null);
        List<String> orderBy = FastList.newInstance();
        orderBy.add("productCategoryId");
        List<EntityCondition> entityConditionList = FastList.newInstance();
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
        List<GenericValue> productCategoryList = FastList.newInstance();
        EntityListIterator eli = null;
        String productCategoryLevel = (String) context.get("productCategoryLevel");
        String productCategoryId = (String) context.get("productCategoryId");

        //查询
        // 品牌分类等级
        if ("1".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));
        } else if ("2".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
        } else if ("3".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));

        }
        // 品牌分类ID
        if (UtilValidate.isNotEmpty(productCategoryId)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
        }
        // do the lookup
        try {
            eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, orderBy, findOpts);
            productCategoryList = eli.getCompleteList();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("productCategoryList", productCategoryList);
        result.put("productCategoryLevel", productCategoryLevel);
        result.put("productCategoryId", productCategoryId);
        result.put("productCategoryInfo", productCategoryList.get(0));
        return result;
    }
    // Add by zhajh at 20151231 商品类型列表处理 End


    // Add by zhajh at 20151231 商品类型列表处理 Begin

    /**
     * 商品等级分类列表的取得
     *

     * @return
     */
    public static String getProductCategoryByLevel1(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productCategoryId = request.getParameter("productCategoryId");
        String productCategoryLevel = request.getParameter("productCategoryLevel");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("PC", "ProductCategory");
        dve.addAliasAll("PC", "", null);
        List<String> orderBy = FastList.newInstance();
        orderBy.add("productCategoryId");
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null), EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        List<EntityCondition> entityConditionList = FastList.newInstance();
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
        List<GenericValue> productCategoryList = FastList.newInstance();
        EntityListIterator eli = null;
        boolean beganTransaction = false;

        //查询
        // 品牌分类等级
        if ("1".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));
        } else if ("2".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            // 品牌分类ID
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                GenericValue productCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryId));
                String primaryParentCategoryId = productCategory.getString("primaryParentCategoryId");
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", primaryParentCategoryId));
            }
        } else {

        }
        entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
        // do the lookup
        try {
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, orderBy, findOpts);
            productCategoryList = eli.getCompleteList();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }finally {
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

//	    Map<String, Object> result = ServiceUtil.returnSuccess();
//	    result.put("productCategoryList", productCategoryList);
//	    result.put("productCategoryLevel", productCategoryLevel);
//	    result.put("productCategoryId", productCategoryId);
//	    result.put( "productCategoryInfo", productCategoryList.get(0));
        request.setAttribute("productCategoryList", productCategoryList);
        return "success";
    }
    // Add by zhajh at 20151231 商品类型列表处理 End


    // Add by zhajh at 20160101 商品特征 Begin

    /**
     * 商品特征的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static String updateProductFeature(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productFeatureTypeId = request.getParameter("productFeatureTypeId");
        String productFeatureTypeName = request.getParameter("productFeatureTypeName");
        String tFeatureInfos = request.getParameter("tFeatureInfos");
        String description = request.getParameter("description");
        String operateType = request.getParameter("operateType");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String isPop = request.getParameter("isPop");

        // 页面跳转用
        String viewSize = request.getParameter("VIEW_SIZE");
        String viewIndex = request.getParameter("VIEW_INDEX");
        String sortField = request.getParameter("sortField");
        String sortType = request.getParameter("sortType");
        String productFeatureTypeNameForFind = request.getParameter("productFeatureTypeNameForFind");
        String descriptionForFind = request.getParameter("descriptionForFind");
        //当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        int curViewIndex = 0;


        List<Map<String, Object>> productFeatureList = FastList.newInstance();//特征属性值
        // 取得特征属性值
        if (UtilValidate.isNotEmpty(tFeatureInfos)) {
            String[] tFeatureInfosArray = tFeatureInfos.split(",");
            for (String featureInfo : tFeatureInfosArray) {
                String[] attrInfos = featureInfo.split("\\|");
                String productFeatureName = attrInfos[0];//特征值名称
                String sequenceNum = attrInfos[1];//排序号
                String curproductFeatureId = attrInfos[2];//特征值ID
                String curOptionType = attrInfos[3];//操作类型

                Map<String, Object> mapTemp;
                if (UtilValidate.isNotEmpty(attrInfos[0])) {
                    mapTemp = FastMap.newInstance();
                    mapTemp.put("productFeatureName", productFeatureName);
                    mapTemp.put("sequenceNum", sequenceNum);
                    mapTemp.put("curproductFeatureId", curproductFeatureId);
                    mapTemp.put("curOptionType", curOptionType);
                    productFeatureList.add(mapTemp);
                }
            }
        }

        if ("create".equals(operateType) || "update".equals(operateType)) {
            // 特征名称输入验证
            if (UtilValidate.isEmpty(productFeatureTypeName)) {
                String errMsg = "特征名称不能为空";
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                return "error";
            }

        }
        if (UtilValidate.isEmpty(productFeatureTypeId) && "create".equals(operateType)) {
            String productFeatureTypeIdCreate = delegator.getNextSeqId("ProductFeatureType");
            GenericValue productFeatureType = null;
            productFeatureType = delegator.makeValue("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeIdCreate));
            // 特征名称
            productFeatureType.set("productFeatureTypeName", productFeatureTypeName);

            // 特征备注
            if (UtilValidate.isNotEmpty(description)) {
                productFeatureType.set("description", description);
            }
            // 创建表
            productFeatureType.create();

            if (productFeatureList.size() > 0) {
                for (Map<String, Object> map : productFeatureList) {

                    GenericValue productFeature = null;
                    String productFeatureIdCreate = delegator.getNextSeqId("ProductFeature");
                    productFeature = delegator.makeValue("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureIdCreate));
                    // 特征值名称
                    productFeature.set("productFeatureName", (String) map.get("productFeatureName"));
                    // 排序
                    productFeature.set("sequenceNum", Long.parseLong((String) map.get("sequenceNum")));
                    // 特征值分类
                    productFeature.set("productFeatureTypeId", productFeatureTypeIdCreate);
                    // 创建表
                    productFeature.create();
                }
            }

            request.setAttribute("productFeatureTypeName", productFeatureTypeName);
            request.setAttribute("productFeatureTypeId", productFeatureTypeIdCreate);

        } else {
            if (UtilValidate.isNotEmpty(productFeatureTypeId)) {
                GenericValue productFeatureTypeIdUpdate = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeId));
                if ("update".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productFeatureTypeIdUpdate)) {
                        // 更新商品特征
                        if (UtilValidate.isNotEmpty(productFeatureTypeName)) {
                            productFeatureTypeIdUpdate.set("productFeatureTypeName", productFeatureTypeName);
                        }
                        if (UtilValidate.isNotEmpty(description)) {
                            productFeatureTypeIdUpdate.set("description", description);
                        }

                        productFeatureTypeIdUpdate.store();

                        if (productFeatureList.size() > 0) {
                            //        	 List<GenericValue> productFeatureDelList = null;
                            //             productFeatureDelList = delegator.findList("ProductFeature", EntityCondition.makeCondition("productFeatureTypeId", productFeatureTypeIdUpdate.get("productFeatureTypeId")), null, null, null, true);
                            //             if(UtilValidate.isNotEmpty(productFeatureDelList)){
                            //            	 delegator.removeAll(productFeatureDelList);
                            //             }

                            for (Map<String, Object> map : productFeatureList) {
                                String currOptionType = (String) map.get("curOptionType");
                                String currFeatureId = (String) map.get("curproductFeatureId");
                                String currProductFeatureName = (String) map.get("productFeatureName");
                                String currSequenceNum = (String) map.get("sequenceNum");
                                if ("update".equals(currOptionType)) {
                                    GenericValue productFeatureUpdate = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", currFeatureId));
                                    if (UtilValidate.isNotEmpty(productFeatureUpdate)) {
                                        if (UtilValidate.isNotEmpty(currProductFeatureName)) {
                                            // 特征值名称
                                            productFeatureUpdate.set("productFeatureName", currProductFeatureName);
                                        }
                                        if (UtilValidate.isNotEmpty(currSequenceNum)) {
                                            // 排序
                                            productFeatureUpdate.set("sequenceNum", Long.parseLong((String) currSequenceNum));
                                        }
                                        productFeatureUpdate.store();
                                    }
                                } else if ("create".equals(currOptionType)) {
                                    GenericValue productFeature = null;
                                    String productFeatureIdCreate = delegator.getNextSeqId("ProductFeature");
                                    productFeature = delegator.makeValue("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureIdCreate));
                                    // 特征值名称
                                    if (UtilValidate.isNotEmpty(currProductFeatureName)) {
                                        productFeature.set("productFeatureName", (String) map.get("productFeatureName"));
                                    }
                                    // 排序
                                    if (UtilValidate.isNotEmpty(currSequenceNum)) {
                                        productFeature.set("sequenceNum", Long.parseLong((String) map.get("sequenceNum")));
                                    }
                                    // 特征值分类
                                    productFeature.set("productFeatureTypeId", productFeatureTypeId);
                                    // 创建表
                                    productFeature.create();

                                } else if ("delete".equals(currOptionType)) {
                                    GenericValue productFeatureDel = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", currFeatureId));
                                    if (UtilValidate.isNotEmpty(productFeatureDel)) {
                                        productFeatureDel.remove();
                                    }

                                } else {
                                }
                            }
                        }
                        if (UtilValidate.isEmpty(isPop)) {
                            // 取得ViewIndex
                            int tempViewIndex = Integer.valueOf(viewIndex);
                            boolean flg = true;
                            for (int i = tempViewIndex; i > 0; i--) {
                                Map<String, String> passedParams = FastMap.newInstance();
                                passedParams.put("VIEW_SIZE", viewSize);
                                passedParams.put("VIEW_INDEX", i + "");
                                passedParams.put("productFeatureTypeName", productFeatureTypeNameForFind);
                                passedParams.put("description", descriptionForFind);
                                passedParams.put("sortField", sortField);
                                passedParams.put("sortType", sortType);
                                flg = getCurViewIndex(passedParams, userLogin, dispatcher);
                                if (flg) {
                                    curViewIndex = i;

                                    break;
                                }
                            }

                        }
                    }
                } else if ("delete".equals(operateType)) {
                    if (UtilValidate.isNotEmpty(productFeatureTypeIdUpdate)) {
                        List<GenericValue> productFeatureDelList = null;
                        productFeatureDelList = delegator.findList("ProductFeature", EntityCondition.makeCondition("productFeatureTypeId", productFeatureTypeIdUpdate.get("productFeatureTypeId")), null, null, null, true);
                        delegator.removeAll(productFeatureDelList);
                        productFeatureTypeIdUpdate.remove();
                    }
                }
            }
        }
        // 保存viewIndex
        request.setAttribute("curViewIndex", curViewIndex);
        return "success";
    }


    // Add by zhajh at 20160105 根据级别商品类型列表初始化处理 Begin

    /**
     * 商品等级分类列表的取得
     *

     * @return
     */
    public static String getInitProductCategoryByLevel(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();

        List<GenericValue> productCategoryLevel1List = FastList.newInstance();
        List<GenericValue> productCategoryLevel2List = FastList.newInstance();
        List<GenericValue> productCategoryLevel3List = FastList.newInstance();
        GenericValue productCategoryLevel1Info = null;
        GenericValue productCategoryLevel2Info = null;
        GenericValue productCategoryLevel3Info = null;
        orderBy.add("sequenceNum");

        String isInner = (String) request.getParameter("isInner");
        String productStoreId = (String) request.getParameter("productStoreId");

        entityConditionList = FastList.newInstance();
        entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));

        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
        if(UtilValidate.isNotEmpty(productStoreId)) {
            entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        }

        productCategoryLevel1List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

        //productCategoryLevel1List = delegator.findList("ProductCategory", EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")), null, orderBy, null, true);

        if (productCategoryLevel1List.size() > 0) {
            productCategoryLevel1Info = productCategoryLevel1List.get(0);
            entityConditionList = FastList.newInstance();
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel1Info.getString("productCategoryId")));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            if(UtilValidate.isNotEmpty(productStoreId)) {
                entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            }

            productCategoryLevel2List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

            if (productCategoryLevel2List.size() > 0) {
                productCategoryLevel2Info = productCategoryLevel2List.get(0);
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(productStoreId)) {
                    entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                }

                productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                if (productCategoryLevel3List.size() > 0) {
                    productCategoryLevel3Info = productCategoryLevel3List.get(0);
                }
            }
        }
        request.setAttribute("productCategoryLevel1List", productCategoryLevel1List);
        request.setAttribute("productCategoryLevel2List", productCategoryLevel2List);
        request.setAttribute("productCategoryLevel3List", productCategoryLevel3List);
        request.setAttribute("productCategoryLevel1Info", productCategoryLevel1Info);
        request.setAttribute("productCategoryLevel2Info", productCategoryLevel2Info);
        request.setAttribute("productCategoryLevel3Info", productCategoryLevel3Info);
        return "success";
    }
    
    // Add by zhajh at 20160105 根据级别商品类型列表初始化处理 End

    /**
     * 商品等级分类列表的根据名称查询处理
     *

     * @return
     */
    public static String searchProductCategoryLevelByName(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        String productCategoryLevel = (String) request.getParameter("productCategoryLevel");
        String parentCategoryId = (String) request.getParameter("productCategoryId");
        String categoryName = (String) request.getParameter("categoryName");
        List<GenericValue> productCategoryLevel1List = FastList.newInstance();
        List<GenericValue> productCategoryLevel2List = FastList.newInstance();
        List<GenericValue> productCategoryLevel3List = FastList.newInstance();
        GenericValue productCategoryLevel1Info = null;
        GenericValue productCategoryLevel2Info = null;
        GenericValue productCategoryLevel3Info = null;
        orderBy.add("sequenceNum");

        String isInner = (String) request.getParameter("isInner");
        String productStoreId = (String) request.getParameter("productStoreId");

        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));

        if(UtilValidate.isNotEmpty(productStoreId)) {
            entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        }
        if ("1".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));
            entityConditionList.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("categoryName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + categoryName + "%")));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel1List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel1List.size() > 0) {
                productCategoryLevel1Info = productCategoryLevel1List.get(0);

                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel1Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(productStoreId)) {
                    entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                }
                productCategoryLevel2List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

                if (productCategoryLevel2List.size() > 0) {
                    productCategoryLevel2Info = productCategoryLevel2List.get(0);
                    entityConditionList = FastList.newInstance();
                    entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                    entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                    entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                    if(UtilValidate.isNotEmpty(productStoreId)) {
                        entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                    }
                    productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                    if (productCategoryLevel3List.size() > 0) {
                        productCategoryLevel3Info = productCategoryLevel3List.get(0);
                    }
                }
            }
        } else if ("2".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            entityConditionList.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("categoryName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + categoryName + "%")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", parentCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel2List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel2List.size() > 0) {
                productCategoryLevel2Info = productCategoryLevel2List.get(0);

                productCategoryLevel2Info = productCategoryLevel2List.get(0);
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if (productCategoryLevel3List.size() > 0) {
                    productCategoryLevel3Info = productCategoryLevel3List.get(0);
                }
                productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

            }
        } else if ("3".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
            entityConditionList.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("categoryName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + categoryName + "%")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", parentCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            if (productCategoryLevel3List.size() > 0) {
                productCategoryLevel3Info = productCategoryLevel3List.get(0);
            }
            productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

        }

        request.setAttribute("productCategoryLevel1List", productCategoryLevel1List);
        request.setAttribute("productCategoryLevel2List", productCategoryLevel2List);
        request.setAttribute("productCategoryLevel3List", productCategoryLevel3List);
        request.setAttribute("productCategoryLevel1Info", productCategoryLevel1Info);
        request.setAttribute("productCategoryLevel2Info", productCategoryLevel2Info);
        request.setAttribute("productCategoryLevel3Info", productCategoryLevel3Info);

//	    if(UtilValidate.isNotEmpty(productCategoryLevel1Info)){
//	    	request.setAttribute("productCategoryIdLevel1", productCategoryLevel1Info.getString("productCategoryId"));
//	    	request.setAttribute("productCategoryNameLevel1", productCategoryLevel1Info.getString("categoryName"));
//	    }
//
//        if(UtilValidate.isNotEmpty(productCategoryLevel2Info)){
//	    	request.setAttribute("productCategoryIdLevel2", productCategoryLevel2Info.getString("productCategoryId"));
//	    	request.setAttribute("productCategoryNameLevel2", productCategoryLevel1Info.getString("categoryName"));
//	    }
//
//        if(UtilValidate.isNotEmpty(productCategoryLevel3Info)){
//			request.setAttribute("productCategoryIdLevel3", productCategoryLevel3Info.getString("productCategoryId"));
//			request.setAttribute("productCategoryNameLevel3", productCategoryLevel1Info.getString("categoryName"));
//		}
        request.setAttribute("productCategoryLevel", productCategoryLevel);
        request.setAttribute("success", true);

        return "success";
    }
    // Add by zhajh at 20160105 根据级别商品类型列表初始化处理 End


    // Add by zhajh at 20160106 商品等级分类列表的根据ID查询处理 Begin

    /**
     * 商品等级分类列表的根据ID查询处理
     *

     * @return
     */
    public static String seclectdeItemProductCategoryLevelById(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        String productCategoryLevel = (String) request.getParameter("productCategoryLevel");
        String productCategoryId = (String) request.getParameter("productCategoryId");

        String isInner = (String) request.getParameter("isInner");
        String productStoreId = (String) request.getParameter("productStoreId");

        List<GenericValue> productCategoryLevel1List = FastList.newInstance();
        List<GenericValue> productCategoryLevel2List = FastList.newInstance();
        List<GenericValue> productCategoryLevel3List = FastList.newInstance();
        GenericValue productCategoryLevel1Info = null;
        GenericValue productCategoryLevel2Info = null;
        GenericValue productCategoryLevel3Info = null;
        orderBy.add("sequenceNum");
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        if(UtilValidate.isNotEmpty(productStoreId)) {
            entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        }
        if ("1".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));
            entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel1List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel1List.size() > 0) {
                productCategoryLevel1Info = productCategoryLevel1List.get(0);

                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel1Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(productStoreId)) {
                    entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                }
                productCategoryLevel2List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

                if (productCategoryLevel2List.size() > 0) {
                    productCategoryLevel2Info = productCategoryLevel2List.get(0);
                    entityConditionList = FastList.newInstance();
                    entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                    entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                    entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                    if(UtilValidate.isNotEmpty(productStoreId)) {
                        entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                    }
                    productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                    if (productCategoryLevel3List.size() > 0) {
                        productCategoryLevel3Info = productCategoryLevel3List.get(0);
                    }
                }
            }
        } else if ("2".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel2List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel2List.size() > 0) {
                productCategoryLevel2Info = productCategoryLevel2List.get(0);

                productCategoryLevel2Info = productCategoryLevel2List.get(0);
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(productStoreId)) {
                    entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                }
                productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                if (productCategoryLevel3List.size() > 0) {
                    productCategoryLevel3Info = productCategoryLevel3List.get(0);
                }
            }
        } else if ("3".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
            entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel3List.size() > 0) {
                productCategoryLevel3Info = productCategoryLevel3List.get(0);
            }
        }

        request.setAttribute("productCategoryLevel1List", productCategoryLevel1List);
        request.setAttribute("productCategoryLevel2List", productCategoryLevel2List);
        request.setAttribute("productCategoryLevel3List", productCategoryLevel3List);
        request.setAttribute("productCategoryLevel1Info", productCategoryLevel1Info);
        request.setAttribute("productCategoryLevel2Info", productCategoryLevel2Info);
        request.setAttribute("productCategoryLevel3Info", productCategoryLevel3Info);

        if (UtilValidate.isNotEmpty(productCategoryLevel1Info)) {
            request.setAttribute("productCategoryIdLevel1", productCategoryLevel1Info.getString("productCategoryId"));
        }
        if (UtilValidate.isNotEmpty(productCategoryLevel2Info)) {
            request.setAttribute("productCategoryIdLevel2", productCategoryLevel2Info.getString("productCategoryId"));
        }
        if (UtilValidate.isNotEmpty(productCategoryLevel3Info)) {
            request.setAttribute("productCategoryIdLevel3", productCategoryLevel3Info.getString("productCategoryId"));
        }
        request.setAttribute("productCategoryLevel", productCategoryLevel);
        request.setAttribute("success", true);

        return "success";
    }
    // Add by zhajh at 20160105 商品等级分类列表的根据ID查询处理 End


    // Add by zhajh at 20160101 商品分类的删除处理 Begin

    /**
     * 商品分类的删除处理ById
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductCategoryById(HttpServletRequest request, HttpServletResponse response) {
        // 产品分类Id
        String productCategoryId = request.getParameter("productCategoryId");//产品分类Id

        // 选择商品分类的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        // 根据条件取得商品分类的数据
        List<GenericValue> productCategoryList = FastList.newInstance();// 分类
//        List<GenericValue> productCategoryAttributeList = FastList.newInstance();//分类属性
        EntityCondition condition = EntityCondition.makeCondition("productCategoryId", EntityOperator.EQUALS, productCategoryId);
        try {
            productCategoryList = delegator.findList("ProductCategory", condition, null, null, null, false);// 分类
//        	productCategoryAttributeList =delegator.findList("ProductCategoryAttribute", condition, null ,null, null, false);// 分类属性
            // 删除处理
//        	// 分类属性
//        	if(productCategoryAttributeList.size()>0){
//        		delegator.removeAll(productCategoryAttributeList);
//        	}
            // 分类
            if (UtilValidate.isNotEmpty(productCategoryList)) {
                for (GenericValue productCategoryInfo : productCategoryList) {
                    productCategoryInfo.set("isDel", "Y");
                    productCategoryInfo.store();
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160101 商品分类的删除处理 Begin


    // Add by zhajh at 20160217 商品分类的删除的Check处理 Begin

    /**
     * 商品分类的删除的检查处理ById
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductCategoryByIdForCheck(HttpServletRequest request, HttpServletResponse response) {
        // 产品分类Id
        String productCategoryId = request.getParameter("productCategoryId");//产品分类Id
        // 产品分类等级
        String productCategoryLevel = request.getParameter("productCategoryLevel");//产品分类等级
        // 选择商品分类的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        // 检查结果
        String checkFlg = "Y";
        String errType = "";
        // 根据条件取得商品分类的数据
        List<GenericValue> productCategoryList = null;
        List<GenericValue> productList = null;
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        List<EntityCondition> conditionList = FastList.newInstance();
        conditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.EQUALS, productCategoryId));
        conditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
        //EntityCondition condition = EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.EQUALS,productCategoryId);
        try {
            productCategoryList = delegator.findList("ProductCategory", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, null, null, false);
            // 删除Check处理
            if (productCategoryList.size() > 0) {
                checkFlg = "N";
                errType = "Category";
            } else {
                if ("3".equals(productCategoryLevel)) {
                    List<EntityCondition> andExprs = FastList.newInstance();
                    andExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.EQUALS, productCategoryId));
                    andExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                    productList = delegator.findList("Product", EntityCondition.makeCondition(andExprs, EntityOperator.AND), null, null, null, false);
                    if (productList.size() > 0) {
                        checkFlg = "N";
                        errType = "Product";
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        // 检查结果
        request.setAttribute("resultFlg", "true");
        request.setAttribute("checkFlg", checkFlg);
        request.setAttribute("errType", errType);
        return "success";
    }
    // Add by zhajh at 20160217 商品分类的删除的Check处理 Begin

    // Add by zhajh at 20160107 商品分类扩展属性的删除处理 Begin

    /**
     * 商品分类扩展属性的删除处理ById
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductExtendAttr(HttpServletRequest request, HttpServletResponse response) {
        // 产品分类Id
        String productCategoryId = request.getParameter("productCategoryId");//产品分类Id
        String attrName = request.getParameter("attrName");//商品分类扩展属性名Id

        List<EntityCondition> entityConditionList = FastList.newInstance();
        entityConditionList = FastList.newInstance();
        entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
        entityConditionList.add(EntityCondition.makeCondition("attrName", attrName));
        // 选择商品分类扩展属性 的删除处理
        // 根据条件取得商品分类的扩展属性的数据
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productCategoryAttributeList = FastList.newInstance();
        List<GenericValue> productOptionList = FastList.newInstance();
        try {
            productCategoryAttributeList = delegator.findList("ProductCategoryAttribute", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
            productOptionList = delegator.findList("ProductOption", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
            // 选项表内容的删除
            if (UtilValidate.isNotEmpty(productOptionList)) {
                delegator.removeAll(productOptionList);
            }

            // 扩展属性表内容的删除处理
            if (UtilValidate.isNotEmpty(productCategoryAttributeList)) {
                delegator.removeAll(productCategoryAttributeList);
            }
        } catch (GenericEntityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            request.setAttribute("success", false);
            request.setAttribute("resultFlg", false);
            return "error";
        }
        // 保存成功
        request.setAttribute("success", true);
        request.setAttribute("resultFlg", true);
        return "success";
    }
    // Add by zhajh at 20160107  商品分类扩展属性的删除处理 End


    // Add by zhajh at 20160107 商品分类扩展属性选项的删除处理 Begin

    /**
     * 商品分类扩展属性选项的删除处理ById
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductExtendOption(HttpServletRequest request, HttpServletResponse response) {
        // 产品分类Id
//        String productCategoryId = request.getParameter("productCategoryId");//产品分类Id
//        String attrName = request.getParameter("attrName");//商品分类扩展属性名Id
        String productOptionId = request.getParameter("productOptionId");//商品分类扩展属性名id

        List<EntityCondition> entityConditionList = FastList.newInstance();
        entityConditionList = FastList.newInstance();
//		entityConditionList.add(EntityCondition.makeCondition("productCategoryId",productCategoryId));
//		entityConditionList.add(EntityCondition.makeCondition("attrName",attrName));
        entityConditionList.add(EntityCondition.makeCondition("productOptionId", productOptionId));

        // 选择商品分类扩展属性 的删除处理
        // 根据条件取得商品分类的扩展属性的数据
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productOptionList = FastList.newInstance();
        try {
            productOptionList = delegator.findList("ProductOption", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
            // 选项表内容的删除
            if (UtilValidate.isNotEmpty(productOptionList)) {
                delegator.removeAll(productOptionList);
            }
        } catch (GenericEntityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            request.setAttribute("success", false);
            request.setAttribute("resultFlg", false);
            return "error";
        }
        // 保存成功
        request.setAttribute("success", true);
        request.setAttribute("resultFlg", true);
        return "success";
    }
    // Add by zhajh at 20160107  商品分类扩展属性选项的删除处理 End


    /**
     * 商品特征的查询处理
     *

     * @return
     */
    public static Map<String, Object> findProductFeature(DispatchContext dct, Map<String, ? extends Object> context) {
        Delegator delegator = dct.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }

//        String orderFiled = (String) context.get("ORDER_FILED");
//        String orderFiledBy = (String) context.get("ORDER_BY");

        String orderFiled = (String) context.get("sortField");
        String orderFiledBy = (String) context.get("sortType");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        String productFeatureTypeName = (String) context.get("productFeatureTypeName");// 特征名称
        String description = (String) context.get("description");// 特征备注

        List<GenericValue> productFeatureList = FastList.newInstance();
        int productFeatureListSize = 0;
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
        // blank param list
        String paramList = "";

        DynamicViewEntity dynamicView = new DynamicViewEntity();

        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();

        // default view settings
        dynamicView.addMemberEntity("PFT", "ProductFeatureType");
        dynamicView.addAliasAll("PFT", "", null);
        dynamicView.addAlias("PFT", "productFeatureTypeId");
        dynamicView.addAlias("PFT", "productFeatureTypeName");
        dynamicView.addAlias("PFT", "description");
        fieldsToSelect.add("productFeatureTypeId");
        fieldsToSelect.add("productFeatureTypeName");
        fieldsToSelect.add("description");

        if (UtilValidate.isNotEmpty(orderFiled)) {
//            orderBy.add(orderFiled + " " + orderFiledBy);
            if ("desc".equals(orderFiledBy)) {
                orderBy.add("-" + orderFiled);
            } else {
                orderBy.add(orderFiled);
            }
        } else {
            orderBy.add("-productFeatureTypeId");
        }

        //查询
        // 特征名称
        if (UtilValidate.isNotEmpty(productFeatureTypeName)) {
            paramList = paramList + "&productFeatureTypeName=" + productFeatureTypeName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productFeatureTypeName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + productFeatureTypeName + "%")));
        }
        // 特征备注
        if (UtilValidate.isNotEmpty(description)) {
            paramList = paramList + "&description=" + description;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("description"), EntityOperator.LIKE, EntityFunction.UPPER("%" + description + "%")));
        }
        andExprs.add(EntityCondition.makeCondition("productFeatureTypeName", EntityOperator.NOT_EQUAL, null));
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
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
                // using list iterator
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

                // get the partial list for this page
                productFeatureList = pli.getPartialList(lowIndex, viewSize);

                // attempt to get the full size
                productFeatureListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > productFeatureListSize) {
                    highIndex = productFeatureListSize;
                }

                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in productBrand find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            productFeatureListSize = 0;
        }
        result.put("productFeatureList", productFeatureList);
        result.put("productFeatureListSize", Integer.valueOf(productFeatureListSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }
    // Add by zhajh at 20160101 商品特征 End


    // Add by zhajh at 20160101 商品特征的删除处理 Begin

    /**
     * 商品特征的删除处理ByIds
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductFeatureByIds(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品特征记录 ids
        String ids = request.getParameter("checkedIds");
        String featureIds = ""; // 已选择的品牌ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                featureIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品特征的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> featureIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(featureIds)) {
            featureIdsList = UtilMisc.toListArray(featureIds.split(","));
        }
        // 根据条件取得商品特征的数据
        List<GenericValue> productFeatureTypeInfoList = null;
        EntityCondition condition = EntityCondition.makeCondition("productFeatureTypeId", EntityOperator.IN, featureIdsList);
        try {
            productFeatureTypeInfoList = delegator.findList("ProductFeatureType", condition, null, null, null, false);
            // 删除处理
            if (UtilValidate.isNotEmpty(productFeatureTypeInfoList)) {
                for (GenericValue productFeatureTypeInfo : productFeatureTypeInfoList) {
                    List<GenericValue> productFeatureDelList = null;
                    productFeatureDelList = delegator.findList("ProductFeature", EntityCondition.makeCondition("productFeatureTypeId", productFeatureTypeInfo.get("productFeatureTypeId")), null, null, null, true);
                    delegator.removeAll(productFeatureDelList);
                    productFeatureTypeInfo.remove();
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160101 商品特征的删除处理 Begin


    // Add by zhajh at 20160307 商品特征的删除处理（删除跳转问题的修改） Begin

    /**
     * 商品特征的删除处理ByIds（删除跳转问题的修改）
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductFeatureByIds1(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品特征记录 ids
        String ids = request.getParameter("checkedIds");
        String viewSize = request.getParameter("VIEW_SIZE");
        String viewIndex = request.getParameter("VIEW_INDEX");
        String productFeatureTypeName = request.getParameter("productFeatureTypeName");
        String description = request.getParameter("description");
        String sortField = request.getParameter("sortField");
        String sortType = request.getParameter("sortType");
        //当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        int curViewIndex = 0;

        String featureIds = ""; // 已选择的品牌ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                featureIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品特征的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> featureIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(featureIds)) {
            featureIdsList = UtilMisc.toListArray(featureIds.split(","));
        }
        // 根据条件取得商品特征的数据
        List<GenericValue> productFeatureTypeInfoList = null;
        EntityCondition condition = EntityCondition.makeCondition("productFeatureTypeId", EntityOperator.IN, featureIdsList);
        try {
            productFeatureTypeInfoList = delegator.findList("ProductFeatureType", condition, null, null, null, false);
            // 删除处理
            if (UtilValidate.isNotEmpty(productFeatureTypeInfoList)) {
                for (GenericValue productFeatureTypeInfo : productFeatureTypeInfoList) {
                    List<GenericValue> productFeatureAssoocList = null;
                    List<GenericValue> productFeatureDelList = null;

                    productFeatureDelList = delegator.findList("ProductFeature", EntityCondition.makeCondition("productFeatureTypeId", productFeatureTypeInfo.get("productFeatureTypeId")), null, null, null, true);
                    // 删除商品特征关系数据
                    for (GenericValue productFeatureInfo : productFeatureDelList) {
                        productFeatureAssoocList = delegator.findByAnd("ProductFeatureAssoc", UtilMisc.toMap("productFeatureId", productFeatureInfo.getString("productFeatureId")));
                        delegator.removeAll(productFeatureAssoocList);
                    }
                    // 删除商品特征值数据
                    delegator.removeAll(productFeatureDelList);
                    // 删除商品特征类型数据
                    productFeatureTypeInfo.remove();
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        // 取得ViewIndex
        int tempViewIndex = Integer.valueOf(viewIndex);
        boolean flg = true;
        for (int i = tempViewIndex; i > 0; i--) {
            Map<String, String> passedParams = FastMap.newInstance();
            passedParams.put("VIEW_SIZE", viewSize);
            passedParams.put("VIEW_INDEX", i + "");
            passedParams.put("productFeatureTypeName", productFeatureTypeName);
            passedParams.put("description", description);
            passedParams.put("sortField", sortField);
            passedParams.put("sortType", sortType);
            flg = getCurViewIndex(passedParams, userLogin, dispatcher);
            if (flg) {
                curViewIndex = i;
                break;
            }
        }
        // 保存成功
        request.setAttribute("resultFlg", "true");
        request.setAttribute("curViewIndex", curViewIndex);
        return "success";
    }

    /**
     * 取得最新的viewIndex的值
     *
     * @param map
     * @param userLogin
     * @param dispatcher
     * @return
     */
    private static boolean getCurViewIndex(Map<String, String> map, GenericValue userLogin, LocalDispatcher dispatcher) {
        String viewSize = map.get("VIEW_SIZE");
        String viewIndex = map.get("VIEW_INDEX");
        String productFeatureTypeName = map.get("productFeatureTypeName");
        String description = map.get("description");
        String sortField = map.get("sortField");
        String sortType = map.get("sortType");
        Map<String, Object> passedParams = FastMap.newInstance();
        passedParams.put("VIEW_INDEX", viewIndex);
        passedParams.put("VIEW_SIZE", viewSize);
        passedParams.put("productFeatureTypeName", productFeatureTypeName);
        passedParams.put("description", description);
        passedParams.put("lookupFlag", "Y");
        passedParams.put("sortField", sortField);
        passedParams.put("sortType", sortType);
        passedParams.put("userLogin", userLogin);
        Map<String, Object> resultMap = FastMap.newInstance();
        boolean flg = true;
        try {
            resultMap = dispatcher.runSync("findProductFeature", passedParams);
            List<GenericValue> productFeatureList = (List<GenericValue>) resultMap.get("productFeatureList");
            if (productFeatureList.size() > 0) {
                flg = true;
            } else {
                if (Integer.valueOf(viewIndex) != 0) {
                    flg = false;
                } else {
                    flg = true;
                }
            }
        } catch (GenericServiceException e) {

        }
        return flg;
    }
    // Add by zhajh at 20160307 商品特征的删除处理（删除跳转问题的修改） Begin


    /**
     * 保存商品特征列表信息
     *
     * @param request
     * @param response
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String saveProductFeatureList(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        List<Map<String, Object>> sessionFeatureList = FastList.newInstance();
        String tFeatureInfos = request.getParameter("tFeatureInfos");//输入产品特征值
        if (UtilValidate.isNotEmpty(tFeatureInfos)) {
            String[] tFeatureInfosArray = tFeatureInfos.split(",");
            for (String tFeatureInfo : tFeatureInfosArray) {
                int tempIndex = tFeatureInfo.indexOf("|");
                String productFeatureName = tFeatureInfo.substring(0, tempIndex);//特征值名称
                String sequenceNum = tFeatureInfo.substring(tempIndex + 1);//排序号
                Map<String, Object> mapTemp;
                if (UtilValidate.isNotEmpty(productFeatureName) && UtilValidate.isNotEmpty(sequenceNum)) {
                    mapTemp = FastMap.newInstance();
                    mapTemp.put("productFeatureName", productFeatureName);
                    mapTemp.put("sequenceNum", sequenceNum);
                    sessionFeatureList.add(mapTemp);
                }
            }
        }
        if (sessionFeatureList.size() > 0) {
            session.setAttribute("sessionFeatures", sessionFeatureList);
        }

        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }

    /**
     * 删除商品特征session
     *
     * @param request
     * @param response
     * @return
     */
    public static String clearProductFeature(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
//        String flag = request.getParameter("clearFlag");
//        if (UtilValidate.isNotEmpty(flag) && flag.equals("Y")){
        session.setAttribute("sessionFeatures", "");
//        }
        return "success";
    }


    // Add by zhajh at 20160104 根据特征类型取出商品特征值 Begin

    /**
     * 根据特征类型取出商品特征值
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static String getProductFeatureByTypeId(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String productFeatureTypeId = request.getParameter("productFeatureTypeId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productFeatureByTypeIdList = null;
        productFeatureByTypeIdList = delegator.findList("ProductFeature", EntityCondition.makeCondition("productFeatureTypeId", productFeatureTypeId), null, null, null, true);
        GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeId));
        // 保存成功
        request.setAttribute("productFeatureByTypeIdList", productFeatureByTypeIdList);
        request.setAttribute("resultFlg", "true");
        request.setAttribute("productFeatureTypeInfo", productFeatureTypeInfo);

        return "success";
    }
    // Add by zhajh at 20160104 根据特征类型取出商品特征值 End


    // Add by zhajh at 20160106 品牌导入 Begin

    /**
     * 品牌导入
     *

     * @return
     */
    public static void brandImport(HttpServletRequest request, HttpServletResponse response) {
        //返回信息，json格式
        String returnJson = "";
        //当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        //Delegator对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            //调用Excel导入方法
            Map rs = dispatcher.runSync("excelImport", UtilMisc.toMap(
                    "request", request,
                    "xmlUrl", "src/org/ofbiz/product/product/BrandValidate.xml",
                    "validateCellData", "brandValidateCell"));
            //获取导入的信息
            returnJson = rs.get("msg").toString();
            //获取导入的数据list
            List<Map> listDatas = (List<Map>) rs.get("listDatas");
            //遍历list，进行新增或修改操作

            for (Map record : listDatas) {
                String brandNameAlias = (String) record.get("brand_name_alias");//品牌别名
                String isUsed = (String) record.get("is_used");//是否启用
                String brandName = (String) record.get("brand_name");//品牌名称
                GenericValue productBrand = null;
                String productBrandIdCreate = delegator.getNextSeqId("ProductBrand");
                productBrand = delegator.makeValue("ProductBrand", UtilMisc.toMap("productBrandId", productBrandIdCreate));
                // 品牌名称
                productBrand.set("brandName", brandName);
                // 品牌别名
                if (UtilValidate.isNotEmpty(brandNameAlias)) {
                    productBrand.set("brandNameAlias", brandNameAlias);
                }
                // 是否启用标识
                if ("是".equals(isUsed)) {
                    productBrand.set("isUsed", "Y");
                } else if ("否".equals(isUsed)) {
                    productBrand.set("isUsed", "N");
                }
                // 删除状态(N:未删除)
                productBrand.set("isDel", "N");
                // 审核状态（1：审核通过）
                productBrand.set("auditStatus", "1");
                // 创建表
                productBrand.create();
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.write(returnJson);
            out.flush();
            out.close();
        }
    }
    // Add by zhajh at 20160106 品牌导入 End


    /**
     * 评价列表查询 Add by Wcy 2016.01.19
     *

     * @return
     */
    public static Map<String, Object> findProductReview(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 响应结果 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取本地配置 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        String orderId = (String) context.get("orderId");
        String productId = (String) context.get("productId");

        int max = 0;
        /** 查询结果集 */
        List<GenericValue> productReviewList = null;
        List<Map<String, Object>> reviewList = null;
        /** 返回json数值 */
        Map<String, Object> resultData = FastMap.newInstance();

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
        if (UtilValidate.isNotEmpty(userLoginId)) {
            GenericValue userLogin = null;
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                result.put("resultData", resultData);
                return result;
            }
            if (null == userLogin) {
                result.put("resultData", resultData);
                return result;
            }
        }
        /** 定义评价视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PV", "ProductReview");
        dynamicViewEntity.addAlias("PV", "orderId");
        dynamicViewEntity.addAlias("PV", "productReviewId");
        dynamicViewEntity.addAlias("PV", "productRating");
        dynamicViewEntity.addAlias("PV", "productReview");
        dynamicViewEntity.addAlias("PV", "postedDateTime");
        dynamicViewEntity.addAlias("PV", "productId");
        // add by gss
        dynamicViewEntity.addAlias("PV", "userLoginId");
        dynamicViewEntity.addAlias("PV", "isShow");
        dynamicViewEntity.addAlias("PV", "postedAnonymous");

        /** 查询字段 & 排序字段 */
        List<String> fieldToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        EntityCondition condition = null;
        orderBy.add("-postedDateTime");

        fieldToSelect.add("orderId");
        fieldToSelect.add("productReviewId");
        fieldToSelect.add("productRating");
        fieldToSelect.add("productReview");
        fieldToSelect.add("postedDateTime");
        fieldToSelect.add("postedAnonymous");
        fieldToSelect.add("productId");
        fieldToSelect.add("userLoginId");

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //orderId和productId不会同时存在
        //orderId不为空时查询该订单商品的评价
        andExprs.add(EntityCondition.makeCondition("isShow", EntityOperator.EQUALS, "1"));
        if (UtilValidate.isNotEmpty(orderId)) {
            andExprs.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
            // add by gss
            resultData.put("max", 1);
        }

        //productId不为空时查询该商品的所有评价
        if (UtilValidate.isNotEmpty(productId)) {
            andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
        /*    try {
                Set<String> fields = FastSet.newInstance();
                fields.add("orderId");
                List<GenericValue> orderList = delegator.findList("OrderItem",
                        EntityCondition.makeCondition(UtilMisc.toList(
                                     EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
                                     EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_WAITEVALUATE")
                                )
                        ), fields, null, null, false);
                condition = EntityCondition.makeCondition("orderId", EntityOperator.IN,orderList);
                andExprs.add(condition);
                resultData.put("max", orderList.size());
            } catch (GenericEntityException e) {
                Debug.logError(e,module);
            }*/
        }
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        /** 查询开始条数*/
        int lowIndex = 0;
        /** 查询结束条数*/
        int highIndex = 0;
        /** 查询结果集*/
        List<GenericValue> orderList = null;
        int orderListSize = 0;
        try {
            //计算开始分页值 & 计算分页结束值
            lowIndex = viewIndex + 1;
            highIndex = viewIndex + viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldToSelect, orderBy, findOpts);
            //获取分页结果集
            productReviewList = pli.getPartialList(lowIndex, viewSize);
            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
       /* if (null == orderList) {
            productReviewList = FastList.newInstance();
        }*/
        if (null == productReviewList) {
            productReviewList = FastList.newInstance();
        }


        //orderId和productId不会同时存在
        //orderId不为空时查询该订单商品的评价，

        //if(UtilValidate.isNotEmpty(orderId)){
        //    condition = EntityCondition.makeCondition("orderId", EntityOperator.EQUALS,orderId);
        //    resultData.put("max",1);
        //}
        //
        ////productId不为空时查询该商品的所有评价
        //if(UtilValidate.isNotEmpty(productId)){
        //    try {
        //        Set<String> fieldToSelect = FastSet.newInstance();
        //        fieldToSelect.add("orderId");
        //        List<GenericValue> orderList = delegator.findList("OrderItem",
        //EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId), fieldToSelect,null,null,false);
        //        condition = EntityCondition.makeCondition("orderId", EntityOperator.IN,orderList);
        //        resultData.put("max", orderList.size());
        //    } catch (GenericEntityException e) {
        //        Debug.logError(e,module);
        //    }
        //}
        //
        //try {
        //    productReviewList = delegator.findList("ProductReview", condition, null, orderBy, null, false);
        //} catch (GenericEntityException e) {
        //    Debug.logError(e,module);
        //}
        reviewList = changeJsonFromList(productReviewList, delegator, locale, timeZone);
        if (null == productReviewList) {
            result.put("resultData", resultData);
            return result;
        }
        resultData.put("reviewList", reviewList);
        result.put("resultData", resultData);
        return result;
    }


    /**
     * 评价列表返回json字符串
     *
     * @param list
     * @param delegator
     * @param locale
     * @param timeZone
     * @return
     */
    public static List<Map<String, Object>> changeJsonFromList(List<GenericValue> list, Delegator delegator, Locale locale, TimeZone timeZone) {
        List<Map<String, Object>> result = FastList.newInstance();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (GenericValue v : list) {
            Map<String, Object> map = FastMap.newInstance();
            GenericValue person = getPartyHead(v.getString("userLoginId"), delegator);
            if (null != person) {
                map.put("imgUrl", person.getString("headphoto"));   //评价人头像路径
                // map.put("picSrc", person.getString("headphoto"));
                map.put("personName", person.getString("name")); //评价人姓名
            } else {
                map.put("imgUrl", "");   //评价人头像路径
                //map.put("picSrc", "");   //评价人头像路径
                map.put("personName", ""); //评价人姓名
            }

            map.put("productReviewId", v.getString("productReviewId"));
            //是否匿名
            map.put("postAnonymous", v.getString("postedAnonymous"));
            //转换成整数
            map.put("productRating", v.getBigDecimal("productRating").multiply(new BigDecimal(2)).intValue()); //评价分数四舍五入后 * 2
            map.put("score", v.getBigDecimal("productRating").multiply(new BigDecimal(2))); //评价分数四舍五入后 * 2
            // map.put("evaluateTime",   formatter.format(UtilDateTime.timeStampToString(v.getTimestamp("postedDateTime"),"yyyy-MM-dd HH:mm",timeZone,locale))); //评价时间
            // add by  gss
            map.put("evaluateTime", formatter.format(v.getTimestamp("postedDateTime"))); //评价时间
            map.put("productReview", v.getString("productReview")); //评价内容
            //评价图片
            List<Map<String, Object>> dataResourceList = ProductWorker.getProductReviewContent(delegator, v.getString("productReviewId"));
            List<String> imgList = new FastList<String>();
            if (UtilValidate.isNotEmpty(dataResourceList)) {
                for (int i = 0; i < dataResourceList.size(); i++) {
                    imgList.add((String) dataResourceList.get(i).get("objectInfo"));
                }
            }
            map.put("reviewImages", imgList);
            // add  by gss
            result.add(map);
        }
        return result;
    }

    public static GenericValue getPartyHead(String userLoginId, Delegator delegator) {
        GenericValue userLogin = null;
        GenericValue person = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return null;
        }
        if (null == userLogin) {
            return null;
        }

        try {
            person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.getString("partyId")));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        return person;

    }
    /*********************************************************************/

    // Add by zhajh at 20160112 品牌 Begin

    // Add by zhajh at 20160112 品牌 Begin

    /**
     * 商品的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> updateProductIcoPro(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String operateType = (String) context.get("operateType");//操作类型


        String productTypeId = (String) context.get("productTypeId");//商品类型
        String productCategoryId = (String) context.get("productCategoryId");//商品分类ID
        String productId = (String) context.get("productId");//商品编号ID
        String mainProductId = (String) context.get("mainProductId");//主商品编号ID
        String productName = (String) context.get("productName");//商品名称
        String productSubheadName = (String) context.get("productSubheadName"); //商品副标题

        String isOnline = (String) context.get("isOnline"); //是否上下架

//        String introductionDate = (String)context.get("introductionDate");//销售开始时间
//        String salesDiscontinuationDate =(String)context.get("salesDiscontinuationDate");//销售结束时间

        Timestamp introductionDate = (Timestamp) context.get("startTime");
        Timestamp salesDiscontinuationDate = (Timestamp) context.get("endTime");

        String businessPartyId = (String) context.get("businessPartyId");//商家名称
        String brandId = (String) context.get("brandId"); //商品品牌Id

        String volume = (String) context.get("volume"); //体积
        String weight = (String) context.get("weight"); //重量
        String isUsedFeature = (String) context.get("isUsedFeature"); //是否使用特征
        String seoKeyword = (String) context.get("seoKeyword"); //SEO关键字
        String pcDetails = (String) context.get("pcDetails"); //PC端详情
        String mobileDetails = (String) context.get("mobileDetails"); //移动端详情

        String productAttrInfos = (String) context.get("productAttrInfos"); //商品属性
        String productParameterInfos = (String) context.get("productParameterInfos"); //商品参数
        String productFacilityInfos = (String) context.get("productFacilityInfos"); //商品库存
        String productFeatureInfos = (String) context.get("productFeatureInfos");//商品特征
        String featureProductName = (String) context.get("featureProductName");// 商品特征名称
        String featureProductId = (String) context.get("featureProductId");// 商品特征ID
        String productContentInfos = (String) context.get("productContentInfos"); //商品图片
        String productAssocInfos = (String) context.get("productAssocInfos"); //关联商品
        String isSku = (String) context.get("isVirtual"); //是否是Sku商品

        String productFeatureGoodsDelIds = (String) context.get("productFeatureGoodsDelIds"); //特征商品删除信息

//        String contentId = "";
//        String fileName = (String) context.get("_uploadedFile_fileName");
//        String contentType = (String) context.get("_uploadedFile_contentType");
//        ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
//        String productContentTypeId =(String)context.get("productContentTypeId");


        String salePrice = (String) context.get("salePrice"); //销售价格
        String marketPrice = (String) context.get("marketPrice"); //市场价格
        String costPrice = (String) context.get("costPrice"); //成本价格
        String productTags = (String) context.get("productTags"); //商品标签
        String[] tags = null;
        if (UtilValidate.isNotEmpty(productTags)) {
            tags = productTags.split(",");//生成商品标签数组
        }


        List<Map<String, Object>> productAttrList = FastList.newInstance();//商品属性列表
        List<Map<String, Object>> productParameterList = FastList.newInstance();//商品参数列表
        List<Map<String, Object>> productFacilityList = FastList.newInstance();//商品库存列表
        List<Map<String, Object>> productFeatureList = FastList.newInstance();//商品特征列表
        List<Map<String, Object>> productContentList = FastList.newInstance();//商品图片列表
        List<Map<String, Object>> productAssocList = FastList.newInstance();//商品关系列表
        List<EntityCondition> entityConditionList = FastList.newInstance();

        // Add by zhajh at 20180306 yabiz相关内容 Begin
        String isRecommendHomePage = (String) context.get("isRecommendHomePage"); //推荐到首页
        String isSupportService = (String) context.get("isSupportService"); //支持服务

        String supportServiceType = (String) context.get("supportServiceType"); //支持服务类型
        // 取得商品和支持服务数据
        String[] supportServiceTypeInfos = null;
        if (UtilValidate.isNotEmpty(supportServiceType)) {
            supportServiceTypeInfos = supportServiceType.split(",");//生成商品服务支持数组
        }
        String integralDeductionType = (String) context.get("integralDeductionType"); //积分抵扣
        String integralDeductionUpper = (String) context.get("integralDeductionUpper"); //积分抵扣上限
        String purchaseLimitationQuantity = (String) context.get("purchaseLimitationQuantity"); //每人限购数量
        String isListShow = (String) context.get("isListShow"); //列表展示
        String voucherAmount = (String) context.get("voucherAmount"); //代金券面额
        String useLimit = (String) context.get("useLimit"); //使用限制
        Timestamp useStartTime = (Timestamp) context.get("useStartTime"); //使用开始时间
        Timestamp useEndTime = (Timestamp) context.get("useEndTime"); //使用结束时间
        String isBondedGoods = (String) context.get("isBondedGoods"); //是否保税商品
        String productStoreId = (String) context.get("productStoreId"); //商铺信息
        String isInner = (String) context.get("isInner"); //是否自营
        String platformClassId = (String) context.get("platformClassId"); //平台分类
        String providerId = (String) context.get("providerId"); //供应商编码


        if(UtilValidate.isNotEmpty(productStoreId)){
            try {
                List<GenericValue> partyGroupInfos = delegator.findByAnd("PartyGroup", UtilMisc.toMap("productStoreId", productStoreId));
                if(UtilValidate.isNotEmpty(partyGroupInfos)){
                    businessPartyId= EntityUtil.getFirst(partyGroupInfos).getString("partyId");
                }
            }catch (Exception e){

            }
        }
        // Add by zhajh at 20180306 yabiz相关内容 End
        if ("create".equals(operateType) || "update".equals(operateType)) {

        }
        if ("create".equals(operateType)) {
            try {
                String productIdCreate = null;
                GenericValue product = null;
                String isHasEntity = "Y";// 判断是否存在准备商品实体
                // 有准备商品的场合
                if (UtilValidate.isNotEmpty(productId)) {
                    productIdCreate = productId;
                    product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    //准备商品标记清空
                    if (UtilValidate.isNotEmpty(product)) {
//                        product.set("isPrepareEntity", "");
                    } else {
                        isHasEntity = "N";
                    }
                    // 没有准备商品的场合
                } else {
                    productIdCreate = delegator.getNextSeqId("Product");
                    product = delegator.makeValue("Product", UtilMisc.toMap("productId", productIdCreate));
                }
                // 在没有实体的处理
                if ("N".equals(isHasEntity)) {
                    productIdCreate = delegator.getNextSeqId("Product");
                    product = delegator.makeValue("Product", UtilMisc.toMap("productId", productIdCreate));
                }

                // 是否审核的判断
                String chkVerifyFlg="Y";
                List<GenericValue> productRulesInfo=delegator.findByAnd("ProductRules");
                if(UtilValidate.isNotEmpty(productRulesInfo)){
                    if("Y".equals(isInner)){
                        if("N".equals(productRulesInfo.get(0).getString("physicalProductStatus"))){
                            chkVerifyFlg="N";
                        }
                    }else{
                        if("N".equals(productRulesInfo.get(0).getString("virtualProductStatus"))){
                            chkVerifyFlg="N";
                        }
                    }
                }

                //商品类型
                if (UtilValidate.isNotEmpty(productTypeId)) {
                    product.set("productTypeId", productTypeId);
                }
                //商品分类ID
                if (UtilValidate.isNotEmpty(productCategoryId)) {
                    product.set("primaryProductCategoryId", productCategoryId);
                }
                //商品名称
                if (UtilValidate.isNotEmpty(productName)) {
                    product.set("productName", productName);
                }
                //商品副标题
                if (UtilValidate.isNotEmpty(productSubheadName)) {
                    product.set("productSubheadName", productSubheadName);
                    product.set("internalName", productSubheadName);
                } else {
                    product.set("internalName", productName);

                }
                //是否上下架
                if (UtilValidate.isNotEmpty(isOnline)) {
                    if("Y".equals(isOnline)){
                        if("N".equals(chkVerifyFlg)){
                            product.set("isVerify","Y");
                        }
                    }
                    product.set("isOnline", isOnline);
                }
                //销售开始时间
                if (UtilValidate.isNotEmpty(introductionDate)) {
                    product.set("introductionDate", introductionDate);
                }
                //销售结束时间
                if (UtilValidate.isNotEmpty(salesDiscontinuationDate)) {
                    product.set("salesDiscontinuationDate", salesDiscontinuationDate);
                }
                //商家名称
                if (UtilValidate.isNotEmpty(businessPartyId)) {
                    product.set("businessPartyId", businessPartyId);
                }
                //商品品牌Id
                if (UtilValidate.isNotEmpty(brandId)) {
                    product.set("brandId", brandId);
                }
                //体积
                if (UtilValidate.isNotEmpty(volume)) {
                    product.set("volume", new BigDecimal(volume));
                }
                //重量
                if (UtilValidate.isNotEmpty(weight)) {
                    product.set("weight", new BigDecimal(weight));
                }
                //是否使用特征
                if (UtilValidate.isNotEmpty(isUsedFeature)) {
                    product.set("isUsedFeature", isUsedFeature);
                }
                //SEO关键字
                if (UtilValidate.isNotEmpty(seoKeyword)) {
                    product.set("seoKeyword", seoKeyword);
                }
                //PC端详情
                if (UtilValidate.isNotEmpty(pcDetails)) {
                    product.set("pcDetails", pcDetails);
                }
                //移动端详情
                if (UtilValidate.isNotEmpty(mobileDetails)) {
                    product.set("mobileDetails", mobileDetails);
                }

                //主商品编号
                if (UtilValidate.isNotEmpty(mainProductId)) {
                    product.set("mainProductId", mainProductId);
                }
                //审核状态
                // 商品特征名称
                if (UtilValidate.isNotEmpty(featureProductName)) {

                    product.set("featureProductName", featureProductName);
                }

                // 商品特征Id
                if (UtilValidate.isNotEmpty(featureProductId)) {

                    product.set("featureProductId", featureProductId);
                }

                //是否是sku
                if (UtilValidate.isNotEmpty(isSku)) {
                    product.set("isVirtual", "Y");
                    product.set("isVariant", "N");
                } else {
                    if (UtilValidate.isNotEmpty(mainProductId)) {
                        product.set("isVirtual", "N");
                        product.set("isVariant", "Y");
                    } else {
                        product.set("isVirtual", "N");
                        product.set("isVariant", "N");
                    }
                }

                // 推荐到首页
                if (UtilValidate.isNotEmpty(isRecommendHomePage)) {
                    product.set("isRecommendHomePage", isRecommendHomePage);
                }
                // 支持服务
                if (UtilValidate.isNotEmpty(isSupportService)) {
                    product.set("isSupportService", isSupportService);
                }
                // 支持服务类型
                //if(UtilValidate.isNotEmpty(supportServiceType)){
                //    product.set("supportServiceType", supportServiceType);
                //}
                // 积分抵扣
                if (UtilValidate.isNotEmpty(integralDeductionType)) {
                    product.set("integralDeductionType", integralDeductionType);
                }
                // 积分抵扣上限
                if (UtilValidate.isNotEmpty(integralDeductionUpper)) {
                    product.set("integralDeductionUpper", new BigDecimal(integralDeductionUpper));
                }
                // 每人限购数量
                if (UtilValidate.isNotEmpty(purchaseLimitationQuantity)) {
                    product.set("purchaseLimitationQuantity", new BigDecimal(purchaseLimitationQuantity));
                }
                // 列表展示
                if (UtilValidate.isNotEmpty(isListShow)) {
                    product.set("isListShow", isListShow);
                }
                // 代金券面额
                if (UtilValidate.isNotEmpty(voucherAmount)) {
                    product.set("voucherAmount", new BigDecimal(voucherAmount));
                }
                // 使用限制
                if (UtilValidate.isNotEmpty(useLimit)) {
                    product.set("useLimit", useLimit);
                }
                // 使用开始时间
                if (UtilValidate.isNotEmpty(useStartTime)) {
                    product.set("useStartTime", useStartTime);
                }
                // 使用结束时间
                if (UtilValidate.isNotEmpty(useEndTime)) {
                    product.set("useEndTime", useEndTime);
                }
                // 是否保税商品
                if (UtilValidate.isNotEmpty(isBondedGoods)) {
                    product.set("isBondedGoods", isBondedGoods);
                }

                // 是否自营
                if (UtilValidate.isNotEmpty(isInner)) {
                    product.set("isInner", isInner);
                }

                // 平台分类
                if (UtilValidate.isNotEmpty(platformClassId)) {
                    product.set("platformClassId", platformClassId);
                }

                // 供应商
                if(UtilValidate.isNotEmpty(providerId)){
                    product.set("providerId",providerId);
                }

                // 删除标记
                product.set("isDel", "N");
                // 商品数据登陆
//	            product.create();
                // 有准备商品的场合
                if (UtilValidate.isNotEmpty(productId)) {
                    if ("N".equals(isHasEntity)) {
                        product.create();
                    } else {
                        product.store();
                    }
                    // 没有准备商品的场合
                } else {
                    product.create();
                }
                result.put("productId", productIdCreate);
//	            // 商品和品牌关系数据的登录
//	            if(UtilValidate.isNotEmpty(brandId)){
//	            	GenericValue productBrandAssoc = null;
//            		String productBrandAssocIdCreate = delegator.getNextSeqId("ProductBrandAssoc");
//            		productBrandAssoc = delegator.makeValue("ProductTagAssoc",UtilMisc.toMap("productBrandAssocId",productBrandAssocIdCreate));
//  	            	 // 商品ID
//            		productBrandAssoc.set("productId", productIdCreate);
//  	            	 // 标签ID
//            		productBrandAssoc.set("brandId", brandId);
//  	                 // 创建表
//            		productBrandAssoc.create();
//	            }


                


                // 商品标签关系表数据的登陆
                if (UtilValidate.isNotEmpty(tags)) {
                    for (int i = 0; i < tags.length; i++) {
                        GenericValue productTagAssoc = null;
                        String productTagAssocIdCreate = delegator.getNextSeqId("ProductTagAssoc");
                        productTagAssoc = delegator.makeValue("ProductTagAssoc", UtilMisc.toMap("productTagAssocId", productTagAssocIdCreate));
                        // 商品ID
                        productTagAssoc.set("productId", productIdCreate);
                        // 标签ID
                        productTagAssoc.set("tagId", tags[i]);
                        // 创建表
                        productTagAssoc.create();
                    }
                }


                // 商品和支持服务关系表数据的登陆
                if (UtilValidate.isNotEmpty(supportServiceTypeInfos)) {
                    for (int i = 0; i < supportServiceTypeInfos.length; i++) {
                        GenericValue productSupportServiceAssoc = null;
                        String productSupportServiceAssocIdCreate = delegator.getNextSeqId("ProductSupportServiceAssoc");
                        productSupportServiceAssoc = delegator.makeValue("ProductSupportServiceAssoc", UtilMisc.toMap("productSupportServiceAssocId", productSupportServiceAssocIdCreate));
                        // 商品ID
                        productSupportServiceAssoc.set("productId", productIdCreate);
                        // 服务支持ID
                        productSupportServiceAssoc.set("enumId", supportServiceTypeInfos[i]);
                        // 创建表
                        productSupportServiceAssoc.create();
                    }
                }


                // 商品价格表信息表的登陆

                //销售价格
                if (UtilValidate.isNotEmpty(salePrice)) {
                    Map<String, Object> passedParams = FastMap.newInstance();
                    passedParams.put("productId", productIdCreate);
                    passedParams.put("productPriceTypeId", "DEFAULT_PRICE");
                    passedParams.put("productPricePurposeId", "PURCHASE");
                    passedParams.put("currencyUomId", "CNY");
                    passedParams.put("productStoreGroupId", "_NA_");
                    passedParams.put("fromDate", UtilDateTime.nowTimestamp());
                    passedParams.put("price", new BigDecimal(salePrice));
                    passedParams.put("userLogin", userLogin);

                    Map<String, Object> resultMap = FastMap.newInstance();
                    try {
                        resultMap = dispatcher.runSync("createProductPrice", passedParams);
                    } catch (GenericServiceException e) {
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }
                //市场价格
                if (UtilValidate.isNotEmpty(marketPrice)) {
                    Map<String, Object> passedParams = FastMap.newInstance();
                    passedParams.put("productId", productIdCreate);
                    passedParams.put("productPriceTypeId", "MARKET_PRICE");
                    passedParams.put("productPricePurposeId", "PURCHASE");
                    passedParams.put("currencyUomId", "CNY");
                    passedParams.put("productStoreGroupId", "_NA_");
                    passedParams.put("fromDate", UtilDateTime.nowTimestamp());
                    passedParams.put("price", new BigDecimal(marketPrice));
                    passedParams.put("userLogin", userLogin);

                    Map<String, Object> resultMap = FastMap.newInstance();
                    try {
                        resultMap = dispatcher.runSync("createProductPrice", passedParams);
                    } catch (GenericServiceException e) {
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }

                //成本价格
                if (UtilValidate.isNotEmpty(costPrice)) {
                    Map<String, Object> passedParams = FastMap.newInstance();
                    passedParams.put("productId", productIdCreate);
                    passedParams.put("productPriceTypeId", "COST_PRICE");
                    passedParams.put("productPricePurposeId", "PURCHASE");
                    passedParams.put("currencyUomId", "CNY");
                    passedParams.put("productStoreGroupId", "_NA_");
                    passedParams.put("fromDate", UtilDateTime.nowTimestamp());
                    passedParams.put("price", new BigDecimal(costPrice));
                    passedParams.put("userLogin", userLogin);

                    Map<String, Object> resultMap = FastMap.newInstance();
                    try {
                        resultMap = dispatcher.runSync("createProductPrice", passedParams);
                    } catch (GenericServiceException e) {
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }
                // 商品属性表信息的登陆
                // 取得商品属性信息
                if (UtilValidate.isNotEmpty(productAttrInfos)) {
                    String[] tProductAttrInfosArray = productAttrInfos.split(",");
                    for (String attrInfo : tProductAttrInfosArray) {
                        String[] attrInfos = attrInfo.split("\\|");
                        String attrName = attrInfos[0];//分类属性名
                        String attrCategoryId = attrInfos[1];//分类ID
                        String attrOption = "";//选项Id
                        if (attrInfos.length == 3) {
                            attrOption = attrInfos[2];//选项Id
                        }
                        if (UtilValidate.isNotEmpty(attrOption)) {
                            Map<String, Object> mapTemp;
                            if (UtilValidate.isNotEmpty(attrName) && UtilValidate.isNotEmpty(attrOption)) {
                                mapTemp = FastMap.newInstance();
                                mapTemp.put("attrName", attrName);
                                mapTemp.put("attrCategoryId", attrCategoryId);
                                mapTemp.put("attrOption", attrOption);
                                productAttrList.add(mapTemp);
                            }
                        }
                    }
                }
                for (Map<String, Object> map : productAttrList) {
                    GenericValue productCategoryattributeAssoc = null;
                    String productCategoryattributeAssocIdCreate = delegator.getNextSeqId("ProductCategoryattributeAssoc");
                    productCategoryattributeAssoc = delegator.makeValue("ProductCategoryattributeAssoc", UtilMisc.toMap("productCategoryattributeAssocId", productCategoryattributeAssocIdCreate));
                    // 属性名称
                    productCategoryattributeAssoc.set("attrName", (String) map.get("attrName"));
                    // 选项ID
                    productCategoryattributeAssoc.set("productOptionId", (String) map.get("attrOption"));
                    // 属性分类ID
                    productCategoryattributeAssoc.set("productCategoryId", (String) map.get("attrCategoryId"));
                    // 商品ID
                    productCategoryattributeAssoc.set("productId", productIdCreate);
                    // 创建表
                    productCategoryattributeAssoc.create();
                }

                // 商品参数表信息的登陆

                // 取得商品参数的值
                if (UtilValidate.isNotEmpty(productParameterInfos)) {
                    String[] tProductParameterInfosArray = productParameterInfos.split(",");
                    for (String parameterInfo : tProductParameterInfosArray) {
                        String[] attrInfos = parameterInfo.split("\\|");
                        String parameterName = attrInfos[0];//参数名
                        String parameterDetails = attrInfos[1];//参数详情
                        Map<String, Object> mapTemp;
                        if (UtilValidate.isNotEmpty(parameterName) && UtilValidate.isNotEmpty(parameterDetails)) {
                            mapTemp = FastMap.newInstance();
                            mapTemp.put("parameterName", parameterName);
                            mapTemp.put("parameterDetails", parameterDetails);
                            productParameterList.add(mapTemp);
                        }
                    }
                }
                for (Map<String, Object> map : productParameterList) {
                    GenericValue productParameter = null;
                    String productParameterIdCreate = delegator.getNextSeqId("ProductParameter");
                    productParameter = delegator.makeValue("ProductParameter", UtilMisc.toMap("productParameterId", productParameterIdCreate));
                    // 参数名称
                    productParameter.set("parameterName", (String) map.get("parameterName"));
                    // 参数详情
                    productParameter.set("parameterDescription", (String) map.get("parameterDetails"));
                    // 商品ID
                    productParameter.set("productId", productIdCreate);
                    // 创建表
                    productParameter.create();
                }
                // 商品库存表信息的登陆
                // 取得商品库存信息
                if (UtilValidate.isNotEmpty(productFacilityInfos)) {
                    String[] tProductFacilityInfosArray = productFacilityInfos.split(",");
                    for (String facilityInfo : tProductFacilityInfosArray) {
                        String[] attrInfos = facilityInfo.split("\\|");

                        String facilityId = attrInfos[0];//仓库ID
                        String inventoryItemId = attrInfos[1];//库存明细ID
                        String accountingQuantityTotal = attrInfos[2];//可用数量
                        String warningQuantity = attrInfos[3];//预警数量
                        String warningMail = "";//预警邮箱
                        if (attrInfos.length == 5) {
                            warningMail = attrInfos[4];
                        }


                        Map<String, Object> mapTemp;
                        if (UtilValidate.isNotEmpty(accountingQuantityTotal) && UtilValidate.isNotEmpty(facilityId)) {
                            mapTemp = FastMap.newInstance();
                            mapTemp.put("facilityId", facilityId);
                            mapTemp.put("accountingQuantityTotal", accountingQuantityTotal);
                            mapTemp.put("warningQuantity", warningQuantity);
                            mapTemp.put("inventoryItemId", inventoryItemId);
                            mapTemp.put("warningMail", warningMail);
                            productFacilityList.add(mapTemp);
                        }
                    }
                }
                for (Map<String, Object> map : productFacilityList) {

                    String facilityId = (String) map.get("facilityId");//仓库ID
                    String accountingQuantityTotal = (String) map.get("accountingQuantityTotal");//可用数量
                    String warningQuantity = (String) map.get("warningQuantity");//预警数量
                    String warningMail = (String) map.get("warningMail");//预警邮箱
                    // 仓库明细数据的登陆
                    Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap(
                            "productId", productIdCreate,
                            "inventoryItemTypeId", "NON_SERIAL_INV_ITEM",
                            "statusId", "",
                            "ownerPartyId", businessPartyId);
                    serviceContext.put("facilityId", facilityId);
                    serviceContext.put("datetimeReceived", UtilDateTime.nowTimestamp());
                    serviceContext.put("datetimeManufactured", UtilDateTime.nowTimestamp());
                    serviceContext.put("comments", "Created by production run " + facilityId);
                    serviceContext.put("accountingQuantityTotal", new BigDecimal(accountingQuantityTotal));
                    serviceContext.put("warningQuantity", new BigDecimal(warningQuantity));
                    serviceContext.put("warningMail", warningMail);
                    serviceContext.put("unitCost", new BigDecimal("0"));
                    serviceContext.put("currencyUomId", "CNY");
                    serviceContext.put("userLogin", userLogin);
                    serviceContext.put("occupiedQuantityTotal", new BigDecimal("0"));

                    Map<String, Object> resultService = dispatcher.runSync("createInventoryItem", serviceContext);
                    // 仓库和商品的关系表的数据的登陆
                    serviceContext = FastMap.newInstance();
                    serviceContext.put("facilityId", facilityId);
                    serviceContext.put("productId", productIdCreate);
                    serviceContext.put("userLogin", userLogin);
                    resultService = dispatcher.runSync("createProductFacility", serviceContext);
                }

                // 商品特征信息的登陆
                if (UtilValidate.isNotEmpty(productFeatureInfos)) {
                    String[] tproductFeatureInfosArray = productFeatureInfos.split(",");
                    Map<String, Object> mapTemp = FastMap.newInstance();
                    for (String featureInfo : tproductFeatureInfosArray) {
                        String[] attrInfos = featureInfo.split("\\|");
                        //String featureId=attrInfos[0];//商品特征ID
                        for (String attrFeatureId : attrInfos) {
                            if (UtilValidate.isNotEmpty(attrFeatureId)) {
                                mapTemp = FastMap.newInstance();
                                mapTemp.put("featureId", attrFeatureId);
                                productFeatureList.add(mapTemp);
                            }
                        }
                    }
                }

                for (Map<String, Object> map : productFeatureList) {
                    GenericValue productFeature = null;
                    String productFeatureAssocIdCreate = delegator.getNextSeqId("ProductFeatureAssoc");
                    productFeature = delegator.makeValue("ProductFeatureAssoc", UtilMisc.toMap("productFeatureAssocId", productFeatureAssocIdCreate));
                    // 商品特征ID
                    productFeature.set("productFeatureId", (String) map.get("featureId"));
                    // 商品ID
                    productFeature.set("productId", productIdCreate);
                    // 创建表
                    productFeature.create();
                }


                // 商品图片表信息的登陆
//	            String productContentInfos = (String)context.get("productContentInfos"); //商品图片
                // 商品关联表信息的登陆
                // 取得商品关联信息
                if (UtilValidate.isNotEmpty(productAssocInfos)) {
                    String[] tProductAssocInfosInfosArray = productAssocInfos.split(",");
                    for (String productAssocInfo : tProductAssocInfosInfosArray) {
                        String assocProductId = productAssocInfo;//关联商品ID
                        Map<String, Object> mapTemp;
                        if (UtilValidate.isNotEmpty(assocProductId)) {
                            mapTemp = FastMap.newInstance();
                            mapTemp.put("assocProductId", assocProductId);
                            productAssocList.add(mapTemp);
                        }
                    }
                }

                for (Map<String, Object> map : productAssocList) {
                    String assocProductId = null;
                    Map<String, Object> serviceContext = FastMap.newInstance();
                    assocProductId = (String) map.get("assocProductId");

                    serviceContext.put("productId", productIdCreate);
                    serviceContext.put("productIdTo", assocProductId);
                    serviceContext.put("productAssocTypeId", "PRODUCT_CONF");
                    serviceContext.put("fromDate", UtilDateTime.nowTimestamp());
                    serviceContext.put("userLogin", userLogin);

                    result = dispatcher.runSync("createProductAssoc", serviceContext);
                    if (ServiceUtil.isError(result)) {
                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                        return null;
                    }
                }


//	            String productAssocInfos = (String)context.get("productAssocInfos"); //关联商品

            } catch (Exception e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        } else {
            if (UtilValidate.isNotEmpty(productId) && ("update".equals(operateType) || "delete".equals(operateType))) {
                try {

                    GenericValue productUpdate = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    if ("update".equals(operateType)) {
                        if (UtilValidate.isNotEmpty(productUpdate)) {
                            //商品类型
                            if (UtilValidate.isNotEmpty(productTypeId)) {
                                productUpdate.set("productTypeId", productTypeId);
                            }
                            //商品分类ID
                            if (UtilValidate.isNotEmpty(productCategoryId)) {
                                productUpdate.set("primaryProductCategoryId", productCategoryId);
                            }
                            //商品名称
                            if (UtilValidate.isNotEmpty(productName)) {
                                productUpdate.set("productName", productName);
                            }
                            //商品副标题
                            if (UtilValidate.isNotEmpty(productSubheadName)) {
                                productUpdate.set("productSubheadName", productSubheadName);
                                productUpdate.set("internalName", productSubheadName);
                            } else {
                                productUpdate.set("internalName", productName);

                            }
                            //是否上下架
                            if (UtilValidate.isNotEmpty(isOnline)) {
                                productUpdate.set("isOnline", isOnline);
                            }
                            //销售开始时间
                            if (UtilValidate.isNotEmpty(introductionDate)) {
                                productUpdate.set("introductionDate", introductionDate);
                            }
                            //销售结束时间
                            if (UtilValidate.isNotEmpty(salesDiscontinuationDate)) {
                                productUpdate.set("salesDiscontinuationDate", salesDiscontinuationDate);
                            }
                            //商家名称
                            if (UtilValidate.isNotEmpty(businessPartyId)) {
                                productUpdate.set("businessPartyId", businessPartyId);
                            }
                            //商品品牌Id
                            if (UtilValidate.isNotEmpty(brandId)) {
                                productUpdate.set("brandId", brandId);
                            }
                            //体积
                            if (UtilValidate.isNotEmpty(volume)) {
                                productUpdate.set("volume", new BigDecimal(volume));
                            }else{
                                productUpdate.set("volume", null);
                            }
                            //重量
                            if (UtilValidate.isNotEmpty(weight)) {
                                productUpdate.set("weight", new BigDecimal(weight));
                            }else{
                                productUpdate.set("weight", null);
                            }
                            //是否使用特征
                            if (UtilValidate.isNotEmpty(isUsedFeature)) {
                                productUpdate.set("isUsedFeature", isUsedFeature);
                            }
                            //SEO关键字
                            if (UtilValidate.isNotEmpty(seoKeyword)) {
                                productUpdate.set("seoKeyword", seoKeyword);
                            }
                            //PC端详情
                            if (UtilValidate.isNotEmpty(pcDetails)) {
                                productUpdate.set("pcDetails", pcDetails);
                            }
                            //移动端详情
                            if (UtilValidate.isNotEmpty(mobileDetails)) {
                                productUpdate.set("mobileDetails", mobileDetails);
                            }else{
                                productUpdate.set("mobileDetails", null);
                            }


                            // 商品特征名称
                            if (UtilValidate.isNotEmpty(featureProductName)) {
                                productUpdate.set("featureProductName", featureProductName);
                            }

                            // 商品特征ID
                            if (UtilValidate.isNotEmpty(featureProductId)) {
                                productUpdate.set("featureProductId", featureProductId);
                            }

                            // 推荐到首页
                            if (UtilValidate.isNotEmpty(isRecommendHomePage)) {
                                productUpdate.set("isRecommendHomePage", isRecommendHomePage);
                            }
                            // 支持服务
                            if (UtilValidate.isNotEmpty(isSupportService)) {
                                productUpdate.set("isSupportService", isSupportService);
                            }
                            // 支持服务类型
                            //            if(UtilValidate.isNotEmpty(supportServiceType)){
                            //                productUpdate.set("supportServiceType", supportServiceType);
                            //            }
                            // 积分抵扣
                            if (UtilValidate.isNotEmpty(integralDeductionType)) {
                                productUpdate.set("integralDeductionType", integralDeductionType);
                            }
                            // 积分抵扣上限
                            if (UtilValidate.isNotEmpty(integralDeductionUpper)) {
                                productUpdate.set("integralDeductionUpper", new BigDecimal(integralDeductionUpper));
                            }
                            // 每人限购数量
                            if (UtilValidate.isNotEmpty(purchaseLimitationQuantity)) {
                                productUpdate.set("purchaseLimitationQuantity", new BigDecimal(purchaseLimitationQuantity));
                            }
                            // 列表展示
                            if (UtilValidate.isNotEmpty(isListShow)) {
                                productUpdate.set("isListShow", isListShow);
                            }
                            // 代金券面额
                            if (UtilValidate.isNotEmpty(voucherAmount)) {
                                productUpdate.set("voucherAmount", new BigDecimal(voucherAmount));
                            }
                            // 使用限制
                            if (UtilValidate.isNotEmpty(useLimit)) {
                                productUpdate.set("useLimit", useLimit);
                            }
                            // 使用开始时间
                            if (UtilValidate.isNotEmpty(useStartTime)) {
                                productUpdate.set("useStartTime", useStartTime);
                            }
                            // 使用结束时间
                            if (UtilValidate.isNotEmpty(useEndTime)) {
                                productUpdate.set("useEndTime", useEndTime);
                            }
                            // 是否保税商品
                            if (UtilValidate.isNotEmpty(isBondedGoods)) {
                                productUpdate.set("isBondedGoods", isBondedGoods);
                            }
                            // 是否自营
                            if (UtilValidate.isNotEmpty(isInner)) {
                                productUpdate.set("isInner", isInner);
                            }

                            // 平台分类
                            if (UtilValidate.isNotEmpty(platformClassId)) {
                                productUpdate.set("platformClassId", platformClassId);
                            }
                            // 供应商
                            if(UtilValidate.isNotEmpty(providerId)){
                                productUpdate.set("providerId",providerId);
                            }
                            //是否是sku
                            if (UtilValidate.isNotEmpty(isSku)) {
                                productUpdate.set("isVirtual", "Y");
                                productUpdate.set("isVariant", "N");

                            } else {
                                if (UtilValidate.isNotEmpty(mainProductId)) {
                                    productUpdate.set("isVirtual", "N");
                                    productUpdate.set("isVariant", "Y");
                                } else {
                                    productUpdate.set("isVirtual", "N");
                                    productUpdate.set("isVariant", "N");
                                }
                            }

                            productUpdate.store();
                            result.put("productId", productId);

                            // 商品标签关系表数据的更新
                            if (UtilValidate.isNotEmpty(tags)) {
                                List<GenericValue> productTagAssocList = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", productId));
                                if (UtilValidate.isNotEmpty(productTagAssocList)) {
                                    delegator.removeAll(productTagAssocList);
                                }
                                for (int i = 0; i < tags.length; i++) {
                                    GenericValue productTagAssoc = null;
                                    String productTagAssocIdCreate = delegator.getNextSeqId("ProductTagAssoc");
                                    productTagAssoc = delegator.makeValue("ProductTagAssoc", UtilMisc.toMap("productTagAssocId", productTagAssocIdCreate));
                                    // 商品ID
                                    productTagAssoc.set("productId", productId);
                                    // 标签ID
                                    productTagAssoc.set("tagId", tags[i]);
                                    // 创建表
                                    productTagAssoc.create();
                                }
                            }


                            // 商品和支持服务关系表数据的更新
                            if (UtilValidate.isNotEmpty(supportServiceTypeInfos)) {
                                List<GenericValue> productSupportServiceAssocList = delegator.findByAnd("ProductSupportServiceAssoc", UtilMisc.toMap("productId", productId));
                                if (UtilValidate.isNotEmpty(productSupportServiceAssocList)) {
                                    delegator.removeAll(productSupportServiceAssocList);
                                }
                                for (int i = 0; i < supportServiceTypeInfos.length; i++) {
                                    GenericValue productSupportServiceAssoc = null;
                                    String productSupportServiceAssocIdCreate = delegator.getNextSeqId("ProductSupportServiceAssoc");
                                    productSupportServiceAssoc = delegator.makeValue("ProductSupportServiceAssoc", UtilMisc.toMap("productSupportServiceAssocId", productSupportServiceAssocIdCreate));
                                    // 商品ID
                                    productSupportServiceAssoc.set("productId", productId);
                                    // 服务支持ID
                                    productSupportServiceAssoc.set("enumId", supportServiceTypeInfos[i]);
                                    // 创建表
                                    productSupportServiceAssoc.create();
                                }
                            }

                            // 商品价格表信息表的更新
                            List<GenericValue> productPriceSaleList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId));
                            if (productPriceSaleList.size() > 0) {
                                delegator.removeAll(productPriceSaleList);
                            }

                            //销售价格
                            if (UtilValidate.isNotEmpty(salePrice)) {

                                Map<String, Object> passedParams = FastMap.newInstance();
                                passedParams.put("productId", productId);
                                passedParams.put("productPriceTypeId", "DEFAULT_PRICE");
                                passedParams.put("productPricePurposeId", "PURCHASE");
                                passedParams.put("currencyUomId", "CNY");
                                passedParams.put("productStoreGroupId", "_NA_");
                                passedParams.put("fromDate", UtilDateTime.nowTimestamp());
                                passedParams.put("price", new BigDecimal(salePrice));
                                passedParams.put("userLogin", userLogin);
//            		            passedParams.put("oldPrice",new BigDecimal("0"));


                                Map<String, Object> resultMap = FastMap.newInstance();
                                try {
                                    resultMap = dispatcher.runSync("createProductPrice", passedParams);
                                } catch (GenericServiceException e) {
                                    return ServiceUtil.returnError(e.getMessage());
                                }
                            }
                            //市场价格
                            if (UtilValidate.isNotEmpty(marketPrice)) {
                                Map<String, Object> passedParams = FastMap.newInstance();
                                passedParams.put("productId", productId);
                                passedParams.put("productPriceTypeId", "MARKET_PRICE");
                                passedParams.put("productPricePurposeId", "PURCHASE");
                                passedParams.put("currencyUomId", "CNY");
                                passedParams.put("productStoreGroupId", "_NA_");
                                passedParams.put("fromDate", UtilDateTime.nowTimestamp());
                                passedParams.put("price", new BigDecimal(marketPrice));
                                passedParams.put("userLogin", userLogin);
//            		            passedParams.put("oldPrice",new BigDecimal("0"));

                                Map<String, Object> resultMap = FastMap.newInstance();
                                try {
                                    resultMap = dispatcher.runSync("createProductPrice", passedParams);
                                } catch (GenericServiceException e) {
                                    return ServiceUtil.returnError(e.getMessage());
                                }
                            }

                            //成本价格
                            if (UtilValidate.isNotEmpty(costPrice)) {
                                Map<String, Object> passedParams = FastMap.newInstance();
                                passedParams.put("productId", productId);
                                passedParams.put("productPriceTypeId", "COST_PRICE");
                                passedParams.put("productPricePurposeId", "PURCHASE");
                                passedParams.put("currencyUomId", "CNY");
                                passedParams.put("productStoreGroupId", "_NA_");
                                passedParams.put("fromDate", UtilDateTime.nowTimestamp());
                                passedParams.put("price", new BigDecimal(costPrice));
                                passedParams.put("userLogin", userLogin);
//            		            passedParams.put("oldPrice",new BigDecimal("0"));

                                Map<String, Object> resultMap = FastMap.newInstance();
                                try {
                                    resultMap = dispatcher.runSync("createProductPrice", passedParams);
                                } catch (GenericServiceException e) {
                                    return ServiceUtil.returnError(e.getMessage());
                                }
                            }


                            // 商品属性表信息的更新
                            // 取得商品属性信息
                            if (UtilValidate.isNotEmpty(productAttrInfos)) {
                                String[] tProductAttrInfosArray = productAttrInfos.split(",");
                                for (String attrInfo : tProductAttrInfosArray) {
                                    String[] attrInfos = attrInfo.split("\\|");
                                    String attrName = attrInfos[0];//分类属性名
                                    String attrCategoryId = attrInfos[1];//分类ID
                                    String attrOption = "";//选项Id
                                    if (attrInfos.length == 3) {
                                        attrOption = attrInfos[2];//选项Id
                                    }
                                    if (UtilValidate.isNotEmpty(attrOption)) {

                                        Map<String, Object> mapTemp;
                                        if (UtilValidate.isNotEmpty(attrName) && UtilValidate.isNotEmpty(attrOption)) {
                                            mapTemp = FastMap.newInstance();
                                            mapTemp.put("attrName", attrName);
                                            mapTemp.put("attrCategoryId", attrCategoryId);
                                            mapTemp.put("attrOption", attrOption);
                                            productAttrList.add(mapTemp);
                                        }
                                    }
                                }
                            }
                            if (productAttrList.size() > 0) {
                                List<GenericValue> productCategoryattributeAssocList = delegator.findByAnd("ProductCategoryattributeAssoc", UtilMisc.toMap("productId", productId));
                                if (productCategoryattributeAssocList.size() > 0) {
                                    delegator.removeAll(productCategoryattributeAssocList);
                                }
                                for (Map<String, Object> map : productAttrList) {
                                    GenericValue productCategoryattributeAssoc = null;
                                    String productCategoryattributeAssocIdCreate = delegator.getNextSeqId("ProductCategoryattributeAssoc");
                                    productCategoryattributeAssoc = delegator.makeValue("ProductCategoryattributeAssoc", UtilMisc.toMap("productCategoryattributeAssocId", productCategoryattributeAssocIdCreate));
                                    // 属性名称
                                    productCategoryattributeAssoc.set("attrName", (String) map.get("attrName"));
                                    // 选项ID
                                    productCategoryattributeAssoc.set("productOptionId", (String) map.get("attrOption"));
                                    // 属性分类ID
                                    productCategoryattributeAssoc.set("productCategoryId", (String) map.get("attrCategoryId"));
                                    // 商品ID
                                    productCategoryattributeAssoc.set("productId", productId);
                                    // 创建表
                                    productCategoryattributeAssoc.create();
                                }
                            } else {
                                List<GenericValue> productCategoryattributeAssocList = delegator.findByAnd("ProductCategoryattributeAssoc", UtilMisc.toMap("productId", productId));
                                if (productCategoryattributeAssocList.size() > 0) {
                                    delegator.removeAll(productCategoryattributeAssocList);
                                }
                            }

                            // 取得商品参数的值
                            if (UtilValidate.isNotEmpty(productParameterInfos)) {

                                String[] tProductParameterInfosArray = productParameterInfos.split(",");
                                for (String parameterInfo : tProductParameterInfosArray) {
                                    String[] attrInfos = parameterInfo.split("\\|");
                                    String parameterName = attrInfos[0];//参数名
                                    String parameterDetails = attrInfos[1];//参数详情
                                    Map<String, Object> mapTemp;
                                    if (UtilValidate.isNotEmpty(parameterName) && UtilValidate.isNotEmpty(parameterDetails)) {
                                        mapTemp = FastMap.newInstance();
                                        mapTemp.put("parameterName", parameterName);
                                        mapTemp.put("parameterDetails", parameterDetails);
                                        productParameterList.add(mapTemp);
                                    }
                                }
                            }
                            if (productParameterList.size() > 0) {
                                List<GenericValue> productParameterDelList = delegator.findByAnd("ProductParameter", UtilMisc.toMap("productId", productId));
                                delegator.removeAll(productParameterDelList);
                                for (Map<String, Object> map : productParameterList) {
                                    GenericValue productParameter = null;
                                    String productParameterIdCreate = delegator.getNextSeqId("ProductParameter");
                                    productParameter = delegator.makeValue("ProductParameter", UtilMisc.toMap("productParameterId", productParameterIdCreate));
                                    // 参数名称
                                    productParameter.set("parameterName", (String) map.get("parameterName"));
                                    // 参数详情
                                    productParameter.set("parameterDescription", (String) map.get("parameterDetails"));
                                    // 商品ID
                                    productParameter.set("productId", productId);
                                    // 创建表
                                    productParameter.create();
                                }
                            } else {
                                List<GenericValue> productParameterDelList = delegator.findByAnd("ProductParameter", UtilMisc.toMap("productId", productId));
                                delegator.removeAll(productParameterDelList);
                            }

                            // 商品库存表信息的更新
                            // 取得商品库存信息
                            if (UtilValidate.isNotEmpty(productFacilityInfos)) {
                                String[] tProductFacilityInfosArray = productFacilityInfos.split(",");
                                for (String facilityInfo : tProductFacilityInfosArray) {
                                    String[] attrInfos = facilityInfo.split("\\|");

                                    String facilityId = attrInfos[0];//仓库ID
                                    String inventoryItemId = attrInfos[1];//库存明细ID
                                    String accountingQuantityTotal = attrInfos[2];//可用数量
                                    String warningQuantity = attrInfos[3];//预警数量
                                    //                    String warningMail=attrInfos[4];//预警邮箱
                                    String warningMail = "";//预警邮箱
                                    if (attrInfos.length == 5) {
                                        warningMail = attrInfos[4];
                                    }
                                    Map<String, Object> mapTemp;
                                    if (UtilValidate.isNotEmpty(accountingQuantityTotal) && UtilValidate.isNotEmpty(facilityId)) {
                                        mapTemp = FastMap.newInstance();
                                        mapTemp.put("facilityId", facilityId);
                                        mapTemp.put("accountingQuantityTotal", accountingQuantityTotal);
                                        mapTemp.put("warningQuantity", warningQuantity);
                                        mapTemp.put("warningMail", warningMail);
                                        mapTemp.put("inventoryItemId", inventoryItemId);
                                        productFacilityList.add(mapTemp);
                                    }
                                }
                            }
                            if (productFacilityList.size() > 0) {

                                List<GenericValue> inventoryItemList = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId));

                                for (Map<String, Object> map : productFacilityList) {
                                    String facilityId = (String) map.get("facilityId");//仓库ID
                                    String accountingQuantityTotal = (String) map.get("accountingQuantityTotal");//可用数量
                                    String warningQuantity = (String) map.get("warningQuantity");//预警数量
                                    String warningMail=(String)map.get("warningMail");//预警邮箱
                                    String inventoryItemId = (String) map.get("inventoryItemId");//库存明细ID
                                    // 仓库明细数据的更新
                                    GenericValue curInventoryItemInfo = null;
                                    String curInventoryItemId = "";


                                    if (UtilValidate.isNotEmpty(inventoryItemId)) {

                                        for (GenericValue curInventoryItem : inventoryItemList) {
                                            if (inventoryItemId.equals(curInventoryItem.getString("inventoryItemId"))) {
                                                curInventoryItemId = curInventoryItem.getString("inventoryItemId");
                                                curInventoryItemInfo = curInventoryItem;
                                            }
                                        }

                                        if (UtilValidate.isNotEmpty(curInventoryItemId)) {
                                            // set the fields on the item
                                            Map serviceContext = UtilMisc.toMap("inventoryItemId", curInventoryItemId);
                                            serviceContext.put("facilityId", facilityId);
                                            serviceContext.put("datetimeReceived", UtilDateTime.nowTimestamp());
                                            serviceContext.put("datetimeManufactured", UtilDateTime.nowTimestamp());
                                            serviceContext.put("comments", "Created by production run " + facilityId);
                                            serviceContext.put("accountingQuantityTotal", new BigDecimal(accountingQuantityTotal));
                                            serviceContext.put("warningQuantity", new BigDecimal(warningQuantity));
                                            serviceContext.put("warningMail",warningMail);
                                            serviceContext.put("unitCost", new BigDecimal("0"));
                                            serviceContext.put("currencyUomId", "CNY");
                                            serviceContext.put("userLogin", userLogin);

                                            try {
                                                Map<String, Object> resultUpdate = dispatcher.runSync("updateInventoryItem", serviceContext);
                                                if (ServiceUtil.isError(resultUpdate)) {
                                                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                                            "ProductInventoryItemStoreProblem",
                                                            UtilMisc.toMap("errorString", ""), locale), null, null, result);
                                                }
                                            } catch (GenericServiceException exc) {
                                                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                                        "ProductInventoryItemStoreProblem",
                                                        UtilMisc.toMap("errorString", exc.getMessage()), locale));
                                            }


                                            // 仓库和商品的关系表的数据的登陆
                                            serviceContext = FastMap.newInstance();
                                            serviceContext.put("facilityId", facilityId);
                                            serviceContext.put("productId", productId);
                                            serviceContext.put("userLogin", userLogin);
                                            try {
                                                Map<String, Object> resultService = dispatcher.runSync("updateProductFacility", serviceContext);
                                                if (ServiceUtil.isError(resultService)) {
                                                    return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                                            "ProductFacilityStoreProblem",
                                                            UtilMisc.toMap("errorString", ""), locale), null, null, result);
                                                }
                                            } catch (GenericServiceException exc) {
                                                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                                                        "ProductFacilityStoreProblem",
                                                        UtilMisc.toMap("errorString", exc.getMessage()), locale));
                                            }

                                        } else {
                                            // 仓库和商品的关系表的数据的删除
                                            Map<String, Object> serviceContext = FastMap.newInstance();
                                            serviceContext = FastMap.newInstance();
                                            serviceContext.put("facilityId", facilityId);
                                            serviceContext.put("productId", productId);
                                            serviceContext.put("userLogin", userLogin);
                                            Map<String, Object> resultService = dispatcher.runSync("deleteProductFacility", serviceContext);
                                            curInventoryItemInfo.remove();
                                        }
                                    } else {
                                        // 删除商品和库存明细的关系
                                        for (GenericValue genericValue : inventoryItemList) {
                                            Map<String, Object> serviceContext = FastMap.newInstance();
                                            serviceContext = FastMap.newInstance();
                                            serviceContext.put("facilityId", genericValue.getString("facilityId"));
                                            serviceContext.put("productId", productId);
                                            serviceContext.put("userLogin", userLogin);
                                            Map<String, Object> resultService = dispatcher.runSync("deleteProductFacility", serviceContext);
                                        }
                                        // 删除商品库存明细
                                        delegator.removeAll(inventoryItemList);

                                        // 更新的场合仓库明细数据的登陆
                                        Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap(
                                                "productId", productId,
                                                "inventoryItemTypeId", "NON_SERIAL_INV_ITEM",
                                                "statusId", "",
                                                "ownerPartyId", businessPartyId);
                                        serviceContext.put("facilityId", facilityId);
                                        serviceContext.put("datetimeReceived", UtilDateTime.nowTimestamp());
                                        serviceContext.put("datetimeManufactured", UtilDateTime.nowTimestamp());
                                        serviceContext.put("comments", "Created by production run " + facilityId);
                                        serviceContext.put("accountingQuantityTotal", new BigDecimal(accountingQuantityTotal));
                                        serviceContext.put("warningQuantity", new BigDecimal(warningQuantity));
                                        serviceContext.put("warningMail",warningMail);
                                        serviceContext.put("unitCost", new BigDecimal("0"));
                                        serviceContext.put("currencyUomId", "CNY");
                                        serviceContext.put("userLogin", userLogin);
                                        serviceContext.put("occupiedQuantityTotal", new BigDecimal("0"));

                                        Map<String, Object> resultService = dispatcher.runSync("createInventoryItem", serviceContext);
                                        // 仓库和商品的关系表的数据的登陆
                                        serviceContext = FastMap.newInstance();
                                        serviceContext.put("facilityId", facilityId);
                                        serviceContext.put("productId", productId);
                                        serviceContext.put("userLogin", userLogin);
                                        resultService = dispatcher.runSync("createProductFacility", serviceContext);

                                    }
                                }
                            } else {
                                List<GenericValue> inventoryItemList = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId));
                                for (GenericValue genericValue : inventoryItemList) {
                                    // 仓库和商品的关系表的数据的删除
                                    Map<String, Object> serviceContext = FastMap.newInstance();
                                    serviceContext = FastMap.newInstance();
                                    serviceContext.put("facilityId", genericValue.getString("facilityId"));
                                    serviceContext.put("productId", productId);
                                    serviceContext.put("userLogin", userLogin);
                                    Map<String, Object> resultService = dispatcher.runSync("deleteProductFacility", serviceContext);
                                }

                                // 删除库存明细表
                                delegator.removeAll(inventoryItemList);
                            }

                            // 商品图片表信息的更新
//            	           String productContentInfos = (String)context.get("productContentInfos"); //商品图片
                            // 商品关联表信息的更新
//            	           String productAssocInfos = (String)context.get("productAssocInfos"); //关联商品

                            // 商品关联表信息的登陆
                            // 取得商品关联信息
                            if (UtilValidate.isNotEmpty(productAssocInfos)) {
                                String[] tProductAssocInfosInfosArray = productAssocInfos.split(",");
                                for (String productAssocInfo : tProductAssocInfosInfosArray) {
                                    String assocProductId = productAssocInfo;//关联商品ID
                                    Map<String, Object> mapTemp;
                                    if (UtilValidate.isNotEmpty(assocProductId)) {
                                        mapTemp = FastMap.newInstance();
                                        mapTemp.put("assocProductId", assocProductId);
                                        productAssocList.add(mapTemp);
                                    }
                                }
                            }
                            List<GenericValue> productAssocItemList = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId));
                            if (productAssocList.size() > 0) {
                                if (UtilValidate.isNotEmpty(productAssocItemList)) {
                                    delegator.removeAll(productAssocItemList);
                                }
                                for (Map<String, Object> map : productAssocList) {
                                    String assocProductId = null;
                                    Map<String, Object> serviceContext = FastMap.newInstance();
                                    assocProductId = (String) map.get("assocProductId");
                                    serviceContext.put("productId", productId);
                                    serviceContext.put("productIdTo", assocProductId);
                                    serviceContext.put("productAssocTypeId", "PRODUCT_CONF");
                                    serviceContext.put("fromDate", UtilDateTime.nowTimestamp());
                                    serviceContext.put("userLogin", userLogin);
                                    result = dispatcher.runSync("createProductAssoc", serviceContext);
                                    if (ServiceUtil.isError(result)) {
                                        Debug.logError(ServiceUtil.getErrorMessage(result), module);
                                        return null;
                                    }
                                }
                            } else {

                                if (UtilValidate.isNotEmpty(productAssocItemList)) {
                                    delegator.removeAll(productAssocItemList);
                                }

                            }

                            // 商品特征信息的登陆
                            if (UtilValidate.isNotEmpty(productFeatureInfos)) {
                                String[] tproductFeatureInfosArray = productFeatureInfos.split(",");
                                Map<String, Object> mapTemp = FastMap.newInstance();
                                for (String featureInfo : tproductFeatureInfosArray) {
                                    String[] attrInfos = featureInfo.split("\\|");

                                    //String featureId=attrInfos[0];//商品特征ID
                                    for (String attrFeatureId : attrInfos) {
                                        if (UtilValidate.isNotEmpty(attrFeatureId)) {
                                            mapTemp = FastMap.newInstance();
                                            mapTemp.put("featureId", attrFeatureId);
                                            productFeatureList.add(mapTemp);
                                        }
                                    }
                                }
                            }
                            if (productFeatureList.size() > 0) {
                                List<GenericValue> productFeatureDelList = delegator.findByAnd("ProductFeatureAssoc", UtilMisc.toMap("productId", productId));
                                if (productFeatureDelList.size() > 0) {
                                    delegator.removeAll(productFeatureDelList);
                                }
                                for (Map<String, Object> map : productFeatureList) {
                                    GenericValue productFeature = null;
                                    String productFeatureAssocIdCreate = delegator.getNextSeqId("ProductFeatureAssoc");
                                    productFeature = delegator.makeValue("ProductFeatureAssoc", UtilMisc.toMap("productFeatureAssocId", productFeatureAssocIdCreate));
                                    // 商品特征ID
                                    productFeature.set("productFeatureId", (String) map.get("featureId"));
                                    // 商品ID
                                    productFeature.set("productId", productId);
                                    // 创建表
                                    productFeature.create();
                                }
                            } else {
                                List<GenericValue> productFeatureDelList = delegator.findByAnd("ProductFeatureAssoc", UtilMisc.toMap("productId", productId));
                                if (productFeatureDelList.size() > 0) {
                                    delegator.removeAll(productFeatureDelList);
                                }
                            }

                        }
                    } else if ("delete".equals(operateType)) {
                        productUpdate.set("isDel", "Y");
                        productUpdate.store();
                        // 删除商品标签关系表
                        List<GenericValue> productTagDelList = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", productId));
                        delegator.removeAll(productTagDelList);


                        //    	 entityConditionList.add(EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, productUpdate.getString("mainProductId")));// 所有sku商品
                        //         List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                        //                 EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
                        //         entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
//
                        //         List<String> delProductIdsForFeature=FastList.newInstance();
                        //         String[] delIds=productFeatureGoodsDelIds.split(",");
                        //         for (String delId : delIds) {
                        //        	 delProductIdsForFeature.add(delId);
//						 }
                        //         if(delProductIdsForFeature.size()>0){
                        //        	 entityConditionList.add(EntityCondition.makeCondition("productId", EntityOperator.NOT_IN, delProductIdsForFeature));
                        //         }
                        //         List<GenericValue> productList = delegator.findList("Product", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
//
                        //         String featureIds=productUpdate.getString("featureProductId");
                        //		 String[] delFeatureIds=featureIds.split("\\|");
                        //		 Boolean isUsedFeatureId=false;
                        //		 for (int i = 0; i < delFeatureIds.length; i++) {
                        //			 String delFeatureId = delFeatureIds[i];
//	                		 for (GenericValue genericValue : productList) {
//								if(!(genericValue.getString("productId").equals(productId))){
//									String curFeatureIds=genericValue.getString("featureProductId");
//									if(curFeatureIds.contains(delFeatureId)){
//										isUsedFeatureId=true;
//									}else{
//										continue;
//									}
//								}
//							 }
//	                		 if(!isUsedFeatureId){
//	                			 //删除该商品的特性关联关系
//	                        	 List<GenericValue> productFeatureDelList=delegator.findByAnd("ProductFeatureAssoc",UtilMisc.toMap("productId", productUpdate.getString("mainProductId"),"productFeatureId",delFeatureId));
//	                    		 if(productFeatureDelList.size()>0){
//	                           		delegator.removeAll(productFeatureDelList);
//	                           	 }
//	                		 }
//						  }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }

        return result;
    }
    ////////////////////////////////////////////////

    /**
     * 商品的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> updateProductFeatureGoodsIcoPro(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String operateType = (String) context.get("operateType");//操作类型
        String productId = (String) context.get("productIdMain");//商品编号ID
        String productTypeId = (String) context.get("productTypeId");//商品类型
        String productCategoryId = (String) context.get("productCategoryId");//商品分类ID
        String productName = (String) context.get("productNameMain");//商品名称
        String productSubheadName = (String) context.get("productSubheadName"); //商品副标题
        String isOnline = (String) context.get("isOnline"); //是否上下架


        Timestamp startTime = (Timestamp) context.get("startTime");//销售开始时间
        Timestamp endTime = (Timestamp) context.get("endTime");//销售结束时间

        String businessPartyId = (String) context.get("businessPartyId");//商家名称
        String brandId = (String) context.get("brandId"); //商品品牌Id

        String volume = (String) context.get("volumeMain"); //体积
        String weight = (String) context.get("weightMain"); //重量
        String isUsedFeature = (String) context.get("isUsedFeature"); //是否使用特征
        String seoKeyword = (String) context.get("seoKeyword"); //SEO关键字
        String salePrice = (String) context.get("salePriceMain"); //销售价格
        String marketPrice = (String) context.get("marketPriceMain"); //市场价格
        String costPrice = (String) context.get("costPriceMain"); //成本价格
        String productTags = (String) context.get("productTags"); //商品标签
        String productParameterInfos = (String) context.get("productParameterInfos"); //商品参数
        String productAttrInfos = (String) context.get("productAttrInfos"); //商品属性
        String productFacilityInfos = (String) context.get("productFacilityInfos"); //商品库存
        String pcDetails = (String) context.get("pcDetails"); //PC端详情
        String mobileDetails = (String) context.get("mobileDetails"); //移动端详情
        String productFeatureInfos = (String) context.get("productFeatureInfos");//商品特征
        String productContentInfos = (String) context.get("productContentInfos"); //商品图片
        String productAssocInfos = (String) context.get("productAssocInfos"); //关联商品

        String productFeatureGoodsDelIds = (String) context.get("productFeatureGoodsDelIds"); //特征商品删除信息

        String productFeatureGoodsInfos = (String) context.get("productFeatureGoodsInfos");//特征商品信息


        // Add by zhajh at 20180306 yabiz相关内容 Begin
        String isRecommendHomePage = (String) context.get("isRecommendHomePage"); //推荐到首页
        String isSupportService = (String) context.get("isSupportService"); //支持服务
        String supportServiceType = (String) context.get("supportServiceType"); //支持服务类型
        String integralDeductionType = (String) context.get("integralDeductionType"); //积分抵扣
        String integralDeductionUpper = (String) context.get("integralDeductionUpper"); //积分抵扣上限
        String purchaseLimitationQuantity = (String) context.get("purchaseLimitationQuantity"); //每人限购数量
        String isListShow = (String) context.get("isListShow"); //列表展示
        String voucherAmount = (String) context.get("voucherAmount"); //代金券面额
        String useLimit = (String) context.get("useLimit"); //使用限制
        Timestamp useStartTime = (Timestamp) context.get("useStartTime"); //使用开始时间
        Timestamp useEndTime = (Timestamp) context.get("useEndTime"); //使用结束时间
        String isBondedGoods = (String) context.get("isBondedGoods"); //是否保税商品
        String productStoreId = (String) context.get("productStoreId"); //商铺信息
        String isInner = (String) context.get("isInner"); //是否自营
        String platformClassId = (String) context.get("platformClassId"); //平台分类
        String providerId =(String) context.get("providerId");// 供应商编码
        // Add by zhajh at 20180306 yabiz相关内容 End

        try {
            Map<String, Object> productParams = FastMap.newInstance();
            // 操作类型
            if (UtilValidate.isNotEmpty(operateType)) {
                productParams.put("operateType", operateType);
            }
            // 商品编号
            if (UtilValidate.isNotEmpty(productId)) {
                productParams.put("productId", productId);
            }
            //商品类型
            if (UtilValidate.isNotEmpty(productTypeId)) {
                productParams.put("productTypeId", productTypeId);
            }
            //商品分类ID
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                productParams.put("productCategoryId", productCategoryId);
            }
            //商品名称
            if (UtilValidate.isNotEmpty(productName)) {
                productParams.put("productName", productName);
            }
            //商品副标题
            if (UtilValidate.isNotEmpty(productSubheadName)) {
                productParams.put("productSubheadName", productSubheadName);
            }

            //是否上下架
            if (UtilValidate.isNotEmpty(isOnline)) {
                productParams.put("isOnline", isOnline);
            }

            //销售开始时间
            if (UtilValidate.isNotEmpty(startTime)) {
                productParams.put("startTime", startTime);
            }
            //销售结束时间
            if (UtilValidate.isNotEmpty(endTime)) {
                productParams.put("endTime", endTime);
            }
            //商家名称
            if (UtilValidate.isNotEmpty(businessPartyId)) {
                productParams.put("businessPartyId", businessPartyId);
            }
            //商品品牌Id
            if (UtilValidate.isNotEmpty(brandId)) {
                productParams.put("brandId", brandId);
            }
            //体积
            if (UtilValidate.isNotEmpty(volume)) {
                productParams.put("volume", volume);
            }
            //重量
            if (UtilValidate.isNotEmpty(weight)) {
                productParams.put("weight", weight);
            }
            //是否使用特征
            if (UtilValidate.isNotEmpty(isUsedFeature)) {
                productParams.put("isUsedFeature", isUsedFeature);
            }
            //SEO关键字
            if (UtilValidate.isNotEmpty(seoKeyword)) {
                productParams.put("seoKeyword", seoKeyword);
            }
            // 销售价格
            if (UtilValidate.isNotEmpty(salePrice)) {
                productParams.put("salePrice", salePrice);
            }
            // 市场价格
            if (UtilValidate.isNotEmpty(marketPrice)) {
                productParams.put("marketPrice", marketPrice);
            }
            // 成本价格
            if (UtilValidate.isNotEmpty(costPrice)) {
                productParams.put("costPrice", costPrice);
            }
            // 商品标签
            if (UtilValidate.isNotEmpty(productTypeId)) {
                productParams.put("productTags", productTags);
            }

            // 参数信息
            if (UtilValidate.isNotEmpty(productParameterInfos)) {
                productParams.put("productParameterInfos", productParameterInfos);
            }
            // 商品分类属性信息
            if (UtilValidate.isNotEmpty(productAttrInfos)) {
                productParams.put("productAttrInfos", productAttrInfos);
            }
            // 商品库存信息
            if (UtilValidate.isNotEmpty(productFacilityInfos)) {
                productParams.put("productFacilityInfos", productFacilityInfos);
            }
            // PC端详情
            if (UtilValidate.isNotEmpty(pcDetails)) {
                productParams.put("pcDetails", pcDetails);
            }
            // 移动端详情
            if (UtilValidate.isNotEmpty(mobileDetails)) {
                productParams.put("mobileDetails", mobileDetails);
            }

            // 商品特征信息
            if (UtilValidate.isNotEmpty(productFeatureInfos)) {
                productParams.put("productFeatureInfos", productFeatureInfos);
            }

            // 图片信息
            if (UtilValidate.isNotEmpty(productContentInfos)) {
                productParams.put("productContentInfos", productContentInfos);
            }
            // 商品关系信息
            if (UtilValidate.isNotEmpty(productAssocInfos)) {
                productParams.put("productAssocInfos", productAssocInfos);
            }

            // Add by zhajh at 20180312 yabiz相关内容 Begin
            // 推荐到首页
            if (UtilValidate.isNotEmpty(isRecommendHomePage)) {
                productParams.put("isRecommendHomePage", isRecommendHomePage);
            }
            // 支持服务
            if (UtilValidate.isNotEmpty(isSupportService)) {
                productParams.put("isSupportService", isSupportService);
            }
            // 支持服务类型
            if(UtilValidate.isNotEmpty(supportServiceType)){
                productParams.put("supportServiceType", supportServiceType);
            }
            // 积分抵扣
            if (UtilValidate.isNotEmpty(integralDeductionType)) {
                productParams.put("integralDeductionType", integralDeductionType);
            }
            // 积分抵扣上限
            if (UtilValidate.isNotEmpty(integralDeductionUpper)) {
                productParams.put("integralDeductionUpper", integralDeductionUpper);
            }
            // 每人限购数量
            if (UtilValidate.isNotEmpty(purchaseLimitationQuantity)) {
                productParams.put("purchaseLimitationQuantity", purchaseLimitationQuantity);
            }
            // 列表展示
            if (UtilValidate.isNotEmpty(isListShow)) {
                productParams.put("isListShow", isListShow);
            }
            // 代金券面额
            if (UtilValidate.isNotEmpty(voucherAmount)) {
                productParams.put("voucherAmount", voucherAmount);
            }
            // 使用限制
            if (UtilValidate.isNotEmpty(useLimit)) {
                productParams.put("useLimit", useLimit);
            }
            // 使用开始时间
            if (UtilValidate.isNotEmpty(useStartTime)) {
                productParams.put("useStartTime", useStartTime);
            }
            // 使用结束时间
            if (UtilValidate.isNotEmpty(useEndTime)) {
                productParams.put("useEndTime", useEndTime);
            }
            // 是否保税商品
            if (UtilValidate.isNotEmpty(isBondedGoods)) {
                productParams.put("isBondedGoods", isBondedGoods);
            }

            // 商铺编码
            if (UtilValidate.isNotEmpty(productStoreId)) {
                productParams.put("productStoreId", productStoreId);
            }
            // 是否自营
            if (UtilValidate.isNotEmpty(isInner)) {
                productParams.put("isInner", isInner);
            }
            // 是否自营
            if (UtilValidate.isNotEmpty(platformClassId)) {
                productParams.put("platformClassId", platformClassId);
            }

            // 供应商编码
            if(UtilValidate.isNotEmpty(providerId)){
                productParams.put("providerId", providerId);

            }
            // Add by zhajh at 20180312 yabiz相关内容 End

            // 是否是Sku信息
            

            productParams.put("userLogin", userLogin);
            Map<String, Object> resultMap = FastMap.newInstance();
            String productIdMain = "";
            try {
                resultMap = dispatcher.runSync("updateProductIcoPro", productParams);
                productIdMain = (String) resultMap.get("productId");
                List<Map<String, Object>> productFeatureGoodsList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(productFeatureGoodsInfos)) {
                    // 取得商品库存信息
                    if (UtilValidate.isNotEmpty(productFeatureGoodsInfos)) {
                        String[] tProductFeatureGoodsInfosArray = productFeatureGoodsInfos.split("\\*");
                        for (String featureGoodsInfo : tProductFeatureGoodsInfosArray) {
                            String[] attrInfos = featureGoodsInfo.split("\\^");

                            String productIdSub = attrInfos[0];//商品编号
                            String productNameSub = attrInfos[1];//商品名称
                            String volumeSub = attrInfos[2];//体积
                            String weightSub = attrInfos[3];//重量
                            String salePriceSub = attrInfos[4];//销售价格
                            String marketPriceSub = attrInfos[5];//市场价格
                            String costPriceSub = attrInfos[6];//成本价格
                            String featureProductNameSub = attrInfos[7];//商品特征
                            String featureProductIdSub = attrInfos[8];//商品特征ID
                            String operateTypeSub = attrInfos[9];//操作种类

                            String productFacilityInfosSub = attrInfos[10];//仓库信息
                            // 处理当特征库存数据为空的场合
                            if ("none".equals(attrInfos[10])) {
                                productFacilityInfosSub = "";
                            }
                            //String productContentInfosSub =attrInfos[11];//图片信息
                            Map<String, Object> mapTemp;
                            if (UtilValidate.isNotEmpty(salePriceSub) && UtilValidate.isNotEmpty(productNameSub)) {
                                mapTemp = FastMap.newInstance();
                                mapTemp.put("productIdSub", productIdSub);
                                mapTemp.put("productNameSub", productNameSub);
                                mapTemp.put("productNameSub", productNameSub);
                                mapTemp.put("weightSub", weightSub);
                                mapTemp.put("volumeSub", volumeSub);
                                mapTemp.put("salePriceSub", salePriceSub);
                                mapTemp.put("marketPriceSub", marketPriceSub);
                                mapTemp.put("costPriceSub", costPriceSub);
                                mapTemp.put("featureProductNameSub", featureProductNameSub);
                                mapTemp.put("featureProductIdSub", featureProductIdSub);
                                mapTemp.put("operateTypeSub", operateTypeSub);
                                mapTemp.put("productFacilityInfosSub", productFacilityInfosSub);
                                //mapTemp.put("productContentInfosSub", productContentInfosSub);
                                productFeatureGoodsList.add(mapTemp);
                            }
                        }
                    }
                }

                for (Map<String, Object> map : productFeatureGoodsList) {

                    String productIdSub = (String) map.get("productIdSub");//商品编号
                    String productNameSub = (String) map.get("productNameSub");//商品名称
                    String volumeSub = (String) map.get("volumeSub");//体积
                    String weightSub = (String) map.get("weightSub");//重量
                    String salePriceSub = (String) map.get("salePriceSub");//销售价格
                    String marketPriceSub = (String) map.get("marketPriceSub");//市场价格
                    String costPriceSub = (String) map.get("costPriceSub");//成本价格
                    String featureProductNameSub = (String) map.get("featureProductNameSub");//商品特征
                    String featureProductIdSub = (String) map.get("featureProductIdSub");//商品特征Id
                    String operateTypeSub = (String) map.get("operateTypeSub");//商品特征操作类型
                    String productFacilityInfosSub = (String) map.get("productFacilityInfosSub");//仓库信息
                    //String productContentInfosSub =(String)map.get("productContentInfosSub");//图片信息


                    //////////////////////////////////////////////////////////////

                    Map<String, Object> productSubParams = FastMap.newInstance();
                    // 操作类型
//            		if(UtilValidate.isNotEmpty(operateType)){
                    //    	productSubParams.put("operateType", operateType);
                    //    }
                    // 商品编号
                    if (UtilValidate.isNotEmpty(productIdSub)) {
                        productSubParams.put("productId", productIdSub);
                    }
                    //商品类型
                    if (UtilValidate.isNotEmpty(productTypeId)) {
                        productSubParams.put("productTypeId", productTypeId);
                    }
                    //商品分类ID
                    if (UtilValidate.isNotEmpty(productCategoryId)) {
                        productSubParams.put("productCategoryId", productCategoryId);
                    }
                    //商品名称
                    if (UtilValidate.isNotEmpty(productNameSub)) {
                        productSubParams.put("productName", productNameSub);
                    }

                    //是否上下架
                    if (UtilValidate.isNotEmpty(isOnline)) {
                        productSubParams.put("isOnline", isOnline);
                    }

                    //商品品牌Id
                    if (UtilValidate.isNotEmpty(brandId)) {
                        productSubParams.put("brandId", brandId);
                    }
                    //体积
                    if (UtilValidate.isNotEmpty(volumeSub)) {
                        productSubParams.put("volume", volumeSub);
                    }
                    //重量
                    if (UtilValidate.isNotEmpty(weightSub)) {
                        productSubParams.put("weight", weightSub);
                    }
                    //是否使用特征
                    if (UtilValidate.isNotEmpty(isUsedFeature)) {
                        productSubParams.put("isUsedFeature", isUsedFeature);
                    }
                    // 销售价格
                    if (UtilValidate.isNotEmpty(salePriceSub)) {
                        productSubParams.put("salePrice", salePriceSub);
                    }
                    // 市场价格
                    if (UtilValidate.isNotEmpty(marketPriceSub)) {
                        productSubParams.put("marketPrice", marketPriceSub);
                    }
                    // 成本价格
                    if (UtilValidate.isNotEmpty(costPriceSub)) {
                        productSubParams.put("costPrice", costPriceSub);
                    }

                    // 商品特征
                    if (UtilValidate.isNotEmpty(featureProductNameSub)) {
                        productSubParams.put("featureProductName", featureProductNameSub);
                    }

                    // 商品特征Id
                    if (UtilValidate.isNotEmpty(featureProductIdSub)) {
                        productSubParams.put("featureProductId", featureProductIdSub);
                        productSubParams.put("productFeatureInfos", featureProductIdSub);
                    }

                    // 商品库存信息
                    if (UtilValidate.isNotEmpty(productFacilityInfosSub)) {
                        productSubParams.put("productFacilityInfos", productFacilityInfosSub);
                    }
                    // 商品特征信息
                    //    if (UtilValidate.isNotEmpty(productFeatureInfos)) {
                    //        productSubParams.put("productFeatureInfos", productFeatureInfos);
                    //    }
                    // 商品特征操作类型
                    if (UtilValidate.isNotEmpty(operateTypeSub)) {
                        productSubParams.put("operateType", operateTypeSub);
                        if ("delete".equals(operateTypeSub)) {
                            if (UtilValidate.isNotEmpty(productFeatureGoodsDelIds)) {
                                productSubParams.put("productFeatureGoodsDelIds", productFeatureGoodsDelIds);
                            }
                        }
                    }
                    //商家名称
                    if (UtilValidate.isNotEmpty(businessPartyId)) {
                        productSubParams.put("businessPartyId", businessPartyId);
                    }

                    // 图片信息
                    if (UtilValidate.isNotEmpty(productContentInfos)) {
                        //productSubParams.put("productContentInfos", productContentInfosSub);
                    }


                    // 主商品共通信息
                    //销售开始时间
                    if (UtilValidate.isNotEmpty(startTime)) {
                        productSubParams.put("startTime", startTime);
                    }
                    //销售结束时间
                    if (UtilValidate.isNotEmpty(endTime)) {
                        productSubParams.put("endTime", endTime);
                    }

                    //商品副标题
                    if (UtilValidate.isNotEmpty(productSubheadName)) {
                        productSubParams.put("productSubheadName", productSubheadName);
                    }

                    //SEO关键字
                    if (UtilValidate.isNotEmpty(seoKeyword)) {
                        productSubParams.put("seoKeyword", seoKeyword);
                    }

                    // PC端详情
                    if (UtilValidate.isNotEmpty(pcDetails)) {
                        productSubParams.put("pcDetails", pcDetails);
                    }
                    // 移动端详情
                    if (UtilValidate.isNotEmpty(mobileDetails)) {
                        productSubParams.put("mobileDetails", mobileDetails);
                    }

                    // 商品分类属性信息
                    if (UtilValidate.isNotEmpty(productAttrInfos)) {
                        productSubParams.put("productAttrInfos", productAttrInfos);
                    }

                    // 商品关系信息
                    if (UtilValidate.isNotEmpty(productAssocInfos)) {
                        productSubParams.put("productAssocInfos", productAssocInfos);
                    }


                    // Add by zhajh at 20180312 yabiz相关内容 Begin
                    // 推荐到首页
                    if (UtilValidate.isNotEmpty(isRecommendHomePage)) {
                        productSubParams.put("isRecommendHomePage", isRecommendHomePage);
                    }
                    // 支持服务
                    if (UtilValidate.isNotEmpty(isSupportService)) {
                        productSubParams.put("isSupportService", isSupportService);
                    }
                    // 支持服务类型
                    if(UtilValidate.isNotEmpty(supportServiceType)){
                        productSubParams.put("supportServiceType", supportServiceType);
                    }
                    // 积分抵扣
                    if (UtilValidate.isNotEmpty(integralDeductionType)) {
                        productSubParams.put("integralDeductionType", integralDeductionType);
                    }
                    // 积分抵扣上限
                    if (UtilValidate.isNotEmpty(integralDeductionUpper)) {
                        productSubParams.put("integralDeductionUpper", integralDeductionUpper);
                    }
                    // 每人限购数量
                    if (UtilValidate.isNotEmpty(purchaseLimitationQuantity)) {
                        productSubParams.put("purchaseLimitationQuantity", purchaseLimitationQuantity);
                    }
                    // 列表展示
                    if (UtilValidate.isNotEmpty(isListShow)) {
                        productSubParams.put("isListShow", isListShow);
                    }
                    // 代金券面额
                    if (UtilValidate.isNotEmpty(voucherAmount)) {
                        productSubParams.put("voucherAmount", voucherAmount);
                    }
                    // 使用限制
                    if (UtilValidate.isNotEmpty(useLimit)) {
                        productSubParams.put("useLimit", useLimit);
                    }
                    // 使用开始时间
                    if (UtilValidate.isNotEmpty(useStartTime)) {
                        productSubParams.put("useStartTime", useStartTime);
                    }
                    // 使用结束时间
                    if (UtilValidate.isNotEmpty(useEndTime)) {
                        productSubParams.put("useEndTime", useEndTime);
                    }
                    // 是否保税商品
                    if (UtilValidate.isNotEmpty(isBondedGoods)) {
                        productSubParams.put("isBondedGoods", isBondedGoods);
                    }

                    // 商铺编码
                    if (UtilValidate.isNotEmpty(productStoreId)) {
                        productSubParams.put("productStoreId", productStoreId);
                    }

                    // 是否自营
                    if (UtilValidate.isNotEmpty(isInner)) {
                        productSubParams.put("isInner", isInner);
                    }

                    // 是否自营
                    if (UtilValidate.isNotEmpty(platformClassId)) {
                        productSubParams.put("platformClassId", platformClassId);
                    }
                    // 供应商编码
                    if(UtilValidate.isNotEmpty(providerId)){
                        productSubParams.put("providerId", providerId);

                    }
                    // Add by zhajh at 20180312 yabiz相关内容 End


                    if (UtilValidate.isNotEmpty(productSubParams)) {
                        // 主商品编号
                        if (UtilValidate.isNotEmpty(productIdMain)) {
                            productSubParams.put("mainProductId", productIdMain);
                        }
                        productSubParams.put("userLogin", userLogin);

                        resultMap = FastMap.newInstance();
                        resultMap = dispatcher.runSync("updateProductIcoPro", productSubParams);
                    }
                    /////////////////////////////////////////////////////////////
                }
            } catch (GenericServiceException e) {
                return ServiceUtil.returnError(e.getMessage());
            }


        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        result = ServiceUtil.returnSuccess();
        return result;
    }


    ////////////////////////////////////////////////
    // Add by zhajh at 20160112 商品 end


    /*********************************************************************/


    // Add by zhajh at 20150115 查找商品 Begin

    /**
     * 查找商品
     *
     * @param dct
     * @param context
     * @return
     * @throws GenericEntityException
     * @throws NumberFormatException
     */
    public static Map<String, Object> findProductIcoPro(DispatchContext dct, Map<String, ? extends Object> context) throws NumberFormatException, GenericEntityException {
        
        Delegator delegator = dct.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
//      String orderFiled = (String) context.get("ORDER_FILED");
//      String orderFiledBy = (String) context.get("ORDER_BY");
        String orderFiled = (String) context.get("sortField");
        String orderFiledBy = (String) context.get("sortType");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        String productName = (String) context.get("productName");// 商品名称
        String productId = (String) context.get("productId");// 商品编号
        String brandId = (String) context.get("brandId");// 商品品牌
        String levelFirst = (String) context.get("levelFirst");// 一级分类
        String levelSecond = (String) context.get("levelSecond");// 二级分类
        String levelThird = (String) context.get("levelThird");// 三级分类
        String isOnline = (String) context.get("isOnline");// 是否上架
        String productTypeId = (String) context.get("productTypeId");// 商品类型
        String editFlg = (String) context.get("editFlg");// 商品编辑标识
        String productStoreId = (String) context.get("productStoreId"); // 店铺编码

        // 商品编辑页面跳转过来的时候 清空参数值
        if (UtilValidate.isNotEmpty(editFlg)) {
            productName = null;// 商品名称
            productId = null;// 商品编号
            brandId = null;// 商品品牌
            levelFirst = null;// 一级分类
            levelSecond = null;// 二级分类
            levelThird = null;// 三级分类
            isOnline = null;// 是否上架
            productTypeId = null;// 商品类型
        }

        List<GenericValue> productList = FastList.newInstance();
        int productListSize = 0;
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
        // blank param list
        String paramList = "";

        DynamicViewEntity dynamicView = new DynamicViewEntity();

        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();

        // default view settings
        dynamicView.addMemberEntity("PR", "Product");
        dynamicView.addAliasAll("PR", "", null);
        dynamicView.addAlias("PR", "productName");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "isOnline");
        dynamicView.addAlias("PR", "primaryProductCategoryId");
        dynamicView.addAlias("PR", "productTypeId");
        dynamicView.addAlias("PR", "brandId");
        dynamicView.addAlias("PR", "businessPartyId");
        dynamicView.addAlias("PR", "isVerify");
        dynamicView.addAlias("PR", "isUsedFeature");
        dynamicView.addAlias("PR", "isVirtual");
//        dynamicView.addAlias("PR", "productStoreId");
        dynamicView.addAlias("PR", "createdStamp");


       

        fieldsToSelect.add("productId");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("primaryProductCategoryId");
        fieldsToSelect.add("isOnline");
        fieldsToSelect.add("productTypeId");
        fieldsToSelect.add("brandId");
        fieldsToSelect.add("businessPartyId");
        fieldsToSelect.add("isVerify");
        fieldsToSelect.add("isUsedFeature");
        fieldsToSelect.add("isVirtual");
        fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("createdStamp");

        if (UtilValidate.isNotEmpty(orderFiled)) {
            if ("desc".equals(orderFiledBy)) {
                orderBy.add("-" + orderFiled);
            } else {
                orderBy.add(orderFiled);
            }
        } else {
            orderBy.add("-createdStamp");

        }

        //查询
        // 商品名称
        if (UtilValidate.isNotEmpty(productName)) {
            paramList = paramList + "&productName=" + productName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + productName + "%")));
        }
        // 商品编号
        if (UtilValidate.isNotEmpty(productId)) {
            paramList = paramList + "&productId=" + productId;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + productId + "%")));
        }


        // 商品品牌
        if (UtilValidate.isNotEmpty(brandId)) {
            paramList = paramList + "&brandId=" + brandId;
            andExprs.add(EntityCondition.makeCondition("brandId", brandId));
        }
        // 是否上架
        if (UtilValidate.isNotEmpty(isOnline)) {
            paramList = paramList + "&isOnline=" + isOnline;
            if ("Y".equals(isOnline)) {
                andExprs.add(EntityCondition.makeCondition("isOnline", "Y"));
                andExprs.add(EntityCondition.makeCondition("isVerify", "Y"));
            } else {
                List<EntityExpr> exprOnlines = UtilMisc.toList(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"),
                        EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "N"));
                andExprs.add(EntityCondition.makeCondition(exprOnlines, EntityOperator.OR));
                List<EntityExpr> exprVerifys = UtilMisc.toList(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "N"),
                        EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, null),
                        EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, ""));
                andExprs.add(EntityCondition.makeCondition(exprVerifys, EntityOperator.OR));

            }
            //andExprs.add(EntityCondition.makeCondition("isOnline", isOnline));
        }

        // 商品类型
        if (UtilValidate.isNotEmpty(productTypeId)) {
            paramList = paramList + "&productTypeId=" + productTypeId;
            andExprs.add(EntityCondition.makeCondition("productTypeId", productTypeId));
        }



        // 一级分类
        if (UtilValidate.isNotEmpty(levelFirst)) {
            paramList = paramList + "&levelFirst=" + levelFirst;
        }
        // 二级分类
        if (UtilValidate.isNotEmpty(levelSecond)) {
            paramList = paramList + "&levelSecond=" + levelSecond;
        }
        // 三级分类
        if (UtilValidate.isNotEmpty(levelThird)) {
            paramList = paramList + "&levelThird=" + levelThird;
        }

        // 取得三级分类ID列表
        List<String> categoryIdList = getProductCategoryIdList(delegator, levelFirst, levelSecond, levelThird);
        if (categoryIdList.size() > 0) {
            andExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, categoryIdList));
        }





        // 店铺信息
        if (UtilValidate.isNotEmpty(productStoreId)) {
            andExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        }

        andExprs.add(EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, null));// 只有主商品
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        andExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
       
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
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
                // using list iterator
                
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

                // get the partial list for this page
                productList = pli.getPartialList(lowIndex, viewSize);

                // attempt to get the full size
                productListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > productListSize) {
                    highIndex = productListSize;
                }

                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in productBrand find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            productListSize = 0;
        }
        result.put("productList", productList);
        result.put("productListSize", Integer.valueOf(productListSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        
        return result;
    }

    // Add by zhajh at 20150115 查找商品 End


    /**
     * 取得三级分类的ID列表
     *
     * @param delegator
     * @param levelFirst
     * @param levelSecond
     * @param levelThird
     * @return
     * @throws NumberFormatException
     * @throws GenericEntityException
     */
    public static List<String> getProductCategoryIdList(Delegator delegator, String levelFirst, String levelSecond, String levelThird) throws NumberFormatException, GenericEntityException {
        List<String> productCategoryIdList = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        orderBy.add("productCategoryId");
        // 三级分类选择的场合
        if (UtilValidate.isNotEmpty(levelThird)) {
            productCategoryIdList.add(levelThird);
            // 二级分类选择的场合
        } else if (UtilValidate.isNotEmpty(levelSecond)) {
            entityConditionList = FastList.newInstance();
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", levelSecond));
            List<GenericValue> categoryIdList = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            for (GenericValue genericValue : categoryIdList) {
                productCategoryIdList.add(genericValue.getString("productCategoryId"));
            }
            // 一级分类选择的场合
        } else if (UtilValidate.isNotEmpty(levelFirst)) {
            entityConditionList = FastList.newInstance();
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", levelFirst));
            List<GenericValue> categoryIdLevel2List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            for (GenericValue genericValue : categoryIdLevel2List) {
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", genericValue.getString("productCategoryId")));
                List<GenericValue> categoryIdList = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                for (GenericValue genericValue2 : categoryIdList) {
                    productCategoryIdList.add(genericValue2.getString("productCategoryId"));
                }
            }
            // 没有选择的场合
        } else {
            entityConditionList = FastList.newInstance();
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
            List<GenericValue> categoryIdList = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            for (GenericValue genericValue : categoryIdList) {
                productCategoryIdList.add(genericValue.getString("productCategoryId"));
            }
        }
        return productCategoryIdList;
    }


    // Add by zhajh at 20160115 商品的删除处理 Begin

    /**
     * 商品的删除处理ByIds
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductByIds(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品记录 ids
        String ids = request.getParameter("checkedIds");
        String productIds = ""; // 已选择的商品ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                productIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> productIdsList = FastList.newInstance();
        List<GenericValue> productSkuList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(productIds)) {
            productIdsList = UtilMisc.toListArray(productIds.split(","));
        }
        // 根据条件取得商品的数据
        List<GenericValue> productInfoList = null;
//        List<GenericValue> productTagAssocList = null ;
//        List<GenericValue> productCategoryattributeAssocList = null ;
//        List<GenericValue> productParameterList = null ;
//        List<GenericValue> productPriceList = null ;
//        List<GenericValue> inventoryItemList = null ;
//        List<GenericValue> productFacilityList = null ;
//        List<GenericValue> productKeywordNewList = null ;

        EntityCondition condition = EntityCondition.makeCondition("productId", EntityOperator.IN, productIdsList);
        try {
            productInfoList = delegator.findList("Product", condition, null, null, null, false);
//        	productTagAssocList =delegator.findList("ProductTagAssoc", condition, null ,null, null, false);
//        	productCategoryattributeAssocList =delegator.findList("ProductCategoryattributeAssoc", condition, null ,null, null, false);
//        	productParameterList =delegator.findList("ProductParameter", condition, null ,null, null, false);
//        	productPriceList =delegator.findList("ProductPrice", condition, null ,null, null, false);
//        	inventoryItemList =delegator.findList("InventoryItem", condition, null ,null, null, false);
//        	productFacilityList =delegator.findList("ProductFacility", condition, null ,null, null, false);
//        	productKeywordNewList=delegator.findList("ProductKeyword", condition, null ,null, null, false);
            // 删除处理
            if (UtilValidate.isNotEmpty(productInfoList)) {
//            	if(UtilValidate.isNotEmpty(productTagAssocList)){
//
//            		delegator.removeAll(productTagAssocList);
//            	}
//				if(UtilValidate.isNotEmpty(productCategoryattributeAssocList)){
//
//					delegator.removeAll(productCategoryattributeAssocList);
//				}
//				if(UtilValidate.isNotEmpty(productParameterList)){
//
//					delegator.removeAll(productParameterList);
//				}
//				if(UtilValidate.isNotEmpty(productPriceList)){
//					delegator.removeAll(productPriceList);
//
//				}
//				if(UtilValidate.isNotEmpty(inventoryItemList)){
//
//					delegator.removeAll(inventoryItemList);
//				}
//				if(UtilValidate.isNotEmpty(productFacilityList)){
//
//					delegator.removeAll(productFacilityList);
//				}
//
                //if(UtilValidate.isNotEmpty(productKeywordNewList)){
//
//					delegator.removeAll(productKeywordNewList);
//				}
                for (GenericValue productInfo : productInfoList) {
                    productInfo.set("isDel", "Y");
                    // 删除主商品的场合，删除sku商品
                    if (UtilValidate.isNotEmpty(productInfo.getString("isVirtual"))) {
                        if ("Y".equals(productInfo.getString("isVirtual"))) {
                            String mainProductId = productInfo.getString("productId");
                            productSkuList = delegator.findList("Product", EntityCondition.makeCondition("mainProductId", mainProductId), null, null, null, false);
                            if (UtilValidate.isNotEmpty(productSkuList)) {
                                for (GenericValue productSkuInfo : productSkuList) {
                                    productSkuInfo.set("isDel", "Y");
                                    productSkuInfo.store();
                                }
                            }
                        }
                    }
                    productInfo.store();
                    // 删除商品标签关系表
                    List<GenericValue> productTagDelList = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", productInfo.getString("productId")));
                    delegator.removeAll(productTagDelList);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }


    /**
     * 检查商品是否上架处理ByIds
     *
     * @param request
     * @param response
     * @return
     */
    public static String chkDelProductByIds(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品记录 ids
        String ids = request.getParameter("checkedIds");
        String productIds = ""; // 已选择的商品ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                productIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> productIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(productIds)) {
            productIdsList = UtilMisc.toListArray(productIds.split(","));
        }
        List<EntityCondition> entityConditionList = FastList.newInstance();
        // 审核状态为未审核，上架申请状态是未上架申请
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "Y"),
                EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
        entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));
        //entityConditionList.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS,"Y"));
        entityConditionList.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productIdsList));
        // 根据条件取得商品的数据
        List<GenericValue> productInfoList = null;
        try {
            productInfoList = delegator.findList("Product", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        // 保存成功
        request.setAttribute("listSize", productInfoList.size());
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160115 删除处理 End


    // Add by zhajh at 20160115 商品的上下架更新处理 Begin

    /**
     * 商品的上下架更新处理ByIds
     *
     * @param request
     * @param response
     * @return
     */
    public static String updateProductIsOnlineStatus(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品记录 ids
        String ids = request.getParameter("checkedIds");
        String onlineStatus = request.getParameter("onlineStatus");
        String isInner =request.getParameter("isInner");
        String productIds = ""; // 已选择的商品ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                productIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品上下架更新处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        List<String> productIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(productIds)) {
            productIdsList = UtilMisc.toListArray(productIds.split(","));
        }

        // 根据条件取得商品的数据
        List<GenericValue> productInfoList = null;
        List<GenericValue> productSkuInfoList = null;

        EntityCondition condition = EntityCondition.makeCondition("productId", EntityOperator.IN, productIdsList);
        try {
            productInfoList = delegator.findList("Product", condition, null, null, null, false);

            // 是否审核的判断
            String chkVerifyFlg="Y";
            List<GenericValue> productRulesInfo=delegator.findByAnd("ProductRules");
            if(UtilValidate.isNotEmpty(productRulesInfo)){
                if(UtilValidate.isNotEmpty(isInner)) {
                    if ("Y".equals(isInner)) {
                        if ("N".equals(productRulesInfo.get(0).getString("physicalProductStatus"))) {
                            chkVerifyFlg = "N";
                        }
                    } else {
                        if ("N".equals(productRulesInfo.get(0).getString("virtualProductStatus"))) {
                            chkVerifyFlg = "N";
                        }
                    }
                }
            }

            // 更新处理
            if (UtilValidate.isNotEmpty(productInfoList)) {
                List<String>  productIdRedisList=FastList.newInstance();
                // TODO isDel 临时 N==>Y  原因是不能添加为上线的商品编码
//                String isDel="N";
                String isDel="Y";
                for (GenericValue productInfo : productInfoList) {
                    if ("Y".equals(onlineStatus)) {
                        productIdRedisList=FastList.newInstance();
                        productInfo.set("isOnline", "Y");
                        if("Y".equals(chkVerifyFlg)) {
                            productInfo.set("isVerify", "");
                        }else{
                            productInfo.set("isVerify", "Y");
                            // 取得当前商品编码
                            String curProductId=productInfo.getString("productId");
                            if(UtilValidate.isNotEmpty(curProductId)){
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
                    } else if ("N".equals(onlineStatus)) {
                        productIdRedisList=FastList.newInstance();
                        productInfo.set("isOnline", "N");//在线状态
                        productInfo.set("isVerify", "");//审核状态
                        // Redis 信息的删除处理
                        String curProductId=productInfo.getString("productId");
                        if(UtilValidate.isNotEmpty(curProductId)){
                            productIdRedisList.add(curProductId);
                        }
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_summary")){
//                            UtilRedis.del(productInfo.getString("productId") + "_summary");// 产品缓存
//                        }
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_downPromo")) {
//                            UtilRedis.del(productInfo.getString("productId") + "_downPromo");// 产品直降信息
//                        }
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_groupOrder")) {
//                            UtilRedis.del(productInfo.getString("productId") + "_groupOrder");// 产品团购信息
//                        }
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_seckill")) {
//                            UtilRedis.del(productInfo.getString("productId") + "_seckill"); // 产品秒杀信息
//                        }
                    }
                    productInfo.store();

                    if ("N".equals(onlineStatus)) {
                        isDel="Y";
                    }

                    if(UtilValidate.isNotEmpty(productIdRedisList)){
                        String redisProductStr = Joiner.on(",").join(productIdRedisList);
                        Map<String, Object> reParams = FastMap.newInstance();
                        reParams.put("productIds", redisProductStr);
                        reParams.put("isDel", isDel);
                        Map<String, Object> resultMap = FastMap.newInstance();
                        try {
                            // 调用服务 productInfoRedisPro
                            resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                        } catch (GenericServiceException e) {
                            return "error";
                        }
                    }


                    // 取得该商品下的所有Sku商品
                    EntityCondition conditionSku = EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, productInfo.getString("productId"));
                    productSkuInfoList = delegator.findList("Product", conditionSku, null, null, null, false);
                    if (productSkuInfoList.size() > 0) {
                        productIdRedisList=FastList.newInstance();
                        for (GenericValue productSkuInfo : productSkuInfoList) {
                            if ("Y".equals(onlineStatus)) {
                                productSkuInfo.set("isOnline", "Y");
                                productSkuInfo.set("isVerify", "");
                            } else if ("N".equals(onlineStatus)) {
                                productSkuInfo.set("isOnline", "N");//在线状态
                                productSkuInfo.set("isVerify", "");//审核状态
                                String curProductId=productSkuInfo.getString("productId");
                                // Redis 信息的删除处理
                                if(UtilValidate.isNotEmpty(curProductId)){
                                    productIdRedisList.add(curProductId);
                                }
//                                if(UtilRedis.exists(productSkuInfo.getString("productId")+"_summary")){
//                                    UtilRedis.del(productSkuInfo.getString("productId") + "_summary");// 产品缓存
//                                }
//                                if(UtilRedis.exists(productSkuInfo.getString("productId")+"_downPromo")) {
//                                    UtilRedis.del(productSkuInfo.getString("productId") + "_downPromo");// 产品直降信息
//                                }
//                                if(UtilRedis.exists(productSkuInfo.getString("productId")+"_groupOrder")) {
//                                    UtilRedis.del(productSkuInfo.getString("productId") + "_groupOrder");// 产品团购信息
//                                }
//                                if(UtilRedis.exists(productSkuInfo.getString("productId")+"_seckill")) {
//                                    UtilRedis.del(productSkuInfo.getString("productId") + "_seckill"); // 产品秒杀信息
//                                }

                            }
                            productSkuInfo.store();
                        }
                        if(UtilValidate.isNotEmpty(productIdRedisList)){
                            String redisProductStr = Joiner.on(",").join(productIdRedisList);
                            Map<String, Object> reParams = FastMap.newInstance();
                            reParams.put("productIds", redisProductStr);
                            reParams.put("isDel", isDel);
                            Map<String, Object> resultMap = FastMap.newInstance();
                            try {
                                // 调用服务 productInfoRedisPro
                                resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                            } catch (GenericServiceException e) {
                                return "error";
                            }
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            return "error";
        }

        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160115 商品在线状态的更新处理 End

    /**
     * 首页
     *

     * @return
     */
    public static Map<String, Object> mainIndex(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        List<String> communityId = UtilGenerics.cast(context.get("communityId"));
        /** 返回json结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put("resultData", resultData);
            return result;
        }
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }

        Map<String, Object> paramContext = FastMap.newInstance();
        paramContext.put("userLogin", userLogin);
        paramContext.put("communityId", communityId);
        //区块数据
        List<List<Map<String, Object>>> banner = null;
        try {
            Map<String, Object> bannerResult = dispatcher.runSync("getBanner", paramContext);
            banner = (List<List<Map<String, Object>>>) bannerResult.get("banner");
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        resultData.put("banner", banner);
        //分类区数据
        Map<String, Object> classifyResult = null;
        try {
            classifyResult = dispatcher.runSync("getClassify", UtilMisc.toMap("userLoginId", userLoginId, "userLogin", userLogin));
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        resultData.put("classify", classifyResult.get("resultData"));
        //抢购区数据
        Map<String, Object> snapUp = getSnapUP(dctx, context);
        if (UtilValidate.isNotEmpty(snapUp.get("activityMap"))) {
            resultData.put("snapUp", snapUp.get("activityMap"));
            resultData.put("isSnapUp", snapUp.get("isSnapUp"));
        } else if (UtilValidate.isNotEmpty(snapUp.get("resultMaps"))) {
            Map<String, Object> snap = (Map<String, Object>) snapUp.get("resultMaps");
            Map<String, Object> snaps = (Map<String, Object>) snap.get("resultData");
            if ((Integer) snaps.get("max") > 0) {
                resultData.put("isSnapUp", true);
            } else {
                resultData.put("isSnapUp", false);
            }
        }

        //活动区数据
        List<List<Map<String, Object>>> activity = null;
        try {
            Map<String, Object> topResults = (Map<String, Object>) dispatcher.runSync("getProductTopicActivity", paramContext);
            activity = (List<List<Map<String, Object>>>) topResults.get("activityList");
            resultData.put("activity", activity);
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        //推荐区数据
        List<GenericValue> recommend = null;
        List<Map<String, Object>> recommendMap = null;
        try {
            Map<String, Object> showIndexResult = dispatcher.runSync("getPromoShowIndex", paramContext);
            recommend = (List<GenericValue>) showIndexResult.get("activityList");
            Map<String, Object> paramContexts = FastMap.newInstance();
            paramContexts.put("userLoginId", userLoginId);
            paramContexts.put("communityId", communityId);
            paramContexts.put("classify", "all");
            paramContexts.put("sort", "default");
            Map<String, Object> showallResult = dispatcher.runSync("getPromoForWap", paramContexts);
            Map<String, Object> s = (Map<String, Object>) showallResult.get("resultData");
            if (UtilValidate.isNotEmpty(recommend)) {
                recommendMap = changeListFormGenericValue(recommend, dctx, context);
                resultData.put("recommend", recommendMap);
                resultData.put("isRecommend", true);
            } else {
                resultData.put("isRecommend", false);
            	/*if((Integer)s.get("max")>0){
            		 resultData.put("isRecommend", true);
            	}else{
            		 resultData.put("isRecommend", false);
            	}*/
            }
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        result.put("resultData", resultData);
        return result;
    }

    /**
     * 推荐区块数据结果集转换为List add by wcy
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> changeListFormGenericValue(List<GenericValue> valueList, DispatchContext dctx, Map<String, ? extends Object> context) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Double[] star = new Double[]{0.5, 1D, 1.5, 2D, 2.5, 3D, 3.5, 4D, 4.5, 5D};
        BigDecimal score = BigDecimal.ZERO;
        Long num = 0L;
        for (GenericValue v : valueList) {
            Map<String, Object> map = FastMap.newInstance();
            List<String> marks = FastList.newInstance();
            if ("Y".equals(v.get("isAnyReturn"))) {
                marks.add("随时退");
            }
            if ("Y".equals(v.get("isSupportOverTimeReturn"))) {
                marks.add("过期退");
            }
            if ("Y".equals(v.get("isPostageFree"))) {
                marks.add("包邮");
            }
            //if("Y".equals(v.get("isSupportScore"))) marks.add("活动可积分");
            //if("Y".equals(v.get("isSupportReturnScore"))) marks.add("退货返回积分");
            //if("Y".equals(v.get("isShowIndex"))) marks.add("推荐到首页");
            map.put("id", v.getString("activityId"));
            GenericValue product;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", v.get("productId")));
                String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                map.put("picSrc", imageUrl);//图片路径
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            map.put("title", v.get("activityName"));
            map.put("marks", marks);
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PR", "ProductReview");
            dynamicView.addAlias("PR", "productId");
            dynamicView.addAlias("PR", "isShow");
            dynamicView.addAlias("PR", "sumProductRating", "productRating", null, null, null, "sum");
            dynamicView.addAlias("PR", "num", "productReviewId", null, null, null, "count");
            List<EntityCondition> andExprs = FastList.newInstance();
            andExprs.add(EntityCondition.makeCondition("isShow", EntityOperator.EQUALS, "1"));
            andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, v.get("productId")));
            EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            EntityFindOptions findOpts1 = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
            // using list iterator
            EntityListIterator productReviewEli = delegator.findListIteratorByCondition(dynamicView,
                    mainCond
                    , null, null, null, findOpts1);

            List<GenericValue> prods = productReviewEli.getCompleteList();
            //List<GenericValue> prods = productReviewEli.getPartialList(0, 1);
            Iterator<GenericValue> prodsIt = prods.iterator();
            while (prodsIt.hasNext()) {
                GenericValue oneProd = prodsIt.next();
                score = oneProd.getBigDecimal("sumProductRating");
                num = oneProd.getLong("num");
            }
            if (score == null || score.compareTo(BigDecimal.ZERO) == 0) {
                score = new BigDecimal(10);
            } else {
                score = score.divide(new BigDecimal(num), 0, BigDecimal.ROUND_HALF_EVEN).multiply(new BigDecimal(2));
            }
            map.put("score", score);   //商品评分，1-10分别对应0.5-5星
            Long activityQuantity = v.getLong("activityQuantity");
            Long hasBuyQuantity = v.getLong("hasBuyQuantity");
            if (hasBuyQuantity == null) {
                hasBuyQuantity = 0L;
            }
            //评价数量
            map.put("commentNum", num);
            //已售数量
            map.put("soldNum", hasBuyQuantity);
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
            map.put("price", salePrice);
            list.add(map);
        }
        return list;
    }

    /**
     * 获取分类区
     * 取商品管理-商品分类的二级分类
     * 每4条数据单独拆分一个数组
     *

     * @return
     */
    public static Map<String, Object> getClassify(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 查询字段 & 排序*/
        Set<String> fieldsToSelect = FastSet.newInstance();
        List<Map<String, Object>> resultData = FastList.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");

        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put("resultData", resultData);
            return result;
        }

        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }

        fieldsToSelect.add("productCategoryId");
        fieldsToSelect.add("categoryName");
        // 图片 id  add by gss
        fieldsToSelect.add("contentId");
        List<String> orderBy = FastList.newInstance();
        //分类升序排序
        orderBy.add("sequenceNum");
        try {
            //List<GenericValue> categoryList = delegator.findList("ProductCategory",EntityCondition.makeCondition("productCategoryLevel",EntityOperator.EQUALS,"2"),fieldsToSelect,orderBy,null,false);
            // add  by gss
            EntityConditionList<EntityExpr> Condition = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, new Long(2)),
                    EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null)), EntityOperator.AND);
            List<GenericValue> categoryList = delegator.findList("ProductCategory", Condition, fieldsToSelect, orderBy, null, false);

            List<Map<String, Object>> category4List = FastList.newInstance();
            //add  by gss
            if (UtilValidate.isNotEmpty(categoryList)) {
                for (int i = 0; i < categoryList.size(); i++) {
                    GenericValue category = categoryList.get(i);
                    Map<String, Object> map = new HashMap<String, Object>();
                    Map<String, Object> paramContext = new HashMap<String, Object>();
                    paramContext.put("userLogin", userLogin);
                    paramContext.put("contentId", category.getString("contentId"));
                    Map<String, Object> dataResource = dispatcher.runSync("getImageUrlByContentId", paramContext);
                    map.put("id", category.getString("productCategoryId"));
                    map.put("title", category.getString("categoryName"));
                    String ip = System.getProperty("ofbiz.home");
                    if (UtilValidate.isNotEmpty(dataResource) && UtilValidate.isNotEmpty(dataResource.get("url"))) {
                        String imgurl = (String) dataResource.get("url");
                        String url = imgurl.substring(imgurl.indexOf("/images/datasource"));
                        //map.put("picSrc", dataResource.get("url"));
                        map.put("picSrc", url);
                    }
                    // category4List.add(map);
                    /*if ((i+1) % 4 == 0) {//0,1,2,3
                    	resultData.add(category4List);
                        category4List = FastList.newInstance();
                    }*/
                    if (UtilValidate.isNotEmpty(map)) {
                        resultData.add(map);
                    }
                }
            }

        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }

        result.put("resultData", resultData);
        return result;
    }

    /**
     * 获取抢购区数据
     * 取营销管理-秒杀管理中“社区”为空及用户所绑定社区
     *

     * @return
     */
    public static Map<String, Object> getSnapUP(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();

        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        List<String> communityId = (List<String>) context.get("communityId");

        List<Map<String, Object>> resultData = FastList.newInstance();
        //未开始：preparation，进行中：ongoing，已结束：finish
       /* Map<String,Object> activityStatus = FastMap.newInstance();
        activityStatus.put("ACTY_AUDIT_UNBEGIN","preparation");
        activityStatus.put("ACTY_AUDIT_DOING","ongoing");
        activityStatus.put("ACTY_AUDIT_END","finish");*/
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put("resultData", resultData);
            return result;
        }
        Map<String, Object> paramContext = FastMap.newInstance();
        Map<String, Object> paramContext1 = FastMap.newInstance();
        paramContext.put("userLogin", userLogin);
        paramContext1.put("userLoginId", userLoginId);
        /** 用户所绑定社区 */
        if (UtilValidate.isNotEmpty(communityId)) {
            paramContext.put("communityId", communityId);
            paramContext1.put("communityId", communityId);
        }
        List<Map<String, Object>> activity1List = FastList.newInstance();
        Date now = new Date();
        try {
            Map<String, Object> resultMap = dispatcher.runSync("getPromoByCommunity", paramContext);
            List<GenericValue> activityList = (List<GenericValue>) resultMap.get("activityList");
            Map<String, Object> resultMaps = dispatcher.runSync("getSecKillForWap", paramContext1);
            if (UtilValidate.isNotEmpty(activityList)) {
                for (int i = 0; i < activityList.size(); i++) {
                    GenericValue activity = activityList.get(i);
                    Map<String, Object> activityMap = FastMap.newInstance();
                    //根据开始时间和结束时间来判断活动状态
                    Date activityStartDate = activity.getTimestamp("activityStartDate");
                    Date activityEndDate = activity.getTimestamp("activityEndDate");
                    //下架时间
                    //Date endDate=activity.getTimestamp("endDate");
                    //活动是否已结束
                    if (UtilValidate.isNotEmpty(activityEndDate) && now.before(activityEndDate)) {
                        activityMap.put("id", activity.getString("activityId"));
                        //当前时间大于开始时间 活动进行中
                        if (UtilValidate.isNotEmpty(activityStartDate) && now.after(activityStartDate)) {
                            activityMap.put("state", "ongoing");
                        } else {
                            activityMap.put("state", "preparation");
                        }
                        //activityMap.put("state",activityStatus.get(activity.getString("activityAuditStatus")));
                        //paramContext.put("contentId",activity.getString("contentId"));
                        //Map<String, Object> dataResource = dispatcher.runSync("getImageUrlByContentId", paramContext);
                        //activityMap.put("picSrc",dataResource.get("url"));
                        activityMap.put("title", activity.getString("activityName"));
                        activityMap.put("orgPrice", activity.getBigDecimal("price"));
                        activityMap.put("curPrice", activity.getBigDecimal("productPrice"));   //当前价格
                        GenericValue product;
                        try {
                            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", activity.get("productId")));
                            String imageUrl = ProductContentWrapper.getProductContentAsText(product, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                            activityMap.put("picSrc", imageUrl);//图片路径
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        activityMap.put("startTimestamp", activity.getTimestamp("activityStartDate").getTime() / 1000); //活动开始时间戳
                        activityMap.put("endTimestamp", activity.getTimestamp("activityEndDate").getTime() / 1000); //活动结束时间戳
                        //activity1List.add(activityMap);
                        /*if ((j+1) % 3 == 0) {//0,1,2,3
                        	resultData.add(activity1List);
                        	activity1List = FastList.newInstance();
                        }
                        j++;*/
                    }
                    if (UtilValidate.isNotEmpty(activityMap)) {
                        resultData.add(activityMap);
                    }
                }
                result.put("isSnapUp", true);
            }
            result.put("resultMaps", resultMaps);
            result.put("activityMap", resultData);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        return result;
    }


    // Add by zhajh at 20160118 根据商品ID取得仓库库存信息 Begin

    /**
     * 根据商品ID取得仓库库存信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getFacilityByProductId(HttpServletRequest request, HttpServletResponse response) {
        // 商品ID
        String productId = request.getParameter("productId");
        Map<String, Object> map = FastMap.newInstance();
        List<Map<String, Object>> facilityInfoList = new LinkedList<Map<String, Object>>();
        // 取得商品的库存信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            // 取得商品仓库信息
            List<GenericValue> productFacilityList = delegator.findByAnd("ProductFacility", UtilMisc.toMap("productId", productId));
            // 取得商品已锁定库存信息
            for (GenericValue genericValue : productFacilityList) {
                map = FastMap.newInstance();
                //根据商品ID获取订单状态为待发货的数量
//                List<GenericValue> productAlreadyLockList = delegator.findByAnd("OrderItemLockQuantity", UtilMisc.toMap("productId", productId, "statusId", "ITEM_WAITSHIP"));
                BigDecimal alreadyLockQuantitySum = new BigDecimal("0");
//                if (productAlreadyLockList.size() > 0) {
//                    alreadyLockQuantitySum = productAlreadyLockList.get(0).getBigDecimal("alreadyLockQuantitySum");
//                }
                // 取得商品库存明细信息
                List<GenericValue> inventoryItemList = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", productId, "facilityId", genericValue.getString("facilityId")));
                // 可用数量
                BigDecimal accountingQuantityTotal = new BigDecimal("0");
                // 预警数量
                BigDecimal warningQuantity = new BigDecimal("0");
                // 预警人邮箱
                String warningMail = "";
                // 库存明细ID
                String inventoryItemId = "";
                if (inventoryItemList.size() > 0) {
                    inventoryItemId = inventoryItemList.get(0).getString("inventoryItemId");
                    if(UtilValidate.isNotEmpty(inventoryItemList.get(0).getBigDecimal("accountingQuantityTotal"))) {
                        accountingQuantityTotal = inventoryItemList.get(0).getBigDecimal("accountingQuantityTotal");
                    }

                    if(UtilValidate.isNotEmpty(inventoryItemList.get(0).getBigDecimal("warningQuantity"))) {
                        warningQuantity = inventoryItemList.get(0).getBigDecimal("warningQuantity");
                    }

                    if(UtilValidate.isNotEmpty(inventoryItemList.get(0).getString("warningMail"))){
                        warningMail = inventoryItemList.get(0).getString("warningMail");
                    }

                    if(UtilValidate.isNotEmpty(inventoryItemList.get(0).getBigDecimal("lockQuantityTotal"))) {
                        alreadyLockQuantitySum = inventoryItemList.get(0).getBigDecimal("lockQuantityTotal");
                    }
                }
                // 库存总量
//                BigDecimal totalNum = alreadyLockQuantitySum.add(accountingQuantityTotal);
                // 可用库存概念理解方面的修改 （凯德） at201180524 by zhajh Begin
                BigDecimal totalNum = accountingQuantityTotal;
                accountingQuantityTotal=accountingQuantityTotal.subtract(alreadyLockQuantitySum);
                // 可用库存概念理解方面的修改 （凯德） at201180524 by zhajh end
                // 仓库名称
                List<GenericValue> facilityList = delegator.findByAnd("Facility", UtilMisc.toMap("facilityId", genericValue.getString("facilityId")));
                String facilityName = "";
                String facilityId = "";
                if (facilityList.size() > 0) {
                    facilityName = facilityList.get(0).getString("facilityName");
                    facilityId = facilityList.get(0).getString("facilityId");
                }
                map.put("alreadyLockQuantitySum", alreadyLockQuantitySum);
                map.put("accountingQuantityTotal", accountingQuantityTotal);
                map.put("warningQuantity", warningQuantity);
                map.put("warningMail", warningMail);
                map.put("totalNum", totalNum);
                map.put("facilityName", facilityName);
                map.put("facilityId", facilityId);
                map.put("inventoryItemId", inventoryItemId);
                facilityInfoList.add(map);
            }
        } catch (Exception e) {
            return "error";
        }
        // 保存成功
        request.setAttribute("facilityInfoList", facilityInfoList);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160118 根据商品ID取得仓库库存信息 End


    /**
     * 根据id递归查询商品分类的树形结构   add by qianjin 2016/01/19
     *
     * @param delegator
     * @param id
     * @param lookParent true：向上查询出所有的父节点,包括当前节点； false：向下查询出所有的子节点，不包括当前节点
     * @param resultMap
     * @return
     */
    public static Map getCateGoryTreeData(Delegator delegator, String id, Boolean lookParent, Map resultMap) {
        List idList = UtilValidate.isEmpty(resultMap.get("idList")) ? FastList.newInstance() : (List) resultMap.get("idList");
        List nameList = UtilValidate.isEmpty(resultMap.get("nameList")) ? FastList.newInstance() : (List) resultMap.get("nameList");
        try {
            //判断向上查询父节点或向下查询子节点
            if (lookParent) {
                //根据商品分类id查询
                List<GenericValue> productCategoryList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryId", id));
                //遍历商品分类list
                for (GenericValue pc_gv : productCategoryList) {
                    //存放商品分类id和name
                    idList.add(pc_gv.get("productCategoryId"));
                    nameList.add(pc_gv.get("categoryName"));
                    resultMap.put("idList", idList);
                    resultMap.put("nameList", nameList);
                    //判断父id是否为空
                    if (UtilValidate.isNotEmpty(pc_gv.get("primaryParentCategoryId"))) {
                        //递归父节点的数据
                        getCateGoryTreeData(delegator, pc_gv.get("primaryParentCategoryId").toString(), lookParent, resultMap);
                    }
                }
            } else {
                //将当前id作为父id查找子节点数据
                List<GenericValue> childCategoryList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", id));
                //遍历子节点list
                for (GenericValue child_gv : childCategoryList) {
                    //存放商品分类id和name
                    idList.add(child_gv.get("productCategoryId"));
                    nameList.add(child_gv.get("categoryName"));
                    resultMap.put("idList", idList);
                    resultMap.put("nameList", nameList);
                    //递归子节点数据
                    getCateGoryTreeData(delegator, child_gv.get("productCategoryId").toString(), lookParent, resultMap);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(resultMap.get("idList"))) {
            resultMap.put("idList", idList);
        }
        if (UtilValidate.isEmpty(resultMap.get("nameList"))) {
            resultMap.put("nameList", nameList);
        }
        return resultMap;
    }

    // Add by zhajh at 20160120 根据商品特征ID取得商品特征信息 Begin

    /**
     * 根据商品ID取得仓库库存信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductFeatureListById(HttpServletRequest request, HttpServletResponse response) {
        // 商品ID
        String featureIds = request.getParameter("featureIds");//商品特征ID
        String productFeatureTypeId = request.getParameter("productFeatureTypeId");
        Map<String, Object> map = FastMap.newInstance();
        List<String> featureIdList = FastList.newInstance();
        List<GenericValue> productFeatureList = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        orderBy.add("sequenceNum");
        // 取得商品的库存信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        int trCount = 0;
        int listSize = 0;
        try {
            // 取得商品特征信息
            productFeatureList = delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeId), orderBy);
            listSize = productFeatureList.size();
            double aa = listSize / 3;
            int bb = listSize % 3;
            int cc = 0;
            if (bb > 0) {
                trCount = (int) aa + 1;
            } else {
                trCount = (int) aa;
            }

            if (UtilValidate.isNotEmpty(featureIds)) {
                String[] atrrFeature = featureIds.split(",");
                for (String featureInfo : atrrFeature) {
                    featureIdList.add(featureInfo);
                }
            }
        } catch (Exception e) {
            return "error";
        }

        map.put("productFeatureList", productFeatureList);
        map.put("featureIdList", featureIdList);
        map.put("trCount", trCount);
        map.put("listSize", listSize);
        // 保存成功
        request.setAttribute("productFeatureInfo", map);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160120 根据商品特征ID取得商品特征信息  End


    // Add by zhajh at 20160121 根据选择商品特征获得得特征商品信息 Begin

    /**
     * 根据选择商品特征获得得特征商品信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductFeatureInfoListByFeature(HttpServletRequest request, HttpServletResponse response) {
        // 商品特征信息
        String productFeatureInfos = request.getParameter("productFeatureInfos");
        String productFeatureIdInfos = request.getParameter("productFeatureIdInfos");
        List<List<String>> productFeatureList = FastList.newInstance();
        List<String> productFeatureInfo = FastList.newInstance();
        List<Map<String, String>> productFeatureInfoList = FastList.newInstance();

        List<List<String>> productFeatureIdList = FastList.newInstance();
        List<String> productFeatureIdInfo = FastList.newInstance();

        Map<String, String> map = FastMap.newInstance();
        Map<String, Object> mapRtn = FastMap.newInstance();
        // 取得商品特征信息
        if (UtilValidate.isNotEmpty(productFeatureInfos)) {
            String[] tProductFeatureInfosArray = productFeatureInfos.split(",");
            for (String featureInfo : tProductFeatureInfosArray) {
                productFeatureInfo = FastList.newInstance();
                String[] attrInfos = featureInfo.split("\\|");
                for (String string : attrInfos) {
                    String featureName = string;//特征名称
                    if (UtilValidate.isNotEmpty(featureName)) {
                        productFeatureInfo.add(featureName);
                    }
                }
                productFeatureList.add(productFeatureInfo);
            }
        }

        // 取得商品特征ID信息
        if (UtilValidate.isNotEmpty(productFeatureIdInfos)) {
            String[] tProductFeatureIdInfosArray = productFeatureIdInfos.split(",");
            for (String featureIdInfo : tProductFeatureIdInfosArray) {
                productFeatureIdInfo = FastList.newInstance();
                String[] attrInfos = featureIdInfo.split("\\|");
                for (String string : attrInfos) {
                    String featureId = string;//特征ID
                    if (UtilValidate.isNotEmpty(featureId)) {
                        productFeatureIdInfo.add(featureId);
                    }
                }
                productFeatureIdList.add(productFeatureIdInfo);
            }
        }


        // 取得商品特征组合名称
        int productFeatureInfoListSize = productFeatureList.size();
        int productFeatureIdInfoListSize = productFeatureIdList.size();
        if ((productFeatureInfoListSize > 0) && (productFeatureIdInfoListSize > 0) && (productFeatureIdInfoListSize == productFeatureInfoListSize)) {
            switch (productFeatureInfoListSize) {
                case 1:
//					for (String featureName : productFeatureList.get(0)) {
//						map=FastMap.newInstance();
//						map.put("featureName", featureName);
//						productFeatureInfoList.add(map);
//					}
                    for (int i = 0; i < productFeatureList.get(0).size(); i++) {
                        String curFeatureName = productFeatureList.get(0).get(i);// 特征名称
                        String curFeatureId = productFeatureIdList.get(0).get(i);// 特征编码
                        map = FastMap.newInstance();
                        map.put("featureName", curFeatureName);
                        map.put("featureId", curFeatureId);
                        productFeatureInfoList.add(map);
                    }
                    break;
                case 2:
                    for (int i = 0; i < productFeatureList.get(0).size(); i++) {
                        for (int j = 0; j < productFeatureList.get(1).size(); j++) {
                            String curFeatureName = productFeatureList.get(0).get(i) + productFeatureList.get(1).get(j);
                            String curFeatureId = productFeatureIdList.get(0).get(i) + "|" + productFeatureIdList.get(1).get(j);
                            map = FastMap.newInstance();
                            map.put("featureName", curFeatureName);
                            map.put("featureId", curFeatureId);
                            productFeatureInfoList.add(map);
                        }
                    }
                    break;
                case 3:
                    for (int i = 0; i < productFeatureList.get(0).size(); i++) {
                        for (int j = 0; j < productFeatureList.get(1).size(); j++) {
                            for (int k = 0; k < productFeatureList.get(2).size(); k++) {
                                String curFeatureName = productFeatureList.get(0).get(i) + productFeatureList.get(1).get(j) + productFeatureList.get(2).get(k);
                                String curFeatureId = productFeatureIdList.get(0).get(i) + "|" + productFeatureIdList.get(1).get(j) + "|" + productFeatureIdList.get(2).get(k);
                                map = FastMap.newInstance();
                                map.put("featureName", curFeatureName);
                                map.put("featureId", curFeatureId);
                                productFeatureInfoList.add(map);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        mapRtn = FastMap.newInstance();
        mapRtn.put("productFeatureInfoList", productFeatureInfoList);
        mapRtn.put("productFeatureInfoListSize", productFeatureInfoList.size());
        // 保存成功
        request.setAttribute("productFeatureInfo", mapRtn);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160121 根据选择商品特征获得得特征商品信息 End


    // Add by zhajh at 20160121 根据选择的特征商品特征商品信息 Begin

    /**
     * 根据选择商品特征获得得特征商品信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductFeatureGoodsByFeature(HttpServletRequest request, HttpServletResponse response) {
        // 特征商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String ids = request.getParameter("ids");
        String featureIds = request.getParameter("featureIds");
        String productId = request.getParameter("productId");
        List<String> productFeatureGoodsList = FastList.newInstance();
        List<String> productFeatureIdsList = FastList.newInstance();
        List<Map<String, String>> productFeatureNameList = FastList.newInstance();
        Map<String, String> map = FastMap.newInstance();
        Map<String, Object> mapRtn = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();

        java.text.DecimalFormat myformat = new java.text.DecimalFormat("0.00");

        orderBy.add("productId");
        // 取得特征商品信息
        if (UtilValidate.isNotEmpty(ids)) {
            String[] tProductFeatureGoodsArray = ids.split(",");
            for (String featureGoodsInfo : tProductFeatureGoodsArray) {
                productFeatureGoodsList.add(featureGoodsInfo);
            }
            // 确定组合特征ID
            if (UtilValidate.isNotEmpty(featureIds)) {
                String[] tProductFeatureIdsArray = featureIds.split(",");
                for (String featureIdsInfo : tProductFeatureIdsArray) {
                    productFeatureIdsList.add(featureIdsInfo);
                }
            }
        } else {
            try {
                List<EntityCondition> entityConditionList = FastList.newInstance();
                entityConditionList = FastList.newInstance();

                entityConditionList.add(EntityCondition.makeCondition("mainProductId", productId));
//        		entityConditionList.add(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null));
                List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                        EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));


                List<GenericValue> productNameList = delegator.findList("Product", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                for (GenericValue genericValue : productNameList) {
                    productFeatureGoodsList.add(genericValue.getString("featureProductName"));
                    productFeatureIdsList.add(genericValue.getString("featureProductId"));
                }
            } catch (GenericEntityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return "error";
            }
        }

        // 取得特征商品名称
//     	if(productFeatureGoodsList.size()>0){
//			for (String productFeatureName : productFeatureGoodsList) {
//				map=FastMap.newInstance();
//				map.put("productFeatureName", productFeatureName);
//				productFeatureNameList.add(map);
//			}
//     	}
        if (productFeatureGoodsList.size() > 0) {
            //if(productFeatureGoodsList.size()>0 && productFeatureIdsList.size()>0 &&productFeatureIdsList.size()==productFeatureGoodsList.size()){
            for (int i = 0; i < productFeatureGoodsList.size(); i++) {
                String curFeatureName = productFeatureGoodsList.get(i);
                String curFeatureId = "";
                if (productFeatureIdsList.size() > 0 && productFeatureIdsList.size() == productFeatureGoodsList.size()) {
                    curFeatureId = productFeatureIdsList.get(i);
                }
                map = FastMap.newInstance();
                map.put("productFeatureName", curFeatureName);
                map.put("productFeatureId", curFeatureId);
                productFeatureNameList.add(map);
            }
        }
        // 取得特征商品信息
        List<GenericValue> productList = FastList.newInstance();
        List<GenericValue> productPirceList = FastList.newInstance();
        List<Map<String, Object>> productFeatureGoodList = FastList.newInstance();
        Map<String, Object> productFeatureGoodItem = FastMap.newInstance();
        Map<String, Object> priceMap = FastMap.newInstance();

        if (UtilValidate.isNotEmpty(productId)) {
            try {
                productList = delegator.findList("Product", EntityCondition.makeCondition("mainProductId", productId), null, orderBy, null, true);
                orderBy = FastList.newInstance();
                orderBy.add("productPriceTypeId");
                for (GenericValue productInfo : productList) {
                    priceMap = FastMap.newInstance();
                    productFeatureGoodItem = FastMap.newInstance();
                    productPirceList = delegator.findList("ProductPrice", EntityCondition.makeCondition("productId", productInfo.get("productId")), null, orderBy, null, true);
                    for (GenericValue productPrice : productPirceList) {
                        priceMap.put(productPrice.getString("productPriceTypeId"), myformat.format(productPrice.getBigDecimal("price")));
                    }
                    productFeatureGoodItem.put("productInfo", productInfo);
                    productFeatureGoodItem.put("productPriceList", productPirceList);
                    productFeatureGoodItem.put("priceMap", priceMap);


                    // 取得特征商品的图片信息
                    String imageUrl = "";
                    String imageUrl2 = "";
                    String imageUrl3 = "";
                    String imageUrl4 = "";
                    String imageUrl5 = "";
                    List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productInfo.getString("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    List<GenericValue> curProductAdditionalImage2 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productInfo.getString("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_2"));
                    List<GenericValue> curProductAdditionalImage3 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productInfo.getString("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_3"));
                    List<GenericValue> curProductAdditionalImage4 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productInfo.getString("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_4"));
                    List<GenericValue> curProductAdditionalImage5 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productInfo.getString("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_5"));
                    if (curProductAdditionalImage1.size() > 0) {
                        imageUrl = ProductContentWrapper.getProductContentAsText(productInfo, "ADDITIONAL_IMAGE_1", request);
                    }
                    if (curProductAdditionalImage2.size() > 0) {
                        imageUrl2 = ProductContentWrapper.getProductContentAsText(productInfo, "ADDITIONAL_IMAGE_2", request);
                    }
                    if (curProductAdditionalImage3.size() > 0) {
                        imageUrl3 = ProductContentWrapper.getProductContentAsText(productInfo, "ADDITIONAL_IMAGE_3", request);
                    }
                    if (curProductAdditionalImage4.size() > 0) {
                        imageUrl4 = ProductContentWrapper.getProductContentAsText(productInfo, "ADDITIONAL_IMAGE_4", request);
                    }
                    if (curProductAdditionalImage5.size() > 0) {
                        imageUrl5 = ProductContentWrapper.getProductContentAsText(productInfo, "ADDITIONAL_IMAGE_5", request);
                    }

                    productFeatureGoodItem.put("imageUrl", imageUrl);
                    productFeatureGoodItem.put("imageUrl2", imageUrl2);
                    productFeatureGoodItem.put("imageUrl3", imageUrl3);
                    productFeatureGoodItem.put("imageUrl4", imageUrl4);
                    productFeatureGoodItem.put("imageUrl5", imageUrl5);
                    productFeatureGoodList.add(productFeatureGoodItem);
                }
            } catch (GenericEntityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "error";
            }
        }

        mapRtn = FastMap.newInstance();
        mapRtn.put("productFeatureNameList", productFeatureNameList);
        mapRtn.put("listSize", productFeatureNameList.size());
        mapRtn.put("productFeatureGoodList", productFeatureGoodList);
        // 保存成功
        request.setAttribute("productFeatureInfo", mapRtn);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160121 根据选择的特征商品特征商品信息


    // Add by zhajh at 20150125 查找审核商品 Begin

    /**
     * 查找审核商品
     *
     * @param dct
     * @param context
     * @return
     * @throws GenericEntityException
     * @throws NumberFormatException
     */
    public static Map<String, Object> findProductForVerifyIcoPro(DispatchContext dct, Map<String, ? extends Object> context) throws NumberFormatException, GenericEntityException {


        Delegator delegator = dct.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }

//        String orderFiled = (String) context.get("ORDER_FILED");
//        String orderFiledBy = (String) context.get("ORDER_BY");

        String orderFiled = (String) context.get("sortField");
        String orderFiledBy = (String) context.get("sortType");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        String productName = (String) context.get("productName");// 商品名称
        String productId = (String) context.get("productId");// 商品编号
        String brandId = (String) context.get("brandId");// 商品品牌
        String levelFirst = (String) context.get("levelFirst");// 一级分类
        String levelSecond = (String) context.get("levelSecond");// 二级分类
        String levelThird = (String) context.get("levelThird");// 三级分类
        String businessPartyId = (String) context.get("businessPartyId");// 商家名称
        String productStoreId = (String) context.get("productStoreIdForVerify"); // 店铺编码

        List<GenericValue> productForVerifyList = FastList.newInstance();
        int productForVerifyListSize = 0;
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
        // blank param list
        String paramList = "";

        DynamicViewEntity dynamicView = new DynamicViewEntity();

        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();

        // default view settings
        dynamicView.addMemberEntity("PR", "Product");
        dynamicView.addAliasAll("PR", "", null);
        dynamicView.addAlias("PR", "productName");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "isOnline");
        dynamicView.addAlias("PR", "primaryProductCategoryId");
        dynamicView.addAlias("PR", "productTypeId");
        dynamicView.addAlias("PR", "brandId");
        dynamicView.addAlias("PR", "businessPartyId");
        dynamicView.addAlias("PR", "isVerify");
        dynamicView.addAlias("PR", "createdStamp");
        dynamicView.addAlias("PR", "productStoreId");
        dynamicView.addAlias("PR", "platformClassId");

        
        dynamicView.addMemberEntity("PS", "ProductStore");
        dynamicView.addAlias("PS", "productStoreId");
        dynamicView.addAlias("PS", "storeName");
        dynamicView.addViewLink("PR", "PS", false, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));

        fieldsToSelect.add("productName");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("primaryProductCategoryId");
        fieldsToSelect.add("isOnline");
        fieldsToSelect.add("productTypeId");
        fieldsToSelect.add("brandId");
        fieldsToSelect.add("businessPartyId");
        fieldsToSelect.add("isVerify");
        fieldsToSelect.add("createdStamp");
        fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("storeName");
        fieldsToSelect.add("platformClassId");


        if (UtilValidate.isNotEmpty(orderFiled)) {
//            orderBy.add(orderFiled + " " + orderFiledBy);
            if ("desc".equals(orderFiledBy)) {
                orderBy.add("-" + orderFiled);
            } else {
                orderBy.add(orderFiled);
            }
        } else {
            orderBy.add("-createdStamp");
        }

        //查询
        // 商品名称
        if (UtilValidate.isNotEmpty(productName)) {
            paramList = paramList + "&productName=" + productName;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + productName + "%")));
        }
        // 商品编号
        if (UtilValidate.isNotEmpty(productId)) {
            paramList = paramList + "&productId=" + productId;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + productId + "%")));
        }
        // 商品品牌
        if (UtilValidate.isNotEmpty(brandId)) {
            paramList = paramList + "&brandId=" + brandId;
            andExprs.add(EntityCondition.makeCondition("brandId", brandId));
        }
        // 商家名称
        if (UtilValidate.isNotEmpty(businessPartyId)) {
            paramList = paramList + "&businessPartyId=" + businessPartyId;
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("businessPartyId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + businessPartyId + "%")));
        }

        // 一级分类
        if (UtilValidate.isNotEmpty(levelFirst)) {
            paramList = paramList + "&levelFirst=" + levelFirst;
        }
        // 二级分类
        if (UtilValidate.isNotEmpty(levelSecond)) {
            paramList = paramList + "&levelSecond=" + levelSecond;
        }
        // 三级分类
        if (UtilValidate.isNotEmpty(levelThird)) {
            paramList = paramList + "&levelThird=" + levelThird;
        }

        // 取得三级分类ID列表
        List<String> categoryIdList = getProductCategoryIdList(delegator, levelFirst, levelSecond, levelThird);
        if (categoryIdList.size() > 0) {
//            andExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, categoryIdList));
            andExprs.add(EntityCondition.makeCondition("platformClassId", EntityOperator.IN, categoryIdList));
        }

        // 店铺信息
        if (UtilValidate.isNotEmpty(productStoreId)) {
            paramList = paramList + "&productStoreIdForVerify=" + productStoreId;
            andExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        }

        // 商品为上架审核中
        andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
        // 审核状态为未审核
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, ""));
        andExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));
        // 商品为未删除
        List<EntityExpr> exprDels = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        andExprs.add(EntityCondition.makeCondition(exprDels, EntityOperator.OR));

        //andExprs.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, null));
        andExprs.add(EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, null));
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
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

                // get the partial list for this page
                productForVerifyList = pli.getPartialList(lowIndex, viewSize);

                // attempt to get the full size
                productForVerifyListSize = pli.getResultsSizeAfterPartialList();
                if (highIndex > productForVerifyListSize) {
                    highIndex = productForVerifyListSize;
                }

                // close the list iterator
                pli.close();
            } catch (GenericEntityException e) {
                String errMsg = "Failure in productBrand find operation, rolling back transaction: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "PartyLookupPartyError",
                        UtilMisc.toMap("errMessage", e.toString()), locale));
            }
        } else {
            productForVerifyListSize = 0;
        }
        result.put("productForVerifyList", productForVerifyList);
        result.put("productForVerifyListSize", Integer.valueOf(productForVerifyListSize));
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    // Add by zhajh at 20150125 查找审核商品 End


    // Add by zhajh at 20160125 商品的审核状态的更新处理 Begin

    /**
     * 商品的审核状态的更新处理ByIds
     *
     * @param request
     * @param response
     * @return
     */
    public static String updateProductIsVerifyStatus(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品记录 ids
        String ids = request.getParameter("checkedIds");
        String verifyStatus = request.getParameter("verifyStatus");
        String productIds = ""; // 已选择的商品ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                productIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品审核状态的更新处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        List<String> productIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(productIds)) {
            productIdsList = UtilMisc.toListArray(productIds.split(","));
        }

        // 根据条件取得商品的数据
        List<GenericValue> productInfoList = null;
        List<GenericValue> productSkuList = FastList.newInstance();
        EntityCondition condition = EntityCondition.makeCondition("productId", EntityOperator.IN, productIdsList);
        try {
            productInfoList = delegator.findList("Product", condition, null, null, null, false);

            // 更新处理
            List<String>  productIdRedisList=FastList.newInstance();
            if (UtilValidate.isNotEmpty(productInfoList)) {
                for (GenericValue productInfo : productInfoList) {
//            		if(verifyStatus.equals("Y")){
//            			productInfo.set("isVerify", "Y");
//            		}else if(verifyStatus.equals("N")){
//            			productInfo.set("isVerify", "N");
//            		}

                    productSkuList = delegator.findList("Product", EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, productInfo.getString("productId")), null, null, null, false);
                    if ("Y".equals(verifyStatus)) {
                        productInfo.set("isVerify", "Y");
                        if (UtilValidate.isNotEmpty(productSkuList)) {
                            productIdRedisList=FastList.newInstance();
                            for (GenericValue skuProductInfo : productSkuList) {
                                skuProductInfo.set("isVerify", "Y");
                                skuProductInfo.store();
                                String curProductId=skuProductInfo.getString("productId");
                                if(UtilValidate.isNotEmpty(curProductId)){
                                    productIdRedisList.add(curProductId);
                                }
                                // Redis 信息的删除处理
//                                if(UtilRedis.exists(skuProductInfo.getString("productId")+"_summary")){
//                                    UtilRedis.del(skuProductInfo.getString("productId")+"_summary");// 产品缓存
//                                }
//                                if(UtilRedis.exists(skuProductInfo.getString("productId")+"_downPromo")){
//                                    UtilRedis.del(skuProductInfo.getString("productId") + "_downPromo");// 产品直降信息
//                                }
//                                if(UtilRedis.exists(skuProductInfo.getString("productId")+"_groupOrder")){
//                                    UtilRedis.del(skuProductInfo.getString("productId") + "_groupOrder");// 产品团购信息
//                                }
//                                if(UtilRedis.exists(skuProductInfo.getString("productId")+"_seckill")) {
//                                    UtilRedis.del(skuProductInfo.getString("productId") + "_seckill"); // 产品秒杀信息
//                                }
                                //发送邮件
                                dispatcher.runAsync("productOnlineEmailSender",UtilMisc.toMap("productId",skuProductInfo.getString("productId")),null,false);
                            }
                            if(UtilValidate.isNotEmpty(productIdRedisList)){
                                String redisProductStr = Joiner.on(",").join(productIdRedisList);
                                Map<String, Object> reParams = FastMap.newInstance();
                                reParams.put("productIds", redisProductStr);
                                // TODO isDel 临时 N==>Y  原因是不能添加为上线的商品编码
                                reParams.put("isDel", "Y");
                                Map<String, Object> resultMap = FastMap.newInstance();
                                try {
                                    // 调用服务 productInfoRedisPro
                                    resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                                } catch (GenericServiceException e) {
                                    return "error";
                                }
                            }
                        }
                        productIdRedisList=FastList.newInstance();
                        String curProductId=productInfo.getString("productId");
                        if(UtilValidate.isNotEmpty(curProductId)){
                            productIdRedisList.add(curProductId);
                        }
                        // Redis 信息的删除处理
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_summary")){
//                            UtilRedis.del(productInfo.getString("productId")+"_summary");// 产品缓存
//                        }
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_downPromo")){
//                            UtilRedis.del(productInfo.getString("productId") + "_downPromo");// 产品直降信息
//                        }
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_groupOrder")){
//                            UtilRedis.del(productInfo.getString("productId") + "_groupOrder");// 产品团购信息
//                        }
//                        if(UtilRedis.exists(productInfo.getString("productId")+"_seckill")) {
//                            UtilRedis.del(productInfo.getString("productId") + "_seckill"); // 产品秒杀信息
//                        }
                    } else if ("N".equals(verifyStatus)) {
                        productInfo.set("isVerify", "N");
                        productInfo.set("isOnline", "N");
                        if (UtilValidate.isNotEmpty(productSkuList)) {
                            for (GenericValue skuProductInfo : productSkuList) {
                                skuProductInfo.set("isVerify", "N");
                                skuProductInfo.store();
                            }

                        }
                    }
                    productInfo.store();
                }
                if(UtilValidate.isNotEmpty(productIdRedisList)){
                    String redisProductStr = Joiner.on(",").join(productIdRedisList);
                    Map<String, Object> reParams = FastMap.newInstance();
                    reParams.put("productIds", redisProductStr);
                    // TODO isDel 临时 N==>Y  原因是不能添加为上线的商品编码
                    reParams.put("isDel", "Y");
                    Map<String, Object> resultMap = FastMap.newInstance();
                    try {
                        // 调用服务 productInfoRedisPro
                        resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                    } catch (GenericServiceException e) {
                        return "error";
                    }
                }
            }
        } catch (GenericEntityException e) {
            return "error";
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return "error";
        }

        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160125 商品审核状态的更新处理 End

    /**
     * 获取分类二级和三级 add by Wcy at 2016.01.26
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getProductCategory(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        LocalDispatcher dispatcher = dct.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        List<Map<String, Object>> level2Result = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        //分类升序排序
        orderBy.add("sequenceNum");
        try {
            //获取二级分类
            EntityConditionList<EntityExpr> Condition = EntityCondition.makeCondition(UtilMisc.toList(
                    EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, new Long(2)),
                    EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null)), EntityOperator.AND);
            List<GenericValue> level2List = delegator.findList("ProductCategory", Condition, null, orderBy, null, false);
            for (GenericValue level1 : level2List) {
                Map<String, Object> map = FastMap.newInstance();
                Map<String, Object> optionmap = FastMap.newInstance();
                optionmap.put("id", level1.getString("productCategoryId"));
                map.put("id", level1.getString("productCategoryId"));
                map.put("title", level1.getString("categoryName"));
                //获取三级分类
                try {
                    Map<String, Object> level3Result = dispatcher.runSync("getProductCategoryByLeveId", optionmap);
                    map.put("subClassify", level3Result.get("categoryList"));
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                }
                level2Result.add(map);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        result.put("resultData", level2Result);
        return result;
    }

    /**
     * 根据分类父编号获取列表信息
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getProductCategoryByLeveId(DispatchContext dct, Map<String, ? extends Object> context) {
        /** 获取调度器 */
        Delegator delegator = dct.getDelegator();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        /** 获取本地 */
        Locale locale = (Locale) context.get("locale");
        /** 获取参数 */
        String id = (String) context.get("id");
        /** 查询结果集 */
        List<GenericValue> categoryList = null;

        /** 定义分类动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PC", "ProductCategory");
        dynamicViewEntity.addAlias("PC", "id", "productCategoryId", null, null, null, null);
        dynamicViewEntity.addAlias("PC", "title", "categoryName", null, null, null, null);
        dynamicViewEntity.addAlias("PC", "productCategoryLevel");
        dynamicViewEntity.addAlias("PC", "primaryParentCategoryId");
        // add by gss
        dynamicViewEntity.addAlias("PC", "sequenceNum");

        /** 查询字段 & 排序字段 */
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("title");

        //排序号从小到大
        orderBy.add("sequenceNum");

        /** 查询条件 */
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        andExprs.add(EntityCondition.makeCondition("primaryParentCategoryId", id));


        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //获取分页结果集
            categoryList = pli.getCompleteList();

            //关闭迭代器
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "CategoryLookupCategoryError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }

        if (null == categoryList) {
            categoryList = FastList.newInstance();
        }

        try {
            List<Map<String, Object>> resultData = toListFormGenericValue(categoryList);
            result.put("categoryList", resultData);
        } catch (Exception e) {
            Debug.logError(e, module);
        }

        return result;
    }

    /**
     * 结果集转换为List add by wcy
     *
     * @param valueList
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> toListFormGenericValue(List<GenericValue> valueList) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (GenericValue v : valueList) {
            list.add(v.getAllFields());
        }
        return list;
    }


    // Add by zhajh at 20160128 取得关联商品表信息 Begin

    /**
     * 根据选择商品特征获得得特征商品信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductAssocGoodListByIds(HttpServletRequest request, HttpServletResponse response) {
        // 关联商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String ids = request.getParameter("ids");
        List<String> productIdList = FastList.newInstance();
        Map<String, Object> map = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        List<GenericValue> productFeatureList = FastList.newInstance();//特征商品列表
        List<GenericValue> productPirceList = FastList.newInstance();//价格信息
        List<Map<String, Object>> productAssocGoodList = FastList.newInstance();
        orderBy.add("productId");
        // 取得关联商品Id信息
        if (UtilValidate.isNotEmpty(ids)) {
            String[] tProductAssocGoodsArray = ids.split(",");
            for (String assocGoodsInfo : tProductAssocGoodsArray) {
                productIdList.add(assocGoodsInfo);
            }
        }
        // 取得关联商品信息
        if (productIdList.size() > 0) {
            try {
                for (String productId : productIdList) {
                    GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    BigDecimal salesPrice = new BigDecimal(0);//价格
                    String productGoodFeature = "";//商品特征
                    productPirceList = delegator.findList("ProductPrice", EntityCondition.makeCondition("productId", productInfo.get("productId")), null, orderBy, null, true);
                    for (GenericValue genericValue : productPirceList) {
                        if ("DEFAULT_PRICE".equals(genericValue.get("productPriceTypeId"))) {
                            salesPrice = genericValue.getBigDecimal("price");
                        }
                    }

                    if ("Y".equals(productInfo.getString("isUsedFeature"))) {
                        productFeatureList = delegator.findList("Product", EntityCondition.makeCondition("mainProductId", productId), null, orderBy, null, true);
                        for (GenericValue productFeatureInfo : productFeatureList) {
                            productGoodFeature = productGoodFeature + "," + productFeatureInfo.getString("featureProductName");
                        }
                    }
                    map = FastMap.newInstance();
                    map.put("productInfo", productInfo);
                    map.put("salesPrice", salesPrice);
                    map.put("productGoodFeature", productGoodFeature);
                    productAssocGoodList.add(map);
                }
            } catch (GenericEntityException e) {
                return "error";
            }
        }

        // 保存成功
        request.setAttribute("productAssocGoodList", productAssocGoodList);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160128 取得关联商品表信息


    // Add by zhajh at 20160130 取得该商品分类的商品件数 Begin

    /**
     * 取得该商品分类的商品件数
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductCountById(HttpServletRequest request, HttpServletResponse response) {
        // 关联商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String curProductCategoryId = request.getParameter("curProductCategoryId");
        List<GenericValue> productList = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        orderBy.add("productId");
        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();

        // 取得商品列表信息
        if (UtilValidate.isNotEmpty(curProductCategoryId)) {
            try {
                List<EntityCondition> entityConditionList = FastList.newInstance();
                entityConditionList = FastList.newInstance();

                entityConditionList.add(EntityCondition.makeCondition("primaryProductCategoryId", curProductCategoryId));
                entityConditionList.add(EntityCondition.makeCondition("isOnline", "Y"));
                entityConditionList.add(EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, null));

                List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                        EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));

                productList = delegator.findList("Product", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

            } catch (GenericEntityException e) {
                return "error";
            }
        }
        // 保存成功
        request.setAttribute("listSize", productList.size());
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160130 取得该商品分类的商品件数


    // Add by zhajh at 20160201  根据选择商品ID获得得关联商品信息 Begin

    /**
     * 根据选择商品ID获得得关联商品信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getAssocGoodListById(HttpServletRequest request, HttpServletResponse response) {
        // 关联商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String id = request.getParameter("id");
        List<String> productIdList = FastList.newInstance();
        Map<String, Object> map = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        List<GenericValue> productFeatureList = FastList.newInstance();//特征商品列表
        List<GenericValue> productPirceList = FastList.newInstance();//价格信息
        List<Map<String, Object>> productAssocGoodList = FastList.newInstance();
        List<GenericValue> productAssocList = FastList.newInstance();
        orderBy.add("productId");
        try {
            // 取得关联商品Id信息
            if (UtilValidate.isNotEmpty(id)) {
                productAssocList = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", id));
                for (GenericValue assocGoodsInfo : productAssocList) {
                    productIdList.add(assocGoodsInfo.getString("productIdTo"));
                }
            }
            // 取得关联商品信息
            if (productIdList.size() > 0) {
                for (String productId : productIdList) {
                    GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    BigDecimal salesPrice = new BigDecimal(0);//价格
                    String productGoodFeature = "";//商品特征
                    productPirceList = delegator.findList("ProductPrice", EntityCondition.makeCondition("productId", productInfo.get("productId")), null, orderBy, null, true);
                    for (GenericValue genericValue : productPirceList) {
                        if ("DEFAULT_PRICE".equals(genericValue.get("productPriceTypeId"))) {
                            salesPrice = genericValue.getBigDecimal("price");
                        }
                    }

                    if ("Y".equals(productInfo.getString("isUsedFeature"))) {
                        productFeatureList = delegator.findList("Product", EntityCondition.makeCondition("mainProductId", productId), null, orderBy, null, true);
                        for (GenericValue productFeatureInfo : productFeatureList) {
                            productGoodFeature = productGoodFeature + "," + productFeatureInfo.getString("featureProductName");
                        }
                    }
                    map = FastMap.newInstance();
                    map.put("productInfo", productInfo);
                    map.put("salesPrice", salesPrice);
                    map.put("productGoodFeature", productGoodFeature);
                    productAssocGoodList.add(map);
                }
            }
        } catch (Exception e) {
            return "error";
        }

        // 保存成功
        request.setAttribute("productAssocGoodList", productAssocGoodList);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160201  根据选择商品ID获得得关联商品信息


    // Add by zhajh at 20160203  根据选择商品ID获得商品图片信息 Begin

    /**
     * 根据选择商品ID获得商品图片信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductPicById(HttpServletRequest request, HttpServletResponse response) {
        // 商品图片信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String productId = request.getParameter("productId");
        List<GenericValue> curProductAdditionalImage1 = FastList.newInstance();
        List<GenericValue> curProductAdditionalImage2 = FastList.newInstance();
        List<GenericValue> curProductAdditionalImage3 = FastList.newInstance();
        List<GenericValue> curProductAdditionalImage4 = FastList.newInstance();
        List<GenericValue> curProductAdditionalImage5 = FastList.newInstance();

        GenericValue productGoodInfo = null;
        String productAdditionalImage1 = "";//图片1信息
        String productAdditionalImage2 = "";//图片2信息
        String productAdditionalImage3 = "";//图片3信息
        String productAdditionalImage4 = "";//图片4信息
        String productAdditionalImage5 = "";//图片5信息
        try {
            // 取得商品信息
            if (UtilValidate.isNotEmpty(productId)) {
                productGoodInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            }
            curProductAdditionalImage1 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
            curProductAdditionalImage2 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_2"));
            curProductAdditionalImage3 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_3"));
            curProductAdditionalImage4 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_4"));
            curProductAdditionalImage5 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_5"));
            // 取得商品图片信息
            if (UtilValidate.isNotEmpty(productGoodInfo)) {

                if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                    productAdditionalImage1 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_1", UtilHttp.getLocale(request), dispatcher);
                }
                if (UtilValidate.isNotEmpty(curProductAdditionalImage2)) {
                    productAdditionalImage2 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_2", UtilHttp.getLocale(request), dispatcher);
                }
                if (UtilValidate.isNotEmpty(curProductAdditionalImage3)) {
                    productAdditionalImage3 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_3", UtilHttp.getLocale(request), dispatcher);
                }
                if (UtilValidate.isNotEmpty(curProductAdditionalImage4)) {
                    productAdditionalImage4 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_4", UtilHttp.getLocale(request), dispatcher);
                }
                if (UtilValidate.isNotEmpty(curProductAdditionalImage5)) {
                    productAdditionalImage5 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_5", UtilHttp.getLocale(request), dispatcher);
                }
            }
        } catch (Exception e) {
            return "error";
        }

        // 保存成功
        request.setAttribute("productAdditionalImage1", productAdditionalImage1);
        request.setAttribute("productAdditionalImage2", productAdditionalImage2);
        request.setAttribute("productAdditionalImage3", productAdditionalImage3);
        request.setAttribute("productAdditionalImage4", productAdditionalImage4);
        request.setAttribute("productAdditionalImage5", productAdditionalImage5);

        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160202   根据选择商品ID获得商品图片信息

//    // Add by zhajh at 20160202 商品列表的导出 Begin
//    /**
//     * 商品列表的导出处理
//     * @param delegator
//     * @param payOnlineIds
//     * @return
//     * @throws org.ofbiz.entity.GenericEntityException
//     */
//    public static Map<String, Object> productGoodListReport (Delegator delegator,String ids) throws GenericEntityException {
//        Map<String,Object> mapTemp= new HashMap<String, Object>();
//        List<Map<String,Object>> productGoodList = new LinkedList<Map<String, Object>>();
//        Map<String,Object> mapResult= new HashMap<String, Object>();
//        List<GenericValue> productGoodInfoList = null ;
//        String productIds= ""; // 已选择的商品ID
//        EntityCondition condition=null;
//        List<String> productGoodIdsList= FastList.newInstance();
//        List<EntityCondition> andExprs = FastList.newInstance();
//        if(UtilValidate.isNotEmpty(ids)){
//        	// 选择商品记录 ids
//            if (UtilValidate.isNotEmpty(ids)){
    //String idsArray[] = ids.split(",");
    //String sessionIds = "";
    //for (String id : idsArray){
    //    if (!sessionIds.contains(id)){
    //        sessionIds = sessionIds + id + ",";
    //    }
    //}
    //if (UtilValidate.isNotEmpty(sessionIds)) {
    //	productIds=sessionIds.substring(0,sessionIds.length() - 1);
    //}
//            }
//            if(UtilValidate.isNotEmpty(productIds)){
//            	productGoodIdsList = UtilMisc.toListArray(productIds.split(","));
//            }
//            andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.IN,productGoodIdsList));
//        }
//
//        andExprs.add(EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, null));
//        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
    // EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
//        andExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));
//        condition =  EntityCondition.makeCondition(andExprs, EntityOperator.AND);
//        try {
//        	productGoodInfoList =delegator.findList("Product", condition, null ,null, null, false);
//        	// 取得商品数据处理
//            if (UtilValidate.isNotEmpty(productGoodInfoList)){
//            	for (GenericValue productGoodInfo : productGoodInfoList){
//            		mapTemp = new HashMap<String,Object>();
//
//            		mapTemp.put("productId",productGoodInfo.getString("productId")); //商品编号
//    			    mapTemp.put("productName",productGoodInfo.getString("productName"));//商品名称
//    			    mapTemp.put("productSubName",productGoodInfo.getString("productSubheadName"));//商品副标题
//
//    			    String primaryProductCategoryId=productGoodInfo.getString("primaryProductCategoryId");//三级分类
//    			    GenericValue productCategoryThird=delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId",primaryProductCategoryId));
//    			    GenericValue productCategorySecond=null;
//    			    GenericValue productCategoryFirst=null;
//    			    if(UtilValidate.isNotEmpty(productCategoryThird)){
//    			    	 productCategorySecond=delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId",productCategoryThird.get("primaryParentCategoryId")));
//    			    	 if(UtilValidate.isNotEmpty(productCategorySecond)){
//    			    	 mapTemp.put("levelSecond",productCategorySecond.getString("productCategoryId"));//二级分类
//    			    	 }
//    			    }
//    			    if(UtilValidate.isNotEmpty(productCategorySecond)){
//    			         productCategoryFirst=delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId",productCategorySecond.get("primaryParentCategoryId")));
//    			         if(UtilValidate.isNotEmpty(productCategoryFirst)){
//    			         mapTemp.put("levelFirst",productCategoryFirst.getString("productCategoryId"));//一级分类
//    			         }
//    			    }
//
//    			    if(UtilValidate.isNotEmpty(productCategoryThird)){
//    			    	mapTemp.put("levelThird",productCategoryThird.get("primaryParentCategoryId"));//三级分类
//    			    }
//
//
//    			    mapTemp.put("saleStartTime",productGoodInfo.getTimestamp("introductionDate"));//销售开始时间
//    			    mapTemp.put("saleEndTime",productGoodInfo.getString("salesDiscontinuationDate"));//销售结束时间
//    			    mapTemp.put("businessPartyId",productGoodInfo.getString("businessPartyId"));//商家名称
//
//    			    List<GenericValue> salePrices=delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId",productGoodInfo.getString("productId"),"productPriceTypeId","DEFAULT_PRICE"));
//    			    List<GenericValue> marketPrices=delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId",productGoodInfo.getString("productId"),"productPriceTypeId","MARKET_PRICE"));
//    			    List<GenericValue> costPrices=delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId",productGoodInfo.getString("productId"),"productPriceTypeId","COST_PRICE"));
//
//
//    			    String salePrice="0";
//    			    String marketPrice="0";
//    			    String costPrice="0";
//    			    if(salePrices.size()>0){
//    			    	salePrice=salePrices.get(0).getBigDecimal("price").toString();
//    			    }
//
//    			    if(marketPrices.size()>0){
//    			    	marketPrice=marketPrices.get(0).getBigDecimal("price").toString();
//    			    }
//    			    if(costPrices.size()>0){
//    			    	costPrice=costPrices.get(0).getBigDecimal("price").toString();
//    			    }
//    			    mapTemp.put("brandName",productGoodInfo.getString("brandId"));//商品品牌
//    			    mapTemp.put("salePrice",salePrice);//销售价格(元)
//    			    mapTemp.put("marketPrice",marketPrice);//市场价格(元)
//
//    			    mapTemp.put("costPrice",costPrice);//成本价格(元)
//    			    mapTemp.put("volume",productGoodInfo.getBigDecimal("volume").toString()); //体积(m3)
//    			    mapTemp.put("weight",productGoodInfo.getBigDecimal("weight").toString());//重量(kg)
//
//    			    productGoodList.add(mapTemp);
//            	}
//            }
//        } catch (GenericEntityException e) {
//            e.printStackTrace();
//        }
//        mapResult.put("productGoodList", productGoodList);
//        return mapResult;
//    }
//    // Add by zhajh at 20160202 商品列表的导出 End

    // Add by zhajh at 20160202 商品列表的导出 Begin

    /**
     * 商品列表的导出处理
     *
     * @param delegator
     * @param payOnlineIds
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> productGoodListReport(Delegator delegator, String ids) throws GenericEntityException {
        Map<String, Object> mapTemp = new HashMap<String, Object>();
        List<Map<String, Object>> productGoodList = new LinkedList<Map<String, Object>>();
        Map<String, Object> mapResult = new HashMap<String, Object>();
        List<GenericValue> productGoodInfoList = null;
        String productIds = ""; // 已选择的商品ID
        EntityCondition condition = null;
        List<String> productGoodIdsList = FastList.newInstance();
        List<EntityCondition> andExprs = FastList.newInstance();
        if (UtilValidate.isNotEmpty(ids)) {
            // 选择商品记录 ids
            if (UtilValidate.isNotEmpty(ids)) {
                String[] idsArray = ids.split(",");
                String sessionIds = "";
                for (String id : idsArray) {
                    if (!sessionIds.contains(id)) {
                        sessionIds = sessionIds + id + ",";
                    }
                }
                if (UtilValidate.isNotEmpty(sessionIds)) {
                    productIds = sessionIds.substring(0, sessionIds.length() - 1);
                }
            }
            if (UtilValidate.isNotEmpty(productIds)) {
                productGoodIdsList = UtilMisc.toListArray(productIds.split(","));
            }
            andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productGoodIdsList));
        }

        andExprs.add(EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, null));
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        andExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));
        condition = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        try {
            productGoodInfoList = delegator.findList("Product", condition, null, null, null, false);
            // 取得商品数据处理
            if (UtilValidate.isNotEmpty(productGoodInfoList)) {
                for (GenericValue productGoodInfo : productGoodInfoList) {
                    mapTemp = new HashMap<String, Object>();
                    if (UtilValidate.isNotEmpty(productGoodInfo)) {
                        mapTemp.put("productId", productGoodInfo.getString("productId")); //商品编号
                        mapTemp.put("productName", productGoodInfo.getString("productName"));//商品名称
                        mapTemp.put("productSubName", productGoodInfo.getString("productSubheadName"));//商品副标题

                        String primaryProductCategoryId = productGoodInfo.getString("primaryProductCategoryId");//三级分类
                        GenericValue productCategoryThird = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", primaryProductCategoryId));
                        GenericValue productCategorySecond = null;
                        GenericValue productCategoryFirst = null;
                        if (UtilValidate.isNotEmpty(productCategoryThird)) {
                            productCategorySecond = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryThird.get("primaryParentCategoryId")));
                            if (UtilValidate.isNotEmpty(productCategorySecond)) {
                                String secondName = (String) productCategorySecond.get("categoryName");
                                mapTemp.put("levelSecond", secondName);//二级分类
                            }
                        }
                        if (UtilValidate.isNotEmpty(productCategorySecond)) {
                            productCategoryFirst = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategorySecond.get("primaryParentCategoryId")));
                            if (UtilValidate.isNotEmpty(productCategoryFirst)) {
                                String firstName = (String) productCategoryFirst.get("categoryName");
                                mapTemp.put("levelFirst", firstName);//一级分类
                            }
                        }

                        if (UtilValidate.isNotEmpty(productCategoryThird)) {
                            String thirdName = (String) productCategoryThird.get("categoryName");
                            mapTemp.put("levelThird", thirdName);//三级分类
                        }


                        mapTemp.put("saleStartTime", productGoodInfo.getTimestamp("introductionDate"));//销售开始时间
                        mapTemp.put("saleEndTime", productGoodInfo.getString("salesDiscontinuationDate"));//销售结束时间
                        mapTemp.put("businessPartyId", productGoodInfo.getString("businessPartyId"));//商家名称

                        List<GenericValue> salePrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productGoodInfo.getString("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                        List<GenericValue> marketPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productGoodInfo.getString("productId"), "productPriceTypeId", "MARKET_PRICE"));
                        List<GenericValue> costPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productGoodInfo.getString("productId"), "productPriceTypeId", "COST_PRICE"));


                        String salePrice = "0";
                        String marketPrice = "0";
                        String costPrice = "0";
                        if (salePrices.size() > 0) {
                            salePrice = salePrices.get(0).getBigDecimal("price").toString();
                        }

                        if (marketPrices.size() > 0) {
                            marketPrice = marketPrices.get(0).getBigDecimal("price").toString();
                        }
                        if (costPrices.size() > 0) {
                            costPrice = costPrices.get(0).getBigDecimal("price").toString();
                        }
                        mapTemp.put("brandName", productGoodInfo.getString("brandId"));//商品品牌
                        mapTemp.put("salePrice", salePrice);//销售价格(元)
                        mapTemp.put("marketPrice", marketPrice);//市场价格(元)

                        mapTemp.put("costPrice", costPrice);//成本价格(元)

                        String volume = "0";
                        String weight = "0";
                        if (UtilValidate.isNotEmpty(productGoodInfo.getBigDecimal("volume"))) {
                            volume = productGoodInfo.getBigDecimal("volume").toString(); //体积(m3)
                        }
                        if (UtilValidate.isNotEmpty(productGoodInfo.getBigDecimal("weight"))) {
                            weight = productGoodInfo.getBigDecimal("weight").toString();//重量(kg)
                        }
                        mapTemp.put("volume", volume); //体积(m3)
                        mapTemp.put("weight", weight);//重量(kg)
                        productGoodList.add(mapTemp);
                    }
                }

            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        mapResult.put("productGoodList", productGoodList);
        return mapResult;
    }


    /**
     * 商品列表的导出处理
     *
     * @param delegator
     * @param payOnlineIds
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> productGoodListReport(Delegator delegator, String ids,String productStoreId) throws GenericEntityException {
        List<Map<String, Object>> productGoodList = new LinkedList<Map<String, Object>>();
        Map<String, Object> mapResult = new HashMap<String, Object>();
        List<GenericValue> productGoodInfoList = null;
//        String productIds = ""; // 已选择的商品ID
//        EntityCondition condition = null;
//        List<String> productGoodIdsList = FastList.newInstance();
//        List<EntityCondition> andExprs = FastList.newInstance();
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


//        if (UtilValidate.isNotEmpty(ids)) {
//            // 选择商品记录 ids
//            if (UtilValidate.isNotEmpty(ids)) {
//                String[] idsArray = ids.split(",");
//                String sessionIds = "";
//                for (String id : idsArray) {
//                    if (!sessionIds.contains(id)) {
//                        sessionIds = sessionIds + id + ",";
//                    }
//                }
//                if (UtilValidate.isNotEmpty(sessionIds)) {
//                    productIds = sessionIds.substring(0, sessionIds.length() - 1);
//                }
//            }
//            if (UtilValidate.isNotEmpty(productIds)) {
//                productGoodIdsList = UtilMisc.toListArray(productIds.split(","));
//            }
//            andExprs.add(EntityCondition.makeCondition("productId", EntityOperator.IN, productGoodIdsList));
//        }
//        andExprs.add(EntityCondition.makeCondition("mainProductId", EntityOperator.EQUALS, null));
//        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
//                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
//        andExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));
//        andExprs.add(EntityCondition.makeCondition("productStoreIdForPs", EntityOperator.EQUALS, productStoreId));
//        condition = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        try {
//            productGoodInfoList = delegator.findList("GetProductInfosByProductStoreIdView", condition, null, null, null, false);
//            // 取得商品数据处理
//            if (UtilValidate.isNotEmpty(productGoodInfoList)) {
//                for (GenericValue productGoodInfo : productGoodInfoList) {
//                    mapTemp = new HashMap<String, Object>();
//                    if (UtilValidate.isNotEmpty(productGoodInfo)) {
//                        mapTemp.put("productId", productGoodInfo.getString("productId")); //商品编号
//                        mapTemp.put("productName", productGoodInfo.getString("productName"));//商品名称
//                        mapTemp.put("productSubName", productGoodInfo.getString("productSubheadName"));//商品副标题
//
//                        String primaryProductCategoryId = productGoodInfo.getString("primaryProductCategoryId");//三级分类
//                        GenericValue productCategoryThird = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", primaryProductCategoryId));
//                        GenericValue productCategorySecond = null;
//                        GenericValue productCategoryFirst = null;
//                        if (UtilValidate.isNotEmpty(productCategoryThird)) {
//                            productCategorySecond = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryThird.get("primaryParentCategoryId")));
//                            if (UtilValidate.isNotEmpty(productCategorySecond)) {
//                                String secondName = (String) productCategorySecond.get("categoryName");
//                                mapTemp.put("levelSecond", secondName);//二级分类
//                            }
//                        }
//                        if (UtilValidate.isNotEmpty(productCategorySecond)) {
//                            productCategoryFirst = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategorySecond.get("primaryParentCategoryId")));
//                            if (UtilValidate.isNotEmpty(productCategoryFirst)) {
//                                String firstName = (String) productCategoryFirst.get("categoryName");
//                                mapTemp.put("levelFirst", firstName);//一级分类
//                            }
//                        }
//
//                        if (UtilValidate.isNotEmpty(productCategoryThird)) {
//                            String thirdName = (String) productCategoryThird.get("categoryName");
//                            mapTemp.put("levelThird", thirdName);//三级分类
//                        }
//
//                        String seleStartTime="";
//                        if(UtilValidate.isNotEmpty(productGoodInfo.getTimestamp("introductionDate"))){
//                            seleStartTime=sdf.format(productGoodInfo.getTimestamp("introductionDate"));
//                        }
//
//                        mapTemp.put("saleStartTime", seleStartTime);//销售开始时间
//                        String saleEndTime="";
//                        if(UtilValidate.isNotEmpty(productGoodInfo.getTimestamp("salesDiscontinuationDate"))){
//                            seleStartTime=sdf.format(productGoodInfo.getTimestamp("salesDiscontinuationDate"));
//                        }
//                        mapTemp.put("saleEndTime", seleStartTime);//销售结束时间
//                        mapTemp.put("businessPartyId", productGoodInfo.getString("businessPartyId"));//商家名称
//
//                        List<GenericValue> salePrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productGoodInfo.getString("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
//                        List<GenericValue> marketPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productGoodInfo.getString("productId"), "productPriceTypeId", "MARKET_PRICE"));
//                        List<GenericValue> costPrices = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productGoodInfo.getString("productId"), "productPriceTypeId", "COST_PRICE"));
//
//                        String salePrice = "0";
//                        String marketPrice = "0";
//                        String costPrice = "0";
//                        if (salePrices.size() > 0) {
//                            salePrice = salePrices.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
//                        }
//                        if (marketPrices.size() > 0) {
//                            marketPrice = marketPrices.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
//                        }
//                        if (costPrices.size() > 0) {
//                            costPrice = costPrices.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
//                        }
//                        mapTemp.put("brandName", productGoodInfo.getString("brandId"));//商品品牌
//                        mapTemp.put("salePrice", salePrice);//销售价格(元)
//                        mapTemp.put("marketPrice", marketPrice);//市场价格(元)
//
//                        mapTemp.put("costPrice", costPrice);//成本价格(元)
//
//                        String volume = "0";
//                        String weight = "0";
//                        if (UtilValidate.isNotEmpty(productGoodInfo.getBigDecimal("volume"))) {
//                            volume = productGoodInfo.getBigDecimal("volume").setScale(2, BigDecimal.ROUND_HALF_UP).toString(); //体积(m3)
//                        }
//                        if (UtilValidate.isNotEmpty(productGoodInfo.getBigDecimal("weight"))) {
//                            weight = productGoodInfo.getBigDecimal("weight").setScale(2, BigDecimal.ROUND_HALF_UP).toString();//重量(kg)
//                        }
//                        mapTemp.put("volume", volume); //体积(m3)
//                        mapTemp.put("weight", weight);//重量(kg)
//                        productGoodList.add(mapTemp);
//                    }
//                }
//
//            }
            String includeIdsExpr="";
            if(UtilValidate.isNotEmpty(ids)){
                includeIdsExpr="and t.product_id in ("+ids+")";
            }
            String sql = "SELECT\n" +
                    "t.product_id productId,t.product_name productName,t.product_subhead_name productSubName,t.introduction_date introductionDate,\n" +
                    "t.sales_Discontinuation_Date salesDiscontinuationDate,t.merchant_Name businessPartyId,t.brand_id brandName,\n" +
                    "t.volume ,t.weight,\n" +
                    "\ty.CATEGORY_NAME levelThird,\n" +
                    "\ty1.CATEGORY_NAME levelSecond,\n" +
                    "\ty2.CATEGORY_NAME levelFirst,\n" +
                    "\tp.PRICE defaultPrice1,\n" +
                    "\tp1.price marketPrice1,\n" +
                    "\tp2.price costPrice1\n" +
                    "FROM\n" +
                    "\tproduct t\n" +
                    "INNER JOIN PRODUCT_STORE_PRODUCT psp on psp.PRODUCT_ID =t.PRODUCT_ID\n" +
                    "LEFT JOIN PRODUCT_CATEGORY y ON t.PRIMARY_PRODUCT_CATEGORY_ID = y.PRODUCT_CATEGORY_ID\n" +
                    "LEFT JOIN product_category y1 ON y.PRIMARY_PARENT_CATEGORY_ID = y1.PRODUCT_CATEGORY_ID\n" +
                    "LEFT JOIN PRODUCT_CATEGORY y2 ON y1.PRIMARY_PARENT_CATEGORY_ID = y2.PRODUCT_CATEGORY_ID\n" +
                    "LEFT JOIN PRODUCT_PRICE p ON t.PRODUCT_ID = p.PRODUCT_ID AND p.PRODUCT_PRICE_TYPE_ID = 'DEFAULT_PRICE'\n" +
                    "LEFT JOIN PRODUCT_PRICE p1 ON t.PRODUCT_ID = p1.PRODUCT_ID AND p1.PRODUCT_PRICE_TYPE_ID = 'MARKET_PRICE'\n" +
                    "LEFT JOIN PRODUCT_PRICE p2 ON t.PRODUCT_ID = p2.PRODUCT_ID AND p2.PRODUCT_PRICE_TYPE_ID = 'COST_PRICE'\n" +
                    "where (t.is_del='N' OR t.is_del is null) and psp.PRODUCT_Store_ID='"+productStoreId+"' and t.main_product_Id is null\n" +includeIdsExpr;
            SQLProcessor sqlP = null;
            try {
                GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
                sqlP = new SQLProcessor(helperInfo);
                sqlP.executeQuery(sql);

                ResultSet rs = sqlP.getResultSet();
                while (rs.next()) {
                    Map tempMap = FastMap.newInstance();
                    tempMap.put("productId",rs.getString("productId"));
                    tempMap.put("productName",rs.getString("productName"));
                    tempMap.put("productSubName",rs.getString("productSubName"));
                    tempMap.put("levelThird",rs.getString("levelThird"));
                    tempMap.put("levelSecond",rs.getString("levelSecond"));
                    tempMap.put("levelFirst",rs.getString("levelFirst"));
                    tempMap.put("brandName",rs.getString("brandName"));
                    tempMap.put("businessPartyId",rs.getString("businessPartyId"));
                    String volume = "0";
                    String weight = "0";
                    if (UtilValidate.isNotEmpty(rs.getBigDecimal("volume"))) {
                        volume = rs.getBigDecimal("volume").setScale(2, BigDecimal.ROUND_HALF_UP).toString(); //体积(m3)
                    }
                    if (UtilValidate.isNotEmpty(rs.getBigDecimal("weight"))) {
                        weight = rs.getBigDecimal("weight").setScale(2, BigDecimal.ROUND_HALF_UP).toString();//重量(kg)
                    }
                    tempMap.put("volume", volume); //体积(m3)
                    tempMap.put("weight", weight);//重量(kg)

                    String salePrice = "0";
                    String marketPrice = "0";
                    String costPrice = "0";
                    if (UtilValidate.isNotEmpty(rs.getBigDecimal("defaultPrice1"))) {
                        salePrice = rs.getBigDecimal("defaultPrice1").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    }
                    if (UtilValidate.isNotEmpty(rs.getBigDecimal("marketPrice1"))) {
                        marketPrice = rs.getBigDecimal("marketPrice1").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    }
                    if (UtilValidate.isNotEmpty(rs.getBigDecimal("costPrice1"))) {
                        costPrice = rs.getBigDecimal("costPrice1").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    }
                    tempMap.put("salePrice", salePrice);//销售价格(元)
                    tempMap.put("marketPrice", marketPrice);//市场价格(元)
                    tempMap.put("costPrice", costPrice);//成本价格(元)

                    String seleStartTime="";
                    if(UtilValidate.isNotEmpty(rs.getTimestamp("introductionDate"))){
                        seleStartTime=sdf.format(rs.getTimestamp("introductionDate"));
                    }

                    tempMap.put("saleStartTime", seleStartTime);//销售开始时间
                    String saleEndTime="";
                    if(UtilValidate.isNotEmpty(rs.getTimestamp("salesDiscontinuationDate"))){
                        seleStartTime=sdf.format(rs.getTimestamp("salesDiscontinuationDate"));
                    }
                    tempMap.put("saleEndTime", seleStartTime);//销售结束时间
                    productGoodList.add(tempMap);
                }

            } catch (Exception e) {
                Debug.logError(e, "查询商品列表异常", module);

            } finally {
                sqlP.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mapResult.put("productGoodList", productGoodList);
        return mapResult;
    }
    // Add by zhajh at 20160202 商品列表的导出 End

    // Add by zhajh at 20160206 商品导入 Begin

    /**
     * 商品导入
     *

     * @return
     */
    public static void goodsImport(HttpServletRequest request, HttpServletResponse response) {
        //返回信息，json格式
        String returnJson = "";
        //当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        //Delegator对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> resultMap = FastMap.newInstance();
        try {
            //调用Excel导入方法
            Map rs = dispatcher.runSync("excelImport", UtilMisc.toMap(
                    "request", request,
                    "xmlUrl", "src/org/ofbiz/product/product/GoodValidate.xml",
                    "validateCellData", "goodValidateCell"));
            //获取导入的信息
            returnJson = rs.get("msg").toString();
            //获取导入的数据list
            @SuppressWarnings("unchecked")
            List<Map> listDatas = (List<Map>) rs.get("listDatas");
            //遍历list，进行新增或修改操作
            List<GenericValue> mrchantNameList = null;
            List<GenericValue> productCategoryLevelThirdList = null;
            for (Map record : listDatas) {
                mrchantNameList = FastList.newInstance();
                String productTypeId = "FINISHED_GOOD";//商品类型
                String isUsedFeature = "N";//是否使用特征
                String isOnline = "Y";//是否上下架
                String isBondedGoods="Y"; // 是否保税
                String supportServiceType="";// 服务支持
                String productName = (String) record.get("productName");//商品标题
                String productSubheadName = (String) record.get("productSubheadName");//商品描述
                String productCategoryId = null;//商品分类
                String productTags=""; // 标签
                String platformClassId="";// 主营分类
                String brandId="";// 品牌编码
                String salePrice = (String) record.get("salePrice");//销售价格(元)
                String marketPrice = (String) record.get("marketPrice");//市场价格(元)
                String costPrice = (String) record.get("costPrice");//成本价格(元)
                String volume = (String) record.get("volume");//体积(m3)
                String weight = (String) record.get("weight");//重量(kg)
                String integralDeductionType="";// 积分抵扣
                String integralDeductionUpper=(String) record.get("integralDeductionUpper");// 积分抵扣上限
                String purchaseLimitationQuantity=(String) record.get("purchaseLimitationQuantity"); //每人限购数量
                String isListShow="Y"; //列表展示
                String seoKeyword="";// 搜索关键字
                String productFacilityInfos="";// 仓库信息
                String productStoreId="";// 商家店铺编码
                String businessPartyId = (String) record.get("businessPartyId");//所属商家
                String providerName  = (String) record.get("providerName");// 供应商
                String providerId="";// 供应商编码
                String facilityId="";// 仓库编码
                String accountingQuantityTotal = (String) record.get("accountingQuantityTotal");//可用库存
                String warningQuantity = (String) record.get("warningQuantity");//库存预警数量
                String warningMail = (String) record.get("warningMail");//预警提示人邮箱
                String isInner="Y";// 自营Y 商家 N

                ///////////////////////////////////////////

                //所属商家(店铺)
                if (UtilValidate.isNotEmpty((String) record.get("businessPartyId"))) {
                    try {
                        if(!"10000".equals(businessPartyId)){
                            isInner="N";// 是否自营
                        }
                        List<GenericValue> marchantInfos = delegator.findByAnd("PartyGroup", UtilMisc.toMap("partyId", record.get("businessPartyId").toString().trim()));
                        if(UtilValidate.isNotEmpty(marchantInfos)){
                            if(UtilValidate.isNotEmpty(marchantInfos.get(0).getString("productStoreId"))){
                                productStoreId=marchantInfos.get(0).getString("productStoreId");
                            }
                        }
                    }catch (GenericEntityException e1) {
                        e1.printStackTrace();
                    }
                }
                //商品类型
                if (UtilValidate.isNotEmpty((String) record.get("productTypeName"))) {
                    if("虚拟商品".equals(record.get("productTypeName"))){
                        productTypeId="VIRTUAL_GOOD";
                    }
                }
                //商品分类
                if (UtilValidate.isNotEmpty((String) record.get("primaryProductCategoryId"))) {
                    String[] attrPc = record.get("primaryProductCategoryId").toString().split(",");
                    try {
                        productCategoryLevelThirdList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("categoryName", attrPc[2], "productCategoryLevel", new Long(3)));
                    } catch (GenericEntityException e1) {
                        e1.printStackTrace();
                    }
                    if (productCategoryLevelThirdList.size() > 0) {
                        productCategoryId = productCategoryLevelThirdList.get(0).getString("productCategoryId");
                    }
                }
                //商品标签
                if (UtilValidate.isNotEmpty((String) record.get("tagNames"))) {
                    String [] attrTagName=record.get("tagNames").toString().split(",");
                    try {
                        if (UtilValidate.isNotEmpty(attrTagName)) {
                            for (String tagName : attrTagName) {
                                List<GenericValue> tagInfos1 = delegator.findByAnd("Tag", UtilMisc.toMap("tagName", tagName, "tagTypeId", "ProdutTypeTag_1"));
                                if(UtilValidate.isNotEmpty(tagInfos1)){
                                    if(UtilValidate.isEmpty(productTags)){
                                        productTags=(String)tagInfos1.get(0).get("tagId");
                                    }else{
                                        productTags=productTags+","+(String)tagInfos1.get(0).get("tagId");
                                    }
                                }

                            }
                        }
                    }catch (GenericEntityException e1) {
                        e1.printStackTrace();
                    }
                }

                //是否申请上架
                if (UtilValidate.isNotEmpty((String) record.get("isOnline"))) {
                    if("否".equals(record.get("isOnline").toString().trim())){
                        isOnline="N";
                    }
                }
                //是否保税商品
                if (UtilValidate.isNotEmpty((String) record.get("isBondedGoods"))) {
                    if("否".equals(record.get("isBondedGoods").toString().trim())){
                        isBondedGoods="N";
                    }
                }
                //服务支持
                if (UtilValidate.isNotEmpty((String) record.get("supportServiceType"))) {
                    try{
                        List<GenericValue> supportServiceTypeList = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "SERVICE_SUPP_TYPE"));

                        String[] attrSeType = record.get("supportServiceType").toString().split(",");

                        for(String curStType:attrSeType){
                            String curSupportServiceType="";
                            for(GenericValue gv:supportServiceTypeList){
                                if(curStType.equals(gv.getString("description"))){
                                    curSupportServiceType=gv.getString("enumId");

                                }
                            }
                            if(UtilValidate.isNotEmpty(curSupportServiceType)) {
                                if (UtilValidate.isEmpty(supportServiceType)) {
                                    supportServiceType = curSupportServiceType;
                                } else {
                                    supportServiceType = curSupportServiceType + "," + curSupportServiceType;

                                }
                            }
                        }
                    }catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                }
                //销售开始时间
                Timestamp introductionDate = null;//销售开始时间
                if (UtilValidate.isNotEmpty((String) record.get("saleStartTime"))) {
                    introductionDate = Timestamp.valueOf((String) record.get("saleStartTime") + ":00");
                }

                //销售结束时间
                Timestamp salesDiscontinuationDate = null;//销售结束时间
                if (UtilValidate.isNotEmpty((String) record.get("saleEndTime"))) {
                    salesDiscontinuationDate = Timestamp.valueOf((String) record.get("saleEndTime") + ":00");
                }
                //主营分类
                if (UtilValidate.isNotEmpty((String) record.get("platformClassId"))) {
                    String[] attrPc = record.get("platformClassId").toString().split(",");
                    try {
                        productCategoryLevelThirdList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("categoryName", attrPc[2], "productCategoryLevel", new Long(3)));
                    } catch (GenericEntityException e1) {
                        e1.printStackTrace();
                    }
                    if (productCategoryLevelThirdList.size() > 0) {
                        platformClassId = productCategoryLevelThirdList.get(0).getString("productCategoryId");
                    }
                }
                //商品品牌
                if (UtilValidate.isNotEmpty((String) record.get("brandName"))) {
                    try {
                        List<GenericValue> brandInfos = FastList.newInstance();
                        if (UtilValidate.isNotEmpty(record.get("brandName"))) {
                            brandInfos = delegator.findByAnd("ProductBrand", UtilMisc.toMap("brandName", record.get("brandName").toString()));
                            if(UtilValidate.isNotEmpty(brandInfos)){
                                brandId=EntityUtil.getFirst(brandInfos).getString("productBrandId");
                            }
                        }
                    }catch (GenericEntityException e1) {
                        e1.printStackTrace();
                    }
                }

                //积分抵扣
                if (UtilValidate.isNotEmpty((String) record.get("integralDeductionType"))) {
                    String curIntegralDeductionType = record.get("integralDeductionType").toString().trim();
                    if("不使用积分抵扣".equals(curIntegralDeductionType)){
                        integralDeductionType="1";
                    }else if("百分比抵扣".equals(curIntegralDeductionType)){
                        integralDeductionType="2";
                    }else if("固定金额抵扣".equals(curIntegralDeductionType)){
                        integralDeductionType="3";
                    }
                }
                //列表展示
                if (UtilValidate.isNotEmpty((String) record.get("isListShow"))) {
                    if("否".equals(record.get("isListShow").toString().trim())){
                        isListShow="N";
                    }
                }
                //是否使用规格
                if (UtilValidate.isNotEmpty((String) record.get("isUsedFeature"))) {
                    if("是".equals(record.get("isUsedFeature").toString().trim())){
                        isUsedFeature="Y";
                    }
                }

                //仓库
                if(UtilValidate.isNotEmpty(productStoreId)){
                    List<GenericValue> facilityInfos=FastList.newInstance();
                    try {
                        facilityInfos = delegator.findByAnd("FacilityByPsId", UtilMisc.toMap("productStoreId", productStoreId));
                        if(UtilValidate.isNotEmpty(facilityInfos)){
                            facilityId=facilityInfos.get(0).getString("facilityId");
                        }
                        if(UtilValidate.isNotEmpty(facilityId)&&UtilValidate.isNotEmpty(accountingQuantityTotal)){
                            if(UtilValidate.isEmpty(warningQuantity)){
                                warningQuantity="0";
                            }
                            String inventoryItemId="";
                            productFacilityInfos=facilityId+"|"+inventoryItemId+"|"+accountingQuantityTotal+"|"+warningQuantity+"|"+warningMail;
                        }
                    }catch (GenericEntityException e1) {
                        e1.printStackTrace();
                    }
                }
                // 搜索关键字
                if (UtilValidate.isNotEmpty((String) record.get("seoKeyword"))) {
                    seoKeyword=record.get("seoKeyword").toString().trim();
                }
                //供应商
                if (UtilValidate.isNotEmpty((String) record.get("providerName"))) {
                    try {
                        List<GenericValue> providerInfos = FastList.newInstance();
                        if (UtilValidate.isNotEmpty(record.get("brandName"))) {
                            providerInfos = delegator.findByAnd("Provider", UtilMisc.toMap("providerName", record.get("providerName").toString()));
                            if(UtilValidate.isNotEmpty(providerInfos)){
                                providerId=EntityUtil.getFirst(providerInfos).getString("providerId");
                            }
                        }
                    }catch (GenericEntityException e1) {
                        e1.printStackTrace();
                    }
                }
                //代金券面额
                //使用开始时间
                //使用结束时间
                //////////////////////////////////////////
                Map<String, Object> productParams = FastMap.newInstance();
                // 商品类型
                if (UtilValidate.isNotEmpty(productTypeId)) {
                    productParams.put("productTypeId", productTypeId);
                }
                // 所属商家
                if (UtilValidate.isNotEmpty(businessPartyId)) {
                    productParams.put("businessPartyId", businessPartyId);
                }
                // 商品标题
                if (UtilValidate.isNotEmpty(productName)) {
                    productParams.put("productName", productName);
                }
                // 商品描述
                if (UtilValidate.isNotEmpty(productSubheadName)) {
                    productParams.put("productSubheadName", productSubheadName);
                }
                // 商品分类
                if (UtilValidate.isNotEmpty(productCategoryId)) {
                    productParams.put("productCategoryId", productCategoryId);
                }
                // 商品标签
                if (UtilValidate.isNotEmpty(productTags)) {
                    productParams.put("productTags", productTags);
                }
                // 是否申请上架
                if (UtilValidate.isNotEmpty(isOnline)) {
                    productParams.put("isOnline", isOnline);
                }
                // 是否保税商品
                if (UtilValidate.isNotEmpty(isBondedGoods)) {
                    productParams.put("isBondedGoods", isBondedGoods);
                }
                // 服务支持
                if(UtilValidate.isNotEmpty(supportServiceType)){
                    productParams.put("supportServiceType", supportServiceType);
                }
                //销售开始时间
                if (UtilValidate.isNotEmpty(introductionDate)) {
                    productParams.put("startTime", introductionDate);
                }
                //销售结束时间
                if (UtilValidate.isNotEmpty(salesDiscontinuationDate)) {
                    productParams.put("endTime", salesDiscontinuationDate);
                }
                // 主营分类
                if (UtilValidate.isNotEmpty(platformClassId)) {
                    productParams.put("platformClassId", platformClassId);
                }
                // 商品品牌
                if (UtilValidate.isNotEmpty(brandId)) {
                    productParams.put("brandId", brandId);
                }
                // 销售价格(元)
                if (UtilValidate.isNotEmpty(salePrice)) {
                    productParams.put("salePrice", salePrice);
                }
                if (UtilValidate.isNotEmpty(integralDeductionType)) {
                    productParams.put("integralDeductionType", integralDeductionType);
                }
                // 积分抵扣上限
                if (UtilValidate.isNotEmpty(integralDeductionUpper)) {
                    productParams.put("integralDeductionUpper", integralDeductionUpper);
                }
                // 每人限购数量
                if (UtilValidate.isNotEmpty(purchaseLimitationQuantity)) {
                    productParams.put("purchaseLimitationQuantity", purchaseLimitationQuantity);
                }
                // 列表展示
                if (UtilValidate.isNotEmpty(isListShow)) {
                    productParams.put("isListShow", isListShow);
                }
                // 市场价格(元)
                if (UtilValidate.isNotEmpty(marketPrice)) {
                    productParams.put("marketPrice", marketPrice);
                }
                // 成本价格(元)
                if (UtilValidate.isNotEmpty(costPrice)) {
                    productParams.put("costPrice", costPrice);
                }
                // 体积(m3)
                if (UtilValidate.isNotEmpty(volume)) {
                    productParams.put("volume", volume);
                }

                // 重量(kg)
                if (UtilValidate.isNotEmpty(weight)) {
                    productParams.put("weight", weight);
                }
                // 是否使用规格
                if (UtilValidate.isNotEmpty(isUsedFeature)) {
                    productParams.put("isUsedFeature", isUsedFeature);
                }
                // 库存信息
                if (UtilValidate.isNotEmpty(productFacilityInfos)) {
                    productParams.put("productFacilityInfos", productFacilityInfos);
                }
                // 搜索关键字
                if (UtilValidate.isNotEmpty(seoKeyword)) {
                    productParams.put("seoKeyword", seoKeyword);
                }
                // 供应商
                if (UtilValidate.isNotEmpty(providerId)) {
                    productParams.put("providerId", providerId);
                }
                //
                // 店铺编码
                if (UtilValidate.isNotEmpty(productStoreId)) {
                    productParams.put("productStoreId", productStoreId);
                }

                // 是否自营
                if(UtilValidate.isNotEmpty(isInner)){
                    productParams.put("isInner", isInner);
                }
                if (UtilValidate.isNotEmpty(productParams)) {
                    productParams.put("userLogin", userLogin);
                    productParams.put("operateType", "create");
                    resultMap = FastMap.newInstance();
                    resultMap = dispatcher.runSync("updateProductIcoPro", productParams);
                }
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        } finally {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.write(returnJson);
            out.flush();
            out.close();
        }
    }
    // Add by zhajh at 20160206 商品导入 End


    // Add by zhajh at 20160218  判断该商品是否是sku商品 Begin

    /**
     * 判断该商品是否是sku商品
     *
     * @param request
     * @param response
     * @return
     */
    public static String isSkuProduct(HttpServletRequest request, HttpServletResponse response) {
        // 特征商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String productId = request.getParameter("productId");
        Map<String, Object> map = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        String skuFlg = "";
        List<GenericValue> productFeatureList = FastList.newInstance();//特征商品列表
        orderBy.add("productId");
        try {
            // 取得特征商品信息
            if (UtilValidate.isNotEmpty(productId)) {
                productFeatureList = delegator.findList("Product", EntityCondition.makeCondition("mainProductId", productId), null, orderBy, null, true);
                if (productFeatureList.size() > 0) {
                    skuFlg = "Y";
                } else {
                    skuFlg = "N";
                }
            }

        } catch (Exception e) {
            return "error";
        }
        // 保存成功
        request.setAttribute("skuFlg", skuFlg);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160219 判断该商品是否是sku商品

    // Add by zhajh at 20160219 判断该商品品牌是否已使用 Begin

    /**
     * 判断该商品品牌是否已使用
     *
     * @param request
     * @param response
     * @return
     */
    public static String isBrandForProduct(HttpServletRequest request, HttpServletResponse response) {
        // 商品品牌信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String brandIds = "";
        String ids = request.getParameter("ids");
        Map<String, Object> map = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        String isUsedFlg = "N";
        List<GenericValue> productProductBrandList = FastList.newInstance();//商品列表
        orderBy.add("productId");

        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                brandIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }

        List<String> brandIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(brandIds)) {
            brandIdsList = UtilMisc.toListArray(brandIds.split(","));
        }
        List<EntityCondition> conditionList = FastList.newInstance();
        try {
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                    EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
            conditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            // 取得商品信息
            for (String brandId : brandIdsList) {
                conditionList.add(EntityCondition.makeCondition("brandId", brandId));
                productProductBrandList = delegator.findList("Product", EntityCondition.makeCondition(conditionList, EntityOperator.AND), null, orderBy, null, true);
                if (productProductBrandList.size() > 0) {
                    isUsedFlg = "Y";
                    break;
                }
            }

        } catch (Exception e) {
            return "error";
        }
        // 保存成功
        request.setAttribute("isUsedFlg", isUsedFlg);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160219  判断该商品品牌是否已使用  End


    // Add by zhajh at 20160220 判断该商品特征是否已使用 Begin

    /**
     * 判断该商品特征是否已使用
     *
     * @param request
     * @param response
     * @return
     */
    public static String isFeatureForProduct(HttpServletRequest request, HttpServletResponse response) {
        // 商品特征信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String featureIds = "";
        String featureValueIds = "";
        String featureTypeIds = request.getParameter("featureTypeIds");
        String featureTypeValueIds = request.getParameter("featureTypeValueIds");
        Map<String, Object> map = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        String isUsedFlg = "N";
        List<GenericValue> productProductFeatureList = FastList.newInstance();//特征列表
        List<GenericValue> productFeatureValueByTypeList = FastList.newInstance();// 指定特征类型下的特征值
        orderBy.add("productId");
        List<EntityCondition> conditionListForType = FastList.newInstance();
        List<EntityCondition> conditionListForValue = FastList.newInstance();
        if (UtilValidate.isNotEmpty(featureTypeIds)) {
            String[] idsArray = featureTypeIds.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                featureIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }


        if (UtilValidate.isNotEmpty(featureTypeValueIds)) {
            String[] idsArray = featureTypeValueIds.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                featureValueIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        List<String> featureIdsList = FastList.newInstance();

        if (UtilValidate.isNotEmpty(featureIds)) {
            featureIdsList = UtilMisc.toListArray(featureIds.split(","));
        }

        List<String> featureValueIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(featureValueIds)) {
            featureValueIdsList = UtilMisc.toListArray(featureValueIds.split(","));
        }

        try {
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                    EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));


            // 取得商品的特征信息
            if (featureIdsList.size() > 0) {
                conditionListForType.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                for (String featreId : featureIdsList) {
                    conditionListForType.add(EntityCondition.makeCondition("productFeatureTypeId", featreId));//特征类型
                    //productProductFeatureList = delegator.findByAnd("GetFeatureIdListForProductId",UtilMisc.toMap("productFeatureTypeId",featreId));
                    productProductFeatureList = delegator.findList("GetFeatureIdListForProductId", EntityCondition.makeCondition(conditionListForType, EntityOperator.AND), null, orderBy, null, true);
                    if (productProductFeatureList.size() > 0) {
                        isUsedFlg = "Y";
                        break;
                    }
                }
            }

            if (featureValueIdsList.size() > 0) {
                conditionListForValue.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                for (String featreValueId : featureValueIdsList) {
                    conditionListForValue.add(EntityCondition.makeCondition("productFeatureId", featreValueId));//特征值
                    productFeatureValueByTypeList = delegator.findList("GetFeatureViewForProductId", EntityCondition.makeCondition(conditionListForValue, EntityOperator.AND), null, orderBy, null, true);
                    if (productFeatureValueByTypeList.size() > 0) {
                        isUsedFlg = "Y";
                        break;
                    }
                }
            }


        } catch (Exception e) {
            return "error";
        }
        // 保存成功
        request.setAttribute("isUsedFlg", isUsedFlg);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160220 判断该商品特征是否已使用End


    // Add by zhajh at 20160101 商品特征值的删除处理 Begin

    /**
     * 商品特征值的删除处理ByIds
     *
     * @param request
     * @param response
     * @return
     */
    public static String delProductFeatureValueByIds(HttpServletRequest request, HttpServletResponse response) {
        // 选择商品特征记录 ids
        String ids = request.getParameter("checkedIds");
        List<EntityCondition> andExprs = FastList.newInstance();
        String productFeatureTypeId = request.getParameter("productFeatureTypeId");
        String featureValueIds = ""; // 已选择的品牌ID
        if (UtilValidate.isNotEmpty(ids)) {
            String[] idsArray = ids.split(",");
            String sessionIds = "";
            for (String id : idsArray) {
                if (!sessionIds.contains(id)) {
                    sessionIds = sessionIds + id + ",";
                }
            }
            if (UtilValidate.isNotEmpty(sessionIds)) {
                featureValueIds = sessionIds.substring(0, sessionIds.length() - 1);
            }
        }
        // 选择商品特征值的删除处理
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> featureValueIdsList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(featureValueIds)) {
            featureValueIdsList = UtilMisc.toListArray(featureValueIds.split(","));
        }
        // 根据条件取得商品特征值的数据
        List<GenericValue> productFeatureInfoList = null;
        EntityCondition condition = null;
        andExprs.add(EntityCondition.makeCondition("productFeatureId", EntityOperator.IN, featureValueIdsList));
        andExprs.add(EntityCondition.makeCondition("productFeatureTypeId", EntityOperator.EQUALS, productFeatureTypeId));
        condition = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        try {
            productFeatureInfoList = delegator.findList("ProductFeature", condition, null, null, null, false);
            // 删除处理
            if (UtilValidate.isNotEmpty(productFeatureInfoList)) {
                delegator.removeAll(productFeatureInfoList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        // 保存成功
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160101 商品特征值的删除处理 Begin


    // Add by zhajh at 20160221 商品分类扩展属性的是否被使用的检查处理 Begin

    /**
     * 商品分类扩展属性的是否被使用的检查处理
     *
     * @param request
     * @param response
     * @return
     */
    public static String isProductExtendAttrForProduct(HttpServletRequest request, HttpServletResponse response) {
        // 产品分类Id
        String productCategoryId = request.getParameter("productCategoryId");//产品分类Id
        String attrName = request.getParameter("attrName");//商品分类扩展属性名Id

        List<EntityCondition> entityConditionList = FastList.newInstance();
        entityConditionList = FastList.newInstance();
        entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
        entityConditionList.add(EntityCondition.makeCondition("attrName", attrName));
        // 选择商品分类扩展属性 的检查处理
        // 根据条件取得商品分类的扩展属性的数据
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productCategoryAttributeList = FastList.newInstance();
        String isUsedFlg = "N";
        try {
            productCategoryAttributeList = delegator.findList("ProductCategoryattributeAssoc", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
            if (productCategoryAttributeList.size() > 0) {
                isUsedFlg = "Y";
            }
        } catch (GenericEntityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            request.setAttribute("success", false);
            request.setAttribute("resultFlg", false);
            return "error";
        }
        // 保存成功
        request.setAttribute("isUsedFlg", isUsedFlg);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160221  商品分类扩展属性的是否被使用的检查处理 End

    // Add by zhajh at 20160221 商品分类扩展属性的操作选项是否被使用的检查处理 Begin

    /**
     * 商品分类扩展属性的操作选项是否被使用的检查处理
     *
     * @param request
     * @param response
     * @return
     */
    public static String isProductExtendAttrOptionForProduct(HttpServletRequest request, HttpServletResponse response) {
        // 产品分类Id
        String productCategoryId = request.getParameter("productCategoryId");//产品分类Id
        String attrName = request.getParameter("attrName");//商品分类扩展属性名
        String productOptionId = request.getParameter("productOptionId");//商品分类扩展属性操作属性Id

        List<EntityCondition> entityConditionList = FastList.newInstance();
        entityConditionList = FastList.newInstance();
        entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
        entityConditionList.add(EntityCondition.makeCondition("attrName", attrName));
        entityConditionList.add(EntityCondition.makeCondition("productOptionId", productOptionId));
        // 选择商品分类扩展属性的操作选项 的检查处理
        // 根据条件取得商品分类的扩展属性的操作选项数据
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productCategoryAttributeOptionList = FastList.newInstance();
        String isUsedFlg = "N";
        try {
            productCategoryAttributeOptionList = delegator.findList("ProductCategoryattributeAssoc", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
            if (productCategoryAttributeOptionList.size() > 0) {
                isUsedFlg = "Y";
            }
        } catch (GenericEntityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            request.setAttribute("success", false);
            request.setAttribute("resultFlg", false);
            return "error";
        }
        // 保存成功
        request.setAttribute("isUsedFlg", isUsedFlg);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160221  商品分类扩展属性的操作选项是否被使用的检查处理  End


    // Add by zhajh at 20160222 商品仓库是否被使用的检查处理 Begin

    /**
     * 商品仓库是否被使用的检查处理
     *
     * @param request
     * @param response
     * @return
     */
    public static String isFacilityForProduct(HttpServletRequest request, HttpServletResponse response) {
        // 仓库Id
        String facilityId = request.getParameter("facilityId");//仓库Id
        String productId = request.getParameter("productId");//产品Id

        List<EntityCondition> entityConditionList = FastList.newInstance();
        entityConditionList = FastList.newInstance();
        entityConditionList.add(EntityCondition.makeCondition("facilityId", facilityId));
        entityConditionList.add(EntityCondition.makeCondition("isVerify", "Y"));
        if (UtilValidate.isNotEmpty(productId)) {
            entityConditionList.add(EntityCondition.makeCondition("productId", productId));
        }

        // 商品仓库的检查处理
        // 根据条件取得商品仓库的数据
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productFacilityList = FastList.newInstance();
        List<GenericValue> productForOrderItemList = FastList.newInstance();
        String isUsedFlg = "N";

        try {
            if (UtilValidate.isNotEmpty(productId) && UtilValidate.isNotEmpty(facilityId)) {
                productFacilityList = delegator.findList("GetFacilityList", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);
                //编辑取得锁定库存数量的条件
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productId", productId));
                entityConditionList.add(EntityCondition.makeCondition("statusId", "ITEM_WAITSHIP"));
                productForOrderItemList = delegator.findList("OrderItemLockQuantity", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, true);

                if (productForOrderItemList.size() > 0) {
                    BigDecimal alreadyLock = productForOrderItemList.get(0).getBigDecimal("alreadyLockQuantitySum");
                    // 锁定库存大于0的场合
                    if (alreadyLock.compareTo(BigDecimal.ZERO) == 1) {
                        isUsedFlg = "Y";
                    }
                } else {
                    if (productFacilityList.size() > 0) {
                        isUsedFlg = "Y";
                    }
                }
            }
        } catch (GenericEntityException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            request.setAttribute("success", false);
            request.setAttribute("resultFlg", "false");
            return "error";
        }
        // 保存成功
        request.setAttribute("isUsedFlg", isUsedFlg);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160222  商品仓库是否被使用的检查处理  End


    // Add by zhajh at 20160223 添加准备商品处理 Begin

    /**
     * 添加准备商品处理
     *
     * @param request
     * @param response
     * @return
     */
    public static String setPrepareProductEntity(HttpServletRequest request, HttpServletResponse response) {
        // 参数
        String productTypeId = request.getParameter("productTypeId");//商品类型
        String productCategoryId = request.getParameter("productCategoryId");//商品分类Id
        String isFeatureGoods = request.getParameter("isFeatureGoods");//是否是特征商品
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String productIdCreate = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
//         	if(isFeatureGoods.equals("N")){
// 	             // 清除失效的准备商品
// 	        	 List<GenericValue>  productForPrepareList=delegator.findByAnd("Product", UtilMisc.toMap("isPrepareEntity","Y","createdByUserLogin",userLogin.getString("userLoginId")));
// 	        	 for (GenericValue genericValue : productForPrepareList) {
// 					String curProductId=genericValue.getString("productId");
// 					// ProductKeyword
// 					List<GenericValue> productKeywordNewList=delegator.findList("ProductKeyword", EntityCondition.makeCondition("productId", EntityOperator.EQUALS,curProductId), null ,null, null, false);
// 					if(productKeywordNewList.size()>0){
// 						delegator.removeAll(productKeywordNewList);
//
// 					}
// 					//ProductContent
// 					List<GenericValue> productContentList=delegator.findList("ProductContent", EntityCondition.makeCondition("productId", EntityOperator.EQUALS,curProductId), null ,null, null, false);
// 					if(productContentList.size()>0){
// 						delegator.removeAll(productContentList);
// 					}
// 					genericValue.remove();
// 	        	 }
//         	 }
            // 创建新商品准备实体
            productIdCreate = delegator.getNextSeqId("Product");//准备商品ID
            GenericValue product = delegator.makeValue("Product", UtilMisc.toMap("productId", productIdCreate));
            //商品类型
            if (UtilValidate.isNotEmpty(productTypeId)) {
                product.set("productTypeId", productTypeId);
            }
            //商品分类ID
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                product.set("primaryProductCategoryId", productCategoryId);
            }
            
            // 登陆用户
            product.set("createdByUserLogin", userLogin.getString("userLoginId"));

            // 添加准备商品
            product.create();
            if (UtilValidate.isNotEmpty(productIdCreate)) {
                Map productContext = UtilMisc.toMap("productId", productIdCreate);
                try {
                    dispatcher.schedule("autoCancelPrepareProductEntity", "pool", "autoCancelPrepareProductEntity", productContext,
                            System.currentTimeMillis() + 30 * 60 * 1000, RecurrenceRule.HOURLY, 1, 1, System.currentTimeMillis() + 35 * 60 * 1000, -1);
                } catch (GenericServiceException e) {
                    return "error";
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return "error";
        }
        // 保存成功
        request.setAttribute("productId", productIdCreate);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160222  添加准备商品处理  End


    // Add by zhajh at 20160311 添加准备商品处理(新建) Begin

    /**
     * 添加准备商品处理(新建)
     *
     * @param request
     * @param response
     * @return
     */
    public static String setPrepareProductEntityForCreate(HttpServletRequest request, HttpServletResponse response) {
        // 参数
        String productTypeId = request.getParameter("productTypeId");//商品类型
        String productCategoryId = request.getParameter("productCategoryId");//商品分类Id
        String isFeatureGoods = request.getParameter("isFeatureGoods");//是否是特征商品
        int listSize = Integer.valueOf(request.getParameter("listSize"));//特征件数
        Map<String, Object> featureGoodCreateInfo = FastMap.newInstance();//新建特征准备商品信息
        List<Map<String, Object>> featureGoodCreateInfoList = FastList.newInstance();//新建特征准备商品列表信息
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String productIdCreate = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        for (int i = 0; i < listSize; i++) {
            featureGoodCreateInfo = FastMap.newInstance();
            try {
                // 创建新商品准备实体
                productIdCreate = delegator.getNextSeqId("Product");//准备商品ID
                GenericValue product = delegator.makeValue("Product", UtilMisc.toMap("productId", productIdCreate));
                //商品类型
                if (UtilValidate.isNotEmpty(productTypeId)) {
                    product.set("productTypeId", productTypeId);
                }
                //商品分类ID
                if (UtilValidate.isNotEmpty(productCategoryId)) {
                    product.set("primaryProductCategoryId", productCategoryId);
                }
                
                // 登陆用户
                product.set("createdByUserLogin", userLogin.getString("userLoginId"));

                // 添加准备商品
                product.create();
                if (UtilValidate.isNotEmpty(productIdCreate)) {
                    Map productContext = UtilMisc.toMap("productId", productIdCreate);
                    try {
                        dispatcher.schedule("autoCancelPrepareProductEntity", "pool", "autoCancelPrepareProductEntity", productContext,
                                System.currentTimeMillis() + 30 * 60 * 1000, RecurrenceRule.HOURLY, 1, 1, System.currentTimeMillis() + 35 * 60 * 1000, -1);
                    } catch (GenericServiceException e) {
                        return "error";
                    }
                }
                featureGoodCreateInfo.put("indexId", i);
                featureGoodCreateInfo.put("productId", productIdCreate);
                featureGoodCreateInfoList.add(featureGoodCreateInfo);
            } catch (GenericEntityException e) {
                e.printStackTrace();
                return "error";
            }

        }

        // 保存成功
        request.setAttribute("featureGoodCreateInfoList", featureGoodCreateInfoList);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20160311  添加准备商品处理  End


    // Add by zhajh at 20160225  删除准备商品处理  Begin

    


    // Add by zhajh at 20160310 删除商品图片 Begin

    /**
     * 删除产品内容的图片
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public static String delProductContentImg(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String productId = request.getParameter("productId");
        String productContentTypeId = request.getParameter("productContentTypeId");
        // 取得productContentId的序号
        String typeNo = productContentTypeId.substring(productContentTypeId.length() - 1, productContentTypeId.length());
        // 编辑需要删除的productContentTypeId
        String additional_image_no = "ADDITIONAL_IMAGE_" + typeNo;
        String xtra_img_no_small = "XTRA_IMG_" + typeNo + "_SMALL";
        String xtra_img_no_medium = "XTRA_IMG_" + typeNo + "_MEDIUM";
        String xtra_img_no_large = "XTRA_IMG_" + typeNo + "_LARGE";
        String xtra_img_no_detail = "XTRA_IMG_" + typeNo + "_DETAIL";
        // 存入待删除ProductContentId存入列表
        List<String> delImgs = FastList.newInstance();
        delImgs.add(additional_image_no);
        delImgs.add(xtra_img_no_small);
        delImgs.add(xtra_img_no_medium);
        delImgs.add(xtra_img_no_large);
        delImgs.add(xtra_img_no_detail);
        // 编辑条件
        List<EntityExpr> exprs = new ArrayList<EntityExpr>();
        exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, UtilMisc.toList(productId)));
        exprs.add(EntityCondition.makeCondition("productContentTypeId", EntityOperator.IN, delImgs));
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productContentList = delegator.findList("ProductContent", EntityCondition.makeCondition(exprs, EntityOperator.AND), null, null, null, false);
        delegator.removeAll(productContentList);

        // 取得其他图片域是否有图片
        GenericValue productGoodInfo = null;
        Map<String, Object> productFeatureGoodItem = FastMap.newInstance();
        // 取得商品信息
        if (UtilValidate.isNotEmpty(productId)) {
            productGoodInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        }
        // 取得特征商品的图片信息
        String imageUrl1 = "";
        String imageUrl2 = "";
        String imageUrl3 = "";
        String imageUrl4 = "";
        String imageUrl5 = "";
        List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
        List<GenericValue> curProductAdditionalImage2 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_2"));
        List<GenericValue> curProductAdditionalImage3 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_3"));
        List<GenericValue> curProductAdditionalImage4 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_4"));
        List<GenericValue> curProductAdditionalImage5 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_5"));
        if (curProductAdditionalImage1.size() > 0) {
            imageUrl1 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_1", request);
        }
        if (curProductAdditionalImage2.size() > 0) {
            imageUrl2 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_2", request);
        }
        if (curProductAdditionalImage3.size() > 0) {
            imageUrl3 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_3", request);
        }
        if (curProductAdditionalImage4.size() > 0) {
            imageUrl4 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_4", request);
        }
        if (curProductAdditionalImage5.size() > 0) {
            imageUrl5 = ProductContentWrapper.getProductContentAsText(productGoodInfo, "ADDITIONAL_IMAGE_5", request);
        }
        productFeatureGoodItem.put("imageUrl1", imageUrl1);
        productFeatureGoodItem.put("imageUrl2", imageUrl2);
        productFeatureGoodItem.put("imageUrl3", imageUrl3);
        productFeatureGoodItem.put("imageUrl4", imageUrl4);
        productFeatureGoodItem.put("imageUrl5", imageUrl5);
        productFeatureGoodItem.put("typeNo", typeNo);

        request.setAttribute("productFeatureGoodItem", productFeatureGoodItem);
        return "success";
    }
    // Add by zhajh at 20160310 删除商品图片 End


    // Add by zhajh at 20160331  功能优化：产品编辑页面添加商品分类（更新的场合取得商品品牌和商品属性信息） Begin

    /**
     * 产品编辑页面添加商品分类（更新的场合取得商品品牌和商品属性信息）
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public static String getProductCategoryInfoById(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String curProductCategoryId = request.getParameter("curProductCategoryId");
//           String productId = request.getParameter("productId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        // 取得商品分类对应的商品品牌信息
        List<GenericValue> productBrandList = delegator.findByAnd("ProductBrandCategory", UtilMisc.toMap("productCategoryId", curProductCategoryId));
//           GenericValue productInfo=delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId",productId));
        List<GenericValue> brandList = FastList.newInstance();
        for (GenericValue productBrand : productBrandList) {
            GenericValue productBrandInfo = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", productBrand.getString("productBrandId")));
            if (UtilValidate.isNotEmpty(productBrandInfo)) {
                if ("Y".equals(productBrandInfo.getString("isUsed"))) {
                    brandList.add(productBrandInfo);
                }
            }
        }
        // 取得商品分类对应的商品属性信息
        List<GenericValue> productCategoryAttributeList = delegator.findByAnd("ProductCategoryAttribute", UtilMisc.toMap("productCategoryId", curProductCategoryId));
        int listSize = productCategoryAttributeList.size();
        int aa = listSize / 3;
        int bb = listSize % 3;
        int cc = 0;
        if (bb > 0) {
            cc = aa + 1;
        }
        Map<String, Object> mapTemp = new HashMap<String, Object>();
        List<Map<String, Object>> productOptionInfos = FastList.newInstance();
        List<Map<String, Object>> optionList = FastList.newInstance();
        Map<String, Object> mapOption = FastMap.newInstance();
        for (int i = 0; i < productCategoryAttributeList.size(); i++) {
            mapTemp = FastMap.newInstance();
            GenericValue productCategoryAttributeInfo = productCategoryAttributeList.get(i);
            mapTemp.put("attrName", productCategoryAttributeInfo.getString("attrName"));// 属性名
            mapTemp.put("productCategoryId", productCategoryAttributeInfo.getString("productCategoryId")); // 分类ID
            mapTemp.put("isRequired", productCategoryAttributeInfo.getString("isRequired"));// 是否必输
//			   List<GenericValue> ProductCategoryattributeAssocList=delegator.findByAnd("ProductCategoryattributeAssoc", UtilMisc.toMap("productId",productId,"productCategoryId",productCategoryAttributeInfo.getString("productCategoryId"),"attrName",productCategoryAttributeInfo.getString("attrName")));
            List<GenericValue> productOptionList = delegator.findByAnd("ProductOption", UtilMisc.toMap("productCategoryId", productCategoryAttributeInfo.getString("productCategoryId"), "attrName", productCategoryAttributeInfo.getString("attrName")));
            optionList = FastList.newInstance();
            for (GenericValue productOption : productOptionList) {
                mapOption = FastMap.newInstance();
//				   GenericValue curOptionName=EntityUtil.getFirst(ProductCategoryattributeAssocList);
//				   mapOption.put("curOptionName", curOptionName);
                mapOption.put("productOptionId", productOption.getString("productOptionId"));
                mapOption.put("optionName", productOption.getString("optionName"));
                optionList.add(mapOption);
            }
            mapTemp.put("optionList", optionList);
            productOptionInfos.add(mapTemp);
        }
        request.setAttribute("brandList", brandList);
//           request.setAttribute("productCategoryAttributeList", productCategoryAttributeList);
        request.setAttribute("listSize", listSize);
        request.setAttribute("aa", aa);
        request.setAttribute("bb", bb);
        request.setAttribute("cc", cc);
        request.setAttribute("productOptionInfos", productOptionInfos);
//           request.setAttribute("productInfo", productInfo);
        return "success";
    }
    // Add by zhajh at 20160331  功能优化：产品编辑页面添加商品分类（更新的场合取得商品品牌和商品属性信息 End


    // Add by zhajh at 20180308 根据特征类型ID取得特征信息 Begin

    /**
     * 根据商品ID取得特征信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductFeatureTypeInfosByIds(HttpServletRequest request, HttpServletResponse response) {
        // 特征类型ID
        String productFeatureTypeIds = request.getParameter("productFeatureTypeIds");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> productFeatureTypeIdList = FastList.newInstance();
        List<GenericValue> productFeatureTypeList = FastList.newInstance();
        GenericValue productFeatureTypeInfo = null;
        List<Map<String, Object>> mapList = FastList.newInstance();
        try {
            if (UtilValidate.isNotEmpty(productFeatureTypeIds)) {
                String[] atrrProductFeatureTypeId = productFeatureTypeIds.split(",");
                for (String productFeatureTypeId : atrrProductFeatureTypeId) {
                    productFeatureTypeInfo = null;
                    productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeId));
                    if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                        Map<String, Object> map = FastMap.newInstance();
                        List<GenericValue> productFeatureList = FastList.newInstance();
                        List<String> orderBy = FastList.newInstance();
                        orderBy.add("sequenceNum");

                        int trCount = 0;
                        int listSize = 0;
                        // 取得商品特征信息
                        productFeatureList = delegator.findByAnd("ProductFeature", UtilMisc.toMap("productFeatureTypeId", productFeatureTypeId), orderBy);
                        listSize = productFeatureList.size();
                        double aa = listSize / 3;
                        int bb = listSize % 3;
                        int cc = 0;
                        if (bb > 0) {
                            trCount = (int) aa + 1;
                        } else {
                            trCount = (int) aa;
                        }

                        map.put("productFeatureList", productFeatureList);
                        map.put("trCount", trCount);
                        map.put("listSize", listSize);
                        map.put("productFeatureTypeInfo", productFeatureTypeInfo);

                        mapList.add(map);
                    }
                }
            }
        } catch (Exception e) {
            return "error";
        }
        // 保存成功
        request.setAttribute("resultFlg", "true");
        request.setAttribute("mapList", mapList);
        return "success";

    }
    // Add by zhajh at 20180308 根据特征类型ID取得特征信息


    // Add by zhajh at 20180314 根据商品编码取得特征信息 Begin

    /**
     * 根据商品ID取得特征信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductFeatureInfosByProductId(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        // 商品编码
        String productId = request.getParameter("productId");
        String strFeatureInfo = "";

        // 根据商品编码取得特征类型ID信息
        // 取得该商品信息
        try {
            if (UtilValidate.isNotEmpty(productId)) {
                GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                if (UtilValidate.isNotEmpty(productInfo)) {
                    if (UtilValidate.isNotEmpty(productInfo.getString("mainProductId"))) {
                        if (UtilValidate.isNotEmpty(productInfo.getString("featureaProductId"))) {
                            GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productInfo.getString("featureaProductId")));
                            if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                                if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                    String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                    String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                    strFeatureInfo = productFeatureTypeName + ":" + productFeatureName;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "error";
        }
        // 保存成功
        request.setAttribute("resultFlg", "true");
        request.setAttribute("strFeatureInfo", strFeatureInfo);
        return "success";
    }
    // Add by zhajh at 20180314 根据商品编码取得特征信息 Begin


    // Add by zhajh at 20180315 根据商品编码取得组合商品信息 Begin

    /**
     * 根据选择商品编码取得商品信息列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getProductGoodsListByIds(HttpServletRequest request, HttpServletResponse response) {
        // 关联商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String ids = request.getParameter("ids");
        List<String> productIdList = FastList.newInstance();
        Map<String, Object> map = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        List<GenericValue> productFeatureList = FastList.newInstance();//特征商品列表
        List<GenericValue> productPirceList = FastList.newInstance();//价格信息
        List<Map<String, Object>> productGoodInfoList = FastList.newInstance();//商品信息列表


        orderBy.add("productId");
        // 取得商品Id信息列表
        if (UtilValidate.isNotEmpty(ids)) {
            String[] tProductGoodsArray = ids.split(",");
            for (String productGoodsInfo : tProductGoodsArray) {
                productIdList.add(productGoodsInfo);
            }
        }
        // 取得关联商品信息
        if (productIdList.size() > 0) {
            try {
                for (String productId : productIdList) {
                    GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    BigDecimal salesPrice = new BigDecimal(0);//价格
                    String productGoodFeature = "";//商品特征
                    productPirceList = delegator.findList("ProductPrice", EntityCondition.makeCondition("productId", productInfo.get("productId")), null, orderBy, null, true);
                    for (GenericValue genericValue : productPirceList) {
                        if ("DEFAULT_PRICE".equals(genericValue.get("productPriceTypeId"))) {
                            salesPrice = genericValue.getBigDecimal("price");
                        }
                    }

                    // 商品特征
                    if (UtilValidate.isNotEmpty(productInfo)) {
                        if (UtilValidate.isNotEmpty(productInfo.getString("mainProductId"))) {
                            if (UtilValidate.isNotEmpty(productInfo.getString("featureaProductId"))) {
                                GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productInfo.getString("featureaProductId")));
                                if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                                    GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatureInfo.getString("productFeatureTypeId")));
                                    if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                        String productFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                        String productFeatureName = productFeatureInfo.getString("productFeatureName");
                                        productGoodFeature = productFeatureTypeName + ":" + productFeatureName;
                                    }
                                }
                            }
                        }
                    }

                    // 商品图片
                    String imgUrl = "";// 商品图片
                    // 根据商品ID获取商品图片url
                    String productAdditionalImage1 = "";
                    List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd("ProductContent",
                            UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                        imgUrl = "/content/control/getImage?contentId=" + curProductAdditionalImage1.get(0).get("contentId");
                    }
                    map = FastMap.newInstance();
                    map.put("productInfo", productInfo);
                    map.put("salesPrice", salesPrice);
                    map.put("productGoodFeature", productGoodFeature);
                    map.put("imgUrl", imgUrl);
                    productGoodInfoList.add(map);
                }
            } catch (GenericEntityException e) {
                return "error";
            }
        }

        // 保存成功
        request.setAttribute("productGoodInfoList", productGoodInfoList);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20180315 根据商品编码取得组合商品信息 End


    // Add by zhajh at 20180508 根据商品编码取得商品特征信息 Begin

    /**
     * 根据商品ID取得特征信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getFeatureInfoByProductId(HttpServletRequest request, HttpServletResponse response) {
        // 商品ID
        String productId = request.getParameter("productId");
        Map<String, Object> map = FastMap.newInstance();
        Map<String, String> typeMap = FastMap.newInstance();
        List<Map<String, Object>> featureInfoList = new LinkedList<Map<String, Object>>();
        // 取得商品的库存信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String curProductTypeIdTemp = "";
        try {
            // 取得商品特征关系信息
            List<GenericValue> productFeatureAssocList = delegator.findByAnd("ProductFeatureAssoc", UtilMisc.toMap("productId", productId));
            // 取得商品特征信息
            String curProductId = "";
            String curProductFeatureId = "";
            String curProductFeatureTypeId = "";
            String curProductFeatureTypeName = "";
            String curfeatureIds = "";

            for (GenericValue genericValue : productFeatureAssocList) {
                map = FastMap.newInstance();
                curProductId = "";
                curProductFeatureId = "";
                curProductFeatureTypeId = "";
                curProductFeatureTypeName = "";
                curfeatureIds = "";
                if (UtilValidate.isNotEmpty(genericValue)) {
                    curProductId = genericValue.getString("productId");
                    curProductFeatureId = genericValue.getString("productFeatureId");
                    if (UtilValidate.isNotEmpty(curProductFeatureId)) {
                        GenericValue productFeatureInfo = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", curProductFeatureId));
                        if (UtilValidate.isNotEmpty(productFeatureInfo)) {
                            curProductFeatureTypeId = productFeatureInfo.getString("productFeatureTypeId");
                            if (UtilValidate.isEmpty(typeMap.get(curProductFeatureTypeId))) {
                                typeMap.put(curProductFeatureTypeId, curProductFeatureTypeId);
                                if (UtilValidate.isNotEmpty(curProductTypeIdTemp)) {
                                    curProductTypeIdTemp = curProductFeatureTypeId;
                                } else {
                                    curProductTypeIdTemp = curProductTypeIdTemp + "," + curProductFeatureTypeId;
                                }
                                if (UtilValidate.isNotEmpty(curProductFeatureTypeId)) {
                                    GenericValue productFeatureTypeInfo = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", curProductFeatureTypeId));
                                    if (UtilValidate.isNotEmpty(productFeatureTypeInfo)) {
                                        curProductFeatureTypeName = productFeatureTypeInfo.getString("productFeatureTypeName");
                                    }
                                    List<GenericValue> featureList = delegator.findByAnd("GetFeatureIdList", UtilMisc.toMap("productId", productId, "productFeatureTypeId", curProductFeatureTypeId));
                                    if (UtilValidate.isNotEmpty(featureList)) {
                                        for (GenericValue gv : featureList) {
                                            if (UtilValidate.isEmpty(curfeatureIds)) {
                                                curfeatureIds = gv.getString("productFeatureId");
                                            } else {
                                                curfeatureIds = curfeatureIds + "," + gv.getString("productFeatureId");
                                            }
                                        }
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                }
                map.put("productId", curProductId);
                map.put("productFeatureId", curProductFeatureId);
                map.put("productFeatureTypeId", curProductFeatureTypeId);
                map.put("productFeatureTypeName", curProductFeatureTypeName);
                map.put("featureIds", curfeatureIds);
                featureInfoList.add(map);
            }
        } catch (Exception e) {
            return "error";
        }
        // 保存成功
        request.setAttribute("featureInfoList", featureInfoList);
        request.setAttribute("productTypeIdTemp", curProductTypeIdTemp);
        request.setAttribute("resultFlg", "true");
        return "success";
    }
    // Add by zhajh at 20180508 根据商品编码取得商品特征信息 End

    public Map<String,Object> countProductQuantityOrdered(DispatchContext dcx,Map<String,? extends Object> context){
        Map<String,Object> resultData = ServiceUtil.returnSuccess();
        String productId = (String)context.get("productId");
        BigDecimal quantity = (BigDecimal)context.get("quantity");
        Delegator delegator = dcx.getDelegator();
        try {
            GenericValue info = delegator.findByPrimaryKey("ProductCalculatedInfo",UtilMisc.toMap("productId",productId));
            if(UtilValidate.isNotEmpty(info)){
                BigDecimal totalQuantityOrdered = info.getBigDecimal("totalQuantityOrdered");
                info.set("totalQuantityOrdered",totalQuantityOrdered.add(quantity));
                info.store();
            }else{

                delegator.create(delegator.makeValue("ProductCalculatedInfo",UtilMisc.toMap("productId",productId,"totalQuantityOrdered",quantity)));
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return resultData;
    }



    /// 商家主营分类

    // Add by zhajh at 20160105 根据级别商品类型列表初始化处理 Begin

    /**
     * 商品等级分类列表的取得
     *

     * @return
     */
    public static String getInitProductCategoryByLevelForPlatformClassByPsId(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();

        List<GenericValue> productCategoryLevel1List = FastList.newInstance();
        List<GenericValue> productCategoryLevel2List = FastList.newInstance();
        List<GenericValue> productCategoryLevel3List = FastList.newInstance();
        GenericValue productCategoryLevel1Info = null;
        GenericValue productCategoryLevel2Info = null;
        GenericValue productCategoryLevel3Info = null;

        String isInner = (String) request.getParameter("isInner");
        String productStoreId = (String) request.getParameter("productStoreId");
        String productCategoryName="ProductCategory";
        orderBy.add("sequenceNum");

        entityConditionList = FastList.newInstance();
        entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));

        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
        if(UtilValidate.isNotEmpty(isInner)){
            if("N".equals(isInner)){
                productCategoryName="ProductCategoryForPlatformClassByPsId";
                entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
            }
        }

//        delegator.findList()
        productCategoryLevel1List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, false);

        //productCategoryLevel1List = delegator.findList("ProductCategory", EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")), null, orderBy, null, true);

        if (productCategoryLevel1List.size() > 0) {
            productCategoryLevel1Info = productCategoryLevel1List.get(0);
            entityConditionList = FastList.newInstance();
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel1Info.getString("productCategoryId")));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            if(UtilValidate.isNotEmpty(isInner)){
                if("N".equals(isInner)){
                    productCategoryName="ProductCategoryForPlatformClassByPsId";
                    entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                }
            }
            productCategoryLevel2List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, false);

            if (productCategoryLevel2List.size() > 0) {
                productCategoryLevel2Info = productCategoryLevel2List.get(0);
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(isInner)){
                    if("N".equals(isInner)){
                        productCategoryName="ProductCategoryForPlatformClassByPsId";
                        entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                    }
                }
                productCategoryLevel3List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, false);
                if (productCategoryLevel3List.size() > 0) {
                    productCategoryLevel3Info = productCategoryLevel3List.get(0);
                }
            }
        }
        request.setAttribute("productCategoryLevel1List", productCategoryLevel1List);
        request.setAttribute("productCategoryLevel2List", productCategoryLevel2List);
        request.setAttribute("productCategoryLevel3List", productCategoryLevel3List);
        request.setAttribute("productCategoryLevel1Info", productCategoryLevel1Info);
        request.setAttribute("productCategoryLevel2Info", productCategoryLevel2Info);
        request.setAttribute("productCategoryLevel3Info", productCategoryLevel3Info);
        return "success";
    }
    // Add by zhajh at 20160105 根据级别商品类型列表初始化处理 End


    // Add by zhajh at 20160105 商品等级分类列表的根据名称查询处理 Begin

    /**
     * 商品等级分类列表的根据名称查询处理
     *

     * @return
     */
    public static String searchProductCategoryLevelByNameForPlatformClassByPsId(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        String productCategoryLevel = (String) request.getParameter("productCategoryLevel");
        String parentCategoryId = (String) request.getParameter("productCategoryId");
        String categoryName = (String) request.getParameter("categoryName");

        String isInner = (String) request.getParameter("isInner");
        String productStoreId = (String) request.getParameter("productStoreId");
        String productCategoryName="ProductCategory";

        List<GenericValue> productCategoryLevel1List = FastList.newInstance();
        List<GenericValue> productCategoryLevel2List = FastList.newInstance();
        List<GenericValue> productCategoryLevel3List = FastList.newInstance();
        GenericValue productCategoryLevel1Info = null;
        GenericValue productCategoryLevel2Info = null;
        GenericValue productCategoryLevel3Info = null;
        orderBy.add("sequenceNum");

        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        if(UtilValidate.isNotEmpty(isInner)){
            if("N".equals(isInner)){
                productCategoryName="ProductCategoryForPlatformClassByPsId";
                entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
            }
        }

        if ("1".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));
            entityConditionList.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("categoryName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + categoryName + "%")));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel1List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel1List.size() > 0) {
                productCategoryLevel1Info = productCategoryLevel1List.get(0);

                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel1Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(isInner)){
                    if("N".equals(isInner)){
                        entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                    }
                }
                productCategoryLevel2List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

                if (productCategoryLevel2List.size() > 0) {
                    productCategoryLevel2Info = productCategoryLevel2List.get(0);
                    entityConditionList = FastList.newInstance();
                    entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                    entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                    entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                    if(UtilValidate.isNotEmpty(isInner)){
                        if("N".equals(isInner)){
                            entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                        }
                    }
                    productCategoryLevel3List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                    if (productCategoryLevel3List.size() > 0) {
                        productCategoryLevel3Info = productCategoryLevel3List.get(0);
                    }
                }
            }
        } else if ("2".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            entityConditionList.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("categoryName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + categoryName + "%")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", parentCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel2List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel2List.size() > 0) {
                productCategoryLevel2Info = productCategoryLevel2List.get(0);

                productCategoryLevel2Info = productCategoryLevel2List.get(0);
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(isInner)){
                    if("N".equals(isInner)){
                        entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                    }
                }
                productCategoryLevel3List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                if (productCategoryLevel3List.size() > 0) {
                    productCategoryLevel3Info = productCategoryLevel3List.get(0);
                }
            }
        } else if ("3".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
            entityConditionList.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("categoryName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + categoryName + "%")));
            entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", parentCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel3List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel3List.size() > 0) {
                productCategoryLevel3Info = productCategoryLevel3List.get(0);
            }
        }

        request.setAttribute("productCategoryLevel1List", productCategoryLevel1List);
        request.setAttribute("productCategoryLevel2List", productCategoryLevel2List);
        request.setAttribute("productCategoryLevel3List", productCategoryLevel3List);
        request.setAttribute("productCategoryLevel1Info", productCategoryLevel1Info);
        request.setAttribute("productCategoryLevel2Info", productCategoryLevel2Info);
        request.setAttribute("productCategoryLevel3Info", productCategoryLevel3Info);

        request.setAttribute("productCategoryLevel", productCategoryLevel);
        request.setAttribute("success", true);

        return "success";
    }
    // Add by zhajh at 20160105 根据级别商品类型列表初始化处理 End


    // Add by zhajh at 20160106 商品等级分类列表的根据ID查询处理 Begin

    /**
     * 商品等级分类列表的根据ID查询处理
     *

     * @return
     */
    public static String seclectdeItemProductCategoryLevelByIdForPlatformClassByPsId(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        String productCategoryLevel = (String) request.getParameter("productCategoryLevel");
        String productCategoryId = (String) request.getParameter("productCategoryId");
        String isInner = (String) request.getParameter("isInner");
        String productStoreId = (String) request.getParameter("productStoreId");
        String productCategoryName="ProductCategory";


        List<GenericValue> productCategoryLevel1List = FastList.newInstance();
        List<GenericValue> productCategoryLevel2List = FastList.newInstance();
        List<GenericValue> productCategoryLevel3List = FastList.newInstance();
        GenericValue productCategoryLevel1Info = null;
        GenericValue productCategoryLevel2Info = null;
        GenericValue productCategoryLevel3Info = null;
        orderBy.add("sequenceNum");
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        if(UtilValidate.isNotEmpty(isInner)){
            if("N".equals(isInner)){
                productCategoryName="ProductCategoryForPlatformClassByPsId";
                entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
            }
        }
        if ("1".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));
            entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel1List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel1List.size() > 0) {
                productCategoryLevel1Info = productCategoryLevel1List.get(0);

                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel1Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(isInner)){
                    if("N".equals(isInner)){
                        entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                    }
                }

                productCategoryLevel2List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);

                if (productCategoryLevel2List.size() > 0) {
                    productCategoryLevel2Info = productCategoryLevel2List.get(0);
                    entityConditionList = FastList.newInstance();
                    entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                    entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                    entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                    if(UtilValidate.isNotEmpty(isInner)){
                        if("N".equals(isInner)){
                            entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                        }
                    }
                    productCategoryLevel3List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                    if (productCategoryLevel3List.size() > 0) {
                        productCategoryLevel3Info = productCategoryLevel3List.get(0);
                    }
                }
            }
        } else if ("2".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("2")));
            entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel2List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel2List.size() > 0) {
                productCategoryLevel2Info = productCategoryLevel2List.get(0);

                productCategoryLevel2Info = productCategoryLevel2List.get(0);
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if(UtilValidate.isNotEmpty(isInner)){
                    if("N".equals(isInner)){
                        entityConditionList.add(EntityCondition.makeCondition("productStoreIdForPs", productStoreId));
                    }
                }
                productCategoryLevel3List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                if (productCategoryLevel3List.size() > 0) {
                    productCategoryLevel3Info = productCategoryLevel3List.get(0);
                }
            }
        } else if ("3".equals(productCategoryLevel)) {
            entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
            entityConditionList.add(EntityCondition.makeCondition("productCategoryId", productCategoryId));
            entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            productCategoryLevel3List = delegator.findList(productCategoryName, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            if (productCategoryLevel3List.size() > 0) {
                productCategoryLevel3Info = productCategoryLevel3List.get(0);
            }
        }

        request.setAttribute("productCategoryLevel1List", productCategoryLevel1List);
        request.setAttribute("productCategoryLevel2List", productCategoryLevel2List);
        request.setAttribute("productCategoryLevel3List", productCategoryLevel3List);
        request.setAttribute("productCategoryLevel1Info", productCategoryLevel1Info);
        request.setAttribute("productCategoryLevel2Info", productCategoryLevel2Info);
        request.setAttribute("productCategoryLevel3Info", productCategoryLevel3Info);

        if (UtilValidate.isNotEmpty(productCategoryLevel1Info)) {
            request.setAttribute("productCategoryIdLevel1", productCategoryLevel1Info.getString("productCategoryId"));
        }
        if (UtilValidate.isNotEmpty(productCategoryLevel2Info)) {
            request.setAttribute("productCategoryIdLevel2", productCategoryLevel2Info.getString("productCategoryId"));
        }
        if (UtilValidate.isNotEmpty(productCategoryLevel3Info)) {
            request.setAttribute("productCategoryIdLevel3", productCategoryLevel3Info.getString("productCategoryId"));
        }
        request.setAttribute("productCategoryLevel", productCategoryLevel);
        request.setAttribute("success", true);

        return "success";
    }
    // Add by zhajh at 20160105 商品等级分类列表的根据ID查询处理 End




    /**
     * 根据商品编码导出商品信息列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String exportProductGoodsByIds(HttpServletRequest request, HttpServletResponse response)throws GenericEntityException  {
        // 关联商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String ids = request.getParameter("ids");
        String productStoreId = request.getParameter("productStoreId");
        Map<String,Object> goodsMap=FastMap.newInstance();
        List<Map<String,Object>> productGoodList= FastList.newInstance();
        List<Object[]> recordsList = FastList.newInstance();
        //  取得商品信息列表
        goodsMap=productGoodListReport(delegator,ids,productStoreId);
        if(UtilValidate.isNotEmpty(goodsMap)){
            productGoodList=(List<Map<String,Object>>)goodsMap.get("productGoodList");
            if(UtilValidate.isNotEmpty(productGoodList)){
                Object[] obj = null;
                for(int i=0;i<productGoodList.size();i++){
                    Map<String,Object> pgMap=productGoodList.get(i);
                    obj = new Object[16];
                    String productId = (String)pgMap.get("productId");
                    String productName = (String)pgMap.get("productName");
                    String productSubName="";
                    if(UtilValidate.isNotEmpty(pgMap.get("productSubName"))) {
                        productSubName = (String) pgMap.get("productSubName");
                    }

                    String levelFirst = (String)pgMap.get("levelFirst");
                    String levelSecond = (String)pgMap.get("levelSecond");
                    String levelThird = (String)pgMap.get("levelThird");
                    String saleStartTime = "";
                    if(UtilValidate.isNotEmpty(pgMap.get("saleStartTime"))) {
                        saleStartTime = (String) pgMap.get("saleStartTime");
                    }

                    String saleEndTime = "";
                    if(UtilValidate.isNotEmpty(pgMap.get("saleEndTime"))) {
                        saleEndTime = (String) pgMap.get("saleEndTime");
                    }
                    String businessPartyId = "";
                    if(UtilValidate.isNotEmpty(pgMap.get("businessPartyId"))) {
                        businessPartyId = (String) pgMap.get("businessPartyId");
                    }

                    String brandName = "";
                    if(UtilValidate.isNotEmpty(pgMap.get("brandName"))) {
                        brandName = (String) pgMap.get("brandName");
                    }
                    String salePrice ="";
                    if(UtilValidate.isNotEmpty(pgMap.get("salePrice"))) {
                        salePrice = (String) pgMap.get("salePrice");
                    }
                    String marketPrice ="";
                    if(UtilValidate.isNotEmpty(pgMap.get("marketPrice"))) {
                        marketPrice = (String) pgMap.get("marketPrice");
                    }
                    String costPrice = "";
                    if(UtilValidate.isNotEmpty(pgMap.get("costPrice"))) {
                        costPrice = (String) pgMap.get("costPrice");
                    }
                    String volume = "";
                    if(UtilValidate.isNotEmpty(pgMap.get("volume"))) {
                        volume = (String) pgMap.get("volume");
                    }
                    String weight = "";
                    if(UtilValidate.isNotEmpty(pgMap.get("weight"))) {
                        weight = (String) pgMap.get("weight");
                    }

                    obj[0] = (i+1)+"";
                    obj[1] = productId;
                    obj[2] = productName;
                    obj[3] = productSubName;

                    obj[4] = levelFirst;
                    obj[5] = levelSecond;
                    obj[6] = levelThird;

                    obj[7] = saleStartTime;
                    obj[8] = saleEndTime;
                    obj[9] = businessPartyId;

                    obj[10] = brandName;
                    obj[11] = salePrice;
                    obj[12] = marketPrice;

                    obj[13] = costPrice;
                    obj[14] = volume;
                    obj[15] = weight;

                    recordsList.add(obj);
                }
            }
        }

        //文件名称
        String fileName = "";
        String title = "";
        //列名
        String[] rowName = null;

        fileName = "商品信息报表_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        title = "商品信息";
        rowName = new String[]{"序号","商品编号","商品名称","商品副标题","一级分类","二级分类","三级分类","销售开始时间","销售结束时间","商家名称","商品品牌","销售价格(元)","市场价格(元)","成本价格(元)","体积(m3)","重量(kg)"};
        try {
            ExcelExport ex = new ExcelExport(response,fileName,title,rowName,recordsList);
            ex.export();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 保存成功
        return "success";
    }
    // Add by zhajh at 20180515 商品导出处理 End



    // Add by zhajh at 20180601 取得skuUrl信息 Begin
    /**
     * 取得skuUrl信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getSkuUrlInfo(HttpServletRequest request, HttpServletResponse response) {
        String urlInfos = "";
        if(UtilValidate.isNotEmpty(urlInfos)){
            urlInfos = request.getParameter("urlInfos");
        }

        // 保存成功
        request.setAttribute("urlInfos", urlInfos);
        request.setAttribute("resultFlg", "true");
        return "success";
    }



    /**
     * 设置session中的某项参数值 add by zhajh 2018.06.4
     * @param request
     * @param response
     * @return
     */
    public static String setSessionByParam(HttpServletRequest request, HttpServletResponse response) {
        String attrName = request.getParameter("attrName");
        String attrVal = request.getParameter("attrVal");
        request.getSession().setAttribute(attrName,attrVal);
        return "success";
    }

    /**
     * 获取session中的某项参数值 add by zhajh 2018.06.4
     * @param request
     * @param response
     * @return
     */
    public static String getSessionByParam(HttpServletRequest request, HttpServletResponse response) {
        String attrName = request.getParameter("attrName");
        request.setAttribute("attrVal", request.getSession().getAttribute(attrName));
        request.getSession().setAttribute(attrName,"");

        return "success";
    }
    // Add by zhajh at 20180601 取得skuUrl信息 End




    // Add by zhajh at 20180605 促销商品验证处理 Begin
    /**
     * 促销商品验证处理
     * @param request
     * @param response
     * @return
     */
    public static String chkPromProcutIsValid(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String productIds = request.getParameter("productIds"); // 商品编码
        String productStoreId = (String) request.getAttribute("productStoreId");// 店铺编码
        String startDate=(String) request.getParameter("startDate");// 开始时间
        String endDate=(String) request.getParameter("endDate");// 结束时间
        List<String> productIdList=FastList.newInstance();// 商品编码列表
        String chkFlg="Y";
        List<String> productIdExists =FastList.newInstance();
        List<String> unUserdProductIdInfos=FastList.newInstance();
        if(UtilValidate.isNotEmpty(startDate)&&(UtilValidate.isNotEmpty(endDate))) {
            unUserdProductIdInfos = getUnUsedProductIds(delegator, productStoreId,Timestamp.valueOf(startDate),Timestamp.valueOf(endDate));
        }
        if(UtilValidate.isNotEmpty(productIds)) {
            String[] prodcutIdArry = productIds.split(",");
            if (UtilValidate.isNotEmpty(prodcutIdArry)) {
                for (String curProductId : prodcutIdArry) {
                    productIdList.add(curProductId);
                }
            }
            // 取得不可以使用的商品编码
//            List<String> unUserdProductIdInfos = getUnUsedProductIds(delegator, productStoreId);

            // 验证处理
            if (UtilValidate.isNotEmpty(productIdList)) {
                for (String curProcutId : productIdList) {
                    if (!unUserdProductIdInfos.contains(curProcutId)) {
                        continue;
                    } else {
                        chkFlg = "N";
                        productIdExists.add(curProcutId);
                    }
                }
            }
        }else{
            //说明此时 的促销是全场促销
            if(UtilValidate.isNotEmpty(unUserdProductIdInfos)){
                chkFlg = "N";
                productIdExists.addAll(unUserdProductIdInfos);
            }
        }

        request.setAttribute("chkFlg", chkFlg);
        request.setAttribute("existProductIds", Joiner.on(",").join(productIdExists));
//        request.setAttribute("errorMsg", "商品id为"+Joiner.on(",").join(productIdExists)+"的商品已经参加过其他促销活动正在活动中！");
        request.setAttribute("errorMsg", "当前时间已存在指定商品的促销，无法创建全场促销");
        if("Y".equalsIgnoreCase(chkFlg)){
            //校验这个时间段是否有全场通用的促销
            boolean isPromoAllExist = ProductServices.isPromoAllExist(delegator,productStoreId,Timestamp.valueOf(startDate),Timestamp.valueOf(endDate));
            if(isPromoAllExist){
                chkFlg = "N";
                request.setAttribute("chkFlg", chkFlg);
                request.setAttribute("errorMsg", "当前时间已存在全场促销");
            }
        }

        return "success";
    }
    // Add by zhajh at 20180605 促销商品验证处理 End




    public static Map<String, Object> findProductCollection(DispatchContext dctx, Map<String, ? extends Object> context) {

        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        Security security = dctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productId = (String) context.get("productId");
        String productName = (String) context.get("productName");
        String productType = (String) context.get("productType");

        // set the page parameters
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
        int lowIndex = 0;
        int highIndex = 0;

        // 选择字段List
        List<String> fieldsToSelect = new LinkedList<String>();
        fieldsToSelect.add("productId");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("isOnline"); //是否上架 N未上架 Y已上架
        fieldsToSelect.add("isVerify"); //是否审核 N未审核 Y已审核

        // list to hold the parameters
        List<String> paramList = FastList.newInstance();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        // list of conditions
        List<EntityCondition> conditions = FastList.newInstance();

        // dynamic view entity
        DynamicViewEntity dve = new DynamicViewEntity();

        dve.addMemberEntity("OH", "ProductCollection"); // 商品收藏表
        dve.addMemberEntity("RI", "Product"); // 商品表
        dve.addMemberEntity("OI", "ProductContent"); //
        dve.addAlias("OH", "productId");
        dve.addAlias("RI", "productName");
        dve.addAlias("RI", "isOnline");
        dve.addAlias("RI", "isVerify");
        dve.addAlias("OI", "contentId");
        dve.addViewLink("OH", "RI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));
        dve.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));

        // start the lookup
        if (UtilValidate.isNotEmpty(productId)) {
            paramList.add("productId=" + productId);
            paramMap.put("productId", productId);
            conditions.add(EntityCondition.makeCondition("productId", EntityOperator.LIKE,"%" + productId + "%"));
        }

        //
        if (UtilValidate.isNotEmpty(productName)) {
            paramList.add("productName=" + productName);
            paramMap.put("productName", productName);
            conditions.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE,"%" + productName + "%"));
        }

        if (UtilValidate.isNotEmpty(productType)){
            paramList.add("productType=" + productType);
            paramMap.put("productType",productType);
            if("01".equals(productType)) {
                // 未上架
                conditions.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "N"));
            } else if ("02".equals(productType)) {
                // 已上架
                conditions.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
                conditions.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "Y"));
            } else if ("03".equals(productType)) {
                // 已下架
                conditions.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "N"));
                conditions.add(EntityCondition.makeCondition("isVerify", EntityOperator.EQUALS, "Y"));
            }
        }

        List<Map<String, Object>> productCollectionList = FastList.newInstance();
        int productCollectionCount = 0;

        // get the index for the partial list
        lowIndex = viewIndex * viewSize + 1;
        highIndex = (viewIndex + 1) * viewSize;

        // set distinct on so we only get one row per order
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        // create the main condition
        EntityCondition cond = null;
        if (conditions.size() > 0) {
            cond = EntityCondition.makeCondition(conditions, EntityOperator.AND);
        }

        String lookupFlag = "N";

        EntityListIterator eli = null;
        try {
            // do the lookup
            eli = delegator.findListIteratorByCondition(dve, cond, null, fieldsToSelect,UtilMisc.toList("productId"), findOpts);
            productCollectionCount = eli.getResultsSizeAfterPartialList();
            List<GenericValue> dd  = eli.getPartialList(lowIndex, viewSize);
            if(productCollectionCount == 0) {
                lookupFlag = "Y";
            } else if (productCollectionCount == 1) {
                GenericValue gv1 = dd.get(0);
                if("".equals(gv1.getString("productId"))) {
                    lookupFlag = "Y";
                } else {
                    for(GenericValue gv : dd) {
                        Map<String, Object> map = FastMap.newInstance();
                        map.put("productId", gv.getString("productId"));
                        map.put("productName", gv.getString("productName"));
                        map.put("collectionNum", "1");
                        map.put("contentId", gv.getString("contentId"));

                        String isOnline = gv.getString("isOnline");
                        String isVerify = gv.getString("isVerify");
                        if(("N".equals(isOnline)&&"N".equals(isVerify))||("Y".equals(isOnline)&&"N".equals(isVerify))) {
                            map.put("productType", "未上架");
                        } else if ("Y".equals(isOnline)&&"Y".equals(isVerify)) {
                            map.put("productType", "已上架");
                        } else if ("N".equals(isOnline)&&"Y".equals(isVerify)) {
                            map.put("productType", "已下架");
                        } else {
                            map.put("productType", "未上架");
                        }
                        productCollectionList.add(map);
                    }
                }
            } else {
                for(GenericValue gv : dd) {
                    Map<String, Object> map = FastMap.newInstance();
                    map.put("productId", gv.getString("productId"));
                    map.put("productName", gv.getString("productName"));
                    map.put("collectionNum", "1");
                    map.put("contentId", gv.getString("contentId"));

                    String isOnline = gv.getString("isOnline");
                    String isVerify = gv.getString("isVerify");
                    if(("N".equals(isOnline)&&"N".equals(isVerify))||("Y".equals(isOnline)&&"N".equals(isVerify))) {
                        map.put("productType", "未上架");
                    } else if ("Y".equals(isOnline)&&"Y".equals(isVerify)) {
                        map.put("productType", "已上架");
                    } else if ("N".equals(isOnline)&&"Y".equals(isVerify)) {
                        map.put("productType", "已下架");
                    } else {
                        map.put("productType", "未上架");
                    }
                    productCollectionList.add(map);
                }
            }

            if (highIndex > productCollectionCount) {
                highIndex = productCollectionCount;
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } finally {
            if (eli != null) {
                try {
                    eli.close();
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, e.getMessage(), module);
                }
            }
        }

        // create the result map
        Map<String, Object> result = ServiceUtil.returnSuccess();

        // format the param list
        String paramString = StringUtil.join(paramList, "&");
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        result.put("viewIndex", viewIndex);
        result.put("lookupFlag", lookupFlag);
        result.put("viewSize", viewSize);
        result.put("paramMap", paramMap);
        result.put("paramList", (paramString != null? paramString: ""));
        result.put("productCollectionList", productCollectionList);
        result.put("productCollectionListSize", Integer.valueOf(productCollectionCount));

        return result;
    }


    /**
     * 获取收藏商品人的具体信息
     *

     * @return
     */
    public static Map<String, Object> getCollectionParty(DispatchContext dctx,
                                                         Map<String, ? extends Object> context) {
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        //商品编码
        String productId = (String) context.get("productId");
        int totalSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // 设置页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 2;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 2;
        }
        result.put("viewSize", Integer.valueOf(viewSize));
        //查询的活动列表
        List<GenericValue> partyList = null;
        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("PAC", "ProductCollection");//活动表

        dve.addMemberEntity("PL", "PartyLevel");//会员表等级
        dve.addMemberEntity("PLT", "PartyLevelType");//会员分类
        dve.addMemberEntity("PS", "Person");//人员表
//		dve.addAliasAll("PAC", "", null);
//		dve.addAliasAll("PL", "", null);
//		dve.addAliasAll("PLT", "", null);
//		dve.addAliasAll("PS", "", null);
        //活动ID
        dve.addAlias("PAC", "productId");
        //活动收藏会员ID
        dve.addAlias("PAC", "partyId");
        //收藏时间
        dve.addAlias("PAC", "createdStamp");
        //会员等级名称
        dve.addAlias("PLT", "levelName");
        //Person表中的手机号
        dve.addAlias("PS", "mobile");
        //Person表中的邮箱
        dve.addAlias("PS", "email");
        //会员等级与会员表关系
        dve.addViewLink("PAC", "PL",Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
        //会员等级与会员等级类型关联关系
        dve.addViewLink("PL", "PLT",Boolean.FALSE, ModelKeyMap.makeKeyMapList("levelId", "levelId"));
        //会员与Person表关系
        dve.addViewLink("PAC", "PS",Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
        //需要查询的字段信息
        List<String> fieldsToSelect = FastList.newInstance();
        //会员ID
        fieldsToSelect.add("partyId");
        //会员等级
        fieldsToSelect.add("levelName");
        //手机号
        fieldsToSelect.add("mobile");
        //Email
        fieldsToSelect.add("email");
        //收藏时间
        fieldsToSelect.add("createdStamp");
        //创建查询条件
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //活动Id
        if (UtilValidate.isNotEmpty(productId))
        {
            andExprs.add(EntityCondition.makeCondition("productId",
                    EntityOperator.EQUALS, productId));
        }
        //创建主要条件
        if (andExprs.size() > 0)
        {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去重复
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //得到iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dve,
                    mainCond, null, fieldsToSelect, null, findOpts);
            partyList = pli.getPartialList(lowIndex, viewSize);
            //获取全部
            totalSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > totalSize)
            {
                highIndex = totalSize;
            }
            //关闭iterator
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        result.put("partyList", partyList);
        result.put("totalSize", Integer.valueOf(totalSize));
        return result;
    }




    /**
     * 商品缓存处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> productInfoRedisPro(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        String productIds = (String) context.get("productIds");//商品编号ID
        String isDel = (String) context.get("isDel"); //是否删除

        String msg="OK";

        String []  strIds= null;
        if (UtilValidate.isNotEmpty(productIds)) {
            strIds = productIds.split(",");//生成商品编码数组
        }
        List<String> productRedisList=FastList.newInstance();
        // 根据商品编码处理缓存
        if (UtilValidate.isNotEmpty(strIds)) {
            for (int i = 0; i < strIds.length; i++) {
                String curProductId=strIds[i];
                productRedisList.add(curProductId);
                // Redis 信息的删除处理
               /* if(UtilRedis.exists(curProductId+"_summary")){
                    UtilRedis.del(curProductId+"_summary");// 产品缓存
                }

                if(UtilRedis.exists(curProductId+"_detail")){
                    UtilRedis.del(curProductId+"_detail");// 产品详情缓存
                }*/

//                if(UtilRedis.exists(curProductId+"_downPromo")){
//                    UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                }
//                if(UtilRedis.exists(curProductId+"_groupOrder")){
//                    UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                }
//                if(UtilRedis.exists(curProductId+"_seckill")) {
//                    UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                }
            }


            if(UtilValidate.isNotEmpty(isDel)){

                if("N".equals(isDel)){
                    Map<String, Object> resultMap = FastMap.newInstance();
                    Map<String, Object> psParams = FastMap.newInstance();
                    Map<String, Object> pdParams = FastMap.newInstance();
                    psParams.put("productIds", productIds);
                    try {
                        // 调用服务 productsSummary
                        resultMap = dispatcher.runSync("productsSummary", psParams);
                        if(UtilValidate.isNotEmpty(productRedisList)){
                            // 调用服务 productDetaiil
                            for(String id:productRedisList){
                                pdParams = FastMap.newInstance();
                                pdParams.put("productId", id);
                                resultMap = FastMap.newInstance();
                                resultMap = dispatcher.runSync("productDetail", pdParams);
                            }
                        }
                    } catch (GenericServiceException e) {
                        result.put("msg",e.getMessage());
                        return ServiceUtil.returnError(e.getMessage());
                    }finally {

                    }
                }
            }

        }
        result.put("msg",msg);
        return result;
    }



    /**
     * 根据商品品牌编码导出商品品牌列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String exportProductBrandsByIds(HttpServletRequest request, HttpServletResponse response)throws GenericEntityException  {
        // 关联商品信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String ids = request.getParameter("ids");
//        String productStoreId = request.getParameter("productStoreId");
        Map<String,Object> brandsMap=FastMap.newInstance();
        List<Map<String,Object>> productBrandList= FastList.newInstance();
        List<Object[]> recordsList = FastList.newInstance();
        //  取得商品信品牌息列表
        brandsMap=productBrandListReport(delegator,ids);
        if(UtilValidate.isNotEmpty(brandsMap)){
            productBrandList=(List<Map<String,Object>>)brandsMap.get("productBrandList");
            if(UtilValidate.isNotEmpty(productBrandList)){
                Object[] obj = null;
                for(int i=0;i<productBrandList.size();i++){
                    Map<String,Object> pgMap=productBrandList.get(i);
                    obj = new Object[4];
                    String brandName = (String)pgMap.get("brandName");
                    String brandNameAlias="";
                    if(UtilValidate.isNotEmpty(pgMap.get("brandNameAlias"))) {
                        brandNameAlias = (String) pgMap.get("brandNameAlias");
                    }
                    String isUsed = (String)pgMap.get("isUsed");
                    obj[0] = (i+1)+"";
                    obj[1] = brandName;
                    obj[2] = brandNameAlias;
                    obj[3] = isUsed;
                    recordsList.add(obj);
                }
            }
        }

        //文件名称
        String fileName = "";
        String title = "";
        //列名
        String[] rowName = null;

        fileName = "商品品牌信息报表_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        title = "商品品牌信息";
        rowName = new String[]{"序号","品牌名称","品牌别名","是否启用"};
        try {
            ExcelExport ex = new ExcelExport(response,fileName,title,rowName,recordsList);
            ex.export();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 保存成功
        return "success";
    }
    // Add by zhajh at 20180515 商品导出处理 End
}


