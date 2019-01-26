package com.yuaoq.yabiz.mobile.common;

import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.entity.jdbc.ConnectionFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 获取代金券的公共方法
 */
@SuppressWarnings("all")
public class CouponUtils {
    /**
     * 从代金券列表中，选择一条，返回，并且修改该代金券的可用数量
     * @param productCouponList
     * @return productPromoCodeId 代金券id
     *  payFill 满多少
        payReduce  减多少
     */
    public synchronized static Map getUsefulCoupon(List<GenericValue> productCouponList, Delegator delegator, LocalDispatcher dispatcher, GenericValue userLogin ) throws GenericEntityException {
        int coupponSize = productCouponList.size();
        int arr[] =new int[coupponSize];
        for(int i=0;i<coupponSize;i++){
            arr[i]=i;
        }
        arr = disOrderArr(arr);
        for(int i=0;i<arr.length;i++){
            GenericValue coupon = productCouponList.get(arr[i]);
            long couponQuantity=coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("couponQuantity");
            long userCount =coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("userCount");
            String couponCode = coupon.getString("couponCode");
            if((couponQuantity-userCount)>0){
                //说明代金券数量不为0
                //创建一条代金券
                Map serviceIn = FastMap.newInstance();
                serviceIn.put("couponCode", couponCode);
                serviceIn.put("userLogin", userLogin);
                SQLProcessor sqlP = null;
                try {
                    Map<String, Object> retMap =dispatcher.runSync("createOneProductPromoCode", serviceIn);
                    Map result = FastMap.newInstance();
                    String productPromoCodeId = (String) retMap.get("productPromoCodeId");
                    //查询该优惠券的其他信息
                    String groupHelperName = delegator.getGroupHelperName("org.ofbiz");
                    String sql ="select * from PRODUCT_PROMO_COUPON where COUPON_CODE =(select ppc.COUPON_CODE from PRODUCT_PROMO_CODE ppc where ppc.product_promo_code_id='"+productPromoCodeId+"')";
                    GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
                    sqlP = new SQLProcessor(helperInfo);
                    sqlP.executeQuery(sql);

                    ResultSet rs = sqlP.getResultSet();
                    Map questionMap=null;
                    if (rs.next()) {
                        String couponType = rs.getString("COUPON_TYPE");
                        long payFill = rs.getLong("PAY_FILL");
                        long payReduce =rs.getLong("PAY_REDUCE");
                        result.put("payFill",payFill);
                        result.put("payReduce",payReduce);
                    }else{
                        //查询不到优惠券信息

                        return null;
                    }
                    result.put("productPromoCodeId",productPromoCodeId);
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    sqlP.close();
                }
            }
        }
        return null;
    }

    /**
     * 打乱数组顺序
     * @param arr
     * @return
     */
    public static int[] disOrderArr(int[] arr) {
        int[] arr2 = new int[arr.length];
        int count = arr.length;
        int cbRandCount = 0;// 索引
        int cbPosition = 0;// 位置
        int k = 0;
        do {
            Random rand = new Random();
            int r = count - cbRandCount;
            cbPosition = rand.nextInt(r);
            arr2[k++] = arr[cbPosition];
            cbRandCount++;
            arr[cbPosition] = arr[r - 1];// 将最后一位数值赋值给已经被使用的cbPosition
        } while (cbRandCount < count);
        return arr2;
    }

    public static Map getCouponByCouponId(String couponCode,Delegator delegator) throws GenericEntityException {
        GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode",couponCode));
        Timestamp useBeginDate=null;
        Timestamp useEndDate=null;
        String validitType = productPromoCoupon.getString("validitType");
        if (validitType.equals("ROLL")) {
            useBeginDate = new Timestamp(System.currentTimeMillis());
            int validitDays = productPromoCoupon.getLong("validitDays").intValue();
            useEndDate = new Timestamp(System.currentTimeMillis() + validitDays * 24 * 60 * 60 * 1000);
        } else {
            useBeginDate = productPromoCoupon.getTimestamp("useBeginDate");
            useEndDate = productPromoCoupon.getTimestamp("useEndDate");
        }
        Map couponInfo = FastMap.newInstance();
        couponInfo.put("useBeginDate",CommonUtils.getStringDate(useBeginDate));
        couponInfo.put("useEndDate",CommonUtils.getStringDate(useEndDate));
        couponInfo.put("couponName",productPromoCoupon.getString("couponName"));
        couponInfo.put("couponType",productPromoCoupon.getString("couponType"));
        couponInfo.put("payFill",productPromoCoupon.getString("payFill"));
        couponInfo.put("payReduce",productPromoCoupon.getString("payReduce"));
        couponInfo.put("applyScope",productPromoCoupon.getString("applyScope"));
        couponInfo.put("couponProductType",productPromoCoupon.getString("couponProductType"));

        return couponInfo;
    }

    public static Map getCouponByPromoCodeId(String productPromoCodeId,Delegator delegator) throws GenericEntityException {

        GenericValue productPromoCode = delegator.findByPrimaryKey("ProductPromoCode", UtilMisc.toMap("productPromoCodeId",productPromoCodeId));

        GenericValue productPromoCoupon = delegator.findByPrimaryKey("ProductPromoCoupon", UtilMisc.toMap("couponCode",productPromoCode.getString("couponCode")));

        Map couponInfo = FastMap.newInstance();
        couponInfo.put("useBeginDate",CommonUtils.getStringDate(productPromoCode.getTimestamp("fromDate")));
        couponInfo.put("useEndDate",CommonUtils.getStringDate(productPromoCode.getTimestamp("thruDate")));
        couponInfo.put("couponName",productPromoCoupon.getString("couponName"));
        couponInfo.put("couponType",productPromoCoupon.getString("couponType"));
        couponInfo.put("payFill",productPromoCoupon.getString("payFill"));
        couponInfo.put("payReduce",productPromoCoupon.getString("payReduce"));
        couponInfo.put("applyScope",productPromoCoupon.getString("applyScope"));
        couponInfo.put("couponProductType",productPromoCoupon.getString("couponProductType"));

        return couponInfo;
    }

    public static synchronized String getUsefulCouponByCouponId(String couponId, Delegator delegator, LocalDispatcher dispatcher, GenericValue userLogin) throws GenericEntityException {
        boolean beganTransaction = TransactionUtil.begin();
        GenericValue coupon = delegator.findByPrimaryKey("ProductPromoCoupon",UtilMisc.toMap("couponCode",couponId));
        TransactionUtil.commit(beganTransaction);
        if(coupon==null){
            return  null;
        }
        long couponQuantity=coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("couponQuantity");
        long userCount =coupon.getLong("couponQuantity")==null? 0L :coupon.getLong("userCount");
        if((couponQuantity-userCount)>0){
            //创建一条代金券
            Map serviceIn = FastMap.newInstance();
            serviceIn.put("couponCode", couponId);
            serviceIn.put("userLogin", userLogin);
            try {
                Map<String, Object> retMap =dispatcher.runSync("createOneProductPromoCode", serviceIn);
                String productPromoCodeId = (String) retMap.get("productPromoCodeId");
                return productPromoCodeId;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
