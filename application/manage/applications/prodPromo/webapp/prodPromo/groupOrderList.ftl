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
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "findGroupOrder?lookupFlag=Y"+ paramList +"&">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.css</@ofbizContentUrl>">

<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">拼团列表</h3>
    </div>
    <div class="box-body">
        <form class="form-inline clearfix" role="form"  id="QueryForm" method="post" action="<@ofbizUrl>findGroupOrder</@ofbizUrl>">
            <input type="hidden" name="activityAuditStatus" id="activityAuditStatus" value="">
            <input type="hidden" name="activityType" value="GROUP_ORDER"/>
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">促销编码</span>
                    <input type="text" class="form-control" name="activityCode" placeholder="${uiLabelMap.activityCode}" value="${activityCode?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">促销名称</span>
                    <input type="text" class="form-control" name="activityName" placeholder="${uiLabelMap.activityName}" value="${activityName?default("")}">
                </div>
            </div>
            <div class="input-group pull-right">
            <#if security.hasEntityPermission("PRODPROMO_GROUPORDER", "_VIEW", session)>
                <button class="btn btn-success btn-flat">${uiLabelMap.CommonView}</button></#if>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
        <div class="row m-b-10">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("PRODPROMO_GROUPORDER", "_ADD", session)>
                    <a class="btn btn-primary" href="<@ofbizUrl>addGroupOrder</@ofbizUrl>" ;>
                    添加
                    </a>
                </#if>

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
                            <option value="40" <#if viewSize==40>selected</#if>>40</option>
                        </select>
                        条
                    </label>
                </div>

            </div>
        </#if>
        </div>
    <ul class="nav nav-tabs">
        <li role="presentation" <#if activityAuditStatus?default("") == "">class="active"</#if>><a
                href="<@ofbizUrl>findGroupOrder</@ofbizUrl>">全部</a></li>
        <li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_INIT">class="active"</#if>><a
                href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_INIT')">待审核</a></li>
        <li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_PASS">class="active"</#if>><a
                href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_PASS')">已通过</a></li>
        <li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_NOPASS">class="active"</#if>><a
                href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_NOPASS')">已驳回</a></li>
    </ul>
    <#if groupList?has_content>
        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover table-responsive js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>促销编码

                        </th>

                        <th>促销名称

                        </th>

                       <#-- <th>促销类型

                        </th>-->
                        <th>促销开始时间

                        </th>
                        <th>促销结束时间

                        </th>

                        <th>促销状态

                        </th>

                       <#-- <th>${uiLabelMap.hasGroup}

                        </th>

                        <th>已购数量

                        </th>-->

                        <th>${uiLabelMap.CommonEmptyHeader}</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list groupList as partyRow>
                        <tr>
                            <td><input class="js-checkchild" type="checkbox"></td>
                            <td>${partyRow.activityCode?default("0")}</td>
                            <td>${partyRow.activityName?default("0")}</td>
                           <#-- <td>拼团
                            </td>-->
                            <td>${partyRow.activityStartDate?default("N/A")?string("yyyy-MM-dd HH:mm")}</td>
                            <td>${partyRow.activityEndDate?default("N/A")?string("yyyy-MM-dd HH:mm")}</td>
                            <td>
                            <#assign activityAuditStatus = partyRow.activityAuditStatus?default("")>
                            <#assign activityStatusDesc = "">
                            <#list activityStatusEnums as activityStatusEnum>
                                <#if activityStatusEnum.enumId == partyRow.activityAuditStatus?default("") >
                                    <#assign activityStatusDesc = (activityStatusEnum.get("description",locale))?default("")>
                                </#if>
                            </#list>

                                <#--待发布（auditStatus为审批通过并且系统当前时间小于发布时间）-->
                                <#--//未开始（auditStatus为审批通过并且系统当前时间小于销售开始时间）-->
                                <#--//进行中（auditStatus为审批通过并且系统当前时间大于等于销售开始时间小于销售结束时间）-->
                                <#--//已结束（auditStatus为审批通过并且系统当前时间大于等于销售结束时间小于下架时间）
                                //已下架（auditStatus为审批通过并且系统当前时间大于下架时间）-->
<#--
                                <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.before(partyRow.getTimestamp("activityStartDate")))>
                                    <#assign activityAuditStatus = "ACTY_AUDIT_PUBING"/>
                                    <#assign activityStatusDesc = "待发布">
                                </#if>
                                &lt;#&ndash;-Mod by zhajh at 20160413 未开始状态判断的修改 Begin&ndash;&gt;
                                &lt;#&ndash;
                                <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.before(partyRow.getTimestamp("activityStartDate")))>
                                &ndash;&gt;
                                <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.before(partyRow.getTimestamp("activityStartDate")))&&(nowDate.after(partyRow.getTimestamp("publishDate")))&&(nowDate.before(partyRow.getTimestamp("endDate")))>
                                &lt;#&ndash;-Mod by zhajh at 20160413 未开始状态判断的修改 End&ndash;&gt;
                                    <#assign activityAuditStatus = "ACTY_AUDIT_UNBEGIN"/>
                                    <#assign activityStatusDesc = "未开始">
                                   </#if>
                                <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.after(partyRow.getTimestamp("activityStartDate")))&& (nowDate.before(partyRow.getTimestamp("activityEndDate")))>
                                    <#assign activityAuditStatus = "ACTY_AUDIT_RUNING"/>
                                    <#assign activityStatusDesc = "进行中"></#if>
                                <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.after(partyRow.getTimestamp("activityEndDate")))>
                                    <#assign activityAuditStatus = "ACTY_AUDIT_FINISH"/><#assign activityStatusDesc = "已结束"></#if>
                                <#if (partyRow.activityAuditStatus?default("")== 'ACTY_AUDIT_PASS') && (nowDate.after(partyRow.getTimestamp("activityEndDate")))>
                                    <#assign activityAuditStatus = "ACTY_AUDIT_REMOVE"/><#assign activityStatusDesc = "已下架">
                                </#if>-->
                            ${activityStatusDesc}
                           <#-- <td><#if partyRow.containsKey("hasGroup")>${partyRow.hasGroup?default("0")}</#if></td>
                            <td><#if partyRow.containsKey("hasBuyQuantity")>${partyRow.hasBuyQuantity?default("0")}</#if></td>-->


                            <td> &nbsp;&nbsp;
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("PRODPROMO_GROUPORDER", "_DETAIL", session)>
                                        <button type="button" class="btn btn-danger btn-sm" onclick="activityDetail('${partyRow.activityId?default("N/A")}')">查看</button>
                                    </#if>

                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <#assign updatePriceFlg=''>
                                    <#if partyRow.isUpdatePrice?has_content>你
                                      <#assign updatePriceFlg=partyRow.isUpdatePrice>
                                    </#if>

                                    <#if ((!((activityAuditStatus == 'ACTY_AUDIT_REMOVE'|| activityAuditStatus == 'ACTY_AUDIT_FINISH'))) ||
                                         (partyRow.activityAuditStatus == 'ACTY_AUDIT_INIT')||
                                         (activityAuditStatus == 'ACTY_AUDIT_INIT'||activityAuditStatus == 'ACTY_AUDIT_NOPASS'||activityAuditStatus == 'ACTY_AUDIT_PUBING')||
                                         ((activityAuditStatus == 'ACTY_AUDIT_REMOVE'||activityAuditStatus == 'ACTY_AUDIT_FINISH')&& updatePriceFlg=='Y'))>

                                    <ul class="dropdown-menu" role="menu">
                                        <#if (security.hasEntityPermission("PRODPROMO_GROUPORDER", "_UPDATE", session) &&  (activityAuditStatus == 'ACTY_AUDIT_PASS'||activityAuditStatus=='ACTY_AUDIT_NOPASS'))>
                                            <li><a href="<@ofbizUrl>editGroupOrder?activityId=${partyRow.activityId}</@ofbizUrl>">编辑</a></li>
                                        </#if>

                                        <#if (security.hasEntityPermission("PRODPROMO_GROUPORDER", "_UPDATE", session) && (partyRow.activityAuditStatus == 'ACTY_AUDIT_INIT'))>
                                            <li><a href="#" onclick="auditGroupOrder('${partyRow.activityId}','${partyRow.activityCode}','${partyRow.activityName}')">审批</a></li>
                                        </#if>
                                        <#if security.hasEntityPermission("PRODPROMO_GROUPORDER", "_DELETE", session)&& partyRow.hasBuyQuantity?default("0")==0 >
                                            <li><a href="#" onclick="deleteGroupOrder(${partyRow.activityId})">删除</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_GROUPORDER", "_UPDATE", session) &&  (activityAuditStatus == 'ACTY_AUDIT_PASS'))>
                                            <li><a href="javascript:void(0)"  onclick="editEndDate('${partyRow.activityId}','${partyRow.activityEndDate?string("yyyy-MM-dd HH:mm:ss")}')">编辑结束时间</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_GROUPORDER", "_LIST", session))>
                                            <li><a href="javascript:void(0)" onclick="auditLog(${partyRow.activityId})">操作日记</a></li>
                                        </#if>
                                    </ul>
                                    </#if>
                                </div>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "findGroupOrder?"+ paramList + "&"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(groupListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", groupListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=groupListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
        <!-- 分页条end -->
    <#else>
        <div class="row">
            <div class="col-sm-10">
                <h3>查询无数据</h3>
            </div>
        </div>
    </#if>
    </div>
</div>

<div class="modal fade" id="editEndDate_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">结束时间编辑</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal"  method="post">
                    <div class="row">
                        <div id="endGroup">
                            <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>
                            <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15"
                                 data-link-field="thruDate">
                                <input class="form-control" size="16" type="text" id="activityEndDate" readonly>
                                <input id="activityEndDate1" class="dp-vd" type="hidden" name="endDate">
                                <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                            </div>
                            <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                        </div>
                    </div>

                    <input type="hidden" class="form-control dp-vd w-p50" id="productPromoId"
                           name="productPromoId" value="N">
                    <!-- 标签备注 end-->
                    <div class="modal-footer" style="text-align:center;">
                        <button type="button" id="btn_EndDate" class="btn btn-primary">确定</button>
                        &nbsp;&nbsp;&nbsp;
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>


<!-- 提示弹出框start -->
<div id="modal_audit" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_audit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">拼团审批</h4>
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

<!-- 审核弹出框start -->
<div id="modal_auditGroupOrder" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">拼团审核</h4>
            </div>
            <div class="modal-body">
                <form id="ApprovalForm" method="post" class="form-horizontal"
                      action="<@ofbizUrl>approvalArticle</@ofbizUrl>">
                    <div class="form-group">
                        <label class="control-label col-sm-2">活动编码:</label>
                        <div class="col-sm-8">
                            <input type="hidden" class="form-control" id="activityId">
                            <input type="text" class="form-control" id="activityCode" name="activityCode" readonly>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">活动名称:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="activityName" name="activityName" readonly>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group" data-type="minCheck" data-number="1" data-mark="操作">
                            <label class="control-label col-sm-2">操作:</label>
                            <div class="col-sm-10">
                                <div class="radio">
                                    <label class="col-sm-4"><input name="activityStatus" type="radio"
                                                                   value="ACTY_AUDIT_PASS" checked
                                                                   class="radioItem">通过</label>
                                    <label class="col-sm-4"><input name="activityStatus" type="radio"
                                                                   value="ACTY_AUDIT_NOPASS" class="radioItem">拒绝</label>
                                    <div class="dp-error-msg"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!--审核意见Start-->
                    <div class="row">
                        <div class="form-group" data-type="required,max" data-mark="审核意见" data-number="50">
                            <label for="title" class="col-sm-2 control-label" id="label_gss"><i class="required-mark">*</i>审核意见:</label>
                            <div class="col-sm-8">
                                        <textarea id="auditMessage" class="form-control  dp-vd" name="auditMessage" rows="3" style="resize: none;"></textarea>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!--审核意见end-->
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">确定</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
        </form>
    </div>
</div><!--审核弹出框start -->



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
                            <h3 class="box-title">拼团活动基本信息</h3>
                        </div>
                        <div class="box-body">
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
                                    <label for="title" class="col-sm-5 control-label">活动编码:</label>
                                    <div class="col-sm-7" id="d_activityCode">
                                    </div>
                                </div>
                                <div class="form-group col-sm-6" data-type="required" data-mark="活动名称">
                                    <label for="subTitle" class="col-sm-5 control-label">活动名称:</label>
                                    <div class="col-sm-7" id="d_activityName"  style="word-wrap: break-word">
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

                            <div class="row">
                                <div class="form-group col-sm-12">
                                    <label class="col-sm-2 control-label">拼团标签:</label>
                                    <div class="col-sm-10">
                                            <label class="col-sm-3" title="随时退" id="d_isAnyReturn">随时退</label>
                                            <label class="col-sm-3" title="支持过期退" id="d_isSupportOverTimeReturn">支持过期退</label>
                                            <label class="col-sm-3" title="动可积分" id="d_isSupportScore">活动可积分</label>
                                            <label class="col-sm-3" title="退货返回积分" id="d_isSupportReturnScore">退货返回积分</label>
                                            <label class="col-sm-3" title="推荐到首页" id="d_isShowIndex">推荐到首页</label>
                                            <label class="col-sm-3" title="包邮" id="d_isPostageFree">包邮</label>
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

                            <div class="row p-l-10 p-r-10">
                                <div class="from-group">
                                    <div class="col-sm-12" id="d_activityDesc">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="box box-info" id="product">
                        <div class="box-header">
                            <h3 class="box-title">参与商品</h3>
                        </div>
                        <div class="box-body table-responsive no-padding">
                            <div class="table-responsive no-padding">
                                <table class="table table-hover js-checkparent js-sort-list addProducts"
                                       id="productTable">
                                    <thead>
                                    <tr>
                                        <th>商品规格</th>
                                        <th>商品编码</th>
                                        <th>商品名称</th>
                                        <th>拼团人数</th>
                                        <th>拼团金额</th>
                                    </tr>
                                    </thead>
                                    <tbody></tbody>
                                </table>
                            </div>
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
            CKEDITOR.replace("Centent");
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

            $(".radioItem").change(function () {
                var activityStatus = $("#modal_auditGroupOrder input[name='activityStatus']:checked").val();
               /* if (activityStatus == 'ACTY_AUDIT_NOPASS') {
                    $('#label_gss').html('<i class="required-mark">*</i>审核意见:');
                    $('#modal_auditGroupOrder #auditMessage').addClass('dp-vd');
                } else {
                    $('#label_gss').html('审核意见:');
                    $('#modal_auditGroupOrder #auditMessage').removeClass('dp-vd');
                }*/
            })
        });
        // 状态值切换
        function changeTab(activityAuditStatus) {
            $("#activityAuditStatus").val(activityAuditStatus);

            $("#QueryForm").submit();
        }
        function activityDetail(id) {
            $.ajax({
                url: "productActivityDetail",
                type: "POST",
                data: {activityId: id},
                dataType: "json",
                success: function (data) {
                    if (data) {
                        $('#d_activityAuditStatusName').text(data.d_activityAuditStatusName);
                        $('#d_activityTypeName').text("拼团");//data.d_activityTypeName
                        $('#d_activityCode').text(data.d_activityCode);
                        $('#d_activityName').text(data.d_activityName);
                        $('#d_activityStartDate').text(data.d_activityStartDate);
                        $('#d_activityEndDate').text(data.d_activityEndDate);
                        $('#d_limitQuantity').text(data.d_limitQuantity);
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
                            $('#d_shipmentTypeDiv').show();
                        }else{
                            $('#virtualProductDateDiv').show();
                            $('#d_shipmentTypeDiv').hide();
                        }
                        CKEDITOR.instances.Centent.setData(data.d_activityDesc);
                        $('#productTable>tbody').empty();
                        var productList = data.productList;
                        for(var i=0;i<productList.length;i++){
                            var product = productList[i];
                            var productId = product.productId;
                            var activityPersonNum =product.activityPersonNum;
                            var activityPrice =product.activityPrice;
                            var tr = '<tr id="' + productId + '">'
                                    + '<td>' + product.productGoodFeature + '</td>'
                                    + '<td>' + productId + '</td>'
                                    + '<td>' + product.productName + '</td>'
                                    + '<td>' + activityPersonNum + '</td>'
                                    + '<td>' + activityPrice + '</td>'
                                    + '</tr>';
                            $('#productTable>tbody').append(tr);
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

        function auditLog(productPromoId){
            $.dataSelectModal({
                url: "auditLogPage?businessId=go_"+productPromoId,
                width: "800",
                title: "操作日志"
            });
        }
        var endActivityId="";
        function  editEndDate( activityId, activityEndDate) {
            $('#editEndDate_Modal #activityEndDate').val(activityEndDate);
            $('#editEndDate_Modal #activityEndDate1').val(activityEndDate);
            $('#editEndDate_Modal').modal('show');
            endActivityId=activityId;
        }

        $('#btn_EndDate').click(function () {
            var data ={
                activityId:endActivityId,
                activityEndDate:$("#editEndDate_Modal #activityEndDate").val()
            }
            $.ajax({
                url: "editTogetherEndDate",
                type: "POST",
                async: false,
                data: data ,
                dataType: "json",
                success: function (data) {
                    if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                        $.tipLayer(data._ERROR_MESSAGE_);
                    }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                        $.tipLayer(data._ERROR_MESSAGE_LIST_);
                    } else {
                        if(data.chkFlg=="Y"){
                            //隐藏新增弹出窗口
                            $('#editEndDate_Modal').modal('toggle');
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                            $('#modal_msg').modal('show');
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').off('hide.bs.modal');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })
                        }else{
                            $('#editEndDate_Modal').modal('toggle');
                            $('#modal_msg #modal_msg_body').html(data.errorMsg);
                            $('#modal_msg').modal('show');
                        }

                    }
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作失败！");
                    $('#modal_msg').modal();
                }
            });
        });
        function deleteGroupOrder(id) {
            $.confirmLayer({
                msg: '确定删除该拼团信息',
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
                            /*$('#confirmLayer').modal('hide');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })*/
                        }

                    })
                }
            })

        }


        function shutDownGroupOrder(id) {

            $.confirmLayer({
                msg: '确定下架该拼团',
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

        function auditGroupOrder(activityId,activityCode,activityName) {
            //审核之前判断里面的商品是否已经被添加了。
            //获取这个促销下的所有商品id。
            var productIds="";
            var startDate="";
            var endDate="";
            $.ajax({
                url: "findProductIdsByActivityId${externalKeyParam}",
                type: "POST",
                async: false,
                data: {
                    activityId: activityId
                },
                dataType: "json",
                success: function (data) {
                    productIds=data.productIds;
                    startDate=data.startDate;
                    endDate=data.endDate;
                },
                error: function (data) {
                    $.tipLayer("操作失败！");
                    return;
                }
            });

            if((!startDate)||(!endDate)) {
                $.tipLayer("开始结束时间不正确");
                return;
            }

            var chkFlg=false;
            var existProductIds ="";
            var errorMsg="";
            //校验
            $.ajax({
                url: "/catalog/control/chkPromProcutIsValid${externalKeyParam}",
                type: "POST",
                async: false,
                data: {
                    productIds: productIds,
                    startDate: startDate,
                    endDate: endDate
                },
                dataType: "json",
                success: function (data) {
                    errorMsg=data.errorMsg;
                    chkFlg=data.chkFlg;
                    existProductIds=data.existProductIds;
                },
                error: function (data) {
                    $.tipLayer("操作失败！");
                    return;
                }
            });

            if(chkFlg=="N"){
                $.tipLayer(errorMsg);
                return;
            }

            $("#modal_auditGroupOrder #activityCode").val(activityCode)
            $("#modal_auditGroupOrder #activityId").val(activityId)
            $("#modal_auditGroupOrder #activityName").val(activityName)
            $('#modal_auditGroupOrder').modal('show');
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

        //单个保存审核按钮点击事件
        $('#modal_auditGroupOrder #save').click(function () {
            $('#ApprovalForm').dpValidate({
                clear: true
            });
            $('#modal_auditGroupOrder #ApprovalForm').submit();
        });

        //编辑表单校验
        $('#modal_auditGroupOrder #ApprovalForm').dpValidate({
            validate: true,
            callback: function () {
                $.ajax({
                    url: "auditGroupOrder",
                    type: "POST",
                    data: {
                        activityAuditStatus : $("input[name='activityStatus']:checked").val(),
                        auditMessage:$('#modal_auditGroupOrder #auditMessage').val(),
                        activityId:$('#modal_auditGroupOrder #activityId').val(),
                    },
                    dataType: "json",
                    success: function (data) {
                        if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                            $.tipLayer(data._ERROR_MESSAGE_);
                        }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                            $.tipLayer(data._ERROR_MESSAGE_LIST_);
                        } else {
                            //设置提示弹出框内容
                            $('#modal_auditPromo').modal('toggle');
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                            $('#modal_msg').modal();
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').off('hide.bs.modal');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })
                        }


                    },
                    error: function (data) {
                        //隐藏新增弹出窗口
                        $('#modal_auditPromo').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

    </script>