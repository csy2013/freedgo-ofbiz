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
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>"
      type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "businessBrandList?lookupFlag=Y&"+ paramList +"&">
<#assign lookupFlag = "Y"/>
<#assign auditStatus = auditStatus?default('')/>
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form class="form-inline clearfix" role="form" method="post" id="QueryForm"
              action="<@ofbizUrl>findPartyBusinessBrand</@ofbizUrl>">
            <input type="hidden" name="auditStatus" id="auditStatus" value="">
            <input type="hidden" name="lookupFlag" value="Y">
            <div class="form-group w-p100">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">品牌名称</span>
                    <input type="text" class="form-control" name="brandName" placeholder="品牌名称"
                           value="${requestParameters.brandName?default("")}">
                </div>

                <div class="input-group pull-right p-l-10">
                <#if security.hasEntityPermission("BUSINESSMGR_BRAND_LIST", "_VIEW", session)>
                    <button class="btn btn-success btn-flat">搜索</button>
                </#if>
                </div>
            </div>
        </form>


        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>

        <div class="row m-b-12" style="margin-bottom:15px;">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("BUSINESSMGR_BRAND", "_AUDIT", session)>
                    <a class="btn btn-primary" href="<@ofbizUrl>businessBrandAuthorize</@ofbizUrl>">品牌授权申请</a>
                </#if>
                </div>
            </div>
        </div>


            <!--  <#if lookupFlag == "Y">-->
        <ul class="nav nav-tabs">
            <li role="presentation" <#if auditStatus?default("") == "">class="active"</#if>><a
                    href="<@ofbizUrl>findPartyBusinessBrand?lookupFlag=Y</@ofbizUrl>">全部</a></li>
            <li role="presentation" <#if auditStatus?default("") == "0">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('0')">待审核</a></li>
            <li role="presentation" <#if auditStatus?default("") == "1">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('1')">已通过</a></li>
            <li role="presentation" <#if auditStatus?default("") == "2">class="active"</#if>><a
                    href="javascript:void(0)" onclick="changeTab('2')">已驳回</a></li>
        </ul>
            <!--</#if>-->
        <!-- 表格区域start -->
    <#if brandList?has_content>
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

                        <th>品牌logo</th>
                        <th>品牌名称</th>
                        <th>品牌别名</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list brandList as brand>
                        <tr>
                            <td>
                                <#if brand.contentId?has_content>
                                    <#assign src='/content/control/stream?contentId='>
                                    <#assign imgsrc = src +brand.contentId/>

                                <#else>
                                    <#assign imgsrc = '<@ofbizContentUrl>/images/products/10000/original.jpg'/>
                                </#if>
                                <img height="100" src="${imgsrc}" class="cssImgSmall" alt=""/>
                            </td>
                            <td>
                            ${brand.brandName?if_exists}
                                <input type="hidden" name="checkBrandName" id="checkBrandName"
                                       value="${brand.brandName?if_exists}"/>
                            </td>
                            <td>
                            ${brand.brandNameAlias?if_exists}
                                <input type="hidden" name="checkBrandNameAlias" id="checkBrandNameAlias"
                                       value="${brand.brandNameAlias?if_exists}"/>
                            </td>
                            <td>
                                <#if brand.auditStatus=='0'>待审核
                                <#elseif brand.auditStatus=='1'>已通过
                                <#elseif brand.auditStatus=='2'>已驳回
                                </#if>
                            </td>
                            <td>
                                <div class="btn-group">
                                    <#if security.hasEntityPermission("BUSINESSMGR_BRAND_LIST", "_VIEW", session)>
                                        <button onclick="brandDetail('${brand.productBrandId}','${brand.brandName?if_exists}','${brand.brandNameAlias?if_exists}','${imgsrc}')"
                                                type="button" class="js-button btn btn-danger btn-sm">查看品牌
                                        </button>
                                    </#if>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown" aria-expanded="false">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                        <#if (security.hasEntityPermission("BUSINESSMGR_BRAND", "_DELETE", session))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="deleteBrand(${brand.partyBusinessBrandId})">删除</a></li>
                                        </#if>
                                        <#if (security.hasEntityPermission("BUSINESSMGR_BRAND_LIST", "_VIEW", session) && (brand.auditStatus=='2'))>
                                            <li><a href="javascript:void(0)"
                                                   onclick="findProductSeckillAuditMessage('${brand.partyBusinessBrandId}')">查看驳回原因</a>
                                            </li>
                                        </#if>
                                    <#--<#if (security.hasEntityPermission("PRODPROMO_SECKILL", "_LIST", session))>-->
                                    <#--<li><a href="javascript:void(0)" onclick="auditLog(${brand.activityId})">操作日记</a></li>-->
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
        <#assign commonUrl = "businessBrandList?lookupFlag=Y&"+ paramList + "&"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(brandListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", brandListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=brandListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
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
        </div>
        <!-- 提示弹出框end -->

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
        </div>
        <!-- 删除确认弹出框end -->

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
        </div>
        <!-- ${uiLabelMap.BrandDel}确认弹出框end -->

        <!--品牌详情弹出框start 需要修改 -->
        <div id="modal_detail_secKill" class="modal fade  bs-example-modal-lg" tabindex="-1" role="dialog"
             aria-labelledby="modal_edit_title">
            <div class="modal-dialog modal-lg" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_edit_title">品牌详细信息</h4>
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
                                <label for="subTitle" class="col-sm-6 control-label">品牌名称:</label>
                                <div class="col-sm-6">
                                    <span id="brandName"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-4">
                                <label for="subTitle" class="col-sm-6 control-label">店铺名称:</label>
                                <div class="col-sm-6">
                                    <span id="partyName"><span>
                                </div>
                            </div>
                            <div class="form-group col-sm-4">
                                <label for="subTitle" class="col-sm-6 control-label">品牌别名:</label>
                                <div class="col-sm-6">
                                    <span id="brandNameAlias"><span>
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
        </div>
        <!--品牌详情弹出框end -->

    <#-- 点击品牌查看弹出 -->
        <div class="modal fade" id="addBrand" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title">品牌详情</h4>
                    <#-- ${uiLabelMap.CreateBrand}-->
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="updateProductBrand" method="post" action=""
                              name="updateProductBrand" enctype="multipart/form-data">

                            <div class="form-group">
                                <label class="control-label col-sm-2"><span style="color: red">*</span>品牌名称:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" readonly name="brandName"
                                           id="brandName">
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="message-text" class="control-label col-sm-2">品牌别名:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" readonly name="brandNameAlias"
                                           id="brandNameAlias">
                                </div>
                            </div>

                            <!-- Log图片 start-->
                            <div class="form-group">
                                <label class="control-label col-sm-2"><span style="color: red">*</span>Log图片</label>
                                <div class="col-sm-10">
                                    <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                                </div>
                            </div>

                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-default" data-dismiss="modal">确定</button>
                    <#--<button type="button" class="btn btn-primary" id="btnBrandSave">${uiLabelMap.BrandSave}</button>-->
                    </div>
                </div>
            </div>
        </div>
    <#-- 点击品牌查看弹出 -->


        <script>
            var setting = {
                check: {
                    enable: true
                <#--
                enable: true,
                chkStyle:"radio",
                radioType:"all"
                -->
                },
                data: {
                    simpleData: {
                        enable: true
                    }
                },
                view: {
                    showIcon: false
                }
            };
            var deleteId = '';
            var batchDeleteIds = '';

            function brandDetail(brandId, brandName, brandAlisName, imgsrc) {
                $('#addBrand #brandName').val(brandName)
                $('#addBrand #brandNameAlias').val(brandAlisName)
                $('#addBrand #img').attr('src', imgsrc);
                $('#addBrand').modal();
            }

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

                //删除弹出框删除按钮点击事件
                $('#modal_confirm #ok').click(function (e) {
                    //异步调用删除方法
                    deleteSecKillByIds(deleteId);
                });

                $('.js-buttonId1111').on('click', function () {
                    var $model = $('#addBrand');
                    var tr = $(this).closest('tr');

                    var productBrandId = tr.find('input[name=curProductBrandId]').val();//品牌Id
                    var contentId = tr.find('input[name=contentId]').val();//图片Logo ContentId
                    var brandName = tr.find('input[name=checkBrandName]').val();//品牌名称
                    var brandNameAlias = tr.find('input[name=checkBrandNameAlias]').val();//品牌别名

                    $model.find('input[name=brandName]').val(brandName);//品牌名称
                    $model.find('input[name=brandNameAlias]').val(brandNameAlias);//品牌别名

                    // $model.find('input[name=operateType]').val("update");//操作
                    $model.find('input[name=productBrandId]').val(productBrandId);//品牌Id
                    $model.find('input[name=contentId]').val(contentId);//图片Logo ContentId
                    $model.find('#exampleModalLabel').text("品牌详情");
                    var contentId = "/content/control/stream?contentId=" + contentId;
                    $('#addBrand #img').attr('src', contentId);
                    if (certificateContentId) {
                        var certificateContentId = "/content/control/stream?contentId=" + certificateContentId;
                        $('#addBrand #img1').attr('src', certificateContentId);
                    }
                    //加载商品分类
                    getProductBrandCategoryList(productBrandId);

                })

            });

            // 取得品牌对应的分类
            function getProductBrandCategoryList(productBrandId) {
                // 选中的项目
                $.ajax({
                    url: "<@ofbizUrl>getPartyProductBrandCategoryList</@ofbizUrl>",
                    type: "GET",
                    data: {productBrandId: productBrandId},
                    dataType: "json",
                    success: function (cateGoryData) {
                        //异步加载所有分类数据
                        $.ajax({
                            url: "<@ofbizUrl>getPartyProductCategoryListByParyId</@ofbizUrl>",//getPartyProductCategorys
                            type: "POST",
                            dataType: "json",

                            success: function (data) {
                                var treeObj = $.fn.zTree.init($("#addProductCategoryArea"), setting, data.productCategoryList);
                                //自动勾选复选框
                                $.each(cateGoryData.paramMap.productBrandCategoryList, function () {
                                    var node = treeObj.getNodeByParam("id", this.productCategoryId);
                                    if (node) {
                                        treeObj.checkNode(node);
                                        //自动展开子节点
                                        if (!node.isParent) {
                                            treeObj.expandNode(node.getParentNode(), true, true, true);
                                        }
                                    } else {

                                    }

                                })
                                $('#addBrand').modal();
                            },
                            error: function (data) {
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                                $('#modal_msg').modal();
                            }
                        });
                    },
                    error: function (data) {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                        $('#modal_msg').modal();
                    }
                });
            }

            // 状态值切换
            function changeTab(auditStatus) {
                $("#auditStatus").val(auditStatus);//activityAuditStatus
                $("#QueryForm").submit();
            }

            // 查看品牌审核 驳回原因
            function findProductSeckillAuditMessage(partyBusinessBrandId) {
                $.ajax({
                    url: "findBrandAuditMessage",//findBrandAuditMessage
                    type: "POST",
                    data: {
                        partyBusinessBrandId: partyBusinessBrandId
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

            // 根据productBrandId查看品牌详情
            function productSeckillDetail(productBrandId) {
                jQuery.ajax({
                    url: '<@ofbizUrl>findPartyProductBrandById</@ofbizUrl>',
                    type: 'POST',
                    data: {
                        'productBrandId': productBrandId
                    },
                    success: function (data) {
                        if (data.brandList) {
                            var brandList = data.brandList;
                            $('#modal_detail_secKill #brandName').text(brandList[0].brandName);
                            $('#modal_detail_secKill #partyName').text(brandList[0].partyName);
                            $('#modal_detail_secKill #brandNameAlias').text(brandList[0].brandNameAlias);

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
            function deleteBrand(partyBusinessBrandId) {
                $.confirmLayer({
                    msg: '确定删除此品牌吗？',
                    confirm: function () {
                        $.ajax({
                            url: "deletePartyProductBrand",
                            type: "GET",
                            data: {partyBusinessBrandId: partyBusinessBrandId},
                            dataType: "json",
                            success: function (data) {
                                if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                                    $.tipLayer(data._ERROR_MESSAGE_);
                                }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                                    $.tipLayer(data._ERROR_MESSAGE_LIST_);
                                } else{
                                    $('#confirmLayer').modal('hide');
                                    $('#modal_msg #modal_msg_body').html("删除成功！");
                                    $('#modal_msg').modal();
                                    $('#modal_msg').on('hide.bs.modal', function () {
                                        //重新加载table
                                        window.location.reload();
                                    })
                                }

                            },
                            error: function (data) {
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                                $('#modal_msg').modal();
                            }
                        });

                    }
                })

            }

            /**
             * 根据品牌id删除品牌
             *
             * @param ids
             */
            function deleteSecKillByIds(ids) {
                //异步调用删除方法
                $.ajax({
                    url: "deletePartyProductBrand",
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


