package com.yuaoq.yabiz.app.mobile.microservice.gift.api.v1;


import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.Paginate;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.common.util.JsonMapper;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.rmi.CORBA.Util;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("all")
@RestController
@RequestMapping(path = "/api/gift/v1")
public class  GiftV1Controller {
    @Value("${image.base.url}")
    String baseImgUrl;

    /**
     * 所有礼品商品分类
     *
     * @return
     */
    @RequestMapping(value = "/getAllCategorys", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getAllCategorys(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException {

        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) request.getAttribute("userLogin");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        List<Map> categoryList = FastList.newInstance();

        String sql = "WITH temp(PRODUCT_CATEGORY_ID,CATEGORY_NAME,PRIMARY_PARENT_CATEGORY_ID)\n" +
                "AS\n" +
                "(\n" +
                "\tselect PRODUCT_CATEGORY_ID ,CATEGORY_NAME ,PRIMARY_PARENT_CATEGORY_ID\n" +
                "\tfrom PRODUCT_CATEGORY  where PRODUCT_CATEGORY_ID in (select DISTINCT(primary_product_category_id) from PRODUCT where product_id in (select DISTINCT (PRODUCT_ID) productId from PROMO_GIFT ))\n" +
                "\t\n" +
                "\tUNION ALL\n" +
                "\n" +
                "\tselect a.PRODUCT_CATEGORY_ID ,a.CATEGORY_NAME ,a.PRIMARY_PARENT_CATEGORY_ID\n" +
                "\tfrom PRODUCT_category a \n" +
                "\tINNER JOIN temp b\n" +
                "\ton a.PRODUCT_CATEGORY_ID = b.PRIMARY_PARENT_CATEGORY_ID\n" +
                ")\n" +
                "select DISTINCT * from temp where PRIMARY_PARENT_CATEGORY_ID is null";
        String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
        //获得数据库的连接
        Connection conn = ConnectionFactory.getConnection(groupHelperName);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        Map questionMap = null;
        while (rs.next()) {
            Map<String, String> category = FastMap.newInstance();
            String categoryId = rs.getString("PRODUCT_CATEGORY_ID");
            String categoryName = rs.getString("CATEGORY_NAME");
            category.put("categoryId", categoryId);
            category.put("categoryName", categoryName);
            categoryList.add(category);
        }
        stmt.close();
        rs.close();
        conn.close();

        resultData.put("categoryList", categoryList);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    /**
     * 根据目录id获取商品信息。
     *
     * @param request
     * @param response
     * @param categoryId 目录id
     * @return
     * @throws GenericEntityException
     * @throws SQLException
     */
    @RequestMapping(value = "/getProductByCategoryId", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getProductByCategoryId(HttpServletRequest request, HttpServletResponse response, String categoryId, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize, JwtAuthenticationToken token) throws GeneralException, SQLException, IOException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getAttribute("userLogin");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }


        //数据库查询到的数据长度
        int size = 0;

        int lowIndex = 0;
        int highIndex = 0;
        //开始页
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        //每页多少条
        int viewSize = 10;
        try {
            viewSize = pageSize;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));

        //查询这个节点下的所有子节点

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PG", "PromoGift");
        dynamicView.addAlias("PG", "productId");
        dynamicView.addAlias("PG", "giftName");
        dynamicView.addAlias("PG", "status");
        dynamicView.addAlias("PG", "isTop");

        dynamicView.addMemberEntity("PR", "Product");
        dynamicView.addAlias("PR", "productId");
        dynamicView.addAlias("PR", "primaryProductCategoryId");
        dynamicView.addViewLink("PG", "PR", false, ModelKeyMap.makeKeyMapList("productId", "productId"));

        // 查询条件集合，用于数据库查询
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        // 排序字段集合
        List<String> orderBy = FastList.newInstance();
        // 显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();

        fieldsToSelect.add("productId");


        List<EntityCondition> filedExprs = FastList.newInstance();
        //有效的赠品
        filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("status"), EntityOperator.EQUALS, "1"));

        if (!UtilValidate.isEmpty(categoryId)) {
            List<String> categoryList = FastList.newInstance();
            String sql = "WITH temp(PRODUCT_CATEGORY_ID,CATEGORY_NAME,PRIMARY_PARENT_CATEGORY_ID)\n" +
                    "AS\n" +
                    "(\n" +
                    "\tselect PRODUCT_CATEGORY_ID ,CATEGORY_NAME ,PRIMARY_PARENT_CATEGORY_ID\n" +
                    "\tfrom PRODUCT_CATEGORY  where PRODUCT_CATEGORY_ID ='" + categoryId + "'\n" +
                    "\tUNION ALL\n" +
                    "\tselect a.PRODUCT_CATEGORY_ID productId,a.CATEGORY_NAME,a.PRIMARY_PARENT_CATEGORY_ID from PRODUCT_category a \n" +
                    "\tINNER JOIN temp b on a.PRIMARY_PARENT_CATEGORY_ID = b.PRODUCT_CATEGORY_ID\n" +
                    ")\n" +
                    "select * from temp";
            String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
            //获得数据库的连接
            Connection conn = ConnectionFactory.getConnection(groupHelperName);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            Map questionMap = null;
            while (rs.next()) {
                Map<String, String> category = FastMap.newInstance();
                String productCategoryId = rs.getString("PRODUCT_CATEGORY_ID");
                categoryList.add(productCategoryId);
            }
            stmt.close();
            rs.close();
            conn.close();

            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("primaryProductCategoryId"), EntityOperator.IN, categoryList));
        }

        mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);

        //置顶排序
        orderBy.add("-isTop");

        boolean beganTransaction = TransactionUtil.begin();

        lowIndex = viewIndex * viewSize + 1;
        highIndex = (viewIndex + 1) * viewSize;
        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);

        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

        List<GenericValue> productList = pli.getPartialList(lowIndex, viewSize);
        size = pli.getResultsSizeAfterPartialList();
        TransactionUtil.commit(beganTransaction);
        pli.close();

        List<String> productIdList = FastList.newInstance();
        for (GenericValue product : productList) {
            Map proMap = FastMap.newInstance();
            String productId = product.getString("productId");
            productIdList.add(productId);
        }
        List<Map> retProductList =ProductUtils.getProductsWithOutFeature(productIdList,delegator,dispatcher,baseImgUrl);

        resultData.put("productList", retProductList);
        resultData.put("totalProductListSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));

        resultData.put("paginate", new Paginate(viewIndex,viewSize,size));

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }


    /**
     * 获取所有礼品卡片
     *
     * @param request
     * @param response
     * @param token
     * @return
     * @throws GeneralException
     * @throws SQLException
     * @throws IOException
     */
    @RequestMapping(value = "/getAllCard", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getAllCard(HttpServletRequest request, HttpServletResponse response) throws GeneralException, SQLException, IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> cardList = delegator.findByAnd("PresentCard", UtilMisc.toMap("status", "Y"));
        TransactionUtil.commit(beganTransaction);
        List<Map> cardMapList = FastList.newInstance();
        for (GenericValue card : cardList) {
            Map cardMap = FastMap.newInstance();
            cardMap.put("cardName", card.getString("cardName"));
            cardMap.put("presentCardId", card.getString("presentCardId"));
            cardMap.put("cardDesc", card.getString("cardDesc"));
            String contentId = card.getString("contentId");
            String imgUrl = "";
            if (UtilValidate.isNotEmpty(contentId)) {
                imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
            }
            cardMap.put("imgUrl", imgUrl);
            cardMapList.add(cardMap);

        }
        resultData.put("cardList", cardMapList);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 创建礼品订单
     *
     * @param request
     * @param response
     * @return
     * @throws GeneralException
     * @throws SQLException
     * @throws IOException
     *//*
    @RequestMapping(value = "/createGiftOrder", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createGiftOrder(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) throws GeneralException, SQLException, IOException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String partyId = CommonUtils.getPartyId(delegator, loginName);


        return null;
    }*/

    /**
     * 设置图片音频视频内容
     * 将礼品订单从待赠送变为待领取状态
     * @param request
     * @param response
     * @return
     * @throws GeneralException
     * @throws SQLException
     * @throws IOException
     */
    @RequestMapping(value = "/updatePresent", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> updatePresent(HttpServletRequest request, HttpServletResponse response, String giftId, String leaveWord, String imgId, String voiceId, String videoId, JwtAuthenticationToken token) throws GeneralException, SQLException, IOException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        //
        String partyId = CommonUtils.getPartyId(delegator, loginName);
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> presentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("giftId", giftId));
        TransactionUtil.commit(beganTransaction);
        if (presentList == null || presentList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "找不到该赠送");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        GenericValue present = presentList.get(0);
        //校验当前登陆人是否发起人
        String sendPartyId = present.getString("sendPartyId");
        if (!partyId.equals(sendPartyId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "不是该赠送的发起人");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String status = present.getString("status");
        if (!"GIFT_WAIT_SEND".equals(status)) {
            resultData.put("retCode", 0);
            resultData.put("message", "该赠送非待赠送状态");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
//        present.put("status", "GIFT_WAIT_RECEIVE");
        present.put("leaveWord", leaveWord);
        present.put("imgId", imgId);
        present.put("voiceId", voiceId);
        present.put("videoId", videoId);
        present.store();

        resultData.put("retCode", 1);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    @RequestMapping(value = "/afterSendFriend", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> afterSendFriend(HttpServletRequest request, HttpServletResponse response, String giftId, JwtAuthenticationToken token) throws GeneralException, SQLException, IOException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(giftId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "giftId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        //
        String partyId = CommonUtils.getPartyId(delegator, loginName);
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> presentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("giftId", giftId));
        TransactionUtil.commit(beganTransaction);
        if (presentList == null || presentList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "找不到该赠送");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        GenericValue present = presentList.get(0);
        //校验当前登陆人是否发起人
        String sendPartyId = present.getString("sendPartyId");
        if (!partyId.equals(sendPartyId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "不是该赠送的发起人");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String status = present.getString("status");
        if (!"GIFT_WAIT_SEND".equals(status)) {
            resultData.put("retCode", 0);
            resultData.put("message", "该赠送非待赠送状态");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        present.put("status", "GIFT_WAIT_RECEIVE");
        present.store();

        resultData.put("retCode", 1);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

        /**
         * 查询我的赠送
         *
         * @param request
         * @param response status   GIFT_WAIT_SEND待赠送
         *                 GIFT_WAIT_RECEIVE待领取
         *                 GIFT_HAS_RECEIVE已领取
         *                 GIFT_HAS_CANCEL已取消
         * @return
         * @throws GeneralException
         * @throws SQLException
         * @throws IOException
         */
    @RequestMapping(value = "/findMyGift", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> findMyGift(HttpServletRequest request, HttpServletResponse response, String status, JwtAuthenticationToken token, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize) throws Exception {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        //数据库查询到的数据长度
        int size = 0;

        int lowIndex = 0;
        int highIndex = 0;
        //开始页
        int viewIndex = 0;
        try {
            viewIndex = page;
        } catch (Exception e) {
            viewIndex = 0;
        }
        resultData.put("viewIndex", Integer.valueOf(viewIndex));
        //每页多少条
        int viewSize = 10;
        try {
            viewSize = pageSize;
        } catch (Exception e) {
            viewSize = 10;
        }
        resultData.put("viewSize", Integer.valueOf(viewSize));


        String partyId = CommonUtils.getPartyId(delegator, loginName);

        //TODO
//        partyId = "10002";

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("PORP", "PartyOrderRelPresent");
        dynamicView.addAlias("PORP", "sendPartyId");
        dynamicView.addAlias("PORP", "giftId");
        dynamicView.addAlias("PORP", "sendDate");
        dynamicView.addAlias("PORP", "status");
        dynamicView.addAlias("PORP", "orderGroupId");

//        dynamicView.addMemberEntity("OH", "OrderHeader");
//        //实付金额
//        dynamicView.addAlias("OH", "orderId");
//
//        dynamicView.addViewLink("PORP", "OH", Boolean.FALSE, ModelKeyMap.makeKeyMapList("orderId", "orderId"));


        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("giftId");
        fieldsToSelect.add("orderGroupId");
        fieldsToSelect.add("sendDate");
        fieldsToSelect.add("status");


        List<EntityCondition> filedExprs = FastList.newInstance();
        filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("sendPartyId"), EntityOperator.EQUALS, partyId));

        if (!UtilValidate.isEmpty(status)) {
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("status"), EntityOperator.EQUALS, status));
        }else{
            //必须是四种状态之一
            List<String> statusList =FastList.newInstance();
            statusList.add("GIFT_WAIT_SEND");
            statusList.add("GIFT_WAIT_RECEIVE");
            statusList.add("GIFT_HAS_RECEIVE");
            statusList.add("GIFT_HAS_CANCEL");
            filedExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("status"), EntityOperator.IN, statusList));
        }

        EntityCondition mainCond = EntityCondition.makeCondition(filedExprs, EntityOperator.AND);

        //排序
        List<String> orderBy = FastList.newInstance();
        orderBy.add("-sendDate");

        boolean beganTransaction = TransactionUtil.begin();
        lowIndex = viewIndex * viewSize + 1;
        highIndex = (viewIndex + 1) * viewSize;

        EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);

        List<GenericValue> presentOrderdList = pli.getPartialList(lowIndex, viewSize);
        size = pli.getResultsSizeAfterPartialList();
        TransactionUtil.commit(beganTransaction);

        pli.close();

        List<Map> orderList = FastList.newInstance();
        if (presentOrderdList != null || presentOrderdList.size() > 0) {
            beganTransaction = TransactionUtil.begin();
            for (GenericValue presentOrder : presentOrderdList) {
                Map orderMap = FastMap.newInstance();
                List<Map> orderItemList = FastList.newInstance();
                String orderGroupId = presentOrder.getString("orderGroupId");
                //查询拆分的所有子订单
                List<GenericValue> orderGroupOrderRelList = delegator.findByAnd("OrderGroupOrderRel",UtilMisc.toMap("orderGroupId",orderGroupId));
                double actualPayMoney = 0;
                for(GenericValue orderGroupOrderRel:orderGroupOrderRelList){
                    String orderId = orderGroupOrderRel.getString("orderId");
                    //查询订单的明细并且查询每个明细产品的图片。
                    List<Map> orderItemListPart = getOrderItemList(orderId, delegator, dispatcher);
                    orderItemList.addAll(orderItemListPart);
                    //实际支付金额
                    List<GenericValue> orderPaymentList = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
                    if (UtilValidate.isNotEmpty(orderPaymentList)) {
                        actualPayMoney += orderPaymentList.get(0).getBigDecimal("maxAmount").doubleValue();
                    }
                }
                orderMap.put("actualPayMoney", actualPayMoney);
                orderMap.put("sendDate", CommonUtils.getStringDate(presentOrder.getTimestamp("sendDate")));
                orderMap.put("item", orderItemList);
                orderMap.put("itemCount", getOrderItemCount(orderItemList));
                orderMap.put("status", presentOrder.getString("status"));
                orderMap.put("giftId", presentOrder.getString("giftId"));
                orderList.add(orderMap);
            }
            TransactionUtil.commit(beganTransaction);
        }


        resultData.put("giftList", orderList);
        resultData.put("giftListSize", Integer.valueOf(size));
        resultData.put("highIndex", Integer.valueOf(highIndex));
        resultData.put("lowIndex", Integer.valueOf(lowIndex));

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
        //开始页
        pMap.put("page", page);
        pMap.put("pages", pages);
        //每页多少条
        pMap.put("perPage", viewSize);
        pMap.put("prev", prev);
        pMap.put("total", size);
        resultData.put("paginate", pMap);
        resultData.put("retCode", 1);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }


    /**
     * 查看礼品赠送详情信息
     *
     * @param request
     * @param response
     * @return
     * @throws GeneralException
     * @throws SQLException
     * @throws IOException
     */
    @RequestMapping(value = "/findGiftDetail", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> findGiftDetail(HttpServletRequest request, HttpServletResponse response, String giftId, JwtAuthenticationToken token) throws Exception {

        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        if (UtilValidate.isEmpty(giftId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "giftId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> presentOrderdList = delegator.findByAnd("PartyOrderRelPresent",UtilMisc.toMap("giftId",giftId));
        TransactionUtil.commit(beganTransaction);
        if (UtilValidate.isEmpty(presentOrderdList)) {
            resultData.put("retCode", 0);
            resultData.put("message", "查询不到该赠送信息！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        GenericValue presentOrder = presentOrderdList.get(0);
        String orderGroupId = presentOrder.getString("orderGroupId");
        String orderId ="";
        Map orderMap = FastMap.newInstance();
        List<Map> orderItemList = FastList.newInstance();
        //查询拆分的所有子订单
        beganTransaction = TransactionUtil.begin();
        List<GenericValue> orderGroupOrderRelList = delegator.findByAnd("OrderGroupOrderRel",UtilMisc.toMap("orderGroupId",orderGroupId));
        double actualPayMoney = 0;
        for(GenericValue orderGroupOrderRel:orderGroupOrderRelList){
            orderId = orderGroupOrderRel.getString("orderId");
            //查询订单的明细并且查询每个明细产品的图片。
            List<Map> orderItemListPart = getOrderItemList(orderId, delegator, dispatcher);
            orderItemList.addAll(orderItemListPart);
            //实际支付金额
            List<GenericValue> orderPaymentList = delegator.findByAnd("OrderPaymentPreference", UtilMisc.toMap("orderId", orderId));
            if (UtilValidate.isNotEmpty(orderPaymentList)) {
                actualPayMoney += orderPaymentList.get(0).getBigDecimal("maxAmount").doubleValue();
            }
        }
        TransactionUtil.commit(beganTransaction);

        orderMap.put("actualPayMoney", actualPayMoney);
        orderMap.put("sendDate", CommonUtils.getStringDate(presentOrder.getTimestamp("sendDate")));
        orderMap.put("item", orderItemList);
        orderMap.put("itemCount", getOrderItemCount(orderItemList));
        orderMap.put("status", presentOrder.getString("status"));
        orderMap.put("giftId", presentOrder.getString("giftId"));
        orderMap.put("presentCardId", presentOrder.getString("presentCardId"));

        String presentCardUrl = "";
        if(UtilValidate.isNotEmpty(presentOrder.getString("presentCardId"))){
            GenericValue presentCard = delegator.findByPrimaryKey("PresentCard",UtilMisc.toMap("presentCardId",presentOrder.getString("presentCardId")));
            String cardContentId = presentCard.getString("contentId");
            presentCardUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, cardContentId, false);
        }
        orderMap.put("presentCardUrl",presentCardUrl);

        String sendPartyId = presentOrder.getString("sendPartyId");
        Map userInfo = CommonUtils.getUserInfo(delegator,sendPartyId);
        orderMap.put("nickname",userInfo.get("nickname"));
        orderMap.put("headphoto",userInfo.get("headphoto"));

        if(!"GIFT_WAIT_SEND".equals(presentOrder.getString("status"))){
            orderMap.put("leaveWord", presentOrder.getString("leaveWord"));

            String imgId = presentOrder.getString("imgId");
            if (UtilValidate.isNotEmpty(imgId)) {
                String imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, imgId, false);
                orderMap.put("imgUrl", imgUrl);
            }

            String voiceId = presentOrder.getString("voiceId");
            if (UtilValidate.isNotEmpty(voiceId)) {
                String voiceUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, voiceId, false);
                orderMap.put("voiceUrl", voiceUrl);
            }
            String videoId = presentOrder.getString("videoId");
            if (UtilValidate.isNotEmpty(videoId)) {
                String videoUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, videoId, false);
                orderMap.put("videoUrl", videoUrl);
            }
        }
        if("GIFT_HAS_RECEIVE".equalsIgnoreCase(presentOrder.getString("status"))){
            //已完成状态下需要返回详细地址
            String contactMechId= EntityUtil.getFirst(delegator.findByAnd("OrderContactMech",UtilMisc.toMap("orderId",orderId,"contactMechPurposeTypeId","SHIPPING_LOCATION"))).getString("contactMechId");
            //查询出对应的省市区地址
            GenericValue postalAddress = delegator.findByPrimaryKey("PostalAddress",UtilMisc.toMap("contactMechId",contactMechId));
            if(UtilValidate.isNotEmpty(postalAddress)){
                GenericValue province = delegator.findByPrimaryKey("Geo",UtilMisc.toMap("geoId", postalAddress.get("stateProvinceGeoId")));
                GenericValue city = delegator.findByPrimaryKey("Geo",UtilMisc.toMap("geoId", postalAddress.get("city")));
                GenericValue country = delegator.findByPrimaryKey("Geo",UtilMisc.toMap("geoId", postalAddress.get("countyGeoId")));
                String provinceName = province.getString("geoName");
                String cityName = province.getString("geoName");
                String countryName = province.getString("geoName");
                String detailAddress=postalAddress.getString("address1");
                String name =postalAddress.getString("toName");
                String phone = postalAddress.getString("mobilePhone");
                Map addr = FastMap.newInstance();
                addr.put("provinceName",provinceName);
                addr.put("cityName",cityName);
                addr.put("countryName",countryName);
                addr.put("detailAddress",detailAddress);
                addr.put("name",name);
                addr.put("phone",phone);
                orderMap.put("receiveAddr",addr);
            }
        }


        resultData.put("giftDetail", orderMap);
        resultData.put("retCode", 1);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    /**
     * 领取礼品
     *
     * @param request
     * @param response
     * @param contactMechId  收件人地址id
     *
     * @return
     * @throws GeneralException
     * @throws SQLException
     * @throws IOException
     */
    @RequestMapping(value = "/accpetGift", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> accpetGift(HttpServletRequest request, HttpServletResponse response, String giftId, String contactMechId ,JwtAuthenticationToken token) throws GeneralException, SQLException, IOException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if(UtilValidate.isEmpty(contactMechId)){
            resultData.put("retCode", 0);
            resultData.put("message", "contactMechId收件人地址不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }
        //
        String receivepartyId = CommonUtils.getPartyId(delegator, loginName);
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> presentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("giftId", giftId));
        TransactionUtil.commit(beganTransaction);
        if (presentList == null || presentList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "找不到该赠送");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        GenericValue present = presentList.get(0);

        String sendPartyId = present.getString("sendPartyId");
        if(!"GIFT_WAIT_RECEIVE".equals(present.getString("status"))){
            resultData.put("retCode", 0);
            resultData.put("message", "该礼品已被领取！！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        //添加收件人信息。
        String orderGroupId = present.getString("orderGroupId");
        beganTransaction = TransactionUtil.begin();
        List<GenericValue> orderGroupOrderRelList = delegator.findByAnd("OrderGroupOrderRel",UtilMisc.toMap("orderGroupId",orderGroupId));
        for(GenericValue orderGroupOrderRel:orderGroupOrderRelList){
            String orderId = orderGroupOrderRel.getString("orderId");
//            GenericValue orderContactMech = delegator.makeValue("OrderContactMech");
//            orderContactMech.put("orderId",orderId);
//            orderContactMech.put("contactMechPurposeTypeId","SHIPPING_LOCATION");
//            orderContactMech.put("contactMechId",contactMechId);
//            orderContactMech.create();
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("contactMechId", contactMechId);
            EntityCondition UpdateCon = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId),
                            EntityCondition.makeCondition("contactMechPurposeTypeId", EntityOperator.EQUALS, "SHIPPING_LOCATION")
                    )
                    , EntityOperator.AND);
            delegator.storeByCondition("OrderContactMech", updateFields, UpdateCon);

        }
        TransactionUtil.commit(beganTransaction);
        //修改赠送状态
        present.put("receiveDate", UtilDateTime.nowTimestamp());
        present.put("receivePartyId", receivepartyId);
        present.put("status", "GIFT_HAS_RECEIVE");
        present.store();

        //TODO 领取完成后要通知领取人和赠送人
        String sendwxAppOpenId = CommonUtils.getWxAppOpenId(delegator, sendPartyId);
        String receivewxAppOpenId = CommonUtils.getWxAppOpenId(delegator, receivepartyId);

        String sendNickName =CommonUtils.getUserNick(sendPartyId,delegator);
        String receiveNickName =CommonUtils.getUserNick(receivepartyId,delegator);

        GenericValue fromconfig = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "GIFT_FROM_NOTIFY")));
        GenericValue toconfig = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "GIFT_TO_NOTIFY")));

//        if(UtilValidate.isNotEmpty(fromconfig)&&UtilValidate.isNotEmpty(sendwxAppOpenId)){
//            String template_id = fromconfig.getString("wxLiteTemplateId");
//            //发送给发起人
//            String remark="您的好友 "+achieveNickName+" 帮您实现了一条心愿，我们将尽快为您发货";
//            Map<String, Object> daMap = FastMap.newInstance();
//            daMap.put("keyword1", new Message.Data(remark,""));
//            daMap.put("keyword2",new Message.Data(wishDate,""));
//            String daJson = JsonMapper.defaultMapper().toJson(daMap);
//            resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", sendwxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=toberealized", "form_id", "", "data",daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", sendwxAppOpenId, "objectValueId", wishId));
//        }
//
//        if(UtilValidate.isNotEmpty(toconfig)&&UtilValidate.isNotEmpty(achievewxAppOpenId)){
//            String template_id = toconfig.getString("wxLiteTemplateId");
//            //发送给发起人
//            String remark="恭喜您成功实现了好友 "+sendNickName+" 的心愿单，我们将尽快为您的好友发货。";
//            Map<String, Object> daMap = FastMap.newInstance();
//            daMap.put("keyword1", new Message.Data(remark,""));
//            daMap.put("keyword2",new Message.Data(wishDate,""));
//            String daJson = JsonMapper.defaultMapper().toJson(daMap);
//            resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", achievewxAppOpenId, "template_id", template_id, "page", "/pages/webview/index?path=toberealizedf", "form_id", "", "data",daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", sendwxAppOpenId, "objectValueId", wishId));
//        }


        resultData.put("retCode",1);
        resultData.put("message", "领取成功");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    @RequestMapping(value = "/cancelGift", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> cancelGift(HttpServletRequest request, HttpServletResponse response, String giftId ,JwtAuthenticationToken token) throws GeneralException, SQLException, IOException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        if(UtilValidate.isEmpty(giftId)){
            resultData.put("retCode", 0);
            resultData.put("message", "giftId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }

        String partyId = CommonUtils.getPartyId(delegator, loginName);
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> presentList = delegator.findByAnd("PartyOrderRelPresent", UtilMisc.toMap("giftId", giftId));
        TransactionUtil.commit(beganTransaction);
        if (presentList == null || presentList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "找不到该赠送");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        GenericValue present = presentList.get(0);

        if(!partyId.equals(present.getString("sendPartyId"))){
            resultData.put("retCode", 0);
            resultData.put("message", "非本人不可取消！！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        if(!"GIFT_WAIT_SEND".equals(present.getString("status"))){
            resultData.put("retCode", 0);
            resultData.put("message", "非待赠送状态不可取消！！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }




        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }
    /**
     * 得到商品明细总数量
     * @param orderItemList
     * @return
     */
    private int getOrderItemCount(List<Map> orderItemList) {
        if(orderItemList==null || orderItemList.size()==0){
            return 0;
        }
        int sum=0;
        for(Map item:orderItemList){
            sum+=(int)item.get("quantity");
        }

        return sum;
    }

    /**
     * 得到商品明细
     * @param orderId
     * @param delegator
     * @param dispatcher
     * @return
     * @throws Exception
     */
    private List<String> getOrderItemProductList(String orderId, Delegator delegator) throws Exception {
        List<String> list = FastList.newInstance();
        List<GenericValue> itemList  = delegator.findByAnd("OrderItem", UtilMisc.toMap("orderId",orderId));
        for(GenericValue item:itemList){
            String productId =item.getString("productId");
            list.add(productId);
        }
        return list;

    }
    private List<Map> getOrderItemList(String orderId, Delegator delegator, LocalDispatcher dispatcher) throws Exception {
        List<Map> list = FastList.newInstance();

        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("OI", "OrderItem");
        dynamicView.addAlias("OI", "orderId");
        dynamicView.addAlias("OI", "productId");
        dynamicView.addAlias("OI", "quantity");
        dynamicView.addAlias("OI", "selectedAmount");
        dynamicView.addAlias("OI", "unitPrice");

        dynamicView.addMemberEntity("P", "Product");
        dynamicView.addAlias("P", "productId");
        dynamicView.addAlias("P", "productName");
        dynamicView.addViewLink("OI", "P", Boolean.FALSE, ModelKeyMap.makeKeyMapList("productId", "productId"));

        EntityCondition mainCond = EntityCondition.makeCondition(
                UtilMisc.toList(
                        EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId)
                )
                , EntityOperator.AND);

        boolean beganTransaction = TransactionUtil.begin();

        EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, null);
        List<GenericValue> itemList = pli.getCompleteList();

        TransactionUtil.commit(beganTransaction);
        pli.close();

        if (UtilValidate.isNotEmpty(itemList)) {
            for (GenericValue item : itemList) {
                Map itemMap = FastMap.newInstance();
                String productId = item.getString("productId");
                itemMap.put("productName", item.getString("productName"));
                itemMap.put("productId", productId);
                //商品数量
                itemMap.put("quantity", item.getBigDecimal("quantity").intValue());
                itemMap.put("unitPrice", item.getBigDecimal("unitPrice").doubleValue());
                String imgUrl = "";
                //图片,选择小图片
                List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", "ADDITIONAL_IMAGE_1"));
                if (UtilValidate.isNotEmpty(productContents)) {
                    String contentId = productContents.get(0).getString("contentId");
                    if (UtilValidate.isNotEmpty(contentId)) {
                        imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);
                    }
                }
                itemMap.put("imgUrl", imgUrl);
                //商品特征
                itemMap.put("feature", ProductUtils.getProductFeature(productId,delegator));
                list.add(itemMap);
            }
        }

        return list;
    }



}
