package com.yuaoq.yabiz.mobile.common;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.LocalDispatcher;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonUtils {
    public static final String module = CommonUtils.class.getName();
    public static String getPartyId(Delegator delegator, String userLoginId) throws GenericEntityException {
        String partyId = delegator.findByAnd("UserLogin", UtilMisc.toMap("userLoginId", userLoginId)).get(0).getString("partyId");
        return partyId;
    }

    public static Map getUserInfo(Delegator delegator, String partyId) throws GenericEntityException {
        if(partyId==null || partyId.length()==0){
           return null;
        }
        Map res=null;
        try{
            res = delegator.findByAnd("Person",UtilMisc.toMap("partyId",partyId)).get(0).toMap();
        }catch (Exception e){
            res= FastMap.newInstance();
            res.put("nickname",partyId+"异常");
            res.put("headphoto","");
            res.put("wxAppOpenId","");
        }
        return res;
    }
    /**
     * 获取今天开始时间
     * @return
     */
    public static Date getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime();
    }

    /**
     * 获取今天结束时间
     * @return
     */
    public static Date getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR_OF_DAY, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime();
    }


    public static String getStringDate(Date timestamp){
        if(timestamp==null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
    }
    public static String getStringDate(Timestamp timestamp){
        if(timestamp==null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(timestamp);
    }

    public static Date getDateFromStr(String timestamp) throws ParseException {
        if(UtilValidate.isEmpty(timestamp)){
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(timestamp);
    }
    public static String getStringDate2(Timestamp timestamp){
        if(timestamp==null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(timestamp);
    }
    public static String getUserNick(String partyId, Delegator delegator){
        try {
            if(UtilValidate.isEmpty(partyId)){
                return "";
            }
            GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));
            if(person==null){
                return "";
            }
            return person.getString("nickname");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error closing EntityListIterator when indexing content keywords.", module);
            return "";
        }

    }

    public static String getImgUrl(Delegator delegator,LocalDispatcher dispatcher,String contentId,String baseImgUrl) throws IOException, GeneralException {
        if(UtilValidate.isEmpty(contentId)){
            return "";
        }
        String imgUrl = baseImgUrl + ContentWorker.renderContentAsText(dispatcher, delegator, contentId, false);

        return imgUrl;
    }

    public static String getWxAppOpenId(Delegator delegator, String partyId) throws GenericEntityException {
        if(UtilValidate.isEmpty(partyId)){
            return "";
        }
        GenericValue person = delegator.findByPrimaryKey("Person",UtilMisc.toMap("partyId",partyId));

        if(person==null){
            return "";
        }
        return person.getString("wxAppOpenId");
    }

    public static List<Map> getVirtualPerson(Delegator delegator,int nums) throws SQLException, GenericEntityException {

        String sql = "select top " + nums + " * from VITUAL_PERSON ORDER BY NEWID()";

        List<Map> list = null;
        SQLProcessor sqlP = null;
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            sqlP = new SQLProcessor(helperInfo);
            sqlP.executeQuery(sql);
            ResultSet rs = sqlP.getResultSet();

            Map resMap = FastMap.newInstance();
            list = FastList.newInstance();
            while (rs.next()) {
                String nickname = rs.getString("NICK_NAME");
                String headphoto = rs.getString("IMG_URL");
                String personId = rs.getString("PERSON_ID");
                Map map = FastMap.newInstance();
                map.put("nickname", nickname);
                map.put("headphoto", headphoto);
                map.put("personId", personId);
                list.add(map);
            }

        } catch (Exception e) {
            Debug.logError(e, "源生sql异常.", module);

        }finally {
            sqlP.close();
        }

        return list;
    }
}
