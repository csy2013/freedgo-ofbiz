package org.ofbiz.common.phone;

import com.google.gson.Gson;
import javolution.util.FastMap;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HttpContext;
import org.ofbiz.base.util.*;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.widget.fo.FoScreenRenderer;
import org.ofbiz.widget.html.HtmlScreenRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sun.misc.BASE64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * desc:通过调用Http post 的方式调用服务
 * Created by Administrator on 2014/12/25.
 */
public class PhoneHttpServices {
    
    public final static String module = PhoneSoapServices.class.getName();
    
    protected static final HtmlScreenRenderer htmlScreenRenderer = new HtmlScreenRenderer();
    protected static final FoScreenRenderer foScreenRenderer = new FoScreenRenderer();
    public static final String resource = "CommonUiLabels";
    
    /**
     * http post方式 传递的xml字符串
     *
     * @param ctx
     * @param context
     * @return
     * @throws GenericServiceException
     */
    
    public static Map<String, Object> httpPostStr(DispatchContext ctx, Map<String, ? extends Object> context) throws GenericServiceException {
        
        String phoneId = (String) context.get("phoneId");
        String messageBody = (String) context.get("messageBody");
        String url = (String) context.get("url");
        
        HttpClient http = new HttpClient(url);
        http.setContentType("text/xml; charset=utf-8");
        http.setHeader("SOAPAction", "http://tempuri.org/senddx");
        String message = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <senddx xmlns=\"http://tempuri.org/\">\n" +
                "      <sjh>" + phoneId + "</sjh>\n" +
                "      <sjnr>" + messageBody + "</sjnr>\n" +
                "    </senddx>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>";
        String postResult = null;
        try {
            postResult = http.post(message);
        } catch (HttpClientException e) {
            throw new GenericServiceException("Problems invoking HTTP request", e);
        }
        
        Map<String, Object> result = null;
        try {
            Document doc = UtilXml.readXmlDocument(postResult);
            String value = doc.getElementsByTagName("senddxResult").item(0).getTextContent();
            result = ServiceUtil.returnSuccess();
            result.put("senddxResult", value);
        } catch (Exception e) {
            result = ServiceUtil.returnFailure(e.getMessage());
//            throw new GenericServiceException("Problems deserializing result.", e);
        
        }
        
        return result;
    }
    
    
    /**
     * http post方式 传递的xml字符串
     *
     * @param ctx
     * @param context
     * @return
     * @throws GenericServiceException
     */
    
    public static Map<String, Object> httpPostXml(DispatchContext ctx, Map<String, ? extends Object> context) throws GenericServiceException {
        
        String phoneId = (String) context.get("phoneId");
        String messageBody = (String) context.get("messageBody");
        String url = (String) context.get("url");
        
        
        HttpClient http = new HttpClient(url);
        http.setContentType("text/xml; charset=utf-8");
        http.setHeader("SOAPAction", "http://tempuri.org/senddx");
        
        
        Document envelopeDoc = UtilXml.makeEmptyXmlDocument(null);
        
        
        Element envelopeElement = envelopeDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soap:Envelope");
        envelopeDoc.appendChild(envelopeElement);
        
        
        // XML request header
        Element body = UtilXml.addChildElement(envelopeElement, "soap:Body", envelopeDoc);
        
        // Example, setting up required and optional XML document
        // elements: the pickup type
        
        
        Element dxElement = envelopeDoc.createElementNS("http://tempuri.org/", "senddx");
        body.appendChild(dxElement);
        
        UtilXml.addChildElementValue(dxElement, "sjh", phoneId, envelopeDoc);
        UtilXml.addChildElementValue(dxElement, "sjnr", messageBody, envelopeDoc);
        
        // Use OFBiz UtilXml utility to create the XML document
        String requestStr = null;
        try {
            requestStr = UtilXml.writeXmlDocument(envelopeDoc);
        } catch (IOException e) {
            String ioeErrMsg =
                    "Error writing RatingServiceSelectionRequest XML Document"
                            + " to a String: " + e.toString();
            Debug.logError(e, ioeErrMsg, module);
            return ServiceUtil.returnFailure(ioeErrMsg);
        }
        
        String postResult = null;
        try {
            postResult = http.post(requestStr);
        } catch (HttpClientException e) {
            throw new GenericServiceException("Problems invoking HTTP request", e);
        }
        
        Map<String, Object> result = null;
        try {
            Document doc = UtilXml.readXmlDocument(postResult);
            String value = doc.getElementsByTagName("senddxResult").item(0).getTextContent();
            result = ServiceUtil.returnSuccess();
            result.put("senddxResult", value);
        } catch (Exception e) {
            result = ServiceUtil.returnFailure(e.getMessage());
//            throw new GenericServiceException("Problems deserializing result.", e);
        
        }
        
        return result;
    }
    
    
    public static Map<String, Object> mobileMessageService(DispatchContext ctx, Map<String, ? extends Object> context) throws GenericServiceException {
        Map<String, Object> result = FastMap.newInstance();
        
        String phoneId = (String) context.get("phoneId");
        String messageBody = (String) context.get("messageBody");
        String code = (String) context.get("code");
        String messageType = (String) context.get("messageType");
        
        Map responseBean = null;
        if (UtilValidate.isNotEmpty(phoneId)) {
            DefaultHttpClient client = new DefaultHttpClient();
            client.addRequestInterceptor(new HttpRequestInterceptor() {
                @Override
                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                    //request.addHeader("Accept-Encoding", "gzip");
                    request.addHeader("Authorization", "Basic " + new BASE64Encoder().encode("api:d714ec19c9e8d259641d9546bffb06de".getBytes("utf-8")));
                }
            });
            client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 30000);
            client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000);
            HttpPost request = new HttpPost("http://sms-api.luosimao.com/v1/send.json");
            ByteArrayOutputStream bos = null;
            InputStream bis = null;
            byte[] buf = new byte[10240];
            String content = null;
            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("mobile", phoneId));
                
                params.add(new BasicNameValuePair("message", messageBody+"【真格邦软件】"));
                request.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
                HttpResponse response = client.execute(request);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    bis = response.getEntity().getContent();
                    bos = new ByteArrayOutputStream();
                    int count;
                    while ((count = bis.read(buf)) != -1) {
                        bos.write(buf, 0, count);
                    }
                    bis.close();
                    content = bos.toString();
                    responseBean = new Gson().fromJson(content, Map.class);
                    result.put("resultData",responseBean);
                } else {
                    result.put("resultData","内部错误");
                }
                
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (bis != null) {
                    try {
                        bis.close();// 最后要关闭BufferedReader
                    } catch (Exception e) {
                    
                    }
                }
            }
        } else {
             result.put("resultData","内部错误");
        }
        
        return result;
    }
    
    
}
