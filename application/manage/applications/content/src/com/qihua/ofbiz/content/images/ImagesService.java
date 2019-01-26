package com.qihua.ofbiz.content.images;

import javolution.util.FastList;
import javolution.util.FastMap;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.jdbc.SQLProcessor;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by changsy on 16/1/20.
 */
public class ImagesService {

//    public static Map<String,Object> listServerImages(DispatchContext dcx,Map<String,? extends Object> context){
//        Map<String,Object> result = ServiceUtil.returnSuccess();
//        Integer page = (Integer) context.get("page")-1;
//        String classify = (String) context.get("classify");
//        Delegator delegator = dcx.getDelegator();
//        int listSize = 0;
//        int lowIndex = 0;
//        int highIndex = 0;
//        int viewSize = 6;
//        try {
//            viewSize = Integer.parseInt((String) context.get("size"));
//        } catch (Exception e) {
//            viewSize = 6;
//        }
//
//        lowIndex = page * viewSize + 1;
//        highIndex = (page + 1) * viewSize;
//        List<String> orderBy = FastList.newInstance();
//        orderBy.add("-fromDate");
//
//        List<String> fieldsToSelect = FastList.newInstance();
//
//
//        List<EntityCondition> andExprs = FastList.newInstance();
//        EntityCondition mainCond = null;
//
//
//        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
//        andExprs.add(EntityCondition.makeCondition("fromDate", EntityOperator.LESS_THAN_EQUAL_TO, nowTimestamp));
//        andExprs.add(EntityCondition.makeCondition(EntityCondition.makeCondition("thruDate", EntityOperator.EQUALS, null), EntityOperator.OR, EntityCondition.makeCondition("thruDate", EntityOperator.GREATER_THAN, nowTimestamp)));
//
//        //party content imglogo
//        //product content
//        //classify  1:party 2:product
//        List images = FastList.newInstance();
//        if(classify.equals("1")){
//            try {
//                fieldsToSelect.add("contentId");
//                fieldsToSelect.add("fromDate");
//                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("partyContentTypeId"), EntityOperator.EQUALS, "LGOIMGURL"));
//                if (andExprs.size() > 0) mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
//                // set distinct on so we only get one row per order
//                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
//                // using list iterator
//
//                EntityListIterator pli = delegator.find("PartyContent", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
//
//                // get the partial list for this page
//                List<GenericValue> pimages = pli.getPartialList(lowIndex, viewSize);
//                if(UtilValidate.isNotEmpty(pimages)){
//                    for (int i = 0; i < pimages.size(); i++) {
//                        GenericValue pcontent =  (GenericValue)pimages.get(i);
//                        Map<String,String> map = new HashMap<String,String>();
//                        String contentId = (String)pcontent.get("contentId");
//                        map.put("id",(String)pcontent.get("contentId"));
//                        map.put("url","/content/control/stream?contentId="+contentId);
//                        images.add(map);
//                    }
//                }
//
//                // attempt to get the full size
//                listSize = pli.getResultsSizeAfterPartialList();
//                if (highIndex > listSize) {
//                    highIndex = listSize;
//                }
//
//                // close the list iterator
//                pli.close();
//
//            } catch (GenericEntityException e) {
//                e.printStackTrace();
//            }
//
//
//        }else if(classify.equals("2")){
//            try {
//                fieldsToSelect.add("contentId");
//                fieldsToSelect.add("objectInfo");
//                andExprs.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("parentTypeId"), EntityOperator.EQUALS, "IMAGE"));
//                if (andExprs.size() > 0) mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.AND);
//                // set distinct on so we only get one row per order
//                EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
//                // using list iterator
//                EntityListIterator pli = delegator.find("ProductContentAndType", mainCond, null, UtilMisc.makeSetWritable(fieldsToSelect), orderBy, findOpts);
//
//                // get the partial list for this page
//                List<GenericValue> pimages = pli.getPartialList(lowIndex, viewSize);
//                if(UtilValidate.isNotEmpty(pimages)){
//                    for (int i = 0; i < pimages.size(); i++) {
//                        GenericValue pcontent =  (GenericValue)pimages.get(i);
//                        Map<String,String> map = new HashMap<String,String>();
//                        map.put("id",(String)pcontent.get("contentId"));
//                        String objectInfo = (String)pcontent.get("objectInfo");
//                        map.put("url", objectInfo);
//                        images.add(map);
//
//                    }
//                }
//
//                // attempt to get the full size
//                listSize = pli.getResultsSizeAfterPartialList();
//                if (highIndex > listSize) {
//                    highIndex = listSize;
//                }
//
//                // close the list iterator
//                pli.close();
//
//            } catch (GenericEntityException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//
//        result.put("curPageNum",page);
//
//        Double dlistsize = new Double(listSize);
//        Double dviewSize = new Double(viewSize);
//        result.put("maxPageNum",new Double(Math.ceil(dlistsize/dviewSize)).intValue());
//
//
//        result.put("imglist",images);
//        List<Map> classList = FastList.newInstance();
//        Map<String,String> map = new HashMap<String,String>();
//        map.put("title","会员图片");
//        map.put("id","1");
//        classList.add(map);
//        Map<String,String> map1 = new HashMap<String,String>();
//        map1.put("title", "产品图片");
//        map1.put("id", "2");
//        classList.add(map1);
//        result.put("classifylist",classList);
//        result.put("status",1);
//
//
//
//        return result;
//    }
    
    
    public static Map<String, Object> listServerImages2(DispatchContext dct, Map<String, ? extends Object> context) {
        Delegator delegator = dct.getDelegator();
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        Integer viewIndex = (Integer) context.get("page") - 1;
        String classify = (String) context.get("classify");
        String imgindex = (String) context.get("imgindex");
        String isInner = (String) context.get("isInner");
        String ownerPartyId = (String) context.get("ownerPartyId");
        List<GenericValue> images = FastList.newInstance();
        List<Map<String, Object>> imageList = FastList.newInstance();
        int imageSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        // set the page parameters
        int viewSize = 6;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 6;
        }
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        
        // default view settings
        dynamicView.addMemberEntity("PCAD", "PartyContentGrpDetailAndDataSource");
        
        dynamicView.addAlias("PCAD", "contentId");
        dynamicView.addAlias("PCAD", "fromDate");
        dynamicView.addAlias("PCAD", "partyContentTypeId");
        dynamicView.addAlias("PCAD", "objectInfo");
        dynamicView.addAlias("PCAD", "partyId");
        dynamicView.addAlias("PCAD","imgGroupId");
        dynamicView.addAlias("PCAD","ftpObjectInfo");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("objectInfo");
        fieldsToSelect.add("partyContentTypeId");
        fieldsToSelect.add("ftpObjectInfo");
        dynamicView.setGroupBy(fieldsToSelect);
        orderBy.add("-fromDate");
    
        if (!"Y".equals(isInner)) {
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_PRODUCT"));
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_CONTENT"));
            List<EntityCondition> partyCond = FastList.newInstance();
            partyCond.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, ownerPartyId));
            if(UtilValidate.isNotEmpty(classify) && (!"1".equals(classify))) {
                partyCond.add(EntityCondition.makeCondition("imgGroupId", EntityOperator.EQUALS, classify));
            }
            partyCond.add(EntityCondition.makeCondition(andExprs, EntityOperator.OR));
        
            mainCond = EntityCondition.makeCondition(partyCond, EntityOperator.AND);
        
        } else {
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_PRODUCT"));
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_CONTENT"));
           
            if(UtilValidate.isNotEmpty(classify) && (!"1".equals(classify))) {
                List<EntityCondition> partyCond = FastList.newInstance();
                partyCond.add(EntityCondition.makeCondition("imgGroupId", EntityOperator.EQUALS, classify));
                partyCond.add(EntityCondition.makeCondition(andExprs, EntityOperator.OR));
                mainCond = EntityCondition.makeCondition(partyCond, EntityOperator.AND);
            }else{
                mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.OR);
            }
          
            
        }
        // build the main condition
        try {
            // get the indexes for the partial list
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            
            // get the partial list for this page
            images = pli.getPartialList(lowIndex, viewSize);
            
            // attempt to get the full size
            imageSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > imageSize) {
                highIndex = imageSize;
            }
            String web_url = new File("").getCanonicalPath() + "/images/webapp";
            if (UtilValidate.isNotEmpty(images)) {
                for (int i = 0; i < images.size(); i++) {
                    GenericValue imageData = images.get(i);
                    Map<String, Object> imageMap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(imageData.get("objectInfo"))) {
                        String objectInfo = imageData.get("objectInfo").toString();
                        int idx = objectInfo.lastIndexOf("/images/");
                        if (idx == -1) {
                            objectInfo = "/images" + objectInfo;
                        } else if (idx > 0) {
                            objectInfo = objectInfo.substring(idx, objectInfo.length());
                        }
                        String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                        if("FTP".equals(uploadType)){
                            String ftpObjectInfo = imageData.getString("ftpObjectInfo");
                            if(UtilValidate.isEmpty(ftpObjectInfo)){
                                ftpObjectInfo = objectInfo;
                            }
                            if(UtilValidate.isNotEmpty(objectInfo)&& UtilValidate.isNotEmpty(ftpObjectInfo)) {
                                Map<String, String> map = new HashMap<String, String>();
                                imageMap.put("id", imageData.get("contentId"));
                                imageMap.put("url", "/content/control/getImage?contentId=" + imageData.get("contentId"));
                            }
                    
                        }else {
                            if(UtilValidate.isNotEmpty(objectInfo)) {
                                //判断图片是否存在，存在则显示，否则不显示
                                if (new File(web_url + objectInfo).exists()) {
                                    Map<String, String> map = new HashMap<String, String>();
                                    imageMap.put("id", imageData.get("contentId"));
                                    imageMap.put("url", "/content/control/getImage?contentId=" + imageData.get("contentId"));
    
                                }
                            }
                        }
                    }
                    if(UtilValidate.isNotEmpty(imageMap)) {
                        imageList.add(imageMap);
                    }
                }
            }
            // close the list iterator
            pli.close();
        } catch (GenericEntityException e) {
            
            return ServiceUtil.returnError(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        EntityCondition cond = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("imgGroupId"), EntityOperator.NOT_EQUAL, "PRIVATE_IMG_GROUP");
        //分组列表
        List<Map> classList = FastList.newInstance();
        try {
            List<GenericValue> ig_list = delegator.findList("ImgGroup", cond, null, null, null, false);
            for (GenericValue ig_gv : ig_list) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("title", ig_gv.getString("imgGroupName"));
                map.put("id", ig_gv.getString("imgGroupId"));
                classList.add(map);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        if (UtilValidate.isNotEmpty(imgindex)) {
            result.put("imgindex", imgindex);
        }
        result.put("maxPageNum", new Double(Math.ceil( new Double(imageSize)/(new Double(viewSize)))).intValue());
        result.put("imglist", imageList);
        result.put("curPageNum", viewIndex + 1);
        result.put("classifylist", classList);
        result.put("status", 1);
        return result;
    }
    
    public static Map<String, Object> listServerImages1(DispatchContext dcx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Integer page = (Integer) context.get("page") - 1;
        String classify = (String) context.get("classify");
        String imgindex = (String) context.get("imgindex");
        String isInner = (String) context.get("isInner");
        String ownerParty = (String) context.get("ownerPartyId");
        Delegator delegator = dcx.getDelegator();
        int listSize = 0;
        int lowIndex = 0;
        int highIndex = 0;
        int viewSize = 6;
        try {
            viewSize = Integer.parseInt((String) context.get("size"));
        } catch (Exception e) {
            viewSize = 6;
        }
        
        List images = FastList.newInstance();
        List totalList = FastList.newInstance();
        
        
        String sql = "";
        //未分类
        if ("1".equals(classify)) {
            sql = "select c.CONTENT_ID,c.CREATED_STAMP,dr.OBJECT_INFO from content c " +
                    "join data_resource dr on dr.DATA_RESOURCE_ID = c.DATA_RESOURCE_ID " +
                    "where c.CONTENT_ID not in ( " +
                    "select iga.CONTENT_ID from img_group_assoc iga " +
                    ") " +
                    "and c.MIME_TYPE_ID like 'image/%' " +
                    "and c.DATA_RESOURCE_ID != 'DEFAULT_IMG' " +
                    "ORDER BY c.CREATED_STAMP desc ";
        } else {
            sql = "select c.CONTENT_ID,c.CREATED_STAMP,dr.OBJECT_INFO from content c " +
                    "join data_resource dr on dr.DATA_RESOURCE_ID = c.DATA_RESOURCE_ID " +
                    "join img_group_assoc iga on iga.CONTENT_ID = c.CONTENT_ID " +
                    "where c.MIME_TYPE_ID like 'image/%' " +
                    "and c.DATA_RESOURCE_ID != 'DEFAULT_IMG' " +
                    "and iga.IMG_GROUP_ID = '" + classify + "' " +
                    "ORDER BY c.CREATED_STAMP desc ";
        }
        
        try {
            GenericHelperInfo helperInfo = delegator.getGroupHelperInfo("org.ofbiz");
            SQLProcessor sqlP = new SQLProcessor(helperInfo);
            //获取总图片数
            ResultSet rs = sqlP.executeQuery(sql);
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            String web_url = new File("").getCanonicalPath() + "/images/webapp";
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), rs.getObject(i));
                }
                //统一图片路径
                if (UtilValidate.isNotEmpty(rowData.get("OBJECT_INFO"))) {
                    String objectInfo = rowData.get("OBJECT_INFO").toString();
                    int idx = objectInfo.lastIndexOf("/images/");
                    if (idx == -1) {
                        objectInfo = "/images" + objectInfo;
                    } else if (idx > 0) {
                        objectInfo = objectInfo.substring(idx, objectInfo.length());
                    }
                    //判断图片是否存在，存在则显示，否则不显示
                    if (new File(web_url + objectInfo).exists()) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("id", (String) rowData.get("CONTENT_ID"));
                        map.put("url", "/content/control/getImage?contentId=" + (String) rowData.get("CONTENT_ID"));
                        totalList.add(map);
                        listSize++;
                    }
                }
            }
            rs.close();
            sqlP.close();
            
            //获取分页数据
            lowIndex = page * viewSize;
            int total_count = totalList.size();
            for (int i = 0; i < viewSize; i++) {
                if (total_count > lowIndex + i) {
                    images.add(totalList.get(lowIndex + i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (UtilValidate.isNotEmpty(imgindex)) {
            result.put("imgindex", imgindex);
        }
        result.put("curPageNum", page + 1);
        Double dlistsize = new Double(listSize);
        Double dviewSize = new Double(viewSize);
        result.put("maxPageNum", new Double(Math.ceil(dlistsize / dviewSize)).intValue());
        
        result.put("imglist", images);
        
        EntityCondition cond = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("imgGroupId"), EntityOperator.NOT_EQUAL, "PRIVATE_IMG_GROUP");
        //分组列表
        List<Map> classList = FastList.newInstance();
        try {
            List<GenericValue> ig_list = delegator.findList("ImgGroup", cond, null, null, null, false);
            for (GenericValue ig_gv : ig_list) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("title", ig_gv.getString("imgGroupName"));
                map.put("id", ig_gv.getString("imgGroupId"));
                classList.add(map);
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        result.put("classifylist", classList);
        result.put("status", 1);
        return result;
    }
    
    
    /**
     * 根据图片分组ID查询图片记录 add by qianjin 2016.04.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getImgListByImgGroupId(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String imgGroupId = (String) context.get("imgGroupId");
        
        try {
            List<GenericValue> imgList = delegator.findByAnd("ImgGroupAssoc", UtilMisc.toMap("imgGroupId", imgGroupId));
            result.put("imgList", imgList);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    
    /////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * 图片分组列表查询 add by qianjin 2016.04.05
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getImgGroupList(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //记录集合
        List<GenericValue> imgGroupList = FastList.newInstance();
        
        EntityCondition cond = EntityCondition.makeCondition(EntityFunction.UPPER_FIELD("imgGroupId"), EntityOperator.NOT_EQUAL, "PRIVATE_IMG_GROUP");
        //动态view
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        dynamicView.addMemberEntity("IG", "ImgGroup");
        dynamicView.addAlias("IG", "imgGroupId", "imgGroupId", null, null, true, null);
        dynamicView.addAlias("IG", "imgGroupName");
        
        dynamicView.addMemberEntity("IGA", "ImgGroupAssoc");
        dynamicView.addAlias("IGA", "imgCount", "imgGroupAssocId", null, null, null, "count");
        dynamicView.addViewLink("IG", "IGA", Boolean.TRUE, ModelKeyMap.makeKeyMapList("imgGroupId", "imgGroupId"));
        
        dynamicView.setGroupBy(UtilMisc.toList("imgGroupId", "imgGroupName"));
        //显示字段集合
        List<String> fieldsToSelect = FastList.newInstance();
        fieldsToSelect.add("imgGroupId");
        fieldsToSelect.add("imgGroupName");
        fieldsToSelect.add("imgCount");
        
        try {
            //去除重复数据
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, -1, true);
            //查询的数据Iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, cond, null, fieldsToSelect, null, findOpts);
            result.put("imgGroupList", pli.getCompleteList());
            pli.close();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 图片分组新增 add by qianjin 2016.04.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addImgGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String imgGroupName = (String) context.get("imgGroupName");
        //获取新的主键ID
        String imgGroupId = delegator.getNextSeqId("ImgGroup");
        //新增一条imgGroup记录
        GenericValue imgGroup_gv = delegator.makeValue("ImgGroup", UtilMisc.toMap("imgGroupId", imgGroupId));
        imgGroup_gv.setString("imgGroupName", imgGroupName);
        try {
            imgGroup_gv.create();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("imgGroupId", imgGroupId);
        result.put("imgGroupName", imgGroupName);
        return result;
    }
    
    /**
     * 图片分组修改 add by qianjin 2016.04.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> editImgGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String imgGroupId = (String) context.get("imgGroupId");
        String imgGroupName = (String) context.get("imgGroupName");
        
        //根据ID查询imgGroup记录
        GenericValue imgGroup_gv;
        try {
            imgGroup_gv = delegator.findByPrimaryKey("ImgGroup", UtilMisc.toMap("imgGroupId", imgGroupId));
            imgGroup_gv.setString("imgGroupName", imgGroupName);
            imgGroup_gv.store();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("imgGroupId", imgGroupId);
        result.put("imgGroupName", imgGroupName);
        return result;
    }
    
    /**
     * 图片分组删除 add by qianjin 2016.04.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> delImgGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //参数
        String imgGroupId = (String) context.get("imgGroupId");
        
        try {
            delegator.removeByAnd("ImgGroupAssoc", UtilMisc.toMap("imgGroupId", imgGroupId));
            delegator.removeByAnd("ImgGroup", UtilMisc.toMap("imgGroupId", imgGroupId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        result.put("imgGroupId", imgGroupId);
        return result;
    }
    
    /**
     * 根据图片分组ID查询图片记录 add by qianjin 2016.04.06
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getImgsByGroupId(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //分组ID
        String groupId = (String) context.get("groupId");
        String isInner = (String) context.get("isInner");
        String ownerPartyId = (String) context.get("ownerPartyId");
        //记录集合
        List<Map<String, Object>> recordsList = FastList.newInstance();
        //总记录数
        int recordsListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        
        // default view settings
        dynamicView.addMemberEntity("PCAD", "PartyContentGrpDetailAndDataSource");
        dynamicView.addAlias("PCAD", "contentId");
        dynamicView.addAlias("PCAD", "fromDate");
        dynamicView.addAlias("PCAD", "partyContentTypeId");
        dynamicView.addAlias("PCAD", "objectInfo");
        dynamicView.addAlias("PCAD", "partyId");
        dynamicView.addAlias("PCAD","imgGroupId");
       
        dynamicView.addAlias("PCAD","ftpObjectInfo");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("objectInfo");
        fieldsToSelect.add("partyContentTypeId");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("ftpObjectInfo");
        dynamicView.setGroupBy(fieldsToSelect);
        orderBy.add("-fromDate");
        if (!"Y".equals(isInner)) {
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_PRODUCT"));
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_CONTENT"));
            List<EntityCondition> partyCond = FastList.newInstance();
            partyCond.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, ownerPartyId));
            partyCond.add(EntityCondition.makeCondition("imgGroupId", EntityOperator.EQUALS, groupId));
            partyCond.add(EntityCondition.makeCondition(andExprs, EntityOperator.OR));
            
            mainCond = EntityCondition.makeCondition(partyCond, EntityOperator.AND);
            
        } else {
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_PRODUCT"));
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_CONTENT"));
            List<EntityCondition> partyCond = FastList.newInstance();
            partyCond.add(EntityCondition.makeCondition("imgGroupId", EntityOperator.EQUALS, groupId));
            partyCond.add(EntityCondition.makeCondition(andExprs, EntityOperator.OR));
    
            mainCond = EntityCondition.makeCondition(partyCond, EntityOperator.AND);
        }
        // build the main condition
        try {
            // get the indexes for the partial list
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            
            // get the partial list for this page
            List<GenericValue> images = pli.getPartialList(lowIndex, viewSize);
            
            // attempt to get the full size
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }
            String web_url = new File("").getCanonicalPath() + "/images/webapp";
            if (UtilValidate.isNotEmpty(images)) {
                for (int i = 0; i < images.size(); i++) {
                    GenericValue imageData = images.get(i);
                    Map<String, Object> imageMap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(imageData.get("objectInfo"))) {
                        String objectInfo = imageData.get("objectInfo").toString();
                        int idx = objectInfo.lastIndexOf("/images/");
                        if (idx == -1) {
                            objectInfo = "/images" + objectInfo;
                        } else if (idx > 0) {
                            objectInfo = objectInfo.substring(idx, objectInfo.length());
                        }
                        String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                        if("FTP".equals(uploadType)){
                            String ftpObjectInfo = imageData.getString("ftpObjectInfo");
                            if(UtilValidate.isEmpty(ftpObjectInfo)){
                                ftpObjectInfo = objectInfo;
                            }
                            if(UtilValidate.isNotEmpty(objectInfo)&& UtilValidate.isNotEmpty(ftpObjectInfo)) {
                                Map<String, String> map = new HashMap<String, String>();
                                imageMap.put("CONTENT_ID", imageData.get("contentId"));
                                imageMap.put("CREATED_STAMP", imageData.get("fromDate"));
                                imageMap.put("OBJECT_INFO", "/content/control/getImage?contentId=" + imageData.get("contentId"));
                            }
                    
                        }else {
                            if(UtilValidate.isNotEmpty(objectInfo)) {
                                //判断图片是否存在，存在则显示，否则不显示
                                if (new File(web_url + objectInfo).exists()) {
                                    Map<String, String> map = new HashMap<String, String>();
                                    imageMap.put("CONTENT_ID", imageData.get("contentId"));
                                    imageMap.put("CREATED_STAMP", imageData.get("fromDate"));
                                    imageMap.put("OBJECT_INFO", "/content/control/getImage?contentId=" + imageData.get("contentId"));
                            
                                }
                            }
                        }
                    }
                    if(UtilValidate.isNotEmpty(imageMap)) {
                        recordsList.add(imageMap);
                    }
                }
            }
            // close the list iterator
            pli.close();
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        
        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", recordsListSize);
        return result;
    }
    
    /**
     * 获取所有未分组图片 add by qianjin 2016.06.15
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> getImgListNotInGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        String isInner = (String) context.get("isInner");
        String ownerPartyId = (String) context.get("ownerPartyId");
        //记录集合
        List<Map<String, Object>> recordsList = FastList.newInstance();
        //总记录数
        int recordsListSize = 0;
        //查询开始条数
        int lowIndex = 0;
        //查询结束条数
        int highIndex = 0;
        
        //跳转的页数
        int viewIndex = 0;
        try {
            viewIndex = Integer.parseInt((String) context.get("VIEW_INDEX"));
        } catch (Exception e) {
            viewIndex = 0;
        }
        
        //每页显示记录条数
        int viewSize = 20;
        try {
            viewSize = Integer.parseInt((String) context.get("VIEW_SIZE"));
        } catch (Exception e) {
            viewSize = 20;
        }
        
        DynamicViewEntity dynamicView = new DynamicViewEntity();
        // define the main condition & expression list
        List<EntityCondition> andExprs = FastList.newInstance();
        EntityCondition mainCond = null;
        
        List<String> orderBy = FastList.newInstance();
        List<String> fieldsToSelect = FastList.newInstance();
        
        // default view settings
        dynamicView.addMemberEntity("PCAD", "PartyContentDetailAndDataSource");
        dynamicView.addAlias("PCAD", "contentId");
        dynamicView.addAlias("PCAD", "fromDate");
        dynamicView.addAlias("PCAD", "partyContentTypeId");
        dynamicView.addAlias("PCAD", "objectInfo");
        dynamicView.addAlias("PCAD", "partyId");
        dynamicView.addAlias("PCAD","objectInfo");
        dynamicView.addAlias("PCAD","ftpObjectInfo");
        fieldsToSelect.add("contentId");
        fieldsToSelect.add("fromDate");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("objectInfo");
        fieldsToSelect.add("partyContentTypeId");
        fieldsToSelect.add("partyId");
        fieldsToSelect.add("ftpObjectInfo");
        fieldsToSelect.add("objectInfo");
        dynamicView.setGroupBy(fieldsToSelect);
        orderBy.add("-fromDate");
        if (!"Y".equals(isInner)) {
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_PRODUCT"));
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_CONTENT"));
            
            List<EntityCondition> partyCond = FastList.newInstance();
            partyCond.add(EntityCondition.makeCondition("partyId", EntityOperator.EQUALS, ownerPartyId));
            partyCond.add(EntityCondition.makeCondition(andExprs, EntityOperator.OR));
            mainCond = EntityCondition.makeCondition(partyCond, EntityOperator.AND);
            
        } else {
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_PRODUCT"));
            andExprs.add(EntityCondition.makeCondition("partyContentTypeId", EntityOperator.EQUALS, "PARTY_IMG_CONTENT"));
            mainCond = EntityCondition.makeCondition(andExprs, EntityOperator.OR);
        }
        // build the main condition
        try {
            // get the indexes for the partial list
            lowIndex = viewIndex * viewSize + 1;
            highIndex = (viewIndex + 1) * viewSize;
            
            // set distinct on so we only get one row per order
            EntityFindOptions findOpts = new EntityFindOptions(true, EntityFindOptions.TYPE_SCROLL_INSENSITIVE, EntityFindOptions.CONCUR_READ_ONLY, -1, highIndex, true);
            // using list iterator
            EntityListIterator pli = delegator.findListIteratorByCondition(dynamicView, mainCond, null, fieldsToSelect, orderBy, findOpts);
            
            // get the partial list for this page
            List<GenericValue> images = pli.getPartialList(lowIndex, viewSize);
            
            // attempt to get the full size
            recordsListSize = pli.getResultsSizeAfterPartialList();
            if (highIndex > recordsListSize) {
                highIndex = recordsListSize;
            }
            String web_url = new File("").getCanonicalPath() + "/images/webapp";
            if (UtilValidate.isNotEmpty(images)) {
                for (int i = 0; i < images.size(); i++) {
                    GenericValue imageData = images.get(i);
                    Map<String, Object> imageMap = FastMap.newInstance();
                    if (UtilValidate.isNotEmpty(imageData.get("objectInfo"))) {
                        String objectInfo = imageData.get("objectInfo").toString();
                        int idx = objectInfo.lastIndexOf("/images/");
                        if (idx == -1) {
                            objectInfo = "/images" + objectInfo;
                        } else if (idx > 0) {
                            objectInfo = objectInfo.substring(idx, objectInfo.length());
                        }
                        String uploadType = UtilProperties.getPropertyValue("content", "content.image.upload.type");
                        if("FTP".equals(uploadType)){
                            String ftpObjectInfo = imageData.getString("ftpObjectInfo");
                            if(UtilValidate.isEmpty(ftpObjectInfo)){
                                ftpObjectInfo = objectInfo;
                            }
                            if(UtilValidate.isNotEmpty(objectInfo)&& UtilValidate.isNotEmpty(ftpObjectInfo)) {
                                Map<String, String> map = new HashMap<String, String>();
                                imageMap.put("CONTENT_ID", imageData.get("contentId"));
                                imageMap.put("CREATED_STAMP", imageData.get("fromDate"));
                                imageMap.put("OBJECT_INFO", "/content/control/getImage?contentId=" + imageData.get("contentId"));
                            }
    
                        }else {
                            if(UtilValidate.isNotEmpty(objectInfo)) {
                                //判断图片是否存在，存在则显示，否则不显示
                                if (new File(web_url + objectInfo).exists()) {
                                    Map<String, String> map = new HashMap<String, String>();
                                    imageMap.put("CONTENT_ID", imageData.get("contentId"));
                                    imageMap.put("CREATED_STAMP", imageData.get("fromDate"));
                                    imageMap.put("OBJECT_INFO", "/content/control/getImage?contentId=" + imageData.get("contentId"));
        
                                }
                            }
                        }
                    }
                    if(UtilValidate.isNotEmpty(imageMap)) {
                        recordsList.add(imageMap);
                    }
                }
            }
            // close the list iterator
            pli.close();
        } catch (GenericEntityException e) {
            
            return ServiceUtil.returnError(e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //返回的参数
        result.put("recordsList", recordsList);
        result.put("totalSize", recordsListSize);
        return result;
    }
    
    /**
     * 图片移动分组 add by qianjin 2016.06.18
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> changeGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //图片ID
        String ids = (String) context.get("ids");
        //分组ID
        String groupId = (String) context.get("groupId");
        
        //转换成list
        List idList = FastList.newInstance();
        for (String id : ids.split(",")) {
            idList.add(id);
        }
        
        try {
            //编辑where条件
            EntityCondition mainCond = EntityCondition.makeCondition("contentId", EntityOperator.IN, idList);
            List<GenericValue> iga_list = delegator.findList("ImgGroupAssoc", mainCond, null, null, null, false);
            //判断是否有该分组数据，如果没有，则新增，有则修改
            if (UtilValidate.isEmpty(iga_list)) {
                for (int i = 0; i < idList.size(); i++) {
                    String nextId = delegator.getNextSeqId("ImgGroupAssoc");
                    GenericValue iga_gv = delegator.makeValue("ImgGroupAssoc", UtilMisc.toMap("imgGroupAssocId", nextId));
                    iga_gv.set("imgGroupId", groupId);
                    iga_gv.set("contentId", idList.get(i));
                    iga_gv.create();
                }
            } else {
                for (GenericValue iga_gv : iga_list) {
                    iga_gv.set("imgGroupId", groupId);
                    iga_gv.store();
                }
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 新增图片并保存到分组 add by qianjin 2016.06.21
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> addImgToGroup(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //图片ID
        String contentId = (String) context.get("contentId");
        String groupId = (String) context.get("groupId");
        
        String nextId = delegator.getNextSeqId("ImgGroupAssoc");
        GenericValue iga_gv = delegator.makeValue("ImgGroupAssoc", UtilMisc.toMap("imgGroupAssocId", nextId));
        iga_gv.set("imgGroupId", groupId);
        iga_gv.set("contentId", contentId);
        try {
            iga_gv.create();
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * 图片删除操作 add by qianjin 2016.06.21
     *
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> delImgs(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Delegator delegator = dctx.getDelegator();
        //图片ID
        String ids = (String) context.get("ids");
        
        //转换成list
        List idList = FastList.newInstance();
        for (String id : ids.split(",")) {
            idList.add(id);
        }
        
        try {
            //编辑where条件
            EntityCondition mainCond = EntityCondition.makeCondition("contentId", EntityOperator.IN, idList);
            List<GenericValue> c_list = delegator.findList("Content", mainCond, null, null, null, false);
            //设置content的dataResourceId为默认图片
            for (GenericValue c_gv : c_list) {
                c_gv.set("dataResourceId", "DEFAULT_IMG");
                c_gv.store();
            }
            //删除图片分组关联表数据
            delegator.removeByCondition("ImgGroupAssoc", mainCond);
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        return result;
    }
    /////////////////////////////////////////////////////////////////////////////////////////
}
