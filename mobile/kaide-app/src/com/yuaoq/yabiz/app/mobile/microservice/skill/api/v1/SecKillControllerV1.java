package com.yuaoq.yabiz.app.mobile.microservice.skill.api.v1;

import com.yuaoq.yabiz.app.mobile.microservice.index.api.v1.IndexControllerV1;
import com.yuaoq.yabiz.mobile.common.Paginate;
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
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Calendar;
import java.math.BigDecimal;

/**
 * Created by changchen on 2018/4/22.
 *
 */
@RestController
@RequestMapping(value = "/api/skill/v1")
public class SecKillControllerV1 {

    @Value("${image.base.url}")
    String baseImgUrl;
    public static final String module = SecKillControllerV1.class.getName();

    /**
     * 秒杀推荐
     * 今日疯抢today 明日预告tomorrow 后日预告nextDay
     *
     * @param request
     * @param dateType
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/todaySecKill", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> todaySecKill(HttpServletRequest request, String dateType, @RequestParam (defaultValue = "0") Integer page,@RequestParam (defaultValue = "20")  Integer pageSize) {
        Map<String, Object> resultData = FastMap.newInstance();
        int limit = pageSize;

        //LocalDispatcher对象
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        //总记录数
        int size = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;

        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));

        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = limit;
        } catch (Exception e) {
            viewSize = 20;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));

        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        //查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        //排序字段集合
        List<String> orderBy = FastList.newInstance();
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        dynamicView.addMemberEntity("PA", "ProductActivity");
        dynamicView.addAlias("PA", "activityId");
        dynamicView.addAlias("PA", "activityName");
        dynamicView.addAlias("PA", "activityStartDate");
        dynamicView.addAlias("PA", "activityEndDate");
        dynamicView.addAlias("PA", "activityAuditStatus");
        dynamicView.addAlias("PA", "activityType");
        dynamicView.addAlias("PA", "createdStamp");

        dynamicView.addMemberEntity("E", "Enumeration");
        dynamicView.addAlias("E", "enumId");
        dynamicView.addAlias("E", "activityTypeName", "description", null, false, null, null);
        dynamicView.addViewLink("PA", "E", false, ModelKeyMap.makeKeyMapList("activityType", "enumId"));

        dynamicView.addMemberEntity("PS","ProductStoreProductActAppl");
        dynamicView.addAlias("PS","fromDate");
        dynamicView.addAlias("PS","thruDate");
        dynamicView.addViewLink("PA", "PS", false, ModelKeyMap.makeKeyMapList("activityId", "activityId"));

        dynamicView.addMemberEntity("PG","ProductActivityGoods");
        dynamicView.addAlias("PG","productId");
        dynamicView.addAlias("PG", "activityPrice");
        dynamicView.addAlias("PG","activityQuantity");
        dynamicView.addViewLink("PA", "PG", false, ModelKeyMap.makeKeyMapList("activityId", "activityId"));

        dynamicView.addMemberEntity("P","Product");
        dynamicView.addAlias("P","isOnline");
        dynamicView.addViewLink("PG", "P", false, ModelKeyMap.makeKeyMapList("productId", "productId"));

        fieldsToSelect.add("activityId");
        fieldsToSelect.add("activityName");
        fieldsToSelect.add("activityStartDate");
        fieldsToSelect.add("activityEndDate");
        fieldsToSelect.add("activityAuditStatus");
        fieldsToSelect.add("activityType");
        fieldsToSelect.add("activityTypeName");
        fieldsToSelect.add("activityPrice");
        fieldsToSelect.add("activityQuantity");
        fieldsToSelect.add("productId");
        //促销活动开始时间
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("thruDate");
        fieldsToSelect.add("createdStamp");
        fieldsToSelect.add("isOnline");

        dynamicView.setGroupBy(fieldsToSelect);

        //排序字段名称
        String sortField = "createdStamp ASC";
        orderBy.add(sortField);

        //按促销类型查询
        andExprs.add(EntityCondition.makeCondition("activityType", EntityOperator.EQUALS, "SEC_KILL"));
        andExprs.add(EntityCondition.makeCondition("activityAuditStatus", EntityOperator.EQUALS, "ACTY_AUDIT_PASS"));
        andExprs.add(EntityCondition.makeCondition("isOnline", EntityOperator.EQUALS, "Y"));

        //默认条件
        List<EntityCondition> defaultExprs2 = FastList.newInstance();
        //进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）
        if (dateType.equals("today")){
            //活动开始时间在一天的开始时间和结束时间之间
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH,0);
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            calendar.set(Calendar.MILLISECOND,0);
            Date firstDate = new Date();
            firstDate = calendar.getTime();

            Date secondDate = new Date();
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,999);
            secondDate = calendar.getTime();

            Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

            List<EntityCondition> defaultExprs1 = FastList.newInstance();
            List<EntityCondition> defaultExprs3 = FastList.newInstance();

            defaultExprs1.add(EntityCondition.makeCondition("activityStartDate", EntityOperator.LESS_THAN, nowTimestamp));
            defaultExprs1.add(EntityCondition.makeCondition("activityEndDate", EntityOperator.GREATER_THAN_EQUAL_TO, nowTimestamp));

            defaultExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.toTimestamp(firstDate)));
            defaultExprs3.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.toTimestamp(secondDate)));

            defaultExprs2.add(EntityCondition.makeCondition(defaultExprs1, EntityOperator.AND));
            defaultExprs2.add(EntityCondition.makeCondition(defaultExprs3, EntityOperator.AND));

            andExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.OR));
        }else if (dateType.equals("tomorrow")){
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH,1);
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            calendar.set(Calendar.MILLISECOND,0);
            Date firstDate = new Date();
            firstDate = calendar.getTime();

            Date secondDate = new Date();
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,999);
            secondDate = calendar.getTime();
            defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.GREATER_THAN_EQUAL_TO,UtilDateTime.toTimestamp(firstDate)));
            defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN_EQUAL_TO,UtilDateTime.toTimestamp(secondDate)));

            andExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.AND));
        }else if (dateType.equals("nextDay")){
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_MONTH,2);
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            calendar.set(Calendar.MILLISECOND,0);
            Date firstDate = new Date();
            firstDate = calendar.getTime();

            Date secondDate = new Date();
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,999);
            secondDate = calendar.getTime();
            defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.toTimestamp(firstDate)));
            defaultExprs2.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("fromDate"), EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.toTimestamp(secondDate)));

            andExprs.add(EntityCondition.makeCondition(defaultExprs2, EntityOperator.AND));
        }
       //添加where条件
        if (andExprs.size() > 0) {
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
        }
        boolean beganTransaction = false;
        EntityListIterator eli = null;
        try {
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
           
            beganTransaction = TransactionUtil.begin();
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);

            eli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);

            List<GenericValue> gvs = eli.getPartialList(lowIndex,viewSize);
            List<Map> recordsList = FastList.newInstance();

            for (GenericValue gv : gvs) {
                Map map = FastMap.newInstance();
                String activityId = gv.getString("activityId");
                String activityName = gv.getString("activityName");
                Timestamp activityStartDate = gv.getTimestamp("activityStartDate");
                Timestamp activityEndDate = gv.getTimestamp("activityEndDate");
                String activityType = gv.getString("activityType");
                String productId = gv.getString("productId");
                String activityPrice = gv.getBigDecimal("activityPrice").setScale(2,BigDecimal.ROUND_HALF_UP).toString();;
                String activityQuantity = gv.getString("activityQuantity");

                map.put("productId",productId);
                map.put("activityId",activityId);
                map.put("activityName",activityName);
                map.put("activityStartDate",activityStartDate);
                map.put("activityEndDate",activityEndDate);
                map.put("activityType",activityType);
                map.put("activityQuantity",activityQuantity);

                if (UtilValidate.isNotEmpty(productId)) {
                    Map<String, Object> resultData1 = dispatcher.runSync("productsSummary", UtilMisc.toMap("productIds", productId.toString()));
                    List<GenericValue> products = (List<GenericValue>) resultData1.get("resultData");
                    if(UtilValidate.isNotEmpty(products)){
                        Map<String,Object> productMap = FastMap.newInstance();
                        List productList = FastList.newInstance();
                        for (int j = 0; j < products.size(); j++) {
                            Map<String,Object> product = products.get(j);
                            List<GenericValue> wxSendWaits = delegator.findByAnd("WxLiteTemplateSendWait",UtilMisc.toMap("objectValueId",activityId));
                            if (UtilValidate.isNotEmpty(wxSendWaits)){
                                GenericValue wxSendWait = wxSendWaits.get(0);
                                String wxPId = wxSendWait.getString("productId");
                                if (UtilValidate.isNotEmpty(wxPId)){
                                    if (wxPId.equals(productId)){
                                        productMap.put("isRecommend","Y");
                                    }else {
                                        productMap.put("isRecommend","N");
                                    }
                                }
                            }else {
                                productMap.put("isRecommend","N");
                            }
                            productMap.put("productId", product.get("productId"));
                            productMap.put("activityPrice", activityPrice);
                            productMap.put("activityId", activityId);
                            productMap.put("price", product.get("price"));
                            productMap.put("activityQuantity", activityQuantity);
                            productMap.put("productName", product.get("productName"));
                            productMap.put("mediumImageUrl", product.get("mediumImageUrl"));
                            productList.add(productMap);
                        }
                        map.put("products",productList);
                    }

                }
                recordsList.add(map);
            }

            // 获取总记录数
            size = eli.getResultsSizeAfterPartialList();

            boolean hasNext = true;
            boolean hasPrev = true;

            int next = viewIndex + 1;
            int pages = 1;
            //分页
            if (highIndex >= size) {
                highIndex = size;
                hasNext = false;
            }
            int prev = 0;
            pages = size % viewSize == 0 ? size / viewSize : size / viewSize + 1;
            if (lowIndex == 1) {
                hasPrev = false;
            }
            if (viewIndex == 0) {
                prev = 0;
            } else {
                prev = viewIndex - 1;
            }

            Map<String, Object> pMap = FastMap.newInstance();
            pMap.put("hasNext", hasNext);
            pMap.put("hasPrev", hasPrev);
            pMap.put("next", next);
            pMap.put("page", page);
            pMap.put("pages", pages);
            pMap.put("perPage", viewSize);
            pMap.put("prev", prev);
            pMap.put("total", size);
            resultData.put("paginate", pMap);
            resultData.put("recordsList", recordsList);
            resultData.put("retCode", 1);
            resultData.put("message", "查询成功");


        } catch (Exception e) {
            e.printStackTrace();
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            if (eli != null) {
                try {
                    eli.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
    
            // commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
        
            }
        }
        //返回的参数
        resultData.put("totalSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

}
