<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">会员编码</span>
                    <input type="text" id="partyId" name="partyId" class="form-control"
                           value="">
                </div>
            <#--  <div class="input-group m-b-10">
                  <span class="input-group-addon">会员分类</span>
                  <select class="form-control" id="partyCategory">
                      <option value="">全部</option>
                      <option value="MEMBER">个人会员</option>
                      <option value="COMPANY">企业会员</option>
                  </select>
              </div>-->

                <#--<div class="input-group m-b-10">-->
                    <#--<span class="input-group-addon">会员等级</span>-->
                    <#--<select class="form-control" id="levelId">-->
                        <#--<option value="">全部</option>-->
                    <#--<#if levelList?has_content >-->
                        <#--<#list levelList as level>-->
                            <#--<option value="${(level.levelId)?if_exists}"-->
                                    <#--<#if levelId?default("")==level.levelId>selected</#if>>${level.levelName?if_exists}</option>-->
                        <#--</#list>-->
                    <#--</#if>-->
                    <#--</select>-->
                <#--</div>-->


                <div class="input-group m-b-10">
                    <span class="input-group-addon">登录账号</span>
                    <input type="text" id="userLoginId" class="form-control" value="">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">真实姓名</span>
                    <input type="text" id="name" class="form-control"
                           value="">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">手机号</span>
                    <input type="text" id="mobile" class="form-control"
                           value="">
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
                <a class="btn btn-primary" href="<@ofbizUrl>memberAdd</@ofbizUrl>" ;> 添加 </a>
                <button class="btn btn-primary" onclick="exportMember()">导出</button>
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

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.MemberOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary"
                        data-dismiss="modal">${uiLabelMap.MemberOk}</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->

<!-- 修改弹出框start -->
<div id="modal_edit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">${uiLabelMap.AdjustEdit} <span
                        style="color:red;font-size:4px;">(调整余额可填写正负数，正数增加余额，负数减少余额)</span></h4>

            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form"
                      action="<@ofbizUrl>adjustAmount</@ofbizUrl>">
                    <input type="hidden" name="partyId"/>
                    <div class="form-group">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.MemberAmount}:</label>
                        <div class="col-sm-10">
                            <p class="form-control-static" id="oldamount" name="oldamount"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="format" data-reg="/^\-?[1-9]+\d*(\.\d{1,2})?$/"
                         data-mark="${uiLabelMap.AdjustAcount}">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>${uiLabelMap.AdjustAcount}:</label>


                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="adjustamount" maxlength="11"
                                   name="amount">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="${uiLabelMap.AdjustCause}">
                        <label class="control-label col-sm-2"><i
                                class="required-mark">*</i>${uiLabelMap.AdjustCause}:</label>
                        <div class="col-sm-10">
                            <textarea class="form-control dp-vd" rows="6" name="adjustCause"
                                      id="adjustCause"></textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default" j
                        data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 修改弹出框end -->

<!-- 余额明细begin -->
<div id="modal_list" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_list_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_list_title">${uiLabelMap.AdjustList}</h4>
            </div>
            <div class="modal-body">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr class="js-sort-list">
                        <th>${uiLabelMap.AdjustAmount}</th>
                        <th>${uiLabelMap.Operator}</th>
                        <th>${uiLabelMap.EditDate}</th>
                        <th>${uiLabelMap.AdjustDescription}</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<!-- 余额明细end -->

<!-- 商家修改密码start -->
<div id="modal_edit_password" class="modal fade " tabindex="-1" role="dialog"
     aria-labelledby="modal_edit_passwordtitle">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_audit_title">修改密码</h4>
            </div>
            <div class="modal-body">
                <form id="PartyEditPwd_Form" class="form-horizontal">
                    <#--<input type="hidden" id="userLoginId"/>-->
                    <div class="form-group">
                        <input type="hidden" id="userLoginId1"/>
                        <label class="control-label col-sm-3">登录账号:</label>
                        <span id="userLoginId2" class="col-sm-9" style="padding-top: 7px;"></span>
                    </div>
                    <div class="form-group" data-type="min" data-number="6" data-msg="必填项，最小位数不能低于6位！">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>新密码:</label>
                        <div class="col-sm-6" style="padding-top: 7px;">
                            <input type="password" class="form-control dp-vd" id="password_1">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="min" data-number="6" data-msg="必填项，最小位数不能低于6位！">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>新密码验证:</label>
                        <div class="col-sm-6" style="padding-top: 7px;">
                            <input type="password" class="form-control dp-vd" id="password_2">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">提交</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 商家修改密码end -->
<!-- script区域start -->
<script>
    var data_tbl;
    var ajaxUrl = "find_memberForJson";
    $(function () {
        data_tbl = $('#data_tbl').dataTable({
            ajaxUrl: ajaxUrl,
            columns: [
                {"title": "会员编码", "code": "partyId", "sort": true},
                {
                    "title": "是否为商家",
                    "handle": function (td, record) {
                        var partyCategory;
                        if (record.partyCategory == 'MEMBER') {
                            partyCategory = "否";
                        } else {
                            partyCategory = "是";
                        }
                        td.append(partyCategory);
                    }
                },
                {"title": "登录账号", "code": "userLoginId", "sort": true},
                {"title": "真实姓名", "code": "name"},
                {
                    "title": "性别",
                    "handle": function (td, record) {
                        var sex;
                        if (record.gender == 'F') {
                            sex = "女";
                        } else {
                            sex = "男";
                        }
                        td.append(sex);
                    }
                },
                {"title": "手机/联系方式", "code": "mobile"},
//                {"title": "会员等级", "code": "levelName"},
//                {"title": "账户余额", "code": "amount"},
                {
                    "title": "操作", "code": "option",
                    "handle": function (td, record) {
                        var btns = "<div class='btn-group'>" +
                                "<button type='button' class='btn btn-danger btn-sm' onclick='location.href=\"member_Detail?partyId=" + record.partyId + "\"'>查看详情</button>" +
                                "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>" +
                                "<span class='caret'></span>" +
                                "<span class='sr-only'>Toggle Dropdown</span>" +
                                "</button>" +
                                "<ul class='dropdown-menu' role='menu'>" +
                                <!-- 是否有余额调整权限-->
                        <#if security.hasEntityPermission("PARTYMGR_AMOUNT", "_UPDATE", session)>
                                "<li><a href='<@ofbizUrl>memberEdit</@ofbizUrl>?partyId=" + record.partyId + "' >编辑</a></li>" +
                        </#if>
                                <!-- 是否有余额明细权限-->
                        <#if security.hasEntityPermission("PARTYMGR_AMOUNT", "_VIEW", session)>
                                "<li><a href='javascript:goEditPWD(\"" + record.userLoginId + "\")'>修改密码</a></li>" +
                        </#if>
                                <!-- 是否有余额明细权限-->
                        <#--<#if security.hasEntityPermission("PARTYMGR_AMOUNT", "_VIEW", session)>
                                "<li><a href='javascript:detailView(\"" + record.partyId + "\")'>余额明细</a></li>" +
                        </#if>
                                <!-- 是否有余额调整权限&ndash;&gt;
                        <#if security.hasEntityPermission("PARTYMGR_AMOUNT", "_UPDATE", session)>
                                "<li><a href='javascript:editInit(\"" + record.partyId + "\",\"" + record.amount + "\")'>余额调整</a></li>" +
                        </#if>-->

                                "</ul>" +
                                "</div>";
                        td.append(btns);
                    }
                }
            ],
            listName: "recordsList",
            paginateEL: "paginateDiv",
            viewSizeEL: "view_size"
        });

        //查询按钮点击事件
        $('#QueryForm #searchBtn').on('click', function () {

            var partyId = $('#QueryForm #partyId').val();
//            var partyCategory = $('#QueryForm #partyCategory').val();
//            var levelId = $('#QueryForm #levelId').val();
            var userLoginId = $('#QueryForm #userLoginId').val();
            var name = $('#QueryForm #name').val();
            var mobile = $('#QueryForm #mobile').val();

            ajaxUrl = changeURLArg(ajaxUrl, "partyId", partyId);
            // ajaxUrl = changeURLArg(ajaxUrl, "partyCategory", partyCategory);
            // ajaxUrl = changeURLArg(ajaxUrl, "levelId", levelId);
            ajaxUrl = changeURLArg(ajaxUrl, "userLoginId", userLoginId);
            ajaxUrl = changeURLArg(ajaxUrl, "name", name);
            ajaxUrl = changeURLArg(ajaxUrl, "mobile", mobile);
            data_tbl.reload(ajaxUrl);
            return false;
        });

        //修改弹出框保存按钮点击事件
        $('#modal_edit #save').click(function () {
            $('#EditForm').dpValidate({
                clear: true
            });

            //减少余额不能大于实际金额
            var oldamount = $('#modal_edit #oldamount').text() || 0,
                    amount = parseFloat($("#modal_edit #adjustamount").val());
            if ((parseFloat(oldamount) + amount) < 0) {
                $("#modal_edit #adjustamount").next('p').text('调整余额不能大于账户余额');
                return false;
            }
            $('#EditForm').submit();
        });
        //表单验证
        $('#EditForm').dpValidate({
            validate: true,
            callback: function () {
                var partyId = $("#modal_edit #partyId").val();
                var amount = parseFloat($("#modal_edit #adjustamount").val());
                var adjustCause = $("#modal_edit #adjustCause").val();

                //异步调用修改方法
                $.ajax({
                    url: "adjustAmount",
                    type: "POST",
                    data: {
                        partyId: partyId,
                        amount: amount,
                        adjustCause: adjustCause
                    },
                    dataType: "json",
                    success: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            data_tbl.reload(ajaxUrl);
                        })
                    },
                    error: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

    })

    //点击编辑按钮事件
    function editInit(id, value) {
        //清空form
        clearForm($("#EditForm"));
        $("#modal_edit #partyId").val(id);
        $("#modal_edit #oldamount").text(value);
        $('#modal_edit').modal();
    }

    //点击查看余额明细
    function detailView(id) {
        $.ajax({
            url: "adjustAmountHistory",
            type: "POST",
            data: {partyId: id},
            dataType: "json",
            success: function (data) {
                var content,
                        list = data.partyAccountDetailList;
                //清空表格,避免叠加
                $('#modal_list tbody').empty();
                for (var i = 0; i < list.length; i++) {
                    content += "<tr>";
                    content += '<td>' + list[i].amount + '</td>';
                    content += '<td>' + list[i].operator + '</td>';
                    content += '<td>' + list[i].createDate + '</td>';
                    content += '<td>' + list[i].description + '</td>';
                    content += '</tr>';
                }
                $('#modal_list tbody').append(content);
                //明细列表窗口
                $('#modal_list').modal();
            },
            error: function (data) {

            }
        });
    }

    //修改密码
    function goEditPWD(userLoginId) {
        clearForm($('#modal_edit_password'));
        $('#modal_edit_password').find('input#userLoginId1').val(userLoginId);
        $('#modal_edit_password').find('span#userLoginId2').text(userLoginId);
        $('#modal_edit_password').modal();
    }

    //修改密码弹出框的保存按钮事件
    $('#modal_edit_password #save').click(function () {
        $("#modal_edit_password #PartyEditPwd_Form").dpValidate({
            clear: true
        });
        $("#modal_edit_password #PartyEditPwd_Form").submit();
    });

    //修改密码弹出窗关闭事件
    $('#modal_edit_password').on('hide.bs.modal', function () {
        $('#PartyEditPwd_Form').dpValidate({
            clear: true
        });
    });


    //修改密码表单校验方法
    $('#modal_edit_password #PartyEditPwd_Form').dpValidate({
        validate: true,
        callback: function(){
            var userLoginId = $('#modal_edit_password').find('input#userLoginId1').val(),
                    password_1 = $('#modal_edit_password').find('input#password_1').val(),
                    password_2 = $('#modal_edit_password').find('input#password_2').val();
            if(password_2 != password_1){
                $('#modal_edit_password').find('input#password_2').closest('.form-group').addClass('has-error');
                $('#modal_edit_password').find('input#password_2').focus().siblings('p').text('两次输入的密码不一致！');
            }else{
                $.ajax({
                    url: "UpdatePasswordForParty",
                    type: "GET",
                    data: {userLoginId : userLoginId,
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

    function exportMember() {
        $.confirmLayer({
            msg: '确定导出所有会员信息吗',
            confirm: function () {
                window.location.href="<@ofbizUrl>exportMember</@ofbizUrl>";

                $('#confirmLayer').modal('hide');


            }
        })
    }

</script>
