package com.yuaoq.yabiz.app.security.auth.ajax;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.yuaoq.yabiz.app.security.exceptions.AuthMethodNotSupportedException;
import com.yuaoq.yabiz.mobile.services.kdmall.KdMallServices;
import com.yuaoq.yabiz.mobile.services.kdmall.KdRetData;
import com.yuaoq.yabiz.weixin.common.decrypt.AES;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * AjaxLoginProcessingFilter
 * /api/auth/login
 *
 * @author vladimir.stankovic
 * <p>
 * Aug 3, 2016
 */
public class AjaxLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {
    private static Logger logger = LoggerFactory.getLogger(AjaxLoginProcessingFilter.class);
    
    private final AuthenticationSuccessHandler successHandler;
    private final AuthenticationFailureHandler failureHandler;
    
    private final ObjectMapper objectMapper;
    
    public AjaxLoginProcessingFilter(String defaultProcessUrl, AuthenticationSuccessHandler successHandler,
                                     AuthenticationFailureHandler failureHandler, ObjectMapper mapper) {
        super(defaultProcessUrl);
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.objectMapper = mapper;
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        String livemode = request.getParameter("livemode");
        if (UtilValidate.isNotEmpty(livemode) && livemode.equals("test")) {
            return attemptAuthentication1(request, response);
        }
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication method not supported. Request method: " + request.getMethod());
            }
            throw new AuthMethodNotSupportedException("Authentication method not supported");
        }
        String userLoginId = null;
        
        String token = null;
        //获取前端传过来的unionId
        LoginRequest loginRequest = objectMapper.readValue(request.getReader(), LoginRequest.class);
    
        //后台根据unionID获取用户信息
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        List<GenericValue> parties = null;
//        System.out.println("loginRequest = " + loginRequest);
        try {
            //根据unionId获取用户信息
            if(UtilValidate.isNotEmpty(loginRequest.getUnionId())) {
                parties = delegator.findByAnd("PartyAndUserLoginAndPerson", UtilMisc.toMap("unionId", loginRequest.getUnionId()));
            }
            if (UtilValidate.isEmpty(parties)) {
                if(UtilValidate.isNotEmpty(loginRequest.getOpenId())) {
                    parties = delegator.findByAnd("PartyAndUserLoginAndPerson", UtilMisc.toMap("wxAppOpenId", loginRequest.getOpenId()));
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("获取用户基本信息错误");
        }
        
        //判断用户是否存在或者当前用户的unionid是不是为空，为空需要重新获取
        if (UtilValidate.isNotEmpty(parties) && (UtilValidate.isNotEmpty(parties.get(0).getString("unionId")) && UtilValidate.isNotEmpty(parties.get(0).getString("wxAppOpenId")))) {
//            System.out.println("parties = " + parties);
            GenericValue partyAndUserLogin = parties.get(0);
            userLoginId = partyAndUserLogin.getString("userLoginId");
            String partyId =  partyAndUserLogin.getString("partyId");
            GenericValue person = null;
            try {
                person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
                if(UtilValidate.isNotEmpty(person)) {
                    if (!person.getString("nickname").equals(loginRequest.getNick_name())) {
                        person.set("nickname", loginRequest.getNick_name());
                        person.store();
                    }
                }
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            /*LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            //调用CRM根据unionId获取mebmer+Token
            KdRetData user = getMemberIdByUnionId(loginRequest.getUnionId(), dispatcher);
            //
            if (UtilValidate.isEmpty(user) || user.getResult().equals("2")) {
                //接口调用错误
                throw new AuthenticationServiceException("根据unionId获取用户信息接口错误");,
            } else if (user.getResult().equals("22002")) {
                //未找到用户 -》 获取用户基本信息 -》用户登录或注册 -》后台用户注册 - 》返回用户信息
                crmRegisterAndSync(request, loginRequest, delegator, true, partyAndUserLogin.getString("partyId"),loginRequest.getMall_id());
                userLoginId = loginRequest.getPhone();
            } else if (user.getResult().equals("22001")) {
//            memberID":"111","token":"1ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f25077"}}
                userLoginId = loginRequest.getPhone();
                Map<String, Object> data = user.getData();
                token = (String) data.get("token");
                try {
                    GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", partyAndUserLogin.get("partyId")));
                    person.set("member_id", data.get("memberID"));
                    person.store();
                    GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", loginRequest.getPhone()));
                    if (UtilValidate.isNotEmpty(userLogin)) {
                        userLogin.set("lastToken", data.get("token"));
                        userLogin.store();
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }*/
            
            
        } else {
            //可能没有openID或unionId， 或者没有用户
            //没有用户的情况
    
            String encryptedData = loginRequest.getEncryptedData();
            String iv = loginRequest.getIv();
            String session_key = loginRequest.getSessionKey();
            if (UtilValidate.isNotEmpty(encryptedData) && UtilValidate.isNotEmpty(iv) && UtilValidate.isNotEmpty(session_key)) {
                String info = AES.wxDecrypt(encryptedData, session_key, iv);
                Map phoneInfo = new Gson().fromJson(info, Map.class);
                String phoneNumber = (String) phoneInfo.get("phoneNumber");
                loginRequest.setPhone(phoneNumber);
            
                if (UtilValidate.isEmpty(loginRequest.getUnionId())) {
                    String encryptedData1 = loginRequest.getEncryptedData1();
                    String iv1 = loginRequest.getIv1();
                    String info1 = AES.wxDecrypt(encryptedData1, session_key, iv1);
                    Map unionMap = new Gson().fromJson(info1, Map.class);
                    String unionId = (String) unionMap.get("unionId");
                    loginRequest.setUnionId(unionId);
                }
                if (UtilValidate.isEmpty(loginRequest.getPhone())) {
                    throw new AuthenticationServiceException("获取手机号码失败");
                }
    
                if (UtilValidate.isEmpty(loginRequest.getUnionId())) {
                    throw new AuthenticationServiceException("获取unionId失败");
                }
                //小程序获取用户基本信息
                //用户注册或登录接口
                //后台注册用户
                //未找到用户 -》 获取用户基本信息 -》用户登录或注册 -》后台用户注册 - 》返回用户信息
                /*后台用户注册 - 》返回用户信息*/
                //根据unionId获取用户信息
                try {
                    parties = delegator.findByAnd("PartyAndUserLoginAndPerson", UtilMisc.toMap("unionId", loginRequest.getUnionId()));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
                if (UtilValidate.isEmpty(parties)) {
                    try {
                        parties = delegator.findByAnd("PartyAndUserLoginAndPerson", UtilMisc.toMap("wxAppOpenId", loginRequest.getOpenId()));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                }
                String partyId = "";
                if (UtilValidate.isNotEmpty(parties)) {
                    partyId = parties.get(0).getString("partyId");
                }
                crmRegisterAndSync(request, loginRequest, delegator, true, partyId, loginRequest.getMall_id());
                userLoginId = loginRequest.getPhone();
    
            }
        }
        if(UtilValidate.isNotEmpty(userLoginId)) {
            GenericValue userLogin = null;
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isEmpty(userLogin)) {
                throw new AuthenticationServiceException("Username or Password not provided");
            }
    
            UsernamePasswordAuthenticationToken token1 = new UsernamePasswordAuthenticationToken(userLoginId, userLogin.get("currentPassword"));
            return this.getAuthenticationManager().authenticate(token1);
        }else{
            throw new AuthenticationServiceException("获取用户登录信息失败");
        }
    }
    
    private String crmRegisterAndSync(HttpServletRequest request, LoginRequest loginRequest, Delegator delegator, Boolean isNew, String partyId,String mallId) {
        String openId = loginRequest.getOpenId();
        String result = null;
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            Map<String, Object> resultObj = dispatcher.runSync("kaide-userLoginOrRegister", UtilMisc.toMap("phone", loginRequest.getPhone(), "mall_id", loginRequest.getMall_id(), "sex", loginRequest.getSex(), "nick_name", loginRequest.getNick_name(), "head_img_url", loginRequest.getHead_img_url(), "unionid", loginRequest.getUnionId()));
            if (UtilValidate.isNotEmpty(resultObj)) {
//                System.out.println("resultObj = " + resultObj);
                KdRetData retData = new Gson().fromJson((String) resultObj.get("result"), KdRetData.class);
                if ("21001".equals(retData.getResult()) || "21002".equals(retData.getResult())) {
                     String isNewCust = "N";
                    if("21002".equals(retData.getResult())){
                        isNewCust = "Y";
                    }
                    //注册登录成功
                    /*后台用户注册 - 》返回用户信息*/
                    /* {"result":21002,"msg":"用户注册成功","data":{"memberID":"111","token":"1ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f250771ce42f9103bf34da1e8746ebd8f25077"}}*/
                    Map memberData = retData.getData();
                    String memberId = (String) memberData.get("memberID");
                    String token = (String) memberData.get("token");
                    result = token;
                    try {
                        
                        Map<String, Object> partyData = dispatcher.runSync("partyPersonRegisterOrLogin", UtilMisc.toMap("partyId", partyId, "memberId", memberId, "phone", loginRequest.getPhone(), "sex", loginRequest.getSex(), "nickName", loginRequest.getNick_name(), "headImgUrl", loginRequest.getHead_img_url(), "token", token, "password", "who123", "unionId", loginRequest.getUnionId(), "isNewCust", isNewCust, "wxAppOpenId", openId,"mallId",mallId));
                        
                    } catch (GenericServiceException e) {
                        e.printStackTrace();
                        throw new AuthenticationServiceException("系统错误");
                    }
                    
                } else {
                    throw new AuthenticationServiceException("CRM用户登录或注册接口错误");
                }
            } else {
                return null;
            }
            
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    
    public Authentication attemptAuthentication1(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Authentication method not supported. Request method: " + request.getMethod());
            }
            throw new AuthMethodNotSupportedException("Authentication method not supported");
        }
        
        GenericValue user = getUserByCrmToken(request);
        if (UtilValidate.isEmpty(user) || UtilValidate.isEmpty(user.get("userLoginId")) || UtilValidate.isEmpty(user.get("currentPassword"))) {
            throw new AuthenticationServiceException("Username or Password not provided");
        }
        
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.get("userLoginId"), user.get("currentPassword"));
        
        return this.getAuthenticationManager().authenticate(token);
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request, response, failed);
    }
    
    /**
     * 根据token获取用户基本信息
     *
     * @return
     */
    private GenericValue getUserByCrmToken(HttpServletRequest request) {
        
        try {
            Delegator delegator = (Delegator) request.getAttribute("delegator");
            String partyId = request.getParameter("partyId");
            
            //判断用户数据是否已经存在数据库，如果没有新增
            List<GenericValue> parties = delegator.findByAnd("PartyAndUserLoginAndPerson", UtilMisc.toMap("partyId", partyId));
            if (UtilValidate.isNotEmpty(parties)) {
                GenericValue partyAndUserLogin = parties.get(0);
                //做用户模拟登录，生成用户jwts
                return partyAndUserLogin;
            } else {
                //待完成，生成客户数据
            }
//            }
        } catch (Exception e) {
        
        }
        return null;
        
    }
    
    
    /**
     * 根据token获取用户基本信息
     *
     * @param unionId
     * @return
     */
    private KdRetData getMemberIdByUnionId(String unionId, LocalDispatcher dispatcher) {
        //调用crm接口通过unionId获取用户的member_id和token
        Map<String, Object> resultData = null;
        try {
            resultData = dispatcher.runSync("kaide-getMemberIdByUnionId", UtilMisc.toMap("unionid", unionId));
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        
        if (UtilValidate.isNotEmpty(resultData)) {
            KdRetData retData = new Gson().fromJson((String) resultData.get("result"), KdRetData.class);
            return retData;
        } else {
            return null;
        }
        
    }
    
    /**
     * 用户登录或注册
     *
     * @return
     */
    private KdRetData userLoginOrRegister(String phone, String mall_id, String sex, String nick_name, String head_img_url, String unionid) {
        //调用crm接口通过unionId获取用户的member_id和token
        
        Map<String, Object> resultData = KdMallServices.userLoginOrRegister(null, UtilMisc.toMap("phone", phone, "mall_id", mall_id, "sex", sex, "nick_name", nick_name, "head_img_url", head_img_url, "unionid", unionid));
        if (UtilValidate.isNotEmpty(resultData)) {
            KdRetData retData = new Gson().fromJson((String) resultData.get("result"), KdRetData.class);
            return retData;
        } else {
            return null;
        }
        
    }
    
    
}
