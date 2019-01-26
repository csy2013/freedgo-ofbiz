<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">验证码核销</h3>
    </div>
    <div class="box-body">
        <form class="form-inline clearfix" id="ticketForm" role="form" method="post" action="<@ofbizUrl>ticketValidate</@ofbizUrl>" onsubmit=" $('#ticketForm').dpValidate({clear: true})">
            <div class="form-group" data-type="required" data-mark="验证码">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">验证码</span>
                    <input type="text" class="form-control input-lg dp-vd" name="ticketNo" size="60"  placeholder="请输入12位验证码" value="<#if ticket?exists && ticket.ticketNo?exists>${ticket.ticketNo?default("")}</#if>">
                </div>
                <div class="input-group pull-right">
                <#if security.hasEntityPermission("TICKET_VALIDATOR", "", session)>
                    <button class="btn btn-success btn-flat btn-lg" id="validateBtn">核销</button>
                </#if>
                </div>
                <p class="dp-error-msg"></p>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
        <#if dealResult?exists>
            <#if dealResult=='success' && ticket?exists && ticket.ticketNo?exists>
                <div class="alert alert-success alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <h4><i class="icon fa fa-check"></i> 核销成功!</h4>
                    <#if ticket?exists && ticket.ticketNo?exists>
                         <#--
                         ${ticket.ticketNo}
                         -->
                         <#--Add by zhajh at 20160318 bug 2077  验证码核销时，提示信息增加活动名称及商品名称  Begin-->
                         <#assign curOrderId=ticket.orderId>
                         <#assign curProductId=ticket.productId>
                         <#assign curProduct = delegator.findByPrimaryKey("Product", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", curProductId))>
                       
                         <#assign curOrderItems = delegator.findByAnd("OrderItem", {"orderId" : curOrderId})>
						 <#if curOrderItems?has_content>
							<#assign curOrderItem = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(curOrderItems) />
							<#assign curProductActivity = delegator.findByPrimaryKey("ProductActivity", Static["org.ofbiz.base.util.UtilMisc"].toMap("activityId", curOrderItem.activityId))>
						 </#if>
						 <#if curProductActivity?has_content>
						         活动名称：${curProductActivity.activityName?if_exists} 
						 </#if>
						 <#if curProduct?has_content>
						    <br>
                                                                 商品名称：${curProduct.productName?if_exists}
                         </#if>
                         <#--Add by zhajh at 20160318 bug 2077  验证码核销时，提示信息增加活动名称及商品名称  End-->
                    </#if>
                </div>
            <#elseif dealResult!='success' && ticket?exists && ticket.ticketNo?exists>
                <div class="alert alert-warning alert-dismissable">
                    <button type="button" class="close" data-dismiss="alert" aria-hidden="true">×</button>
                    <h4><i class="icon fa fa-warning"></i>
                        核销失败
                    </h4>
                    <#if dealResult =='notFind'> 找不到该验证码,请再次输入!<#else></#if>
                    <#if dealResult =='hasUsed'> 该验证码已于<#if ticket.useDate?has_content>${ticket.useDate?string("yyyy-MM-dd HH:mm:ss")}</#if>核销使用!<#else></#if>
                    <#if dealResult =='notAudited'> 该验证码未审核,请再次输入!<#else></#if>
                    <#if dealResult =='notRefunded'> 该验证码已于${ticket.lastUpdatedStamp?string("yyyy-MM-dd HH:mm:ss")}申请退款!<#else></#if>
                    <#if dealResult =='hasRefuned'> 该验证码已于${ticket.lastUpdatedStamp?string("yyyy-MM-dd HH:mm:ss")}退款成功!<#else></#if>
                    <#if dealResult =='rejectApplication'> 该验证码拒绝申请,请再次输入!<#else></#if>
                    <#if dealResult =='expired'> 该验证码已经失效,请再次输入!<#else></#if>
                </div>
            </#if>

        </#if>
    <#if ticketList?has_content>
        <div class="row">
            <div class="col-sm-6">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox">全选 <button class="btn btn-primary" name="dealbtn" onclick="doAllValidate();">核销</th>
                        <th>验证码</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list ticketList as ticketRow>
                        <tr>
                            <td><input class="js-checkchild" type="checkbox" value="${ticketRow.ticketNo}">
                            </td>
                            <td><#if ticketRow.containsKey("ticketNo")>${ticketRow.ticketNo?default("N/A")}</#if></td>

                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    <#else>

    </#if>
    </div>

    <!-- 提示弹出框start -->
    <div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="modal_msg_title">操作提示</h4>
                </div>
                <div class="modal-body">
                    <h4 id="modal_msg_body"></h4>
                </div>
                <div class="modal-footer">
                    <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
                </div>
            </div>
        </div>
    </div>
    <!-- 提示弹出框end -->

    <script type="text/javascript">
        /*$("#validateBtn").click(function(){
            $('#ticketForm').dpValidate({
                clear: true
            });
        });*/
        $(function(){
            $('#ticketForm').dpValidate({
                validate: true,
                clear: true,
                callback: function () {
                    var ticketNo = $("input[name='ticketNo']").val();
//                    if( /^\d{12}$/.test(ticketNo)) {
                        document.getElementById("ticketForm").submit();
//                    }else{
//                        alert('核销号格式不正确，请输入12位数字')
//                    }
                }
            })
        });




        function doAllValidate(){
            $('#ticketForm').dpValidate({clear: true});
            var checks = $('.js-checkparent .js-checkchild:checked');

            //判断是否选中记录
                if(checks.size() > 0 ){
                    deal_ids = "";
                    //编辑id字符串
                    checks.each(function(){
                        deal_ids += $(this).val() + ",";
                    });
                    deal_ids=deal_ids.substring(0,deal_ids.length-1);
                    console.log(deal_ids)
                    $.ajax({
                        url: "ticketListValidate",
                        type: "POST",
                        data: {ticketNos : deal_ids},
                        dataType : "json",
                        success: function(data){
                            if(data.dealAllResult == 'success'){
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                $('#modal_msg').modal();
                                $('#modal_msg').off('hide.bs.modal');
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.href='ticketValidate';
                                })
                            }else{
                                //设置删除弹出框内容
                                $('#modal_confirm #modal_msg_body').html("操作失败");
                                $('#modal_confirm').modal('show');
                                $('#modal_msg').off('hide.bs.modal');
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.href='ticketValidate';
                                })
                            }
                        },
                        error: function(data){
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作失败");
                            $('#modal_msg').modal();
                            $('#modal_msg').off('hide.bs.modal');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.href='ticketValidate';
                            })
                        }
                    });
                }else{
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("请选择其中一个");
                    $('#modal_msg').modal();

             }
        }

     </script>

