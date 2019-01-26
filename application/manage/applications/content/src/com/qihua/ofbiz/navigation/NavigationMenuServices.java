package com.qihua.ofbiz.navigation;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

//delegator.getNextSeqId("LogisticsTemple"); //获取主键
public class NavigationMenuServices {
	public static final String module = NavigationMenuServices.class.getName();

	private final static String ERROR = "error";

	/**
	 * 查询导航菜单信息
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryNavMenu(DispatchContext dctx, Map<String, Object> context)
			throws GenericEntityException {
		Map<String, Object> rs = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		// 排序字段列表
		List<String> orderBys = FastList.newInstance();
		orderBys.add("seqNo ASC");
		// 根据序号排序
		// List<GenericValue> list = delegator.findList("NavigationMenu", null,
		// null, orderBys, null, false);
		// 查询站点信息

		// 修改为动态查询
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		dynamicView.addMemberEntity("NM", "NavigationMenu");
		dynamicView.addAliasAll("NM", "", null);
		// 导航菜单类型
//		dynamicView.addMemberEntity("EN", "Enumeration");
//		dynamicView.addAlias("EN", "navTypeName", "description", null, false, false, null);
//		dynamicView.addViewLink("NM", "EN", Boolean.FALSE, ModelKeyMap.makeKeyMapList("navType", "enumId"));

		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("navId");
		fieldsToSelect.add("navName");
		fieldsToSelect.add("navUrl");
		fieldsToSelect.add("navDesc");
		fieldsToSelect.add("isEnabled");
		fieldsToSelect.add("seqNo");
		fieldsToSelect.add("exPlat");
		fieldsToSelect.add("navType");
		fieldsToSelect.add("contentId");
		fieldsToSelect.add("isAllWebSite");
//		fieldsToSelect.add("navTypeName");
		fieldsToSelect.add("firstLinkType");
		fieldsToSelect.add("linkUrl");
		fieldsToSelect.add("linkId");
		fieldsToSelect.add("linkName");


		EntityListIterator elIter = delegator.findListIteratorByCondition(dynamicView, null, null, fieldsToSelect,
				orderBys, null);
		List<GenericValue> list = elIter.getCompleteList();
		elIter.close();
		rs.put("navMenus", list);
		rs.put("totalSize", list.size());
		return rs;
	}

	/**
	 * 根据菜单的id查询菜单所使用的站点的id
	 * 
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> queryNavWebSites(DispatchContext dctx, Map<String, Object> context)
			throws GenericEntityException {
		Map<String, Object> rs = getResult();
		Delegator delegator = dctx.getDelegator();
		String navId = (String) context.get("navId");
		List<GenericValue> list = delegator.findByAnd("NavigationMenuWebSiteRef", UtilMisc.toMap("navId", navId));
		rs.put("webSites", list);
		return rs;

	}

	/**
	 * 根据主键删除导航菜单信息
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> delNavMenu(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> rs = getResult();
		// 根据条件删除
		String ids = (String) context.get("navIds");

		if (isNotBlank(ids)) {
			try {
				String[] idArr = ids.split(",");
				List<String> list = Arrays.asList(idArr);
				// IN类型条件
				EntityCondition delCond = EntityCondition.makeCondition("navId", EntityOperator.IN, list);
				// 关链表删除
				delegator.removeByCondition("NavigationMenuWebSiteRef", delCond);
				delegator.removeByCondition("NavigationMenu", delCond);

			} catch (GenericEntityException e) {
				// 删除失败,事物回滚
				try {
					TransactionUtil.rollback();
				} catch (GenericTransactionException e1) {
					Debug.logError(e, module);
				}
				rs.put("status", ERROR);
				Debug.logError(e, module);
			}
		} else {
			rs.put("status", ERROR);
			rs.put("msg", "无删除数据！");
		}

		return rs;
	}

	/**
	 * 导航菜单新增
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> saveNavMenu(DispatchContext dctx, Map<String, Object> context) {
		Map<String, Object> rs = getResult();
		Delegator delegator = dctx.getDelegator();

		// 展示前台字段处理
		context.put("exPlat", convertList2Str(context.get("exPlat")));
		// 站点处理
		String isAllWebSite = (String) context.get("isAllWebSite");
		try {
			String navId = delegator.getNextSeqId("NavigationMenu");

			String firstLinkType = (String)context.get("firstLinkType");
			String linkUrl = (String)context.get("linkUrl");
			String linkId = (String)context.get("linkId");
			String linkName = (String)context.get("linkName");
			// 序号更新失败,数据回滚
			updateSeqNo(delegator, (Long) context.get("seqNo"), null);
			// 获取主键
			GenericValue val = delegator.makeValue("NavigationMenu", UtilMisc.toMap("navId", navId));

			if (isBlank(isAllWebSite)) {
				context.put("isAllWebSite", "1");
			} else {
				context.put("isAllWebSite", isAllWebSite);
			}
			val.setNonPKFields(context);
			val.create();
			if (isBlank(isAllWebSite)) {
				// 未选择全部站点
				@SuppressWarnings("unchecked")
				List<String> websites = (List<String>) context.get("webSite");
				if (UtilValidate.isEmpty(websites)) {
					TransactionUtil.rollback();
					// 未选择站点，错误返回
					rs.put("msg", "未选择站点");
					rs.put("status", ERROR);
					return rs;
				} else {
					// 保存菜单站点关联信息
					saveNavMenuWebSiteRef(websites, navId, delegator);
				}
			}

		} catch (Exception e) {
			// 数据保存失败
			e.printStackTrace();
			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				// 数据回滚失败
				e1.printStackTrace();
			}
			rs.put("status", ERROR);
		}
		return rs;
	}

	// 保存菜单站点关联信息
	private static void saveNavMenuWebSiteRef(List<String> webSites, String navId, Delegator delegator)
			throws GenericEntityException {

		for (String webSite : webSites) {
			GenericValue val = delegator.makeValue("NavigationMenuWebSiteRef",
					UtilMisc.toMap("id", delegator.getNextSeqId("NavigationMenuWebSiteRef")));
			val.set("navId", navId);
			val.set("webSiteId", webSite);
			val.create();
		}

	}

	// 删除关系表数据
	private static void delNavMenuWebSiteRef(String navId, Delegator delegator) throws GenericEntityException {
		delegator.removeByAnd("NavigationMenuWebSiteRef", UtilMisc.toMap("navId", navId));
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

	/**
	 * 取消禁用，启用
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateNavStatus(DispatchContext dctx, Map<String, Object> context) {
		Map<String, Object> rs = getResult();
		Delegator delegator = dctx.getDelegator();
		String navId = (String) context.get("navId");
		String isEnabled = (String) context.get("isEnabled");
		// 根据id查询然后更新
		try {
			GenericValue entity = delegator.findByPrimaryKey("NavigationMenu", UtilMisc.toMap("navId", navId));
			if (UtilValidate.isNotEmpty(entity)) {
				if ("Y".equals(isEnabled)) {
					entity.set("isEnabled", "N");
				} else if ("N".equals(isEnabled)) {
					entity.set("isEnabled", "Y");
				}
				delegator.store(entity);
			} else {
				rs.put("status", "error");
				rs.put("msg", "导航菜单或更新状态不存在！");
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			// 更新异常
			rs.put("status", "error");
			rs.put("msg", e.getMessage());
		}
		return rs;
	}

	private static Map<String, Object> getResult() {
		Map<String, Object> rs = FastMap.newInstance();
		// 响应结果，默认成功
		rs.put("status", "success");
		return rs;
	}

	// 录入相同的排序则同序号及N+1的序号都+1
	public static void updateSeqNo(Delegator delegator, Long seqNo, Object id)
			throws SQLException, GenericEntityException {
		// 大于当前排序的sequenceId+1
		List<GenericValue> bannerList;
		bannerList = delegator.findByAnd("NavigationMenu", UtilMisc.toMap("seqNo", seqNo));

		if (UtilValidate.isNotEmpty(bannerList)) {
//			// 使用源生的JDBC方式
//			String sql = "UPDATE navigation_menu b set b.seq_no = b.seq_no+1 " + "WHERE b.seq_no >= " + seqNo;
//			if (UtilValidate.isNotEmpty(id)) {
//				sql += " and nav_id='" + id + "'";
//			}
//			// 获得gropuHelperName
//			String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
//			// 获得数据库的连接
//			Connection conn = ConnectionFactory.getConnection(groupHelperName);
//			// 获得Statement
//			Statement stmt = conn.createStatement();
//			// 执行sql
//			stmt.executeUpdate(sql);
//			stmt.close();

			List<EntityCondition> andExprs = FastList.newInstance();
			EntityCondition mainCond = null;
			List<GenericValue> navigationMenuList = delegator.findByAnd("NavigationMenu",UtilMisc.toMap("seqNo",seqNo));
			if(UtilValidate.isNotEmpty(navigationMenuList)){
				andExprs.add(EntityCondition.makeCondition("seqNo", EntityOperator.GREATER_THAN_EQUAL_TO, seqNo));
				if (UtilValidate.isNotEmpty(id)) {
					andExprs.add(EntityCondition.makeCondition("navId", EntityOperator.NOT_EQUAL, id));
				}
				if (andExprs.size() > 0) {
					mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
					//去除重复数据
					EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, false);
					EntityListIterator pli = delegator.find("NavigationMenu", mainCond, null, null, null, findOpts);
					List<GenericValue> seqList = pli.getCompleteList();
					if (UtilValidate.isNotEmpty(seqList)) {
						for (GenericValue gv : seqList) {
							Long curSeqNo = gv.getLong("seqNo")+ Long.valueOf("1");
							gv.setString("seqNo", curSeqNo.toString());
							gv.store();
						}
					}
					pli.close();
				}
			}
		}



		if(UtilValidate.isNotEmpty(seqNo)){
		}
	}

	/**
	 * 更新导航菜单
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateNavMenu(DispatchContext dctx, Map<String, Object> context) {
		Map<String, Object> rs = getResult();
		Delegator delegator = dctx.getDelegator();
		// 展示前台字段处理
		context.put("exPlat", convertList2Str(context.get("exPlat")));
		String navId = (String) context.get("navId");
		String isAllWebSite = (String) context.get("isAllWebSite");

		try {
			TransactionUtil.begin();
			// 删除关联表数据
			delNavMenuWebSiteRef(navId, delegator);

			// 序号更新失败,数据回滚
			updateSeqNo(delegator, (Long) context.get("seqNo"), navId);
			// 持久化
			String firstLinkType = (String)context.get("firstLinkType");
			String linkUrl = (String)context.get("linkUrl");
			String linkId = (String)context.get("linkId");
			String linkName = (String)context.get("linkName");

			GenericValue val = delegator.makeValue("NavigationMenu", UtilMisc.toMap("navId", context.get("navId")));
			context.put("isAllWebSite", isBlank(isAllWebSite) ? "1" : isAllWebSite);
			val.setNonPKFields(context);
			val.store();

			if (isBlank(isAllWebSite)) {
				// 未选择全部站点
				@SuppressWarnings("unchecked")
				List<String> websites = (List<String>) context.get("webSite");
				if (UtilValidate.isEmpty(websites)) {
					TransactionUtil.rollback();
					// 未选择站点，错误返回
					rs.put("msg", "未选择站点");
					rs.put("status", ERROR);
					return rs;
				} else {
					// 保存菜单站点关联信息
					saveNavMenuWebSiteRef(websites, navId, delegator);
				}
			}
			TransactionUtil.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				TransactionUtil.rollback();
			} catch (GenericTransactionException e1) {
				// 回滚失败
				e1.printStackTrace();
			}
			rs.put("status", "error");
			rs.put("msg", e.getMessage());
		}

		return rs;
	}

}
