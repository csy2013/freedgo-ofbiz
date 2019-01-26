package org.ofbiz.order.kuaidi100;


import org.ofbiz.base.util.HttpClient;
import org.ofbiz.base.util.HttpClientException;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.order.kuaidi100.pojo.TaskRequest;
import org.ofbiz.order.kuaidi100.pojo.TaskResponse;

import java.util.HashMap;
import java.util.Locale;


public class PostOrder {
    
    public static final String key = "gRbJSxRW317"; //快递100提供的key
    public static final String callbackurl = "http://121.40.203.83:8083/ordermgr/control/callback"; //回调URL，生产机
//    public static final String callbackurl = "http://221.6.35.90:9008/ordermgr/control/callback"; //回调URL，测试机
    
    /**
     * 向快递100发送订阅请求  Add By AlexYao 2016-3-1 17:53:35
     *
     * @param company         快递公司编号
     * @param number          快递单号
     * @param from            出发地城市（可为空）
     * @param to              目的地城市（可为空）
     * @param mobiletelephone 收件人的手机号
     * @param seller          寄件商家的名称
     * @param commodity       寄给收件人的商品名
     * @return
     */
    public static String postOrder(String company, String number, String from, String to, String mobiletelephone, String seller, String commodity) {
        TaskRequest req = new TaskRequest();
        req.setCompany(company);
        if (UtilValidate.isNotEmpty(from)) {
            req.setFrom(from);
        }
        if (UtilValidate.isNotEmpty(to)) {
            req.setTo(to);
        }
        req.setNumber(number);
        req.getParameters().put("callbackurl", callbackurl);
        req.getParameters().put("mobiletelephone", mobiletelephone);
        req.getParameters().put("seller", seller);
        req.getParameters().put("commodity", commodity);
        req.setKey(key);
        
        HashMap<String, String> p = new HashMap<String, String>();
        p.put("schema", "json");
        p.put("param", JacksonHelper.toJSON(req));
        try {
            String ret = HttpRequest.postData("http://www.kuaidi100.com/poll", p, "UTF-8");
            TaskResponse resp = JacksonHelper.fromJSON(ret, TaskResponse.class);
            if (resp.getResult() == true) {
            
            } else {
            
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }
    
    /**
     * 向快递100发送订阅请求  Add By AlexYao 2016-3-1 17:53:35
     *
     * @param company 快递公司编号
     * @param number  快递单号
     * @return
     */
    public static String query(String company, String number) {
        String result = "";
        try {
            String key = UtilProperties.getMessage("kd100", "kd100.api.key", Locale.CHINA);
            HttpClient client = new HttpClient();
            client.setUrl("http://api.kuaidi100.com/api");
            client.setParameter("id", key);
            client.setParameter("com", company);
            client.setParameter("nu", number);
            client.setParameter("show", "0");
            client.setParameter("muti", "1");
            client.setParameter("order", "desc");
            result = client.get();
        } catch (HttpClientException e) {
            e.printStackTrace();
        }
        return result;
    }
}
