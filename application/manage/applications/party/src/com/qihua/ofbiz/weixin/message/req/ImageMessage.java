package com.qihua.ofbiz.weixin.message.req;

/**
 * Created with intellij.
 * User: changsy
 * Date: 14-5-12
 */
public class ImageMessage extends BaseMessage{
    // 图片链接
    private String PicUrl;

    public String getPicUrl() {
        return PicUrl;
    }

    public void setPicUrl(String picUrl) {
        PicUrl = picUrl;
    }
}
