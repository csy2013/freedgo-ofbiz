<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<#assign return = delegator.findByAnd("ReturnItem",{"returnId": requestParameters.returnId})[0] />
<#assign returns = delegator.findByAnd("ReturnItem",{"returnId": requestParameters.returnId}) />
<#assign returnReasonEnum = delegator.findByAnd("Enumeration",{"enumId": return.returnReasonId})[0] />

<#-- 应付金额以后会用到 <#assign orderItem = delegator.findByAnd("OrderItem",{"orderId": return.orderId,"productId": return.productId})[0] />-->
<#assign product = delegator.findByAnd("ProductAndPriceView",{"productId":return.productId,"productPriceTypeId":"DEFAULT_PRICE"})[0] />
<#assign info = (Static["org.ofbiz.order.order.OrderReadHelper"].getOrderPurchaseInfo(delegator,return.orderId))!'' />
<#assign businessName = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderBusinessName(delegator,return.orderId) />
<#assign returnTickets = delegator.findByAnd("ReturnTicket",{"returnId":return.returnId}) />
<#assign orderPaymentPreferences =  delegator.findByAnd("OrderPaymentPreference",{"orderId":return.orderId}) />
<#assign orderItem = delegator.findByAnd("OrderItem",{"orderId":return.orderId})[0]>
<#assign orderHeader = delegator.findByPrimaryKey("OrderHeader",{"orderId":return.orderId})>
<#assign balance = (orderHeader.balance)!'0'>
<#if  orderPaymentPreferences?has_content>
    <#assign  orderPaymentPreference = orderPaymentPreferences[0]>
    <#assign  paymentMethod = delegator.findByPrimaryKey("PaymentMethodType",{"paymentMethodTypeId":orderPaymentPreference.get("paymentMethodTypeId")})>
<#else>
    <#assign  paymentMethod = "">
</#if>
<#assign paymentGatewayResponse =  (delegator.findByAnd("PaymentGatewayResponse",{"orderPaymentPreferenceId":orderPaymentPreference.orderPaymentPreferenceId})[0])!'' />
<#assign applyMoney = return.applyMoney />
<#assign  integralDiscount = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":orderHeader.orderId,"attrName" : "integralDiscount"}).attrValue?number)!0>
<#assign integralPerMoney = (delegator.findByPrimaryKey("PartyIntegralSet",{"partyIntegralSetId" : "PARTY_INTEGRAL_SET"}).integralValue)!0 >
<#assign integralDiscount = integralDiscount*integralPerMoney >

<#if orderItem.lastUnitPrice?default(0) != 0>
    <#assign unitPrice = orderItem.lastUnitPrice>
<#else>
    <#assign unitPrice = orderItem.unitPrice>
</#if>
<#assign num = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsNum(delegator,orderHeader.orderId) />
<#assign originalPrice = unitPrice*num /> <!-- 原始金额 -->
<#assign  integralMoney = (delegator.findByPrimaryKey("OrderAttribute",{"orderId":orderHeader.orderId,"attrName" : "integralDiscount"}).attrValue?number)!0 >
<#assign  shouldPayMoney = (originalPrice - integralMoney)!0 >
<#assign shouldReturnMoney = (shouldPayMoney / originalPrice) * unitPrice >

<#assign shouldReturnIntegral = 0 > <!-- 应退积分 -->
<#assign productActivityGoods = (delegator.findByPrimaryKey("ProductActivityGoods", {"activityId": orderItem.activityId, "productId": orderItem.productId}))!''>
<#if productActivityGoods?has_content && productActivityGoods.isSupportReturnScore == "Y" && orderHeader.statusId != "ORDER_WAITRECEIVE">
   <#assign shouldReturnIntegral = (orderHeader.useIntegral / orderItem.quantity) * return.returnQuantity >
</#if>
<div class="box box-info">
    <p class = "del">
        <#if returnType?default("0") == "1">
            退款单处理 &nbsp;&nbsp;&nbsp;&nbsp;<a  class="btn btn-primary pull-right"  href ="<@ofbizUrl>findrefund</@ofbizUrl>">返回</a>
        <#else>
            退货单处理 &nbsp;&nbsp;&nbsp;&nbsp;<a  class="btn btn-primary pull-right"  href ="<@ofbizUrl>findreturn</@ofbizUrl>">返回</a>
        </#if>
    </p>

    <table class="basic-info" border="1">
        <tr class="t_head"><th colspan="2">基本信息</th><th>支付信息</th></tr>
        <tr>
            <td>
                <div class="refund-msg">
                    <p><label>退单号&nbsp;:</label><span>${return.returnId}</span></p>
                    <p><label>订单号&nbsp;:</label><span>${return.orderId}</span></p>
                    <p><label>用户名&nbsp;:</label><span>${(info.name)!''}</span></p>
                    <p><label>收货人&nbsp;:</label><span>${(info.toName)!''}</span></p>
                    <p><label>手机号&nbsp;:</label><span>${(info.mobilePhone)!''}</span></p>
                    <p><label>商家&nbsp;:</label><span>${businessName}</span></p>
                    <#--<p><label>申请凭证&nbsp;:</label><span>发票</span></p>-->
                    <p><label>申请退款金额&nbsp;:</label><span>${(return.applyMoney)!''}</span></p>
                    <#if returnType?default("0") == "0">
                        <p><label>商品返回方式&nbsp;:</label><span>快递</span></p>
                    </#if>
                </div>
            </td>
            <td width="500px">
                <div class="refund-msg">
                    <p style="margin-bottom:10px;">退货原因&nbsp;:${returnReasonEnum.description}</p>
                    <p style="margin-bottom:10px;">
                        <label>
                    <#if returnType?default("0") == "1">
                        退款说明
                    <#else>
                        退货说明
                    </#if>&nbsp;:</label><span>${(return.returnReason)!''}</span>
                    </p>
                    <p>凭证&nbsp;:</p>
                    <div class="img-box">
                        <ul class="banner">
                            <#assign contentIds = Static["org.ofbiz.order.order.OrderReadHelper"].getRturnImagesContentIds(delegator,requestParameters.returnId) />
                            <#list contentIds as contentId>
                                <li><img src="/content/control/stream?contentId=${contentId}"></li>
                            </#list>
                        </ul>

                        <div class="number">
                            <a href="javascript:;" class="glyphicon glyphicon-chevron-left icon1"></a>
                            <a href="javaScript:;" class="glyphicon glyphicon-chevron-right icon2"></a>
                        </div>
                    </div>
                </div>
            </td>
            <td>
                <div class="refund-msg">
                    
                    <input id="payMethod" value = "${(orderPaymentPreference.get("paymentMethodTypeId"))!''}" type="hidden">
                    <p><label>第三方支付方式&nbsp;:</label ><span ><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></span></p>
                    <p><label>支付金额&nbsp;:</label><span>${(orderPaymentPreference.maxAmount)!''}元</span></p>
                    <p><label>支付流水号&nbsp;:</label><span><#if paymentGatewayResponse!= ""> ${(paymentGatewayResponse.referenceNum)!''} </#if> </span></p>
                    <p><label>余额支付&nbsp;:</label><span>${balance}</span></p>
                    <p><label>使用积分&nbsp;:</label><span>${integralDiscount}</span></p>
                </div>
            </td>
        </tr>
    </table><!--基本信息表结束-->
    <!--商品表-->
    <table border="1" class="table-hover each-product">
        <tr><th>商品名称</th><th>商品编码</th><th>数量</th><th>销售单价</th><th>应退金额</th><th>应退积分</th><th>应退合计</th><#--<#if returnTickets?has_content><th>验证码</th></#if>--></tr>

            <#if returns?has_content>
                <#list returns as rt>
                    <#--<#assign ticket = delegator.findByPrimaryKey("Ticket",{"ticketId" : rt.ticketId}) />-->
                    <#--<#if rt_index == 0>-->
                    <tr>
                        <td>${product.productName}</td>
                        <td>${product.productId}</td>
                        <td>${return.returnQuantity}</td>
                        <td>${orderItem.unitPrice}</td>
                        <td>${orderItem.realPrice?string("#.##")}</td>
                        <td>${shouldReturnIntegral?string("#")}</td>
                        <td>${orderItem.realPrice?string("#.##")}</td>
                        <#--<td>${ticket.ticketNo}</td>-->
                    </tr>
                    <#--<#else>
                    <tr>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td>${ticket.ticketNo}</td>
                    </tr>
                    </#if>-->
                </#list>
            <#else>
            <tr>
                <td>${product.productName}</td>
                <td>${product.productId}</td>
                <td>${return.returnQuantity}</td>
                <td>${unitPrice}</td>
                <td>${return.realPrice?string("#.##")}</td>
                <td>${shouldReturnIntegral?string("#")}</td>
                <td>${(shouldReturnMoney * return.returnQuantity)?string("#.##")}</td>
            </tr>
            </#if>
        </tr>

    </table>
    <!--操作日志开始-->
    <div class="operate">
        <div class="operate-head">操作日志</div>
        <div class="operate-container clearfix">
            <div class="span-left">
                <#list logList as log>
                    <p>${log.operateTime?string("yyyy-MM-dd HH:mm:ss")}</p>
                </#list>
            </div>
            <div class="span-right">
                <#list logList as log>
                    <p>${log.operateType}&nbsp;(&nbsp;操作：<i>${log.operator}</i>&nbsp;)
                        <#if log.operateReason?has_content>
                            &nbsp;&nbsp;备注：${(log.operateReason)!''}
                        </#if>

                    </p>
                </#list>
            </div>
        </div>

    </div><!--操作日志结束-->

<#if returnType?default("0") == "0" && return.statusId != "RETURN_REJECTAPPLY" && return.statusId != "RETURN_WAITEXAMINE">
    <!--物流信息-->
    <div class="logist">
        <div class="logist-head">物流信息</div>
        <div class="logist-container">
            <p><span>物流名称&nbsp;:<i><#--申通--></i></span><span class="logist-num">物流单号&nbsp;:<i><#--11111--></i></span></p>
            <p style="font-weight:700;">物流状态</p>
          <#--  <p>2015-12-08周二13:20:20您的订单已入库</p>
            <p>14:32:45商品已打包</p>
            <p>14:32:45商品已出库</p>
            <p>14:32:45商品正通知快递公司接件</p>-->
        </div>
    </div>
</#if>
    <#if requestParameters.statusId = "RETURN_WAITEXAMINE">

        <div class="refund-money">
            <div class="logist-head">
                <#if returnType?default("0") == "1">
                    退款审核
                <#else>
                    退货审核
                </#if>
            </div>
            <form id="form" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>updateReturn</@ofbizUrl>">
                <input name="returnType" value = "${(requestParameters.returnType)!''}" type="hidden">
                <input name="returnId" value = "${return.returnId}" type="hidden">
                <input name="orderId" value="${return.orderId}" type="hidden">
                <div class="form-group">
                    <div class="col-sm-4">
                        <#if returnType?default("0") == "1">
                            &nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="operateType" value="同意退款" checked/>&nbsp;&nbsp;同意退款&nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="radio" name="operateType" value="拒绝退款" />&nbsp;&nbsp;拒绝退款
                        <#else>
                            &nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="operateType" value="同意退货" checked/>&nbsp;&nbsp;同意退货&nbsp;&nbsp;&nbsp;&nbsp;
                            <input type="radio" name="operateType" value="拒绝退货" />&nbsp;&nbsp;拒绝退货
                        </#if>
                    </div>
                </div>
                <div class="form-group" data-type="required" data-mark="操作原因">
                    <div class="col-sm-4">
                        <textarea id="operateReason" name="operateReason" class="form-control dp-vd" rows="3" style="resize: none;"></textarea>
                        <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-2">
                    </div>
                    <div class="col-sm-4">
                            <button id="next" class="btn btn-primary">
                                <i class="fa">确定</i>
                            </button>
                    </div>
                </div>
            </form>
        </div>
    <#elseif requestParameters.statusId = "RETURN_WAITRECEIVE">
    <div class="refund-money">
        <div class="logist-head">收货确认</div>
        <form id="form" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>updateReturn</@ofbizUrl>">
            <input name="returnId" value = "${return.returnId}" type="hidden">
            <input name="orderId" value="${return.orderId}" type="hidden">
            <input name="returnType" value = "${(requestParameters.returnType)!''}" type="hidden">
            <div class="form-group">
                <div class="col-sm-4">
                    &nbsp;&nbsp;&nbsp;&nbsp;<input type="radio" name="operateType" value="同意收货" checked/>&nbsp;&nbsp;同意收货&nbsp;&nbsp;&nbsp;&nbsp;
                    <input type="radio" name="operateType" value="拒绝收货" />&nbsp;&nbsp;拒绝收货
                </div>
            </div>
            <div class="form-group" data-type="required" data-mark="操作原因">
                <div class="col-sm-4">
                    <textarea id="operateReason" name="operateReason" class="form-control dp-vd" rows="3" style="resize: none;"></textarea>
                    <p class="dp-error-msg"></p>
                </div>
                <div class="col-sm-2">
                </div>
                <div class="col-sm-3">
                    <div class="dp-tables_btn">
                        <button id="next" class="btn btn-primary">
                            <i class="fa">确定</i>
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
    <#elseif requestParameters.statusId = "RETURN_WAITFEFUND">
        <div class="refund-money">
            <div class="logist-head">退款</div>
            <form id="form" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>updateReturn</@ofbizUrl>">
                <input name="returnId" value = "${return.returnId}" type="hidden">
                <input name="returnType" value = "${(requestParameters.returnType)!''}" type="hidden">
                <input name="orderId" value="${return.orderId}" type="hidden">
                <div class="form-group" data-type="required" data-mark="实退金额" style="margin-top: 5px">
                    <label for="currenPartyId" class="col-sm-1 control-label"><i class="required-mark">*</i>实退金额</label>
                    <div class="col-sm-3">
                        <input type="text" class="form-control dp-vd"  name="actualPaymentMoney" id="actualPaymentMoney" onkeyup="clearNoNum(this)" value="">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group" data-type="required" data-mark="操作原因">
                    <div class="col-sm-4">
                        <textarea id="operateReason" name="operateReason" class="form-control dp-vd" rows="3" style="resize: none;"></textarea>
                        <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-2">
                    </div>
                    <div class="col-sm-2">
                        <div class="dp-tables_btn">
                            <button id="next" class="btn btn-primary">
                                <i class="fa">确定</i>
                            </button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </#if>


</div>

<script>
    $(function(){
       
        $('#next').click(function(){
            $('#form').dpValidate({
                clear: true
            })
//        $('#form').submit();  //按钮在表单外
        });

        $('#form').dpValidate({
            validate: true,
            callback: function(){
                $("#form #next").prop("disabled",true);
                var shouldPayMoney = parseFloat("${shouldPayMoney}");
                var applyMoney = parseFloat("${applyMoney}");
                var money = 0;
                if (shouldPayMoney <= applyMoney){
                    money = shouldPayMoney;
                }else{
                    money = applyMoney;
                }
                var actualPaymentMoney = parseFloat($("#actualPaymentMoney").val());
                var statusId =  "${requestParameters.statusId}";
                if (statusId == "RETURN_WAITFEFUND" && (actualPaymentMoney > money)){
                    alert("实付金额应小于应退金额");
                    $("#form #next").prop("disabled",false);
                }else{
                    var payMethod= $("#payMethod").val();
			        if(payMethod=="EXT_ALIPAY" && "${return.statusId}" === "RETURN_WAITFEFUND"){
			           document.getElementById('form').action="<@ofbizUrl>refundAlipay</@ofbizUrl>";
			        } else {
			           document.getElementById('form').action="<@ofbizUrl>updateReturn</@ofbizUrl>";
			        } 
                    document.getElementById('form').submit();
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


</script>