package com.qihua.ofbiz.store;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;

import javolution.util.FastList;
import javolution.util.FastMap;

public class ComunityStoreServices {
	/* 社区店实体名称 */
	private final static String ENTITY_NAME = "CommunityStore";

	private final static String ID_KEY = "commStoreId";

	/**
	 * 社区店信息检索服务（需要分页）
	 * 
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryComunityStore(DispatchContext dctx, Map<String, Object> context)
			throws GenericEntityException {
		Map<String, Object> rs = getResult();
		Delegator delegator = dctx.getDelegator();
		int pageSize = 10;
		int pageNo = 1;

		// 获取页码，页码数据量
		Object ob = context.get("VIEW_SIZE");
		if (ob != null) {
			pageSize = (Integer) ob;
		}
		ob = context.get("VIEW_INDEX");
		if (ob != null) {
			pageNo = (Integer) ob + 1;
		}
		rs.put("viewIndex", pageNo - 1);
		rs.put("viewSize", pageSize);

		// 动态view
		DynamicViewEntity dynamicView = new DynamicViewEntity();

		// 查询条件列
		String storeId = (String) context.get("storeId");
		String storeName = (String) context.get("storeName");
		String storeType = (String) context.get("storeType");
		// 查询条件集合，用于数据库查询
		List<EntityCondition> conds = FastList.newInstance();
		conds.add(EntityCondition.makeCondition("isDelete", EntityOperator.EQUALS, "N"));
		if (isNotBlank(storeId)) {
			conds.add(EntityCondition.makeCondition("commStoreId", EntityOperator.EQUALS, storeId));

		}
		if (isNotBlank(storeName)) {
			conds.add(EntityCondition.makeCondition("storeName", EntityOperator.LIKE, "%" + storeName + "%"));
		}
		if (isNotBlank(storeType)) {
			conds.add(EntityCondition.makeCondition("storeType", EntityOperator.EQUALS, storeType));
		}
		EntityCondition cond = null;
		if (conds.size() > 0) {
			cond = EntityCondition.makeCondition(conds);
		}

		// 显示字段设置
		dynamicView.addMemberEntity("CS", ENTITY_NAME);
		dynamicView.addAlias("CS", "commStoreId");
		dynamicView.addAlias("CS", "storeName");
		dynamicView.addAlias("CS", "isEnabled");
		dynamicView.addAlias("CS", "isDelete");
		dynamicView.addAlias("CS", "storeType");
		dynamicView.addAlias("CS", "address");
		dynamicView.addAlias("CS", "province");
		dynamicView.addAlias("CS", "city");
		dynamicView.addAlias("CS", "county");
		dynamicView.addAlias("CS", "payType");
		dynamicView.addAlias("CS", "businessHours");
		dynamicView.addAlias("CS", "contactMan");
		dynamicView.addAlias("CS", "contactPhone");
		dynamicView.addAlias("CS", "remark");
		dynamicView.addAlias("CS", "iconUrl");

		// 省信息
		dynamicView.addMemberEntity("GP", "Geo");
		// dynamicView.addRelation(type, title, relEntityName, modelKeyMaps);
		dynamicView.addViewLink("CS", "GP", Boolean.FALSE, ModelKeyMap.makeKeyMapList("province", "geoId"));
		dynamicView.addAlias("GP", "provinceName", "geoName", null, false, false, null);

		// dynamicView.addAlias("GP", "geoName");

		dynamicView.addMemberEntity("GC", "Geo");
		dynamicView.addViewLink("CS", "GC", Boolean.FALSE, ModelKeyMap.makeKeyMapList("city", "geoId"));

		dynamicView.addAlias("GC", "cityName", "geoName", null, false, false, null);
		dynamicView.addMemberEntity("GCO", "Geo");
		dynamicView.addViewLink("CS", "GCO", Boolean.FALSE, ModelKeyMap.makeKeyMapList("county", "geoId"));
		dynamicView.addAlias("GCO", "countyName", "geoName", null, false, false, null);
		// dynamicView.addAlias("GCO", "geoName");
		// 排序(升序)
		List<String> orderBys = FastList.newInstance();

		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("commStoreId");
		fieldsToSelect.add("storeName");
		fieldsToSelect.add("isEnabled");
		fieldsToSelect.add("storeType");
		fieldsToSelect.add("address");
		fieldsToSelect.add("payType");
		fieldsToSelect.add("iconUrl");

		fieldsToSelect.add("province");
		fieldsToSelect.add("city");
		fieldsToSelect.add("county");
		fieldsToSelect.add("provinceName");
		fieldsToSelect.add("cityName");
		fieldsToSelect.add("countyName");

		fieldsToSelect.add("businessHours");
		fieldsToSelect.add("contactMan");
		fieldsToSelect.add("contactPhone");
		fieldsToSelect.add("remark");

		orderBys.add("commStoreId ASC");
		EntityListIterator elIter = delegator.findListIteratorByCondition(dynamicView, cond, null, fieldsToSelect,
				orderBys, null);
		int start = (pageNo - 1) * pageSize + 1;
		int end = pageSize * pageNo;

		rs.put("lowIndex", start);
		rs.put("highIndex", end);
		rs.put("storeList", elIter.getPartialList(start, pageSize));
		rs.put("totalSize", elIter.getResultsSizeAfterPartialList());
		elIter.close();
		return rs;
	}

	private static String getReverseStatus(String status) {
		if ("Y".equals(status)) {
            return "N";
        } else if ("N".equals("Y")) {
            return "N";
        }
		throw new RuntimeException("状态不存在！");
	}

	/**
	 * 根据id删除社区店信息
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> delCommunityStore(DispatchContext dctx, Map<String, Object> context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		String ids = (String) context.get("commStoreIds");
		if (isNotBlank(ids)) {
			List<String> idList = Arrays.asList(ids.split(","));
			for(String str : idList) {
				GenericValue communityStore = delegator.findByPrimaryKey(ENTITY_NAME, UtilMisc.toMap(ID_KEY, str));
				communityStore.set("isDelete", "Y");
				delegator.store(communityStore);
			}
//			EntityCondition delCond = EntityCondition.makeCondition(ID_KEY, EntityOperator.IN, idList);
//			dctx.getDelegator().removeByCondition(ENTITY_NAME, delCond);
		} else {
			throw new RuntimeException("社区店的id不能为空！");
		}
		return getResult();
	}

	/**
	 * 保存社区店信息
	 * 
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> saveCommunityStore(DispatchContext dctx, Map<String, Object> context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> rs = getResult();
		// 支付方式处理
		context.put("payType", convertList2Str(context.get("payType")));

		String storeId = delegator.getNextSeqId(ENTITY_NAME);
		GenericValue val = delegator.makeValue(ENTITY_NAME, UtilMisc.toMap(ID_KEY, storeId));
		val.setNonPKFields(context);
		val.set("orderQuantity",0L);
		val.set("isDelete", "N");
		val.create();
		return rs;
	}

	/**
	 * 社区店信息更新
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> updateCommunityStore(DispatchContext dctx, Map<String, Object> context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> rs = getResult();
		// 支付方式处理
		context.put("payType", convertList2Str(context.get("payType")));

		String storeId = (String) context.get(ID_KEY);
		GenericValue val = delegator.makeValue(ENTITY_NAME, UtilMisc.toMap(ID_KEY, storeId));
		val.setNonPKFields(context);
		delegator.store(val);
		return rs;
	}

	/**
	 * 更新社区店状态 Y: 禁用社区店 ，N：启用社区店
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> updateCommunityStoreStatus(DispatchContext dctx, Map<String, Object> context)
			throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> rs = getResult();

		String commStoreId = (String) context.get("commStoreId");
		String isEnabled = (String) context.get("isEnabled");

		if (isNotBlank(commStoreId)) {
			GenericValue entity = delegator.findByPrimaryKey(ENTITY_NAME, UtilMisc.toMap("commStoreId", commStoreId));
			if (UtilValidate.isNotEmpty(entity)) {
				entity.set("isEnabled", isEnabled);
				delegator.store(entity);
				rs.put("isEnabled", isEnabled);
			} else {
				// 传入的社区店id不存在
				rs.put("status", "error");
				rs.put("msg", "社区店不存在！");
			}
		} else {
			rs.put("status", "error");
			rs.put("msg", "更新的社区店为空");

		}
		return rs;
	}

	private static Map<String, Object> getResult() {
		Map<String, Object> rs = FastMap.newInstance();
		// 响应结果，默认成功
		rs.put("status", "success");
		return rs;
	}

	private static String convertList2Str(Object ob) {
		if (ob == null) {
			return "";
		}
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) ob;

		StringBuilder sitesBuilder = new StringBuilder("");
		int size = list.size();
		for (int i = 0; i < size; i++) {
			sitesBuilder.append(list.get(i));
			if (i < size - 1) {
				sitesBuilder.append(",");
			}
		}
		return sitesBuilder.toString();
	}
}
