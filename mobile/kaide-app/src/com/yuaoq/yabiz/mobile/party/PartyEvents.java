package com.yuaoq.yabiz.mobile.party;

import com.yuaoq.yabiz.mobile.order.shoppingcart.ShoppingCart;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 * Created by changsy on 2018/3/15.
 */
public class PartyEvents {
    
    /**
     * 查询收货地址
     *
     * @param request
     * @param response
     * @return
     */
    public static String queryPersonAddressList(HttpServletRequest request, HttpServletResponse response) {
        
        Map<String, Object> resultData = FastMap.newInstance();
        String userName = ShoppingCart.getUserNameFromRequest(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            Map<String, Object> result = dispatcher.runSync("customAddressQuery", UtilMisc.toMap("loginId", userLogin.get("userLoginId"), "userLogin", userLogin));
            Map<String, Object> result1 = dispatcher.runSync("queryCustomDefaultPostAddress", UtilMisc.toMap("loginId", userLogin.get("userLoginId"), "productStoreId", null, "userLogin", userLogin));
            Map partyAndContactMech = (Map) result1.get("partyAndContactMech");
            
            if (UtilValidate.isNotEmpty(partyAndContactMech)) {
                resultData.put("defaultAddressId", partyAndContactMech.get("contactMechId"));
            }
            if (UtilValidate.isNotEmpty(result)) {
                resultData.put("adderess", result.get("contactMech"));
            }
            resultData.put("message", result.get("responseMessage"));
            resultData.put("retCode", 1);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 0);
            request.setAttribute("resultData", resultData);
            return "error";
        } catch (GenericServiceException e) {
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 1);
            e.printStackTrace();
            request.setAttribute("resultData", resultData);
            return "error";
        }
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    
    public static String getPersonAddressDetail(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultData = FastMap.newInstance();
        String userName = ShoppingCart.getUserNameFromRequest(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> paramMap = UtilHttp.getCombinedMap(request);
        String contactMechId = (String) paramMap.get("contactMechId");
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            Map<String, Object> result = dispatcher.runSync("customAddressDetail", UtilMisc.toMap("contactMechId", contactMechId, "loginId", userLogin.get("userLoginId"), "userLogin", userLogin, "productStoreId", null));
            GenericValue postalAddress = (GenericValue) result.get("postalAddress");
            resultData.put("postalAddressData", postalAddress);
            if (UtilValidate.isNotEmpty(postalAddress)) {
                String provinedId = postalAddress.getString("stateProvinceGeoId");
                if (UtilValidate.isNotEmpty(provinedId)) {
                    GenericValue pGeo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", provinedId));
                    if(UtilValidate.isNotEmpty(pGeo)) {
                        resultData.put("provinceName", pGeo.getString("geoName"));
                    }
                }
                String cityId = postalAddress.getString("city");
                if(UtilValidate.isNotEmpty(cityId)) {
                    GenericValue cGeo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", cityId));
                    if(UtilValidate.isNotEmpty(cGeo)) {
                        resultData.put("cityName", cGeo.getString("geoName"));
                    }
                }
               
                String countyGeoId = postalAddress.getString("countyGeoId");
                if(UtilValidate.isNotEmpty(countyGeoId)) {
                    GenericValue coGeo = delegator.findByPrimaryKey("Geo", UtilMisc.toMap("geoId", countyGeoId));
                    if(UtilValidate.isNotEmpty(coGeo)) {
                        resultData.put("countyName", coGeo.getString("geoName"));
                    }
                }
            }
            
            resultData.put("message", result.get("responseMessage"));
            resultData.put("retCode", 1);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 0);
            request.setAttribute("resultData", resultData);
            return "error";
        } catch (GenericServiceException e) {
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 1);
            e.printStackTrace();
            request.setAttribute("resultData", resultData);
            return "error";
        }
        
        
        request.setAttribute("resultData", resultData);
        return "success";
    }
    
    /**
     * 修改收货地址
     * add by changsy
     *
     * @param request
     * @param response
     * @return
     */
    public static String updatePersonAddress(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> paramMap = UtilHttp.getCombinedMap(request);
        String userName = ShoppingCart.getUserNameFromRequest(request);
        Map<String, Object> resultData = FastMap.newInstance();
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            String contactMechId = (String) paramMap.get("contactMechId");
            
            String address1 = (String) paramMap.get("address");
            String city = (String) paramMap.get("city") == null ? "_NA_" : (String) paramMap.get("city");
            String countyGeoId = (String) paramMap.get("countyGeoId") == null ? "_NA_" : (String) paramMap.get("countyGeoId");
            String mobilePhone = (String) paramMap.get("tel");
            String postalCode = (String) paramMap.get("postalCode") == null ? "_NA_" : (String) paramMap.get("postalCode");
            String stateProvinceGeoId = (String) paramMap.get("stateProvinceGeoId");
            String attnName = (String) paramMap.get("name");
            String is_def = (String) paramMap.get("is_def");
            Map<String, Object> serviceIn = UtilMisc.toMap("contactMechId",
                    contactMechId, "address1", address1, "city", city, "partyId", userLogin.get("partyId"), "countyGeoId", countyGeoId, "mobilePhone", mobilePhone, "attnName", attnName, "postalCode", postalCode, "stateProvinceGeoId", stateProvinceGeoId, "userLogin", userLogin,"toName",attnName,"isDefault",is_def,"countryGeoId","CHN");
            
            Map<String, Object> result = dispatcher.runSync("updatePartyPostalAddress", serviceIn);
            //设置为默认货物目的地地址
            //先检查是否已经是货物目的地址
            contactMechId = (String) result.get("contactMechId");
            List<GenericValue> partyContactMechs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", (String) userLogin.get("partyId"), "contactMechId", contactMechId)), true);
            GenericValue partyContactMech = EntityUtil.getFirst(partyContactMechs);
            boolean hasShippingLocation = false;
            if (partyContactMech != null) {
                List<GenericValue> partyContactMechPurposes = EntityUtil.filterByDate(partyContactMech.getRelated("PartyContactMechPurpose"), true);
                if (UtilValidate.isNotEmpty(partyContactMechPurposes)) {
                    for (GenericValue partyContactMechPurpose : partyContactMechPurposes) {
                        if (partyContactMechPurpose.get("contactMechPurposeTypeId").equals("SHIPPING_LOCATION")) {
                            hasShippingLocation = true;
                            break;
                        }
                    }
                }
            }
            if (!hasShippingLocation) {
                String contactMechPurposeTypeId = "SHIPPING_LOCATION";
                serviceIn = UtilMisc.toMap("contactMechId", result.get("contactMechId"), "partyId", userLogin.get("partyId"), "contactMechPurposeTypeId", contactMechPurposeTypeId, "userLogin", userLogin);
                result = dispatcher.runSync("createPartyContactMechPurpose", serviceIn);
            }
            //设置为默认货物目的地地址
            if (is_def.equals("Y")) {
                serviceIn = UtilMisc.toMap("productStoreId", null, "defaultShipAddr", contactMechId, "partyId", userLogin.get("partyId"), "userLogin", userLogin);
                result = dispatcher.runSync("setPartyProfileDefaults", serviceIn);
                
            }
            resultData.put("message", result.get("responseMessage"));
            resultData.put("retCode", 1);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 0);
            request.setAttribute("resultData", resultData);
            return "error";
        } catch (GenericServiceException e) {
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 1);
            e.printStackTrace();
            request.setAttribute("resultData", resultData);
            return "error";
        }
        
        request.setAttribute("resultData", resultData);
        return "success";
        
    }
    
    /**
     * 新增收货地址
     * add by changsy
     *
     * @param request
     * @param response
     * @return
     */
    public static String addPersonAddress(HttpServletRequest request, HttpServletResponse response) {
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> paramMap = UtilHttp.getCombinedMap(request);
        String userName = ShoppingCart.getUserNameFromRequest(request);
        Map<String, Object> resultData = FastMap.newInstance();
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            String address1 = (String) paramMap.get("address");
            String city = (String) paramMap.get("city") == null ? "_NA_" : (String) paramMap.get("city");
            String countyGeoId = (String) paramMap.get("countyGeoId") == null ? "_NA_" : (String) paramMap.get("countyGeoId");
            String mobilePhone = (String) paramMap.get("tel");
            String postalCode = (String) paramMap.get("postalCode") == null ? "_NA_" : (String) paramMap.get("postalCode");
            String stateProvinceGeoId = (String) paramMap.get("stateProvinceGeoId");
            String attnName = (String) paramMap.get("name");
            String is_def = (String) paramMap.get("is_def");
            Map<String, Object> serviceIn = UtilMisc.toMap("address1", address1, "city", city, "partyId", userLogin.get("partyId"),"countryGeoId","CHN", "countyGeoId", countyGeoId, "mobilePhone", mobilePhone, "attnName", attnName, "postalCode", postalCode, "stateProvinceGeoId", stateProvinceGeoId, "userLogin", userLogin,"toName",attnName,"isDefault",is_def);
            
            Map<String, Object> result = dispatcher.runSync("createPartyPostalAddress", serviceIn);
            String contactMechId = (String)result.get("contactMechId");
            String contactMechPurposeTypeId = "SHIPPING_LOCATION";
            serviceIn = UtilMisc.toMap("contactMechId", result.get("contactMechId"), "partyId", userLogin.get("partyId"), "contactMechPurposeTypeId", contactMechPurposeTypeId, "userLogin", userLogin);
            result = dispatcher.runSync("createPartyContactMechPurpose", serviceIn);
            
            //设置为默认货物目的地地址
            if (is_def.equals("Y")) {
                serviceIn = UtilMisc.toMap("productStoreId", null, "defaultShipAddr", contactMechId, "partyId", userLogin.get("partyId"), "userLogin", userLogin);
                result = dispatcher.runSync("setPartyProfileDefaults", serviceIn);
                
            }
            resultData.put("message", result.get("responseMessage"));
            resultData.put("retCode", 1);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 0);
            request.setAttribute("resultData", resultData);
            return "error";
        } catch (GenericServiceException e) {
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 1);
            e.printStackTrace();
            request.setAttribute("resultData", resultData);
            return "error";
        }
        
        
        request.setAttribute("resultData", resultData);
        return "success";
        
    }
    
    /**
     * 删除收货地址
     * add by changsy
     *
     * @param request
     * @param response
     * @return
     */
    public static String delPersonAddress(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> paramMap = UtilHttp.getCombinedMap(request);
        String userName = ShoppingCart.getUserNameFromRequest(request);
        Map<String, Object> resultData = FastMap.newInstance();
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            String contactMechId = request.getParameter("contactMechId");
            
            Map<String, Object> serviceIn = UtilMisc.toMap("contactMechId",
                    contactMechId, "partyId", userLogin.get("partyId"), "userLogin", userLogin);
            
            Map<String, Object> result = dispatcher.runSync("deletePartyContactMech", serviceIn);
            
            resultData.put("message", result.get("responseMessage"));
            resultData.put("retCode", 1);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 0);
            request.setAttribute("resultData", resultData);
            return "error";
        } catch (GenericServiceException e) {
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 1);
            e.printStackTrace();
            request.setAttribute("resultData", resultData);
            return "error";
        }
        request.setAttribute("resultData", resultData);
        return "success";
        
    }
    
    /**
     * 查询收货地址
     * 设置会员默认的收货地址，当PartyProfileDefault 没有默认收货地址的时候，取最近的一个收货地址
     *
     * @param request
     * @param response
     * @return
     */
    public static String defaultPersonAddress(HttpServletRequest request, HttpServletResponse response) {
        
        Map<String, Object> resultData = FastMap.newInstance();
        String userName = ShoppingCart.getUserNameFromRequest(request);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userName));
            
            Map<String, Object> result = dispatcher.runSync("queryCustomDefaultPostAddress", UtilMisc.toMap("loginId", userLogin.get("userLoginId"), "productStoreId", null, "userLogin", userLogin));
            resultData.put("message", result.get("responseMessage"));
            resultData.put("retCode", 1);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 0);
            request.setAttribute("resultData", resultData);
            return "error";
        } catch (GenericServiceException e) {
            resultData.put("message", e.getMessage());
            resultData.put("retCode", 1);
            e.printStackTrace();
            request.setAttribute("resultData", resultData);
            return "error";
        }
        request.setAttribute("resultData", resultData);
        return "success";
    }
}



