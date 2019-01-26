package com.yuaoq.yabiz.mobile.services.kdmall;

import java.util.Map;

/**
 * Created by changsy on 2018/4/23.
 */
public class KdRetData {
    
    private String msg;
    private String result;
    private Map<String,Object> data;
    
    public String getMsg() {
        return msg;
    }
    
    public void setMsg(String msg) {
        this.msg = msg;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
