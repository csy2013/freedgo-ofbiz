<!-- begin 基本信息 -->
<div class="box box-info">
    <div class="box-header with-border">
        <ul class="nav nav-tabs">
            <li role="presentation" class="active"><a
                    href="<@ofbizUrl>member_Detail</@ofbizUrl>?partyId=${(personDetail.partyId)?if_exists}">基本信息</a>
            </li>
          <#--  <li role="presentation"><a
                    href="<@ofbizUrl>productPromoCodeParty</@ofbizUrl>?partyId=${(personDetail.partyId)?if_exists}">已领取优惠券</a>
            </li>-->
        <#--<li role="presentation"  ><a href="<@ofbizUrl>CustomJuice</@ofbizUrl>?partyId=${(personDetail.partyId)?if_exists}" >定制果汁</a></li>-->
        </ul>

    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form">
            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">会员编号：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(personDetail.partyId)?if_exists}</p>
                </div>
                <label class="col-md-3 col-lg-3  control-label" id="nickname" for="nickname">会员类型：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    <#if party.partyCategory=='MEMBER'>
                        个人会员
                    <#else>
                        企业会员
                    </#if>
                    </p>
                </div>
            </div>
        <#if party.partyCategory=='MEMBER'>
            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="realname" for="realname">真实姓名：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(personDetail.name)?if_exists}</p>
                </div>
                <label class="col-md-3 col-lg-3  control-label" id="gender" for="gender">性别：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                        <#if personDetail.gender?has_content>
                            <#if personDetail.gender=='M'>
                                男
                            <#else>
                                女
                            </#if>
                        <#else>
                            男
                        </#if>
                    </p>
                </div>
            </div>
            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="mobile" for="mobile">手机号：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(personDetail.mobile)?if_exists}</p>
                </div>
                <label class="col-md-3 col-lg-3  control-label" id="nickname" for="nickname">昵称：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(personDetail.nickname)?if_exists}</p>
                </div>
            </div>

            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="mobile" for="mobile">登录账号：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(userLoginId)?if_exists}</p>
                </div>
                <#--&lt;#&ndash;<label class="col-md-3 col-lg-3  control-label">会员等级：</label>&ndash;&gt;
                &lt;#&ndash;<div class="col-md-3 col-lg-3 ">&ndash;&gt;
                    &lt;#&ndash;<p class="form-control-static">&ndash;&gt;
                    &lt;#&ndash;${(partyLevel)?if_exists}&ndash;&gt;
                    &lt;#&ndash;</p>&ndash;&gt;
                &lt;#&ndash;</div>&ndash;&gt;-->
                <label class="col-md-3 col-lg-3  control-label">会员积分：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(partyScore.scoreValue)?default(0)}
                    </p>
                </div>
            </div>
            <#--<div class="form-group m-b-xs">
                &lt;#&ndash;<label class="col-md-3 col-lg-3  control-label">成长值：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(partyAttribute.attrValue)?default(0)}
                    </p>
                </div>&ndash;&gt;

                <label class="col-md-3 col-lg-3  control-label">会员积分：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(partyScore.scoreValue)?default(0)}
                    </p>
                </div>
            </div>
            &lt;#&ndash;<div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">账户余额：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(partyAccount.amount)?default(0)}
                    </p>
                </div>&ndash;&gt;
            </div>
-->
            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">用户标签：</label>
                <#assign temp=0>
                <label class="col-md-9 col-lg-9   control-label" style="text-align: left" >
                    <#list partyLables as label>
                        <#if temp==0>
                            ${label.tagName}
                        <#else>
                            ,${label.tagName}
                        </#if>
                        <#assign temp=temp+1>
                    </#list>
                </label>

            </div>
        <#else>
            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="realname" for="realname">企业名称：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(personDetail.name)?if_exists}</p>
                </div>
                <label class="col-md-3 col-lg-3  control-label" id="gender" for="gender">联系电话：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(personDetail.mobile)?if_exists}
                    </p>
                </div>
            </div>

            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">企业地址：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(party.description)?default("")}
                    </p>
                </div>
            </div>

            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="mobile" for="mobile">登录账号：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(userLoginId)?if_exists}</p>
                </div>
                <label class="col-md-3 col-lg-3  control-label">昵称：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(personDetail.nickname)?if_exists}
                    </p>
                </div>
            </div>
            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="mobile" for="mobile">会员等级：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">${(partyLevel)?if_exists}</p>
                </div>
                <label class="col-md-3 col-lg-3  control-label">成长值：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(partyAttribute.attrValue)?default(0)}
                    </p>
                </div>
            </div>
            <div class="form-group m-b-xs">
                <label class="col-md-3 col-lg-3  control-label" id="mobile" for="mobile">积分：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static"> ${(partyScore.scoreValue)?default(0)}</p>
                </div>
                <label class="col-md-3 col-lg-3  control-label">账户余额：</label>
                <div class="col-md-3 col-lg-3 ">
                    <p class="form-control-static">
                    ${(partyAccount.amount)?default(0)}
                    </p>
                </div>
            </div>
        </#if>
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
       <#-- <a href="javascript:addAddress('${partyId}')" id="add_address" class="btn btn-primary">创建</a>-->
    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form">
        <#if shippingContactMechList?has_content>
            <#list shippingContactMechList as s>
                <#assign shippingContactMech = delegator.findByPrimaryKey("PostalAddress",{"contactMechId" : s.contactMechId})>
                <div>
                    <div class="form-group m-b-xs">
                        <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">收货人：</label>

                        <div class="col-md-3 col-lg-3 ">
                            <p class="form-control-static">${(shippingContactMech.toName)?if_exists}</p>
                        </div>
                        <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">联系方式：</label>

                        <div class="col-md-3 col-lg-3 ">
                            <p class="form-control-static">${(shippingContactMech.mobilePhone)?if_exists}</p>
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
                        <div class="col-md-1 col-lg-1 ">
                            <input type="radio" value='${s.contactMechId}' name="setDefault" id="${s.contactMechId}"
                                   <#if (shippingContactMech.isDefault)?default("0") == '1'>checked="checked"</#if>/>
                        </div>
                       <#-- <div class="col-md-3 col-lg-3 ">
                            <p class="form-control-static">
                                <a class="btn btn-primary"
                                   href="javascript:editAddress('${shippingContactMech.toName?if_exists}','${(shippingContactMech.mobilePhone)?if_exists}','${(shippingContactMech.stateProvinceGeoId)?if_exists}','${(shippingContactMech.city)?if_exists}','${(shippingContactMech.countyGeoId)?if_exists}','${(shippingContactMech.address1)?if_exists}','${(shippingContactMech.contactMechId)?if_exists}','${partyId}','${(shippingContactMech.isDefault)?default("0")}')">更新</a>
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
                <form class="form-horizontal" id="AddMemberAddressForm" action="" method="post">
                    <input type="hidden" class="form-control dp-vd w-p50" id="preContactMechTypeId"
                           name="preContactMechTypeId" value="POSTAL_ADDRESS">
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
                                    <select name="countryGeoId" id="AddMemberAddressForm_countryGeoId">
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
                                            id="AddMemberAddressForm_stateProvinceGeoId">
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
                                    <select class="form-control" name="city" id="AddMemberAddressForm_cityGeoId">
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
                                            id="AddMemberAddressForm_countyGeoId">
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
                                <textarea class="form-control  dp-vd w-p80" id="address1" name="address1"></textarea>
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
                        <button type="button" id="addMember_btn" class="btn btn-primary">保存</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<!--新增收货地址弹出框 end-->

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
                <form class="form-horizontal" id="MemberAddressForm"
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
                                <textarea class="form-control  dp-vd w-p80" id="address1" name="address1"></textarea>
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
                        <button type="button" id="Member_btn" class="btn btn-primary">保存</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<!--编辑收货地址 end-->

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

<form name="partyDeleteContact" id="partyDeleteContact" method="post"
      action="<@ofbizUrl>deleteContactMechforcustomer</@ofbizUrl>">
    <input name="partyId" id="partyId" value="${partyId}" type="hidden"/>
    <input name="contactMechId" id="contactMechId" type="hidden"/>
    <input name="isDefault" id="isDefault" type="hidden"/>
</form>
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


    $("input[name='setDefault']").click(function () {
        $.post("<@ofbizUrl>setDefault</@ofbizUrl>", {
            contactMechId: $(this).val(),
            partyId: '${partyId}'
        }, function (data) {

        })
    });

    function addAddress(partyId) {
        $('#AddMemberAddressForm #partyId').val(partyId);
        if ($('#AddMemberAddressForm').length) {
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddMemberAddressForm_countryGeoId', 'AddMemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddMemberAddressForm_stateProvinceGeoId', 'AddMemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName');
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddMemberAddressForm_cityGeoId', 'AddMemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName');
            //国家
            $("#AddMemberAddressForm_countryGeoId").change(function (e, data) {
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddMemberAddressForm_countryGeoId', 'AddMemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName');
            });
            $("#AddMemberAddressForm_stateProvinceGeoId").change(function (e, data) {
                //省
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddMemberAddressForm_stateProvinceGeoId', 'AddMemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName');
            });
            $("#AddMemberAddressForm_cityGeoId").change(function (e, data) {
                /* 市*/
                getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'AddMemberAddressForm_cityGeoId', 'AddMemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName');
            });
        }
        $('#add_Modal').modal('show');
    }

    //创建地址提交按钮点击事件
    $('#addMember_btn').click(function () {
        $('#AddMemberAddressForm').dpValidate({
            clear: true
        });
        $('#AddMemberAddressForm').submit();
    });
    //新增表单校验
    $('#AddMemberAddressForm').dpValidate({
        validate: true,
        callback: function () {
            $.ajax({
                url: "createPostalAddressforIco",
                type: "POST",
                data: $('#AddMemberAddressForm').serialize(),
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
                        window.location.href = '<@ofbizUrl>member_Detail</@ofbizUrl>?partyId=' +${partyId};
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

    function editAddress(toName, mobilePhone, stateProvinceGeoId, city, countyGeoId, address1, contactMechId, partyId, isDefault) {
        $('#MemberAddressForm #toName').val(toName);
        $('#MemberAddressForm #mobilePhone').val(mobilePhone);
        $('#MemberAddressForm #address1').val(address1);
        $('#MemberAddressForm #contactMechId').val(contactMechId);
        $('#MemberAddressForm #partyId').val(partyId);
        $('#MemberAddressForm :radio').attr("checked", 'isDefault');
        if ($('#MemberAddressForm').length) {
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'stateList', 'geoId', 'geoName', stateProvinceGeoId);
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_stateProvinceGeoId', 'MemberAddressForm_cityGeoId', 'stateList', 'geoId', 'geoName', city);
            getDependentDropdownValues('getAssociatedStateList', 'countryGeoId', 'MemberAddressForm_cityGeoId', 'MemberAddressForm_countyGeoId', 'stateList', 'geoId', 'geoName', countyGeoId);
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
        $('#edit_Modal').modal('show');
    }

    //编辑地址提交按钮点击事件
    $('#Member_btn').click(function () {
        $('#MemberAddressForm').dpValidate({
            clear: true
        });
        $('#MemberAddressForm').submit();
    });
    //编辑表单校验
    $('#MemberAddressForm').dpValidate({
        validate: true,
        callback: function () {
            $.ajax({
                url: "updatePostalAddressforIco",
                type: "POST",
                data: $('#MemberAddressForm').serialize(),
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
                        window.location.href = '<@ofbizUrl>member_Detail</@ofbizUrl>?partyId=' +${partyId};
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