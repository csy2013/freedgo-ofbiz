package com.qihua.ofbiz.party.party;

import com.qihua.ofbiz.party.common.HttpUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.time.DateFormatUtils;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.common.crm.CrmSynchronizeServices;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.io.IOException;
import java.util.*;

/**
 * Crm系统会员信息同步
 * 
 * @author liruishi
 *
 */

// UserLogin:framework\security\entitydef\entitymodel.xml
// Person:applications\party\entitydef\entitymodel.xml,用户的一些基本信息
// Party:applications\party\entitydef\entitymodel.xml,1501

public class CrmSynchronizeParty extends CrmSynchronizeServices{

	private final static String module = CrmSynchronizeParty.class.getName();

	/** 默认时间格式 */
	private final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss:SSS";
	/** CRM系统和Ico中字段的映射关系 */
	private final static Map<String, String> partyMap = FastMap.newInstance();
	/** 固定值属性 */
	private final static Map<String, String> fixedPropertyMap = FastMap.newInstance();

	private final static String CREATE_URL = "/cust/addCusttomerAndMerge";

	private final static String UPDATE_URL = "/cust/updateCustInfo";

	// Party(PERSON),UserLogin,Person,UserLoginSecurityGroup,PartyLevel,PartyAccount,PartyDataSource,PartyScore,PartyAttribute,PartyRole
	static {
		partyMap.put("productBrandId", "partyId");// 商户id,Person
		partyMap.put("nick", "nickname");// 昵称,Person
		fixedPropertyMap.put("channelId", "123456"); // 渠道id，固定
		partyMap.put("mobile", "mobile"); // 手机号码,Person
		// ========================以上为必要字段===========================
		partyMap.put("realName", "name"); // 真实名称 ,Person
		partyMap.put("gender", "gender");// 性别:F,M，Person
		partyMap.put("mail", "email"); // 邮箱,Person
		partyMap.put("wechat", "openId"); // 微信openid,UserLogin
		partyMap.put("occupation", "occuption");// 职业,Person
		partyMap.put("picUrl", "headphoto"); // 头像地址,Person

		partyMap.put("modifyId", "lastModifiedByUserLogin"); // 修改人id,Party
		partyMap.put("createId", "createdByUserLogin"); // 创建人id,Party
		partyMap.put("modifyTime", "lastModifiedDate"); // 修改时间,Party

		fixedPropertyMap.put("sourceId", "5"); // 来源，固定为5

		// ====================以上为映射好数据==============================
		// allowNotice

		/*
		 * partyMap.put("", "custCode"); // 客户编号 partyMap.put("", "vipCode"); //
		 * 会员编号
		 * 
		 * partyMap.put("", "custType"); // 客户类型
		 * 
		 * partyMap.put("", "sourceSubId"); // 来源子渠道 partyMap.put("",
		 * "postCode"); // 邮编 partyMap.put("", "address"); // 地址
		 * partyMap.put("", "storeId"); // 门店id partyMap.put("", "birthday"); //
		 * 生日
		 * 
		 * partyMap.put("", "qq"); // QQ partyMap.put("", "blog"); // 微博
		 * 
		 * // =============证件================ partyMap.put("", "passportType");
		 * // 证件类型 partyMap.put("passportNumber", "passportNo"); // 证件号,Person
		 * // =============证件================
		 * 
		 * partyMap.put("", "workplace"); // 工作单位地址 partyMap.put("", "hobby");
		 * // 爱好 partyMap.put("", "income"); // 收入 partyMap.put("", "province");
		 * // 省份 partyMap.put("", "city");// 城市 partyMap.put("", "area"); // 地区
		 * 
		 * // DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS")
		 * 
		 * partyMap.put("", "status"); // 状态 partyMap.put("", "isFollow"); //
		 * 是否关注 partyMap.put("", "storeId"); // 门店id partyMap.put("",
		 * "birthday"); // 第三方会员编码
		 * 
		 */
	}

	/**
	 * 会员新增
	 * 
	 * @return
	 */
	public static Map<String, Object> createCustomer(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String userLoginId = (String) context.get("userLoginId");

		try {
			syncCustomer(delegator, userLoginId, true);
		} catch (Throwable t) {
			// TODO 在这里向用户表插入同步失败标记位
			t.printStackTrace();
			updateSyncStatus(delegator, userLoginId, true, false);
			return ServiceUtil.returnFailure();
		}

		return ServiceUtil.returnSuccess();
	}

	/*
	 * 更新用户同步状态 0：成功，1：创建失败，2：更新失败
	 */
	private static void updateSyncStatus(Delegator delegator, String userLoginId, boolean isCreated,
			boolean isSuccess) {
		String syncStatus = null;
		if (isSuccess) {
			syncStatus = "0";
		} else if (isCreated) {
			syncStatus = "1";
		} else {
			syncStatus = "2";
		}
		try {
			GenericValue loginVal = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
			GenericValue partyVal = delegator.findByPrimaryKey("Party",
					UtilMisc.toMap("partyId", loginVal.getString("partyId")));
			partyVal.set("syncStatus", syncStatus);
			partyVal.store();
		} catch (GenericEntityException e) {

			e.printStackTrace();
			throw new RuntimeException("用户信息同步失败！");
		}
	}

	/**
	 * 会员更新
	 * 
	 * @return
	 */
	public static Map<String, Object> updateCustomer(DispatchContext dctx, Map<String, Object> context) {
		Delegator delegator = dctx.getDelegator();
		String userLoginId = (String) context.get("userLoginId");

		try {
			syncCustomer(delegator, userLoginId, false);
		} catch (Throwable t) {
			// TODO 在这里向用户表插入同步失败标记位
			t.printStackTrace();
			updateSyncStatus(delegator, userLoginId, false, false);
			return ServiceUtil.returnFailure();
		}
		return ServiceUtil.returnSuccess();
	}

	/* 用户同步 */
	private static void syncCustomer(Delegator delegator, String userLoginId, boolean isCreated)
			throws GenericEntityException {
		String baseUrl = getBaseUrl();
		String url = isCreated ? baseUrl + CREATE_URL : baseUrl + UPDATE_URL;

		DynamicViewEntity dynamicView = new DynamicViewEntity();
		dynamicView.addMemberEntity("UL", "UserLogin");
		dynamicView.addAlias("UL", "userLoginId");
		dynamicView.addAlias("UL", "openId");

		dynamicView.addMemberEntity("PA", "Party");
		dynamicView.addAlias("PA", "partyId");
		dynamicView.addAlias("PA", "lastModifiedByUserLogin");
		dynamicView.addAlias("PA", "createdByUserLogin");
		dynamicView.addAlias("PA", "lastModifiedDate"); // date

		dynamicView.addViewLink("UL", "PA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

		dynamicView.addMemberEntity("PE", "Person");
		dynamicView.addAlias("PE", "name");
		dynamicView.addAlias("PE", "gender");
		dynamicView.addAlias("PE", "email");

		dynamicView.addAlias("PE", "occupation");
		dynamicView.addAlias("PE", "headphoto");
		dynamicView.addAlias("PE", "mobile");

		dynamicView.addViewLink("UL", "PE", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

		EntityCondition cond = EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId);

		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("userLoginId");
		fieldsToSelect.add("openId");
		fieldsToSelect.add("partyId");
		fieldsToSelect.add("lastModifiedByUserLogin");
		fieldsToSelect.add("createdByUserLogin");
		fieldsToSelect.add("lastModifiedDate");
		fieldsToSelect.add("name");

		fieldsToSelect.add("gender");
		fieldsToSelect.add("email");
		fieldsToSelect.add("occupation");
		fieldsToSelect.add("headphoto");
		fieldsToSelect.add("mobile");
		EntityListIterator elIter = delegator.findListIteratorByCondition(dynamicView, cond, null, fieldsToSelect, null,
				null);
		GenericValue val = elIter.getCompleteList().get(0);
		Set<String> keys = partyMap.keySet();
		Map<String, Object> syncInfo = FastMap.newInstance();
		for (String key : keys) {
			Object value = val.get(partyMap.get(key));
			if (value == null) {
				continue;
			}
			if (value instanceof String) {
				syncInfo.put(key, (String) value);
			} else if (value instanceof Date) {
				syncInfo.put(key, DateFormatUtils.format((Date) value, DEFAULT_DATE_FORMAT));
			} else {
				throw new IllegalArgumentException("数据类型非法,type" + value.getClass() + ",数据:" + value);
			}
		}
		syncInfo.putAll(fixedPropertyMap);

		String rs = doPost(url, syncInfo);

		JSONObject resJson = JSONObject.fromObject(rs);
		if ("SUCCESS".equals(resJson.getString("status"))) {
			// 数据同步成功
			updateSyncStatus(delegator, userLoginId, isCreated, true);
		} else {
			updateSyncStatus(delegator, userLoginId, isCreated, false);
		}
	}

	/**
	 * 获取请求地址的基本URL
	 * 
	 * @return
	 */
	/*private final static String getBaseUrl() {
		return UtilProperties.getPropertyValue("security.properties", "crm.url");
	}*/

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

	// 用户权益查询
	private final static String SEARCH_CUST_RIGHTS = "/cust/searchCustRights";

	/**
	 * 会员权益查询(CRM)
	 * 
	 * 用户在微商城及官网个人中心查询资料，百邦内部人员在BOMS、ECM查询会员信息
	 * 
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> searchCustRights(DispatchContext dctx, Map<String, Object> context) {

		return queryFromCrm(context, SEARCH_CUST_RIGHTS);
	}

	// 用户优惠券查询接口
	private final static String SEARCH_COUPON_ACCOUNT = "/cust/searchCouponAccount";

	/**
	 * 会员优惠券查询（/cust/searchCouponAccount）
	 * 
	 * 用户在个人中心中查询优惠券及在订单结算页面调用此接口
	 * 
	 * @return
	 */
	public static Map<String, Object> searchCouponAccount(DispatchContext dctx, Map<String, Object> context) {
		return queryFromCrm(context, SEARCH_COUPON_ACCOUNT);
	}

	private final static String CONSUME_CARD = "/coupon/consumeCard";

	/**
	 * 优惠券消耗
	 * 
	 * 用户在移动端/web中使用券下订单时调用
	 * 
	 * @return
	 */
	public static Map<String, Object> consumeCard(DispatchContext dctx, Map<String, Object> context) {

		String res = doPost(getBaseUrl() + CONSUME_CARD, context);
		JSONObject jsonOb = JSONObject.fromObject(res);
		Map<String, Object> resMap = FastMap.newInstance();
		resMap.put("status", jsonOb.getString("status"));
		resMap.put("msg", jsonOb.getString("msg"));
		return resMap;
	}

	// 从CRM做数据分页查询
	private static Map<String, Object> queryFromCrm(Map<String, Object> context, String url) {
		url = getBaseUrl() + url;
		// Map<String, String> param = convert2String(context);
		String res = doPost(url, context);
		JSONObject resOb = JSONObject.fromObject(res);

		if (!"SUCCESS".equals(resOb.getString("status"))) {
			Debug.logError("响应报文：" + res + "\n请求接口：" + url, module);
			// 信息查询失败
			return ServiceUtil.returnError("CRM信息查询失败");
		}

		// 响应数据
		Map<String, Object> resMap = FastMap.newInstance();

		JSONObject data = resOb.getJSONObject("data");
		// 分页数据
		resMap.put("current", data.getInt("current"));
		resMap.put("pageSize", data.getInt("pageSize"));
		resMap.put("totalRecord", data.getInt("totalRecord"));
		resMap.put("totalPage", data.getInt("totalPage"));
		resMap.put("pre", data.getInt("pre"));
		resMap.put("next", data.getInt("next"));
		resMap.put("start", data.getInt("start"));

		// list ,将JSONArray转换为List
		resMap.put("list", convertArray2List(data.getJSONArray("list")));

		return resMap;
	}

	/**
	 * 将Json数组转换为list
	 * 
	 * @param arr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> convertArray2List(JSONArray arr) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(arr.size());
		for (int i = 0; i < arr.size(); i++) {
			list.add((Map<String, Object>) JSONObject.toBean(arr.getJSONObject(i), Map.class));
		}
		return list;
	}


}
