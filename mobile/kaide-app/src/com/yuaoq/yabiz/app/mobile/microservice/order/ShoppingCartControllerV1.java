package com.yuaoq.yabiz.app.mobile.microservice.order;

import com.yuaoq.yabiz.app.mobile.microservice.BaseControllerV1;
import javolution.util.FastMap;
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
 * Created by changsy on 2018/4/7.
 */
@RestController
@RequestMapping(value = "/api/cart/v1")
public class ShoppingCartControllerV1 extends BaseControllerV1 {
    
    public static String module = ShoppingCartControllerV1.class.getName();
    
    @RequestMapping(value = "/addToCart", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addToCart(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultData = FastMap.newInstance();
        String result = com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCartEvents.addToCart(request, response);
        getResultInfo(request, resultData, result);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    
    @RequestMapping(value = "/getCart", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getCart(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultData = FastMap.newInstance();
        String result = com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCartEvents.getShoppingCart(request, response);
        getResultInfo(request, resultData, result);
        resultData = (Map<String, Object>) request.getAttribute("resultData");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
    
}
