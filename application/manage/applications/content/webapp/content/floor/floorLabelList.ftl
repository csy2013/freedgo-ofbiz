<#assign commonUrl = "floorLabelManager?floorId="+parameters.floorId+"&lookupFlag=Y&">
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<div class="box box-info">
    <div class="box-body">
        <div class="row m-b-10">
            <div class="col-sm-6">
            <div class="dp-tables_btn">
            <#if security.hasEntityPermission("FLOOR_LABEL", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary">
                    <i class="fa fa-plus"></i>
                ${uiLabelMap.CommonAdd}
                </button>
            </#if>
            <#if security.hasEntityPermission("FLOOR_LABEL", "_DELETE", session)>
                <button id="btn_del" class="btn btn-primary">
                    <i class="fa fa-trash"></i>${uiLabelMap.CommonDelete}
                </button>
                <button class="btn btn-primary"
                        onclick="window.location.href='<@ofbizUrl>floorManager</@ofbizUrl>${externalKeyParam}'">
                    返回楼层
                </button>
            </div>
            </#if>
            </div>
        <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
        <#if floorLabelList?has_content>
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

    <#if floorLabelList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
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
                        <th>标签名称
                            <#if orderFiled == 'floorLabelName'>
                                <#if orderBy == 'DESC'>
                                    <a class="fa fa-sort-amount-desc"
                                       href="${commonUrl}ORDER_FILED=floorLabelName&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc"
                                       href="${commonUrl}ORDER_FILED=floorLabelName&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted  fa-sort-amount-desc"
                                   href="${commonUrl}ORDER_FILED=floorLabelName&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>是否显示标签广告</th>
                        <th>是否显示标签品牌</th>
                        <th>是否启用</th>
                        <th>序号
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
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list floorLabelList as floorLabel>

                        <tr>
                            <td><input value="${floorLabel.floorLabelId?if_exists}" class="js-checkchild"
                                       type="checkbox"></td>
                            <td>${floorLabel.floorName?default("N/A")}</td>
                            <td>${floorLabel.floorLabelName?default("N/A")}</td>
                            <td>
                                <#if (floorLabel.showBanner)?default("N") == "N">
                                    <button type='button' class='btn btn-sm' value="N"
                                            id="${floorLabel.floorLabelId}" onclick="changeEnabled(this,'showBanner')">否
                                    </button>
                                <#else>
                                    <button type='button' class='btn btn-primary btn-sm' value="Y"
                                            id="${floorLabel.floorLabelId}" onclick="changeEnabled(this,'showBanner')">是
                                    </button>
                                </#if>
                            </td>
                            <td>
                                <#if (floorLabel.showBrand)?default("N") == "N">
                                    <button type='button' class='btn btn-sm' value="N"
                                            id="${floorLabel.floorLabelId}" onclick="changeEnabled(this,'showBrand')">否
                                    </button>
                                <#else>
                                    <button type='button' class='btn btn-primary btn-sm' value="Y"
                                            id="${floorLabel.floorLabelId}" onclick="changeEnabled(this,'showBrand')">是
                                    </button>
                                </#if>
                            </td>
                            <td>
                                <#if (floorLabel.isEnabled)?default("N") == "N">
                                    <button type='button' class='btn btn-sm' value="N"
                                            id="${floorLabel.floorLabelId}" onclick="changeEnabled(this,'isEnabled')">否
                                    </button>
                                <#else>
                                    <button type='button' class='btn btn-primary btn-sm' value="Y"
                                            id="${floorLabel.floorLabelId}" onclick="changeEnabled(this,'isEnabled')">是
                                    </button>
                                </#if>
                            </td>
                            <td>${floorLabel.sequenceNum?default("N/A")}</td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("FLOOR_LABEL", "_UPDATE", session)>
                                        <button type="button" class="btn btn-danger btn-sm"
                                                onclick="editFloor('${floorLabel.floorLabelId?default("N/A")}')">编辑
                                        </button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if security.hasEntityPermission("FLOOR_LABEL", "_DELETE", session)>
                                            <li><a href="#"
                                                   onclick="delFloor('${floorLabel.floorLabelId?default("N/A")}')">删除</a>
                                            </li>
                                        </#if>
                                        <#if security.hasEntityPermission("FLOOR_LABEL_PRODUCT", "_VIEW", session)>
                                            <li>
                                                <a href="<@ofbizUrl>floorLabelProductManager</@ofbizUrl>${externalKeyParam}&floorId=${floorLabel.floorId}&floorLabelId=${floorLabel.floorLabelId}">设置标签商品</a>
                                            </li>
                                        </#if>
                                        <#if security.hasEntityPermission("FLOOR_LABEL_BANNER", "_VIEW", session)>
                                            <li>
                                                <a href="<@ofbizUrl>floorLabelBannerManager</@ofbizUrl>${externalKeyParam}&floorId=${floorLabel.floorId}&floorLabelId=${floorLabel.floorLabelId}">设置标签广告</a>
                                            </li>
                                        </#if>
                                        <#if security.hasEntityPermission("FLOOR_LABEL_BRAND", "_VIEW", session)>
                                            <li>
                                                <a href="<@ofbizUrl>floorLabelBrandManager</@ofbizUrl>${externalKeyParam}&floorId=${floorLabel.floorId}&floorLabelId=${floorLabel.floorLabelId}">设置标签品牌</a>
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
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(floorLabelListSize, viewSize) />

        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", floorLabelListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=floorLabelListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
                <h4 id="header" class="modal-title">新增楼层标签</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="addForm" action="">
                    <input id="floorLabelId" name="floorLabelId" type="hidden">
                    <input id="floorId" name="floorId" type="hidden" value="${parameters.floorId}">
                    <div class="form-group" data-type="required" data-mark="标签名称">
                        <label for="floorLabelName" class="control-label col-sm-2"><i
                                class="required-mark">*</i>标签名称:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" name="floorLabelName" id="floorLabelName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否显示标签广告:</label>
                        <div class="radio col-sm-10">
                            <label class="col-sm-6"><input id="banner1" name="showBanner" type="radio" checked
                                                           value="Y">是</label>
                            <label class="col-sm-6"><input id="banner2" name="showBanner" type="radio"
                                                           value="N">否</label>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否显示标签品牌:</label>
                        <div class="radio col-sm-10">
                            <label class="col-sm-6"><input id="brand1" name="showBrand" type="radio" checked
                                                           value="Y">是</label>
                            <label class="col-sm-6"><input id="brand2" name="showBrand" type="radio"
                                                           value="N">否</label>
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
                    <div class="form-group" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="sequenceNum" name="sequenceNum"/>
                            <p class="dp-error-msg"></p>
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
<script type="text/javascript">
    var floorLabelIds;
    $(function () {

        $('#btn_add').click(function () {
            $('#addForm').attr("action", "<@ofbizUrl>addFloorLabel</@ofbizUrl>");
            $("#header").html('新增楼层标签');
            $("#floorLabelId").val('');
            $("#floorLabelName").val('');
            $("#showBanner").val('');
            $('#showBrand').val('');
            $('#isEnabled').val('');
            $('#banner1').click();
            $('#brand1').click();
            $('#enabled1').click();
            $('#sequenceNum').val('');
            $("#addForm").dpValidate({
                clear: true
            });
            $('#modal_add').modal('show');
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
                var floorLabelId = $('#floorLabelId').val();
                var floorId = $('#floorId').val();
                var floorLabelName = $('#floorLabelName').val();
                var showBanner = $('input[name="showBanner"]:checked').val();
                var showBrand = $('input[name="showBrand"]:checked').val();
                var isEnabled = $('input[name="isEnabled"]:checked').val();
                var sequenceNum = $('#sequenceNum').val();

                $.ajax({
                    url: $('#addForm').attr("action"),
                    type: "POST",
                    data: {
                        floorLabelId: floorLabelId,
                        floorId: floorId,
                        floorLabelName: floorLabelName,
                        showBanner: showBanner,
                        showBrand: showBrand,
                        isEnabled: isEnabled,
                        sequenceNum: sequenceNum
                    },
                    dataType: "json",
                    success: function (data) {
                        $.tipLayer("操作成功！");
                        window.location.reload();
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
                floorLabelIds = ids;
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
                url: "<@ofbizUrl>removeFloorLabel</@ofbizUrl>",
                type: "POST",
                data: {floorLabelIds: floorLabelIds},
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
    function changeEnabled(obj, type) {
        var obj = $(obj);
        var value;
        var floorLabelId = obj.attr("id");
        if (obj.attr("value") == "N") {
            obj.html("是");
            value = "Y";
            obj.attr("value", "Y");
            obj.addClass("btn-primary");
        } else {
            obj.html("否");
            value = "N";
            obj.attr("value", "N");
            obj.removeClass("btn-primary");
        }
        if (type == 'showBanner') {
            $.ajax({
                url: "<@ofbizUrl>editFloorLabel</@ofbizUrl>",
                type: "POST",
                data: {
                    floorLabelId: floorLabelId,
                    showBanner: value
                },
                dataType: "json",
                success: function (data) {
                },
                error: function (data) {
                }
            });
        } else if (type == 'showBrand') {
            $.ajax({
                url: "<@ofbizUrl>editFloorLabel</@ofbizUrl>",
                type: "POST",
                data: {
                    floorLabelId: floorLabelId,
                    showBrand: value
                },
                dataType: "json",
                success: function (data) {
                },
                error: function (data) {
                }
            });
        } else if (type == 'isEnabled') {
            $.ajax({
                url: "<@ofbizUrl>editFloorLabel</@ofbizUrl>",
                type: "POST",
                data: {
                    floorLabelId: floorLabelId,
                    isEnabled: value
                },
                dataType: "json",
                success: function (data) {
                },
                error: function (data) {
                }
            });
        }
    }
    function delFloor(floorLabelId) {
        floorLabelIds = floorLabelId;
        //设置删除弹出框内容
        $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
        $('#modal_confirm').modal('show');
    }
    function editFloor(floorLabelId) {
        $.ajax({
            url: "<@ofbizUrl>floorLabelDetail</@ofbizUrl>",
            type: "POST",
            data: {
                floorLabelId: floorLabelId
            },
            dataType: "json",
            success: function (data) {
                $('#addForm').attr("action", "<@ofbizUrl>editFloorLabel</@ofbizUrl>");
                $("#header").html('编辑楼层标签');
                $("#floorLabelId").val(data.floorLabelId);
                $("#floorLabelName").val(data.floorLabelName);
                if (data.showBanner == 'Y') {
                    $('#banner1').click();
                } else {
                    $('#banner2').click();
                }
                if (data.showBrand == 'Y') {
                    $('#brand1').click();
                } else {
                    $('#brand2').click();
                }
                if (data.isEnabled == 'Y') {
                    $('#enabled1').click();
                } else {
                    $('#enabled2').click();
                }
                $('#sequenceNum').val(data.sequenceNum);
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