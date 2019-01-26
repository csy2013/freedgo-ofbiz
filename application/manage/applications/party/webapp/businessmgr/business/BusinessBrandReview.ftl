<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="Business_QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">品牌名称</span>
                    <input type="text" id="brandName" class="form-control" value="">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">店铺名称</span>
                    <input type="text" id="storeName" class="form-control" value="">
                </div>

            </div>
            <div class="input-group pull-right">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form>
        <!-- 条件查询end -->

    <#-- 审核确认弹出框begin -->
        <div id="modal_confirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
             aria-labelledby="modal_confirm_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_confirm_title">操作提示</h4>
                    </div>
                    <div class="modal-body">
                        <h4 id="modal_confirm_body"></h4>
                    </div>
                    <div class="modal-footer">
                        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div>
        <!-- 审核确认弹出框end -->

        <#--<div class="dp-tables_btn">-->
            <#--<#if security.hasEntityPermission("BUSINESSMGR_BRAND", "_AUDIT", session)>-->
                <#--<a class="btn btn-primary" href="<@ofbizUrl>businessBrandAuthorize</@ofbizUrl>">品牌授权申请</a>-->
            <#--</#if>-->
        <#--</div>-->
        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!-- tab选项卡begin -->
        <ul id="Business_Nav" class="nav nav-tabs" style="margin-bottom: 10px;">
            <li class="active"><a id="" href="javascript:void(0);">全部</a></li>
            <li><a id="0" href="javascript:void(0);">待审核</a></li>
            <li><a id="1" href="javascript:void(0);">已通过</a></li>
            <li><a id="2" href="javascript:void(0);">已拒绝</a></li>
        </ul>
        <!-- tab选项卡end -->

        <!--工具栏start -->
        <div class="row m-b-10">
        <#--<div class="col-sm-6">-->
        <#--<div class="dp-tables_btn">-->
        <#--<a class="btn btn-primary" href="<@ofbizUrl>memberAdd</@ofbizUrl>">品牌授权申请</a>-->
        <#--</div>-->
        <#--</div>-->
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">

                </div>
            </div>
            <!-- 操作按钮组end -->


            <!-- 列表当前分页条数start -->
            <div class="col-sm-6">
                <div id="Business_ViewSize" class="dp-tables_length">
                </div>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->
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
</div>
<!-- 提示弹出框end -->

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
                            <textarea id="refuseReason"  class="form-control  dp-vd" rows="5" style="resize: none;"></textarea>
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


<!-- script区域start -->
<script>
    var Business_DataTbl;
    var Business_AjaxUrl = "getPartyBusinessBrandForReview";
    $(function(){
        Business_DataTbl = $('#Business_DataTbl').dataTable({
            ajaxUrl: Business_AjaxUrl,
            columns:[
                <!-- 是否有审核权限-->
          	<#--<#if security.hasEntityPermission("BUSINESSMGR_LIST", "_AUDIT", session)>-->
			<#--// {"title":"复选框","code":"partyId","checked":true},-->
            <#--</#if>-->
                {"title":"品牌logo","code":"logoContentId",
                    "handle":function(td,record){
                        if(record.logoContentId){
                            td.html("<img height='100' class='cssImgSmall' src='/content/control/stream?contentId='"+record.logoContentId+"/>");
                        }
                    }
                },
                {"title":"品牌名称","code":"brandName"},
                {"title":"品牌别名","code":"brandNameAlias"},
                // {"title":"品牌详情","code":"brandDesc"},
                {"title":"状态","code":"auditStatus",
                    "handle":function(td,record){
                        if(record.auditStatus == "0"){
                            td.html("待审核");
                        }else if(record.auditStatus == "1"){
                            td.html("已通过");
                        }else if(record.auditStatus == "2"){
                            td.html("已拒绝");
                        }
                    }
                },
                {
                    //操作 审核 拒绝 查看
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
        $('#Business_QueryForm #searchBtn').on('click',function(){
            var brandName = $('#Business_QueryForm #brandName').val();
            // var legalPersonName = $('#Business_QueryForm #legalPersonName').val();
            Business_AjaxUrl = changeURLArg(Business_AjaxUrl,"brandName",brandName);
            // Business_AjaxUrl = changeURLArg(Business_AjaxUrl,"legalPersonName",legalPersonName);
            Business_DataTbl.reload(Business_AjaxUrl);
            return false;
        });

        //切换选项卡
        $('#Business_Nav a').click(function(e){
            if(!$(this).closest('li').hasClass('active')){
                $(this).closest('li').addClass('active').siblings('.active').removeClass('active');
            }
            var auditStatus = $(this).attr('id');
            var businessName = $('#Business_QueryForm #brandName').val();
            // var legalPersonName = $('#Business_QueryForm #legalPersonName').val();
            Business_AjaxUrl = changeURLArg(Business_AjaxUrl,"businessName",brandName);
            // Business_AjaxUrl = changeURLArg(Business_AjaxUrl,"legalPersonName",legalPersonName);
            Business_AjaxUrl = changeURLArg(Business_AjaxUrl,"auditStatus",auditStatus);
            Business_DataTbl.reload(Business_AjaxUrl,function(){
                switch(auditStatus){
                    case '0':{
                        $('#Business_DataTbl .js-allcheck').closest('th').show();
                        $('#Business_DataTbl .js-checkchild').closest('td').show();
                        $('#btn_pass').show();
                        $('#btn_refuse').show();
                    }
                        break;
                    case '1':{
                        $('#Business_DataTbl .js-allcheck').closest('th').hide();
                        $('#Business_DataTbl .js-checkchild').closest('td').hide();
                        $('#btn_pass').hide();
                        $('#btn_refuse').hide();
                    }
                        break;
                    case '2':{
                        $('#Business_DataTbl .js-allcheck').closest('th').hide();
                        $('#Business_DataTbl .js-checkchild').closest('td').hide();
                        $('#btn_pass').hide();
                        $('#btn_refuse').hide();
                    }
                        break;
                    default:{
                        $('#Business_DataTbl .js-allcheck').closest('th').hide();
                        $('#Business_DataTbl .js-checkchild').closest('td').hide();
                        $('#btn_pass').hide();
                        $('#btn_refuse').hide();
                    }
                        break;
                }
            });
        });

        //表单校验方法
        $('#modal_audit #BusinessAudit_Form').dpValidate({
            validate: true,
            callback: function(){
                // var businessId = $('#modal_audit #BusinessAudit_Form #businessId').val();
                var auditStatus = $('#modal_audit #BusinessAudit_Form input[name="auditStatus"]:checked').val();
                var auditContent = $('#modal_audit #BusinessAudit_Form #auditContent').val();

                $.ajax({
                    url: "businessAudit",
                    type: "GET",
                    data: {businessId : businessId,
                        auditStatus:auditStatus,
                        auditContent:auditContent
                    },
                    dataType : "json",
                    success: function(data){
                        Business_DataTbl.reload(Business_AjaxUrl,function(){
                            $('#Business_DataTbl .js-allcheck').closest('th').show();
                            $('#Business_DataTbl .js-checkchild').closest('td').show();
                            $('#btn_pass').show();
                            $('#btn_refuse').show();
                        });
                        $('#modal_audit').modal("toggle");
                    },
                    error: function(data){
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

        //审核弹出框的确定按钮
        $('#modal_audit #ok').click(function(){
            $("#modal_audit #BusinessAudit_Form").dpValidate({
                clear: true
            });
            $("#modal_audit #BusinessAudit_Form").submit();
        });

        //审核弹出窗关闭事件
        $('#modal_audit').on('hide.bs.modal', function () {
            $('#BusinessAudit_Form').dpValidate({
                clear: true
            });
        });

        //审核弹出框中的操作单选按钮切换
        $('#modal_audit #BusinessAudit_Form input[name="auditStatus"]').change(function() {
            var auditStatus = $(this).val();
            if(auditStatus == 1){
                $('#modal_audit #BusinessAudit_Form #auditContent_lbl').html('审核意见:');
                $('#modal_audit #BusinessAudit_Form #auditContent').removeClass('dp-vd');
            }else{
                $('#modal_audit #BusinessAudit_Form #auditContent_lbl').html('<i class="required-mark">*</i>审核意见:');
                $('#modal_audit #BusinessAudit_Form #auditContent').addClass('dp-vd');
            }
        });

        //批量通过按钮点击事件
        $('#btn_pass').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                var auditBody="";
                //编辑id字符串
                checks.each(function(){
                    var b_id = $(this).val();
                    var b_name = $(this).closest('tr').find('td').eq(1).html();
                    auditBody += createPassBody(b_id,b_name);
                });
                $('#modal_pass #BusinessPass_Form').html(auditBody);
                $('#modal_pass').modal();
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });

        //批量通过弹出框的确定按钮点击事件
        $('#modal_pass #ok').click(function(){
            var auditContents=$('#modal_pass #BusinessPass_Form').find('textarea');
            auditContents.each(function(){
                var businessId = $(this).data("id"),
                        auditContent = $(this).val();
                $.ajax({
                    url: "businessAudit",
                    type: "GET",
                    data: {businessId : businessId,
                        auditStatus:'1',
                        auditContent:auditContent
                    },
                    dataType : "json",
                    success: function(data){
                        Business_DataTbl.reload(Business_AjaxUrl,function(){
                            $('#Business_DataTbl .js-allcheck').closest('th').show();
                            $('#Business_DataTbl .js-checkchild').closest('td').show();
                            $('#btn_pass').show();
                            $('#btn_refuse').show();
                        });
                        $('#modal_pass').modal("hide");
                    },
                    error: function(data){
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                        $('#modal_msg').modal();
                    }
                });
            });
        });


        //批量拒绝按钮点击事件
        $('#btn_refuse').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                var auditBody="";
                //编辑id字符串
                checks.each(function(){
                    var b_id = $(this).val();
                    var b_name = $(this).closest('tr').find('td').eq(1).html();
                    auditBody += createRefuseBody(b_id,b_name);
                });
                $('#modal_refuse #BusinessRefuse_Form').html(auditBody);
                $('#modal_refuse').modal();
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });

        //批量拒绝表单校验方法
        $('#modal_refuse #BusinessRefuse_Form').dpValidate({
            validate: true,
            callback: function(){
                var auditContents=$('#modal_refuse #BusinessRefuse_Form').find('textarea');
                auditContents.each(function(){
                    var businessId = $(this).data("id"),
                            auditContent = $(this).val();
                    $.ajax({
                        url: "businessAudit",
                        type: "GET",
                        data: {businessId : businessId,
                            auditStatus:'2',
                            auditContent:auditContent
                        },
                        dataType : "json",
                        success: function(data){
                            Business_DataTbl.reload(Business_AjaxUrl,function(){
                                $('#Business_DataTbl .js-allcheck').closest('th').show();
                                $('#Business_DataTbl .js-checkchild').closest('td').show();
                                $('#btn_pass').show();
                                $('#btn_refuse').show();
                            });
                            $('#modal_refuse').modal("hide");
                        },
                        error: function(data){
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                            $('#modal_msg').modal();
                        }
                    });
                });
            }
        });

        //批量拒绝弹出框的确定按钮点击事件
        $('#modal_refuse #ok').click(function(){
            $("#modal_refuse #BusinessRefuse_Form").dpValidate({
                clear: true
            });
            $("#modal_refuse #BusinessRefuse_Form").submit();
        });

        //修改密码弹出框的保存按钮事件
        $('#modal_edit_password #save').click(function(){
            $("#modal_edit_password #BusinessEditPwd_Form").dpValidate({
                clear: true
            });
            $("#modal_edit_password #BusinessEditPwd_Form").submit();
        });

        //修改密码弹出窗关闭事件
        $('#modal_edit_password').on('hide.bs.modal', function () {
            $('#BusinessEditPwd_Form').dpValidate({
                clear: true
            });
        });

        //确认弹出框确认按钮点击事件
        $('#modal_confirm #ok').click(function (e) {
            //异步调用审核添加的方法
            $.ajax({
                url: "businessBrandsAdd",
                type: "POST",
                data: {
                    businessName:$('#businessName').val(),
                    brandsNickName:$('#brandsNickName').val(),
                    logoImg:$('#logoImg').val(),
                    idCardProsImg:$('#idCardProsImg').val()
                },
                dataType: "json",
                success: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.reload();
                    })
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        });


        //修改密码表单校验方法
        $('#modal_edit_password #BusinessEditPwd_Form').dpValidate({
            validate: true,
            callback: function(){
                var businessId = $('#modal_edit_password').find('input#businessId').val(),
                        password_1 = $('#modal_edit_password').find('input#password_1').val(),
                        password_2 = $('#modal_edit_password').find('input#password_2').val();
                if(password_2 != password_1){
                    $('#modal_edit_password').find('input#password_2').closest('.form-group').addClass('has-error');
                    $('#modal_edit_password').find('input#password_2').focus().siblings('p').text('两次输入的密码不一致！');
                }else{
                    $.ajax({
                        url: "UpdatePasswordForBusiness",
                        type: "GET",
                        data: {userLoginId : businessId,
                            newPassword:password_1,
                            newPasswordVerify:password_2
                        },
                        dataType : "json",
                        success: function(data){
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("修改成功！");
                            $('#modal_msg').modal();
                            $('#modal_edit_password').modal("hide");
                        },
                        error: function(data){
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                            $('#modal_msg').modal();
                        }
                    });
                }
            }
        });
    });

    // 审核弹出框
    function deletePromo() {
        $('#BrandsAddForm').dpValidate({
            validate: true,
            clear: true
        });
        $('#modal_confirm #modal_confirm_body').html("你确定申请品牌审核吗？");
        $('#modal_confirm').modal('show');
    }


    //修改商家是否启用状态
    function editBusinessIsUse(businessId,isUse){
        $.ajax({
            url: "editBusinessIsUse",
            type: "GET",
            data: {businessId : businessId,
                isUse:isUse
            },
            dataType : "json",
            success: function(data){
                Business_DataTbl.reload(Business_AjaxUrl);
            },
            error: function(data){
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    };

    //查看商家详情信息
    function queryBusinessInfo(businessId){
        $.dataSelectModal({
            url: "businessInfoPage?businessId="+businessId,
            width:	"800",
            title:	"商家详情"
        });
    }

    //商家修改页面
    function goEdit(businessId){
        window.location = '<@ofbizUrl>businessEditPage</@ofbizUrl>?businessId='+businessId;
    }

    //商家修改密码
    function goEditPWD(businessId){
        clearForm($('#modal_edit_password'));
        $('#modal_edit_password').find('input#businessId').val(businessId);
        $('#modal_edit_password').find('span#businessId').text(businessId);
        $('#modal_edit_password').modal();
    }

    //商家审核
    function goAudit(businessId,businessName){
        $('#modal_audit #BusinessAudit_Form input[name="auditStatus"][value=1]').attr("checked",true);
        $('#modal_audit #BusinessAudit_Form #auditContent').val('');
        $('#modal_audit #BusinessAudit_Form #businessId').val(businessId);
        $('#modal_audit #BusinessAudit_Form #businessName').html(businessName);
        $('#modal_audit').modal();
    }

    //查看商家拒绝原因
    function goRefuseReason(businessId){
        $.ajax({
            url: "businessRefuseReason",
            type: "GET",
            data: {businessId : businessId},
            dataType : "json",
            success: function(data){
                $('#modal_refuseReason #modal_refuseReason_body').html(data.auditContent);
                $('#modal_refuseReason').modal();
            },
            error: function(data){
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    }

    //创建批量通过内容
    function createPassBody(b_id,b_name){
        return '<div class="form-group">'
                +'<label class="control-label col-sm-2">商家名称:</label>'
                +'<span id="businessName_'+b_id+'" class="col-sm-10" style="padding-top: 7px;">'+b_name+'</span>'
                +'</div>'
                +'<div class="form-group">'
                +'<label class="control-label col-sm-2">审核意见:</label>'
                +'<div class="col-sm-10" style="padding-top: 7px;">'
                +'<textarea id="auditContent_'+b_id+'"  data-id="'+b_id+'" class="form-control" rows="3" style="resize: none;"></textarea>'
                +'</div>'
                +'</div>'
                +'<div class="cut-off-rule bg-gray">'
                +'</div>'
    }

    //创建批量拒绝内容
    function createRefuseBody(b_id,b_name){
        return '<div class="form-group">'
                +'<label class="control-label col-sm-2">商家名称:</label>'
                +'<span id="businessName_'+b_id+'" class="col-sm-10" style="padding-top: 7px;">'+b_name+'</span>'
                +'</div>'
                +'<div class="form-group"  data-type="required" data-mark="审核意见">'
                +'<label class="control-label col-sm-2"><i class="required-mark">*</i>审核意见:</label>'
                +'<div class="col-sm-10" style="padding-top: 7px;">'
                +'<textarea id="auditContent_'+b_id+'"  data-id="'+b_id+'" class="form-control  dp-vd" rows="3" style="resize: none;"></textarea>'
                +'<p class="dp-error-msg"></p>'
                +'</div>'
                +'</div>'
                +'<div class="cut-off-rule bg-gray">'
                +'</div>'
    }
</script><!-- script区域end -->
