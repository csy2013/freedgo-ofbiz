<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/bootcss/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->

        <form id="QueryForm" class="form-inline clearfix" onsubmit="return false;" >
            <#--<div class="form-group">-->
                <#--<div class="input-group m-b-10">-->
                    <#--<span class="input-group-addon">商品名称</span>-->
                    <#--<input type="text" id="cardName" name="productName" class="form-control" value="">-->
                <#--</div>-->
            <#--</div>-->
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">赠送人</span>
                    <input type="text" id="sender" name="sender" class="form-control" value="">
                </div>
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
            <div class="input-group pull-right m-l-10">
                <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
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
                    <!-- 是否有新增权限-->
                <#--<#if security.hasEntityPermission("GPRESENT_LIST", "_CREATE", session)>-->
                    <#--<button id="btn_add" class="btn btn-primary">-->
                        <#--<i class="fa fa-plus"></i>添加-->
                    <#--</button>-->
                <#--</#if>-->
                    <!-- 是否有删除权限-->
                <#--<#if security.hasEntityPermission("GPRESENT_LIST", "_DELETE", session)>-->
                    <#--<button id="btn_del" class="btn btn-primary">-->
                        <#--<i class="fa fa-trash"></i>删除-->
                    <#--</button>-->
                <#--</#if>-->
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
    </div><!-- /.box-body -->
</div><!-- 内容end -->



<!----------------订单明细------------------->
<div class="modal fade dx-modal" id="myModal" tabindex="-1" role="dialog"   aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog" style="width:800px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close"
                        data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
            </div>
            <div class="modal-body">
                <p class="order_header"><span>订单号：<span class="js-orderId"</span></span><span>下单日期：<span class="js-orderDate"</span></span><span><#if paymentMethod != "">${(paymentMethod.get("description",locale))!""}</#if></span>
                    <span>用户：<span class="js-userName"</span></span>
                </p>
                <div class="xl_content">
                    <div class="mark-group col-6 active">
                        <span class="title">待支付</span>
                        <div class="mark-box">
                            <i class="num-mark">1</i>
                        </div>
                        <span class="time"></span>
                    </div>
                    <div class="mark-group col-6">
                        <span class="title">待收货</span>
                        <div class="mark-box">
                            <i class="num-mark">2</i>
                        </div>
                        <span class="time"></span>
                    </div>

                    <div class="mark-group col-6">
                        <span class="title">待评价</span>
                        <div class="mark-box">
                            <i class="num-mark">3</i>
                        </div>
                        <span class="time"></span>
                    </div>
                    <div class="mark-group col-6">
                        <span class="title">已完成</span>
                        <div class="mark-box">
                            <i class="num-mark">4</i>
                        </div>
                        <span class="time"</span>
                    </div>
                </div>
                <div class="pay_meg">
                    <p class="pay_head">支付信息：</p>
                    <div class="col-sm-6">
                        <span>原始金额：</span>
                        <span style="margin-left:36px" class="js-originalPrice"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>运费：</span>
                        <span class="js-shipCost"></span>
                    </div>

                    <div class="col-sm-6">
                        <span>促销类型：</span>
                        <span style="margin-left:36px" class="js-promoType"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>促销优惠：</span>
                        <span class="js-promoSale"></span>
                    </div>

                    <div class="col-sm-6">
                        <span>代金券名称：</span>
                        <span style="margin-left:36px" class="js-couponName"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>代金券优惠：</span>
                        <span class="js-couponSale"></span>
                    </div>

                    <div class="col-sm-6">
                        <span>订单使用积分：</span>
                        <span style="margin-left:36px" class="js-orderUseIntegral"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>积分优惠：</span>
                        <span class="js-integralDiscount"></span>
                    </div>


                    <div class="col-sm-6">
                        <span>应付金额：</span>
                        <span style="margin-left:36px" class="js-shouldPayMoney"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>实付金额：</span>
                        <span class="js-actualPayMoney"></span>
                    </div>


                    <div class="col-sm-6">
                        <span>支付方式：</span>
                        <span style="margin-left:36px" class="js-payMoney"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>支付流水号：</span>
                        <span class="js-referenceNum"></span>
                    </div>

                    <div class="col-sm-6">
                        <span>获得积分：</span>
                        <span style="margin-left:36px" class="js-getIntegral"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>店铺：</span>
                        <span class="js-store"></span>
                    </div>
                </div>


                <div class="pay_meg">
                    <p class="pay_head">物流信息：</p>
                    <div class="col-sm-6">
                        <span>配送方式：</span>
                        <span style="margin-left:36px" class="js-delivery"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>快递公司：</span>
                        <span class="js-expressCompany"></span>
                    </div>

                    <div class="col-sm-6">
                        <span>快递单号：</span>
                        <span style="margin-left:36px" class="js-expressNumber"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>运费：</span>
                        <span class="js-carriage"></span>
                    </div>

                    <div class="col-sm-6">
                        <span>收货地址：</span>
                        <span style="margin-left:36px" class="js-receiveAddress"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>详细地址：</span>
                        <span class="js-detailAddress"></span>
                    </div>

                    <div class="col-sm-6">
                        <span>收货人：</span>
                        <span style="margin-left:36px" class="js-receiver"></span>
                    </div>
                    <div class="col-sm-6">
                        <span>电话：</span>
                        <span class="js-tel"></span>
                    </div>
                </div>
                <div class="pay_meg">
                    <p style="margin-left:15px"><label>客户留言：</label><span class="js-customerMessage"></span></p>
                </div>

                <div class="pay_meg">
                    <p class="pay_head">发票信息：</p>
                    <p style="margin-left:15px"><label>发票类型：</label><span class="js-invoiceType"></span></p>
                </div>

                <div class="produce">
                    <p class="pay_head">商品信息</p>
                    <table width="750" border="1">
                        <thead>
                            <th>商品名称</th>
                            <th>原始价格</th>
                            <th width="20%">商品规格</th>
                            <th>数量</th>
                            <th>惠后价格</th>
                            <th>商品总价</th>
                            <th>状态</th>
                        </thead>
                        <tbody>
                        <#--<#list orderItems as oi>-->
                            <#--<#assign  curProductInfo=delegator.findByPrimaryKey("Product",{"productId" : "${oi.productId?if_exists}"})/>-->
                            <#--<#if curProductInfo?has_content>-->
                                <#--<#if curProductInfo.productTypeId=="CAR_GOOD">-->
                                    <#--<#assign originalPrice = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderProductsPrice(delegator,oi.orderId,oi.orderItemSeqId) />-->
                                    <#--<#assign productSettingName = Static["org.ofbiz.order.order.OrderReadHelper"].getProductSettingName(delegator,oi.productId,oi.orderId,oi.orderItemSeqId) />-->
                                    <#--<#assign  p =  delegator.findByPrimaryKey("Product",{"productId": oi.productId})>-->
                                    <#--<#assign  activity =  (delegator.findByPrimaryKey("ProductActivity",{"activityId": oi.activityId}))!''>-->
                                    <#--<#assign returnType = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderReturnType(delegator,order.orderId,oi.productId) />-->
                                    <#--<#assign fs = Static["org.ofbiz.order.order.OrderReadHelper"].getProductFeature(delegator,oi.productId) />-->

                                <#--<tr>-->
                                    <#--<td>-->
                                    <#--${(p.productName)!''}-->
                                    <#--</td>-->
                                <#--&lt;#&ndash;-->
                                <#--<td><#if (p.productName)?default("") != (oi.itemDescription)?default("")>${(oi.itemDescription)!''}</#if></td>-->
                                <#--&ndash;&gt;-->
                                    <#--<td>${originalPrice}</td>-->
                                    <#--<td>${oi.quantity}</td>-->
                                    <#--<td>-->
                                    <#--&lt;#&ndash;取得商品特征属性&ndash;&gt;-->
                                        <#--<#assign productFeatures = Static["org.ofbiz.product.product.ProductWorker"].getProductFeatureInfos(delegator,"${oi.productId?if_exists}")>-->

                                        <#--<div class="express_area"><p> ${productFeatures?if_exists}</p></div>-->
                                    <#--</td>-->

                                    <#--<td>${oi.unitPrice}</td>-->
                                    <#--<td>${oi.quantity * oi.unitPrice}</td>-->
                                    <#--<td>-->
                                        <#--<#assign orderStatusName = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderStatusNameByOrderType(order,"${parameters.salesOrderType?if_exists}",locale) />-->
                                        <#--${orderStatusName?if_exists}-->
                                            <#--&lt;#&ndash;<#assign status = delegator.findByPrimaryKey("StatusItem",{"statusId":oi.get("statusId")})>&ndash;&gt;-->
                                        <#--&lt;#&ndash;${(status.get("description",locale))!""}&ndash;&gt;-->
                                    <#--</td>-->
                                <#--</tr>-->
                                <#--<tr>-->
                                    <#--<td>-->
                                    <#--</td>-->
                                <#--&lt;#&ndash;-->
                                <#--<td></td>-->
                                <#--&ndash;&gt;-->
                                    <#--<td></td>-->
                                    <#--<td></td>-->
                                    <#--<td></td>-->
                                    <#--<td>-->
                                        <#--<#if activity != "">${(delegator.findByPrimaryKey("Enumeration",{"enumId":activity.activityType}).description)!''}</#if>优惠：&lt;#&ndash;${(originalPrice -  oi.unitPrice) * oi.quantity}&ndash;&gt;0元</td>-->
                                    <#--<td></td>-->
                                    <#--<td>-->
                                        <#--<#if returnType != "">-->
                                            <#--<#if returnType == "0">-->
                                                <#--<#assign  url = "findreturn?orderId=${order.orderId}&lookupFlag=Y">-->
                                            <#--<#elseif returnType == "1">-->
                                                <#--<#assign  url = "findrefund?orderId=${order.orderId}&lookupFlag=Y&returnType=1">-->
                                            <#--</#if>-->
                                            <#--<a href="<@ofbizUrl>${url}</@ofbizUrl>">退款/退货</a>-->
                                        <#--</#if>-->
                                    <#--</td>-->
                                <#--</tr>-->
                                <#--</#if>-->

                            <#--</#if>-->


                        <#--</#list>-->

                        </tbody>
                    </table>
                </div>
            </div>
            <div class="modal-footer">


            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>



<!------------------------------------------>



<!-- script区域start -->
<script>
    var data_tbl;
    var ajaxUrl = "givePresentList";
    var del_ids;
    var curQuestionIds="";

    var currOrderId=0;
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
        data_tbl = $('#data_tbl').dataTable({
            ajaxUrl: ajaxUrl,
            columns:[
                {"title":"订单编号","code":"orderId"},
                {"title":"赠送人","code":"sMobile"},
                {"title":"赠送时间","code":"sendDate"},
                {"title":"领取人","code":"rMobile"},
                {"title":"领取时间","code":"receiveDate"},
//                {"title":"商品","code":"productName"},
                {"title":"操作","code":"option",
                    "handle":function(td,record){
                        var btns = "<div class='btn-group'>"+
                                <!-- 是否都有权限-->
                                <#if security.hasEntityPermission("GIVEPRESENT_LIST", "_VIEW", session)>
                                "<button type='button' class='btn btn-danger btn-sm'  onclick='javascript:getOrder("+record.orderId+")'>查看</button>"+
                                </#if>
                                "</div>";
                        td.append(btns);
                    }
                }
            ],
            listName: "givePresentList",
            paginateEL: "paginateDiv",
            viewSizeEL: "view_size"
        });


        //查询按钮点击事件
        $('#QueryForm #searchBtn').on('click',function(){
            var sender = $('#QueryForm #sender').val();
//            var giveTimeBegin = $('#QueryForm #giveTimeBegin').val();
//            var giveTimeEnd = $('#QueryForm #giveTimeEnd').val();
            var startDate = $('#QueryForm #startDate').val();
            var endDate = $('#QueryForm #endDate').val();
            ajaxUrl = changeURLArg(ajaxUrl,"sender",sender);
//            ajaxUrl = changeURLArg(ajaxUrl,"giveTimeBegin",giveTimeBegin);
//            ajaxUrl = changeURLArg(ajaxUrl,"giveTimeEnd",giveTimeEnd);
            ajaxUrl = changeURLArg(ajaxUrl,"startDate",startDate);
            ajaxUrl = changeURLArg(ajaxUrl,"endDate",endDate);
            data_tbl.reload(ajaxUrl);
            return false;
        });
    });


    // 查看订单信息
    function getOrder(id){
        console.log(id)
        currOrderId=id;


        //清空序号
        $.ajax({
            url: "getOrderInfoById",
            type: "POST",
            data : {orderId:id
            },
            dataType : "json",
            success: function(data){
                if(data){

                    console.log(data.orderId);
                }

//                if(data){
//                    var questionId=data.questionId;
//                    var question=data.question;
//                    var questionType=data.questionType;
//                    var answerResult=data.answerResult;
//                    var answerList=data.answerList;
//
//
//                }

            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
        //设置提示弹出框内容

        $('#myModal' ).modal();
    }

</script><!-- script区域end -->