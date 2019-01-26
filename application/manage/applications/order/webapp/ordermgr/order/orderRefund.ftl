<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "findrefund?lookupFlag=Y&"+ paramList +"&">
<#--${commonUrl}-->
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" method="post" class="form-inline clearfix" role="form" action="<@ofbizUrl>findrefund</@ofbizUrl>">
        	<input type="hidden" name="searchfunction" value="findReturns"/>
            <input type="hidden" name="lookupFlag" value="Y">
            <input type="hidden" name="returnType" value="1">
            <input type="hidden" name="returnStatus" id ="returnStatus" value="${(paramMap.returnStatus)!''}">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">退单号&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <input type="text" name="returnId" class="form-control" value="${(paramMap.returnId)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单号&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <input type="text" name="orderId" class="form-control" value="${(paramMap.orderId)!''}">
                </div>
                <#--<div class="input-group m-b-10">
                    <span class="input-group-addon">收货人&nbsp;&nbsp;&nbsp;</span>
                    <input type="text" name="receivePartyName" class="form-control" value="${(paramMap.receivePartyName)!''}">
                </div>-->
                <#--<div class="input-group m-b-10">-->
                    <#--<span class="input-group-addon">商家</span>-->
                    <#--<input type="text" name="businessName" class="form-control" value="${(paramMap.businessName)!''}">-->
                <#--</div>-->
                <div class="input-group m-b-10">
                    <span class="input-group-addon">手机</span>
                    <input type="text" name="partyPhone" class="form-control" value="${(paramMap.partyPhone)!''}">
                </div>
            </div>

            <div class="form-group" data-type="linkLt" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间" data-relation="">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" name="productName" class="form-control" value="${(paramMap.productName)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">用户</span>
                    <input type="text" name="billToName" class="form-control" value="${(paramMap.billToName)!''}">
                </div>
                <div class="input-group m-b-10 input-group date form_date"  data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" data-link-field="startDate">
                    <span class="input-group-addon">开始时间</span>
                    <input class="form-control" size="16" type="text" readonly value="${(paramMap.startDate)!''}">
                    <input id="startDate" type="hidden" name="startDate" value="${(paramMap.startDate)!''}">
                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                </div>
                <div class="input-group m-b-10 input-group date form_date"  data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" data-link-field="endDate">
                    <span class="input-group-addon">结束时间</span>
                    <input class="form-control" size="16" type="text" readonly value="${(paramMap.endDate)!''}">
                    <input id="endDate" type="hidden" name="endDate" value="${(paramMap.endDate)!''}">
                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                </div>
            </div>

            <div class="form-group">
                <div class="input-group m-b-10" style="display:none">
                    <span class="input-group-addon">支付方式</span>
                    <select name = "paymentMethodType" class="form-control">
                        <option value="" <#if (paramMap.paymentMethodType)?default("") == ""> selected="selected" </#if> >全部</option>
                        <option value="EXT_ALIPAY" <#if (paramMap.paymentMethodType)?default("") == "EXT_ALIPAY"> selected="selected" </#if>>支付宝</option>
                        <option value="EXT_WEIXIN" <#if (paramMap.paymentMethodType)?default("") == "EXT_WEIXIN"> selected="selected" </#if>>微信</option>
                        <option value="EXT_UNIONPAY" <#if (paramMap.paymentMethodType)?default("") == "EXT_UNIONPAY"> selected="selected" </#if>>银联</option>
                        <option value="EXT_COD" <#if (paramMap.paymentMethodType)?default("") == "EXT_COD"> selected="selected" </#if>>货到付款</option>
                    </select>
                </div>
                <#--<div class="input-group m-b-10">
                    <span class="input-group-addon">商家</span>
                    <input type="text" name="businessName" class="form-control" value="${(paramMap.businessName)!''}">
                </div>-->
            </div>

            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">查询</button>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <div class="row m-b-12" style="margin-bottom:15px;">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <#--<button id="btn_add" class="btn btn-primary">-->
                        <#--<i class="fa fa-plus">添加</i>-->
                    <#--</button>-->
                    <#if security.hasEntityPermission("REFUND_EXPORT", "_VIEW", session)>
                        <button id="exportOrder" class="btn btn-primary">
                            <i class="fa">批量导出</i>
                        </button>
                    </#if>
                </div>
            </div>
        </div>
    <#if lookupFlag == "Y">
        <ul class="nav nav-tabs">
            <li role="presentation" <#if returnStatus?default("") == "">class="active"</#if>><a href="<@ofbizUrl>findrefund?lookupFlag=Y</@ofbizUrl>">全部退单</a></li>
            <li role="presentation" <#if returnStatus?default("") == "RETURN_WAITEXAMINE">class="active"</#if> ><a  href="javascript:void(0)" onclick="changeTab('RETURN_WAITEXAMINE')">待审核</a></li>
            <li role="presentation" <#if returnStatus?default("") == "RETURN_WAITFEFUND">class="active"</#if> ><a  href="javascript:void(0)" onclick="changeTab('RETURN_WAITFEFUND')">待退款</a></li>
            <li role="presentation" <#if returnStatus?default("") == "RETURN_COMPLETED">class="active"</#if> ><a  href="javascript:void(0)" onclick="changeTab('RETURN_COMPLETED')">已完成</a></li>
            <li role="presentation" <#if returnStatus?default("") == "RETURN_REJECTAPPLY">class="active"</#if> ><a  href="javascript:void(0)" onclick="changeTab('RETURN_REJECTAPPLY')">拒绝退款</a></li>
        </ul>
    </#if>
        <!-- 表格区域start -->
    <#if returnList?has_content>
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
                        <th>商品</th>
                        <th>所属商家</th>
                        <th>供应商</th>
                        <th>实付金额/数量</th>
                        <th>退单时间</th>
                        <th>买家信息</th>
                        <th>申请退款金额</th>
                        <th>实退金额</th>
                        <th>退单状态</th>
                        <th>商家</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list returnList as return>
                            <#assign orderItem = delegator.findByAnd("OrderItem",{"orderId":return.orderId})[0]>
                            <#assign orderPaymentPreferences =  delegator.findByAnd("OrderPaymentPreference",{"orderId":return.orderId}) />
                            <#if  orderPaymentPreferences?has_content>
                                <#assign  orderPaymentPreference = orderPaymentPreferences[0]>
                                <#assign  paymentMethod = delegator.findByPrimaryKey("PaymentMethodType",{"paymentMethodTypeId":orderPaymentPreference.get("paymentMethodTypeId")})>
                            <#else>
                                <#assign  paymentMethod = "">
                            </#if>
                            <#assign product = delegator.findByPrimaryKey("Product",{"productId":return.productId}) />
                            <#assign info = (Static["org.ofbiz.order.order.OrderReadHelper"].getOrderPurchaseInfo(delegator,return.orderId))!'' />
                            <#assign businessName = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderBusinessName(delegator,return.orderId) />

                            <#assign  partyGroupInfos=delegator.findByAnd("PartyGroup",{"productStoreId":return.productStoreId})/>
                            <#if partyGroupInfos?has_content>
                                <#assign  partyName = partyGroupInfos[0].partyName?if_exists/>
                            <#else>
                                <#assign  partyName = "">
                            </#if>

                            <#assign  providerInfos=delegator.findByAnd("Provider",{"providerId":return.providerId?if_exists})/>
                            <#if providerInfos?has_content>
                                <#assign  providerName=providerInfos[0].providerName?if_exists/>
                            <#else>
                                <#assign  providerName=""/>
                            </#if>

                        <tr class="xl_order_num" >
                            <td colspan="9"><input value="${return.returnId}-${return.returnItemSeqId}" class="js-checkchild" type="checkbox"> &nbsp;&nbsp;&nbsp;&nbsp;
                                退单号:${return.returnId}&nbsp;&nbsp;&nbsp;&nbsp;订单号:${return.orderId}&nbsp;&nbsp;&nbsp;&nbsp;
                                <#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if>
                            </td>
                        </tr>
                        <tr class="xl_meg">
                            <td></td>

                            <td>
                            ${product.productName}<br/>
                            </td>
                            <td>${partyName?if_exists}</td>
                            <td>${providerName?if_exists}</td>
                            <td>
                            ${orderItem.unitPrice * return.returnQuantity}（${(return.returnQuantity)!''}件）
                            </td>
                            <td>${return.createdStamp?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>
                                用户：${(info.name)!''}<br/>
                                收货人：${(info.toName)!''}<br/>
                                电话：${(info.mobilePhone)!''}
                            </td>
                            <td>${(return.applyMoney)!''}</td>
                            <td>${(return.actualPaymentMoney)!''}</td>
                            <td>${return.getRelatedOneCache("StatusItem")?if_exists.get("description",locale)?if_exists}</td>
                            <td>${businessName}</td>
                            <td>
                                <#if return.statusId == "RETURN_WAITEXAMINE">
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateReturn("${return.returnId}","RETURN_WAITEXAMINE")'>审核</button>
                                <#elseif return.statusId == "RETURN_WAITSHIP">
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateReturn("${return.returnId}","RETURN_WAITSHIP")'>查看</button>
                                <#elseif return.statusId == "RETURN_WAITRECEIVE">
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateReturn("${return.returnId}","RETURN_WAITRECEIVE")'>收货</button>
                                <#elseif return.statusId == "RETURN_WAITFEFUND">
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateReturn("${return.returnId}","RETURN_WAITFEFUND")'>退款</button>
                                <#elseif return.statusId == "RETURN_COMPLETED">
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateReturn("${return.returnId}","RETURN_COMPLETED")'>查看</button>
                                <#elseif return.statusId == "RETURN_REJECTAPPLY">
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateReturn("${return.returnId}","RETURN_REJECTAPPLY")'>查看</button>
                                <#elseif return.statusId == "RETURN_REJECTRECEIVE">
                                    <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='operateReturn("${return.returnId}","RETURN_REJECTRECEIVE")'>查看</button>
                                </#if>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "findrefund?lookupFlag=Y&"+ paramList + "&"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(returnListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", returnListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=returnListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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




        <script>
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

                $("#exportOrder").click(function(){
                    if (getSelectedIds() != ""){
                        window.location.href = "<@ofbizUrl>exportReturn?ids=" + getSelectedIds() + "</@ofbizUrl>";
                    }else{
                        alert("请至少勾选一条记录");
//                        var checks = $('.js-checkchild');
//		                if(checks.size() >0){
//			                $("#QueryForm").attr("action", "exportReturn").submit();
//			                $("#QueryForm").attr("action", "findrefund");
//		                }else{
//		                	alert("请至少勾选一条记录");
//		                }
                    }
                });

                $('#myModal').modal({
                    show:false
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

            function operateReturn(returnId,statusId){
                window.location.href = "<@ofbizUrl>operateReturn?statusId=" + statusId + "&returnId=" + returnId + "&returnType=1" + "</@ofbizUrl>";
            }

            function changeTab(returnStatus){
                $("#returnStatus").val(returnStatus);
                $("#QueryForm").submit();
            }
        </script>


