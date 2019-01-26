
<!-- begin 基本信息 -->
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">基本信息</h3>
    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form">
          <div class="form-group m-b-xs">
              <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">会员编号：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">${(personDetail.partyId)!}</p>
              </div>
              <label class="col-md-3 col-lg-3  control-label" id="nickname" for="nickname">昵称：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">${(personDetail.nickname)!}</p>
              </div>
          </div>
          <div class="form-group m-b-xs">
              <label class="col-md-3 col-lg-3  control-label" id="realname" for="realname">真实姓名：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">${(personDetail.name)!}</p>
              </div>
              <label class="col-md-3 col-lg-3  control-label" id="gender" for="gender">性别：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">${(personDetail.gender)!}</p>
              </div>
          </div>
          <div class="form-group m-b-xs">
              <label class="col-md-3 col-lg-3  control-label" id="mobile" for="mobile">手机号：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">${(personDetail.mobile)!}</p>
              </div>
              <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">社区：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">
                      <!-- begin 遍历关联表，获取社区基本信息 -->
                      <#if partyCommunityList?has_content>
                          <#list partyCommunityList as partyCommunity>
                            <#assign community = delegator.findByPrimaryKey("Community",{"communityId":(partyCommunity.communityId)}?if_exists)?if_exists/>
                            ${(community.name)!}
                            <#if partyCommunity_index!=partyCommunityList.size()-1>
                                、
                            </#if>
                          </#list>
                      </#if>
                      <!-- end 遍历关联表，获取社区基本信息 -->
                  </p>
              </div>
          </div>
          <div class="form-group m-b-xs">
              <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">账户余额：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">
                      ${(partyAccount.amount)?default(0)}
                  </p>
              </div>
              <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">商家编号：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">
                      <#if suppliers?has_content>
                          <#list suppliers as supplier>
                                ${(supplier.partyIdFrom)!}
                              <#if supplier_index!=suppliers.size()-1>
                                  、
                              </#if>
                          </#list>
                      </#if>
                  </p>
              </div>
          </div>
          
          <div class="form-group m-b-xs">
              <label class="col-md-3 col-lg-3  control-label">会员等级：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">
                      ${(partyLevel)!}
                  </p>
              </div>
              
              <label class="col-md-3 col-lg-3  control-label">会员积分：</label>
              <div class="col-md-3 col-lg-3 ">
                  <p class="form-control-static">
                      ${(partyScore.scoreValue)?default(0)}
                  </p>
              </div>
          </div>
     </form>
    </div>
</div><!-- end 基本信息 -->

<div >
    <form method="post" action="<@ofbizUrl>editcontactmechforcustomer</@ofbizUrl>" class="am-form am-form-horizontal" name="createcontactmechform">
        <input type="hidden" name="partyId" value="${partyId}" />
        <input type="hidden" name="preContactMechTypeId" value="POSTAL_ADDRESS" />
        <input type="hidden" name="isFirst" value="${shippingContactMechList?has_content?string('0','1')}" /> <!-- 是否为第一条地址 -->

    </form>
</div>
<!-- begin 收货地址 -->
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">收货地址</h3>
        <a href="javascript:document.createcontactmechform.submit()" class="btn btn-primary">创建</a>
    </div>
    <div class="box-body">
        <form class="form-horizontal" role="form">
            <#if shippingContactMechList?has_content>
                <#list shippingContactMechList as s>
                    <#assign shippingContactMech = delegator.findByPrimaryKey("PostalAddress",{"contactMechId" : s.contactMechId})>
                <div class="form-group m-b-xs">
                    <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">收货人：</label>

                    <div class="col-md-3 col-lg-3 ">
                        <p class="form-control-static">${(shippingContactMech.toName)!}</p>
                    </div>
                    <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">联系方式：</label>

                    <div class="col-md-3 col-lg-3 ">
                        <p class="form-control-static">${(shippingContactMech.mobilePhone)!}</p>
                    </div>
                </div>
                <div class="form-group m-b-xs">
                    <label class="col-md-3 col-lg-3  control-label" id="partyId" for="partyId">地址：</label>

                    <div class="col-md-3 col-lg-3 ">
                        <p class="form-control-static">
                            <#assign countryGeo = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",shippingContactMech.stateProvinceGeoId)))!''>
                            <#assign countryGeoCity = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",shippingContactMech.city)))!''>
                            <#assign countryGeoCounty = (delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",shippingContactMech.countyGeoId)))!''>
                            <#if countryGeo != "">${(countryGeo.get("geoName",locale))!''}</#if>
                            <#if countryGeoCity != "">${(countryGeoCity.get("geoName",locale))!''}</#if>
                            <#if countryGeoCounty != "">${(countryGeoCounty.get("geoName",locale))!''}</#if>${(shippingContactMech.address1)!}
                        </p>
                    </div>
                    <div class="col-md-1 col-lg-1 ">
                        设为默认
                    </div>
                    <div class="col-md-2 col-lg-2 ">
                        <input type="radio" value = '${s.contactMechId}' name="setDefault" id="${s.contactMechId}" <#if (shippingContactMech.isDefault)?default("0") == '1'>checked = "checked"</#if> />
                    </div>
                    <div class="col-md-3 col-lg-3 ">
                        <p class="form-control-static">
                            <a  class="btn btn-primary"  href="<@ofbizUrl>editcontactmechforcustomer?partyId=${partyId}&contactMechId=${s.contactMechId}</@ofbizUrl>">更新</a>
                            <a  class="btn btn-primary"  href="javascript:void(0)" onclick="deleteContactMechforcustomer('${s.contactMechId}')">${uiLabelMap.CommonExpire}</a>
                        </p>
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
<form name="partyDeleteContact" method="post" action="<@ofbizUrl>deleteContactMechforcustomer</@ofbizUrl>">
    <input name="partyId" value="${partyId}" type="hidden"/>
    <input name="contactMechId" id="contactMechId" type="hidden"/>
    <input name="isDefault" id="isDefault" type="hidden"/>
</form>
<script>
    function deleteContactMechforcustomer(contactMechId){
        $("#contactMechId").val(contactMechId);
        var isDefault = "";
        if ($("#"+contactMechId).attr("checked") == "checked"){
            isDefault = "1";
        }
        $("#isDefault").val(isDefault);
        this.document.partyDeleteContact.submit();
    }

    $("input[name='setDefault']").click(function(){
        $.post("<@ofbizUrl>setDefault</@ofbizUrl>",{contactMechId : $(this).val(),partyId:'${partyId}'},function(data){

        })
    });
</script>