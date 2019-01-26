package org.ofbiz.order.kuaidi100;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.collections.LifoSet;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.kuaidi100.pojo.NoticeRequest;
import org.ofbiz.order.kuaidi100.pojo.NoticeResponse;
import org.ofbiz.order.kuaidi100.pojo.Result;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class Callback extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Callback() {
        super();
    }

    public static String kuaidiCallback(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        JSONObject jsonObject = null;
        NoticeResponse resp = new NoticeResponse();
        resp.setResult(false);
        resp.setReturnCode("500");
        resp.setMessage("保存失败");
        try {
            String param = request.getParameter("param");
            NoticeRequest nReq = JacksonHelper.fromJSON(param,
                    NoticeRequest.class);

            Result result = nReq.getLastResult();
            // 处理快递结果
            jsonObject = JSONObject.fromObject(result.toString());
            
            String com = (String) jsonObject.get("com");
            String nu = (String) jsonObject.get("nu");
            String state = (String) jsonObject.get("state");
            JSONArray dataArray = (JSONArray) jsonObject.get("data");
            List<GenericValue> orderDeliverys = delegator.findByAnd("OrderDelivery", UtilMisc.toMap("deliveryCompany", com, "logisticsNumber1", nu));
            List<GenericValue> deliveryItems = delegator.findByAnd("DeliveryItem", UtilMisc.toMap("companyId", com, "logisticsNumber", nu));
            if(UtilValidate.isNotEmpty(orderDeliverys)){
                List<GenericValue> tobeStore;
                for(int i=0;i<orderDeliverys.size();i++){
                    orderDeliverys.get(i).set("state",state);
                }
                tobeStore = orderDeliverys;
                for(int j=0;j<dataArray.size();j++){
                    JSONObject data = (JSONObject) dataArray.get(j);
                    GenericValue deliveryItem = delegator.makeValue("DeliveryItem");
                    deliveryItem.set("id",delegator.getNextSeqId("DeliveryItem"));
                    deliveryItem.set("companyId",com);
                    deliveryItem.set("logisticsNumber",nu);
                    deliveryItem.set("dateTime", Timestamp.valueOf(data.getString("time")));
                    deliveryItem.set("description",data.get("context"));
                    tobeStore.add(deliveryItem);
                }
                if(UtilValidate.isNotEmpty(deliveryItems)){
                    delegator.removeAll(deliveryItems);
                }
                delegator.storeAll(tobeStore);
            }
            resp.setResult(true);
            resp.setReturnCode("200");
            response.getWriter().print(JacksonHelper.toJSON(resp)); //这里必须返回，否则认为失败，过30分钟又会重复推送。
        } catch (Exception e) {
            resp.setMessage("保存失败" + e.getMessage());
            response.getWriter().print(JacksonHelper.toJSON(resp));//保存失败，服务端等30分钟会重复推送。
        }
        return "success";
    }

}
