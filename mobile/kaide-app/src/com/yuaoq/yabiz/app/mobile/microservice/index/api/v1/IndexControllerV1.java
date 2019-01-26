package com.yuaoq.yabiz.app.mobile.microservice.index.api.v1;

import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.Paginate;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.bouncycastle.util.Times;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFieldValue;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import org.ofbiz.product.product.ProductContentWrapper;
/**
 * Created by changsy on 2018/3/31.
 */
@RestController
@RequestMapping(value = "/api/index/v1")
public class IndexControllerV1 {
    
    @Value("${image.base.url}")
    String baseImgUrl;
    public static final String module = IndexControllerV1.class.getName();
    public static UtilCache<String, Map> appIndexCache = UtilCache.createUtilCache("app.index.content", true);
    
    /**
     * 首页轮播图
     *
     * @return
     */
    @RequestMapping(value = "/banner", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexBanner(HttpServletRequest request) {
        
        Map<String, Object> resultData = FastMap.newInstance();
    
//        if (appIndexCache.get("indexBanner") != null) {
//             resultData = appIndexCache.get("indexBanner");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        String webSiteId = request.getHeader("client");
        
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            
            beganTransaction = TransactionUtil.begin();
            
            //轮播图区块--广告
            DynamicViewEntity bannerViewEntity = new DynamicViewEntity();
            bannerViewEntity.addMemberEntity("B", "Banner");
            bannerViewEntity.addAlias("B", "bannerId", "bannerId", null, true, true, null);
            bannerViewEntity.addAlias("B", "isUse");
            bannerViewEntity.addAlias("B", "isAllWebSite");
            bannerViewEntity.addAlias("B", "sequenceId");
            bannerViewEntity.addAlias("B", "contentId");
            bannerViewEntity.addAlias("B", "firstLinkType");
            bannerViewEntity.addAlias("B", "linkName");
            bannerViewEntity.addAlias("B", "linkId");
            bannerViewEntity.addAlias("B", "linkUrl");
            bannerViewEntity.addAlias("B", "productStoreId");
            bannerViewEntity.addAlias("B",  "fromDate");
            bannerViewEntity.addAlias("B",  "thruDate");
            bannerViewEntity.addMemberEntity("BWS", "BannerWebSite");
            bannerViewEntity.addAlias("BWS", "webSiteId");
            bannerViewEntity.addViewLink("B", "BWS", true, ModelKeyMap.makeKeyMapList("bannerId", "bannerId"));
            List<EntityCondition> bannerConditions1 = FastList.newInstance();
            //isUse 是否使用 0是 1否
            bannerConditions1.add(EntityCondition.makeCondition("isUse", "0"));

            Timestamp now = new Timestamp(System.currentTimeMillis());
            bannerConditions1.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, now));
            bannerConditions1.add(EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, now));
            //关联店铺 是否是自营 isInner
            GenericValue group = EntityUtil.getFirst(delegator.findByAnd("PartyGroup", UtilMisc.toMap("isInner", "Y")));
            String productStoreId = "10000";
            if (UtilValidate.isNotEmpty(group)) {
                productStoreId = group.getString("productStoreId");
            }
            bannerConditions1.add(EntityCondition.makeCondition("productStoreId", productStoreId));
            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            //isAllWebSite 是否是全站点 0是 1否
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            bannerConditions1.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            List selectList = UtilMisc.toList("bannerId", "isUse", "isAllWebSite", "sequenceId", "contentId", "firstLinkType", "linkId", "linkUrl", "applyScope");
            selectList.add("webSiteId");
            selectList.add("linkName");
            selectList.add("productStoreId");
            bannerViewEntity.setGroupBy(selectList);
            eli = delegator.findListIteratorByCondition(bannerViewEntity, EntityCondition.makeCondition(bannerConditions1, EntityOperator.AND), null, null, UtilMisc.toList("sequenceId"), null);
            List<GenericValue> banners = eli.getCompleteList();
            
            if (UtilValidate.isNotEmpty(banners)) {
                List<Map> bannerList = FastList.newInstance();
                for (GenericValue banner : banners) {
                    Map bannerMap = FastMap.newInstance();
                    String contentId = banner.getString("contentId");
                    String linkName = banner.getString("linkName");
                    String firstLinkType = banner.getString("firstLinkType");
                    String linkUrl = banner.getString("linkUrl");
                    String linkId = banner.getString("linkId");
                    bannerMap.put("linkName", linkName);
                    bannerMap.put("contentId", contentId);
                    bannerMap.put("firstLinkType", firstLinkType);
                    bannerMap.put("linkId", linkId);
                    bannerMap.put("linkUrl", linkUrl);
                    bannerList.add(bannerMap);
                }

                List<Map> newBrannerList = FastList.newInstance();
                for (int b = 0;b<bannerList.size();b++){
                    Map<String,Object> branner = bannerList.get(b);
                    Map newBranner = FastMap.newInstance();
                    newBranner.put("linkName",branner.get("linkName"));
                    String contentId = branner.get("contentId").toString();
                    String imgUrl = "";
                    if (UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                    newBranner.put("imgUrl",imgUrl);
                    String firstLinkType = UtilValidate.isEmpty(branner.get("firstLinkType"))?null:branner.get("firstLinkType").toString(); ;
                    if (UtilValidate.isNotEmpty(firstLinkType)){
                        if (UtilValidate.areEqual("FLT_ZDYLJ", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkUrl", branner.get("linkUrl"));
                        } else if (UtilValidate.areEqual("FLT_SPLJ", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_ZSLP", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_XYD", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_DTYJF", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_ZZK", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_PT", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_CHB", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_WZLJ", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        }else if (UtilValidate.areEqual("FLT_HDLJ", firstLinkType)) {
                            newBranner.put("linkType", firstLinkType);
                            newBranner.put("linkId", branner.get("linkId"));
                        }
                    }else {
                        newBranner.put("linkType", null);
                        newBranner.put("linkId", null);
                    }
                    newBrannerList.add(newBranner);
                }

                resultData.put("bannerList", newBrannerList);
            }
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
//        if (appIndexCache != null) {
//            appIndexCache.put("indexBanner", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 菜单导航
     *
     * @return
     */
    @RequestMapping(value = "/navigationMenu", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexNavi(HttpServletRequest request) {
        String webSiteId = request.getHeader("client");
        Map<String, Object> resultData = FastMap.newInstance();
//        if (appIndexCache.get("navigationMenu") != null) {
//            resultData = appIndexCache.get("navigationMenu");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {

            beganTransaction = TransactionUtil.begin();
            
            Delegator delegator = (Delegator) request.getAttribute("delegator");
            LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
            // 排序字段列表
            List<String> orderBys = FastList.newInstance();
            orderBys.add("seqNo ASC");
            
            // 修改为动态查询
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("NM", "NavigationMenu");
            dynamicView.addAliasAll("NM", "", null);
            dynamicView.addAlias("NM", "navId");
            dynamicView.addAlias("NM", "navName");
            dynamicView.addAlias("NM", "contentId");
            dynamicView.addAlias("NM", "firstLinkType");
            dynamicView.addAlias("NM", "seqNo");
            dynamicView.addAlias("NM", "isAllWebSite");
            dynamicView.addAlias("NM", "linkUrl");
            dynamicView.addAlias("NM", "linkId");
            dynamicView.addAlias("NM", "linkName");
            dynamicView.addMemberEntity("BWS", "NavigationMenuWebSiteRef");
            dynamicView.addAlias("BWS", "webSiteId");
            dynamicView.addViewLink("NM", "BWS", true, ModelKeyMap.makeKeyMapList("navId", "navId"));

            List<String> fieldsToSelect = FastList.newInstance();
            fieldsToSelect.add("navId");
            fieldsToSelect.add("navName");
            fieldsToSelect.add("navUrl");
            fieldsToSelect.add("seqNo");
            fieldsToSelect.add("contentId");
            fieldsToSelect.add("isAllWebSite");
            fieldsToSelect.add("firstLinkType");
            fieldsToSelect.add("linkUrl");
            fieldsToSelect.add("linkId");
            fieldsToSelect.add("linkName");
            fieldsToSelect.add("webSiteId");
            
            dynamicView.setGroupBy(fieldsToSelect);
            EntityCondition mainCond = null;

            List<EntityCondition> andExprs = FastList.newInstance();
            //isEnabled 是否启用 Y是 N否
            andExprs.add(EntityCondition.makeCondition("isEnabled", "Y"));
            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            //isAllWebSite 0所有站点
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            andExprs.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            
            if (bannerConditions2.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }

            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBys, null);
            List<GenericValue> menuLists = eli.getCompleteList();
            if (UtilValidate.isNotEmpty(menuLists)) {
                List<Map> navigationMenuList = FastList.newInstance();
                for (GenericValue menuList : menuLists) {
                    Map menuMap = FastMap.newInstance();
                    String contentId = menuList.getString("contentId");
                    String navName = menuList.getString("navName");
                    String firstLinkType = menuList.getString("firstLinkType");
                    String linkUrl = menuList.getString("linkUrl");
                    String linkId = menuList.getString("linkId");
                    menuMap.put("navName", navName);
                    menuMap.put("contentId", contentId);
                    menuMap.put("firstLinkType", firstLinkType);
                    menuMap.put("linkId", linkId);
                    menuMap.put("linkUrl", linkUrl);
                    navigationMenuList.add(menuMap);
                }

                List<Map> newNavigationMenuList = FastList.newInstance();
                for (int i=0;i<navigationMenuList.size();i++){
                    Map<String,Object> navigetion = navigationMenuList.get(i);
                    Map newNavigetionMap = FastMap.newInstance();
                    String navName = navigetion.get("navName").toString();
                    String contentId = navigetion.get("contentId").toString();
                    String imgUrl = "";
                    if (UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                    //图片
                    newNavigetionMap.put("imgUrl", imgUrl);
                    newNavigetionMap.put("navName", navName);
                    String firstLinkType = navigetion.get("firstLinkType").toString();
                    if (UtilValidate.isNotEmpty(firstLinkType)){
                        if (UtilValidate.areEqual("FLT_ZDYLJ", firstLinkType)) {
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkUrl", navigetion.get("linkUrl"));
                        } else if (UtilValidate.areEqual("FLT_SPLJ", firstLinkType)) {
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                            newNavigetionMap.put("linkType", firstLinkType);
                        } else if (UtilValidate.areEqual("FLT_ZSLP", firstLinkType)) {
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_XYD", firstLinkType)) {
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_DTYJF", firstLinkType)) {
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_ZZK", firstLinkType)) {
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_PT", firstLinkType)) {
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        } else if (UtilValidate.areEqual("FLT_CHB", firstLinkType)) {
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        }else if (UtilValidate.areEqual("FLT_CXLJ", firstLinkType)) {//促销导航
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        }else if (UtilValidate.areEqual("FLT_HDLJ", firstLinkType)) {//活动导航
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        }else if (UtilValidate.areEqual("FLT_SPLJ", firstLinkType)) {//商品导航
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        }else if (UtilValidate.areEqual("FLT_WZLJ", firstLinkType)) {//文章导航
                            newNavigetionMap.put("linkType", firstLinkType);
                            newNavigetionMap.put("linkId", navigetion.get("linkId"));
                        }

                    }

                    newNavigationMenuList.add(newNavigetionMap);
                }
                resultData.put("navMenus", newNavigationMenuList);
            }
            
            resultData.put("totalSize", menuLists.size());
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
//        if (appIndexCache != null) {
//            appIndexCache.put("navigationMenu", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 首页公告查询
     *
     * @return
     */
    @RequestMapping(value = "/notice", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexNotice(HttpServletRequest request, @RequestBody Paginate paginate) {
        Map<String, Object> resultData = FastMap.newInstance();
//        if (appIndexCache.get("indexNotice") != null) {
//            resultData = appIndexCache.get("indexNotice");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        
        String webSiteId = request.getHeader("client");
       
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {

            beganTransaction = TransactionUtil.begin();
            List<String> orderBys = FastList.newInstance();
            orderBys.add("sequenceId ASC");
            
            //公告主表
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("NE", "Notice");
            dynamicView.addAliasAll("NE", "", null);
            //关联站点
            dynamicView.addMemberEntity("NWS", "NoticeWebSite");
            dynamicView.addAlias("NWS", "webSiteId");
            dynamicView.addViewLink("NE", "NWS", true, ModelKeyMap.makeKeyMapList("noticeId", "noticeId"));
            List<EntityCondition> bannerConditions1 = FastList.newInstance();
            //isUse 是否启用 0是 1否
            bannerConditions1.add(EntityCondition.makeCondition("isUse", "0"));
            
            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            //isAllWebSite 0所有站点
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            //webSiteId 传入的值WebStore表示小程序
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            bannerConditions1.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            List<String> fieldsToSelect = FastList.newInstance();
            fieldsToSelect.add("noticeId");
            fieldsToSelect.add("noticeTitle");
            fieldsToSelect.add("firstLinkType");
            fieldsToSelect.add("secondLinkType");
            fieldsToSelect.add("linkUrl");
            fieldsToSelect.add("linkId");
            fieldsToSelect.add("linkName");
            fieldsToSelect.add("isUse");
            fieldsToSelect.add("isAllWebSite");
            fieldsToSelect.add("isAllGeo");
            fieldsToSelect.add("sequenceId");
            fieldsToSelect.add("tagId");

            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            eli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(bannerConditions1, EntityOperator.AND), null, fieldsToSelect,
                    orderBys, findOpts);
            List<GenericValue> notices = eli.getPartialList(lowIndex, viewSize);
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();
            
            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            
            resultData.put("notices", notices);
            resultData.put("noticeSize", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
            
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
//        if (appIndexCache != null) {
//            appIndexCache.put("indexNotice", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
   
    
    /**
     * 首页-拼团
     *
     * @return
     */
    @RequestMapping(value = "/orderGroup", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexOrderGroup(HttpServletRequest request, @RequestBody Paginate paginate) {
        Map<String, Object> resultData = FastMap.newInstance();
//        if (appIndexCache.get("orderGroup") != null) {
//            resultData = appIndexCache.get("orderGroup");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        int page = paginate.getPage();
        int limit = paginate.getPageSize();

        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //记录集合
        List<Map> recordsList = FastList.newInstance();

        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 2;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 2;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        boolean beganTransaction = false;
        EntityListIterator eli = null;

        try {

            beganTransaction = TransactionUtil.begin();
            //活动表 主表
            dynamicView.addMemberEntity("PA", "ProductActivity");
            dynamicView.addAlias("PA", "activityId");
            dynamicView.addAlias("PA", "activityName");
            dynamicView.addAlias("PA", "activityStartDate");
            dynamicView.addAlias("PA", "activityEndDate");
            dynamicView.addAlias("PA", "activityAuditStatus");
            dynamicView.addAlias("PA", "activityType");
            //活动商品表
            dynamicView.addMemberEntity("PAG","ProductActivityGoods");
            dynamicView.addAlias("PAG","activityPrice");
            dynamicView.addAlias("PAG","productId");
            dynamicView.addAlias("PAG","occupiedQuantityTotal");
            dynamicView.addViewLink("PA", "PAG", false, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
            //关联Product 判断该商品是否上架 isOnline Y上架
            dynamicView.addMemberEntity("P","Product");
            dynamicView.addAlias("P","isOnline");
            dynamicView.addViewLink("PAG", "P", false, ModelKeyMap.makeKeyMapList("productId", "productId"));

            fieldsToSelect.add("activityId");
            fieldsToSelect.add("activityStartDate");
            fieldsToSelect.add("activityEndDate");
            fieldsToSelect.add("activityAuditStatus");
            fieldsToSelect.add("activityType");
            fieldsToSelect.add("activityPrice");
            fieldsToSelect.add("productId");
            fieldsToSelect.add("occupiedQuantityTotal");
            fieldsToSelect.add("isOnline");
            dynamicView.setGroupBy(fieldsToSelect);

            //排序字段名称 根据活动创建时间倒序
            String sortField = "-activityStartDate";
            orderBy.add(sortField);
            //判断活动类型 是否是拼团活动
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            //是否上架
            andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
            //按活动状态查询 ACTY_AUDIT_PASS审核通过
            andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            //活动的开始 结束时间是否有效
            andExprs.add(EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            andExprs.add(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp()));

            //添加where条件
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            Set<String> productIds = FastSet.newInstance();
            // 获取分页所需的记录集合
            for (GenericValue gv : eli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String activityId = gv.getString("activityId");
                //活动开始时间
                Timestamp activityStartDate = gv.getTimestamp("activityStartDate");
                //活动结束时间
                Timestamp activityEndDate = gv.getTimestamp("activityEndDate");
                //活动状态
                String activityAuditStatus = gv.getString("activityAuditStatus");
                //活动类型
                String activityType = gv.getString("activityType");
                //保留小数点后两位
                String activityPrice = gv.getBigDecimal("activityPrice").setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                //已团件数
                String occupiedQuantityTotal = gv.getString("occupiedQuantityTotal");
                String productId = gv.getString("productId");
                map.put("activityId", activityId);
                map.put("activityType", activityType);
                map.put("activityStartDate", activityStartDate);
                map.put("activityEndDate", activityEndDate);
                map.put("activityPrice", activityPrice);
                map.put("occupiedQuantityTotal", occupiedQuantityTotal);
                map.put("productId", productId);
                productIds.add(productId);
                String audityStatus = "";
                //如果活动开始时间大于当前的时间 则该活动是未开始
                if (activityAuditStatus.equals("ACTY_AUDIT_PASS") && activityStartDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "未开始";
                    //如果当前时间 介于活动开始时间和活动结束时间之间 则该活动是进行中
                } else if (activityAuditStatus.equals("ACTY_AUDIT_PASS") && activityStartDate.before(UtilDateTime.nowTimestamp()) && activityEndDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "进行中";
                }
                map.put("audityStatus", audityStatus);
                recordsList.add(map);
            }

            StringBuffer proIds = new StringBuffer();
            for (String pId : productIds){
                proIds.append(pId);
                proIds.append(",");
            }

            String prodStr = proIds.toString();
            if (prodStr.endsWith(",")) {
                prodStr = prodStr.substring(0, prodStr.length() - 1);
            }

            List<Map> productList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(prodStr)) {
                //productsSummary 公共的服务 用来处理商品的几种价格
                Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr.toString()));
                List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                if (UtilValidate.isNotEmpty(products)) {
                    for (int j = 0; j < products.size(); j++) {
                        Map<String, Object> product = products.get(j);
                        Map<String, Object> productMap = FastMap.newInstance();
                        String pId = product.get("productId").toString();
                        productMap.put("productId", pId);
                        BigDecimal price = (BigDecimal) product.get("price");
                        if (UtilValidate.isEmpty(price)) {
                            price = BigDecimal.ZERO;
                        }
                        productMap.put("price", price);
                        productMap.put("productName", product.get("productName"));
                        productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                        if (UtilValidate.isNotEmpty(pId)) {
                            //根据商品id找到相应的拼团主表信息 获取拼团主表TogetherGroup的id togetherId
                            List<GenericValue> toGroups = delegator.findByAnd("TogetherGroup", UtilMisc.toMap("productId", pId),UtilMisc.toList("-createDate"));
                            List<String> personList = FastList.newInstance();
                            if (UtilValidate.isNotEmpty(toGroups)) {
                                Set<String> userPartyId = FastSet.newInstance();
                                for (int t=0;t<toGroups.size();t++){
                                    String togetherId = toGroups.get(t).getString("togetherId");
                                    //根据togetherId 关联拼团订单表TogetherGroupRelOrder获取到当前拼团商品下的用户的头像
                                    List<GenericValue> groupRelOrders = delegator.findByAnd("TogetherGroupRelOrder", UtilMisc.toMap("togetherId", togetherId));
                                    if (UtilValidate.isNotEmpty(groupRelOrders)) {
                                        for (int g = 0; g < groupRelOrders.size(); g++) {
                                            String orderId = groupRelOrders.get(g).getString("orderId");
                                            String orderUserId = groupRelOrders.get(g).getString("orderUserId");
                                            List<GenericValue> orderPayments = delegator.findByAnd("OrderPaymentPreference",UtilMisc.toMap("orderId",orderId));
                                            if (UtilValidate.isNotEmpty(orderPayments)){
                                                GenericValue orderPayment = orderPayments.get(0);
                                                String statusId = orderPayment.getString("statusId");
                                                //查询出订单支付状态 将对应的id添加
                                                if ("PAYMENT_RECEIVED".equals(statusId)){
                                                    userPartyId.add(orderUserId);
                                                }
                                            }

                                        }
                                    }
                                }

                                for (String partyId : userPartyId ){
                                    GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
                                    if (UtilValidate.isNotEmpty(person)){
                                        String headphoto = person.getString("headphoto");
                                        personList.add(headphoto);
                                    }
                                }

                                //头像展示 大于5个则展示5个
                                if (personList.size() > 5) {
                                    personList = personList.subList(0, 5);
                                }
                               
                                productMap.put("headImlUrl", personList);
                            }
                        }

                        productList.add(productMap);
                    }
                }
            }

            List<Map> newRecordList = FastList.newInstance();
            for (int i = 0;i<recordsList.size();i++){
                Map<String,Object> record = recordsList.get(i);
                Map<String,Object> newRecordMap = FastMap.newInstance();
                newRecordMap.put("activityId",record.get("activityId"));
                newRecordMap.put("activityType",record.get("activityType"));
                newRecordMap.put("activityStartDate",record.get("activityStartDate"));
                newRecordMap.put("activityEndDate",record.get("activityEndDate"));
                String activityPrice = record.get("activityPrice").toString();
                newRecordMap.put("activityPrice",record.get("activityPrice"));
                String occupiedQuantityTotal =UtilValidate.isEmpty(record.get("occupiedQuantityTotal"))?"0":record.get("occupiedQuantityTotal").toString();
                String productId = record.get("productId").toString();
                newRecordMap.put("productId",record.get("productId"));
                newRecordMap.put("audityStatus",record.get("audityStatus"));
                List<Map> newProductList = FastList.newInstance();
                for (int j=0;j<productList.size();j++){
                    Map<String,Object> product = productList.get(j);
                    Map<String,Object> newProductMap = FastMap.newInstance();
                    String pId = product.get("productId").toString();
                    if (pId.equals(productId)){
                        newProductMap.put("productId",pId);
                        newProductMap.put("price",product.get("price"));
                        newProductMap.put("productName",product.get("productName"));
                        newProductMap.put("mediumImageUrl",product.get("mediumImageUrl"));
                        newProductMap.put("activityPrice",activityPrice);
                        newProductMap.put("activityQuantity",occupiedQuantityTotal);
                        List<String> newHeadImgList = FastList.newInstance();
                        List<String> headImgList =UtilValidate.isEmpty(product.get("headImlUrl"))?null: (List<String>) product.get("headImlUrl");
                        if(UtilValidate.isNotEmpty(headImgList)){
                            for (int k = 0;k<headImgList.size();k++){
                                String headImgUrl = headImgList.get(k);
                                newHeadImgList.add(headImgUrl);
                            }
                            newProductMap.put("headImlUrl",newHeadImgList);
                        }
                        if (UtilValidate.isNotEmpty(newProductMap)){
                            newProductList.add(newProductMap);
                        }

                    }

                }
                newRecordMap.put("products",newProductList);
                newRecordList.add(newRecordMap);
            }

            resultData.put("recordsList", newRecordList);

            // 获取总记录数
            size = eli.getResultsSizeAfterPartialList();

            boolean hasNext = true;
            boolean hasPrev = true;

            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }

            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
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
        //返回的参数

        resultData.put("totalSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));

//        if (appIndexCache != null) {
//            appIndexCache.put("orderGroup", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


    /**
     * 首页-活动推荐
     *
     * @return
     */
    @RequestMapping(value = "/activityRecommend", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexActivityRecomm(HttpServletRequest request, @RequestBody Paginate paginate) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();
        EntityCondition mainCond = null;
//        if (appIndexCache.get("activityRecommend") != null) {
//            resultData = appIndexCache.get("activityRecommend");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        String webSiteId = request.getHeader("client");
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 6;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 6;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("error", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        
        try {
            beganTransaction = TransactionUtil.begin();
            //活动区块--活动管理
            DynamicViewEntity topicActivityViewEntity = new DynamicViewEntity();
            //显示字段集合
            List<String> fieldsToSelect = FastList.newInstance();
            //专题活动表 主表
            topicActivityViewEntity.addMemberEntity("PTA", "ProductTopicActivity");
            topicActivityViewEntity.addAlias("PTA", "productTopicActivityId");
            topicActivityViewEntity.addAlias("PTA", "isUse");
            topicActivityViewEntity.addAlias("PTA", "isAllWebSite");
            topicActivityViewEntity.addAlias("PTA", "bigImg");
            topicActivityViewEntity.addAlias("PTA", "linkType");
            topicActivityViewEntity.addAlias("PTA", "linkUrl");
            topicActivityViewEntity.addAlias("PTA", "linkId");
            topicActivityViewEntity.addAlias("PTA", "sequenceId");
            topicActivityViewEntity.addAlias("PTA", "topicActivityName");
            //专题活动站点表 关联表
            topicActivityViewEntity.addMemberEntity("PTAWS", "ProductTopicActivityWebSite");
            topicActivityViewEntity.addAlias("PTAWS", "webSiteId");
            topicActivityViewEntity.addViewLink("PTA", "PTAWS", true, ModelKeyMap.makeKeyMapList("productTopicActivityId", "productTopicActivityId"));

            fieldsToSelect.add("productTopicActivityId");
            fieldsToSelect.add("isUse");
            fieldsToSelect.add("bigImg");
            fieldsToSelect.add("linkType");
            fieldsToSelect.add("isAllWebSite");
            fieldsToSelect.add("webSiteId");
            fieldsToSelect.add("topicActivityName");
            fieldsToSelect.add("linkId");
            fieldsToSelect.add("sequenceId");
            topicActivityViewEntity.setGroupBy(fieldsToSelect);
            
            List<String> orderBy = FastList.newInstance();
            //根据序列号 正序查询
            orderBy.add("sequenceId");
            List<EntityCondition> topicActivityConditions1 = FastList.newInstance();
            //isUse 是否启用 0是 1否
            topicActivityConditions1.add(EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, "0"));
            //链接类型不为空
            //topicActivityConditions1.add(EntityCondition.makeCondition("linkType", EntityOperator.NOT_EQUAL, null));
            List<EntityCondition> topicActivityConditions2 = FastList.newInstance();
            //isAllWebSite 0全站点
            topicActivityConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            //webSiteId 小程序
            topicActivityConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            topicActivityConditions1.add(EntityCondition.makeCondition(topicActivityConditions2, EntityOperator.OR));

            if (topicActivityConditions1.size()>0){
                mainCond = EntityCondition.makeCondition(topicActivityConditions1, EntityOperator.AND);
            }

            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            eli = delegator.findListIteratorByCondition(topicActivityViewEntity, mainCond, null, fieldsToSelect, orderBy, findOpts);
            List<GenericValue> topicActivitys = eli.getPartialList(lowIndex, viewSize);
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();

            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            
            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));

            if (UtilValidate.isNotEmpty(topicActivitys)) {
                List<Map> activityList = FastList.newInstance();
                for (GenericValue topicActivity : topicActivitys) {
                    Map<String,Object> activityMap = FastMap.newInstance();
                    activityMap.put("activityName", topicActivity.getString("topicActivityName"));
                    String contentId = (String) topicActivity.get("bigImg");
                    String linkType = (String) topicActivity.get("linkType");
                    String linkUrl = UtilValidate.isEmpty(topicActivity.get("linkUrl"))?null:topicActivity.get("linkUrl").toString();
                    String linkId = (String) topicActivity.get("linkId");
                    activityMap.put("contentId", contentId);
                    activityMap.put("linkType", linkType);
                    activityMap.put("linkUrl", linkUrl);
                    activityMap.put("linkId", linkId);

                    activityList.add(activityMap);
                }

                List<Map> newActivityList = FastList.newInstance();
                for (int i = 0;i<activityList.size();i++){
                    Map<String,Object> activity = activityList.get(i);
                    Map<String,Object> newActivityMap = FastMap.newInstance();
                    String activityName = activity.get("activityName").toString();
                    String contentId = activity.get("contentId").toString();

                    String imgUrl = "";
                    if (UtilValidate.isNotEmpty(contentId)) {
                        //图片
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                        newActivityMap.put("imgUrl", imgUrl);
                    }
                    newActivityMap.put("activityName",activityName);
                    String linkType = activity.get("linkType").toString();
                    if (UtilValidate.isNotEmpty(linkType)){
                        if (UtilValidate.areEqual("FLT_ZDYLJ", linkType)) {
                            newActivityMap.put("linkType", linkType);
                            newActivityMap.put("linkUrl", activity.get("linkUrl"));
                        } else if (UtilValidate.areEqual("FLT_SPLJ", linkType)) {
                            newActivityMap.put("linkType", linkType);
                            newActivityMap.put("linkId", activity.get("linkId"));
                        } else {
                            newActivityMap.put("linkType", linkType);
                            newActivityMap.put("linkId", activity.get("linkId"));
                        }
                    }
                    newActivityList.add(newActivityMap);
                }
                resultData.put("activityList", newActivityList);
            }
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
//        if (appIndexCache != null) {
//            appIndexCache.put("activityRecommend", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 首页-秒杀
     *
     * @return
     */
    @RequestMapping(value = "/seckill", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexSeckill(HttpServletRequest request, @RequestBody Paginate paginate) {
        Map<String, Object> resultData = FastMap.newInstance();

//        if (appIndexCache.get("seckill") != null) {
//            resultData = appIndexCache.get("seckill");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }

        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //记录集合
        List<Map> recordsList = FastList.newInstance();

        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {

            beganTransaction = TransactionUtil.begin();
            //动态view
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            //查询条件集合，用于数据库查询
            List<EntityCondition> andExprs = FastList.newInstance();
            EntityCondition mainCond = null;
            //排序字段集合
            List<String> orderBy = FastList.newInstance();
            //显示字段集合
            List<String> fieldsToSelect = FastList.newInstance();
            //活动表 主表
            dynamicView.addMemberEntity("PA", "ProductActivity");
            dynamicView.addAlias("PA", "activityId");
            //活动名称
            dynamicView.addAlias("PA", "activityName");
            //活动开始时间
            dynamicView.addAlias("PA", "activityStartDate");
            //活动结束时间
            dynamicView.addAlias("PA", "activityEndDate");
            //活动状态 ACTY_AUDIT_PASS 都是已经审核过的
            dynamicView.addAlias("PA", "activityAuditStatus");
            //活动类型
            dynamicView.addAlias("PA", "activityType");
            //枚举值表
            dynamicView.addMemberEntity("E", "Enumeration");
            dynamicView.addAlias("E", "enumId");
            dynamicView.addAlias("E", "activityTypeName", "description", null, false, null, null);
            dynamicView.addViewLink("PA", "E", false, ModelKeyMap.makeKeyMapList("activityType", "enumId"));
            //活动商品表 关联表
            dynamicView.addMemberEntity("PAG","ProductActivityGoods");
            //秒杀活动价
            dynamicView.addAlias("PAG","activityPrice");
            dynamicView.addAlias("PAG","productId");
            dynamicView.addAlias("PAG","activityQuantity");
            dynamicView.addViewLink("PA", "PAG", false, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
            //商品表 根据productId与活动商品表进行关联
            dynamicView.addMemberEntity("P","Product");
            dynamicView.addAlias("P","isOnline");
            dynamicView.addViewLink("PAG", "P", false, ModelKeyMap.makeKeyMapList("productId", "productId"));
            fieldsToSelect.add("activityId");
            fieldsToSelect.add("activityName");
            fieldsToSelect.add("activityStartDate");
            fieldsToSelect.add("activityEndDate");
            fieldsToSelect.add("activityAuditStatus");
            fieldsToSelect.add("activityType");
            fieldsToSelect.add("activityTypeName");
            fieldsToSelect.add("activityPrice");
            fieldsToSelect.add("productId");
            fieldsToSelect.add("activityQuantity");
            fieldsToSelect.add("isOnline");
            dynamicView.setGroupBy(fieldsToSelect);

            //排序字段名称
            String sortField = "-activityStartDate";
            orderBy.add(sortField);

            //按促销类型查询
            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
            andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
            //进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
            andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
            andExprs.add(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp()));
            andExprs.add(EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));

            //添加where条件
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }

            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            Set<String> productIds = FastSet.newInstance();
            // 获取分页所需的记录集合
            for (GenericValue gv : eli.getPartialList(lowIndex, viewSize)) {
                Map map = FastMap.newInstance();
                String activityId = gv.getString("activityId");
                String activityName = gv.getString("activityName");
                Timestamp activityStartDate = gv.getTimestamp("activityStartDate");
                Timestamp activityEndDate = gv.getTimestamp("activityEndDate");
                String activityAuditStatus = gv.getString("activityAuditStatus");
                String activityType = gv.getString("activityType");
                String activityTypeName = gv.getString("activityTypeName");
                BigDecimal activityPrice = gv.getBigDecimal("activityPrice").setScale(2, BigDecimal.ROUND_HALF_UP);
                String activityQuantity = gv.getString("activityQuantity");
                String productId = gv.getString("productId");
                map.put("activityId", activityId);
                map.put("activityType", activityType);
                map.put("activityTypeName", activityTypeName);
                map.put("activityName", activityName);
                map.put("activityStartDate", activityStartDate);
                map.put("activityEndDate", activityEndDate);
                map.put("productId",productId);
                map.put("activityPrice", activityPrice);
                map.put("activityQuantity", activityQuantity);
                productIds.add(productId);
                String audityStatus = "";
                if (activityAuditStatus.equals("ACTY_AUDIT_PASS") && activityStartDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "未开始";
                } else if (activityAuditStatus.equals("ACTY_AUDIT_PASS") && activityStartDate.before(UtilDateTime.nowTimestamp()) && activityEndDate.after(UtilDateTime.nowTimestamp())) {
                    audityStatus = "进行中";
                }
                map.put("audityStatus", audityStatus);
                recordsList.add(map);
            }

            StringBuffer proIds = new StringBuffer();
            for (String pId : productIds){
                proIds.append(pId);
                proIds.append(",");
            }

            String prodStr = proIds.toString();
            if (prodStr.endsWith(",")) {
                prodStr = prodStr.substring(0, prodStr.length() - 1);
            }

            List<Map> productList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(prodStr)) {
                Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr));
                List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                if (UtilValidate.isNotEmpty(products)) {
                    for (int j = 0; j < products.size(); j++) {
                        Map<String, Object> product = products.get(j);
                        Map<String, Object> productMap = FastMap.newInstance();
                        productMap.put("productId", product.get("productId"));
                        productMap.put("price", product.get("price"));
                        productMap.put("productName", product.get("productName"));
                        productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                        productList.add(productMap);
                    }
                }
            }

            List<Map> newRecordsList = FastList.newInstance();
            for (int i=0;i<recordsList.size();i++){
                Map<String,Object> record = recordsList.get(i);
                Map newRecord = FastMap.newInstance();
                newRecord.put("activityId",record.get("activityId"));
                newRecord.put("activityType",record.get("activityType"));
                newRecord.put("activityName",record.get("activityName"));
                newRecord.put("activityStartDate",record.get("activityStartDate"));
                newRecord.put("activityEndDate",record.get("activityEndDate"));
                String activityPrice = record.get("activityPrice").toString();
                String activityQuantity = record.get("activityQuantity").toString();
                newRecord.put("audityStatus",record.get("audityStatus"));
                newRecord.put("productId",record.get("productId"));
                String productId = record.get("productId").toString();
                List<Map> newProductList = FastList.newInstance();
                for (int j = 0;j<productList.size();j++){
                    Map<String,Object> product = productList.get(j);
                    Map<String,Object> newProductMap = FastMap.newInstance();
                    String pId = product.get("productId").toString();
                    if (pId.equals(productId)){
                        newProductMap.put("productId",pId);
                        newProductMap.put("price",product.get("price"));
                        newProductMap.put("productName",product.get("productName"));
                        newProductMap.put("activityPrice",activityPrice);
                        newProductMap.put("activityQuantity",activityQuantity);
                        newProductMap.put("mediumImageUrl",product.get("mediumImageUrl"));
                    }
                    if(UtilValidate.isNotEmpty(newProductMap)){
                        newProductList.add(newProductMap);
                    }
                }
                newRecord.put("products",newProductList);
                newRecordsList.add(newRecord);
            }
            resultData.put("recordsList", newRecordsList);

            // 获取总记录数
            size = eli.getResultsSizeAfterPartialList();

            boolean hasNext = true;
            boolean hasPrev = true;

            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }

            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);

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

        resultData.put("totalSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));

//        if (appIndexCache != null) {
//            appIndexCache.put("seckill", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 首页-微刊
     *
     * @return
     */
    @RequestMapping(value = "/microPaper", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexMacroPaper(HttpServletRequest request, @RequestBody Paginate paginate) {
        Map<String, Object> resultData = FastMap.newInstance();
//        if (appIndexCache.get("microPaper") != null) {
//            resultData = appIndexCache.get("microPaper");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        // 文章类型
        String articleTypeId = "STAT_MICRO_PAPER";
        // 文章状态--已审批
        String articleStatus = "2";
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        int size = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));

        List<GenericValue> articleList = null;
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            
            beganTransaction = TransactionUtil.begin();
            
            DynamicViewEntity dve = new DynamicViewEntity();
            dve.addMemberEntity("A", "Article");
            // 文章编号
            dve.addAlias("A", "articleId");
            // 文章标题
            dve.addAlias("A", "articleTitle");
            // 文章作者
            dve.addAlias("A", "articleAuthor");
            // 文章类型
            dve.addAlias("A", "articleTypeId");
            // 文章状态
            dve.addAlias("A", "articleStatus");
            // 创建时间
            dve.addAlias("A", "createdStamp");
            // 店铺信息
            dve.addAlias("A", "productStoreId");
            dve.addMemberEntity("AT", "ArticleType");
            // 文章类型ID
            dve.addAlias("AT", "articleTypeId");
            // 类型描述
            dve.addAlias("AT", "description");
            dve.addViewLink("A", "AT", Boolean.FALSE, ModelKeyMap.makeKeyMapList("articleTypeId", "articleTypeId"));
            //文章内容
            dve.addMemberEntity("AC","ArticleContent");
            dve.addAlias("AC","articleContentTypeId");
            dve.addAlias("AC","contentId");
            dve.addViewLink("A", "AC", Boolean.FALSE, ModelKeyMap.makeKeyMapList("articleId", "articleId"));

            List<String> fieldsToSelect = FastList.newInstance();
            fieldsToSelect.add("articleId");
            fieldsToSelect.add("articleTitle");
            fieldsToSelect.add("articleAuthor");
            fieldsToSelect.add("articleTypeId");
            fieldsToSelect.add("articleStatus");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("articleContentTypeId");
            fieldsToSelect.add("contentId");
            dve.setGroupBy(fieldsToSelect);
            
            List<String> orderBy = FastList.newInstance();
            orderBy.add("createdStamp DESC");
            
            // define the main condition & expression list
            List<EntityCondition> andExprs = FastList.newInstance();
            EntityCondition mainCond = null;
            
            if (UtilValidate.isNotEmpty(articleStatus) && !articleStatus.equals("-1")) {
                andExprs.add(EntityCondition.makeCondition("articleStatus", EntityOperator.EQUALS, articleStatus));
            }
            if (UtilValidate.isNotEmpty(articleTypeId) && !articleTypeId.equals("-1")) {
                andExprs.add(EntityCondition.makeCondition("articleTypeId", EntityOperator.EQUALS, articleTypeId));
            }
            andExprs.add(EntityCondition.makeCondition("articleContentTypeId", EntityOperator.EQUALS, "ARTICLE_FIGURE"));
            // build the main condition
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            eli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, orderBy, findOpts);
            articleList = eli.getPartialList(lowIndex, viewSize);
            // attempt to get the full size
            size = eli.getResultsSizeAfterPartialList();

            if (UtilValidate.isNotEmpty(articleList)) {
                List<Map> newArtList = FastList.newInstance();
                for (int i = 0; i < articleList.size(); i++) {
                    Map<String, Object> art = articleList.get(i);
                    Map newArtMap = FastMap.newInstance();
                    String articleId = art.get("articleId").toString();
                    newArtMap.put("articleId", art.get("articleId"));
                    newArtMap.put("articleTitle", art.get("articleTitle"));
                    newArtMap.put("contentId",art.get("contentId"));
                    List<GenericValue> articleTagAss = delegator.findByAnd("ArticleTagAssocAndTag",UtilMisc.toMap("articleId",articleId));
                    if (UtilValidate.isNotEmpty(articleTagAss)) {
                        List<Map> newTagList = FastList.newInstance();
                        for (int a = 0; a < articleTagAss.size(); a++) {
                            Map<String, Object> newTagMap = FastMap.newInstance();
                            String tagName = articleTagAss.get(a).getString("tagName");
                            newTagMap.put("tagName", tagName);
                            newTagList.add(newTagMap);
                        }
                        if (UtilValidate.isNotEmpty(newTagList)){
                            newArtMap.put("tagList", newTagList);
                        }
                    }
                    newArtList.add(newArtMap);
                }

                List<Map> newArticleList = FastList.newInstance();
                for (int a = 0;a<newArtList.size();a++){
                    Map<String,Object> art = newArtList.get(a);
                    Map<String,Object> newArticleMap = FastMap.newInstance();
                    newArticleMap.put("articleId",art.get("articleId"));
                    newArticleMap.put("articleTitle",art.get("articleTitle"));
                    String contentId = art.get("contentId").toString();
                    String imgUrl = "";
                    if (UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                        newArticleMap.put("imgUrl", imgUrl);
                    }
                    List<Map> newTagList = FastList.newInstance();
                    List<Map> tagList = UtilValidate.isEmpty(art.get("tagList"))?null:(List<Map>) art.get("tagList");
                    if (UtilValidate.isNotEmpty(tagList)){
                        for (int t = 0;t<tagList.size();t++){
                            Map<String,Object> newTagMap = FastMap.newInstance();
                            String tagName = tagList.get(t).get("tagName").toString();
                            newTagMap.put("tagName",tagName);
                            newTagList.add(newTagMap);
                        }
                    }
                    newArticleMap.put("tagList",newTagList);
                    newArticleList.add(newArticleMap);
                }
                resultData.put("articleList", newArticleList);
            }

            resultData.put("articleListSize", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));

            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);

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
//        if(appIndexCache!=null){
//            appIndexCache.put("microPaper",resultData);
//        }
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 首页 促销专题
     *
     * @param request
     * @param paginate
     * @return
     */
    @RequestMapping(value = "/promoArea", method = RequestMethod.POST,produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> indexPromoArea( HttpServletRequest request , @RequestBody Paginate paginate) {
        Map<String, Object> resultData = FastMap.newInstance();
    
//        if (appIndexCache.get("promoArea") != null) {
//            resultData = appIndexCache.get("promoArea");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        LocalDispatcher dispatcher = (LocalDispatcher)request.getAttribute("dispatcher");
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        List<Map> recordsList = FastList.newInstance();
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));

        boolean beganTransaction = false;
        EntityListIterator eli =null;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PAM", "ProductActivityManager");
            dynamicView.addAlias("PAM",  "productActivityManagerId");
            dynamicView.addAlias("PAM",  "activityManagerName");
            dynamicView.addAlias("PAM",  "contentId");
            dynamicView.addAlias("PAM",  "sequenceId");
            dynamicView.addAlias("PAM",  "productStoreId");
            dynamicView.addAlias("PAM",  "fromDate");
            dynamicView.addAlias("PAM",  "thruDate");
            fieldsToSelect.add("productActivityManagerId");
            fieldsToSelect.add("activityManagerName");
            fieldsToSelect.add("contentId");
            fieldsToSelect.add("sequenceId");
            dynamicView.setGroupBy(fieldsToSelect);
            orderBy.add("sequenceId");

            List<GenericValue> partyGroups = delegator.findByAnd("PartyGroup",UtilMisc.toMap("isInner","Y"));
            String productStoreId = partyGroups.get(0).getString("productStoreId");
            Timestamp now = new Timestamp(System.currentTimeMillis());
            //有促销专题活动即都筛选出来
            andExprs.add(EntityCondition.makeCondition("activityManagerName", EntityOperator.NOT_EQUAL, null));
            andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, productStoreId));
            andExprs.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN, now));
            andExprs.add(EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, now));

            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }

            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,EntityFindOptions.TYPE_SCROLL_INSENSITIVE,EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);

            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 查询出总的记录数
            Integer size = eli.getResultsSizeAfterPartialList();

            resultData.put("highIndex",highIndex);
            resultData.put("lowIndex",lowIndex);
            resultData.put("size",size);

            for (GenericValue activityManager : eli.getPartialList(lowIndex,highIndex)){
                Map<String,Object> activityMap = FastMap.newInstance();
                String productActivityManagerId = activityManager.getString("productActivityManagerId");
                String contentId = activityManager.getString("contentId");
                String activityManagerName = activityManager.getString("activityManagerName");
                activityMap.put("contentId",contentId);
                activityMap.put("productActivityManagerId",productActivityManagerId);
                activityMap.put("activityManagerName",activityManagerName);
                List productList = FastList.newInstance();

                List<GenericValue> newSubjects = delegator.findByAnd("ManagerAndSubjectAndProduct",UtilMisc.toMap("productActivityManagerId",productActivityManagerId),UtilMisc.toList("-createdStamp"));
                if (UtilValidate.isNotEmpty(newSubjects)){
                    Set<String> productIds = FastSet.newInstance();
                    for (int i = 0;i<newSubjects.size();i++){
                        String productId = newSubjects.get(i).getString("productId");
                        productIds.add(productId);
                    }

                    StringBuffer pIds = new StringBuffer();
                    for (String pId : productIds){
                        pIds.append(pId);
                        pIds.append(",");
                    }
                    String prodStr = pIds.toString();
                    if (productIds.size()<6){
                        if (prodStr.endsWith(",")) {
                            prodStr = prodStr.substring(0, prodStr.length() - 1);
                        }
                    }else {
                        if (prodStr.endsWith(",")) {
                            prodStr = prodStr.substring(0, 35);
                        }
                    }

                    if (UtilValidate.isNotEmpty(prodStr)) {
                        Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr.toString()));
                        List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                        if(UtilValidate.isNotEmpty(products)){
                            for (int k = 0; k < products.size(); k++) {
                                Map<String,Object> product = products.get(k);
                                Map<String,Object> productMap = FastMap.newInstance();
                                productMap.put("productId", product.get("productId"));
                                productMap.put("productName", product.get("productName"));
                                productMap.put("integralDeductionType",product.get("integralDeductionType"));
                                //是否使用积分进行判断
                                if (UtilValidate.areEqual("2", product.get("integralDeductionType")) || UtilValidate.areEqual("3", product.get("integralDeductionType"))) {
                                    //所需要的积分数
                                    productMap.put("scoreValue", product.get("scoreValue"));
                                    BigDecimal diffPrice = (BigDecimal) product.get("diffPrice");
                                    if (UtilValidate.isEmpty(diffPrice)) {
                                        diffPrice = BigDecimal.ZERO;
                                    }
                                    //差价
                                    productMap.put("diffPrice", diffPrice);
                                }else {
                                    productMap.put("scoreValue", null);
                                    productMap.put("diffPrice", null);
                                }
                                BigDecimal price = ((BigDecimal) product.get("price")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                if (UtilValidate.isEmpty(price)) {
                                    price = BigDecimal.ZERO;
                                }
                                BigDecimal activityPrice = BigDecimal.ZERO;
                                BigDecimal amount = BigDecimal.ZERO;
                                Map<String, Object> orderGroupInfos = (Map<String, Object>) product.get("orderGroupInfo");
                                Map<String, Object> secKillInfo = (Map<String, Object>) product.get("secKillInfo");
                                Map<String, Object> priceDownInfo = (Map<String, Object>) product.get("priceDownInfo");
                                if (UtilValidate.isNotEmpty(orderGroupInfos) && orderGroupInfos.size() > 0) {
                                    activityPrice = ((BigDecimal) orderGroupInfos.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("activityPrice", activityPrice);
                                } else if (UtilValidate.isNotEmpty(secKillInfo) && secKillInfo.size() > 0) {
                                    activityPrice = ((BigDecimal) secKillInfo.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("activityPrice", activityPrice);
                                } else if (UtilValidate.isNotEmpty(priceDownInfo) && priceDownInfo.size() > 0) {
                                    amount = ((BigDecimal) priceDownInfo.get("amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("activityPrice", amount);
                                } else if (UtilValidate.isEmpty(orderGroupInfos) && UtilValidate.isEmpty(secKillInfo) && UtilValidate.isEmpty(priceDownInfo)) {
                                    productMap.put("price", price);
                                }
                                productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                productList.add(productMap);
                            }

                        }
                    }

                }
                activityMap.put("products", productList);
                recordsList.add(activityMap);
            }

            List<Map> newRedordsList = FastList.newInstance();
            for (int i = 0;i<recordsList.size();i++){
                Map<String,Object> activity = recordsList.get(i);
                Map newActivityMap = FastMap.newInstance();
                newActivityMap.put("activityManagerName",activity.get("activityManagerName"));
                newActivityMap.put("productActivityManagerId",activity.get("productActivityManagerId"));
                String contentId = activity.get("contentId").toString();
                String imgUrl = "";
                if(UtilValidate.isNotEmpty(contentId)) {
                    imgUrl = baseImgUrl+ ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                }
                //图片
                newActivityMap.put("activityImg",imgUrl);
                List<Map> products = (List) activity.get("products");
                List<Map> newProduct = FastList.newInstance();
                for (int p = 0;p<products.size();p++){
                    Map<String,Object> productMap = products.get(p);
                    Map newProductMap = FastMap.newInstance();
                    newProductMap.put("productId",productMap.get("productId"));
                    newProductMap.put("productName",productMap.get("productName"));
                    //商品图片
                    newProductMap.put("mediumImageUrl",productMap.get("mediumImageUrl"));
                    String integralDeductionType = productMap.get("integralDeductionType").toString();
                    if ("2".equals(integralDeductionType)){
                        int scoreValue2 = UtilValidate.isEmpty(productMap.get("scoreValue"))?null:(Integer) productMap.get("scoreValue");
                        BigDecimal diffPrice2 =UtilValidate.isEmpty (productMap.get("diffPrice"))?null:((BigDecimal) productMap.get("diffPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                        //所需积分数
                        if (UtilValidate.isNotEmpty(scoreValue2)){
                            newProductMap.put("scoreValue", scoreValue2);
                        }
                        if (UtilValidate.isNotEmpty(diffPrice2)){
                            newProductMap.put("diffPrice", diffPrice2);
                        }

                    }else if ("3".equals(integralDeductionType)){
                        BigDecimal scoreValue3 =UtilValidate.isEmpty (productMap.get("scoreValue"))?null:((BigDecimal) productMap.get("scoreValue")).setScale(2, BigDecimal.ROUND_HALF_UP);
                        BigDecimal diffPrice3 =UtilValidate.isEmpty (productMap.get("diffPrice"))?null:((BigDecimal) productMap.get("diffPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);

                        if (UtilValidate.isNotEmpty(scoreValue3)){
                            newProductMap.put("scoreValue", scoreValue3);
                        }
                        if (UtilValidate.isNotEmpty(diffPrice3)){
                            newProductMap.put("diffPrice", diffPrice3);
                        }
                    }

                    BigDecimal price1 = UtilValidate.isEmpty((BigDecimal) productMap.get("price"))?null:(BigDecimal) productMap.get("price");
                    BigDecimal activityPrice1 = UtilValidate.isEmpty((BigDecimal) productMap.get("activityPrice"))?null:(BigDecimal) productMap.get("activityPrice");
                    if (UtilValidate.isEmpty(price1)){
                        newProductMap.put("price", activityPrice1);
                    }else {
                        newProductMap.put("price", price1);
                    }
                    newProduct.add(newProductMap);
                }
                newActivityMap.put("products",newProduct);
                newRedordsList.add(newActivityMap);

            }

            boolean hasNext = true;
            boolean hasPrev = true;

            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("activityList",newRedordsList);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");

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
//        if (appIndexCache != null) {
//            appIndexCache.put("promoArea", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 首页-热门品牌
     *
     * @return
     */
    @RequestMapping(value = "/hotBrand", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexHotBrand(HttpServletRequest request, @RequestBody Paginate paginate) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();
//        if (appIndexCache.get("hotBrand") != null) {
//            resultData = appIndexCache.get("hotBrand");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        String webSiteId = request.getHeader("client");
        
        EntityCondition mainCond = null;
        
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> andExprs = FastList.newInstance();
        
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 6;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 6;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("BR", "BrandRecommend");
            dynamicView.addAlias("BR", "recommendId");
            dynamicView.addAlias("BR", "brandId");
            dynamicView.addAlias("BR", "isAllWebSite");
            dynamicView.addAlias("BR", "sequenceId");
            dynamicView.addAlias("BR", "status");

            dynamicView.addMemberEntity("BWS", "BrandRecommendWebSite");
            dynamicView.addAlias("BWS", "webSiteId");
            dynamicView.addViewLink("BR", "BWS", true, ModelKeyMap.makeKeyMapList("recommendId", "recommendId"));
            
            dynamicView.addMemberEntity("BD", "ProductBrand");
            dynamicView.addAlias("BD","contentId");
            dynamicView.addAlias("BD","brandName");
            dynamicView.addAlias("BD","productBrandId");
            dynamicView.addViewLink("BR", "BD", true, ModelKeyMap.makeKeyMapList("brandId", "productBrandId"));
            
            List<String> fieldToSel = FastList.newInstance();
            fieldToSel.add("recommendId");
            fieldToSel.add("brandId");
            fieldToSel.add("productBrandId");
            fieldToSel.add("brandName");
            fieldToSel.add("contentId");
            fieldToSel.add("isAllWebSite");
            fieldToSel.add("webSiteId");
            fieldToSel.add("sequenceId");
            fieldToSel.add("status");
            dynamicView.setGroupBy(fieldToSel);
            
            //排序字段名称
            String sortField = "sequenceId";
            orderBy.add(sortField);
            
            andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, "Y"));
            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            andExprs.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            
            //添加where条件
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldToSel, orderBy, findOpts);
            List<GenericValue> brandRecommends = eli.getPartialList(lowIndex, viewSize);
            // 查出处总的的记录数
            Integer size = eli.getResultsSizeAfterPartialList();
            
            List<Map<String, Object>> brandList = FastList.newInstance();
            
            if (UtilValidate.isNotEmpty(brandRecommends)) {
                for (GenericValue brand : brandRecommends) {
                    Map<String, Object> brandMap = FastMap.newInstance();
                    brandMap.put("sequenceId", brand.getString("sequenceId"));
                    brandMap.put("productBrandId", brand.getString("productBrandId"));
                    brandMap.put("brandName", brand.getString("brandName"));
                    String contentId = UtilValidate.isEmpty(brand.getString("contentId"))?null:brand.getString("contentId");
                    brandMap.put("contentId", contentId);
                    brandList.add(brandMap);
                }

                List<Map> newBrandList = FastList.newInstance();
                for (int i =0 ;i<brandList.size();i++){
                    Map<String,Object> brand = brandList.get(i);
                    Map newBrandMap = FastMap.newInstance();
                    String sequenceId = brand.get("sequenceId").toString();
                    String productBrandId = brand.get("productBrandId").toString();
                    String brandName = brand.get("brandName").toString();
                    String contentId = UtilValidate.isEmpty(brand.get("contentId"))?null:brand.get("contentId").toString();
                    //根据contentId查询图片
                    String imgUrl = "";
                    if (UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }else {
                        newBrandMap.put("brandImg", null);
                    }
                    newBrandMap.put("sequenceId", sequenceId);
                    newBrandMap.put("productBrandId", productBrandId);
                    newBrandMap.put("brandName", brandName);
                    newBrandMap.put("brandImg", imgUrl);
                    newBrandList.add(newBrandMap);
                }
                resultData.put("brands", newBrandList);
                
            }

            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            
            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));

            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
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
//        if (appIndexCache != null) {
//            appIndexCache.put("hotBrand", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 首页-店铺推荐
     *
     * @return
     */
    @RequestMapping(value = "/storeReCommend", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> indexProductStore(HttpServletRequest request, @RequestBody Paginate paginate) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();
//        if (appIndexCache.get("storeReCommend") != null) {
//            resultData = appIndexCache.get("storeReCommend");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        String webSiteId = request.getHeader("client");
        
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 6;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 6;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PSRD", "ProductStoreRecommend");
            dynamicView.addAlias("PSRD", "recommendId");
            dynamicView.addAlias("PSRD", "productStoreId");
            dynamicView.addAlias("PSRD", "isAllWebSite");
            dynamicView.addAlias("PSRD", "status");
            dynamicView.addAlias("PSRD", "sequenceId");

            dynamicView.addMemberEntity("PSRWS", "ProductStoreRecommendWebSite");
            dynamicView.addAlias("PSRWS", "webSiteId");
            dynamicView.addViewLink("PSRD", "PSRWS", true, ModelKeyMap.makeKeyMapList("recommendId", "recommendId"));

            dynamicView.addMemberEntity("PG","PartyGroup");
            dynamicView.addAlias("PG","partyId");
            dynamicView.addViewLink("PSRD", "PG", true, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));

            dynamicView.addMemberEntity("PB","PartyBusiness");
            dynamicView.addAlias("PB","logoImg");
            dynamicView.addViewLink("PG", "PB", true, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

            dynamicView.addMemberEntity("PS", "ProductStore");
            dynamicView.addAlias("PS", "storeName");
            dynamicView.addViewLink("PSRD", "PS", true, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));
            
            List<String> fieldToSel = FastList.newInstance();
            fieldToSel.add("productStoreId");
            fieldToSel.add("storeName");
            fieldToSel.add("recommendId");
            fieldToSel.add("sequenceId");
            fieldToSel.add("isAllWebSite");
            fieldToSel.add("webSiteId");
            fieldToSel.add("status");
            fieldToSel.add("partyId");
            fieldToSel.add("logoImg");
            dynamicView.setGroupBy(fieldToSel);
            
            //排序字段名称
            String sortField = "sequenceId";
            orderBy.add(sortField);
            
            andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, "Y"));
            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            andExprs.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,EntityFindOptions.TYPE_SCROLL_INSENSITIVE,EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldToSel, orderBy, findOpts);
            
            // 查询出总记录数
            Integer size = eli.getResultsSizeAfterPartialList();
            
            List<GenericValue> productStores = eli.getPartialList(lowIndex, highIndex);
            List<Map<String, Object>> storeList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(productStores)) {
                for (GenericValue productStore : productStores) {
                    Map<String, Object> productStoreMap = FastMap.newInstance();
                    productStoreMap.put("storeId", productStore.get("productStoreId"));
                    productStoreMap.put("storeName", productStore.get("storeName"));
                    String contentId = (String) productStore.get("logoImg");
                    productStoreMap.put("contentId", contentId);
                    storeList.add(productStoreMap);
                }

                List<Map> newStoreList = FastList.newInstance();
                for (int s=0;s<storeList.size();s++){
                    Map<String,Object> store = storeList.get(s);
                    Map<String,Object> newStoreMap = FastMap.newInstance();
                    newStoreMap.put("storeId",store.get("storeId"));
                    newStoreMap.put("storeName",store.get("storeName"));
                    String contentId =UtilValidate.isEmpty(store.get("contentId"))?null: store.get("contentId").toString();
                    //根据contentId查询图片
                    String imgUrl = "";
                    if (UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                    newStoreMap.put("storeImgUrl",imgUrl);
                    newStoreList.add(newStoreMap);
                }
                resultData.put("stores", newStoreList);
            }
           
            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
            
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
//        if (appIndexCache != null) {
//            appIndexCache.put("storeReCommend", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 首页-商品推荐
     *
     * @return
     */
    @RequestMapping(value = "/productReCommend", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> indexGoodCommend(HttpServletRequest request, @RequestBody Paginate paginate) {
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        
//        if (appIndexCache.get("productReCommend") != null) {
//            resultData = appIndexCache.get("productReCommend");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        
        List<String> fieldsToSelect = FastList.newInstance();
        String webSiteId = request.getHeader("client");
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy = FastList.newInstance();
        
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 10;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PRD", "ProductRecommend");
            dynamicView.addAlias("PRD", "recommendId");
            dynamicView.addAlias("PRD", "productId");
            dynamicView.addAlias("PRD", "isAllWebSite");
            dynamicView.addAlias("PRD", "status");
            dynamicView.addAlias("PRD", "sequenceId");
            
            dynamicView.addMemberEntity("PRWS", "ProductRecommendWebSite");
            dynamicView.addAlias("PRWS", "webSiteId");
            dynamicView.addViewLink("PRD", "PRWS", true, ModelKeyMap.makeKeyMapList("recommendId", "recommendId"));

            dynamicView.addMemberEntity("P","Product");
            dynamicView.addAlias("P","isOnline");
            dynamicView.addViewLink("PRD", "P", true, ModelKeyMap.makeKeyMapList("productId", "productId"));
            fieldsToSelect.add("recommendId");
            fieldsToSelect.add("productId");
            fieldsToSelect.add("isAllWebSite");
            fieldsToSelect.add("webSiteId");
            fieldsToSelect.add("status");
            fieldsToSelect.add("sequenceId");
            fieldsToSelect.add("isOnline");
            dynamicView.setGroupBy(fieldsToSelect);
            //排序字段
            orderBy.add("sequenceId");
            
            andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, "Y"));
            andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));

            List<EntityCondition> bannerConditions2 = FastList.newInstance();
            bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            andExprs.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));
            
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            
            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 查询出的总记录数
            Integer size = eli.getResultsSizeAfterPartialList();
            
            resultData.put("highIndex", highIndex);
            resultData.put("lowIndex", lowIndex);
            resultData.put("size", size);
            List<GenericValue> productRecommends = eli.getPartialList(lowIndex, viewSize);
            
            StringBuffer productIds = new StringBuffer();
            if (UtilValidate.isNotEmpty(productRecommends)) {
                for (GenericValue recommend : productRecommends) {
                    productIds.append(recommend.getString("productId"));
                    productIds.append(",");
                }

            String prodStr = productIds.toString();
            if (prodStr.endsWith(",")) {
                prodStr = prodStr.substring(0, prodStr.length() - 1);
            }
            List productList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(prodStr)) {
                Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr));
                List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                if (UtilValidate.isNotEmpty(products)) {
                    for (int i = 0; i < products.size(); i++) {
                        Map<String, Object> productMap = FastMap.newInstance();
                        Map<String, Object> product = products.get(i);
                        productMap.put("productId", product.get("productId"));
                        productMap.put("productName", product.get("productName"));
                        //是否使用积分进行判断
                        if (UtilValidate.areEqual("2", product.get("integralDeductionType")) || UtilValidate.areEqual("3", product.get("integralDeductionType"))) {
                            //所需要的积分数
                            productMap.put("scoreValue", product.get("scoreValue"));
                            //差价
                            productMap.put("diffPrice", product.get("diffPrice"));
                        }
                        productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                        BigDecimal activityPrice = BigDecimal.ZERO;
                        BigDecimal amount = BigDecimal.ZERO;
                        BigDecimal price = ((BigDecimal) product.get("price")).setScale(2, BigDecimal.ROUND_HALF_UP);
                        if (UtilValidate.isEmpty(price)) {
                            price = BigDecimal.ZERO;
                        }
                        Map<String, Object> orderGroupInfos = (Map<String, Object>) product.get("orderGroupInfo");
                        Map<String, Object> secKillInfo = (Map<String, Object>) product.get("secKillInfo");
                        Map<String, Object> priceDownInfo = (Map<String, Object>) product.get("priceDownInfo");
                        if (UtilValidate.isNotEmpty(orderGroupInfos) && orderGroupInfos.size() > 0) {
                            activityPrice = ((BigDecimal) orderGroupInfos.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                            productMap.put("price", activityPrice);
                        } else if (UtilValidate.isNotEmpty(secKillInfo) && secKillInfo.size() > 0) {
                            activityPrice = ((BigDecimal) secKillInfo.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                            productMap.put("price", activityPrice);
                        } else if (UtilValidate.isNotEmpty(priceDownInfo) && priceDownInfo.size() > 0) {
                            amount = ((BigDecimal) priceDownInfo.get("amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
                            productMap.put("price", amount);
                        } else if (UtilValidate.isEmpty(orderGroupInfos) && UtilValidate.isEmpty(secKillInfo) && UtilValidate.isEmpty(priceDownInfo)) {
                            productMap.put("price", price);
                        }
                        productList.add(productMap);
                    }
                }
                //如果商品数大于4个取4
                if (productList.size()>4){
                   productList = productList.subList(0,4);
                }
                resultData.put("products", productList);
            }

        }

            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
            
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
//        if(appIndexCache!=null){
//            appIndexCache.put("productReCommend",resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 猜你喜欢
     *
     * @param request
     * @param paginate
     * @param token
     * @return
     */
    @RequestMapping(value = "/guess", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> indexGuess(HttpServletRequest request, @RequestBody Paginate paginate, JwtAuthenticationToken token) {
        Map<String, Object> resultData = FastMap.newInstance();
//        if (appIndexCache.get("guess") != null) {
//            resultData = appIndexCache.get("guess");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String webSiteId = request.getHeader("client");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //记录集合
        List<Map> recordsList = FastList.newInstance();
        
        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 10;

        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        List<EntityCondition> andExprs2 = FastList.newInstance();
        List<EntityCondition> andExprs3 = FastList.newInstance();
        EntityCondition mainCond = null;
        EntityCondition mainCond2 = null;
        EntityCondition mainCond3 = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        Set<String> tagList = FastSet.newInstance();
        List<String> browsHistoryIdList = FastList.newInstance();
        Set<String> pIds = FastSet.newInstance();
        try {

            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            beganTransaction = TransactionUtil.begin();

            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin",UtilMisc.toMap("userLoginId",loginName));
            String partyId = "";
            if (UtilValidate.isNotEmpty(userLogin)){
                partyId = userLogin.getString("partyId");
            }
            if (UtilValidate.isNotEmpty(partyId)){
                List<GenericValue> partyTags= delegator.findByAnd("PartyTag",UtilMisc.toMap("partyId",partyId));
                //后续处理 如果新增时 partyTagy一定有tagId 非认为删除 下面的判断就可删除
                if (UtilValidate.isNotEmpty(partyTags)){
                    for (int t=0;t<partyTags.size();t++){
                        String tagId = partyTags.get(t).getString("tagId");
                        if (UtilValidate.isNotEmpty(tagId)){
                            tagList.add(tagId);
                        }
                    }
                }
                //如果用户没有对应的标签则查找该用户的浏览记录
                if (UtilValidate.isEmpty(tagList)){
                    List<GenericValue> browseHistorys = delegator.findByAnd("PartyBrowseHistory",UtilMisc.toMap("partyId",partyId));
                    if (UtilValidate.isNotEmpty(browseHistorys)){
                        for (int b=0;b<browseHistorys.size();b++){
                            String productId =browseHistorys.get(b).getString("productId");
                            if (UtilValidate.isNotEmpty(productId)){
                                browsHistoryIdList.add(productId);
                            }

                        }
                    }
                }
            }

            andExprs2.add(EntityCondition.makeCondition("userLoginId", EntityOperator.EQUALS, loginName));
            andExprs2.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
            andExprs2.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));

            //添加where条件
            if (andExprs2.size() > 0) {
                mainCond2 = EntityCondition.makeCondition(andExprs2, EntityOperator.AND);
            }

            andExprs3.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
            andExprs3.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));

            //添加where条件
            if (andExprs3.size() > 0) {
                mainCond3 = EntityCondition.makeCondition(andExprs3, EntityOperator.AND);
            }

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 40, true);

            //排序字段名称
            String sortField = "-createdStamp";
            orderBy.add(sortField);

            List<Map> newProductList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(tagList)){
                andExprs.add(EntityCondition.makeCondition("tagTypeId", EntityOperator.IN, UtilMisc.toList("ProdutTypeTag_1","ProdutTypeTag","ProdutTypeTag_2")));
                andExprs.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, partyId));
                andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));
                andExprs.add(EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));

                //添加where条件
                if (andExprs.size() > 0) {
                    mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                }

                //查出标签下的商品
                List<GenericValue> newGvs = delegator.findList("ProductTagAndUserLogin",mainCond,null,orderBy,findOpts,false);

                StringBuffer productIds1 = new StringBuffer();
                // 获取分页所需的记录集合
                for (GenericValue gv : newGvs) {
                    String productId = gv.getString("productId");
                    if (UtilValidate.isNotEmpty(productId)){
                        pIds.add(productId);
                    }
                }

                List setToList = new ArrayList();
                setToList.clear();
                setToList.addAll(pIds);

                if (pIds.size()>40){
                    setToList = setToList.subList(0,40);

                }else {
                    //pIds 小于40 从浏览记录里查出
                    List<GenericValue> newPartyHistoryList = delegator.findList("PartyBrowseHistoryAndTagAndProduct",mainCond2,null,orderBy,findOpts,false);
                    if (UtilValidate.isNotEmpty(newPartyHistoryList)){
                        for (GenericValue gv : newPartyHistoryList){
                            String historyProductId = gv.getString("productId");
                            if (UtilValidate.isNotEmpty(historyProductId)){
                                pIds.add(historyProductId);
                            }
                        }
                    }
                    setToList.clear();
                    setToList.addAll(pIds);
                    if (pIds.size()>40){
                        setToList = setToList.subList(0,40);
                    }else {
                        //直接查看商品
                        List<GenericValue> productAndProductStore = delegator.findList("ProductAndProductStoreProduct",mainCond3,null,orderBy,findOpts,false);
                        if (UtilValidate.isNotEmpty(productAndProductStore)){
                            for (GenericValue gv : productAndProductStore){
                                String proProductId = gv.getString("productId");
                                if (UtilValidate.isNotEmpty(proProductId)){
                                    pIds.add(proProductId);
                                }
                            }
                        }
                        setToList.clear();
                        setToList.addAll(pIds);
                        if (pIds.size()>40){
                            setToList = setToList.subList(0,40);
                        }
                    }

                }

                Set<String> newPIds = new HashSet<>(setToList);

                for (String pId : newPIds){
                    productIds1.append(pId);
                    productIds1.append(",");
                }

                String prodStr1 = productIds1.toString();
                if (prodStr1.endsWith(",")) {
                    prodStr1 = prodStr1.substring(0, prodStr1.length() - 1);
                }

                    //根据标签来查询 商品
                    List<Map> productList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(prodStr1)) {
                        Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", prodStr1));
                        List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                        if (UtilValidate.isNotEmpty(products)) {
                            for (int k = 0; k < products.size(); k++) {
                                Map<String, Object> product = products.get(k);
                                Map<String, Object> productMap = FastMap.newInstance();
                                productMap.put("productId", product.get("productId"));
                                productMap.put("productName", product.get("productName"));
                                //是否使用积分进行判断
                                if (UtilValidate.areEqual("2", product.get("integralDeductionType")) || UtilValidate.areEqual("3", product.get("integralDeductionType"))) {
                                    //所需要的积分数
                                    productMap.put("scoreValue", product.get("scoreValue"));
                                    BigDecimal diffPrice = (BigDecimal) product.get("diffPrice");
                                    if (UtilValidate.isEmpty(diffPrice)) {
                                        diffPrice = BigDecimal.ZERO;
                                    }
                                    //差价
                                    productMap.put("diffPrice", diffPrice);
                                }
                                BigDecimal price = ((BigDecimal) product.get("price")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                if (UtilValidate.isEmpty(price)) {
                                    price = BigDecimal.ZERO;
                                }
                                productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                BigDecimal activityPrice = BigDecimal.ZERO;
                                BigDecimal amount = BigDecimal.ZERO;
                                Map<String, Object> orderGroupInfos = (Map<String, Object>) product.get("orderGroupInfo");
                                Map<String, Object> secKillInfo = (Map<String, Object>) product.get("secKillInfo");
                                Map<String, Object> priceDownInfo = (Map<String, Object>) product.get("priceDownInfo");
                                if (UtilValidate.isNotEmpty(orderGroupInfos) && orderGroupInfos.size() > 0) {
                                    activityPrice = ((BigDecimal) orderGroupInfos.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", activityPrice);
                                } else if (UtilValidate.isNotEmpty(secKillInfo) && secKillInfo.size() > 0) {
                                    activityPrice = ((BigDecimal) secKillInfo.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", activityPrice);
                                } else if (UtilValidate.isNotEmpty(priceDownInfo) && priceDownInfo.size() > 0) {
                                    amount = ((BigDecimal) priceDownInfo.get("amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", amount);
                                } else if (UtilValidate.isEmpty(orderGroupInfos) && UtilValidate.isEmpty(secKillInfo) && UtilValidate.isEmpty(priceDownInfo)) {
                                    productMap.put("price", price);
                                }
                                productList.add(productMap);

                            }
                        }
                    }

                    for (int p=0;p<productList.size();p++){
                        Map<String,Object> product = productList.get(p);
                        Map<String,Object> newProductMap = FastMap.newInstance();
                        newProductMap.put("productId",product.get("productId"));
                        newProductMap.put("productName",product.get("productName"));
                        newProductMap.put("mediumImageUrl",product.get("mediumImageUrl"));
                        newProductMap.put("price",product.get("price"));
                        String diffPrice = UtilValidate.isEmpty(product.get("diffPrice"))?null:product.get("diffPrice").toString();
                        String scoreValue = UtilValidate.isEmpty(product.get("scoreValue"))?null:product.get("scoreValue").toString();
                        if (UtilValidate.isNotEmpty(diffPrice)){
                            newProductMap.put("diffPrice",diffPrice);
                        }
                        if (UtilValidate.isNotEmpty(scoreValue)){
                            newProductMap.put("scoreValue",scoreValue);
                        }
                        if (UtilValidate.isNotEmpty(newProductMap)){
                            newProductList.add(newProductMap);
                        }
                    }

                    size = newProductList.size();
                    if (page == 0){
                        newProductList = newProductList.subList(0,10);
                    }else if (page == 1){
                        newProductList = newProductList.subList(10,20);
                    }else if (page == 2){
                        newProductList = newProductList.subList(20,30);
                    }else if (page ==3){
                        newProductList = newProductList.subList(30,40);
                    }

                resultData.put("products", newProductList);

        }

            if (UtilValidate.isEmpty(tagList)&&UtilValidate.isNotEmpty(browsHistoryIdList)){

                //查找浏览记录
                List<GenericValue> partyHistoryList = delegator.findList("PartyBrowseHistoryAndTagAndProduct",mainCond2,null,orderBy,findOpts,false);
                StringBuffer historyProductIds = new StringBuffer();
                Set<String> historyPIds = FastSet.newInstance();
                // 获取分页所需的记录集合
                if (UtilValidate.isNotEmpty(partyHistoryList)){
                    for (GenericValue gv : partyHistoryList) {
                        String hisProductId1 = gv.getString("productId");
                        if (UtilValidate.isNotEmpty(hisProductId1)){
                            historyPIds.add(hisProductId1);
                        }
                    }
                }

                List setToList = new ArrayList();
                setToList.clear();
                setToList.addAll(historyPIds);

                if (historyPIds.size()>40){
                    setToList = setToList.subList(0,40);
                }else {
                    //查找商品
                    List<GenericValue> gvs = delegator.findList("ProductAndProductStoreProduct",mainCond3,null,orderBy,findOpts,false);

                    // 获取分页所需的记录集合
                    for (GenericValue gv : gvs) {
                        String hisProductId2 = gv.getString("productId");
                        if (UtilValidate.isNotEmpty(hisProductId2)){
                            historyPIds.add(hisProductId2);
                        }
                    }
                    setToList.clear();
                    setToList.addAll(historyPIds);

                    if (historyPIds.size()>40){
                        setToList = setToList.subList(0,40);
                    }

                }

                Set<String> newHistoryPIds = new HashSet(setToList);

                for (String pId : newHistoryPIds){
                    historyProductIds.append(pId);
                    historyProductIds.append(",");
                }

                String hisProdStr1 = historyProductIds.toString();
                if (hisProdStr1.endsWith(",")) {
                    hisProdStr1 = hisProdStr1.substring(0, hisProdStr1.length() - 1);
                }

                List<Map> productList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(hisProdStr1)) {
                    Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", hisProdStr1));
                    List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                    if (UtilValidate.isNotEmpty(products)) {
                        for (int k = 0; k < products.size(); k++) {
                            Map<String, Object> product = products.get(k);
                            Map<String, Object> productMap = FastMap.newInstance();
                            productMap.put("productId", product.get("productId"));
                            productMap.put("productName", product.get("productName"));
                            //是否使用积分进行判断
                            if (UtilValidate.areEqual("2", product.get("integralDeductionType")) || UtilValidate.areEqual("3", product.get("integralDeductionType"))) {
                                //所需要的积分数
                                productMap.put("scoreValue", product.get("scoreValue"));
                                BigDecimal diffPrice = (BigDecimal) product.get("diffPrice");
                                if (UtilValidate.isEmpty(diffPrice)) {
                                    diffPrice = BigDecimal.ZERO;
                                }
                                //差价
                                productMap.put("diffPrice", diffPrice);
                            }
                            BigDecimal price = ((BigDecimal) product.get("price")).setScale(2, BigDecimal.ROUND_HALF_UP);
                            if (UtilValidate.isEmpty(price)) {
                                price = BigDecimal.ZERO;
                            }
                            BigDecimal activityPrice = BigDecimal.ZERO;
                            BigDecimal amount = BigDecimal.ZERO;
                            Map<String, Object> orderGroupInfos = (Map<String, Object>) product.get("orderGroupInfo");
                            Map<String, Object> secKillInfo = (Map<String, Object>) product.get("secKillInfo");
                            Map<String, Object> priceDownInfo = (Map<String, Object>) product.get("priceDownInfo");
                            if (UtilValidate.isNotEmpty(orderGroupInfos) && orderGroupInfos.size() > 0) {
                                activityPrice = ((BigDecimal) orderGroupInfos.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                productMap.put("price", activityPrice);
                            } else if (UtilValidate.isNotEmpty(secKillInfo) && secKillInfo.size() > 0) {
                                activityPrice = ((BigDecimal) secKillInfo.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                productMap.put("price", activityPrice);
                            } else if (UtilValidate.isNotEmpty(priceDownInfo) && priceDownInfo.size() > 0) {
                                amount = ((BigDecimal) priceDownInfo.get("amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                productMap.put("price", amount);
                            } else if (UtilValidate.isEmpty(orderGroupInfos) && UtilValidate.isEmpty(secKillInfo) && UtilValidate.isEmpty(priceDownInfo)) {

                                productMap.put("price", price);
                            }
                            productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                            productList.add(productMap);
                        }
                    }
                }

                for (int p=0;p<productList.size();p++){
                    Map<String,Object> product = productList.get(p);
                    Map<String,Object> newProductMap = FastMap.newInstance();
                    newProductMap.put("productId",product.get("productId"));
                    newProductMap.put("productName",product.get("productName"));
                    newProductMap.put("mediumImageUrl",product.get("mediumImageUrl"));
                    newProductMap.put("price",product.get("price"));
                    String diffPrice = UtilValidate.isEmpty(product.get("diffPrice"))?null:product.get("diffPrice").toString();
                    String scoreValue = UtilValidate.isEmpty(product.get("scoreValue"))?null:product.get("scoreValue").toString();
                    if (UtilValidate.isNotEmpty(diffPrice)){
                        newProductMap.put("diffPrice",diffPrice);
                    }
                    if (UtilValidate.isNotEmpty(scoreValue)){
                        newProductMap.put("scoreValue",scoreValue);
                    }
                    if (UtilValidate.isNotEmpty(newProductMap)){
                        newProductList.add(newProductMap);
                    }
                }

                size = newProductList.size();
                if (page == 0){
                    newProductList = newProductList.subList(0,10);
                }else if (page ==1){
                    newProductList = newProductList.subList(10,20);
                }else if (page == 2){
                    newProductList = newProductList.subList(20,30);
                }else if (page == 3){
                    newProductList = newProductList.subList(30,40);
                }

                resultData.put("products", newProductList);

            }

            if (UtilValidate.isEmpty(tagList)&&UtilValidate.isEmpty(browsHistoryIdList)){
                //查询商品
                List<GenericValue> proProduct = delegator.findList("ProductAndProductStoreProduct",mainCond3,null,orderBy,findOpts,false);
                StringBuffer proProductIds = new StringBuffer();
                Set<String> proPIds = FastSet.newInstance();
                // 获取分页所需的记录集合
                for (GenericValue gv : proProduct) {
                    String proProductId = gv.getString("productId");
                    if (UtilValidate.isNotEmpty(proProductId)){
                        proPIds.add(proProductId);
                    }
                }

                List setToList = new ArrayList();
                setToList.clear();
                setToList.addAll(proPIds);

                if (proPIds.size()>40){
                    setToList = setToList.subList(0,40);
                }

                Set<String> newProPIds = new HashSet<>(setToList);

                for (String pId : newProPIds){
                    proProductIds.append(pId);
                    proProductIds.append(",");
                }

                String proProdStr = proProductIds.toString();
                if (proProdStr.endsWith(",")) {
                    proProdStr = proProdStr.substring(0, proProdStr.length() - 1);
                }
                List<Map> proProductList = FastList.newInstance();
                if (UtilValidate.isNotEmpty(proProdStr)) {
                    Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", proProdStr));
                    List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                    if (UtilValidate.isNotEmpty(products)) {
                        for (int k = 0; k < products.size(); k++) {
                            Map<String, Object> product = products.get(k);
                            Map<String, Object> productMap = FastMap.newInstance();
                                productMap.put("productId", product.get("productId"));
                                productMap.put("productName", product.get("productName"));
                                //是否使用积分进行判断
                                if (UtilValidate.areEqual("2", product.get("integralDeductionType")) || UtilValidate.areEqual("3", product.get("integralDeductionType"))) {
                                    //所需要的积分数
                                    productMap.put("scoreValue", product.get("scoreValue"));
                                    BigDecimal diffPrice = (BigDecimal) product.get("diffPrice");
                                    if (UtilValidate.isEmpty(diffPrice)) {
                                        diffPrice = BigDecimal.ZERO;
                                    }
                                    //差价
                                    productMap.put("diffPrice", diffPrice);
                                }
                                BigDecimal price = ((BigDecimal) product.get("price")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                if (UtilValidate.isEmpty(price)) {
                                    price = BigDecimal.ZERO;
                                }
                                BigDecimal activityPrice = BigDecimal.ZERO;
                                BigDecimal amount = BigDecimal.ZERO;
                                Map<String, Object> orderGroupInfos = (Map<String, Object>) product.get("orderGroupInfo");
                                Map<String, Object> secKillInfo = (Map<String, Object>) product.get("secKillInfo");
                                Map<String, Object> priceDownInfo = (Map<String, Object>) product.get("priceDownInfo");
                                if (UtilValidate.isNotEmpty(orderGroupInfos) && orderGroupInfos.size() > 0) {
                                    activityPrice = ((BigDecimal) orderGroupInfos.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", activityPrice);
                                } else if (UtilValidate.isNotEmpty(secKillInfo) && secKillInfo.size() > 0) {
                                    activityPrice = ((BigDecimal) secKillInfo.get("orderPrice")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", activityPrice);
                                } else if (UtilValidate.isNotEmpty(priceDownInfo) && priceDownInfo.size() > 0) {
                                    amount = ((BigDecimal) priceDownInfo.get("amount")).setScale(2, BigDecimal.ROUND_HALF_UP);
                                    productMap.put("price", amount);
                                } else if (UtilValidate.isEmpty(orderGroupInfos) && UtilValidate.isEmpty(secKillInfo) && UtilValidate.isEmpty(priceDownInfo)) {
                                    productMap.put("price", price);
                                }
                                productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                                proProductList.add(productMap);

                        }

                    }

                }
                for (int p=0;p<proProductList.size();p++){
                    Map<String,Object> product = proProductList.get(p);
                    Map<String,Object> newProductMap = FastMap.newInstance();
                    newProductMap.put("productId",product.get("productId"));
                    newProductMap.put("productName",product.get("productName"));
                    newProductMap.put("mediumImageUrl",product.get("mediumImageUrl"));
                    newProductMap.put("price",product.get("price"));
                    String diffPrice = UtilValidate.isEmpty(product.get("diffPrice"))?null:product.get("diffPrice").toString();
                    String scoreValue = UtilValidate.isEmpty(product.get("scoreValue"))?null:product.get("scoreValue").toString();
                    if (UtilValidate.isNotEmpty(diffPrice)){
                        newProductMap.put("diffPrice",diffPrice);
                    }
                    if (UtilValidate.isNotEmpty(scoreValue)){
                        newProductMap.put("scoreValue",scoreValue);
                    }
                    if (UtilValidate.isNotEmpty(newProductMap)){
                        newProductList.add(newProductMap);
                    }
                }

                size = newProductList.size();
                if (page ==0){
                    newProductList = newProductList.subList(0,10);
                }else if (page ==1){
                    newProductList = newProductList.subList(10,20);
                }else if (page ==2){
                    newProductList = newProductList.subList(20,30);
                }else if (page ==3){
                    newProductList = newProductList.subList(30,40);
                }

                resultData.put("products", newProductList);

            }
            //总记录数
            resultData.put("totalSize", size);

            boolean hasNext = true;
            boolean hasPrev = true;
            
            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }
            
            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
        }catch (Exception e) {
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
        //返回的参数

        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));
//        if (appIndexCache != null) {
//            appIndexCache.put("guess", resultData);
//        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    @RequestMapping(value = "/getIndexTemplate", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getIndexTemplate(HttpServletRequest request) throws GenericEntityException {
        Map<String, Object> resultData = FastMap.newInstance();
        if (appIndexCache.get("getIndexTemplate") != null) {
            resultData = appIndexCache.get("getIndexTemplate");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String webSiteId = request.getHeader("client");
        
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        
        List<GenericValue> indexTemplateList = delegator.findByAnd("IndexTemplate", UtilMisc.toMap("isUsed", "Y"));
        if (indexTemplateList == null || indexTemplateList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "找不到首页模板");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        
        GenericValue indexTemplate = indexTemplateList.get(0);
        String templateId = indexTemplate.getString("templateId");
        
        List<String> sort = FastList.newInstance();
        sort.add("sequenceNum");
        List<GenericValue> templateContents = delegator.findByAnd("IndexTemplateContent", UtilMisc.toMap("templateId", templateId), sort);
        
        resultData.put("retCode", 1);
        resultData.put("templateContents", templateContents);
        if(appIndexCache!=null){
            appIndexCache.put("getIndexTemplate",resultData);
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    /**
     * 首页活动
     *
     * @param request
     * @return
     * @throws GenericEntityException
     */
    @RequestMapping(value = "/getIndexActivity", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getIndexActivity(HttpServletRequest request) throws GeneralException, IOException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (appIndexCache.get("getIndexActivity") != null) {
            resultData = appIndexCache.get("getIndexActivity");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {

            beganTransaction = TransactionUtil.begin();

            //获取最新的团购信息
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("TG", "TogetherGroup");
            dynamicView.addAlias("TG", "togetherId");
            dynamicView.addAlias("TG", "createUserId");
            dynamicView.addAlias("TG", "productId");
            dynamicView.addAlias("TG", "status");
            dynamicView.addAlias("TG", "currentNum");
            dynamicView.addAlias("TG", "activityId");
            dynamicView.addAlias("TG", "createDate");
            dynamicView.addAlias("TG", "limitUserNum");

            dynamicView.addMemberEntity("P", "Product");
            dynamicView.addAlias("P", "productId");
            dynamicView.addAlias("P", "salesDiscontinuationDate");

            dynamicView.addViewLink("TG", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId"));

            EntityCondition mainCond1 = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null),
                            EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN, new Timestamp(System.currentTimeMillis()))
                    )
                    , EntityOperator.OR);
            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            mainCond1,
                            EntityCondition.makeCondition("status", EntityOperator.EQUALS, "TOGETHER_RUNING"),
                            EntityCondition.makeCondition("currentNum", EntityOperator.LESS_THAN, EntityFieldValue.makeFieldValue("limitUserNum"))
                    )
                    , EntityOperator.AND);

            List<String> orderBy = FastList.newInstance();
            orderBy.add("-createDate");

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 1, false);

              eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, orderBy, findOpts);

            List<GenericValue> togetherGroupList = eli.getPartialList(1, 1);
            int size = eli.getResultsSizeAfterPartialList();

            if (size == 0) {
                resultData.put("togetherGroup", "");
            } else {
                GenericValue togetherGroup = togetherGroupList.get(0);
                Map togetherGroupMap = togetherGroup.toMap();
                togetherGroupMap.put("createDate", CommonUtils.getStringDate((Timestamp) togetherGroupMap.get("createDate")));
                String productId = (String) togetherGroupMap.get("productId");
                String partyId = (String) togetherGroupMap.get("createUserId");
                //获取用户昵称和头像
                Map userInfo = CommonUtils.getUserInfo(delegator, partyId);
                togetherGroupMap.put("nickname", userInfo.get("nickname"));
                togetherGroupMap.put("headphoto", userInfo.get("headphoto"));

                List<String> productIds = FastList.newInstance();
                productIds.add(productId);
                togetherGroupMap.put("product", ProductUtils.getProductsWithOutFeature(productIds, delegator, dispatcher, baseImgUrl).get(0));
                resultData.put("togetherGroup", togetherGroupMap);
            }

            //获取最新购买的一个订单
            dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("ORE", "OrderRole");
            dynamicView.addAlias("ORE", "partyId");
            dynamicView.addAlias("ORE", "orderId");
            dynamicView.addAlias("ORE", "createdStamp");
            dynamicView.addAlias("ORE", "roleTypeId");

            dynamicView.addMemberEntity("OPP", "OrderPaymentPreference");
            dynamicView.addAlias("OPP", "orderId");
            dynamicView.addAlias("OPP", "statusId");

            dynamicView.addViewLink("ORE", "OPP", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId"));
            orderBy = FastList.newInstance();
            orderBy.add("-createdStamp");

            List<String> fieldsToSelect = FastList.newInstance();
            fieldsToSelect.add("partyId");
            fieldsToSelect.add("orderId");
            fieldsToSelect.add("createdStamp");

            mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("roleTypeId", EntityOperator.EQUALS, "PLACING_CUSTOMER"),
                            EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PAYMENT_RECEIVED")
                    )
                    , EntityOperator.AND);

            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, null);
            List<GenericValue> orderRoleList = eli.getPartialList(1, 1);
            size = eli.getResultsSizeAfterPartialList();

            if (size == 0) {
                resultData.put("order", "");
            } else {
                GenericValue orderRole = orderRoleList.get(0);
                String orderId = orderRole.getString("orderId");
                String partyId = orderRole.getString("partyId");
                GenericValue orderItem = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId)).get(0);
                String productId = orderItem.getString("productId");
                Map promoInfo = dispatcher.runSync("getProductPromoInfoByProductId",UtilMisc.toMap("productId",productId));
                Map order = FastMap.newInstance();
                List<String> productIds = FastList.newInstance();
                productIds.add(productId);
                order.put("product", ProductUtils.getProductsWithOutFeature(productIds, delegator, dispatcher, baseImgUrl).get(0));
                order.put("promoInfo", promoInfo);
                Map userInfo = CommonUtils.getUserInfo(delegator, partyId);
                order.put("nickname", userInfo.get("nickname"));
                order.put("headphoto", userInfo.get("headphoto"));
                order.put("createDate", CommonUtils.getStringDate(orderRole.getTimestamp("createdStamp")));

                resultData.put("order", order);
            }

            //查询最近的拆红包
            dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("URPD", "UserRedPackageDetail");
            dynamicView.addAlias("URPD", "partyId");
            dynamicView.addAlias("URPD", "couponCodeId");
            dynamicView.addAlias("URPD", "status");
            dynamicView.addAlias("URPD", "createdStamp");

            orderBy = FastList.newInstance();
            orderBy.add("-createdStamp");
            fieldsToSelect = FastList.newInstance();
            fieldsToSelect.add("partyId");
            fieldsToSelect.add("couponCodeId");
            fieldsToSelect.add("createdStamp");

            mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("status", EntityOperator.EQUALS, "S")
                    )
                    , EntityOperator.AND);

            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, null);
            List<GenericValue> redpackageDetails = eli.getPartialList(1, 1);


            if (redpackageDetails == null || redpackageDetails.size() == 0) {
                resultData.put("redpackage", "");
            } else {
                GenericValue redpackage = redpackageDetails.get(0);
                String partyId = redpackage.getString("partyId");
                String couponCodeId = redpackage.getString("couponCodeId");

                List<GenericValue> productPromoCodeList = delegator.findByAnd("ProductPromoCodeParty", UtilMisc.toMap("productPromoCodeId", couponCodeId));
                if (UtilValidate.isEmpty(productPromoCodeList)) {
                    resultData.put("redpackage", "");
                } else {
                    GenericValue productPromoCodeParty = productPromoCodeList.get(0);
                    GenericValue promoCode = productPromoCodeParty.getRelatedOne("ProductPromoCode");
                    GenericValue coupon = promoCode.getRelatedOne("ProductPromoCoupon");

                    Map couponInfo = FastMap.newInstance();
                    couponInfo.put("productPromoCodeId", productPromoCodeParty.getString("productPromoCodeId"));
                    couponInfo.put("couponName", coupon.getString("couponName"));
                    couponInfo.put("couponType", coupon.getString("couponType"));
                    couponInfo.put("payFill", coupon.getString("payFill"));
                    couponInfo.put("payReduce", coupon.getString("payReduce"));
                    couponInfo.put("applyScope", coupon.getString("applyScope"));
                    couponInfo.put("couponProductType", coupon.getString("couponProductType"));

                    Map map = FastMap.newInstance();
                    map.put("coupon", couponInfo);
                    Map userInfo = CommonUtils.getUserInfo(delegator, partyId);
                    map.put("nickname", userInfo.get("nickname"));
                    map.put("headphoto", userInfo.get("headphoto"));
                    map.put("createDate", CommonUtils.getStringDate(redpackage.getTimestamp("createdStamp")));
                    resultData.put("redpackage", map);
                }


            }

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
        if(appIndexCache!=null){
            appIndexCache.put("getIndexActivity",resultData);
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
}
