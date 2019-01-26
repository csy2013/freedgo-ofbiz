<link rel="stylesheet"  href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>"     type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>"      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript"  src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<!-- 内容start -->

<div class="box box-info">
    <div class="box-body">
        <form class="form-horizontal" id="form" method="post" action="<@ofbizUrl>saveCategoryNavigation</@ofbizUrl>">
            <input name="navigationGroupId" value="${(navigationGroup.navigationGroupId)!''}" type="hidden" />
            <div class="row m-b-12">
                <div class="col-sm-3">
                    <label class="form-group col-sm-12 control-label"><i class="required-mark">*</i>分类导航名称</label>
                </div>
                <div class="col-sm-3">
                    <div class="form-group col-sm-12" data-type="required" data-mark="分类导航名称">
                        <input type="text" class="form-control dp-vd" name="navigationGroupName" value="${(navigationGroup.navigationGroupName)!''}"/>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="col-sm-6">

                </div>
            </div>
            <div class="row m-b-12">
                <div class="col-sm-3">
                    <label class="form-group col-sm-12 control-label"><i class="required-mark">*</i>是否启用</label>
                </div>
                <div class="col-sm-9" style="margin-top: 5px">
                    <input type="radio" name="isEnabled" value="1" <#if (navigationGroup.isEnable)?default("1")== "1">checked </#if> >是&nbsp;&nbsp;&nbsp;&nbsp;
                    <input type="radio" name="isEnabled" value="0" <#if (navigationGroup.isEnable)?default("1")== "0">checked </#if>>否
                </div>
            </div>
            <div class="row m-b-12">
                <div class="col-sm-3">
                    <label class="form-group col-sm-12 control-label"><i class="required-mark">*</i>是否显示品牌</label>
                </div>
                <div class="col-sm-9" style="margin-top: 5px">
                    <input type="radio" name="isShowBrand" value="1" <#if (navigationGroup.isShowBrand)?default("1")== "1">checked </#if> >是&nbsp;&nbsp;&nbsp;&nbsp;
                    <input type="radio" name="isShowBrand" value="0" <#if (navigationGroup.isShowBrand)?default("1")== "0">checked </#if> >否
                </div>
            </div>
            <div class="row m-b-12">
                <!-- 去掉class可对齐，但无法验证 -->
                <div class="form-group col-sm-12"  data-type="required" data-mark="站点">
                    <div class="col-sm-3">
                        <label class="form-group col-sm-12 control-label"><i class="required-mark">*</i>站点</label>
                    </div>
                    <div class="col-sm-4">
                        <input type = "hidden" value = "" name="webSiteIds" id = "webSiteIds"/>
                        <select id="webSiteId" name="webSiteId" class="form-control select2WebSite dp-vd"
                                multiple="multiple" data-placeholder="请选择站点">
                        <#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >
                        <#if webSiteList?has_content>
                            <#list webSiteList as webSite>
                                <option value="${webSite.webSiteId}">${webSite.siteName}</option>
                            </#list>
                        </#if>
                        </select>
                        <p class="dp-error-msg"></p>

                    </div>
                    <div class="col-sm-3">
                        <div class="checkbox">
                            <label>
                                <input id="isAllSite" name="isAllSite" value="1" type="checkbox">所有站点
                            </label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row m-b-12">
                <div class="col-sm-3">

                </div>
                <div class="col-sm-6" style="margin-top: 10px">
                    <div class="dp-tables_btn">
                        <button id="save" class="btn btn-primary">
                            <i class="fa">保存</i>
                        </button>
                    </div>
                </div>
            </div>
        </form>

    </div>
</div>

<script>
    $(function () {
        $(".select2WebSite").select2();
        //所有站点的选中事件
        $('#isAllSite').change(function(){
            if($(this).prop("checked")){
                $("#webSiteId").val(null).trigger("change");
                $("#webSiteId").prop("disabled", true);
                $("#webSiteId").removeClass('dp-vd');
            }else{
                $("#webSiteId").prop("disabled", false);
                $("#webSiteId").addClass('dp-vd');
            }
        });
    <#if navigationGroup?has_content>
        //初始化站点选择框
        <#if navigationGroup.isAllSite?default("0") == "1">
            $('#isAllSite').prop("checked","true");
            $("#webSiteId").prop("disabled", true);
            $("#webSiteId").removeClass('dp-vd');
        <#else>
            // 初始化站点
            var webSiteTemp="";
            <#if webSiteListForEdit?has_content >
                <#list webSiteListForEdit as website>
                    var webSiteId = '${website.siteId}';
                    //$('.select2WebSite').val(webSiteId).trigger("change");
                    <#if website_index!=webSiteListForEdit.size()-1>
                        webSiteTemp=webSiteTemp+webSiteId+",";
                    <#else>
                        webSiteTemp=webSiteTemp+webSiteId;
                    </#if>
                </#list>
            </#if>
            $(".select2WebSite").val(webSiteTemp.split(',')).trigger("change");
        </#if>
    </#if>
    });

    $('#save').click(function () {
        $('#form').dpValidate({
            clear: true
        });
    });

    $('#form').dpValidate({
        validate: true,
        callback: function () {
            // 站点ID
            var webSiteIds = $('#webSiteId').val() !=null ? $('#webSiteId').val().join(',') : '';
            $("#webSiteIds").val(webSiteIds);
            document.getElementById('form').submit();
        }
    });
</script>