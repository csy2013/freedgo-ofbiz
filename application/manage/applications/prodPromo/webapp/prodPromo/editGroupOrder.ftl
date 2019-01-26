<!-- Date Picker -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<!-- Daterange picker -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker-bs3.css</@ofbizContentUrl>">
<!-- daterangepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/moment.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker.js</@ofbizContentUrl>"></script>
<!-- datetimepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<input type="hidden" name="activityId" id="activityId" value="${request.getParameter('activityId')}"/>
<input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
<input type="hidden" id="linkId"/>
<input type="hidden" id="selectName"/>
<input type="hidden" id="isPass" name="isPass" value="${parameters.isPass?if_exists}"/>
<form class="form-horizontal" role="form" method="post" name="editGroupOrder" id="editGroupOrder" action="<@ofbizUrl>editGroupOrder</@ofbizUrl>">

    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">修改拼团促销</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="form-group col-sm-6" data-type="required,max" data-number="50" data-mark="活动名称">
                    <label for="subTitle" class="col-sm-4 control-label"><i class="required-mark">*</i>活动名称</label>

                    <div class="col-sm-8">
                        <input type="text" class="form-control dp-vd" id="activityName" name="activityName">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required,linkLt" id="startTimeGroup" data-compare-link="endTimeGroup"
                     data-mark="开始时间" data-compare-mark="结束时间">
                    <label for="startTime" class="col-sm-4 control-label"><i class="required-mark">*</i>开始时间</label>
                    <div class="input-group date form_datetime col-sm-8 p-l-15 p-r-15" data-link-field="startTime">
                        <input id="activityStartDate1" class="form-control" size="16" type="text" readonly>
                        <input id="activityStartDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-4 col-sm-8"></div>
                </div>
            </div>
            <div class="row">
                <div id="endTimeGroup" class="form-group col-sm-6" data-type="required,linkGt" data-compare-link="startTimeGroup"
                     data-mark="结束时间" data-compare-mark="开始时间">
                    <label for="endTime" class="col-sm-4 control-label"><i class="required-mark">*</i>结束时间</label>

                    <div class="input-group date form_datetime col-sm-8 p-l-15 p-r-15" data-link-field="endTime">
                        <input  id="activityEndDate1"  class="form-control" size="16" type="text" readonly>
                        <input id="activityEndDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-4 col-sm-8"></div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>选择商品</label>

                    <div class="col-sm-8">
                    <#--  <@htmlTemplate.lookupField formName="addGroupOrder" position="center" name="productId" id="productId" fieldFormName="LookupProduct"/>
                          <p class="dp-error-msg"></p>-->
                        <button id="addProducts" type="button" class="btn btn-primary">
                            <i class="fa fa-plus">选择参加促销商品</i>
                        </button>
                    </div>
                </div>


            </div>

            <div class="row">
                <div class="form-group col-sm-12">
                    <label for="title" class="col-sm-2 control-label">已选择商品</label>

                    <div class="col-sm-10">
                        <div class="table-responsive no-padding">
                            <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                                <thead>
                                <tr style="background-color: #DDDDDD">
                                    <th>货品图片</th>
                                    <th>货品规格</th>
                                    <th>货品编号</th>
                                    <th>货品名称</th>
                                    <th>拼团人数</th>
                                    <th>拼团金额￥</th>
                                    <th>商品金额￥</th>
                                    <th>操作</th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="required,max" data-number="8" data-mark="单个ID限购数量">
                    <label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>单个ID限购数量</label>
                    <div class="col-sm-8">
                        <input type="number" class="form-control dp-vd" id="limitQuantity" name="limitQuantity">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-sm-12">
                    <label class="col-sm-2 control-label">拼团标签</label>
                    <div class="col-sm-10">
                        <div class="checkbox clearfix">
                            <label class="col-sm-3" title="随时退"><input name="isAnyReturn" id="isAnyReturn" type="checkbox" value="Y">随时退</label>
                        <#--<input type="hidden" name="isAnyReturn" value="N"/>-->
                            <label class="col-sm-3" title="支持过期退"><input name="isSupportOverTimeReturn" id="isSupportOverTimeReturn" value="Y" type="checkbox">支持过期退</label>
                        <#-- <label class="col-sm-3" title="活动可积分"><input name="isSupportScore" id="isSupportScore"  type="checkbox" value="Y">活动可积分</label>
                         <label class="col-sm-3" title="退货返回积分"><input name="isSupportReturnScore"    id="isSupportReturnScore" value="Y"  type="checkbox">退货返回积分</label>
                         <label class="col-sm-3" title="推荐到首页"><input name="isShowIndex" id="isShowIndex" type="checkbox" value="Y">推荐到首页</label>
                         <label class="col-sm-3" title="包邮"><input name="isPostageFree" id="isPostageFree" type="checkbox" value="Y">包邮</label>-->
                        </div>
                        <div class="dp-error-msg"></div>
                    </div>
                </div>
            </div>


            <div class="row p-l-10 p-r-10">
                <div class="form-group">
                    <label for="seo" class="col-sm-2 control-label">活动描述</label>
                    <input type="hidden" id="DetailsContent" >
                    <div class="col-sm-8">
                        <div class="box-body pad">
                            <textarea id="Centent" name="Centent" value="">
                            </textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>


    <div class="box-footer text-center">
        <#if security.hasEntityPermission("PRODPROMO_GROUPORDER", "_ADD", session)>
            <button id="save" class="btn btn-primary m-r-10"  onclick="saveGroupOrder();">保存</button>
        </#if>
        <button type="button" class="btn btn-primary m-r-10" onclick="back()">返回</button>
    </div>
    
</form>


<!-- add user Modal -->
<div id="modal_add" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">>
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">新增拼团规则</h4>
            </div>
            <div class="modal-body">
                <div class="form-group" data-type="required" data-mark="阶梯编号">
                    <label for="orderGouprRuleId" class="control-label col-sm-3"><span class="text-danger">*</span>阶梯编号</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control required" name="orderGouprRuleId" id="orderGouprRuleId">
                    </div>
                    <span id="usertip"></span>
                </div>
                <div class="form-group" data-type="required" data-mark="团购人数">
                    <label for="orderGouprRulePersonNum" class="control-label col-sm-3"><span class="text-danger">*</span>团购人数</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control required" name="orderGouprRulePersonNum" id="orderGouprRulePersonNum">
                    </div>
                </div>

                <div class="form-group" data-type="required" data-mark="团购价格">
                    <label for="orderGouprRulePersonAmount" class="control-label col-sm-3"><span class="text-danger">*</span>团购价格</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control required" name="orderGouprRulePersonAmount" id="orderGouprRulePersonAmount">
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="saveGroupOrderRule();">确定</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
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
<script type="text/javascript">


    $(function () {
        CKEDITOR.replace("Centent");
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
        $('#addProducts').click(function () {
            var sDate = $('#activityStartDate').val();
            var eDate = $('#activityEndDate').val();
            var timestamp2 = Date.parse(new Date(sDate));
            var timestamp3 = Date.parse(new Date(eDate));
            timestamp2 = timestamp2 / 1000;
            timestamp3 = timestamp3 / 1000;
            if (!(sDate && eDate)) {
                $.tipLayer("请输入开始时间、结束时间！");
                return;
            }
            if(sDate>=eDate) {
                $.tipLayer("开始时间要小于等于结束时间！");
                return;
            }
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
        })
        init();

    });

    function init(){
        //获取当前团购信息
        var activityId= $("#activityId").val();
        $.ajax({
            url: "productActivityDetail",
            type: "POST",
            data: {activityId: activityId},
            dataType: "json",
            success: function (data) {
                $("#activityName").val(data.d_activityName);
                //开始时间，结束时间赋值
                var fromDate = data.d_activityStartDate;
                var thruDate = data.d_activityEndDate;
                $("#activityStartDate1").val(fromDate)
                $("#activityStartDate").val(fromDate)
                $("#activityEndDate1").val(thruDate)
                $("#activityEndDate").val(thruDate)

                $("#limitQuantity").val(data.d_limitQuantity);

                if(data.d_isAnyReturn=="Y"){
                    $("#isAnyReturn").prop({checked:true});
                }
                if(data.d_isSupportOverTimeReturn=="Y"){
                    $("#isSupportOverTimeReturn").prop({checked:true});
                }
                var DetailsContent = $("#DetailsContent").val(data.d_activityDesc);
                if (DetailsContent) {
                    CKEDITOR.instances.Centent.setData(data.d_activityDesc);
                }
                //设置促销商品
                var products = data.productList;
                for(var i = 0;i<products.length;i++){
                    var product = products[i];
                    createTable(product);
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
            }
        })


    }

    var curProductIds="";
    var curProductNames="";
    /**
     * 根据商品编码取得商品信息列表
     * @param ids
     */
    function getProductGoodsInfoListByIds(ids){
        $.ajax({
            url: "/catalog/control/getProductGoodsListByIds${externalKeyParam}",
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
                                + '<td>' + productGoodFeature + '</td>'
                                + '<td>' + productId + '</td>'
                                + '<td>' + productName + '</td>'
                                + '<td><select name="activityPersonNum"><option value="2">2人</option><option value="4">4人</option><option value="6">6人</option><option value="8">8人</option></select></td>'
                                + '<td><input name="activityPrice" type="text"></td>'
                                + '<td>' + salesPrice + '</td>'
                                + '<td class="fc_td"><button type="button" data-id="'+productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                + '</tr>';
                        $('#productTable>tbody').append(tr);
                        if(curProductIds==""){
                            curProductIds=productId;
                            curProductNames=productName;
                        }else{
                            curProductIds+=','+productId;
                            curProductNames+=','+productName;
                        }
                    }

                };
                $("#linkId").val(curProductIds)
                $("#selectName").val(curProductNames)
            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });
    }

    function  createTable(product) {
        var salesPrice = product.salesPrice;
        var imgUrl=product.imgUrl;
        var productGoodFeature = product.productGoodFeature;
        var productId = product.productId;
        var productName = product.productName;
        var activityPersonNum =product.activityPersonNum;
        var activityPrice =product.activityPrice;

        if(chkProductIdIsSelected(product.productId)){
            var tr = '<tr id="' + productId + '">'
                    + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                    + '<td>' + productGoodFeature + '</td>'
                    + '<td>' + productId + '</td>'
                    + '<td>' + productName + '</td>'
                    + '<td><select name="activityPersonNum" ><option value="2">2人</option><option value="4">4人</option><option value="6">6人</option><option value="8">8人</option></select></td>'
                    + '<td><input name="activityPrice" type="text" value="'+activityPrice+'"></td>'
                    + '<td>' + salesPrice + '</td>'
                    + '<td class="fc_td"><button type="button" data-id="'+productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                    + '</tr>';
            $('#productTable>tbody').append(tr);
            $("tr[id='"+productId+"']").parent().find("td").eq(4).find("select").eq(0).val(activityPersonNum);
            if(curProductIds==""){
                curProductIds=productId;
                curProductNames=productName;
            }else{
                curProductIds+=','+productId;
                curProductNames+=','+productName;
            }
        }

        $("#linkId").val(curProductIds)
        $("#selectName").val(curProductNames)


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
     * 返回处理
     */
    function back() {
        window.location.href = '<@ofbizUrl>findGroupOrder</@ofbizUrl>';
    }

    //商品删除按钮事件
    $(document).on('click', '.js-button-assocgood', function () {
        var id=$(this).data("id");
        $(this).parent().parent().remove();
        curProductIds=updateProductIdsInfo(id);
        $("#linkId").val(curProductIds);
    })
    function updateProductIdsInfo(productId){
        var ids=curProductIds.split(",");
        var newIds="";
        for(var i=0;i<ids.length;i++){
            var curProductId=ids[i];
            if(productId!=curProductId){
                if(newIds==""){
                    newIds=curProductId;
                }else{
                    newIds=newIds+","+curProductId;
                }
            }
        }
        return newIds
    }

    var numFlag=true;
    function saveGroupOrder() {
        $('#editGroupOrder').dpValidate({
            clear: true
        });
    }
    $("#editGroupOrder").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (numFlag) {
                editGroupOrder();
            }
        }
    });
    function editGroupOrder() {
        $('#addGroupOrder').dpValidate({
            clear: true
        });
        var lineLen = $('#productTable>tbody').find("tr").length;
        if(lineLen==0){
            $('#modal_msg #modal_msg_body').html("请选择参加团购的商品");
            $('#modal_msg').modal();
            return;
        }
        //获取团购商品的详细信息
        var productLineList = "";
        var productIds="";
        var lines =$('#productTable>tbody').find("tr");
        for(var i = 0; i<lines.length;i++){
            var tdArr = $(lines[i]);
            var productId = tdArr.find('td').eq(2).text();
            var activityPersonNum = tdArr.find('td').eq(4).find("select").eq(0).val();//团购人数
            if(activityPersonNum==null||activityPersonNum==""){
                $('#modal_msg #modal_msg_body').html("商品编码为："+productId+" 的团购人数不能为空");
                $('#modal_msg').modal();
                return;
            }
            var activityPrice = tdArr.find('td').eq(5).find("input").eq(0).val();//团购金额
            if(activityPrice==null||activityPrice==""){
                $('#modal_msg #modal_msg_body').html("商品编码为："+productId+" 的团购金额不能为空");
                $('#modal_msg').modal();
                return;
            }
            var line = productId+","+activityPersonNum+","+activityPrice;
            if(productIds==""){
                productIds=productId;
            }else{
                productIds=productIds+","+productId;
            }
            productLineList=productLineList+line+"|"
        }
        var isAnyReturn = 'N';
        if ($('#isAnyReturn').is(':checked')) {
            isAnyReturn = 'Y';
        }
        var isSupportOverTimeReturn = 'N';
        if ($('#isSupportOverTimeReturn').is(':checked')) {
            isSupportOverTimeReturn = 'Y';
        }
        var data = {
            activityId:$("#activityId").val(),
            productStoreId: $('#productStoreId').val(),
            productLineList:productLineList,
            activityName: $('#activityName').val(),
            activityStartDate: $('#activityStartDate').val(),
            activityEndDate: $('#activityEndDate').val(),
            limitQuantity: $('#limitQuantity').val(),//每个id限购数量
            isAnyReturn: isAnyReturn,
            isSupportOverTimeReturn: isSupportOverTimeReturn,
            isSupportScore: "N",
            isSupportReturnScore: "N",
            isShowIndex: "N",
            isPostageFree: "N",
            activityDesc: CKEDITOR.instances.Centent.getData(),
            productIds:productIds
        }

        $.ajax({
            url: "updateGroupOrder",
            type: "POST",
            async: false,
            data: data,
            dataType: "json",
            success: function (data) {
                if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                    $.tipLayer(data._ERROR_MESSAGE_);
                }else{
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
                            window.location.href = '<@ofbizUrl>findGroupOrder</@ofbizUrl>';
                        })
                    }
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
                /*$('#modal_msg').on('hide.bs.modal', function () {
                    window.location.reload();
                })*/
            }
        })


    }


</script>
