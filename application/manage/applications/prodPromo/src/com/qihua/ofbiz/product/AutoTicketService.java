/*
 * 文件名：AutoTicketService.java
 * 版权：启华
 * 描述：团购券自动过期服务类
 * 修改人：gss
 * 修改时间：2016-2-1
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.product;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;


public class  AutoTicketService{

	/***
	 * 团购券自动过期
	 * @param dctx
	 * @param context
	 * @return
	 */
    public static Map<String, Object> autoExpireTicket(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        DynamicViewEntity dve = new DynamicViewEntity();
		dve.addMemberEntity("TK", "Ticket");
		dve.addMemberEntity("PG", "ProductActivityGoods");
		dve.addMemberEntity("PT", "Product");
		//团购码状态
		dve.addAlias("TK", "ticketStatus");
		//团购码
		dve.addAlias("TK", "ticketId");
		//虚拟商品有效期至
		dve.addAlias("PG", "virtualProductEndDate");
		//商品类型
		dve.addAlias("PT", "productTypeId");
		dve.addViewLink("TK", "PG",Boolean.FALSE, ModelKeyMap.makeKeyMapList("activityId", "activityId"));
		dve.addViewLink("PG", "PT",Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));
		List<String> fieldsToSelect = FastList.newInstance();
		fieldsToSelect.add("ticketId");
		fieldsToSelect.add("virtualProductEndDate");
		List<EntityCondition> andExprs = FastList.newInstance();
		EntityCondition mainCond = null;
		List<GenericValue> ticketToCheck = null;
		//团购码是未使用
		andExprs.add(EntityCondition.makeCondition("ticketStatus",
				EntityOperator.EQUALS, "notUsed"));
		//商品类型为虚拟商品
		andExprs.add(EntityCondition.makeCondition("productTypeId",
				EntityOperator.EQUALS, "VIRTUAL_GOOD"));
		 if (andExprs.size() > 0)
         {
      	mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
         }
		 EntityFindOptions findOpts = new EntityFindOptions(true,
					EntityFindOptions.TYPE_SCROLL_INSENSITIVE,
					EntityFindOptions.CONCUR_READ_ONLY,  true);
		EntityListIterator pli;
		try {
			pli = delegator.findListIteratorByCondition(dve,
								mainCond, null, fieldsToSelect, null, findOpts);
			//获取所有未使用的团购券
	        ticketToCheck=pli.getCompleteList();
	        //关闭pli
	        pli.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        for(GenericValue ticketToChecks : ticketToCheck) {
        	//虚拟商品有效期至
        	Timestamp virtualProductEndDate = ticketToChecks.getTimestamp("virtualProductEndDate");
        	//团购码ID
        	String ticketId = ticketToChecks.getString("ticketId");
        	Date nowDate = new Date();
        	if (virtualProductEndDate.equals(nowDate) || nowDate.after(virtualProductEndDate))
            {
        		//团购券状态更新为已过期 
    			try {
					GenericValue Ticket = delegator.findByPrimaryKey("Ticket", UtilMisc.toMap("ticketId",ticketId));
					Ticket.set("ticketStatus", "expired");
					Ticket.store();
    			} catch (GenericEntityException e) {
					e.printStackTrace();
				}
            }
        }
        return ServiceUtil.returnSuccess();
    }
	
}
