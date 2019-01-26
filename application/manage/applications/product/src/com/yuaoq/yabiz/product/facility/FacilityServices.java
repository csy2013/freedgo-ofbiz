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

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.ObjectType;
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
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


/**
 * 仓库service
 * @author 钱进 2015/12/18
 *
 */
public class FacilityServices {
    public static final String module = FacilityServices.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * 查询仓库列表
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getFacilityList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //当前用户登录信息  
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        //记录集合
        List<GenericValue> facilityList = FastList.newInstance();
        
        //总记录数
        int facilityListSize = 0;
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
        
        //排序字段名称
        String sortField = "facilityId";
        if (UtilValidate.isNotEmpty(context.get("sortField"))) {
        	sortField = (String)context.get("sortField");
        }
        //排序类型
        String sortType = "";
        if(UtilValidate.isNotEmpty(context.get("sortType"))) {
        	sortType = (String)context.get("sortType");
        }
        orderBy.add(sortType+sortField);
        
        //设置动态View
        dynamicView.addMemberEntity("F", "Facility");
        dynamicView.addAlias("F", "facilityId");
        dynamicView.addAlias("F", "facilityName");
        dynamicView.addAlias("F", "facilityInnerCode");
        dynamicView.addAlias("F", "ownerPartyId");
        dynamicView.addAlias("F", "description");
        dynamicView.addAlias("F", "capacity");
        fieldsToSelect.add("facilityId");
        fieldsToSelect.add("facilityName");
        fieldsToSelect.add("facilityInnerCode");
        fieldsToSelect.add("description");
        fieldsToSelect.add("capacity");
        
        //拼接where条件
		try {
			//根据用户登录ID获取用户所属组织
			Map partyIdFromMap = dispatcher.runSync("getPartyIdFrom", UtilMisc.toMap("partyIdTo",userLogin.get("partyId")));
			andExprs.add(EntityCondition.makeCondition("ownerPartyId", EntityOperator.EQUALS,partyIdFromMap.get("partyIdFrom")));
		} catch (GenericServiceException e1) {
			e1.printStackTrace();
		}
		//按仓库名称模糊查询
        if (UtilValidate.isNotEmpty(context.get("facilityName"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("facilityName"), EntityOperator.LIKE, EntityFunction.UPPER("%"+context.get("facilityName")+"%")));
        }
        //按仓库编号迷糊查询
        if (UtilValidate.isNotEmpty(context.get("facilityId"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("facilityId"), EntityOperator.LIKE, EntityFunction.UPPER("%"+context.get("facilityId")+"%")));
        }
        //按仓库内部编号模糊查询
        if (UtilValidate.isNotEmpty(context.get("facilityInnerCode"))) {
            andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("facilityInnerCode"), EntityOperator.LIKE, EntityFunction.UPPER("%"+context.get("facilityInnerCode")+"%")));
        }
        
        //添加where条件
        if (andExprs.size() > 0){
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
            facilityList = pli.getPartialList(lowIndex, viewSize);
            
            // 获取总记录数
            facilityListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > facilityListSize) {
                highIndex = facilityListSize;
            }

            //关闭 iterator
            pli.close();
        } catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
            Debug.logError(e, errMsg, module);
        }
        
        //返回的参数
        result.put("facilityList",facilityList);
        result.put("totalSize", Integer.valueOf(facilityListSize));
        result.put("highIndex", Integer.valueOf(highIndex));
        result.put("lowIndex", Integer.valueOf(lowIndex));
        return result;
    }
    
    
    /**
     * 根据ID删除仓库,可批量删除
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> deleteFacility(DispatchContext dctx, Map<String, ? extends Object> context) {
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
        EntityCondition mainCond = EntityCondition.makeCondition("facilityId", EntityOperator.IN,idList);
        
        try {
        	delegator.removeByCondition("FacilityCoverageArea", mainCond);
			delegator.removeByCondition("Facility", mainCond);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return result;
    }
    
    /**
     * 根据仓库ID获取产品数量
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getProductByFacilityId(DispatchContext dctx, Map<String, ? extends Object> context) {
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
        EntityCondition mainCond = EntityCondition.makeCondition("facilityId", EntityOperator.IN,idList);
        
        try {
        	Long product_count = delegator.findCountByCondition("ProductFacility", mainCond, null, null);
        	result.put("productCount", product_count);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return result;
    }
    
    /**
     * 仓库新增
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addFacility(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	Delegator delegator = dctx.getDelegator();
    	//当前用户登录信息  
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        
        try {
        	//参数仓库ID
            String facilityId = (String)context.get("facilityId");
            //新增一条仓库记录
        	GenericValue facility_gv = delegator.makeValue("Facility", UtilMisc.toMap("facilityId",facilityId));
        	facility_gv.setString("facilityName", (String)context.get("facilityName"));
        	facility_gv.setString("facilityInnerCode", (String)context.get("facilityInnerCode"));
        	facility_gv.setString("description", (String)context.get("description"));
        	facility_gv.setString("capacity", ((BigDecimal)context.get("capacity")).toString());
        	facility_gv.setString("facilityTypeId","WAREHOUSE");
        	//根据用户登录ID获取用户所属组织
        	Map partyIdFromMap = dispatcher.runSync("getPartyIdFrom", UtilMisc.toMap("partyIdTo",userLogin.get("partyId")));
        	if(UtilValidate.isNotEmpty(partyIdFromMap.get("partyIdFrom"))){
        		facility_gv.setString("ownerPartyId",partyIdFromMap.get("partyIdFrom").toString());
        	}
        	facility_gv.setString("defaultInventoryItemTypeId","NON_SERIAL_INV_ITEM");
        	facility_gv.create();
        	
        	//覆盖区域ID
        	String[] area_ids = ((String)context.get("facilityCoverageArea")).split(",");
        	for(String area_id : area_ids){
        		//根据地区ID，获取最大排序号
        		Long maxSeq = 0L;
        		//设置动态View
        		DynamicViewEntity dynamicView = new DynamicViewEntity();
                dynamicView.addMemberEntity("F", "FacilityCoverageArea");
                dynamicView.addAlias("F", "sequenceId","sequenceId","sequenceId",null,null,"max");
                dynamicView.addAlias("F", "geoId");
                
                EntityCondition whereCond =  EntityCondition.makeCondition("geoId", EntityOperator.EQUALS, area_id);
                try {
                    EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                            EntityFindOptions.CONCUR_READ_ONLY,   true);
                    //查询的数据Iterator
                    EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, whereCond, null, null, null, findOpts);
                    if(UtilValidate.isNotEmpty(pli.getCompleteList())){
                    	if(UtilValidate.isNotEmpty(EntityUtil.getFirst(pli.getCompleteList()).getLong("sequenceId"))){
                    		maxSeq = EntityUtil.getFirst(pli.getCompleteList()).getLong("sequenceId");
                    	}
                    }
                    //关闭pli
                    pli.close();
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
        		
        		//获取仓库覆盖范围表新的ID
                String fca_new_id = delegator.getNextSeqId("FacilityCoverageArea").toString();
            	//新增仓库覆盖范围记录
                GenericValue fca_gv = delegator.makeValue("FacilityCoverageArea", UtilMisc.toMap("facilityCoverageAreaId",fca_new_id));
                fca_gv.setString("facilityId",facilityId);
                fca_gv.setString("geoId",area_id);
                fca_gv.set("sequenceId",maxSeq + 1 );
                fca_gv.set("updateFlag","0");
                fca_gv.create();
            }
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
        
        return result;
    }
    
    /**
     * 仓库修改
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editFacility(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数仓库ID
        String facilityId = (String)context.get("facilityId");
        
        try {
            //根据ID获取仓库记录并修改
        	GenericValue facility_gv = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId",facilityId));
        	facility_gv.setString("facilityName", (String)context.get("facilityName"));
        	facility_gv.setString("facilityInnerCode", (String)context.get("facilityInnerCode"));
        	facility_gv.setString("description", (String)context.get("description"));
        	facility_gv.setString("capacity",((BigDecimal)context.get("capacity")).toString());
        	facility_gv.store();
        	
        	//覆盖区域ID
            if(UtilValidate.isNotEmpty(context.get("facilityCoverageArea"))) {
        	    String[] area_ids = ((String)context.get("facilityCoverageArea")).split(",");

                for (String area_id : area_ids) {
                    List<GenericValue> fca_list = delegator.findByAnd("FacilityCoverageArea", UtilMisc.toMap("facilityId", facilityId, "geoId", area_id));
                    if (UtilValidate.isEmpty(fca_list)) {
                        //根据地区ID，获取最大排序号
                        Long maxSeq = 0L;
                        //设置动态View
                        DynamicViewEntity dynamicView = new DynamicViewEntity();
                        dynamicView.addMemberEntity("F", "FacilityCoverageArea");
                        dynamicView.addAlias("F", "sequenceId", "sequenceId", "sequenceId", null, null, "max");
                        dynamicView.addAlias("F", "geoId");

                        EntityCondition whereCond = EntityCondition.makeCondition("geoId", EntityOperator.EQUALS, area_id);
                        try {
                            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                                    EntityFindOptions.CONCUR_READ_ONLY, true);
                            //查询的数据Iterator
                            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, whereCond, null, null, null, findOpts);
                            if (UtilValidate.isNotEmpty(pli.getCompleteList())) {
                                if (UtilValidate.isNotEmpty(EntityUtil.getFirst(pli.getCompleteList()).getLong("sequenceId"))) {
                                    maxSeq = EntityUtil.getFirst(pli.getCompleteList()).getLong("sequenceId");
                                }
                            }
                            //关闭pli
                            pli.close();
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }

                        //获取仓库覆盖范围表新的ID
                        String fca_new_id = delegator.getNextSeqId("FacilityCoverageArea").toString();
                        //新增仓库覆盖范围记录
                        GenericValue fca_gv = delegator.makeValue("FacilityCoverageArea", UtilMisc.toMap("facilityCoverageAreaId", fca_new_id));
                        fca_gv.setString("facilityId", facilityId);
                        fca_gv.setString("geoId", area_id);
                        fca_gv.set("sequenceId", maxSeq + 1);
                        fca_gv.set("updateFlag", "1");
                        fca_gv.create();
                    } else {
                        GenericValue fca_gv = EntityUtil.getFirst(fca_list);
                        fca_gv.set("updateFlag", "1");
                        fca_gv.store();
                    }
                }
            }
        	
        	//删除该仓库的所有updateFlag为0的数据
        	delegator.removeByAnd("FacilityCoverageArea", UtilMisc.toMap("facilityId",facilityId,"updateFlag","0"));
        	//重置该仓库的所有updateFlag为0
        	delegator.storeByCondition("FacilityCoverageArea",  UtilMisc.toMap("updateFlag","0"), EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId));
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return result;
    }
    
    /**
     * 根据ID获取仓库信息 
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getFacilityById(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        Map paramMap = FastMap.newInstance();
        //参数仓库ID
        String facilityId = (String)context.get("facilityId");
        
        try {
        	//根据ID获取仓库记录并修改
        	GenericValue gv = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", facilityId));
        	paramMap.put("facilityId",gv.getString("facilityId"));
        	paramMap.put("facilityName",gv.getString("facilityName"));
        	paramMap.put("facilityInnerCode",gv.getString("facilityInnerCode"));
        	paramMap.put("description",gv.getString("description"));
        	paramMap.put("capacity",gv.getString("capacity"));
        	
        	//根据仓库ID获取该仓库的覆盖范围
        	List<GenericValue> fcaList = delegator.findByAnd("FacilityCoverageArea", UtilMisc.toMap("facilityId", facilityId), null);
        	paramMap.put("fcaList",fcaList);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        result.put("paramMap",paramMap);
        return result;
    }
    
    /**
     * 跳转新增页面
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> goAddPage(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        
        //获取仓库表新的ID
        String facility_new_id = delegator.getNextSeqId("Facility").toString();
        //获取地区List
        result = getGeoList(dctx,context);
        result.put("facilityId", facility_new_id);
        return result;
    }
    
    /**
     * 获取地区列表
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getGeoList(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //地区集合
        List<GenericValue> geoList = FastList.newInstance();
        //查询条件
        EntityCondition cond1 = EntityCondition.makeCondition("pId", EntityOperator.LIKE,"CN-%");
        EntityCondition cond2 = EntityCondition.makeCondition("pId", EntityOperator.EQUALS,"CHN");
        EntityCondition whereCond = EntityCondition.makeCondition(EntityOperator.OR, cond1,cond2);
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("G","Geo");
        dynamicView.addAlias("G","id","geoId",null,false,false,null);
        dynamicView.addAlias("G","name","geoName",null,false,false,null);
        dynamicView.addAlias("G","geoTypeId");
        dynamicView.addAlias("G","sequenceNum");
        
        dynamicView.addMemberEntity("GA","GeoAssoc");
        dynamicView.addAlias("GA","geoIdTo");
        dynamicView.addAlias("GA","pId","geoId",null,false,false,null);
        dynamicView.addViewLink("G","GA", Boolean.FALSE,ModelKeyMap.makeKeyMapList("geoId", "geoIdTo"));
        
        List<String> fieldsToSelect = FastList.newInstance();
        List<String> orderBy = FastList.newInstance();
        fieldsToSelect.add("id");
        fieldsToSelect.add("name");
        fieldsToSelect.add("pId");
        orderBy.add("sequenceNum");
        orderBy.add("id");
        
        try {
            //查询的数据Iterator
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
                    EntityFindOptions.CONCUR_READ_ONLY,   true);
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, whereCond, null, fieldsToSelect, orderBy, findOpts);
            geoList = pli.getCompleteList();
            
            //关闭pli
            pli.close();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot lookup State Geos: " + e.toString(), module);
        }
        
        result.put("geoList", geoList);
        return result;
    }
    
    
    /**
     * 根据仓库ID获取仓库产能明细 add by qianjin 2016.04.21
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getCapacityByFacilityId(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String facilityId = (String)context.get("facilityId");
        
        try {
        	//获取仓库的产能
			GenericValue f_gv = delegator.findByPrimaryKey("Facility", UtilMisc.toMap("facilityId", facilityId));
			double capacity = f_gv.getDouble("capacity");
			//获取该仓库订单的商品数量
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
			Map capacity_map = FastMap.newInstance();
			for(int i=0;i<8;i++){
				//获取当前日期
				Date date=new Date();//取时间
				Calendar calendar = new GregorianCalendar();
				calendar.setTime(date);
				calendar.add(Calendar.DATE,i);//把日期往后增加一天.整数往后推,负数往前移动
				date=calendar.getTime(); //这个时间就是日期往后推一天的结果 
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
				String dateString = formatter.format(date);
				List<GenericValue> list = getQuantityByFacilityId(dctx,facilityId,dateString);
				//获取订单的商品总数
				double quantity = 0;
				if(UtilValidate.isNotEmpty(list)){
					GenericValue gv = EntityUtil.getFirst(list);
					if(UtilValidate.isNotEmpty(gv.get("quantity"))){
						quantity = gv.getDouble("quantity");
					}
				}
				capacity_map.put(""+ i, capacity - quantity);
			}
			result.put("capacitys", capacity_map);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }
    
    /**
     * 根据仓库ID获取订单的商品总数 add by qianjin 2016.04.21
     * @param dctx
     * @param context
     * @return
     */
    public static List<GenericValue> getQuantityByFacilityId(DispatchContext dctx,String facilityId,String date) {
        Delegator delegator = dctx.getDelegator();
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("OH","OrderHeader");
        dynamicView.addAlias("OH","originFacilityId");
        dynamicView.addAlias("OH","deliveryDate");
        dynamicView.addAlias("OH","statusId");
        
        dynamicView.addMemberEntity("OI","OrderItem");
        dynamicView.addAlias("OI","quantity","quantity",null,null,null,"sum");
        dynamicView.addViewLink("OI","OH", Boolean.FALSE,ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("quantity");
        fieldsToSelect.add("deliveryDate");
        
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        //默认条件
        filedExprs.add(EntityCondition.makeCondition("originFacilityId", EntityOperator.EQUALS,facilityId));
        filedExprs.add(EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_WAITPAY","ORDER_WAITSHIP","ORDER_WAITRECEIVE","ORDER_COMPLETED","ORDER_WAITPRODUCE")));
        String start_date = date+ " " + "00:00:00.000";
        String end_date = date+ " " + "23:59:59.999";
        try {
			filedExprs.add(EntityCondition.makeCondition("deliveryDate",EntityOperator.GREATER_THAN_EQUAL_TO, ObjectType.simpleTypeConvert(start_date, "Timestamp", null, null)));
			filedExprs.add(EntityCondition.makeCondition("deliveryDate",EntityOperator.LESS_THAN_EQUAL_TO, ObjectType.simpleTypeConvert(end_date, "Timestamp", null, null)));
		} catch (GeneralException e1) {
			e1.printStackTrace();
		}
        
        //添加where条件
        EntityCondition mainCond = null;
        if (filedExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
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
