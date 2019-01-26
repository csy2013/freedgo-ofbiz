/*
 * 文件名：PayMentConfigServices.java
 * 版权：启华
 * 描述：支付接口服务类
 * 修改人：gss
 * 修改时间：2016-1-11
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


public class PayMentConfigServices{
    public static final String module = PayMentConfigServices.class.getName();
    public static final String resource = "ContentUiLabels";
    
	/**
	 * 支付宝详情查询 add by gss 2016-1-14
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> queryAliPay(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//支付宝类型Id
		String paymentGatewayConfigId = (String) context.get("paymentGatewayConfigId");
		if(UtilValidate.isNotEmpty(paymentGatewayConfigId))
		{
		try {
			//支付宝配置信息
			GenericValue AliPay = delegator.findByPrimaryKey("PaymentGatewayAliPay", UtilMisc.toMap("paymentGatewayConfigId",paymentGatewayConfigId));
			//支付类型信息
			GenericValue AliPaytype = delegator.findByPrimaryKey("PaymentGatewayConfig", UtilMisc.toMap("paymentGatewayConfigId",paymentGatewayConfigId));
			if(UtilValidate.isNotEmpty(AliPaytype))
			{
				//支付类型
				result.put("payType", AliPaytype.get("description"));	
			}
			
			//判断支付宝配置信息是否为空
			if(UtilValidate.isNotEmpty(AliPay))
			{
				result.put("aliPay", AliPay);
				//支付问题关联表ID
				String  desContentId=(String)AliPay.get("desContentId");
			    //是否有支付问题描述
				if(UtilValidate.isNotEmpty(desContentId))
			    {
				  //获取内容表数据
		    	  GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", desContentId)));
		    	  if(UtilValidate.isNotEmpty(Content))
		    	  {
		    		  GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId"))));
		    			if(UtilValidate.isNotEmpty(dataResource))
		    			{
		    				//支付问题描述内容
				    		  GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",dataResource.get("dataResourceId"))); 
				    			if(UtilValidate.isNotEmpty(electronicText))
				    			{
				    				result.put("textData", electronicText.get("textData"));
				    			}
		    			}
		    	  }
			    }
				//图片关联ID
				String  imgContentId=(String)AliPay.get("imgContentId");
			    //判断是否有支付图标
				if(UtilValidate.isNotEmpty(imgContentId))
			    {
					  //获取内容表数据
			    	 GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", imgContentId)));
			    	 if(UtilValidate.isNotEmpty(Content))
			    	 {
			    		 GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId")))); 
			    	   if(UtilValidate.isNotEmpty(dataResource))
			    	   {
			    		   //获取图片路径
			    		   result.put("objectInfo", dataResource.get("objectInfo"));
			    	   }
			    	 }
			    }
			}
		    }catch (Exception e) {
		         e.printStackTrace();
		    }
		}
		return result;
	}
    
	
	/**
	 * 更新支付宝接口信息 add by gss 2016-1-14
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateAliPay(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		LocalDispatcher dispatcher = dctx.getDispatcher(); 
 		Map<String, Object> result = ServiceUtil.returnSuccess();
		//支付信息接口ID
		String paymentGatewayConfigId = (String) context.get("paymentGatewayConfigId");
		//支付名称
		String paymentName = (String) context.get("paymentName");
		//是否启用
	    String isEnabled = (String) context.get("isEnabled");
	    //是否默认
	    String isDefault = (String) context.get("isDefault");
	    //Api-Key
	    String apiKey = (String) context.get("apiKey");
	    //Secret-key
	    String secretKey = (String) context.get("secretKey");
	    //收款账号
	    String payAccount = (String) context.get("payAccount");
	    //后台回调地址
	    String payUrl = (String) context.get("payUrl");
	    //前台回调地址
	    String backUrl = (String) context.get("backUrl");
	    //手机支付回调
	    String mobileBindUrl = (String) context.get("mobileBindUrl");
	    //支付描述
	    String description = (String) context.get("description");
	    //图标
	    String contentId = (String) context.get("contentId");
	    //支付问题描述
	    String textData = (String) context.get("textData");
	    
		if (paymentGatewayConfigId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "NotFound", UtilMisc.toMap("paymentGatewayConfigId", ""), locale));
	        }
		//定义实体类
		GenericValue AliPay;
        try {
        	AliPay = delegator.findByPrimaryKey("PaymentGatewayAliPay", UtilMisc.toMap("paymentGatewayConfigId", paymentGatewayConfigId));
            } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
            }
        if(UtilValidate.isNotEmpty(AliPay))
        {
        	AliPay.set("paymentName", paymentName);
        	AliPay.set("isEnabled", isEnabled);
        	AliPay.set("isDefault", isDefault);
        	AliPay.set("apiKey", apiKey);
        	AliPay.set("secretKey", secretKey);
        	AliPay.set("payAccount", payAccount);
        	AliPay.set("payUrl", payUrl);
        	AliPay.set("backUrl", backUrl);
        	AliPay.set("mobileBindUrl", mobileBindUrl);
        	AliPay.set("description", description);
        	AliPay.set("imgContentId", contentId);
        	//支付问题描述
            String desContentId=(String)AliPay.get("desContentId");
            
            Map<String, Object> passedParams = FastMap.newInstance();
            if(UtilValidate.isNotEmpty(desContentId))
            {
          	//获取内容表数据
      		GenericValue Content;
		    try {
		    	Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", desContentId)));
		    	if(UtilValidate.isNotEmpty(Content))
	      		  {
	      			GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId"))));
	      			//查询文章内容
	      			GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",dataResource.get("dataResourceId")));
	      			//判断文章内容是否存在
	      			if(UtilValidate.isNotEmpty(electronicText))
	      			  {
	      				if(UtilValidate.isEmpty(textData))
	      				  {
	      					AliPay.set("desContentId", "");
	      				  }else if(!textData.equals((String)electronicText.get("textData")))//判断文章内容是否修改
	      				          {
	      	    					//更新支付问题描述
	      	    					electronicText.set("textData", textData);
	      	    					electronicText.store();
	      				        }
	      			  }
	      		  }
		        } catch (Exception e) {
						e.printStackTrace();
			    }
            }else if (UtilValidate.isEmpty(desContentId)&&textData!=null)
                     {
     			passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",textData
     					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
     			 try {
     				result = dispatcher.runSync("createTextContent", passedParams);
     				if(UtilValidate.isNotEmpty(result))
         		      {
     					AliPay.set("desContentId", (String)result.get("contentId"));
         		      }
     			 } catch (GenericServiceException e) {
     				e.printStackTrace();
     			}
              }
            
        }
        try {
        	AliPay.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 支付宝详情查询 add by gss 2016-1-14
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> queryWeixinPay(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//微信支付类型Id
		String paymentGatewayConfigId = (String) context.get("paymentGatewayConfigId");
		if(UtilValidate.isNotEmpty(paymentGatewayConfigId))
		{
		try {
			//微信支付配置信息
			GenericValue WeixinPay = delegator.findByPrimaryKey("PaymentGatewayWeixinPay", UtilMisc.toMap("paymentGatewayConfigId",paymentGatewayConfigId));
			//支付类型信息
			GenericValue WeixinPaytype = delegator.findByPrimaryKey("PaymentGatewayConfig", UtilMisc.toMap("paymentGatewayConfigId",paymentGatewayConfigId));
			if(UtilValidate.isNotEmpty(WeixinPay))
			{
				//支付类型
				result.put("payType", WeixinPaytype.get("description"));	
			}
			
			//判断微信支付配置信息是否为空
			if(UtilValidate.isNotEmpty(WeixinPay))
			{
				result.put("weixinPay", WeixinPay);
				//支付问题关联表ID
				String  desContentId=(String)WeixinPay.get("desContentId");
			    //是否有支付问题描述
				if(UtilValidate.isNotEmpty(desContentId))
			    {
				  //获取内容表数据
		    	  GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", desContentId)));
		    	  if(UtilValidate.isNotEmpty(Content))
		    	  {
		    		  GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId"))));
		    			if(UtilValidate.isNotEmpty(dataResource))
		    			{
		    				//支付问题描述内容
				    		  GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",dataResource.get("dataResourceId"))); 
				    			if(UtilValidate.isNotEmpty(electronicText))
				    			{
				    				result.put("textData", electronicText.get("textData"));
				    			}
		    			}
		    	  }
			    }
				//图片关联ID
				String  imgContentId=(String)WeixinPay.get("imgContentId");
			    //判断是否有支付图标
				if(UtilValidate.isNotEmpty(imgContentId))
			    {
					  //获取内容表数据
			    	 GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", imgContentId)));
			    	 if(UtilValidate.isNotEmpty(Content))
			    	 {
			    		 GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId")))); 
			    	   if(UtilValidate.isNotEmpty(dataResource))
			    	   {
			    		   //获取图片路径
			    		   result.put("objectInfo", dataResource.get("objectInfo"));
			    	   }
			    	 }
			    }
			}
		    }catch (Exception e) {
		         e.printStackTrace();
		    }
		}
		return result;
	}
	
	/**
	 * 更新微信接口信息 add by gss 2016-1-14
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateWeixinPay(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		LocalDispatcher dispatcher = dctx.getDispatcher(); 
 		Map<String, Object> result = ServiceUtil.returnSuccess();
		//支付信息接口ID
		String paymentGatewayConfigId = (String) context.get("paymentGatewayConfigId");
		//支付名称
		String paymentName = (String) context.get("paymentName");
		//是否启用
	    String isEnabled = (String) context.get("isEnabled");
	    //是否默认
	    String isDefault = (String) context.get("isDefault");
	    //Api-Key
	    String apiKey = (String) context.get("apiKey");
	    //Secret-key
	    String secretKey = (String) context.get("secretKey");
	    //商户号
	    String partner = (String) context.get("partner");
	    //商户标识
	    String partnerKey = (String) context.get("partnerKey");
	    //通知URL
	    String notifyUrl = (String) context.get("notifyUrl");
	    //支付描述
	    String description = (String) context.get("description");
	    //支付问题描述
	    String textData = (String) context.get("textData");
	    //图标
	    String contentId = (String) context.get("contentId");
	    
		if (paymentGatewayConfigId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "NotFound", UtilMisc.toMap("paymentGatewayConfigId", ""), locale));
	        }
		//定义实体类
		GenericValue WeixnPay;
        try {
        	WeixnPay = delegator.findByPrimaryKey("PaymentGatewayWeixinPay", UtilMisc.toMap("paymentGatewayConfigId", paymentGatewayConfigId));
            } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
            }
        if(UtilValidate.isNotEmpty(WeixnPay))
        {
        	
        	WeixnPay.set("paymentName", paymentName);
        	WeixnPay.set("isEnabled", isEnabled);
        	WeixnPay.set("isDefault", isDefault);
        	WeixnPay.set("apiKey", apiKey);
        	WeixnPay.set("secretKey", secretKey);
        	WeixnPay.set("partner", partner);
        	WeixnPay.set("partnerKey", partnerKey);
        	WeixnPay.set("notifyUrl", notifyUrl);
        	WeixnPay.set("description", description);
        	WeixnPay.set("imgContentId", contentId);
        	//支付问题描述
            String desContentId=(String)WeixnPay.get("desContentId");
            Map<String, Object> passedParams = FastMap.newInstance();
            if(UtilValidate.isNotEmpty(desContentId))
            {
          	//获取内容表数据
      		GenericValue Content;
		    try {
		    	Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", desContentId)));
		    	if(UtilValidate.isNotEmpty(Content))
	      		  {
	      			GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId"))));
	      			//查询文章内容
	      			GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",dataResource.get("dataResourceId")));
	      			//判断文章内容是否存在
	      			if(UtilValidate.isNotEmpty(electronicText))
	      			  {
	      				if(UtilValidate.isEmpty(textData))
	      				  {
	      					WeixnPay.set("desContentId", "");
	      				  }else if(!textData.equals((String)electronicText.get("textData")))//判断文章内容是否修改
	      				          {
	      	    					//更新支付问题描述
	      	    					electronicText.set("textData", textData);
	      	    					electronicText.store();
	      				        }
	      			  }
	      		  }
		        } catch (Exception e) {
						e.printStackTrace();
			    }
            }else if (UtilValidate.isEmpty(desContentId)&&textData!=null)
                     {
     			passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",textData
     					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
     			 try {
     				result = dispatcher.runSync("createTextContent", passedParams);
     				if(UtilValidate.isNotEmpty(result))
         		      {
     					WeixnPay.set("desContentId", (String)result.get("contentId"));
         		      }
     			 } catch (GenericServiceException e) {
     				e.printStackTrace();
     			}
              }
            
        }
        try {
        	WeixnPay.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return ServiceUtil.returnSuccess();
	}
}


