package org.ofbiz.accounting.thirdparty.weixin;


import javolution.util.FastMap;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.ofbiz.accounting.thirdparty.alipay.util.UtilDate;
import org.ofbiz.accounting.thirdparty.weixin.util.Sha1Util;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.OrderChangeHelper;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by csy on 2014/11/2.
 */
public class WeixinEvents {

    public static final String resource = "AccountingUiLabels";
    public static final String resourceErr = "AccountingErrorUiLabels";
    public static final String commonResource = "CommonUiLabels";
    public static final String module = WeixinEvents.class.getName();

    public static String authWeixinPay(HttpServletRequest request,HttpServletResponse response) throws IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Locale locale = request.getLocale();
        String orderId = request.getParameter("orderId");
        if(orderId==null) {
            orderId = (String) request.getAttribute("orderId");
        }
        //out_trade_no 64字符，为了保证唯一性orderId+timestamp
      //
      // get the order header
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get the order header for order: " + orderId, module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "aliPayEvents.problemsGettingOrderHeader", locale));
            return "error";
        }

        // get the order total
        String orderTotal = orderHeader.getBigDecimal("grandTotal").toPlainString();
        String price = request.getParameter("price");
        //IF user enter amount
        if(price!=null && (!"".equals(price))){
            orderTotal = price;
            //因为是用户输入金额
        }
        // get the product store
        GenericValue productStore = ProductStoreWorker.getProductStore(request);

        // get the payment properties file
        // 设置weixin的
        GenericValue paymentConfig = ProductStoreWorker.getProductStorePaymentSetting(delegator, productStore.getString("productStoreId"), "EXT_WEIXIN", null, true);
        String configString = null;
        String paymentGatewayConfigId = null;
        if (paymentConfig != null) {
            paymentGatewayConfigId = paymentConfig.getString("paymentGatewayConfigId");
            configString = paymentConfig.getString("paymentPropertiesPath");
        }
        if (configString == null) {
            configString = "payment.properties";
        }
        String company = UtilFormatOut.checkEmpty(productStore.getString("companyName"), "");
        String itemName = UtilProperties.getMessage(resource, "AccountingOrderNr", locale) + orderId + " " +
                (company != null ? UtilProperties.getMessage(commonResource, "CommonFrom", locale) + " "+ company : "");
        //共账号及商户相关参数
        String appId  = getPaymentGatewayConfigValue(delegator, paymentGatewayConfigId, "appId", configString, "payment.weixin.appId");
        String payUrl = getPaymentGatewayConfigValue(delegator, paymentGatewayConfigId, "payUrl", configString, "payment.weixin.payUrl");
        //授权后要跳转的链接所需的参数一般有会员号，金额，订单号之类，
        //最好自己带上一个加密字符串将金额加上一个自定义的key用MD5签名或者自己写的签名,
        //比如 Sign = %3D%2F%CS%
        payUrl = payUrl+"?orderId="+orderId+"&price="+orderTotal;
        //URLEncoder.encode 后可以在backUri 的url里面获取传递的所有参数
        payUrl = URLEncoder.encode(payUrl);
        //scope 参数视各自需求而定，这里用scope=snsapi_base 不弹出授权页面直接授权目的只获取统一支付接口的openid
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize?" +
                "appid=" + appId+
                "&redirect_uri=" +
                 payUrl+
                "&response_type=code&scope=snsapi_userinfo&state=123#wechat_redirect";
        response.sendRedirect(url);
        return "success";
    }


    /** WeiXin Call-Back Event  目前只用于已步返回的情况 */
    public static String weiXinCallBack(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        Locale locale = UtilHttp.getLocale(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        // get the product store
        GenericValue productStore = ProductStoreWorker.getProductStore(request);
        if (productStore == null) {
            Debug.logError("ProductStore is null", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.problemsGettingMerchantConfiguration", locale));
            return "error";
        }

        // get the system user
        GenericValue userLogin = null;
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "system"));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get UserLogin for: system; cannot continue", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.problemsGettingAuthenticationUser", locale));
            return "error";
        }
        WxPayResponseHandler resHandler = new WxPayResponseHandler(request, response);
        String orderId = resHandler.getParameter("out_trade_no");
        orderId = orderId.substring(0,orderId.indexOf("_"));
        // get the order header
        GenericValue orderHeader = null;
        if (UtilValidate.isNotEmpty(orderId)) {
            try {
                orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Cannot get the order header for order: " + orderId, module);
                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.problemsGettingOrderHeader", locale));
                return "error";
            }
        } else {
            Debug.logError("WeiXin did not callback with a valid orderId!", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.noValidOrderIdReturned", locale));
            return "error";
        }

        if (orderHeader == null) {
            Debug.logError("Cannot get the order header for order: " + orderId, module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.problemsGettingOrderHeader", locale));
            return "error";
        }
        // 设置weixin的
        GenericValue paymentConfig = ProductStoreWorker.getProductStorePaymentSetting(delegator, productStore.getString("productStoreId"), "EXT_WEIXIN", null, true);
        String configString = null;
        String paymentGatewayConfigId = null;
        if (paymentConfig != null) {
            paymentGatewayConfigId = paymentConfig.getString("paymentGatewayConfigId");
            configString = paymentConfig.getString("paymentPropertiesPath");        }

        if (configString == null) {
            configString = "payment.properties";
        }

        String appkey = getPaymentGatewayConfigValue(delegator, paymentGatewayConfigId, "appkey", configString, "payment.weixin.appkey");
        resHandler.setKey(appkey);
        //创建请求对象

        boolean okay = false;
        if (resHandler.isValidSign() == true) {
            boolean beganTransaction = false;
            try {
//                if (resHandler.isWXsign() == true) {
                    //商户订单号
                    //财付通订单号
                    String transaction_id = resHandler.getParameter("transaction_id");
                    //金额,以分为单位
                    String total_fee = resHandler.getParameter("total_fee");
                    //如果有使用折扣券，discount有值，total_fee+discount=原请求的total_fee
                    String discount = resHandler.getParameter("discount");
                    //支付结果
                    String trade_state = resHandler.getParameter("result_code");
                    //判断签名及结果
                    beganTransaction = TransactionUtil.begin();

                    if ("SUCCESS".equals(trade_state)) {
                        //判断该笔订单是否在商户网站中已经做过处理
                        //如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                        //如果有做过处理，不执行商户的业务程序
                        okay = OrderChangeHelper.completeOrder(dispatcher, userLogin, orderId);
                        //注意：
                        //该种交易状态只在两种情况下出现
                        //1、开通了普通即时到账，买家付款成功后。
                        //2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
                    } else {
                        //是否还有其他的情况？？？？
//                        okay = OrderChangeHelper.cancelOrder(dispatcher, userLogin, orderId);
                    }

                    if (okay) {
                        // set the payment preference
                        okay = setPaymentPreferences(delegator, dispatcher, userLogin, orderId, transaction_id,total_fee,discount,trade_state,request);
                        resHandler.sendToCFT("success");
                    }
                    //给财付通系统发送成功信息，财付通系统收到此结果后不再进行后续通知


            /*}else{//sha1签名失败
                Debug.logError("fail Weixin -SHA1 failed",module);
                resHandler.sendToCFT("fail");
            }*/
            }catch (Exception e) {
                String errMsg = "Error handling WeiXin notification";
                Debug.logError(e, errMsg, module);
                try {
                    TransactionUtil.rollback(beganTransaction, errMsg, e);
                } catch (GenericTransactionException gte2) {
                    Debug.logError(gte2, "Unable to rollback transaction", module);
                }
            } finally {
                if (!okay) {
                    try {
                        TransactionUtil.rollback(beganTransaction, "Failure in processing WeiXin callback", null);
                    } catch (GenericTransactionException gte) {
                        Debug.logError(gte, "Unable to rollback transaction", module);
                    }
                } else {
                    try {
                        TransactionUtil.commit(beganTransaction);
                    } catch (GenericTransactionException gte) {
                        Debug.logError(gte, "Unable to commit transaction", module);
                    }
                }
            }
        }else {//MD5签名失败
            Debug.logError("Problems Weixin -Md5 failed", module);
        }
        if (okay) {
            // attempt to release the offline hold on the order (workflow)
            OrderChangeHelper.releaseInitialOrderHold(dispatcher, orderId);

            // call the email confirm service
            Map<String, String> emailContext = UtilMisc.toMap("orderId", orderId);
            try {
                dispatcher.runSync("sendOrderConfirmation", emailContext);
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problems sending email confirmation", module);
            }
        }
        return "success";
    }

    /** Event called when customer cancels a paypal order */
    public static String cancellWeixinOrder(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");

        // get the stored order id from the session
        String orderId = (String) request.getSession().getAttribute("WEIXIN_ORDER");

        // attempt to start a transaction
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
        } catch (GenericTransactionException gte) {
            Debug.logError(gte, "Unable to begin transaction", module);
        }

        // cancel the order
        boolean okay = OrderChangeHelper.cancelOrder(dispatcher, userLogin, orderId);

        if (okay) {
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (GenericTransactionException gte) {
                Debug.logError(gte, "Unable to commit transaction", module);
            }
        } else {
            try {
                TransactionUtil.rollback(beganTransaction, "Failure in processing WeiXin cancel callback", null);
            } catch (GenericTransactionException gte) {
                Debug.logError(gte, "Unable to rollback transaction", module);
            }
        }

        // attempt to release the offline hold on the order (workflow)
        if (okay) {
            OrderChangeHelper.releaseInitialOrderHold(dispatcher, orderId);
        }

        request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.previousWeiXinOrderHasBeenCancelled", locale));
        return "success";
    }

    private static boolean setPaymentPreferences(Delegator delegator, LocalDispatcher dispatcher, GenericValue userLogin, String orderId, String transactionId,String totalFee,String discount,String status,HttpServletRequest request) {
        Debug.logVerbose("Setting payment prefrences..", module);
        List<GenericValue> paymentPrefs = null;
        try {
            Map <String, String> paymentFields = UtilMisc.toMap("orderId", orderId, "statusId", "PAYMENT_NOT_RECEIVED");
            paymentPrefs = delegator.findByAnd("OrderPaymentPreference", paymentFields);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get payment preferences for order #" + orderId, module);
            return false;
        }
        if (paymentPrefs.size() > 0) {
            for(GenericValue pref : paymentPrefs) {
                boolean okay = setPaymentPreference(dispatcher, userLogin, pref, orderId,transactionId,totalFee,discount,status,request);
                if (!okay) {
                    return false;
                }
            }
        }
        return true;
    }


    private static boolean setPaymentPreference(LocalDispatcher dispatcher, GenericValue userLogin, GenericValue paymentPreference, String orderId,String transactionId,String totalFee,String discount,String status,HttpServletRequest request) {
        Locale locale = UtilHttp.getLocale(request);
        List <GenericValue> toStore = new LinkedList <GenericValue> ();

        // WeiXin returns the timestamp in the format 'hh:mm:ss Jan 1, 2000 PST'
        // Parse this into a valid Timestamp Object
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss MMM d, yyyy z");
        java.sql.Timestamp authDate = UtilDateTime.nowTimestamp();

        paymentPreference.set("maxAmount", new BigDecimal(((Double)(new BigDecimal(totalFee).doubleValue() / 100)).toString()));

        if ("success".equalsIgnoreCase(status)) {
            paymentPreference.set("statusId", "PAYMENT_RECEIVED");
        } else if ("Pending".equals(status)) {
            paymentPreference.set("statusId", "PAYMENT_NOT_RECEIVED");
        } else {
            paymentPreference.set("statusId", "PAYMENT_RECEIVED");
        }
        paymentPreference.set("paymentMethodTypeId","EXT_WEIXIN");
        toStore.add(paymentPreference);


        Delegator delegator = paymentPreference.getDelegator();

        // create the PaymentGatewayResponse
        String responseId = delegator.getNextSeqId("PaymentGatewayResponse");
        GenericValue response = delegator.makeValue("PaymentGatewayResponse");
        response.set("paymentGatewayResponseId", responseId);
        response.set("paymentServiceTypeEnumId", "PRDS_PAY_EXTERNAL");
        response.set("orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"));
        response.set("paymentMethodTypeId", paymentPreference.get("paymentMethodTypeId"));
        response.set("paymentMethodId", paymentPreference.get("paymentMethodId"));

        // set the auth info
        response.set("amount", new BigDecimal(((Double)(new BigDecimal(totalFee).doubleValue() / 100)).toString()));
        response.set("referenceNum", transactionId);
        response.set("gatewayCode", status);
        response.set("gatewayFlag", status.substring(0,1));
        response.set("gatewayMessage", status);
        response.set("transactionDate", authDate);
        toStore.add(response);

        try {
            delegator.storeAll(toStore);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot set payment preference/payment info", module);
            return false;
        }

        // create a payment record too
        Map <String, Object> results = null;
        try {
            String comment = UtilProperties.getMessage(resource, "AccountingPaymentReceiveViaWeiXin", locale);
            results = dispatcher.runSync("createPaymentFromPreference", UtilMisc.toMap("userLogin", userLogin,
                    "orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"), "comments", comment));
        } catch (GenericServiceException e) {
            Debug.logError(e, "Failed to execute service createPaymentFromPreference", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.failedToExecuteServiceCreatePaymentFromPreference", locale));
            return false;
        }

        if ((results == null) || (results.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
            Debug.logError((String) results.get(ModelService.ERROR_MESSAGE), module);
            request.setAttribute("_ERROR_MESSAGE_", results.get(ModelService.ERROR_MESSAGE));
            return false;
        }

        return true;
    }

    private static String getPaymentGatewayConfigValue(Delegator delegator, String paymentGatewayConfigId, String paymentGatewayConfigParameterName,
                                                       String resource, String parameterName) {
        String returnValue = "";
        if (UtilValidate.isNotEmpty(paymentGatewayConfigId)) {
            try {
                GenericValue weixin = delegator.findOne("PaymentGatewayWeixin", UtilMisc.toMap("paymentGatewayConfigId", paymentGatewayConfigId), false);
                if (UtilValidate.isNotEmpty(weixin)) {
                    Object weixinField = weixin.get(paymentGatewayConfigParameterName);
                    if (weixinField != null) {
                        returnValue = weixinField.toString().trim();
                    }
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        } else {
            String value = UtilProperties.getPropertyValue(resource, parameterName);
            if (value != null) {
                returnValue = value.trim();
            }
        }
        return returnValue;
    }
    
    
    /**
     * 微信退款服务 add by gss 2016/5/23
     * @param dctx
     * @param context
     * @return
     */
  /*  public static Map<String, Object> refundWeixin(DispatchContext dctx,
                                                   Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        Map<String, Object> result = FastMap.newInstance();
        Locale locale = (Locale) context.get("locale");
        //=----订单号
        //OrderPaymentPreference  paymentGatewayResponse
        //ico 参数  退款单号
        String returnId = (String) context.get("returnId");
        // 退款金额
        String refundfee = (String) context.get("refundfee");
        //订单号
        String orderId = (String) context.get("orderId");
	   String returnId = "10220";
		// 退款金额
		String refundfee = "0.01";
		//订单号
		String orderId = "1000011120";
        //退款单号
        String out_refund_no = "";
        String curDate = UtilDate.getOrderNum();
        out_refund_no = returnId + "_"+curDate;
        // 总金额
        Integer total_fee = 0;
        // 退款金额
        Integer refund_fee = 0;
        refund_fee=((Double)(new BigDecimal(refundfee).doubleValue()*100)).intValue();
        微信订单号  商户订单号  二选一 
        String transaction_id="";
	       商户订单号
		String out_trade_no = "";
        // 操作员
        String op_user_id = "";
        // get the order header
        GenericValue orderHeader = null;
        if (UtilValidate.isNotEmpty(orderId)) {
            try {
                orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Cannot get the order header for order: " + orderId, module);
                result.put("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.problemsGettingOrderHeader", locale));
                return result;
            }
        } else {
            Debug.logError("WeiXin did not callback with a valid orderId!", module);
            result.put("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.noValidOrderIdReturned", locale));
            return result;
        }
        if (orderHeader == null) {
            Debug.logError("Cannot get the order header for order: " + orderId, module);
            result.put("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.problemsGettingOrderHeader", locale));
            return result;
        }
        // get the product store
        GenericValue productStore = null;
        try {
            productStore = orderHeader.getRelatedOne("ProductStore");
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if(UtilValidate.isEmpty(productStore)){
            String productStoreId = "10000";
            // GenericValue productStore = ProductStoreWorker.getProductStore(request);
            try {
                productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId",productStoreId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (productStore == null) {
                Debug.logError("ProductStore is null", module);
                result.put("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "aliPayEvents.problemsGettingMerchantConfiguration", locale));
                return result;
            }
        }
        设置weixin的
        GenericValue paymentConfig = ProductStoreWorker.getProductStorePaymentSetting(delegator, productStore.getString("productStoreId"), "EXT_WEIXIN", null, true);
        String configString = null;
        String paymentGatewayConfigId = null;
        if (paymentConfig != null) {
            paymentGatewayConfigId = paymentConfig.getString("paymentGatewayConfigId");
            configString = paymentConfig.getString("paymentPropertiesPath");        }
        if (configString == null) {
            configString = "payment.properties";
        }
        微信公众号apid
        String appId  = getPaymentGatewayConfigValue(delegator, paymentGatewayConfigId, "appId", configString, "payment.weixin.appId");
        微信公众号appsecret
        String appSecret = getPaymentGatewayConfigValue(delegator, paymentGatewayConfigId, "appSecret", configString, "payment.weixin.appSecret");
        微信商户id
        String partner = getPaymentGatewayConfigValue(delegator, paymentGatewayConfigId, "partner", configString, "payment.weixin.partner");
        商户平台上KEY
        String partnerKey = getPaymentGatewayConfigValue(delegator, paymentGatewayConfigId, "partnerKey", configString, "payment.weixin.partnerKey");
        
        if ( UtilValidate.isEmpty(partnerKey)
                || UtilValidate.isEmpty(partner)
                || UtilValidate.isEmpty(appId)
                || UtilValidate.isEmpty(appSecret)) {
            Debug.logError("Payment properties is not configured properly, some notify URL from Weixin is not correctly defined!", module);
            result.put("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "weiXinEvents.problemsGettingMerchantConfiguration", locale));
            return result;
        }
        随机字符串
        String nonce_str = Sha1Util.getNonceStr();
        RequestHandler reqHandler = new RequestHandler(
                null, null);
        reqHandler.init(appId, appSecret,partnerKey);
        
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        String createOrderURL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
        try {
            GenericValue OrderPaymentPreference = EntityUtil.getFirst(delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId",orderId,"paymentMethodTypeId","EXT_WEIXIN"))) ;
            if(UtilValidate.isNotEmpty(OrderPaymentPreference)){
                GenericValue paymentGatewayResponse = EntityUtil.getFirst(delegator.findByAnd("PaymentGatewayResponse", UtilMisc.toMap("orderPaymentPreferenceId",OrderPaymentPreference.get("orderPaymentPreferenceId"),"paymentMethodTypeId","EXT_WEIXIN"))) ;
                if(UtilValidate.isNotEmpty(paymentGatewayResponse)){
                    transaction_id=paymentGatewayResponse.getString("referenceNum");
                    total_fee=((Double)(paymentGatewayResponse.getBigDecimal("amount").doubleValue()*100)).intValue();
                    packageParams.put("appid", appId);
                    packageParams.put("mch_id", partner);
                    packageParams.put("nonce_str", nonce_str);
                    packageParams.put("device_info","WEB");
                    packageParams.put("transaction_id", transaction_id);
                    packageParams.put("out_trade_no", "");
                    packageParams.put("out_refund_no", out_refund_no);
                    packageParams.put("total_fee", new Integer(total_fee).toString());
                    packageParams.put("refund_fee",new Integer(refund_fee).toString() );
                    packageParams.put("op_user_id", partner);
                    String sign = reqHandler.createSign(packageParams);
                    
                    String xml = "<xml>" + "<appid>" + appId + "</appid>" + "<mch_id>"
                            + partner + "</mch_id>" + "<nonce_str>" + nonce_str
                            + "</nonce_str>" + "<sign>"+ sign +"</sign>"
                            + "<transaction_id>" + transaction_id + "</transaction_id>"
                            + "<out_refund_no>" + out_refund_no + "</out_refund_no>"
                            + "<total_fee>" + total_fee + "</total_fee>"
                            + "<refund_fee>" + refund_fee + "</refund_fee>"
                            + "<op_user_id>" + partner + "</op_user_id>"
                            +"<device_info>WEB</device_info>"
                            + "</xml>";
                    try {
                        *
                         * 注意PKCS12证书 是从微信商户平台-》账户设置-》 API安全 中下载的
                         
                        KeyStore keyStore = KeyStore.getInstance("PKCS12");
                        String configFile = "/applications/accounting/config/apiclient_cert.p12";
                        String configFileLocation = System.getProperty("ofbiz.home") + configFile;
                        FileInputStream instream = new FileInputStream(
                                new File(configFileLocation));// P12文件目录
                        try {
                            keyStore.load(instream, partner.toCharArray());// 这里写密码..默认是你的MCHID
                        } finally {
                            instream.close();
                        }
                        // Trust own CA and all self-signed certs
                        SSLContext sslcontext = SSLContexts.custom()
                                .loadKeyMaterial(keyStore, partner.toCharArray())// 这里也是写密码的
                                .build();
                        
                        // Allow TLSv1 protocol only
                        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                                sslcontext, new String[] { "TLSv1" }, null,
                                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
                        CloseableHttpClient httpclient = HttpClients.custom()
                                .setSSLSocketFactory(sslsf).build();
                        try {
                            HttpPost httpost= HttpClientConnectionManager.getPostMethod(createOrderURL);
                            httpost.setEntity(new StringEntity(xml, "UTF-8"));
                            CloseableHttpResponse response = httpclient.execute(httpost);
                            try {
                                HttpEntity entity = response.getEntity();
                                String jsonStr = EntityUtils.toString(response.getEntity(),
                                        "UTF-8");
                                
                                System.out.println(jsonStr);
                                if (entity != null) {
                                    System.out.println("Response content length: "
                                            + entity.getContentLength());
                                    SAXReader saxReader = new SAXReader();
                                    Document document = saxReader.read(new StringReader(jsonStr));
                                    Element rootElt = document.getRootElement();
		                            System.out.println("根节点：" + rootElt.getName());
		                            System.out.println("==="+rootElt.elementText("return_code"));
		                            System.out.println("==="+rootElt.elementText("result_code"));
		                            System.out.println("==="+rootElt.elementText("transaction_id"));
		                            System.out.println("==="+rootElt.elementText("out_trade_no"));
		                            System.out.println("==="+rootElt.elementText("out_refund_no"));
		                            System.out.println("==="+rootElt.elementText("refund_id"));
		                            System.out.println("==="+rootElt.elementText("refund_channel"));
		                            System.out.println("==="+rootElt.elementText("refund_fee"));
		                            System.out.println("==="+rootElt.elementText("coupon_refund_fee"));  
                                    String returnCode = rootElt.elementText("return_code");
                                    JSONObject results = new JSONObject();
                                    if(returnCode.equals("SUCCESS")){
                                        System.out.println("==="+rootElt.elementText("refund_id"));
		                            	 ReturnHist rh = new ReturnHist();
		                                rh.setOtherRefundId(rootElt.elementText("out_refund_no"));
		                                rh.setReturnChannelType(rootElt.elementText("refund_channel"));
		                                rh.setReturnMoneyBak(Float.parseFloat(totalFee)/100);
		                                rh.setReturnMoneyBalance((Float.parseFloat(totalFee)-Float.parseFloat(rootElt.elementText("refund_fee")))/100);
		                                rh.setReturnOrderNo(rootElt.elementText("out_refund_no"));
		                                rh.setAppKey(jsonO.getString("appKey"));
		                                weiXinRefundService.addRefundInfo(rh);
		                                System.out.println("======================微信退款成功=================");
		                                
		                                result.put("status","success");
		                                result.put("msg","success");
		                                result.put("returnChannelType", rh.getReturnChannelType());
		                                result.put("otherRefundId", rh.getOtherRefundId());
		                                result.put("returnMoneyBak", rh.getReturnMoneyBak());
		                                result.put("returnMoneyBalance", rh.getReturnMoneyBalance());
		                                result.put("returnOrderNo", rh.getReturnOrderNo());  
                                        result.put("status",true);
                                        result.put("msg","success");
                                        result.put("refundId",rootElt.elementText("refund_id"));
                                    }else{
                                        result.put("status",false);
                                        result.put("msg",rootElt.elementText("err_code_des"));
                                    }
                                    return result;
                                }
                                EntityUtils.consume(entity);
                                //return jsonStr;
                            } finally {
                                response.close();
                            }
                        } finally {
                            httpclient.close();
                        }
                        //String s=  new GetWxOrderno().doRefund(createOrderURL, xml);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    result.put("status",false);
                    result.put("msg","没有支付记录！");
                    //result.put("_ERROR_MESSAGE_", "没有支付记录！");
                    return result;
                }
            }else{
                result.put("status",false);
                result.put("msg","没有支付记录！");
                //result.put("_ERROR_MESSAGE_", "没有支付记录！");
                return result;
            }
        } catch (GenericEntityException e1) {
            e1.printStackTrace();
        }
        return result;
    }*/
}
