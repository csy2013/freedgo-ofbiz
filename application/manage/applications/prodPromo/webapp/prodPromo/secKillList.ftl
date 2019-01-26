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
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "findSecKill?lookupFlag=Y"+ paramList +"&">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.css</@ofbizContentUrl>">
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">${uiLabelMap.secKillList}</h3>
    </div>
    <div class="box-body">
        <form class="form-inline clearfix" role="form" method="post" action="<@ofbizUrl>findSecKill</@ofbizUrl>">
            <input type="hidden" name="lookupFlag" value="Y">
            <input type="hidden" name="activityType" value="SEC_KILL"/>
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.activityCode}</span>
                    <input type="text" class="form-control" name="activityCode" placeholder="${uiLabelMap.activityCode}" value="${activityCode?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.activityName}</span>
                    <input type="text" class="form-control" name="activityName" placeholder="${uiLabelMap.activityName}" value="${activityName?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.activityAuditStatus}</span>
                    <select class="form-control" id="activityAuditStatus" name="activityAuditStatus">
                        <option value="">全部</option>
                    <#list activityStatusEnums as activityStatusEnum>
                        <#if activityStatusEnum.enumId != 'ACTY_AUDIT_PASS'>
                        <option value="${(activityStatusEnum.enumId)?if_exists}" <#if activityAuditStatus?default('')==activityStatusEnum.enumId>selected</#if>>${(activityStatusEnum.get("description",locale))?if_exists}</option>
                        </#if>
                    </#list>
                    </select>
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.activityStartDate}</span>

                    <div class="input-group date form_datetime col-sm-10 " data-link-field="activityStartDate">
                        <input class="form-control" size="16" type="text" readonly value="${activityStartDate?default("")}">
                        <input id="activityStartDate" class="dp-vd" type="hidden" name="activityStartDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.activityEndDate}</span>

                    <div class="input-group date form_datetime col-sm-10 " data-link-field="activityEndDate">
                        <input class="form-control" size="16" type="text" readonly value="${activityEndDate?default("")}">
                        <input id="activityEndDate" class="dp-vd" type="hidden" name="activityEndDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                </div>
            </div>
            <div class="input-group pull-right">
            <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_VIEW", session)>
                <button class="btn btn-success btn-flat">${uiLabelMap.CommonView}</button></#if>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
        <div class="row m-b-10">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_ADD", session)>
                    <a class="btn btn-primary" href="<@ofbizUrl>addSecKill</@ofbizUrl>" ;>
                    ${uiLabelMap.CommonAdd}
                    </a>
                </#if>
                <#--<#if security.hasEntityPermission("PRODPROMO_SECKILL", "_DELETE", session)>
                    <button class="btn btn-primary" onclick="addUser()" ;>
                        删除
                    </button>
                </#if>
                <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_AUDIT", session)>
                    <button class="btn btn-primary" onclick="addUser()" ;>
                        审批
                    </button>
                </#if>-->
                </div>
            </div>
        <#assign commonUrl1 = commonUrl+"ORDER_FILED=${orderFiled}&amp;ORDER_BY=${orderBy}&amp;"/>
        <#if groupList?has_content>

            <div class="col-sm-6">
                <div class="dp-tables_length">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                onchange="location.href='${commonUrl1}VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                            <option value="10" <#if viewSize ==10>selected</#if>>10</option>
                            <option value="20" <#if viewSize==20>selected</#if>>20</option>
                            <option value="30" <#if viewSize==30>selected</#if>>30</option>
                            <option value="40" <#if viewSize==50>selected</#if>>50</option>
                        </select>
                        条
                    </label>
                </div>

            </div>
        </#if>
        </div>


    <#if groupList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>${uiLabelMap.activityCode}
                            <#if orderFiled?default("") =='activityCode'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityCode&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=activityCode&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityCode&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>${uiLabelMap.activityName}
                            <#if orderFiled?default("") =='activityName'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityName&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=activityName&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityName&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>${uiLabelMap.activityType}
                            <#if orderFiled?default("") =='activityType'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityType&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=activityType&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityType&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.activityStartDate}
                            <#if orderFiled?default("") =='activityStartDate'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityStartDate&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=activityStartDate&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityStartDate&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>
                        <th>${uiLabelMap.activityEndDate}
                            <#if orderFiled?default("") =='activityEndDate'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityEndDate&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=activityEndDate&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityEndDate&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>${uiLabelMap.activityAuditStatus}
                            <#if orderFiled?default("") =='activityAuditStatus'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityAuditStatus&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=activityAuditStatus&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=activityAuditStatus&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                       <#-- <th>${uiLabelMap.hasGroup}
                            <#if orderFiled?default("") =='hasGroup'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=hasGroup&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=hasGroup&amp;ORDER_BY=ASC"
                                </#if>
                            <#else>
                                <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=hasGroup&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>-->

                        <th>已购数量
                            <#if orderFiled?default("") =='hasBuyQuantity'>
                                <#if orderBy =='DESC'>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=hasBuyQuantity&amp;ORDER_BY=ASC"></a>
                                <#else>
                                    <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=hasBuyQuantity&amp;ORDER_BY=DESC"
                                </#if>
                            <#else>
                                <a class="fa text-muted fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=hasBuyQuantity&amp;ORDER_BY=ASC"></a>
                            </#if>
                        </th>

                        <th>${uiLabelMap.CommonEmptyHeader}</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list groupList as partyRow>
                        <tr>
                            <td><input class="js-checkchild" type="checkbox"></td>
                            <td><#if partyRow.containsKey("activityCode")>${partyRow.activityCode?default("0")}</#if></td>
                            <td><#if partyRow.containsKey("activityName")>${partyRow.activityName?default("0")}</#if></td>
                            <td><#if partyRow.containsKey("activityType")>
                              <#list activityTypeEnums as activityTypeEnum>
                                <#if activityTypeEnum.enumId == partyRow.activityType >${(activityTypeEnum.get("description",locale))?if_exists}</#if>
                            </#list>
                            </#if>
                            </td>
                            <td><#if partyRow.containsKey("activityStartDate")>${partyRow.activityStartDate?default("N/A")?string("yyyy-MM-dd HH:mm")}</#if></td>
                            <td><#if partyRow.containsKey("activityEndDate")>${partyRow.activityEndDate?string("yyyy-MM-dd HH:mm")?default("N/A")}</#if></td>
                            <td>
                                <#assign activityAuditStatus = partyRow.activityAuditStatus?default("")>
                                <#assign activityStatusDesc = "">
                                <#list activityStatusEnums as activityStatusEnum>
                                    <#if activityStatusEnum.enumId == partyRow.activityAuditStatus?default("") >
                                        <#assign activityStatusDesc = (activityStatusEnum.get("description",locale))?default("")>
                                        <#if (partyRow.activityAuditStatus?default("")!= 'ACTY_AUDIT_PASS')>
                                            <#assign activityStatusDesc = activityStatusEnum.get("description",locale)?if_exists></#if>
                                    </#if>
                                </#list>



                                <#--待审核：未审核通过的促销-->
                                <#--未开始：审批通过并且系统当前时间小于促销开始时间-->
                                <#--进行中：审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间-->
                                <#--已结束：审批通过并且系统当前时间大于等于销售结束时间-->

                               <#--<#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') >-->
                                <#--<#assign activityAuditStatus = "ACTY_AUDIT_PUBING"/>-->
                                <#--<#assign activityStatusDesc = "待发布">-->
                            <#--</#if>-->
                                <#--<#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.before(partyRow.getTimestamp("activityStartDate")))>-->
                                <#--<#assign activityAuditStatus = "ACTY_AUDIT_UNBEGIN"/>-->
                                <#--<#assign activityStatusDesc = "未开始">-->
                            <#--</#if>-->
                                <#--<#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.after(partyRow.getTimestamp("activityStartDate")))&& (nowDate.before(partyRow.getTimestamp("activityEndDate")))>-->
                                <#--<#assign activityAuditStatus = "ACTY_AUDIT_RUNING"/>-->
                                <#--<#assign activityStatusDesc = "进行中"></#if>-->
                                <#--<#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.after(partyRow.getTimestamp("activityEndDate")))>-->
                                <#--<#assign activityAuditStatus = "ACTY_AUDIT_FINISH"/><#assign activityStatusDesc = "已结束"></#if>-->
                                <#--<#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS')>-->
                                <#--<#assign activityAuditStatus = "ACTY_AUDIT_REMOVE"/><#assign activityStatusDesc = "已下架">-->
                            <#--</#if-->


                            <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_INIT') >
                                <#assign activityAuditStatus = "ACTY_AUDIT_INIT"/>
                                <#assign activityStatusDesc = "待审核">
                            </#if>
                            <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.before(partyRow.getTimestamp("activityStartDate")))>
                                <#assign activityAuditStatus = "ACTY_AUDIT_UNBEGIN"/>
                                <#assign activityStatusDesc = "未开始">
                            </#if>
                            <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.after(partyRow.getTimestamp("activityStartDate")))&& (nowDate.before(partyRow.getTimestamp("activityEndDate")))>
                                <#assign activityAuditStatus = "ACTY_AUDIT_RUNING"/>
                                <#assign activityStatusDesc = "进行中"></#if>
                            <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.after(partyRow.getTimestamp("activityEndDate")))>
                                <#assign activityAuditStatus = "ACTY_AUDIT_FINISH"/><#assign activityStatusDesc = "已结束"></#if>
                            ${activityStatusDesc}
                            <#--<td><#if partyRow.containsKey("hasGroup")>${partyRow.hasGroup?default("0")}</#if></td>-->
                            <td><#if partyRow.containsKey("hasBuyQuantity")>${partyRow.hasBuyQuantity?default("0")}</#if></td>


                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_DETAIL", session)>
                                        <button type="button" class="btn btn-danger btn-sm" onclick="productSkDetail('${partyRow.activityId?default("N/A")}')">查看</button>
                                    </#if>

                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#--<#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session) && (activityAuditStatus == 'ACTY_AUDIT_INIT' ||
                                        activityAuditStatus == 'ACTY_AUDIT_PASS'||activityAuditStatus == 'ACTY_AUDIT_NOPASS'||activityAuditStatus == 'ACTY_AUDIT_PUBING'
                                        ||activityAuditStatus == 'ACTY_AUDIT_UNBEGIN'))>-->
                                            <#if  (activityAuditStatus != 'ACTY_AUDIT_RUNING')>
                                                <li><a href="<@ofbizUrl>editSecKill?activityId=${partyRow.activityId}</@ofbizUrl>">编辑</a></li>
                                            </#if>
                                        <#--进行中的活动必须变成下架才可以重新上架-->
                                            <#if  (activityAuditStatus == 'ACTY_AUDIT_RUNING')>
                                                <li><a href="javascript:;" onclick="shutDownSecKill(${partyRow.activityId})">下架</a></li>
                                            </#if>
                                        <#--</#if>-->
                                            <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session) && (partyRow.activityAuditStatus == 'ACTY_AUDIT_INIT'))>
                                                <li><a href="#" onclick="auditSecKill(${partyRow.activityId})">审批</a></li>
                                            </#if>
                                            <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_DELETE", session) && (activityAuditStatus == 'ACTY_AUDIT_INIT'||
                                            activityAuditStatus == 'ACTY_AUDIT_NOPASS'))>
                                                <li><a href="#" onclick="deleteSecKill(${partyRow.activityId})">删除</a></li>
                                            </#if>
                                    </ul>
                                </div>
                            </td>
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
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(groupListSize, viewSize) />

        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", groupListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl1 ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=groupListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />

    <#else>
        <div class="row">
            <div class="col-sm-10">
                <h3>查询无数据</h3>
            </div>
        </div>
    </#if>
    </div>
    <!-- /.box-body -->
</div>





<!-- 提示弹出框start -->
<div id="modal_audit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_audit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">秒杀审批</h4>
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
                            <h3 class="box-title">秒杀活动基本信息</h3>
                        </div>
                        <div class="box-body">
                            <#--<div class="row">-->
                                <#--<div class="form-group col-sm-6">-->
                                    <#--<label for="title" class="col-sm-5 control-label">活动类型:</label>-->

                                    <#--<div class="col-sm-7" id="d_activityTypeName">-->

                                    <#--</div>-->
                                <#--</div>-->
                                <#--<div class="form-group col-sm-6">-->
                                    <#--<label for="subTitle" class="col-sm-5 control-label">活动状态:</label>-->

                                    <#--<div class="col-sm-7" id="d_activityAuditStatusName">-->

                                    <#--</div>-->
                                <#--</div>-->
                            <#--</div>-->

                            <div class="row">
                                <div class="form-group col-sm-6" data-type="required" data-mark="活动编码">
                                    <label for="title" class="col-sm-5 control-label"><i class="required-mark">*</i>活动编码:</label>

                                    <div class="col-sm-7" id="d_activityCode">

                                    </div>
                                </div>
                                <div class="form-group col-sm-6" data-type="required" data-mark="促销名称">
                                    <label for="subTitle" class="col-sm-5 control-label"><i class="required-mark">*</i>促销名称:</label>

                                    <div class="col-sm-7" id="d_activityName">


                                    </div>
                                </div>
                            </div>




                            <div class="row">
                                <div class="form-group col-sm-6">
                                    <label for="startTime" class="col-sm-5 control-label">活动开始时间:</label>

                                    <div class="col-sm-7" id="d_activityStartDate">

                                    </div>
                                </div>
                                <div id="endTimeGroup" class="form-group col-sm-6">
                                    <label for="endTime" class="col-sm-5 control-label">活动结束时间:</label>

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
                            </div>

                            <div class="row title">
                                <div class="form-group col-sm-10">
                                    <label for="subTitle" class="col-sm-2 control-label">促销描述:</label>
                                    <div class="col-sm-10" >
                                        <div class="box-body pad" >
                                            <textarea id="Centent" name="Centent"  value="" >
                                            </textarea>
                                            <p class="dp-error-msg"></p>
                                        </div>
                                    </div>
                                </div>
                            </div>





                            <div class="box box-info" id="product">
                                <div class="box-header">
                                    <h3 class="box-title">秒杀商品</h3>
                                </div>
                                <div class="box-body table-responsive no-padding">
                                    <div class="table-responsive no-padding">
                                        <table class="table table-hover js-checkparent js-sort-list addProducts"
                                               id="productTable">
                                            <thead>
                                            <tr>
                                                <#--<th>商品图片</th>-->
                                                <th>商品编码</th>
                                                <th>商品名称</th>
                                                <th>规格</th>
                                                <th>活动数量</th>
                                                <th>秒杀金额</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>



                            <#--<div class="row p-l-10 p-r-10">-->
                                <#--<div class="form-group">-->
                                    <#--<label for="seo" class="col-sm-5 control-label">活动描述:</label>-->

                                <#--</div>-->

                            <#--</div>-->
                            <#--<div class="row p-l-10 p-r-10">-->

                                <#--<div class="from-group">-->
                                    <#--<div class="col-sm-12" id="d_activityDesc">-->
                                    <#--</div>-->
                                <#--</div>-->
                            <#--</div>-->


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
                    <#--<div class="box box-info">-->
                        <#--<div class="box-header">-->
                            <#--<h3 class="box-title">参加的社区</h3>-->
                        <#--</div>-->
                        <#--<div class="box-body table-responsive no-padding">-->
                            <#--<table class="table table-hover" id="d_productActivityAreas">-->
                                <#--<tr>-->
                                    <#--<th>序号</th>-->
                                    <#--<th>社区编号</th>-->
                                    <#--<th>社区名称</th>-->

                                <#--</tr>-->

                            <#--</table>-->
                        <#--</div>-->
                    <#--</div>-->
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

    <#--<script src="<@ofbizContentUrl>/images/jquery/plugins/select2/select2.min.js</@ofbizContentUrl>"></script>-->
    <script type="text/javascript">
        $(function () {
            CKEDITOR.replace("Centent");
            $('.form_datetime').datetimepicker({
                language: 'zh-CN',
                todayBtn: 1,
                autoclose: 1,
                todayHighlight: 1,
                startView: 2,
                forceParse: 0,
                showMeridian:1
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



        function deleteSecKill(id) {

            $.confirmLayer({
                msg: '确定删除该秒杀信息',
                confirm: function () {
                    $.ajax({
                        url: 'deleteSecKill',
                        data: {activityId: id, pass: 'Y'},
                        type: "POST",
                        async: false,
                        dataType: "json",
                        success: function (data) {
                            if (data) {
                                $('#confirmLayer').modal('hide');
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                $('#modal_msg').modal();


                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.href='<@ofbizUrl>findSecKill</@ofbizUrl>';
                                })
                            }
                        },
                        error: function (data) {
                            $('#confirmLayer').modal('hide');
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作失败！");
                            $('#modal_msg').modal();
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })
                        }

                    })
                }
            })

        }


        function shutDownSecKill(id) {

            $.confirmLayer({
                msg: '确定下架该秒杀',
                confirm: function () {
                    $.ajax({
                        url: 'shutDownActivity',
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
                                    window.location.href='<@ofbizUrl>findSecKill</@ofbizUrl>';
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

        function auditSecKill(id) {
            $('#modal_audit').modal('show');
            console.log(id);
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
            console.log( $('#auditactivityId').val());
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
                                window.location.href = '<@ofbizUrl>findSecKill</@ofbizUrl>';
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


        <#-- 查看秒杀详情-->
        function productSkDetail(productSkId) {
           // alert(1);
            //商品秒杀的新增
            jQuery.ajax({
                url: '<@ofbizUrl>productSkDetail</@ofbizUrl>',
                type: 'POST',
                data: {
                    'productSkId':productSkId
                },
                success: function(data){

                    if (data.productActivity) {
                        var productActivity = data.productActivity;
                        $('#modal_detail #d_activityCode').text(productActivity.activityCode);
                        $('#modal_detail #d_activityName').text(productActivity.activityName);
                        $('#modal_detail #d_activityStartDate').text(timeStamp2String2(productActivity.activityStartDate));
                        $('#modal_detail #d_activityEndDate').text(timeStamp2String2(productActivity.activityEndDate));
                        $('#modal_detail #d_limitQuantity').text(productActivity.limitQuantity);
//                        $('#modal_detail #d_activityDesc').text(productActivity.activityDesc);
                        CKEDITOR.instances.Centent.setData(productActivity.activityDesc);
                        var product_List = data.productSkProductInfoList;
                        $('#productTable>tbody').empty();
                        var tr1 = "";
                        for (var i = 0; i < product_List.length; i++) {

                            tr1 += "<tr>"
//                                    + "<td></td>"
                                    + "<td>" + product_List[i].productInfo.productId + "</td>"
                                    + "<td>" + product_List[i].productInfo.productName + "</td>"
                                    + "<td>" + product_List[i].featureInfo + "</td>"
                                    + "<td>" + product_List[i].activityQuantity + "</td>"
                                    + "<td>" + product_List[i].activityPrice + "</td>"
                                    + "</tr>"
                        }
                        $('#productTable>tbody').append(tr1);

                        $('#modal_detail').modal();
                    }
                },
                error: function (data) {
                    //隐藏新增弹出窗口
                    $('#modal_detail').modal('toggle');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        }


        //时间格式化2
        function timeStamp2String2(datetime){
            var year = datetime.year+1900;
            var month = datetime.month + 1 < 10 ? "0" + (datetime.month + 1) : datetime.month + 1;
            /*var date = datetime.day < 10 ? "0" + datetime.day : datetime.day;*/  //这是取周几
            var date = datetime.date < 10 ? "0" + datetime.date : datetime.date;
            var hour = datetime.hours< 10 ? "0" + datetime.hours : datetime.hours;
            var minute = datetime.minutes< 10 ? "0" + datetime.minutes : datetime.minutes;
            var second = datetime.seconds < 10 ? "0" + datetime.seconds : datetime.seconds;
            return year + "-" + month + "-" + date+" "+hour+":"+minute+":"+second;
        };


    </script>