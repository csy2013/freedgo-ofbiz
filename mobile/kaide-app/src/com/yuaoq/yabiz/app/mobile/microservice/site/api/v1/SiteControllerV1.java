package com.yuaoq.yabiz.app.mobile.microservice.site.api.v1;



import com.yuaoq.yabiz.app.mobile.microservice.index.api.v1.IndexControllerV1;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.app.template.Templates;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
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
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by changsy on 2018/3/17.
 */
@RestController
@RequestMapping(path = "/api/site/v1")
public class SiteControllerV1 {
    
    public static final String module = SiteControllerV1.class.getName();
    
    @RequestMapping(value = "/hotsearch", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getHotSearch(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("Client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("error", "客户端类型不能为空");
            return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.FORBIDDEN));
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            String productStoreId = (String) request.getParameter("productStoreId");
            if (UtilValidate.isEmpty(productStoreId)) {
                //如果为空创建
                List<GenericValue> groups = delegator.findByAnd("PartyGroup", UtilMisc.toMap("isInner", "Y"));
                if (UtilValidate.isNotEmpty(groups)) {
                    for (int i = 0; i < groups.size(); i++) {
                        GenericValue group = groups.get(i);
                        productStoreId = (String) group.get("productStoreId");
                    }
                }
            }
            
            List<String> webSiteIds = FastList.newInstance();
            
            webSiteIds.add(webSiteId);
            //热搜
            DynamicViewEntity hotSearchViewEntity = new DynamicViewEntity();
            hotSearchViewEntity.addMemberEntity("HS", "HotSearch");
            hotSearchViewEntity.addAlias("HS", "hotSearchId", "hotSearchId", null, true, true, null);
            hotSearchViewEntity.addAlias("HS", "hotSearchKeyName");
            hotSearchViewEntity.addAlias("HS", "sequenceId");
            hotSearchViewEntity.addAlias("HS", "isAllWebSite");
            hotSearchViewEntity.addAlias("HS", "productStoreId");
            hotSearchViewEntity.addMemberEntity("HSWS", "HotSearchWebSite");
            hotSearchViewEntity.addAlias("HSWS", "webSiteId");
            hotSearchViewEntity.addViewLink("HS", "HSWS", true, ModelKeyMap.makeKeyMapList("hotSearchId", "hotSearchId"));
            List<EntityCondition> hotSearchConditions = FastList.newInstance();
            hotSearchConditions.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            hotSearchConditions.add(EntityCondition.makeCondition("webSiteId", EntityOperator.IN, webSiteIds));
            hotSearchConditions.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
            hotSearchViewEntity.setGroupBy(UtilMisc.toList("hotSearchId", "hotSearchKeyName", "sequenceId", "isAllWebSite", "productStoreId", "webSiteId"));
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, false);
              eli = delegator.findListIteratorByCondition(hotSearchViewEntity, EntityCondition.makeCondition(hotSearchConditions, EntityOperator.OR), null, null, UtilMisc.toList("sequenceId"), findOpts);
            List<GenericValue> hotSearchs = eli.getCompleteList();
            
            List<String> resultList = FastList.newInstance();
            int size = 0;
            if (UtilValidate.isNotEmpty(hotSearchs)) {
                size = hotSearchs.size();
                for (GenericValue hotSearch : hotSearchs) {
                    resultList.add(hotSearch.getString("hotSearchKeyName"));
                }
            }
            resultData.put("size", size);
            resultData.put("resultList", resultList);
        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (eli != null) {
                try {
                    eli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
    }
    
    @RequestMapping(value = "/userLoginOrRegister", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> userLoginOrRegister(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("Client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("error", "客户端类型不能为空");
            return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.FORBIDDEN));
        }
        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        Map<String,Object> resultObj = null;
        try {
            resultObj = dispatcher.runSync("kaide-userLoginOrRegister",UtilMisc.toMap("mall_id","10","phone","13705188361","sex","1","nick_name","常","head_img_url","https://img14.360buyimg.com/cms/jfs/t17344/246/1625085370/131681/69d5ffc/5ad1ce9fNe66d0506.jpg","unionid","o_Tg3uLzCos7joJUkrb_A5rBlGB0"));
    
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        if(ServiceUtil.isError(resultObj)){
            resultData.put("error",ServiceUtil.getErrorMessage(resultObj));
        }
        resultData.put("result", resultObj);
       
        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
    }


    @RequestMapping(value = "/userAddress", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> userAddress(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("Client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("error", "客户端类型不能为空");
            return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.FORBIDDEN));
        }

        String token = request.getParameter("token");
        String member_id = request.getParameter("member_id");
        String AddressType = request.getParameter("AddressType");
        String Address1 = request.getParameter("Address1");
        String Address2 = request.getParameter("Address2");
        String Address3 = request.getParameter("Address3");
        String Address4 = request.getParameter("Address4");
        String State = request.getParameter("State");
        String StateValue = request.getParameter("StateValue");
        String City = request.getParameter("City");
        String CityCode = request.getParameter("CityCode");
        String District = request.getParameter("District");
        String DistrictCode = request.getParameter("DistrictCode");
        String SubDistrict = request.getParameter("SubDistrict");
        String SubDistrictCode = request.getParameter("SubDistrictCode");
        String PostalCode = request.getParameter("PostalCode");
        String CountryCode = request.getParameter("CountryCode");

        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        Map<String,Object> resultObj = null;
        try {
            resultObj = dispatcher.runSync("kaide-userAddress",
                    UtilMisc.toMap("token",token,"member_id",member_id,"AddressType",AddressType,"Address1",Address1,
                            "Address2",Address2,"Address3",Address3,"Address4",Address4,"State",State,
                            "StateValue",StateValue,"City",City,"CityCode",CityCode,"District",District,
                            "DistrictCode",DistrictCode,"SubDistrict",SubDistrict,"SubDistrictCode",SubDistrictCode,
                            "PostalCode",PostalCode,"CountryCode",CountryCode));

        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        if(ServiceUtil.isError(resultObj)){
            resultData.put("error",ServiceUtil.getErrorMessage(resultObj));
        }
        resultData.put("result", resultObj);

        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
    }

    @RequestMapping(value = "/userInfo", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> userInfo(HttpServletRequest request, HttpServletResponse response) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("Client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("error", "客户端类型不能为空");
            return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.FORBIDDEN));
        }
        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        Map<String,Object> resultObj = null;
        try {
            resultObj = dispatcher.runSync("kaide-consumeIntegral",UtilMisc.toMap("member_id","FD25DF5C10EF66D6","mall_id","57","token","A9DC9F79A600496E8B88CB410CC1BC5A","integral","100","description","下单","merchant_id","57"));
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        if(ServiceUtil.isError(resultObj)){
            resultData.put("error",ServiceUtil.getErrorMessage(resultObj));
        }
        resultData.put("result", resultObj);

        return Optional.ofNullable(resultData).map(result -> new ResponseEntity(result, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NO_CONTENT));
    }


}
