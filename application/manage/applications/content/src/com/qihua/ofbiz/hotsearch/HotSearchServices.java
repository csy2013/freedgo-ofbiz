package com.qihua.ofbiz.hotsearch;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
 * 热门搜索 service
 * @author qianjin 2016.05.09
 *
 */
public class HotSearchServices {
    
    /**
     * 热门搜索列表查询 add by qianjin 2016.05.09
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getHotSearchList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> hotSearchList = FastList.newInstance();
        
        //总记录数
        int hotSearchListSize = 0;
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
        dynamicView.addMemberEntity("HS","HotSearch");
        dynamicView.addAlias("HS","hotSearchId");
        dynamicView.addAlias("HS","hotSearchKeyName");
        dynamicView.addAlias("HS","sequenceId");
        dynamicView.addAlias("HS","isAllWebSite");
        dynamicView.addAlias("HS","isAllGeo");
		dynamicView.addAlias("HS", "productStoreId");
        
        dynamicView.addMemberEntity("HSWS","HotSearchWebSite");
        dynamicView.addViewLink("HS","HSWS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("hotSearchId", "hotSearchId"));
        
        dynamicView.addMemberEntity("WS","WebSite");
        dynamicView.addAlias("WS","siteName");
        dynamicView.addViewLink("HSWS","WS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
        
        dynamicView.addMemberEntity("HSC","HotSearchGeo");
        dynamicView.addViewLink("HS","HSC", Boolean.TRUE,ModelKeyMap.makeKeyMapList("hotSearchId", "hotSearchId"));
        
        dynamicView.addMemberEntity("G","Geo");
        dynamicView.addAlias("G","geoId");
        dynamicView.addAlias("G","geoName");
        dynamicView.addViewLink("HSC","G", Boolean.TRUE,ModelKeyMap.makeKeyMapList("geoId","geoId"));
        
        fieldsToSelect.add("hotSearchId");
        fieldsToSelect.add("hotSearchKeyName");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("isAllWebSite");
        fieldsToSelect.add("isAllGeo");
		fieldsToSelect.add("productStoreId");
        
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
        
        // 按关键字模糊查询
        if(UtilValidate.isNotEmpty(context.get("hotSearchKeyName"))) {
        	filedExprs.add(EntityCondition.makeCondition("hotSearchKeyName", EntityOperator.LIKE,"%" + context.get("hotSearchKeyName") + "%"));
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
            	String hotSearchId = gv.getString("hotSearchId");
            	String hotSearchKeyName = gv.getString("hotSearchKeyName");
            	String sequenceId = gv.getString("sequenceId");
            	String isAllWebSite = gv.getString("isAllWebSite");
            	String isAllGeo = gv.getString("isAllGeo");
            	
            	Map map = FastMap.newInstance();
            	map.put("hotSearchId", hotSearchId);
        		map.put("hotSearchKeyName", hotSearchKeyName);
        		map.put("sequenceId", sequenceId);
        		//获取站点名称
        		String webSiteName = "";
        		if("1".equals(isAllWebSite)){
        			//动态view
        	        DynamicViewEntity webSite_dv = new DynamicViewEntity();
        	        webSite_dv.addMemberEntity("HSWS","HotSearchWebSite");
        	        webSite_dv.addAlias("HSWS","hotSearchId");
        	        
        	        webSite_dv.addMemberEntity("WS","WebSite");
        	        webSite_dv.addAlias("WS","siteName");
        	        webSite_dv.addViewLink("HSWS","WS", Boolean.FALSE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
        	        
        	        //编辑where条件
        	        EntityCondition ws_whereCond = EntityCondition.makeCondition("hotSearchId", EntityOperator.EQUALS,hotSearchId);
        	        
        	        try {
                     
        	            //查询的数据Iterator
        	            EntityListIterator ws_pli = delegator.findListIteratorByCondition(webSite_dv, ws_whereCond, null, null, null, findOpts);
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
        	            e.printStackTrace();
        	        }
        	        
        		}else{
        			webSiteName = "全部站点";
        		}
        		
        		//获取地区名称
        		String geoName = "";
        		map.put("webSiteName", webSiteName);
        		map.put("geoName", geoName);
        		hotSearchList.add(map);
            }

            // 获取总记录数
            hotSearchListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > hotSearchListSize) {
                highIndex = hotSearchListSize;
            }

            //关闭 iterator
            pli.close();
		} catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            e.printStackTrace();
        } 
        
        
        //返回的参数
        result.put("recordsList",hotSearchList);
        result.put("totalSize", Integer.valueOf(hotSearchListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        
        return result;
    }
    
    /**
     * 关键字新增	add by qianjin 2016.05.09
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addHotSearch(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
        String hotSearchKeyName = (String)context.get("hotSearchKeyName");
        Long sequenceId = (Long)context.get("sequenceId");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String isAllGeo = (String)context.get("isAllGeo");
        String geo = (String)context.get("geo");
        String productStoreId = (String)context.get("productStoreId");
        try {
        	//大于当前排序的sequenceId+1
        	List<GenericValue> bannerList = delegator.findByAnd("HotSearch",UtilMisc.toMap("sequenceId", sequenceId));
        	if(UtilValidate.isNotEmpty(bannerList)){
        		try{
					//使用源生的JDBC方式  
		        	String sql = "UPDATE hot_search  set SEQUENCE_ID = SEQUENCE_ID+1 "+
		        				 "WHERE SEQUENCE_ID >= "+sequenceId;
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
        	
        	String hotSearchId = delegator.getNextSeqId("HotSearch");
            //新增一条关键词记录
        	GenericValue hs_gv = delegator.makeValue("HotSearch", UtilMisc.toMap("hotSearchId",hotSearchId));
        	hs_gv.setString("hotSearchKeyName",hotSearchKeyName);
        	hs_gv.setString("sequenceId", sequenceId.toString());
        	hs_gv.setString("isAllWebSite",isAllWebSite);
        	hs_gv.setString("isAllGeo",isAllGeo);
            hs_gv.setString("productStoreId",productStoreId);
        	hs_gv.create();
        	
        	if(UtilValidate.isNotEmpty(webSite)){
        		for(String ws_id : webSite.split(",")){
        			String hswsId = delegator.getNextSeqId("HotSearchWebSite");
        			//新增一条banner站点记录
                	GenericValue hsws_gv = delegator.makeValue("HotSearchWebSite", UtilMisc.toMap("hotSearchWebSiteId",hswsId));
                	hsws_gv.setString("hotSearchId",hotSearchId);
                	hsws_gv.setString("webSiteId",ws_id);
                	hsws_gv.create();
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(geo)){
        		for(String g_id : geo.split(",")){
        			String hscId = delegator.getNextSeqId("HotSearchGeo");
        			//新增一条关键词地区记录
                	GenericValue hsg_gv = delegator.makeValue("HotSearchGeo", UtilMisc.toMap("hotSearchGeoId",hscId));
                	hsg_gv.setString("hotSearchId",hotSearchId);
                	hsg_gv.setString("geoId",g_id);
                	hsg_gv.create();
        		}
        	}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * 根据ID查询热门搜索信息	add by qianjin 2016.05.09
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getHotSearchById(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
        String hotSearchId = (String)context.get("hotSearchId");
        
        try {
            //根据ID查找热门搜索记录
        	GenericValue hs_gv = delegator.findByPrimaryKey("HotSearch", UtilMisc.toMap("hotSearchId",hotSearchId));
        	result.put("hotSearchKeyName",hs_gv.get("hotSearchKeyName"));
        	result.put("sequenceId", hs_gv.get("sequenceId"));
        	result.put("isAllWebSite",hs_gv.get("isAllWebSite"));
        	result.put("isAllGeo",hs_gv.get("isAllGeo"));
        	
        	//获取站点
        	String ws_arr="";
        	List<GenericValue> ws_list = delegator.findByAnd("HotSearchWebSite",UtilMisc.toMap("hotSearchId", hotSearchId));
        	for(int i=0;i<ws_list.size();i++){
        		if(i != ws_list.size()-1){
        			ws_arr += ws_list.get(i).get("webSiteId")+",";
        		}else{
        			ws_arr += ws_list.get(i).get("webSiteId");
        		}
        	}
        	result.put("webSite",ws_arr);
        	
        	//获取地区
        	String g_arr="";
        	List<GenericValue> g_list = delegator.findByAnd("HotSearchWebSite",UtilMisc.toMap("hotSearchId", hotSearchId));
        	for(int i=0;i<g_list.size();i++){
        		if(i != g_list.size()-1){
        			g_arr += g_list.get(i).get("geoId")+",";
        		}else{
        			g_arr += g_list.get(i).get("geoId");
        		}
        	}
        	result.put("geo",g_arr);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * 热门搜索修改	add by qianjin 2016.05.09
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editHotSearch(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
    	String hotSearchId = (String)context.get("hotSearchId");
        String hotSearchKeyName = (String)context.get("hotSearchKeyName");
        Long sequenceId = (Long)context.get("sequenceId");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String isAllGeo = (String)context.get("isAllGeo");
        String geo = (String)context.get("geo");
        String productStoreId = (String)context.get("productStoreId");
        try {
            //根据ID查找热门搜索记录
        	GenericValue hs_gv = delegator.findByPrimaryKey("HotSearch", UtilMisc.toMap("hotSearchId",hotSearchId));
        	hs_gv.setString("hotSearchKeyName",hotSearchKeyName);
        	if(!sequenceId.equals(hs_gv.getLong("sequenceId"))){
        		//大于当前排序的sequenceId+1
            	List<GenericValue> bannerList = delegator.findByAnd("HotSearch",UtilMisc.toMap("sequenceId", sequenceId));
            	if(UtilValidate.isNotEmpty(bannerList)){
            		try{
    					//使用源生的JDBC方式  
    		        	String sql = "UPDATE hot_search b set b.SEQUENCE_ID = b.SEQUENCE_ID+1 "+
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
        	hs_gv.setString("sequenceId", sequenceId.toString());
        	hs_gv.setString("isAllWebSite",isAllWebSite);
        	hs_gv.setString("isAllGeo",isAllGeo);
            hs_gv.setString("productStoreId",productStoreId);
        	hs_gv.store();
        	
        	if(UtilValidate.isNotEmpty(webSite)){
        		//删除该热门搜索的所有站点
            	delegator.removeByAnd("HotSearchWebSite", UtilMisc.toMap("hotSearchId",hotSearchId));
        		for(String ws_id : webSite.split(",")){
        			String hswsId = delegator.getNextSeqId("HotSearchWebSite");
        			//新增一条热门搜索站点记录
                	GenericValue hsws_gv = delegator.makeValue("HotSearchWebSite", UtilMisc.toMap("hotSearchWebSiteId",hswsId));
                	hsws_gv.setString("hotSearchId",hotSearchId);
                	hsws_gv.setString("webSiteId",ws_id);
                	hsws_gv.create();
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(geo)){
        		//删除该热门搜索的所有地区
            	delegator.removeByAnd("HotSearchGeo", UtilMisc.toMap("hotSearchId",hotSearchId));
        		for(String g_id : geo.split(",")){
        			String hsgId = delegator.getNextSeqId("HotSearchGeo");
        			//新增一条热门搜索地区记录
                	GenericValue hsg_gv = delegator.makeValue("HotSearchGeo", UtilMisc.toMap("hotSearchGeoId",hsgId));
                	hsg_gv.setString("hotSearchId",hotSearchId);
                	hsg_gv.setString("geoId",g_id);
                	hsg_gv.create();
        		}
        	}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * 根据ID删除热门搜索,可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> delHotSearch(DispatchContext dctx, Map<String, ? extends Object> context) {
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
        EntityCondition mainCond = EntityCondition.makeCondition("hotSearchId", EntityOperator.IN,idList);
        
        try {
        	delegator.removeByCondition("HotSearchGeo", mainCond);
        	delegator.removeByCondition("HotSearchWebSite", mainCond);
			delegator.removeByCondition("HotSearch", mainCond);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return result;
    }
}
