<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="Template_QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">模板名称</span>
                    <input type="text" id="templateName" class="form-control" value="">
                </div>
            </div>
            <div class="input-group pull-right">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!--工具栏start -->
        <div class="row m-b-10">

            <!-- 列表当前分页条数start -->
            <div class="col-sm-6">
            <#if security.hasEntityPermission("MODULECONFIG", "_CREATE", session)>
                <button class="btn btn-primary" onclick="addIndexTemplateView()">新增</button>
            </#if>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->
        <!-- 表格区域start -->
        <div class="row">
            <div class="col-sm-12">
                <table id="Template_DataTbl" class="table table-bordered table-hover js-checkparent">
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <div class="row" id="Template_Paginate">
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

<div id="modal_addIndexTemplate" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_msg_title">新增模板</h4>
            </div>
            <div class="modal-body">
                <div class="row">
                    <div class="form-group col-sm-offset-1" data-type="required" data-mark="模版名称">
                        <label class="col-sm-3 control-label"><i class="required-mark">*</i>模版名称</label>
                        <div class="col-sm-7">
                            <input type="text" class="form-control" id="Add_templateName"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group col-sm-offset-1" data-type="required" data-mark="是否使用">
                        <label class="col-sm-3 control-label"><i class="required-mark">*</i>是否使用</label>
                        <div class="col-sm-7  p-t-5">
                            <label class="col-sm-3"><input name="Add_IsUse" type="radio" checked value="Y">是</label>
                            <label class="col-sm-4"><input name="Add_IsUse" type="radio" value="N">否</label>
                        </div>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button id="ok" onclick="saveIndexTemplate()" type="button" class="btn btn-primary">保存</button>
                <button type="button" class="btn " data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>


<!-- script区域start -->
<script>
    var Template_DataTbl;
    var Template_AjaxUrl = "getIndexTemplateListJson";
    $(function () {
        Template_DataTbl = $('#Template_DataTbl').dataTable({
            ajaxUrl: Template_AjaxUrl,
            columns: [
                {"title": "模版id", "code": "templateId", "sort": true},
                {"title": "模板名称", "code": "templateName", "sort": true},
                {
                    "title": "是否启用", "code": "isUsed",
                    "handle": function (td, record) {
                        <!-- 是否有审核权限-->
                        <#if security.hasEntityPermission("MODULECONFIG", "_UPDATE", session)>
                            if (record.isUsed == "Y") {
                                td.html("<button type='button' onclick='javascript:editTemplateStatus(\"" + record.templateId + "\",\"N\");' class='btn btn-primary'>是</button>");
                            } else {
                                td.html("<button type='button' onclick='javascript:editTemplateStatus(\"" + record.templateId + "\",\"Y\");' class='btn btn-default'>否</button>");
                            }
                        <#else>
                            if (record.isUsed == "Y") {
                                td.html("是");
                            } else {
                                td.html("否");
                            }
                        </#if>
                    }
                },
                {
                    "title": "操作", "code": "option",
                    "handle": function (td, record) {
                        var liGroup = "";
                        <!-- 是否有审核权限-->
                        <#if security.hasEntityPermission("MODULECONFIG", "_DELETE", session)>
                            liGroup += "<li class='audit_li'><a href='javascript:deleteTemplate(\"" + record.templateId + "\")'>删除</a></li>";
                        </#if>
                        var btnGroup = "<div class='btn-group'>" +
                                "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:editTemplate(\"" + record.templateId + "\")'>编辑模块</button>" +
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
            paginateEL: "Template_Paginate",
            viewSizeEL: "Template_ViewSize"
        });

        //查询按钮点击事件
        $('#Template_QueryForm #searchBtn').on('click', function () {
            return reloadTable();
        });


        //审核弹出窗关闭事件
        $('#modal_audit').on('hide.bs.modal', function () {
            $('#TemplateAudit_Form').dpValidate({
                clear: true
            });
        });


    });

    function reloadTable(){
        var templateName = $('#Template_QueryForm #templateName').val();
        Template_AjaxUrl = changeURLArg(Template_AjaxUrl, "templateName", templateName);
        Template_DataTbl.reload(Template_AjaxUrl);
        return false;
    }

    function addIndexTemplateView() {
        $("#modal_addIndexTemplate").modal()

    }

    function editTemplate(templateId){
        window.location = '<@ofbizUrl>moduleConfigPage</@ofbizUrl>?templateId='+templateId;

    }
    function deleteTemplate(templateId){
        $.confirmLayer({
            msg: '确定删除该信息',
            confirm: function () {
                $.ajax({
                    url: "deleteTemplate",
                    type: "POST",
                    data:{
                        templateId:templateId,
                    },
                    dataType: "json",
                    success: function (data) {
                        if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                            $.tipLayer(data._ERROR_MESSAGE_);
                        }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                            $.tipLayer(data._ERROR_MESSAGE_LIST_);
                        } else{
                            $('#confirmLayer').modal('hide');
                            $('#modal_msg #modal_msg_body').html("删除成功！");
                            $('#modal_msg').modal();
                            $('#modal_msg').on('hide.bs.modal', function () {
                                //重新加载table
                                reloadTable();
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
    function editTemplateStatus(templateId,isUsed){
        $.ajax({
            url: "editTemplateStatus",
            type: "POST",
            data:{
                templateId:templateId,
                isUsed:isUsed
            },
            dataType: "json",
            success: function (data) {
                if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                    $.tipLayer(data._ERROR_MESSAGE_);
                }else{
                        //重新加载table
                        reloadTable();
                }
            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });
    }

    function saveIndexTemplate() {
        if ($("#modal_addIndexTemplate #Add_templateName").val() == "") {
            $.tipLayer("模版名称不能为空！");
            return;
        }
        var data = {
            templateName: $("#modal_addIndexTemplate #Add_templateName").val(),
            isUsed: $("input[name='Add_IsUse']:checked").val()
        }
        $.ajax({
            url: "indexTemplateAdd",
            type: "POST",
            data: data,
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                } else {
                    $("#modal_addIndexTemplate").modal("hide")
                    $('#modal_msg #modal_msg_body').html("操作成功！");
                    $('#modal_msg').modal();
                    $('#modal_msg').on('hide.bs.modal', function () {
                        //重新加载table
                        reloadTable();
                    })
                }
            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });


    }


</script><!-- script区域end -->
