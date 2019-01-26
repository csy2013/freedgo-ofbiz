package com.yuaoq.yabiz.mobile.order.shoppingcart;


import com.yuaoq.yabiz.mobile.util.TokenUtils;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.shoppingcart.CheckOutHelper;
import org.ofbiz.order.shoppinglist.ShoppingListEvents;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.website.WebSiteWorker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by changsy on 16/6/28.
 */
public class ShoppingCart extends org.ofbiz.order.shoppingcart.ShoppingCart {

    public ShoppingCart(HttpServletRequest request, Locale locale, String currencyUom) {
        super((Delegator) request.getAttribute("delegator"), ProductStoreWorker.getProductStoreId(request),
                WebSiteWorker.getWebSiteId(request), (locale != null ? locale : ProductStoreWorker.getStoreLocale(request)),
                (currencyUom != null ? currencyUom : ProductStoreWorker.getStoreCurrencyUomId(request)),
                request.getParameter("billToCustomerPartyId"),
                (request.getParameter("supplierPartyId") != null ? request.getParameter("supplierPartyId") : request.getParameter("billFromVendorPartyId")));


        String username = getUserNameFromRequest(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        try {
            this.userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", username));
            this.autoUserLogin = userLogin;
            this.orderPartyId = (String) userLogin.get("partyId");
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }


    }

    public ShoppingCart(HttpServletRequest request) {
        this(request, UtilHttp.getLocale(request), UtilHttp.getCurrencyUom(request));
    }

    /**
     * Creates a new cloned ShoppingCart Object.
     */
    public ShoppingCart(org.ofbiz.order.shoppingcart.ShoppingCart cart) {
        super(cart);
    }

    public static String getUserNameFromRequest(HttpServletRequest request) {
       
        String tokenName = UtilProperties.getPropertyValue("application.properties", "app.security.token.header");
        String secret = UtilProperties.getPropertyValue("application.properties", "app.security.jwt.tokenSigningKey");
        String expire = UtilProperties.getPropertyValue("application.properties", "app.security.jwt.refreshTokenExpTime");
    
    
        String token = request.getHeader(tokenName);
        if (UtilValidate.isNotEmpty(tokenName)) {
            TokenUtils tokenUtils = new TokenUtils(secret,new Long(expire));
            if(token.startsWith("Bearer ")){
                token = token.substring("Bearer ".length());
            }
            String username = tokenUtils.getUsernameFromToken(token);
          
            return username;
        } else {
            return null;
        }
    }

    public static String setCheckOutOptions(HttpServletRequest request, HttpServletResponse response) {

        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        // Set the payment options
        Map<String, Map<String, Object>> selectedPaymentMethods = getSelectedPaymentMethods(request, cart);

        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // get the billing account and amount
        String billingAccountId = request.getParameter("billingAccountId");
        if (UtilValidate.isNotEmpty(billingAccountId)) {
            BigDecimal billingAccountAmt = null;
            billingAccountAmt = determineBillingAccountAmount(billingAccountId, request.getParameter("billingAccountAmount"), dispatcher);
            if (billingAccountAmt == null) {
                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error, "OrderInvalidAmountSetForBillingAccount", UtilMisc.toMap("billingAccountId", billingAccountId), (cart != null ? cart.getLocale() : Locale.getDefault())));
                return "error";
            }
            selectedPaymentMethods.put("EXT_BILLACT", UtilMisc.<String, Object>toMap("amount", billingAccountAmt, "securityCode", null));
        }

        if (selectedPaymentMethods == null) {
            return "error";
        }

        String shippingMethod = request.getParameter("shipping_method");
        String shippingContactMechId = request.getParameter("shipping_contact_mech_id");

        String taxAuthPartyGeoIds = request.getParameter("taxAuthPartyGeoIds");
        String partyTaxId = request.getParameter("partyTaxId");
        String isExempt = request.getParameter("isExempt");

        String shippingInstructions = request.getParameter("shipping_instructions");
        String orderAdditionalEmails = request.getParameter("order_additional_emails");
        String maySplit = request.getParameter("may_split");
        String giftMessage = request.getParameter("gift_message");
        String isGift = request.getParameter("is_gift");
        String internalCode = request.getParameter("internalCode");
        String shipBeforeDate = request.getParameter("shipBeforeDate");
        String shipAfterDate = request.getParameter("shipAfterDate");

        List<String> singleUsePayments = new ArrayList<String>();

        // get a request map of parameters
        Map<String, Object> params = UtilHttp.getParameterMap(request);

        // if taxAuthPartyGeoIds is not empty drop that into the database
        if (UtilValidate.isNotEmpty(taxAuthPartyGeoIds)) {
            try {
                Map<String, Object> createCustomerTaxAuthInfoResult = dispatcher.runSync("createCustomerTaxAuthInfo",
                        UtilMisc.toMap("partyId", cart.getPartyId(), "taxAuthPartyGeoIds", taxAuthPartyGeoIds, "partyTaxId", partyTaxId, "isExempt", isExempt));
                ServiceUtil.getMessages(request, createCustomerTaxAuthInfoResult, null);
                if (ServiceUtil.isError(createCustomerTaxAuthInfoResult)) {
                    return "error";
                }
            } catch (GenericServiceException e) {
                String errMsg = "Error setting customer tax info: " + e.toString();
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                return "error";
            }
        }

        // check for gift card not on file
        Map<String, Object> gcResult = checkOutHelper.checkGiftCard(params, selectedPaymentMethods);
        ServiceUtil.getMessages(request, gcResult, null);
        if (ServiceUtil.isError(gcResult)) {
            return "error";
        }

        String gcPaymentMethodId = (String) gcResult.get("paymentMethodId");
        BigDecimal gcAmount = (BigDecimal) gcResult.get("amount");
        if (gcPaymentMethodId != null) {
            selectedPaymentMethods.put(gcPaymentMethodId, UtilMisc.<String, Object>toMap("amount", gcAmount, "securityCode", null));
            if ("Y".equalsIgnoreCase(request.getParameter("singleUseGiftCard"))) {
                singleUsePayments.add(gcPaymentMethodId);
            }
        }

        Map<String, Object> optResult = checkOutHelper.setCheckOutOptions(shippingMethod, shippingContactMechId, selectedPaymentMethods,
                  internalCode, shipBeforeDate, shipAfterDate,null);

        ServiceUtil.getMessages(request, optResult, null);
        if (ServiceUtil.isError(optResult)) {
            return "error";
        }

        return "success";
    }

    public static Map<String, Map<String, Object>> getSelectedPaymentMethods(HttpServletRequest request, ShoppingCart cart) {
        //Locale locale = UtilHttp.getLocale(request);
        Map<String, Map<String, Object>> selectedPaymentMethods = new HashMap<String, Map<String, Object>>();
        String[] paymentMethods = request.getParameterValues("checkOutPaymentId");

        if (UtilValidate.isNotEmpty(request.getParameter("issuerId"))) {
            request.setAttribute("issuerId", request.getParameter("issuerId"));
        }

        String errMsg = null;

        if (paymentMethods != null) {
            for (int i = 0; i < paymentMethods.length; i++) {
                Map<String, Object> paymentMethodInfo = FastMap.newInstance();

                String securityCode = request.getParameter("securityCode_" + paymentMethods[i]);
                if (UtilValidate.isNotEmpty(securityCode)) {
                    paymentMethodInfo.put("securityCode", securityCode);
                }
                String amountStr = request.getParameter("amount_" + paymentMethods[i]);
                BigDecimal amount = null;
                if (UtilValidate.isNotEmpty(amountStr) && !"REMAINING".equals(amountStr)) {
                    try {
                        amount = new BigDecimal(amountStr);
                    } catch (NumberFormatException e) {
                        Debug.logError(e, module);
                        errMsg = UtilProperties.getMessage(resource_error, "checkevents.invalid_amount_set_for_payment_method", (cart != null ? cart.getLocale() : Locale.getDefault()));
                        request.setAttribute("_ERROR_MESSAGE_", errMsg);
                        return null;
                    }
                }
                paymentMethodInfo.put("amount", amount);
                selectedPaymentMethods.put(paymentMethods[i], paymentMethodInfo);
            }
        }
        Debug.logInfo("Selected Payment Methods : " + selectedPaymentMethods, module);
        return selectedPaymentMethods;
    }

    /**
     * Determine what billing account amount to use based on the form input.
     * This method returns the amount that will be charged to the billing account.
     * <p>
     * An amount can be associated with the billingAccountId with a
     * parameter billingAccountAmount.  If no amount is specified, then
     * the entire available balance of the given billing account will be used.
     * If there is an error, a null will be returned.
     *
     * @return Amount to charge billing account or null if there was an error
     */
    private static BigDecimal determineBillingAccountAmount(String billingAccountId, String billingAccountAmount, LocalDispatcher dispatcher) {
        BigDecimal billingAccountAmt = null;

        // set the billing account amount to the minimum of billing account available balance or amount input if less than balance
        if (UtilValidate.isNotEmpty(billingAccountId)) {
            // parse the amount to a decimal
            if (UtilValidate.isNotEmpty(billingAccountAmount)) {
                try {
                    billingAccountAmt = new BigDecimal(billingAccountAmount);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            if (billingAccountAmt == null) {
                billingAccountAmt = BigDecimal.ZERO;
            }
            BigDecimal availableBalance = CheckOutHelper.availableAccountBalance(billingAccountId, dispatcher);

            // set amount to be charged to entered amount unless it exceeds the available balance
            BigDecimal chargeAmount = BigDecimal.ZERO;
            if (billingAccountAmt.compareTo(availableBalance) < 0) {
                chargeAmount = billingAccountAmt;
            } else {
                chargeAmount = availableBalance;
            }
            if (chargeAmount.compareTo(BigDecimal.ZERO) < 0.0) {
                chargeAmount = BigDecimal.ZERO;
            }

            return chargeAmount;
        } else {
            return null;
        }
    }

    /**
     * Clears out the cart.
     */
    public void clear() {
        super.clear();

        // clear the auto-save info

        GenericValue ul = this.getUserLogin();
        if (ul == null) {
            ul = this.getAutoUserLogin();
        }
        // autoSaveListId shouldn't be set to null for anonymous user until the list is not cleared from the database
        if (ul != null && !"anonymous".equals(ul.getString("userLoginId"))) {
            this.setAutoSaveListId(null);
        }
        // load the auto-save list ID
        String autoSaveListId = this.getAutoSaveListId();
        if (autoSaveListId == null) {
            try {
                autoSaveListId = ShoppingListEvents.getAutoSaveListId(this.getDelegator(), null, null, ul, null);
            } catch (GeneralException e) {
                Debug.logError(e, module);
            }
        }

        // clear the list
        if (autoSaveListId != null) {
            try {
                ShoppingListEvents.clearListInfo(this.getDelegator(), autoSaveListId);
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        this.setLastListRestore(null);
        this.setAutoSaveListId(null);
    }
    /**
     * Clears out the cart.
     */
    public void wishCartClear() {
        super.clear();
        
        // clear the auto-save info
        
        GenericValue ul = this.getUserLogin();
        if (ul == null) {
            ul = this.getAutoUserLogin();
        }
        // autoSaveListId shouldn't be set to null for anonymous user until the list is not cleared from the database
        if (ul != null && !"anonymous".equals(ul.getString("userLoginId"))) {
            this.setWishListId(null);
        }
        // load the auto-save list ID
        String wishListId = this.getWishListId();
        if (wishListId == null) {
            try {
                wishListId = ShoppingListEvents.getWishListId(this.getDelegator(), null, null, ul, null);
            } catch (GeneralException e) {
                Debug.logError(e, module);
            }
        }
        
        // clear the list
        if (wishListId != null) {
            try {
                ShoppingListEvents.clearListInfo(this.getDelegator(), wishListId);
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        this.setLastListRestore(null);
        this.setWishListId(null);
    }
    
    public void buyCartClear() {
        super.clear();
        
        // clear the auto-save info
        
        GenericValue ul = this.getUserLogin();
        if (ul == null) {
            ul = this.getAutoUserLogin();
        }
        // autoSaveListId shouldn't be set to null for anonymous user until the list is not cleared from the database
        if (ul != null && !"anonymous".equals(ul.getString("userLoginId"))) {
            this.setBuyListId(null);
        }
        // load the auto-save list ID
        String buyListId = this.getBuyListId();
        if (buyListId == null) {
            try {
                buyListId = ShoppingListEvents.getBuyListId(this.getDelegator(), null, null, ul, null);
            } catch (GeneralException e) {
                Debug.logError(e, module);
            }
        }
        
        // clear the list
        if (buyListId != null) {
            try {
                ShoppingListEvents.clearListInfo(this.getDelegator(), buyListId);
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        this.setLastListRestore(null);
        this.setBuyListId(null);
    }
  
}
