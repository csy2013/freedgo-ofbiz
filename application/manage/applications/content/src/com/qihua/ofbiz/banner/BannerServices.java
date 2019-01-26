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
package com.qihua.ofbiz.banner;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


/**
 * banner service
 * @author qianjin 2016/01/13
 *
 */
public class BannerServices {
    public static final String module = BannerServices.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * banner列表查询 add by qianjin 2016/01/13
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBannerList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> bannerList = FastList.newInstance();
        
        //总记录数
        int bannerListSize = 0;
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

		String productStoreId = (String) context.get("productStoreId"); // 店铺编码
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("B","Banner");
        dynamicView.addAlias("B","bannerId");
        dynamicView.addAlias("B","contentId");
        dynamicView.addAlias("B","linkUrl");
        dynamicView.addAlias("B","isUse");
        dynamicView.addAlias("B","sequenceId");
        dynamicView.addAlias("B","isAllWebSite");
        dynamicView.addAlias("B","isAllGeo");
		dynamicView.addAlias("B","productStoreId");
		dynamicView.addAlias("B","fromDate");
		dynamicView.addAlias("B","thruDate");

        dynamicView.addMemberEntity("BWS","BannerWebSite");
        dynamicView.addAlias("BWS","bannerWebSiteId");
        //dynamicView.addAlias("BWS","bannerId");
        dynamicView.addAlias("BWS","webSiteId");
        dynamicView.addViewLink("B","BWS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("bannerId", "bannerId"));
        
        dynamicView.addMemberEntity("WS","WebSite");
        dynamicView.addAlias("WS","webSiteId");
        dynamicView.addAlias("WS","siteName");
        dynamicView.addViewLink("BWS","WS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
        
        dynamicView.addMemberEntity("BC","BannerGeo");
        dynamicView.addAlias("BC","bannerGeoId");
        //dynamicView.addAlias("BC","bannerId");
        dynamicView.addAlias("BC","geoId");
        dynamicView.addViewLink("B","BC", Boolean.TRUE,ModelKeyMap.makeKeyMapList("bannerId", "bannerId"));
        
        dynamicView.addMemberEntity("C","Geo");
        dynamicView.addAlias("C","geoId");
        dynamicView.addAlias("C","geoName");
        dynamicView.addViewLink("BC","C", Boolean.TRUE,ModelKeyMap.makeKeyMapList("geoId","geoId"));
        
        fieldsToSelect.add("bannerId");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("linkUrl");
        fieldsToSelect.add("isUse");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("isAllWebSite");
        fieldsToSelect.add("isAllGeo");
		fieldsToSelect.add("productStoreId");
		fieldsToSelect.add("fromDate");
		fieldsToSelect.add("thruDate");

        //按站点名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("webSiteName"))) {
        	List<EntityCondition> exprs_list1 = FastList.newInstance();
        	exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllWebSite"), EntityOperator.EQUALS, "1"));
        	exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("siteName"), EntityOperator.LIKE, EntityFunction.UPPER("%"+context.get("webSiteName")+"%")));
        	
        	List<EntityCondition> exprs_list2 = FastList.newInstance();
        	exprs_list2.add(EntityCondition.makeCondition(exprs_list1,EntityOperator.AND));
        	exprs_list2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllWebSite"), EntityOperator.EQUALS, "0"));
        	
        	filedExprs.add(EntityCondition.makeCondition(exprs_list2,EntityOperator.OR));
        }
        
      //按地区名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("geoId")) && !"CHN".equals(context.get("geoId"))) {
        	List<EntityCondition> exprs_list1 = FastList.newInstance();
        	exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllGeo"), EntityOperator.EQUALS, "1"));
        	exprs_list1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("geoId"), EntityOperator.EQUALS, context.get("geoId")));
        	
        	List<EntityCondition> exprs_list2 = FastList.newInstance();
        	exprs_list2.add(EntityCondition.makeCondition(exprs_list1,EntityOperator.AND));
        	exprs_list2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllGeo"), EntityOperator.EQUALS, "0"));
        	
        	filedExprs.add(EntityCondition.makeCondition(exprs_list2,EntityOperator.OR));
        }

		// 店铺信息
		if(UtilValidate.isNotEmpty(productStoreId)){
			filedExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
		}
        //排序字段名称
        String sortField = "sequenceId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
        	sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
        	sortType = (String)context.get("sortType");
        }
        orderBy.add(sortType+sortField);
        
        //添加where条件
        if (filedExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }
        
        try {
        	lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            for(GenericValue gv : pli.getPartialList(lowIndex, viewSize)){
            	String bannerId = gv.getString("bannerId");
            	String contentId = gv.getString("contentId");
            	String linkUrl = gv.getString("linkUrl");
            	String isUse = gv.getString("isUse");
            	String sequenceId = gv.getString("sequenceId");
            	String isAllWebSite = gv.getString("isAllWebSite");
            	String isAllGeo = gv.getString("isAllGeo");
            	String thruDate = gv.getString("thruDate");
            	String fromDate = gv.getString("fromDate");

            	Map map = FastMap.newInstance();
            	map.put("bannerId", bannerId);
            	//根据商品ID获取广告图片url
        		//Map contentMap = dispatcher.runSync("getImageUrlByContentId", UtilMisc.toMap("contentId",contentId));
        		//String bannerImgUrl= UtilValidate.isEmpty(contentMap.get("url")) ? "" : contentMap.get("url").toString();
        		map.put("bannerImgUrl", contentId);
        		map.put("linkUrl", linkUrl);
        		map.put("isUse", isUse);
        		map.put("sequenceId", sequenceId);
        		map.put("fromDate", fromDate);
        		map.put("thruDate", thruDate);
        		//获取站点名称
        		String webSiteName = "";
        		if("1".equals(isAllWebSite)){
        			//动态view
        	        DynamicViewEntity webSite_dv = new DynamicViewEntity();
        	        webSite_dv.addMemberEntity("BWS","BannerWebSite");
        	        webSite_dv.addAlias("BWS","bannerWebSiteId");
        	        webSite_dv.addAlias("BWS","bannerId");
        	        webSite_dv.addAlias("BWS","webSiteId");
        	        
        	        webSite_dv.addMemberEntity("WS","WebSite");
        	        webSite_dv.addAlias("WS","webSiteId");
        	        webSite_dv.addAlias("WS","siteName");
        	        webSite_dv.addViewLink("BWS","WS", Boolean.FALSE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
        	        
        	        List<String> ws_fieldsToSelect = FastList.newInstance();
        	        ws_fieldsToSelect.add("bannerWebSiteId");
        	        ws_fieldsToSelect.add("bannerId");
        	        ws_fieldsToSelect.add("webSiteId");
        	        ws_fieldsToSelect.add("siteName");
        	        
        	        //编辑where条件
        	        EntityCondition ws_whereCond = EntityCondition.makeCondition("bannerId", EntityOperator.EQUALS,bannerId);
        	        
        	        try {
        	            //查询的数据Iterator
                     
        	            EntityListIterator ws_pli = delegator.findListIteratorByCondition(webSite_dv, ws_whereCond, null, ws_fieldsToSelect, null, findOpts);
        	            for(int i=0;i<ws_pli.getCompleteList().size();i++){
        	            	String siteName = ws_pli.getCompleteList().get(i).getString("siteName");
        	            	if(i == 0){
        	            		webSiteName += siteName;
        	            	}else{
        	            		webSiteName += "，"+siteName;
        	            	}
        	            }
        	            //关闭pli
        	            ws_pli.close();
        	        } catch (GenericEntityException e) {
        	            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
        	        }
        	        
        		}else{
        			webSiteName = "全部站点";
        		}
        		
        		//获取地区名称
        		String geoName = "";
        		/*if("1".equals(isAllGeo)){
        			//动态view
        	        DynamicViewEntity geo_dv = new DynamicViewEntity();
        	        geo_dv.addMemberEntity("BC","BannerGeo");
        	        geo_dv.addAlias("BC","bannerGeoId");
        	        geo_dv.addAlias("BC","bannerId");
        	        geo_dv.addAlias("BC","geoId");
        	        
        	        geo_dv.addMemberEntity("C","Geo");
        	        geo_dv.addAlias("C","geoId");
        	        geo_dv.addAlias("C","geoName");
        	        geo_dv.addViewLink("BC","C", Boolean.FALSE,ModelKeyMap.makeKeyMapList("geoId", "geoId"));
        	        
        	        List<String> geo_fieldsToSelect = FastList.newInstance();
        	        geo_fieldsToSelect.add("bannerGeoId");
        	        geo_fieldsToSelect.add("bannerId");
        	        geo_fieldsToSelect.add("geoId");
        	        geo_fieldsToSelect.add("geoName");
        	        
        	        //编辑where条件
        	        EntityCondition geo_whereCond = EntityCondition.makeCondition("bannerId", EntityOperator.EQUALS,bannerId);
        	        
        	        try {
        	            //查询的数据Iterator
        	            EntityListIterator geo_pli = delegator.findListIteratorByCondition(geo_dv, geo_whereCond, null, geo_fieldsToSelect, null, null);
        	            for(int i=0;i<geo_pli.getCompleteList().size();i++){
        	            	String name = geo_pli.getCompleteList().get(i).getString("geoName");
        	            	if(i == 0){
        	            		geoName += name;
        	            	}else{
        	            		geoName += "，"+name;
        	            	}
        	            }
        	            //关闭pli
        	            geo_pli.close();
        	        } catch (GenericEntityException e) {
        	            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
        	        }
        		}else{
        	        geoName = "全部地区";
        		}*/
        		map.put("webSiteName", webSiteName);
        		map.put("geoName", geoName);
        		bannerList.add(map);
            }

            // 获取总记录数
            bannerListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > bannerListSize) {
                highIndex = bannerListSize;
            }

            //关闭 iterator
            pli.close();
		} catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        } 
        
        
        //返回的参数
        result.put("bannerList",bannerList);
        result.put("totalSize", Integer.valueOf(bannerListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        
        return result;
    }
    
    /**
     * banner修改是否启用状态 add by qianjin 2016/01/14
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editBannerIsUse(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //广告ID
        String bannerId = (String)context.get("bannerId");
        //是否启用状态
        String isUse = (String)context.get("isUse");
        
        try {
            //根据ID获取广告记录并修改
        	GenericValue banner_gv = delegator.findByPrimaryKey("Banner", UtilMisc.toMap("bannerId",bannerId));
        	banner_gv.setString("isUse", isUse);
        	banner_gv.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }
    
    /**
     * 根据类型ID获取enum数据 add by qianjin 2016/01/14
     * @param delegator
     * @param enumTypeId
     * @return
     */
    public static List getEnumByTypeId(Delegator delegator, String enumTypeId) {
        //返回的数据List
        List enumList = FastList.newInstance();
        try {
            //根据类型ID获取enum数据
        	Map paramMap = UtilMisc.toMap("enumTypeId", enumTypeId,"enumCode" , "N");
        	List orderBy = UtilMisc.toList("sequenceId");
        	enumList = delegator.findList("Enumeration",EntityCondition.makeCondition(paramMap), null, orderBy, null, false);
        	
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return enumList;
    }

    /**
     * 根据社区获取广告列表  add by Wcy 2016.01.20
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBannersByCondition(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        List<String> communityId = (List<String>) context.get("communityId");
        List<String> bannerIdList = new ArrayList<String>();
        //获取用户绑定地区广告
        if (UtilValidate.isNotEmpty(communityId)) {
            try {
                List<GenericValue> bannerCommunity = delegator.findList("BannerAndBannerCommunity", EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition("communityId", EntityOperator.IN, communityId),
                        EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, "0")
                )), null, null, null, false);
                if(UtilValidate.isNotEmpty(bannerCommunity)){
                	for (GenericValue  bannerLists :bannerCommunity) {
                    	bannerIdList.add((String)bannerLists.get("bannerId"));
        			}
                	result.put("bannerList",bannerIdList);
                }
                
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        return result;
    }

    /**
     * 取banner表中isAllCommunity为0且是启用 add by wcy 2016.01.20
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String,Object> getIsAllCommuityBanner(DispatchContext dctx, Map<String, ? extends Object> context){
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        orderBy.add("sequenceId");  //序号升序排序
		List<GenericValue> bannerList = new ArrayList<GenericValue>();
		List<String> bannerIdList = new ArrayList<String>();
        try {
            bannerList = delegator.findList("Banner", EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("isAllCommunity", EntityOperator.EQUALS, "0"),
                            EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, "0")
                    )), null, orderBy, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e,module);
        }
		if(UtilValidate.isNotEmpty(bannerList)){
			for (GenericValue  bannerLists :bannerList) {
				bannerIdList.add((String)bannerLists.get("bannerId"));
			}
			result.put("bannerList",bannerIdList);
		}
        return result;
    }
    /**
     * banner新增	add by qianjin 2016/01/21
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> bannerAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
        String contentId = (String)context.get("contentId");
        String firstLinkType = (String)context.get("firstLinkType");
        String secondLinkType = (String)context.get("secondLinkType");
        String linkUrl = (String)context.get("linkUrl");
        String linkId = (String)context.get("linkId");
        String linkName = (String)context.get("linkName");
        Long sequenceId = (Long)context.get("sequenceId");
        String isUse = (String)context.get("isUse");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String isAllGeo = (String)context.get("isAllGeo");
        String geo = (String)context.get("geo");
		String applyScope = (String) context.get("applyScope");
		Timestamp fromDate = (Timestamp) context.get("fromDate");
		Timestamp thruDate = (Timestamp) context.get("thruDate");
		// 店铺
		String productStoreId=(String) context.get("productStoreId");
        try {
        	//大于当前排序的sequenceId+1
        	List<GenericValue> bannerList = delegator.findByAnd("Banner",UtilMisc.toMap("sequenceId", sequenceId));
        	if(UtilValidate.isNotEmpty(bannerList)){
        		try{
					//使用源生的JDBC方式  
		        	String sql = "UPDATE banner b set b.SEQUENCE_ID = b.SEQUENCE_ID+1 "+
		        				 "WHERE b.SEQUENCE_ID >= "+sequenceId;  
		        	//获得gropuHelperName  
		        	String groupHelperName = delegator.getGroupHelperName("org.ofbiz"); 
					//获得数据库的连接  
		        	Connection conn = ConnectionFactory.getConnection(groupHelperName);
		        	//获得Statement  
		        	Statement stmt = conn.createStatement();  
		        	//执行sql 
		        	stmt.executeUpdate(sql);
		        	stmt.close();
		        	conn.close();
        		} catch (SQLException e) {
    				e.printStackTrace();
    			} 
			}
        	
        	String bannerId = delegator.getNextSeqId("Banner");
            //新增一条banner记录
        	GenericValue banner_gv = delegator.makeValue("Banner", UtilMisc.toMap("bannerId",bannerId));
        	banner_gv.setString("contentId",contentId);
        	banner_gv.setString("firstLinkType",firstLinkType);
        	banner_gv.setString("secondLinkType", secondLinkType);
        	banner_gv.setString("linkUrl",linkUrl);
        	banner_gv.setString("linkId",linkId);
        	banner_gv.setString("linkName",linkName);
        	banner_gv.setString("sequenceId", sequenceId.toString());
        	banner_gv.setString("isUse",isUse);
        	banner_gv.setString("isAllWebSite",isAllWebSite);
        	banner_gv.setString("isAllGeo",isAllGeo);
//			banner_gv.setString("applyScope", applyScope);
			banner_gv.setString("productStoreId", productStoreId);
			banner_gv.set("fromDate", fromDate);
			banner_gv.set("thruDate", thruDate);
        	banner_gv.create();
        	
        	if(UtilValidate.isNotEmpty(webSite)){
        		for(String ws_id : webSite.split(",")){
        			String bwsId = delegator.getNextSeqId("BannerWebSite");
        			//新增一条banner站点记录
                	GenericValue bws_gv = delegator.makeValue("BannerWebSite", UtilMisc.toMap("bannerWebSiteId",bwsId));
                	bws_gv.setString("bannerId",bannerId);
                	bws_gv.setString("webSiteId",ws_id);
                	bws_gv.create();
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(geo)){
        		for(String c_id : geo.split(",")){
        			String bcId = delegator.getNextSeqId("BannerGeo");
        			//新增一条banner地区记录
                	GenericValue bc_gv = delegator.makeValue("BannerGeo", UtilMisc.toMap("bannerGeoId",bcId));
                	bc_gv.setString("bannerId",bannerId);
                	bc_gv.setString("geoId",c_id);
                	bc_gv.create();
        		}
        	}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * banner修改数据初始化	add by qianjin 2016/01/22
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> bannerEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
        String bannerId = (String)context.get("bannerId");
        
        try {
            //新增一条banner记录
        	GenericValue banner_gv = delegator.findByPrimaryKey("Banner", UtilMisc.toMap("bannerId",bannerId));
        	result.put("contentId",banner_gv.get("contentId"));
        	result.put("firstLinkType",banner_gv.get("firstLinkType"));
        	result.put("secondLinkType", banner_gv.get("secondLinkType"));
        	result.put("linkUrl",banner_gv.get("linkUrl"));
        	result.put("linkId",banner_gv.get("linkId"));
        	result.put("linkName",banner_gv.get("linkName"));
        	result.put("sequenceId", banner_gv.get("sequenceId"));
        	result.put("isUse",banner_gv.get("isUse"));
        	result.put("isAllWebSite",banner_gv.get("isAllWebSite"));
        	result.put("isAllGeo",banner_gv.get("isAllGeo"));
			result.put("applyScope", banner_gv.get("applyScope"));
			result.put("fromDate", banner_gv.get("fromDate"));
			result.put("thruDate", banner_gv.get("thruDate"));

        	
        	//获取站点
        	String ws_arr="";
        	List<GenericValue> ws_list = delegator.findByAnd("BannerWebSite",UtilMisc.toMap("bannerId", bannerId));
        	for(int i=0;i<ws_list.size();i++){
        		if(i != ws_list.size()-1){
        			ws_arr += ws_list.get(i).get("webSiteId")+",";
        		}else{
        			ws_arr += ws_list.get(i).get("webSiteId");
        		}
        	}
        	result.put("webSite",ws_arr);
        	
        	//获取地区
        	String c_arr="";
        	List<GenericValue> c_list = delegator.findByAnd("BannerGeo",UtilMisc.toMap("bannerId", bannerId));
        	for(int i=0;i<c_list.size();i++){
        		if(i != c_list.size()-1){
        			c_arr += c_list.get(i).get("geoId")+",";
        		}else{
        			c_arr += c_list.get(i).get("geoId");
        		}
        	}
        	result.put("geo",c_arr);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * banner修改	add by qianjin 2016/01/22
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> bannerEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
    	String bannerId = (String)context.get("bannerId");
        String contentId = (String)context.get("contentId");
        String firstLinkType = (String)context.get("firstLinkType");
        String secondLinkType = (String)context.get("secondLinkType");
        String linkUrl = (String)context.get("linkUrl");
        String linkId = (String)context.get("linkId");
        String linkName = (String)context.get("linkName");
        Long sequenceId = (Long)context.get("sequenceId");
        String isUse = (String)context.get("isUse");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String isAllGeo = (String)context.get("isAllGeo");
        String geo = (String)context.get("geo");
		String applyScope = (String) context.get("applyScope");
		Timestamp fromDate = (Timestamp) context.get("fromDate");
		Timestamp thruDate = (Timestamp) context.get("thruDate");
        try {
            //新增一条banner记录
        	GenericValue banner_gv = delegator.findByPrimaryKey("Banner", UtilMisc.toMap("bannerId",bannerId));
        	banner_gv.setString("contentId",contentId);
        	banner_gv.setString("firstLinkType",firstLinkType);
        	banner_gv.setString("secondLinkType", secondLinkType);
        	banner_gv.setString("linkUrl",linkUrl);
        	banner_gv.setString("linkId",linkId);
        	banner_gv.setString("linkName",linkName);
			banner_gv.set("fromDate", fromDate);
			banner_gv.set("thruDate", thruDate);
        	if(!sequenceId.equals(banner_gv.getLong("sequenceId"))){
        		//大于当前排序的sequenceId+1
            	List<GenericValue> bannerList = delegator.findByAnd("Banner",UtilMisc.toMap("sequenceId", sequenceId));
            	if(UtilValidate.isNotEmpty(bannerList)){
            		try{
    					//使用源生的JDBC方式  
    		        	String sql = "UPDATE banner b set b.SEQUENCE_ID = b.SEQUENCE_ID+1 "+
    		        				 "WHERE b.SEQUENCE_ID >= "+sequenceId;  
    		        	//获得gropuHelperName  
    		        	String groupHelperName = delegator.getGroupHelperName("org.ofbiz"); 
    					//获得数据库的连接  
    		        	Connection conn = ConnectionFactory.getConnection(groupHelperName);
    		        	//获得Statement  
    		        	Statement stmt = conn.createStatement();  
    		        	//执行sql 
    		        	stmt.executeUpdate(sql);
    		        	stmt.close();
    		        	conn.close();
            		} catch (SQLException e) {
        				e.printStackTrace();
        			} 
    			}
        	}
        	banner_gv.setString("sequenceId", sequenceId.toString());
        	banner_gv.setString("isUse",isUse);
        	banner_gv.setString("isAllWebSite",isAllWebSite);
        	banner_gv.setString("isAllGeo",isAllGeo);
//			banner_gv.setString("applyScope", applyScope);
        	banner_gv.store();
        	
        	if(UtilValidate.isNotEmpty(webSite)){
        		//删除该banner的所有站点
            	delegator.removeByAnd("BannerWebSite", UtilMisc.toMap("bannerId",bannerId));
        		for(String ws_id : webSite.split(",")){
        			String bwsId = delegator.getNextSeqId("BannerWebSite");
        			//新增一条banner站点记录
                	GenericValue bws_gv = delegator.makeValue("BannerWebSite", UtilMisc.toMap("bannerWebSiteId",bwsId));
                	bws_gv.setString("bannerId",bannerId);
                	bws_gv.setString("webSiteId",ws_id);
                	bws_gv.create();
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(geo)){
        		//删除该banner的所有地区
            	delegator.removeByAnd("BannerGeo", UtilMisc.toMap("bannerId",bannerId));
        		for(String c_id : geo.split(",")){
        			String bcId = delegator.getNextSeqId("BannerGeo");
        			//新增一条banner地区记录
                	GenericValue bc_gv = delegator.makeValue("BannerGeo", UtilMisc.toMap("bannerGeoId",bcId));
                	bc_gv.setString("bannerId",bannerId);
                	bc_gv.setString("geoId",c_id);
                	bc_gv.create();
        		}
        	}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * 根据ID删除banner,可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> bannerDel(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String)context.get("ids");
        //转换成list
        List idList = FastList.newInstance();
        for(String id : ids.split(",")){
        	idList.add(id);
        }
        //编辑where条件
        EntityCondition mainCond = EntityCondition.makeCondition("bannerId", EntityOperator.IN,idList);
        
        try {
        	delegator.removeByCondition("BannerGeo", mainCond);
        	delegator.removeByCondition("BannerWebSite", mainCond);
			delegator.removeByCondition("Banner", mainCond);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return result;
    }



    /**
     * 分类导航广告修改数据初始化 add by dongxiao 2016/04/20
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> navigationBannerEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String navigationId = (String)context.get("navigationId");

        try {
            //新增一条banner记录
            GenericValue banner_gv = delegator.findByPrimaryKey("NavigationBanner", UtilMisc.toMap("navigationId",navigationId));
            if (UtilValidate.isNotEmpty(banner_gv)){
                result.put("contentId",banner_gv.get("contentId"));
                result.put("firstLinkType",banner_gv.get("firstLinkType"));
                result.put("secondLinkType", banner_gv.get("secondLinkType"));
                result.put("linkUrl",banner_gv.get("linkUrl"));
                result.put("linkId",banner_gv.get("linkId"));
                result.put("linkName",banner_gv.get("linkName"));
                result.put("sequenceId", banner_gv.get("sequenceId"));
                result.put("isUse",banner_gv.get("isUse"));
                result.put("isAllWebSite",banner_gv.get("isAllWebSite"));
                result.put("isAllGeo",banner_gv.get("isAllGeo"));
                result.put("navigationId",navigationId);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 分类导航广告修改	add by dongxiao 2016/04/20
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> navigationBannerEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String navigationId = (String)context.get("navigationId");
        String contentId = (String)context.get("contentId");
        String firstLinkType = (String)context.get("firstLinkType");
        String secondLinkType = (String)context.get("secondLinkType");
        String linkUrl = (String)context.get("linkUrl");
        String linkId = (String)context.get("linkId");
        String linkName = (String)context.get("linkName");
        Long sequenceId = (Long)context.get("sequenceId");
        String isUse = (String)context.get("isUse");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String isAllGeo = (String)context.get("isAllGeo");

        try {

            GenericValue banner_gv = delegator.findByPrimaryKey("NavigationBanner", UtilMisc.toMap("navigationId",navigationId));
            if (UtilValidate.isEmpty(banner_gv)){
                banner_gv = delegator.makeValidValue("NavigationBanner",UtilMisc.toMap("navigationId",navigationId));
            }
            banner_gv.setString("contentId",contentId);
            banner_gv.setString("firstLinkType",firstLinkType);
            banner_gv.setString("secondLinkType", secondLinkType);
            banner_gv.setString("linkUrl",linkUrl);
            banner_gv.setString("linkId",linkId);
            banner_gv.setString("linkName",linkName);
            delegator.createOrStore(banner_gv);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
}
