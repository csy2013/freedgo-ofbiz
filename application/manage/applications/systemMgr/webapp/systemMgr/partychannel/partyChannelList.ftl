<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="Channel_QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">渠道编码</span>
                    <input type="text" id="channelCode" class="form-control" value="">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">渠道名称</span>
                    <input type="text" id="channelName" class="form-control" value="">
                </div>
            </div>
            <div class="input-group pull-right">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!-- 表格区域start -->
        <div class="row">
            <div class="col-sm-12">
                <table id="Channel_DataTbl" class="table table-bordered table-hover js-checkparent">
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <div class="row" id="Channel_Paginate">
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

<!-- 拒绝原因弹出框start -->
<div id="modal_audit_noPass" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_refuseReason_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_refuseReason_title">拒绝原因</h4>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="form-group" data-type="required" data-mark="拒绝原因">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>拒绝原因:</label>
                        <div class="col-sm-7">
                            <textarea id="refuseReason" class="form-control  dp-vd" rows="5"
                                      style="resize: none;"></textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary">确定</button>
            </div>
        </div>
    </div>
</div><!-- 拒绝原因弹出框end -->


<div id="channeldetail_modal" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document" style="width: 50%;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_msg_title">操作信息</h4>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label">渠道ID：</label>
                            <span id="channelId" class="col-sm-8"></span>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label">渠道编码：</label>
                            <span id="channelCode" class="col-sm-8"></span>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label">渠道名称：</label>
                            <span id="channelName" class="col-sm-8"></span>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="form-group">
                            <label class="col-sm-4 control-label">是否启用：</label>
                            <span id="isUse" class="col-sm-8"></span>
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-sm-12">
                        <div class="form-group">
                            <label class="col-sm-2 control-label">备注：</label>
                            <span id="remark" class="col-sm-10"></span>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div>

<!-- script区域start -->
<script>
    var curPartyId;
    var Channel_DataTbl;
    var Channel_AjaxUrl = "getChannelListForJson";
    $(function () {
        Channel_DataTbl = $('#Channel_DataTbl').dataTable({
            ajaxUrl: Channel_AjaxUrl,
            columns: [
                <!-- 是否有审核权限-->
                {"title": "渠道ID", "code": "channelId"},
                {"title": "渠道编码", "code": "channelCode"},
                {"title": "渠道名称", "code": "channelName"},
                {
                    "title": "申请时间", "code": "createdStamp",
                    "handle": function (td, record) {
                        td.empty();
                        td.append(timeStamp2String(record.createdStamp.time));
                    }
                },
                {"title": "是否启用", "code": "status",
                    "handle": function (td, record) {
                        if (record.status == "0") {
                            td.html("否");
                        } else if (record.status == "1") {
                            td.html("是");
                        }
                    }
                },
                {
                    "title": "操作", "code": "option",
                    "handle": function (td, record) {
                        console.log(record)
                        var data={
                            name:"asdf"
                        }
                        var liGroup = "";
                        <#if security.hasEntityPermission("CHANNEL_MANAGE", "_UPDATE", session)>
                            liGroup += "<li class='audit_li'><a href='javascript:updateChannel(\"" + record.channelId + "\")'>编辑</a></li>";
                        </#if>
                        <#if security.hasEntityPermission("CHANNEL_MANAGE", "_DELETE", session)>
                            liGroup += "<li class='audit_li'><a href='javascript:deleteChannel(\"" + record.channelId+ "\")'>删除</a></li>";
                        </#if>
                        var btnGroup = "<div class='btn-group'>" +
                                "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:queryChannelInfo(" + JSON.stringify(record) + ")'>查看详情</button>" +
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
            paginateEL: "Channel_Paginate",
            viewSizeEL: "Channel_ViewSize"
        });

        //查询按钮点击事件
        $('#Channel_QueryForm #searchBtn').on('click', function () {
           return reloadTable();
        });

    });

    function reloadTable(){
        var channelCode = $('#Channel_QueryForm #channelCode').val();
        var channelName = $('#Channel_QueryForm #channelName').val();
        Channel_AjaxUrl = changeURLArg(Channel_AjaxUrl, "channelCode", channelCode);
        Channel_AjaxUrl = changeURLArg(Channel_AjaxUrl, "channelName", channelName);
        Channel_DataTbl.reload(Channel_AjaxUrl);
        return false;
    }
    function queryChannelInfo(record){
        console.log(record)
        $("#channeldetail_modal #channelId").text(record.channelId)
        $("#channeldetail_modal #channelName").text(record.channelName)
        $("#channeldetail_modal #channelCode").text(record.channelCode)
        $("#channeldetail_modal #remark").text(record.remark)
        var isUse=""
        if(record.status=="0"){
            isUse="否"
        }else{
            isUse="是"
        }
        $("#isUse").text(isUse)

        $("#channeldetail_modal").modal()

    }
    function deleteChannel(channelId){
        $.confirmLayer({
            msg: '确定删除该信息吗？',
            confirm: function () {
                $.ajax({
                    url: "deleteChannel",
                    type: "POST",
                    data:{
                        channelId:channelId
                    },
                    dataType: "json",
                    success: function (data) {
                        if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                            $.tipLayer(data._ERROR_MESSAGE_);
                        }else{
                            $("#confirmLayer").modal("hide")
                            $('#modal_msg #modal_msg_body').html("删除成功！");
                            $('#modal_msg').modal();
                            $('#modal_msg').on('hide.bs.modal', function () {
                                reloadTable()
                            })
                        }
                    },
                    error: function (data) {
                        $.tipLayer("操作失败！");
                    }
                });


            }
        })

    }

    function updateChannel(channelId){
        window.location = '<@ofbizUrl>channelEditPage</@ofbizUrl>?channelId=' + channelId;
    }

</script><!-- script区域end -->
