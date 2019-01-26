package com.yuaoq.yabiz.app.security.auth.ajax;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model intended to be used for AJAX based authentication.
 * 
 * @author vladimir.stankovic
 *
 * Aug 3, 2016
 */

public class LoginRequest {
    
    private String memberId;
    private String token;
    private String unionId;
    private String phone;
    private String mall_id;
    private String sex;
    private String nick_name;
    private String head_img_url;
    private String encryptedData;
    private String iv;
    private String encryptedData1;
    private String sessionKey;
    private String openId;
    
    public String getSessionKey() {
        return sessionKey;
    }
    
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
    
    private String iv1;
    public String getIv() {
        return iv;
    }
    
    public void setIv(String iv) {
        this.iv = iv;
    }
    
    public String getEncryptedData() {
        return encryptedData;
    }
    
    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getMall_id() {
        return mall_id;
    }
    
    public void setMall_id(String mall_id) {
        this.mall_id = mall_id;
    }
    
    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }
    
    public String getNick_name() {
        return nick_name;
    }
    
    public void setNick_name(String nick_name) {
        this.nick_name = nick_name;
    }
    
    public String getHead_img_url() {
        return head_img_url;
    }
    
    public void setHead_img_url(String head_img_url) {
        this.head_img_url = head_img_url;
    }
    
    /*@JsonCreator
    public LoginRequest(@JsonProperty("unionId")String unionId, @JsonProperty("phone")String phone,@JsonProperty("mall_id") String mall_id, @JsonProperty("sex")String sex, @JsonProperty("nick_name")String nick_name,@JsonProperty("head_img_url") String head_img_url) {
        this.unionId = unionId;
        this.phone = phone;
        this.mall_id = mall_id;
        this.sex = sex;
        this.nick_name = nick_name;
        this.head_img_url = head_img_url;
    }*/
    
   /* @JsonCreator
    public LoginRequest(@JsonProperty("memberId") String memberId, @JsonProperty("token") String token) {
        this.memberId = memberId;
        this.token = token;
    }
    @JsonCreator
    public LoginRequest(@JsonProperty("unionId") String unionId) {
        this.unionId = unionId;
    }*/
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getUnionId() {
        return unionId;
    }
    
    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }
    
    public String getMemberId() {
        return memberId;
    }
    
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setTokenId(String token) {
        this.token = token;
    }
    
    public String getEncryptedData1() {
        return encryptedData1;
    }
    
    public void setEncryptedData1(String encryptedData1) {
        this.encryptedData1 = encryptedData1;
    }
    
    public String getIv1() {
        return iv1;
    }
    
    public void setIv1(String iv1) {
        this.iv1 = iv1;
    }
    
    public String getOpenId() {
        return openId;
    }
    
    public void setOpenId(String openId) {
        this.openId = openId;
    }
    
    @Override
    public String toString() {
        return "LoginRequest{" +
                "memberId='" + memberId + '\'' +
                ", token='" + token + '\'' +
                ", unionId='" + unionId + '\'' +
                ", phone='" + phone + '\'' +
                ", mall_id='" + mall_id + '\'' +
                ", sex='" + sex + '\'' +
                ", nick_name='" + nick_name + '\'' +
                ", head_img_url='" + head_img_url + '\'' +
                ", encryptedData='" + encryptedData + '\'' +
                ", iv='" + iv + '\'' +
                ", encryptedData1='" + encryptedData1 + '\'' +
                ", sessionKey='" + sessionKey + '\'' +
                ", openId='" + openId + '\'' +
                ", iv1='" + iv1 + '\'' +
                '}';
    }
}
