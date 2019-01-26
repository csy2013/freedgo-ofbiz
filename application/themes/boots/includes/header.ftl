<#--定义header-->
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
    <link rel="shortcut icon" href="<@ofbizContentUrl>${StringUtil.wrapString(shortcutIcon)}</@ofbizContentUrl>"/>
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
    <#assign hdrJavaScripts = Static["org.ofbiz.base.util.UtilMisc"].toSetWithoutNull(layoutSettings.VT_HDR_JAVASCRIPT)/>
    <#list layoutSettings.VT_HDR_JAVASCRIPT as javaScript>
        <#if javaScript?exists && hdrJavaScripts.contains(javaScript)>
            <#assign nothing = hdrJavaScripts.remove(javaScript)/>
            <script src="<@ofbizContentUrl>${StringUtil.wrapString(javaScript)}</@ofbizContentUrl>" type="text/javascript"></script>

        </#if>
    </#list>
</#if>

<#--minify js-->
    <#--<script src="<@ofbizUrl>barebone.js</@ofbizUrl>${externalKeyParam}" type="text/javascript"></script>-->


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
  <#--  <script>
        var _hmt = _hmt || [];
        (function () {
            var hm = document.createElement("script");
            hm.src = "//hm.baidu.com/hm.js?8aa259178d3e2dd5d3165f5e6f5919d4";
            var s = document.getElementsByTagName("script")[0];
            s.parentNode.insertBefore(hm, s);
        })();
    </script>

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
<body  <#if userPreferences.VISUAL_THEME_NAVBARMINI?default("")=='Y'> class="mini-navbar"</#if>>
<div id="wrapper">
