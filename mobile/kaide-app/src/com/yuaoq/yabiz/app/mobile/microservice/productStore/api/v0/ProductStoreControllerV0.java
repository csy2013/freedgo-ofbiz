package com.yuaoq.yabiz.app.mobile.microservice.productStore.api.v0;

import com.yuaoq.yabiz.mobile.common.Paginate;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
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
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.*;
/**
 * Created by changsy on 2018/3/31.
 */
@RestController
@RequestMapping(path = "/api/productStore/v0")
public class ProductStoreControllerV0 {

    @Value("${image.base.url}")
    String baseImgUrl;
    public static final String module = ProductStoreControllerV0.class.getName();

    /**
     * 获取首页店铺一级分类
     *
     * @param request
     * @param paginate
     * @return
     */
    @RequestMapping(value = "/category/classify", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> productCategories(HttpServletRequest request, @RequestBody Paginate paginate) {
        int page = paginate.getPage();
        int limit = paginate.getPageSize();
        Map resultData = FastMap.newInstance();
        Map<String, Object> retMap = ServiceUtil.returnSuccess();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        String orderFiled = request.getParameter("ORDER_FILED");
        String orderFiledBy = request.getParameter("ORDER_BY");
        String productStoreId = request.getParameter("productStoreId");
        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", page);
        
        int viewSize = 20;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 20;
        }
        
        
        boolean hasNext = true;
        boolean hasPrev = true;
        int next = 1;
       
        int perPage = 10;
        int prev = 1;
        int pages = 1;
        int total = 1;
        
        page = viewIndex;
        perPage = viewSize;
        next = viewIndex + 1;
        if (viewIndex == 0) {
            prev = 0;
        } else {
            prev = viewIndex - 1;
        }
        
        resultData.put("viewSize", Integer.valueOf(viewSize));
        List<String> orderBy = FastList.newInstance();
        if (UtilValidate.isNotEmpty(orderFiled)) {
            orderBy.add(orderFiled + " " + orderFiledBy);
        } else {
            orderBy.add("sequenceNum ASC ");
        }
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("categoryName");
        fieldsToSelect.add("description");
        fieldsToSelect.add("productCategoryId");
        fieldsToSelect.add("productCategoryTypeId");
        fieldsToSelect.add("sequenceNum");
        fieldsToSelect.add("primaryParentCategoryId");
        List<GenericValue> categoryData = FastList.newInstance();
        boolean beganTransaction = false;
        //根据catalog获取对应的分类根
        try {
            // get the indexes for the partial list
            // get the indexes for the partial list
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            beganTransaction = TransactionUtil.begin();
            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            EntityCondition mainCond = null;
            EntityListIterator pli = delegator.find("ProductCategory", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
            // get the partial list for this page
            categoryData = pli.getPartialList(lowIndex, viewSize);
            
            // attempt to get the full size
            listSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > listSize) {
                highIndex = listSize;
                hasNext = false;
            }
            // close the list iterator
            pli.close();
            total = listSize;
            pages = total % viewSize == 0 ? total / viewSize : total / viewSize + 1;
            
            if (lowIndex == 1) {
                hasPrev = false;
            }
            
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            resultData.put("retCode","0");
            resultData.put("message",errMsg);
            try {
                TransactionUtil.rollback();
            } catch (GenericTransactionException e1) {
                e = e1 ;
            }
            
        }finally {
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (GenericTransactionException e) {
                Debug.logError(e, "Unable to commit transaction");
            }
        }
        Map<String, Object> pMap = FastMap.newInstance();
        pMap.put("hasNext", hasNext);
        pMap.put("hasPrev", hasPrev);
        pMap.put("next", next);
        pMap.put("page", page);
        pMap.put("pages", pages);
        pMap.put("perPage", perPage);
        pMap.put("prev", prev);
        pMap.put("total", total);
        resultData.put("paginate", pMap);
        resultData.put("categories", categoryData);
        
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


    /**
     * 店铺推荐 列表展示
     *
     * @param request
     * @param page
     * @param pageSize
     * @param productCategoryId
     * @return
     */
    @RequestMapping(value = "/storeReCommendList", method = RequestMethod.POST,produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> storeReCommendList( HttpServletRequest request,@RequestParam (defaultValue = "0") Integer page,@RequestParam (defaultValue = "10") Integer pageSize,@RequestParam (defaultValue = "") String productCategoryId) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        int limit = pageSize;
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
        boolean beganTransaction =false;
        EntityListIterator eli = null;
        try {

            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            List<String> partyIds = FastList.newInstance();
            if (UtilValidate.isNotEmpty(productCategoryId)){
                List<GenericValue> partyCategorys = delegator.findByAnd("PartyProductCategory",UtilMisc.toMap("productCategoryId",productCategoryId));//Party_Product_Category
                if (UtilValidate.isNotEmpty(partyCategorys)){
                    for (int i=0;i<partyCategorys.size();i++){
                        String partyId = partyCategorys.get(i).getString("partyId");
                        partyIds.add(partyId);
                    }
                }
            }

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
            dynamicView.addAlias("PS", "contentId");
            dynamicView.addAliasAll("PS", "", null);
            dynamicView.addViewLink("PSRD", "PS", true, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));

            List<String> fieldToSel = FastList.newInstance();

            fieldToSel.add("productStoreId");
            fieldToSel.add("storeName");
            fieldToSel.add("contentId");
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

            if (UtilValidate.isNotEmpty(productCategoryId)){
                andExprs.add(EntityCondition.makeCondition("partyId",EntityOperator.IN, partyIds));
            }else {
                bannerConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
                bannerConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));
            }
            andExprs.add(EntityCondition.makeCondition(bannerConditions2, EntityOperator.OR));

            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }

            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);

            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldToSel, orderBy, findOpts);

            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();

            List<GenericValue> productBrands = eli.getPartialList(lowIndex,highIndex);
            List<Map<String,Object>> storeList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(productBrands)) {
                for (GenericValue productBrand : productBrands) {
                    Map<String, Object> brandMap = FastMap.newInstance();
                    brandMap.put("productStoreId", productBrand.get("productStoreId"));
                    brandMap.put("storeName", productBrand.get("storeName"));
                    String contentId = (String) productBrand.get("logoImg");
                    //根据contentId查询图片
                    String imgUrl ="";
                    if(UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl+ ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                    brandMap.put("storeImgUrl",imgUrl);

                    storeList.add(brandMap);
                }
                resultData.put("stores",storeList);

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

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_FOUND));
    }

    /**
     * 获取店铺一级分类
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getStoreCategories", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getStoreCategories(HttpServletRequest request ) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String webSiteId = request.getHeader("client");
        Map<String, Object> resultData = FastMap.newInstance();
        List<String> orderBy = FastList.newInstance();
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        int lowIndex = 0;
        int highIndex = 0;
        int viewIndex = 0;
        resultData.put("viewIndex", Integer.valueOf(viewIndex));

        int viewSize = 10;

        resultData.put("viewSize", Integer.valueOf(viewSize));
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }

        boolean beganTransaction =false;
        EntityListIterator eli = null;
        try {
            beganTransaction = TransactionUtil.begin();
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            DynamicViewEntity dynamicView = new DynamicViewEntity();//product_category

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
            dynamicView.addAlias("PS", "contentId");
            dynamicView.addAliasAll("PS", "", null);
            dynamicView.addViewLink("PSRD", "PS", true, ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));

            dynamicView.addMemberEntity("PPC", "PartyProductCategory");
            dynamicView.addAlias("PPC",  "productCategoryId");
            dynamicView.addAlias("PPC",  "parentCategoryId");//PARENT_CATEGORY_ID
            dynamicView.addAlias("PPC","categoryName");
            dynamicView.addAlias("PPC","createdStamp");//CREATED_STAMP
            dynamicView.addAlias("PPC","partyId");

            dynamicView.addViewLink("PB", "PPC", true, ModelKeyMap.makeKeyMapList("partyId", "partyId"));

            List<String> fieldToSel = FastList.newInstance();
            fieldToSel.add("productCategoryId");
            fieldToSel.add("parentCategoryId");
            fieldToSel.add("categoryName");
            fieldToSel.add("createdStamp");
            fieldToSel.add("status");
            dynamicView.setGroupBy(fieldToSel);
            orderBy.add("-createdStamp");

            List<EntityCondition> bannerConditions2 = FastList.newInstance();

            andExprs.add(EntityCondition.makeCondition("parentCategoryId", EntityOperator.EQUALS, null));
            andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, "Y"));

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
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldToSel, orderBy, findOpts);
            List<GenericValue> productCategorys = eli.getPartialList(lowIndex, viewSize);
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();

            List<Map<String, String>> productCategoryList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(productCategorys)) {
                for (GenericValue productCategory : productCategorys) {
                    Map<String, String> brandMap = FastMap.newInstance();
                    String categoryName = productCategory.getString("categoryName");
                    String productCategoryId = productCategory.getString("productCategoryId");
                    brandMap.put("tagId",productCategoryId);
                    brandMap.put("name",categoryName);
                    productCategoryList.add(brandMap);
                }

            }

            Map<String, Map> msp = new HashMap<String, Map>();
            List<Map<String, String>> listMap = new ArrayList<Map<String,String>>();
            for (int i = productCategoryList.size()-1;i>=0;i--){
                Map map = productCategoryList.get(i);
                String tagId = map.get("tagId").toString();
                map.remove("tagId");
                msp.put(tagId,map);
            }

            Set<String> mspKey = msp.keySet();
            for(String key: mspKey){
                Map newMap = msp.get(key);
                newMap.put("tagId", key);
                listMap.add(newMap);
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
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);

            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
            resultData.put("partyCategorys",listMap);
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

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


}
