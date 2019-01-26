<#assign commonUrl = "ticketList?lookupFlag=Y"+ paramList?default("") +"&">
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">验证码列表</h3>
    </div>
<#--${ticketList}-->
    <div class="box-body">
        <form class="form-inline clearfix" role="form" method="post" id="ticketForm" action="<@ofbizUrl>ticketList</@ofbizUrl>">
            <input type="hidden" name="lookupFlag" value="Y">
            <input type="hidden" name="status" value="${status?default('')}"/>
            <div class="form-group">
                <#if !isBusiness>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商家名称</span>
                    <input type="text" class="form-control" name="partyName" placeholder="商家名称" value="${partyName?default("")}">
                </div>

                <#else >

                 </#if>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">用户名</span>
                    <input type="text" class="form-control" name="userName" placeholder="用户名" value="${userName?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">手机号码</span>
                    <input type="text" class="form-control" name="mobile" placeholder="手机号码" value="${mobile?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">订单号</span>
                    <input type="text" class="form-control" name="orderId" placeholder="订单号" value="${orderId?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" class="form-control" name="productName" placeholder="商品名称" value="${productName?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">验证码</span>
                    <input type="text" class="form-control" name="ticketNo" placeholder="验证码" value="${ticketNo?default("")}">
                </div>
            </div>

            <div class="input-group pull-right m-l-10">
                <a href="#" class="btn btn-success btn-flat" id="selectedItemExport">导出选择项</a>
            </div>
            <div class="input-group pull-right m-l-10">
                <button class="btn btn-success btn-flat" id="btn-query">${uiLabelMap.CommonView}</button>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>

    <#if security.hasEntityPermission("TICKET_LIST", "_VIEW", session)>
        <ul class="nav nav-tabs">
            <li role="presentation" <#if status?default("") == ''> class="active"</#if>><a href="#" onclick="queryTicketList('')">全部</a></li>
            <li role="presentation" <#if status?default("") =='notUsed'>class="active"</#if>><a href="#" onclick="queryTicketList('notUsed')">未使用</a></li>
            <li role="presentation" <#if status?default("") =='hasUsed'>class="active"</#if>><a href="#" onclick="queryTicketList('hasUsed')">已使用</a></li>
            <li role="presentation" <#if status?default("") =='expired'>class="active"</#if>><a href="#" onclick="queryTicketList('expired')">已过期</a></li>
            <li role="presentation" <#if status?default("") =='notAudited'>class="active"</#if>><a href="#" onclick="queryTicketList('notAudited')">未审批</a></li>
            <li role="presentation" <#if status?default("") =='notRefunded'>class="active"</#if>><a href="#" onclick="queryTicketList('notRefunded')">退款中</a></li>
            <li role="presentation" <#if status?default("") =='hasRefuned'>class="active"</#if>><a href="#" onclick="queryTicketList('hasRefuned')">已退款</a></li>
        </ul>

    </#if>
    <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
    <#if ticketList?has_content>
        <div class="row m-b-12 m-t-10">
            <div class="col-sm-6">
            </div>
            <div class="col-sm-6">
                <div class="dp-tables_length ">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                onchange="location.href='${commonUrl1}VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                            <option value="20" <#if viewSize ==20>selected</#if>>20</option>
                            <option value="30" <#if viewSize==30>selected</#if>>30</option>
                            <option value="50" <#if viewSize==50>selected</#if>>50</option>
                            <option value="100" <#if viewSize==100>selected</#if>>100</option>
                        </select>
                        条
                    </label>
                </div>
            </div>
            <!-- 列表当前分页条数end -->
        </div>

    </#if>
        <br/>
<#if ticketList?has_content>
<div class="row">
    <div class="col-sm-12">
        <table class="table table-bordered table-hover js-checkparent js-sort-list">
            <thead>

                <th><input class="js-allcheck" type="checkbox" id="checkAll"></th>

                <th>商家名称
                    <#if orderFiled == 'businessName'>
                        <#if orderBy == 'DESC'>
                            <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=businessName&amp;ORDER_BY=ASC"></a>
                        <#else>
                            <a class="fa  fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=businessName&amp;ORDER_BY=DESC"
                        </#if>
                    <#else>
                        <a class="fa  text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=businessName&amp;ORDER_BY=DESC"></a>
                    </#if>
                </th>
                <th>用户名
                    <#if orderFiled == 'name'>
                        <#if orderBy == 'DESC'>
                            <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=ASC"></a>
                        <#else>
                            <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=DESC"
                        </#if>
                    <#else>
                        <a class="fa  text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=name&amp;ORDER_BY=DESC"></a>
                    </#if>
                </th>
                <th>手机号码
                    <#if orderFiled == 'mobile'>
                        <#if orderBy == 'DESC'>
                            <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=mobile&amp;ORDER_BY=ASC"></a>
                        <#else>
                            <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=mobile&amp;ORDER_BY=DESC"
                        </#if>
                    <#else>
                        <a class="fa  text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=mobile&amp;ORDER_BY=DESC"></a>
                    </#if>
                </th>

                <th>订单号
                    <#if orderFiled == 'orderId'>
                        <#if orderBy == 'DESC'>
                            <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=orderId&amp;ORDER_BY=ASC"></a>
                        <#else>
                            <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=orderId&amp;ORDER_BY=DESC"
                        </#if>
                    <#else>
                        <a class="fa  text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=orderId&amp;ORDER_BY=DESC"></a>
                    </#if>
                </th>
                <th>商品名称
                    <#if orderFiled == 'productName'>
                        <#if orderBy == 'DESC'>
                            <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productName&amp;ORDER_BY=ASC"></a>
                        <#else>
                            <a class="fa  fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=productName&amp;ORDER_BY=DESC"
                        </#if>
                    <#else>
                        <a class="fa text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=productName&amp;ORDER_BY=DESC"></a>
                    </#if>
                </th>
                <th>验证码
                    <#if orderFiled == 'ticketNo'>
                        <#if orderBy == 'DESC'>
                            <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=ticketNo&amp;ORDER_BY=ASC"></a>
                        <#else>
                            <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=ticketNo&amp;ORDER_BY=DESC"
                        </#if>
                    <#else>
                        <a class="fa text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=ticketNo&amp;ORDER_BY=DESC"></a>
                    </#if>
                </th>
                <th>状态
                    <#if orderFiled == 'ticketStatus'>
                        <#if orderBy == 'DESC'>
                            <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=ticketStatus&amp;ORDER_BY=ASC"></a>
                        <#else>
                            <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=ticketStatus&amp;ORDER_BY=DESC"
                        </#if>
                    <#else>
                        <a class="fa text-muted  fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=ticketStatus&amp;ORDER_BY=DESC"></a>
                    </#if>
                </th>
            </tr>
            </thead>
            <tbody>
                <#list ticketList as partyRow>
                <input id="ids" type="hidden"/>
                <tr>
                    <td><input class="js-checkchild" type="checkbox" id="${partyRow.ticketId?if_exists}" value="${partyRow.ticketId?if_exists}"/></td>
                    <td>${partyRow.businessName?default("")}</td>
                    <td>${partyRow.name?default("")}</td>
                    <td>${partyRow.mobile?default("")}</td>
                    <td>${partyRow.orderId?default("")}</td>
                    <td>${partyRow.productName?default("")}</td>
                    <td>${partyRow.ticketNo?default("")}</td>
                    <td><#if partyRow.ticketStatus =='notUsed'>未使用</#if>
                        <#if partyRow.ticketStatus =='hasUsed'>已使用</#if>
                        <#if partyRow.ticketStatus =='notAudited'>未审批</#if>
                        <#if partyRow.ticketStatus =='notRefunded'>待退款</#if>
                        <#if partyRow.ticketStatus =='hasRefuned'>已退款</#if>
                        <#if partyRow.ticketStatus =='rejectApplication'>审批拒绝</#if>
                        <#if partyRow.ticketStatus =='expired'>已过期</#if></td>
                </tr>
                </#list>
            </tbody>
        </table>
    </div>
</div>
    <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
    <#assign viewIndexFirst = 0/>
    <#assign viewIndexPrevious = viewIndex - 1/>
    <#assign viewIndexNext = viewIndex + 1/>
    <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(ticketListSize, viewSize) />
    <#--${context}-->

    <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", ticketListSize)/>
    <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
    <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
    listSize=ticketListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
    pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
    paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
<#else>
<div class="row">
    <div class="col-sm-12">
        <h3>&nbsp;&nbsp;无结果</h3>
    </div>
</div>
</#if>

<!-- /.box-body -->
</div>
</div>
<script type="text/javascript">
    function queryTicketList(status) {
        $("input[name='status']").val(status);
        $('#ticketForm').submit();
    }

    $(function(){
        $("#selectedItemExport").click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            if(checks.size() > 0 ){
                var ids ="";
                $('input[class="js-checkchild"]:checked').each(function(){
                    ids = ids + "," + $(this).val();
                    $("#ids").val(ids.substr(1,ids.length));
                });
                var idsView =$("#ids").val();
                if(idsView!=""){
                    document.location.href="<@ofbizUrl>ticketListReport.xls?ids="+idsView+"</@ofbizUrl>";
                }
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
                $('#modal_msg').modal();
            }
        })
    })

</script>