<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker-bs3.css</@ofbizContentUrl>">

<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/upload/ajaxupload.js</@ofbizContentUrl>"></script>

<style>
    .select-info1 li.current {
        background-color: #e4e4e4;
        border: 1px solid #aaa;
        border-radius: 4px;
        cursor: default;
        float: left;
        margin-right: 80px;
        margin-top: 30px;
        margin-bottom: 30px;
        padding: 0px;
        background-color: #3c8dbc;
        border-color: #367fa9;
        padding: 1px 10px;
        color: #fff;
        list-style-type: none;
    }

    .select-info1 li.current span.icon-del {
        color: #999;
        cursor: pointer;
        display: inline-block;
        font-weight: bold;
        color: rgba(255, 255, 255, 0.7);
    }


</style>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<input type="hidden" id="linkId"/>
<input type="hidden" id="selectName"/>
<form id="BusinessAddForm" class="form-horizontal" name="BusinessAddForm">
    <!-- 内容start -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">基本信息</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-6 form-group">
                    <label class="col-sm-4 control-label">商家编码</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" disabled readonly />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-6 form-group" data-type="required" data-mark="商家名称">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>商家名称</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="partyName"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-6 form-group" data-type="required" data-mark="商家地址">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>商家地址</label>
                    <div class="col-sm-8">
                        <div style="display:none;">
                            <select name="countryGeoId" id="BusinessAddForm_countryGeoId">
                            ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                            <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
                                <option selected="selected" value="${defaultCountryGeoId}">
                                <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
								${countryGeo.get("geoName",locale)}
                                </option>
                            </select>
                        </div>

                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="stateProvinceGeoId"
                                    id="BusinessAddForm_stateProvinceGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="city" id="BusinessAddForm_cityGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="countyGeoId" id="BusinessAddForm_countyGeoId">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-12" style="margin-top: 10px;padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="address"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6  form-group">
                    <label class="col-sm-4 control-label">商家描述</label>
                    <div id="linkDiv" class="col-sm-8">
                        <textarea id="description" name="description" class="form-control" rows="3"
                                  style="resize: none;"></textarea>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-6  form-group" data-type="required" data-mark="会员编码">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>会员编码:</label>
                    <div class="col-sm-8" >
                        <div class="col-sm-12 input-group input-group-sm" style="padding-left:0px">
                            <input type="text" id="memberId" class="form-control dp-vd" readonly style="height:34px;border-radius:0px;">
                            <span class="input-group-btn">
		                      <button class="btn btn-default btn-flat" type="button" id="party_search" style="height:34px;">
		                      	<i class="fa fa-search"></i>
		                      </button>
		                    </span>
                        </div>
                        <p id="party_msg" class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-6 form-group">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>是否启用</label>
                    <div class="col-sm-8 p-t-5">
                        <label class="col-sm-3"><input name="statusId" type="radio" checked
                                                       value="PARTY_ENABLED">是</label>
                        <label class="col-sm-4"><input name="statusId" type="radio" value="PARTY_DISABLED">否</label>
                    </div>
                </div>

            </div>
            <div class="row">
                <div class="col-sm-6 form-group">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>店铺logo</label>
                    <div class="col-sm-4">
                        <img alt="" src="" id="img_logoImgContentId" style="max-height: 100px;max-width: 200px;">
                        <button type="button" class="btn btn-primary" id="logoImgUpload" name="logoImgUpload" value="选择图片">
                            <span class="glyphicon glyphicon-picture" aria-hidden="true"></span>选择图片
                        </button>
                        <input type="hidden" id="logoImgContentId"/>
                    </div>
                    <div class="col-sm-4">
                        <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 300*200px</div>
                    </div>
                </div>
            </div>
        </div><!-- box-body end -->

    </div><!-- 内容end -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">公司营业执照信息</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-4 form-group" data-type="required" data-mark="公司名称">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>公司名称</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="companyName"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-4" id="useBeginDateGroup">
                    <label for="publishDate" class="col-sm-4 control-label">成立时间</label>
                    <div class="input-group date form_datetime col-sm-8 p-l-15 p-r-15">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="companyCreateDate" class="dp-vd" type="hidden" name="companyCreateDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-4 col-sm-10"></div>
                </div>
                <div class="form-group col-sm-4" id="useBeginDateGroup">
                    <label for="publishDate" class="col-sm-4 control-label">营业期限</label>
                    <div class="input-group date form_datetime col-sm-8 p-l-15 p-r-15">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="businessEndDate" class="dp-vd" type="hidden" name="businessEndDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-4 col-sm-10"></div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 form-group" data-type="required" data-mark="法人姓名">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>法人姓名</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="leageName"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-4 form-group" data-type="required" data-mark="联系电话">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>联系电话</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="leageTel"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-4 form-group" data-type="required" data-mark="电子邮箱">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>电子邮箱</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="leageEmail"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 form-group" data-type="required" data-mark="身份证号">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>身份证号</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="leageCardNo"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>

                <div class="col-sm-8 form-group" data-type="required" data-mark="营业执照所在地">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>营业执照所在地:</label>
                    <div class="col-sm-10">
                        <div style="display:none;">
                            <select name="countryGeoId1" id="BusinessAddForm_countryGeoId1">
                            ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                            <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
                                <option selected="selected" value="${defaultCountryGeoId}">
                                <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
								${countryGeo.get("geoName",locale)}
                                </option>
                            </select>
                        </div>

                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="stateProvinceGeoId1"
                                    id="BusinessAddForm_stateProvinceGeoId1">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="city1" id="BusinessAddForm_cityGeoId1">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="countyGeoId1" id="BusinessAddForm_countyGeoId1">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-12" style="margin-top: 10px;padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="busiAddress"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>


            </div>
            <div class="row">
                <div class="col-sm-4 form-group" data-type="required" data-mark="统一社会信用代码">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>统一社会信用代码</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="socialCardNo"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>

                <div class="col-sm-4 form-group">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>身份证正面</label>
                    <div class="col-sm-8">
                        <img alt="" src="" id="img_idCardProsImgContentId" style="max-height: 100px;max-width: 200px;">
                        <button type="button" class="btn btn-primary" id="idCardProsImgUpload" name="idCardProsImgUpload" value="选择图片">
                            <span class="glyphicon glyphicon-picture" aria-hidden="true"></span>选择图片
                        </button>
                        <input type="hidden" id="idCardProsImgContentId"/>
                    </div>
                </div>
                <div class="col-sm-4 form-group">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>身份证反面</label>
                    <div class="col-sm-8">
                        <img alt="" src="" id="img_idCardConsImgContentId" style="max-height: 100px;max-width: 200px;">
                        <button type="button" class="btn btn-primary" id="idCardConsImgUpload" name="idCardConsImgUpload" value="选择图片">
                            <span class="glyphicon glyphicon-picture" aria-hidden="true"></span>选择图片
                        </button>
                        <input type="hidden" id="idCardConsImgContentId"/>
                    </div>
                </div>


            </div>
            <div class="row">

                <div class="col-sm-4 form-group">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>营业执照上传</label>
                    <div class="col-sm-8">
                        <img alt="" src="" id="img_busiImgContentId" style="max-height: 100px;max-width: 200px;">
                        <button type="button" class="btn btn-primary" id="businessLicenseImgUpload"
                                name="businessLicenseImgUpload" value="选择图片">
                            <span class="glyphicon glyphicon-picture" aria-hidden="true"></span>选择图片
                        </button>
                        <input type="hidden" id="busiImgContentId"/>
                    </div>

                </div>
                <div class="col-sm-4 form-group">
                    <label class="col-sm-4 control-label">相关资质(餐饮酒水类)</label>
                    <div class="col-sm-8">
                        <img alt="" src="" id="img_qualifImgContentId" style="max-height: 100px;max-width: 200px;">
                        <button type="button" class="btn btn-primary" id="qualifImgUpload"
                                name="qualifImgUpload" value="选择图片">
                            <span class="glyphicon glyphicon-picture" aria-hidden="true"></span>选择图片
                        </button>
                        <input type="hidden" id="qualifImgContentId"/>
                    </div>
                </div>
                <div class="col-sm-4 form-group">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>合同附件</label>
                    <div class="col-sm-8">
                        <input type="hidden" id="contractDoc">
                        <span id="contractDocName"></span>
                        <button type="button" class="btn btn-primary" id="contractDocBut" name="contractDocBut" >
                            <span class="glyphicon glyphicon-picture" aria-hidden="true">点击上传</span>
                        </button>
                    </div>
                </div>
            </div>
        </div><!-- box-body end -->
    </div>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">结算银行登记</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-4 form-group" data-type="required" data-mark="银行开户名">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>银行开户名</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="bankAcountName"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-4 form-group" data-type="required" data-mark="公司银行账号">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>公司银行账号</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="bankAccount"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-4 form-group" data-type="required" data-mark="开户支行名称">
                    <label class="col-sm-4 control-label"><i class="required-mark">*</i>开户支行名称</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="bankBranchName"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4 form-group">
                    <label class="col-sm-4 control-label">开户行支行联行号</label>
                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="bankBranchAcount"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-8 form-group" data-type="required" data-mark="开户行支行所在地">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>开户行支行所在地</label>
                    <div class="col-sm-10">
                        <div style="display:none;">
                            <select name="countryGeoId" id="BusinessAddForm_countryGeoId2">
                            ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                            <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
                                <option selected="selected" value="${defaultCountryGeoId}">
                                <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
								${countryGeo.get("geoName",locale)}
                                </option>
                            </select>
                        </div>
                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="stateProvinceGeoId2"
                                    id="BusinessAddForm_stateProvinceGeoId2">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="city" id="BusinessAddForm_cityGeoId2">
                                <option value=""></option>
                            </select>
                        </div>
                        <div class="col-sm-4" style="padding-left: 0px;">
                            <select class="form-control" name="countyGeoId" id="BusinessAddForm_countyGeoId2">
                                <option value=""></option>
                            </select>
                        </div>
                    </div>
                </div>

            </div>

        </div><!-- box-body end -->
    </div>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">主营分类</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-4">
                    <button type="button" onclick="addContractClassificView()" class="btn btn-primary" value="添加主营分类">
                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>添加主营分类
                    </button>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12 select-info1">
                    <ul class="p-0" id="addContractClassificUL">

                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">签约品牌</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-4">
                    <button type="button" onclick="addContracBrand()" class="btn btn-primary" value="添加签约品牌">
                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>添加签约品牌
                    </button>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12 select-info1">
                    <ul class="p-0" id="addContractBrandUL">

                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="box box-info text-center">
        <button onclick="saveBusiness()" type="button" class="btn btn-primary m-r-10">保存</button>
    </div>
</form>


<div id="addContractClassificView" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="myModalLabel">添加主营分类</h4>
            </div>
            <div class="modal-body">
                <div class="row form-group">
                    <label class="control-label col-sm-2"><span style="color: red">*</span>主营分类</label>
                    <div class="col-sm-10">
                        <ul id="addContractClassificTree" class="ztree"></ul>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="addContractClassific()">添加</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal-dialog -->
</div>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_msg_title">操作提示</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->


<div id="modal_import"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_import_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_import_title">合同文件导入</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <div class="form-group">
                        <label class="control-label col-sm-2">${uiLabelMap.SelectFile}:</label>
                        <div class="col-sm-10 uploadFile">
                            <input type="text" class="form-control w-p80" style="float: left" disabled id="showFileName">
                            <input type="hidden" id="hidFileName" />
                            <input type="button" class="btn btn-default m-l-5" id="btnUpload" value="${uiLabelMap.Upload}" />
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="upload" type="button" class="btn btn-primary">${uiLabelMap.Import}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div>

<!-- script区域start -->
<script>
    /*签约品牌JS  start*/
    var chosedContracBrand = [];//已经被选中的

    $(document).on("click", '#addContractBrandUL .icon-del', function (e) {
        var productBrandId = $(e.target).closest('li').attr("productBrandId");
        $("#addContractBrandUL").find('li[productBrandId="' + productBrandId + '"]').eq(0).remove();
    });

    var uploadOption =
            {
                action: "/businessmgr/control/uploadPartyContent",  // 提交目标
//                action: "/content/control/questionImport",  // 提交目标
                name: "uploadedFile",       // 服务端接收的名称
                autoSubmit: false,          // 是否自动提交
                data:{
                    localString:"zh",
                    partyContentTypeId:"DOCUMENT"
                },
                responseType:"json",
                // 选择文件之后…
                onChange: function (file, extension)
                {
                    $('#showFileName').val(file);
                },
                // 开始上传文件
                onSubmit: function (file, extension)
                {
                    console.log(file)
                },
                // 上传完成之后
                onComplete: function (file, response)
                {
                    $("#contractDoc").val(response.contentId);
                    $("#contractDocName").text(file);
                    $('#modal_import').modal("hide");

                }
            }
    // 初始化图片上传框
    var au = new AjaxUpload($('#btnUpload'), uploadOption);
    // 导入按钮点击事件
    $('#upload').click(function(){
        au.submit();
    });
    function addContracBrand() {
        $.dataSelectModal({
            url: "ProductBrandListMultiModalPage${externalKeyParam}",
            width: "800",
            title: "选择签约品牌",
            selectId: "linkId",
            selectName: "selectName",
            multi: true,
            selectCallBack: function (el) {
                var productBrandIds = el.data('id');
                var productBrandName = el.data('name');
                var productBrandIdArr = productBrandIds.toString().split(",");
                var productBrandNameArr = productBrandName.toString().split(",");
                for (var i = 0; i < productBrandIdArr.length; i++) {
                    var productBrandId = productBrandIdArr[i];
                    if (!isInChosedContractBrand(productBrandId)) {
                        var item = "<li productBrandId=" + productBrandId + " class='current' title='" + productBrandNameArr[i] + "'>" + productBrandNameArr[i] + "<span class='icon-del'>×</span></li>"
                        $("#addContractBrandUL").append(item)
                    }
                }
            }
        });
    }

    function isInChosedContractBrand(productBrandId) {
        var items = $("#addContractBrandUL").find("li");
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            if ($(item).attr("productBrandId") == productBrandId) {
                return true;
            }
        }
        return false;
    }

    /*签约品牌JS  end*/


    /*主营分类JS  start*/
    var chosedContractClassific = [];//已经被选中的
    $(document).on("click", '#addContractClassificUL .icon-del', function (e) {
        var categoryId = $(e.target).closest('li').attr("categoryId");
        $("#addContractClassificUL").find('li[categoryId="' + categoryId + '"]').eq(0).remove();
    });

    function addContractClassific() {
        //获取所有选中的子节点
        var treeObj = $.fn.zTree.getZTreeObj("addContractClassificTree");
        var nodes = treeObj.getCheckedNodes(true);
        for (var i = 0; i < nodes.length; i++) {
//            if(!nodes[i].isParent){
            //说明是子节点
            var categoryId = nodes[i].categoryId;
            var name = nodes[i].name;
            var parentCategoryId = nodes[i].parentCategoryId;
            if (!isInChosedContractClassific(categoryId)) {
                var item = "<li categoryId='" + categoryId + "' categoryName='"+name+"'  parentCategoryId='"+parentCategoryId+"'  class='current' title='" + name + "'>" + name + "<span class='icon-del'>×</span></li>"
                $("#addContractClassificUL").append(item)
            }
//            }
            $('#addContractClassificView').modal('hide');
        }
    }

    function isInChosedContractClassific(categoryId) {
        var items = $("#addContractClassificUL").find("li");
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            if ($(item).attr("categoryId") == categoryId) {
                return true;
            }
        }
        return false;
    }

    //获取被选中的主营分类
    function getChosedContractClassificIds() {
        var items = $("#addContractClassificUL").find("li");
        var choseditems = [];
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            choseditems.add($(item).attr("categoryId"))
        }
        return choseditems;
    }

    function addContractClassificView() {
        var zTreeObj;
        var setting = {
            check: {
                enable: true
            },
            data: {
                simpleData: {
                    enable: true
                }
            },
            view: {
                showIcon: false
            }
        };
        $.ajax({
            url: "getCategoryTree",
            type: "POST",
            data: {},
            dataType: "json",
            success: function (data) {
                var znodes = data.categoryList;
                $.fn.zTree.init($("#addContractClassificTree"), setting, znodes);

                var chosedItems = getChosedContractClassificIds();
                var treeObj = $.fn.zTree.getZTreeObj("addContractClassificTree");

                for (var i = 0; i < chosedItems.length; i++) {
                    treeObj.checkNode(treeObj.getNodesByParam("categoryId", chosedItems[i], null)[0], true, true);
                }
                $('#addContractClassificView').modal('show');
            },
            error: function (data) {
                console.log(data)
            }
        });


    }

    /*主营分类JS  end*/

    //营业执照所在地
    jQuery(document).ready(function () {
        if (jQuery('#${dependentForm}').length) {
            //      国家
            jQuery("#BusinessAddForm_countryGeoId1").change(function (e, data) {
                getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', 'BusinessAddForm_countryGeoId1', 'BusinessAddForm_stateProvinceGeoId1', '${responseName}', '${dependentKeyName}', '${descName}');
            });
            //    省
            jQuery("#${dependentForm}_stateProvinceGeoId1").change(function (e, data) {
                getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_stateProvinceGeoId1', '${dependentForm}_cityGeoId1', '${responseName}', '${dependentKeyName}', '${descName}');
            });
            /* 市*/
            jQuery("#${dependentForm}_cityGeoId1").change(function (e, data) {
                getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_cityGeoId1', '${dependentForm}_${dependentId2}1', '${responseName}', '${dependentKeyName}', '${descName}');
            });
            getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_countryGeoId1', '${dependentForm}_stateProvinceGeoId1', '${responseName}', '${dependentKeyName}', '${descName}', '${selectedDependentOption}');
            getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_stateProvinceGeoId1', '${dependentForm}_cityGeoId1', '${responseName}', '${dependentKeyName}', '${descName}', '${selectedDependentOption1}');
            getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_cityGeoId1', '${dependentForm}_countyGeoId1', '${responseName}', '${dependentKeyName}', '${descName}', '${selectedDependentOption2}');
        }
    })
    //开户行支行所在地
    jQuery(document).ready(function () {
        if (jQuery('#${dependentForm}').length) {
            //      国家
            jQuery("#BusinessAddForm_countryGeoId2").change(function (e, data) {
                getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', 'BusinessAddForm_countryGeoId2', 'BusinessAddForm_stateProvinceGeoId2', '${responseName}', '${dependentKeyName}', '${descName}');
            });
            //    省
            jQuery("#${dependentForm}_stateProvinceGeoId2").change(function (e, data) {
                getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_stateProvinceGeoId2', '${dependentForm}_cityGeoId2', '${responseName}', '${dependentKeyName}', '${descName}');
            });
            /* 市*/
            jQuery("#${dependentForm}_cityGeoId2").change(function (e, data) {
                getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_cityGeoId2', '${dependentForm}_${dependentId2}2', '${responseName}', '${dependentKeyName}', '${descName}');
            });
            getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_countryGeoId2', '${dependentForm}_stateProvinceGeoId2', '${responseName}', '${dependentKeyName}', '${descName}', '${selectedDependentOption}');
            getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_stateProvinceGeoId2', '${dependentForm}_cityGeoId2', '${responseName}', '${dependentKeyName}', '${descName}', '${selectedDependentOption1}');
            getDependentDropdownValues('<@ofbizUrl>${requestName}</@ofbizUrl>', 'countryGeoId', '${dependentForm}_cityGeoId2', '${dependentForm}_countyGeoId2', '${responseName}', '${dependentKeyName}', '${descName}', '${selectedDependentOption2}');
        }
    })


    var select_id;
    $(function () {
        var chooseImageModal;
        var imgGroupId;

        $('.form_datetime').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });

        $('.form_date').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            minView: 2,
            forceParse: 0
        });

        $('.form_time').datetimepicker({
            language: 'zh-CN',
            autoclose: 1,
            startView: 1,
            forceParse: 0
        });
        //初始化select2
        $(".select2").select2({
            closeOnSelect: false
        });
        $('#party_search').click(function(e){
            $.dataSelectModal({
                url: "/membermgr/control/personListModalPage?externalLoginKey=${externalLoginKey}",
                width:	"800",
                title:	"选择会员",
                selectId: "partyId",
                selectCallBack: function(el){
                    //检查该用户是否已经是商家
                    $.ajax({
                        url: "checkPartyIsBusiness",
                        type: "POST",
                        data : {partyId:el.data('id')},
                        dataType : "json",
                        success: function(data){
                            if(!data.status){
                                $("#BusinessAddForm").dpValidate({
                                    clear: true
                                });
                                $(e.target).closest('div.form-group').addClass('has-error');
                                $('#party_msg').html('该用户已是商家！');
                                $("#memberId").val("")
                            }else{
                                $(e.target).closest('div.form-group').removeClass('has-error');
                                $('#party_msg').html('');
                                $("#memberId").val(el.data('id'))
                            }
                        },
                        error: function(data){
                            $.tipLayer("操作失败！");
                        }
                    });
                }
            });
        });
        // 初始化图片选择
        $.chooseImage.int({
            userId: '',
            serverChooseNum: 1,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });

        //图片保存按钮事件
        $('body').on('click', '.img-submit-btn', function () {
            var obj = $.chooseImage.getImgData();
            obj.imgGroupId = imgGroupId;
            $.chooseImage.choose(obj, function (data) {
                $('#' + chooseImageModal).val(data.uploadedFile0);
                $('#img_' + chooseImageModal).attr({"src": "/content/control/getImage?contentId=" + data.uploadedFile0});
            })
        });


        $('#logoImgUpload').click(function () {
            chooseImageModal = "logoImgContentId";
            imgGroupId = "PRIVATE_IMG_GROUP";
            $.chooseImage.show();
        });
        // 身份证正面图片选择控件显示
        $('#idCardProsImgUpload').click(function () {
            chooseImageModal = "idCardProsImgContentId";
            imgGroupId = "PRIVATE_IMG_GROUP";
            $.chooseImage.show();
        });

        // 身份证反面图片选择控件显示
        $('#idCardConsImgUpload').click(function () {
            chooseImageModal = "idCardConsImgContentId";
            imgGroupId = "PRIVATE_IMG_GROUP";
            $.chooseImage.show();
        });
        //相关资质
        $('#qualifImgUpload').click(function () {
            chooseImageModal = "qualifImgContentId";
            imgGroupId = "PRIVATE_IMG_GROUP";
            $.chooseImage.show();
        });

        // 营业执照图片选择控件显示
        $('#businessLicenseImgUpload').click(function () {
            chooseImageModal = "busiImgContentId";
            imgGroupId = "PRIVATE_IMG_GROUP";
            $.chooseImage.show();
        });



        //表单校验方法

        $("#contractDocBut").click(function(){
            $('#modal_import').modal();
        })


    });

    var flag = true;

    $("#BusinessAddForm").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (flag) {
                addBusiness();
            }
        }
    });

    function saveBusiness() {
        $("#BusinessAddForm").dpValidate({
            clear: true
        });
        $("#BusinessAddForm").submit();

    }

    function addBusiness() {
        var idCardProsImgContentId= $("#idCardProsImgContentId").val();
        if(idCardProsImgContentId==null||idCardProsImgContentId==""){
            $.tipLayer("请上传身份证正面！");
            return;
        }
        var idCardConsImgContentId= $("#idCardConsImgContentId").val();
        if(idCardConsImgContentId==null||idCardConsImgContentId==""){
            $.tipLayer("请上传身份证反面！");
            return;
        }
        var busiImgContentId= $("#busiImgContentId").val();
        if(busiImgContentId==null||busiImgContentId==""){
            $.tipLayer("请上传营业执照！");
            return;
        }
        var logoImgContentId= $("#logoImgContentId").val();
        if(logoImgContentId==null||logoImgContentId==""){
            $.tipLayer("请上传店铺logo！");
            return;
        }
        //获取合同附件
        var contractDoc= $("#contractDoc").val();
        //TODO
        if(contractDoc==null||contractDoc==""){
            $.tipLayer("请上传合同附件！");
            return;
        }

        //获取主营分类
        var partyCategoryArrs ="";
        var items = $("#addContractClassificUL").find("li");
        if(items.length==0){
            $.tipLayer("请选择主营分类！");
            return;
        }
        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            var categoryId = $(item).attr("categoryId");
            var categoryName =$(item).attr("categoryName");
            var parentCategoryId = $(item).attr("parentCategoryId");
            var partyCategory =categoryId+","+categoryName+","+parentCategoryId;
            partyCategoryArrs=partyCategoryArrs+partyCategory+"|";
        }

        var partyBrandArrs ="";
        var brandItems = $("#addContractBrandUL").find("li");
        for (var i = 0; i < brandItems.length; i++) {
            var item = brandItems[i];
            var categoryId = $(item).attr("productBrandId");
            partyBrandArrs=partyBrandArrs+categoryId+"|";
        }


        var data = {
            memberId:$("#memberId").val(),//会员id
            partyName: $("#partyName").val(),//商家名称
            province: $('#BusinessAddForm #BusinessAddForm_stateProvinceGeoId').val(),
            city: $('#BusinessAddForm #BusinessAddForm_cityGeoId').val(),
            county: $('#BusinessAddForm #BusinessAddForm_countyGeoId').val(),
            address: $('#BusinessAddForm #address').val(),
            statusId: $("input[name='statusId']:checked").val(),//是否启用
            description: $("#description").val(),//商家描述
            companyName: $("#companyName").val(),//公司名称
            companyCreateDate: $("#companyCreateDate").val(),//成立时间
            businessEndDate: $("#businessEndDate").val(),//营业期限
            leageName: $("#leageName").val(),//
            leageTel: $("#leageTel").val(),//
            leageEmail: $("#leageEmail").val(),//
            leageCardNo: $("#leageCardNo").val(),//
//            leageImgContentId: $("#leageImgContentId").val(),//
            idCardProsImg:idCardProsImgContentId,
            idCardConsImg:idCardConsImgContentId,
            qualifImg:$("#qualifImgContentId").val(),
            logoImg:$("#logoImgContentId").val(),
            contractDoc:contractDoc,
            socialCardNo: $("#socialCardNo").val(),//
            busiImgContentId: $("#busiImgContentId").val(),//
            busiProvince: $('#BusinessAddForm #BusinessAddForm_stateProvinceGeoId1').val(),
            busiCity:$('#BusinessAddForm #BusinessAddForm_cityGeoId1').val(),
            busiCounty:  $('#BusinessAddForm #BusinessAddForm_countyGeoId1').val(),
            busiAddress:  $('#BusinessAddForm #busiAddress').val(),
            bankAcountName: $("#bankAcountName").val(),//
            bankAccount: $("#bankAccount").val(),//
            bankBranchName: $("#bankBranchName").val(),//
            bankBranchAcount: $("#bankBranchAcount").val(),//
            branchProvince: $('#BusinessAddForm #BusinessAddForm_stateProvinceGeoId2').val(),
            branchCity: $('#BusinessAddForm #BusinessAddForm_cityGeoId2').val(),
            branchCounty: $('#BusinessAddForm #BusinessAddForm_countyGeoId2').val(),
            partyCategoryArrs:partyCategoryArrs,//主营分类
            partyBrandArrs:partyBrandArrs//签约品牌

        }
        console.log(data);
        $.ajax({
            url: "businessAdd${externalKeyParam}",
            type: "POST",
            data:data,
            dataType: "json",
            success: function (data) {
                if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                    $.tipLayer(data._ERROR_MESSAGE_);
                }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                    $.tipLayer(data._ERROR_MESSAGE_LIST_);
                }else{
                    $('#modal_msg #modal_msg_body').html("操作成功！");
                    $('#modal_msg').modal();
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location = '<@ofbizUrl>businessToAuditList</@ofbizUrl>';
                    })
                }
            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });

    }


</script><!-- script区域end -->
