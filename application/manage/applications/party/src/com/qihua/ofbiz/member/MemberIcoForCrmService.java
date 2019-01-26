package com.qihua.ofbiz.member;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;

/**
 * ICO提供接口给CRM提供
 * 
 * @author spj
 * @date 2017-6-21
 * 
 */
public class MemberIcoForCrmService {

	/**
	 * CRM会员新增/修改接口
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	public static Map<String, Object> crmCustomer(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();

		String custCode = (String) context.get("custCode"); // 客户编号
		String rowId = (String) context.get("rowId"); // 会员编号
		// 返回map
		Map<String, Object> params = FastMap.newInstance();

		if (UtilValidate.isEmpty(custCode)) {
			params.put("status", "ERROR");
			params.put("code", "2");
			params.put("msg", "客户编号(custCode)不能为空");
			return params;
		}
		if (UtilValidate.isEmpty(rowId)) {
			params.put("status", "ERROR");
			params.put("code", "2");
			params.put("msg", "会员编号(rowId)不能为空");
			return params;
		}

		List<GenericValue> userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("custCode", custCode, "custId", rowId));
		if (UtilValidate.isNotEmpty(userLogins)) { // 会员信息修改
			params = updateCustomer(dctx, context);
		} else { // 会员信息增加
			params = createCustomer(dctx, context);
		}

		return params;
	}

	/**
	 * 修改CRM会员信息
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException
	 */
	private static Map<String, Object> updateCustomer(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();

		String custCode = (String) context.get("custCode"); // 客户编号
		String rowId = (String) context.get("rowId"); // 会员编号
		// 返回map
		Map<String, Object> params = FastMap.newInstance();

		if (UtilValidate.isEmpty(custCode)) {
			params.put("status", "ERROR");
			params.put("code", "2");
			params.put("msg", "客户编号(custCode)不能为空");
			return params;
		}
		if (UtilValidate.isEmpty(rowId)) {
			params.put("status", "ERROR");
			params.put("code", "2");
			params.put("msg", "会员编号(rowId)不能为空");
			return params;
		}

		String nickName = (String) context.get("nick"); // 昵称
		String realName = (String) context.get("realName"); // 真实名称
		String picUrl = (String) context.get("picUrl"); // 头像
		String mobile = (String) context.get("mobile"); // 手机号
		String gender = (String) context.get("gender"); // 性别
		String wechat = (String) context.get("wechat"); // 微信openid
		String storeId = (String) context.get("storeId"); // 门店（实体）
		Date birthday = (Date) context.get("birthday"); // 生日
		String mail = (String) context.get("mail"); // 邮箱
		String qq = (String) context.get("qq"); // QQ
		String blog = (String) context.get("blog"); // 微博
		String passportType = (String) context.get("passportType"); // 证件类型
		String passportNo = (String) context.get("passportNo"); // 证件号

		GenericValue userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("custCode", custCode, "custId", rowId)).get(0);
		String partyId = userLogin.getString("partyId"); // 获取会员id

		List<GenericValue> tobeStore = new ArrayList<GenericValue>();

		// 登录信息
		String userLoginId = null;
		String accountType = null;
		if (UtilValidate.isNotEmpty(mobile)) {
			userLoginId = mobile;
			accountType = "mobile";
		} else if (UtilValidate.isNotEmpty(wechat)) {
			userLoginId = wechat;
			accountType = "weixin";
		} else if (UtilValidate.isNotEmpty(mail)) {
			userLoginId = mail;
		}
		userLogin.set("userLoginId", userLoginId);
		if (UtilValidate.isNotEmpty(wechat)) {
			userLogin.set("openId", wechat);
		}
		userLogin.set("accountType", accountType);
		userLogin.set("custCode", custCode);
		userLogin.set("custId", rowId);
		tobeStore.add(userLogin);

		// 创建人员信息
		GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
		if (UtilValidate.isNotEmpty(nickName)) {
			person.set("nickname", nickName);
		}
		if (UtilValidate.isNotEmpty(realName)) {
			person.set("name", realName);
		}
		if (UtilValidate.isNotEmpty(birthday)) {
			person.set("birthDate", birthday);
		}
		if (UtilValidate.isNotEmpty(qq)) {
			person.set("qq", qq);
		}
		if (UtilValidate.isNotEmpty(blog)) {
			person.set("blog", blog);
		}
		if (UtilValidate.isNotEmpty(mail)) {
			person.set("email", mail);
		}
		if (UtilValidate.isNotEmpty(passportNo)) {
			person.set("idNumber", passportNo);
		}
		String sex = "S";
		if("0".equals(gender)) {
			sex = "F";
		} else if ("1".equals(gender)) {
			sex = "M";
		}
		person.set("gender", sex);
		if (UtilValidate.isNotEmpty(picUrl)) {
			person.set("headphoto", picUrl);
		}
		if (!userLoginId.equals(mobile)) {
			person.set("mobile", mobile);
		}
		tobeStore.add(person);
		String status = "", code = "", msg = "";
		try {
			delegator.storeAll(tobeStore);
			status = "SUCCESS";
			code = "1";
			msg = "操作成功！";
		} catch (GenericEntityException e) {
			status = "ERROR";
			code = "5";
			msg = "操作失败！";
			e.printStackTrace();
		}
		params.put("status", status);
		params.put("code", code);
		params.put("msg", msg);
		return params;
	}

	/**
	 * CRM会员新增
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private static Map<String, Object> createCustomer(DispatchContext dctx, Map<String, ? extends Object> context) {
		Delegator delegator = dctx.getDelegator();

		String custCode = (String) context.get("custCode"); // 客户编号
		String rowId = (String) context.get("rowId"); // 会员编号
		// 返回map
		Map<String, Object> params = FastMap.newInstance();

		if (UtilValidate.isEmpty(custCode)) {
			params.put("status", "ERROR");
			params.put("code", "2");
			params.put("msg", "客户编号(custCode)不能为空");
			return params;
		}
		if (UtilValidate.isEmpty(rowId)) {
			params.put("status", "ERROR");
			params.put("code", "2");
			params.put("msg", "会员编号(rowId)不能为空");
			return params;
		}

		String nickName = (String) context.get("nick"); // 昵称
		String realName = (String) context.get("realName"); // 真实名称
		String picUrl = (String) context.get("picUrl"); // 头像
		String mobile = (String) context.get("mobile"); // 手机号
		String gender = (String) context.get("gender"); // 性别
		String wechat = (String) context.get("wechat"); // 微信openid
		String storeId = (String) context.get("storeId"); // 门店（实体）
		Date birthday = (Date) context.get("birthday"); // 生日
		String mail = (String) context.get("mail"); // 邮箱
		String qq = (String) context.get("qq"); // QQ
		String blog = (String) context.get("blog"); // 微博
		String passportType = (String) context.get("passportType"); // 证件类型
		String passportNo = (String) context.get("passportNo"); // 证件号

		List<GenericValue> tobeStore = new ArrayList<GenericValue>();
		// 会员
		GenericValue party = delegator.makeValue("Party");
		String partyId = delegator.getNextSeqId("Party");
		party.set("partyId", partyId);
		party.set("partyTypeId", "PERSON");
		party.set("createdDate", UtilDateTime.nowTimestamp());
		party.set("merchants", "member");
		party.set("statusId", "PARTY_ENABLED");
		party.set("partyCategory", "MEMBER");
		tobeStore.add(party);

		// 登录信息
		GenericValue userLogin = delegator.makeValue("UserLogin");
		String userLoginId = null;
		String accountType = null;
		if (UtilValidate.isNotEmpty(mobile)) {
			userLoginId = mobile;
			accountType = "mobile";
		} else if (UtilValidate.isNotEmpty(wechat)) {
			userLoginId = wechat;
			accountType = "weixin";
		} else if (UtilValidate.isNotEmpty(mail)) {
			userLoginId = mail;
		}
		userLogin.set("userLoginId", userLoginId);
		userLogin.set("partyId", partyId);
//		userLogin.set("enabled", "N");
		if (UtilValidate.isNotEmpty(wechat)) {
			userLogin.set("openId", wechat);
		}
		userLogin.set("accountType", accountType);
		userLogin.set("custCode", custCode);
		userLogin.set("custId", rowId);
		tobeStore.add(userLogin);

		// 创建人员信息
		GenericValue person = delegator.makeValue("Person");
		person.set("partyId", partyId);
		if (UtilValidate.isNotEmpty(nickName)) {
			person.set("nickname", nickName);
		}
		if (UtilValidate.isNotEmpty(realName)) {
			person.set("name", realName);
		}
		if (UtilValidate.isNotEmpty(birthday)) {
			person.set("birthDate", birthday);
		}
		if (UtilValidate.isNotEmpty(qq)) {
			person.set("qq", qq);
		}
		if (UtilValidate.isNotEmpty(blog)) {
			person.set("blog", blog);
		}
		if (UtilValidate.isNotEmpty(mail)) {
			person.set("email", mail);
		}
		if (UtilValidate.isNotEmpty(passportNo)) {
			person.set("idNumber", passportNo);
		}
		person.set("gender", gender);
		if (UtilValidate.isNotEmpty(picUrl)) {
			person.set("headphoto", picUrl);
		}
		if (!userLoginId.equals(mobile)) {
			person.set("mobile", mobile);
		}
		tobeStore.add(person);
		String status = "", code = "", msg = "";
		try {
			delegator.storeAll(tobeStore);
			status = "SUCCESS";
			code = "1";
			msg = "操作成功！";
		} catch (GenericEntityException e) {
			status = "ERROR";
			code = "5";
			msg = "操作失败！";
			e.printStackTrace();
		}
		params.put("status", status);
		params.put("code", code);
		params.put("msg", msg);
		return params;
	}
	
	/**
	 * 优惠券新增和发放
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> createCouponAndGrant(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericEntityException {
		Delegator delegator = dctx.getDelegator();
		Map<String, Object> resultMap = FastMap.newInstance();
		String custCode = (String) context.get("custCode"); // 客户编号
		if (UtilValidate.isEmpty(custCode)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "客户编号不能为空！");
			return resultMap;
		}
		// 判断客户是否存在
		GenericValue userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("custCode", custCode)).get(0);
		if (UtilValidate.isEmpty(userLogin)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "该客户不存在！");
			return resultMap;
		}
		String couponId = (String) context.get("couponId"); // 优惠券编号
		if (UtilValidate.isEmpty(couponId)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券编号不能为空！");
			return resultMap;
		}
		String batch = (String) context.get("batch"); // 优惠券批次
		if (UtilValidate.isEmpty(batch)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券批次不能为空！");
			return resultMap;
		}
		String couponType = (String) context.get("couponType"); // 优惠券类型
		BigDecimal couponDiscount = (BigDecimal) context.get("couponDiscount"); // 折扣比例
		BigDecimal couponPar = (BigDecimal) context.get("couponPar"); // 金额
		if (UtilValidate.isEmpty(couponType)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券类型不能为空！");
			return resultMap;
		} else if (UtilValidate.isNotEmpty(couponType)) {
			if ("80".equals(couponType) && UtilValidate.isEmpty(couponDiscount)) {
				resultMap.put("status", "ERROR");
				resultMap.put("code", "2");
				resultMap.put("msg", "折扣券的折扣比例不能为空！");
				return resultMap;
			} else if ("81".equals(couponType) && UtilValidate.isEmpty(couponPar)) {
				resultMap.put("status", "ERROR");
				resultMap.put("code", "2");
				resultMap.put("msg", "现金券的金额不能为空！");
				return resultMap;
			}
		}
		String couponName = (String) context.get("couponName"); // 优惠券名称
		if (UtilValidate.isEmpty(couponName)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券名称不能为空！");
			return resultMap;
		}
		String userCaseType = (String) context.get("userCaseType"); // 使用条件类型    1、无条件 2满XX元 3满XX件
		String userCase = (String) context.get("userCase"); // 使用条件
		if (UtilValidate.isEmpty(userCaseType)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券编号不能为空！");
			return resultMap;
		} else if ("2".equals(userCase) || "3".equals(userCase)) {
			if (UtilValidate.isEmpty(userCase)) {
				resultMap.put("status", "ERROR");
				resultMap.put("code", "2");
				resultMap.put("msg", "优惠券使用条件不能为空！");
				return resultMap;
			}
		}
		String activeDate = (String) context.get("activeDate"); // 起始时间
		if (UtilValidate.isEmpty(activeDate)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券起始时间不能为空！");
			return resultMap;
		}
		String endDate = (String) context.get("endDate"); // 截止时间
		if (UtilValidate.isEmpty(endDate)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券截止时间不能为空！");
			return resultMap;
		}
		String grantDate = (String) context.get("grantDate"); // 发放时间
		if (UtilValidate.isEmpty(grantDate)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券发放时间不能为空！");
			return resultMap;
		}
		String couponStatus = (String) context.get("couponStatus"); // 优惠券状态
		if (UtilValidate.isEmpty(couponStatus)) {
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券状态不能为空！");
			return resultMap;
		}
		
		// 保存CRM优惠券
		GenericValue couponCrm = delegator.makeValue("CouponCrm");
		couponCrm.set("partyId", userLogin.getString("partyId")); // 客户编号
		couponCrm.set("couponId", couponId); // 优惠券编号
		couponCrm.set("batch", batch); // 优惠券批次
		couponCrm.set("couponType", couponType); // 优惠券类型
		couponCrm.set("couponName", couponName); // 优惠券名称
		if (UtilValidate.isNotEmpty(couponDiscount)) {
			couponCrm.set("couponDiscount", couponDiscount); // 折扣比例
		}
		if (UtilValidate.isNotEmpty(couponPar)) {
			couponCrm.set("couponPar", couponPar); // 金额
		}
		couponCrm.set("userCaseType", userCaseType); // 使用条件类型
		couponCrm.set("userCase", userCaseType); // 使用条件
		couponCrm.set("activeDate", UtilDateTime.toTimestamp(activeDate)); // 起始时间
		couponCrm.set("endDate", UtilDateTime.toTimestamp(endDate)); // 截止时间
		couponCrm.set("grantDate", UtilDateTime.toTimestamp(grantDate)); // 发放时间
		couponCrm.set("couponStatus", couponStatus); // 优惠券状态  0:可用 1:不可用（已核销）
		try { 
			// 保存CRM优惠券
			delegator.create(couponCrm);
			resultMap.put("status", "SUCCESS");
			resultMap.put("code", "1");
			resultMap.put("msg", "优惠券同步成功！");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("status", "ERROR");
			resultMap.put("code", "2");
			resultMap.put("msg", "优惠券失败，请稍后再试！");
		}
		return resultMap;
	}

}
