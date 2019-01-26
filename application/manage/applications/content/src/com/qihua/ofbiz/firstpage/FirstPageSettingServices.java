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
package com.qihua.ofbiz.firstpage;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.util.List;
import java.util.Map;


/**
 * FirstPageSetting service
 * @author zhajh 2018/04/09
 *
 */
public class FirstPageSettingServices {
    public static final String module = FirstPageSettingServices.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * 首页设置列表查询 add by qianjin 2016/01/13
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getFirstPageSettingList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> firstPageList = FastList.newInstance();
        
        //总记录数
        int firstPageListSize = 0;
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

		String productStoreId = (String) context.get("productStoreId"); // 店铺编码
		String firstPageType = (String) context.get("firstPageType"); // 首页配置类别
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("FP","FirstPageSetting");
        dynamicView.addAlias("FP","firstPageSettingId");
        dynamicView.addAlias("FP","firstPageType");
        dynamicView.addAlias("FP","sequence");
        dynamicView.addAlias("FP","id");
		dynamicView.addAlias("FP","productStoreId");
        dynamicView.addAlias("FP","status");

        fieldsToSelect.add("firstPageSettingId");
        fieldsToSelect.add("firstPageType");
        fieldsToSelect.add("sequence");
        fieldsToSelect.add("id");
		fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("status");


		// 店铺信息
//		if(UtilValidate.isNotEmpty(productStoreId)){
//			filedExprs.add(EntityCondition.makeCondition("productStoreId", productStoreId));
//		}

		// 首页配置类别
		if(UtilValidate.isNotEmpty(firstPageType)){
			filedExprs.add(EntityCondition.makeCondition("firstPageType", firstPageType));
		}
        filedExprs.add(EntityCondition.makeCondition("status", "Y"));
        //排序字段名称
        String sortField = "sequence";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
        	sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
        	sortType = (String)context.get("sortType");
        }
        orderBy.add(sortType+sortField);
        
        //添加where条件
        if (filedExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }
        
        try {
        	lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
			GenericValue curProductInfo=null;
			GenericValue curProductStoreInfo=null;
			GenericValue curProductBrandInfo=null;
			GenericValue curStoreInfo=null;
            List <GenericValue> curFristPageSettingList=FastList.newInstance();
            curFristPageSettingList=pli.getPartialList(lowIndex, viewSize);
            for(GenericValue gv : curFristPageSettingList){
				String curFirstPageSettingId = gv.getString("firstPageSettingId");
            	String curFirstPageType = gv.getString("firstPageType");
            	String curSequence = gv.getString("sequence");
            	String id = gv.getString("id");
            	String curProductStoreId = gv.getString("productStoreId");

            	Map map = FastMap.newInstance();
            	map.put("firstPageType", curFirstPageType);
				map.put("firstPageSettingId", curFirstPageSettingId);
        		map.put("productStoreId", curProductStoreId);
        		map.put("sequence", curSequence);

				curProductInfo=null;
				curProductStoreInfo=null;
				curProductBrandInfo=null;
				curStoreInfo=null;
				if("0".equals(curFirstPageType)){
					// 根据商品编码取得推荐商品
					curProductInfo= delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", id));
					if(UtilValidate.isNotEmpty(curProductInfo)){
						map.put("productInfo", curProductInfo);
                        map.put("productName", curProductInfo.getString("productName"));
                        map.put("productId", curProductInfo.getString("productId"));
                        if(UtilValidate.isNotEmpty(curProductInfo.getString("brandId"))){
                            GenericValue productBrandInfo= delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", curProductInfo.getString("brandId")));
                            if(UtilValidate.isNotEmpty(productBrandInfo)){
                                map.put("brandName", productBrandInfo.getString("brandName"));
                            }
                        }
					}
					// 根据商品编码取得商品店铺编码
					List<GenericValue> productStoreInfos= delegator.findByAnd("ProductStoreProductView", UtilMisc.toMap("productId", id));
					if(UtilValidate.isNotEmpty(productStoreInfos)){
						curProductStoreInfo=EntityUtil.getFirst(productStoreInfos);
						if(UtilValidate.isNotEmpty(curProductStoreInfo)){
							map.put("productStoreInfo", curProductStoreInfo);
                            map.put("storeName", curProductStoreInfo.getString("storeName"));
						}
					}
				}else if("1".equals(curFirstPageType)){
					// 根据品牌编码取得热门品牌
					curProductBrandInfo= delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", id));
					if(UtilValidate.isNotEmpty(curProductBrandInfo)){
                        map.put("productBrandInfo", curProductBrandInfo);
						map.put("brandName", curProductBrandInfo.getString("brandName"));
                        map.put("contentId", curProductBrandInfo.getString("contentId"));
                        map.put("brandNameAlias", curProductBrandInfo.getString("brandNameAlias"));
					}

				}else if("2".equals(curFirstPageType)){

					// 根据店铺编码取得热门店铺
					curStoreInfo=delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", id));
					if(UtilValidate.isNotEmpty(curStoreInfo)){
						map.put("storeInfo", curStoreInfo);
					}
				}else{

				}
				firstPageList.add(map);
            }

            // 获取总记录数
            firstPageListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > firstPageListSize) {
                highIndex = firstPageListSize;
            }

            //关闭 iterator
            pli.close();
		} catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        } 
        
        
        //返回的参数
        result.put("firstPageList",firstPageList);
        result.put("totalSize", Integer.valueOf(firstPageListSize));
        result.put("highIndex", Integer.valueOf(highIndex));

        return result;
    }

    /**
     * 首页设置新增
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> firstPageSettingAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        // 参数
		// 首页设置类别
        String firstPageType = (String)context.get("firstPageType");
		// 设置编码
        String ids = (String)context.get("ids");
		// 店铺
		String productStoreId=(String) context.get("productStoreId");

        String[] curIds = null;// 目标编码数组
        List<String> idList = FastList.newInstance();
        if(UtilValidate.isNotEmpty(ids)){
            for(String id : ids.split(",")){
                idList.add(id);
            }
        }

        List<String> curIdList = FastList.newInstance();// 插入用编码列表
        List<String> curErrIdList = FastList.newInstance();// 重复的编码列表
        Boolean chk=true;
        try {
        	//取得该类首页类别的条数
        	List<GenericValue> firstPageSettingList = delegator.findByAnd("FirstPageSetting",UtilMisc.toMap("firstPageType", firstPageType,"status","Y"));

            if(UtilValidate.isNotEmpty(idList)){
                for(String curId:idList){
                    if(UtilValidate.isNotEmpty(firstPageSettingList)) {
                        chk=true;
                        for (GenericValue fpsInfo : firstPageSettingList) {
                            String fpsId=fpsInfo.getString("id");
                            if(curId.equals(fpsId)){
                                curErrIdList.add(curId);
                                chk=false;
                                break;
                            }
                        }
                        if(chk) {
                            curIdList.add(curId);
                        }
                    }else{
                        curIdList.add(curId);
                    }
                }
            }

            int curSequenceNum=1;
        	if(UtilValidate.isNotEmpty(firstPageSettingList)){
                curSequenceNum=firstPageSettingList.size()+1;
			}

			if(UtilValidate.isNotEmpty(curIdList)){
                String curId="";
                for(int i=0;i<curIdList.size();i++){
                    curSequenceNum=curSequenceNum+i;
                    curId="";
                    curId=curIdList.get(i);
                    String firstPageSettingId = delegator.getNextSeqId("FirstPageSetting");
                    //新增一条FirstPageSetting记录
                    GenericValue firstPageSetting_gv = delegator.makeValue("FirstPageSetting", UtilMisc.toMap("firstPageSettingId",firstPageSettingId));
                    firstPageSetting_gv.setString("firstPageType",firstPageType);
                    firstPageSetting_gv.set("sequence", Long.valueOf(curSequenceNum));
                    firstPageSetting_gv.setString("id",curId);
                    firstPageSetting_gv.setString("productStoreId", productStoreId);
                    firstPageSetting_gv.setString("status", "Y");
                    firstPageSetting_gv.create();
                }

            }
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }

    /**
     * firstPageSetting修改数据初始化
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> firstPageSettingEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
        String firstPageSettingId = (String)context.get("firstPageSettingId");
        try {
            //根据首页设置编码取得一条firstPageSetting信息
        	GenericValue firstPageSetting_gv = delegator.findByPrimaryKey("FirstPageSetting", UtilMisc.toMap("firstPageSettingId",firstPageSettingId));
        	result.put("sequence",firstPageSetting_gv.get("sequence"));
        	result.put("firstPageType",firstPageSetting_gv.get("firstPageType"));
        	result.put("id", firstPageSetting_gv.get("id"));
        	result.put("productStoreId",firstPageSetting_gv.get("productStoreId"));
            result.put("firstPageSettingId",firstPageSettingId);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }

    /**
     * firstPageSetting修改序号
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> firstPageSettingEditSequence(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
        //参数
    	String firstPageSettingId = (String)context.get("firstPageSettingId");
//        String firstPageType = (String)context.get("firstPageType");
//        String id = (String)context.get("id");
//        String productStoreId = (String)context.get("productStoreId");
        String sequence = (String)context.get("sequence");

        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;

        try {
            //新增一条firstPageSetting记录
            GenericValue firstPageSetting_gv = delegator.findByPrimaryKey("FirstPageSetting", UtilMisc.toMap("firstPageSettingId",firstPageSettingId));
            if(UtilValidate.isNotEmpty(firstPageSetting_gv)){
                if(UtilValidate.isNotEmpty(sequence)){
                    andExprs.add(EntityCondition.makeCondition("sequence", EntityOperator.GREATER_THAN_EQUAL_TO, Long.valueOf(sequence)));
                    andExprs.add(EntityCondition.makeCondition("firstPageType", EntityOperator.EQUALS, firstPageSetting_gv.getString("firstPageType")));

                    if (andExprs.size() > 0) {
                        mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                    }
                    EntityListIterator pli=delegator.find("FirstPageSetting",mainCond,null,null,null,null);
                    List<GenericValue> fpsListForSeq = pli.getCompleteList();
                    if(UtilValidate.isNotEmpty(fpsListForSeq)){

                        for(GenericValue curFirstPageSettingInfo:fpsListForSeq){
                            curFirstPageSettingInfo.set("sequence",curFirstPageSettingInfo.getLong("sequence")+1);
                            curFirstPageSettingInfo.store();
                        }
                    }
                    firstPageSetting_gv.set("sequence",Long.valueOf(sequence));
                    firstPageSetting_gv.store();
                }
            }
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }

    /**
     * 根据ID删除firstPageSetting可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> firstPageSettingDel(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String)context.get("ids");
        //转换成list
        List idList = FastList.newInstance();
        for(String id : ids.split(",")){
        	idList.add(id);
        }
        //编辑where条件
        EntityCondition mainCond = EntityCondition.makeCondition("firstPageSettingId", EntityOperator.IN,idList);
        try {

            EntityListIterator pli=delegator.find("FirstPageSetting",mainCond,null,null,null,null);
            List<GenericValue> firstPageSettingList = pli.getCompleteList();
            for(GenericValue firstPageSettingInfo:firstPageSettingList){
                if(UtilValidate.isNotEmpty(firstPageSettingInfo)){
                    firstPageSettingInfo.setString("status","N");
                    firstPageSettingInfo.store();
                }
            }
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }

    /*************************************推荐商品***********************************/
    /**
     * 推荐商品首页设置列表查询
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getProdRecommendList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> firstPageList = FastList.newInstance();

        //总记录数
        int firstPageListSize = 0;
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

        String productName = (String) context.get("productName"); // 商品名称
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("PR","ProductRecommend");
        dynamicView.addAlias("PR","recommendId");
        dynamicView.addAlias("PR","productId");
        dynamicView.addAlias("PR","sequenceId");
        dynamicView.addAlias("PR","isAllWebSite");
        dynamicView.addAlias("PR","status");


        dynamicView.addMemberEntity("P","Product");
        dynamicView.addAlias("P","productName");
        dynamicView.addAlias("P","productId");
        dynamicView.addAlias("P","brandId");
        dynamicView.addViewLink("PR","P", Boolean.TRUE,ModelKeyMap.makeKeyMapList("productId", "productId"));

        dynamicView.addMemberEntity("B","ProductBrand");
        dynamicView.addAlias("B","brandName");
        dynamicView.addAlias("B","productBrandId");
        dynamicView.addViewLink("P","B", Boolean.TRUE,ModelKeyMap.makeKeyMapList("brandId", "productBrandId"));


//        dynamicView.addMemberEntity("PWS","ProductRecommendWebSite");
//        dynamicView.addAlias("PWS","recommendId");
//        dynamicView.addAlias("PWS","webSiteId");
//        dynamicView.addViewLink("PR","PWS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("recommendId", "recommendId"));
//
//        dynamicView.addMemberEntity("WS","WebSite");
//        dynamicView.addAlias("WS","webSiteId");
//        dynamicView.addAlias("WS","siteName");
//        dynamicView.addViewLink("PWS","WS", Boolean.TRUE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));


        fieldsToSelect.add("recommendId");
        fieldsToSelect.add("productId");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("isAllWebSite");

        fieldsToSelect.add("productName");
        fieldsToSelect.add("brandId");
        fieldsToSelect.add("brandName");
        fieldsToSelect.add("status");




        // 商品名称
        if(UtilValidate.isNotEmpty(productName)){
              filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("productName"), EntityOperator.LIKE, EntityFunction.UPPER("%"+productName+"%")));
        }
        filedExprs.add(EntityCondition.makeCondition("status", "Y"));

        //排序字段名称
        String sortField = "sequenceId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String)context.get("sortType");
        }

//        orderBy.add(sortType+sortField);
        orderBy.add("sequenceId");

        //添加where条件
        if (filedExprs.size() > 0){
            mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;

            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            GenericValue curProductInfo=null;
            GenericValue curProductStoreInfo=null;
            GenericValue curProductBrandInfo=null;
            GenericValue curStoreInfo=null;
            List <GenericValue> curProductRecomendList=FastList.newInstance();
            curProductRecomendList=pli.getPartialList(lowIndex, viewSize);
            for(GenericValue gv : curProductRecomendList){
                String recommendId = gv.getString("recommendId");
                String productId = gv.getString("productId");
                String sequenceId = gv.getString("sequenceId");
                String isAllWebSite = gv.getString("isAllWebSite");
                String curProductName = gv.getString("productName");
                String brandName = gv.getString("brandName");

                Map map = FastMap.newInstance();
                map.put("recommendId", recommendId);
                map.put("productId", productId);
                map.put("sequenceId", sequenceId);
                map.put("isAllWebSite", isAllWebSite);
                map.put("productName", curProductName);
                map.put("brandName", brandName);

                curProductInfo=null;
                curProductStoreInfo=null;
                curProductBrandInfo=null;
                curStoreInfo=null;

                // 根据商品编码取得推荐商品
//                curProductInfo= delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
//                if(UtilValidate.isNotEmpty(curProductInfo)){
//                    map.put("productInfo", curProductInfo);
//                    map.put("productName", curProductInfo.getString("productName"));
//                    map.put("productId", curProductInfo.getString("productId"));
//                    if(UtilValidate.isNotEmpty(curProductInfo.getString("brandId"))){
//                        GenericValue productBrandInfo= delegator.findByPrimaryKey("ProductBrand", UtilMisc.toMap("productBrandId", curProductInfo.getString("brandId")));
//                        if(UtilValidate.isNotEmpty(productBrandInfo)){
//                            map.put("brandName", productBrandInfo.getString("brandName"));
//                        }
//                    }
//                }
                // 根据商品编码取得商品店铺编码
                List<GenericValue> productStoreInfos= delegator.findByAnd("ProductStoreProductView", UtilMisc.toMap("productId", productId));
                if(UtilValidate.isNotEmpty(productStoreInfos)){
                    curProductStoreInfo=EntityUtil.getFirst(productStoreInfos);
                    if(UtilValidate.isNotEmpty(curProductStoreInfo)){
                        map.put("productStoreInfo", curProductStoreInfo);
                        map.put("storeName", curProductStoreInfo.getString("storeName"));
                    }
                }


                //获取站点名称
                String webSiteName = "";
                if("1".equals(isAllWebSite)){
                    //动态view
                    DynamicViewEntity webSite_dv = new DynamicViewEntity();
                    webSite_dv.addMemberEntity("PWS","ProductRecommendWebSite");
                    webSite_dv.addAlias("PWS","recommendId");
                    webSite_dv.addAlias("PWS","webSiteId");

                    webSite_dv.addMemberEntity("WS","WebSite");
                    webSite_dv.addAlias("WS","webSiteId");
                    webSite_dv.addAlias("WS","siteName");
                    webSite_dv.addViewLink("PWS","WS", Boolean.FALSE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));

                    List<String> ws_fieldsToSelect = FastList.newInstance();
                    ws_fieldsToSelect.add("recommendId");
                    ws_fieldsToSelect.add("webSiteId");
                    ws_fieldsToSelect.add("siteName");

                    //编辑where条件
                    EntityCondition ws_whereCond = EntityCondition.makeCondition("recommendId", EntityOperator.EQUALS,recommendId);

                    try {
                        //查询的数据Iterator
                        EntityListIterator ws_pli = delegator.findListIteratorByCondition(webSite_dv, ws_whereCond, null, ws_fieldsToSelect, null, findOpts);
                        for(int i=0;i<ws_pli.getCompleteList().size();i++){
                            String siteName = ws_pli.getCompleteList().get(i).getString("siteName");
                            if(i == 0){
                                webSiteName += siteName;
                            }else{
                                webSiteName += "，"+siteName;
                            }
                        }
                        //关闭pli
                        ws_pli.close();
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
                    }

                }else{
                    webSiteName = "全部站点";
                }
                map.put("webSiteName", webSiteName);
                firstPageList.add(map);
            }

            // 获取总记录数
            firstPageListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > firstPageListSize) {
                highIndex = firstPageListSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }


        //返回的参数
        result.put("firstPageList",firstPageList);
        result.put("totalSize", Integer.valueOf(firstPageListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }


    /**
     * 推荐商品新增
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> prodRecommendAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String ids = (String)context.get("ids");

        String[] curIds = null;// 目标编码数组
        List<String> idList = FastList.newInstance();
        if(UtilValidate.isNotEmpty(ids)){
            for(String id : ids.split(",")){
                idList.add(id);
            }
        }
        List<String> curIdList = FastList.newInstance();// 插入用编码列表
        List<String> curErrIdList = FastList.newInstance();// 重复的编码列表
        Boolean chk=true;
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy=FastList.newInstance();
        orderBy.add("sequenceId");
        try {
            // 重复编码的过滤
            List<GenericValue> prodRecommendInfos = delegator.findByAnd("ProductRecommend",UtilMisc.toMap("status","Y"),orderBy);
            if(UtilValidate.isNotEmpty(idList)){
                for(String curId:idList){
                    if(UtilValidate.isNotEmpty(prodRecommendInfos)) {
                        chk=true;
                        for (GenericValue fpsInfo : prodRecommendInfos) {
                            String fpsId=fpsInfo.getString("productId");
                            if(curId.equals(fpsId)){
                                curErrIdList.add(curId);
                                chk=false;
                                break;
                            }
                        }
                        if(chk) {
                            curIdList.add(curId);
                        }
                    }else{
                        curIdList.add(curId);
                    }
                }
            }

            Long curSequenceNum=Long.valueOf("1");
            if(UtilValidate.isNotEmpty(prodRecommendInfos)){
                String curSequenceId =prodRecommendInfos.get(prodRecommendInfos.size()-1).getString("sequenceId");
                curSequenceNum=Long.valueOf(curSequenceId)+Long.valueOf("1");
            }

            if(UtilValidate.isNotEmpty(curIdList)){
                String curId="";
                for(int i=0;i<curIdList.size();i++){

                    curId="";
                    curId=curIdList.get(i);
                    String createRecommendId = delegator.getNextSeqId("ProductRecommend");
                    //新增一条ProductRecommend记录
                    GenericValue productRecommend_gv = delegator.makeValue("ProductRecommend", UtilMisc.toMap("recommendId",createRecommendId));
                    productRecommend_gv.setString("productId",curId);
                    productRecommend_gv.setString("sequenceId", curSequenceNum.toString());
                    productRecommend_gv.setString("isAllWebSite",isAllWebSite);
                    productRecommend_gv.setString("status", "Y");
                    productRecommend_gv.create();

                    if(UtilValidate.isNotEmpty(webSite)){
                        for(String ws_id : webSite.split(",")){
                            GenericValue productRecommendWebSite = null;
                            Map<String, Object> productRecommendWebSiteMap=FastMap.newInstance();
                            productRecommendWebSiteMap.put("recommendId", createRecommendId);
                            productRecommendWebSiteMap.put("webSiteId", ws_id);
                            productRecommendWebSite = delegator.makeValue("ProductRecommendWebSite",productRecommendWebSiteMap);
                            // 创建表
                            productRecommendWebSite.create();
                        }
                    }
                    curSequenceNum++;
                }

            }
            //////////////////////////////////////////////////////
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据ID删除推荐商品信息可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> prodRecommendDel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String)context.get("ids");
        //转换成list
        List idList = FastList.newInstance();
        for(String id : ids.split(",")){
            idList.add(id);
        }
        //编辑where条件
        EntityCondition mainCond = EntityCondition.makeCondition("recommendId", EntityOperator.IN,idList);
        try {

            EntityListIterator pli=delegator.find("ProductRecommend",mainCond,null,null,null,null);
            List<GenericValue> productRecommendList = pli.getCompleteList();
            for(GenericValue productRecommendInfo:productRecommendList){
                if(UtilValidate.isNotEmpty(productRecommendInfo)){
                    productRecommendInfo.setString("status","N");
                    productRecommendInfo.store();
                }
            }
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据编码取得推荐商品信息
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> prodRecommendEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String recommendId = (String)context.get("recommendId");
        try {
            //根据首页设置编码取得一条firstPageSetting信息
            GenericValue productRecommend_gv = delegator.findByPrimaryKey("ProductRecommend", UtilMisc.toMap("recommendId",recommendId));

            result.put("recommendId",productRecommend_gv.get("recommendId"));
            result.put("productId",productRecommend_gv.get("productId"));
            result.put("sequenceId",productRecommend_gv.get("sequenceId"));
            result.put("isAllWebSite", productRecommend_gv.get("isAllWebSite"));

            //获取站点
            String ws_arr="";
            List<GenericValue> ws_list = delegator.findByAnd("ProductRecommendWebSite",UtilMisc.toMap("recommendId", recommendId));
            for(int i=0;i<ws_list.size();i++){
                if(i != ws_list.size()-1){
                    ws_arr += ws_list.get(i).get("webSiteId")+",";
                }else{
                    ws_arr += ws_list.get(i).get("webSiteId");
                }
            }
            result.put("webSite",ws_arr);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 推荐商品编辑处理
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> prodRecommendEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String recommendId = (String)context.get("recommendId");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String sequenceId = (String)context.get("sequenceId");
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        try {
            //取得一条推荐商品记录
            GenericValue productRecommend_gv = delegator.findByPrimaryKey("ProductRecommend", UtilMisc.toMap("recommendId",recommendId));
            if(UtilValidate.isNotEmpty(productRecommend_gv)){
                if(UtilValidate.isNotEmpty(sequenceId)){
                    if(!sequenceId.equals(productRecommend_gv.getString("sequenceId"))){
                        List<GenericValue> pRecommendHasSeqInfos = delegator.findByAnd("ProductRecommend",UtilMisc.toMap("sequenceId",sequenceId,"status","Y"));
                        if(UtilValidate.isNotEmpty(pRecommendHasSeqInfos)){
                            andExprs.add(EntityCondition.makeCondition("sequenceId", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceId));
                            andExprs.add(EntityCondition.makeCondition("recommendId", EntityOperator.NOT_EQUAL, recommendId));
                            if (andExprs.size() > 0) {
                                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                                //去除重复数据
                                //EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
                                EntityListIterator pli=delegator.find("ProductRecommend",mainCond,null,null,null,null);
                                List<GenericValue> fpsListForSeq = pli.getCompleteList();
                                if(UtilValidate.isNotEmpty(fpsListForSeq)){
                                    for(GenericValue curProductRecommendInfo:fpsListForSeq){
                                        String curSequenceId =curProductRecommendInfo.getString("sequenceId");
                                        Long curSequenceNum=Long.valueOf(curSequenceId)+Long.valueOf("1");
                                        curProductRecommendInfo.setString("sequenceId",curSequenceNum.toString());
                                        curProductRecommendInfo.store();
                                    }
                                }
                            }
                        }
                    }

                    productRecommend_gv.setString("sequenceId",sequenceId);
                    productRecommend_gv.setString("isAllWebSite",isAllWebSite);
                    productRecommend_gv.store();
                    if(UtilValidate.isNotEmpty(webSite)){
                        //删除该banner的所有站点
                        delegator.removeByAnd("ProductRecommendWebSite", UtilMisc.toMap("recommendId",recommendId));
                        for(String ws_id : webSite.split(",")){
                            GenericValue productRecommendWebSite = null;
                            Map<String, Object> productRecommendWebSiteMap=FastMap.newInstance();
                            productRecommendWebSiteMap.put("recommendId", recommendId);
                            productRecommendWebSiteMap.put("webSiteId", ws_id);
                            productRecommendWebSite = delegator.makeValue("ProductRecommendWebSite",productRecommendWebSiteMap);
                            // 创建表
                            productRecommendWebSite.create();
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*************************************热门品牌***********************************/

    /**
     * 热门品牌首页设置列表查询
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getBrandRecommendList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> firstPageList = FastList.newInstance();
        //总记录数
        int firstPageListSize = 0;
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

        String brandName = (String) context.get("brandName"); // 品牌名称
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("BR","BrandRecommend");
        dynamicView.addAlias("BR","recommendId");
        dynamicView.addAlias("BR","brandId");
        dynamicView.addAlias("BR","sequenceId");
        dynamicView.addAlias("BR","isAllWebSite");
        dynamicView.addAlias("BR","status");


        dynamicView.addMemberEntity("B","ProductBrand");
        dynamicView.addAlias("B","brandName");
        dynamicView.addAlias("B","productBrandId");
        dynamicView.addAlias("B","contentId");
        dynamicView.addAlias("B","brandNameAlias");
        dynamicView.addViewLink("BR","B", Boolean.TRUE,ModelKeyMap.makeKeyMapList("brandId", "productBrandId"));

        fieldsToSelect.add("recommendId");
        fieldsToSelect.add("brandId");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("isAllWebSite");

        fieldsToSelect.add("brandName");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("brandNameAlias");
        fieldsToSelect.add("status");

        // 商品名称
        if(UtilValidate.isNotEmpty(brandName)){
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("brandName"), EntityOperator.LIKE, EntityFunction.UPPER("%"+brandName+"%")));
        }
        filedExprs.add(EntityCondition.makeCondition("status", "Y"));

        //排序字段名称
        String sortField = "sequenceId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String)context.get("sortType");
        }
//        orderBy.add(sortType+sortField);
        orderBy.add("sequenceId");

        //添加where条件
        if (filedExprs.size() > 0){
            mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            GenericValue curbranductInfo=null;
            GenericValue curbranductStoreInfo=null;
            GenericValue curbranductBrandInfo=null;
            GenericValue curStoreInfo=null;
            List <GenericValue> curbranductRecomendList=FastList.newInstance();
            curbranductRecomendList=pli.getPartialList(lowIndex, viewSize);
            for(GenericValue gv : curbranductRecomendList){
                String recommendId = gv.getString("recommendId");
                String brandId = gv.getString("brandId");
                String sequenceId = gv.getString("sequenceId");
                String isAllWebSite = gv.getString("isAllWebSite");
                String curbrandName = gv.getString("brandName");

                String contentId = gv.getString("contentId");
                String brandNameAlias = gv.getString("brandNameAlias");

                Map map = FastMap.newInstance();
                map.put("recommendId", recommendId);
                map.put("brandId", brandId);
                map.put("sequenceId", sequenceId);
                map.put("isAllWebSite", isAllWebSite);
                map.put("brandName", curbrandName);
                map.put("contentId", contentId);
                map.put("brandNameAlias", brandNameAlias);

                curbranductInfo=null;
                curbranductStoreInfo=null;
                curbranductBrandInfo=null;
                curStoreInfo=null;


                //获取站点名称
                String webSiteName = "";
                if("1".equals(isAllWebSite)){
                    //动态view
                    DynamicViewEntity webSite_dv = new DynamicViewEntity();
                    webSite_dv.addMemberEntity("BWS","BrandRecommendWebSite");
                    webSite_dv.addAlias("BWS","recommendId");
                    webSite_dv.addAlias("BWS","webSiteId");

                    webSite_dv.addMemberEntity("WS","WebSite");
                    webSite_dv.addAlias("WS","webSiteId");
                    webSite_dv.addAlias("WS","siteName");
                    webSite_dv.addViewLink("BWS","WS", Boolean.FALSE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));

                    List<String> ws_fieldsToSelect = FastList.newInstance();
                    ws_fieldsToSelect.add("recommendId");
                    ws_fieldsToSelect.add("webSiteId");
                    ws_fieldsToSelect.add("siteName");

                    //编辑where条件
                    EntityCondition ws_whereCond = EntityCondition.makeCondition("recommendId", EntityOperator.EQUALS,recommendId);

                    try {
                        //查询的数据Iterator
                        EntityListIterator ws_pli = delegator.findListIteratorByCondition(webSite_dv, ws_whereCond, null, ws_fieldsToSelect, null, findOpts);
                        for(int i=0;i<ws_pli.getCompleteList().size();i++){
                            String siteName = ws_pli.getCompleteList().get(i).getString("siteName");
                            if(i == 0){
                                webSiteName += siteName;
                            }else{
                                webSiteName += "，"+siteName;
                            }
                        }
                        //关闭pli
                        ws_pli.close();
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
                    }

                }else{
                    webSiteName = "全部站点";
                }
                map.put("webSiteName", webSiteName);
                firstPageList.add(map);
            }

            // 获取总记录数
            firstPageListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > firstPageListSize) {
                highIndex = firstPageListSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }


        //返回的参数
        result.put("firstPageList",firstPageList);
        result.put("totalSize", Integer.valueOf(firstPageListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }


    /**
     * 热门品牌新增
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> brandRecommendAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String ids = (String)context.get("ids");

        String[] curIds = null;// 目标编码数组
        List<String> idList = FastList.newInstance();
        if(UtilValidate.isNotEmpty(ids)){
            for(String id : ids.split(",")){
                idList.add(id);
            }
        }
        List<String> curIdList = FastList.newInstance();// 插入用编码列表
        List<String> curErrIdList = FastList.newInstance();// 重复的编码列表
        Boolean chk=true;
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy=FastList.newInstance();
        orderBy.add("sequenceId");
        try {
            // 重复编码的过滤
            List<GenericValue> brandRecommendInfos = delegator.findByAnd("BrandRecommend",UtilMisc.toMap("status","Y"),orderBy);
            if(UtilValidate.isNotEmpty(idList)){
                for(String curId:idList){
                    if(UtilValidate.isNotEmpty(brandRecommendInfos)) {
                        chk=true;
                        for (GenericValue fpsInfo : brandRecommendInfos) {
                            String fpsId=fpsInfo.getString("brandId");
                            if(curId.equals(fpsId)){
                                curErrIdList.add(curId);
                                chk=false;
                                break;
                            }
                        }
                        if(chk) {
                            curIdList.add(curId);
                        }
                    }else{
                        curIdList.add(curId);
                    }
                }
            }

            Long curSequenceNum=Long.valueOf("1");
            if(UtilValidate.isNotEmpty(brandRecommendInfos)){
                String curSequenceId =brandRecommendInfos.get(brandRecommendInfos.size()-1).getString("sequenceId");
                curSequenceNum=Long.valueOf(curSequenceId)+Long.valueOf("1");
            }

            if(UtilValidate.isNotEmpty(curIdList)){
                String curId="";
                for(int i=0;i<curIdList.size();i++){
//                    curSequenceNum=curSequenceNum+i;
                    curId="";
                    curId=curIdList.get(i);
                    String createRecommendId = delegator.getNextSeqId("BrandRecommend");
                    //新增一条branductRecommend记录
                    GenericValue brandRecommend_gv = delegator.makeValue("BrandRecommend", UtilMisc.toMap("recommendId",createRecommendId));
                    brandRecommend_gv.setString("brandId",curId);
                    brandRecommend_gv.setString("sequenceId", curSequenceNum.toString());
                    brandRecommend_gv.setString("isAllWebSite",isAllWebSite);
                    brandRecommend_gv.setString("status", "Y");
                    brandRecommend_gv.create();
                    if(UtilValidate.isNotEmpty(webSite)){
                        for(String ws_id : webSite.split(",")){
                            GenericValue brandRecommendWebSite = null;
                            Map<String, Object> brandRecommendWebSiteMap=FastMap.newInstance();
                            brandRecommendWebSiteMap.put("recommendId", createRecommendId);
                            brandRecommendWebSiteMap.put("webSiteId", ws_id);
                            brandRecommendWebSite = delegator.makeValue("BrandRecommendWebSite",brandRecommendWebSiteMap);
                            // 创建表
                            brandRecommendWebSite.create();
                        }
                    }
                    curSequenceNum++;
                }

            }
            //////////////////////////////////////////////////////
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据ID删除热门品牌信息可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> brandRecommendDel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String)context.get("ids");
        //转换成list
        List idList = FastList.newInstance();
        for(String id : ids.split(",")){
            idList.add(id);
        }
        //编辑where条件
        EntityCondition mainCond = EntityCondition.makeCondition("recommendId", EntityOperator.IN,idList);
        try {

            EntityListIterator pli=delegator.find("BrandRecommend",mainCond,null,null,null,null);
            List<GenericValue> brandRecommendList = pli.getCompleteList();
            for(GenericValue brandRecommendInfo:brandRecommendList){
                if(UtilValidate.isNotEmpty(brandRecommendInfo)){
                    brandRecommendInfo.setString("status","N");
                    brandRecommendInfo.store();
                }
            }
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据编码取得热门品牌信息
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> brandRecommendEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String recommendId = (String)context.get("recommendId");
        try {
            //根据首页设置编码取得一条firstPageSetting信息
            GenericValue branductRecommend_gv = delegator.findByPrimaryKey("BrandRecommend", UtilMisc.toMap("recommendId",recommendId));

            result.put("recommendId",branductRecommend_gv.get("recommendId"));
            result.put("brandId",branductRecommend_gv.get("brandId"));
            result.put("sequenceId",branductRecommend_gv.get("sequenceId"));
            result.put("isAllWebSite", branductRecommend_gv.get("isAllWebSite"));

            //获取站点
            String ws_arr="";
            List<GenericValue> ws_list = delegator.findByAnd("BrandRecommendWebSite",UtilMisc.toMap("recommendId", recommendId));
            for(int i=0;i<ws_list.size();i++){
                if(i != ws_list.size()-1){
                    ws_arr += ws_list.get(i).get("webSiteId")+",";
                }else{
                    ws_arr += ws_list.get(i).get("webSiteId");
                }
            }
            result.put("webSite",ws_arr);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 热门品牌编辑处理
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> brandRecommendEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String recommendId = (String)context.get("recommendId");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String sequenceId = (String)context.get("sequenceId");
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        try {
            //取得一条热门品牌记录
            GenericValue brandRecommend_gv = delegator.findByPrimaryKey("BrandRecommend", UtilMisc.toMap("recommendId",recommendId));
            if(UtilValidate.isNotEmpty(brandRecommend_gv)){
                if(UtilValidate.isNotEmpty(sequenceId)){
                    List<GenericValue> pRecommendHasSeqInfos = delegator.findByAnd("BrandRecommend",UtilMisc.toMap("sequenceId",sequenceId,"status","Y"));
                    if(UtilValidate.isNotEmpty(pRecommendHasSeqInfos)){
                        if(!sequenceId.equals(brandRecommend_gv.getString("sequenceId"))) {
                            andExprs.add(EntityCondition.makeCondition("sequenceId", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceId));
                            andExprs.add(EntityCondition.makeCondition("recommendId", EntityOperator.NOT_EQUAL, recommendId));
                            if (andExprs.size() > 0) {
                                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                                //去除重复数据
                                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, false);
                                EntityListIterator pli = delegator.find("BrandRecommend", mainCond, null, null, null, findOpts);
                                List<GenericValue> fpsListForSeq = pli.getCompleteList();
                                if (UtilValidate.isNotEmpty(fpsListForSeq)) {
                                    for (GenericValue curbranductRecommendInfo : fpsListForSeq) {
                                        String curSequenceId = curbranductRecommendInfo.getString("sequenceId");
                                        Long curSequenceNum = Long.valueOf(curSequenceId) + Long.valueOf("1");
                                        curbranductRecommendInfo.setString("sequenceId", curSequenceNum.toString());
                                        curbranductRecommendInfo.store();
                                    }
                                }
                                pli.close();
                            }
                        }
                    }
                    brandRecommend_gv.setString("sequenceId",sequenceId);
                    brandRecommend_gv.setString("isAllWebSite",isAllWebSite);
                    brandRecommend_gv.store();
                    if(UtilValidate.isNotEmpty(webSite)){
                        //删除该banner的所有站点
                        delegator.removeByAnd("BrandRecommendWebSite", UtilMisc.toMap("recommendId",recommendId));
                        for(String ws_id : webSite.split(",")){
                            GenericValue brandRecommendWebSite = null;
                            Map<String, Object> brandRecommendWebSiteMap=FastMap.newInstance();
                            brandRecommendWebSiteMap.put("recommendId", recommendId);
                            brandRecommendWebSiteMap.put("webSiteId", ws_id);
                            brandRecommendWebSite = delegator.makeValue("BrandRecommendWebSite",brandRecommendWebSiteMap);
                            // 创建表
                            brandRecommendWebSite.create();
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    /*************************************热门店铺***********************************/
    /**
     * 热门店铺首页设置列表查询
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getStoreRecommendList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<Map> firstPageList = FastList.newInstance();
        //总记录数
        int firstPageListSize = 0;
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

        String storeName = (String) context.get("storeName"); // 店铺名称
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("PS","ProductStoreRecommend");
        dynamicView.addAlias("PS","recommendId");
        dynamicView.addAlias("PS","productStoreId");
        dynamicView.addAlias("PS","sequenceId");
        dynamicView.addAlias("PS","isAllWebSite");
        dynamicView.addAlias("PS","status");

        dynamicView.addMemberEntity("PG","PartyGroup");
        dynamicView.addAlias("PG","partyId");
        dynamicView.addAlias("PG","partyName");
        dynamicView.addAlias("PG","productStoreId");
        dynamicView.addViewLink("PS","PG", Boolean.TRUE,ModelKeyMap.makeKeyMapList("productStoreId", "productStoreId"));


        dynamicView.addMemberEntity("PB","PartyBusiness");
//        dynamicView.addAlias("PB","partyId");
        dynamicView.addAlias("PB","description");
        dynamicView.addAlias("PB","logoImg");
        dynamicView.addViewLink("PG","PB", Boolean.TRUE,ModelKeyMap.makeKeyMapList("partyId", "partyId"));

        fieldsToSelect.add("recommendId");
        fieldsToSelect.add("productStoreId");
        fieldsToSelect.add("sequenceId");
        fieldsToSelect.add("isAllWebSite");
        fieldsToSelect.add("partyName");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("logoImg");
        fieldsToSelect.add("status");

        // 店铺名称(在这里用商家名称)
        if(UtilValidate.isNotEmpty(storeName)){
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyName"), EntityOperator.LIKE, EntityFunction.UPPER("%"+storeName+"%")));
        }
        filedExprs.add(EntityCondition.makeCondition("status", "Y"));

        //排序字段名称
        String sortField = "sequenceId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
            sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
            sortType = (String)context.get("sortType");
        }
//        orderBy.add(sortType+sortField);
        orderBy.add("sequenceId");

        //添加where条件
        if (filedExprs.size() > 0){
            mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }

        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            // 获取分页所需的记录集合
            List <GenericValue> curStoreRecomendList=FastList.newInstance();
            curStoreRecomendList=pli.getPartialList(lowIndex, viewSize);
            for(GenericValue gv : curStoreRecomendList){
                String recommendId = gv.getString("recommendId");
                String productStoreId = gv.getString("productStoreId");
                String sequenceId = gv.getString("sequenceId");
                String isAllWebSite = gv.getString("isAllWebSite");
                String curStoreName = gv.getString("partyName");
                String logoImg = gv.getString("logoImg");
                String partyId = gv.getString("partyId");

                Map map = FastMap.newInstance();
                map.put("recommendId", recommendId);
                map.put("productStoreId", productStoreId);
                map.put("sequenceId", sequenceId);
                map.put("isAllWebSite", isAllWebSite);
                map.put("storeName", curStoreName);
                map.put("logoImg", logoImg);
                map.put("partyId", partyId);

                //获取站点名称
                String webSiteName = "";
                if("1".equals(isAllWebSite)){
                    //动态view
                    DynamicViewEntity webSite_dv = new DynamicViewEntity();
                    webSite_dv.addMemberEntity("PWS","ProductStoreRecommendWebSite");
                    webSite_dv.addAlias("PWS","recommendId");
                    webSite_dv.addAlias("PWS","webSiteId");

                    webSite_dv.addMemberEntity("WS","WebSite");
                    webSite_dv.addAlias("WS","webSiteId");
                    webSite_dv.addAlias("WS","siteName");
                    webSite_dv.addViewLink("PWS","WS", Boolean.FALSE,ModelKeyMap.makeKeyMapList("webSiteId", "webSiteId"));

                    List<String> ws_fieldsToSelect = FastList.newInstance();
                    ws_fieldsToSelect.add("recommendId");
                    ws_fieldsToSelect.add("webSiteId");
                    ws_fieldsToSelect.add("siteName");

                    //编辑where条件
                    EntityCondition ws_whereCond = EntityCondition.makeCondition("recommendId", EntityOperator.EQUALS,recommendId);

                    try {
                        //查询的数据Iterator
                        EntityListIterator ws_pli = delegator.findListIteratorByCondition(webSite_dv, ws_whereCond, null, ws_fieldsToSelect, null, findOpts);
                        for(int i=0;i<ws_pli.getCompleteList().size();i++){
                            String siteName = ws_pli.getCompleteList().get(i).getString("siteName");
                            if(i == 0){
                                webSiteName += siteName;
                            }else{
                                webSiteName += "，"+siteName;
                            }
                        }
                        //关闭pli
                        ws_pli.close();
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
                    }

                }else{
                    webSiteName = "全部站点";
                }
                map.put("webSiteName", webSiteName);
                firstPageList.add(map);
            }

            // 获取总记录数
            firstPageListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > firstPageListSize) {
                highIndex = firstPageListSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }


        //返回的参数
        result.put("firstPageList",firstPageList);
        result.put("totalSize", Integer.valueOf(firstPageListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }


    /**
     * 热门店铺新增
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> storeRecommendAdd(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String ids = (String)context.get("ids");

        String[] curIds = null;// 目标编码数组
        List<String> idList = FastList.newInstance();
        if(UtilValidate.isNotEmpty(ids)){
            for(String id : ids.split(",")){
                idList.add(id);
            }
        }
        List<String> curIdList = FastList.newInstance();// 插入用编码列表
        List<String> curErrIdList = FastList.newInstance();// 重复的编码列表
        Boolean chk=true;
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        List<String> orderBy=FastList.newInstance();
        orderBy.add("sequenceId");
        try {
            // 重复编码的过滤
            List<GenericValue> storeRecommendInfos = delegator.findByAnd("ProductStoreRecommend",UtilMisc.toMap("status","Y"),orderBy);
            if(UtilValidate.isNotEmpty(idList)){
                for(String curId:idList){
                    if(UtilValidate.isNotEmpty(storeRecommendInfos)) {
                        chk=true;
                        for (GenericValue fpsInfo : storeRecommendInfos) {
                            String fpsId=fpsInfo.getString("productStoreId");
                            if(curId.equals(fpsId)){
                                curErrIdList.add(curId);
                                chk=false;
                                break;
                            }
                        }
                        if(chk) {
                            curIdList.add(curId);
                        }
                    }else{
                        curIdList.add(curId);
                    }
                }
            }

            Long curSequenceNum=Long.valueOf("1");
            if(UtilValidate.isNotEmpty(storeRecommendInfos)){
                String curSequenceId =storeRecommendInfos.get(storeRecommendInfos.size()-1).getString("sequenceId");
                curSequenceNum=Long.valueOf(curSequenceId)+Long.valueOf("1");
            }

            if(UtilValidate.isNotEmpty(curIdList)){
                String curId="";
                for(int i=0;i<curIdList.size();i++){
//                    curSequenceNum=curSequenceNum+i;
                    curId="";
                    curId=curIdList.get(i);
                    String createRecommendId = delegator.getNextSeqId("ProductStoreRecommend");
                    //新增一条storeuctRecommend记录
                    GenericValue storeRecommend_gv = delegator.makeValue("ProductStoreRecommend", UtilMisc.toMap("recommendId",createRecommendId));
                    storeRecommend_gv.setString("productStoreId",curId);
                    storeRecommend_gv.setString("sequenceId", curSequenceNum.toString());
                    storeRecommend_gv.setString("isAllWebSite",isAllWebSite);
                    storeRecommend_gv.setString("status", "Y");
                    storeRecommend_gv.create();
                    if(UtilValidate.isNotEmpty(webSite)){
                        for(String ws_id : webSite.split(",")){
                            GenericValue storeRecommendWebSite = null;
                            Map<String, Object> storeRecommendWebSiteMap=FastMap.newInstance();
                            storeRecommendWebSiteMap.put("recommendId", createRecommendId);
                            storeRecommendWebSiteMap.put("webSiteId", ws_id);
                            storeRecommendWebSite = delegator.makeValue("ProductStoreRecommendWebSite",storeRecommendWebSiteMap);
                            // 创建表
                            storeRecommendWebSite.create();
                        }
                    }
                    curSequenceNum++;
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据ID删除热门店铺信息可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> storeRecommendDel(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //获取ids参数
        String ids = (String)context.get("ids");
        //转换成list
        List idList = FastList.newInstance();
        for(String id : ids.split(",")){
            idList.add(id);
        }
        //编辑where条件
        EntityCondition mainCond = EntityCondition.makeCondition("recommendId", EntityOperator.IN,idList);
        try {

            EntityListIterator pli=delegator.find("ProductStoreRecommend",mainCond,null,null,null,null);
            List<GenericValue> storeRecommendList = pli.getCompleteList();
            for(GenericValue storeRecommendInfo:storeRecommendList){
                if(UtilValidate.isNotEmpty(storeRecommendInfo)){
                    storeRecommendInfo.setString("status","N");
                    storeRecommendInfo.store();
                }
            }
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 根据编码取得热门店铺信息
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> storeRecommendEditInit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String recommendId = (String)context.get("recommendId");
        try {
            //根据首页设置编码取得一条firstPageSetting信息
            GenericValue storeuctRecommend_gv = delegator.findByPrimaryKey("ProductStoreRecommend", UtilMisc.toMap("recommendId",recommendId));

            result.put("recommendId",storeuctRecommend_gv.get("recommendId"));
            result.put("productStoreId",storeuctRecommend_gv.get("productStoreId"));
            result.put("sequenceId",storeuctRecommend_gv.get("sequenceId"));
            result.put("isAllWebSite", storeuctRecommend_gv.get("isAllWebSite"));

            //获取站点
            String ws_arr="";
            List<GenericValue> ws_list = delegator.findByAnd("ProductStoreRecommendWebSite",UtilMisc.toMap("recommendId", recommendId));
            for(int i=0;i<ws_list.size();i++){
                if(i != ws_list.size()-1){
                    ws_arr += ws_list.get(i).get("webSiteId")+",";
                }else{
                    ws_arr += ws_list.get(i).get("webSiteId");
                }
            }
            result.put("webSite",ws_arr);

        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 热门店铺编辑处理
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> storeRecommendEdit(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String recommendId = (String)context.get("recommendId");
        String isAllWebSite = (String)context.get("isAllWebSite");
        String webSite = (String)context.get("webSite");
        String sequenceId = (String)context.get("sequenceId");
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        try {
            //取得一条热门店铺记录
            GenericValue storeRecommend_gv = delegator.findByPrimaryKey("ProductStoreRecommend", UtilMisc.toMap("recommendId",recommendId));
            if(UtilValidate.isNotEmpty(storeRecommend_gv)){
                if(UtilValidate.isNotEmpty(sequenceId)){
                    List<GenericValue> pRecommendHasSeqInfos = delegator.findByAnd("ProductStoreRecommend",UtilMisc.toMap("sequenceId",sequenceId,"status","Y"));
                    if(UtilValidate.isNotEmpty(pRecommendHasSeqInfos)){
                        if(!sequenceId.equals(storeRecommend_gv.getString("sequenceId"))) {
                            andExprs.add(EntityCondition.makeCondition("sequenceId", EntityOperator.GREATER_THAN_EQUAL_TO, sequenceId));
                            andExprs.add(EntityCondition.makeCondition("recommendId", EntityOperator.NOT_EQUAL, recommendId));
                            if (andExprs.size() > 0) {
                                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
                                EntityListIterator pli = delegator.find("ProductStoreRecommend", mainCond, null, null, null, null);
                                List<GenericValue> fpsListForSeq = pli.getCompleteList();
                                if (UtilValidate.isNotEmpty(fpsListForSeq)) {
                                    for (GenericValue curstoreuctRecommendInfo : fpsListForSeq) {
                                        String curSequenceId = curstoreuctRecommendInfo.getString("sequenceId");
                                        Long curSequenceNum = Long.valueOf(curSequenceId) + Long.valueOf("1");
                                        curstoreuctRecommendInfo.setString("sequenceId", curSequenceNum.toString());
                                        curstoreuctRecommendInfo.store();
                                    }
                                }
                                pli.close();
                            }
                        }
                    }
                    storeRecommend_gv.setString("sequenceId",sequenceId);
                    storeRecommend_gv.setString("isAllWebSite",isAllWebSite);
                    storeRecommend_gv.store();
                    if(UtilValidate.isNotEmpty(webSite)){
                        //删除该banner的所有站点
                        delegator.removeByAnd("ProductStoreRecommendWebSite", UtilMisc.toMap("recommendId",recommendId));
                        for(String ws_id : webSite.split(",")){
                            GenericValue storeRecommendWebSite = null;
                            Map<String, Object> storeRecommendWebSiteMap=FastMap.newInstance();
                            storeRecommendWebSiteMap.put("recommendId", recommendId);
                            storeRecommendWebSiteMap.put("webSiteId", ws_id);
                            storeRecommendWebSite = delegator.makeValue("ProductStoreRecommendWebSite",storeRecommendWebSiteMap);
                            // 创建表
                            storeRecommendWebSite.create();
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
}
