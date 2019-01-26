package com.yuaoq.yabiz.weixin.app.template;

import com.yuaoq.yabiz.weixin.app.AppWxClientFactory;
import com.yuaoq.yabiz.weixin.app.base.AppSetting;
import com.yuaoq.yabiz.weixin.app.base.WxEndpoint;
import com.yuaoq.yabiz.weixin.common.WxClient;
import com.yuaoq.yabiz.weixin.common.util.JsonMapper;
import javolution.util.FastMap;
import org.ofbiz.service.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 模板消息API
 *
 * Created by Borball on 11/07/2016.
 */
public class Templates {

    private static Logger logger = LoggerFactory.getLogger(Templates.class);

    private WxClient wxClient;

    public static Templates defaultTemplates() {
        return with(AppSetting.defaultSettings());
    }

    public static Templates with(AppSetting appSetting) {
        Templates templates = new Templates();
        templates.setWxClient(AppWxClientFactory.getInstance().with(appSetting));
        return templates;
    }

    public void setWxClient(WxClient wxClient) {
        this.wxClient = wxClient;
    }

    /**
     * 发送模板消息
     *
     * @param message
     */
    public void send(Message message) {
        String sendUrl = WxEndpoint.get("url.template.send");
        String json = JsonMapper.defaultMapper().toJson(message);

        logger.debug("template message, send message: {}", json);
        String response = wxClient.post(sendUrl, json);
        JsonMapper.defaultMapper().json2Map(response);
    }
    
    /**
     * 发送模板消息
     *
     * @param message
     */
    public Map sendWithResult(Message message) {
        Map response = FastMap.newInstance();
        try {
            String sendUrl = WxEndpoint.get("url.template.send");
            String json = JsonMapper.defaultMapper().toJson(message);

            logger.debug("template message, send message: {}", json);
            String wxresponse = wxClient.post(sendUrl, json);
            response = ServiceUtil.returnSuccess(wxresponse) ;
        } catch (Exception e) {
            response= ServiceUtil.returnSuccess(e.getMessage());
        }
        return response;
    }

}
