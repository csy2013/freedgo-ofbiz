package com.qihua.ofbiz.member;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ofbiz.base.crypto.HashCrypt;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.util.EntityUtilProperties;
import org.ofbiz.party.contact.ContactHelper;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

import com.ibm.icu.text.SimpleDateFormat;

/**
 * 文件名：com.qihua.ofbiz.member
 * 版权：启华
 * 描述：会员管理：同步弘阳会员数据到本地库，在中台进行查询,以及对账户余额进行修改
 * 修改人：Wcy
 * 修改时间：2016-01-06
 * 修改单号：
 * 修改内容：
 */
public class MemberService {

    /**
     * 定义类名字符串
     */
    public static final String module = MemberService.class.getName();
    /**
     * 国际化资源文件名
     */
    public static final String resource = "PartyUiLabels";
    /**
     * 异常消息国际化资源文件名
     */
    public static final String resourceError = "PartyErrorUiLabels";


    /**
     * 弘阳接口调用：通过此方法自动登录并同步会员基本信息
     * 和社区信息
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> loginSync(DispatchContext dctx, Map<String, ? extends Object> context) {
        /**获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        /** 获取参数 */
        String sign = (String) context.get("sign");
        String token = (String) context.get("token");
        //String token = "6670ad76e0df4c4da1b2d5a98c0c48c91458531892128";
        String phone = (String) context.get("phone");
        //String phone = "15195821011";
        String recData = "{" + "\"phone\"" + ":" + "\"" + phone + "\"" + ",\"token\"" + ":" + "\"" + token + "\"" + "}";
        String result = "";
        String errMsg = "";
        try {
            /** 发送请求并获取响应数据 */
            //result = invoiceAPI(sign, token, "loginautoapi", "loginautoapi",recData);
            result = invoiceAPI(sign, token, "ecommerceapi", "shopuserinfoapi", recData);
        } catch (IOException e) {
            /** 异常消息 */
            Debug.log(e.getMessage());
            errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.post_autologin_request_error", locale);
            successResult.put("info", errMsg);
            successResult.put("status", false);
            return successResult;
        }
        if (null == result || "".equals(result)) {
            /** 异常消息 */
            errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.post_autologin_response_error", locale);
            successResult.put("info", errMsg);
            successResult.put("status", false);
            return successResult;
        }
        /** 解析响应数据，并同步到本地数据库 */
        // Map<String,Object> responseData = saveData(result, delegator, locale);
        Map<String, Object> responseData = saveData(result, token, delegator, locale, dctx);
        //if(null != responseData) {
        //    if (UtilValidate.isNotEmpty(responseData.get("status")) &&
        //            "false".equals(responseData.get("status"))) {
        //        successResult.put("status", responseData.get("status"));
        //        successResult.put(ModelService.RESPONSE_MESSAGE, "error");
        //        successResult.put("info", (String) responseData.get("info"));
        //    }else {
        //        successResult.put("status", responseData.get("status"));
        //        successResult.put("info", responseData.get("info"));
        //    }
        //}
        return responseData;
    }

    /**
     * 调用弘阳接口 add by wcy
     *
     * @param sign
     * @return
     * @throws IOException
     */
    public static String invoiceAPI(String sign, String token, String api, String method, String recData) throws IOException {
        /** 获取接口地址 */
        String url = UtilProperties.getPropertyValue("member.properties", api);
        /** 定义头部和参数对象 */
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, String> params = new HashMap<String, String>();
        /** 获取参数 */
        String nowDateStr = UtilDateTime.nowDateString("yyyyMMddHHmmssSSS");
        /** 设置传递参数 */
        params.put("METHOD", method);
        params.put("REQTIME", nowDateStr);
        params.put("SIGN", sign);
        params.put("TOKEN", token);

        if (UtilValidate.isNotEmpty(recData)) {
            params.put("RECDATA", recData);
        }
        /** 发送请求并获取响应数据 */
        return post(url, headers, params);
    }

    /**
     * 弘阳接口调用：通过此方法同步社区列表 add by wcy
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> communityListSync(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String sign = "12345";
        //规定默认传-1 add  by gss
        String token = "-1";
        String recData = "{" + "\"token\"" + ":" + "\"" + token + "\"}";
        String errMsg = "";
        String result = "";
        try {
            result = invoiceAPI(sign, token, "ecommerceapi", "shopcommunitylistapi", recData);
        } catch (IOException e) {
            /** 异常消息 */
            Debug.log(e.getMessage());
            errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.post_community_request_error", locale);
            successResult.put("info", errMsg);
            successResult.put("status", false);
            return successResult;
        }

        if (null == result || "".equals(result)) {
            /** 异常消息 */
            errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.post_community_response_error", locale);
            successResult.put("info", errMsg);
            successResult.put("status", false);
            return successResult;
        }
        /** 解析响应数据，并同步到本地数据库 */
        Map<String, Object> responseData = getCommunityData(dctx, result, delegator, locale);
        if (null != responseData) {
            successResult.put("status", responseData.get("status"));
            successResult.put("info", responseData.get("info"));
        }
        return successResult;
    }

    /**
     * 调整账户余额
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> adjustAmount(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        /** 获取参数 */
        String partyId = (String) context.get("partyId");
        String adjustCause = (String) context.get("adjustCause");
        BigDecimal amount = BigDecimal.ZERO;
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        if (null != userLogin) {
            String userLoginId = userLogin.getString("userLoginId");
            try {
                /** 更新账户余额 */
                amount = (BigDecimal) context.get("amount");
                GenericValue partyAccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", partyId));
                if (null != partyAccount) {
                    BigDecimal oldAmount = partyAccount.getBigDecimal("amount");
                    amount = oldAmount.add(amount);
                    partyAccount.set("amount", amount);
                    partyAccount.store();
                } else {
                    partyAccount = delegator.makeValue("PartyAccount");
                    partyAccount.set("partyId", partyId);
                    partyAccount.set("amount", amount);
                    partyAccount.set("createDate", UtilDateTime.nowTimestamp());
                    delegator.create(partyAccount);
                }

                /** 保存调整余额明细 */
                GenericValue partyAccountDetail = delegator.makeValue("PartyAccountDetail");
                String detailId = delegator.getNextSeqId("PartyAccountDetail", 1);
                partyAccountDetail.set("detailId", detailId);
                partyAccountDetail.set("partyId", partyId);
                partyAccountDetail.set("amount", (BigDecimal) context.get("amount"));
                partyAccountDetail.set("createDate", UtilDateTime.nowTimestamp());
                partyAccountDetail.set("description", adjustCause);
                partyAccountDetail.set("operator", userLoginId);

                delegator.create(partyAccountDetail);
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.adjust_amount_error", locale);
                successResult.put("status", "false");
                successResult.put("info", errMsg);
            }
            // successResult.put("status", "true");
            return successResult;
        }
        //successResult.put("status", "false");
        //successResult.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.login_timeout", locale));
        return successResult;
    }

    /**
     * 账户余额明细
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> adjustAmountHistory(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取本地化 */
        TimeZone timeZone = (TimeZone) context.get("timeZone");
        Locale locale = (Locale) context.get("locale");
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        /** 获取参数 */
        String partyId = (String) context.get("partyId");
        /** 查询余额明细列表 */
        try {
            List<String> orderBy = FastList.newInstance();
            orderBy.add("-createDate");
            List<GenericValue> partyAccountDetailList = EntityUtil.orderBy(delegator.findByAnd("PartyAccountDetail", UtilMisc.toMap("partyId", partyId)), orderBy);
            List<Map> records_list = FastList.newInstance();
            for (GenericValue partyAccountDetail : partyAccountDetailList) {
                Map map = FastMap.newInstance();
                map.put("amount", partyAccountDetail.getString("amount"));
                map.put("operator", partyAccountDetail.getString("operator"));
                map.put("createDate", UtilDateTime.timeStampToString(partyAccountDetail.getTimestamp("createDate"), "yyyy-MM-dd HH:mm:ss", timeZone, locale));
                map.put("description", partyAccountDetail.getString("description"));
                records_list.add(map);
            }
            successResult.put("partyAccountDetailList", records_list);
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            //TODO:国际化异常消息
        }
        return successResult;
    }

    /**
     * 将接口数据转换成json对象
     *
     * @param data
     * @return
     */
    public static JSONObject convertToJSONObject(String data) {
        if (UtilValidate.isNotEmpty(data)) {
            JSONObject userData = JSONObject.fromObject(data);
            return userData;
        }
        return null;
    }

    /**
     * 将接口数据转换成json数组
     *
     * @param data
     * @return
     */
    public static JSONArray convertToJSONArray(String data) {
        if (UtilValidate.isNotEmpty(data)) {
            JSONArray houses = JSONArray.fromObject(data);
            return houses;
        }
        return null;
    }


    /**
     * 将同步数据保存到对应的数据表中
     * 会员基础表,社区表,会员等级
     *
     * @param data
     * @return
     */
    public static Map<String, Object> saveData(String data, String token, Delegator delegator, Locale locale, DispatchContext dctx) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 响应数据转换成json格式 */
        JSONObject userData = convertToJSONObject(data);
        if (null != userData) {
            if (UtilValidate.isNotEmpty(userData.getString("msg"))) {
                result.put("status", "false");
                result.put("info", userData.getString("msg"));
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                return result;
            }
            /** 获取会员基础信息*/
            JSONObject dataJSON = convertToJSONObject(userData.getString("data"));
            /** 判断是否已同步,已同步则更新数据 */
            try {
                Map<String, Object> checkResult = checkSyncData(dataJSON, delegator);
                /** 存在同步数据,更新数据*/
                if ("false".equals(checkResult.get("status"))) {
                    result = updateSyncData(dataJSON, delegator, locale, result, String.valueOf(checkResult.get("info")), token, dctx);
                    return result;
                }
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.check_sync_data_exist", locale);
                result.put("status", "false");
                result.put("info", errMsg);
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            }
            /** 同步会员信息到本地数据库 */
            Map<String, Object> partyResult = partySync(delegator, locale);
            String newPartyId = "";
            if ("true".equals(String.valueOf(partyResult.get("status")))) {
                newPartyId = partyResult.get("info").toString();
            } else {
                result.put("status", "false");
                result.put("info", partyResult.get("info").toString());
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                return result;
            }

            /** 同步人员信息到本地数据库 */
            Map<String, Object> personResult = personSync(dataJSON, newPartyId, delegator, locale);
            if ("false".equals(String.valueOf(personResult.get("status")))) {
                result.put("status", "false");
                result.put("info", personResult.get("info").toString());
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                return result;
            }

            /** 同步会员积分*/
            Map<String, Object> partyScoreResult = partyScoreSync(dataJSON, newPartyId, delegator, locale);
            if ("false".equals(String.valueOf(partyScoreResult.get("status")))) {
                result.put("status", "false");
                result.put("info", partyScoreResult.get("info").toString());
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                return result;
            }
            /** 同步会员等级*/
            Map<String, Object> partyLevelResult = partyLevelSync(dataJSON, newPartyId, delegator, locale);
            if ("false".equals(String.valueOf(partyLevelResult.get("status")))) {
                result.put("status", "false");
                result.put("info", partyLevelResult.get("info").toString());
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                return result;
            }
            /** 获取社区集合 */
            if (null != dataJSON && dataJSON.size() > 0) {
                //JSONArray houses = convertToJSONArray(dataJSON.getString("houses"));
                JSONArray houses = convertToJSONArray(dataJSON.getString("item"));
                /** 同步社区到本地数据库*/
                Map<String, Object> communityResult = communitySync(houses, newPartyId, delegator, locale, dctx);
                if ("false".equals(String.valueOf(communityResult.get("status")))) {
                    result.put("status", "false");
                    result.put("info", communityResult.get("info").toString());
                    result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                    return result;
                }
            }
            /** 同步登录信息到本地数据库 */
            Map<String, Object> userLoginResult = userLoginSync(dataJSON, newPartyId, token, delegator, locale);
            if ("false".equals(String.valueOf(userLoginResult.get("status")))) {
                result.put("status", "false");
                result.put("info", userLoginResult.get("info").toString());
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                return result;
            } else {
                result.put("USERNAME", userLoginResult.get("USERNAME"));
                result.put("PASSWORD", userLoginResult.get("PASSWORD"));
            }

        }
        result.put("status", "true");
        result.put("info", UtilProperties.getMessage(resource,
                "response_success", locale));
        return result;
    }

    /**
     * 将同步数据保存到对应的数据表中
     * 社区表
     *
     * @param data
     * @return
     */
    public static Map<String, Object> getCommunityData(DispatchContext dctx, String data, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 响应数据转换成json格式 */
        JSONObject userData = convertToJSONObject(data);
        if (null != userData) {
            /** 获取会员基础信息*/
            JSONArray houses = convertToJSONArray(userData.getString("data"));

            /** 获取社区集合 */
            //if (null != dataJSON) {
            // JSONArray houses = convertToJSONArray(dataJSON.getString("communitys"));
            /** 同步社区到本地数据库*/
            Map<String, Object> communityResult = communityListSync(houses, delegator, locale);
            if ("false".equals(String.valueOf(communityResult.get("status")))) {
                result.put("status", "false");
                result.put("info", communityResult.get("info").toString());
                return result;
            }
            // }
        }
        result.put("status", "true");
        result.put("info", UtilProperties.getMessage(resource,
                "response_success", locale));
        return result;
    }

    /**
     * 判断是否已同步,已同步则更新数据
     *
     * @param delegator
     * @return
     */
    public static Map<String, Object> checkSyncData(JSONObject userData, Delegator delegator) throws GenericEntityException {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        if (null != userData) {
            /** 用户唯一标识 */
            String uid = userData.get("uid") == null ? "" : userData.getString("uid");
            List<GenericValue> personList = delegator.findByAnd("Person", UtilMisc.toMap("outId", uid));
            /** 不存在同步数据 */
            if (null == personList || personList.size() == 0) {
                result.put("status", "true");
                return result;
            }
            result.put("info", EntityUtil.getFirst(personList).get("partyId"));
            result.put("status", "false");
            return result;
        }
        result.put("status", "true");
        return result;
    }

    /**
     * 同步数据已存在，则更新
     *
     * @param userData
     * @param delegator
     * @param result
     */
    public static Map<String, Object> updateSyncData(JSONObject userData, Delegator delegator, Locale locale, Map<String, Object> result, String partyId, String token, DispatchContext dctx) {
        String errMsg = "";
        /** 获取登录信息*/
        Map<String, Object> userLoginResult = getUserLogin(partyId, delegator, locale);
        if ("false".equals(String.valueOf(userLoginResult.get("status")))) {
            result.put("status", "false");
            errMsg += userLoginResult.get("info").toString();
        } else {
            result.put("USERNAME", userLoginResult.get("USERNAME"));
            result.put("PASSWORD", userLoginResult.get("PASSWORD"));
        }
        try {
            GenericValue UserLogin = EntityUtil.getFirst(delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId)));
            if (UtilValidate.isNotEmpty(UserLogin)) {
                UserLogin.set("token", token);
                try {
                    UserLogin.store();
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        GenericValue partys = null;
        try {
            partys = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", partyId));
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        if (UtilValidate.isEmpty(partys)) {
            GenericValue party = delegator.makeValue("Party");
            String newPartyId = delegator.getNextSeqId("Party");
            party.set("partyId", newPartyId);
            party.set("partyTypeId", "PERSON");
            party.set("createdDate", UtilDateTime.nowTimestamp());
            party.set("merchants", "member");
            party.set("statusId", "PARTY_ENABLED");
            party.set("partyCategory", "MEMBER");
            try {
                party.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
        }

        /** 同步人员信息到本地数据库 */
        Map<String, Object> personResult = updatePersonSync(userData, partyId, delegator, locale);
        if ("false".equals(String.valueOf(personResult.get("status")))) {
            result.put("status", "false");
            errMsg += personResult.get("info").toString();
        }

        /** 同步会员积分*/
        Map<String, Object> partyScoreResult = updatePartyScoreSync(userData, partyId, delegator, locale);
        if ("false".equals(String.valueOf(partyScoreResult.get("status")))) {
            result.put("status", "false");
            errMsg += personResult.get("info").toString() + "\n";
        }

        /** 同步会员等级*/
        Map<String, Object> partyLevelResult = updatePartyLevelSync(userData, partyId, delegator, locale);
        if ("false".equals(String.valueOf(partyLevelResult.get("status")))) {
            result.put("status", "false");
            errMsg += personResult.get("info").toString() + "\n";
        }
        /** 获取社区集合 */
        if (null != userData) {
            //JSONArray houses = convertToJSONArray(userData.getString("houses"));
            JSONArray houses = convertToJSONArray(userData.getString("item"));
            /** 同步社区到本地数据库*/
            Map<String, Object> communityResult = updateCommunitySync(houses, partyId, delegator, locale, dctx);
            if ("false".equals(String.valueOf(communityResult.get("status")))) {
                result.put("status", "false");
                errMsg += personResult.get("info").toString() + "\n";
            }
        }
        if (UtilValidate.isNotEmpty(errMsg)) {
            result.put("status", "false");
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
        } else {
            result.put("status", "true");
        }
        result.put("info", errMsg);
        return result;
    }

    /**
     * 获取登录信息
     *
     * @param partyId
     * @param delegator
     * @param locale
     * @return
     */
    public static Map<String, Object> getUserLogin(String partyId, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();

        try {
            String defaultPassword = UtilProperties.getPropertyValue("member.properties", "defaultPassword");
            GenericValue userLogin = EntityUtil.getFirst(delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId)));
            if (null != userLogin) {
                result.put("USERNAME", userLogin.get("userLoginId"));
                result.put("PASSWORD", defaultPassword);
                //result.put("PASSWORD",userLogin.get("currentPassword"));

            }
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            String errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.user_login_findby_error", locale);
            result.put("status", "false");
            result.put("info", errMsg);
            return result;
        }
        result.put("status", "true");
        return result;
    }

    /**
     * 同步会员信息到本地数据库
     *
     * @param delegator
     * @return
     */
    public static Map<String, Object> partySync(Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();

        /** 创建会员信息 */
        GenericValue party = delegator.makeValue("Party");
        String newPartyId = delegator.getNextSeqId("Party");
        party.set("partyId", newPartyId);
        party.set("partyTypeId", "PERSON");
        party.set("createdDate", UtilDateTime.nowTimestamp());
        party.set("merchants", "member");
        party.set("statusId", "PARTY_ENABLED");
        party.set("partyCategory", "MEMBER");

        try {
            party.create();
            result.put("status", "true");
            result.put("info", newPartyId);
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
            String errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.party_create_error", locale);
            result.put("status", "false");
            result.put("info", errMsg);
        }
        return result;
    }

    /**
     * 同步登录信息到本地数据库
     *
     * @param userData
     * @param newPartyId
     * @param delegator
     * @param locale
     * @return
     */
    public static Map<String, Object> userLoginSync(JSONObject userData, String newPartyId, String token, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 判断登录账号是否已经存在 */
        if (null != userData) {
            String phone = userData.get("phone") == null ? "" : userData.getString("phone");
            GenericValue userLogin = null;
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", phone));
                /** 创建登录信息 */
                if (null == userLogin) {
                    String defaultPassword = UtilProperties.getPropertyValue("member.properties", "defaultPassword");
                    boolean useEncryption = "true".equals(UtilProperties.getPropertyValue("security.properties", "password.encrypt"));
                    userLogin = delegator.makeValue("UserLogin");
                    userLogin.set("userLoginId", phone);
                    userLogin.set("partyId", newPartyId);
                    userLogin.set("enabled", "Y");
                    // 同步token
                    userLogin.set("token", token);
                    userLogin.set("currentPassword", useEncryption ? HashCrypt.cryptPassword(getHashType(), defaultPassword) : defaultPassword);
                    delegator.create(userLogin);
                    result.put("USERNAME", phone);
                    result.put("PASSWORD", defaultPassword);

                    /** 用户访问系统的权限设置 */
                    GenericValue userLoginSecurityGroup = delegator.makeValue("UserLoginSecurityGroup");
                    userLoginSecurityGroup.set("userLoginId", phone);
                    userLoginSecurityGroup.set("groupId", "FULLADMIN");
                    userLoginSecurityGroup.set("fromDate", UtilDateTime.nowTimestamp());
                    delegator.create(userLoginSecurityGroup);
                }
                result.put("status", "true");
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.userLogin_create_error", locale);
                result.put("status", "false");
                result.put("info", errMsg);
            }

        }
        result.put("status", "true");
        return result;
    }


    /**
     * 同步人员信息到本地数据库
     *
     * @param userData
     * @param delegator
     * @return
     */
    public static Map<String, Object> personSync(JSONObject userData, String newPartyId, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 定义字段 */
        if (null != userData) {
            String uid = userData.get("uid") == null ? "" : userData.getString("uid");
            String name = userData.get("name") == null ? "" : userData.getString("name");
            String phone = userData.get("phone") == null ? "" : userData.getString("phone");
            String nickname = userData.get("nickname") == null ? "" : userData.getString("nickname");
            String sex = userData.get("sex") == null ? "" : userData.getString("sex");
            String headphoto = userData.get("pic_name") == null ? "" : userData.getString("pic_name");
            String url = "http://img.hongshenghuo.net/";

            /** 创建人员信息 */
            GenericValue person = delegator.makeValue("Person");
            person.set("partyId", newPartyId);
            //person.set("name",name);
            String str = null;
            try {
                str = new String(nickname.getBytes("ISO8859-1"), "utf-8");


            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            person.set("name", nickname);
            person.set("nickname", nickname);
            person.set("mobile", phone);
            person.set("gender", sex);
            person.set("outId", uid);
            if (UtilValidate.isNotEmpty(headphoto)) {
                person.set("headphoto", url + headphoto);
            } else {
                person.set("headphoto", "");
            }
            try {
                person.create();
                result.put("status", "true");
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.person_create_error", locale);
                result.put("info", errMsg);
                result.put("status", "false");

            }
            return result;
        }
        ///** 异常消息返回 */
        //result.put("status","false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.person_sync_lack_of_data", locale));

        result.put("status", "true");
        return result;
    }

    /**
     * 同步人员信息到本地数据库
     *
     * @param userData
     * @param delegator
     * @return
     */
    public static Map<String, Object> updatePersonSync(JSONObject userData, String newPartyId, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 定义字段 */
        if (null != userData) {
            String uid = userData.get("uid") == null ? "" : userData.getString("uid");
            String phone = userData.get("phone") == null ? "" : userData.getString("phone");
            String nickname = userData.get("nickname") == null ? "" : userData.getString("nickname");
            String sex = userData.get("sex") == null ? "" : userData.getString("sex");
            //头像信息
            String headphoto = userData.get("pic_name") == null ? "" : userData.getString("pic_name");
            String url = "http://img.hongshenghuo.net/";
            try {
                /** 更新人员信息 */
                GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", newPartyId));
                person.set("nickname", nickname);
                person.set("mobile", phone);
                person.set("gender", sex);
                person.set("outId", uid);
                if (UtilValidate.isNotEmpty(headphoto)) {
                    person.set("headphoto", url + headphoto);
                } else {
                    person.set("headphoto", "");
                }
                person.store();
                result.put("status", "true");
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.person_update_error", locale);
                result.put("info", errMsg);
                result.put("status", "false");

            }
            return result;
        }
        ///** 异常消息返回 */
        //result.put("status","false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.person_sync_lack_of_data", locale));

        result.put("status", "true");
        return result;
    }

    /**
     * 同步社区信息到本地数据库
     *
     * @param houses
     * @param newPartyId
     * @param delegator
     * @return
     */
    public static Map<String, Object> communitySync(JSONArray houses, String newPartyId, Delegator delegator, Locale locale, DispatchContext dctx) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        if (houses != null && houses.size() > 0) {
            for (int i = 0; i < houses.size(); i++) {
                /** 获取json对象 */
                JSONObject jsonObject = houses.getJSONObject(i);
                /** 创建社区 */
                //GenericValue community = delegator.makeValue("Community");
                /** 主键ID */
                //String communityId = delegator.getNextSeqId("Community", 1);
                String communityId = jsonObject.get("communityid") == null ? delegator.getNextSeqId("Community", 1) : jsonObject.getString("communityid");
                String code = jsonObject.get("communitycode") == null ? "" : jsonObject.getString("communitycode");
                //String name = jsonObject.get("communityname") == null ? "" : jsonObject.getString("communityname");
                GenericValue oldCommunity = null;
                try {
                    oldCommunity = delegator.findByPrimaryKey("Community", UtilMisc.toMap("communityId", communityId));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
                if (null == oldCommunity) {
                    /*如果社区ID为空,重新同步一次社区信息*/
                    try {
                        Map<String, Object> communityResult = dispatcher.runSync("communitySync", null);
                    } catch (GenericServiceException e1) {
                        e1.printStackTrace();
                    }
                }
                    /*community.set("communityId", communityId);
                    community.set("code", code);
                    community.set("name", name);*/
                /** 创建会员社区对应关系 */
                List<GenericValue> partyCommunityList;
                try {
                    partyCommunityList = delegator.findByAnd("PartyCommunity", UtilMisc.toMap("partyId", newPartyId, "communityId", communityId));
                    if (UtilValidate.isEmpty(partyCommunityList)) {
                        GenericValue partyCommunity = delegator.makeValue("PartyCommunity");
                        partyCommunity.set("partyId", newPartyId);
                        partyCommunity.set("communityId", communityId);
                        try {
                            partyCommunity.create();
                            result.put("status", "true");
                        } catch (GenericEntityException e) {
                            Debug.log(e.getMessage());
                            String errMsg = UtilProperties.getMessage(resourceError,
                                    "memberservices.party_community_create_error", locale);
                            result.put("info", errMsg);
                            result.put("status", "false");
                            return result;
                        }
                    }
                } catch (GenericEntityException e1) {
                    e1.printStackTrace();
                }
               
               /* else{
                    oldCommunity.set("code", code);
                    oldCommunity.set("name", name);
                    try {
                        oldCommunity.store();
                    } catch (GenericEntityException e) {
                        Debug.logError(e,module);
                    }
                }*/

            }
        }
        ///** 异常消息返回 */
        //result.put("status", "false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.party_community_sync_lack_of_data", locale));
        result.put("status", "true");
        return result;
    }


    /**
     * 同步社区信息到本地数据库
     *
     * @param houses
     * @param delegator
     * @return
     */
    public static Map<String, Object> communityListSync(JSONArray houses, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        if (houses != null && houses.size() > 0) {
            for (int i = 0; i < houses.size(); i++) {
                /** 获取json对象 */
                JSONObject jsonObject = houses.getJSONObject(i);
                String id = jsonObject.get("communityid") == null ? delegator.getNextSeqId("Community", 1) : jsonObject.getString("communityid");
                String code = jsonObject.get("communitycode") == null ? "" : jsonObject.getString("communitycode");
                String name = jsonObject.get("communityname") == null ? "" : jsonObject.getString("communityname");
                GenericValue community = null;
                try {
                    community = EntityUtil.getFirst(delegator.findByAnd("Community", UtilMisc.toMap("communityId", id)));

                    if (community == null) {
                        /** 创建社区 */
                        community = delegator.makeValue("Community");
                        /** 主键ID */
                        //String communityId = delegator.getNextSeqId("Community", 1);
                        String communityId = id;
                        community.set("communityId", communityId);
                        community.set("code", code);
                        community.set("name", name);
                        community.create();
                    } else {
                        community.set("name", name);
                        community.store();
                    }
                    result.put("status", "true");
                } catch (GenericEntityException e) {
                    Debug.log(e.getMessage());
                    String errMsg = UtilProperties.getMessage(resourceError,
                            "memberservices.party_community_create_error", locale);
                    result.put("info", errMsg);
                    result.put("status", "false");
                    return result;
                }
            }
        }
        ///** 异常消息返回 */
        //result.put("status", "false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.party_community_sync_lack_of_data", locale));
        result.put("status", "true");
        return result;
    }

    /**
     * 同步社区信息到本地数据库
     *
     * @param houses
     * @param newPartyId
     * @param delegator
     * @return
     */
    public static Map<String, Object> updateCommunitySync(JSONArray houses, String newPartyId, Delegator delegator, Locale locale, DispatchContext dctx) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 更新会员社区对应关系 */
        List<GenericValue> partyCommunityList = null;
        try {
            partyCommunityList = delegator.findByAnd("PartyCommunity", UtilMisc.toMap("partyId", newPartyId));
            if (UtilValidate.isNotEmpty(partyCommunityList)) {
                delegator.removeAll(partyCommunityList);
            }
        } catch (GenericEntityException e2) {
            e2.printStackTrace();
        }
        if (houses != null && houses.size() > 0) {
            for (int i = 0; i < houses.size(); i++) {
                /** 获取json对象 */
                JSONObject jsonObject = houses.getJSONObject(i);
                String id = jsonObject.get("communityid") == null ? delegator.getNextSeqId("Community", 1) : jsonObject.getString("communityid");
                String code = jsonObject.get("communitycode") == null ? "" : jsonObject.getString("communitycode");
                //String name = jsonObject.get("communityname") == null ? "" : jsonObject.getString("communityname");
                /** 创建社区 */
                GenericValue oldCommunity = null;
                try {
                    oldCommunity = delegator.findByPrimaryKey("Community", UtilMisc.toMap("communityId", id));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
                if (null == oldCommunity) {
                    /*如果社区ID为空,重新同步一次社区信息*/
                    try {
                        Map<String, Object> communityResult = dispatcher.runSync("communitySync", null);
                    } catch (GenericServiceException e1) {
                        e1.printStackTrace();
                    }
                }
                try {
                    GenericValue newPartyCommunity = delegator.makeValue("PartyCommunity");
                    newPartyCommunity.set("partyId", newPartyId);
                    newPartyCommunity.set("communityId", id);
                    newPartyCommunity.create();
                  /*  for(GenericValue partyCommunity : partyCommunityList){
                        
                        GenericValue community = EntityUtil.getFirst(delegator.findByAnd("Community", UtilMisc.toMap("communityId", partyCommunity.get("communityId"))));//, "code",code
                        if(null == community) {
                            //创建会员社对应关系
                           community = delegator.makeValue("Community");
                           community.set("communityId",id);
                           community.set("code",code);
                           //community.set("name",name);
                           community.create();
                           GenericValue newPartyCommunity = delegator.makeValue("PartyCommunity");
                           newPartyCommunity.set("partyId",newPartyCommunity);
                           newPartyCommunity.set("communityId",id);
                           newPartyCommunity.create();
                           continue;
                        }
                        community.set("name", name);
                        community.store();
                    }*/
                    result.put("status", "true");
                } catch (GenericEntityException e) {
                    Debug.log(e.getMessage());
                    String errMsg = UtilProperties.getMessage(resourceError,
                            "memberservices.party_community_update_error", locale);
                    result.put("info", errMsg);
                    result.put("status", "false");
                    return result;
                }

            }
        }
        ///** 异常消息返回 */
        //result.put("status", "false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.party_community_sync_lack_of_data", locale));
        result.put("status", "true");
        return result;
    }

    /**
     * 同步会员积分到本地数据库
     *
     * @param userData
     * @param newPartyId
     * @param delegator
     * @return
     */
    public static Map<String, Object> partyScoreSync(JSONObject userData, String newPartyId, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        if (null != userData) {
            try {
                /** 会员积分*/
                long score = userData.get("integralnum") == null ? 0L : userData.getLong("integralnum");
                /** 创建会员积分记录 */
                GenericValue partyScore = delegator.makeValue("PartyScore");
                partyScore.set("partyId", newPartyId);
                partyScore.set("scoreValue", score);
                partyScore.create();
                result.put("status", "true");
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.partyscore_create_error", locale);
                result.put("info", errMsg);
                result.put("status", "false");

            }
            return result;
        }
        ///** 异常消息返回 */
        //result.put("status", "false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.partyscore_sync_lack_of_data", locale));
        result.put("status", "true");
        return result;
    }

    /**
     * 同步会员积分到本地数据库
     *
     * @param userData
     * @param newPartyId
     * @param delegator
     * @return
     */
    public static Map<String, Object> updatePartyScoreSync(JSONObject userData, String newPartyId, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        if (null != userData) {
            try {
                /** 会员积分*/
                long score = userData.get("integralnum") == null ? 0L : userData.getLong("integralnum");
                /** 创建会员积分记录 */
                GenericValue partyScore = delegator.findByPrimaryKey("PartyScore", UtilMisc.toMap("partyId", newPartyId));
                if (UtilValidate.isEmpty(partyScore)) {
                    /** 创建会员等级记录*/
                    GenericValue partyScores = delegator.makeValue("PartyScore");
                    partyScores.set("partyId", newPartyId);
                    partyScores.set("scoreValue", score);
                    partyScores.create();
                    result.put("status", "true");
                } else {
                    partyScore.set("scoreValue", score);
                    partyScore.store();
                    result.put("status", "true");
                }

            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.partyscore_update_error", locale);
                result.put("info", errMsg);
                result.put("status", "false");

            }
            return result;
        }
        ///** 异常消息返回 */
        //result.put("status", "false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.partyscore_sync_lack_of_data", locale));
        result.put("status", "true");
        return result;
    }

    /**
     * 同步会员等级到本地数据库
     *
     * @param userData
     * @param newPartyId
     * @param delegator
     * @return
     */
    public static Map<String, Object> partyLevelSync(JSONObject userData, String newPartyId, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        if (null != userData) {
            /** 会员等级 */
            String currentlevel = userData.get("currentlevel") == null ? "" : userData.getString("currentlevel");

            try {
             /*   *//** 会员等级类型表 *//*
                GenericValue partyLevelType = delegator.findByPrimaryKey("PartyLevelType", UtilMisc.toMap("levelId", currentlevel));
                if(null == partyLevelType){
                    partyLevelType = delegator.makeValue("PartyLevelType");
                    partyLevelType.set("levelId",currentlevel);
                    partyLevelType.set("levelName",currentlevel);
                    partyLevelType.set("levelCode",currentlevel);
                    partyLevelType.create();
                }*/
                /** 创建会员等级记录*/
                GenericValue partyLevel = delegator.makeValue("PartyLevel");
                partyLevel.set("partyId", newPartyId);
                partyLevel.set("levelId", currentlevel);
                partyLevel.set("levelName", currentlevel);
                partyLevel.create();
                result.put("status", "true");
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.partylevel_create_error", locale);
                result.put("info", errMsg);
                result.put("status", "false");
            }

            return result;
        }
        ///** 异常消息返回 */
        //result.put("status", "false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.partylevel_sync_lack_of_data", locale));
        result.put("status", "true");
        return result;
    }

    /**
     * 同步会员等级到本地数据库
     *
     * @param userData
     * @param newPartyId
     * @param delegator
     * @return
     */
    public static Map<String, Object> updatePartyLevelSync(JSONObject userData, String newPartyId, Delegator delegator, Locale locale) {
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        if (null != userData) {
            /** 会员等级 */
            String currentlevel = userData.get("currentlevel") == null ? "" : userData.getString("currentlevel");
            /** 更新会员等级记录*/
            try {
                GenericValue partyLevel = delegator.findByPrimaryKey("PartyLevel", UtilMisc.toMap("partyId", newPartyId));
                if (UtilValidate.isEmpty(partyLevel)) {
                    GenericValue Party_Level = delegator.makeValue("PartyLevel");
                    Party_Level.set("partyId", newPartyId);
                    Party_Level.set("levelId", currentlevel);
                    Party_Level.set("levelName", currentlevel);
                    Party_Level.create();
                    GenericValue Party_Level_his = delegator.makeValue("PartyLevelHis");
                    Party_Level_his.set("partyId", newPartyId);
                    Party_Level_his.set("levelId", currentlevel);
                    Party_Level_his.set("levelName", currentlevel);
                    Party_Level_his.set("startDate", UtilDateTime.nowTimestamp());
                    Party_Level_his.create();
                } else {
                    if (!partyLevel.get("levelId").equals(currentlevel)) {
                        partyLevel.remove();
                        GenericValue Party_Level = delegator
                                .makeValue("PartyLevel");
                        Party_Level.set("partyId", newPartyId);
                        Party_Level.set("levelId", currentlevel);
                        Party_Level.set("levelName", currentlevel);
                        Party_Level.create();
                        GenericValue Party_Level_his = delegator
                                .makeValue("PartyLevelHis");
                        Party_Level_his.set("partyId", newPartyId);
                        Party_Level_his.set("levelId", currentlevel);
                        Party_Level_his.set("levelName", currentlevel);
                        Party_Level_his.set("startDate", UtilDateTime.nowTimestamp());
                        Party_Level_his.create();
                    }
                }
                result.put("status", "true");
            } catch (GenericEntityException e) {
                Debug.log(e.getMessage());
                String errMsg = UtilProperties.getMessage(resourceError,
                        "memberservices.partylevel_update_error", locale);
                result.put("info", errMsg);
                result.put("status", "false");
            }
            return result;
        }
        ///** 异常消息返回 */
        //result.put("status", "false");
        //result.put("info", UtilProperties.getMessage(resourceError,
        //        "memberservices.partylevel_sync_lack_of_data", locale));
        result.put("status", "true");
        return result;
    }

    /**
     * 查询会员信息:查询条件(昵称,手机号,社区)
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findmember(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));


        List<Map> memberList = FastList.newInstance();
        int memberListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PT", "Party");
        dynamicView.addAlias("PT", "partyId");
        dynamicView.addAlias("PT", "partyTypeId");
        dynamicView.addAlias("PT", "statusId");
        dynamicView.addAlias("PT", "partyCategory");

        dynamicView.addMemberEntity("PS", "Person");
        dynamicView.addAlias("PS", "nickname");
        dynamicView.addAlias("PS", "gender");
        dynamicView.addAlias("PS", "name");
        dynamicView.addAlias("PS", "mobile");
        dynamicView.addAlias("PS", "outId");

        dynamicView.addViewLink("PT", "PS", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("outId");
        fieldsToSelect.add("nickname");
        fieldsToSelect.add("gender");
        fieldsToSelect.add("mobile");
        fieldsToSelect.add("name");

        String sortField = "partyId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        //根据昵称模糊查询
        if (UtilValidate.isNotEmpty(context.get("nickname"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("nickname"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("nickname") + "%")));
        }

        //根据电话号码模糊查询
        if (UtilValidate.isNotEmpty(context.get("telphone"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("mobile"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("telphone") + "%")));
        }

        //根据社区名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("communtiy"))) {
            dynamicView.addMemberEntity("CT", "Community");
            dynamicView.addAlias("CT", "communityId");
            dynamicView.addAlias("CT", "communtiyName", "name", null, null, null, null);

            dynamicView.addMemberEntity("PC", "PartyCommunity");
            dynamicView.addViewLink("PS", "PC", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));
            dynamicView.addViewLink("PC", "CT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("communityId"));

            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("communtiyName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("communtiy") + "%")));
        }

        andExprs.add(EntityCondition.makeCondition("partyTypeId", EntityOperator.EQUALS, "PERSON"));
        // andExprs.add(EntityCondition.makeCondition("outId", EntityOperator.NOT_EQUAL, null));
        andExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PARTY_ENABLED"));
        andExprs.add(EntityCondition.makeCondition("partyCategory", EntityOperator.EQUALS, "MEMBER"));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

            //遍历查询结果集
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map record = FastMap.newInstance();
                String partyId = gv.getString("partyId");
                String nickname = gv.getString("nickname");
                String name = gv.getString("name");
                String gender = gv.getString("gender");
                String mobile = gv.getString("mobile");

                record.put("partyId", partyId);
                record.put("nickname", nickname);
                record.put("name", name);
                record.put("gender", gender);
                record.put("mobile", mobile);

                //会员等级
                DynamicViewEntity pl_dv = new DynamicViewEntity();
                pl_dv.addMemberEntity("PL", "PartyLevel");
                pl_dv.addAlias("PL", "partyId");

                pl_dv.addMemberEntity("PLT", "PartyLevelType");
                pl_dv.addAlias("PLT", "levelName");
                pl_dv.addViewLink("PL", "PLT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("levelId", "levelId"));
                EntityListIterator pl_pli = delegator.findListIteratorByCondition(pl_dv, EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId), null, null, null, null);
                String partyLevel = "";
                for (GenericValue pl_gv : pl_pli.getCompleteList()) {
                    partyLevel = pl_gv.getString("levelName");
                }
                pl_pli.close();
                record.put("partyLevel", partyLevel);

                //会员的社区
                DynamicViewEntity pc_dv = new DynamicViewEntity();
                pc_dv.addMemberEntity("PC", "PartyCommunity");
                pc_dv.addAlias("PC", "partyId");

                pc_dv.addMemberEntity("C", "Community");
                pc_dv.addAlias("C", "name");
                pc_dv.addViewLink("PC", "C", Boolean.FALSE, ModelKeyMap.makeKeyMapList("communityId", "communityId"));
                EntityListIterator pc_pli = delegator.findListIteratorByCondition(pc_dv, EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId), null, null, null, null);
                String communtity = "";
                //将多个社区名称编辑从字符串，用中文、分隔，如：AA、BB、CC
                for (int i = 0; i < pc_pli.getCompleteList().size(); i++) {
                    communtity += pc_pli.getCompleteList().get(i).getString("name");
                    if (i != pc_pli.getCompleteList().size() - 1) {
                        communtity += "、";
                    }
                }
                pc_pli.close();
                record.put("communtity", communtity);
                //会员的账号余额
                String amount = "0";
                GenericValue pa_gv = EntityUtil.getFirst(delegator.findByAnd("PartyAccount", UtilMisc.toMap("partyId", partyId)));
                if (UtilValidate.isNotEmpty(pa_gv)) {
                    amount = pa_gv.getString("amount");
                }
                record.put("amount", amount);
                memberList.add(record);
            }
            //总记录数
            memberListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > memberListSize) {
                highIndex = memberListSize;
            }

            //关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        result.put("recordsList", memberList);
        result.put("totalSize", Integer.valueOf(memberListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));

        return result;
    }

    /**
     * 获取用户个人信息
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> userInfo(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");
        /** 定义查询字段 */
        List<String> fieldsToSelect = FastList.newInstance();

        /** 定义实体动态视图:登录信息 */
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("UL", "UserLogin");
        dynamicView.addAlias("UL", "partyId");
        dynamicView.addAlias("UL", "userLoginId");

        /** 定义实体动态视图：会员信息 */
        dynamicView.addMemberEntity("PT", "Party");
        dynamicView.addAlias("PT", "partyId");
        dynamicView.addAlias("PT", "merchants");//商家

        ///** 定义实体动态视图：会员账号 */
        //dynamicView.addMemberEntity("PA","PartyAccount");
        //dynamicView.addAlias("PA","partyId");
        //dynamicView.addAlias("PA","amount");
        //
        /** 建立表的关联关系 */
        dynamicView.addViewLink("UL", "PT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));
        //dynamicView.addViewLink("PT", "PA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId"));
        //
        /** 查询字段 */
        fieldsToSelect.add("merchants");
        fieldsToSelect.add("partyId");
        //fieldsToSelect.add("amount");
        /** 定义查询条件集合 */
        EntityCondition mainCond = EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("userLoginId"), EntityOperator.EQUALS, userLoginId)
                )
                , EntityOperator.AND);

        /** 获取结果集 */
        List<GenericValue> userInfoList = null;
        Map<String, Object> resultData = FastMap.newInstance();
        try {
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, true);
            //填充查询条件,查询字段，排序字段
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, findOpts);
            //获取结果集
            userInfoList = pli.getCompleteList();
        } catch (GenericEntityException e) {
            Debug.log(e.getMessage());
        }

        /** 响应结果集 */
        if (null != userInfoList && userInfoList.size() > 0) {
            GenericValue userInfo = EntityUtil.getFirst(userInfoList);
            // String userRole = UtilValidate.isEmpty(userInfo.getString("merchants")) ? "member":"merchant";
            GenericValue partyAccount = null;
            try {
                partyAccount = delegator.findByPrimaryKey("PartyAccount", UtilMisc.toMap("partyId", userInfo.getString("partyId")));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
            if (null != partyAccount) {
                resultData.put("balance", partyAccount.getBigDecimal("amount") == null ? 0 : partyAccount.getBigDecimal("amount"));
            } else {
                resultData.put("balance", 0);
            }
            resultData.put("userLoginId", userLoginId);
            // resultData.put("userRole", userRole);

        } else {
            resultData.put("userLoginId", userLoginId);
            // resultData.put("userRole", "");
            resultData.put("balance", 0);
        }
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            result.put("resultData", resultData);
            return result;
        }
        if (null == userLogin) {
            result.put("resultData", resultData);
            return result;
        }
        // 判断当前用户是不是商家--审核中 ---会员 add by gss
        GenericValue party_relationship;
        try {
            party_relationship = EntityUtil.getFirst(delegator.findByAnd(
                    "PartyRelationship", UtilMisc.toMap("partyIdTo",
                            userLogin.get("partyId"), "roleTypeIdFrom",
                            "SUPPLIER")));
            if (UtilValidate.isNotEmpty(party_relationship)) {

                GenericValue partyBusiness = delegator.findByPrimaryKey(
                        "PartyBusiness",
                        UtilMisc.toMap("partyId",
                                party_relationship.get("partyIdFrom")));
                if (UtilValidate.isNotEmpty(partyBusiness)) {
                    //0 待审核1 商家 2 拒绝
                    if ("0".equals(partyBusiness.get("auditStatus"))) {
                        resultData.put("userRole", "pending");
                    } else if ("1".equals(partyBusiness.get("auditStatus"))) {
                        resultData.put("userRole", "merchant");
                    } else if ("2".equals(partyBusiness.get("auditStatus"))) {
                        resultData.put("userRole", "member");
                    }

                } else {
                    resultData.put("userRole", "member");
                }

            } else {
                resultData.put("userRole", "member");
            }
        } catch (GenericEntityException e2) {
            e2.printStackTrace();
        }

        //获取用户各个订单状态下的数量
        try {
            Map<String, Object> userOrderResult = dispatcher.runSync("userOrderNum", context);
            if (null != userOrderResult) {
                //未使用数量
                resultData.put("notUsedNum", userOrderResult.get("notUsedNum"));
                resultData.put("notReceivedNum", userOrderResult.get("notReceivedNum"));
                resultData.put("notReviewedNum", userOrderResult.get("notReviewedNum"));
            } else {
                resultData.put("notUsedNum", 0L);
                resultData.put("notReceivedNum", 0L);
                resultData.put("notReviewedNum", 0L);
            }
        } catch (GenericServiceException e) {
            Debug.log(e.getMessage());
        }
        result.put("resultData", resultData);
        return result;
    }

    /**
     * 用户订单信息
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> userOrder(DispatchContext dctx, Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 响应结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 获取参数 */
        String userLoginId = (String) context.get("userLoginId");

        return result;
    }

    /**
     * 用户积分变动 add by wcy 2016.01.21
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> partyScoreChange(DispatchContext dctx, Map<String, ? extends Object> context) {
        /**获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 响应结果 */
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        /** 获取参数 */

        String sign = "12345";
        String token = (String) context.get("token");
        String changedir = (String) context.get("changedir");
        Integer point = (Integer) context.get("point");
        String vouchercode = (String) context.get("vouchercode");
        String type = (String) context.get("type");
       /* String sign = (String) context.get("sign");
        String vouchercode = (String) context.get("vouchercode");
        String token = "0b08f972559a4b919327b8e0d7d6be9e14577549918081";
        String changedir = "plus"; //minus
        int point=1;
        String type = "1";//;1 购物奖励   2购物消耗  3购物退货    */
        String recData = "{" + "\"changedir\"" + ":" + "\"" + changedir + "\"" + ",\"point\"" + ":" + point + ",\"vouchercode\"" + ":" + "\"" + vouchercode + "\"" + ",\"type\"" + ":" + "\"" + type + "\"" + "}";
        String result = "";
        String errMsg = "";
        try {
            /** 发送请求并获取响应数据 */
            result = invoiceAPI(sign, token, "ecommerceapi", "shoppointchangeapi", recData);
        } catch (IOException e) {
            /** 异常消息 */
            Debug.log(e.getMessage());
            errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.post_scorechange_request_error", locale);
            successResult.put("info", errMsg);
            successResult.put("status", false);
            return successResult;
        }
        if (null == result || "".equals(result)) {
            /** 异常消息 */
            errMsg = UtilProperties.getMessage(resourceError,
                    "memberservices.post_scorechange_response_error", locale);
            successResult.put("info", errMsg);
            successResult.put("status", false);
            return successResult;
        }
        /** 响应数据转换成json格式 */
        JSONObject userData = convertToJSONObject(result);
        JSONObject dataJSON = convertToJSONObject(userData.getString("data"));
        if (UtilValidate.isEmpty(dataJSON)) {
            successResult.put("status", false);
            successResult.put("info", userData.getString("msg"));
        } else {
            if (UtilValidate.isNotEmpty(dataJSON.getString("result")) && "1".equals(dataJSON.getString("result"))) {
                successResult.put("status", true);
            } else {
                successResult.put("status", false);
                successResult.put("info", dataJSON.getString("desc") == null ? UtilProperties.getMessage(resourceError,
                        "memberservices.post_scorechange_response_error", locale) : dataJSON.getString("desc"));
            }
        }

        return successResult;

    }


    /**
     * 公共POST请求
     *
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        /** 定义http请求 */
        HttpClient client = new DefaultHttpClient();
        /** post请求地址设置 */
        HttpPost post = new HttpPost(url);
        /** 发送内容设置 */
        post.setEntity(new UrlEncodedFormEntity(parseParamToNameValuePair(params), Charset.forName("UTF-8")));
        return getContentFromResponse(client.execute(post));
    }

    /**
     * 解析Map传递的参数，使用一个键值对对象BasicNameValuePair保存
     *
     * @param map
     * @return
     */
    public static List<NameValuePair> parseParamToNameValuePair(Map<String, String> map) {
        /** 定义集合 */
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        /** 遍历Map参数 */
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                /** 解析Map传递的参数，使用一个键值对对象BasicNameValuePair保存 */
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        return list;
    }

    /**
     * 获取响应消息
     *
     * @param response
     * @return
     * @throws IOException
     */
    public static String getContentFromResponse(HttpResponse response) throws IOException {
        /** 判断是否请求成功，为200时表示成功，其他均问有问题。 */
        if (response.getStatusLine().getStatusCode() == 200) {
            /**  通过HttpEntity获得响应流 */
            InputStream inputStream = response.getEntity().getContent();
            return changeInputStream(inputStream, "utf-8");
        }
        return "";
    }

    /**
     * 通过消息流转换成字符串消息
     *
     * @param inputStream
     * @param encode
     * @return
     */
    public static String changeInputStream(InputStream inputStream, String encode) {
        /** 定义字节输出流 */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        String result = "";
        if (inputStream != null) {
            try {
                /** 消息流转换字符串 */
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                result = new String(outputStream.toByteArray(), encode);
            } catch (IOException e) {
                Debug.log(e.getMessage());
                return "";
            }
        }
        return result;
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
     * 选择地区页面接口
     * add by gss
     *
     * @param dctx
     * @param context
     */
    @SuppressWarnings({"rawtypes", "unchecked", "null"})
    public static Map<String, Object> getAllStateList(DispatchContext dctx,
                                                      Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 返回结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 获取参数  省Id*/
        String provinceId = (String) context.get("provinceId");
        /** 获取参数  市Id*/
        String cityId = (String) context.get("cityId");
        /** 默认国家*/
        String country = null;
        if (UtilValidate.isEmpty(provinceId) && UtilValidate.isEmpty(cityId)) {
            /**加载系统默认国家*/
            country = EntityUtilProperties.getPropertyValue("general.properties", "country.geo.id.default", delegator);
        }
        List<GenericValue> geoList = FastList.newInstance();
        /** 默认排序*/
        String listOrderBy = "geoId";
        List<String> sortList = UtilMisc.toList(listOrderBy);

        try {
            /** 创建查询条件*/
            EntityCondition stateProvinceFindCond = null;
            /** 如果省市Id 都为空  查询国家下所有省*/
            if (UtilValidate.isEmpty(provinceId) && UtilValidate.isEmpty(cityId)) {
                stateProvinceFindCond = EntityCondition.makeCondition(
                        EntityCondition.makeCondition("geoIdFrom", country),
                        EntityCondition.makeCondition("geoAssocTypeId", "REGIONS"),
                        EntityCondition.makeCondition(EntityOperator.AND,
                                EntityCondition.makeCondition("geoTypeId", "PROVINCE")
                        ));
                geoList.addAll(delegator.findList("GeoAssocAndGeoToWithState", stateProvinceFindCond, null, sortList, null, true));
            }
            /** 如果省Id不为空 ,市Id为空  查询省下所有市*/
            else if (UtilValidate.isNotEmpty(provinceId) && UtilValidate.isEmpty(cityId))//
            {
                stateProvinceFindCond = EntityCondition.makeCondition(
                        EntityCondition.makeCondition("geoIdFrom", provinceId),
                        EntityCondition.makeCondition("geoAssocTypeId", "REGIONS"),
                        EntityCondition.makeCondition(EntityOperator.AND,
                                EntityCondition.makeCondition("geoTypeId", "CITY")
                        ));
                geoList.addAll(delegator.findList("GeoAssocAndGeoToWithState", stateProvinceFindCond, null, sortList, null, true));
            }
            /** 如果省Id不为空 ,市Id不为空  查询市下所区*/
            else if (UtilValidate.isNotEmpty(provinceId) && UtilValidate.isNotEmpty(cityId))//
            {
                stateProvinceFindCond = EntityCondition.makeCondition(
                        EntityCondition.makeCondition("geoIdFrom", cityId),
                        EntityCondition.makeCondition("geoAssocTypeId", "REGIONS"),
                        EntityCondition.makeCondition(EntityOperator.AND,
                                EntityCondition.makeCondition("geoTypeId", "COUNTY")
                        ));
                geoList.addAll(delegator.findList("GeoAssocAndGeoToWithState", stateProvinceFindCond, null, sortList, null, true));
            }
            List jsonArray = new ArrayList();
            for (GenericValue geolist : geoList) {
                JSONObject map = new JSONObject();
                map.put("id", geolist.get("geoId"));
                map.put("title", geolist.get("geoName"));
                jsonArray.add(map);
            }
            result.put("resultData", jsonArray);
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        return result;
    }

    /**
     * 更新修改收货地址接口
     * add by gss
     *
     * @param dctx
     * @param context
     */
    public static Map<String, Object> createOrUpdatePostalAddressAndPurposes(DispatchContext dctx,
                                                                             Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 返回结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 获取参数  省Id*/
        String userLoginId = (String) context.get("userLoginId");
        /** 地址Id*/
        String contactMechId = (String) context.get("id");
        /** 收货人姓名*/
        String toName = (String) context.get("name");
        /** 收货人联系方式*/
        String mobilePhone = (String) context.get("telphone");
        /**收货地址详情(不包含省市区)*/
        String address1 = (String) context.get("location");
        /**省*/
        String stateProvinceGeoId = (String) context.get("stateProvinceGeoId");
        /**市*/
        String city = (String) context.get("city");
        /**区*/
        String countyGeoId = (String) context.get("countyGeoId");
        /**是否默认*/
        String defaultId = (String) context.get("defaultId");

        /**根据用户登录信息获取PartyId*/
        String partyId = null;
        GenericValue UserLogin;
        try {
            UserLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(UserLogin)) {
                partyId = (String) UserLogin.get("partyId");
            }
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }

        /**contactMechId 地址Id是否为空 ,如果为空 表示新增收货地址,否则更新收货地址*/
        if (UtilValidate.isNotEmpty(contactMechId)) {
            try {
                GenericValue PostalAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", contactMechId));
                if (UtilValidate.isNotEmpty(contactMechId)) {
                    PostalAddress.set("toName", toName);
                    PostalAddress.set("address1", address1);
                    PostalAddress.set("mobilePhone", mobilePhone);
                    PostalAddress.set("stateProvinceGeoId", stateProvinceGeoId);
                    PostalAddress.set("city", city);
                    PostalAddress.set("countyGeoId", countyGeoId);
                    PostalAddress.store();
                }
                GenericValue profiledefs = EntityUtil.getFirst(delegator.findByAnd("PartyProfileDefault", UtilMisc.toMap("partyId", partyId)));
                if (UtilValidate.isNotEmpty(profiledefs)) {
                    profiledefs.set("defaultShipAddr", defaultId);
                    profiledefs.store();
                } else {
                    GenericValue PartyProfileDefault = delegator.makeValue("PartyProfileDefault");
                    PartyProfileDefault.set("partyId", partyId);
                    PartyProfileDefault.set("defaultShipAddr", defaultId);
                    PartyProfileDefault.create();
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            resultData.put("status", true);
            resultData.put("info", "更新成功");
        } else {

            GenericValue ContactMech = delegator.makeValue("ContactMech");
            String contactId = delegator.getNextSeqId("ContactMech");
            ContactMech.set("contactMechId", contactId);
            ContactMech.set("contactMechTypeId", "POSTAL_ADDRESS");
            try {
                ContactMech.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue PostalAddress = delegator.makeValue("PostalAddress");

            PostalAddress.set("contactMechId", contactId);
            PostalAddress.set("toName", toName);
            PostalAddress.set("address1", address1);
            PostalAddress.set("mobilePhone", mobilePhone);
            PostalAddress.set("stateProvinceGeoId", stateProvinceGeoId);
            PostalAddress.set("city", city);
            PostalAddress.set("countyGeoId", countyGeoId);
            try {
                PostalAddress.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue PartyContactMech = delegator.makeValue("PartyContactMech");
            PartyContactMech.set("partyId", partyId);
            PartyContactMech.set("contactMechId", contactId);
            PartyContactMech.set("fromDate", UtilDateTime.nowTimestamp());
            try {
                PartyContactMech.create();
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            GenericValue PartyContactMechPurpose = delegator.makeValue("PartyContactMechPurpose");
            PartyContactMechPurpose.set("partyId", partyId);
            PartyContactMechPurpose.set("contactMechId", contactId);
            PartyContactMechPurpose.set("contactMechPurposeTypeId", "SHIPPING_LOCATION");
            PartyContactMechPurpose.set("fromDate", UtilDateTime.nowTimestamp());
            try {
                PartyContactMechPurpose.create();
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
                            GenericValue PartyProfileDefault = delegator.makeValue("PartyProfileDefault");
                            PartyProfileDefault.set("partyId", partyId);
                            PartyProfileDefault.set("productStoreId", productStoreId);
                            PartyProfileDefault.set("defaultShipAddr", contactId);
                            PartyProfileDefault.create();
                        }
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            resultData.put("status", true);
            resultData.put("info", "创建成功");
            resultData.put("addressId", contactId);
        }
        result.put("resultData", resultData);
        return result;
    }

    /**
     * 删除地址
     * add by gss
     *
     * @param dctx
     * @param context
     */
    public static Map<String, Object> deletePostalAddress(DispatchContext dctx,
                                                          Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        /** 返回结果集 */
        Map<String, Object> result = FastMap.newInstance();
        /** 查询结果 */
        Map<String, Object> resultData = FastMap.newInstance();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 用户ID*/
        String userLoginId = (String) context.get("userLoginId");
        /** 地址Id*/
        String contactMechId = (String) context.get("delId");
        /**根据用户登录信息获取PartyId*/
        String partyId = null;
        GenericValue UserLogin;
        try {
            UserLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(UserLogin)) {
                partyId = (String) UserLogin.get("partyId");
            }
        } catch (GenericEntityException ex) {
            return ServiceUtil.returnError(ex.getMessage());
        }
        try {
            Map<String, Object> paramContext = FastMap.newInstance();
            paramContext.put("contactMechId", contactMechId);
            paramContext.put("partyId", partyId);
            paramContext.put("userLogin", UserLogin);
            try {
                Map<String, Object> resultContext = dispatcher.runSync("deletePartyContactMech", paramContext);
            } catch (GenericServiceException e1) {
                e1.printStackTrace();
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
                            GenericValue PartyProfileDefault = delegator.makeValue("PartyProfileDefault");
                            PartyProfileDefault.set("partyId", partyId);
                            PartyProfileDefault.set("defaultShipAddr", shippingContactMechList.get(0).get("contactMechId"));
                            PartyProfileDefault.create();
                        }
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    resultData.put("defaultId", shippingContactMechList.get(0).get("contactMechId"));
                } else {
                    profiledefs.remove();
                    resultData.put("defaultId", null);
                }
            }
            resultData.put("status", true);
            resultData.put("info", "删除成功");
            result.put("resultData", resultData);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 会员消费查询
     * add  by gss  2016/3/30
     *
     * @param dcx
     * @param context
     * @return
     */
    public static Map<String, Object> findPartyConsumption(DispatchContext dcx, Map<String, ? extends Object> context) {

        Map<String, Object> result = ServiceUtil.returnSuccess();
        //用户名
        String userLoginId = (String) context.get("userLoginId");
        //会员昵称
        String nickname = (String) context.get("nickname");
        //手机号
        String mobile = (String) context.get("mobile");
        //订单号
        String orderId = (String) context.get("orderId");
        //开始时间
        String startDate = (String) context.get("startDate");
        //结束时间
        String endDate = (String) context.get("endDate");
        Delegator delegator = dcx.getDelegator();

        Locale locale = (Locale) context.get("locale");
        String lookupFlag = (String) context.get("lookupFlag");
        if (lookupFlag == null) {
            lookupFlag = "Y";
        }
        String orderFiled = (String) context.get("ORDER_FILED");
        String orderFiledBy = (String) context.get("ORDER_BY");

        result.put("orderFiled", orderFiled == null ? "" : orderFiled);
        result.put("orderBy", orderFiledBy == null ? "" : orderFiledBy);

        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 10;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 10;
        }
        result.put("viewSize", Integer.valueOf(viewSize));

        // int lowIndex = 0;
        //计算开始分页值 & 计算分页结束值
        lowIndex = viewIndex * viewSize;
        /** sql 语句*/
        GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
        SQLProcessor sqlP = new SQLProcessor(helperInfo);
        SQLProcessor sqlPs = new SQLProcessor(helperInfo);

        lowIndex = viewIndex * viewSize;
        highIndex = (viewIndex + 1) * viewSize;
        String limitSql = "limit " + lowIndex + "," + viewSize;
        String ordersql = "select distinct oh.order_id as orderId ,oh.order_id as returnId, oh.grand_total as totalmount , os.created_stamp as createTime,ul.user_login_id, ps.nickname, ps.mobile, ol.party_id" +
                " from  order_header oh,order_role ol, order_item b, product c,order_status os ,user_login ul ,person ps"
                + " where oh.order_id=ol.order_id"
                + " and oh.order_id=b.order_id"
                + " and b.product_id= c.product_id"
                + " and oh.order_id=b.order_id"
                + " and ul.party_id=ol.party_id"
                + " and ps.party_id=ol.party_id"
                + " and os.order_id=oh.order_id"
                + " and ol.role_type_id= 'PLACING_CUSTOMER' "
                + " and oh.status_id in ('ORDER_COMPLETED','ORDER_WAITEVALUATE','ORDER_RETURNED')"
                //物流配送的订单：订单状态变为“待发货”的时间 ,  自提订单：订单状态变为“待评价”的时间 ,虚拟订单：订单状态变为“待评价”的时间
                + "and ((c.product_type_id= 'VIRTUAL_GOOD' and  os.status_id='ORDER_WAITEVALUATE') or (oh.distribution_method in ('ZMPS','GZRPS') and os.status_id='ORDER_WAITSHIP') or (oh.distribution_method='SMZT' and os.status_id='ORDER_WAITEVALUATE') )";
        // +" and os.status_id in ('ORDER_WAITSHIP','ORDER_WAITEVALUATE')";
        String returnsql = "select distinct rh.order_id as orderId , rh.return_id as returnId , rh.actual_payment_money as totalmount, rh.created_stamp as createTime  ,ul.user_login_id, ps.nickname, ps.mobile, ol.party_id" +
                " from order_role ol, return_item rh ,user_login ul ,person ps"
                + " where rh.order_id=ol.order_id "
                + " and ul.party_id=ol.party_id"
                + " and ps.party_id=ol.party_id"
                + " and ol.role_type_id= 'PLACING_CUSTOMER' "
                + " and rh.status_id='RETURN_COMPLETED'";

        String paramList = "";
        /**用户名*/
        if (UtilValidate.isNotEmpty(userLoginId)) {
            paramList = paramList + "&userLoginId=" + userLoginId;
            ordersql = ordersql + " and ul.user_login_id like " + "'%" + userLoginId + "%'";
            returnsql = returnsql + " and ul.user_login_id like " + "'%" + userLoginId + "%'";
            // andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("userLoginId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + userLoginId + "%")));
        }
        /**昵称*/
        if (UtilValidate.isNotEmpty(nickname)) {
            paramList = paramList + "&nickname=" + nickname;
            ordersql = ordersql + " and ps.nickname like " + "'%" + nickname + "%'";
            returnsql = returnsql + " and ps.nickname like " + "'%" + nickname + "%'";
            //andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("nickname"), EntityOperator.LIKE, EntityFunction.UPPER("%" + nickname + "%")));
        }
        /**手机*/
        if (UtilValidate.isNotEmpty(mobile)) {
            paramList = paramList + "&mobile=" + mobile;
            ordersql = ordersql + " and ps.mobile like " + "'%" + mobile + "%'";
            returnsql = returnsql + " and ps.mobile like " + "'%" + mobile + "%'";
            //andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("mobile"), EntityOperator.LIKE, EntityFunction.UPPER("%" + mobile + "%")));
        }
        /**订单号*/
        if (UtilValidate.isNotEmpty(orderId)) {
            paramList = paramList + "&orderId=" + orderId;
            ordersql = ordersql + " and oh.order_id like " + "'%" + orderId + "%'" + " ";
            returnsql = returnsql + " and rh.order_id like " + "'%" + orderId + "%'" + " ";
            //andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("orderId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + orderId + "%")));
        }
        /**开始时间*/
        if (UtilValidate.isNotEmpty(startDate) && startDate.length() > 8) {
            paramList = paramList + "&startDate=" + startDate;
            startDate = startDate.trim();
            returnsql = returnsql + " and rh.created_stamp >=" + "'" + startDate + "'" + " ";
            ordersql = ordersql + " and os.created_stamp >=" + "'" + startDate + "'" + " ";
            //ordersql=ordersql+" and (os.created_stamp >="+"'"+ converted+"'"+ "and os.status_id='ORDER_WAITSHIP') or (os.created_stamp >="+"'"+converted+"'"+" and os.status_id='ORDER_WAITSHIP')" +" ";
        }
        /**结束时间*/
        if (UtilValidate.isNotEmpty(endDate) && endDate.length() > 8) {
            paramList = paramList + "&endDate=" + endDate;
            endDate = endDate.trim();
            //ordersql=ordersql+" and (os.created_time >="+"'"+ converted+"'"+"and os.status_id='ORDER_WAITSHIP') or (rh.created_time >="+"'"+converted+"'"+" and os.status_id='ORDER_WAITSHIP')" +" ";
            ordersql = ordersql + " and os.created_stamp <=" + "'" + endDate + "'" + " ";
            returnsql = returnsql + " and rh.created_stamp <=" + "'" + endDate + "'" + " ";
        }

        String sql = "select *  " +
                "  from( " +
                ordersql +
                " union all " +
                returnsql +
                ") as temptb ";

        if (UtilValidate.isNotEmpty(orderFiled)
                && UtilValidate.isNotEmpty(orderFiledBy)) {
            sql = sql + "order by" + " " + orderFiled + " " + orderFiledBy + " ";
        } else {
            sql = sql + " order by orderId desc ";
        }
        String countsql = "SELECT count(*) as number FROM (" + sql + ") as tempdate";
        long max = 0L;
        try {
            sqlPs.prepareStatement(countsql);
            ResultSet resultSizeSet = sqlPs.executeQuery();
            while (resultSizeSet.next()) {
                max = resultSizeSet.getLong("number");
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        List<Map<String, Object>> jsonArray = null;
        try {
            sqlP.prepareStatement(sql + limitSql);
            ResultSet rs = sqlP.executeQuery();
            jsonArray = getListFromResultSet(rs);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取总记录数
        if (highIndex > max) {
            highIndex = (int) max;
        }

        result.put("userLoginId", userLoginId);
        result.put("nickname", nickname);
        result.put("mobile", mobile);
        result.put("orderId", orderId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("partyConsumptionList", jsonArray);
        result.put("partyConsumptionListSize", max);
        result.put("paramList", paramList);
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex + 1));
        return result;
    }

    /**
     * 结果集转换为List add by wcy
     *
     * @param rs
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> getListFromResultSet(ResultSet rs) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();
        while (rs.next()) {
            Map<String, Object> rowData = new HashMap<String, Object>();
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getObject(i));
            }
            list.add(rowData);
        }
        return list;
    }

    /**
     * 创建会员信息 2016-4-6
     * add by gss
     *
     * @param dctx
     * @param context
     */
    public static Map<String, Object> createCustomerforIco(DispatchContext dctx,
                                                           Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        List<GenericValue> toBeStored = FastList.newInstance();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 真实姓名*/
        String name = (String) context.get("name");
        String allAddress = (String) context.get("allAddress");
        /** 企业名称*/
        String company_name = (String) context.get("company_name");
        /**性别*/
        String gender = (String) context.get("gender");
        /**电话*/
        String mobile = (String) context.get("mobile");
        /** 企业电话*/
        String company_mobile = (String) context.get("company_mobile");
        /** 企业地址*/
        String description = (String) context.get("description");
        String company_userLoginId = (String) context.get("company_userLoginId");
        String company_currentPassword = (String) context.get("company_currentPassword");
        /**昵称*/
        String nickname = (String) context.get("nickname");
        /**企业会员昵称*/
        String company_nickname = (String) context.get("company_nickname");
        /**会员类型*/
        String partyCategory = (String) context.get("partyCategory");
        /**用户名*/
        String userLoginId = (String) context.get("userLoginId");
        /**密码*/
        String currentPassword = (String) context.get("currentPassword");
        // 会员编码
        String curPartyId=(String) context.get("partyId");
        /*密码确认 前端操作*/
        String confirm_password = currentPassword;
        if ((userLoginId != null) && ("true".equalsIgnoreCase(UtilProperties.getPropertyValue("security.properties", "username.lowercase")))) {
            userLoginId = userLoginId.toLowerCase();
        }
        if ((currentPassword != null) && ("true".equalsIgnoreCase(UtilProperties.getPropertyValue("security.properties", "password.lowercase")))) {
            currentPassword = currentPassword.toLowerCase();
            confirm_password = confirm_password.toLowerCase();
        }
        Map<String, Object> userloginContext = FastMap.newInstance();
        userloginContext.put("userLogin", (GenericValue) context.get("userLogin"));
        userloginContext.put("enabled", "Y");
        GenericValue MemberpartyLevel = null;
        GenericValue CompanypartyLevel = null;
        try {
            MemberpartyLevel = EntityUtil.getFirst(delegator.findByAnd("PartyLevelType", UtilMisc.toMap("partyType", "MEMBER"), UtilMisc.toList("levelCode")));
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        try {
            CompanypartyLevel = EntityUtil.getFirst(delegator.findByAnd("PartyLevelType", UtilMisc.toMap("partyType", "COMPANY"), UtilMisc.toList("levelCode")));
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        /* 会员等级数据 */
        Map<String, Object> partyLevelContext = FastMap.newInstance();
        if ("MEMBER".equals(partyCategory)) {
            if (UtilValidate.isNotEmpty(MemberpartyLevel)) {
                partyLevelContext.put("levelId", MemberpartyLevel.get("levelId"));
                partyLevelContext.put("levelName", MemberpartyLevel.get("levelName"));
            } else {
                result.put("status", false);
                result.put("info", "没有个人会员等级规则信息！请添加个人会员等级规则");
                return result;
            }
        }
        if ("COMPANY".equals(partyCategory)) {
            if (UtilValidate.isNotEmpty(CompanypartyLevel)) {
                partyLevelContext.put("levelId", CompanypartyLevel.get("levelId"));
                partyLevelContext.put("levelName", CompanypartyLevel.get("levelName"));
            } else {
                result.put("status", false);
                result.put("info", "没有企业会员等级规则信息！请添加企业会员等级规则");
                return result;
            }
        }
        Map<String, Object> personContext = FastMap.newInstance();
        if (UtilValidate.isNotEmpty(partyCategory) && "MEMBER".equals(partyCategory)) {
            personContext.put("name", name);
            personContext.put("gender", gender);
            personContext.put("nickname", nickname);
            personContext.put("mobile", mobile);
            personContext.put("partyId",curPartyId);

            userloginContext.put("userLoginId", userLoginId);
            userloginContext.put("currentPassword", currentPassword);
            userloginContext.put("currentPasswordVerify", currentPassword);
        } else if (UtilValidate.isNotEmpty(partyCategory) && "COMPANY".equals(partyCategory)) {
            personContext.put("name", company_name);
            personContext.put("description", description);
            personContext.put("nickname", company_nickname);
            personContext.put("mobile", company_mobile);
            userloginContext.put("userLoginId", company_userLoginId);
            userloginContext.put("currentPassword", company_currentPassword);
            userloginContext.put("currentPasswordVerify", company_currentPassword);
        }
        personContext.put("partyCategory", partyCategory);
        // 调用创建userLogin服务
        String partyId = null;
        try {
            Map<String, Object> partyContext = dispatcher.runSync(
                    "createPerson", personContext);
            partyId = (String) partyContext.get("partyId");
        } catch (GenericServiceException e1) {
            e1.printStackTrace();
        }

		/* 会员data数据 */
        Map<String, Object> partySourceContext = FastMap.newInstance();
        partySourceContext.put("dataSourceId", "ECOMMERCE_SITE");
        partySourceContext.put("fromDate", UtilDateTime.nowTimestamp());
        partySourceContext.put("isCreate", "Y");
        partySourceContext.put("visitId", "");
		
		/* 创建用户角色类型 */
        Map<String, Object> partyRoleContext = FastMap.newInstance();
        partyRoleContext.put("userLogin", (GenericValue) context.get("userLogin"));
        partyRoleContext.put("roleTypeId", "CUSTOMER");

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
		/* 会员店铺关联表 */
        Map<String, Object> productStoreRoleContext = FastMap.newInstance();
        productStoreRoleContext.put("userLogin", (GenericValue) context.get("userLogin"));
        productStoreRoleContext.put("roleTypeId", "CUSTOMER");
        try {
            List<GenericValue> productStore = delegator.findByAnd("ProductStore", (Object[]) null);
            if (UtilValidate.isNotEmpty(productStore)) {
                productStoreRoleContext.put("productStoreId", productStore.get(0).get("productStoreId"));
            }
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }

        if (UtilValidate.isNotEmpty(partyId)) {
            userloginContext.put("partyId", partyId);
            partySourceContext.put("partyId", partyId);
            partyLevelContext.put("partyId", partyId);
            partyAccountContext.put("partyId", partyId);
            partyScoreContext.put("partyId", partyId);
            partyAttributeContext.put("partyId", partyId);
            productStoreRoleContext.put("partyId", partyId);
            GenericValue partyLevelS = delegator.makeValue("PartyLevel", partyLevelContext);
            GenericValue partyAccount = delegator.makeValue("PartyAccount", partyAccountContext);
            GenericValue partyDataSource = delegator.makeValue("PartyDataSource", partySourceContext);
            GenericValue partyScore = delegator.makeValue("PartyScore", partyScoreContext);
            GenericValue partyAttribute = delegator.makeValue("PartyAttribute", partyAttributeContext);
            toBeStored.add(partyDataSource);
            toBeStored.add(partyLevelS);
            toBeStored.add(partyAccount);
            toBeStored.add(partyScore);
            toBeStored.add(partyAttribute);
            try {
                delegator.storeAll(toBeStored);
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
                return ServiceUtil.returnError(UtilProperties.getMessage(resourceError,
                        "person.create.db_error", new Object[]{e.getMessage()}, locale));
            }
            partyRoleContext.put("partyId", partyId);
            try {
                // 调用创建userLogin服务
                dispatcher.runSync("createUserLogin", userloginContext);
                // 调用创建用户角色信息服务
                dispatcher.runSync("createPartyRole", partyRoleContext);
                //店铺会员关联
                dispatcher.runSync("createProductStoreRole", productStoreRoleContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (UtilValidate.isNotEmpty(allAddress)) {
            Map<String, Object> postalMap = FastMap.newInstance();
            postalMap.put("userLogin", (GenericValue) context.get("userLogin"));
            postalMap.put("fromDate", UtilDateTime.nowTimestamp());
            postalMap.put("countyGeoId", "CHN");
            postalMap.put("preContactMechTypeId", "POSTAL_ADDRESS");
            postalMap.put("contactMechTypeId", "POSTAL_ADDRESS");
            postalMap.put("partyId", partyId);
            Map<String, Object> outMap = null;
            String[] all_Address = allAddress.split(",");
            for (int i = 0; i < all_Address.length; i++) {
                String address = all_Address[i];
                String[] one_address = address.split(":");
                if (one_address[0] != null && !"".equals(one_address[0])) {
                    postalMap.put("toName", one_address[0]);
                }
                if (one_address[1] != null && !"".equals(one_address[1])) {
                    postalMap.put("mobilePhone", one_address[1]);
                }
                if (one_address[2] != null && !"".equals(one_address[2])) {
                    postalMap.put("stateProvinceGeoId", one_address[2]);
                }
                if (one_address[3] != null && !"".equals(one_address[3])) {
                    postalMap.put("city", one_address[3]);
                }
                if (one_address[4] != null && !"".equals(one_address[4])) {
                    postalMap.put("countyGeoId", one_address[4]);
                }
                if (one_address[5] != null && !"".equals(one_address[5])) {
                    postalMap.put("address1", one_address[5]);
                }
                if (one_address[6] != null && !"".equals(one_address[6])) {
                    postalMap.put("isDefault", one_address[6]);
                }
                try {
                    outMap = dispatcher.runSync("createPartyPostalAddress", postalMap);
                } catch (GenericServiceException e) {
                    e.printStackTrace();
                }

            }
        }
        result.put("status", true);
        result.put("info", "会员创建成功");
        return result;
    }

    /**
     * 创建会员信息 2016-4-6
     * add by gss
     *
     * @param dctx
     * @param context
     */
    public static Map<String, Object> updateCustomerforIco(DispatchContext dctx,
                                                           Map<String, ? extends Object> context) {
        /** 获取托管 */
        Delegator delegator = dctx.getDelegator();
        /** 返回结果集 */
        Map<String, Object> result = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        List<GenericValue> toBeStored = FastList.newInstance();
        /** 获取本地化 */
        Locale locale = (Locale) context.get("locale");
        /** 真实姓名*/
        String name = (String) context.get("name");
        /** 企业名称*/
        String company_name = (String) context.get("company_name");
        /**性别*/
        String gender = (String) context.get("gender");
        String partyId = (String) context.get("partyId");
        /**电话*/
        String mobile = (String) context.get("mobile");
        /** 企业电话*/
        String company_mobile = (String) context.get("company_mobile");
        /** 企业地址*/
        String description = (String) context.get("description");
        /**昵称*/
        String nickname = (String) context.get("nickname");
        /**企业会员昵称*/
        String company_nickname = (String) context.get("company_nickname");
        /**会员类型*/
        String partyCategory = (String) context.get("partyCategory");
        /**用户名*/
        String userLoginId = (String) context.get("userLoginId");
        /**密码*/
        String currentPassword = (String) context.get("currentPassword");
		/*密码确认 前端操作*/
        String confirm_password = currentPassword;
        if ((userLoginId != null) && ("true".equalsIgnoreCase(UtilProperties.getPropertyValue("security.properties", "username.lowercase")))) {
            userLoginId = userLoginId.toLowerCase();
        }
        if ((currentPassword != null) && ("true".equalsIgnoreCase(UtilProperties.getPropertyValue("security.properties", "password.lowercase")))) {
            currentPassword = currentPassword.toLowerCase();
            confirm_password = confirm_password.toLowerCase();
        }
        Map<String, Object> personContext = FastMap.newInstance();
        personContext.put("userLogin", (GenericValue) context.get("userLogin"));
        if (UtilValidate.isNotEmpty(partyCategory) && "MEMBER".equals(partyCategory)) {
            personContext.put("name", name);
            personContext.put("gender", gender);
            personContext.put("nickname", nickname);
            personContext.put("mobile", mobile);
        } else if (UtilValidate.isNotEmpty(partyCategory) && "COMPANY".equals(partyCategory)) {
            personContext.put("name", company_name);
            personContext.put("description", description);
            personContext.put("nickname", company_nickname);
            personContext.put("mobile", company_mobile);
        }
        personContext.put("partyCategory", partyCategory);
        personContext.put("partyId", partyId);
        // 调用创建userLogin服务
        try {
            Map<String, Object> partyContext = dispatcher.runSync(
                    "updatePerson", personContext);
        } catch (GenericServiceException e1) {
            e1.printStackTrace();
        }
        return result;
    }

    /**
     * 查询会员信息:查询条件(会员编码 ,会员分类,会员等级,登录账号,真实姓名,手机号)
     * add  by gss  2016-4-6
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findPartymember(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");

        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));


        List<Map> memberList = FastList.newInstance();
        int memberListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PT", "Party");
        dynamicView.addAlias("PT", "partyId");
        dynamicView.addAlias("PT", "partyTypeId");
        dynamicView.addAlias("PT", "statusId");
        dynamicView.addAlias("PT", "partyCategory");

        dynamicView.addMemberEntity("PS", "Person");
        dynamicView.addAlias("PS", "nickname");
        dynamicView.addAlias("PS", "gender");
        dynamicView.addAlias("PS", "name");
        dynamicView.addAlias("PS", "mobile");

        dynamicView.addMemberEntity("UL", "UserLogin");
        dynamicView.addAlias("UL", "userLoginId");

//        dynamicView.addMemberEntity("PL", "PartyLevel");
//        dynamicView.addAlias("PL", "partyId");
//
//        dynamicView.addMemberEntity("PLT", "PartyLevelType");
//        dynamicView.addAlias("PLT", "levelName");
//        dynamicView.addAlias("PLT", "levelId");

//        dynamicView.addViewLink("PT", "PL", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
//        dynamicView.addViewLink("PL", "PLT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("levelId", "levelId"));
        dynamicView.addViewLink("PT", "PS", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));
        dynamicView.addViewLink("PT", "UL", Boolean.FALSE, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("nickname");
        fieldsToSelect.add("gender");
        fieldsToSelect.add("mobile");
        fieldsToSelect.add("name");
//        fieldsToSelect.add("levelName");
        fieldsToSelect.add("partyCategory");
        fieldsToSelect.add("userLoginId");

        String sortField = "partyId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);

        // 根据会员编号模糊查询
        if (UtilValidate.isNotEmpty(context.get("partyId"))) {
            andExprs.add(EntityCondition.makeCondition(
                    EntityFunction.UPPER_FIELD("partyId"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("partyId") + "%")));
        }
        // 根据电话号码模糊查询
        if (UtilValidate.isNotEmpty(context.get("mobile"))) {
            andExprs.add(EntityCondition.makeCondition(
                    EntityFunction.UPPER_FIELD("mobile"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("mobile") + "%")));
        }
        //现在只查询个人会员
        List<String> partyCategoryList = FastList.newInstance();
        partyCategoryList.add("MEMBER");
        partyCategoryList.add("BUSINESS");
        // 会员类型
        andExprs.add(EntityCondition.makeCondition(
                EntityFunction.UPPER_FIELD("partyCategory"),
                EntityOperator.IN, partyCategoryList));
        //会员等级
       /* if (UtilValidate.isNotEmpty(context.get("levelId"))) {
            andExprs.add(EntityCondition.makeCondition(
                    EntityFunction.UPPER_FIELD("levelId"),
                    EntityOperator.EQUALS, context.get("levelId")));
        }*/
        //登录账号
        if (UtilValidate.isNotEmpty(context.get("userLoginId"))) {
            andExprs.add(EntityCondition.makeCondition(
                    EntityFunction.UPPER_FIELD("userLoginId"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("userLoginId") + "%")));
        }
        //会员名称
        if (UtilValidate.isNotEmpty(context.get("name"))) {
            andExprs.add(EntityCondition.makeCondition(
                    EntityFunction.UPPER_FIELD("name"), EntityOperator.LIKE,
                    EntityFunction.UPPER("%" + context.get("name") + "%")));
        }
        andExprs.add(EntityCondition.makeCondition("partyTypeId", EntityOperator.EQUALS, "PERSON"));
        // andExprs.add(EntityCondition.makeCondition("outId", EntityOperator.NOT_EQUAL, null));
        andExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PARTY_ENABLED"));
        //andExprs.add(EntityCondition.makeCondition("partyCategory", EntityOperator.EQUALS, "MEMBER"));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            //遍历查询结果集
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                Map record = FastMap.newInstance();
                String partyId = gv.getString("partyId");
                String nickname = gv.getString("nickname");
                String name = gv.getString("name");
                String gender = gv.getString("gender");
                String mobile = gv.getString("mobile");
                String levelName = gv.getString("levelName");
                String partyCategory = gv.getString("partyCategory");
                String userLoginId = gv.getString("userLoginId");

                record.put("partyId", partyId);
                record.put("nickname", nickname);
                record.put("name", name);
                record.put("gender", gender);
                record.put("mobile", mobile);
                record.put("levelName", levelName);
                record.put("partyCategory", partyCategory);
                record.put("userLoginId", userLoginId);

                //会员的账号余额
                String amount = "0";
                GenericValue pa_gv = EntityUtil.getFirst(delegator.findByAnd("PartyAccount", UtilMisc.toMap("partyId", partyId)));
                if (UtilValidate.isNotEmpty(pa_gv)) {
                    amount = pa_gv.getString("amount");
                }
                record.put("amount", amount);
                memberList.add(record);
            }
            //总记录数
            memberListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > memberListSize) {
                highIndex = memberListSize;
            }
            //关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in member find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "MemberLookupMemberError",
                    UtilMisc.toMap("errMessage", e.toString()), locale));
        }
        result.put("recordsList", memberList);
        result.put("totalSize", Integer.valueOf(memberListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }

    /**
     * 字段详情查询 add by gss 2016-1-9
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findUserloginIdById(DispatchContext dctx,
                                                          Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        //geoId

        String userLoginId = (String) context.get("userLoginId");
        try {
            //字段信息
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            if (UtilValidate.isNotEmpty(userLogin)) {
                result.put("status", false);
                result.put("info", "登录账号已存在！");
            } else {
                result.put("status", true);
                result.put("info", "可用的登录账号！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 会员优惠券信息 add by gss 2016-1-9
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findProductPromoCodeParty(DispatchContext dctx,
                                                                Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        List<Map<String, Object>> promoList = FastList.newInstance();
        //geoId
        String partyId = (String) context.get("partyId");
        int promoListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        //已使用的券
        int usedCoupon = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));
        // blank param list
        String paramList = "";

        DynamicViewEntity dve = new DynamicViewEntity();
	    /*会员优惠券关联表*/
        dve.addMemberEntity("PPCP", "ProductPromoCodeParty");
        dve.addAlias("PPCP", "partyId");
        dve.addAlias("PPCP", "productPromoCodeId");
        dve.addAlias("PPCP", "useDate");
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("productPromoCodeId");
        fieldsToSelect.add("useDate"); //领取时间
        List<String> orderBy = FastList.newInstance();

        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<GenericValue> List = null;
        if (UtilValidate.isNotEmpty(partyId)) {
            paramList = paramList + "&partyId=" + partyId;
            andExprs.add(EntityCondition.makeCondition("partyId",
                    EntityOperator.EQUALS, partyId));
        }
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dve,
                    mainCond, null, fieldsToSelect, orderBy, findOpts);
            List = pli.getPartialList(lowIndex, viewSize);
            promoListSize = pli.getResultsSizeAfterPartialList();
            //遍历查询结果集
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (UtilValidate.isNotEmpty(List)) {
                for (GenericValue gv : List) {
                    Map<String, Object> map = FastMap.newInstance();
                    map.put("productPromoCodeId", gv.get("productPromoCodeId"));//券号
                    map.put("useDate", sdf.format(gv.getTimestamp("useDate")));//领取时间
                    GenericValue orderProductPromoCode = EntityUtil.getFirst(delegator.findByAnd("OrderProductPromoCode", UtilMisc.toMap("productPromoCodeId", gv.get("productPromoCodeId"))));
                    if (UtilValidate.isNotEmpty(orderProductPromoCode)) {
                        map.put("isUsed", true);//是否使用==已使用
                        map.put("usedTime", sdf.format(orderProductPromoCode.getTimestamp("createdStamp")));//是否使用==已使用
                        usedCoupon++;
                    } else {
                        map.put("isUsed", false);//是否使用==未使用
                        map.put("usedTime", "");//使用时间
                    }
                    GenericValue productPromoCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId", gv.get("productPromoCodeId")));
                    if (UtilValidate.isNotEmpty(productPromoCode)) {
                        GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", productPromoCode.get("couponCode")));
                        if (UtilValidate.isNotEmpty(productPromoCoupon)) {
                            map.put("couponType", productPromoCoupon.get("couponType"));//优惠券类型
                            map.put("couponName", productPromoCoupon.get("couponName"));//优惠券名称
                            //map.put("money",productPromoCoupon.getBigDecimal("payReduce").setScale(2));
                        }
                    }
                    promoList.add(map);
                }
            }
            if (highIndex > promoListSize) {
                highIndex = promoListSize;
            }
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("promoListSize", Integer.valueOf(promoListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        result.put("paramList", paramList);
        result.put("usedCouponSize", Integer.valueOf(usedCoupon));
        result.put("promoList", promoList);
        return result;
    }

    /**
     * 会员定制果汁 add by gss 2016-1-9
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> findCustomJuice(DispatchContext dctx,
                                                      Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        List<Map<String, Object>> promoList = FastList.newInstance();
        String partyId = (String) context.get("partyId");
        int promoListSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        //已使用的券
        int usedCoupon = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));
        // blank param list
        String paramList = "";

        /** 定义订单动态视图 */
        DynamicViewEntity dynamicViewEntity = new DynamicViewEntity();
        dynamicViewEntity.addMemberEntity("OH", "OrderHeader");
        dynamicViewEntity.addAlias("OH", "orderStatus", "statusId", null, null, null, null);
        dynamicViewEntity.addAlias("OH", "createdStamp");//定制时间
        /*add by gss*/
        dynamicViewEntity.addMemberEntity("ORE", "OrderRole");
        //会员ID
        dynamicViewEntity.addAlias("ORE", "partyId");
        //角色类型ID
        dynamicViewEntity.addAlias("ORE", "roleTypeId");
        dynamicViewEntity.addAlias("OH", "statusId");

        dynamicViewEntity.addMemberEntity("OI", "OrderItem");
        dynamicViewEntity.addAlias("OI", "orderId");
        dynamicViewEntity.addAlias("OI", "productId");
        dynamicViewEntity.addAlias("OI", "itemDescription");//定制商品名称
        dynamicViewEntity.addAlias("OI", "unitPrice");//定制商品价格
        dynamicViewEntity.addAlias("OI", "orderItemSeqId");//定制商品价格

        /** 定义商品动态视图 */
        dynamicViewEntity.addMemberEntity("PR", "Product");
        dynamicViewEntity.addAlias("PR", "productId");
        dynamicViewEntity.addAlias("PR", "productTypeId");
        dynamicViewEntity.addAlias("PR", "primaryProductCategoryId");
        dynamicViewEntity.addAlias("PR", "volume");//体积

        /** 建立关联关系 */
        dynamicViewEntity.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
        dynamicViewEntity.addViewLink("OI", "PR", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));
        /**orderOrle 与orderItem*/
        dynamicViewEntity.addViewLink("OH", "ORE", false, UtilMisc.toList(new ModelKeyMap("orderId", "orderId")));
		
		/*果汁配置关联表*/
        //dve.addMemberEntity("OSP", "OrderItemSemiProductProportion");

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("createdStamp"); //定制时间
        fieldsToSelect.add("orderId");
        fieldsToSelect.add("productId"); //商ID
        fieldsToSelect.add("itemDescription"); //名称
        fieldsToSelect.add("volume"); //体积
        fieldsToSelect.add("unitPrice"); //价格
        fieldsToSelect.add("orderItemSeqId"); //行项目序列号
        List<String> orderBy = FastList.newInstance();
        orderBy.add("createdStamp");
        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<GenericValue> List = null;
        // andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"));
        andExprs.add(EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "BILL_TO_CUSTOMER"));
        if (UtilValidate.isNotEmpty(partyId)) {
            paramList = paramList + "partyId=" + partyId;
            andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
        }

        andExprs.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
        andExprs.add(EntityCondition.makeCondition("primaryProductCategoryId", EntityOperator.EQUALS, "DIY_GOODS"));
        //待生成,已完成,待收货
        andExprs.add(EntityCondition.makeCondition("orderStatus", EntityOperator.IN, UtilMisc.toList("ORDER_WAITPRODUCE", "ORDER_WAITRECEIVE", "ORDER_COMPLETED")));
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
			/*EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);*/
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicViewEntity,
                    mainCond, null, fieldsToSelect, orderBy, null);
            List = pli.getPartialList(lowIndex, viewSize);
            promoListSize = pli.getResultsSizeAfterPartialList();
            //遍历查询结果集
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (UtilValidate.isNotEmpty(List)) {
                for (GenericValue gv : List) {
                    Map<String, Object> map = FastMap.newInstance();
                    List<GenericValue> orderItemSemiProductProportion = delegator.findByAnd("OrderItemSemiProductProportion", UtilMisc.toMap("orderId", gv.get("orderId"), "orderItemSeqId", gv.get("orderItemSeqId")));
                    String config_Name = "";
                    DecimalFormat dfs = new DecimalFormat("0.00");
                    int i = 0;
                    if (UtilValidate.isNotEmpty(orderItemSemiProductProportion)) {
                        for (GenericValue gvs : orderItemSemiProductProportion) {
                            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", gvs.get("semiProductProportionId")));
                            String configName = "";
                            if (UtilValidate.isNotEmpty(product)) {
                                if ("SEMI_FINISHED_GOOD".equals(product.get("productTypeId"))) {
                                    if (i == orderItemSemiProductProportion.size() - 1) {
                                        configName = product.getString("productName") + UtilMisc.doubleTrans(gvs.getBigDecimal("proportion")) + "%";
                                    } else {
                                        configName = product.getString("productName") + UtilMisc.doubleTrans(gvs.getBigDecimal("proportion")) + "%" + "+";
                                    }

                                } else if ("FRUIT_GRAIN_GOOD".equals(product.get("productTypeId"))) {
                                    configName = product.getString("productName");
                                }
                            }
                            config_Name += configName;
                            i++;
                        }
                    }

                    //定制时间
                    map.put("configName", config_Name);
                    //定制时间
                    map.put("createdStamp", sdf.format(gv.getTimestamp("createdStamp")));
                    //定制名称
                    map.put("itemDescription", gv.get("itemDescription"));
                    //体积
                    map.put("volume", gv.get("volume"));
                    //价格
                    map.put("unitPrice", gv.get("unitPrice"));
                    promoList.add(map);
                }
            }
            if (highIndex > promoListSize) {
                highIndex = promoListSize;
            }
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("promoListSize", Integer.valueOf(promoListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        result.put("paramList", paramList);
        result.put("usedCouponSize", Integer.valueOf(usedCoupon));
        result.put("promoList", promoList);
        return result;
    }

    /**
     * 删除验证码
     *
     * @param ctx
     * @param context
     * @return
     */
    public static Map<String, Object> removeCheckCode(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Map<String, Object> successResult = ServiceUtil.returnSuccess();
        String phoneId = (String) context.get("phoneId");
        String checkCode = (String) context.get("checkCode");
        int result = 0;
        try {
            result = delegator.removeByAnd("MobileCheckCode", UtilMisc.toMap("phoneId", phoneId, "checkCode", checkCode));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (result > 0) {
            successResult.put("resultData", true);
        } else {
            successResult.put("resultData", false);
        }
        return successResult;
    }
}
