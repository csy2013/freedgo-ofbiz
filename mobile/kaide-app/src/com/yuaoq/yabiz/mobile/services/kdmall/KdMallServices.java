package com.yuaoq.yabiz.mobile.services.kdmall;


import com.google.gson.Gson;
import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by changsy on 2018/4/17.
 */

public class KdMallServices {
    
//    static String url = "http://kdmallapipv.companycn.net";
//    static String api_url = "http://kdmallapipv.companycn.net/APP_API/index";
//
//    static String url = "https://api.capitaland.com.cn";
//    static String api_url = "https://api.capitaland.com.cn/APP_API/index";
    protected static Logger log = Logger.getLogger(KdMallServices.class);
    public static final String module = KdMallServices.class.getName();

    static String url = UtilProperties.getMessage("application.properties","kaide.interface.url",Locale.CHINA);
    static String api_url = UtilProperties.getMessage("application.properties","kaide.interface.api.url",Locale.CHINA);
    /**
     * 获取用户基本信息接口
     *com.yuaoq.yabiz.mobile.services.kdmall.KdMallServices
     * @return
     */
    public static Map<String, Object> getUserInfo(DispatchContext dct, Map<String, Object> context) {
        Delegator delegator = dct.getDelegator();
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "get_primary_info";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String object = "";
        String token = (String) context.get("token");
        String mall_id = (String) context.get("mall_id");
        String member_id = (String) context.get("member_id");
        
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("token", token);
            httpClient.setParameter("mall_id", mall_id);
            httpClient.setParameter("member_id", member_id);
            httpClient.setUrl(url + "/camelot/merchant_api");
            
            object = httpClient.post();
            
            Map result = new Gson().fromJson(object, Map.class);
            String retried = (String) context.get("retried");
            if (((Double) result.get("result")).intValue() == 2 && (UtilValidate.isEmpty(retried))) {
                
                Object token1 = retryUserToken(delegator, member_id);
                context.put("token", token1);
                context.put("retried", "Y");
                resultData = getUserInfo(dct, context);
            } else {
                resultData.put("result", object);
            }
            
            
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    private static String retryUserToken(Delegator delegator, String member_id) {
        String token = "";
        //代表token 失效
        List<GenericValue> persons = null;
        try {
            persons = delegator.findByAnd("Person", UtilMisc.toMap("member_id", member_id));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        if (UtilValidate.isNotEmpty(persons)) {
            GenericValue person = EntityUtil.getFirst(persons);
            String unionId = person.getString("unionId");
            String tp1 = "get_member_id_by_unionid";
            String timestamp1 = UtilDateTime.nowAsTenString();
            String nonce1 = getRandomStr();
            String Md5Sn1 = getMD5Sn(tp1, timestamp1, nonce1);
            HttpClient httpClient1 = new HttpClient();
            httpClient1.setDebug(true);
            httpClient1.setHeader("Content-Type", "application/x-www-form-urlencoded");
            try {
                httpClient1.setParameter("tp", tp1);
                httpClient1.setParameter("timestamp", timestamp1);
                httpClient1.setParameter("nonce", nonce1);
                httpClient1.setParameter("sn", Md5Sn1);
                httpClient1.setParameter("unionid", unionId);
                httpClient1.setUrl(url + "/camelot/merchant_api");
                httpClient1.setDebug(true);
                String object = httpClient1.post();
                System.out.println("object = " + object);
                KdRetData retData = new Gson().fromJson(object, KdRetData.class);
                if (UtilValidate.isNotEmpty(retData.getData())) {
                    //获取成功，{"result":22001,"msg":"用户ID获取成功","data":{"memberID":"111","token":"1ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f25077"}}
                    String memberId = (String) retData.getData().get("memberID");
                    token = (String) retData.getData().get("token");
                    
                    List<GenericValue> userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", person.getString("partyId")));
                    if (UtilValidate.isNotEmpty(userLogins)) {
                        for (int i = 0; i < userLogins.size(); i++) {
                            GenericValue userLogin = userLogins.get(i);
                            userLogin.set("lastToken", token);
                            userLogin.store();
                        }
                    }
                    
                }
            } catch (HttpClientException e) {
                e.printStackTrace();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }
        
        return token;
    }

    private static Map retryUserToken2(Delegator delegator, String member_id) {
        Map resMap = FastMap.newInstance();

        //代表token 失效
        List<GenericValue> persons = null;
        try {
            persons = delegator.findByAnd("Person", UtilMisc.toMap("member_id", member_id));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        if (UtilValidate.isNotEmpty(persons)) {
            GenericValue person = EntityUtil.getFirst(persons);
            String unionId = person.getString("unionId");
            String tp1 = "get_member_id_by_unionid";
            String timestamp1 = UtilDateTime.nowAsTenString();
            String nonce1 = getRandomStr();
            String Md5Sn1 = getMD5Sn(tp1, timestamp1, nonce1);
            HttpClient httpClient1 = new HttpClient();
            httpClient1.setDebug(true);
            httpClient1.setHeader("Content-Type", "application/x-www-form-urlencoded");
            try {
                httpClient1.setParameter("tp", tp1);
                httpClient1.setParameter("timestamp", timestamp1);
                httpClient1.setParameter("nonce", nonce1);
                httpClient1.setParameter("sn", Md5Sn1);
                httpClient1.setParameter("unionid", unionId);
                httpClient1.setUrl(url + "/camelot/merchant_api");
                httpClient1.setDebug(true);
                String object = httpClient1.post();
                
                KdRetData retData = new Gson().fromJson(object, KdRetData.class);
                if (UtilValidate.isNotEmpty(retData.getData())) {
                    //获取成功，{"result":22001,"msg":"用户ID获取成功","data":{"memberID":"111","token":"1ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f25077"}}
                    String memberId = (String) retData.getData().get("memberID");
                    String token = (String) retData.getData().get("token");
                    resMap.put("memberId",memberId);
                    resMap.put("token",token);
                    List<GenericValue> userLogins = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", person.getString("partyId")));
                    if (UtilValidate.isNotEmpty(userLogins)) {
                        for (int i = 0; i < userLogins.size(); i++) {
                            GenericValue userLogin = userLogins.get(i);
                            userLogin.set("lastToken", token);
                            userLogin.store();
                        }
                    }

                }
            } catch (HttpClientException e) {
                e.printStackTrace();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }

        return resMap;
    }
    
    
    /**
     * 根据unionId获取用户memberId,tokenId
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getMemberIdByUnionId(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "get_member_id_by_unionid";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String object = "";
        String unionid = (String) context.get("unionid");
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("unionid", unionid);
            httpClient.setUrl(url + "/camelot/merchant_api");
            
            object = httpClient.post();
            resultData.put("result", object);
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    /**
     * 用户登录注册接口
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> userLoginOrRegister(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "sync_member";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String object = "";
        String phone = (String) context.get("phone");
        String mall_id = (String) context.get("mall_id");
        String sex = (String) context.get("sex");
        String nick_name = (String) context.get("nick_name");
        String head_img_url = (String) context.get("head_img_url");
        String unionid = (String) context.get("unionid");
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("phone", phone);
            httpClient.setParameter("mall_id", mall_id);
            httpClient.setParameter("sex", sex);
            httpClient.setParameter("nick_name", nick_name);
            httpClient.setParameter("head_img_url", head_img_url);
            httpClient.setParameter("unionid", unionid);
            httpClient.setUrl(url + "/camelot/merchant_api");
            object = httpClient.post();
            
            resultData.put("result", object);
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    /**
     * 用户收货地址
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> userAddress(DispatchContext dct, Map<String, Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "update_member_address_list";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String object = "";
        String token = (String) context.get("token");
        String member_id = (String) context.get("member_id");
        String AddressType = (String) context.get("AddressType");
        String Address1 = (String) context.get("Address1");
        String Address2 = (String) context.get("Address2");
        String Address3 = (String) context.get("Address3");
        String Address4 = (String) context.get("Address4");
        String State = (String) context.get("State");
        String StateValue = (String) context.get("StateValue");
        String City = (String) context.get("City");
        String CityCode = (String) context.get("CityCode");
        String District = (String) context.get("District");
        String DistrictCode = (String) context.get("DistrictCode");
        String SubDistrict = (String) context.get("SubDistrict");
        String SubDistrictCode = (String) context.get("SubDistrictCode");
        String PostalCode = (String) context.get("PostalCode");
        String CountryCode = (String) context.get("CountryCode");
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("token", token);
            httpClient.setParameter("member_id", member_id);
            httpClient.setParameter("AddressType", AddressType);
            httpClient.setParameter("Address1", Address1);
            httpClient.setParameter("Address2", Address2);
            httpClient.setParameter("Address3", Address3);
            httpClient.setParameter("Address4", Address4);
            httpClient.setParameter("State", State);
            httpClient.setParameter("StateValue", StateValue);
            httpClient.setParameter("City", City);
            httpClient.setParameter("CityCode", CityCode);
            httpClient.setParameter("District", District);
            httpClient.setParameter("DistrictCode", DistrictCode);
            httpClient.setParameter("SubDistrict", SubDistrict);
            httpClient.setParameter("SubDistrictCode", SubDistrictCode);
            httpClient.setParameter("PostalCode", PostalCode);
            httpClient.setParameter("CountryCode", CountryCode);
            httpClient.setUrl(url + "/camelot/merchant_api");
            
            object = httpClient.post();
            
            Map result = new Gson().fromJson(object, Map.class);
            String retried = (String) context.get("retried");
            if (((Double) result.get("result")).intValue() == 2 && (UtilValidate.isEmpty(retried))) {
                Delegator delegator = dct.getDelegator();
                Object token1 = retryUserToken(delegator, member_id);
                context.put("token", token1);
                context.put("retried", "Y");
                resultData = userAddress(dct, context);
            } else {
                resultData.put("result", object);
            }
            
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    
    /**
     * 添加用户积分
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> addUserScore(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "add_point_by_code";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String result = "";
        
        String member_id = (String) context.get("member_id"); // 会员编号
        String integralCode = (String) context.get("integralCode");// 积分CODE
        String locationCode = (String) context.get("locationCode");// 地址code
        
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            
            //会员卡号
            httpClient.setParameter("memberNo", member_id);
            //商场位置代码
//            httpClient.setParameter("locationCode", locationCode);
            //积分code
//            httpClient.setParameter("integralCode", integralCode);
            String sendurl=url + "/rtmaps/member_api?locationCode=" + locationCode + "&integralCode=" + integralCode;
            log.info("url地址："+sendurl);
            log.info("参数："+httpClient.getParameters());
            log.info("header："+httpClient.getHeaders());

            

            httpClient.setUrl(sendurl);
            result = httpClient.post();
            
            resultData.put("result", result);
            
        } catch (HttpClientException e) {
            e.printStackTrace();
            resultData.put("result", "");
        }
        return resultData;
    }
    
    
    public static Map<String, Object> getUserScore(DispatchContext dct, Map<String, Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "get_integral";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String object = "";
        
        String token = (String) context.get("token"); //
        String member_id = (String) context.get("member_id");// 会员编号
        String mall_id = (String) context.get("mall_id");// 商场编号
        
        HttpClient httpClient = new HttpClient();
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("token", token);
            httpClient.setParameter("member_id", member_id);
            httpClient.setParameter("mall_id", mall_id);
            httpClient.setUrl(url + "/camelot/merchant_api");
            httpClient.setDebug(true);
            object = httpClient.post();
            
            Map result = new Gson().fromJson(object, Map.class);
            String retried = (String) context.get("retried");
            if (((Double) result.get("result")).intValue() == 2 && (UtilValidate.isEmpty(retried))) {
                Delegator delegator = dct.getDelegator();
                Map tokenRes = retryUserToken2(delegator, member_id);
                context.put("token", tokenRes.get("token"));
                context.put("member_id", tokenRes.get("memberId"));
                context.put("retried", "Y");
                resultData = getUserScore(dct, context);
            } else {
                resultData.put("result", object);
            }
            
        } catch (HttpClientException e) {
            e.printStackTrace();
            
        }
        return resultData;
    }
    
    /**
     * 三、	E-com_03通过unionID获取token和memberID
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getTokenAndMemberIdByUnionIdKd03(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "get_member_id_by_unionid";// 固定值get_member_id_by_unionid，该值会用于计算SN签名算法
        String timestamp = UtilDateTime.nowAsTenString();// 发起请求时的Unix时间戳
        String nonce = getRandomStr();// 位随机字符串
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);//防止外部恶意调用安全签名，参考SN签名算法生成。
        String unionid = (String) context.get("unionid"); // 用户的微信unionID
        String object = "";
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("unionid", unionid);
            httpClient.setUrl(url + "/camelot/merchant_api");
            httpClient.setDebug(true);
            object = httpClient.post();
            resultData.put("result", object);
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    
    /**
     * 六、	E-com_08传订单交易信息
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> sendOrderInfoKd08(DispatchContext dct, Map<String, Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "register_transaction";// 固定值register_transaction，该值会用于计算SN签名算法
        String timestamp = UtilDateTime.nowAsTenString();// 发起请求时的Unix时间戳
        String nonce = getRandomStr();// 位随机字符串
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);//防止外部恶意调用安全签名，参考SN签名算法生成。
        String token = (String) context.get("token"); // token
        String member_id = (String) context.get("member_id"); // 会员编号
        String mall_id = (String) context.get("mall_id");// 商场编号
        String amount = (String) context.get("amount");// 交易金额单位元
        String storeCode = (String) context.get("storeCode");// 商户代码（CRM提供）
        String receiptNo = (String) context.get("receiptNo");// 订单号
        String locationId = (String) context.get("locationId");// 地址
        String object = "";
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("token", token);
            httpClient.setParameter("member_id", member_id);
            httpClient.setParameter("mall_id", mall_id);
            httpClient.setParameter("amount", amount);
            httpClient.setParameter("storeCode", storeCode);
            httpClient.setParameter("receiptNo", receiptNo);
//            httpClient.setParameter("locationId", locationId);
            String tempStr="tp:"+tp+" timestamp:"+timestamp+" nonce:"+nonce+" sn:"+Md5Sn+" token:"+token+" member_id:"+member_id+" mall_id:"+mall_id+" amount:"+amount+" storeCode:"+storeCode+" receiptNo:"+receiptNo;
            System.out.println(tempStr);
            httpClient.setDebug(true);
//            httpClient.setUrl("http://kdmallapipv.companycn.net/camelot/merchant_api");
            httpClient.setUrl(url + "/camelot/merchant_api");
            object = httpClient.post();
            
            Map result = new Gson().fromJson(object, Map.class);
            String retried = (String) context.get("retried");
            if (((Double) result.get("result")).intValue() == 2 && (UtilValidate.isEmpty(retried))) {
                Delegator delegator = dct.getDelegator();
                Object token1 = retryUserToken(delegator, member_id);
                context.put("token", token1);
                context.put("retried", "Y");
                resultData = sendOrderInfoKd08(dct, context);
            } else {
                resultData.put("result", object);
            }
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    /**
     * 十、	E-com_15扣减积分API
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> consumeIntegral(DispatchContext dct, Map<String, Object> context) {
        Debug.logInfo("center consumeIntegral ==========================================="+context.get("token"),module);
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "consume_integral";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String object = "";
        String token = (String) context.get("token");
        String member_id = (String) context.get("member_id");
        String mall_id = (String) context.get("mall_id");
        String merchant_id = (String) context.get("merchant_id");
        String integral = (String) context.get("integral");
        String description = (String) context.get("description");
        
        HttpClient httpClient = new HttpClient();
        httpClient.setDebug(true);
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("token", token);
            httpClient.setParameter("member_id", member_id);
            httpClient.setParameter("mall_id", mall_id);
            httpClient.setParameter("integral", integral);
            httpClient.setParameter("description", description);
            httpClient.setParameter("merchant_id", merchant_id);
            httpClient.setUrl(url + "/camelot/merchant_api");
            Debug.logInfo("center consumeIntegral url ==========================================="+url,module);
            object = httpClient.post();
            Debug.logInfo("consumeIntegral==========================================="+object,module);
            
            Map result = new Gson().fromJson(object, Map.class);
            String retried = (String) context.get("retried");
            if (((Double) result.get("result")).intValue() == 2 && (UtilValidate.isEmpty(retried))) {
                Delegator delegator = dct.getDelegator();
                Object token1 = retryUserToken(delegator, member_id);
                context.put("token", token1);
                context.put("retried", "Y");
                resultData = consumeIntegral(dct, context);
            } else {
                resultData.put("result", object);
            }
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    /**
     * 根据经纬度获取用户最近的mallId
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> getMallByLngLat(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String tp = "getmalllist";
        String timestamp = UtilDateTime.nowAsTenString();
        String nonce = getRandomStr();
        String Md5Sn = getMD5Sn(tp, timestamp, nonce);
        String object = "";
        String lng = (String) context.get("lng");
        String lat = (String) context.get("lat");
        HttpClient httpClient = new HttpClient();
        httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
        try {
            httpClient.setParameter("tp", tp);
            httpClient.setParameter("timestamp", timestamp);
            httpClient.setParameter("nonce", nonce);
            httpClient.setParameter("sn", Md5Sn);
            httpClient.setParameter("lng", lng);
            httpClient.setParameter("lat", lat);
            httpClient.setUrl(api_url);
            httpClient.setDebug(true);
            object = httpClient.post();
            resultData.put("result", object);
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return resultData;
    }
    
    private static String getMD5Sn(String tp, String timestamp, String nonce) {
        List<String> paramsList = new ArrayList<String>();
        paramsList.add(timestamp);
        paramsList.add(nonce);
        paramsList.add(tp);
        // 字段顺序排序
        Collections.sort(paramsList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String str1 = o1.toUpperCase();
                String str2 = o2.toUpperCase();
                return str1.compareTo(str2);
            }
        });
        String joinStr = paramsList.get(0) + "^" + paramsList.get(1) + "^" + paramsList.get(2);
        String md5Str = getMD5Str(joinStr);
        String subStr = md5Str.substring(0, 10) + "Companycn" + md5Str.substring(10, md5Str.length());
        String md5Str2 = getMD5Str(subStr);
        return md5Str2;
    }
    
    // 随机生成16位字符串
    private static String getRandomStr() {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }
    
    private static String getMD5Str(String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        byte[] byteArray = messageDigest.digest();
        
        StringBuffer md5StrBuff = new StringBuffer();
        
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString();
    }

    
   
    
    
}
