package com.yuaoq.yabiz.mobile.services;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.RequestUtil;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by changsy on 2018/3/8.
 */
public class IndexService {
    
    /**
     * 首页查询热搜
     *
     * @param request
     * @param response
     * @return
     */
    public static String getHotSearch(HttpServletRequest request, HttpServletResponse response) {
        
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("Client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("error", "客户端类型不能为空");
            response.setStatus(403);
            request.setAttribute("resultData",resultData);
            return "error";
        }
      
        try {
            
            
            boolean beganTransaction;
            String productStoreId = (String)request.getParameter("productStoreId");
            if(UtilValidate.isEmpty(productStoreId)){
                //如果为空创建
                List<GenericValue> groups = delegator.findByAnd("PartyGroup",UtilMisc.toMap("isInner","Y"));
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
            hotSearchViewEntity.setGroupBy(UtilMisc.toList("hotSearchId","hotSearchKeyName","sequenceId","isAllWebSite","productStoreId","webSiteId"));
            beganTransaction = TransactionUtil.begin();
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
            EntityListIterator eli = delegator.findListIteratorByCondition(hotSearchViewEntity, EntityCondition.makeCondition(hotSearchConditions, EntityOperator.OR), null, UtilMisc.toSet("hotSearchKeyName","sequenceId"), UtilMisc.toList("sequenceId"), findOpts);
            List<GenericValue> hotSearchs = eli.getCompleteList();
            eli.close();
            TransactionUtil.commit(beganTransaction);
            
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
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        request.setAttribute("resultData",resultData);
        return "success";
        
    }
}
