package com.yuaoq.ofbiz.content.data.ftp;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by changsy on 16/4/26.
 */
public class FileUploadServices {
    
    protected static final String module = FileUploadServices.class.getName();
    
    //设置好账号的ACCESS_KEY和SECRET_KEY
    private static final String userName = UtilProperties.getPropertyValue("content", "content.image.upload.ftp.username");
    private static final String password = UtilProperties.getPropertyValue("content", "content.image.upload.ftp.password");
    private static final String hostname = UtilProperties.getPropertyValue("content", "content.image.upload.ftp.hostname");
    private static final String prefix = UtilProperties.getPropertyValue("content", "content.image.upload.ftp.prefix.path");
    /**
     * 覆盖上传
     *
     * @throws IOException
     */
    public static Map<String, Object> ftpUpload(DispatchContext dcx, Map<String, ? extends Object> context) throws IOException {
        //WINDOWS系统
        Map<String, Object> result = ServiceUtil.returnSuccess();
        String filePath = (String) context.get("filePath");
        filePath = filePath.replace("\\","/");
        filePath = filePath.replace("//","/");
        filePath = filePath.replace("//","/");
        
        String rfile = "";
        String remoteDir = "";
        if(filePath.indexOf("images")!=-1){
            rfile = filePath.substring(filePath.lastIndexOf("/webapp/images/")+("/webapp/").length());
            remoteDir =  rfile.substring(0,rfile.lastIndexOf("/"));
            rfile =  rfile.substring(rfile.lastIndexOf("/")+1);
        }
        
        String remoteFilename = rfile;
        LocalDispatcher dispatcher = dcx.getDispatcher();
        filePath = filePath.replace("/",File.separator);
        remoteDir = remoteDir.replace("/","\\");
        try {
            dispatcher.runSync("ftpPutFile", UtilMisc.toMap("hostname", hostname, "username", userName, "password", password, "localFilename", filePath,"remoteFilename",remoteFilename,"remoteDir",remoteDir,"binaryTransfer",true));
        } catch (GenericServiceException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    
}
