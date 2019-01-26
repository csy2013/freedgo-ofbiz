package com.yuaoq.yabiz.product.service;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilTimer;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by changsy on 2018/8/26.
 */
public class ProductPriceServices {
    
    public static Map<String, Object> updateProductPrice(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> ret = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String productId = (String) context.get("productId");
        String priceType = (String)context.get("productPriceTypeId");
        GenericValue userLogin = (GenericValue)context.get("userLogin");
        try {
            List<GenericValue> productPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice",UtilMisc.toMap("productId",productId,"productPriceTypeId",priceType)));
            if(UtilValidate.isNotEmpty(productPrices)){
                for (int i = 0; i < productPrices.size(); i++) {
                    GenericValue oldProductPrice = productPrices.get(i);
                    oldProductPrice.set("thruDate",UtilDateTime.nowTimestamp());
                    oldProductPrice.set("lastModifiedDate",UtilDateTime.nowTimestamp());
                    oldProductPrice.set("lastModifiedByUserLogin",userLogin.getString("userLoginId"));
                    oldProductPrice.store();
                }
                
                GenericValue productPrice = delegator.makeValue("ProductPrice");
                productPrice.setNonPKFields(context);
                productPrice.setPKFields(context);
                productPrice.set("fromDate",UtilDateTime.nowTimestamp());
                productPrice.set("lastModifiedDate",UtilDateTime.nowTimestamp());
                productPrice.set("lastModifiedByUserLogin",userLogin.getString("userLoginId"));
                productPrice.set("createdByUserLogin",userLogin.getString("userLoginId"));
                productPrice.create();
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return ret;
        
    }
}
