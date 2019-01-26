package com.yuaoq.yabiz.app.mobile.microservice.order.api.v1;

import com.pingplusplus.Pingpp;
import com.pingplusplus.exception.*;
import com.pingplusplus.model.Charge;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/api/pay/v1")
public class PaymentControllerV1 {
    
    @Value("${ping.wxLite.appId}")
    String pingAppId;
    
    @Value(("${ping.wxList.appKey}"))
    String pingAppKey;
    
    @Value("${wx.lite.appId}")
    String wxLiteAppId;
    
    @Value("${ping.rsa.private.path}")
    public String pingRsaPath;
    
    /**
     * 小程序支付调用ping++支付接口
     * Order: 如果是orderGroup：G_开头 ，如果是order:O_
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     */
    @RequestMapping(value="/pingPayForWxLite",method = RequestMethod.POST)
    public   ResponseEntity<Map<String, Object>> createOrder(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        String orderId = request.getParameter("orderId");
        String orderType = request.getParameter("orderType");
        String orderGroupId = request.getParameter("orderGroupId");
        String openId = request.getParameter("openId");
        Delegator delegator = (Delegator)request.getAttribute("delegator");
        //如果是orderGroupId则对应orderGroup表做多商家支付
        BigDecimal totalAmount = BigDecimal.ZERO;
        StringBuffer subjectBuffer = new StringBuffer();
        StringBuffer bodyBuffer = new StringBuffer();
        String pingOrderId = "";
        if(orderType.equals("orderGroup") && UtilValidate.isNotEmpty(orderGroupId)){
            pingOrderId = "G"+orderGroupId+UtilDateTime.nowAsTenString();
           List<GenericValue> orderRels =  delegator.findByAnd("OrderGroupOrderRel",UtilMisc.toMap("orderGroupId",orderGroupId));
            List<GenericValue> orders = EntityUtil.getRelated("OrderHeader",orderRels);
            if(UtilValidate.isNotEmpty(orders)){
                for (int i = 0; i < orders.size(); i++) {
                    GenericValue orderHeader = orders.get(i);
                    List<GenericValue> orderItem = orderHeader.getRelated("OrderItem");
                    if(UtilValidate.isNotEmpty(orderItem)){
                        for (int j = 0; j < orderItem.size(); j++) {
                            GenericValue item = orderItem.get(j);
                            subjectBuffer.append(item.getString("itemDescription"));
                            bodyBuffer.append(item.getString("itemDescription"));
                        }
                    }
                    totalAmount = totalAmount.add(orderHeader.getBigDecimal("grandTotal"));
                }
            }
        }
        
        else if(orderType.equals("order") && UtilValidate.isNotEmpty(orderId)){
            pingOrderId = "O"+orderId+UtilDateTime.nowAsTenString();
            GenericValue orderHeader =  delegator.findByPrimaryKey("OrderHeader",UtilMisc.toMap("orderId",orderId));
    
            totalAmount = totalAmount.add(orderHeader.getBigDecimal("grandTotal"));
    
            List<GenericValue> orderItem = orderHeader.getRelated("OrderItem");
            if(UtilValidate.isNotEmpty(orderItem)){
                for (int j = 0; j < orderItem.size(); j++) {
                    GenericValue item = orderItem.get(j);
                    subjectBuffer.append(item.getString("itemDescription"));
                    bodyBuffer.append(item.getString("itemDescription"));
                }
            }
            
        }
        //subject
        String subject = subjectBuffer.toString();
        String body = bodyBuffer.toString();
        if(subject.length()>32){
            subject = subject.substring(0,31);
        }
        if(body.length()>128){
            body = subject.substring(0,127);
        }
        Charge charge =  createCharge("wx_lite",totalAmount,subject,body,pingOrderId,request.getLocalAddr(),openId);
        return Optional.ofNullable(charge).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    

    
    /**
     * 创建 Charge
     *
     * 创建 Charge 用户需要组装一个 map 对象作为参数传递给 Charge.create();
     * map 里面参数的具体说明请参考：https://www.pingxx.com/api#api-c-new
     * @return Charge
     */
    public Charge createCharge(String channel,BigDecimal amount,String subject,String body,String orderNo,String clientIp,String openId) {
        Pingpp.apiKey = pingAppKey;
        // 设置私钥路径，用于请求签名
        Pingpp.privateKeyPath =  this.getClass().getClassLoader().getResource("../"+pingRsaPath).getFile();
        
        
        Charge charge = null;
        Map<String, Object> chargeMap = new HashMap<String, Object>();
        amount = amount.multiply(new BigDecimal(100)).setScale(0,BigDecimal.ROUND_HALF_UP);
        chargeMap.put("amount", amount);//订单总金额, 人民币单位：分（如订单总金额为 1 元，此处请填 100）
        
//        chargeMap.put("amount",1);
        chargeMap.put("currency", "cny");
        chargeMap.put("subject", "test");
        chargeMap.put("body", "test");
        //设置test模式
//        chargeMap.put("livemode",false);
        //订单+时间戳
        chargeMap.put("order_no", orderNo);// 推荐使用 8-20 位，要求数字或字母，不允许其他字符
        chargeMap.put("channel", channel);// 支付使用的第三方支付渠道取值，请参考：https://www.pingxx.com/api#api-c-new
        chargeMap.put("client_ip", "127.0.0.1"); // 发起支付请求客户端的 IP 地址，格式为 IPV4，如: 127.0.0.1
        Map<String, String> app = new HashMap<String, String>();
        
        app.put("id", pingAppId);
//        chargeMap.put("apiKey",pingAppKey);
        chargeMap.put("app", app);
        
        // extra 取值请查看相应方法说明
        chargeMap.put("extra", wxLiteExtra(openId));
        
        try {
            //发起交易请求
            charge = Charge.create(chargeMap);
            // 传到客户端请先转成字符串 .toString(), 调该方法，会自动转成正确的 JSON 字符串
            String chargeString = charge.toString();
            System.out.println(chargeString);
        } catch (APIConnectionException e) {
            e.printStackTrace();
        } catch (ChannelException e) {
            e.printStackTrace();
        } catch (RateLimitException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (APIException e) {
            e.printStackTrace();
        } catch (InvalidRequestException e) {
            e.printStackTrace();
        }
        return charge;
    }
    
    /**
     * 小程序支付的
     * @return
     */
    private Map<String, Object> wxLiteExtra(String openId) {
        Map<String, Object> extra = new HashMap<>();
        // 可选，指定支付方式，指定不能使用信用卡支付可设置为 no_credit 。
//        extra.put("limit_pay", "no_credit");
        // 可选，商品标记，代金券或立减优惠功能的参数。
        // extra.put("goods_tag", "YOUR_GOODS_TAG");
        
        // 必须，用户在商户 appid 下的唯一标识。
        extra.put("open_id", openId);
        
        return extra;
    }
}
