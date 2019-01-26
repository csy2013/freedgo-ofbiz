/*
 * 文件名：IntegralServices.java
 * 版权：启华
 * 描述：消费积分规则服务类
 * 修改人：gss
 * 修改时间：2016-1-18
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.integral;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceAuthException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.ServiceValidationException;



public class IntegralServices{
    public static final String module = IntegralServices.class.getName();
    public static final String resource = "ContentUiLabels";
  
    /**
     * 查询等级分类
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> queryAllLevelType(DispatchContext dcx, Map<String, ? extends Object> context) {
        Delegator delegator = dcx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        try {
            List<GenericValue> partyLevelList = delegator.findByAnd("PartyLevelType", (Object[]) null);
            String option="";
            for (GenericValue levelList : partyLevelList) 
            {
            	option+="<"+"option"+" "+"value"+"="+levelList.get("levelId") +">"+levelList.get("levelName")+"</option>";
            }
            result.put("option", option);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * 查询等级类型
     * @param dcx
     * @param context
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map<String, Object> getLevelType(DispatchContext dcx, Map<String, ? extends Object> context) {
    	Delegator delegator = dcx.getDelegator();
    	Map<String, Object> result = ServiceUtil.returnSuccess();
    	String  levelId=(String)context.get("levelId");
    	try {
    		GenericValue partyLevel = delegator.findByPrimaryKey("PartyLevelType", UtilMisc.toMap("levelId", levelId));
    		result.put("partyLevel", partyLevel);
    	} catch (GenericEntityException e) {
    		e.printStackTrace();
    	}
    	return result;
    }
    /**
     * 查询等级分类 数组
     * @param dcx
     * @param context
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Object> getAllLevelType(DispatchContext dcx, Map<String, ? extends Object> context) {
    	Delegator delegator = dcx.getDelegator();
    	Map<String, Object> result = ServiceUtil.returnSuccess();
    	List level = new ArrayList();
    	try {
    		List<GenericValue> partyLevelList = delegator.findByAnd("PartyLevelType", (Object[]) null);
    		JSONObject jsonObject1 = new JSONObject();
			jsonObject1.put("value", "-1");
 	        jsonObject1.put("view", "All");
 	        level.add(jsonObject1);
    		for (GenericValue levelList : partyLevelList) 
    		{
    	    JSONObject jsonObject = new JSONObject();
    		jsonObject.put("value", levelList.get("levelId"));
     	    jsonObject.put("view", levelList.get("levelName"));
     	    level.add(jsonObject);
    		}
    		result.put("level", level);
    	} catch (GenericEntityException e) {
    		e.printStackTrace();
    	}
    	return result;
    }
   
    /**
     * 添加积分规则
     * @param dcx
     * @param context
     * @return
     */
	public static Map<String, Object> createPartyIntegral(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		 Delegator delegator = dctx.getDelegator();
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		
		 //integral
		 String integral = (String) context.get("integral");
		 try {
			List<GenericValue> partyLevelList = delegator.findByAnd("PartyIntegral", (Object[]) null);
			delegator.removeAll(partyLevelList);
		} catch (GenericEntityException e1) {
			e1.printStackTrace();
		}
		 String[] a=integral.split(",");
		 for (int i = 0; i < a.length; i++) 
		 {
			GenericValue partyIntegra = delegator.makeValue("PartyIntegral");
			String b=a[i];
			String[] c=b.split(":");
			partyIntegra.set("partyIntegralId", delegator.getNextSeqId("PartyIntegral"));
			if (c[0]!=null&&!"-1".equals(c[0]) &&!"".equals(c[0]))
			{
				partyIntegra.set("levelId", c[0]);
			}
			if (c[1]!=null&&!"".equals(c[1])) {
				partyIntegra.set("integralValue", new Long(c[1]));
			}
			
			if (c[2]!=null&&!"".equals(c[2])) {
				partyIntegra.set("productCategoryId", c[2]);
			}
			if (c[3]!=null&&!"".equals(c[3])) {
				partyIntegra.set("productId", c[3]);
			}
			partyIntegra.set("partyIntegralType", c[4]);
			try {
				partyIntegra.create();
			  } catch (GenericEntityException e) {
				e.printStackTrace();
			  }
		 }
		return result;
	}	
	
	/**
     * 保存积分操作记录	add by qianjin 2016.03.09
     * @param dcx
     * @param context
     * @return
     */
	public static Map<String, Object> savePartyIntegralHistory(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		 Delegator delegator = dctx.getDelegator();
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		 //参数
		 String partyId = (String) context.get("partyId");	//会员ID
		 BigDecimal integralValue = (BigDecimal) context.get("integralValue");	//积分值
		 String getWay = (String) context.get("getWay");	//获取方式
		 String description = (String) context.get("description");	//描述
		 String changedir = (String) context.get("changedir");	//增减方式  增加：plus  减少：minus
		 
		 try {
			String new_id = delegator.getNextSeqId("PartyIntegralHistory");
			GenericValue pih_gv = delegator.makeValue("PartyIntegralHistory", UtilMisc.toMap("partyIntegralHistoryId", new_id));
			pih_gv.set("partyId", partyId);
			if("plus".equals(changedir)){
				pih_gv.set("integralValue", integralValue.longValue());
			}else{
				pih_gv.set("integralValue", -integralValue.longValue());
			}
			pih_gv.set("getWay", getWay);
			pih_gv.set("description", description);
			pih_gv.create();
		 } catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
     * 积分操作	add by qianjin 2016.03.14
     * @param
     * @param context
     * @return
     */
	public static Map<String, Object> partyIntegralChange(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		 Delegator delegator = dctx.getDelegator();
		 LocalDispatcher dispatcher = dctx.getDispatcher();	//dispatcher对象
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		 //参数
		 String partyId = (String) context.get("partyId");	//会员ID
		 BigDecimal integralValue = (BigDecimal) context.get("integralValue");	//积分值
		 String changedir = (String) context.get("changedir");	//增减方式  增加：plus  减少：minus
		 String type = (String) context.get("type");	//积分发生类型 1：购物奖励 2：购物消耗 3：购物退货
		 String orderId = (String) context.get("orderId");	//订单号
		
		 try {
			 //待修改的实体对象
			 List<GenericValue> toBeStore = new ArrayList<GenericValue>();
			 //会员积分实体
			 GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", partyId));
			 if(UtilValidate.isNotEmpty(partyScore) && UtilValidate.isNotEmpty(partyScore.getLong("scoreValue"))){
				 if("plus".equals(changedir)){
					 partyScore.set("scoreValue", partyScore.getLong("scoreValue") + integralValue.longValue());
				 }else{
					 partyScore.set("scoreValue", partyScore.getLong("scoreValue") - integralValue.longValue());
				 }
				 toBeStore.add(partyScore);
			 }
			 
			 if(UtilValidate.isNotEmpty(orderId)){
				//订单实体
				GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
				BigDecimal getIntegral = BigDecimal.ZERO;
				if(UtilValidate.isNotEmpty(orderHeader) && UtilValidate.isNotEmpty(orderHeader.getBigDecimal("getIntegral"))){
					getIntegral = orderHeader.getBigDecimal("getIntegral");
				}
				if("plus".equals(changedir)){
					orderHeader.set("getIntegral", getIntegral.add(integralValue));
				}else{
					orderHeader.set("getIntegral", getIntegral.subtract(integralValue));
				}
				toBeStore.add(orderHeader);
			 }
			 delegator.storeAll(toBeStore);
			 
			 //保存积分获取记录
			 savePartyIntegralHistory(dctx,context);
		 } catch (GenericEntityException e) {
			e.printStackTrace();
			result.put("errorMsg", "保存积分记录服务调用错误！");
		}
		
		 //yabiz商城的积分变动接口
		 try {
			 //会员登录账号实体
			 List<GenericValue> userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId));
			 Map<String, Object> paramContext  = FastMap.newInstance();
			 paramContext.put("token", userLogin.get(0).get("token"));
			 paramContext.put("changedir", changedir);
			 paramContext.put("point", integralValue.intValue());
			 paramContext.put("vouchercode", UtilValidate.isEmpty(orderId) ? "" : orderId);
			 paramContext.put("type", type);
			 dispatcher.runSync("partyScoreChange",paramContext);
		 } catch (ServiceAuthException e) {
			 e.printStackTrace();
			 result.put("errorMsg", "yabiz商城积分变更服务调用错误！");
		 } catch (ServiceValidationException e) {
			 e.printStackTrace();
			 result.put("errorMsg", "yabiz商城积分变更服务调用错误！");
		 } catch (GenericServiceException e) {
			 e.printStackTrace();
			 result.put("errorMsg", "yabiz商城积分变更服务调用错误！");
		 } catch (GenericEntityException e) {
			e.printStackTrace();
			result.put("errorMsg", "根据partyId查询UserLogin错误！");
		}
		return result;
	}
}


