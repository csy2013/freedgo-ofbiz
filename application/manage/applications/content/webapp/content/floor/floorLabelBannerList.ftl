<#assign commonUrl = "floorLabelBannerManager?floorId="+parameters.floorId+"&floorLabelId="+parameters.floorLabelId+"&lookupFlag=Y&">
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<div class="box box-info">
    <div class="box-body">
        <div class="row m-b-10">
            <div class="col-sm-6">
            <div class="dp-tables_btn">
            <#if security.hasEntityPermission("FLOOR_LABEL_BANNER", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary">
                    <i class="fa fa-plus"></i>
                ${uiLabelMap.CommonAdd}
                </button>
            </#if>
            <#if security.hasEntityPermission("FLOOR_LABEL_BANNER", "_DELETE", session)>
                <button id="btn_del" class="btn btn-primary">
                    <i class="fa fa-trash"></i>${uiLabelMap.CommonDelete}
                </button>
                <button class="btn btn-primary"
                        onclick="window.location.href='<@ofbizUrl>floorLabelManager</@ofbizUrl>${externalKeyParam}&floorId=${parameters.floorId}'">
                    返回楼层标签
                </button>
            </div>
            </#if>
            </div>
        <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
        <#if floorLabelBannerList?has_content>
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

    <#if floorLabelBannerList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>图片</th>
                        <th>广告名称</th>
                        <th>链接地址</th>
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
                        <#list floorLabelBannerList as floorLabelBanner>
                        <tr>
                            <td><input value="${floorLabelBanner.floorLabelBannerId?if_exists}" class="js-checkchild"
                                       type="checkbox"></td>
                            <td><img height="100" src="${floorLabelBanner.imgUrl?if_exists}" class="cssImgSmall"
                                     alt=""/></td>
                            <td>${floorLabelBanner.bannerName?default("N/A")}</td>
                            <td>${floorLabelBanner.linkUrl?default("N/A")}</td>
                            <td>
                                <#if (floorLabelBanner.isEnabled)?default("N") == "N">
                                    <button type='button' class='btn btn-sm' value="N"
                                            id="${floorLabelBanner.floorLabelBannerId}" onclick="changeEnabled(this)">否
                                    </button>
                                <#else>
                                    <button type='button' class='btn btn-primary btn-sm' value="Y"
                                            id="${floorLabelBanner.floorLabelBannerId}" onclick="changeEnabled(this)">是
                                    </button>
                                </#if>
                            </td>
                            <td>${floorLabelBanner.sequenceNum?default("N/A")}</td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("FLOOR_LABEL_BANNER", "_UPDATE", session)>
                                        <button type="button" class="btn btn-danger btn-sm"
                                                onclick="editFloor('${floorLabelBanner.floorLabelBannerId?default("N/A")}')">
                                            编辑
                                        </button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if security.hasEntityPermission("FLOOR_LABEL_BANNER", "_DELETE", session)>
                                            <li><a href="#"
                                                   onclick="delFloor('${floorLabelBanner.floorLabelBannerId?default("N/A")}')">删除</a>
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
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(floorLabelBannerListSize, viewSize) />

        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", floorLabelBannerListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=floorLabelBannerListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
                <h4 id="header" class="modal-title">新增标签广告</h4>
            </div>
            <div class="modal-body">
                <form role="form" class="form-horizontal" id="addForm" action="">
                    <input id="floorLabelBannerId" name="floorLabelBannerId" type="hidden">
                    <input id="floorId" name="floorId" type="hidden" value="${parameters.floorId}">
                    <input id="floorLabelId" name="floorLabelId" type="hidden" value="${parameters.floorLabelId}">
                    <div class="form-group" data-type="required" data-mark="广告名称">
                        <label for="bannerName" class="col-sm-2 control-label"><i
                                class="required-mark">*</i>广告名称:</label>
                        <div class="radio col-sm-10">
                            <input type="text" class="form-control dp-vd" id="bannerName" name="bannerName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="广告图片">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>广告图片:</label>
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
                    <div class="form-group" data-type="required" data-mark="链接类型">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>链接类型:</label>
                        <div class="col-sm-10">
                            <div style="overflow:hidden;margin-bottom: 10px;">
                                <select id="firstLinkType" class="form-control dp-vd"
                                        style="width:200px;float: left;margin-right: 20px;">
                                    <option value="">===请选择链接类型===</option>
                                <#if linkTypes?has_content && (linkTypes?size > 0)>
                                    <#list linkTypes as linkType>
                                        <option value="${linkType.enumId}">${linkType.description}</option>
                                    </#list>
                                </#if>
                                </select>
                            </div>

                            <div id="selectViewDiv" style="margin-top: 10px;display:none;">
                                <span>已选择:</span>
                                <span id="selectName" style="margin-left: 10px;color: blue;cursor: pointer;"></span>
                                <input type="hidden" id="linkId"/>
                            </div>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div id="linkDiv" class="form-group" style="display:none;" data-type="required" data-mark="链接地址">
                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>链接地址:</label>
                        <div class="col-sm-10">
                            <div class="col-sm-5" style="padding-left: 0px;">
                                <input type="text" class="form-control dp-vd" id="linkUrl"/>
                            </div>
                            <p class="dp-error-msg"></p>
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
                    <div class="form-group">
                        <label class="col-sm-2 control-label">描述:</label>
                        <div class="radio col-sm-10">
                            <textarea class="form-control dp-vd" id="description" name="description"
                                      rows="3"></textarea>
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

    // 链接类型初始化
    (function () {

        window.initLinkType = function (data) {
            var linkType = data.firstLinkType
            var linkUrl = data.linkUrl
            var linkName = data.linkName
            var linkId = data.linkId

            $('#firstLinkType').val(linkType)
            $("#linkUrl").val(linkUrl)
            $('#addForm #selectName').html(linkName);
            $("#linkId").val(linkId)


            switch (linkType) {
                // 自定义链接
                case 'FLT_ZDYLJ':
                    $('#linkDiv').show();
                    $('#selectViewDiv').hide();
                    break;
                default:
                    if (linkUrl) {
                        $('#selectViewDiv').show();
                        $('#linkDiv').hide();
                    }
            }

        }


        // 链接类型选择链接回调处理
        var selectLinkHandler = function (type) {
            switch (type) {
                case 'FLT_WZLJ':
                {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "ArticleListModalPage",
                        width: "800",
                        title: "选择文章",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            $('#linkUrl').val('modalName=WZ&id=' + el.data('id'));
                        }
                    });
                }
                    break;
                case 'FLT_HDLJ':
                {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "/prodPromo/control/ProActivityMgrListModalPage?externalLoginKey=${externalLoginKey}",
                        width: "800",
                        title: "选择活动",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            $('#linkUrl').val('modalName=HD&id=' + el.data('id'));
                        }
                    });
                }
                    break;
            <#-- 自定义链接  -->
                case 'FLT_ZDYLJ':
                {
                    $('#selectViewDiv').hide();
                    $('#linkId').val('');
                    $('#selectName').html('');
                    $('#linkUrl').val('');
                    $('#linkDiv').show();
                }
                    break;
            <#-- 商品选择添加 -->
                case 'FLT_SPLJ':
                {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
                        width: "800",
                        title: "选择商品",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            $('#linkUrl').val('modalName=SP&id=' + el.data('id'));
                        }
                    });
                }
                    break;
                // 促销类型
                case 'FLT_CXLJ':
                {
                    $('#linkDiv').hide();
                    $.dataSelectModal({
                        url: "/prodPromo/control/PromoListModalPage?externalLoginKey=${externalLoginKey}",
                        width: "800",
                        title: "选择促销",
                        selectId: "linkId",
                        selectName: "selectName",
                        selectCallBack: function (el) {
                            $('#selectViewDiv').show();
                            var modalName = el.data('record').activityType;
                            $('#linkUrl').val('modalName=' + modalName + '&id=' + el.data('id'));
                        }
                    });
                }
                default:
                {
                    $('#linkId').val('');
                    $('#selectName').html('');
                    $('#linkUrl').val('');
                    $('#linkDiv').hide();
                    $('#selectViewDiv').hide();
                }
            }
        }

        //已选的名称点击事件
        $('#selectName').on('click', function () {
            var flt = $('#firstLinkType').val()
            selectLinkHandler(flt)
        });

        //链接地址一的选项切换事件
        $('#firstLinkType').on('change', function () {
            var type = $(this).val()
            selectLinkHandler(type)
        });

    })()


    var floorLabelBannerIds;
    $(function () {
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
            $('#addForm').attr("action", "<@ofbizUrl>addFloorLabelBanner</@ofbizUrl>");
            $("#header").html('新增标签广告');
            $("#floorLabelBannerId").val('');
            $("#bannerName").val('');
            $("#imgUrl").val('');
            $('#img').attr('src', '');
            $("#linkUrl").val('');
            $('#enabled1').click();
            $('#sequenceNum').val('');
            $('#description').val('');
            $("#addForm").dpValidate({
                clear: true
            });

            // 清除已经选择的链接类型
            $('#firstLinkType').val('');
            $('#linkId').val('');
            $('#selectName').html('');

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
                var floorLabelBannerId = $('#floorLabelBannerId').val();
                var floorId = $('#floorId').val();
                var floorLabelId = $('#floorLabelId').val();
                var bannerName = $('#bannerName').val();
                var imgUrl = $('#imgUrl').val();

                var firstLinkType = $('#firstLinkType').val();
                var linkUrl = $('#linkUrl').val();
                var linkId = $('#linkId').val();
                var linkName = $('#selectName').html();

                var isEnabled = $('input[name="isEnabled"]:checked').val();
                var sequenceNum = $('#sequenceNum').val();
                var description = $('#description').val();

                $.ajax({
                    url: $('#addForm').attr("action"),
                    type: "POST",
                    data: {
                        floorLabelBannerId: floorLabelBannerId,
                        floorId: floorId,
                        floorLabelId: floorLabelId,
                        bannerName: bannerName,
                        imgUrl: imgUrl,

                        firstLinkType: firstLinkType,
                        linkUrl: linkUrl,
                        linkId: linkId,
                        linkName: linkName,

                        isEnabled: isEnabled,
                        sequenceNum: sequenceNum,
                        description: description
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
                floorLabelBannerIds = ids;
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
                url: "<@ofbizUrl>removeFloorLabelBanner</@ofbizUrl>",
                type: "POST",
                data: {floorLabelBannerIds: floorLabelBannerIds},
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
    function delFloor(floorLabelBannerId) {
        floorLabelBannerIds = floorLabelBannerId;
        //设置删除弹出框内容
        $('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
        $('#modal_confirm').modal('show');
    }
    function editFloor(floorLabelBannerId) {
        $.ajax({
            url: "<@ofbizUrl>floorLabelBannerDetail</@ofbizUrl>",
            type: "POST",
            data: {
                floorLabelBannerId: floorLabelBannerId
            },
            dataType: "json",
            success: function (data) {

                initLinkType(data)

                $('#addForm').attr("action", "<@ofbizUrl>editFloorLabelBanner</@ofbizUrl>");
                $("#header").html('编辑标签广告');
                $("#floorLabelBannerId").val(data.floorLabelBannerId);
                $("#bannerName").val(data.bannerName);
                $("#imgUrl").val(data.imgUrl);
                $('#img').attr('src', data.imgUrl);
                $("#linkUrl").val(data.linkUrl);
                if (data.isEnabled == 'Y') {
                    $('#enabled1').click();
                } else {
                    $('#enabled2').click();
                }
                $('#sequenceNum').val(data.sequenceNum);
                $('#description').val(data.description);
                $("#addForm").dpValidate({
                    clear: true
                });
                $('#modal_add').modal('show');
            },
            error: function (data) {
            }
        });
    }
    function changeEnabled(obj) {
        var obj = $(obj);
        var isEnabled;
        var floorLabelBannerId = obj.attr("id");
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
            url: "<@ofbizUrl>editFloorLabelBanner</@ofbizUrl>",
            type: "POST",
            data: {
                floorLabelBannerId: floorLabelBannerId,
                isEnabled: isEnabled
            },
            dataType: "json",
            success: function (data) {
            },
            error: function (data) {
            }
        });
    }
</script>