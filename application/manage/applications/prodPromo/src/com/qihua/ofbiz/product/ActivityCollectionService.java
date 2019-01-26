package com.qihua.ofbiz.product;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 
 */
public class  ActivityCollectionService{

	/**
	 * 根据活动编码,活动名称,活动类型,活动状态 查询 活动收藏 add by gss 2016/1/21
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findActCollection(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		//活动编码
		String activityId = (String) context.get("activityId");
		//活动名称
		String activityName = (String) context.get("activityName");
		//活动类型
		String activityType = (String) context.get("activityType");
		//活动状态
		String activityAuditStatus = (String) context.get("activityAuditStatus");
		result.put("activityId", activityId);
		result.put("activityName", activityName);
		result.put("activityType", activityType);
		result.put("activityAuditStatus", activityAuditStatus);
		
		int totalSize = 0;
		int lowIndex = 0;
		int highIndex = 0;
		// 设置页数
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		result.put("viewIndex", Integer.valueOf(viewIndex));

		int viewSize = 5;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 5;
		}
		result.put("viewSize", Integer.valueOf(viewSize));
		
		//查询的活动列表
		List<Map> recordsList = FastList.newInstance();
		DynamicViewEntity dve = new DynamicViewEntity();
		//活动收藏表
		dve.addMemberEntity("PAC","ProductActivityCollection");
		//活动表
		dve.addMemberEntity("PA", "ProductActivity");	
		dve.addAlias("PA", "activityId","activityId",null, null, true, null);			//活动ID
		dve.addAlias("PA", "activityType");			//活动类型
		dve.addAlias("PA", "activityAuditStatus");	//活动状态
		dve.addAlias("PA", "activityDesc");			//活动描述
		dve.addAlias("PA", "activityName");			//活动名称
		dve.addAlias("PA", "collectionNum","activityId",null, null, null, "count");			//收藏次数
		dve.addAlias("PA", "activityStartDate");	//活动开始时间
		dve.addAlias("PA", "activityEndDate");		//活动结束时间
		dve.addAlias("PA", "publishDate");			//发布时间
		dve.addAlias("PA", "endDate");				//下架时间
		dve.addViewLink("PAC", "PA",Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
		
		//枚举表
		dve.addMemberEntity("ENUM", "Enumeration");
		dve.addAlias("ENUM","activityTypeName","description",null,null,null,null);			//产品编号
		dve.addViewLink("PA", "ENUM",Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityType", "enumId"));
		
		//活动商品关联表
		dve.addMemberEntity("PAG", "ProductActivityGoods");
		dve.addAlias("PAG", "productId");			//产品编号
		dve.addViewLink("PA", "PAG",Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
		
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("activityId");
		fieldsToSelect.add("activityTypeName");
		fieldsToSelect.add("activityAuditStatus");
		fieldsToSelect.add("activityDesc");
		fieldsToSelect.add("activityName");
		fieldsToSelect.add("productId");
		fieldsToSelect.add("collectionNum");
		fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("publishDate");
        fieldsToSelect.add("endDate");
        
		List<String> orderBy = FastList.newInstance();
		
		List<EntityCondition> filedExprs = FastList.newInstance();
		EntityCondition mainCond = null;

		//按活动ID模糊查询
        if (UtilValidate.isNotEmpty(activityId)) {
        	filedExprs.add(EntityCondition.makeCondition("activityId", EntityOperator.LIKE,"%"+activityId+"%"));
        }
        
        //按活动名称模糊查询
        if (UtilValidate.isNotEmpty(activityName)) {
        	filedExprs.add(EntityCondition.makeCondition("activityName", EntityOperator.LIKE,"%"+activityName+"%"));
        }
        
        //按活动类型精确查询
        if (UtilValidate.isNotEmpty(activityType)) {
        	filedExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS,activityType));
        }
        
        //按活动状态精确查询
        if (UtilValidate.isNotEmpty(activityAuditStatus)) {
        	Timestamp noTime = UtilDateTime.nowTimestamp();
        	if ("ACTY_AUDIT_UNBEGIN".equals(activityAuditStatus)) {
                //未开始（auditStatus为审批通过并且系统当前时间小于销售开始时间）
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_DOING".equals(activityAuditStatus)) {
                //进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityStartDate"), EntityOperator.LESS_THAN, noTime));
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_END".equals(activityAuditStatus)) {
                //已结束（auditStatus为审批通过并且系统当前时间大于等于销售结束时间小于下架时间）
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityEndDate"), EntityOperator.LESS_THAN, noTime));
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.GREATER_THAN_EQUAL_TO, noTime));
            } else if ("ACTY_AUDIT_OFF".equals(activityAuditStatus)) {
                //已下架（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("activityAuditStatus"), EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            	filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("endDate"), EntityOperator.LESS_THAN, noTime));
            }
        }
		//添加where条件
        if (filedExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }
		
		//排序字段名称
        String sortField = "activityId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
        	sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
        	sortType = (String)context.get("sortType");
        }
        orderBy.add(sortType+sortField);
        
		try {
			lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, orderBy, findOpts);
			for(GenericValue gv : pli.getPartialList(lowIndex, viewSize)){
				Map map = FastMap.newInstance();
				map.put("activityId",gv.getString("activityId"));
				map.put("activityName",gv.getString("activityName"));
				map.put("activityTypeName",gv.getString("activityTypeName"));
				map.put("activityDesc",gv.getString("activityDesc"));
				map.put("productId",gv.getString("productId"));
				map.put("collectionNum",gv.getString("collectionNum"));
				String activityStatus = "";
				
				Timestamp noTime = UtilDateTime.nowTimestamp();
	        	if ("ACTY_AUDIT_PASS".equals(gv.getString("activityAuditStatus"))
	        				&& (noTime.before(gv.getTimestamp("activityStartDate")) || noTime.equals(gv.getTimestamp("activityStartDate")))) {
	        		activityStatus="未开始";
	            } else if ("ACTY_AUDIT_PASS".equals(gv.getString("activityAuditStatus"))
	            			&& noTime.after(gv.getTimestamp("activityStartDate")) 
	            			&& (noTime.before(gv.getTimestamp("activityEndDate")) || noTime.equals(gv.getTimestamp("activityEndDate")))) {
	            	activityStatus="进行中";
	            } else if ("ACTY_AUDIT_PASS".equals(gv.getString("activityAuditStatus"))
	            			&& noTime.after(gv.getTimestamp("activityEndDate"))
	            			&& (noTime.before(gv.getTimestamp("endDate")) || noTime.before(gv.getTimestamp("endDate")))) {
	            	activityStatus="已结束";
	            } else if ("ACTY_AUDIT_PASS".equals(gv.getString("activityAuditStatus"))
	            			&& noTime.after(gv.getTimestamp("endDate"))) {
	            	activityStatus="已下架";
	            }
				map.put("activityAuditStatus",activityStatus);
				recordsList.add(map);
			}
			//获取全部
			totalSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > totalSize)
			{
				highIndex = totalSize;
			}
			//关闭iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("recordsList", recordsList);
		result.put("totalSize", Integer.valueOf(totalSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		return result;
	}
	/**
	 * 查询收藏的会员信息add by gss 2016/1/24
	 * @param dctx
	 * @param context
	 * @throws GenericServiceException
	 * @throws GenericEntityException
	 * @return
	 */
	public static Map<String, Object> findPartyInfo(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		Map<String, Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		Locale locale = (Locale) context.get("locale");
		//活动编码
		String activityId = (String) context.get("activityId");
		int totalSize = 0;
		int lowIndex = 0;
		int highIndex = 0;
		// 设置页数
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		result.put("viewIndex", Integer.valueOf(viewIndex));

		int viewSize = 2;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 2;
		}
		result.put("viewSize", Integer.valueOf(viewSize));
		//查询的活动列表
		List<GenericValue> partyList = null;
		DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("PAC", "ProductActivityCollection");//活动表
		
		dve.addMemberEntity("PL", "PartyLevel");//会员表等级
		dve.addMemberEntity("PLT", "PartyLevelType");//会员分类
		dve.addMemberEntity("PS", "Person");//人员表
		dve.addAliasAll("PAC", "", null);
		dve.addAliasAll("PL", "", null);
		dve.addAliasAll("PLT", "", null);
		dve.addAliasAll("PS", "", null);
        //活动ID
		dve.addAlias("PAC", "activityId");
		//活动收藏会员ID
		dve.addAlias("PAC", "partyId");
		//收藏时间
		dve.addAlias("PAC", "createdStamp");
		//会员等级名称
		dve.addAlias("PLT", "levelName");
	    //Person表中的手机号
	    dve.addAlias("PS", "mobile");
	    //Person表中的邮箱
	    dve.addAlias("PS", "email");
		//会员等级与会员等级类型关联关系
		dve.addViewLink("PL", "PLT",Boolean.FALSE, ModelKeyMap.makeKeyMapList("levelId", "levelId"));
		//会员等级与会员表关系
		dve.addViewLink("PL", "PAC",Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
		//会员与Person表关系
		dve.addViewLink("PAC", "PS",Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
		//需要查询的字段信息
		List<String> fieldsToSelect = FastList.newInstance();
		//会员ID
		fieldsToSelect.add("partyId");
		//会员等级
		fieldsToSelect.add("levelName");
		//手机号
		fieldsToSelect.add("mobile");
		//Email
		fieldsToSelect.add("email");
		//收藏时间
		fieldsToSelect.add("createdStamp");
		//创建查询条件
		List<EntityCondition> andExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		//活动Id
		if (UtilValidate.isNotEmpty(activityId))
		{
			andExprs.add(EntityCondition.makeCondition("activityId",
					EntityOperator.EQUALS, activityId));
		}
		//创建主要条件
		if (andExprs.size() > 0)
		{
			mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
		}
		try {
			lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
			//去重复
            EntityFindOptions findOpts = new EntityFindOptions(true,
         					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
         					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //得到iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dve,
					mainCond, null, fieldsToSelect, null, findOpts);
			partyList = pli.getPartialList(lowIndex, viewSize);
			//获取全部
			totalSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > totalSize)
			{
				highIndex = totalSize;
			}
			//关闭iterator
			pli.close();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));
		result.put("partyList", partyList);
		result.put("totalSize", Integer.valueOf(totalSize));
		return result;
	}
	
}
