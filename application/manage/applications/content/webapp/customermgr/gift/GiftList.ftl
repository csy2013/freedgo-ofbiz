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
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <input type="hidden" id="linkId"/>
        <input type="hidden" id="selectName"/>
        <form id="QueryForm" class="form-inline clearfix" onsubmit="return false;">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">礼品名称</span>
                    <input type="text" id="searchGiftName" class="form-control" value="">
                </div>

            </div>
                <div class="input-group pull-right m-l-10">
                    <button id="searchBtn" class="btn btn-success btn-flat">搜索</button>
                </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <!-- 是否有新增权限-->
                <#if security.hasEntityPermission("GIFTMANAGE_LIST", "_CREATE", session)>
                    <button id="addProducts" type="button" class="btn btn-primary">
                        <i class="fa fa-plus">添加商品</i>
                    </button>
                </#if>
                    <!-- 是否有删除权限-->
                <#if security.hasEntityPermission("GIFTMANAGE_LIST", "_DEL", session)>
                    <button id="btn_del" class="btn btn-primary">
                        <i class="fa fa-trash"></i>删除
                    </button>
                </#if>
                </div>
            </div><!-- 操作按钮组end -->
            <!-- 列表当前分页条数start -->
            <div class="col-sm-6">
                <div id="view_size" class="dp-tables_length">
                </div>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->
        <!-- 表格区域start -->
        <div class="row">
            <div class="col-sm-12">
                <table id="data_tbl" class="table table-bordered table-hover js-checkparent">
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <div class="row" id="paginateDiv">
        </div><!-- 分页条end -->
    </div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- 删除确认弹出框start 编辑下方的删除 -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
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

<!-- 删除确认弹出框 上方删除start -->
<div id="modal_confirm_batch"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title">${uiLabelMap.FacilityOptionMsg}</h4>
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

<!-- 提示弹出框start -->
<div id="modal_msg"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
            </div>
        </div>
    </div>
</div>
<!-- 提示弹出框end -->

<!-- 修改弹出框start -->
<div id="modal_edit"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_edit_title">编辑礼品</h4>
            </div>
            <div class="modal-body">
                <form id="EditForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>savePromoGiftProducts</@ofbizUrl>">

                    <input type="hidden" id="promoGiftId" name="promoGiftId">
                    <div class="form-group" data-type="required" data-mark="礼品名称">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>礼品名称:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="giftName" name="giftName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group" data-type="required" data-mark="礼品类型">
                        <label class="control-label col-sm-3"><i class="required-mark">*</i>礼品类型:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control dp-vd" id="giftType" name="giftType" readonly>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>

                    <div class="form-group">
                        <label class="control-label col-sm-3">是否置顶:</label>
                        <div class="col-sm-9 radio">
                            <label class="col-sm-3"><input name="isTop" type="radio" value="Y">是</label>
                            <label class="col-sm-3"><input name="isTop" type="radio" value="N">否</label>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div>
<!-- 修改弹出框end -->

<!-- script区域start -->
<script>
    var hc={
        hcDataTable:'',
        init:function(){
            //
            /*alert( $("#AddForm input[name='isShow']").eq(0).prop("checked")) ;
            console.log($("#AddForm input[name='isShow']").eq(0))

            $("#AddForm input[name='isShow']").eq(0).prop("checked",true) ;*/
            hc.loadData() ;

            //添加商品按钮
            $("#addProducts").click(function () {
                hc.addGift();
            });


            //查询按钮点击事件
            $('#QueryForm #searchBtn').on('click',function(){
                hc.searchByCondition() ;
            });

            // 删除操作
            $("#btn_del").click(function(){
                hc.deleteData();
            });

            //修改弹出窗关闭事件
            $('#modal_edit').on('hide.bs.modal', function () {
                $('#EditForm').dpValidate({
                    clear: true
                });
            });

        },
        URL:{
            getDataUrl:function(){return "findPromoGiftProducts"} ,
            deleteUrl:function(){return "deletePromoGiftProducts"},
            findOneUrl:function(){return "editPromoGiftProducts"},
            saveDataUrl:function(){return "addPromoGiftProducts"},
            editDataUrl:function(){return "savePromoGiftProducts"},
            settingIsUseUrl:function(){return "settingIsUse"},
            getProduct:function(){return "/catalog/control/ProductListMultiModalPageForGift${externalKeyParam}"}
        },
        loadData:function(){
            hc.hcDataTable = $('#data_tbl').dataTable({
                ajaxUrl: hc.URL.getDataUrl(),
                columns:[
                    <!-- 是否有审核权限-->
                    {"title":"复选框","code":"promoGiftId","checked":true},
                    {"title":"礼品名称","code":"giftName"},
                    {"title":"礼品类型","code":"giftType",
                        "handle":function(td,record){
                            if(record.giftType == "实物商品"){
                                td.html("商品");
                            }else if(record.giftType == "商品"){
                                td.html("商品");
                            }else{
                                td.html("代金卷");
                            }
                        }},
                    {"title":"创建时间","code":"createdStamp","handle":function(td,record){
                        td.html((new Date(record.createdStamp.time)).Format("yyyy-MM-dd hh:mm:ss"));
                    }},
                    {"title":"是否置顶","code":"isTop",
                        "handle":function(td,record){
                            if(record.isTop == "Y"){
                                td.html("<button class='btn btn-primary' onclick='javascript:hc.editIsShowData("+record.promoGiftId+",\"N\")'>${uiLabelMap.Y}</button>");
                            }else if(record.isTop == "N"){
                                td.html("<button class='btn btn-default' onclick='javascript:hc.editIsShowData("+record.promoGiftId+", \"Y\")'>${uiLabelMap.N}</button>");
                            }
                        }},
                    {"title":"操作","code":"option",

                        "handle":function(td,record){
                            var btns = "<div class='btn-group'>"+
                                    <!-- 是否都有权限-->
                                    "<button type='button' class='btn btn-danger btn-sm'  onclick='javascript:hc.editData("+record.promoGiftId+")'>编辑</button>"+
                                    "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
                                    "<span class='caret'></span>"+
                                    "<span class='sr-only'>Toggle Dropdown</span>"+
                                    "</button>"+
                                    "<ul class='dropdown-menu' role='menu'>"+
                                    "<li class='edit_li'>" +
                                    //后面需要加上
                                    <#if security.hasEntityPermission("GIFTMANAGE_LIST", "_DEL", session)>
                                    "<a href=\"#\" class=\"gss_Up\"  onclick='javascript:setDelete("+record.promoGiftId+")'>删除</a> </li>"+
                                    </#if>
                                    "</ul>"+

                                    "</div>";
                            td.append(btns);
                        }
                    }
                ],
                listName: "recordsList",
                paginateEL: "paginateDiv",
                viewSizeEL: "view_size"
            });
        },
        changeURLArg:function(url,arg,arg_val){
            if ('undefined' == typeof arg_val || "" == arg_val)
                return  url ;
            var pattern=arg+'=([^&]*)';
            var replaceText=arg+'='+arg_val;
            if(url.match(pattern)){
                var tmp='/('+ arg+'=)([^&]*)/gi';
                tmp=url.replace(eval(tmp),replaceText);
                return tmp;
            }else{
                if(url.match('[\?]')){
                    return url+'&'+replaceText;
                }else{
                    return url+'?'+replaceText;
                }
            }
            return  url+'\n'+arg+'\n'+arg_val ;
        },
        searchByCondition:function(){
            var giftName = $('#QueryForm #searchGiftName').val();
            var search_AjaxUrl = hc.changeURLArg(hc.URL.getDataUrl(),"giftName",giftName);
            hc.hcDataTable.reload(search_AjaxUrl);
            return false;
        },
        addGift:function () {
            $.dataSelectModal({
                url: hc.URL.getProduct(),
                width: "800",
                title: "选择商品",
                selectId: "linkId",
                selectName: "selectName",
                multi: true,
                selectCallBack: function (el) {
                    var productId = el.data('id');
                    console.log(productId)
//                    var productTypeName=el.data('typename');
                    savePromoGift(productId);
                }
            });

        },
        editIsShowData : function(id, isTop){
        // spj
        	if(id){
        		$.ajax({
                    url: hc.URL.saveDataUrl(),
                    type: "POST",
                    data: {promoGiftId:id, isTop:isTop},
                    dataType : "json",
                    success: function(data){
	                	hc.hcDataTable.reload(hc.URL.getDataUrl());
                    },
                    error: function(data){
                        //隐藏新增弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
        	}
        },
        editData:function(id){
            if(id){
                //清空form
                clearForm($("#EditForm"));
                $.ajax({
                    url: hc.URL.findOneUrl(),
                    type: "GET",
                    data : {promoGiftId:id},
                    dataType : "json",
                    success: function(data){
                        $('#modal_edit #promoGiftId').val(data.promoGift.promoGiftId);
                        $('#modal_edit #giftName').val(data.promoGift.giftName);

                        if(data.promoGift.giftType == "实物商品"){

                            $('#modal_edit #giftType').val("商品");

                        }else if (data.promoGift.giftType == "商品"){

                            $('#modal_edit #giftType').val("商品");

                        }else if(data.promoGift.giftType == "虚拟商品"){

                            $('#modal_edit #giftType').val("代金卷");

                        }else{
                            $('#modal_edit #giftType').val("代金卷");
                        }

                        if(data.promoGift.isTop){
                            $("#EditForm input[name='isTop']").each(function(i,e){
                                if(data.promoGift.isTop == $(e).val()){
                                    $(e).attr("checked","true");
                                }
                            });
                        }
                        $('#modal_edit').modal();
                    },
                    error: function(data){
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                        $('#modal_msg').modal();
                    }
                });
            }
            return false ;
        },
        deleteData:function(){
            var idArr = new Array() ;
            $("#data_tbl .js-checkchild:checked").each(function(){
                idArr.push($(this).val()) ;
            });
            if(idArr.isEmpty()){
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
                return false;
            }
            
	        //设置提示弹出框内容
	        $('#modal_confirm_batch #modal_confirm_title').html("删除提示");
	        $('#modal_confirm_batch #modal_confirm_body').html("删除后无法再被使用，是否继续？");
	        $('#modal_confirm_batch').modal('show');
	        
            //删除弹出框确定按钮点击事件
	        $('#modal_confirm_batch #ok').click(function(e){
	            $.post(hc.URL.deleteUrl(),{"promoGiftIds":idArr.toString()},function(data){
	                $.tipLayer("操作成功！");
	                hc.hcDataTable.reload(hc.URL.getDataUrl());
	            });
	        });
        },
        changeIsUse:function(id,isUse){
            $.post(hc.URL.settingIsUseUrl(),{"helpCategoryId":id,"isUse":isUse},function(data){
                $.tipLayer("操作成功！");
                hc.hcDataTable.reload(hc.URL.getDataUrl());
            })
        }
    };

    Date.prototype.Format = function(fmt){ //author: meizz
        var o = {
            "M+" : this.getMonth()+1,                 //月份
            "d+" : this.getDate(),                    //日
            "h+" : this.getHours(),                   //小时
            "m+" : this.getMinutes(),                 //分
            "s+" : this.getSeconds(),                 //秒
            "q+" : Math.floor((this.getMonth()+3)/3), //季度
            "S"  : this.getMilliseconds()             //毫秒
        };
        if(/(y+)/.test(fmt))
            fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));
        for(var k in o)
            if(new RegExp("("+ k +")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));
        return fmt;
    };

    var del_ids;  //行删除Id
    $(function(){
        hc.init() ;

        $("#EditForm").dpValidate({
            validate: true,
            callback: function(){

                //异步调用新增方法
                $.ajax({
                    url: hc.URL.editDataUrl(),
                    type: "POST",
                    // data: $('#EditForm').serialize(),
                    data: {
                        promoGiftId:$("#modal_edit #promoGiftId").val(),
                        giftName:$("#modal_edit #giftName").val(),
                        giftType:$("#modal_edit #giftType").val(),
                        isTop:$("#EditForm input[name='isTop']").val()
                    },

                    dataType : "json",
                    success: function(data){
                        // console.log('data-->'+data);

                        //隐藏新增弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            hc.hcDataTable.reload(hc.URL.getDataUrl());
                        })
                    },
                    error: function(data){
                        //隐藏新增弹出窗口
                        $('#modal_edit').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

        //修改弹出框保存按钮点击事件
        $('#modal_edit #save').click(function(){
            $("#EditForm").dpValidate({
                clear: true
            });
            $('#EditForm').submit();
        });

        //删除弹出框确定按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用删除方法
            $.ajax({
                url: hc.URL.deleteUrl(),
                type: "GET",
                data: {promoGiftIds : del_ids},
                dataType : "json",
                success: function(data){
                    //弹出提示信息
                    $.tipLayer("操作成功！");
                    hc.hcDataTable.reload(hc.URL.getDataUrl());
                },
                error: function(data){
                    //弹出提示信息
                    $.tipLayer("操作成功！");
                }
            });
        });

    });

    var curProductIds="";

    /**
     * 根据商品编码取得商品信息列表
     * @param ids
     */
    function getProductGoodsInfoListByIds(ids){
        $.ajax({
            // url: "/catalog/control/getProductGoodsListByIds",
            url: "/catalog/control/ProductListForJson?externalLoginKey=${externalLoginKey}",
            type: "POST",
            data: {productId: ids},
            dataType: "json",
            success: function (data) {
                console.log('查询后的数据data-->'+data);
                // var productGoodInfoList = data.productGoodInfoList;
                var recordsList = data.recordsList[0];
                var productName = recordsList.productName;
                var productTypeName = recordsList.productTypeName;
                savePromoGift(ids,productName,productTypeName);

            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });
    }

    //新增保存
    function savePromoGift(productId){
        $.ajax({
            url: "addPromoGiftProducts",
            type: "POST",
            data: {
                productId: productId
            },
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                }else if(data.hasOwnProperty("_ERROR_MESSAGE_LIST_")){
                    $.tipLayer(data._ERROR_MESSAGE_LIST_);
                } else {
                    if(data.retCode==0){
                        $('#modal_msg #modal_msg_body').html(data.message);
                        $('#modal_msg').modal();
                    }else{
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作成功");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').on('hide.bs.modal', function () {
                            window.location.href = '<@ofbizUrl>GiftList</@ofbizUrl>';
                        });
                    }
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
            }
        });
    }

    //行删除操作
    function setDelete(id){
        del_ids = id;
        //设置提示弹出框内容
        $('#modal_confirm #modal_confirm_title').html("删除提示");
        $('#modal_confirm #modal_confirm_body').html("删除后无法再被使用，是否继续？");
        $('#modal_confirm').modal('show');
    }
</script><!-- script区域end -->
