<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="Business_QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商家名称</span>
                    <input type="text" id="partyName" class="form-control" value="">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">法人姓名</span>
                    <input type="text" id="leageName" class="form-control" value="">
                </div>
            </div>
            <div class="input-group pull-right">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!-- tab选项卡begin -->
        <ul id="Business_Nav" class="nav nav-tabs" style="margin-bottom: 10px;">
            <li class="active"><a id="1" href="javascript:void(0);">已通过</a></li>
            <li><a id="2" href="javascript:void(0);">已拒绝</a></li>
            <li><a id="" href="javascript:void(0);">全部</a></li>
        </ul><!-- tab选项卡end -->

        <!-- 表格区域start -->
        <div class="row">
            <div class="col-sm-12">
                <table id="Business_DataTbl" class="table table-bordered table-hover js-checkparent">
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <div class="row" id="Business_Paginate">
        </div><!-- 分页条end -->
    </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_msg_title">操作信息</h4>
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




<!-- 商家审核弹出框start -->
<div id="modal_audit" class="modal fade">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_audit_title">审核信息</h4>
            </div>
            <div class="modal-body">
                <form id="BusinessAudit_Form">
                    <div class="row">
                        <div class="col-sm-12 form-group ">
                            <label class="control-label col-sm-3"><i class="required-mark">*</i>结算方式:</label>
                            <div class="col-sm-8">
                                <select class="form-control" name="settingType" id="settingType">
                                    <option value="PARTY_SETTLE_TYPE_MONTH">月结</option>
                                    <option value="PARTY_SETTLE_TYPE_T">T+N</option>
                                </select>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row" id="settle_month">
                        <div class="col-sm-12 form-group " data-type="required" data-mark="x号结算">
                            <label class="control-label col-sm-3"></label>
                            <div class="col-sm-2">
                                <input type="text" class="form-control" id="settle_days_input"/>
                                <p class="dp-error-msg"></p>
                            </div>
                            <label class="col-sm-2 control-label">号结算</label>
                        </div>
                    </div>

                    <div class="row" id="settle_t">
                        <div class="col-sm-12 form-group " data-type="required" data-mark="T+*">
                            <label class="control-label col-sm-3"></label>
                            <label class="col-sm-1 control-label">T+</label>
                            <div class="col-sm-2">
                                <input type="text" class="form-control" id="settle_t_input"/>
                                <p class="dp-error-msg"></p>
                            </div>

                        </div>
                    </div>

                    <div class="row">
                        <div class="col-sm-12 form-group">
                            <label class="control-label col-sm-3"><i class="required-mark">*</i>佣金:</label>
                            <div class="col-sm-2">
                                <input type="text" class="form-control" id="commission"/>
                                <p class="dp-error-msg"></p>
                            </div>
                            <label class="col-sm-2 control-label">%</label>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary">确定</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 商家审核弹出框end -->


<!-- script区域start -->
<script>
    var curPartyId;
    var auditStatus ="1";
    var Business_DataTbl;
    var Business_AjaxUrl = "getBusinessListForJson";
    $(function () {
        Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "auditStatus", "1");
        Business_DataTbl = $('#Business_DataTbl').dataTable({
            ajaxUrl: Business_AjaxUrl,
            columns: [
                {"title": "商家名称", "code": "partyName", "sort": true},
                {"title": "商家编码", "code": "partyId", "sort": true},
                {"title": "法人姓名", "code": "leageName"},
                {"title": "联系电话", "code": "leageTel"},
                {
                    "title": "申请时间", "code": "createdStamp",
                    "handle": function (td, record) {
                        td.empty();
                        td.append(timeStamp2String(record.createdStamp.time));
                    }
                },
                {
                    "title": "状态", "code": "auditStatus",
                    "handle": function (td, record) {
                        if (record.auditStatus == "0") {
                            td.html("待审核");
                        } else if (record.auditStatus == "1") {
                            td.html("已通过");
                        } else if (record.auditStatus == "2") {
                            td.html("已拒绝");
                        }
                    }
                },

                {
                    "title": "是否启用", "code": "statusId",
                    "handle": function (td, record) {
                        if (record.auditStatus == "1") {
                            <!-- 是否有审核权限-->
                        <#if security.hasEntityPermission("BUSINESSMGR_LIST", "_AUDIT", session)>
                            if (record.statusId == "PARTY_ENABLED") {
                                td.html("<button type='button' onclick='javascript:editBusinessIsUse(\"" + record.partyId + "\",\"PARTY_DISABLED\");' class='btn btn-primary'>是</button>");
                            } else {
                                td.html("<button type='button' onclick='javascript:editBusinessIsUse(\"" + record.partyId + "\",\"PARTY_ENABLED\");' class='btn btn-default'>否</button>");
                            }
                        <#else>
                            if (record.statusId == "PARTY_ENABLED") {
                                td.html("是");
                            } else {
                                td.html("否");
                            }
                        </#if>
                        } else {
                            if (record.statusId == "PARTY_ENABLED") {
                                td.html("是");
                            } else {
                                td.html("否");
                            }
                        }
                    }
                },
                {
                    "title": "操作", "code": "option",
                    "handle": function (td, record) {
                        var liGroup = "";
                        if (record.auditStatus == "1") {
                            <#if security.hasEntityPermission("BUSINESSMGR_LIST", "_UPDATE", session)>
                                liGroup += "<li class='edit_li'><a href='javascript:goEdit(\"" + record.partyId +"\",\""+record.auditStatus+"\")'>编辑</a></li>";
                            </#if>
                            <!-- 是否有编辑权限-->
                            <#if security.hasEntityPermission("BUSINESSMGR_LIST", "_UPDATE", session)>
                                liGroup += "<li class='edit_li'><a href='javascript:editSettleSetting(\"" + record.partyId + "\")'>结算信息</a></li>";
                            </#if>

                        } else if (record.auditStatus == "2") {
                            liGroup = "<li class='refuseQuery_li'><a href='javascript:goRefuseReason(\"" + record.partyId + "\")'>查看拒绝原因</a></li>";

                            <#if security.hasEntityPermission("BUSINESSMGR_LIST", "_UPDATE", session)>
                                liGroup += "<li class='edit_li'><a href='javascript:goEdit(\"" + record.partyId +"\",\""+record.auditStatus+"\")'>编辑</a></li>";
                            </#if>
                            <#if security.hasEntityPermission("BUSINESSMGR_LIST", "_UPDATE", session)>
                                liGroup += "<li class='edit_li'><a href='javascript:commitAudit(\"" + record.partyId + "\")'>提交审核</a></li>";
                            </#if>
                        }

                        <#--<#if security.hasEntityPermission("BUSINESSMGR", "_DELETE", session)>
                            liGroup += "<li class='refuseQuery_li'><a href='javascript:deleteBusiness(\"" + record.partyId + "\")'>删除</a></li>";
                        </#if>-->
                        var btnGroup = "<div class='btn-group'>" +
                                "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:queryBusinessInfo(\"" + record.partyId + "\")'>查看详情</button>" +
                                "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>" +
                                "<span class='caret'></span>" +
                                "<span class='sr-only'>Toggle Dropdown</span>" +
                                "</button>" +
                                "<ul class='dropdown-menu' role='menu'>" +
                                    liGroup +
                                "</ul>" +
                                "</div>";
                        td.html(btnGroup);
                    }
                }
            ],
            listName: "recordsList",
            paginateEL: "Business_Paginate",
            viewSizeEL: "Business_ViewSize"
        });

        //查询按钮点击事件
        $('#Business_QueryForm #searchBtn').on('click', function () {
            return reloadTable();
        });



        //切换选项卡
        $('#Business_Nav a').click(function (e) {
            if (!$(this).closest('li').hasClass('active')) {
                $(this).closest('li').addClass('active').siblings('.active').removeClass('active');
            }
            auditStatus = $(this).attr('id');
            var partyName = $('#Business_QueryForm #partyName').val();
            var leageName = $('#Business_QueryForm #leageName').val();
            Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "partyName", partyName);
            Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "leageName", leageName);
            Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "auditStatus", auditStatus);
            Business_DataTbl.reload(Business_AjaxUrl);
        });

        $("#settingType").change(function () {
            var settingType = $(this).val();
            if (settingType == "PARTY_SETTLE_TYPE_T") {
                $("#settle_month").hide();
                $("#settle_t").show();
            } else {
                $("#settle_t").hide();
                $("#settle_month").show();
            }

        });

        //审核弹出框的确定按钮
        $('#modal_audit #ok').click(function () {
            var settingType = $("#settingType").val();
            var daysNum = "";
            if (settingType == "PARTY_SETTLE_TYPE_T") {
                if ($("#settle_t_input").val() == "") {
                    $.tipLayer("T+N不能为空！");
                    return;
                }
                daysNum = $("#settle_t_input").val();
            } else {
                if ($("#settle_days_input").val() == "") {
                    $.tipLayer("x号结算不能为空！");
                    return;
                }
                daysNum = $("#settle_days_input").val();
            }
            if ($("#commission").val() == "") {
                $.tipLayer("佣金不能为空！");
                return;
            }
            var commission = $("#commission").val();
            //提交审核通过
            var data = {
                settingType: settingType,
                daysNum: daysNum,
                commission: commission,
                partyId: curPartyId
            }
            $.ajax({
                url: "updateBusinessSettle",
                type: "post",
                data: data,
                dataType: "json",
                success: function (data) {
                    if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                        $.tipLayer(data._ERROR_MESSAGE_);
                    } else {
                        $("#modal_audit").modal('hide');
                        $('#modal_msg #modal_msg_body').html("操作成功！");
                        $('#modal_msg').modal();
                    }
                },
                error: function (data) {
                    $.tipLayer("系统错误");
                }
            });


        });

    });
    function reloadTable(){
        var partyName = $('#Business_QueryForm #partyName').val();
        var leageName = $('#Business_QueryForm #leageName').val();
        Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "partyName", partyName);
        Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "leageName", leageName);
        Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "auditStatus", auditStatus);
        Business_DataTbl.reload(Business_AjaxUrl);
        return false;
    }
    //修改商家是否启用状态
    function editBusinessIsUse(partyId, isUse) {
        $.ajax({
            url: "editBusinessIsUse",
            type: "GET",
            data: {
                partyId: partyId,
                isUse: isUse
            },
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                } else {
                    return reloadTable();
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    };

    //查看商家详情信息
    function queryBusinessInfo(partyId) {
        $.dataSelectModal({
            url: "businessInfoPage?partyId=" + partyId,
            width: "800",
            title: "商家详情"
        });
    }

    //商家修改页面
    function goEdit(partyId,auditStatus) {
        window.location = '<@ofbizUrl>businessEditPage</@ofbizUrl>?partyId='+partyId+"&auditStatus="+auditStatus;
    }

    function deleteBusiness(partyId){

    }
    //重新提交审核
    function commitAudit(partyId){
        $.ajax({
            url: "commitAudit",
            type: "GET",
            data: {partyId: partyId},
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                } else {
                    $('#modal_msg #modal_msg_body').html("重新提交成功！");
                    $('#modal_msg').modal();
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location = '<@ofbizUrl>businessToAuditList</@ofbizUrl>';
                    })

                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });

    }
    //修改结算信息
    function editSettleSetting(partyId){
        curPartyId=partyId;
        $.ajax({
            url: "getSettleSettingInfo",
            type: "GET",
            data: {partyId: partyId},
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                } else {
                    $("#settle_days_input").val("")
                    $("#settle_t_input").val("")
                    var partySettleSetting =data.partySettleSetting;
                    console.log(partySettleSetting)
                    var settingType=partySettleSetting.settingType;
                    var daysNum=partySettleSetting.daysNum;
                    var commission= partySettleSetting.commission;
                    $("#settingType").val(settingType)
                    if (settingType == "PARTY_SETTLE_TYPE_T") {
                        $("#settle_month").hide();
                        $("#settle_t").show();
                        $("#settle_t_input").val(daysNum)
                    } else {
                        $("#settle_t").hide();
                        $("#settle_month").show();
                        $("#settle_days_input").val(daysNum)
                    }
                    $("#commission").val(commission)
                    $('#modal_audit').modal();
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });

    }




    //查看商家拒绝原因
    function goRefuseReason(partyId) {
        $.ajax({
            url: "businessRefuseReason",
            type: "GET",
            data: {partyId: partyId},
            dataType: "json",
            success: function (data) {
                $('#modal_msg #modal_msg_body').html(data.auditContent);
                $('#modal_msg').modal();
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    }


</script><!-- script区域end -->
