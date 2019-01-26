<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>"
      type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/jquery/plugins/upload/ajaxupload.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "businessBrandAuthorize?lookupFlag=Y"+ paramList +"&" >
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form class="form-inline clearfix" role="form" action="<@ofbizUrl>businessBrandAuthorize</@ofbizUrl>">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.BrandName}</span>
                    <input type="text" class="form-control" id="brandName" name="brandName" value="${requestParameters.brandName?default("")}"/>
                </div>

            </div>
            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
            </div>
        </form>
        <!-- 条件查询end -->

    <#-- 自定义按钮功能 -->
        <div class="modal fade" id="addBrand" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.CreateBrand}</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="updateProductBrand" method="post" action=""
                              name="updateProductBrand">
                            <input type="hidden" name="productStoreId" id="productStoreId"
                                   value="${requestAttributes.productStoreId}"/>
                            <div class="form-group" data-type="required,max" data-mark="品牌名称" data-number="30">
                                <label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.BrandName}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" name="brandName" id="brandName">
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>

                            <div class="form-group" data-type="max" data-number="30">
                                <label for="message-text" class="control-label col-sm-2">${uiLabelMap.BrandNameAlias}
                                    :</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" name="brandNameAlias" id="brandNameAlias">
                                    <p class="dp-error-msg"></p>

                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-2 control-label"><i class="required-mark">*</i>Logo图片</label>
                                <div class="col-sm-8">
                                    <img alt="" src="" id="img_logoContentId"
                                         style="max-height: 100px;max-width: 200px;">
                                    <button type="button" class="btn btn-primary" id="logoImgUpload"
                                            name="logoImgUpload" value="选择图片">
                                        <span class="glyphicon glyphicon-picture" aria-hidden="true"></span>选择图片
                                    </button>
                                    <input type="hidden" name="logoContentId" id="logoContentId"/>
                                </div>
                            </div>

                            <#--<div class="form-group">
                                <label class="col-sm-2 control-label"><i class="required-mark">*</i>品牌证书</label>
                                <div class="col-sm-8">
                                    <img alt="" src="" id="img_licenseContentId"
                                         style="max-height: 100px;max-width: 200px;">
                                    <button type="button" class="btn btn-primary" id="licenseImgUpload"
                                            name="licenseImgUpload" value="选择图片">
                                        <span class="glyphicon glyphicon-picture" aria-hidden="true"></span>选择图片
                                    </button>
                                    <input type="hidden" name="licenseContentId" id="licenseContentId"/>
                                </div>
                            </div>-->

                           <#-- <div class="form-group">
                                <input type="hidden" id="productCategoryIds" name="productCategoryIds"/>
                                <label class="control-label col-sm-2">商品分类:</label>
                                <div class="col-sm-10">
                                    <div class="zTreeDemoBackground left">
                                        <ul id="addProductCategoryArea" class="ztree"></ul>
                                    </div>
                                </div>
                            </div>-->

                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary" id="btnBrandSave">${uiLabelMap.BrandSave}</button>
                        <button type="button" class="btn btn-default"
                                data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 删除确认弹出框start -->
        <div id="modal_confirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
             aria-labelledby="modal_confirm_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
                    </div>
                    <div class="modal-body">
                        <h4 id="modal_confirm_body"></h4>
                    </div>
                    <div class="modal-footer">
                        <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
                    </div>
                </div>
            </div>
        </div>
        <!-- 删除确认弹出框end -->

        <!-- 提示弹出框start -->
        <div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
             aria-labelledby="modal_msg_title">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                                aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="modal_msg_title">操作信息</h4>
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


        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>


        <div class="row m-b-12">
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                <#if security.hasEntityPermission("BUSINESSMGR_BRAND", "_AUDIT", session)>
                    <button id="btnAuthorize" class="btn btn-primary">申请授权</button>
                </#if>
                <#if security.hasEntityPermission("BUSINESSMGR_BRAND", "_CREATE", session)>
                    <button class="btn btn-primary" id="btn_add">申请自定义授权</button>
                </#if>
                </div>
            </div>
            <!-- 列表当前分页条数start -->
            <div class="dp-tables_length">
                <label>
                ${uiLabelMap.DisplayPage}
                    <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                            onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                        <option value="5" <#if viewSize ==5>selected</#if>>5</option>
                        <option value="15" <#if viewSize==15>selected</#if>>15</option>
                        <option value="20" <#if viewSize==20>selected</#if>>20</option>
                        <option value="25" <#if viewSize==25>selected</#if>>25</option>
                    </select>
                ${uiLabelMap.BrandItem}
                </label>
            </div>
        </div>



    <#-- 分页 -->
    <#if productBrandList?has_content>
        <input id="ids" type="hidden"/>

        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent js-sort-list">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox" id="checkAll"></th>
                        <th>${uiLabelMap.BrandLogo}
                            <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="logoContentId"
                               id="a_contentId"></a>
                        </th>
                        <th>${uiLabelMap.BrandName}
                            <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="brandName"
                               id="a_brandName"></a>
                        </th>
                        <th>${uiLabelMap.BrandNameAlias}
                            <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="brandNameAlias"
                               id="a_brandNameAlias"></a>
                        </th>
                    <#--<th>${uiLabelMap.BrandIsUsed}-->
                    <#--<a class="fa fa-sort-amount-desc text-muted js-sort" data-key="isUsed" id="a_isUsed"></a>-->
                    <#--</th>-->
                    <#--<th>${uiLabelMap.BrandOption}</th>-->
                    <#--<th>品牌详情-->
                    <#--<a class="fa fa-sort-amount-desc text-muted js-sort" data-key="brandDesc" id="a_brandDesc"></a>-->
                    <#--</th>-->
                    </tr>
                    </thead>
                    <tbody>
                    <form method="post" action="" name="editProductBrand" id="editProductBrand">
                        <#list productBrandList as productBrandRow>
                            <tr>
                                <td><input class="js-checkchild" type="checkbox"
                                           id="${productBrandRow.productBrandId?if_exists}"
                                           value="${productBrandRow.productBrandId?if_exists}"/>
                                    <input type="hidden" name="curProductBrandId" id="curProductBrandId"
                                           value="${productBrandRow.productBrandId?if_exists}"/>
                                </td>
                                <td>
                                    <#if productBrandRow.contentId?has_content>
                                        <#assign src='/content/control/stream?contentId='>
                                        <#assign imgsrc = src +productBrandRow.contentId/>
                                        <input type="hidden" name="contentId" id="contentId"
                                               value="${productBrandRow.contentId?if_exists}"/>
                                        <img height="100" src="${imgsrc}" class="cssImgSmall" alt=""/>
                                    </#if>
                                </td>
                                <td>${productBrandRow.brandName?if_exists}</td>
                                <td>${productBrandRow.brandNameAlias?if_exists}</td>
                            <#--<td>${productBrandRow.brandDesc?if_exists}</td>-->

                            </tr>
                        </#list>
                    </form>
                    </tbody>
                </table>
            </div>
        </div>
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(productBrandListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", productBrandListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=productBrandListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
    <#else>
        <div id="">
            <h3>没有商品品牌数据</h3>
        </div>
    </#if>

        <!-- 表格区域end -->
        <!-- 分页条start -->
    <#--<div class="row" id="Business_Paginate">-->
    <#--</div><!-- 分页条end &ndash;&gt;-->
    </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script language="JavaScript" type="text/javascript">

    var setting = {
        check: {
            enable: true
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

    var isCommitted = false;//表单是否已经提交标识，默认为false

    $(function () {

        // 初始化图片选择
        $.chooseImage.int({
            userId: '',
            serverChooseNum: 1,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });

        //图片保存按钮事件
        $('body').on('click', '.img-submit-btn', function () {
            var obj = $.chooseImage.getImgData();
            obj.imgGroupId = imgGroupId;
            $.chooseImage.choose(obj, function (data) {
                $('#img_' + chooseImageModal).attr({"src": "/content/control/getImage?contentId=" + data.uploadedFile0});
                $('#' + chooseImageModal).val(data.uploadedFile0);
            })
        });

        //logo图片
        $('#logoImgUpload').click(function () {
            chooseImageModal = "logoContentId";
            imgGroupId = "PRIVATE_IMG_GROUP";
            $.chooseImage.show();
        });

        //品牌证书
        $('#licenseImgUpload').click(function () {
            chooseImageModal = "licenseContentId";
            imgGroupId = "PRIVATE_IMG_GROUP";
            $.chooseImage.show();
        });

        //新增按钮点击事件
        $('#btn_add').click(function () {
            //异步加载分类数据
            /*$.ajax({
                url: "<@ofbizUrl>getPartyProductCategorys</@ofbizUrl>",// getProductCategoryList
                type: "POST",
                dataType: "json",
                success: function (data) {
                    $.fn.zTree.init($("#addProductCategoryArea"), setting, data.productCategoryList);
                    //设置提示弹出框内容
                    var $model = $('#addBrand');
                    $model.find('#exampleModalLabel').text("添加品牌");
                    $model.find('input[name=brandName]').val("");//品牌名称
                    $model.find('input[name=brandNameAlias]').val("");//品牌别名

                    // $model.find('input[name=operateType]').val("create");//操作
                    $model.find('input[name=productBrandId]').val("");//品牌Id
                    $model.find('input[name=logoContentId]').val("");//图片Logo ContentId
                    $model.find('input[name=licenseContentId]').val("");//证书Logo ContentId
                    $('#addBrand #img_logoContentId').attr('src', "");
                    $('#addBrand #img_licenseContentId').attr('src', "");
                    $('#addBrand').modal();
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                    $('#modal_msg').modal();
                }
            });*/
            $('#addBrand').modal();
        });

        $("#btnBrandSave").click(function () {
            $("#updateProductBrand").dpValidate({
                clear: true
            });
            $("#updateProductBrand").submit();
        });

        // 已经调通
        $("#updateProductBrand").dpValidate({
            validate: true,
            callback: function () {
               /* var treeObj = $.fn.zTree.getZTreeObj("addProductCategoryArea"),
                        nodes = treeObj.getCheckedNodes(true),
                        ids = '';
                for (var i = 0; i < nodes.length; i++) {
                    ids += nodes[i].id + ",";
                }
                var tempIds = ids.substr(0, ids.length - 1);*/

                $.ajax({
                    url: "<@ofbizUrl>createProductBrands</@ofbizUrl>",
                    type: "POST",
                    data: {
                        brandName: $("#updateProductBrand #brandName").val(),
                        brandNameAlias: $("#updateProductBrand #brandNameAlias").val(),
                        logoContentId: $("#updateProductBrand #logoContentId").val(),
                        /*productCategoryIds: tempIds,
                        licenseContentId: $("#updateProductBrand #licenseContentId").val(),
                        productStoreId: $("#productStoreId").val()*/
                    },
                    dataType: "json",
                    success: function (data) {
                        if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                            $.tipLayer(data._ERROR_MESSAGE_);
                        }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                            $.tipLayer(data._ERROR_MESSAGE_LIST_);
                        } else {
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作成功！");
                            $('#modal_msg').modal();
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.href = "<@ofbizUrl>businessBrandList</@ofbizUrl>";
                            });
                        }

                    },
                    error: function (data) {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作失败！");
                        $('#modal_msg').modal();
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.reload();
                        <#--document.location.href="<@ofbizUrl>businessBrandAuthorize</@ofbizUrl>";-->
                        });
                    }
                });

            }
        });

        $("#btnAuthorize").click(function () {
            var ids = "";
            var checks = $('.js-checkparent .js-checkchild:checked');

            //判断是否选中记录
            if (checks.size() > 0) {
                $('input[class="js-checkchild"]:checked').each(function () {
                    if(ids==""){
                        ids=$(this).val();
                    }else{
                        ids = ids + "," + $(this).val();
                    }

                });
                $.confirmLayer({
                    msg: '确定申请选中的品牌吗？',
                    confirm: function () {
                        $.ajax({
                            url: "brandAuth",
                            type: "POST",
                            data:{
                                ids:ids
                            },
                            dataType: "json",
                            success: function (data) {
                                if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                                    $.tipLayer(data._ERROR_MESSAGE_);
                                }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                                    $.tipLayer(data._ERROR_MESSAGE_LIST_);
                                } else{
                                    $("#confirmLayer").modal("hide")
                                    $('#modal_msg #modal_msg_body').html("提交审核成功！");
                                    $('#modal_msg').modal();
                                    $('#modal_msg').on('hide.bs.modal', function () {
                                        window.location.href = "<@ofbizUrl>businessBrandList</@ofbizUrl>";
                                    })
                                }
                            },
                            error: function (data) {
                                $.tipLayer("操作失败！");
                            }
                        });
                    }
                })
            } else {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
                $('#modal_msg').modal();
            }
        });

        //删除弹出框删除按钮点击事件
        $('#modal_confirm #ok').click(function (e) {
            delBrandDataById();
        });

    });

    // 将选择的记录id删除
    function delBrandDataById() {
        // 选中的项目
        var checkedIds = $("#ids").val();
        if (checkedIds != "") {
            jQuery.ajax({
                //软删除 修改根据id修改状态
                url: '<@ofbizUrl>authorizeProductBrandByIds</@ofbizUrl>',
                type: 'POST',
                data: {
                    'checkedIds': checkedIds
                },
                success: function (data) {
                    document.location.href = "<@ofbizUrl>businessBrandAuthorize</@ofbizUrl>";
                }
            });
        }
    }

    //没有用到该方法
    // 判断商品品牌是否是被商品使用
    function isBrandForProduct() {
        var ids = $("#ids").val();
        if (ids != "") {
            jQuery.ajax({
                url: '<@ofbizUrl>isForProductBrand</@ofbizUrl>',
                type: 'POST',
                data: {
                    'ids': ids
                },
                success: function (data) {
                    console.log('返回的数据' + data);
                    var isUsedFlg = data.isUsedFlg;
                    if (isUsedFlg == "Y") {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("该商品品牌已使用，不能申请授权");
                        $('#modal_msg').modal();
                    } else {
                        //设置授权弹出框
                        $('#modal_confirm #modal_confirm_body').html("你确定申请品牌审核吗？");
                        $('#modal_confirm').modal('show');
                    }
                }
            });
        }
    }

    function dosubmit() {
        if (isCommitted == false) {
            isCommitted = true;//提交表单后，将表单是否已经提交标识设置为true
            return true;//返回true让表单正常提交
        } else {
            return false;//返回false那么表单将不提交
        }
    }

</script><!-- script区域end -->
