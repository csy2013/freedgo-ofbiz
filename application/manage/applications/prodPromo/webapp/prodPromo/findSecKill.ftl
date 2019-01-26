<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>"
      type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/bootstrap/main.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "findSecKill?lookupFlag=Y&"+ paramList +"&">
<#assign lookupFlag = "Y"/>
<#assign activityAuditStatus = activityAuditStatus?default('')/>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form class="form-inline clearfix" role="form" method="post" id="QueryForm"  action="<@ofbizUrl>findSecKill</@ofbizUrl>">
            <input type="hidden" name="activityAuditStatus" id="activityAuditStatus" value="">
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group w-p100">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">促销名称</span>
                    <input type="text" class="form-control" name="activityName" placeholder="促销名称"  value="${activityName?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">促销编码</span>
                    <input type="text" class="form-control" name="activityCode" placeholder="促销编码"  value="${activityCode?default("")}">
                </div>
                <div class="input-group pull-right p-l-10">
                <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_VIEW", session)>
                    <button class="btn btn-success btn-flat">搜索</button>
                </#if>
                </div>
            </div>
        </form>

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>

        <!-- 分割线end -->
       <div class="row m-b-12" style="margin-bottom:15px;">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_DELETE",session)>
                        <button id="btn_add" class="btn btn-primary">
                            <i class="fa fa-plus"></i>添加
                        </button>
                    </#if>
                   <#-- <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_DELETE", session)>
                        <button class="btn btn-primary" id="btnProductSeckillDel">
                            <i class="fa fa-trash"></i>删除
                        </button>
                    </#if>-->

                    <#--<#if (security.hasEntityPermission("PRODUCT_GROUP", "_UPDATE", session))&&(activityAuditStatus=='ACTY_AUDIT_INIT')>-->
                        <#--<button id="btn_approval" class="btn btn-primary">-->
                             <#--<i class="fa">批量通过</i>-->
                        <#--</button>-->
                        <#--<button id="btn_resolute" class="btn btn-primary">-->
                             <#--<i class="fa">批量驳回</i>-->
                        <#--</button>-->
                    <#--</#if>-->
                </div>
            </div>
        </div>

            <!--  <#if lookupFlag == "Y">-->
        <ul class="nav nav-tabs">
            <li role="presentation" <#if activityAuditStatus?default("") == "">class="active"</#if>><a
                    href="<@ofbizUrl>findSecKill?lookupFlag=Y</@ofbizUrl>">全部</a></li>
            <li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_INIT">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_INIT')">待审核</a></li>
            <li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_PASS">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_PASS')">已通过</a></li>
            <li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_NOPASS">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_NOPASS')">已驳回</a></li>
        </ul>
            <!--</#if>-->
        <!-- 表格区域start -->
    <#if groupList?has_content>
        <!-- 列表当前分页条数start -->
        <div class="row m-b-12">
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
                <table class="table table-bordered table-hover js-checkparent" id="prom_table">
                    <thead>
                    <tr class="js-sort-list">
                        <#--<#if activityAuditStatus=='ACTY_AUDIT_INIT'>-->
                            <th><input class="js-allcheck" type="checkbox"></th>
                        <#--</#if>-->
                        <th>促销编码</th>
                        <th>促销名称</th>
                        <th>开始时间</th>
                        <th>结束时间</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list groupList as promo>
                        <tr>
                            <#--<#if activityAuditStatus=='ACTY_AUDIT_INIT' >-->
                                <td><input class="js-checkchild js-chk" type="checkbox"
                                           value="${promo.activityId?if_exists}"></td>
                            <#--</#if>-->
                            <td>${promo.activityCode?if_exists}</td>
                            <td>${promo.activityName?if_exists}</td>
                            <td>${promo.activityStartDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${promo.activityEndDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>
                                <#if promo.activityAuditStatus=='ACTY_AUDIT_INIT'>待审核
                                <#elseif promo.activityAuditStatus=='ACTY_AUDIT_PASS'>已通过
                                <#elseif promo.activityAuditStatus=='ACTY_AUDIT_NOPASS'>已驳回
                                </#if>
                            </td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_VIEW", session)>
                                        <button type="button" class="btn btn-danger btn-sm" onclick="productSeckillDetail('${promo.activityId?default("N/A")}')">查看
                                        </button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session) && (promo.activityAuditStatus=='ACTY_AUDIT_NOPASS'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="editProductSeckill(${promo.activityId})">编辑</a></li>
                                        </#if>

                                        <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session) && (promo.activityAuditStatus=='ACTY_AUDIT_INIT'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="auditProductSeckill(${promo.activityId})">审核</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session) && (promo.activityAuditStatus=='ACTY_AUDIT_PASS'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="editEndDate(${promo.activityId},'${promo.activityEndDate?string("yyyy-MM-dd HH:mm:ss")}','${promo.activityStartDate?string("yyyy-MM-dd HH:mm:ss")}')">编辑结束时间</a>
                                            </li>
                                            <li><a href="javascript:void(0)"
                                                   onclick="editProductSeckill(${promo.activityId},'Y')">编辑</a></li>

                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_DELETE", session))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="deleteSecKill(${promo.activityId})">删除</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_VIEW", session) && (promo.activityAuditStatus=='ACTY_AUDIT_NOPASS'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="findProductSeckillAuditMessage('${promo.activityId}')">查看驳回原因</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_LIST", session))>
                                            <li><a href="javascript:void(0)" onclick="auditLog(${promo.activityId})">操作日记</a></li>
                                        </#if>
                                    </ul>
                                </div>
                            </td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "findSecKill?lookupFlag=Y&"+ paramList + "&"/>
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
        <#if lookupFlag == "Y">
            <div id="findPartyResults_2" class="col-sm-12">
                <h3>没有数据</h3>
            </div>
        </#if>
    </#if>


        <!-- 提示弹出框start -->
        <div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
             aria-labelledby="modal_msg_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
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

        <!--编辑结束时间start-->
        <div class="modal fade" id="editEndDate_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel">商品秒杀结束时间编辑</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="editEndDateForm" action="<@ofbizUrl>createTag</@ofbizUrl>"
                              method="post">

                            <div class="row" hidden>
                                <div class="form-group col-sm-6" data-type="linkLt" id="fromDateGroup"
                                     data-compare-link="endGroup" data-mark="开始时间" data-compare-mark="结束时间">
                                    <label for="publishDate" class="col-sm-3 control-label"><i
                                            class="required-mark">*</i>开始时间</label>

                                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15"
                                         data-link-field="fromDate">
                                        <input class="form-control" id="startDate" size="16" type="text" readonly>
                                        <input id="fromDate" class="dp-vd" type="hidden" name="fromDate">
                                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                    </div>
                                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                                </div>
                            </div>
                            <div class="row">
                                <div id="endGroup" class="form-group col-sm-10" data-type="linkGt"
                                     data-compare-link="fromDateGroup" data-mark="结束时间" data-compare-mark="开始时间">
                                    <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>
                                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15"
                                         data-link-field="thruDate">
                                        <input class="form-control" size="16" type="text" id="endDate" readonly>
                                        <input id="thruDate" class="dp-vd" type="hidden" name="endDate">
                                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                    </div>
                                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                                </div>
                            </div>

                            <input type="hidden" class="form-control dp-vd w-p50" id="productSkId"
                                   name="productSkId" value="N">
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
        <!--编辑结束时间 end-->

        <!-- 删除确认弹出框start -->
        <div id="modal_confirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
             aria-labelledby="modal_confirm_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_confirm_title">操作提示</h4>
                    </div>
                    <div class="modal-body">
                        <h4 id="modal_confirm_body"></h4>
                    </div>
                    <div class="modal-footer">
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">删除</button>
                    </div>
                </div>
            </div>
        </div><!-- 删除确认弹出框end -->


        <!-- 批量删除确认弹出框start -->
        <div id="modal_confirm_batch_del" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
             aria-labelledby="modal_confirm_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_confirm_title">操作提示</h4>
                    </div>
                    <div class="modal-body">
                        <h4 id="modal_confirm_body"></h4>
                    </div>
                    <div class="modal-footer">
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">删除</button>
                    </div>
                </div>
            </div>
        </div><!-- 批量删除确认弹出框end -->


        <#--<!-- 批量通过弹出框start &ndash;&gt;-->
        <#--<div id="modal_approval" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">-->
            <#--<div class="modal-dialog" role="document">-->
                <#--<div class="modal-content">-->
                    <#--<div class="modal-header">-->
                        <#--<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span-->
                                <#--aria-hidden="true">&times;</span></button>-->
                        <#--<h4 class="modal-title" id="modal_add_title">秒杀商品-批量通过</h4>-->
                    <#--</div>-->
                    <#--<div class="modal-body">-->
                        <#--<form id="Approval_Form" method="post" class="form-horizontal" role="form" action="">-->

                        <#--</form>-->
                    <#--</div>-->
                    <#--<div class="modal-footer">-->
                        <#--<button id="save_approval" type="button" class="btn btn-primary">确定</button>-->
                        <#--<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>-->
                    <#--</div>-->
                <#--</div>-->
            <#--</div>-->
        <#--</div><!-- 批量通过弹出框end &ndash;&gt;-->


        <#--<!-- 批量拒绝弹出框start  &ndash;&gt;-->
        <#--<div id="modal_resolute" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">-->
            <#--<div class="modal-dialog" role="document">-->
                <#--<div class="modal-content">-->
                    <#--<div class="modal-header">-->
                        <#--<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span-->
                                <#--aria-hidden="true">&times;</span></button>-->
                        <#--<h4 class="modal-title" id="modal_add_title">秒杀商品-批量拒绝</h4>-->
                    <#--</div>-->
                    <#--<div class="modal-body">-->
                        <#--<form id="ResoluteForm" method="post" class="form-horizontal" role="form" action="">-->

                        <#--</form>-->
                    <#--</div>-->
                    <#--<div class="modal-footer">-->
                        <#--<button id="save_resolute" type="button" class="btn btn-primary">确定</button>-->
                        <#--<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>-->
                    <#--</div>-->
                <#--</div>-->
            <#--</div>-->
        <#--</div><!-- 批量通过弹出框end &ndash;&gt;-->


        <!-- 驳回原因弹出框start -->
        <div id="modal_NoPassconfirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
             aria-labelledby="modal_confirm_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_confirm_title">驳回原因</h4>
                    </div>
                    <div class="modal-body">
                        <h4 id="modal_confirm_body"></h4>
                    </div>
                    <div class="modal-footer" style="text-align: center;">
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">确定</button>
                    </div>
                </div>
            </div>
        </div><!-- ${uiLabelMap.BrandDel}确认弹出框end -->
        <!-- 审核弹出框start -->
        <div id="modal_audit_productSeckill" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_add_title">秒杀商品审核</h4>
                    </div>
                    <div class="modal-body">
                        <form id="ApprovalForm" method="post" class="form-horizontal"
                              action="<@ofbizUrl>approvalArticle</@ofbizUrl>">
                            <div class="form-group">
                                <label class="control-label col-sm-2">秒杀编码:</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="activityCode" name="activityCode" readonly>
                                    <input type="hidden" class="form-control" id="activityId" name="activityId">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-2">秒杀名称:</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="activityName" name="activityName" readonly>
                                </div>
                            </div>

                            <div class="row">
                                <div class="form-group" data-type="minCheck" data-number="1" data-mark="操作">
                                    <label class="control-label col-sm-2">操作:</label>
                                    <div class="col-sm-10">
                                        <div class="radio">
                                            <label class="col-sm-4"><input name="activityAuditStatus" type="radio"
                                                                           value="ACTY_AUDIT_PASS" checked
                                                                           class="radioItem">通过</label>
                                            <label class="col-sm-4"><input name="activityAuditStatus" type="radio"
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
                                        <textarea id="auditMessage" class="form-control   dp-vd" name="auditMessage" rows="3" style="resize: none;"></textarea>
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


        <!--秒杀商品详情弹出框start -->
        <div id="modal_detail_secKill" class="modal fade  bs-example-modal-lg" tabindex="-1" role="dialog"
             aria-labelledby="modal_edit_title">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_edit_title">秒杀商品详细信息</h4>
                    </div>
                    <div class="modal-body">

                        <div class="box box-info" id="">
                            <div class="box-header">
                                <h3 class="box-title">基本信息</h3>
                            </div>
                            <div class="box-body table-responsive no-padding">

                            </div>
                        </div>
                        <div class="row title">
                            <div class="form-group col-sm-3">
                                <label for="subTitle" class="col-sm-6 control-label">促销编码:</label>
                                <div class="col-sm-3">
                                    <span id="promoCode"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-4">
                                <label for="subTitle" class="col-sm-6 control-label">秒杀名称:</label>
                                <div class="col-sm-6">
                                    <span id="promoName"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-5">
                                <label for="subTitle" class="col-sm-4 control-label">开始时间:</label>
                                <div class="col-sm-7">
                                     <span id="fromDate"><span>
                                </div>
                            </div>
                        </div>

                        <div class="row title">
                            <div class="form-group col-sm-5">
                                <label for="subTitle" class="col-sm-4 control-label">结束时间:</label>
                                <div class="col-sm-7">
                                     <span id="thruDate"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-5">
                                <label for="subTitle" class="col-sm-5 control-label">单个ID限购数量:</label>
                                <div class="col-sm-7">
                                     <span id="limitQuantity"><span>
                                </div>
                            </div>
                        </div>

                        <div class="row title" style="display: none">
                            <div class="form-group col-sm-10">
                                <label for="subTitle" class="col-sm-2 control-label">促销描述:</label>
                                <div class="col-sm-10">
                                    <div class="box-body pad">
                                        <textarea id="Centent" name="Centent" value=""></textarea>
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
                                                <th>商品图片</th>
                                                <th>商品编码</th>
                                                <th>商品名称</th>
                                                <th>规格</th>
                                                <th>数量</th>
                                                <th>销售价</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        <div class="modal-footer">
                            <button id="cancel" type="button" class="btn btn-default" j
                                    data-dismiss="modal">取消
                            </button>
                        </div>
                    </div>
                </div>
            </div><!-- 满减详情弹出框end -->


            <script>
                var deleteId = '';
                var batchDeleteIds='';
                $(function () {
                    CKEDITOR.replace("Centent");
                    CKEDITOR.on('instanceReady', function (event) {
                        editor = event.editor;
                        editor.setReadOnly(true); //只读
                    });
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


                    // 批量删除秒杀商品
                    $("#btnProductSeckillDel").click(function () {
                        var selectedIds=getSelectedIds();
                        if (selectedIds!="") {
                            batchDeleteIds = selectedIds;
                            $('#modal_confirm_batch_del #modal_confirm_body').html("确定删除所选秒杀商品吗？");
                            $('#modal_confirm_batch_del').modal('show');
                        } else {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("请选择需要删除的秒杀商品");
                            $('#modal_msg').modal();
                        }
                    });
                    //批量删除弹出框删除按钮点击事件
                    $('#modal_confirm_batch_del #ok').click(function (e) {
                        //异步调用删除方法
                        deleteSecKillByIds(batchDeleteIds);
                    });

                    // 添加按钮
                    $("#btn_add").click(function(){
                        window.location.href = "<@ofbizUrl>addSecKill</@ofbizUrl>";
                    });

                    //删除弹出框删除按钮点击事件
                    $('#modal_confirm #ok').click(function (e) {
                        //异步调用删除方法
                        deleteSecKillByIds(deleteId);
                    });

                    //单个保存审核按钮点击事件
                    $('#modal_audit_productSeckill #save').click(function () {
                        $('#ApprovalForm').dpValidate({
                            clear: true
                        });
                        $('#ApprovalForm').submit();
                    });


                    //编辑表单校验
                    $('#ApprovalForm').dpValidate({
                        validate: true,
                        callback: function () {
                            $.ajax({
                                url: "auditGroupOrder",
                                type: "POST",
                                data: $('#ApprovalForm').serialize(),
                                dataType: "json",
                                success: function (data) {
                                    //隐藏新增弹出窗口
                                    $('#modal_audit_productSeckill').modal('toggle');
                                    //设置提示弹出框内容
                                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                                    $('#modal_msg').modal();
                                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                    $('#modal_msg').off('hide.bs.modal');
                                    $('#modal_msg').on('hide.bs.modal', function () {
                                        window.location.reload();
                                    })
                                },
                                error: function (data) {
                                    //隐藏新增弹出窗口
                                    $('#modal_audit_productSeckill').modal('toggle');
                                    //设置提示弹出框内容
                                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                                    $('#modal_msg').modal();
                                }
                            });
                        }
                    });


                    // 审核意见切换
                    $(".radioItem").change(function () {
                        var activityAuditStatus = $("input[name='pass']:checked").val();
                       /* if (activityAuditStatus == 'ACTY_AUDIT_NOPASS') {
                            $('#label_gss').html('<i class="required-mark">*</i>审核意见:');
                            $('#modal_audit_productSeckill #auditMessage').addClass('dp-vd');
                        } else {
                            $('#label_gss').html('审核意见:');
                            $('#modal_audit_productSeckill #auditMessage').removeClass('dp-vd');
                        }*/
                    });


                    //编辑结束时间提交按钮点击事件
                    $('#btn_EndDate').click(function () {
                        $('#editEndDateForm').dpValidate({
                            clear: true
                        });
                        $('#editEndDateForm').submit();
                    });
                    //编辑表单校验
                    $('#editEndDateForm').dpValidate({
                        validate: true,
                        callback: function () {
                            $.ajax({
                                url: "editEndDateSecKill",
                                type: "POST",
                                data: $('#editEndDateForm').serialize(),
                                dataType: "json",
                                success: function (data) {
                                    <#--//隐藏新增弹出窗口-->
                                    <#--$('#editEndDate_Modal').modal('toggle');-->
                                    <#--//设置提示弹出框内容-->
                                    <#--$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");-->
                                    <#--$('#modal_msg').modal('show');-->
                                    <#--//提示弹出框隐藏事件，隐藏后重新加载当前页面-->
                                    <#--$('#modal_msg').off('hide.bs.modal');-->
                                    <#--$('#modal_msg').on('hide.bs.modal', function () {-->
                                        <#--window.location.reload();-->
                                    <#--})-->
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
                                },
                                error: function (data) {
                                    //隐藏新增弹出窗口
                                    $('#add_Modal').modal('toggle');
                                    //设置提示弹出框内容
                                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                                    $('#modal_msg').modal();
                                }
                            });
                        }
                    });



                    //批量通过按钮点击事件
                    $('#btn_approval').click(function () {
                        var checks = $('#prom_table .js-checkchild:checked');
                        //判断是否选中记录
                        var c = '';
                        var tr = checks.closest('tr');
                        if (checks.size() > 0) {
                            //编辑id字符串
                            tr.each(function () {
                                var productGrpId = $(this).find('td').eq(0).find("input[type='checkbox']").val();
                                var productCode = $(this).find('td').eq(1).text();
                                var proPromName = $(this).find('td').eq(2).text();
                                c += '<div class="form-group">'
                                        + '<label class="control-label col-sm-2">促销编码:</label>'
                                        + '<div class="col-sm-10 p-t-5">'
                                        + '<span>' + productCode + '</span>'
                                        + '</div>'
                                        + '</div>'
                                        + '<div class="form-group">'
                                        + '<label class="control-label col-sm-2">秒杀名称:</label>'
                                        + '<div class="col-sm-10 p-t-5">'
                                        + '<span>' + proPromName + '</span>'
                                        + '</div>'
                                        + '</div>'
                                        + '<div class="form-group">'
                                        + '<label class="control-label col-sm-2">审核意见:</label>'
                                        + '<div class="col-sm-8">'
                                        + '<textarea id=""  data-id="' + productGrpId + '" name="" class="form-control" rows="3" style="resize: none;"></textarea>'
                                        + '</div>'
                                        + '</div>'
                                        + '<div class="cut-off-rule bg-gray">'
                                        + '</div>'

                            });
                            $('#modal_approval .modal-body .form-horizontal').html(c);
                            $('#modal_approval').modal();
                        } else {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html
                            ("${uiLabelMap.MustSelectOne}");
                            $('#modal_msg').modal();
                        }
                    });

                    //批量通过提交按钮点击事件
                    $('#save_approval').click(function () {
                        var textarea = $('#modal_approval').find('textarea');
                        var obj = '';
                        textarea.each(function () {
                            var id = $(this).data("id");
                            var value = $(this).val();
                            obj += id + ':' + value + ',';
                        });
                        $.ajax({
                            url: "batchAuditProductSeckill",
                            type: "POST",
                            data: {
                                obj: obj,
                                activityAuditStatus: 'ACTY_AUDIT_PASS'
                            },
                            dataType: "json",
                            success: function (data) {
                                //隐藏新增弹出窗口
                                $('#modal_approval').modal('toggle');
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                                $('#modal_msg').modal();
                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                $('#modal_msg').off('hide.bs.modal');
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.reload();
                                })
                            },
                            error: function (data) {
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                                $('#modal_msg').modal();
                            }
                        });
                    });


                    //批量拒绝按钮点击事件
                    $('#btn_resolute').click(function () {
                        var checks = $('#prom_table .js-checkchild:checked');
                        //判断是否选中记录
                        var c = '';
                        var tr = checks.closest('tr');
                        if (checks.size() > 0) {
                            //编辑id字符串
                            tr.each(function () {
                                var productGrpId = $(this).find('td').eq(0).find("input[type='checkbox']").val();
                                var productCode = $(this).find('td').eq(1).text();
                                var proPromName = $(this).find('td').eq(2).text();
                                c += '<div class="row">'
                                        + '<div class="form-group">'
                                        + '<label class="control-label col-sm-2">促销编码:</label>'
                                        + '<div class="col-sm-10 p-t-5">'
                                        + '<span>' + productCode + '</span>'
                                        + '</div>'
                                        + '</div>'
                                        + '<div class="form-group">'
                                        + '<label class="control-label col-sm-2">促销名称:</label>'
                                        + '<div class="col-sm-10 p-t-5">'
                                        + '<span>' + proPromName + '</span>'
                                        + '</div>'
                                        + '</div>'
                                        + '<div class="form-group" data-type="required" data-mark="审核意见">'
                                        + '<label class="control-label col-sm-2"><i class="required-mark">*</i>审核意见:</label>'
                                        + '<div class="col-sm-8">'
                                        + '<textarea id=""  class="form-control dp-vd" data-id="' + productGrpId + '" name="" class="form-control" rows="3" style="resize: none;"></textarea>'
                                        + '<p class="dp-error-msg"></p>'
                                        + '</div>'
                                        + '</div>'
                                        + '</div>'
                                        + '<div class="cut-off-rule bg-gray">'
                                        + '</div>'
                            });
                            $('#modal_resolute .modal-body .form-horizontal').html(c);
                            $('#modal_resolute').modal();
                        } else {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html
                            ("${uiLabelMap.MustSelectOne}");
                            $('#modal_msg').modal();
                        }
                    });


                    //批量拒绝提交按钮点击事件
                    $('#save_resolute').click(function () {
                        $('#ResoluteForm').dpValidate({
                            clear: true
                        });
                        $('#ResoluteForm').submit();
                    });
                    //表单校验
                    $('#ResoluteForm').dpValidate({
                        validate: true,
                        callback: function () {
                            var textarea = $('#modal_resolute').find('textarea');
                            var obj = '';
                            textarea.each(function () {
                                var id = $(this).data("id");
                                var value = $(this).val();
                                obj += id + ':' + value + ',';
                            });
                            $.ajax({
                                url: "batchAuditProductSeckill",
                                type: "POST",
                                data: {
                                    obj: obj,
                                    activityAuditStatus: 'ACTY_AUDIT_NOPASS'
                                },
                                dataType: "json",
                                success: function (data) {
                                    //隐藏新增弹出窗口
                                    $('#modal_resolute').modal('toggle');
                                    //设置提示弹出框内容
                                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                                    $('#modal_msg').modal();
                                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                    $('#modal_msg').off('hide.bs.modal');
                                    $('#modal_msg').on('hide.bs.modal', function () {
                                        window.location.reload();
                                    })
                                },
                                error: function (data) {
                                    //设置提示弹出框内容
                                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                                    $('#modal_msg').modal();
                                }
                            });
                        }
                    });

                });

                // 取得选择的节点
                function getSelectedIds() {
                    var ids = ""
                   var checks = $('.js-checkchild:checked');
                    // var checks = $('.js-chk:checked');
                    //判断是否选中记录
                    if (checks.size() > 0) {
                        //编辑id字符串
                        checks.each(function () {
                            ids += $(this).val() + ",";
                        });
                        ids = ids.substring(0, ids.length - 1);
                    }
                    return ids;
                }
                // 状态值切换
                function changeTab(activityAuditStatus) {
                    $("#activityAuditStatus").val(activityAuditStatus);
                    $("#QueryForm").submit();
                }

                // 设置结束时间
                function editEndDate(activityId, endDate, startDate) {
                    $('#editEndDate_Modal #endDate').val(endDate);
                    $('#editEndDate_Modal #thruDate').val(endDate);
                    $('#editEndDate_Modal #startDate').val(startDate);
                    $('#editEndDate_Modal #fromDate').val(startDate);
                    $('#editEndDate_Modal #productSkId').val(activityId);
                    $('#editEndDateForm').dpValidate({
                        clear: true
                    });
                    $('#editEndDate_Modal').modal('show');
                }

                // 编辑秒杀商品
                function editProductSeckill(activityId,isPass) {
                    if(isPass) {
                        if (isPass == 'Y') {
                            window.location.href = "<@ofbizUrl>editSecKill?activityId=" + activityId +"&"+"isPass="+isPass+"</@ofbizUrl>";
                        }
                    }else {
                        window.location.href = "<@ofbizUrl>editSecKill?activityId=" + activityId +"</@ofbizUrl>";
                    }
                }
                // 根据秒杀商品ID审核秒杀商品
                function auditProductSeckill(productSkId) {


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
                            activityId: productSkId
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
                            chkFlg=data.chkFlg;
                            existProductIds=data.existProductIds;
                            errorMsg=data.errorMsg;
                        },
                        error: function (data) {
                            $.tipLayer("操作失败！");
                            return;
                        }
                    });

                    if(chkFlg=="N"){
                        $.tipLayer(errorMsg);
                        return;
                    }else{
                        $.ajax({
                            url: "<@ofbizUrl>productSkDetail</@ofbizUrl>",
                            type: "POST",
                            async: false,
                            data: {
                                productSkId: productSkId
                            },
                            dataType: "json",
                            success: function (data) {
                                if (data.productActivity) {
                                    var productActivity = data.productActivity;
                                    $('#modal_audit_productSeckill #activityId').val(productActivity.activityId);
                                    $('#modal_audit_productSeckill #activityName').val(productActivity.activityName);
                                    $('#modal_audit_productSeckill #activityCode').val(productActivity.activityCode);
                                    $('#modal_audit_productSeckill').modal();
                                }
                            },
                            error: function (data) {
                                //隐藏新增弹出窗口
                                $('#modal_audit_productSeckill').modal('toggle');
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                                $('#modal_msg').modal();
                            }
                        });
                    }
                }


                function auditLog(productPromoId){
                    $.dataSelectModal({
                        url: "auditLogPage?businessId=go_"+productPromoId,
                        width: "800",
                        title: "操作日志"
                    });
                }
                // 查看秒杀商品驳回原因
                function findProductSeckillAuditMessage(productSkId) {
                    $.ajax({
                        url: "findProductSkAuditMessage",
                        type: "POST",
                        data: {
                            productSkId: productSkId
                        },
                        dataType: "json",
                        success: function (data) {
                            if (data.curAuditMessage) {
                                var curAuditMessage = data.curAuditMessage;
                                $('#modal_NoPassconfirm #modal_confirm_body').html(curAuditMessage);
                                $('#modal_NoPassconfirm').modal('show');
                            }
                        }
                    });
                }

                // 查看秒杀商品详情
                function productSeckillDetail(productSkId) {
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
                                $('#modal_detail_secKill #promoCode').text(productActivity.activityCode);
                                $('#modal_detail_secKill #promoName').text(productActivity.activityName);
                                // console.log("**"+productActivity.activityStartDate.toString());
                                var time = new Date(productActivity.activityStartDate);
                                console.log(time);

                                $('#modal_detail_secKill #fromDate').text(timeStamp2String(productActivity.activityStartDate.time));
                                $('#modal_detail_secKill #thruDate').text(timeStamp2String(productActivity.activityEndDate.time));
                                $('#modal_detail_secKill #limitQuantity').text(productActivity.limitQuantity);
                                var product_List = data.productSkProductInfoList;
                                $('#productTable>tbody').empty();
                                var tr1 = "";
                                for (var i = 0; i < product_List.length; i++) {

                                    tr1 += "<tr>"
                                    + "<td></td>"
                                    + "<td>" + product_List[i].productInfo.productId + "</td>"
                                    + "<td>" + product_List[i].productInfo.productName + "</td>"
                                    + "<td>" + product_List[i].featureInfo + "</td>"
                                    + "<td>" + product_List[i].activityQuantity + "</td>"
                                    + "<td>" + parseFloat(product_List[i].activityPrice).toFixed(2) + "</td>"
                                    + "</tr>"
                                }
                                $('#productTable>tbody').append(tr1);

                                $('#modal_detail_secKill').modal();
                            }
                        },
                        error: function (data) {
                            //隐藏新增弹出窗口
                            $('#modal_audit_productSeckill').modal('toggle');
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                            $('#modal_msg').modal();
                        }
                    });
                }


                /**
                 * 根据商品秒杀编码删除商品秒杀处理
                 * @param productPromoId
                 */
                function deleteSecKill(productSkId) {
                    deleteId = productSkId;
                    $('#modal_confirm #modal_confirm_body').html("确定删除此商品秒杀吗？");
                    $('#modal_confirm').modal('show');
                }


                /**
                 * 根据秒杀商品编码批量删除秒杀商品
                 * @param ids
                 */
                function deleteSecKillByIds(ids){
                    //异步调用删除方法
                    $.ajax({
                        url: "deleteSecKill",
                        type: "GET",
                        data: {activityId: ids},
                        dataType: "json",
                        success: function (data) {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                            $('#modal_msg').modal();
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').off('hide.bs.modal');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.reload();
                            })
                        },
                        error: function (data) {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                            $('#modal_msg').modal();
                        }
                    });
                }





            </script>


