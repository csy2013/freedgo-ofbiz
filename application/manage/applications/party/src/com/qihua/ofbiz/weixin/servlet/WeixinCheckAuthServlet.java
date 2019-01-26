package com.qihua.ofbiz.weixin.servlet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Created with intellij.
 * User: changsy
 * Date: 14-5-12
 */
public class WeixinCheckAuthServlet extends HttpServlet {

    public static final String module = WeixinServlet.class.getName();

    /**
     * 确认请求来自微信服务器
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * 绑定微信
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String userLoginId = request.getParameter("userLoginId");
        String fromUrl = request.getParameter("fromUrl");
        String toUrl = request.getParameter("toUrl");
        if (UtilValidate.isEmpty(userLoginId)) {
            request.setAttribute("error", "登陆账号不能为空");
            return;
        }
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isEmpty(userLogin)) {
            request.setAttribute("error", "该用户不存在");
            return;
        }
        if (UtilValidate.isEmpty(fromUrl)) {
            request.setAttribute("error", "缺少参数fromUrl");
            return;
        }
        if (UtilValidate.isEmpty(toUrl)) {
            request.setAttribute("error", "缺少参数toUrl");
            return;
        }
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appId", "");
        String authBackUrl = UtilProperties.getPropertyValue("weixin.properties", "authBackUrlBind", "");
        authBackUrl = URLEncoder.encode(authBackUrl + "?userLoginId="+userLoginId+"&fromUrl=" + fromUrl.replaceAll("&", "%26") + "&toUrl=" + toUrl.replaceAll("&", "%26"));

        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=" + appId +
                "&redirect_uri=" +
                authBackUrl +
                "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
        Debug.log("wapUrl++++++++++++++++++++++++++++++++++++++:" + url);
        response.sendRedirect(url);

        return;
    }
}
