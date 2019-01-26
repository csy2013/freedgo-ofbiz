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
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<#assign commonUrl = "findDiscount?lookupFlag=Y&"+ paramList +"&">
<#assign lookupFlag = "Y"/>
<#assign promoStatus = promoStatus?default('')/>
<#--${commonUrl}-->
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form class="form-inline clearfix" role="form" method="post" id="QueryForm"
              action="<@ofbizUrl>findDiscount</@ofbizUrl>">
            <input type="hidden" name="productStoreId" id="productStoreId" value="${requestAttributes.productStoreId}"/>
            <input type="hidden" name="promoStatus" id="promoStatus" value="">
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group w-p100">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">促销代码</span>
                    <input type="text" class="form-control" name="promoCode" placeholder="促销代码"
                           value="${promoCode?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">促销名称</span>
                    <input type="text" class="form-control" name="promoName" placeholder="促销名称"
                           value="${promoName?default("")}">
                </div>

                <div class="input-group pull-right p-l-10">
                <#if security.hasEntityPermission("PRODPROMO_DISCOUNT", "_LIST", session)>
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
                <#if security.hasEntityPermission("PRODPROMO_DISCOUNT", "_DELETE",session)>
                    <button id="btn_add" class="btn btn-primary">
                        <i class="fa fa-plus"></i>添加
                    </button>
                </#if>
                <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_UPDATE", session))&&(promoStatus=='ACTY_AUDIT_INIT')>
                    <button id="btn_approval" class="btn btn-primary">
                        <i class="fa">批量通过</i>
                    </button>
                    <button id="btn_resolute" class="btn btn-primary">
                        <i class="fa">批量驳回</i>
                    </button>
                </#if>
                </div>
            </div>
        </div>
            <!--  <#if lookupFlag == "Y">-->
        <ul class="nav nav-tabs">
            <li role="presentation" <#if promoStatus?default("") == "">class="active"</#if>><a
                    href="<@ofbizUrl>findDiscount?lookupFlag=Y</@ofbizUrl>">全部</a></li>
            <li role="presentation" <#if promoStatus?default("") == "ACTY_AUDIT_INIT">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_INIT')">待审核</a></li>
            <li role="presentation" <#if promoStatus?default("") == "ACTY_AUDIT_PASS">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_PASS')">已通过</a></li>
            <li role="presentation" <#if promoStatus?default("") == "ACTY_AUDIT_NOPASS">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_NOPASS')">已驳回</a></li>
        </ul>
            <!--</#if>-->
        <!-- 表格区域start -->
    <#if promoList?has_content>
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
                        <#if promoStatus=='ACTY_AUDIT_INIT'>
                            <th><input class="js-allcheck" type="checkbox"></th>
                        </#if>
                        <th>促销编码</th>
                        <th>促销名称</th>
                        <th>开始时间</th>
                        <th>结束时间</th>
                        <th>审核状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list promoList as promo>
                        <tr>
                            <#if promoStatus=='ACTY_AUDIT_INIT'>
                                <td><input class="js-checkchild" type="checkbox"
                                           value="${promo.productPromoId?if_exists}"></td>
                            </#if>
                            <td>${promo.promoCode?if_exists}</td>
                            <td>${promo.promoName?if_exists}</td>
                        <#--<td><#if promo.promoType=='PPIP_ORDER_TOTAL'>按金额<#else>按商品件数</#if></td>-->
                            <td>${promo.fromDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>${promo.thruDate?string("yyyy-MM-dd HH:mm:ss")}</td>
                            <td>
                                <#if promo.promoStatus=='ACTY_AUDIT_INIT'>待审核
                                <#elseif promo.promoStatus=='ACTY_AUDIT_PASS'>已通过
                                <#elseif promo.promoStatus=='ACTY_AUDIT_NOPASS'>已驳回
                                </#if>
                            </td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("PRODPROMO_DISCOUNT", "_LIST", session)>
                                        <button type="button" class="btn btn-danger btn-sm"
                                                onclick="reduceDetail('${promo.productPromoId?default("N/A")}')">查看
                                        </button>
                                    </#if>

                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_UPDATE", session) && (promo.promoStatus=='ACTY_AUDIT_NOPASS'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="editDiscount('${promo.productPromoId}','N')">编辑</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_UPDATE", session) && (promo.promoStatus=='ACTY_AUDIT_PASS'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="editDiscount('${promo.productPromoId}','Y')">编辑</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_UPDATE", session) && (promo.promoStatus=='ACTY_AUDIT_INIT'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="auditPromoReduce(${promo.productPromoId})">审核</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_UPDATE", session) && (promo.promoStatus=='ACTY_AUDIT_PASS'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="editEndDate(${promo.productPromoId},'${promo.thruDate?string("yyyy-MM-dd HH:mm:ss")}','${promo.fromDate?string("yyyy-MM-dd HH:mm:ss")}')">编辑结束时间</a>
                                            </li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_DELETE", session))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="deletePromo(${promo.productPromoId})">删除</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_LIST", session))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="auditLog(${promo.productPromoId})">操作日记</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_DISCOUNT", "_LIST", session) && (promo.promoStatus=='ACTY_AUDIT_NOPASS'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="findAuditMessage('${promo.productPromoId}')">查看驳回原因</a></li>
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
        <#assign commonUrl = "findDiscount?lookupFlag=Y&"+ paramList + "&"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(promoListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", promoListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=promoListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
                        <h4 class="modal-title" id="exampleModalLabel">促销结束时间编辑</h4>
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

                            <input type="hidden" class="form-control dp-vd w-p50" name="productPromoId" value="N">
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


        <!-- 批量通过弹出框start -->
        <div id="modal_approval" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_add_title">批量通过</h4>
                    </div>
                    <div class="modal-body">
                        <form id="Approval_Form" method="post" class="form-horizontal" role="form" action="">

                        </form>
                    </div>
                    <div class="modal-footer">
                        <button id="save_approval" type="button" class="btn btn-primary">确定</button>
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div><!-- 批量通过弹出框end -->


        <!-- 批量拒绝弹出框start  -->
        <div id="modal_resolute" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_add_title">批量拒绝</h4>
                    </div>
                    <div class="modal-body">
                        <form id="ResoluteForm" method="post" class="form-horizontal" role="form" action="">

                        </form>
                    </div>
                    <div class="modal-footer">
                        <button id="save_resolute" type="button" class="btn btn-primary">确定</button>
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div><!-- 批量通过弹出框end -->


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
        <div id="modal_auditPromo" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_add_title">促销审核</h4>
                    </div>
                    <div class="modal-body">
                        <form id="ApprovalForm" method="post" class="form-horizontal"
                              action="<@ofbizUrl>approvalArticle</@ofbizUrl>">
                            <div class="form-group">
                                <label class="control-label col-sm-2">促销编码:</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="promoCode" name="promoCode" readonly>
                                    <input type="hidden" class="form-control" id="productPromoId" name="productPromoId">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-2">促销名称:</label>
                                <div class="col-sm-8">
                                    <input type="text" class="form-control" id="promoName" name="promoName" readonly>
                                </div>
                            </div>

                            <div class="row">
                                <div class="form-group" data-type="minCheck" data-number="1" data-mark="操作">
                                    <label class="control-label col-sm-2">操作:</label>
                                    <div class="col-sm-10">
                                        <div class="radio">
                                            <label class="col-sm-4"><input name="promoStatus" type="radio"
                                                                           value="ACTY_AUDIT_PASS" checked
                                                                           class="radioItem">通过</label>
                                            <label class="col-sm-4"><input name="promoStatus" type="radio"
                                                                           value="ACTY_AUDIT_NOPASS" class="radioItem">拒绝</label>
                                            <div class="dp-error-msg"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!--审核意见Start-->
                            <div class="row">
                                <div class="form-group" data-type="required,max" data-mark="审核意见" data-number="50">
                                    <label for="title" class="col-sm-2 control-label" id="label_gss"><i
                                            class="required-mark">*</i>审核意见:</label>
                                    <div class="col-sm-8">
                                        <textarea id="auditMessage" class="form-control  dp-vd" name="auditMessage"
                                                  rows="3" style="resize: none;"></textarea>
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


        <!--满减详情弹出框start -->
        <div id="modal_detailReduce" class="modal fade  bs-example-modal-lg" tabindex="-1" role="dialog"
             aria-labelledby="modal_edit_title">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_edit_title">促销详细信息</h4>
                    </div>
                    <div class="modal-body">

                        <div class="box box-info" id="">
                            <div class="box-header">
                                <h3 class="box-title">基本信息</h3>
                            </div>
                            <div class="box-body table-responsive no-padding">

                            </div>
                        </div>
                        <!--半成品编号  半成品类型start-->
                        <div class="row title">
                            <div class="form-group col-sm-3">
                                <label for="subTitle" class="col-sm-6 control-label">促销编码:</label>
                                <div class="col-sm-3">
               <span id="promoCode"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-4">
                                <label for="subTitle" class="col-sm-6 control-label">促销名称:</label>
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
                        <!--半成品编号  半成品类型start-->
                        <div class="row title">
                            <div class="form-group col-sm-3">
                                <label for="subTitle" class="col-sm-6 control-label">折扣类型:</label>
                                <div class="col-sm-6">
               <span id="promo_Rule"><span>
                                </div>
                            </div>
                        <#--<div class="form-group col-sm-4">-->
                        <#--<label for="subTitle" class="col-sm-6 control-label">参与方式:</label>-->
                        <#--<div class="col-sm-6">-->
                        <#--<span id="promoProductType"><span>-->
                        <#--</div>-->
                        <#--</div>-->
                            <div class="form-group col-sm-5">
                                <label for="subTitle" class="col-sm-4 control-label">结束时间:</label>
                                <div class="col-sm-7">
               <span id="thruDate"><span>
                                </div>
                            </div>
                        </div>
                        <!--活动规则start-->
                        <div class="row title">
                            <div class="form-group col-sm-12">
                                <label for="subTitle" class="col-sm-2 control-label">活动规则:</label>
                                <div class="col-sm-6">
                                    <span id="promoRule"><span>
                                </div>
                            </div>
                        </div>
                        <!--活动规则start-->
                    <#--<div class="row title" style="display: none">-->
                    <#--<div class="form-group col-sm-10">-->
                    <#--<label for="subTitle" class="col-sm-2 control-label">促销描述:</label>-->
                    <#--<div class="col-sm-10">-->
                    <#--<div class="box-body pad">-->
                    <#--<textarea id="Centent" name="Centent" value=""></textarea>-->
                    <#--<p class="dp-error-msg"></p>-->
                    <#--</div>-->
                    <#--</div>-->
                    <#--</div>-->
                    <#--</div>-->

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
                                            <th>货品图片</th>
                                            <th>商品规格</th>
                                            <th>商品编码</th>
                                            <th>货品名称</th>
                                        </tr>
                                        </thead>
                                        <tbody></tbody>
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

                    $("#btn_add").click(function () {
                        window.location.href = "<@ofbizUrl>addDiscount</@ofbizUrl>";
                    });

                    $("#exportOrder").click(function () {
                        if (getSelectedIds() != "") {
                            window.location.href = "<@ofbizUrl>exportReturn?ids=" + getSelectedIds() + "</@ofbizUrl>";
                        } else {
                            $.tipLayer("请勾选一条记录");
                        }
                    });

                    $('#myModal').modal({
                        show: false
                    });

                });

                function showDeliveryForm(orderId) {
                    $("#deliveryForm_update #orderId").val(orderId);
                    $('#deliveryForm_update').modal();
                }

                function getSelectedIds() {
                    var ids = "";
                    var checks = $('.js-checkchild:checked');
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


                //修改弹出框保存按钮点击事件
                $('#deliveryForm_update #save').click(function () {
                    $('#deliveryForm').dpValidate({
                        clear: true
                    });
                    $('#deliveryForm').submit();
                });

                $('#deliveryForm').dpValidate({
                    validate: true,
                    callback: function () {
                        document.getElementById('deliveryForm').submit();
                    }
                });

                //弹出窗关闭事件
                $('#deliveryForm_update').on('hide.bs.modal', function () {
                    $('#deliveryForm').dpValidate({
                        clear: true
                    });
                });


                function updateMoney(orderId) {
                    if (confirm("订单金额不能随意修改，你确定修改吗？")) {
                        $("#moneyForm_update #orderId").val(orderId);
                        $('#moneyForm_update').modal();
                    }
                }


                $('#moneyForm_update #save').click(function () {
                    $('#moneyForm').dpValidate({
                        clear: true
                    });
                    $('#moneyForm').submit();
                });

                $('#moneyForm').dpValidate({
                    validate: true,
                    callback: function () {
                        document.getElementById('moneyForm').submit();
                    }
                });

                //弹出窗关闭事件
                $('#moneyForm_update').on('hide.bs.modal', function () {
                    $('#moneyForm').dpValidate({
                        clear: true
                    });
                });

                $('#showOperateLog').on('hide.bs.modal', function () {
                    $("#logTable tbody").html("");
                });

                function showOperateLog(orderId) {
                    $.post("orderLog", {orderId: orderId}, function (data) {
                        var logList = data.logList;
                        if (logList != null && logList != "" && logList != undefined) {
                            var h = "";
                            for (var i = 0; i < logList.length; i++) {
                                var l = logList[i];
                                h += "<tr><td>" + l.operateType + "</td><td>" + l.operator + "</td><td>" + l.operateTime + "</td><td></td></tr>";
                            }
                            $("#logTable tbody").append(h);
                            $('#showOperateLog').modal();
                        }
                    });
                }

                function viewOrderInfo(orderId) {
                    $('#myModal' + orderId).modal();
                }

                function cancelOrder(orderId) {
                    if (confirm("订单不能随意中断，你确定修改吗？")) {
                        $("#cancelForm_update #orderId").val(orderId);
                        $('#cancelForm_update').modal();
                    }
                }

                $('#cancelForm_update').on('hide.bs.modal', function () {
                    $('#cancelForm').dpValidate({
                        clear: true
                    });
                });

                $('#cancelForm_update #save').click(function () {
                    $('#cancelForm').dpValidate({
                        clear: true
                    });
                    $('#cancelForm').submit();
                });

                $('#cancelForm').dpValidate({
                    validate: true,
                    callback: function () {
                        document.getElementById('cancelForm').submit();
                    }
                });

                function operateReturn(returnId, statusId) {
                    window.location.href = "<@ofbizUrl>operateReturn?statusId=" + statusId + "&returnId=" + returnId + "</@ofbizUrl>";
                }


                function changeTab(promoStatus) {
                    $("#promoStatus").val(promoStatus);
                    $("#QueryForm").submit();
                }

                function editEndDate(productPromoId, endDate, startDate) {
                    $('#editEndDate_Modal #endDate').val(endDate);
                    $('#editEndDate_Modal #thruDate').val(endDate);
                    $('#editEndDate_Modal #startDate').val(startDate);
                    $('#editEndDate_Modal #fromDate').val(startDate);
                    $('#editEndDate_Modal #productPromoId').val(productPromoId);
                    $('#editEndDateForm').dpValidate({
                        clear: true
                    });
                    $('#editEndDate_Modal').modal('show');
                }

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
                            url: "endPromoReduce",
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

                                if (data.chkFlg == "Y") {
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
                                } else {
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
                var deleteId = '';

                function deletePromo(productPromoId) {
                    deleteId = productPromoId;
                    $('#modal_confirm #modal_confirm_body').html("确定删除此折扣吗？");
                    $('#modal_confirm').modal('show');
                }

                function auditLog(productPromoId) {
                    $.dataSelectModal({
                        url: "auditLogPage?businessId=promo_" + productPromoId,
                        width: "800",
                        title: "操作日志"
                    });
                }

                //删除弹出框删除按钮点击事件
                $('#modal_confirm #ok').click(function (e) {
                    //异步调用删除方法
                    $.ajax({
                        url: "deleteProductPromo",
                        type: "GET",
                        data: {productPromoId: deleteId},
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
                });

                function editDiscount(productPromoId,isPass) {
                    window.location.href = "<@ofbizUrl>editDiscount?productPromoId=" + productPromoId +"&isPass="+isPass+"</@ofbizUrl>";
                }

                function auditPromoReduce(productPromoId) {
                    //审核之前判断里面的商品是否已经被添加了。
                    //获取这个促销下的所有商品id。
                    var productIds = "";
                    var startDate = "";
                    var endDate = "";
                    $.ajax({
                        url: "findProductIdsByPromoId${externalKeyParam}",
                        type: "POST",
                        async: false,
                        data: {
                            productPromoId: productPromoId
                        },
                        dataType: "json",
                        success: function (data) {
                            productIds = data.productIds;
                            startDate = data.startDate;
                            endDate = data.endDate;
                        },
                        error: function (data) {
                            $.tipLayer("操作失败！");
                            return;
                        }
                    });

                    var chkFlg = false;
                    var existProductIds = "";
                    var errorMsg = "";
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
                            errorMsg = data.errorMsg;
                            chkFlg = data.chkFlg;
                            existProductIds = data.existProductIds;
                        },
                        error: function (data) {
                            $.tipLayer("操作失败！");
                            return;
                        }
                    });

                    if (chkFlg == "N") {
                        $.tipLayer(errorMsg);
                        return;
                    }


                    $.ajax({
                        url: "productPromoDetail",
                        type: "POST",
                        data: {
                            productPromoId: productPromoId
                        },
                        dataType: "json",
                        success: function (data) {
                            if (data.productPromo) {
                                var productPromo = data.productPromo;
                                $('#modal_auditPromo #productPromoId').val(productPromo.productPromoId);
                                $('#modal_auditPromo #promoName').val(productPromo.promoName);
                                $('#modal_auditPromo #promoCode').val(productPromo.promoCode);
                                $('#modal_auditPromo').modal();
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

                function findAuditMessage(productPromoId) {
                    $.ajax({
                        url: "findAuditMessage",
                        type: "POST",
                        data: {
                            productPromoId: productPromoId
                        },
                        dataType: "json",
                        success: function (data) {
                            if (data.ProductPromoAudit) {
                                var productPromoAudit = data.ProductPromoAudit;
                                $('#modal_NoPassconfirm #modal_confirm_body').html(productPromoAudit.auditMessage);
                                $('#modal_NoPassconfirm').modal('show');
                            }
                        }
                    });
                }

                //单个保存审核按钮点击事件
                $('#modal_auditPromo #save').click(function () {
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
                            url: "auditPromoPromo",
                            type: "POST",
                            data: $('#ApprovalForm').serialize(),
                            dataType: "json",
                            success: function (data) {
                                //隐藏新增弹出窗口
                                $('#modal_auditPromo').modal('toggle');
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
                                $('#modal_auditPromo').modal('toggle');
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                                $('#modal_msg').modal();
                            }
                        });
                    }
                });

                //时间格式化2
                function timeStamp2String2(datetime) {
                    var year = datetime.year + 1900;
                    var month = datetime.month + 1 < 10 ? "0" + (datetime.month + 1) : datetime.month + 1;
                    /*var date = datetime.day < 10 ? "0" + datetime.day : datetime.day;*/  //这是取周几
                    var date = datetime.date < 10 ? "0" + datetime.date : datetime.date;
                    var hour = datetime.hours < 10 ? "0" + datetime.hours : datetime.hours;
                    var minute = datetime.minutes < 10 ? "0" + datetime.minutes : datetime.minutes;
                    var second = datetime.seconds < 10 ? "0" + datetime.seconds : datetime.seconds;
                    return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
                };

                function reduceDetail(productPromoId) {

                    $.ajax({
                        url: "productPromoDiscountDetail",
                        type: "POST",
                        data: {
                            productPromoId: productPromoId
                        },
                        dataType: "json",
                        success: function (data) {
                            if (data.productPromo) {
                                var productPromo = data.productPromo;
                                var productPromoConds = data.productPromoConds;
                                // var productPromoActions = data.productPromoActions;
                                var condActList = data.condActList;
                                // CKEDITOR.instances.Centent.setData(productPromo.promoText);
                                $('#modal_detailReduce #promoCode').text(productPromo.promoCode);
                                $('#modal_detailReduce #promoName').text(productPromo.promoName);
                                for (var i = 0; i < condActList.length; i++) {
                                    var parmoType = condActList[i].inputParamEnumId;

                                }

                                var temp = "";
                                if (parmoType == 'PPIP_ORDER_TOTAL') {
                                    temp = "按金额";
                                } else if (parmoType == 'PPIP_PRODUCT_QUANT') {
                                    temp = "按商品件数";
                                }

                                var ParamEnumId = "";
                                if (parmoType == 'PPIP_ORDER_TOTAL') {
                                    ParamEnumId = "元";
                                } else if (parmoType == 'PPIP_PRODUCT_QUANT') {
                                    ParamEnumId = "件";
                                }

                                // var promoRule="满 "+condValue+" "+temp+" 减"+amount+"元";
                                $("#modal_detailReduce #promo_Rule").text(temp);

                                if (productPromo.promoProductType == 'PROMO_PRT_ALL') {
                                    $('#modal_detailReduce #promoProductType').text('全部产品参与');
                                    $('#product').hide();
                                } else if (productPromo.promoProductType == 'PROMO_PRT_PART_EX') {
                                    $('#modal_detailReduce #promoProductType').text('部分产品不参与 ');
                                    $('#product').show();
                                } else if (productPromo.promoProductType == 'PROMO_PRT_PART_IN') {
                                    $('#modal_detailReduce #promoProductType').text('部分产品参与');
                                    $('#product').show();
                                }
                                if (data.productStorePromoAppls) {
                                    var productStorePromoAppl = data.productStorePromoAppls;
                                    // console.log(productStorePromoAppl.fromDate.time);
                                    $('#modal_detailReduce #fromDate').text(timeStamp2String2(productStorePromoAppl.fromDate));
                                    $('#modal_detailReduce #thruDate').text(timeStamp2String2(productStorePromoAppl.thruDate));
                                }


                                var rule = "";
                                for (var i = 0; i < condActList.length; i++) {
                                    rule += "满" + condActList[i].condValue + "打" + condActList[i].amount + "折,";
                                }
                                rule = rule.substr(0, rule.length - 1);
                                $('#promoRule').text(rule);

                                // $('#promoRule').text(rule);

                                $('#productTable>tbody').empty();
                                // console.log(data.productList);
                                var product_List = data.productList;
                                var productPromo = data.productPromo;
                                var tr1 = "";
                                for (var i = 0; i < product_List.length; i++) {
                                    tr1 += "<tr>"
                                            + "<td>" + product_List[i].productId + "</td>"
                                            + "<td>" + product_List[i].productId + "</td>"
                                            + "<td>" + productPromo.promoCode + "</td>"
                                            + "<td>" + product_List[i].productName + "</td>"
                                            + "</tr>"
                                }
                                $('#productTable>tbody').append(tr1);
                                $('#modal_detailReduce').modal();
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

                $(".radioItem").change(function () {
                    var promoStatus = $("input[name='promoStatus']:checked").val();
                    /* if (promoStatus == 'ACTY_AUDIT_NOPASS') {
                         $('#label_gss').html('<i class="required-mark">*</i>审核意见:');
                         $('#modal_auditPromo #auditMessage').addClass('dp-vd');
                     } else {
                         $('#label_gss').html('审核意见:');
                         $('#modal_auditPromo #auditMessage').removeClass('dp-vd');
                     }*/
                })

                //批量通过按钮点击事件
                $('#btn_approval').click(function () {
                    var checks = $('#prom_table .js-checkchild:checked');
                    //判断是否选中记录
                    var c = '';
                    var tr = checks.closest('tr');
                    if (checks.size() > 0) {
                        //编辑id字符串
                        tr.each(function () {
                            var productPromoId = $(this).find('td').eq(0).find("input[type='checkbox']").val();
                            var productCode = $(this).find('td').eq(1).text();
                            var proPromName = $(this).find('td').eq(2).text();
                            c += '<div class="form-group">'
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
                                    + '<div class="form-group">'
                                    + '<label class="control-label col-sm-2">审核意见:</label>'
                                    + '<div class="col-sm-8">'
                                    + '<textarea id=""  data-id="' + productPromoId + '" name="" class="form-control" rows="3" style="resize: none;"></textarea>'
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
                        var id = $(this).data("id"),
                                value = $(this).val();
                        obj += id + ':' + value + ',';
                    });
                    $.ajax({
                        url: "batchAuditPromoReduce",
                        type: "POST",
                        data: {
                            obj: obj,
                            promoStatus: 'ACTY_AUDIT_PASS'
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
                            var productPromoId = $(this).find('td').eq(0).find("input[type='checkbox']").val();
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
                                    + '<textarea id=""  class="form-control dp-vd" data-id="' + productPromoId + '" name="" class="form-control" rows="3" style="resize: none;"></textarea>'
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
                            var id = $(this).data("id"),
                                    value = $(this).val();
                            obj += id + ':' + value + ',';
                        });
                        $.ajax({
                            url: "batchAuditPromoReduce",
                            type: "POST",
                            data: {
                                obj: obj,
                                promoStatus: 'ACTY_AUDIT_NOPASS'
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
            </script>


