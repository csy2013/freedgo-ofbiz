package com.yuaoq.yabiz.app.mobile.microservice.coverPoint;

import com.yuaoq.yabiz.app.mobile.microservice.article.api.v1.ArticleV1Controller;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCart;
import com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCartEvents;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.TransactionUtil;
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
 * Created by changsy on 2018/5/2.
 */
@RestController
@RequestMapping(path = "/api/coverPoint/v1")
public class WxLiteCoverPointControllerV1 {
    public static final String module = WxLiteCoverPointControllerV1.class.getName();
    /**
     * 创建埋点
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/saveCoverPoint", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> saveCoverPoint(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) {
        Map<String,Object> resultData = ServiceUtil.returnSuccess();
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        String eventCode = request.getParameter("event_code");
        String lng = request.getParameter("lng");
        String lat = request.getParameter("lat");
        String brand = request.getParameter("brand");
        String model = request.getParameter("model");
        String version = request.getParameter("version");
        String platform = request.getParameter("platform");
        String unionId = request.getParameter("unionId");
        String memberId = request.getParameter("member_id");
        String trigger = request.getParameter("trigger");
        String pageCode = request.getParameter("page_code");
        String buttonCode = request.getParameter("button_code");
        String serviceUrl = request.getParameter("service_url");
        String servicePostData = request.getParameter("service_post_data");
        String channelCode = request.getParameter("channel_code");
        String platformCode = request.getParameter("platform_code");
    
        String productId = request.getParameter("product_id");
        String valueId = request.getParameter("value_id");
        String productName = request.getParameter("product_name");
        String productPrice = request.getParameter("product_price");
        String productType = request.getParameter("product_type");
        String productTotalPrice = request.getParameter("product_total_price");
        String productPromoPrice = request.getParameter("product_promo_price");
        String grantTotal = request.getParameter("grant_total");
        String orderId = request.getParameter("order_id");
        String orderType = request.getParameter("order_type");
        String promoCodeId = request.getParameter("promo_code_id");
        String registEntrance = request.getParameter("regist_entrance");
        String mallId = request.getParameter("mall_id");
        
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin",UtilMisc.toMap("userLoginId",loginName));
            GenericValue pointData = delegator.makeValue("WxLiteCoverPointData");
            pointData.set("eventCode",eventCode);
            pointData.set("lng",lng);
            pointData.set("lat",lat);
            pointData.set("brand",brand);
            pointData.set("model",model);
            pointData.set("vesion",version);
            pointData.set("platform",platform);
            pointData.set("unionId",unionId);
            pointData.set("memberId",memberId);
            pointData.set("partyId",userLogin.getString("partyId"));
            pointData.set("trigger1",trigger);
            pointData.set("pageCode",pageCode);
            pointData.set("buttonCode",buttonCode);
            pointData.set("serviceUrl",serviceUrl);
            pointData.set("servicePostData",servicePostData);
            pointData.set("channelCode",channelCode);
            pointData.set("platformCode",platformCode);
            pointData.set("createDate",UtilDateTime.nowTimestamp());
            pointData.set("mallId",mallId);
            pointData.set("productId",productId);
            pointData.set("valueId",valueId);
            pointData.set("productName",productName);
            pointData.set("productPrice",productPrice);
            pointData.set("productType",productType);
            pointData.set("productTotalPrice",productTotalPrice);
            pointData.set("productPromoPrice",productPromoPrice);
            pointData.set("grantTotal",grantTotal);
            pointData.set("orderId",orderId);
            pointData.set("orderType",orderType);
            pointData.set("promoCodeId",promoCodeId);
            pointData.set("registEntrance",registEntrance);
            
            pointData.setNextSeqId();
            pointData.create();
            TransactionUtil.commit(beganTransaction);
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
    
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
        
    }
}
