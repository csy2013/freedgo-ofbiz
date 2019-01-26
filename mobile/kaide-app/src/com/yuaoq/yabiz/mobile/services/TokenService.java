package com.yuaoq.yabiz.mobile.services;

import com.google.gson.Gson;
import com.yuaoq.yabiz.mobile.dt.CustInfo;
import javolution.util.FastMap;
import org.ofbiz.base.util.HttpClient;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 2018/3/7.
 */
public class TokenService {
    
    
    /**
     * token获取用户数据
     *
     * @return
     * @throws
     */
    public static Map<String, Object> partyTokeLogin(DispatchContext dispatchContext, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String token = (String) context.get("token");
        String memberId = (String) context.get("memberId");
        
        Delegator delegator = dispatchContext.getDelegator();
        LocalDispatcher localDispatcher = dispatchContext.getDispatcher();
        Locale locale = (Locale) context.get("locale");
        //根据token和memberId获取对应的用户数据
        
        //做用户模拟登录，生成用户jwts
        String kdHost = UtilProperties.getMessage("application", "app.interface.kaidexing.demo", locale);
        String funcName = UtilProperties.getMessage("application","app.interface.kaidexing.fun.token",locale);
        try {
            HttpClient httpClient = new HttpClient(kdHost+funcName);
            String response = httpClient.post("token="+token+"&memberId="+memberId);
            Gson gson = new Gson();
            CustInfo custInfo =  gson.fromJson(response, CustInfo.class);
            if(UtilValidate.isNotEmpty(custInfo)) {
                //判断用户数据是否已经存在数据库，如果没有新增
                List<GenericValue> parties = delegator.findByAnd("PartyAndUserLogin", "crmPartyId", custInfo.getCrmPartyId());
                if(UtilValidate.isNotEmpty(parties)){
                    GenericValue partyAndUserLogin = parties.get(0);
                    //做用户模拟登录，生成用户jwts
                    result.put("userLogin",partyAndUserLogin);
                }else{
                    //待完成，生成客户数据
                }
                
            }
        } catch (Exception e) {
        
        }
        
        return result;
    }
}
