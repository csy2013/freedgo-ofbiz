package com.yuaoq.yabiz.product.service;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.jdom.JDOMException;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.common.geo.GeoWorker;
import org.ofbiz.content.data.OfbizUrlContentWrapper;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityTypeUtil;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.image.ScaleImage;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.ProductSearchSession;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.service.*;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by changsy on 15/12/18.
 */
public class ProductServices {
    
    public static final String module = ProductServices.class.getName();
    public static final String resource_error = "ProductErrorUiLabels";
    public static final int DEFAULT_TX_TIMEOUT = 600;
    
    public static final String resource = "ProductUiLabels";
    public static final String resourceError = "ProductErrorUiLabels";
    
    /**
     * <attribute name="AVAILABILITY_FILTER" type="Boolean" mode="IN"  default-value="true" />
     * <attribute name="SEARCH_OPERATOR_OR" type="String" mode="IN" default-value="OR" />
     * <attribute name="PAGING"  type="String" default-value="Y" mode="IN"/>
     * <attribute name="VIEW_SIZE"   type="Integer" mode="IN" />
     * <attribute name="VIEW_INDEX"  type="Integer" mode="IN"/>
     * <attribute name="SEARCH_STRING"  type="String" mode="IN"/>
     * <attribute name="sortAscending"  type="String" mode="IN"/>
     * <attribute name="sortOrder"  type="String" mode="IN"/>
     * <attribute name="attributeNames" type="String" mode="IN"/>
     * <attribute name="attributeValues" type="String" mode="IN"/>
     * <attribute name="DEFAULT_PRICE_HIGH" type="String" mode="IN"/>
     * <attribute name="DEFAULT_PRICE_LOW" type="String" mode="IN"/>
     * <attribute name="productBrandString" type="String" mode="IN"/>
     * <attribute name="productPriceString" type="String" mode="IN"/>
     * <attribute name="partBrandname" type="String" mode="IN"/>
     * <attribute name="partSerialname" type="String" mode="IN"/>
     * <attribute name="partGroupname" type="String" mode="IN"/>
     * <attribute name="partCarname" type="String" mode="IN"/>
     * <attribute name="hasInventoryOnly" type="String" mode="IN"/>
     * <attribute name="currentCategoryname" type="String" mode="IN"/>
     * <attribute mode="OUT" name="resultData" optional="true" type="java.util.Map"/>
     *
     * @param request
     * @param response
     * @return "success" or "error"
     */
    public static String keywordSearch(HttpServletRequest request, HttpServletResponse response) {
        String errMsg = null;
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin(DEFAULT_TX_TIMEOUT);
            ProductSearchSession.checkDoKeywordOverride(request, response);
            Delegator delegator = (Delegator) request.getAttribute("delegator");
            Map<String, Object> parameters = FastMap.newInstance();
            ProductSearchSession.processSearchParameters(parameters, request);
            String prodCatalogId = CatalogWorker.getCurrentCatalogId(request);
            Map<String, Object> result = ProductSearchSession.getProductSearchResult(request, delegator, prodCatalogId);
            request.setAttribute("resultData", result);
        } catch (GenericTransactionException e) {
            Map<String, String> messageMap = UtilMisc.toMap("errSearchResult", e.toString());
            errMsg = UtilProperties.getMessage(resource_error, "productsearchevents.error_getting_search_results", messageMap, UtilHttp.getLocale(request));
            Debug.logError(e, errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        } finally {
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (GenericTransactionException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }
    
    /**
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> productReview(DispatchContext dctx, Map<String, ? extends Object> context) {
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
                List ratingReviews = EntityUtil.filterByAnd(reviews, UtilMisc.toList(EntityCondition.makeCondition("productRating", EntityOperator.NOT_EQUAL, null)));
                if (ratingReviews != null && (!ratingReviews.isEmpty())) {
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
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> queryProductCategory(DispatchContext dcx, Map<String, ? extends Object> context) {
        Delegator delegator = dcx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        List<Map<String, Object>> completedTree = new ArrayList<Map<String, Object>>();
        try {
            List<GenericValue> prodCatalogs = delegator.findList("ProdCatalog", null, null, null, null, false);
            if (prodCatalogs != null) {
                for (GenericValue prodCatalog : prodCatalogs) {
                    Map prodCatalogMap = new HashMap();
                    prodCatalogMap.put("productCategoryId", prodCatalog.get("productCategoryId"));
                    prodCatalogMap.put("categoryName", prodCatalog.get("categoryName"));
                    prodCatalogMap.put("isCatalog", true);
                    prodCatalogMap.put("isCategoryType", false);
                    List<GenericValue> prodCatalogCategories = EntityUtil.filterByDate(delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", prodCatalog.get("prodCatalogId"))));
                    if (prodCatalogCategories != null) {
                        prodCatalogMap.put("child", separateRootType(prodCatalogCategories));
                    }
                    completedTree.add(prodCatalogMap);
                    
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnFailure(e.getMessage());
        }
        
        result.put("categories", completedTree);
        return result;
    }
    
    /**
     * 获取商品的可用库存
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> queryAvailableInventoryForProduct(DispatchContext dcx, Map<String, ? extends Object> context) {
        Delegator delegator = dcx.getDelegator();
        String productId = (String) context.get("productId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue product = null;
        BigDecimal availableInventory = BigDecimal.ZERO;
        
        try {
            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
            boolean isMarketingPackage = EntityTypeUtil.hasParentType(delegator, "ProductType", "productTypeId", (String) product.get("productTypeId"), "parentTypeId", "MARKETING_PKG");
            if (isMarketingPackage) {
                LocalDispatcher dispatcher = dcx.getDispatcher();
                Map<String, Object> resultOutput = null;
                try {
                    resultOutput = dispatcher.runSync("getMktgPackagesAvailable", UtilMisc.toMap("productId", productId));
                    availableInventory = (BigDecimal) resultOutput.get("availableToPromiseTotal");
                    
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Error calling the getMktgPackagesAvailable when query queryVailableInventoryForProduct: " + e.toString(), module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                
            } else {
                List<GenericValue> facilities = delegator.findList("ProductFacility",
                        EntityCondition.makeCondition(UtilMisc.toMap("productId", productId)), null, null, null, false);
                
                if (facilities != null) {
                    for (GenericValue facility : facilities) {
                        BigDecimal lastInventoryCount = (BigDecimal) facility.get("lastInventoryCount");
                        if (lastInventoryCount != null) {
                            availableInventory.add(lastInventoryCount);
                        }
                    }
                    
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("availableInventory", availableInventory);
        return result;
    }
    
    
    public static List<Map<String, Object>> separateRootType(List<GenericValue> prodCatalogCategories) throws GenericEntityException {
        if (prodCatalogCategories != null) {
            List<Map<String, Object>> prodRootTypeTree = new ArrayList<Map<String, Object>>();
            for (GenericValue prodCatalogCategory : prodCatalogCategories) {
                Map prodCateMap = new HashMap();
                GenericValue productCategory = prodCatalogCategory.getRelatedOne("ProductCategory");
                prodCateMap.put("productCategoryId", productCategory.getString("productCategoryId"));
                prodCateMap.put("categoryName", productCategory.getString("categoryName"));
                prodCateMap.put("isCatalog", false);
                prodCateMap.put("isCategoryType", true);
                prodRootTypeTree.add(prodCateMap);
            }
            return prodRootTypeTree;
        }
        return null;
    }
    
    public static String lookupProduct(HttpServletRequest request, HttpServletResponse response) {
        String productName = request.getParameter("name");
        String pageNo = request.getParameter("pageNo");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map serviceIn = FastMap.newInstance();
        serviceIn.put("entityName", "Product");
        if (UtilValidate.isNotEmpty(productName)) {
            serviceIn.put("inputFields", UtilMisc.toMap("productName", productName, "productName_op", "contains", "productName_ic", "Y"));
            
        } else {
            serviceIn.put("inputFields", UtilMisc.toMap("productName", ""));
        }
        if (UtilValidate.isEmpty(pageNo)) {
            serviceIn.put("viewIndex", 0);
        } else {
            serviceIn.put("viewIndex", Integer.parseInt(pageNo));
        }
        serviceIn.put("noConditionFind", "Y");
        serviceIn.put("viewSize", 10);
        try {
            Map<String, Object> result = dispatcher.runSync("performFindList", serviceIn);
            request.setAttribute("resultData", result);
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        return "success";
    }
    
    /**
     * 根据店铺名称获取店铺，LOGO，地址信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String lookupStore(HttpServletRequest request, HttpServletResponse response) {
        String storeName = request.getParameter("name");
        String pageNo = request.getParameter("pageNo");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = request.getLocale();
        Map serviceIn = FastMap.newInstance();
        serviceIn.put("entityName", "ProductStore");
        if (UtilValidate.isNotEmpty(storeName)) {
            serviceIn.put("inputFields", UtilMisc.toMap("storeName", storeName, "storeName_op", "contains", "storeName_ic", "Y"));
            
        } else {
            serviceIn.put("inputFields", UtilMisc.toMap("storeName", ""));
        }
        if (UtilValidate.isEmpty(pageNo)) {
            serviceIn.put("viewIndex", 0);
        } else {
            serviceIn.put("viewIndex", Integer.parseInt(pageNo));
        }
        serviceIn.put("noConditionFind", "Y");
        serviceIn.put("viewSize", 10);
        try {
            result = dispatcher.runSync("performFindList", serviceIn);
            if (ServiceUtil.isSuccess(result)) {
                List<GenericValue> stores = (List<GenericValue>) result.get("list");
                if (UtilValidate.isNotEmpty(stores)) {
                    List<Map<String, String>> storeMaps = FastList.newInstance();
                    for (int i = 0; i < stores.size(); i++) {
                        GenericValue store = stores.get(i);
                        Map<String, String> storeMap = FastMap.newInstance();
                        String productStoreId = store.getString("productStoreId");
                        storeMap.put("storeId", productStoreId);
                        storeMap.put("storeName", store.getString("storeName"));
                        List<Map<String, Object>> contactMechMap = ContactMechWorker.getProductStoreContactMechValueMaps(delegator, productStoreId, false, null);
                        if (UtilValidate.isNotEmpty(contactMechMap)) {
                            for (int j = 0; j < contactMechMap.size(); j++) {
                                Map<String, Object> contactMap = contactMechMap.get(j);
                                if (contactMap.containsKey("postalAddress")) {
                                    GenericValue postalAddress = (GenericValue) contactMap.get("postalAddress");
                                    String cityCode = (String) postalAddress.get("city");
                                    storeMap.put("cityName", GeoWorker.getGeoNameById(cityCode, delegator));
                                    String countyCode = (String) postalAddress.get("countyGeoId");
                                    storeMap.put("countyName", GeoWorker.getGeoNameById(countyCode, delegator));
                                }
                            }
                        }
                        
                        GenericValue storeContent = EntityUtil.getFirst(EntityUtil.filterByDate(delegator.findByAnd("ProductStoreContent", UtilMisc.toMap("productStoreId", productStoreId))));
                        if (UtilValidate.isNotEmpty(storeContent)) {
                            String contentId = storeContent.getString("contentId");
                            String imgUrl = OfbizUrlContentWrapper.getOfbizUrlContentAsText(storeContent.getRelatedOne("Content"), locale, dispatcher);
                            String baseUrl = UtilProperties.getMessage("content.properties", "kaide.images.baseUrl", locale);
                            storeMap.put("logo", baseUrl + imgUrl);
                        }
                        storeMaps.add(storeMap);
                    }
                    result.put("list", storeMaps);
                }
            }
            
        } catch (GenericServiceException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("resultData", result);
        return "success";
    }
    
    
    public static String importProductImages(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        try {
            List<GenericValue> productImages = delegator.findList("ProductImageTemp", null, null, null, null, false);
            if (UtilValidate.isNotEmpty(productImages)) {
                for (int i = 0; i < productImages.size(); i++) {
                    GenericValue productImage = (GenericValue) productImages.get(i);
                    String productId = productImage.getString("productId");
                    String images = productImage.getString("images");
                    String[] imageList = images.split("\\|");
                    if (UtilValidate.isNotEmpty(imageList)) {
                        List<Map<String, Object>> resultList = FastList.newInstance();
                        for (int j = 0; j < imageList.length; j++) {
                            String imagePath = imageList[j];
//                              https://file.capitaland.com.cn/webupload/433-1/Inegral/images/201712/201712051532390601.jpg"
                            String imageFileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                            String contentType = "";
                            if (imagePath.endsWith("jpg") || imagePath.endsWith("jpeg")) {
                                contentType = "image/jpeg";
                            } else if (imagePath.endsWith("png")) {
                                contentType = "image/png";
                            } else if (imagePath.endsWith("gif")) {
                                contentType = "image/gif";
                            }
                            HttpClient httpClient = new HttpClient(imagePath);
                            httpClient.setHostVerificationLevel(0);
                            httpClient.setAllowUntrusted(true);
                            ByteBuffer imageData = null;
                            try {
                                InputStream stream = httpClient.getStream();
                                byte[] bytes = StreamUtils.copyToByteArray(stream);
                                imageData = ByteBuffer.wrap(bytes);
                            } catch (HttpClientException e) {
                                Debug.logError("product=" + productId + ",imagePath=" + imagePath + e.getMessage(), module);
                                e.printStackTrace();
                            } catch (IOException e) {
                                Debug.logError("product=" + productId + ",imagePath=" + imagePath + e.getMessage(), module);
                                e.printStackTrace();
                            }
                            
                            try {
                                Map<String, Object> result = dispatcher.runSync("addAdditionalViewForProduct", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_" + (j + 1), "uploadedFile", imageData, "_uploadedFile_fileName", imageFileName, "_uploadedFile_contentType", contentType, "ownerPartyId", userLogin.getString("partyId"), "userLogin", userLogin));
                                if (ServiceUtil.isError(result)) {
                                    Debug.logError("product=" + productId + ",imagePath=" + imagePath + ServiceUtil.getErrorMessage(result), module);
                                } else {
                                    resultList.add(result);
                                }
                            } catch (GenericServiceException e) {
                                e.printStackTrace();
                                Debug.logError("product=" + productId + ",imagePath=" + imagePath + e.getMessage(), module);
                            }
                        }
                        resultData.put(productId, resultList);
                        
                    }
                    
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            Debug.logError(e.getMessage(), module);
        }
        
        request.setAttribute("resultData", resultData);
        return "success";
        
    }
    
    /**
     * 新的新增产品方法
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> updateProduct(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        String productCategoryId = (String) context.get("productCategoryId");
        String brandId = (String) context.get("brandId");
        String productTypeId = (String) context.get("productTypeId");
        Date salesDiscontinuationDate = (Date) context.get("salesDiscontinuationDate");
        Date releaseDate = (Date) context.get("releaseDate");
        String includeInPromotions = (String) context.get("includeInPromotions");
        String productName = (String) context.get("productName");
        String returnable = (String) context.get("returnable");
        String productId = (String) context.get("productId");
        String defaultPrice = (String) context.get("defaultPrice");
        String averageCost = (String) context.get("averageCost");
        String listPrice = (String) context.get("listPrice");
        String valid_period = (String) context.get("valid_period");
        String productCode = (String) context.get("productCode");
        String autoCreateKeywords = (String) context.get("autoCreateKeywords");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productImage = (String) context.get("productImage");
        String additionalImg1 = (String) context.get("additionalImg1");
        String additionalImg2 = (String) context.get("additionalImg2");
        String additionalImg3 = (String) context.get("additionalImg3");
        String additionalImg4 = (String) context.get("additionalImg4");
        String additionalImg5 = (String) context.get("additionalImg5");
        String additionalImg6 = (String) context.get("additionalImg6");
        String productFacilityAmount = (String) context.get("productFacilityAmount");
        String productStoreId = (String) context.get("productStoreId");
        String businessPartyId = (String) context.get("ownerPartyId");
        String allfeatureIds = (String) context.get("featureIds");
        String longDescription = (String) context.get("longDescription");
        String longWapDescription = (String) context.get("longWapDescription");
        String isInner = (String) context.get("isInner");
        BigDecimal weight = (BigDecimal) context.get("weight");
        String isRecommendHomePage = (String) context.get("isRecommendHomePage");
        String isListShow = (String) context.get("isListShow");
        BigDecimal volume = (BigDecimal) context.get("volume");
        String seoKeyword = (String) context.get("seoKeyword");
        String isVirtual = "N";
        if (UtilValidate.isNotEmpty(allfeatureIds)) {
            isVirtual = "Y";
        }
        
        if (UtilValidate.isNotEmpty(valid_period) && "0".equals(valid_period)) {
            releaseDate = UtilDateTime.nowTimestamp();
            salesDiscontinuationDate = null;
        }
        
        //1、新增产品方法, 设置产品图片
        String smallImageUrl = "";
        String mediumImageUrl = "";
        String largeImageUrl = "";
        String detailImageUrl = productImage;
        String originalImageUrl = "";
        String thumbnailImageUrl = "";
        if (UtilValidate.isNotEmpty(productImage)) {
            ///images/products/10725/detail.jpg
            smallImageUrl = productImage.replace("detail", "small");
            mediumImageUrl = productImage.replace("detail", "medium");
            largeImageUrl = productImage.replace("detail", "large");
            originalImageUrl = productImage.replace("detail", "original");
            thumbnailImageUrl = productImage.replace("detail", "thumbnail");
        }
        try {
            Map<String, Object> ret = dispatcher.runSync("updateProduct", UtilMisc.toMap("productName", productName, "productTypeId", productTypeId, "internalName", productName, "salesDiscontinuationDate", salesDiscontinuationDate, "releaseDate", releaseDate, "includeInPromotions", includeInPromotions, "returnable", returnable, "userLogin", userLogin, "brandId", brandId, "isVirtual", isVirtual, "smallImageUrl", smallImageUrl, "mediumImageUrl", mediumImageUrl, "largeImageUrl", largeImageUrl, "originalImageUrl", originalImageUrl, "detailImageUrl", detailImageUrl, "thumbnailImageUrl", thumbnailImageUrl, "pcDetails", longDescription, "mobileDetails", longWapDescription, "productId", productId, "primaryProductCategoryId", productCategoryId, "businessPartyId", businessPartyId, "productStoreId", productStoreId, "isDel", "N", "isOnline", "N", "isInner", isInner, "weight", weight, "volume", volume, "isRecommendHomePage", isRecommendHomePage, "isListShow", isListShow, "seoKeyword", seoKeyword, "isVerify", "N", "productCode", productCode, "autoCreateKeywords", autoCreateKeywords));
            if (ServiceUtil.isSuccess(ret)) {
                
                if (UtilValidate.isNotEmpty(productId)) {
                    
                    // 2、设置产品的价格
                    
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    result.put("product", product);
                    //default price
                    Map<String, Object> priceRet = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(defaultPrice)) {
                        List<GenericValue> productPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY")));
    
                        if (UtilValidate.isNotEmpty(productPrices)) {
                            GenericValue defaultPriceProduct = productPrices.get(0);
                            Map map = defaultPriceProduct.toMap();
                            map.put("userLogin", userLogin);
                            map.put("price", new BigDecimal(defaultPrice));
                            map.remove("priceWithoutTax");
                            map.remove("priceWithTax");
                            map.remove("taxAmount");
                            map.remove("createdDate");
                            map.remove("createdByUserLogin");
                            map.remove("lastModifiedDate");
                            map.remove("lastModifiedByUserLogin");
                            map.remove("lastUpdatedTxStamp");
                            map.remove("createdTxStamp");
                            map.remove("createdStamp");
                            map.remove("lastUpdatedStamp");
                            priceRet = dispatcher.runSync("updateProductPrice", map);
                            if (ServiceUtil.isError(priceRet)) {
                                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(priceRet));
                            }
                        } else {
                            priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(defaultPrice)));
        
                        }
                    }
                    
                   
                    //list price
                    if (UtilValidate.isNotEmpty(listPrice)) {
                        
                        List<GenericValue> productPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY")));
                        
                        if (UtilValidate.isNotEmpty(productPrices)) {
                            GenericValue listPriceProduct = productPrices.get(0);
                            Map map = listPriceProduct.toMap();
                            map.put("userLogin", userLogin);
                            map.put("price", new BigDecimal(listPrice));
                            map.remove("priceWithoutTax");
                            map.remove("priceWithTax");
                            map.remove("taxAmount");
                            map.remove("createdDate");
                            map.remove("createdByUserLogin");
                            map.remove("lastModifiedDate");
                            map.remove("lastModifiedByUserLogin");
                            map.remove("lastUpdatedTxStamp");
                            map.remove("createdTxStamp");
                            map.remove("createdStamp");
                            map.remove("lastUpdatedStamp");
                            priceRet = dispatcher.runSync("updateProductPrice", map);
                            if(ServiceUtil.isError(priceRet)){
                                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(priceRet));
                            }
                        }else{
                            priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "MARKET_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(listPrice)));
    
                        }
                    }
                    
                    if (UtilValidate.isNotEmpty(averageCost)) {
                        
                        List<GenericValue> productPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "COST_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY")));
                        
                        if (UtilValidate.isNotEmpty(productPrices)) {
                            GenericValue costProduct = productPrices.get(0);
                            Map map = costProduct.toMap();
                            map.put("price", new BigDecimal(averageCost));
                            map.put("userLogin", userLogin);
                            map.remove("priceWithoutTax");
                            map.remove("priceWithTax");
                            map.remove("taxAmount");
                            map.remove("createdDate");
                            map.remove("createdByUserLogin");
                            map.remove("lastModifiedDate");
                            map.remove("lastModifiedByUserLogin");
                            map.remove("lastUpdatedTxStamp");
                            map.remove("createdTxStamp");
                            map.remove("createdStamp");
                            map.remove("lastUpdatedStamp");
                            priceRet = dispatcher.runSync("updateProductPrice", map);
                            if(ServiceUtil.isError(priceRet)){
                                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(priceRet));
                            }
                        }else{
                            priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "COST_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(averageCost)));
    
                        }
                        
                    }
                    
                    //3、设置产品的内容,additionImg
                    Map<String, Object> contentRet = dispatcher.runSync("AddProductAdditionalViewImages", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "additionalImg1", additionalImg1, "additionalImg2", additionalImg2, "additionalImg3", additionalImg3, "additionalImg4", additionalImg4, "additionalImg5", additionalImg5, "additionalImg6", additionalImg6));
                    
                    //4、产品的分类
                    
                    
                    //5、设置产品库存 设置库存场所，接受库存
                   /* if (UtilValidate.isEmpty(productStoreId)) {
                        List<GenericValue> stores = delegator.findByAndCache("ProductStore", null);
                        if (UtilValidate.isNotEmpty(stores)) {
                            productStoreId = (String) stores.get(0).get("productStoreId");
                        }
                    }*/
                    
                    //productFacilityAmount
                    List<GenericValue> storeFacilities = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreFacility", UtilMisc.toMap("productStoreId", productStoreId)));
                    String facilityId = "";
                    for (int i = 0; i < storeFacilities.size(); i++) {
                        GenericValue storeFacility = storeFacilities.get(i);
                        GenericValue facility = storeFacility.getRelatedOne("Facility");
                        if ("WAREHOUSE".equals(facility.get("facilityTypeId"))) {
                            facilityId = (String) facility.get("facilityId");
                            break;
                        }
                    }
                    dispatcher.runSync("updateProductFacility", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "facilityId", facilityId, "minimumStock", 1, "reorderQuantity", 1, "daysToShip", 1));
                    List<GenericValue> parties = delegator.findByAnd("PartyGroup", UtilMisc.toMap("isInner", "Y"));
                    /*String ownerPartyId = "10000";
                    if (UtilValidate.isNotEmpty(parties)) {
                        for (int i = 0; i < parties.size(); i++) {
                            GenericValue party = parties.get(i);
                            ownerPartyId = (String) party.get("partyId");
                            break;
                        }
                    }*/
                    //接受库存
                    dispatcher.runSync("receiveInventoryProduct", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "facilityId", facilityId, "inventoryItemTypeId", "NON_SERIAL_INV_ITEM", "ownerPartyId", businessPartyId, "datetimeReceived", UtilDateTime.nowTimestamp(), "quantityRejected", 0, "quantityAccepted", Integer.parseInt(productFacilityAmount), "unitCost", 0));
                    //6、设置产品默认店铺
                    /* dispatcher.runSync("createProductStoreProduct", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreId", productStoreId, "fromDate", UtilDateTime.nowTimestamp()));*/
                    
                    //6、设置多样化sku产品
                    String featurePrices = (String) context.get("featurePrices");
                    String featureStockNum = (String) context.get("featureStockNum");
                    String featureCode = (String) context.get("featureCode");
                    String featureCostPrice = (String) context.get("featureCostPrice");
                    
                    
                    if (UtilValidate.isNotEmpty(allfeatureIds)) {
                        String[] featrueIdArr = allfeatureIds.split(",");
                        String[] featurePricesArr = featurePrices.split(",");
                        String[] featureStockNumArr = featureStockNum.split(",");
                        String[] featureCodeArr = featureCode.split(",");
                        String[] featureCostPriceArr = featureCostPrice.split(",");
                        List<String> variantProductIds = FastList.newInstance();
                        for (int i = 0; i < featrueIdArr.length; i++) {
                            String featureIds = featrueIdArr[i];
                            featureIds = featureIds.substring("sku_opt_".length());
                            String productFeatureIds = featureIds.replace("_", "|");
                            String productVariantId = productId + "|" + featureIds.replace(",", "|");
                            if (productVariantId.length() > 20) {
                                productVariantId = productVariantId.substring(0, 20);
                            }
                            variantProductIds.add(productVariantId);
                            //创建一个变形商品
                            Map<String, Object> varProductRet = dispatcher.runSync("quickAddVariant", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productFeatureIds", productFeatureIds, "productVariantId", productVariantId, "sequenceNum", new Long((i + 1) * 20)));
                            if (ServiceUtil.isSuccess(varProductRet)) {
                                String varProductId = (String) varProductRet.get("productVariantId");
                                String price = featurePricesArr[i];
                                String costPrice = featureCostPriceArr[i];
                                if (UtilValidate.isNotEmpty(price)) {
                                    
                                    List<GenericValue> productPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", varProductId, "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY")));
                                    if (UtilValidate.isNotEmpty(productPrices)) {
                                        GenericValue defalutPriceProduct = productPrices.get(0);
                                        defalutPriceProduct.set("price", new BigDecimal(price));
                                        Map map = defalutPriceProduct.toMap();
                                        map.put("userLogin", userLogin);
                                        map.remove("priceWithoutTax");
                                        map.remove("priceWithTax");
                                        map.remove("taxAmount");
                                        map.remove("createdDate");
                                        map.remove("createdByUserLogin");
                                        map.remove("lastModifiedDate");
                                        map.remove("lastModifiedByUserLogin");
                                        map.remove("lastUpdatedTxStamp");
                                        map.remove("createdTxStamp");
                                        map.remove("createdStamp");
                                        map.remove("lastUpdatedStamp");
                                        priceRet = dispatcher.runSync("updateProductPrice", map);
                                        if(ServiceUtil.isError(priceRet)){
                                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(priceRet));
                                        }
                                        
                                    }else{
                                        priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(price)));
    
                                    }
                                }
                                if (UtilValidate.isNotEmpty(costPrice)) {
                                    List<GenericValue> productPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", varProductId, "productPriceTypeId", "COST_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY")));
                                    if (UtilValidate.isNotEmpty(productPrices)) {
                                        GenericValue defalutPriceProduct = productPrices.get(0);
                                        Map map = defalutPriceProduct.toMap();
                                        map.put("price", new BigDecimal(costPrice));
                                        map.put("userLogin", userLogin);
                                        map.remove("priceWithoutTax");
                                        map.remove("priceWithTax");
                                        map.remove("taxAmount");
                                        map.remove("createdDate");
                                        map.remove("createdByUserLogin");
                                        map.remove("lastModifiedDate");
                                        map.remove("lastModifiedByUserLogin");
                                        map.remove("lastUpdatedTxStamp");
                                        map.remove("createdTxStamp");
                                        map.remove("createdStamp");
                                        map.remove("lastUpdatedStamp");
                                        priceRet = dispatcher.runSync("updateProductPrice", map);
                                        if(ServiceUtil.isError(priceRet)){
                                            return ServiceUtil.returnError(ServiceUtil.getErrorMessage(priceRet));
                                        }
                                    }else{
                                        priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "COST_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(costPrice)));
    
                                    }
                                }
                                
                                String featureStockNum1 = featureStockNumArr[i];
                               /* dispatcher.runSync("updateProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "facilityId", facilityId, "minimumStock", 1, "reorderQuantity", 1, "daysToShip", 1));*/
                                
                                //接受库存
                                dispatcher.runSync("receiveInventoryProduct", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "facilityId", facilityId, "inventoryItemTypeId", "NON_SERIAL_INV_ITEM", "ownerPartyId", businessPartyId, "datetimeReceived", UtilDateTime.nowTimestamp(), "quantityRejected", 0, "quantityAccepted", Integer.parseInt(featureStockNum1), "unitCost", 0));
                                GenericValue varProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", varProductId));
                                String code = featureCodeArr[i];
                                if (UtilValidate.isNotEmpty(varProduct)) {
                                    varProduct.put("productCode", code);
                                    varProduct.store();
                                }
                                
                            }
                        }
                       //删除isDel
                       List<GenericValue> oldVariantProducts = delegator.findByAnd("Product",UtilMisc.toMap("mainProductId",productId));
                        if(UtilValidate.isNotEmpty(oldVariantProducts)){
                            for (int i = 0; i < oldVariantProducts.size(); i++) {
                                boolean hasIn = false;
                                GenericValue oldProduct = oldVariantProducts.get(i);
                                for (int j = 0; j < variantProductIds.size(); j++) {
                                    String featureProductId =  variantProductIds.get(j);
                                    if(featureProductId.equals(oldProduct.getString("productId"))){
                                        hasIn = true;
                                        break;
                                    }
                                }
                                if(!hasIn){
                                    oldProduct.set("isDel","Y");
                                    oldProduct.store();
                                }
                            }
                        }
                        
                    }
                    
                    
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
    
    /**
     * 新的新增产品方法
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> addProduct(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        String productCategoryId = (String) context.get("productCategoryId");
        String brandId = (String) context.get("brandId");
        String productTypeId = (String) context.get("productTypeId");
        Date salesDiscontinuationDate = (Date) context.get("salesDiscontinuationDate");
        Date releaseDate = (Date) context.get("releaseDate");
        String includeInPromotions = (String) context.get("includeInPromotions");
        String productName = (String) context.get("productName");
        String returnable = (String) context.get("returnable");
        String productId = (String) context.get("productId");
        String defaultPrice = (String) context.get("defaultPrice");
        String averageCost = (String) context.get("averageCost");
        String listPrice = (String) context.get("listPrice");
        String valid_period = (String) context.get("valid_period");
        String autoCreateKeywords = (String) context.get("autoCreateKeywords");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productImage = (String) context.get("productImage");
        String additionalImg1 = (String) context.get("additionalImg1");
        String additionalImg2 = (String) context.get("additionalImg2");
        String additionalImg3 = (String) context.get("additionalImg3");
        String additionalImg4 = (String) context.get("additionalImg4");
        String additionalImg5 = (String) context.get("additionalImg5");
        String additionalImg6 = (String) context.get("additionalImg6");
        String productFacilityAmount = (String) context.get("productFacilityAmount");
        String productStoreId = (String) context.get("productStoreId");
        String businessPartyId = (String) context.get("ownerPartyId");
        String allfeatureIds = (String) context.get("featureIds");
        String longDescription = (String) context.get("longDescription");
        String longWapDescription = (String) context.get("longWapDescription");
        String isInner = (String) context.get("isInner");
        BigDecimal weight = (BigDecimal) context.get("weight");
        String isRecommendHomePage = (String) context.get("isRecommendHomePage");
        String productCode = (String) context.get("productCode");
        String isListShow = (String) context.get("isListShow");
        BigDecimal volume = (BigDecimal) context.get("volume");
        String seoKeyword = (String) context.get("seoKeyword");
        String isVirtual = "N";
        if (UtilValidate.isNotEmpty(allfeatureIds)) {
            isVirtual = "Y";
        }
        
        if (UtilValidate.isNotEmpty(valid_period) && "0".equals(valid_period)) {
            releaseDate = UtilDateTime.nowTimestamp();
            salesDiscontinuationDate = null;
        }
        
        //1、新增产品方法, 设置产品图片
        String smallImageUrl = "";
        String mediumImageUrl = "";
        String largeImageUrl = "";
        String detailImageUrl = productImage;
        String originalImageUrl = "";
        String thumbnailImageUrl = "";
        if (UtilValidate.isNotEmpty(productImage)) {
            ///images/products/10725/detail.jpg
            smallImageUrl = productImage.replace("detail", "small");
            mediumImageUrl = productImage.replace("detail", "medium");
            largeImageUrl = productImage.replace("detail", "large");
            originalImageUrl = productImage.replace("detail", "original");
            thumbnailImageUrl = productImage.replace("detail", "thumbnail");
        }
        try {
            Map<String, Object> ret = dispatcher.runSync("createProduct", UtilMisc.toMap("productName", productName, "productTypeId", productTypeId, "internalName", productName, "salesDiscontinuationDate", salesDiscontinuationDate, "releaseDate", releaseDate, "includeInPromotions", includeInPromotions, "returnable", returnable, "userLogin", userLogin, "brandId", brandId, "isVirtual", isVirtual, "smallImageUrl", smallImageUrl, "mediumImageUrl", mediumImageUrl, "largeImageUrl", largeImageUrl, "originalImageUrl", originalImageUrl, "detailImageUrl", detailImageUrl, "thumbnailImageUrl", thumbnailImageUrl, "pcDetails", longDescription, "mobileDetails", longWapDescription, "productId", productId, "primaryProductCategoryId", productCategoryId, "businessPartyId", businessPartyId, "productStoreId", productStoreId, "isDel", "N", "isOnline", "N", "isInner", isInner, "weight", weight, "volume", volume, "isRecommendHomePage", isRecommendHomePage, "isListShow", isListShow, "seoKeyword", seoKeyword, "isVerify", "N", "productCode", productCode, "autoCreateKeywords", autoCreateKeywords));
            if (ServiceUtil.isSuccess(ret)) {
                
                if (UtilValidate.isNotEmpty(ret.get("productId"))) {
                    
                    // 2、设置产品的价格
                    
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                    result.put("product", product);
                    //default price
                    Map<String, Object> priceRet = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(defaultPrice)) {
                        priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(defaultPrice)));
                    }
                    //list price
                    if (UtilValidate.isNotEmpty(listPrice)) {
                        priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "MARKET_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(listPrice)));
                    }
                    
                    if (ServiceUtil.isSuccess(priceRet)) {
                        if (UtilValidate.isNotEmpty(averageCost)) {
                            priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "COST_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(averageCost)));
                        }
                    }
                    
                    //3、设置产品的内容,additionImg
                    Map<String, Object> contentRet = dispatcher.runSync("AddProductAdditionalViewImages", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "additionalImg1", additionalImg1, "additionalImg2", additionalImg2, "additionalImg3", additionalImg3, "additionalImg4", additionalImg4, "additionalImg5", additionalImg5, "additionalImg6", additionalImg6));
                    
                    //4、产品的分类
                    
                    
                    //5、设置产品库存 设置库存场所，接受库存
                    
                    
                    //productFacilityAmount
                    List<GenericValue> storeFacilities = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreFacility", UtilMisc.toMap("productStoreId", productStoreId)));
                    String facilityId = "";
                    for (int i = 0; i < storeFacilities.size(); i++) {
                        GenericValue storeFacility = storeFacilities.get(i);
                        GenericValue facility = storeFacility.getRelatedOne("Facility");
                        if ("WAREHOUSE".equals(facility.get("facilityTypeId"))) {
                            facilityId = (String) facility.get("facilityId");
                            break;
                        }
                    }
                    /* dispatcher.runSync("updateProductFacility", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "facilityId", facilityId, "minimumStock", 1, "reorderQuantity", 1, "daysToShip", 1));*/
//                    List<GenericValue> parties = delegator.findByAnd("PartyGroup", UtilMisc.toMap("isInner", "Y"));
                    
                    //接受库存
                    dispatcher.runSync("receiveInventoryProduct", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "facilityId", facilityId, "inventoryItemTypeId", "NON_SERIAL_INV_ITEM", "ownerPartyId", businessPartyId, "datetimeReceived", UtilDateTime.nowTimestamp(), "quantityRejected", 0, "quantityAccepted", Integer.parseInt(productFacilityAmount), "unitCost", 0));
                    //6、设置产品默认店铺
                    /* dispatcher.runSync("createProductStoreProduct", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productStoreId", productStoreId, "fromDate", UtilDateTime.nowTimestamp()));*/
                    
                    //6、设置多样化sku产品
                    String featurePrices = (String) context.get("featurePrices");
                    String featureStockNum = (String) context.get("featureStockNum");
                    String featureCode = (String) context.get("featureCode");
                    String featureCostPrice = (String) context.get("featureCostPrice");
                    
                    
                    if (UtilValidate.isNotEmpty(allfeatureIds)) {
                        String[] featrueIdArr = allfeatureIds.split(",");
                        String[] featurePricesArr = featurePrices.split(",");
                        String[] featureStockNumArr = featureStockNum.split(",");
                        String[] featureCodeArr = featureCode.split(",");
                        String[] featureCostPriceArr = featureCostPrice.split(",");
                        for (int i = 0; i < featrueIdArr.length; i++) {
                            String featureIds = featrueIdArr[i];
                            featureIds = featureIds.substring("sku_opt_".length());
                            String productFeatureIds = featureIds.replace("_", "|");
                            String productVariantId = productId + "|" + featureIds.replace(",", "|");
                            if (productVariantId.length() > 20) {
                                productVariantId = productVariantId.substring(0, 20);
                            }
                            //创建一个变形商品
                            Map<String, Object> varProductRet = dispatcher.runSync("quickAddVariant", UtilMisc.toMap("userLogin", userLogin, "productId", productId, "productFeatureIds", productFeatureIds, "productVariantId", productVariantId, "sequenceNum", new Long((i + 1) * 20)));
                            if (ServiceUtil.isSuccess(varProductRet)) {
                                String varProductId = (String) varProductRet.get("productVariantId");
                                String price = featurePricesArr[i];
                                String costPrice = featureCostPriceArr[i];
                                if (UtilValidate.isNotEmpty(price)) {
                                    priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "DEFAULT_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(price)));
                                }
                                if (UtilValidate.isNotEmpty(costPrice)) {
                                    priceRet = dispatcher.runSync("createProductPrice", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "productStoreGroupId", "_NA_", "taxInPrice", "Y", "productPriceTypeId", "COST_PRICE", "productPricePurposeId", "PURCHASE", "currencyUomId", "CNY", "price", new BigDecimal(costPrice)));
                                }
                                
                                String featureStockNum1 = featureStockNumArr[i];
//                                dispatcher.runSync("updateProductFacility", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "facilityId", facilityId, "minimumStock", 1, "reorderQuantity", 1, "daysToShip", 1));
                                
                                //接受库存
                                dispatcher.runSync("receiveInventoryProduct", UtilMisc.toMap("userLogin", userLogin, "productId", varProductId, "facilityId", facilityId, "inventoryItemTypeId", "NON_SERIAL_INV_ITEM", "ownerPartyId", businessPartyId, "datetimeReceived", UtilDateTime.nowTimestamp(), "quantityRejected", 0, "quantityAccepted", Integer.parseInt(featureStockNum1), "unitCost", 0));
                                GenericValue varProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", varProductId));
                                String code = featureCodeArr[i];
                                if (UtilValidate.isNotEmpty(varProduct)) {
                                    varProduct.put("productCode", code);
                                    varProduct.store();
                                }
                                
                            }
                        }
                        
                        
                    }
                    
                    
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
    
    public static Map<String, Object> AddProductAdditionalViewImages(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        String productId = (String) context.get("productId");
        String additionalImg1 = (String) context.get("additionalImg1");
        String additionalImg2 = (String) context.get("additionalImg2");
        String additionalImg3 = (String) context.get("additionalImg3");
        String additionalImg4 = (String) context.get("additionalImg4");
        String additionalImg5 = (String) context.get("additionalImg5");
        String additionalImg6 = (String) context.get("additionalImg6");
        GenericValue userLogin = (GenericValue)context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        Map additionalImgs = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(additionalImg1)) {
            additionalImgs.put("1", additionalImg1);
        }else{
            //删除商品图片
            try {
                dispatcher.runSync("removeProductContentsAndImageFiles",UtilMisc.toMap("userLogin", userLogin,"productId",productId,"productContentTypeId","ADDITIONAL_IMAGE_1"));
                
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
        }
        if (UtilValidate.isNotEmpty(additionalImg2)) {
            additionalImgs.put("2", additionalImg2);
        }else{
            //删除商品图片
            try {
                dispatcher.runSync("removeProductContentsAndImageFiles",UtilMisc.toMap("userLogin", userLogin,"productId",productId,"productContentTypeId","ADDITIONAL_IMAGE_2"));
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
        }
        if (UtilValidate.isNotEmpty(additionalImg3)) {
            additionalImgs.put("3", additionalImg3);
        }else{
            //删除商品图片
            try {
                dispatcher.runSync("removeProductContentsAndImageFiles",UtilMisc.toMap("userLogin", userLogin,"productId",productId,"productContentTypeId","ADDITIONAL_IMAGE_3"));
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
        }
        if (UtilValidate.isNotEmpty(additionalImg4)) {
            additionalImgs.put("4", additionalImg4);
        }else{
            //删除商品图片
            try {
                dispatcher.runSync("removeProductContentsAndImageFiles",UtilMisc.toMap("userLogin", userLogin,"productId",productId,"productContentTypeId","ADDITIONAL_IMAGE_4"));
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
        }
        if (UtilValidate.isNotEmpty(additionalImg5)) {
            additionalImgs.put("5", additionalImg5);
        }else{
            //删除商品图片
            try {
                dispatcher.runSync("removeProductContentsAndImageFiles",UtilMisc.toMap("userLogin", userLogin,"productId",productId,"productContentTypeId","ADDITIONAL_IMAGE_5"));
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
        }
        if (UtilValidate.isNotEmpty(additionalImg6)) {
            additionalImgs.put("6", additionalImg6);
        }else{
            //删除商品图片
            try {
                dispatcher.runSync("removeProductContentsAndImageFiles",UtilMisc.toMap("userLogin", userLogin,"productId",productId,"productContentTypeId","ADDITIONAL_IMAGE_6"));
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
        }
        if (UtilValidate.isNotEmpty(additionalImgs)) {
            Iterator keyIter = additionalImgs.keySet().iterator();
            while (keyIter.hasNext()) {
                String index = (String) keyIter.next();
                String productContentTypeId = "ADDITIONAL_IMAGE_" + index;
                String viewType = "additional" + index;
                String additionalImg = (String) additionalImgs.get(index);
                
                String imageFilenameFormat = UtilProperties.getPropertyValue("catalog", "image.filename.additionalviewsize.format");
                String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.server.path"), context);
                while (imageServerPath.endsWith("/")) {
                    imageServerPath = imageServerPath.substring(0, imageServerPath.length() - 1);
                }
                String imageUrlPrefix = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.url.prefix"), context);
                if (imageUrlPrefix.endsWith("/")) {
                    imageUrlPrefix = imageUrlPrefix.substring(0, imageUrlPrefix.length() - 1);
                }
                FlexibleStringExpander filenameExpander = FlexibleStringExpander.getInstance(imageFilenameFormat);
                String viewNumber = String.valueOf(productContentTypeId.charAt(productContentTypeId.length() - 1));
                
                String id = productId;
                if (imageFilenameFormat.endsWith("${id}")) {
                    id = productId + "_View_" + viewNumber;
                    viewType = "additional";
                }
                String fileLocation = filenameExpander.expandString(UtilMisc.toMap("location", "products", "id", id, "viewtype", viewType, "sizetype", "original"));
                String filePathPrefix = "";
                String filenameToUse = fileLocation;
                if (fileLocation.lastIndexOf("/") != -1) {
                    filePathPrefix = fileLocation.substring(0, fileLocation.lastIndexOf("/") + 1); // adding 1 to include the trailing slash
                    filenameToUse = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
                }
                
                String mimeType = additionalImg.substring(additionalImg.lastIndexOf("."));
                String imageUrl = additionalImg;
                String saveImagePath = imageUrlPrefix + "/" + filePathPrefix;
                
                String saveImageKey = saveImagePath + "original" + mimeType;
                /* store the imageUrl version of the image, for backwards compatibility with code that does not use scaled versions */
                result = addImageResource(dispatcher, delegator, context, imageUrl, saveImageKey, productContentTypeId);
                
                if (ServiceUtil.isError(result)) {
                    return result;
                }
                
                /* scale Image in different sizes */
                
                Map<String, String> qiniuImageUrlMap = FastMap.newInstance();
                //文件上传到服务器之后(备份使用)，选择是否上传到云存储
                String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                
                // Save each Url
                
                Map imageUrlMap = FastMap.newInstance();
                for (String sizeType : ScaleImage.sizeTypeList) {
                    
                    // Build full path for the new scaled image
                    String newFileLocation = null;
                    newFileLocation = filenameExpander.expandString(UtilMisc.toMap("location", "products", "id", id, "viewtype", viewType, "sizetype", sizeType));
                    String imageUrl1 = imageUrlPrefix + "/" + newFileLocation + mimeType;
                    imageUrlMap.put(sizeType, imageUrl1);
                    
                }
                final List<String> sizeTypeList = UtilMisc.toList("small", "medium", "large", "detail", "thumbnail");
                if ("qiniu".equals(uploadType)) {
                    //异步调用qiniu 上传文件的服务器
                    
                    for (String sizeType : sizeTypeList) {
                        if ("small".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/200");
                        } else if ("medium".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/400");
                        } else if ("large".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/800");
                        } else if ("detail".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/600");
                        } else if ("thumbnail".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/220");
                        }
                        
                    }
                    
                } else if ("FTP".equals(uploadType)) {
                    try {
                        dispatcher.runSync("ftpUpload", UtilMisc.toMap("filePath", saveImagePath));
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                    }
                }
                
                
                for (String sizeType : sizeTypeList) {
                    imageUrl = (String) imageUrlMap.get(sizeType);
                    String qiniuImageUrl = qiniuImageUrlMap.get(sizeType);
                    if (UtilValidate.isNotEmpty(imageUrl)) {
                        result = addImageResource(dispatcher, delegator, context, imageUrl, qiniuImageUrl, "XTRA_IMG_" + viewNumber + "_" + sizeType.toUpperCase());
                        if (ServiceUtil.isError(result)) {
                            return result;
                        }
                    }
                }
            }
        }
        return ServiceUtil.returnSuccess();
    }
    
    private static Map<String, Object> addImageResource(LocalDispatcher dispatcher, Delegator delegator, Map<String, ? extends Object> context,
                                                        String imageUrl, String qiniuImageUrl, String productContentTypeId) {
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productId = (String) context.get("productId");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        if (UtilValidate.isNotEmpty(imageUrl) && imageUrl.length() > 0) {
            String contentId = (String) context.get("contentId");
            
            Map<String, Object> dataResourceCtx = FastMap.newInstance();
            dataResourceCtx.put("objectInfo", imageUrl);
            dataResourceCtx.put("ftpObjectInfo", imageUrl);
            dataResourceCtx.put("qiniuObjectInfo", qiniuImageUrl);
            dataResourceCtx.put("dataResourceName", context.get("_uploadedFile_fileName"));
            dataResourceCtx.put("isPublic", "Y");
            dataResourceCtx.put("userLogin", userLogin);
            
            Map<String, Object> productContentCtx = FastMap.newInstance();
            productContentCtx.put("productId", productId);
            productContentCtx.put("productContentTypeId", productContentTypeId);
            productContentCtx.put("fromDate", context.get("fromDate"));
            productContentCtx.put("thruDate", context.get("thruDate"));
            productContentCtx.put("userLogin", userLogin);
            
            if (UtilValidate.isNotEmpty(contentId)) {
                GenericValue content = null;
                try {
                    content = delegator.findOne("Content", UtilMisc.toMap("contentId", contentId), false);
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                
                if (content != null) {
                    GenericValue dataResource = null;
                    try {
                        dataResource = content.getRelatedOne("DataResource");
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                    
                    if (dataResource != null) {
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
                    
                    productContentCtx.put("contentId", contentId);
                    try {
                        dispatcher.runSync("updateProductContent", productContentCtx);
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
                
                productContentCtx.put("contentId", contentResult.get("contentId"));
                try {
                    result = dispatcher.runSync("createProductContent", productContentCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        return result;
    }
    
    public static Map<String, Object> addAdditionalViewForProduct(DispatchContext dctx, Map<String, ? extends Object> context)
            throws IOException, JDOMException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> retObj = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        String productId = (String) context.get("productId");
        String productContentTypeId = (String) context.get("productContentTypeId");
        ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
        Locale locale = (Locale) context.get("locale");
        
        if (UtilValidate.isNotEmpty(context.get("_uploadedFile_fileName"))) {
            String imageFilenameFormat = UtilProperties.getPropertyValue("catalog", "image.filename.additionalviewsize.format");
            String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.server.path"), context);
            while (imageServerPath.endsWith("/")) {
                imageServerPath = imageServerPath.substring(0, imageServerPath.length() - 1);
            }
            String imageUrlPrefix = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.url.prefix"), context);
            if (imageUrlPrefix.endsWith("/")) {
                imageUrlPrefix = imageUrlPrefix.substring(0, imageUrlPrefix.length() - 1);
            }
            FlexibleStringExpander filenameExpander = FlexibleStringExpander.getInstance(imageFilenameFormat);
            String viewNumber = String.valueOf(productContentTypeId.charAt(productContentTypeId.length() - 1));
            if ("1".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageOne");
            } else if ("2".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageTwo");
            } else if ("3".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageThree");
            } else if ("4".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageFour");
            } else if ("5".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageFive");
            } else if ("6".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageSix");
            } else if ("7".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageSeven");
            } else if ("8".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageEight");
            }
            String viewType = "additional" + viewNumber;
            String id = productId;
            if (imageFilenameFormat.endsWith("${id}")) {
                id = productId + "_View_" + viewNumber;
                viewType = "additional";
            }
            String fileLocation = filenameExpander.expandString(UtilMisc.toMap("location", "products", "id", id, "viewtype", viewType, "sizetype", "original"));
            String filePathPrefix = "";
            String filenameToUse = fileLocation;
            if (fileLocation.lastIndexOf("/") != -1) {
                filePathPrefix = fileLocation.substring(0, fileLocation.lastIndexOf("/") + 1); // adding 1 to include the trailing slash
                filenameToUse = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
            }
            
            List<GenericValue> fileExtension = FastList.newInstance();
            try {
                fileExtension = delegator.findByAnd("FileExtension", UtilMisc.toMap("mimeTypeId", context.get("_uploadedFile_contentType")));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            GenericValue extension = EntityUtil.getFirst(fileExtension);
            if (extension != null) {
                filenameToUse += "." + extension.getString("fileExtensionId");
                retObj.put("name", filenameToUse);
                retObj.put("type", extension.getString("fileExtensionId"));
            }
            
            retObj.put("productContentTypeId", productContentTypeId);
            retObj.put("productId", productId);
            retObj.put("size", imageData.array().length);
            /* Write the new image file */
            String targetDirectory = imageServerPath + "/" + filePathPrefix;
            try {
                File targetDir = new File(targetDirectory);
                // Create the new directory
                if (!targetDir.exists()) {
                    boolean created = targetDir.mkdirs();
                    if (!created) {
                        String errMsg = UtilProperties.getMessage(resource, "ScaleImage.unable_to_create_target_directory", locale) + " - " + targetDirectory;
                        Debug.logFatal(errMsg, module);
                        return ServiceUtil.returnError(errMsg);
                    }
                    // Delete existing image files
                    // Images are ordered by productId (${location}/${id}/${viewtype}/${sizetype})
                } else if (!filenameToUse.contains(productId)) {
                    try {
                        File[] files = targetDir.listFiles();
                        for (File file : files) {
                            if (file.isFile()) {
                                file.delete();
                            }
                        }
                    } catch (SecurityException e) {
                        Debug.logError(e, module);
                    }
                    // Images aren't ordered by productId (${location}/${viewtype}/${sizetype}/${id})
                } else {
                    try {
                        File[] files = targetDir.listFiles();
                        for (File file : files) {
                            if (file.isFile() && file.getName().startsWith(productId + "_View_" + viewNumber)) {
                                file.delete();
                            }
                        }
                    } catch (SecurityException e) {
                        Debug.logError(e, module);
                    }
                }
            } catch (NullPointerException e) {
                Debug.logError(e, module);
            }
            // Write
            String filePath = imageServerPath + "/" + fileLocation + "." + extension.getString("fileExtensionId");
            try {
                File file = new File(filePath);
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
            } catch (NullPointerException e) {
                Debug.logError(e, module);
            }
            
            //文件上传到服务器之后(备份使用)，选择是否上传到云存储
            String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
            
            
            /* scale Image in different sizes */
            Map<String, Object> resultResize = FastMap.newInstance();
            Map<String, String> qiniuImageUrlMap = FastMap.newInstance();
            String saveImagePath = imageUrlPrefix + "/" + filePathPrefix;
            String saveImageKey = (saveImagePath + "original." + extension.getString("fileExtensionId"));
            try {
                resultResize.putAll(ScaleImage.scaleImageInAllSize(context, filenameToUse, "additional", viewNumber, "products", productId, dispatcher));
                if ("qiniu".equals(uploadType)) {
                    //异步调用qiniu 上传文件的服务器
                    dispatcher.runAsync("coverUpload", UtilMisc.toMap("filePath", filePath, "fileKey", saveImageKey));
                    for (String sizeType : ScaleImage.sizeTypeList) {
                        if ("small".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/200");
                        } else if ("medium".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/400");
                        } else if ("large".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/800");
                        } else if ("detail".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/600");
                        } else if ("thumbnail".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/220");
                        }
                        
                    }
                    
                } else if ("FTP".equals(uploadType)) {
                    //异步调用qiniu 上传文件的服务器
                    dispatcher.runSync("ftpUpload", UtilMisc.toMap("filePath", filePath));
                }
                
            } catch (IOException e) {
                Debug.logError(e, "Scale additional image in all different sizes is impossible : " + e.toString(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductImageViewScaleImpossible", UtilMisc.toMap("errorString", e.toString()), locale));
            } catch (JDOMException e) {
                Debug.logError(e, "Errors occur in parsing ImageProperties.xml : " + e.toString(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductImageViewParsingError", UtilMisc.toMap("errorString", e.toString()), locale));
            } catch (ServiceValidationException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            } catch (ServiceAuthException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            } catch (GenericServiceException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            }
            
            String imageUrl = imageUrlPrefix + "/" + fileLocation + "." + extension.getString("fileExtensionId");
            /* store the imageUrl version of the image, for backwards compatibility with code that does not use scaled versions */
            Map<String, Object> result1 = addImageResource(dispatcher, delegator, context, imageUrl, saveImageKey, productContentTypeId);
            //增加商品图片对应的店铺商家
            String partyId = (String) context.get("ownerPartyId");
            String contentId = (String) result1.get("contentId");
            result.put("contentId", result1.get("contentId"));
            try {
                dispatcher.runSync("createPartyContent", UtilMisc.toMap("partyId", partyId, "contentId", contentId, "partyContentTypeId", "PARTY_IMG_PRODUCT", "fromDate", UtilDateTime.nowTimestamp(), "userLogin", context.get("userLogin")));
            } catch (GenericServiceException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            if (ServiceUtil.isError(result)) {
                return result;
            }
            GenericValue product = null;
            ProductContentWrapper contentWrapper = null;
            try {
                product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                contentWrapper = new ProductContentWrapper(dispatcher, product, locale, "text/html");
                String url = contentWrapper.get(productContentTypeId).toString();
                retObj.put("url", url);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            
            /* now store the image versions created by ScaleImage.scaleImageInAllSize */
            /* have to shrink length of productContentTypeId, as otherwise value is too long for database field */
            Map<String, String> imageUrlMap = UtilGenerics.checkMap(resultResize.get("imageUrlMap"));
            for (String sizeType : ScaleImage.sizeTypeList) {
                imageUrl = imageUrlMap.get(sizeType);
                String qiniuImageUrl = qiniuImageUrlMap.get(sizeType);
                if (UtilValidate.isNotEmpty(imageUrl)) {
                    result1 = addImageResource(dispatcher, delegator, context, imageUrl, qiniuImageUrl, "XTRA_IMG_" + viewNumber + "_" + sizeType.toUpperCase());
                    
                    if (contentWrapper != null) {
                        String domain = UtilProperties.getPropertyValue("content", "img.qiniu.domain");
                        String pathUrl = contentWrapper.get("XTRA_IMG_" + viewNumber + "_" + sizeType.toUpperCase()).toString();
                        pathUrl = UtilStrings.replace(pathUrl, domain, "/");
                        retObj.put(sizeType.toLowerCase() + "_url", pathUrl);
                    }
                    if (ServiceUtil.isError(result1)) {
                        return result1;
                    }
                    //增加商品图片对应的店铺商家
                    partyId = (String) context.get("ownerPartyId");
                    contentId = (String) result1.get("contentId");
                    try {
                        dispatcher.runSync("createPartyContent", UtilMisc.toMap("partyId", partyId, "contentId", contentId, "partyContentTypeId", "PARTY_IMG_PRODUCT", "fromDate", UtilDateTime.nowTimestamp(), "userLogin", context.get("userLogin")));
                    } catch (GenericServiceException e) {
                        return ServiceUtil.returnError(e.getMessage());
                    }
                    if (ServiceUtil.isError(result)) {
                        return result;
                    }
                }
            }
            
        }
        result.put("retObj", retObj);
        return result;
    }
    
    /**
     * 创建产品的addition 图片，不创建product content by changsy 2017
     *
     * @return
     * @throws IOException
     * @throws JDOMException
     */
    public static Map<String, Object> addAdditionalViewForProductPre(DispatchContext dctx, Map<String, ? extends Object> context)
            throws IOException, JDOMException {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Map<String, Object> retObj = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        String productId = (String) context.get("productId");
        String productContentTypeId = (String) context.get("productContentTypeId");
        ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
        Locale locale = (Locale) context.get("locale");
        
        if (UtilValidate.isNotEmpty(context.get("_uploadedFile_fileName"))) {
            String imageFilenameFormat = UtilProperties.getPropertyValue("catalog", "image.filename.additionalviewsize.format");
            String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.server.path"), context);
            while (imageServerPath.endsWith("/")) {
                imageServerPath = imageServerPath.substring(0, imageServerPath.length() - 1);
            }
            String imageUrlPrefix = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.url.prefix"), context);
            if (imageUrlPrefix.endsWith("/")) {
                imageUrlPrefix = imageUrlPrefix.substring(0, imageUrlPrefix.length() - 1);
            }
            FlexibleStringExpander filenameExpander = FlexibleStringExpander.getInstance(imageFilenameFormat);
            String viewNumber = String.valueOf(productContentTypeId.charAt(productContentTypeId.length() - 1));
            if ("1".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageOne");
            } else if ("2".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageTwo");
            } else if ("3".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageThree");
            } else if ("4".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageFour");
            } else if ("5".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageFive");
            } else if ("6".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageSix");
            } else if ("7".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageSeven");
            } else if ("8".equals(viewNumber)) {
                retObj.put("paramName", "additionalImageEight");
            }
            String viewType = "additional" + viewNumber;
            String id = productId;
            if (imageFilenameFormat.endsWith("${id}")) {
                id = productId + "_View_" + viewNumber;
                viewType = "additional";
            }
            String fileLocation = filenameExpander.expandString(UtilMisc.toMap("location", "products", "id", id, "viewtype", viewType, "sizetype", "original"));
            String filePathPrefix = "";
            String filenameToUse = fileLocation;
            if (fileLocation.lastIndexOf("/") != -1) {
                filePathPrefix = fileLocation.substring(0, fileLocation.lastIndexOf("/") + 1); // adding 1 to include the trailing slash
                filenameToUse = fileLocation.substring(fileLocation.lastIndexOf("/") + 1);
            }
            
            List<GenericValue> fileExtension = FastList.newInstance();
            try {
                fileExtension = delegator.findByAnd("FileExtension", UtilMisc.toMap("mimeTypeId", context.get("_uploadedFile_contentType")));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            GenericValue extension = EntityUtil.getFirst(fileExtension);
            if (extension != null) {
                filenameToUse += "." + extension.getString("fileExtensionId");
                retObj.put("name", filenameToUse);
                retObj.put("type", extension.getString("fileExtensionId"));
            }
            retObj.put("delete_url", "removeProductImages");
            retObj.put("delete_type", "POST");
            retObj.put("productContentTypeId", productContentTypeId);
            retObj.put("productId", productId);
            retObj.put("size", imageData.array().length);
            /* Write the new image file */
            String targetDirectory = imageServerPath + "/" + filePathPrefix;
            try {
                File targetDir = new File(targetDirectory);
                // Create the new directory
                if (!targetDir.exists()) {
                    boolean created = targetDir.mkdirs();
                    if (!created) {
                        String errMsg = UtilProperties.getMessage(resource, "ScaleImage.unable_to_create_target_directory", locale) + " - " + targetDirectory;
                        Debug.logFatal(errMsg, module);
                        return ServiceUtil.returnError(errMsg);
                    }
                    // Delete existing image files
                    // Images are ordered by productId (${location}/${id}/${viewtype}/${sizetype})
                } else if (!filenameToUse.contains(productId)) {
                    try {
                        File[] files = targetDir.listFiles();
                        for (File file : files) {
                            if (file.isFile()) {
                                file.delete();
                            }
                        }
                    } catch (SecurityException e) {
                        Debug.logError(e, module);
                    }
                    // Images aren't ordered by productId (${location}/${viewtype}/${sizetype}/${id})
                } else {
                    try {
                        File[] files = targetDir.listFiles();
                        for (File file : files) {
                            if (file.isFile() && file.getName().startsWith(productId + "_View_" + viewNumber)) {
                                file.delete();
                            }
                        }
                    } catch (SecurityException e) {
                        Debug.logError(e, module);
                    }
                }
            } catch (NullPointerException e) {
                Debug.logError(e, module);
            }
            // Write
            String filePath = imageServerPath + "/" + fileLocation + "." + extension.getString("fileExtensionId");
            try {
                File file = new File(filePath);
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
            } catch (NullPointerException e) {
                Debug.logError(e, module);
            }
            
            //文件上传到服务器之后(备份使用)，选择是否上传到云存储
            String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
            
            
            /* scale Image in different sizes */
            Map<String, Object> resultResize = FastMap.newInstance();
            Map<String, String> qiniuImageUrlMap = FastMap.newInstance();
            String saveImagePath = imageUrlPrefix + "/" + filePathPrefix;
            String saveImageKey = (saveImagePath + "original." + extension.getString("fileExtensionId"));
            try {
                resultResize.putAll(ScaleImage.scaleImageInAllSize(context, filenameToUse, "additional", viewNumber, "products", productId, dispatcher));
                if ("qiniu".equals(uploadType)) {
                    //异步调用qiniu 上传文件的服务器
                    dispatcher.runAsync("coverUpload", UtilMisc.toMap("filePath", filePath, "fileKey", saveImageKey));
                    for (String sizeType : ScaleImage.sizeTypeList) {
                        if ("small".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/200");
                        } else if ("medium".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/400");
                        } else if ("large".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/800");
                        } else if ("detail".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/600");
                        } else if ("thumbnail".equals(sizeType)) {
                            qiniuImageUrlMap.put(sizeType, saveImageKey + "?imageView2/2/w/220");
                        }
                        
                    }
                    
                } else if ("FTP".equals(uploadType)) {
                    dispatcher.runSync("ftpUpload", UtilMisc.toMap("filePath", filePath));
                }
            } catch (IOException e) {
                Debug.logError(e, "Scale additional image in all different sizes is impossible : " + e.toString(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductImageViewScaleImpossible", UtilMisc.toMap("errorString", e.toString()), locale));
            } catch (JDOMException e) {
                Debug.logError(e, "Errors occur in parsing ImageProperties.xml : " + e.toString(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                        "ProductImageViewParsingError", UtilMisc.toMap("errorString", e.toString()), locale));
            } catch (ServiceValidationException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            } catch (ServiceAuthException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            } catch (GenericServiceException e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        result.put("retObj", retObj);
        return result;
    }
    
    public static Map<String, Object> quickAddVariant(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        Locale locale = (Locale) context.get("locale");
        String errMsg = null;
        String productId = (String) context.get("productId");
        String variantProductId = (String) context.get("productVariantId");
        String productFeatureIds = (String) context.get("productFeatureIds");
        String primaryProductCategoryId = (String) context.get("primaryProductCategoryId");
        Long prodAssocSeqNum = (Long) context.get("sequenceNum");
        
        String[] featureIds = productFeatureIds.split("\\|");
        String featureNames = "";
        for (int i = 0; i < featureIds.length; i++) {
            String featureId = featureIds[i];
            try {
                GenericValue feature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", featureId));
                featureNames += feature.getString("productFeatureName");
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        
        
        try {
            // read the product, duplicate it with the given id
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            if (product == null) {
                Map<String, String> messageMap = UtilMisc.toMap("productId", productId);
                errMsg = UtilProperties.getMessage(resourceError,
                        "productservices.product_not_found_with_ID", messageMap, locale);
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, errMsg);
                return result;
            }
            // check if product exists
            GenericValue variantProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", variantProductId));
            boolean variantProductExists = (variantProduct != null);
            if (variantProduct == null) {
                //if product does not exist
                variantProduct = GenericValue.create(product);
                variantProduct.set("productId", variantProductId);
                variantProduct.set("isVirtual", "N");
                variantProduct.set("isVariant", "Y");
                variantProduct.set("isDel","N");
                variantProduct.set("featureProductId", productFeatureIds);
                variantProduct.set("isUsedFeature", "Y");
                variantProduct.set("featureProductName", featureNames);
                variantProduct.set("mainProductId", productId);
                //create new
                variantProduct.create();
            } else {
                variantProduct = GenericValue.create(product);
                //if product does exist
                variantProduct.set("isVirtual", "N");
                variantProduct.set("isVariant", "Y");
                variantProduct.set("productId", variantProductId);
                variantProduct.set("isDel","N");
                variantProduct.set("featureProductId", productFeatureIds);
                variantProduct.set("isUsedFeature", "Y");
                variantProduct.set("featureProductName", featureNames);
                variantProduct.set("mainProductId", productId);
                //update entry
                variantProduct.store();
            }
            if (variantProductExists) {
                // Since the variant product is already a variant, first of all we remove the old features
                // and the associations of type PRODUCT_VARIANT: a given product can be a variant of only one product.
                delegator.removeByAnd("ProductAssoc", UtilMisc.toMap("productIdTo", variantProductId,
                        "productAssocTypeId", "PRODUCT_VARIANT"));
                delegator.removeByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", variantProductId,
                        "productFeatureApplTypeId", "STANDARD_FEATURE"));
            }
            // add an association from productId to variantProductId of the PRODUCT_VARIANT
            Map<String, Object> productAssocMap = UtilMisc.toMap("productId", productId, "productIdTo", variantProductId,
                    "productAssocTypeId", "PRODUCT_VARIANT",
                    "fromDate", UtilDateTime.nowTimestamp());
            if (prodAssocSeqNum != null) {
                productAssocMap.put("sequenceNum", prodAssocSeqNum);
            }
            GenericValue productAssoc = delegator.makeValue("ProductAssoc", productAssocMap);
            productAssoc.create();
            
            // add the selected standard features to the new product given the productFeatureIds
            java.util.StringTokenizer st = new java.util.StringTokenizer(productFeatureIds, "|");
            while (st.hasMoreTokens()) {
                String productFeatureId = st.nextToken();
                
                GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", productFeatureId));
                
                GenericValue productFeatureAppl = delegator.makeValue("ProductFeatureAppl",
                        UtilMisc.toMap("productId", variantProductId, "productFeatureId", productFeatureId,
                                "productFeatureApplTypeId", "STANDARD_FEATURE", "fromDate", UtilDateTime.nowTimestamp()));
                
                // set the default seq num if it's there...
                if (productFeature != null) {
                    productFeatureAppl.set("sequenceNum", productFeature.get("defaultSequenceNum"));
                }
                
                productFeatureAppl.create();
            }
            
        } catch (GenericEntityException e) {
            Debug.logError(e, "Entity error creating quick add variant data", module);
            Map<String, String> messageMap = UtilMisc.toMap("errMessage", e.toString());
            errMsg = UtilProperties.getMessage(resourceError,
                    "productservices.entity_error_quick_add_variant_data", messageMap, locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }
        result.put("productVariantId", variantProductId);
        return result;
    }
    
}
