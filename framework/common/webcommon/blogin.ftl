<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录</title>

    <link rel="icon" href="" type="image/x-icon">
    <!--[if lt IE 9]>
    <script src="//cdnjs.cloudflare.com/ajax/libs/html5shiv/3.6.1/html5shiv.js" type="text/javascript"></script>
    <![endif]-->
</head>
<body>


<div class="wrap clearfix">
<form name="loginform" action="<@ofbizUrl>login</@ofbizUrl>" method="post">
    <img src="/images/comImage/login_logo.png" class="logo">

    <#escape x as x?html>
        <#if requestAttributes.errorMessageList?has_content><#assign errorMessageList=requestAttributes.errorMessageList></#if>
        <#if requestAttributes.eventMessageList?has_content><#assign eventMessageList=requestAttributes.eventMessageList></#if>
        <#if requestAttributes.serviceValidationException?exists><#assign serviceValidationException = requestAttributes.serviceValidationException></#if>
        <#if requestAttributes.uiLabelMap?has_content><#assign uiLabelMap = requestAttributes.uiLabelMap></#if>

        <#if !errorMessage?has_content>
            <#assign errorMessage = requestAttributes._ERROR_MESSAGE_?if_exists>
        </#if>
        <#if !errorMessageList?has_content>
            <#assign errorMessageList = requestAttributes._ERROR_MESSAGE_LIST_?if_exists>
        </#if>
        <#if !eventMessage?has_content>
            <#assign eventMessage = requestAttributes._EVENT_MESSAGE_?if_exists>
        </#if>
        <#if !eventMessageList?has_content>
            <#assign eventMessageList = requestAttributes._EVENT_MESSAGE_LIST_?if_exists>
        </#if>

    <#-- display the error messages -->
        <#if (errorMessage?has_content || errorMessageList?has_content)>
            <div id="msg" class="errors">
                        <#if errorMessage?has_content>
                            ${StringUtil.wrapString(errorMessage)}
                        </#if>
                        <#if errorMessageList?has_content>
                            <#list errorMessageList as errorMsg>
                                ${StringUtil.wrapString(errorMsg)}
                            </#list>
                        </#if>
            </div>
        </#if>

    <#-- display the event messages -->
        <#if (eventMessage?has_content || eventMessageList?has_content)>
           <div id="msg" class="errors">
               <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
               <h4><i class="icon fa fa-info"></i> ${StringUtil.wrapString(uiLabelMap.CommonFollowingOccurred)}:</h4>
                                <#if eventMessage?has_content>
                                    ${StringUtil.wrapString(eventMessage)}
                                </#if>
                                <#if eventMessageList?has_content>
                                    <#list eventMessageList as eventMsg>
                                        ${StringUtil.wrapString(eventMsg)}
                                    </#list>
                                </#if>
           </div>

        </#if>
    </#escape>
<input type="hidden" name="JavaScriptEnabled" value="N"/>
<div class="main">
    <div class="top clearfix">
        <span>欢迎登录</span>
        <i class="arrow-right"></i>
    </div>
    <div class="user-wrp">
        <i class="icon"></i>
        <input id="USERNAME" name="USERNAME" tabindex="1" accesskey="u" type="text" value="" size="25" autocomplete="off">
    </div>
    <div class="psd-wrp">
        <i class="icon"></i>

        <input id="PASSWORD" name="PASSWORD" class="required" tabindex="2" accesskey="p" type="password" value="" size="25" autocomplete="off">
    </div>
    <div class="btn-wrp">
        <input type="hidden" name="lt" value="LT-1047-QPpJnaWC5BXRlX1zmzcPe5zXqeDm4R-cas01.example.org">
        <input type="hidden" name="execution" value="e1s1">
        <input type="hidden" name="_eventId" value="submit">
        <button name="submit" accesskey="l" tabindex="4" type="submit">立即登录</button>
    </div>
</div>
</form>
</div>
</body>
</html>

<!-- Mainly scripts -->
<script src="/images/themes/adminlet/js/plugins/jQuery/jQuery-2.1.4.min.js"></script>
<script src="/images/themes/adminlet/js/bootstrap.min.js"></script>

<script language="JavaScript" type="text/javascript">
    document.loginform.JavaScriptEnabled.value = "Y";
    <#if focusName??>
        document.loginform.USERNAME.focus();
    <#else>
         document.loginform.PASSWORD.focus();
    </#if>
</script>

<style>
    * {
        margin: 0;
        padding: 0;
        outline: 0 none;
    }

    body {
        -webkit-user-select: none;
        -webkit-touch-callout: none;
        margin: 0;
        padding: 0;
        font-family: Helvetica, "微软雅黑";
        margin: auto;
        background: #ffffff;
        -webkit-tap-highlight-color: rgba(255, 255, 255, 0);
    }

    div {
        margin: 0;
        padding: 0;
    }

    img {
        max-width: 100%;
        border: 0;
    }

    li {
        list-style-type: none;
    }

    h4 {
        font-size: 13px;
    }

    ul li {
        outline: none;
    }

    li {
        margin: 0;
        padding: 0;
    }

    a {
        text-decoration: none;
    }

    html {
        -webkit-text-size-adjust: none;
    }

    .clearfix:after {
        visibility: hidden;
        display: block;
        font-size: 0;
        content: " ";
        clear: both;
        height: 0;
    }

    .clearfix {
        zoom: 1;
    }

    html {
        width: 100%;
        height: 100%;
    }

    body {
        width: 100%;
        height: 100%;
        background: url("../../images/comImage/login_bg.jpg") no-repeat center;
        background-size: cover;
    }

    .wrap {
        width: 100%;
        text-align: center;
        position: absolute;
        top: 45%;
        margin-top: -257px;
    }

    .main {
        width: 290px;
        height: 290px;
        margin: 28px auto 0;
        border-radius: 35px;
        background: rgba(255, 255, 255, .72);
        box-shadow: 3px 3px 10px -3px rgba(13, 5, 9, .58);
        overflow: auto;
        padding: 35px;
    }

    .main .top {
        margin-bottom: 30px;
    }

    .main .top span {
        display: block;
        font-size: 30px;
        color: #6c6b6b;
        float: left;
    }

    .arrow-right {
        display: block;
        width: 30px;
        height: 30px;
        background: url("../../images/comImage/login_icon_3.png") no-repeat center;
        float: right;
    }

    .user-wrp,
    .psd-wrp {
        position: relative;
        height: 40px;
        border: 1px solid #e0dfde;
        margin-bottom: 23px;
        padding-left: 40px;
        padding-right: 5px;
        background: #fff;
    }

    .user-wrp i.icon,
    .psd-wrp i.icon {
        position: absolute;
        top: 50%;
        margin-top: -9px;
        left: 13px;
        display: block;
        width: 18px;
        height: 18px;
    }

    .user-wrp i.icon {
        background: url("../../images/comImage/login_icon_2.png") no-repeat center;
    }

    .psd-wrp i.icon {
        background: url("../../images/comImage/login_icon_1.png") no-repeat center;
    }

    .user-wrp input,
    .psd-wrp input {
        width: 100%;
        height: 100%;
        border: 0 none;
        background: transparent;
    }

    ::-webkit-input-placeholder {
        color: #b2b2b2;
    }

    ::-moz-placeholder {
        　color: #b2b2b2;
    }

    ::-moz-placeholder {
        　color: #b2b2b2;
    }

    ::-ms-input-placeholder {
        　color: #b2b2b2;
    }

    .btn-wrp {
        margin-top: 48px;
    }

    .btn-wrp button {
        width: 100%;
        height: 40px;
        background: #284a91;
        color: #fff;
        border-radius: 5px;
        font-size: 16px;
        border: 0 none;
        cursor: pointer;
    }

</style>