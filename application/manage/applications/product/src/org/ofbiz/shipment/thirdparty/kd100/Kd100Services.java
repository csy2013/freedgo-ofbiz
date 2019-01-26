package org.ofbiz.shipment.thirdparty.kd100;

import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 2018/3/24.
 */
public class Kd100Services {
    
    
    public static Map<String, Object> kd100RateEstimate(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Locale locale = (Locale) context.get("locale");
    
        // some of these can be refactored
        String carrierPartyId = (String) context.get("carrierPartyId");
        String shipmentMethodTypeId = (String) context.get("shipmentMethodTypeId");
        String shippingContactMechId = (String) context.get("shippingContactMechId");
        BigDecimal shippableWeight = (BigDecimal) context.get("shippableWeight");
    
        if ("NO_SHIPPING".equals(shipmentMethodTypeId)) {
        
            result.put("shippingEstimateAmount", null);
            return result;
        }
    
        return result;
    }
}
