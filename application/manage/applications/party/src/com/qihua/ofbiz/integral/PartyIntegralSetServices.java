/*
 * 文件名：PartyIntegralSetServices.java
 * 版权：启华
 * 描述：积分使用规则服务类
 * 修改人：gss
 * 修改时间：2016-1-26
 * 修改单号：
 * 修改内容：
 */
package com.qihua.ofbiz.integral;

import java.util.Map;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class PartyIntegralSetServices{
    public static final String module = PartyIntegralSetServices.class.getName();
    public static final String resource = "ContentUiLabels";
    /**
     * 更新积分使用规则
     * @param dcx
     * @param context
     * @return
     */
	public static Map<String, Object> updatePartyIntegralSet(DispatchContext dctx,
			Map<String, ? extends Object> context) {
		 Delegator delegator = dctx.getDelegator();
		 Map<String, Object> result = ServiceUtil.returnSuccess();
		 //需要积分
		 String integralValue = (String) context.get("integralValue");
		 String partyIntegralSetId = (String) context.get("partyIntegralSetId");
		 try {
			//更新需要的积分
			GenericValue partyIntegralSet = delegator.findByPrimaryKey("PartyIntegralSet", UtilMisc.toMap("partyIntegralSetId", partyIntegralSetId));
			partyIntegralSet.set("integralValue", new Long(integralValue));
			partyIntegralSet.store();
		 } catch (GenericEntityException e) {
			e.printStackTrace();
		}
		return result;
	}	
}


