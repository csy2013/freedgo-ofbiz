/*
 * 文件名：ArticleServices.java
 * 版权：启华
 * 描述：文章服务类
 * 修改人：gss
 * 修改时间：2015-12-28
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.systemMgr;

import java.util.Locale;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class ExceptionPageServices{
    public static final String module = ExceptionPageServices.class.getName();
    public static final String resource = "ContentUiLabels";
    /**
	 * 新增异常页面 add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createExceptionPage(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		LocalDispatcher dispatcher = dctx.getDispatcher();  
		Locale locale = (Locale) context.get("locale");
		 //异常页面
		GenericValue ExceptionPage = delegator.makeValue("ExceptionPage");
         //页面编号
		 String exceptionPageId = (String) context.get("exceptionPageId");
		 //页面标题
		 String pageTitle = (String) context.get("pageTitle");
		 //启用标识
		 String isEnabled = (String) context.get("isEnabled");
		 //异常页面描述
		 String content = (String) context.get("content");
		 ExceptionPage.set("exceptionPageId", exceptionPageId);
		 ExceptionPage.set("pageTitle", pageTitle);
		 ExceptionPage.set("isEnabled", isEnabled);
 		 Map<String, Object> passedParams = FastMap.newInstance();
 		 String contentId="";
 		 //判断是否有异常页面描述
 		 if(UtilValidate.isNotEmpty(content))
 		   {
 			passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",content
 					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
 			 try {
 				result = dispatcher.runSync("createTextContent", passedParams);
 				if(UtilValidate.isNotEmpty(result))
     		    {
 			    contentId = (String)result.get("contentId");
     		   }
 			 } catch (GenericServiceException e) {
 				e.printStackTrace();
 			}
 		   }
 		ExceptionPage.set("contentId", contentId);
 		try {
 			//创建异常页面
			ExceptionPage.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return ServiceUtil.returnSuccess();
	}
    
	/**
	 * 更新异常页面 add by gss  add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateExceptionPage(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		LocalDispatcher dispatcher = dctx.getDispatcher(); 
 		Map<String, Object> result = ServiceUtil.returnSuccess();
		 //页面编号
		 String exceptionPageId = (String) context.get("exceptionPageId");
		 //页面标题
		 String pageTitle = (String) context.get("pageTitle");
		 //启用标识
		 String isEnabled = (String) context.get("isEnabled");
		 //异常页面描述
		 String content = (String) context.get("editcontent");
		 //判断Id是否存在
		if (exceptionPageId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "NotFound", UtilMisc.toMap("exceptionPageId", ""), locale));
	        }
		//定义实体类
		GenericValue ExceptionPage;
        try {
        	ExceptionPage = delegator.findByPrimaryKey("ExceptionPage", UtilMisc.toMap("exceptionPageId", exceptionPageId));
            } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
            }
        ExceptionPage.set("pageTitle", pageTitle);
        ExceptionPage.set("isEnabled", isEnabled);
        ExceptionPage.set("pageTitle", pageTitle);
        Map<String, Object> passedParams = FastMap.newInstance();
        if(UtilValidate.isNotEmpty(ExceptionPage.get("contentId")))
        {
      	//获取内容表数据
  		GenericValue Content;
			try {
				Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", ExceptionPage.get("contentId"))));
  		if(UtilValidate.isNotEmpty(Content))
  		  {
  			GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId"))));
  			//查询文章内容
  			GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",dataResource.get("dataResourceId")));
  			//判断文章内容是否存在
  			if(UtilValidate.isNotEmpty(electronicText))
  			  {
  				if(UtilValidate.isEmpty(content))
  				  {
  					Content.remove();
  					dataResource.remove();
  					electronicText.remove();
  					ExceptionPage.set("contentId", "");
  				  }else if(!content.equals((String)electronicText.get("textData")))//判断文章内容是否修改
  				          {
  	    					//更新文章内容
  	    					electronicText.set("textData", content);
  	    					electronicText.store();
  				        }
  				
  			  }
  		  }
			} catch (GenericEntityException e) {
				e.printStackTrace();
			}
        }else if (UtilValidate.isEmpty(ExceptionPage.get("contentId"))&&content!=null)
                 {
 			passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",content
 					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
 			 try {
 				result = dispatcher.runSync("createTextContent", passedParams);
 				if(UtilValidate.isNotEmpty(result))
     		      {
 					ExceptionPage.set("contentId", (String)result.get("contentId"));
     		      }
 			 } catch (GenericServiceException e) {
 				e.printStackTrace();
 			}
          }
        try {
			ExceptionPage.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return ServiceUtil.returnSuccess();
	}

	/**
	 * 异常页面详情查询 add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findExceptionPage(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//页面编号
		String exceptionPageId = (String) context.get("exceptionPageId");
		if(UtilValidate.isNotEmpty(exceptionPageId)){
		try {
			//文章内容
			GenericValue exceptionPage = delegator.findByPrimaryKey("ExceptionPage", UtilMisc.toMap("exceptionPageId",exceptionPageId));
			//判断异常页面是否为空
			if(UtilValidate.isNotEmpty(exceptionPage))
			  {
				result.put("exceptionPage", exceptionPage);
				//判断是否有异常页面描述
				if(UtilValidate.isNotEmpty(exceptionPage.get("contentId")))
				  {
					GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", exceptionPage.get("contentId"))));
					if(UtilValidate.isNotEmpty(Content))
		    		  {
		    			GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId"))));
		    			//查询文章内容
		    			GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",dataResource.get("dataResourceId")));
		    		   if(UtilValidate.isNotEmpty(electronicText))
		    		     {
		    			   result.put("textData", electronicText.get("textData"));
		    		     }
		    		  }
				  }else{
					  result.put("textData", "");
				  }
			  }
		  }catch (Exception e) {
		  e.printStackTrace();
		  }
		}
		return result;
	}

}


