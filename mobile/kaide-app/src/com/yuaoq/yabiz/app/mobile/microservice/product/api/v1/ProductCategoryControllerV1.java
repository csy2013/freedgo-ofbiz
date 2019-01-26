package com.yuaoq.yabiz.app.mobile.microservice.product.api.v1;

import com.yuaoq.yabiz.app.mobile.microservice.index.api.v1.IndexControllerV1;
import com.yuaoq.yabiz.mobile.common.Paginate;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Created by changsy on 2018/4/16.
 */
@RestController
@RequestMapping(value = "/api/productCategory/v1")
public class ProductCategoryControllerV1 {
    
    
    @Value("${image.base.url}")
    String baseImgUrl;
    public static final String module = ProductCategoryControllerV1.class.getName();
    /**
     * 分类查询
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/firstCategories", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> firstCategories(HttpServletRequest request) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        List<EntityCondition> exps = FastList.newInstance();
        exps.add(EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, 1L));
        List<EntityExpr> isDelExp = FastList.newInstance();
        isDelExp.add(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        isDelExp.add(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null));
        
        exps.add(EntityCondition.makeCondition(isDelExp,EntityOperator.OR));
        exps.add(EntityCondition.makeCondition("productStoreId","10000"));
        
        boolean beganTransaction =false;
        try {
            beganTransaction = TransactionUtil.begin();
            List<GenericValue> firstCategories = delegator.findList("ProductCategory", EntityCondition.makeCondition(exps, EntityOperator.AND), UtilMisc.toSet("productCategoryId", "categoryName", "contentId","sequenceNum"), UtilMisc.toList("sequenceNum"), findOpts, false);
            List<Map> categories = FastList.newInstance();
            if (UtilValidate.isNotEmpty(firstCategories)) {
                for (GenericValue firstCategory : firstCategories) {
                    Map cateMap = FastMap.newInstance();
                    String contentId = firstCategory.getString("contentId");
                    String imgUrl = "";
                    if(UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl+ ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                    cateMap.put("productCategoryId", firstCategory.getString("productCategoryId"));
                    cateMap.put("categoryName", firstCategory.getString("categoryName"));
                    cateMap.put("categoryUrl", imgUrl);
                    categories.add(cateMap);
                }
            }
            TransactionUtil.commit(beganTransaction);
            resultData.put("categories", categories);
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
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
                resultData.put("retCode", 0);
                resultData.put("message", e.getMessage());
            }
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }
    
    /**
     * 分类查询
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/{parentId}/subtree", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> productDetail(HttpServletRequest request, @PathVariable(name="parentId")String parentId) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, true);
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        List<EntityCondition> exps = FastList.newInstance();
        exps.add(EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, 2L));
        List<EntityExpr> isDelExp = FastList.newInstance();
        isDelExp.add(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, "N"));
        isDelExp.add(EntityCondition.makeCondition("isDel", EntityOperator.EQUALS, null));
        exps.add(EntityCondition.makeCondition(isDelExp,EntityOperator.OR));
        exps.add(EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.EQUALS, parentId));
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
            List<GenericValue> secondCategories = delegator.findList("ProductCategory", EntityCondition.makeCondition(exps, EntityOperator.AND), UtilMisc.toSet("productCategoryId", "categoryName", "contentId","sequenceNum"), UtilMisc.toList("sequenceNum"), findOpts, false);
            List<Map> categories = FastList.newInstance();
            if (UtilValidate.isNotEmpty(secondCategories)) {
                for (GenericValue secondCategory : secondCategories) {
                    Map cateMap = FastMap.newInstance();
                    String contentId = secondCategory.getString("contentId");
                    String imgUrl = "";
                    if(UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl+ ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                    cateMap.put("productCategoryId", secondCategory.getString("productCategoryId"));
                    cateMap.put("categoryName", secondCategory.getString("categoryName"));
                    cateMap.put("categoryUrl", imgUrl);
                   
                    //三级菜单
                    exps = FastList.newInstance();
                    exps.add(EntityCondition.makeCondition("productCategoryLevel", EntityOperator.EQUALS, 3L));
                    exps.add(EntityCondition.makeCondition(isDelExp,EntityOperator.OR));
                    exps.add(EntityCondition.makeCondition("primaryParentCategoryId", EntityOperator.EQUALS, secondCategory.getString("productCategoryId")));
                    List<GenericValue> thirdCategories = delegator.findList("ProductCategory", EntityCondition.makeCondition(exps, EntityOperator.AND), UtilMisc.toSet("productCategoryId", "categoryName", "contentId","sequenceNum"), UtilMisc.toList("sequenceNum"), findOpts, false);
                    List<Map> thirdList = FastList.newInstance();
                    if (UtilValidate.isNotEmpty(thirdCategories)) {
                        for (GenericValue thirdCategory : thirdCategories) {
                            Map thirdCateMap = FastMap.newInstance();
                            String thirdContentId = thirdCategory.getString("contentId");
                            String thirdImgUrl = "";
                            if(UtilValidate.isNotEmpty(thirdContentId)) {
                                thirdImgUrl = baseImgUrl+ ContentWorker.renderContentAsText(dispatcher, delegator, thirdContentId, false);
                            }
                            thirdCateMap.put("productCategoryId", thirdCategory.getString("productCategoryId"));
                            thirdCateMap.put("categoryName", thirdCategory.getString("categoryName"));
                            thirdCateMap.put("categoryUrl", thirdImgUrl);
                            thirdList.add(thirdCateMap);
                        }
                    }
                    cateMap.put("childCategories",thirdList);
                    categories.add(cateMap);
                    
                }
                TransactionUtil.commit(beganTransaction);
            }
            resultData.put("categories", categories);
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
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        
    }

    /**
     * 获取商品分类 一级分类
     * @param request
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getCategories", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getCategories(HttpServletRequest request,@RequestParam (defaultValue = "0") Integer page,@RequestParam (defaultValue = "100")Integer pageSize ) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String webSiteId = request.getHeader("client");
        Map<String, Object> resultData = FastMap.newInstance();
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        int limit = pageSize;
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
            DynamicViewEntity dynamicView = new DynamicViewEntity();//product_category

            dynamicView.addMemberEntity("PRD", "ProductRecommend");
            dynamicView.addAlias("PRD", "recommendId");
            dynamicView.addAlias("PRD", "productId");
            dynamicView.addAlias("PRD", "isAllWebSite");
            dynamicView.addAlias("PRD", "status");
            dynamicView.addAlias("PRD", "sequenceId");//SEQUENCE_ID sequenceId
            dynamicView.addAlias("PRD", "createdStamp");//CREATED_STAMP

            dynamicView.addMemberEntity("PRWS", "ProductRecommendWebSite");
            dynamicView.addAlias("PRWS", "webSiteId");
            dynamicView.addViewLink("PRD", "PRWS", true, ModelKeyMap.makeKeyMapList("recommendId", "recommendId"));

            List<String> fieldToSel = FastList.newInstance();

            fieldToSel.add("recommendId");
            fieldToSel.add("productId");
            fieldToSel.add("isAllWebSite");
            fieldToSel.add("webSiteId");
            fieldToSel.add("status");
            fieldToSel.add("sequenceId");
            fieldToSel.add("createdStamp");

            dynamicView.setGroupBy(fieldToSel);

            andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, "Y"));
            List<EntityCondition> defaultExprs2 = FastList.newInstance();
            defaultExprs2.add(EntityCondition.makeCondition("isAllWebSite", EntityOperator.EQUALS, "0"));
            defaultExprs2.add(EntityCondition.makeCondition("webSiteId", EntityOperator.EQUALS, webSiteId));
            andExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.OR));
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }
            //获取品牌列表
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldToSel, null, findOpts);
            List<GenericValue> products = eli.getPartialList(lowIndex, viewSize);
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();
            List<Map> productCategoryList = FastList.newInstance();
            if (UtilValidate.isNotEmpty(products)) {
                for (GenericValue product : products) {
                    Map<String, Object> groupProducCategoryMap = FastMap.newInstance();//PRODUCT_CATEGORY_LEVEL

                    String productId = product.getString("productId");

                    GenericValue createProduct = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",productId));
                    if (UtilValidate.isNotEmpty(createProduct)){

                        //primaryProductCategoryId商品分类 platformClassId平台分类
                        String primaryProductCategoryId = createProduct.getString("platformClassId");
                        if (UtilValidate.isNotEmpty(primaryProductCategoryId)){

                            GenericValue thirdProductCategory = delegator.findByPrimaryKey("ProductCategory",UtilMisc.toMap("productCategoryId",primaryProductCategoryId));
                            if (UtilValidate.isNotEmpty(thirdProductCategory)){
                                String productCategoryLevel = thirdProductCategory.getString("productCategoryLevel");
                                String primaryParentCategoryId = thirdProductCategory.getString("primaryParentCategoryId");
                                if ("3".equals(productCategoryLevel)){
                                    //如果商品等级为3，就找到这个等级的商品，获取这个商品的等级和他的ID
                                    GenericValue second = delegator.findByPrimaryKey("ProductCategory",UtilMisc.toMap("productCategoryId",primaryParentCategoryId));
                                    String secondProductCategoryLevel = second.getString("productCategoryLevel");
                                    String secondPrimaryParentCategoryId = second.getString("primaryParentCategoryId");
                                    if ("2".equals(secondProductCategoryLevel)){
                                        GenericValue first = delegator.findByPrimaryKey("ProductCategory",UtilMisc.toMap("productCategoryId",secondPrimaryParentCategoryId));
                                        if (UtilValidate.isNotEmpty(first)){
                                            String productCategoryId = first.getString("productCategoryId");
                                            String categoryName = first.getString("categoryName");
                                            String level = first.getString("productCategoryLevel");
                                            String isDel = first.getString("isDel");
                                            if ("N".equals(isDel)||UtilValidate.isEmpty(isDel)){
                                                groupProducCategoryMap.put("tagId",productCategoryId);
                                                groupProducCategoryMap.put("name",categoryName);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                    productCategoryList.add(groupProducCategoryMap);
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
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);

            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
            resultData.put("productCategorys",listMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
        }  catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 获取拼团推荐下 商品的一级分类
     *
     * @param request
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/getGroupProductCategories", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getGroupProductCategories(HttpServletRequest request,@RequestParam (defaultValue = "0") Integer page,@RequestParam (defaultValue = "100")Integer pageSize ) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        int limit = pageSize;
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
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("PA","ProductActivity");
            dynamicView.addAlias("PA","activityType");
            dynamicView.addAlias("PA","activityId");
            dynamicView.addAlias("PA","activityAuditStatus");
            dynamicView.addAlias("PA", "activityStartDate");
            dynamicView.addAlias("PA", "activityEndDate");

            dynamicView.addMemberEntity("PAG","ProductActivityGoods");
            dynamicView.addAlias("PAG","productId");
            dynamicView.addViewLink("PA", "PAG", false, ModelKeyMap.makeKeyMapList("activityId", "activityId"));

            List<String> fieldToSel = FastList.newInstance();
            fieldToSel.add("activityType");
            fieldToSel.add("activityAuditStatus");
            fieldToSel.add("productId");
            fieldToSel.add("activityId");

            dynamicView.setGroupBy(fieldToSel);

            andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "GROUP_ORDER"));
            andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));

            //拼团活动的时间限制
            andExprs.add(EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp()));
            andExprs.add(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN, UtilDateTime.nowTimestamp()));

            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }

            //去重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            findOpts.setDistinct(true);
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldToSel, null, findOpts);
            List<GenericValue> productCategorys = eli.getCompleteList();

            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();

            List<Map<String,String>> tmpList=new ArrayList<Map<String,String>>();
            List<Map> groupProducCategoryList = FastList.newInstance();
                for (GenericValue productCategory : eli.getCompleteList()) {
                    Map<String, Object> groupProducCategoryMap = FastMap.newInstance();//PRODUCT_CATEGORY_LEVEL
                    String productId = productCategory.getString("productId");
                    if (UtilValidate.isNotEmpty(productId)){
                        GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",productId));
                        if (UtilValidate.isNotEmpty(product)){
                            String primaryProductCategoryId = product.getString("platformClassId");//platformClassId 平台分类id
                            if(UtilValidate.isNotEmpty(primaryProductCategoryId)){
                                GenericValue thirdProductCatrgory = delegator.findByPrimaryKey("ProductCategory",UtilMisc.toMap("productCategoryId",primaryProductCategoryId));
                                if (UtilValidate.isNotEmpty(thirdProductCatrgory)){
                                    String primaryParentCategoryId = thirdProductCatrgory.getString("primaryParentCategoryId");
                                    String productCategoryLevel = thirdProductCatrgory.getString("productCategoryLevel");
                                    if ("3".equals(productCategoryLevel)){
                                        //如果商品等级为3，就找到这个等级的商品，获取这个商品的等级和他的ID
                                        GenericValue second = delegator.findByPrimaryKey("ProductCategory",UtilMisc.toMap("productCategoryId",primaryParentCategoryId));
                                        String secondProductCategoryLevel = second.getString("productCategoryLevel");
                                        String secondPrimaryParentCategoryId = second.getString("primaryParentCategoryId");
                                        if ("2".equals(secondProductCategoryLevel)){
                                            GenericValue first = delegator.findByPrimaryKey("ProductCategory",UtilMisc.toMap("productCategoryId",secondPrimaryParentCategoryId));
                                            if (UtilValidate.isNotEmpty(first)){
                                                String productCategoryId = first.getString("productCategoryId");
                                                String categoryName = first.getString("categoryName");
                                                String level = first.getString("productCategoryLevel");
                                                String isDel = first.getString("isDel");
                                                if ("N".equals(isDel)||UtilValidate.isEmpty(isDel)){
                                                    groupProducCategoryMap.put("tagId",productCategoryId);
                                                    groupProducCategoryMap.put("name",categoryName);
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }

                    groupProducCategoryList.add(groupProducCategoryMap);
                }

            Map<String, Map> msp = new HashMap<String, Map>();
            List<Map<String, String>> listMap = new ArrayList<Map<String,String>>();

            for (int i = groupProducCategoryList.size()-1;i>=0;i--){
                Map map = groupProducCategoryList.get(i);
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
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);

            resultData.put("size", Integer.valueOf(size));
            resultData.put("highIndex", Integer.valueOf(highIndex));
            resultData.put("lowIndex", Integer.valueOf(lowIndex));
            resultData.put("productCategorys",listMap);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");
        }  catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {

            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {

            }
        }

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


}
