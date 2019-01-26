package com.qihua.ofbiz.notice;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * notice service
 *
 * @author AlexYao 2016/04/06
 */
public class NoticeServices {
	public static final String module = NoticeServices.class.getName();
	public static final String resource = "ProductUiLabels";

	/**
	 * 公告列表查询 add by AlexYao 2016/04/06
	 *
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getNoticeList(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();

		// 记录集合
		List<Map> noticeList = FastList.newInstance();

		// 总记录数
		int noticeListSize = 0;
		// 查询开始条数
		int lowIndex = 0;
		// 查询结束条数
		int highIndex = 0;

		// 跳转的页数
		int viewIndex = 0;
		try {
			viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
		} catch (Exception e) {
			viewIndex = 0;
		}
		result.put("viewIndex", Integer.valueOf(viewIndex));

		// 每页显示记录条数
		int viewSize = 20;
		try {
			viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
		} catch (Exception e) {
			viewSize = 20;
		}
		result.put("viewSize", Integer.valueOf(viewSize));

		// 动态view
		DynamicViewEntity dynamicView = new DynamicViewEntity();
		// 查询条件集合，用于数据库查询
		List<EntityCondition> filedExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		// 排序字段集合
		List<String> orderBy = FastList.newInstance();
		// 显示字段集合
		List<String> fieldsToSelect = FastList.newInstance();
		dynamicView.addMemberEntity("N", "Notice");
		dynamicView.addAlias("N", "noticeId");
		dynamicView.addAlias("N", "noticeTitle");
		dynamicView.addAlias("N", "linkUrl");
		dynamicView.addAlias("N", "isUse");
		dynamicView.addAlias("N", "sequenceId");
		dynamicView.addAlias("N", "isAllWebSite");
		dynamicView.addAlias("N", "isAllGeo");

		dynamicView.addMemberEntity("NWS", "NoticeWebSite");
		dynamicView.addAlias("NWS", "noticeWebSiteId");
		dynamicView.addAlias("NWS", "webSiteId");
		dynamicView.addViewLink("N", "NWS", Boolean.TRUE, ModelKeyMap.makeKeyMapList("noticeId", "noticeId"));

		dynamicView.addMemberEntity("WS", "WebSite");
		dynamicView.addAlias("WS", "webSiteId");
		dynamicView.addAlias("WS", "siteName");
		dynamicView.addViewLink("NWS", "WS", Boolean.TRUE, ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));

		dynamicView.addMemberEntity("NG", "NoticeGeo");
		dynamicView.addAlias("NG", "noticeGeoId");
		dynamicView.addAlias("NG", "geoId");
		dynamicView.addViewLink("N", "NG", Boolean.TRUE, ModelKeyMap.makeKeyMapList("noticeId", "noticeId"));

		dynamicView.addMemberEntity("G", "Geo");
		dynamicView.addAlias("G", "geoId");
		dynamicView.addAlias("G", "geoName");
		dynamicView.addViewLink("NG", "G", Boolean.TRUE, ModelKeyMap.makeKeyMapList("geoId", "geoId"));

		fieldsToSelect.add("noticeId");
		fieldsToSelect.add("noticeTitle");
		fieldsToSelect.add("linkUrl");
		fieldsToSelect.add("isUse");
		fieldsToSelect.add("sequenceId");
		fieldsToSelect.add("isAllWebSite");
		fieldsToSelect.add("isAllGeo");

		// 按站点名称模糊查询
		if (UtilValidate.isNotEmpty(context.get("noticeTitle"))) {
			filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("noticeTitle"), EntityOperator.LIKE,
					EntityFunction.UPPER("%" + context.get("noticeTitle") + "%")));
		}

		// 排序字段名称
		String sortField = "sequenceId";
		if (UtilValidate.isNotEmpty(context.get("sortField"))) {
			sortField = (String) context.get("sortField");
		}
		// 排序类型
		String sortType = "";
		if (UtilValidate.isNotEmpty(context.get("sortType"))) {
			sortType = (String) context.get("sortType");
		}
		orderBy.add(sortType + sortField);

		// 添加where条件
		if (filedExprs.size() > 0) {
			mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
		}

		try {
			lowIndex = viewIndex * viewSize + 1;
			highIndex = (viewIndex + 1) * viewSize;

			// 去除重复数据
			EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
			// 查询的数据Iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect,
					orderBy, findOpts);
			// 获取分页所需的记录集合
			for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
				String noticeId = gv.getString("noticeId");
				String noticeTitle = gv.getString("noticeTitle");
				String linkUrl = gv.getString("linkUrl");
				String isUse = gv.getString("isUse");
				String sequenceId = gv.getString("sequenceId");
				String isAllWebSite = gv.getString("isAllWebSite");
				String isAllGeo = gv.getString("isAllGeo");

				Map map = FastMap.newInstance();
				map.put("noticeId", noticeId);
				map.put("noticeTitle", noticeTitle);
				map.put("linkUrl", linkUrl);
				map.put("isUse", isUse);
				map.put("sequenceId", sequenceId);
				// 获取站点名称
				String webSiteName = "";
				if ("1".equals(isAllWebSite)) {
					// 动态view
					DynamicViewEntity webSite_dv = new DynamicViewEntity();
					webSite_dv.addMemberEntity("NWS", "NoticeWebSite");
					webSite_dv.addAlias("NWS", "noticeWebSiteId");
					webSite_dv.addAlias("NWS", "noticeId");
					webSite_dv.addAlias("NWS", "webSiteId");

					webSite_dv.addMemberEntity("WS", "WebSite");
					webSite_dv.addAlias("WS", "webSiteId");
					webSite_dv.addAlias("WS", "siteName");
					webSite_dv.addViewLink("NWS", "WS", Boolean.FALSE,
							ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));

					List<String> ws_fieldsToSelect = FastList.newInstance();
					ws_fieldsToSelect.add("noticeWebSiteId");
					ws_fieldsToSelect.add("noticeId");
					ws_fieldsToSelect.add("webSiteId");
					ws_fieldsToSelect.add("siteName");

					// 编辑where条件
					EntityCondition ws_whereCond = EntityCondition.makeCondition("noticeId", EntityOperator.EQUALS,
							noticeId);

					try {
						// 查询的数据Iterator
						EntityListIterator ws_pli = delegator.findListIteratorByCondition(webSite_dv, ws_whereCond,
								null, ws_fieldsToSelect, null, findOpts);
						for (int i = 0; i < ws_pli.getCompleteList().size(); i++) {
							String siteName = ws_pli.getCompleteList().get(i).getString("siteName");
							if (i == 0) {
								webSiteName += siteName;
							} else {
								webSiteName += "，" + siteName;
							}
						}
						// 关闭pli
						ws_pli.close();
					} catch (GenericEntityException e) {
						Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
					}

				} else {
					webSiteName = "全部站点";
				}

				// 获取地区区名称
				String geoName = "";
				/*
				 * if ("1".equals(isAllGeo)) { //动态view DynamicViewEntity
				 * community_dv = new DynamicViewEntity();
				 * community_dv.addMemberEntity("NG", "NoticeGeo");
				 * community_dv.addAlias("NG", "noticeGeoId");
				 * community_dv.addAlias("NG", "noticeId");
				 * community_dv.addAlias("NG", "geoId");
				 * 
				 * community_dv.addMemberEntity("G", "Geo");
				 * community_dv.addAlias("G", "geoId");
				 * community_dv.addAlias("G", "geoName");
				 * community_dv.addViewLink("NG", "G", Boolean.FALSE,
				 * ModelKeyMap.makeKeyMapList("geoId", "geoId"));
				 * 
				 * List<String> community_fieldsToSelect =
				 * FastList.newInstance();
				 * community_fieldsToSelect.add("noticeGeoId");
				 * community_fieldsToSelect.add("noticeId");
				 * community_fieldsToSelect.add("geoId");
				 * community_fieldsToSelect.add("geoName");
				 * 
				 * //编辑where条件 EntityCondition community_whereCond =
				 * EntityCondition.makeCondition("noticeId",
				 * EntityOperator.EQUALS, noticeId);
				 * 
				 * try { //查询的数据Iterator EntityListIterator community_pli =
				 * delegator.findListIteratorByCondition(community_dv,
				 * community_whereCond, null, community_fieldsToSelect, null,
				 * null); for (int i = 0; i <
				 * community_pli.getCompleteList().size(); i++) { if
				 * (!"CHN".equals(community_pli.getCompleteList().get(i).
				 * getString("geoId"))) { String name =
				 * community_pli.getCompleteList().get(i).getString("geoName");
				 * if (UtilValidate.isEmpty(geoName)) { geoName += name; } else
				 * { geoName += "，" + name; } } } //关闭pli community_pli.close();
				 * } catch (GenericEntityException e) { Debug.logError(e,
				 * "Cannot lookup State Geos: " + e.toString(), module); } }
				 * else { geoName = "全部地区"; }
				 */
				map.put("webSiteName", webSiteName);
				map.put("geoName", geoName);
				noticeList.add(map);
			}

			// 获取总记录数
			noticeListSize = pli.getResultsSizeAfterPartialList();
			if (highIndex > noticeListSize) {
				highIndex = noticeListSize;
			}

			// 关闭 iterator
			pli.close();
		} catch (GenericEntityException e) {
			String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
			Debug.logError(e, errMsg, module);
		}

		// 返回的参数
		result.put("noticeList", noticeList);
		result.put("totalSize", Integer.valueOf(noticeListSize));
		result.put("highIndex", Integer.valueOf(highIndex));
		result.put("lowIndex", Integer.valueOf(lowIndex));

		return result;
	}

	/**
	 * 公告修改是否启用状态 add by qianjin 2016/04/06
	 *
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> editNoticeIsUse(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 公告ID
		String noticeId = (String) context.get("noticeId");
		// 是否启用状态
		String isUse = (String) context.get("isUse");

		try {
			// 根据ID获取公告记录并修改
			GenericValue notice_gv = delegator.findByPrimaryKey("Notice", UtilMisc.toMap("noticeId", noticeId));
			notice_gv.setString("isUse", isUse);
			notice_gv.store();
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据类型ID获取enum数据 add by AlexYao 2016/04/06
	 *
	 * @param delegator
	 * @param enumTypeId
	 * @return
	 */
	public static List getEnumByTypeId(Delegator delegator, String enumTypeId) {
		// 返回的数据List
		List enumList = FastList.newInstance();
		try {
			// 根据类型ID获取enum数据
			Map paramMap = UtilMisc.toMap("enumTypeId", enumTypeId,"enumCode" , "N");
			List orderBy = UtilMisc.toList("sequenceId");
			enumList = delegator.findList("Enumeration", EntityCondition.makeCondition(paramMap), null, orderBy, null,
					false);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return enumList;
	}

	/**
	 * 公告新增 add by AlexYao 2016/04/06
	 *
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> noticeAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 参数
		String noticeTitle = (String) context.get("noticeTitle");
		String firstLinkType = (String) context.get("firstLinkType");
		String secondLinkType = (String) context.get("secondLinkType");
		String linkUrl = (String) context.get("linkUrl");
		String linkId = (String) context.get("linkId");
		String linkName = (String) context.get("linkName");
		Long sequenceId = (Long) context.get("sequenceId");
		String isUse = (String) context.get("isUse");
		String isAllWebSite = (String) context.get("isAllWebSite");
		String webSite = (String) context.get("webSite");
		String isAllGeo = (String) context.get("isAllGeo");
		String geo = (String) context.get("geo");
//		String applyScope = (String) context.get("applyScope");

		try {
			// 大于当前排序的sequenceId+1
			List<GenericValue> noticeList = delegator.findByAnd("Notice", UtilMisc.toMap("sequenceId", sequenceId));
			if (UtilValidate.isNotEmpty(noticeList)) {
				try {
					// 使用源生的JDBC方式
					String sql = "UPDATE notice b set b.SEQUENCE_ID = b.SEQUENCE_ID+1 " + "WHERE b.SEQUENCE_ID >= "
							+ sequenceId;
					// 获得gropuHelperName
					String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
					// 获得数据库的连接
					Connection conn = ConnectionFactory.getConnection(groupHelperName);
					// 获得Statement
					Statement stmt = conn.createStatement();
					// 执行sql
					stmt.executeUpdate(sql);
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			String noticeId = delegator.getNextSeqId("Notice");
			// 新增一条公告记录
			GenericValue notice_gv = delegator.makeValue("Notice", UtilMisc.toMap("noticeId", noticeId));
			notice_gv.setString("noticeTitle", noticeTitle);
			notice_gv.setString("firstLinkType", firstLinkType);
			notice_gv.setString("secondLinkType", secondLinkType);
			notice_gv.setString("linkUrl", linkUrl);
			notice_gv.setString("linkId", linkId);
			notice_gv.setString("linkName", linkName);
			notice_gv.setString("sequenceId", sequenceId.toString());
			notice_gv.setString("isUse", isUse);
			notice_gv.setString("isAllWebSite", isAllWebSite);
			notice_gv.setString("isAllGeo", isAllGeo);
//			notice_gv.setString("applyScope", applyScope);

			// 标签处理
			String noticeTag = (String) context.get("noticeTag");
			notice_gv.set("tagId", noticeTag);

			notice_gv.create();

			if (UtilValidate.isNotEmpty(webSite)) {
				for (String ws_id : webSite.split(",")) {
					String nwsId = delegator.getNextSeqId("NoticeWebSite");
					// 新增一条公告站点记录
					GenericValue bws_gv = delegator.makeValue("NoticeWebSite",
							UtilMisc.toMap("noticeWebSiteId", nwsId));
					bws_gv.setString("noticeId", noticeId);
					bws_gv.setString("webSiteId", ws_id);
					bws_gv.create();
				}
			}

			if (UtilValidate.isNotEmpty(geo)) {
				for (String g_id : geo.split(",")) {
					String ngId = delegator.getNextSeqId("NoticeGeo");
					// 新增一条公告地区记录
					GenericValue ng_gv = delegator.makeValue("NoticeGeo", UtilMisc.toMap("noticeGeoId", ngId));
					ng_gv.setString("noticeId", noticeId);
					ng_gv.setString("geoId", g_id);
					ng_gv.create();
				}
			}

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 查询地区树，树的根节点为CHN ，查询每个父节点对应子节点需要使用递归方法 Add By AlexYao
	 *
	 * @param dcx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> findAreaTree(DispatchContext dcx, Map<String, ? extends Object> context) {
		List tree = FastList.newInstance();
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dcx.getDelegator();
		Map<String, String> oriMap = new HashMap<String, String>();
		oriMap.put("id", "CHN");
		oriMap.put("name", "中国");
		oriMap.put("pId", "");
		tree.add(oriMap);
		try {
			List<GenericValue> geoAssocs = delegator.findByAnd("GeoAssoc",
					UtilMisc.toMap("geoId", "CHN", "geoAssocTypeId", "REGIONS"));
			if (UtilValidate.isNotEmpty(geoAssocs)) {
				for (GenericValue geoAssoc : geoAssocs) {
					String geoId = geoAssoc.getString("geoIdTo");
					GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", geoId));
					String name = geo.getString("geoName");
					String parentGeoId = geoAssoc.getString("geoIdTo");
					Map<String, String> map = new HashMap<String, String>();
					map.put("id", geoId);
					map.put("name", name);
					map.put("pId", "CHN");
					tree.add(map);
					areaRecursive(tree, delegator, parentGeoId);
				}
			}
		} catch (GenericEntityException e) {
			ServiceUtil.returnError(e.getMessage());
		}
		result.put("areaTree", tree);
		return result;
	}

	private static void areaRecursive(List<Map<String, String>> list, Delegator delegator, String pGeoId) {
		try {
			List<GenericValue> geoAssocs = delegator.findByAnd("GeoAssoc",
					UtilMisc.toMap("geoId", pGeoId, "geoAssocTypeId", "REGIONS"));
			if (UtilValidate.isNotEmpty(geoAssocs)) {
				for (GenericValue geoAssoc : geoAssocs) {
					String geoId = geoAssoc.getString("geoIdTo");
					GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", geoId));
					String name = geo.getString("geoName");
					String parentGeoId = geoAssoc.getString("geoId");
					Map<String, String> map = new HashMap<String, String>();
					map.put("id", geoId);
					map.put("name", name);
					map.put("pId", parentGeoId);
					list.add(map);
					areaRecursive(list, delegator, geoId);
				}
			}
		} catch (GenericEntityException e) {
			ServiceUtil.returnError(e.getMessage());
		}
	}

	/**
	 * 公告修改数据初始化 add by AlexYao 2016/04/06
	 *
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> noticeEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 参数
		String noticeId = (String) context.get("noticeId");

		try {
			GenericValue notice_gv = delegator.findByPrimaryKey("Notice", UtilMisc.toMap("noticeId", noticeId));
			result.put("noticeTitle", notice_gv.get("noticeTitle"));
			result.put("firstLinkType", notice_gv.get("firstLinkType"));
			result.put("secondLinkType", notice_gv.get("secondLinkType"));
			result.put("linkUrl", notice_gv.get("linkUrl"));
			result.put("linkId", notice_gv.get("linkId"));
			result.put("linkName", notice_gv.get("linkName"));
			result.put("sequenceId", notice_gv.get("sequenceId"));
			result.put("isUse", notice_gv.get("isUse"));
			result.put("isAllWebSite", notice_gv.get("isAllWebSite"));
			result.put("isAllGeo", notice_gv.get("isAllGeo"));
			result.put("noticeTag", notice_gv.get("tagId"));
			result.put("applyScope", notice_gv.get("applyScope"));

			// 获取站点
			String ws_arr = "";
			List<GenericValue> ws_list = delegator.findByAnd("NoticeWebSite", UtilMisc.toMap("noticeId", noticeId));
			for (int i = 0; i < ws_list.size(); i++) {
				if (i != ws_list.size() - 1) {
					ws_arr += ws_list.get(i).get("webSiteId") + ",";
				} else {
					ws_arr += ws_list.get(i).get("webSiteId");
				}
			}
			result.put("webSite", ws_arr);
			// 获取地区
			String g_arr = "";
			List<GenericValue> g_list = delegator.findByAnd("NoticeGeo", UtilMisc.toMap("noticeId", noticeId));
			if (UtilValidate.isNotEmpty(g_list)) {
				for (int i = 0; i < g_list.size(); i++) {
					if (i != g_list.size() - 1) {
						g_arr += g_list.get(i).get("geoId") + ",";
					} else {
						g_arr += g_list.get(i).get("geoId");
					}
				}
			}
			result.put("geo", g_arr);

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 公告修改 add by AlexYao 2016/04/06
	 *
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> noticeEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 参数
		String noticeId = (String) context.get("noticeId");
		String noticeTitle = (String) context.get("noticeTitle");
		String firstLinkType = (String) context.get("firstLinkType");
		String secondLinkType = (String) context.get("secondLinkType");
		String linkUrl = (String) context.get("linkUrl");
		String linkId = (String) context.get("linkId");
		String linkName = (String) context.get("linkName");
		Long sequenceId = (Long) context.get("sequenceId");
		String isUse = (String) context.get("isUse");
		String isAllWebSite = (String) context.get("isAllWebSite");
		String webSite = (String) context.get("webSite");
		String isAllGeo = (String) context.get("isAllGeo");
		String geo = (String) context.get("geo");
		String applyScope=(String) context.get("applyScope");


		try {
			// 新增一条公告记录
			GenericValue notice_gv = delegator.findByPrimaryKey("Notice", UtilMisc.toMap("noticeId", noticeId));
			notice_gv.setString("noticeTitle", noticeTitle);
			notice_gv.setString("firstLinkType", firstLinkType);
			notice_gv.setString("secondLinkType", secondLinkType);
			notice_gv.setString("linkUrl", linkUrl);
			notice_gv.setString("linkId", linkId);
			notice_gv.setString("linkName", linkName);
			if (!sequenceId.equals(notice_gv.getLong("sequenceId"))) {
				// 大于当前排序的sequenceId+1
				List<GenericValue> noticeList = delegator.findByAnd("Notice", UtilMisc.toMap("sequenceId", sequenceId));
				if (UtilValidate.isNotEmpty(noticeList)) {
					try {
						// 使用源生的JDBC方式
						String sql = "UPDATE notice b set b.SEQUENCE_ID = b.SEQUENCE_ID+1 " + "WHERE b.SEQUENCE_ID >= "
								+ sequenceId;
						// 获得gropuHelperName
						String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
						// 获得数据库的连接
						Connection conn = ConnectionFactory.getConnection(groupHelperName);
						// 获得Statement
						Statement stmt = conn.createStatement();
						// 执行sql
						stmt.executeUpdate(sql);
						stmt.close();
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			notice_gv.setString("sequenceId", sequenceId.toString());
			notice_gv.setString("isUse", isUse);
			notice_gv.setString("isAllWebSite", isAllWebSite);
			notice_gv.setString("isAllGeo", isAllGeo);

			// 标签处理
			String noticeTag = (String) context.get("noticeTag");
			notice_gv.set("tagId", noticeTag);

			// 适用范围
//			notice_gv.setString("applyScope", applyScope);

			notice_gv.store();

			// 删除该公告的所有站点
			delegator.removeByAnd("NoticeWebSite", UtilMisc.toMap("noticeId", noticeId));
			if (!"0".equals(isAllWebSite) && UtilValidate.isNotEmpty(webSite)) {
				for (String ws_id : webSite.split(",")) {
					String nwsId = delegator.getNextSeqId("NoticeWebSite");
					// 新增一条公告站点记录
					GenericValue nws_gv = delegator.makeValue("NoticeWebSite",
							UtilMisc.toMap("noticeWebSiteId", nwsId));
					nws_gv.setString("noticeId", noticeId);
					nws_gv.setString("webSiteId", ws_id);
					nws_gv.create();
				}
			}

			// 删除该公告的所有地区
			delegator.removeByAnd("NoticeGeo", UtilMisc.toMap("noticeId", noticeId));
			if (!"0".equals(isAllGeo) && UtilValidate.isNotEmpty(geo)) {
				for (String g_id : geo.split(",")) {
					String ngId = delegator.getNextSeqId("NoticeGeo");
					// 新增一条公告地区记录
					GenericValue ng_gv = delegator.makeValue("NoticeGeo", UtilMisc.toMap("noticeGeoId", ngId));
					ng_gv.setString("noticeId", noticeId);
					ng_gv.setString("geoId", g_id);
					ng_gv.create();
				}
			}

		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 根据ID删除公告,可批量删除 add by AlexYao 2016/04/06
	 *
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> noticeDel(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String, Object> result = ServiceUtil.returnSuccess();
		Delegator delegator = dctx.getDelegator();
		// 获取ids参数
		String ids = (String) context.get("ids");
		// 转换成list
		List idList = FastList.newInstance();
		for (String id : ids.split(",")) {
			idList.add(id);
		}
		// 编辑where条件
		EntityCondition mainCond = EntityCondition.makeCondition("noticeId", EntityOperator.IN, idList);

		try {
			delegator.removeByCondition("NoticeGeo", mainCond);
			delegator.removeByCondition("NoticeWebSite", mainCond);
			delegator.removeByCondition("Notice", mainCond);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}

		return result;
	}
}
