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

                <#--<div class="input-group m-b-10">
                    <span class="input-group-addon">反馈类型</span>
                    <select class="form-control" id="feedbackType1" name="feedbackType">
                        <option value="">全部</option>
                        <option value="PRODUCT">商品相关</option>
                        <option value="ACTIVITY">活动相关</option>
                        <option value="LOGISTIC">物流相关</option>
                        <option value="PRICE">价格相关</option>
                        <option value="AFTERSALE">售后相关</option>
                        <option value="SERVICE">服务相关</option>
                        <option value="OPERATE">操作意见</option>
                        <option value="OTHER">其他方面</option>
                    </select>
                </div>-->

                <div class="input-group m-b-10">
                    <span class="input-group-addon">是否回复</span>
                    <select class="form-control" id="isReply1" name="isReply">
                        <option value="">全部</option>
                        <option value="Y">是</option>
                        <option value="N">否</option>
                    </select>
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">内容</span>
                    <input type="text" class="form-control" id="feedbackContent1" name="feedbackContent" value=""/>
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

<!-- 修改弹出框start -->
<div id="modal_edit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">反馈详情</h4>
            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>replyFeedback</@ofbizUrl>">
                    <input type="hidden" id="feedbackId" name="feedbackId">
                    <div class="form-group">
                        <label class="control-label col-sm-2">发表人</label>
                        <div class="col-sm-4">
                            <a href="" id="nickname"></a>
                        </div>
                        <label class="control-label col-sm-2">时间</label>
                        <div class="col-sm-4" id="createDate">
                        </div>
                    </div>
                    <#--<div class="form-group">
                        <label class="control-label col-sm-2">反馈类型</label>
                        <div class="col-sm-10" id="feedbackType">
                        </div>
                    </div>-->
                    <div class="form-group">
                        <label class="control-label col-sm-2">手机号/邮箱</label>
                        <div class="col-sm-10" id="contactMethod">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">内容</label>
                        <div class="col-sm-10" id="feedbackContent">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">反馈图片</label>
                        <div class="col-sm-10" id="feekbackImg" >

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
                    <#if security.hasEntityPermission("FEEDBACK_LIST", "_UPDATE", session)>
                        <label class="control-label col-sm-2" ><a href="javascript:editReply()" id="editButton">修改</a></label>
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
            <#if security.hasEntityPermission("FEEDBACK_LIST", "_UPDATE", session)>
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
    var ajaxUrl = "feedbackListJson";
    $(function () {
        data_tbl = $('#data_tbl').dataTable({
            ajaxUrl: ajaxUrl,
            columns:[
                {"title":"主键","code":"feedbackId","checked":true},
                /*{"title":"反馈类型","code":"feedbackType",
                    "handle":function(td,record){
                        if(record.feedbackType == "PRODUCT"){
                            td.html("商品相关");
                        }else if(record.feedbackType == "ACTIVITY"){
                            td.html("活动相关");
                        }else if(record.feedbackType == "LOGISTIC"){
                            td.html("物流相关");
                        }else if(record.feedbackType == "PRICE"){
                            td.html("价格相关");
                        }else if(record.feedbackType == "AFTERSALE"){
                            td.html("售后相关");
                        }else if(record.feedbackType == "SERVICE"){
                            td.html("服务相关");
                        }else if(record.feedbackType == "OPERATE"){
                            td.html("操作意见");
                        }else if(record.feedbackType == "OTHER"){
                            td.html("其他方面");
                        }
                    }
                },*/
                {"title":"发表人","code":"nickname"},
                {"title":"内容","code":"feedbackContent"},
                {"title":"发表时间","code":"createDate","sort":true},
                {"title":"手机号/邮箱","code":"contactMethod"},
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
                                "<button type='button' class='btn btn-danger btn-sm' onclick='editInit(\""+record.feedbackId+"\",\""+record.feedbackType+"\",\""+record.createPartyId+"\",\""+record.nickname+"\",\""+record.contactMethod+"\",\""+record.feedbackContent+"\",\""+record.createDate+"\",\""+record.replyDate+"\",\""+record.isShowReply+"\",\""+record.replyName+"\",\""+record.index+"\",\""+record.contentIds+"\")'>查看</button>"+
                                "</div>";
                        if(record.index == 0){
                            replyContents = [];
                        }
                        td.append(btns);
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
//            var feedbackType = $('#QueryForm #feedbackType1').val();
            var isReply = $('#QueryForm #isReply1').val();
            var feedbackContent = $('#QueryForm #feedbackContent1').val();

            ajaxUrl = changeURLArg(ajaxUrl,"nickname",nickname);
//            ajaxUrl = changeURLArg(ajaxUrl,"feedbackType",feedbackType);
            ajaxUrl = changeURLArg(ajaxUrl,"isReply",isReply);
            ajaxUrl = changeURLArg(ajaxUrl,"feedbackContent",feedbackContent);
            data_tbl.reload(ajaxUrl);
            return false;
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
    function editInit(feedbackId,feedbackType,createPartyId,nickname,contactMethod,feedbackContent,createDate,replyDate,isShowReply,replyName,index,contentIds) {
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
        $("#modal_edit #feedbackId").val(feedbackId);
        /*if(feedbackType == "PRODUCT"){
            $("#modal_edit #feedbackType").text("商品相关");
        }else if(feedbackType == "ACTIVITY"){
            $("#modal_edit #feedbackType").text("活动相关");
        }else if(feedbackType == "LOGISTIC"){
            $("#modal_edit #feedbackType").text("物流相关");
        }else if(feedbackType == "PRICE"){
            $("#modal_edit #feedbackType").text("价格相关");
        }else if(feedbackType == "AFTERSALE"){
            $("#modal_edit #feedbackType").text("售后相关");
        }else if(feedbackType == "SERVICE"){
            $("#modal_edit #feedbackType").text("服务相关");
        }else if(feedbackType == "OPERATE"){
            $("#modal_edit #feedbackType").text("操作意见");
        }else if(feedbackType == "OTHER"){
            $("#modal_edit #feedbackType").text("其他方面");
        }*/
        $("#modal_edit #nickname").attr("href","/membermgr/control/memberDetail?partyId="+createPartyId);
        $("#modal_edit #nickname").text(nickname);
        $("#modal_edit #contactMethod").text(contactMethod);
        $("#modal_edit #feedbackContent").text(feedbackContent);
        $("#modal_edit #createDate").text(createDate);
        $("#modal_edit #replyDate").text(replyDate);
        $("#modal_edit #replyContent").html(replyContents[index].replace(new RegExp(" ","gm"),"&nbsp;").replace(new RegExp("\n","gm"),"<br>"));
        $("#modal_edit #replyContent1").val(replyContents[index]);
        $("#modal_edit #replyName").text(replyName);
        var images="";
        var contentIdList = contentIds.split(",");
        for(var i=0 ; i<contentIdList.length ; i++){
            images+='<img alt="" src="'+'/content/control/getImage?contentId='+contentIdList[i]+'" class="cssImgSmall" style="max-height: 150px;max-width: 150px;">'
        }
        $("#modal_edit #feekbackImg").text("");
        $("#modal_edit #feekbackImg").append(images);
        
        if(isShowReply == "N"){
            $("#modal_edit #isShowReply2").click();
        }else{
            $("#modal_edit #isShowReply1").click();
        }
        $('#modal_edit').modal();
    }

</script>
