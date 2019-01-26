package com.qihua.ofbiz.pay;

import com.google.zxing.WriterException;
import com.qihua.ofbiz.member.MemberEvents;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.content.content.ContentEvents;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.LocalDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Created by Alex on 2017/3/17.
 */
public class GetQrCode {
    /**
     * 获取付款二维码
     *
     * @param request
     * @param response
     * @return
     * @throws WriterException
     * @throws GenericEntityException
     * @throws IOException
     */
    public static String getQrCode(HttpServletRequest request, HttpServletResponse response) throws WriterException, GenericEntityException, IOException {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Map<String, String> tokenMap = MemberEvents.checkAuthToken(request, response);
        if ("error".equals(tokenMap.get("status"))) {
            request.setAttribute("error", tokenMap.get("error"));
            return "error";
        }
        String userLoginId = tokenMap.get("userLoginId");
        GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", userLogin.get("partyId")));

        //生成唯一ID
        String uuid = getFixLenthString(18);
        //二维码内容
        String content = uuid;
        //生成二维码
        String contentId = ContentEvents.getQRCode(dispatcher, delegator, userLogin.getString("partyId"), content);
        //生成条形码
        String contentId1 = ContentEvents.generateBarCode128(dispatcher, delegator, userLogin.getString("partyId"), content, "0.5", "30");

        //生成的图片访问地址
        String qrCodeImg = request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + contentId;
        String barCodeImg = request.getAttribute("_SERVER_ROOT_URL_") + "/content/control/getImage?contentId=" + contentId1;
//        String jsonStr = "{\"uuid\":" + uuid + ",\"qrCodeImg\":\"" + qrCodeImg + "\"}";
        party.set("uuid", uuid);
        delegator.store(party);
        request.setAttribute("uuid", uuid);
        request.setAttribute("qrCodeImg", qrCodeImg);
        request.setAttribute("barCodeImg", barCodeImg);
        return "success";
    }

    private static String getFixLenthString(int strLength) {

        Random rm = new Random();

        // 获得随机数
        double pross = (1 + rm.nextDouble()) * Math.pow(10, strLength);

        // 将获得的获得随机数转化为字符串
        String fixLenthString = String.valueOf(pross);

        // 返回固定的长度的随机数
        return fixLenthString.substring(1, strLength + 1);
    }

}
