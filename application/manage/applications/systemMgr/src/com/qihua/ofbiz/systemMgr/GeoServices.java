/*
 * 文件名：GeoServices.java
 * 版权：启华
 * 描述：地区管理服务类
 * 修改人：gss
 * 修改时间：2016-4-12
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.systemMgr;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.CommonWorkers;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.EntityUtilProperties;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class GeoServices{
    public static final String module = EnumerationServices.class.getName();
    public static final String resource = "ContentUiLabels";
    /**
	 * 新增地区 add by gss 2016-4-12
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createGeo(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		Map<String, Object> result = ServiceUtil.returnSuccess();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		/*地区名称*/
		String geoName = (String) context.get("geoName");
		/*排序号*/
		String sequenceNum = (String) context.get("sequenceNum");
		/*省*/
		String stateProvinceGeoId = (String) context.get("stateProvinceGeoId");
		/*市*/
		String cityGeoId = (String) context.get("cityGeoId");
		// 系统默认国家
        String country = EntityUtilProperties.getPropertyValue("general.properties", "country.geo.id.default", delegator);
		Map<String, Object> geoMap = FastMap.newInstance();
		String geoId = delegator.getNextSeqId("Geo");
		geoMap.put("geoCode", geoId);
		geoMap.put("abbreviation", geoName);
		geoMap.put("wellKnownText", geoName);
		geoMap.put("sequenceNum", Long.parseLong(sequenceNum));
		geoId="CN-"+geoId;
		geoMap.put("geoId", geoId);
		geoMap.put("geoName", geoName);
		Map<String, Object> countryAddressMap = FastMap.newInstance();
		countryAddressMap.put("geoId", geoId);
		countryAddressMap.put("geoAssocTypeId", "REGIONS");
		result.put("geoId", geoId);
		Map<String, Object> geolinkMap = FastMap.newInstance();
		//geolinkMap.put("userLogin",(GenericValue) context.get("userLogin"));
		geolinkMap.put("geoAssocTypeId", "REGIONS");
		/*省市都为空---添加省*/
		if(UtilValidate.isEmpty(stateProvinceGeoId)&&UtilValidate.isEmpty(cityGeoId)){
			geoMap.put("geoTypeId","PROVINCE");
			geolinkMap.put("geoId", country);
			geolinkMap.put("geoIdTo", geoId );
			updateSequenceNum(dctx, country, Long.parseLong(sequenceNum));
		}else if(UtilValidate.isNotEmpty(stateProvinceGeoId)&&UtilValidate.isEmpty(cityGeoId)){//添加市
			geoMap.put("geoTypeId","CITY");
			geolinkMap.put("geoId", stateProvinceGeoId);
			geolinkMap.put("geoIdTo", geoId );
			updateSequenceNum(dctx, stateProvinceGeoId, Long.parseLong(sequenceNum));
		}else if(UtilValidate.isNotEmpty(stateProvinceGeoId)&&UtilValidate.isNotEmpty(cityGeoId)){//添加区
			geoMap.put("geoTypeId","COUNTY");
			geolinkMap.put("geoId", cityGeoId);
			geolinkMap.put("geoIdTo", geoId );
			updateSequenceNum(dctx, cityGeoId, Long.parseLong(sequenceNum));
		}
		GenericValue CountryAddressFormat = delegator.makeValue("CountryAddressFormat",countryAddressMap);
		GenericValue Geo = delegator.makeValue("Geo",geoMap);
		GenericValue GeoAssoc = delegator.makeValue("GeoAssoc",geolinkMap);
		try {
			Geo.create();
			CountryAddressFormat.create();
			GeoAssoc.create();
			
			//dispatcher.runAsync("linkGeos", geolinkMap);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return ServiceUtil.returnSuccess();
	}
    
	/**
	 * 更新地区 add by gss  add by gss 2016-4-11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateGeo(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		// ID
		String geoId = (String) context.get("geoId");
		String geoIdFrom = (String) context.get("geoIdFrom");
		/* 地区名称 */
		String geoName = (String) context.get("geoName");
		/* 排序号 */
		String sequenceNum = (String) context.get("sequenceNum");
		/*仓库信息*/
		String facility = (String) context.get("facility");

		if (geoId == null) {
			return ServiceUtil.returnError(UtilProperties.getMessage(resource,
					"NotFound", UtilMisc.toMap("Geo", ""), locale));
		}
		// 定义实体类
		GenericValue Geo;
		try {
			Geo = delegator.findByPrimaryKey("Geo",
					UtilMisc.toMap("geoId", geoId));
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		Geo.set("geoName", geoName);
		Geo.set("sequenceNum", Long.parseLong(sequenceNum));
		updateSequenceNum(dctx, geoIdFrom, Long.parseLong(sequenceNum));
		try {
			Geo.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if(UtilValidate.isNotEmpty(facility)){
			String[] all_facility=facility.split(",");
			for (int i = 0; i < all_facility.length; i++) {
				String facilitys = all_facility[i];
				String[] one_facilitys = facilitys.split(":");
				GenericValue acilityCoverageArea;
				try {
					acilityCoverageArea = delegator.findByPrimaryKey("FacilityCoverageArea",
							UtilMisc.toMap("facilityCoverageAreaId", one_facilitys[0]));
				} catch (GenericEntityException ex) {
					return ServiceUtil.returnError(ex.getMessage());
				}
				if(one_facilitys.length>1){
					if(UtilValidate.isNotEmpty(one_facilitys[1])){
						acilityCoverageArea.set("sequenceId",Long.parseLong(one_facilitys[1]));
						List<GenericValue> facilityCoverageAreaNums=FastList.newInstance();
			        	try {
			        		facilityCoverageAreaNums = delegator.findByAnd("FacilityCoverageArea", UtilMisc.toMap("sequenceId",one_facilitys[1],"geoId",geoId));
						  if(UtilValidate.isNotEmpty(facilityCoverageAreaNums)){
							  acilityCoverageArea.store();
							  updatefacilitySeq(dctx, geoId, Long.parseLong(one_facilitys[1]));
						  }else{
							  acilityCoverageArea.store();  
						  }
			        	}catch (Exception e) {
							e.printStackTrace();
						} 
					}
				}
			}
		}
		return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 删除地区add by gss 2016-4-11
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> delGeo(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //删除地区Id
        String geoId = (String)context.get("geoId");
        GenericValue geo=delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", geoId));
       // List<GenericValue> geoList= CommonWorkers.getAssociatedStateList(delegator, geoId);
       if(UtilValidate.isNotEmpty(geo)){
        	result.put("status", true);
        	delegator.removeByAnd("GeoAssoc",UtilMisc.toMap("geoIdTo", geoId));
        	delegator.removeByAnd("Geo",UtilMisc.toMap("geoId", geoId));
        	delegator.removeByAnd("CountryAddressFormat",UtilMisc.toMap("geoId", geoId));
        }
        return result;
    }
    /**
	 * 字段详情查询 add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findGeoById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		//geoId
		String geoId = (String) context.get("geoId");
		try {
			//字段信息
			GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId",geoId));
			if(UtilValidate.isNotEmpty(geo)){
				result.put("geoId", geoId);
				result.put("geoName", geo.get("geoName"));
				result.put("sequenceNum", UtilValidate.isNotEmpty(geo.getLong("sequenceNum"))?geo.getLong("sequenceNum"): 0L);
			}
			GenericValue GeoAssoc= EntityUtil.getFirst(delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoIdTo", geoId)));
			if(UtilValidate.isNotEmpty(GeoAssoc)){
				GenericValue geos = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId",GeoAssoc.get("geoId")));
				if(UtilValidate.isNotEmpty(GeoAssoc)){
					result.put("geoIdFrom", GeoAssoc.get("geoId"));
					result.put("geoToName", geos.get("geoName"));
				}
			}
			
			DynamicViewEntity dynamicView = new DynamicViewEntity();
			List<String> fieldsToSelect = FastList.newInstance();
			List<String> orderBy = FastList.newInstance();
			dynamicView.addMemberEntity("FA", "Facility");
			dynamicView.addAlias("FA", "facilityName"); /*仓库名称*/
			dynamicView.addAlias("FA", "facilityId"); /*仓库名称*/
			dynamicView.addMemberEntity("FCA", "FacilityCoverageArea");
			dynamicView.addAlias("FCA", "geoId"); /*地区*/
			dynamicView.addAlias("FCA", "sequenceId"); /*地区*/
			dynamicView.addAlias("FCA", "facilityCoverageAreaId"); /*地区*/
			dynamicView.addViewLink("FA", "FCA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("facilityId"));
			fieldsToSelect.add("facilityCoverageAreaId");
			fieldsToSelect.add("facilityName");
			fieldsToSelect.add("sequenceId");
			orderBy.add("sequenceId");
			 //条件
	        EntityCondition whereCond = EntityCondition.makeCondition("geoId", EntityOperator.EQUALS, geoId);
			EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY,true);
			// using list iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView,
					whereCond, null, fieldsToSelect, orderBy, findOpts);
			List<GenericValue> List = pli.getCompleteList();
			result.put("facilityList", List);
	    }catch (Exception e) {
	         e.printStackTrace();
	    }
		return result;
	}
	
	public static Map<String, Object> findCityGeoById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		//geoId
		String geoId = (String) context.get("geoId");
		try {
			//字段信息
			GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId",geoId));
			if(UtilValidate.isNotEmpty(geo)){
				result.put("geoId", geoId);
				result.put("geoName", geo.get("geoName"));
				result.put("sequenceNum", UtilValidate.isNotEmpty(geo.getLong("sequenceNum"))?geo.getLong("sequenceNum"): 0L);
			}
			GenericValue GeoAssoc= EntityUtil.getFirst(delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoIdTo", geoId)));
			String citygeoToName=null;
			if(UtilValidate.isNotEmpty(GeoAssoc)){
				GenericValue geos = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId",GeoAssoc.get("geoId")));
				if(UtilValidate.isNotEmpty(GeoAssoc)){
					citygeoToName=(String)geos.get("geoName");
					result.put("geoIdFrom", geos.get("geoId"));
				}
			}
			String stategeoToName=null;
			String geoToName=null;
			GenericValue GeoAssocs= EntityUtil.getFirst(delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoIdTo", GeoAssoc.get("geoId"))));
			if(UtilValidate.isNotEmpty(GeoAssocs)){
				GenericValue geos = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId",GeoAssocs.get("geoId")));
				if(UtilValidate.isNotEmpty(GeoAssoc)){
					stategeoToName=(String)geos.get("geoName");
				}
			}
			geoToName=stategeoToName+"-"+citygeoToName;
			result.put("geoToName", geoToName);
			DynamicViewEntity dynamicView = new DynamicViewEntity();
			List<String> fieldsToSelect = FastList.newInstance();
			List<String> orderBy = FastList.newInstance();
			dynamicView.addMemberEntity("FA", "Facility");
			dynamicView.addAlias("FA", "facilityName"); /*仓库名称*/
			dynamicView.addAlias("FA", "facilityId"); /*仓库名称*/
			dynamicView.addMemberEntity("FCA", "FacilityCoverageArea");
			dynamicView.addAlias("FCA", "geoId"); /*地区*/
			dynamicView.addAlias("FCA", "sequenceId"); /*地区*/
			dynamicView.addAlias("FCA", "facilityCoverageAreaId"); /*地区*/
			dynamicView.addViewLink("FA", "FCA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("facilityId"));
			fieldsToSelect.add("facilityCoverageAreaId");
			fieldsToSelect.add("facilityName");
			fieldsToSelect.add("sequenceId");
			orderBy.add("sequenceId");
			 //条件
	        EntityCondition whereCond = EntityCondition.makeCondition("geoId", EntityOperator.EQUALS, geoId);
			EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY,true);
			// using list iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView,
					whereCond, null, fieldsToSelect, orderBy, findOpts);
			List<GenericValue> List = pli.getCompleteList();
			result.put("facilityList", List);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 字段详情查询 add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findGeoByName(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		// geoId
		String geoName = (String) context.get("geoName");
		String geoId = (String) context.get("geoId");
		List<String> orderBy = UtilMisc.toList("sequenceNum");
		
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		dynamicView.addMemberEntity("GA", "GeoAssoc");
		dynamicView.addAlias("GA", "geoIdFrom", "geoId", null, null, null, null);
		dynamicView.addAlias("GA", "geoAssocTypeId");
		dynamicView.addMemberEntity("GTO", "Geo");
		dynamicView.addAliasAll("GTO", null,null);
		dynamicView.addMemberEntity("GWS", "CountryAddressFormat");
		dynamicView.addViewLink("GA", "GTO", Boolean.FALSE, ModelKeyMap.makeKeyMapList("geoIdTo","geoId"));
		dynamicView.addViewLink("GA", "GWS", Boolean.FALSE, ModelKeyMap.makeKeyMapList("geoId"));
		
		
		List<EntityCondition> Exprs = FastList.newInstance();
		EntityCondition mainCond = null;
		Exprs.add(EntityCondition.makeCondition(
                EntityCondition.makeCondition("geoIdFrom", geoId),
                EntityCondition.makeCondition("geoAssocTypeId", "REGIONS"),
                EntityCondition.makeCondition(EntityOperator.OR, EntityCondition.makeCondition("geoTypeId", "STATE"), EntityCondition.makeCondition("geoTypeId", "PROVINCE"), EntityCondition.makeCondition("geoTypeId", "MUNICIPALITY"),
                        EntityCondition.makeCondition("geoTypeId", "COUNTY"),EntityCondition.makeCondition("geoTypeId","CITY"))));
		if (UtilValidate.isNotEmpty(geoName)) {
			Exprs.add(EntityCondition.makeCondition(
					EntityFunction.UPPER_FIELD("geoName"), EntityOperator.LIKE,
					EntityFunction.UPPER("%" + geoName + "%")));
		}
		// 添加where条件
		if (Exprs.size() > 0) {
			mainCond = EntityCondition.makeCondition(Exprs, EntityOperator.AND);
		}
		
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY,false);
		List<GenericValue> geoList = FastList.newInstance();
		// using list iterator
		EntityListIterator pli;
		try {
			pli = delegator.findListIteratorByCondition(dynamicView,
					mainCond, null, null, orderBy, findOpts);
			List<GenericValue> List = pli.getCompleteList();
			geoList.addAll(List);
			pli.close();
		} catch (GenericEntityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
/*		try {
			geoList.addAll(delegator.findList("GeoAssocAndGeoToWithState",
					mainCond, null, orderBy, null, true));
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}*/
		List<String> geo_List = FastList.newInstance();
		for (GenericValue geoLists :geoList) {
			geo_List.add(geoLists.get("geoName")+":"+geoLists.get("geoId"));
		}
		result.put("stateList", geo_List);
		
		return result;
	}
	
	
	/**
	 * 更新地区 add by gss  add by gss 2016-4-11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateSequenceNum(DispatchContext dctx,
			String geoId,Long sequenceNum) {
		Delegator delegator = dctx.getDelegator();
		List<String> orderBy = UtilMisc.toList("sequenceNum");
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		dynamicView.addMemberEntity("GA", "GeoAssoc");
		dynamicView.addAlias("GA", "geoIdFrom", "geoId", null, null, null, null);
		dynamicView.addAlias("GA", "geoAssocTypeId");
		dynamicView.addMemberEntity("GTO", "Geo");
		dynamicView.addAliasAll("GTO", null,null);
		dynamicView.addMemberEntity("GWS", "CountryAddressFormat");
		dynamicView.addViewLink("GA", "GTO", Boolean.FALSE, ModelKeyMap.makeKeyMapList("geoIdTo","geoId"));
		dynamicView.addViewLink("GA", "GWS", Boolean.FALSE, ModelKeyMap.makeKeyMapList("geoId"));
		List<EntityCondition> Exprs = FastList.newInstance();
		EntityCondition mainCond = null;
		Exprs.add(EntityCondition.makeCondition(
				EntityFunction.UPPER_FIELD("sequenceNum"), EntityOperator.EQUALS,
				sequenceNum));
		Exprs.add(EntityCondition.makeCondition(
                EntityCondition.makeCondition("geoIdFrom", geoId),
                EntityCondition.makeCondition("geoAssocTypeId", "REGIONS"),
                EntityCondition.makeCondition(EntityOperator.OR, EntityCondition.makeCondition("geoTypeId", "STATE"), EntityCondition.makeCondition("geoTypeId", "PROVINCE"), EntityCondition.makeCondition("geoTypeId", "MUNICIPALITY"),
                        EntityCondition.makeCondition("geoTypeId", "COUNTY"),EntityCondition.makeCondition("geoTypeId","CITY"))));
		// 添加where条件
		if (Exprs.size() > 0) {
			mainCond = EntityCondition.makeCondition(Exprs, EntityOperator.AND);
		}
		EntityFindOptions findOpts = new EntityFindOptions(true,
				EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
				EntityFindOptions.CONCUR_READ_ONLY,true);
		List<GenericValue> geoNumList = FastList.newInstance();
		// using list iterator
		EntityListIterator pli;
		try {
			pli = delegator.findListIteratorByCondition(dynamicView,
					mainCond, null, null, orderBy, findOpts);
			 geoNumList = pli.getCompleteList();
			pli.close();
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		if(UtilValidate.isEmpty(geoNumList)){
			ServiceUtil.returnSuccess();
		}else{
			List<GenericValue> geoList= CommonWorkers.getAssociatedStateList(delegator, geoId);
	    	if(UtilValidate.isNotEmpty(geoList)){
				for (GenericValue geo_List : geoList) {
					GenericValue Geo;
					try {
						Geo = delegator.findByPrimaryKey("Geo",
								UtilMisc.toMap("geoId", geo_List.get("geoId")));
						if(UtilValidate.isNotEmpty(Geo.get("sequenceNum"))){
							if(Geo.getLong("sequenceNum")>= sequenceNum){
								Geo.set("sequenceNum", Geo.getLong("sequenceNum")+1);
								Geo.store();
							}
	    				}else{
	    					continue;
	    				}
					} catch (GenericEntityException ex) {
						return ServiceUtil.returnError(ex.getMessage());
					}
				}
	    	}
		}
		return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 更新仓库编号 add by gss  add by gss 2016-4-11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updatefacilitySeq(DispatchContext dctx,
			String geoId,Long sequenceId) {
		Delegator delegator = dctx.getDelegator();
		List<String> orderBy = UtilMisc.toList("sequenceId");
		EntityCondition whereCond = EntityCondition.makeCondition("geoId", EntityOperator.EQUALS, geoId);
        try {
			List<GenericValue> facilityCoverageArea  = delegator.findList ("FacilityCoverageArea", whereCond, null, orderBy, null, false);
			if(UtilValidate.isNotEmpty(facilityCoverageArea)){
				for (GenericValue geo_List : facilityCoverageArea) {
					try {
						if(UtilValidate.isNotEmpty(geo_List.get("sequenceId"))){
							if(Objects.equals(geo_List.getLong("sequenceId"), sequenceId)){
								geo_List.set("sequenceId", geo_List.getLong("sequenceId")+1);
								geo_List.store();
							}
						}else{
							continue;
						}
					} catch (GenericEntityException ex) {
						return ServiceUtil.returnError(ex.getMessage());
					}
				}
			}
        } catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return ServiceUtil.returnSuccess();
	}

	/**
	 * 创建默认地址 add by gss 2016-1-9
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createDefaultAdd(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		String stateProvinceGeoId = (String) context.get("stateProvinceGeoId");
		String cityGeoId = (String) context.get("cityGeoId");
		String countyGeoId = (String) context.get("countyGeoId");
		Map<String, Object> result = ServiceUtil.returnSuccess();
		try {
			GenericValue defaultAddress =EntityUtil.getFirst(delegator.findByAnd ("DefaultAddress", (Object[]) null));
		    if(UtilValidate.isNotEmpty(defaultAddress)){
		    	defaultAddress.set("stateProvinceGeoId",stateProvinceGeoId );
		    	defaultAddress.set("cityGeoId",cityGeoId );
		    	defaultAddress.set("countyGeoId",countyGeoId);
		    	defaultAddress.store();
		    }else{
		    	String defaultAddressId = delegator.getNextSeqId("DefaultAddress");
		    	GenericValue DefaultAddress = delegator.makeValue("DefaultAddress");
		    	DefaultAddress.set("defaultAddressId",defaultAddressId );
		    	DefaultAddress.set("stateProvinceGeoId",stateProvinceGeoId );
		    	DefaultAddress.set("cityGeoId",cityGeoId );
		    	DefaultAddress.set("countyGeoId",countyGeoId);
		    	DefaultAddress.create();
		    }
	    } catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		return result;
	}
}


