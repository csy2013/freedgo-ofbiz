package com.yuaoq.yabiz.weixin.app.message;

import com.yuaoq.yabiz.weixin.app.event.UserEnterSession;
import com.yuaoq.yabiz.weixin.common.event.*;
import com.yuaoq.yabiz.weixin.common.exception.WxRuntimeException;
import com.yuaoq.yabiz.weixin.common.message.XmlMessageHeader;
import com.yuaoq.yabiz.weixin.common.request.*;
import com.yuaoq.yabiz.weixin.common.util.XmlObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 小程序回调消息和事件
 * @borball on 12/29/2016.
 */
public class AppXmlMessages {

    private static Logger logger = LoggerFactory.getLogger(AppXmlMessages.class);

    public static XmlMessageHeader fromXml(String xml) {
        try {
            XmlMessageHeader xmlRequest = XmlObjectMapper.defaultMapper().fromXml(xml, XmlMessageHeader.class);
            switch (xmlRequest.getMsgType()) {
                case text:
                    return XmlObjectMapper.defaultMapper().fromXml(xml, TextRequest.class);
                case image:
                    return XmlObjectMapper.defaultMapper().fromXml(xml, ImageRequest.class);
                case event:
                    return toEvent(xml);
                default:
                    logger.warn("xml to bean failed, unknown message type {}.", xmlRequest.getMsgType());
                    throw new WxRuntimeException(999, "xml to bean failed, unknown message type " + xmlRequest.getMsgType());
            }
        } catch (IOException e) {
            logger.error("xml to message request failed", e);
            throw new WxRuntimeException(999, "xml to message request failed," + e.getMessage());
        }
    }

    private static EventRequest toEvent(String xml) {
        try {
            EventRequest eventRequest = XmlObjectMapper.defaultMapper().fromXml(xml, EventRequest.class);
            switch (eventRequest.getEventType()) {
                case user_enter_tempsession:
                    return XmlObjectMapper.defaultMapper().fromXml(xml, UserEnterSession.class);
                default:
                    logger.warn("xml to event, unknown event type {}.", eventRequest.getEventType());
                    throw new WxRuntimeException(999, "xml to bean event, unknown event type " + eventRequest.getEventType());
            }
        } catch (IOException e) {
            logger.error("xml to event failed", e);
            throw new WxRuntimeException(999, "xml to event failed," + e.getMessage());
        }
    }
}
