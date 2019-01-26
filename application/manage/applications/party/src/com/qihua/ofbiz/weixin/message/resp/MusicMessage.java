package com.qihua.ofbiz.weixin.message.resp;

/**
 * Created with intellij.
 * User: changsy
 * Date: 14-5-12
 */
public class MusicMessage extends BaseMessage {
    // 音乐
    private Music Music;

    public Music getMusic() {
        return Music;
    }

    public void setMusic(Music music) {
        Music = music;
    }
}
