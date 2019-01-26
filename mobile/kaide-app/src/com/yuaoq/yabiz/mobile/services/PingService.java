package com.yuaoq.yabiz.mobile.services;

import com.pingplusplus.Pingpp;
import com.pingplusplus.exception.*;
import com.pingplusplus.model.Refund;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;
import org.omg.CORBA.portable.Delegate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 2018/5/11.
 */
public class PingService {
    
    public static Map<String,Object> pingRefundRequest(DispatchContext dcx,Map<String,? extends Object> context){
        
        Pingpp.apiKey = UtilProperties.getMessage("order.properties","ping.wxList.appKey", Locale.CHINA);
        // 设置私钥路径，用于请求签名
        
        String pingPrivateKeyPath = UtilProperties.getMessage("order.properties","ping.rsa.private.path", Locale.CHINA);
        Pingpp.privateKeyPath =   ClassLoader.getSystemResource(pingPrivateKeyPath).getFile();
        Map<String,Object> resultData = ServiceUtil.returnSuccess();
        String orderId = (String)context.get("orderId");
        String chargeId = (String)context.get("chargeId");
        String amount = (String)context.get("amount");
        GenericValue returnHeader = (GenericValue)context.get("returnHeader");
        Delegator delegate = dcx.getDelegator();
        
        BigDecimal am = new BigDecimal(amount);
        am = am.multiply(new BigDecimal(100));
        Refund refund = null;
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("description", orderId);
        params.put("amount", am.intValue());//
        // 退款的金额, 单位为对应币种的最小货币单位，例如：人民币为分（如退款金额为 1 元，此处请填 100）。必须小于等于可退款金额，默认为全额退款
    
        try {
            refund = Refund.create(chargeId, params);
            returnHeader.set("pingRefundId",refund.getId());
            delegate.store(returnHeader);
            System.out.println(refund);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            ServiceUtil.returnError(e.getMessage());
        } catch (InvalidRequestException e) {
            e.printStackTrace();
            ServiceUtil.returnError(e.getMessage());
        } catch (APIConnectionException e) {
            e.printStackTrace();
            ServiceUtil.returnError(e.getMessage());
        } catch (APIException e) {
            e.printStackTrace();
            ServiceUtil.returnError(e.getMessage());
        } catch (ChannelException e) {
            e.printStackTrace();
            ServiceUtil.returnError(e.getMessage());
        } catch (RateLimitException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            ServiceUtil.returnError(e.getMessage());
        }
        return resultData;
    }
}
