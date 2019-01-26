/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.order.shoppingcart;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.icu.text.SimpleDateFormat;
import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.xmlrpc.metadata.Util;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.GeneralRuntimeException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;

import org.ofbiz.marketing.tracking.TrackingCodeEvents;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.party.party.PartyWorker;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.stats.VisitHandler;

/**
 * Events used for processing checkout and orders.
 */
public class CheckOutEvents {

    public static final String module = CheckOutEvents.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";

    public static String cartNotEmpty(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);

        if (cart != null && UtilValidate.isNotEmpty(cart.items())) {
            return "success";
        } else {
            String errMsg = UtilProperties.getMessage(resource_error, "checkevents.cart_empty", (cart != null ? cart.getLocale() : UtilHttp.getLocale(request)));
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
    }

    public static String setCheckOutPages(HttpServletRequest request, HttpServletResponse response) {
        if ("error".equals(CheckOutEvents.cartNotEmpty(request, response)) == true) {
            return "error";
        }

        HttpSession session = request.getSession();

        //Locale locale = UtilHttp.getLocale(request);
        String curPage = request.getParameter("checkoutpage");
        Debug.logInfo("CheckoutPage: " + curPage, module);

        ShoppingCart cart = (ShoppingCart) session.getAttribute("shoppingCart");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        GenericValue userLogin = cart.getUserLogin();
        if (userLogin == null) {
            userLogin = (GenericValue) session.getAttribute("userLogin");
        }
        if (curPage == null) {
            try {
                cart.createDropShipGroups(dispatcher);
            } catch (CartItemModifyException e) {
                Debug.logError(e, module);
            }
        } else if ("shippingoptions".equals(curPage) == true) {
            //remove empty ship group
            cart.cleanUpShipGroups();
        }
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        if ("shippingaddress".equals(curPage) == true) {
            // Set the shipping address options
            String shippingContactMechId = request.getParameter("shipping_contact_mech_id");

            String taxAuthPartyGeoIds = request.getParameter("taxAuthPartyGeoIds");
            String partyTaxId = request.getParameter("partyTaxId");
            String isExempt = request.getParameter("isExempt");

            List<String> errorMessages = new ArrayList<String>();
            Map<String, Object> errorMaps = new HashMap<String, Object>();
            for (int shipGroupIndex = 0; shipGroupIndex < cart.getShipGroupSize(); shipGroupIndex++) {
                // set the shipping method
                if (shippingContactMechId == null) {
                    shippingContactMechId = (String) request.getAttribute("contactMechId"); // FIXME
                }
                String supplierPartyId = (String) request.getAttribute(shipGroupIndex + "_supplierPartyId");
                Map<String, ? extends Object> callResult = checkOutHelper.finalizeOrderEntryShip(shipGroupIndex, shippingContactMechId, supplierPartyId);
                ServiceUtil.addErrors(errorMessages, errorMaps, callResult);
            }

            // if taxAuthPartyGeoIds is not empty drop that into the database
            if (UtilValidate.isNotEmpty(taxAuthPartyGeoIds)) {
                try {
                    Map<String, ? extends Object> createCustomerTaxAuthInfoResult = dispatcher.runSync("createCustomerTaxAuthInfo",
                            UtilMisc.<String, Object>toMap("partyId", cart.getPartyId(), "taxAuthPartyGeoIds", taxAuthPartyGeoIds, "partyTaxId", partyTaxId, "isExempt", isExempt, "userLogin", userLogin));
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

            Map<String, ? extends Object> callResult = checkOutHelper.setCheckOutShippingAddress(shippingContactMechId);
            ServiceUtil.getMessages(request, callResult, null);

            if (!(ServiceUtil.isError(callResult))) {
                // No errors so push the user onto the next page
                curPage = "shippingoptions";
            }
        } else if ("shippingoptions".equals(curPage) == true) {
            // Set the general shipping options
            String shippingMethod = request.getParameter("shipping_method");
            String shippingInstructions = request.getParameter("shipping_instructions");
            String orderAdditionalEmails = request.getParameter("order_additional_emails");
            String maySplit = request.getParameter("may_split");
            String giftMessage = request.getParameter("gift_message");
            String isGift = request.getParameter("is_gift");
            String internalCode = request.getParameter("internalCode");
            String shipBeforeDate =  request.getParameter("shipBeforeDate");
            String shipAfterDate = request.getParameter("shipAfterDate");
            Map<String, ? extends Object> callResult = ServiceUtil.returnSuccess();

            for (int shipGroupIndex = 0; shipGroupIndex < cart.getShipGroupSize(); shipGroupIndex++) {
                callResult = checkOutHelper.finalizeOrderEntryOptions(shipGroupIndex, shippingMethod, shippingInstructions, maySplit, giftMessage, isGift, internalCode, shipBeforeDate, shipAfterDate, orderAdditionalEmails);
                ServiceUtil.getMessages(request, callResult, null);
            }
            if (!(callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
                // No errors so push the user onto the next page
                curPage = "payment";
            }
        } else if ("payment".equals(curPage) == true) {
            // Set the payment options
            Map<String, Map<String, Object>> selectedPaymentMethods = getSelectedPaymentMethods(request);

            String billingAccountId = request.getParameter("billingAccountId");
            if (UtilValidate.isNotEmpty(billingAccountId)) {
                BigDecimal billingAccountAmt = null;
                billingAccountAmt = determineBillingAccountAmount(billingAccountId, request.getParameter("billingAccountAmount"), dispatcher);
                if ((billingAccountId != null) && !"_NA_".equals(billingAccountId) && (billingAccountAmt == null)) {
                    request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderInvalidAmountSetForBillingAccount", UtilMisc.toMap("billingAccountId",billingAccountId), cart.getLocale()));
                    return "error";
                }
                selectedPaymentMethods.put("EXT_BILLACT", UtilMisc.<String, Object>toMap("amount", billingAccountAmt, "securityCode", null));
            }

            if (UtilValidate.isEmpty(selectedPaymentMethods)) {
                return "error";
            }

            List<String> singleUsePayments = new ArrayList<String>();

            // check for gift card not on file
            Map<String, Object> params = UtilHttp.getParameterMap(request);
            Map<String, Object> gcResult = checkOutHelper.checkGiftCard(params, selectedPaymentMethods);
            ServiceUtil.getMessages(request, gcResult, null);
            if (gcResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
                return "error";
            } else {
                String gcPaymentMethodId = (String) gcResult.get("paymentMethodId");
                BigDecimal gcAmount = (BigDecimal) gcResult.get("amount");
                if (gcPaymentMethodId != null) {
                    selectedPaymentMethods.put(gcPaymentMethodId, UtilMisc.<String, Object>toMap("amount", gcAmount, "securityCode", null));
                    if ("Y".equalsIgnoreCase(request.getParameter("singleUseGiftCard"))) {
                        singleUsePayments.add(gcPaymentMethodId);
                    }
                }
            }

            Map<String, Object> callResult = checkOutHelper.setCheckOutPayment(selectedPaymentMethods);
            ServiceUtil.getMessages(request, callResult, null);

            if (!(callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
                // No errors so push the user onto the next page
                curPage = "confirm";
            }
        } else {
            curPage = determineInitialCheckOutPage(cart);
        }

        return curPage;
    }

    private static final String DEFAULT_INIT_CHECKOUT_PAGE = "shippingaddress";

    /**
     * Method to determine the initial checkout page based on requirements. This will also set
     * any cart variables necessary to satisfy the requirements, such as setting the
     * shipment method according to the type of items in the cart.
     */
    public static String determineInitialCheckOutPage(ShoppingCart cart) {
        String page = DEFAULT_INIT_CHECKOUT_PAGE;
        if (cart == null) {
            return page;
        }

        // if no shipping applies, set the no shipment method and skip to payment
        if (!cart.shippingApplies()) {
            cart.setShipmentMethodTypeId("NO_SHIPPING");
            cart.setCarrierPartyId("_NA_");
            page = "payment";
        }

        return page;
    }

    public static String setCheckOutError(HttpServletRequest request, HttpServletResponse response) {
        String currentPage = request.getParameter("checkoutpage");
        if (UtilValidate.isEmpty(currentPage)) {
            return "error";
        } else {
            return currentPage;
        }
    }

    /**
     * Use for quickcheckout submit.  It calculates the tax before setting the payment options.
     * Shipment option should already be set by the quickcheckout form.
     */
    public static String setQuickCheckOutOptions(HttpServletRequest request, HttpServletResponse response) {
        String result = calcTax(request, response);
        if ("error".equals(result)) {
            return "error";
        }
        return setCheckOutOptions(request, response);
    }

    public static String setPartialCheckOutOptions(HttpServletRequest request, HttpServletResponse response) {
        // FIXME response need to be checked ?
        // String resp = setCheckOutOptions(request, response);
        setCheckOutOptions(request, response);
        request.setAttribute("_ERROR_MESSAGE_", null);
        return "success";
    }

    public static String setCartShipToCustomerParty(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        String shipToCustomerPartyId = request.getParameter("shipToCustomerPartyId");
        cart.setShipToCustomerPartyId(shipToCustomerPartyId);
        cart.setShippingContactMechId(null);
        return "success";
    }

    public static String checkPaymentMethods(HttpServletRequest request, HttpServletResponse response) {
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        System.out.println("+++++++++++++++++++++++++ CREATE ORDER - validatePaymentMethods1, START TIME :" + sdf.format(new java.util.Date()));
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);
        Map<String, Object> resp = checkOutHelper.validatePaymentMethods();
        if (ServiceUtil.isError(resp)) {
            request.setAttribute("_ERROR_MESSAGE_", ServiceUtil.getErrorMessage(resp));
            return "error";
        }
        return "success";
    }

    public static Map<String, Map<String, Object>> getSelectedPaymentMethods(HttpServletRequest request) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
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

    // this servlet is used by quick checkout
    public static String setCheckOutOptions(HttpServletRequest request, HttpServletResponse response) {
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        System.out.println("+++++++++++++++++++++++++ CREATE ORDER - setOptions1, START TIME :" + sdf.format(new java.util.Date()));
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        // Set the payment options
        Map<String, Map<String, Object>> selectedPaymentMethods = getSelectedPaymentMethods(request);

        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // get the billing account and amount
        String billingAccountId = request.getParameter("billingAccountId");
        if (UtilValidate.isNotEmpty(billingAccountId)) {
            BigDecimal billingAccountAmt = null;
            billingAccountAmt = determineBillingAccountAmount(billingAccountId, request.getParameter("billingAccountAmount"), dispatcher);
            if (billingAccountAmt == null) {
                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderInvalidAmountSetForBillingAccount", UtilMisc.toMap("billingAccountId",billingAccountId), (cart != null ? cart.getLocale() : Locale.getDefault())));
                return "error";
            }
            selectedPaymentMethods.put("EXT_BILLACT", UtilMisc.<String, Object>toMap("amount", billingAccountAmt, "securityCode", null));
        }

        if (selectedPaymentMethods == null) {
            return "error";
        }

        String shippingMethod = request.getParameter("shipping_method");
        String shippingContactMechId = null;
        if(UtilValidate.isEmpty(request.getParameter("shipping_contact_mech_id"))){
            GenericValue postalAddress = null;
            try {
                postalAddress = EntityUtil.getFirst(delegator.findByAnd("PostalAddress", UtilMisc.toMap("countryGeoId","CHN")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            shippingContactMechId = postalAddress.getString("contacyMechId");
        }else {
            shippingContactMechId = request.getParameter("shipping_contact_mech_id");
        }

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

        Map<String, Object> optResult = checkOutHelper.setCheckOutOptions(shippingMethod, shippingContactMechId, selectedPaymentMethods, internalCode, shipBeforeDate, shipAfterDate,null);

        ServiceUtil.getMessages(request, optResult, null);
        if (ServiceUtil.isError(optResult)) {
            return "error";
        }

        return "success";
    }

    // Create order event - uses createOrder service for processing 注释 by dongxiao 2016.01.5
    /*public static String createOrder(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);
        Map<String, Object> callResult;

        if (UtilValidate.isEmpty(userLogin)) {
            userLogin = cart.getUserLogin();
            session.setAttribute("userLogin", userLogin);
        }
        // remove this whenever creating an order so quick reorder cache will refresh/recalc
        session.removeAttribute("_QUICK_REORDER_PRODUCTS_");

        boolean areOrderItemsExploded = explodeOrderItems(delegator, cart);

        //get the TrackingCodeOrder List
//        List<GenericValue> trackingCodeOrders = TrackingCodeEvents.makeTrackingCodeOrders(request);
        List<GenericValue> trackingCodeOrders = null;
        String distributorId = (String) session.getAttribute("_DISTRIBUTOR_ID_");
        String affiliateId = (String) session.getAttribute("_AFFILIATE_ID_");
        String visitId = VisitHandler.getVisitId(session);
        String webSiteId = CatalogWorker.getWebSiteId(request);

        callResult = checkOutHelper.createOrder(userLogin, distributorId, affiliateId, trackingCodeOrders, areOrderItemsExploded, visitId, webSiteId);
        if (callResult != null) {
            ServiceUtil.getMessages(request, callResult, null);
            if (ServiceUtil.isError(callResult)) {
                // messages already setup with the getMessages call, just return the error response code
                return "error";
            }
            if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS)) {
                // set the orderId for use by chained events
                String orderId = cart.getOrderId();
                request.setAttribute("orderId", orderId);
                request.setAttribute("orderAdditionalEmails", cart.getOrderAdditionalEmails());
            }
        }
        
        String issuerId = request.getParameter("issuerId");
        if (UtilValidate.isNotEmpty(issuerId)) {
            request.setAttribute("issuerId", issuerId);
        }
        

        return cart.getOrderType().toLowerCase();
    }*/

    

    // Event wrapper for the tax calc.
    public static String calcTax(HttpServletRequest request, HttpServletResponse response) {
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        System.out.println("+++++++++++++++++++++++++ CREATE ORDER - calcTax1, START TIME :" + sdf.format(new java.util.Date()));
        try {
            calcTax(request);
        } catch (GeneralException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
            return "error";
        }
        return "success";
    }

    // Invoke the taxCalc
    private static void calcTax(HttpServletRequest request) throws GeneralException {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        
    }

    public static boolean explodeOrderItems(Delegator delegator, ShoppingCart cart) {
        if (cart == null) {
            return false;
        }
        GenericValue productStore = ProductStoreWorker.getProductStore(null, delegator);
        if (productStore == null || productStore.get("explodeOrderItems") == null) {
            return false;
        }
        return productStore.getBoolean("explodeOrderItems").booleanValue();
    }

    public static String checkShipmentNeeded(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        GenericValue productStore = null;
        try {
            productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", null));
            Debug.logInfo("checkShipmentNeeded: reqShipAddrForDigItems=" + productStore.getString("reqShipAddrForDigItems"), module);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error getting ProductStore: " + e.toString(), module);
        }

        

        return "shipmentNeeded";
    }

    // Event wrapper for processPayment.
    public static String processPayment(HttpServletRequest request, HttpServletResponse response) {
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        System.out.println("+++++++++++++++++++++++++ CREATE ORDER - processpayment1, START TIME :" + sdf.format(new java.util.Date()));
        // run the process payment process + approve order when complete; may also run sync fulfillments
        int failureCode = 0;
        try {
            if (!processPayment(request)) {
                failureCode = 1;
            }
        } catch (GeneralException e) {
            Debug.logError(e, module);
            ServiceUtil.setMessages(request, e.getMessage(), null, null);
            failureCode = 2;
        } catch (GeneralRuntimeException e) {
            Debug.logError(e, module);
            ServiceUtil.setMessages(request, e.getMessage(), null, null);
        }

        // event return based on failureCode
        switch (failureCode) {
            case 0:
                return "success";
            case 1:
                return "fail";
            default:
                return "error";
        }
    }

    private static boolean processPayment(HttpServletRequest request) throws GeneralException {
        HttpSession session = request.getSession();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // check if the order is to be held (processing)
        boolean holdOrder = cart.getHoldOrder();

        // load the ProductStore settings
        GenericValue productStore = ProductStoreWorker.getProductStore(null, delegator);
        Map<String, Object> callResult = checkOutHelper.processPayment(productStore, userLogin, false, holdOrder);

        if (ServiceUtil.isError(callResult)) {
            // clear out the rejected payment methods (if any) from the cart, so they don't get re-authorized
            cart.clearDeclinedPaymentMethods(delegator);
            // null out the orderId for next pass
            cart.setOrderId(null);
        }

        // generate any messages required
        ServiceUtil.getMessages(request, callResult, null);

        // check for customer message(s)
        List<String> messages = UtilGenerics.checkList(callResult.get("authResultMsgs"));
        if (UtilValidate.isNotEmpty(messages)) {
            request.setAttribute("_EVENT_MESSAGE_LIST_", messages);
        }

        // determine whether it was a success or failure
        return (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS));
    }

    public static String checkOrderBlacklist(HttpServletRequest request, HttpServletResponse response) {
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        System.out.println("+++++++++++++++++++++++++ CREATE ORDER - checkBlacklist1, START TIME :" + sdf.format(new java.util.Date()));
        HttpSession session = request.getSession();
        ShoppingCart cart = (ShoppingCart) session.getAttribute("shoppingCart");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        CheckOutHelper checkOutHelper = new CheckOutHelper(null, delegator, cart);
        String result;

        Map<String, Object> callResult = checkOutHelper.checkOrderBlackList();
        if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            request.setAttribute("_ERROR_MESSAGE_", callResult.get(ModelService.ERROR_MESSAGE));
            result = "error";
        } else if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_FAIL)) {
            request.setAttribute("_ERROR_MESSAGE_", callResult.get(ModelService.ERROR_MESSAGE));
            result = "failed";
        } else {
            result = (String) callResult.get(ModelService.SUCCESS_MESSAGE);
        }

        return result;
    }

    public static String failedBlacklistCheck(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        ShoppingCart cart = (ShoppingCart) session.getAttribute("shoppingCart");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String orderPartyId = cart.getOrderPartyId();
        GenericValue userLogin = PartyWorker.findPartyLatestUserLogin(orderPartyId, delegator);
        GenericValue currentUser = (GenericValue) session.getAttribute("userLogin");
        String result;

        // Load the properties store
        GenericValue productStore = ProductStoreWorker.getProductStore(null, delegator);
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);
        Map<String, Object> callResult = checkOutHelper.failedBlacklistCheck(userLogin, productStore);

        //Generate any messages required
        ServiceUtil.getMessages(request, callResult, null);

        // wipe the session
        if (("anonymous".equals(currentUser.getString("userLoginId"))) || (currentUser.getString("userLoginId")).equals(userLogin.getString("userLoginId"))) {
            session.invalidate();
        }
        //Determine whether it was a success or not
        if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            result = (String) callResult.get(ModelService.ERROR_MESSAGE);
            request.setAttribute("_ERROR_MESSAGE_", result);
            result = "error";
        } else {
            result = (String) callResult.get(ModelService.ERROR_MESSAGE);
            request.setAttribute("_ERROR_MESSAGE_", result);
            result = "success";
        }
        return result;
    }

    /*public static String checkExternalCheckout(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        GenericValue productStore = ProductStoreWorker.getProductStore(cart.getProductStoreId(), delegator);
        String paymentMethodTypeId = request.getParameter("paymentMethodTypeId");
        if ("EXT_PAYPAL".equals(paymentMethodTypeId) || cart.getPaymentMethodTypeIds().contains("EXT_PAYPAL")) {
            List<GenericValue> payPalProdStorePaySettings = null;
            try {
                payPalProdStorePaySettings = delegator.findByAnd("ProductStorePaymentSetting", "productStoreId", productStore.getString("productStoreId"), "paymentMethodTypeId", "EXT_PAYPAL");
                GenericValue payPalProdStorePaySetting = EntityUtil.getFirst(payPalProdStorePaySettings);
                if (payPalProdStorePaySetting != null) {
                    GenericValue gatewayConfig = payPalProdStorePaySetting.getRelatedOne("PaymentGatewayConfig");
                    if (gatewayConfig != null && "PAYFLOWPRO".equals(gatewayConfig.getString("paymentGatewayConfigTypeId"))) {
                        return "paypal";
                    }
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        return "success";
    }*/

    public static String checkExternalPayment(HttpServletRequest request, HttpServletResponse response) {
        // warning there can only be ONE payment preference for this to work
        // you cannot accept multiple payment type when using an external gateway
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String result;

        String orderId = (String) request.getAttribute("orderId");
        CheckOutHelper checkOutHelper = new CheckOutHelper(null, delegator, null);
        Map<String, Object> callResult = checkOutHelper.checkExternalPayment(orderId);

        //Generate any messages required
        ServiceUtil.getMessages(request, callResult, null);

        // any error messages have prepared for display, return the type ('error' if failed)
        result = (String) callResult.get("type");
        return result;
    }

    public static String finalizeOrderEntry(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
        String shippingContactMechId = null;
        String shippingMethod = null;
        BigDecimal shipEstimate = null; 
        String shippingInstructions = null;
        String maySplit = null;
        String giftMessage = null;
        String isGift = null;
        String internalCode = null;
        String methodType = null;
        //FIXME can be removed ?
        // String singleUsePayment = null;
        // String appendPayment = null;
        String shipBeforeDate = null;
        String shipAfterDate = null;
        String internalOrderNotes = null;
        String shippingNotes = null;

        String mode = request.getParameter("finalizeMode");
        Debug.logInfo("FinalizeMode: " + mode, module);
        // necessary to avoid infinite looping when in a funny state, and will go right back to beginning
        if (mode == null) {
            return "customer";
        }

        // check the userLogin object
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");

        // if null then we must be an anonymous shopper
        if (userLogin == null) {
            // remove auto-login fields
            request.getSession().removeAttribute("autoUserLogin");
            request.getSession().removeAttribute("autoName");
            // clear out the login fields from the cart
            try {
                cart.setAutoUserLogin(null, dispatcher);
            } catch (CartItemModifyException e) {
                Debug.logError(e, module);
            }
        }

        // Reassign items requiring drop-shipping to new or existing drop-ship groups
        if ("init".equals(mode) || "default".equals(mode)) {
            try {
                cart.createDropShipGroups(dispatcher);
            } catch (CartItemModifyException e) {
                Debug.logError(e, module);
            }
        }

        // set the customer info
        if ("default".equals(mode)) {
            cart.setDefaultCheckoutOptions(dispatcher);
        }

        // remove the empty ship groups
        if ("removeEmptyShipGroups".equals(mode)) {
            cart.cleanUpShipGroups();
        }

        // set the customer info
        if ("cust".equals(mode)) {
            String partyId = (String) request.getAttribute("partyId");
            if (partyId != null) {
                cart.setOrderPartyId(partyId);
                // no userLogin means we are an anonymous shopper; fake the UL for service calls
                if (userLogin == null) {
                    try {
                        userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "anonymous"));
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                    }
                    if (userLogin != null) {
                        userLogin.set("partyId", partyId);
                    }
                    request.getSession().setAttribute("userLogin", userLogin);
                    try {
                        cart.setUserLogin(userLogin, dispatcher);
                    } catch (CartItemModifyException e) {
                        Debug.logError(e, module);
                    }
                    Debug.logInfo("Anonymous user-login has been activated", module);
                }
            }
        }

        if ("addpty".equals(mode)) {
            cart.setAttribute("addpty", "Y");
        }

        if ("term".equals(mode)) {
           cart.setOrderTermSet(true);
        }

        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // ====================================================================================
        if ("ship".equals(mode) || "options".equals(mode)) {
            Map<String, Object> callResult = ServiceUtil.returnSuccess();
            List<String> errorMessages = new ArrayList<String>();
            Map<String, Object> errorMaps = new HashMap<String, Object>();
            for (int shipGroupIndex = 0; shipGroupIndex < cart.getShipGroupSize(); shipGroupIndex++) {
                // set the shipping method
                if ("ship".equals(mode)) {
                    shippingContactMechId = request.getParameter(shipGroupIndex + "_shipping_contact_mech_id");
                    String facilityId = request.getParameter(shipGroupIndex + "_shipGroupFacilityId");
                    if (shippingContactMechId == null) {
                        shippingContactMechId = (String) request.getAttribute("contactMechId"); // FIXME
                    } else if("PURCHASE_ORDER".equals(cart.getOrderType())){
                        String[] shipInfo = shippingContactMechId.split("_@_");
                        if(shipInfo.length > 1){
                            shippingContactMechId = shipInfo[0];
                            facilityId = shipInfo[1];   
                        }
                    }
                    String supplierPartyId = request.getParameter(shipGroupIndex + "_supplierPartyId");
                    if (UtilValidate.isNotEmpty(facilityId)) {
                        cart.setShipGroupFacilityId(shipGroupIndex, facilityId);
                    }
                    callResult = checkOutHelper.finalizeOrderEntryShip(shipGroupIndex, shippingContactMechId, supplierPartyId);
                    ServiceUtil.addErrors(errorMessages, errorMaps, callResult);
                }
                // set the options
                if ("options".equals(mode)) {
                    shippingMethod = request.getParameter(shipGroupIndex + "_shipping_method");
                    if (UtilValidate.isEmpty(shippingMethod)) {
                        shippingMethod = request.getParameter("shipping_method");
                    }
                    shippingInstructions = request.getParameter(shipGroupIndex + "_shipping_instructions");
                    if (UtilValidate.isEmpty(shippingInstructions)) {
                        shippingInstructions = request.getParameter("shipping_instructions");
                    }
                    maySplit = request.getParameter(shipGroupIndex + "_may_split");
                    if (UtilValidate.isEmpty(maySplit)) {
                        maySplit = request.getParameter("may_split");
                    }
                    giftMessage = request.getParameter(shipGroupIndex + "_gift_message");
                    isGift = request.getParameter(shipGroupIndex + "_is_gift");
                    internalCode = request.getParameter("internalCode"); // FIXME
                    shipBeforeDate = request.getParameter("sgi" + shipGroupIndex + "_shipBeforeDate");
                    shipAfterDate = request.getParameter("sgi" + shipGroupIndex + "_shipAfterDate");
                    internalOrderNotes = request.getParameter("internal_order_notes");
                    shippingNotes = request.getParameter("shippingNotes");
                    if (UtilValidate.isNotEmpty(request.getParameter(shipGroupIndex + "_ship_estimate"))) {
                        shipEstimate = new BigDecimal(request.getParameter(shipGroupIndex + "_ship_estimate"));
                    }
                    cart.clearOrderNotes();
                    cart.clearInternalOrderNotes();
                    if (shipEstimate == null) {  // allow ship estimate to be set manually if a purchase order
                        callResult = checkOutHelper.finalizeOrderEntryOptions(shipGroupIndex, shippingMethod, shippingInstructions, maySplit, giftMessage, isGift, internalCode, shipBeforeDate, shipAfterDate, internalOrderNotes, shippingNotes);
                    } else {
                        callResult = checkOutHelper.finalizeOrderEntryOptions(shipGroupIndex, shippingMethod, shippingInstructions, maySplit, giftMessage, isGift, internalCode, shipBeforeDate, shipAfterDate, internalOrderNotes, shippingNotes, shipEstimate);
                    }
                    ServiceUtil.addErrors(errorMessages, errorMaps, callResult);
                }
            }
            //See whether we need to return an error or not
            callResult = ServiceUtil.returnSuccess();
            if (errorMessages.size() > 0) {
                callResult.put(ModelService.ERROR_MESSAGE_LIST,  errorMessages);
                callResult.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            }
            if (errorMaps.size() > 0) {
                callResult.put(ModelService.ERROR_MESSAGE_MAP, errorMaps);
                callResult.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            }
            // generate any messages required
            ServiceUtil.getMessages(request, callResult, null);
            // determine whether it was a success or not
            if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
                if ("ship".equals(mode)) {
                    return "shipping";
                }
                if ("options".equals(mode)) {
                    return "options";
                }
                return "error";
            }
        }
        // ###############################################################################

        // check for offline payment type
        // payment option; if offline we skip the payment screen
        methodType = request.getParameter("paymentMethodType");
        if ("offline".equals(methodType)) {
            Debug.logInfo("Changing mode from->to: " + mode + "->payment", module);
            mode = "payment";
        }
        //FIXME can be removed ?
        // singleUsePayment = request.getParameter("singleUsePayment");
        // appendPayment = request.getParameter("appendPayment");
        // boolean isSingleUsePayment = singleUsePayment != null && "Y".equalsIgnoreCase(singleUsePayment) ? true : false;
        // boolean doAppendPayment = appendPayment != null && "Y".equalsIgnoreCase(appendPayment) ? true : false;

        if ("payment".equals(mode)) {
            Map<String, Object> callResult = ServiceUtil.returnSuccess();
            List<String> errorMessages = new ArrayList<String>();
            Map<String, Object> errorMaps = new HashMap<String, Object>();

            // Set the payment options
            Map<String, Map<String, Object>> selectedPaymentMethods = getSelectedPaymentMethods(request);

            // Set the billing account (if any)
            String billingAccountId = request.getParameter("billingAccountId");
            if (UtilValidate.isNotEmpty(billingAccountId)) {
                BigDecimal billingAccountAmt = null;
                billingAccountAmt = determineBillingAccountAmount(billingAccountId, request.getParameter("billingAccountAmount"), dispatcher);
                if (billingAccountAmt == null) {
                    request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resource_error,"OrderInvalidAmountSetForBillingAccount", UtilMisc.toMap("billingAccountId",billingAccountId), (cart != null ? cart.getLocale() : Locale.getDefault())));
                    return "error";
                }
                selectedPaymentMethods.put("EXT_BILLACT", UtilMisc.<String, Object>toMap("amount", billingAccountAmt, "securityCode", null));
            }

            // If the user has just created a new payment method, add it to the map with a null amount, so that
            //  it becomes the sole payment method for the order.
            String newPaymentMethodId = (String) request.getAttribute("paymentMethodId");
            if (! UtilValidate.isEmpty(newPaymentMethodId)) {
                selectedPaymentMethods.put(newPaymentMethodId, null);
                if (!selectedPaymentMethods.containsKey(newPaymentMethodId)) {
                    selectedPaymentMethods.put(newPaymentMethodId, UtilMisc.toMap("amount", null, "securityCode", null));
                }
            }

            // The selected payment methods are set
            errorMessages.addAll(checkOutHelper.setCheckOutPaymentInternal(selectedPaymentMethods));
            // Verify if a gift card has been selected during order entry
            callResult = checkOutHelper.checkGiftCard(paramMap, selectedPaymentMethods);
            ServiceUtil.addErrors(errorMessages, errorMaps, callResult);
            if (errorMessages.size() == 0 && errorMaps.size() == 0) {
                String gcPaymentMethodId = (String) callResult.get("paymentMethodId");
                BigDecimal giftCardAmount = (BigDecimal) callResult.get("amount");
                // WARNING: if gcPaymentMethodId is not empty, all the previously set payment methods will be removed
                Map<String, Object> gcCallRes = checkOutHelper.finalizeOrderEntryPayment(gcPaymentMethodId, giftCardAmount, true, true);
                ServiceUtil.addErrors(errorMessages, errorMaps, gcCallRes);
            }
            //See whether we need to return an error or not
            callResult = ServiceUtil.returnSuccess();
            if (errorMessages.size() > 0) {
                callResult.put(ModelService.ERROR_MESSAGE_LIST, errorMessages);
                callResult.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            }
            if (errorMaps.size() > 0) {
                callResult.put(ModelService.ERROR_MESSAGE_MAP, errorMaps);
                callResult.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            }
            // generate any messages required
            ServiceUtil.getMessages(request, callResult, null);
            // determine whether it was a success or not
            if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
                return "paymentError";
            }
        }
        // determine where to direct the browser
        return determineNextFinalizeStep(request, response);
    }

    public static String determineNextFinalizeStep(HttpServletRequest request, HttpServletResponse response) {
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        // flag anoymous checkout to bypass additional party settings
        boolean isAnonymousCheckout = false;
        if (userLogin != null && "anonymous".equals(userLogin.getString("userLoginId"))) {
            isAnonymousCheckout = true;
        }

        // determine where to direct the browser
        // these are the default values
        boolean requireCustomer = true;
        boolean requireNewShippingAddress = false;
        boolean requireShipping = true;
        boolean requireOptions = true;
        boolean requireShipGroups = false;
        boolean requirePayment = !"PURCHASE_ORDER".equals(cart.getOrderType());
        boolean requireTerm = true;
        boolean requireAdditionalParty = isAnonymousCheckout;
        boolean isSingleUsePayment = true;
        // these options are not available to anonymous shoppers (security)
        if (userLogin != null && !"anonymous".equals(userLogin.getString("userLoginId"))) {
            String requireCustomerStr = request.getParameter("finalizeReqCustInfo");
            String requireNewShippingAddressStr = request.getParameter("finalizeReqNewShipAddress");
            String requireShippingStr = request.getParameter("finalizeReqShipInfo");
            String requireOptionsStr = request.getParameter("finalizeReqOptions");
            String requirePaymentStr = request.getParameter("finalizeReqPayInfo");
            String requireTermStr = request.getParameter("finalizeReqTermInfo");
            String requireAdditionalPartyStr = request.getParameter("finalizeReqAdditionalParty");
            String requireShipGroupsStr = request.getParameter("finalizeReqShipGroups");
            String singleUsePaymentStr = request.getParameter("singleUsePayment");
            requireCustomer = requireCustomerStr == null || "true".equalsIgnoreCase(requireCustomerStr);
            requireNewShippingAddress = requireNewShippingAddressStr != null && "true".equalsIgnoreCase(requireNewShippingAddressStr);
//            requireShipping = requireShippingStr == null || requireShippingStr.equalsIgnoreCase("true");
            requireShipping = false;
            requireOptions = requireOptionsStr == null || "true".equalsIgnoreCase(requireOptionsStr);
            requireShipGroups = requireShipGroupsStr != null && "true".equalsIgnoreCase(requireShipGroupsStr);
            if (requirePayment) {
                requirePayment = requirePaymentStr == null || "true".equalsIgnoreCase(requirePaymentStr);
            }
            if (requireTerm) {
                requireTerm = requireTermStr == null || "true".equalsIgnoreCase(requireTermStr);
            }
            requireAdditionalParty = requireAdditionalPartyStr == null || "true".equalsIgnoreCase(requireAdditionalPartyStr);
            isSingleUsePayment = singleUsePaymentStr != null && "Y".equalsIgnoreCase(singleUsePaymentStr) ? true : false;
        }

        boolean shippingAddressSet = true;
        boolean shippingOptionsSet = true;
        for (int shipGroupIndex = 0; shipGroupIndex < cart.getShipGroupSize(); shipGroupIndex++) {
            String shipContactMechId = cart.getShippingContactMechId(shipGroupIndex);
            if (shipContactMechId == null) {
                shippingAddressSet = false;
            }
            String shipmentMethodTypeId = cart.getShipmentMethodTypeId(shipGroupIndex);
            if (shipmentMethodTypeId == null) {
                shippingOptionsSet = false;
            }
        }

        String customerPartyId = cart.getPartyId();

        String[] processOrder = {"customer", "shipping", "shipGroups", "options", "term", "payment",
                                 "addparty", "paysplit"};

        if ("PURCHASE_ORDER".equals(cart.getOrderType())) {
            // Force checks for the following
            requireCustomer = true; requireShipping = true; requireOptions = true;
            processOrder = new String[] {"customer", "term", "shipping", "shipGroups", "options", "payment",
                                         "addparty", "paysplit"};
        }

        for (int i = 0; i < processOrder.length; i++) {
            String currProcess = processOrder[i];
            if ("customer".equals(currProcess)) {
                if (requireCustomer && (customerPartyId == null || "_NA_".equals(customerPartyId))) {
                    return "customer";
                }
            } else if ("shipping".equals(currProcess)) {
                if (requireShipping) {
                    if (requireNewShippingAddress) {
                        return "shippingAddress";
                    } else if (!shippingAddressSet) {
                        return "shipping";
                    }
                }
            } else if ("shipGroups".equals(currProcess)) {
                if (requireShipGroups) {
                    return "shipGroups";
                }
            } else if ("options".equals(currProcess)) {
                if (requireOptions && !shippingOptionsSet) {
                    return "options";
                }
            } else if ("term".equals(currProcess)) {
                if (requireTerm && !cart.isOrderTermSet()) {
                    return "term";
                }
            } else if ("payment".equals(currProcess)) {
                List<String> paymentMethodIds = cart.getPaymentMethodIds();
                List<String> paymentMethodTypeIds = cart.getPaymentMethodTypeIds();
                if (requirePayment && UtilValidate.isEmpty(paymentMethodIds) && UtilValidate.isEmpty(paymentMethodTypeIds)) {
                    return "payment";
                }
            } else if ("addparty".equals(currProcess)) {
                if (requireAdditionalParty && cart.getAttribute("addpty") == null) {
                    return "addparty";
                }
            } else if ("paysplit".equals(currProcess)) {
                if (isSingleUsePayment) {
                    return "paysplit";
                }
            }
        }

        // Finally, if all checks go through, finalize the order.

       // this is used to go back to a previous page in checkout after processing all of the changes, just to make sure we get everything...
        String checkoutGoTo = request.getParameter("checkoutGoTo");
        if (UtilValidate.isNotEmpty(checkoutGoTo)) {
            return checkoutGoTo;
        }

        if ("SALES_ORDER".equals(cart.getOrderType())) {
            return "sales";
        } else {
            return "po";
        }
    }

    public static String finalizeOrderEntryError(HttpServletRequest request, HttpServletResponse response) {
        String finalizePage = request.getParameter("finalizeMode");
        if (UtilValidate.isEmpty(finalizePage)) {
            return "error";
        } else {
            return finalizePage;
        }
    }

    /**
     * Determine what billing account amount to use based on the form input.
     * This method returns the amount that will be charged to the billing account.
     *
     * An amount can be associated with the billingAccountId with a
     * parameter billingAccountAmount.  If no amount is specified, then
     * the entire available balance of the given billing account will be used.
     * If there is an error, a null will be returned.
     *
     * @return  Amount to charge billing account or null if there was an error
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
     * add by dx 2016.01.05
     * 完成订单
     * @param request
     * @param response
     * @return
     */
    public static String salesFinalizeOrderEntry(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> paramMap = UtilHttp.getParameterMap(request);
        String shippingContactMechId = null;
        String shippingMethod = null;
        BigDecimal shipEstimate = null;
        String shippingInstructions = null;
        String maySplit = null;
        String giftMessage = null;
        String isGift = null;
        String internalCode = null;
        String methodType = null;
        String shipBeforeDate = null;
        String shipAfterDate = null;
        String internalOrderNotes = null;
        String shippingNotes = null;
        String mode = request.getParameter("finalizeMode");
        // check the userLogin object
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        // 产品店铺
        String currenProductStoreId = request.getParameter("currenProductStoreId");
        // 销售渠道
        String currenSalesChannelEnumId = request.getParameter("currenSalesChannelEnumId");
        // 支付方法
        String currenPaymentMethodTypeAndId = request.getParameter("currenPaymentMethodTypeAndId");
        // 送货方式
        String currenSalesShippingMethod = request.getParameter("currenSalesShippingMethod");
        // 送货地址
        String currenShippingContactMechId = request.getParameter("currenShippingContactMechId");
        //创建订单收货地址
        if (UtilValidate.isEmpty(currenShippingContactMechId)){
            Map<String, Object> contactMechMap = new HashMap<String, Object>();
            String contactMechId = delegator.getNextSeqId("ContactMech");
            GenericValue contactMech = null;
            try {
                contactMech = delegator.create("ContactMech",UtilMisc.toMap("contactMechId",contactMechId,"contactMechTypeId","POSTAL_ADDRESS"));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            //创建地址
            if (UtilValidate.isNotEmpty(contactMech)){
                String consigneeName = request.getParameter("consigneeName");
                String countryGeoId = request.getParameter("countryGeoId");
                String stateProvinceGeoId = request.getParameter("stateProvinceGeoId");
                String city = request.getParameter("city");
                String countyGeoId = request.getParameter("countyGeoId");
                String consigneeAddress = request.getParameter("consigneeAddress");
                String phone = request.getParameter("phone");
                String tel = request.getParameter("tel");
                String postalCode = request.getParameter("postalCode");
                try {
                    delegator.create("PostalAddress",UtilMisc.toMap("contactMechId",contactMech.getString("contactMechId"),"toName",consigneeName,"address1",consigneeAddress,
                    "city",city,"countryGeoId",countryGeoId,"stateProvinceGeoId",stateProvinceGeoId,"countyGeoId",countyGeoId,"mobilePhone",phone,"tel",tel,"postalCode",postalCode));
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            //创建订单地址
            if (UtilValidate.isNotEmpty(contactMech)){
                currenShippingContactMechId = contactMech.getString("contactMechId");
            }

        }
        // 客户名称
        String currenPartyId = request.getParameter("currenPartyId");
        // 经销商标识
        String currenPartyIdDistribute = request.getParameter("currenPartyIdDistribute");
        // 仓库
        String currenShipGroupFacilityId = request.getParameter("currenShipGroupFacilityId");
        // 订单名称
        String currenSalesOrderName = request.getParameter("currenSalesOrderName");
        // 选择目录
        String currenCatalogId = request.getParameter("currenCatalogId");
        // 缺省最早送货日期
        String currenShipAfterDate = request.getParameter("currenShipAfterDate");
        // 缺省最迟送货日期
        String currenShipBeforeDate = request.getParameter("currenShipBeforeDate");
        // 预约单号
        String currenReserveOrderId = request.getParameter("currenReserveOrderId");
        // 拆分偏好
        String currenMaySplit = request.getParameter("currenMaySplit");
        // 货币
        String currencyUomId = request.getParameter("currencyUomId");
        // 是否开运输发票
        String currenInvoicePerShipment = request.getParameter("currenInvoicePerShipment");
        // 发票标题
        String currenInvoiceTitle = request.getParameter("currenInvoiceTitle");
        //配送方式
        String distributionMethod = request.getParameter("distributionMethod");

        //add by dong xiao 2016.2.15 订单发票相关
        String invoiceType = request.getParameter("invoiceType");           //发票类型
        String invoiceTitle = request.getParameter("invoiceTitle");         //发票抬头
        String invoiceContent = request.getParameter("invoiceContent");     //发票内容
        cart.setInvoiceType(invoiceType);
        cart.setInvoiceTitle(invoiceTitle);
       
        //add by dongxiao 2016.1.27
        BigDecimal originalMoney = (UtilValidate.isNotEmpty(request.getParameter("originalMoney")))? new BigDecimal(request.getParameter("originalMoney")) : null;
        BigDecimal distributeMoney = (UtilValidate.isNotEmpty(request.getParameter("distributeMoney")))? new BigDecimal(request.getParameter("distributeMoney")) : null;
        BigDecimal businessHandDiscount = (UtilValidate.isNotEmpty(request.getParameter("businessHandDiscount")))? new BigDecimal(request.getParameter("businessHandDiscount")) : null;
        BigDecimal businessDiscountTotal = (UtilValidate.isNotEmpty(request.getParameter("businessDiscountTotal")))? new BigDecimal(request.getParameter("businessDiscountTotal")) : null;
        BigDecimal integralDiscount = (UtilValidate.isNotEmpty(request.getParameter("integralDiscount")))? new BigDecimal(request.getParameter("integralDiscount")) : null;
        BigDecimal platDiscount = (UtilValidate.isNotEmpty(request.getParameter("platDiscount")))? new BigDecimal(request.getParameter("platDiscount")) : null;
        BigDecimal platDiscountTotal = (UtilValidate.isNotEmpty(request.getParameter("platDiscountTotal")))? new BigDecimal(request.getParameter("platDiscountTotal")) : null;
        BigDecimal discountMoney = (UtilValidate.isNotEmpty(request.getParameter("discountMoney")))? new BigDecimal(request.getParameter("discountMoney")) : null;
        BigDecimal shouldPayMoney = (UtilValidate.isNotEmpty(request.getParameter("shouldPayMoney")))? new BigDecimal(request.getParameter("shouldPayMoney")) : null;
        BigDecimal getIntegral = (UtilValidate.isNotEmpty(request.getParameter("getIntegral")))? new BigDecimal(request.getParameter("getIntegral")) : null;
        BigDecimal actualPayMoney = (UtilValidate.isNotEmpty(request.getParameter("actualPayMoney")))? new BigDecimal(request.getParameter("actualPayMoney")) : null;
        BigDecimal notPayMoney = (UtilValidate.isNotEmpty(request.getParameter("notPayMoney")))? new BigDecimal(request.getParameter("notPayMoney")) : null;
        String remarks = (UtilValidate.isNotEmpty(request.getParameter("remarks")))? request.getParameter("remarks") : null;
        // 销售订单参数取得
        setSalesOrderCurrencyAgreementShipDates(request, response, currenSalesOrderName,
                currenCatalogId, currenShipAfterDate, currenShipBeforeDate, currencyUomId, currenPartyIdDistribute,
                currenReserveOrderId, currenInvoicePerShipment, currenInvoiceTitle);
        // //////////////////////////////////////////////////////////////////////

        // 参数的保存
        ShoppingCartEvents.saveSalesCurrInfo(request, response);

        

        if (remarks != null){
            cart.setRemarks(remarks);
        }


        // 经销渠道
        if (UtilValidate.isNotEmpty(currenSalesChannelEnumId)) {
            cart.setChannelType(currenSalesChannelEnumId);
        }

        // if null then we must be an anonymous shopper
        if (userLogin == null) {
            // remove auto-login fields
            request.getSession().removeAttribute("autoUserLogin");
            request.getSession().removeAttribute("autoName");
            // clear out the login fields from the cart
            try {
                cart.setAutoUserLogin(null, dispatcher);
            } catch (CartItemModifyException e) {
                Debug.logError(e, module);
            }
        }

        // 附加会员记录
        cart.setAttribute("addpty", "Y");

        // 订单条款
        cart.setOrderTermSet(true);

        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // ====================================================================================
        // if (mode.equals("ship") || mode.equals("options")) {
        Map<String, Object> callResult = ServiceUtil.returnSuccess();
        List<String> errorMessages = new ArrayList<String>();
        Map<String, Object> errorMaps = new HashMap<String, Object>();
        for (int shipGroupIndex = 0; shipGroupIndex < cart.getShipGroupSize(); shipGroupIndex++) {
            // 发货设置
            // shippingContactMechId = request.getParameter(shipGroupIndex + "_shipping_contact_mech_id");
            shippingContactMechId = currenShippingContactMechId;
            // String facilityId = request.getParameter(shipGroupIndex + "_shipGroupFacilityId");
            // 仓库
            String facilityId = currenShipGroupFacilityId;
            if (shippingContactMechId == null) {
                shippingContactMechId = (String) request.getAttribute("contactMechId"); // FIXME
            } else if ("PURCHASE_ORDER".equals(cart.getOrderType())) {
                String[] shipInfo = shippingContactMechId.split("_@_");
                if (shipInfo.length > 1) {
                    shippingContactMechId = shipInfo[0];
                    facilityId = shipInfo[1];
                }
            }
            String supplierPartyId = request.getParameter(shipGroupIndex + "_supplierPartyId");
            if (UtilValidate.isNotEmpty(facilityId)) {
                cart.setShipGroupFacilityId(shipGroupIndex, facilityId);
                cart.setFacilityId(facilityId);
            }
            callResult = checkOutHelper.finalizeOrderEntryShip(shipGroupIndex, shippingContactMechId, supplierPartyId);
            ServiceUtil.addErrors(errorMessages, errorMaps, callResult);

            // 订单选项设置
            // shippingMethod = request.getParameter(shipGroupIndex + "_shipping_method");
            shippingMethod = currenSalesShippingMethod;

            if (UtilValidate.isEmpty(shippingMethod)) {
                shippingMethod = request.getParameter("shipping_method");
            }
            shippingInstructions = request.getParameter(shipGroupIndex + "_shipping_instructions");
            if (UtilValidate.isEmpty(shippingInstructions)) {
                shippingInstructions = request.getParameter("shipping_instructions");
            }
            // maySplit = request.getParameter(shipGroupIndex + "_may_split");
            maySplit = currenMaySplit;
            if (UtilValidate.isEmpty(maySplit)) {
                maySplit = request.getParameter("may_split");
            }
            giftMessage = request.getParameter(shipGroupIndex + "_gift_message");
            isGift = request.getParameter(shipGroupIndex + "_is_gift");
            internalCode = request.getParameter("internalCode"); // FIXME
            // shipBeforeDate = request.getParameter("sgi" + shipGroupIndex + "_shipBeforeDate");

            if (UtilValidate.isNotEmpty(currenShipBeforeDate)) {
                shipBeforeDate = string2Date(currenShipBeforeDate);
            }
            // shipAfterDate = request.getParameter("sgi" + shipGroupIndex + "_shipAfterDate");
            if (UtilValidate.isNotEmpty(currenShipAfterDate)) {
                shipAfterDate = string2Date(currenShipAfterDate);
            }

            internalOrderNotes = request.getParameter("internal_order_notes");
            shippingNotes = request.getParameter("shippingNotes");
            if (UtilValidate.isNotEmpty(request.getParameter(shipGroupIndex + "_ship_estimate"))) {
                shipEstimate = new BigDecimal(request.getParameter(shipGroupIndex + "_ship_estimate"));
            }
            cart.clearOrderNotes();
            cart.clearInternalOrderNotes();
            if (shipEstimate == null) { // allow ship estimate to be set manually if a purchase order
                callResult = checkOutHelper.finalizeOrderEntryOptions(shipGroupIndex, shippingMethod, shippingInstructions,
                        maySplit, giftMessage, isGift, internalCode, shipBeforeDate, shipAfterDate, internalOrderNotes,
                        shippingNotes);
            } else {
                callResult = checkOutHelper.finalizeOrderEntryOptions(shipGroupIndex, shippingMethod, shippingInstructions,
                        maySplit, giftMessage, isGift, internalCode, shipBeforeDate, shipAfterDate, internalOrderNotes,
                        shippingNotes, shipEstimate);
            }
            ServiceUtil.addErrors(errorMessages, errorMaps, callResult);
        }
        // See whether we need to return an error or not
        callResult = ServiceUtil.returnSuccess();
        if (errorMessages.size() > 0) {
            callResult.put(ModelService.ERROR_MESSAGE_LIST, errorMessages);
            callResult.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
        }
        if (errorMaps.size() > 0) {
            callResult.put(ModelService.ERROR_MESSAGE_MAP, errorMaps);
            callResult.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
        }
        // generate any messages required
        ServiceUtil.getMessages(request, callResult, null);
        // determine whether it was a success or not
        if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            return "error";
        }
        // ###############################################################################

        // check for offline payment type
        // payment option; if offline we skip the payment screen
        methodType = request.getParameter("paymentMethodType");
        if ("offline".equals(methodType)) {
            Debug.logInfo("Changing mode from->to: " + mode + "->payment", module);
            mode = "payment";
        }
        // FIXME can be removed ?
        // singleUsePayment = request.getParameter("singleUsePayment");
        // appendPayment = request.getParameter("appendPayment");
        // boolean isSingleUsePayment = singleUsePayment != null && "Y".equalsIgnoreCase(singleUsePayment) ? true : false;
        // boolean doAppendPayment = appendPayment != null && "Y".equalsIgnoreCase(appendPayment) ? true : false;

        // if (mode.equals("payment")) {
        // 支付设定
        Map<String, Object> callResultPay = ServiceUtil.returnSuccess();
        List<String> errorMessagesPay = new ArrayList<String>();
        Map<String, Object> errorMapsPay = new HashMap<String, Object>();

        // Set the payment options
        // Map<String, Map<String, Object>> selectedPaymentMethods = getSelectedPaymentMethods(request);
        Map<String, Map<String, Object>> selectedPaymentMethods = getSalesSelectedPaymentMethods(request,
                currenPaymentMethodTypeAndId);

        // Set the billing account (if any)
        String billingAccountId = request.getParameter("billingAccountId");
        if (UtilValidate.isNotEmpty(billingAccountId)) {
            BigDecimal billingAccountAmt = null;
            billingAccountAmt = determineBillingAccountAmount(billingAccountId, request.getParameter("billingAccountAmount"),
                    dispatcher);
            if (billingAccountAmt == null) {
                request.setAttribute(
                        "_ERROR_MESSAGE_",
                        UtilProperties.getMessage(resource_error, "OrderInvalidAmountSetForBillingAccount",
                                UtilMisc.toMap("billingAccountId", billingAccountId),
                                (cart != null ? cart.getLocale() : Locale.getDefault())));
                return "error";
            }
            selectedPaymentMethods.put("EXT_BILLACT",
                    UtilMisc.<String, Object> toMap("amount", billingAccountAmt, "securityCode", null));
        }

        // If the user has just created a new payment method, add it to the map with a null amount, so that
        // it becomes the sole payment method for the order.
        String newPaymentMethodId = (String) request.getAttribute("paymentMethodId");
        if (!UtilValidate.isEmpty(newPaymentMethodId)) {
            selectedPaymentMethods.put(newPaymentMethodId, null);
            if (!selectedPaymentMethods.containsKey(newPaymentMethodId)) {
                selectedPaymentMethods.put(newPaymentMethodId, UtilMisc.toMap("amount", null, "securityCode", null));
            }
        }

        // The selected payment methods are set
        errorMessagesPay.addAll(checkOutHelper.setCheckOutPaymentInternal(selectedPaymentMethods));
        // Verify if a gift card has been selected during order entry
        callResultPay = checkOutHelper.checkGiftCard(paramMap, selectedPaymentMethods);
        ServiceUtil.addErrors(errorMessagesPay, errorMapsPay, callResultPay);
        if ((errorMessagesPay.size() == 0) && (errorMapsPay.size() == 0)) {
            String gcPaymentMethodId = (String) callResultPay.get("paymentMethodId");
            BigDecimal giftCardAmount = (BigDecimal) callResultPay.get("amount");
            // WARNING: if gcPaymentMethodId is not empty, all the previously set payment methods will be removed
            Map<String, Object> gcCallRes = checkOutHelper.finalizeOrderEntryPayment(gcPaymentMethodId, giftCardAmount, true,
                    true);
            ServiceUtil.addErrors(errorMessagesPay, errorMapsPay, gcCallRes);
        }
        // See whether we need to return an error or not
        callResultPay = ServiceUtil.returnSuccess();
        if (errorMessagesPay.size() > 0) {
            callResultPay.put(ModelService.ERROR_MESSAGE_LIST, errorMessagesPay);
            callResultPay.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
        }
        if (errorMapsPay.size() > 0) {
            callResultPay.put(ModelService.ERROR_MESSAGE_MAP, errorMapsPay);
            callResultPay.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
        }
        // generate any messages required
        ServiceUtil.getMessages(request, callResultPay, null);
        // determine whether it was a success or not
        if (callResultPay.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            return "paymentError";
        }
        // }
        // determine where to direct the browser
        return determineNextFinalizeStep(request, response);
    }


    /**
     * add by AlexYao 2016.01.28
     * 完成订单
     * @param request
     * @param response
     * @return
     */
    public static String checkCartNotEmpty(HttpServletRequest request, HttpServletResponse response) {
//        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
//        System.out.println("+++++++++++++++++++++++++ CREATE ORDER - orderSubmit, START TIME :" + sdf.format(new java.util.Date()));
        Map resultData = new HashMap();
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String activityId = request.getParameter("activityId");//活动id
        String userLoginId = request.getParameter("userLoginId");//用户登陆id
        String productId = request.getParameter("add_product_id");//商品id
        String quantity = request.getParameter("quantity");//商品数量
        String partyId = request.getParameter("shipToCustomerPartyId");//会员id
        String productType = request.getParameter("type");//商品类型
        String logistics = request.getParameter("logistics");//货运方式
        String price = request.getParameter("price");//购买的单价，即惠后单价
        String telphone = request.getParameter("telphone");//接收券号的手机号
        String score = request.getParameter("score");//使用积分
        String balance = request.getParameter("balance");//使用余额
        String total = request.getParameter("total");//实际支付价格
        String productStoreId = request.getParameter("productStoreId");//店铺id
        String remarks = request.getParameter("remarks");//备注
        String contactMechId = request.getParameter("shipping_contact_mech_id");//收货地址id
        if(UtilValidate.isNotEmpty(logistics) && !"SMZT".equals(logistics) && UtilValidate.isNotEmpty(contactMechId)){
            GenericValue postallAddress = null;
            try {
                postallAddress = delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId",contactMechId));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isEmpty(postallAddress) || UtilValidate.isEmpty(postallAddress.get("communityId"))){
                resultData.put("status",false);
                resultData.put("info","您的收货地址信息不完整，请补充填写！");
                request.setAttribute("resultData", resultData);
                return "error";
            }
        }
        GenericValue productActivity = null;
        if(UtilValidate.isEmpty(partyId)){
            resultData.put("status",false);
            resultData.put("info","会员id不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isEmpty(quantity)){
            resultData.put("status",false);
            resultData.put("info","商品数量不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isEmpty(productType)){
            resultData.put("status",false);
            resultData.put("info","商品类型不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isEmpty(productId)){
            resultData.put("status",false);
            resultData.put("info","商品id不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isEmpty(activityId)){
            resultData.put("status",false);
            resultData.put("info","活动id不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }else{
            //验证活动
            try {
                productActivity = delegator.findByPrimaryKey("ProductActivity",UtilMisc.toMap("activityId",activityId));//活动主表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isEmpty(productActivity)){
                resultData.put("status",false);
                resultData.put("info","活动不存在");
                request.setAttribute("resultData", resultData);
                return "error";
            }else{
                Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
                if(!"ACTY_AUDIT_PASS".equals(productActivity.get("activityAuditStatus")) || nowTimestamp.after(productActivity.getTimestamp("activityEndDate"))){
                    resultData.put("status",false);
                    resultData.put("info","活动已结束");
                    request.setAttribute("resultData", resultData);
                    return "error";
                }else if(nowTimestamp.before(productActivity.getTimestamp("activityStartDate"))){
                    resultData.put("status",false);
                    resultData.put("info","活动未开始");
                    request.setAttribute("resultData", resultData);
                    return "error";
                }
                Long occupiedQuantityTotal = 0L;
                if(UtilValidate.isNotEmpty(productActivity.getLong("occupiedQuantityTotal"))){
                    occupiedQuantityTotal = productActivity.getLong("occupiedQuantityTotal");
                }
                if(productActivity.getLong("activityQuantity") - productActivity.getLong("hasBuyQuantity") - occupiedQuantityTotal <= 0L){//活动剩余数量
                    resultData.put("status",false);
                    resultData.put("info","可购数量不足");
                    request.setAttribute("resultData", resultData);
                    return "error";
                }else{
                    //查询该活动已下单数量
                    Long buyNum = Long.valueOf(quantity);
                    if("esse".equals(productType)){
                        GenericValue productActivityGoods = null;
                        try {
                            productActivityGoods = delegator.findByPrimaryKey("ProductActivityGoods",UtilMisc.toMap("activityId",activityId,"productId",productId));
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        //是否自提
                        if (!"SHIPMENT_OWN".equals(productActivityGoods.get("shipmentType"))) {
                            if(UtilValidate.isEmpty(logistics)){
                                resultData.put("status",false);
                                resultData.put("info","货运方式不能为空");
                                request.setAttribute("resultData", resultData);
                                return "error";
                            }
                        }

                        boolean beganTransaction = false;
                        try {
                            beganTransaction = TransactionUtil.begin();
                            DynamicViewEntity dve = new DynamicViewEntity();
                            dve.addMemberEntity("OHR", "OrderHeader");
                            dve.addMemberEntity("ORE", "OrderRole");
                            dve.addMemberEntity("OIM", "OrderItem");
                            dve.addViewLink("OHR", "ORE", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
                            dve.addViewLink("OHR", "OIM", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
                            dve.addAlias("OHR", "orderId", "orderId", null, true, true, null);
                            dve.addAlias("OHR", "statusId");
                            dve.addAlias("ORE", "partyId");
                            dve.addAlias("ORE", "roleTypeId");
                            dve.addAlias("OIM", "activityId");
                            dve.addAlias("OIM", "quantity");
                            List<EntityCondition>  entityConditionList =  FastList.newInstance();
                            entityConditionList.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_IN,UtilMisc.toList("ORDER_WAITPAY","ORDER_CANCELLED","ORDER_RETURNED")));
                            entityConditionList.add(EntityCondition.makeCondition("partyId", partyId));
                            entityConditionList.add(EntityCondition.makeCondition("roleTypeId", "PLACING_CUSTOMER"));
                            entityConditionList.add(EntityCondition.makeCondition("activityId", activityId));
                            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
                            EntityListIterator eli = null;
                            // do the lookup
                            try {
                                eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(entityConditionList, EntityOperator.AND), null, null, null, findOpts);
                                List<GenericValue> resultList = eli.getCompleteList();
                                if (UtilValidate.isNotEmpty(resultList)){
                                    for(GenericValue result : resultList){
                                        buyNum += result.getBigDecimal("quantity").longValue();
                                    }
                                }
                            } catch (GenericEntityException e) {
                                e.printStackTrace();
                            } finally {
                                if (eli != null) {
                                    try {
                                        eli.close();
                                    } catch (GenericEntityException e) {
                                        Debug.logWarning(e, e.getMessage(), module);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            String errMsg = "Error handling query physical order number";
                            Debug.logError(e, errMsg, module);
                            try {
                                TransactionUtil.rollback(beganTransaction, errMsg, e);
                            } catch (GenericTransactionException gte2) {
                                Debug.logError(gte2, "Unable to rollback transaction", module);
                            }
                        } finally {
                            try {
                                TransactionUtil.commit(beganTransaction);
                            } catch (GenericTransactionException gte) {
                                Debug.logError(gte, "Unable to commit transaction", module);
                            }
                        }
                    }else if("fictitious".equals(productType)){
                        List<GenericValue> tickets = new ArrayList<GenericValue>();
                        List<EntityCondition> ticketConditiona = FastList.newInstance();
                        ticketConditiona.add(EntityCondition.makeCondition("partyId",partyId));
                        ticketConditiona.add(EntityCondition.makeCondition("activityId",activityId));
                        ticketConditiona.add(EntityCondition.makeCondition("ticketStatus",EntityOperator.IN,UtilMisc.toList("notUsed","hasUsed","notAudited","notRefunded","rejectApplication","expired")));
                        try {
                            tickets = delegator.findList("Ticket", EntityCondition.makeCondition(ticketConditiona,EntityOperator.AND),null,null,null,false);
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        if(UtilValidate.isNotEmpty(tickets)){
                            buyNum += Long.valueOf(tickets.size());
                        }
                        if(UtilValidate.isEmpty(telphone)){
                            resultData.put("status",false);
                            resultData.put("info","手机号不能为空");
                            request.setAttribute("resultData", resultData);
                            return "error";
                        }else{
                            cart.setTelPhone(telphone);
                        }
                    }
                    if(productActivity.getLong("limitQuantity").compareTo(Long.valueOf(buyNum)) < 0){//每人限制购买数量，下单数量加上已购买数量比较
                        resultData.put("status",false);
                        resultData.put("info","已超出限购数量");
                        request.setAttribute("resultData", resultData);
                        return "error";
                    }
                }
            }
        }
        if(UtilValidate.isEmpty(userLoginId)){
            resultData.put("status",false);
            resultData.put("info","登陆id不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        //验证产品库存
        List<GenericValue> productFacilitys = new ArrayList<GenericValue>();
        try {
            productFacilitys = delegator.findByAnd("ProductFacility", UtilMisc.toMap("productId",productId));//产品库存场所
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if(UtilValidate.isNotEmpty(productFacilitys)){
            List<GenericValue> inventoryItems = new ArrayList<GenericValue>();
            try {
                inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId",productId,"facilityId",productFacilitys.get(0).get("facilityId")));//库存明细
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isNotEmpty(inventoryItems)){
//                BigDecimal occupiedQuantityTotal = BigDecimal.ZERO;
//                if(UtilValidate.isNotEmpty(inventoryItems.get(0).getBigDecimal("occupiedQuantityTotal"))){
//                    occupiedQuantityTotal = inventoryItems.get(0).getBigDecimal("occupiedQuantityTotal");
//                }
                if(inventoryItems.get(0).getBigDecimal("accountingQuantityTotal").compareTo(BigDecimal.ZERO) <= 0){
                    resultData.put("status",false);
                    resultData.put("info","可购数量不足");
                    request.setAttribute("resultData", resultData);
                    return "error";
                }
            }else{
                resultData.put("status",false);
                resultData.put("info","可购数量不足");
                request.setAttribute("resultData", resultData);
                return "error";
            }
        }else{
            resultData.put("status",false);
            resultData.put("info","可购数量不足");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isEmpty(price)){
            resultData.put("status",false);
            resultData.put("info","购买单价不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isEmpty(total)){
            resultData.put("status",false);
            resultData.put("info","实际支付金额不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isEmpty(productStoreId)){
            resultData.put("status",false);
            resultData.put("info","店铺id不能为空");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        if(UtilValidate.isNotEmpty(score)){
            GenericValue partyScore = null;
            try {
                partyScore = delegator.findByPrimaryKeyCache("PartyScore", UtilMisc.toMap("partyId",partyId));//会员积分表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isEmpty(partyScore)){
                resultData.put("status",false);
                resultData.put("info","积分不足");
                request.setAttribute("resultData", resultData);
                return "error";
            }else if(partyScore.getLong("scoreValue").compareTo(Long.valueOf(score)) < 0){
                resultData.put("status",false);
                resultData.put("info","积分不足");
                request.setAttribute("resultData", resultData);
                return "error";
            }
            if(UtilValidate.isNotEmpty(productActivity.getLong("scoreValue")) && productActivity.getLong("scoreValue").compareTo(Long.valueOf(score)) < 0){//限制使用积分数量
                resultData.put("status",false);
                resultData.put("info","最多使用"+productActivity.getLong("scoreValue")+"积分");
                request.setAttribute("resultData", resultData);
                return "error";
            }
        }else{
            score = "0";
        }
        if(UtilValidate.isNotEmpty(balance) && new BigDecimal(balance).compareTo(BigDecimal.ZERO) != 0){
            GenericValue partyAccount = null;
            try {
                partyAccount = delegator.findByPrimaryKeyCache("PartyAccount", UtilMisc.toMap("partyId",partyId));//会员余额表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isEmpty(partyAccount)){
                resultData.put("status",false);
                resultData.put("info","余额不足");
                request.setAttribute("resultData", resultData);
                return "error";
            }else if(partyAccount.getBigDecimal("amount").compareTo(new BigDecimal(balance)) < 0){
                resultData.put("status",false);
                resultData.put("info","余额不足");
                request.setAttribute("resultData", resultData);
                return "error";
            }
        }else{
            balance = "0";
        }
        GenericValue integralPerMoney = null;
        try {
            integralPerMoney = delegator.findByPrimaryKey("PartyIntegralSet",UtilMisc.toMap("partyIntegralSetId","PARTY_INTEGRAL_SET"));//积分抵现规则表
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        Long soldNum = productActivity.getLong("hasBuyQuantity") + Long.valueOf(quantity);
        BigDecimal salePrice = null;
        if("SEC_KILL".equals(productActivity.get("activityType"))){
            salePrice = productActivity.getBigDecimal("productPrice");
        }else if("GROUP_ORDER".equals(productActivity.get("activityType"))){
            List<GenericValue> productGroupOrderRules = new ArrayList<GenericValue>();
            try {
                productGroupOrderRules = delegator.findByAnd("ProductGroupOrderRule",UtilMisc.toMap("activityId",activityId),UtilMisc.toList("orderQuantity"));//阶梯价规则表
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            salePrice = productGroupOrderRules.get(0).getBigDecimal("orderPrice");
            for(int i=0;i<productGroupOrderRules.size();i++){
                if(productGroupOrderRules.get(i).getLong("orderQuantity").compareTo(Long.valueOf(soldNum)) <= 0){
                    salePrice = productGroupOrderRules.get(i).getBigDecimal("orderPrice");
                }else{
                    break;
                }
            }
        }
        if(salePrice.subtract(new BigDecimal(price)).compareTo(BigDecimal.ZERO) != 0){
            resultData.put("status",false);
            resultData.put("info","商品单价有误");
            request.setAttribute("resultData", resultData);
            return "error";
        }
        BigDecimal saleTotal = salePrice.multiply(new BigDecimal(quantity)).subtract(new BigDecimal(balance)).subtract(new BigDecimal(score).divide(new BigDecimal(integralPerMoney.getLong("integralValue"))).setScale(2, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_HALF_UP);
        if(saleTotal.subtract(new BigDecimal(total)).compareTo(BigDecimal.ZERO) != 0){
            resultData.put("status",false);
            resultData.put("info","订单金额有误");
            request.setAttribute("resultData", resultData);
            return "error";
        }



        if ((cart != null) && UtilValidate.isNotEmpty(cart.items())) {
            for(int i = 0 ; i < cart.items().size() ; i++) {
                cart.items().get(i).setActivityId(activityId);//将活动id加入item中
                cart.items().get(i).setBasePrice(new BigDecimal(price));//将活动商品价格加入item中
            }
            
            cart.setIntegralDiscount(new BigDecimal(score).divide(new BigDecimal(integralPerMoney.getLong("integralValue"))).setScale(2, BigDecimal.ROUND_HALF_UP));//积分抵扣金额
            

            if(UtilValidate.isNotEmpty(remarks)){
                cart.setRemarks(remarks);
            }
            return "success";
        } else {
            String errMsg = UtilProperties.getMessage(resource_error, "checkevents.cart_empty", (cart != null ? cart.getLocale() : UtilHttp.getLocale(request)));
            Debug.log("errMsg");
            resultData.put("status",false);
            resultData.put("info","商品不在销售时间内");
            request.setAttribute("resultData", resultData);
            return "error";
        }
    }

    public static String checkOrderProductNum(HttpServletRequest request, HttpServletResponse response){
        Map resultData = new HashMap();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String activityId = request.getParameter("activityId");     //活动id
        String productId = request.getParameter("productId");       //商品id
        String num = request.getParameter("num");                   //产品数量
        resultData.put("status",true);
        //验证产品库存
        List<GenericValue> productFacilitys = new ArrayList<GenericValue>();
        try {
            productFacilitys = delegator.findByAnd("ProductFacility", UtilMisc.toMap("productId",productId));//产品库存场所
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if(UtilValidate.isNotEmpty(productFacilitys)){
            List<GenericValue> inventoryItems = new ArrayList<GenericValue>();
            try {
                inventoryItems = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId",productId,"facilityId",productFacilitys.get(0).get("facilityId")));//库存明细
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if(UtilValidate.isNotEmpty(inventoryItems)){
                if(inventoryItems.get(0).getBigDecimal("accountingQuantityTotal").compareTo(new BigDecimal(num)) < 0){
                    resultData.put("status",false);
                    resultData.put("info","可购数量不足");
                }
            }else{
                resultData.put("status",false);
                resultData.put("info","可购数量不足");
            }
        }else{
            resultData.put("status",false);
            resultData.put("info","可购数量不足");
        }
        request.setAttribute("resultData", resultData);
        return "success";
    }

    /**
     * 销售订单参数的取得处理
     *
     * @param request
     * @param response
     * @return
     */
    public static String setSalesOrderCurrencyAgreementShipDates(HttpServletRequest request,
                                                                 HttpServletResponse response, String currenSalesOrderName,
                                                                 String currenCatalogId, String currenShipAfterDate, String currenShipBeforeDate, String currencyUomIdCny,
                                                                 String currenPartyIdDistribute, String currenReserveOrderId, String currenInvoicePerShipment,
                                                                 String currenInvoiceTitle) {

        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        ShoppingCartHelper cartHelper = new ShoppingCartHelper(delegator, dispatcher, cart);
        // 货币
        String currencyUomId = currencyUomIdCny;
        // 缺省最迟送货日期
        String shipBeforeDateStr = "";
        if (UtilValidate.isNotEmpty(currenShipBeforeDate)) {
            shipBeforeDateStr = string2Date(currenShipBeforeDate);
        }
        // 缺省最早送货日期
        String shipAfterDateStr = "";
        if (UtilValidate.isNotEmpty(currenShipAfterDate)) {
            shipAfterDateStr = string2Date(currenShipAfterDate);
        }
        // 销售订单名称
        String orderName = currenSalesOrderName;
        Locale locale = UtilHttp.getLocale(request);
        Map<String, Object> result = null;

        if (UtilValidate.isNotEmpty(currencyUomId)) {
            result = cartHelper.setCurrency(currencyUomId);
        }
        // set the order name
        cart.setOrderName(orderName);
        return "success";
    }

    /**
     * 支付方法的取得处理
     *
     * @param request
     * @param currenPaymentMethodTypeAndId
     * @return
     */
    public static Map<String, Map<String, Object>> getSalesSelectedPaymentMethods(HttpServletRequest request,
                                                                                  String currenPaymentMethodTypeAndId) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        // Locale locale = UtilHttp.getLocale(request);
        Map<String, Map<String, Object>> selectedPaymentMethods = new HashMap<String, Map<String, Object>>();
        String[] paymentMethods = new String[] { currenPaymentMethodTypeAndId };

        if (UtilValidate.isNotEmpty(request.getParameter("issuerId"))) {
            request.setAttribute("issuerId", request.getParameter("issuerId"));
        }

        String errMsg = null;

        if (paymentMethods != null) {
            for (String paymentMethod : paymentMethods) {
                Map<String, Object> paymentMethodInfo = FastMap.newInstance();

                String securityCode = request.getParameter("securityCode_" + paymentMethod);
                if (UtilValidate.isNotEmpty(securityCode)) {
                    paymentMethodInfo.put("securityCode", securityCode);
                }
                String amountStr = request.getParameter("amount_" + paymentMethod);
                BigDecimal amount = null;
                if (UtilValidate.isNotEmpty(amountStr) && !"REMAINING".equals(amountStr)) {
                    try {
                        amount = new BigDecimal(amountStr);
                    } catch (NumberFormatException e) {
                        Debug.logError(e, module);
                        errMsg = UtilProperties.getMessage(resource_error, "checkevents.invalid_amount_set_for_payment_method",
                                (cart != null ? cart.getLocale() : Locale.getDefault()));
                        request.setAttribute("_ERROR_MESSAGE_", errMsg);
                        return null;
                    }
                }
                paymentMethodInfo.put("amount", amount);
                selectedPaymentMethods.put(paymentMethod, paymentMethodInfo);
            }
        }
        Debug.logInfo("Selected Payment Methods : " + selectedPaymentMethods, module);
        return selectedPaymentMethods;
    }

    /**
     * 字符转日期
     *
     * @param time
     * @return
     */
    public static String string2Date(String time) {
        Date d = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time = formatter.format(d);
        return time;
    }
}
