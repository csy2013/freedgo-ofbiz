package com.yuaoq.yabiz.product.facility;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
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
 * 标签service
 * @author 钱进 2016.05.03
 *
 */
public class LabelServices {
    
    /**
     * 查询订单信息 add by qianjin 2016.05.03
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getOrderItemByOrderIdAndSeqId(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //返回参数
        Map order_map = FastMap.newInstance();
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("OI","OrderItem");
        dynamicView.addAlias("OI","orderId");
        dynamicView.addAlias("OI","orderItemSeqId");
        dynamicView.addAlias("OI","quantity");
        
        dynamicView.addMemberEntity("P","Product");
        dynamicView.addAlias("P","productName");
        dynamicView.addAlias("P","barcode");
        dynamicView.addAlias("P","volume");
        dynamicView.addViewLink("OI","P", Boolean.FALSE,ModelKeyMap.makeKeyMapList("productId", "productId"));
        
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        
        //按订单号查询
        if (UtilValidate.isNotEmpty(context.get("orderId"))) {
        	filedExprs.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS,context.get("orderId")));
        }
        
        //按行项目号查询
        if (UtilValidate.isNotEmpty(context.get("orderItemSeqId"))) {
        	filedExprs.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS,context.get("orderItemSeqId")));
        }
        
        //添加where条件
        if (filedExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }
        
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, findOpts);
            // 获取所需的记录集合
            for(GenericValue gv : pli.getCompleteList()){
            	String orderId = gv.getString("orderId");
            	String orderItemSeqId = gv.getString("orderItemSeqId");
            	String quantity = UtilMisc.doubleTrans(gv.getDouble("quantity"));
            	String productName = gv.getString("productName");
            	String barcode = gv.getString("barcode");
            	String volume = UtilMisc.doubleTrans(gv.getDouble("volume"));
            	
            	order_map.put("orderId", orderId);
            	order_map.put("orderItemSeqId", orderItemSeqId);
            	order_map.put("quantity", quantity);
            	order_map.put("productName", productName);
            	order_map.put("barcode", barcode);
            	order_map.put("volume", volume);
            	
            	//查询配料动态view
                DynamicViewEntity dv = new DynamicViewEntity();
                dv.addMemberEntity("OISPP","OrderItemSemiProductProportion");
                dv.addAlias("OISPP","orderId");
                dv.addAlias("OISPP","orderItemSeqId");
                
                dv.addMemberEntity("P","Product");
                dv.addAlias("P","productName");
                dv.addAlias("P","productTypeId");
                dv.addAlias("P","productId");
                dv.addViewLink("OISPP","P", Boolean.FALSE,ModelKeyMap.makeKeyMapList("semiProductProportionId", "productId"));
                
                EntityCondition cond =	EntityCondition.makeCondition(
                		UtilMisc.toList(
                					EntityCondition.makeCondition("orderId", EntityOperator.EQUALS,context.get("orderId")),
                					EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS,context.get("orderItemSeqId"))
                				)
                		,EntityOperator.AND);
                
                
                //查询配料数据的Iterator
                EntityListIterator i_pli = delegator.findListIteratorByCondition(dv, cond, null, null, UtilMisc.toList("-productTypeId","productId"), findOpts);
                String ingredients = "";
                //编辑配料名称
                for(int i = 0;i<i_pli.getCompleteList().size();i++){
                	GenericValue i_gv = i_pli.getCompleteList().get(i);
                	ingredients += i_gv.getString("productName");
                	if(i != i_pli.getCompleteList().size()-1){
                		ingredients += "、";
                	}
                }
                order_map.put("ingredients", ingredients);
                
                i_pli.close();
            }

            //关闭 iterator
            pli.close();
		} catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }
        
        result.put("orderMap", order_map);
        result.put("deliveryMap", getOrderDeliveryByOrderId(dctx,context).get("deliveryMap"));
        return result;
    }
    
    /**
     * 查询订单配送信息 add by qianjin 2016.05.04
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getOrderDeliveryByOrderId(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //返回参数
        Map delivery_map = FastMap.newInstance();
        
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("OD","OrderDelivery");
        dynamicView.addAlias("OD","id");
        dynamicView.addAlias("OD","orderId");
        dynamicView.addAlias("OD","isPrint");
        dynamicView.addAlias("OD","logisticsNumber1");
        dynamicView.addAlias("OD","productName");
        dynamicView.addAlias("OD","createdStamp");
        
        dynamicView.addMemberEntity("LC","LogisticsCompany");
        dynamicView.addAlias("LC","companyName");
        dynamicView.addViewLink("OD","LC", Boolean.FALSE,ModelKeyMap.makeKeyMapList("deliveryCompany", "id"));
        
        //查询条件集合，用于数据库查询
        List<EntityCondition> filedExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        
        //按订单号查询
        if (UtilValidate.isNotEmpty(context.get("orderId"))) {
        	filedExprs.add(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS,context.get("orderId")));
        }
        
        //添加where条件
        if (filedExprs.size() > 0){
        	mainCond = EntityCondition.makeCondition(filedExprs,EntityOperator.AND);
        }
        
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, UtilMisc.toList("-createdStamp"), findOpts);
            // 获取所需的记录集合
            GenericValue gv = EntityUtil.getFirst(pli.getCompleteList());
            if(UtilValidate.isNotEmpty(gv)){
            	String id = gv.getString("id");
            	String orderId = gv.getString("orderId");
            	String isPrint = gv.getString("isPrint");
            	String logisticsNumber1 = gv.getString("logisticsNumber1");
            	String productName = gv.getString("productName");
            	String companyName = gv.getString("companyName");
            	
            	delivery_map.put("id", id);
            	delivery_map.put("orderId", orderId);
            	delivery_map.put("isPrint", isPrint);
            	delivery_map.put("logisticsNumber1", logisticsNumber1);
            	delivery_map.put("productName", productName);
            	delivery_map.put("companyName", companyName);
            	
            	
            	//查询订单商品总数
            	String totalNum = "0"; 
            	//查询订单商品总数动态view
                DynamicViewEntity dv = new DynamicViewEntity();
                dv.addMemberEntity("OI","OrderItem");
                dv.addAlias("OI","orderId");
                dv.addAlias("OI","isStorage");
                dv.addAlias("OI","quantity","quantity","quantity",false,false,"sum");
                EntityCondition cond_1 =	EntityCondition.makeCondition(
                		UtilMisc.toList(
                					EntityCondition.makeCondition("orderId", EntityOperator.EQUALS,context.get("orderId"))
                				)
                		,EntityOperator.AND);
                //查询订单商品总数的Iterator
                EntityListIterator pli_1 = delegator.findListIteratorByCondition(dv, cond_1, null, null, null, findOpts);
                GenericValue gv_1 = EntityUtil.getFirst(pli_1.getCompleteList());
                if(UtilValidate.isNotEmpty(gv_1)){
                	double quantity = UtilValidate.isNotEmpty(gv_1.getDouble("quantity")) ? gv_1.getDouble("quantity") : 0;
                	totalNum = UtilMisc.doubleTrans(gv_1.getDouble("quantity"));
                }
                pli_1.close();
                delivery_map.put("totalNum", totalNum);
                
                //查询订单已入库商品数量
                String storageNum = "0";
                EntityCondition cond_2 =	EntityCondition.makeCondition(
                		UtilMisc.toList(
                					EntityCondition.makeCondition("orderId", EntityOperator.EQUALS,context.get("orderId")),
                					EntityCondition.makeCondition("isStorage", EntityOperator.EQUALS,"Y")
                				)
                		,EntityOperator.AND);
                //查询订单已入库商品数量的Iterator
                EntityListIterator pli_2 = delegator.findListIteratorByCondition(dv, cond_2, null, null, null, findOpts);
                GenericValue gv_2 = EntityUtil.getFirst(pli_2.getCompleteList());
                if(UtilValidate.isNotEmpty(gv_2)){
                	double quantity = UtilValidate.isNotEmpty(gv_2.getDouble("quantity")) ? gv_2.getDouble("quantity") : 0;
                	storageNum = UtilMisc.doubleTrans(quantity);
                } 
                pli_2.close();
                delivery_map.put("storageNum", storageNum);
            }
            
            //关闭 iterator
            pli.close();
		} catch (GenericEntityException e) {
            String errMsg = "Failure in party find operation, rolling back transaction: " + e.toString();
        }
        
        result.put("deliveryMap", delivery_map);
        return result;
    }
    
    /**
     * 查询标签设定信息 add by qianjin 2016.05.04
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getLabelPrintSet(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        try {
        	//标签实体
			GenericValue gv = EntityUtil.getFirst(delegator.findByAnd("LabelPrintSet", FastMap.newInstance()));
			//标签和营养成分关联数据
			List<GenericValue> lpsa_list = FastList.newInstance();
			if(UtilValidate.isNotEmpty(gv)){
				String labelPrintSetId = gv.getString("labelPrintSetId");
				lpsa_list = delegator.findByAnd("LabelPrintSetAssoc", UtilMisc.toMap("labelPrintSetId", labelPrintSetId));
			}
			result.put("labelPrintSet", gv);
			result.put("labelPrintSetAssocList", lpsa_list);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        return result;
    }  
    
    /**
     * 添加标签打印配置 add by qianjin 2016.05.04
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addLabelSet(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String labelPrintSetId = (String) context.get("labelPrintSetId");
        String headInfo = (String) context.get("headInfo");
        String htmlContent = (String) context.get("htmlContent");
        String nutrients = (String) context.get("nutrients");
        
        try {
	        //判断是新增还是修改
	        if(UtilValidate.isNotEmpty(labelPrintSetId)){
	        	GenericValue gv = delegator.findByPrimaryKey("LabelPrintSet", UtilMisc.toMap("labelPrintSetId", labelPrintSetId));
	        	gv.set("headInfo", headInfo);
	        	gv.set("htmlContent", htmlContent);
	        	gv.store();
	        	//删除关联的营养成分
	        	delegator.removeByAnd("LabelPrintSetAssoc", UtilMisc.toMap("labelPrintSetId", labelPrintSetId));
	        }else{
	        	labelPrintSetId = delegator.getNextSeqId("LabelPrintSet");
	        	GenericValue gv = delegator.makeValue("LabelPrintSet", UtilMisc.toMap("labelPrintSetId", labelPrintSetId));
	        	gv.set("headInfo", headInfo);
	        	gv.set("htmlContent", htmlContent);
	        	gv.create();
	        }
	        
	        //创建营养成分的关联记录
	  		JSONArray nutrients_arr = JSONArray.fromObject(nutrients);
	  		if(!nutrients_arr.isEmpty()){
	  			for(Object obj : nutrients_arr){
	  				JSONObject nutrient = JSONObject.fromObject(obj);
	  				//新的ID
	  				String labelPrintSetAccosId = delegator.getNextSeqId("LabelPrintSetAssoc");
	  				GenericValue lpsa_gv = delegator.makeValue("LabelPrintSetAssoc", UtilMisc.toMap("labelPrintSetAccosId",labelPrintSetAccosId));
	  				lpsa_gv.set("labelPrintSetId", labelPrintSetId);
	  				lpsa_gv.set("nutrientId", nutrient.get("id"));
	  				lpsa_gv.set("nutrientName", nutrient.get("name"));
	  				lpsa_gv.set("nutrientUnit", nutrient.get("dw"));
	  				lpsa_gv.create();
	  			}
	  		} 
        } catch (GenericEntityException e) {
			e.printStackTrace();
		}
        return result;
    }
    
    /**
     * 订单入库 add by qianjin 2016.05.07
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> orderItemStorage(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //LocalDispatcher对象  
        LocalDispatcher dispatcher = dctx.getDispatcher();
        
        //当前用户登录信息  
    	GenericValue userLogin = (GenericValue) context.get("userLogin");
        //参数
        String orderId = (String) context.get("orderId");
        String orderItemSeqId = (String) context.get("orderItemSeqId");
        
		try {
			//修改订单项的出库标识
			GenericValue oi_gv = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", orderId,"orderItemSeqId",orderItemSeqId));
			oi_gv.setString("isStorage", "Y");
		    oi_gv.store();
		    
		    //判断该订单下是否有未入库的行项目
		    List<GenericValue> oi_list = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId,"isStorage",null));
		    if(UtilValidate.isEmpty(oi_list)){
		    	GenericValue oh_gv = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
		    	//判断订单状态如果为待生产，则修改整个订单状态
		    	if("ORDER_WAITPRODUCE".equals(oh_gv.getString("statusId"))){
		    		//订单状态改变服务
				    Map<String, Object> serviceContext = UtilMisc.<String, Object>toMap("orderId", orderId, "statusId", "ORDER_WAITSHIP", "userLogin", userLogin, "locale", Locale.CHINESE, "setItemStatus", "Y");
				    dispatcher.runSyncIgnore("setOrderStatus", serviceContext);
		    	}
		    }
		    
		    //SAP订单入库接口
		    Map<String, Object> sap_map = UtilMisc.<String, Object>toMap("VBELN", orderId, "POSNR", orderItemSeqId, "MATNR", oi_gv.getString("productId"), "KWMENG", UtilMisc.doubleTrans(oi_gv.getDouble("quantity")));
		    dispatcher.runSyncIgnore("productStorageWS", sap_map);
		} catch (GenericEntityException e) {
			e.printStackTrace();
		} catch (GenericServiceException e) {
			e.printStackTrace();
		}
        return result;
    }
    
    /**
     * 根据订单号查询快递单 add by qianjin 2016.05.07
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getExpressOrderByOrderId(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //返回参数
        Map expressOrderInfo = FastMap.newInstance();
        //参数
        String orderId = (String) context.get("orderId");
        
        try {
			//获取该订单号下所有行项目信息
			List<GenericValue> oi_list = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId", orderId));
			String productName = "";
			double referenceWeight = 0;
            if(UtilValidate.isNotEmpty(oi_list)){
            	for(int i=0;i < oi_list.size();i++){
            		GenericValue oi_gv = oi_list.get(i);
            		//获取商品实体
	            	GenericValue p_gv = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", oi_gv.getString("productId")));
	            	//拼接商品名称
	            	productName += p_gv.getString("productName");
	            	if(i < oi_list.size()-1){
	            		productName += ",";
	            	}
	            	//累计商品重量
	            	if(UtilValidate.isNotEmpty(p_gv.getDouble("weight"))){
	            		referenceWeight += p_gv.getDouble("weight") * oi_gv.getDouble("quantity");
	            	}
            	}
            	
            }
            expressOrderInfo.put("productName", productName);
            expressOrderInfo.put("referenceWeight", UtilMisc.doubleTrans(referenceWeight));

        	//根据订单号获取快递单信息
			List<GenericValue> od_list = delegator.findByAnd("OrderDelivery",UtilMisc.toMap("orderId", orderId));
			if(UtilValidate.isNotEmpty(od_list)){
				GenericValue od_gv = EntityUtil.getFirst(od_list);
				String logisticsNumber1 = od_gv.getString("logisticsNumber1");
				String sendName = od_gv.getString("sendName");
				String sendAddress = od_gv.getString("sendAddress");
				String sendTelphone = od_gv.getString("sendTelphone");
				String deliveryCompany=od_gv.getString("deliveryCompany");
				
				expressOrderInfo.put("sendName", sendName);
				expressOrderInfo.put("sendAddress", sendAddress);
				expressOrderInfo.put("sendTelphone", sendTelphone);
				expressOrderInfo.put("orderDeliveryId", od_gv.getString("id"));
				expressOrderInfo.put("logisticsNumber1", logisticsNumber1);
				expressOrderInfo.put("deliveryCompany", deliveryCompany);
				
				//根据快递公司ID查询快递公司信息
				GenericValue lc_gv = delegator.findByPrimaryKey("LogisticsCompany", UtilMisc.toMap("id", deliveryCompany));
				String companyName = lc_gv.getString("companyName");
				
				expressOrderInfo.put("companyName", companyName);
				
				//根据快递公司ID查询物流单据配置
				String logisticsCompanyId = "";
				List<GenericValue> ld_list = delegator.findByAnd("LogisticsDocuments", UtilMisc.toMap("logisticsCompanyId", deliveryCompany));
				if(UtilValidate.isNotEmpty(ld_list)){
					GenericValue ld_gv = EntityUtil.getFirst(ld_list);
					logisticsCompanyId = ld_gv.getString("logisticsCompanyId");
					String width = ld_gv.getString("width");
					String height = ld_gv.getString("height");
					String contentId = ld_gv.getString("contentId");
					
					expressOrderInfo.put("width", width);
					expressOrderInfo.put("height", height);
					expressOrderInfo.put("contentId", contentId);
					
					//替换单据配置信息
					String content = ld_gv.getString("content");
					String toName = "";
					String province = "";
					String city = "";
					String country = "";
					String address1 = "";
					String mobilePhone = "";
					String postalCode = "";
					
					if(UtilValidate.isNotEmpty(sendName)){
						content = content.replace("发件人-姓名<i class=\"del\"></i><i class=\"resize_btn\"></i>", sendName);
					}
					if(UtilValidate.isNotEmpty(sendAddress)){
						content = content.replace("发件人-地址<i class=\"del\"></i><i class=\"resize_btn\"></i>", sendAddress);
					}
					if(UtilValidate.isNotEmpty(sendTelphone)){
						content = content.replace("发件人-联系电话<i class=\"del\"></i><i class=\"resize_btn\"></i>", sendTelphone);
					}
					if(UtilValidate.isNotEmpty(productName)){
						content = content.replace("货品名称<i class=\"del\"></i><i class=\"resize_btn\"></i>", productName);
					}
					Map info = getOrderToCustomerInfo(delegator,orderId);
					if(UtilValidate.isNotEmpty(info)) {
						if(UtilValidate.isNotEmpty(info.get("toName"))){
							toName = info.get("toName").toString();
							content = content.replace("收件人-姓名<i class=\"del\"></i><i class=\"resize_btn\"></i>", toName);
						}
						if(UtilValidate.isNotEmpty(info.get("province"))){
							province = info.get("province").toString();
							content = content.replace("收件人-省<i class=\"del\"></i><i class=\"resize_btn\"></i>", province);
						}
						if(UtilValidate.isNotEmpty(info.get("city"))){
							city = info.get("city").toString();
							content = content.replace("收件人-市<i class=\"del\"></i><i class=\"resize_btn\"></i>", city);
						}
						if(UtilValidate.isNotEmpty(info.get("country"))){
							country = info.get("country").toString();
							content = content.replace("收件人-区<i class=\"del\"></i><i class=\"resize_btn\"></i>", country);
						}
						if(UtilValidate.isNotEmpty(info.get("address1"))){
							address1 = info.get("address1").toString();
							content = content.replace("收件人-地址<i class=\"del\"></i><i class=\"resize_btn\"></i>", address1);
						}
						if(UtilValidate.isNotEmpty(info.get("mobilePhone"))){
							mobilePhone = info.get("mobilePhone").toString();
							content = content.replace("收件人-手机号码<i class=\"del\"></i><i class=\"resize_btn\"></i>", mobilePhone);
						}
						if(UtilValidate.isNotEmpty(info.get("postalCode"))){
							postalCode = info.get("postalCode").toString();
							content = content.replace("收件人-邮政编码<i class=\"del\"></i><i class=\"resize_btn\"></i>", postalCode);
						}
		            }else{
		            	content = "";
		            }
					expressOrderInfo.put("toName", toName);
					expressOrderInfo.put("province", province);
					expressOrderInfo.put("city", city);
					expressOrderInfo.put("country", country);
					expressOrderInfo.put("address1", address1);
					expressOrderInfo.put("mobilePhone", mobilePhone);
					expressOrderInfo.put("postalCode", postalCode);
					expressOrderInfo.put("content", content);
					expressOrderInfo.put("logisticsCompanyId", logisticsCompanyId);
				}
			}
			
		} catch (GenericEntityException e) {
			e.printStackTrace();
		}
        
        result.put("expressOrderInfo",expressOrderInfo);
        return result;
    }
    
  //add by AlexYao 得到收货人信息
    public static  Map<String,Object>   getOrderToCustomerInfo(Delegator delegator,String orderId) {
        DynamicViewEntity dve = new DynamicViewEntity();
        Map<String,Object> info = new HashMap<String, Object>();
        List<EntityCondition> conditions = FastList.newInstance();
        conditions.add(EntityCondition.makeCondition("orderId", orderId));
        conditions.add(EntityCondition.makeCondition("roleTypeId", "BILL_TO_CUSTOMER"));
        GenericValue billToCustomer = null;
        GenericValue receivingCustomer = null;
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, false);
        EntityListIterator eli = null;
        dve = new DynamicViewEntity();
        conditions.clear();
        dve.addMemberEntity("OCM", "OrderContactMech");
        dve.addMemberEntity("PA", "PostalAddress");
        dve.addAliasAll("OCM", "", null);
        dve.addAliasAll("PA", "", null);
        dve.addViewLink("OCM", "PA", Boolean.FALSE, ModelKeyMap.makeKeyMapList("contactMechId"));
        conditions.add(EntityCondition.makeCondition("orderId", orderId));
        try {
            eli = delegator.findListIteratorByCondition(dve, EntityCondition.makeCondition(conditions, EntityOperator.AND), null, null, null, findOpts);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        try {
            if (UtilValidate.isNotEmpty(eli.getCompleteList())){
                receivingCustomer = eli.getCompleteList().get(0);
                info.put("toName",receivingCustomer.get("toName"));
                info.put("mobilePhone",receivingCustomer.get("mobilePhone"));
                //获取地址信息
                GenericValue province = delegator.findByPrimaryKey("Geo",UtilMisc.toMap("geoId", receivingCustomer.get("stateProvinceGeoId")));
                GenericValue city = delegator.findByPrimaryKey("Geo",UtilMisc.toMap("geoId", receivingCustomer.get("city")));
                GenericValue country = delegator.findByPrimaryKey("Geo",UtilMisc.toMap("geoId", receivingCustomer.get("countyGeoId")));
                if (province != null){
                    info.put("province",province.getString("geoName"));
                }
                if (city != null){
                    info.put("city",city.getString("geoName"));
                }
                if (country != null){
                    info.put("country",country.getString("geoName"));
                }
                info.put("address1",receivingCustomer.get("address1"));
                info.put("postalCode",receivingCustomer.get("postalCode"));
                info.put("tel",receivingCustomer.get("tel"));
            }else{
                info.put("toName","");
                info.put("mobilePhone","");
                info.put("address1","");
                info.put("postalCode","");
                info.put("tel","");
                info.put("province","");
                info.put("city","");
                info.put("country","");
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }

        if (eli != null) {
            try {
                eli.close();
            } catch (GenericEntityException e) {
            	e.printStackTrace();
            }
        }

        return info;
    }
}
