package com.qihua.ofbiz.systemMgr;

import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 2018/5/10.
 */
public class EmailService {
    public static final String module = EmailService.class.getName();
    
    
    public Map<String, Object> productPromoCouponWarnEmailSender(DispatchContext dcx, Map<String, ? extends Object> context) {
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        GenericValue productPromoCoupon = (GenericValue) context.get("productPromoCoupon");
        String promoCode = (String) productPromoCoupon.get("couponCode");
        Delegator delegator = dcx.getDelegator();
        try {
            productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode", promoCode));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        Long couponQuantity = productPromoCoupon.get("couponQuantity") == null ? 0L : (Long) productPromoCoupon.get("couponQuantity");
        Long userCount = productPromoCoupon.get("userCount") == null ? 0L : (Long) productPromoCoupon.get("userCount");
        
        LocalDispatcher dispatcher = dcx.getDispatcher();
        if (couponQuantity.compareTo(userCount) <= 0) {
            //发送邮件
            // prepare the order information
            Map<String, Object> sendMap = FastMap.newInstance();
            Map<String, Object> bodyParameters = UtilMisc.<String, Object>toMap("productPromoCoupon", productPromoCoupon);
            GenericValue emailTemplate = null;
            try {
                emailTemplate = delegator.findByPrimaryKey("EmailTemplateSetting", UtilMisc.toMap("emailTemplateSettingId", "PRODUCT_COUPON_WARN"));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problem getting the EmailTemplateSetting", module);
            }
            if (emailTemplate == null) {
                return ServiceUtil.returnFailure("邮件发送模板不存在!");
            }
            
            // the override screenUri
            String fromAddress = UtilProperties.getMessage("general.properties", "defaultFromEmailAddress", Locale.CHINA);
            String bodyScreenLocation = emailTemplate.getString("bodyScreenLocation");
            sendMap.put("bodyScreenUri", bodyScreenLocation);
            sendMap.put("bodyParameters", bodyParameters);
            
            String subjectString = emailTemplate.getString("subject");
            sendMap.put("subject", subjectString);
            
            sendMap.put("contentType", "text/html");
            sendMap.put("sendFrom", fromAddress);
//            sendMap.put("sendCc", warningMail);
            try {
                List<GenericValue> coupApps = EntityUtil.filterByDate(delegator.findByAnd("ProductStoreCouponAppl", UtilMisc.toMap("couponCode", productPromoCoupon.getString("couponCode"))));
                if (UtilValidate.isNotEmpty(coupApps)) {
                    GenericValue couponApp = coupApps.get(0);
                    GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", couponApp.getString("productStoreId")));
                    GenericValue partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", productStore.getString("ownerPartyId")));
                    if (UtilValidate.isNotEmpty(partyBusiness)) {
                        String warningMail = partyBusiness.getString("leageEmail");
                        sendMap.put("sendTo", warningMail);
                    } else {
                        sendMap.put("sendTo", fromAddress);
                    }
                }
                
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            
            
            // send the notification
            Map<String, Object> sendResp = null;
            try {
                sendResp = dispatcher.runSync("sendMailFromScreen", sendMap);
            } catch (Exception e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError("代金劵预警邮件发送错误:" + e.getMessage());
            }
            
            
        }
        return resultData;
    }
    
    public Map<String, Object> inventoryItemWarnEmailSender(DispatchContext dcx, Map<String, ? extends Object> context) {
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        GenericValue inventoryItem = (GenericValue) context.get("inventoryItem");
        String inventoryItemId = (String) inventoryItem.get("inventoryItemId");
        Delegator delegator = dcx.getDelegator();
        try {
            inventoryItem = delegator.findByPrimaryKey("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        BigDecimal accountingQuantityTotal = inventoryItem.get("accountingQuantityTotal") == null ? BigDecimal.ZERO : (BigDecimal) inventoryItem.get("accountingQuantityTotal");
        BigDecimal lockQuantityTotal = inventoryItem.get("lockQuantityTotal") == null ? BigDecimal.ZERO : (BigDecimal) inventoryItem.get("lockQuantityTotal");
        BigDecimal warningQuantity = inventoryItem.get("warningQuantity") == null ? BigDecimal.ZERO : (BigDecimal) inventoryItem.get("warningQuantity");
        String warningMail = (String) inventoryItem.get("warningMail");
        
        LocalDispatcher dispatcher = dcx.getDispatcher();
        if (accountingQuantityTotal.subtract(lockQuantityTotal).compareTo(warningQuantity) <= 0) {
            //发送邮件
            // prepare the order information
            Map<String, Object> sendMap = FastMap.newInstance();
            String productId = inventoryItem.getString("productId");
            Map<String, Object> bodyParameters = UtilMisc.<String, Object>toMap("productId", productId, "inventoryItemId", inventoryItemId);
            GenericValue emailTemplate = null;
            try {
                emailTemplate = delegator.findByPrimaryKey("EmailTemplateSetting", UtilMisc.toMap("emailTemplateSettingId", "INVENTORY_ITEM_WARN"));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problem getting the EmailTemplateSetting", module);
            }
            if (emailTemplate == null) {
                return ServiceUtil.returnFailure("邮件发送模板不存在!");
            }
            
            // the override screenUri
            String fromAddress = UtilProperties.getMessage("general.properties", "defaultFromEmailAddress", Locale.CHINA);
            String bodyScreenLocation = emailTemplate.getString("bodyScreenLocation");
            sendMap.put("bodyScreenUri", bodyScreenLocation);
            sendMap.put("bodyParameters", bodyParameters);
            
            String subjectString = emailTemplate.getString("subject");
            sendMap.put("subject", subjectString);
            
            sendMap.put("contentType", "text/html");
            sendMap.put("sendFrom", fromAddress);
//            sendMap.put("sendCc", warningMail);
            sendMap.put("sendTo", warningMail);
            
            if(UtilValidate.isNotEmpty(warningMail)) {
                // send the notification
                Map<String, Object> sendResp = null;
                try {
                    sendResp = dispatcher.runSync("sendMailFromScreen", sendMap);
                } catch (Exception e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError("库存预警邮件发送错误:" + e.getMessage());
                }
            }
            
        }
        return resultData;
    }
    
    public Map<String, Object> applyReturnEmailSender(DispatchContext dcx, Map<String, ? extends Object> context) {
        
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String orderId = (String) context.get("orderId");
        String userId = (String) context.get("userId");
        String returnId = (String) context.get("returnId");
        Delegator delegator = dcx.getDelegator();
        // prepare the order information
        Map<String, Object> sendMap = FastMap.newInstance();
        
        LocalDispatcher dispatcher = dcx.getDispatcher();
        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            
            //发送邮件
            
            Map<String, Object> bodyParameters = UtilMisc.<String, Object>toMap("orderId", orderId, "returnId", returnId);
            GenericValue emailTemplate = null;
            try {
                emailTemplate = delegator.findByPrimaryKey("EmailTemplateSetting", UtilMisc.toMap("emailTemplateSettingId", "CUST_APPLY_RETURN"));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problem getting the EmailTemplateSetting", module);
            }
            if (emailTemplate == null) {
                return ServiceUtil.returnFailure("邮件发送模板不存在!");
            }
            
            // the override screenUri
            String fromAddress = UtilProperties.getMessage("general.properties", "defaultFromEmailAddress", Locale.CHINA);
            String bodyScreenLocation = emailTemplate.getString("bodyScreenLocation");
            sendMap.put("bodyScreenUri", bodyScreenLocation);
            sendMap.put("bodyParameters", bodyParameters);
            
            String subjectString = emailTemplate.getString("subject");
            sendMap.put("subject", subjectString);
            
            sendMap.put("contentType", "text/html");
            sendMap.put("sendFrom", fromAddress);
//            sendMap.put("sendCc", warningMail);
            GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", orderHeader.getString("productStoreId")));
            GenericValue partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", productStore.getString("ownerPartyId")));
            
            if (UtilValidate.isNotEmpty(partyBusiness)) {
                String warningMail = partyBusiness.getString("leageEmail");
                sendMap.put("sendTo", warningMail);
            } else {
                sendMap.put("sendTo", fromAddress);
            }
            
            
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        
        // send the notification
        Map<String, Object> sendResp = null;
        try {
            sendResp = dispatcher.runSync("sendMailFromScreen", sendMap);
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError("用户申请退款:" + e.getMessage());
        }
        return resultData;
    }
    
    public Map<String, Object> productOnlineEmailSender(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String productId = (String) context.get("productId");
        Delegator delegator = dcx.getDelegator();
        // prepare the order information
        Map<String, Object> sendMap = FastMap.newInstance();
        
        LocalDispatcher dispatcher = dcx.getDispatcher();
        try {
            
            //发送邮件
            
            Map<String, Object> bodyParameters = UtilMisc.<String, Object>toMap("productId", productId);
            GenericValue emailTemplate = null;
            try {
                emailTemplate = delegator.findByPrimaryKey("EmailTemplateSetting", UtilMisc.toMap("emailTemplateSettingId", "PRODUCT_ONLINE_APPLY"));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problem getting the EmailTemplateSetting", module);
            }
            if (emailTemplate == null) {
                return ServiceUtil.returnFailure("邮件发送模板不存在!");
            }
            
            // the override screenUri
            String fromAddress = UtilProperties.getMessage("general.properties", "defaultFromEmailAddress", Locale.CHINA);
            String bodyScreenLocation = emailTemplate.getString("bodyScreenLocation");
            sendMap.put("bodyScreenUri", bodyScreenLocation);
            sendMap.put("bodyParameters", bodyParameters);
            
            String subjectString = emailTemplate.getString("subject");
            sendMap.put("subject", subjectString);
            
            sendMap.put("contentType", "text/html");
            sendMap.put("sendFrom", fromAddress);
    
    
            GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
            if(UtilValidate.isNotEmpty(product)) {
                GenericValue partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId", product.getString("businessPartyId")));
    
                if (UtilValidate.isNotEmpty(partyBusiness)) {
                    String warningMail = partyBusiness.getString("leageEmail");
                    sendMap.put("sendTo", warningMail);
                } else {
                    sendMap.put("sendTo", fromAddress);
                }
            }
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        // send the notification
        Map<String, Object> sendResp = null;
        try {
            sendResp = dispatcher.runSync("sendMailFromScreen", sendMap);
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError("商品审批通过通知:" + e.getMessage());
        }
        return resultData;
    }
    
}
