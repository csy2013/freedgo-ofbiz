<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<!-- Date Picker -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<!-- Daterange picker -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker-bs3.css</@ofbizContentUrl>">
<!-- daterangepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/moment.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker.js</@ofbizContentUrl>"></script>
<!-- datetimepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>

<#assign commonUrl = "partyConsumption?lookupFlag=Y"+ paramList +"&">

<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.css</@ofbizContentUrl>">
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">会员消费查询</h3>
    </div>
    <div class="box-body">
        <form class="form-inline clearfix" role="form" method="post" action="<@ofbizUrl>partyConsumption</@ofbizUrl>">
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">用户名</span>
                    <input type="text" class="form-control" name="userLoginId" placeholder="用户名" value="${userLoginId?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">会员昵称</span>
                    <input type="text" class="form-control" name="nickname" placeholder="会员昵称" value="${nickname?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">手机号</span>
                    <input type="text" class="form-control" name="mobile" placeholder="手机号" value="${mobile?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单号</span>
                    <input type="text" class="form-control" name="orderId" placeholder="订单号" value="${orderId?default("")}">
                </div>
                
                <div class="input-group m-b-10">
                    <span class="input-group-addon">开始时间</span>
                    <div class="input-group date form_datetime col-sm-10 " data-link-field="startDate">
                        <input class="form-control" size="16" type="text" readonly value="${startDate?default("")}">
                        <input id="startDate" class="dp-vd" type="hidden" name="startDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">结束时间</span>

                    <div class="input-group date form_datetime col-sm-10 " data-link-field="endDate">
                        <input class="form-control" size="16" type="text" readonly value="${endDate?default("")}">
                        <input id="endDate" class="dp-vd" type="hidden" name="endDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                </div>
            </div>
            <div class="input-group pull-right">
            <#if security.hasEntityPermission("PRODPROMO_GROUPORDER", "_VIEW", session)>
                <button class="btn btn-success btn-flat">${uiLabelMap.CommonView}</button></#if>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
        
        <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
        <div class="row m-b-10">
        <div class="col-sm-6">
	      </div><!-- 操作按钮组end -->
        <#if partyConsumptionList?has_content>

            <div class="col-sm-6">
                <div class="dp-tables_length">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                onchange="location.href='${commonUrl1}VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                            <option value="10" <#if viewSize ==10>selected</#if>>10</option>
                            <option value="20" <#if viewSize==20>selected</#if>>20</option>
                            <option value="30" <#if viewSize==30>selected</#if>>30</option>
                            <option value="40" <#if viewSize==40>selected</#if>>40</option>
                        </select>
                        条
                    </label>
                </div>

            </div>
        </#if>
        </div>
    <#if partyConsumptionList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th>会员编码
                            <#if orderFiled?default("") =='party_id'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=party_id&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=party_id&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=party_id&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>用户名
                            <#if orderFiled?default("") =='user_login_id'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=user_login_id&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=user_login_id&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=user_login_id&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>会员昵称
                            <#if orderFiled?default("") =='nickname'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=nickname&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=nickname&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=nickname&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>手机号
                            <#if orderFiled?default("") =='mobile'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=mobile&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=mobile&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=mobile&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>订单号
                            <#if orderFiled?default("") =='orderId'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=orderId&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=orderId&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityEndDate&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>退单号
                            <#if orderFiled?default("") =='returnId'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=returnId&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=returnId&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=returnId&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>金额
                            <#if orderFiled?default("") =='totalmount'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=totalmount&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=totalmount&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=totalmount&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>支付方式
                        <!--
                            <#if orderFiled?default("") =='hasGroup'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=hasGroup&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=hasGroup&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=hasGroup&amp;ORDER_BY=ASC"></a>
                            </#if>-->
                        </th>

                        <th>消费/退款时间
                            <#if orderFiled?default("") =='createTime'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=createTime&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=createTime&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=createTime&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list partyConsumptionList as partyConsumption>
                        <tr>
                            <td><#if partyConsumption.containsKey("party_id")>${partyConsumption.party_id?default("")}</#if></td>
                            <td><#if partyConsumption.containsKey("user_login_id")>${partyConsumption.user_login_id?default("")}</#if></td>
                            <td><#if partyConsumption.containsKey("nickname")>${partyConsumption.nickname?default("")}</#if></td>
                            <td><#if partyConsumption.containsKey("mobile")>${partyConsumption.mobile?default("")}</#if></td>
                            <td><#if partyConsumption.containsKey("orderId")>${partyConsumption.orderId?default("")}</#if></td>
                            <td><#if partyConsumption.containsKey("returnId")>
                            <#if partyConsumption.returnId!=partyConsumption.orderId>
                             ${partyConsumption.returnId?default("")}
                            </#if>
                            </#if></td>
                            <td><#if partyConsumption.containsKey("totalmount")>${partyConsumption.totalmount?default("0.00")}</#if></td>
                            <td>
                            <#assign orderPaymentPreferences =  delegator.findByAnd("OrderPaymentPreference",{"orderId":partyConsumption.orderId}) />
                            <#if  orderPaymentPreferences?has_content>
                                <#assign  orderPaymentPreference = orderPaymentPreferences[0]>
                                <#assign  paymentMethod = delegator.findByPrimaryKey("PaymentMethodType",{"paymentMethodTypeId":orderPaymentPreference.get("paymentMethodTypeId")})>
                            <#else>
                                <#assign  paymentMethod = "">
                            </#if>
                            <#if paymentMethod != "" &&partyConsumption.returnId==partyConsumption.orderId>${(paymentMethod.get("description",locale))?default(" ")}</#if>
                            </td>
                            <td><#if partyConsumption.containsKey("createTime")>${partyConsumption.createTime?string("yyyy-MM-dd HH:mm:ss")}</#if></td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>


    <#--<#if viewIndex ==-->

        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(partyConsumptionListSize, viewSize) />

        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", partyConsumptionListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=partyConsumptionListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
    <#else>
        <div class="row">
            <div class="col-sm-10">
                <h3>查询无数据</h3>
            </div>
        </div>
    </#if>
        <#--<div class="">
            <button class="btn btn-default" onclick="imageManage()">图片处理</button>
            <script type="text/javascript">
                function imageManage() {
                    $.chooseImage.int({
                        userId: 3446,
                        serverChooseNum: 5,
                        getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
                        submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
                        submitServerImgUrl: '',
                        submitNetworkImgUrl: ''
                    });
                    $.chooseImage.show();
                }
                $(function(){
                   $('body').on('click','.img-submit-btn',function(){
                       var obj = $.chooseImage.getImgData();
                       $.chooseImage.choose(obj,function(data){
                          console.log(data)
                       })
                   })
                });
            </script>
        </div>-->
    </div>
    <!-- /.box-body -->
</div>

<#--<div>-->
    <#--<form action="/content/control/uploadFile" method="post" enctype="multipart/form-data">-->

        <#--<input type="file" name="uploadedFile1"/>-->
        <#--<input type="hidden" name="type" value="product">-->
        <#--<input type="hidden" name="productId" value="10000-001">-->
        <#--<input type="file" name="uploadedFile2"/>-->
        <#--<input type="submit" value="上传"/>-->
    <#--</form>-->
<#--</div>-->


<!-- 提示弹出框start -->
<div id="modal_audit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_audit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">团购审批</h4>
            </div>
            <div class="modal-body">
                <input type="hidden" name="auditactivityId" id="auditactivityId">
                <div class="box-body">
                    <div class="row">
                        <div class="form-group col-sm-6">
                            <label for="title" class="col-sm-5 control-label">审批结果:</label>

                            <div class="col-sm-7" id="">
                                <label class="labe">
                                    <input type="radio" name="auditResult" value="N"/>审批不通过
                                </label>
                                <label class="labe">
                                    <input type="radio" name="auditResult" value="Y"/>审批通过
                                </label>
                            </div>
                        </div>
                    </div>


                </div>
                <div class="modal-footer">
                    <button id="ok" type="button" class="btn btn-primary" onclick="doAudit();" data-dismiss="modal">确定</button>
                </div>
            </div>
        </div>
    </div>
    </div>
    <!-- 提示弹出框end -->

    <!-- edit user Modal -->
    <div id="modal_detail" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modal_detail_title">>
        <div class="modal-dialog" style="width: 800px;">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <div class="text-center">
                    <#--<button type="button" id="aduitBtn" class="btn btn-primary" onclick="audit();">审批</button>-->

                    </div>
                </div>
                <div class="modal-body">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">团购活动基本信息</h3>
                        </div>
                        <div class="box-body">
                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="title" class="col-sm-5 control-label">使用店铺:</label>

                                    <div class="col-sm-7" id="d_productStoreName"></div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="title" class="col-sm-5 control-label">活动类型:</label>

                                    <div class="col-sm-7" id="d_activityTypeName">

                                    </div>
                                </div>
                                <div class="form-group col-sm-6">
                                    <label for="subTitle" class="col-sm-5 control-label">活动状态:</label>

                                    <div class="col-sm-7" id="d_activityAuditStatusName">

                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="form-group col-sm-6" data-type="required" data-mark="活动编码">
                                    <label for="title" class="col-sm-5 control-label"><i class="required-mark">*</i>活动编码:</label>

                                    <div class="col-sm-7" id="d_activityCode">

                                    </div>
                                </div>
                                <div class="form-group col-sm-6" data-type="required" data-mark="活动名称">
                                    <label for="subTitle" class="col-sm-5 control-label"><i class="required-mark">*</i>活动名称:</label>

                                    <div class="col-sm-7" id="d_activityName">


                                    </div>
                                </div>
                            </div>


                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="publishDate" class="col-sm-5 control-label"><i class="required-mark">*</i>发布时间:</label>

                                    <div class="col-sm-7" id="d_publishDate">

                                    </div>
                                </div>

                                <div id="endGroup" class="form-group col-sm-6">
                                    <label for="endDate" class="col-sm-5 control-label">下架时间:</label>

                                    <div class="col-sm-7" id="d_endDate">

                                    </div>
                                </div>
                            </div>


                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="startTime" class="col-sm-5 control-label">销售开始时间:</label>

                                    <div class="col-sm-7" id="d_activityStartDate">

                                    </div>
                                </div>
                                <div id="endTimeGroup" class="form-group col-sm-6">
                                    <label for="endTime" class="col-sm-5 control-label">销售结束时间:</label>

                                    <div class="col-sm-7" id="d_activityEndDate">

                                    </div>

                                </div>
                            </div>


                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="title" class="col-sm-5 control-label">单个ID限购数量:</label>

                                    <div class="col-sm-7" id="d_limitQuantity">
                                    </div>
                                </div>
                                <div class="form-group col-sm-6">
                                    <label for="subTitle" class="col-sm-5 control-label">活动总数量:</label>

                                    <div class="col-sm-7" id="d_activityQuantity">
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="title" class="col-sm-5 control-label">团购商品:</label>

                                    <div class="col-sm-7" id="d_productName">
                                    </div>

                                </div>

                                <div class="form-group col-sm-6" id="d_shipmentTypeDiv">
                                    <label for="subTitle" class="col-sm-5 control-label"><i class="required-mark">*</i>配送方式:</label>

                                    <div class="col-sm-7" id="d_shipmentTypeName">

                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="title" class="col-sm-5 control-label">最多可抵扣的积分:</label>

                                    <div class="col-sm-7" id="d_scoreValue">
                                    </div>

                                </div>

                                <div class="form-group col-sm-6">
                                    <label for="subTitle" class="col-sm-5 control-label">参团支付条件:</label>

                                    <div class="col-sm-6" id="d_activityPayTypeName">

                                    </div>
                                    <div class="col-sm-3" id="d_productPrice">
                                    </div>
                                </div>
                            </div>

                            <div class="row" id="virtualProductDateDiv">
                                <div class="form-group col-sm-6">
                                    <label for="virtualStartTime" class="col-sm-5 control-label"><i class="required-mark">*</i>虚拟商品有效期自:</label>

                                    <div class="col-sm-7" id="d_virtualProductStartDate"></div>
                                </div>
                                <div id="virtualEndTimeGroup" class="form-group col-sm-6">
                                    <label for="endTime" class="col-sm-5 control-label"><i class="required-mark">*</i>虚拟商品有效期至:</label>

                                    <div class="col-sm-7" id="d_virtualProductEndDate"></div>
                                </div>
                            </div>


                            <div class="row">
                                <div class="form-group col-sm-12">
                                    <label class="col-sm-2 control-label">团购选项:</label>

                                    <div class="col-sm-10">
                                        <div class="checkbox clearfix">
                                            <label class="col-sm-3" title="随时退" id="d_isAnyReturn">随时退</label>
                                            <label class="col-sm-3" title="支持过期退" id="d_isSupportOverTimeReturn">支持过期退</label>
                                            <label class="col-sm-3" title="动可积分" id="d_isSupportScore">活动可积分</label>
                                            <label class="col-sm-3" title="退货返回积分" id="d_isSupportReturnScore">退货返回积分</label>
                                            <label class="col-sm-3" title="推荐到首页" id="d_isShowIndex">推荐到首页</label>
                                            <label class="col-sm-3" title="包邮" id="d_isPostageFree">包邮</label>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="row p-l-10 p-r-10">
                                <div class="form-group">
                                    <label for="seo" class="col-sm-5 control-label">活动描述:</label>

                                </div>

                            </div>
                            <div class="row p-l-10 p-r-10">

                                <div class="from-group">
                                    <div class="col-sm-12" id="d_activityDesc">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="box box-info">
                        <div class="box-header">
                            <h3 class="box-title">团购规则设置</h3>

                        </div>
                        <div class="box-body table-responsive no-padding">
                            <table class="table table-hover" id="d_productGroupOrderRules">
                                <tr>
                                    <th>序号</th>
                                    <th>阶梯编号</th>
                                    <th>团购人数</th>
                                    <th>团购价格</th>

                                </tr>
                            </table>
                        </div>
                    </div>
                    <#-- Del by zhajh  at 20160315  2023 新增团购或秒杀活动中，能参加的会员等级模块直接隐藏  Begin-->
                    <#--
                    <div class="box box-info">
                        <div class="box-header">
                            <h3 class="box-title">参加的会员等级</h3>

                        </div>
                        <div class="box-body table-responsive no-padding">
                            <table class="table table-hover" id="d_productActivityPartyLevels">
                                <tr>
                                    <th>序号</th>
                                    <th>会员等级</th>
                                    <th>会员等级名称</th>
                                </tr>
                            </table>
                        </div>
                    </div>
                    -->
                     <#-- Del by zhajh  at 20160315  2023 新增团购或秒杀活动中，能参加的会员等级模块直接隐藏  End-->
                    <div class="box box-info">
                        <div class="box-header">
                            <h3 class="box-title">参加的社区</h3>
                        </div>
                        <div class="box-body table-responsive no-padding">
                            <table class="table table-hover" id="d_productActivityAreas">
                                <tr>
                                    <th>序号</th>
                                    <th>社区编号</th>
                                    <th>社区名称</th>

                                </tr>

                            </table>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
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
</div><!-- 提示弹出框end -->


    <script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.js</@ofbizContentUrl>"></script>
    <script type="text/javascript">
        $(function () {
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

        function activityDetail(id) {
            $.ajax({
                url: "productActivityDetail",
                type: "POST",
                data: {activityId: id},
                dataType: "json",
                success: function (data) {
                    if (data) {
                        $('#d_productStoreName').text(data.d_productStoreName);
                        $('#d_activityTypeName').text(data.d_activityTypeName);
                        $('#d_activityAuditStatusName').text(data.d_activityAuditStatusName);

                        if (data.activityAuditStatus) {
                            if (data.activityAuditStatus == 'ACTY_AUDIT_INIT') {
                                $('#aduitBtn').show();
                            } else {
                                $('#aduitBtn').hide();
                            }
                        } else {
                            $('#aduitBtn').hide();
                        }

                        $('#d_activityCode').text(data.d_activityCode);
                        $('#d_activityName').text(data.d_activityName);
                        $('#d_publishDate').text(data.d_publishDate);
                        $('#d_endDate').text(data.d_endDate);
                        $('#d_activityStartDate').text(data.d_activityStartDate);
                        $('#d_activityEndDate').text(data.d_activityEndDate);
                        $('#d_limitQuantity').text(data.d_limitQuantity);
                        $('#d_activityQuantity').text(data.d_activityQuantity);
                        $('#d_productName').text(data.d_productName);
                        $('#d_shipmentTypeName').text(data.d_shipmentTypeName);
                        $('#d_scoreValue').text(data.d_scoreValue);
                        $('#d_activityPayTypeName').text(data.d_activityPayTypeName);
                        $('#d_productPrice').text(data.d_productPrice);

                        console.log(data.d_virtualProductStartDate)
                        if(data.d_virtualProductStartDate!=''&& data.d_virtualProductEndDate!='') {
                            $('#d_virtualProductStartDate').text(data.d_virtualProductStartDate);
                            $('#d_virtualProductEndDate').text(data.d_virtualProductEndDate);
                            $('#virtualProductDateDiv').show()
                        }else{
                            $('#virtualProductDateDiv').hide()
                        }
                        if (data.d_isAnyReturn == 'Y') {
                            $('#d_isAnyReturn').show();
                        }else{
                            $('#d_isAnyReturn').hide();
                        }
                        if (data.d_isSupportOverTimeReturn == 'Y') {
                            $('#d_isSupportOverTimeReturn').show();
                        }else{
                            $('#d_isSupportOverTimeReturn').hide();
                        }
                        if (data.d_isSupportScore == 'Y') {
                            $('#d_isSupportScore').show();
                        }else{
                            $('#d_isSupportScore').hide();
                        }
                        if (data.d_isSupportReturnScore == 'Y') {
                            $('#d_isSupportReturnScore').show();
                        }else{
                            $('#d_isSupportReturnScore').hide();
                        }
                        if (data.d_isShowIndex == 'Y') {
                            $('#d_isShowIndex').show();
                        }else{
                            $('#d_isShowIndex').hide();
                        }
                        
                        if (data.d_isPostageFree == 'Y') {
                            $('#d_isPostageFree').show();
                        }else{
                            $('#d_isPostageFree').hide();
                        }

                        if(data.d_productType!='VIRTUAL_GOOD'){
                            $('#virtualProductDateDiv').hide();
                        }else{
                            $('#d_shipmentTypeDiv').hide();
                        }

                        $('#d_activityDesc').html(data.d_activityDesc);
                        $('#d_productGroupOrderRules').find('tr').each(function (i) {
                            if (i > 0) {
                                $(this).remove()
                            }
                        })
                        if (data.d_productGroupOrderRules) {
                            for (var i = 0; i < data.d_productGroupOrderRules.length; i++) {
                                var trObj = "";
                                trObj += "<tr>"
                                var obj = data.d_productGroupOrderRules[i];
                                trObj += "<td>" + (i + 1) + "</td>";
                                trObj += "<td>" + obj.seqId + "</td>";
                                trObj += "<td>" + obj.orderQuantity + "</td>";
                                trObj += "<td>" + obj.orderPrice + "</td>";
                                trObj += "</tr>";
                                $('#d_productGroupOrderRules').find('tr').parent().append(trObj);
                            }
                        }

                        <#-- Del by zhajh  at 20160315  2023 新增团购或秒杀活动中，能参加的会员等级模块直接隐藏  Begin-->
                        <#--
                        $('#d_productActivityPartyLevels').find('tr').each(function (i) {
                            if (i > 0) {
                                $(this).remove()
                            }
                        })
                        if (data.d_productActivityPartyLevels) {
                            for (var i = 0; i < data.d_productActivityPartyLevels.length; i++) {
                                var trObj = "";
                                trObj += "<tr>"
                                var obj = data.d_productActivityPartyLevels[i];
                                trObj += "<td>" + (i + 1) + "</td>";
                                trObj += "<td>" + obj.levelId + "</td>";
                                trObj += "<td>" + obj.levelName + "</td>";
                                trObj += "</tr>";
                                $('#d_productActivityPartyLevels').find('tr').parent().append(trObj);
                            }

                        }
                        -->
                        <#-- Del by zhajh  at 20160315  2023 新增团购或秒杀活动中，能参加的会员等级模块直接隐藏  End-->
                        
                        $('#d_productActivityAreas').find('tr').each(function (i) {
                            if (i > 0) {
                                $(this).remove()
                            }
                        })
                        if (data.d_productActivityAreas) {
                            for (var i = 0; i < data.d_productActivityAreas.length; i++) {
                                var trObj = "";
                                trObj += "<tr>"
                                var obj = data.d_productActivityAreas[i];
                                trObj += "<td>" + (i + 1) + "</td>";
                                trObj += "<td>" + obj.communityId + "</td>";
                                trObj += "<td>" + obj.communityName + "</td>";
                                trObj += "</tr>";
                                $('#d_productActivityAreas').find('tr').parent().append(trObj);
                            }
                        }

                        $('#modal_detail').modal('show');
                    }
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作失败！");
                    $('#modal_msg').modal();
                }
            })
        }

        function deleteGroupOrder(id) {

            $.confirmLayer({
                msg: '确定删除该团购信息',
                confirm: function () {
                    $.ajax({
                        url: 'deleteGroupOrder',
                        data: {activityId: id, pass: 'Y'},
                        type: "POST",
                        async: false,
                        dataType: "json",
                        success: function (data) {
                            if (data) {
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                $('#modal_msg').modal();
                                $('#confirmLayer').modal('hide');
                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.href='<@ofbizUrl>findGroupOrder</@ofbizUrl>';
                                })
                            }
                        },
                        error: function (data) {

                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作失败！");
                            $('#modal_msg').modal();
                            $('#confirmLayer').modal('hide');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })
                        }

                    })
                }
            })

        }
        function updateGroupOrderPrice(id) {

            $.confirmLayer({
                msg: '确定要更新选中的促销活动价格吗?',
                confirm: function () {
                    $.ajax({
                        url: 'updateGroupOrderPrice',
                        data: {activityId: id, pass: 'Y'},
                        type: "POST",
                        async: false,
                        dataType: "json",
                        success: function (data) {
                            if (data) {

                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                $('#modal_msg').modal();
                                $('#confirmLayer').modal('hide');
                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.href='<@ofbizUrl>findGroupOrder</@ofbizUrl>';
                                })
                            }
                        },
                        error: function (data) {

                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作失败！");
                            $('#modal_msg').modal();
                            $('#confirmLayer').modal('hide');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })
                        }

                    })
                }
            })

        }

        function auditGroupOrder(id) {
            $('#modal_audit').modal('show');
            console.log(id)
            $('#auditactivityId').val(id)


        }

        function doAudit() {
            var hasSel = 0;
            var val = '';
            $("input[name='auditResult']").each(function () {
                if ($(this).is(':checked')) {
                    hasSel = 1;
                    val = $(this).val();

                }
            });
            console.log( $('#auditactivityId').val())
            if (!hasSel) {
                alert('请选择审批结果')
            } else {
                $.ajax({
                    url: 'auditGroupOrder',
                    data: {activityId: $('#auditactivityId').val(), pass: val},
                    type: "POST",
                    async: false,
                    dataType: "json",
                    success: function (data) {
                        if (data) {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作成功！");
                            $('#modal_msg').modal();
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.href = '<@ofbizUrl>findGroupOrder</@ofbizUrl>';
                            })
                        }
                    },
                    error: function (data) {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作失败！");
                        $('#modal_msg').modal();
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.reload();
                        })
                    }

                })
            }
        }


    </script>