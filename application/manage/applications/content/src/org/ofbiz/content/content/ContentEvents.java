/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.content.content;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.jbarcode.JBarcode;
import org.jbarcode.encode.Code128Encoder;
import org.jbarcode.paint.BaseLineTextPainter;
import org.jbarcode.paint.EAN13TextPainter;
import org.jbarcode.paint.WidthCodedPainter;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.security.Security;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


/**
 * ContentEvents Class
 */
public class ContentEvents {
    
    public static final String module = ContentEvents.class.getName();
    public static final String resource = "ContentErrorUiLabels";
    private static int imageCount = 0;
    private static String imagePath;
    private static String fileSeparator = File.separator;
    
    public static File checkExistsImage(File file) {
        if (!file.exists()) {
            imageCount = 0;
            imagePath = null;
            return file;
        }
        imageCount++;
        String filePath = imagePath.substring(0, imagePath.lastIndexOf("."));
        String type = imagePath.substring(imagePath.lastIndexOf(".") + 1);
        file = new File(filePath + "(" + imageCount + ")." + type);
        return checkExistsImage(file);
    }
    
    /**
     * Updates/adds keywords for all contents
     *
     * @param request  HTTPRequest object for the current request
     * @param response HTTPResponse object for the current request
     * @return String specifying the exit status of this event
     */
    public static String updateAllContentKeywords(HttpServletRequest request, HttpServletResponse response) {
        //String errMsg = "";
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        Security security = (Security) request.getAttribute("security");
        
        String updateMode = "CREATE";
        String errMsg = null;
        
        String doAll = request.getParameter("doAll");
        
        // check permissions before moving on...
        if (!security.hasEntityPermission("CONTENTMGR", "_" + updateMode, request.getSession())) {
            Map<String, String> messageMap = UtilMisc.toMap("updateMode", updateMode);
            errMsg = UtilProperties.getMessage(resource, "contentevents.not_sufficient_permissions", messageMap, UtilHttp.getLocale(request));
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
        
        EntityListIterator entityListIterator = null;
        int numConts = 0;
        int errConts = 0;
        
        boolean beganTx = false;
        try {
            // begin the transaction
            beganTx = TransactionUtil.begin(7200);
            try {
                if (Debug.infoOn()) {
                    long count = delegator.findCountByCondition("Content", null, null, null);
                    Debug.logInfo("========== Found " + count + " contents to index ==========", module);
                }
                entityListIterator = delegator.find("Content", null, null, null, null, null);
            } catch (GenericEntityException gee) {
                Debug.logWarning(gee, gee.getMessage(), module);
                Map<String, String> messageMap = UtilMisc.toMap("gee", gee.toString());
                errMsg = UtilProperties.getMessage(resource, "contentevents.error_getting_content_list", messageMap, UtilHttp.getLocale(request));
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                throw gee;
            }
            
            GenericValue content;
            while ((content = entityListIterator.next()) != null) {
                try {
                    ContentKeywordIndex.indexKeywords(content, "Y".equals(doAll));
                } catch (GenericEntityException e) {
                    //request.setAttribute("_ERROR_MESSAGE_", errMsg);
                    Debug.logWarning("[ContentEvents.updateAllContentKeywords] Could not create content-keyword (write error); message: " + e.getMessage(), module);
                    errConts++;
                }
                numConts++;
                if (numConts % 500 == 0) {
                    Debug.logInfo("Keywords indexed for " + numConts + " so far", module);
                }
            }
        } catch (GenericEntityException e) {
            try {
                TransactionUtil.rollback(beganTx, e.getMessage(), e);
            } catch (Exception e1) {
                Debug.logError(e1, module);
            }
            return "error";
        } catch (Throwable t) {
            Debug.logError(t, module);
            request.setAttribute("_ERROR_MESSAGE_", t.getMessage());
            try {
                TransactionUtil.rollback(beganTx, t.getMessage(), t);
            } catch (Exception e2) {
                Debug.logError(e2, module);
            }
            return "error";
        } finally {
            if (entityListIterator != null) {
                try {
                    entityListIterator.close();
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "Error closing EntityListIterator when indexing content keywords.", module);
                }
            }
            
            // commit the transaction
            try {
                TransactionUtil.commit(beganTx);
            } catch (Exception e) {
                Debug.logError(e, module);
            }
        }
        
        if (errConts == 0) {
            Map<String, String> messageMap = UtilMisc.toMap("numConts", Integer.toString(numConts));
            errMsg = UtilProperties.getMessage(resource, "contentevents.keyword_creation_complete_for_contents", messageMap, UtilHttp.getLocale(request));
            request.setAttribute("_EVENT_MESSAGE_", errMsg);
            return "success";
        } else {
            Map<String, String> messageMap = UtilMisc.toMap("numConts", Integer.toString(numConts));
            messageMap.put("errConts", Integer.toString(errConts));
            errMsg = UtilProperties.getMessage(resource, "contentevents.keyword_creation_complete_for_contents_with_errors", messageMap, UtilHttp.getLocale(request));
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
    }
    
    /**
     * 删除二维码白边 Add By AlexYao 2016/01/31
     *
     * @param matrix
     * @return
     */
    public static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 11;
        int resHeight = rec[3] + 11;
        
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1])) {
                    resMatrix.set(i + 5, j + 5);
                }
            }
        }
        return resMatrix;
    }
    
    /**
     * 生成二维码并保存  Add By AlexYao 2016/01/31
     *
     * @param dispatcher
     * @param delegator
     * @param partyId
     * @param text
     * @return
     * @throws Exception
     */
    public static String getQRCode(LocalDispatcher dispatcher, Delegator delegator, String partyId, String text) throws WriterException, GenericEntityException, IOException {
        int width = 200;
        int height = 200;
        String format = "png";
        Hashtable hints = new Hashtable();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix bitMatrix = deleteWhite(new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints));
        
        
        List<GenericValue> userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId));
        Map<String, Object> context;
        FastMap.newInstance();
        try {
            context = dispatcher.runSync("uploadQRCode", null);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return "error";
        }
        String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.QRCode.path"), context);
        String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.QRCode.url"), context);
        imageServerPath += fileSeparator + partyId;  //以partyId命名文件夹
        imageServerUrl += fileSeparator + partyId;  //以partyId命名文件夹
        String rootTargetDirectory = imageServerPath;
        File rootTargetDir = new File(rootTargetDirectory);
        if (!rootTargetDir.exists()) {
            boolean created = rootTargetDir.mkdirs();
            if (!created) {
                String errMsg = "Cannot create the target directory";
                Debug.logFatal(errMsg, module);
                return "error";
            }
        }
        Map<String, Object> contentCtx = FastMap.newInstance();
        contentCtx.put("contentTypeId", "DOCUMENT");
        contentCtx.put("userLogin", userLogin.get(0));
        Map<String, Object> contentResult = FastMap.newInstance();
        try {
            contentResult = dispatcher.runSync("createContent", contentCtx);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return "error";
        }
        
        String contentId = (String) contentResult.get("contentId");
        
        // File to use for original image
        
        String fileContentType = "image/png";
        
        // Create folder product id.
        String targetDirectory = imageServerPath;
        File targetDir = new File(targetDirectory);
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (!created) {
                String errMsg = "Cannot create the target directory";
                Debug.logFatal(errMsg, module);
                return "error";
            }
        }
        String uploadFileName = System.currentTimeMillis() + ".png";
        File file = new File(imageServerPath + fileSeparator + uploadFileName);
        String imageName = null;
        imagePath = imageServerPath + fileSeparator + uploadFileName;
        file = checkExistsImage(file);
        if (UtilValidate.isNotEmpty(file)) {
            imageName = file.getPath();
            imageName = imageName.substring(imageName.lastIndexOf(fileSeparator) + 1);
        }
        
        String imageUrl = new File(imageServerUrl + fileSeparator + imageName).getPath().replaceAll("\\\\", "/");
        
        
        File outputFile = new File(imageServerPath + fileSeparator + imageName);
        MatrixToImageWriter.writeToFile(bitMatrix, format, outputFile);
        createContentAndDataResource(dispatcher, delegator, userLogin.get(0), uploadFileName, imageUrl, contentId, fileContentType);
        return contentId;
    }
    
    /**
     * 128条形码
     *
     * @param dispatcher
     * @param delegator
     * @param partyId
     * @param strBarCode 条形码：0-100位
     * @param dimension  商品条形码：尺寸
     * @param barheight  商品条形码：高度
     * @return 图片ID
     */
    public static String generateBarCode128(LocalDispatcher dispatcher, Delegator delegator, String partyId, String strBarCode, String dimension, String barheight) {
        
        
        try {
            BufferedImage bi = null;
            int len = strBarCode.length();
            JBarcode productBarcode = new JBarcode(Code128Encoder.getInstance(),
                    WidthCodedPainter.getInstance(),
                    EAN13TextPainter.getInstance());
            
            // 尺寸，面积，大小 密集程度
            productBarcode.setXDimension(Double.valueOf(dimension).doubleValue());
            // 高度 10.0 = 1cm 默认1.5cm
            productBarcode.setBarHeight(Double.valueOf(barheight).doubleValue());
            // 宽度
            productBarcode.setWideRatio(Double.valueOf(30).doubleValue());
//                  是否显示字体
            productBarcode.setShowText(true);
//                 显示字体样式
            productBarcode.setTextPainter(BaseLineTextPainter.getInstance());
            
            // 生成二维码
            bi = productBarcode.createBarcode(strBarCode);
            
            List<GenericValue> userLogin = delegator.findByAnd("UserLogin", UtilMisc.toMap("partyId", partyId));
            Map<String, Object> context;
            FastMap.newInstance();
            try {
                context = dispatcher.runSync("uploadQRCode", null);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return "error";
            }
            String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.QRCode.path"), context);
            String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.QRCode.url"), context);
            imageServerPath += fileSeparator + partyId;  //以partyId命名文件夹
            imageServerUrl += fileSeparator + partyId;  //以partyId命名文件夹
            String rootTargetDirectory = imageServerPath;
            File rootTargetDir = new File(rootTargetDirectory);
            if (!rootTargetDir.exists()) {
                boolean created = rootTargetDir.mkdirs();
                if (!created) {
                    String errMsg = "Cannot create the target directory";
                    Debug.logFatal(errMsg, module);
                    return "error";
                }
            }
            Map<String, Object> contentCtx = FastMap.newInstance();
            contentCtx.put("contentTypeId", "DOCUMENT");
            contentCtx.put("userLogin", userLogin.get(0));
            Map<String, Object> contentResult = FastMap.newInstance();
            try {
                contentResult = dispatcher.runSync("createContent", contentCtx);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
                return "error";
            }
            
            String contentId = (String) contentResult.get("contentId");
            
            // File to use for original image
            
            String fileContentType = "image/png";
            
            // Create folder product id.
            String targetDirectory = imageServerPath;
            File targetDir = new File(targetDirectory);
            if (!targetDir.exists()) {
                boolean created = targetDir.mkdirs();
                if (!created) {
                    String errMsg = "Cannot create the target directory";
                    Debug.logFatal(errMsg, module);
                    return "error";
                }
            }
            String uploadFileName = System.currentTimeMillis() + ".png";
            File file = new File(imageServerPath + fileSeparator + uploadFileName);
            String imageName = null;
            imagePath = imageServerPath + fileSeparator + uploadFileName;
            file = checkExistsImage(file);
            if (UtilValidate.isNotEmpty(file)) {
                imageName = file.getPath();
                imageName = imageName.substring(imageName.lastIndexOf(fileSeparator) + 1);
            }
            
            String imageUrl = new File(imageServerUrl + fileSeparator + imageName).getPath().replaceAll("\\\\", "/");
            
            
            File outputFile = new File(imageServerPath + fileSeparator + imageName);
            ImageIO.write(bi, "jpg", outputFile);
            createContentAndDataResource(dispatcher, delegator, userLogin.get(0), uploadFileName, imageUrl, contentId, fileContentType);
            return contentId;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "encodeError";
        }
    }
    
    /**
     * 创建dataresource Add By AlexYao 2016/01/31
     *
     * @param dispatcher
     * @param delegator
     * @param userLogin
     * @param filenameToUse
     * @param imageUrl
     * @param contentId
     * @param fileContentType
     * @return
     */
    public static Map<String, Object> createContentAndDataResource(LocalDispatcher dispatcher, Delegator delegator, GenericValue userLogin, String filenameToUse, String imageUrl, String contentId, String fileContentType) {
        Map<String, Object> result = FastMap.newInstance();
        
        Map<String, Object> dataResourceCtx = FastMap.newInstance();
        
        dataResourceCtx.put("objectInfo", imageUrl);
        dataResourceCtx.put("dataResourceName", filenameToUse);
        dataResourceCtx.put("userLogin", userLogin);
        dataResourceCtx.put("dataResourceTypeId", "IMAGE_OBJECT");
        dataResourceCtx.put("mimeTypeId", fileContentType);
        dataResourceCtx.put("isPublic", "Y");
        
        Map<String, Object> dataResourceResult = FastMap.newInstance();
        try {
            dataResourceResult = dispatcher.runSync("createDataResource", dataResourceCtx);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        String dataResourceId = (String) dataResourceResult.get("dataResourceId");
        result.put("dataResourceFrameId", dataResourceId);
        result.put("dataResourceId", dataResourceId);
        
        Map<String, Object> contentUp = FastMap.newInstance();
        contentUp.put("contentId", contentId);
        contentUp.put("dataResourceId", dataResourceResult.get("dataResourceId"));
        contentUp.put("contentName", filenameToUse);
        contentUp.put("userLogin", userLogin);
        try {
            dispatcher.runSync("updateContent", contentUp);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        GenericValue content = null;
        try {
            content = delegator.findOne("Content", UtilMisc.toMap("contentId", contentId), false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        
        if (content != null) {
            GenericValue dataResource = null;
            try {
                dataResource = content.getRelatedOne("DataResource");
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
            
            if (dataResource != null) {
                dataResourceCtx.put("dataResourceId", dataResource.getString("dataResourceId"));
                try {
                    dispatcher.runSync("updateDataResource", dataResourceCtx);
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        return result;
    }
    
    /**
     * 上传头像  Add By AlexYao
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String appHeadUploadImage(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //对request进行封装，requestContext提供了对request多个访问方法
        RequestContext requestContext = new ServletRequestContext(request);
        //判断表单是否是Multipart类型的。这里可以直接对request进行判断
        if (FileUpload.isMultipartContent(requestContext)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            
            ServletFileUpload upload = new ServletFileUpload(factory);
            //设置上传文件大小的上限， -1 表示无上限
            upload.setFileSizeMax(100000 * 1024 * 1024);
            List<FileItem> items;
            try {
                // 上传文件，并解析出所有的表单字段，包括普通字段和文件字段
                items = upload.parseRequest(requestContext);
            } catch (FileUploadException e1) {
                request.setAttribute("error", "文件上传发生错误" + e1.getMessage());
                return "error";
            }
            String userLoginId = (String) request.getAttribute("userLoginId");
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            // 下面对每个字段进行处理，分普通字段和文件字段
            Iterator<FileItem> it = items.iterator();
            while (it.hasNext()) {
                DiskFileItem fileItem = (DiskFileItem) it.next();
                // 如果是普通字段
                if (fileItem.isFormField()) {
                } else {
                    Map<String, Object> context;
                    try {
                        context = dispatcher.runSync("uploadQRCode", null);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    String imageServerPath = null;
                    String imageServerUrl = null;
                    imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyHeadImage.path"), context);
                    imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyHeadImage.url"), context);
                    String rootTargetDirectory = imageServerPath;
                    File rootTargetDir = new File(rootTargetDirectory);
                    if (!rootTargetDir.exists()) {
                        boolean created = rootTargetDir.mkdirs();
                        if (!created) {
                            String errMsg = "Cannot create the target directory";
                            Debug.logFatal(errMsg, module);
                            return "error";
                        }
                    }
                    
                    Map<String, Object> contentCtx = FastMap.newInstance();
                    contentCtx.put("contentTypeId", "PERSON");
                    contentCtx.put("userLogin", userLogin);
                    Map<String, Object> contentResult = FastMap.newInstance();
                    try {
                        contentResult = dispatcher.runSync("createContent", contentCtx);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    
                    String contentId = (String) contentResult.get("contentId");
                    
                    String uploadFileName = fileItem.getName();
                    String fileContentType = fileItem.getContentType();
                    
                    Long time = System.currentTimeMillis();
                    String filePath = imageServerPath + fileSeparator + time.toString() + uploadFileName.substring(uploadFileName.lastIndexOf("."));
                    File file = new File(filePath);
                    String imageName = null;
                    String oldImageName = uploadFileName;
                    file = checkExistsImage(file);
                    if (UtilValidate.isNotEmpty(file)) {
                        imageName = file.getPath();
                        imageName = imageName.substring(imageName.lastIndexOf(fileSeparator) + 1);
                    }
                    //传入的不是简单的字符串，而是图片，音频，视频等二进制文件
                    // 保存文件，其实就是把缓存里的数据写到目标路径下
                    if (fileItem.getName() != null && fileItem.getSize() != 0) {
                        OutputStream out = new FileOutputStream(file);
                        
                        InputStream in = fileItem.getInputStream();
                        int length = 0;
                        byte[] buf = new byte[1024];
                        while ((length = in.read(buf)) != -1) {
                            out.write(buf, 0, length);
                        }
                        in.close();
                        out.close();
                        
                        //绘制缩小图 386*386
                        String newFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "_small" + filePath.substring(filePath.lastIndexOf("."));
                        try {
                            ImgCompress.compress(filePath, newFilePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        request.setAttribute("error", "文件没有选择 或 文件内容为空");
 
                        return "error";
                    }
                    String imageUrl = new File(imageServerUrl + fileSeparator + imageName).getPath().replaceAll("\\\\", "/");
                    createContentAndDataResource(dispatcher, delegator, userLogin, oldImageName, imageUrl, contentId, fileContentType);
                    GenericValue imgGroupAssoc = delegator.makeValue("ImgGroupAssoc");
                    imgGroupAssoc.set("imgGroupAssocId", delegator.getNextSeqId("ImgGroupAssoc"));
                    imgGroupAssoc.set("imgGroupId", "PRIVATE_IMG_GROUP");
                    imgGroupAssoc.set("contentId", contentId);
                    delegator.create(imgGroupAssoc);
                    //更新头像
                    GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.get("partyId")));
                    GenericValue content = null;
                    try {
                        content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", contentId));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(content)) {
                        GenericValue dataResource = null;
                        try {
                            dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        if (UtilValidate.isNotEmpty(dataResource)) {
                            String imgUrl = request.getAttribute("_SERVER_ROOT_URL_") + dataResource.getString("objectInfo");
                            String smallImgUrl = imgUrl.substring(0, imgUrl.lastIndexOf(".")) + "_small" + imgUrl.substring(imgUrl.lastIndexOf("."));
                            person.set("headphoto", imgUrl);
                            delegator.store(person);
                            request.setAttribute("imgUrl", imgUrl);
                            request.setAttribute("smallImgUrl", smallImgUrl);
                            request.setAttribute("success", "上传成功");
                        }
                    }
                }
            }
        }
        
        return "error";
    }
    
    public static String appHeadUploadImage1(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String accessToken = (String) request.getAttribute("accessToken");
        String mediaId = (String) request.getAttribute("mediaId");
        
        Map<String, Object> result = FastMap.newInstance();
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getAttribute("userLogin");
        String contentTypeId = (String) request.getAttribute("contentTypeId");
        Map<String, Object> context;
        try {
            context = dispatcher.runSync("uploadQRCode", null);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return "error";
        }
        String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyReviewImage.path"), context);
        String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyReviewImage.url"), context);
        String rootTargetDirectory = imageServerPath;
        File rootTargetDir = new File(rootTargetDirectory);
        if (!rootTargetDir.exists()) {
            boolean created = rootTargetDir.mkdirs();
            if (!created) {
                String errMsg = "Cannot create the target directory";
                Debug.logFatal(errMsg, module);
                return "error";
            }
        }
        Map<String, Object> contentCtx = FastMap.newInstance();
        contentCtx.put("contentTypeId", contentTypeId);
        contentCtx.put("userLogin", userLogin);
        Map<String, Object> contentResult = FastMap.newInstance();
        try {
            contentResult = dispatcher.runSync("createContent", contentCtx);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return "error";
        }
        
        String contentId = (String) contentResult.get("contentId");
        result.put("contentFrameId", contentId);
        result.put("contentId", contentId);
        
        // Create folder product id.
        String targetDirectory = imageServerPath;
        File targetDir = new File(targetDirectory);
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (!created) {
                String errMsg = "Cannot create the target directory";
                Debug.logFatal(errMsg, module);
                return "error";
            }
        }
        
        
        String requestUrl = "http://api.weixin.qq.com/cgi-bin/media/get?access_token=" + accessToken + "&media_id=" + mediaId;
//        System.out.println(requestUrl);
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        
        // 根据内容类型获取扩展名
        String fileContentType = conn.getHeaderField("Content-Type");
        if ("image/pjpeg".equals(fileContentType)) {
            fileContentType = "image/jpeg";
        } else if ("image/x-png".equals(fileContentType)) {
            fileContentType = "image/png";
        }
        //获取文件名
        String uploadFileName = conn.getHeaderField("Content-disposition").split("filename=")[1].replaceAll("\"", "");
        // 将mediaId作为文件名
        String filePath = imageServerPath + fileSeparator + uploadFileName;
        File file = new File(filePath);
        String imageName = null;
        imagePath = imageServerPath + fileSeparator + uploadFileName;
        file = checkExistsImage(file);
        if (UtilValidate.isNotEmpty(file)) {
            imageName = file.getPath();
            imageName = imageName.substring(imageName.lastIndexOf(fileSeparator) + 1);
        }
        
        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buf = new byte[8096000];
        int size = 0;
        while ((size = bis.read(buf)) != -1) {
            fos.write(buf, 0, size);
        }
        fos.close();
        bis.close();
        
        //绘制缩小图 386*386
        String newFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "_small" + filePath.substring(filePath.lastIndexOf("."));
        try {
            ImgCompress.compress(filePath, newFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        conn.disconnect();
//        String info = String.format("下载媒体文件成功，imagePath=" + imageServerUrl + fileSeparator + imageName);
//        System.out.println(info);
        
        String imageUrl = new File(imageServerUrl + fileSeparator + imageName).getPath().replaceAll("\\\\", "/");
        createContentAndDataResource(dispatcher, delegator, userLogin, imageName, imageUrl, contentId, fileContentType);
        GenericValue imgGroupAssoc = delegator.makeValue("ImgGroupAssoc");
        imgGroupAssoc.set("imgGroupAssocId", delegator.getNextSeqId("ImgGroupAssoc"));
        imgGroupAssoc.set("imgGroupId", "PRIVATE_IMG_GROUP");
        imgGroupAssoc.set("contentId", contentId);
        delegator.create(imgGroupAssoc);
        //更新头像
        GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.get("partyId")));
        GenericValue content = null;
        try {
            content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", contentId));
        } catch (GenericEntityException e) {
            e.printStackTrace();
        }
        if (UtilValidate.isNotEmpty(content)) {
            GenericValue dataResource = null;
            try {
                dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
            } catch (GenericEntityException e) {
                e.printStackTrace();
            }
            if (UtilValidate.isNotEmpty(dataResource)) {
                String imgUrl = request.getAttribute("_SERVER_ROOT_URL_") + dataResource.getString("objectInfo");
                String smallImgUrl = imgUrl.substring(0, imgUrl.lastIndexOf(".")) + "_small" + imgUrl.substring(imgUrl.lastIndexOf("."));
                person.set("headphoto", imgUrl);
                delegator.store(person);
                request.setAttribute("imgUrl", imgUrl);
                request.setAttribute("smallImgUrl", smallImgUrl);
                request.setAttribute("success", "上传成功");
            }
        }
        
        return "error";
    }
    
    /**
     * 修改用户信息
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     * @throws GenericServiceException
     */
    public static String editUserInfo(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException, GenericServiceException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //对request进行封装，requestContext提供了对request多个访问方法
        RequestContext requestContext = new ServletRequestContext(request);
        //判断表单是否是Multipart类型的。这里可以直接对request进行判断
        if (FileUpload.isMultipartContent(requestContext)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            
            ServletFileUpload upload = new ServletFileUpload(factory);
            //设置上传文件大小的上限， -1 表示无上限
            upload.setFileSizeMax(100000 * 1024 * 1024);
            List<FileItem> items;
            try {
                // 上传文件，并解析出所有的表单字段，包括普通字段和文件字段
                items = upload.parseRequest(requestContext);
            } catch (FileUploadException e1) {
                request.setAttribute("error", "文件上传发生错误" + e1.getMessage());
                return "error";
            }
            String userLoginId = (String) request.getAttribute("userLoginId");
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            // 下面对每个字段进行处理，分普通字段和文件字段
            Iterator<FileItem> it = items.iterator();
            String name = null;
            String nickname = null;
            String email = null;
            String gender = null;
            String idNumber = null;
            String drivingLicence = null;
            String headphoto = null;
            while (it.hasNext()) {
                DiskFileItem fileItem = (DiskFileItem) it.next();
                // 如果是普通字段
                if (fileItem.isFormField()) {
                    if (UtilValidate.areEqual("name", fileItem.getFieldName())) {
                        name = fileItem.getString("UTF-8");
                    }
                    if (UtilValidate.areEqual("nickname", fileItem.getFieldName())) {
                        nickname = fileItem.getString("UTF-8");
                    }
                    if (UtilValidate.areEqual("email", fileItem.getFieldName())) {
                        email = fileItem.getString("UTF-8");
                    }
                    if (UtilValidate.areEqual("gender", fileItem.getFieldName())) {
                        gender = fileItem.getString("UTF-8");
                    }
                    if (UtilValidate.areEqual("idNumber", fileItem.getFieldName())) {
                        idNumber = fileItem.getString("UTF-8");
                    }
                    if (UtilValidate.areEqual("drivingLicence", fileItem.getFieldName())) {
                        drivingLicence = fileItem.getString("UTF-8");
                    }
                } else {
                    Map<String, Object> context;
                    try {
                        context = dispatcher.runSync("uploadQRCode", null);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    String imageServerPath = null;
                    String imageServerUrl = null;
                    imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyHeadImage.path"), context);
                    imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyHeadImage.url"), context);
                    String rootTargetDirectory = imageServerPath;
                    File rootTargetDir = new File(rootTargetDirectory);
                    if (!rootTargetDir.exists()) {
                        boolean created = rootTargetDir.mkdirs();
                        if (!created) {
                            String errMsg = "Cannot create the target directory";
                            Debug.logFatal(errMsg, module);
                            return "error";
                        }
                    }
                    
                    Map<String, Object> contentCtx = FastMap.newInstance();
                    contentCtx.put("contentTypeId", "PERSON");
                    contentCtx.put("userLogin", userLogin);
                    Map<String, Object> contentResult = FastMap.newInstance();
                    try {
                        contentResult = dispatcher.runSync("createContent", contentCtx);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    
                    String contentId = (String) contentResult.get("contentId");
                    
                    String uploadFileName = fileItem.getName();
                    String fileContentType = fileItem.getContentType();
                    
                    Long time = System.currentTimeMillis();
                    String filePath = imageServerPath + fileSeparator + time.toString() + uploadFileName.substring(uploadFileName.lastIndexOf("."));
                    File file = new File(filePath);
                    String imageName = null;
                    String oldImageName = uploadFileName;
                    file = checkExistsImage(file);
                    if (UtilValidate.isNotEmpty(file)) {
                        imageName = file.getPath();
                        imageName = imageName.substring(imageName.lastIndexOf(fileSeparator) + 1);
                    }
                    //传入的不是简单的字符串，而是图片，音频，视频等二进制文件
                    // 保存文件，其实就是把缓存里的数据写到目标路径下
                    if (fileItem.getName() != null && fileItem.getSize() != 0) {
                        OutputStream out = new FileOutputStream(file);
                        
                        InputStream in = fileItem.getInputStream();
                        int length = 0;
                        byte[] buf = new byte[1024];
                        while ((length = in.read(buf)) != -1) {
                            out.write(buf, 0, length);
                        }
                        in.close();
                        out.close();
                        
                        //绘制缩小图 386*386
                        String newFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "_small" + filePath.substring(filePath.lastIndexOf("."));
                        try {
                            ImgCompress.compress(filePath, newFilePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        request.setAttribute("error", "文件没有选择 或 文件内容为空");
                        return "error";
                    }
                    String imageUrl = new File(imageServerUrl + fileSeparator + imageName).getPath().replaceAll("\\\\", "/");
                    createContentAndDataResource(dispatcher, delegator, userLogin, oldImageName, imageUrl, contentId, fileContentType);
                    GenericValue content = null;
                    try {
                        content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", contentId));
                    } catch (GenericEntityException e) {
                        e.printStackTrace();
                    }
                    if (UtilValidate.isNotEmpty(content)) {
                        GenericValue dataResource = null;
                        try {
                            dataResource = delegator.findByPrimaryKey("DataResource", UtilMisc.toMap("dataResourceId", content.get("dataResourceId")));
                        } catch (GenericEntityException e) {
                            e.printStackTrace();
                        }
                        if (UtilValidate.isNotEmpty(dataResource)) {
                            headphoto = request.getAttribute("_SERVER_ROOT_URL_") + dataResource.getString("objectInfo");
                        }
                    }
                    GenericValue imgGroupAssoc = delegator.makeValue("ImgGroupAssoc");
                    imgGroupAssoc.set("imgGroupAssocId", delegator.getNextSeqId("ImgGroupAssoc"));
                    imgGroupAssoc.set("imgGroupId", "PRIVATE_IMG_GROUP");
                    imgGroupAssoc.set("contentId", contentId);
                    delegator.create(imgGroupAssoc);
                }
            }
            //更新头像
            GenericValue person = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", userLogin.get("partyId")));
            Map<String, Object> paramsCRM = FastMap.newInstance(); // 保存CRM修改参数
            if (UtilValidate.isNotEmpty(name)) {
                person.set("name", name);
                paramsCRM.put("realName", name);
            }
            if (UtilValidate.isNotEmpty(nickname)) {
                person.set("nickname", nickname);
                paramsCRM.put("nick", nickname);
            }
            if (UtilValidate.isNotEmpty(email)) {
                person.set("email", email);
                paramsCRM.put("mail", email);
            }
            if (UtilValidate.isNotEmpty(gender)) {
                person.set("gender", gender);
                String sex = "0";
                if ("S".equals(gender)) {
                    person.set("gender", null);
                    sex = "2";
                } else {
                    person.set("gender", gender);
                    if ("M".equals(gender)) {
                        sex = "1";
                    } else {
                        sex = "0";
                    }
                }
                paramsCRM.put("gender", sex);
            }
            if (UtilValidate.isNotEmpty(idNumber)) {
                person.set("idNumber", idNumber);
                paramsCRM.put("passportNo", idNumber);
            }
            if (UtilValidate.isNotEmpty(drivingLicence)) {
                person.set("drivingLicence", drivingLicence);
                paramsCRM.put("drivingLicence", drivingLicence);
            }
            if (UtilValidate.isNotEmpty(headphoto)) {
                person.set("headphoto", headphoto);
                paramsCRM.put("picUrl", headphoto);
            }
            delegator.store(person);// 用户完善资料行为
            // ===============ICO修改成功之后，同步给CRM begin spj==================
            paramsCRM.put("productBrandId", "1");
            paramsCRM.put("channelId", "1");
            
            paramsCRM.put("custId", userLogin.getString("custId"));
            // 将接口加入业务中，调试放开
            dispatcher.runSync("updateCustInfoCrm02", paramsCRM);
            // 用户完善资料行为
            dispatcher.runSync("addActivityInfoCrm21", UtilMisc.<String, Object>toMap("productBrandId", "1", "custId", userLogin.getString("custId"), "activityType", "6", "createTime", UtilDateTime.nowTimestamp(), "sourceId", "1"));
            // ===============ICO修改成功之后，同步给CRM end spj==================
            request.setAttribute("success", "修改成功");
            return "success";
        }
        
        request.setAttribute("error", "修改失败");
        response.setStatus(403);
        return "error";
    }
    
    /**
     * 创建意见反馈
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String appFeedBackUploadImage(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        //对request进行封装，requestContext提供了对request多个访问方法
        RequestContext requestContext = new ServletRequestContext(request);
        //判断表单是否是Multipart类型的。这里可以直接对request进行判断
        if (FileUpload.isMultipartContent(requestContext)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            
            ServletFileUpload upload = new ServletFileUpload(factory);
            //设置上传文件大小的上限， -1 表示无上限
            upload.setFileSizeMax(100000 * 1024 * 1024);
            List<FileItem> items;
            try {
                // 上传文件，并解析出所有的表单字段，包括普通字段和文件字段
                items = upload.parseRequest(requestContext);
            } catch (FileUploadException e1) {
                request.setAttribute("error", "文件上传发生错误" + e1.getMessage());
                return "error";
            }
            String userLoginId = (String) request.getAttribute("userLoginId");
            GenericValue userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
            // 下面对每个字段进行处理，分普通字段和文件字段
            Iterator<FileItem> it = items.iterator();
            String feedbackContent = null;
            String contactMethod = null;
            List<String> contentIds = FastList.newInstance();
            while (it.hasNext()) {
                DiskFileItem fileItem = (DiskFileItem) it.next();
                // 如果是普通字段
                if (fileItem.isFormField()) {
                    if (UtilValidate.areEqual("feedbackContent", fileItem.getFieldName())) {
                        feedbackContent = fileItem.getString("UTF-8");
                    }
                    if (UtilValidate.areEqual("contactMethod", fileItem.getFieldName())) {
                        contactMethod = fileItem.getString("UTF-8");
                    }
                } else {
                    Map<String, Object> context;
                    try {
                        context = dispatcher.runSync("uploadQRCode", null);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    String imageServerPath = null;
                    String imageServerUrl = null;
                    imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyComplainImage.path"), context);
                    imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyComplainImage.url"), context);
                    String rootTargetDirectory = imageServerPath;
                    File rootTargetDir = new File(rootTargetDirectory);
                    if (!rootTargetDir.exists()) {
                        boolean created = rootTargetDir.mkdirs();
                        if (!created) {
                            String errMsg = "Cannot create the target directory";
                            Debug.logFatal(errMsg, module);
                            return "error";
                        }
                    }
                    
                    Map<String, Object> contentCtx = FastMap.newInstance();
                    contentCtx.put("contentTypeId", "PERSON");
                    contentCtx.put("userLogin", userLogin);
                    Map<String, Object> contentResult = FastMap.newInstance();
                    try {
                        contentResult = dispatcher.runSync("createContent", contentCtx);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    
                    String contentId = (String) contentResult.get("contentId");
                    
                    String uploadFileName = fileItem.getName();
                    String fileContentType = fileItem.getContentType();
                    
                    Long time = System.currentTimeMillis();
                    String filePath = imageServerPath + fileSeparator + time.toString() + uploadFileName.substring(uploadFileName.lastIndexOf("."));
                    File file = new File(filePath);
                    String imageName = null;
                    String oldImageName = uploadFileName;
                    file = checkExistsImage(file);
                    if (UtilValidate.isNotEmpty(file)) {
                        imageName = file.getPath();
                        imageName = imageName.substring(imageName.lastIndexOf(fileSeparator) + 1);
                    }
                    //传入的不是简单的字符串，而是图片，音频，视频等二进制文件
                    // 保存文件，其实就是把缓存里的数据写到目标路径下
                    if (fileItem.getName() != null && fileItem.getSize() != 0) {
                        OutputStream out = new FileOutputStream(file);
                        
                        InputStream in = fileItem.getInputStream();
                        int length = 0;
                        byte[] buf = new byte[1024];
                        while ((length = in.read(buf)) != -1) {
                            out.write(buf, 0, length);
                        }
                        in.close();
                        out.close();
                        
                        //绘制缩小图 386*386
                        String newFilePath = filePath.substring(0, filePath.lastIndexOf(".")) + "_small" + filePath.substring(filePath.lastIndexOf("."));
                        try {
                            ImgCompress.compress(filePath, newFilePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        request.setAttribute("error", "文件没有选择 或 文件内容为空");
                        return "error";
                    }
                    String imageUrl = new File(imageServerUrl + fileSeparator + imageName).getPath().replaceAll("\\\\", "/");
                    createContentAndDataResource(dispatcher, delegator, userLogin, oldImageName, imageUrl, contentId, fileContentType);
                    GenericValue imgGroupAssoc = delegator.makeValue("ImgGroupAssoc");
                    imgGroupAssoc.set("imgGroupAssocId", delegator.getNextSeqId("ImgGroupAssoc"));
                    imgGroupAssoc.set("imgGroupId", "PRIVATE_IMG_GROUP");
                    imgGroupAssoc.set("contentId", contentId);
                    delegator.create(imgGroupAssoc);
                    contentIds.add(contentId);
                }
            }
            if (UtilValidate.isEmpty(feedbackContent)) {
                request.setAttribute("error", "反馈内容不能为空");
                response.setStatus(403);
                return "error";
            }
            if (UtilValidate.isEmpty(contactMethod)) {
                request.setAttribute("error", "联系方式不能为空");
                response.setStatus(403);
                return "error";
            }
            List<GenericValue> toBeSaved = new LinkedList<GenericValue>();
            String partyId = userLogin.getString("partyId");
            // 创建反馈信息
            GenericValue feedback = delegator.makeValue("Feedback");
            String feedbackId = delegator.getNextSeqId("Feedback");
            feedback.set("feedbackId", feedbackId);
            feedback.set("createPartyId", partyId);
            feedback.set("createDate", UtilDateTime.nowTimestamp());
            feedback.set("feedbackContent", feedbackContent);
            feedback.set("contactMethod", contactMethod);
            toBeSaved.add(feedback);
            // 创建图片关联关系
            if (contentIds != null) {
                for (String contentId : contentIds) {
                    GenericValue feedback_Content = delegator.makeValue("FeedbackContent");
                    feedback_Content.set("feedbackId", feedbackId);
                    feedback_Content.set("contentId", contentId);
                    toBeSaved.add(feedback_Content);
                }
            }
            delegator.storeAll(toBeSaved);
            request.setAttribute("feedbackId", feedbackId);
            request.setAttribute("success", "创建成功");
            return "success";
        }
        request.setAttribute("error", "提交数据不能为空");
        response.setStatus(403);
        return "error";
    }
    
    /**
     * 实物退款上传图片  Add By AlexYao
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String appRefundUploadImage(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        
        // TODO Auto-generated method stub
        //对request进行封装，requestContext提供了对request多个访问方法
        RequestContext requestContext = new ServletRequestContext(request);
        //判断表单是否是Multipart类型的。这里可以直接对request进行判断
        if (FileUpload.isMultipartContent(requestContext)) {
            DiskFileItemFactory factory = new DiskFileItemFactory();
            
            ServletFileUpload upload = new ServletFileUpload(factory);
            //设置上传文件大小的上限， -1 表示无上限
            upload.setFileSizeMax(100000 * 1024 * 1024);
            List<FileItem> items;
            try {
                // 上传文件，并解析出所有的表单字段，包括普通字段和文件字段
                items = upload.parseRequest(requestContext);
            } catch (FileUploadException e1) {
                
                return "error";
            }
            String userLoginId = "";
            GenericValue userLogin = null;
            // 下面对每个字段进行处理，分普通字段和文件字段
            Iterator<FileItem> it = items.iterator();
            while (it.hasNext()) {
                DiskFileItem fileItem = (DiskFileItem) it.next();
                // 如果是普通字段
                if (fileItem.isFormField()) {
                    userLoginId = fileItem.getString("UTF-8");
                    /** 获取登录信息 */
                    userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
                    if (null == userLogin) {
                        request.setAttribute("status", "fail");
                        return "error";
                    }
                } else {
                    Map<String, Object> context;
                    try {
                        context = dispatcher.runSync("uploadQRCode", null);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    String imageServerPath = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyRefundImage.path"), context);
                    String imageServerUrl = FlexibleStringExpander.expandString(UtilProperties.getPropertyValue("catalog", "image.partyRefundImage.url"), context);
                    String rootTargetDirectory = imageServerPath;
                    File rootTargetDir = new File(rootTargetDirectory);
                    if (!rootTargetDir.exists()) {
                        boolean created = rootTargetDir.mkdirs();
                        if (!created) {
                            String errMsg = "Cannot create the target directory";
                            Debug.logFatal(errMsg, module);
                            return "error";
                        }
                    }
                    
                    Map<String, Object> contentCtx = FastMap.newInstance();
                    contentCtx.put("contentTypeId", "PERSON_REVIEW");
                    contentCtx.put("userLogin", userLogin);
                    Map<String, Object> contentResult = FastMap.newInstance();
                    try {
                        contentResult = dispatcher.runSync("createContent", contentCtx);
                    } catch (GenericServiceException e) {
                        Debug.logError(e, module);
                        return "error";
                    }
                    
                    String contentId = (String) contentResult.get("contentId");
                    result.put("contentFrameId", contentId);
                    result.put("contentId", contentId);
                    
                    String uploadFileName = fileItem.getName();
                    String fileContentType = fileItem.getContentType();
                    
                    Long time = System.currentTimeMillis();
                    File file = new File(imageServerPath + fileSeparator + time.toString() + uploadFileName.substring(uploadFileName.lastIndexOf(".")));
                    String imageName = null;
                    String oldImageName = uploadFileName;
                    imagePath = imageServerPath + fileSeparator + time.toString() + uploadFileName.substring(uploadFileName.lastIndexOf("."));
                    file = checkExistsImage(file);
                    if (UtilValidate.isNotEmpty(file)) {
                        imageName = file.getPath();
                        imageName = imageName.substring(imageName.lastIndexOf(fileSeparator) + 1);
                    }
                    //传入的不是简单的字符串，而是图片，音频，视频等二进制文件
                    // 保存文件，其实就是把缓存里的数据写到目标路径下
                    if (fileItem.getName() != null && fileItem.getSize() != 0) {
                        OutputStream out = new FileOutputStream(file);
                        
                        InputStream in = fileItem.getInputStream();
                        int length = 0;
                        byte[] buf = new byte[1024];
                        while ((length = in.read(buf)) != -1) {
                            out.write(buf, 0, length);
                        }
                        in.close();
                        out.close();
                    } else {
                        
                        return "error";
                    }
                    String imageUrl = new File(imageServerUrl + fileSeparator + imageName).getPath().replaceAll("\\\\", "/");
                    createContentAndDataResource(dispatcher, delegator, userLogin, oldImageName, imageUrl, contentId, fileContentType);
                }
                
            }
        }
        
        request.setAttribute("status", "success");
        request.setAttribute("contentId", result.get("contentId"));
        
        
        return "error";
    }
    
    /**
     * 保存分类导航组
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String saveCategoryNavigation(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String navigationGroupId = request.getParameter("navigationGroupId");
        String isEnabled = request.getParameter("isEnabled");
        String isShowBrand = request.getParameter("isShowBrand");
        String webSiteIds = request.getParameter("webSiteIds");
        String isAllSite = request.getParameter("isAllSite");
        String navigationGroupName = request.getParameter("navigationGroupName");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        if (UtilValidate.isEmpty(navigationGroupId)) {
            navigationGroupId = delegator.getNextSeqId("NavigationGroup");
        } else {
            List<GenericValue> naviSites = delegator.findByAnd("NavigationGroupSite", UtilMisc.toMap("navigationGroupId", navigationGroupId));
            delegator.removeAll(naviSites);
        }
        GenericValue genericValue = delegator.makeValue("NavigationGroup", UtilMisc.toMap("navigationGroupId", navigationGroupId, "isEnable", isEnabled, "isAllSite", isAllSite, "navigationGroupName", navigationGroupName, "isShowBrand", isShowBrand));
        delegator.createOrStore(genericValue);
        if (genericValue != null) {
            if (isAllSite == null) {
                String[] webSiteIdArray = webSiteIds.split(",");
                for (String siteId : webSiteIdArray) {
                    delegator.create("NavigationGroupSite", UtilMisc.toMap("navigationGroupId", navigationGroupId, "siteId", siteId));
                }
            }
        }
        return "success";
    }
    
    /**
     * 删除导航分组
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String delCategoryNavigation(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String navigationGroupIds = request.getParameter("navigationGroupIds");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String[] navigationGroupIdArray = navigationGroupIds.split(",");
        for (String navigationGroupId : navigationGroupIdArray) {
            delegator.removeByAnd("NavigationGroupSite", UtilMisc.toMap("navigationGroupId", navigationGroupId));
            delegator.removeByAnd("NavigationGroup", UtilMisc.toMap("navigationGroupId", navigationGroupId));
        }
        request.setAttribute("status", true);
        return "success";
    }
    
    /**
     * 更新是否启用，是否显示品牌
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String changeNavigationGroup(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String navigationGroupId = request.getParameter("navigationGroupId");
        String type = request.getParameter("type");
        String newStatus = request.getParameter("newStatus");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue genericValue = delegator.findByPrimaryKey("NavigationGroup", UtilMisc.toMap("navigationGroupId", navigationGroupId));
        genericValue.put(type, newStatus);
        genericValue.store();
        return "success";
    }
    
    /**
     * 更新是否启用，是否显示品牌
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String changeNavigation(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        String newStatus = request.getParameter("newStatus");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue genericValue = delegator.findByPrimaryKey("Navigation", UtilMisc.toMap("id", id));
        genericValue.put(type, newStatus);
        genericValue.store();
        return "success";
    }
    
    /**
     * 更新导航商品 add by dongxiao 2016.4.19
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String updateNavigationProduct(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String navigationId = request.getParameter("navigationId");
        String productId = request.getParameter("productId");
        String isEnable = request.getParameter("isEnabled");
        String seq = request.getParameter("seq");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        delegator.create("NavigationProduct", UtilMisc.toMap("id", delegator.getNextSeqId("NavigationProduct"), "productId", productId, "isEnable", isEnable, "seq", seq, "navigationId", navigationId));
        request.setAttribute("navigationId", navigationId);
        return "success";
    }
    
    /**
     * 更新导航品牌 add by dongxiao 2016.4.19
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String updateNavigationBrand(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String navigationId = request.getParameter("navigationId");
        String brandId = request.getParameter("productBrandId");
        String brandImg = request.getParameter("contentId");
        String isEnable = request.getParameter("isEnabled");
        String seq = request.getParameter("seq");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        delegator.create("NavigationBrand", UtilMisc.toMap("id", delegator.getNextSeqId("NavigationBrand"), "productBrandId", brandId, "brandImg", brandImg, "isEnable", isEnable, "seq", seq, "navigationId", navigationId));
        request.setAttribute("navigationId", navigationId);
        return "success";
    }
    
    /**
     * 更新是否启用
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String changeIsEnable(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        String newStatus = request.getParameter("newStatus");
        String entityName = request.getParameter("entityName");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue genericValue = delegator.findByPrimaryKey(entityName, UtilMisc.toMap("id", id));
        genericValue.put(type, newStatus);
        genericValue.store();
        return "success";
    }
    
    /**
     * 删除实体 add by dongxiao 2016.4.20
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String deleteByIds(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String ids = request.getParameter("ids");
        String entityName = request.getParameter("entityName");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            GenericValue g = delegator.findByPrimaryKey(entityName, UtilMisc.toMap("id", id));
            delegator.removeValue(g);
        }
        return "success";
    }
    
    /**
     * 保存实体序号 add by dongxiao 2016.4.20
     *
     * @param request
     * @param response
     * @return
     * @throws GenericEntityException
     * @throws ServletException
     * @throws IOException
     */
    public static String saveSeq(HttpServletRequest request, HttpServletResponse response) throws GenericEntityException, ServletException, IOException {
        String id = request.getParameter("id");
        String entityName = request.getParameter("entityName");
        String seq = request.getParameter("seq");
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        GenericValue g = delegator.findByPrimaryKey(entityName, UtilMisc.toMap("id", id));
        g.setString("seq", seq);
        g.store();
        return "success";
    }
    
}
