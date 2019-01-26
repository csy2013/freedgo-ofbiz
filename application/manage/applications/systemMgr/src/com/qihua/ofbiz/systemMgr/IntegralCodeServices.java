/*
 * 文件名：IntegralCodeServices.java
 * 版权：启华
 * 描述：积分Code服务类
 * 修改人：zhajh
 * 修改时间：2018-04-28
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.systemMgr;

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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class IntegralCodeServices{
    public static final String module = IntegralCodeServices.class.getName();
    public static final String resource = "ContentUiLabels";
    /**
	 * 新增积分Code add by zhajh 2018-4-28
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createIntegralCode(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");

        //积分Code设置
		String integralCodeId = delegator.getNextSeqId("IntegralCode");
		GenericValue integralCode_gv = delegator.makeValue("IntegralCode", UtilMisc.toMap("integralCodeId",integralCodeId));
		// 积分Code名称
		String integralCodeName = (String) context.get("integralCodeName");
		// 积分Code
		String integralCodeNo = (String) context.get("integralCodeNo");
		integralCode_gv.set("integralCodeName",integralCodeName);//积分Code名称
		integralCode_gv.set("integralCodeNo",integralCodeNo);// 积分Code
		integralCode_gv.set("status", "Y");// 状态
 		try {
 			//创建积分Code
			integralCode_gv.create();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return ServiceUtil.returnSuccess();
	}
    
	/**
	 * 更新积分Code add by zhajh  add by zhajh 2018-4-28
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateIntegralCode(DispatchContext dctx,
														 Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");

		// 积分Code编码
		String integralCodeId = (String) context.get("integralCodeId");
		// 积分Code名称
		String integralCodeName = (String) context.get("integralCodeName");
		// 积分Code
		String integralCodeNo = (String) context.get("integralCodeNo");
		// 积分Code状态
		String status=(String) context.get("status");
		if (integralCodeId == null) {
			return ServiceUtil.returnError(UtilProperties.getMessage(resource,
					"NotFound", UtilMisc.toMap("integralCodeId", ""), locale));
		}
		//定义实体类
		GenericValue integralCode_gv;
		try {
			integralCode_gv = delegator.findByPrimaryKey("IntegralCode", UtilMisc.toMap("integralCodeId", integralCodeId));

			if(UtilValidate.isNotEmpty(integralCode_gv)){
				integralCode_gv.set("integralCodeName", integralCodeName);
				integralCode_gv.set("integralCodeNo", integralCodeNo);
			}

			integralCode_gv.store();
		} catch (GenericEntityException e) {
			return ServiceUtil.returnError(e.getMessage());
		}
		return ServiceUtil.returnSuccess();
	}

	/**
	 * 积分Code详情查询 add by zhajh 2018-4-28
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findIntegralCodeById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//积分Code序号
		String integralCodeId = (String) context.get("integralCodeId");
		if(UtilValidate.isNotEmpty(integralCodeId)){
		try {
			//积分Code信息
			GenericValue integralCode_gv = delegator.findByPrimaryKey("IntegralCode", UtilMisc.toMap("integralCodeId",integralCodeId));
			//判断积分Code是否为空
			if(UtilValidate.isNotEmpty(integralCode_gv))
			  {
				result.put("integralCode", integralCode_gv);
			  }
		    }catch (Exception e) {
		         e.printStackTrace();
		    }
		}
		return result;
	}

	
	/**
	 * 分页查询所有积分Code信息 add by zhajh 2016-1-11
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findIntegralCodeList(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		int integralCodeListSize = 0;
		int lowIndex = 0;
		int highIndex = 0;
		// set the page parameters
		int viewIndex = 0;
		try {
			viewIndex = (Integer) context.get("viewIndex");
		} catch (Exception e) {
			viewIndex = 0;
		}
		result.put("viewIndex", viewIndex);
		int viewSize = 5;
		try {
			viewSize = (Integer) context.get("viewSize");
		} catch (Exception e) {
			viewSize = 5;
		}
		result.put("viewSize", Integer.valueOf(viewSize));
		// blank param list
        String paramList = "";
		
		List<GenericValue> integralCodeList = null;
		DynamicViewEntity dve = new DynamicViewEntity();
		
		dve.addMemberEntity("IC", "IntegralCode");
		dve.addAliasAll("IC", "", null);
		// 积分CodeID
		dve.addAlias("IC", "integralCodeId");
		// 积分Code名称
		dve.addAlias("IC", "integralCodeName");
		// 积分Code
		dve.addAlias("IC", "integralCodeNo");
		// 状态
		dve.addAlias("IC", "status");

		
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("integralCodeId");
		fieldsToSelect.add("integralCodeName");
		fieldsToSelect.add("integralCodeNo");
		fieldsToSelect.add("status");
		List<String> orderBy = FastList.newInstance();
		orderBy.add("integralCodeId");

		// 查询条件和列表
		List<EntityCondition> andExprs = FastList.newInstance();
		andExprs.add(EntityCondition.makeCondition("status", "Y"));
		EntityCondition mainCond = null;
		 //判断查询条件是否为空
        if (andExprs.size() > 0)
           {
        	mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
           }
		try {
			lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
			// 获取一行
			EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
			// iterator循环
			EntityListIterator pli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, orderBy, findOpts);
			integralCodeList = pli.getPartialList(lowIndex, viewSize);
			// 统计总条数
			integralCodeListSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > integralCodeListSize)
			   {
				highIndex = integralCodeListSize;
			   }
			// 关闭iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("integralCodeList", integralCodeList);
		result.put("integralCodeListSize", Integer.valueOf(integralCodeListSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		return result;
	}
	/**
	 * 删除积分Codeadd by zhajh 2018-4-28
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> delIntegralCode(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        //删除积分CodeId
        String deleteId = (String)context.get("deleteId");
        try {
        //判断删除Id是否为空
        if(UtilValidate.isNotEmpty(deleteId))
          {
            String[] deleteIds = deleteId.split(",");
                //查询积分Code信息
                List<GenericValue> integralCodeList = delegator.findList("IntegralCode", EntityCondition.makeCondition("integralCodeId", EntityOperator.IN, Arrays.asList(deleteIds)), null, null, null, false);
                if(UtilValidate.isNotEmpty(integralCodeList)) {
                    //删除积分Code
					for(GenericValue icInfo:integralCodeList){
						icInfo.setString("status","N");
						icInfo.store();
					}
//                	delegator.removeAll(integralCodeList);
				}
         }
        }catch(GenericEntityException e){
            Debug.log(e.getMessage());
        }
        return result;
    }
}


