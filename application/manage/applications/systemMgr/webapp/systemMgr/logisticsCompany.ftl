<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" class="form-inline clearfix" >
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">物流公司名称</span>
                    <input type="text" class="form-control"  id="companyName1" name="companyName" value=""/>
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">物流公司代码</span>
                    <input type="text" class="form-control" id="localCompanyId1" name="localCompanyId" value=""/>
                </div>

            </div>
            <div class="input-group pull-right">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form>
        <!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <!-- 是否有修改权限-->
                    <#if security.hasEntityPermission("LOGISTICS_COMPANY", "_CREATE", session)>
                        <button class="btn btn-primary" id="createDeliveryCompany">添加</button>
                    </#if>
                    <#if security.hasEntityPermission("LOGISTICS_COMPANY", "_DELETE", session)>
                        <button class="btn btn-primary" id="deleteDeliveryCompany">删除</button>
                    </#if>
                </div>
            </div><!-- 操作按钮组end -->

            <!-- 列表当前分页条数start -->
            <div class="col-sm-6">
                <div id="view_size" class="dp-tables_length">
                </div>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->

        <!-- 表格区域start -->
        <div class="row">
            <div class="col-sm-12">
                <table id="data_tbl" class="table table-bordered table-hover js-checkparent">
                </table>
            </div>
        </div><!-- 表格区域end -->

        <!-- 分页条start -->
        <div class="row" id="paginateDiv">
        </div><!-- 分页条end -->
    </div>
    <!-- /.box-body -->
</div><!-- 内容end -->

<!-- 显示或隐藏确认弹出框start -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title"></h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 显示或隐藏确认弹出框end -->

<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
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


<!-- 新增弹出框start -->
<div id="modal_add" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">新增物流公司</h4>
            </div>
            <div class="modal-body">
                <form id="AddForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>addLogisticsCompany</@ofbizUrl>">
                    <div class="form-group" data-type="required" data-mark="物流公司名称">
                        <label class="control-label col-sm-4"><i class="required-mark">*</i>物流公司名称：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="companyName" name="companyName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="物流公司代码">
                        <label class="control-label col-sm-4"><i class="required-mark">*</i>物流公司代码：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="localCompanyId" name="localCompanyId">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="快递100物流公司代码">
                        <label class="control-label col-sm-4"><i class="required-mark">*</i>快递100物流公司代码：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="companyId" name="companyId">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-4">公司网址：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="companyWebsite" name="companyWebsite">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-4" >询件网址：</label>
                        <div class="col-sm-8">
                            <textarea class="form-control dp-vd" rows="6" id="inquireWebsite" name="inquireWebsite"></textarea>
                        </div>
                    </div>
                    <div class="form-group" data-type="format" data-reg="/^[0-9]*$/"  data-mark="排序">
                        <label class="control-label col-sm-4">排序：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="sequenceId" name="sequenceId">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-4">是否启用：</label>
                        <div class="col-sm-8">
                            <div class="radio">
                                <label class="col-sm-4"><input id="isEnabled1" name="isEnabled" type="radio" value="Y" class="radioItem" checked="checked">是</label>
                                <label class="col-sm-4"><input id="isEnabled2" name="isEnabled" type="radio" value="N" class="radioItem">否</label>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <!-- 是否有新增权限-->
            <#if security.hasEntityPermission("LOGISTICS_COMPANY", "_CREATE", session)>
                <button id="save" type="button" class="btn btn-primary">保存</button>
            </#if>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 新增弹出框end -->

<!-- 修改弹出框start -->
<div id="modal_edit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">编辑物流公司</h4>
            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>editLogisticsCompany</@ofbizUrl>">
                    <input type="hidden" id="id" name="id">
                    <div class="form-group" data-type="required" data-mark="物流公司名称">
                        <label class="control-label col-sm-4"><i class="required-mark">*</i>物流公司名称：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="companyName" name="companyName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="物流公司代码">
                        <label class="control-label col-sm-4"><i class="required-mark">*</i>物流公司代码：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="localCompanyId" name="localCompanyId">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="快递100物流公司代码">
                        <label class="control-label col-sm-4"><i class="required-mark">*</i>快递100物流公司代码：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="companyId" name="companyId">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-4">公司网址：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="companyWebsite" name="companyWebsite">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-4" >询件网址：</label>
                        <div class="col-sm-8">
                            <textarea class="form-control" rows="6" id="inquireWebsite" name="inquireWebsite"></textarea>
                        </div>
                    </div>
                    <div class="form-group" data-type="format" data-reg="/^[0-9]*$/"  data-mark="排序">
                        <label class="control-label col-sm-4">排序：</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="sequenceId" name="sequenceId">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-4">是否启用：</label>
                        <div class="col-sm-8">
                            <div class="radio">
                                <label class="col-sm-4"><input id="isEnabled1" name="isEnabled" type="radio" value="Y" class="radioItem">是</label>
                                <label class="col-sm-4"><input id="isEnabled2" name="isEnabled" type="radio" value="N" class="radioItem">否</label>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
             <!-- 是否有修改权限-->
            <#if security.hasEntityPermission("LOGISTICS_COMPANY", "_UPDATE", session)>
                <button id="save" type="button" class="btn btn-primary">保存</button>
            </#if>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 修改弹出框end -->

<!-- script区域start -->
<script>
    var data_id,data_isEnabled;
    var data_tbl;
    var ajaxUrl = "logisticsCompanyJson";
    $(function () {
        data_tbl = $('#data_tbl').dataTable({
            ajaxUrl: ajaxUrl,
            listName: "recordsList",
            columns:[
                {"title":"主键","code":"id","checked":true},
                {"title":"物流公司名称","code":"companyName"},
                {"title":"物流公司代码","code":"localCompanyId"},
                {"title":"快递100物流公司代码","code":"companyId"},
                {"title":"排序","code":"sequenceId","sort":true},
                {"title":"是否启用","code":"isEnabled",
                    "handle":function(td,record){
                        if(record.isEnabled == "Y"){
                            td.html("<button type='button' onclick='javascript:editIsEnable(\""+record.id+"\",\"N\");' class='btn btn-info'>是</button>");
                        }else{
                            td.html("<button type='button' onclick='javascript:editIsEnable(\""+record.id+"\",\"Y\");' class='btn btn-default'>否</button>");
                        }
                    }
                },
                {"title":"操作","code":"option",
                    "handle":function(td,record){
                        var btns = "<div class='btn-group'>"+
                                "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:updateLogistics(\""+record.id+"\")'>编辑</button>"+
                                "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
                                "<span class='caret'></span>"+
                                "<span class='sr-only'>Toggle Dropdown</span>"+
                                "</button>"+
                                "<ul class='dropdown-menu' role='menu'>"+
                                    <!-- 是否有删除权限-->
                                <#if security.hasEntityPermission("LOGISTICS_COMPANY", "_DELETE", session)>
                                "<li><a href='javascript:deleteLogistics(\""+record.id+"\")'>删除</a></li>"+
                                </#if>
                                "</ul>"+
                                "</div>";
                        td.append(btns);
                    }
                }
            ],
            paginateEL: "paginateDiv",
            viewSizeEL: "view_size"
        });

        //查询按钮点击事件
        $('#QueryForm #searchBtn').on('click',function(){
            var companyName = $('#QueryForm #companyName1').val();
            var localCompanyId = $('#QueryForm #localCompanyId1').val();

            ajaxUrl = changeURLArg(ajaxUrl,"companyName",companyName);
            ajaxUrl = changeURLArg(ajaxUrl,"localCompanyId",localCompanyId);
            data_tbl.reload(ajaxUrl);
            return false;
        });

        //添加按钮点击事件
        $('#createDeliveryCompany').click(function(){
            $('#AddForm').dpValidate({
                clear: true
            });
            clearForm($("#AddForm"));
            $("#modal_add #isEnabled1").click();
            $('#modal_add').modal();
        });

        //批量删除按钮点击事件
        $('#deleteDeliveryCompany').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                data_id = "";
                //编辑id字符串
                checks.each(function(){
                    data_id += $(this).val() + ",";
                });
                //设置提示弹出框内容
                $('#modal_confirm #modal_confirm_title').html("删除物流公司");
                $('#modal_confirm #modal_confirm_body').html("确定删除这些信息？");
                $('#modal_confirm').modal('show');
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });

        //批量删除按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用显示方法
            $.ajax({
                url: "deleteLogisticsCompany",
                type: "GET",
                data: {ids : data_id},
                dataType : "json",
                success: function(data){
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        data_tbl.reload(ajaxUrl);
                    })
                },
                error: function(data){
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        });

        //新增弹出框保存按钮点击事件
        $('#modal_add #save').click(function () {
            $('#AddForm').dpValidate({
                clear: true
            });

            $('#AddForm').submit();
        });
        //表单校验方法
        $('#AddForm').dpValidate({
            validate: true,
            callback: function(){
                $.ajax({
                    url: "<@ofbizUrl>addLogisticsCompany</@ofbizUrl>",
                    type: "POST",
                    data : $('#AddForm').serialize(),
                    dataType : "json",
                    success: function(data){
                        $('#modal_add').modal("hide");
                        $.tipLayer("操作成功！");
                        data_tbl.reload(ajaxUrl);
                    },
                    error: function(data){
                        $.tipLayer("操作失败！");
                    }
                });
            }
        });
        //修改弹出框保存按钮点击事件
        $('#modal_edit #save').click(function () {
            $('#EditForm').dpValidate({
                clear: true
            });

            $('#EditForm').submit();
        });
        //表单校验方法
        $('#EditForm').dpValidate({
            validate: true,
            callback: function(){
                $.ajax({
                    url: "<@ofbizUrl>editLogisticsCompany</@ofbizUrl>",
                    type: "POST",
                    data : $('#EditForm').serialize(),
                    dataType : "json",
                    success: function(data){
                        $('#modal_edit').modal("hide");
                        $.tipLayer("操作成功！");
                        data_tbl.reload(ajaxUrl);
                    },
                    error: function(data){
                        $.tipLayer("操作失败！");
                    }
                });
            }
        });
    });

    //点击编辑按钮事件
    function updateLogistics(id) {
        $.ajax({
            url: "logisticsCompanyDetail",
            type: "GET",
            data: {id : id},
            dataType : "json",
            success: function(data){
                //清空form
                clearForm($("#EditForm"));
                $("#modal_edit #id").val(data.id);
                $("#modal_edit #localCompanyId").val(data.localCompanyId);
                $("#modal_edit #companyId").val(data.companyId);
                $("#modal_edit #companyName").val(data.companyName);
                $("#modal_edit #companyWebsite").val(data.companyWebsite);
                $("#modal_edit #inquireWebsite").val(data.inquireWebsite);
                $("#modal_edit #sequenceId").val(data.sequenceId);
                if(data.isEnabled == "N"){
                    $("#modal_edit #isEnabled2").click();
                }else{
                    $("#modal_edit #isEnabled1").click();
                }
                $('#EditForm').dpValidate({
                    clear: true
                });
                $('#modal_edit').modal();
            },
            error: function(data){
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                $('#modal_msg').modal();
            }
        });
    }

    function deleteLogistics(id){
        data_id = id;
        $('#modal_confirm #modal_confirm_title').html("删除物流公司");
        $('#modal_confirm #modal_confirm_body').html("确定删除这些信息？");
        $('#modal_confirm').modal('show');
    }

    function editIsEnable(id,isEnabled){
        $.ajax({
            url: "editLCIsEnable",
            type: "GET",
            data: {id : id,isEnabled : isEnabled},
            dataType : "json",
            success: function(data){
                data_tbl.reload(ajaxUrl);
            },
            error: function(data){
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                $('#modal_msg').modal();
            }
        });
    }

</script>
