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
        src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "BusinessBrandReviewTest?lookupFlag=Y&"+ paramList +"&">
<#assign lookupFlag = "Y"/>
<#assign activityAuditStatus = activityAuditStatus?default('')/>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form class="form-inline clearfix" role="form" method="post" id="QueryForm"  action="<@ofbizUrl>findProductBrands</@ofbizUrl>">
            <#--<input type="hidden" name="activityAuditStatus" id="activityAuditStatus" value="">-->
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group w-p100">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">品牌名称</span>
                    <input type="text" class="form-control" name="brandName" placeholder="品牌名称"  >
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">店铺名称</span>
                    <input type="text" class="form-control" name="partyName" placeholder="店铺名称"  >
                </div>
                <div class="input-group pull-right p-l-10">
                <#if security.hasEntityPermission("PARTY_BUSINESS_BRAND", "_VIEW", session)>
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
                    <#--<#if security.hasEntityPermission("PRODPROMO_SECKILL", "_DELETE",session)>-->
                        <#--<button id="btn_add" class="btn btn-primary">-->
                            <#--<i class="fa fa-plus"></i>添加-->
                        <#--</button>-->
                    <#--</#if>-->
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

            <#--<!--  <#if lookupFlag == "Y">&ndash;&gt;-->
        <#--<ul class="nav nav-tabs">-->
            <#--<li role="presentation" <#if activityAuditStatus?default("") == "">class="active"</#if>><a-->
                    <#--href="<@ofbizUrl>BusinessBrandReviewTest?lookupFlag=Y</@ofbizUrl>">全部</a></li>-->
            <#--<li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_INIT">class="active"</#if>><a-->
                    <#--href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_INIT')">待审核</a></li>-->
            <#--<li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_PASS">class="active"</#if>><a-->
                    <#--href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_PASS')">已通过</a></li>-->
            <#--<li role="presentation" <#if activityAuditStatus?default("") == "ACTY_AUDIT_NOPASS">class="active"</#if>><a-->
                    <#--href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_NOPASS')">已驳回</a></li>-->
        <#--</ul>-->
            <#--<!--</#if>&ndash;&gt;-->
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
                        <#--<th><input class="js-allcheck" type="checkbox"></th>-->
                    <#--</#if>-->
                        <th>申请店铺</th>
                        <th>品牌logo</th>
                        <th>品牌名称</th>
                        <th>品牌别名</th>
                        <#--<th>状态</th>-->
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list groupList as promo>
                        <tr>
                        <#--<#if activityAuditStatus=='ACTY_AUDIT_INIT' >-->
                            <#-- 多选框-->
                            <#--<td><input class="js-checkchild js-chk" type="checkbox"-->
                                       <#--value="${promo.activityId?if_exists}"></td>-->
                        <#--</#if>-->
                            <td>${promo.partyName?if_exists}</td>
                            <td>
                                <#if promo.contentId?has_content>
                                    <#assign src='/content/control/stream?contentId='>
                                    <#assign imgsrc = src + promo.contentId/>

                                <input type="hidden" name="contentId" id="contentId" value="${promo.contentId?if_exists}"/>
                                <img height="100" src="${imgsrc}" class="cssImgSmall" alt="" />
                                </#if>
                            </td>
                            <td>${promo.brandName?if_exists}</td>
                            <td>${promo.brandNameAlias?if_exists}</td>
                            <#--<td>${promo.activityStartDate?string("yyyy-MM-dd HH:mm:ss")}</td>-->
                            <#--<td>${promo.activityEndDate?string("yyyy-MM-dd HH:mm:ss")}</td>-->
                            <#--<td>-->
                                <#--<#if promo.activityAuditStatus=='ACTY_AUDIT_INIT'>待审核-->
                                <#--<#elseif promo.activityAuditStatus=='ACTY_AUDIT_PASS'>已通过-->
                                <#--<#elseif promo.activityAuditStatus=='ACTY_AUDIT_NOPASS'>已驳回-->
                                <#--</#if>-->
                            <#--</td>-->
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("PARTY_BUSINESS_BRAND", "_VIEW", session)>
                                        <button type="button" class="btn btn-danger btn-sm" onclick="productSeckillDetail('${promo.productBrandId?default("N/A")}')">查看
                                        </button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#--<#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session) && (promo.activityAuditStatus=='ACTY_AUDIT_NOPASS'))>-->
                                            <#--<li><a href="javascript:void(0)"-->
                                                   <#--onclick="editProductSeckill(${promo.activityId})">编辑</a></li>-->
                                        <#--</#if>-->

                                        <#if (security.hasEntityPermission("PARTY_BUSINESS_BRAND", "_REVIEW", session) && (promo.isDel=='B'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="auditProductSeckill(${promo.productBrandId})">审核</a></li>
                                        </#if>
                                        <#--<#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session) && (promo.activityAuditStatus=='ACTY_AUDIT_PASS'))>-->
                                            <#--<li><a href="javascript:void(0)"-->
                                                   <#--onclick="editEndDate(${promo.activityId},'${promo.activityEndDate?string("yyyy-MM-dd HH:mm:ss")}','${promo.activityStartDate?string("yyyy-MM-dd HH:mm:ss")}')">编辑结束时间</a>-->
                                            <#--</li>-->
                                        <#--</#if>-->
                                        <#if (security.hasEntityPermission("PARTY_BUSINESS_BRAND", "_DELETE", session))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="deleteSecKill(${promo.productBrandId})">删除</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PARTY_BUSINESS_BRAND", "_VIEW", session) && (promo.isDel=='Y'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="findProductSeckillAuditMessage('${promo.productBrandId}')">查看驳回原因</a></li>
                                        </#if>
                                        <#--<#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_LIST", session))>-->
                                            <#--<li><a href="javascript:void(0)" onclick="auditLog(${promo.activityId})">操作日记</a></li>-->
                                        <#--</#if>-->
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
                        <h4 class="modal-title" id="modal_add_title">品牌审核</h4>
                    </div>
                    <div class="modal-body">
                        <form id="ApprovalForm" method="post" class="form-horizontal"
                              action="<@ofbizUrl>approvalArticle</@ofbizUrl>">
                            <div class="form-group">
                                <label class="control-label col-sm-2">品牌名称:</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="brandName" name="brandName" readonly>
                                    <input type="hidden" class="form-control" id="productBrandId" name="productBrandId">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-2">店铺名称:</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="partyName" name="partyName" readonly>
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
                                <div class="form-group" data-type="required" data-mark="审核意见">
                                    <label for="title" class="col-sm-2 control-label" id="label_gss">审核意见:</label>
                                    <div class="col-sm-8">
                                        <textarea id="auditMessage" class="form-control " name="auditMessage"
                                                  class="form-control" rows="3" style="resize: none;"></textarea>
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


        <#-- 查看按钮 根据productBrandId查看品牌详情 start -->
        <div id="modal_detail_secKill" class="modal fade  bs-example-modal-lg" tabindex="-1" role="dialog"
             aria-labelledby="modal_edit_title">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_edit_title">品牌详情</h4>
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
                                <label for="subTitle" class="col-sm-6 control-label">店铺名称:</label>
                                <div class="col-sm-3">
                                    <span id="partyName"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-4">
                                <label for="subTitle" class="col-sm-6 control-label">品牌名称:</label>
                                <div class="col-sm-6">
                                    <span id="brandName"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-5">
                                <label for="subTitle" class="col-sm-4 control-label">品牌别名:</label>
                                <div class="col-sm-7">
                                     <span id="fromDate"><span>
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
            </div>
            <#-- 查看按钮 根据productBrandId查看品牌详情 end -->


            <script>
                var deleteId = '';
                var batchDeleteIds='';
                $(function () {
                    // CKEDITOR.replace("Centent");
                    // CKEDITOR.on('instanceReady', function (event) {
                    //     editor = event.editor;
                    //     editor.setReadOnly(true); //只读
                    // });
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
                                url: "auditProductBrand", //auditGroupOrder
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
                        if (activityAuditStatus == 'ACTY_AUDIT_NOPASS') {
                            $('#label_gss').html('<i class="required-mark">*</i>审核意见:');
                            $('#modal_audit_productSeckill #auditMessage').addClass('dp-vd');
                        } else {
                            $('#label_gss').html('审核意见:');
                            $('#modal_audit_productSeckill #auditMessage').removeClass('dp-vd');
                        }
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
                // function changeTab(activityAuditStatus) {
                //     $("#activityAuditStatus").val(activityAuditStatus);
                //     $("#QueryForm").submit();
                // }

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
                function editProductSeckill(activityId) {
                    window.location.href = "<@ofbizUrl>editSecKill?activityId=" + activityId +"</@ofbizUrl>";
                }

                // 点击审核按钮后 弹出审核操作的页面  根据productBrandId查出需要填充的数据
                function auditProductSeckill(productBrandId) {
                    $.ajax({
                        url: "<@ofbizUrl>findProductBrandById</@ofbizUrl>",
                        type: "POST",
                        data: {
                            productBrandId: productBrandId
                        },
                        dataType: "json",
                        success: function (data) {
                            if (data.groupList) {
                                var groupList = data.groupList;
                                var productBrandId = data.productBrandId;
                                // $('#modal_audit_productSeckill #productBrandId').val(groupList.productBrandId);
                                $('#modal_audit_productSeckill #brandName').val(groupList[0].brandName);
                                $('#modal_audit_productSeckill #partyName').val(groupList[0].partyName);
                                $('#modal_audit_productSeckill #productBrandId').val(productBrandId);
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


                function auditLog(productPromoId){
                    $.dataSelectModal({
                        url: "auditLogPage?businessId=go_"+productPromoId,
                        width: "800",
                        title: "操作日志"
                    });
                }
                // 查看秒杀商品驳回原因

                function findProductSeckillAuditMessage(productBrandId) {
                    $.ajax({
                        url: "findBrandAuditMessage",//findBrandAuditMessage
                        type: "POST",
                        data: {
                            productBrandId: productBrandId
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

                // 根据productBrandId查看品牌
                function productSeckillDetail(productBrandId) {
                    jQuery.ajax({
                        url: '<@ofbizUrl>findProductBrandById</@ofbizUrl>',
                        type: 'POST',
                        data: {
                            'productBrandId':productBrandId
                        },
                        success: function(data){
                            if (data.groupList) {
                                var groupList = data.groupList;
                                $('#modal_detail_secKill #brandName').text(groupList[0].brandName);
                                $('#modal_detail_secKill #partyName').text(groupList[0].partyName);
                                $('#modal_detail_secKill #brandNameAlias').text(groupList[0].brandNameAlias);

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
                function deleteSecKill(productBrandId) {
                    deleteId = productBrandId;
                    $('#modal_confirm #modal_confirm_body').html("确定删除此商品秒杀吗？");
                    $('#modal_confirm').modal('show');
                }

                /**
                 * 根据品牌id删除品牌
                 * @param ids
                 */
                function deleteSecKillByIds(ids){
                    //异步调用删除方法
                    $.ajax({
                        url: "deleteProductBrand",
                        type: "GET",
                        data: {productBrandId: ids},
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


