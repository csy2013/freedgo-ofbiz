package com.yuaoq.yabiz.app.mobile.microservice.wx;

import cn.jiguang.common.utils.StringUtils;
import com.google.gson.Gson;
import com.yuaoq.yabiz.app.mobile.microservice.wx.commons.DuplicatedMessageChecker;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.services.kdmall.KdRetData;
import com.yuaoq.yabiz.weixin.app.base.AppSetting;
import com.yuaoq.yabiz.weixin.app.message.AppXmlMessages;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.app.user.SessionKey;
import com.yuaoq.yabiz.weixin.app.user.Users;
import com.yuaoq.yabiz.weixin.common.decrypt.AesException;
import com.yuaoq.yabiz.weixin.common.decrypt.MessageDecryption;
import com.yuaoq.yabiz.weixin.common.decrypt.SHA1;
import com.yuaoq.yabiz.weixin.common.message.XmlMessageHeader;
import javolution.util.FastMap;
import org.ofbiz.base.util.RequestUtil;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by changsy on 2018/4/18.
 */
@RestController
@RequestMapping(path = "/api/wx/v1")
public class WxControllerV1 {
    
    @Autowired
    private DuplicatedMessageChecker duplicatedMessageChecker;
    
    public void setDuplicatedMessageChecker(DuplicatedMessageChecker duplicatedMessageChecker) {
        this.duplicatedMessageChecker = duplicatedMessageChecker;
    }
   
   /* 退货申请
   一张代金券即将到期
    您发起的组团拆红包，由于人数不足，组团失败
   领取了好友赠送的礼品
    礼品尚未被领取，
    心愿单尚未实现，即将到期
    订单消耗了积分
    积分已退还到您的星积分账户*/
    
    /**
     * 定时发送template message
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/template/add/async", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addMessageAsync(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        String touser = request.getParameter("touser");
        String page = request.getParameter("page");
        String form_id = request.getParameter("form_id");
        String data = request.getParameter("data");
        String color = request.getParameter("color");
        String emphasis_keyword = request.getParameter("emphasis_keyword");
        String sendType = request.getParameter("sendType");
        String sendTimeStr = request.getParameter("sendTime");//yyyy-MM-dd HH:mm:ss
        String objectValueId = request.getParameter("objectValueId");
        String productId = request.getParameter("productId");
//        System.out.print("从小程序获取的商品Id"+productId);
        Timestamp sendTime =null;
        try {
            sendTime = new Timestamp(Long.parseLong(sendTimeStr));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            if (UtilValidate.isNotEmpty(sendType)) {
//                如果sendType类型是：SECKILL_NOTIFY
                GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", sendType)));
                if (UtilValidate.isNotEmpty(config)) {
                    String template_id = config.getString("templateId");
                    GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                    String partyId = userLogin.getString("partyId");
                    //sendType："1"不发
                    resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", touser, "template_id", template_id, "page", page, "form_id", form_id, "data", data, "color", color, "emphasis_keyword", emphasis_keyword, "sendType", "1", "partyId", partyId, "objectValueId", objectValueId,"sendTime",sendTime,"productId",productId));
                } else {
                    resultData.put("retCode", 0);
                    resultData.put("message", "消息类型对应的template为空");
                }
                
            } else {
                resultData.put("retCode", 0);
                resultData.put("message", "消息类型不能为空");
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "错误：" + e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "错误：" + e.getMessage());
        }
        
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
        
    }
    
    /**
     * 实时发送template message
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/template/add/sync", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addMessage(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        String touser = request.getParameter("touser");
        String page = request.getParameter("page");
        String form_id = request.getParameter("form_id");
        String data = request.getParameter("data");
        String color = request.getParameter("color");
        String emphasis_keyword = request.getParameter("emphasis_keyword");
        String sendType = request.getParameter("sendType");
        String objectValueId = request.getParameter("objectValueId");
        
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            if (UtilValidate.isNotEmpty(sendType)) {
                GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", sendType)));
                if (UtilValidate.isNotEmpty(config)) {
                    String template_id = config.getString("wxLiteTemplateId");
                    GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginName));
                    String partyId = userLogin.getString("partyId");
                   
                    resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", touser, "template_id", template_id, "page", page, "form_id", form_id, "data", data, "color", color, "emphasis_keyword", emphasis_keyword, "sendType", "0", "partyId", partyId, "objectValueId", objectValueId));
    
                    //小程序发送礼品、礼品领取成功通知（赠送人）
                   
                    
                } else {
                    resultData.put("retCode", 0);
                    resultData.put("message", "消息类型对应的template为空");
                }
            } else {
                resultData.put("retCode", 0);
                resultData.put("message", "消息类型不能为空");
            }
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "错误：" + e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "错误：" + e.getMessage());
        }
        
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
        
    }
    
    
    /**
     * wx.login后
     * 根据code获取用户的sessionKey，openid,unionid
     * 如果用户unionId在ico中已经存在则返回用户信息
     * 如果用户unionId在ICO中不存在，则调用CRM根据unionID获取用户信息的接口
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/getUserInfoByCode", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> wxLogin(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultData = FastMap.newInstance();
        String code = request.getParameter("code");
        String mallId = request.getParameter("mallId");
        Map map = FastMap.newInstance();
        SessionKey sessionKey = Users.defaultUsers().code2Session(code);
        String openId = sessionKey.getOpenId();
        String unionId = sessionKey.getUnionid();
//        request.getSession(true).setAttribute("sessionKey",sessionKey.getSessionKey());
//        String unionId = RandomStringGenerator.getRandomStringByLength(16);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            //1、获取用户的信息
           
//            System.out.println("openId = " + openId);
            if (UtilValidate.isNotEmpty(openId)) {
                List<GenericValue> persons = delegator.findByAnd("Person", UtilMisc.toMap("wxAppOpenId", openId));
                if (UtilValidate.isNotEmpty(persons)) {
                    GenericValue person = persons.get(0);
                    map = person.toMap();
//                    System.out.println("person = " + person);
                } else {
                    if(UtilValidate.isNotEmpty(unionId)) {
                        //如果没有根据unionId获取用户
                        Map<String, Object> result = dispatcher.runSync("kaide-getMemberIdByUnionId", UtilMisc.toMap("unionid", unionId));
                        if (ServiceUtil.isError(result)) {
                            resultData.put("retCode", 0);
                            resultData.put("message", "根据unionid获取crm信息错误");
                        }
                        KdRetData retData = new Gson().fromJson((String) result.get("result"), KdRetData.class);
                        if ("22001".equalsIgnoreCase(retData.getResult())) {
                            //获取成功，{"result":22001,"msg":"用户ID获取成功","data":{"memberID":"111","token":"1ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f25077"}}
                            String memberId = (String) retData.getData().get("memberID");
                            String token = (String) retData.getData().get("token");
        
                            //如果没有根据unionId获取用户
                            result = dispatcher.runSync("kaide-userInfo", UtilMisc.toMap("member_id", memberId, "mall_id", mallId, "token", token));
                            retData = new Gson().fromJson((String) result.get("result"), KdRetData.class);
                            if ("1".equalsIgnoreCase(retData.getResult())) {
                                /* {"result":1,"msg":"用户信息获取成功","data":{"memberNo":"1D3A0E","nickName":"Damos","cardType":"Star","integral":100,"sex":1, "head_img_url":"http://member.capitaland.com.cn/api/member/headimg.ashx?fp=100&mt=get_mem_head_img&uMem_id=2459"}}*/
                                //创建新用户
                                Map dataMap = retData.getData();
                                String phone = (String) dataMap.get("phone");
                                String sex = (String) dataMap.get("sex");
                                String nickName = (String) dataMap.get("nickName");
                                String headImgUrl = (String) dataMap.get("head_img_url");
                                if (!UtilValidate.isEmpty(phone)) {
                                    Map<String, Object> partyData = dispatcher.runSync("partyPersonRegisterOrLogin", UtilMisc.toMap("memberId", memberId, "phone", phone, "sex", sex, "nickName", nickName, "headImgUrl", headImgUrl, "token", token, "password", "who123","isNewCust", "N", "unionId", unionId,"wxAppOpenId",openId,"mallId",mallId));
                                    if (ServiceUtil.isError(partyData)) {
                                        resultData.put("retCode", 0);
                                        resultData.put("message", "创建用户基本信息失败");
                                        String partyId = (String) partyData.get("partyId");
                                        GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyId));
                                        map = person.toMap();
                                    }
                                } else {
                                    resultData.put("retCode", 0);
                                    resultData.put("message", "获取用户基本信息失败没有手机号码");
                                }
            
                            }
                        }
                    }
                }
                
            }
           
            resultData.put("person", map);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
        }
        map.put("openId", openId);
        map.put("unionId", unionId);
        map.put("sessionKey", sessionKey.getSessionKey());
        resultData.put("unionId", sessionKey.getUnionid() == null ? unionId : sessionKey.getUnionid());
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
        
    }
    
    
    /**
     * wx.获取经纬度之后获取店铺Mall
     * 根据code获取用户的sessionKey，openid,unionid
     *
     * @param request
     * @param response
     * @param message
     * @return
     */
    @RequestMapping(value = "/getMallByLan", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getMallByLan(HttpServletRequest request, HttpServletResponse response, Message message, JwtAuthenticationToken token) {
        Map<String, Object> resultData = FastMap.newInstance();
        resultData.put("retCode", 1);
        String lng = request.getParameter("lng");
        String lat = request.getParameter("lat");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            Map<String, Object> resultData1 = dispatcher.runSync("kaide-getMallByLan", UtilMisc.toMap("lng", lng, "lat", lat));
            if (ServiceUtil.isError(resultData1)) {
                resultData.put("retCode", 0);
                resultData.put("message", "错误：" + ServiceUtil.getErrorMessage(resultData));
            } else {
                String result = (String) resultData1.get("result");
                KdRetData kdRetData = new Gson().fromJson(result, KdRetData.class);
                Map<String, Object> data = kdRetData.getData();
                if (UtilValidate.isNotEmpty(data)) {
                    if (UtilValidate.isNotEmpty(data.get("near_mall"))) {
                        resultData.put("near_mall", data.get("near_mall"));
                    } else {
                        resultData.put("near_mall", "57");
                    }
                }
            }
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "错误：" + e.getMessage());
        }
        resultData.put("result", resultData.get("result"));
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
        
    }
    
    
    /**
     * 获取小程序的formId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/getFormId", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getFormId(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String, Object> resultData = FastMap.newInstance();
        resultData.put("retCode", 1);
        String formId = request.getParameter("formId");
        String openId = request.getParameter("openId");
        Delegator delegator = (Delegator)request.getAttribute("delegator");
         try {
            GenericValue wxLiteForm = delegator.makeValue("WxLiteForm");
            wxLiteForm.set("formId",formId);
            wxLiteForm.set("openId",openId);
            wxLiteForm.create();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "错误：" + e.getMessage());
        }
        resultData.put("result", resultData.get("result"));
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
        
    }
    
    
    /**
     * 微信授权
     *
     * @param request
     * @param response
     * @param message
     * @return
     */
    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> wxAuth(HttpServletRequest request, HttpServletResponse response, Message message, JwtAuthenticationToken token) {
        Map<String, Object> resultData = FastMap.newInstance();
        resultData.put("retCode", 1);
        String lng = request.getParameter("lng");
        String lat = request.getParameter("lat");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            resultData = dispatcher.runSync("kaide-getMallByLan", UtilMisc.toMap("lng", lng, "lat", lat));
        } catch (GenericServiceException e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", "错误：" + e.getMessage());
        }
        
        resultData.put("result", resultData.get("result"));
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
        
    }
    
    /**
     * 小程序回调接口
     *
     * @return
     */
    @RequestMapping("/app")
    @ResponseBody
    public String mp(HttpServletRequest request, HttpServletResponse response) {
        
        String signature = request.getParameter("signature");
        String msg_signature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");
        String encrypt_type = request.getParameter("encrypt_type");
        String content = null;
        try {
            content = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        AppSetting appSetting = AppSetting.defaultSettings();
        try {
            if (!SHA1.getSHA1(appSetting.getToken(), timestamp, nonce).equals(signature)) {
                
                return "invalid request.";
            }
        } catch (AesException e) {
            
            return "invalid request.";
        }
        
        if (!StringUtils.isEmpty(echostr)) {
            return echostr;
        }
        
        XmlMessageHeader xmlRequest = null;
        if ("aes".equals(encrypt_type)) {
            try {
                MessageDecryption messageDecryption = new MessageDecryption(appSetting.getToken(), appSetting.getAesKey(), appSetting.getAppId());
                xmlRequest = AppXmlMessages.fromXml(messageDecryption.decrypt(msg_signature, timestamp, nonce, content));
            } catch (AesException e) {
            }
        } else {
            xmlRequest = AppXmlMessages.fromXml(content);
        }
        
        dispatch(xmlRequest);
        
        return "";
    }
    
    /**
     * 具体业务逻辑
     *
     * @param xmlRequest
     */
    private void dispatch(XmlMessageHeader xmlRequest) {
        if (!duplicatedMessageChecker.isDuplicated(xmlRequest.getFromUser() + xmlRequest.getCreateTime().getTime())) {
            //如果有需要可以调用客服接口或者模板消息接口发送消息给用户
            //Message message = new Message();
            //Templates.defaultTemplates().send(message);
            
            //CareMessages.defaultCareMessages().text(xmlRequest.getFromUser(), "Hello!");
            //CareMessages.defaultCareMessages().image(xmlRequest.getFromUser(), "image_media_id");
        } else {
        
        }
        
    }
    
    
    private XmlMessageHeader qyDispatch(XmlMessageHeader xmlRequest) {
        //添加处理逻辑
        
        //需要同步返回消息（被动响应消息）给用户则构造一个XmlMessageHeader类型，比较鸡肋，因为处理逻辑如果比较复杂响应太慢会影响用户感知，建议直接返回null；
        //如果有消息需要发送给用户则可以调用主动消息发送接口进行异步发送
        return null;
    }
    
    
}
