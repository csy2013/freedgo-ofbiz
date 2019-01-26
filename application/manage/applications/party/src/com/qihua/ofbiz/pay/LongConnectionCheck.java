package com.qihua.ofbiz.pay;


import com.qihua.ofbiz.member.MemberEvents;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * 用长连接，检查付款状态
 *
 * @author Alex
 */
public class LongConnectionCheck {
    public static String longConnectionCheck(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = MemberEvents.checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));
        String uuid = request.getParameter("uuid");
        String result = "";
        System.out.println("in");
        System.out.println("uuid:" + uuid);
        long inTime = System.currentTimeMillis();
        Boolean bool = true;
        if (UtilValidate.isNotEmpty(party.get("uuid")) && !UtilValidate.areEqual(uuid, party.get("uuid"))) {
            result = "error";
            bool = false;
            request.setAttribute("uuid", party.get("uuid"));
        }
        while (bool) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //检测付款
            List<GenericValue> partys = delegator.findByAnd("Party", UtilMisc.toMap("uuid", uuid));
            if (UtilValidate.isEmpty(partys)) {
                result = "success";
                bool = false;
            } else {
                if (System.currentTimeMillis() - inTime > 30000) {
                    result = "timeout";
                    bool = false;
                }
            }
        }
        request.setAttribute("result", result);
        return "success";
    }
}
