/*
 * 文件名：EnumerationServices.java
 * 版权：启华
 * 描述：字段表服务类
 * 修改人：gss
 * 修改时间：2015-12-28
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.systemMgr;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;


public class EnumerationServices{
    public static final String module = EnumerationServices.class.getName();
    public static final String resource = "ContentUiLabels";
    /**
	 * 新增字段表 add by gss 2016-1-21
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createEnumeration(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		//字段表设置
		 GenericValue Enumeration = delegator.makeValue("Enumeration");
		//字段类型
		 String enumTypeId = (String) context.get("enumTypeId");
         //编码
		 String enumId = (String) context.get("enumId");
		 //描述
		 String description = (String) context.get("description");
		 Enumeration.set("enumTypeId", enumTypeId);
		 Enumeration.set("enumId", enumId);
		 Enumeration.set("description", description);
		 Enumeration.set("enumCode", "N");
 		try {
 			//创建号段
 			Enumeration.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return ServiceUtil.returnSuccess();
	}
    
	/**
	 * 更新号段 add by gss  add by gss 2016-1-11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateEnumeration(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
         //序号
		 String enumId = (String) context.get("enumId");
		 //描述
		 String description = (String) context.get("description");
		if (enumId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "NotFound", UtilMisc.toMap("Enumeration", ""), locale));
	        }
		//定义实体类
		GenericValue Enumeration;
        try {
        	Enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", enumId));
            } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
            }
        Enumeration.set("description", description);
     
        try {
        	Enumeration.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return ServiceUtil.returnSuccess();
	}
	
	/**
	 * 删除字段表信息add by gss 2016-1-21
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> delEnumeration(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //删除Id
        String ids = (String)context.get("ids");
        try {
        	for(String id : ids.split(",")){
        		GenericValue Enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", id));
        		Enumeration.setString("enumCode","Y");
        		Enumeration.store();
        	}
        }catch(GenericEntityException e){
            Debug.log(e.getMessage());
        }
        return result;
    }
    /**
	 * 字段详情查询 add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findEnumerationById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		//字段序号
		String enumId = (String) context.get("enumId");
		try {
			//字段信息
			GenericValue Enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId",enumId));
			result.put("isExits",UtilValidate.isEmpty(Enumeration) ? false : true );
			result.put("Enumeration",Enumeration);
	    }catch (Exception e) {
	         e.printStackTrace();
	    }
		return result;
	}
	
	/**
	 * 根据类型Id查询枚举列表 add by qianjin 2016.02.26
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getEnumListByEnumTypeId(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		//类型ID
		String enumTypeId = (String) context.get("enumTypeId");
		
		try {
			//枚举信息
			List<GenericValue> enum_list = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId",enumTypeId,"enumCode","N"));
			result.put("recordsList", enum_list);
	    }catch (Exception e) {
	         e.printStackTrace();
	    }
		return result;
	}
}


