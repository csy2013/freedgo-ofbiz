package com.yuaoq.yabiz.app.mobile.microservice.order.api.v1;

import com.google.gson.Gson;
import com.pingplusplus.model.*;
import com.yuaoq.yabiz.weixin.app.template.Message;
import javolution.util.FastMap;
import org.apache.commons.codec.binary.Base64;
import org.ofbiz.base.util.RequestUtil;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by changsy on 2018/4/29.
 */

@RestController
@RequestMapping(path = "/api/ping/v1")
public class PingControllerV1 {

    @Value("${ping.rsa.path}")
    public String pingRsaPath;

    /**
     * ping++回调接口
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/webhooks", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> completePay(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String event = "";
        try {
            event = RequestUtil.convertStreamToString(request.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println("event = " + event);

        Enumeration<String> headerNames = request.getHeaderNames();
        String signatureString = "";
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            if (header.equalsIgnoreCase("X-Pingplusplus-Signature")) {
                signatureString = request.getHeader(header);
            }
        }
        try {
            String path = this.getClass().getClassLoader().getResource("../" + pingRsaPath).getFile();
            PublicKey publicKey = getPubKey(path);
            if (verifyData(event, signatureString, publicKey)) {
//                System.out.println("verifyData =  success");
                // 解析 webhooks 可以采用如下方法
                Object obj = Webhooks.getObject(event);
                if (obj instanceof Charge) {
                    Charge charge = (Charge) obj;
//                    System.out.println("charge = " + charge);
                    String orderNo = charge.getOrderNo();
                    if (charge.getPaid()) {
                        if (orderNo.startsWith("G")) {
                            String orderGroupId = orderNo.substring(1, orderNo.length() - 10);
//                            System.out.println("orderGroupId = " + orderGroupId);
                            List<GenericValue> orderRels = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderGroupId", orderGroupId));
                            GenericValue orderGroup = delegator.findByPrimaryKey("OrderGroup", UtilMisc.toMap("orderGroupId", orderGroupId));
                            String orderGroupType = orderGroup.getString("orderGroupType");
                            //支付成功回调
                            if (UtilValidate.isNotEmpty(orderRels)) {
                                for (int i = 0; i < orderRels.size(); i++) {
                                    GenericValue order = orderRels.get(i);
                                    String orderId = order.getString("orderId");
                                    //如果orderId對應的orderPaymentRef 的狀態則不用再調？？
                                    List<GenericValue> payments = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
                                    if (UtilValidate.isNotEmpty(payments)) {
                                        GenericValue payment = payments.get(0);
                                        String statusId = payment.getString("statusId");
                                        if (!"PAYMENT_RECEIVED".equals(statusId)) {
                                            dispatcher.runSync("processPaymentCallback", UtilMisc.toMap(UtilMisc.toMap("orderId", orderId, "transactionId", charge.getTransactionNo(), "totalFee", charge.getAmount(), "discount", 0, "status", "success", "chargeId", charge.getId())));

                                        }
                                    }
                                }
                                if (UtilValidate.isNotEmpty(orderGroupId)) {
                                    dispatcher.runSync("completePay", UtilMisc.toMap("orderGroupId", orderGroupId));
                                }

                                 /*if("ORDER_GIFT".equals(orderGroupType)){
                                     //赠送商品类型订单
                                     resultData= this.handleGiftOrder(orderGroupId,delegator);
        
                                 }else{
                                     //处理其他类型订单
                                 }*/

                            }
                        } else if (orderNo.startsWith("O")) {
                            String orderId = orderNo.substring(1, orderNo.length() - 10);
//                            System.out.println("orderId = " + orderId);
                            //支付成功回调
                            dispatcher.runSync("processPaymentCallback", UtilMisc.toMap(UtilMisc.toMap("orderId", orderId, "transactionId", charge.getTransactionNo(), "totalFee", charge.getAmount(), "discount", 0, "status", "success", "chargeId", charge.getId())));
                            //
//                             找到对应的orderGroup
                            List<GenericValue> orderGorupRels = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderId", orderId));
                            if (UtilValidate.isNotEmpty(orderGorupRels)) {
                                GenericValue groupRel = orderGorupRels.get(0);
                                String orderGroupId = groupRel.getString("orderGroupId");
                                if (UtilValidate.isNotEmpty(orderGroupId)) {
                                    dispatcher.runSync("completePay", UtilMisc.toMap("orderGroupId", orderGroupId));
                                }
                            }
                        }

                    }
                    //发送小程序模板消息
                   /* delegator.findByAnd("OrderPaymentPreference",UtilMisc.toMap(""))*/

                } else if (obj instanceof Refund) {
                    //如果事件等于refund退款动作
                    Refund refund = (Refund) obj;
                    if (refund.getSucceed()) {
                        String chargeId = refund.getCharge();
                        String refundId = refund.getId();
                        //获取第一个ReturnHeader通过拼团的退款id
                        GenericValue returnHeader = EntityUtil.getFirst(delegator.findByAnd("ReturnHeader", UtilMisc.toMap("pingRefundId", refundId)));
                        //如果returnHeader不为空
                        if (UtilValidate.isNotEmpty(returnHeader)) {
                            //通过returnHeader获取退款发起人的id，与UserLogin的userLoginId是主外键关系
                            String userId = returnHeader.getString("createdBy");
                            //获取退款订单id
                            String returnId = returnHeader.getString("returnId");
                            //通过退款发起人的id获取UserLogin实体对象
                            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userId));

                            GenericValue person = userLogin.getRelatedOne("Person");
                            if (UtilValidate.isNotEmpty(person)) {
                                String openId = person.getString("wxAppOpenId");
                                Map<String, Object> daMap = FastMap.newInstance();
                                daMap.put("keyword1", returnHeader.getString("orderId"));

                                List<GenericValue> returnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("returnId", returnId));
                                BigDecimal retPay = BigDecimal.ZERO;
                                String productInfo = "";
                                String returnReason = "";
                                if (UtilValidate.isNotEmpty(returnItems)) {
                                    for (int i = 0; i < returnItems.size(); i++) {
                                        GenericValue returnItem = returnItems.get(i);
                                        //获取商家实际付给客户多少钱，并且对他进行保留后面两位小数和四舍五入
                                        retPay.add(returnItem.getBigDecimal("actualPaymentMoney").setScale(2, BigDecimal.ROUND_HALF_UP));
                                        productInfo += returnItem.getRelatedOne("Product").getString("productName") + " ";
                                        returnReason += returnItem.getString("returnReason") + " ";
                                    }
                                }//您的退款申请已经到账,请查收！
                                daMap.put("keyword2", returnReason);
                                daMap.put("keyword3", retPay.toString());
                                daMap.put("keyword4", productInfo);
                                resultData = dispatcher.runSync("xgro-sendTemplateMsg1", UtilMisc.toMap("templateSendType", "REFUND_APPLY_NOTIFY", "touser", openId, "data", daMap, "partyId", userLogin.getString("partyId"), "objectValueId", returnHeader.getString("orderId")));

                                if (ServiceUtil.isError(resultData)) {
                                    resultData.put("retCode", 0);
                                    resultData.put("message", "消息类型对应的template为空");
                                }
                            }
                        }
                    }
                    //发送小程序模板消息
//                    System.out.println("webhooks 发送了 Refund");
                } else if (obj instanceof Summary) {
//                    System.out.println("webhooks 发送了 Summary");
                }

            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


    @RequestMapping(value = "/webhooksZero", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> completePayZero(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        String orderGroupId = request.getParameter("orderGroupId");
        if(UtilValidate.isEmpty(orderGroupId)){
            resultData.put("retCode","0");
            resultData.put("errorMsg","orderGroupId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }


        try {
            List<GenericValue> orderRels = delegator.findByAnd("OrderGroupOrderRel", UtilMisc.toMap("orderGroupId", orderGroupId));
            GenericValue orderGroup = delegator.findByPrimaryKey("OrderGroup", UtilMisc.toMap("orderGroupId", orderGroupId));
            String orderGroupType = orderGroup.getString("orderGroupType");
            //支付成功回调
            if (UtilValidate.isNotEmpty(orderRels)) {
                for (int i = 0; i < orderRels.size(); i++) {
                    GenericValue order = orderRels.get(i);
                    String orderId = order.getString("orderId");
                    //如果orderId對應的orderPaymentRef 的狀態則不用再調？？
                    List<GenericValue> payments = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
                    if (UtilValidate.isNotEmpty(payments)) {
                        GenericValue payment = payments.get(0);
                        String statusId = payment.getString("statusId");
                        if (!"PAYMENT_RECEIVED".equals(statusId)) {
                            dispatcher.runSync("processPaymentCallback", UtilMisc.toMap(UtilMisc.toMap("orderId", orderId, "transactionId", "", "totalFee", 0, "discount", 0, "status", "success", "chargeId", "")));
                        }
                    }
                }
                if (UtilValidate.isNotEmpty(orderGroupId)) {
                    dispatcher.runSync("completePay", UtilMisc.toMap("orderGroupId", orderGroupId));
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        resultData.put("retCode","1");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 礼品订单完成后，需要修改礼品单状态为待赠送
     *
     * @param orderGroupId
     * @param delegator
     * @return
     */

    private Map<String, Object> handleGiftOrder(String orderGroupId, Delegator delegator) throws GenericEntityException {
        Map<String, Object> resultData = FastMap.newInstance();
        List<GenericValue> partyOrderRelPresentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("orderGroupId", orderGroupId));
        GenericValue partyOrderRelPresent = partyOrderRelPresentList.get(0);
        String status = partyOrderRelPresent.getString("status");
        if (!"GIFT_WAIT_PAY".equals(status)) {
            resultData.put("retCode", "0");
            resultData.put("message", "查询不到该订单");
            return resultData;
        }
        //修改赠送表状态
        partyOrderRelPresent.put("status", "GIFT_WAIT_SEND");
        partyOrderRelPresent.store();

        resultData.put("retCode", "1");
        resultData.put("message", "success");
        return resultData;
    }


    /**
     * 读取文件, 部署 web 程序的时候, 签名和验签内容需要从 request 中获得
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    public static String getStringFromFile(String filePath) throws Exception {
        FileInputStream in = new FileInputStream(filePath);
        InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
        BufferedReader bf = new BufferedReader(inReader);
        StringBuilder sb = new StringBuilder();
        String line;
        do {
            line = bf.readLine();
            if (line != null) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append(line);
            }
        } while (line != null);

        return sb.toString();
    }

    /**
     * 获得公钥
     *
     * @return
     * @throws Exception
     */
    public static PublicKey getPubKey(String pingRsaPath) throws Exception {


        String pubKeyString = getStringFromFile(pingRsaPath);
        pubKeyString = pubKeyString.replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|-+END PUBLIC KEY-+\\r?\\n?)", "");
        byte[] keyBytes = Base64.decodeBase64(pubKeyString);

        // generate public key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(spec);
        return publicKey;
    }

    /**
     * 验证签名
     *
     * @param dataString
     * @param signatureString
     * @param publicKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static boolean verifyData(String dataString, String signatureString, PublicKey publicKey)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
        byte[] signatureBytes = Base64.decodeBase64(signatureString);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(dataString.getBytes("UTF-8"));
        return signature.verify(signatureBytes);
    }
}
