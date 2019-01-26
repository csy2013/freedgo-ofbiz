package com.yuaoq.yabiz.mobile.order.order;

import com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCart;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by changsy on 16/6/30.
 */
public class OrderEvents {

    public static String module = OrderEvents.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";

    private static final String NO_ERROR = "noerror";
    private static final String NON_CRITICAL_ERROR = "noncritical";
    private static final String ERROR = "error";


    // Event wrapper for the tax calc.
    public static String getOrderHistory(HttpServletRequest request, HttpServletResponse response) {
        String userName = ShoppingCart.getUserNameFromRequest(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            Map<String, Object> resultData = dispatcher.runSync("queryOrderHistory", UtilMisc.toMap("loginId", userLogin.get("userLoginId"), "userLogin", userLogin));
            request.setAttribute("resultData", resultData);

        } catch (GenericEntityException e) {
            e.printStackTrace();
            return "error";
        } catch (GenericServiceException e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }
}
