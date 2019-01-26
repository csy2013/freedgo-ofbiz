package com.yuaoq.yabiz.app.mobile.microservice.redpackage.api.v1;

import com.yuaoq.yabiz.app.security.auth.JwtAuthenticationToken;
import com.yuaoq.yabiz.app.security.model.UserContext;
import com.yuaoq.yabiz.mobile.common.CommonUtils;
import com.yuaoq.yabiz.mobile.common.CouponUtils;
import com.yuaoq.yabiz.weixin.app.template.Message;
import com.yuaoq.yabiz.weixin.common.util.JsonMapper;
import javolution.util.FastList;
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
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.ConnectionFactory;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("all")
@RestController
@RequestMapping(path = "/api/redpackage/v1")
public class RedPackageV1Controller {
    public static final String module = RedPackageV1Controller.class.getName();

    /**
     * 首页是否弹出红包。
     *
     * @param request
     * @param response
     * @param token
     * @return
     * @throws GenericEntityException
     */
    @RequestMapping(value = "/isShowIndex", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> isShowIndex(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        boolean beganTransaction = false;
        //查看红包设置是否开启拆红包功能
        try {
            beganTransaction = TransactionUtil.begin();
            List<GenericValue> packageSettingList = delegator.findByAnd("RedPackageSetting", UtilMisc.toMap("status", "1", "isUsed", "Y"));
            if (packageSettingList == null || packageSettingList.size() == 0) {
                resultData.put("retCode", 1);
                resultData.put("isShow", "N");
                resultData.put("message", "拆红包功能未启用");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }

            //校验用户是否24小时内已经弹出了
            //根据登录名获取partyId
            String partyId = CommonUtils.getPartyId(delegator, loginName);
            List<GenericValue> indexHistoryList = delegator.findByAnd("RedPackageIndexHistory", UtilMisc.toMap("partyId", partyId));
            if (indexHistoryList == null || indexHistoryList.size() == 0) {
                //将当前人的记录插入到数据库
                delegator.makeValue("RedPackageIndexHistory", UtilMisc.toMap("rpihId", delegator.getNextSeqId("RedPackageIndexHistory"), "partyId", partyId, "lastVisitTime", new Timestamp(System.currentTimeMillis()))).create();
                resultData.put("retCode", 1);
                resultData.put("isShow", "Y");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

            }
            //检查上次弹出是否24小时之内
            GenericValue indexHistory = indexHistoryList.get(0);
            Timestamp lastVisitTime = indexHistory.getTimestamp("lastVisitTime");
            Timestamp now = new Timestamp(System.currentTimeMillis());
            long between = now.getTime() - lastVisitTime.getTime();
            if (between > (long) (24 * 3600000)) {
                //说明超过一天了,更新这次弹出时间。
                Map<String, Object> updateFields = FastMap.newInstance();
                updateFields.put("lastVisitTime", new Timestamp(System.currentTimeMillis()));
                EntityCondition UpdateCon = EntityCondition.makeCondition("partyId", EntityComparisonOperator.EQUALS, partyId);
                delegator.storeByCondition("RedPackageIndexHistory", updateFields, UpdateCon);

                resultData.put("retCode", 1);
                resultData.put("isShow", "Y");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

            } else {
                resultData.put("retCode", 1);
                resultData.put("isShow", "N");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

            }
        } catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());
            try {
                TransactionUtil.rollback(beganTransaction, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
        } finally {
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {

            }
        }
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

    /**
     * 发起拆红包的动作。
     *
     * @param request
     * @param response
     * @param token
     * @return status 0 代表用户已经拆过红包，同时返回红包userPackageId  1代表没有拆红包，正常拆红包返回   2代表拆红包失败
     * message 失败原因
     * @throws GenericEntityException
     */
    @RequestMapping(value = "/dismantlingRedPackage", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> dismantlingRedPackage(HttpServletRequest request, HttpServletResponse response, JwtAuthenticationToken token) throws GenericEntityException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String partyId = CommonUtils.getPartyId(delegator, loginName);
        //正常拆红包
        //查找红包设置表

        List<GenericValue> packageSettingList = delegator.findByAnd("RedPackageSetting", UtilMisc.toMap("status", "1", "isUsed", "Y"));

        if (packageSettingList == null || packageSettingList.size() == 0) {
            resultData.put("retCode", 1);
            resultData.put("status", "2");
            resultData.put("message", "拆红包功能未启用");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        GenericValue packageSetting = packageSettingList.get(0);
        long hours = packageSetting.getLong("hourRange");
        Timestamp between = new Timestamp(System.currentTimeMillis() - hours * 3600000L);
        //用户24小时内有没有发起的没有拆完的红包
        String sql = "select * from USER_RED_PACKAGE where CREATE_DATE >'" + CommonUtils.getStringDate(between) + "' and GET_NUMS<COUPON_NUMS and CREATE_PERSON_ID='" + partyId + "'";
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            if (rs.next()) {
                String userPackageId = rs.getString("USER_PACKAGE_ID");
                resultData.put("retCode", 1);
                resultData.put("status", "0");
                resultData.put("userPackageId", userPackageId);
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }

        } catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());

            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        } finally {
            sqlP.close();
        }

        String packageId = packageSetting.getString("packageId");

//        beganTransaction = TransactionUtil.begin();
//        List<GenericValue> redPackageCouponSettingList = delegator.findByAnd("RedPackageCouponSetting",UtilMisc.toMap("packageId",packageId));
//        TransactionUtil.commit(beganTransaction);
        List<String> couponIds = FastList.newInstance();
        sql = "select COUPON_CODE from PRODUCT_PROMO_COUPON where COUPON_CODE in(select COUPON_ID from RED_PACKAGE_COUPON_SETTING where PACKAGE_ID='" + packageId + "') and COUPON_QUANTITY -USER_COUNT>0";
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);

            ResultSet rs = sqlP.getResultSet();
            while (rs.next()) {
                String couponId = rs.getString("COUPON_CODE");
                couponIds.add(couponId);
            }

        } catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
            resultData.put("retCode", 0);
            resultData.put("message", e.getMessage());

            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

        } finally {
            sqlP.close();
        }


        if (couponIds == null || couponIds.size() == 0) {
            resultData.put("retCode", 1);
            resultData.put("status", "3");
            resultData.put("message", "无可用代金券发放！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        //拆红包主表
        long nums = packageSetting.getLong("nums");
        String userPackageId = delegator.getNextSeqId("UserRedPackage");
        GenericValue userRedPackage = delegator.makeValue("UserRedPackage", UtilMisc.toMap("packageId", packageId, "userPackageId", userPackageId, "createPersonId", partyId));
        userRedPackage.put("couponNums", nums);
        userRedPackage.put("getNums", 1L);
        userRedPackage.put("createDate", new Timestamp(System.currentTimeMillis()));
        userRedPackage.create();

        //创建多个明细,并且设置一条随机的代金券
        List<GenericValue> redPackageDetailList = FastList.newInstance();
        for (long i = 0; i < nums; i++) {
            GenericValue userRedPackageDetail = delegator.makeValue("UserRedPackageDetail");
            userRedPackageDetail.put("userPackageId", userPackageId);
            userRedPackageDetail.put("userPackageDetailId", delegator.getNextSeqId("UserRedPackageDetail"));
            userRedPackageDetail.put("status", "I");
            //随机获取一条代金券信息。
            userRedPackageDetail.put("couponId", getRandomCouponId(couponIds, delegator));
            if (i == 0) {
                userRedPackageDetail.put("partyId", partyId);//设置发起人partyId
            }
            redPackageDetailList.add(userRedPackageDetail);
        }
        delegator.storeAll(redPackageDetailList);

        resultData.put("retCode", 1);
        resultData.put("status", "1");
        resultData.put("userPackageId", userPackageId);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }


    /**
     * 红包领取列表页面
     *
     * @param request
     * @param response
     * @param token
     * @return
     * @throws GenericEntityException
     */
    @RequestMapping(value = "/redPackageReveList", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> redPackageReveList(HttpServletRequest request, HttpServletResponse response, @RequestParam String userPackageId, JwtAuthenticationToken token) throws GenericEntityException {
        String loginName = ((UserContext) token.getPrincipal()).getUsername();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, Object> resultData = FastMap.newInstance();

        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        String partyId = CommonUtils.getPartyId(delegator, loginName);
        //红包id
        if (userPackageId == null || "".equals(userPackageId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "userPackageId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        EntityListIterator pli = null;
        boolean beganTransaction = false;
        //查询红包以及明细信息。
        try {
            beganTransaction = TransactionUtil.begin();
            GenericValue userRedPackage = delegator.findByPrimaryKey("UserRedPackage", UtilMisc.toMap("userPackageId", userPackageId));

            if (userRedPackage == null) {
                resultData.put("retCode", 0);
                resultData.put("message", "查询不到该红包信息");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

            }

            List<GenericValue> packageSettingList = delegator.findByAnd("RedPackageSetting", UtilMisc.toMap("packageId", userRedPackage.getString("packageId")));
            if(UtilValidate.isEmpty(packageSettingList)){
                resultData.put("retCode", 0);
                resultData.put("message", "查询不到该红包setting信息");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }
            //判断该红包是否过期
            Timestamp createDate = userRedPackage.getTimestamp("createDate");
            GenericValue packageSetting = packageSettingList.get(0);
            //红包有效期
            long hours = packageSetting.getLong("hourRange");
            Timestamp endDate = new Timestamp(createDate.getTime()+hours*60*60*1000);
            resultData.put("endDate", CommonUtils.getStringDate(endDate));
            if(endDate.getTime()<System.currentTimeMillis()){
                resultData.put("isExpire", "Y");
            }else{
                resultData.put("isExpire", "N");
            }

            //计算红包总金额
            List<GenericValue> details = delegator.findByAnd("UserRedPackageDetail",UtilMisc.toMap("userPackageId",userPackageId));
            Double couponTotalAmount =0.0;
            for(GenericValue detail:details){
                String couponId = detail.getString("couponId");
                String reduce = (String) CouponUtils.getCouponByCouponId(couponId, delegator).get("payReduce");
                couponTotalAmount+=Double.parseDouble(reduce);
            }
            resultData.put("couponTotalAmount", couponTotalAmount);
            String createPersonId = userRedPackage.getString("createPersonId");
            if (partyId.equals(createPersonId)) {
                //当前打开人是发起人
                resultData.put("isOwner", "Y");
            } else {
                resultData.put("isOwner", "N");
            }
            resultData.put("couponNums", userRedPackage.getLong("couponNums"));
            resultData.put("getNums", userRedPackage.getLong("getNums"));
            if (userRedPackage.getLong("couponNums") == userRedPackage.getLong("getNums")) {
                resultData.put("isEnd", "Y");
            } else {
                resultData.put("isEnd", "N");
            }
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            resultData.put("createDate", time.format(userRedPackage.getTimestamp("createDate")));

            DynamicViewEntity dynamicView = new DynamicViewEntity();

            List<String> fieldsToSelect = FastList.newInstance();
            fieldsToSelect.add("partyId");
            fieldsToSelect.add("headphoto");
            fieldsToSelect.add("nickname");
            fieldsToSelect.add("couponId");
            fieldsToSelect.add("userPackageId");
            fieldsToSelect.add("status");

            dynamicView.addMemberEntity("URPD", "UserRedPackageDetail");
            dynamicView.addAlias("URPD", "partyId");
            dynamicView.addAlias("URPD", "userPackageId");
            dynamicView.addAlias("URPD", "couponId");
            dynamicView.addAlias("URPD", "status");

            dynamicView.addMemberEntity("P", "Person");
            dynamicView.addAlias("P", "headphoto");
            dynamicView.addAlias("P", "nickname");

            dynamicView.addViewLink("URPD", "P", false, ModelKeyMap.makeKeyMapList("partyId"));


            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("partyId", EntityOperator.NOT_EQUAL, null),
                            EntityCondition.makeCondition("userPackageId", EntityOperator.EQUALS, userPackageId)
                    )
                    , EntityOperator.AND);


            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, null, null);
            List<GenericValue> userRedPackageDetailList = pli.getCompleteList();


            List<Map> detailMapList = FastList.newInstance();
            for (GenericValue userRedPackageDetail : userRedPackageDetailList) {
                String userPartyId = userRedPackageDetail.getString("partyId");
                String couponId = userRedPackageDetail.getString("couponId");
                Map detail = userRedPackageDetail.toMap();
                if (partyId.equals(userPartyId)) {
                    detail.put("ownThisCoupon", "Y");
                } else {
                    detail.put("ownThisCoupon", "N");
                }
                detail.put("couponInfo", CouponUtils.getCouponByCouponId(couponId, delegator));
                detailMapList.add(detail);
            }

            resultData.put("detail", detailMapList);
        } catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
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
        resultData.put("retCode", 1);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }


    /**
     * 拆红包
     *
     * @param request
     * @param response
     * @param token
     * @return status 0 当前登陆人已经领取过， 1正常领取成功 ，2红包已经被全部领取完，3领取失败
     * isLast Y 当前人是最后一个领取红包的  N不是最后一个
     * message 失败原因
     * @throws GenericEntityException
     */
    @RequestMapping(value = "/openRedPackage", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> openRedPackage(HttpServletRequest request, HttpServletResponse response,String userPackageId,String page, JwtAuthenticationToken token) throws GenericEntityException {
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
        String partyId = CommonUtils.getPartyId(delegator, loginName);

        //红包id
        if (userPackageId == null || "".equals(userPackageId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "userPackageId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (page == null || "".equals(page)) {
            resultData.put("retCode", 0);
            resultData.put("message", "page不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        //查询红包信息。
        boolean beganTransaction = TransactionUtil.begin();
        EntityListIterator pli = null;
        try {
            GenericValue userRedPackage = delegator.findByPrimaryKey("UserRedPackage", UtilMisc.toMap("userPackageId", userPackageId));
            if (userRedPackage == null) {
                resultData.put("retCode", 0);
                resultData.put("message", "查询不到该红包信息");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }
            List<GenericValue> packageSettingList = delegator.findByAnd("RedPackageSetting", UtilMisc.toMap("packageId", userRedPackage.getString("packageId")));
            if(UtilValidate.isEmpty(packageSettingList)){
                resultData.put("retCode", 0);
                resultData.put("message", "查询不到该红包setting信息");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }
            //判断当前人是否已经拆过
            List<GenericValue> redPackageDetails = delegator.findByAnd("UserRedPackageDetail", UtilMisc.toMap("userPackageId", userPackageId, "partyId", partyId));
            if (redPackageDetails != null && redPackageDetails.size() > 0) {
                resultData.put("retCode", 1);
                resultData.put("status", "0");
                resultData.put("message", "当前登陆人已经领取过红包！");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

            }
            //判断红包是否已经全部领取完
            long getNums = userRedPackage.getLong("getNums");
            long couponNums = userRedPackage.getLong("couponNums");
            String ownerPartyId = userRedPackage.getString("createPersonId");
            if (getNums == couponNums) {
                resultData.put("retCode", 1);
                resultData.put("status", "2");
                resultData.put("message", "当前红包已经被领取完！");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }


            //判断该红包是否过期
            Timestamp createDate = userRedPackage.getTimestamp("createDate");
            GenericValue packageSetting = packageSettingList.get(0);
            //红包有效期
            long hours = packageSetting.getLong("hourRange");
            Timestamp endDate = new Timestamp(createDate.getTime()+hours*60*60*1000);
            resultData.put("endDate", CommonUtils.getStringDate(endDate));
            if(endDate.getTime()<System.currentTimeMillis()){
                resultData.put("retCode", 1);
                resultData.put("status", "4");
                resultData.put("message", "红包已过期，无法领取");
                return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
            }


            //正常领取
            DynamicViewEntity dynamicView = new DynamicViewEntity();
            dynamicView.addMemberEntity("URPD", "UserRedPackageDetail");
            dynamicView.addAlias("URPD", "partyId");
            dynamicView.addAlias("URPD", "userPackageId");
            dynamicView.addAlias("URPD", "userPackageDetailId");

            EntityCondition mainCond = EntityCondition.makeCondition(
                    UtilMisc.toList(
                            EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, null),
                            EntityCondition.makeCondition("userPackageId", EntityOperator.EQUALS, userPackageId)
                    )
                    , EntityOperator.AND);

            pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, null, null, null);
            List<GenericValue> userRedPackageDetailList = pli.getCompleteList();

            String userPackageDetailId = userRedPackageDetailList.get(0).getString("userPackageDetailId");
            Map<String, Object> updateFields = FastMap.newInstance();
            updateFields.put("partyId", partyId);
            EntityCondition UpdateCon = EntityCondition.makeCondition("userPackageDetailId", EntityComparisonOperator.EQUALS, userPackageDetailId);
            delegator.storeByCondition("UserRedPackageDetail", updateFields, UpdateCon);


            userRedPackage.put("getNums", getNums + 1);
            userRedPackage.store();

            String isLast = "N";

            if (getNums + 1 == couponNums) {

                //说明此时的是最后一个红包，需要生成
                isLast = "Y";
                //正常随机发放代金券。
                List<Map> acceptDetails = FastList.newInstance();
                List<GenericValue> redPackageDetailsAll = delegator.findByAnd("UserRedPackageDetail", UtilMisc.toMap("userPackageId", userPackageId));
                for (GenericValue detail : redPackageDetailsAll) {
                    Map acceptDetail = FastMap.newInstance();
                    String detailPartyId = detail.getString("partyId");
                    if (ownerPartyId.equalsIgnoreCase(detailPartyId)) {
                        acceptDetail.put("isOwner", "Y");
                    } else {
                        acceptDetail.put("isOwner", "N");
                    }
                    String wxAppOpenId = CommonUtils.getWxAppOpenId(delegator, detailPartyId);
                    acceptDetail.put("wxAppOpenId", wxAppOpenId);
                    String couponCodeId = CouponUtils.getUsefulCouponByCouponId(detail.getString("couponId"), delegator, dispatcher, userLogin);
                    if (couponCodeId == null) {
                        acceptDetail.put("status", "F");
                        acceptDetail.put("message", "无可用代金券！");
                        acceptDetails.add(acceptDetail);
                        detail.put("status", "F");
//                        System.out.println("获取不到代金券了！");
                        detail.store();
                        //通知领取人领取失败
                        continue;
                    }
                    acceptDetail.put("status", "S");

                    //目前最后一个人不发通知
                    if (!partyId.equalsIgnoreCase(detailPartyId)) {
                        acceptDetails.add(acceptDetail);
                    }

                    //修改明细状态
                    detail.put("status", "S");
                    detail.put("couponCodeId", couponCodeId);
                    detail.store();

                    //修改代金券的status为G已经领取
                    updateFields = FastMap.newInstance();
                    updateFields.put("promoCodeStatus", "G");
                    UpdateCon = EntityCondition.makeCondition("productPromoCodeId", EntityComparisonOperator.EQUALS, couponCodeId);
                    delegator.storeByCondition("ProductPromoCode", updateFields, UpdateCon);

                    //添加用户和代金券的关联
                    GenericValue productPromoCodeParty = delegator.makeValue("ProductPromoCodeParty");
                    productPromoCodeParty.put("productPromoCodeId", couponCodeId);
                    productPromoCodeParty.put("sourceTypeId", "SOURCE_TYPE_REDPACKAGE");
                    productPromoCodeParty.put("partyId", detailPartyId);
                    productPromoCodeParty.put("getDate", new Timestamp(System.currentTimeMillis()));
                    productPromoCodeParty.create();

                }
                resultData.put("acceptDetails", acceptDetails);
                //发送小程序模板信息

                GenericValue successconfig = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "COUPON_GROUP_SUCCESS_NOTIFY")));
//                GenericValue errorconfig = EntityUtil.getFirst(delegator.findByAnd("WxLiteTemplateConfig", UtilMisc.toMap("templateType", "COUPON_GROUP_FAIL_NOTIFY")));
                if (UtilValidate.isNotEmpty(successconfig)) {
                    String template_id = successconfig.getString("wxLiteTemplateId");
                    for (Map detail : acceptDetails) {
                        if (UtilValidate.isNotEmpty(detail.get("wxAppOpenId"))) {
                            String wxAppOpenId = (String) detail.get("wxAppOpenId");
                            Map<String, Object> daMap = FastMap.newInstance();
//                            String remark=successconfig.getString("remark");
                            String remark=CommonUtils.getUserNick(userRedPackage.getString("createPersonId"),delegator)+"发起的组团拆红包，组团成功。";
                            daMap.put("keyword1", new Message.Data(remark,""));
                            daMap.put("keyword2",new Message.Data(couponNums+"",""));
                            daMap.put("keyword3",new Message.Data(CommonUtils.getStringDate(userRedPackage.getTimestamp("createDate")),""));
                            String daJson = JsonMapper.defaultMapper().toJson(daMap);
                            resultData = dispatcher.runSync("xgro-sendTemplateMsg", UtilMisc.toMap("touser", wxAppOpenId, "template_id", template_id, "page", page, "form_id", "", "data",daJson, "color", "", "emphasis_keyword", "", "sendType", "0", "partyId", partyId, "objectValueId", userRedPackage.getString("userPackageId")));
                        }
                    }
                }
            }

            resultData.put("retCode", 1);
            resultData.put("status", "1");
            resultData.put("isLast", isLast);
        } catch (Exception e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
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

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
    }


    /**
     * 随机获取一条代金券的id
     *
     * @param productCouponIdList
     * @param delegator
     * @return
     */
    public synchronized String getRandomCouponId(List<String> productCouponIdList, Delegator delegator) {
        int coupponSize = productCouponIdList.size();
        return productCouponIdList.get(new Random().nextInt(coupponSize));
    }


}
