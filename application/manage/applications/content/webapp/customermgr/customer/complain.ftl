<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "complain?lookupFlag=Y&"+ paramList +"&">
<#--${commonUrl}-->
<style>
    .express_area {
    	width : 200px;
        position: relative;
        height: 25px;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .express_area p {
    	width : 200px;
        position: absolute;
        left: 0;
        top: 0;
        display: block;
        padding: 0 10px;
        line-height: 25px;
        word-wrap: break-word;
    }

    .express_area:hover {
        overflow: visible;
    }

    .express_area:hover p {
        background: #DFEDF7;
        z-index: 99;
    }
</style>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" method="post" class="form-inline clearfix" role="form"
              action="<@ofbizUrl>complain</@ofbizUrl>">
            <input type="hidden" name="lookupFlag" value="Y">
            <input type="hidden" name="complainStatus" id="complainStatus" value="${(paramMap.complainStatus)!''}">

            <div class="form-group w-p100">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">申诉单号</span>
                    <input type="text" name="complainId" class="form-control" value="${(paramMap.complainId)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单号</span>
                    <input type="text" name="orderId" class="form-control" value="${(paramMap.orderId)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">用户</span>
                    <input type="text" name="complainer" class="form-control" value="${(paramMap.complainer)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">手机号</span>
                    <input type="text" name="partyPhone" class="form-control" value="${(paramMap.partyPhone)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">处理状态</span>
                    <select  class="form-control" onchange="changeStatus(this)">
                        <option value="" <#if (paramMap.complainStatus)?default("") == "">    selected="selected" </#if> >全部</option>
                        <option value="COMPLAIN_HASACCEPTED" <#if (paramMap.complainStatus)?default("") == "COMPLAIN_HASACCEPTED">  selected="selected" </#if>>已受理 </option>
                        <option value="COMPLAIN_PROCESSING" <#if (paramMap.complainStatus)?default("") == "COMPLAIN_PROCESSING"> selected="selected" </#if>>处理中</option>
                        <option value="COMPLAIN_COMPLETED" <#if (paramMap.complainStatus)?default("") == "COMPLAIN_COMPLETED"> selected="selected" </#if>>已完成</option>
                    </select>
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" name="productName" class="form-control" value="${(paramMap.productName)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">申诉说明</span>
                    <input type="text" name="remarks" class="form-control" value="${(paramMap.remarks)!''}">
                </div>
                <div class="input-group pull-right">
                    <button class="btn btn-success btn-flat">搜索</button>
                </div>
            </div>

        </form>
        <!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <div class="row m-b-12" style="margin-bottom:15px;">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <#if security.hasEntityPermission("COMPLAIN", "_CREATE", session)>
                    <button id="btn_add" class="btn btn-primary"  data-toggle="modal" data-target="#exampleModal">
                        <i class="fa fa-plus">添加</i>
                    </button>
                    </#if>
                </div>
            </div>
        </div>
    <#if lookupFlag == "Y">
        <ul class="nav nav-tabs">
            <li role="presentation" <#if complainStatus?default("") == "">class="active"</#if>><a href="<@ofbizUrl>complain?lookupFlag=Y</@ofbizUrl>">全部</a></li>
            <li role="presentation" <#if complainStatus?default("") == "COMPLAIN_HASACCEPTED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('COMPLAIN_HASACCEPTED')">已受理</a></li>
            <li role="presentation" <#if complainStatus?default("") == "COMPLAIN_PROCESSING">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('COMPLAIN_PROCESSING')">处理中</a></li>
            <li role="presentation" <#if complainStatus?default("") == "COMPLAIN_COMPLETED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('COMPLAIN_COMPLETED')">已完成</a></li>
        </ul>
    </#if>
        <!-- 表格区域start -->
    <#if complainList?has_content>
    <#--${order.getRelatedOneCache("StatusItem").get("description",locale)}-->
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
            </div>
            <!-- 列表当前分页条数end -->
        </div>
        <!-- 工具栏end -->

        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr class="js-sort-list">
                        <th>申诉单号</th>
                        <th>订单号</th>
                        <th>商品名称</th>
                        <th>申诉人信息</th>
                        <th width="200px">申诉说明</th>
                        <th>提交时间</th>
                        <th>处理状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list complainList as c>
                            <#assign products = Static["org.ofbiz.order.order.OrderReadHelper"].getComplainProducts(delegator,c.complainId) />
                            <#assign info = (Static["org.ofbiz.order.order.OrderReadHelper"].getOrderPurchaseInfo(delegator,c.orderId))!'' />

                        <tr>
                            <td>
                                ${c.complainId}
                            </td>
                            <td>
                                ${c.orderId}
                            </td>
                            <td>
                                <#list products as p>
                                    <#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : p.productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>
                                    <#if curProductAdditionalImage1?has_content>
                                        <#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(p, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />
                                    <#else>
                                        <#assign productAdditionalImage1 =""/>
                                    </#if>
                                    <span class="col-sm-6">
		                                  <img height="100" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>"  class="cssImgSmall" title="${(p.productName)!''}" />
                                    <#--<img height="100" src="/images/party/review/1456732224392.jpg"  class="cssImgSmall" title="${(p.productName)!''}" />-->
		                             </span>
                                </#list>
                            </td>
                            <td>
                                <#assign contactMechId = delegator.findByAnd("OrderContactMech",{"orderId" : c.orderId})[0].get("contactMechId") >
                                <#assign postalAddress = delegator.findByAnd("PostalAddress",{"contactMechId" : contactMechId}) >
                                用户：<#if postalAddress?has_content>${postalAddress[0].toName?default("")}</#if><br/>
                                手机：<#if postalAddress?has_content>${postalAddress[0].mobilePhone?default("")}</#if>
                            </td>
                            <td>
                            	<#assign remarks = Static["org.ofbiz.base.util.UtilHttp"].decodeURL(c.remarks?default("")) >
                                 <div class="express_area">
                                 	<nobr>${remarks}</nobr>
                                 	<p style='display:none;'>${remarks}</p>
                                 </div>
                            </td>
                            <td>${(c.createdStamp)?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${c.getRelatedOneCache("StatusItem").get("description",locale)}</td>
                            <td>
                                <#if c.statusId == "COMPLAIN_HASACCEPTED"&&security.hasEntityPermission("COMPLAIN", "_HANDLE", session)>
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateComplain("${c.complainId}","COMPLAIN_HASACCEPTED")'>处理</button>
                                <#elseif c.statusId == "COMPLAIN_PROCESSING"&&security.hasEntityPermission("COMPLAIN", "_VIEW", session)>
                                    <button type='button' class='btn btn-danger btn-sm btn-sm' onclick='operateComplain("${c.complainId}","COMPLAIN_PROCESSING")'>查看 </button>
                                <#elseif c.statusId == "COMPLAIN_COMPLETED"&&security.hasEntityPermission("COMPLAIN", "_VIEW", session)>
                                    <button type='button' class='btn btn-danger btn-sm btn-sm' onclick='operateComplain("${c.complainId}","COMPLAIN_COMPLETED")'>查看</button>
                                </#if>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
        <!-- 表格区域end -->
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "complain?lookupFlag=Y&"+ paramList +"&">
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(complainListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", complainListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=complainListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
        <div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel">添加投诉单</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="addFirstForm" >
                            <div class="row">
                                <div class="form-group" data-type="required" data-mark="订单号">
                                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>请输入订单号:</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control dp-vd w-p50" id="orderId">
                                        <p class="dp-error-msg"></p>
                                    </div>
                                </div>
                            </div>

                            <div class="modal-footer">
                                <button type="button" class="btn btn-primary btn_save1">${uiLabelMap.Save}</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>


        <div class="modal fade" id="exampleModal2" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" style="width:800px;">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel"></h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="addFirstForm2" method="post" >
                            <input type = "hidden" name = "orderId" id="orderId" />
                            <div class="row">
                                <div class="form-group" data-type="required" data-mark="申诉问题">
                                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>申诉问题:</label>
                                    <div class="col-sm-9">
                                        <#assign questions = delegator.findByAnd("Enumeration",{"enumTypeId" : "APPEAL_PROBLEM","enumCode" : "N"} ,["sequenceId"]) >
                                        <select id="complainQuestion" name="complainQuestion" class="form-control dp-vd w-p50">
                                            <#list questions as q>
                                                <option value="${q.enumId}" >${q.description}</option>
                                            </#list>
                                        </select>
                                        <p class="dp-error-msg"></p>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="form-group" data-type="required" data-mark="申诉说明">
                                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>申诉说明:</label>
                                    <div class="col-sm-9">
                                        <input type="text" class="form-control dp-vd w-p90" id="remarks" name="remarks">
                                        <p class="dp-error-msg"></p>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-sm-12">
                                    <table class="table table-bordered table-hover js-checkparent"">
                                        <thead>
                                        <tr class="js-sort-list">
                                            <th><input class="js-allcheck" type="checkbox"></th>
                                            <th>商品</th>
                                            <th>用户自定义名称</th>
                                            <th>商品配置</th>
                                            <th>数量</th>
                                        </tr>
                                        </thead>
                                        <tbody>

                                        </tbody>
                                    </table>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-primary btn_save2">${uiLabelMap.Save}</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
<script>
$(function(){
	//文字溢出
	$(document).on("mouseover",'.express_area',function(){
		$(this).find("nobr").hide();
		$(this).find("p").show();
	});
	
	$(document).on("mouseout",'.express_area',function(){
		$(this).find("p").hide();
		$(this).find("nobr").show();
	});

    //添加一级分类弹框关闭事件
    $('#exampleModal').on('hide.bs.modal', function () {
        $('#addFirstForm').dpValidate({
            clear: true
        });
    })
    //添加一级分类提交按钮点击事件
    $('.btn_save1').click(function(){
        $('#addFirstForm').dpValidate({
            clear: true
        });
        $('#addFirstForm').submit();
    });
    //表单校验
    $('#addFirstForm').dpValidate({
        validate: true,
        console: true,
        callback: function(){
            $("#exampleModal2 .table tbody").html("");
            var orderId = $("#addFirstForm #orderId").val();
            $.post("checkValidOrder",{"orderId" : orderId},function(data){
                if (data.flag == false){
                    $.otherLayer({
                        title : '提示信息',
                        content : '请输入有效的订单'
                    });
                }else{
                    $.post("getOrderProduct",{"orderId" : orderId},function(data){
                        var b = '';
                        var list = data.products;
                        for(var i=0; i < list.length; i++){
                            var pp = list[i];
                            b+='<tr id="'+ pp.orderItemSeqId +'">'
                            +'<td><input class="js-checkchild" type="checkbox" orderItemSeqId="'+pp.orderItemSeqId +'" value="'+ pp.productId +'"></td>'
                            +'<td>'+ pp.productName +'</td>'
                            +'<td>' +pp.itemDescription + '</td>'
                            +'<td>' +pp.productSettingName + '</td>' //onkeyup="clearNoNum(this)" pp.quantity
                            +'<td><input type="text" maxQuantity="'+ pp.quantity +'" name = "' + pp.productId+'-'+pp.orderItemSeqId  + '" class="product" size="1" orderItemSeqId="' + pp.orderItemSeqId + '" value = "' + pp.quantity + '" onkeyup="clearNoNum(this)" /></td>'
                            +'</tr>';
                        }
                        $("#exampleModal2 #orderId").val(orderId);
                        $("#exampleModal2 .table tbody").append(b);
                        $('#exampleModal2').modal('toggle');
                    });
                }
            });
        }
    });

    $('#exampleModal2').on('hide.bs.modal', function () {
        $('#addFirstForm2').dpValidate({
            clear: true
        });
    })
    //添加一级分类提交按钮点击事件
    $('.btn_save2').click(function(){
        $('#addFirstForm2').dpValidate({
            clear: true
        });
        $('#addFirstForm2').submit();
    });
    //表单校验
    $('#addFirstForm2').dpValidate({
        validate: true,
        console: true,
        callback: function(){
            var params = getSelect();
            var complainQuestion = $("#exampleModal2 #complainQuestion").val();
            var remarks =  $("#exampleModal2 #remarks").val();
            var orderId =  $("#exampleModal2 #orderId").val();
            var statusId = "COMPLAIN_HASACCEPTED";
            if (params != ""){
                var checks = $('#exampleModal2 .js-checkchild:checked');
                var flag = true;
                var msg = "";
                //判断是否选中记录
                if (checks.size() > 0) {
                    //编辑id字符串
                    checks.each(function () {
                        var orderItemSeqId =  $(this).attr("orderItemSeqId");
                        var maxQuantity = $("input[name=" + $(this).val() +"-" +orderItemSeqId + "]").attr("maxQuantity");
                        //数量
                        var value = $("input[name=" + $(this).val() +"-" +orderItemSeqId + "]").val();

                        if (value == ""){
                            msg = "数量不能为空";
                            flag = false;
                            return;
                        }else  if (parseFloat(value) === 0){
                            msg = "数量不能为0";
                            flag = false;
                            return;
                        }else if (parseFloat(value) > parseFloat(maxQuantity) ){
                            msg = "数量不能大于原订单商品数量";
                            flag = false;
                            return;
                        }
                    });
                }
                if (flag){
                    $(".btn_save2").prop("disabled",true);
                    $.post("createComplain",{params : params,complainQuestion : complainQuestion,remarks : remarks,orderId : orderId.trim(),statusId: statusId},function(data){
                        window.location.href = "<@ofbizUrl>operateComplain?statusId=" + statusId + "&complainId=" + data.complainId + "</@ofbizUrl>";
                    });
                }else{
                    $.otherLayer({
                        title : '提示信息',
                        content : msg
                    });
                }

            }else{
                $.tipLayer("请勾选一条数据");
            }

           /* if (params === ""){0
                alert("请勾选一条记录");
            }else{
                $.post("createComplain",{params : params,complainQuestion : complainQuestion,remarks : remarks,orderId : orderId,statusId: statusId},function(data){
                    window.location.href = "<@ofbizUrl>operateComplain?statusId=" + statusId + "&complainId=" + data.complainId + "</@ofbizUrl>";
                });
            }*/
        }
    });
})

function operateComplain(complainId, statusId) {
   window.location.href = "<@ofbizUrl>operateComplain?statusId=" + statusId + "&complainId=" + complainId + "</@ofbizUrl>";
}

function changeTab(complainStatus) {
   $("#complainStatus").val(complainStatus);
   $("#QueryForm").submit();
}

function changeStatus(obj){
    $("#complainStatus").val($(obj).val());
}

function clearNoNum(obj){
    obj.value = obj.value.replace(/\D+/g,'')
}
function getSelect(){
    var ids = ""
    var checks = $('.js-checkchild:checked');
    //判断是否选中记录
    if (checks.size() > 0) {
        //编辑id字符串
        checks.each(function () {
            var orderItemSeqId =  $(this).attr("orderItemSeqId");
            var value = $("input[name=" + $(this).val() +"-" +orderItemSeqId + "]").val();
            if (value == ""){
             $.tipLayer("数量不能为空");
                return;
            }else{
                ids += $(this).val() + ":" + orderItemSeqId + "&" + value + "," ;
            }
        });
        ids = ids.substring(0,ids.length  -1);
    }
    return ids;
}
</script>





