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
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<form class="form-horizontal" role="form" method="post" name="addProductSecKill" id="addProductSecKill" action="<@ofbizUrl>addProductGroupInfo</@ofbizUrl>">
    <#if prodSk?has_content>
        <div class="box box-info">
            <div class="box-header with-border">
                <h3 class="box-title">秒杀商品基本信息</h3>
            </div>
            <div class="box-body">
                <div class="row js-promoCode">
                    <input type="hidden" id="linkId"/>
                    <input type="hidden" id="selectName"/>
                    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
                    <input type="hidden" id="productSkId" name="productSkId" value="${prodSk.activityId}"/>
                    <input type="hidden" id="isPass" name="isPass" value="${parameters.isPass?if_exists}"/>
                    <div class="form-group col-sm-6"  data-mark="活动编码">
                        <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>活动编码</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="activityCode" name="activityCode" readOnly="readOnly" value="${prodSk.activityCode?if_exists}">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,max"  data-number="50" data-mark="促销名称">
                        <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>促销名称</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="activityName" name="activityName" value="${prodSk.activityName?if_exists}">

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,linkLt" id="startTimeGroup" data-compare-link="endTimeGroup" data-mark="开始时间" data-compare-mark="销售结束时间">
                        <label for="startTime" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="startTime">
                            <input class="form-control" size="16" type="text" readonly value="<#if prodSk.activityStartDate?has_content >${prodSk.activityStartDate?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>">
                            <input id="activityStartDate" class="dp-vd" type="hidden" name="activityStartDate" value="<#if prodSk.activityStartDate?has_content >${prodSk.activityStartDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>">

                            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                            <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                        </div>
                        <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>


                </div>
                <div class="row">
                    <div id="endTimeGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="endGroup" data-mark="结束时间" data-compare-mark="下架时间">
                        <label for="endTime" class="col-sm-3 control-label"><i class="required-mark">*</i>结束时间</label>

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="endTime">
                            <input class="form-control" size="16" type="text" readonly value="<#if prodSk.activityEndDate?has_content >${prodSk.activityEndDate?string('yyyy-MM-dd HH:mm')?if_exists}<#else></#if>">
                            <input id="activityEndDate" class="dp-vd" type="hidden" name="activityEndDate" value="<#if prodSk.activityEndDate?has_content >${prodSk.activityEndDate?string('yyyy-MM-dd HH:mm:ss')?if_exists}<#else></#if>">


                            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                            <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                        </div>
                        <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,format" data-reg="/(^[1-9]+\d*$)|(^0$)/"  data-mark="单个ID限购数量">
                        <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>单个ID限购数量</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="limitQuantity" name="limitQuantity" value="${prodSk.limitQuantity?if_exists}">

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
                <div class="dp-tables_btn p-l-15 p-b-15">
                    <button id="addProducts" type="button" class="btn btn-primary">
                        <i class="fa fa-plus">选择参加促销的商品</i>
                    </button>
                </div>
                <div class="table-responsive no-padding">
                    <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                        <thead>
                        <tr>
                            <th>商品图片</th>
                            <th>规格</th>
                            <th>商品编码</th>
                            <th>商品名称</th>
                            <th>数量</th>
                            <th>秒杀金额</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="row p-l-10 p-r-10">
            <#--<div class="form-group">-->
                <#--<label for="seo" class="col-sm-2 control-label">活动描述</label>-->

                <#--<div class="col-sm-8">-->
                    <#--<@htmlTemplate.renderTextareaField name="activityDesc" className="dojo-ResizableTextArea" alert="false"-->
                    <#--value="" cols="60" rows="15" id="textData" readonly="" visualEditorEnable="true" language="zh_CN" buttons="maxi" />-->
                    <#--<p class="dp-error-msg"></p>-->
                <#--</div>-->
            <#--</div>-->
            <div class="form-group">
                <label for="seo" class="col-sm-2 control-label">活动描述</label>
                <div class="col-sm-8">
                    <div class="box-body pad">
                        <textarea id="Centent" name="Centent" value="">
                        </textarea>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
        </div>
    <#else>
        <div class="box box-info">
            <div class="box-header with-border">
                <h3 class="box-title">秒杀商品基本信息</h3>
            </div>
            <div class="box-body">
                <div class="row js-promoCode">
                    <input type="hidden" id="linkId"/>
                    <input type="hidden" id="selectName"/>
                    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
                    <div class="form-group col-sm-6"  data-mark="活动编码">
                        <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>活动编码</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="activityCode" name="activityCode" readOnly="readOnly" >
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,max"  data-number="50" data-mark="促销名称">
                        <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>促销名称</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="activityName" name="activityName" >

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,linkLt" id="fromDateGroup" data-compare-link="endGroup"
                         data-mark="开始时间" data-compare-mark="结束时间">
                        <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>开始时间</label>

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="startTime">
                            <input class="form-control" size="16" type="text" readonly>
                            <input id="activityStartDate" class="dp-vd" type="hidden" name="activityStartDate">

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

                        <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="endTime">
                            <input class="form-control" size="16" type="text" readonly>
                            <input id="activityEndDate" class="dp-vd" type="hidden" name="activityEndDate">

                            <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                            <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                        </div>
                        <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                    </div>
                </div>


                <div class="row">
                    <div class="form-group col-sm-6" data-type="required,format" data-reg="/(^[1-9]+\d*$)|(^0$)/" data-mark="单个ID限购数量">
                        <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>单个ID限购数量</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" id="limitQuantity" name="limitQuantity"  >

                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>


        <div class="box box-info" id="product">
            <div class="box-header">
                <h3 class="box-title">秒杀商品</h3>
            </div>
            <div class="box-body table-responsive no-padding">
                <div class="dp-tables_btn p-l-15 p-b-15">
                    <button id="addProducts" type="button" class="btn btn-primary">
                        <i class="fa fa-plus">选择参加促销的商品</i>
                    </button>
                </div>
                <div class="table-responsive no-padding">
                    <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                        <thead>
                        <tr>
                            <th>商品图片</th>
                            <th>规格</th>
                            <th>商品编码</th>
                            <th>商品名称</th>
                            <th>数量</th>
                            <th>秒杀金额</th>
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
            <#--<div class="form-group">-->
                <#--<label for="seo" class="col-sm-2 control-label">活动描述</label>-->

                <#--<div class="col-sm-8">-->
                    <#--<@htmlTemplate.renderTextareaField name="activityDesc" className="dojo-ResizableTextArea" alert="false"-->
                    <#--value="" cols="60" rows="15" id="textData" readonly="" visualEditorEnable="true" language="zh_CN" buttons="maxi" />-->
                    <#--<p class="dp-error-msg"></p>-->
                <#--</div>-->
            <#--</div>-->

            <div class="form-group">
                <label for="seo" class="col-sm-2 control-label">活动描述</label>
                <div class="col-sm-8">
                    <div class="box-body pad">
                        <textarea id="Centent" name="Centent" value="">
                        </textarea>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

        </div>



    </#if>

    <div class="box-footer text-center">
    <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_ADD", session)>
        <button id="btnProductSecKillSave" class="btn btn-primary m-r-10">保存</button>
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
        CKEDITOR.replace("Centent", {
            filebrowserImageBrowseUrl: '/content/control/file${externalKeyParam}&file?directory=/datasource'
        });
        var isPass='${parameters.isPass?if_exists}';
        if(isPass !='Y') {
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
        // 取得促销商品明细
        var curActivityId='${parameters.activityId?if_exists}';
        // 取得组合商品明细
        if(curActivityId) {
            getProductSkInfo('${parameters.activityId?if_exists}'); //秒杀商品信息初始化
        }

        // 添加商品
        $('#addProducts').click(function () {
            var sDate = $('#activityStartDate').val();
            var eDate = $('#activityEndDate').val();

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
        $("#btnProductSecKillSave").click(function(){
            $('#addProductSecKill').dpValidate({
                clear: true
            });
        });

        $("#addProductSecKill").dpValidate({
            validate: true,
            clear: true,
            callback: function () {
                addProductSecKillInfo();
            }
        });

        //商品删除按钮事件
        $(document).on('click', '.js-button-assocgood', function () {
            var id=$(this).data("id");
            $(this).parent().parent().remove();
            curProductIds=updateProductIdsInfo(id);
        })
    });

    // 编辑促销商品
    function addProductSecKillInfo() {
        /*选择促销的商品*/
        var productSecKillInfos = "";
        var productIds="";
        var flg=1
        $('#productTable>tbody').find("tr").each(function () {
            var tdArr = $(this);
            for (var i = 0; i < tdArr.length; i++) {
                var productId = tdArr.eq(i).find('td').eq(2).text();//产品编号
                var quantity =tdArr.eq(i).find('td').eq(4).find("input").val();//产品数量
                var skPrice =tdArr.eq(i).find('td').eq(5).find("input").val();//秒杀金额
                var curProductSecKillInfo=productId+"|"+quantity+"|"+skPrice;
                if (curProductSecKillInfo) {
                    productSecKillInfos += curProductSecKillInfo + ",";
                }

                if(productIds==""){
                    productIds=productId;
                }else{
                    productIds=productIds+","+productId;
                }
                if(!quantity||(!skPrice)){
                    flg=0;
                }else{
                    var reg=/^[0-9]+([.]{1}[0-9]{1,2})?$/;
                    var reg2=/^\+?[1-9]\d*$/;
                    if((!chkFun(skPrice,reg))||(!chkFun(quantity,reg2))){
                        flg=0;
                    }
                }
            }
        });

        if(flg){
            if (productSecKillInfos == "") {
                $('#modal_msg #modal_msg_body').html("请添加需要促销的商品！！");
                $('#modal_msg').modal();
                return;
            }else{

                var productSkId=$('#productSkId').val();
                var activityCode=$('#activityCode').val();
                var activityName=$('#activityName').val();
                var activityStartDate=$('#activityStartDate').val();
                var activityEndDate=$('#activityEndDate').val();
                var productStoreId=$('#productStoreId').val();
                var limitQuantity=$('#limitQuantity').val();
                var pgInfos=productSecKillInfos.substr(0,productSecKillInfos.length-1);
                var activityDesc=CKEDITOR.instances.Centent.getData();
                var cutProductSkId='${parameters.activityId?if_exists}';
               //秒杀商品的新增
                $.ajax({
                    type: 'post',
                    url: '<@ofbizUrl>checkActiveCodeExist</@ofbizUrl>',
                    data: {activityCode: $('#activityCode').val()},
                    async: false,
                    success: function (data) {
                        console.log(data)

                        if (data && data.activity) {
                            if(!cutProductSkId) {
                                $("#userName").addClass('error');
                                //设置提示弹出框内容
                                $('#modal_msg #modal_msg_body').html("秒杀编码已存在！");
                                $('#modal_msg').modal();
                            }else{
                                $.ajax({
                                    url: "updateSecKillService",
                                    type: "POST",
                                    async: false,
                                    data: {
                                        productSkId:productSkId,
                                        activityCode: activityCode,
                                        activityName: activityName,
                                        activityStartDate: activityStartDate,
                                        activityEndDate: activityEndDate,
                                        limitQuantity: limitQuantity,
                                        activityDesc: activityDesc,
                                        productStoreId:productStoreId,
                                        productSkInfos:pgInfos,
                                        productIds:productIds
                                    },
                                    dataType: "json",
                                    success: function (data) {
                                        if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                                            $.tipLayer(data._ERROR_MESSAGE_);
                                        }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                                            $.tipLayer(data._ERROR_MESSAGE_LIST_);
                                        } else {
                                            if(data.chkFlg=="N") {
                                                $('#modal_msg #modal_msg_body').html(data.errorMsg);
                                                $('#modal_msg').modal();
                                                return;
                                            }else {
                                                //设置提示弹出框内容
                                                $('#modal_msg #modal_msg_body').html("操作成功！");
                                                $('#modal_msg').modal();
                                                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                                $('#modal_msg').on('hide.bs.modal', function () {
                                                    window.location.href = '<@ofbizUrl>findSecKill</@ofbizUrl>';
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
                                })
                            }
                        } else {
                            $.ajax({
                                url: "updateSecKillService",
                                type: "POST",
                                async: false,
                                data: {
                                    productSkId:productSkId,
                                    activityCode: activityCode,
                                    activityName: activityName,
                                    activityStartDate: activityStartDate,
                                    activityEndDate: activityEndDate,
                                    limitQuantity: limitQuantity,
                                    activityDesc: activityDesc,
                                    productStoreId:productStoreId,
                                    productSkInfos:pgInfos,
                                    productIds:productIds
                                },
                                dataType: "json",
                                success: function (data) {
                                    if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                                        $.tipLayer(data._ERROR_MESSAGE_);
                                    }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                                        $.tipLayer(data._ERROR_MESSAGE_LIST_);
                                    } else {
                                        if(data.chkFlg=="N") {
                                            $('#modal_msg #modal_msg_body').html(data.errorMsg);
                                            $('#modal_msg').modal();
                                            return;
                                        }else {
                                            $('#modal_msg #modal_msg_body').html("操作成功！");
                                            $('#modal_msg').modal();
                                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                                            $('#modal_msg').on('hide.bs.modal', function () {
                                                window.location.href = '<@ofbizUrl>findSecKill</@ofbizUrl>';
                                            })
                                        }
                                    }
                                    //设置提示弹出框内容

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
                });

            }
        }else{
//            $('#modal_msg #modal_msg_body').html("请添加需要秒杀的商品的数量、价格！！");
            $('#modal_msg #modal_msg_body').html("请添加正确秒杀的商品的数量（正整数）、价格（正整数，或有两位小数）！！");
            $('#modal_msg').modal();
            return;
        }

    }

    /**
     * 返回处理
     */
    function back() {
        window.location.href = '<@ofbizUrl>findSecKill</@ofbizUrl>';
    }

    /**
     * 根据编码取得秒杀商品信息
     * @param productSkId
     */
    function getProductSkInfo(productSkId) {
        //秒杀商品信息的取得
        jQuery.ajax({
            url: '<@ofbizUrl>productSkDetail</@ofbizUrl>',
            type: 'POST',
            data: {
                'productSkId':productSkId
            },
            success: function(data){

                if (data.productSkProductInfoList) {
                    var product_List = data.productSkProductInfoList;
                    $('#productTable>tbody').empty();
                    var tr1 = "";
                    for (var i = 0; i < product_List.length; i++) {
                        var curId=product_List[i].productInfo.productId;
                        var tr = '<tr id="' + product_List[i].productInfo.productId + '">'
                                + '<td><img height="100" src="'+product_List[i].imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + product_List[i].featureInfo+'</td>'
                                + '<td>' + product_List[i].productInfo.productId + '</td>'
                                + '<td>' + product_List[i].productInfo.productName + '</td>'

                                + '<td><input type="text" value="'+product_List[i].activityQuantity+'"/> </td>'
                                + '<td><input type="text" value="'+product_List[i].activityPrice+'"/> </td>'
                                + '<td class="fc_td"><button type="button" data-id="'+product_List[i].productInfo.productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);
                        if(i==0){
                            curProductIds=curId;
                        }else{
                            curProductIds+=','+curId;
                        }
                    }
                }
                if(data.productActivity){
                    $('#activityDesc').html(data.productActivity.activityDesc);
                    CKEDITOR.instances.Centent.setData(data.productActivity.activityDesc);
//                    CKEDITOR.instances.Centent.setData(data.productActivity.activityDesc);
                }
            },
            error: function (data) {
                //隐藏新增弹出窗口
                $('#modal_audit_productGrouping').modal('toggle');
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("促销商品取得失败");
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
                    var imgUrl=productGoodInfo.imgUrl;
                    var productGoodFeature = productGoodInfo.productGoodFeature;
                    var productId = productInfo.productId;
                    var productName = productInfo.productName;

                    if(chkProductIdIsSelected(productInfo.productId)){
                        var strOnKeyUp1="value=value.replace(/\\D+/g,'')";
                        var strOnKeyUp2="value=value.replace(/^[0-9]+([.]{1}[0-9]{1,2})?$/,'')";
                        var tr = '<tr id="' + productId + '">'
                                + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
                                + '<td>' + productName + '</td>'
                                + '<td><input type="text" maxlength="10"/> </td>'
                                + '<td><input type="text" maxlength="20" /> </td>'
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
                    newIds=newIds+','+curProductId;
                }
            }
        }
        return newIds
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
