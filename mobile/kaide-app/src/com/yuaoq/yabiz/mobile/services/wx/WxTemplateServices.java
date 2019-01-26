package com.yuaoq.yabiz.mobile.services.wx;

import com.google.gson.Gson;
import com.yuaoq.yabiz.app.mobile.microservice.index.api.v1.DynamicIndexControllerV1;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.app.template.Templates;
import com.yuaoq.yabiz.weixin.common.util.JsonMapper;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by changsy on 2018/4/18.
 */
public class WxTemplateServices {

    public static final String module = WxTemplateServices.class.getName();
    
    /**
     * 实时发送小程序消息
     *
     * @return
     */
    public static Map<String, Object> sendTemplateMsg(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dct.getDelegator();
        String touser = (String) context.get("touser");
        String template_id = (String) context.get("template_id");
        String page = (String) context.get("page");
        String form_id = (String) context.get("form_id");
        String data = (String) context.get("data");
        String color = (String) context.get("color");
        String emphasis_keyword = (String) context.get("emphasis_keyword");
        String objectValueId = (String) context.get("objectValueId");
        String partyId = (String) context.get("partyId");
        String sendType = (String) context.get("sendType");
        String productId =  context.get("productId")==null?"":(String) context.get("productId");
        if (sendType.equals("0")) {
            Message message = new Message();
            message.setToUser(touser);
            Map<String, Object> dataMap = JsonMapper.defaultMapper().json2Map(data);
            if (UtilValidate.isNotEmpty(dataMap)) {
                Iterator keyIter = dataMap.keySet().iterator();
                Map<String, Message.Data> daMap = FastMap.newInstance();
                while (keyIter.hasNext()) {
                    String keyword = (String) keyIter.next();
                    Map oldMap = (Map) dataMap.get(keyword);
                    if (oldMap.get("value") != null) {
                        Message.Data data1 = new Message.Data();
                        data1.setColor((String) oldMap.get("color"));
                        Object value = oldMap.get("value");
                        if (value instanceof String) {
                            data1.setValue((String) value);
                        } else if (value instanceof Integer) {
                            data1.setValue(((Integer) value).toString());
                        } else {
                            data1.setValue(value.toString());
                        }
                        
                        daMap.put(keyword, data1);
                    }
                    
                }
                message.setData(daMap);
            }
            if (UtilValidate.isEmpty(form_id)) {
                GenericValue wxLiteForm = null;
                try {
                    wxLiteForm = EntityUtil.getFirst(delegator.findByAnd("WxLiteForm", UtilMisc.toMap("openId", touser), UtilMisc.toList("-createdStamp")));
                    if (UtilValidate.isNotEmpty(wxLiteForm)) {
                        form_id = wxLiteForm.getString("formId");
                        wxLiteForm.remove();
                    }
                } catch (GenericEntityException e) {
                    e.printStackTrace();
                }
            }
            message.setFormId(form_id);
            message.setTemplateId(template_id);
            message.setPage(page);
            if (UtilValidate.isNotEmpty(form_id)) {
                resultData = Templates.defaultTemplates().sendWithResult(message);
            }
 
        } else {
            GenericValue templateLog = delegator.makeValue("WxLiteTemplateSendWait");
            templateLog.setNextSeqId();
            templateLog.set("toUser", touser);
            templateLog.set("template_id", template_id);
            templateLog.set("page", page);
            templateLog.set("form_id", form_id);
            templateLog.set("data", data);
            templateLog.set("color", color);
            templateLog.set("emphasis_keyword", emphasis_keyword);
            templateLog.set("objectValueId", objectValueId);
            templateLog.set("partyId", partyId);
            templateLog.set("sendType", sendType);
            templateLog.set("partyId", partyId);
            templateLog.set("productId", productId);
            templateLog.set("status", "I");
            templateLog.set("sendTime",context.get("sendTime"));
            try {
                templateLog.create();
            } catch (Exception e) {
                e.printStackTrace();
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        
        //记录小程序模板发送记录
        return resultData;
        
    }
    
    /**
     * 实时发送小程序消息
     *
     * @return
     */
    public static Map<String, Object> sendTemplateMsg1(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dct.getDelegator();
        String touser = (String) context.get("touser");
        Map data = (Map) context.get("data");
        String color = (String) context.get("color");
        String emphasis_keyword = (String) context.get("emphasis_keyword");
        String objectValueId = (String) context.get("objectValueId");
        String partyId = (String) context.get("partyId");
        //先获取模板发送类型，
        String templateSendType = (String) context.get("templateSendType");

        GenericValue wxLiteForm = null;
        try {
            //通过模板发送类型获取具体的WxLiteTemplateConfig实体类。
            GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", templateSendType)));
            if (UtilValidate.isNotEmpty(config)) {
                String template_id = config.getString("wxLiteTemplateId");
                String page = config.getString("gotoPage");
                String comment = config.getString("remark");

                Map<String, Message.Data> dataMap = FastMap.newInstance();
                //判断是否有keyword1,2,3,4
                dataMap.put("keyword1", new Message.Data((String) data.get("keyword1"), ""));
                dataMap.put("keyword2", new Message.Data((String) data.get("keyword2"), ""));
                dataMap.put("keyword3", new Message.Data((String) data.get("keyword3"), ""));
                dataMap.put("keyword4", new Message.Data((String) data.get("keyword4"), ""));
                wxLiteForm = EntityUtil.getFirst(delegator.findByAnd("WxLiteForm", UtilMisc.toMap("openId", touser), UtilMisc.toList("-createdStamp")));

                String form_id = wxLiteForm.getString("formId");
                wxLiteForm.remove();
                Message message = new Message();
                message.setToUser(touser);
                message.setData(dataMap);
                message.setFormId(form_id);
                message.setTemplateId(template_id);
                message.setPage(page);
                Map<String, Object> result = Templates.defaultTemplates().sendWithResult(message);
                GenericValue templateLog = delegator.makeValue("WxLiteTemplateSendLog");
                templateLog.setNextSeqId();
                templateLog.set("toUser", touser);
                templateLog.set("template_id", template_id);
                templateLog.set("page", page);
                templateLog.set("form_id", form_id);
                templateLog.set("data", new Gson().toJson(dataMap));
                templateLog.set("color", color);
                templateLog.set("emphasis_keyword", emphasis_keyword);
                templateLog.set("objectValueId", objectValueId);
                templateLog.set("partyId", partyId);
                templateLog.set("sendType", "0");
                templateLog.set("status", "S");
                templateLog.set("returnMsg", JsonMapper.defaultMapper().toJson(result));
                templateLog.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }
        

        //记录小程序模板发送记录
        return resultData;
        
    }

    /**
     * 实时发送小程序消息
     *
     * @return
     */
    public static Map<String, Object> sendTemplateMsg2(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dct.getDelegator();
        String touser = (String) context.get("touser");
        Map data = (Map) context.get("data");
        String color = (String) context.get("color");
        String emphasis_keyword = (String) context.get("emphasis_keyword");
        String objectValueId = (String) context.get("objectValueId");
        String partyId = (String) context.get("partyId");
        String page = (String) context.get("page");
        //先获取模板发送类型，
        String templateSendType = (String) context.get("templateSendType");

        GenericValue wxLiteForm = null;
        try {
            //通过模板发送类型获取具体的WxLiteTemplateConfig实体类。
            GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", templateSendType)));
            if (UtilValidate.isNotEmpty(config)) {
                String template_id = config.getString("wxLiteTemplateId");

                Map<String, Message.Data> dataMap = FastMap.newInstance();
                //判断是否有keyword1,2,3,4
                dataMap.put("keyword1", new Message.Data((String) data.get("keyword1"), ""));
                dataMap.put("keyword2", new Message.Data((String) data.get("keyword2"), ""));
                dataMap.put("keyword3", new Message.Data((String) data.get("keyword3"), ""));
                dataMap.put("keyword4", new Message.Data((String) data.get("keyword4"), ""));
                wxLiteForm = EntityUtil.getFirst(delegator.findByAnd("WxLiteForm", UtilMisc.toMap("openId", touser), UtilMisc.toList("-createdStamp")));

                String form_id = wxLiteForm.getString("formId");
                wxLiteForm.remove();
                Message message = new Message();
                message.setToUser(touser);
                message.setData(dataMap);
                message.setFormId(form_id);
                message.setTemplateId(template_id);
                message.setPage(page);
                Map<String, Object> result = Templates.defaultTemplates().sendWithResult(message);
                GenericValue templateLog = delegator.makeValue("WxLiteTemplateSendLog");
                templateLog.setNextSeqId();
                templateLog.set("toUser", touser);
                templateLog.set("template_id", template_id);
                templateLog.set("page", page);
                templateLog.set("form_id", form_id);
                templateLog.set("data", new Gson().toJson(dataMap));
                templateLog.set("color", color);
                templateLog.set("emphasis_keyword", emphasis_keyword);
                templateLog.set("objectValueId", objectValueId);
                templateLog.set("partyId", partyId);
                templateLog.set("sendType", "0");
                templateLog.set("status", "S");
                templateLog.set("returnMsg", JsonMapper.defaultMapper().toJson(result));
                templateLog.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }


        //记录小程序模板发送记录
        return resultData;

    }
    /**
     * 提供模板消息的定时任务
     *
     * @param dcx
     * @param context
     * @return
     */
    public Map<String, Object> templateSendMsgTask(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dcx.getDelegator();
        LocalDispatcher dispatcher = dcx.getDispatcher();
        boolean beganTransaction = false;
        EntityListIterator pli=null;
        try {
            beganTransaction = TransactionUtil.begin();
            //查找一个小时内的未执行的活动
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("WTSW", "WxLiteTemplateSendWait");
            dynamicView.addAliasAll("WTSW","",null);

            Timestamp time = new Timestamp(System.currentTimeMillis()+(long)60*60*1000);

            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("status", EntityOperator.EQUALS, "I"),
                            EntityCondition.makeCondition("sendTime", EntityOperator.LESS_THAN, time)
                    )
                    , EntityOperator.AND);

            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, null);
            List<GenericValue> sendWaits = pli.getCompleteList();

            if (UtilValidate.isEmpty(sendWaits)) {
                return resultData;
            }
            for (int i = 0; i < sendWaits.size(); i++) {
                GenericValue sendTemplate = sendWaits.get(i);
                if(UtilValidate.isEmpty(sendTemplate.getString("template_id"))){
                    continue;
                }
                String templateId = sendTemplate.getString("template_id");
                GenericValue config =delegator.findByPrimaryKey("WxLiteTemplateConfig",UtilMisc.toMap("templateId",templateId));
                if(UtilValidate.isEmpty(config)){
                    continue;
                }
                String template_wx_id = config.getString("wxLiteTemplateId");
                if("SECKILL_NOTIFY".equalsIgnoreCase(config.getString("templateType"))){
                    String objectValueId = sendTemplate.getString("objectValueId");
                    //发送通知
                    String touser = sendTemplate.getString("toUser");
                    String page =   sendTemplate.getString("page");
                    String form_id ="";
                    String data = sendTemplate.getString("data");
                    String color = sendTemplate.getString("color");
                    String emphasis_keyword = sendTemplate.getString("emphasis_keyword");
                    String sendId = sendTemplate.getString("sendId");

                    if (UtilValidate.isNotEmpty(sendTemplate.get("data"))){
                        form_id = sendTemplate.getString("form_id");
                    }
                    try {
                        resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", touser, "template_id", template_wx_id, "page", page, "form_id", form_id, "data",data, "color", color, "emphasis_keyword",emphasis_keyword, "sendType", "0", "partyId", sendId, "objectValueId", objectValueId));
                    } catch (GenericServiceException e) {
                        resultData = ServiceUtil.returnError(e.getMessage());
                    }
//                    sendTemplate.remove();

//                    System.out.println(resultData);
                    Map<String, Object> updateFields = FastMap.newInstance();
                    updateFields.put("result", resultData.toString());
                    updateFields.put("status", "G");
                    EntityCondition UpdateCon = EntityCondition.makeCondition("sendId", EntityComparisonOperator.EQUALS, sendId);
                    delegator.storeByCondition("WxLiteTemplateSendWait", updateFields, UpdateCon);
                }
            }
        } catch (Exception e) {
            Debug.logError(e, module);
        } finally {
            try {
                pli.close();
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultData;
    }


    /**
     * 退款 审核成功 模板消息定时任务
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> sendTemplateMsgReturnSuccess(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dct.getDelegator();
        String touser = (String) context.get("touser");
        Map data = (Map) context.get("data");
        String color = (String) context.get("color");
        String emphasis_keyword = (String) context.get("emphasis_keyword");
        String objectValueId = (String) context.get("objectValueId");
        String partyId = (String) context.get("partyId");
        //先获取模板发送类型，
        String templateSendType = (String) context.get("templateSendType");

        GenericValue wxLiteForm = null;
        try {
            //通过模板发送类型获取具体的WxLiteTemplateConfig实体类。
            GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", templateSendType)));
            if (UtilValidate.isNotEmpty(config)) {
                String template_id = config.getString("wxLiteTemplateId");
                String page = config.getString("gotoPage");

                Map<String, Message.Data> dataMap = FastMap.newInstance();
                //判断是否有keyword1,2,3,4
                dataMap.put("keyword1", new Message.Data((String) data.get("keyword1"), ""));
                dataMap.put("keyword2", new Message.Data((String) data.get("keyword2"), ""));
                dataMap.put("keyword3", new Message.Data((String) data.get("keyword3"), ""));
                dataMap.put("keyword4", new Message.Data((String) data.get("keyword4"), ""));
                wxLiteForm = EntityUtil.getFirst(delegator.findByAnd("WxLiteForm", UtilMisc.toMap("openId", touser)));
                String form_id = wxLiteForm.getString("formId");
                
                wxLiteForm.remove();
                Message message = new Message();
                message.setToUser(touser);
                message.setData(dataMap);
                message.setFormId(form_id);
                message.setTemplateId(template_id);
                message.setPage(page);
                Map<String, Object> result = Templates.defaultTemplates().sendWithResult(message);
                GenericValue templateLog = delegator.makeValue("WxLiteTemplateSendLog");
                templateLog.setNextSeqId();
                templateLog.set("toUser", touser);
                templateLog.set("template_id", template_id);
                templateLog.set("page", page + objectValueId);
                templateLog.set("form_id", form_id);
                templateLog.set("data", new Gson().toJson(dataMap));
                templateLog.set("color", color);
                templateLog.set("emphasis_keyword", emphasis_keyword);
                templateLog.set("objectValueId", objectValueId);
                templateLog.set("partyId", partyId);
                templateLog.set("sendType", "0");
                templateLog.set("status", "S");
                templateLog.set("returnMsg", JsonMapper.defaultMapper().toJson(result));

                templateLog.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        //记录小程序模板发送记录
        return resultData;

    }

    /**
     * 退款审核 不通过 模板消息发送定时任务
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> sendTemplateMsgReturnFail(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dct.getDelegator();
        String touser = (String) context.get("touser");
        Map data = (Map) context.get("data");
        String color = (String) context.get("color");
        String emphasis_keyword = (String) context.get("emphasis_keyword");
        String objectValueId = (String) context.get("objectValueId");
        String partyId = (String) context.get("partyId");
        //先获取模板发送类型，
        String templateSendType = (String) context.get("templateSendType");

        GenericValue wxLiteForm = null;
        try {
            //通过模板发送类型获取具体的WxLiteTemplateConfig实体类。
            GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", templateSendType)));
            if (UtilValidate.isNotEmpty(config)) {
                String template_id = config.getString("wxLiteTemplateId");
                String page = config.getString("gotoPage");

                Map<String, Message.Data> dataMap = FastMap.newInstance();
                //判断是否有keyword1,2,3,4
                dataMap.put("keyword1", new Message.Data((String) data.get("keyword1"), ""));
                dataMap.put("keyword2", new Message.Data((String) data.get("keyword2"), ""));
                dataMap.put("keyword3", new Message.Data((String) data.get("keyword3"), ""));
                dataMap.put("keyword4", new Message.Data((String) data.get("keyword4"), ""));
                wxLiteForm = EntityUtil.getFirst(delegator.findByAnd("WxLiteForm", UtilMisc.toMap("openId", touser)));
                String form_id = wxLiteForm.getString("formId");
                
                wxLiteForm.remove();
                Message message = new Message();
                message.setToUser(touser);
                message.setData(dataMap);
                message.setFormId(form_id);
                message.setTemplateId(template_id);
                message.setPage(page);
                Map<String, Object> result = Templates.defaultTemplates().sendWithResult(message);
                GenericValue templateLog = delegator.makeValue("WxLiteTemplateSendLog");
                templateLog.setNextSeqId();
                templateLog.set("toUser", touser);
                templateLog.set("template_id", template_id);
                templateLog.set("page", page + objectValueId);
                templateLog.set("form_id", form_id);
                templateLog.set("data", new Gson().toJson(dataMap));
                templateLog.set("color", color);
                templateLog.set("emphasis_keyword", emphasis_keyword);
                templateLog.set("objectValueId", objectValueId);
                templateLog.set("partyId", partyId);
                templateLog.set("sendType", "0");
                templateLog.set("status", "S");
                templateLog.set("returnMsg", JsonMapper.defaultMapper().toJson(result));

                templateLog.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        //记录小程序模板发送记录
        return resultData;

    }

    /**
     * 退货 审核不通过 模板消息发送定时任务
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> sendTemplateMsgReturnProductFail(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dct.getDelegator();
        String touser = (String) context.get("touser");
        Map data = (Map) context.get("data");
        String color = (String) context.get("color");
        String emphasis_keyword = (String) context.get("emphasis_keyword");
        String objectValueId = (String) context.get("objectValueId");
        String partyId = (String) context.get("partyId");
        //先获取模板发送类型，
        String templateSendType = (String) context.get("templateSendType");

        GenericValue wxLiteForm = null;
        try {
            //通过模板发送类型获取具体的WxLiteTemplateConfig实体类。
            GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", templateSendType)));
            if (UtilValidate.isNotEmpty(config)) {
                String template_id = config.getString("wxLiteTemplateId");
                String page = config.getString("gotoPage");

                Map<String, Message.Data> dataMap = FastMap.newInstance();
                //判断是否有keyword1,2,3,4
                dataMap.put("keyword1", new Message.Data((String) data.get("keyword1"), ""));
                dataMap.put("keyword2", new Message.Data((String) data.get("keyword2"), ""));
                dataMap.put("keyword3", new Message.Data((String) data.get("keyword3"), ""));
                dataMap.put("keyword4", new Message.Data((String) data.get("keyword4"), ""));
                wxLiteForm = EntityUtil.getFirst(delegator.findByAnd("WxLiteForm", UtilMisc.toMap("openId", touser)));
                String form_id = wxLiteForm.getString("formId");
                
                wxLiteForm.remove();
                Message message = new Message();
                message.setToUser(touser);
                message.setData(dataMap);
                message.setFormId(form_id);
                message.setTemplateId(template_id);
                message.setPage(page);
                Map<String, Object> result = Templates.defaultTemplates().sendWithResult(message);
                GenericValue templateLog = delegator.makeValue("WxLiteTemplateSendLog");
                templateLog.setNextSeqId();
                templateLog.set("toUser", touser);
                templateLog.set("template_id", template_id);
                templateLog.set("page", page + objectValueId);
                templateLog.set("form_id", form_id);
                templateLog.set("data", new Gson().toJson(dataMap));
                templateLog.set("color", color);
                templateLog.set("emphasis_keyword", emphasis_keyword);
                templateLog.set("objectValueId", objectValueId);
                templateLog.set("partyId", partyId);
                templateLog.set("sendType", "0");
                templateLog.set("status", "S");
                templateLog.set("returnMsg", JsonMapper.defaultMapper().toJson(result));

                templateLog.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        //记录小程序模板发送记录
        return resultData;

    }

    /**
     * 退货 审核通过 模板消息发送定时任务
     *
     * @param dct
     * @param context
     * @return
     */
    public static Map<String, Object> sendTemplateMsgReturnProductSuccess(DispatchContext dct, Map<String, ? extends Object> context) {
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
        Delegator delegator = dct.getDelegator();
        String touser = (String) context.get("touser");
        Map data = (Map) context.get("data");
        String color = (String) context.get("color");
        String emphasis_keyword = (String) context.get("emphasis_keyword");
        String objectValueId = (String) context.get("objectValueId");
        String partyId = (String) context.get("partyId");
        //先获取模板发送类型，
        String templateSendType = (String) context.get("templateSendType");

        GenericValue wxLiteForm = null;
        try {
            //通过模板发送类型获取具体的WxLiteTemplateConfig实体类。
            GenericValue config = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", templateSendType)));
            if (UtilValidate.isNotEmpty(config)) {
                String template_id = config.getString("wxLiteTemplateId");
                String page = config.getString("gotoPage");

                Map<String, Message.Data> dataMap = FastMap.newInstance();
                //判断是否有keyword1,2,3,4
                dataMap.put("keyword1", new Message.Data((String) data.get("keyword1"), ""));
                dataMap.put("keyword2", new Message.Data((String) data.get("keyword2"), ""));
                dataMap.put("keyword3", new Message.Data((String) data.get("keyword3"), ""));
                dataMap.put("keyword4", new Message.Data((String) data.get("keyword4"), ""));
                wxLiteForm = EntityUtil.getFirst(delegator.findByAnd("WxLiteForm", UtilMisc.toMap("openId", touser)));
                String form_id = wxLiteForm.getString("formId");
                
                wxLiteForm.remove();
                Message message = new Message();
                message.setToUser(touser);
                message.setData(dataMap);
                message.setFormId(form_id);
                message.setTemplateId(template_id);
                message.setPage(page);
                Map<String, Object> result = Templates.defaultTemplates().sendWithResult(message);
                GenericValue templateLog = delegator.makeValue("WxLiteTemplateSendLog");
                templateLog.setNextSeqId();
                templateLog.set("toUser", touser);
                templateLog.set("template_id", template_id);
                templateLog.set("page", page + objectValueId);
                templateLog.set("form_id", form_id);
                templateLog.set("data", new Gson().toJson(dataMap));
                templateLog.set("color", color);
                templateLog.set("emphasis_keyword", emphasis_keyword);
                templateLog.set("objectValueId", objectValueId);
                templateLog.set("partyId", partyId);
                templateLog.set("sendType", "0");
                templateLog.set("status", "S");
                templateLog.set("returnMsg", JsonMapper.defaultMapper().toJson(result));

                templateLog.create();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceUtil.returnError(e.getMessage());
        }

        //记录小程序模板发送记录
        return resultData;

    }

}

