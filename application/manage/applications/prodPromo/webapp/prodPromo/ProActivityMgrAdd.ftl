<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>"
      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/jquery/plugins/upload/ajaxupload.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<style>
    .table-bordered {
        border: 1px solid #d2d6de;
    }

    .table-bordered > thead > tr > th,
    .table-bordered > tbody > tr > th,
    .table-bordered > tfoot > tr > th,
    .table-bordered > thead > tr > td,
    .table-bordered > tbody > tr > td,
    .table-bordered > tfoot > tr > td {
        border: 1px solid #d2d6de;
    }

    a.empty {
        position: relative;
        display: block;
        background: #ededed;
        text-align: center;
        height: 100px;
        margin-right: 4px;
        overflow: hidden;
        zoom: 1;
        text-align: center;
        opacity: 0.7;
        border: 1px dashed #b4b4b4;
        height: 98px;
    }

    a.empty:hover {
        text-decoration: none;
        opacity: 1;
    }

    .btn_uploadFile {
        position: absolute;
        display: block;
        left: 0;
        top: 0;
        z-index: 10;
        cursor: pointer;
        opacity: 0;
        filter: alpha(opacity=0);
        overflow: hidden;
        zoom: 1;
    }

    .reselect {
        display: none;
        width: 100%;
        height: 100%;
        text-align: center;
        border: 1px dashed #b4b4b4;
        background: #ededed;
        z-index: 999;
        position: absolute;
        top: 0px;
        opacity: 0.5;
        cursor: pointer;
    }

    .reselect-text {
        position: absolute;
        bottom: 0px;
        left: 60px;
        color: #7E7E7E;
        font-size: 20px;
        font-weight: 700;
    }

    .x {
        margin-left: 5px;
        cursor: pointer;
    }
</style>


<!-- 内容start -->
<div class="box box-info">
    <form id="ProActivityMgrAddForm" class="form-horizontal">
        <!--店铺编码-->
        <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
        <input type="hidden" id="linkId"/>
        <input type="hidden" id="selectName"/>
        <div class="box-body">
            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="专题名称">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>专题名称:</label>
                    <div class="col-sm-10">
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="activityManagerName"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>
                    <div class="col-sm-10">
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="sequenceId"/>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="专题图片">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>专题图片:</label>
                    <div class="col-sm-4">
                        <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                        <input class="btn btn-primary" type="button" id="uploadedFile" name="uploadedFile"
                               value="选择图片"/>
                        <input type="hidden" id="contentId" class="dp-vd"/>
                        <p class="dp-error-msg"></p>
                    </div>
                    <div class="col-sm-6">
                        <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 750*383px</div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-12" data-type="required,linkLt" id="fromDateGroup"
                     data-compare-link="endGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="publishDate" class="col-sm-2 control-label"><i class="required-mark">*</i>开始时间</label>

                    <div class="input-group date form_datetime col-sm-4 p-l-15 p-r-15" data-link-field="fromDate">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="fromDate" class="dp-vd" type="hidden" name="fromDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
            </div>
            <div class="row">
                <div id="endGroup" class="form-group col-sm-12" data-type="required,linkGt"
                     data-compare-link="fromDateGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="thruDate" class="col-sm-2 control-label"><i class="required-mark">*</i>结束时间</label>
                    <div class="input-group date form_datetime col-sm-4 p-l-15 p-r-15" data-link-field="thruDate">
                        <input class="form-control" size="16" type="text" readonly>
                        <input id="thruDate" class="dp-vd" type="hidden" name="thruDate">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

            </div>
            <div class="row">
                <div class="form-group col-sm-12">
                    <label  class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用:</label>
                    <div class="col-sm-4 p-t-5">
                        <label><input type="radio" name="isUsed" value="Y" checked>是</label>&nbsp;&nbsp;&nbsp;&nbsp;
                        <label><input type="radio" name="isUsed" value="N">否</label>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="专题栏目">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>专题栏目:</label>
                    <div class="col-sm-10">
                        <button style="margin-left: 5px" class="btn btn-primary" type="button" id="addSubjectColumn"
                                name="addSubjectColumn" value="新增"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span>新增
                        </button>
                    <#--<input type="hidden" id="subjectColumnInfo" class="dp-vd" />-->
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="关联商品">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>关联商品:</label>
                    <div class="col-sm-10">
                        <div class="box-header with-border">
                            <ul class="nav nav-tabs js-ul">
                            <#--<li class="js-recommend-product js-col-1 active"><a href="javascript:void(0)" onclick="changeTab('0')">推荐商品</a></li>-->
                                <#--<li class="js-recommend-brand js-col-2"><a href="javascript:void(0)" onclick="changeTab('1')">热门品牌</a></li>-->
                                <#--<li class="js-recommend-store js-col-3"><a href="javascript:void(0)" onclick="changeTab('2')">热门店铺</a></li>-->
                                    <#--<li class="js-recommend-product js-col-title active"><a href="javascript:void(0)" >推荐商品</a></li>-->
                                    <#--<li class="js-recommend-brand js-col-title"><a href="javascript:void(0)" >热门品牌</a></li>-->
                                    <#--<li class="js-recommend-store js-col-title"><a href="javascript:void(0)" >热门店铺</a></li>-->
                            </ul>
                        </div>

                        <div class="con">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>


            </div><!-- box-body end -->

            <!-- 按钮组 -->
            <div class="box-footer text-center">
                <!-- <button id="preview" type="button" class="btn btn-primary m-r-10">预览</button> -->
                <button id="save" type="button" class="btn btn-primary m-r-10">保存</button>
            </div>
    </form>
</div><!-- 内容end -->

<!-- 新增栏目弹出框start -->
<div id="modal_add" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_add_title">新增栏目</h4>
            </div>
            <div class="modal-body">
                <form id="addForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl></@ofbizUrl>">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="栏目">
                            <label class="control-label col-sm-2"><i class="required-mark">*</i>栏目名称:</label>
                            <div class="col-sm-8">
                                <input type="text" class="form-control dp-vd" id="columnName" name="columnName">
                            <#--<textarea class="form-control dp-vd js-question" rows="6" value=""></textarea>-->
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default"
                        data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 新增栏目弹出框end -->
<!-- script区域start -->
<script>
    $(function () {
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


//	CKEDITOR.replace("activityManagerText");
        var max_seq = 0;


///////////////////////////////////////////////
        var liSize = $(".js-col-title").length;
        if (liSize) {
//        $(this).addClass("active").siblings().removeClass("active");
            $(".js-col-title li:first").addClass("active").siblings().removeClass("active");

            $(".con .js-col" + 0).show().siblings().hide();
        }
        //添加按钮点击事件
        $('#addSubjectColumn').click(function () {
            //新增弹出框内容
            $("#model_add #columnName").val('');
            $('#modal_add').modal();
        });


        $('#modal_add #save').click(function () {
            $("#addForm").dpValidate({
                clear: true
            });
            $('#addForm').submit();
        });

        $("#addForm").dpValidate({
            validate: true,
            callback: function () {
                var columnName = $('#addForm #columnName').val();
                console.log(columnName);
                var liSize = $(".js-col-title").length;
                getSubjectColumnInfo(liSize, columnName);
                $('#modal_add').modal('hide');

            }
        });

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
            $.chooseImage.choose(obj, function (data) {
                $('#contentId').val(data.uploadedFile0);
                $('#img').attr({"src": "/content/control/getImage?contentId=" + data.uploadedFile0});
            })
        });

        // 图片选择控件显示
        $('#uploadedFile').click(function () {
            $.chooseImage.show();
        });
        $(document).on('click', '.js-col-title', function () {
            var curIndex = $(this).index();
            $(this).addClass("active").siblings().removeClass("active");
            $(".con .js-col" + curIndex).show().siblings().hide();
        })

        // 添加商品
        $(document).on('click', '.js-addProducts', function () {
            var curId = $(this).data("id");
            $("#selectName").val('');
            $("#linkId").val('');
            $.dataSelectModal({
                url: "/catalog/control/ProductListMultiModalPage${externalKeyParam}",
                width: "800",
                title: "选择商品",
                selectId: "linkId",
                selectName: "selectName",
                multi: true,
                selectCallBack: function (el) {
                    var productIds = el.data('id');
                    getProductGoodsInfoListByIds(productIds, curId);
                }
            });
        })


        //商品删除按钮事件
        $(document).on('click', '.js-button-assocgood', function () {
            var id = $(this).data("id");
            var colIndex = $(this).data("col");
            $(this).parent().parent().remove();
            var curProductIds = updateProductIdsInfo(id, colIndex);
            $('.js-col' + colIndex).find('#prodIds' + colIndex).val(curProductIds);

        })
        ///////////////////////////////////////////////////

        //表单校验方法
        $('#ProActivityMgrAddForm').dpValidate({
            validate: true,
            callback: function () {
                var activityManagerName = $('#activityManagerName').val();
                var sequenceId = $('#sequenceId').val();
                var contentId = $('#contentId').val();
                // 店铺信息
                var productStoreId = $("#productStoreId").length == 0 ? '' : $('#productStoreId').val();
                var colInfos = '';
                if (chkColInfo() != "0") {
                    $.tipLayer("请输入完整的栏目信息！");
                    return false;
                } else {

                    $(".js-col-title").each(function () {
                        var curIndex = $(this).index();
                        ;
                        var curColName = $(this).find("a .js-col-titleColname").text();
                        var curProdIds = $('#prodIds' + curIndex).val();
                        curColInfo = curColName + '^' + curProdIds;
                        if (colInfos == "") {
                            colInfos = curColInfo;
                        } else {
                            colInfos = colInfos + "|" + curColInfo;
                        }
                    });
                }

                console.log(colInfos);
                //异步活动管理新增方法
                $.ajax({
                    url: "/prodPromo/control/activityMgrAddForJson${externalKeyParam}",
                    type: "POST",
                    data: {
                        activityManagerName: activityManagerName,
                        contentId: contentId,
                        sequenceId: sequenceId,
                        productStoreId: productStoreId,
                        colInfos: colInfos,
                        fromDate: $('#fromDate').val(),
                        thruDate: $('#thruDate').val(),
                        isUsed: $("input[name='isUsed']:checked").val(),

                    },
                    dataType: "json",
                    success: function (data) {
                        $.tipLayer("操作成功！");
                        window.location = '<@ofbizUrl>activityMgrPage</@ofbizUrl>';
                    },
                    error: function (data) {
                        $.tipLayer("操作失败！");
                    }
                });
            }
        });

        //保存按钮点击事件
        $('#save').click(function () {
            $("#ProActivityMgrAddForm").dpValidate({
                clear: true
            });
            $("#ProActivityMgrAddForm").submit();
        });

        $(document).on('click', '.x', function () {
            var curIndex = $(this).parent().parent().data("id");
            $(".js-col" + curIndex).remove();
            $(this).parent().parent().remove();
        });


    });


    /////////////////////////////////////////////////////
    //function changeTab(tabIndex){
    //    if(tabIndex=='0') {
    //        curFirstPageType='0';
    //        $('.js-product').show();
    //        $('.js-brand').hide();
    //        $('.js-store').hide();
    //        $(".js-recommend-product").addClass("active").siblings().removeClass("active");
    //    } else if(tabIndex=='1'){
    //        curFirstPageType='1';
    //        $('.js-brand').show();
    //        $('.js-product').hide();
    //        $('.js-store').hide();
    //        $(".js-recommend-brand").addClass("active").siblings().removeClass("active");
    //    }else if(tabIndex='2'){
    //        curFirstPageType='2';
    //        $('.js-store').show();
    //        $('.js-product').hide();
    //        $('.js-brand').hide();
    //        $(".js-recommend-store").addClass("active").siblings().removeClass("active");
    //    }
    //}

    // 取得栏目信息
    function getSubjectColumnInfo(i, colName) {

        var strli = '<li class="js-recommend-product js-col-title" data-id=' + i + '><a href="javascript:void(0)" ><span class="js-col-titleColname">' + colName + '</span><span aria-hidden="true" class="x">×</span></a></li>';
        var strCol = '<div class="box-body js-product js-col' + i + '">' +
                '    <div class="row">' +
                '        <div class="form-group col-sm-12" >' +
                '            <div class="col-sm-12">' +
                //				'                <button id="btn_prod_add'+i+'" data-id='+i+'class="btn btn-primary js-addProducts" >'+
                //				'                    添加('+i+')'+
                //				'                </button>'+
                '                <input type="button" id="btn_prod_add' + 1 + '" data-id="' + i + '" class="btn btn-primary js-addProducts" value="添加"/>' +
                '                <input type="hidden" id="prodIds' + i + '"/>' +
                '                <input type="hidden" id="prodNames' + i + '"/>' +
                '                <table id="prodtbl' + i + '" class="table table-bordered table-hover m-t-10">' +
                '                    <thead>' +
                '                    <tr>' +
                '                        <th>商品图片</th>' +
                '                        <th>商品名称</th>' +
                '                        <th>规格</th>' +
                '                        <th>商品编号</th>' +
                '                        <th>操作</th>' +
                '                    </tr>' +
                '                    </thead>' +
                '                    <tbody>' +
                '                    </tbody>' +
                '                </table>' +
                '            </div>' +
                '        </div>' +
                '    </div>' +
                '</div>';
        $('.con').append(strCol);
        $('.js-ul').append(strli);
        var liSize = $(".js-col-title").length;
        $(".js-col-title").each(function () {
            var curIndex = $(this).index();
            if (curIndex == '0') {
                $(this).addClass("active");
                $(".con .js-col" + curIndex).show();
            } else {
                $(this).removeClass("active");
                $(".con .js-col" + curIndex).hide();
            }
            console.log(curIndex);
        })
    }

    /**
     * 根据商品编码取得商品信息列表
     * @param ids
     */
    function getProductGoodsInfoListByIds(ids, curIndex) {
//	alert(curIndex);
        $.ajax({
            url: "/catalog/control/getProductGoodsListByIds?externalLoginKey=${externalLoginKey}",
            type: "POST",
            data: {ids: ids},
            dataType: "json",
            success: function (data) {
                var productGoodInfoList = data.productGoodInfoList;
                for (var i = 0; i < productGoodInfoList.length; i++) {
                    var productGoodInfo = productGoodInfoList[i];
                    var productInfo = productGoodInfo.productInfo;
                    var salesPrice = productGoodInfo.salesPrice;
                    var imgUrl = productGoodInfo.imgUrl;
                    var productGoodFeature = productGoodInfo.productGoodFeature;
                    var productId = productInfo.productId;
                    var productName = productInfo.productName;

                    if (chkProductIdIsSelected(productInfo.productId, curIndex)) {
                        var tr = '<tr id="' + productId + '">'
                                + '<td><img height="100" src="' + imgUrl + '" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + productName + '</td>'
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
                                + '<td class="fc_td"><button type="button" data-id="' + productId + '" data-col="' + curIndex + '" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#prodtbl' + curIndex + '>tbody').append(tr);
                        var curProductIds = $('.js-col' + curIndex).find('#prodIds' + curIndex).val();
                        if (curProductIds == "") {
                            curProductIds = productId;
                        } else {
                            curProductIds += ',' + productId;
                        }
                        $('.js-col' + curIndex).find('#prodIds' + curIndex).val(curProductIds);
                    }

                }
                ;
            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });
    }


    /**
     * 验证商品编码是否使用
     * @param productId
     * @returns {number}
     */
    function chkProductIdIsSelected(productId, curIndex) {
        var chkFlg = 1;
        var curProductIds = $('.js-col' + curIndex).find('#prodIds' + curIndex).val();
        var ids = curProductIds.split(",");
        for (var i = 0; i < ids.length; i++) {
            var curProductId = ids[i];
            if (curProductId == productId) {
                chkFlg = 0;
                return chkFlg;
            }
        }
        return chkFlg;
    }

    /**
     * 更新商品编码集合
     * @param productId
     * @returns {string}
     */
    function updateProductIdsInfo(productId, curIndex) {
        var curProductIds = $('.js-col' + curIndex).find('#prodIds' + curIndex).val();
        var ids = curProductIds.split(",");
        var newIds = "";
        for (var i = 0; i < ids.length; i++) {
            var curProductId = ids[i];
            if (curProductId != productId) {
                if (newIds == "") {
                    newIds = curProductId
                } else {
                    newIds = newIds + ',' + curProductId;
                }
            }
        }
        return newIds
    }


    function chkColInfo() {
        var chkFlg = "0";
        var listSize = $(".js-col-title").length == 0 ? '0' : $(".js-col-title").length;
        if (listSize == 0) {
            chkFlg = "1";
        } else {
            $(".js-col-title").each(function () {
                var curIndex = $(this).index();
                var curProdIds = $('#prodIds' + curIndex).val();
                if (!curProdIds) {
                    chkFlg = "2";
                }
            })
        }
        return chkFlg;

    }

    /////////////////////////////////////////////////////
</script><!-- script区域end -->
