<%--
  Created by IntelliJ IDEA.
  User: Administrator
  Date: 2015/5/21
  Time: 9:18
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
  String appId = request.getParameter("appId");
  String timeStamp = request.getParameter("timeStamp");
  String nonceStr = request.getParameter("nonceStr");
  String package1 = request.getParameter("package");
  String paySign = request.getParameter("paySign");
%>
<html>
<head>
  <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
  <title>微信安全支付</title>

  <script type="text/javascript">

    //调用微信JS api 支付
    function jsApiCall()
    {

      WeixinJSBridge.invoke(
              'getBrandWCPayRequest', {
                "appId": '<%=appId%>',
                "timeStamp": '<%=timeStamp%>',
                "nonceStr": '<%=nonceStr%>',
                "package": '<%=package1%>',
                "signType": 'MD5',
                "paySign": '<%=paySign%>'
              },
              function(res){
                WeixinJSBridge.log(res.err_msg);
                if(res.err_msg == "get_brand_wcpay_request:ok" ) {
                  document.location.href="/wap/control/myOrder";
                }
              }
      );
    }

    function callpay()
    {

      if (typeof WeixinJSBridge == "undefined"){

        if( document.addEventListener ){

          document.addEventListener('WeixinJSBridgeReady', jsApiCall, false);
        }else if (document.attachEvent){

          document.attachEvent('WeixinJSBridgeReady', jsApiCall);

          document.attachEvent('onWeixinJSBridgeReady', jsApiCall);
        }
      }else{
        jsApiCall();
      }
    }
  </script>
</head>
<body>

</body>
</html>
<script type="text/javascript">
  callpay();
</script>