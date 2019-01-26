<#macro fieldErrors fieldName>
    <#if errorMessageList?has_content>
        <#assign fieldMessages = Static["org.ofbiz.base.util.MessageString"].getMessagesForField(fieldName, true, errorMessageList)>

    <span class="input_tips_warn"></span>
    <br/><span style="color: red;"><#list fieldMessages as errorMsg>
           ${errorMsg}
        </#list></span>
    <#else>
    </#if>
</#macro>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/alifind/css/qq.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/alifind/css/style.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/alifind/js/jquery-ui.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/alifind/xcConfirm/xcConfirm.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/js/jquery-1.11.3.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/js/jquery-ui.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/js/common.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/js/index.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/js/jquery.json.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/js/transport_jquery.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/js/utils.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/alifind/xcConfirm/xcConfirm.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/selectall.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/tab/switch-tab.js</@ofbizContentUrl>"></script>

<style>
    .login-left {
        width: 0px;
        height: 636px;
        float: left;
        overflow: hidden;
        position: relative;
    }

    .login-right {
        width: 420px;
        height: 430px;
        float: none;
        overflow: hidden;
        position: relative;
        margin-top: 20px;
    }

    .logintit {
        margin: 40px 0 0 0;
        height: 36px;
        overflow: hidden;
        width: 300px;
        align-content: center;
        margin-left: 400px;
    }

</style>
<script language="javascript" type="text/javascript">
    function changeCheckCode() {
        $("#checkCodeImge").attr("src", '<@ofbizUrl>captcha.jpg?captchaCodeId=reg</@ofbizUrl>&ss=' + Math.random());
    }

    function getMobileCheckCode(o) {
        var mobile = document.getElementById("CUSTOMER_MOBILE_CONTACT").value;
        var reg = /^(13[0-9]|14[0-9]|15[0-9]|18[0-9]|17[0-9])\d{8}$/;
        var ismobile = reg.test(mobile);
        if (!ismobile) {
            document.getElementById("errorMobile").innerHTML = "手机格式不正确！";
            return false;
        } else {
            document.getElementById("errorMobile").innerHTML = "";
            jQuery.ajax({
                url: 'ajaxGetMobileAuthCode',
                type: 'POST',
                data: {"phoneId": mobile},
                error: function (data) {
                    alert(data.errorMessage);
                },
                success: function (data) {
//                    alert(data.responseMessage);
                    time(o);
                }

            });
        }

    }

    var wait = 100;

    function time(o) {
        if (wait == 0) {
            o.removeAttribute("disabled");
            o.value = "免费获取验证码";
            wait = 100;
        } else {
            o.setAttribute("disabled", true);
            o.value = "重新发送(" + wait + ")";
            wait--;
            setTimeout(function () {
                        time(o)
                    },
                    1000)
        }
    }

    $(document).ready(function () {


    });


    $(function () {

        $(".tips_input").focus(function (event) {
            if ($(this).val() == this.defaultValue) {
                $(this).val("").css("color", "#000");
            }
        });
        $(".tips_input").blur(function (event) {
            if ($(this).val() == "") {
                $(this).val(this.defaultValue).css("color", "#b0b0b0");
            }
        });
        $("#xlpass").focus(function (event) {
            $(this).attr("style", "display:none");
            $("#PASSWORD").attr("style", "");
            $("#PASSWORD").focus();
        });
        $("#PASSWORD").blur(function (event) {
            if ($(this).val() == "") {
                $(this).attr("style", "display:none");
                $("#xlpass").attr("style", "");
            }
        });
        $("#xlrepass").focus(function (event) {
            $(this).attr("style", "display:none");
            $("#CONFIRM_PASSWORD").attr("style", "");
            $("#CONFIRM_PASSWORD").focus();
        });
        $("#CONFIRM_PASSWORD").blur(function (event) {
            if ($(this).val() == "") {
                $(this).attr("style", "display:none");
                $("#xlrepass").attr("style", "");
            }
        });
        $("#xlpass1").focus(function (event) {
            $(this).attr("style", "display:none");
            $("#PASSWORD1").attr("style", "");
            $("#PASSWORD1").focus();
        });
        $("#PASSWORD1").blur(function (event) {
            if ($(this).val() == "") {
                $(this).attr("style", "display:none");
                $("#xlpass1").attr("style", "");
            }
        });
        $("#xlrepass1").focus(function (event) {
            $(this).attr("style", "display:none");
            $("#CONFIRM_PASSWORD1").attr("style", "");
            $("#CONFIRM_PASSWORD1").focus();
        });
        $("#CONFIRM_PASSWORD1").blur(function (event) {
            if ($(this).val() == "") {
                $(this).attr("style", "display:none");
                $("#xlrepass1").attr("style", "");
            }
        });
    });

    function checkEmail(t) {
        var email = document.getElementById("CUSTOMER_EMAIL").value;
        var reg = /^\w+((-\w+)|(\.\w+))*\@[A-Za-z0-9]+((\.|-)[A-Za-z0-9]+)*\.[A-Za-z0-9]+$/;
        var ismail = reg.test(email);
        if (t == 1) {
            if (!ismail) {
                document.getElementById("errorEmail").innerHTML = "邮箱格式不正确！";
                return false;
            } else {
                jQuery.ajax({
                    url: '<@ofbizUrl>checkRegisterLoginId</@ofbizUrl>',
                    type: 'POST',
                    async: false,
                    data: {"USER_LOGIN_ID": email},
                    success: function (data) {
                        if (!data._PASSED_) {
                            document.getElementById("errorEmail").innerHTML = "该邮箱已注册！";
                            event.preventDefault();
                            return false;
                        }
                    }
                });
                document.getElementById("errorEmail").innerHTML = "";
            }
        } else {
            if (email == "") {
                document.getElementById("errorEmail").innerHTML = "";
            } else {
                if (!ismail) {
                    document.getElementById("errorEmail").innerHTML = "邮箱格式不正确！";
                    return false;
                } else {
                    jQuery.ajax({
                        url: '<@ofbizUrl>checkRegisterLoginId</@ofbizUrl>',
                        type: 'POST',
                        data: {"USER_LOGIN_ID": email},
                        success: function (data) {
                            if (!data._PASSED_) {
                                document.getElementById("errorEmail").innerHTML = "该邮箱已注册！";
                                event.preventDefault();
                                return false;
                            }
                        }
                    });
                    document.getElementById("errorEmail").innerHTML = "";
                }
            }
        }
        return true;
    }

    function checkMobile(t) {
        var mobile = document.getElementById("CUSTOMER_MOBILE_CONTACT").value;
        var reg = /^(13[0-9]|14[0-9]|15[0-9]|18[0-9]|17[0-9])\d{8}$/;
        var ismobile = reg.test(mobile);
        if (t == 1) {
            if (!ismobile) {
                document.getElementById("errorMobile").innerHTML = "手机格式不正确！";
                return false;
            } else {
                jQuery.ajax({
                    url: '<@ofbizUrl>checkRegisterLoginId</@ofbizUrl>',
                    type: 'POST',
                    data: {"USER_LOGIN_ID": mobile},
                    success: function (data) {
                        console.log(data);
                        if (!data._PASSED_) {
                            document.getElementById("errorMobile").innerHTML = "该手机号已注册！";
                            event.preventDefault();
                            return false;
                        }
                    }
                });
                document.getElementById("errorMobile").innerHTML = "";
            }
        } else {
            if (mobile == "") {
                document.getElementById("errorMobile").innerHTML = "";
            } else {
                if (!ismobile) {
                    document.getElementById("errorMobile").innerHTML = "手机格式不正确！";
                    return false;
                } else {
                    jQuery.ajax({
                        url: '<@ofbizUrl>checkRegisterLoginId</@ofbizUrl>',
                        type: 'POST',
                        data: {"USER_LOGIN_ID": mobile},
                        success: function (data) {
                            if (!data._PASSED_) {
                                document.getElementById("errorMobile").innerHTML = "该手机号已注册！";
                                event.preventDefault();
                                return false;
                            }
                        }
                    });
                    document.getElementById("errorMobile").innerHTML = "";
                }
            }
        }
        return true;
    }

    function checkPass(pass, t) {
        var ls = 0;
        if (pass.length < 6 || pass.length > 20) {
            return -1;
        }
        if (pass.match(/[a-z]{1,20}/)) {
            ls++;
        }
        if (pass.match(/[A-Z]{1,20}/)) {
            ls++;
        }
        if (pass.match(/[0-9]{1,20}/)) {
            ls++;
        }
        if (pass.match(/[_-]{1,20}/)) {
            ls++;
        }
        return ls;
    }

    function Validate(n, a, t) {
        var logPWD = document.getElementById(a);
        if (t == 1) {
            if (logPWD.value == "") {
                document.getElementById("errorPassword" + n).innerHTML = "登录密码不能为空!";
                return false;
            } else {
                document.getElementById("errorPassword" + n).innerHTML = "";
            }
            if (checkPass(logPWD.value) == -1) {
                document.getElementById("errorPassword" + n).innerHTML = "登录密码长度不符合!";
                return false;
            } else {
                document.getElementById("errorPassword" + n).innerHTML = "";
            }
            if (checkPass(logPWD.value) < 2) {
                document.getElementById("errorPassword" + n).innerHTML = "登录密码必须为6-20位大小写字母，数字及'-'、'_'组合!";
                return false;
            } else {
                document.getElementById("errorPassword" + n).innerHTML = "";
            }
        } else {
            if (logPWD.value == "") {
                document.getElementById("errorPassword" + n).innerHTML = "";
                return false;
            }
            if (checkPass(logPWD.value) == -1) {
                document.getElementById("errorPassword" + n).innerHTML = "登录密码长度不符合!";
                return false;
            } else {
                document.getElementById("errorPassword" + n).innerHTML = "";
            }
            if (checkPass(logPWD.value) < 2) {
                document.getElementById("errorPassword" + n).innerHTML = "登录密码必须为6-20位大小写字母，数字及'-'、'_'组合!";
                return false;
            } else {
                document.getElementById("errorPassword" + n).innerHTML = "";
            }
        }

        return true;
    }

    function Validate1(n, a, b, t) {
        var logPWD = document.getElementById(a);
        var confirmLogPWD = document.getElementById(b);
        if (t == 1) {
            if (confirmLogPWD.value == "") {
                document.getElementById("errorConfirm" + n).innerHTML = "确认密码不能为空!";
                return false;
            }
            if (logPWD.value != confirmLogPWD.value) {
                document.getElementById("errorConfirm" + n).innerHTML = "登录密码必须和确认密码相同!";
                return false;
            } else {
                document.getElementById("errorConfirm" + n).innerHTML = "";
            }
        } else {
            if (confirmLogPWD.value == "") {
                document.getElementById("errorConfirm" + n).innerHTML = "";
                return false;
            }
            if (logPWD.value != confirmLogPWD.value) {
                document.getElementById("errorConfirm" + n).innerHTML = "登录密码必须和确认密码相同!";
                return false;
            } else {
                document.getElementById("errorConfirm" + n).innerHTML = "";
            }
        }

        return true;
    }



    function Validate3(n, d, t, cb) {
        var check = document.getElementById(d);
        if (t == 1) {
            if (check.value == "" || check.value == check.defaultValue) {
                document.getElementById("errorCheck" + n).innerHTML = "验证码不能为空!";
                return false;
            } else {
                jQuery.ajax({
                    url: '<@ofbizUrl>checkMobileCheckCode</@ofbizUrl>',
                    type: 'POST',
                    async: false,
                    data: {"CHECK_CODE": check.value},
                    success: function (data) {
                        if (!data._PASSED_) {
                            document.getElementById("errorCheck" + n).innerHTML = "验证码不正确!";
                            event.preventDefault();
                            return false;
                        } else {
//                            cb(t);
                        }
                    }
                });
                document.getElementById("errorCheck" + n).innerHTML = "";
            }
        } else {
            if (check.value == "" || check.value == check.defaultValue) {
                document.getElementById("errorCheck" + n).innerHTML = "";
                return false;
            } else {
                jQuery.ajax({
                    url: '<@ofbizUrl>checkMobileCheckCode</@ofbizUrl>',
                    type: 'POST',
                    data: {"CHECK_CODE": check.value},
                    success: function (data) {
                        if (!data._PASSED_) {
                            document.getElementById("errorCheck" + n).innerHTML = "验证码不正确!";
//                            event.preventDefault();
                            return false;
                        } else {
                            cb(t);
                        }
                    }
                });
                document.getElementById("errorCheck" + n).innerHTML = "";
                return false;
            }
        }
        return true;
    }

    function SubmitForm(a, b, c, d, t) {

            if (Validate(1, a, t) && Validate1(1, a, b, t) && $("#p_check1").attr("checked") == "checked") {
//                var con;
//                con=confirm("请牢记您的密码,确认执行该操作吗？");
//                if(con==true){
                if (Validate3(1, d, t, checkMobile)) {
                    jQuery.ajax({
                        url: '<@ofbizUrl>createCustomerByPhone</@ofbizUrl>',
                        type: 'POST',
                        data: $("#newuserform1").serialize(),
                        success: function (data) {
                            if(data){
                              username = $('#CUSTOMER_MOBILE_CONTACT').val();
                              pass = $('#PASSWORD1').val();
                              document.location.href='<@ofbizUrl>login</@ofbizUrl>?USERNAME='+username+'&PASSWORD='+pass+'&JavaScriptEnabled=Y';
                              return;
                            }

                        }
                    });
                }
//                }

        }
    }
</script>
<div class="authbg">
    <div class="loginbox">
        <div class="login-left">
            <div class="loginl_img"></div>
        </div>
        <div class="logintit">
            <h1>免费注册</h1>
            <a href="<@ofbizUrl>checkLogin/main</@ofbizUrl>" class="fr" style="padding-right:45px; padding-top:12px; color:#a80000">登录</a>
        </div>
        <div class="login-right">

            <div id="form-reg" style="height:364px; padding-bottom:20px;padding-top:10px;">
                <div class="tab-reg" style="margin-bottom:10px">
                    <ul class="tab">
                        <li><a href="#reg-phone">手机注册</a></li>
                    </ul>
                </div>
                <div class="list-wrap">
                    <!--手机注册 -->
                    <div id="reg-phone">
                        <form class="form-reg" action="<@ofbizUrl></@ofbizUrl>" id="newuserform1" name="newuserform1" method="post">
                            <input type="hidden" name="USER_FIRST_NAME" value="">
                            <input type="hidden" name="emailProductStoreId" value="${productStoreId}"/>
                        <#--<input type="hidden" name="emailProductStoreId" value="${productStoreId}"/>-->
                            <p class="input_w01">
                                <label><em>手机</em>
                                    <input class="tips_input" autocomplete="off" tips="11数字，且以13/14/15/18开头" id="CUSTOMER_MOBILE_CONTACT" name="CUSTOMER_MOBILE_CONTACT" type="text"
                                           value="请输入手机号" onblur="checkMobile(0);"></label>
                                <span class=""></span>
                                <em id="errorMobile" class="errorMsg p_pMsg">&nbsp;</em>
                            </p>
                            <!--<p class="input_w01">
                                <label>
                                    <em>会员名</em>
                                    <input value="请输入会员名" autocomplete="off" tips="请输入会员名" name="nick" class="use_nick phone_nick" type="text">
                                </label>
                                <span class=""></span>
                                <em class="errorMsg p_nMsg">&nbsp;</em>
                            </p>-->
                            <p class="input_w01">
                                <label>
                                    <em>设置密码</em>
                                    <input class="tips_input" autocomplete="off" tips="6-20位大小写字母，数字及'-'、'_'组合" id="xlpass1" type="text" name="PASSWORD1"
                                           value="6-20位大小写字母，数字及'-'、'_'组合">
                                    <input name="PASSWORD" id="PASSWORD1" type="password" style="display:none" value="" onblur="Validate(1,'PASSWORD1',0);" onpaste="return  false">
                                </label>
                            <@fieldErrors fieldName="PASSWORD"/>
                                <span class=""></span>
                                <em id="errorPassword1" class="errorMsg p_pMsg">&nbsp;</em>
                            </p>
                            <p class="input_w01">
                                <label>
                                    <em>确认密码</em>
                                    <input class="tips_input" autocomplete="off" value="请再次输入密码" tips="请再次输入密码" id="xlrepass1" type="text" name="CONFIRM_PASSWORD1">
                                    <input name="CONFIRM_PASSWORD" id="CONFIRM_PASSWORD1" type="password" style="display:none" value=""
                                           onblur="Validate1(1,'PASSWORD1','CONFIRM_PASSWORD1',0);" onpaste="return  false">
                                </label>
                            <@fieldErrors fieldName="CONFIRM_PASSWORD"/>
                                <span class=""></span>
                                <em id="errorConfirm1" class="errorMsg p_pMsg">&nbsp;</em>
                            </p>
                            <p class="input_w02">
                                <label>
                                    <em>验证码</em>
                                    <input autocomplete="off" tips="请输入验证码" name="checker_str" data="1" id="checker_str" type="text" value="6位验证码" class="input_code tips_input"
                                           onblur="Validate3(1,'checker_str',0)">
                                </label>
                                <input type="button" id="authCode" name="authCode" value="免费获取手机验证码" class="code_send" onclick="getMobileCheckCode(this)"/>
                            <@fieldErrors fieldName="authCode"/>
                                <span class=""></span>
                                <em id="errorCheck1" class="errorMsg p_pMsg">&nbsp;</em>
                            </p>
                            <p class="input_w03">
                                <input type="checkbox" id="p_check1" checked="checked">
                                已阅读并同意<a target="_blank" href="/auth/regAgreement">《网上商城用户服务协议》</a>
                            </p>
                            <p class="input_w04"><input type="button" class="reg_btn" id="regphone_submit" value="注 册"
                                                        onclick="SubmitForm('PASSWORD1','CONFIRM_PASSWORD1','CUSTOMER_MOBILE_CONTACT','checker_str',1)"></p>


                        </form>
                    </div>

                </div>
            </div>

        </div>
