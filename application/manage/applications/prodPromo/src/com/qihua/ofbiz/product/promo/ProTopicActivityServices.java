package com.qihua.ofbiz.product.promo;

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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;


/**
 * banner service
 * @author qianjin 2016.02.22
 *
 */
public class ProTopicActivityServices {
    public static final String module = ProTopicActivityServices.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * 专题活动列表查询 add by qianjin 2016.02.22
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getTopicActivityList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> recordsList = FastList.newInstance();
        
        //总记录数
        int totalSize = 0;
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
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("PTA","ProductTopicActivity");
        dynamicView.addAlias("PTA","productTopicActivityId");
        dynamicView.addAlias("PTA","topicActivityName");
        dynamicView.addAlias("PTA","linkType");
        dynamicView.addAlias("PTA","linkName");
        dynamicView.addAlias("PTA","isUse");
        dynamicView.addAlias("PTA","sequenceId");
        dynamicView.addAlias("PTA","isAllWebSite");
        dynamicView.addAlias("PTA","isAllCommunity");
		dynamicView.addAlias("PTA","tagId");
        
        dynamicView.addMemberEntity("PTAWS","ProductTopicActivityWebSite");
        dynamicView.addViewLink("PTA","PTAWS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("productTopicActivityId", "productTopicActivityId"));
        
        dynamicView.addMemberEntity("WS","WebSite");
        dynamicView.addAlias("WS","siteName");
        dynamicView.addViewLink("PTAWS","WS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
        
        dynamicView.addMemberEntity("PTAC","ProductTopicActivityCommunity");
        dynamicView.addViewLink("PTA","PTAC", Boolean.TRUE,ModelKeyMap.makeKeyMapList("productTopicActivityId", "productTopicActivityId"));
        
        dynamicView.addMemberEntity("C","Community");
        dynamicView.addAlias("C","name");
        dynamicView.addViewLink("PTAC","C", Boolean.TRUE,ModelKeyMap.makeKeyMapList("communityId", "communityId"));
        
        fieldsToSelect.add("productTopicActivityId");
        fieldsToSelect.add("topicActivityName");
        fieldsToSelect.add("linkType");
        fieldsToSelect.add("linkName");
        fieldsToSelect.add("isUse");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("isAllWebSite");
        fieldsToSelect.add("isAllCommunity");
		fieldsToSelect.add("tagId");
        
        //按专题名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("topicActivityName"))) {
        	filedExprs.add(EntityCondition.makeCondition("topicActivityName", EntityOperator.LIKE, "%"+context.get("topicActivityName")+"%"));
        }
        
        //按站点名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("webSiteName"))) {
        	if((context.get("webSiteName").toString().indexOf("全部")!=-1) ||
					(context.get("webSiteName").toString().indexOf("全部站点")!=-1)||
					(context.get("webSiteName").toString().indexOf("全")!=-1) ||
					(context.get("webSiteName").toString().indexOf("部")!=-1)){
				filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllWebSite"), EntityOperator.EQUALS, "0"));
			}else{
				filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllWebSite"), EntityOperator.EQUALS, "1"));
				filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("siteName"), EntityOperator.LIKE, EntityFunction.UPPER("%"+context.get("webSiteName")+"%")));
			}

        }
        
        //按社区名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("communityName"))) {
        	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("isAllCommunity"), EntityOperator.EQUALS, "1"));
        	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("name"), EntityOperator.LIKE, EntityFunction.UPPER("%"+context.get("communityName")+"%")));
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
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            for(GenericValue gv : pli.getPartialList(lowIndex, viewSize)){
            	String productTopicActivityId = gv.getString("productTopicActivityId");
            	String topicActivityName = gv.getString("topicActivityName");
            	String linkType = gv.getString("linkType");
            	String linkName = gv.getString("linkName");
            	String isUse = gv.getString("isUse");
            	String sequenceId = gv.getString("sequenceId");
            	String isAllWebSite = gv.getString("isAllWebSite");
            	String isAllCommunity = gv.getString("isAllCommunity");
            	
            	Map map = FastMap.newInstance();
            	map.put("productTopicActivityId", productTopicActivityId);
        		map.put("topicActivityName", topicActivityName);
        		map.put("linkType", linkType);
        		map.put("linkName", linkName);
        		map.put("isUse", isUse);
        		map.put("sequenceId", sequenceId);
        		//获取站点名称
        		String webSiteName = "";
        		if("1".equals(isAllWebSite)){
        			//动态view
        	        DynamicViewEntity webSite_dv = new DynamicViewEntity();
        	        webSite_dv.addMemberEntity("PTAWS","ProductTopicActivityWebSite");
        	        webSite_dv.addAlias("PTAWS", "productTopicActivityId");
        	        
        	        webSite_dv.addMemberEntity("WS","WebSite");
        	        webSite_dv.addAlias("WS", "siteName");
        	        webSite_dv.addViewLink("PTAWS","WS", Boolean.FALSE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
        	        
        	        List<String> ws_fieldsToSelect = FastList.newInstance();
        	        ws_fieldsToSelect.add("siteName");
        	        
        	        //编辑where条件
        	        EntityCondition ws_whereCond = EntityCondition.makeCondition("productTopicActivityId", EntityOperator.EQUALS,productTopicActivityId);
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
        		
        		//获取社区名称
        		String communityName = "";
        		if("1".equals(isAllCommunity)){
        			//动态view
        	        DynamicViewEntity community_dv = new DynamicViewEntity();
        	        community_dv.addMemberEntity("PTAC","ProductTopicActivityCommunity");
        	        community_dv.addAlias("PTAC", "productTopicActivityId");
        	        
        	        community_dv.addMemberEntity("C","Community");
        	        community_dv.addAlias("C","name");
        	        community_dv.addViewLink("PTAC","C", Boolean.FALSE,ModelKeyMap.makeKeyMapList("communityId", "communityId"));
        	        
        	        List<String> community_fieldsToSelect = FastList.newInstance();
        	        community_fieldsToSelect.add("name");
        	        
        	        //编辑where条件
        	        EntityCondition community_whereCond = EntityCondition.makeCondition("productTopicActivityId", EntityOperator.EQUALS,productTopicActivityId);
        	        try {
        	            //查询的数据Iterator
        	            EntityListIterator community_pli = delegator.findListIteratorByCondition(community_dv, community_whereCond, null, community_fieldsToSelect, null, null);
        	            for(int i=0;i<community_pli.getCompleteList().size();i++){
        	            	String name = community_pli.getCompleteList().get(i).getString("name");
        	            	if(i == 0){
        	            		communityName += name;
        	            	}else{
        	            		communityName += "，"+name;
        	            	}
        	            }
        	            //关闭pli
        	            community_pli.close();
        	        } catch (GenericEntityException e) {
        	            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
        	        }
        		}else{
        	        communityName = "全部社区";
        		}
        		map.put("webSiteName", webSiteName);
        		map.put("communityName", communityName);
        		recordsList.add(map);
            }

            // 获取总记录数
            totalSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > totalSize) {
                highIndex = totalSize;
            }

            //关闭 iterator
            pli.close();
		} catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        } 
        
        
        //返回的参数
        result.put("recordsList",recordsList);
        result.put("totalSize", Integer.valueOf(totalSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        
        return result;
    }
    
    /**
     * 根据专题活动ID修改启用状态 add by qianjin 2016.02.22
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editIsUseByProTopicActivityId(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //专题活动ID
        String productTopicActivityId = (String)context.get("productTopicActivityId");
        //是否启用状态
        String isUse = (String)context.get("isUse");
        
        try {
            //根据专题活动ID修改启用状态
        	GenericValue pta_gv = delegator.findByPrimaryKey("ProductTopicActivity", UtilMisc.toMap("productTopicActivityId",productTopicActivityId));
        	pta_gv.setString("isUse", isUse);
        	pta_gv.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }

    /**
     * 专题活动新增	add by qianjin 2016.02.22
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> topicActivityAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
    	String topicActivityName = (String)context.get("topicActivityName");
        String smallImg = (String)context.get("smallImg");
        String bigImg = (String)context.get("bigImg");
        String linkType = (String)context.get("linkType");
        String linkUrl = (String)context.get("linkUrl");
        String linkId = (String)context.get("linkId");
        String linkName = (String)context.get("linkName");
        Long sequenceId = (Long)context.get("sequenceId");
        String isUse = (String)context.get("isUse");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String isAllCommunity = (String)context.get("isAllCommunity");
        String community = (String)context.get("community");
		String tagId = (String)context.get("tagId");
        
        try {
        	//大于当前排序的sequenceId+1
			try {
				//判断该序号是否已存在
				List<GenericValue> pta_list = delegator.findByAnd("ProductTopicActivity", UtilMisc.toMap("sequenceId", sequenceId));
				if(UtilValidate.isNotEmpty(pta_list)){
					//使用源生的JDBC方式  
		        	String sql = "UPDATE product_topic_activity pta set pta.SEQUENCE_ID = pta.SEQUENCE_ID+1 "+
		        				 "WHERE pta.SEQUENCE_ID >= "+sequenceId;  
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
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}  
        	
        	String productTopicActivityId = delegator.getNextSeqId("ProductTopicActivity");
            //新增一条专题活动记录
        	GenericValue pta_gv = delegator.makeValue("ProductTopicActivity", UtilMisc.toMap("productTopicActivityId",productTopicActivityId));
        	pta_gv.setString("topicActivityName",topicActivityName);
        	pta_gv.setString("smallImg",smallImg);
        	pta_gv.setString("bigImg", bigImg);
        	pta_gv.setString("linkType", linkType);
        	pta_gv.setString("linkUrl",linkUrl);
        	pta_gv.setString("linkId",linkId);
        	pta_gv.setString("linkName",linkName);
        	pta_gv.setString("sequenceId", sequenceId.toString());
        	pta_gv.setString("isUse",isUse);
        	pta_gv.setString("isAllWebSite",isAllWebSite);
        	pta_gv.setString("isAllCommunity",isAllCommunity);
			pta_gv.setString("tagId",tagId);
        	pta_gv.create();
        	
        	if(UtilValidate.isNotEmpty(webSite)){
        		for(String ws_id : webSite.split(",")){
        			String ptawsId = delegator.getNextSeqId("ProductTopicActivityWebSite");
        			//新增一条专题活动站点记录
                	GenericValue ptaws_gv = delegator.makeValue("ProductTopicActivityWebSite", UtilMisc.toMap("productTopicActivityWebSiteId",ptawsId));
                	ptaws_gv.setString("productTopicActivityId",productTopicActivityId);
                	ptaws_gv.setString("webSiteId",ws_id);
                	ptaws_gv.create();
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(community)){
        		for(String c_id : community.split(",")){
        			String ptacId = delegator.getNextSeqId("ProductTopicActivityCommunity");
        			//新增一条专题活动社区记录
                	GenericValue ptac_gv = delegator.makeValue("ProductTopicActivityCommunity", UtilMisc.toMap("productTopicActivityCommunityId",ptacId));
                	ptac_gv.setString("productTopicActivityId",productTopicActivityId);
                	ptac_gv.setString("communityId",c_id);
                	ptac_gv.create();
        		}
        	}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * 根据ID获取专题活动信息	add by qianjin 2016.02.23
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getTopicActivityById(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
        String productTopicActivityId = (String)context.get("productTopicActivityId");
        
        try {
            //新增一条专题活动记录
        	GenericValue pta_gv = delegator.findByPrimaryKey("ProductTopicActivity", UtilMisc.toMap("productTopicActivityId",productTopicActivityId));
        	result.put("productTopicActivityId",productTopicActivityId);
        	result.put("topicActivityName",pta_gv.get("topicActivityName"));
        	result.put("smallImg",pta_gv.get("smallImg"));
        	result.put("bigImg",pta_gv.get("bigImg"));
        	result.put("linkType",pta_gv.get("linkType"));
        	result.put("linkUrl",pta_gv.get("linkUrl"));
        	result.put("linkId",pta_gv.get("linkId"));
        	result.put("linkName",pta_gv.get("linkName"));
        	result.put("sequenceId", pta_gv.get("sequenceId"));
        	result.put("isUse",pta_gv.get("isUse"));
        	result.put("isAllWebSite",pta_gv.get("isAllWebSite"));
        	result.put("isAllCommunity",pta_gv.get("isAllCommunity"));
			result.put("tagId",pta_gv.get("tagId"));
        	
        	//获取站点
        	String ws_arr="";
        	List<GenericValue> ws_list = delegator.findByAnd("ProductTopicActivityWebSite",UtilMisc.toMap("productTopicActivityId", productTopicActivityId));
        	for(int i=0;i<ws_list.size();i++){
        		if(i != ws_list.size()-1){
        			ws_arr += ws_list.get(i).get("webSiteId")+",";
        		}else{
        			ws_arr += ws_list.get(i).get("webSiteId");
        		}
        	}
        	result.put("webSite",ws_arr);
        	
        	//获取社区
        	String c_arr="";
        	List<GenericValue> c_list = delegator.findByAnd("ProductTopicActivityCommunity",UtilMisc.toMap("productTopicActivityId", productTopicActivityId));
        	for(int i=0;i<c_list.size();i++){
        		if(i != c_list.size()-1){
        			c_arr += c_list.get(i).get("communityId")+",";
        		}else{
        			c_arr += c_list.get(i).get("communityId");
        		}
        	}
        	result.put("community",c_arr);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * 专题活动修改	add by qianjin 2016.02.23
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> topicActivityEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
    	String productTopicActivityId = (String)context.get("productTopicActivityId");
    	String topicActivityName = (String)context.get("topicActivityName");
        String smallImg = (String)context.get("smallImg");
        String bigImg = (String)context.get("bigImg");
        String linkType = (String)context.get("linkType");
        String linkUrl = (String)context.get("linkUrl");
        String linkId = (String)context.get("linkId");
        String linkName = (String)context.get("linkName");
        Long sequenceId = (Long)context.get("sequenceId");
        String isUse = (String)context.get("isUse");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String isAllCommunity = (String)context.get("isAllCommunity");
        String community = (String)context.get("community");
		String tagId = (String)context.get("tagId");
        
        try {
            //新增一条专题活动记录
        	GenericValue pta_gv = delegator.findByPrimaryKey("ProductTopicActivity", UtilMisc.toMap("productTopicActivityId",productTopicActivityId));
        	pta_gv.setString("topicActivityName",topicActivityName);
        	pta_gv.setString("smallImg",smallImg);
        	pta_gv.setString("bigImg", bigImg);
        	pta_gv.setString("linkType", linkType);
        	pta_gv.setString("linkUrl",linkUrl);
        	pta_gv.setString("linkId",linkId);
        	pta_gv.setString("linkName",linkName);
        	if(!sequenceId.equals(pta_gv.getLong("sequenceId"))){
        		//大于当前排序的sequenceId+1
    			try {
    				//判断该序号是否已存在
    				List<GenericValue> pta_list = delegator.findByAnd("ProductTopicActivity", UtilMisc.toMap("sequenceId", sequenceId));
    				if(UtilValidate.isNotEmpty(pta_list)){
    					//使用源生的JDBC方式  
    		        	String sql = "UPDATE product_topic_activity pta set pta.SEQUENCE_ID = pta.SEQUENCE_ID+1 "+
    		        				 "WHERE pta.SEQUENCE_ID >= "+sequenceId;  
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
    				}
    			} catch (SQLException e) {
    				e.printStackTrace();
    			}
        	}
			pta_gv.setString("sequenceId", sequenceId.toString());
        	pta_gv.setString("isUse",isUse);
        	pta_gv.setString("isAllWebSite",isAllWebSite);
        	pta_gv.setString("isAllCommunity",isAllCommunity);
			pta_gv.setString("tagId",tagId);
        	pta_gv.store();
        	
        	if(UtilValidate.isNotEmpty(webSite)){
        		//删除该专题活动的所有站点
            	delegator.removeByAnd("ProductTopicActivityWebSite", UtilMisc.toMap("productTopicActivityId",productTopicActivityId));
        		for(String ws_id : webSite.split(",")){
        			String bwsId = delegator.getNextSeqId("ProductTopicActivityWebSite");
        			//新增一条专题活动站点记录
        			GenericValue bws_gv = delegator.makeValue("ProductTopicActivityWebSite", UtilMisc.toMap("productTopicActivityWebSiteId",bwsId));
        			bws_gv.setString("productTopicActivityId",productTopicActivityId);
        			bws_gv.setString("webSiteId",ws_id);
        			bws_gv.create();
        		}
        	}
        	
        	if(UtilValidate.isNotEmpty(community)){
        		//删除该专题活动的所有社区
            	delegator.removeByAnd("ProductTopicActivityCommunity", UtilMisc.toMap("productTopicActivityId",productTopicActivityId));
        		for(String c_id : community.split(",")){
        			String bcId = delegator.getNextSeqId("ProductTopicActivityCommunity");
        			//新增一条专题活动社区记录
                	GenericValue bc_gv = delegator.makeValue("ProductTopicActivityCommunity", UtilMisc.toMap("productTopicActivityCommunityId",bcId));
                	bc_gv.setString("productTopicActivityId",productTopicActivityId);
                	bc_gv.setString("communityId",c_id);
                	bc_gv.create();
        		}
        	}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} 
        return result;
    }
    
    /**
     * 根据ID删除专题活动,可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> topicActivityDel(DispatchContext dctx, Map<String, ? extends Object> context) {
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
        EntityCondition mainCond = EntityCondition.makeCondition("productTopicActivityId", EntityOperator.IN,idList);
        
        try {
        	delegator.removeByCondition("ProductTopicActivityCommunity", mainCond);
        	delegator.removeByCondition("ProductTopicActivityWebSite", mainCond);
			delegator.removeByCondition("ProductTopicActivity", mainCond);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return result;
    }
}
