package com.yuaoq.yabiz.app.mobile.microservice.store.api.v1;

import com.yuaoq.yabiz.app.mobile.microservice.wish.api.v1.WishV1Controller;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
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
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.LocalDispatcher;
import org.slf4j.helpers.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("all")
@RestController
@RequestMapping(path = "/api/store/v1")
public class StoreV1Controller {


    public static final String module = StoreV1Controller.class.getName();

    @Value("${image.base.url}")
    String baseImgUrl;
    @RequestMapping(value = "/getStoreCategory", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getStoreCategory(HttpServletRequest request, String storeId, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        Map<String, Object> resultData = FastMap.newInstance();
        if (UtilValidate.isEmpty(storeId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "storeId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }

        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        List<GenericValue> storeList = delegator.findByAnd("PartyGroup", UtilMisc.toMap("productStoreId", storeId));
        if (storeList == null || storeList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "查询不到该店铺信息");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        GenericValue store = storeList.get(0);
//        String storePartyId = store.getString("partyId");

        //查询店铺所有签约商品的一级分类

        /*DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PPC", "PartyProductCategory");
        dynamicView.addAlias("PPC", "partyId");
        dynamicView.addAlias("PPC", "productCategoryId");*/
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PC", "ProductCategory");
        dynamicView.addAlias("PC", "productCategoryId");
        dynamicView.addAlias("PC", "productCategoryLevel");
        dynamicView.addAlias("PC", "categoryName");
        dynamicView.addAlias("PC", "productStoreId");
        dynamicView.addAlias("PC", "isDel");

//        dynamicView.addViewLink("PPC", "PC", false, ModelKeyMap.makeKeyMapList("productCategoryId", "productCategoryId"));


        EntityCondition mainCond = EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, 1L),
                        EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, storeId),
                        EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null)
                )
                , EntityOperator.AND);

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("categoryName");
        fieldsToSelect.add("productCategoryId");
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        List<GenericValue> categorys = null;
        try {
            beganTransaction = TransactionUtil.begin();
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, null);
            categorys = pli.getCompleteList();
            resultData.put("retCode", 1);
            resultData.put("categorys", categorys);
        } catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (pli != null) {
                try {
                    pli.close();
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


        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    @RequestMapping(value = "/getStoreBanner", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getStoreBanner(HttpServletRequest request, String storeId, HttpServletResponse response, JwtAuthenticationToken token) throws GeneralException, SQLException, IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        Map<String, Object> resultData = FastMap.newInstance();
        if (UtilValidate.isEmpty(storeId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "storeId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        List<GenericValue> storeList = delegator.findByAnd("PartyGroup", UtilMisc.toMap("productStoreId", storeId));
        if (storeList == null || storeList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "查询不到该店铺信息");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        GenericValue store = storeList.get(0);
        String storePartyId = store.getString("partyId");

        //获取店铺名称和店铺logo
        GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId",storeId));
        String storeName = productStore.getString("storeName");

        GenericValue partyBusiness = delegator.findByPrimaryKey("PartyBusiness", UtilMisc.toMap("partyId",storePartyId));
        String logoImg = partyBusiness.get("logoImg")==null?null:partyBusiness.getString("logoImg");
        String logoImgUrl = CommonUtils.getImgUrl(delegator,dispatcher,logoImg,baseImgUrl);

        //获取轮播图
        DynamicViewEntity bannerViewEntity = new DynamicViewEntity();
        bannerViewEntity.addMemberEntity("B", "Banner");
        bannerViewEntity.addAlias("B", "bannerId");
        bannerViewEntity.addAlias("B", "isUse");
        bannerViewEntity.addAlias("B", "isAllWebSite");
        bannerViewEntity.addAlias("B", "sequenceId");
        bannerViewEntity.addAlias("B", "contentId");
        bannerViewEntity.addAlias("B", "firstLinkType");
        bannerViewEntity.addAlias("B", "linkName");
        bannerViewEntity.addAlias("B", "linkId");
        bannerViewEntity.addAlias("B", "linkUrl");
        bannerViewEntity.addAlias("B", "applyScope");
        bannerViewEntity.addAlias("B", "productStoreId");
        bannerViewEntity.addMemberEntity("BWS", "BannerWebSite");
        bannerViewEntity.addAlias("BWS", "webSiteId");
        bannerViewEntity.addViewLink("B", "BWS", true, ModelKeyMap.makeKeyMapList("bannerId", "bannerId"));

        List<EntityCondition> bannerConditions2 = FastList.newInstance();
        bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
        bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));

        List<EntityCondition> bannerConditions1 = FastList.newInstance();
        bannerConditions1.add(EntityCondition.makeCondition("isUse", "0"));
        bannerConditions1.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
        bannerConditions1.add(EntityCondition.makeCondition("productStoreId", storeId));

        List selectList = UtilMisc.toList("bannerId","isUse","isAllWebSite","sequenceId","contentId","firstLinkType","linkId","linkUrl","applyScope");
        selectList.add("webSiteId");
        selectList.add("linkName");
        boolean beganTransaction=false;
        EntityListIterator eli=null;
        try {
            beganTransaction = TransactionUtil.begin();
            eli = delegator.findListIteratorByCondition(bannerViewEntity, EntityCondition.makeCondition(bannerConditions1, EntityOperator.AND), null, null, null, null);
            List<GenericValue> banners = eli.getCompleteList();

            List<Map> bannerList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(banners)) {
                for (GenericValue banner : banners) {
                    Map bannerMap = FastMap.newInstance();
                    String contentId = banner.getString("contentId");
                    String linkName = banner.getString("linkName");
                    String imgUrl = CommonUtils.getImgUrl(delegator,dispatcher,contentId,baseImgUrl);
                    //图片
                    bannerMap.put("imgUrl",imgUrl);
                    bannerMap.put("linkName",linkName);
                    bannerMap.put("linkType", banner.get("firstLinkType"));
                    bannerMap.put("linkUrl", banner.get("linkUrl"));
                    bannerList.add(bannerMap);
                }
            }

            resultData.put("retCode",1);
            resultData.put("logoImgUrl",logoImgUrl);
            resultData.put("storeName",storeName);
            resultData.put("bannerList",bannerList);
        }catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
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

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

}
