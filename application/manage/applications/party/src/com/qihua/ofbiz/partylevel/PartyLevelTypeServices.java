/*
 * 文件名：PartyLevelTypeServices.java
 * 版权：启华
 * 描述：等级规则服务类
 * 修改人：gss
 * 修改时间：2016-1-20
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.partylevel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;





public class PartyLevelTypeServices{
    public static final String module = PartyLevelTypeServices.class.getName();
    public static final String resource = "PartyUiLabels";
    
    /**
     * 添加等级规则
     * @param dcx
     * @param context
     * @return
     */
	public static Map<String, Object> createPartyLevelType(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		 Delegator delegator = dctx.getDelegator();
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		 //获取等级名称
		 String levelName = (String) context.get("levelName");
		 //获取等级描述
		 String levelDescrption = (String) context.get("levelDescrption");
		 //获取等级成长值
		 String levelExperience = (String) context.get("levelExperience");
		 //获取等级折扣
		 String levelDiscount = (String) context.get("levelDiscount");
		 //等级类型 企业会员、个人会员
		 String partyType = (String) context.get("partyType");
		 
		 GenericValue partyLevelType = delegator.makeValue("PartyLevelType");
		 partyLevelType.set("levelId", delegator.getNextSeqId("PartyLevelType"));
		 partyLevelType.set("levelName", levelName);
		 partyLevelType.set("partyType", partyType);
		 partyLevelType.set("levelDescrption",levelDescrption );
		 partyLevelType.set("levelExperience", new Long(levelExperience));
		 if(UtilValidate.isEmpty(levelDiscount)){
			 partyLevelType.set("levelDiscount", new BigDecimal(1));
		 }else{
			 partyLevelType.set("levelDiscount", new BigDecimal(levelDiscount));
		 }
		 
		 try {
				partyLevelType.create();
			  } catch (GenericEntityException e) {
				e.printStackTrace();
			  }
		return result;
	}	
	/**
	 * 修改等级规则
	 * @param dcx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updatePartyLevelType(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		//获取等级名称
		String levelName = (String) context.get("levelName");
		//获取等级类型id
		String levelId = (String) context.get("levelId");
		//获取等级描述
		String levelDescrption = (String) context.get("levelDescrption");
		//获取等级成长值
		String levelExperience = (String) context.get("levelExperience");
		//获取等级折扣
		String levelDiscount = (String) context.get("levelDiscount");
		//定义实体类
		GenericValue partyLevelType;
        try {
        	partyLevelType = delegator.findByPrimaryKey("PartyLevelType", UtilMisc.toMap("levelId", levelId));
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
		partyLevelType.set("levelName", levelName);
		partyLevelType.set("levelDescrption",levelDescrption );
		partyLevelType.set("levelExperience", new Long(levelExperience));
		 if(UtilValidate.isEmpty(levelDiscount)){
			 partyLevelType.set("levelDiscount", new BigDecimal(10));
		 }else{
			 partyLevelType.set("levelDiscount", new BigDecimal(levelDiscount));
		 }
		try {
			partyLevelType.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}	
	/**
	 * 删除等级规则
	 * @param dcx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> deletePartyLevelType(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		//获取等级类型id
		String levelId = (String) context.get("levelId");
		//定义实体类
		GenericValue partyLevelType;
		try {
			partyLevelType = delegator.findByPrimaryKey("PartyLevelType", UtilMisc.toMap("levelId", levelId));
		} catch (GenericEntityException ex) {
			return ServiceUtil.returnError(ex.getMessage());
		}
		try {
			partyLevelType.remove();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}	
}


