package com.qihua.ofbiz.weixin.service;

import com.qihua.ofbiz.weixin.util.CommonUtil;
import net.sf.json.JSONObject;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by AlexYao 2016-4-22 14:46:38
 */
public class WeixinEvents {

    public static final String module = WeixinEvents.class.getName();

    /**
     * 获取code
     *
     * @param req
     * @param resp
     * @return
     */
    public static String weixinAuth(HttpServletRequest req, HttpServletResponse resp) {
        String fromUrl = req.getParameter("fromUrl");
        String toUrl = req.getParameter("toUrl");
        String toUrl1 = req.getParameter("toUrl1");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(req.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(fromUrl) && UtilValidate.isNotEmpty(jsonObject.get("fromUrl"))) {
            fromUrl = jsonObject.getString("fromUrl");
        }
        if (UtilValidate.isEmpty(toUrl) && UtilValidate.isNotEmpty(jsonObject.get("toUrl"))) {
            toUrl = jsonObject.getString("toUrl");
        }
        if (UtilValidate.isEmpty(toUrl1) && UtilValidate.isNotEmpty(jsonObject.get("toUrl1"))) {
            toUrl1 = jsonObject.getString("toUrl1");
        }
        if (UtilValidate.isEmpty(fromUrl)) {
            req.setAttribute("error", "缺少参数fromUrl");
            resp.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(toUrl)) {
            req.setAttribute("error", "缺少参数toUrl");
            resp.setStatus(403);
            return "error";
        }
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appId", "");
        String authBackUrl = UtilProperties.getPropertyValue("weixin.properties", "weixinAuthback", "");
        authBackUrl = URLEncoder.encode(authBackUrl + "?fromUrl=" + fromUrl.replaceAll("&", "%26").replaceAll("#", "_365_") + "&toUrl=" + toUrl.replaceAll("&", "%26").replaceAll("#", "_365_") + "&toUrl1=" + toUrl1.replaceAll("&", "%26").replaceAll("#", "_365_"));

        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=" + appId +
                "&redirect_uri=" +
                authBackUrl +
                "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
        Debug.log("wapUrl++++++++++++++++++++++++++++++++++++++:" + url);
        try {
            resp.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 获取微信信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String weixinAuthback(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Debug.log("I'm coming++++++++++++++++++++++++++++++++++++++++++" + request.getParameterMap().toString());
        String fromUrl = request.getParameter("fromUrl").replaceAll("_365_", "#");
        Debug.log("fromUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + fromUrl);
        String toUrl = request.getParameter("toUrl").replaceAll("_365_", "#");
        Debug.log("toUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl);
        String toUrl1 = request.getParameter("toUrl1").replaceAll("_365_", "#");
        Debug.log("toUrl1++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl1);
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appId", "");
        String appSecret = UtilProperties.getPropertyValue("weixin.properties", "appSecret", "");
        String code = request.getParameter("code");
        String refreshToken = request.getParameter("refresh_token");
        JSONObject userInfoJsonObject = null;
        Debug.log("code++++++++++++++++++++++++++++++++++++++++++++++++:" + code);
        String URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecret + "&code=" + code + "&grant_type=authorization_code";//通过code换取网页授权access_token
        JSONObject jsonObject = CommonUtil.httpsRequest(URL, "GET", null);
        Debug.log("jsonObject++++++++++++++++++++++++++++++++++++++++++++++++:" + jsonObject.toString());
        if (null != jsonObject) {
            String openId = jsonObject.getString("openid");
            String accessToken = jsonObject.getString("access_token");
            if (UtilValidate.isNotEmpty(openId) && UtilValidate.isNotEmpty(accessToken)) {
                String checkAccessTokenURL = "https://api.weixin.qq.com/sns/auth?access_token=" + accessToken + "&openid=" + openId;//检验授权凭证（access_token）是否有效
                JSONObject checkAccessTokenJsonObject = CommonUtil.httpsRequest(checkAccessTokenURL, "GET", null);
                String errcode = checkAccessTokenJsonObject.getString("errcode");
                if ("0".equals(errcode)) {
                    String getUserInfoURL = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";//拉取用户信息(需scope为 snsapi_userinfo)
                    userInfoJsonObject = CommonUtil.httpsRequest(getUserInfoURL, "GET", null);
                } else {
                    String refreshURL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=" + appId + "&grant_type=refresh_token&refresh_token=" + refreshToken;//刷新access_token
                    JSONObject refreshJsonObject = CommonUtil.httpsRequest(refreshURL, "GET", null);
                    if (null != jsonObject) {
                        openId = refreshJsonObject.getString("openid");
                        accessToken = refreshJsonObject.getString("access_token");
                        String getUserInfoURL = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";//拉取用户信息(需scope为 snsapi_userinfo)
                        userInfoJsonObject = CommonUtil.httpsRequest(getUserInfoURL, "GET", null);
                    } else {
                        request.setAttribute("error", "刷新access_token失败");
//                        return "error";
                    }
                }
                if (UtilValidate.isNotEmpty(userInfoJsonObject.getString("nickname"))) {
                    //获取用户信息成功
                    String nickname = userInfoJsonObject.getString("nickname");
                    String sex = userInfoJsonObject.getString("sex");
                    String headimgurl = userInfoJsonObject.getString("headimgurl");
                    String unionId = userInfoJsonObject.getString("unionid");

                    GenericValue userLogin = null;
                    try {
                        userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", unionId));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(userLogin)) {
                        if (UtilValidate.isEmpty(userLogin.get("openId"))) {
                            userLogin.set("openId", openId);
                            delegator.store(userLogin);
                        }
                        if (UtilValidate.isEmpty(userLogin.get("token"))) {
                            String token = new TokenProcessor().generateToken(userLogin.getString("userLoginId"), true);
                            userLogin.set("token", token);
                            delegator.store(userLogin);
                        }
                        if (toUrl.contains("?")) {
                            toUrl1 = toUrl1 + "&unionId=" + unionId + "&authToken=" + userLogin.get("token");
                        } else {
                            toUrl1 = toUrl1 + "?unionId=" + unionId + "&authToken=" + userLogin.get("token");
                        }
                        Debug.log("toUrl1++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl1);
                        try {
                            response.sendRedirect(toUrl1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "success";
                    }

                    if (toUrl.contains("?")) {
                        toUrl = toUrl + "&openId=" + openId + "&nickname=" + URLEncoder.encode(nickname) + "&sex=" + sex + "&headimgurl=" + URLEncoder.encode(headimgurl) + "&unionId=" + unionId;
                    } else {
                        toUrl = toUrl + "?openId=" + openId + "&nickname=" + URLEncoder.encode(nickname) + "&sex=" + sex + "&headimgurl=" + URLEncoder.encode(headimgurl) + "&unionId=" + unionId;
                    }
                    Debug.log("toUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl);
                    try {
                        response.sendRedirect(toUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "success";
                } else {
                    request.setAttribute("error", "请先关注我们的公众号");
//                    return "error";
                }
            }
        } else {
            request.setAttribute("error", "通过code换取网页授权access_token失败");
//            return "error";
        }
        try {
            response.sendRedirect(fromUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * PC端获取code
     *
     * @param req
     * @param resp
     * @return
     */
    public static String weixinAuthPC(HttpServletRequest req, HttpServletResponse resp) {
        String fromUrl = req.getParameter("fromUrl");
        String toUrl = req.getParameter("toUrl");
        String toUrl1 = req.getParameter("toUrl1");
        JSONObject jsonObject = new JSONObject();
        String input = null;
        try {
            input = RequestUtil.convertStreamToString(req.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(input)) {
            jsonObject = JSONObject.fromObject(input);
        }
        if (UtilValidate.isEmpty(fromUrl) && UtilValidate.isNotEmpty(jsonObject.get("fromUrl"))) {
            fromUrl = jsonObject.getString("fromUrl");
        }
        if (UtilValidate.isEmpty(toUrl) && UtilValidate.isNotEmpty(jsonObject.get("toUrl"))) {
            toUrl = jsonObject.getString("toUrl");
        }
        if (UtilValidate.isEmpty(toUrl1) && UtilValidate.isNotEmpty(jsonObject.get("toUrl1"))) {
            toUrl1 = jsonObject.getString("toUrl1");
        }
        if (UtilValidate.isEmpty(fromUrl)) {
            req.setAttribute("error", "缺少参数fromUrl");
            resp.setStatus(403);
            return "error";
        }
        if (UtilValidate.isEmpty(toUrl)) {
            req.setAttribute("error", "缺少参数toUrl");
            resp.setStatus(403);
            return "error";
        }
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appIdPC", "");
        String authBackUrl = UtilProperties.getPropertyValue("weixin.properties", "weixinAuthbackPC", "");
        authBackUrl = URLEncoder.encode(authBackUrl + "?fromUrl=" + fromUrl.replaceAll("&", "%26").replaceAll("#", "_365_") + "&toUrl=" + toUrl.replaceAll("&", "%26").replaceAll("#", "_365_") + "&toUrl1=" + toUrl1.replaceAll("&", "%26").replaceAll("#", "_365_"));

        String url = "https://open.weixin.qq.com/connect/qrconnect?" +
                "appid=" + appId +
                "&redirect_uri=" + authBackUrl +
                "&response_type=code&scope=snsapi_login,snsapi_userinfo&state=123#wechat_redirect";
        Debug.log("wapUrl++++++++++++++++++++++++++++++++++++++:" + url);
        try {
            resp.sendRedirect(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * PC端获取微信信息
     *
     * @param request
     * @param response
     * @return
     */
    public static String weixinAuthbackPC(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Debug.log("I'm coming++++++++++++++++++++++++++++++++++++++++++" + request.getParameterMap().toString());
        String fromUrl = request.getParameter("fromUrl").replaceAll("_365_", "#").replaceAll("车展", URLEncoder.encode("车展", "utf-8"));
        Debug.log("fromUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + fromUrl);
        String toUrl = request.getParameter("toUrl").replaceAll("_365_", "#").replaceAll("车展", URLEncoder.encode("车展", "utf-8"));
        Debug.log("toUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl);
        String toUrl1 = request.getParameter("toUrl1").replaceAll("_365_", "#").replaceAll("车展", URLEncoder.encode("车展", "utf-8"));
        Debug.log("toUrl1++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl1);
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appIdPC", "");
        String appSecret = UtilProperties.getPropertyValue("weixin.properties", "appSecretPC", "");
        String code = request.getParameter("code");
        String refreshToken = request.getParameter("refresh_token");
        JSONObject userInfoJsonObject = null;
        Debug.log("code++++++++++++++++++++++++++++++++++++++++++++++++:" + code);
        String URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecret + "&code=" + code + "&grant_type=authorization_code";//通过code换取网页授权access_token
        JSONObject jsonObject = CommonUtil.httpsRequest(URL, "GET", null);
        Debug.log("jsonObject++++++++++++++++++++++++++++++++++++++++++++++++:" + jsonObject.toString());
        if (null != jsonObject) {
            String openId = jsonObject.getString("openid");
            String accessToken = jsonObject.getString("access_token");
            if (UtilValidate.isNotEmpty(openId) && UtilValidate.isNotEmpty(accessToken)) {
                String checkAccessTokenURL = "https://api.weixin.qq.com/sns/auth?access_token=" + accessToken + "&openid=" + openId;//检验授权凭证（access_token）是否有效
                JSONObject checkAccessTokenJsonObject = CommonUtil.httpsRequest(checkAccessTokenURL, "GET", null);
                String errcode = checkAccessTokenJsonObject.getString("errcode");
                if ("0".equals(errcode)) {
                    String getUserInfoURL = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";//拉取用户信息(需scope为 snsapi_userinfo)
                    userInfoJsonObject = CommonUtil.httpsRequest(getUserInfoURL, "GET", null);
                } else {
                    String refreshURL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=" + appId + "&grant_type=refresh_token&refresh_token=" + refreshToken;//刷新access_token
                    JSONObject refreshJsonObject = CommonUtil.httpsRequest(refreshURL, "GET", null);
                    if (null != jsonObject) {
                        openId = refreshJsonObject.getString("openid");
                        accessToken = refreshJsonObject.getString("access_token");
                        String getUserInfoURL = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken + "&openid=" + openId + "&lang=zh_CN";//拉取用户信息(需scope为 snsapi_userinfo)
                        userInfoJsonObject = CommonUtil.httpsRequest(getUserInfoURL, "GET", null);
                    } else {
                        request.setAttribute("error", "刷新access_token失败");
//                        return "error";
                    }
                }
                if (UtilValidate.isNotEmpty(userInfoJsonObject.getString("nickname"))) {
                    //获取用户信息成功
                    String nickname = userInfoJsonObject.getString("nickname");
                    String sex = userInfoJsonObject.getString("sex");
                    String headimgurl = userInfoJsonObject.getString("headimgurl");
                    String unionId = userInfoJsonObject.getString("unionid");

                    GenericValue userLogin = null;
                    try {
                        userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", unionId));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(userLogin)) {
                        if (UtilValidate.isEmpty(userLogin.get("openId"))) {
                            userLogin.set("openId", openId);
                            delegator.store(userLogin);
                        }
                        if (UtilValidate.isEmpty(userLogin.get("token"))) {
                            String token = new TokenProcessor().generateToken(userLogin.getString("userLoginId"), true);
                            userLogin.set("token", token);
                            delegator.store(userLogin);
                        }
                        if (toUrl1.contains("?")) {
                            toUrl1 = toUrl1 + "&unionId=" + unionId + "&authToken=" + userLogin.get("token");
                        } else {
                            toUrl1 = toUrl1 + "?unionId=" + unionId + "&authToken=" + userLogin.get("token");
                        }
                        Debug.log("toUrl1++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl1);
                        try {
                            response.sendRedirect(toUrl1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "success";
                    }

                    if (toUrl.contains("?")) {
                        toUrl = toUrl + "&openId=" + openId + "&nickname=" + URLEncoder.encode(nickname) + "&sex=" + sex + "&headimgurl=" + URLEncoder.encode(headimgurl) + "&unionId=" + unionId;
                    } else {
                        toUrl = toUrl + "?openId=" + openId + "&nickname=" + URLEncoder.encode(nickname) + "&sex=" + sex + "&headimgurl=" + URLEncoder.encode(headimgurl) + "&unionId=" + unionId;
                    }
                    Debug.log("toUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl);
                    try {
                        response.sendRedirect(toUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "success";
                } else {
                    request.setAttribute("error", "请先关注我们的公众号");
//                    return "error";
                }
            }
        } else {
            request.setAttribute("error", "通过code换取网页授权access_token失败");
//            return "error";
        }
        try {
            response.sendRedirect(fromUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }
}
