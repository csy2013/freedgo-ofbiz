<#--定义header-->
<#if (requestAttributes.person)?exists><#assign person = requestAttributes.person></#if>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<#if (requestAttributes.person)?exists><#assign person = requestAttributes.person></#if>
<#if (requestAttributes.partyGroup)?exists><#assign partyGroup = requestAttributes.partyGroup></#if>
<#assign docLangAttr = locale.toString()?replace("_", "-")>
<#assign langDir = "ltr">
<#if "ar.iw"?contains(docLangAttr?substring(0, 2))>
  <#assign langDir = "rtl">
</#if>
<html lang="${docLangAttr}" dir="${langDir}" xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
    <meta name="description" content="yabiz商城中台管理系统">

    <META HTTP-EQUIV="Pragma" CONTENT="no-cache">
    <META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
    <META HTTP-EQUIV="Expires" CONTENT="0">
    <title>yabiz商城中台管理系统: <#if (page.titleProperty)?has_content>${uiLabelMap[page.titleProperty]}<#else>${(page.title)?if_exists}</#if></title>
<#if layoutSettings.shortcutIcon?has_content>
  <#assign shortcutIcon = layoutSettings.shortcutIcon/>
<#elseif layoutSettings.VT_SHORTCUT_ICON?has_content>
  <#assign shortcutIcon = layoutSettings.VT_SHORTCUT_ICON.get(0)/>
</#if>
<#if shortcutIcon?has_content>
    <#--<link rel="shortcut icon" href="<@ofbizContentUrl>${StringUtil.wrapString(shortcutIcon)}</@ofbizContentUrl>"/>-->
</#if>
<#if layoutSettings.javaScripts?has_content>
<#--layoutSettings.javaScripts is a list of java scripts. -->
<#-- use a Set to make sure each javascript is declared only once, but iterate the list to maintain the correct order -->
  <#assign javaScriptsSet = Static["org.ofbiz.base.util.UtilMisc"].toSetWithoutNull(layoutSettings.javaScripts)/>
  <#list layoutSettings.javaScripts as javaScript>
    <#if javaScript?exists && javaScriptsSet.contains(javaScript)>
      <#assign nothing = javaScriptsSet.remove(javaScript)/>
        <script src="<@ofbizContentUrl>${StringUtil.wrapString(javaScript)}</@ofbizContentUrl>" type="text/javascript"></script>
    </#if>
  </#list>
</#if>
<#if layoutSettings.VT_HDR_JAVASCRIPT?has_content>
  <#list layoutSettings.VT_HDR_JAVASCRIPT as javaScript>
      <script src="<@ofbizContentUrl>${StringUtil.wrapString(javaScript)}</@ofbizContentUrl>" type="text/javascript"></script>
  </#list>
</#if>

<#if layoutSettings.styleSheets?has_content>
  <#assign styleSheetsSet = Static["org.ofbiz.base.util.UtilMisc"].toSetWithoutNull(layoutSettings.styleSheets)/>

<#--layoutSettings.styleSheets is a list of style sheets. So, you can have a user-specified "main" style sheet, AND a component style sheet.-->
  <#list layoutSettings.styleSheets as styleSheet>
    <#if styleSheet?exists && styleSheetsSet.contains(styleSheet)>
      <#assign nothing = styleSheetsSet.remove(styleSheet)/>
        <link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
    </#if>
  </#list>
</#if>
<#if layoutSettings.VT_STYLESHEET?has_content>
  <#list layoutSettings.VT_STYLESHEET as styleSheet>
      <link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
  </#list>
</#if>
<#if layoutSettings.rtlStyleSheets?has_content && langDir == "rtl">
<#--layoutSettings.rtlStyleSheets is a list of rtl style sheets.-->
  <#list layoutSettings.rtlStyleSheets as styleSheet>
      <link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
  </#list>
</#if>
<#if layoutSettings.VT_RTL_STYLESHEET?has_content && langDir == "rtl">
  <#list layoutSettings.VT_RTL_STYLESHEET as styleSheet>
      <link rel="stylesheet" href="<@ofbizContentUrl>${StringUtil.wrapString(styleSheet)}</@ofbizContentUrl>" type="text/css"/>
  </#list>
</#if>
<#--<script src="<@ofbizUrl>barebone.js</@ofbizUrl>${externalKeyParam}" type="text/javascript"></script>-->
<#--<link rel="stylesheet" href="<@ofbizUrl>barebone.css</@ofbizUrl>${externalKeyParam}" type="text/css"/>-->
<#if layoutSettings.VT_EXTRA_HEAD?has_content>
  <#list layoutSettings.VT_EXTRA_HEAD as extraHead>
  ${extraHead}
  </#list>
</#if>
<#if layoutSettings.WEB_ANALYTICS?has_content>
    <script language="JavaScript" type="text/javascript">
        <#list layoutSettings.WEB_ANALYTICS as webAnalyticsConfig>
    ${StringUtil.wrapString(webAnalyticsConfig.webAnalyticsCode?if_exists)}
    </#list>
      </script>
</#if>
   <#-- <script>
        var _hmt = _hmt || [];
        (function () {
            var hm = document.createElement("script");
            hm.src = "//hm.baidu.com/hm.js?8aa259178d3e2dd5d3165f5e6f5919d4";
            var s = document.getElementsByTagName("script")[0];
            s.parentNode.insertBefore(hm, s);
        })();
    </script>

    <!--[if lt IE 9]>
  <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
  <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]&ndash;&gt;
    <script type="text/javascript">
        $.fn.bootstrapBtn = $.fn.button.noConflict();
        //        $.fn.bootstrapDP = $.fn.datepicker.noConflict(); // return $.fn.datepicker to previously assigned value
        //解决bootstrap button 与 jquery ui button 冲突
    </script>-->
</head>
<#if layoutSettings.headerImageLinkUrl?exists>
  <#assign logoLinkURL = "${layoutSettings.headerImageLinkUrl}">
<#else>
  <#assign logoLinkURL = "${layoutSettings.commonHeaderImageLinkUrl}">
</#if>
<#assign organizationLogoLinkURL = "${layoutSettings.organizationLogoLinkUrl?if_exists}">

<body class="skin-blue sidebar-mini">
<div id="wrapper" class="wrapper">
    <header class="main-header">
        <!-- Logo -->
        <a href="#" class="logo">
            <!-- mini logo for sidebar mini 50x50 pixels -->
            <span class="logo-mini"><b>I</b>CO</span>
            <!-- logo for regular state and mobile devices -->
            <span class="logo-lg"><b></b>电商中台管理</span>
        </a>
        <!-- Header Navbar: style can be found in header.less -->
        <nav class="navbar navbar-static-top" role="navigation">
            <!-- Sidebar toggle button-->
            <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button">
                <span class="sr-only"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>

            <div class="navbar-custom-menu">
                <ul class="nav navbar-nav">
                    <!-- Messages: style can be found in dropdown.less-->
                <#assign  count = 0 />
                <#if layoutSettings.middleTopMessage1?has_content>
                  <#assign count = (count+1) />
                </#if>
                <#if layoutSettings.middleTopMessage2?has_content>
                  <#assign count = (count+1) />
                </#if>
                <#if layoutSettings.middleTopMessage3?has_content>
                  <#assign count = (count+1) />
                </#if>


                    <!-- User Account: style can be found in dropdown.less -->
                    <li class="dropdown user user-menu">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <img src="<#if layoutSettings.personLogoLinkUrl?has_content>${layoutSettings.personLogoLinkUrl}<#else><@ofbizContentUrl>/images/themes/coloradmin/img/user-1.jpg</@ofbizContentUrl></#if>"
                                 class="user-image" alt="User Image">
                            <span class="hidden-xs"><#if person?exists>${person.name?if_exists}</#if></span>
                        </a>
                        <ul class="dropdown-menu">
                            <!-- User image -->
                            <li class="user-header">

                                <img src="<#if layoutSettings.personLogoLinkUrl?has_content>${layoutSettings.personLogoLinkUrl}<#else><@ofbizContentUrl>/images/themes/coloradmin/img/user-1.jpg</@ofbizContentUrl></#if>"
                                     class="img-circle" alt="User Image">
                            <#--<p>-->
                            <#--Alexander Pierce - Web Developer-->
                            <#--<small>Member since Nov. 2012</small>-->
                            <#--</p>-->
                            </li>
                            <!-- Menu Body -->
                            <#--<li class="user-body">
                                <div class="col-xs-4 text-center">
                                    <a href="#">个人中心</a>
                                </div>
                                <div class="col-xs-4 text-center">
                                    <a href="<@ofbizUrl>ListVisualThemes</@ofbizUrl>">${uiLabelMap.CommonVisualThemes}</a>
                                </div>
                                <div class="col-xs-4 text-center">
                                <@htmlScreenTemplate.renderModalPage id="header_setLocale" name="header_setLocale"  modalUrl="ListLocales" buttonType="custom"
                                modalTitle="${StringUtil.wrapString(uiLabelMap.CommonChooseLanguage)}" description="${StringUtil.wrapString(uiLabelMap.CommonChooseLanguage)}"/>

                                </div>
                            </li>-->
                            <!-- Menu Footer-->
                            <li class="user-footer">
                                <div class="pull-left">
                                    <a href="#pwdUpdateModal" role="button" class="btn" data-toggle="modal">修改密码</a>
                                </div>
                                <div class="pull-right">
                                    <a class="btn btn-default btn-flat" href="<@ofbizUrl>logout</@ofbizUrl>">${uiLabelMap.CommonLogout}</a>
                                </div>
                            </li>
                        </ul>
                    </li>
                    <!-- Control Sidebar Toggle Button -->
                    <#--<li>
                        <a href="#" data-toggle="control-sidebar"><i class="fa fa-gears"></i></a>
                    </li>-->
                </ul>
            </div>
        </nav>
    </header>

    <div id="pwdUpdateModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal_add_title">>
        <div class="modal-dialog">

            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>
                    <h4 class="modal-title">密码修改</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" name="editSystemUserLoginForm" id="editSystemUserLoginForm" action="<@ofbizUrl>updateSystemPassword</@ofbizUrl>" method="post">
                        <div class="form-group">
                            <label class="control-label col-md-4">当前密码:</label>

                            <div class="col-md-6">
                                <input class="form-control" id="currentPassword" name="currentPassword"  type="password" maxlength="20" placeholder="密码">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4">密码:</label>

                            <div class="col-md-6">
                                <input class="form-control" id="newPassword" name="newPassword"  type="password" maxlength="20" placeholder="密码">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="control-label col-md-4">确认密码:</label>

                            <div class="col-md-6">
                                <input class="form-control" id="newPasswordVerify" name="newPasswordVerify" type="password" maxlength="20" placeholder="密码">
                            </div>
                        </div>
                    </form>

                </div>
                <div class="modal-footer">
                    <button type="submit" class="btn btn-primary" id="pwdUpdateModal_save" onclick="savePwd()">保存</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>

        </div>
    </div>
    <script type="text/javascript">
        $(function(){
            $('#pwdUpdateModal').on('hide',function(){
                $('#newPassword').val('');
                $('#newPasswordVerify').val('');
                $('#currentPassword').val();
            })
        });
        function savePwd(){

            var password = $('#newPassword').val();
            var repassword = $('#newPasswordVerify').val();
            var currentPassword = $('#currentPassword').val();
            if(password !== repassword){
                $.tipLayer('密码不一致!')
                return;
            }
            if(password.length<5){
                $.tipLayer('密码至少5个字符!')
                return;
            }
            if(currentPassword==''){
                $.tipLayer('请输入当前密码!')
                return;
            }
            $.ajax({
                type: 'post',
                url: $('#editSystemUserLoginForm').attr('action'),
                data: $('#editSystemUserLoginForm').serialize(),
                async: false,
                success: function (data) {
                    console.log(data)
                    if (data && data.updatedUserLogin) {
                        $.tipLayer('操作成功') ;
                        $('#pwdUpdateModal').modal('hide');

                    } else {
                        if(data._ERROR_MESSAGE_LIST_){
                            $.tipLayer('操作失败:'+data._ERROR_MESSAGE_LIST_[0])
                        }
                        if(data._ERROR_MESSAGE_){
                            $.tipLayer('操作失败:'+data._ERROR_MESSAGE_)
                        }

                    }
                }
            });
        }
    </script>