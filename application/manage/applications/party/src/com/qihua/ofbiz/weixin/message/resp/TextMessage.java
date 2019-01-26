package com.qihua.ofbiz.weixin.message.resp;

/**
 * Created with intellij.
 * User: changsy
 * Date: 14-5-12
 */
public class TextMessage extends BaseMessage {
    // 回复的消息内容
    private String Content;

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }
}