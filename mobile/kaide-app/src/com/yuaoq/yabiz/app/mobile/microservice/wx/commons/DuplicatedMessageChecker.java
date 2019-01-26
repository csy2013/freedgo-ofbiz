package com.yuaoq.yabiz.app.mobile.microservice.wx.commons;

/**
 * Created by exizhai on 11/15/2015.
 */
public interface DuplicatedMessageChecker {

    boolean isDuplicated(String msgKey);

}
