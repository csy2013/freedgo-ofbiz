/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.yuaoq.yabiz.product.facility;

import com.google.common.base.Joiner;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * 仓库service
 *
 * @author 钱进 2015/12/18
 */
public class InventoryServices {
    public static final String module = InventoryServices.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * 查询库存列表
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getInventoryList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> inventoryList = FastList.newInstance();
        
        //总记录数
        int inventoryListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        
        dynamicView.addMemberEntity("II", "InventoryItem");
        dynamicView.addAlias("II", "inventoryItemId");
        dynamicView.addAlias("II", "productId");
        dynamicView.addAlias("II", "facilityId");
        dynamicView.addAlias("II", "accountingQuantityTotal");
        dynamicView.addAlias("II", "lockQuantityTotal");
        dynamicView.addAlias("II", "quantityOnHandTotal");
        
        dynamicView.addMemberEntity("P", "Product");
        dynamicView.addAlias("P", "productId");
        dynamicView.addAlias("P", "productName");
        
        
        dynamicView.addViewLink("II", "P", false, ModelKeyMap.makeKeyMapList("productId", "productId"));
        
        fieldsToSelect.add("inventoryItemId");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("facilityId");
        fieldsToSelect.add("accountingQuantityTotal");
        fieldsToSelect.add("lockQuantityTotal");
        fieldsToSelect.add("quantityOnHandTotal");
        fieldsToSelect.add("productName");
        
        //排序字段名称
        String sortField = "productId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);
        
        //仓库ID
        if (UtilValidate.isNotEmpty(context.get("facilityId"))) {
            result.put("facilityId", context.get("facilityId"));
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("facilityId"), EntityOperator.EQUALS, EntityFunction.UPPER(context.get("facilityId"))));
        }
        //按商品名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("productName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("productName") + "%")));
        }
        //按商品编号查询
        if (UtilValidate.isNotEmpty(context.get("productId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("productId") + "%")));
        }
        
        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            for (GenericValue gv : pli.getPartialList(lowIndex, viewSize)) {
                String product_id = gv.getString("productId");
                Map map = FastMap.newInstance();
                map.put("inventoryItemId", gv.getString("inventoryItemId"));
                map.put("productId", product_id);
                map.put("facilityId", gv.getString("facilityId"));
                map.put("productName", gv.getString("productName"));
                
                //商品价格
                List<GenericValue> pp_list = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_id, "productPriceTypeId", "DEFAULT_PRICE"));
                String price = "";
                if (UtilValidate.isNotEmpty(pp_list)) {
                    GenericValue pp_gv = EntityUtil.getFirst(pp_list);
                    price = UtilMisc.doubleTrans(pp_gv.getBigDecimal("price"));
                }
                map.put("price", price);
                map.put("imgUrl", "");
                //根据商品ID获取商品图片url
                //List<GenericValue> productContentList = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId",product_id,"productContentTypeId","MEDIUM_IMAGE_URL"));
                //for(GenericValue pc_gv : productContentList){
                //	map.put("imgUrl", pc_gv.getString("contentId"));
                //}
                String productAdditionalImage1 = "";
                List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd(
                        "ProductContent", UtilMisc.toMap("productId",
                                product_id, "productContentTypeId",
                                "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                    GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product_id));
                    productAdditionalImage1 = ProductContentWrapper.getProductContentAsText(productInfo, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                }
                
                map.put("imgUrl", productAdditionalImage1);
                //锁定库存
                map.put("alreadyLock", gv.getDouble("lockQuantityTotal"));
                //可用库存
                Double unUsedQuantity = (gv.getDouble("accountingQuantityTotal") == null ? 0 : gv.getDouble("accountingQuantityTotal")) - (gv.getDouble("lockQuantityTotal") == null ? 0 : gv.getDouble("lockQuantityTotal"));
                map.put("available", unUsedQuantity);
                //总库存
                map.put("totalNum", gv.getDouble("accountingQuantityTotal"));
                //根据商品ID获取特征list
                DynamicViewEntity featureView = new DynamicViewEntity();
                featureView.addMemberEntity("PF", "ProductFeature");
                featureView.addAlias("PF", "productFeatureId");
                featureView.addAlias("PF", "description");
                
                featureView.addMemberEntity("PFA", "ProductFeatureAppl");
                featureView.addAlias("PFA", "productFeatureId");
                featureView.addAlias("PFA", "productId");
                featureView.addViewLink("PF", "PFA", false, ModelKeyMap.makeKeyMapList("productFeatureId", "productFeatureId"));
                
                EntityConditionList<EntityExpr> whereConditions = EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition("productId", EntityOperator.EQUALS, product_id)
                ), EntityOperator.AND);
                
                EntityListIterator feature_iterator = delegator.findListIteratorByCondition(featureView, whereConditions, null, UtilMisc.toSet("description"), null, findOpts);
                String features = "";
                for (GenericValue fi_gv : feature_iterator.getCompleteList()) {
                    features += fi_gv.get("description") + " ";
                }
                if (UtilValidate.isNotEmpty(features)) {
                    features = "(" + features.trim() + ")";
                }
                map.put("features", features);
                feature_iterator.close();
                inventoryList.add(map);
            }
            
            // 获取总记录数
            inventoryListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > inventoryListSize) {
                highIndex = inventoryListSize;
            }
            
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }
        
        //返回的参数
        result.put("inventoryList", inventoryList);
        result.put("totalSize", Integer.valueOf(inventoryListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        
        return result;
    }
    
    /**
     * 查询预警库存列表
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getWarningInventoryList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> inventoryList = FastList.newInstance();
        
        //总记录数
        int inventoryListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        result.put("viewIndex", Integer.valueOf(viewIndex));
        
        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        result.put("viewSize", Integer.valueOf(viewSize));
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        
        dynamicView.addMemberEntity("II", "InventoryItem");
        dynamicView.addAlias("II", "inventoryItemId");
        dynamicView.addAlias("II", "productId");
        dynamicView.addAlias("II", "facilityId");
        dynamicView.addAlias("II", "accountingQuantityTotal");
        dynamicView.addAlias("II", "warningQuantity");
        dynamicView.addAlias("II","lockQuantityTotal");
        dynamicView.addMemberEntity("P", "Product");
        dynamicView.addAlias("P", "productId");
        dynamicView.addAlias("P", "productName");
        
        dynamicView.addMemberEntity("F", "Facility");
        dynamicView.addAlias("F", "facilityId");
        dynamicView.addAlias("F", "facilityName");
        
       
        
        dynamicView.addMemberEntity("PB", "PartyBusiness");
        dynamicView.addAlias("PB", "partyId");
        dynamicView.addAlias("PB", "businessName");
        
        dynamicView.addMemberEntity("PF", "ProductStoreFacility");
        dynamicView.addAlias("PF", "productStoreId");
        
        dynamicView.addViewLink("II", "P", false, ModelKeyMap.makeKeyMapList("productId", "productId"));
        
        dynamicView.addViewLink("II", "PB", false, ModelKeyMap.makeKeyMapList("ownerPartyId", "partyId"));
        dynamicView.addViewLink("II", "F", false, ModelKeyMap.makeKeyMapList("facilityId", "facilityId"));
        dynamicView.addViewLink("F", "PF", false, ModelKeyMap.makeKeyMapList("facilityId", "facilityId"));
        
        fieldsToSelect.add("inventoryItemId");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("facilityId");
        fieldsToSelect.add("facilityName");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("businessName");
        fieldsToSelect.add("accountingQuantityTotal");
        fieldsToSelect.add("warningQuantity");
        fieldsToSelect.add("productName");
        fieldsToSelect.add("lockQuantityTotal");
        
        //排序字段名称
        String sortField = "productId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String) context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if (UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String) context.get("sortType");
        }
        orderBy.add(sortType + sortField);
        
        //按商品名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("productName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("productName") + "%")));
        }
        //按商品编号查询
        if (UtilValidate.isNotEmpty(context.get("productId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productId"), EntityOperator.LIKE, EntityFunction.UPPER("%" + context.get("productId") + "%")));
        }
        //按仓库编号查询
        if (UtilValidate.isNotEmpty(context.get("facilityId")) &&  (!"undefined".equals(context.get("facilityId").toString()))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("facilityId"), EntityOperator.EQUALS, context.get("facilityId")));
        }
        
        // 店铺
        if (UtilValidate.isNotEmpty(context.get("productStoreId"))) {
            andExprs.add(EntityCondition.makeCondition("productStoreId", EntityOperator.EQUALS, context.get("productStoreId")));
        }
        
        /* andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("warningQuantity"), EntityOperator.GREATER_THAN_EQUAL_TO, EntityFieldValue.makeFieldValue("accountingQuantityTotal")));*/
//        andExprs.add(EntityCondition.makeCondition("productPriceTypeId", EntityOperator.EQUALS,"DEFAULT_PRICE"));
        
        //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            List<GenericValue> inventoryItems = pli.getCompleteList();
           
            List<GenericValue> warningInventoryItems = FastList.newInstance();
            if(UtilValidate.isNotEmpty(inventoryItems)){
                for (int i = 0; i < inventoryItems.size(); i++) {
                    GenericValue inventoryItem = inventoryItems.get(i);
                    BigDecimal accountingQuantityTotal =  inventoryItem.getBigDecimal("accountingQuantityTotal")==null?BigDecimal.ZERO:inventoryItem.getBigDecimal("accountingQuantityTotal");
                    BigDecimal lockQuantityTotal =  inventoryItem.getBigDecimal("lockQuantityTotal")==null?BigDecimal.ZERO:inventoryItem.getBigDecimal("lockQuantityTotal");
                    BigDecimal warningQuantity =  inventoryItem.getBigDecimal("warningQuantity")==null?BigDecimal.ZERO:inventoryItem.getBigDecimal("warningQuantity");
                    BigDecimal amount = accountingQuantityTotal.subtract(lockQuantityTotal);
                    
                    if(amount.compareTo(warningQuantity)<=0){
                        warningInventoryItems.add(inventoryItem);
                    }
                }
            }
            
            inventoryListSize = warningInventoryItems.size();
            List<GenericValue> subInventoryItems = FastList.newInstance();
            if (UtilValidate.isNotEmpty(warningInventoryItems)) {
                if(warningInventoryItems.size()>highIndex) {
                    subInventoryItems = warningInventoryItems.subList(lowIndex-1, highIndex);
                }else{
                    subInventoryItems = warningInventoryItems.subList(lowIndex-1,warningInventoryItems.size());
                }
            }
            
            // 获取分页所需的记录集合
            for (GenericValue gv : subInventoryItems) {
                String product_id = gv.getString("productId");
                Map map = FastMap.newInstance();
                map.put("inventoryItemId", gv.getString("inventoryItemId"));
                map.put("productId", product_id);
                map.put("facilityId", gv.getString("facilityId"));
                map.put("facilityName", gv.getString("facilityName"));
                map.put("productName", gv.getString("productName"));
                
                //商品价格
                List<GenericValue> pp_list = delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", product_id, "productPriceTypeId", "DEFAULT_PRICE"));
                String price = "";
                if (UtilValidate.isNotEmpty(pp_list)) {
                    GenericValue pp_gv = EntityUtil.getFirst(pp_list);
                    price = UtilMisc.doubleTrans(pp_gv.getBigDecimal("price"));
                }
                map.put("price", price);
                map.put("imgUrl", "");
                //根据商品ID获取商品图片url
//            	List<GenericValue> productContentList = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId",product_id,"productContentTypeId","MEDIUM_IMAGE_URL"));
//            	for(GenericValue pc_gv : productContentList){
//            		map.put("imgUrl", pc_gv.getString("contentId"));
//            	}
                String productAdditionalImage1 = "";
                List<GenericValue> curProductAdditionalImage1 = delegator.findByAnd(
                        "ProductContent", UtilMisc.toMap("productId",
                                product_id, "productContentTypeId",
                                "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(curProductAdditionalImage1)) {
                    GenericValue productInfo = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", product_id));
                    productAdditionalImage1 = ProductContentWrapper.getProductContentAsText(productInfo, "ADDITIONAL_IMAGE_1", locale, dispatcher);
                    map.put("imgUrl", curProductAdditionalImage1.get(0).getString("contentId"));
                }
                BigDecimal lockQuantityTotal = gv.getBigDecimal("lockQuantityTotal")==null?BigDecimal.ZERO:gv.getBigDecimal("lockQuantityTotal");
                BigDecimal warningQuantity = gv.getBigDecimal("warningQuantity")==null?BigDecimal.ZERO:gv.getBigDecimal("warningQuantity");
                BigDecimal accountingQ = gv.getBigDecimal("accountingQuantityTotal")==null?BigDecimal.ZERO:gv.getBigDecimal("accountingQuantityTotal");
                //可用库存
                map.put("available", accountingQ.subtract(lockQuantityTotal));
                //预警库存
                map.put("warningQuantity", warningQuantity);
                //
                map.put("totalQuantity",accountingQ);
                //根据商品ID获取特征list
                DynamicViewEntity featureView = new DynamicViewEntity();
                featureView.addMemberEntity("PF", "ProductFeature");
                featureView.addAlias("PF", "productFeatureId");
                featureView.addAlias("PF", "description");
                
                featureView.addMemberEntity("PFA", "ProductFeatureAppl");
                featureView.addAlias("PFA", "productFeatureId");
                featureView.addAlias("PFA", "productId");
                featureView.addViewLink("PF", "PFA", false, ModelKeyMap.makeKeyMapList("productFeatureId", "productFeatureId"));
                
                EntityCondition whereConditions = EntityCondition.makeCondition(UtilMisc.toList(
                        EntityCondition.makeCondition("productId", EntityOperator.EQUALS, product_id)
                ), EntityOperator.AND);
                
                EntityListIterator feature_iterator = delegator.findListIteratorByCondition(featureView, whereConditions, null, UtilMisc.toSet("description"), null, findOpts);
                String features = "";
                for (GenericValue fi_gv : feature_iterator.getCompleteList()) {
                    features += fi_gv.get("description") + " ";
                }
                if (UtilValidate.isNotEmpty(features)) {
                    features = "(" + features.trim() + ")";
                }
                map.put("features", features);
                feature_iterator.close();
                inventoryList.add(map);
            }
            
            // 获取总记录数
            
            if (highIndex > inventoryListSize) {
                highIndex = inventoryListSize;
            }
            
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }
        
        
        //返回的参数
        result.put("inventoryList", inventoryList);
        result.put("totalSize", Integer.valueOf(inventoryListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        
        return result;
    }
    
    /**
     * 库存修改时加载初始化数据
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> goInventoryEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //记录集合
        List<GenericValue> inventoryList = FastList.newInstance();
        //获取参数
        String inventoryItemId = (String) context.get("inventoryItemId");
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("II", "InventoryItem");
        dynamicView.addAlias("II", "inventoryItemId");
        dynamicView.addAlias("II", "accountingQuantityTotal");
        dynamicView.addAlias("II", "facilityId");
        
        dynamicView.addMemberEntity("F", "Facility");
        dynamicView.addAlias("F", "facilityId");
        dynamicView.addAlias("F", "facilityName");
        dynamicView.addViewLink("II", "F", false, ModelKeyMap.makeKeyMapList("facilityId", "facilityId"));
        
        EntityCondition whereCondition = EntityCondition.makeCondition("inventoryItemId", EntityOperator.EQUALS, inventoryItemId);
        try {
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY, true);
            EntityListIterator iterator = delegator.findListIteratorByCondition(dynamicView, whereCondition, null, UtilMisc.toSet("inventoryItemId", "facilityName", "accountingQuantityTotal"), null, findOpts);
            inventoryList = iterator.getCompleteList();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("inventoryList", inventoryList);
        return result;
    }
    
    /**
     * 库存修改时加载初始化数据
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editInventory(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //获取参数
        String inventoryItemId = (String) context.get("inventoryItemId");
        String available = (String) context.get("available");
        
        try {
            //根据ID获取仓库记录并修改
            GenericValue inventory_gv = delegator.findByPrimaryKey("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemId));
            inventory_gv.setString("accountingQuantityTotal", available);
            inventory_gv.set("unitCost", new BigDecimal("0"));
            inventory_gv.setString("currencyUomId", "CNY");
            inventory_gv.set("occupiedQuantityTotal", new BigDecimal("0"));
            inventory_gv.store();
            // 删除缓存
            if(UtilValidate.isNotEmpty(inventory_gv)){
                String curProductId=inventory_gv.getString("productId");
                // Redis 信息的删除处理
                Map<String, Object> reParams = FastMap.newInstance();
                reParams.put("productIds", curProductId);
                // TODO isDel 临时 N==>Y  原因是不能添加为上线的商品编码
//                reParams.put("isDel", "N");
                reParams.put("isDel", "Y");
                Map<String, Object> resultMap = FastMap.newInstance();
                try {
                    // 调用服务 productInfoRedisPro
                    resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
                } catch (GenericServiceException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
//                if(UtilRedis.exists(curProductId+"_summary")){
//                    UtilRedis.del(curProductId+"_summary");// 产品缓存
//                }
//                if(UtilRedis.exists(curProductId+"_downPromo")){
//                    UtilRedis.del(curProductId + "_downPromo");// 产品直降信息
//                }
//                if(UtilRedis.exists(curProductId+"_groupOrder")){
//                    UtilRedis.del(curProductId+ "_groupOrder");// 产品团购信息
//                }
//                if(UtilRedis.exists(curProductId+"_seckill")) {
//                    UtilRedis.del(curProductId + "_seckill"); // 产品秒杀信息
//                }

            }



        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 新增库存
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addInventory(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //当前用户登录信息  
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        try {
            GenericValue p_gv = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", (String) context.get("productId")));
            
            //获取库存表新的ID
            String inventoryItem_new_id = delegator.getNextSeqId("InventoryItem").toString();
            //新增一条仓库记录
            GenericValue inventoryItem_gv = delegator.makeValue("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItem_new_id));
            inventoryItem_gv.setString("productId", (String) context.get("productId"));
            inventoryItem_gv.setString("facilityId", (String) context.get("facilityId"));
            inventoryItem_gv.setString("accountingQuantityTotal", (String) context.get("available"));
            inventoryItem_gv.setString("inventoryItemTypeId", "NON_SERIAL_INV_ITEM");
            inventoryItem_gv.setString("partyId", userLogin.get("partyId").toString());
            inventoryItem_gv.setString("ownerPartyId", p_gv.getString("businessPartyId"));

            inventoryItem_gv.set("unitCost", new BigDecimal("0"));
            inventoryItem_gv.setString("currencyUomId", "CNY");
            inventoryItem_gv.set("occupiedQuantityTotal", new BigDecimal("0"));

            inventoryItem_gv.create();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 库存导入
     *
     * @param dctx
     * @param context
     * @return
     */
    public static void inventoryImport(HttpServletRequest request, HttpServletResponse response) {
        //返回信息，json格式
        String returnJson = "";
        //当前用户
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        //Delegator对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        //LocalDispatcher对象
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        try {
            //调用Excel导入方法
            Map rs = dispatcher.runSync("excelImport", UtilMisc.toMap(
                    "request", request,
                    "xmlUrl", "src/com/qihua/ofbiz/excel/InventoryValidate.xml",
                    "validateCellData", "inventoryValidateCell"));
            //获取导入的信息
            returnJson = rs.get("msg").toString();
            //获取导入的数据list
            List<Map> listDatas = (List<Map>) rs.get("listDatas");
            List<String>  productIdRedisList=FastList.newInstance();
            //遍历list，进行新增或修改操作
            for (Map record : listDatas) {
                String product_id = (String) record.get("product_id");
                String facility_id = (String) record.get("facility_id");
                String available = (String) record.get("available");
                try {
                    //根据仓库ID和商品ID查找库存是否存在
                    List<GenericValue> pf_list = delegator.findByAnd("ProductFacility", UtilMisc.toMap("productId", product_id, "facilityId", facility_id));
                    if (pf_list == null || pf_list.size() == 0) {
                        //新增一条商品和仓库的关联记录
                        GenericValue pf_gv = delegator.makeValue("ProductFacility", UtilMisc.toMap("productId", product_id, "facilityId", facility_id));
                        pf_gv.create();
                    }
                    
                    //根据仓库ID和商品ID查找库存是否存在
                    List<GenericValue> list = delegator.findByAnd("InventoryItem", UtilMisc.toMap("productId", product_id, "facilityId", facility_id));
                    //如果库存不存在，则调用新增库存服务，如果存在，则调用修改库存服务
                    if (list.isEmpty()) {
                        dispatcher.runSync("addInventory", UtilMisc.toMap("productId", product_id, "facilityId", facility_id, "available", available, "userLogin", userLogin));
                    } else {
                        for (GenericValue gv : list) {
                            dispatcher.runSync("editInventory", UtilMisc.toMap("inventoryItemId", gv.getString("inventoryItemId"), "available", available));
                        }
                    }

                    // Redis 信息的删除处理
                    if(UtilValidate.isNotEmpty(product_id)){
                        productIdRedisList.add(product_id);
                    }
//                    if(UtilRedis.exists(product_id+"_summary")){
//                        UtilRedis.del(product_id+"_summary");// 产品缓存
//                    }
//                    if(UtilRedis.exists(product_id+"_downPromo")){
//                        UtilRedis.del(product_id + "_downPromo");// 产品直降信息
//                    }
//                    if(UtilRedis.exists(product_id+"_groupOrder")){
//                        UtilRedis.del(product_id+ "_groupOrder");// 产品团购信息
//                    }
//                    if(UtilRedis.exists(product_id+"_seckill")) {
//                        UtilRedis.del(product_id + "_seckill"); // 产品秒杀信息
//                    }

                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            if(UtilValidate.isNotEmpty(productIdRedisList)){
                String redisProductStr = Joiner.on(",").join(productIdRedisList);
                Map<String, Object> reParams = FastMap.newInstance();
                reParams.put("productIds", redisProductStr);
                // TODO isDel 临时 N==>Y  原因是不能添加为上线的商品编码
//                reParams.put("isDel", "N");
                reParams.put("isDel", "Y");
                Map<String, Object> resultMap = FastMap.newInstance();
                // 调用服务 productInfoRedisPro
                resultMap = dispatcher.runSync("productInfoRedisPro", reParams);
            }

        } catch (GenericServiceException e) {
            e.printStackTrace();
        } finally {
            PrintWriter out = null;
            try {
                out = response.getWriter();
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.write(returnJson);
            out.flush();
            out.close();
        }
    }
    
    /**
     * 库存检核 add by qian 2016.04.25
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> validateInventory(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        List<Map> param_list = (List) context.get("paramList");
        
        try {
            boolean validateFlag = true;
            String msg = "";
            for (Map map : param_list) {
                String id = map.get("id").toString();    //id
                double num = Double.valueOf(map.get("num").toString());    //数量
                String type = map.get("type").toString();    //类型
                String date = UtilValidate.isNotEmpty(map.get("date")) ? map.get("date").toString() : "";    //日期
                String facilityId = map.get("facilityId").toString();    //类型
                //根据类型，进行不同的校验  半成品：SEMI_FINISHED_GOOD 果粒：FRUIT_GRAIN_GOOD 仓库：FACILITY
                if ("SEMI_FINISHED_GOOD".equals(type)) {
                    //编辑查询条件，productId = id and facilityId = facilityId and accountingQuantityTotal = num
                    EntityCondition main_cond = EntityCondition.makeCondition(
                            UtilMisc.toList(
                                    EntityCondition.makeCondition("productId", EntityOperator.EQUALS, id),
                                    EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId),
                                    EntityCondition.makeCondition("accountingQuantityTotal", EntityOperator.GREATER_THAN_EQUAL_TO, new BigDecimal(num))
                            ),
                            EntityOperator.AND
                    );
                    List<GenericValue> product_list = delegator.findList("InventoryItem", main_cond, null, null, null, false);
                    if (UtilValidate.isEmpty(product_list)) {
                        validateFlag = false;
                        msg += "半成品【" + id + "】库存不足！";
                        break;
                    }
                } else if ("FRUIT_GRAIN_GOOD".equals(type)) {
                    if (UtilValidate.isNotEmpty(date)) {
                        //编辑查询条件，productId = id and facilityId = facilityId and accountingQuantityTotal = num
                        EntityCondition main_cond = EntityCondition.makeCondition(
                                UtilMisc.toList(
                                        EntityCondition.makeCondition("productId", EntityOperator.EQUALS, id),
                                        EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId),
                                        EntityCondition.makeCondition("accountingQuantityTotal", EntityOperator.GREATER_THAN, new BigDecimal(0))
                                ),
                                EntityOperator.AND
                        );
                        List<GenericValue> product_list = delegator.findList("InventoryItem", main_cond, null, null, null, false);
                        if (UtilValidate.isEmpty(product_list)) {
                            validateFlag = false;
                            msg += "果粒【" + id + "】库存不足！";
                            break;
                        } else {
                            //获取果粒的每天总产能
                            GenericValue psa_gv = delegator.findByPrimaryKey("ProductSemiAssoc", UtilMisc.toMap("productId", id));
                            double capacity = psa_gv.getLong("perQuantity");
                            //获取果粒已锁定产能
                            List<GenericValue> list = getQuantityByGlId(dctx, id, date);
                            double quantity = 0;
                            if (UtilValidate.isNotEmpty(list)) {
                                GenericValue gl_gv = EntityUtil.getFirst(list);
                                if (UtilValidate.isNotEmpty(gl_gv.get("proportion"))) {
                                    quantity = gl_gv.getDouble("proportion");    //果粒已锁定库存
                                }
                            }
                            //判断果粒剩余产能是否满足，capacity:总产能	quantity：已锁定产能	num：需求的产能
                            if ((capacity - quantity - num) < 0) {
                                validateFlag = false;
                                msg += "果粒【" + id + "】产能不足！";
                                break;
                            }
                        }
                    } else {
                        validateFlag = false;
                        msg += "果粒产能判断需填写日期！";
                        break;
                    }
                } else if ("FACILITY".equals(type)) {
                    if (UtilValidate.isNotEmpty(date)) {
                        //获取仓库的产能
                        GenericValue f_gv = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", facilityId));
                        double capacity = f_gv.getDouble("capacity");
                        List<GenericValue> list = getQuantityByFacilityId(dctx, id, date);
                        //获取订单的商品总数
                        double quantity = 0;
                        if (UtilValidate.isNotEmpty(list)) {
                            GenericValue gv = EntityUtil.getFirst(list);
                            if (UtilValidate.isNotEmpty(gv.get("quantity"))) {
                                quantity = gv.getDouble("quantity");
                            }
                        }
                        
                        //判断仓库剩余产能是否满足，capacity:总产能	quantity：已锁定产能	num：需求的产能
                        if ((capacity - quantity - num) < 0) {
                            validateFlag = false;
                            msg += "仓库【" + id + "】产能不足！";
                            break;
                        }
                    } else {
                        validateFlag = false;
                        msg += "仓库产能判断需填写日期！";
                        break;
                    }
                }
            }
            result.put("validateFlag", validateFlag);
            result.put("msg", msg);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 查看仓库已锁定库存 add by qianjin 2016.04.25
     *
     * @param dctx
     * @param context
     * @return
     */
    public static List<GenericValue> getQuantityByFacilityId(DispatchContext dctx, String facilityId, String date) {
        Delegator delegator = dctx.getDelegator();
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("OH", "OrderHeader");
        dynamicView.addAlias("OH", "originFacilityId");
        dynamicView.addAlias("OH", "deliveryDate");
        dynamicView.addAlias("OH", "statusId");
        
        dynamicView.addMemberEntity("OI", "OrderItem");
        dynamicView.addAlias("OI", "quantity", "quantity", null, null, null, "sum");
        dynamicView.addViewLink("OI", "OH", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("quantity");
        fieldsToSelect.add("deliveryDate");
        
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        //默认条件
        filedExprs.add(EntityCondition.makeCondition("originFacilityId", EntityOperator.EQUALS, facilityId));
        filedExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_WAITPAY", "ORDER_WAITSHIP", "ORDER_WAITRECEIVE", "ORDER_COMPLETED", "ORDER_WAITPRODUCE")));
        String start_date = date + " " + "00:00:00.000";
        String end_date = date + " " + "23:59:59.999";
        try {
            filedExprs.add(EntityCondition.makeCondition("deliveryDate", EntityOperator.GREATER_THAN_EQUAL_TO, ObjectType.simpleTypeConvert(start_date, "Timestamp", null, null)));
            filedExprs.add(EntityCondition.makeCondition("deliveryDate", EntityOperator.LESS_THAN_EQUAL_TO, ObjectType.simpleTypeConvert(end_date, "Timestamp", null, null)));
        } catch (GeneralException e1) {
            e1.printStackTrace();
        }
        
        //添加where条件
        EntityCondition mainCond = null;
        if (filedExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
        }
        
        //记录集合
        List<GenericValue> recordsList = FastList.newInstance();
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, findOpts);
            // 获取分页所需的记录集合
            recordsList = pli.getCompleteList();
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }
        return recordsList;
    }
    
    /**
     * 查看果粒已锁定库存 add by qianjin 2016.04.25
     *
     * @param dctx
     * @param context
     * @return
     */
    public static List<GenericValue> getQuantityByGlId(DispatchContext dctx, String glId, String date) {
        Delegator delegator = dctx.getDelegator();
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("OH", "OrderHeader");
        dynamicView.addAlias("OH", "deliveryDate");
        dynamicView.addAlias("OH", "statusId");
        
        dynamicView.addMemberEntity("OI", "OrderItem");
        dynamicView.addAliasAll("OI", "", null);
        dynamicView.addViewLink("OH", "OI", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        
        dynamicView.addMemberEntity("OISPP", "OrderItemSemiProductProportion");
        dynamicView.addAlias("OISPP", "proportion", "proportion", null, null, null, "sum");
        dynamicView.addAlias("OISPP", "semiProductProportionId", "semiProductProportionId", null, null, true, null);
        dynamicView.addViewLink("OI", "OISPP", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId", "orderItemSeqId", "orderItemSeqId"));
        
        dynamicView.addMemberEntity("P", "Product");
        dynamicView.addAlias("P", "productTypeId");
        dynamicView.addViewLink("OISPP", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("semiProductProportionId", "productId"));
        
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("proportion");
        fieldsToSelect.add("semiProductProportionId");
        
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        //默认条件
        filedExprs.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FRUIT_GRAIN_GOOD"));
        filedExprs.add(EntityCondition.makeCondition("semiProductProportionId", EntityOperator.EQUALS, glId));
        filedExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_WAITPAY", "ORDER_WAITSHIP", "ORDER_WAITRECEIVE", "ORDER_COMPLETED", "ORDER_WAITPRODUCE")));
        String start_date = date + " " + "00:00:00.000";
        String end_date = date + " " + "23:59:59.999";
        try {
            filedExprs.add(EntityCondition.makeCondition("deliveryDate", EntityOperator.GREATER_THAN_EQUAL_TO, ObjectType.simpleTypeConvert(start_date, "Timestamp", null, null)));
            filedExprs.add(EntityCondition.makeCondition("deliveryDate", EntityOperator.LESS_THAN_EQUAL_TO, ObjectType.simpleTypeConvert(end_date, "Timestamp", null, null)));
        } catch (GeneralException e1) {
            e1.printStackTrace();
        }
        
        //添加where条件
        EntityCondition mainCond = null;
        if (filedExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);
        }
        
        //记录集合
        List<GenericValue> recordsList = FastList.newInstance();
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, findOpts);
            // 获取分页所需的记录集合
            recordsList = pli.getCompleteList();
            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }
        return recordsList;
    }
}
