<!-- Date Picker -->
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<!-- Daterange picker -->
<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker-bs3.css</@ofbizContentUrl>">
<!-- daterangepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/moment.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker.js</@ofbizContentUrl>"></script>
<!-- datetimepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<form class="form-horizontal" role="form" method="post" name="addProductGrouping" id="addProductGrouping" action="<@ofbizUrl>addProductGroupInfo</@ofbizUrl>">
    <#if prodGrp?has_content>
        <div class="box box-info">
            <div class="box-header with-border">
                <h3 class="box-title">商品组合基本信息</h3>
            </div>
            <div class="box-body">
                <div class="row js-promoCode">
                    <input type="hidden" id="linkId"/>
                    <input type="hidden" id="selectName"/>
                    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
                    <input type="hidden" id="productGrpId" name="productGrpId" value="${prodGrp.productGrpId}"/>
                    <input type="hidden" id="isPass" name="isPass" value="${parameters.productGrpId?if_exists}"/>
                    <div class="form-group col-sm-6"  data-mark="促销编码">
                        <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>促销编码</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="promoCode" name="promoCode" readOnly="readOnly" value="${prodGrp.promoCode?if_exists}">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,max" data-number="50" data-mark="组合名称">
                        <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>组合名称</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="promoName" name="promoName" value="${prodGrp.promoName?if_exists}">

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row js-ispass">
                    <div class="form-group col-sm-6" data-type="required,linkLt" id="fromDateGroup" data-compare-link="endGroup"
                         data-mark="开始时间" data-compare-mark="结束时间">
                        <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                            <input class="form-control" size="16" type="text"   readonly value="<#if prodGrp.fromDate?has_content >${prodGrp.fromDate?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>" readonly>
                            <input id="fromDate" class="dp-vd" type="hidden" name="fromDate" value="<#if prodGrp.fromDate?has_content >${prodGrp.fromDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>">
                            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                            <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                        </div>
                        <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>


                </div>
                <div class="row js-ispass">
                    <div id="endGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="fromDateGroup"
                         data-mark="结束时间" data-compare-mark="开始时间">
                        <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                            <input class="form-control" size="16" type="text" readonly value="<#if prodGrp.thruDate?has_content >${prodGrp.thruDate?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>" readonly>
                            <input id="thruDate" class="dp-vd" type="hidden" name="thruDate" value="<#if prodGrp.thruDate?has_content >${prodGrp.thruDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>">
                            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                            <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                        </div>
                        <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>
                </div>
            </div>
        </div>


        <div class="box box-info" id="product">
            <div class="box-header">
                <h3 class="box-title">组合商品</h3>
            </div>
            <div class="box-body table-responsive no-padding">
                <div class="dp-tables_btn p-l-15 p-b-15">
                    <button id="addProducts" type="button" class="btn btn-primary">
                        <i class="fa fa-plus">添加组合商品</i>
                    </button>
                </div>
                <div class="table-responsive no-padding">
                    <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                        <thead>
                        <tr>
                            <th>商品图片</th>
                            <th>商品编码</th>
                            <th>商品名称</th>
                            <th>规格</th>
                            <th>销售价格</th>
                            <th>数量</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="form-group col-sm-6" data-type="required,format" data-reg="/^[0-9]+([.]{1}[0-9]{1,2})?$/" data-mark="组合后商品金额">
                <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>组合后商品金额:</label>
                <div class="col-sm-8">
                    <input type="text" class="form-control dp-vd" id="promoPrice" name="promoPrice" value="${prodGrp.promoPrice?if_exists}">

                    <p class="dp-error-msg"></p>
                </div>
                <label for="title" class="col-sm-1 js-integral-upper-name control-label">元</label>
            </div>
        </div>
    <#else>
        <div class="box box-info">
            <div class="box-header with-border">
                <h3 class="box-title">商品组合基本信息</h3>
            </div>
            <div class="box-body">
                <div class="row js-promoCode">
                    <input type="hidden" id="linkId"/>
                    <input type="hidden" id="selectName"/>
                    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
                    <div class="form-group col-sm-6"  data-mark="促销编码">
                        <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>促销编码</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="promoCode" name="promoCode" readOnly="readOnly">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,max"  data-number="50" data-mark="组合名称">
                        <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>组合名称</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="promoName" name="promoName">

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,linkLt" id="fromDateGroup" data-compare-link="endGroup"
                         data-mark="开始时间" data-compare-mark="结束时间">
                        <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="fromDate">
                            <input class="form-control" size="16" type="text" readonly>
                            <input id="fromDate" class="dp-vd" type="hidden" name="fromDate">
                            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                            <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                        </div>
                        <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>


                </div>
                <div class="row">
                    <div id="endGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="fromDateGroup"
                         data-mark="结束时间" data-compare-mark="开始时间">
                        <label for="thruDate" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="thruDate">
                            <input class="form-control" size="16" type="text" readonly>
                            <input id="thruDate" class="dp-vd" type="hidden" name="thruDate">
                            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                            <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                        </div>
                        <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>
                </div>
            </div>
        </div>


        <div class="box box-info" id="product">
            <div class="box-header">
                <h3 class="box-title">组合商品</h3>
            </div>
            <div class="box-body table-responsive no-padding">
                <div class="dp-tables_btn p-l-15 p-b-15">
                    <button id="addProducts" type="button" class="btn btn-primary">
                        <i class="fa fa-plus">添加组合商品</i>
                    </button>
                </div>
                <div class="table-responsive no-padding">
                    <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                        <thead>
                        <tr>
                            <th>商品图片</th>
                            <th>商品编码</th>
                            <th>商品名称</th>
                            <th>规格</th>
                            <th>销售价格</th>
                            <th>数量</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="form-group col-sm-6" data-type="required,format" data-reg="/^[0-9]+([.]{1}[0-9]{1,2})?$/" data-mark="组合后商品金额">
                <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>组合后商品金额:</label>
                <div class="col-sm-8">
                    <input type="text" class="form-control dp-vd" id="promoPrice" name="promoPrice">

                    <p class="dp-error-msg"></p>
                </div>
                <label for="title" class="col-sm-1 js-integral-upper-name control-label">元</label>
            </div>
        </div>
    </#if>

    <div class="box-footer text-center">
    <#if security.hasEntityPermission("PRODUCT_GROUP", "_ADD", session)>
        <button id="btnProductGroupSave" class="btn btn-primary m-r-10">保存</button>
    </#if>
        <button type="button" class="btn btn-primary m-r-10" onclick="back()">返回</button>
    </div>


</form>
<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
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


<!-- 确认弹出框start -->
<div id="modal_confirm" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">修改</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">继续</button>
            </div>
        </div>
    </div>
</div><!--确认弹出框end -->

<!-- /.box-body -->
</div>
<!-- 内容end -->

<!-- script区域start -->
<script type="text/javascript">;
    var curProductIds="";
    $(function () {
        var isPass = '${parameters.isPass?if_exists}';

        if (isPass != 'Y') {
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
        }

        var curProductGrpId='${parameters.productGrpId?if_exists}';
        // 取得组合商品明细
        if(curProductGrpId) {
            getProductGroupingInfo('${parameters.productGrpId?if_exists}'); //组合商品信息初始化
        }


        // 添加商品
        $('#addProducts').click(function () {
            var sDate = $('#fromDate').val();
            var eDate = $('#thruDate').val();

            var timestamp2 = Date.parse(new Date(sDate));
            var timestamp3 = Date.parse(new Date(eDate));
            timestamp2 = timestamp2 / 1000;
            timestamp3 = timestamp3 / 1000;

            if (sDate && eDate) {

                if(sDate<=eDate) {
                    $.dataSelectModal({
                        url: "/catalog/control/ProductListMultiModalPage2${externalKeyParam}&startDate="+timestamp2+"&endDate="+timestamp3,
                        width: "800",
                        title: "选择商品",
                        selectId: "linkId",
                        selectName: "selectName",
                        multi: true,
                        selectCallBack: function (el) {
                            var productIds = el.data('id');
                            getProductGoodsInfoListByIds(productIds);
                        }
                    });
                }else{
                    $.tipLayer("开始时间要小于等于结束时间！");
                }
            }else{
                $.tipLayer("请输入开始时间、结束时间！");
            }

        })


        // 保存处理
        $("#btnProductGroupSave").click(function(){
            $('#addProductGrouping').dpValidate({
                clear: true
            });
        });

        $("#addProductGrouping").dpValidate({
            validate: true,
            clear: true,
            callback: function () {
                addProductGroup();
            }
        });

        //商品删除按钮事件
        $(document).on('click', '.js-button-assocgood', function () {
            var id=$(this).data("id");
            $(this).parent().parent().remove();
            curProductIds=updateProductIdsInfo(id);
        })
    });

    // 编辑组合商品
    function addProductGroup() {
        /*选择组合的商品*/
        var productGroupInfos = "";
        var productIds=""
        var flg=1
        $('#productTable>tbody').find("tr").each(function () {
            var tdArr = $(this);
            for (var i = 0; i < tdArr.length; i++) {
                var productId = tdArr.eq(i).find('td').eq(1).text();//产品编号
                var quantity =tdArr.eq(i).find('td').eq(5).find("input").val();//产品数量
                var curProductGroupInfo=productId+"|"+quantity;
                if (curProductGroupInfo) {
                    productGroupInfos += curProductGroupInfo + ",";
                }
                if(productIds==""){
                    productIds=productId;
                }else{
                    productIds=productIds+","+productId;
                }
                if(!quantity){
                    flg=0;
                }else{
                    var reg2=/^\+?[1-9]\d*$/;
                    if(!chkFun(quantity,reg2)){
                        flg=2;
                    }else {
                        var curPromoPrice = $("#promoPrice").val();
                        var chkFlg = chkPromoPrice(curPromoPrice);
                        if (chkFlg == "NG") {
                            flg = 0;
                        }
                    }
                }
            }
        });

        if(flg==1){
            if (productGroupInfos == "") {
                $('#modal_msg #modal_msg_body').html("请添加需要组合的商品！！");
                $('#modal_msg').modal();
                return;
            }else{
                var productGrpId=$('#productGrpId').val();
                var promoCode=$('#promoCode').val();
                var promoName=$('#promoName').val();
                var fromDate=$('#fromDate').val();
                var thruDate=$('#thruDate').val();
                var promoPrice=$('#promoPrice').val();
                var productStoreId=$('#productStoreId').val();
                var pgInfos=productGroupInfos.substr(0,productGroupInfos.length-1);

                //商品组合的新增
                jQuery.ajax({
                    url: '<@ofbizUrl>updateProductGroupingService</@ofbizUrl>',
                    type: 'POST',
                    data: {
                        'productGrpId':productGrpId,
                        'promoCode':promoCode,
                        'promoName':promoName,
                        'fromDate':fromDate,
                        'thruDate':thruDate,
                        'promoPrice':promoPrice,
                        'productStoreId':productStoreId,
                        'productGroupInfos':pgInfos,
                        'productIds':productIds
                    },
                    success: function(data){
                        if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                            $('#modal_msg #modal_msg_body').html(data._ERROR_MESSAGE_);
                            $('#modal_msg').modal();
                        }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                            $('#modal_msg #modal_msg_body').html(data._ERROR_MESSAGE_);
                            $('#modal_msg').modal();
                        } else {
                            if(data.chkFlg=="N"){
                                $('#modal_msg #modal_msg_body').html(data.errorMsg);
                                $('#modal_msg').modal();
                                return;
                            }else {
                                //设置提示弹出框内容
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                $('#modal_msg').modal();
                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                $('#modal_msg').on('hide.bs.modal', function () {
                                    window.location.href = '<@ofbizUrl>findProductGrouping</@ofbizUrl>';
                                })
                            }
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
                });
            }
        }else
            if(flg==0){
                $('#modal_msg #modal_msg_body').html("请检查需要组合的商品的数量是否填写,优惠价格不能大于商品总价！！");
            }else{
                $('#modal_msg #modal_msg_body').html("数量必须为整数！！");
            }

            $('#modal_msg').modal();
            return;
        }



    /**
     * 返回处理
     */
    function back() {
        window.location.href = '<@ofbizUrl>findProductGrouping</@ofbizUrl>';
    }

    /**
     * 根据编码取得组合商品信息
     * @param productGrpId
     */
    function getProductGroupingInfo(productGrpId) {
        //商品组合信息的取得
        jQuery.ajax({
            url: '<@ofbizUrl>productGroupingDetail</@ofbizUrl>',
            type: 'POST',
            data: {
                'productGrpId':productGrpId
            },
            success: function(data){

                if (data.productGroupList) {
                    var product_List = data.productGroupList;
                    $('#productTable>tbody').empty();
                    var tr1 = "";
                    for (var i = 0; i < product_List.length; i++) {
                        var curId=product_List[i].productId;
                        var tr = '<tr id="' + product_List[i].productId + '">'
                                + '<td><img height="100" src="'+product_List[i].imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + product_List[i].productId + '</td>'
                                + '<td>' + product_List[i].productName + '</td>'
                                + '<td>' + product_List[i].featureInfo+'</td>'
                                + '<td>' + product_List[i].defaultprice+'</td>'
                                + '<td><input type="text" value="'+product_List[i].quantity+'"/> </td>'
                                + '<td class="fc_td"><button type="button" data-id="'+product_List[i].productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);
                        if(i==0){
                            curProductIds=curId;
                        }else{
                            curProductIds+=','+curId;
                        }
                    }

                }
            },
            error: function (data) {
                //隐藏新增弹出窗口
                $('#modal_audit_productGrouping').modal('toggle');
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("组合商品取得失败");
                $('#modal_msg').modal();
            }
        });
    }


    /**
     * 根据商品编码取得商品信息列表
     * @param ids
     */
    function getProductGoodsInfoListByIds(ids){
        $.ajax({
            url: "/catalog/control/getProductGoodsListByIds?externalLoginKey=${externalLoginKey}",
            type: "POST",
            data: {ids: ids},
            dataType: "json",
            success: function (data) {
                var productGoodInfoList = data.productGoodInfoList;
                for (var i=0;i<productGoodInfoList.length;i++) {
                    var productGoodInfo = productGoodInfoList[i];
                    var productInfo = productGoodInfo.productInfo;
                    var salesPrice = productGoodInfo.salesPrice;
                    var imgUrl=productGoodInfo.imgUrl;
                    var productGoodFeature = productGoodInfo.productGoodFeature;
                    var productId = productInfo.productId;
                    var productName = productInfo.productName;

                    if(chkProductIdIsSelected(productInfo.productId)){
                        var tr = '<tr id="' + productId + '">'
                                + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + productId + '</td>'
                                + '<td>' + productName + '</td>'
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' +salesPrice+ '</td>'
                                + '<td><input type="text"/> </td>'
                                + '<td class="fc_td"><button type="button" data-id="'+productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);

                        if(curProductIds==""){
                            curProductIds=productId;
                        }else{
                            curProductIds+=','+productId;
                        }
                    }

                };
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
    function chkProductIdIsSelected(productId){
        var chkFlg=1;
        var ids=curProductIds.split(",");
        for(var i=0;i<ids.length;i++){
            var curProductId=ids[i];
            if(curProductId==productId){
                chkFlg=0;
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
    function updateProductIdsInfo(productId){
        var ids=curProductIds.split(",");
        var newIds="";
        for(var i=0;i<ids.length;i++){
            var curProductId=ids[i];
            if(curProductId!=productId){
                if(newIds==""){
                    newIds= curProductId
                }else{
                    newIds=newIds+','+curProductId  ;
                }
            }
        }
        return newIds
    }


/**
 * 优惠金额的验证
 * @param promoPrice
 */
function chkPromoPrice(promoPrice){
    var chkFlg="OK";

    var salePriceTotal="0";
    $('#productTable>tbody').find("tr").each(function () {
        var tdArr = $(this);
        for (var i = 0; i < tdArr.length; i++) {
            var price = tdArr.eq(i).find('td').eq(4).text();//产品价格
            var quantity =tdArr.eq(i).find('td').eq(5).find("input").val();//产品数量
            salePriceTotal=parseInt(salePriceTotal)+parseInt(quantity)*parseFloat(price);

        }
    });
    if(parseFloat(promoPrice)>parseFloat(salePriceTotal)){
        chkFlg="NG";
    }
    return chkFlg;

}


function chkFun(curValue,reg){
    var chkFlg=true;
//        var reg=/^[0-9]+([.]{1}[0-9]{1,2})?$/;
    reg = eval(reg);
    if (!reg.test(curValue)) {
        chkFlg=false;
    }
    return chkFlg;
}

</script>
