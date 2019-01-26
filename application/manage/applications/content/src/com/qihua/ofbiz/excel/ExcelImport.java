package com.qihua.ofbiz.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;


/**
 * Excel导入service
 * @author 钱进 2015/12/18
 *
 */
public class ExcelImport {
    public static final String module = ExcelImport.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * Excel导入
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> excelImport(DispatchContext dctx, Map<String, ? extends Object> context) {
    	Map<String,Object> result = ServiceUtil.returnSuccess();
    	String returnJson = "";
    	//获取request
    	HttpServletRequest request = (HttpServletRequest) context.get("request");
    	//dispatcher对象
    	LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher"); 
    	//获取校验的xml文件路径
    	String xmlUrl = (String) context.get("xmlUrl");
    	//获取单元格校验方法
    	String validateCellData = context.get("validateCellData") == null ? "validateCell" : (String)context.get("validateCellData");
    	
    	// 是否是车型库的导入
    	String isCarModel=(String)context.get("isCarModel");
    	
        DiskFileItemFactory factory = new DiskFileItemFactory();  
        //最大缓存  
        factory.setSizeThreshold(5*1024);  
        ServletFileUpload upload = new ServletFileUpload(factory);  
        upload.setSizeMax(-1);
        //获取服务器本地路径
        String url = request.getServletContext().getRealPath("");
        String first_url =  url.substring(0,url.lastIndexOf("webapp"));
        //获取配置文件全路径
        String fullUrl = first_url+xmlUrl;
		try {
			//获取所有文件列表  
	        List<FileItem> items = upload.parseRequest(request);
			for (FileItem item : items) {
	        	if(!item.isFormField()){
	        		File xmlFile = new File(fullUrl);
	        		FileInputStream fis = (FileInputStream)item.getInputStream();
	        		ParseExcelUtil peu=null;
//	        		if(UtilValidate.isNotEmpty(isCarModel) && isCarModel.equals("model") ){
	        		    peu = new ParseExcelUtil(fis, xmlFile,validateCellData,dispatcher,request);
//	        		}else{
//	        			peu = new ParseExcelUtil(fis, xmlFile,validateCellData,dispatcher);
//	        		}
	        		
	        		if(UtilValidate.isNotEmpty(peu.getErrorString().toString())){
	    				returnJson = "{\"success\":false,\"errorMsg\":["+peu.getErrorString().toString()+"]}";
	    				result.put("listDatas", new ArrayList());
	    			}else{
	    				returnJson = "{\"success\":true,\"successMsg\":\"导入成功！<br/>共导入"+peu.getListDatas().size()+"条数据！\"}";
	    				result.put("listDatas", peu.getListDatas());
	    			}
	        	}
	        }
		} catch (FileUploadException e) {
			e.printStackTrace();
			returnJson = "{\"success\":false,\"errorMsg\":\"文件读取错误！\"}";
		} catch (IOException e) {
			e.printStackTrace();
			returnJson = "{\"success\":false,\"errorMsg\":\"文件读取错误！\"}";
		}
		result.put("msg", returnJson);
        return result;
    }
}
