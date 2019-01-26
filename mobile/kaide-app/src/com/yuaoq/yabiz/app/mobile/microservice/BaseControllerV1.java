package com.yuaoq.yabiz.app.mobile.microservice;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by changsy on 2018/4/7.
 */
public class BaseControllerV1 {
    
    protected void getResultInfo(HttpServletRequest request, Map<String, Object> resultData, String result) {
        if (result.equals("error")) {
            String message = (String) request.getAttribute("_ERROR_MESSAGE_");
            String eventMessage = (String) request.getAttribute("_ERROR_MESSAGE_");
            resultData.put("message",message+eventMessage);
            resultData.put("retCode",0);
        } else {
            resultData.put("message", result);
            resultData.put("retCode", 1);
        }
    }
}
