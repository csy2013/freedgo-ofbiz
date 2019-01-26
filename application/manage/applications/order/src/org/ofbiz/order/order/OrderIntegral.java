package org.ofbiz.order.order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceAuthException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.ServiceValidationException;

public class OrderIntegral {
	/**
	 * 实物订单的积分保存方法		add by qianjin 2016.03.09
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> savePhysicalOrderIntegral(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		/** 获取参数 */
		List<String> orderId_list = (List)context.get("orderIdList");	//订单ID
		for(String order_id : orderId_list){
			try {
				Map paramMap = FastMap.newInstance();
				paramMap.put("orderId", order_id);
				//订单应付金额
				List<GenericValue> oi_list = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", order_id));
				if(UtilValidate.isNotEmpty(oi_list)){
					GenericValue orderItem = EntityUtil.getFirst(oi_list);
					BigDecimal amount = BigDecimal.ZERO;
					if(UtilValidate.isNotEmpty(orderItem.getBigDecimal("lastUnitPrice"))){
						amount = orderItem.getBigDecimal("lastUnitPrice");
					}else{
						amount = orderItem.getBigDecimal("unitPrice");
					}
					paramMap.put("amount", amount.multiply(orderItem.getBigDecimal("quantity")).setScale(2));
					//调用保存积分值方法
					result = saveOrderIntegral(dctx,paramMap);
				}
			} catch (GenericEntityException e) {
				e.printStackTrace();
				result.put("errorMsg", "查询OrderHeader实体错误！");
			}
		}
		return result;
	}
	
	/**
	 * 虚拟订单的积分保存方法		add by qianjin 2016.03.09
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> saveVirtualOrderIntegral(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		/** 获取参数 */
		String order_id = (String)context.get("orderId");	//订单ID
		List<String> ticket_list = (List)context.get("ticketList");	//券号集合
		try {
			Map paramMap = FastMap.newInstance();
			paramMap.put("orderId", order_id);
			//订单应付金额
			List<GenericValue> oi_list = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", order_id));
			if(UtilValidate.isNotEmpty(oi_list)){
				GenericValue orderItem = EntityUtil.getFirst(oi_list);
				BigDecimal amount = BigDecimal.ZERO;
				if(UtilValidate.isNotEmpty(orderItem.getBigDecimal("lastUnitPrice"))){
					amount = orderItem.getBigDecimal("lastUnitPrice");
				}else{
					amount = orderItem.getBigDecimal("unitPrice");
				}
				paramMap.put("amount", amount);
				
				for(String ticket_no : ticket_list){
					paramMap.put("ticketNo", ticket_no);
					//调用保存积分值方法
					result = saveOrderIntegral(dctx,paramMap);
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			result.put("errorMsg", "查询OrderHeader实体错误！");
		}
		return result;
	}
	
	/**
	 * 根据订单规则保存积分值		add by qianjin 2016.03.09
	 * @param dctx
	 * @param context
	 * @return
	 * @throws GenericEntityException 
	 */
	public static Map<String, Object> saveOrderIntegral(DispatchContext dctx, Map context){
		Delegator delegator = dctx.getDelegator();	//delegator对象
		LocalDispatcher dispatcher = dctx.getDispatcher();	//dispatcher对象
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		/** 获取参数 */
		String order_id = (String)context.get("orderId");	//订单ID
		String ticket_no = UtilValidate.isNotEmpty(context.get("ticketNo")) ? (String)context.get("ticketNo") : "";	//券号
		BigDecimal amount = (BigDecimal) context.get("amount");	//消费金额
		
		/** 获取积分操作 */
		BigDecimal getIntegral = BigDecimal.ZERO;	//初始化积分为0
		try {
			//订单实体
			GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", order_id));
			List<GenericValue> orderItems = delegator.findByAnd("OrderItem",UtilMisc.toMap("orderId",order_id));
			List<GenericValue> orderRole = delegator.findByAnd("OrderRole",UtilMisc.toMap("orderId",order_id,"roleTypeId","PLACING_CUSTOMER"));
			//商品实体
			GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",orderItems.get(0).get("productId")));
			//商品分类列表（递归出所有父级）
			Map categoryMap = getCateGoryTreeData(delegator,product.getString("primaryProductCategoryId"),true,FastMap.newInstance());
			//活动实体
			GenericValue productActivity = delegator.findByPrimaryKey("ProductActivityGoods",UtilMisc.toMap("activityId",orderItems.get(0).get("activityId"),"productId",orderItems.get(0).get("productId")));
			//操作记录信息
			String description = "";
			//判断活动不能为空，并且该活动可以获得积分
			String isSupportScore="";
			if(UtilValidate.isNotEmpty(productActivity)){
				isSupportScore = productActivity.getString("isSupportScore");
			}
			if (UtilValidate.isNotEmpty(isSupportScore) && "Y".equals(isSupportScore)) {
				//根据商品ID，获取不积分商品列表
				List<GenericValue> nonProduct = delegator.findByAnd("PartyIntegral",UtilMisc.toMap("productId",product.get("productId"),"partyIntegralType","NONPRODUCT"));
				//判断不积分商品列表是否为空
				if (UtilValidate.isEmpty(nonProduct)){
					//根据商品类型，获取不积分商品类型列表
					EntityCondition condition=EntityCondition.makeCondition(
								EntityCondition.makeCondition("productCategoryId", EntityOperator.IN,(List)categoryMap.get("idList")),
								EntityCondition.makeCondition("partyIntegralType", EntityOperator.EQUALS,"NONCATEGORY")
							);
					List<GenericValue> nonCategory = delegator.findList("PartyIntegral",condition,null,null,null,true);
					//判断不积分商品类型是否为空
					if (UtilValidate.isEmpty(nonCategory)){
						//根据会员ID，获取会员等级
						List<GenericValue> partyLevel = FastList.newInstance();
						//判断orderRole实体是否为空
						if(UtilValidate.isNotEmpty(orderRole)){
							partyLevel = delegator.findByAnd("PartyLevel",UtilMisc.toMap("partyId",orderRole.get(0).get("partyId")));
						}
						//判断会员等级是否为空
				      	if(UtilValidate.isNotEmpty(partyLevel)){
				      		//根据商品ID，获取特殊商品列表
				      		EntityCondition specialProduct_condition=EntityCondition.makeCondition(
				      				EntityCondition.makeCondition(
		      							UtilMisc.toList(
  											EntityCondition.makeCondition("levelId", EntityOperator.EQUALS,partyLevel.get(0).getString("levelId")),
						      				EntityCondition.makeCondition("levelId", EntityOperator.EQUALS,null)	
		      							),
		      							EntityJoinOperator.OR
		      						),
									EntityCondition.makeCondition("productId", EntityOperator.EQUALS,product.get("productId")),
									EntityCondition.makeCondition("partyIntegralType", EntityOperator.EQUALS,"SPECIALPRODUCT")
								);
				      		List<GenericValue> specialProduct = delegator.findList("PartyIntegral",specialProduct_condition,null,null,null,true);
				      		//判断特殊商品列表是否为空
				      		if(UtilValidate.isNotEmpty(specialProduct)){
				      			//根据规则获取相应积分，四舍五入，保留2位小数
					        	getIntegral = amount.divide(new BigDecimal(specialProduct.get(0).getLong("integralValue")),0,BigDecimal.ROUND_HALF_UP);
					        	description = "积分规则【特殊商品】-" +
					        				  "会员等级【"+partyLevel.get(0).getString("levelName")+"】-" +
					        				  "商品编号【"+product.get("productId")+"】-" +
					        				  "消费金额【"+amount+"元】-" +
					        				  "每积一分消费金额【"+specialProduct.get(0).getLong("integralValue")+"元】";
				      		}else{
				      			//根据会员等级和商品类型ID，获取特殊商品类型列表
				      			EntityCondition specialCategory_condition =EntityCondition.makeCondition(
			      					EntityCondition.makeCondition(
			      							UtilMisc.toList(
      											EntityCondition.makeCondition("levelId", EntityOperator.EQUALS,partyLevel.get(0).getString("levelId")),
							      				EntityCondition.makeCondition("levelId", EntityOperator.EQUALS,null)	
			      							),
			      							EntityJoinOperator.OR
			      						),
									EntityCondition.makeCondition("productCategoryId", EntityOperator.IN,(List)categoryMap.get("idList")),
									EntityCondition.makeCondition("partyIntegralType", EntityOperator.EQUALS,"SPECIALCATEGORY")
								);
				      			List<GenericValue> specialCategory = delegator.findList("PartyIntegral",specialCategory_condition,null,null,null,true);
				      			//判断特殊商品类型列表是否为空
				      			if(UtilValidate.isNotEmpty(specialCategory)){
				      				//根据规则获取相应积分，四舍五入，保留2位小数
				      				getIntegral = amount.divide(new BigDecimal(specialCategory.get(0).getLong("integralValue")),0,BigDecimal.ROUND_HALF_UP);
				      				description = "积分规则【特殊商品分类】-" +
					        				      "会员等级【"+partyLevel.get(0).getString("levelName")+"】-" +
					        				      "商品编号【"+product.get("productId")+"】-" +
					        				      "消费金额【"+amount+"元】-" +
					        				      "每积一分消费金额【"+specialCategory.get(0).getLong("integralValue")+"元】";
				      			}else{
				      				//根据会员等级，获取一般积分列表
				      				EntityCondition normal_condition =EntityCondition.makeCondition(
				      					EntityCondition.makeCondition(
				      							UtilMisc.toList(
	      											EntityCondition.makeCondition("levelId", EntityOperator.EQUALS,partyLevel.get(0).getString("levelId")),
								      				EntityCondition.makeCondition("levelId", EntityOperator.EQUALS,null)	
				      							),
				      							EntityJoinOperator.OR
				      						),
										EntityCondition.makeCondition("partyIntegralType", EntityOperator.EQUALS,"NORMAL")
									);
				      				
				      				List<GenericValue> normal = delegator.findList("PartyIntegral",normal_condition,null,null,null,true);
				      				//判断一般积分列表是否为空
				      				if(UtilValidate.isNotEmpty(normal)){
				      					//根据规则获取相应积分，四舍五入，保留2位小数
				      					getIntegral = amount.divide(new BigDecimal(normal.get(0).getLong("integralValue")),0,BigDecimal.ROUND_HALF_UP);
				      					description = "积分规则【一般积分】-" +
						        				      "会员等级【"+partyLevel.get(0).getString("levelName")+"】-" +
						        				      "商品编号【"+product.get("productId")+"】-" +
						        				      "消费金额【"+amount+"元】-" +
						        				      "每积一分消费金额【"+normal.get(0).getLong("integralValue")+"元】";
				      				}
				      			}
				      		}
				      	}
					}
				}
			}										
			//判断积分增加是否为0
			if(getIntegral.compareTo(BigDecimal.ZERO) != 0){
				try {
					//保存积分获取记录
					String msg = "";
					String getWay = "";
					if(UtilValidate.isNotEmpty(ticket_no)){
						msg = "使用消费券【"+ticket_no+"】获取了 "+getIntegral+"积分。";
						getWay = "虚拟订单消费";
					}else{
						msg = "消费订单【"+order_id+"】获取了 "+getIntegral+"积分。";
						getWay = "实物订单消费";
					}
					dispatcher.runSync("partyIntegralChange",UtilMisc.toMap("partyId",orderRole.get(0).get("partyId"),
																			"integralValue",getIntegral,
																			"getWay",getWay,
																			"description",msg+description,
																			"changedir","plus",
																			"type","1",
																			"orderId",order_id
																			));
				} catch (GenericServiceException e) {
					e.printStackTrace();
					result.put("errorMsg", "保存积分记录服务调用错误！");
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
			result.put("errorMsg", "订单查询错误！");
		}
		return result;
	}
	
	/**
     * 根据id递归查询商品分类的树形结构   add by qianjin 2016.03.09
     * @param delegator
     * @param id
     * @param lookParent	true：向上查询出所有的父节点,包括当前节点； false：向下查询出所有的子节点，不包括当前节点
     * @param resultMap
     * @return
     */
    public static Map getCateGoryTreeData(Delegator delegator,String id,Boolean lookParent,Map resultMap){
    	List idList = UtilValidate.isEmpty(resultMap.get("idList")) ? FastList.newInstance() : (List)resultMap.get("idList");
    	List nameList = UtilValidate.isEmpty(resultMap.get("nameList")) ? FastList.newInstance() : (List)resultMap.get("nameList");
		try {
			//判断向上查询父节点或向下查询子节点
			if(lookParent){
				//根据商品分类id查询
				List<GenericValue> productCategoryList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("productCategoryId",id));
				//遍历商品分类list
				for(GenericValue pc_gv : productCategoryList){
					//存放商品分类id和name
					idList.add(pc_gv.get("productCategoryId"));
					nameList.add(pc_gv.get("categoryName"));
					resultMap.put("idList", idList);
					resultMap.put("nameList", nameList);
					//判断父id是否为空
					if(UtilValidate.isNotEmpty(pc_gv.get("primaryParentCategoryId"))){
						//递归父节点的数据
		    			getCateGoryTreeData(delegator,pc_gv.get("primaryParentCategoryId").toString(),lookParent,resultMap);
		    		}
				}
			}else{
				//将当前id作为父id查找子节点数据
				List<GenericValue> childCategoryList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("primaryParentCategoryId",id));
				//遍历子节点list
				for(GenericValue child_gv : childCategoryList){
					//存放商品分类id和name
					idList.add(child_gv.get("productCategoryId"));
					nameList.add(child_gv.get("categoryName"));
					resultMap.put("idList", idList);
					resultMap.put("nameList", nameList);
					//递归子节点数据
					getCateGoryTreeData(delegator,child_gv.get("productCategoryId").toString(),lookParent,resultMap);
				}
			}
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
		if(UtilValidate.isEmpty(resultMap.get("idList"))){
			resultMap.put("idList", idList);
		}
		if(UtilValidate.isEmpty(resultMap.get("nameList"))){
			resultMap.put("nameList", nameList);
		}
    	return resultMap;
    }
}
