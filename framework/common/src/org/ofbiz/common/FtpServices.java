/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.common;

import javolution.util.FastList;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.ofbiz.base.util.*;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.ofbiz.base.util.UtilGenerics.checkList;

/**
 * FTP Services
 */
public class FtpServices {
    
    public final static String module = FtpServices.class.getName();
    public static final String resource = "CommonUiLabels";
    
    public static Map<String, Object> putFile(DispatchContext dctx, Map<String, ?> context) {
        Locale locale = (Locale) context.get("locale");
        Debug.logInfo("[putFile] starting...", module);
        
        InputStream localFile = null;
        String localFileName = (String) context.get("localFilename");
        
        List<String> errorList = FastList.newInstance();
        FTPClient ftp = new FTPClient();
        try {
            Debug.logInfo("[putFile] connecting to: " + context.get("hostname"), module);
            ftp.connect((String) context.get("hostname"));
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                Debug.logInfo("[putFile] Server refused connection", module);
                errorList.add(UtilProperties.getMessage(resource, "CommonFtpConnectionRefused", locale));
            } else {
                String username = (String) context.get("username");
                String password = (String) context.get("password");
                Debug.logInfo("[putFile] logging in: username=" + username + ", password=" + password, module);
                if (!ftp.login(username, password)) {
                    Debug.logInfo("[putFile] login failed", module);
                    errorList.add(UtilProperties.getMessage(resource, "CommonFtpLoginFailure", UtilMisc.toMap("username", username, "password", password), locale));
                } else {
                    String LOCAL_CHARSET = "";
                    if (FTPReply.isPositiveCompletion(ftp.sendCommand("OPTS UTF-8", "ON"))) {
                        LOCAL_CHARSET = "UTF-8";
                        localFileName = new String(localFileName.getBytes("UTF-8"));
                        ftp.setControlEncoding(LOCAL_CHARSET);//中文支持
                        Debug.logInfo("[OPTS UTF-8] is accepted!", module);
                        try {
                            localFile = new FileInputStream(localFileName);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                            Debug.logError(ioe, "[putFile] Problem opening local file:" + localFileName, module);
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonFtpFileCannotBeOpen", locale));
                        }
                    } else {
    
                        try {
                            localFile = new FileInputStream(localFileName);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                            Debug.logError(ioe, "[putFile] Problem opening local file:" + localFileName, module);
                            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonFtpFileCannotBeOpen", locale));
                        }
                    }
                    
                    Boolean binaryTransfer = (Boolean) context.get("binaryTransfer");
                    boolean binary = binaryTransfer != null && binaryTransfer.booleanValue();
                    if (binary) {
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);
                    }
                    
                    Boolean passiveMode = (Boolean) context.get("passiveMode");
                    boolean passive = (passiveMode == null) || passiveMode.booleanValue();
                    if (passive) {
                        ftp.enterLocalPassiveMode();
                    }
                    Debug.logInfo("[putFile] storing local file name as: " + localFileName, module);
                    
                    Debug.logInfo("[putFile] storing local file remotely as: " + context.get("remoteFilename"), module);
                    //如果没有目录创建
                    String remoteDir = (String) context.get("remoteDir");
                    if (createDir(remoteDir, ftp)) {
                        String remoteFileName = (String) context.get("remoteFilename");
                        if (isChinese(remoteFileName)) {
                            remoteFileName = UtilDateTime.nowAsString() + remoteFileName.substring(remoteFileName.lastIndexOf("."));
                        } else {
                        
                        }
                        
                        if (!ftp.storeFile(remoteFileName, localFile)) {
                            Debug.logInfo("[putFile] store was unsuccessful" + ftp.getReplyString(), module);
                            errorList.add(UtilProperties.getMessage(resource, "CommonFtpFileNotSentSuccesfully", UtilMisc.toMap("replyString", ftp.getReplyString()), locale));
                        } else {
                            Debug.logInfo("[putFile] store was successful", module);
                            List<String> siteCommands = checkList(context.get("siteCommands"), String.class);
                            if (siteCommands != null) {
                                for (String command : siteCommands) {
                                    Debug.logInfo("[putFile] sending SITE command: " + command, module);
                                    if (!ftp.sendSiteCommand(command)) {
                                        errorList.add(UtilProperties.getMessage(resource, "CommonFtpSiteCommandFailed", UtilMisc.toMap("command", command, "replyString", ftp.getReplyString()), locale));
                                    }
                                }
                            }
                        }
                    } else {
                        Debug.logInfo("[createDir]  failed", module);
                    }
                }
                ftp.logout();
            }
        } catch (IOException ioe) {
            Debug.logInfo(ioe, "[putFile] caught exception: " + ioe.getMessage(), module);
            errorList.add(UtilProperties.getMessage(resource, "CommonFtpProblemWithTransfer", UtilMisc.toMap("errorString", ioe.getMessage()), locale));
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException dce) {
                    Debug.logWarning(dce, "[putFile] Problem with FTP disconnect", module);
                }
            }
        }
        
        try {
            localFile.close();
        } catch (IOException ce) {
            Debug.logWarning(ce, "[putFile] Problem closing local file", module);
        }
        
        if (errorList.size() > 0) {
            Debug.logError("[putFile] The following error(s) (" + errorList.size() + ") occurred: " + errorList, module);
            return ServiceUtil.returnError(errorList);
        }
        
        Debug.logInfo("[putFile] finished successfully", module);
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> getFile(DispatchContext dctx, Map<String, ?> context) {
        Locale locale = (Locale) context.get("locale");
        String localFilename = (String) context.get("localFilename");
        
        OutputStream localFile = null;
        try {
            localFile = new FileOutputStream(localFilename);
        } catch (IOException ioe) {
            Debug.logError(ioe, "[getFile] Problem opening local file", module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource, "CommonFtpFileCannotBeOpen", locale));
        }
        
        List<String> errorList = FastList.newInstance();
        
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect((String) context.get("hostname"));
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                errorList.add(UtilProperties.getMessage(resource, "CommonFtpConnectionRefused", locale));
            } else {
                String username = (String) context.get("username");
                String password = (String) context.get("password");
                
                if (!ftp.login(username, password)) {
                    errorList.add(UtilProperties.getMessage(resource, "CommonFtpLoginFailure", UtilMisc.toMap("username", username, "password", password), locale));
                } else {
                    Boolean binaryTransfer = (Boolean) context.get("binaryTransfer");
                    boolean binary = binaryTransfer != null && binaryTransfer.booleanValue();
                    if (binary) {
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);
                    }
                    
                    Boolean passiveMode = (Boolean) context.get("passiveMode");
                    boolean passive = passiveMode != null && passiveMode.booleanValue();
                    if (passive) {
                        ftp.enterLocalPassiveMode();
                    }
                    
                    if (!ftp.retrieveFile((String) context.get("remoteFilename"), localFile)) {
                        errorList.add(UtilProperties.getMessage(resource, "CommonFtpFileNotSentSuccesfully", UtilMisc.toMap("replyString", ftp.getReplyString()), locale));
                    }
                }
                ftp.logout();
            }
        } catch (IOException ioe) {
            errorList.add(UtilProperties.getMessage(resource, "CommonFtpProblemWithTransfer", UtilMisc.toMap("errorString", ioe.getMessage()), locale));
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException dce) {
                    Debug.logWarning(dce, "[getFile] Problem with FTP disconnect", module);
                }
            }
        }
        
        try {
            localFile.close();
        } catch (IOException ce) {
            Debug.logWarning(ce, "[getFile] Problem closing local file", module);
        }
        
        if (errorList.size() > 0) {
            Debug.logError("[getFile] The following error(s) (" + errorList.size() + ") occurred: " + errorList, module);
            return ServiceUtil.returnError(errorList);
        }
        
        return ServiceUtil.returnSuccess();
    }
    
    public static Map<String, Object> listDirs(DispatchContext dctx, Map<String, ? extends Object> context) {
        
        Locale locale = (Locale) context.get("locale");
        String localFilename = (String) context.get("localFilename");
        String path = (String) context.get("directory");
        OutputStream localFile = null;
        Map<String, Object> resultData = ServiceUtil.returnSuccess();
    
        List<String> errorList = FastList.newInstance();
    
        FTPClient ftp = new FTPClient();
        try {
            ftp.connect((String) context.get("hostname"));
            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
                errorList.add(UtilProperties.getMessage(resource, "CommonFtpConnectionRefused", locale));
            } else {
                String username = (String) context.get("username");
                String password = (String) context.get("password");
    
                if (!ftp.login(username, password)) {
                    errorList.add(UtilProperties.getMessage(resource, "CommonFtpLoginFailure", UtilMisc.toMap("username", username, "password", password), locale));
                } else {
                    Boolean binaryTransfer = (Boolean) context.get("binaryTransfer");
                    boolean binary = binaryTransfer != null && binaryTransfer.booleanValue();
                    if (binary) {
                        ftp.setFileType(FTP.BINARY_FILE_TYPE);
                    }
        
                    Boolean passiveMode = (Boolean) context.get("passiveMode");
                    boolean passive = passiveMode != null && passiveMode.booleanValue();
                    if (passive) {
                        ftp.enterLocalPassiveMode();
                    }
        
                    //获取文件夹列表
                    FTPFile[] files = ftp.listFiles(path);
                    List<FTPFile> ftpFiles = new ArrayList<>();
                    if (UtilValidate.isNotEmpty(files)) {
                        for (int i = 0; i < files.length; i++) {
                            FTPFile file = files[i];
                            ftpFiles.add(file);
                        }
                    }
                    resultData.put("returnDirs", ftpFiles);
        
                }
                ftp.logout();
            }
        } catch (IOException ioe) {
            errorList.add(UtilProperties.getMessage(resource, "CommonFtpProblemWithTransfer", UtilMisc.toMap("errorString", ioe.getMessage()), locale));
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException dce) {
                    Debug.logWarning(dce, "[getFile] Problem with FTP disconnect", module);
                }
            }
        }
    
    
        if (errorList.size() > 0) {
            Debug.logError("[getFile] The following error(s) (" + errorList.size() + ") occurred: " + errorList, module);
            return ServiceUtil.returnError(errorList);
        }
    
        return resultData;
    }
    
    
    /**
     * 创建目录(有则切换目录，没有则创建目录)
     *
     * @param dir
     * @return
     */
    public static boolean createDir(String dir, FTPClient ftp) {
        if (UtilValidate.isEmpty(dir)) {
            return true;
        }
        String d;
        try {
            //目录编码，解决中文路径问题
            d = new String(dir.toString().getBytes(), "UTF-8");
            d = d.replace("\\", "/");
            
            //尝试切入目录
            if (ftp.changeWorkingDirectory(d)) {
                return true;
            }
    
            String[] arr = d.split("/");
            StringBuffer sbfDir = new StringBuffer();
            //循环生成子目录
            for (String s : arr) {
    
                sbfDir.append("/");
                s = s.replace("\\", "/");
                sbfDir.append(s);
                //目录编码，解决中文路径问题
                d = new String(sbfDir.toString().getBytes(), "UTF-8");
                //尝试切入目录
                
                if (ftp.changeWorkingDirectory(d)) {
                    continue;
                }
                if (!ftp.makeDirectory(d)) {
                    return false;
                }
                
            }
            //将目录切换至指定路径
            return ftp.changeWorkingDirectory(d);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    
    /**
     * 删除起始字符
     *
     * @return
     * @author xxj 2017年4月27日
     */
    public static String trimStart(String str, String trim) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("^(" + trim + ")+", "");
    }
    
    /**
     * 删除末尾字符
     *
     * @return
     * @author xxj 2017年4月27日
     */
    public static String trimEnd(String str, String trim) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("(" + trim + ")+$", "");
    }
    
    
    public static boolean isChinese(String str) {
        if (str == null) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (isChinese(c)) {
                return true;// 有一个中文字符就返回
            }
        }
        return false;
        
    }
    
    // 判断一个字符是否是中文
    public static boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }
}