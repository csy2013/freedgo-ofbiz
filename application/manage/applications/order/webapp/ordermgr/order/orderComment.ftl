<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "findcomment?lookupFlag=Y&"+ paramList +"&">
<#--${commonUrl}-->
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" method="post" class="form-inline clearfix" role="form" action="<@ofbizUrl>findcomment</@ofbizUrl>">
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单号</span>
                    <input type="text" name="orderId" class="form-control" value="${(paramMap.orderId)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">发表人&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <input type="text" name="publishUserId" class="form-control" value="${(paramMap.publishUserId)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" name="productName" class="form-control" value="${(paramMap.productName)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">评价级别</span>
                    <select name = "commentLevel" class="form-control">
                        <option value="" <#if (paramMap.commentLevel)?default("") == ""> selected="selected" </#if> >全部</option>
                        <option value="5" <#if (paramMap.commentLevel)?default("") == "5"> selected="selected" </#if>>5星</option>
                        <option value="4" <#if (paramMap.commentLevel)?default("") == "4"> selected="selected" </#if>>4星</option>
                        <option value="3" <#if (paramMap.commentLevel)?default("") == "3"> selected="selected" </#if>>3星</option>
                        <option value="2" <#if (paramMap.commentLevel)?default("") == "2"> selected="selected" </#if>>2星</option>
                        <option value="1" <#if (paramMap.commentLevel)?default("") == "1"> selected="selected" </#if>>1星</option>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">是否回复</span>
                    <select name = "isReply" class="form-control">
                        <option value="" <#if (paramMap.isReply)?default("") == ""> selected="selected" </#if> >全部</option>
                        <option value="1" <#if (paramMap.isReply)?default("") == "1"> selected="selected" </#if> >是</option>
                        <option value="0" <#if (paramMap.isReply)?default("") == "0"> selected="selected" </#if> >否</option>

                    </select>
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商家</span>
                    <input type="text" name="businessName" class="form-control" value="${(paramMap.businessName)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">内容</span>
                    <input type="text" name="comment" class="form-control" value="${(paramMap.comment)!''}">
                </div>
            </div>

            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">查询</button>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->
        <#if commentList?has_content>
            <div class="row m-b-12" style="margin-bottom:15px;">
                <div class="col-sm-6">
                    <div class="dp-tables_btn">
                    <#if security.hasEntityPermission("ORDER_COMMENTBATCH", "_VIEW", session)>
                        <button id="batchShow" class="btn btn-primary">
                            <i class="fa">批量显示</i>
                        </button>
                        <button id="batchHide" class="btn btn-primary">
                            <i class="fa">批量屏蔽</i>
                        </button>
                    </#if>
                    </div>

                </div>
            </div>
        </#if>
        <!-- 表格区域start -->
    <#if commentList?has_content>
        <!-- 列表当前分页条数start -->
        <div class="row m-b-12">
            <div class="col-sm-6">
            </div>
            <div class="col-sm-6">
                <div class="dp-tables_length">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                            <option value="10" <#if viewSize==10>selected</#if>>10</option>
                            <option value="20" <#if viewSize==20>selected</#if>>20</option>
                            <option value="30" <#if viewSize==30>selected</#if>>30</option>
                            <option value="40" <#if viewSize==40>selected</#if>>40</option>
                        </select>
                        条
                    </label>
                </div>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->

        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr class="js-sort-list">
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>商品名称</th>
                        <th>订单号</th>
                        <th>发表人</th>
                        <th>内容</th>
                        <th>前台显示</th>
                        <th>发表时间</th>
                        <th>评价级别</th>
                        <th>是否回复</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list commentList as comment>
                        <tr>
                            <td><input value="${comment.productReviewId}" class="js-checkchild" type="checkbox"></td>
                            <td><#--${(comment.productName)!''}-->
                                <#if StringUtil.wrapString((comment.productName))?length gt 15>
                                    ${(StringUtil.wrapString((comment.productName))[0..15])!''}...
                                <#else>
                                    ${(comment.productName)!''}
                                </#if>

                            </td>
                            <td>${(comment.orderId)!''}</td>
                            <td>${(comment.userLoginId)!''}</td>
                            <td>
                                <#if StringUtil.wrapString((comment.productReview))?length gt 10>
                                    ${(StringUtil.wrapString((comment.productReview))[0..10])!''}...
                                <#else>
                                    ${(comment.productReview)!''}
                                </#if>
                            </td>
                            <td>

                                <#if (comment.isShow)?default("0") == "0">
                                    <button type='button' class='btn btn-primary btn-sm btn-sm' value="0" productReviewId="${comment.productReviewId}" id="${comment.productReviewId}" onclick="changeShow(this)">否</button>
                                <#else>
                                    <button type='button' class='btn btn-primary btn-sm btn-sm' value="1" productReviewId="${comment.productReviewId}"  id="${comment.productReviewId}" onclick="changeShow(this)">是</button>
                                </#if>
                            </td>
                            <td>${(comment.postedDateTime)?string('yyyy-MM-dd HH:mm:ss')}</td>
                            <td>${(comment.productRating)!''}星</td>
                            <td>${((comment.isReply)?default("0") == "0")?string('否','是')}</td>
                            <td>
                                <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateComment("${comment.productReviewId}","${comment.userLoginId}","${(comment.postedDateTime)?string('yyyy-MM-dd HH:mm:ss')}","${(comment.orderId)!''}","${StringUtil.wrapString((comment.productReview)!'')}","${StringUtil.wrapString((comment.replyComment)!'')}","${(comment.isShow)!'0'}","${(comment.seeType)!'0'}","${(comment.productRating)!''}星","${(comment.productName)!''}")'>查看</button>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "findcomment?lookupFlag=Y&"+ paramList + "&"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(commentListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", commentListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=commentListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
        <!-- 分页条end -->
    <#else>
        <#if lookupFlag == "Y">
            <div id="findPartyResults_2" class="col-sm-12">
                <h3>没有数据</h3>
            </div>
        </#if>
    </#if>
    <#if commentList?has_content>
        <#list commentList as comment>
            <div class="modal fade" id="myModal${comment.productReviewId}" tabindex="-1" role="dialog"
                 aria-labelledby="myModalLabel" aria-hidden="true" >
                <form  id="form${comment.productReviewId}" class="form-horizontal" action="<@ofbizUrl>updateOrderComment</@ofbizUrl>" method="post">
                    <input type="hidden" name="orderId" value="${(paramMap.orderId)!''}"/>
                    <input type="hidden" name="publishUserId" value="${(paramMap.userLoginId)!''}"/>
                    <input type="hidden" name="productName" value="${(paramMap.productName)!''}"/>
                    <input type="hidden" name="commentLevel" value="${(paramMap.commentLevel)!''}"/>
                    <input type="hidden" name="isReply" value="${(paramMap.isReply)!''}"/>
                    <input type="hidden" name="businessName" value="${(paramMap.businessName)!''}"/>
                    <input type="hidden" name="lookupFlag" value="Y">
                <#--<input type="hidden" name="comment" value="${(paramMap.comment)!''}"/>-->
                    <input hidden="hidden" name="commentId" id="commentId" value="${comment.productReviewId}"/>
                    <div class="modal-dialog" style="width: 900px">
                        <div class="modal-content">
                            <div class="modal-header">

                                <h4 class="modal-title" id="myModalLabel">
                                    <label id = "productName">${(comment.productName)!''}</label>
                                </h4>
                            </div>
                            <div class="modal-body">
                                <div class="pay_meg">
                                    <p>
                                        <label>发表人:</label><span id="publishUserId">${(comment.userLoginId)!''}</span><label></label>
                                        <label>时间:</label><span id="publishTime">${(comment.postedDateTime)?string('yyyy-MM-dd HH:mm:ss')}</span><label></label>
                                        <label>订单号:</label><span id="orderId">${(comment.orderId)!''}</span>
                                    </p>
                                    <p>
                                        <label>评价等级:</label><span id="commentLevel">${(comment.productRating)!''}星</span><label></label>

                                    </p>
                                    <div class="row" style="margin-bottom: 5px">
                                        <div class="form-group">
                                            <label for="title" class="control-label col-sm-1">内容:</label>
                                            <div class="col-sm-6">
                                                <textarea class="form-control w-p80" id="comment"<#-- name="comment"--> readonly>${StringUtil.wrapString((comment.productReview)!'')}</textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="row" style="margin-bottom: 10px">
                                        <div class="form-group">
                                            <label for="title" class="control-label col-sm-1">回复:</label>
                                            <div class="col-sm-6">
                                                <textarea class="form-control w-p80" id="replyComment" name="replyComment">${StringUtil.wrapString((comment.replyComment)!'')}</textarea>
                                            </div>
                                        </div>
                                    </div>
                                    <p>
                                        <input type="radio" name="isShow" value="1"
                                               <#if (comment.isShow)?default('0') == "1">checked = "checked"</#if>
                                                >&nbsp;&nbsp;显示评价&nbsp;&nbsp;&nbsp;<input type="radio" name="isShow" <#if (comment.isShow)?default('0') == "0">checked = "checked"</#if> value="0">&nbsp;&nbsp;不显示评价
                                    </p>
                                    <p>
                                        <#--${ (comment.seeType)?default('1') }-->
                                        <input type="radio" name="seeType" <#if (comment.seeType)?default('1') == '1'>checked = "checked"</#if> value="1">&nbsp;&nbsp;所有人可见&nbsp;&nbsp;&nbsp;
                                        <input type="radio" name="seeType" <#if (comment.seeType)?default('1') == '0'>checked = "checked"</#if>  value="0">&nbsp;&nbsp;评论者可见
                                    </p>
                                </div>
                                <#assign images = Static["org.ofbiz.product.product.ProductWorker"].getProductReviewContent(delegator,comment.productReviewId) />
                                <#if images?has_content>
                                    <div class="refund-msg">
                                        <div class="img-box">
                                            <ul class="banner" id="images">
                                                <#list images as img>
                                                    <li><img src="${(img.objectInfo)!''}"></li>
                                                </#list>
                                            </ul>

                                            <div class="number">
                                                <a href="javascript:;" class="glyphicon glyphicon-chevron-left icon1"></a>
                                                <a href="javaScript:;" class="glyphicon glyphicon-chevron-right icon2"></a>
                                            </div>
                                        </div>
                                    </div>
                                </#if>


                            </div>


                            <div class="modal-footer xl_footer">
                                <button type="button" class="btn btn-default"
                                        data-dismiss="modal">取消
                                </button>
                                <#if security.hasEntityPermission("ORDER_COMMENTSAVE", "_VIEW", session)>
                                    <button id="save${comment.productReviewId}" commentId="${comment.productReviewId}" type="button" class="btn btn-primary xl_confirm">
                                        确认
                                    </button>
                                </#if>
                            </div>
                        </div><!-- /.modal-content -->
                    </div><!-- /.modal -->
                </form>
            </div>
        </#list>

    </#if>



 <script>
     <#if commentList?has_content>
         <#list commentList as comment>
         $("#save"+${comment.productReviewId}).click(function(){
             $('#form'+ ${comment.productReviewId}).submit();
         });
         </#list>
     </#if>
            $(function(){
                $('.form_datetime').datetimepicker({
                    language:  'zh-CN',
                    todayBtn:  1,
                    autoclose: 1,
                    todayHighlight: 1,
                    startView: 2,
                    forceParse: 0,
                    showMeridian: 1
                });

                $('.form_date').datetimepicker({
                    language:  'zh-CN',
                    todayBtn:  1,
                    autoclose: 1,
                    todayHighlight: 1,
                    startView: 2,
                    minView: 2,
                    forceParse: 0
                });

                $('.form_time').datetimepicker({
                    language:  'zh-CN',
                    autoclose: 1,
                    startView: 1,
                    forceParse: 0
                });

                $("#btn_add").click(function(){
                    window.location.href = "<@ofbizUrl>addOrder</@ofbizUrl>";
                });

                $("#batchShow").click(function(){
                    if (getSelectedIds() != ""){
                        window.location.href = "<@ofbizUrl>batchShowOrHidden?isShow=1&lookupFlag=Y&ids=" + getSelectedIds() + "</@ofbizUrl>";
                    }else{
                        alert("请勾选一条记录");
                    }
                });

                $("#batchHide").click(function(){
                    if (getSelectedIds() != ""){
                        window.location.href = "<@ofbizUrl>batchShowOrHidden?isShow=0&lookupFlag=Y&ids=" + getSelectedIds() + "</@ofbizUrl>";
                    }else{
                        alert("请勾选一条记录");
                    }
                });

                $('#myModal').modal({
                    show:false
                });

                function showNumber(num){
                    $(".banner>li").eq(num).fadeIn().siblings().fadeOut();
                };
                $(".img-box").hover(function(){
                    $(".number").show();
                },function(){
                    $(".number").hide();
                });
                $(".banner>li:first-child").fadeIn().siblings().fadeOut();
                var a='<ul class="icon" style="display: none;">';
                for(var i=1;i<=$(".banner>li").length;i++){
                    a+='<li>'+i+'</li>'
                }
                a+='</ul>';
                $(".img-box").append(a);
                $(".icon li").eq(0).addClass("on");

                $(".icon li").on("mouseenter",function(){
                    $(this).addClass("on").siblings().removeClass("on");
                    var b=$(".icon li").index($(this));
                    showNumber(b);
                });

                $(".number .icon1").on("click",function(){
                    if($(".on").prev().length==0){
                        $(".icon li").last().triggerHandler("mouseenter");
                    }else{
                        $(".on").prev().triggerHandler("mouseenter");
                    }
                });
                $(".number .icon2").on("click",function(){
                    if($(".on").next().length==0){
                        $(".icon li").eq(0).triggerHandler("mouseenter");
                    }else{
                        $(".on").next().triggerHandler("mouseenter");
                    }
                });
            });

            function showDeliveryForm(orderId){
                $("#deliveryForm_update #orderId").val(orderId);
                $('#deliveryForm_update').modal();
            }

            function getSelectedIds(){
                var ids = ""
                var checks = $('.js-checkchild:checked');
                //判断是否选中记录
                if (checks.size() > 0) {
                    //编辑id字符串
                    checks.each(function () {
                        ids += $(this).val() + ",";
                    });
                    ids = ids.substring(0,ids.length  -1);
                }
                return ids;
            }


            //修改弹出框保存按钮点击事件
            $('#deliveryForm_update #save').click(function(){
                $('#deliveryForm').dpValidate({
                    clear: true
                });
                $('#deliveryForm').submit();
            });

            $('#deliveryForm').dpValidate({
                validate: true,
                callback: function(){
                    document.getElementById('deliveryForm').submit();
                }
            });

            //弹出窗关闭事件
            $('#deliveryForm_update').on('hide.bs.modal', function () {
                $('#deliveryForm').dpValidate({
                    clear: true
                });
            });

            function updateMoney(orderId){
                if (confirm("订单金额不能随意修改，你确定修改吗？")){
                    $("#moneyForm_update #orderId").val(orderId);
                    $('#moneyForm_update').modal();
                }
            }

            $('#moneyForm_update #save').click(function(){
                $('#moneyForm').dpValidate({
                    clear: true
                });
                $('#moneyForm').submit();
            });

            $('#moneyForm').dpValidate({
                validate: true,
                callback: function(){
                    document.getElementById('moneyForm').submit();
                }
            });

            //弹出窗关闭事件
            $('#moneyForm_update').on('hide.bs.modal', function () {
                $('#moneyForm').dpValidate({
                    clear: true
                });
            });

            $('#showOperateLog').on('hide.bs.modal', function () {
                $("#logTable tbody").html("");
            });

            function showOperateLog(orderId){
                $.post("orderLog",{orderId : orderId},function(data){
                    var logList = data.logList;
                    if (logList != null && logList != "" && logList != undefined){
                        var h= "";
                        for (var i = 0; i < logList.length; i++){
                            var l = logList[i];
                            h += "<tr><td>" + l.operateType + "</td><td>" + l.operator + "</td><td>" + l.operateTime + "</td><td></td></tr>";
                        }
                        $("#logTable tbody").append(h);
                        $('#showOperateLog').modal();
                    }
                });
            }

            function viewOrderInfo(orderId ){
                $('#myModal' + orderId).modal();
            }

            function cancelOrder(orderId){
                if(confirm("订单不能随意中断，你确定修改吗？")){
                    $("#cancelForm_update #orderId").val(orderId);
                    $('#cancelForm_update').modal();
                }
            }

            $('#cancelForm_update').on('hide.bs.modal', function () {
                $('#cancelForm').dpValidate({
                    clear: true
                });
            });

            $('#cancelForm_update #save').click(function(){
                $('#cancelForm').dpValidate({
                    clear: true
                });
                $('#cancelForm').submit();
            });

            $('#cancelForm').dpValidate({
                validate: true,
                callback: function(){
                    document.getElementById('cancelForm').submit();
                }
            });

            function operateComment(commentId,publishUserId,publishTime,orderId,comment,replyComment,isShow,seeType,level,productName){
                /*$("#myModal #commentId").val(commentId);
                $("#myModal #publishUserId").html(publishUserId);
                $("#myModal #productName").html(productName);
                $("#myModal #publishTime").html(publishTime);
                $("#myModal #orderId").html(orderId);
                $("#myModal #comment").val(comment);
                $("#myModal #replyComment").val(replyComment);
                $("#myModal #commentLevel").html(level);
                $("#myModal" +commentId+ " input[name='seeType']").each(function(){
                    if ($(this).val() == seeType){
                        $(this).attr("checked","checked");
                    }
                });*/
                var isShoww = $("#"+  commentId ).attr("value");
                $("#myModal" +commentId+ " input[name='isShow']").each(function(){
                    if ($(this).val() == isShoww){
                        $(this).attr("checked","checked");
                    }
                });

                $("#myModal" + commentId).modal("show");
               /* $.post("getProductReviewImages",{productReviewId:commentId},function(data){
                    var imagesStr = '';
                    var images = data.images;
                    for (var i = 0;i< images.length;i++){
                        var imgUrl = images[i].objectInfo;
                        imagesStr += '<li><img src="' + imgUrl *//*+ '?' + Math.random()*//* + '"></li>';
                    }
                    $("#myModal #images").html(imagesStr);
                    $("#myModal").modal("show");
                });*/
            }

     function changeShow(obj){
         var obj = $(obj);
         var newShow = "";
         var productReviewId =  obj.attr("productReviewId");
         if (obj.attr("value") == "0"){
             obj.html("是");
             newShow = "1";
             obj.attr("value","1");
         }else{
             obj.html("否");
             newShow = "0";
             obj.attr("value","0");
         }
         $.post("changeIsShow",{isShow : newShow,productReviewId:productReviewId},function(data){});
     }
</script>

