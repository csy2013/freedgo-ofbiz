<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<style>

    .express_area {
        position: relative;
        height: 25px;
        overflow-y: hidden;
        z-index: 99;
    }

    .express_area p {
        position: absolute;
        left: 0;
        top: 0;
        display: block;
        padding: 0 10px;
        line-height: 25px;
    }

    .express_area:hover {
        overflow-y: visible;
    }

    .express_area:hover p {
        background: #DFEDF7;
    }
</style>
<#assign complainHeader = delegator.findByPrimaryKey("ComplainHeader",{"complainId" : requestParameters.complainId}) >
<#assign originalOrder = delegator.findByPrimaryKey("OrderHeader",{"orderId" : complainHeader.orderId}) >
<#assign partyId = delegator.findByAnd("UserLogin",{"userLoginId" : complainHeader.complainer})[0].get("partyId") >
<#assign complainItems = delegator.findByAnd("ComplainItem",{"complainId" : complainHeader.complainId}) >
<#assign mobile = (delegator.findByAnd("PartyAndPerson",{"partyId" : partyId})[0].get("mobile"))!'' >
<#assign contactMechId = delegator.findByAnd("OrderContactMech",{"orderId" : complainHeader.orderId})[0].get("contactMechId") >
<#assign postalAddress = delegator.findByAnd("PostalAddress",{"contactMechId" : contactMechId}) >
<#assign currenPartyId = (delegator.findByAnd("OrderRole",{"orderId" : complainHeader.orderId})[0].get("partyId"))!'' >
<#assign refunds = delegator.findByAnd("ReturnItem",{"complainId" : complainHeader.complainId}) >
<#assign orders = delegator.findByAnd("OrderHeader",{"complainId" : complainHeader.complainId}) >
<#assign isComplainCompeleted = (Static["org.ofbiz.order.order.OrderReadHelper"].getIsComplainCompeleted(delegator,complainHeader.orderId,complainHeader.complainId))!'' />

<#if refunds?has_content>
    <#assign refund = refunds[0]>
<#else>
    <#assign refund = "">
</#if>
<#if orders?has_content>
    <#assign order = orders[0]>
<#else>
    <#assign order = "">
</#if>
<div class="box box-info">
   <#-- <p class = "del">
    <#if returnType?default("0") == "1">
        退款单处理 &nbsp;&nbsp;&nbsp;&nbsp;<a  class="btn btn-primary pull-right"  href ="javascript:history.go(-1);">返回</a>
    <#else>
        退货单处理 &nbsp;&nbsp;&nbsp;&nbsp;<a  class="btn btn-primary pull-right"  href ="javascript:history.go(-1);">返回</a>
    </#if>
    </p>-->
       <p class = "del">
           申诉单处理 &nbsp;&nbsp;&nbsp;&nbsp;<a  class="btn btn-primary pull-right"  href ="javascript:history.go(-1);">返回</a>
       </p>
    <table class="basic-info" border="1">
        <tr class="t_head"><th colspan="2">基本信息</th></tr>
        <tr>
            <td width="50%">
                <div class="refund-msg">
                    <p><label>申诉单号&nbsp;:</label><span>${complainHeader.complainId}</span></p>
                    <p><label>订单号&nbsp;:</label><a style="cursor: pointer;" href="javascript:window.location='/ordermgr/control/findorders${externalKeyParam}&orderId=${complainHeader.orderId}';">${complainHeader.orderId}</a></p>
                    <p><label>用户名&nbsp;:</label><span><#if postalAddress?has_content>${postalAddress[0].toName?default("")}</#if></span></p>
                    <p><label>手机号&nbsp;:</label><span><#if postalAddress?has_content>${postalAddress[0].mobilePhone?default("")}</#if></span></p>
                    <p><label>创建时间&nbsp;:</label><span>${complainHeader.get("createdStamp")?string("yyyy-MM-dd HH:mm:ss")}</span></p>
                    <p>
                        <label>申诉问题&nbsp;:</label>
                        <span>
                            <#if complainHeader.statusId == "COMPLAIN_HASACCEPTED">
                                <#assign questions = delegator.findByAnd("Enumeration",{"enumTypeId" : "APPEAL_PROBLEM","enumCode" : "N"} ,["sequenceId"]) >
                                <select class="dp-vd w-p5" onchange="changeQuestion(this)">
                                <#list questions as q>
                                    <option value="${q.enumId}" <#if complainHeader.complainQuestion == "${q.enumId}" > selected </#if>>${q.description}</option>
                                </#list>
                                </select>
                            <#else>
                                ${(delegator.findByPrimaryKey("Enumeration",{"enumId":delegator.findByPrimaryKey("ComplainHeader",{"complainId" : complainHeader.complainId}).complainQuestion}).description)!''}
                            </#if>
                        </span>
                    </p>
                </div>
            </td>
            <td>
                <div class="refund-msg">
                    <p style="margin-bottom:5px;"><label>
                    申诉说明&nbsp;:</label>

                    <div class="express_area">
                    	<p>
                    	${(Static["org.ofbiz.base.util.UtilHttp"].decodeURL(complainHeader.remarks))}
                   		</p>
                   	</div>
                        <span></span>
                    </p>
                    <p style="margin-top:10px"> 凭证&nbsp;:</p>
                    <#assign imagesUrls = Static["org.ofbiz.order.order.OrderReadHelper"].getComplainImages(delegator,requestParameters.complainId) />
                    <#if imagesUrls?has_content>
                        <div class="img-box">
                            <ul class="banner">
                                <#list imagesUrls as url>
                                    <li><img src="${url}"></li>
                                </#list>
                            </ul>

                            <div class="number">
                                <a href="javascript:;" class="glyphicon glyphicon-chevron-left icon1"></a>
                                <a href="javaScript:;" class="glyphicon glyphicon-chevron-right icon2"></a>
                            </div>
                        </div>
                    </#if>
                </div>
            </td>

        </tr>
    </table><!--基本信息表结束-->
    <!--商品表-->
    <table border="1" class="table-hover each-product">
        <tr><th>商品名称</th><th>用户定义名称</th><th>商品配置</th><th>数量</th>
        <#if refund != "" || order != ""><th>处理单号</th></#if>
        </tr>
        <#list complainItems as ci>
            <#assign product = delegator.findByPrimaryKey("Product",{"productId" : ci.productId }) >
            <#assign productSettingName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductSettingName(delegator,ci.productId,ci.orderId,ci.orderItemSeqId) />
            <#assign item = delegator.findByPrimaryKey("OrderItem",{"orderId" : ci.orderId,"orderItemSeqId" : ci.orderItemSeqId}) >
            <tr>
                <td>${product.productName}</td>
                <td><#if (product.productName)?default("") != (item.itemDescription)?default("")> ${(item.itemDescription)!''}</#if></td>
                <td>${productSettingName}</td>
                <td>${ci.quantity}</td>
                <#if refund != "">
                    <td><a href="/ordermgr/control/findrefund?returnId=${refund.returnId}&lookupFlag=Y&returnType=1">退款单：${refund.returnId}</a></td>
                <#elseif order != "">
                    <td><a href="/ordermgr/control/findorders?orderId=${order.orderId}">订单：${order.orderId}</a></td>
                </#if>
            </tr>
        </#list>
    </table>
    <!--操作日志开始-->
    <div class="operate">
        <div class="operate-head">操作日志</div>
        <div class="operate-container clearfix">
            <div class="span-left">
            <#list comList as com>
                <p>${com.operateTime?string("yyyy-MM-dd HH:mm:ss")}</p>
            </#list>
            </div>
            <div class="span-right">
            <#list comList as com>
            	<#assign userLogin = delegator.findByPrimaryKey("UserLogin",{"userLoginId" : com.operator}) >
            	<#assign person = delegator.findByPrimaryKey("Person",{"partyId" : userLogin.partyId}) >
                <p>
                    ${com.operateType}&nbsp;(&nbsp;操作人：<i>
                    ${person.partyId}
                    <#if person.nickname?has_content>
                    	_${Static["org.ofbiz.base.util.UtilHttp"].decodeURL(person.nickname)}
                    </#if>
                    </i>&nbsp;)
                    <#if com.operateReason?has_content>
                        &nbsp;&nbsp;备注：${(com.operateReason)!''}
                    </#if>
                </p>
            </#list>
            </div>
        </div>

    </div><!--操作日志结束-->

    <#if requestParameters.statusId == "COMPLAIN_HASACCEPTED" >
        <div class="refund-money">
            <div class="logist-head">客服处理</div>
            <form id="form" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>updateComplain</@ofbizUrl>">
                <input name="complainId" value = "${complainHeader.complainId}" type="hidden">
                <input name="statusId" value = "COMPLAIN_COMPLETED" type="hidden">
                <input id="complainQuestion" name="complainQuestion" value = "${complainHeader.complainQuestion}" type="hidden">
                <div class="form-group">
                    <div class="col-sm-4 col-sm-offset-2" style="margin-top:5px">
                            <#if originalOrder.orderTypeId != "REPLENISHMENT_ORDER">
                                <button type="button" class="btn btn-primary btn_save1" id="create"  data-toggle="modal">创建退款单</button>
                            </#if>
                            <button type="button" class="btn btn-primary btn_save2" id="order"  data-toggle="modal">补下订单</button>
                    </div>
                </div>
                <div class="form-group" data-type="required" data-mark="处理结果">
                    <label  class="col-sm-2 control-label"><i class="required-mark">*</i>备注</label>
                    <div class="col-sm-4">
                        <textarea id="operateReason" name="operateReason" class="form-control dp-vd" rows="3" style="resize: none;"></textarea>
                        <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-4 col-sm-offset-2">
                        <button id="next" class="btn btn-primary" style="margin-top:35px">
                            <i class="fa">确定</i>
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </#if>
</div>
<#assign retrunItems = delegator.findByAnd("ReturnItem",{"orderId" : complainHeader.orderId}) >
<#assign orderItems = Static["org.ofbiz.order.order.OrderReadHelper"].getBHOrderItems(delegator,complainHeader.orderId) >
<div class="otherLayerHiddenBox"  id = "otherLayerHiddenBox" style="display:none">
</div>
<div class="modal fade bs-example-modal-lg" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog modal-lg"  role="dialog">
        <div class="modal-content">
            <#--<div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">添加投诉单</h4>
            </div>-->
            <div class="modal-body">
                <div class="col-sm-12">
                    <#if retrunItems?has_content>
                        <table class="table table-bordered table-hover">
                            <tr><th>商品名称</th><th>用户自定义名称</th><th>商品配置</th><th>数量</th><th>处理单号</th></tr>
                            <#list retrunItems as ri>
                                <#assign product = delegator.findByPrimaryKey("Product",{"productId" : ri.productId}) >
                                <#assign productSettingName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductSettingName(delegator,ri.productId,ri.orderId,ri.orderItemSeqId) />
                                <#assign orderItem = delegator.findByPrimaryKey("OrderItem",{"orderId" : ri.orderId,"orderItemSeqId" : ri.orderItemSeqId}) >
                                <tr>
                                    <td>${product.productName}</td>
                                    <td>${(orderItem.itemDescription)!''}</td>
                                    <td>${productSettingName}</td>
                                    <td>${ri.returnQuantity}</td>
                                    <td><a href="/ordermgr/control/findrefund?returnId=${ri.returnId}&lookupFlag=Y&returnType=1">退款单：${ri.returnId}</a></td>
                            </#list>
                        </table>
                    </#if>
                    <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr class="js-sort-list">
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>商品名称</th><th>用户自定义名称</th><th>商品配置</th><th>数量</th><th>惠后价格</th><th>订单惠后价格</th><th>商品总价</th>

                    </tr>
                    </thead>
                    <tbody>
                    <#assign fee = (originalOrder.distributeMoney)?default(0) - (originalOrder.freeFee)?default(0) >
                    <#if (originalOrder.shouldPayMoney > fee)>
                        <#assign isReturnFee = "1">
                    <#else>
                        <#assign isReturnFee = "0">
                    </#if>
                    <#list complainItems as ci>
                        <#assign product = delegator.findByPrimaryKey("Product",{"productId" : ci.productId }) >
                        <#assign oi = delegator.findByPrimaryKey("OrderItem",{"orderId" : ci.orderId, "orderItemSeqId" : ci.orderItemSeqId}) >
                        <#assign productSettingName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductSettingName(delegator,ci.productId,ci.orderId,ci.orderItemSeqId) />
                        <#if isReturnFee == "1">
                            <#assign proportion = (originalOrder.shouldPayMoney - fee) / originalOrder.originalMoney >
                        <#else>
                            <#assign proportion = originalOrder.shouldPayMoney / originalOrder.originalMoney >
                        </#if>
                        <tr>
                            <td><input class="js-checkchild" type="checkbox" value="${product.productId}"></td>
                            <td>${product.productName}</td>
                            <td>${(oi.itemDescription)!''}</td>
                            <td>${productSettingName}</td>
                            <td><input type="text" name = "${product.productId}" class="product" size="1" unitPrice="${oi.unitPrice * proportion}" maxQuantity="${ci.quantity}" orderItemSeqId="${ci.orderItemSeqId}" value = "${ci.quantity}" onkeyup="value=this.value.replace(/\D+/g,'')" /></td>
                            <td>${oi.unitPrice}</td>
                            <td>${oi.unitPrice * proportion}</td>
                            <td>${oi.unitPrice * proportion * ci.quantity}</td>
                        </tr>
                    </#list>
                    </tbody>
                    </table>

                </div>
                <form class="form-horizontal" id="addFirstForm" method="post" action="<@ofbizUrl>createRefund</@ofbizUrl>" >
                    <input type="hidden" name = "complainId" value="${complainHeader.complainId}" />
                    <input type="hidden" name = "orderId" value="${complainHeader.orderId}" />
                    <input type="hidden" name = "params" id="paramss" value="" />
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="退款原因">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>退款原因:</label>
                            <div class="col-sm-9">
                                <textarea id="operateReason" name="operateReason" class="form-control dp-vd  w-p50" rows="3" style="resize: none;"></textarea>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="应退运费">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>应退运费:</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control dp-vd w-p50" readonly id="logisticsMoney" name="logisticsMoney" onkeyup="clearNoNum(this)" value="0" >
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="申请退款金额">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>申请退款金额:</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control dp-vd w-p50" readonly id="applyMoney" name="applyMoney" onkeyup="clearNoNum(this)" >
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" id="save" class="btn btn-primary btn_save1">${uiLabelMap.Save}</button>
                        <button type="button"   class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- 补下订单弹出框  -->
<div class="modal fade bs-example-modal-lg" id="exampleModal1" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog modal-lg"  role="dialog">
        <div class="modal-content">
        <#--<div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">添加投诉单</h4>
        </div>-->
            <div class="modal-body">
                <div class="col-sm-12">
                <#if orderItems?has_content>
                    <table class="table table-bordered table-hover">
                        <tr><th>商品名称</th><th>用户自定义名称</th><th>商品配置</th><th>数量</th><th>处理单号</th></tr>
                        <#list orderItems as oi>
                            <#assign product = delegator.findByPrimaryKey("Product",{"productId" : oi.PRODUCT_ID}) >
                            <#assign productSettingName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductSettingName(delegator,oi.PRODUCT_ID,oi.ORDER_ID,oi.ORDER_ITEM_SEQ_ID) />
                        <tr>
                            <td>${product.productName}</td>
                            <td>${oi.ITEM_DESCRIPTION}</td>
                            <td>${productSettingName}</td>
                            <td>${oi.QUANTITY}</td>
                            <td><a href="/ordermgr/control/findorders?orderId=${oi.ORDER_ID}">订单：${oi.ORDER_ID}</a></td>
                        </#list>
                    </table>
                </#if>
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
                    <#assign index = 0>
                    <#list complainItems as ci>
                        <#assign index = index +1>
                        <#assign product = delegator.findByPrimaryKey("Product",{"productId" : ci.productId }) >
                        <#assign oi = delegator.findByPrimaryKey("OrderItem",{"orderId" : ci.orderId, "orderItemSeqId" : ci.orderItemSeqId}) >
                        <#assign productSettingName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductSettingName(delegator,ci.productId,ci.orderId,ci.orderItemSeqId) />
                        <tr>
                            <td><input class="js-checkchild" type="checkbox" orderItemSeqId="${index}" value="${product.productId}"></td>
                            <td>${product.productName}</td>
                            <td>${(oi.itemDescription)!''}</td>
                            <td>${productSettingName}</td>
                            <td><input type="text"  name = "${product.productId}-${index}" class="product" size="1" orderId="${ci.orderId}" maxQuantity="${ci.quantity}" unitPrice="${oi.unitPrice}" orderItemSeqId="${ci.orderItemSeqId}" value = "${ci.quantity}" onkeyup="value=this.value.replace(/\D+/g,'')" /></td>
                        </tr>
                    </#list>
                    </tbody>
                    </table>

                </div>
                <form class="form-horizontal" id="addFirstForm1" name="addFirstForm1" method="post" action="<@ofbizUrl>addBHOrder</@ofbizUrl>">
                    <input type="hidden" name="params" id = "params"/>
                    <input type="hidden" value="${currenPartyId}" name="currenPartyId" id = "currenPartyId"/>
                    <input type="hidden" name="currenSalesShippingMethod" value="NO_SHIPPING@_NA_">
                    <input type="hidden" name="currenSalesChannelId" value="MIDCON_SALES_CHANNEL">
                    <input type="hidden" name="currenShippingContactMechId" value="">
                    <input type="hidden" name = "complainId" value="${complainHeader.complainId}" />
                    <div class="row">
                        <div class="form-group col-sm-3">
                            <label class="col-sm-12 control-label"><i class="required-mark">*</i>支付方式选择</label>
                        </div>
                        <div class="form-group col-sm-6">
                            <div class="col-sm-12">
                                <div class="radio">
                                    <#--<label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_ALIPAY" checked>支付宝</label>
                                    <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_WEIXIN" checked>微信</label>
                                    <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_UNIONPAY" checked>银联</label>
                                    <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_COD" checked>货到付款</label>-->
                                        <label class="col-sm-3"><input name="currenPaymentMethodTypeAndId" type="radio" value="EXT_WEIXIN" checked>微信</label>
                                </div>
                                <div class="dp-error-msg"></div>
                            </div>
                        </div>
                    </div>
                    <div class="row">

                        <div class="form-group col-sm-3">
                            <label class="col-sm-12 control-label"><i class="required-mark">*</i>配送方式</label>
                        </div>

                        <div class="form-group col-sm-2">
                            <select class="form-control" id="distributionMethod" name="distributionMethod">
                                <option value="ZMPS">周末配送</option>
                                <option value="GZRPS">工作日配送</option>
                                <option value="SMZT">上门自提</option>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>

                        <div class="form-group col-sm-3">
                            <label class="col-sm-12 control-label"><i class="required-mark">*</i>到货日期</label>
                        </div>

                        <div class="form-group col-sm-3" data-type="required" data-mark="到货日期"  >
                            <div class="input-group m-b-12 input-group date form_date"   data-date-format="yyyy-mm-dd" data-link-format="yyyy-mm-dd" data-link-field="deliveryDate">
                                <input class="form-control dp-vd" size="16" type="text" readonly value="">
                                <input id="deliveryDate" type="hidden" name="deliveryDate" value="">
                                <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                            <#--<p class="dp-error-msg"></p>-->
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group col-sm-3">
                            <label class="col-sm-12 control-label"><i class="required-mark">*</i>配送物流</label>
                        </div>

                        <div class="form-group col-sm-2">
                            <#assign logisticsTemples = (delegator.findByAnd("LogisticsTemple"))!''>
                            <select id="deliveryCompany" name="deliveryCompany" class="form-control input-sm">
                            <#if logisticsTemples != "">
                                <#list logisticsTemples as ltt>
                                    <#assign dc = delegator.findByPrimaryKey("LogisticsCompany",{"id" : ltt.logisticsCompanyId})>
                                    <option value="${ltt.logisticsTempleId}" >${dc.companyName}</option>
                                </#list>
                            <#else>
                                <option value="" ></option>
                            </#if>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>

                        <div class="form-group col-sm-3">
                            <label class="col-sm-12 control-label"><i class="required-mark">*</i>配送仓库</label>
                        </div>

                        <div class="form-group col-sm-3">
                        <#assign facilitys = (delegator.findByAnd("Facility"))!''>
                            <select id="facilityId" name="facilityId" class="form-control input-sm">
                            <#if facilitys != "">
                                <#list facilitys as f>
                                    <option value="${f.facilityId}" >${f.facilityName}</option>
                                </#list>
                            <#else>
                                <option value="" ></option>
                            </#if>
                            </select>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="row" style="display: none">
                        <td width="26%" align="right" valign="top"><div>${uiLabelMap.CommonCountry}</div></td>
                        <td width="5">&nbsp;</td>
                        <td width="74%">
                            <select name="countryGeoId" id="addFirstForm1_countryGeoId" >
                            ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                                <option selected="selected" value="${defaultCountryGeoId}">
                                <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
                                    ${countryGeo.get("geoName",locale)}
                                </option>
                            </select>
                            *</td>
                    </div>

                    <div class="row">
                        <div class="form-group col-sm-3">
                            <label class="col-sm-12 control-label"><i class="required-mark">*</i>收货人信息</label>
                        </div>

                        <div class="form-group col-sm-9">
                            <div class="form-group col-sm-12"  data-type="required" data-mark="收货人姓名">
                                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>收货人姓名</label>
                                <div class="col-sm-3">
                                    <input type="text" class="form-control dp-vd" id="consigneeName" name="consigneeName" value="" />
                                    <p class="dp-error-msg"></p>
                                </div><#--
                                <div class="col-sm-1">
                                    <div class="dp-tables_btn">
                                        <button id="defaultAddress" type="button" class="btn btn-primary">
                                            <i class="fa">默认收货地址</i>
                                        </button>
                                    </div>
                                </div>-->
                            </div>

                            <div class="form-group col-sm-12">
                                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>收货地区</label>
                                <div class="col-sm-3">
                                    <select class="form-control" name="stateProvinceGeoId" id="addFirstForm1_stateProvinceGeoId">
                                        <option value=""></option>
                                    </select>
                                    <p class="dp-error-msg"></p>
                                </div>
                                <div class="col-sm-3">
                                    <select class="form-control" name="city" id="addFirstForm1_cityGeoId">
                                        <option value=""></option>
                                    </select>
                                    <p class="dp-error-msg"></p>
                                </div>
                                <div class="col-sm-3">
                                    <select class="form-control" name="countyGeoId" id="addFirstForm1_countyGeoId">
                                        <option value=""></option>
                                    </select>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group col-sm-12"  data-type="required" data-mark="收货人地址">
                                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>收货人地址</label>
                                <div class="col-sm-9">
                                    <input type="text" class="form-control dp-vd" id="consigneeAddress" name="consigneeAddress" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group col-sm-12"  data-type="required" data-mark="手机号码">
                                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>手机</label>
                                <div class="col-sm-3">
                                    <input type="text" class="form-control dp-vd" id="phone" name="phone" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group col-sm-12">
                                <label  class="col-sm-3 control-label">&nbsp;&nbsp;固话</label>
                                <div class="col-sm-3">
                                    <input type="text" class="form-control" id="tel" name="tel" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group col-sm-12">
                                <label  class="col-sm-3 control-label">&nbsp;&nbsp;邮编</label>
                                <div class="col-sm-3">
                                    <input type="text" class="form-control" id="postalCode" name="postalCode" value="" />
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" id="save1" class="btn btn-primary btn_save1">${uiLabelMap.Save}</button>
                        <button type="button"   class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                    </div>

                    <div class="row" style="display:none" >
                        <div class="form-group col-sm-2">
                            <label class="col-sm-12 control-label"><i class="required-mark">*</i>支付信息</label>
                        </div>

                        <div class="form-group col-sm-10">
                            <div class="form-group col-sm-12">
                                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>原始价格</label>
                                <div class="col-sm-2">
                                    <input type="text" class="form-control" id="originalMoney" name="originalMoney" value="" readonly/>
                                    <p class="dp-error-msg"></p>
                                </div>
                                <label  class="col-sm-2 control-label">配送费用</label>
                                <div class="col-sm-2">
                                    <input type="text" class="form-control" id="distributeMoney" name="distributeMoney" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group col-sm-12" style="display: none">
                                <label class="col-sm-2 control-label">商家优惠</label>
                            <#--<div class="form-group col-sm-6 p-0">-->
                                <div class="form-group col-sm-6">
                                <#--<div class="row">
                                    <div class="col-sm-4">
                                        <div class="checkbox clearfix m-0">
                                            <label class="col-sm-12" title="活动1"><input name="mark" type="checkbox">活动1</label>
                                        </div>
                                    </div>
                                    <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" id="" name="" value="" />
                                        <p class="dp-error-msg"></p>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-sm-4">
                                        <div class="checkbox clearfix m-0">
                                            <label class="col-sm-12" title="商家优惠卷"><input name="mark" type="checkbox">商家优惠卷</label>
                                        </div>
                                    </div>
                                    <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" id="" name="" value="" />
                                        <p class="dp-error-msg"></p>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-sm-4">
                                        <div class="checkbox clearfix m-0">
                                            <label class="col-sm-12" title="活动3"><input name="mark" type="checkbox">活动3</label>
                                        </div>
                                    </div>
                                    <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" id="" name="" value="" />
                                        <p class="dp-error-msg"></p>
                                    </div>
                                </div>-->

                                    <div class="row">
                                        <div class="col-sm-4">
                                            <div class="checkbox clearfix m-0">
                                                <label class="col-sm-12" title="手动优惠"><input name="mark" type="checkbox" name="isBusinessHandDiscount" id="isBusinessHandDiscount">手动优惠</label>
                                            </div>
                                        </div>
                                        <label  class="col-sm-4 control-label">优惠</label>
                                        <div class="col-sm-4">
                                            <input type="text"  class="form-control" readonly id="businessHandDiscount" name="businessHandDiscount" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                                            <p class="dp-error-msg"></p>
                                        </div>
                                    </div>

                                    <div class="row">
                                        <div class="col-sm-4">
                                        </div>
                                        <label  class="col-sm-4 control-label">商家优惠小计</label>
                                        <div class="col-sm-4">
                                            <input type="text" class="form-control" id="businessDiscountTotal" name="businessDiscountTotal" value="" readonly/>
                                            <p class="dp-error-msg"></p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group col-sm-12"  style="display: none">
                                <label class="col-sm-2 control-label">使用积分</label>
                                <div class="form-group col-sm-6">
                                    <div class="row">
                                        <div class="col-sm-4">
                                            <input type="text" class="form-control" id="useIntegral" name="useIntegral" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                                        </div>
                                        <div  class="col-sm-3">积分 </div>
                                        <div  class="col-sm-1">抵 </div>
                                        <div class="col-sm-4">
                                            <input type="text" class="form-control" id="integralDiscount" name="integralDiscount" value="" readonly/>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        <#--<div class="form-group col-sm-12">
                            <label class="col-sm-2 control-label">免运费</label>
                            <div class="form-group col-sm-6">
                                <div class="row">
                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" id="" name="" value="" />
                                    </div>
                                </div>
                            </div>
                        </div>-->


                            <div class="form-group col-sm-12">
                                <label class="col-sm-2 control-label">平台优惠</label>
                            <#--<div class="form-group col-sm-6 p-0">-->
                                <div class="form-group col-sm-6">
                                <#--<div class="row">
                                    <div class="col-sm-4">
                                        <div class="checkbox clearfix m-0">
                                            <label class="col-sm-12" title="活动1"><input name="mark" type="checkbox">活动1</label>
                                        </div>
                                    </div>
                                    <label  class="col-sm-4 control-label"><i class="required-mark">*</i>优惠</label>
                                    <div class="col-sm-4">
                                        <input type="text" class="form-control" id="" name="" value="" />
                                        <p class="dp-error-msg"></p>
                                    </div>
                                </div>-->
                                    <div class="row">
                                        <div class="col-sm-4">
                                            <div class="checkbox clearfix m-0">
                                                <label class="col-sm-12"><input name="isPlatDiscount" id="isPlatDiscount" type="checkbox" >平台优惠券</label>
                                            </div>
                                        </div>
                                        <label  class="col-sm-4 control-label">优惠</label>
                                        <div class="col-sm-4">
                                            <input type="text" class="form-control" readonly id="platDiscount" name="platDiscount" value="" onkeyup="value=this.value.replace(/\D+/g,'')"/>
                                            <p class="dp-error-msg"></p>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-sm-4">
                                        </div>
                                        <label  class="col-sm-4 control-label">平台优惠小计</label>
                                        <div class="col-sm-4">
                                            <input type="text" class="form-control" id="platDiscountTotal" name="platDiscountTotal" value="" readonly/>
                                            <p class="dp-error-msg"></p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group col-sm-12">
                                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>优惠小计</label>
                                <div class="col-sm-2">
                                    <input type="text" class="form-control" id="discountMoney" name="discountMoney" value="" readonly/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group col-sm-12">
                                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>应付金额</label>
                                <div class="col-sm-2">
                                    <input type="text" class="form-control" id="shouldPayMoney" name="shouldPayMoney" value="" readonly/>
                                    <p class="dp-error-msg"></p>
                                </div>
                                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>获得积分</label>
                                <div class="col-sm-2">
                                    <input type="text" class="form-control" id="getIntegral" name="getIntegral" value="" readonly/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group col-sm-12">
                                <label  class="col-sm-2 control-label">实付金额</label>
                                <div class="col-sm-2">
                                    <input type="text" class="form-control" id="actualPayMoney" name="actualPayMoney" value="" onkeyup="clearNoNum(this)"/>
                                    <p class="dp-error-msg"></p>
                                </div>
                                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>未付金额</label>
                                <div class="col-sm-2">
                                    <input type="text" class="form-control" id="notPayMoney" name="notPayMoney" value="" readonly/>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>


<script>
    $(function(){
        getOrderPostalAddress();
        setInterval(calcuApplyPrice,500);
        setInterval(calcuPrice,500);
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


        $('#next').click(function(){
            $('#form').dpValidate({
                clear: true
            });
//        $('#form').submit();  //按钮在表单外

        });

        $('#form').dpValidate({
            validate: true,
            callback: function(){
                document.getElementById("form").commit();
            }
        });




        //添加一级分类提交按钮点击事件
        $('#save').click(function(){
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
                if (getRefundSelect() != ""){
                    var checks = $('#exampleModal .js-checkchild:checked');
                    var flag = true;
                    var msg = "";
                    //判断是否选中记录
                    if (checks.size() > 0) {
                        //编辑id字符串
                        checks.each(function () {
                            var maxQuantity = $("#exampleModal input[name=" + $(this).val() + "]").attr("maxQuantity");
                            //数量
                            var value = $("#exampleModal input[name=" + $(this).val() + "]").val();

                            if (value == ""){
                                msg = "数量不能为空";
                                flag = false;
                                return;
                            }else  if (parseFloat(value) === 0){
                                msg = "数量不能为0";
                                flag = false;
                                return;
                            }else if (parseFloat(value) > parseFloat(maxQuantity) ){
                                msg = "数量不能大于可退数量";
                                flag = false;
                                return;
                            }
                        });
                    }
                    if (flag){
                        $("#paramss").val(getRefundSelect());
                        $("#save").prop("disabled",true);
                        document.getElementById("addFirstForm").submit();
                    }else{
                        $("#otherLayerHiddenBox").html(msg);
                        $.otherLayer({
                            title : '提示信息',
                            content : $("#otherLayerHiddenBox")
                        });
                    }

                }else{
                $.tipLayer("请勾选一条数据");
                }
            }
        });

        $("#create").click(function(){
            if ("false" === "${isComplainCompeleted}"){
            $.tipLayer("存在流程未结束的申诉单，不能创建新的退款单");
              //  alert("存在流程未结束的申诉单，不能创建新的退款单");
            }else{
            $("#exampleModal").modal();
            }
        });
        $("#order").click(function(){
            if ("false" === "${isComplainCompeleted}"){
            $.tipLayer("存在流程未结束的申诉单，不能创建新的补货单");
                //alert("存在流程未结束的申诉单，不能创建新的补货单");
            }else{
                $("#exampleModal1").modal();
            }
        });

        $('#save1').click(function(){
            $('#addFirstForm1').dpValidate({
                clear: true
            });
            $('#addFirstForm1').submit();
        });
        //表单校验
        $('#addFirstForm1').dpValidate({
            validate: true,
            console: true,
            callback: function(){
            	var params = getSelect();
                if (params != ""){
                    var checks = $('#exampleModal1 .js-checkchild:checked');
                    var flag = true;
                    var msg = "";
                    //判断是否选中记录
                    if (checks.size() > 0) {
                        //编辑id字符串
                        checks.each(function () {
                            var orderItemSeqId =  $(this).attr("orderItemSeqId");
                            var maxQuantity = $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").attr("maxQuantity");
                            //数量
                            var value = $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").val();
                            if (value == ""){
                                msg = "数量不能为空";
                                flag = false;
                                return;
                            }else  if (parseFloat(value) === 0){
                                msg = "数量不能为0";
                                flag = false;
                                return;
                            }else if (parseFloat(value) > parseFloat(maxQuantity) ){
                                msg = "数量不能大于原始数量";
                                flag = false;
                                return;
                            }
                        });
                    }
                    if (flag){
                    	$.ajax({
					        url: "/customermgr/control/checkInventoryForBHOrder${externalKeyParam}",
					        type: "POST",
					        data:{
					        	params : params
					        },
					        dataType: "json",
					        async : false,
					        success: function (data) {
					        	if(data.map.validateFlag){
					        		$("#addFirstForm1 #params").val(params);
                                    $("#save1").prop("disabled",true);
                        			document.getElementById("addFirstForm1").submit();
					        	}else{
					        		$.otherLayer({
			                            title : '提示信息',
			                            content : data.map.msg
			                        });
					        	}
					        },
					        error: function (data) {
					            //设置提示弹出框内容
					            $('#modal_msg #modal_msg_body').html("网络异常！");
					            $('#modal_msg').modal();
					        }
					    });
                    }else{
                        $("#otherLayerHiddenBox").html(msg);
                        $.otherLayer({
                            title : '提示信息',
                            content : $("#otherLayerHiddenBox")
                        });
                    }
                }else{
                    $.otherLayer({
                        title : '提示信息',
                        content : '请选择一条数据'
                    });
                }


            }
        });

        function showNumber(num){
            $(".banner>li").eq(num).fadeIn().siblings().fadeOut();
        };
        $(".img-box").hover(function(){
            $(".number").show();
        },function(){
            $(".number").hide();
        });
        showNumber(0);
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
        })
    })

    function clearNoNum(obj){
        obj.value = obj.value.replace(/[^\d.]/g,"");  //清除“数字”和“.”以外的字符
        obj.value = obj.value.replace(/^\./g,"");  //验证第一个字符是数字而不是.
        obj.value = obj.value.replace(/\.{2,}/g,"."); //只保留第一个. 清除多余的.
        obj.value = obj.value.replace(".","$#$").replace(/\./g,"").replace("$#$",".");
    }

    function changeQuestion(obj){
        $("#complainQuestion").val($(obj).val());
    }

    function getSelect(){
        var ids = "";
        var checks = $('#exampleModal1 .js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            //编辑id字符串
            checks.each(function () {
                var orderItemSeqId =  $(this).attr("orderItemSeqId");
                var value = $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").val();
                var unitPrice =  $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").attr("unitPrice");
                var orderId = $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").attr("orderId");
                orderItemSeqId = $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").attr("orderItemSeqId");
                var facilityId = $("#exampleModal1 #facilityId").val();
                var deliveryDate = $("#exampleModal1 #deliveryDate").val();
                ids += $(this).val() + ":" +orderId + "&" + orderItemSeqId + "&" + unitPrice + "&" + value + "&" + facilityId + "&" + deliveryDate + "," ;
            });
            ids = ids.substring(0,ids.length  -1);
        }
        return ids;
    }

    function getRefundSelect(){
        var ids = "";
        var checks = $('#exampleModal .js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            //编辑id字符串
            checks.each(function () {
                //单价
                var unitPrice = $("#exampleModal input[name=" + $(this).val() + "]").attr("unitPrice");
                var orderItemSeqId = $("#exampleModal input[name=" + $(this).val() + "]").attr("orderItemSeqId");
                var maxQuantity = $("#exampleModal input[name=" + $(this).val() + "]").attr("maxQuantity");
                //数量
                var value = $("#exampleModal input[name=" + $(this).val() + "]").val();
                ids += unitPrice + ":" + value + ":" +  $(this).val() + ":" + orderItemSeqId + "," ;
            });
            ids = ids.substring(0,ids.length  -1);
        }
        return ids;
    }

    //计算申请金额
    function calcuApplyPrice(){
        var params = getRefundSelect();
        var price = 0;
        if (params != ""){
            var array = params.split(",");
            for (var i= 0; i < array.length; i ++){
                var ps = array[i].split(":");
                var unitPrice= parseFloat(ps[0]);
                var quantity  = parseFloat(ps[1]);
                price += unitPrice * quantity;
            }
        }
        var isReturnFee = '${isReturnFee}';
        if (isAllReturn() && isReturnFee === "1"){
            var distributeMoney = parseFloat('${(originalOrder.distributeMoney)!0}');
            var freeFee = parseFloat('${(originalOrder.freeFee)!0}');
            $("#logisticsMoney").val(distributeMoney - freeFee);
            $("#applyMoney").val(price + parseFloat($("#logisticsMoney").val()));
        }else{
            $("#logisticsMoney").val(0);
            $("#applyMoney").val(price);
        }

    }

    function calcuPrice(){
        var originalMoney = 0 ;
        var checks = $('#exampleModal1 .js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            //编辑id字符串
            checks.each(function () {
                var orderItemSeqId =  $(this).attr("orderItemSeqId");
                var value = $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").val();
                var unitPrice = $("#exampleModal1 input[name=" + $(this).val() +"-" +orderItemSeqId + "]").attr("unitPrice");
                originalMoney += parseFloat(unitPrice) * parseFloat(value);
            });
        }
        $("#originalMoney").val(originalMoney);
        var shouldPayMoney = originalMoney;

        var actualPayMoney = parseFloat(($("#actualPayMoney").val())?$("#actualPayMoney").val() : 0);
        $("#getIntegral").val(actualPayMoney);
//        var notPayMoney = shouldPayMoney - actualPayMoney;
        var notPayMoney = 0;
        $("#notPayMoney").val(notPayMoney);
        //补货单特殊
        $("#platDiscount").val(originalMoney);
        $("#platDiscountTotal").val(originalMoney);
        $("#discountMoney").val(originalMoney);
        $("#shouldPayMoney").val(0);
    }

    function getOrderPostalAddress(){
        $.post("<@ofbizUrl>getOrderPostalAddress</@ofbizUrl>",{orderId : "${complainHeader.orderId}"},function(data){
            if (data.postalAddress != undefined && data.postalAddress != null && data.postalAddress != ""){
                var postalAddress = data.postalAddress;
                $("#consigneeName").val(postalAddress.toName);
                $("#addFirstForm1_stateProvinceGeoId").val(postalAddress.stateProvinceGeoId).trigger('change');
                $("#addFirstForm1_cityGeoId").val(postalAddress.city).trigger('change');
                $("#addFirstForm1_countyGeoId").val(postalAddress.countyGeoId);
                $("#consigneeAddress").val(postalAddress.address1);
                $("#phone").val(postalAddress.mobilePhone);
                $("#tel").val(postalAddress.tel);
            }
        })
    }

    function isAllReturn(){
        var flag = false;
        var orderTotal = ${Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsNum(delegator,complainHeader.orderId)}
        var returnedTotal = ${Static["org.ofbiz.order.order.OrderReadHelper"].getReturnNotCancelProductNum(delegator,complainHeader.orderId)}
        var selectNum = 0;
        var checks = $('#exampleModal .js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            //编辑id字符串
            checks.each(function () {
                //数量
                var value = $("#exampleModal input[name=" + $(this).val() + "]").val();
                selectNum += parseFloat(value);
            });
        }
        if (selectNum + returnedTotal == orderTotal){
            flag = true;
        }
        return flag;
    }

</script>
<#--
${(originalOrder.distributeMoney)!0}

${(originalOrder.freeFee)!0}-->
