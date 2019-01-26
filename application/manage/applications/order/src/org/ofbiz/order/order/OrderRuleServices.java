/*
 * 文件名：OrderRuleServices.java
 * 版权：启华
 * 描述：订单设置服务类
 * 修改人：gss
 * 修改时间：2016-1-11
 * 修改单号：
 * 修改内容：
 */
package org.ofbiz.order.order;

import com.ibm.icu.util.Calendar;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


public class OrderRuleServices{
    public static final String module = OrderRuleServices.class.getName();
    public static final String resource = "ContentUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";
	
	/***
	 * 自动取消订单,自动确认收货时间,自动审核时间,
	 * 退单自动取消时间,退单自动确认收货时间,自动评价时间,
	 * 可提交退货时间
	 * @param dctx
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static Map<String, Object> cancelFlaggedSalesOrders(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        //获取订单规则
        GenericValue OrderRule = null;
        try {
        	OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
        }
      
        DynamicViewEntity dv = new DynamicViewEntity();
        dv.addMemberEntity("OH", "OrderHeader");
        dv.addAlias("OH", "orderId");
        dv.addAlias("OH", "orderTypeId");
        dv.addAlias("OH", "statusId");
        
        dv.addMemberEntity("OS", "OrderStatus");
        dv.addAlias("OS", "statusDatetime");
        dv.addViewLink("OH", "OS", false, ModelKeyMap.makeKeyMapList("orderId", "orderId","statusId","statusId"));
        // 查询条件，订单类型为普通订单和补货订单，状态为待支付，收货时间大于等于当前时间+规则设定时间
        EntityCondition whereCond = EntityCondition.makeCondition(
        		UtilMisc.toList(
                	EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER" ),//普通订单
                	EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITPAY") //创建查询表达式 （查询所有待支付的订单）
        			), 
        		EntityOperator.AND);
        //去除重复数据
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
        try {
			EntityListIterator pli = delegator.findListIteratorByCondition(dv, whereCond, null, null, null, findOpts);
			 for(GenericValue orderHeader : pli.getCompleteList()) {
		        	//订单编号
		            String orderId = orderHeader.getString("orderId");
		            //订单录入时间
		            Timestamp orderDate = orderHeader.getTimestamp("orderDate");
		            //订单items条件
		            List<EntityExpr> orderexprs = new ArrayList<EntityExpr>();
		            orderexprs.add(EntityCondition.makeCondition("orderId", orderId));//订单编号
		            orderexprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_WAITPAY")); //未支付状态
		            List<GenericValue> orderItems = null;
		            try {
		                orderItems = delegator.findList("OrderItem", EntityCondition.makeCondition(orderexprs, EntityOperator.AND), null, null, null, false);
		            } catch (GenericEntityException e) {
		            	return ServiceUtil.returnSuccess();
		            }
		            if(UtilValidate.isNotEmpty(orderItems)){
		            	   //循环orderItems
		                for(GenericValue orderCheckItem : orderItems) {
		                	//商品编号
		                	String productId =(String)orderCheckItem.get("productId");
		                	//活动Id
		                	String activityId =(String)orderCheckItem.get("activityId");
		                	//判断活动Id是否为空 ,不为空（秒杀,团购）为空(普通订单) 
		                	if(UtilValidate.isNotEmpty(activityId))
		                	{
		                		try {
		                			//查询活动
		    						GenericValue productActivity=delegator.findByPrimaryKey("ProductActivity",UtilMisc.toMap("activityId",activityId));
		    					    if(UtilValidate.isNotEmpty(productActivity))
		    					    {
		    					    	//活动类型
		    					    	String activityType=(String)productActivity.get("activityType");
		    					    	//判断是否为团购订单
		    					    	if(UtilValidate.isNotEmpty(activityType)&& "GROUP_ORDER".equals(activityType))
		    					    	{
		    					    		  if (OrderRule != null)
		    				                    {
		    				                    	//团购订单自动取消时间
		    				                        int groupCancelStamp = OrderRule.getLong("groupCancelStamp").intValue();
		    				                        //团购订单自动取消时间单位
		    				                        String groupCancelUom = (String)OrderRule.get("groupCancelUom");
		    				                        if (groupCancelStamp > 0) {
		    				                            Calendar cal = Calendar.getInstance();
		    				                            cal.setTimeInMillis(orderDate.getTime());
		    				                            if ("d".equals(groupCancelUom))
		    				                            {
		    				                               cal.add(Calendar.DAY_OF_YEAR, groupCancelStamp);
		    				        					}else if ("h".equals(groupCancelUom))
		    				        					{
		    				        						cal.add(Calendar.HOUR, groupCancelStamp);
		    				        					}else{
		    				        						cal.add(Calendar.MINUTE, groupCancelStamp);
		    				        					}
		    				                            Date cancelDate = cal.getTime();
		    				                            Date nowDate = new Date();
		    				                            if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
		    				                                //团购订单自动取消
		    				                            	  Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_CANCELLED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
		    	    			                                try {
		    	    			                                	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
		    	    			                                } catch (Exception e) {
		    	    			                                    Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
		    	    			                                }
		    				                            }
		    				                        }
		    				                    }
		    					    	}else if(UtilValidate.isNotEmpty(activityType)&& "SEC_KILL".equals(activityType))//是否为秒杀订单
		    					    	{
		    					    		if (OrderRule != null)
		    			                    {
		    			                    	//秒杀订单自动取消时间
		    			                        int seckillCancelStamp = OrderRule.getLong("seckillCancelStamp").intValue();
		    			                        //秒杀订单自动取消时间单位
		    			                        String seckillCancelUom = (String)OrderRule.get("seckillCancelUom");
		    			                        if (seckillCancelStamp > 0) {
		    			                            Calendar cal = Calendar.getInstance();
		    			                            cal.setTimeInMillis(orderDate.getTime());
		    			                            if ("d".equals(seckillCancelUom))
		    			                            {
		    			                               cal.add(Calendar.DAY_OF_YEAR, seckillCancelStamp);
		    			        					}else if ("h".equals(seckillCancelUom))
		    			        					{
		    			        						cal.add(Calendar.HOUR, seckillCancelStamp);
		    			        					}else{
		    			        						cal.add(Calendar.MINUTE, seckillCancelStamp);
		    			        					}
		    			                            Date cancelDate = cal.getTime();
		    			                            Date nowDate = new Date();
		    			                            if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
		    			                                //秒杀订单自动取消
					                                	  Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_CANCELLED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
		    			                                try {
		    			                                	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
		    			                                } catch (Exception e) {
		    			                                    Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
		    			                                }
		    			                            }
		    			                        }
		    			                    }	
		    					    	}
		    					    }
		                		} catch (Exception e) {
		    						e.printStackTrace();
		    					}
		                	}else{
		                		//订单规则中获取普通订单取消时间（取消未支付的天数）
		                        if (OrderRule != null)
		                        {
		                        	//默认普通取消时间
		                            int ordinaryCancelStamp = OrderRule.getLong("ordinaryCancelStamp").intValue();
		                            //默认取消时间单位
		                            String ordinaryCancelUom = (String)OrderRule.get("ordinaryCancelUom");
		                            if (ordinaryCancelStamp > 0) {
		                                Calendar cal = Calendar.getInstance();
		                                cal.setTimeInMillis(orderDate.getTime());
		                                if ("d".equals(ordinaryCancelUom))
		                                {
		                                   cal.add(Calendar.DAY_OF_YEAR, ordinaryCancelStamp);
		            					}else if ("h".equals(ordinaryCancelUom))
		            					{
		            						cal.add(Calendar.HOUR, ordinaryCancelStamp);
		            					}else{
		            						cal.add(Calendar.MINUTE, ordinaryCancelStamp);
		            					}
		                                Date cancelDate = cal.getTime();
		                                Date nowDate = new Date();
		                                if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
		                                    //取消订单
		                                	  Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_CANCELLED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
				                                try {
				                                	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
				                                } catch (Exception e) {
				                                    Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
				                              }
		                                }
		                            }
		                        }
		                	}
		                }
		            }
		        }
        } catch (GenericEntityException e2) {
			e2.printStackTrace();
		}
        
        
        //订单自动确认收货---待收货状态自动变成已收货
        List<GenericValue> orderswaitReceive = null;
        // 创建查询表达式 ---待收货订单
        List<EntityExpr> waitexprs = UtilMisc.toList(
                EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER"),
                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITRECEIVE")//待收货
                );
        EntityConditionList<EntityExpr> waitecl = EntityCondition.makeCondition(waitexprs, EntityOperator.AND);
        // 获取所有待收货销售订单
        try {
        	orderswaitReceive = delegator.findList("OrderHeader", waitecl, null, UtilMisc.toList("orderDate"), null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting order headers", module);
        }
      
        for(GenericValue waitOrderHeader : orderswaitReceive) {
            String orderId = waitOrderHeader.getString("orderId");
            Timestamp orderDate = waitOrderHeader.getTimestamp("orderDate");//订单录入时间
        	if (UtilValidate.isNotEmpty(waitOrderHeader
					.get("distributionMethod"))
					&& !"SMZT".equals(waitOrderHeader.get("distributionMethod"))) {
				GenericValue orderdelivery;
				try {
					orderdelivery = EntityUtil
							.getFirst(delegator.findByAnd("OrderDelivery",
									UtilMisc.toMap("orderId", orderId)));
					if (UtilValidate.isNotEmpty(orderdelivery)) {
						orderDate = orderdelivery.getTimestamp("createdStamp");
					}
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
			}
            if (OrderRule != null)
            {
            	//订单自动确认收货时间
                int confirmOrderStamp = OrderRule.getLong("confirmOrderStamp").intValue();
                if (confirmOrderStamp > 0) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(orderDate.getTime());
                    cal.add(Calendar.DAY_OF_YEAR, confirmOrderStamp);
                    Date cancelDate = cal.getTime();
                    Date nowDate = new Date();
                    //Debug.logInfo("Cancel Date : " + cancelDate, module);
                    //Debug.logInfo("Current Date : " + nowDate, module);
                    if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
                        //订单状态变为待评价
                        Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_WAITEVALUATE", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
                        try {
                        	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
                        } catch (Exception e) {
                            Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
                        }
                    }
                }
            }
        }
        
        //自动评价时间---待评价自动变成已完成
        List<GenericValue> orderswaitEvaluate = null;
        // 创建查询表达式 ---待评价订单
        List<EntityExpr> receiveExprs = UtilMisc.toList(
        		EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER"),
        		EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITRECEIVE")//待收货
        		);
        
        EntityConditionList<EntityExpr> evaluateEcl = EntityCondition.makeCondition(receiveExprs, EntityOperator.AND);
        // 获取所有待评价销售订单
        try {
        	orderswaitEvaluate = delegator.findList("OrderHeader", evaluateEcl, null, UtilMisc.toList("orderDate"), null, false);
        } catch (GenericEntityException e) {
        	Debug.logError(e, "Problem getting order headers", module);
        }
      /*  if (UtilValidate.isEmpty(orderswaitReceive)) {
        	Debug.logInfo("No orders to check, finished", module);
        	return ServiceUtil.returnSuccess();
        }*/
        String productId = "";
        for(GenericValue evaluateOrderHeader : orderswaitEvaluate) {
        	 GenericValue orderItems;
			try {
				orderItems = EntityUtil.getFirst(evaluateOrderHeader.getRelated("OrderItem", UtilMisc.toList("orderItemSeqId")));
				 if(null != orderItems) {
					 productId = orderItems.getString("productId");
				 }
			} catch (GenericEntityException e1) {
				e1.printStackTrace();
			}
        	String orderId = evaluateOrderHeader.getString("orderId");
        	//String orderStatus = orderHeader.getString("statusId");
        	//首先检查未付订单
        	Timestamp orderDate = evaluateOrderHeader.getTimestamp("orderDate");//订单录入时间
        	if (OrderRule != null)
        	{
        		//订单自动确认收货时间
        		int reviewStamp = OrderRule.getLong("reviewStamp").intValue();
        		if (reviewStamp > 0) {
        			Calendar cal = Calendar.getInstance();
        			cal.setTimeInMillis(orderDate.getTime());
        			cal.add(Calendar.DAY_OF_YEAR, reviewStamp);
        			Date cancelDate = cal.getTime();
        			Date nowDate = new Date();
        			//Debug.logInfo("Cancel Date : " + cancelDate, module);
        			//Debug.logInfo("Current Date : " + nowDate, module);
        			if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
        				//为此订单创建评价
        				Map<String,Object> paramContext = FastMap.newInstance();
        		        paramContext.put("userLogin",userLogin);
        		        paramContext.put("productStoreId",evaluateOrderHeader.getString("productStoreId"));
        		        paramContext.put("productRating", new BigDecimal(5));
        		        paramContext.put("productReview", "好评!");
        		        paramContext.put("orderId", orderId);
        		        paramContext.put("productId",productId);
        		        try {
							Map<String,Object> resultContext = dispatcher.runSync("createProductReview", paramContext);
						} catch (GenericServiceException e) {
							e.printStackTrace();
						}
        				//订单状态变为已完成
        				 Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_COMPLETED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
                         try {
                        	 OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
                         } catch (Exception e) {
                             Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
                         }
        			}
        		}
        	}
        }
        
        
        //退单自动审核时间
        List<GenericValue> orderReturn = null;
        // 创建查询表达式 ---退货单
        List<EntityExpr> returnexprs = UtilMisc.toList(
                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_REQUESTED")//待审核
                );
        EntityConditionList<EntityExpr> returnecl = EntityCondition.makeCondition(returnexprs, EntityOperator.AND);
        try {
			orderReturn = delegator.findList("ReturnHeader", returnecl, null, UtilMisc.toList("entryDate"), null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        List<GenericValue> toBeSaved = new LinkedList<GenericValue>();
        for(GenericValue orderReturns : orderReturn) {
        	 //退单ID
        	 String returnId=(String)orderReturns.get("returnId");
        	 Timestamp entryDate = orderReturns.getTimestamp("entryDate");//订单录入时间
     		//退单自动审核时间
     		int returnToexamineStamp = OrderRule.getLong("returnToexamineStamp").intValue();
     		if (returnToexamineStamp > 0) {
     			Calendar cal = Calendar.getInstance();
     			cal.setTimeInMillis(entryDate.getTime());
     			cal.add(Calendar.DAY_OF_YEAR, returnToexamineStamp);
     			Date cancelDate = cal.getTime();
     			Date nowDate = new Date();
     			//Debug.logInfo("Cancel Date : " + cancelDate, module);
     			//Debug.logInfo("Current Date : " + nowDate, module);
     			if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
     				 try {
     				//退单表头
     	           GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader",UtilMisc.toMap("returnId",returnId));
     	           returnHeader.set("statusId","RETURN_WAITSHIP");//待发货
     	           GenericValue returnItem = delegator.findByAnd("ReturnItem",UtilMisc.toMap("returnId",returnId)).get(0);
     	           returnItem.set("statusId","RETURN_WAITSHIP");//待发货
     	           GenericValue returnStatus = delegator.makeValue("ReturnStatus",UtilMisc.toMap("returnStatusId",delegator.getNextSeqId("ReturnStatus"),
     	                   "statusId","RETURN_WAITSHIP","returnId",returnId,"returnItemSeqId",returnItem.get("returnItemSeqId"),"changeByUserLoginId",userLogin.get("userLoginId"),
     	                   "statusDatetime", UtilDateTime.nowTimestamp()));
     	           toBeSaved.add(returnHeader);
     	           toBeSaved.add(returnItem);
     	           toBeSaved.add(returnStatus);
				   delegator.storeAll(toBeSaved);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
     			}
     		}
        }
        
        //退单自动取消时间
        List<GenericValue> ordercancelReturn = null;
        // 创建查询表达式 ---退货单
        List<EntityExpr> cancelexprs = UtilMisc.toList(
                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_WAITSHIP")//待发货 ---拒绝
                );
        EntityConditionList<EntityExpr> cancelecl = EntityCondition.makeCondition(cancelexprs, EntityOperator.AND);
        try {
        	ordercancelReturn = delegator.findList("ReturnHeader", cancelecl, null, UtilMisc.toList("entryDate"), null, false);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        for(GenericValue ordercancelReturns : ordercancelReturn) {
       	 //退单ID
       	 String returnId=(String)ordercancelReturns.get("returnId");
       	 Timestamp entryDate = ordercancelReturns.getTimestamp("entryDate");//订单录入时间
    		//退单自动取消时间
    		int returnCancelStamp = OrderRule.getLong("returnCancelStamp").intValue();
    		if (returnCancelStamp > 0) {
    			Calendar cal = Calendar.getInstance();
    			cal.setTimeInMillis(entryDate.getTime());
    			cal.add(Calendar.DAY_OF_YEAR, returnCancelStamp);
    			Date cancelDate = cal.getTime();
    			Date nowDate = new Date();
    			//Debug.logInfo("Cancel Date : " + cancelDate, module);
    			//Debug.logInfo("Current Date : " + nowDate, module);
    			if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
    				 try {
    				//退单表头
    	           GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader",UtilMisc.toMap("returnId",returnId));
    	           returnHeader.set("statusId","RETURN_REJECTAPPLY");//待发货
    	           GenericValue returnItem = delegator.findByAnd("ReturnItem",UtilMisc.toMap("returnId",returnId)).get(0);
    	           returnItem.set("statusId","RETURN_REJECTAPPLY");//待发货
    	           GenericValue returnStatus = delegator.makeValue("ReturnStatus",UtilMisc.toMap("returnStatusId",delegator.getNextSeqId("ReturnStatus"),
    	                   "statusId","RETURN_REJECTAPPLY","returnId",returnId,"returnItemSeqId",returnItem.get("returnItemSeqId"),"changeByUserLoginId",userLogin.get("userLoginId"),
    	                   "statusDatetime", UtilDateTime.nowTimestamp()));
    	           toBeSaved.add(returnHeader);
    	           toBeSaved.add(returnItem);
    	           toBeSaved.add(returnStatus);
				   delegator.storeAll(toBeSaved);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
    			}
    		}
        }
        
        //退单自动确认收货时间returnConfirmStamp
        List<GenericValue> returnConfirmReturn = null;
        // 创建查询表达式 ---退货单
        List<EntityExpr> confirmexprs = UtilMisc.toList(
        		EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_WAITRECEIVE")//待收货 ---待退款
        		);
        EntityConditionList<EntityExpr> confirmecl = EntityCondition.makeCondition(confirmexprs, EntityOperator.AND);
        try {
        	returnConfirmReturn = delegator.findList("ReturnHeader", confirmecl, null, UtilMisc.toList("entryDate"), null, false);
        } catch (GenericEntityException e) {
        	e.printStackTrace();
        }
        for(GenericValue returnConfirmReturns : returnConfirmReturn) {
        	//退单ID
        	String returnId=(String)returnConfirmReturns.get("returnId");
        	Timestamp entryDate = returnConfirmReturns.getTimestamp("entryDate");//订单录入时间
        	//退单自动确认收货时间
        	int returnConfirmStamp = OrderRule.getLong("returnConfirmStamp").intValue();
        	if (returnConfirmStamp > 0) {
        		Calendar cal = Calendar.getInstance();
        		cal.setTimeInMillis(entryDate.getTime());
        		cal.add(Calendar.DAY_OF_YEAR, returnConfirmStamp);
        		Date cancelDate = cal.getTime();
        		Date nowDate = new Date();
        		//Debug.logInfo("Cancel Date : " + cancelDate, module);
        		//Debug.logInfo("Current Date : " + nowDate, module);
        		if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
        			try {
        				//退单表头
        				GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader",UtilMisc.toMap("returnId",returnId));
        				returnHeader.set("statusId","RETURN_WAITFEFUND");//待发货
        				GenericValue returnItem = delegator.findByAnd("ReturnItem",UtilMisc.toMap("returnId",returnId)).get(0);
        				returnItem.set("statusId","RETURN_WAITFEFUND");//待发货
        				GenericValue returnStatus = delegator.makeValue("ReturnStatus",UtilMisc.toMap("returnStatusId",delegator.getNextSeqId("ReturnStatus"),
        						"statusId","RETURN_WAITFEFUND","returnId",returnId,"returnItemSeqId",returnItem.get("returnItemSeqId"),"changeByUserLoginId",userLogin.get("userLoginId"),
        						"statusDatetime", UtilDateTime.nowTimestamp()));
        				toBeSaved.add(returnHeader);
        				toBeSaved.add(returnItem);
        				toBeSaved.add(returnStatus);
        				delegator.storeAll(toBeSaved);
        			} catch (GenericEntityException e) {
        				e.printStackTrace();
        			}
        		}
        	}
        }
        return ServiceUtil.returnSuccess();
    }
	
	/**
	 * 修改订单退货状态，订单确认收货后的几天后，更改退货状态为否 add by qianjin 2016.03.10
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> updateOrderReturn(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		LocalDispatcher dispatcher = dctx.getDispatcher();	//dispatcher对象
		
        //获取订单规则
		int returnCommitStamp = 0;
        try {
        	GenericValue OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
        	returnCommitStamp = OrderRule.getLong("returnCommitStamp").intValue();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
        }
		
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        dynamicView.addMemberEntity("OH","OrderHeader");
        dynamicView.addAlias("OH","orderId");
        dynamicView.addAlias("OH","isAllowReturn");
        
        dynamicView.addMemberEntity("OI","OrderItem");
        dynamicView.addViewLink("OH","OI", Boolean.FALSE,ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        
        dynamicView.addMemberEntity("P","Product");
        dynamicView.addAlias("P","productTypeId");
        dynamicView.addViewLink("OI","P", Boolean.FALSE,ModelKeyMap.makeKeyMapList("productId", "productId"));
        
        dynamicView.addMemberEntity("OS","OrderStatus");
        dynamicView.addAlias("OS","status_id","statusId", null, null, null,null);
        dynamicView.addAlias("OS","created_date","createdStamp", null, null, null,null);
        dynamicView.addViewLink("OH","OS", Boolean.FALSE,ModelKeyMap.makeKeyMapList("orderId", "orderId"));
        
        //添加条件
        filedExprs.add(EntityCondition.makeCondition("isAllowReturn", EntityOperator.EQUALS, "1"));
        filedExprs.add(EntityCondition.makeCondition("productTypeId", EntityOperator.EQUALS, "FINISHED_GOOD"));
        filedExprs.add(EntityCondition.makeCondition("status_id", EntityOperator.EQUALS, "ORDER_WAITEVALUATE"));
        
        //查询字段
        fieldsToSelect.add("orderId");
        fieldsToSelect.add("isAllowReturn");
        
        //当前时间减去可提交退货时间
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        filedExprs.add(EntityCondition.makeCondition("created_date", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.addDaysToTimestamp(nowTimestamp, -returnCommitStamp)));
        
        //mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        
        //去除重复数据
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
        try {
        	//查询的数据Iterator
			EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, EntityCondition.makeCondition(filedExprs,EntityOperator.AND), null, fieldsToSelect, null, findOpts);
			//订单ID集合
			List orderIds = FastList.newInstance();
			//待修改的实体对象
			List<GenericValue> toBeStore = new ArrayList<GenericValue>();
			for(GenericValue gv : pli.getCompleteList()){
				GenericValue oh_gv = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", gv.getString("orderId")));
				oh_gv.set("isAllowReturn", "0");
				toBeStore.add(oh_gv);
				orderIds.add(gv.getString("orderId"));
			}
			pli.close();
			//修改订单退货状态为否，并调用订单积分服务
			if(UtilValidate.isNotEmpty(toBeStore) && UtilValidate.isNotEmpty(orderIds)){
				delegator.storeAll(toBeStore);
				dispatcher.runSync("savePhysicalOrderIntegral",UtilMisc.toMap("orderIdList",orderIds));
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 自动取消订单 add by gss 2016.07.11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cancelOrdinaryOrder(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		LocalDispatcher dispatcher = dctx.getDispatcher();	//dispatcher对象
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		//获取订单规则
		try {
			GenericValue OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
			DynamicViewEntity dv = new DynamicViewEntity();
	        dv.addMemberEntity("OH", "OrderHeader");
	        dv.addAlias("OH", "orderId");
	        dv.addAlias("OH", "orderTypeId");
	        dv.addAlias("OH", "statusId");
	        dv.addMemberEntity("OS", "OrderStatus");
	        dv.addAlias("OS", "statusDatetime");
	        dv.addViewLink("OH", "OS", false, ModelKeyMap.makeKeyMapList("orderId", "orderId","statusId","statusId"));
	        // 查询条件，订单类型为普通订单 , 待支付
	        EntityCondition whereCond = EntityCondition.makeCondition(
	        		UtilMisc.toList(
	                	EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER" ),//普通订单
	                	EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITPAY") //创建查询表达式 （查询所有待支付的订单）
	        			), 
	        		EntityOperator.AND);
	        //去除重复数据
	        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
	        try {
				EntityListIterator pli = delegator.findListIteratorByCondition(dv, whereCond, null, null, null, findOpts);
				 for(GenericValue orderHeader : pli.getCompleteList()) {
			        	//订单编号
			            String orderId = orderHeader.getString("orderId");
			            //订单录入时间
			            Timestamp orderDate = orderHeader.getTimestamp("statusDatetime");
			            //订单items条件
			            List<EntityExpr> orderexprs = new ArrayList<EntityExpr>();
			            orderexprs.add(EntityCondition.makeCondition("orderId", orderId));//订单编号
			            orderexprs.add(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_WAITPAY")); //未支付状态
			            List<GenericValue> orderItems = null;
			            try {
			                orderItems = delegator.findList("OrderItem", EntityCondition.makeCondition(orderexprs, EntityOperator.AND), null, null, null, false);
			            } catch (GenericEntityException e) {
			            	return ServiceUtil.returnSuccess();
			            }
			            if(UtilValidate.isNotEmpty(orderItems)){
			            	   //循环orderItems
			                for(GenericValue orderCheckItem : orderItems) {
			                	//商品编号
			                	String productId =(String)orderCheckItem.get("productId");
			                	//活动Id
			                	String activityId =(String)orderCheckItem.get("activityId");
			                	//判断活动Id是否为空 ,不为空（秒杀,团购）为空(普通订单) 
			                	if(UtilValidate.isNotEmpty(activityId))
			                	{
			                		try {
			                			//查询活动
			    						GenericValue productActivity=delegator.findByPrimaryKey("ProductActivity",UtilMisc.toMap("activityId",activityId));
			    					    if(UtilValidate.isNotEmpty(productActivity))
			    					    {
			    					    	//活动类型
			    					    	String activityType=(String)productActivity.get("activityType");
			    					    	//判断是否为团购订单
			    					    	if(UtilValidate.isNotEmpty(activityType)&& "GROUP_ORDER".equals(activityType))
			    					    	{
			    					    		  if (OrderRule != null)
			    				                    {
			    				                    	//团购订单自动取消时间
			    				                        int groupCancelStamp = OrderRule.getLong("groupCancelStamp").intValue();
			    				                        //团购订单自动取消时间单位
			    				                        String groupCancelUom = (String)OrderRule.get("groupCancelUom");
			    				                        if (groupCancelStamp > 0) {
			    				                            Calendar cal = Calendar.getInstance();
			    				                            cal.setTimeInMillis(orderDate.getTime());
			    				                            if ("d".equals(groupCancelUom))
			    				                            {
			    				                               cal.add(Calendar.DAY_OF_YEAR, groupCancelStamp);
			    				        					}else if ("h".equals(groupCancelUom))
			    				        					{
			    				        						cal.add(Calendar.HOUR, groupCancelStamp);
			    				        					}else{
			    				        						cal.add(Calendar.MINUTE, groupCancelStamp);
			    				        					}
			    				                            Date cancelDate = cal.getTime();
			    				                            Date nowDate = new Date();
			    				                            if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
			    				                                //团购订单自动取消
			    				                            	  Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_CANCELLED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
			    	    			                                try {
			    	    			                                	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
			    	    			                                } catch (Exception e) {
			    	    			                                    Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
			    	    			                                }
			    				                            }
			    				                        }
			    				                    }
			    					    	}else if(UtilValidate.isNotEmpty(activityType)&& "SEC_KILL".equals(activityType))//是否为秒杀订单
			    					    	{
			    					    		if (OrderRule != null)
			    			                    {
			    			                    	//秒杀订单自动取消时间
			    			                        int seckillCancelStamp = OrderRule.getLong("seckillCancelStamp").intValue();
			    			                        //秒杀订单自动取消时间单位
			    			                        String seckillCancelUom = (String)OrderRule.get("seckillCancelUom");
			    			                        if (seckillCancelStamp > 0) {
			    			                            Calendar cal = Calendar.getInstance();
			    			                            cal.setTimeInMillis(orderDate.getTime());
			    			                            if ("d".equals(seckillCancelUom))
			    			                            {
			    			                               cal.add(Calendar.DAY_OF_YEAR, seckillCancelStamp);
			    			        					}else if ("h".equals(seckillCancelUom))
			    			        					{
			    			        						cal.add(Calendar.HOUR, seckillCancelStamp);
			    			        					}else{
			    			        						cal.add(Calendar.MINUTE, seckillCancelStamp);
			    			        					}
			    			                            Date cancelDate = cal.getTime();
			    			                            Date nowDate = new Date();
			    			                            if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
			    			                                //秒杀订单自动取消
						                                	  Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_CANCELLED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
			    			                                try {
			    			                                	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
			    			                                } catch (Exception e) {
			    			                                    Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
			    			                                }
			    			                            }
			    			                        }
			    			                    }	
			    					    	}
			    					    }
			                		} catch (Exception e) {
			    						e.printStackTrace();
			    					}
			                	}else{
			                		//订单规则中获取普通订单取消时间（取消未支付的天数）
			                        if (OrderRule != null)
			                        {
			                        	//默认普通取消时间
			                            int ordinaryCancelStamp = OrderRule.getLong("ordinaryCancelStamp").intValue();
			                            //默认取消时间单位
			                            String ordinaryCancelUom = (String)OrderRule.get("ordinaryCancelUom");
			                            if (ordinaryCancelStamp > 0) {
			                                Calendar cal = Calendar.getInstance();
			                                cal.setTimeInMillis(orderDate.getTime());
			                                if ("d".equals(ordinaryCancelUom))
			                                {
			                                   cal.add(Calendar.DAY_OF_YEAR, ordinaryCancelStamp);
			            					}else if ("h".equals(ordinaryCancelUom))
			            					{
			            						cal.add(Calendar.HOUR, ordinaryCancelStamp);
			            					}else{
			            						cal.add(Calendar.MINUTE, ordinaryCancelStamp);
			            					}
			                                Date cancelDate = cal.getTime();
			                                Date nowDate = new Date();
			                                if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
			                                    //取消订单
			                                	  Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_CANCELLED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
					                                try {
					                                	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
					                                } catch (Exception e) {
					                                    Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
					                              }
			                                }
			                            }
			                        }
			                	}
			                }
			            }
			        }
	        } catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
		}
		return result;
	}
	
	/**
	 * 自动确认收货时间 add by gss 2016.07.11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> confirmWaitOrder(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		LocalDispatcher dispatcher = dctx.getDispatcher();	//dispatcher对象
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		//获取订单规则
		try {
			GenericValue OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
			DynamicViewEntity dv = new DynamicViewEntity();
	        dv.addMemberEntity("OH", "OrderHeader");
	        dv.addAlias("OH", "orderId");
	        dv.addAlias("OH", "orderTypeId");
	        dv.addAlias("OH", "statusId");
	        dv.addMemberEntity("OS", "OrderStatus");
	        dv.addAlias("OS", "statusDatetime");
	        dv.addViewLink("OH", "OS", false, ModelKeyMap.makeKeyMapList("orderId", "orderId","statusId","statusId"));
	        // 查询条件，订单类型为普通订单 , 待收货
	        EntityCondition whereCond = EntityCondition.makeCondition(
            		UtilMisc.toList(
	                	EntityCondition.makeCondition("orderTypeId", EntityOperator.IN, UtilMisc.toList("SALES_ORDER") ),
	                	EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITRECEIVE")//待收货
            			), 
            		EntityOperator.AND);
	        //去除重复数据
	        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
	        try {
				EntityListIterator pli = delegator.findListIteratorByCondition(dv, whereCond, null, null, null, findOpts);
				 for(GenericValue waitOrderHeader : pli.getCompleteList()) {

			            String orderId = waitOrderHeader.getString("orderId");
			            Timestamp orderDate = waitOrderHeader.getTimestamp("statusDatetime");//订单状态改变时间
			        	if (UtilValidate.isNotEmpty(waitOrderHeader
								.get("distributionMethod"))
								&& !"SMZT".equals(waitOrderHeader.get("distributionMethod"))) {
							GenericValue orderdelivery;
							try {
								orderdelivery = EntityUtil
										.getFirst(delegator.findByAnd("OrderDelivery",
												UtilMisc.toMap("orderId", orderId)));
								if (UtilValidate.isNotEmpty(orderdelivery)) {
									orderDate = orderdelivery.getTimestamp("createdStamp");
								}
							} catch (GenericEntityException e) {
								e.printStackTrace();
							}
						}
			        	
			            if (OrderRule != null){
			            	//订单自动确认收货时间
			                int confirmOrderStamp=0;
			    			if(UtilValidate.isNotEmpty(OrderRule)){
			    				//订单自动确认收货时间
			    	            confirmOrderStamp = OrderRule.getLong("confirmOrderStamp").intValue();
			    			}
			                if (confirmOrderStamp > 0) {
			                    Calendar cal = Calendar.getInstance();
			                    cal.setTimeInMillis(orderDate.getTime());
			                    cal.add(Calendar.DAY_OF_YEAR, confirmOrderStamp);
			                    Date cancelDate = cal.getTime();
			                    Date nowDate = new Date();
			                    if (cancelDate.equals(nowDate) || nowDate.after(cancelDate)) {
			                        //订单状态变为待评价
			                        Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_WAITEVALUATE", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
			                        try {
			                        	OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
			                        } catch (Exception e) {
			                            Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
			                        }
			                    }
			                }
			            }
				 }
	        } catch (GenericEntityException e2) {
				e2.printStackTrace();
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
		}
		return result;
	}
	
	/**
	 * 自动评价时间 add by gss 2016.07.11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> confirmEvaluateOrder(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		LocalDispatcher dispatcher = dctx.getDispatcher();	//dispatcher对象
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		//获取订单规则
		try {
			GenericValue OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
			//自动评价时间
            int reviewStamp=0;
			if(UtilValidate.isNotEmpty(OrderRule)){
				//自动评价时间
				reviewStamp = OrderRule.getLong("reviewStamp").intValue();
			}
    		if(reviewStamp>0){
    			DynamicViewEntity dv = new DynamicViewEntity();
    	        dv.addMemberEntity("OH", "OrderHeader");
    	        dv.addAlias("OH", "orderId");
    	        dv.addAlias("OH", "orderTypeId");
    	        dv.addAlias("OH", "statusId");
    	        dv.addAlias("OH", "productStoreId");
    	        dv.addMemberEntity("OS", "OrderStatus");
    	        dv.addAlias("OS", "statusDatetime");
    	        dv.addViewLink("OH", "OS", false, ModelKeyMap.makeKeyMapList("orderId", "orderId","statusId","statusId"));
    	        // 查询条件，订单类型为普通订单 , 待收货
    	        Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.add(Calendar.DAY_OF_YEAR, -reviewStamp);
                Date reviewDate = cal.getTime();
    	        EntityCondition whereCond = EntityCondition.makeCondition(
                		UtilMisc.toList(
    	                	EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER" ),
    	                	EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ORDER_WAITEVALUATE"),
                		    EntityCondition.makeCondition("statusDatetime", EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(reviewDate.getTime()))
                			), 
                		EntityOperator.AND);
    	        //去除重复数据
    	        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
    	        try {
    				EntityListIterator pli = delegator.findListIteratorByCondition(dv, whereCond, null, null, null, findOpts);
    				 for(GenericValue evaluateOrderHeader : pli.getCompleteList()) {
    					 String orderId = evaluateOrderHeader.getString("orderId");
    		        	 GenericValue orderItems;
    		        	 String  productId="";
    		        	 try { 
    						orderItems = EntityUtil.getFirst(delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId)));
    						if(null != orderItems){
    							  productId= orderItems.getString("productId");
    						 }
    					} catch (GenericEntityException e1) {
    						e1.printStackTrace();
    					}
    		        	
    		        	if (OrderRule != null){
            				//为此订单创建评价
            				Map<String,Object> paramContext = FastMap.newInstance();
            		        paramContext.put("userLogin",userLogin);
            		        paramContext.put("productStoreId",evaluateOrderHeader.getString("productStoreId"));
            		        paramContext.put("productRating", new BigDecimal(5));
            		        paramContext.put("productReview", "好评!");
            		        paramContext.put("orderId", orderId);
            		        paramContext.put("productId",productId);
            		        try {
    							Map<String,Object> resultContext = dispatcher.runSync("createProductReview", paramContext);
    						} catch (GenericServiceException e) {
    							e.printStackTrace();
    						}
            				//订单状态变为已完成
            				 Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_COMPLETED", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
                             try {
                            	 OrderServices.setOrderStatus(dispatcher.getDispatchContext(), serviceContext);
                             } catch (Exception e) {
                                 Debug.logError(e, "Problem calling change item status service : " + serviceContext, module);
                             }
    		        	}
    				}
    	        } catch (GenericEntityException e2) {
    				e2.printStackTrace();
    			}
    		}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
		}
		return result;
	}
	
	/**
	 * 退单自动审核时间 add by gss 2016.07.11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> auditReturnOrder(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		Map<String, Object> resultData = FastMap.newInstance();
		LocalDispatcher dispatcher = dctx.getDispatcher();
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		String partyId = "";
		//获取订单规则
		try {
			 GenericValue OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
			//退单自动审核时间
            int returnToexamineStamp=0;
			if(UtilValidate.isNotEmpty(OrderRule)){
				//退单自动审核时间
				returnToexamineStamp = OrderRule.getLong("returnToexamineStamp").intValue();
			}
			if(returnToexamineStamp>0){
				//退单自动审核时间
		        List<GenericValue> orderReturn = null;
		        Calendar cal = Calendar.getInstance();
	            cal.setTimeInMillis(System.currentTimeMillis());
	            cal.add(Calendar.DAY_OF_YEAR, -returnToexamineStamp);
	            Date reviewDate = cal.getTime();
	            // 创建查询表达式 ---退货单
	            EntityCondition returnecl = EntityCondition.makeCondition(
	            		UtilMisc.toList(
	                    	EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_WAITEXAMINE"),//待审核
	                    	EntityCondition.makeCondition("entryDate", EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(reviewDate.getTime()))
	            		), 
	            		EntityOperator.AND);
		        try {
					orderReturn = delegator.findList("ReturnHeader", returnecl, null, UtilMisc.toList("entryDate"), null, false);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
		        List<GenericValue> toBeSaved = new LinkedList<GenericValue>();
		        for(GenericValue orderReturns : orderReturn) {
		        	 //退单ID
		        	 String returnId=(String)orderReturns.get("returnId");
//		        	 String orderId=(String)orderReturns.get("orderId");
		        	 try {
		     				//退单表头
		     	           GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader",UtilMisc.toMap("returnId",returnId));
		     	           returnHeader.set("statusId","RETURN_WAITFEFUND");//待退款
		     	           GenericValue returnItem = EntityUtil.getFirst(delegator.findByAnd("ReturnItem",UtilMisc.toMap("returnId",returnId)));
		     	           String productName = "";
		     	           String applyMoney = "";
		     	           String returnType = "";
		     	           if(UtilValidate.isNotEmpty(returnItem)){
		     	        	  returnItem.set("statusId","RETURN_WAITFEFUND");//待退款
		     	        	  GenericValue returnStatus = delegator.makeValue("ReturnStatus",UtilMisc.toMap("returnStatusId",delegator.getNextSeqId("ReturnStatus"),
			     	                   "statusId","RETURN_WAITFEFUND","returnId",returnId,"returnItemSeqId",returnItem.get("returnItemSeqId"),"changeByUserLoginId",userLogin.get("userLoginId"),
			     	                   "statusDatetime", UtilDateTime.nowTimestamp()));
		     	        	  toBeSaved.add(returnItem);
			     	          toBeSaved.add(returnStatus);//applyMoney
							   applyMoney = returnItem.getBigDecimal("applyMoney").setScale(2,BigDecimal.ROUND_HALF_UP).toString();
			     	          String productId = returnItem.getString("productId");
			     	          if (UtilValidate.isNotEmpty(productId)){
			     	          	GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",productId));
			     	          	productName = product.getString("productName");
							  }
							   returnType = returnItem.getString("returnType");

		     	           }
		     	           toBeSaved.add(returnHeader);
						   delegator.storeAll(toBeSaved);
						   if ("0".equals(returnType)){

							   if (UtilValidate.isNotEmpty(userLogin)){
								   partyId = userLogin.getString("partyId");
							   }

							   GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
							   String openId = person.getString("wxAppOpenId");
							   Map<String, Object> daMap = FastMap.newInstance();
							   daMap.put("keyword1", returnId);
							   daMap.put("keyword2", "商家同意了您的退货申请");
							   daMap.put("keyword3", applyMoney);
							   daMap.put("keyword4", productName);
							   try {
//
								   if (UtilValidate.isNotEmpty(openId)){
									   resultData = dispatcher.runSync("xgro-sendTemplateMsgReturnProductSuccess", UtilMisc.toMap( "templateSendType", "REFUND_APPLY_NOTIFY", "touser", openId, "data", daMap, "partyId", userLogin.getString("partyId"), "objectValueId", returnHeader.getString("orderId")));
								   }

							   }catch (Exception e) {
								   e.printStackTrace();
								   resultData.put("retCode", 0);
								   resultData.put("message", e.getMessage());
							   }
						   }else {

							   if (UtilValidate.isNotEmpty(userLogin)){
								   partyId = userLogin.getString("partyId");
							   }

							   GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
							   String openId = person.getString("wxAppOpenId");
							   Map<String, Object> daMap = FastMap.newInstance();
							   daMap.put("keyword1", returnId);
							   daMap.put("keyword2", "商家同意了您的退款申请");
							   daMap.put("keyword3", applyMoney);
							   daMap.put("keyword4", productName);
							   try {
//
								   if (UtilValidate.isNotEmpty(openId)){
									   resultData = dispatcher.runSync("xgro-sendTemplateMsgReturnSuccess", UtilMisc.toMap( "templateSendType", "REFUND_APPLY_NOTIFY", "touser", openId, "data", daMap, "partyId", userLogin.getString("partyId"), "objectValueId", returnHeader.getString("orderId")));
								   }

							   }catch (Exception e) {
								   e.printStackTrace();
								   resultData.put("retCode", 0);
								   resultData.put("message", e.getMessage());
							   }

						   }




						} catch (GenericEntityException e) {
							e.printStackTrace();
						}
		        }
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
		}
		return result;
	}
	
	/**
	 * 退单自动取消时间 add by gss 2016.07.11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> cancelReturnOrder(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		LocalDispatcher dispatcher = dctx.getDispatcher();
		String partyId = "";
		Map<String, Object> resultData = FastMap.newInstance();
		//获取订单规则
		try {
			GenericValue OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
			//退单自动取消时间
			int returnCancelStamp=0;
			if(UtilValidate.isNotEmpty(OrderRule)){
				//退单自动取消时间
				returnCancelStamp = OrderRule.getLong("returnCancelStamp").intValue();
			}
			if(returnCancelStamp>0){
				//退单自动取消时间
		        List<GenericValue> ordercancelReturn = null;
		        Calendar cal = Calendar.getInstance();
	            cal.setTimeInMillis(System.currentTimeMillis());
	            cal.add(Calendar.DAY_OF_YEAR, -returnCancelStamp);
	            Date reviewDate = cal.getTime();
	            // 创建查询表达式 ---退货单 待发货 ---拒绝
	            EntityCondition cancelecl = EntityCondition.makeCondition(
	            		UtilMisc.toList(
	                    	EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_WAITSHIP"),//待发货 ---拒绝
	                    	EntityCondition.makeCondition("entryDate", EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(reviewDate.getTime()))
	            		), 
	            		EntityOperator.AND);
		        try {
		        	ordercancelReturn = delegator.findList("ReturnHeader", cancelecl, null, UtilMisc.toList("entryDate"), null, false);
				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
		        List<GenericValue> toBeSaved = new LinkedList<GenericValue>();
		        for(GenericValue ordercancelReturns : ordercancelReturn) {
		       	 //退单ID
		       	 String returnId=(String)ordercancelReturns.get("returnId");
    				 try {
    				//退单表头
    	           GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader",UtilMisc.toMap("returnId",returnId));
    	           returnHeader.set("statusId","RETURN_REJECTAPPLY");//拒绝申请
    	           GenericValue returnItem = delegator.findByAnd("ReturnItem",UtilMisc.toMap("returnId",returnId)).get(0);
    	           //returnReason;
				 String productName = "";
				 String returnReason = "";
				 String applyMoney = "";
				 String returnType = "";
				 String statusId = "";

				 if (UtilValidate.isNotEmpty(returnItem)){
					 String productId  = returnItem.getString("productId");
					 GenericValue product = delegator.findByPrimaryKey("Product",UtilMisc.toMap("productId",productId));
					 productName = product.getString("productName");
					 //退款原因
					 returnReason = returnItem.getString("returnReason");
					 //退款金额
					 applyMoney = returnItem.getBigDecimal("applyMoney").setScale(2,BigDecimal.ROUND_HALF_UP).toString();
				 	 //退单类型 0退货 1退款
					 returnType = returnItem.getString("returnType");
					 statusId = returnItem.getString("statusId");

				 }

    	           returnItem.set("statusId","RETURN_REJECTAPPLY");//拒绝申请
    	           GenericValue returnStatus = delegator.makeValue("ReturnStatus",UtilMisc.toMap("returnStatusId",delegator.getNextSeqId("ReturnStatus"),
    	                   "statusId","RETURN_REJECTAPPLY","returnId",returnId,"returnItemSeqId",returnItem.get("returnItemSeqId"),"changeByUserLoginId",userLogin.get("userLoginId"),
    	                   "statusDatetime", UtilDateTime.nowTimestamp()));
    	           toBeSaved.add(returnHeader);
    	           toBeSaved.add(returnItem);
    	           toBeSaved.add(returnStatus);
				   delegator.storeAll(toBeSaved);

				 if (UtilValidate.isNotEmpty(userLogin)){
					 partyId = userLogin.getString("partyId");
				 }
				 if ("0".equals(returnType)){
					 GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
					 String openId = person.getString("wxAppOpenId");
					 Map<String, Object> daMap = FastMap.newInstance();
					 daMap.put("keyword1", returnId);
					 daMap.put("keyword2", "商家驳回了您的退款申请");//statusId
					 daMap.put("keyword3", applyMoney);
					 daMap.put("keyword4", productName);

					 try {
//
						 if (UtilValidate.isNotEmpty(openId)){
							 resultData = dispatcher.runSync("xgro-sendTemplateMsgReturnProductFail", UtilMisc.toMap( "templateSendType", "REFUND_APPLY_NOTIFY", "touser", openId, "data", daMap, "partyId", userLogin.getString("partyId"), "objectValueId", returnHeader.getString("orderId")));
						 }

					 }catch (Exception e) {
						 e.printStackTrace();
						 resultData.put("retCode", 0);
						 resultData.put("message", e.getMessage());
					 }

				 }else {
					 GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
					 String openId = person.getString("wxAppOpenId");
					 Map<String, Object> daMap = FastMap.newInstance();
					 daMap.put("keyword1", returnId);
					 daMap.put("keyword2", "商家同意了您的退货申请");
					 daMap.put("keyword3", applyMoney);
					 daMap.put("keyword4", productName);

					 try {
//
						 if (UtilValidate.isNotEmpty(openId)){
							 resultData = dispatcher.runSync("xgro-sendTemplateMsgReturnFail", UtilMisc.toMap( "templateSendType", "REFUND_APPLY_NOTIFY", "touser", openId, "data", daMap, "partyId", userLogin.getString("partyId"), "objectValueId", returnHeader.getString("orderId")));
						 }

					 }catch (Exception e) {
						 e.printStackTrace();
						 resultData.put("retCode", 0);
						 resultData.put("message", e.getMessage());
					 }
				 }


				} catch (GenericEntityException e) {
					e.printStackTrace();
				}
		      }
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
		}
		return result;
	}
	
	/**
	 * 退单自动确认收货时间 add by gss 2016.07.11
	 * @param dctx
	 * @param context
	 * @return
	 */
	public static Map<String, Object> confirmReturnOrder(DispatchContext dctx, Map<String, ? extends Object> context){
		Map<String, Object> result = ServiceUtil.returnSuccess();	//返回的结果集
		Delegator delegator = dctx.getDelegator();	//delegator对象
		GenericValue userLogin = (GenericValue) context.get("userLogin");
		//获取订单规则
		try {
			GenericValue OrderRule =delegator.findByPrimaryKey("OrderRule", UtilMisc.toMap("orderRuleId", "order_Rule"));
			//退单自动确认收货时间
			int returnConfirmStamp=0;
			if(UtilValidate.isNotEmpty(OrderRule)){
				//退单自动确认收货时间
				returnConfirmStamp= OrderRule.getLong("returnConfirmStamp").intValue();
			}
			if(returnConfirmStamp>0){
				//退单自动确认收货时间returnConfirmStamp
		        Calendar cal = Calendar.getInstance();
	            cal.setTimeInMillis(System.currentTimeMillis());
	            cal.add(Calendar.DAY_OF_YEAR, -returnConfirmStamp);
	            Date reviewDate = cal.getTime();
	            // 创建查询表达式 ---退货单 待收货 ---待退款
	            EntityCondition confirmexprs = EntityCondition.makeCondition(
	            		UtilMisc.toList(
	                    	EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "RETURN_WAITRECEIVE"),//待收货 ---待退款
	                    	EntityCondition.makeCondition("entryDate", EntityOperator.LESS_THAN_EQUAL_TO, new Timestamp(reviewDate.getTime()))
	            		), 
	            		EntityOperator.AND);
				 //退单自动确认收货时间returnConfirmStamp
		        List<GenericValue> returnConfirmReturn = null;
		        try {
		        	returnConfirmReturn = delegator.findList("ReturnHeader", confirmexprs, null, UtilMisc.toList("entryDate"), null, false);
		        } catch (GenericEntityException e) {
		        	e.printStackTrace();
		        }
		        List<GenericValue> toBeSaved = new LinkedList<GenericValue>();
		        for(GenericValue returnConfirmReturns : returnConfirmReturn) {
		        	//退单ID
		        	String returnId=(String)returnConfirmReturns.get("returnId");
        			try {
        				//退单表头
        				GenericValue returnHeader = delegator.findByPrimaryKey("ReturnHeader",UtilMisc.toMap("returnId",returnId));
        				returnHeader.set("statusId","RETURN_WAITFEFUND");//待退款
        				GenericValue returnItem = delegator.findByAnd("ReturnItem",UtilMisc.toMap("returnId",returnId)).get(0);
        				returnItem.set("statusId","RETURN_WAITFEFUND");//待退款
        				GenericValue returnStatus = delegator.makeValue("ReturnStatus",UtilMisc.toMap("returnStatusId",delegator.getNextSeqId("ReturnStatus"),
        						"statusId","RETURN_WAITFEFUND","returnId",returnId,"returnItemSeqId",returnItem.get("returnItemSeqId"),"changeByUserLoginId",userLogin.get("userLoginId"),
        						"statusDatetime", UtilDateTime.nowTimestamp()));
        				toBeSaved.add(returnHeader);
        				toBeSaved.add(returnItem);
        				toBeSaved.add(returnStatus);
        				delegator.storeAll(toBeSaved);
        			} catch (GenericEntityException e) {
        				e.printStackTrace();
        			}
		        }
			}
		} catch (GenericEntityException e) {
			Debug.logError(e, "Unable to get ProductStore from OrderHeader", module);
		}
		return result;
	}
	
	
}


