package com.qihua.ofbiz.weixin.service;

import com.qihua.ofbiz.weixin.util.HttpClientConnectionManager;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Alex on 2016/5/26.
 */
public class WeixinServices {
    public static final String module = WeixinServices.class.getName();
    public static final String resource = "WeixinUiLabels";
    public static final String resourceError = "WeixinErrorUiLabels";

    public static DefaultHttpClient httpclient;

    static {
        httpclient = new DefaultHttpClient();
        httpclient = (DefaultHttpClient) HttpClientConnectionManager.getSSLInstance(httpclient);
    }

    /**
     * 获取微信服务器Access Token， 通过定时任务和在应用加载时使用
     *
     * @param dctx
     * @param context
     * @return
     * @throws org.ofbiz.service.GenericServiceException
     */
    public static Map<String, Object> getWeixinAccessToken(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException {
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> result = FastMap.newInstance();
        String errMsg = null;
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appId", "");
        String appSecret = UtilProperties.getPropertyValue("weixin.properties", "appSecret", "");
        if ("".equals(appId) || "".equals(appSecret)) {
            errMsg = UtilProperties.getMessage(resourceError,
                    "WeixinServices.problems_getting_weixin_app", locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }

        Map<String, Object> parameters = FastMap.newInstance();
        parameters.put("grant_type", UtilProperties.getPropertyValue("weixin.properties", "accessToken", "client_credential"));
        parameters.put("appid", appId);
        parameters.put("secret", appSecret);
        String weixinUrl = UtilProperties.getPropertyValue("weixin.properties", "weixinUrl");
        HttpClient http = new HttpClient(weixinUrl, parameters);
        http.setAllowUntrusted(true);
        String getResult = null;
        try {
            getResult = http.get(true); //通过get方法获取https ，true：信任如何ssl
        } catch (HttpClientException e) {
            throw new GenericServiceException("Problems invoking HTTP request", e);
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = JSONObject.fromObject(getResult);
        } catch (Exception e) {
            throw new GenericServiceException("Problems convert result.", e);
        }

        Object accessToken = jsonObject.get("access_token") == null ? "" : (String) jsonObject.get("access_token");
        Debug.log("get weixin server access token=" + accessToken, module);
        Long expiresIn = jsonObject.get("expires_in") == null ? -1L : ((Integer) jsonObject.get("expires_in")).longValue();
        Long errcode = jsonObject.get("errcode") == null ? -1L : ((Integer) jsonObject.get("errcode")).longValue();
        String errmsg = jsonObject.get("errmsg") == null ? "" : (String) jsonObject.get("errmsg");
        Debug.log("errcode=" + errcode);
        Debug.log("errmsg=" + errmsg);

        //begin
        String jsapi_ticket = "";
        if (errcode.longValue() == -1L || ("".equals(errmsg))) {
            String weixinUrl1 = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + String.valueOf(accessToken) + "&type=jsapi";
            HttpClient http1 = new HttpClient(weixinUrl1);
            http1.setAllowUntrusted(true);
            String getResult1 = null;
            try {
                getResult1 = http1.get(true); //通过get方法获取https ，true：信任如何ssl
            } catch (HttpClientException e) {
                throw new GenericServiceException("Problems invoking HTTP request", e);
            }

            JSONObject jsonObject1 = null;
            try {
                jsonObject1 = JSONObject.fromObject(getResult1);
            } catch (Exception e) {
                throw new GenericServiceException("Problems convert result.", e);
            }

            Object ticket = jsonObject1.get("ticket");
            Debug.log("get weixin server ticket=" + ticket, module);

            jsapi_ticket = String.valueOf(ticket);
        }
        //end

        Delegator delegator = dctx.getDelegator();
        try {
            String id = delegator.getNextSeqId("WeixinAccessToken");
            if (errcode.longValue() != -1L && (!"".equals(errmsg))) {
                GenericValue hisToken = delegator.makeValidValue("WeixinAccessTokenHis", UtilMisc.toMap("errcode", errcode, "errmsg", errmsg, "id", id));
                hisToken.create();
            } else {
                delegator.removeAll("WeixinAccessToken");
                GenericValue token = delegator.makeValidValue("WeixinAccessToken", UtilMisc.toMap("accessToken", accessToken, "expiresIn", expiresIn, "jsapiTicket", jsapi_ticket));
                token.put("id", id);
                token.create();
                GenericValue hisToken = delegator.makeValidValue("WeixinAccessTokenHis", UtilMisc.toMap("accessToken", accessToken, "expiresIn", expiresIn, "id", id, "jsapiTicket", jsapi_ticket));
                hisToken.create();
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, "Entity/data problem creating commission invoice: " + e.toString(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "GetWeixinAccessTokenDataProblem",
                    UtilMisc.toMap("reason", e.toString()), locale));
        }
        return result;
    }

    /**
     * 获取Access Token， 供微信各接口调用使用
     *
     * @param dctx
     * @param context
     * @return
     * @throws org.ofbiz.service.GenericServiceException
     */
    public static Map<String, Object> getAccessToken(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException {
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = dctx.getDelegator();
        try {
            GenericValue value = delegator.findOne("WeixinAccessToken", true, null);
            result = ServiceUtil.returnSuccess();
            result.put("accessToken", value.get("accessToken"));

        } catch (GenericEntityException e) {
            Debug.logError(e, "Entity/data problem creating commission invoice: " + e.toString(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "GetWeixinAccessTokenDataProblem",
                    UtilMisc.toMap("reason", e.toString()), locale));
        }
        return result;
    }

    /**
     * 获取jsapi_ticket、nonceStr、timestamp、signature， 供微信各接口调用使用 Add By AlexYao
     *
     * @param dctx
     * @param context
     * @return
     * @throws org.ofbiz.service.GenericServiceException
     */
    public static Map<String, Object> getWeixinConfig(DispatchContext dctx, Map<String, ? extends Object> context) throws GenericServiceException {
        Locale locale = (Locale) context.get("locale");
        Delegator delegator = dctx.getDelegator();
        String url = (String) context.get("url");
        Map<String, Object> result = FastMap.newInstance();
        String errMsg = null;
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appId", "");
        String appSecret = UtilProperties.getPropertyValue("weixin.properties", "appSecret", "");
        if ("".equals(appId) || "".equals(appSecret)) {
            errMsg = UtilProperties.getMessage(resourceError,
                    "WeixinServices.problems_getting_weixin_app", locale);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, errMsg);
            return result;
        }
        result.put("appId", appId);

        String access_token = "";
        String jsapi_ticket = "";
        try {
            GenericValue value = EntityUtil.getFirst(delegator.findList("WeixinAccessToken", null, null, UtilMisc.toList("id"), null, false));
            access_token = (String) value.get("accessToken");
            jsapi_ticket = (String) value.get("jsapiTicket");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Entity/data problem creating commission invoice: " + e.toString(), module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "GetWeixinAccessTokenDataProblem",
                    UtilMisc.toMap("reason", e.toString()), locale));
        }
        result.put("accessToken", access_token);


        Map<String, Object> ret = sign(jsapi_ticket, url);
        for (Map.Entry entry : ret.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
//            System.out.println(entry.getKey() + ", " + entry.getValue());
        }
        return result;
    }


    public static Map<String, Object> sign(String jsapi_ticket, String url) {
        Map<String, Object> ret = new HashMap<String, Object>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;
//        System.out.println(string1);

        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

//        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("timestamp", timestamp);
        ret.put("nonceStr", nonce_str);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    public static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    public static Map<String, Object> sendWeiXinMessage(DispatchContext ctx, Map<String, ? extends Object> context) {
        Map<String, Object> returnMap = new HashMap<String, Object>();
        String partyId = (String) context.get("partyId");
        String productReviewId = (String) context.get("productReviewId");
        Delegator delegator = ctx.getDelegator();

        String openId = "";

        GenericValue person = null;
        GenericValue productReview = null;
        try {
            person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
            productReview = delegator.findByPrimaryKey("ProductReview", UtilMisc.toMap("productReviewId", productReviewId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", productReview.get("userLoginId")));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        GenericValue value = null;

        try {
            value = EntityUtil.getFirst(delegator.findList("WeixinAccessToken", null, null, UtilMisc.toList("id"), null, false));
        } catch (GenericEntityException e) {
            e.printStackTrace();
            returnMap.put("result", "fail");
        }
        if (UtilValidate.isNotEmpty(userLogin.get("openId"))) {
            openId = userLogin.getString("openId");
        } else {
            returnMap.put("result", "fail");
        }
        if (UtilValidate.isNotEmpty(openId)) {
            //处理用户授权

            //发送模块消息 TM00533
            //n5bXf0f36WyTUVTeqKNPEA7cWUCl4esL-Oq3sT4iIbk  生产机
            //4MRvdaG98erD-h3GdELiLCBu804ZLDNtsvq2bwNPd8U  测试机

            if (value != null) {

                String sendUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + value.get("accessToken");
                String xmlParam = "  {\n" +
                        "           \"touser\":\"" + openId + "\",\n" +
                        "           \"template_id\":\"n5bXf0f36WyTUVTeqKNPEA7cWUCl4esL-Oq3sT4iIbk\",\n" +
                        "           \"url\":\"http://www.yanhuitong.com/hotelapp/control/orderUn?type=wait&orderId=" + 1 + "\",\n" +
                        "           \"topcolor\":\"#FF0000\",\n" +
                        "           \"data\":{\n" +
                        "                   \"first\": {\n" +
                        "                       \"value\":\"收到一个新的订单，请及时处理\",\n" +
                        "                       \"color\":\"#173177\"\n" +
                        "                   },\n" +
                        "                   \"Day\":{\n" +
                        "                       \"value\":\"" + 1 + "\",\n" +
                        "                       \"color\":\"#173177\"\n" +
                        "                   },\n" +
                        "                   \"orderId\": {\n" +
                        "                       \"value\":\"" + 1 + "\",\n" +
                        "                       \"color\":\"#173177\"\n" +
                        "                   },\n" +
                        "                   \"orderType\": {\n" +
                        "                       \"value\":\"" + 1 + "\",\n" +
                        "                       \"color\":\"#173177\"\n" +
                        "                   },\n" +
                        "                   \"customerName\":{\n" +
                        "                       \"value\":\"" + 1 + "\",\n" +
                        "                       \"color\":\"#173177\"\n" +
                        "                   },\n" +
                        "                   \"customerPhone\": {\n" +
                        "                       \"value\":\"" + 1 + "\",\n" +
                        "                       \"color\":\"#173177\"\n" +
                        "                   },\n" +
                        "                   \"remark\": {\n" +
                        "                       \"value\":\"" + 1 + "\",\n" +
                        "                       \"color\":\"#173177\"\n" +
                        "                   }\n" +
                        "           }\n" +
                        "       }";
                DefaultHttpClient client = new DefaultHttpClient();
                client.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
                HttpPost httpost = HttpClientConnectionManager.getPostMethod(sendUrl);

                try {
                    httpost.setEntity(new StringEntity(xmlParam, "UTF-8"));
                    HttpResponse res = httpclient.execute(httpost);
                    String jsonStr = EntityUtils.toString(res.getEntity(), "UTF-8");

                    Debug.log(jsonStr);
                    if (jsonStr.indexOf("40001") != -1 && jsonStr.indexOf("access_token") != -1) {
                        LocalDispatcher dispatcher = ctx.getDispatcher();
                        dispatcher.runSync("weixinAccessTokenGet", null);
                        //token过期，推送消息到酒店人员微信
                        Map<String, Object> serviceContext = FastMap.newInstance();
                        serviceContext.put("productReviewId", productReviewId);
                        try {
                            dispatcher.runSync("sendWeiXinMessageServices", serviceContext);
                        } catch (GenericServiceException e) {
                            e.printStackTrace();
                        }
                        returnMap.put("result", "success");
                        return returnMap;
                    }
//                        Map map = doXMLParse(jsonStr);
//                        String return_code = (String) map.get("errcode");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    returnMap.put("result", "fail");
                }
            } else {
                returnMap.put("result", "fail");
            }
//            request.setAttribute("success", "TRUE");
//            request.setAttribute("openId", openId);
//                return "success";
        } else {
            returnMap.put("result", "fail");
        }
        returnMap.put("result", "success");
        return returnMap;

    }

}
