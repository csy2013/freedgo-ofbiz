<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>

<#assign commonUrl = "givePresentListPage?lookupFlag=Y" +"&">

<div class="box box-info">
	<div class="box-body">
    	<form class="form-inline clearfix" role="form" action="<@ofbizUrl>givePresentListPage</@ofbizUrl>">
        	<div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">赠送人</span>
                    <input type="text" id="sender" name="sender" class="form-control" value="">
                </div>

                <div class="input-group m-b-10 input-group date form_date dp-date-inline"  data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" data-link-field="startDate">
                    <span class="input-group-addon">赠送开始时间</span>
                    <input class="form-control" size="16" type="text" readonly value="">
                    <input id="startDate" type="hidden" name="startDate" value="">
                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                </div>
                <div class="input-group m-b-10 input-group date form_date dp-date-inline"  data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" data-link-field="endDate">
                    <span class="input-group-addon">赠送结束时间</span>
                    <input class="form-control" size="16" type="text" readonly value="">
                    <input id="endDate" type="hidden" name="endDate" value="">
                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                </div>
            </div>
            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
            <div class="row m-b-10">
                <div class="col-sm-6">
                    <div class="dp-tables_btn">

					</div>
                </div>
                
            </div>
        </div>
        <#if givePresentList?has_content>
		   <input id="ids" type="hidden"/>
           <div class="row m-b-10">
                <div class="col-sm-12">
                    <div class="dp-tables_length">
                        <label>
                            ${uiLabelMap.DisplayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="5" <#if viewSize ==5>selected</#if>>5</option>
                                <option value="15" <#if viewSize==15>selected</#if>>15</option>
                                <option value="20" <#if viewSize==20>selected</#if>>20</option>
                                <option value="25" <#if viewSize==25>selected</#if>>25</option>
                            </select>
                            ${uiLabelMap.BrandItem}
                        </label>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <table class="table table-bordered table-hover js-checkparent js-sort-list">
                        <thead>
                        <tr>
                            <th>订单编号</th>
                            <th>赠送人</th>
                            <th>赠送时间</th>
                            <th>领取人</th>
                            <th>领取时间</th>
                            <th>商品</th>
                            <th>${uiLabelMap.BrandOption}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <form method="post" action="" name="editProductBrand" id="editProductBrand">
	                            <#list givePresentList as givePresentRow>
		                            <tr>
				                        <td>${givePresentRow.orderId?if_exists}</td>
				                        <td>${givePresentRow.sMobile?if_exists}</td>
				                        <td><#if givePresentRow.sendDate?has_content>${givePresentRow.sendDate?substring(0,20)}</#if></td>
                                        <#--<td>${givePresentRow.sendDate?if_exists}</td>-->
                                        <td>${givePresentRow.rMobile?if_exists}</td>
                                        <td><#if givePresentRow.receiveDate?has_content>${givePresentRow.receiveDate?substring(0,20)}</#if></td>
                                        <#--<td>${givePresentRow.receiveDate?if_exists}</td>-->
                                        <td>${givePresentRow.productNames?if_exists}</td>

				                        <td>
				                        	<div class="btn-group">
				                        	    <#if security.hasEntityPermission("GIVEPRESENT_LIST", "_VIEW", session)>
					                            <button type="button" class="js-button btn btn-danger btn-sm"  onclick='viewOrderInfo("${givePresentRow.orderId}")' >查看</button>
					                            </#if>
				                        	</div>
				                        </td>
			                        </tr>
	                            </#list>
		                        <input type="hidden" name="operateType" id="operateType"/>
                            </form>
                        </tbody>
                    </table>
                </div>
            </div>
            <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
            <#assign viewIndexFirst = 0/>
            <#assign viewIndexPrevious = viewIndex - 1/>
            <#assign viewIndexNext = viewIndex + 1/>
            <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(totalSize, viewSize) />
            <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", totalSize)/>
            <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
            <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
            listSize=totalSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
            pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
            paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />    
        <#else>
            <div id="">
                <h3>没有赠送数据</h3>
            </div>
        </#if>
        </div><!-- /.box-body -->
    </div>


   <!------------------------------------------------>


<#list givePresentList as givePresentInfo>
    <#if givePresentInfo.orderId?has_content>
		<#assign orders =  delegator.findByAnd("OrderHeader",{"orderId":givePresentInfo.orderId}) />
        <#if orders?has_content>
            <#assign order =  Static["org.ofbiz.entity.util.EntityUtil"].getFirst(orders) />
        </#if>
    </#if>
    <#if order?has_content>
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

        <#if orderItem.lastUnitPrice?default(0) != 0>
            <#assign unitPrice = orderItem.lastUnitPrice>
        <#else>
            <#assign unitPrice = orderItem.unitPrice>
        </#if>
        <#assign num = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsNum(delegator,order.orderId) />
        <#assign info = Static["org.ofbiz.order.order.OrderReadHelper"].getToCustomerInfo(delegator,order.orderId) />
        <#assign userInfo = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderUserInfo(delegator,order.orderId) />
        <#assign name = (Static["org.ofbiz.order.order.OrderReadHelper"].getCustomerName(delegator,order.orderId))!'' />
        <#assign businessName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductStoreNameByOrderId(delegator,order.orderId) />
        <#assign orderGroupId = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderGroupId(delegator,order.orderId) />
        <#assign orderAdjustments = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderAdjustments(delegator,order.orderId) />

        <#assign originalPrice = unitPrice*num />
        <#assign  businessDiscountTotal = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "businessHandDiscount"}).attrValue)!0>
        <#assign  integralDiscount = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "integralDiscount"}).attrValue?number)!0>
        <#assign  platDiscountTotal = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "platDiscountTotal"}).attrValue?number)!0>
        <#assign  invoiceType = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "invoiceType"}).attrValue)!'0'>
        <#assign  invoiceTitle = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "invoiceTitle"}).attrValue)!''>
        <#assign  invoiceContentId = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "invoiceContent"}).attrValue)!''>
        <#assign  invoiceContent = (delegator.findByPrimaryKey("Enumeration",{"enumId":invoiceContentId}).description)!''>
        <#assign  needInvoice = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "needInvoice"}).attrValue)!''>
        <#assign  taxNo = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "taxNo"}).attrValue)!''>
        <#assign  partyNo = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":order.orderId,"attrName" : "partyNo"}).attrValue)!''>

        <#assign waitShipTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITSHIP"})[0].statusDatetime)!'' />
        <#assign waitReceiveTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITRECEIVE"})[0].statusDatetime)!'' />
        <#assign waitEvaluateTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_WAITEVALUATE"})[0].statusDatetime)!'' />
        <#assign orderCompeletedTime =  (delegator.findByAnd("OrderStatus",{"orderId":order.orderId, "statusId": "ORDER_COMPLETED"})[0].statusDatetime)!'' />
        <#assign distributeMethod = "">
        <!--配送方式-->
        <#assign orderItemShipGroups =delegator.findByAnd("OrderItemShipGroup",{"orderId":order.orderId})/>
        <#if orderItemShipGroups?has_content>
            <#assign orderItemShipGroup = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(orderItemShipGroups)/>
            <#if orderItemShipGroup?has_content>
                <#if orderItemShipGroup.shipmentMethodTypeId?default("")=="EXPRESS">
                    <#assign distributeMethod = "快递到家">
                </#if>
            </#if>

        </#if>
       <div class="xl_dialog" style="display: none;"></div>

        <div class="modal fade dx-modal" id="myModal${order.orderId}" tabindex="-1" role="dialog"
             aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal fade"  id = "logistics${order.orderId}" tabindex="1" role="dialog"
                 aria-labelledby="myModalLabel" aria-hidden="true">
                <div class="modal-dialog" style="width:600px;">
                    <div class="modal-content">
                        <div class="modal-header">
                            物流信息
                        </div>
                        <div class="modal-body">
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
                    </div>
                </div>
            </div>
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
                            <#if order.statusId = 'ORDER_WAITRECEIVE'>
                                <#if  paymentMethod != "" && paymentMethod.paymentMethodTypeId == "EXT_COD">
                                    <p class="p1"><span class="xl_active">待发货</span><span class="xl_active">待收货</span><span>待评价</span><span>已完成</span></p>
                                    <p class="p2"><span class="xl_active">①</span><i class="xl_active"></i><span  class="xl_active">②</span><i class="xl_active"></i><span>③</span><i></i><span>④</span></p>
                                    <p>
                                    <#if waitShipTime?has_content>
                                        <span class="xl_active xl_time">${waitShipTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                    </#if>
                                    <#if waitReceiveTime?has_content>
                                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitReceiveTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                    </#if>
                                    </p>
                                <#elseif distributeMethod == "上门自提">
                                    <p class="p1"><span class="xl_active">待支付</span><span class="xl_active">待收货</span><span>待评价</span><span>已完成</span></p>
                                    <p class="p2"><span class="xl_active">①</span><i class="xl_active"></i><span class="xl_active">②</span><i class="xl_active"></i><span>③</span><i></i><span>④</span></p>
                                    <p><span class="xl_active xl_time">${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</span>

                                        <#if waitReceiveTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitReceiveTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                    </p>
                                <#else>

                                    <p class="p1"><span class="xl_active">待支付</span><span class="xl_active">待发货</span><span class="xl_active">待收货</span><span>待评价</span><span>已完成</span></p>
                                    <p class="p2"><span class="xl_active">①</span><i class="xl_active"></i><span  class="xl_active">②</span><i class="xl_active"></i><span class="xl_active">③</span><i></i><span>④</span><i></i><span>⑤</span></p>
                                    <p><span class="xl_active xl_time">${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</span>
                                    <#if waitShipTime?has_content>
                                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitShipTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                    </#if>
                                    <#if waitReceiveTime?has_content>
                                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitReceiveTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                    </#if>
                                    </p>
                                </#if>

                            <#else>
                                <#if paymentMethod != "" && paymentMethod.paymentMethodTypeId == "EXT_COD">
                                    <p class="p1"><span class="xl_active">待发货</span><span>待收货</span><span>待评价</span><span>已完成</span></p>
                                    <p class="p2"><span class="xl_active">①</span><i class="xl_active"></i><span>②</span><i></i><span>③</span><i></i><span>④</span></p>
                                    <p>
                                    <#if waitShipTime?has_content>
                                        <span class="xl_active xl_time">${waitShipTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                    </#if>
                                    <#if waitReceiveTime?has_content>
                                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitReceiveTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                    </#if>
                                    </p>
                                <#elseif distributeMethod == "上门自提">
                                    <p class="p1"><span class="xl_active">待支付</span>
                                        <span <#if waitReceiveTime?has_content>  class="xl_active" </#if>>待收货</span>
                                        <span <#if waitEvaluateTime?has_content>  class="xl_active" </#if>>待评价</span>
                                        <span <#if orderCompeletedTime?has_content>  class="xl_active" </#if>>已完成</span>
                                    </p>
                                    <p class="p2">
                                        <span class="xl_active">①</span>
                                        <i <#if waitReceiveTime?has_content>  class="xl_active" </#if>></i><span <#if waitReceiveTime?has_content>  class="xl_active" </#if>>②</span>
                                        <i <#if waitEvaluateTime?has_content>  class="xl_active" </#if>></i><span <#if waitEvaluateTime?has_content>  class="xl_active" </#if>>③</span>
                                        <i <#if orderCompeletedTime?has_content>  class="xl_active" </#if>></i><span <#if orderCompeletedTime?has_content>  class="xl_active" </#if>>④</span>
                                    </p>
                                    <p>
                                        <span class="xl_active xl_time">${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        <#if waitReceiveTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitReceiveTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                        <#if waitEvaluateTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitEvaluateTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                        <#if orderCompeletedTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${orderCompeletedTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                    </p>
                                <#else>
                                    <p class="p1"><span class="xl_active">待支付</span>
                                        <span  <#if waitShipTime?has_content>  class="xl_active" </#if> >待发货</span>
                                        <span <#if waitReceiveTime?has_content>  class="xl_active" </#if>>待收货</span>
                                        <span <#if waitEvaluateTime?has_content>  class="xl_active" </#if>>待评价</span>
                                        <span <#if orderCompeletedTime?has_content>  class="xl_active" </#if>>已完成</span>
                                    </p>
                                    <p class="p2">
                                        <span class="xl_active">①</span>
                                        <i <#if waitShipTime?has_content>  class="xl_active" </#if>></i><span <#if waitShipTime?has_content>  class="xl_active" </#if>>②</span>
                                        <i <#if waitReceiveTime?has_content>  class="xl_active" </#if>></i><span <#if waitReceiveTime?has_content>  class="xl_active" </#if>>③</span>
                                        <i <#if waitEvaluateTime?has_content>  class="xl_active" </#if>></i><span <#if waitEvaluateTime?has_content>  class="xl_active" </#if>>④</span>
                                        <i <#if orderCompeletedTime?has_content>  class="xl_active" </#if>></i><span <#if orderCompeletedTime?has_content>  class="xl_active" </#if>>⑤</span>
                                    </p>
                                    <p><span class="xl_active xl_time">${order.orderDate?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        <#if waitShipTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitShipTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                        <#if waitReceiveTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitReceiveTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                        <#if waitEvaluateTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${waitEvaluateTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                        <#if orderCompeletedTime?has_content>
                                            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span class="xl_active xl_time">${orderCompeletedTime?string("yyyy-MM-dd HH:mm:ss")}</span>
                                        </#if>
                                    </p>
                                </#if>

                            </#if>

                        </div>
                        <div class="logistic_order">
                            <p class="pay_head">支付信息:</p>
                            <p class="goods">
                                <i class="address"><span>原始金额:</span><span class="span2">
                                  <#--   <#assign getOrderItemsTotal = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemsTotal() />-->
                                     <#assign getOrderItemsTotal = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemsTotal(orderItems,[]) />
                                      ${getOrderItemsTotal!""}
                                </span></i>
                                <i class="address">
                                    <span>运费:</span>
                                    <span class="span2">
                                    <#list  orderAdjustments as orderAdjustment>
                                        <#if orderAdjustment.orderAdjustmentTypeId == "SHIPPING_CHARGES">
                                            ${(orderAdjustment.amount)!0}
                                        </#if>
                                    </#list>
                                    </span>
                                </i>
                            </p>
                            <#list  orderAdjustments as orderAdjustment>
                                <#if orderAdjustment.orderAdjustmentTypeId == "PROMOTION_ADJUSTMENT">
                                    <#assign  promoName = (delegator.findByPrimaryKey("ProductPromo",{"productPromoId":orderAdjustment.productPromoId}).promoName)!''>
                                    <#if orderAdjustment.amount !=0>
                                        <p class="goods">
                                            <i class="address">
                                                <span>促销类型:</span>
                                                <span class="span2">
                                                 <#--<#list promoTypes as promoType>
                                                    ${promoType ! ""}
                                                        <#if promoType_has_next>,</#if>
                                                    </#list>-->
                                                     ${promoName !""}
                                        </span>
                                            </i>
                                            <i class="address">
                                                <span>促销优惠:</span><span class="span2"><#if orderAdjustment.amount<0> ${-orderAdjustment.amount}<#else>${orderAdjustment.amount}</#if></span>
                                            </i>
                                        </p>
                                    </#if>
                                </#if>

                                <#if orderAdjustment.orderAdjustmentTypeId == "COUPON_ADJUESTMENT">
                                    <p class="goods">
                                        <i class="address"><span>优惠券名称:</span><span class="span2">${orderAdjustment.comments!''}</span></i>
                                        <i class="address">
                                            <span>优惠券优惠:</span><span class="span2"><#if orderAdjustment.amount<0> ${-orderAdjustment.amount}<#else>${orderAdjustment.amount}</#if></span>
                                        </i>
                                    </p>
                                </#if>

                                <#if orderAdjustment.orderAdjustmentTypeId == "TICKET_ADJUESTMENT">
                                    <p class="goods">
                                        <i class="address"><span>代金券名称:</span><span class="span2">${orderAdjustment.comments!''}</span></i>
                                        <i class="address">
                                            <span>代金券优惠:</span><span class="span2"><#if orderAdjustment.amount<0> ${-orderAdjustment.amount}<#else>${orderAdjustment.amount}</#if></span>
                                        </i>
                                    </p>
                                </#if>

                                <#if orderAdjustment.orderAdjustmentTypeId == "INTEGRAL_ADJUESTMENT">
                                    <p class="goods">
                                        <i class="address"><span>订单使用积分:</span><span class="span2">${orderAdjustment.recurringAmount!''}</span></i>
                                        <i class="address">
                                            <span>积分优惠:</span><span class="span2"><#if orderAdjustment.amount<0> ${-orderAdjustment.amount}<#else>${orderAdjustment.amount}</#if></span>
                                        </i>
                                    </p>
                                </#if>

                                <#if orderAdjustment.orderAdjustmentTypeId == "GROUP_PROMO_TYPE">
                                    <#assign  promoName = (delegator.findByPrimaryKey("ProductGroupPromo",{"productGrpId":orderAdjustment.productPromoId}).promoName)!''>
                                    <#if orderAdjustment.amount !=0>
                                        <p class="goods">
                                            <i class="address">
                                                <span>促销名称:</span>
                                                <span class="span2">
                                                    ${promoName !""}
                                                </span>
                                            </i>
                                            <i class="address">
                                                <span>促销优惠:</span><span class="span2"><#if orderAdjustment.amount<0> ${-orderAdjustment.amount}<#else>${orderAdjustment.amount}</#if></span>
                                            </i>
                                        </p>
                                    </#if>
                                </#if>
                            </#list>
                            <p class="goods">
                                <i class="address"><span>应付金额:</span><span class="span2">${(order.grandTotal!)?if_exists.toString()?html}</span></i>
                                <i class="address">
                                    <span>实付金额:</span><span class="span2"><#if order.statusId!="ORDER_WAITPAY">${(order.grandTotal!)?if_exists.toString()?html}</#if></span>
                                </i>
                            </p>
                            <p class="goods">
                                <i class="address"><span>支付方式:</span><span class="span2"><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></span></i>
                                <i class="address">
                                    <span>支付流水号:</span><span class="span2"><#if paymentGatewayResponse!= ""> ${(paymentGatewayResponse.referenceNum)!''}</#if></span>
                                </i>
                            </p>
                            <p class="goods">
                                <i class="address"><span>获得积分:</span><span class="span2">${order.get("getIntegral")!'0'}</span></i>
                                <i class="address">
                                    <span>店铺:</span><span class="span2">${businessName}</span>
                                </i>
                            </p>
                        </div>


                        <div class="logistic_order">
                            <p class="pay_head">物流信息:</p>
                                <#assign shipInfo = (delegator.findByAnd("OrderDelivery",{"orderId":order.orderId})[0])!''>
                            <p class="goods">
                                <i class="address"><span>配送方式:</span><span class="span2">${distributeMethod}</span></i>
                                <i class="address"><span>快递公司:</span>
                                        <#if shipInfo != "">
                                            <span class="span2">
                                                ${(delegator.findByAnd("LogisticsCompany",{"companyId":shipInfo.deliveryCompany})[0].companyName)!''}
                                            </span>
                                        </#if>
                                </i>
                            </p>
                            <p class="goods">
                                <i class="address"><span>快递单号:</span><span class="span2">
                                        <#if shipInfo != "">
                                            ${(shipInfo.logisticsNumber1)!''}&nbsp;&nbsp;${(shipInfo.logisticsNumber2)!''}&nbsp;&nbsp;${(shipInfo.logisticsNumber3)!''}</span>
                                        </#if>
                                    </span></i>
                                <i class="address"><span>运费:</span><span class="span2">
                                        <#list  orderAdjustments as orderAdjustment>
                                        <#if orderAdjustment.orderAdjustmentTypeId == "SHIPPING_CHARGES">
                                            ${(orderAdjustment.amount)!0}
                                        </#if>
                                        </#list>
                                </span></i>
                            </p>
                            <p class="goods">
                                <i class="address"><span>收货地址:</span><span class="span2">${(info.address)!''}</span></i>
                                <i class="address">
                                    <span>详细地址:</span><span class="span2">${(info.detailAddress)!''}</span>
                                </i>
                            </p>
                            <p class="goods">
                                <i class="address"><span>收货人:</span><span class="span2">${(info.toName)!''}</span></i>
                                <i class="address">
                                    <span>电话:</span><span class="span2">${(info.tel)!''} &nbsp;${info.mobilePhone}</span>
                                </i>
                            </p>
                            <p class="goods">
                                <i class="address"><span>客户留言:</span><span class="span2">${(order.remarks)!''}</span></i>
                                <i class="address">
                                    <span>身份证号:</span><span class="span2">${partyNo?if_exists}</span>
                                </i>
                            </p>
                        </div>


                        <div class="logistic_order">
                            <p class="pay_head">发票信息:</p>
                            <#if needInvoice=="Y">
                                <p class="goods">
                                    <i class="address"><span>发票类型:</span><span class="span2">${(invoiceType == "Company")?string("企业发票","个人发票")}</span></i>
                                    <#if invoiceType == "Company">
                                    <#--<i class="address"><span>发票内容:</span><span class="span2">${invoiceContent}</span></i>-->
                                        <i class="address"><span>纳税人识别号:</span><span class="span2">${taxNo}</span></i>
                                    </#if>
                                </p>
                                <#if invoiceType == "Company">
                                        <p class="goods"><span>发票抬头:</span><span class="span2">${invoiceTitle}</span></p>
                                </#if>
                            <#else>
                                <p class="goods">
                                    <i class="address"><span>发票类型:</span><span class="span2">不需要发票</span></i>
                                </p>
                            </#if>
                        </div>


                        <div class="produce">
                            <p class="pay_head">商品信息</p>
                            <table>
                                <thead>
                                <th>商品名称</th>
                                <th>原始价格</th>
                                <th>商品规格</th>
                                <th>数量</th>
                                <th>惠后价格</th>
                                <#--<th>商品总价</th>-->
                                <th>状态</th>
                                </thead>
                                <tbody>
                                    <#list orderItems as oi>
                                        <#assign originalPrice = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsPrice(delegator,oi.orderId) />
                                        <#assign  p =  delegator.findByPrimaryKey("Product",{"productId": oi.productId})>
                                        <#assign  activity =  (delegator.findByPrimaryKey("ProductActivity",{"activityId": oi.activityId}))!''>
                                        <#assign returnType = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderReturnType(delegator,order.orderId,oi.productId) />
                                        <#assign fs = Static["org.ofbiz.order.order.OrderReadHelper"].getProductFeature(delegator,oi.productId) />
                                        <#assign getOrderItemSubTotal = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemSubTotal(oi,orderAdjustments,false,false) />

                                    <tr>
                                        <td title="${(p.productName)!''}"><#--${p.productName}-->
                                            <#assign curProductAdditionalImage1 = delegator.findByAnd("ProductContent", {"productId" : p.productId?if_exists,"productContentTypeId":"ADDITIONAL_IMAGE_1"})/>
                                            <#if curProductAdditionalImage1?has_content>
                                                <#assign productAdditionalImage1 = (Static["org.ofbiz.product.product.ProductContentWrapper"].getProductContentAsText(p, "ADDITIONAL_IMAGE_1", locale, dispatcher))?if_exists />
                                            <#else>
                                                <#assign productAdditionalImage1 =""/>
                                            </#if>
                                            <img height="100" src="<#if productAdditionalImage1?has_content><@ofbizContentUrl>${productAdditionalImage1}</@ofbizContentUrl></#if>"  class="cssImgSmall"  />

                                            <#if StringUtil.wrapString((p.productName))?length gt 11>
                                                ${(StringUtil.wrapString((p.productName))[0..11])!''}...
                                            <#else>
                                                ${(p.productName)!''}
                                            </#if>
                                        </td>
                                        <td>
                                            <#if oi.lastUnitPrice?default(0) != 0>
                                                <#assign unitPrice = oi.lastUnitPrice>
                                            <#else>
                                                <#assign unitPrice = oi.unitPrice>
                                            </#if>
                                            ${unitPrice!''}

                                        </td>
                                        <td>
                                            ${(p.featureProductName)!''}
                                        </td>
                                        <td>${oi.quantity}</td>
                                        <td>${getOrderItemSubTotal}</td>

                                        <td >
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
                                            <#if activity != "">${(delegator.findByPrimaryKey("Enumeration",{"enumId":activity.activityType}).description)!''}</#if>优惠：${unitPrice*oi.quantity - getOrderItemSubTotal}元</td>
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
                    </div>
                    <div class="modal-footer"></div>
                </div><!-- /.modal-content -->
            </div><!-- /.modal -->
        </div>
    </#if>
</#list>


  <!-------------------------------------------------->

<script language="JavaScript" type="text/javascript">

    $(function() {
        $('.form_datetime').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });

        $('.form_date').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            minView: 2,
            forceParse: 0
        });

        $('.form_time').datetimepicker({
            language: 'zh-CN',
            autoclose: 1,
            startView: 1,
            forceParse: 0
        });
    });
    function viewOrderInfo(orderId ){
        $('#myModal' + orderId).modal();
    }
</script>

    


