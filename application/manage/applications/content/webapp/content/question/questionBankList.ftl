<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/upload/ajaxupload.js</@ofbizContentUrl>"></script>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<style>
    .boxadd-group{padding:0 100px;}
    .boxadd{margin-right:20px;margin-bottom:20px;display:inline-block;}
    .boxadd2{margin-right:100px;margin-bottom:20px;display:inline-block;}
    .boxadd2 .fa-minus{margin-left:10px;color:green;}
    .boxadd .fa-minus{margin-left:10px;color:green;}
    .boxadd .tag-name{margin-left:10px;}
</style>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <input type="hidden" id="linkId"/>
        <input type="hidden" id="selectName"/>
        <!-- 条件查询start -->
        <form id="QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">序号</span>
                    <input type="text" id="questionId" class="form-control" value="">
                </div>
            </div>
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">类型</span>
                    <select class="form-control" id="questionType" name="questionType">
                        <option value="">全部</option>
                        <option value="0">常规</option>
                        <option value="1">画像</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">题干</span>
                    <input type="text" id="question" class="form-control" value="">
                </div>
            </div>
            <div class="input-group pull-right m-l-10">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <!-- 是否有新增权限-->
                <#if security.hasEntityPermission("QUESTIONBANK_LIST", "_CREATE", session)>
                    <button id="btn_add" class="btn btn-primary">
                        <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
                    </button>
                </#if>
                    <!-- 是否有删除权限-->
                <#if security.hasEntityPermission("QUESTIONBANK_LIST", "_DELETE", session)>
                    <button id="btn_del" class="btn btn-primary">
                        <i class="fa fa-trash"></i>${uiLabelMap.BrandDel}
                    </button>
                </#if>

                <#if security.hasEntityPermission("QUESTIONBANK_LIST", "_IMP", session)>
                    <button class="btn btn-primary" id="downTemplate">${uiLabelMap.BrandTemplate}</button>
                    <button class="btn btn-primary" id="questionImport">题库导入</button>
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
    </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- 删除确认弹出框start -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.BrandDel}</button>
            </div>
        </div>
    </div>
</div><!-- 删除确认弹出框end -->

<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->



<!-- 编辑弹出框start -->
<div id="modal_edit"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">编辑题目</h4>
            </div>
            <div class="modal-body">
                <form id="editForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl></@ofbizUrl>">
                    <input type="hidden" class="js-questionId"/>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="题干">
                            <label class="control-label col-sm-2"><i class="required-mark">*</i>题干:</label>
                            <div class="col-sm-8">
                                <textarea class="form-control dp-vd js-question" rows="6" value=""></textarea>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="类型">
                            <label class="control-label col-sm-2"><i class="required-mark">*</i>类型:</label>
                            <div class="col-sm-8">
                                <select class="form-control dp-vd js-questionType" >
                                    <option value="0">常规</option>
                                    <option value="1">画像</option>
                                </select>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>选项:</label>
                            <div class="col-sm-4">
                                <button type='button'  class="btn btn-danger btn-sm js-btn-plus">
                                    新增选项</i>
                                </button>
                                <#--<button type='button'  class="btn btn-danger btn-sm js-btn-sub">-->
                                    <#--<i class="fa fa-minus"></i>-->
                                <#--</button>-->
                            </div>
                        </div>
                    </div>


                    <div class="row">
                        <div class="form-group boxadd-group js-answer-item" data-type="required" data-mark="选项">



                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 编辑序号弹出框end -->


<!-- 新增弹出框start -->
<div id="modal_add"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">新增题目</h4>
            </div>
            <div class="modal-body">
                <form id="addForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl></@ofbizUrl>">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="题干">
                            <label class="control-label col-sm-2"><i class="required-mark">*</i>题干:</label>
                            <div class="col-sm-8">
                                <#--<input type="text" class="form-control dp-vd" id="sequenceId" name="sequenceId">-->
                                <textarea class="form-control dp-vd js-question" rows="6" value=""></textarea>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="类型">
                            <label class="control-label col-sm-2"><i class="required-mark">*</i>类型:</label>
                            <div class="col-sm-8">
                                <select class="form-control dp-vd js-questionType" >
                                    <option value="0">常规</option>
                                    <option value="1">画像</option>
                                </select>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>选项:</label>
                            <div class="col-sm-4">
                                <button type='button'  class="btn btn-danger btn-sm js-btn-plus">
                                    新增选项</i>
                                </button>
                                <#--<button type='button'  class="btn btn-danger btn-sm js-btn-sub">-->
                                    <#--<i class="fa fa-minus"></i>-->
                                <#--</button>-->
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group boxadd-group js-answer-item" data-type="required" data-mark="选项">

                            <span class="boxadd">
                                <input name="seq" data-id="1" type="checkbox"><input type="text" size=15" name="answer_1" value="" >
                            </span>
                            <span class="boxadd">
                                <input name="seq" data-id="2" type="checkbox"><input type="text" size=15" name="answer_2" value="" >
                            </span>

                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 编辑序号弹出框end -->


<!-- 导入弹出框start -->
<div id="modal_import"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_import_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_import_title">题库试题导入</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <div class="form-group">
                        <label class="control-label col-sm-2">${uiLabelMap.SelectFile}:</label>
                        <div class="col-sm-10 uploadFile">
                            <input type="text" class="form-control w-p80" style="float: left" disabled id="doc">
                            <input type="hidden" id="hidFileName" />
                            <input type="button" class="btn btn-default m-l-5" id="btnUpload" value="${uiLabelMap.Upload}" />
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="upload" type="button" class="btn btn-primary">${uiLabelMap.Import}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 导入弹出框end -->

<!-- 导入错误提示框start -->
<div id="modal_error"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_error_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_error_title">${uiLabelMap.Error}</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal">
                    <!-- 错误提示start -->
                    <div class="row" id="error_list">
                        <div class="col-xs-12">
                            <div class="box" style="border-top:0px">
                                <div class="box-header">
                                    <h3 class="box-title" style="color: red">
                                        <icon class="fa fa-times-circle"></icon>${uiLabelMap.ImportFail}
                                    </h3>
                                </div><!-- /.box-header -->
                                <div class="box-body no-padding" style="border: 1px solid #ddd;overflow-y: auto;height: 350px;" align="left">
                                    <table class="table table-condensed" >
                                        <tbody>
                                        </tbody>
                                    </table>
                                </div><!-- /.box-body -->
                            </div>
                        </div>
                    </div><!-- 错误提示end -->
                </form>
            </div>
            <div class="modal-footer">
                <button id="importAgain" type="button" class="btn btn-primary">${uiLabelMap.ImportAgain}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 导入错误提示框end -->


<!-- script区域start -->
<script>
    var data_tbl;
    var ajaxUrl = "questionBankList";
    var del_ids;
    var curQuestionIds="";
    var curAnswerTagIds="";


    // 创建一个上传参数
    var uploadOption =
    {
        action: "questionImport",    // 提交目标
        name: "file",                // 服务端接收的名称
        autoSubmit: false,          // 是否自动提交
        // 选择文件之后…
        onChange: function (file, extension)
        {
            $('#doc').val(file);
        },
        // 开始上传文件
        onSubmit: function (file, extension)
        {
            if (!(extension && /^(xls|XLS|xlsx|XLSX)$/.test(extension))) {
                alert("只支持xls或xlsx格式文件！");
                return false;
            }
        },
        // 上传完成之后
        onComplete: function (file, response)
        {
            var data = eval('(' + response + ')')
            if(data.success){
                $('#modal_import').modal('hide');
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html(data.successMsg);
                $('#modal_msg').modal();
                window.location.href="<@ofbizUrl>questionBankPage</@ofbizUrl>";
            }else{
                $('#error_list table tbody').empty();
                $('#error_list .box-header .box-title small').empty();
                $.each(data.errorMsg, function(){
                    $('#error_list table tbody').append("<tr><td style='text-align:left;color:#ff0000'>"+this.msg+"</td></tr>");
                });
                $('#error_list .box-header .box-title').append("<small style='color:red'>（共"+data.errorMsg.length+"条错误）</small>");
                $('#modal_import').modal('hide');
                $('#modal_error').modal();
            }
        }
    }

    $(function(){
        data_tbl = $('#data_tbl').dataTable({
            ajaxUrl: ajaxUrl,
            columns:[
                {"title":"复选框","code":"questionId","checked":true},
                {"title":"序号","code":"questionId"},
                {"title":"题干","code":"question"},
                {"title":"类型","code":"questionType",
                    "handle":function(td,record) {
                        var questionTypeName = "";
                        td.empty();
                        if (record.questionType == "0") {
                            questionTypeName = "常规";
                        } else if (record.questionType == "1") {
                            questionTypeName = "画像";
                        }
                        td.html(questionTypeName);
                    }
                },
                {"title":"操作","code":"option",
                    "handle":function(td,record){
                        var btns = "<div class='btn-group'>"+
                                <!-- 是否都有权限-->
                                <#if security.hasEntityPermission("QUESTIONBANK_LIST", "_UPDATE", session)>
                                "<button type='button' class='btn btn-danger btn-sm'  onclick='javascript:editQuestion("+record.questionId+")'>编辑</button>"+
                                </#if>
                                "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
                                "<span class='caret'></span>"+
                                "<span class='sr-only'>Toggle Dropdown</span>"+
                                "</button>"+
                                "<ul class='dropdown-menu' role='menu'>"+
                                "<li class='edit_li'>" +
                                <#if security.hasEntityPermission("QUESTIONBANK_LIST", "_DELETE", session)>
                                "<a href='javascript:setDelete("+record.questionId+")'>删除</a> </li>"+
                                </#if>
                                "</ul>"+

                                "</div>";
                        td.append(btns);
                    }
                }
            ],
            listName: "questionBankList",
            paginateEL: "paginateDiv",
            viewSizeEL: "view_size"
        });


        //查询按钮点击事件
        $('#QueryForm #searchBtn').on('click',function(){
            var question = $('#QueryForm #question').val();
            var questionId = $('#QueryForm #questionId').val();
            var questionType = $('#QueryForm #questionType').val();
            ajaxUrl = changeURLArg(ajaxUrl,"questionId",questionId);
            ajaxUrl = changeURLArg(ajaxUrl,"questionType",questionType);
            ajaxUrl = changeURLArg(ajaxUrl,"question",question);
            data_tbl.reload(ajaxUrl);
            return false;
        });
/////////////////////////////////////////////////////////////////////////////////////////////////

        //添加按钮点击事件
        $('#btn_add').click(function(){
            $('#modal_add .js-answer-item').empty();
            $('#modal_add .js-question').val('');
            $('#modal_add .js-questionType').val('0');
            createAnswerInfosBySize('A',2);

            $('#addForm input[name="seq"]').each(function(i,el){
                $(this).show();
                $('#addForm .js-tag').hide();
                $('#addForm .js-boxadd').removeClass('boxadd');
                $('#addForm .js-boxadd').addClass('boxadd2');
            });
            //新增弹出框内容
            $('#modal_add').modal();
        });


        $('#modal_add #save').click(function(){
            $("#addForm").dpValidate({
                clear: true
            });
            $('#addForm').submit();
        });

        $("#addForm").dpValidate({
            validate: true,
            callback: function(){
                var question=$("#addForm .js-question").val();
                var questionType=$("#addForm .js-questionType").val();
                var answerInfos=""
                var chkFlg="OK";
                $('#addForm input[name="seq"]').each(function(){
                    var curSeqId="";
                    var curAnswerContent="";
                    var curAnswerInfos="";
                    var curAnswerTagId="";
                    var curAnswerTagText="";
                    curSeqId=$(this).data("id");
                    curAnswerContent=$('#addForm input[name="answer_'+$(this).data("id")+'"]').val();
                    curAnswerTagId = $('#addForm select[name="tag_'+$(this).data("id")+'"]').val() !=null ? $('#addForm select[name="tag_'+$(this).data("id")+'"]').val().join(',') : '';
//                    curAnswerTagTest = $('#addForm select[name="tag_'+$(this).data("id")+'"]').find("option:selected").text();
                    curAnswerTagText =$('#addForm select[name="tag_'+$(this).data("id")+'"]').find("option:selected").text()!=null ? $('#addForm select[name="tag_'+$(this).data("id")+'"]').find("option:selected").text() : '';

                    // alert(curAnswerTagText.length);

                    if(curAnswerTagText.length>10){
                        chkFlg="NG";
                        return false;
                    }


                    curAnswerInfos=curSeqId+"^"+curAnswerContent+"^"+curAnswerTagId+"^"+curAnswerTagText;
                    console.log(curAnswerInfos);
                    if(answerInfos==""){
                        answerInfos=curAnswerInfos;
                    }else{
                        answerInfos=answerInfos+","+ curAnswerInfos;
                    }
                });

                if(chkFlg=='OK') {
                    var selSeqs = "";
                    $('#addForm input[name="seq"]:checked').each(function () {
                        var curSelSeqId = "";
                        curSelSeqId = $(this).data("id");
                        if (selSeqs == "") {
                            selSeqs = "" + curSelSeqId;
                        } else {
                            selSeqs = selSeqs + "," + curSelSeqId;
                        }
                    });

                    if (chkAnswerInfo('A', questionType, answerInfos, selSeqs) == '0') {
                        addQuestionInfo(question, questionType, answerInfos, selSeqs);
                    } else {
                        $.tipLayer("答案选项不正确！");
                    }
                }else{
                    $.tipLayer("标签长度不能超过10个字！");
                }

            }
        });


        //删除按钮点击事件
        $('#btn_del').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                del_ids = "";
                //编辑id字符串
                checks.each(function(){
                    del_ids += $(this).val() + ",";
                });

                //设置删除弹出框内容
                $('#modal_confirm #modal_confirm_body').html("确认删除勾选的记录吗？");
                $('#modal_confirm').modal('show');
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });

        //删除弹出框删除按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用删除方法
            $.ajax({
                url: "questionBankDel",
                type: "GET",
                data: {ids : del_ids},
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


        $('#modal_edit #save').click(function(){
            $("#editForm").dpValidate({
                clear: true
            });
            $('#editForm').submit();
        });

        $("#editForm").dpValidate({
            validate: true,
            callback: function(){
                var questionId=$("#editForm .js-questionId").val();
                var question=$("#editForm .js-question").val();
                var questionType = $('#editForm .js-questionType').val();

                var answerInfos=""
                var chkFlg="OK";
                $('#editForm input[name="seq"]').each(function(){
                    var curSeqId="";
                    var curAnswerContent="";
                    var curAnswerInfos="";
                    var curAnswerTagId="";
                    var curAnswerTagText="";
                    curSeqId=$(this).data("id");
                    curAnswerContent=$('#editForm input[name="answer_'+$(this).data("id")+'"]').val();
                    curAnswerTagId = $('#editForm select[name="tag_'+$(this).data("id")+'"]').val() !=null ? $('#editForm select[name="tag_'+$(this).data("id")+'"]').val().join(',') : '';
                    curAnswerTagText =$('#editForm select[name="tag_'+$(this).data("id")+'"]').find("option:selected").text()!=null ? $('#editForm select[name="tag_'+$(this).data("id")+'"]').find("option:selected").text() : '';


                    if(curAnswerTagText.length>10){
                        chkFlg="NG";
                        return false;
                    }
                    curAnswerInfos=curSeqId+"^"+curAnswerContent+"^"+curAnswerTagId+"^"+curAnswerTagText;
                    console.log(curAnswerInfos);
                    if(answerInfos==""){
                        answerInfos=curAnswerInfos;
                    }else{
                        answerInfos=answerInfos+","+ curAnswerInfos;
                    }
                });
                var selSeqs="";
                if(chkFlg=="OK") {
                    $('#editForm input[name="seq"]:checked').each(function () {
                        var curSelSeqId = "";
                        curSelSeqId = $(this).data("id");
                        if (selSeqs == "") {
                            selSeqs = ""+curSelSeqId;
                        } else {
                            selSeqs = selSeqs + "," + curSelSeqId;
                        }
                    });
                    console.log(selSeqs);
                    console.log(answerInfos);

                    if (chkAnswerInfo('U', questionType, answerInfos, selSeqs) == '0') {
                        updateQuestionInfo(questionId, question, questionType, answerInfos, selSeqs);
                    } else {
                        $.tipLayer("答案选项不正确！");
                    }
                }else{
                    $.tipLayer("标签长度不能超过10个字！");
                }
            }
        });


        $("#addForm .js-btn-plus").click(function(){
            var aLength= $('#addForm input[name="seq"]').length;
            if(aLength==8){
                $.tipLayer("选项最多8条！");
            }else {
                addAnwerItemTemplete('A');
            }
        });

        $("#addForm .js-btn-sub").click(function(){
            subAnwerItemTemplete('A');
        });

        $("#editForm .js-btn-plus").click(function(){
            var uLength= $('#editForm input[name="seq"]').length;
            if(uLength==8){
                $.tipLayer("选项最多8条！");
            }else {
                addAnwerItemTemplete('U');
            }

        });

        $("#editForm .js-btn-sub").click(function(){
            subAnwerItemTemplete('U');
        });


        $('#addForm .js-questionType').change(function(){
            if( $('#addForm .js-questionType').val()=='1'){
                $('#addForm input[name="seq"]').each(function(){
                    $(this).hide();
                    $('#addForm .js-tag').show();
                    $('#addForm .js-boxadd').removeClass('boxadd2');
                    $('#addForm .js-boxadd').addClass('boxadd');
                });

            }else{
                $('#addForm input[name="seq"]').each(function(i,el){
                    $(this).show();
                    $('#addForm .js-tag').hide();
                    $('#addForm .js-boxadd').removeClass('boxadd');
                    $('#addForm .js-boxadd').addClass('boxadd2');
                });

            }
        });

        $('#editForm .js-questionType').change(function(){
            if( $('#editForm .js-questionType').val()=='1'){
                $('#editForm input[name="seq"]').each(function(){
                    $(this).hide();
                    $('#editForm .js-tag').show();
                    $('#editForm .js-boxadd').removeClass('boxadd2');
                    $('#editForm .js-boxadd').addClass('boxadd');
                });
            }else{
                $('#editForm input[name="seq"]').each(function(){
                    $(this).show();
                    $('#editForm .js-tag').hide();
                    $('#editForm .js-boxadd').removeClass('boxadd');
                    $('#editForm .js-boxadd').addClass('boxadd2');
                });
            }
        });

        //下载模板按钮点击事件
        $('#downTemplate').click(function(){
            window.location="<@ofbizContentUrl>/images/importTemplate/Question/questionTemplate.xlsx</@ofbizContentUrl>";
        });


        // 题库导入按钮点击事件
        $('#questionImport').click(function(){
            $('#doc').val("");
            $('#modal_import').modal();
        });

        // 初始化图片上传框
        var au = new AjaxUpload($('#btnUpload'), uploadOption);

        // 导入按钮点击事件
        $('#upload').click(function(){
            au.submit();
        });


        // 答案内容行删除
        $(document).on('click','#modal_add .js-item-minus',function(){
            subAnwerItemTemplete($(this),'A');
        });

        $(document).on('click','#modal_edit .js-item-minus',function(){
            subAnwerItemTemplete($(this),'U');
//            $(this).parent().remove();
//            $('#addForm input[name="seq"]').each(function(i,el){
//
//                strCurAnswerInfo=  '<span class="boxadd2">'+
//                        '<input name="seq" data-id="'+(i+1)+'" type="radio">'+getCodeBySeqId(i+1)+'：<input type="text" size=15" name="answer_'+(i+1)+'" value="" >'+
//                        '<i class="fa fa-minus js-item-minus"></i></span>';
//                strAnswerInfos=strAnswerInfos+strCurAnswerInfo;
//
//            });
//            $('#modal_add .js-answer-item').empty();
//            $('#modal_add .js-answer-item').append(strAnswerInfos);
        });
    });

    //行删除操作
    function setDelete(id){
        del_ids = id;
        //设置提示弹出框内容
        $('#modal_confirm #modal_confirm_title').html("删除提示");
        $('#modal_confirm #modal_confirm_body').html("该题库信息信息删除后将无法使用，是否继续？");
        $('#modal_confirm').modal('show');
    }

    // 编辑题库信息
    function editQuestion(id){
        //清空序号
        $('#modal_edit .js-questionId').val('');
        $.ajax({
            url: "questionBankEditInit",
            type: "POST",
            data : {questionId:id
            },
            dataType : "json",
            success: function(data){
                if(data){
                    var questionId=data.questionId;
                    var question=data.question;
                    var questionType=data.questionType;
                    var answerResult=data.answerResult;
                    var answerList=data.answerList;
                    console.log(answerList);

                    $('#modal_edit .js-questionId').val(questionId);
                    $('#modal_edit .js-question').val(question);
                    $('#modal_edit .js-questionType').val(questionType);
                    //初始化答案列表
                    if(answerList){
                        var answerSize=answerList.length;
                        $('#modal_edit .js-answer-item').empty();
                        createAnswerInfosBySize('U',answerSize);

                        $('#editForm input[name="seq"]').each(function(){
                            var curSeqId="";
                            curSeqId=$(this).data("id");
                            $('#editForm input[name="answer_'+$(this).data("id")+'"]').val(answerList[curSeqId-1].answer);
                            $('#editForm input[name="answer_'+$(this).data("id")+'"]').data("id",answerList[curSeqId-1].answerId);
                            var curAnswerId=answerList[curSeqId-1].answerId;
                            var chkFlg=chkResultStatus(answerResult,curAnswerId);
                            if(chkFlg){
                                $(this).prop("checked","true");
                            }
                            if(questionType=='1'){
                                $('#editForm select[name="tag_'+$(this).data("id")+'"]').val(answerList[curSeqId-1].tagId.split(',')).trigger("change");
                            }
                        });
                        questionTypeChangeStatus("U");
                    }
                }

            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
        //设置提示弹出框内容
        $('#modal_edit').modal();
    }
    /**
     * 更新题库信息
     * @param questionId
     * @param question
     * @param questionType
     * @param answerInfos
     * @param answerResult
     */
    function updateQuestionInfo(questionId,question,questionType,answerInfos,answerResult){
            //异步调用编辑序号方法
            console.log(questionId);
            console.log(question);
            console.log(questionType);
            console.log(answerInfos);
            console.log(answerResult);

        $.ajax({
            url: "questionBankEdit",
            type: "POST",
            data: {questionId:questionId,
                question : question,
                questionType:questionType,
                answerInfos:answerInfos,
                answerResult:answerResult
            },
            dataType : "json",
            success: function(data){
                $.tipLayer("操作成功！");
                window.location = '<@ofbizUrl>questionBankPage</@ofbizUrl>';
            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
    }

    /**
     * 添加题库信息
     * @param question
     * @param questionType
     * @param answerInfos
     * @param answerResult
     */
    function addQuestionInfo(question,questionType,answerInfos,answerResult){
        //异步调用添加方法
        $.ajax({
            url: "questionBankAdd",
            type: "POST",
            data: {question : question,
                questionType:questionType,
                answerInfos:answerInfos,
                answerResult:answerResult
            },
            dataType : "json",
            success: function(data){
                $.tipLayer("操作成功！");
                window.location = '<@ofbizUrl>questionBankPage</@ofbizUrl>';
            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
    }

    /**
     * 根据答案的个数生成答案列表
     */
    function createAnswerInfosBySize(modetype,answerSize){;
        anwerItemTemplete(modetype,answerSize);
    }


    //更新时答案选项初始化
    function anwerItemTemplete(modetype,counts){
        var strAnswerInfos="";
        var strCurAnswerInfo="";
        var strCurAnswerInfo="";
        if(counts){
            for(var i=0;i<counts;i++){
//                strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
//                        '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="" ><span class="js-tag">'+
//                         getCodeBySeqId(i+1)+'标签：</span><input  class="js-tag" type="text" size=15" name="tag_'+(i+1)+'" value="" ><i class="fa fa-minus js-item-minus"></i></span>';
                strCurAnswerInfo=getAnswerItemInfo(i);
                strAnswerInfos=strAnswerInfos+strCurAnswerInfo;
            }
        }
        if(modetype=="A"){
            $('#modal_add .js-answer-item').append(strAnswerInfos);
            $("#modal_add .select2").select2({
                tags: true,
                maximumSelectionLength: 1  //最多能够选择的个数
            });
        }
        if(modetype=="U") {
            $('#modal_edit .js-answer-item').append(strAnswerInfos);
            $("#modal_edit .select2").select2({
                tags: true,
                maximumSelectionLength: 1  //最多能够选择的个数
            });
        }
    }


    //添加答案选项
    function addAnwerItemTemplete(modetype){
        var strCurAnswerInfo="";
        if(modetype=="A"){
            var aLength= $('#addForm input[name="seq"]').length;
            strCurAnswerInfo=getAnswerItemInfo(aLength);
            $('#modal_add .js-answer-item').append(strCurAnswerInfo);
            questionTypeChangeStatus("A");
            $("#modal_add .select2").select2({
                tags: true,
                maximumSelectionLength: 1  //最多能够选择的个数
            });
        }
        if(modetype=="U"){
            var uLength= $('#editForm input[name="seq"]').length;
            strCurAnswerInfo=getAnswerItemInfo(uLength);
            $('#modal_edit .js-answer-item').append(strCurAnswerInfo)
            questionTypeChangeStatus("U");
            $("#modal_edit .select2").select2({
                tags: true,
                maximumSelectionLength: 1  //最多能够选择的个数
            });
        }
    }


    //删除答案选项
    function subAnwerItemTemplete(curitem ,modetype){
        var strAnswerInfos="";
        var strCurAnswerInfo="";
        curAnswerTagIds="";
        if(modetype=="A"){

//            var aLength= $('#addForm input[name="seq"]').length;
//            $('#addForm input[name="answer_'+aLength+'"]').parent().remove();
            curitem.parent().remove();
            var curQuestionType=$('#modal_add js-questionType').val();

            var addNum=1;
            $('#addForm input[name="seq"]').each(function(i,el){
//                strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
//                                 '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="" ><span class="js-tag">'+
//                                getCodeBySeqId(i+1)+'标签：</span><input  class="js-tag" type="text" size=15" name="tag_'+(i+1)+'" value="" ><i class="fa fa-minus js-item-minus"></i></span>';
//                 strCurAnswerInfo=getAnswerItemInfo(i);

                var curIndex=i+addNum;
                if($('#addForm input[name="answer_'+curIndex+'"]').length==0){
                    addNum=2;
                    curIndex=i+addNum;
                }
                var curAnswerContent=$('#addForm input[name="answer_'+curIndex+'"]').val()!=null?$('#addForm input[name="answer_'+curIndex+'"]').val():'';
                var curAnswerTagId = $('#addForm select[name="tag_'+curIndex+'"]').val() !=null ? $('#addForm select[name="tag_'+curIndex+'"]').val().join(',') : 'isEmpty';
                console.log(curAnswerContent);
                console.log(curAnswerTagId);
                strCurAnswerInfo=getAnswerItemInfoByVal(i,curAnswerContent);
                strAnswerInfos=strAnswerInfos+strCurAnswerInfo;
                if(curAnswerTagIds==""){
                    curAnswerTagIds=curAnswerTagId;
                }else{
                    curAnswerTagIds=curAnswerTagIds+","+curAnswerTagId;
                }
            });
            $('#modal_add .js-answer-item').empty();
            $('#modal_add .js-answer-item').append(strAnswerInfos);
            $("#modal_add .select2").select2({
                tags: true,
                maximumSelectionLength: 1  //最多能够选择的个数
            });
            questionTypeChangeStatus(modetype);

        }
        if(modetype=="U"){
//            var uLength= $('#editForm input[name="seq"]').length;
//            $('#editForm input[name="answer_'+uLength+'"]').parent().remove();

            curitem.parent().remove();
            var curQuestionType=$('#modal_add js-questionType').val();
            var addNum=1;
            $('#editForm input[name="seq"]').each(function(i,el){
//                strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
//                                 '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="" ><span class="js-tag">'+
//                                getCodeBySeqId(i+1)+'标签：</span><input  class="js-tag" type="text" size=15" name="tag_'+(i+1)+'" value="" ><i class="fa fa-minus js-item-minus"></i></span>';
//                strCurAnswerInfo=getAnswerItemInfo2(i)

                if($('#editForm input[name="answer_'+curIndex+'"]').length==0){
                    addNum=2;
                    curIndex=i+addNum;
                }
                var curAnswerContent=$('#editForm input[name="answer_'+curIndex+'"]').val()!=null?$('#editForm input[name="answer_'+curIndex+'"]').val():'';
                var curAnswerTagId = $('#editForm select[name="tag_'+curIndex+'"]').val() !=null ? $('#editForm select[name="tag_'+curIndex+'"]').val().join(',') : 'isEmpty';
                strCurAnswerInfo=getAnswerItemInfoByVal(i,curAnswerContent);
                strAnswerInfos=strAnswerInfos+strCurAnswerInfo;
                if(curAnswerTagIds==""){
                    curAnswerTagIds=curAnswerTagId;
                }else{
                    curAnswerTagIds=curAnswerTagIds+","+curAnswerTagId;
                }
            });
            $('#modal_edit .js-answer-item').empty();
            $('#modal_edit .js-answer-item').append(strAnswerInfos);
            $("#modal_edit .select2").select2({
                tags: true,
                maximumSelectionLength: 1  //最多能够选择的个数
            });
            questionTypeChangeStatus(modetype);


        }
    }





    /**
     * 答案项验证
     * @param answerResults
     * @param curResult
     * @returns {boolean}
     */
    function chkResultStatus(answerResults,curResult){

        var chkFlg=false;
        if(answerResults){
            var resultList=answerResults.split(',')
            for(var i=0;i<resultList.length;i++){
                curId=resultList[i];
                if(curResult==curId){
                    chkFlg=true;
                }
            }
        }
        return chkFlg;
    }


    function getCodeBySeqId(seqId){
        console.log(seqId);
        var resultCode="";
        switch(seqId){
            case 1:
                resultCode='A';
                break;
            case 2:
                resultCode='B';
                break;
            case 3:
                resultCode='C';
                break;
            case 4:
                resultCode='D';
                break;
            case 5:
                resultCode='E';
                break;
            case 6:
                resultCode='F';
                break;
            case 7:
                resultCode='G';
                break;
            case 8:
                resultCode='H';
                break;
            default:
                break;
        }
        return resultCode;
    }

    // 题库类型改变状态后的响应
    function questionTypeChangeStatus(modetype){
        if(modetype=="A") {
            if ($('#addForm .js-questionType').val() == '1') {
                var tagIds=""
                if(curAnswerTagIds){
                    tagIds=curAnswerTagIds.split(",");
                    if(tagIds){
                        for(var i=0;i<tagIds.length;i++){
                            var curTagId=tagIds[i];
                            if(curTagId!="isEmpty") {
                                $('#addForm select[name="tag_'+(i+1)+'"]').val(curTagId.split(',')).trigger("change");
                            }
                        }
                    }
                }
                $('#addForm input[name="seq"]').each(function () {
                    $(this).hide();
                    $('#addForm .js-tag').show();
                    $('#addForm .js-boxadd').removeClass('boxadd2');
                    $('#addForm .js-boxadd').addClass('boxadd');
                });

            } else {
                $('#addForm input[name="seq"]').each(function (i, el) {
                    $(this).show();
                    $('#addForm .js-tag').hide();
                    $('#addForm .select2').hide();
                    $('#addForm .js-boxadd').removeClass('boxadd');
                    $('#addForm .js-boxadd').addClass('boxadd2');
                });

            }
        }

        if(modetype=="U"){
            if ($('#editForm .js-questionType').val() == '1') {
                var tagIds=""
                if(curAnswerTagIds){
                    tagIds=curAnswerTagIds.split(",");
                    if(tagIds){
                        for(var i=0;i<tagIds.length;i++){
                            var curTagId=tagIds[i];
                            if(curTagId!="isEmpty") {
                                $('#editForm select[name="tag_'+(i+1)+'"]').val(curTagId.split(',')).trigger("change");
                            }
                        }
                    }
                }
                $('#editForm input[name="seq"]').each(function () {
                    $(this).hide();
                    $('#editForm .js-tag').show();
                    $('#editForm .js-boxadd').removeClass('boxadd2');
                    $('#editForm .js-boxadd').addClass('boxadd');
                });

            } else {
                $('#addForm input[name="seq"]').each(function (i, el) {
                    $(this).show();
                    $('#editForm .js-tag').hide();
                    $('#editForm .js-boxadd').removeClass('boxadd');
                    $('#editForm .js-boxadd').addClass('boxadd2');
                });

            }
        }
    }


    function getAnswerItemInfo(i){
//        var strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
//                '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="" ><span class="js-tag">'+
//                getCodeBySeqId(i+1)+'标签：</span><input  class="js-tag" type="text" size=15" name="tag_'+(i+1)+'" value="" ><i class="fa fa-minus js-item-minus"></i></span>';

        <#assign tagList = delegator.findByAnd("Tag",{"tagTypeId":"QuestionTag","isDel":"N"}) >
        var strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
                '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="" ><span class="js-tag">'+
                getCodeBySeqId(i+1)+'标签：</span><span class="js-tag"><select  class=" select2 " name="tag_'+(i+1)+'"multiple="multiple" data-placeholder="请输入标签">';
        <#if tagList?has_content>
            <#list tagList as tag >
                strCurAnswerInfo=strCurAnswerInfo+'<option value="${tag.tagId}">${tag.tagName}</option>';
            </#list>
        </#if>
        strCurAnswerInfo=strCurAnswerInfo+'</select></span><i class="fa fa-minus js-item-minus"></i></span>';
        return strCurAnswerInfo;
    }



    function getAnswerItemInfo2(i){
        <#assign tagList = delegator.findByAnd("Tag",{"tagTypeId":"QuestionTag","isDel":"N"}) >
            var strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
                    '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="'+$('#editForm input[name="answer_'+(i+1)+'"]').val()+'" ><span class="js-tag">'+
                    getCodeBySeqId(i+1)+'标签：</span><span class="js-tag"><select  class=" select2 " name="tag_'+(i+1)+'"multiple="multiple" data-placeholder="请输入标签">';
        <#if tagList?has_content>
            <#list tagList as tag >
                strCurAnswerInfo=strCurAnswerInfo+'<option value="${tag.tagId}">${tag.tagName}</option>';
            </#list>
        </#if>
        strCurAnswerInfo=strCurAnswerInfo+'</select></span><i class="fa fa-minus js-item-minus"></i></span>';
        return strCurAnswerInfo;


    }


    function getAnswerItemInfoByVal(i,answerContent){
//        var strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
//                '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="" ><span class="js-tag">'+
//                getCodeBySeqId(i+1)+'标签：</span><input  class="js-tag" type="text" size=15" name="tag_'+(i+1)+'" value="" ><i class="fa fa-minus js-item-minus"></i></span>';

        <#assign tagList = delegator.findByAnd("Tag",{"tagTypeId":"QuestionTag","isDel":"N"}) >
        var strCurAnswerInfo=  '<span class="boxadd js-boxadd">'+
                '<input name="seq" data-id="'+(i+1)+'" type="radio"><span>'+getCodeBySeqId(i+1)+'</span>：<input type="text" size=15" name="answer_'+(i+1)+'" value="'+answerContent+'" ><span class="js-tag">'+
                getCodeBySeqId(i+1)+'标签：</span><span class="js-tag"><select  class=" select2 " name="tag_'+(i+1)+'"multiple="multiple" data-placeholder="请输入标签">';
        <#if tagList?has_content>
            <#list tagList as tag >
                strCurAnswerInfo=strCurAnswerInfo+'<option value="${tag.tagId}">${tag.tagName}</option>';
            </#list>
        </#if>
        strCurAnswerInfo=strCurAnswerInfo+'</select></span><i class="fa fa-minus js-item-minus"></i></span>';
        return strCurAnswerInfo;
    }


    /**
     * 答案项检查
     * @param question
     * @param questionType
     * @param answerInfos
     * @param answerResult
     */
    function chkAnswerInfo(modetype,questionType,answerInfos,answerResult){

        var chkFlg='0';
        var answerInfos=answerInfos.split(',');
        var answerInfoSize=answerInfos.length;
        var answerResultInfos=answerResult.split(',');
        var answerResultSize= answerResultInfos.length;
        if(questionType=='0') {
            if (answerInfoSize < 2) {
                chkFlg = '1';
            } else {
                for (var i = 0; i < answerInfoSize; i++) {
                    var curName='answer_'+(i+1);
                    var answerContents=$('#addForm input[name="'+curName+'"]').val();
                    if(modetype=='U'){
                        answerContents=$('#editForm input[name="'+curName+'"]').val();
                    }
                    if(!answerContents){
                        chkFlg = '2';
                    }
                }
                if(answerResult==""){
                    chkFlg = '3';
                }
            }
        }else if(questionType=='1'){
            if (answerInfoSize < 2) {
                chkFlg = '4';
            } else {
                for (var i = 0; i < answerInfoSize; i++) {
                    var curName='answer_'+(i+1);
                    var answerContents=$('#addForm input[name="'+curName+'"]').val();
                    if(modetype=='U'){
                        answerContents=$('#editForm input[name="'+curName+'"]').val();
                    }
                    if(!answerContents){
                        chkFlg = '5';
                    }
                    var curTagName="tag_"+(i+1);
                    var answerTagInfo=$('#addForm select[name="'+curTagName+'"]').find("option:selected").text();
                    if(modetype=='U'){
                        answerTagInfo=$('#editForm select[name="'+curTagName+'"]').find("option:selected").text();
                    }
                    if(!answerTagInfo){
                        chkFlg = '6';
                    }
                }
            }
        }

        return chkFlg;


    }

</script><!-- script区域end -->
