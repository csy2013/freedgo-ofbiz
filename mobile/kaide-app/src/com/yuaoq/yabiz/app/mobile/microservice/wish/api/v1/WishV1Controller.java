package com.yuaoq.yabiz.app.mobile.microservice.wish.api.v1;

import com.yuaoq.yabiz.app.mobile.microservice.article.api.v1.ArticleV1Controller;
import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.Paginate;
import com.yuaoq.yabiz.mobile.common.ProductUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("all")
@RestController
@RequestMapping(path = "/api/wish/v1")
public class WishV1Controller {
    public static final String module = WishV1Controller.class.getName();

    @Value("${image.base.url}")
    String baseImgUrl;
    /**
     * 生成一个心愿单
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws SQLException
     */
    @RequestMapping(value = "/addWish", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addWish(HttpServletRequest request, String productIds, String contactMechId, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException {
        Map<String, Object> resultData = FastMap.newInstance();

        if (UtilValidate.isEmpty(productIds)) {
            resultData.put("retCode", 0);
            resultData.put("message", "心愿单产品不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }
      /*  if (UtilValidate.isEmpty(contactMechId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "contactMechId收件人地址不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }*/
        String webSiteId = request.getHeader("client");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        String partyId = CommonUtils.getPartyId(delegator, loginName);
        boolean beganTransaction = TransactionUtil.begin();
        //创建心愿单主表
        GenericValue orderWish = delegator.makeValue("PartyOrderWish");
        String wishId = delegator.getNextSeqId("PartyOrderWish");
        orderWish.put("wishId", wishId);
        orderWish.put("status", "WISH_WAIT_ACHIEVE");
        orderWish.put("sendPartyId", partyId);
        orderWish.put("contactMechId", contactMechId);
        orderWish.put("sendDate", new Timestamp(System.currentTimeMillis()));
        orderWish.create();

        //创建心愿单明细
        String productArr[] = productIds.split(",");
        List<GenericValue> wishDetailList = FastList.newInstance();
        for (String productId : productArr) {
            GenericValue wishDetail = delegator.makeValue("PartyOrderWishDetail");
            wishDetail.setNextSeqId();
            wishDetail.put("wishId", wishId);
            wishDetail.put("productId", productId.trim());
            wishDetailList.add(wishDetail);
        }

        delegator.storeAll(wishDetailList);
        TransactionUtil.commit(beganTransaction);
        resultData.put("retCode", 1);
        resultData.put("wishId", wishId);
        resultData.put("message", "success");
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    /**
     * 查看心愿详情
     *
     * @param request
     * @param wishId
     * @param response
     * @param token
     * @return
     * @throws GenericEntityException
     * @throws SQLException
     * @throws GenericServiceException
     */
    @RequestMapping(value = "/viewWish", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> viewWish(HttpServletRequest request, String wishId, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException, SQLException, GenericServiceException {

        Map<String, Object> resultData = FastMap.newInstance();

        if (UtilValidate.isEmpty(wishId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "wishId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        }
        String webSiteId = request.getHeader("client");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

        String partyId = CommonUtils.getPartyId(delegator, loginName);
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> wishList = delegator.findByAnd("PartyOrderWish", UtilMisc.toMap("wishId", wishId));
        TransactionUtil.commit(beganTransaction);
        if (wishList == null || wishList.size() == 0) {
            resultData.put("retCode", 0);
            resultData.put("message", "查找不到该心愿单");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        GenericValue wish = wishList.get(0);
        String sendPartyId = wish.getString("sendPartyId");
        if (!partyId.equals(sendPartyId)) {
            //发起人不是当前查看人
            List<GenericValue> wishViewList = delegator.findByAnd("PartyOrderWishView", UtilMisc.toMap("wishId", wishId, "viewPartyId", partyId));
            if (wishViewList == null || wishViewList.size() == 0) {
                //当前人第一次访问，添加访问记录
                GenericValue wishView = delegator.makeValue("PartyOrderWishView");
                wishView.setNextSeqId();
                wishView.put("wishId", wishId);
                wishView.put("viewPartyId", partyId);
                wishView.create();
            }
        }

        Map wishMap = getWishMap(wish, delegator, dispatcher);
        String status = (String) wishMap.get("status");
        if ("WISH_HAS_ACHIEVE".equalsIgnoreCase(status)) {
            String achievePartyId = (String) wishMap.get("achievePartyId");
            String isBuyOwn = "N";
            if (partyId.equals(achievePartyId)) {
                isBuyOwn = "Y";
            }
            wishMap.put("isBuyOwn", isBuyOwn);
        }

        resultData.put("wish", wishMap);
        resultData.put("retCode", 1);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }


    /**
     * 好友心愿列表
     *
     * @param request
     * @param token
     * @return
     * @throws GenericEntityException
     * @throws SQLException
     * @throws GenericServiceException
     */
    @RequestMapping(value = "/friendsWish", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> friendsWish(HttpServletRequest request, String status, HttpServletResponse response, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize, JwtAuthenticationToken token) throws GenericEntityException, SQLException, GenericServiceException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(status)) {
            resultData.put("retCode", 0);
            resultData.put("message", "status不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String partyId = CommonUtils.getPartyId(delegator, loginName);

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

        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("POW", "PartyOrderWish");
        dve.addAlias("POW", "wishId");
        dve.addAlias("POW", "status");
        dve.addAlias("POW", "sendPartyId");
        dve.addAlias("POW", "achievePartyId");
        dve.addAlias("POW", "sendDate");
        dve.addAlias("POW", "achieveDate");
        dve.addAlias("POW", "contactMechId");

        dve.addMemberEntity("POWV", "PartyOrderWishView");
        dve.addAlias("POWV", "wishId");
        dve.addAlias("POWV", "viewPartyId");
        dve.addAlias("POWV", "createdStamp");

        dve.addViewLink("POW", "POWV", Boolean.FALSE, ModelKeyMap.makeKeyMapList("wishId", "wishId"));

        List<EntityCondition> andExprs = FastList.newInstance();
        andExprs.add(EntityCondition.makeCondition("viewPartyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, status));

        List<String> orderBy = FastList.newInstance();
        orderBy.add("-createdStamp");

        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("wishId");
        fieldsToSelect.add("status");
        fieldsToSelect.add("sendPartyId");
        fieldsToSelect.add("achievePartyId");
        fieldsToSelect.add("achieveDate");
        fieldsToSelect.add("sendDate");
        fieldsToSelect.add("contactMechId");

        lowIndex = viewIndex * viewSize + 1;
        highIndex = (viewIndex + 1) * viewSize;
        boolean beganTransaction = TransactionUtil.begin();

        EntityListIterator pli = null;
        List<GenericValue> wishList = null;
        try {
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            pli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, orderBy, findOpts);

            wishList = pli.getPartialList(lowIndex, viewSize);
            size = pli.getResultsSizeAfterPartialList();
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
            if (pli != null) {
                try {
                    pli.close();
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

        List<Map> wishMapList = FastList.newInstance();
        for (GenericValue wish : wishList) {
            Map wishMap = getWishMap(wish, delegator, dispatcher);
            wishMapList.add(wishMap);
        }
        resultData.put("wishList", wishMapList);
        resultData.put("wishListSize", Integer.valueOf(size));
        resultData.put("paginate", new Paginate(viewIndex, viewSize, size));
        resultData.put("retCode", 1);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    /**
     * 我的已经实现列表
     *
     * @param request
     * @param status
     * @param response
     * @param page
     * @param pageSize
     * @param token
     * @return
     * @throws GenericEntityException
     * @throws SQLException
     * @throws GenericServiceException
     */
    @RequestMapping(value = "/findMyAchievedWish", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> findMyAchievedWish(HttpServletRequest request, String status, HttpServletResponse response, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer pageSize, JwtAuthenticationToken token) throws GenericEntityException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        String partyId = CommonUtils.getPartyId(delegator, loginName);

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

        DynamicViewEntity dve = new DynamicViewEntity();
        dve.addMemberEntity("POW", "PartyOrderWish");
        dve.addAlias("POW", "wishId");
        dve.addAlias("POW", "status");
        dve.addAlias("POW", "sendPartyId");
        dve.addAlias("POW", "achievePartyId");
        dve.addAlias("POW", "sendDate");
        dve.addAlias("POW", "achieveDate");


        List<EntityCondition> andExprs = FastList.newInstance();
        andExprs.add(EntityCondition.makeCondition("sendPartyId", EntityOperator.EQUALS, partyId));
        andExprs.add(EntityCondition.makeCondition("status", EntityOperator.EQUALS, "WISH_HAS_ACHIEVE"));

        EntityCondition mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);

        List<String> orderBy = FastList.newInstance();
        orderBy.add("-sendDate");

        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("wishId");
        fieldsToSelect.add("status");
        fieldsToSelect.add("sendPartyId");
        fieldsToSelect.add("achievePartyId");
        fieldsToSelect.add("achieveDate");
        fieldsToSelect.add("sendDate");

        lowIndex = viewIndex * viewSize + 1;
        highIndex = (viewIndex + 1) * viewSize;
        boolean beganTransaction = false;
        EntityListIterator pli = null;
        List<GenericValue> wishList = null;
        try {
            beganTransaction = TransactionUtil.begin();

            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, false);
            pli = delegator.findListIteratorByCondition(dve, mainCond, null, fieldsToSelect, orderBy, findOpts);

            wishList = pli.getPartialList(lowIndex, viewSize);
            size = pli.getResultsSizeAfterPartialList();
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
            if (pli != null) {
                try {
                    pli.close();
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

        List<Map> wishMapList = FastList.newInstance();
        for (GenericValue wish : wishList) {
            Map wishMap = getWishMap(wish, delegator, dispatcher);
            wishMapList.add(wishMap);
        }

        for (Map wishMap : wishMapList) {
            String achievePartyId = (String) wishMap.get("achievePartyId");
            String isBuyOwn = "N";
            if (partyId.equals(achievePartyId)) {
                isBuyOwn = "Y";
            }
            wishMap.put("isBuyOwn", isBuyOwn);
        }
        resultData.put("wishList", wishMapList);
        resultData.put("wishListSize", Integer.valueOf(size));
        resultData.put("paginate", new Paginate(viewIndex, viewSize, size));
        resultData.put("retCode", 1);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }

    /**
     * 获取心愿单商品等信息
     *
     * @param wish
     * @param delegator
     * @param dispatcher
     * @return
     * @throws GenericEntityException
     * @throws GenericServiceException
     */
    private Map getWishMap(GenericValue wish, Delegator delegator, LocalDispatcher dispatcher) throws GenericEntityException {
        Map wishMap = FastMap.newInstance();
        String sendPartyId = wish.getString("sendPartyId");
        String achievePartyId = wish.getString("achievePartyId");
        String wishId = wish.getString("wishId");
        wishMap.put("wishId", wishId);
        wishMap.put("contactMechId", wish.getString("contactMechId"));
        //心愿发起时间
        wishMap.put("sendDate", CommonUtils.getStringDate(wish.getTimestamp("sendDate")));
        //发起心愿人的partyId
        wishMap.put("sendPartyId", sendPartyId);
        wishMap.put("achievePartyId", achievePartyId);
        //发起心愿单昵称
        wishMap.put("sendPartyName", CommonUtils.getUserNick(sendPartyId, delegator));
        wishMap.put("achievePartyName", CommonUtils.getUserNick(achievePartyId, delegator));
        wishMap.put("status", wish.getString("status"));
        wishMap.put("achieveDate", CommonUtils.getStringDate(wish.get("achieveDate") == null ? null : wish.getTimestamp("achieveDate")));

        //查找该心愿单商品明细
        List<String> productIdList = FastList.newInstance();
        Map<String, Object> productsSummaryRes = null;

        List<GenericValue> wishItemList = delegator.findByAnd("PartyOrderWishDetail", UtilMisc.toMap("wishId", wishId));
        for (GenericValue wishItem : wishItemList) {
            productIdList.add(wishItem.getString("productId"));
        }

        List<Map> productList = ProductUtils.getProductsWithOutFeature(productIdList,delegator,dispatcher,baseImgUrl);
        double sumPrice = 0;
        if (UtilValidate.isNotEmpty(productList)) {
            Map<String, Object> productMap = FastMap.newInstance();
            for (int i = 0; i < productList.size(); i++) {
                Map<String, Object> product = productList.get(i);
                double price = (double) product.get("price");
                sumPrice +=price;
            }
        }
        wishMap.put("products", productList);
        wishMap.put("sumPrice", sumPrice);
        return wishMap;

    }


}
