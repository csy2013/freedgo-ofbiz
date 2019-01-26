package com.qihua.ofbiz.party.party;

import java.io.IOException;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.service.DispatchContext;

import com.qihua.ofbiz.party.common.HttpUtil;

import javolution.util.FastMap;
import net.sf.json.JSONObject;

/**
 * Crm系统权限信息同步
 * 
 * @author liruishi
 *
 */
public class CrmSyncPrivilege {
	// 修改角色信息
	private final static String MODIFY_ROLE = "/CRM/privilege/modifyRole.do";

	/**
	 * 更改角色
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> modifyRole(DispatchContext dctx, Map<String, Object> context) {
		String url = getBaseUrl() + MODIFY_ROLE;
		JSONObject jsonOb = JSONObject.fromObject(doPost(url, context));
		return (Map<String, Object>) JSONObject.toBean(jsonOb, Map.class);
	}

	private final static String ADD_ROLE = "/CRM/privilege/addRole.do";

	/**
	 * 添加角色
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> addRole(DispatchContext dctx, Map<String, Object> context) {
		String url = getBaseUrl() + ADD_ROLE;
		JSONObject jsonOb = JSONObject.fromObject(doPost(url, context));
		return (Map<String, Object>) JSONObject.toBean(jsonOb, Map.class);
	}

	private final static String SAVE_ROLE_PERMISSION = "/CRM/privilege/saveRolePermission.do";

	/**
	 * 保存角色权限
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> saveRolePermission(DispatchContext dctx, Map<String, Object> context) {
		String url = getBaseUrl() + SAVE_ROLE_PERMISSION;
		JSONObject jsonOb = JSONObject.fromObject(doPost(url, context));
		return (Map<String, Object>) JSONObject.toBean(jsonOb, Map.class);
	}

	private final static String GET_MENUS_BY_ROLE_ID = "/CRM/privilege/getMenuIdsByRoleId.do";

	/**
	 * 根据角色id查询菜单信息
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getMenusByRoleId(DispatchContext dctx, Map<String, Object> context) {
		String url = getBaseUrl() + GET_MENUS_BY_ROLE_ID;
		JSONObject jsonOb = JSONObject.fromObject(doPost(url, context));

		Map<String, Object> rs = getBaseReturnMap(jsonOb);
		// 将data数据以json字符串形式取出
		rs.put("data", jsonOb.getString("data"));
		return rs;
	}

	private final static String GET_BRAND_MENU_LIST = "/privilege/getBrandMenuList";

	/**
	 * 获取商户菜单列表
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> getBrandMenuList(DispatchContext dctx, Map<String, Object> context) {
		String url = getBaseUrl() + GET_BRAND_MENU_LIST;
		JSONObject jsonOb = JSONObject.fromObject(doPost(url, context));
		Map<String, Object> rs = getBaseReturnMap(jsonOb);
		rs.put("data", jsonOb.getString("data"));

		return rs;
	}

	private static Map<String, Object> getBaseReturnMap(JSONObject jsonOb) {
		Map<String, Object> map = FastMap.newInstance();
		map.put("status", jsonOb.getString("status"));
		map.put("code", jsonOb.getInt("code"));
		map.put("msg", jsonOb.getString("msg"));

		map.put("requestId", jsonOb.getString("requestId"));
		map.put("receivetime", jsonOb.getString("receivetime"));
		/*map.put("syscode", jsonOb.getString("syscode"));
		map.put("itfcode", jsonOb.getString("itfcode"));*/

		return map;
	}

	/**
	 * 获取请求地址的基本URL
	 * 
	 * @return
	 */
	private final static String getBaseUrl() {
		return UtilProperties.getPropertyValue("security.properties", "crm.url");
	}

	/*
	 * 向CRM系统发出请求
	 */
	private static String doPost(String url, Map<String, Object> data) {

		Debug.log("请求数据:" + data.toString());
		try {
			return HttpUtil.post(url, null, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
