<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<#assign commonUrl = "floorManager?lookupFlag=Y&">
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<div class="box box-info">
<#--<div class="box-header with-border">
    <h3 class="box-title">${uiLabelMap.searchCondition}</h3>
</div>-->
    <div class="box-body">
    <#--<form class="form-inline clearfix" role="form" method="post" action="<@ofbizUrl>floorList</@ofbizUrl>">
        <input type="hidden" name="lookupFlag" value="Y">

        <div class="form-group">
            <div class="input-group m-b-10">
                <span class="input-group-addon">${uiLabelMap.roleId}</span>
                <input type="text" class="form-control" name="groupId" placeholder="${uiLabelMap.roleName}"
                       value="${groupId?default("")}">
            </div>
            <div class="input-group m-b-10">
                <span class="input-group-addon">${uiLabelMap.roleName}</span>
                <input type="text" class="form-control" name="name" placeholder="${uiLabelMap.roleName}"
                       value="${name?default("")}">
            </div>
            <div class="input-group m-b-10">
                <span class="input-group-addon">${uiLabelMap.description}</span>
                <input type="text" class="form-control" name="description" placeholder="${uiLabelMap.description}"
                       value="${description?default("")}">
            </div>
        </div>
        <div class="input-group pull-right">
            <button class="btn btn-success btn-flat">${uiLabelMap.CommonView}</button>
        </div>
    </form>-->
    <#--<div class="cut-off-rule bg-gray"></div>-->
        <div class="row m-b-10">
            <div class="col-sm-6">
            <div class="dp-tables_btn">
            <#if security.hasEntityPermission("FLOOR", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary">
                    <i class="fa fa-plus"></i>
                ${uiLabelMap.CommonAdd}
                </button>
            </#if>
            <#if security.hasEntityPermission("FLOOR", "_DELETE", session)>
                <button id="btn_del" class="btn btn-primary">
                    <i class="fa fa-trash"></i>${uiLabelMap.CommonDelete}
                </button>
            </div>
            </#if>
            </div>
        <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
        <#if floorList?has_content>

            <div class="col-sm-6">
                <div class="dp-tables_length">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                onchange="location.href='${commonUrl1}VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                            <option value="10" <#if viewSize ==10>selected</#if>>10</option>
                            <option value="20" <#if viewSize==20>selected</#if>>20</option>
                            <option value="30" <#if viewSize==30>selected</#if>>30</option>
                            <option value="40" <#if viewSize==40>selected</#if>>40</option>
                        </select>
                        条
                    </label>
                </div>

            </div>
        </#if>
        </div>

    <#if floorList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>楼层分类
                            <#if orderFiled == 'categoryName'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc"
                                       href="${commonUrl}ORDER_FILED=categoryName&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc"
                                       href="${commonUrl}ORDER_FILED=categoryName&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc"
                                   href="${commonUrl}ORDER_FILED=categoryName&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>楼层名称
                            <#if orderFiled == 'floorName'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc"
                                       href="${commonUrl}ORDER_FILED=floorName&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc"
                                       href="${commonUrl}ORDER_FILED=floorName&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted  fa-sort-amount-desc"
                                   href="${commonUrl}ORDER_FILED=floorName&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>楼层图片</th>
                        <th>是否启用</th>
                        <th>楼层
                            <#if orderFiled == 'sequenceNum'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc"
                                       href="${commonUrl}ORDER_FILED=sequenceNum&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc"
                                       href="${commonUrl}ORDER_FILED=sequenceNum&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc"
                                   href="${commonUrl}ORDER_FILED=sequenceNum&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>使用站点</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list floorList as floor>

                        <tr>
                            <td><input value="${floor.floorId?if_exists}" class="js-checkchild" type="checkbox"></td>
                            <td>${floor.categoryName?default("N/A")}</td>
                            <td>${floor.floorName?default("N/A")}</td>
                            <td><#if floor.imgUrl?has_content><img
                                    src="${floor.imgUrl}" height="100"
                                    class="cssImgSmall"></#if></td>
                            <td>
                                <#if (floor.isEnabled)?default("N") == "N">
                                    <button type='button' class='btn btn-sm btn-sm' value="N"
                                            id="${floor.floorId}" onclick="changeEnabled(this)">否
                                    </button>
                                <#else>
                                    <button type='button' class='btn btn-primary btn-sm' value="Y"
                                            id="${floor.floorId}" onclick="changeEnabled(this)">是
                                    </button>
                                </#if>
                            </td>
                            <td>${floor.sequenceNum?default("N/A")}F</td>
                            <td>${floor.siteNames?default("N/A")}</td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("FLOOR", "_UPDATE", session)>
                                        <button type="button" class="btn btn-danger btn-sm"
                                                onclick="editFloor('${floor.floorId?default("N/A")}')">编辑
                                        </button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if security.hasEntityPermission("FLOOR", "_DELETE", session)>
                                            <li><a href="#" onclick="delFloor('${floor.floorId?default("N/A")}')">删除</a>
                                            </li>
                                        </#if>
                                        <#if security.hasEntityPermission("FLOOR_LABEL", "_VIEW", session)>
                                            <li>
                                                <a href="<@ofbizUrl>floorLabelManager</@ofbizUrl>${externalKeyParam}&floorId=${floor.floorId}">楼层标签</a>
                                            </li>
                                        </#if>
                                        <#if security.hasEntityPermission("FLOOR_PRODUCT", "_VIEW", session)>
                                            <li>
                                                <a href="<@ofbizUrl>floorProductManager</@ofbizUrl>${externalKeyParam}&floorId=${floor.floorId}">楼层商品</a>
                                            </li>
                                        </#if>
                                        <#if security.hasEntityPermission("FLOOR_BANNER", "_VIEW", session)>
                                            <li>
                                                <a href="<@ofbizUrl>floorBannerManager</@ofbizUrl>${externalKeyParam}&floorId=${floor.floorId}">楼层广告</a>
                                            </li>
                                        </#if>
                                        <#if security.hasEntityPermission("FLOOR_BRAND", "_VIEW", session)>
                                            <li>
                                                <a href="<@ofbizUrl>floorBrandManager</@ofbizUrl>${externalKeyParam}&floorId=${floor.floorId}">楼层品牌</a>
                                            </li>
                                        </#if>
                                    </ul>
                                </div>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(floorListSize, viewSize) />

        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", floorListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=floorListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />

    <#else>
        <div class="row">
            <div class="col-sm-10">
                <h3>无显示记录。</h3>
            </div>
        </div>
    </#if>
    </div>
    <!-- /.box-body -->
</div>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
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
</div>
<!-- 提示弹出框end -->

<!-- 删除确认弹出框start -->
<div id="modal_confirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default"
                        data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
                <button id="ok" type="button" class="btn btn-danger"
                        data-dismiss="modal">${uiLabelMap.BrandDel}</button>
            </div>
        </div>
    </div>
</div>
<!-- 删除确认弹出框end -->

<!-- add user Modal -->
<div id="modal_add" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal_add_title">>
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 id="header" class="modal-title">新增楼层</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="addForm" action="">
                    <input id="floorId" name="floorId" type="hidden">
                    <div class="form-group" data-type="required" data-mark="楼层分类">
                        <label for="productCategoryId" class="control-label col-sm-2"><i class="required-mark">*</i>楼层分类:</label>
                        <div class="col-sm-10">
                        <#assign productCategorys = Static["com.qihua.ofbiz.floor.FloorServices"].getFirstCategory(delegator)>
                            <select class="form-control dp-vd" name="productCategoryId" id="productCategoryId">
                            <#if productCategorys?has_content && (productCategorys?size > 0)>
                                <#list productCategorys as productCategory>
                                    <option value="${productCategory.productCategoryId}">${productCategory.categoryName}</option>
                                </#list>
                            </#if>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="楼层名称">
                        <label for="floorName" class="control-label col-sm-2"><i
                                class="required-mark">*</i>楼层名称:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" name="floorName" id="floorName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="楼层图片">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>楼层图片:</label>
                        <div class="col-sm-7">
                            <input type="text" class="form-control dp-vd" id="imgUrl" name="imgUrl" class="dp-vd"/>
                            <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                            <p class="dp-error-msg"></p>
                        </div>
                        <div class="col-sm-3">
                            <input style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile"
                                   value="选择图片"/>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用:</label>
                        <div class="radio col-sm-10">
                            <label class="col-sm-6"><input id="enabled1" name="isEnabled" type="radio" checked
                                                           value="Y">是</label>
                            <label class="col-sm-6"><input id="enabled2" name="isEnabled" type="radio"
                                                           value="N">否</label>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="楼层">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>楼层:</label>
                        <div class="col-sm-9">
                            <select class="form-control dp-vd" id="sequenceNum" name="sequenceNum">
                                <option value="1">1F</option>
                                <option value="2">2F</option>
                                <option value="3">3F</option>
                                <option value="4">4F</option>
                                <option value="5">5F</option>
                                <option value="6">6F</option>
                                <option value="7">7F</option>
                                <option value="8">8F</option>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="站点">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
                        <div class="col-sm-7">
                            <select id="webSiteIds" name="webSiteIds" class="form-control select2 dp-vd"
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
                        <div class="col-sm-3" style="padding-left: 0px;">
                            <div class="checkbox">
                                <label>
                                    <input id="isAllWebSite" type="checkbox">所有站点
                                </label>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" id="save">保存</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript">
    var floorIds;
    $(function () {
        //初始化select2
        $(".select2").select2({
            closeOnSelect: false
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
            $.chooseImage.choose(obj, function (data) {
                $('#imgUrl').val("/content/control/getImage?contentId=" + data.uploadedFile0);
                $('#img').attr({"src": "/content/control/getImage?contentId=" + data.uploadedFile0});
                $('#img').show();
            })
        });

        // 图片选择控件显示
        $('#uploadedFile').click(function () {
            $.chooseImage.show();
        });

        $('#imgUrl').bind('input propertychange', function () {
            if ($('#imgUrl').val() == '') {
                $('#img').hide();
            } else {
                $('#img').attr('src', $('#imgUrl').val());
                $('#img').show();
            }
        });

        $('#btn_add').click(function () {
            $('#addForm').attr("action", "<@ofbizUrl>addFloor</@ofbizUrl>");
            $("#header").html('新增楼层');
            $("#productCategoryId").val('');
            $("#floorName").val('');
            $('#img').attr('src', '');
            $('#imgUrl').val('');
            $('#enabled1').click();
            $('#sequenceNum').val('');
            $("#webSiteIds").val(null).trigger("change");
            $("#webSiteIds").prop("disabled", false);
            $("#webSiteIds").addClass('dp-vd');
            $('#isAllWebSite').checkbox = false;
            $("#addForm").dpValidate({
                clear: true
            });
            $('#modal_add').modal('show');
        });
        //所有站点的选中事件
        $('#isAllWebSite').change(function () {
            if ($(this).prop("checked")) {
                $("#webSiteIds").val(null).trigger("change");
                $("#webSiteIds").prop("disabled", true);
                $("#webSiteIds").removeClass('dp-vd');
            } else {
                $("#webSiteIds").prop("disabled", false);
                $("#webSiteIds").addClass('dp-vd');
            }
        });
        $('#save').click(function () {
            $("#addForm").dpValidate({
                clear: true
            });
            $("#addForm").submit();
        });
        //表单校验方法
        $('#addForm').dpValidate({
            validate: true,
            callback: function () {
                var floorId = $('#floorId').val();
                var productCategoryId = $('#productCategoryId').val();
                var floorName = $('#floorName').val();
                var imgUrl = $('#imgUrl').val();
                var isEnabled = $('input[name="isEnabled"]:checked').val();
                var sequenceNum = $('#sequenceNum').val();
                var webSiteIds = $('#webSiteIds').val() != null ? $('#webSiteIds').val().join(',') : '';
                var isAllWebSite = $('#isAllWebSite').is(':checked') ? 'Y' : 'N';

                $.ajax({
                    url: $('#addForm').attr("action"),
                    type: "POST",
                    data: {
                        floorId: floorId,
                        productCategoryId: productCategoryId,
                        floorName: floorName,
                        imgUrl: imgUrl,
                        isEnabled: isEnabled,
                        sequenceNum: sequenceNum,
                        webSiteIds: webSiteIds,
                        isAllWebSite: isAllWebSite
                    },
                    dataType: "json",
                    success: function (data) {
                        if (data.error) {
                            $.tipLayer(data.error);
                        } else {
                            $.tipLayer("操作成功！");
                            window.location.reload();
                        }
                    },
                    error: function (data) {
                        $.tipLayer("操作失败！");
                    }
                });
            }
        });
        $('#btn_del').click(function () {
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if (checks.size() > 0) {
                //编辑id字符串
                var ids = "";
                checks.each(function () {
                    ids += $(this).val() + ",";
                });
                floorIds = ids;
                //设置删除弹出框内容
                $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
                $('#modal_confirm').modal('show');
            } else {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });
        //删除弹出框删除按钮点击事件
        $('#modal_confirm #ok').click(function (e) {
            //异步调用删除方法
            $.ajax({
                url: "<@ofbizUrl>removeFloor</@ofbizUrl>",
                type: "POST",
                data: {floorIds: floorIds},
                dataType: "json",
                success: function (data) {
                    delIds = "";
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作成功！");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.reload();
                    })
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作失败！");
                    $('#modal_msg').modal();
                }
            });
        });
    });
    function changeEnabled(obj) {
        var obj = $(obj);
        var isEnabled;
        var floorId = obj.attr("id");
        if (obj.attr("value") == "N") {
            obj.html("是");
            isEnabled = "Y";
            obj.attr("value", "Y");
            obj.addClass("btn-primary");
        } else {
            obj.html("否");
            isEnabled = "N";
            obj.attr("value", "N");
            obj.removeClass("btn-primary");
        }
        $.ajax({
            url: "<@ofbizUrl>editFloor</@ofbizUrl>",
            type: "POST",
            data: {
                floorId: floorId,
                isEnabled: isEnabled
            },
            dataType: "json",
            success: function (data) {
            },
            error: function (data) {
            }
        });
    }
    function delFloor(floorId) {
        floorIds = floorId;
        //设置删除弹出框内容
        $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
        $('#modal_confirm').modal('show');
    }
    function editFloor(floorId) {
        $.ajax({
            url: "<@ofbizUrl>floorDetail</@ofbizUrl>",
            type: "POST",
            data: {
                floorId: floorId
            },
            dataType: "json",
            success: function (data) {
                $('#addForm').attr("action", "<@ofbizUrl>editFloor</@ofbizUrl>");
                $("#header").html('新增楼层');
                $("#floorId").val(data.floorId);
                $("#productCategoryId").val(data.productCategoryId);
                $("#floorName").val(data.floorName);
                $('#img').attr('src', data.imgUrl);
                $('#imgUrl').val(data.imgUrl);
                if (data.isEnabled == 'Y') {
                    $('#enabled1').click();
                } else {
                    $('#enabled2').click();
                }
                $('#sequenceNum').val(data.sequenceNum);
                $("#webSiteIds").val(data.webSiteIds.split(',')).trigger("change");
                $('#isAllWebSite').checkbox = false;
                $("#addForm").dpValidate({
                    clear: true
                });
                $('#modal_add').modal('show');
            },
            error: function (data) {
            }
        });
    }
</script>