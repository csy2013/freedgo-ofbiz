package com.qihua.ofbiz.party.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
public class HttpUtil {

	/**
     * 将接口数据转换成json对象
     * @param data
     * @return
     */
    public static JSONObject convertToJSONObject(String data) {
    	JSONObject userData=null;
        if (UtilValidate.isNotEmpty(data)){
            userData = JSONObject.fromObject(data);
            return userData;
        }
        return null;
    }
    
    /**
     * 将接口数据转换成json数组
     * @param data
     * @return
     */
    public static JSONArray convertToJSONArray(String data) {
        if (UtilValidate.isNotEmpty(data)){
            JSONArray houses = JSONArray.fromObject(data);
            return houses;
        }
        return null;
    }
    
    /**
     * 公共POST请求
     *
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws IOException
     */
    public static String post(String url, Map<String, String> headers, Map<String, Object> params) throws IOException {
        /** 定义http请求 */
        CloseableHttpClient client = HttpClients.createDefault();
        /** post请求地址设置 */
        HttpPost post = new HttpPost(url);
        // 设置请求头
        if (headers != null) {
			Set<Entry<String, String>> entrys = headers.entrySet();
			Iterator<Entry<String, String>> its = entrys.iterator();
			Entry<String, String> entry = null;
			while (its.hasNext()) {
				entry = its.next();
				post.addHeader(entry.getKey(), entry.getValue());
			}
		}
        /** 发送内容设置 */
        post.setEntity(new UrlEncodedFormEntity(parseParamToNameValuePair(params), Charset.forName("UTF-8")));
        return getContentFromResponse(client.execute(post));
    }

    
    /**
     * 解析Map传递的参数，使用一个键值对对象BasicNameValuePair保存
     *
     * @param map
     * @return
     */
    public static List<NameValuePair> parseParamToNameValuePair(Map<String, Object> map) {
        /** 定义集合 */
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        /** 遍历Map参数 */
        Set<Entry<String, Object>> entrys = map.entrySet();
		Iterator<Entry<String, Object>> its = entrys.iterator();
		Entry<String, Object> entry = null;
		while (its.hasNext()) {
			entry = its.next();
			list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()!=null?String.valueOf(entry.getValue()):""));
		}
        return list;
    }
    
    
    /**
     * 获取响应消息
     *
     * @param response
     * @return
     * @throws IOException
     */
    public static String getContentFromResponse(HttpResponse response) throws IOException {
        /** 判断是否请求成功，为200时表示成功，其他均问有问题。 */
        if (response.getStatusLine().getStatusCode() == 200){
            /**  通过HttpEntity获得响应流 */
            InputStream inputStream = response.getEntity().getContent();
            return changeInputStream(inputStream, "UTF-8");
        }
        return "";
    }
    
    
    /**
     * 通过消息流转换成字符串消息
     *
     * @param inputStream
     * @param encode
     * @return
     */
    public static String changeInputStream(InputStream inputStream, String encode) {
        /** 定义字节输出流 */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[102400];
        int len = 0;
        String result = "";
        if (inputStream != null){
            try {
                /** 消息流转换字符串 */
                while ((len = inputStream.read(data,0,102400)) != -1)
                {
                    outputStream.write(data, 0, len);
                }
                byte[] lens = outputStream.toByteArray();
//                String result = new String(lens);//result结果显示正常：含中文无乱码
                result = new String(lens, encode);
            } catch (IOException e) {
                Debug.log(e.getMessage());
                return "";
            }
        }
        return result;
    }
    
    
    /**
     * json项目判断是否为空
     * @param data
     * @return
     */
    public static Boolean checkJsonKey(Object data) {
    	Boolean chkFlg=true;
    	if(data instanceof JSONNull){
    		chkFlg=false;
    	}else{
        	if("null".equals(data.toString()) || "NULL".equals(data.toString()) ||data.toString().trim().isEmpty()){
        		chkFlg=false;
        	}
        }
    	return chkFlg;
    }
}

