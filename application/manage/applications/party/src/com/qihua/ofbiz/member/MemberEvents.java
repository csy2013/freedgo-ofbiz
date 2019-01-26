package com.qihua.ofbiz.member;

import com.qihua.ofbiz.party.common.HttpUtil;
import com.qihua.ofbiz.weixin.util.CommonUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.base.crypto.HashCrypt;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.party.contact.ContactHelper;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceAuthException;
import org.ofbiz.service.ServiceValidationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by AlexYao 2016-4-25 14:46:38
 */
public class MemberEvents {

    public static final String module = MemberEvents.class.getName();


    private static char[] base64EncodeChars = new char[]{'A', 'B', 'C', 'D',
            'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
            'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
            'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '+', '/',};

    private static byte[] base64DecodeChars = new byte[]{-1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59,
            60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
            -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
            -1, -1};

    /**
     * 解密
     *
     * @param str
     * @return
     */
    public static byte[] decode(String str) {
        byte[] data = str.getBytes();
        int len = data.length;
        ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
        int i = 0;
        int b1, b2, b3, b4;

        while (i < len) {
            do {
                b1 = base64DecodeChars[data[i++]];
            } while (i < len && b1 == -1);
            if (b1 == -1) {
                break;
            }

            do {
                b2 = base64DecodeChars[data[i++]];
            } while (i < len && b2 == -1);
            if (b2 == -1) {
                break;
            }
            buf.write((int) ((b1 << 2) | ((b2 & 0x30) >>> 4)));

            do {
                b3 = data[i++];
                if (b3 == 61) {
                    return buf.toByteArray();
                }
                b3 = base64DecodeChars[b3];
            } while (i < len && b3 == -1);
            if (b3 == -1) {
                break;
            }
            buf.write((int) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));

            do {
                b4 = data[i++];
                if (b4 == 61) {
                    return buf.toByteArray();
                }
                b4 = base64DecodeChars[b4];
            } while (i < len && b4 == -1);
            if (b4 == -1) {
                break;
            }
            buf.write((int) (((b3 & 0x03) << 6) | b4));
        }
        return buf.toByteArray();
    }

    /**
     * 验证token
     *
     * @param request
     * @param response
     * @return
     */
    public static Map<String, String> checkAuthToken(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map result = FastMap.newInstance();
        String authorization = request.getHeader("Authorization");
        System.out.println("Authorization:+++++++++++++++++++++" + authorization);
        String debug = request.getParameter("debug");
        if (UtilValidate.isNotEmpty(debug)) {
            result.put("status", "success");//todo:暂时默认
            result.put("userLoginId", "15251909677");
        } else {
            if (UtilValidate.isEmpty(authorization)) {
                result.put("status", "error");
                result.put("error", "未授权");
                response.setStatus(401);
            } else {
                List<GenericValue> userLogin = null;
                try {
                    userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("token", authorization));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isEmpty(userLogin)) {
                    String strBackUrl = request.getRequestURL() + "?" + (request.getQueryString()); //参数
                    System.out.println("ErrorRequest:////////////////////////////////////////////////////////////" + strBackUrl);
                    System.out.println("Error Authorization:+++++++++++++++++++++" + authorization);
                    result.put("status", "error");
                    result.put("error", "该用户不存在");
                    response.setStatus(401);
                } else {
                    if (authorization.equals(userLogin.get(0).get("token"))) {
                        result.put("status", "success");
                        result.put("userLoginId", userLogin.get(0).get("userLoginId"));
                    } else {
                        result.put("status", "error");
                        result.put("error", "Token错误");
                        response.setStatus(401);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 验证手机验证码
     *
     * @param request
     * @param response
     * @return
     */
    public static String validateCheckCode(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String phoneId = request.getParameter("phoneId");
        String checkCode = request.getParameter("checkCode");
        String hasUser = request.getParameter("hasUser");
        if (UtilValidate.isEmpty(phoneId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            phoneId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(hasUser) && UtilValidate.isNotEmpty(jsonObject.get("hasUser"))) {
            hasUser = jsonObject.getString("hasUser");
        }
        if (UtilValidate.isEmpty(phoneId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        if (UtilValidate.isNotEmpty(hasUser) && "Y".equals(hasUser)) {
            GenericValue userLogin = null;
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phoneId));
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            if (UtilValidate.isEmpty(userLogin)) {
                request.setAttribute("error", "用户不存在");
                response.setStatus(403);
                return "error";
            }
        }
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(mobileCheckCode) || UtilValidate.isEmpty(mobileCheckCode.get("checkCode")) || !checkCode.equals(mobileCheckCode.get("checkCode"))) {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
            return "error";
        }
        try {
            delegator.removeValue(mobileCheckCode);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("success", "验证成功");
        return "success";

    }

    /**
     * 手机注册账号
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     * @throws GenericEntityException
     */
    public static String normalRegister(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException, GenericEntityException {
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String phoneId = request.getParameter("phoneId");
        String password = request.getParameter("password");
        String checkCode = request.getParameter("checkCode");
        if (UtilValidate.isEmpty(phoneId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            phoneId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(password) && UtilValidate.isNotEmpty(jsonObject.get("password"))) {
            password = jsonObject.getString("password");
        }
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(phoneId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(password)) {
            request.setAttribute("error", "密码不能为空");
            response.setStatus(403);
            return "error";
        }
        password = new String(decode(password));
        String userLoginId = request.getParameter("userLoginId");//第三方账号
        String openId = request.getParameter("openId");//仅在工公众平台才有该参数
        String nickname = request.getParameter("nickname");//第三方昵称
        String headimgurl = request.getParameter("headimgurl");//第三方头像
        String sex = request.getParameter("sex");//第三方性别
        String accountType = request.getParameter("accountType");//第三方账户类型
        if (UtilValidate.isEmpty(userLoginId) && UtilValidate.isNotEmpty(jsonObject.get("userLoginId"))) {
            userLoginId = jsonObject.getString("userLoginId");
        }
        if (UtilValidate.isEmpty(nickname) && UtilValidate.isNotEmpty(jsonObject.get("nickname"))) {
            nickname = jsonObject.getString("nickname");
        }
        if (UtilValidate.isEmpty(headimgurl) && UtilValidate.isNotEmpty(jsonObject.get("headimgurl"))) {
            headimgurl = jsonObject.getString("headimgurl");
        }
        if (UtilValidate.isEmpty(sex) && UtilValidate.isNotEmpty(jsonObject.get("sex"))) {
            sex = jsonObject.getString("sex");
        }
        if (UtilValidate.isEmpty(accountType) && UtilValidate.isNotEmpty(jsonObject.get("accountType"))) {
            accountType = jsonObject.getString("accountType");
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = null;
        if (UtilValidate.isNotEmpty(userLoginId)) {
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(userLogin) && UtilValidate.areEqual("Y", userLogin.get("enabled"))) {
                request.setAttribute("error", "该账号已注册");
                response.setStatus(403);
                return "error";
            }
            userLogin = null;
        }
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin) && UtilValidate.areEqual("Y", userLogin.get("enabled"))) {
            request.setAttribute("error", "该手机号已注册");
            response.setStatus(403);
            return "error";
        }
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(mobileCheckCode) || UtilValidate.isEmpty(mobileCheckCode.get("checkCode")) || !checkCode.equals(mobileCheckCode.get("checkCode"))) {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
            return "error";
        }
        try {
            delegator.removeValue(mobileCheckCode);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map result;
        if (UtilValidate.areEqual("qq", accountType)) {
            result = createCustomer(dispatcher, delegator, nickname, sex, headimgurl, userLoginId, null, userLoginId, password, "qq", phoneId);
        } else if (UtilValidate.areEqual("weibo", accountType)) {
            result = createCustomer(dispatcher, delegator, nickname, sex, headimgurl, null, userLoginId, userLoginId, password, "weibo", phoneId);
        } else if (UtilValidate.areEqual("weixin", accountType)) {
            result = createCustomer(dispatcher, delegator, nickname, sex, headimgurl, openId, null, userLoginId, password, "weixin", phoneId);
        } else {
            result = createCustomer(dispatcher, delegator, phoneId, null, null, null, null, phoneId, password, "mobile", phoneId);
        }
        if ("success".equals(result.get("status"))) {
            request.setAttribute("success", "注册成功");
            /*if (UtilValidate.areEqual("qq", accountType) || UtilValidate.areEqual("weibo", accountType) || UtilValidate.areEqual("weixin", accountType)) {
                request.setAttribute("USERNAME", userLoginId);
                request.setAttribute("PASSWORD", defaultPassword);
            } else {
                request.setAttribute("USERNAME", phoneId);
                request.setAttribute("PASSWORD", password);
            }*/
            return "success";
        }
        request.setAttribute("error", "注册失败");
        response.setStatus(403);
        return "error";

    }

    /**
     * 获取第三方账号信息
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getOtherLoginInfo(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        List<GenericValue> qqLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", userLogin.get("partyId"), "enabled", "Y", "accountType", "qq"));
        if (UtilValidate.isNotEmpty(qqLogin)) {
            request.setAttribute("qq", true);
        } else {
            request.setAttribute("qq", false);
        }
        List<GenericValue> weiboLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", userLogin.get("partyId"), "enabled", "Y", "accountType", "weibo"));
        if (UtilValidate.isNotEmpty(weiboLogin)) {
            request.setAttribute("weibo", true);
        } else {
            request.setAttribute("weibo", false);
        }
        List<GenericValue> weixinLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", userLogin.get("partyId"), "enabled", "Y", "accountType", "weixin"));
        if (UtilValidate.isNotEmpty(weixinLogin)) {
            request.setAttribute("weixin", true);
        } else {
            request.setAttribute("weixin", false);
        }
        return "error";

    }

    /**
     * 绑定第三方账号
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws GenericServiceException
     */
    public static String bindOtherLoginId(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, GenericServiceException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", tokenMap.get("userLoginId")));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String userLoginId = request.getParameter("userLoginId");
        String accountType = request.getParameter("accountType");
        if (UtilValidate.isEmpty(userLoginId) && UtilValidate.isNotEmpty(jsonObject.get("userLoginId"))) {
            userLoginId = jsonObject.getString("userLoginId");
        }
        if (UtilValidate.isEmpty(accountType) && UtilValidate.isNotEmpty(jsonObject.get("accountType"))) {
            accountType = jsonObject.getString("accountType");
        }
        if (UtilValidate.isEmpty(userLoginId)) {
            request.setAttribute("error", "第三方账号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(accountType)) {
            request.setAttribute("error", "账号类型不能为空");
            response.setStatus(403);
            return "error";
        }
        List<GenericValue> userLogin1 = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", userLoginId, "enabled", "Y", "accountType", accountType));
        if (UtilValidate.isNotEmpty(userLogin1)) {
            request.setAttribute("error", "该帐号已经绑定，请更换其他帐号！");
            response.setStatus(403);
            return "error";
        }
        // CRM同步MAP
        Map<String, Object> params = FastMap.newInstance();
        params.put("productBrandId", "1");
        params.put("channelId", "1");
        params.put("custId", userLogin.getString("custId"));
        List<GenericValue> tobeStore = FastList.newInstance();
        /** 登陆信息 */
        boolean useEncryption = "true".equals(UtilProperties.getPropertyValue("security.properties", "password.encrypt"));
        GenericValue newUserLogin = delegator.makeValue("UserLogin");
        newUserLogin.set("userLoginId", userLoginId);
        newUserLogin.set("partyId", userLogin.get("partyId"));
        newUserLogin.set("enabled", "Y");
        newUserLogin.set("accountType", accountType);
        newUserLogin.set("custId", userLogin.getString("custId"));
        newUserLogin.set("custCode", userLogin.getString("custCode"));
        if (UtilValidate.areEqual("qq", accountType)) {
            newUserLogin.set("openId", userLoginId);
            params.put("qq", userLoginId); // qq 同步给CRM
        } else if (UtilValidate.areEqual("weibo", accountType)) {
            newUserLogin.set("uid", userLoginId);
            params.put("blog", userLoginId); // 微博 同步给CRM
        } else if (UtilValidate.areEqual("weixin", accountType)) {
            params.put("wechat", userLoginId); // 微信 同步给CRM
        }
        String defaultPassword = UtilProperties.getPropertyValue("member.properties", "defaultPassword");
        newUserLogin.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), defaultPassword) : defaultPassword);
        tobeStore.add(newUserLogin);

        /** 用户访问系统的权限设置 */
        GenericValue userLoginSecurityGroup = delegator.makeValue("UserLoginSecurityGroup");
        userLoginSecurityGroup.set("userLoginId", userLoginId);
        userLoginSecurityGroup.set("groupId", "FULLADMIN");
        userLoginSecurityGroup.set("fromDate", UtilDateTime.nowTimestamp());
        tobeStore.add(userLoginSecurityGroup);
        delegator.storeAll(tobeStore);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        // 将接口加入业务中，调试放开
        dispatcher.runSync("updateCustInfoCrm02", params);
        request.setAttribute("success", "绑定成功");
        return "success";

    }

    /**
     * 手机注册成功后获取token
     *
     * @param request
     * @param response
     * @return
     */
    public static String registerSuccess(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String phoneId = request.getParameter("phoneId");
        String password = request.getParameter("password");
        String toUrl = request.getParameter("toUrl");
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appId", "");
        String appSecret = UtilProperties.getPropertyValue("weixin.properties", "appSecret", "");
        String code = request.getParameter("code");
        String URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecret + "&code=" + code + "&grant_type=authorization_code";//通过code换取网页授权access_token
        JSONObject jsonObject = CommonUtil.httpsRequest(URL, "GET", null);
        if (null != jsonObject) {
            String openId = jsonObject.getString("openid");
            GenericValue userLogin = null;
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phoneId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(userLogin)) {
                userLogin.set("openId", openId);
                try {
                    delegator.store(userLogin);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            if (toUrl.contains("?")) {
                toUrl = toUrl + "&openid=" + openId;
            } else {
                toUrl = toUrl + "?openid=" + openId;
            }
        }
        request.setAttribute("USERNAME", phoneId);
        request.setAttribute("PASSWORD", password);
        request.setAttribute("url", toUrl);
        return "success";
    }

    /**
     * 创建会员
     *
     * @param delegator
     * @param nickname
     * @param sex
     * @param headimgurl
     * @param openId
     * @param uid
     * @param userLoginId
     * @param password
     * @return
     */
    public static Map<String, Object> createCustomer(LocalDispatcher dispatcher, Delegator delegator, String nickname, String sex, String headimgurl, String openId, String uid, String userLoginId, String password, String accountType, String mobile) {
        Map<String, Object> result = FastMap.newInstance();
        List<GenericValue> tobeStore = new ArrayList<GenericValue>();

        GenericValue party = delegator.makeValue("Party");
        String newPartyId = delegator.getNextSeqId("Party");
        party.set("partyId", newPartyId);
        party.set("partyTypeId", "PERSON");
        party.set("createdDate", UtilDateTime.nowTimestamp());
        party.set("merchants", "member");
        party.set("statusId", "PARTY_ENABLED");
        party.set("partyCategory", "MEMBER");
        tobeStore.add(party);

        /** 登陆信息 */
        boolean useEncryption = "true".equals(UtilProperties.getPropertyValue("security.properties", "password.encrypt"));
        GenericValue userLogin = delegator.makeValue("UserLogin");
        userLogin.set("userLoginId", userLoginId);
        userLogin.set("partyId", newPartyId);
        userLogin.set("enabled", "Y");
        if (UtilValidate.isNotEmpty(openId)) {
            userLogin.set("openId", openId);
        }
        if (UtilValidate.isNotEmpty(uid)) {
            userLogin.set("uid", uid);
        }
        userLogin.set("accountType", accountType);
        if (UtilValidate.areEqual("qq", accountType) || UtilValidate.areEqual("weibo", accountType) || UtilValidate.areEqual("weixin", accountType)) {
            String defaultPassword = UtilProperties.getPropertyValue("member.properties", "defaultPassword");
            userLogin.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), defaultPassword) : defaultPassword);
        } else {
            userLogin.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), password) : password);
        }
        tobeStore.add(userLogin);

        /** 用户访问系统的权限设置 */
        GenericValue userLoginSecurityGroup = delegator.makeValue("UserLoginSecurityGroup");
        userLoginSecurityGroup.set("userLoginId", userLoginId);
        userLoginSecurityGroup.set("groupId", "FULLADMIN");
        userLoginSecurityGroup.set("fromDate", UtilDateTime.nowTimestamp());
        tobeStore.add(userLoginSecurityGroup);

        if (!UtilValidate.areEqual(userLoginId, mobile)) {//登陆账号与手机号不同，则创建手机号作为登陆账号
            GenericValue userLogin1 = delegator.makeValue("UserLogin");
            userLogin1.set("userLoginId", mobile);
            userLogin1.set("partyId", newPartyId);
            userLogin1.set("enabled", "Y");
            userLogin1.set("accountType", "mobile");
            userLogin1.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), password) : password);
            tobeStore.add(userLogin1);

            /** 用户访问系统的权限设置 */
            GenericValue userLoginSecurityGroup1 = delegator.makeValue("UserLoginSecurityGroup");
            userLoginSecurityGroup1.set("userLoginId", mobile);
            userLoginSecurityGroup1.set("groupId", "FULLADMIN");
            userLoginSecurityGroup1.set("fromDate", UtilDateTime.nowTimestamp());
            tobeStore.add(userLoginSecurityGroup1);
        }

        /** 创建人员信息 */
        GenericValue person = delegator.makeValue("Person");
        person.set("partyId", newPartyId);
        if (UtilValidate.isNotEmpty(nickname)) {
            person.set("name", nickname);
            person.set("nickname", nickname);
        }
        if ("1".equals(sex)) {
            person.set("gender", "M");
        } else if ("2".equals(sex)) {
            person.set("gender", "F");
        }
        if (UtilValidate.isNotEmpty(headimgurl)) {
            person.set("headphoto", headimgurl);
        }
        if (UtilValidate.isNotEmpty(mobile)) {
            person.set("mobile", mobile);
        }
        tobeStore.add(person);

        /* 会员data数据 */
        Map<String, Object> partySourceContext = FastMap.newInstance();
        partySourceContext.put("dataSourceId", "ECOMMERCE_SITE");
        partySourceContext.put("fromDate", UtilDateTime.nowTimestamp());
        partySourceContext.put("isCreate", "Y");
        partySourceContext.put("visitId", "");
        /* 创建用户角色类型 */
        Map<String, Object> partyRoleContext = FastMap.newInstance();
        GenericValue MemberpartyLevel = null;
        partyRoleContext.put("roleTypeId", "CUSTOMER");
        try {
            MemberpartyLevel = EntityUtil.getFirst(delegator.findByAnd("PartyLevelType", null, UtilMisc.toList("levelCode")));
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        /* 会员等级数据 */
        Map<String, Object> partyLevelContext = FastMap.newInstance();
        partyLevelContext.put("levelId", MemberpartyLevel.get("levelId"));
        partyLevelContext.put("levelName", MemberpartyLevel.get("levelName"));
        partyLevelContext.put("startDate", UtilDateTime.nowTimestamp());
        /* 会员余额 */
        Map<String, Object> partyAccountContext = FastMap.newInstance();
        partyAccountContext.put("amount", new BigDecimal(0));
        partyAccountContext.put("createDate", UtilDateTime.nowTimestamp());
        /* 会员积分 */
        Map<String, Object> partyScoreContext = FastMap.newInstance();
        partyScoreContext.put("scoreValue", 0L);
        /* 会员成长值 */
        Map<String, Object> partyAttributeContext = FastMap.newInstance();
        partyAttributeContext.put("attrName", "EXPERIENCE");
        partyAttributeContext.put("attrValue", "0");

        List<GenericValue> productStores = null;
        try {
            productStores = delegator.findByAnd("ProductStore", (Object[]) null);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(productStores)) {
            for (GenericValue productStore : productStores) {
                /* 会员店铺关联表 */
                Map<String, Object> productStoreRoleContext = FastMap.newInstance();
                productStoreRoleContext.put("fromDate", UtilDateTime.nowTimestamp());
                productStoreRoleContext.put("roleTypeId", "CUSTOMER");
                productStoreRoleContext.put("productStoreId", productStore.get("productStoreId"));
                productStoreRoleContext.put("partyId", newPartyId);
                GenericValue productStoreRole = delegator.makeValue("ProductStoreRole", productStoreRoleContext);
                tobeStore.add(productStoreRole);

                GenericValue shoppingList = delegator.makeValue("ShoppingList");
                String shoppingListId = delegator.getNextSeqId("ShoppingList");
                shoppingList.set("shoppingListId", shoppingListId);
                shoppingList.set("shoppingListTypeId", "SLT_SPEC_PURP");
                shoppingList.set("productStoreId", productStore.get("productStoreId"));
                shoppingList.set("partyId", newPartyId);
                shoppingList.set("listName", "auto-save");
                shoppingList.set("isPublic", "N");
                shoppingList.set("isActive", "Y");
                tobeStore.add(shoppingList);
            }
        }

        partySourceContext.put("partyId", newPartyId);
        partyLevelContext.put("partyId", newPartyId);
        partyAccountContext.put("partyId", newPartyId);
        partyScoreContext.put("partyId", newPartyId);
        partyAttributeContext.put("partyId", newPartyId);
        partyRoleContext.put("partyId", newPartyId);
        GenericValue partyLevelS = delegator.makeValue("PartyLevel", partyLevelContext);
        GenericValue partyAccount = delegator.makeValue("PartyAccount", partyAccountContext);
        GenericValue partyDataSource = delegator.makeValue("PartyDataSource", partySourceContext);
        GenericValue partyScore = delegator.makeValue("PartyScore", partyScoreContext);
        GenericValue partyAttribute = delegator.makeValue("PartyAttribute", partyAttributeContext);
        GenericValue partyRole = delegator.makeValue("PartyRole", partyRoleContext);
        tobeStore.add(partyDataSource);
        tobeStore.add(partyLevelS);
        tobeStore.add(partyAccount);
        tobeStore.add(partyScore);
        tobeStore.add(partyAttribute);
        tobeStore.add(partyRole);

        try {
            delegator.storeAll(tobeStore);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        // 用户注册同步到CRM begin spj
        try {
            Map<String, Object> params = FastMap.newInstance();
            if (UtilValidate.areEqual("qq", accountType)) {
                params.put("qq", userLoginId);
            } else if (UtilValidate.areEqual("weibo", accountType)) {
                params.put("blog", userLoginId);
            } else if (UtilValidate.areEqual("weixin", accountType)) {
                params.put("wechat", userLoginId);
            }
            params.put("productBrandId", "1");
            params.put("channelId", "1");
            params.put("mobile", mobile);
            params.put("accountMac", userLoginId);
            params.put("nick", nickname);
            params.put("picUrl", headimgurl);
            if ("1".equals(sex)) {
                params.put("gender", "1");
            } else if ("2".equals(sex)) {
                params.put("gender", "0");
            } else {
                params.put("gender", "2");
            }

            Map<String, Object> resultCRM = dispatcher.runSync("createPartyInfoCrm01", params);
            GenericValue partyNew = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", newPartyId));
            List<GenericValue> userLoginNews = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", newPartyId));
            // 判断是否更新成功
            String status = String.valueOf(resultCRM.get("expStatus"));
            if ("SUCCESS".equals(status)) {
                JSONObject jObject = HttpUtil.convertToJSONObject(String.valueOf(resultCRM.get("data")));
                String custCode = null;
                String custId = null;
                if (jObject.containsKey("custCode")) {
                    custCode = jObject.getString("custCode");// 客户编号
                }
                if (jObject.containsKey("headId")) {
                    custId = jObject.getString("headId");// 真实名称
                }

                if (null != custCode && !"".equals(custCode)) {
                    for (GenericValue ul : userLoginNews) {
                        ul.set("custCode", custCode);
                    }
                }
                if (null != custId && !"".equals(custId)) {
                    for (GenericValue ul : userLoginNews) {
                        ul.set("custId", custId);
                    }
                }
//            delegator.store(userLoginNew);
                if (UtilValidate.isNotEmpty(custId) || UtilValidate.isNotEmpty(custCode)) {
                    // 新增成功
                    int count = delegator.storeAll(userLoginNews);
                    if (count > 0) {
                        partyNew.set("syncStatus", "0");
                        delegator.store(partyNew);
                    }
                }
                // 添加用户注册行为
                dispatcher.runSync("addActivityInfoCrm21", UtilMisc.<String, Object>toMap("productBrandId", "1", "custId", custId, "activityType", "1", "createTime", UtilDateTime.nowTimestamp(), "sourceId", "1", "ipAddress", null, "urlAddress", null, "desp", null));
            } else {
                // 新增失败
                partyNew.set("syncStatus", "1");
                delegator.store(partyNew);
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        // 用户注册同步到CRM end spj

        result.put("status", "success");
        result.put("partyId", newPartyId);
        return result;
    }

    /**
     * 将emoji表情替换成*
     *
     * @param source
     * @return 过滤后的字符串
     */
    public static String filterEmoji(String source) {
        if (StringUtils.isNotBlank(source)) {
            return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "*");
        } else {
            return source;
        }
    }

    /**
     * 获取密码编码形式
     *
     * @return
     */
    public static String getHashType() {
        String hashType = UtilProperties.getPropertyValue("security.properties", "password.encrypt.hash.type");

        if (UtilValidate.isEmpty(hashType)) {
            Debug.logWarning("Password encrypt hash type is not specified in security.properties, use SHA", module);
            hashType = "SHA";
        }

        return hashType;
    }

    /**
     * 会员基础数据获取接口
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     */
    public static String getPartyBaseInfo(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            String partyId = userLogin.getString("partyId");
            try {
                //基础信息
                GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(person)) {
                    request.setAttribute("headphoto", person.get("headphoto"));
                    if (UtilValidate.isNotEmpty(person.get("nickname"))) {
                        request.setAttribute("nickname", person.get("nickname"));
                    } else {
                        request.setAttribute("nickname", userLoginId);
                    }
                }
                //会员等级
                // ============同步CRM会员信息 begin spj============
                LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
                String partyLevel = "";
                Map<String, Object> results = dispatcher.runSync("searchPageCustInfoCrm0301", UtilMisc.toMap("custCode", userLogin.getString("custCode"), "productBrandId", "1", "mobile", person.get("mobile")));
                if (UtilValidate.isNotEmpty(results) && UtilValidate.isNotEmpty(results.get("custInfo"))) {
                    Map<String, Object> custInfo = (Map<String, Object>) results.get("custInfo");
                    if (UtilValidate.isNotEmpty(custInfo) && UtilValidate.isNotEmpty(custInfo.get("list"))) {
                        List<Map<String, Object>> custInfos = (List<Map<String, Object>>) custInfo.get("list");
                        Map<String, Object> cInfo = custInfos.get(0);
                        // 等级
                        partyLevel = String.valueOf(cInfo.get("curGrade"));
                    }
                }
                request.setAttribute("partyLevel", partyLevel);
                // ============同步CRM会员信息 end spj============
                //==================会员升级信息 接口调用  begin  spj=======================
                String nowGrowthQuantity = "0";   //用户当前积分
                String needGrowthQuantity = "0";   //升级所需积分
                String nextLevelQuantity = "0";  // 下一等级的积分数
                String nextLevelName = ""; // 下一等级的名称
                Map<String, Object> resultMap = dispatcher.runSync("sycnLevelCrm42", UtilMisc.toMap("custId", userLogin.getString("custId")));
                if ("SUCCESS".equals(resultMap.get("expStatus"))) {
                    String data = String.valueOf(resultMap.get("data"));
                    JSONObject jsonObject = HttpUtil.convertToJSONObject(data);

                    if (jsonObject.containsKey("nowGrowthQuantity")) {
                        nowGrowthQuantity = jsonObject.getString("nowGrowthQuantity");// 用户当前积分
                    }
                    if (jsonObject.containsKey("needGrowthQuantity")) {
                        needGrowthQuantity = jsonObject.getString("needGrowthQuantity");// 升级所需积分
                    }
                    if (jsonObject.containsKey("nextLevelQuantity")) {
                        nextLevelQuantity = jsonObject.getString("nextLevelQuantity");// 用户当前积分
                    }
                    if (jsonObject.containsKey("nextLevelName")) {
                        nextLevelName = jsonObject.getString("nextLevelName");// 升级所需积分
                    }
                }
                request.setAttribute("nowGrowthValue", nowGrowthQuantity);//成长值
                request.setAttribute("needGrowthValue", needGrowthQuantity);//升级成长值
                request.setAttribute("nextLevelQuantity", nextLevelQuantity);//下一等级的积分数
                request.setAttribute("nextLevelName", nextLevelName);//下一等级的名称
                //==================会员升级信息 接口调用  end  spj=======================
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        } else {
            request.setAttribute("error", "登录用户不存在");
            response.setStatus(403);
            return "error";
        }
        return "success";
    }

    /**
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws GenericServiceException
     */
    @SuppressWarnings("unchecked")
    public static String getIntegralDesc(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, GenericServiceException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
//		String userLoginId = "13092301837";
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        if (UtilValidate.isNotEmpty(userLogin)) {
            String partyId = userLogin.getString("partyId");
            // 基础信息
            GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            // 获取用户的积分
            String score = "0";
            Map<String, Object> results = dispatcher.runSync("searchPageCustInfoCrm0301", UtilMisc.toMap("custCode", userLogin.getString("custCode"), "productBrandId", "1", "mobile", person.get("mobile")));
            if (UtilValidate.isNotEmpty(results) && UtilValidate.isNotEmpty(results.get("custInfo"))) {
                Map<String, Object> custInfo = (Map<String, Object>) results.get("custInfo");
                if (UtilValidate.isNotEmpty(custInfo) && UtilValidate.isNotEmpty(custInfo.get("list"))) {
                    List<Map<String, Object>> custInfos = (List<Map<String, Object>>) custInfo.get("list");
                    Map<String, Object> cInfo = custInfos.get(0);
                    // 保存用户的权益，账户，积分，优惠券，等级
                    score = cInfo.get("buyerQuantity").toString();
                }
            }
            request.setAttribute("score", score);
            // 获取积分兑换规则
            String amt = "0", integral = "0";
            Map<String, Object> params = FastMap.newInstance();
            results = dispatcher.runSync("syncIntegralDescCrm43", params);
            if (UtilValidate.isNotEmpty(results) && UtilValidate.isNotEmpty(results.get("data"))) {
                String data = (String) results.get("data");
                JSONObject jsonObject = HttpUtil.convertToJSONObject(data);
                if (jsonObject.containsKey("amt")) {
                    amt = jsonObject.getString("amt");
                }
                if (jsonObject.containsKey("integral")) {
                    integral = jsonObject.getString("integral");
                }
            }
            request.setAttribute("amt", amt);
            request.setAttribute("integral", integral);
        }
        return "success";

    }

    /**
     * 获取个人资料
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     * @throws ServiceValidationException
     * @throws ServiceAuthException
     */
    public static String getPersonalInfo(HttpServletRequest request, HttpServletResponse response) throws ServiceAuthException, ServiceValidationException, GenericServiceException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            String partyId = userLogin.getString("partyId");
            try {
                //基础信息
                GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(person)) {
                    request.setAttribute("headphoto", person.get("headphoto"));
                    if (UtilValidate.isNotEmpty(person.get("nickname"))) {
                        request.setAttribute("nickname", person.get("nickname"));
                    } else {
                        request.setAttribute("nickname", userLoginId);
                    }
                }
                //会员等级
                GenericValue partyLevel = delegator.findByPrimaryKey("PartyLevel", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(partyLevel)) {
                    request.setAttribute("partyLevel", partyLevel.get("levelName"));
                }
                //会员消息
                List<EntityCondition> conditions = FastList.newInstance();
                conditions.add(EntityCondition.makeCondition("partyId", partyId));
                conditions.add(EntityCondition.makeCondition("isView", "N"));
                Long unViewMessageSize = delegator.findCountByCondition("PartyMessage", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null);
                request.setAttribute("messageSize", unViewMessageSize);
                //会员账户
                GenericValue partyAccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(partyAccount)) {
                    request.setAttribute("balance", partyAccount.get("amount"));
                }
                //会员积分
                GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(partyScore)) {
                    request.setAttribute("score", partyScore.get("scoreValue"));
                }
                //会员优惠券
                Integer index = 0;
                List<GenericValue> productPromoCodePartys = delegator.findByAnd("ProductPromoCodeParty", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(productPromoCodePartys)) {
                    for (GenericValue productPromoCodeParty : productPromoCodePartys) {
                        List<GenericValue> orderProductPromoCode = delegator.findByAnd("OrderProductPromoCode", UtilMisc.toMap("productPromoCodeId", productPromoCodeParty.get("productPromoCodeId")));
                        if (UtilValidate.isEmpty(orderProductPromoCode)) {
                            index++;
                        }
                    }
                }
                request.setAttribute("promoSize", index);
                //会员升级信息
                GenericValue partyLevelInfo = delegator.findByPrimaryKey("PartyLevelInfo", UtilMisc.toMap("partyId", partyId));
                if (UtilValidate.isNotEmpty(partyLevelInfo)) {
                    request.setAttribute("nowGrowthValue", partyLevelInfo.get("nowGrowthValue"));//成长值
                }
                // ============同步CRM会员信息 begin spj============
                LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
                Map<String, Object> results = dispatcher.runSync("searchPageCustInfoCrm0301", UtilMisc.toMap("custCode", userLogin.getString("custCode"), "productBrandId", "1", "mobile", person.get("mobile")));
                if (UtilValidate.isNotEmpty(results) && UtilValidate.isNotEmpty(results.get("custInfo"))) {
                    Map<String, Object> custInfo = (Map<String, Object>) results.get("custInfo");
                    if (UtilValidate.isNotEmpty(custInfo) && UtilValidate.isNotEmpty(custInfo.get("list"))) {
                        List<Map<String, Object>> custInfos = (List<Map<String, Object>>) custInfo.get("list");
                        Map<String, Object> cInfo = custInfos.get(0);
                        // 保存用户的权益，账户，积分，优惠券，等级
                        request.setAttribute("partyLevel", cInfo.get("curGrade"));
//                        request.setAttribute("balance", cInfo.get("curMoney"));
                        request.setAttribute("score", cInfo.get("buyerQuantity"));
                        request.setAttribute("promoSize", cInfo.get("couponNum"));
                    }
                }
                // ============同步CRM会员信息 end spj============
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    /**
     * 获取用户信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getUserInfo(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            String partyId = userLogin.getString("partyId");
            GenericValue person = null;
            try {
                person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(person)) {
                request.setAttribute("headphoto", person.get("headphoto"));
                request.setAttribute("nickname", person.get("nickname"));
                request.setAttribute("name", person.get("name"));
                if (UtilValidate.areEqual("M", person.get("gender"))) {
                    request.setAttribute("gender", "男");
                } else if (UtilValidate.areEqual("F", person.get("gender"))) {
                    request.setAttribute("gender", "女");
                }
                request.setAttribute("idNumber", person.get("idNumber"));
                request.setAttribute("mobile", person.get("mobile"));
                request.setAttribute("email", person.get("email"));
                request.setAttribute("drivingLicence", person.get("drivingLicence"));
            }
        }
        return "success";
    }

    /**
     * 修改用户信息
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     */
    public static String updateUserInfo(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            GenericValue person = null;
            try {
                person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(person)) {
                JSONObject jsonObject = new JSONObject();
                String input = null;
                try {
                    input = RequestUtil.convertStreamToString(request.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(input)) {
                    jsonObject = JSONObject.fromObject(input);
                }

                Map<String, Object> paramsCRM = FastMap.newInstance(); // 保存CRM修改参数

                String nickname = request.getParameter("nickname");
                if (UtilValidate.isEmpty(nickname) && UtilValidate.isNotEmpty(jsonObject.get("nickname"))) {
                    nickname = jsonObject.getString("nickname");
                }
                if (UtilValidate.isNotEmpty(nickname)) {
                    person.set("nickname", nickname);
                    paramsCRM.put("nick", nickname);
                }
                String name = request.getParameter("name");
                if (UtilValidate.isEmpty(name) && UtilValidate.isNotEmpty(jsonObject.get("name"))) {
                    name = jsonObject.getString("name");
                }
                if (UtilValidate.isNotEmpty(name)) {
                    person.set("name", name);
                    paramsCRM.put("realName", name);
                }
                String gender = request.getParameter("gender");
                if (UtilValidate.isEmpty(gender) && UtilValidate.isNotEmpty(jsonObject.get("gender"))) {
                    gender = jsonObject.getString("gender");
                }
                if (UtilValidate.isNotEmpty(gender)) {
                    String sex = "0";
                    if ("S".equals(gender)) {
                        person.set("gender", null);
                        sex = "2";
                    } else {
                        person.set("gender", gender);
                        if ("M".equals(gender)) {
                            sex = "1";
                        } else {
                            sex = "0";
                        }
                    }
                    paramsCRM.put("gender", sex);
                }
                String idNumber = request.getParameter("idNumber");
                if (UtilValidate.isEmpty(idNumber) && UtilValidate.isNotEmpty(jsonObject.get("idNumber"))) {
                    idNumber = jsonObject.getString("idNumber");
                }
                if (UtilValidate.isNotEmpty(idNumber)) {
                    person.set("idNumber", idNumber);
                    paramsCRM.put("passportNo", idNumber);
                }
                String drivingLicence = request.getParameter("drivingLicence");
                if (UtilValidate.isEmpty(drivingLicence) && UtilValidate.isNotEmpty(jsonObject.get("drivingLicence"))) {
                    drivingLicence = jsonObject.getString("drivingLicence");
                }
                if (UtilValidate.isNotEmpty(drivingLicence)) {
                    person.set("drivingLicence", drivingLicence);
                }
                try {
                    int result = delegator.store(person);
                    if (result > 0) {
                        // ===============ICO修改成功之后，同步给CRM begin spj==================
                        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

                        paramsCRM.put("productBrandId", "1");
                        paramsCRM.put("channelId", "1");
                        // 判断用户的注册类型，手机、qq、blog
                        String accountType = userLogin.getString("accountType");
                        if ("mobile".equals(accountType)) {
                            paramsCRM.put("mobile", userLogin.getString("userLoginId"));
                        } else if ("qq".equals(accountType)) {
                            paramsCRM.put("qq", userLogin.getString("userLoginId"));
                        } else if ("blog".equals(accountType)) {
                            paramsCRM.put("blog", userLogin.getString("userLoginId"));
                        }
                        paramsCRM.put("custId", userLogin.getString("custId"));
                        paramsCRM.put("drivingLicence", drivingLicence);
                        // 将接口加入业务中，调试放开
                        dispatcher.runSync("updateCustInfoCrm02", paramsCRM);
                        // 用户完善资料行为
                        dispatcher.runSync("addActivityInfoCrm21", UtilMisc.<String, Object>toMap("productBrandId", "1", "custId", userLogin.getString("custId"), "activityType", "6", "createTime", UtilDateTime.nowTimestamp(), "sourceId", "1"));
                        // ===============ICO修改成功之后，同步给CRM end spj==================
                        request.setAttribute("success", "修改成功");
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        }
        return "success";
    }

    /**
     * 账号列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String accountList(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");

        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        String partyId = userLogin.getString("partyId");
        List<GenericValue> userLogins = null;
        GenericValue person = null;
        try {
            userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId, "enabled", "Y"));
            person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("partyId", partyId);
        String nickname = "";
        if (UtilValidate.isNotEmpty(person) && UtilValidate.isNotEmpty(person.get("nickname"))) {
            nickname = person.getString("nickname");
        }
        request.setAttribute("nickname", nickname);
        if (UtilValidate.isNotEmpty(userLogins)) {
            List list = FastList.newInstance();
            for (GenericValue login : userLogins) {
                Map map = new HashMap();
                map.put("userLoginId", login.get("userLoginId"));
                map.put("accountType", login.get("accountType"));
                list.add(map);
            }
            request.setAttribute("accountList", list);
            return "success";
        }
        return "error";

    }

    /**
     * 绑定手机号
     *
     * @param request
     * @param response
     * @return
     */
    public static String bindMobile(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String phoneId = request.getParameter("phoneId");
        if (UtilValidate.isEmpty(phoneId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            phoneId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(phoneId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        String password = request.getParameter("password");
        if (UtilValidate.isEmpty(password) && UtilValidate.isNotEmpty(jsonObject.get("password"))) {
            password = jsonObject.getString("password");
        }
        if (UtilValidate.isEmpty(password)) {
            request.setAttribute("error", "密码不能为空");
            response.setStatus(403);
            return "error";
        }
        String checkCode = request.getParameter("checkCode");
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(mobileCheckCode) && checkCode.equals(mobileCheckCode.get("checkCode"))) {
            GenericValue userLogins = null;
            try {
                userLogins = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phoneId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isEmpty(userLogins)) {
                GenericValue newUserLogin = delegator.makeValue("UserLogin");
                newUserLogin.set("userLoginId", phoneId);
                newUserLogin.set("partyId", userLogin.get("partyId"));
                newUserLogin.set("enabled", "Y");
                boolean useEncryption = "true".equals(UtilProperties.getPropertyValue("security.properties", "password.encrypt"));
                newUserLogin.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), password) : password);
                newUserLogin.set("accountType", "mobile");
                newUserLogin.set("openId", userLogin.get("openId"));
                try {
                    delegator.create(newUserLogin);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                GenericValue userLoginSecurityGroup = delegator.makeValue("UserLoginSecurityGroup");
                userLoginSecurityGroup.set("userLoginId", phoneId);
                userLoginSecurityGroup.set("groupId", "FULLADMIN");
                userLoginSecurityGroup.set("fromDate", UtilDateTime.nowTimestamp());
                try {
                    delegator.create(userLoginSecurityGroup);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            } else {
                userLogins.set("partyId", userLogin.get("partyId"));
                userLogins.set("enabled", "Y");
                boolean useEncryption = "true".equals(UtilProperties.getPropertyValue("security.properties", "password.encrypt"));
                userLogins.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), password) : password);
                userLogins.set("openId", userLogin.get("openId"));
                try {
                    delegator.store(userLogins);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            GenericValue person = null;
            try {
                person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("party", userLogin.get("partyId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            person.set("mobile", phoneId);
            try {
                delegator.store(person);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            request.setAttribute("success", "绑定成功");
            return "success";
        } else {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
        }
        return "error";

    }

    /**
     * 解绑手机号
     *
     * @param request
     * @param response
     * @return
     */
    public static String unbindMobile(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String phoneId = request.getParameter("phoneId");
        if (UtilValidate.isEmpty(phoneId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            phoneId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(phoneId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        String checkCode = request.getParameter("checkCode");
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(mobileCheckCode) || UtilValidate.isEmpty(mobileCheckCode.get("checkCode")) || !checkCode.equals(mobileCheckCode.get("checkCode"))) {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
            return "error";
        }
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (UtilValidate.isEmpty(userLogin)) {
            request.setAttribute("error", "该用户不存在");
            response.setStatus(403);
            return "error";
        } else {
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phoneId));
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            if (UtilValidate.isEmpty(userLogin)) {
                request.setAttribute("error", "该账号不存在");
                response.setStatus(403);
                return "error";
            } else {
                userLogin.set("enabled", "N");
                try {
                    delegator.store(userLogin);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                request.setAttribute("success", "解绑成功");
                if (!userLoginId.equals(phoneId)) {
                    request.setAttribute("userLoginId", userLoginId);
                }
//                response.setStatus(204);
                return "success";
            }
        }
    }

    /**
     * 检验旧手机号
     *
     * @param request
     * @param response
     * @return
     */
    public static String checkOldMobile(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String phoneId = request.getParameter("phoneId");
        if (UtilValidate.isEmpty(phoneId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            phoneId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(phoneId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        String checkCode = request.getParameter("checkCode");
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(mobileCheckCode) && checkCode.equals(mobileCheckCode.get("checkCode"))) {
            GenericValue userLogin = null;
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            String partyId = userLogin.getString("partyId");
            List<GenericValue> userLogins = null;
            try {
                userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", phoneId, "partyId", partyId));
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            if (UtilValidate.isEmpty(userLogins)) {
                request.setAttribute("error", "手机号错误，验证失败");
                response.setStatus(403);
                return "error";
            } else {
                request.setAttribute("success", "验证成功");
                return "success";
            }
        } else {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
            return "error";
        }
    }

    /**
     * 绑定新手机号
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     * @throws GenericEntityException
     */
    public static String bindNewMobile(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException, GenericEntityException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String oldPhoneId = request.getParameter("oldPhoneId");
        if (UtilValidate.isEmpty(oldPhoneId) && UtilValidate.isNotEmpty(jsonObject.get("oldPhoneId"))) {
            oldPhoneId = jsonObject.getString("oldPhoneId");
        }
        if (UtilValidate.isEmpty(oldPhoneId)) {
            request.setAttribute("error", "旧手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        String newPhoneId = request.getParameter("newPhoneId");
        if (UtilValidate.isEmpty(newPhoneId) && UtilValidate.isNotEmpty(jsonObject.get("newPhoneId"))) {
            newPhoneId = jsonObject.getString("newPhoneId");
        }
        if (UtilValidate.isEmpty(newPhoneId)) {
            request.setAttribute("error", "新手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        String checkCode = request.getParameter("checkCode");
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", newPhoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(mobileCheckCode) && checkCode.equals(mobileCheckCode.get("checkCode"))) {
            List<GenericValue> oldUserLogin = null;
            GenericValue userLogins = null;
            GenericValue person = null;
            try {
                oldUserLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", oldPhoneId, "partyId", userLogin.get("partyId"), "accountType", "mobile"));
                userLogins = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", newPhoneId));
                person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isEmpty(oldUserLogin)) {
                request.setAttribute("error", "旧账号不存在");
                response.setStatus(403);
                return "error";
            } else {
                oldUserLogin.get(0).set("enabled", "N");
                try {
                    delegator.storeAll(oldUserLogin);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            if (UtilValidate.isEmpty(userLogins)) {
                GenericValue newUserLogin = delegator.makeValue("UserLogin");
                newUserLogin.set("userLoginId", newPhoneId);
                newUserLogin.set("partyId", oldUserLogin.get(0).get("partyId"));
                newUserLogin.set("enabled", "Y");
                newUserLogin.set("currentPassword", oldUserLogin.get(0).get("currentPassword"));
                newUserLogin.set("accountType", "mobile");
                try {
                    delegator.create(newUserLogin);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                GenericValue userLoginSecurityGroup = delegator.makeValue("UserLoginSecurityGroup");
                userLoginSecurityGroup.set("userLoginId", newPhoneId);
                userLoginSecurityGroup.set("groupId", "FULLADMIN");
                userLoginSecurityGroup.set("fromDate", UtilDateTime.nowTimestamp());
                try {
                    delegator.create(userLoginSecurityGroup);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            } else {
                userLogins.set("partyId", oldUserLogin.get(0).get("partyId"));
                userLogins.set("enabled", "Y");
                userLogins.set("currentPassword", oldUserLogin.get(0).get("currentPassword"));
                try {
                    delegator.store(userLogins);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }

            person.set("mobile", newPhoneId);
            try {
                delegator.store(person);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            try {
                delegator.removeValue(mobileCheckCode);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }

            // ===============ICO修改成功之后，同步给CRM begin spj==================
            Map<String, Object> paramsCRM = FastMap.newInstance();

            paramsCRM.put("productBrandId", "1");
            paramsCRM.put("channelId", "1");

            // 判断改账户在CRM是否存在
            String custId = String.valueOf(oldUserLogin.get(0).get("custId"));
            String custCode = String.valueOf(oldUserLogin.get(0).get("custCode"));
            if (UtilValidate.isNotEmpty(custId) && UtilValidate.isNotEmpty(custCode) && !"null".equals(custId) && !"null".equals(custCode)) {
                // 判断用户的注册类型，手机、qq、blog
                paramsCRM.put("mobile", newPhoneId);
                paramsCRM.put("custId", userLogin.getString("custId"));
                // 将接口加入业务中，调试放开
                dispatcher.runSync("updateCustInfoCrm02", paramsCRM);
            } else {
                // ===============ICO保存成功之后，同步给CRM begin spj==================
                paramsCRM.put("mobile", newPhoneId);
                paramsCRM.put("accountMac", userLoginId);
                paramsCRM.put("nick", person.get("nickname"));
                String sex = String.valueOf(person.get("gender"));
                if ("M".equals(sex)) {
                    paramsCRM.put("gender", "1");
                } else if ("F".equals(sex)) {
                    paramsCRM.put("gender", "0");
                } else {
                    paramsCRM.put("gender", "2");
                }
                paramsCRM.put("custType", "mobile");
                Map<String, Object> resultCRM = dispatcher.runSync("createPartyInfoCrm01", paramsCRM);

                GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", person.get("partyId")));
                GenericValue userLoginNew = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", newPhoneId));
                // 判断是否更新成功
                String status = String.valueOf(resultCRM.get("expStatus"));
                if ("SUCCESS".equals(status)) {
                    JSONObject jObject = HttpUtil.convertToJSONObject(String.valueOf(resultCRM.get("data")));
                    String custCode1 = null;
                    String custId1 = null;
                    if (jObject.containsKey("custCode")) {
                        custCode1 = jObject.getString("custCode");// 客户编号
                    }
                    if (jObject.containsKey("headId")) {
                        custId1 = jObject.getString("headId");// 真实名称
                    }

                    if (null != custCode1 && !"".equals(custCode1)) {
                        userLoginNew.set("custCode", custCode1);
                    }
                    if (null != custId1 && !"".equals(custId1)) {
                        userLoginNew.set("custId", custId1);
                    }
                    if (UtilValidate.isNotEmpty(custId1) || UtilValidate.isNotEmpty(custCode1)) {
                        // 新增成功
                        int count = delegator.store(userLoginNew);
                        if (count > 0) {
                            party.set("syncStatus", "0");
                            delegator.store(party);
                        }
                    }
                } else {
                    // 新增失败
                    party.set("syncStatus", "1");
                    delegator.store(party);
                }
            }
            // ===============ICO修改成功之后，同步给CRM end spj==================

            request.setAttribute("success", "绑定成功");
            return "success";
        } else {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
        }
        return "error";

    }


    /**
     * 密码找回 重置/绑定手机后设置密码
     *
     * @param request
     * @param response
     * @return
     */
    public static String resetPassword(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String userLoginId = request.getParameter("phoneId");
        if (UtilValidate.isEmpty(userLoginId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            userLoginId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(userLoginId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        String password = request.getParameter("password");
        if (UtilValidate.isEmpty(password) && UtilValidate.isNotEmpty(jsonObject.get("password"))) {
            password = jsonObject.getString("password");
        }
        if (UtilValidate.isEmpty(password)) {
            request.setAttribute("error", "密码不能为空");
            response.setStatus(403);
            return "error";
        }
        password = new String(decode(password));
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        if (UtilValidate.isEmpty(userLogin)) {
            request.setAttribute("error", "用户不存在");
            response.setStatus(403);
            return "error";
        } else {
            boolean useEncryption = "true".equals(UtilProperties.getPropertyValue("security.properties", "password.encrypt"));
            userLogin.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), password) : password);
            try {
                delegator.store(userLogin);
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
            }
            request.setAttribute("success", "设置成功");
            return "success";
        }
    }


    /**
     * 获取上海地区列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String findAreaTree(HttpServletRequest request, HttpServletResponse response) {
        List tree = FastList.newInstance();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> oriMap = FastMap.newInstance();
        oriMap.put("id", "CN-31");
        oriMap.put("name", "上海市");
        oriMap.put("pId", "CHN");
        tree.add(oriMap);
        List<GenericValue> geoAssocs = null;
        try {
            geoAssocs = delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoId", "CN-31", "geoAssocTypeId", "REGIONS"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(geoAssocs)) {
            for (GenericValue geoAssoc : geoAssocs) {
                String geoId = geoAssoc.getString("geoIdTo");
                GenericValue geo = null;
                try {
                    geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", geoId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                String name = geo.getString("geoName");
                String parentGeoId = geoAssoc.getString("geoIdTo");
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", geoId);
                map.put("name", name);
                map.put("pId", "CN-31");
                tree.add(map);
                areaRecursive(tree, delegator, parentGeoId);
            }
        }
        Map<String, String> oriMap1 = FastMap.newInstance();
        oriMap1.put("id", "CN-33");
        oriMap1.put("name", "浙江");
        oriMap1.put("pId", "CHN");
        tree.add(oriMap1);
        List<GenericValue> geoAssocs1 = null;
        try {
            geoAssocs1 = delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoId", "CN-33", "geoAssocTypeId", "REGIONS"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(geoAssocs1)) {
            for (GenericValue geoAssoc : geoAssocs1) {
                String geoId = geoAssoc.getString("geoIdTo");
                GenericValue geo = null;
                try {
                    geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", geoId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                String name = geo.getString("geoName");
                String parentGeoId = geoAssoc.getString("geoIdTo");
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", geoId);
                map.put("name", name);
                map.put("pId", "CN-33");
                tree.add(map);
                areaRecursive(tree, delegator, parentGeoId);
            }
        }
        request.setAttribute("geoList", tree);
        return "sucess";
    }

    private static void areaRecursive(List<Map<String, String>> list, Delegator delegator, String pGeoId) {
        List<GenericValue> geoAssocs = null;
        try {
            geoAssocs = delegator.findByAnd("GeoAssoc", UtilMisc.toMap("geoId", pGeoId, "geoAssocTypeId", "REGIONS"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(geoAssocs)) {
            for (GenericValue geoAssoc : geoAssocs) {
                String geoId = geoAssoc.getString("geoIdTo");
                GenericValue geo = null;
                try {
                    geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", geoId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
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
    }


    /**
     * 获取收货地址
     *
     * @param request
     * @param response
     * @return
     */
    public static String getPartyPostalAddresses(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map<String, Object>> addressList = FastList.newInstance();

        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        String partyId = userLogin.getString("partyId");
        //默认地址
        GenericValue profiledefs;
        try {
            profiledefs = EntityUtil.getFirst(delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId)));
            if (UtilValidate.isNotEmpty(profiledefs)) {
                request.setAttribute("defaultId", profiledefs.get("defaultShipAddr"));
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        try {
            GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            List<GenericValue> shippingContactMechList = (List<GenericValue>) ContactHelper.getContactMechByType(party, "POSTAL_ADDRESS", false);
            if (UtilValidate.isNotEmpty(shippingContactMechList)) {
                for (int i = 0; i < shippingContactMechList.size(); i++) {
                    GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", shippingContactMechList.get(i).get("contactMechId")));
                    Map<String, Object> map = FastMap.newInstance();
                    map.put("contactMechId", postalAddress.get("contactMechId"));
                    map.put("toName", postalAddress.get("toName"));
                    map.put("mobilePhone", postalAddress.get("mobilePhone"));
                    Map<String, Object> stateProvincemap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(postalAddress.getString("stateProvinceGeoId"))) {
                        GenericValue stateProvinceGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("stateProvinceGeoId")));
                        stateProvincemap.put("id", postalAddress.getString("stateProvinceGeoId"));
                        stateProvincemap.put("title", stateProvinceGeoId.getString("geoName"));
                        map.put("stateProvinceGeoId", stateProvincemap);
                    }
                    Map<String, Object> citymap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(postalAddress.getString("city"))) {
                        GenericValue cityGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("city")));
                        citymap.put("id", postalAddress.getString("city"));
                        citymap.put("title", cityGeoId.getString("geoName"));
                        map.put("city", citymap);
                    }
                    Map<String, Object> countymap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(postalAddress.getString("countyGeoId"))) {
                        GenericValue countyGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("countyGeoId")));
                        countymap.put("id", postalAddress.getString("countyGeoId"));
                        countymap.put("title", countyGeoId.getString("geoName"));
                        map.put("countyGeoId", countymap);
                    }
                    map.put("address", postalAddress.get("address1"));
                    addressList.add(map);
                }
            }
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        request.setAttribute("addressList", addressList);
        return "success";
    }

    /**
     * 获取收货地址详情
     *
     * @param request
     * @param response
     * @return
     */
    public static String getPartyPostalAddressesDetail(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        /** 地址Id*/
        String contactMechId = request.getParameter("contactMechId");
        if (UtilValidate.isEmpty(contactMechId) && UtilValidate.isNotEmpty(jsonObject.get("contactMechId"))) {
            contactMechId = jsonObject.getString("contactMechId");
        }
        if (UtilValidate.isEmpty(contactMechId)) {
            request.setAttribute("error", "收货地址Id不能为空");
            response.setStatus(403);
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", contactMechId));
        request.setAttribute("contactMechId", postalAddress.get("contactMechId"));
        request.setAttribute("toName", postalAddress.get("toName"));
        request.setAttribute("mobilePhone", postalAddress.get("mobilePhone"));
        Map<String, Object> stateProvincemap = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(postalAddress.getString("stateProvinceGeoId"))) {
            GenericValue stateProvinceGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("stateProvinceGeoId")));
            stateProvincemap.put("id", postalAddress.getString("stateProvinceGeoId"));
            stateProvincemap.put("title", stateProvinceGeoId.getString("geoName"));
            request.setAttribute("stateProvinceGeoId", stateProvincemap);
        }
        Map<String, Object> citymap = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(postalAddress.getString("city"))) {
            GenericValue cityGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("city")));
            citymap.put("id", postalAddress.getString("city"));
            citymap.put("title", cityGeoId.getString("geoName"));
            request.setAttribute("city", citymap);
        }
        Map<String, Object> countymap = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(postalAddress.getString("countyGeoId"))) {
            GenericValue countyGeoId = delegator.findByPrimaryKeyCache("Geo", UtilMisc.toMap("geoId", postalAddress.getString("countyGeoId")));
            countymap.put("id", postalAddress.getString("countyGeoId"));
            countymap.put("title", countyGeoId.getString("geoName"));
            request.setAttribute("countyGeoId", countymap);
        }
        request.setAttribute("address", postalAddress.get("address1"));
        return "success";
    }


    /**
     * 更新修改收货地址接口
     *
     * @param request
     * @param response
     */
    public static String createOrUpdatePostalAddressAndPurposes(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        /** 获取托管 */
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        /** 地址Id*/
        String contactMechId = request.getParameter("contactMechId");
        if (UtilValidate.isEmpty(contactMechId) && UtilValidate.isNotEmpty(jsonObject.get("contactMechId"))) {
            contactMechId = jsonObject.getString("contactMechId");
        }
        /** 收货人姓名*/
        String toName = request.getParameter("toName");
        if (UtilValidate.isEmpty(toName) && UtilValidate.isNotEmpty(jsonObject.get("toName"))) {
            toName = jsonObject.getString("toName");
        }
        if (UtilValidate.isEmpty(toName)) {
            request.setAttribute("error", "收货人姓名不能为空");
            response.setStatus(403);
            return "error";
        }
        /** 收货人联系方式*/
        String mobilePhone = request.getParameter("mobilePhone");
        if (UtilValidate.isEmpty(mobilePhone) && UtilValidate.isNotEmpty(jsonObject.get("mobilePhone"))) {
            mobilePhone = jsonObject.getString("mobilePhone");
        }
        if (UtilValidate.isEmpty(mobilePhone)) {
            request.setAttribute("error", "联系方式不能为空");
            response.setStatus(403);
            return "error";
        }
        /**收货地址详情(不包含省市区)*/
        String address1 = request.getParameter("address");
        if (UtilValidate.isEmpty(address1) && UtilValidate.isNotEmpty(jsonObject.get("address"))) {
            address1 = jsonObject.getString("address");
        }
        /**省*/
        String stateProvinceGeoId = request.getParameter("stateProvinceGeoId");
        if (UtilValidate.isEmpty(stateProvinceGeoId) && UtilValidate.isNotEmpty(jsonObject.get("stateProvinceGeoId"))) {
            stateProvinceGeoId = jsonObject.getString("stateProvinceGeoId");
        }
        /**市*/
        String city = request.getParameter("city");
        if (UtilValidate.isEmpty(city) && UtilValidate.isNotEmpty(jsonObject.get("city"))) {
            city = jsonObject.getString("city");
        }
        /**区*/
        String countyGeoId = request.getParameter("countyGeoId");
        if (UtilValidate.isEmpty(countyGeoId) && UtilValidate.isNotEmpty(jsonObject.get("countyGeoId"))) {
            countyGeoId = jsonObject.getString("countyGeoId");
        }
        /**是否默认*/
        String defaultId = request.getParameter("defaultId");
        if (UtilValidate.isEmpty(defaultId) && UtilValidate.isNotEmpty(jsonObject.get("defaultId"))) {
            defaultId = jsonObject.getString("defaultId");
        }

        /**根据用户登录信息获取PartyId*/
        String partyId = null;
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            partyId = userLogin.getString("partyId");
        }

        /**contactMechId 地址Id是否为空 ,如果为空 表示新增收货地址,否则更新收货地址*/
        if (UtilValidate.isNotEmpty(contactMechId)) {
            try {
                GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", contactMechId));
                if (UtilValidate.isNotEmpty(postalAddress)) {
                    postalAddress.set("toName", toName);
                    postalAddress.set("address1", address1);
                    postalAddress.set("mobilePhone", mobilePhone);
                    postalAddress.set("stateProvinceGeoId", stateProvinceGeoId);
                    postalAddress.set("city", city);
                    postalAddress.set("countyGeoId", countyGeoId);
                    postalAddress.store();
                }
                GenericValue profiledefs = EntityUtil.getFirst(delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId)));
                if (UtilValidate.isNotEmpty(profiledefs)) {
                    profiledefs.set("defaultShipAddr", defaultId);
                    profiledefs.store();
                } else {
                    GenericValue partyProfileDefault = delegator.makeValue("PartyProfileDefault");
                    partyProfileDefault.set("partyId", partyId);
                    partyProfileDefault.set("defaultShipAddr", defaultId);
                    partyProfileDefault.create();
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            request.setAttribute("success", "更新成功");
        } else {

            GenericValue contactMech = delegator.makeValue("ContactMech");
            String contactId = delegator.getNextSeqId("ContactMech");
            contactMech.set("contactMechId", contactId);
            contactMech.set("contactMechTypeId", "POSTAL_ADDRESS");
            try {
                contactMech.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue postalAddress = delegator.makeValue("PostalAddress");

            postalAddress.set("contactMechId", contactId);
            postalAddress.set("toName", toName);
            postalAddress.set("address1", address1);
            postalAddress.set("mobilePhone", mobilePhone);
            postalAddress.set("stateProvinceGeoId", stateProvinceGeoId);
            postalAddress.set("city", city);
            postalAddress.set("countyGeoId", countyGeoId);
            try {
                postalAddress.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue partyContactMech = delegator.makeValue("PartyContactMech");
            partyContactMech.set("partyId", partyId);
            partyContactMech.set("contactMechId", contactId);
            partyContactMech.set("fromDate", UtilDateTime.nowTimestamp());
            try {
                partyContactMech.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue partyContactMechPurpose = delegator.makeValue("PartyContactMechPurpose");
            partyContactMechPurpose.set("partyId", partyId);
            partyContactMechPurpose.set("contactMechId", contactId);
            partyContactMechPurpose.set("contactMechPurposeTypeId", "SHIPPING_LOCATION");
            partyContactMechPurpose.set("fromDate", UtilDateTime.nowTimestamp());
            try {
                partyContactMechPurpose.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }

            if (defaultId == null) {
                GenericValue profiledefs;
                try {
                    profiledefs = EntityUtil.getFirst(delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId)));
                    if (UtilValidate.isNotEmpty(profiledefs)) {
                        profiledefs.set("defaultShipAddr", contactId);
                        profiledefs.store();
                    } else {
                        List<GenericValue> productStore = delegator.findByAnd("ProductStore", (Object[]) null);
                        String productStoreId = null;
                        if (UtilValidate.isNotEmpty(productStore)) {
                            productStoreId = (String) productStore.get(0).get("productStoreId");
                            GenericValue partyProfileDefault = delegator.makeValue("PartyProfileDefault");
                            partyProfileDefault.set("partyId", partyId);
                            partyProfileDefault.set("productStoreId", productStoreId);
                            partyProfileDefault.set("defaultShipAddr", contactId);
                            partyProfileDefault.create();
                        }
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            request.setAttribute("success", "创建成功");
            request.setAttribute("addressId", contactId);
        }
        return "success";
    }

    /**
     * 删除地址
     *
     * @param request
     * @param response
     */
    public static String deletePostalAddress(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        /** 获取托管 */
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        /** 用户ID*/
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        /** 地址Id*/
        String contactMechId = request.getParameter("contactMechId");
        if (UtilValidate.isEmpty(contactMechId) && UtilValidate.isNotEmpty(jsonObject.get("contactMechId"))) {
            contactMechId = jsonObject.getString("contactMechId");
        }
        if (UtilValidate.isEmpty(contactMechId)) {
            request.setAttribute("error", "地址ID不能为空");
            response.setStatus(403);
            return "error";
        }
        /**根据用户登录信息获取PartyId*/
        String partyId = null;
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            partyId = userLogin.getString("partyId");
        }
        try {
            Map<String, Object> paramContext = FastMap.newInstance();
            paramContext.put("contactMechId", contactMechId);
            paramContext.put("partyId", partyId);
            paramContext.put("userLogin", userLogin);
            try {
                Map<String, Object> resultContext = dispatcher.runSync("deletePartyContactMech", paramContext);
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
            //如果删除的是默认地址 
            GenericValue profiledefs = EntityUtil.getFirst(delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId, "defaultShipAddr", contactMechId)));
            if (UtilValidate.isNotEmpty(profiledefs)) {
                GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
                List<GenericValue> shippingContactMechList = (List<GenericValue>) ContactHelper.getContactMechByType(party, "POSTAL_ADDRESS", false);
                if (UtilValidate.isNotEmpty(shippingContactMechList)) {
                    GenericValue profiledef;
                    try {
                        profiledef = EntityUtil.getFirst(delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId)));
                        if (UtilValidate.isNotEmpty(profiledefs)) {
                            profiledef.set("defaultShipAddr", shippingContactMechList.get(0).get("contactMechId"));
                            profiledef.store();
                        } else {
                            GenericValue partyProfileDefault = delegator.makeValue("PartyProfileDefault");
                            partyProfileDefault.set("partyId", partyId);
                            partyProfileDefault.set("defaultShipAddr", shippingContactMechList.get(0).get("contactMechId"));
                            partyProfileDefault.create();
                        }
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                } else {
                    profiledefs.remove();
                }
            }

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("success", "删除成功");
        return "success";
    }

    /**
     * 设置默认地址
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String savePostalAddressOtherIsDefault(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String contactMechId = request.getParameter("contactMechId");
        if (UtilValidate.isEmpty(contactMechId) && UtilValidate.isNotEmpty(jsonObject.get("contactMechId"))) {
            contactMechId = jsonObject.getString("contactMechId");
        }
        if (UtilValidate.isEmpty(contactMechId)) {
            request.setAttribute("error", "地址ID不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        String partyId = userLogin.getString("partyId");
        List<GenericValue> defalutAddress = delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId));
        if (defalutAddress != null) {
            defalutAddress.get(0).set("defaultShipAddr", contactMechId);
            delegator.storeAll(defalutAddress);
        } else {
            delegator.create("PartyProfileDefault", UtilMisc.toMap("partyId", partyId, "productStoreId", "10000", "defaultShipAddr", contactMechId));
        }
        request.setAttribute("success", "设置成功");
        return "success";
    }

    /**
     * 获取会员优惠券列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getPartyPromoCode(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map<String, Object>> promoList = FastList.newInstance();
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        String partyId = userLogin.getString("partyId");

        try {
            List<GenericValue> productPromoCodePartys = delegator.findByAnd("ProductPromoCodeParty", UtilMisc.toMap("partyId", partyId));
            if (UtilValidate.isNotEmpty(productPromoCodePartys)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (GenericValue productPromoCodeParty : productPromoCodePartys) {
                    List<GenericValue> orderProductPromoCode = delegator.findByAnd("OrderProductPromoCode", UtilMisc.toMap("productPromoCodeId", productPromoCodeParty.get("productPromoCodeId")));
                    if (UtilValidate.isEmpty(orderProductPromoCode)) {
                        GenericValue productPromoCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId", productPromoCodeParty.get("productPromoCodeId")));
                        if (UtilValidate.isNotEmpty(productPromoCode)) {
                            GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", productPromoCode.get("couponCode")));
                            if (UtilValidate.isNotEmpty(productPromoCoupon) && "COUPON_RANGE_COMM".equals(productPromoCoupon.get("couponRange"))) {
                                Map<String, Object> map = FastMap.newInstance();
                                map.put("productPromoCodeId", productPromoCodeParty.get("productPromoCodeId"));
                                map.put("useBeginDate", sdf.format(productPromoCoupon.getTimestamp("useBeginDate")));
                                map.put("useEndDate", sdf.format(productPromoCoupon.getTimestamp("useEndDate")));
                                if ("COUPON_TYPE_REDUCE".equals(productPromoCoupon.get("couponType"))) {
                                    map.put("couponType", "reduce");
                                    map.put("condition", productPromoCoupon.getLong("payFill"));
                                    map.put("money", productPromoCoupon.getBigDecimal("payReduce").setScale(2, BigDecimal.ROUND_HALF_UP));
                                } else if ("COUPON_TYPE_CASH".equals(productPromoCoupon.get("couponType"))) {
                                    map.put("couponType", "cash");
                                    map.put("money", productPromoCoupon.getBigDecimal("arrivedAmount").setScale(2, BigDecimal.ROUND_HALF_UP));
                                }
                                promoList.add(map);
                            }
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }
        request.setAttribute("promoList", promoList);
        return "success";
    }

    /**
     * 消息类型
     *
     * @param request
     * @param response
     * @return
     */
    public static String messageType(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                String partyId = userLogin.getString("partyId");
                Map systemMap = FastMap.newInstance();
                Map activityMap = FastMap.newInstance();
                Map interactMap = FastMap.newInstance();
                DynamicViewEntity dynamicView = new DynamicViewEntity();
                dynamicView.addMemberEntity("PM", "PartyMessage");
                dynamicView.addAlias("PM", "partyId");
                dynamicView.addAlias("PM", "messageType");
                dynamicView.addAlias("PM", "messageContent");
                dynamicView.addAlias("PM", "createdStamp");
                boolean beganTransaction;
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
                //系统消息
                List<EntityCondition> systemConditions = FastList.newInstance();
                systemConditions.add(EntityCondition.makeCondition("partyId", partyId));
                systemConditions.add(EntityCondition.makeCondition("messageType", "system"));
                beganTransaction = TransactionUtil.begin();
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(systemConditions, EntityOperator.AND), null, null, UtilMisc.toList("-createdStamp"), findOpts);
                TransactionUtil.commit(beganTransaction);
                List<GenericValue> systemMessages = pli.getCompleteList();
                systemConditions.add(EntityCondition.makeCondition("isView", "N"));
                Long systemNum = delegator.findCountByCondition("PartyMessage", EntityCondition.makeCondition(systemConditions, EntityOperator.AND), null, null);
                if (UtilValidate.isNotEmpty(systemMessages)) {
                    systemMap.put("messageType", systemMessages.get(0).get("messageType"));
                    systemMap.put("messageContent", systemMessages.get(0).get("messageContent"));
                    if (dateFormat.format(systemMessages.get(0).getTimestamp("createdStamp")).equals(dateFormat.format(new Date()))) {
                        systemMap.put("createdStamp", timeFormat.format(systemMessages.get(0).getTimestamp("createdStamp")));
                    } else {
                        systemMap.put("createdStamp", dateFormat.format(systemMessages.get(0).getTimestamp("createdStamp")));
                    }
                }
                if (UtilValidate.isNotEmpty(systemNum)) {
                    systemMap.put("num", systemNum);
                }
                //活动消息
                List<EntityCondition> activityConditions = FastList.newInstance();
                activityConditions.add(EntityCondition.makeCondition("partyId", partyId));
                activityConditions.add(EntityCondition.makeCondition("messageType", "activity"));
                beganTransaction = TransactionUtil.begin();
                pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(activityConditions, EntityOperator.AND), null, null, UtilMisc.toList("-createdStamp"), findOpts);
                TransactionUtil.commit(beganTransaction);
                List<GenericValue> activityMessages = pli.getCompleteList();
                activityConditions.add(EntityCondition.makeCondition("isView", "N"));
                Long activityNum = delegator.findCountByCondition("PartyMessage", EntityCondition.makeCondition(activityConditions, EntityOperator.AND), null, null);
                if (UtilValidate.isNotEmpty(activityMessages)) {
                    activityMap.put("messageType", activityMessages.get(0).get("messageType"));
                    activityMap.put("messageContent", activityMessages.get(0).get("messageContent"));
                    if (dateFormat.format(activityMessages.get(0).getTimestamp("createdStamp")).equals(dateFormat.format(new Date()))) {
                        activityMap.put("createdStamp", timeFormat.format(activityMessages.get(0).getTimestamp("createdStamp")));
                    } else {
                        activityMap.put("createdStamp", dateFormat.format(activityMessages.get(0).getTimestamp("createdStamp")));
                    }
                }
                if (UtilValidate.isNotEmpty(activityNum)) {
                    activityMap.put("num", activityNum);
                }
                //互动消息
                List<EntityCondition> interactConditions = FastList.newInstance();
                interactConditions.add(EntityCondition.makeCondition("partyId", partyId));
                interactConditions.add(EntityCondition.makeCondition("messageType", "interact"));
                beganTransaction = TransactionUtil.begin();
                pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(interactConditions, EntityOperator.AND), null, null, UtilMisc.toList("-createdStamp"), findOpts);
                TransactionUtil.commit(beganTransaction);
                List<GenericValue> interactMessages = pli.getCompleteList();
                interactConditions.add(EntityCondition.makeCondition("isView", "N"));
                Long interactNum = delegator.findCountByCondition("PartyMessage", EntityCondition.makeCondition(interactConditions, EntityOperator.AND), null, null);
                if (UtilValidate.isNotEmpty(interactMessages)) {
                    interactMap.put("messageType", interactMessages.get(0).get("messageType"));
                    interactMap.put("messageContent", interactMessages.get(0).get("messageContent"));
                    if (dateFormat.format(interactMessages.get(0).getTimestamp("createdStamp")).equals(dateFormat.format(new Date()))) {
                        interactMap.put("createdStamp", timeFormat.format(interactMessages.get(0).getTimestamp("createdStamp")));
                    } else {
                        interactMap.put("createdStamp", dateFormat.format(interactMessages.get(0).getTimestamp("createdStamp")));
                    }
                }
                if (UtilValidate.isNotEmpty(interactNum)) {
                    interactMap.put("num", interactNum);
                }
                pli.close();
                request.setAttribute("system", systemMap);
                request.setAttribute("activity", activityMap);
                request.setAttribute("interact", interactMap);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    /**
     * 消息列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String messageList(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String messageType = request.getParameter("messageType");
        if (UtilValidate.isEmpty(messageType) && UtilValidate.isNotEmpty(jsonObject.get("messageType"))) {
            messageType = jsonObject.getString("messageType");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            List<Map> resultList = FastList.newInstance();
            int lowIndex;
            int highIndex;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            try {
                DynamicViewEntity dynamicView = new DynamicViewEntity();
                dynamicView.addMemberEntity("PM", "PartyMessage");
                dynamicView.addAlias("PM", "partyMessageId");
                dynamicView.addAlias("PM", "partyId");
                dynamicView.addAlias("PM", "messageType");
                dynamicView.addAlias("PM", "isView");
                dynamicView.addAlias("PM", "messageContent");
                dynamicView.addAlias("PM", "linkType");
                dynamicView.addAlias("PM", "linkId");
                dynamicView.addAlias("PM", "createdStamp");
                // get the indexes for the partial list
                lowIndex = viewIndex + 1;
                highIndex = viewIndex + viewSize;

                List<EntityCondition> conditions = FastList.newInstance();
                if (UtilValidate.isNotEmpty(messageType)) {
                    conditions.add(EntityCondition.makeCondition("messageType", messageType));
                }
                conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
                // set distinct on so we only get one row per order
                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                // using list iterator
                boolean beganTransaction = TransactionUtil.begin();
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-createdStamp"), findOpts);
                TransactionUtil.commit(beganTransaction);
                for (GenericValue result : pli.getPartialList(lowIndex, viewSize)) {
                    Map map = FastMap.newInstance();
                    map.put("partyMessageId", result.get("partyMessageId"));
                    map.put("isView", result.get("isView"));
                    map.put("messageContent", result.get("messageContent"));
                    if (UtilValidate.areEqual("order", result.get("linkType"))) {
                        GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", result.get("linkId")));
                        map.put("linkType", orderHeader.get("salesOrderType"));
                    } else if (UtilValidate.areEqual("FLT_SPLJ", result.get("linkType"))) {
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", result.get("linkId")));
                        if (UtilValidate.areEqual("CAR_GOOD", product.get("productTypeId"))) {
                            map.put("linkType", product.get("carTypeId"));
                        } else if (UtilValidate.areEqual("RECREATION_GOOD", product.get("productTypeId"))) {
                            map.put("linkType", product.get("recreationType"));
                        } else {
                            map.put("linkType", product.get("productTypeId"));
                        }
                    } else {
                        map.put("linkType", result.get("linkType"));
                    }
                    map.put("linkId", result.get("linkId"));
                    if (dateFormat.format(result.getTimestamp("createdStamp")).equals(dateFormat.format(new Date()))) {
                        map.put("createdStamp", timeFormat.format(result.getTimestamp("createdStamp")));
                    } else {
                        map.put("createdStamp", dateFormat.format(result.getTimestamp("createdStamp")));
                    }
                    resultList.add(map);
                }
                int resultSize = pli.getResultsSizeAfterPartialList();
                // close the list iterator
                pli.close();
                request.setAttribute("resultList", resultList);
                request.setAttribute("max", resultSize);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    /**
     * 更新消息
     *
     * @param request
     * @param response
     * @return
     */
    public static String updateMessage(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String messageType = request.getParameter("messageType");
        if (UtilValidate.isEmpty(messageType) && UtilValidate.isNotEmpty(jsonObject.get("messageType"))) {
            messageType = jsonObject.getString("messageType");
        }
        if (UtilValidate.isEmpty(messageType)) {
            request.setAttribute("error", "消息类型不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("messageType", messageType));
        conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
        try {
            int num = delegator.storeByCondition("PartyMessage", UtilMisc.toMap("isView", "Y"), EntityCondition.makeCondition(conditions, EntityOperator.AND));
            if (num > 0) {
                request.setAttribute("success", "更新成功");
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        return "success";
    }

    /**
     * 获取基础数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBaseInfo(HttpServletRequest request, HttpServletResponse response) {
        String authorization = request.getHeader("Authorization");
        Map<String, String> tokenMap = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(authorization)) {
            tokenMap = checkAuthToken(request, response);
        }
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        String webSiteId = request.getParameter("webSiteId");
        if (UtilValidate.isEmpty(webSiteId) && UtilValidate.isNotEmpty(jsonObject.get("webSiteId"))) {
            webSiteId = jsonObject.getString("webSiteId");
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            request.setAttribute("error", "站点编号不能为空");
            response.setStatus(403);
            return "error";
        }
        String exPlat = request.getParameter("exPlat");
        if (UtilValidate.isEmpty(exPlat) && UtilValidate.isNotEmpty(jsonObject.get("exPlat"))) {
            exPlat = jsonObject.getString("exPlat");
        }
        if (UtilValidate.isEmpty(exPlat)) {
            request.setAttribute("error", "请指定PC端或者移动端");
            response.setStatus(403);
            return "error";
        }
        try {
            GenericValue productStore;
            GenericValue userLogin = null;
            if (UtilValidate.isNotEmpty(userLoginId)) {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            }
            if (UtilValidate.isEmpty(productStoreId)) {
                if (UtilValidate.isNotEmpty(userLogin)) {
                    GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
                    if (UtilValidate.isNotEmpty(party.get("productStoreId"))) {
                        productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", party.get("productStoreId")));
                    } else {
                        productStore = EntityUtil.getFirst(delegator.findByAnd("ProductStore", FastMap.newInstance()));
                    }
                } else {
                    productStore = EntityUtil.getFirst(delegator.findByAnd("ProductStore", FastMap.newInstance()));
                }
            } else {
                productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
            }
            boolean beganTransaction;
            //站点
            DynamicViewEntity webSiteViewEntity = new DynamicViewEntity();
            webSiteViewEntity.addMemberEntity("WS", "WebSite");
            webSiteViewEntity.addAlias("WS", "webSiteId", "webSiteId", null, true, true, null);
            webSiteViewEntity.addAlias("WS", "isEnabled");
            webSiteViewEntity.addMemberEntity("WPS", "WebsiteProductStorelink");
            webSiteViewEntity.addAlias("WPS", "productStoreId");
            webSiteViewEntity.addViewLink("WS", "WPS", true, ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
            List<EntityCondition> webSiteConditions = FastList.newInstance();
            webSiteConditions.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            webSiteConditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
            webSiteConditions.add(EntityCondition.makeCondition("productStoreId", productStore.get("productStoreId")));
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(webSiteViewEntity, EntityCondition.makeCondition(webSiteConditions, EntityOperator.AND), null, null, null,findOpts );
            List<GenericValue> webSites = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<String> webSiteIds = FastList.newInstance();
            if (UtilValidate.isNotEmpty(webSites)) {
                for (GenericValue website : webSites) {
                    webSiteIds.add(website.getString("webSiteId"));
                }
            }
            request.setAttribute("productStoreId", productStore.get("productStoreId"));
            request.setAttribute("cityGeoId", productStore.get("cityGeoId"));
            request.setAttribute("storeName", productStore.get("storeName"));
            String productStoreAddress = "";
            if (UtilValidate.isNotEmpty(productStore.get("stateProvinceGeoId"))) {
                GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("stateProvinceGeoId")));
                productStoreAddress += geo.getString("geoName");
            }
            if (UtilValidate.isNotEmpty(productStore.get("cityGeoId"))) {
                GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("cityGeoId")));
                productStoreAddress += geo.getString("geoName");
            }
            if (UtilValidate.isNotEmpty(productStore.get("countyGeoId"))) {
                GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("countyGeoId")));
                productStoreAddress += geo.getString("geoName");
            }
            if (UtilValidate.isNotEmpty(productStore.get("address"))) {
                productStoreAddress += productStore.getString("address");
            }
            //地址
            request.setAttribute("productStoreAddress", productStoreAddress);
            if (UtilValidate.isNotEmpty(userLogin)) {
                List<EntityCondition> conditions = FastList.newInstance();
                conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
                conditions.add(EntityCondition.makeCondition("isView", "N"));
                Long messageNum = delegator.findCountByCondition("PartyMessage", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null);
                request.setAttribute("messageNum", messageNum);
            } else {
                request.setAttribute("messageNum", 0);
            }
            //头条数据--公告
            DynamicViewEntity noticeViewEntity = new DynamicViewEntity();
            noticeViewEntity.addMemberEntity("N", "Notice");
            noticeViewEntity.addAlias("N", "noticeId", "noticeId", null, true, true, null);
            noticeViewEntity.addAlias("N", "noticeTitle");
            noticeViewEntity.addAlias("N", "tagId");
            noticeViewEntity.addAlias("N", "isUse");
            noticeViewEntity.addAlias("N", "isAllWebSite");
            noticeViewEntity.addAlias("N", "sequenceId");
            noticeViewEntity.addAlias("N", "firstLinkType");
            noticeViewEntity.addAlias("N", "linkUrl");
            noticeViewEntity.addAlias("N", "linkId");
            noticeViewEntity.addMemberEntity("NWS", "NoticeWebSite");
            noticeViewEntity.addAlias("NWS", "webSiteId");
            noticeViewEntity.addViewLink("N", "NWS", true, ModelKeyMap.makeKeyMapList("noticeId", "noticeId"));
            List<EntityCondition> noticeConditions1 = FastList.newInstance();
            noticeConditions1.add(EntityCondition.makeCondition("isUse", "0"));
            List<EntityCondition> noticeConditions2 = FastList.newInstance();
            noticeConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            noticeConditions2.add(EntityCondition.makeCondition("webSiteId", EntityOperator.IN, webSiteIds));
            noticeConditions1.add(EntityCondition.makeCondition(noticeConditions2, EntityOperator.OR));
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(noticeViewEntity, EntityCondition.makeCondition(noticeConditions1, EntityOperator.AND), null, null, UtilMisc.toList("sequenceId"), findOpts);
            List<GenericValue> notices = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(notices)) {
                List<Map> noticeList = FastList.newInstance();
                for (GenericValue notice : notices) {
                    Map noticeMap = FastMap.newInstance();
                    noticeMap.put("noticeTitle", notice.get("noticeTitle"));
                    if (UtilValidate.isNotEmpty(notice.get("tagId"))) {
                        GenericValue tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", notice.get("tagId")));
                        noticeMap.put("noticeTag", tag.get("tagName"));
                    }
                    if (UtilValidate.areEqual("FLT_ZDYLJ", notice.get("firstLinkType"))) {
                        noticeMap.put("linkType", notice.get("firstLinkType"));
                        noticeMap.put("linkUrl", notice.get("linkUrl"));
                    } else if (UtilValidate.areEqual("FLT_SPLJ", notice.get("firstLinkType"))) {
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", notice.get("linkId")));
                        if (UtilValidate.areEqual("CAR_GOOD", product.get("productTypeId"))) {
                            noticeMap.put("linkType", product.get("carTypeId"));
                        } else if (UtilValidate.areEqual("RECREATION_GOOD", product.get("productTypeId"))) {
                            noticeMap.put("linkType", product.get("recreationType"));
                        } else {
                            noticeMap.put("linkType", product.get("productTypeId"));
                        }
                        noticeMap.put("linkId", notice.get("linkId"));
                    } else {
                        noticeMap.put("linkType", notice.get("firstLinkType"));
                        noticeMap.put("linkId", notice.get("linkId"));
                    }
                    noticeList.add(noticeMap);
                }
                request.setAttribute("noticeList", noticeList);
            }
            //轮播图区块--广告
            DynamicViewEntity bannerViewEntity1 = new DynamicViewEntity();
            bannerViewEntity1.addMemberEntity("B", "Banner");
            bannerViewEntity1.addAlias("B", "lastUpdatedStamp");
            DynamicViewEntity bannerViewEntity2 = new DynamicViewEntity();
            bannerViewEntity2.addMemberEntity("BWS", "BannerWebSite");
            bannerViewEntity2.addAlias("BWS", "lastUpdatedStamp");
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(bannerViewEntity1, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> banners1 = eli.getCompleteList();
            eli = delegator.findListIteratorByCondition(bannerViewEntity2, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> banners2 = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(banners1) && UtilValidate.isEmpty(banners2)) {
                request.setAttribute("bannerTime", UtilDateTime.timeStampToString(banners1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isEmpty(banners1) && UtilValidate.isNotEmpty(banners2)) {
                request.setAttribute("bannerTime", UtilDateTime.timeStampToString(banners2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isNotEmpty(banners1) && UtilValidate.isNotEmpty(banners2)) {
                if (banners1.get(0).getTimestamp("lastUpdatedStamp").after(banners2.get(0).getTimestamp("lastUpdatedStamp"))) {
                    request.setAttribute("bannerTime", UtilDateTime.timeStampToString(banners1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                } else {
                    request.setAttribute("bannerTime", UtilDateTime.timeStampToString(banners2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                }
            }
            //分类区块--导航菜单
            DynamicViewEntity navigationViewEntity1 = new DynamicViewEntity();
            navigationViewEntity1.addMemberEntity("NM", "NavigationMenu");
            navigationViewEntity1.addAlias("NM", "lastUpdatedStamp");
            DynamicViewEntity navigationViewEntity2 = new DynamicViewEntity();
            navigationViewEntity2.addMemberEntity("NMWSR", "NavigationMenuWebSiteRef");
            navigationViewEntity2.addAlias("NMWSR", "lastUpdatedStamp");
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(navigationViewEntity1, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> navigationMenus1 = eli.getCompleteList();
            eli = delegator.findListIteratorByCondition(navigationViewEntity2, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> navigationMenus2 = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(navigationMenus1) && UtilValidate.isEmpty(navigationMenus2)) {
                request.setAttribute("navigationTime", UtilDateTime.timeStampToString(navigationMenus1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isEmpty(navigationMenus1) && UtilValidate.isNotEmpty(navigationMenus2)) {
                request.setAttribute("navigationTime", UtilDateTime.timeStampToString(navigationMenus2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isNotEmpty(navigationMenus1) && UtilValidate.isNotEmpty(navigationMenus2)) {
                if (banners1.get(0).getTimestamp("lastUpdatedStamp").after(banners2.get(0).getTimestamp("lastUpdatedStamp"))) {
                    request.setAttribute("navigationTime", UtilDateTime.timeStampToString(navigationMenus1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                } else {
                    request.setAttribute("navigationTime", UtilDateTime.timeStampToString(navigationMenus2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                }
            }
            //活动区块--活动管理
            DynamicViewEntity topicActivityViewEntity1 = new DynamicViewEntity();
            topicActivityViewEntity1.addMemberEntity("PTA", "ProductTopicActivity");
            topicActivityViewEntity1.addAlias("PTA", "lastUpdatedStamp");
            DynamicViewEntity topicActivityViewEntity2 = new DynamicViewEntity();
            topicActivityViewEntity2.addMemberEntity("PTAWS", "ProductTopicActivityWebSite");
            topicActivityViewEntity2.addAlias("PTAWS", "lastUpdatedStamp");
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(topicActivityViewEntity1, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> topicActivityManagers1 = eli.getCompleteList();
            eli = delegator.findListIteratorByCondition(topicActivityViewEntity2, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> topicActivityManagers2 = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(topicActivityManagers1) && UtilValidate.isEmpty(topicActivityManagers2)) {
                request.setAttribute("activityTime", UtilDateTime.timeStampToString(topicActivityManagers1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isEmpty(topicActivityManagers1) && UtilValidate.isNotEmpty(topicActivityManagers2)) {
                request.setAttribute("activityTime", UtilDateTime.timeStampToString(topicActivityManagers2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isNotEmpty(topicActivityManagers1) && UtilValidate.isNotEmpty(topicActivityManagers2)) {
                if (banners1.get(0).getTimestamp("lastUpdatedStamp").after(banners2.get(0).getTimestamp("lastUpdatedStamp"))) {
                    request.setAttribute("activityTime", UtilDateTime.timeStampToString(topicActivityManagers1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                } else {
                    request.setAttribute("activityTime", UtilDateTime.timeStampToString(topicActivityManagers2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                }
            }
            //品牌区块
            DynamicViewEntity brandViewEntity1 = new DynamicViewEntity();
            brandViewEntity1.addMemberEntity("PB", "ProductBrand");
            brandViewEntity1.addAlias("PB", "lastUpdatedStamp");
            DynamicViewEntity brandViewEntity2 = new DynamicViewEntity();
            brandViewEntity2.addMemberEntity("PBC", "ProductBrandCategory");
            brandViewEntity2.addAlias("PBC", "lastUpdatedStamp");
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(brandViewEntity1, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> productBrands1 = eli.getCompleteList();
            eli = delegator.findListIteratorByCondition(brandViewEntity2, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> productBrands2 = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(productBrands1) && UtilValidate.isEmpty(productBrands2)) {
                request.setAttribute("brandTime", UtilDateTime.timeStampToString(productBrands1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isEmpty(productBrands1) && UtilValidate.isNotEmpty(productBrands2)) {
                request.setAttribute("brandTime", UtilDateTime.timeStampToString(productBrands2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            } else if (UtilValidate.isNotEmpty(productBrands1) && UtilValidate.isNotEmpty(productBrands2)) {
                if (banners1.get(0).getTimestamp("lastUpdatedStamp").after(banners2.get(0).getTimestamp("lastUpdatedStamp"))) {
                    request.setAttribute("brandTime", UtilDateTime.timeStampToString(productBrands1.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                } else {
                    request.setAttribute("brandTime", UtilDateTime.timeStampToString(productBrands2.get(0).getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                }
            }
            //车型区块
            DynamicViewEntity carModelViewEntity1 = new DynamicViewEntity();
            carModelViewEntity1.addMemberEntity("PCM", "ProductCarModel");
            carModelViewEntity1.addAlias("PCM", "lastUpdatedStamp");
            DynamicViewEntity carModelViewEntity2 = new DynamicViewEntity();
            carModelViewEntity2.addMemberEntity("P", "Product");
            carModelViewEntity2.addAlias("P", "lastUpdatedStamp");
            DynamicViewEntity carModelViewEntity3 = new DynamicViewEntity();
            carModelViewEntity3.addMemberEntity("PSPA", "ProductStoreProductAssoc");
            carModelViewEntity3.addAlias("PSPA", "lastUpdatedStamp");
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(carModelViewEntity1, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> productCarModels1 = eli.getCompleteList();
            eli = delegator.findListIteratorByCondition(carModelViewEntity2, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> productCarModels2 = eli.getCompleteList();
            eli = delegator.findListIteratorByCondition(carModelViewEntity3, null, null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> productCarModels3 = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            Timestamp lastUpdatedStamp = null;
            if (UtilValidate.isNotEmpty(productCarModels1)) {
                lastUpdatedStamp = productCarModels1.get(0).getTimestamp("lastUpdatedStamp");
                if (UtilValidate.isNotEmpty(productCarModels2) && lastUpdatedStamp.before(productCarModels2.get(0).getTimestamp("lastUpdatedStamp"))) {
                    lastUpdatedStamp = productCarModels2.get(0).getTimestamp("lastUpdatedStamp");
                }
                if (UtilValidate.isNotEmpty(productCarModels3) && lastUpdatedStamp.before(productCarModels3.get(0).getTimestamp("lastUpdatedStamp"))) {
                    lastUpdatedStamp = productCarModels3.get(0).getTimestamp("lastUpdatedStamp");
                }
            } else if (UtilValidate.isNotEmpty(productCarModels2)) {
                lastUpdatedStamp = productCarModels2.get(0).getTimestamp("lastUpdatedStamp");
                if (UtilValidate.isNotEmpty(productCarModels3) && lastUpdatedStamp.before(productCarModels3.get(0).getTimestamp("lastUpdatedStamp"))) {
                    lastUpdatedStamp = productCarModels3.get(0).getTimestamp("lastUpdatedStamp");
                }
            } else if (UtilValidate.isNotEmpty(productCarModels3)) {
                lastUpdatedStamp = productCarModels3.get(0).getTimestamp("lastUpdatedStamp");
            }
            request.setAttribute("carModelTime", UtilDateTime.timeStampToString(lastUpdatedStamp, "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            //系统时间
            request.setAttribute("systemTime", UtilDateTime.timeStampToString(UtilDateTime.nowTimestamp(), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            if (UtilValidate.isNotEmpty(userLogin)) {
                GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
                party.set("productStoreId", productStore.get("productStoreId"));
                delegator.store(party);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 首页轮播数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBaseBanner(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String webSiteId = request.getParameter("webSiteId");
        if (UtilValidate.isEmpty(webSiteId) && UtilValidate.isNotEmpty(jsonObject.get("webSiteId"))) {
            webSiteId = jsonObject.getString("webSiteId");
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            request.setAttribute("error", "站点编号不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            boolean beganTransaction;
            //站点
            DynamicViewEntity webSiteViewEntity = new DynamicViewEntity();
            webSiteViewEntity.addMemberEntity("WS", "WebSite");
            webSiteViewEntity.addAlias("WS", "webSiteId", "webSiteId", null, true, true, null);
            webSiteViewEntity.addAlias("WS", "isEnabled");
            webSiteViewEntity.addMemberEntity("WPS", "WebsiteProductStorelink");
            webSiteViewEntity.addAlias("WPS", "productStoreId");
            webSiteViewEntity.addViewLink("WS", "WPS", true, ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
            List<EntityCondition> webSiteConditions = FastList.newInstance();
            webSiteConditions.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            webSiteConditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
            webSiteConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(webSiteViewEntity, EntityCondition.makeCondition(webSiteConditions, EntityOperator.AND), null, null, null, findOpts);
            List<GenericValue> webSites = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<String> webSiteIds = FastList.newInstance();
            if (UtilValidate.isNotEmpty(webSites)) {
                for (GenericValue website : webSites) {
                    webSiteIds.add(website.getString("webSiteId"));
                }
            }
            //轮播图区块--广告
            DynamicViewEntity bannerViewEntity = new DynamicViewEntity();
            bannerViewEntity.addMemberEntity("B", "Banner");
            bannerViewEntity.addAlias("B", "bannerId", "bannerId", null, true, true, null);
            bannerViewEntity.addAlias("B", "isUse");
            bannerViewEntity.addAlias("B", "isAllWebSite");
            bannerViewEntity.addAlias("B", "sequenceId");
            bannerViewEntity.addAlias("B", "contentId");
            bannerViewEntity.addAlias("B", "firstLinkType");
            bannerViewEntity.addAlias("B", "linkId");
            bannerViewEntity.addAlias("B", "linkUrl");
            bannerViewEntity.addAlias("B", "applyScope");
            bannerViewEntity.addMemberEntity("BWS", "BannerWebSite");
            bannerViewEntity.addAlias("BWS", "webSiteId");
            bannerViewEntity.addViewLink("B", "BWS", true, ModelKeyMap.makeKeyMapList("bannerId", "bannerId"));
            List<EntityCondition> bannerConditions1 = FastList.newInstance();
            bannerConditions1.add(EntityCondition.makeCondition("isUse", "0"));
            bannerConditions1.add(EntityCondition.makeCondition("applyScope", null));
            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", EntityOperator.IN, webSiteIds));
            bannerConditions1.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            beganTransaction = TransactionUtil.begin();
            
            eli = delegator.findListIteratorByCondition(bannerViewEntity, EntityCondition.makeCondition(bannerConditions1, EntityOperator.AND), null, null, UtilMisc.toList("sequenceId"), findOpts);
            List<GenericValue> banners = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(banners)) {
                List<Map> bannerList = FastList.newInstance();
                for (GenericValue banner : banners) {
                    Map bannerMap = FastMap.newInstance();
                    bannerMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + banner.getString("contentId"));
                    if (UtilValidate.areEqual("FLT_ZDYLJ", banner.get("firstLinkType"))) {
                        bannerMap.put("linkType", banner.get("firstLinkType"));
                        bannerMap.put("linkUrl", banner.get("linkUrl"));
                    } else if (UtilValidate.areEqual("FLT_SPLJ", banner.get("firstLinkType"))) {
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", banner.get("linkId")));
                        if (UtilValidate.areEqual("CAR_GOOD", product.get("productTypeId"))) {
                            bannerMap.put("linkType", product.get("carTypeId"));
                        } else if (UtilValidate.areEqual("RECREATION_GOOD", product.get("productTypeId"))) {
                            bannerMap.put("linkType", product.get("recreationType"));
                        } else {
                            bannerMap.put("linkType", product.get("productTypeId"));
                        }
                        bannerMap.put("linkId", banner.get("linkId"));
                    } else {
                        bannerMap.put("linkType", banner.get("firstLinkType"));
                        bannerMap.put("linkId", banner.get("linkId"));
                    }
                    bannerList.add(bannerMap);
                }
                request.setAttribute("bannerList", bannerList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 首页分类数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBaseNavigation(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String exPlat = request.getParameter("exPlat");
        if (UtilValidate.isEmpty(exPlat) && UtilValidate.isNotEmpty(jsonObject.get("exPlat"))) {
            exPlat = jsonObject.getString("exPlat");
        }
        if (UtilValidate.isEmpty(exPlat)) {
            request.setAttribute("error", "请指定PC端或者移动端");
            response.setStatus(403);
            return "error";
        }
        String webSiteId = request.getParameter("webSiteId");
        if (UtilValidate.isEmpty(webSiteId) && UtilValidate.isNotEmpty(jsonObject.get("webSiteId"))) {
            webSiteId = jsonObject.getString("webSiteId");
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            request.setAttribute("error", "站点编号不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            boolean beganTransaction;
            //站点
            DynamicViewEntity webSiteViewEntity = new DynamicViewEntity();
            webSiteViewEntity.addMemberEntity("WS", "WebSite");
            webSiteViewEntity.addAlias("WS", "webSiteId", "webSiteId", null, true, true, null);
            webSiteViewEntity.addAlias("WS", "isEnabled");
            webSiteViewEntity.addMemberEntity("WPS", "WebsiteProductStorelink");
            webSiteViewEntity.addAlias("WPS", "productStoreId");
            webSiteViewEntity.addViewLink("WS", "WPS", true, ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
            List<EntityCondition> webSiteConditions = FastList.newInstance();
            webSiteConditions.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            webSiteConditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
            webSiteConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(webSiteViewEntity, EntityCondition.makeCondition(webSiteConditions, EntityOperator.AND), null, null, null, findOpts);
            List<GenericValue> webSites = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<String> webSiteIds = FastList.newInstance();
            if (UtilValidate.isNotEmpty(webSites)) {
                for (GenericValue website : webSites) {
                    webSiteIds.add(website.getString("webSiteId"));
                }
            }
            //分类区块--导航菜单
            DynamicViewEntity navigationViewEntity = new DynamicViewEntity();
            navigationViewEntity.addMemberEntity("NM", "NavigationMenu");
            navigationViewEntity.addAlias("NM", "navId", "navId", null, true, true, null);
            navigationViewEntity.addAlias("NM", "isEnabled");
            navigationViewEntity.addAlias("NM", "exPlat");
            navigationViewEntity.addAlias("NM", "isAllWebSite");
            navigationViewEntity.addAlias("NM", "seqNo");
            navigationViewEntity.addAlias("NM", "navName");
            navigationViewEntity.addAlias("NM", "navType");
            navigationViewEntity.addAlias("NM", "contentId");
            navigationViewEntity.addMemberEntity("NMWSR", "NavigationMenuWebSiteRef");
            navigationViewEntity.addAlias("NMWSR", "webSiteId");
            navigationViewEntity.addViewLink("NM", "NMWSR", true, ModelKeyMap.makeKeyMapList("navId", "navId"));
            List<EntityCondition> navigationConditions1 = FastList.newInstance();
            navigationConditions1.add(EntityCondition.makeCondition("isEnabled", "Y"));
            navigationConditions1.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("exPlat"), EntityOperator.LIKE, EntityFunction.UPPER("%" + exPlat + "%")));
            List<EntityCondition> navigationConditions2 = FastList.newInstance();
            navigationConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            navigationConditions2.add(EntityCondition.makeCondition("webSiteId", EntityOperator.IN, webSiteIds));
            navigationConditions1.add(EntityCondition.makeCondition(navigationConditions2, EntityOperator.OR));
            beganTransaction = TransactionUtil.begin();
         
            eli = delegator.findListIteratorByCondition(navigationViewEntity, EntityCondition.makeCondition(navigationConditions1, EntityOperator.AND), null, null, UtilMisc.toList("seqNo"), findOpts);
            List<GenericValue> navigationMenus = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(navigationMenus)) {
                List<Map> navigationList = FastList.newInstance();
                for (GenericValue navigationMenu : navigationMenus) {
                    Map navigationMap = FastMap.newInstance();
                    navigationMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + navigationMenu.getString("contentId"));
                    navigationMap.put("navName", navigationMenu.get("navName"));
                    navigationMap.put("navType", navigationMenu.get("navType"));
                    navigationList.add(navigationMap);
                }
                request.setAttribute("navigationList", navigationList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 首页专题活动数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBaseTopicActivity(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String webSiteId = request.getParameter("webSiteId");
        if (UtilValidate.isEmpty(webSiteId) && UtilValidate.isNotEmpty(jsonObject.get("webSiteId"))) {
            webSiteId = jsonObject.getString("webSiteId");
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            request.setAttribute("error", "站点编号不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            boolean beganTransaction;
            //站点
            DynamicViewEntity webSiteViewEntity = new DynamicViewEntity();
            webSiteViewEntity.addMemberEntity("WS", "WebSite");
            webSiteViewEntity.addAlias("WS", "webSiteId", "webSiteId", null, true, true, null);
            webSiteViewEntity.addAlias("WS", "isEnabled");
            webSiteViewEntity.addMemberEntity("WPS", "WebsiteProductStorelink");
            webSiteViewEntity.addAlias("WPS", "productStoreId");
            webSiteViewEntity.addViewLink("WS", "WPS", true, ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
            List<EntityCondition> webSiteConditions = FastList.newInstance();
            webSiteConditions.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            webSiteConditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
            webSiteConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(webSiteViewEntity, EntityCondition.makeCondition(webSiteConditions, EntityOperator.AND), null, null, null, findOpts);
            List<GenericValue> webSites = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<String> webSiteIds = FastList.newInstance();
            if (UtilValidate.isNotEmpty(webSites)) {
                for (GenericValue website : webSites) {
                    webSiteIds.add(website.getString("webSiteId"));
                }
            }
            //活动区块--活动管理
            DynamicViewEntity topicActivityViewEntity = new DynamicViewEntity();
            topicActivityViewEntity.addMemberEntity("PTA", "ProductTopicActivity");
            topicActivityViewEntity.addAlias("PTA", "productTopicActivityId", "productTopicActivityId", null, true, true, null);
            topicActivityViewEntity.addAlias("PTA", "isUse");
            topicActivityViewEntity.addAlias("PTA", "isAllWebSite");
            topicActivityViewEntity.addAlias("PTA", "bigImg");
            topicActivityViewEntity.addAlias("PTA", "linkType");
            topicActivityViewEntity.addAlias("PTA", "linkUrl");
            topicActivityViewEntity.addAlias("PTA", "linkId");
            topicActivityViewEntity.addAlias("PTA", "sequenceId");
            topicActivityViewEntity.addMemberEntity("PTAWS", "ProductTopicActivityWebSite");
            topicActivityViewEntity.addAlias("PTAWS", "webSiteId");
            topicActivityViewEntity.addViewLink("PTA", "PTAWS", true, ModelKeyMap.makeKeyMapList("productTopicActivityId", "productTopicActivityId"));
            List<EntityCondition> topicActivityConditions1 = FastList.newInstance();
            topicActivityConditions1.add(EntityCondition.makeCondition("isUse", "0"));
            List<EntityCondition> topicActivityConditions2 = FastList.newInstance();
            topicActivityConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            topicActivityConditions2.add(EntityCondition.makeCondition("webSiteId", EntityOperator.IN, webSiteIds));
            topicActivityConditions1.add(EntityCondition.makeCondition(topicActivityConditions2, EntityOperator.OR));
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(topicActivityViewEntity, EntityCondition.makeCondition(topicActivityConditions1, EntityOperator.AND), null, null, UtilMisc.toList("sequenceId"), findOpts);
            List<GenericValue> topicActivitys = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(topicActivitys)) {
                List<Map> activityList = FastList.newInstance();
                for (GenericValue topicActivity : topicActivitys) {
                    Map activityMap = FastMap.newInstance();
                    activityMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + topicActivity.getString("bigImg"));
                    if (UtilValidate.areEqual("FLT_ZDYLJ", topicActivity.get("linkType"))) {
                        activityMap.put("linkType", topicActivity.get("linkType"));
                        activityMap.put("linkUrl", topicActivity.get("linkUrl"));
                    } else if (UtilValidate.areEqual("FLT_SPLJ", topicActivity.get("linkType"))) {
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", topicActivity.get("linkId")));
                        if (UtilValidate.areEqual("CAR_GOOD", product.get("productTypeId"))) {
                            activityMap.put("linkType", product.get("carTypeId"));
                        } else if (UtilValidate.areEqual("RECREATION_GOOD", product.get("productTypeId"))) {
                            activityMap.put("linkType", product.get("recreationType"));
                        } else {
                            activityMap.put("linkType", product.get("productTypeId"));
                        }
                        activityMap.put("linkId", topicActivity.get("linkId"));
                    } else {
                        activityMap.put("linkType", topicActivity.get("linkType"));
                        activityMap.put("linkId", topicActivity.get("linkId"));
                    }
                    activityList.add(activityMap);
                }
                request.setAttribute("activityList", activityList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 首页品牌数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBaseBrand(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String brandType = request.getParameter("brandType");
        if (UtilValidate.isEmpty(brandType) && UtilValidate.isNotEmpty(jsonObject.get("brandType"))) {
            brandType = jsonObject.getString("brandType");
        }
        if (UtilValidate.isEmpty(brandType)) {
            request.setAttribute("error", "品牌分类不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            //品牌区块
            DynamicViewEntity brandViewEntity = new DynamicViewEntity();
            brandViewEntity.addMemberEntity("PB", "ProductBrand");
            brandViewEntity.addAlias("PB", "productBrandId", "productBrandId", null, false, true, null);
            brandViewEntity.addAlias("PB", "isHotNewCar");
            brandViewEntity.addAlias("PB", "isHotOldCar");
            brandViewEntity.addAlias("PB", "isHotCarType");
            brandViewEntity.addAlias("PB", "isCarTypeBrand");
            brandViewEntity.addAlias("PB", "brandName");
            brandViewEntity.addAlias("PB", "contentId");
            brandViewEntity.addAlias("PB", "isUsed");
            brandViewEntity.addAlias("PB", "isDel");
            brandViewEntity.addMemberEntity("PBC", "ProductBrandCategory");
            brandViewEntity.addAlias("PBC", "productCategoryId");
            brandViewEntity.addViewLink("PB", "PBC", true, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));
            List<EntityCondition> brandConditions = FastList.newInstance();
            brandConditions.add(EntityCondition.makeCondition("isUsed", "Y"));
            brandConditions.add(EntityCondition.makeCondition("isDel", "N"));
            if (UtilValidate.areEqual("NEW", brandType)) {
                brandConditions.add(EntityCondition.makeCondition("isHotNewCar", "Y"));
                brandConditions.add(EntityCondition.makeCondition("productCategoryId", "NEW_CAR"));
            } else if (UtilValidate.areEqual("OLD", brandType)) {
                brandConditions.add(EntityCondition.makeCondition("isHotOldCar", "Y"));
                brandConditions.add(EntityCondition.makeCondition("productCategoryId", "USED_CAR"));
            } else if (UtilValidate.areEqual("MODEL", brandType)) {
                brandConditions.add(EntityCondition.makeCondition("isHotCarType", "Y"));
                brandConditions.add(EntityCondition.makeCondition("isCarTypeBrand", "Y"));
            } else if (UtilValidate.areEqual("TYRE", brandType)) {
                brandConditions.add(EntityCondition.makeCondition("productCategoryId", "CHANGE_TYRE"));
            } else if (UtilValidate.areEqual("STORE", brandType)) {
                brandConditions.add(EntityCondition.makeCondition("productCategoryId", "CARGOODS_STORE"));
            }
            //前端设置
            GenericValue frontRule = delegator.findByPrimaryKey("FrontRule", UtilMisc.toMap("frontRuleId", "front_rule"));
            EntityFindOptions findOpts = null;
            if (UtilValidate.isNotEmpty(frontRule) && UtilValidate.isNotEmpty(frontRule.get("hotBrandShowNum"))) {
                findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, frontRule.getLong("hotBrandShowNum").intValue(), true);
            }
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(brandViewEntity, EntityCondition.makeCondition(brandConditions, EntityOperator.AND), null, null, null, findOpts);
            List<GenericValue> productBrands = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(productBrands)) {
                List<Map> brandList = FastList.newInstance();
                for (GenericValue productBrand : productBrands) {
                    Map brandMap = FastMap.newInstance();
                    brandMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productBrand.getString("contentId"));
                    brandMap.put("productBrandId", productBrand.get("productBrandId"));
                    brandMap.put("brandName", productBrand.get("brandName"));
                    brandList.add(brandMap);
                }
                request.setAttribute("brandList", brandList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 首页车型数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBaseCarModel(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        try {
            //热门车型区块
            DynamicViewEntity carModelViewEntity = new DynamicViewEntity();
            carModelViewEntity.addMemberEntity("PCM", "ProductCarModel");
            carModelViewEntity.addAlias("PCM", "productCarModelId");
            carModelViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
            carModelViewEntity.addMemberEntity("P", "Product");
            carModelViewEntity.addAlias("P", "productId");
            carModelViewEntity.addAlias("P", "isOnline");
            carModelViewEntity.addAlias("P", "isVerify");
            carModelViewEntity.addAlias("P", "isDel");
            carModelViewEntity.addAlias("P", "isRecommend");
            carModelViewEntity.addAlias("P", "introductionDate");
            carModelViewEntity.addAlias("P", "salesDiscontinuationDate");
            carModelViewEntity.addAlias("P", "productTypeId");
            carModelViewEntity.addAlias("P", "carTypeId");
            carModelViewEntity.addAlias("P", "mainProductId");
            carModelViewEntity.addAlias("P", "productName");
            carModelViewEntity.addAlias("P", "productSubheadName");
            carModelViewEntity.addViewLink("PCM", "P", false, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
            carModelViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
            carModelViewEntity.addAlias("PSPA", "productStoreId");
            carModelViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            List<EntityCondition> carModelConditions = FastList.newInstance();
            carModelConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
            carModelConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
            carModelConditions.add(EntityCondition.makeCondition("isDel", "N"));
            carModelConditions.add(EntityCondition.makeCondition("isRecommend", "Y"));
            carModelConditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));
            carModelConditions.add(EntityCondition.makeCondition("carTypeId", "NEW_CAR"));
            carModelConditions.add(EntityCondition.makeCondition("mainProductId", null));
            carModelConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            List<EntityCondition> list2 = FastList.newInstance();
            list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
            carModelConditions.add(EntityCondition.makeCondition(list2, EntityOperator.OR));
            carModelConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            carModelConditions.add(EntityCondition.makeCondition("isDel2", "N"));
            Boolean beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(carModelViewEntity, EntityCondition.makeCondition(carModelConditions, EntityOperator.AND), null, null, null,findOpts );
            List<GenericValue> carModels = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(carModels)) {
                List<Map> carModelList = FastList.newInstance();
                for (GenericValue carModel : carModels) {
                    Map modelMap = FastMap.newInstance();
                    modelMap.put("productId", carModel.get("productId"));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", carModel.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        modelMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                    }
//                    modelMap.put("productTypeId", carModel.get("productTypeId"));
                    modelMap.put("productName", carModel.get("productName"));
                    modelMap.put("productSubheadName", carModel.get("productSubheadName"));
                    //销售价
                    List<GenericValue> defaultPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", carModel.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                    if (UtilValidate.isNotEmpty(defaultPriceList)) {
                        BigDecimal productPrice;
                        productPrice = defaultPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        modelMap.put("productPrice", productPrice);
                    }
                    //市场价
                    List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", carModel.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                    if (UtilValidate.isNotEmpty(marketPriceList)) {
                        BigDecimal marketPrice;
                        marketPrice = marketPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        modelMap.put("marketPrice", marketPrice);
                    }
                    //月销量
                    List<EntityCondition> orderConditions = FastList.newInstance();
                    orderConditions.add(EntityCondition.makeCondition("productId", carModel.get("productId")));
                    orderConditions.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.adjustTimestamp(UtilDateTime.nowTimestamp(), Calendar.MONTH, -1)));
                    DynamicViewEntity orderViewEntity = new DynamicViewEntity();
                    orderViewEntity.addMemberEntity("OI", "OrderItem");
                    orderViewEntity.addAlias("OI", "quantity", "quantity", null, false, false, "sum");
                    orderViewEntity.addAlias("OI", "productId");
                    orderViewEntity.addAlias("OI", "createdStamp");
                    beganTransaction = TransactionUtil.begin();
                    
                    eli = delegator.findListIteratorByCondition(orderViewEntity, EntityCondition.makeCondition(orderConditions, EntityOperator.AND), null, UtilMisc.toSet("quantity"), null, findOpts);
                    List<GenericValue> orderItems = eli.getCompleteList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    int saleNum = 0;
                    if (UtilValidate.isNotEmpty(orderItems) && UtilValidate.isNotEmpty(orderItems.get(0).get("quantity"))) {
                        saleNum = orderItems.get(0).getBigDecimal("quantity").intValue();
                    }
                    modelMap.put("saleNum", saleNum);
                    List tagList = FastList.newInstance();
                    //标签
                    List<GenericValue> productTagAssocs = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", carModel.get("productId")));
                    if (UtilValidate.isNotEmpty(productTagAssocs)) {
                        for (GenericValue productTagAssoc : productTagAssocs) {
                            GenericValue tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", productTagAssoc.get("tagId")));
                            if (UtilValidate.isNotEmpty(tag)) {
                                tagList.add(tag.get("tagName"));
                            }
                        }
                    }
                    modelMap.put("tagList", tagList);
                    carModelList.add(modelMap);
                }
                request.setAttribute("carModelList", carModelList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }


    /**
     * 首页猜你喜欢数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBaseBrowse(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            boolean beganTransaction;
            //猜你喜欢
            DynamicViewEntity browseViewEntity = new DynamicViewEntity();
            browseViewEntity.addMemberEntity("PBH", "PartyBrowseHistory");
            browseViewEntity.addAlias("PBH", "partyBrowseHistoryId");
            browseViewEntity.addAlias("PBH", "partyId");
            browseViewEntity.addMemberEntity("PA", "ProductAssoc");
            browseViewEntity.addAlias("PA", "productId", "productIdTo", null, false, true, null);
            browseViewEntity.addAlias("PA", "productAssocTypeId");
            browseViewEntity.addViewLink("PBH", "PA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            browseViewEntity.addMemberEntity("P", "Product");
            browseViewEntity.addAlias("P", "isOnline");
            browseViewEntity.addAlias("P", "isVerify");
            browseViewEntity.addAlias("P", "isDel");
            browseViewEntity.addAlias("P", "introductionDate");
            browseViewEntity.addAlias("P", "salesDiscontinuationDate");
            browseViewEntity.addAlias("P", "productTypeId");
            browseViewEntity.addAlias("P", "carTypeId");
            browseViewEntity.addAlias("P", "recreationType");
            browseViewEntity.addAlias("P", "mainProductId");
            browseViewEntity.addAlias("P", "productName");
            browseViewEntity.addAlias("P", "lastUpdatedStamp");
            browseViewEntity.addViewLink("PA", "P", true, ModelKeyMap.makeKeyMapList("productIdTo", "productId"));
            browseViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
            browseViewEntity.addAlias("PSPA", "productStoreId");
            browseViewEntity.addViewLink("PA", "PSPA", true, ModelKeyMap.makeKeyMapList("productIdTo", "productId"));
            List<EntityCondition> browseConditions = FastList.newInstance();
            browseConditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
            browseConditions.add(EntityCondition.makeCondition("productAssocTypeId", "PRODUCT_CONF_GUESS"));
            browseConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
            browseConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
            browseConditions.add(EntityCondition.makeCondition("isDel", "N"));
            browseConditions.add(EntityCondition.makeCondition("mainProductId", null));
            browseConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            List<EntityCondition> list2 = FastList.newInstance();
            list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
            browseConditions.add(EntityCondition.makeCondition(list2, EntityOperator.OR));
            browseConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            //前端设置
            GenericValue frontRule = delegator.findByPrimaryKey("FrontRule", UtilMisc.toMap("frontRuleId", "front_rule"));
            EntityFindOptions findOpts = null;
            if (UtilValidate.isNotEmpty(frontRule) && UtilValidate.isNotEmpty(frontRule.get("guessLikeShowNum"))) {
                findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, frontRule.getLong("guessLikeShowNum").intValue(), true);
            }
            beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(browseViewEntity, EntityCondition.makeCondition(browseConditions, EntityOperator.AND), null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> partyBrowseHistorys = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(partyBrowseHistorys)) {
                List<Map> likeList = FastList.newInstance();
                for (GenericValue partyBrowseHistory : partyBrowseHistorys) {
                    Map likeMap = FastMap.newInstance();
                    likeMap.put("productId", partyBrowseHistory.get("productId"));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", partyBrowseHistory.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        likeMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                    }
                    if (UtilValidate.areEqual("CAR_GOOD", partyBrowseHistory.get("productTypeId"))) {
                        likeMap.put("productTypeId", partyBrowseHistory.get("carTypeId"));
                    } else if (UtilValidate.areEqual("RECREATION_GOOD", partyBrowseHistory.get("productTypeId"))) {
                        likeMap.put("linkType", partyBrowseHistory.get("recreationType"));
                    } else {
                        likeMap.put("productTypeId", partyBrowseHistory.get("productTypeId"));
                    }
                    likeMap.put("productName", partyBrowseHistory.get("productName"));
                    List<GenericValue> productPriceSaleList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", partyBrowseHistory.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                    if (UtilValidate.isNotEmpty(productPriceSaleList)) {
                        BigDecimal productPrice;
                        if (UtilValidate.areEqual("CAR_GOOD", partyBrowseHistory.get("productTypeId"))) {
                            productPrice = productPriceSaleList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        } else {
                            productPrice = productPriceSaleList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP);
                        }
                        likeMap.put("productPrice", productPrice);
                    }
                    //购买人数
                    DynamicViewEntity orderViewEntity = new DynamicViewEntity();
                    orderViewEntity.addMemberEntity("ORL", "OrderRole");
                    orderViewEntity.addAlias("ORL", "partyId", "partyId", null, false, true, null);
                    orderViewEntity.addMemberEntity("OI", "OrderItem");
                    orderViewEntity.addAlias("OI", "productId");
                    orderViewEntity.addViewLink("ORL", "OI", true, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
                    List<EntityCondition> orderConditions = FastList.newInstance();
                    orderConditions.add(EntityCondition.makeCondition("productId", partyBrowseHistory.get("productId")));
                    beganTransaction = TransactionUtil.begin();
                    eli = delegator.findListIteratorByCondition(orderViewEntity, EntityCondition.makeCondition(orderConditions, EntityOperator.AND), null, null, null, findOpts);
                    int saleNum = eli.getResultsSizeAfterPartialList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    likeMap.put("saleNum", saleNum);
                    List tagList = FastList.newInstance();
                    //标签
                    List<GenericValue> productTagAssocs = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", partyBrowseHistory.get("productId")));
                    if (UtilValidate.isNotEmpty(productTagAssocs)) {
                        for (GenericValue productTagAssoc : productTagAssocs) {
                            GenericValue tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", productTagAssoc.get("tagId")));
                            if (UtilValidate.isNotEmpty(tag)) {
                                tagList.add(tag.get("tagName"));
                            }
                        }
                    }
                    likeMap.put("tagList", tagList);
                    likeList.add(likeMap);
                }
                request.setAttribute("likeList", likeList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 未登录状态查询前端缓存商品
     *
     * @param request
     * @param response
     * @return
     */
    public static String getPartyBrowse(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String productIds = request.getParameter("productIds");
        if (UtilValidate.isEmpty(productIds) && UtilValidate.isNotEmpty(jsonObject.get("productIds"))) {
            productIds = jsonObject.getString("productIds");
        }
        if (UtilValidate.isEmpty(productIds)) {
            request.setAttribute("error", "商品ID不能为空");
            response.setStatus(403);
            return "error";
        }

        try {
            boolean beganTransaction;
            //猜你喜欢
            DynamicViewEntity browseViewEntity = new DynamicViewEntity();
            browseViewEntity.addMemberEntity("PA", "ProductAssoc");
            browseViewEntity.addAlias("PA", "originalProductId", "productId", null, false, false, null);
            browseViewEntity.addAlias("PA", "productId", "productIdTo", null, false, true, null);
            browseViewEntity.addAlias("PA", "productAssocTypeId");
            browseViewEntity.addMemberEntity("P", "Product");
            browseViewEntity.addAlias("P", "isOnline");
            browseViewEntity.addAlias("P", "isVerify");
            browseViewEntity.addAlias("P", "isDel");
            browseViewEntity.addAlias("P", "introductionDate");
            browseViewEntity.addAlias("P", "salesDiscontinuationDate");
            browseViewEntity.addAlias("P", "productTypeId");
            browseViewEntity.addAlias("P", "carTypeId");
            browseViewEntity.addAlias("P", "mainProductId");
            browseViewEntity.addAlias("P", "productName");
            browseViewEntity.addAlias("P", "productSubheadName");
            browseViewEntity.addAlias("P", "frontMoney");
            browseViewEntity.addAlias("P", "ratio");
            browseViewEntity.addAlias("P", "lastUpdatedStamp");
            browseViewEntity.addViewLink("PA", "P", true, ModelKeyMap.makeKeyMapList("productIdTo", "productId"));
            browseViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
            browseViewEntity.addAlias("PSPA", "productStoreId");
            browseViewEntity.addViewLink("PA", "PSPA", true, ModelKeyMap.makeKeyMapList("productIdTo", "productId"));
            List<EntityCondition> browseConditions = FastList.newInstance();
            browseConditions.add(EntityCondition.makeCondition("originalProductId", EntityOperator.IN, UtilMisc.toListArray(productIds.split(","))));
            browseConditions.add(EntityCondition.makeCondition("productAssocTypeId", "PRODUCT_CONF_GUESS"));
            browseConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
            browseConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
            browseConditions.add(EntityCondition.makeCondition("isDel", "N"));
            browseConditions.add(EntityCondition.makeCondition("mainProductId", null));
            browseConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            List<EntityCondition> list2 = FastList.newInstance();
            list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
            browseConditions.add(EntityCondition.makeCondition(list2, EntityOperator.OR));
            browseConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(browseViewEntity, EntityCondition.makeCondition(browseConditions, EntityOperator.AND), null, null, UtilMisc.toList("lastUpdatedStamp"), null);
            List<GenericValue> partyBrowseHistorys = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(partyBrowseHistorys)) {
                List<Map> likeList = FastList.newInstance();
                for (GenericValue partyBrowseHistory : partyBrowseHistorys) {
                    Map likeMap = FastMap.newInstance();
                    likeMap.put("productId", partyBrowseHistory.get("productId"));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", partyBrowseHistory.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        likeMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                    }
                    if (UtilValidate.areEqual("CAR_GOOD", partyBrowseHistory.get("productTypeId"))) {
                        likeMap.put("productTypeId", partyBrowseHistory.get("carTypeId"));
                    } else if (UtilValidate.areEqual("RECREATION_GOOD", partyBrowseHistory.get("productTypeId"))) {
                        likeMap.put("linkType", partyBrowseHistory.get("recreationType"));
                    } else {
                        likeMap.put("productTypeId", partyBrowseHistory.get("productTypeId"));
                    }
                    likeMap.put("productName", partyBrowseHistory.get("productName"));
                    List<GenericValue> productPriceSaleList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", partyBrowseHistory.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                    if (UtilValidate.isNotEmpty(productPriceSaleList)) {
                        BigDecimal productPrice;
                        if (UtilValidate.areEqual("CAR_GOOD", partyBrowseHistory.get("productTypeId"))) {
                            productPrice = productPriceSaleList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        } else {
                            productPrice = productPriceSaleList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP);
                        }
                        likeMap.put("productPrice", productPrice);
                    }
                    //购买人数
                    DynamicViewEntity orderViewEntity = new DynamicViewEntity();
                    orderViewEntity.addMemberEntity("ORL", "OrderRole");
                    orderViewEntity.addAlias("ORL", "partyId", "partyId", null, false, true, null);
                    orderViewEntity.addMemberEntity("OI", "OrderItem");
                    orderViewEntity.addAlias("OI", "productId");
                    orderViewEntity.addViewLink("ORL", "OI", true, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
                    List<EntityCondition> orderConditions = FastList.newInstance();
                    orderConditions.add(EntityCondition.makeCondition("productId", partyBrowseHistory.get("productId")));
                    beganTransaction = TransactionUtil.begin();
                    EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                            EntityFindOptions.CONCUR_READ_ONLY,   true);
                    eli = delegator.findListIteratorByCondition(orderViewEntity, EntityCondition.makeCondition(orderConditions, EntityOperator.AND), null, null, null, findOpts);
                    int saleNum = eli.getResultsSizeAfterPartialList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    likeMap.put("saleNum", saleNum);
                    List tagList = FastList.newInstance();
                    //标签
                    List<GenericValue> productTagAssocs = delegator.findByAnd("ProductTagAssoc", UtilMisc.toMap("productId", partyBrowseHistory.get("productId")));
                    if (UtilValidate.isNotEmpty(productTagAssocs)) {
                        for (GenericValue productTagAssoc : productTagAssocs) {
                            GenericValue tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", productTagAssoc.get("tagId")));
                            if (UtilValidate.isNotEmpty(tag)) {
                                tagList.add(tag.get("tagName"));
                            }
                        }
                    }
                    likeMap.put("tagList", tagList);
                    likeList.add(likeMap);
                    //副标题
                    likeMap.put("productSubheadName", partyBrowseHistory.get("productSubheadName"));
                    //销售价
                    List<GenericValue> defaultPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", partyBrowseHistory.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                    //市场价
                    List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", partyBrowseHistory.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                    if (UtilValidate.isNotEmpty(marketPriceList)) {
                        BigDecimal marketPrice;
                        marketPrice = marketPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        likeMap.put("marketPrice", marketPrice);
                    }
                    //定金
                    BigDecimal frontMoney;
                    BigDecimal ratio = UtilValidate.isNotEmpty(partyBrowseHistory.getBigDecimal("ratio")) ? partyBrowseHistory.getBigDecimal("ratio") : BigDecimal.ZERO;
                    if (UtilValidate.isNotEmpty(partyBrowseHistory.get("frontMoney"))) {
                        frontMoney = partyBrowseHistory.getBigDecimal("frontMoney").setScale(2, BigDecimal.ROUND_HALF_UP);
                    } else {
                        frontMoney = defaultPriceList.get(0).getBigDecimal("price").multiply(ratio).divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    likeMap.put("frontMoney", frontMoney);
                    //销量
                    GenericValue productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", partyBrowseHistory.get("productId")));
                    int saleNumPC = 0;
                    if (UtilValidate.isNotEmpty(productCalculatedInfo) && UtilValidate.isNotEmpty(productCalculatedInfo.get("totalQuantityOrdered"))) {
                        saleNumPC = productCalculatedInfo.getBigDecimal("totalQuantityOrdered").intValue();
                    }
                    likeMap.put("saleNumPC", saleNumPC);
                }
                request.setAttribute("likeList", likeList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取所有店铺
     *
     * @param request
     * @param response
     * @return
     */
    public static String getAllProductStore(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> productStores = delegator.findByAnd("ProductStore", UtilMisc.toMap("isEnabled", "Y"));
        List<Map> productStoreList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(productStores)) {
            for (GenericValue productStore : productStores) {
                Map storeMap = FastMap.newInstance();
                storeMap.put("productStoreId", productStore.get("productStoreId"));
                storeMap.put("storeName", productStore.get("storeName"));
                String productStoreAddress = "";
                if (UtilValidate.isNotEmpty(productStore.get("stateProvinceGeoId"))) {
                    GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("stateProvinceGeoId")));
                    productStoreAddress += geo.getString("geoName");
                }
                if (UtilValidate.isNotEmpty(productStore.get("cityGeoId"))) {
                    GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("cityGeoId")));
                    productStoreAddress += geo.getString("geoName");
                }
                if (UtilValidate.isNotEmpty(productStore.get("countyGeoId"))) {
                    GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("countyGeoId")));
                    productStoreAddress += geo.getString("geoName");
                }
                if (UtilValidate.isNotEmpty(productStore.get("address"))) {
                    productStoreAddress += productStore.getString("address");
                }
                storeMap.put("productStoreAddress", productStoreAddress);
                productStoreList.add(storeMap);
            }
        }
        request.setAttribute("productStoreList", productStoreList);
        return "success";
    }

    /**
     * 获取所有分类
     *
     * @param request
     * @param response
     * @return
     */
    public static String getAllCategory(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONArray jsonArray = new JSONArray();
        try {
            List<GenericValue> firstCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryLevel", 1L));
            if (UtilValidate.isNotEmpty(firstCategorys)) {
                for (GenericValue firstCategory : firstCategorys) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("productCategoryId", firstCategory.get("productCategoryId"));
                    jsonObject.put("categoryName", firstCategory.get("categoryName"));
                    jsonObject.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + firstCategory.getString("contentId"));
                    JSONArray jsonArray1 = new JSONArray();
                    List<GenericValue> secondCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", firstCategory.get("productCategoryId")));
                    if (UtilValidate.isNotEmpty(secondCategorys)) {
                        for (GenericValue secondCategory : secondCategorys) {
                            JSONObject jsonObject1 = new JSONObject();
                            jsonObject1.put("productCategoryId", secondCategory.get("productCategoryId"));
                            jsonObject1.put("categoryName", secondCategory.get("categoryName"));
                            jsonObject1.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + secondCategory.getString("contentId"));
                            jsonArray1.add(jsonObject1);
                        }
                    }
                    jsonObject.put("categoryList", jsonArray1);
                    jsonArray.add(jsonObject);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("categoryList", jsonArray);
        return "success";
    }

    /**
     * 获取用户设置
     *
     * @param request
     * @param response
     * @return
     */
    public static String getPartyConfigInfo(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            if (UtilValidate.areEqual("N", party.get("allowNotice"))) {
                request.setAttribute("allowNotice", "N");
            } else {
                request.setAttribute("allowNotice", "Y");
            }
            //TODO:最新版本号
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 编辑用户设置
     *
     * @param request
     * @param response
     * @return
     */
    public static String editPartyConfigInfo(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String allowNotice = request.getParameter("allowNotice");
        if (UtilValidate.isEmpty(allowNotice) && UtilValidate.isNotEmpty(jsonObject.get("allowNotice"))) {
            allowNotice = jsonObject.getString("allowNotice");
        }
        if (UtilValidate.isEmpty(allowNotice)) {
            request.setAttribute("error", "请选择是否允许推送通知");
            response.setStatus(403);
            return "error";
        }
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            party.set("allowNotice", allowNotice);
            delegator.store(party);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取当前店铺下新车数量
     *
     * @param request
     * @param response
     * @return
     */
    public static String getNewCarNum(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("P", "Product");
        dynamicViewEntity.addAlias("P", "isOnline");
        dynamicViewEntity.addAlias("P", "isVerify");
        dynamicViewEntity.addAlias("P", "isDel");
        dynamicViewEntity.addAlias("P", "carTypeId");
        dynamicViewEntity.addAlias("P", "mainProductId");
        dynamicViewEntity.addAlias("P", "introductionDate");
        dynamicViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewEntity.addMemberEntity("PCM", "ProductCarModel");
        dynamicViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        dynamicViewEntity.addViewLink("P", "PCM", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        conditions.add(EntityCondition.makeCondition("carTypeId", "NEW_CAR"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list2 = FastList.newInstance();
        list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list2, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, findOpts);
            int num = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            request.setAttribute("num", num);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取品牌数据
     *
     * @param request
     * @param response
     * @return
     */
    public static String getBrandList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String brandType = request.getParameter("brandType");
        if (UtilValidate.isEmpty(brandType) && UtilValidate.isNotEmpty(jsonObject.get("brandType"))) {
            brandType = jsonObject.getString("brandType");
        }
        if (UtilValidate.isEmpty(brandType)) {
            request.setAttribute("error", "品牌分类不能为空");
            response.setStatus(403);
            return "error";
        }

        Delegator delegator = (Delegator) request.getAttribute("delegator");
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("PB", "ProductBrand");
        dynamicViewEntity.addAlias("PB", "headChar", "headChar", null, false, true, null);
        dynamicViewEntity.addAlias("PB", "isCarTypeBrand");
        dynamicViewEntity.addAlias("PB", "isUsed");
        dynamicViewEntity.addAlias("PB", "isDel");
        dynamicViewEntity.addMemberEntity("PBC", "ProductBrandCategory");
        dynamicViewEntity.addAlias("PBC", "productCategoryId");
        dynamicViewEntity.addViewLink("PB", "PBC", true, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));
        List<EntityCondition> brandConditions = FastList.newInstance();
        brandConditions.add(EntityCondition.makeCondition("isUsed", "Y"));
        brandConditions.add(EntityCondition.makeCondition("isDel", "N"));
        if (UtilValidate.areEqual("NEW", brandType)) {
            brandConditions.add(EntityCondition.makeCondition("productCategoryId", "NEW_CAR"));
        } else if (UtilValidate.areEqual("OLD", brandType)) {
            brandConditions.add(EntityCondition.makeCondition("productCategoryId", "USED_CAR"));
        } else if (UtilValidate.areEqual("IMPORTED", brandType)) {
            brandConditions.add(EntityCondition.makeCondition("productCategoryId", "IMPORTED_CAR"));
        } else if (UtilValidate.areEqual("MODEL", brandType)) {
            brandConditions.add(EntityCondition.makeCondition("isCarTypeBrand", "Y"));
        } else if (UtilValidate.areEqual("TYRE", brandType)) {
            brandConditions.add(EntityCondition.makeCondition("productCategoryId", "CHANGE_TYRE"));
        } else if (UtilValidate.areEqual("STORE", brandType)) {
            brandConditions.add(EntityCondition.makeCondition("productCategoryId", "CARGOODS_STORE"));
        }
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewEntity, EntityCondition.makeCondition(brandConditions, EntityOperator.AND), null, null, null, findOpts);
            List<GenericValue> headChars = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> brandList = FastList.newInstance();
            for (GenericValue headChar : headChars) {
                Map brandMap = FastMap.newInstance();
                brandMap.put("groupName", headChar.get("headChar"));
                DynamicViewEntity brandViewEntity = new DynamicViewEntity();
                brandViewEntity.addMemberEntity("PB", "ProductBrand");
                brandViewEntity.addAlias("PB", "productBrandId", "productBrandId", null, false, true, null);
                brandViewEntity.addAlias("PB", "brandName");
                brandViewEntity.addAlias("PB", "imgUrl");
                brandViewEntity.addAlias("PB", "headChar");
                brandViewEntity.addAlias("PB", "headString");
                brandViewEntity.addAlias("PB", "contentId");
                brandViewEntity.addAlias("PB", "isCarTypeBrand");
                brandViewEntity.addAlias("PB", "isUsed");
                brandViewEntity.addAlias("PB", "isDel");
                brandViewEntity.addMemberEntity("PBC", "ProductBrandCategory");
                brandViewEntity.addAlias("PBC", "productCategoryId");
                brandViewEntity.addViewLink("PB", "PBC", true, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));
                List<EntityCondition> conditions = FastList.newInstance();
                conditions.add(EntityCondition.makeCondition("headChar", headChar.get("headChar")));
                conditions.add(EntityCondition.makeCondition("isUsed", "Y"));
                conditions.add(EntityCondition.makeCondition("isDel", "N"));
                if (UtilValidate.areEqual("NEW", brandType)) {
                    conditions.add(EntityCondition.makeCondition("productCategoryId", "NEW_CAR"));
                } else if (UtilValidate.areEqual("OLD", brandType)) {
                    conditions.add(EntityCondition.makeCondition("productCategoryId", "USED_CAR"));
                } else if (UtilValidate.areEqual("IMPORTED", brandType)) {
                    conditions.add(EntityCondition.makeCondition("productCategoryId", "IMPORTED_CAR"));
                } else if (UtilValidate.areEqual("MODEL", brandType)) {
                    conditions.add(EntityCondition.makeCondition("isCarTypeBrand", "Y"));
                } else if (UtilValidate.areEqual("TYRE", brandType)) {
                    brandConditions.add(EntityCondition.makeCondition("productCategoryId", "CHANGE_TYRE"));
                } else if (UtilValidate.areEqual("STORE", brandType)) {
                    brandConditions.add(EntityCondition.makeCondition("productCategoryId", "CARGOODS_STORE"));
                }
                beganTransaction = TransactionUtil.begin();
                
                eli = delegator.findListIteratorByCondition(brandViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("headString"), findOpts);
                List<GenericValue> productBrands = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                List<Map> groupList = FastList.newInstance();
                for (GenericValue productBrand : productBrands) {
                    Map groupMap = FastMap.newInstance();
                    groupMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productBrand.getString("contentId"));
                    groupMap.put("productBrandId", productBrand.get("productBrandId"));
                    groupMap.put("brandName", productBrand.get("brandName"));
                    groupList.add(groupMap);
                }
                brandMap.put("groupList", groupList);
                brandList.add(brandMap);
            }
            request.setAttribute("brandList", brandList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取车型库条件
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarCondition(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            //级别
            List<GenericValue> carLevels = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_LEVEL", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray levelArray = new JSONArray();
            for (GenericValue carLevel : carLevels) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carLevel.get("enumId"));
                jsonObject.put("description", carLevel.get("description"));
                levelArray.add(jsonObject);
            }
            request.setAttribute("levelArray", levelArray);
            //国别
            List<GenericValue> carCountrys = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_COUNTRY", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray countryArray = new JSONArray();
            for (GenericValue carCountry : carCountrys) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carCountry.get("enumId"));
                jsonObject.put("description", carCountry.get("description"));
                countryArray.add(jsonObject);
            }
            request.setAttribute("countryArray", countryArray);
            //变速箱
            List<GenericValue> carTransmissions = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_TRANSMISSION", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray transmissionArray = new JSONArray();
            for (GenericValue carTransmission : carTransmissions) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carTransmission.get("enumId"));
                jsonObject.put("description", carTransmission.get("description"));
                transmissionArray.add(jsonObject);
            }
            request.setAttribute("transmissionArray", transmissionArray);
            //能源
            List<GenericValue> carFuels = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_FUEl", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray energyArray = new JSONArray();
            for (GenericValue carFuel : carFuels) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carFuel.get("enumId"));
                jsonObject.put("description", carFuel.get("description"));
                energyArray.add(jsonObject);
            }
            request.setAttribute("energyArray", energyArray);
            //排量
            List<GenericValue> carDisplacements = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_DISPLACEMENT", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray displacementArray = new JSONArray();
            for (GenericValue carDisplacement : carDisplacements) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carDisplacement.get("enumId"));
                jsonObject.put("description", carDisplacement.get("description"));
                displacementArray.add(jsonObject);
            }
            request.setAttribute("displacementArray", displacementArray);
            //座位数
            List<GenericValue> carSeats = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_SEAT", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray seatArray = new JSONArray();
            for (GenericValue carSeat : carSeats) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carSeat.get("enumId"));
                jsonObject.put("description", carSeat.get("description"));
                seatArray.add(jsonObject);
            }
            request.setAttribute("seatArray", seatArray);
            //结构
            List<GenericValue> carStructures = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_STRUCTURE", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray structureArray = new JSONArray();
            for (GenericValue carStructure : carStructures) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carStructure.get("enumId"));
                jsonObject.put("description", carStructure.get("description"));
                structureArray.add(jsonObject);
            }
            request.setAttribute("structureArray", structureArray);
            //生产厂商
            List<GenericValue> carMfrs = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_MFRS", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray mfrArray = new JSONArray();
            for (GenericValue carMfr : carMfrs) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carMfr.get("enumId"));
                jsonObject.put("description", carMfr.get("description"));
                mfrArray.add(jsonObject);
            }
            request.setAttribute("mfrArray", mfrArray);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 根据车型库条件查询新车
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarByCondition(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        //最低价格
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        //品牌编号
        String productBrandIds = request.getParameter("productBrandIds");
        if (UtilValidate.isEmpty(productBrandIds) && UtilValidate.isNotEmpty(jsonObject.get("productBrandIds"))) {
            productBrandIds = jsonObject.getString("productBrandIds");
        }
        //级别编号
        String carLevelIds = request.getParameter("carLevelIds");
        if (UtilValidate.isEmpty(carLevelIds) && UtilValidate.isNotEmpty(jsonObject.get("carLevelIds"))) {
            carLevelIds = jsonObject.getString("carLevelIds");
        }
        //国别编号
        String carCountryIds = request.getParameter("carCountryIds");
        if (UtilValidate.isEmpty(carCountryIds) && UtilValidate.isNotEmpty(jsonObject.get("carCountryIds"))) {
            carCountryIds = jsonObject.getString("carCountryIds");
        }
        //变速箱
        String transmissionTypes = request.getParameter("transmissionTypes");
        if (UtilValidate.isEmpty(transmissionTypes) && UtilValidate.isNotEmpty(jsonObject.get("transmissionTypes"))) {
            transmissionTypes = jsonObject.getString("transmissionTypes");
        }
        //能源
        String carFuelIds = request.getParameter("carFuelIds");
        if (UtilValidate.isEmpty(carFuelIds) && UtilValidate.isNotEmpty(jsonObject.get("carFuelIds"))) {
            carFuelIds = jsonObject.getString("carFuelIds");
        }
        //排量
        String carDisplacementIds = request.getParameter("carDisplacementIds");
        if (UtilValidate.isEmpty(carDisplacementIds) && UtilValidate.isNotEmpty(jsonObject.get("carDisplacementIds"))) {
            carDisplacementIds = jsonObject.getString("carDisplacementIds");
        }
        //座位
        String carSeatIds = request.getParameter("carSeatIds");
        if (UtilValidate.isEmpty(carSeatIds) && UtilValidate.isNotEmpty(jsonObject.get("carSeatIds"))) {
            carSeatIds = jsonObject.getString("carSeatIds");
        }
        //结构
        String carStructureIds = request.getParameter("carStructureIds");
        if (UtilValidate.isEmpty(carStructureIds) && UtilValidate.isNotEmpty(jsonObject.get("carStructureIds"))) {
            carStructureIds = jsonObject.getString("carStructureIds");
        }
        //制造商
        String productionModels = request.getParameter("productionModels");
        if (UtilValidate.isEmpty(productionModels) && UtilValidate.isNotEmpty(jsonObject.get("productionModels"))) {
            productionModels = jsonObject.getString("productionModels");
        }
        //关键字
        String keyWord = request.getParameter("keyWord");
        if (UtilValidate.isEmpty(keyWord) && UtilValidate.isNotEmpty(jsonObject.get("keyWord"))) {
            keyWord = jsonObject.getString("keyWord");
        }

        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        //车型视图
        DynamicViewEntity carModelViewViewEntity = new DynamicViewEntity();
        carModelViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        carModelViewViewEntity.addAlias("PCM", "productCarModelId", "productCarModelId", null, false, true, null);
        carModelViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        carModelViewViewEntity.addAlias("PCM", "productionModel");//制造商
        carModelViewViewEntity.addAlias("PCM", "carStructureId");//结构
        carModelViewViewEntity.addAlias("PCM", "carSeatId");//座位数
        carModelViewViewEntity.addAlias("PCM", "carDisplacementId");//排量
        carModelViewViewEntity.addAlias("PCM", "carFuelId");//能源
        carModelViewViewEntity.addAlias("PCM", "transmissionType");//变速箱
        carModelViewViewEntity.addAlias("PCM", "carModelName");//车型名称
        carModelViewViewEntity.addAlias("PCM", "carDisplacementName");//排量名称
        carModelViewViewEntity.addAlias("PCM", "carFuelName");//燃料名称
        carModelViewViewEntity.addAlias("PCM", "carStructureName");//结构名称
        carModelViewViewEntity.addAlias("PCM", "carDriverName");//驱动名称
        carModelViewViewEntity.addAlias("PCM", "carSeatName");//座位名称
        carModelViewViewEntity.addAlias("PCM", "productionModelName");//制造商名称
        carModelViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        carModelViewViewEntity.addAlias("PCS", "productCarSeriesId");
        carModelViewViewEntity.addAlias("PCS", "isDel1", "isDel", null, false, false, null);
        carModelViewViewEntity.addAlias("PCS", "carCountryId");//国别
        carModelViewViewEntity.addAlias("PCS", "carLevelId");//级别
        carModelViewViewEntity.addAlias("PCS", "productBrandId");//品牌
        carModelViewViewEntity.addAlias("PCS", "carSeriesName");//车系名称
        carModelViewViewEntity.addAlias("PCS", "productBrandName");//品牌名称
        carModelViewViewEntity.addAlias("PCS", "carCountryName");//国别名称
        carModelViewViewEntity.addAlias("PCS", "carLevelName");//级别名称
        carModelViewViewEntity.addViewLink("PCM", "PCS", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        carModelViewViewEntity.addMemberEntity("P", "Product");
        carModelViewViewEntity.addAlias("P", "productId");
        carModelViewViewEntity.addAlias("P", "isOnline");
        carModelViewViewEntity.addAlias("P", "isVerify");
        carModelViewViewEntity.addAlias("P", "isDel");
        carModelViewViewEntity.addAlias("P", "carTypeId");
        carModelViewViewEntity.addAlias("P", "productTypeId");
        carModelViewViewEntity.addAlias("P", "mainProductId");
        carModelViewViewEntity.addAlias("P", "introductionDate");
        carModelViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        carModelViewViewEntity.addViewLink("PCM", "P", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        carModelViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        carModelViewViewEntity.addAlias("PSPA", "productStoreId");
        carModelViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        carModelViewViewEntity.addMemberEntity("PP", "ProductPrice");
        carModelViewViewEntity.addAlias("PP", "productPriceTypeId");
        carModelViewViewEntity.addAlias("PP", "price");
        carModelViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));//默认价格
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));//是否上架
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));//是否审核
        conditions.add(EntityCondition.makeCondition("isDel", "N"));//是否删除
        conditions.add(EntityCondition.makeCondition("carTypeId", "NEW_CAR"));//车辆类别
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));//商品类别
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售开始日期
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售结束日期
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));//店铺编号
        conditions.add(EntityCondition.makeCondition("isDel1", "N"));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        if (UtilValidate.isNotEmpty(productBrandIds)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", EntityOperator.IN, UtilMisc.toListArray(productBrandIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carLevelIds)) {
            conditions.add(EntityCondition.makeCondition("carLevelId", EntityOperator.IN, UtilMisc.toListArray(carLevelIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carCountryIds)) {
            conditions.add(EntityCondition.makeCondition("carCountryId", EntityOperator.IN, UtilMisc.toListArray(carCountryIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carFuelIds)) {
            conditions.add(EntityCondition.makeCondition("carFuelId", EntityOperator.IN, UtilMisc.toListArray(carFuelIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(transmissionTypes)) {
            conditions.add(EntityCondition.makeCondition("transmissionType", EntityOperator.IN, UtilMisc.toListArray(transmissionTypes.split(","))));
        }
        if (UtilValidate.isNotEmpty(carDisplacementIds)) {
            conditions.add(EntityCondition.makeCondition("carDisplacementId", EntityOperator.IN, UtilMisc.toListArray(carDisplacementIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carSeatIds)) {
            conditions.add(EntityCondition.makeCondition("carSeatId", EntityOperator.IN, UtilMisc.toListArray(carSeatIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carStructureIds)) {
            conditions.add(EntityCondition.makeCondition("carStructureId", EntityOperator.IN, UtilMisc.toListArray(carStructureIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(productionModels)) {
            conditions.add(EntityCondition.makeCondition("productionModel", EntityOperator.IN, UtilMisc.toListArray(productionModels.split(","))));
        }
        if (UtilValidate.isNotEmpty(keyWord)) {
            List<EntityCondition> keyWordConditions = FastList.newInstance();
            keyWordConditions.add(EntityCondition.makeCondition("carModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDisplacementName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carFuelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carStructureName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDriverName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeatName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productionModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeriesName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productBrandName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carCountryName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carLevelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            conditions.add(EntityCondition.makeCondition(keyWordConditions, EntityOperator.OR));
        }
        List<String> carModelToSelect = FastList.newInstance();
        carModelToSelect.add("productCarModelId");
        carModelToSelect.add("price");
        //车系视图
        DynamicViewEntity carSeriesViewViewEntity = new DynamicViewEntity();
        carSeriesViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        carSeriesViewViewEntity.addAlias("PCS", "productCarSeriesId", "productCarSeriesId", null, false, true, null);
        carSeriesViewViewEntity.addAlias("PCS", "isDel1", "isDel", null, false, false, null);
        carSeriesViewViewEntity.addAlias("PCS", "carSeriesName");
        carSeriesViewViewEntity.addAlias("PCS", "carCountryId");//国别
        carSeriesViewViewEntity.addAlias("PCS", "carLevelId");//级别
        carSeriesViewViewEntity.addAlias("PCS", "productBrandId");//品牌
        carSeriesViewViewEntity.addAlias("PCS", "productBrandName");//品牌名称
        carSeriesViewViewEntity.addAlias("PCS", "carCountryName");//国别名称
        carSeriesViewViewEntity.addAlias("PCS", "carLevelName");//级别名称
        carSeriesViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        carSeriesViewViewEntity.addAlias("PCM", "productCarModelId");
        carSeriesViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        carSeriesViewViewEntity.addAlias("PCM", "productionModel");//制造商
        carSeriesViewViewEntity.addAlias("PCM", "carStructureId");//结构
        carSeriesViewViewEntity.addAlias("PCM", "carSeatId");//座位数
        carSeriesViewViewEntity.addAlias("PCM", "carDisplacementId");//排量
        carSeriesViewViewEntity.addAlias("PCM", "carFuelId");//能源
        carSeriesViewViewEntity.addAlias("PCM", "transmissionType");//变速箱
        carSeriesViewViewEntity.addAlias("PCM", "carModelName");//车型名称
        carSeriesViewViewEntity.addAlias("PCM", "carDisplacementName");//排量名称
        carSeriesViewViewEntity.addAlias("PCM", "carFuelName");//燃料名称
        carSeriesViewViewEntity.addAlias("PCM", "carStructureName");//结构名称
        carSeriesViewViewEntity.addAlias("PCM", "carDriverName");//驱动名称
        carSeriesViewViewEntity.addAlias("PCM", "carSeatName");//座位名称
        carSeriesViewViewEntity.addAlias("PCM", "productionModelName");//制造商名称
        carSeriesViewViewEntity.addViewLink("PCS", "PCM", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        carSeriesViewViewEntity.addMemberEntity("P", "Product");
        carSeriesViewViewEntity.addAlias("P", "productId");
        carSeriesViewViewEntity.addAlias("P", "isOnline");
        carSeriesViewViewEntity.addAlias("P", "isVerify");
        carSeriesViewViewEntity.addAlias("P", "isDel");
        carSeriesViewViewEntity.addAlias("P", "carTypeId");
        carSeriesViewViewEntity.addAlias("P", "productTypeId");
        carSeriesViewViewEntity.addAlias("P", "mainProductId");
        carSeriesViewViewEntity.addAlias("P", "introductionDate");
        carSeriesViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        carSeriesViewViewEntity.addViewLink("PCM", "P", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        carSeriesViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        carSeriesViewViewEntity.addAlias("PSPA", "productStoreId");
        carSeriesViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        carSeriesViewViewEntity.addMemberEntity("PP", "ProductPrice");
        carSeriesViewViewEntity.addAlias("PP", "productPriceTypeId");
        carSeriesViewViewEntity.addAlias("PP", "price");
        carSeriesViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        EntityFindOptions carSeriesFindOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        List<String> carSeriesToSelect = FastList.newInstance();
        carSeriesToSelect.add("productCarSeriesId");
        carSeriesToSelect.add("carSeriesName");
        carSeriesToSelect.add("carLevelId");
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            //车量商品数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator eli = delegator.findListIteratorByCondition(carModelViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, carModelToSelect, null, findOpts);
            int carModelNum = eli.getResultsSizeAfterPartialList();
            request.setAttribute("carModelNum", carModelNum);
            //车系数据
            eli = delegator.findListIteratorByCondition(carSeriesViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, carSeriesToSelect, null, carSeriesFindOpts);
            List<GenericValue> carSeriess = eli.getPartialList(lowIndex, highIndex);
            int carSeriesNum = eli.getResultsSizeAfterPartialList();
            request.setAttribute("carSeriesNum", carSeriesNum);
            List<Map> carSeriesList = FastList.newInstance();
            EntityFindOptions carModelOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
            for (GenericValue carSeries : carSeriess) {
                Map carSeriesMap = FastMap.newInstance();
                carSeriesMap.put("productCarSeriesId", carSeries.get("productCarSeriesId"));
                carSeriesMap.put("carSeriesName", carSeries.get("carSeriesName"));
                GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", carSeries.get("carLevelId")));
                carSeriesMap.put("carLevelId", enumeration.get("description"));
                List<GenericValue> productCarSeriesContents = delegator.findByAnd("ProductCarSeriesContent", UtilMisc.toMap("productCarSeriesId", carSeries.get("productCarSeriesId")));
                if (UtilValidate.isNotEmpty(productCarSeriesContents)) {
                    carSeriesMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productCarSeriesContents.get(0).get("contentId"));
                }
                conditions.add(EntityCondition.makeCondition("productCarSeriesId", carSeries.get("productCarSeriesId")));
                //该车系下车型数量
                
                eli = delegator.findListIteratorByCondition(carModelViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, carModelToSelect, null, findOpts);
                int modelNum = eli.getResultsSizeAfterPartialList();
                carSeriesMap.put("carModelNum", modelNum);
                //该车系下车型最低价
                eli = delegator.findListIteratorByCondition(carModelViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, carModelToSelect, UtilMisc.toList("price"), carModelOpts);
                List<GenericValue> priceLowModel = eli.getCompleteList();
                if (UtilValidate.isNotEmpty(priceLowModel)) {
                    carSeriesMap.put("priceLow", priceLowModel.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                //该车系下车型最高价
                eli = delegator.findListIteratorByCondition(carModelViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, carModelToSelect, UtilMisc.toList("-price"), carModelOpts);
                priceLowModel = eli.getCompleteList();
                if (UtilValidate.isNotEmpty(priceLowModel)) {
                    carSeriesMap.put("priceHigh", priceLowModel.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                conditions.remove(EntityCondition.makeCondition("productCarSeriesId", carSeries.get("productCarSeriesId")));
                carSeriesList.add(carSeriesMap);
            }
            request.setAttribute("carSeriesList", carSeriesList);
            eli.close();
            TransactionUtil.commit(beganTransaction);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 车系详情
     *
     * @param request
     * @param response
     * @return
     */
    public static String carSeriesDetail(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        //最低价格
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        //品牌编号
        String productBrandIds = request.getParameter("productBrandIds");
        if (UtilValidate.isEmpty(productBrandIds) && UtilValidate.isNotEmpty(jsonObject.get("productBrandIds"))) {
            productBrandIds = jsonObject.getString("productBrandIds");
        }
        //级别编号
        String carLevelIds = request.getParameter("carLevelIds");
        if (UtilValidate.isEmpty(carLevelIds) && UtilValidate.isNotEmpty(jsonObject.get("carLevelIds"))) {
            carLevelIds = jsonObject.getString("carLevelIds");
        }
        //国别编号
        String carCountryIds = request.getParameter("carCountryIds");
        if (UtilValidate.isEmpty(carCountryIds) && UtilValidate.isNotEmpty(jsonObject.get("carCountryIds"))) {
            carCountryIds = jsonObject.getString("carCountryIds");
        }
        //变速箱
        String transmissionTypes = request.getParameter("transmissionTypes");
        if (UtilValidate.isEmpty(transmissionTypes) && UtilValidate.isNotEmpty(jsonObject.get("transmissionTypes"))) {
            transmissionTypes = jsonObject.getString("transmissionTypes");
        }
        //能源
        String carFuelIds = request.getParameter("carFuelIds");
        if (UtilValidate.isEmpty(carFuelIds) && UtilValidate.isNotEmpty(jsonObject.get("carFuelIds"))) {
            carFuelIds = jsonObject.getString("carFuelIds");
        }
        //排量
        String carDisplacementIds = request.getParameter("carDisplacementIds");
        if (UtilValidate.isEmpty(carDisplacementIds) && UtilValidate.isNotEmpty(jsonObject.get("carDisplacementIds"))) {
            carDisplacementIds = jsonObject.getString("carDisplacementIds");
        }
        //座位
        String carSeatIds = request.getParameter("carSeatIds");
        if (UtilValidate.isEmpty(carSeatIds) && UtilValidate.isNotEmpty(jsonObject.get("carSeatIds"))) {
            carSeatIds = jsonObject.getString("carSeatIds");
        }
        //结构
        String carStructureIds = request.getParameter("carStructureIds");
        if (UtilValidate.isEmpty(carStructureIds) && UtilValidate.isNotEmpty(jsonObject.get("carStructureIds"))) {
            carStructureIds = jsonObject.getString("carStructureIds");
        }
        //制造商
        String productionModels = request.getParameter("productionModels");
        if (UtilValidate.isEmpty(productionModels) && UtilValidate.isNotEmpty(jsonObject.get("productionModels"))) {
            productionModels = jsonObject.getString("productionModels");
        }
        //关键字
        String keyWord = request.getParameter("keyWord");
        if (UtilValidate.isEmpty(keyWord) && UtilValidate.isNotEmpty(jsonObject.get("keyWord"))) {
            keyWord = jsonObject.getString("keyWord");
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        //车系编号
        String productCarSeriesId = request.getParameter("productCarSeriesId");
        if (UtilValidate.isEmpty(productCarSeriesId) && UtilValidate.isNotEmpty(jsonObject.get("productCarSeriesId"))) {
            productCarSeriesId = jsonObject.getString("productCarSeriesId");
        }
        if (UtilValidate.isEmpty(productCarSeriesId)) {
            request.setAttribute("error", "车系ID不能为空");
            response.setStatus(403);
            return "error";
        }

        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        //车型视图
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        dynamicViewViewEntity.addAlias("PCM", "productCarModelId", "productCarModelId", null, false, true, null);
        dynamicViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        dynamicViewViewEntity.addAlias("PCM", "carModelName");
        dynamicViewViewEntity.addAlias("PCM", "productionModel");//制造商
        dynamicViewViewEntity.addAlias("PCM", "carStructureId");//结构
        dynamicViewViewEntity.addAlias("PCM", "carSeatId");//座位数
        dynamicViewViewEntity.addAlias("PCM", "carDisplacementId");//排量
        dynamicViewViewEntity.addAlias("PCM", "carFuelId");//能源
        dynamicViewViewEntity.addAlias("PCM", "transmissionType");//变速箱
        dynamicViewViewEntity.addAlias("PCM", "carDisplacementName");//排量名称
        dynamicViewViewEntity.addAlias("PCM", "carFuelName");//燃料名称
        dynamicViewViewEntity.addAlias("PCM", "carStructureName");//结构名称
        dynamicViewViewEntity.addAlias("PCM", "carDriverName");//驱动名称
        dynamicViewViewEntity.addAlias("PCM", "carSeatName");//座位名称
        dynamicViewViewEntity.addAlias("PCM", "productionModelName");//制造商名称
        dynamicViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        dynamicViewViewEntity.addAlias("PCS", "productCarSeriesId");
        dynamicViewViewEntity.addAlias("PCS", "isDel1", "isDel", null, false, false, null);
        dynamicViewViewEntity.addAlias("PCS", "carCountryId");//国别
        dynamicViewViewEntity.addAlias("PCS", "carLevelId");//级别
        dynamicViewViewEntity.addAlias("PCS", "productBrandId");//品牌
        dynamicViewViewEntity.addAlias("PCS", "carSeriesName");//车系名称
        dynamicViewViewEntity.addAlias("PCS", "productBrandName");//品牌名称
        dynamicViewViewEntity.addAlias("PCS", "carCountryName");//国别名称
        dynamicViewViewEntity.addAlias("PCS", "carLevelName");//级别名称
        dynamicViewViewEntity.addViewLink("PCM", "PCS", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        dynamicViewViewEntity.addMemberEntity("P", "Product");
        dynamicViewViewEntity.addAlias("P", "productId");
        dynamicViewViewEntity.addAlias("P", "productName");
        dynamicViewViewEntity.addAlias("P", "isOnline");
        dynamicViewViewEntity.addAlias("P", "isVerify");
        dynamicViewViewEntity.addAlias("P", "isDel");
        dynamicViewViewEntity.addAlias("P", "carTypeId");
        dynamicViewViewEntity.addAlias("P", "productTypeId");
        dynamicViewViewEntity.addAlias("P", "mainProductId");
        dynamicViewViewEntity.addAlias("P", "productCarSeriesId");
        dynamicViewViewEntity.addAlias("P", "introductionDate");
        dynamicViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewViewEntity.addAlias("P", "createdStamp");
        dynamicViewViewEntity.addViewLink("PCM", "P", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        dynamicViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PP", "ProductPrice");
        dynamicViewViewEntity.addAlias("PP", "productPriceTypeId");
        dynamicViewViewEntity.addAlias("PP", "price");
        dynamicViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));//默认价格
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));//是否上架
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));//是否审核
        conditions.add(EntityCondition.makeCondition("isDel", "N"));//是否删除
        conditions.add(EntityCondition.makeCondition("carTypeId", "NEW_CAR"));//车辆类别
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));//商品类别
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("productCarSeriesId", productCarSeriesId));//车系编号
        conditions.add(EntityCondition.makeCondition("isDel1", "N"));//是否删除
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));//是否删除
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售开始日期
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售结束日期
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));//店铺编号
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        if (UtilValidate.isNotEmpty(productBrandIds)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", EntityOperator.IN, UtilMisc.toListArray(productBrandIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carLevelIds)) {
            conditions.add(EntityCondition.makeCondition("carLevelId", EntityOperator.IN, UtilMisc.toListArray(carLevelIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carCountryIds)) {
            conditions.add(EntityCondition.makeCondition("carCountryId", EntityOperator.IN, UtilMisc.toListArray(carCountryIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carFuelIds)) {
            conditions.add(EntityCondition.makeCondition("carFuelId", EntityOperator.IN, UtilMisc.toListArray(carFuelIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(transmissionTypes)) {
            conditions.add(EntityCondition.makeCondition("transmissionType", EntityOperator.IN, UtilMisc.toListArray(transmissionTypes.split(","))));
        }
        if (UtilValidate.isNotEmpty(carDisplacementIds)) {
            conditions.add(EntityCondition.makeCondition("carDisplacementId", EntityOperator.IN, UtilMisc.toListArray(carDisplacementIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carSeatIds)) {
            conditions.add(EntityCondition.makeCondition("carSeatId", EntityOperator.IN, UtilMisc.toListArray(carSeatIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carStructureIds)) {
            conditions.add(EntityCondition.makeCondition("carStructureId", EntityOperator.IN, UtilMisc.toListArray(carStructureIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(productionModels)) {
            conditions.add(EntityCondition.makeCondition("productionModel", EntityOperator.IN, UtilMisc.toListArray(productionModels.split(","))));
        }
        if (UtilValidate.isNotEmpty(keyWord)) {
            List<EntityCondition> keyWordConditions = FastList.newInstance();
            keyWordConditions.add(EntityCondition.makeCondition("carModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDisplacementName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carFuelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carStructureName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDriverName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeatName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productionModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeriesName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productBrandName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carCountryName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carLevelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            conditions.add(EntityCondition.makeCondition(keyWordConditions, EntityOperator.OR));
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        List<String> fieldToSelect = FastList.newInstance();
        fieldToSelect.add("productId");
        fieldToSelect.add("carModelName");
        fieldToSelect.add("price");
        EntityFindOptions carModelOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
        try {
            //车系
            GenericValue productCarSeries = delegator.findByPrimaryKey("ProductCarSeries", UtilMisc.toMap("productCarSeriesId", productCarSeriesId));
            List<GenericValue> productCarSeriesContents = delegator.findByAnd("ProductCarSeriesContent", UtilMisc.toMap("productCarSeriesId", productCarSeriesId));
            if (UtilValidate.isNotEmpty(productCarSeriesContents)) {
                List<String> imgList = FastList.newInstance();
                for (GenericValue productCarSeriesContent : productCarSeriesContents) {
                    imgList.add(request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productCarSeriesContent.get("contentId"));
                }
                request.setAttribute("imgList", imgList);
            }
            //品牌
            GenericValue productBrand = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", productCarSeries.get("productBrandId")));
            request.setAttribute("brandName", productBrand.get("brandName"));

            Boolean beganTransaction = TransactionUtil.begin();
            //车量商品数据
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, fieldToSelect, UtilMisc.toList("-introductionDate"), findOpts);
            List<GenericValue> cars = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();

            //该车系下车型最低价
            eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, fieldToSelect, UtilMisc.toList("price"), carModelOpts);
            List<GenericValue> priceLowModel = eli.getCompleteList();
            if (UtilValidate.isNotEmpty(priceLowModel)) {
                request.setAttribute("defaultPriceLow", priceLowModel.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            //该车系下车型最高价
            eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, fieldToSelect, UtilMisc.toList("-price"), carModelOpts);
            List<GenericValue> priceHighModel = eli.getCompleteList();
            if (UtilValidate.isNotEmpty(priceHighModel)) {
                request.setAttribute("defaultPriceHigh", priceHighModel.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            dynamicViewViewEntity.addMemberEntity("MPP", "ProductPrice");
            dynamicViewViewEntity.addAlias("MPP", "productPriceTypeId1", "productPriceTypeId", null, false, false, null);
            dynamicViewViewEntity.addAlias("MPP", "marketPrice", "price", null, false, false, null);
            dynamicViewViewEntity.addViewLink("P", "MPP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            fieldToSelect.add("marketPrice");
            conditions.add(EntityCondition.makeCondition("productPriceTypeId1", "MARKET_PRICE"));
            //该车系下车型最低价
            eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, fieldToSelect, UtilMisc.toList("marketPrice"), carModelOpts);
            priceLowModel = eli.getCompleteList();
            if (UtilValidate.isNotEmpty(priceLowModel)) {
                request.setAttribute("marketPriceLow", priceLowModel.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            //该车系下车型最高价
            eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, fieldToSelect, UtilMisc.toList("-marketPrice"), carModelOpts);
            priceHighModel = eli.getCompleteList();
            if (UtilValidate.isNotEmpty(priceHighModel)) {
                request.setAttribute("marketPriceHigh", priceHighModel.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            eli.close();
            TransactionUtil.commit(beganTransaction);
            request.setAttribute("max", resultSize);
            List<Map> resultList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(cars)) {
                for (GenericValue car : cars) {
                    Map carMap = FastMap.newInstance();
                    carMap.put("productId", car.get("productId"));
                    carMap.put("carModelName", car.get("carModelName"));
                    carMap.put("defaultPrice", car.getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
                    List<GenericValue> productPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", car.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                    if (UtilValidate.isNotEmpty(productPriceList)) {
                        carMap.put("marketPrice", productPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    resultList.add(carMap);
                }
            }
            request.setAttribute("carList", resultList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 车型详情
     *
     * @param request
     * @param response
     * @return
     */
    public static String carModelDetail(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productId = request.getParameter("productId");
        if (UtilValidate.isEmpty(productId) && UtilValidate.isNotEmpty(jsonObject.get("productId"))) {
            productId = jsonObject.getString("productId");
        }
        if (UtilValidate.isEmpty(productId)) {
            request.setAttribute("error", "商品ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            if (UtilValidate.isEmpty(product)) {
                request.setAttribute("error", "车辆不存在");
                response.setStatus(403);
                return "error";
            }
            request.setAttribute("productCarModelId", product.get("productCarModelId"));
            //车辆主标题
            request.setAttribute("productName", product.get("productName"));
            //车辆副标题
            request.setAttribute("productSubheadName", product.get("productSubheadName"));
            //车辆图片
            List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId));
            if (UtilValidate.isNotEmpty(productContents)) {
                List<String> imgList = FastList.newInstance();
                for (GenericValue productContent : productContents) {
                    imgList.add(request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContent.getString("contentId"));
                }
                request.setAttribute("imgList", imgList);
            }
            //默认价即销售价
            List<GenericValue> defaultPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "DEFAULT_PRICE"));
            if (UtilValidate.isNotEmpty(defaultPriceList)) {
                request.setAttribute("defaultPrice", defaultPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            //车辆市场价即指导价
            List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId, "productPriceTypeId", "MARKET_PRICE"));
            if (UtilValidate.isNotEmpty(marketPriceList)) {
                request.setAttribute("marketPrice", marketPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
            //定金
            BigDecimal frontMoney = null;
            BigDecimal frontMoneys = product.getBigDecimal("frontMoney");
            BigDecimal ratio = UtilValidate.isNotEmpty(product.getBigDecimal("ratio")) ? product.getBigDecimal("ratio") : BigDecimal.ZERO;
            if (UtilValidate.isNotEmpty(frontMoneys)) {
                frontMoney = frontMoneys.setScale(2, BigDecimal.ROUND_HALF_UP);
            } else {
                frontMoney = defaultPriceList.get(0).getBigDecimal("price").multiply(ratio).divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
            request.setAttribute("frontMoney", frontMoney);
            //月销量
            List<EntityCondition> orderConditions = FastList.newInstance();
            orderConditions.add(EntityCondition.makeCondition("productId", productId));
            orderConditions.add(EntityCondition.makeCondition("createdStamp", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.adjustTimestamp(UtilDateTime.nowTimestamp(), Calendar.MONTH, -1)));
            DynamicViewEntity orderViewEntity = new DynamicViewEntity();
            orderViewEntity.addMemberEntity("OI", "OrderItem");
            orderViewEntity.addAlias("OI", "quantity", "quantity", null, false, false, "sum");
            orderViewEntity.addAlias("OI", "productId");
            orderViewEntity.addAlias("OI", "createdStamp");
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(orderViewEntity, EntityCondition.makeCondition(orderConditions, EntityOperator.AND), null, UtilMisc.toSet("quantity"), null, null);
            List<GenericValue> orderItems = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            int saleNum = 0;
            if (UtilValidate.isNotEmpty(orderItems) && UtilValidate.isNotEmpty(orderItems.get(0).get("quantity"))) {
                saleNum = orderItems.get(0).getBigDecimal("quantity").intValue();
            }
            request.setAttribute("saleNum", saleNum);
            request.setAttribute("carTypeId", product.get("carTypeId"));
            /*新车、平行进口车特有字段*/
            if (UtilValidate.areEqual("NEW_CAR", product.get("carTypeId")) || UtilValidate.areEqual("IMPORTED_CAR", product.get("carTypeId"))) {
                //特征
                List<GenericValue> productFeatrueTypes = delegator.findByAnd("GetFeatureIdListForProductId", UtilMisc.toMap("productId", productId));
                if (UtilValidate.isNotEmpty(productFeatrueTypes)) {
                    List<Map> featureTypeList = FastList.newInstance();
                    for (GenericValue productFeatrueType : productFeatrueTypes) {
                        Map featureTypeMap = FastMap.newInstance();
                        GenericValue featureType = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeatrueType.get("productFeatureTypeId")));
                        featureTypeMap.put("featureTypeName", featureType.get("productFeatureTypeName"));
                        DynamicViewEntity productFeatureViewEntity = new DynamicViewEntity();
                        productFeatureViewEntity.addMemberEntity("PF", "ProductFeature");
                        productFeatureViewEntity.addAlias("PF", "productFeatureId");
                        productFeatureViewEntity.addAlias("PF", "productFeatureName");
                        productFeatureViewEntity.addAlias("PF", "productFeatureTypeId");
                        productFeatureViewEntity.addAlias("PF", "sequenceNum");
                        productFeatureViewEntity.addMemberEntity("PFA", "ProductFeatureAssoc");
                        productFeatureViewEntity.addAlias("PFA", "productId");
                        productFeatureViewEntity.addViewLink("PF", "PFA", true, ModelKeyMap.makeKeyMapList("productFeatureId", "productFeatureId"));
                        List<EntityCondition> productFeatureConditions = FastList.newInstance();
                        productFeatureConditions.add(EntityCondition.makeCondition("productFeatureTypeId", productFeatrueType.get("productFeatureTypeId")));
                        productFeatureConditions.add(EntityCondition.makeCondition("productId", productId));
                        beganTransaction = TransactionUtil.begin();
                        eli = delegator.findListIteratorByCondition(productFeatureViewEntity, EntityCondition.makeCondition(productFeatureConditions, EntityOperator.AND), null, UtilMisc.toSet("productFeatureId", "productFeatureName"), UtilMisc.toList("sequenceNum"), null);
                        List<GenericValue> productFeatures = eli.getCompleteList();
                        eli.close();
                        TransactionUtil.commit(beganTransaction);
                        if (UtilValidate.isNotEmpty(productFeatures)) {
                            featureTypeMap.put("featureList", productFeatures);
                        }
                        featureTypeList.add(featureTypeMap);
                    }
                    request.setAttribute("featureTypeList", featureTypeList);
                }
                //装潢服务,关联的配件商品
                DynamicViewEntity configViewEntity = new DynamicViewEntity();
                configViewEntity.addMemberEntity("PA", "ProductAssoc");
                configViewEntity.addAlias("PA", "originalProductId", "productId", null, false, false, null);
                configViewEntity.addAlias("PA", "productIdTo", "productIdTo", null, false, true, null);
                configViewEntity.addAlias("PA", "productAssocTypeId");
                configViewEntity.addMemberEntity("P", "Product");
                configViewEntity.addAlias("PA", "productId");
                configViewEntity.addAlias("P", "productName");
                configViewEntity.addAlias("P", "isOnline");
                configViewEntity.addAlias("P", "isVerify");
                configViewEntity.addAlias("P", "isDel");
                configViewEntity.addAlias("P", "introductionDate");
                configViewEntity.addAlias("P", "salesDiscontinuationDate");
                configViewEntity.addAlias("P", "productTypeId");
                configViewEntity.addAlias("P", "mainProductId");
                configViewEntity.addViewLink("PA", "P", true, ModelKeyMap.makeKeyMapList("productIdTo", "productId"));
                configViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
                configViewEntity.addAlias("PSPA", "productStoreId");
                configViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
                List<EntityCondition> configConditions = FastList.newInstance();
                configConditions.add(EntityCondition.makeCondition("originalProductId", productId));
                configConditions.add(EntityCondition.makeCondition("productAssocTypeId", "PRODUCT_CONF"));
                configConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
                configConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
                configConditions.add(EntityCondition.makeCondition("isDel", "N"));
                configConditions.add(EntityCondition.makeCondition("productTypeId", "PARETS_GOOD"));
                configConditions.add(EntityCondition.makeCondition("mainProductId", null));
                configConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                List<EntityCondition> list2 = FastList.newInstance();
                list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
                configConditions.add(EntityCondition.makeCondition(list2, EntityOperator.OR));
                configConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                beganTransaction = TransactionUtil.begin();
                eli = delegator.findListIteratorByCondition(configViewEntity, EntityCondition.makeCondition(configConditions, EntityOperator.AND), null, null, null, null);
                List<GenericValue> productConfigs = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                List<Map> productConfigList = FastList.newInstance();
                for (GenericValue productConfig : productConfigs) {
                    Map map = FastMap.newInstance();
                    map.put("productId", productConfig.get("productIdTo"));
                    productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productConfig.get("productIdTo"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                    }
                    map.put("productName", productConfig.get("productName"));
                    List<GenericValue> priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productConfig.get("productIdTo"), "productPriceTypeId", "DEFAULT_PRICE"));
                    if (UtilValidate.isNotEmpty(priceList)) {
                        map.put("defaultPrice", priceList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productConfig.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                    if (UtilValidate.isNotEmpty(priceList)) {
                        map.put("marketPrice", priceList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    productConfigList.add(map);
                }
                request.setAttribute("productConfigList", productConfigList);
                //TODO:车险
                //其他服务，相同车型下的所有增值商品
                DynamicViewEntity otherViewEntity = new DynamicViewEntity();
                otherViewEntity.addMemberEntity("P", "Product");
                otherViewEntity.addAlias("P", "productId", "productId", null, false, true, null);
                otherViewEntity.addAlias("P", "productName");
                otherViewEntity.addAlias("P", "isOnline");
                otherViewEntity.addAlias("P", "isVerify");
                otherViewEntity.addAlias("P", "isDel");
                otherViewEntity.addAlias("P", "introductionDate");
                otherViewEntity.addAlias("P", "salesDiscontinuationDate");
                otherViewEntity.addAlias("P", "productTypeId");
                otherViewEntity.addAlias("P", "mainProductId");
                otherViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
                otherViewEntity.addAlias("PSPA", "productStoreId");
                otherViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
                otherViewEntity.addMemberEntity("PMR", "ProductModelReference");
                otherViewEntity.addAlias("PMR", "productCarModelId");
                otherViewEntity.addViewLink("P", "PMR", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
                List<EntityCondition> otherConditions = FastList.newInstance();
                otherConditions.add(EntityCondition.makeCondition("productCarModelId", product.get("productCarModelId")));
                otherConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
                otherConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
                otherConditions.add(EntityCondition.makeCondition("isDel", "N"));
                otherConditions.add(EntityCondition.makeCondition("productTypeId", "INCREMENT_GOOD"));
                otherConditions.add(EntityCondition.makeCondition("mainProductId", null));
                otherConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                List<EntityCondition> list3 = FastList.newInstance();
                list3.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                list3.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
                otherConditions.add(EntityCondition.makeCondition(list3, EntityOperator.OR));
                otherConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                beganTransaction = TransactionUtil.begin();
                eli = delegator.findListIteratorByCondition(otherViewEntity, EntityCondition.makeCondition(otherConditions, EntityOperator.AND), null, null, null, null);
                List<GenericValue> otherProducts = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                List<Map> otherProductList = FastList.newInstance();
                for (GenericValue otherProduct : otherProducts) {
                    Map map = FastMap.newInstance();
                    map.put("productId", otherProduct.get("productId"));
                    productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", otherProduct.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        request.setAttribute("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                    }
                    map.put("productName", otherProduct.get("productName"));
                    List<GenericValue> priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", otherProduct.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                    if (UtilValidate.isNotEmpty(priceList)) {
                        map.put("defaultPrice", priceList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    priceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", otherProduct.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                    if (UtilValidate.isNotEmpty(priceList)) {
                        map.put("marketPrice", priceList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                    otherProductList.add(map);
                }
                request.setAttribute("otherProductList", otherProductList);

                //贷款商品
                DynamicViewEntity loanViewEntity = new DynamicViewEntity();
                loanViewEntity.addMemberEntity("P", "Product");
                loanViewEntity.addAlias("P", "productId", "productId", null, false, true, null);
                loanViewEntity.addAlias("P", "rate");
                loanViewEntity.addAlias("P", "isOnline");
                loanViewEntity.addAlias("P", "isVerify");
                loanViewEntity.addAlias("P", "isDel");
                loanViewEntity.addAlias("P", "introductionDate");
                loanViewEntity.addAlias("P", "salesDiscontinuationDate");
                loanViewEntity.addAlias("P", "productTypeId");
                loanViewEntity.addAlias("P", "mainProductId");
                loanViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
                loanViewEntity.addAlias("PSPA", "productStoreId");
                loanViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
                List<EntityCondition> loanConditions = FastList.newInstance();
                loanConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
                loanConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
                loanConditions.add(EntityCondition.makeCondition("isDel", "N"));
                loanConditions.add(EntityCondition.makeCondition("productTypeId", "LOAN_GOOD"));
                loanConditions.add(EntityCondition.makeCondition("mainProductId", null));
                loanConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                List<EntityCondition> list4 = FastList.newInstance();
                list4.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                list4.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
                loanConditions.add(EntityCondition.makeCondition(list4, EntityOperator.OR));
                loanConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                beganTransaction = TransactionUtil.begin();
                eli = delegator.findListIteratorByCondition(loanViewEntity, EntityCondition.makeCondition(loanConditions, EntityOperator.AND), null, null, null, null);
                List<GenericValue> loanProducts = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                if (UtilValidate.isNotEmpty(loanProducts)) {
                    request.setAttribute("loanProductId", loanProducts.get(0).get("productId"));
                    request.setAttribute("loanRate", loanProducts.get(0).get("rate"));
                }
            }
            /*新车、平行进口车特有字段*/
            /*二手车特有字段*/
            if (UtilValidate.areEqual("USED_CAR", product.get("carTypeId"))) {
                //行驶里程数
                request.setAttribute("carMileage", UtilValidate.isNotEmpty(product.get("carMileage")) ? product.getBigDecimal("carMileage").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP) : "");
                //上路年月
                request.setAttribute("useTime", UtilValidate.isNotEmpty(product.get("useTime")) ? product.getString("useTime").replace("-", "年") + "月" : "");
                //颜色
                request.setAttribute("oldCarColor", product.get("oldCarColor"));
                //是否过户
                if (UtilValidate.areEqual("Y", product.get("isTransferName"))) {
                    request.setAttribute("isTransfer", "是");
                } else {
                    request.setAttribute("isTransfer", "否");
                }
                //保险到期日
                request.setAttribute("insuranceDueDate", UtilValidate.isNotEmpty(product.get("insuranceDueDate")) ? UtilDateTime.timeStampToString(product.getTimestamp("insuranceDueDate"), "yyyy年MM月", timeZone, locale) : "");
                //年审年月
                request.setAttribute("annualYm", UtilValidate.isNotEmpty(product.get("annualYm")) ? UtilDateTime.timeStampToString(product.getTimestamp("annualYm"), "yyyy年MM月", timeZone, locale) : "");
            }
            /*二手车特有字段*/
            //商品详情
            request.setAttribute("description", UtilValidate.isNotEmpty(product.get("mobileDetails")) ? product.getString("mobileDetails").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
            //PC端详情
            request.setAttribute("pcDetails", UtilValidate.isNotEmpty(product.get("pcDetails")) ? product.getString("pcDetails").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
            //商品配置
            GenericValue productCarModel = delegator.findByPrimaryKey("ProductCarModel", UtilMisc.toMap("productCarModelId", product.get("productCarModelId")));
            GenericValue productCarSeries = delegator.findByPrimaryKey("ProductCarSeries", UtilMisc.toMap("productCarSeriesId", product.get("productCarSeriesId")));
            GenericValue productBrnad = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", product.get("productCarLibBrandId")));
            if (UtilValidate.isNotEmpty(productCarModel)) {
                GenericValue productCarModelCollocation = delegator.findByPrimaryKey("ProductCarModelCollocation", UtilMisc.toMap("productCarModelCollocationId", productCarModel.get("productCarModelCollocationId")));
                Map<String, Map> carSettings = FastMap.newInstance();
                Map map1 = FastMap.newInstance();
                map1.put("厂家", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("productionModelName") : "");
                map1.put("品牌", UtilValidate.isNotEmpty(productBrnad) ? productBrnad.get("brandName") : "");
                map1.put("车系", UtilValidate.isNotEmpty(productCarSeries) ? productCarSeries.get("carSeriesName") : "");
                map1.put("车型", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("carModelName") : "");
                map1.put("销售名称", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("salesName") : "");
                map1.put("年款", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("carYearId") : "");
                map1.put("车辆类型", UtilValidate.isNotEmpty(productCarSeries) ? productCarSeries.get("carType") : "");
                map1.put("车辆级别", UtilValidate.isNotEmpty(productCarSeries) ? productCarSeries.get("carLevelName") : "");
                map1.put("指导价格", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("bpGuidingPrice") : "");
                map1.put("上市年份", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("bpListingYear") : "");
                map1.put("上市月份", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("bpListedMonth") : "");
                map1.put("生产年份", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("bpProductionYear") : "");
                map1.put("停产年份", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("bpShutdownYear") : "");
                map1.put("生产状态", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("bpProductionState") : "");
                map1.put("国别", UtilValidate.isNotEmpty(productCarSeries) ? productCarSeries.get("carCountryName") : "");
                map1.put("国产合资进口", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("bpDomesticImport") : "");
                carSettings.put("基本参数", map1);
                Map map2 = FastMap.newInstance();
                map2.put("车身型式", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("carStructureName") : "");
                map2.put("长(mm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbLong") : "");
                map2.put("宽(mm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbWidth") : "");
                map2.put("高(mm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbHeight") : "");
                map2.put("轴距(mm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbWheelbase") : "");
                map2.put("前轮距(mm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbFrontTrack") : "");
                map2.put("后轮距(mm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbRearWheel") : "");
                map2.put("整备质量(kg)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbTheQualityOfService") : "");
                map2.put("最大载重质量(kg)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbMaximumLoadMass") : "");
                map2.put("油箱容积(L)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbFuelTankVolume") : "");
                map2.put("行李厢容积(L)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbLuggageCompartmentCapacity") : "");
                map2.put("车门数", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("cbNumberOfDoors") : "");
                map2.put("座位数(个)", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("carSeatName") : "");
                carSettings.put("车身", map2);
                Map map3 = FastMap.newInstance();
                map3.put("排放标准", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egEmissionStandard") : "");
                map3.put("气缸容积", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egCylinderVolume") : "");
                map3.put("排量(升)", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("carDisplacementName") : "");
                map3.put("进气形式", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egInletForm") : "");
                map3.put("燃料类型", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("carFuelName") : "");
                map3.put("燃油标号", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egFuelLabel") : "");
                map3.put("最大马力(ps)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egMaximumHorsepower") : "");
                map3.put("最大功率(kW)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egMaximumPower") : "");
                map3.put("最大功率转速(rpm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egMaximumPowerRotationalSpeed") : "");
                map3.put("最大扭矩(N·m)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egMaximumTorque") : "");
                map3.put("最大扭矩转速(rpm)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egMaximumTorqueSpeed") : "");
                map3.put("气缸排列形式", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egArrangementOfCylinders") : "");
                map3.put("气缸数(个)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egNumberOfCylinders") : "");
                map3.put("每缸气门数(个)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egNumberOfValvesPerCylinder") : "");
                map3.put("压缩比", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egCompressionRatio") : "");
                map3.put("供油方式", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egFuelSupplyMode") : "");
                map3.put("综合工况油耗", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egComprehensiveOperatingOilConsumption") : "");
                map3.put("加速时间(0-100km/h)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egAccelerationTime") : "");
                map3.put("最高车速(km/h)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("egMaximumSpeed") : "");
                carSettings.put("发动机", map3);
                Map map4 = FastMap.newInstance();
                map4.put("前悬挂类型", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("uvFrontSuspensionType") : "");
                map4.put("后悬挂类型", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("uvRearSuspensionType") : "");
                map4.put("转向机形式", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("uvSteeringGearForm") : "");
                map4.put("助力类型", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("uvPowerType") : "");
                map4.put("发动机位置", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("uvEnginePosition") : "");
                map4.put("驱动方式", UtilValidate.isNotEmpty(productCarModel) ? productCarModel.get("carDriverName") : "");
                map4.put("驱动形式", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("uvDriveForm") : "");
                carSettings.put("底盘转向", map4);
                Map map5 = FastMap.newInstance();
                map5.put("前制动器类型", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbFrontBrakeType") : "");
                map5.put("后制动器类型", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbRearBrakeType") : "");
                map5.put("前轮胎规格", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbFrontTyreSpecification") : "");
                map5.put("后轮胎规格", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbRearTireSpecification") : "");
                map5.put("前轮毂规格", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbFrontHubSpecification") : "");
                map5.put("后轮毂规格", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbRearWheelHubSpecification") : "");
                map5.put("轮毂材料", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbHubMaterial") : "");
                map5.put("备胎规格", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("wbSpareWheelSpecification") : "");
                carSettings.put("车轮制动", map5);
                Map map6 = FastMap.newInstance();
                map6.put("驾驶座安全气囊", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdDab") : "");
                map6.put("副驾驶安全气囊", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdCopilotAirbag") : "");
                map6.put("前排侧气囊", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdFrontSideAirbag") : "");
                map6.put("后排侧气囊", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdBackSideAirbag") : "");
                map6.put("前排头部气囊(气帘)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdFrontHeadAirbag") : "");
                map6.put("后排头部气囊(气帘)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdRearHeadAirbag") : "");
                map6.put("膝部气囊", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdKneeAirbag") : "");
                map6.put("胎压监测装置", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdTirePressureMonitoringDevice") : "");
                map6.put("零胎压继续行驶", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdZeroTirePressureContinues") : "");
                map6.put("安全带未系提示", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdTheSeatBeltIsNotIndicated") : "");
                map6.put("ISOFIX儿童座椅接口", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdIsofixChildSeatInterface") : "");
                map6.put("LATCH座椅接口", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdLatchSeatInterface") : "");
                map6.put("发动机电子防盗", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdElectronicEngineAntiTheft") : "");
                map6.put("中控锁", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdLock") : "");
                map6.put("遥控钥匙", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdRemoteKey") : "");
                map6.put("无钥匙启动系统", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdKeylessStartSystem") : "");
                map6.put("ABS防抱死", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdAbs") : "");
                map6.put("制动力分配(EBD/CBC等)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdBrakingForceDistribution") : "");
                map6.put("刹车辅助(EBA/BAS/BA等)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdBrakeAssist") : "");
                map6.put("牵引力控制(ASR/TCS/TRC等)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdTractionControl") : "");
                map6.put("车身稳定控制(ESP/DSC/VSC等)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("sdCarBodyStabilityControl") : "");
                carSettings.put("主/被动安全装置", map6);
                Map map7 = FastMap.newInstance();
                map7.put("自动驻车/上坡辅助", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccAutomaticParking") : "");
                map7.put("陡坡缓降", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccHdc") : "");
                map7.put("可变悬挂", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccVariableSuspension") : "");
                map7.put("空气悬挂", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccAirSuspension") : "");
                map7.put("可变转向比", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccVariableSteeringRatio") : "");
                map7.put("并线辅助", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccAuxiliary") : "");
                map7.put("主动刹车", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccActiveBrake") : "");
                map7.put("主动转向系统", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccActiveSteeringSystem") : "");
                map7.put("定速巡航", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccCruiseControl") : "");
                map7.put("泊车辅助", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccParkingAssistance") : "");
                map7.put("自动泊车入位", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccAutomaticParkingAccess") : "");
                map7.put("夜视系统", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccNightVisionSystem") : "");
                map7.put("中控液晶屏分屏显示", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccLcdScreenDisplay") : "");
                map7.put("自适应巡航", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccAdaptiveCruise") : "");
                map7.put("全景摄像头", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccPanoramicCamera") : "");
                map7.put("倒车雷达", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("ccPdcParkingDistanceControl") : "");
                carSettings.put("辅助/操控配置", map7);
                Map map8 = FastMap.newInstance();
                map8.put("电动后备箱", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("scElectricTrunk") : "");
                map8.put("运动外观套件", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("scSportsAppearanceKit") : "");
                map8.put("电动吸合门", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("scElectricPullDoor") : "");
                map8.put("电动天窗", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("scPowerSunroof") : "");
                map8.put("全景天窗", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("scPanoramicSunroof") : "");
                carSettings.put("外部/防盗配置", map8);
                Map map9 = FastMap.newInstance();
                map9.put("真皮方向盘", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icLeatherSteeringWheel") : "");
                map9.put("方向盘上下调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSteeringWheelUpAndDownAdjustment") : "");
                map9.put("方向盘前后调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSteeringWheelFrontAndBackAdjustment") : "");
                map9.put("方向盘电动调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSteeringWheelElectricRegulation") : "");
                map9.put("多功能方向盘", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icMultifunctionalSteeringWheel") : "");
                map9.put("方向盘换挡", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSteeringWheelShift") : "");
                map9.put("真皮座椅", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icLeatherSeat") : "");
                map9.put("运动座椅", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSportSeat") : "");
                map9.put("座椅高低调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSeatAdjustment") : "");
                map9.put("腰部支撑调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icLumbarSupportRegulation") : "");
                map9.put("肩部支撑调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icShoulderSupportAdjustment") : "");
                map9.put("驾驶座座椅电动调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icMotorizedAdjustmentOfDriver") : "");
                map9.put("副驾驶座座椅电动调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icViceDriverSeatElectricAdjustment") : "");
                map9.put("第二排靠背角度调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icAngleAdjustmentOfSecondRowsOfBackrest") : "");
                map9.put("第二排座椅移动", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icMoveTheSecondRowOfSeats") : "");
                map9.put("后排座椅电动调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearSeatElectricAdjustment") : "");
                map9.put("电动座椅记忆", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icElectricSeatMemory") : "");
                map9.put("前排座椅加热", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icHeatedFrontSeats") : "");
                map9.put("后排座椅加热", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearSeatHeating") : "");
                map9.put("座椅通风", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSeatVentilation") : "");
                map9.put("座椅按摩", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSeatMassage") : "");
                map9.put("后排座椅整体放倒", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearSeatOverallRecline") : "");
                map9.put("后排座椅比例放倒", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearSeatRatioDown") : "");
                map9.put("第三排座椅", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icThirdRowSeat") : "");
                map9.put("前座中央扶手", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icFrontCenterArmrest") : "");
                map9.put("后座中央扶手", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearCenterArmrest") : "");
                map9.put("后排杯架", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearCupHolder") : "");
                map9.put("车内氛围灯", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icInteriorLight") : "");
                map9.put("后风挡遮阳帘", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearWindshieldShade") : "");
                map9.put("后排侧遮阳帘", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icRearSideShade") : "");
                map9.put("遮阳板化妆镜", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("icSunShadingMirror") : "");
                carSettings.put("内部配置", map9);
                Map map10 = FastMap.newInstance();
                map10.put("倒车视频影像", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcReversingVideoImages") : "");
                map10.put("行车电脑显示屏", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcDrivingDomputerDisplayScreen") : "");
                map10.put("HUD抬头数字显示", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcHudHeadsUpDigitalDisplay") : "");
                map10.put("GPS导航", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcGpsNavigation") : "");
                map10.put("定位互动服务", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcLocationInteractiveService") : "");
                map10.put("中控台彩色大屏", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcLargeColorScreenOfCenterConsole") : "");
                map10.put("人机交互系统", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcHumanComputerInteractionSystem") : "");
                map10.put("内置硬盘", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcBuiltInHardDisk") : "");
                map10.put("蓝牙/车载电话", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcCarPhone") : "");
                map10.put("车载电视", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcCarTv") : "");
                map10.put("后排液晶屏", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcRearLcdScreen") : "");
                map10.put("外接音源接口(AUX/USB/iPod等)", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcExternalAudioSourceInterface") : "");
                map10.put("音频支持MP3", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcAudioSupportForMp3") : "");
                map10.put("单碟CD", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcSingleDiscCd") : "");
                map10.put("多碟CD", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcMultiDiscCd") : "");
                map10.put("虚拟多碟CD", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcVirtualMultiDiscCd") : "");
                map10.put("单碟DVD", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcSingleDiscDvd") : "");
                map10.put("多碟DVD", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcMultiDiscDvd") : "");
                map10.put("扬声器数量", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcLoudspeakerQuantity") : "");
                map10.put("车载信息服务", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcTelematics") : "");
                carSettings.put("多媒体配置", map10);
                Map map11 = FastMap.newInstance();
                map11.put("氙气大灯", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("lcXenonHeadlight") : "");
                map11.put("LED大灯", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("lcLedHeadlight") : "");
                map11.put("日间行车灯", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("lcDrl") : "");
                map11.put("自动头灯", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("lcAutomaticHeadlamp") : "");
                map11.put("转向头灯", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("lcTurnHeadLamp") : "");
                map11.put("前雾灯", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("lcFrontFogLamp") : "");
                map11.put("大灯高度可调", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("mcReversingVideoImages") : "");
                map11.put("大灯清洗装置", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("lcHeadlightCleaningDevice") : "");
                carSettings.put("灯光配置", map11);
                Map map12 = FastMap.newInstance();
                map12.put("玻璃/后视镜", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmFrontPowerWindow") : "");
                map12.put("后电动车窗", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmRearPowerWindow") : "");
                map12.put("车窗防夹手功能", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmWindowClampHandFunction") : "");
                map12.put("隔热玻璃", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmInsulatingGlass") : "");
                map12.put("后视镜电动调节", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmElectricAdjustmentOfRearviewMirror") : "");
                map12.put("后视镜加热", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmRearviewMirrorHeating") : "");
                map12.put("后视镜自动防眩目", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmAutomaticAntiGlareOfRearviewMirror") : "");
                map12.put("后视镜电动折叠", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmRearviewMirrorPowerFolding") : "");
                map12.put("后视镜记忆", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmRearviewMirrorMemory") : "");
                map12.put("后雨刷", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmRearWiper") : "");
                map12.put("感应雨刷", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("rmInductionWiper") : "");
                carSettings.put("灯光配置", map12);
                Map map13 = FastMap.newInstance();
                map13.put("玻璃/后视镜", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("acAirConditioner") : "");
                map13.put("自动空调", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("acAutomaticAirConditioning") : "");
                map13.put("后排独立空调", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("acRearIndependentAirConditioning") : "");
                map13.put("后座出风口", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("acRearAirOutlet") : "");
                map13.put("温度分区控制", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("acTemperatureZoningControl") : "");
                map13.put("空气调节/花粉过滤", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("acAirConditioning") : "");
                map13.put("车载冰箱", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("acCarRefrigerator") : "");
                carSettings.put("空调/冰箱", map13);
                Map map14 = FastMap.newInstance();
                map14.put("变速器类型", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("transmissionType") : "");
                map14.put("变速器描述", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("gbTransmissionDescription") : "");
                map14.put("档位数", UtilValidate.isNotEmpty(productCarModelCollocation) ? productCarModelCollocation.get("gbNumberOfGears") : "");
                carSettings.put("变速箱", map14);
                request.setAttribute("carSettings", carSettings);
            }
            //客服电话
            GenericValue frontRule = delegator.findByPrimaryKey("FrontRule", UtilMisc.toMap("frontRuleId", "front_rule"));
            if (UtilValidate.isNotEmpty(frontRule)) {
                request.setAttribute("customerServiceNumber", frontRule.get("customerServiceNumber"));
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取车辆国别
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarCountry(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> carCountrys = null;
        try {
            carCountrys = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_COUNTRY", "enumCode", "N"), UtilMisc.toList("sequenceId"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(carCountrys)) {
            List<Map> countrys = FastList.newInstance();
            for (GenericValue carCountry : carCountrys) {
                Map map = FastMap.newInstance();
                map.put("countryId", carCountry.get("enumId"));
                map.put("countryName", carCountry.get("description"));
                countrys.add(map);
            }
            request.setAttribute("countrys", countrys);
        }
        return "success";
    }

    /**
     * 根据品牌获取车系
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarSeries(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productBrandId = request.getParameter("productBrandId");
        if (UtilValidate.isEmpty(productBrandId) && UtilValidate.isNotEmpty(jsonObject.get("productBrandId"))) {
            productBrandId = jsonObject.getString("productBrandId");
        }
        if (UtilValidate.isEmpty(productBrandId)) {
            request.setAttribute("error", "品牌ID不能为空");
            response.setStatus(403);
            return "error";
        }
        List<GenericValue> carSeriesList = null;
        try {
            carSeriesList = delegator.findByAnd("ProductCarSeries", UtilMisc.toMap("productBrandId", productBrandId, "isCanSell", "Y", "isDel", "N"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(carSeriesList)) {
            List<Map> carSeriess = FastList.newInstance();
            for (GenericValue carSeries : carSeriesList) {
                Map map = FastMap.newInstance();
                map.put("productCarSeriesId", carSeries.get("productCarSeriesId"));
                map.put("carSeriesName", carSeries.get("carSeriesName"));
                carSeriess.add(map);
            }
            request.setAttribute("carSeriesList", carSeriess);
        }
        return "success";
    }

    /**
     * 查询平行进口车
     *
     * @param request
     * @param response
     * @return
     */
    public static String getImportedCar(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        //品牌
        String productBrandId = request.getParameter("productBrandId");
        if (UtilValidate.isEmpty(productBrandId) && UtilValidate.isNotEmpty(jsonObject.get("productBrandId"))) {
            productBrandId = jsonObject.getString("productBrandId");
        }
        //车系
        String productCarSeriesId = request.getParameter("productCarSeriesId");
        if (UtilValidate.isEmpty(productCarSeriesId) && UtilValidate.isNotEmpty(jsonObject.get("productCarSeriesId"))) {
            productCarSeriesId = jsonObject.getString("productCarSeriesId");
        }
        //进口国家
        String countryId = request.getParameter("countryId");
        if (UtilValidate.isEmpty(countryId) && UtilValidate.isNotEmpty(jsonObject.get("countryId"))) {
            countryId = jsonObject.getString("countryId");
        }
        //最低价格
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        //关键字
        String keyWord = request.getParameter("keyWord");
        if (UtilValidate.isEmpty(keyWord) && UtilValidate.isNotEmpty(jsonObject.get("keyWord"))) {
            keyWord = jsonObject.getString("keyWord");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("P", "Product");
        dynamicViewViewEntity.addAlias("P", "productId");
        dynamicViewViewEntity.addAlias("P", "productName");
        dynamicViewViewEntity.addAlias("P", "isOnline");
        dynamicViewViewEntity.addAlias("P", "isVerify");
        dynamicViewViewEntity.addAlias("P", "isDel");
        dynamicViewViewEntity.addAlias("P", "carTypeId");
        dynamicViewViewEntity.addAlias("P", "productTypeId");
        dynamicViewViewEntity.addAlias("P", "mainProductId");
        dynamicViewViewEntity.addAlias("P", "productBrandId");
        dynamicViewViewEntity.addAlias("P", "productCarSeriesId");
        dynamicViewViewEntity.addAlias("P", "productCarModelId");
        dynamicViewViewEntity.addAlias("P", "introductionDate");
        dynamicViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewViewEntity.addAlias("P", "createdStamp");
        dynamicViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PP", "ProductPrice");
        dynamicViewViewEntity.addAlias("PP", "productPriceTypeId");
        dynamicViewViewEntity.addAlias("PP", "price");
        dynamicViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        dynamicViewViewEntity.addAlias("PCS", "isDel1", "isDel", null, false, false, null);
        dynamicViewViewEntity.addAlias("PCS", "carCountryId");
        dynamicViewViewEntity.addViewLink("P", "PCS", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        dynamicViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        dynamicViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        dynamicViewViewEntity.addViewLink("P", "PCM", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        conditions.add(EntityCondition.makeCondition("carTypeId", "IMPORTED_CAR"));
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("isDel1", "N"));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        if (UtilValidate.isNotEmpty(productBrandId)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", productBrandId));
        }
        if (UtilValidate.isNotEmpty(productCarSeriesId)) {
            conditions.add(EntityCondition.makeCondition("productCarSeriesId", productCarSeriesId));
        }
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        if (UtilValidate.isNotEmpty(countryId)) {
            conditions.add(EntityCondition.makeCondition("carCountryId", countryId));
        }
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        if (UtilValidate.isNotEmpty(keyWord)) {
            conditions.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + keyWord + "%"));
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-introductionDate"), findOpts);
            List<GenericValue> cars = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue car : cars) {
                Map map = FastMap.newInstance();
                map.put("productId", car.get("productId"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", car.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                map.put("productName", car.get("productName"));
                BigDecimal productPrice = car.getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                map.put("productPrice", productPrice);
                List<GenericValue> productPriceSaleList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", car.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                if (UtilValidate.isNotEmpty(productPriceSaleList)) {
                    BigDecimal marketPrice = productPriceSaleList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    map.put("marketPrice", marketPrice);
                }
                //销量
                GenericValue productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", car.get("productId")));
                int saleNum = 0;
                if (UtilValidate.isNotEmpty(productCalculatedInfo) && UtilValidate.isNotEmpty(productCalculatedInfo.get("totalQuantityOrdered"))) {
                    saleNum = productCalculatedInfo.getBigDecimal("totalQuantityOrdered").intValue();
                }
                map.put("saleNum", saleNum);
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 查询二手车推荐车辆接口
     *
     * @param request
     * @param response
     * @return
     */
    public static String getRecommendCar(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("P", "Product");
        dynamicViewViewEntity.addAlias("P", "productId");
        dynamicViewViewEntity.addAlias("P", "productName");
        dynamicViewViewEntity.addAlias("P", "isOnline");
        dynamicViewViewEntity.addAlias("P", "isVerify");
        dynamicViewViewEntity.addAlias("P", "isDel");
        dynamicViewViewEntity.addAlias("P", "carTypeId");
        dynamicViewViewEntity.addAlias("P", "productTypeId");
        dynamicViewViewEntity.addAlias("P", "isRecommend");
        dynamicViewViewEntity.addAlias("P", "mainProductId");
        dynamicViewViewEntity.addAlias("P", "introductionDate");
        dynamicViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewViewEntity.addAlias("P", "createdStamp");
        dynamicViewViewEntity.addAlias("P", "carMileage");
        dynamicViewViewEntity.addAlias("P", "useTime");
        dynamicViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        dynamicViewViewEntity.addAlias("PCM", "productCarModelCollocationId");
        dynamicViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        dynamicViewViewEntity.addViewLink("P", "PCM", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        conditions.add(EntityCondition.makeCondition("carTypeId", "USED_CAR"));
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));
        conditions.add(EntityCondition.makeCondition("isRecommend", "Y"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-introductionDate"), findOpts);
            List<GenericValue> cars = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue car : cars) {
                Map map = FastMap.newInstance();
                map.put("productId", car.get("productId"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", car.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                map.put("productName", car.get("productName"));
                List<GenericValue> productPriceSaleList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", car.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                if (UtilValidate.isNotEmpty(productPriceSaleList)) {
                    BigDecimal productPrice = productPriceSaleList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    map.put("productPrice", productPrice);
                }
                GenericValue curProductCarModelCollocationIn = delegator.findByPrimaryKey("ProductCarModelCollocation", UtilMisc.toMap("productCarModelCollocationId", car.get("productCarModelCollocationId")));
                if (UtilValidate.isNotEmpty(curProductCarModelCollocationIn) && UtilValidate.isNotEmpty(curProductCarModelCollocationIn.get("bpGuidingPrice"))) {
                    BigDecimal guidePrice = curProductCarModelCollocationIn.getBigDecimal("bpGuidingPrice").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    map.put("guidePrice", guidePrice);
                }
                map.put("carMileage", car.get("carMileage"));
                if (UtilValidate.isNotEmpty(car.get("useTime"))) {
                    map.put("useTime", car.getString("useTime").split("-")[0] + "年");
                }
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 二手车列表获取
     *
     * @param request
     * @param response
     * @return
     */
    public static String getUsedCar(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        //排量
        String carDisplacementIds = request.getParameter("carDisplacementIds");
        if (UtilValidate.isEmpty(carDisplacementIds) && UtilValidate.isNotEmpty(jsonObject.get("carDisplacementIds"))) {
            carDisplacementIds = jsonObject.getString("carDisplacementIds");
        }
        //级别
        String carLevel = request.getParameter("carLevel");
        if (UtilValidate.isEmpty(carLevel) && UtilValidate.isNotEmpty(jsonObject.get("carLevel"))) {
            carLevel = jsonObject.getString("carLevel");
        }
        //品牌
        String productBrandId = request.getParameter("productBrandId");
        if (UtilValidate.isEmpty(productBrandId) && UtilValidate.isNotEmpty(jsonObject.get("productBrandId"))) {
            productBrandId = jsonObject.getString("productBrandId");
        }
        //车系
        String productCarSeriesId = request.getParameter("productCarSeriesId");
        if (UtilValidate.isEmpty(productCarSeriesId) && UtilValidate.isNotEmpty(jsonObject.get("productCarSeriesId"))) {
            productCarSeriesId = jsonObject.getString("productCarSeriesId");
        }
        //最低价格，单位元
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        //最低车龄
        String useTimeLow = request.getParameter("useTimeLow");
        if (UtilValidate.isEmpty(useTimeLow) && UtilValidate.isNotEmpty(jsonObject.get("useTimeLow"))) {
            useTimeLow = jsonObject.getString("useTimeLow");
        }
        //最高车龄
        String useTimeHigh = request.getParameter("useTimeHigh");
        if (UtilValidate.isEmpty(useTimeHigh) && UtilValidate.isNotEmpty(jsonObject.get("useTimeHigh"))) {
            useTimeHigh = jsonObject.getString("useTimeHigh");
        }
        //最低里程，单位万公里
        String carMileageLow = request.getParameter("carMileageLow");
        if (UtilValidate.isEmpty(carMileageLow) && UtilValidate.isNotEmpty(jsonObject.get("carMileageLow"))) {
            carMileageLow = jsonObject.getString("carMileageLow");
        }
        //最高里程
        String carMileageHigh = request.getParameter("carMileageHigh");
        if (UtilValidate.isEmpty(carMileageHigh) && UtilValidate.isNotEmpty(jsonObject.get("carMileageHigh"))) {
            carMileageHigh = jsonObject.getString("carMileageHigh");
        }
        //关键字
        String keyWord = request.getParameter("keyWord");
        if (UtilValidate.isEmpty(keyWord) && UtilValidate.isNotEmpty(jsonObject.get("keyWord"))) {
            keyWord = jsonObject.getString("keyWord");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("P", "Product");
        dynamicViewViewEntity.addAlias("P", "productId");
        dynamicViewViewEntity.addAlias("P", "productName");
        dynamicViewViewEntity.addAlias("P", "isOnline");
        dynamicViewViewEntity.addAlias("P", "isVerify");
        dynamicViewViewEntity.addAlias("P", "isDel");
        dynamicViewViewEntity.addAlias("P", "carTypeId");
        dynamicViewViewEntity.addAlias("P", "productTypeId");
        dynamicViewViewEntity.addAlias("P", "mainProductId");
        dynamicViewViewEntity.addAlias("P", "productBrandId");
        dynamicViewViewEntity.addAlias("P", "productCarSeriesId");
        dynamicViewViewEntity.addAlias("P", "productCarModelId");
        dynamicViewViewEntity.addAlias("P", "introductionDate");
        dynamicViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewViewEntity.addAlias("P", "useTime");
        dynamicViewViewEntity.addAlias("P", "carMileage");
        dynamicViewViewEntity.addAlias("P", "createdStamp");
        dynamicViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PP", "ProductPrice");
        dynamicViewViewEntity.addAlias("PP", "productPriceTypeId");
        dynamicViewViewEntity.addAlias("PP", "price");
        dynamicViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        dynamicViewViewEntity.addAlias("PCS", "isDel1", "isDel", null, false, false, null);
        dynamicViewViewEntity.addAlias("PCS", "carLevelId");
        dynamicViewViewEntity.addViewLink("P", "PCS", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        dynamicViewViewEntity.addMemberEntity("E", "Enumeration");
        dynamicViewViewEntity.addAlias("E", "description");
        dynamicViewViewEntity.addViewLink("PCS", "E", true, ModelKeyMap.makeKeyMapList("carLevelId", "enumId"));
        dynamicViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        dynamicViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        dynamicViewViewEntity.addAlias("PCM", "carDisplacementId");
        dynamicViewViewEntity.addViewLink("P", "PCM", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        conditions.add(EntityCondition.makeCondition("carTypeId", "USED_CAR"));
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("isDel1", "N"));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        //品牌
        if (UtilValidate.isNotEmpty(productBrandId)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", productBrandId));
        }
        //车系
        if (UtilValidate.isNotEmpty(productCarSeriesId)) {
            conditions.add(EntityCondition.makeCondition("productCarSeriesId", productCarSeriesId));
        }
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));
        //最低价
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        //最高价
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        //排量
        if (UtilValidate.isNotEmpty(carDisplacementIds)) {
            conditions.add(EntityCondition.makeCondition("carDisplacementId", EntityOperator.IN, UtilMisc.toListArray(carDisplacementIds.split(","))));
        }
        //级别
        if (UtilValidate.isNotEmpty(carLevel)) {
            conditions.add(EntityCondition.makeCondition("description", EntityOperator.LIKE, "%" + carLevel + "%"));
        }
        //最低车龄
        if (UtilValidate.isNotEmpty(useTimeLow)) {
            String timeLow = UtilDateTime.timeStampToString(UtilDateTime.adjustTimestamp(UtilDateTime.nowTimestamp(), Calendar.YEAR, -Integer.valueOf(useTimeLow)), "yyyy-MM", timeZone, locale);
            conditions.add(EntityCondition.makeCondition("useTime", EntityOperator.LESS_THAN_EQUAL_TO, timeLow));
        }
        //最高车龄
        if (UtilValidate.isNotEmpty(useTimeHigh)) {
            String timeHigh = UtilDateTime.timeStampToString(UtilDateTime.adjustTimestamp(UtilDateTime.nowTimestamp(), Calendar.YEAR, -Integer.valueOf(useTimeHigh)), "yyyy-MM", timeZone, locale);
            conditions.add(EntityCondition.makeCondition("useTime", EntityOperator.GREATER_THAN_EQUAL_TO, timeHigh));
        }
        //最低里程
        if (UtilValidate.isNotEmpty(carMileageLow)) {
            conditions.add(EntityCondition.makeCondition("carMileage", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(carMileageLow).multiply(new BigDecimal(10000))));
        }
        //最高里程
        if (UtilValidate.isNotEmpty(carMileageHigh)) {
            conditions.add(EntityCondition.makeCondition("carMileage", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(carMileageHigh).multiply(new BigDecimal(10000))));
        }
        //关键字
        if (UtilValidate.isNotEmpty(keyWord)) {
            conditions.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + keyWord + "%"));
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-introductionDate"), findOpts);
            List<GenericValue> cars = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue car : cars) {
                Map map = FastMap.newInstance();
                map.put("productId", car.get("productId"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", car.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                map.put("productName", car.get("productName"));
                BigDecimal productPrice = car.getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                map.put("productPrice", productPrice);
                //该车型的新车
                if (UtilValidate.isNotEmpty(car.get("productCarModelId"))) {
                    DynamicViewEntity carModelViewEntity = new DynamicViewEntity();
                    carModelViewEntity.addMemberEntity("P", "Product");
                    carModelViewEntity.addAlias("P", "productId");
                    carModelViewEntity.addAlias("P", "isOnline");
                    carModelViewEntity.addAlias("P", "isVerify");
                    carModelViewEntity.addAlias("P", "isDel");
                    carModelViewEntity.addAlias("P", "isRecommend");
                    carModelViewEntity.addAlias("P", "introductionDate");
                    carModelViewEntity.addAlias("P", "salesDiscontinuationDate");
                    carModelViewEntity.addAlias("P", "productTypeId");
                    carModelViewEntity.addAlias("P", "carTypeId");
                    carModelViewEntity.addAlias("P", "mainProductId");
                    carModelViewEntity.addAlias("P", "productCarModelId");
                    carModelViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
                    carModelViewEntity.addAlias("PSPA", "productStoreId");
                    carModelViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
                    carModelViewEntity.addMemberEntity("PP", "ProductPrice");
                    carModelViewEntity.addAlias("PP", "price");
                    carModelViewEntity.addAlias("PP", "productPriceTypeId");
                    carModelViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
                    List<EntityCondition> carModelConditions = FastList.newInstance();
                    carModelConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
                    carModelConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
                    carModelConditions.add(EntityCondition.makeCondition("isDel", "N"));
                    carModelConditions.add(EntityCondition.makeCondition("isRecommend", "Y"));
                    carModelConditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));
                    carModelConditions.add(EntityCondition.makeCondition("carTypeId", "NEW_CAR"));
                    carModelConditions.add(EntityCondition.makeCondition("mainProductId", null));
                    carModelConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                    List<EntityCondition> list1 = FastList.newInstance();
                    list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
                    list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
                    carModelConditions.add(EntityCondition.makeCondition(list1, EntityOperator.OR));
                    carModelConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
                    carModelConditions.add(EntityCondition.makeCondition("productCarModelId", car.get("productCarModelId")));
                    carModelConditions.add(EntityCondition.makeCondition("productPriceTypeId", "MARKET_PRICE"));
                    beganTransaction = TransactionUtil.begin();
                    eli = delegator.findListIteratorByCondition(carModelViewEntity, EntityCondition.makeCondition(carModelConditions, EntityOperator.AND), null, null, null, null);
                    List<GenericValue> productCarModels = eli.getCompleteList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    if (UtilValidate.isNotEmpty(productCarModels)) {
                        BigDecimal marketPrice = productCarModels.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                        map.put("marketPrice", marketPrice);
                    }
                }
                map.put("carMileage", UtilValidate.isNotEmpty(car.get("carMileage")) ? car.getBigDecimal("carMileage").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP) : "");
                map.put("useTime", car.get("useTime"));
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 保存浏览历史记录
     *
     * @param request
     * @param response
     * @return
     */
    public static String savePartyBrowse(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productIds = request.getParameter("productIds");
        if (UtilValidate.isEmpty(productIds) && UtilValidate.isNotEmpty(jsonObject.get("productIds"))) {
            productIds = jsonObject.getString("productIds");
        }
        if (UtilValidate.isEmpty(productIds)) {
            request.setAttribute("error", "车辆ID不能为空");
            response.setStatus(403);
            return "error";
        }
        for (String productId : productIds.split(",")) {
            try {
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                if (UtilValidate.isEmpty(product)) {
                    continue;
                }
                GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
                //删除重复数据
                List<GenericValue> historys = delegator.findByAnd("PartyBrowseHistory", UtilMisc.toMap("partyId", userLogin.get("partyId"), "productId", product.get("productId")));
                if (UtilValidate.isNotEmpty(historys)) {
                    delegator.removeAll(historys);
                }
                //删除超量的数据，最终保留10条
                List<GenericValue> partyBrowseHistorys = delegator.findList("PartyBrowseHistory", EntityCondition.makeCondition("partyId", userLogin.get("partyId")), null, UtilMisc.toList("createdStamp"), null, false);
                if (UtilValidate.isNotEmpty(partyBrowseHistorys) && partyBrowseHistorys.size() > 9) {
                    do {
                        delegator.removeByPrimaryKey(partyBrowseHistorys.get(0).getPrimaryKey());
                        partyBrowseHistorys.remove(0);
                    } while (partyBrowseHistorys.size() > 9);
                }
                GenericValue partyBrowseHistory = delegator.makeValue("PartyBrowseHistory");
                partyBrowseHistory.set("partyBrowseHistoryId", delegator.getNextSeqId("PartyBrowseHistory"));
                partyBrowseHistory.set("partyId", userLogin.get("partyId"));
                partyBrowseHistory.set("productId", productId);
                delegator.create(partyBrowseHistory);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        return "success";
    }

    /**
     * 根据车型查找二手车
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarByModel(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String productCarModelId = request.getParameter("productCarModelId");
        if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        if (UtilValidate.isEmpty(productCarModelId)) {
            request.setAttribute("error", "商品ID不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("P", "Product");
        dynamicViewViewEntity.addAlias("P", "productId");
        dynamicViewViewEntity.addAlias("P", "productName");
        dynamicViewViewEntity.addAlias("P", "isOnline");
        dynamicViewViewEntity.addAlias("P", "isVerify");
        dynamicViewViewEntity.addAlias("P", "isDel");
        dynamicViewViewEntity.addAlias("P", "carTypeId");
        dynamicViewViewEntity.addAlias("P", "productTypeId");
        dynamicViewViewEntity.addAlias("P", "mainProductId");
        dynamicViewViewEntity.addAlias("P", "productCarModelId");
        dynamicViewViewEntity.addAlias("P", "introductionDate");
        dynamicViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewViewEntity.addAlias("P", "createdStamp");
        dynamicViewViewEntity.addAlias("P", "carMileage");
        dynamicViewViewEntity.addAlias("P", "useTime");
        dynamicViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        dynamicViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        dynamicViewViewEntity.addViewLink("P", "PCM", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        conditions.add(EntityCondition.makeCondition("carTypeId", "USED_CAR"));
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-introductionDate"), findOpts);
            List<GenericValue> cars = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue car : cars) {
                Map map = FastMap.newInstance();
                map.put("productId", car.get("productId"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", car.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                map.put("productName", car.get("productName"));
                List<GenericValue> productPriceSaleList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", car.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                if (UtilValidate.isNotEmpty(productPriceSaleList)) {
                    BigDecimal productPrice = productPriceSaleList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    map.put("productPrice", productPrice);
                    map.put("guidePrice", productPrice);//TODO:车型库的指导价
                }
                map.put("carMileage", car.get("carMileage"));
                if (UtilValidate.isNotEmpty(car.get("useTime"))) {
                    map.put("useTime", car.getString("useTime").split("-")[0] + "年");
                }
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取资讯类型
     *
     * @param request
     * @param response
     * @return
     */
    public static String getNewsType(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            List<Map> newsTypeList = FastList.newInstance();
            List<GenericValue> articleTypes = delegator.findByAnd("ArticleType", UtilMisc.toMap("parentTypeId", "NEWS"));
            for (GenericValue articleType : articleTypes) {
                Map map = FastMap.newInstance();
                map.put("articleTypeId", articleType.get("articleTypeId"));
                map.put("description", articleType.get("description"));
                newsTypeList.add(map);
            }
            request.setAttribute("newsTypeList", newsTypeList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取资讯列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getNewsList(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String articleTypeId = request.getParameter("articleTypeId");
        if (UtilValidate.isEmpty(articleTypeId) && UtilValidate.isNotEmpty(jsonObject.get("articleTypeId"))) {
            articleTypeId = jsonObject.getString("articleTypeId");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("A", "Article");
        dynamicViewViewEntity.addAlias("A", "articleId");
        dynamicViewViewEntity.addAlias("A", "articleTitle");
        dynamicViewViewEntity.addAlias("A", "articleTypeId");
        dynamicViewViewEntity.addAlias("A", "articleStatus");
        dynamicViewViewEntity.addAlias("A", "lastUpdatedStamp");
        List<EntityCondition> conditions = FastList.newInstance();
        if (UtilValidate.isNotEmpty(articleTypeId)) {
            conditions.add(EntityCondition.makeCondition("articleTypeId", articleTypeId));
        } else {
            List<GenericValue> articleTypes = null;
            try {
                articleTypes = delegator.findByAnd("ArticleType", UtilMisc.toMap("parentTypeId", "NEWS"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(articleTypes)) {
                List<String> articleTypeList = FastList.newInstance();
                for (GenericValue articleType : articleTypes) {
                    articleTypeList.add(articleType.getString("articleTypeId"));
                }
                articleTypeList.add("NEWS");
                conditions.add(EntityCondition.makeCondition("articleTypeId", EntityOperator.IN, articleTypeList));
            } else {
                request.setAttribute("resultList", null);
                request.setAttribute("max", 0);
                return "success";
            }
        }
        conditions.add(EntityCondition.makeCondition("articleStatus", "2"));
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-lastUpdatedStamp"), findOpts);
            List<GenericValue> articles = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue article : articles) {
                Map map = FastMap.newInstance();
                map.put("articleId", article.get("articleId"));
                List<GenericValue> articleContents = delegator.findByAnd("ArticleContent", UtilMisc.toMap("articleId", article.get("articleId"), "articleContentTypeId", "ARTICLE_FIGURE"));
                if (UtilValidate.isNotEmpty(articleContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + articleContents.get(0).get("contentId"));
                }
                map.put("articleTitle", article.get("articleTitle"));
                List<GenericValue> articleTagAssocs = delegator.findByAnd("ArticleTagAssoc", UtilMisc.toMap("articleId", article.get("articleId")));
                if (UtilValidate.isNotEmpty(articleTagAssocs)) {
                    List<String> tagNames = FastList.newInstance();
                    for (GenericValue articleTagAssoc : articleTagAssocs) {
                        GenericValue tag = delegator.findByPrimaryKey("Tag", UtilMisc.toMap("tagId", articleTagAssoc.get("tagId")));
                        tagNames.add(tag.getString("tagName"));
                    }
                    map.put("tagNames", tagNames);
                }
                map.put("publishTime", UtilDateTime.timeStampToString(article.getTimestamp("lastUpdatedStamp"), "MM-dd", timeZone, locale));
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 查询资讯内容
     *
     * @param request
     * @param response
     * @return
     */
    public static String getNewsDetail(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String articleId = request.getParameter("articleId");
        if (UtilValidate.isEmpty(articleId) && UtilValidate.isNotEmpty(jsonObject.get("articleId"))) {
            articleId = jsonObject.getString("articleId");
        }
        if (UtilValidate.isEmpty(articleId)) {
            request.setAttribute("error", "资讯ID不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            GenericValue article = delegator.findByPrimaryKey("Article", UtilMisc.toMap("articleId", articleId));
            if (UtilValidate.isNotEmpty(article)) {
                request.setAttribute("articleTitle", article.get("articleTitle"));
                request.setAttribute("articleAuthor", article.get("articleAuthor"));
                request.setAttribute("publishTime", UtilDateTime.timeStampToString(article.getTimestamp("lastUpdatedStamp"), "yyyy-MM-dd", timeZone, locale));
                // 查询文章内容
                List<GenericValue> articleConContent = delegator.findByAnd("ArticleContent", UtilMisc.toMap("articleId", articleId, "articleContentTypeId", "ARTICLE_CONTENT"));
                if (UtilValidate.isNotEmpty(articleConContent)) {
                    GenericValue content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", articleConContent.get(0).get("contentId")));
                    if (UtilValidate.isNotEmpty(content)) {
                        GenericValue contenttext = delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                        if (UtilValidate.isNotEmpty(contenttext)) {
                            request.setAttribute("articleContent", UtilValidate.isNotEmpty(contenttext.get("textData")) ? contenttext.getString("textData").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 根据车系查询车型
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarModel(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productCarSeriesId = request.getParameter("productCarSeriesId");
        if (UtilValidate.isEmpty(productCarSeriesId) && UtilValidate.isNotEmpty(jsonObject.get("productCarSeriesId"))) {
            productCarSeriesId = jsonObject.getString("productCarSeriesId");
        }
        if (UtilValidate.isEmpty(productCarSeriesId)) {
            request.setAttribute("error", "车系ID不能为空");
            response.setStatus(403);
            return "error";
        }
        DynamicViewEntity yearViewEntity = new DynamicViewEntity();
        yearViewEntity.addMemberEntity("PCM", "ProductCarModel");
        yearViewEntity.addAlias("PCM", "productCarSeriesId");
        yearViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        yearViewEntity.addAlias("PCM", "carYearId", "carYearId", null, false, true, null);
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("productCarSeriesId", productCarSeriesId));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(yearViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-carYearId"), null);
            List<GenericValue> years = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue year : years) {
                Map yearMap = FastMap.newInstance();
                yearMap.put("year", year.get("carYearId"));
                List<Map> carModels = FastList.newInstance();
                List<GenericValue> productCarModels = delegator.findByAnd("ProductCarModel", UtilMisc.toMap("productCarSeriesId", productCarSeriesId, "carYearId", year.get("carYearId"), "isDel", "N"));
                for (GenericValue productCarModel : productCarModels) {
                    Map map = FastMap.newInstance();
                    map.put("productCarModelId", productCarModel.get("productCarModelId"));
                    map.put("carModelName", productCarModel.get("carModelName"));
                    carModels.add(map);
                }
                yearMap.put("carModels", carModels);
                resultList.add(yearMap);
            }
            request.setAttribute("resultList", resultList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取小保养项目
     *
     * @param request
     * @param response
     * @return
     */
    public static String getMaintainInfo(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String productCarModelId = request.getParameter("productCarModelId");
        if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        if (UtilValidate.isEmpty(productCarModelId)) {
            request.setAttribute("error", "车型ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String carMileage = request.getParameter("carMileage");
        if (UtilValidate.isEmpty(carMileage) && UtilValidate.isNotEmpty(jsonObject.get("carMileage"))) {
            carMileage = jsonObject.getString("carMileage");
        }
        String useTime = request.getParameter("useTime");
        if (UtilValidate.isEmpty(useTime) && UtilValidate.isNotEmpty(jsonObject.get("useTime"))) {
            useTime = jsonObject.getString("useTime");
        }
        int month = 0;
        if (UtilValidate.isNotEmpty(useTime)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
            Date start = null;
            try {
                start = df.parse(useTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date end = new Date();
            System.out.println("###start:==" + start);
            System.out.println("###end:==" + end);
            month = getMonth(start, end);
            System.out.println("###getMonth():=" + getMonth(start, end));
        }
        try {
            Boolean isAll = false;
            Boolean isUrgent = false;
            String maintainStandard = null;
            List<String> maintainTypeIds = FastList.newInstance();
            if (UtilValidate.isEmpty(carMileage) && UtilValidate.isEmpty(useTime)) {//行驶里程及上路时间都为空，则查该车型的所有小保养信息
                DynamicViewEntity maintainInfoView = new DynamicViewEntity();
                maintainInfoView.addMemberEntity("PCMI", "ProductCarMaintainInfo");
                maintainInfoView.addAlias("PCMI", "productCarMaintainInfoId");
                maintainInfoView.addAlias("PCMI", "productCarModelId");
                maintainInfoView.addAlias("PCMI", "maintainTypeId");
                maintainInfoView.addAlias("PCMI", "isDel");
                maintainInfoView.addAlias("PCMI", "isUsed1", "isUsed", null, false, false, null);
                maintainInfoView.addMemberEntity("MT", "MaintainType");
                maintainInfoView.addAlias("MT", "maintainTypeProject");
                maintainInfoView.addAlias("MT", "isUsed2", "isUsed", null, false, false, null);
                maintainInfoView.addViewLink("PCMI", "MT", false, ModelKeyMap.makeKeyMapList("maintainTypeId", "maintainTypeId"));
                List<EntityCondition> conditions = FastList.newInstance();
                conditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                conditions.add(EntityCondition.makeCondition("isDel", "N"));
                conditions.add(EntityCondition.makeCondition("isUsed1", "0"));
                conditions.add(EntityCondition.makeCondition("isUsed2", "0"));
                conditions.add(EntityCondition.makeCondition("maintainTypeProject", "1"));//小保养
                Boolean beganTransaction = TransactionUtil.begin();
                EntityListIterator eli = delegator.findListIteratorByCondition(maintainInfoView, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
                List<GenericValue> productCarMaintainInfos = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {
                    for (GenericValue productCarMaintainInfo : productCarMaintainInfos) {
                        maintainTypeIds.add(productCarMaintainInfo.getString("maintainTypeId"));
                    }
                    isAll = true;
                }
            } else if (UtilValidate.isEmpty(carMileage) && UtilValidate.isNotEmpty(useTime)) {//行驶里程为空，上路时间不为空，则查上路时间满足的档位的保养项目
                List<EntityCondition> referenceConditions = FastList.newInstance();
                referenceConditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions.add(EntityCondition.makeCondition("maintainMonth", EntityOperator.GREATER_THAN_EQUAL_TO, (long) month));
                List<GenericValue> maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions, EntityOperator.AND), null, UtilMisc.toList("maintainMonth"), null, false);//满足条件的档位
                String maintainReferenceId = null;
                if (UtilValidate.isEmpty(maintainReferences)) {
                    maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainMonth"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences)) {
                    maintainReferenceId = maintainReferences.get(0).getString("maintainReferenceId");
                    maintainStandard = maintainReferences.get(0).getLong("maintainKilometers").toString() + "km或" + maintainReferences.get(0).getLong("maintainMonth").toString() + "个月/次";
                }
                if (UtilValidate.isNotEmpty(maintainReferenceId)) {
                    List<GenericValue> maintainTypeReferences = delegator.findByAnd("MaintainTypeReference", UtilMisc.toMap("maintainReferenceId", maintainReferenceId));
                    if (UtilValidate.isNotEmpty(maintainTypeReferences)) {
                        for (GenericValue maintainTypeReference : maintainTypeReferences) {
                            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeId", maintainTypeReference.get("maintainTypeId"), "maintainTypeProject", "1", "isUsed", "0"));
                            if (UtilValidate.isNotEmpty(maintainTypes)) {//小保养，已启用
                                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainTypeReference.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {//已启用
                                    maintainTypeIds.add(maintainTypeReference.getString("maintainTypeId"));
                                }
                            }
                        }
                    }
                }
            } else if (UtilValidate.isNotEmpty(carMileage) && UtilValidate.isEmpty(useTime)) {//行驶里程不为空，上路时间为空，则查行驶里程满足的档位的保养项目
                List<EntityCondition> referenceConditions = FastList.newInstance();
                referenceConditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions.add(EntityCondition.makeCondition("maintainKilometers", EntityOperator.GREATER_THAN_EQUAL_TO, Long.valueOf(carMileage)));
                List<GenericValue> maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions, EntityOperator.AND), null, UtilMisc.toList("maintainKilometers"), null, false);//满足条件的档位
                String maintainReferenceId = null;
                if (UtilValidate.isEmpty(maintainReferences)) {
                    maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainKilometers"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences)) {
                    maintainReferenceId = maintainReferences.get(0).getString("maintainReferenceId");
                    maintainStandard = maintainReferences.get(0).getLong("maintainKilometers").toString() + "km或" + maintainReferences.get(0).getLong("maintainMonth").toString() + "个月/次";
                }
                if (UtilValidate.isNotEmpty(maintainReferenceId)) {
                    List<GenericValue> maintainTypeReferences = delegator.findByAnd("MaintainTypeReference", UtilMisc.toMap("maintainReferenceId", maintainReferenceId));
                    if (UtilValidate.isNotEmpty(maintainTypeReferences)) {
                        for (GenericValue maintainTypeReference : maintainTypeReferences) {
                            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeId", maintainTypeReference.get("maintainTypeId"), "maintainTypeProject", "1", "isUsed", "0"));
                            if (UtilValidate.isNotEmpty(maintainTypes)) {//小保养，已启用
                                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainTypeReference.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {//已启用
                                    maintainTypeIds.add(maintainTypeReference.getString("maintainTypeId"));
                                }
                            }
                        }
                    }
                }
            } else if (UtilValidate.isNotEmpty(carMileage) && UtilValidate.isNotEmpty(useTime)) {//行驶里程及上路时间都不为空，则查满足条件的档位高的保养项目
                //上路时间为条件
                List<EntityCondition> referenceConditions1 = FastList.newInstance();
                referenceConditions1.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions1.add(EntityCondition.makeCondition("maintainMonth", EntityOperator.GREATER_THAN_EQUAL_TO, (long) month));
                List<GenericValue> maintainReferences1 = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions1, EntityOperator.AND), null, UtilMisc.toList("maintainMonth"), null, false);//满足条件的档位
                GenericValue maintainReference1 = null;
                String maintainStandard1 = null;
                if (UtilValidate.isEmpty(maintainReferences1)) {
                    maintainReferences1 = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainMonth"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences1)) {
                    maintainReference1 = maintainReferences1.get(0);
                    maintainStandard1 = maintainReferences1.get(0).getLong("maintainKilometers").toString() + "km或" + maintainReferences1.get(0).getLong("maintainMonth").toString() + "个月/次";
                }
                //行驶里程为条件
                List<EntityCondition> referenceConditions2 = FastList.newInstance();
                referenceConditions2.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions2.add(EntityCondition.makeCondition("maintainKilometers", EntityOperator.GREATER_THAN_EQUAL_TO, Long.valueOf(carMileage)));
                List<GenericValue> maintainReferences2 = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions2, EntityOperator.AND), null, UtilMisc.toList("maintainKilometers"), null, false);//满足条件的档位
                GenericValue maintainReference2 = null;
                String maintainStandard2 = null;
                if (UtilValidate.isEmpty(maintainReferences2)) {
                    maintainReferences2 = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainKilometers"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences2)) {
                    maintainReference2 = maintainReferences2.get(0);
                    maintainStandard2 = maintainReferences2.get(0).getLong("maintainKilometers").toString() + "km或" + maintainReferences2.get(0).getLong("maintainMonth").toString() + "个月/次";
                }
                String maintainReferenceId = null;
                if (UtilValidate.isNotEmpty(maintainReference1) && UtilValidate.isNotEmpty(maintainReference2)) {
                    if (maintainReference1.getLong("maintainKilometers") < maintainReference2.getLong("maintainKilometers")) {//选取档位高的保养项目
                        maintainReferenceId = maintainReference2.getString("maintainReferenceId");
                        maintainStandard = maintainStandard2;
                    } else {
                        maintainReferenceId = maintainReference1.getString("maintainReferenceId");
                        maintainStandard = maintainStandard1;
                    }
                }
                if (UtilValidate.isNotEmpty(maintainReferenceId)) {
                    List<GenericValue> maintainTypeReferences = delegator.findByAnd("MaintainTypeReference", UtilMisc.toMap("maintainReferenceId", maintainReferenceId));
                    if (UtilValidate.isNotEmpty(maintainTypeReferences)) {
                        for (GenericValue maintainTypeReference : maintainTypeReferences) {
                            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeId", maintainTypeReference.get("maintainTypeId"), "maintainTypeProject", "1", "isUsed", "0"));
                            if (UtilValidate.isNotEmpty(maintainTypes)) {//小保养，已启用
                                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainTypeReference.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {//已启用
                                    maintainTypeIds.add(maintainTypeReference.getString("maintainTypeId"));
                                }
                            }
                        }
                    }
                }
            }

            List<Map> maintainInfoList = FastList.newInstance();
            //保养信息商品视图
            DynamicViewEntity maintainProductView = new DynamicViewEntity();
            maintainProductView.addMemberEntity("MP", "MaintainProduct");
            maintainProductView.addAlias("MP", "maintainProductId");
            maintainProductView.addAlias("MP", "productCarMaintainInfoId");
            maintainProductView.addAlias("MP", "sequenceNum");
            maintainProductView.addAlias("MP", "number");
            maintainProductView.addAlias("MP", "productId");
            maintainProductView.addAlias("MP", "parentProductId");
            maintainProductView.addMemberEntity("P", "Product");
            maintainProductView.addAlias("P", "productName");
            maintainProductView.addAlias("P", "isOnline");
            maintainProductView.addAlias("P", "isVerify");
            maintainProductView.addAlias("P", "isDel");
            maintainProductView.addAlias("P", "introductionDate");
            maintainProductView.addAlias("P", "salesDiscontinuationDate");
            maintainProductView.addAlias("P", "productTypeId");
            maintainProductView.addAlias("P", "mainProductId");
            maintainProductView.addAlias("P", "lastUpdatedStamp");
            maintainProductView.addViewLink("MP", "P", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            maintainProductView.addMemberEntity("PSPA", "ProductStoreProductAssoc");
            maintainProductView.addAlias("PSPA", "productStoreId");
            maintainProductView.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            List<EntityCondition> maintainProductConditions = FastList.newInstance();
            maintainProductConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
            maintainProductConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
            maintainProductConditions.add(EntityCondition.makeCondition("isDel", "N"));
            maintainProductConditions.add(EntityCondition.makeCondition("productTypeId", "PARETS_GOOD"));
            maintainProductConditions.add(EntityCondition.makeCondition("mainProductId", null));
            maintainProductConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            List<EntityCondition> list1 = FastList.newInstance();
            list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
            maintainProductConditions.add(EntityCondition.makeCondition(list1, EntityOperator.OR));
            maintainProductConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));

            //小保养
            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeProject", "1", "isUsed", "0"), UtilMisc.toList("sequenceNum"));
            for (GenericValue maintainType : maintainTypes) {
                Map maintainInfoMap = FastMap.newInstance();
                maintainInfoMap.put("maintainTypeId", maintainType.get("maintainTypeId"));
                maintainInfoMap.put("maintainTypeName", maintainType.get("maintainTypeName"));
                maintainInfoMap.put("maintainTypeImg", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + maintainType.get("contentId"));
                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainType.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {
                    maintainProductConditions.add(EntityCondition.makeCondition("productCarMaintainInfoId", productCarMaintainInfos.get(0).get("productCarMaintainInfoId")));
                    maintainInfoMap.put("productCarMaintainInfoId", productCarMaintainInfos.get(0).get("productCarMaintainInfoId"));

                    //主商品
                    List<Map> productList = FastList.newInstance();
                    maintainProductConditions.add(EntityCondition.makeCondition("parentProductId", null));
                    Boolean beganTransaction = TransactionUtil.begin();
                    EntityListIterator eli = delegator.findListIteratorByCondition(maintainProductView, EntityCondition.makeCondition(maintainProductConditions, EntityOperator.AND), null, null, UtilMisc.toList("sequenceNum"), null);
                    List<GenericValue> mainProducts = eli.getCompleteList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    if (UtilValidate.isNotEmpty(mainProducts) && maintainTypeIds.contains(maintainType.get("maintainTypeId")) && !isAll) {//主商品存在且该保养在满足条件的保养手册中
                        isUrgent = true;
                    }
                    for (GenericValue maintainProduct : mainProducts) {
                        Map productMap = FastMap.newInstance();
                        Map mainProductMap = FastMap.newInstance();
                        mainProductMap.put("productId", maintainProduct.get("productId"));
                        mainProductMap.put("quantity", maintainProduct.get("number"));
                        mainProductMap.put("productName", maintainProduct.get("productName"));
                        List<GenericValue> productPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", maintainProduct.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                        if (UtilValidate.isNotEmpty(productPriceList)) {
                            mainProductMap.put("defaultPrice", productPriceList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                        List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", maintainProduct.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                        if (UtilValidate.isNotEmpty(productContents)) {
                            mainProductMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                        }
                        productMap.put("mainProduct", mainProductMap);
                        maintainProductConditions.remove(EntityCondition.makeCondition("parentProductId", null));
                        //替代商品
                        List<Map> replaceProductList = FastList.newInstance();
                        maintainProductConditions.add(EntityCondition.makeCondition("parentProductId", EntityOperator.EQUALS, maintainProduct.get("maintainProductId")));
                        beganTransaction = TransactionUtil.begin();
                        eli = delegator.findListIteratorByCondition(maintainProductView, EntityCondition.makeCondition(maintainProductConditions, EntityOperator.AND), null, null, UtilMisc.toList("sequenceNum"), null);
                        List<GenericValue> replaceProducts = eli.getCompleteList();
                        eli.close();
                        TransactionUtil.commit(beganTransaction);
                        for (GenericValue replaceProduct : replaceProducts) {
                            Map productMap1 = FastMap.newInstance();
                            productMap1.put("productId", replaceProduct.get("productId"));
                            productMap1.put("quantity", replaceProduct.get("number"));
                            productMap1.put("productName", replaceProduct.get("productName"));
                            List<GenericValue> productPriceList1 = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", replaceProduct.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                            if (UtilValidate.isNotEmpty(productPriceList1)) {
                                productMap1.put("defaultPrice", productPriceList1.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                            }
                            List<GenericValue> productContents1 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", replaceProduct.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                            if (UtilValidate.isNotEmpty(productContents1)) {
                                productMap1.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents1.get(0).get("contentId"));
                            }
                            replaceProductList.add(productMap1);
                        }
                        productMap.put("replaceProductList", replaceProductList);
                        maintainProductConditions.remove(EntityCondition.makeCondition("parentProductId", EntityOperator.EQUALS, maintainProduct.get("maintainProductId")));
                        productList.add(productMap);
                    }
                    maintainInfoMap.put("productList", productList);

                    maintainProductConditions.remove(EntityCondition.makeCondition("productCarMaintainInfoId", productCarMaintainInfos.get(0).get("productCarMaintainInfoId")));
                }
                maintainInfoList.add(maintainInfoMap);
            }
            request.setAttribute("isUrgent", isUrgent);
            request.setAttribute("maintainStandard", maintainStandard);
            request.setAttribute("maintainInfoList", maintainInfoList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取其他保养项目
     *
     * @param request
     * @param response
     * @return
     */
    public static String getOtherMaintain(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String productCarModelId = request.getParameter("productCarModelId");
        if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        if (UtilValidate.isEmpty(productCarModelId)) {
            request.setAttribute("error", "车型ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String carMileage = request.getParameter("carMileage");
        if (UtilValidate.isEmpty(carMileage) && UtilValidate.isNotEmpty(jsonObject.get("carMileage"))) {
            carMileage = jsonObject.getString("carMileage");
        }
        String useTime = request.getParameter("useTime");
        if (UtilValidate.isEmpty(useTime) && UtilValidate.isNotEmpty(jsonObject.get("useTime"))) {
            useTime = jsonObject.getString("useTime");
        }
        int month = 0;
        if (UtilValidate.isNotEmpty(useTime)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
            Date start = null;
            try {
                start = df.parse(useTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date end = new Date();
            System.out.println("###start:==" + start);
            System.out.println("###end:==" + end);
            month = getMonth(start, end);
            System.out.println("###getMonth():=" + getMonth(start, end));
        }
        try {
            List<Map> maintainTypeIds = FastList.newInstance();
            if (UtilValidate.isEmpty(carMileage) && UtilValidate.isEmpty(useTime)) {//行驶里程及上路时间都为空，则查该车型的所有小保养信息
                DynamicViewEntity maintainInfoView = new DynamicViewEntity();
                maintainInfoView.addMemberEntity("PCMI", "ProductCarMaintainInfo");
                maintainInfoView.addAlias("PCMI", "productCarMaintainInfoId");
                maintainInfoView.addAlias("PCMI", "productCarModelId");
                maintainInfoView.addAlias("PCMI", "maintainTypeId");
                maintainInfoView.addAlias("PCMI", "isDel");
                maintainInfoView.addAlias("PCMI", "isUsed1", "isUsed", null, false, false, null);
                maintainInfoView.addMemberEntity("MT", "MaintainType");
                maintainInfoView.addAlias("MT", "maintainTypeProject");
                maintainInfoView.addAlias("MT", "isUsed2", "isUsed", null, false, false, null);
                maintainInfoView.addViewLink("PCMI", "MT", false, ModelKeyMap.makeKeyMapList("maintainTypeId", "maintainTypeId"));
                List<EntityCondition> conditions = FastList.newInstance();
                conditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                conditions.add(EntityCondition.makeCondition("isDel", "N"));
                conditions.add(EntityCondition.makeCondition("isUsed1", "0"));
                conditions.add(EntityCondition.makeCondition("isUsed2", "0"));
                conditions.add(EntityCondition.makeCondition("maintainTypeProject", "2"));//其他保养
                Boolean beganTransaction = TransactionUtil.begin();
                EntityListIterator eli = delegator.findListIteratorByCondition(maintainInfoView, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
                List<GenericValue> productCarMaintainInfos = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {
                    for (GenericValue productCarMaintainInfo : productCarMaintainInfos) {
                        maintainTypeIds.add(UtilMisc.toMap("maintainTypeId", productCarMaintainInfo.getString("maintainTypeId")));
                    }
                }
            } else if (UtilValidate.isEmpty(carMileage) && UtilValidate.isNotEmpty(useTime)) {//行驶里程为空，上路时间不为空，则查上路时间满足的档位的保养项目
                List<EntityCondition> referenceConditions = FastList.newInstance();
                referenceConditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions.add(EntityCondition.makeCondition("maintainMonth", EntityOperator.GREATER_THAN_EQUAL_TO, (long) month));
                List<GenericValue> maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions, EntityOperator.AND), null, UtilMisc.toList("maintainMonth"), null, false);//满足条件的档位
                String maintainReferenceId = null;
                if (UtilValidate.isEmpty(maintainReferences)) {
                    maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainMonth"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences)) {
                    maintainReferenceId = maintainReferences.get(0).getString("maintainReferenceId");
                }
                if (UtilValidate.isNotEmpty(maintainReferenceId)) {
                    List<GenericValue> maintainTypeReferences = delegator.findByAnd("MaintainTypeReference", UtilMisc.toMap("maintainReferenceId", maintainReferenceId));
                    if (UtilValidate.isNotEmpty(maintainTypeReferences)) {
                        for (GenericValue maintainTypeReference : maintainTypeReferences) {
                            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeId", maintainTypeReference.get("maintainTypeId"), "maintainTypeProject", "2", "isUsed", "0"));
                            if (UtilValidate.isNotEmpty(maintainTypes)) {//其他保养，已启用
                                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainTypeReference.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {//已启用
                                    maintainTypeIds.add(UtilMisc.toMap("maintainTypeId", maintainTypeReference.getString("maintainTypeId"), "maintainReferenceId", maintainTypeReference.getString("maintainReferenceId")));
                                }
                            }
                        }
                    }
                }
            } else if (UtilValidate.isNotEmpty(carMileage) && UtilValidate.isEmpty(useTime)) {//行驶里程不为空，上路时间为空，则查行驶里程满足的档位的保养项目
                List<EntityCondition> referenceConditions = FastList.newInstance();
                referenceConditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions.add(EntityCondition.makeCondition("maintainKilometers", EntityOperator.GREATER_THAN_EQUAL_TO, Long.valueOf(carMileage)));
                List<GenericValue> maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions, EntityOperator.AND), null, UtilMisc.toList("maintainKilometers"), null, false);//满足条件的档位
                String maintainReferenceId = null;
                if (UtilValidate.isEmpty(maintainReferences)) {
                    maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainKilometers"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences)) {
                    maintainReferenceId = maintainReferences.get(0).getString("maintainReferenceId");
                }
                if (UtilValidate.isNotEmpty(maintainReferenceId)) {
                    List<GenericValue> maintainTypeReferences = delegator.findByAnd("MaintainTypeReference", UtilMisc.toMap("maintainReferenceId", maintainReferenceId));
                    if (UtilValidate.isNotEmpty(maintainTypeReferences)) {
                        for (GenericValue maintainTypeReference : maintainTypeReferences) {
                            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeId", maintainTypeReference.get("maintainTypeId"), "maintainTypeProject", "2", "isUsed", "0"));
                            if (UtilValidate.isNotEmpty(maintainTypes)) {//其他保养，已启用
                                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainTypeReference.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {//已启用
                                    maintainTypeIds.add(UtilMisc.toMap("maintainTypeId", maintainTypeReference.getString("maintainTypeId"), "maintainReferenceId", maintainTypeReference.getString("maintainReferenceId")));
                                }
                            }
                        }
                    }
                }
            } else if (UtilValidate.isNotEmpty(carMileage) && UtilValidate.isNotEmpty(useTime)) {//行驶里程及上路时间都不为空，则查满足条件的档位高的保养项目
                //上路时间为条件
                List<EntityCondition> referenceConditions1 = FastList.newInstance();
                referenceConditions1.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions1.add(EntityCondition.makeCondition("maintainMonth", EntityOperator.GREATER_THAN_EQUAL_TO, (long) month));
                List<GenericValue> maintainReferences1 = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions1, EntityOperator.AND), null, UtilMisc.toList("maintainMonth"), null, false);//满足条件的档位
                GenericValue maintainReference1 = null;
                if (UtilValidate.isEmpty(maintainReferences1)) {
                    maintainReferences1 = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainMonth"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences1)) {
                    maintainReference1 = maintainReferences1.get(0);
                }
                //行驶里程为条件
                List<EntityCondition> referenceConditions2 = FastList.newInstance();
                referenceConditions2.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
                referenceConditions2.add(EntityCondition.makeCondition("maintainKilometers", EntityOperator.GREATER_THAN_EQUAL_TO, Long.valueOf(carMileage)));
                List<GenericValue> maintainReferences2 = delegator.findList("MaintainReference", EntityCondition.makeCondition(referenceConditions2, EntityOperator.AND), null, UtilMisc.toList("maintainKilometers"), null, false);//满足条件的档位
                GenericValue maintainReference2 = null;
                if (UtilValidate.isEmpty(maintainReferences2)) {
                    maintainReferences2 = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("-maintainKilometers"), null, false);//满足条件的档位
                }
                if (UtilValidate.isNotEmpty(maintainReferences2)) {
                    maintainReference2 = maintainReferences2.get(0);
                }
                String maintainReferenceId = null;
                if (UtilValidate.isNotEmpty(maintainReference1) && UtilValidate.isNotEmpty(maintainReference2)) {
                    if (maintainReference1.getLong("maintainKilometers") < maintainReference2.getLong("maintainKilometers")) {//选取档位高的保养项目
                        maintainReferenceId = maintainReference2.getString("maintainReferenceId");
                    } else {
                        maintainReferenceId = maintainReference1.getString("maintainReferenceId");
                    }
                }
                if (UtilValidate.isNotEmpty(maintainReferenceId)) {
                    List<GenericValue> maintainTypeReferences = delegator.findByAnd("MaintainTypeReference", UtilMisc.toMap("maintainReferenceId", maintainReferenceId));
                    if (UtilValidate.isNotEmpty(maintainTypeReferences)) {
                        for (GenericValue maintainTypeReference : maintainTypeReferences) {
                            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeId", maintainTypeReference.get("maintainTypeId"), "maintainTypeProject", "2", "isUsed", "0"));
                            if (UtilValidate.isNotEmpty(maintainTypes)) {//其他保养，已启用
                                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainTypeReference.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {//已启用
                                    maintainTypeIds.add(UtilMisc.toMap("maintainTypeId", maintainTypeReference.getString("maintainTypeId"), "maintainReferenceId", maintainTypeReference.getString("maintainReferenceId")));
                                }
                            }
                        }
                    }
                }
            }

            List<Map> maintainInfoList = FastList.newInstance();
            //保养信息商品视图
            DynamicViewEntity maintainProductView = new DynamicViewEntity();
            maintainProductView.addMemberEntity("MP", "MaintainProduct");
            maintainProductView.addAlias("MP", "maintainProductId");
            maintainProductView.addAlias("MP", "productCarMaintainInfoId");
            maintainProductView.addAlias("MP", "sequenceNum");
            maintainProductView.addAlias("MP", "number");
            maintainProductView.addAlias("MP", "productId");
            maintainProductView.addAlias("MP", "parentProductId");
            maintainProductView.addMemberEntity("P", "Product");
            maintainProductView.addAlias("P", "productName");
            maintainProductView.addAlias("P", "isOnline");
            maintainProductView.addAlias("P", "isVerify");
            maintainProductView.addAlias("P", "isDel");
            maintainProductView.addAlias("P", "introductionDate");
            maintainProductView.addAlias("P", "salesDiscontinuationDate");
            maintainProductView.addAlias("P", "productTypeId");
            maintainProductView.addAlias("P", "mainProductId");
            maintainProductView.addAlias("P", "lastUpdatedStamp");
            maintainProductView.addViewLink("MP", "P", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            maintainProductView.addMemberEntity("PSPA", "ProductStoreProductAssoc");
            maintainProductView.addAlias("PSPA", "productStoreId");
            maintainProductView.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            List<EntityCondition> maintainProductConditions = FastList.newInstance();
            maintainProductConditions.add(EntityCondition.makeCondition("isOnline", "Y"));
            maintainProductConditions.add(EntityCondition.makeCondition("isVerify", "Y"));
            maintainProductConditions.add(EntityCondition.makeCondition("isDel", "N"));
            maintainProductConditions.add(EntityCondition.makeCondition("productTypeId", "PARETS_GOOD"));
            maintainProductConditions.add(EntityCondition.makeCondition("mainProductId", null));
            maintainProductConditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            List<EntityCondition> list1 = FastList.newInstance();
            list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
            maintainProductConditions.add(EntityCondition.makeCondition(list1, EntityOperator.OR));
            maintainProductConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));

            //其他保养
            List<GenericValue> maintainTypes = delegator.findByAnd("MaintainType", UtilMisc.toMap("maintainTypeProject", "2", "isUsed", "0"), UtilMisc.toList("sequenceNum"));
            for (GenericValue maintainType : maintainTypes) {
                Map maintainInfoMap = FastMap.newInstance();
                maintainInfoMap.put("maintainTypeId", maintainType.get("maintainTypeId"));
                maintainInfoMap.put("maintainTypeName", maintainType.get("maintainTypeName"));
                maintainInfoMap.put("maintainTypeImg", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + maintainType.get("contentId"));
                List<GenericValue> productCarMaintainInfos = delegator.findByAnd("ProductCarMaintainInfo", UtilMisc.toMap("productCarModelId", productCarModelId, "maintainTypeId", maintainType.get("maintainTypeId"), "isUsed", "0", "isDel", "N"));
                if (UtilValidate.isNotEmpty(productCarMaintainInfos)) {
                    maintainProductConditions.add(EntityCondition.makeCondition("productCarMaintainInfoId", productCarMaintainInfos.get(0).get("productCarMaintainInfoId")));
                    maintainInfoMap.put("productCarMaintainInfoId", productCarMaintainInfos.get(0).get("productCarMaintainInfoId"));

                    //主商品
                    List<Map> productList = FastList.newInstance();
                    maintainProductConditions.add(EntityCondition.makeCondition("parentProductId", null));
                    Boolean beganTransaction = TransactionUtil.begin();
                    EntityListIterator eli = delegator.findListIteratorByCondition(maintainProductView, EntityCondition.makeCondition(maintainProductConditions, EntityOperator.AND), null, null, UtilMisc.toList("sequenceNum"), null);
                    List<GenericValue> mainProducts = eli.getCompleteList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    Boolean isUrgent = false;
                    String maintainStandard = "";
                    if (UtilValidate.isNotEmpty(mainProducts)) {//主商品存在
                        for (Map maintainTypeId : maintainTypeIds) {
                            if (UtilValidate.areEqual(maintainTypeId.get("maintainTypeId"), maintainType.get("maintainTypeId"))) {//该保养在满足条件的保养手册中
                                if (UtilValidate.isNotEmpty(maintainTypeId.get("maintainReferenceId"))) {
                                    isUrgent = true;
                                    GenericValue maintainReference = delegator.findByPrimaryKey("MaintainReference", UtilMisc.toMap("maintainReferenceId", maintainTypeId.get("maintainReferenceId")));
                                    maintainStandard = maintainReference.getLong("maintainKilometers").toString() + "km或" + maintainReference.getLong("maintainMonth").toString() + "个月/次";
                                }
                            }
                        }
                    }
                    maintainInfoMap.put("isUrgent", isUrgent);
                    maintainInfoMap.put("maintainStandard", maintainStandard);

                    for (GenericValue maintainProduct : mainProducts) {
                        Map productMap = FastMap.newInstance();
                        Map mainProductMap = FastMap.newInstance();
                        mainProductMap.put("productId", maintainProduct.get("productId"));
                        mainProductMap.put("quantity", maintainProduct.get("number"));
                        mainProductMap.put("productName", maintainProduct.get("productName"));
                        List<GenericValue> productPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", maintainProduct.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                        if (UtilValidate.isNotEmpty(productPriceList)) {
                            mainProductMap.put("defaultPrice", productPriceList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                        }
                        List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", maintainProduct.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                        if (UtilValidate.isNotEmpty(productContents)) {
                            mainProductMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                        }
                        productMap.put("mainProduct", mainProductMap);
                        maintainProductConditions.remove(EntityCondition.makeCondition("parentProductId", null));
                        //替代商品
                        List<Map> replaceProductList = FastList.newInstance();
                        maintainProductConditions.add(EntityCondition.makeCondition("parentProductId", EntityOperator.EQUALS, maintainProduct.get("maintainProductId")));
                        beganTransaction = TransactionUtil.begin();
                        eli = delegator.findListIteratorByCondition(maintainProductView, EntityCondition.makeCondition(maintainProductConditions, EntityOperator.AND), null, null, UtilMisc.toList("sequenceNum"), null);
                        List<GenericValue> replaceProducts = eli.getCompleteList();
                        eli.close();
                        TransactionUtil.commit(beganTransaction);
                        for (GenericValue replaceProduct : replaceProducts) {
                            Map productMap1 = FastMap.newInstance();
                            productMap1.put("productId", replaceProduct.get("productId"));
                            productMap1.put("quantity", replaceProduct.get("number"));
                            productMap1.put("productName", replaceProduct.get("productName"));
                            List<GenericValue> productPriceList1 = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", replaceProduct.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                            if (UtilValidate.isNotEmpty(productPriceList1)) {
                                productMap1.put("defaultPrice", productPriceList1.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP));
                            }
                            List<GenericValue> productContents1 = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", replaceProduct.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                            if (UtilValidate.isNotEmpty(productContents1)) {
                                productMap1.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents1.get(0).get("contentId"));
                            }
                            replaceProductList.add(productMap1);
                        }
                        productMap.put("replaceProductList", replaceProductList);
                        maintainProductConditions.remove(EntityCondition.makeCondition("parentProductId", EntityOperator.EQUALS, maintainProduct.get("maintainProductId")));
                        productList.add(productMap);
                    }
                    maintainInfoMap.put("productList", productList);

                    maintainProductConditions.remove(EntityCondition.makeCondition("productCarMaintainInfoId", productCarMaintainInfos.get(0).get("productCarMaintainInfoId")));
                }
                maintainInfoList.add(maintainInfoMap);
            }
            request.setAttribute("maintainInfoList", maintainInfoList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    public static int getMonth(Date start, Date end) {
        if (start.after(end)) {
            Date t = start;
            start = end;
            end = t;
        }
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);
        Calendar temp = Calendar.getInstance();
        temp.setTime(end);
        temp.add(Calendar.DATE, 1);

        int year = endCalendar.get(Calendar.YEAR)
                - startCalendar.get(Calendar.YEAR);
        int month = endCalendar.get(Calendar.MONTH)
                - startCalendar.get(Calendar.MONTH);

        if ((startCalendar.get(Calendar.DATE) == 1)
                && (temp.get(Calendar.DATE) == 1)) {
            return year * 12 + month + 1;
        } else if ((startCalendar.get(Calendar.DATE) != 1)
                && (temp.get(Calendar.DATE) == 1)) {
            return year * 12 + month;
        } else if ((startCalendar.get(Calendar.DATE) == 1)
                && (temp.get(Calendar.DATE) != 1)) {
            return year * 12 + month;
        } else {
            return (year * 12 + month - 1) < 0 ? 0 : (year * 12 + month);
        }
    }

    // 将月份加几个月
    public final static String addMonthsToDate(Date date, int months) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, months);// 加3个月

        c.set(Calendar.DAY_OF_MONTH, 1);// 设置月份的月初

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //设置时间格式
        String defaultStartDate = sdf.format(c.getTime()); //格式化前3月的时间

        return defaultStartDate;
    }

    /**
     * 获取可用时间段
     *
     * @param request
     * @param response
     * @return
     */
    public static String getMaintainServiceCharge(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String maintainDate = request.getParameter("maintainDate");
        if (UtilValidate.isEmpty(maintainDate) && UtilValidate.isNotEmpty(jsonObject.get("maintainDate"))) {
            maintainDate = jsonObject.getString("maintainDate");
        }
        if (UtilValidate.isEmpty(maintainDate)) {
            request.setAttribute("error", "时间不能为空");
            response.setStatus(403);
            return "error";
        }
        String productCarMaintainInfoIds = request.getParameter("productCarMaintainInfoIds");
        if (UtilValidate.isEmpty(productCarMaintainInfoIds) && UtilValidate.isNotEmpty(jsonObject.get("productCarMaintainInfoIds"))) {
            productCarMaintainInfoIds = jsonObject.getString("productCarMaintainInfoIds");
        }
        if (UtilValidate.isEmpty(productCarMaintainInfoIds)) {
            request.setAttribute("error", "保养项目编号不能为空");
            response.setStatus(403);
            return "error";
        }
        String[] productCarMaintainInfoIdList = productCarMaintainInfoIds.split(",");
        List<Map> list = FastList.newInstance();

        Map map1 = FastMap.newInstance();
        map1.put("time", "8:00-10:00");
        BigDecimal serviceCharge1 = BigDecimal.ZERO;
        List<Map> unitPriceList1 = FastList.newInstance();
        List<String> serviceNameList1 = FastList.newInstance();
        for (String productCarMaintainInfoId : productCarMaintainInfoIdList) {
            Map map = FastMap.newInstance();
            map.put("productCarMaintainInfoId", productCarMaintainInfoId);
            try {
                GenericValue productCarMaintainInfo = delegator.findByPrimaryKey("ProductCarMaintainInfo", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId));
                serviceNameList1.add(productCarMaintainInfo.getString("serviceChargeId"));
                List<GenericValue> maintainDifferentialPays = delegator.findByAnd("MaintainDifferentialPay", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId, "week", maintainDate, "time", "8:00-10:00"));
                if (UtilValidate.isNotEmpty(maintainDifferentialPays)) {
                    serviceCharge1 = serviceCharge1.add(maintainDifferentialPays.get(0).getBigDecimal("price"));
                    map.put("serviceCharge", maintainDifferentialPays.get(0).getBigDecimal("price"));
                } else {
                    serviceCharge1 = serviceCharge1.add(productCarMaintainInfo.getBigDecimal("serviceCharge"));
                    map.put("serviceCharge", productCarMaintainInfo.getBigDecimal("serviceCharge"));
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            unitPriceList1.add(map);
        }
        map1.put("serviceCharge", serviceCharge1);
        map1.put("unitPriceList", unitPriceList1);
        map1.put("serviceNameList", serviceNameList1);
        list.add(map1);

        Map map2 = FastMap.newInstance();
        map2.put("time", "10:00-12:00");
        BigDecimal serviceCharge2 = BigDecimal.ZERO;
        List<Map> unitPriceList2 = FastList.newInstance();
        List<String> serviceNameList2 = FastList.newInstance();
        for (String productCarMaintainInfoId : productCarMaintainInfoIdList) {
            Map map = FastMap.newInstance();
            map.put("productCarMaintainInfoId", productCarMaintainInfoId);
            try {
                GenericValue productCarMaintainInfo = delegator.findByPrimaryKey("ProductCarMaintainInfo", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId));
                serviceNameList2.add(productCarMaintainInfo.getString("serviceChargeId"));
                List<GenericValue> maintainDifferentialPays = delegator.findByAnd("MaintainDifferentialPay", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId, "week", maintainDate, "time", "10:00-12:00"));
                if (UtilValidate.isNotEmpty(maintainDifferentialPays)) {
                    serviceCharge2 = serviceCharge2.add(maintainDifferentialPays.get(0).getBigDecimal("price"));
                    map.put("serviceCharge", maintainDifferentialPays.get(0).getBigDecimal("price"));
                } else {
                    serviceCharge2 = serviceCharge2.add(productCarMaintainInfo.getBigDecimal("serviceCharge"));
                    map.put("serviceCharge", productCarMaintainInfo.getBigDecimal("serviceCharge"));
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            unitPriceList2.add(map);
        }
        map2.put("serviceCharge", serviceCharge2);
        map2.put("unitPriceList", unitPriceList2);
        map2.put("serviceNameList", serviceNameList2);
        list.add(map2);

        Map map3 = FastMap.newInstance();
        map3.put("time", "14:00-16:00");
        BigDecimal serviceCharge3 = BigDecimal.ZERO;
        List<Map> unitPriceList3 = FastList.newInstance();
        List<String> serviceNameList3 = FastList.newInstance();
        for (String productCarMaintainInfoId : productCarMaintainInfoIdList) {
            Map map = FastMap.newInstance();
            map.put("productCarMaintainInfoId", productCarMaintainInfoId);
            try {
                GenericValue productCarMaintainInfo = delegator.findByPrimaryKey("ProductCarMaintainInfo", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId));
                serviceNameList3.add(productCarMaintainInfo.getString("serviceChargeId"));
                List<GenericValue> maintainDifferentialPays = delegator.findByAnd("MaintainDifferentialPay", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId, "week", maintainDate, "time", "14:00-16:00"));
                if (UtilValidate.isNotEmpty(maintainDifferentialPays)) {
                    serviceCharge3 = serviceCharge3.add(maintainDifferentialPays.get(0).getBigDecimal("price"));
                    map.put("serviceCharge", maintainDifferentialPays.get(0).getBigDecimal("price"));
                } else {
                    serviceCharge3 = serviceCharge3.add(productCarMaintainInfo.getBigDecimal("serviceCharge"));
                    map.put("serviceCharge", productCarMaintainInfo.getBigDecimal("serviceCharge"));
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            unitPriceList3.add(map);
        }
        map3.put("serviceCharge", serviceCharge3);
        map3.put("unitPriceList", unitPriceList3);
        map3.put("serviceNameList", serviceNameList3);
        list.add(map3);

        Map map4 = FastMap.newInstance();
        map4.put("time", "16:00-18:00");
        BigDecimal serviceCharge4 = BigDecimal.ZERO;
        List<Map> unitPriceList4 = FastList.newInstance();
        List<String> serviceNameList4 = FastList.newInstance();
        for (String productCarMaintainInfoId : productCarMaintainInfoIdList) {
            Map map = FastMap.newInstance();
            map.put("productCarMaintainInfoId", productCarMaintainInfoId);
            try {
                GenericValue productCarMaintainInfo = delegator.findByPrimaryKey("ProductCarMaintainInfo", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId));
                serviceNameList4.add(productCarMaintainInfo.getString("serviceChargeId"));
                List<GenericValue> maintainDifferentialPays = delegator.findByAnd("MaintainDifferentialPay", UtilMisc.toMap("productCarMaintainInfoId", productCarMaintainInfoId, "week", maintainDate, "time", "16:00-18:00"));
                if (UtilValidate.isNotEmpty(maintainDifferentialPays)) {
                    serviceCharge4 = serviceCharge4.add(maintainDifferentialPays.get(0).getBigDecimal("price"));
                    map.put("serviceCharge", maintainDifferentialPays.get(0).getBigDecimal("price"));
                } else {
                    serviceCharge4 = serviceCharge4.add(productCarMaintainInfo.getBigDecimal("serviceCharge"));
                    map.put("serviceCharge", productCarMaintainInfo.getBigDecimal("serviceCharge"));
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            unitPriceList4.add(map);
        }
        map4.put("serviceCharge", serviceCharge4);
        map4.put("unitPriceList", unitPriceList4);
        map4.put("serviceNameList", serviceNameList4);
        list.add(map4);

        request.setAttribute("resultList", list);

        return "success";
    }

    /**
     * 获取社区店类型
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCommunityStoreType(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            List<GenericValue> enumerations = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "COMMUNITY_STORE_TYPE"), UtilMisc.toList("sequenceId"));
            List<Map> storeTypes = FastList.newInstance();
            storeTypes.add(UtilMisc.toMap("enumId", "", "description", "全部"));
            for (GenericValue enumeration : enumerations) {
                Map map = FastMap.newInstance();
                map.put("enumId", enumeration.get("enumId"));
                map.put("description", enumeration.get("description"));
                storeTypes.add(map);
            }
            request.setAttribute("storeTypeList", storeTypes);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取社区店列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCommunityStore(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String city = request.getParameter("city");
        if (UtilValidate.isEmpty(city) && UtilValidate.isNotEmpty(jsonObject.get("city"))) {
            city = jsonObject.getString("city");
        }
        String sortOrder = request.getParameter("sortOrder");
        if (UtilValidate.isEmpty(sortOrder) && UtilValidate.isNotEmpty(jsonObject.get("sortOrder"))) {
            sortOrder = jsonObject.getString("sortOrder");
        }
        String storeType = request.getParameter("storeType");
        if (UtilValidate.isEmpty(storeType) && UtilValidate.isNotEmpty(jsonObject.get("storeType"))) {
            storeType = jsonObject.getString("storeType");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("CS", "CommunityStore");
        dev.addAlias("CS", "commStoreId");
        dev.addAlias("CS", "storeName");
        dev.addAlias("CS", "isEnabled");
        dev.addAlias("CS", "iconUrl");
        dev.addAlias("CS", "storeType");
        dev.addAlias("CS", "payType");
        dev.addAlias("CS", "contactPhone");
        dev.addAlias("CS", "province");
        dev.addAlias("CS", "city");
        dev.addAlias("CS", "orderQuantity");
        dev.addAlias("CS", "isDelete");
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
        conditions.add(EntityCondition.makeCondition("isDelete", "N"));
        if (UtilValidate.isNotEmpty(city)) {
            List<EntityCondition> geoConditions = FastList.newInstance();
            geoConditions.add(EntityCondition.makeCondition("city", city));
            geoConditions.add(EntityCondition.makeCondition("province", city));
            conditions.add(EntityCondition.makeCondition(geoConditions, EntityOperator.OR));
        }
        if (UtilValidate.isNotEmpty(storeType)) {
            conditions.add(EntityCondition.makeCondition("storeType", storeType));
        }
        List<String> sort = FastList.newInstance();
        if (UtilValidate.isNotEmpty(sortOrder)) {
            sort.add(sortOrder);
        }

        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dev, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, sort, findOpts);
            List<GenericValue> results = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue result : results) {
                Map map = FastMap.newInstance();
                map.put("commStoreId", result.get("commStoreId"));
                map.put("storeName", result.get("storeName"));
                map.put("iconUrl", UtilValidate.isNotEmpty(result.get("iconUrl")) ? (request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + result.get("iconUrl")) : "");
                map.put("contactPhone", result.get("contactPhone"));
                map.put("orderQuantity", result.get("orderQuantity"));
                map.put("payType", UtilValidate.isNotEmpty(result.get("payType")) ? UtilMisc.toListArray(result.getString("payType").split(",")) : "");
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取社区店地区
     *
     * @param request
     * @param response
     * @return
     */
    public static String getStoreGeo(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("CS", "CommunityStore");
        dev.addAlias("CS", "isEnabled");
        dev.addAlias("CS", "city", "city", null, false, true, null);
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
        conditions.add(EntityCondition.makeCondition("city", EntityOperator.NOT_EQUAL, null));

        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dev, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
            List<GenericValue> results = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue result : results) {
                GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", result.get("city")));
                if (UtilValidate.isNotEmpty(geo)) {
                    Map map = FastMap.newInstance();
                    map.put("geoId", geo.get("geoId"));
                    map.put("geoName", geo.get("geoName"));
                    resultList.add(map);
                }
            }
            request.setAttribute("resultList", resultList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取保险公司
     *
     * @param request
     * @param response
     * @return
     */
    public static String getInsuranceBusiness(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            List<GenericValue> partyBusinessList = delegator.findByAnd("PartyBusiness", UtilMisc.toMap("auditStatus", "1", "businessType", "INSURANCE_BUSINESS"));
            List<Map> resultList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(partyBusinessList)) {
                for (GenericValue partyBusiness : partyBusinessList) {
                    Map map = FastMap.newInstance();
                    map.put("partyId", partyBusiness.get("partyId"));
                    map.put("logoImg", UtilValidate.isNotEmpty(partyBusiness.get("logoImg")) ? (request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + partyBusiness.get("logoImg")) : "");
                    resultList.add(map);
                }
            }
            request.setAttribute("resultList", resultList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取险种列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getInsuranceProduct(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("P", "Product");
        dynamicViewEntity.addAlias("P", "productId");
        dynamicViewEntity.addAlias("P", "isOnline");
        dynamicViewEntity.addAlias("P", "isVerify");
        dynamicViewEntity.addAlias("P", "isDel");
        dynamicViewEntity.addAlias("P", "productTypeId");
        dynamicViewEntity.addAlias("P", "mainProductId");
        dynamicViewEntity.addAlias("P", "introductionDate");
        dynamicViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewEntity.addAlias("P", "productName");
        dynamicViewEntity.addAlias("P", "productSubheadName");
        dynamicViewEntity.addAlias("P", "isMustChoose");
        dynamicViewEntity.addAlias("P", "isUsedFeature");
        dynamicViewEntity.addAlias("P", "pcDetails");
        dynamicViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        conditions.add(EntityCondition.makeCondition("productTypeId", "INSURANCE_GOOD"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list2 = FastList.newInstance();
        list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list2, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
            List<GenericValue> results = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> insuranceList = FastList.newInstance();
            for (GenericValue result : results) {
                Map map = FastMap.newInstance();
                map.put("productId", result.get("productId"));
                map.put("productName", result.get("productName"));
                map.put("productSubheadName", result.get("productSubheadName"));
                if (UtilValidate.areEqual("Y", result.get("isMustChoose"))) {
                    map.put("isMustChoose", true);
                } else if (UtilValidate.areEqual("N", result.get("isMustChoose"))) {
                    map.put("isMustChoose", false);
                }
                if (UtilValidate.areEqual("Y", result.get("isUsedFeature"))) {
                    List<GenericValue> products = delegator.findByAnd("Product", UtilMisc.toMap("mainProductId", result.get("productId")));
                    List<Map> featureList = FastList.newInstance();
                    for (GenericValue product : products) {
                        Map map1 = FastMap.newInstance();
                        map1.put("productId", product.get("productId"));
                        map1.put("featureProductName", product.get("featureProductName"));
                        featureList.add(map1);
                    }
                    map.put("featureList", featureList);
                }
                //PC端详情
                map.put("pcDetails", UtilValidate.isNotEmpty(result.get("pcDetails")) ? result.getString("pcDetails").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
                insuranceList.add(map);
            }
            request.setAttribute("insuranceList", insuranceList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取保险预订信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getInsuranceReserveDetail(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String reserveId = request.getParameter("reserveId");
        if (UtilValidate.isEmpty(reserveId) && UtilValidate.isNotEmpty(jsonObject.get("reserveId"))) {
            reserveId = jsonObject.getString("reserveId");
        }
        if (UtilValidate.isEmpty(reserveId)) {
            request.setAttribute("error", "预约单号不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            GenericValue reserveHeader = delegator.findByPrimaryKey("ReserveHeader", UtilMisc.toMap("reserveId", reserveId));
            if (UtilValidate.isNotEmpty(reserveHeader) && UtilValidate.areEqual(userLogin.get("partyId"), reserveHeader.get("partyId"))) {
                //预约状态
                request.setAttribute("statusId", reserveHeader.get("statusId"));
                //车牌号
                request.setAttribute("plateNumber", reserveHeader.get("plateNumber"));
                //预约人
                request.setAttribute("reservePerson", reserveHeader.get("reservePerson"));
                //预约电话
                request.setAttribute("reserveTel", reserveHeader.get("reserveTel"));
                //身份证号
                request.setAttribute("idNumber", reserveHeader.get("idNumber"));
                //是否过户
                if (UtilValidate.areEqual("Y", reserveHeader.get("isTransfer"))) {
                    request.setAttribute("isTransfer", true);
                } else {
                    request.setAttribute("isTransfer", false);
                }
                //已选定险种
                Boolean beganTransaction = TransactionUtil.begin();
                DynamicViewEntity viewEntity1 = new DynamicViewEntity();
                viewEntity1.addMemberEntity("RI", "ReserveItem");
                viewEntity1.addAlias("RI", "reserveId");
                viewEntity1.addAlias("RI", "productId", "productId", null, false, true, null);
                viewEntity1.addAlias("RI", "itemDescription");
                EntityListIterator eli = delegator.findListIteratorByCondition(viewEntity1, EntityCondition.makeCondition("reserveId", reserveId), null, null, null, null);
                List<GenericValue> reserveProducts = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                List<String> productList = FastList.newInstance();
                for (GenericValue reserveProduct : reserveProducts) {
                    productList.add(reserveProduct.getString("itemDescription"));
                }
                request.setAttribute("productList", productList);
                //意向投保公司
                beganTransaction = TransactionUtil.begin();
                DynamicViewEntity viewEntity2 = new DynamicViewEntity();
                viewEntity2.addMemberEntity("RI", "ReserveItem");
                viewEntity2.addAlias("RI", "reserveId");
                viewEntity2.addAlias("RI", "partyId", "partyId", null, false, true, null);
                viewEntity2.addAlias("RI", "businessName");
                viewEntity2.addAlias("RI", "unitPrice", "unitPrice", null, false, false, "sum");
                eli = delegator.findListIteratorByCondition(viewEntity2, EntityCondition.makeCondition("reserveId", reserveId), null, null, null, null);
                List<GenericValue> reserveBusiness = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                List<Map> businessList = FastList.newInstance();
                for (GenericValue business : reserveBusiness) {
                    Map map = FastMap.newInstance();
                    map.put("partyId", business.get("partyId"));
                    map.put("businessName", business.get("businessName"));
                    map.put("unitPrice", business.get("unitPrice"));
                    if (UtilValidate.isNotEmpty(reserveHeader.get("deadline"))) {
                        map.put("insuranceTime", sdf.format(reserveHeader.getDate("deadline")));
                    }
                    businessList.add(map);
                }
                request.setAttribute("businessList", businessList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取维修类型
     *
     * @param request
     * @param response
     * @return
     */
    public static String getRepairType(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> enum_list = null;
        try {
            enum_list = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "REPAIR_TYPE", "enumCode", "N"), UtilMisc.toList("sequenceId"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        List<Map> resultList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(enum_list)) {
            for (GenericValue enumeration : enum_list) {
                Map map = FastMap.newInstance();
                map.put("enumId", enumeration.get("enumId"));
                map.put("description", enumeration.get("description"));
                map.put("imgUrl", UtilValidate.isNotEmpty(enumeration.get("contentId")) ? (request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + enumeration.get("contentId")) : "");
                resultList.add(map);
            }
        }
        request.setAttribute("resultList", resultList);
        return "success";
    }

    /**
     * 获取维修标签
     *
     * @param request
     * @param response
     * @return
     */
    public static String getRepairTag(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> enum_list = null;
        try {
            enum_list = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "REPAIR_TAG", "enumCode", "N"), UtilMisc.toList("sequenceId"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        List<Map> resultList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(enum_list)) {
            for (GenericValue enumeration : enum_list) {
                Map map = FastMap.newInstance();
                map.put("enumId", enumeration.get("enumId"));
                map.put("description", enumeration.get("description"));
                resultList.add(map);
            }
        }
        request.setAttribute("resultList", resultList);
        return "success";
    }

    /**
     * 获取轮胎列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getTyreList(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        //车型ID
        String productCarModelId = request.getParameter("productCarModelId");
        if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        //销量排序
        String sortSale = request.getParameter("sortSale");
        if (UtilValidate.isEmpty(sortSale) && UtilValidate.isNotEmpty(jsonObject.get("sortSale"))) {
            sortSale = jsonObject.getString("sortSale");
        }
        //价格排序
        String sortPrice = request.getParameter("sortPrice");
        if (UtilValidate.isEmpty(sortPrice) && UtilValidate.isNotEmpty(jsonObject.get("sortPrice"))) {
            sortPrice = jsonObject.getString("sortPrice");
        }
        //评论排序
        String sortReview = request.getParameter("sortReview");
        if (UtilValidate.isEmpty(sortReview) && UtilValidate.isNotEmpty(jsonObject.get("sortReview"))) {
            sortReview = jsonObject.getString("sortReview");
        }
        //品牌
        String productBrandIds = request.getParameter("productBrandIds");
        if (UtilValidate.isEmpty(productBrandIds) && UtilValidate.isNotEmpty(jsonObject.get("productBrandIds"))) {
            productBrandIds = jsonObject.getString("productBrandIds");
        }
        //宽度
        String breadth = request.getParameter("breadth");
        if (UtilValidate.isEmpty(breadth) && UtilValidate.isNotEmpty(jsonObject.get("breadth"))) {
            breadth = jsonObject.getString("breadth");
        }
        //扁平比
        String flattening = request.getParameter("flattening");
        if (UtilValidate.isEmpty(flattening) && UtilValidate.isNotEmpty(jsonObject.get("flattening"))) {
            flattening = jsonObject.getString("flattening");
        }
        //尺寸
        String carSize = request.getParameter("carSize");
        if (UtilValidate.isEmpty(carSize) && UtilValidate.isNotEmpty(jsonObject.get("carSize"))) {
            carSize = jsonObject.getString("carSize");
        }
        //最低价格
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("P", "Product");
        dynamicViewViewEntity.addAlias("P", "productId", "productId", null, true, true, null);
        dynamicViewViewEntity.addAlias("P", "productName");
        dynamicViewViewEntity.addAlias("P", "isOnline");
        dynamicViewViewEntity.addAlias("P", "isVerify");
        dynamicViewViewEntity.addAlias("P", "isDel");
        dynamicViewViewEntity.addAlias("P", "primaryProductCategoryId");
        dynamicViewViewEntity.addAlias("P", "productTypeId");
        dynamicViewViewEntity.addAlias("P", "mainProductId");
        dynamicViewViewEntity.addAlias("P", "breadth");
        dynamicViewViewEntity.addAlias("P", "flattening");
        dynamicViewViewEntity.addAlias("P", "carSize");
        dynamicViewViewEntity.addAlias("P", "introductionDate");
        dynamicViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewViewEntity.addAlias("P", "createdStamp");
        dynamicViewViewEntity.addAlias("P", "productBrandId");
        dynamicViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PP", "ProductPrice");
        dynamicViewViewEntity.addAlias("PP", "productPriceTypeId");
        dynamicViewViewEntity.addAlias("PP", "price");
        dynamicViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PMR", "ProductModelReference");
        dynamicViewViewEntity.addAlias("PMR", "productCarModelId");
        dynamicViewViewEntity.addViewLink("P", "PMR", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PCI", "ProductCalculatedInfo");
        dynamicViewViewEntity.addAlias("PCI", "totalQuantityOrdered");
        dynamicViewViewEntity.addViewLink("P", "PCI", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        if (UtilValidate.isNotEmpty(sortReview)) {
            dynamicViewViewEntity.addMemberEntity("PR", "ProductReview");
            dynamicViewViewEntity.addAlias("PR", "reviewNum", "productReviewId", null, false, false, "count");
            dynamicViewViewEntity.addViewLink("P", "PR", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        }
        //查询条件
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        //换轮胎分类下的所有子分类
        List<String> categoryIds = FastList.newInstance();
        List<GenericValue> productCategoreys = null;
        try {
            productCategoreys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", "CHANGE_TYRE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(productCategoreys)) {
            for (GenericValue productCategorey : productCategoreys) {
                categoryIds.add(productCategorey.getString("productCategoryId"));
            }
        }
        conditions.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, categoryIds));
        conditions.add(EntityCondition.makeCondition("productTypeId", "PARETS_GOOD"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        if (UtilValidate.isNotEmpty(productBrandIds)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", EntityOperator.IN, UtilMisc.toListArray(productBrandIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(productCarModelId)) {
            conditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
        }
        if (UtilValidate.isNotEmpty(breadth)) {
            String[] breadths = breadth.split(",");
            List<EntityCondition> breadthConds = FastList.newInstance();
            for (String cond : breadths) {
                breadthConds.add(EntityCondition.makeCondition("breadth", new BigDecimal(cond)));
            }
            conditions.add(EntityCondition.makeCondition(breadthConds, EntityOperator.OR));
        }
        if (UtilValidate.isNotEmpty(flattening)) {
            String[] flattenings = flattening.split(",");
            List<EntityCondition> flatteningConds = FastList.newInstance();
            for (String cond : flattenings) {
                flatteningConds.add(EntityCondition.makeCondition("flattening", new BigDecimal(cond)));
            }
            conditions.add(EntityCondition.makeCondition(flatteningConds, EntityOperator.OR));
        }
        if (UtilValidate.isNotEmpty(carSize)) {
            String[] carSizes = carSize.split(",");
            List<EntityCondition> carSizeConds = FastList.newInstance();
            for (String cond : carSizes) {
                carSizeConds.add(EntityCondition.makeCondition("carSize", new BigDecimal(cond)));
            }
            conditions.add(EntityCondition.makeCondition(carSizeConds, EntityOperator.OR));
        }
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        //排序条件
        List<String> sortFields = FastList.newInstance();
        if (UtilValidate.areEqual("Y", sortSale)) {
            sortFields.add("totalQuantityOrdered");
        } else if (UtilValidate.areEqual("N", sortSale)) {
            sortFields.add("-totalQuantityOrdered");
        } else if (UtilValidate.areEqual("Y", sortPrice)) {
            sortFields.add("price");
        } else if (UtilValidate.areEqual("N", sortPrice)) {
            sortFields.add("-price");
        } else if (UtilValidate.areEqual("Y", sortReview)) {
            sortFields.add("reviewNum");
        } else if (UtilValidate.areEqual("N", sortReview)) {
            sortFields.add("-reviewNum");
        } else {
            sortFields.add("-introductionDate");
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, sortFields, findOpts);
            List<GenericValue> tyres = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue tyre : tyres) {
                Map map = FastMap.newInstance();
                map.put("productId", tyre.get("productId"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", tyre.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                map.put("productName", tyre.get("productName"));
                BigDecimal productPrice = tyre.getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP);
                map.put("productPrice", productPrice);
                map.put("saleNum", tyre.get("totalQuantityOrdered"));
                List<GenericValue> inventoryItem = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", tyre.get("productId")));
                if (UtilValidate.isNotEmpty(inventoryItem)) {
                    map.put("quantity", inventoryItem.get(0).getBigDecimal("accountingQuantityTotal").subtract(inventoryItem.get(0).getBigDecimal("occupiedQuantityTotal")));
                }
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取车品商城下的三级分类
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarGoodsCategory(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map> categoryList = FastList.newInstance();
        List<GenericValue> productCategoreys = null;
        try {
            productCategoreys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", "CARGOODS_STORE"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(productCategoreys)) {
            for (GenericValue productCategorey : productCategoreys) {
                Map map = FastMap.newInstance();
                map.put("productCategoryId", productCategorey.get("productCategoryId"));
                map.put("categoryName", productCategorey.get("categoryName"));
                map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productCategorey.get("contentId"));
                categoryList.add(map);
            }
        }
        request.setAttribute("categoryList", categoryList);

        return "success";
    }

    /**
     * 获取车品商城列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getParetsList(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        //车型ID
        String productCarModelId = request.getParameter("productCarModelId");
        if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        //分类ID
        String productCategoryId = request.getParameter("productCategoryId");
        if (UtilValidate.isEmpty(productCategoryId) && UtilValidate.isNotEmpty(jsonObject.get("productCategoryId"))) {
            productCategoryId = jsonObject.getString("productCategoryId");
        }
        //最低价格
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        //品牌编号
        String productBrandIds = request.getParameter("productBrandIds");
        if (UtilValidate.isEmpty(productBrandIds) && UtilValidate.isNotEmpty(jsonObject.get("productBrandIds"))) {
            productBrandIds = jsonObject.getString("productBrandIds");
        }
        //销量排序
        String sortSale = request.getParameter("sortSale");
        if (UtilValidate.isEmpty(sortSale) && UtilValidate.isNotEmpty(jsonObject.get("sortSale"))) {
            sortSale = jsonObject.getString("sortSale");
        }
        //价格排序
        String sortPrice = request.getParameter("sortPrice");
        if (UtilValidate.isEmpty(sortPrice) && UtilValidate.isNotEmpty(jsonObject.get("sortPrice"))) {
            sortPrice = jsonObject.getString("sortPrice");
        }
        //评论排序
        String sortReview = request.getParameter("sortReview");
        if (UtilValidate.isEmpty(sortReview) && UtilValidate.isNotEmpty(jsonObject.get("sortReview"))) {
            sortReview = jsonObject.getString("sortReview");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("P", "Product");
        dynamicViewViewEntity.addAlias("P", "productId", "productId", null, true, true, null);
        dynamicViewViewEntity.addAlias("P", "productName");
        dynamicViewViewEntity.addAlias("P", "isOnline");
        dynamicViewViewEntity.addAlias("P", "isVerify");
        dynamicViewViewEntity.addAlias("P", "isDel");
        dynamicViewViewEntity.addAlias("P", "primaryProductCategoryId");
        dynamicViewViewEntity.addAlias("P", "productTypeId");
        dynamicViewViewEntity.addAlias("P", "mainProductId");
        dynamicViewViewEntity.addAlias("P", "productBrandId");
        dynamicViewViewEntity.addAlias("P", "introductionDate");
        dynamicViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        dynamicViewViewEntity.addAlias("P", "createdStamp");
        dynamicViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        dynamicViewViewEntity.addAlias("PSPA", "productStoreId");
        dynamicViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PP", "ProductPrice");
        dynamicViewViewEntity.addAlias("PP", "productPriceTypeId");
        dynamicViewViewEntity.addAlias("PP", "price");
        dynamicViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PMR", "ProductModelReference");
        dynamicViewViewEntity.addAlias("PMR", "productCarModelId");
        dynamicViewViewEntity.addViewLink("P", "PMR", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        dynamicViewViewEntity.addMemberEntity("PCI", "ProductCalculatedInfo");
        dynamicViewViewEntity.addAlias("PCI", "totalQuantityOrdered");
        dynamicViewViewEntity.addViewLink("P", "PCI", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        if (UtilValidate.isNotEmpty(sortReview)) {
            dynamicViewViewEntity.addMemberEntity("PR", "ProductReview");
            dynamicViewViewEntity.addAlias("PR", "reviewNum", "productReviewId", null, false, false, "count");
            dynamicViewViewEntity.addViewLink("P", "PR", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        }
        //查询条件
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        if (UtilValidate.isNotEmpty(productCategoryId)) {
            conditions.add(EntityCondition.makeCondition("primaryProductCategoryId", productCategoryId));
        } else {
            //车品商城分类下的所有子分类
            List<String> categoryIds = FastList.newInstance();
            List<GenericValue> productCategoreys = null;
            try {
                productCategoreys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", "CARGOODS_STORE"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(productCategoreys)) {
                for (GenericValue productCategorey : productCategoreys) {
                    categoryIds.add(productCategorey.getString("productCategoryId"));
                }
            }
            conditions.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.IN, categoryIds));
        }
        conditions.add(EntityCondition.makeCondition("productTypeId", "PARETS_GOOD"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        if (UtilValidate.isNotEmpty(productCarModelId)) {
            conditions.add(EntityCondition.makeCondition("productCarModelId", productCarModelId));
        }
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        if (UtilValidate.isNotEmpty(productBrandIds)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", EntityOperator.IN, UtilMisc.toListArray(productBrandIds.split(","))));
        }
        //排序条件
        List<String> sortFields = FastList.newInstance();
        if (UtilValidate.areEqual("Y", sortSale)) {
            sortFields.add("totalQuantityOrdered");
        } else if (UtilValidate.areEqual("N", sortSale)) {
            sortFields.add("-totalQuantityOrdered");
        } else if (UtilValidate.areEqual("Y", sortPrice)) {
            sortFields.add("price");
        } else if (UtilValidate.areEqual("N", sortPrice)) {
            sortFields.add("-price");
        } else if (UtilValidate.areEqual("Y", sortReview)) {
            sortFields.add("reviewNum");
        } else if (UtilValidate.areEqual("N", sortReview)) {
            sortFields.add("-reviewNum");
        }
        sortFields.add("-introductionDate");
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, sortFields, findOpts);
            List<GenericValue> parets = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue paret : parets) {
                Map map = FastMap.newInstance();
                map.put("productId", paret.get("productId"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", paret.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                map.put("productName", paret.get("productName"));
                BigDecimal productPrice = paret.getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP);
                map.put("productPrice", productPrice);
                map.put("saleNum", paret.get("totalQuantityOrdered"));
                List<GenericValue> inventoryItem = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", paret.get("productId")));
                if (UtilValidate.isNotEmpty(inventoryItem)) {
                    map.put("quantity", inventoryItem.get(0).getBigDecimal("accountingQuantityTotal").subtract(inventoryItem.get(0).getBigDecimal("occupiedQuantityTotal")));
                }
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 会员成长足迹获取接口
     *
     * @param request
     * @param response
     * @return
     * @author gss  017-04-12
     */
    public static String getPartyGrowthHis(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("PH", "PartyGrowthHis");
        dev.addAlias("PH", "description");
        dev.addAlias("PH", "growthValue");
        dev.addAlias("PH", "createdStamp");
        dev.addAlias("PH", "partyId");
        List<EntityCondition> conditions = FastList.newInstance();

        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        if (UtilValidate.isNotEmpty(userLogin)) {
            List<Map> resultList = FastList.newInstance();
            String partyId = userLogin.getString("partyId");
            if (UtilValidate.isEmpty(partyId)) {
                request.setAttribute("error", "会员不存在");
                response.setStatus(403);
                return "error";
            } else {
                try {
                    conditions.add(EntityCondition.makeCondition("partyId", partyId));
                    Boolean beganTransaction = TransactionUtil.begin();
                    EntityListIterator eli = delegator.findListIteratorByCondition(dev, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, findOpts);
                    List<GenericValue> results = eli.getPartialList(lowIndex, viewSize);
                    int resultSize = eli.getResultsSizeAfterPartialList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    //会员成长足迹获取
                    for (GenericValue result : results) {
                        Map map = FastMap.newInstance();
                        map.put("growthValue", result.get("growthValue"));
                        map.put("description", result.get("description"));
                        map.put("createdDate", dateFormat.format(result.getTimestamp("createdStamp")));
                        resultList.add(map);
                    }
                    request.setAttribute("resultList", resultList);
                    request.setAttribute("max", resultSize);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            request.setAttribute("error", "登录用户不存在");
            response.setStatus(403);
            return "error";
        }
        return "success";
    }

    /**
     * 会员当前余额获取接口
     *
     * @param request
     * @param response
     * @return
     * @author gss  017-04-12
     */
    public static String getPartyAccount(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            String partyId = userLogin.getString("partyId");
            if (UtilValidate.isEmpty(partyId)) {
                request.setAttribute("error", "会员不存在");
                response.setStatus(403);
                return "error";
            } else {
                //会员账户信息
                GenericValue partyaccount = null;
                try {
                    partyaccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", partyId));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isNotEmpty(partyaccount)) {
                    request.setAttribute("amount", partyaccount.get("amount"));
                }
            }
        } else {
            request.setAttribute("error", "登录账号不存在");
            response.setStatus(403);
            return "error";
        }
        return "success";
    }

    /**
     * PC端余额流水
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getPartyAccountPC(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询页数不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 20;
        }
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        String partyId = userLogin.getString("partyId");
        //会员账户信息
        GenericValue partyaccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", partyId));
        if (UtilValidate.isNotEmpty(partyaccount)) {
            request.setAttribute("amount", partyaccount.get("amount"));
        }
        int lowIndex = (viewIndex - 1) * viewSize + 1;
        int highIndex = viewIndex * viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("PA", "PartyAccountDetail");
        dev.addAlias("PA", "detailId");
        dev.addAlias("PA", "description");
        dev.addAlias("PA", "resultType");
        dev.addAlias("PA", "amount");
        dev.addAlias("PA", "createDate");
        dev.addAlias("PA", "orderId");
        dev.addAlias("PA", "partyId");
        List<EntityCondition> conditions = FastList.newInstance();
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        conditions.add(EntityCondition.makeCondition("partyId", partyId));
        Boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator eli = delegator.findListIteratorByCondition(dev, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-createDate"), findOpts);
        List<GenericValue> results = eli.getPartialList(lowIndex, viewSize);
        int resultSize = eli.getResultsSizeAfterPartialList();
        eli.close();
        TransactionUtil.commit(beganTransaction);
        //余额流水信息
        List<Map> resultList = FastList.newInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        for (GenericValue result : results) {
            Map map = FastMap.newInstance();
                        /*发生流水动作*/
            map.put("description", result.get("description"));
                        /* 发生流水结果，对应键值为 收益IN  支出 OUT*/
            map.put("resultType", result.get("resultType"));
                        /*发生流水金额，精确到两位小数*/
            map.put("amount", result.getBigDecimal("amount"));
                        /*发生流水时间，格式为：yyyy-mm-dd hh:mm:ss*/
            map.put("createDate", dateFormat.format(result.getTimestamp("createDate")));
                        /*发生流水的订单流水号*/
            map.put("orderId", result.get("orderId"));
            resultList.add(map);
        }
        request.setAttribute("resultList", resultList);
        request.setAttribute("max", resultSize % viewSize == 0 ? resultSize / viewSize : resultSize / viewSize + 1);
        return "success";
    }

    /**
     * 设置支付密码
     *
     * @param request
     * @param response
     * @return
     */
    public static String setPayPassword(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String phoneId = request.getParameter("phoneId");
        String checkCode = request.getParameter("checkCode");
        String payPassword = request.getParameter("payPassword");
        if (UtilValidate.isEmpty(phoneId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            phoneId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(payPassword) && UtilValidate.isNotEmpty(jsonObject.get("payPassword"))) {
            payPassword = jsonObject.getString("payPassword");
        }
        if (UtilValidate.isEmpty(phoneId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(payPassword)) {
            request.setAttribute("error", "支付密码不能为空");
            response.setStatus(403);
            return "error";
        }
        payPassword = new String(decode(payPassword));
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        GenericValue userLogin1 = null;
        GenericValue party = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            userLogin1 = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phoneId));
            party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(userLogin1)) {
            request.setAttribute("error", "该手机号不可用");
            response.setStatus(403);
            return "error";
        }
        if (!UtilValidate.areEqual(userLogin.get("partyId"), userLogin1.get("partyId"))) {
            request.setAttribute("error", "该手机号不属于当前登陆用户");
            response.setStatus(403);
            return "error";
        }
        /*if (UtilValidate.isNotEmpty(party.get("payPassword"))) {
            request.setAttribute("error", "该账号已存在支付密码");
            response.setStatus(403);
            return "error";
        }*/
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(mobileCheckCode) || UtilValidate.isEmpty(mobileCheckCode.get("checkCode")) || !checkCode.equals(mobileCheckCode.get("checkCode"))) {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
            return "error";
        }
        party.set("payPassword", HashCrypt.cryptPassword(MemberEvents.getHashType(), payPassword));
        try {
            delegator.store(party);
            delegator.removeValue(mobileCheckCode);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("success", "设置成功");
        return "success";

    }

    /**
     * 积分流水数据获取接口
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     * @throws DateParseException
     * @author gss  017-04-14
     */
    public static String getPartyScoreHis(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException, DateParseException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String getWay = null;
        if (UtilValidate.isNotEmpty(request.getParameter("getWay"))) {
            getWay = request.getParameter("getWay");
        } else if (UtilValidate.isEmpty(getWay) && UtilValidate.isNotEmpty(jsonObject.get("getWay"))) {
            getWay = jsonObject.getString("getWay");
        }
        if (UtilValidate.isEmpty(getWay)) {
            request.setAttribute("error", "getWay不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }

        List<Map<String, Object>> resultList = FastList.newInstance();
        String max = "0"; // 总条数
        // ========CRM接口调用 begin spj=========
        if (UtilValidate.isNotEmpty(userLogin)) {
            Map<String, Object> params = FastMap.newInstance();
            params.put("currentPage", String.valueOf((viewIndex + viewSize) / viewSize)); // 当前页数
            params.put("pageSize", String.valueOf(viewSize)); // 每页大小
            params.put("custId", userLogin.getString("custId")); // 会员编号
            params.put("getWay", getWay); // 积分流水的查询条件

            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            Map<String, Object> resultMap = dispatcher.runSync("queryCustomerIntegralCrm22", params);
            if ("SUCCESS".equals(resultMap.get("expStatus"))) {
                String data = (String) resultMap.get("data");
                if (UtilValidate.isNotEmpty(data)) {

                    JSONObject jectx = HttpUtil.convertToJSONObject(data);
                    if (UtilValidate.isNotEmpty(jectx)) {
                        String results = ""; // 结果集
                        if (jectx.containsKey("max")) {
                            max = jectx.getString("max");
                        }
                        if (jectx.containsKey("results")) {
                            results = jectx.getString("results");
                        }

                        JSONArray jects = HttpUtil.convertToJSONArray(results);
                        Map<String, Object> rMap = FastMap.newInstance();
                        for (int i = 0; i < jects.size(); i++) {
                            rMap = new HashMap<String, Object>();
                            JSONObject ject = jects.getJSONObject(i);
                            String custCode = ""; // 会员编号
                            String orderId = ""; // 发生流水订单号
                            String description = ""; // 发生流水动作
                            String scoreValue = ""; // 积分变动数
                            String createDate = ""; // 积分入账日期
                            String way = ""; // 积分流水的查询条件
                            String residualScore = ""; // 发生流水后剩余积分
                            String validityDate = ""; // 积分到期日期

                            if (ject.containsKey("custCode")) {
                                custCode = ject.getString("custCode"); // 会员编号
                            }
                            if (ject.containsKey("orderId")) {
                                orderId = ject.getString("orderId"); // 发生流水订单号
                            }
                            if (ject.containsKey("description")) {
                                description = ject.getString("description"); // 发生流水动作
                            }
                            if (ject.containsKey("scoreValue")) {
                                scoreValue = ject.getString("scoreValue"); // 积分变动数
//                                scoreValue = scoreValue.substring(0, scoreValue.indexOf("."));
                            }
                            if (ject.containsKey("createTime")) {
                                createDate = ject.getString("createTime").substring(0, 10); // 积分入账日期
                            }
                            if (ject.containsKey("getWay")) {
                                way = ject.getString("getWay"); // 积分流水的查询条件
                            }
                            if (ject.containsKey("residualScore")) {
                                residualScore = ject.getString("residualScore"); // 发生流水后剩余积分
//                                residualScore = residualScore.substring(0, residualScore.indexOf("."));
                            }
                            if (ject.containsKey("validityDate")) {
                                validityDate = ject.getString("validityDate").substring(0, 10); // 积分到期日期
                            }

                            rMap.put("custCode", custCode); // 会员编号
                            rMap.put("orderId", orderId); // 发生流水订单号
                            rMap.put("description", description); // 积分变动数
                            rMap.put("scoreValue", scoreValue); // 积分变动数
                            rMap.put("createDate", createDate); // 积分入账日期
                            rMap.put("getWay", way); // 积分入账日期
                            rMap.put("residualScore", residualScore); // 发生流水后剩余积分
                            rMap.put("validityDate", validityDate); // 积分到期日期

                            resultList.add(rMap);
                        }
                    }
                }
            }
        }
        request.setAttribute("resultList", resultList);
        request.setAttribute("max", Integer.parseInt(max));
        // ========CRM接口调用 end spj=========


//        int lowIndex = viewIndex + 1;
//        int highIndex = viewIndex + viewSize;
//        DynamicViewEntity dev = new DynamicViewEntity();
//        dev.addMemberEntity("PSH", "PartyScoreHistory");
//        dev.addAlias("PSH", "partyScoreHistoryId");
//        dev.addAlias("PSH", "scoreValue");
//        dev.addAlias("PSH", "residualScore");
//        dev.addAlias("PSH", "partyId");
//        dev.addAlias("PSH", "orderId");
//        dev.addAlias("PSH", "createDate");
//        dev.addAlias("PSH", "getWay");
//        dev.addAlias("PSH", "description");
//        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
//        List<EntityExpr> exprs = FastList.newInstance();
//        EntityCondition mainCond = null;
//
//        if (UtilValidate.isNotEmpty(getWay) && "ALL".equals(getWay)) {
//            exprs.add(EntityCondition.makeCondition("getWay", EntityOperator.NOT_EQUAL, null));
//        } else {
//            exprs.add(EntityCondition.makeCondition("getWay", EntityOperator.EQUALS, getWay));
//        }
//        if (UtilValidate.isNotEmpty(userLogin)) {
//            List<Map> resultList = FastList.newInstance();
//            String partyId = userLogin.getString("partyId");
//            if (UtilValidate.isEmpty(partyId)) {
//                request.setAttribute("error", "会员不存在");
//                response.setStatus(403);
//                return "error";
//            } else {
//                try {
//                    exprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
//                    if (exprs.size() > 0) {
//                        mainCond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
//                    }
//                    Boolean beganTransaction = TransactionUtil.begin();
//                    EntityListIterator eli = delegator.findListIteratorByCondition(dev, mainCond, null, null, UtilMisc.toList("-createDate"), findOpts);
//                    List<GenericValue> results = eli.getPartialList(lowIndex, viewSize);
//                    int resultSize = eli.getResultsSizeAfterPartialList();
//                    eli.close();
//                    TransactionUtil.commit(beganTransaction);
//                    //会员成长足迹获取
//                    for (GenericValue result : results) {
//                        Map map = FastMap.newInstance();
//                        map.put("partyScoreHistoryId", result.get("partyScoreHistoryId"));
//                        map.put("scoreValue", result.get("scoreValue"));
//                        map.put("residualScore", result.get("residualScore"));
//                        map.put("orderId", result.get("orderId"));
//                        map.put("getWay", result.get("getWay"));
//                        map.put("description", result.get("description"));
//                        map.put("createDate", dateFormat.format(result.getTimestamp("createDate")));
//                        resultList.add(map);
//                    }
//                    request.setAttribute("resultList", resultList);
//                    request.setAttribute("max", resultSize);
//                } catch (GenericEntityException e) {
//                    e.printStackTrace();
//                }
//            }
//        } else {
//            request.setAttribute("error", "登录用户不存在");
//            response.setStatus(403);
//            return "error";
//        }
        return "success";
    }

    /**
     * PC端获取积分流水
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     * @throws DateParseException
     */
    public static String getPartyScoreHisPC(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, GenericServiceException, DateParseException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String getWay = null;
        if (UtilValidate.isNotEmpty(request.getParameter("getWay"))) {
            getWay = request.getParameter("getWay");
        } else if (UtilValidate.isEmpty(getWay) && UtilValidate.isNotEmpty(jsonObject.get("getWay"))) {
            getWay = jsonObject.getString("getWay");
        }
        if (UtilValidate.isEmpty(getWay)) {
            request.setAttribute("error", "getWay不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 20;
        }
        List<Map<String, Object>> resultList = FastList.newInstance();
        String max = "0"; // 总条数
        String score = "0"; // 可用总积分

        // ========CRM接口调用 begin spj=========
        if (UtilValidate.isNotEmpty(userLogin)) {
            Map<String, Object> params = FastMap.newInstance();
            params.put("currentPage", String.valueOf(viewIndex)); // 当前页数
            params.put("pageSize", String.valueOf(viewSize)); // 每页大小
            params.put("custId", userLogin.getString("custId")); // 会员编号
            params.put("getWay", getWay); // 积分流水的查询条件

            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            Map<String, Object> resultMap = dispatcher.runSync("queryCustomerIntegralCrm22", params);
            if ("SUCCESS".equals(resultMap.get("expStatus"))) {
                String data = (String) resultMap.get("data");
                if (UtilValidate.isNotEmpty(data)) {

                    JSONObject jectx = HttpUtil.convertToJSONObject(data);
                    if (UtilValidate.isNotEmpty(jectx)) {
                        String results = ""; // 结果集
                        if (jectx.containsKey("max")) {
                            max = jectx.getString("max");
                        }
                        if (jectx.containsKey("results")) {
                            results = jectx.getString("results");
                        }
                        if (jectx.containsKey("score")) {
                            score = jectx.getString("score");
                            score = score.substring(0, score.indexOf("."));
                        }

                        JSONArray jects = HttpUtil.convertToJSONArray(results);
                        Map<String, Object> rMap = FastMap.newInstance();
                        for (int i = 0; i < jects.size(); i++) {
                            rMap = new HashMap<String, Object>();
                            JSONObject ject = jects.getJSONObject(i);
                            String custCode = ""; // 会员编号
                            String orderId = ""; // 发生流水订单号
                            String description = ""; // 发生流水动作
                            String scoreValue = ""; // 积分变动数
                            String createDate = ""; // 积分入账日期
                            String way = ""; // 积分流水的查询条件
                            String residualScore = ""; // 发生流水后剩余积分
                            String validityDate = ""; // 积分到期日期

                            if (ject.containsKey("custCode")) {
                                custCode = ject.getString("custCode"); // 会员编号
                            }
                            if (ject.containsKey("orderId")) {
                                orderId = ject.getString("orderId"); // 发生流水订单号
                            }
                            if (ject.containsKey("description")) {
                                description = ject.getString("description"); // 发生流水动作
                            }
                            if (ject.containsKey("scoreValue")) {
                                scoreValue = ject.getString("scoreValue"); // 积分变动数
//                                scoreValue = scoreValue.substring(0, scoreValue.indexOf("."));
                            }
                            if (ject.containsKey("createTime")) {
                                createDate = ject.getString("createTime").substring(0, 10); // 积分入账日期
                            }
                            if (ject.containsKey("getWay")) {
                                way = ject.getString("getWay"); // 积分流水的查询条件
                            }
                            if (ject.containsKey("residualScore")) {
                                residualScore = ject.getString("residualScore"); // 发生流水后剩余积分
//                                residualScore = residualScore.substring(0, residualScore.indexOf("."));
                            }
                            if (ject.containsKey("validityDate")) {
                                validityDate = ject.getString("validityDate").substring(0, 10); // 积分到期日期
                            }

                            rMap.put("custCode", custCode); // 会员编号
                            rMap.put("orderId", orderId); // 发生流水订单号
                            rMap.put("description", description); // 积分变动数
                            rMap.put("scoreValue", scoreValue); // 积分变动数
                            rMap.put("createDate", createDate); // 积分入账日期
                            rMap.put("getWay", way); // 积分入账日期
                            rMap.put("residualScore", residualScore); // 发生流水后剩余积分
                            rMap.put("validityDate", validityDate); // 积分到期日期

                            resultList.add(rMap);
                        }
                    }
                }
            }
        }

        request.setAttribute("resultList", resultList);
        request.setAttribute("max", Integer.parseInt(max));
        request.setAttribute("score", score);
        // ========CRM接口调用 end spj=========


//        int lowIndex = (viewIndex - 1) * viewSize + 1;
//        int highIndex = viewIndex * viewSize;
//        DynamicViewEntity dev = new DynamicViewEntity();
//        dev.addMemberEntity("PSH", "PartyScoreHistory");
//        dev.addAlias("PSH", "partyScoreHistoryId");
//        dev.addAlias("PSH", "description");
//        dev.addAlias("PSH", "scoreValue");
//        dev.addAlias("PSH", "residualScore");
//        dev.addAlias("PSH", "partyId");
//        dev.addAlias("PSH", "orderId");
//        dev.addAlias("PSH", "createDate");
//        dev.addAlias("PSH", "getWay");
//        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
//        List<EntityExpr> exprs = FastList.newInstance();
//        EntityCondition mainCond = null;
//
//        if (UtilValidate.isNotEmpty(getWay) && "ALL".equals(getWay)) {
//            exprs.add(EntityCondition.makeCondition("getWay", EntityOperator.NOT_EQUAL, null));
//        } else {
//            exprs.add(EntityCondition.makeCondition("getWay", EntityOperator.EQUALS, getWay));
//        }
//        List<Map> resultList = FastList.newInstance();
//        String partyId = userLogin.getString("partyId");
//        exprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
//        if (exprs.size() > 0) {
//            mainCond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
//        }
//        Boolean beganTransaction = TransactionUtil.begin();
//        EntityListIterator eli = delegator.findListIteratorByCondition(dev, mainCond, null, null, UtilMisc.toList("-createDate"), findOpts);
//        List<GenericValue> results = eli.getPartialList(lowIndex, viewSize);
//        int resultSize = eli.getResultsSizeAfterPartialList();
//        eli.close();
//        TransactionUtil.commit(beganTransaction);
//        //会员成长足迹获取
//        for (GenericValue result : results) {
//            Map map = FastMap.newInstance();
//            map.put("description", result.get("description"));
//            map.put("scoreValue", result.get("scoreValue"));
//            map.put("residualScore", result.get("residualScore"));
//            map.put("orderId", result.get("orderId"));
//            map.put("createDate", dateFormat.format(result.getTimestamp("createDate")));
//            resultList.add(map);
//        }
//        request.setAttribute("resultList", resultList);
//        request.setAttribute("max", resultSize % viewSize == 0 ? resultSize / viewSize : resultSize / viewSize + 1);
//        Long score = 0L;
//        //我的积分
//        GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", partyId));
//        if (UtilValidate.isNotEmpty(partyScore)) {
//            score = partyScore.getLong("scoreValue");
//        }
//        request.setAttribute("score", score);
        return "success";
    }

    /**
     * 充值项
     *
     * @param request
     * @param response
     * @return
     */
    public static String getRecAmount(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map> resultList = FastList.newInstance();
        try {
            // 枚举信息--充值金额
            List<GenericValue> enum_list = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "FRONT_REC_AMOUNT", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            for (GenericValue result : enum_list) {
                Map map = FastMap.newInstance();
                map.put("enumId", result.get("enumId"));
                map.put("originalAmount", new BigDecimal(result.getString("description")));
                map.put("actualAmount", new BigDecimal(result.getString("remark")));
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 赠送积分项获取
     * add by gss 2017.04.14
     *
     * @param
     * @param
     * @return
     */
    public static String getPointsGift(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map> resultList = FastList.newInstance();
        try {
            // 枚举信息--积分奖励
            List<GenericValue> enum_list = delegator.findByAnd("Enumeration",
                    UtilMisc.toMap("enumTypeId", "FRONT_POINTS_GIFT", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            for (GenericValue result : enum_list) {
                Map map = FastMap.newInstance();
                map.put("enumId", result.get("enumId"));
                map.put("description", result.get("description"));
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取用户可分享积分数
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws GenericServiceException
     */
    public static String getSharePartyScore(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, GenericServiceException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> params = FastMap.newInstance();
        params.put("custId", userLogin.getString("custId"));

        Map<String, Object> result = dispatcher.runSync("syncTotalIntegralCrm44", params);
        if ("SUCCESS".equals(result.get("expStatus")) && UtilValidate.isNotEmpty(result.get("data"))) {
            double partyScore = Double.parseDouble(result.get("data").toString()); // 用户可分享的积分
            request.setAttribute("partyScore", partyScore);
            return "success";
        } else {
            request.setAttribute("partyScore", 0);
            return "error";
        }
    }

    /**
     * 积分赠送接口
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String sharePartyScore(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        //积分项Id
        String enumId = request.getParameter("enumId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(enumId) && UtilValidate.isNotEmpty(jsonObject.get("enumId"))) {
            enumId = (String) jsonObject.get("enumId");
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        if (UtilValidate.isEmpty(enumId)) {
            request.setAttribute("error", "积分赠送项Id不存在");
            response.setStatus(403);
            return "error";
        }
        //获取赠送积分项
        GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", enumId));
        //判断积分项是否存在
        if (UtilValidate.isEmpty(enumeration)) {
            request.setAttribute("error", "积分赠送项不存在");
            response.setStatus(403);
            return "error";
        } else {
            String partyId = userLogin.getString("partyId");
            double value = new Long(Long.parseLong(enumeration.getString("remark"))).doubleValue(); // 当前分享积分
            //获取会员积分账户信息
            // =============扣减用户积分 begin ============
            try {
                LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
                Map<String, Object> params = FastMap.newInstance();
                params.put("custId", userLogin.getString("custId"));

                Map<String, Object> result = dispatcher.runSync("syncTotalIntegralCrm44", params);
                if ("SUCCESS".equals(result.get("expStatus")) && UtilValidate.isNotEmpty(result.get("data"))) {
                    double partyScore = Double.parseDouble(result.get("data").toString()); // 用户可分享的积分
                    if (value > partyScore) {
                        request.setAttribute("error", "可分享积分不足");
                        response.setStatus(403);
                        return "error";
                    }
                } else {
                    request.setAttribute("error", "可分享积分不足");
                    response.setStatus(403);
                    return "error";
                }
            } catch (GenericServiceException e) {
                e.printStackTrace();
            }
            // =============扣减用户积分 end ==============
//            GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", partyId));
//            if (UtilValidate.isEmpty(partyScore)) {
//                request.setAttribute("error", "可分享积分不足");
//                response.setStatus(403);
//                return "error";
//            }
//            Long scoreValue = partyScore.getLong("scoreValue");
//            Long value = Long.parseLong(enumeration.getString("remark"));
//            if (scoreValue < value) {
//                request.setAttribute("error", "可分享积分不足");
//                response.setStatus(403);
//                return "error";
//            }
            String partyScoreShareId = delegator.getNextSeqId("PartyScoreShare");
            GenericValue partyScoreShare = delegator.makeValue("PartyScoreShare");
            partyScoreShare.set("partyScoreShareId", partyScoreShareId);
            partyScoreShare.set("sharePartyId", partyId);
            partyScoreShare.set("shareTime", UtilDateTime.nowTimestamp());
            partyScoreShare.set("scoreValue", Long.parseLong(enumeration.getString("remark")));
            delegator.create(partyScoreShare);
            request.setAttribute("partyScoreShareId", partyScoreShareId);
            return "success";
        }
    }

    /**
     * 积分扣减接口
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String subtractPartyScore(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //分享记录Id
        String partyScoreShareId = request.getParameter("partyScoreShareId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(partyScoreShareId) && UtilValidate.isNotEmpty(jsonObject.get("partyScoreShareId"))) {
            partyScoreShareId = (String) jsonObject.get("partyScoreShareId");
        }
        if (UtilValidate.isEmpty(partyScoreShareId)) {
            request.setAttribute("error", "分享记录编号不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue partyScoreShare = delegator.findByPrimaryKey("PartyScoreShare", UtilMisc.toMap("partyScoreShareId", partyScoreShareId));
        if (UtilValidate.isEmpty(partyScoreShare)) {
            request.setAttribute("error", "分享记录不存在");
            response.setStatus(403);
            return "error";
        }
        String partyId = partyScoreShare.getString("sharePartyId");
        Long integralNum = partyScoreShare.getLong("scoreValue");

        // =============扣减用户积分 begin ============
        try {
            GenericValue userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId)).get(0);
            if (UtilValidate.isNotEmpty(integralNum)) {
                LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
                Map<String, Object> params = FastMap.newInstance();
                params.put("custId", userLogin.getString("custId"));
                params.put("integralNum", integralNum.toString());
                params.put("getWay", "1");
                params.put("changeType", "181"); // 赠送积分
                params.put("serialType", "181");
                params.put("integralType", "1"); // 因赠送扣减积分
                params.put("productBrandId", "1");
                params.put("channelId", "10002"); // 虚拟店铺
                params.put("isCreateOrder", "N");

                Map<String, Object> result = dispatcher.runSync("modifyCustomerIntegralCrm23", params);
                if ("ARGS_FAIL".equals(result.get("expStatus")) && UtilValidate.isNotEmpty(result.get("data"))) {
                    Integer partyScoreShareValue = new Long(integralNum).intValue(); // 用户分享积分
                    Integer partyScore = Integer.parseInt(result.get("data").toString()); // 用户可分享的积分
                    if (partyScoreShareValue > partyScore) {
                        request.setAttribute("error", "可分享积分不足");
                        response.setStatus(403);
                        return "error";
                    }
                }
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        // =============扣减用户积分 end ==============
        //获取会员积分账户信息
//        GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", partyId));
//        if (UtilValidate.isEmpty(partyScore)) {
//            request.setAttribute("error", "可分享积分不足");
//            response.setStatus(403);
//            return "error";
//        }
//        Long scoreValue = partyScore.getLong("scoreValue");
//        Long value = partyScoreShare.getLong("scoreValue");
//        if (scoreValue < value) {
//            request.setAttribute("error", "可分享积分不足");
//            response.setStatus(403);
//            return "error";
//        }
//        partyScore.set("scoreValue", scoreValue - value);
//        partyScore.store();
//        //积分流水记录
//        GenericValue partyScoreHis = delegator.makeValue("PartyScoreHistory");
//        partyScoreHis.put("partyScoreHistoryId", delegator.getNextSeqId("PartyScoreHistory"));
//        partyScoreHis.put("partyId", partyId);
//        partyScoreHis.put("scoreValue", value);
//        partyScoreHis.put("residualScore", scoreValue - value);
//        partyScoreHis.put("getWay", "OUT");
//        partyScoreHis.put("createDate", UtilDateTime.nowTimestamp());
//        partyScoreHis.create();
        return "success";
    }

    /**
     * 分享记录
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getScoreShareDetail(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //分享记录Id
        String partyScoreShareId = request.getParameter("partyScoreShareId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(partyScoreShareId) && UtilValidate.isNotEmpty(jsonObject.get("partyScoreShareId"))) {
            partyScoreShareId = (String) jsonObject.get("partyScoreShareId");
        }
        if (UtilValidate.isEmpty(partyScoreShareId)) {
            request.setAttribute("error", "分享记录编号不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue partyScoreShare = delegator.findByPrimaryKey("PartyScoreShare", UtilMisc.toMap("partyScoreShareId", partyScoreShareId));
        if (UtilValidate.isEmpty(partyScoreShare)) {
            request.setAttribute("error", "分享记录不存在");
            response.setStatus(403);
            return "error";
        }
        GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyScoreShare.get("sharePartyId")));
        request.setAttribute("nickName", person.get("nickname"));
        request.setAttribute("scoreValue", partyScoreShare.get("scoreValue"));
        request.setAttribute("isGet", UtilValidate.isEmpty(partyScoreShare.get("getPartyId")) ? "N" : "Y");
        return "success";
    }

    /**
     * 领取积分
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getPartyScore(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //分享记录Id
        String partyScoreShareId = request.getParameter("partyScoreShareId");
        //手机号
        String mobile = request.getParameter("mobile");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(partyScoreShareId) && UtilValidate.isNotEmpty(jsonObject.get("partyScoreShareId"))) {
            partyScoreShareId = (String) jsonObject.get("partyScoreShareId");
        }
        if (UtilValidate.isEmpty(mobile) && UtilValidate.isNotEmpty(jsonObject.get("mobile"))) {
            mobile = (String) jsonObject.get("mobile");
        }
        if (UtilValidate.isEmpty(partyScoreShareId)) {
            request.setAttribute("error", "分享记录编号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(mobile)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        List<GenericValue> tobeStore = FastList.newInstance();
        GenericValue partyScoreShare = delegator.findByPrimaryKey("PartyScoreShare", UtilMisc.toMap("partyScoreShareId", partyScoreShareId));
        if (UtilValidate.isEmpty(partyScoreShare)) {
            request.setAttribute("error", "分享记录不存在");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isNotEmpty(partyScoreShare.get("getPartyId"))) {
            request.setAttribute("error", "该积分已领取");
            response.setStatus(403);
            return "error";
        }
        Long value = partyScoreShare.getLong("scoreValue");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", mobile));
        if (UtilValidate.isEmpty(userLogin) || UtilValidate.areEqual("N", userLogin.get("enabled"))) {
            request.setAttribute("error", "账户不存在");
            response.setStatus(403);
            return "error";
        }

        // =============用户领取积分 begin ============
        try {
            if (UtilValidate.isNotEmpty(value)) {
                LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
                Map<String, Object> params = FastMap.newInstance();
                params.put("custId", userLogin.getString("custId"));
                params.put("integralNum", value.toString());
                params.put("getWay", "0"); // 增加积分
                params.put("changeType", "182"); // 赠送积分
                params.put("serialType", "182");
                params.put("integralType", "1"); // 领取的赠送积分
                params.put("productBrandId", "1");
                params.put("channelId", "10002"); // 虚拟店铺
                params.put("isCreateOrder", "N");

                dispatcher.runSync("modifyCustomerIntegralCrm23", params);
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        // =============用户领取积分 end ==============
//        GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", userLogin.get("partyId")));
//        Long scoreValue = 0L;
//        //判断积分账户是否存在
//        if (UtilValidate.isNotEmpty(partyScore)) {
//            partyScore.set("scoreValue", partyScore.getLong("scoreValue") + value);
//        } else {
//            //不存在积分账户新增积分账户
//            partyScore = delegator.makeValue("PartyScore");
//            partyScore.put("partyId", userLogin.get("partyId"));
//            partyScore.put("scoreValue", value);
//        }
//        tobeStore.add(partyScore);
        //积分流水记录
//        GenericValue partyScoreHis = delegator.makeValue("PartyScoreHistory");
//        partyScoreHis.put("partyScoreHistoryId", delegator.getNextSeqId("PartyScoreHistory"));
//        partyScoreHis.put("partyId", userLogin.get("partyId"));
//        partyScoreHis.put("scoreValue", value);
//        partyScoreHis.put("residualScore", scoreValue + value);
//        partyScoreHis.put("getWay", "IN");
//        partyScoreHis.put("createDate", UtilDateTime.nowTimestamp());
//        tobeStore.add(partyScoreHis);
        if (UtilValidate.isNotEmpty(partyScoreShare.get("getPartyId"))) {
            request.setAttribute("error", "该积分已领取");
            response.setStatus(403);
            return "error";
        }
        partyScoreShare.set("getPartyId", userLogin.get("partyId"));
        partyScoreShare.set("getTime", UtilDateTime.nowTimestamp());
        tobeStore.add(partyScoreShare);
        delegator.storeAll(tobeStore);
        return "success";
    }

    /**
     * 余额流水获取接口
     * add by gss 2017.04.17
     *
     * @param
     * @param
     * @return
     */
    public static String getPartyAcountDetail(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map> resultList = FastList.newInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("PA", "PartyAccountDetail");
        dev.addAlias("PA", "detailId");
        dev.addAlias("PA", "description");
        dev.addAlias("PA", "resultType");
        dev.addAlias("PA", "amount");
        dev.addAlias("PA", "createDate");
        dev.addAlias("PA", "orderId");
        dev.addAlias("PA", "partyId");
        List<EntityCondition> conditions = FastList.newInstance();
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(userLogin)) {
            String partyId = userLogin.getString("partyId");
            if (UtilValidate.isEmpty(partyId)) {
                request.setAttribute("error", "会员不存在");
                response.setStatus(403);
                return "error";
            } else {
                try {
                    conditions.add(EntityCondition.makeCondition("partyId", partyId));
                    Boolean beganTransaction = TransactionUtil.begin();
                    EntityListIterator eli = delegator.findListIteratorByCondition(dev, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-createDate"), findOpts);
                    List<GenericValue> results = eli.getPartialList(lowIndex, viewSize);
                    int resultSize = eli.getResultsSizeAfterPartialList();
                    eli.close();
                    TransactionUtil.commit(beganTransaction);
                    //余额流水信息
                    for (GenericValue result : results) {
                        Map map = FastMap.newInstance();
                        /*流水号*/
                        map.put("detailId", result.get("detailId"));
                        /*发生流水动作*/
                        map.put("description", result.get("description"));
                        /* 发生流水结果，对应键值为 收益IN  支出 OUT*/
                        map.put("resultType", result.get("resultType"));
                        /*发生流水金额，精确到两位小数*/
                        map.put("amount", result.getBigDecimal("amount"));
                        /*发生流水时间，格式为：yyyy-mm-dd hh:mm:ss*/
                        map.put("createDate", dateFormat.format(result.getTimestamp("createDate")));
                        /*发生流水的订单号*/
                        map.put("orderId", result.get("orderId"));
                        resultList.add(map);
                    }
                    request.setAttribute("resultList", resultList);
                    request.setAttribute("max", resultSize);
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        } else {
            request.setAttribute("error", "登录用户不存在");
            response.setStatus(403);
            return "error";
        }
        return "success";
    }

    /**
     * 车辆收藏  Add By gss
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     */
    public static String getProductCollection(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map> tagList = FastList.newInstance();
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");

        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        //商品(车辆)id
        String productId = request.getParameter("productId");
        if (UtilValidate.isEmpty(productId) && UtilValidate.isNotEmpty(jsonObject.get("productId"))) {
            productId = jsonObject.getString("productId");
        }
        String flag = request.getParameter("flag");
        if (UtilValidate.isEmpty(flag) && UtilValidate.isNotEmpty(jsonObject.get("flag"))) {
            flag = jsonObject.getString("flag");
        }

        GenericValue userLogin = null;
        Boolean status = false;
        if (UtilValidate.isEmpty(productId)) {
            request.setAttribute("error", "商品不存在");
            response.setStatus(403);
            return "error";
        }
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(userLogin)) {
                String partyId = userLogin.getString("partyId");
                // 查询车辆收藏记录
                List<GenericValue> productCollection = delegator.findByAnd("ProductCollection",
                        UtilMisc.toMap("partyId", partyId, "productId", productId));
                if (UtilValidate.isNotEmpty(productCollection)) {
                    if (UtilValidate.areEqual("Y", flag)) {
                        delegator.removeAll(productCollection);
                        status = false;
                    } else {
                        status = true;
                    }
                } else {
                    if (UtilValidate.areEqual("Y", flag)) {
                        // 创建收藏信息
                        GenericValue collection = delegator.makeValue("ProductCollection");
                        collection.set("partyId", partyId);
                        collection.set("productId", productId);
                        collection.create();
                        status = true;
                    } else {
                        status = false;
                    }
                }
            } else {
                request.setAttribute("error", "登录用户不存在");
                response.setStatus(403);
                return "error";
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("status", status);
        return "success";
    }

    /**
     * 我的爱车列表  Add By gss
     *
     * @param request
     * @param response
     * @return
     */
    public static String getPartyFavoriteCarList(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<Map> resultList = FastList.newInstance();
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("PFC", "PartyFavoriteCar");
        dynamicViewViewEntity.addMemberEntity("PB", "ProductBrand");
        dynamicViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        dynamicViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        dynamicViewViewEntity.addAliasAll("PFC", null, null);
        dynamicViewViewEntity.addAlias("PFC", "partyFavoriteCarId");
        dynamicViewViewEntity.addAlias("PFC", "isDefault");
        dynamicViewViewEntity.addAlias("PFC", "createdStamp");
        dynamicViewViewEntity.addAlias("PB", "brandName");
        dynamicViewViewEntity.addAlias("PB", "contentId");
        dynamicViewViewEntity.addAlias("PCM", "carModelName");
        dynamicViewViewEntity.addAlias("PCM", "carDisplacementId");
        dynamicViewViewEntity.addAlias("PCM", "carYearId");
        dynamicViewViewEntity.addAlias("PCS", "carSeriesName");
        dynamicViewViewEntity.addViewLink("PFC", "PB", true, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));
        dynamicViewViewEntity.addViewLink("PFC", "PCM", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        dynamicViewViewEntity.addViewLink("PFC", "PCS", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        List<EntityCondition> conditions = FastList.newInstance();
        boolean beganTransaction;
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(userLogin)) {
                String partyId = userLogin.getString("partyId");
                conditions.add(EntityCondition.makeCondition("partyId", partyId));
                beganTransaction = TransactionUtil.begin();
                // 查询我的爱车
                EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-isDefault", "-createdStamp"), findOpts);
                TransactionUtil.commit(beganTransaction);
                List<GenericValue> favoriteCarList = pli.getPartialList(lowIndex, viewSize);
                int resultSize = pli.getResultsSizeAfterPartialList();
                pli.close();
               /* List<GenericValue> favoriteCarList = pli.getCompleteList();*/
                if (UtilValidate.isNotEmpty(favoriteCarList)) {
                    for (GenericValue result : favoriteCarList) {
                        Map map = FastMap.newInstance();
                        //爱车ID
                        map.put("partyFavoriteCarId", result.get("partyFavoriteCarId"));
                        //品牌名称
                        map.put("brandName", result.get("brandName"));
                        //品牌图片url
                        map.put("imgUrl", UtilValidate.isNotEmpty(result.get("contentId")) ? (request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + result.get("contentId")) : "");
                        //品牌Id
                        map.put("productBrandId", result.get("productBrandId"));
                        //车系Id
                        map.put("productCarSeriesId", result.get("productCarSeriesId"));
                        //车型Id
                        map.put("productCarModelId", result.get("productCarModelId"));
                        //车型
                        map.put("carModelName", result.get("carModelName"));
                        //车系
                        map.put("carSeriesName", result.get("carSeriesName"));
                        //排量
                        map.put("carDisplacementId", result.get("carDisplacementId"));
                        //年份
                        map.put("carYearId", result.get("carYearId"));
                        //是否是默认车
                        map.put("isDefault", result.get("isDefault"));
                        //行驶里程
                        map.put("carMileage", result.get("carMileage"));
                        //上路时间
                        map.put("useTime", UtilValidate.isNotEmpty(result.getTimestamp("useTime")) ? sdf.format(result.getTimestamp("useTime")) : "");
                        //车牌号
                        map.put("carNum", result.get("carNum"));
                        //保险到期
                        map.put("insuranceEndTime", UtilValidate.isNotEmpty(result.getTimestamp("insuranceEndTime")) ? sdf.format(result.getTimestamp("insuranceEndTime")) : "");
                        resultList.add(map);
                    }
                }
                request.setAttribute("resultList", resultList);
                request.setAttribute("max", resultSize);
            } else {
                request.setAttribute("error", "登录用户不存在");
                response.setStatus(403);
                return "error";
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 我的爱车信息更新  Add By gss
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     */
    public static String updatePartyFavoriteCar(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        String partyFavoriteCarId = request.getParameter("partyFavoriteCarId");
        String carMileage = request.getParameter("carMileage");
        Timestamp useTime = UtilValidate.isNotEmpty(request.getParameter("useTime")) ? Timestamp.valueOf(request.getParameter("useTime") + " 00:00:00") : null;
        String carNum = request.getParameter("carNum");
        Timestamp insuranceEndTime = UtilValidate.isNotEmpty(request.getParameter("insuranceEndTime")) ? Timestamp.valueOf(request.getParameter("insuranceEndTime") + " 00:00:00") : null;
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(partyFavoriteCarId) && UtilValidate.isNotEmpty(jsonObject.get("partyFavoriteCarId"))) {
            partyFavoriteCarId = jsonObject.getString("partyFavoriteCarId");
        }
        if (UtilValidate.isEmpty(carMileage) && UtilValidate.isNotEmpty(jsonObject.get("carMileage"))) {
            carMileage = jsonObject.getString("carMileage");
        }
        if (UtilValidate.isEmpty(carNum) && UtilValidate.isNotEmpty(jsonObject.get("carNum"))) {
            carNum = jsonObject.getString("carNum");
        }
        if (UtilValidate.isEmpty(useTime) && UtilValidate.isNotEmpty(jsonObject.get("useTime"))) {
            useTime = Timestamp.valueOf(jsonObject.getString("useTime") + " 00:00:00");
        }
        if (UtilValidate.isEmpty(insuranceEndTime) && UtilValidate.isNotEmpty(jsonObject.get("insuranceEndTime"))) {
            insuranceEndTime = Timestamp.valueOf(jsonObject.getString("insuranceEndTime") + " 00:00:00");
        }
        if (UtilValidate.isEmpty(partyFavoriteCarId)) {
            request.setAttribute("error", "爱车Id不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue userLogin = null;
        boolean beganTransaction;
        Boolean status = false;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(userLogin)) {
                String partyId = userLogin.getString("partyId");
                beganTransaction = TransactionUtil.begin();
                // 查询我的爱车
                GenericValue partyFavoriteCar = delegator.findByPrimaryKey("PartyFavoriteCar", UtilMisc.toMap("partyFavoriteCarId", partyFavoriteCarId));
                TransactionUtil.commit(beganTransaction);
                if (UtilValidate.isNotEmpty(partyFavoriteCar)) {
                    if (UtilValidate.isNotEmpty(carMileage)) {
                        partyFavoriteCar.set("carMileage", Long.valueOf(carMileage));
                    }
                    if (UtilValidate.isNotEmpty(useTime)) {
                        partyFavoriteCar.set("useTime", useTime);
                    }
                    if (UtilValidate.isNotEmpty(carNum)) {
                        partyFavoriteCar.set("carNum", carNum);
                    }
                    if (UtilValidate.isNotEmpty(insuranceEndTime)) {
                        partyFavoriteCar.set("insuranceEndTime", insuranceEndTime);
                    }
                    partyFavoriteCar.store();
                    status = true;
                    // ============同步资产到CRM begin spj============
                    LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
                    // 保存同步信息
                    Map<String, Object> params = FastMap.newInstance();
                    params.put("rowId", partyFavoriteCar.get("rowId")); // 主键ID(CRM)
                    params.put("custId", userLogin.getString("custId")); // 客户编号
                    params.put("vehiModelId", partyFavoriteCar.get("productCarModelId"));
                    params.put("mileage", partyFavoriteCar.getLong("carMileage").intValue()); // 行驶里程
                    params.put("roadTime", partyFavoriteCar.getTimestamp("useTime")); // 上路时间
                    params.put("vehiNum", partyFavoriteCar.getString("carNum")); // 车牌号
                    params.put("insuranceEndDate", partyFavoriteCar.getTimestamp("insuranceEndTime")); // 保险到期日期

                    Map<String, Object> result = FastMap.newInstance();
                    result = dispatcher.runSync("syncCustVehiCrm14", params);
                    if ("SUCCESS".equals(result.get("expStatus"))) {
                        String rowId = result.get("data").toString();
                        if (UtilValidate.isNotEmpty(rowId)) {
                            partyFavoriteCar.set("rowId", rowId);
                            delegator.store(partyFavoriteCar);
                        }
                    }

                    // ============同步资产到CRM end spj============
                } else {
                    request.setAttribute("error", "爱车信息不存在");
                    response.setStatus(403);
                    return "error";
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("status", status);
        return "success";
    }


    /**
     * 我的爱车更新车型信息
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     */
    public static String updateFavoriteCar(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String partyFavoriteCarId = request.getParameter("partyFavoriteCarId");
        if (UtilValidate.isEmpty(partyFavoriteCarId) && UtilValidate.isNotEmpty(jsonObject.get("partyFavoriteCarId"))) {
            partyFavoriteCarId = jsonObject.getString("partyFavoriteCarId");
        }
        if (UtilValidate.isEmpty(partyFavoriteCarId)) {
            request.setAttribute("error", "爱车Id不能为空");
            response.setStatus(403);
            return "error";
        }
        String productBrandId = request.getParameter("productBrandId");
        if (UtilValidate.isEmpty(productBrandId) && UtilValidate.isNotEmpty(jsonObject.get("productBrandId"))) {
            productBrandId = jsonObject.getString("productBrandId");
        }
        if (UtilValidate.isEmpty(productBrandId)) {
            request.setAttribute("error", "品牌Id不能为空");
            response.setStatus(403);
            return "error";
        }
        String productCarSeriesId = request.getParameter("productCarSeriesId");
        if (UtilValidate.isEmpty(productCarSeriesId) && UtilValidate.isNotEmpty(jsonObject.get("productCarSeriesId"))) {
            productCarSeriesId = jsonObject.getString("productCarSeriesId");
        }
        if (UtilValidate.isEmpty(productCarSeriesId)) {
            request.setAttribute("error", "车系Id不能为空");
            response.setStatus(403);
            return "error";
        }
        String productCarModelId = request.getParameter("productCarModelId");
        if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        if (UtilValidate.isEmpty(productCarModelId)) {
            request.setAttribute("error", "车型Id不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue partyFavoriteCar = delegator.findByPrimaryKey("PartyFavoriteCar", UtilMisc.toMap("partyFavoriteCarId", partyFavoriteCarId));
        if (UtilValidate.isNotEmpty(partyFavoriteCar)) {
            partyFavoriteCar.set("productBrandId", productBrandId);
            partyFavoriteCar.set("productCarSeriesId", productCarSeriesId);
            partyFavoriteCar.set("productCarModelId", productCarModelId);
            delegator.store(partyFavoriteCar);
            request.setAttribute("status", true);
        } else {
            request.setAttribute("status", false);
        }
        return "success";
    }

    /**
     * 我的爱车信息删除  Add By gss
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     */
    public static String deletePartyFavoriteCar(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String partyFavoriteCarId = request.getParameter("partyFavoriteCarId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(partyFavoriteCarId) && UtilValidate.isNotEmpty(jsonObject.get("partyFavoriteCarId"))) {
            partyFavoriteCarId = jsonObject.getString("partyFavoriteCarId");
        }
        if (UtilValidate.isEmpty(partyFavoriteCarId)) {
            request.setAttribute("error", "爱车Id不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue userLogin = null;
        boolean beganTransaction;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            beganTransaction = TransactionUtil.begin();
            // 查询我的爱车
            GenericValue partyFavoriteCar = delegator.findByPrimaryKey("PartyFavoriteCar", UtilMisc.toMap("partyFavoriteCarId", partyFavoriteCarId));
            TransactionUtil.commit(beganTransaction);
            partyFavoriteCar.remove();
            // ============同步资产到CRM begin spj============
            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            // 保存同步信息
            Map<String, Object> params = FastMap.newInstance();
            params.put("rowId", partyFavoriteCar.get("rowId")); // 主键ID(CRM)
            params.put("custId", userLogin.getString("custId")); // 客户编号
            params.put("vehiModelId", partyFavoriteCar.get("productCarModelId"));
            params.put("status", "1"); // 删除

            dispatcher.runSync("syncCustVehiCrm14", params);

            // ============同步资产到CRM end spj============
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("status", true);
        return "success";
    }

    /**
     * 我的爱车新增  Add By gss
     *
     * @param request
     * @param response
     * @return
     * @throws GenericServiceException
     */
    public static String createPartyFavoriteCar(HttpServletRequest request, HttpServletResponse response) throws GenericServiceException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        String productBrandId = request.getParameter("productBrandId");
        String productCarSeriesId = request.getParameter("productCarSeriesId");
        String productCarModelId = request.getParameter("productCarModelId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(productBrandId) && UtilValidate.isNotEmpty(jsonObject.get("productBrandId"))) {
            productBrandId = jsonObject.getString("productBrandId");
        }
        if (UtilValidate.isEmpty(productBrandId)) {
            request.setAttribute("error", "品牌编号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        if (UtilValidate.isEmpty(productCarModelId)) {
            request.setAttribute("error", "车型编号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(productCarSeriesId) && UtilValidate.isNotEmpty(jsonObject.get("productCarSeriesId"))) {
            productCarSeriesId = jsonObject.getString("productCarSeriesId");
        }
        if (UtilValidate.isEmpty(productCarSeriesId)) {
            request.setAttribute("error", "车系编号不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue userLogin = null;
        boolean beganTransaction;
        Boolean status = false;
        String partyFavoriteCarId = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(userLogin)) {
                String partyId = (String) userLogin.get("partyId");
                List<GenericValue> partyFavoriteCars = delegator.findByAnd("PartyFavoriteCar", UtilMisc.toMap("partyId", partyId, "productBrandId", productBrandId, "productCarSeriesId", productCarSeriesId, "productCarModelId", productCarModelId));
                if (UtilValidate.isNotEmpty(partyFavoriteCars)) {
                    request.setAttribute("partyFavoriteCarId", partyFavoriteCars.get(0).get("partyFavoriteCarId"));
                    return "success";
                }
                //我的爱车
                Map<String, Object> partyFavoriteCar = FastMap.newInstance();
                partyFavoriteCarId = delegator.getNextSeqId("PartyFavoriteCar");
                partyFavoriteCar.put("partyFavoriteCarId", partyFavoriteCarId);
                partyFavoriteCar.put("partyId", partyId);
                partyFavoriteCar.put("productBrandId", productBrandId);
                partyFavoriteCar.put("productCarSeriesId", productCarSeriesId);
                partyFavoriteCar.put("productCarModelId", productCarModelId);
                GenericValue partyScoreHis = delegator.makeValue("PartyFavoriteCar", partyFavoriteCar);
                try {
                    beganTransaction = TransactionUtil.begin();
                    partyScoreHis.create();
                    TransactionUtil.commit(beganTransaction);

                    // ============同步资产到CRM begin spj============
                    LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
//                  // 保存同步信息
                    Map<String, Object> params = FastMap.newInstance();
                    params.put("custId", userLogin.getString("custId")); // 客户编号
//                  // 获取车型信息
                    GenericValue productCarModel = delegator.findByPrimaryKey("ProductCarModel", UtilMisc.toMap("productCarModelId", productCarModelId));
                    params.put("vehiModelId", productCarModelId); // 车型编号
                    params.put("vehiModelName", productCarModel.getString("carModelName")); // 车型名称
//                  // 获取车辆品牌
                    GenericValue productBrand = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", productBrandId));
                    params.put("vehiBrand", productBrand.getString("brandName")); // 爱车品牌
//                  // 获取爱车信息
                    GenericValue partyFCar = delegator.findByPrimaryKey("PartyFavoriteCar", UtilMisc.toMap("partyFavoriteCarId", partyFavoriteCarId));
                    params.put("roadTime", partyFCar.getDate("useTime")); // 上路时间
                    params.put("mileage", partyFCar.getBigDecimal("carMileage")); // 行驶里程
                    params.put("vehiNum", partyFCar.getString("carNum")); // 车牌号
                    params.put("insuranceEndDate", partyFCar.getDate("insuranceEndTime")); // 保险到期日期

                    GenericValue productCarSeries = delegator.findByPrimaryKey("ProductCarSeries", UtilMisc.toMap("productCarSeriesId", productCarSeriesId));
                    params.put("vehiTypeName", productCarSeries.get("carSeriesName")); // 车系品牌名称

//                  // 获取商品信息
//                    GenericValue product = delegator.findByAnd("Product", UtilMisc.toMap("productCarModelId", productCarModelId, "productCarSeriesId", productCarSeriesId, "productCarLibBrandId", productBrandId)).get(0);
//                    if ("N".equals(product.getString("isTransferName"))) {
//                        params.put("isTransfer", 0); // 是否过户
//                    } else {
//                        params.put("isTransfer", 1); // 是否过户
//                    }

//                    params.put("engineNum", ); // 发动机号（ 没有字段）
//                    params.put("maintenanceDate", ); // 上次保养时间（ 没有字段）
//                    params.put("purchaseChannel", ); // 购买渠道（ 没有字段）

                    Map<String, Object> resultCRM = dispatcher.runSync("syncCustVehiCrm14", params);

                    String ss = String.valueOf(resultCRM.get("expStatus"));
                    if ("SUCCESS".equals(ss)) {
                        String rowId = String.valueOf(resultCRM.get("data")); // 返回
                        partyFCar.set("rowId", rowId);
                        delegator.store(partyFCar); // 回写CRM车辆id
                    }

                    // ============同步资产到CRM end spj============
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            } else {
                request.setAttribute("error", "登录用户不存在");
                response.setStatus(403);
                return "error";
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("status", true);
        request.setAttribute("partyFavoriteCarId", partyFavoriteCarId);
        return "success";
    }

    /**
     * 设置默认我的爱车信息 Add By gss
     *
     * @param request
     * @param response
     * @return
     */
    public static String setDefaultFavoriteCar(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String partyFavoriteCarId = request.getParameter("partyFavoriteCarId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(partyFavoriteCarId) && UtilValidate.isNotEmpty(jsonObject.get("partyFavoriteCarId"))) {
            partyFavoriteCarId = jsonObject.getString("partyFavoriteCarId");
        }
        if (UtilValidate.isEmpty(partyFavoriteCarId)) {
            request.setAttribute("error", "爱车Id不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue userLogin = null;
        boolean beganTransaction;
        try {
            beganTransaction = TransactionUtil.begin();
            // 查询我的爱车
            GenericValue partyFavoriteCar = delegator.findByPrimaryKey("PartyFavoriteCar", UtilMisc.toMap("partyFavoriteCarId", partyFavoriteCarId));
            if (UtilValidate.isNotEmpty(partyFavoriteCar)) {
                delegator.storeByCondition("PartyFavoriteCar", UtilMisc.toMap("isDefault", "N"), EntityCondition.makeCondition("partyId", partyFavoriteCar.get("partyId")));
                partyFavoriteCar.set("isDefault", "Y");
                partyFavoriteCar.store();
            } else {
                request.setAttribute("error", "爱车信息不存在");
                response.setStatus(403);
                return "error";
            }
            TransactionUtil.commit(beganTransaction);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("status", true);
        return "success";
    }


    /**
     * 我的反馈列表 新增 Add By gss
     *
     * @param request
     * @param response
     * @return
     */
    public static String getFeedBackList(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("F", "Feedback");
        dynamicViewViewEntity.addAlias("F", "feedbackId");
        dynamicViewViewEntity.addAlias("F", "createPartyId");
        dynamicViewViewEntity.addAlias("F", "createDate");
        dynamicViewViewEntity.addAlias("F", "feedbackContent");
        dynamicViewViewEntity.addAlias("F", "replyDate");
        dynamicViewViewEntity.addAlias("F", "replyContent");
        dynamicViewViewEntity.addAlias("F", "contactMethod");
        List<EntityCondition> conditions = FastList.newInstance();
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            GenericValue userLogin = null;
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(userLogin)) {
                conditions.add(EntityCondition.makeCondition("createPartyId", userLogin.getString("partyId")));
                Boolean beganTransaction = TransactionUtil.begin();
                EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-createDate"), findOpts);
                List<GenericValue> feedbackList = eli.getPartialList(lowIndex, viewSize);
                int resultSize = eli.getResultsSizeAfterPartialList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                List<Map> resultList = FastList.newInstance();
                for (GenericValue feedback : feedbackList) {
                    Map map = FastMap.newInstance();
                    //反馈ID
                    map.put("feedbackId", feedback.get("feedbackId"));
                    //反馈内容
                    map.put("feedbackContent", feedback.get("feedbackContent"));
                    //客服回复内容
                    map.put("replyContent", feedback.get("replyContent"));
                    //反馈时间
                    map.put("createDate", dateFormat.format(feedback.getTimestamp("createDate")));
                    //客服回复时间
                    map.put("replyDate", UtilValidate.isNotEmpty(feedback.get("replyDate")) ? dateFormat.format(feedback.getTimestamp("replyDate")) : "");
                    //反馈图片
                    List<String> contentIds = FastList.newInstance();
                    List<GenericValue> feedbackContents = delegator.findByAnd("FeedbackContent", UtilMisc.toMap("feedbackId", feedback.get("feedbackId")));
                    if (UtilValidate.isNotEmpty(feedbackContents)) {
                        for (GenericValue feedbackContent : feedbackContents) {
                            contentIds.add(request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + feedbackContent.get("contentId"));
                        }
                    }
                    map.put("imageUrl", contentIds);
                    resultList.add(map);
                }
                request.setAttribute("resultList", resultList);
                request.setAttribute("max", resultSize);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 我的订单列表 新增 Add By gss 2017-5-03
     *
     * @param request
     * @param response
     * @return
     */
    public static String getMyOrderList(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        // 订单类型
        String orderType = request.getParameter("orderType");
        // 订单状态
        String orderStatus = request.getParameter("orderStatus");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        //登录用户ID
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        if (UtilValidate.isEmpty(orderType) && UtilValidate.isNotEmpty(jsonObject.get("orderType"))) {
            orderType = (String) jsonObject.get("orderType");
        }
        if (UtilValidate.isEmpty(orderStatus) && UtilValidate.isNotEmpty(jsonObject.get("orderStatus"))) {
            orderStatus = (String) jsonObject.get("orderStatus");
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("OH", "OrderHeader");
        dev.addMemberEntity("ORE", "OrderRole");
        dev.addMemberEntity("UL", "UserLogin");
        dev.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        dev.addViewLink("ORE", "UL", false, UtilMisc.toList(new ModelKeyMap("partyId", "partyId")));
        dev.addAlias("OH", "orderId");
        dev.addAlias("UL", "userLoginId");
        dev.addAlias("ORE", "roleTypeId");
        dev.addAlias("OH", "salesOrderType");
        dev.addAlias("OH", "statusId");
        dev.addAlias("OH", "notPayMoney");
        dev.addAlias("OH", "actualPayMoney");
        dev.addAlias("OH", "shouldFrontMoney");
        dev.addAlias("OH", "entryDate");
        List<EntityCondition> conditions = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderType)) {
            conditions.add(EntityCondition.makeCondition("salesOrderType", EntityOperator.IN, UtilMisc.toListArray(orderType.split(","))));
        } else {
            conditions.add(EntityCondition.makeCondition("salesOrderType", EntityOperator.NOT_EQUAL, "INTEGRAL_ORDER"));
        }
        if (UtilValidate.isNotEmpty(orderStatus) && "ORDER_WAITPAY".equals(orderStatus)) {
            //订单状态为  待付款ORDER_WAITPAY
            conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_WAITPAY", "ORDER_WAITPAYFINAL")));
        } else if (UtilValidate.isNotEmpty(orderStatus) && "ORDER_WAITPRODUCE".equals(orderStatus)) {//待验证
            //订单状态为待验证 ORDER_WAITPRODUCE
            conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITPRODUCE"));
        } else if (UtilValidate.isNotEmpty(orderStatus) && "ORDER_WAITEVALUATE".equals(orderStatus)) {//待评价
            conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITEVALUATE"));
        }
        conditions.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        conditions.add(EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, userLoginId));
        EntityCondition cond = null;
        if (conditions.size() > 0) {
            cond = EntityCondition.makeCondition(conditions, EntityOperator.AND);
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dev, cond, null, null, UtilMisc.toList("-entryDate"), findOpts);
            List<GenericValue> orderList = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> resultList = FastList.newInstance();
            for (GenericValue order : orderList) {
                List<Map> resultLists = FastList.newInstance();
                Map map = FastMap.newInstance();
                //订单编号
                map.put("orderId", order.get("orderId"));
                map.put("orderTypeId", order.get("salesOrderType"));
                //未在线支付金额
                if ("BUY_CAR_ORDER".equals(order.get("salesOrderType"))) {
                    map.put("notPayMoney", order.get("shouldFrontMoney"));
                } else {
                    map.put("notPayMoney", order.get("notPayMoney"));
                }
                //实付金额
                map.put("actualPayMoney", order.get("actualPayMoney"));
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", order.get("orderId")));
                for (GenericValue orderItem : orderItems) {
                    Map maps = FastMap.newInstance();
                    // 商品数量
                    maps.put("quantity", orderItem.getBigDecimal("quantity").intValue());
                    // 商品价格
                    maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        //商品图片url
                        maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                    }
                    maps.put("productName", orderItem.get("itemDescription"));
                    maps.put("productId", orderItem.get("productId"));
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get("productId")));
                    maps.put("productTypeId", product.get("productTypeId"));
                    resultLists.add(maps);
                }
                //商品信息
                map.put("productList", resultLists);
                //订单状态
                map.put("statusId", order.get("statusId"));
                //订单完成时间
                List<GenericValue> orderStatusList = delegator.findByAnd("OrderStatus", UtilMisc.toMap("orderId", order.get("orderId"), "statusId", "ORDER_COMPLETED"));
                if (UtilValidate.isNotEmpty(orderStatusList)) {
                    map.put("completedTime", UtilDateTime.timeStampToString(orderStatusList.get(0).getTimestamp("statusDatetime"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                }
                resultList.add(map);
            }
            request.setAttribute("resultList", resultList);
            request.setAttribute("max", resultSize);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 我的订单详情 Add By gss 2017-5-03
     *
     * @param request
     * @param response
     * @return
     */
    public static String getMyOrderDetail(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        // 订单类型
        String orderId = request.getParameter("orderId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(orderId)) {
            orderId = jsonObject.getString("orderId");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        //登录用户ID
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        List<GenericValue> orderRole = delegator.findByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", userLogin.get("partyId")));
        if (UtilValidate.isEmpty(orderRole)) {
            request.setAttribute("error", "订单不存在");
            response.setStatus(403);
            return "success";
        }
        List<Map> resultLists = FastList.newInstance();
        GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        //订单头信息
        if (UtilValidate.isNotEmpty(orderHeader)) {
            // 订单类型
            String orderTypes = orderHeader.getString("salesOrderType");
            // 点单编号
            request.setAttribute("orderId", orderHeader.get("orderId"));
            // 订单总金额
            request.setAttribute("grandTotal", orderHeader.getBigDecimal("grandTotal").setScale(2, BigDecimal.ROUND_HALF_UP));
            // 下单时间
            request.setAttribute("orderDate", dateFormat.format(orderHeader.getTimestamp("orderDate")));
            //实付金额
            request.setAttribute("actualPayMoney", orderHeader.get("actualPayMoney"));
            List<GenericValue> orderPaymentPreferences = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
            String paymentType = null;
            if (UtilValidate.isNotEmpty(orderPaymentPreferences)) {
                GenericValue paymentMethod = delegator.findByPrimaryKey("PaymentMethodType", UtilMisc.toMap("paymentMethodTypeId", orderPaymentPreferences.get(0).get("paymentMethodTypeId")));
                paymentType = UtilValidate.isNotEmpty(orderPaymentPreferences) ? paymentMethod.getString("paymentMethodTypeId") : null;
            }
            // 支付方式
            request.setAttribute("paymentType", paymentType);
            // 订单状态
            request.setAttribute("orderStatusId", orderHeader.get("statusId"));

            // 应付尾款
            request.setAttribute("notPayMoney", UtilValidate.isNotEmpty(orderHeader.get("notPayMoney")) ? orderHeader.getBigDecimal("notPayMoney").setScale(2, BigDecimal.ROUND_HALF_UP) : 0);
            if ("BUY_CAR_ORDER".equals(orderTypes)) {
                // 定金
                request.setAttribute("shouldFrontMoney", orderHeader.getBigDecimal("shouldFrontMoney").setScale(2, BigDecimal.ROUND_HALF_UP));
                GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", orderHeader.get("productStoreId")));
                String storeName = UtilValidate.isNotEmpty(productStore.getString("storeName")) ? productStore.getString("storeName") : null;
                //提车门店
                request.setAttribute("storeName", storeName);
                // 购车人
                request.setAttribute("buyerName", orderHeader.get("buyerName"));
                // 手机号
                request.setAttribute("buyerTelphone", orderHeader.get("buyerTelphone"));
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                for (GenericValue orderItem : orderItems) {
                    Map maps = FastMap.newInstance();
                    // 商品数量
                    maps.put("quantity", orderItem.getBigDecimal("quantity").setScale(2, BigDecimal.ROUND_HALF_UP));
                    // 商品价格
                    maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        //商品图片url
                        maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                    }
                    maps.put("productName", orderItem.get("itemDescription"));
                    maps.put("productId", orderItem.get("productId"));
                    GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get("productId")));
                    maps.put("productTypeId", product.get("productTypeId"));

                    if (UtilValidate.isNotEmpty(product.get("featureProductId"))) {
                        String[] featureProductIds = product.getString("featureProductId").split("\\|");
                        String feature = "";
                        for (String featureProductId : featureProductIds) {
                            GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", featureProductId));
                            if (UtilValidate.isNotEmpty(productFeature)) {
                                GenericValue productFeatureType = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeature.get("productFeatureTypeId")));
                                if (UtilValidate.isNotEmpty(productFeatureType)) {
                                    feature += productFeatureType.getString("productFeatureTypeName") + "：" + productFeature.getString("productFeatureName") + " ";
                                }
                            }
                        }
                        maps.put("feature", feature);
                    }
                    resultLists.add(maps);
                }
                //商品信息
                request.setAttribute("productList", resultLists);
            } else if ("MAINTAIN_CAR_ORDER".equals(orderTypes)) {  //保养订单
                GenericValue communityStore = delegator.findByPrimaryKey("CommunityStore", UtilMisc.toMap("commStoreId", orderHeader.get("commStoreId")));
                String storeName = communityStore.getString("storeName");
                //配送门店
                request.setAttribute("storeName", storeName);
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                for (GenericValue orderItem : orderItems) {
                    Map maps = FastMap.newInstance();
                    // 商品数量
                    maps.put("quantity", orderItem.getBigDecimal("quantity").setScale(2, BigDecimal.ROUND_HALF_UP));
                    // 商品价格
                    maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        //商品图片url
                        maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                    }
                    maps.put("productName", orderItem.get("itemDescription"));
                    maps.put("productId", orderItem.get("productId"));
                    resultLists.add(maps);
                }
                //商品信息
                request.setAttribute("productList", resultLists);
                List<GenericValue> orderHeaderExtend = delegator.findByAnd("OrderHeaderExtend", UtilMisc.toMap("orderId", orderId));
                //预约时间
                if (UtilValidate.isNotEmpty(orderHeaderExtend)) {
                    request.setAttribute("maintenance", orderHeaderExtend.get(0).get("maintenance"));
                }
                //保养项目
                List<Map> serviceList = FastList.newInstance();
                List<GenericValue> orderMaintainReferences = delegator.findByAnd("OrderMaintainReference", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                for (GenericValue orderMaintainReference : orderMaintainReferences) {
                    Map maps = FastMap.newInstance();
                    maps.put("serviceCharge", orderMaintainReference.get("maintainServicePrice"));
                    GenericValue productCarMaintainInfo = delegator.findByPrimaryKey("ProductCarMaintainInfo", UtilMisc.toMap("productCarMaintainInfoId", orderMaintainReference.get("productCarMaintainInfoId")));
                    if (UtilValidate.isNotEmpty(productCarMaintainInfo)) {
                        maps.put("serviceName", productCarMaintainInfo.get("serviceChargeId"));
                    }
                    serviceList.add(maps);
                }
                request.setAttribute("serviceList", serviceList);
                request.setAttribute("buyerName", orderHeader.get("buyerName"));
                request.setAttribute("buyerTelphone", orderHeader.get("buyerTelphone"));
            } else if ("SELL_CAR_ORDER".equals(orderTypes)) { // 卖车评估订单
                //查询卖车预约单
                List<GenericValue> reserveItem = delegator.findByAnd("ReserveItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                if (UtilValidate.isNotEmpty(reserveItem)) {
                    //订单行项目
                    List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                    for (GenericValue orderItem : orderItems) {
                        Map maps = FastMap.newInstance();
                        // 商品数量
                        maps.put("quantity", orderItem.getBigDecimal("quantity").intValue());
                        // 商品价格
                        maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                        List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                        if (UtilValidate.isNotEmpty(productContents)) {
                            //商品图片url
                            maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                        }
                        maps.put("productName", orderItem.get("itemDescription"));
                        maps.put("productId", orderItem.get("productId"));
                        resultLists.add(maps);
                    }
                    //商品信息
                    request.setAttribute("productList", resultLists);
                    GenericValue item = reserveItem.get(0);
                    //预约单号 reserveId
                    request.setAttribute("reserveId", item.get("reserveId"));
                    //检测费用
                    request.setAttribute("unitPrice", item.getBigDecimal("unitPrice"));
                    // 车款
                    List<GenericValue> orderHeaderExtend = delegator.findByAnd("OrderHeaderExtend", UtilMisc.toMap("orderId", item.get("orderId")));
                    if (UtilValidate.isNotEmpty(orderHeaderExtend)) {
                        GenericValue productCarModel = delegator.findByPrimaryKey("ProductCarModel", UtilMisc.toMap("productCarModelId", orderHeaderExtend.get(0).get("productCarModelId")));
                        request.setAttribute("carModelName", productCarModel.get("carModelName"));
                    }
                    GenericValue reserveHeader = delegator.findByPrimaryKey("ReserveHeader", UtilMisc.toMap("reserveId", item.get("reserveId")));
                    //carValue  车估值
                    request.setAttribute("carValue", reserveHeader.getBigDecimal("carValue"));
                    //useDate  上路时间
                    request.setAttribute("useDate", dateFormat.format(reserveHeader.getDate("useDate")));
                    //行驶历程 mileage
                    request.setAttribute("mileage", reserveHeader.getBigDecimal("mileage").divide(new BigDecimal(10000)));
                    //提交人（预约人）reservePerson
                    request.setAttribute("reservePerson", reserveHeader.get("reservePerson"));
                    GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", reserveHeader.get("productStoreId")));
                    String productStoreAddress = "";
                    if (UtilValidate.isNotEmpty(productStore.get("stateProvinceGeoId"))) {
                        GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("stateProvinceGeoId")));
                        productStoreAddress += geo.getString("geoName");
                    }
                    if (UtilValidate.isNotEmpty(productStore.get("cityGeoId"))) {
                        GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("cityGeoId")));
                        productStoreAddress += geo.getString("geoName");
                    }
                    if (UtilValidate.isNotEmpty(productStore.get("countyGeoId"))) {
                        GenericValue geo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("countyGeoId")));
                        productStoreAddress += geo.getString("geoName");
                    }
                    if (UtilValidate.isNotEmpty(productStore.get("address"))) {
                        productStoreAddress += productStore.getString("address");
                    }
                    //验车地址
                    request.setAttribute("productStoreAddress", productStoreAddress);
                } else {
                    request.setAttribute("error", "卖车预约单不存在");
                    response.setStatus(403);
                    return "error";
                }
            } else if ("REPAIR_ORDER".equals(orderTypes)) {//维修订单
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                request.setAttribute("productId", orderItems.get(0).getString("productId"));
                // 车款
                List<GenericValue> orderHeaderExtend = delegator.findByAnd("OrderHeaderExtend", UtilMisc.toMap("orderId", orderItems.get(0).get("orderId")));
                if (UtilValidate.isNotEmpty(orderHeaderExtend)) {
                    GenericValue productCarModel = delegator.findByPrimaryKey("ProductCarModel", UtilMisc.toMap("productCarModelId", orderHeaderExtend.get(0).get("productCarModelId")));
                    request.setAttribute("carModelName", productCarModel.get("carModelName"));
                }
                //预约人
                request.setAttribute("reservePerson", orderHeader.get("servicePersonName"));
                //手机号
                request.setAttribute("reserveTel", orderHeader.get("buyerTelphone"));
                //  预约时间
                request.setAttribute("reserveTime", orderHeaderExtend.get(0).get("maintenance"));
                //维修项 repairContent
                if (UtilValidate.isNotEmpty(orderHeader.get("repairType"))) {
                    String[] repairTypes = orderHeader.getString("repairType").split(",");
                    List<String> repairContents = FastList.newInstance();
                    for (String repairType : repairTypes) {
                        GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", repairType));
                        if (UtilValidate.isNotEmpty(enumeration)) {
                            repairContents.add(enumeration.getString("description"));
                        }
                    }
                    request.setAttribute("repairContent", repairContents);
                }
                //维修标签
                if (UtilValidate.isNotEmpty(orderHeaderExtend)) {
                    List<String> repairTags = FastList.newInstance();
                    String repairTagIds = orderHeaderExtend.get(0).getString("repairTagIds");
                    if (UtilValidate.isNotEmpty(repairTagIds)) {
                        String[] repairTagIdList = repairTagIds.split(",");
                        for (String repairTagId : repairTagIdList) {
                            GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", repairTagId));
                            if (UtilValidate.isNotEmpty(enumeration)) {
                                repairTags.add(enumeration.getString("description"));
                            }
                        }
                    }
                    request.setAttribute("repairTags", repairTags);
                    request.setAttribute("repairDescription", orderHeaderExtend.get(0).getString("carCondition"));
                }
                GenericValue communityStore = delegator.findByPrimaryKey("CommunityStore", UtilMisc.toMap("commStoreId", orderHeader.get("commStoreId")));
                String storeName = "";
                if (UtilValidate.isNotEmpty(communityStore)) {
                    storeName = communityStore.getString("storeName");
                }
                //维修门店（预约单 预约门店）
                request.setAttribute("storeName", storeName);
                List<String> imgUrlList = FastList.newInstance();
                //查询维修凭证
                List<GenericValue> repairOrderContents = delegator.findByAnd("RepairOrderContent", UtilMisc.toMap("orderId", orderId));
                if (UtilValidate.isNotEmpty(repairOrderContents)) {
                    for (GenericValue content : repairOrderContents) {
                        imgUrlList.add(request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + content.getString("contentId"));
                    }
                }
                // 维修凭证 图片URL
                request.setAttribute("imgUrlList", imgUrlList);
                for (GenericValue orderItem : orderItems) {
                    Map maps = FastMap.newInstance();
                    // 商品数量
                    maps.put("quantity", orderItem.getBigDecimal("quantity").intValue());
                    // 商品价格
                    maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        //商品图片url
                        maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                    }
                    maps.put("productName", orderItem.get("itemDescription"));
                    maps.put("productId", orderItem.get("productId"));
                    resultLists.add(maps);
                }
                //商品信息
                request.setAttribute("productList", resultLists);
            } else if ("INSURANCE_ORDER".equals(orderTypes)) {//保险订单
                List<GenericValue> orderHeaderExtend = delegator.findByAnd("OrderHeaderExtend", UtilMisc.toMap("orderId", orderId));

                //车牌号
                request.setAttribute("licencePlate", orderHeader.get("licencePlate"));
                //车主姓名
                request.setAttribute("appointmentName", orderHeader.get("appointmentName"));
                //身份证号
                request.setAttribute("idNumber", orderHeader.get("idNumber"));
                //是否过户 Y/N 是/否
                request.setAttribute("isTransferNames", orderHeader.get("isTransferNames"));

                String businessName = null;
                GenericValue partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", orderHeader.get("insureBusinessName")));
                if (UtilValidate.isNotEmpty(partyBusiness)) {
                    businessName = partyBusiness.getString("businessName");
                }
                // 投保公司
                request.setAttribute("businessName", businessName);
                //投保截止日期
                request.setAttribute("insuranceClosingTime", UtilDateTime.timeStampToString(orderHeaderExtend.get(0).getTimestamp("insuranceClosingTime"), "yyyy-MM-dd", timeZone, locale));
                //投保费用(应付金额)
                request.setAttribute("shouldPayMoney", UtilValidate.isNotEmpty(orderHeader.get("shouldPayMoney")) ? orderHeader.getBigDecimal("shouldPayMoney").setScale(2, BigDecimal.ROUND_HALF_UP) : 0);
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                List<Map> productList = FastList.newInstance();
                for (GenericValue orderItem : orderItems) {
                    Map maps = FastMap.newInstance();
                    maps.put("productId", orderItem.get("productId"));
                    maps.put("productName", orderItem.get("itemDescription"));
                    productList.add(maps);
                }
                //险种列表
                request.setAttribute("productList", productList);
            } else if ("PARETS_ORDER".equals(orderTypes)) {//配件订单
                //配送方式 写死的
                GenericValue communityStore = delegator.findByPrimaryKey("CommunityStore", UtilMisc.toMap("commStoreId", orderHeader.get("commStoreId")));
                String storeName = communityStore.getString("storeName");
                //配送门店
                request.setAttribute("storeName", storeName);
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                for (GenericValue orderItem : orderItems) {
                    Map maps = FastMap.newInstance();
                    // 商品数量
                    maps.put("quantity", orderItem.getBigDecimal("quantity").intValue());
                    // 商品价格
                    maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        //商品图片url
                        maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                    }
                    maps.put("productName", orderItem.get("itemDescription"));
                    maps.put("productId", orderItem.get("productId"));
                    resultLists.add(maps);
                }
                //商品信息
                request.setAttribute("productList", resultLists);
                request.setAttribute("buyerName", orderHeader.get("buyerName"));
                request.setAttribute("buyerTelphone", orderHeader.get("buyerTelphone"));
            } else if ("PAY_ORDER".equals(orderTypes)) {//买单订单
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                request.setAttribute("productId", orderItems.get(0).get("productId"));
                //配送门店
                request.setAttribute("storeName", orderItems.get(0).get("itemDescription"));
            } else if ("RENEW_ORDER".equals(orderTypes)) {//续费订单
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                for (GenericValue orderItem : orderItems) {
                    Map maps = FastMap.newInstance();
                    // 商品数量
                    maps.put("quantity", orderItem.getBigDecimal("quantity").intValue());
                    // 商品价格
                    maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        //商品图片url
                        maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                    }
                    maps.put("productName", orderItem.get("itemDescription"));
                    maps.put("productId", orderItem.get("productId"));
                    resultLists.add(maps);
                }
                //商品信息
                request.setAttribute("productList", resultLists);
                request.setAttribute("buyerName", orderHeader.get("buyerName"));
                request.setAttribute("buyerTelphone", orderHeader.get("buyerTelphone"));
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItems.get(0).get("productId")));
                request.setAttribute("salesDiscontinuationDate", UtilDateTime.timeStampToString(product.getTimestamp("salesDiscontinuationDate"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                request.setAttribute("renewDays", product.getBigDecimal("renewDays").setScale(0, BigDecimal.ROUND_HALF_UP).intValue());

            } else if ("HOTEL_ORDER".equals(orderTypes)) {//酒店订单
                List<GenericValue> orderHeaderExtend = delegator.findByAnd("OrderHeaderExtend", UtilMisc.toMap("orderId", orderId));
                List<GenericValue> orderHotelPrices = delegator.findByAnd("OrderHotelPrice", UtilMisc.toMap("orderId", orderId));
                //订单行项目
                List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItems.get(0).get("productId")));
                GenericValue mainProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product.get("mainProductId")));
                GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", product.get("featureProductId")));
                //房型ID
                request.setAttribute("productId", product.get("productId"));
                //酒店名称
                request.setAttribute("productName", mainProduct.get("productName"));
                //酒店地址
                request.setAttribute("merchantAddress", mainProduct.get("merchantAddress"));
                //房型名称
                request.setAttribute("productFeatureName", productFeature.get("productFeatureName"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", product.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    //房型图片
                    request.setAttribute("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                request.setAttribute("quantity", orderItems.get(0).get("quantity"));//数量
                request.setAttribute("name", orderHeader.get("buyerName"));//姓名
                request.setAttribute("telphone", orderHeader.get("buyerTelphone"));   //电话
                request.setAttribute("chkInHotelDate", UtilDateTime.timeStampToString(orderHeaderExtend.get(0).getTimestamp("chkInHotelDate"), "yyyy-MM-dd", timeZone, locale));   //住店日期
                request.setAttribute("chkOutHotelDate", UtilDateTime.timeStampToString(orderHeaderExtend.get(0).getTimestamp("chkOutHotelDate"), "yyyy-MM-dd", timeZone, locale));   //离店日期
                request.setAttribute("arriveHotelLatest", UtilDateTime.timeStampToString(orderHeaderExtend.get(0).getTimestamp("arriveHotelLatest"), "yyyy-MM-dd HH:mm", timeZone, locale));   //最晚到店时间
                if (UtilValidate.isNotEmpty(orderHotelPrices)) {
                    List<Map> orderHotelPriceList = FastList.newInstance();
                    for (GenericValue orderHotelPrice : orderHotelPrices) {
                        Map map = FastMap.newInstance();
                        map.put("date", UtilDateTime.timeStampToString(orderHotelPrice.getTimestamp("date"), "yyyy-MM-dd", timeZone, locale));
                        map.put("price", orderHotelPrice.get("price"));
                        orderHotelPriceList.add(map);
                    }
                    request.setAttribute("orderHotelPriceList", orderHotelPriceList);
                }
            }
        }
        //客服电话
        GenericValue frontRule = delegator.findByPrimaryKey("FrontRule", UtilMisc.toMap("frontRuleId", "front_rule"));
        if (UtilValidate.isNotEmpty(frontRule)) {
            request.setAttribute("customerServiceNumber", frontRule.get("customerServiceNumber"));
        }
        //订单完成时间
        List<GenericValue> orderStatusList = delegator.findByAnd("OrderStatus", UtilMisc.toMap("orderId", orderId, "statusId", "ORDER_COMPLETED"));
        if (UtilValidate.isNotEmpty(orderStatusList)) {
            request.setAttribute("completedTime", UtilDateTime.timeStampToString(orderStatusList.get(0).getTimestamp("statusDatetime"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
        }
        //退款单
        List<GenericValue> returnHeaders = delegator.findByAnd("ReturnHeader", UtilMisc.toMap("orderId", orderId));
        if (UtilValidate.isNotEmpty(returnHeaders)) {
            request.setAttribute("returnId", returnHeaders.get(0).get("returnId"));
            request.setAttribute("returnTotal", returnHeaders.get(0).get("applyTotal"));
            request.setAttribute("returnTime", UtilDateTime.timeStampToString(returnHeaders.get(0).getTimestamp("entryDate"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
        }
        return "success";
    }

    /**
     * PC端订单列表接口
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getOrderList(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        //登录用户ID
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        // 订单类型
        String orderType = request.getParameter("orderType");
        if (UtilValidate.isEmpty(orderType) && UtilValidate.isNotEmpty(jsonObject.get("orderType"))) {
            orderType = (String) jsonObject.get("orderType");
        }
        // 订单状态
        String orderStatus = request.getParameter("orderStatus");
        if (UtilValidate.isEmpty(orderStatus) && UtilValidate.isNotEmpty(jsonObject.get("orderStatus"))) {
            orderStatus = (String) jsonObject.get("orderStatus");
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前页数不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 20;
        }
        int lowIndex = (viewIndex - 1) * viewSize + 1;
        int highIndex = viewIndex * viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("OH", "OrderHeader");
        dev.addAlias("OH", "orderId");
        dev.addAlias("OH", "salesOrderType");
        dev.addAlias("OH", "statusId");
        dev.addAlias("OH", "notPayMoney");
        dev.addAlias("OH", "shouldFrontMoney");
        dev.addAlias("OH", "entryDate");
        dev.addAlias("OH", "grandTotal");
        dev.addAlias("OH", "actualPayMoney");
        dev.addMemberEntity("ORE", "OrderRole");
        dev.addAlias("ORE", "roleTypeId");
        dev.addAlias("ORE", "partyId");
        dev.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        List<EntityCondition> conditions = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderType)) {
            conditions.add(EntityCondition.makeCondition("salesOrderType", EntityOperator.IN, UtilMisc.toListArray(orderType.split(","))));
        } else {
            conditions.add(EntityCondition.makeCondition("salesOrderType", EntityOperator.NOT_EQUAL, "INTEGRAL_ORDER"));
        }
        if (UtilValidate.isNotEmpty(orderStatus) && "ORDER_WAITPAY".equals(orderStatus)) {//待付款
            conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_WAITPAY", "ORDER_WAITPAYFINAL")));
        } else if (UtilValidate.isNotEmpty(orderStatus) && "ORDER_WAITPRODUCE".equals(orderStatus)) {//待验证
            conditions.add(EntityCondition.makeCondition("statusId", orderStatus));
        } else if (UtilValidate.isNotEmpty(orderStatus) && "ORDER_WAITEVALUATE".equals(orderStatus)) {//待评价
            conditions.add(EntityCondition.makeCondition("statusId", orderStatus));
        }
        conditions.add(EntityCondition.makeCondition("roleTypeId", "PLACING_CUSTOMER"));
        conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
        EntityCondition cond = null;
        if (conditions.size() > 0) {
            cond = EntityCondition.makeCondition(conditions, EntityOperator.AND);
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        Boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator eli = delegator.findListIteratorByCondition(dev, cond, null, null, UtilMisc.toList("-entryDate"), findOpts);
        List<GenericValue> orderList = eli.getPartialList(lowIndex, viewSize);
        int resultSize = eli.getResultsSizeAfterPartialList();
        eli.close();
        TransactionUtil.commit(beganTransaction);
        List<Map> resultList = FastList.newInstance();
        for (GenericValue order : orderList) {
            List<Map> resultLists = FastList.newInstance();
            Map map = FastMap.newInstance();
            //订单编号
            map.put("orderId", order.get("orderId"));
            map.put("orderTypeId", order.get("salesOrderType"));
            map.put("grandTotal", order.get("grandTotal"));
            map.put("entryDate", UtilDateTime.timeStampToString(order.getTimestamp("entryDate"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            //未在线支付金额
            if ("BUY_CAR_ORDER".equals(order.get("salesOrderType"))) {
                map.put("notPayMoney", order.get("shouldFrontMoney"));
            } else {
                map.put("notPayMoney", order.get("notPayMoney"));
            }
            //实付金额
            map.put("actualPayMoney", order.get("actualPayMoney"));
            //订单行项目
            List<GenericValue> orderItems = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", order.get("orderId")));
            for (GenericValue orderItem : orderItems) {
                Map maps = FastMap.newInstance();
                // 商品数量
                maps.put("quantity", orderItem.getBigDecimal("quantity").intValue());
                // 商品价格
                maps.put("unitPrice", orderItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", orderItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    //商品图片url
                    maps.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId"));
                }
                maps.put("productName", orderItem.get("itemDescription"));
                maps.put("productId", orderItem.get("productId"));
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", orderItem.get("productId")));
                if (UtilValidate.isNotEmpty(product.get("featureProductId"))) {
                    String[] featureProductIds = product.getString("featureProductId").split("\\|");
                    String feature = "";
                    for (String featureProductId : featureProductIds) {
                        GenericValue productFeature = delegator.findByPrimaryKey("ProductFeature", UtilMisc.toMap("productFeatureId", featureProductId));
                        if (UtilValidate.isNotEmpty(productFeature)) {
                            GenericValue productFeatureType = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId", productFeature.get("productFeatureTypeId")));
                            if (UtilValidate.isNotEmpty(productFeatureType)) {
                                feature += productFeatureType.getString("productFeatureTypeName") + "：" + productFeature.getString("productFeatureName") + " ";
                            }
                        }
                    }
                    maps.put("feature", feature);
                }
                resultLists.add(maps);
            }
            //商品信息
            map.put("productList", resultLists);
            //订单状态
            map.put("statusId", order.get("statusId"));
            //订单完成时间
            List<GenericValue> orderStatusList = delegator.findByAnd("OrderStatus", UtilMisc.toMap("orderId", order.get("orderId"), "statusId", "ORDER_COMPLETED"));
            if (UtilValidate.isNotEmpty(orderStatusList)) {
                map.put("completedTime", UtilDateTime.timeStampToString(orderStatusList.get(0).getTimestamp("statusDatetime"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            }
            resultList.add(map);
        }
        request.setAttribute("resultList", resultList);
        request.setAttribute("max", resultSize % viewSize == 0 ? resultSize / viewSize : resultSize / viewSize + 1);

        return "success";
    }

    /**
     * PC端订单数量
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getOrderNum(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        //登录用户ID
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        // 订单类型
        String orderType = request.getParameter("orderType");
        if (UtilValidate.isEmpty(orderType) && UtilValidate.isNotEmpty(jsonObject.get("orderType"))) {
            orderType = (String) jsonObject.get("orderType");
        }
        // 订单状态
        String orderStatus = request.getParameter("orderStatus");
        if (UtilValidate.isEmpty(orderStatus) && UtilValidate.isNotEmpty(jsonObject.get("orderStatus"))) {
            orderStatus = (String) jsonObject.get("orderStatus");
        }
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("OH", "OrderHeader");
        dev.addAlias("OH", "orderId");
        dev.addAlias("OH", "salesOrderType");
        dev.addAlias("OH", "statusId");
        dev.addAlias("OH", "entryDate");
        dev.addMemberEntity("ORE", "OrderRole");
        dev.addAlias("ORE", "roleTypeId");
        dev.addAlias("ORE", "partyId");
        dev.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
        List<EntityCondition> conditions = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderType)) {
            conditions.add(EntityCondition.makeCondition("salesOrderType", EntityOperator.IN, UtilMisc.toListArray(orderType.split(","))));
        } else {
            conditions.add(EntityCondition.makeCondition("salesOrderType", EntityOperator.NOT_EQUAL, "INTEGRAL_ORDER"));
        }
        if (UtilValidate.isNotEmpty(orderStatus)) {
            if (UtilValidate.isNotEmpty(orderStatus) && "ORDER_WAITPAY".equals(orderStatus)) {//待付款
                conditions.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_WAITPAY", "ORDER_WAITPAYFINAL")));
            } else {
                conditions.add(EntityCondition.makeCondition("statusId", orderStatus));
            }
        }
        conditions.add(EntityCondition.makeCondition("roleTypeId", "PLACING_CUSTOMER"));
        conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
        EntityCondition cond = null;
        if (conditions.size() > 0) {
            cond = EntityCondition.makeCondition(conditions, EntityOperator.AND);
        }
        Boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator eli = delegator.findListIteratorByCondition(dev, cond, null, null, null, null);
        int resultSize = eli.getResultsSizeAfterPartialList();
        eli.close();
        TransactionUtil.commit(beganTransaction);

        request.setAttribute("num", resultSize);

        return "success";
    }

    /**
     * 获取评价标签
     *
     * @param request
     * @param response
     * @return
     */
    public static String getReviewLabel(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productIds = request.getParameter("productIds");
        if (UtilValidate.isEmpty(productIds) && UtilValidate.isNotEmpty(jsonObject.get("productIds"))) {
            productIds = jsonObject.getString("productIds");
        }
        if (UtilValidate.isEmpty(productIds)) {
            request.setAttribute("error", "商品编号不能为空");
            response.setStatus(403);
            return "error";
        }
        String stars = request.getParameter("stars");
        if (UtilValidate.isEmpty(stars) && UtilValidate.isNotEmpty(jsonObject.get("stars"))) {
            stars = jsonObject.getString("stars");
        }
        if (UtilValidate.isEmpty(stars)) {
            request.setAttribute("error", "评价星级不能为空");
            response.setStatus(403);
            return "error";
        }
        String[] productIdList = productIds.split(",");
        String[] starList = stars.split(",");
        List<Map> labels = FastList.newInstance();
        for (int i = 0; i < productIdList.length; i++) {
            Map labelMap = FastMap.newInstance();
            labelMap.put("productId", productIdList[i]);
            List<String> productCategoryIds = FastList.newInstance();
            try {
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productIdList[i]));
                if (UtilValidate.isEmpty(product)) {
                    request.setAttribute("error", "商品不存在");
                    response.setStatus(403);
                    return "error";
                }
                String productCategoryId = product.getString("primaryProductCategoryId");
                productCategoryIds.add(productCategoryId);
                GenericValue productCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategoryId));
                if (UtilValidate.isEmpty(productCategory)) {
                    return "success";
                }
                if (UtilValidate.isNotEmpty(productCategory.get("primaryParentCategoryId"))) {
                    productCategoryIds.add(productCategory.getString("primaryParentCategoryId"));
                    productCategory = delegator.findByPrimaryKey("ProductCategory", UtilMisc.toMap("productCategoryId", productCategory.getString("primaryParentCategoryId")));
                    if (UtilValidate.isNotEmpty(productCategory.get("primaryParentCategoryId"))) {
                        productCategoryIds.add(productCategory.getString("primaryParentCategoryId"));
                    }
                }
                List<EntityCondition> conditions = FastList.newInstance();
                conditions.add(EntityCondition.makeCondition("isUse", "0"));
                conditions.add(EntityCondition.makeCondition("productCategoryId", EntityOperator.IN, productCategoryIds));
                conditions.add(EntityCondition.makeCondition("star", new BigDecimal(starList[i])));
                List<GenericValue> reviewLabels = delegator.findList("ReviewLabel", EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, false);
                List<Map> reviewLabelList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(reviewLabels)) {
                    for (GenericValue reviewLabel : reviewLabels) {
                        Map map = FastMap.newInstance();
                        map.put("reviewLabelId", reviewLabel.get("reviewLabelId"));
                        map.put("name", reviewLabel.get("name"));
                        reviewLabelList.add(map);
                    }
                }
                labelMap.put("reviewLabelList", reviewLabelList);
                labels.add(labelMap);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        request.setAttribute("reviewLabels", labels);
        return "success";
    }

    /**
     * 获取保养服务列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getMaintainType(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> maintainTypes = null;
        try {
            maintainTypes = delegator.findList("MaintainType", EntityCondition.makeCondition("isUsed", "0"), UtilMisc.toSet("maintainTypeId", "maintainTypeName"), UtilMisc.toList("createdStamp"), null, false);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("maintainTypes", maintainTypes);
        return "success";
    }

    /**
     * 获取保养计划
     *
     * @param request
     * @param response
     * @return
     */
    public static String getMaintainReference(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productCarModelId = null;
        if (UtilValidate.isNotEmpty(request.getParameter("productCarModelId"))) {
            productCarModelId = request.getParameter("productCarModelId");
        } else if (UtilValidate.isEmpty(productCarModelId) && UtilValidate.isNotEmpty(jsonObject.get("productCarModelId"))) {
            productCarModelId = jsonObject.getString("productCarModelId");
        }
        if (UtilValidate.isEmpty(productCarModelId)) {
            request.setAttribute("error", "车型编号不能为空");
            response.setStatus(403);
            return "error";
        }
        List<Map> maintainReferenceList = FastList.newInstance();
        List<GenericValue> maintainReferences = null;
        try {
            maintainReferences = delegator.findList("MaintainReference", EntityCondition.makeCondition("productCarModelId", productCarModelId), null, UtilMisc.toList("maintainKilometers"), null, false);
            if (UtilValidate.isNotEmpty(maintainReferences)) {
                for (GenericValue maintainReference : maintainReferences) {
                    Map referenceMap = FastMap.newInstance();
                    referenceMap.put("maintainKilometers", maintainReference.get("maintainKilometers"));
                    referenceMap.put("maintainMonth", maintainReference.get("maintainMonth"));
                    List<GenericValue> maintainTypeReferences = delegator.findByAnd("MaintainTypeReference", UtilMisc.toMap("maintainReferenceId", maintainReference.get("maintainReferenceId")));
                    List<String> maintainTypes = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(maintainTypeReferences)) {
                        for (GenericValue maintainTypeReference : maintainTypeReferences) {
                            maintainTypes.add(maintainTypeReference.getString("maintainTypeId"));
                        }
                    }
                    referenceMap.put("maintainTypes", maintainTypes);
                    maintainReferenceList.add(referenceMap);
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("maintainReferenceList", maintainReferenceList);
        return "success";
    }

    /**
     * 活动详情
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getActivityManager(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productActivityManagerId = request.getParameter("productActivityManagerId");
        if (UtilValidate.isEmpty(productActivityManagerId) && UtilValidate.isNotEmpty(jsonObject.get("productActivityManagerId"))) {
            productActivityManagerId = jsonObject.getString("productActivityManagerId");
        }
        if (UtilValidate.isEmpty(productActivityManagerId)) {
            request.setAttribute("error", "活动编号不能为空");
            response.setStatus(403);
            return "error";
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺编号不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue productActivityManager = delegator.findByPrimaryKey("ProductActivityManager", UtilMisc.toMap("productActivityManagerId", productActivityManagerId));
        if (UtilValidate.isEmpty(productActivityManager)) {
            request.setAttribute("error", "活动不存在");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isNotEmpty(productActivityManager.get("activityManagerText"))) {
            GenericValue content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", productActivityManager.get("activityManagerText")));
            if (UtilValidate.isNotEmpty(content)) {
                GenericValue contenttext = delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                if (UtilValidate.isNotEmpty(contenttext)) {
                    request.setAttribute("activityContent", UtilValidate.isNotEmpty(contenttext.get("textData")) ? contenttext.getString("textData").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
                }
            }
        }
        //活动商品
        DynamicViewEntity viewEntity = new DynamicViewEntity();
        viewEntity.addMemberEntity("PAMP", "ProductActivityManagerProduct");
        viewEntity.addAlias("PAMP", "productActivityManagerId");
        viewEntity.addAlias("PAMP", "productId");
        viewEntity.addAlias("PAMP", "discountPrice");
        viewEntity.addMemberEntity("P", "Product");
        viewEntity.addAlias("P", "productTypeId");
        viewEntity.addAlias("P", "carTypeId");
        viewEntity.addAlias("P", "productName");
        viewEntity.addAlias("P", "isOnline");
        viewEntity.addAlias("P", "isVerify");
        viewEntity.addAlias("P", "isDel");
        viewEntity.addAlias("P", "introductionDate");
        viewEntity.addAlias("P", "salesDiscontinuationDate");
        viewEntity.addAlias("P", "mainProductId");
        viewEntity.addViewLink("PAMP", "P", false, ModelKeyMap.makeKeyMapList("productId", "productId"));
        viewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        viewEntity.addAlias("PSPA", "productStoreId");
        viewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("productActivityManagerId", productActivityManagerId));
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));
        conditions.add(EntityCondition.makeCondition("isDel", "N"));
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        List<EntityCondition> list1 = FastList.newInstance();
        list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
        list1.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list1, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        Boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator eli = delegator.findListIteratorByCondition(viewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
        List<GenericValue> products = eli.getCompleteList();
        eli.close();
        TransactionUtil.commit(beganTransaction);
        List<Map> productList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(products)) {
            for (GenericValue product : products) {
                Map map = FastMap.newInstance();
                map.put("productId", product.get("productId"));
                map.put("productName", product.get("productName"));
                if (UtilValidate.areEqual("CAR_GOOD", product.get("productTypeId"))) {
                    map.put("productTypeId", product.get("carTypeId"));
                } else if (UtilValidate.areEqual("RECREATION_GOOD", product.get("productTypeId"))) {
                    map.put("linkType", product.get("recreationType"));
                } else {
                    map.put("productTypeId", product.get("productTypeId"));
                }
                //图片
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", product.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                }
                //售价
                List<GenericValue> productPriceSaleList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product.get("productId"), "productPriceTypeId", "DEFAULT_PRICE"));
                if (UtilValidate.isNotEmpty(productPriceSaleList)) {
                    BigDecimal productPrice;
                    if (UtilValidate.areEqual("CAR_GOOD", product.get("productTypeId"))) {
                        productPrice = productPriceSaleList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    } else {
                        productPrice = productPriceSaleList.get(0).getBigDecimal("price").setScale(2, BigDecimal.ROUND_HALF_UP);
                    }
                    map.put("productPrice", productPrice);
                }
                //销量
                GenericValue productCalculatedInfo = delegator.findByPrimaryKey("ProductCalculatedInfo", UtilMisc.toMap("productId", product.get("productId")));
                int saleNum = 0;
                if (UtilValidate.isNotEmpty(productCalculatedInfo) && UtilValidate.isNotEmpty(productCalculatedInfo.get("totalQuantityOrdered"))) {
                    saleNum = productCalculatedInfo.getBigDecimal("totalQuantityOrdered").intValue();
                }
                map.put("saleNum", saleNum);
                //评价条数
                DynamicViewEntity reviewViewEntity = new DynamicViewEntity();
                reviewViewEntity.addMemberEntity("PR", "ProductReview");
                reviewViewEntity.addAlias("PR", "num", "productId", null, false, false, "count");
                reviewViewEntity.addAlias("PR", "productId");
                beganTransaction = TransactionUtil.begin();
                eli = delegator.findListIteratorByCondition(reviewViewEntity, EntityCondition.makeCondition("productId", product.get("productId")), null, UtilMisc.toSet("num"), null, null);
                List<GenericValue> productReview = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                Long num = 0L;
                if (UtilValidate.isNotEmpty(productReview) && UtilValidate.isNotEmpty(productReview.get(0).get("num"))) {
                    num = productReview.get(0).getLong("num");
                }
                map.put("num", num);
                map.put("productRating", productCalculatedInfo.get("averageCustomerRating"));
                //评价星数
                productList.add(map);
            }
        }
        request.setAttribute("productList", productList);
        return "success";
    }

    /**
     * 关于我们
     *
     * @param request
     * @param response
     * @return
     */
    public static String getIntroduce(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("A", "Article");
        dynamicViewViewEntity.addAlias("A", "articleId");
        dynamicViewViewEntity.addAlias("A", "articleTypeId");
        dynamicViewViewEntity.addAlias("A", "articleStatus");
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("articleTypeId", "ABOUT_US"));
        conditions.add(EntityCondition.makeCondition("articleStatus", "2"));
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, findOpts);
            List<GenericValue> articles = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(articles)) {
                // 查询文章内容
                List<GenericValue> articleConContent = delegator.findByAnd("ArticleContent", UtilMisc.toMap("articleId", articles.get(0).get("articleId"), "articleContentTypeId", "ARTICLE_CONTENT"));
                if (UtilValidate.isNotEmpty(articleConContent)) {
                    GenericValue content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", articleConContent.get(0).get("contentId")));
                    if (UtilValidate.isNotEmpty(content)) {
                        GenericValue contenttext = delegator.findByPrimaryKey("ElectronicText", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                        if (UtilValidate.isNotEmpty(contenttext)) {
                            request.setAttribute("articleContent", UtilValidate.isNotEmpty(contenttext.get("textData")) ? contenttext.getString("textData").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 帮助分类
     *
     * @param request
     * @param response
     * @return
     */
    public static String getHelpCategory(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String channel = request.getParameter("channel");
        if (UtilValidate.isEmpty(channel) && UtilValidate.isNotEmpty(jsonObject.get("channel"))) {
            channel = jsonObject.getString("channel");
        }
        if (UtilValidate.isEmpty(channel)) {
            request.setAttribute("error", "请指定PC端或者移动端");
            response.setStatus(403);
            return "error";
        }
        try {
            List<GenericValue> helpCategorys = delegator.findList("HelpCategory", EntityCondition.makeCondition("showChannel", EntityOperator.LIKE, "%" + channel + "%"), UtilMisc.toSet("helpCategoryId", "categoryName", "isShow"), UtilMisc.toList("sequenceNum"), null, false);
            if (UtilValidate.isNotEmpty(helpCategorys)) {
                List<Map> helpCategoryList = FastList.newInstance();
                for (GenericValue helpCategory : helpCategorys) {
                    Map categoryMap = FastMap.newInstance();
                    categoryMap.put("helpCategoryId", helpCategory.get("helpCategoryId"));
                    categoryMap.put("categoryName", helpCategory.get("categoryName"));
                    categoryMap.put("isShow", helpCategory.get("isShow"));
                    List<EntityCondition> conditions = FastList.newInstance();
                    conditions.add(EntityCondition.makeCondition("helpCategoryId", helpCategory.get("helpCategoryId")));
                    conditions.add(EntityCondition.makeCondition("showChannel", EntityOperator.LIKE, "%" + channel + "%"));
                    List<GenericValue> helpInfos = delegator.findList("HelpInfo", EntityCondition.makeCondition(conditions, EntityOperator.AND), UtilMisc.toSet("helpInfoId", "helpTitle", "isShow"), UtilMisc.toList("sequenceNum"), null, false);
                    if (UtilValidate.isNotEmpty(helpInfos)) {
                        List<Map> helpInfoList = FastList.newInstance();
                        for (GenericValue helpInfo : helpInfos) {
                            Map helpInfoMap = FastMap.newInstance();
                            helpInfoMap.put("helpInfoId", helpInfo.get("helpInfoId"));
                            helpInfoMap.put("helpTitle", helpInfo.get("helpTitle"));
                            helpInfoMap.put("isShow", helpInfo.get("isShow"));
                            helpInfoList.add(helpInfoMap);
                        }
                        categoryMap.put("helpInfoList", helpInfoList);
                    }
                    helpCategoryList.add(categoryMap);
                }
                request.setAttribute("helpCategoryList", helpCategoryList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取帮助信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String getHelpInfo(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String helpInfoId = request.getParameter("helpInfoId");
        if (UtilValidate.isEmpty(helpInfoId) && UtilValidate.isNotEmpty(jsonObject.get("helpInfoId"))) {
            helpInfoId = jsonObject.getString("helpInfoId");
        }
        if (UtilValidate.isEmpty(helpInfoId)) {
            request.setAttribute("error", "帮助信息编号不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            GenericValue helpInfo = delegator.findByPrimaryKey("HelpInfo", UtilMisc.toMap("helpInfoId", helpInfoId));
            request.setAttribute("helpTitle", helpInfo.get("helpTitle"));
            request.setAttribute("helpContent", UtilValidate.isNotEmpty(helpInfo.get("helpContent")) ? helpInfo.getString("helpContent").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * PC端获取帮组中心
     *
     * @param request
     * @param response
     * @return
     */
    public static String getHelpInfoList(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String keyWord = request.getParameter("keyWord");
        if (UtilValidate.isEmpty(keyWord) && UtilValidate.isNotEmpty(jsonObject.get("keyWord"))) {
            keyWord = jsonObject.getString("keyWord");
        }

        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前页码不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 24;
        }
        int lowIndex = (viewIndex - 1) * viewSize + 1;
        int highIndex = viewIndex * viewSize;
        //车型视图
        DynamicViewEntity viewEntity = new DynamicViewEntity();
        viewEntity.addMemberEntity("H", "HelpInfo");
        viewEntity.addAlias("H", "helpInfoId");
        viewEntity.addAlias("H", "helpTitle");
        viewEntity.addAlias("H", "helpContent");
        viewEntity.addAlias("H", "sequenceNum");
        viewEntity.addAlias("H", "showChannel");
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            List<EntityCondition> conditions = FastList.newInstance();
            if (UtilValidate.isNotEmpty(keyWord)) {
                List<EntityCondition> keyWordConditions = FastList.newInstance();
                keyWordConditions.add(EntityCondition.makeCondition("helpTitle", EntityOperator.LIKE, "%" + keyWord + "%"));
                keyWordConditions.add(EntityCondition.makeCondition("helpContent", EntityOperator.LIKE, "%" + keyWord + "%"));
                conditions.add(EntityCondition.makeCondition(keyWordConditions, EntityOperator.OR));
            }
            conditions.add(EntityCondition.makeCondition("showChannel", EntityOperator.LIKE, "%P%"));

            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(viewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("sequenceNum"), findOpts);
            List<GenericValue> helpInfos = eli.getPartialList(lowIndex, highIndex);
            int max = eli.getResultsSizeAfterPartialList();
            request.setAttribute("max", max % viewSize == 0 ? max / viewSize : max / viewSize + 1);
            List<Map> helpInfoList = FastList.newInstance();
            for (GenericValue helpInfo : helpInfos) {
                Map helpInfoMap = FastMap.newInstance();
                helpInfoMap.put("helpInfoId", helpInfo.get("helpInfoId"));
                helpInfoMap.put("helpTitle", helpInfo.get("helpTitle"));
                helpInfoMap.put("helpContent", UtilValidate.isNotEmpty(helpInfo.get("helpContent")) ? helpInfo.getString("helpContent").replace("/images/", request.getAttribute("_SERVER_ROOT_URL_") + "/images/") : "");
                helpInfoList.add(helpInfoMap);
            }
            request.setAttribute("helpInfoList", helpInfoList);
            eli.close();
            TransactionUtil.commit(beganTransaction);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取预约单列表
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getReserveList(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String reserveType = request.getParameter("reserveType");
        if (UtilValidate.isEmpty(reserveType) && UtilValidate.isNotEmpty(jsonObject.get("reserveType"))) {
            reserveType = jsonObject.getString("reserveType");
        }
        String statusId = request.getParameter("statusId");
        if (UtilValidate.isEmpty(statusId) && UtilValidate.isNotEmpty(jsonObject.get("statusId"))) {
            statusId = jsonObject.getString("statusId");
        }
        /*String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺编号不能为空");
            response.setStatus(403);
            return "error";
        }*/
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("RH", "ReserveHeader");
        dynamicView.addAlias("RH", "reserveId");
        dynamicView.addAlias("RH", "reserveType");
        dynamicView.addAlias("RH", "statusId");
        dynamicView.addAlias("RH", "reserveTime");
        dynamicView.addAlias("RH", "reserveEndTime");
        dynamicView.addAlias("RH", "partyId");
//        dynamicView.addAlias("RH", "productStoreId");
        dynamicView.addAlias("RH", "productCarModelId");
        dynamicView.addAlias("RH", "grandTotal");
        dynamicView.addAlias("RH", "createTime");

        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
//        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        //按预约单类型
        if (UtilValidate.isNotEmpty(reserveType)) {
            conditions.add(EntityCondition.makeCondition("reserveType", reserveType));
        }
        //按状态查询
        if (UtilValidate.isNotEmpty(statusId)) {
            conditions.add(EntityCondition.makeCondition("statusId", statusId));
        }

        // set distinct on so we only get one row per order
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        Boolean beganTransaction = TransactionUtil.begin();
        // using list iterator
        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-createTime"), findOpts);

        int reserveListSize = pli.getResultsSizeAfterPartialList();
        List<Map> reserveList = FastList.newInstance();
        for (GenericValue reserve : pli.getPartialList(lowIndex, viewSize)) {
            Map reserveMap = FastMap.newInstance();
            reserveMap.put("reserveId", reserve.get("reserveId"));
            reserveMap.put("reserveType", reserve.get("reserveType"));
            reserveMap.put("statusId", reserve.get("statusId"));
            reserveMap.put("grandTotal", reserve.get("grandTotal"));
            reserveMap.put("createTime", UtilDateTime.timeStampToString(reserve.getTimestamp("createTime"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
            if (UtilValidate.areEqual("RESTAURANT", reserve.get("reserveType")) || UtilValidate.areEqual("LEISURE", reserve.get("reserveType"))) {
                reserveMap.put("reserveTime", UtilDateTime.timeStampToString(reserve.getTimestamp("reserveTime"), "yyyy-MM-dd HH:mm", timeZone, locale));
            } else {
                reserveMap.put("reserveTime", UtilDateTime.timeStampToString(reserve.getTimestamp("reserveTime"), "yyyy-MM-dd", timeZone, locale) + " " + UtilDateTime.timeStampToString(reserve.getTimestamp("reserveTime"), "HH:mm", timeZone, locale) + "-" + UtilDateTime.timeStampToString(reserve.getTimestamp("reserveEndTime"), "HH:mm", timeZone, locale));
            }
            List<GenericValue> reserveItems = delegator.findByAnd("ReserveItem", UtilMisc.toMap("reserveId", reserve.get("reserveId")));
            if (UtilValidate.areEqual("DRIVER", reserve.get("reserveType")) || UtilValidate.areEqual("REPAIR", reserve.get("reserveType")) || UtilValidate.areEqual("SELLCAR", reserve.get("reserveType"))) {//试驾、维修、卖车
                List<GenericValue> productCarModelContents = delegator.findByAnd("ProductCarModelContent", UtilMisc.toMap("productCarModelId", reserve.get("productCarModelId")));
                if (UtilValidate.isNotEmpty(productCarModelContents)) {
                    reserveMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productCarModelContents.get(0).getString("contentId"));
                }
            } else {
                if (UtilValidate.isNotEmpty(reserveItems)) {
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", reserveItems.get(0).get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    if (UtilValidate.isNotEmpty(productContents)) {
                        reserveMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                    }
                }
            }
            if (UtilValidate.areEqual("INSURANCE", reserve.get("reserveType"))) {//保险
                String itemDescriptions = "";
                for (GenericValue reserveItem : reserveItems) {
                    if (UtilValidate.isNotEmpty(reserveItem.get("itemDescription")) && !itemDescriptions.contains(reserveItem.getString("itemDescription"))) {
                        if (UtilValidate.isEmpty(itemDescriptions)) {
                            itemDescriptions = reserveItem.getString("itemDescription");
                        } else {
                            itemDescriptions += "/" + reserveItem.getString("itemDescription");
                        }
                    }
                }
                reserveMap.put("itemDescriptions", itemDescriptions);
            } else if (UtilValidate.areEqual("REPAIR", reserve.get("reserveType"))) {//维修
                List<GenericValue> reserveRepairs = delegator.findByAnd("ReserveRepair", UtilMisc.toMap("reserveId", reserve.get("reserveId")));
                String repairContents = "";
                if (UtilValidate.isNotEmpty(reserveRepairs)) {
                    for (GenericValue reserveRepair : reserveRepairs) {
                        GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", reserveRepair.get("repairId")));
                        if (UtilValidate.isNotEmpty(enumeration)) {
                            if (UtilValidate.isEmpty(repairContents)) {
                                repairContents = enumeration.getString("description");
                            } else {
                                repairContents += "/" + enumeration.getString("description");
                            }
                        }
                    }
                }
                reserveMap.put("repairContents", repairContents);
            } else if (UtilValidate.areEqual("SELLCAR", reserve.get("reserveType")) || UtilValidate.areEqual("DRIVER", reserve.get("reserveType"))) {//卖车，试驾
                GenericValue productCarModel = delegator.findByPrimaryKey("ProductCarModel", UtilMisc.toMap("productCarModelId", reserve.get("productCarModelId")));
                reserveMap.put("carModelName", productCarModel.get("carModelName"));
            } else if (UtilValidate.areEqual("RESTAURANT", reserve.get("reserveType")) || UtilValidate.areEqual("LEISURE", reserve.get("reserveType"))) {//餐厅，休闲
                if (UtilValidate.isNotEmpty(reserveItems)) {
                    reserveMap.put("businessName", reserveItems.get(0).get("businessName"));
                }
            }
            if (UtilValidate.isNotEmpty(reserveItems)) {
                String orderId = null;
                for (GenericValue reserveItem : reserveItems) {
                    if (UtilValidate.isNotEmpty(reserveItem.get("orderId"))) {
                        orderId = reserveItem.getString("orderId");
                        break;
                    }
                }
                if (UtilValidate.isNotEmpty(orderId)) {
                    reserveMap.put("orderId", orderId);
                    GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
                    reserveMap.put("orderStatus", orderHeader.get("statusId"));
                }
            }
            reserveList.add(reserveMap);
        }
        // close the list iterator
        pli.close();
        TransactionUtil.commit(beganTransaction);
        request.setAttribute("reserveList", reserveList);
        request.setAttribute("max", reserveListSize);

        return "success";

    }

    /**
     * 获取预约单数量
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getReserveNum(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String reserveType = request.getParameter("reserveType");
        if (UtilValidate.isEmpty(reserveType) && UtilValidate.isNotEmpty(jsonObject.get("reserveType"))) {
            reserveType = jsonObject.getString("reserveType");
        }
        String statusId = request.getParameter("statusId");
        if (UtilValidate.isEmpty(statusId) && UtilValidate.isNotEmpty(jsonObject.get("statusId"))) {
            statusId = jsonObject.getString("statusId");
        }
        /*String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺编号不能为空");
            response.setStatus(403);
            return "error";
        }*/
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("RH", "ReserveHeader");
        dynamicView.addAlias("RH", "reserveId");
        dynamicView.addAlias("RH", "reserveType");
        dynamicView.addAlias("RH", "statusId");
        dynamicView.addAlias("RH", "partyId");
//        dynamicView.addAlias("RH", "productStoreId");

        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
//        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
        //按预约单类型
        if (UtilValidate.isNotEmpty(reserveType)) {
            conditions.add(EntityCondition.makeCondition("reserveType", reserveType));
        }
        //按状态查询
        if (UtilValidate.isNotEmpty(statusId)) {
            conditions.add(EntityCondition.makeCondition("statusId", statusId));
        }

        Boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
        int reserveListSize = pli.getResultsSizeAfterPartialList();
        pli.close();
        TransactionUtil.commit(beganTransaction);
        request.setAttribute("num", reserveListSize);

        return "success";

    }

    /**
     * 预约维修详情获取接口
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getReserveRepair(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String reserveId = request.getParameter("reserveId");
        if (UtilValidate.isEmpty(reserveId) && UtilValidate.isNotEmpty(jsonObject.get("reserveId"))) {
            reserveId = jsonObject.getString("reserveId");
        }
        if (UtilValidate.isEmpty(reserveId)) {
            request.setAttribute("error", "预约单号不能为空");
            response.setStatus(403);
            return "error";
        }
        GenericValue reserveHeader = delegator.findByPrimaryKey("ReserveHeader", UtilMisc.toMap("reserveId", reserveId));
        List<GenericValue> reserveItems = delegator.findByAnd("ReserveItem", UtilMisc.toMap("reserveId", reserveId));
        List<GenericValue> reserveRepairs = delegator.findByAnd("ReserveRepair", UtilMisc.toMap("reserveId", reserveId));
        List<GenericValue> reserveRepairTags = delegator.findByAnd("ReserveRepairTag", UtilMisc.toMap("reserveId", reserveId));
        List<GenericValue> reserveContents = delegator.findByAnd("ReserveContent", UtilMisc.toMap("reserveId", reserveId));
        if (UtilValidate.isEmpty(reserveHeader)) {
            request.setAttribute("error", "预约单不存在");
            response.setStatus(403);
            return "error";
        }
        //状态
        request.setAttribute("statusId", reserveHeader.get("statusId"));
        if (UtilValidate.areEqual("DRIVER", reserveHeader.get("reserveType"))) {
            GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", reserveHeader.get("productStoreId")));
            if (UtilValidate.isNotEmpty(productStore)) {
                //店铺名称
                request.setAttribute("storeName", productStore.get("storeName"));
                //店铺地址
                GenericValue province = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("stateProvinceGeoId")));
                GenericValue city = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("cityGeoId")));
                GenericValue county = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", productStore.get("countyGeoId")));
                request.setAttribute("storeAddress", province.getString("geoName") + city.getString("geoName") + county.getString("geoName") + productStore.getString("address"));
            }
        } else {
            if (UtilValidate.isNotEmpty(reserveHeader.get("commStoreId"))) {
                GenericValue communityStore = delegator.findByPrimaryKey("CommunityStore", UtilMisc.toMap("commStoreId", reserveHeader.get("commStoreId")));
                if (UtilValidate.isNotEmpty(communityStore)) {
                    //门店名称
                    request.setAttribute("storeName", communityStore.get("storeName"));
                    //门店地址
                    GenericValue province = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", communityStore.get("province")));
                    GenericValue city = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", communityStore.get("city")));
                    GenericValue county = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", communityStore.get("county")));
                    request.setAttribute("storeAddress", province.getString("geoName") + city.getString("geoName") + county.getString("geoName") + communityStore.getString("address"));
                }
            }
        }
        //下单时间
        request.setAttribute("createTime", UtilDateTime.timeStampToString(reserveHeader.getTimestamp("createTime"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
        //预约时间
        request.setAttribute("reserveTime", UtilDateTime.timeStampToString(reserveHeader.getTimestamp("reserveTime"), "yyyy-MM-dd", timeZone, locale) + " " + UtilDateTime.timeStampToString(reserveHeader.getTimestamp("reserveTime"), "HH:mm", timeZone, locale) + "-" + UtilDateTime.timeStampToString(reserveHeader.getTimestamp("reserveEndTime"), "HH:mm", timeZone, locale));
        //品牌
        if (UtilValidate.isNotEmpty(reserveHeader.get("productCarLibBrandId"))) {
            GenericValue productBrand = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", reserveHeader.get("productCarLibBrandId")));
            if (UtilValidate.isNotEmpty(productBrand)) {
                request.setAttribute("brandName", productBrand.get("brandName"));
            }
        }
        //车系
        if (UtilValidate.isNotEmpty(reserveHeader.get("productCarSeriesId"))) {
            GenericValue productCarSeries = delegator.findByPrimaryKey("ProductCarSeries", UtilMisc.toMap("productCarSeriesId", reserveHeader.get("productCarSeriesId")));
            if (UtilValidate.isNotEmpty(productCarSeries)) {
                request.setAttribute("carSeriesName", productCarSeries.get("carSeriesName"));
            }
        }
        //车型
        if (UtilValidate.isNotEmpty(reserveHeader.get("productCarModelId"))) {
            GenericValue productCarModel = delegator.findByPrimaryKey("ProductCarModel", UtilMisc.toMap("productCarModelId", reserveHeader.get("productCarModelId")));
            if (UtilValidate.isNotEmpty(productCarModel)) {
                request.setAttribute("carModelName", productCarModel.get("carModelName"));
            }
        }
        //预约人
        request.setAttribute("reservePerson", reserveHeader.get("reservePerson"));
        //预约电话
        request.setAttribute("reserveTel", reserveHeader.get("reserveTel"));
        //维修描述
        request.setAttribute("repairDescription", reserveHeader.get("repairDescription"));
        //维修项目
        if (UtilValidate.isNotEmpty(reserveRepairs)) {
            String repairContents = "";
            for (GenericValue reserveRepair : reserveRepairs) {
                GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", reserveRepair.get("repairId")));
                if (UtilValidate.isNotEmpty(enumeration)) {
                    if (UtilValidate.isEmpty(repairContents)) {
                        repairContents = enumeration.getString("description");
                    } else {
                        repairContents += "、" + enumeration.getString("description");
                    }
                }
            }
            request.setAttribute("repairContents", repairContents);
        }
        //维修标签
        if (UtilValidate.isNotEmpty(reserveRepairTags)) {
            List<String> repairTags = FastList.newInstance();
            for (GenericValue reserveRepairTag : reserveRepairTags) {
                GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", reserveRepairTag.get("repairId")));
                if (UtilValidate.isNotEmpty(enumeration)) {
                    repairTags.add(enumeration.getString("description"));
                }
            }
            request.setAttribute("repairTags", repairTags);
        }
        //维修商品
        if (UtilValidate.isNotEmpty(reserveItems)) {
            List<Map> products = FastList.newInstance();
            for (GenericValue reserveItem : reserveItems) {
                Map map = FastMap.newInstance();
                map.put("productName", reserveItem.get("itemDescription"));
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", reserveItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    map.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                }
                map.put("unitPrice", UtilValidate.isNotEmpty(reserveItem.get("unitPrice")) ? reserveItem.getBigDecimal("unitPrice").setScale(2, BigDecimal.ROUND_HALF_UP) : "");
                map.put("quantity", reserveItem.get("quantity"));
                products.add(map);
            }
            request.setAttribute("products", products);
        }
        //维修凭证
        if (UtilValidate.isNotEmpty(reserveContents)) {
            List<String> imgList = FastList.newInstance();
            for (GenericValue reserveContent : reserveContents) {
                imgList.add(request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + reserveContent.getString("contentId"));
            }
            request.setAttribute("imgList", imgList);
        }
        //订单信息
        for (GenericValue reserveItem : reserveItems) {
            if (UtilValidate.isNotEmpty(reserveItem.get("orderId"))) {
                request.setAttribute("orderId", reserveItem.get("orderId"));
                GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", reserveItem.get("orderId")));
                request.setAttribute("orderStatus", orderHeader.get("statusId"));
                request.setAttribute("grandTotal", orderHeader.get("grandTotal"));
                request.setAttribute("orderDate", UtilDateTime.timeStampToString(orderHeader.getTimestamp("orderDate"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                request.setAttribute("actualPayMoney", orderHeader.get("actualPayMoney"));
            }
        }

        return "success";

    }

    /**
     * 取消预约单
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String cancelReserveOrder(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String reserveId = request.getParameter("reserveId");
        if (UtilValidate.isEmpty(reserveId) && UtilValidate.isNotEmpty(jsonObject.get("reserveId"))) {
            reserveId = jsonObject.getString("reserveId");
        }
        if (UtilValidate.isEmpty(reserveId)) {
            request.setAttribute("error", "预约单号不能为空");
            response.setStatus(403);
            return "error";
        }
        List<GenericValue> tobeStore = FastList.newInstance();
        GenericValue reserveHeader = delegator.findByPrimaryKey("ReserveHeader", UtilMisc.toMap("reserveId", reserveId));
        if (UtilValidate.isNotEmpty(reserveHeader)) {
            reserveHeader.set("statusId", "RESERVE_CANCELED");
            tobeStore.add(reserveHeader);
            GenericValue reserveStatus = delegator.makeValue("ReserveStatus");
            reserveStatus.set("reserveStatusId", delegator.getNextSeqId("ReserveStatus"));
            reserveStatus.set("statusId", "RESERVE_CANCELED");
            reserveStatus.set("reserveId", reserveId);
            reserveStatus.set("statusDatetime", UtilDateTime.nowTimestamp());
            reserveStatus.set("statusUserLogin", userLoginId);
            tobeStore.add(reserveStatus);
            List<GenericValue> reserveItems = delegator.findByAnd("ReserveItem", UtilMisc.toMap("reserveId", reserveId));
            if (UtilValidate.isNotEmpty(reserveItems)) {
                for (GenericValue reserveItem : reserveItems) {
                    reserveItem.set("statusId", "RESERVE_CANCELED");
                    tobeStore.add(reserveItem);
                    GenericValue reserveStatus1 = delegator.makeValue("ReserveStatus");
                    reserveStatus1.set("reserveStatusId", delegator.getNextSeqId("ReserveStatus"));
                    reserveStatus1.set("statusId", "RESERVE_CANCELED");
                    reserveStatus1.set("reserveId", reserveId);
                    reserveStatus1.set("reserveItemSeqId", reserveItem.get("reserveItemSeqId"));
                    reserveStatus1.set("statusDatetime", UtilDateTime.nowTimestamp());
                    reserveStatus1.set("statusUserLogin", userLoginId);
                    tobeStore.add(reserveStatus1);
                }
            }
        }
        if (UtilValidate.isNotEmpty(tobeStore)) {
            delegator.storeAll(tobeStore);
        }
        return "success";
    }

    /**
     * 获取退款原因
     *
     * @param request
     * @param response
     * @return
     */
    public static String getReturnType(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> enum_list = null;
        try {
            enum_list = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "FIELD_RETURN", "enumCode", "N"), UtilMisc.toList("sequenceId"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        List<Map> resultList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(enum_list)) {
            for (GenericValue enumeration : enum_list) {
                Map map = FastMap.newInstance();
                map.put("enumId", enumeration.get("enumId"));
                map.put("description", enumeration.get("description"));
                resultList.add(map);
            }
        }
        request.setAttribute("resultList", resultList);
        return "success";
    }

    /**
     * 退款单列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getReturnList(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前已查询数量不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 10;
        }
        int lowIndex = viewIndex + 1;
        int highIndex = viewIndex + viewSize;
        DynamicViewEntity dev = new DynamicViewEntity();
        dev.addMemberEntity("RH", "ReturnHeader");
        dev.addAlias("RH", "returnId");
        dev.addAlias("RH", "statusId");
        dev.addAlias("RH", "fromPartyId");
        dev.addAlias("RH", "applyTotal");
        dev.addAlias("RH", "entryDate");
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("fromPartyId", userLogin.get("partyId")));
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(dev, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, UtilMisc.toList("-entryDate"), findOpts);
            List<GenericValue> returnHeaders = eli.getPartialList(lowIndex, viewSize);
            int resultSize = eli.getResultsSizeAfterPartialList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<Map> returnList = FastList.newInstance();
            for (GenericValue returnHeader : returnHeaders) {
                Map map = FastMap.newInstance();
                map.put("returnId", returnHeader.get("returnId"));
                map.put("statusId", returnHeader.get("statusId"));
                map.put("applyTotal", returnHeader.get("applyTotal"));
                List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("returnId", returnHeader.get("returnId")));
                List<Map> products = FastList.newInstance();
                for (GenericValue returnItem : returnItems) {
                    Map productMap = FastMap.newInstance();
                    productMap.put("productName", returnItem.get("description"));
                    productMap.put("returnQuantity", returnItem.get("returnQuantity"));
                    List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", returnItem.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                    String imageUrl = "";
                    if (UtilValidate.isNotEmpty(productContents)) {
                        imageUrl = request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).get("contentId");
                    }
                    productMap.put("imageUrl", imageUrl);
                    products.add(productMap);
                }
                map.put("products", products);
                returnList.add(map);
            }

            request.setAttribute("returnList", returnList);
            request.setAttribute("max", resultSize);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取取消订单原因
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCancelType(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> enum_list = null;
        try {
            enum_list = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "FIELD_CANCEL", "enumCode", "N"), UtilMisc.toList("sequenceId"));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        List<Map> resultList = FastList.newInstance();
        if (UtilValidate.isNotEmpty(enum_list)) {
            for (GenericValue enumeration : enum_list) {
                Map map = FastMap.newInstance();
                map.put("enumId", enumeration.get("enumId"));
                map.put("description", enumeration.get("description"));
                map.put("remark", enumeration.get("remark"));
                resultList.add(map);
            }
        }
        request.setAttribute("resultList", resultList);
        return "success";
    }

    /**
     * 会员积分兑换记录创建，更新会员积分值
     *
     * @param request
     * @param response
     * @return
     * @author wcy
     * @date 2017-5-11
     */
    public static String updatePartyIntegralAndHistory(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> tobeStore = new ArrayList<GenericValue>();

        //获取预处理数据
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        String partyId = userLogin.getString("partyId"); //会员编号
        Map<String, Object> resultData = (Map<String, Object>) request.getAttribute("resultData");
        String orderId = (String) resultData.get("orderId"); //订单编号
        BigDecimal totalIntegral = (BigDecimal) request.getAttribute("totalIntegral"); //总积分 = 积分 * 数量

        //更新会员积分值
        BigDecimal residualScore = BigDecimal.ZERO;
        GenericValue partyScore = null;
        try {
            partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", partyId));
            if (partyScore != null) {
                BigDecimal scoreValue = new BigDecimal(partyScore.getString("scoreValue"));
                residualScore = scoreValue.subtract(totalIntegral);
                partyScore.set("scoreValue", residualScore.longValue());
                tobeStore.add(partyScore);
            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            return "error";
        }

        //创建积分兑换记录信息
        GenericValue partyScoreHistory = delegator.makeValue("PartyScoreHistory");
        String newPartyScoreHistoryId = delegator.getNextSeqId("PartyScoreHistory");
        partyScoreHistory.set("partyScoreHistoryId", newPartyScoreHistoryId);
        partyScoreHistory.set("partyId", partyId);
        partyScoreHistory.set("residualScore", residualScore.longValue());
        partyScoreHistory.set("scoreValue", totalIntegral.longValue());
        partyScoreHistory.set("orderId", orderId);
        partyScoreHistory.set("getWay", "OUT");
        partyScoreHistory.set("description", "积分订单消费");
        partyScoreHistory.set("createDate", UtilDateTime.nowTimestamp());
        tobeStore.add(partyScoreHistory);

        try {
            try {
                delegator.storeAll(tobeStore);
            } catch (GenericEntityException e) {
                TransactionUtil.rollback();
                Debug.log(e.getMessage());
                return "error";
            }
        } catch (GenericTransactionException e) {
            Debug.log(e.getMessage());
            return "error";
        }

        return "success";
    }

    /**
     * 回写预约单订单信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String updateReserveItem(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        //获取预处理数据
        Map<String, Object> resultData = (Map<String, Object>) request.getAttribute("resultData");
        String orderId = (String) resultData.get("orderId"); //订单编号
        String reserveId = (String) request.getAttribute("reserveId"); //预约单编号
        String insureBusinessName = (String) request.getAttribute("insureBusinessName"); //投保意向人
        String orderType = (String) request.getAttribute("salesOrderType"); //订单类型

        Map<String, String> paramMap = UtilMisc.toMap("reserveId", reserveId);
        //保险订单，需查询投保意向人
        if ("INSURANCE_ORDER".equals(orderType)) {
            paramMap.put("partyId", insureBusinessName);
        }
        try {
            List<GenericValue> reserveItemList = delegator.findByAnd("ReserveItem", paramMap);
            for (GenericValue reserveItem : reserveItemList) {
                reserveItem.set("orderId", orderId);
            }

            delegator.storeAll(reserveItemList);
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }

        return "success";
    }

    /**
     * 清除创建订单流程中的参数值
     *
     * @param request
     * @param response
     */
    public static void removeOrderAttribute(HttpServletRequest request, HttpServletResponse response) {
        request.removeAttribute("checkOutPaymentId"); //支付方式
        request.removeAttribute("productCarSeriesId"); //车系ID
        request.removeAttribute("productCarModelId"); //车型ID
        request.removeAttribute("productCarLibBrandId"); //品牌ID
        request.removeAttribute("buyerTelphone"); //手机号
        request.removeAttribute("reserveId"); //预约单号
        request.removeAttribute("appointmentName"); //车主姓名
        request.removeAttribute("licencePlate"); //车牌号
        request.removeAttribute("idNumber"); //身份证号
        request.removeAttribute("insureCity"); //投保城市
        request.removeAttribute("isTransferNames"); //是否过户
        request.removeAttribute("insureBusinessName");//意向商家
        request.removeAttribute("productStoreId"); //店铺ID
        request.removeAttribute("salesOrderType"); //订单类型
        request.removeAttribute("quantity"); //数量
        request.removeAttribute("totalIntegral"); //总积分
        request.removeAttribute("addProductId"); //购买商品ID
        request.removeAttribute("price"); //商品价格
        request.removeAttribute("checkMaintainPrice"); //差异费用
        request.removeAttribute("resultData"); //订单创建流程，响应的参数
        request.removeAttribute("itemId");
        request.removeAttribute("_LOGIN_PASSED_");
        request.removeAttribute("USERNAME"); //登录账号
        request.removeAttribute("maintainServiceChargeInfo"); //服务费
        request.removeAttribute("currMaintainTypeId"); //保养类型ID
        request.removeAttribute("originalMoney"); //原始价格
        request.removeAttribute("distributeMoney"); //配送运费
        request.removeAttribute("discountMoney"); //优惠金额
        request.removeAttribute("shouldPayMoney"); //应付金额
        request.removeAttribute("actualPayMoney"); //实付金额
        request.removeAttribute("notPayMoney"); //未付金额
        request.removeAttribute("getIntegral"); //获得积分
        request.removeAttribute("useIntegral"); //使用积分
        request.removeAttribute("shouldFrontMoney"); //应付定金
        request.removeAttribute("commStoreId"); //店铺ID
        request.removeAttribute("maintenance"); //维修时间
        request.removeAttribute("repairContentIds");//维修凭证
        request.removeAttribute("repairType"); //维修清单
        request.removeAttribute("carCondition"); //维修描述
        request.removeAttribute("repairTagIds"); //维修标签
        request.removeAttribute("shoppingListItemSeqIds"); //购物车编号
        request.removeAttribute("success"); //其他方法提示消息
        request.removeAttribute("maintainTime"); //保养时间：日期段
        request.removeAttribute("timeQuantum"); //保养时间：时间段
        request.removeAttribute("buyerName"); //积分订单：购买人
        request.removeAttribute("insuranceClosingTime"); //保险订单：投保截止日期
    }

    /**
     * 楼层列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String getFloorList(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String webSiteId = request.getParameter("webSiteId");
        if (UtilValidate.isEmpty(webSiteId) && UtilValidate.isNotEmpty(jsonObject.get("webSiteId"))) {
            webSiteId = jsonObject.getString("webSiteId");
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            request.setAttribute("error", "站点编号不能为空");
            response.setStatus(403);
            return "error";
        }
        try {
            boolean beganTransaction;
            //站点
            DynamicViewEntity webSiteViewEntity = new DynamicViewEntity();
            webSiteViewEntity.addMemberEntity("WS", "WebSite");
            webSiteViewEntity.addAlias("WS", "webSiteId", "webSiteId", null, true, true, null);
            webSiteViewEntity.addAlias("WS", "isEnabled");
            webSiteViewEntity.addMemberEntity("WPS", "WebsiteProductStorelink");
            webSiteViewEntity.addAlias("WPS", "productStoreId");
            webSiteViewEntity.addViewLink("WS", "WPS", true, ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));
            List<EntityCondition> webSiteConditions = FastList.newInstance();
            webSiteConditions.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            webSiteConditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
            webSiteConditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            beganTransaction = TransactionUtil.begin();
            EntityListIterator eli = delegator.findListIteratorByCondition(webSiteViewEntity, EntityCondition.makeCondition(webSiteConditions, EntityOperator.AND), null, null, null, null);
            List<GenericValue> webSites = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            List<String> webSiteIds = FastList.newInstance();
            if (UtilValidate.isNotEmpty(webSites)) {
                for (GenericValue website : webSites) {
                    webSiteIds.add(website.getString("webSiteId"));
                }
            }
            DynamicViewEntity floorViewEntity = new DynamicViewEntity();
            floorViewEntity.addMemberEntity("F", "Floor");
            floorViewEntity.addAlias("F", "floorId", "floorId", null, true, true, null);
            floorViewEntity.addAlias("F", "productCategoryId");
            floorViewEntity.addAlias("F", "floorName");
            floorViewEntity.addAlias("F", "isEnabled");
            floorViewEntity.addAlias("F", "sequenceNum");
            floorViewEntity.addMemberEntity("FWS", "FloorWebSite");
            floorViewEntity.addAlias("FWS", "webSiteId");
            floorViewEntity.addViewLink("F", "FWS", true, ModelKeyMap.makeKeyMapList("floorId", "floorId"));
            List<EntityCondition> floorConditions = FastList.newInstance();
            floorConditions.add(EntityCondition.makeCondition("isEnabled", "Y"));
            floorConditions.add(EntityCondition.makeCondition("webSiteId", EntityOperator.IN, webSiteIds));
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(floorViewEntity, EntityCondition.makeCondition(floorConditions, EntityOperator.AND), null, null, UtilMisc.toList("sequenceNum"), null);
            List<GenericValue> floors = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            if (UtilValidate.isNotEmpty(floors)) {
                List<Map> floorList = FastList.newInstance();
                for (GenericValue floor : floors) {
                    Map floorMap = FastMap.newInstance();
                    //楼层编号
                    floorMap.put("floorId", floor.get("floorId"));
                    //楼层名称
                    floorMap.put("floorName", floor.get("floorName"));
                    //楼层分类
                    floorMap.put("productCategoryId", floor.get("productCategoryId"));
                    //楼层分类的下级分类
                    List<GenericValue> productCategorys = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId", floor.get("productCategoryId")), UtilMisc.toList("sequenceNum"));
                    List<Map> categoryList = FastList.newInstance();
                    for (GenericValue productCategory : productCategorys) {
                        Map categoryMap = FastMap.newInstance();
                        categoryMap.put("productCategoryId", productCategory.get("productCategoryId"));
                        categoryMap.put("categoryName", productCategory.get("categoryName"));
                        categoryList.add(categoryMap);
                    }
                    floorMap.put("categoryList", categoryList);
                    //楼层广告
                    List<GenericValue> floorBanners = delegator.findByAnd("FloorBanner", UtilMisc.toMap("floorId", floor.get("floorId"), "isEnabled", "Y"), UtilMisc.toList("sequenceNum"));
                    List<Map> bannerList = FastList.newInstance();
                    for (GenericValue floorBanner : floorBanners) {
                        Map bannerMap = FastMap.newInstance();
                        //广告名称
                        bannerMap.put("bannerName", floorBanner.get("bannerName"));
                        //广告描述
                        bannerMap.put("description", floorBanner.get("description"));
                        //广告图片
                        bannerMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + floorBanner.getString("imgUrl"));
                        //广告链接
                        if (UtilValidate.areEqual("FLT_ZDYLJ", floorBanner.get("firstLinkType"))) {
                            bannerMap.put("linkType", floorBanner.get("firstLinkType"));
                            bannerMap.put("linkUrl", floorBanner.get("linkUrl"));
                        } else if (UtilValidate.areEqual("FLT_SPLJ", floorBanner.get("firstLinkType"))) {
                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", floorBanner.get("linkId")));
                            if (UtilValidate.areEqual("CAR_GOOD", product.get("productTypeId"))) {
                                bannerMap.put("linkType", product.get("carTypeId"));
                            } else {
                                bannerMap.put("linkType", product.get("productTypeId"));
                            }
                            bannerMap.put("linkId", floorBanner.get("linkId"));
                        } else {
                            bannerMap.put("linkType", floorBanner.get("firstLinkType"));
                            bannerMap.put("linkId", floorBanner.get("linkId"));
                        }
                        bannerList.add(bannerMap);
                    }
                    floorMap.put("bannerList", bannerList);
                    //买车的相关数据
                    if (UtilValidate.areEqual("365_BUYCAR", floor.get("productCategoryId"))) {
                        //楼层品牌
                        List<GenericValue> floorBrands = delegator.findByAnd("FloorBrand", UtilMisc.toMap("floorId", floor.get("floorId"), "isEnabled", "Y"), UtilMisc.toList("sequenceNum"));
                        List<Map> brandList = FastList.newInstance();
                        for (GenericValue floorBrand : floorBrands) {
                            Map brandMap = FastMap.newInstance();
                            GenericValue productBrand = delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", floorBrand.get("productBrandId")));
                            if (UtilValidate.isNotEmpty(productBrand) && UtilValidate.areEqual("Y", productBrand.get("isUsed"))) {
                                brandMap.put("productBrandId", floorBrand.get("productBrandId"));
                                brandMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productBrand.getString("contentId"));
                                brandList.add(brandMap);
                            }
                        }
                        floorMap.put("brandList", brandList);
                        //车型
                        List<GenericValue> carLevels = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_LEVEL", "enumCode", "N"), UtilMisc.toList("sequenceId"));
                        List<Map> levelList = FastList.newInstance();
                        for (GenericValue carLevel : carLevels) {
                            Map enumMap = FastMap.newInstance();
                            enumMap.put("enumId", carLevel.get("enumId"));
                            enumMap.put("description", carLevel.get("description"));
//                            enumMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + carLevel.getString("contentId"));
                            levelList.add(enumMap);
                        }
                        floorMap.put("levelList", levelList);
                    }
                    floorList.add(floorMap);
                }
                request.setAttribute("floorList", floorList);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * PC端获取车型库条件
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarConditionPC(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            //品牌
            DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
            dynamicViewEntity.addMemberEntity("PB", "ProductBrand");
            dynamicViewEntity.addAlias("PB", "productBrandId", "productBrandId", null, false, true, null);
            dynamicViewEntity.addAlias("PB", "brandName");
            dynamicViewEntity.addAlias("PB", "headString");
            dynamicViewEntity.addAlias("PB", "isUsed");
            dynamicViewEntity.addAlias("PB", "isDel");
            dynamicViewEntity.addMemberEntity("PBC", "ProductBrandCategory");
            dynamicViewEntity.addAlias("PBC", "productCategoryId");
            dynamicViewEntity.addViewLink("PB", "PBC", true, ModelKeyMap.makeKeyMapList("productBrandId", "productBrandId"));
            List<EntityCondition> brandConditions = FastList.newInstance();
            brandConditions.add(EntityCondition.makeCondition("isUsed", "Y"));
            brandConditions.add(EntityCondition.makeCondition("isDel", "N"));
            brandConditions.add(EntityCondition.makeCondition("productCategoryId", "NEW_CAR"));
            List<String> fieldsToSelect = FastList.newInstance();
            fieldsToSelect.add("productBrandId");
            fieldsToSelect.add("brandName");
            try {
                Boolean beganTransaction = TransactionUtil.begin();
                EntityListIterator eli = delegator.findListIteratorByCondition(dynamicViewEntity, EntityCondition.makeCondition(brandConditions, EntityOperator.AND), null, fieldsToSelect, UtilMisc.toList("headString"), null);
                List<GenericValue> productBrands = eli.getCompleteList();
                eli.close();
                TransactionUtil.commit(beganTransaction);
                request.setAttribute("brandArray", productBrands);
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            //级别
            List<GenericValue> carLevels = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_LEVEL", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray levelArray = new JSONArray();
            for (GenericValue carLevel : carLevels) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carLevel.get("enumId"));
                jsonObject.put("description", carLevel.get("description"));
                levelArray.add(jsonObject);
            }
            request.setAttribute("levelArray", levelArray);
            //国别
            List<GenericValue> carCountrys = delegator.findByAnd("Enumeration", UtilMisc.toMap("enumTypeId", "CAR_COUNTRY", "enumCode", "N"), UtilMisc.toList("sequenceId"));
            JSONArray countryArray = new JSONArray();
            for (GenericValue carCountry : carCountrys) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enumId", carCountry.get("enumId"));
                jsonObject.put("description", carCountry.get("description"));
                countryArray.add(jsonObject);
            }
            request.setAttribute("countryArray", countryArray);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * PC端根据车型库条件查询新车、平行进口车
     *
     * @param request
     * @param response
     * @return
     */
    public static String getCarByConditionPC(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        //最低价格
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        //品牌编号
        String productBrandIds = request.getParameter("productBrandIds");
        if (UtilValidate.isEmpty(productBrandIds) && UtilValidate.isNotEmpty(jsonObject.get("productBrandIds"))) {
            productBrandIds = jsonObject.getString("productBrandIds");
        }
        //级别编号
        String carLevelIds = request.getParameter("carLevelIds");
        if (UtilValidate.isEmpty(carLevelIds) && UtilValidate.isNotEmpty(jsonObject.get("carLevelIds"))) {
            carLevelIds = jsonObject.getString("carLevelIds");
        }
        //国别编号
        String carCountryIds = request.getParameter("carCountryIds");
        if (UtilValidate.isEmpty(carCountryIds) && UtilValidate.isNotEmpty(jsonObject.get("carCountryIds"))) {
            carCountryIds = jsonObject.getString("carCountryIds");
        }
        //关键字
        String keyWord = request.getParameter("keyWord");
        if (UtilValidate.isEmpty(keyWord) && UtilValidate.isNotEmpty(jsonObject.get("keyWord"))) {
            keyWord = jsonObject.getString("keyWord");
        }
        String orderField = request.getParameter("orderField");
        if (UtilValidate.isEmpty(orderField) && UtilValidate.isNotEmpty(jsonObject.get("orderField"))) {
            orderField = jsonObject.getString("orderField");
        }

        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        String carTypeId = request.getParameter("carTypeId");
        if (UtilValidate.isEmpty(carTypeId) && UtilValidate.isNotEmpty(jsonObject.get("carTypeId"))) {
            carTypeId = jsonObject.getString("carTypeId");
        }
        if (UtilValidate.isEmpty(carTypeId)) {
            request.setAttribute("error", "车辆类别不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前页码不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 24;
        }
        int lowIndex = (viewIndex - 1) * viewSize + 1;
        int highIndex = viewIndex * viewSize;
        //车型视图
        DynamicViewEntity carModelViewViewEntity = new DynamicViewEntity();
        carModelViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        carModelViewViewEntity.addAlias("PCM", "productCarModelId", "productCarModelId", null, false, true, null);
        carModelViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        carModelViewViewEntity.addAlias("PCM", "productionModel");//制造商
        carModelViewViewEntity.addAlias("PCM", "carStructureId");//结构
        carModelViewViewEntity.addAlias("PCM", "carSeatId");//座位数
        carModelViewViewEntity.addAlias("PCM", "carDisplacementId");//排量
        carModelViewViewEntity.addAlias("PCM", "carFuelId");//能源
        carModelViewViewEntity.addAlias("PCM", "carModelName");//车型名称
        carModelViewViewEntity.addAlias("PCM", "carDisplacementName");//排量名称
        carModelViewViewEntity.addAlias("PCM", "carFuelName");//燃料名称
        carModelViewViewEntity.addAlias("PCM", "carStructureName");//结构名称
        carModelViewViewEntity.addAlias("PCM", "carDriverName");//驱动名称
        carModelViewViewEntity.addAlias("PCM", "carSeatName");//座位名称
        carModelViewViewEntity.addAlias("PCM", "productionModelName");//制造商名称
        carModelViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        carModelViewViewEntity.addAlias("PCS", "productCarSeriesId");
        carModelViewViewEntity.addAlias("PCS", "isDel1", "isDel", null, false, false, null);
        carModelViewViewEntity.addAlias("PCS", "carCountryId");//国别
        carModelViewViewEntity.addAlias("PCS", "carLevelId");//级别
        carModelViewViewEntity.addAlias("PCS", "productBrandId");//品牌
        carModelViewViewEntity.addAlias("PCS", "carSeriesName");//车系名称
        carModelViewViewEntity.addAlias("PCS", "productBrandName");//品牌名称
        carModelViewViewEntity.addAlias("PCS", "carCountryName");//国别名称
        carModelViewViewEntity.addAlias("PCS", "carLevelName");//级别名称
        carModelViewViewEntity.addViewLink("PCM", "PCS", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        carModelViewViewEntity.addMemberEntity("P", "Product");
        carModelViewViewEntity.addAlias("P", "productId");
        carModelViewViewEntity.addAlias("P", "productName");
        carModelViewViewEntity.addAlias("P", "productSubheadName");
        carModelViewViewEntity.addAlias("P", "frontMoney");
        carModelViewViewEntity.addAlias("P", "ratio");
        carModelViewViewEntity.addAlias("P", "isOnline");
        carModelViewViewEntity.addAlias("P", "isVerify");
        carModelViewViewEntity.addAlias("P", "isDel");
        carModelViewViewEntity.addAlias("P", "carTypeId");
        carModelViewViewEntity.addAlias("P", "productTypeId");
        carModelViewViewEntity.addAlias("P", "mainProductId");
        carModelViewViewEntity.addAlias("P", "introductionDate");
        carModelViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        carModelViewViewEntity.addViewLink("PCM", "P", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        carModelViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        carModelViewViewEntity.addAlias("PSPA", "productStoreId");
        carModelViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        carModelViewViewEntity.addMemberEntity("PP", "ProductPrice");
        carModelViewViewEntity.addAlias("PP", "productPriceTypeId");
        carModelViewViewEntity.addAlias("PP", "price");
        carModelViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        carModelViewViewEntity.addMemberEntity("PCI", "ProductCalculatedInfo");
        carModelViewViewEntity.addAlias("PCI", "saleNum", "totalQuantityOrdered", null, false, false, null);
        carModelViewViewEntity.addViewLink("P", "PCI", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));//默认价格
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));//是否上架
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));//是否审核
        conditions.add(EntityCondition.makeCondition("isDel", "N"));//是否删除
        conditions.add(EntityCondition.makeCondition("carTypeId", carTypeId));//车辆类别
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));//商品类别
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售开始日期
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售结束日期
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));//店铺编号
        conditions.add(EntityCondition.makeCondition("isDel1", "N"));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        if (UtilValidate.isNotEmpty(productBrandIds)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", EntityOperator.IN, UtilMisc.toListArray(productBrandIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carLevelIds)) {
            conditions.add(EntityCondition.makeCondition("carLevelId", EntityOperator.IN, UtilMisc.toListArray(carLevelIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(carCountryIds)) {
            conditions.add(EntityCondition.makeCondition("carCountryId", EntityOperator.IN, UtilMisc.toListArray(carCountryIds.split(","))));
        }
        if (UtilValidate.isNotEmpty(keyWord)) {
            List<EntityCondition> keyWordConditions = FastList.newInstance();
            keyWordConditions.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productSubheadName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDisplacementName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carFuelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carStructureName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDriverName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeatName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productionModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeriesName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productBrandName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carCountryName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carLevelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            conditions.add(EntityCondition.makeCondition(keyWordConditions, EntityOperator.OR));
        }
        List<String> carModelToSelect = FastList.newInstance();
        carModelToSelect.add("productCarModelId");
        carModelToSelect.add("productId");
        carModelToSelect.add("productName");
        carModelToSelect.add("productSubheadName");
        carModelToSelect.add("frontMoney");
        carModelToSelect.add("ratio");
        carModelToSelect.add("price");
        carModelToSelect.add("saleNum");
        List<String> carModelToOrder = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderField)) {
            carModelToOrder.add(orderField);
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            //车量商品数据
            EntityListIterator eli = delegator.findListIteratorByCondition(carModelViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, carModelToSelect, carModelToOrder, findOpts);
            List<GenericValue> carModels = eli.getPartialList(lowIndex, highIndex);
            int max = eli.getResultsSizeAfterPartialList();
            request.setAttribute("max", max % viewSize == 0 ? max / viewSize : max / viewSize + 1);
            List<Map> carModelList = FastList.newInstance();
            for (GenericValue carModel : carModels) {
                Map modelMap = FastMap.newInstance();
                modelMap.put("productId", carModel.get("productId"));
                modelMap.put("productName", carModel.get("productName"));
                modelMap.put("productSubheadName", carModel.get("productSubheadName"));
                //售价
                modelMap.put("price", carModel.getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
                //市场价
                List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", carModel.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                if (UtilValidate.isNotEmpty(marketPriceList)) {
                    BigDecimal marketPrice;
                    marketPrice = marketPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    modelMap.put("marketPrice", marketPrice);
                }
                //定金
                BigDecimal frontMoney;
                BigDecimal ratio = UtilValidate.isNotEmpty(carModel.getBigDecimal("ratio")) ? carModel.getBigDecimal("ratio") : BigDecimal.ZERO;
                if (UtilValidate.isNotEmpty(carModel.get("frontMoney"))) {
                    frontMoney = carModel.getBigDecimal("frontMoney").setScale(2, BigDecimal.ROUND_HALF_UP);
                } else {
                    frontMoney = carModel.getBigDecimal("price").multiply(ratio).divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                modelMap.put("frontMoney", frontMoney);
                //图片
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", carModel.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    modelMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                }
                //销量
                modelMap.put("saleNum", carModel.get("saleNum"));
                carModelList.add(modelMap);
            }
            request.setAttribute("carModelList", carModelList);
            eli.close();
            TransactionUtil.commit(beganTransaction);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * PC端根据车型库条件查询二手车
     *
     * @param request
     * @param response
     * @return
     */
    public static String getUsedCarByConditionPC(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        //最低价格
        String priceLow = request.getParameter("priceLow");
        if (UtilValidate.isEmpty(priceLow) && UtilValidate.isNotEmpty(jsonObject.get("priceLow"))) {
            priceLow = jsonObject.getString("priceLow");
        }
        //最高价格
        String priceHigh = request.getParameter("priceHigh");
        if (UtilValidate.isEmpty(priceHigh) && UtilValidate.isNotEmpty(jsonObject.get("priceHigh"))) {
            priceHigh = jsonObject.getString("priceHigh");
        }
        //品牌编号
        String productBrandIds = request.getParameter("productBrandIds");
        if (UtilValidate.isEmpty(productBrandIds) && UtilValidate.isNotEmpty(jsonObject.get("productBrandIds"))) {
            productBrandIds = jsonObject.getString("productBrandIds");
        }
        //最低车龄
        String useTimeLow = request.getParameter("useTimeLow");
        if (UtilValidate.isEmpty(useTimeLow) && UtilValidate.isNotEmpty(jsonObject.get("useTimeLow"))) {
            useTimeLow = jsonObject.getString("useTimeLow");
        }
        //最高车龄
        String useTimeHigh = request.getParameter("useTimeHigh");
        if (UtilValidate.isEmpty(useTimeHigh) && UtilValidate.isNotEmpty(jsonObject.get("useTimeHigh"))) {
            useTimeHigh = jsonObject.getString("useTimeHigh");
        }
        //最低里程，单位万公里
        String carMileageLow = request.getParameter("carMileageLow");
        if (UtilValidate.isEmpty(carMileageLow) && UtilValidate.isNotEmpty(jsonObject.get("carMileageLow"))) {
            carMileageLow = jsonObject.getString("carMileageLow");
        }
        //最高里程
        String carMileageHigh = request.getParameter("carMileageHigh");
        if (UtilValidate.isEmpty(carMileageHigh) && UtilValidate.isNotEmpty(jsonObject.get("carMileageHigh"))) {
            carMileageHigh = jsonObject.getString("carMileageHigh");
        }
        //关键字
        String keyWord = request.getParameter("keyWord");
        if (UtilValidate.isEmpty(keyWord) && UtilValidate.isNotEmpty(jsonObject.get("keyWord"))) {
            keyWord = jsonObject.getString("keyWord");
        }
        String orderField = request.getParameter("orderField");
        if (UtilValidate.isEmpty(orderField) && UtilValidate.isNotEmpty(jsonObject.get("orderField"))) {
            orderField = jsonObject.getString("orderField");
        }

        String productStoreId = request.getParameter("productStoreId");
        if (UtilValidate.isEmpty(productStoreId) && UtilValidate.isNotEmpty(jsonObject.get("productStoreId"))) {
            productStoreId = jsonObject.getString("productStoreId");
        }
        if (UtilValidate.isEmpty(productStoreId)) {
            request.setAttribute("error", "店铺ID不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewIndex = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewIndex"))) {
            viewIndex = Integer.valueOf(request.getParameter("viewIndex"));
        } else if (UtilValidate.isEmpty(viewIndex) && UtilValidate.isNotEmpty(jsonObject.get("viewIndex"))) {
            viewIndex = jsonObject.getInt("viewIndex");
        }
        if (UtilValidate.isEmpty(viewIndex)) {
            request.setAttribute("error", "当前页码不能为空");
            response.setStatus(403);
            return "error";
        }
        Integer viewSize = null;
        if (UtilValidate.isNotEmpty(request.getParameter("viewSize"))) {
            viewSize = Integer.valueOf(request.getParameter("viewSize"));
        } else if (UtilValidate.isEmpty(viewSize) && UtilValidate.isNotEmpty(jsonObject.get("viewSize"))) {
            viewSize = jsonObject.getInt("viewSize");
        }
        if (UtilValidate.isEmpty(viewSize)) {
            viewSize = 24;
        }
        int lowIndex = (viewIndex - 1) * viewSize + 1;
        int highIndex = viewIndex * viewSize;
        //车型视图
        DynamicViewEntity carModelViewViewEntity = new DynamicViewEntity();
        carModelViewViewEntity.addMemberEntity("P", "Product");
        carModelViewViewEntity.addAlias("P", "productId", "productId", null, false, true, null);
        carModelViewViewEntity.addAlias("P", "productName");
        carModelViewViewEntity.addAlias("P", "productSubheadName");
        carModelViewViewEntity.addAlias("P", "frontMoney");
        carModelViewViewEntity.addAlias("P", "ratio");
        carModelViewViewEntity.addAlias("P", "isOnline");
        carModelViewViewEntity.addAlias("P", "isVerify");
        carModelViewViewEntity.addAlias("P", "isDel");
        carModelViewViewEntity.addAlias("P", "carTypeId");
        carModelViewViewEntity.addAlias("P", "productTypeId");
        carModelViewViewEntity.addAlias("P", "mainProductId");
        carModelViewViewEntity.addAlias("P", "introductionDate");
        carModelViewViewEntity.addAlias("P", "salesDiscontinuationDate");
        carModelViewViewEntity.addAlias("P", "useTime");
        carModelViewViewEntity.addAlias("P", "carMileage");
        carModelViewViewEntity.addMemberEntity("PCM", "ProductCarModel");
        carModelViewViewEntity.addAlias("PCM", "productCarModelId");
        carModelViewViewEntity.addAlias("PCM", "isDel2", "isDel", null, false, false, null);
        carModelViewViewEntity.addAlias("PCM", "productionModel");//制造商
        carModelViewViewEntity.addAlias("PCM", "carStructureId");//结构
        carModelViewViewEntity.addAlias("PCM", "carSeatId");//座位数
        carModelViewViewEntity.addAlias("PCM", "carDisplacementId");//排量
        carModelViewViewEntity.addAlias("PCM", "carFuelId");//能源
        carModelViewViewEntity.addAlias("PCM", "carModelName");//车型名称
        carModelViewViewEntity.addAlias("PCM", "carDisplacementName");//排量名称
        carModelViewViewEntity.addAlias("PCM", "carFuelName");//燃料名称
        carModelViewViewEntity.addAlias("PCM", "carStructureName");//结构名称
        carModelViewViewEntity.addAlias("PCM", "carDriverName");//驱动名称
        carModelViewViewEntity.addAlias("PCM", "carSeatName");//座位名称
        carModelViewViewEntity.addAlias("PCM", "productionModelName");//制造商名称
        carModelViewViewEntity.addViewLink("P", "PCM", true, ModelKeyMap.makeKeyMapList("productCarModelId", "productCarModelId"));
        carModelViewViewEntity.addMemberEntity("PCS", "ProductCarSeries");
        carModelViewViewEntity.addAlias("PCS", "productCarSeriesId");
        carModelViewViewEntity.addAlias("PCS", "isDel1", "isDel", null, false, false, null);
        carModelViewViewEntity.addAlias("PCS", "carCountryId");//国别
        carModelViewViewEntity.addAlias("PCS", "carLevelId");//级别
        carModelViewViewEntity.addAlias("PCS", "productBrandId");//品牌
        carModelViewViewEntity.addAlias("PCS", "carSeriesName");//车系名称
        carModelViewViewEntity.addAlias("PCS", "productBrandName");//品牌名称
        carModelViewViewEntity.addAlias("PCS", "carCountryName");//国别名称
        carModelViewViewEntity.addAlias("PCS", "carLevelName");//级别名称
        carModelViewViewEntity.addViewLink("P", "PCS", true, ModelKeyMap.makeKeyMapList("productCarSeriesId", "productCarSeriesId"));
        carModelViewViewEntity.addMemberEntity("PSPA", "ProductStoreProductAssoc");
        carModelViewViewEntity.addAlias("PSPA", "productStoreId");
        carModelViewViewEntity.addViewLink("P", "PSPA", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        carModelViewViewEntity.addMemberEntity("PP", "ProductPrice");
        carModelViewViewEntity.addAlias("PP", "productPriceTypeId");
        carModelViewViewEntity.addAlias("PP", "price");
        carModelViewViewEntity.addViewLink("P", "PP", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        carModelViewViewEntity.addMemberEntity("PCI", "ProductCalculatedInfo");
        carModelViewViewEntity.addAlias("PCI", "saleNum", "totalQuantityOrdered", null, false, false, null);
        carModelViewViewEntity.addViewLink("P", "PCI", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("productPriceTypeId", "DEFAULT_PRICE"));//默认价格
        conditions.add(EntityCondition.makeCondition("isOnline", "Y"));//是否上架
        conditions.add(EntityCondition.makeCondition("isVerify", "Y"));//是否审核
        conditions.add(EntityCondition.makeCondition("isDel", "N"));//是否删除
        conditions.add(EntityCondition.makeCondition("carTypeId", "USED_CAR"));//车辆类别
        conditions.add(EntityCondition.makeCondition("productTypeId", "CAR_GOOD"));//商品类别
        conditions.add(EntityCondition.makeCondition("mainProductId", null));
        conditions.add(EntityCondition.makeCondition("introductionDate", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售开始日期
        List<EntityCondition> list = FastList.newInstance();
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));//销售结束日期
        list.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null));
        conditions.add(EntityCondition.makeCondition(list, EntityOperator.OR));
        conditions.add(EntityCondition.makeCondition("productStoreId", productStoreId));//店铺编号
        conditions.add(EntityCondition.makeCondition("isDel1", "N"));
        conditions.add(EntityCondition.makeCondition("isDel2", "N"));
        if (UtilValidate.isNotEmpty(priceLow)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(priceLow)));
        }
        if (UtilValidate.isNotEmpty(priceHigh)) {
            conditions.add(EntityCondition.makeCondition("price", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(priceHigh)));
        }
        if (UtilValidate.isNotEmpty(productBrandIds)) {
            conditions.add(EntityCondition.makeCondition("productBrandId", EntityOperator.IN, UtilMisc.toListArray(productBrandIds.split(","))));
        }
        //最低车龄
        if (UtilValidate.isNotEmpty(useTimeLow)) {
            String timeLow = UtilDateTime.timeStampToString(UtilDateTime.adjustTimestamp(UtilDateTime.nowTimestamp(), Calendar.YEAR, -Integer.valueOf(useTimeLow)), "yyyy-MM", timeZone, locale);
            conditions.add(EntityCondition.makeCondition("useTime", EntityOperator.LESS_THAN_EQUAL_TO, timeLow));
        }
        //最高车龄
        if (UtilValidate.isNotEmpty(useTimeHigh)) {
            String timeHigh = UtilDateTime.timeStampToString(UtilDateTime.adjustTimestamp(UtilDateTime.nowTimestamp(), Calendar.YEAR, -Integer.valueOf(useTimeHigh)), "yyyy-MM", timeZone, locale);
            conditions.add(EntityCondition.makeCondition("useTime", EntityOperator.GREATER_THAN_EQUAL_TO, timeHigh));
        }
        //最低里程
        if (UtilValidate.isNotEmpty(carMileageLow)) {
            conditions.add(EntityCondition.makeCondition("carMileage", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(carMileageLow).multiply(new BigDecimal(10000))));
        }
        //最高里程
        if (UtilValidate.isNotEmpty(carMileageHigh)) {
            conditions.add(EntityCondition.makeCondition("carMileage", EntityOperator.LESS_THAN_EQUAL_TO, new BigDecimal(carMileageHigh).multiply(new BigDecimal(10000))));
        }
        if (UtilValidate.isNotEmpty(keyWord)) {
            List<EntityCondition> keyWordConditions = FastList.newInstance();
            keyWordConditions.add(EntityCondition.makeCondition("productName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productSubheadName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDisplacementName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carFuelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carStructureName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carDriverName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeatName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productionModelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carSeriesName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("productBrandName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carCountryName", EntityOperator.LIKE, "%" + keyWord + "%"));
            keyWordConditions.add(EntityCondition.makeCondition("carLevelName", EntityOperator.LIKE, "%" + keyWord + "%"));
            conditions.add(EntityCondition.makeCondition(keyWordConditions, EntityOperator.OR));
        }
        List<String> carModelToSelect = FastList.newInstance();
        carModelToSelect.add("productCarModelId");
        carModelToSelect.add("productId");
        carModelToSelect.add("productName");
        carModelToSelect.add("productSubheadName");
        carModelToSelect.add("frontMoney");
        carModelToSelect.add("ratio");
        carModelToSelect.add("price");
        carModelToSelect.add("saleNum");
        List<String> carModelToOrder = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderField)) {
            carModelToOrder.add(orderField);
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
        try {
            Boolean beganTransaction = TransactionUtil.begin();
            //车量商品数据
            EntityListIterator eli = delegator.findListIteratorByCondition(carModelViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, carModelToSelect, carModelToOrder, findOpts);
            List<GenericValue> carModels = eli.getPartialList(lowIndex, highIndex);
            int max = eli.getResultsSizeAfterPartialList();
            request.setAttribute("max", max % viewSize == 0 ? max / viewSize : max / viewSize + 1);
            List<Map> carModelList = FastList.newInstance();
            for (GenericValue carModel : carModels) {
                Map modelMap = FastMap.newInstance();
                modelMap.put("productId", carModel.get("productId"));
                modelMap.put("productName", carModel.get("productName"));
                modelMap.put("productSubheadName", carModel.get("productSubheadName"));
                modelMap.put("useTime", carModel.get("useTime"));
                modelMap.put("carMileage", UtilValidate.isNotEmpty(carModel.get("carMileage")) ? carModel.getBigDecimal("carMileage").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP) : "");
                //售价
                modelMap.put("price", carModel.getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP));
                //市场价
                List<GenericValue> marketPriceList = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", carModel.get("productId"), "productPriceTypeId", "MARKET_PRICE"));
                if (UtilValidate.isNotEmpty(marketPriceList)) {
                    BigDecimal marketPrice;
                    marketPrice = marketPriceList.get(0).getBigDecimal("price").divide(new BigDecimal(10000)).setScale(2, BigDecimal.ROUND_HALF_UP);
                    modelMap.put("marketPrice", marketPrice);
                }
                //定金
                BigDecimal frontMoney;
                BigDecimal ratio = UtilValidate.isNotEmpty(carModel.getBigDecimal("ratio")) ? carModel.getBigDecimal("ratio") : BigDecimal.ZERO;
                if (UtilValidate.isNotEmpty(carModel.get("frontMoney"))) {
                    frontMoney = carModel.getBigDecimal("frontMoney").setScale(2, BigDecimal.ROUND_HALF_UP);
                } else {
                    frontMoney = carModel.getBigDecimal("price").multiply(ratio).divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP);
                }
                modelMap.put("frontMoney", frontMoney);
                //图片
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", carModel.get("productId"), "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    modelMap.put("imgUrl", request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + productContents.get(0).getString("contentId"));
                }
                //销量
                modelMap.put("saleNum", carModel.get("saleNum"));
                carModelList.add(modelMap);
            }
            request.setAttribute("carModelList", carModelList);
            eli.close();
            TransactionUtil.commit(beganTransaction);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 验证支付密码
     *
     * @param request
     * @param response
     * @return
     */
    public static String checkPayPassword(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String phoneId = request.getParameter("phoneId");
        String checkCode = request.getParameter("checkCode");
        String payPassword = request.getParameter("payPassword");
        if (UtilValidate.isEmpty(phoneId) && UtilValidate.isNotEmpty(jsonObject.get("phoneId"))) {
            phoneId = jsonObject.getString("phoneId");
        }
        if (UtilValidate.isEmpty(checkCode) && UtilValidate.isNotEmpty(jsonObject.get("checkCode"))) {
            checkCode = jsonObject.getString("checkCode");
        }
        if (UtilValidate.isEmpty(payPassword) && UtilValidate.isNotEmpty(jsonObject.get("payPassword"))) {
            payPassword = jsonObject.getString("payPassword");
        }
        if (UtilValidate.isEmpty(phoneId)) {
            request.setAttribute("error", "手机号不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(checkCode)) {
            request.setAttribute("error", "验证码不能为空");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(payPassword)) {
            request.setAttribute("error", "支付密码不能为空");
            response.setStatus(403);
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = null;
        GenericValue userLogin1 = null;
        GenericValue party = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            userLogin1 = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phoneId));
            party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(userLogin1)) {
            request.setAttribute("error", "该手机号不可用");
            response.setStatus(403);
            return "error";
        }
        if (!UtilValidate.areEqual(userLogin.get("partyId"), userLogin1.get("partyId"))) {
            request.setAttribute("error", "该手机号不属于当前登陆用户");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(party)) {
            request.setAttribute("error", "该会员不存在");
            response.setStatus(403);
            return "error";
        }
        GenericValue mobileCheckCode = null;
        try {
            mobileCheckCode = delegator.findByPrimaryKey("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(mobileCheckCode) || UtilValidate.isEmpty(mobileCheckCode.get("checkCode")) || !checkCode.equals(mobileCheckCode.get("checkCode"))) {
            request.setAttribute("error", "验证码不正确");
            response.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(party.get("payPassword"))) {
            request.setAttribute("error", "您未设置过支付密码");
            response.setStatus(403);
            return "error";
        } else {
            Boolean samePassword = HashCrypt.comparePassword(party.getString("payPassword"), MemberEvents.getHashType(), payPassword);
            if (samePassword) {
                request.setAttribute("success", "验证成功");
                return "success";
            } else {
                request.setAttribute("error", "密码错误");
                response.setStatus(403);
                return "error";
            }
        }
    }

    public static String getSystemTime(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        request.setAttribute("systemTime", UtilDateTime.timeStampToString(UtilDateTime.nowTimestamp(), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
        return "success";
    }

    public static String getServiceTel(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue frontRule = delegator.findByPrimaryKey("FrontRule", UtilMisc.toMap("frontRuleId", "front_rule"));
        if (UtilValidate.isNotEmpty(frontRule)) {
            request.setAttribute("serviceTel", frontRule.get("customerServiceNumber"));
        }
        return "success";
    }

    /**
     * 积分兑换数量获取
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String getScoreOrderNum(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        String statusId = request.getParameter("statusId");
        if (UtilValidate.isEmpty(statusId) && UtilValidate.isNotEmpty(jsonObject.get("statusId"))) {
            statusId = jsonObject.getString("statusId");
        }
        DynamicViewEntity dynamicViewViewEntity = new DynamicViewEntity();
        dynamicViewViewEntity.addMemberEntity("PSH", "PartyScoreHistory");   //会员兑换历史表
        dynamicViewViewEntity.addAlias("PSH", "orderId");
        dynamicViewViewEntity.addAlias("PSH", "partyId");
        dynamicViewViewEntity.addAlias("PSH", "scoreValue");
        dynamicViewViewEntity.addAlias("PSH", "createdStamp");

        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("partyId", userLogin.get("partyId")));
        if (UtilValidate.areEqual("ORDER_WAITPRODUCE", statusId)) {
            dynamicViewViewEntity.addMemberEntity("OH", "OrderHeader"); //订单
            dynamicViewViewEntity.addAlias("OH", "orderId");
            dynamicViewViewEntity.addAlias("OH", "statusId");
            dynamicViewViewEntity.addViewLink("PSH", "OH", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));

            //订单状态为：待提货
            conditions.add(EntityCondition.makeCondition("statusId", "ORDER_WAITPRODUCE"));
        }

        Boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewViewEntity, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, null);
        int resultListSize = pli.getResultsSizeAfterPartialList();
        pli.close();
        TransactionUtil.commit(beganTransaction);
        request.setAttribute("num", resultListSize);

        return "success";

    }

    /**
     * 积分规则
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws IOException
     */
    public static String getIntegralContent(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, IOException {
        String httpUrl = "http://116.62.159.142:18080/mmoserver/integral/queryIntegralContent";
        Map<String, String> headers = FastMap.newInstance();
        headers.put("code", "10001");
        headers.put("key", "e40dd4fa89ea4cfa9a424c5b47749755");
        headers.put("itfcode", "CRM_40");
        Map<String, Object> params = FastMap.newInstance();
        params.put("rowId", "25001961921183766");
        String result = HttpUtil.post(httpUrl, headers, params);
        JSONObject outInfo = HttpUtil.convertToJSONObject(result);
        String data = outInfo.getString("data");
        JSONObject dataInfo = HttpUtil.convertToJSONObject(data);
        String content = dataInfo.getString("content");
        request.setAttribute("content", content);
        return "success";

    }

    /**
     * 升级规则
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws IOException
     */
    public static String getLevelContent(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, IOException {
        String httpUrl = "http://116.62.159.142:18080/mmoserver/level/queryLevelContent";
        Map<String, String> headers = FastMap.newInstance();
        headers.put("code", "10001");
        headers.put("key", "e40dd4fa89ea4cfa9a424c5b47749755");
        headers.put("itfcode", "CRM_41");
        Map<String, Object> params = FastMap.newInstance();
        params.put("rowId", "25156037900238921");
        String result = HttpUtil.post(httpUrl, headers, params);
        JSONObject outInfo = HttpUtil.convertToJSONObject(result);
        String data = outInfo.getString("data");
        JSONObject dataInfo = HttpUtil.convertToJSONObject(data);
        String content = dataInfo.getString("content");
        request.setAttribute("content", content);
        return "success";

    }

    public static String getPartyDetail(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String partyId = request.getParameter("partyId");
        GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));

        request.setAttribute("name", UtilValidate.isNotEmpty(person.get("name")) ? person.get("name") : "");
        request.setAttribute("mobile", UtilValidate.isNotEmpty(person.get("mobile")) ? person.get("mobile") : "");
        return "success";

    }

    /**
     * 获取充值单号
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    public static String createRechargeDetail(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        //充值项编号
        String enumId = request.getParameter("enumId");
        if (UtilValidate.isEmpty(enumId) && UtilValidate.isNotEmpty(jsonObject.get("enumId"))) {
            enumId = jsonObject.getString("enumId");
        }
        //自定义充值金额
        String amount = request.getParameter("amount");
        if (UtilValidate.isEmpty(amount) && UtilValidate.isNotEmpty(jsonObject.get("amount"))) {
            amount = jsonObject.getString("amount");
        }
        //支付方式
        String paymentMethodTypeId = request.getParameter("paymentMethodTypeId");
        if (UtilValidate.isEmpty(paymentMethodTypeId) && UtilValidate.isNotEmpty(jsonObject.get("paymentMethodTypeId"))) {
            paymentMethodTypeId = jsonObject.getString("paymentMethodTypeId");
        }
        if (UtilValidate.isEmpty(paymentMethodTypeId)) {
            request.setAttribute("error", "支付方式不能为空");
            response.setStatus(403);
            return "error";
        }

        BigDecimal originalAmount = null;
        BigDecimal actualAmount = null;
        if (UtilValidate.isNotEmpty(enumId)) {
            GenericValue enumeration = delegator.findByPrimaryKey("Enumeration", UtilMisc.toMap("enumId", enumId));
            originalAmount = new BigDecimal(enumeration.getString("description"));
            actualAmount = new BigDecimal(enumeration.getString("remark"));
        } else {
            originalAmount = new BigDecimal(amount);
            actualAmount = new BigDecimal(amount);
        }
        String rechargeDetailId = delegator.getNextSeqId("RechargeDetail");
        GenericValue rechargeDetail = delegator.makeValue("RechargeDetail");
        rechargeDetail.set("rechargeDetailId", rechargeDetailId);
        rechargeDetail.set("partyId", userLogin.get("partyId"));
        rechargeDetail.set("originalAmount", originalAmount);
        rechargeDetail.set("actualAmount", actualAmount);
        rechargeDetail.set("paymentMethodTypeId", paymentMethodTypeId);
        rechargeDetail.set("statusId", "PAYMENT_NOT_RECEIVED");
        delegator.create(rechargeDetail);
        request.setAttribute("rechargeDetailId", rechargeDetailId);

        return "success";

    }

}
