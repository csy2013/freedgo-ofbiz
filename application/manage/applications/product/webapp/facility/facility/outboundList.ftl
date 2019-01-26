<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<#assign commonUrl = "outboundList?lookupFlag=Y&"+ paramList +"&">
<#--${commonUrl}-->
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" method="post" class="form-inline clearfix" role="form" action="<@ofbizUrl>outboundList</@ofbizUrl>">

            <input type="hidden" name="orderStatus" id ="orderStatus" value="${(paramMap.orderStatus)!''}">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单号</span>
                    <input type="text" name="orderId" class="form-control" value="${(paramMap.orderId)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">收货人</span>
                    <input type="text" name="receivePartyName" class="form-control" value="${(paramMap.receivePartyName)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">手机</span>
                    <input type="text" name="partyPhone" class="form-control" value="${(paramMap.partyPhone)!''}">
                </div>
            </div>
			<div class="form-group w-p100">
				<div class="input-group m-b-10">
                    <span class="input-group-addon">运单号</span>
                    <input type="text" name="logisticsNumber1" class="form-control" value="${(paramMap.logisticsNumber1)!''}">
                </div>
	            <div class="form-group" data-type="linkLt" data-compare-link="endTimeGroup" data-mark="送达时间" data-compare-mark="送达时间至" data-relation="">
	                <div class="input-group m-b-10 input-group date form_date dp-date-inline"  data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" data-link-field="startDate">
	                    <span class="input-group-addon">送达时间</span>
	                    <input class="form-control" size="16" type="text" readonly value="${(paramMap.deliveryStartDate)!''}">
	                    <input id="deliveryStartDate" type="hidden" name="deliveryStartDate" value="${(paramMap.deliveryStartDate)!''}">
	                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
	                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
	                </div>
	                <div class="input-group m-b-10 input-group date form_date dp-date-inline"  data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" data-link-field="endDate">
	                    <span class="input-group-addon">至</span>
	                    <input class="form-control" size="16" type="text" readonly value="${(paramMap.deliveryEndDate)!''}">
	                    <input id="deliveryEndDate" type="hidden" name="deliveryEndDate" value="${(paramMap.deliveryEndDate)!''}">
	                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
	                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
	                </div>
	            </div>
	            
	            <div class="input-group pull-right">
	                <button class="btn btn-success btn-flat">搜索</button>
	            </div>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->
        
		<div class="row">
			<div class="dp-tables_btn col-sm-12 m-b-10">
	        	<#if orderStatus == "ORDER_WAITSHIP" > 
		          <button id="btn_batch_out_bound" class="btn btn-primary">
		                        批量出库
		          </button>
		        </#if>
		        
		        <div class='btn-group'>
	                <button type='button' class='btn btn-primary' onclick='exportOutBoundOrder("ALL")'>导出全部</button>
	                <button type='button' class='btn btn-primary dropdown-toggle' data-toggle='dropdown'>
	                    <span class='caret'></span>
	                    <span class='sr-only'>Toggle Dropdown</span>
	                </button>
	                <ul class='dropdown-menu' role='menu'>
	                    <li><a href='javascript:exportOutBoundOrder("SELECT")'>导出选中项</a></li>
	                </ul>
	            </div>
	        </div>
        </div>

        <ul class="nav nav-tabs">
            <li role="presentation" <#if orderStatus?default("") == "ORDER_WAITSHIP">class="active"</#if>><a href="<@ofbizUrl>outboundList</@ofbizUrl>">待发货</a></li>
            <li role="presentation" <#if orderStatus?default("") == "AlreadyShip">class="active"</#if> ><a href="javascript:void(0)" onclick="changeTab('AlreadyShip')">已发货</a></li>
        </ul>
        <!-- 表格区域start -->
    <#if orderList?has_content>
        <!-- 列表当前分页条数start -->
        <div class="row m-b-12 m-t-10">
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
                    	<th>
                    		<input class="js-allcheck" type="checkbox"/>
                    	</th>
                        <th>商品</th>
                        <th>总价/数量</th>
                        <th>收货人</th>
                        <th>下单时间</th>
                        <th>送达时间</th>
                        <th>发货仓库</th>
                        <th>运单号</th>
                        <th>物流公司</th>
                        <th>打印状态</th>
                        <th style="width: 120px;">操作</th>
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
                            <#assign  facilityName = (delegator.findByPrimaryKey("Facility",{"facilityId":order.originFacilityId}).facilityName)!''>
                            <#assign orderDelivery =  delegator.findByAnd("OrderDelivery",{"orderId":order.orderId}) />
                        <tr class="xl_order_num" >
                            <td colspan="10">订单号:${order.orderId}</td>
                        </tr>
                        <tr class="xl_meg">
                        	<td>
                        		<input value="${order.orderId}" class="js-checkchild" type="checkbox">
                        	</td>
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
                            ${(order.shouldPayMoney)!''}<br/>（${num}件）
                            </td>
                            <td>
                                ${(info.toName)!''}<br/>
                                ${(info.mobilePhone)!''}
                            </td>
                            <td>${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td><#if order.deliveryDate?has_content>${order.deliveryDate?string("yyyy-MM-dd")}</#if></td>
                            <td>${facilityName}</td>
                            <td>
                            	<#if orderDelivery?has_content>
                            		<span id="span_${orderDelivery.get(0).id}">
                            			${(orderDelivery.get(0).logisticsNumber1)!''}
                            		</span>
                            	</#if>
                            </td>
                            <td>
                            <#if orderDelivery?has_content>
                            	<#assign  logisticsCompanyId = (delegator.findByAnd("LogisticsTemple",{"logisticsTempleId":orderDelivery.get(0).deliveryCompany}).get(0).logisticsCompanyId)!''>
                            	<#assign  companyName = (delegator.findByPrimaryKey("LogisticsCompany",{"id":logisticsCompanyId}).companyName)!''>
                            	${companyName}
                            </#if>
                            </td>
                            <td>
                            <#assign isPrint = 'N' />
                            <#if orderDelivery?has_content>
	                            <#if orderDelivery.get(0).isPrint?has_content && orderDelivery.get(0).isPrint == "Y">
	                            	已打印
	                            	<#assign isPrint = 'Y' />
	                            	<#else>
	                            	未打印
	                            </#if>
                            	<#else>未打印
                            </#if>
                            </td>
                            <td>
                                <#if order.statusId == "ORDER_WAITSHIP">
                                    <div class='btn-group'>
                                        <button type='button' class='btn btn-danger btn-sm' onclick='showDeliveryForm("${order.orderId}","${isPrint}")'>商品出库</button>
                                        <button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>
                                            <span class='caret'></span>
                                            <span class='sr-only'>Toggle Dropdown</span>
                                        </button>
                                        <ul class='dropdown-menu' role='menu'>
                                            <li><a href='javascript:viewOrderInfo("${order.orderId}")'>查看详情</a></li>
                                            <li><a href='javascript:showOperateLog("${order.orderId}")'>操作日志</a></li>
                                        </ul>
                                    </div>
                                <#else>
                                    <div class='btn-group'>
                                        <button type='button' class='btn btn-danger btn-sm btn-sm'  onclick='viewOrderInfo("${order.orderId}")'>查看详情</button>
                                        <button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>
                                            <span class='caret'></span>
                                            <span class='sr-only'>Toggle Dropdown</span>
                                        </button>
                                        <ul class='dropdown-menu' role='menu'>
                                        <#if orderDelivery?has_content && security.hasEntityPermission("FACILITYMGR_OUT", "_UPDATE", session)>
                                            <li><a id="updateLogisticsNumber_${orderDelivery.get(0).id}" href='javascript:updateLogisticsNumber("${orderDelivery.get(0).id}","${orderDelivery.get(0).logisticsNumber1?if_exists}","${order.orderId}")'>修改运单号</a></li>
                                        </#if>
                                            <li><a href='javascript:showOperateLog("${order.orderId}")'>操作日志</a></li>
                                        </ul>
                                    </div>
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
        <#assign commonUrl = "outboundList?lookupFlag=Y&"+ paramList + "&"/>
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
    <#assign orderPaymentPreferences =  delegator.findByAnd("OrderPaymentPreference",{"orderId":order.orderId}) />
    <#if  orderPaymentPreferences?has_content>
        <#assign  orderPaymentPreference = orderPaymentPreferences[0]>
        <#assign  paymentMethod = delegator.findByPrimaryKey("PaymentMethodType",{"paymentMethodTypeId":orderPaymentPreference.get("paymentMethodTypeId")})>
    <#else>
        <#assign  orderPaymentPreference = "">
        <#assign  paymentMethod = "">
    </#if>
    <#if orderPaymentPreference != "">
        <#assign paymentGatewayResponse =  (delegator.findByAnd("PaymentGatewayResponse",{"orderPaymentPreferenceId":orderPaymentPreference.orderPaymentPreferenceId})[0])!'' />
    <#else>
        <#assign paymentGatewayResponse = "">
    </#if>
    <#assign orderItems = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItems(delegator,order.orderId) />
    <#assign orderItem = orderItems[0] />
    <#assign originalPrice = 0/>
    <#list orderItems as oi >
        <#assign originalPrice = originalPrice + oi.unitPrice * oi.quantity />
    </#list>
    <#assign info = Static["org.ofbiz.order.order.OrderReadHelper"].getToCustomerInfo(delegator,order.orderId) />
    <#assign userInfo = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderUserInfo(delegator,order.orderId) />
    <#assign name = (Static["org.ofbiz.order.order.OrderReadHelper"].getCustomerName(delegator,order.orderId))!'' />
    <#assign businessName = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderBusinessName(delegator,order.orderId)?default("") />
    <#assign  businessDiscountTotal = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "businessHandDiscount"}).attrValue)!0>
    <#assign  integralDiscount = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "integralDiscount"}).attrValue?number)!0>
    <#assign  platDiscountTotal = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "platDiscountTotal"}).attrValue?number)!0>
    <#assign  invoiceType = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "invoiceType"}).attrValue)!'0'>
    <#assign  invoiceTitle = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "invoiceTitle"}).attrValue)!''>
    <#assign  invoiceContentId = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "invoiceContent"}).attrValue)!''>
    <#assign  invoiceContent = (delegator.findByPrimaryKey("Enumeration",{"enumId":invoiceContentId}).description)!''>

    <#assign waitShipTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITSHIP"})[0].statusDatetime?string("yyyy-MM-dd HH:mm:ss"))!'' />
    <#assign waitProduceTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITPRODUCE"})[0].statusDatetime?string("yyyy-MM-dd HH:mm:ss"))!'' />
    <#assign waitReceiveTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITRECEIVE"})[0].statusDatetime?string("yyyy-MM-dd HH:mm:ss"))!'' />
    <#assign waitEvaluateTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITEVALUATE"})[0].statusDatetime?string("yyyy-MM-dd HH:mm:ss"))!'' />
    <#assign orderCompeletedTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_COMPLETED"})[0].statusDatetime?string("yyyy-MM-dd HH:mm:ss"))!'' />
    <#if (order.distributionMethod)?default("") == "GZRPS">
        <#assign shipTime = "工作日配送">
        <#assign distributeMethod = "快递配送">
    <#elseif (order.distributionMethod)?default("") == "ZMPS">
        <#assign shipTime = "周末假日配送">
        <#assign distributeMethod = "快递配送">
    <#else>
        <#assign shipTime = "">
        <#assign distributeMethod = "上门自提">
    </#if>
<div class="xl_dialog" style="display: none;"></div>

<div class="otherLayerHiddenBox"  id = "logistics${order.orderId}" style="display:none">
        <#assign logisticss = delegator.findByAnd("OrderDelivery",{"orderId": order.orderId}) />
        <#if logisticss?has_content>
            <#assign logisticsNumber = (logisticss.get(0).logisticsNumber1)!''/>
        <#else>
            <#assign logisticsNumber = ""/>
        </#if>
        <#if logisticsNumber == "">
            没有物流信息
        <#else>
            <#assign deliveryItems = Static ["org.ofbiz.order.order.OrderReadHelper"].getDeliveryItems(delegator,logisticsNumber) />
            <#if deliveryItems?has_content>
                <#list deliveryItems as di>
                    <p>${di.dateTime?string("yyyy-MM-dd HH:mm:ss")} &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ${di.description}   </p>
                </#list>
            <#else>
                没有物流信息
            </#if>
        </#if>
</div>
<div class="modal fade dx-modal" id="myModal${order.orderId}" tabindex="-1" role="dialog"   aria-labelledby="myModalLabel" aria-hidden="true">

    <div class="modal-dialog" style="width:800px;">
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
                    <div class="mark-group col-6 active">
                        <span class="title">待支付</span>
                        <div class="mark-box">
                            <i class="num-mark">1</i>
                        </div>
                        <span class="time">${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</span>
                    </div>
                    <div class="mark-group col-6 <#if waitProduceTime?has_content> active</#if>">
                        <span class="title">待生产</span>
                        <div class="mark-box">
                            <i class="num-mark">2</i>
                        </div>
                        <span class="time">${waitProduceTime}</span>
                    </div>
                    <div class="mark-group col-6 <#if waitShipTime?has_content> active</#if>">
                        <span class="title">待发货</span>
                        <div class="mark-box">
                            <i class="num-mark">3</i>
                        </div>
                        <span class="time">${waitShipTime}</span>
                    </div>
                    <div class="mark-group col-6 <#if waitReceiveTime?has_content> active</#if>">
                        <span class="title">待收货</span>
                        <div class="mark-box">
                            <i class="num-mark">4</i>
                        </div>
                        <span class="time">${waitReceiveTime}</span>
                    </div>
                    <div class="mark-group col-6 <#if waitEvaluateTime?has_content> active</#if>">
                        <span class="title">待评价</span>
                        <div class="mark-box">
                            <i class="num-mark">5</i>
                        </div>
                        <span class="time">${waitEvaluateTime}</span>
                    </div>
                    <div class="mark-group col-6  <#if orderCompeletedTime?has_content> active</#if>">
                        <span class="title">已完成</span>
                        <div class="mark-box">
                            <i class="num-mark">6</i>
                        </div>
                        <span class="time"${orderCompeletedTime}</span>
                    </div>
                </div>
                <div class="pay_meg">
                    <p class="pay_head">支付信息:</p>
                    <p><label>原始金额:</label><span>${originalPrice}</span></p>
                    <p><label>运费:</label><span>${(order.distributeMoney)!0}</span></p>
                </div>
                <div class="pay_meg">

                    <p class="pay_head"><span class="all_price">优惠前总额:</span><span>${originalPrice + (order.distributeMoney)!0}</span></p>
                    <p><label>平台优惠:</label><span>${platDiscountTotal}</span></p>
                    <p><label>积分优惠:</label><span>${integralDiscount}</span></p>
                    <#--<p><label>商家优惠:</label><span>${businessDiscountTotal}</span></p>-->
                <#--<p><label>免运费:</label><span>111111</span></p>-->
                <#--<p><label>商家优惠:</label><span>111111</span><label class="free">免运费:</label><span>111111</span></p>-->
                </div>

                <div class="pay_meg">
                    <p class="pay_head"><span class="all_price">优惠小计:</span><span><#if order.discountMoney?has_content>${(order.discountMoney)!0}<#else>${(integralDiscount)!0}</#if> </span></p>
                    <p><label>应付金额:</label><span>${((originalPrice + (order.distributeMoney)!0) - integralDiscount)!''}</span></p>
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
                            <span><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if>:</span>
                                <span style="margin-left:36px">
                                    <#if orderPaymentPreference != "">
                                    <#if orderPaymentPreference.paymentMethodTypeId != "EXT_OFFLINE" &&  orderPaymentPreference.paymentMethodTypeId != "EXT_QB" && orderPaymentPreference.statusId != "PAYMENT_NOT_RECEIVED">
                                    ${(orderPaymentPreference.maxAmount)!''}
                                    </#if>
                                    </#if>
                                </span>
                        </div>
                        <div class="col-sm-6">
                            <span>钱包支付：</span>
                                <span>
                                    <#if orderPaymentPreference != "">
                                    <#if orderPaymentPreference.paymentMethodTypeId == "EXT_QB">
                                    ${(orderPaymentPreference.maxAmount - integralDiscount)!''}
                                    </#if>
                                    </#if>
                                </span>
                        </div>
                    </div>
                </div>

                <div class="pay_meg">
                    <p class="pay_head"><span class="all_price">获得积分:</span><span>${order.get("getIntegral")!'0'}</span></p>
                </div>

                <#--<div class="pay_meg">
                    <p class="pay_head"><span class="all_price">商家信息:</span><span>${businessName}</span></p>
                </div>-->

                    <div class="pay_meg">
                        <p class="pay_head">
                            <span class="all_price">订单类型:</span>
                            <span>${delegator.findByPrimaryKey("OrderType",{"orderTypeId" : order.orderTypeId}).get("description",locale)}</span>
                        </p>
                        <#if (order.complainId)?default("") != "">
                            <p class="pay_head">
                            <span class="all_price">申诉说明:</span>
                            <span>${delegator.findByPrimaryKey("ComplainHeader",{"complainId" : order.complainId}).remarks}</span>
                            </p>
                        </#if>
                    </div>


                <div class="produce">
                    <p class="pay_head">商品信息</p>
                    <table width="750" border="1">
                        <thead>
                        <th>商品名称</th>
                        <th>原始价格</th>
                        <th width="20%">商品配置</th>
                        <th>数量</th>
                        <th>惠后价格</th>
                        <th>商品总价</th>
                        <th>状态</th>
                        </thead>
                        <tbody>
                            <#list orderItems as oi>
                                <#assign originalPrice = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsPrice(delegator,oi.orderId,oi.orderItemSeqId) />
                                <#assign productSettingName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductSettingName(delegator,oi.productId,oi.orderId,oi.orderItemSeqId) />
                                <#assign  p =  delegator.findByPrimaryKey("Product",{"productId": oi.productId})>
                                <#assign  activity =  (delegator.findByPrimaryKey("ProductActivity",{"activityId": oi.activityId}))!''>
                                <#assign returnType = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderReturnType(delegator,order.orderId,oi.productId) />
                                <#assign fs = Static["org.ofbiz.order.order.OrderReadHelper"].getProductFeature(delegator,oi.productId) />

                            <tr>
                                <td><#--${p.productName}-->
                                    <#--<#if StringUtil.wrapString((p.productName))?length gt 4>
                                    <a title=" ${(p.productName)!''}">${(StringUtil.wrapString((p.productName))[0..4])!''}...</a>
                                    <#else>
                                    ${(p.productName)!''}
                                    </#if>-->
                                ${(p.productName)!''}
                                </td>
                                <td>${originalPrice}</td>
                                <td>
                                    <div class="express_area"><p> ${productSettingName}</p></div>
                                </td>
                                <td>${oi.quantity}</td>
                                <td>${oi.unitPrice}</td>
                                <td>${oi.quantity * oi.unitPrice}</td>
                                <td>
                                    <#assign status = delegator.findByPrimaryKey("StatusItem",{"statusId":oi.get("statusId")})>
                                        ${(status.get("description",locale))!""}
                                </td>
                            </tr>
                            <tr>
                                <td>
                                </td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td>
                                    <#if activity != "">${(delegator.findByPrimaryKey("Enumeration",{"enumId":activity.activityType}).description)!''}</#if>优惠：<#--${(originalPrice -  oi.unitPrice) * oi.quantity}-->0元</td>
                                <td></td>
                                <td>
                                    <#if returnType != "">
                                        <#if returnType == "0">
                                            <#assign  url = "findreturn?orderId=${order.orderId}&lookupFlag=Y">
                                        <#elseif returnType == "1">
                                            <#assign  url = "findrefund?orderId=${order.orderId}&lookupFlag=Y&returnType=1">
                                        </#if>
                                        <a href="<@ofbizUrl>${url}</@ofbizUrl>">退款/退货</a>
                                    </#if>
                                </td>
                            </tr>
                            </#list>

                        </tbody>
                    </table>

                </div>
                <#if shipTime!= "">
                    <div class="logistic_order">
                        <p class="pay_head">物流信息:</p>
                        <p class="goods"><span>送货时间:</span>
                                <span class="span2">
                                ${shipTime}
                                </span>
                        </p>
                        <p class="goods">
                            <i class="address"><span>收货地址:</span><span class="span2">${(info.address)!''}</span></i>
                            <i class="address"><span>详细地址:</span><span class="span2">${(info.detailAddress)!''}</span></i>
                        </p>

                        <p class="goods">
                            <i class="address"><span>收货人:</span><span class="span2">${info.toName}</span></i>
                            <i class="address"><span>联系电话:</span><span class="span2">${(info.tel)!''}</span></i>
                        </p>

                        <p class="goods">
                            <i class="address"><span>手机:</span><span class="span2">${info.mobilePhone}</span></i>
                            <i class="address"><span>邮编:</span><span class="span2">${(info.postalCode)!''}</span></i>
                        </p>
                    </div>
                <#else>
                    <div class="logistic_order">
                        <p class="pay_head">用户信息:</p>
                        <p class="goods">
                            <i class="address"><span>用户昵称:</span><span class="span2">${userInfo.name}</span></i>
                            <i class="address"><span>手机号码:</span><span class="span2">${userInfo.fromMobile}</span></i>
                        </p>
                    </div>
                </#if>

                <div class="logistic_order">
                    <p class="pay_head">快递信息:</p>
                    <#assign shipInfo = (delegator.findByAnd("OrderDelivery",{"orderId":order.orderId})[0])!''>
                    <p class="goods">
                        <i class="address"><span>配送方式:</span><span class="span2">${distributeMethod}</span></i>
                        <i class="address">
                            <#if shipInfo != "">
                                <span>快递公司:</span>
                                <span class="span2">
                                    <#assign ltt = (delegator.findByPrimaryKey("LogisticsTemple",{"logisticsTempleId" : shipInfo.deliveryCompany}))!''>
                                    <#if ltt != "">
                                    <#assign dc = delegator.findByPrimaryKey("LogisticsCompany",{"id" : ltt.logisticsCompanyId})>
                                        <#if dc != "">
                                            ${(dc.companyName)!''}
                                        </#if>
                                    </#if>
                                </span>
                            </#if>
                        </i>
                    </p>
                    <#if shipInfo != "">
                        <p class="goods">
                            <span>快递单号:</span><span id="myModal_logisticsNumber1" class="span2">
                            <#if shipInfo != "">
                            ${(shipInfo.logisticsNumber1)!''}&nbsp;&nbsp;${(shipInfo.logisticsNumber2)!''}&nbsp;&nbsp;${(shipInfo.logisticsNumber3)!''}</span>
                            </#if>
                        <p class="goods"> <button class="btn btn-success btn-flat" onclick="showLogistics('${order.orderId}')">查看物流</button></p>
                        </p>
                    </#if>
                <#--<p class="goods"><span>客户留言:</span><span class="span2">${(order.remarks)!""}</span></p>-->

                </div>

                <div class="logistic_order">
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
                    <p class="goods">
                        <span>评价内容:</span>
                        <#assign orderComments = delegator.findByAnd("ProductReview",{"orderId" : order.orderId}) >
                        <#if orderComments?has_content>
                            <a class="btn btn-success btn-flat" href="findcomment?orderId=${order.orderId}&lookupFlag=Y">查看评价</a>
                        </#if>

                    </p>
                </div>
                <div class="logistic_order">
                    <p class="pay_head">备注:</p>
                    <p class="goods">
                        <i class="address"><span>${(order.remarks)!''}</span></i>
                    </p>
                </div>
            </div>
            <div class="modal-footer"></div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>
</#list>


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
                        <th>操作结果</th>
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

<div id="deliveryForm_update"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="_update_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="deliveryForm_update_title">修改运单号</h4>
            </div>
            <div class="modal-body">
                <form id="deliveryForm" method="post" class="form-horizontal">
                    <input type="hidden" name = "id" id = "id" />
                    <input type="hidden" name="orderId" id="orderId" />
                    <div class="form-group">
                        <label class="control-label col-sm-2">原运单号:</label>
                        <input type="hidden" id="oldNumber">
                        <div class="col-sm-10" id="oldNumberText">

                        </div>
                    </div>
                    <div class="form-group" data-type="required" data-mark="物流单号">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>新运单号:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="logisticsNumber1" name="logisticsNumber1">
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

        $('#myModal').modal({
            show:false
        });
        
        $("#btn_batch_out_bound").click(function(){
        	var del_ids="";
        	var checks = $('.js-checkparent .js-checkchild:checked');
	    	//判断是否选中记录
	    	if(checks.size() > 0 ){
		    	//编辑id字符串
	    		checks.each(function(){ 
					del_ids += $(this).val() + ","; 
				});
				
				//异步调用出库操作
				$.ajax({
					url: "/ordermgr/control/batchOutBound?externalLoginKey=${externalLoginKey}",
					type: "GET",
					data : {ids : del_ids},
					dataType : "json",
					success: function(data){
						var html = "已选订单数量："+data.totalNum + "</br>"+
								   "出库成功："+data.successNum + "</br>"+
								   "出库失败："+data.failNum;
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html(html);
						$('#modal_msg').modal();
						if(data.successNum > 0){
							window.location.reload();//刷新当前页面
						}
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
						$('#modal_msg').modal();
					}
				});
	    	}else{
	    		//设置提示弹出框内容
	    		$('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
	    		$('#modal_msg').modal();
	    	}
        });
    });

    //出库
    function showDeliveryForm(orderId,isPrint){
    	//根据订单号查询快递单
		$.ajax({
			url: "getExpressOrderByOrderId${externalKeyParam}",
			type: "POST",
			async : false,
			data:{orderId : orderId},
			dataType : "json",
			success: function(data){
				if(!data.expressOrderInfo.logisticsCompanyId){
					$.tipLayer("请设置快递单据打印模板！");
				}else{
					if(isPrint == "N"){
			    		window.location.href = "<@ofbizUrl>deliveryOrder</@ofbizUrl>?orderId="+orderId;
			    	}else{
						//出库
			            $.ajax({
			                url: "<@ofbizUrl>updateDelivery</@ofbizUrl>",
			                type: "POST",
			                data: {
			                	orderId : orderId,
			                	deliveryCompany : data.expressOrderInfo.companyName,
			                	logisticsNumber : data.expressOrderInfo.logisticsNumber1,
			                	defaultName : "Y",
			                	productName : data.expressOrderInfo.productName,
			                	sendName : data.expressOrderInfo.sendName,
			                	sendAddress : data.expressOrderInfo.sendAddress,
			                	sendTelphone : data.expressOrderInfo.sendTelphone
			                },
			                dataType: "json",
			                success: function (data) {
			                    if (data.status == "S") {
			                        $.tipLayer("出库成功！");
			                        window.location.reload();//刷新当前页面
			                    } else {
			                        $.tipLayer("出库失败："+data.msg);
			                    }
			                },
			                error: function (data) {
			                    $.tipLayer("操作失败！");
			                }
			            });
					}
				}				
			},
			error: function(data){
				$.tipLayer("操作失败！");
			}
		});
    }

    //修改物流单号
    function updateLogisticsNumber(id,logisticsNumber,orderId){
        $("#deliveryForm_update #id").val(id);
        $("#deliveryForm_update #orderId").val(orderId);
        $("#deliveryForm_update #oldNumber").val(logisticsNumber);
        $("#deliveryForm_update #oldNumberText").text(logisticsNumber);
        $("#deliveryForm_update #logisticsNumber1").val("");
        $('#deliveryForm_update').modal();
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
            $('#deliveryForm_update').modal('hide');
            $.ajax({
                url: "<@ofbizUrl>updateLogisticsNumber</@ofbizUrl>",
                type: "POST",
                data : $('#deliveryForm').serialize(),
                dataType : "json",
                success: function(data){
                    $("#updateLogisticsNumber_"+$("#deliveryForm_update #id").val()).attr("href",'javascript:updateLogisticsNumber("'+$("#deliveryForm_update #id").val()+'","'+$("#deliveryForm_update #logisticsNumber1").val()+'","'+$("#deliveryForm_update #orderId").val()+'")');
                    $("#span_"+$("#deliveryForm_update #id").val()).text($("#deliveryForm_update #logisticsNumber1").val());
                    $("#myModal"+$("#deliveryForm_update #orderId").val()+" #myModal_logisticsNumber1").text($("#deliveryForm_update #logisticsNumber1").val());
                    $.tipLayer("操作成功！");
                },
                error: function(data){
                    $.tipLayer("操作失败！");
                }
            });
        }
    });

    //弹出窗关闭事件
    $('#deliveryForm_update').on('hide.bs.modal', function () {
        $('#deliveryForm').dpValidate({
            clear: true
        });
    });

    $('#showOperateLog').on('hide.bs.modal', function () {
        $("#logTable tbody").html("");
    });

    //查看操作日志
    function showOperateLog(orderId){
        $.post("orderDeliveryLog",{orderId : orderId},function(data){
            var logList = data.logList;
            if (logList != null && logList != "" && logList != undefined){
                var h= "";
                for (var i = 0; i < logList.length; i++){
                    var l = logList[i];
                    h += "<tr><td>" + l.operateType + "</td><td>" + l.operator + "</td><td>" + l.operateTime + "</td><td>"+ l.operateResult +"</td></tr>";
                }
                $("#logTable tbody").append(h);
                $('#showOperateLog').modal();
            }else{
                $("#logTable tbody").append("<tr><td colspan='4'>无操作记录</td></tr>");
                $('#showOperateLog').modal();
            }
        });
    }

    //查看详情
    function viewOrderInfo(orderId ){
        $('#myModal' + orderId).modal();
    }

    function changeTab(orderStatus){
        $("#orderStatus").val(orderStatus);
        $("#QueryForm").submit();
    }
    
    //导出
    function exportOutBoundOrder(type){
    	var del_ids="";
    	if(type == "SELECT"){
    		var checks = $('.js-checkparent .js-checkchild:checked');
    		if(checks.size() == 0 ){
    			//设置提示弹出框内容
	    		$('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
	    		$('#modal_msg').modal();
	    		return;
    		}else{
		    	//编辑id字符串
	    		checks.each(function(){ 
					del_ids += $(this).val() + ","; 
				});
    		}
    	}
    	
    	window.location.href = "/ordermgr/control/exportOutBoundOrder?ids=" + del_ids +"&exportType="+type+"&orderStatus=${orderStatus}";
    	
    }
    
    function showLogistics(orderId){
        $.otherLayer({
            title : '物流信息',
            content : $("#logistics" + orderId)
        });
    }
</script>

