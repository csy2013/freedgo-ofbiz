package com.qihua.ofbiz.weibo;


import com.qihua.ofbiz.weibo.weibo4j.Oauth;
import com.qihua.ofbiz.weibo.weibo4j.Users;
import com.qihua.ofbiz.weibo.weibo4j.http.AccessToken;
import com.qihua.ofbiz.weibo.weibo4j.model.User;
import com.qihua.ofbiz.weibo.weibo4j.model.WeiboException;
import com.qihua.ofbiz.weibo.weibo4j.util.BareBonesBrowserLaunch;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.TokenProcessor;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;


/**
 * Created by gss 2016-4-22 14:46:38
 */
public class WeiBoEvents {

    public static final String module = WeiBoEvents.class.getName();

    /**
     * 新浪微博授权登陆注册账号
     *
     * @param request
     * @param response
     * @return
     */
    public static String WeiboRegister(HttpServletRequest request, HttpServletResponse response) throws WeiboException, IOException {
        Oauth oauth = new Oauth();
        BareBonesBrowserLaunch.openURL(oauth.authorize("code", null, "all"));
     /*   System.out.println(oauth.authorize("code",null,null));
        System.out.print("Hit enter when it's done.[Enter]:");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String code = br.readLine();
        try{
            System.out.println(oauth.getAccessTokenByCode(code));
        } catch (WeiboException e) {
            if(401 == e.getStatusCode()){
               *//* Log.logInfo("Unable to get the access token.");*//*
            }else{
                e.printStackTrace();
            }
        }*/
        return "success";
    }

    /**
     * 新浪微博授权登陆注册账号-回调方法
     *
     * @param request
     * @param response
     * @return
     */
    public static String afterWeiboLogin(HttpServletRequest request, HttpServletResponse response) throws IOException, WeiboException, GenericEntityException {
        Debug.log("I'm coming++++++++++++++++++++++++++++++++++++++++++" + request.getParameterMap().toString());
        String fromUrl = request.getParameter("fromUrl").replaceAll("_365_", "#").replaceAll("车展", URLEncoder.encode("车展", "utf-8"));
        Debug.log("fromUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + fromUrl);
        String toUrl = request.getParameter("toUrl").replaceAll("_365_", "#").replaceAll("车展", URLEncoder.encode("车展", "utf-8"));
        Debug.log("toUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl);
        String toUrl1 = request.getParameter("toUrl1").replaceAll("_365_", "#").replaceAll("车展", URLEncoder.encode("车展", "utf-8"));
        Debug.log("toUrl1++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl1);
        Oauth oauth = new Oauth();
        String code = request.getParameter("code");
        AccessToken accessTokenObj = oauth.getAccessTokenByCode(code);
        String accessToken = null, Uid = null;
        String tokenExpireIn = null;
        if ("".equals(accessTokenObj.getAccessToken())) {
//                我们的网站被CSRF攻击了或者用户取消了授权
//                做一些数据统计工作
            System.out.print("没有获取到响应参数");
        } else {
            accessToken = accessTokenObj.getAccessToken();
            tokenExpireIn = accessTokenObj.getExpireIn();
            Uid = accessTokenObj.getUid();
            request.getSession().setAttribute("access_token", accessToken);
            request.getSession().setAttribute("token_expirein", tokenExpireIn);
            Users um = new Users();
            um.client.setToken(accessToken);
            User user = um.showUserById(Uid);
            //昵称
            String nickName = user.getScreenName();
            //大头像
            String avatarLarge = user.getAvatarLarge();
            //性别 m--男，f--女,n--未知
            String gender = user.getGender();
            if ("m".equals(gender)) {
                gender = "1";
            } else if ("f".equals(gender)) {
                gender = "2";
            } else if ("n".equals(gender)) {
                gender = "1";
            }
            Delegator delegator = (Delegator) request.getAttribute("delegator");
            GenericValue userLogin = null;
            try {
                userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", Uid));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(userLogin)) {
                if (UtilValidate.isEmpty(userLogin.get("token"))) {
                    String token = new TokenProcessor().generateToken(userLogin.getString("userLoginId"), true);
                    userLogin.set("token", token);
                    delegator.store(userLogin);
                }
                if (toUrl1.contains("?")) {
                    toUrl1 = toUrl1 + "&uid=" + Uid + "&authToken=" + userLogin.get("token");
                } else {
                    toUrl1 = toUrl1 + "?uid=" + Uid + "&authToken=" + userLogin.get("token");
                }
                Debug.log("toUrl1++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl1);
                response.sendRedirect(toUrl1);
                return "success";
            } else {
                if (toUrl.contains("?")) {
                    toUrl = toUrl + "&uid=" + Uid + "&nickname=" + URLEncoder.encode(nickName) + "&sex=" + gender + "&headimgurl=" + URLEncoder.encode(avatarLarge);
                } else {
                    toUrl = toUrl + "?uid=" + Uid + "&nickname=" + URLEncoder.encode(nickName) + "&sex=" + gender + "&headimgurl=" + URLEncoder.encode(avatarLarge);
                }
                Debug.log("toUrl++++++++++++++++++++++++++++++++++++++++++++++++:" + toUrl);
                response.sendRedirect(toUrl);
                return "success";
            }
        }
        response.sendRedirect(fromUrl);
        return "error";
    }
}
