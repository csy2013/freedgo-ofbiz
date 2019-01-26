package com.yuaoq.yabiz.product.service;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.product.ProductServices;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 2018/8/22.
 */
public class CategoryServices {
    public static final String module = CategoryServices.class.getName();
    
    /**
     * 根据目录和分类类型获取对应的一级分类
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> findTopCategories(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String catalogId = (String) context.get("catalogId");
        String categoryType = (String) context.get("categoryType");
        String categoryName = (String) context.get("categoryName");
        //根据storeId获取catelogId
        Delegator delegator = dispatchContext.getDelegator();
        try {
            //catalogId获取对应的浏览根分类
            List<GenericValue> catalogCategories = EntityUtil.filterByDate(delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", catalogId, "prodCatalogCategoryTypeId", categoryType)));
            //根据ProdCatalogCategory获取对应的productCategory 根分类
            List<GenericValue> categories = EntityUtil.getRelated("ProductCategory", catalogCategories);
            if (UtilValidate.isNotEmpty(categories)) {
                List subCategories = FastList.newInstance();
                for (int i = 0; i < categories.size(); i++) {
                    GenericValue root = categories.get(i);
                    List<GenericValue> categoryRollups = (EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryRollupAndChild", UtilMisc.toMap("parentProductCategoryId", root.get("productCategoryId")), UtilMisc.toList("sequenceNum"))));
                    subCategories.addAll(categoryRollups);
                }
                if (UtilValidate.isNotEmpty(categoryName)) {
                    subCategories = EntityUtil.filterByCondition(subCategories, EntityCondition.makeCondition("categoryName", EntityOperator.LIKE, "%" + categoryName + "%"));
                }
                result.put("categories", subCategories);
                
            }
            
            
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取当前分类下的下级分类
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> findCategoryMembers(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String categoryId = (String) context.get("categoryId");
        Delegator delegator = dispatchContext.getDelegator();
        String name = (String) context.get("categoryName");
        try {
            // blank param list
            List<EntityCondition> andExprs = FastList.newInstance();
            andExprs.add(EntityCondition.makeCondition("primaryParentCategoryId", categoryId));
            if (UtilValidate.isNotEmpty(name)) {
                andExprs.add(EntityCondition.makeCondition("categoryName", EntityOperator.LIKE, "%" + name + "%"));
            }
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                    EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
            andExprs.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
            
            EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            List<GenericValue> categories = delegator.findList("ProductCategory", mainCond, null, UtilMisc.toList("sequenceNum"), null, false);
            
            result.put("categories", categories);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    /**
     * 根据分类名称判断分类是否存在
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> checkCategoryNameExist(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String name = (String) context.get("categoryName");
        try {
            EntityCondition mainCond = EntityCondition.makeCondition("categoryName", EntityOperator.EQUALS, name);
            List<GenericValue> categories = EntityUtil.filterByDate(delegator.findList("ProductCategoryRollupAndChild", mainCond, null, null, null, false));
            if (UtilValidate.isNotEmpty(categories)) {
                result.put("isExist", "1");
            } else {
                result.put("isExist", "0");
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    /**
     * 创建分类即对应的目录
     */
    public static Map<String, Object> addCategoryInCatalog(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String categoryName = (String) context.get("categoryName");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String prodCatalogId = (String) context.get("prodCatalogId");
        
        try {
            //获取PCCT_BROWSE_ROOT ，如果没有则新增,理论上只有一个浏览根
            List<GenericValue> catalogCategories = EntityUtil.filterByDate(delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", prodCatalogId, "prodCatalogCategoryTypeId", "PCCT_BROWSE_ROOT")));
            String parentCategoryId = "";
            if (UtilValidate.isNotEmpty(catalogCategories)) {
                GenericValue catalogCagegory = catalogCategories.get(0);
                parentCategoryId = (String) catalogCagegory.get("productCategoryId");
                
            } else {
                //创建浏览根
                GenericValue category = delegator.makeValue("ProductCategory");
                category.set("categoryName", categoryName + "_BROWSE_ROOT");
                category.set("productCategoryTypeId", "CATALOG_CATEGORY");
                category.setNextSeqId();
                category = delegator.create(category);
                parentCategoryId = (String) category.get("productCategoryId");
                //创建目录与浏览根关系
                GenericValue catalogCategory = delegator.makeValue("ProdCatalogCategory");
                catalogCategory.set("sequenceNum", sequenceNum);
                catalogCategory.set("prodCatalogId", prodCatalogId);
                catalogCategory.set("productCategoryId", category.get("productCategoryId"));
                catalogCategory.set("fromDate", UtilDateTime.nowTimestamp());
                catalogCategory.set("prodCatalogCategoryTypeId", "PCCT_BROWSE_ROOT");
                delegator.create(catalogCategory);
            }
            //浏览根下的对应的分类及以浏览根之间的关系
            GenericValue category = delegator.makeValue("ProductCategory");
            category.set("categoryName", categoryName);
            category.set("productCategoryTypeId", "CATALOG_CATEGORY");
            category.setNextSeqId();
            category = delegator.createOrStore(category);
            GenericValue rollup = delegator.makeValue("ProductCategoryRollup");
            rollup.set("productCategoryId", category.get("productCategoryId"));
            rollup.set("parentProductCategoryId", parentCategoryId);
            rollup.set("fromDate", UtilDateTime.nowTimestamp());
            rollup.set("sequenceNum", sequenceNum);
            rollup.create();
            
            result.put("category", category);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 修改分类即对应的目录
     */
    public static Map<String, Object> updateCategoryInCatalog(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String categoryName = (String) context.get("categoryName");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String prodCatalogId = (String) context.get("prodCatalogId");
        String productCategoryId = (String) context.get("productCategoryId");
        
        try {
            //获取PCCT_BROWSE_ROOT ，如果没有则新增,理论上只有一个浏览根
            List<GenericValue> catalogCategories = EntityUtil.filterByDate(delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", prodCatalogId, "prodCatalogCategoryTypeId", "PCCT_BROWSE_ROOT")));
            String parentCategoryId = "";
            if (UtilValidate.isNotEmpty(catalogCategories)) {
                GenericValue catalogCagegory = catalogCategories.get(0);
                parentCategoryId = (String) catalogCagegory.get("productCategoryId");
                
            } else {
                //创建浏览根
                GenericValue category = delegator.makeValue("ProductCategory");
                category.set("categoryName", categoryName + "_BROWSE_ROOT");
                category.set("productCategoryTypeId", "CATALOG_CATEGORY");
                category.setNextSeqId();
                category = delegator.create(category);
                parentCategoryId = (String) category.get("productCategoryId");
                //创建目录与浏览根关系
                
                GenericValue catalogCategory = delegator.makeValue("ProdCatalogCategory");
                catalogCategory.set("sequenceNum", sequenceNum);
                catalogCategory.set("prodCatalogId", prodCatalogId);
                catalogCategory.set("productCategoryId", category.get("productCategoryId"));
                catalogCategory.set("fromDate", UtilDateTime.nowTimestamp());
                catalogCategory.set("prodCatalogCategoryTypeId", "PCCT_BROWSE_ROOT");
                delegator.store(catalogCategory);
            }
            //浏览根下的对应的分类及以浏览根之间的关系
            GenericValue category = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryId));
            category.set("categoryName", categoryName);
            delegator.store(category);
            //先删除，后新增
            delegator.removeByAnd("ProductCategoryRollup", UtilMisc.toMap("productCategoryId", productCategoryId));
            GenericValue rollup = delegator.makeValue("ProductCategoryRollup");
            rollup.set("productCategoryId", category.get("productCategoryId"));
            rollup.set("parentProductCategoryId", parentCategoryId);
            rollup.set("fromDate", UtilDateTime.nowTimestamp());
            rollup.set("sequenceNum", sequenceNum);
            rollup.create();
            
            result.put("category", category);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 查询分类信息
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> queryCategoryById(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String productCategoryId = (String) context.get("productCategoryId");
        try {
            List<GenericValue> categories = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryRollupAndChild", UtilMisc.toMap("productCategoryId", productCategoryId)));
            if (UtilValidate.isNotEmpty(categories)) {
                result.put("category", categories.get(0));
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    /**
     * 判断是否可以删除catalog
     */
    
    public static Map<String, Object> checkCategoryDel(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String productCategoryId = (String) context.get("productCategoryId");
        try {
            List<GenericValue> categorys = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryRollup", UtilMisc.toMap("parentProductCategoryId", productCategoryId)));
            List<GenericValue> members = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap("productCategoryId", productCategoryId)));
            
            if (UtilValidate.isNotEmpty(categorys) || UtilValidate.isNotEmpty(members)) {
                result.put("canDel", "0");
            } else {
                result.put("canDel", "1");
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 创建三级分类
     */
    public static Map<String, Object> addThreeCategory(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String categoryName = (String) context.get("categoryName");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String parentProductCategoryId = (String) context.get("parentProductCategoryId");
        
        try {
            
            //创建分类
            GenericValue category = delegator.makeValue("ProductCategory");
            category.set("categoryName", categoryName);
            category.set("productCategoryTypeId", "CATALOG_CATEGORY");
            category.setNextSeqId();
            category = delegator.create(category);
            
            //创建分类之间关系
            
            GenericValue rollup = delegator.makeValue("ProductCategoryRollup");
            rollup.set("productCategoryId", category.get("productCategoryId"));
            rollup.set("parentProductCategoryId", parentProductCategoryId);
            rollup.set("fromDate", UtilDateTime.nowTimestamp());
            rollup.set("sequenceNum", sequenceNum);
            rollup.create();
            
            result.put("category", category);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 修改三级分类
     */
    public static Map<String, Object> updateThreeCategory(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String categoryName = (String) context.get("categoryName");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String productCategoryId = (String) context.get("productCategoryId");
        String parentProductCategoryId = (String) context.get("parentProductCategoryId");
        
        try {
            //修改分类
            GenericValue category = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryId));
            category.set("categoryName", categoryName);
            category.store();
            
            //创建分类之间关系
            List<GenericValue> categories = EntityUtil.filterByDate(delegator.findByAnd("ProductCategoryRollup", UtilMisc.toMap("parentProductCategoryId", parentProductCategoryId, "productCategoryId", productCategoryId)));
            //如果之前的关系不变，则不处理，如果没有值则代表 换了父分类则删除旧关系，新增新关系
            if (UtilValidate.isEmpty(categories)) {
                delegator.removeByAnd("ProductCategoryRollup", UtilMisc.toMap("productCategoryId", productCategoryId));
                GenericValue rollup = delegator.makeValue("ProductCategoryRollup");
                rollup.set("productCategoryId", productCategoryId);
                rollup.set("parentProductCategoryId", parentProductCategoryId);
                rollup.set("fromDate", UtilDateTime.nowTimestamp());
                rollup.set("sequenceNum", sequenceNum);
                rollup.create();
            } else {
                GenericValue rollup = categories.get(0);
                rollup.set("sequenceNum", sequenceNum);
                rollup.store();
            }
            result.put("category", category);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return result;
    }
    
    public static Map<String, Object> getSpecialProducts(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher localDispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        List<String> productCategoryIds = UtilGenerics.checkList(context.get("productCategoryIds"));
        boolean limitView = Boolean.valueOf((String) context.get("limitView"));
        int defaultViewSize = (new Integer((String) context.get("defaultViewSize")));
        String siteId = (String) context.get("siteId");
        //发布时间
        Timestamp introductionDateLimit = (Timestamp) context.get("introductionDateLimit");
        //结束时间
        Timestamp releaseDateLimit = (Timestamp) context.get("releaseDateLimit");
        List<String> orderByFields = UtilGenerics.checkList(context.get("orderByFields"));
        String dataGetType = (String) context.get("dataGetType");
        String categoryType = (String) context.get("categoryType");
        
        int viewIndex = 0;
        try {
            viewIndex = Integer.valueOf((String) context.get("viewIndexString")).intValue();
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        int viewSize = defaultViewSize;
        try {
            viewSize = Integer.valueOf((String) context.get("viewSizeString")).intValue();
        } catch (Exception e) {
            viewSize = defaultViewSize;
        }
        
        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        
        if (limitView) {
            // get the indexes for the partial list
            lowIndex = ((viewIndex * viewSize) + 1);
            highIndex = (viewIndex + 1) * viewSize;
        } else {
            lowIndex = 0;
            highIndex = 0;
        }
        
        boolean limitView1 = limitView;
        int defaultViewSize1 = defaultViewSize;
        if ("first".equals(dataGetType)) {
            limitView1 = true;
            defaultViewSize1 = 1;
        }
        if ("appoint".equals(dataGetType) || "random".equals(dataGetType)) {
            limitView1 = false;
            defaultViewSize1 = 50;
            //最多50个
        }
        List resultProdCateMembers = FastList.newInstance();
        boolean useCacheForMembers = (context.get("useCacheForMembers") == null || ((Boolean) context.get("useCacheForMembers")).booleanValue());
        boolean activeOnly = (context.get("activeOnly") == null || (Boolean.valueOf((String) context.get("activeOnly"))));
        if (UtilValidate.isNotEmpty(productCategoryIds)) {
            for (int i = 0; i < productCategoryIds.size(); i++) {
                String categoryId = productCategoryIds.get(i);
                Map categoryMap = UtilMisc.toMap("defaultViewSize", defaultViewSize1, "limitView", limitView1, "activeOnly", activeOnly, "useCacheForMembers", useCacheForMembers, "productCategoryId", categoryId, "introductionDateLimit", introductionDateLimit, "releaseDateLimit", releaseDateLimit, "orderByFields", orderByFields);
                try {
                    Map resultData = localDispatcher.runSync("getProductCategoryAndLimitedMembers", categoryMap);
                    if (resultData.get("productCategoryMembers") != null) {
                        resultProdCateMembers.addAll((List) resultData.get("productCategoryMembers"));
                    }
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
            
        } else {
            //获取所有促销 目录下(PCCT_PROMOTIONS)的分类
            List<GenericValue> catalogs = catalogs = CatalogWorker.getStoreCatalogsBySiteId(siteId, delegator);
            if (UtilValidate.isNotEmpty(catalogs)) {
                for (int i = 0; i < catalogs.size(); i++) {
                    GenericValue catalog = catalogs.get(i);
                    String categoryId = "";
                    if ("PCCT_PROMOTIONS".equals(categoryType)) {
                        categoryId = CatalogWorker.getCatalogCategoryIdByTypeId(delegator, (String) catalog.get("prodCatalogId"), "PCCT_PROMOTIONS");
                    } else if ("PCCT_BEST_SELL".equals(categoryType)) {
                        categoryId = CatalogWorker.getCatalogCategoryIdByTypeId(delegator, (String) catalog.get("prodCatalogId"), "PCCT_BEST_SELL");
                    } else if ("PCCT_MOST_POPULAR".equals(categoryType)) {
                        categoryId = CatalogWorker.getCatalogCategoryIdByTypeId(delegator, (String) catalog.get("prodCatalogId"), "PCCT_MOST_POPULAR");
                    } else if ("PCCT_WHATS_NEW".equals(categoryType)) {
                        categoryId = CatalogWorker.getCatalogCategoryIdByTypeId(delegator, (String) catalog.get("prodCatalogId"), "PCCT_WHATS_NEW");
                    }
                    if (UtilValidate.isNotEmpty(categoryId)) {
                        Map categoryMap = UtilMisc.toMap("defaultViewSize", defaultViewSize1, "limitView", limitView1, "activeOnly", activeOnly, "useCacheForMembers", useCacheForMembers, "productCategoryId", categoryId, "introductionDateLimit", introductionDateLimit, "releaseDateLimit", releaseDateLimit, "orderByFields", orderByFields);
                        try {
                            Map resultData = localDispatcher.runSync("getProductCategoryAndLimitedMembers", categoryMap);
                            resultProdCateMembers.addAll((List) resultData.get("productCategoryMembers"));
                        } catch (GenericServiceException e) {
                            e.printStackTrace();
                            return ServiceUtil.returnError(e.getMessage());
                        }
                    }
                }
            }
        }
        // set the index and size
        listSize = resultProdCateMembers.size();
        if (highIndex > listSize) {
            highIndex = listSize;
        }
        
        // get only between low and high indexes
        if (limitView) {
            if (UtilValidate.isNotEmpty(resultProdCateMembers)) {
                if ("random".equals(dataGetType)) {
                    int[] randomInts = null;
                    if (viewSize <= resultProdCateMembers.size()) {
                        randomInts = UtilNumber.randomInt(viewSize, resultProdCateMembers.size());
                    } else {
                        randomInts = UtilNumber.randomInt(resultProdCateMembers.size(), resultProdCateMembers.size());
                    }
                    List randomProdCateMembers = FastList.newInstance();
                    for (int i = 0; i < randomInts.length; i++) {
                        int randomInt = randomInts[i];
                        randomProdCateMembers.add(resultProdCateMembers.get(randomInt - 1));
                    }
                    resultProdCateMembers = randomProdCateMembers;
                } else {
                    resultProdCateMembers = resultProdCateMembers.subList(lowIndex - 1, highIndex);
                }
            }
        } else {
            lowIndex = 1;
            highIndex = listSize;
            //
            if (UtilValidate.isNotEmpty(resultProdCateMembers)) {
                List randomProdCateMembers = FastList.newInstance();
                int[] randomInts = UtilNumber.randomInt(resultProdCateMembers.size(), resultProdCateMembers.size());
                for (int i = 0; i < randomInts.length; i++) {
                    int randomInt = randomInts[i];
                    randomProdCateMembers.add(resultProdCateMembers.get(randomInt - 1));
                }
                resultProdCateMembers = randomProdCateMembers;
            }
        }
        
        
        result.put("viewIndex", Integer.valueOf(viewIndex));
        result.put("viewSize", Integer.valueOf(viewSize));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("listSize", Integer.valueOf(listSize));
        
        if (resultProdCateMembers != null) {
            result.put("productCategoryMembers", resultProdCateMembers);
        }
        return result;
    }
    
    /**
     * 商品等级分类列表的取得
     *
     * @return
     */
    public static Map<String, Object> getInitProductCategoryByLevel(DispatchContext dcx, Map<String, ? extends Object> context) throws GenericEntityException {
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        
        List<GenericValue> productCategoryLevel1List = FastList.newInstance();
        List<GenericValue> productCategoryLevel2List = FastList.newInstance();
        List<GenericValue> productCategoryLevel3List = FastList.newInstance();
        GenericValue productCategoryLevel1Info = null;
        GenericValue productCategoryLevel2Info = null;
        GenericValue productCategoryLevel3Info = null;
        orderBy.add("sequenceNum");
        
        
        String productStoreId = (String) context.get("productStoreId");
        
        entityConditionList = FastList.newInstance();
        entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("1")));
        
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null),
                EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
        if (UtilValidate.isNotEmpty(productStoreId)) {
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
            if (UtilValidate.isNotEmpty(productStoreId)) {
                entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            }
            
            productCategoryLevel2List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
            
            if (productCategoryLevel2List.size() > 0) {
                productCategoryLevel2Info = productCategoryLevel2List.get(0);
                entityConditionList = FastList.newInstance();
                entityConditionList.add(EntityCondition.makeCondition("productCategoryLevel", Long.parseLong("3")));
                entityConditionList.add(EntityCondition.makeCondition("primaryParentCategoryId", productCategoryLevel2Info.getString("productCategoryId")));
                entityConditionList.add(EntityCondition.makeCondition(exprs, EntityOperator.OR));//非删除的商品
                if (UtilValidate.isNotEmpty(productStoreId)) {
                    entityConditionList.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                }
                
                productCategoryLevel3List = delegator.findList("ProductCategory", EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, orderBy, null, true);
                if (productCategoryLevel3List.size() > 0) {
                    productCategoryLevel3Info = productCategoryLevel3List.get(0);
                }
            }
        }
        resultData.put("productCategoryLevel1List", productCategoryLevel1List);
        resultData.put("productCategoryLevel2List", productCategoryLevel2List);
        resultData.put("productCategoryLevel3List", productCategoryLevel3List);
        resultData.put("productCategoryLevel1Info", productCategoryLevel1Info);
        resultData.put("productCategoryLevel2Info", productCategoryLevel2Info);
        resultData.put("productCategoryLevel3Info", productCategoryLevel3Info);
        return resultData;
    }
    
    public static  Map<String,Object> updateProductCategory(DispatchContext dcx,Map<String,? extends Object> context){
        String categoryId = (String)context.get("productCategoryId");
        Delegator delegator = dcx.getDelegator();
        try {
            GenericValue productCategory = delegator.findByPrimaryKey("ProductCategory",UtilMisc.toMap("productCategoryId",categoryId));
            productCategory.setNonPKFields(context);
            productCategory.store();
           
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return ServiceUtil.returnSuccess();
    }
    
    
    /**
     * 商品分类的更新处理
     *
     * @param request
     * @param response
     * @return
     * @throws org.ofbiz.entity.GenericEntityException
     */
    public static Map<String, Object> updateProductCategoryIcoPro(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String productCategoryId = (String) context.get("productCategoryId");//分类ID
        String categoryName = (String) context.get("categoryName"); //分类名称
        String primaryParentCategoryId = (String) context.get("primaryParentCategoryId"); //分类父级ID
        String sequenceNum = (String) context.get("sequenceNum");//排序号
        String isHasExtendAttr = (String) context.get("isHasExtendAttr");//是否有扩展属性
        String operateType = (String) context.get("operateType");//操作类型
        String productCategoryLevel = (String) context.get("productCategoryLevel");//分类级别
        String extendAttrInfos = (String) context.get("extendAttrInfos"); //商品分类的扩展属性
        
        String contentId = (String) context.get("contentId"); //商品分类的图片;
        String productStroeId=(String) context.get("productStoreId"); // 店铺编码
        String fileName = (String) context.get("_uploadedFile_fileName");
        String contentType = (String) context.get("_uploadedFile_contentType");
        ByteBuffer imageData = (ByteBuffer) context.get("uploadedFile");
        String productContentTypeId = (String) context.get("productContentTypeId");
        String productId = (String) context.get("productId");
        LocalDispatcher dispatcher = dcx.getDispatcher();
        Delegator delegator = dcx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        
        List<Map<String, Object>> productCategoryAttrList = FastList.newInstance();
        List<Map<String, Object>> productCategoryAttrOptionList = FastList.newInstance();
        List<EntityCondition> entityConditionList = FastList.newInstance();
        if ("create".equals(operateType) || "update".equals(operateType)) {
            try {
                List<String> orderBy = UtilMisc.toList("sequenceNum");
                List<GenericValue> productCategoryAll = delegator.findList("ProductCategory", EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, Long.parseLong(productCategoryLevel)), null, orderBy, null, false);
                
                if (UtilValidate.isEmpty(sequenceNum)) {
                    String errMsg = "序列号不能为空，请输入序列号";
                    return ServiceUtil.returnError(errMsg);
                } else {
                    List<GenericValue> productCategorySeqNums = delegator.findByAnd("ProductCategory", UtilMisc.toMap("sequenceNum", Long.parseLong(sequenceNum), "productCategoryLevel", Long.parseLong(productCategoryLevel)));
                    if (UtilValidate.isNotEmpty(productCategorySeqNums)) {
                        for (GenericValue productCategory : productCategoryAll) {
                            if (productCategory.getLong("sequenceNum") >= Long.parseLong(sequenceNum)) {
                                productCategory.set("sequenceNum", productCategory.getLong("sequenceNum") + 1);
                                productCategory.store();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            
            // 取得商品分类扩展属性的值
            if (UtilValidate.isNotEmpty(extendAttrInfos)) {
                String[] tExtendAttrInfosArray = extendAttrInfos.split(",");
                for (String ExtendAttrInfo : tExtendAttrInfosArray) {
                    String[] attrInfos = ExtendAttrInfo.split("\\|");
                    String attrName = attrInfos[0];//属性名
                    String isRequired = attrInfos[1];//是否必填
                    String curOptionType = attrInfos[2];//操作类型
                    String extendOptions = attrInfos[3];//可选项
                    Map<String, Object> mapTemp;
                    if (UtilValidate.isNotEmpty(attrName) && UtilValidate.isNotEmpty(isRequired)) {
                        mapTemp = FastMap.newInstance();
                        mapTemp.put("attrName", attrName);
                        mapTemp.put("isRequired", isRequired);
                        mapTemp.put("curOptionType", curOptionType);
                        mapTemp.put("extendOptions", extendOptions);
                        productCategoryAttrList.add(mapTemp);
                    }
                }
            }
            // 品牌分类名称输入验证
            if (UtilValidate.isEmpty(categoryName)) {
                String errMsg = "品牌分类名称不能为空";
                return ServiceUtil.returnError(errMsg);
            }
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
//	        }
        }
        if (UtilValidate.isEmpty(productCategoryId) && "create".equals(operateType)) {
            try {
                String productCategoryIdCreate = delegator.getNextSeqId("ProductCategory");
                GenericValue productCategory = null;
                productCategory = delegator.makeValue("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryIdCreate));
                // 商品分类名称
                productCategory.set("categoryName", categoryName);
                // 商品分类上级分类
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
                // 商品分类图片
                if (UtilValidate.isNotEmpty(contentId)) {
                    productCategory.set("contentId", contentId);
                }
                
                // 级别
                productCategory.set("productCategoryLevel", Long.parseLong(productCategoryLevel));
                
                // 店铺编码
                if(UtilValidate.isNotEmpty(productStroeId)) {
                    productCategory.set("productStoreId", productStroeId);
                }
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

//	       	            	 // 扩展属性的可选项
//	       	            	 String extendOptions=(String)map.get("extendOptions");
//	       	            	 String[] options = extendOptions.split("\\^");
//	       	            	 for (int i = 0; i < options.length; i++) {
//								String productOptionIdCreate = delegator.getNextSeqId("ProductOption");
//		   	            	    GenericValue productOption = null;
//		   	            	    productOption = delegator.makeValue("ProductOption",UtilMisc.toMap("productOptionId",productOptionIdCreate));
//		   	            	    // 产品分类ID
//		   	            	    productOption.set("productCategoryId", productCategoryIdCreate);
//		      	            	// 产品分类属性名称
//		   	            	    productOption.set("attrName", (String)map.get("attrName"));
//		      	            	// 选项名称
//		   	            	    productOption.set("optionName", options[i]);
//		      	                // 创建表
//		   	            	    productOption.create();
//							 }
                            // 扩展属性的可选项
                            String extendOptions = (String) map.get("extendOptions");
                            String[] options = extendOptions.split("\\^");
                            for (String optionInfo : options) {
                                String[] attrInfos = optionInfo.split("\\*");
                                String optionInfoForOption = attrInfos[0];//选项名称
                                String optionInfoForProductOptionId = attrInfos[1];//选项ID
                                String optiocurOptionOptionType = attrInfos[2];//操作类型
                                if ("create".equals(optiocurOptionOptionType)) {
                                    String productOptionIdCreate = delegator.getNextSeqId("ProductOption");
                                    GenericValue productOption = null;
                                    productOption = delegator.makeValue("ProductOption", UtilMisc.toMap("productOptionId", productOptionIdCreate));
                                    // 产品分类ID
                                    productOption.set("productCategoryId", productCategoryIdCreate);
                                    // 产品分类属性名称
                                    productOption.set("attrName", (String) map.get("attrName"));
                                    // 选项名称
                                    productOption.set("optionName", optionInfoForOption);
                                    // 创建表
                                    productOption.create();
                                }
                            }
                            
                            
                        }
                    }
                }
                List categoryFeatureTypeId = (List) context.get("categoryFeatureTypeId");
                
                if(UtilValidate.isNotEmpty(categoryFeatureTypeId)){
        
                    for (int i = 0; i < categoryFeatureTypeId.size(); i++) {
                        String featureTypeId = (String) categoryFeatureTypeId.get(i);
                        GenericValue featureCateAppl = delegator.makeValue("ProductFeatureCategoryAppl",UtilMisc.toMap("productCategoryId",productCategoryIdCreate,"productFeatureCategoryId",featureTypeId,"fromDate",UtilDateTime.nowTimestamp()));
                        featureCateAppl.create();
            
                    }
                }
            } catch (Exception e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
    
            
        } else {
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                try {
                    
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
                            
                            // 商品分类图片
                            if (UtilValidate.isNotEmpty(contentId)) {
                                productCategoryUpdate.set("contentId", contentId);
                            }
                            productCategoryUpdate.store();
                            
                            if ("Y".equals(isHasExtendAttr)) {
                                
                                //                 List<GenericValue> productCategoryAttributeDelList = null;
                                //                 List<GenericValue> productOptionDelList = null;
                                //                 productCategoryAttributeDelList = delegator.findList("ProductCategoryAttribute", EntityCondition.makeCondition("productCategoryId", productCategoryId), null, null, null, true);
                                //                 productOptionDelList = delegator.findList("ProductOption", EntityCondition.makeCondition("productCategoryId", productCategoryId), null, null, null, true);
                                //                 if(UtilValidate.isNotEmpty(productOptionDelList)){
                                //                 	delegator.removeAll(productOptionDelList);
                                //                 }
                                //                 if(UtilValidate.isNotEmpty(productCategoryAttributeDelList)){
                                //                 	delegator.removeAll(productCategoryAttributeDelList);
                                //                 }
                                
                                if (productCategoryAttrList.size() > 0) {
                                    for (Map<String, Object> map : productCategoryAttrList) {
                                        String attrName = (String) map.get("attrName");
                                        String isRequired = (String) map.get("isRequired");
                                        String curOptionType = (String) map.get("curOptionType");
                                        String extendOptions = (String) map.get("extendOptions");
                                        
                                        if ("create".equals(curOptionType)) {
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
                                            String[] options = extendOptions.split("\\^");
                                            for (String optionInfo : options) {
                                                String[] attrInfos = optionInfo.split("\\*");
                                                String optionInfoForOption = attrInfos[0];//选项名称
                                                String optionInfoForProductOptionId = attrInfos[1];//选项ID
                                                String optiocurOptionOptionType = attrInfos[2];//操作类型
                                                if ("create".equals(optiocurOptionOptionType)) {
                                                    String productOptionIdCreate = delegator.getNextSeqId("ProductOption");
                                                    GenericValue productOption = null;
                                                    productOption = delegator.makeValue("ProductOption", UtilMisc.toMap("productOptionId", productOptionIdCreate));
                                                    // 产品分类ID
                                                    productOption.set("productCategoryId", productCategoryId);
                                                    // 产品分类属性名称
                                                    productOption.set("attrName", (String) map.get("attrName"));
                                                    // 选项名称
                                                    productOption.set("optionName", optionInfoForOption);
                                                    // 创建表
                                                    productOption.create();
                                                } else if ("update".equals(optiocurOptionOptionType)) {
                                                    GenericValue productOptionUpdate = delegator.findByPrimaryKey("ProductOption", UtilMisc.toMap("productOptionId", optionInfoForProductOptionId));
                                                    if (UtilValidate.isNotEmpty(productOptionUpdate)) {
                                                        // 产品分类ID
                                                        productOptionUpdate.set("productCategoryId", productCategoryId);
                                                        // 产品分类属性名称
                                                        productOptionUpdate.set("attrName", (String) map.get("attrName"));
                                                        // 选项名称
                                                        productOptionUpdate.set("optionName", optionInfoForOption);
                                                        productOptionUpdate.store();
                                                    }
                                                } else if ("delete".equals(optiocurOptionOptionType)) {
                                                    GenericValue productOptionDel = delegator.findByPrimaryKey("ProductOption", UtilMisc.toMap("productOptionId", optionInfoForProductOptionId));
                                                    if (UtilValidate.isNotEmpty(productOptionDel)) {
                                                        productOptionDel.remove();
                                                    }
                                                } else {
                                                }
                                            }
                                            
                                        } else if ("update".equals(curOptionType)) {
                                            // 产品分类ID
                                            GenericValue productCategoryAttributeUpdate = delegator.findByPrimaryKey("ProductCategoryAttribute", UtilMisc.toMap("productCategoryId", productCategoryId, "attrName", attrName));
                                            if (UtilValidate.isNotEmpty(productCategoryAttributeUpdate)) {
                                                // 产品分类ID
                                                productCategoryAttributeUpdate.set("productCategoryId", productCategoryId);
                                                // 产品分类属性名称
                                                productCategoryAttributeUpdate.set("attrName", attrName);
                                                // 是否必填
                                                productCategoryAttributeUpdate.set("isRequired", isRequired);
                                                // 更新表
                                                productCategoryAttributeUpdate.store();
                                                
                                                // 扩展属性的可选项
                                                String[] options = extendOptions.split("\\^");
                                                for (String optionInfo : options) {
                                                    String[] attrInfos = optionInfo.split("\\*");
                                                    String optionInfoForOption = attrInfos[0];//选项名称
                                                    String optionInfoForProductOptionId = attrInfos[1];//选项ID
                                                    String optiocurOptionOptionType = attrInfos[2];//操作类型
                                                    if ("create".equals(optiocurOptionOptionType)) {
                                                        String productOptionIdCreate = delegator.getNextSeqId("ProductOption");
                                                        GenericValue productOption = null;
                                                        productOption = delegator.makeValue("ProductOption", UtilMisc.toMap("productOptionId", productOptionIdCreate));
                                                        // 产品分类ID
                                                        productOption.set("productCategoryId", productCategoryId);
                                                        // 产品分类属性名称
                                                        productOption.set("attrName", (String) map.get("attrName"));
                                                        // 选项名称
                                                        productOption.set("optionName", optionInfoForOption);
                                                        // 创建表
                                                        productOption.create();
                                                    } else if ("update".equals(optiocurOptionOptionType)) {
                                                        GenericValue productOptionUpdate = delegator.findByPrimaryKey("ProductOption", UtilMisc.toMap("productOptionId", optionInfoForProductOptionId));
                                                        if (UtilValidate.isNotEmpty(productOptionUpdate)) {
                                                            // 产品分类ID
                                                            productOptionUpdate.set("productCategoryId", productCategoryId);
                                                            // 产品分类属性名称
                                                            productOptionUpdate.set("attrName", (String) map.get("attrName"));
                                                            // 选项名称
                                                            productOptionUpdate.set("optionName", optionInfoForOption);
                                                            productOptionUpdate.store();
                                                        }
                                                    } else if ("delete".equals(optiocurOptionOptionType)) {
                                                        GenericValue productOptionDel = delegator.findByPrimaryKey("ProductOption", UtilMisc.toMap("productOptionId", optionInfoForProductOptionId));
                                                        if (UtilValidate.isNotEmpty(productOptionDel)) {
                                                            productOptionDel.remove();
                                                        }
                                                    } else {
                                                    }
                                                }
                                            }
                                        } else if ("delete".equals(curOptionType)) {
                                            if (UtilValidate.isNotEmpty(productCategoryId)) {
                                                GenericValue productCategoryAttributeInfoDel = null;
                                                List<GenericValue> productOptionInfoDelList = null;
                                                productCategoryAttributeInfoDel = delegator.findByPrimaryKey("ProductCategoryAttribute", UtilMisc.toMap("productCategoryId", productCategoryId, "attrName", attrName));
                                                productOptionInfoDelList = delegator.findList("ProductOption", EntityCondition.makeCondition("productCategoryId", productCategoryId), null, null, null, true);
                                                
                                                if (UtilValidate.isNotEmpty(productOptionInfoDelList)) {
                                                    delegator.removeAll(productOptionInfoDelList);
                                                }
                                                if (UtilValidate.isNotEmpty(productCategoryAttributeInfoDel)) {
                                                    productCategoryAttributeInfoDel.remove();
                                                }
                                            }
                                        } else {
                                        }
                                    }
                                    
                                }
                                 /*


                                 // 商品分类的扩展属性数据的登陆
                             	if(productCategoryAttrList.size()>0){
                                   	 for (Map<String,Object> map : productCategoryAttrList) {
                        	            	 GenericValue productCategoryAttribute = null;
                        	            	 productCategoryAttribute = delegator.makeValue("ProductCategoryAttribute");
                        	            	 // 产品分类ID
                        	            	 productCategoryAttribute.set("productCategoryId", productCategoryId);
                        	            	 // 产品分类属性名称
                        	            	 productCategoryAttribute.set("attrName", (String)map.get("attrName"));
                        	            	 // 是否必填
                        	            	 productCategoryAttribute.set("isRequired", (String)map.get("isRequired"));
                        	                 // 创建表
                        	            	 productCategoryAttribute.create();

                        	            	 // 扩展属性的可选项
                        	            	 String extendOptions=(String)map.get("extendOptions");
                        	            	 String[] options = extendOptions.split("\\^");
                        	            	 for (int i = 0; i < options.length; i++) {
                 							String productOptionIdCreate = delegator.getNextSeqId("ProductOption");
                 	   	            	    GenericValue productOption = null;
                 	   	            	    productOption = delegator.makeValue("ProductOption",UtilMisc.toMap("productOptionId",productOptionIdCreate));
                 	   	            	    // 产品分类ID
                 	   	            	    productOption.set("productCategoryId", productCategoryId);
                 	      	            	// 产品分类属性名称
                 	   	            	    productOption.set("attrName", (String)map.get("attrName"));
                 	      	            	// 选项名称
                 	   	            	    productOption.set("optionName", options[i]);
                 	      	                // 创建表
                 	   	            	    productOption.create();
                 						 }
                                   	 }
                                 }*/
                            }
                        }
    
                        List categoryFeatureTypeId = (List) context.get("categoryFeatureTypeId");
                        delegator.removeByAnd("ProductFeatureCategoryAppl",UtilMisc.toMap("productCategoryId",productCategoryId));
                        if(UtilValidate.isNotEmpty(categoryFeatureTypeId)){
                            
                            for (int i = 0; i < categoryFeatureTypeId.size(); i++) {
                                String featureTypeId = (String) categoryFeatureTypeId.get(i);
                                GenericValue featureCateAppl = delegator.makeValue("ProductFeatureCategoryAppl",UtilMisc.toMap("productCategoryId",productCategoryId,"productFeatureCategoryId",featureTypeId,"fromDate",UtilDateTime.nowTimestamp()));
                                featureCateAppl.create();
            
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
                            delegator.removeByAnd("ProductFeatureCategoryAppl",UtilMisc.toMap("productCategoryId",productCategoryId));
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
}
