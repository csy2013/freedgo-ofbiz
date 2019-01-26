package com.yuaoq.yabiz.app.mobile.microservice.starscore.api.v1;


import javolution.util.FastMap;
import net.sf.json.JSONObject;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 星积分
 */

@SuppressWarnings("all")
@RestController
@RequestMapping(path = "/api/starscore/v1")
public class StarScoreV1Controller {

    @RequestMapping(value = "/queryScore", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> queryScore(HttpServletRequest request, HttpServletResponse response, String token, String mallId, String memberId) throws GenericEntityException, SQLException, GenericServiceException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");

        if (UtilValidate.isEmpty(token)) {
            resultData.put("retCode", 0);
            resultData.put("message", "token不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(mallId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "mallId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(memberId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "memberId不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");

//        String mallId ="10";
//        String token ="436AFAA568A1475EA60D21130CA8A2B9";
//        String memberId ="FD25DF5C10EF66D6";

        Map map = FastMap.newInstance();
        map.put("mall_id", mallId);
        map.put("token", token);
        map.put("member_id", memberId);
        Map res = dispatcher.runSync("kaide-userGetScore", map);
        if (res == null || res.get("result") == null) {
            resultData.put("retCode", 0);
            resultData.put("message", "请求失败！");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        String resString = (String) res.get("result");
        JSONObject jsonRes = JSONObject.fromObject(resString);

        String result = jsonRes.getString("result");
        if ("1".equals(result)) {
            resultData.put("retCode", 1);
            Integer score = jsonRes.getInt("data");
            resultData.put("score", score);
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        String errormsg = jsonRes.getString("msg");
        resultData.put("retCode", 0);
        resultData.put("message", errormsg);
        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));


//        GenericValue kdMallInfo =  delegator.findByPrimaryKey("KdMallInfo", UtilMisc.toMap("mallId",mallId));
//        if(kdMallInfo==null){
//            resultData.put("retCode", 0);
//            resultData.put("message", "找不到该商场信息！");
//            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
//        }
//        String locationId = kdMallInfo.getString("locationId");


    }


    @RequestMapping(value = "/getScoreRule", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> getScoreRule(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, SQLException, GenericServiceException {
        Map<String, Object> resultData = FastMap.newInstance();
        String webSiteId = request.getHeader("client");
        if (UtilValidate.isEmpty(webSiteId)) {
            resultData.put("retCode", 0);
            resultData.put("message", "站点编号不能为空");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        boolean beganTransaction = TransactionUtil.begin();
        List<GenericValue> partyIntegralSets = delegator.findByAnd("PartyIntegralSet",FastMap.newInstance());
        TransactionUtil.commit(beganTransaction);
        if(partyIntegralSets==null){
            resultData.put("retCode", 0);
            resultData.put("message", "当前没有设置");
            return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));
        }

        long integralValue = partyIntegralSets.get(0).getLong("integralValue");

        resultData.put("retCode", 1);
        resultData.put("integralValue", integralValue);

        return Optional.ofNullable(resultData).map(returnResult -> new ResponseEntity(returnResult, HttpStatus.OK)).orElse(new ResponseEntity<List<Map<String, Object>>>(HttpStatus.NOT_IMPLEMENTED));

    }

}
