/*
 * 文件名：TagServices.java
 * 版权：启华
 * 描述：标签服务类
 * 修改人：gss
 * 修改时间：2015-12-23
 * 修改单号：
 * 修改内容：
 */

package com.qihua.ofbiz.store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastList;
import javolution.util.FastMap;



public class StoreServices{
    public static final String module = StoreServices.class.getName();
    public static final String resource = "ContentUiLabels";
    /**
	 * 新增店铺 add by gss 2017/03/23
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createStore(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		 GenericValue productStore = delegator.makeValue("ProductStore");
         String productStoreId = delegator.getNextSeqId("ProductStore");
         productStore.set("productStoreId", productStoreId);
         productStore.set("storeName", (String) context.get("storeName"));
         productStore.set("isEnabled", (String) context.get("isEnabled"));
         productStore.set("contentId", (String) context.get("contentId"));
         productStore.set("inventoryFacilityId", (String) context.get("inventoryFacilityId"));
         productStore.set("stateProvinceGeoId", (String) context.get("stateProvinceGeoId"));
         productStore.set("cityGeoId", (String) context.get("cityGeoId"));
         productStore.set("countyGeoId", (String) context.get("countyGeoId"));
         productStore.set("address", (String) context.get("address"));
         productStore.set("businessTime", (Long) context.get("businessTime"));
         productStore.set("contacts", (String) context.get("contacts"));
         productStore.set("telephone", (String) context.get("telephone"));
         productStore.set("remark", (String) context.get("remark"));
		 // Add by zhajh at 20170509 应对下单中库存验证的问题 Begin
		 // 设置默认值 “N”(应对下单中库存验证的问题)
		 productStore.set("requireInventory","N");
		 // Add by zhajh at 20170509 应对下单中库存验证的问题 End
         try {
        	 productStore.create();
		  } catch (GenericEntityException e) {
			e.printStackTrace();
		  }
		return ServiceUtil.returnSuccess();
	}
    
	
	/**
	 * 根据 店铺名称,地址,店铺编号查询 add by gss 2017-03-23
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findStoreList(DispatchContext dctx,
			Map<String, ? extends Object> context) throws GenericEntityException {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		String storeName = (String) context.get("storeName");
		String productStoreId = (String) context.get("productStoreId");
		String address = (String) context.get("address");
		int storeListSize = 0;
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
		result.put("storeName", storeName);
		result.put("productStoreId", productStoreId);
		result.put("address", address);
		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		result.put("viewSize", Integer.valueOf(viewSize));
		// blank param list
        String paramList = "";
		List<GenericValue> storeList = null;
		DynamicViewEntity dve = new DynamicViewEntity();	
		dve.addMemberEntity("PS", "ProductStore");
		dve.addAliasAll("PS", "", null);
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("storeName");
		fieldsToSelect.add("productStoreId");
		fieldsToSelect.add("remark");
		fieldsToSelect.add("isEnabled");
		fieldsToSelect.add("address");
		fieldsToSelect.add("stateProvinceGeoId");
		fieldsToSelect.add("cityGeoId");
		fieldsToSelect.add("countyGeoId");
		List<String> orderBy = FastList.newInstance();
		orderBy.add("productStoreId");
		// define the main condition & expression list
		List<EntityCondition> andExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		
		if (UtilValidate.isNotEmpty(storeName)){
			 paramList = paramList + "&storeName="+ storeName;
			andExprs.add(EntityCondition.makeCondition("storeName",
					EntityOperator.LIKE, "%" + storeName + "%"));
		}
		if (UtilValidate.isNotEmpty(productStoreId)){
			 paramList = paramList + "&productStoreId="+ productStoreId;
			andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.LIKE, "%" + productStoreId + "%"));
		}
		if (UtilValidate.isNotEmpty(address)){
			paramList = paramList + "&address="+ address;
//			dve.addMemberEntity("PG", "Geo");
//			dve.addViewLink("PS", "PG", true, ModelKeyMap.makeKeyMapList("stateProvinceGeoId", "geoId"));
//			dve.addAlias("PG", "geoName");
//			dve.addMemberEntity("PG1", "Geo");
//			dve.addViewLink("PS", "PG1", true, ModelKeyMap.makeKeyMapList("cityGeoId", "geoId"));
//			dve.addAlias("PG1", "geoName");
//			dve.addMemberEntity("PG2", "Geo");
//			dve.addViewLink("PS", "PG2", true, ModelKeyMap.makeKeyMapList("countyGeoId", "geoId"));
//			dve.addAlias("PG2", "geoName");
			DynamicViewEntity dve01 = new DynamicViewEntity();
			dve01.addMemberEntity("ps", "Geo");
			
			List<EntityCondition> geoExprs = FastList.newInstance();
			geoExprs.add(EntityCondition.makeCondition("geoName", EntityOperator.LIKE, "%" + address + "%"));

			EntityCondition mainCond01 = EntityCondition.makeCondition(geoExprs, EntityOperator.AND);
			// 获取所有相关地址
			List<GenericValue> geos = delegator.findList("Geo", mainCond01, null, null, null, false);
			List<String> geoIds = new ArrayList<String>();
			for(GenericValue gv : geos) {
				geoIds.add(gv.getString("geoId"));
			}
			andExprs.add(EntityCondition.makeCondition("address", EntityOperator.LIKE, "%" + address + "%"));
//			andExprs.add(EntityCondition.makeCondition("geoName", EntityOperator.LIKE, "%" + address + "%"));
			if(geoIds != null && geoIds.size() > 0) {
				andExprs.add(EntityCondition.makeCondition(UtilMisc.toList(
						EntityCondition.makeCondition("cityGeoId", EntityOperator.IN, geoIds),
						EntityCondition.makeCondition("countyGeoId", EntityOperator.IN, geoIds),
						EntityCondition.makeCondition("stateProvinceGeoId", EntityOperator.IN, geoIds)
						), EntityOperator.OR));
			}
		}
		   // build the main condition
        if (andExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.OR);
        }
		try {
			lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
			// set distinct on so we only get one row per order
			EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
			// using list iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, orderBy, findOpts);
			storeList = pli.getPartialList(lowIndex, viewSize);
			// attempt to get the full size
			storeListSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > storeListSize){
				highIndex = storeListSize;
			}
			// close the list iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("storeList", storeList);
		result.put("storeListSize", Integer.valueOf(storeListSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("paramList", paramList);
		return result;
	}
	
	
	/**
	 * 禁用店铺   isEnabled  == Y  add by gss 2017-03-23
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> disableStore(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String productStoreId = (String)context.get("deleteId");
        String isEnabled = (String)context.get("isEnabled");
        try {
        //判断删除Id是否为空
        if(UtilValidate.isNotEmpty(productStoreId)){
            String[] storeIds = productStoreId.split(",");
            GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", storeIds[0]));

    		productStore.set("isEnabled", isEnabled);
    		delegator.store(productStore);
            result.put("status", true);
            result.put("isEnabled", isEnabled);
            
//            List<GenericValue> storeList = delegator.findList("ProductStore", EntityCondition.makeCondition("productStoreId", EntityOperator.IN, Arrays.asList(storeIds)), null, null, null, false);
//            if(UtilValidate.isNotEmpty(storeList))
//              { 
//            	for (GenericValue productStore :storeList) {
//				}
//              }
         }
        }catch(GenericEntityException e){
            Debug.log(e.getMessage());
        }
        return result;
    }
	
	/**
	 *更新店铺信息 add by gss 2017-3-23
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateStore(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		String productStoreId = (String) context.get("productStoreId");
		//判断Id是否存在
		if (productStoreId == null){
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "StoreNotFound", UtilMisc.toMap("productStoreId", ""), locale));
	    }
		//定义实体类
		GenericValue productStore;
        try {
        	productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        //判断实体是否存在
        if (productStore != null){
        	productStore.set("storeName", (String) context.get("storeName"));
        	productStore.set("isEnabled", (String) context.get("isEnabled"));
        	productStore.set("contentId", (String) context.get("contentId"));
        	productStore.set("inventoryFacilityId", (String) context.get("inventoryFacilityId"));
        	productStore.set("stateProvinceGeoId", (String) context.get("stateProvinceGeoId"));
        	productStore.set("cityGeoId", (String) context.get("cityGeoId"));
        	productStore.set("countyGeoId", (String) context.get("countyGeoId"));
        	productStore.set("address", (String) context.get("address"));
        	productStore.set("businessTime", (Long) context.get("businessTime"));
        	productStore.set("contacts", (String) context.get("contacts"));
        	productStore.set("telephone", (String) context.get("telephone"));
        	productStore.set("remark", (String) context.get("remark"));
        	 try {
        		 productStore.store();
             } catch (GenericEntityException e) {
                 return ServiceUtil.returnError(e.getMessage());
             }
           }
        return ServiceUtil.returnSuccess();
	}

	/**
	 * 根据ID查询店铺信息add by gss 2017-3-24
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
	public static Map<String,Object> queryStore(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Locale locale = (Locale) context.get("locale");
		//店铺Id
		String productStoreId = (String)context.get("productStoreId");
		try {
			//判断删除Id是否为空
			if(UtilValidate.isNotEmpty(productStoreId)) {
				//查询店铺
				GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
				//判断店铺是否为空
				if(UtilValidate.isNotEmpty(productStore)) {
					result.put("store", productStore);
				}
			}
		}catch(GenericEntityException e){
			Debug.log(e.getMessage());
		}
		return result;
	}
}
