/*
 * 文件名：SegmentNoServices.java
 * 版权：启华
 * 描述：号段服务类
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


public class SegmentNoServices{
    public static final String module = SegmentNoServices.class.getName();
    public static final String resource = "ContentUiLabels";
    /**
	 * 新增号段 add by gss 2016-1-11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> createSegmentNo(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		//号段设置
		GenericValue SegmentNumber = delegator.makeValue("SegmentNumber");
		//序号
		String segmentNumberId = (String) context.get("segmentNumberId");
         //号段自
		 String segmentNoFrom = (String) context.get("segmentNoFrom");
		 //号段至
		 String segmentNoTo = (String) context.get("segmentNoTo");
		 //内部给号
		 String isInside = (String) context.get("isInside");
		 //启用标识
		 String isEnabled = (String) context.get("isEnabled");
		 SegmentNumber.set("segmentNumberId", segmentNumberId);
		 SegmentNumber.set("segmentNoFrom", segmentNoFrom);
		 SegmentNumber.set("segmentNoTo", segmentNoTo);
		//判断是否是内部给号
		 if(UtilValidate.isEmpty(isInside))
		   {
			 isInside="N";
		   }
		//判断是否启用
		 if(UtilValidate.isEmpty(isEnabled))
		   {
			 isEnabled="N";
		   }
		 SegmentNumber.set("isEnabled", isEnabled);
		 SegmentNumber.set("isInside", isInside);
 		 
 		try {
 			//创建号段
 			SegmentNumber.create();
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
	public static Map<String, Object> updateSegmentNo(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		//序号
		String segmentNumberId = (String) context.get("segmentNumberId");
		//号段自
		String segmentNoFrom = (String) context.get("segmentNoFrom");
		//号段至
	    String segmentNoTo = (String) context.get("segmentNoTo");
	     //内部给号
        String isInside = (String) context.get("isInside");
         //启用标识
        String isEnabled = (String) context.get("isEnabled");
		if (segmentNumberId == null) 
		   {
	            return ServiceUtil.returnError(UtilProperties.getMessage(resource, 
	                    "NotFound", UtilMisc.toMap("segmentNumberId", ""), locale));
	        }
		//定义实体类
		GenericValue SegmentNumber;
        try {
        	SegmentNumber = delegator.findByPrimaryKey("SegmentNumber", UtilMisc.toMap("segmentNumberId", segmentNumberId));
            } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
            }
        
        SegmentNumber.set("segmentNoFrom", segmentNoFrom);
        SegmentNumber.set("segmentNoTo", segmentNoTo);
      //判断是否是内部给号
		 if(UtilValidate.isEmpty(isInside))
		   {
			 isInside="N";
		   }
		//判断是否启用
		 if(UtilValidate.isEmpty(isEnabled))
		   {
			 isEnabled="N";
		   }
        SegmentNumber.set("isInside", isInside);
        SegmentNumber.set("isEnabled", isEnabled);
        try {
        	SegmentNumber.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return ServiceUtil.returnSuccess();
	}

	/**
	 * 号段详情查询 add by gss 2016-1-9
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findSegmentNoById(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> result = FastMap.newInstance();
		Locale locale = (Locale) context.get("locale");
		//号段序号
		String segmentNumberId = (String) context.get("segmentNumberId");
		if(UtilValidate.isNotEmpty(segmentNumberId)){
		try {
			//号段信息
			GenericValue segmentNumber = delegator.findByPrimaryKey("SegmentNumber", UtilMisc.toMap("segmentNumberId",segmentNumberId));
			//判断号段是否为空
			if(UtilValidate.isNotEmpty(segmentNumber))
			  {
				result.put("segmentNumber", segmentNumber);
			  }
		    }catch (Exception e) {
		         e.printStackTrace();
		    }
		}
		return result;
	}

	
	/**
	 * 分页查询所有号段信息 add by gss 2016-1-11
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findSegmentNoList(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		int segmentNoListSize = 0;
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
		
		List<GenericValue> segmentNoList = null;
		DynamicViewEntity dve = new DynamicViewEntity();
		
		dve.addMemberEntity("SN", "SegmentNumber");
		
		dve.addAliasAll("SN", "", null);
		// 号段ID
		dve.addAlias("SN", "segmentNumberId");
		// 号段自
		dve.addAlias("SN", "segmentNoFrom");
		// 号段至
		dve.addAlias("SN", "segmentNoTo");
		// 是否启用
		dve.addAlias("SN", "isEnabled");
		// 是否内部给号
		dve.addAlias("SN", "isInside");
		// 当前编号
		dve.addAlias("SN", "currentNo");
		
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("segmentNumberId");
		fieldsToSelect.add("segmentNoFrom");
		fieldsToSelect.add("segmentNoTo");
		fieldsToSelect.add("isEnabled");
		fieldsToSelect.add("isInside");
		fieldsToSelect.add("currentNo");
		List<String> orderBy = FastList.newInstance();
		orderBy.add("segmentNumberId");

		// 查询条件和列表
		List<EntityCondition> andExprs = FastList.newInstance();
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
			EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
			// iterator循环
			EntityListIterator pli = delegator.findListIteratorByCondition(dve,
					mainCond, null, fieldsToSelect, orderBy, findOpts);
			segmentNoList = pli.getPartialList(lowIndex, viewSize);
			// 统计总条数
			segmentNoListSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > segmentNoListSize)
			   {
				highIndex = segmentNoListSize;
			   }
			// 关闭iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("segmentNoList", segmentNoList);
		result.put("segmentNoListSize", Integer.valueOf(segmentNoListSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		return result;
	}
	/**
	 * 删除号段add by gss 2016-1-11
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 */
    public static Map<String,Object> delsegmentNumber(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException, GenericEntityException{
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        //删除号段Id
        String deleteId = (String)context.get("deleteId");
        try {
        //判断删除Id是否为空
        if(UtilValidate.isNotEmpty(deleteId))
          {
            String[] deleteIds = deleteId.split(",");
                //查询号段信息
                List<GenericValue> segmentNumberList = delegator.findList("SegmentNumber", EntityCondition.makeCondition("segmentNumberId", EntityOperator.IN, Arrays.asList(deleteIds)), null, null, null, false);
                if(UtilValidate.isNotEmpty(segmentNumberList))
                  {
                //删除号段
                	delegator.removeAll(segmentNumberList);
                  }
         }
        }catch(GenericEntityException e){
            Debug.log(e.getMessage());
        }
        return result;
    }
}


