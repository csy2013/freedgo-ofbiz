<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "communityReport?lookupFlag=Y&"+ paramList +"&">
<#--${commonUrl}-->
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" method="post" class="form-inline clearfix" role="form" action="<@ofbizUrl>communityReport</@ofbizUrl>">
			<input type="hidden" name="searchfunction" value="findOrders"/>
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group">
                <div class="input-group m-b-10 input-group date form_datetime" data-link-field="startDate">
                    <span class="input-group-addon">开始时间</span>
                    <input class="form-control" size="16" type="text" readonly value="${(paramMap.startDate)!''}">
                    <input id="startDate" type="hidden" name="startDate" value="${(paramMap.startDate)!''}">
                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                </div>
                <div class="input-group m-b-10 input-group date form_datetime"  data-link-field="endDate">
                    <span class="input-group-addon">结束时间</span>
                    <input class="form-control" size="16" type="text" readonly value="${(paramMap.endDate)!''}">
                    <input id="endDate" type="hidden" name="endDate" value="${(paramMap.endDate)!''}">
                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">社区</span>
                    <select name = "communityId" class="form-control">
                        <option value="" <#if (paramMap.communityId)?default("") == ""> selected="selected" </#if> >全部</option>
                        <#list communitys as c >
                            <option value="${c.communityId}" <#if (paramMap.communityId)?default("") == "${c.communityId}"> selected="selected" </#if>>${c.name}</option>
                        </#list>
                    </select>
                </div>
            </div>

            <div class="form-group" data-type="linkLt" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间" data-relation="">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商家</span>
                    <select name = "businessId" class="form-control">
                        <option value="" <#if (paramMap.businessId)?default("") == ""> selected="selected" </#if> >全部</option>
                        <#list businesss as b >
                            <option value="${b.partyId}" <#if (paramMap.businessId)?default("") == "${b.partyId}"> selected="selected" </#if>>${b.businessName}</option>
                        </#list>
                    </select>
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" name="productName" class="form-control" value="${(paramMap.productName)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">下单人手机号</span>
                    <input type="text" name="orderPhone" class="form-control" value="${(paramMap.orderPhone)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单状态</span>
                    <select name = "orderStatus" class="form-control">
                        <option value="" <#if (orderStatus)?default("") == ""> selected="selected" </#if> >全部</option>
                        <option value="ORDER_COMPLETED" <#if (orderStatus)?default("") == "ORDER_COMPLETED"> selected="selected" </#if> >已完成</option>
                        <option value="ORDER_WAITPAY" <#if (orderStatus)?default("") == "ORDER_WAITPAY"> selected="selected" </#if> >待支付</option>
                        <option value="ORDER_WAITSHIP" <#if (orderStatus)?default("") == "ORDER_WAITSHIP"> selected="selected" </#if> >待发货</option>
                        <option value="ORDER_WAITRECEIVE" <#if (orderStatus)?default("") == "ORDER_WAITRECEIVE"> selected="selected" </#if> >待收货</option>
                        <option value="ORDER_WAITEVALUATE" <#if (orderStatus)?default("") == "ORDER_WAITEVALUATE"> selected="selected" </#if> >待评价</option>
                        <option value="ORDER_CANCELLED" <#if (orderStatus)?default("") == "ORDER_CANCELLED"> selected="selected" </#if> >已取消</option>
                        <option value="ORDER_RETURNED" <#if (orderStatus)?default("") == "ORDER_RETURNED"> selected="selected" </#if> >退单</option>
                    </select>
                </div>
                
                 <div class="form-group">
	                <div class="input-group m-b-10">
	                    <span class="input-group-addon">配送方式</span>
	                    <select name = "distributionMethoddd" class="form-control">
	                        <option value="" <#if (paramMap.distributionMethod)?default("") == ""> selected="selected" </#if> >全部</option>
	                        <option value="KDPS" <#if (paramMap.distributionMethod)?default("") == "KDPS"> selected="selected" </#if>>快递配送</option>
	                        <option value="SMZT" <#if (paramMap.distributionMethod)?default("") == "SMZT"> selected="selected" </#if>>上门自提</option>
	                    </select>
	                </div>
	            </div>

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
                    <#if security.hasEntityPermission("VIRTUALORDER_EXPORT", "_VIEW", session)>
                        <button id="exportOrder" class="btn btn-primary">
                            <i class="fa">批量导出</i>
                        </button>
                    </#if>
                </div>
            </div>
        </div>
    <#--<#if lookupFlag == "Y">
        <ul class="nav nav-tabs">
            <li role="presentation" <#if orderStatus?default("") == "">class="active"</#if>><a href="<@ofbizUrl>findorders?lookupFlag=Y</@ofbizUrl>">全部</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_COMPLETED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_COMPLETED')">已完成</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_WAITPAY">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_WAITPAY')">待支付</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_WAITSHIP">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_WAITSHIP')">待发货</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_WAITRECEIVE">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_WAITRECEIVE')">待收货</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_WAITEVALUATE">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_WAITEVALUATE')">待评价</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_CANCELLED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_CANCELLED')">已取消</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_RETURNED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_RETURNED')">退单</a></li>
        </ul>
    </#if>-->
        <!-- 表格区域start -->
    <#if orderList?has_content>
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
                        <th>总价/数量</th>
                        <th>下单时间</th>
                        <th>买家信息</th>
                        <th>实付金额</th>
                        <th>支付方式</th>
                        <th>订单状态</th>
                       <!-- <th>商家</th>-->
                    </tr>
                    </thead>
                    <tbody>
                        <#list orderList as order>
                            <#assign orderPaymentPreferences =  delegator.findByAnd("OrderPaymentPreference",{"orderId":order.orderId}) />
                            <#if  orderPaymentPreferences?has_content>
                                <#assign  orderPaymentPreference = orderPaymentPreferences[0]>
                                <#assign  paymentMethod = delegator.findByPrimaryKey("PaymentMethodType",{"paymentMethodTypeId":orderPaymentPreference.get("paymentMethodTypeId")})>
                            <#else>
                                <#assign  paymentMethod = "">
                            </#if>
                            <#assign products = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProducts(delegator,order.orderId) />
                            <#assign num = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsNum(delegator,order.orderId) />
                            <#assign info = Static["org.ofbiz.order.order.OrderReadHelper"].getToCustomerInfo(delegator,order.orderId) />
                            <#assign name = (Static["org.ofbiz.order.order.OrderReadHelper"].getCustomerName(delegator,order.orderId))!'' />
                            <#assign businessName = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderBusinessName(delegator,order.orderId)?default("") />
                            <#assign  integralDiscount = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "integralDiscount"}).attrValue?number)!0>
                        <tr class="xl_order_num" >
                            <td colspan="10"><input value="${order.orderId}" class="js-checkchild" type="checkbox"> &nbsp;&nbsp;&nbsp;&nbsp;订单号:${order.orderId}
                                &nbsp;&nbsp;&nbsp;&nbsp; <#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></td>
                        </tr>
                        <tr class="xl_meg">
                            <td></td>
                            <td>
                                <#list products as p>
                                <#--${p.productName}<br/>-->
                                <#-- <#if StringUtil.wrapString((p.productName))?length gt 15>
                                 ${(StringUtil.wrapString((p.productName))[0..15])!''}...
                                 <#else>
                                 ${(p.productName)!''}
                                 </#if><br/>-->

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
                            ${(order.shouldPayMoney)!''}（${num}件）
                                <#if order.statusId == "ORDER_WAITPAY">
                                    <br/>
                                    <#if security.hasEntityPermission("ORDER_ADD", "_VIEW", session)>
                                        <button type='button' class='btn btn-sm' onclick='updateMoney("${order.orderId}","${(order.shouldPayMoney)!''}")'>修改金额</button>
                                    </#if>
                                </#if>
                            </td>
                            <td>${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>
                                用户：${name}<br/>
                                收货人：${(info.toName)!''}<br/>
                                电话：${(info.mobilePhone)!''}<br/>
                                收货地址: ${(info.detailAddress)!''}                  
                            </td>
                            <td>${((order.actualPayMoney)?default(0) - integralDiscount)!''}</td>
                            <td>
                                <#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}
                            </#if></td>
                            <td>
                                ${order.getRelatedOneCache("StatusItem").get("description",locale)}
                                <#if order.statusId == "ORDER_WAITSHIP">
                                    <#assign orderItems = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItems(delegator,order.orderId) />
                                    <#list orderItems as oi>
                                        <#assign returnType = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderReturnType(delegator,order.orderId,oi.productId) />
                                        <#if returnType != "">
                                            <#if returnType == "0">
                                                <#assign  url = "findreturn?orderId=${order.orderId}&lookupFlag=Y">
                                            <#elseif returnType == "1">
                                                <#assign  url = "findrefund?orderId=${order.orderId}&lookupFlag=Y&returnType=1">
                                            </#if>
                                            <br/>
                                            <a href="<@ofbizUrl>${url}</@ofbizUrl>" style="color: #ff4500">退款/退货</a>
                                        </#if>
                                    </#list>
                                </#if>
                            </td>
                            <!--<td>${businessName}</td>-->
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "communityReport?lookupFlag=Y&"+ paramList + "&"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(orderListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", orderListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=orderListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
    </div><!-- /.box-body -->
</div><!-- 内容end -->


<script>
    $(function(){
        $('body').append($('#deliveryForm_update'));
        $('body').append($('.dx-modal'));
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
                window.location.href = "<@ofbizUrl>exportOrder?ids=" + getSelectedIds() + "</@ofbizUrl>";
            }else{
                var checks = $('.js-checkchild');
                if(checks.size() >0){
	                $("#QueryForm").attr("action", "exportOrder").submit();
	                $("#QueryForm").attr("action", "communityReport");
                }else{
                	alert("请至少勾选一条记录");
                }
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



    function updateMoney(orderId,shouldPayMoney){
        if (confirm("订单金额不能随意修改，你确定修改吗？")){
            $("#moneyForm_update #orderId").val(orderId);
            $("#moneyForm_update #shouldPayMoney").val(shouldPayMoney);
            $('#moneyForm_update').modal();
        }
    }


    $('#moneyForm_update #save').click(function(){
        $('#moneyForm').dpValidate({
            clear: true
        });
        var shouldPayMoney = parseFloat( $("#shouldPayMoney").val());
        var discountMoney = parseFloat($("#discountMoney").val());
        if (discountMoney > shouldPayMoney){
            alert("优惠金额不能大于应付金额");
            $("#moneyForm_update #save").prop("disabled",false);
        }else{
            $('#moneyForm').submit();
        }
    });

    $('#moneyForm').dpValidate({
        validate: true,
        callback: function(){
            $("#moneyForm_update #save").prop("disabled",true);
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
                    h += "<tr><td>" + l.operateType + "</td><td>" + l.operator + "</td><td>" + l.operateTime + "</td><td>"+ l.operateReason +"</td></tr>";
                }
                $("#logTable tbody").append(h);
                $('#showOperateLog').modal();
            }else{
                $("#logTable tbody").append("<tr><td colspan='4'>无操作记录</td></tr>");
                $('#showOperateLog').modal();
            }
        });
    }

    function viewOrderInfo(orderId ){
        $('#myModal' + orderId).modal();
    }

    function showLogistics(orderId){
        $("#logistics" + orderId).modal();
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

    function changeTab(orderStatus){
        $("#orderStatus").val(orderStatus);
        $("#QueryForm").submit();
    }
</script>


