package com.qihua.ofbiz.weixin.servlet;

import net.sf.json.JSONObject;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.RequestUtil;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Add By AlexYao  2016-4-22 14:23:41
 */
public class WeixinAuthServlet extends HttpServlet {

    public static final String module = WeixinAuthServlet.class.getName();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    /**
     * 微信授权登陆
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fromUrl = req.getParameter("fromUrl");
        String toUrl = req.getParameter("toUrl");
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
        if (UtilValidate.isEmpty(fromUrl)) {
            req.setAttribute("error", "缺少参数fromUrl");
            resp.setStatus(403);
            return;
        }
        if (UtilValidate.isEmpty(toUrl)) {
            req.setAttribute("error", "缺少参数toUrl");
            resp.setStatus(403);
            return;
        }
        String appId = UtilProperties.getPropertyValue("weixin.properties", "appId", "");
        String authBackUrl = UtilProperties.getPropertyValue("weixin.properties", "authBackUrlLogin", "");
        authBackUrl = URLEncoder.encode(authBackUrl + "?fromUrl=" + fromUrl.replaceAll("&", "%26") + "&toUrl=" + toUrl.replaceAll("&", "%26"));

        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=" + appId +
                "&redirect_uri=" +
                authBackUrl +
                "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
        Debug.log("wapUrl++++++++++++++++++++++++++++++++++++++:" + url);
        resp.sendRedirect(url);

        return;


    }
}
