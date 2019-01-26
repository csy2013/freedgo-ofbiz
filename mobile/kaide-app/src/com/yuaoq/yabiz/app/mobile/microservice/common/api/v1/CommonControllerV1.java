package com.yuaoq.yabiz.app.mobile.microservice.common.api.v1;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by changsy on 2018/4/21.
 */
@RestController
@RequestMapping(path = "/api/common/v1")
public class CommonControllerV1 {
    @RequestMapping(value = "/getAssociatedStateList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createOrder(HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> resultData = ServiceUtil.returnSuccess();
        String countryGeoId = request.getParameter("countryGeoId");
        String listOrderBy = request.getParameter("listOrderBy");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            resultData = dispatcher.runSync("getAssociatedStateListJson", UtilMisc.toMap("countryGeoId",countryGeoId,"listOrderBy",listOrderBy));
        } catch (GenericServiceException e) {
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            e.printStackTrace();
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
    
    @RequestMapping(value = "/heartbeat", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> heartbeat(HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> resultData = ServiceUtil.returnSuccess();
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
}
