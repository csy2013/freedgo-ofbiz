package org.ofbiz.product.catalog;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Created by changsy on 2017/10/3.
 */
public class CatalogService {
    /**
     * 获取店铺对应的分类目录
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> findCatalogByStoreId(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String storeId = (String) context.get("productStoreId");
        String name = (String) context.get("catalogName");
        Delegator delegator = dispatchContext.getDelegator();
        //catalogId获取对应的浏览根分类
        List<GenericValue> catalogCategories = null;
        try {
            List<GenericValue> storeCatalogs = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("productStoreId", storeId), UtilMisc.toList("sequenceNum")));
            List<GenericValue> catalogs = EntityUtil.getRelated("ProdCatalog", storeCatalogs);
            if (UtilValidate.isNotEmpty(name)) {
                catalogs = EntityUtil.filterByCondition(catalogs, EntityCondition.makeCondition("catalogName", EntityOperator.LIKE, "%" + name + "%"));
            }
            result.put("catalogs", catalogs);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        return result;
    }
    
    /**
     * 获取店铺对应的分类目录
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> checkCatalogNameExist(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String storeId = (String) context.get("productStoreId");
        String name = (String) context.get("catalogName");
        Delegator delegator = dispatchContext.getDelegator();
        //catalogId获取对应的浏览根分类
        List<GenericValue> catalogCategories = null;
        try {
            List<GenericValue> storeCatalogs = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("productStoreId", storeId)));
            List<GenericValue> catalogs = EntityUtil.getRelated("ProdCatalog", storeCatalogs);
            
            catalogs = EntityUtil.filterByCondition(catalogs, EntityCondition.makeCondition("catalogName", EntityOperator.EQUALS, name));
            if (UtilValidate.isNotEmpty(catalogs)) {
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
     * 新增目录及对应的店铺
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> addCatalogInStore(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dispatchContext.getDispatcher();
        String productStoreId = (String) context.get("productStoreId");
        Long sequenceNum = (Long) context.get("sequenceNum");
        try {
            result = dispatcher.runSync("createProdCatalog", context);
            if (ServiceUtil.isSuccess(result)) {
                result = dispatcher.runSync("createProductStoreCatalog", UtilMisc.toMap("productStoreId", productStoreId, "sequenceNum", sequenceNum, "prodCatalogId", result.get("prodCatalogId"), "fromDate", UtilDateTime.nowTimestamp(), "userLogin", context.get("userLogin")));
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 修改目录及对应的店铺
     */
    public static Map<String, Object> updateCatalogInStore(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dispatchContext.getDispatcher();
        Delegator delegator = dispatchContext.getDelegator();
        String productStoreId = (String) context.get("productStoreId");
        Long sequenceNum = (Long) context.get("sequenceNum");
        String prodCatalogId = (String) context.get("prodCatalogId");
        String catalogName = (String) context.get("catalogName");
        try {
            GenericValue prodCatalog = delegator.findByPrimaryKey("ProdCatalog", UtilMisc.toMap("prodCatalogId", prodCatalogId));
            prodCatalog.set("catalogName", catalogName);
            prodCatalog.store();
            
            List<GenericValue> productStoreCatalogs = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreCatalog", UtilMisc.toMap("productStoreId", productStoreId, "prodCatalogId", prodCatalogId)));
            Timestamp fromDate = null;
            GenericValue storeCatalog = delegator.makeValue("ProductStoreCatalog");
            if (UtilValidate.isNotEmpty(productStoreCatalogs)) {
                storeCatalog = productStoreCatalogs.get(0);
            } else {
                fromDate = UtilDateTime.nowTimestamp();
                storeCatalog.set("fromDate", fromDate);
            }
            storeCatalog.set("productStoreId", productStoreId);
            storeCatalog.set("sequenceNum", sequenceNum);
            storeCatalog.set("prodCatalogId", prodCatalogId);
            
            storeCatalog.store();
            
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 详细的目录信息
     *
     * @param dispatchContext
     * @param context
     * @return
     */
    public static Map<String, Object> queryCatalogById(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String prodCatalogId = (String) context.get("prodCatalogId");
        try {
            List<GenericValue> catalogs = delegator.findByAnd("ProdCatalogStore", UtilMisc.toMap("prodCatalogId", prodCatalogId));
            if (UtilValidate.isNotEmpty(catalogs)) {
                result.put("prodCatalog", catalogs.get(0));
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
    
    public static Map<String,Object> checkCatalogDel(DispatchContext dispatchContext,Map<String,? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dispatchContext.getDelegator();
        String prodCatalogId = (String) context.get("prodCatalogId");
        try {
            List<GenericValue> catalogs = delegator.findByAnd("ProdCatalogCategory", UtilMisc.toMap("prodCatalogId", prodCatalogId));
            if (UtilValidate.isNotEmpty(catalogs)) {
                result.put("canDel","0");
            }else{
                result.put("canDel","1");
            }
        
        } catch (GenericEntityException e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
    
        return result;
    }
}
