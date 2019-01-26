<!-- begin 基本信息 -->
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">基本信息</h3>
    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form" id="editForm" method="post" action="">
            <div class="form-group m-b-xs">
                <div class="row">
                    <div class="form-group" data-type="required" data-mark="会员编号：">
                        <label for="title" class="col-md-3 col-lg-3  control-label">会员编号:</label>
                        <div class="col-md-3 col-lg-3">
                            <input type="text" class="form-control  w-p60" id="partyId" name="partyId"
                                   value="${partyId?if_exists}" readonly/>
                            <p class="dp-error-msg"></p>
                        </div>
                        <label for="title" class="col-md-3 col-lg-3  control-label"><i class="required-mark">*</i>会员类型:</label>
                        <div class="col-md-3 col-lg-3">
                            <select class="form-control w-p40" id="partyCategory" name="partyCategory" readonly>
                                <option value="MEMBER" <#if party.partyCategory=='MEMBER'>selected</#if>>个人会员</option>
                            <#--<option value="COMPANY" <#if party.partyCategory=='COMPANY'>selected</#if>>企业会员</option>-->
                            </select>
                        </div>
                    </div>
                </div>
            </div>
            <div id="COMPANY" <#if party.partyCategory=='MEMBER'>hidden</#if>> <!-- 企业会员 new begin-->
                <div class="form-group m-b-xs">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="企业名称：">
                            <label for="title" class="col-md-3 col-lg-3  control-label"> <i class="required-mark">*</i>企业名称:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p60" id="company_name"
                                       name="company_name" value="${personDetail.name?if_exists}">
                                <p class="dp-error-msg"></p>
                            </div>
                            <label for="title" class="col-md-3 col-lg-3  control-label">联系电话:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p60" id="company_mobile"
                                       name="company_mobile" value="${personDetail.mobile?if_exists}">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-group m-b-xs">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="企业地址">
                            <label for="title" class="col-md-3 col-lg-3  control-label">企业地址:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p100" id="description" name="description"
                                       value="${personDetail.mobile?if_exists}">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group m-b-xs">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="登录账号：">
                            <label for="title" class="col-md-3 col-lg-3  control-label"><i class="required-mark">*</i>登录账号:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p80" id="company_userLoginId"
                                       name="company_userLoginId" value="${userLoginId?if_exists}" readonly/>
                                <p class="dp-error-msg"></p>
                            </div>
                            <!--
                            <label for="title" class="col-md-3 col-lg-3  control-label"><i class="required-mark">*</i>登录密码:</label>
                            <div class="col-md-3 col-lg-3">
                               <input type="text" class="form-control dp-vd w-p80" id="company_currentPassword" name="company_currentPassword" >
                                  <p class="dp-error-msg"></p>
                            </div>
                            -->
                        </div>
                    </div>
                </div>

                <div class="form-group m-b-xs">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="会员昵称">
                            <label for="title" class="col-md-3 col-lg-3  control-label">会员昵称:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p80" id="company_nickname"
                                       name="company_nickname" value="${personDetail.nickname?if_exists}">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                </div>
            </div><!--   企业会员 new div end-->


            <div id="MEMBER" <#if party.partyCategory=='COMPANY'>hidden</#if>> <!-- 个人会员 new begin-->
                <div class="form-group m-b-xs">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="真实姓名">
                            <label for="title" class="col-md-3 col-lg-3  control-label">真实姓名:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p60" id="name" name="name"
                                       value='${personDetail.name?if_exists}'>
                                <p class="dp-error-msg"></p>
                            </div>
                            <label for="title" class="col-md-3 col-lg-3  control-label">性别:</label>
                            <div class="col-md-3 col-lg-3">
                                <select class="form-control w-p40" id="gender" name="gender">
                                    <option value="M"
                                            <#if personDetail.gender?has_content><#if personDetail.gender=='M'>selected</#if><#else>selected</#if>>
                                        男
                                    </option>
                                    <option value="F" <#if personDetail.gender?has_content><#if personDetail.gender=='F'>selected</#if></#if>>
                                        女
                                    </option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="form-group m-b-xs">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="手机号：">
                            <label for="title" class="col-md-3 col-lg-3  control-label"><i class="required-mark">*</i>手机号:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p60" id="mobile" name="mobile"
                                       value='${personDetail.mobile?if_exists}'>
                                <p class="dp-error-msg"></p>
                            </div>
                            <label for="title" class="col-md-3 col-lg-3  control-label">会员昵称:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p60" id="nickname" name="nickname"
                                       value="${personDetail.nickname?if_exists}">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-group m-b-xs">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="登录账号">
                            <label for="title" class="col-md-3 col-lg-3  control-label"><i class="required-mark">*</i>登录账号:</label>
                            <div class="col-md-3 col-lg-3">
                                <input type="text" class="form-control dp-vd w-p80" id="userLoginId" name="userLoginId"
                                       value="${userLoginId?if_exists}" readonly>
                                <p class="dp-error-msg"></p>
                            </div>
                            <!--
                            <label for="title" class="col-md-3 col-lg-3  control-label"><i class="required-mark">*</i>登录密码:</label>
                            <div class="col-md-3 col-lg-3">
                               <input type="password" class="form-control dp-vd w-p80" id="currentPassword" name="currentPassword" value="${userLogin.currentPassword}">
                                  <p class="dp-error-msg"></p>
                            </div>
                            -->
                        </div>
                    </div>
                </div>
            </div><!-- new div end-->

        </form>
    </div>
</div><!-- end 基本信息 -->

<div>
    <form method="post" action="<@ofbizUrl>editcontactmechforcustomer</@ofbizUrl>" class="am-form am-form-horizontal"
          name="createcontactmechform">
        <input type="hidden" name="partyId" value="${partyId}"/>
        <input type="hidden" name="preContactMechTypeId" value="POSTAL_ADDRESS"/>
        <input type="hidden" name="isFirst" value="${shippingContactMechList?has_content?string('0','1')}"/>
        <!-- 是否为第一条地址 -->
    </form>
</div>


<!-- begin 收货地址 -->
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">收货地址</h3>
    <#-- <a id="btn_add"  class="btn btn-primary">创建</a>-->
    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form" id="addressForms">
        <#if shippingContactMechList?has_content>
            <#list shippingContactMechList as s>
                <#assign shippingContactMech = delegator.findByPrimaryKey("PostalAddress",{"contactMechId" : s.contactMechId})>
                <div>
                    <div class="gss">
                        <input type="hidden" value="${(shippingContactMech.contactMechId)?default("")}"
                               name="contactMechId"/>
                        <input type="hidden" value="${(shippingContactMech.toName)?default("")}" name="toname"/>
                        <input type="hidden" value="${(shippingContactMech.mobilePhone)?default("")}"
                               name="mobilePhone"/>
                        <input type="hidden" value="${shippingContactMech.stateProvinceGeoId}"
                               name="stateProvinceGeoId"/>
                        <input type="hidden" value="${(shippingContactMech.cityGeoId)?default("")}" name="cityGeoId"/>
                        <input type="hidden" value="${(shippingContactMech.countyGeoId)?default("")}"
                               name="countyGeoId"/>
                        <input type="hidden" value="${(shippingContactMech.address1)?default("")}" name="address1"/>
                        <input type="hidden" value="'+isDefault+'" name="isDefault"/>
                    </div>

                    <div class="form-group m-b-xs">
                        <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">收货人：</label>

                        <div class="col-md-3 col-lg-3 ">
                            <p class="form-control-static">${(shippingContactMech.toName)?default("")}</p>
                        </div>
                        <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">联系方式：</label>

                        <div class="col-md-3 col-lg-3 ">
                            <p class="form-control-static">${(shippingContactMech.mobilePhone)?default("")}</p>
                        </div>
                    </div>
                    <div class="form-group m-b-xs">
                        <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">地址：</label>

                        <div class="col-md-3 col-lg-3 ">
                            <p class="form-control-static">
                                <#assign countryGeo = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",shippingContactMech.stateProvinceGeoId)))?default('')>
                            <#assign countryGeoCity = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",shippingContactMech.city)))?default('')>
                            <#assign countryGeoCounty = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",shippingContactMech.countyGeoId)))?default('')>
                            <#if countryGeo != "">${(countryGeo.get("geoName",locale))?default('')}</#if>
                            <#if countryGeoCity != "">${(countryGeoCity.get("geoName",locale))?default('')}</#if>
                            <#if countryGeoCounty != "">${(countryGeoCounty.get("geoName",locale))?default('')}</#if>${(shippingContactMech.address1)?if_exists}
                            </p>
                        </div>
                        <div class="col-md-1 col-lg-1 ">
                            是否默认
                        </div>
                        <div class="col-md-2 col-lg-2 ">
                            <input type="radio" value='${s.contactMechId}' name="setDefault" id="${s.contactMechId}"
                                   <#if (shippingContactMech.isDefault)?default("0") == '1'>checked="checked"</#if> disabled/>
                        </div>
                       <#-- <div class="col-md-3 col-lg-3 ">
                            <p class="form-control-static">
                                <a class="btn btn-primary"
                                   href="javascript:editAddress('${shippingContactMech.toName}','${(shippingContactMech.mobilePhone)?if_exists}','${(shippingContactMech.stateProvinceGeoId)?if_exists}','${(shippingContactMech.city)?if_exists}','${(shippingContactMech.countyGeoId)?if_exists}','${(shippingContactMech.address1)?if_exists}','${(shippingContactMech.contactMechId)?if_exists}','${partyId}',${(shippingContactMech.isDefault)?default("0")})">更新</a>
                                <a class="btn btn-primary address_Expire" href="javascript:void(0)"
                                   data-id="${s.contactMechId?if_exists}">${uiLabelMap.CommonExpire?if_exists}</a>
                            </p>
                        </div>-->
                    </div>

                </div>
                <#if s_index!=shippingContactMechList.size()-1>
                    <!-- 分割线start -->
                    <div class="cut-off-rule bg-gray"></div>
                    <!-- 分割线end -->
                </#if>
            </#list>
        </#if>
        </form>

    </div>
</div><!-- end 收货地址 -->

<div class="form-group" style="TEXT-ALIGN: center;">
    <button id="btn_save" type="button" class="btn btn-primary m-l-20">保存</button>
    <button id="cancel_submit" type="button" class="btn btn-primary m-l-20" data-dismiss="modal">返回</button>
</div>


<form name="partyDeleteContact" id="partyDeleteContact" method="post"
      action="<@ofbizUrl>deleteContactMechforcustomer</@ofbizUrl>">
    <input name="partyId" id="partyId" value="${partyId}" type="hidden"/>
    <input name="contactMechId" id="contactMechId" type="hidden"/>
    <input name="isDefault" id="isDefault" type="hidden"/>
</form>


<!--新增收货地址 start-->
<div class="modal fade" id="add_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="exampleModalLabel">收货地址编辑/新增</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="MemberAddressForm" action="<@ofbizUrl></@ofbizUrl>" method="post">
                    <!-- 收货人 start-->
                    <input type="hidden" class="form-control dp-vd w-p50" id="preContactMechTypeId"
                           name="preContactMechTypeId" value="POSTAL_ADDRESS">
                    <input type="hidden" class="form-control dp-vd w-p50" id="contactMechTypeId"
                           name="contactMechTypeId" value="POSTAL_ADDRESS">
                    <input type="hidden" class="form-control dp-vd w-p50" id="partyId" name="partyId">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="收货人">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>收货人:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="toName" name="toName">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div><!-- 收货人 end-->

                    <!-- 联系方式 start-->
                    <div class="row">
                        <div class="form-group" data-type="format"
                             data-reg="/^1(?:3[0-9]|5[0-35-9]|7[6-8]|8[0-9])\d{8}$/" data-mark="联系方式">
                            <label for="title" class="col-sm-2 control-label"><i
                                    class="required-mark">*</i>联系方式:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="mobilePhone" name="mobilePhone">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!-- 联系方式 end-->

                    <!--默认国家- 省start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="省">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>省:</label>
                            <div class="col-sm-10">
                                <div style="display:none;">
                                    <select name="countryGeoId" id="MemberAddressForm_countryGeoId">
                                    ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                                    <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
                                        <option selected="selected" value="${defaultCountryGeoId}">
                                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
		                        ${countryGeo.get("geoName",locale)}
                                        </option>
                                    </select>
                                </div>
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="stateProvinceGeoId"
                                            id="MemberAddressForm_stateProvinceGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div> <!--默认国家- 省end-->

                    <!--市start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="市">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>市:</label>
                            <div class="col-sm-10">
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="city" id="MemberAddressForm_cityGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div> <!-- 市end-->

                    <!-- 区start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="区">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>区:</label>
                            <div class="col-sm-10">
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="countyGeoId" id="MemberAddressForm_countyGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div> <!-- 区end-->

                    <!-- 详细地址start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="详细地址">
                            <label for="title" class="control-label col-sm-2"> <i
                                    class="required-mark">*</i>详细地址:</label>
                            <div class="col-sm-10">
                                <textarea class="form-control dp-vd w-p80" id="address1" name="address1"></textarea>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!-- 详细地址end-->

                    <!-- 详细地址start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="默认地址">
                            <label for="title" class="control-label col-sm-2"> <i
                                    class="required-mark">*</i>默认地址:</label>
                            <div class="col-sm-10">
                                <input name="isDefault" value="1" type="radio" checked="checked"/>&nbsp;&nbsp;是
                                <input name="isDefault" value="0" type="radio"/>&nbsp;&nbsp;否
                            </div>
                        </div>
                    </div>
                    <!-- 详细地址end-->
                    <div class="modal-footer">
                        <button type="button" id="address_btn" class="btn btn-primary">保存</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<!--新增收货地址弹出框 end-->

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary"
                        data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->

<!--编辑收货地址 start-->
<div class="modal fade" id="edit_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="exampleModalLabel">收货地址编辑/新增</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="editMemberAddressForm"
                      action="<@ofbizUrl>updatePostalAddressforcustomer</@ofbizUrl>" method="post">
                    <input type="hidden" class="form-control dp-vd w-p50" id="contactMechId" name="contactMechId">
                    <input type="hidden" class="form-control dp-vd w-p50" id="contactMechTypeId"
                           name="contactMechTypeId" value="POSTAL_ADDRESS">
                    <input type="hidden" class="form-control dp-vd w-p50" id="partyId" name="partyId">
                    <!-- 收货人 start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="收货人">
                            <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>收货人:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="toName" name="toName">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div><!-- 收货人 end-->

                    <!-- 联系方式 start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="联系方式">
                            <label for="title" class="col-sm-2 control-label"><i
                                    class="required-mark">*</i>联系方式:</label>
                            <div class="col-sm-10">
                                <input type="text" class="form-control dp-vd w-p50" id="mobilePhone" name="mobilePhone">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!-- 联系方式 end-->

                    <!--默认国家- 省start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="省">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>省:</label>
                            <div class="col-sm-10">
                                <div style="display:none;">
                                    <select name="countryGeoId" id="editMemberAddressForm_countryGeoId">
                                    ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                                    <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
                                        <option selected="selected" value="${defaultCountryGeoId}">
                                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
		                        ${countryGeo.get("geoName",locale)}
                                        </option>
                                    </select>
                                </div>
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="stateProvinceGeoId"
                                            id="editMemberAddressForm_stateProvinceGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div> <!--默认国家- 省end-->

                    <!--市start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="市">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>市:</label>
                            <div class="col-sm-10">
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="city" id="editMemberAddressForm_cityGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div> <!-- 市end-->

                    <!-- 区start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="区">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>区:</label>
                            <div class="col-sm-10">
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <select class="form-control" name="countyGeoId"
                                            id="editMemberAddressForm_countyGeoId">
                                        <option value=""></option>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div> <!-- 区end-->

                    <!-- 详细地址start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="详细地址">
                            <label for="title" class="control-label col-sm-2"> <i
                                    class="required-mark">*</i>详细地址:</label>
                            <div class="col-sm-10">
                                <textarea class="form-control w-p80" id="address1" name="address1"></textarea>
                            </div>
                        </div>
                    </div>
                    <!-- 详细地址end-->

                    <!-- 详细地址start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="默认地址">
                            <label for="title" class="control-label col-sm-2"> <i
                                    class="required-mark">*</i>默认地址:</label>
                            <div class="col-sm-10">
                                <input name="isDefault" value="1" type="radio"/>&nbsp;&nbsp;是
                                <input name="isDefault" value="0" type="radio"/>&nbsp;&nbsp;否
                            </div>
                        </div>
                    </div>
                    <!-- 详细地址end-->
                    <div class="modal-footer">
                        <button type="button" id="Member_btn" class="btn btn-primary">保存</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<!--编辑收货地址 end-->

<script>
    $('.address_Expire').click(function () {
        var $this = $(this);
        var contactMechId = $(this).data('id');
        $("#partyDeleteContact #contactMechId").val(contactMechId);
        var party_Id = $('#partyDeleteContact #partyId').val();
        var isDefault = "";
        if ($("#" + contactMechId).attr("checked") == "checked") {
            isDefault = "1";
        }
        $("#isDefault").val(isDefault);
        $.ajax({
            url: "deleteContactMechforcustomer",
            type: "POST",
            data: $('#partyDeleteContact').serialize(),
            dataType: "json",
            success: function (data) {
                $this.parent().parent().parent().parent().remove();
            },
        });
    });


    $(function () {
        //添加按钮点击事件
        $('#btn_add').click(function () {
            $('#MemberAddressForm').dpValidate({
                clear: true
            });
            $("#add_Modal #toName").val('');
            $("#add_Modal #mobilePhone").val('');
            $("#add_Modal #address1").val('');
            $('#MemberAddressForm #partyId').val(${partyId});
            if ($('#MemberAddressForm').length) {
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'MemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName');
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_cityGeoId', 'MemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName');
                //国家
                $("#MemberAddressForm_countryGeoId").change(function (e, data) {
                    getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
                });
                $("#MemberAddressForm_stateProvinceGeoId").change(function (e, data) {
                    //省
                    getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'MemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName');
                });
                $("#MemberAddressForm_cityGeoId").change(function (e, data) {
                    /* 市*/
                    getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_cityGeoId', 'MemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName');
                });
            }
            $('#add_Modal').modal('show');
        });

        //创建地址提交按钮点击事件
        $('#address_btn').click(function () {
            $('#MemberAddressForm').dpValidate({
                clear: true
            });
            $('#MemberAddressForm').submit();
        });
        //新增表单校验
        $('#MemberAddressForm').dpValidate({
            validate: true,
            callback: function () {
                $.ajax({
                    url: "createPostalAddressforIco",
                    type: "POST",
                    data: $('#MemberAddressForm').serialize(),
                    dataType: "json",
                    success: function (data) {
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal('show');
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href = '<@ofbizUrl>memberEdit</@ofbizUrl>?partyId=' +${partyId};
                        })
                    },
                    error: function (data) {
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

        //会员类型一的选项切换事件
        $('#partyCategory').on('change', function () {
            switch ($(this).val()) {
                case 'MEMBER': {
                    $('#COMPANY').hide();
                    $('#COMPANY #company_name').val('');
                    $('#COMPANY #company_nickname').val('');
                    $('#COMPANY #company_mobile').val('');
                    $('#COMPANY #description').val('');
                    $('#COMPANY #company_userLoginId').val('');
                    $('#COMPANY #company_currentPassword').val('');
                    $('#MEMBER').show();
                }
                    break;
                case 'COMPANY': {
                    $('#COMPANY').show();
                    $('#MEMBER').hide();
                    $('#MEMBER').hide();
                    $('#MEMBER #name').val('');
                    $('#MEMBER #nickname').val('');
                    $('#MEMBER #mobile').val('');
                    $('#MEMBER #userLoginId').val('');
                    $("#MEMBER #gender").find("option[value='M']").attr("selected", true);
                    $('#MEMBER #currentPassword').val('');
                }
                    break;
            }
        });

        //返回
        $('#cancel_submit').click(function () {
            window.location.href = "javascript:history.go(-1)";
        });
        //保存按钮点击事件
        $('#btn_save').click(function () {
            $('#editForm').dpValidate({
                clear: true
            });
            var all_address = "";
            $('#addressForms .gss').each(function () {
                var Arr = $(this);
                for (var i = 0; i < Arr.length; i++) {
                    var toname = Arr.find("input[name=toname]").val(); //收货人
                    var mobilePhone = Arr.find("input[name=mobilePhone]").val();//手机号
                    var stateProvinceGeoId = Arr.find("input[name=stateProvinceGeoId]").val();//省
                    var cityGeoId = Arr.find("input[name=cityGeoId]").val();//市
                    var countyGeoId = Arr.find("input[name=countyGeoId]").val();//区
                    var address1 = Arr.find("input[name=address1]").val();//详细地址
                    var contactMechId = Arr.find("input[name=contactMechId]").val();//默认地址
                    var isDefault = Arr.find("input[name=isDefault]").val();//默认地址
                    all_address += toname + ":" + mobilePhone + ":" + stateProvinceGeoId + ":" + cityGeoId + ":" + countyGeoId + ":" + address1 + ":" + contactMechId + ":" + isDefault + ",";
                }
            });
            $.ajax({
                url: "updateCustomerforIco",
                type: "POST",
                data: $('#editForm').serialize() + "&allAddress=" + all_address,
                dataType: "json",
                success: function (data) {

                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                    $('#modal_msg').modal('show');
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.href = '<@ofbizUrl>partyMasterManage</@ofbizUrl>';
                    })
                },
                error: function (data) {
                    //隐藏新增弹出窗口
                    $('#add_Modal').modal('toggle');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        });
        //保存按钮表单校验
        $('#editForm').dpValidate({
            validate: true,
            callback: function () {
                var all_address = "";
                $('#addressForms .gss').each(function () {
                    var Arr = $(this);
                    for (var i = 0; i < Arr.length; i++) {
                        var toname = td.find("input[name=toname]").val(); //收货人
                        var mobilePhone = Arr.find("input[name=mobilePhone]").val();//手机号
                        var stateProvinceGeoId = Arr.find("input[name=stateProvinceGeoId]").val();//省
                        var cityGeoId = Arr.find("input[name=cityGeoId]").val();//市
                        var countyGeoId = Arr.find("input[name=countyGeoId]").val();//区
                        var address1 = Arr.find("input[name=address1]").val();//详细地址
                        var isDefault = Arr.find("input[name=isDefault]").val();//默认地址
                        all_address += toname + ":" + mobilePhone + ":" + stateProvinceGeoId + ":" + cityGeoId + ":" + countyGeoId + ":" + address1 + ":" + isDefault + ",";
                    }
                });
                $.ajax({
                    url: "updateCustomerforIco",
                    type: "POST",
                    data: $('#editForm').serialize() + "&allAddress=" + all_address,
                    dataType: "json",
                    success: function (data) {
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal('show');
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href = '<@ofbizUrl>TagList</@ofbizUrl>';
                        })
                    },
                    error: function (data) {
                        //隐藏新增弹出窗口
                        $('#add_Modal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });
    });


    function editAddress(toName, mobilePhone, stateProvinceGeoId, city, countyGeoId, address1, contactMechId, partyId, isDefault) {
        $('#editMemberAddressForm #toName').val(toName);
        $('#editMemberAddressForm #mobilePhone').val(mobilePhone);
        $('#editMemberAddressForm #address1').val(address1);
        $('#editMemberAddressForm #contactMechId').val(contactMechId);
        $('#editMemberAddressForm #partyId').val(partyId);
        $('#editMemberAddressForm :radio[value=' + isDefault + ']').prop("checked", true);
        if ($('#editMemberAddressForm').length) {
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'editMemberAddressForm_countryGeoId', 'editMemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName', stateProvinceGeoId);
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'editMemberAddressForm_stateProvinceGeoId', 'editMemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName', city);
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'editMemberAddressForm_cityGeoId', 'editMemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName', countyGeoId);
            //国家
            $("#editMemberAddressForm_countryGeoId").change(function (e, data) {
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'editMemberAddressForm_countryGeoId', 'editMemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
            });
            $("#editMemberAddressForm_stateProvinceGeoId").change(function (e, data) {
                //省
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'editMemberAddressForm_stateProvinceGeoId', 'editMemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName');
            });
            $("#editMemberAddressForm_cityGeoId").change(function (e, data) {
                /* 市*/
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'editMemberAddressForm_cityGeoId', 'editMemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName');
            });
        }
        $('#edit_Modal').modal('show');
    }


    //编辑地址提交按钮点击事件
    $('#Member_btn').click(function () {
        $('#editMemberAddressForm').dpValidate({
            clear: true
        });
        $('#editMemberAddressForm').submit();
    });
    //编辑表单校验
    $('#editMemberAddressForm').dpValidate({
        validate: true,
        callback: function () {
            $.ajax({
                url: "updatePostalAddressforIco",
                type: "POST",
                data: $('#editMemberAddressForm').serialize(),
                dataType: "json",
                success: function (data) {
                    //隐藏新增弹出窗口
                    $('#edit_Modal').modal('toggle');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                    $('#modal_msg').modal('show');
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.href = '<@ofbizUrl>memberEdit</@ofbizUrl>?partyId=' +${partyId};
                    })
                },
                error: function (data) {
                    //隐藏新增弹出窗口
                    $('#add_Modal').modal('toggle');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        }
    });

</script>