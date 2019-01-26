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
<#assign commonUrl = "findCoupon?lookupFlag=Y&"+ paramList +"&">
<#assign lookupFlag = "Y"/>
<#assign couponStatus = couponStatus?default('')/>
<#--${commonUrl}-->
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form class="form-inline clearfix" role="form" method="post" id="QueryForm"
              action="<@ofbizUrl>findCoupon</@ofbizUrl>">
            <input type="hidden" name="couponStatus" id="couponStatus" value="">
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">代金券编码</span>
                    <input type="text" class="form-control" name="couponCode" placeholder="代金券编码"
                           value="${couponCode?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">代金券名称</span>
                    <input type="text" class="form-control" name="couponName" placeholder="代金券名称"
                           value="${couponName?default("")}">
                </div>
            <#-- <div class="input-group m-b-10">
                 <span class="input-group-addon">优惠券类型</span>
                 <select class="form-control  dp-vd" id="promoType" name="promoType">
                     <option value="" >全部</option>
                     <#list couponTypeList as couponType_List>
                         <option value="${(couponType_List.enumId)?if_exists}" >${(couponType_List.get("description",locale))?if_exists}</option>
                     </#list>
                 </select>
             </div>
             <div class="input-group m-b-10">
                 <span class="input-group-addon">发放方式</span>
                <select class="form-control  dp-vd" id="promoType" name="promoType">
                     <option value="" >全部</option>
                     <#list couponPublishTypeList as couponPublishType_List>
                         <option value="${(couponPublishType_List.enumId)?if_exists}" >${(couponPublishType_List.get("description",locale))?if_exists}</option>
                     </#list>
                 </select>
             </div>
             <div class="input-group m-b-10">
                 <span class="input-group-addon">优惠范围</span>
                 <select class="form-control  dp-vd" id="promoType" name="promoType">
                     <option value="" >全部</option>
                     <#list couponRangeTypeList as couponRangeType_List>
                         <option value="${(couponRangeType_List.enumId)?if_exists}" >${(couponRangeType_List.get("description",locale))?if_exists}</option>
                     </#list>
                 </select>
             </div>-->
            </div>
            <div class="input-group pull-right">

            <#if security.hasEntityPermission("PRODPROMO_COUPON", "_LIST", session)>
                <button class="btn btn-success btn-flat">搜索</button>
            </#if>
            </div>
        </form>

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->
        <div class="row m-b-12" style="margin-bottom:15px;">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("PRODPROMO_GIFT", "_DELETE",session)>
                    <button id="btn_add" class="btn btn-primary">
                        <i class="fa fa-plus"></i>添加
                    </button>
                </#if>
                <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_UPDATE", session))&&(couponStatus=='ACTY_AUDIT_INIT')>
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
            <li role="presentation" <#if couponStatus?default("") == "">class="active"</#if>><a
                    href="<@ofbizUrl>findCoupon?lookupFlag=Y</@ofbizUrl>">全部</a></li>
            <li role="presentation" <#if couponStatus?default("") == "ACTY_AUDIT_INIT">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_INIT')">待审核</a></li>
            <li role="presentation" <#if couponStatus?default("") == "ACTY_AUDIT_PASS">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_PASS')">已通过</a></li>
            <li role="presentation" <#if couponStatus?default("") == "ACTY_AUDIT_NOPASS">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('ACTY_AUDIT_NOPASS')">已驳回</a></li>
        </ul>
            <!--</#if>-->
        <!-- 表格区域start -->
    <#if couponList?has_content>
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
                        <#if couponStatus=='ACTY_AUDIT_INIT'>
                            <th><input class="js-allcheck" type="checkbox"></th>
                        </#if>
                        <th>所属商家</th>
                        <th>代金券编码</th>
                        <th>代金券名称</th>
                        <th>代金券类型</th>
                        <th>发放方式</th>
                        <th>代金券数量</th>
                        <th>已发放数量</th>
                        <th>已使用数量</th>
                        <th>促销状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list couponList as promo>
                        <tr>
                            <#if couponStatus=='ACTY_AUDIT_INIT'>
                                <td><input class="js-checkchild" type="checkbox" value="${promo.couponCode?if_exists}">
                                </td>
                            </#if>
                            <td>${promo.partyName?if_exists}</td>
                            <td>${promo.couponCode?if_exists}</td>
                            <td>${promo.couponName?if_exists}</td>
                            <td><#if promo.couponType=='COUPON_TYPE_REDUCE'>满减类型<#else>现金抵用</#if></td>
                            <td><#if promo.publishType=='COUPON_PRDE_DIR'>直接发放<#else>参加活动发放</#if></td>
                            <td>${promo.couponQuantity?if_exists}</td>
                            <td>${promo.userCount?if_exists}</td>
                            <td>${promo.orderCount?if_exists}</td>
                            <td>
                                <#if promo.couponStatus=='ACTY_AUDIT_INIT'>待审核
                                <#elseif (promo.couponStatus=='ACTY_AUDIT_PASS')<#--&&(nowDate.before(promo.getTimestamp("endDate")))-->>
                                    已通过
                                <#elseif promo.couponStatus=='ACTY_AUDIT_NOPASS'>已驳回
                                <#elseif (promo.couponStatus=='ACTY_AUDIT_PASS')<#--&&(nowDate.after(promo.getTimestamp("endDate")))-->>
                                    已结束
                                </#if>
                            </td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("PRODPROMO_COUPON", "_LIST", session)>
                                        <button type="button" class="btn btn-danger btn-sm"  onclick="couponDetail('${promo.couponCode?default("N/A")}')">查看
                                        </button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_UPDATE", session) && (promo.couponStatus=='ACTY_AUDIT_NOPASS'))>
                                            <li><a href="javascript:void(0)" onclick="editPromCoupon('${promo.couponCode?if_exists}')">编辑</a></li>
                                        </#if>

                                        <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_UPDATE", session) && (promo.couponStatus=='ACTY_AUDIT_INIT'))>
                                            <li><a href="javascript:void(0)" onclick="editPromCoupon(${promo.couponCode})">编辑</a></li>

                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_AUDIT", session) && (promo.couponStatus=='ACTY_AUDIT_INIT'))>
                                            <li><a href="javascript:void(0)" onclick="auditPromoCoupon(${promo.couponCode?if_exists})">审核</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_AUDIT", session)&&(promo.couponStatus=='ACTY_AUDIT_PASS'))>
                                            <li><a href="javascript:void(0)" onclick="editEndDate('${promo.couponCode}','${promo.validitType}','${promo.endDate}','${promo.useEndDate}')">编辑结束时间</a></li>
                                        </#if>

                                        <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_DELETE", session))>
                                            <li><a href="javascript:void(0)" onclick="deletePromo(${promo.couponCode})">删除</a>
                                            </li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_LIST", session))>
                                            <li><a href="javascript:void(0)" onclick="auditLog(${promo.couponCode})">操作日记</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("PRODPROMO_COUPON", "_DETAIL", session) && (promo.couponStatus=='ACTY_AUDIT_NOPASS'))>
                                            <li><a href="javascript:void(0)" onclick="findAuditMessage('${promo.couponCode?if_exists}')">查看驳回原因</a>
                                            </li>
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
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(couponListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", couponListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=couponListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
                    <div class="modal-body"  class="form-horizontal">
                        <div class="row" id="endDateId">
                            <div>
                                <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>发放结束时间</label>
                                <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15"
                                     data-link-field="thruDate">
                                    <input class="form-control" size="16" type="text" id="endDate" readonly>
                                    <input id="endDate1" class="dp-vd" type="hidden" name="endDate">
                                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                    <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                </div>
                            </div>
                            <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                        </div>
                        <div class="row" id="useEndDateId">
                            <div>
                                <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>使用结束时间</label>
                                <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15"  data-link-field="thruDate">
                                    <input class="form-control" size="16" type="text" id="useEndDate" readonly>
                                    <input id="useEndDate1" class="dp-vd" type="hidden" name="useEndDate">
                                    <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                                    <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                                </div>
                            </div>
                            <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                        </div>

                    </div>
                    <div class="modal-footer" style="text-align: center;">
                        <button type="button" id="btn_EndDate" class="btn btn-primary">确定</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
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
                        <h4 class="modal-title" id="modal_add_title">满减-批量通过</h4>
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
                        <h4 class="modal-title" id="modal_add_title">满减-批量拒绝</h4>
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
                        <h4 class="modal-title" id="modal_add_title">代金券审核</h4>
                    </div>
                    <div class="modal-body">
                        <form id="ApprovalForm" method="post" class="form-horizontal"
                              action="<@ofbizUrl>approvalArticle</@ofbizUrl>">
                            <div class="form-group">
                                <label class="control-label col-sm-3">代金券编码:</label>
                                <div class="col-sm-8 p-t-5">
                                    <span id="couponCode"></span>
                                    <input type="hidden" class="form-control" id="couponCodes" name="couponCode">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label col-sm-3">代金券名称:</label>
                                <div class="col-sm-8 p-t-5">
                                    <span id="couponName"></span>
                                </div>
                            </div>

                            <div class="row">
                                <div class="form-group" data-type="minCheck" data-number="1" data-mark="操作">
                                    <label class="control-label col-sm-3">操作:</label>
                                    <div class="col-sm-8">
                                        <div class="radio">
                                            <label class="col-sm-4"><input name="couponStatus" type="radio"
                                                                           value="ACTY_AUDIT_PASS" checked
                                                                           class="radioItem">通过</label>
                                            <label class="col-sm-4"><input name="couponStatus" type="radio"
                                                                           value="ACTY_AUDIT_NOPASS" class="radioItem">拒绝</label>
                                            <div class="dp-error-msg"></div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <!--审核意见Start-->
                            <div class="row">
                                <div class="form-group" data-type="required,max" data-mark="审核意见" data-number="50">
                                    <label for="title" class="col-sm-3 control-label" id="label_gss"><i class="required-mark">*</i>审核意见:</label>
                                    <div class="col-sm-8">
                                        <textarea id="auditMessage" class="form-control dp-vd" name="auditMessage"  rows="3" style="resize: none;"></textarea>
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
        <div id="modal_detailCoupon" class="modal fade  bs-example-modal-lg" tabindex="-1" role="dialog"
             aria-labelledby="modal_edit_title">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_edit_title">代金券详细信息</h4>
                    </div>
                    <div class="modal-body">
                        <div class="box box-info" id="">
                            <div class="box-header">
                                <h3 class="box-title">基本信息</h3>
                            </div>
                        </div>
                        <!--优惠券编码  优惠券类型start-->
                        <div class="row title">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">代金券编码:</label>
                                <div class="col-sm-3">
                           <span id="couponCode"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">优惠券名称:</label>
                                <div class="col-sm-8">
                           <span id="couponName"><span>
                                </div>
                            </div>

                        </div>

                        <!--促销状态start-->
                        <div class="row title">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">代金券规则:</label>
                                <div class="col-sm-8">
                               <span id="payFill"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">促销状态:</label>
                                <div class="col-sm-8">
                                <span id="couponStatus"><span>
                                </div>
                            </div>
                        </div>
                        <!--优惠券名称 优惠券数量start-->
                        <div class="row title">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">代金券类型:</label>
                                <div class="col-sm-6">
                           <span id="couponType"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">优惠券数量:</label>
                                <div class="col-sm-6">
                           <span id="couponQuantity"><span>
                                </div>
                            </div>
                        </div>
                        <!--每人限领数量 每天限领数量start-->
                        <div class="row title">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">每人限领数量:</label>
                                <div class="col-sm-3">
                           <span id="couponPreCustomer"><span>
                                </div>
                            </div>
                           <#-- <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">每天限领数量:</label>
                                <div class="col-sm-6">
                           <span id="couponPerDay"><span>
                                </div>
                            </div>-->
                        </div>
                        <!--发放结束时间start-->
                        <div class="row title">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">发放开始时间:</label>
                                <div class="col-sm-6">
                           <span id="startDate"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">发放结束时间:</label>
                                <div class="col-sm-6">
                           <span id="endDate"><span>
                                </div>
                            </div>
                        </div>
                        <div class="row title" id="validit">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">代金券有效期:</label>
                                <div class="col-sm-8">
                                    <span id="validitDays"></span><span >天</span>
                                </div>
                            </div>
                        </div>
                        <!--有效开始时间 有效结束时间start-->
                        <div class="row title" id="useDateId">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">有效开始时间:</label>
                                <div class="col-sm-6">
                           <span id="useBeginDate"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">有效结束时间:</label>
                                <div class="col-sm-6">
                                    <span id="useEndDate"><span>
                                </div>
                            </div>
                        </div>
                        <!--发放方式start-->
                        <div class="row title">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">发放方式:</label>
                                <div class="col-sm-8">
                           <span id="publishType"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-6" id="use_Integral">
                                <label for="subTitle" class="col-sm-4 control-label">使用积分:</label>
                                <div class="col-sm-8">
                                    <span id="useIntegral"><span>
                                </div>
                            </div>
                        </div>

                        <div class="row title">
                            <div class="form-group col-sm-6">
                                <label for="subTitle" class="col-sm-4 control-label">适用店铺:</label>
                                <div class="col-sm-8">
                                    <span id="applyScope"><span>
                                </div>
                            </div>
                        </div>
                        <!--发放方式start-->
                        <!--所属商家start-->
                    <#-- <div class="row title">
                         <div class="form-group col-sm-6">
                            <label for="subTitle" class="col-sm-4 control-label">所属商家:</label>
                            <div class="col-sm-3" >
                            <span id="promoCode"><span>
                            </div>
                         </div>
                     </div>-->
                        <!--促销描述start-->
                        <div class="row title">
                            <div class="form-group col-sm-10">
                                <label for="subTitle" class="col-sm-2 control-label">促销描述:</label>
                                <div class="col-sm-10">
                                    <div class="box-body pad">
                                <textarea id="Centent" name="Centent" value="">
                                </textarea>
                                        <p class="dp-error-msg"></p>
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
                                            <th>商品编码</th>
                                            <th>商品名称</th>
                                            <th>市场价</th>
                                            <th>销售价</th>
                                        </tr>
                                        </thead>
                                        <tbody></tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    <#--<div class="box box-info js-party-level">
                      <div class="box-header">
                          <h3 class="box-title">参加的会员等级</h3>
                      </div>
                      <div class="box-body table-responsive no-padding">
                          <table class="table table-hover js-checkparent js-sort-list" id="partyLevelTable">
                              <tr>
                                  <th>序号</th>
                                  <th>会员等级</th>
                              </tr>

                          </table>
                      </div>-->
                    </div>
                    <div class="modal-footer">
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                    </div>
                </div>
            </div>
        </div><!-- 满减详情弹出框end -->


        <script>
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

                $("#btn_add").click(function () {
                    window.location.href = "<@ofbizUrl>addCoupon</@ofbizUrl>";
                });

                $("#exportOrder").click(function () {
                    if (getSelectedIds() != "") {
                        window.location.href = "<@ofbizUrl>exportReturn?ids=" + getSelectedIds() + "</@ofbizUrl>";
                    } else {
                        alert("请勾选一条记录");
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
                var ids = ""
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

            //切换优惠券状态
            function changeTab(couponStatus) {
                $("#couponStatus").val(couponStatus);
                $("#QueryForm").submit();
            }

            //优惠券下架
            function editEndDate(couponCode,validitType,endDate,useEndDate) {
                if(validitType=="ROLL"){
                    $("#editEndDate_Modal #useEndDateId").hide();
                }else{
                    $("#editEndDate_Modal #useEndDateId").show();
                    $("#editEndDate_Modal #useEndDate").val(useEndDate);
                    $("#editEndDate_Modal #useEndDate1").val(useEndDate);
                }
                $("#editEndDate_Modal #endDate").val(endDate);
                $("#editEndDate_Modal #endDate1").val(endDate);

                $('#editEndDate_Modal').modal('show');

                $('#editEndDate_Modal #btn_EndDate').click(function () {
                    var data={
                        couponCode:couponCode,
                        validitType:validitType,
                        endDate:$("#editEndDate_Modal #endDate").val(),
                        useEndDate:$("#editEndDate_Modal #useEndDate").val()
                    }
                    $.ajax({
                        url: "editCouponEndTime",
                        type: "POST",
                        data:data,
                        dataType: "json",
                        success: function (data) {
                            if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                                $.tipLayer(data._ERROR_MESSAGE_);
                            }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                                $.tipLayer(data._ERROR_MESSAGE_LIST_);
                            } else {
                                $('#editEndDate_Modal').modal('toggle');
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                $('#modal_msg').modal();
                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.href = '<@ofbizUrl>findCoupon</@ofbizUrl>';
                                })
                            }
                        },
                        error: function (data) {
                            $.tipLayer("操作失败！");
                        }
                    });
                });
            }

            //下架OK按钮点击事件
            $('#editEndDate_Modal #ok').click(function (e) {
                var couponCode = $('#editEndDate_Modal #couponCode').val();
                //异步调用删除方法
                $.ajax({
                    url: "endCoupon",
                    type: "GET",
                    data: {couponCode: couponCode},
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

            var deleteId = '';

            function deletePromo(couponCode) {
                deleteId = couponCode;
                $('#modal_confirm #modal_confirm_body').html("确定删除此满减吗？");
                $('#modal_confirm').modal('show');
            }

            //删除弹出框删除按钮点击事件
            $('#modal_confirm #ok').click(function (e) {
                //异步调用删除方法
                $.ajax({
                    url: "deleteCoupon",
                    type: "GET",
                    data: {couponCode: deleteId},
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

            function editPromCoupon(couponCode) {
                window.location.href = "<@ofbizUrl>editCoupon?couponCode=" + couponCode +"</@ofbizUrl>";
            }

            //单个优惠券审核
            function auditPromoCoupon(couponCode) {
                $.ajax({
                    url: "productCouponDetail",
                    type: "POST",
                    data: {
                        couponCode: couponCode
                    },
                    dataType: "json",
                    success: function (data) {
                        console.log(data)
                        var productPromoCoupon = data.productPromoCoupon;
                        if (productPromoCoupon) {
                            $('#modal_auditPromo #couponCodes').val(productPromoCoupon.couponCode)
                            $('#modal_auditPromo #couponCode').text(productPromoCoupon.couponCode)
                            $('#modal_auditPromo #couponName').text(productPromoCoupon.couponName)
                        }
                        $('#modal_auditPromo').modal();
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

            //审核通过不通过校验
            $(".radioItem").change(function () {
                var couponStatus = $("input[name='couponStatus']:checked").val();
                /*if (couponStatus == 'ACTY_AUDIT_NOPASS') {
                    $('#label_gss').html('<i class="required-mark">*</i>审核意见:');
                    $('#modal_auditPromo #auditMessage').addClass('dp-vd');
                } else {
                    $('#couponStatus').html('审核意见:');
                    $('#modal_auditPromo #auditMessage').removeClass('dp-vd');
                }*/
            })

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
                        url: "auditCoupon",
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

            function auditLog(couponCode){
                $.dataSelectModal({
                    url: "auditLogPage?businessId=coupon_"+couponCode,
                    width: "800",
                    title: "操作日志"
                });
            }

            function findAuditMessage(couponCode) {
                $.ajax({
                    url: "findCouponAuditMessage",
                    type: "POST",
                    data: {
                        couponCode: couponCode
                    },
                    dataType: "json",
                    success: function (data) {
                        if (data.ProductCouponAudit) {
                            var ProductCouponAudit = data.ProductCouponAudit;
                            $('#modal_NoPassconfirm #modal_confirm_body').html(ProductCouponAudit.auditMessage);
                            $('#modal_NoPassconfirm').modal('show');
                        }
                    }
                });
            }


            //时间格式化2
            function timeStamp2String2(datetime) {
                var year = datetime.year+1900;
                var month = datetime.month + 1 < 10 ? "0" + (datetime.month + 1) : datetime.month + 1;
                /*var date = datetime.day < 10 ? "0" + datetime.day : datetime.day;*/  //这是取周几
                var date = datetime.date < 10 ? "0" + datetime.date : datetime.date;
                var hour = datetime.hours < 10 ? "0" + datetime.hours : datetime.hours;
                var minute = datetime.minutes < 10 ? "0" + datetime.minutes : datetime.minutes;
                var second = datetime.seconds < 10 ? "0" + datetime.seconds : datetime.seconds;
                return year + "-" + month + "-" + date + " " + hour + ":" + minute + ":" + second;
            };

            //优惠券详情
            function couponDetail(couponCode) {
                $.ajax({
                    url: "productCouponDetail",
                    type: "POST",
                    data: {
                        couponCode: couponCode
                    },
                    dataType: "json",
                    success: function (data) {
                        if (data.productPromoCoupon) {
                            var productPromoCoupon = data.productPromoCoupon;
                            CKEDITOR.instances.Centent.setData(productPromoCoupon.couponDesc);
                            $('#modal_detailCoupon #couponCode').text(productPromoCoupon.couponCode);
                            $('#modal_detailCoupon #couponName').text(productPromoCoupon.couponName);
                            $('#modal_detailCoupon #couponQuantity').text(productPromoCoupon.couponQuantity);
                            $('#modal_detailCoupon #couponPreCustomer').text(productPromoCoupon.couponPreCustomer);
//                            $('#modal_detailCoupon #couponPerDay').text(productPromoCoupon.couponPerDay);
                            $('#modal_detailCoupon #startDate').text(timeStamp2String(productPromoCoupon.startDate.time));
                            $('#modal_detailCoupon #endDate').text(timeStamp2String(productPromoCoupon.endDate.time));

                            $('#modal_detailCoupon #validitDays').text(productPromoCoupon.validitDays);

                            var validitType = productPromoCoupon.validitType;
                            if (validitType == "FIX") {
                                //固定有效期
                                $('#validit').hide();
                                $('#useDateId').show();
                                $('#modal_detailCoupon #useBeginDate').text(timeStamp2String(productPromoCoupon.useBeginDate.time));
                                $('#modal_detailCoupon #useEndDate').text(timeStamp2String(productPromoCoupon.useEndDate.time));
                            } else {
                                $('#validit').show();
                                $('#useDateId').hide();
                            }

                            var applyScope="";
                            if(productPromoCoupon.applyScope=="A"){
                                applyScope="全渠道";
                            }else{
                                applyScope="本店铺（自营）";
                            }
                            $('#modal_detailCoupon #applyScope').text(applyScope);
                            var pay = '';

                            if (productPromoCoupon.couponType == 'COUPON_TYPE_REDUCE') {
                                $('#modal_detailCoupon #couponType').text('满减类型');
                                pay = '订单满' + productPromoCoupon.payFill + '减' + productPromoCoupon.payReduce
                                $('#modal_detailCoupon #payFill').text(pay);
                            } else {
                                $('#modal_detailCoupon #couponType').text('现金抵用');
                                $('#modal_detailCoupon #payFill').text(pay);
                                pay = '抵用金额' + productPromoCoupon.arrivedAmount + '元'
                                $('#modal_detailCoupon #payFill').text(pay);
                            }
                            if (productPromoCoupon.publishType == 'COUPON_PRDE_DIR') {
                                $('#modal_detailCoupon #publishType').text('直接发放');
                                $('#modal_detailCoupon #use_Integral').hide();
                            } else if (productPromoCoupon.publishType == 'COUPON_ACT_DIR') {
                                $('#modal_detailCoupon #publishType').text('参加活动发放');
                                $('#modal_detailCoupon #use_Integral').show();
                                $('#modal_detailCoupon #useIntegral').text(productPromoCoupon.useIntegral);
                            }

                            if (productPromoCoupon.couponStatus == 'ACTY_AUDIT_INIT') {
                                $('#modal_detailCoupon #couponStatus').text('待审核');
                            } else if (productPromoCoupon.couponStatus == 'ACTY_AUDIT_PASS') {
                                $('#modal_detailCoupon #couponStatus').text('已通过');
                            } else if (productPromoCoupon.couponStatus == 'ACTY_AUDIT_NOPASS') {
                                $('#modal_detailCoupon #couponStatus').text('已驳回');
                            }
                            var productCouponProducts = data.productCouponProducts;
                            if (productCouponProducts.length == 0) {

                            } else {
                                $('#productTable>tbody').empty();
                                var tr1 = "";
                                for (var i = 0; i < productCouponProducts.length; i++) {
                                    tr1 += "<tr>"
                                            + "<td>" + productCouponProducts[i].productId + "</td>"
                                            + "<td>" + productCouponProducts[i].productName + "</td>"
                                            + "<td>" + productCouponProducts[i].marketprice + "</td>"
                                            + "<td>" + productCouponProducts[i].defaultprice + "</td>"
                                            + "</tr>"
                                }
                                $('#productTable>tbody').append(tr1);
                            }
                        }

                        $('#modal_detailCoupon').modal();
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

            //批量通过按钮点击事件
            $('#btn_approval').click(function () {
                var checks = $('#prom_table .js-checkchild:checked');
                //判断是否选中记录
                var c = '';
                var tr = checks.closest('tr');
                if (checks.size() > 0) {
                    //编辑id字符串
                    tr.each(function () {
                        var couponCode = $(this).find('td').eq(1).text();
                        var couponName = $(this).find('td').eq(2).text();
                        c += '<div class="form-group">'
                                + '<label class="control-label col-sm-2">促销编码:</label>'
                                + '<div class="col-sm-10 p-t-5">'
                                + '<span>' + couponCode + '</span>'
                                + '</div>'
                                + '</div>'
                                + '<div class="form-group">'
                                + '<label class="control-label col-sm-2">促销名称:</label>'
                                + '<div class="col-sm-10 p-t-5">'
                                + '<span>' + couponName + '</span>'
                                + '</div>'
                                + '</div>'
                                + '<div class="form-group">'
                                + '<label class="control-label col-sm-2">审核意见:</label>'
                                + '<div class="col-sm-8">'
                                + '<textarea id=""  data-id="' + couponCode + '" name="" class="form-control" rows="3" style="resize: none;"></textarea>'
                                + '</div>'
                                + '</div>'
                                + '<div class="cut-off-rule bg-gray">'
                                + '</div>'

                    });
                    $('#modal_approval .modal-body .form-horizontal').html(c);
                    $('#modal_approval').modal();
                } else {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("请至少选择一条！");
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
                    url: "batchAuditPromoCoupon",
                    type: "POST",
                    data: {
                        obj: obj,
                        couponStatus: 'ACTY_AUDIT_PASS'
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
                        var couponCode = $(this).find('td').eq(1).text();
                        var couponName = $(this).find('td').eq(2).text();
                        c += '<div class="row">'
                                + '<div class="form-group">'
                                + '<label class="control-label col-sm-2">促销编码:</label>'
                                + '<div class="col-sm-10 p-t-5">'
                                + '<span>' + couponCode + '</span>'
                                + '</div>'
                                + '</div>'
                                + '<div class="form-group">'
                                + '<label class="control-label col-sm-2">促销名称:</label>'
                                + '<div class="col-sm-10 p-t-5">'
                                + '<span>' + couponName + '</span>'
                                + '</div>'
                                + '</div>'
                                + '<div class="form-group" data-type="required" data-mark="审核意见">'
                                + '<label class="control-label col-sm-2"><i class="required-mark">*</i>审核意见:</label>'
                                + '<div class="col-sm-8">'
                                + '<textarea id=""  class="form-control dp-vd" data-id="' + couponCode + '" name="" class="form-control" rows="3" style="resize: none;"></textarea>'
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
                        url: "batchAuditPromoCoupon",
                        type: "POST",
                        data: {
                            obj: obj,
                            couponStatus: 'ACTY_AUDIT_NOPASS'
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


