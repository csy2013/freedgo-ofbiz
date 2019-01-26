package org.ofbiz.common.crm;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilProperties;

import java.util.Map;

/**
 * Created by Administrator on 2017/6/26.
 */
public class CrmSynchronizeServices {
    public final static String module = CrmSynchronizeServices.class.getName();

    // 服务器调用地址
    public static final String SOURCE = "/middle/invoker/ico/crm";
    // 优惠券发放
    public final static String PUSH_COUPON_ACCOUNT = "/couponInfo/pushCouponAccount";
    // 同步客户钱包余额
    public final static String SYNC_QB = "";

    /**
     * 获取请求地址的基本URL
     *
     * @return
     */
    public final static String getBaseUrl() {
        return UtilProperties.getPropertyValue("security.properties", "crm.url");
    }





}
