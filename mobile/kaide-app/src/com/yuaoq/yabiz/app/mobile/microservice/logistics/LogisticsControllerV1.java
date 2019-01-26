package com.yuaoq.yabiz.app.mobile.microservice.logistics;

import com.google.gson.Gson;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.kuaidi100.PostOrder;
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
 * Created by changsy on 2018/4/29.
 */
@RestController
@RequestMapping(value = "/api/logistics/v1")
public class LogisticsControllerV1 {
    
    /**
     * 查询物流信息
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/kd100/api", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createOrder(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String,Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String orderId = request.getParameter("orderId");
      
        try {
            List<GenericValue> orderDeliverys = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("orderId", orderId));
            if(UtilValidate.isNotEmpty(orderDeliverys)){
               GenericValue orderDelivery =  orderDeliverys.get(0);
               String logisticsNumber1 = orderDelivery.getString("logisticsNumber1");
               String deliveryCompany = orderDelivery.getString("deliveryCompany");
               String result =  PostOrder.query(deliveryCompany,logisticsNumber1);
               resultData.put("result",new Gson().fromJson(result,Map.class));
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
        
    }
    
}
