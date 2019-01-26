package com.yuaoq.yabiz.app.mobile.microservice.activity.api.v1;

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
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
/**
 * Created by changsy on 2018/3/31.
 */
@RestController
@RequestMapping(value = "/api/activity/v1")
public class ActivityControllerV1 {
    
    @Value("${image.base.url}")
    String baseImgUrl;
    public static final String module = ActivityControllerV1.class.getName();
    
    /**
     * 活动推荐 获取活动标签
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/getActivityTags", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getActivityTags(HttpServletRequest request) {
        Map<String, Object> resultData = FastMap.newInstance();
        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("error", "站点编号不能为空");
        }

        int size = 0;
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        try {

            beganTransaction = TransactionUtil.begin();

            dynamicView.addMemberEntity("PTA", "ProductTopicActivity");
            dynamicView.addAlias("PTA", "productTopicActivityId", "productTopicActivityId", null, true, true, null);
            dynamicView.addAlias("PTA", "isUse");
            dynamicView.addAlias("PTA", "isAllWebSite");
            dynamicView.addAlias("PTA", "bigImg");
            dynamicView.addAlias("PTA", "linkType");
            dynamicView.addAlias("PTA", "linkUrl");
            dynamicView.addAlias("PTA", "linkId");
            dynamicView.addAlias("PTA", "tagId");
            dynamicView.addAlias("PTA", "createdStamp");
            dynamicView.addAlias("PTA", "sequenceId");
            dynamicView.addAlias("PTA", "topicActivityName");
            dynamicView.addMemberEntity("PTAWS", "ProductTopicActivityWebSite");
            dynamicView.addAlias("PTAWS", "webSiteId");
            dynamicView.addViewLink("PTA", "PTAWS", true, ModelKeyMap.makeKeyMapList("productTopicActivityId", "productTopicActivityId"));

            fieldsToSelect.add("productTopicActivityId");
            fieldsToSelect.add("isUse");
            fieldsToSelect.add("bigImg");
            fieldsToSelect.add("linkType");
            fieldsToSelect.add("isAllWebSite");
            fieldsToSelect.add("webSiteId");
            fieldsToSelect.add("topicActivityName");
            fieldsToSelect.add("linkId");
            fieldsToSelect.add("tagId");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("sequenceId");

            dynamicView.setGroupBy(fieldsToSelect);

            andExprs.add(EntityCondition.makeCondition("tagId", EntityOperator.NOT_EQUAL, null));

            //是否使用 0是 1否
            andExprs.add(EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, "0"));
            //链接类型判断
            andExprs.add(EntityCondition.makeCondition("linkType", EntityOperator.NOT_EQUAL, null));
            //isAllWebSite 0全部站点
            List<EntityCondition> topicActivityConditions2 = FastList.newInstance();
            topicActivityConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
            topicActivityConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));

            andExprs.add(EntityCondition.makeCondition(topicActivityConditions2, EntityOperator.OR));

            //添加where条件
            if (andExprs.size() > 0) {
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
            }

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, 10, true);
            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), null, findOpts);

            List<Map> tagList = FastList.newInstance();

            for (GenericValue gv : pli.getCompleteList()) {
                Map map = FastMap.newInstance();
                String activityTagId = gv.getString("tagId");
                StringBuffer activityTags = new StringBuffer();
                activityTags.append(activityTagId);
                activityTags.append(",");
                String tagIds = activityTags.toString();
                if (tagIds.endsWith(",")) {
                    tagIds = tagIds.substring(0, tagIds.length() - 1);
                }

                if (UtilValidate.isNotEmpty(tagIds)){
                    String [] tagArr = null;
                    tagArr=tagIds.split(",");

                    for (int t=0;t<tagArr.length;t++){
                        String tagId = tagArr[t];
                        if (UtilValidate.isNotEmpty(tagId)){
                            GenericValue activityTag = delegator.findByPrimaryKey("Tag",UtilMisc.toMap("tagId",tagId));
                            if (UtilValidate.isNotEmpty(activityTag)){
                                String newTagId = activityTag.getString("tagId");
                                String activityTagName = activityTag.getString("tagName");
                                map.put("tagId", newTagId);
                                map.put("name", activityTagName);

                            }
                        }

                    }

                }
                tagList.add(map);

            }

            Map<String, Map> msp = new HashMap<String, Map>();
            List<Map<String, String>> listMap = new ArrayList<Map<String,String>>();

           
            for (int i = tagList.size()-1;i>=0;i--){
                Map map = tagList.get(i);
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

            // 获取总记录数
            size = pli.getResultsSizeAfterPartialList();

            boolean hasNext = true;
            boolean hasPrev = true;

            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);

            resultData.put("tagList", listMap);
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
        //返回的参数
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 活动推荐列表查询
     *
     * @param request
     * @param page
     * @param pageSize
     * @param tagId
     * @return
     */
    @RequestMapping(value = "/activityList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> activityList(HttpServletRequest request, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize, String tagId) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        List<String> orderBy = FastList.newInstance();
        int limit = pageSize;
        int lowIndex = 0;
        int highIndex = 0;
        int size = 0;
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
            resultData.put("error", "站点编号不能为空");
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            beganTransaction = TransactionUtil.begin();

            //活动区块--活动管理
            DynamicViewEntity topicActivityViewEntity = new DynamicViewEntity();
            //显示字段集合
            List<String> fieldsToSelect = FastList.newInstance();

            topicActivityViewEntity.addMemberEntity("PTA", "ProductTopicActivity");
            topicActivityViewEntity.addAlias("PTA", "productTopicActivityId", "productTopicActivityId", null, true, true, null);
            topicActivityViewEntity.addAlias("PTA", "isUse");
            topicActivityViewEntity.addAlias("PTA", "isAllWebSite");
            topicActivityViewEntity.addAlias("PTA", "bigImg");
            topicActivityViewEntity.addAlias("PTA", "linkType");
            topicActivityViewEntity.addAlias("PTA", "linkUrl");
            topicActivityViewEntity.addAlias("PTA", "linkId");
            topicActivityViewEntity.addAlias("PTA", "tagId");
            topicActivityViewEntity.addAlias("PTA", "createdStamp");
            topicActivityViewEntity.addAlias("PTA", "sequenceId");
            topicActivityViewEntity.addAlias("PTA", "topicActivityName");
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
            fieldsToSelect.add("tagId");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("sequenceId");

            topicActivityViewEntity.setGroupBy(fieldsToSelect);
            orderBy.add("sequenceId ASC");
            List<EntityCondition> topicActivityConditions1 = FastList.newInstance();

            if (UtilValidate.isEmpty(tagId)){

                //是否使用 0是 1否
                topicActivityConditions1.add(EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, "0"));
                //isAllWebSite 0全部站点
                List<EntityCondition> topicActivityConditions2 = FastList.newInstance();
                topicActivityConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
                topicActivityConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));

                topicActivityConditions1.add(EntityCondition.makeCondition(topicActivityConditions2, EntityOperator.OR));
                //去重
                EntityFindOptions findOpts = new EntityFindOptions(true,EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);

                eli = delegator.findListIteratorByCondition(topicActivityViewEntity, EntityCondition.makeCondition(topicActivityConditions1, EntityOperator.AND), null, fieldsToSelect, orderBy, findOpts);
                List<GenericValue> topicActivitys = eli.getPartialList(lowIndex, viewSize);

                if (UtilValidate.isNotEmpty(topicActivitys)) {
                    List<Map> activityList = FastList.newInstance();
                    for (GenericValue topicActivity : topicActivitys) {
                        Map activityMap = FastMap.newInstance();
                        activityMap.put("activityName", topicActivity.getString("topicActivityName"));
                        String contentId = (String) topicActivity.get("bigImg");
                        if (UtilValidate.isNotEmpty(contentId)) {
                            String imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                            activityMap.put("imgUrl", imgUrl);
                        }
                        String linkType = UtilValidate.isNotEmpty(topicActivity.getString("linkType"))?topicActivity.getString("linkType"):null;
                        String linkId = topicActivity.getString("linkId");
                        activityMap.put("linkType", linkType);
                        activityMap.put("linkId", linkId);
                        activityList.add(activityMap);
                    }
                    resultData.put("activityList", activityList);
                    resultData.put("retCode", 1);
                    resultData.put("message", "查询成功");
                }

            }else {

                //是否使用 0是 1否
                topicActivityConditions1.add(EntityCondition.makeCondition("isUse", EntityOperator.EQUALS, "0"));
                //isAllWebSite 0全部站点
                List<EntityCondition> topicActivityConditions2 = FastList.newInstance();
                topicActivityConditions2.add(EntityCondition.makeCondition("isAllWebSite", "0"));
                topicActivityConditions2.add(EntityCondition.makeCondition("webSiteId", webSiteId));

                topicActivityConditions1.add(EntityCondition.makeCondition(topicActivityConditions2, EntityOperator.OR));
                //去重
                EntityFindOptions findOpts = new EntityFindOptions(true,
                        EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);

                eli = delegator.findListIteratorByCondition(topicActivityViewEntity, EntityCondition.makeCondition(topicActivityConditions1, EntityOperator.AND), null, fieldsToSelect, orderBy, findOpts);
                List<GenericValue> topicActivitys = eli.getPartialList(lowIndex, viewSize);

                if (UtilValidate.isNotEmpty(topicActivitys)) {
                    List<Map> activityList = FastList.newInstance();
                    for (GenericValue topicActivity : topicActivitys) {
                        Map activityMap = FastMap.newInstance();
                        String activityTagId = topicActivity.getString("tagId");
                        StringBuffer activityTags = new StringBuffer();
                        activityTags.append(activityTagId);
                        activityTags.append(",");
                        String tagIds = activityTags.toString();
                        if (tagIds.endsWith(",")) {
                            tagIds = tagIds.substring(0, tagIds.length() - 1);
                        }
                        String [] tagArr = null;
                        tagArr=tagIds.split(",");
                        for (int t=0;t<tagArr.length;t++){
                            String newtagId = tagArr[t];
                            //如果匹配
                            if (tagId.equals(newtagId)){
                                activityMap.put("activityName", topicActivity.getString("topicActivityName"));
                                String contentId = (String) topicActivity.get("bigImg");
                                if (UtilValidate.isNotEmpty(contentId)) {
                                    String imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                                    activityMap.put("imgUrl", imgUrl);
                                }
                                String linkType = UtilValidate.isEmpty(topicActivity.getString("linkType"))?null:topicActivity.getString("linkType");
                                String linkId = topicActivity.getString("linkId");
                                activityMap.put("linkType", linkType);
                                activityMap.put("linkId", linkId);
                            }
                        }

                        if (UtilValidate.isNotEmpty(activityMap)){
                            activityList.add(activityMap);
                        }

                    }

                    resultData.put("activityList", activityList);
                    resultData.put("retCode", 1);
                    resultData.put("message", "查询成功");
                }

            }

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
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 促销活动详情
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/promoAreaDetail", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> promoAreaDetail(HttpServletRequest request, HttpServletResponse response) {
        
        String activityId = request.getParameter("activityId");
        String productCategoryId = request.getParameter("productCategoryId");
        String page = request.getParameter("page");
        String limit = request.getParameter("pageSize");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        String webSiteId = request.getHeader("client");
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt(page);
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        
        int viewSize = 10;
        try {
            viewSize = Integer.parseInt(limit);
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
            dynamicView.addMemberEntity("PAM", "ProductActivityManager");
            dynamicView.addAlias("PAM", "productActivityManagerId");
            dynamicView.addAlias("PAM", "activityManagerName");
            dynamicView.addAlias("PAM", "contentId");

            dynamicView.addMemberEntity("SCA", "SubjectColumnActivityAssoc");
            dynamicView.addAlias("SCA", "productActivityManagerId");
            dynamicView.addAlias("SCA", "subjectColumnId");
            dynamicView.addAlias("SCA", "productActivityManagerId");
            
            dynamicView.addMemberEntity("SCP", "SubjectColumnProductAssoc");
            dynamicView.addAlias("SCP", "subjectColumnId");
            dynamicView.addAlias("SCP", "productId");
            dynamicView.addAlias("SCP","createdStamp");
            
            dynamicView.addMemberEntity("P", "Product");
            dynamicView.addAlias("P", "productId");
            dynamicView.addAlias("P", "isOnline");
            dynamicView.addAlias("P", "primaryProductCategoryId");
            
            dynamicView.addViewLink("PAM", "SCA", false, ModelKeyMap.makeKeyMapList("productActivityManagerId"));
            dynamicView.addViewLink("SCA", "SCP", false, ModelKeyMap.makeKeyMapList("subjectColumnId"));
            dynamicView.addViewLink("SCP", "P", false, ModelKeyMap.makeKeyMapList("productId"));

            fieldsToSelect.add("productId");
            fieldsToSelect.add("createdStamp");
            fieldsToSelect.add("isOnline");
            dynamicView.setGroupBy(fieldsToSelect);
            List<EntityCondition> exps = FastList.newInstance();
            orderBy.add("createdStamp ASC");
            
            EntityCondition mainCond = null;
            exps.add(EntityCondition.makeCondition("productActivityManagerId", activityId));
            //商品必须是上架的
            exps.add(EntityCondition.makeCondition("isOnline", "Y"));
            if (UtilValidate.isNotEmpty(productCategoryId)) {
                exps.add(EntityCondition.makeCondition("subjectColumnId", productCategoryId));
            }

            if (exps.size()>0){
                mainCond = EntityCondition.makeCondition(exps, EntityOperator.AND);
            }
            //去重
            EntityFindOptions findOpts = new EntityFindOptions(true,
                    EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            
            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // attempt to get the full size
            Integer size = eli.getResultsSizeAfterPartialList();
            
            resultData.put("highIndex", highIndex);
            resultData.put("lowIndex", lowIndex);
            resultData.put("size", size);
            List<GenericValue> productActivityManagers = eli.getPartialList(lowIndex, highIndex);
            StringBuffer productIds = new StringBuffer();
            if (UtilValidate.isNotEmpty(productActivityManagers)) {
                for (int i = 0; i < productActivityManagers.size(); i++) {
                    GenericValue manager = productActivityManagers.get(i);
                    String productId = manager.getString("productId");
                    productIds.append(productId);
                    productIds.append(",");
                }
                if (UtilValidate.isNotEmpty(productIds)) {
                    
                    Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productIds.toString()));
                    List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                    
                    if (UtilValidate.isNotEmpty(products)) {
                        List productList = FastList.newInstance();
                        for (int k = 0; k < products.size(); k++) {
                            Map<String, Object> product = products.get(k);
                            Map<String, Object> productMap = FastMap.newInstance();
                            String productId = (String) product.get("productId");
                                productMap.put("productId", productId);
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
                        resultData.put("products", productList);
                    }
                    
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
        
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
    
    
    /**
     * 促销活动 列表查询
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/promoAreaCategory", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> promoAreaListTest(HttpServletRequest request, HttpServletResponse response) {
        
        String activityId = request.getParameter("activityId");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
            List<GenericValue> activities = delegator.findByAnd("ProductActivityManager", UtilMisc.toMap("productActivityManagerId", activityId));
            if (UtilValidate.isNotEmpty(activities)) {
                GenericValue activity = activities.get(0);
                Map activityMap = activity.toMap();
                String contentId = activity.getString("contentId");
                activityMap.put("imgUrl", baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false));
                resultData.put("activity", activityMap);
                List<GenericValue> assocs = EntityUtil.getRelated("SubjectColumnActivityAssoc", activities);
                List<GenericValue> cateogires = EntityUtil.getRelated("SubjectColumn", assocs);
                resultData.put("categories", cateogires);
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
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }
}
