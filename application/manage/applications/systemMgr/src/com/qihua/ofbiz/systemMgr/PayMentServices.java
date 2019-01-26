/*
 * 文件名：PayMentServices.java
 * 版权：启华
 * 描述：支付方式服务类
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
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;


public class PayMentServices{
    public static final String module = PayMentServices.class.getName();
    public static final String resource = "ContentUiLabels";
  
    
    
	/**
	 * 支付方式详情查询 add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findPayMentById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//支付方式Id
		String paymentGatewayConfigTypeId = (String) context.get("paymentGatewayConfigTypeId");
		if(UtilValidate.isNotEmpty(paymentGatewayConfigTypeId)){
		try {
			//支付方式信息
			GenericValue payment = delegator.findByPrimaryKey("PaymentGatewayConfigType", UtilMisc.toMap("paymentGatewayConfigTypeId",paymentGatewayConfigTypeId));
			//判断支付方式是否为空
			if(UtilValidate.isNotEmpty(payment))
			  {
				result.put("payMent", payment);
			  }
		    }catch (Exception e) {
		         e.printStackTrace();
		    }
		}
		return result;
	}
    
	/**
	 * 更新支付方式 add by gss  add by gss 2016-1-11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updatePayMent(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		//支付方式序号
		String paymentGatewayConfigTypeId = (String) context.get("paymentGatewayConfigTypeId");
		//是否启用
		String hasTable = (String) context.get("hasTable");
		//描述
	    String description = (String) context.get("description");
	  
		if (paymentGatewayConfigTypeId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "NotFound", UtilMisc.toMap("paymentGatewayConfigTypeId", ""), locale));
	        }
		//定义实体类
		GenericValue PayMent;
        try {
        	PayMent = delegator.findByPrimaryKey("PaymentGatewayConfigType", UtilMisc.toMap("paymentGatewayConfigTypeId", paymentGatewayConfigTypeId));
            } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
            }
        PayMent.set("hasTable", hasTable);
        PayMent.set("description", description);
        try {
        	PayMent.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return ServiceUtil.returnSuccess();
	}
}


