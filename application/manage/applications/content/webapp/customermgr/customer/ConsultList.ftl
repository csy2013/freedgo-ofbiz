<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" class="form-inline clearfix" >
            <div class="form-group w-p100">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">用户</span>
                    <input type="text" class="form-control"  id="nickname1" name="nickname" value=""/>
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" class="form-control" id="productName1" name="productName" value=""/>
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">咨询类型</span>
                    <select class="form-control" id="consultType1" name="consultType">
                        <option value="">全部</option>
                        <option value="PRODUCT">商品咨询</option>
                        <option value="FACILITY">库存配送</option>
                        <option value="PAY">支付</option>
                        <option value="INVOICE">发票与保修</option>
                        <option value="PROMO">促销及赠品</option>
                    </select>
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">是否回复</span>
                    <select class="form-control" id="isReply1" name="isReply">
                        <option value="">全部</option>
                        <option value="Y">是</option>
                        <option value="N">否</option>
                    </select>
                </div>

                <#--<div class="input-group m-b-10">-->
                    <#--<span class="input-group-addon">商家</span>-->
                    <#--<input type="text" class="form-control" id="businessName1" name="businessName" value=""/>-->
                <#--</div>-->

                <div class="input-group m-b-10">
                    <span class="input-group-addon">内容</span>
                    <input type="text" class="form-control" id="consultContent1" name="consultContent" value=""/>
                </div>
                
                <div class="input-group pull-right">
	                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
	            </div>
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
                    <#if security.hasEntityPermission("CONSULT_LIST", "_UPDATE", session)>
                        <button class="btn btn-primary" id="editAllConsultIsShow">批量显示</button>
                        <button class="btn btn-primary" id="editAllConsultNotShow">批量屏蔽</button>
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


<!-- 修改弹出框start -->
<div id="modal_edit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">咨询详情</h4>
            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>replyConsult</@ofbizUrl>">
                    <input type="hidden" id="consultId" name="consultId">
                    <div class="form-group">
                        <div class="col-sm-4">
                            <img src="" id="imgUrl" style="width: 150px;">
                        </div>
                        <div class="col-sm-8" id="productName">
                        </div>
                    </div>
                    <div class="cut-off-rule bg-gray"></div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">发表人</label>
                        <div class="col-sm-4">
                            <a href="" id="nickname"></a>
                        </div>
                        <label class="control-label col-sm-2">时间</label>
                        <div class="col-sm-4" id="createDate">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">咨询类型</label>
                        <div class="col-sm-10" id="consultType">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">内容</label>
                        <div class="col-sm-10" id="consultContent">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-sm-12">
                            <div class="radio">
                                <label class="col-sm-5"><input id="isShow1" name="isShow" type="radio" value="Y" class="radioItem">显示咨询</label>
                                <label class="col-sm-5"><input id="isShow2" name="isShow" type="radio" value="N" class="radioItem">不显示咨询</label>
                            </div>
                        </div>
                    </div>
                    <div class="cut-off-rule bg-gray"></div>
                    <div class="form-group" id="replyDiv1">
                        <div class="col-sm-6" id="replyDate">
                        </div>
                        <div class="col-sm-6">
                            <span id="replyName"></span>回复：
                        </div>
                    </div>
                    <div class="form-group" id="replyDiv2">
                        <div class="col-sm-10" id="replyContent">
                        </div>
                        <!-- 是否有修改权限-->
                        <#if security.hasEntityPermission("CONSULT_LIST", "_UPDATE", session)>
                        <label class="control-label col-sm-2" ><a href="javascript:editReply()">修改</a></label>
                        </#if>
                    </div>
                    <div class="form-group" data-type="required" data-mark="回复"  id="replyDiv3">
                        <label class="control-label col-sm-2" >回复：</label>
                        <div class="col-sm-8">
                            <textarea class="form-control dp-vd" rows="6" name="replyContent"  id="replyContent1"></textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                        <label class="control-label col-sm-2" ><a href="javascript:cancelReply()" id="cancelButton">取消</a></label>
                    </div>
                    <div class="form-group">
                        <div class="col-sm-12">
                            <div class="radio">
                                <label class="col-sm-5"><input id="isShowReply1" name="isShowReply" type="radio" value="Y" class="radioItem">显示回复</label>
                                <label class="col-sm-5"><input id="isShowReply2" name="isShowReply" type="radio" value="N" class="radioItem">不显示回复</label>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
             <!-- 是否有修改权限-->
            <#if security.hasEntityPermission("CONSULT_LIST", "_UPDATE", session)>
                <button id="save" type="button" class="btn btn-primary">保存</button>
            </#if>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 修改弹出框end -->

<!-- script区域start -->
<script>
    var replyContents = [];
    var data_consultId,data_isShow;
    var data_tbl;
    var ajaxUrl = "consultListJson";
    $(function () {
        data_tbl = $('#data_tbl').dataTable({
            ajaxUrl: ajaxUrl,
            columns:[
                {"title":"主键","code":"consultId","checked":true},
                {"title":"商品名称","code":"productName",
                    "handle":function(td,record){
                        td.html("<img src='" + record.imgUrl + "' class='cssImgSmall'>" + record.productName);
                    }
                },
                {"title":"咨询类型","code":"consultType",
                    "handle":function(td,record){
                        if(record.consultType == "PRODUCT"){
                            td.html("商品咨询");
                        }else if(record.consultType == "FACILITY"){
                            td.html("库存配送");
                        }else if(record.consultType == "PAY"){
                            td.html("支付");
                        }else if(record.consultType == "INVOICE"){
                            td.html("发票与保修");
                        }else if(record.consultType == "PROMO"){
                            td.html("促销及赠品");
                        }
                    }
                },
                {"title":"发表人","code":"nickname"},
                {"title":"内容","code":"consultContent"},
                {"title":"前台显示","code":"isShow",
                    "handle":function(td,record){
                        if(record.isShow == "Y"){
                            td.html("<button type='button' onclick='javascript:editConsultIsShow(\""+record.consultId+"\",\"Y\");' class='btn btn-primary'>是</button>");
                        }else{
                            td.html("<button type='button' onclick='javascript:editConsultIsShow(\""+record.consultId+"\",\"N\");' class='btn btn-default'>否</button>");
                        }
                    }
                },
                {"title":"发表时间","code":"createDate","sort":true},
//                {"title":"商家","code":"businessName"},
                {"title":"是否回复","code":"isReply",
                    "handle":function(td,record){
                        if(record.isReply == "Y"){
                            td.html("是");
                        }else{
                            td.html("否");
                        }
                    }
                },
                {"title":"操作","code":"option",
                    "handle":function(td,record){
                        var btns = "<div class='btn-group'>"+
                                "<button type='button' class='btn btn-danger btn-sm' onclick='editInit(\""+record.consultId+"\",\""+record.imgUrl+"\",\""+record.productName+"\",\""+record.consultType+"\",\""+record.createPartyId+"\",\""+record.nickname+"\",\""+record.consultContent+"\",\""+record.isShow+"\",\""+record.createDate+"\",\""+record.replyDate+"\",\""+record.isShowReply+"\",\""+record.replyName+"\",\""+record.index+"\")'>查看</button>"+
                                "</div>";
                        td.append(btns);
                        if(record.index == 0){
                            replyContents = [];
                        }
                        replyContents.push(record.replyContent);
                    }
                }
            ],
            listName: "recordsList",
            paginateEL: "paginateDiv",
            viewSizeEL: "view_size"
        });

        //查询按钮点击事件
        $('#QueryForm #searchBtn').on('click',function(){
            var nickname = $('#QueryForm #nickname1').val();
            var productName = $('#QueryForm #productName1').val();
            var consultType = $('#QueryForm #consultType1').val();
            var isReply = $('#QueryForm #isReply1').val();
//            var businessName = $('#QueryForm #businessName1').val();
            var consultContent = $('#QueryForm #consultContent1').val();

            ajaxUrl = changeURLArg(ajaxUrl,"nickname",nickname);
            ajaxUrl = changeURLArg(ajaxUrl,"productName",productName);
            ajaxUrl = changeURLArg(ajaxUrl,"consultType",consultType);
            ajaxUrl = changeURLArg(ajaxUrl,"isReply",isReply);
//            ajaxUrl = changeURLArg(ajaxUrl,"businessName",businessName);
            ajaxUrl = changeURLArg(ajaxUrl,"consultContent",consultContent);
            data_tbl.reload(ajaxUrl);
            return false;
        });

        //批量显示按钮点击事件
        $('#editAllConsultIsShow').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                data_consultId = "";
                //编辑id字符串
                checks.each(function(){
                    data_consultId += $(this).val() + ",";
                });
                data_isShow = 'Y';
                //设置提示弹出框内容
                $('#modal_confirm #modal_confirm_title').html("显示咨询");
                $('#modal_confirm #modal_confirm_body').html("确定显示这些信息？");
                $('#modal_confirm').modal('show');
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });

        //批量屏蔽按钮点击事件
        $('#editAllConsultNotShow').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                data_consultId = "";
                //编辑id字符串
                checks.each(function(){
                    data_consultId += $(this).val() + ",";
                });
                data_isShow = 'N';
                //设置提示弹出框内容
                $('#modal_confirm #modal_confirm_title').html("屏蔽咨询");
                $('#modal_confirm #modal_confirm_body').html("确定屏蔽这些信息？");
                $('#modal_confirm').modal('show');
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });

        //批量显示或隐藏按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用显示方法
            $.ajax({
                url: "editConsultIsShow",
                type: "GET",
                data: {consultIds : data_consultId,isShow : data_isShow},
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

        //修改弹出框保存按钮点击事件
        $('#modal_edit #save').click(function () {
            $('#EditForm').dpValidate({
                clear: true
            });

            $('#EditForm').submit();
        });
    });

    function cancelReply(){
        $("#modal_edit #replyDiv1").show();
        $("#modal_edit #replyDiv2").show();
        $("#modal_edit #replyDiv3").hide();
    }
    function editReply(){
        $("#modal_edit #replyDiv1").hide();
        $("#modal_edit #replyDiv2").hide();
        $("#modal_edit #replyDiv3").show();
        $("#modal_edit #cancelButton").show();
    }

    //点击查看按钮事件
    function editInit(consultId,imgUrl,productName,consultType,createPartyId,nickname,consultContent,isShow,createDate,replyDate,isShowReply,replyName,index) {
        if(replyDate){
            $("#modal_edit #replyDiv1").show();
            $("#modal_edit #replyDiv2").show();
            $("#modal_edit #replyDiv3").hide();
        }else{
            $("#modal_edit #replyDiv1").hide();
            $("#modal_edit #replyDiv2").hide();
            $("#modal_edit #replyDiv3").show();
            $("#modal_edit #cancelButton").hide();
        }

        //清空form
        clearForm($("#EditForm"));
        $("#modal_edit #consultId").val(consultId);
        $("#modal_edit #imgUrl").attr("src",imgUrl);
        $("#modal_edit #productName").text(productName);
        if(consultType == "PRODUCT"){
            $("#modal_edit #consultType").text("商品咨询");
        }else if(consultType == "FACILITY"){
            $("#modal_edit #consultType").text("库存配送");
        }else if(consultType == "PAY"){
            $("#modal_edit #consultType").text("支付");
        }else if(consultType == "INVOICE"){
            $("#modal_edit #consultType").text("发票与保修");
        }else if(consultType == "PROMO"){
            $("#modal_edit #consultType").text("促销及赠品");
        }
        $("#modal_edit #nickname").attr("href","/membermgr/control/memberDetail?partyId="+createPartyId);
        $("#modal_edit #nickname").text(nickname);
        $("#modal_edit #consultContent").text(consultContent);
        if(isShow == "N"){
            $("#modal_edit #isShow2").click();
        }else{
            $("#modal_edit #isShow1").click();
        }
        $("#modal_edit #createDate").text(createDate);
        $("#modal_edit #replyDate").text(replyDate);
        $("#modal_edit #replyContent").html(replyContents[index].replace(new RegExp(" ","gm"),"&nbsp;").replace(new RegExp("\n","gm"),"<br>"));
        $("#modal_edit #replyContent1").val(replyContents[index]);
        $("#modal_edit #replyName").text(replyName);
        if(isShowReply == "N"){
            $("#modal_edit #isShowReply2").click();
        }else{
            $("#modal_edit #isShowReply1").click();
        }
        $('#modal_edit').modal();
    }

    function editConsultIsShow(consultId,isShow){
        data_consultId = consultId;
        if(isShow == 'Y'){
            data_isShow = 'N';
            //设置提示弹出框内容
            $('#modal_confirm #modal_confirm_title').html("屏蔽咨询");
            $('#modal_confirm #modal_confirm_body').html("确定屏蔽这条信息？");
            $('#modal_confirm').modal('show');
        }else{
            data_isShow = 'Y';
            //设置提示弹出框内容
            $('#modal_confirm #modal_confirm_title').html("显示咨询");
            $('#modal_confirm #modal_confirm_body').html("确定显示这条信息？");
            $('#modal_confirm').modal('show');
        }
    }

</script>
