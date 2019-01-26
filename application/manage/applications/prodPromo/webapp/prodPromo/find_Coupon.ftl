<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="Business_QueryForm" class="form-inline clearfix">
            <input type="hidden" name="couponStatus" id="couponStatus" value="">
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group w-p100">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">优惠券编码</span>
                    <input type="text" class="form-control" name="couponCode" id="couponCode" placeholder="优惠券编码"
                           value="">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">优惠券名称</span>
                    <input type="text" class="form-control" name="couponName" id="couponName" placeholder="优惠券名称"
                           value="${couponName?default("")}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">优惠券类型</span>
                    <select class="form-control  dp-vd" id="couponType" id="couponType" name="promoType">
                        <option value="">全部</option>
                    <#list couponTypeList as couponType_List>
                        <option value="${(couponType_List.enumId)?if_exists}">${(couponType_List.get("description",locale))?if_exists}</option>
                    </#list>
                    </select>
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">发放方式</span>
                    <select class="form-control  dp-vd" id="publishType" name="publishType">
                        <option value="">全部</option>
                    <#list couponPublishTypeList as couponPublishType_List>
                        <option value="${(couponPublishType_List.enumId)?if_exists}">${(couponPublishType_List.get("description",locale))?if_exists}</option>
                    </#list>
                    </select>
                </div>
                <!--
                <div class="input-group m-b-10">
                    <span class="input-group-addon">优惠范围</span>
                    <select class="form-control  dp-vd" id="promoType" name="promoType">
                        <option value="" >全部</option>
                        <#list couponRangeTypeList as couponRangeType_List>
                            <option value="${(couponRangeType_List.enumId)?if_exists}" >${(couponRangeType_List.get("description",locale))?if_exists}</option>
                        </#list>
                    </select>
                </div>-->
                <div class="input-group pull-right">
                    <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
                </div>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->
        <div class="row m-b-12" style="margin-bottom:15px;">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <!-- 是否有审核权限-->
                <#if security.hasEntityPermission("PRODPROMO_COUPON", "_AUDIT", session)>
                    <button id="btn_pass" class="btn btn-primary" style="display:none;">
                        批量通过
                    </button>
                    <button id="btn_refuse" class="btn btn-primary" style="display:none;">
                        批量拒绝
                    </button>
                </#if>
                </div>
            </div><!-- 操作按钮组end -->
        </div>
        <!-- tab选项卡begin -->
        <ul id="Business_Nav" class="nav nav-tabs" style="margin-bottom: 10px;">
            <li class="active"><a id="" href="javascript:void(0);">全部</a></li>
            <li><a id="ACTY_AUDIT_INIT" href="javascript:void(0);">待审核</a></li>
            <li><a id="ACTY_AUDIT_PASS" href="javascript:void(0);">已通过</a></li>
            <li><a id="ACTY_AUDIT_NOPASS" href="javascript:void(0);">已驳回</a></li>
        </ul><!-- tab选项卡end -->

        <!--工具栏start -->
        <div class="row m-b-10">


            <!-- 列表当前分页条数start -->
            <div class="col-sm-12">
                <div id="Business_ViewSize" class="dp-tables_length">
                </div>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->
        <!-- 表格区域start -->
        <div class="row">
            <div class="col-sm-12">
                <table id="Business_DataTbl" class="table table-bordered table-hover js-checkparent">
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <div class="row" id="Business_Paginate">
        </div><!-- 分页条end -->
    </div><!-- /.box-body -->
</div><!-- 内容end -->


<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
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
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="exampleModalLabel">优惠券下架</h4>
            </div>
            <div class="modal-body">
                <input type="hidden" id="couponCode">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer" style="text-align: center;">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">下架</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
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
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
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
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_add_title">优惠券-批量通过</h4>
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
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_add_title">优惠券-批量拒绝</h4>
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
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
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
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_add_title">促销审核</h4>
            </div>
            <div class="modal-body">
                <form id="ApprovalForm" method="post" class="form-horizontal"
                      action="<@ofbizUrl>approvalArticle</@ofbizUrl>">
                    <div class="form-group">
                        <label class="control-label col-sm-2">促销编码:</label>
                        <div class="col-sm-8 p-t-5">
                            <span id="couponCode"></span>
                            <input type="hidden" class="form-control" id="couponCodes" name="couponCode">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-sm-2">促销名称:</label>
                        <div class="col-sm-8 p-t-5">
                            <span id="couponName"></span>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group" data-type="minCheck" data-number="1" data-mark="操作">
                            <label class="control-label col-sm-2">操作:</label>
                            <div class="col-sm-10">
                                <div class="radio">
                                    <label class="col-sm-4"><input name="couponStatus" type="radio"
                                                                   value="ACTY_AUDIT_PASS" checked class="radioItem">通过</label>
                                    <label class="col-sm-4"><input name="couponStatus" type="radio"
                                                                   value="ACTY_AUDIT_NOPASS"
                                                                   class="radioItem">拒绝</label>
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


<!--优惠券详情弹出框start -->
<div id="modal_detailCoupon" class="modal fade  bs-example-modal-lg" tabindex="-1" role="dialog"
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
                <!--优惠券编码  优惠券类型start-->
                <div class="row title">
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-4 control-label">优惠券编码:</label>
                        <div class="col-sm-3">
               <span id="couponCode"><span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-4 control-label">优惠券类型:</label>
                        <div class="col-sm-6">
               <span id="couponType"><span>
                        </div>
                    </div>
                </div>

                <!--促销状态start-->
                <div class="row title">
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-6 control-label" id="payFill"></label>
                        <div class="col-sm-3">
                        </div>
                    </div>
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-4 control-label">促销状态:</label>
                        <div class="col-sm-6">
               <span id="couponStatus"><span>
                        </div>
                    </div>
                </div>
                <!--优惠券名称 优惠券数量start-->
                <div class="row title">
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-4 control-label">优惠券名称:</label>
                        <div class="col-sm-6">
               <span id="couponName"><span>
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
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-4 control-label">每天限领数量:</label>
                        <div class="col-sm-6">
               <span id="couponPerDay"><span>
                        </div>
                    </div>
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
                <!--有效开始时间 有效结束时间start-->
                <div class="row title">
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
                        <div class="col-sm-3">
               <span id="publishType"><span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6" id="use_Integral">
                        <label for="subTitle" class="col-sm-4 control-label">使用积分:</label>
                        <div class="col-sm-3">
               <span id="useIntegral"><span>
                        </div>
                    </div>
                    <div class="form-group col-sm-6" id="afterPay_Num">
                        <div class="col-sm-6">
               <span id="afterPayNum"><span>
                        </div>
                    </div>
                </div>
                <!--发放方式start-->
                <!--所属商家start-->
                <!--
                 <div class="row title">
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
                        <h3 class="box-title">优惠券范围</h3>
                    </div>
                    <div class="box-body table-responsive no-padding">
                    </div>
                </div>
                <div class="row title">
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-4 control-label">优惠券范围:</label>
                        <div class="col-sm-3">
               <span id="couponRange"><span>
                        </div>
                    </div>
                </div>


                <div class="box box-info js-party-level">
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
                    </div>
                </div>
                <div class="modal-footer">
                    <button id="cancel" type="button" class="btn btn-default" j
                            data-dismiss="modal">取消
                    </button>
                </div>
            </div>
        </div>
    </div><!-- 优惠券详情弹出框end -->


    <!-- script区域start -->
    <script>
        var Business_DataTbl;
        var Business_AjaxUrl = "findCoupons?couponRange=COUPON_RANGE_COMM";
        $(function () {
            CKEDITOR.replace("Centent");
            CKEDITOR.on('instanceReady', function (event) {
                editor = event.editor;
                editor.setReadOnly(true); //只读
            });
            Business_DataTbl = $('#Business_DataTbl').dataTable({
                ajaxUrl: Business_AjaxUrl,
                columns: [
                    <!-- 是否有审核权限-->
                <#if security.hasEntityPermission("PRODPROMO_COUPON", "_AUDIT", session)>
                    {"title": "复选框", "code": "couponCode", "checked": true, "hidden": "true"},
                </#if>
                    {"title": "优惠券编码", "code": "couponCode", "sort": true},
                    {"title": "优惠券名称", "code": "couponName", "sort": true},
                    {
                        "title": "优惠券类型", "code": "couponType",
                        "handle": function (td, record) {
                            if (record.couponType == "COUPON_TYPE_CASH") {
                                td.html("现金抵用");
                            } else if (record.couponType == "COUPON_TYPE_REDUCE") {
                                td.html("满减类型");
                            }
                        }
                    },
                    {
                        "title": "发放方式", "code": "publishType",
                        "handle": function (td, record) {
                            if (record.publishType == "COUPON_PRDE_DIR") {
                                td.html("直接领取");
                            } else if (record.publishType == "COUPON_PRDE_SCORE") {
                                td.html("积分兑换");
                            } else if (record.publishType == "COUPON_PRDE_PAY") {
                                td.html("支付后领取");
                            }
                        }
                    },
                    {
                        "title": "优惠范围", "code": "couponRange",
                        "handle": function (td, record) {
                            if (record.couponRange == "COUPON_RANGE_COMM") {
                                td.html("通用类型");
                            } else if (record.couponRange == "COUPON_RANGE_SINGLE") {
                                td.html("单品优惠");
                            } else if (record.couponRange == "COUPON_TYPE_CATE") {
                                td.html("分类优惠");
                            } else if (record.couponRange == "COUPON_TYPE_BRAND") {
                                td.html("品牌优惠");
                            }
                        }
                    },
                    {"title": "优惠券数量", "code": "couponQuantity"},
                    {"title": "已发放数量", "code": "userCount"},
                    {"title": "已使用数量", "code": "orderCount"},
                    {
                        "title": "促销状态", "code": "createdStamp",
                        "handle": function (td, record) {
                            if (record.couponStatus == "ACTY_AUDIT_INIT") {
                                td.html("待审核");
                            } else if (record.couponStatus == "ACTY_AUDIT_NOPASS") {
                                td.html("已驳回");
                            } else if (record.couponStatus == "ACTY_AUDIT_PASS") {
                                if (Date.parse(record.endDate) > Date.parse(record.nowDate)) {
                                    td.html("已通过");
                                } else if (Date.parse(record.endDate) < Date.parse(record.nowDate)) {
                                    td.html("已结束");
                                }
                            }
                        }
                    },
                    {
                        "title": "操作", "code": "option",
                        "handle": function (td, record) {
                            var liGroup = "";
                            if (record.couponStatus == "ACTY_AUDIT_INIT") {
                                <!-- 是否有编辑权限-->
                            <#if security.hasEntityPermission("PRODPROMO_COUPON", "_UPDATE", session)>
                                liGroup += "<li class='edit_li'><a href='javascript:editPromCoupon(\"" + record.couponCode + "\")'>编辑</a></li>";
                            </#if>
                                <!-- 是否有审核权限-->
                            <#if security.hasEntityPermission("PRODPROMO_COUPON", "_AUDIT", session)>
                                liGroup += "<li class='audit_li'><a href='javascript:auditPromoCoupon(\"" + record.couponCode + "\")'>审核</a></li>";
                            </#if>
                            } else if (record.couponStatus == "ACTY_AUDIT_NOPASS") {
                                <!-- 是否有编辑权限-->
                            <#if security.hasEntityPermission("PRODPROMO_COUPON", "_UPDATE", session)>
                                liGroup += "<li class='edit_li'><a href='javascript:editPromCoupon(\"" + record.couponCode + "\")'>编辑</a></li>";
                            </#if>
                                <!-- 是否有查看权限-->
                            <#if security.hasEntityPermission("PRODPROMO_COUPON", "_LIST", session)>
                                liGroup += "<li class='audit_li'><a href='javascript:findAuditMessage(\"" + record.couponCode + "\")'>查看驳回原因</a></li>";
                            </#if>
                            } else if (record.couponStatus == "ACTY_AUDIT_PASS") {
                                if (Date.parse(record.endDate) > Date.parse(record.nowDate)) {
                                    <!-- 是否有下架权限-->
                                <#if security.hasEntityPermission("PRODPROMO_COUPON", "_UPDATE", session)>
                                    liGroup += "<li class='edit_li'><a href='javascript:editEndDate(\"" + record.couponCode + "\")'>下架</a></li>";
                                </#if>
                                }
                                <!-- 是否有查看权限-->
                            <#if security.hasEntityPermission("PRODPROMO_COUPON", "_LIST", session)>
                                liGroup += "<li class='edit_li'><a href='javascript:findCouponcode(\"" + record.couponCode + "\",\"" + record.couponQuantity + "\",\"" + record.userCount + "\",\"" + record.orderCount + "\")'>已领取列表</a></li>";
                            </#if>
                            }
                            var btnGroup = "<div class='btn-group'>" +
                                    "<button type='button' class='btn btn-danger btn-sm' onclick='javascript:couponDetail(\"" + record.couponCode + "\")'>查看详情</button>" +
                                    "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>" +
                                    "<span class='caret'></span>" +
                                    "<span class='sr-only'>Toggle Dropdown</span>" +
                                    "</button>" +
                                    "<ul class='dropdown-menu' role='menu'>" +
                                    liGroup +
                                    "</ul>" +
                                    "</div>";
                            td.html(btnGroup);
                        }
                    }
                ],
                listName: "recordsList",
                paginateEL: "Business_Paginate",
                viewSizeEL: "Business_ViewSize"
            });

            //查询按钮点击事件
            $('#Business_QueryForm #searchBtn').on('click', function () {
                var couponCode = $('#Business_QueryForm #couponCode').val();
                var couponName = $('#Business_QueryForm #couponName').val();
                var couponType = $('#Business_QueryForm #couponType').val();
                var publishType = $('#Business_QueryForm #publishType').val();
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "couponCode", couponCode);
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "couponName", couponName);
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "couponType", couponType);
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "publishType", publishType);
                Business_DataTbl.reload(Business_AjaxUrl);
                return false;
            });

            //切换选项卡
            $('#Business_Nav a').click(function (e) {
                if (!$(this).closest('li').hasClass('active')) {
                    $(this).closest('li').addClass('active').siblings('.active').removeClass('active');
                }
                var couponStatus = $(this).attr('id');
                var couponCode = $('#Business_QueryForm #couponCode').val();
                var couponName = $('#Business_QueryForm #couponName').val();
                var promoType = $('#Business_QueryForm #promoType').val();
                var publishType = $('#Business_QueryForm #publishType').val();
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "couponCode", couponCode);
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "couponName", couponName);
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "promoType", promoType);
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "publishType", publishType);
                Business_AjaxUrl = changeURLArg(Business_AjaxUrl, "couponStatus", couponStatus);
                Business_DataTbl.reload(Business_AjaxUrl, function () {
                    switch (couponStatus) {
                        case 'ACTY_AUDIT_INIT': {
                            $('#Business_DataTbl .js-allcheck').closest('th').show();
                            $('#Business_DataTbl .js-checkchild').closest('td').show();
                            $('#btn_pass').show();
                            $('#btn_refuse').show();
                        }
                            break;
                        case 'ACTY_AUDIT_NOPASS': {
                            $('#Business_DataTbl .js-allcheck').closest('th').hide();
                            $('#Business_DataTbl .js-checkchild').closest('td').hide();
                            $('#btn_pass').hide();
                            $('#btn_refuse').hide();
                        }
                            break;
                        case 'ACTY_AUDIT_PASS': {
                            $('#Business_DataTbl .js-allcheck').closest('th').hide();
                            $('#Business_DataTbl .js-checkchild').closest('td').hide();
                            $('#btn_pass').hide();
                            $('#btn_refuse').hide();
                        }
                            break;
                        default: {
                            $('#Business_DataTbl .js-allcheck').closest('th').hide();
                            $('#Business_DataTbl .js-checkchild').closest('td').hide();
                            $('#btn_pass').hide();
                            $('#btn_refuse').hide();
                        }
                            break;
                    }
                });
            });

        });


        //优惠券下架
        function editEndDate(couponCode) {
            $('#editEndDate_Modal #couponCode').val(couponCode);
            $('#editEndDate_Modal #modal_confirm_body').html("下架后不再发放优惠券，已发放的优惠券按照当前的有效时间使用");
            $('#editEndDate_Modal').modal('show');
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

        function deletePromo(productPromoId) {
            deleteId = productPromoId;
            $('#modal_confirm #modal_confirm_body').html("确定删除此满减吗？");
            $('#modal_confirm').modal('show');
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
            if (couponStatus == 'ACTY_AUDIT_NOPASS') {
                $('#label_gss').html('<i class="required-mark">*</i>审核意见:');
                $('#modal_auditPromo #auditMessage').addClass('dp-vd');
            } else {
                $('#couponStatus').html('审核意见:');
                $('#modal_auditPromo #auditMessage').removeClass('dp-vd');
            }
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
                        $('#modal_detailCoupon #couponPerDay').text(productPromoCoupon.couponPerDay);
                        $('#modal_detailCoupon #startDate').text(timeStamp2String(productPromoCoupon.startDate.time));
                        $('#modal_detailCoupon #endDate').text(timeStamp2String(productPromoCoupon.endDate.time));
                        $('#modal_detailCoupon #useBeginDate').text(timeStamp2String(productPromoCoupon.useBeginDate.time));
                        $('#modal_detailCoupon #useEndDate').text(timeStamp2String(productPromoCoupon.useEndDate.time));
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
                            $('#modal_detailCoupon #publishType').text('直接领取');
                            $('#modal_detailCoupon #use_Integral').hide();
                        } else if (productPromoCoupon.publishType == 'COUPON_PRDE_SCORE') {
                            $('#modal_detailCoupon #publishType').text('积分兑换');
                            $('#modal_detailCoupon #use_Integral').show();
                            $('#modal_detailCoupon #useIntegral').text(productPromoCoupon.useIntegral);
                        } else if (productPromoCoupon.publishType == 'COUPON_PRDE_PAY') {
                            $('#modal_detailCoupon #publishType').text('支付后领取');
                            $('#modal_detailCoupon #use_Integral').hide();
                            $('#modal_detailCoupon #afterPay_Num').show();
                            var afterPayUom = '';
                            if (productPromoCoupon.afterPayUom == 'Yuan') {
                                afterPayUom = '元';
                            } else {
                                afterPayUom = '件';
                            }
                            var afterpay = '订单满' + productPromoCoupon.afterPayNum + afterPayUom + '领取';
                            $('#modal_detailCoupon #afterPayNum').text(afterpay);
                        }
                        if (productPromoCoupon.couponRange == 'COUPON_RANGE_COMM') {
                            $('#modal_detailCoupon #couponRange').text('通用类型');
                        } else if (productPromoCoupon.couponRange == 'COUPON_RANGE_SINGLE') {
                            $('#modal_detailCoupon #couponRange').text('单品优惠');
                        } else if (productPromoCoupon.couponRange == 'COUPON_TYPE_CATE') {
                            $('#modal_detailCoupon #couponRange').text('分类优惠');
                        } else if (productPromoCoupon.couponRange == 'COUPON_TYPE_BRAND') {
                            $('#modal_detailCoupon #couponRange').text('品牌优惠');
                        }

                        if (productPromoCoupon.couponStatus == 'ACTY_AUDIT_INIT') {
                            $('#modal_detailCoupon #couponStatus').text('待审核');
                        } else if (productPromoCoupon.couponStatus == 'ACTY_AUDIT_PASS') {
                            $('#modal_detailCoupon #couponStatus').text('已通过');
                        } else if (productPromoCoupon.couponStatus == 'ACTY_AUDIT_NOPASS') {
                            $('#modal_detailCoupon #couponStatus').text('已驳回');
                        }
                    }
                    var productCouponPartyLevel = data.productCouponPartyLevel;
                    var tr = "";
                    if (productCouponPartyLevel) {
                        for (var i = 0; i < productCouponPartyLevel.length; i++) {
                            tr += "<tr>"
                                    + "<td>" + (i + 1) + "</td>"
                                    + "<td>" + productCouponPartyLevel[i] + "</td>"
                                    + "</tr>"
                        }
                        $('#partyLevelTable>tbody').empty();
                        $('#partyLevelTable>tbody').append(tr);
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
        $('#btn_pass').click(function () {
            var checks = $('#Business_DataTbl .js-checkchild:checked');
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
        $('#btn_refuse').click(function () {

            var checks = $('#Business_DataTbl .js-checkchild:checked');
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
                ("请至少选择一条！");
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

        function findCouponcode(couponCode, couponQuantity, userCount, orderCount) {
            $.dataSelectModal({
                url: "/prodPromo/control/findCouponCode?externalLoginKey=${externalLoginKey}&couponCode=" + couponCode + '&couponQuantity=' + couponQuantity + '&userCount=' + userCount + '&orderCount=' + orderCount,
                width: "800",
                title: "优惠券列表",
            });
        }
    </script><!-- script区域end -->
