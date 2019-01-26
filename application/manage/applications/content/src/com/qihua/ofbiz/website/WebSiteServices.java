/*
 * 文件名：WebSiteServices.java
 * 版权：启华
 * 描述：站点服务类
 * 修改人：gss
 * 修改时间：2015-12-23
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.website;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

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
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;


public class WebSiteServices{
    public static final String module = WebSiteServices.class.getName();
    public static final String resource = "ContentUiLabels";
    
	/**
	 * 根据站点名称查询 add by gss 2015/12/21
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findWebSiteByName(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		String siteName = (String) context.get("siteName1");
		int webSiteListSize = 0;
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
		result.put("siteName1", siteName);

		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		result.put("viewSize", Integer.valueOf(viewSize));
		// blank param list
        String paramList = "";
		
		List<GenericValue> webSiteList = null;
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("WS", "WebSite");
		dve.addAliasAll("WS", "", null);
		// 网站编号
		dve.addAlias("WS", "webSiteId");
		// 网站名称
		dve.addAlias("WS", "siteName");
		// 网站地址
		dve.addAlias("WS", "httpHost");
		// 网站标题
		dve.addAlias("WS", "siteTitle");
		// 网站备注
		dve.addAlias("WS", "siteRemark");
		dve.addAlias("WS", "siteKeyword");
		dve.addAlias("WS", "siteAbstract");
		dve.addAlias("WS", "siteDescription");
		dve.addAlias("WS", "isEnabled");
		dve.addAlias("WS","sequenceId");
		//dve.addAlias("WS", "productStoreId");
		
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("webSiteId");
		fieldsToSelect.add("siteName");
		fieldsToSelect.add("httpHost");
		fieldsToSelect.add("siteTitle");
		fieldsToSelect.add("siteRemark");
		fieldsToSelect.add("siteKeyword");
		fieldsToSelect.add("siteAbstract");
		fieldsToSelect.add("siteDescription");
		fieldsToSelect.add("isEnabled");
		fieldsToSelect.add("sequenceId");
		//fieldsToSelect.add("productStoreId");

		List<String> orderBy = FastList.newInstance();
		orderBy.add("sequenceId");

		// define the main condition & expression list
		List<EntityCondition> andExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		paramList = paramList + "&siteName1=";
		if (UtilValidate.isNotEmpty(siteName))
		   {
			 paramList = paramList  + siteName;
			andExprs.add(EntityCondition.makeCondition("siteName",
					EntityOperator.LIKE, "%" + siteName + "%"));
		   }
		   // build the main condition
        if (andExprs.size() > 0)
           {
        	mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
           }
		try {
			lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
			// set distinct on so we only get one row per order
			EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
			// using list iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dve,
					mainCond, null, fieldsToSelect, orderBy, findOpts);
			webSiteList = pli.getPartialList(lowIndex, viewSize);
			// attempt to get the full size
			webSiteListSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > webSiteListSize)
			   {
				highIndex = webSiteListSize;
			   }
			// close the list iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("webSites", webSiteList);
		result.put("webSiteListSize", Integer.valueOf(webSiteListSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("paramList", paramList);
		return result;
	}

	
	/**
	 * 修改站点启用状态 add by gss 2015/12/21
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> editSiteStatus(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//站点ID
		String webSiteId = (String) context.get("webSiteId");
		//站点是否启用
		String isEnabled = (String) context.get("isEnabled");
		//判断标签Id是否存在
		if (webSiteId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "TagTypeNotFound", UtilMisc.toMap("tagTypeId", ""), locale));
	        }
		//定义实体类
		GenericValue webSite;
        try { 
        	webSite = delegator.findByPrimaryKey("WebSite", UtilMisc.toMap("webSiteId", webSiteId));
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        //判断实体是否存在
        if (webSite != null) 
           {
        	if(isEnabled!=null&& "Y".equals(isEnabled))
        	  { 
        		webSite.set("isEnabled", "N");
        	  }
        	if(isEnabled!=null&& "N".equals(isEnabled))
        	  { 
        		webSite.set("isEnabled", "Y");
        	  }
        	try {
       		 webSite.store();
       	     //定义实体类
     		 GenericValue webSite1 = delegator.findByPrimaryKey("WebSite", UtilMisc.toMap("webSiteId", webSiteId));
             result.put("status", true);
             result.put("isEnabled", webSite1.get("isEnabled"));
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            }
           else
            {
        	   result.put("status", false);
            }
        return result;
	}
	/**
	 * 上下移动站点 add by gss 2015/12/21
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> moveWebSite(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		//Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//站点ID
		String webSiteId = (String) context.get("webSiteId");
		//对换的站点ID
		String nextwebSiteId = (String) context.get("nextwebSiteId");
		//判断标签Id是否存在
		if (webSiteId == null || nextwebSiteId==null) 
		{
			return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
					"TagTypeNotFound", UtilMisc.toMap("tagTypeId", ""), locale));
		}
		String nextsequenceId=null;	
		String sequenceId=null;
		//定义实体类
		GenericValue webSite;
		try {
			webSite = delegator.findByPrimaryKey("WebSite", UtilMisc.toMap("webSiteId", webSiteId));
			 sequenceId=webSite.getString("sequenceId");
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		//定义实体类
		GenericValue nextwebSite;
		try {
			nextwebSite = delegator.findByPrimaryKey("WebSite", UtilMisc.toMap("webSiteId", nextwebSiteId));
			 nextsequenceId=nextwebSite.getString("sequenceId");	
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		//判断实体是否存在
		if (webSite != null&&nextwebSite != null) 
		   {
			webSite.set("sequenceId", nextsequenceId);
			nextwebSite.set("sequenceId", sequenceId);
			try {
				webSite.store();
				nextwebSite.store();
			} catch (GenericEntityException e) {
				return ServiceUtil.returnError(e.getMessage());
			}
		}
		return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 删除站点
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> deleteWebSite(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        String deleteId = (String)context.get("deleteId");
        try {
        if(UtilValidate.isNotEmpty(deleteId)) {
            String[] webSiteIds = deleteId.split(",");
                List<GenericValue> webSiteList = delegator.findList("WebSite", EntityCondition.makeCondition("webSiteId", EntityOperator.IN, Arrays.asList(webSiteIds)), null, null, null, false);
                delegator.removeAll(webSiteList);
        }
        }catch(GenericEntityException e){
            Debug.log(e.getMessage());
            result.put("status",true);
            result.put("info","删除失败");
        }
        result.put("status",true);
        result.put("info","删除成功");
        return result;
    }
   
    
    /**
     * 
     * @param dctx
     * @param context
     * @return
     * @throws GenericServiceException
     * @throws GenericEntityException
     */
    public static Map<String,Object> FindWebSiteById(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
    	Delegator delegator = dctx.getDelegator();
    	Map<String, Object> result = ServiceUtil.returnSuccess();
    	Locale locale = (Locale) context.get("locale");
    	String webSiteId = (String)context.get("webSiteId");
    	GenericValue webSite=null;
    	try {
    		if(UtilValidate.isNotEmpty(webSiteId)) {
    			webSite = delegator.findByPrimaryKey("WebSite", UtilMisc.toMap("webSiteId", webSiteId));
    		}
    	}catch(GenericEntityException e){
    		Debug.log(e.getMessage());
    	}
    	result.put("status",true);
    	result.put("webSite",webSite);
    	return result;
    }
}
