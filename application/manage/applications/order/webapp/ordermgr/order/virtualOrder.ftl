<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "findVirtualOrders?lookupFlag=Y&"+ paramList +"&">
<#--${commonUrl}-->
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" method="post" class="form-inline clearfix" role="form" action="<@ofbizUrl>findVirtualOrders</@ofbizUrl>">
            <input type="hidden" name="type" value="virtual"/>
            <input type="hidden" name="searchfunction" value="findVirtualOrders"/>
            <input type="hidden" name="lookupFlag" value="Y">
            <input type="hidden" name="orderStatus" id ="orderStatus" value="${(paramMap.orderStatus)!''}">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单号&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <input type="text" name="orderId" class="form-control" value="${(paramMap.orderId)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">用户</span>
                    <input type="text" name="billToName" class="form-control" value="${(paramMap.billToName)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">手机</span>
                    <input type="text" name="partyPhone" class="form-control" value="${(paramMap.partyPhone)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" name="productName" class="form-control" value="${(paramMap.productName)!''}">
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
            <#--<div class="input-group m-b-10">
                <span class="input-group-addon">商家</span>
                <input type="text" name="businessName" class="form-control" value="${(paramMap.businessName)!''}">
            </div>-->
                <div class="input-group pull-right">
                    <button class="btn btn-success btn-flat js-search">搜索</button>
                </div>
            </div>



        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <#--<div class="row m-b-12" style="margin-bottom:15px;">-->
            <#--<div class="col-sm-6">-->
                <#--<div class="dp-tables_btn">-->
                    <#--<#if security.hasEntityPermission("ORDER_EXPORT", "_VIEW", session)>-->
                        <#--<button id="exportOrder" class="btn btn-primary">-->
                            <#--<i class="fa">批量导出</i>-->
                        <#--</button>-->
                    <#--</#if>-->
                <#--</div>-->
            <#--</div>-->
        <#--</div>-->

    <#if security.hasEntityPermission("ORDER_EXPORT", "_VIEW", session)>
        <div class="btn-group">
            <button type="button" class="btn btn-primary" id="allExportOrder">${uiLabelMap.ExportAll}</button>
            <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                <span class="caret"></span>
                <span class="sr-only">Toggle Dropdown</span>
            </button>
            <ul class="dropdown-menu" role="menu">
                <li><a href="#" id="exportOrder">${uiLabelMap.ExportSelectedItem}</a></li>
            </ul>
        </div>
    </#if>

    <#if lookupFlag == "Y">
        <ul class="nav nav-tabs">
            <li role="presentation" <#if orderStatus?default("") == "">class="active"</#if>><a href="<@ofbizUrl>findVirtualOrders?lookupFlag=Y</@ofbizUrl>">全部</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_WAITPAY">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_WAITPAY')">待支付</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_WAITEVALUATE">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_WAITEVALUATE')">待评价</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_COMPLETED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_COMPLETED')">已完成</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_CANCELLED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_CANCELLED')">已取消</a></li>
            <li role="presentation" <#if orderStatus?default("") == "ORDER_RETURNED">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('ORDER_RETURNED')">退单</a></li>
        </ul>
    </#if>
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
                        <#--<th>商家</th>-->
                        <th>操作</th>
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
                            <#assign info = (Static["org.ofbiz.order.order.OrderReadHelper"].getToCustomerInfo(delegator,order.orderId))!'' />
                            <#assign name = (Static["org.ofbiz.order.order.OrderReadHelper"].getCustomerName(delegator,order.orderId))!'' />
                            <#assign businessName = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderBusinessName(delegator,order.orderId)?default("") />
                        <tr class="xl_order_num" >
                            <td colspan="10"><input value="${order.orderId}" class="js-checkchild" type="checkbox"> &nbsp;&nbsp;&nbsp;&nbsp;订单号:${order.orderId}
                                &nbsp;&nbsp;&nbsp;&nbsp; <#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></td>
                        </tr>
                        <tr class="xl_meg">
                            <td></td>
                            <td>
                                <#list products as p>
                                ${p.productName}<br/>
                                </#list>
                            </td>
                            <td>
                            ${order.grandTotal}（${num}件）
                                <#--<#if order.statusId == "ORDER_WAITPAY">-->
                                    <#--<br/>-->
                                    <#--<button type='button' class='btn btn-sm' onclick='updateMoney("${order.orderId}")'>修改金额</button>-->
                                <#--</#if>-->
                            </td>
                            <td>${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>
                                用户：${(name)!''}<br/>
                                <#--<#assign  telPhone = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "telPhone"}).attrValue)!0>-->
                                <#--电话：${info.mobilePhone!''}-->
                            </td>
                            <td>${order.grandTotal}</td>
                            <td><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></td>
                            <td>${order.getRelatedOneCache("StatusItem").get("description",locale)}</td>
                            <#--<td>${businessName}</td>-->
                            <td>
                                <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='viewOrderInfo("${order.orderId}")'>查看</button>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "findVirtualOrders?lookupFlag=Y&"+ paramList + "&"/>
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


<#list orderList as order>
    <#if order.statusId != "ORDER_WAITSHIP">
        <#assign orderPaymentPreferences =  (delegator.findByAnd("OrderPaymentPreference",{"orderId":order.orderId}))!'' />
        <#if orderPaymentPreferences!= "" && orderPaymentPreferences?has_content>
            <#assign  orderPaymentPreference = orderPaymentPreferences[0]>
            <#assign  paymentMethod = delegator.findByPrimaryKey("PaymentMethodType",{"paymentMethodTypeId":orderPaymentPreference.get("paymentMethodTypeId")})>
        <#else>
            <#assign  paymentMethod = "">
            <#assign  orderPaymentPreference = "">
        </#if>
        <#if orderPaymentPreference != "">
            <#assign paymentGatewayResponse =  (delegator.findByAnd("PaymentGatewayResponse",{"orderPaymentPreferenceId":orderPaymentPreference.orderPaymentPreferenceId})[0])!'' />
        <#else>
            <#assign paymentGatewayResponse = "">
        </#if>
        <#assign products = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProducts(delegator,order.orderId) />
        <#assign num = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsNum(delegator,order.orderId) />
        <#assign info = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderPurchaseInfo(delegator,order.orderId) />
        <#assign name = (Static["org.ofbiz.order.order.OrderReadHelper"].getCustomerName(delegator,order.orderId))!'' />
        <#assign businessName = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderBusinessName(delegator,order.orderId)?default("") />
        <#assign  businessDiscountTotal = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "businessHandDiscount"}).attrValue)!0>
        <#assign  integralDiscount = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "integralDiscount"}).attrValue?number)!0>
        <#assign  platDiscountTotal = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "platDiscountTotal"}).attrValue?number)!0>
        <#assign orderAdjustments = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderAdjustments(delegator,order.orderId) />

        <#assign product =  delegator.findByAnd("ProductAndPriceView",{"productId":products[0].productId})[0] />
        <#assign orderItem =  delegator.findByAnd("OrderItem",{"orderId": order.orderId})[0] />
        <#if orderItem.lastUnitPrice?default(0) != 0>
            <#assign unitPrice = orderItem.lastUnitPrice>
        <#else>
            <#assign unitPrice = orderItem.unitPrice>
        </#if>
        <#assign tickets =  (delegator.findByAnd("Ticket",{"orderId":order.orderId}))!'' />
        <#assign originalPrice = unitPrice*num />
        <#assign waitEvaluateTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITEVALUATE"})[0].statusDatetime)!'' />
        <#assign orderCompeletedTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_COMPLETED"})[0].statusDatetime)!'' />


    <div class="xl_dialog" style="display: none;"></div>
    <div class="modal fade dx-modal" id="myModal${order.orderId}" tabindex="-1" role="dialog"
         aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-dialog" style="width:900px;">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close"
                            data-dismiss="modal" aria-hidden="true">
                        &times;
                    </button>
                </div>
                <div class="modal-body">
                    <p class="order_header"><span>订单号：${order.orderId}</span><span>下单日期：${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</span><span><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></span>
                        <span>用户：${name}</span>
                    </p>
                    <div class="xl_content">
                        <p class="p1"><span class="xl_active">待支付</span>
                            <span <#if waitEvaluateTime?has_content>  class="xl_active" </#if>>待评价</span>
                            <span <#if orderCompeletedTime?has_content>  class="xl_active" </#if>>已完成</span>
                        </p>
                        <p class="p2"><span class="xl_active">①</span>
                            <i <#if waitEvaluateTime?has_content>  class="xl_active" </#if>></i><span <#if waitEvaluateTime?has_content>  class="xl_active" </#if>>②</span>
                            <i <#if orderCompeletedTime?has_content>  class="xl_active" </#if>></i><span <#if orderCompeletedTime?has_content>  class="xl_active" </#if>>③</span></p>
                        <p><span class="xl_active xl_time">${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</span>
                            <#if waitEvaluateTime?has_content>
                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitEvaluateTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                            </#if>
                            <#if orderCompeletedTime?has_content>
                                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${orderCompeletedTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                            </#if>
                        </p>
                    </div>
                    <#--<div class="pay_meg">
                        <p class="pay_head">支付信息:</p>
                        <p><label>原始金额:</label><span>${originalPrice}</span></p>
                        <p><label>运费:</label><span>${(order.distributeMoney)!0}</span></p>
                    </div>
                    <div class="pay_meg">
                        <p class="pay_head"><span class="all_price">优惠前总额:</span><span>${originalPrice + (order.distributeMoney)!0}</span></p>
                        <p><label>平台优惠:</label><span>${platDiscountTotal}</span></p>
                        <p><label>积分优惠:</label><span>${integralDiscount}</span></p>
                        <p><label>商家优惠:</label><span>${businessDiscountTotal}</span></p>
                    &lt;#&ndash;<p><label>免运费:</label><span>111111</span></p>&ndash;&gt;
                    &lt;#&ndash;<p><label>商家优惠:</label><span>111111</span><label class="free">免运费:</label><span>111111</span></p>&ndash;&gt;
                    </div>

                    <div class="pay_meg">
                        <p class="pay_head"><span class="all_price">优惠小计:</span><span><#if order.discountMoney?has_content>${(order.discountMoney)!0}<#else>${(integralDiscount)!0}</#if></span></p>
                        <p><label>应付金额:</label><span>${(originalPrice - integralDiscount)!''}</span></p>
                        <div class="row" style="margin-top:10px;">
                            <div class="col-sm-6">
                                <span>实付金额:</span>
                                <span style="margin-left:36px">${((order.actualPayMoney)?default(0) - integralDiscount)!''}</span>
                            </div>
                            <div class="col-sm-6">
                                <span>未付金额：</span>
                                <span>${(order.notPayMoney)!''}</span>
                            </div>
                        </div>
                        <div class="row" style="margin-top:10px;">
                            <div class="col-sm-6">
                                <span>支付方式:</span>
                                <span style="margin-left:36px"><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></span>
                            </div>
                            <div class="col-sm-6">
                                <span>支付流水号：</span>
                                <span><#if paymentGatewayResponse!= ""> ${(paymentGatewayResponse.referenceNum)!''} </#if></span>
                            </div>
                        </div>

                        <div class="row" style="margin-top:10px;margin-bottom:10px">
                            <div class="col-sm-6">
                                <span><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></span>
                                <span style="margin-left:36px">
                                    <#if orderPaymentPreference != "">
                                    <#if orderPaymentPreference.paymentMethodTypeId != "EXT_OFFLINE" &&  orderPaymentPreference.paymentMethodTypeId != "EXT_QB">
                                    ${(orderPaymentPreference.maxAmount)!''}
                                    </#if>
                                    </#if>
                                </span>
                            </div>
                            <div class="col-sm-6">
                                <span>钱包支付：</span>
                                <span>
                                	<!-- 钱宝支付：直接取余额字段 &ndash;&gt;
                                    ${(order.balance)!''}
                                   <!--
                                    <#if orderPaymentPreference != "">
                                    <#if orderPaymentPreference.paymentMethodTypeId == "EXT_QB">
                                    ${(orderPaymentPreference.maxAmount - integralDiscount)!''}
                                    </#if>
                                    </#if>
                                    &ndash;&gt;
                                </span>
                            </div>
                        </div>
                    </div>-->
                    <div class="logistic_order">
                        <p class="pay_head">支付信息:</p>
                        <p class="goods">
                            <i class="address"><span>订单金额:</span><span class="span2">
                                <#assign orderItems = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItems(delegator,order.orderId) />
                               <#assign getOrderItemsTotal = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemsTotal(orderItems,[]) />
                                ${getOrderItemsTotal!""}
                                <#--#{originalPrice!;m2M2}-->
                            </span></i>
                            <i class="address">
                                <span>订单使用积分:</span>
                                <span class="span2">
                                         <#list  orderAdjustments as orderAdjustment>
                                             <#if orderAdjustment.orderAdjustmentTypeId == "COUPON_ADJUESTMENT">
                                             ${orderAdjustment.recurringAmount!''}
                                             </#if>
                                         </#list>

                                    <#--${order.useIntegral ! ""}-->

                                   <#-- <#list  orderAdjustments as orderAdjustment>
                                        <#if orderAdjustment.orderAdjustmentTypeId == "COUPON_ADJUESTMENT">
                                        ${orderAdjustment.recurringAmount!''}
                                        </#if>
                                         <#if orderAdjustment.orderAdjustmentTypeId != "COUPON_ADJUESTMENT">
                                          0
                                        </#if>
                                    </#list>-->
                                </span>
                            </i>
                        </p>
                        <p class="goods">
                            <i class="address">
                                <span>积分优惠:</span>
                                <span class="span2">
                                     <#list  orderAdjustments as orderAdjustment>
                                         <#if orderAdjustment.orderAdjustmentTypeId == "COUPON_ADJUESTMENT">
                                             <#if orderAdjustment.amount<0> ${-orderAdjustment.amount}<#else>${orderAdjustment.amount}</#if>
                                         </#if>
                                     </#list>
                                   <#-- ${order.useIntegral ! ""}-->
                                 </span>

                                    <#--<#list  orderAdjustments as orderAdjustment>
                                        <#if orderAdjustment.orderAdjustmentTypeId == "COUPON_ADJUESTMENT">
                                            <span>积分优惠:</span>
                                           <span class="span2">
                                            <#if orderAdjustment.amount<0> ${-orderAdjustment.amount}<#else>${orderAdjustment.amount}</#if>
                                           </span>
                                        </#if>
                                    </#list>-->
                            </i>
                            <i class="address">
                                <span>应付金额:</span><span class="span2">${(order.grandTotal!)?if_exists.toString()?html}</span>
                            </i>
                        </p>
                        <p class="goods">
                            <i class="address">
                                <span>实付金额:</span><span class="span2">${(order.grandTotal!)?if_exists.toString()?html}</span>
                            </i>
                            <i class="address"><span>支付方式:</span><span class="span2"><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></span></i>
                        </p>
                        <p class="goods">
                            <i class="address"><span>获得积分:</span><span class="span2">${order.get("getIntegral")!'0'}</span></i>
                            <i class="address">
                                <span>支付流水号:</span><span class="span2"><#if paymentGatewayResponse!= ""> ${(paymentGatewayResponse.referenceNum)!''}</#if></span>
                            </i>
                        </p>
                    </div>

                    <div class="logistic_order">
                        <#assign  invoiceType = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "invoiceType"}).attrValue)!'0'>
                        <p class="pay_head">发票信息:</p>
                        <p class="goods">
                            <i class="address"><span>发票类型:</span><span class="span2">${(invoiceType == "1")?string("需要发票","不需要发票")}</span></i>
                            <#if invoiceType == "1">
                                <i class="address"><span>发票内容:</span><span class="span2">${invoiceContent}</span></i>
                            </#if>
                        </p>
                        <#if invoiceType == "1">
                            <p class="goods"><span>发票抬头:</span><span class="span2">${invoiceTitle}</span></p>
                        </#if>
                    </div>

                    <#--<div class="pay_meg">
                        <p class="pay_head"><span class="all_price">商家信息:</span><span>${businessName}</span></p>
                    </div>-->
                    <#assign originalPrice = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsPrice(delegator,order.orderId) />
                    <#--<div class="produce">
                        <p class="pay_head">商品信息</p>
                        <table>
                            <thead><tr><th>商品名称</th><th>原始价格</th><th>惠后价格</th><th>数量</th><th>验证码</th><th>状态</th><th></th></tr></thead>
                                &lt;#&ndash;${tickets?size}&ndash;&gt;
                                <#if tickets?has_content>
                                    <#list tickets as ticket>
                                        <#if ticket.ticketStatus == "notUsed">
                                            <#assign ticketStr = "未使用">
                                        <#elseif  ticket.ticketStatus == "hasUsed">
                                            <#assign ticketStr = "已使用">
                                        <#elseif  ticket.ticketStatus == "notAudited">
                                            <#assign ticketStr = "待审核">
                                        <#elseif  ticket.ticketStatus == "notRefunded">
                                            <#assign ticketStr = "待退款">
                                            <#assign statusId = "RETURN_WAITFEFUND">
                                        <#elseif  ticket.ticketStatus == "hasRefuned">
                                            <#assign ticketStr = "已退款">
                                            <#assign statusId = "RETURN_COMPLETED">
                                        <#elseif  ticket.ticketStatus == "rejectApplication">
                                            <#assign ticketStr = "拒绝申请">
                                            <#assign statusId = "RETURN_REJECTAPPLY">
                                        <#elseif  ticket.ticketStatus == "expired">
                                            <#assign ticketStr = "已过期">
                                        <#else>
                                            <#assign ticketStr = "">
                                         </#if>
                                        <#assign returnId = (delegator.findByAnd("ReturnTicket",{"ticketId" : ticket.ticketId})[0].returnId)!'' >
                                        <#if ticket_index == 0>
                                            <tr>
                                                <td>${product.productName}</td>
                                                <td>${originalPrice}</td>
                                                <td>${unitPrice}</td>
                                                <td>${orderItem.quantity}</td>
                                                <td>${ticket.ticketNo}</td>
                                                <td>${ticketStr}</td>
                                                <td>
                                                    <#if statusId?has_content>
                                                        退款单：<a href="<@ofbizUrl>operateReturn?statusId=${statusId}&returnId=${returnId}&returnType=1</@ofbizUrl>">${returnId}</a>
                                                    </#if>
                                                </td>
                                            </tr>

                                            &lt;#&ndash;<tr> 单个行项目
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                                <td>
                                                    <#assign refund = delegator.findByAnd("ReturnItem",{"orderId" : order.orderId})>
                                                    <#if refund?has_content>
                                                        <a href="<@ofbizUrl>findrefund?orderId=${order.orderId}&lookupFlag=Y&returnType=1</@ofbizUrl>">退款</a>
                                                    </#if>
                                                </td>
                                            </tr>&ndash;&gt;
                                        <#else>
                                            <tr>
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                                <td></td>
                                                <td>${ticket.ticketNo}</td>
                                                <td>${ticketStr}</td>
                                                <td>
                                                    <#if statusId?has_content && returnId?has_content>
                                                        退款单：<a href="<@ofbizUrl>operateReturn?statusId=${statusId}&returnId=${returnId}&returnType=1</@ofbizUrl>">${returnId}</a>
                                                    </#if>
                                                </td>
                                            </tr>

                                        &lt;#&ndash;单个行项目 <tr>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td></td>
                                            <td>
                                                <#assign refund = delegator.findByAnd("ReturnItem",{"orderId" : order.orderId})>
                                                <#if refund?has_content>
                                                    <a href="<@ofbizUrl>findrefund?orderId=${order.orderId}&lookupFlag=Y&returnType=1</@ofbizUrl>">退款</a>
                                                </#if>
                                            </td>
                                            </tr>&ndash;&gt;

                                        </#if>
                                    </#list>
                                    &lt;#&ndash;<tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td>优惠：${(originalPrice -  orderItem.unitPrice) * orderItem.quantity}元</td>
                                        <td></td>
                                        <td>
                                            &lt;#&ndash;<#assign refund = delegator.findByAnd("ReturnItem",{"orderId" : order.orderId})>
                                            <#if refund?has_content>
                                                <a href="<@ofbizUrl>findrefund?orderId=${order.orderId}&lookupFlag=Y&returnType=1</@ofbizUrl>">退款</a>
                                            </#if>&ndash;&gt;
                                        </td>
                                        <td></td>
                                    </tr>&ndash;&gt;
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td>优惠：${(originalPrice -  unitPrice) * orderItem.quantity}元</td>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                </#if>
                            </tr>
                        </table>

                    </div>-->
                    <div class="produce">
                        <p class="pay_head">商品信息</p>
                        <#--<#if tickets?has_content>-->
                            <#--<#list tickets as ticket>-->
                                <#--<p>代金券名称: ${ticket.ticketName}&nbsp;&nbsp;&nbsp;代金券金额:${ticket.ticketName}</p>-->
                                <#--&lt;#&ndash;<#assign productAssoc =  delegator.findByAnd("ProductAssoc",{"productId":products[0].productId})[0] />&ndash;&gt;-->
                                <#--<#assign productAssocInfos =  delegator.findByAnd("ProductAssoc",{"productId":products[0].productId}) />-->
                                <#--<#if productAssocInfos?has_content>-->
                                    <#--<#assign productAssoc =  productAssocInfos[0]  />-->
                                <#--</#if>-->
                                <#--<p>代金券使用范围:-->

                                    <#--<#if productAssoc?if_exists != "">-->
                                        <#--指定商品-->
                                    <#--<#else>-->
                                        <#--全渠道-->
                                    <#--</#if>-->
                                <#--</p>-->
                            <#--</#list>-->
                        <#--</#if>-->
                        <#if products?has_content>
                            <#list products as productInfo>
                                <#assign productPriceSaleList = delegator.findByAnd("ProductPrice", {"productId" : "${productInfo.productId?if_exists}","productPriceTypeId":"DEFAULT_PRICE"})/>
                                <#if productPriceSaleList?has_content>
                                    <#assign priceSaleInfo = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(productPriceSaleList) />
                                    <#if priceSaleInfo?has_content>
                                        <#assign priceSale =priceSaleInfo.price?if_exists/>
                                    </#if>
                                </#if>
                                <p>代金券名称: ${productInfo.productName}&nbsp;&nbsp;&nbsp;代金券金额:${priceSale?if_exists}&nbsp;&nbsp;&nbsp;代金券面额:${productInfo.voucherAmount?default(0)}</p>
                                <#assign productAssocInfos =  delegator.findByAnd("ProductAssoc",{"productId":productInfo.productId}) />
                                <#if productAssocInfos?has_content>
                                    <#assign productAssoc =  productAssocInfos[0]  />
                                </#if>
                                <p>代金券使用范围:

                                    <#if productAssoc?if_exists != "">
                                        指定商品
                                    <#else>
                                        全渠道
                                    </#if>
                                </p>
                            </#list>
                        </#if>


                        <div class="modal-body">
                            <table class="table table-bordered table-hover js-checkparent xl_table" id="logTable">
                                <thead>
                                <tr>
                                    <th>商品</th>
                                    <th>商品编号</th>
                                    <th>商品规格</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if productAssocInfos?has_content>
                                        <#list productAssocInfos as productAssocInfo>
                                            <#assign pInfos=delegator.findByAnd("Product",{"productId":productAssocInfo.productIdTo })/>
                                            <#if pInfos?has_content>
                                                <#assign p=Static["org.ofbiz.entity.util.EntityUtil"].getFirst(pInfos)/>
                                                <tr>
                                                    <td title="${p.productName }">
                                                        <#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : p.productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>
                                                        <#if curProductAdditionalImage1?has_content>
                                                            <#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(p, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />
                                                        <#else>
                                                            <#assign productAdditionalImage1 =""/>
                                                        </#if>
                                                        <span class="col-sm-6">
                                                              <img height="100" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>"  class="cssImgSmall"/>
                                                        </span>

                                                        <#if StringUtil.wrapString((p.productName))?length gt 15>
                                                        ${(StringUtil.wrapString((p.productName))[0..15])!''}...
                                                        <#else>
                                                        ${(p.productName)!''}
                                                        </#if>
                                                    </td>
                                                    <td>${p.primaryProductCategoryId}</td>
                                                    <td>${(p.featureProductName)!''}</td>
                                                </tr>
                                            </#if>

                                        </#list>
                                    </#if>
                                    <#--<#list products as p>-->
                                    <#--<tr>-->
                                        <#--<td title="${p.productName }">-->
                                            <#--<#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : p.productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>-->
                                            <#--<#if curProductAdditionalImage1?has_content>-->
                                                <#--<#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(p, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />-->
                                            <#--<#else>-->
                                                <#--<#assign productAdditionalImage1 =""/>-->
                                            <#--</#if>-->
                                            <#--<span class="col-sm-6">-->
		                                  <#--<img height="100" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>"  class="cssImgSmall"/>-->
		                             <#--</span>-->

                                            <#--<#if StringUtil.wrapString((p.productName))?length gt 15>-->
                                            <#--${(StringUtil.wrapString((p.productName))[0..15])!''}...-->
                                            <#--<#else>-->
                                            <#--${(p.productName)!''}-->
                                            <#--</#if>-->
                                        <#--</td>-->
                                        <#--<td>${p.primaryProductCategoryId}</td>-->
                                        <#--<td>${(p.featureProductName)!''}</td>-->
                                    <#--</tr>-->
                                    <#--</#list>-->
                                </tbody>
                            </table>
                        </div>
                    <#--<div class="logistic_order">
                        <p class="pay_head">物流信息:</p>
                        <p class="goods"><span>送货时间:</span><span class="span2">ddddd</span></p>
                        <p class="goods">
                            <i class="address"><span>收货地址:</span><span class="span2">ddddd</span></i>
                            <i class="address"><span>详细地址:</span><span class="span2">ddddd</span></i>
                        </p>

                        <p class="goods">
                            <i class="address"><span>收货人:</span><span class="span2">${info.toName}</span></i>
                            <i class="address"><span>联系电话:</span><span class="span2">0558 444444</span></i>
                        </p>

                        <p class="goods">
                            <i class="address"><span>手机:</span><span class="span2">${info.mobilePhone}</span></i>
                            <i class="address"><span>邮编:</span><span class="span2"></span></i>
                        </p>

                        <p class="goods"><span>客户留言:</span><span class="span2">好！不错！</span></p>

                    </div>-->
                   <#-- <div class="logistic_order">
                        <p class="pay_head">用户信息：</p>
                        <p class="goods"><span>用户：</span><span class="span2">${info.name}</span></p>
                        <#assign  telPhone = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "telPhone"}).attrValue)!0>
                        <p class="goods"><span>手机号码：</span><span class="span2">${telPhone}</span></p>
                    </div>
                    <#if order.statusId = "ORDER_COMPLETED">
                        <div class="logistic_order">
                            <p class="pay_head">评价：</p>
                            <#assign orderComments = delegator.findByAnd("ProductReview",{"orderId" : order.orderId}) >
                            <#if orderComments?has_content>
                                <a class="btn btn-success btn-flat" href="findcomment?orderId=${order.orderId}&lookupFlag=Y">查看评价</a>
                            </#if>
                        </div>
                    </#if>-->



                </div>
                <div class="modal-footer"></div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal -->
    </div>
    </#if>
</#list>

<div id="deliveryForm_update"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="deliveryForm_update_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="deliveryForm_update_title">订单发货</h4>
            </div>
            <div class="modal-body">
                <form id="deliveryForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>updateDelivery</@ofbizUrl>">
                    <input type="hidden" name = "orderId" id = "orderId" />
                    <div class="form-group" data-type="required" data-mark="快递公司">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>快递公司:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="deliveryCompany" name="deliveryCompany" >
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="物流单号1">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>物流单号:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="logisticsNumber1" name="logisticsNumber1">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">&nbsp;</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="logisticsNumber2" name="logisticsNumber2">
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-sm-2">&nbsp;</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control" id="logisticsNumber3" name="logisticsNumber3">
                        </div>
                    </div>

                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">确认</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>


<div id="moneyForm_update"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="moneyForm_update_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="moneyForm_update_title">修改订单金额</h4>
            </div>
            <div class="modal-body">
                <form id="moneyForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>updateOrderDiscountMoney</@ofbizUrl>">
                    <input type="hidden" name = "orderId" id = "orderId" />
                    <div class="form-group" data-type="required" data-mark="优惠金额">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>优惠金额:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="discountMoney" name="discountMoney" onkeyup="value=this.value.replace(/\D+/g,'')" >
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="优惠原因">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>优惠原因:</label>
                        <div class="col-sm-10">
                            <textarea id="operateReason" name="operateReason" class="form-control dp-vd" rows="3" style="resize: none;"></textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">确认</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>

<div id="cancelForm_update"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="cancelForm_update_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="cancelForm_update_title">取消订单</h4>
            </div>
            <div class="modal-body">
                <form id="cancelForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>cancelOrder</@ofbizUrl>">
                    <input type="hidden" name = "orderId" id = "orderId" />
                    <div class="form-group" data-type="required" data-mark="取消原因">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>取消原因:</label>
                        <div class="col-sm-10">
                            <textarea id="operateReason" name="operateReason" class="form-control dp-vd" rows="3" style="resize: none;"></textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">确认</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>


<div id="showOperateLog"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="showOperateLog_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="showOperateLog_title">操作日志</h4>
            </div>
            <div class="modal-body">
                <table class="table table-bordered table-hover js-checkparent xl_table" id="logTable">
                    <thead>
                    <tr>
                        <th>操作类型</th>
                        <th>操作人</th>
                        <th>操作时间</th>
                        <th>操作原因</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
                <ul class="pagination xl_pages">

                </ul>
            </div>
        <#--<div class="modal-footer">
            <button id="save" type="button" class="btn btn-primary">确认</button>
            <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
        </div>-->
        </div>
    </div>
</div>


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

        <#--$("#exportOrder").click(function(){-->
            <#--if (getSelectedIds() != ""){-->
                <#--window.location.href = "<@ofbizUrl>exportOrder?type=virtual&ids=" + getSelectedIds() + "</@ofbizUrl>";-->
            <#--}else{-->
             	<#--var checks = $('.js-checkchild');-->
                <#--if(checks.size() >0){-->
	                <#--$("#QueryForm").attr("action", "exportOrder").submit();-->
	                <#--$("#QueryForm").attr("action", "findVirtualOrders");-->
                <#--}else{-->
                	<#--alert("请至少勾选一条记录");-->
                <#--}-->
            <#--}-->
        <#--});-->

        $('.js-search').click(function () {
            var endDate= $("#endDate").val();
            var startDate=$("#startDate").val();
            if(startDate>endDate){
                $.tipLayer("开始时间不能大于结束时间");
            }else{
                $('#QueryForm').submit();
            }
            return false;
        });

        $("#exportOrder").click(function(){
            var isHasPaymentMethodType="Y";
            var isHasCommunityStore="N";
            var isHasBusinessName="N";
            var paymentMethodTypeInfo=$("select[name='paymentMethodType']").val();
            var communityStoreIdInfo="";
            var businessNameInfo="";
            var salesOrderType="VIRTUAL";
            if (getSelectedIds() != ""){
                window.location.href = "<@ofbizUrl>exportOrder?ids=" + getSelectedIds() + "&"
                        +"isHasPaymentMethodType="+isHasPaymentMethodType+"&"
                        +"isHasCommunityStore="+isHasCommunityStore+"&"
                        +"isHasBusinessName="+isHasBusinessName+"&"
                        +"paymentMethodTypeInfo="+paymentMethodTypeInfo+"&"
                        +"communityStoreIdInfo="+communityStoreIdInfo+"&"
                        +"businessNameInfo="+businessNameInfo+"&"
                        +"salesOrderType="+salesOrderType+
                                "</@ofbizUrl>";
            }else{
                $.tipLayer("请勾选一条记录");
                //alert("请勾选一条记录");
            }
        });


        $("#allExportOrder").click(function(){
            var orderId=$("input[name='orderId']").val();
            var orderStatus=$("input[name='orderStatus']").val();
            var receivePartyName=$("input[name='receivePartyName']").val();
            var partyPhone=$("input[name='partyPhone']").val();
            var productName=$("input[name='productName']").val();
            var billToName=$("input[name='billToName']").val();
            var startDate=$("input[name='startDate']").val();
            var endDate=$("input[name='endDate']").val();

            var isHasPaymentMethodType="Y";
            var isHasCommunityStore="N";
            var isHasBusinessName="N";
            var paymentMethodTypeInfo=$("select[name='paymentMethodType']").val();
            var communityStoreIdInfo="";
            var businessNameInfo="";
            var salesOrderType="VIRTUAL";


            window.location.href="<@ofbizUrl>allExportOrder?orderId="+orderId+ "&"
                    +"orderStatus="+orderStatus+"&"
                    +"receivePartyName="+receivePartyName+"&"
                    +"partyPhone="+partyPhone+"&"
                    +"productName="+productName+"&"
                    +"billToName="+billToName+"&"
                    +"isHasPaymentMethodType="+isHasPaymentMethodType+"&"
                    +"isHasCommunityStore="+isHasCommunityStore+"&"
                    +"isHasBusinessName="+isHasBusinessName+"&"
                    +"paymentMethodTypeInfo="+paymentMethodTypeInfo+"&"
                    +"communityStoreIdInfo="+communityStoreIdInfo+"&"
                    +"businessNameInfo="+businessNameInfo+"&"
                    +"salesOrderType="+salesOrderType+"&"
                    +"startDate="+startDate+"&"
                    +"endDate="+endDate+
                            "</@ofbizUrl>";
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
        var ids = "";
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

    function changeTab(orderStatus){
        $("#orderStatus").val(orderStatus);
        $("#QueryForm").submit();
    }
</script>


