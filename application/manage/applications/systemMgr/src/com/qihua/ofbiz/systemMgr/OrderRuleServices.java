/*
 * 文件名：OrderRuleServices.java
 * 版权：启华
 * 描述：订单设置服务类
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
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


public class OrderRuleServices{
    public static final String module = OrderRuleServices.class.getName();
    public static final String resource = "ContentUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";
	/**
	 * 更新订单规则  add by gss 2016-1-11 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateOrderRule(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
 		LocalDispatcher dispatcher = dctx.getDispatcher(); 
 		Map<String, Object> result = ServiceUtil.returnSuccess();
		//订单规则Id
		String orderRuleId = (String) context.get("orderRuleId");
		String name = (String) context.get("name");
		String nameValue = (String) context.get("nameValue");
		String nameUom = (String) context.get("nameUom");
		String nameUomValue = (String) context.get("nameUomValue");
		String isReturn = (String) context.get("isReturn");
		String return_content = (String) context.get("return_content");
		String refund_content = (String) context.get("refund_content");

		if (orderRuleId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "NotFound", UtilMisc.toMap("orderRuleId", ""), locale));
	       }
		//定义实体类
		GenericValue orderRule;
        try {
        	orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", orderRuleId));
            } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
            }
         //是否允许退单是否更改
          if(UtilValidate.isNotEmpty(isReturn)&& "Y".equals(isReturn))
            {
        	  orderRule.set("isReturn","N");
            }
          if(UtilValidate.isNotEmpty(isReturn)&& "N".equals(isReturn))
            {
        	  orderRule.set("isReturn","Y");
            }
          //普通订单自动取消时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "ordinaryCancelStamp".equals(name))
            {
        	  orderRule.set("ordinaryCancelStamp",new Long(nameValue));
            }
          //普通订单自动取消时间单位是否更改
          if(UtilValidate.isNotEmpty(nameUom)&& "ordinaryCancelUom".equals(nameUom))
            {
        	  orderRule.set("ordinaryCancelUom",nameUomValue);
            }
          //团购订单自动取消时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "groupCancelStamp".equals(name))
            {
        	  orderRule.set("groupCancelStamp",new Long(nameValue));
            }
          //团购订单自动取消时间单位是否更改
          if( UtilValidate.isNotEmpty(nameUom)&& "groupCancelUom".equals(nameUom))
            {
        	  orderRule.set("groupCancelUom",nameUomValue);
            }
          //秒杀订单自动取消时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "seckillCancelStamp".equals(name))
            {
        	  orderRule.set("seckillCancelStamp",new Long(nameValue));
            }
          //秒杀订单自动取消时间单位是否更改
          if(UtilValidate.isNotEmpty(nameUom)&& "seckillCancelUom".equals(nameUom))
            {
        	  orderRule.set("seckillCancelUom",nameUomValue);
            }
          //订单自动确认收货时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "confirmOrderStamp".equals(name))
            {
        	  orderRule.set("confirmOrderStamp",new Long(nameValue));
            }
          //退单自动审核时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "returnToexamineStamp".equals(name))
            {
        	  orderRule.set("returnToexamineStamp",new Long(nameValue));
            }
          //退单自动确认收货时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "returnConfirmStamp".equals(name))
          {
        	  orderRule.set("returnConfirmStamp",new Long(nameValue));
          }
          //退单自动确认收货时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "returnCancelStamp".equals(name))
            {
        	  orderRule.set("returnCancelStamp",new Long(nameValue));
            }
          //自动评价时间
          if(UtilValidate.isNotEmpty(name)&& "reviewStamp".equals(name))
            {
        	  orderRule.set("reviewStamp",new Long(nameValue));
            }
          //可提交退货时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "returnCommitStamp".equals(name))
            {
        	  orderRule.set("returnCommitStamp",new Long(nameValue));
            }
          //预计退款时间是否更改
          if(UtilValidate.isNotEmpty(name)&& "expectedRefundStamp".equals(name))
          {
        	  orderRule.set("expectedRefundStamp",new Long(nameValue));
          }
        try {
        	orderRule.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 更新订单规则 退货说明  add by gss 2016-1-11 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updaterReturnContent(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		LocalDispatcher dispatcher = dctx.getDispatcher(); 
		Map<String, Object> result = ServiceUtil.returnSuccess();
		//订单规则Id
		String orderRuleId = (String) context.get("orderRuleId");
		String return_content = (String) context.get("return_content");
		if (orderRuleId == null) 
		{
			return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
					"NotFound", UtilMisc.toMap("orderRuleId", ""), locale));
		}
		//定义实体类
		GenericValue orderRule;
		try {
			orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", orderRuleId));
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		
		Map<String, Object> passedParams = FastMap.newInstance();
		Map<String, Object> result1 = FastMap.newInstance();
		//判断是否已有退货说明
		if(UtilValidate.isNotEmpty(orderRule.get("returnContentId")))
		{
			//获取内容表数据
			try {
				GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", orderRule.get("returnContentId"))));
				if(UtilValidate.isNotEmpty(Content))
				{
					GenericValue dataResource = EntityUtil.getFirst(delegator.findByAnd("DataResource", UtilMisc.toMap("dataResourceId", Content.get("dataResourceId"))));
					//查询文章内容
					GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",Content.get("dataResourceId")));
					if(UtilValidate.isNotEmpty(electronicText)&&UtilValidate.isEmpty(return_content))
					{
						orderRule.set("returnContentId", null);
					}else if(!return_content.equals((String)electronicText.get("textData")))//判断文章内容是否修改
					{
						//更新文章内容
						electronicText.set("textData", return_content);
						electronicText.store();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(return_content!=null)
		{
			passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",return_content
					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
			try {
				result1 = dispatcher.runSync("createTextContent", passedParams);
				if(UtilValidate.isNotEmpty(result1))
				{
					orderRule.set("returnContentId", (String)result1.get("contentId"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			orderRule.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 更新订单规则 退款说明  add by gss 2016-1-11 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updaterRefundContent(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		LocalDispatcher dispatcher = dctx.getDispatcher(); 
		Map<String, Object> result = ServiceUtil.returnSuccess();
		//订单规则Id
		String orderRuleId = (String) context.get("orderRuleId");
		String refund_content = (String) context.get("refund_content");
		if (orderRuleId == null) 
		{
			return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
					"NotFound", UtilMisc.toMap("orderRuleId", ""), locale));
		}
		//定义实体类
		GenericValue orderRule;
		try {
			orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", orderRuleId));
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		Map<String, Object> passedParams = FastMap.newInstance();
		Map<String, Object> result1 = FastMap.newInstance();
		//判断是否已有退货说明
		if(UtilValidate.isNotEmpty(orderRule.get("refundContentId")))
		{
			//获取内容表数据
			try {
				GenericValue Content = EntityUtil.getFirst(delegator.findByAnd("Content", UtilMisc.toMap("contentId", orderRule.get("refundContentId"))));
				if(UtilValidate.isNotEmpty(Content))
				{
					//查询文章内容
					GenericValue electronicText=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",Content.get("dataResourceId")));
					if(UtilValidate.isNotEmpty(electronicText)&&UtilValidate.isEmpty(refund_content))
					{
						orderRule.set("refundContentId", null);
					}else if(!refund_content.equals((String)electronicText.get("textData")))//判断文章内容是否修改
					{
						//更新文章内容
						electronicText.set("textData", refund_content);
						electronicText.store();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(refund_content!=null)
		{
			passedParams = UtilMisc.toMap( "dataResourceTypeId", "ELECTRONIC_TEXT","dataTemplateTypeId","NONE",  "contentPurposeTypeId","ARTICLE", "textData",refund_content
					,"statusId", "CTNT_INITIAL_DRAFT","userLogin", userLogin,"contentAssocTypeId","SUB_CONTENT");
			try {
				result1 = dispatcher.runSync("createTextContent", passedParams);
				if(UtilValidate.isNotEmpty(result1))
				{
					orderRule.set("refundContentId", (String)result1.get("contentId"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			orderRule.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 查询退款说明  add by gss 2016-1-27
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findOrdercontent(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//订单设置ID
		String orderRuleId = (String) context.get("orderRuleId");
		String type = (String) context.get("type");
		result.put("type", type);
		if(UtilValidate.isNotEmpty(orderRuleId))
		{
			GenericValue orderRule;
	        try {
	        	orderRule = delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", orderRuleId));
	            } catch (GenericEntityException ex) {
	            return ServiceUtil.returnError(ex.getMessage());
	            }
	        if(UtilValidate.isNotEmpty(orderRule))
	        {
	        //退货说明
	        if("return".equals(type) &&orderRule.get("returnContentId")!=null){
	        	 
				try {
					GenericValue Content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId",orderRule.get("returnContentId")));
					if(UtilValidate.isNotEmpty(Content))
			    	 {
			    		 GenericValue Contenttext=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",Content.get("dataResourceId"))); 
			    		 if(UtilValidate.isNotEmpty(Contenttext))
			 			 {
			 				result.put("textData", (String)Contenttext.get("textData"));
			 			 }
			    	 }
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
	        }else if("refund".equals(type) &&orderRule.get("refundContentId")!=null)
	        {
	        	try {
					GenericValue Content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId",orderRule.get("refundContentId")));
					if(UtilValidate.isNotEmpty(Content))
			    	 {
			    		 GenericValue Contenttext=delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId",Content.get("dataResourceId"))); 
			    		 if(UtilValidate.isNotEmpty(Contenttext))
			 			 {
			 				result.put("textData", (String)Contenttext.get("textData"));
			 			 }
			    	 }
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
	        }
	     }
       }
	return result;
	}
	
	
}


