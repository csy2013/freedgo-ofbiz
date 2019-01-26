<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <input type="hidden" id="linkId"/>
        <input type="hidden" id="selectName"/>
        <!-- 条件查询start -->
        <form id="QueryForm" class="form-inline clearfix">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" id="productName" class="form-control" value="">
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
                <#if security.hasEntityPermission("BANNERMGR_LIST", "_CREATE", session)>
                    <button id="btn_add" class="btn btn-primary">
                        <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
                    </button>
                </#if>
                    <!-- 是否有删除权限-->
                <#if security.hasEntityPermission("BANNERMGR_LIST", "_DEL", session)>
                    <button id="btn_del" class="btn btn-primary">
                        <i class="fa fa-trash"></i>${uiLabelMap.BrandDel}
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

<!-- 删除确认弹出框start -->
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
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">${uiLabelMap.BrandDel}</button>
            </div>
        </div>
    </div>
</div><!-- 删除确认弹出框end -->

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
</div><!-- 提示弹出框end -->



<!-- 编辑弹出框start -->
<div id="modal_edit"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_edit_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">编辑推荐商品</h4>
            </div>
            <div class="modal-body">
                <form id="editForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl></@ofbizUrl>">
                    <input type="hidden" id="recommendId" name="recommendId"/>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="序号">
                            <label class="control-label col-sm-2"><i class="required-mark">*</i>序号:</label>
                            <div class="col-sm-8">
                                <input type="text" class="form-control dp-vd" id="sequenceId" name="sequenceId">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="站点">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
                            <div class="col-sm-10">
                                <div class="col-sm-5" style="padding-left: 0px;">
                                    <select id="webSite" class="form-control select2 dp-vd" multiple="multiple" data-placeholder="请选择站点">
                                    <#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >
                                    <#if webSiteList?has_content>
                                        <#list webSiteList as webSite>
                                            <option value="${webSite.webSiteId}">${webSite.siteName}</option>
                                        </#list>
                                    </#if>
                                    </select>
                                    <p class="dp-error-msg"></p>
                                </div>
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <div class="checkbox">
                                        <label>
                                            <input id="isAllWebSite" type="checkbox">所有站点
                                        </label>
                                    </div>
                                </div>
                            </div>
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
</div><!-- 编辑序号弹出框end -->



<#--<!-- 添加推荐商品弹出框start &ndash;&gt;-->
<#--<div id="editProdutRecommentModal" class="modal fade" tabindex="-1">-->
    <#--<div class="modal-dialog" >-->
        <#--<div class="modal-content">-->
            <#--<div class="modal-header">-->
                <#--<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span-->
                        <#--aria-hidden="true">&times;</span></button>-->
                <#--<h4 class="modal-title" ></h4>-->
            <#--</div>-->
            <#--<div class="modal-body">-->
                <#--<!-- 隐藏输入框 &ndash;&gt;-->
                <#--<form id="mainForm" class="form-horizontal">-->
                    <#--<input type="hidden" name="recommendId" />-->
                    <#--<div class="row">-->
                        <#--<div class="form-group" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">-->
                            <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>-->
                            <#--<div class="col-sm-10">-->
                                <#--<input type="text" name="sequenceId" placeholder="请输序号" class="form-control dp-vd w-p60"/>-->
                                <#--<p class="dp-error-msg"></p>-->
                            <#--</div>-->
                        <#--</div>-->
                    <#--</div>-->

                    <#--<div class="row">-->
                        <#--<div class="form-group" data-type="required" data-mark="站点">-->
                            <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>-->
                            <#--<div class="col-sm-10">-->
                                <#--<div class="col-sm-5" style="padding-left: 0px;">-->
                                    <#--<select id="webSite" class="form-control select2" multiple="multiple" name="webSite" data-placeholder="请选择站点" style="width: 200px;">-->
                                    <#--<#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >-->
                                    <#--<#if webSiteList?has_content>-->
                                        <#--<#list webSiteList as webSite>-->
                                            <#--<option value="${webSite.webSiteId}">${webSite.siteName}</option>-->
                                        <#--</#list>-->
                                    <#--</#if>-->
                                    <#--</select>-->
                                    <#--<p class="dp-error-msg"></p>-->
                                <#--</div>-->
                                <#--<div class="col-sm-3" style="padding-left: 0px;">-->
                                    <#--<div class="checkbox">-->
                                        <#--<label>-->
                                            <#--<input id="isAllWebSite" type="checkbox" value="0" checked name="isAllWebSite">所有站点-->
                                        <#--</label>-->
                                    <#--</div>-->
                                <#--</div>-->
                            <#--</div>-->
                        <#--</div>-->
                    <#--</div>-->

                    <#--<div class="row">-->
                        <#--<div class="form-group" data-type="required"  data-mark="推荐商品">-->
                            <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>推荐商品:</label>-->
                            <#--<div class="col-sm-10">-->
                                <#--<button id="addProducts" type="button" class="btn btn-primary">-->
                                    <#--<i class="fa fa-plus">选择推荐的商品</i>-->
                                <#--</button>-->
                                <#--<input type="hidden" name="selectedProductIds" class="form-control dp-vd w-p60"/>-->
                                <#--<p class="dp-error-msg"></p>-->
                            <#--</div>-->
                        <#--</div>-->
                    <#--</div>-->
                    <#--<div class="row">-->
                        <#--<div class="form-group">-->
                            <#--&lt;#&ndash;<label class="col-sm-2 control-label"></label>&ndash;&gt;-->
                            <#--<div class="col-sm-12">-->
                                <#--<div class="table-responsive no-padding">-->
                                    <#--<table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">-->
                                        <#--<thead>-->
                                        <#--<tr>-->
                                            <#--<th>商品图片</th>-->
                                            <#--<th>商品编码</th>-->
                                            <#--<th>商品名称</th>-->
                                            <#--<th></th>-->
                                        <#--</tr>-->
                                        <#--</thead>-->
                                        <#--<tbody>-->
                                        <#--</tbody>-->
                                    <#--</table>-->
                                <#--</div>-->
                            <#--</div>-->
                        <#--</div>-->
                    <#--</div>-->
                <#--</form>-->
            <#--</div>-->
            <#--<div class="modal-footer">-->
                <#--<!-- 是否有修改权限&ndash;&gt;-->
                <#--<button id="btnSave" type="button" class="btn btn-primary" >保存</button>-->
                <#--<button id="btnCancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>-->
            <#--</div>-->
        <#--</div>-->
    <#--</div>-->
<#--</div>-->
<#--<!-- 修改弹出框end &ndash;&gt;-->

<!-- script区域start -->
<script>
    var data_tbl;
    var ajaxUrl = "prodRecommendList";
    var del_ids;
    var curProductIds="";

    $(function(){
        //初始化select2
        $(".select2").select2({
            closeOnSelect:false
        });
        data_tbl = $('#data_tbl').dataTable({
            ajaxUrl: ajaxUrl,
            columns:[
                {"title":"复选框","code":"recommendId","checked":true},
                {"title":"商品名称","code":"productName"},
                {"title":"商品编号","code":"productId"},
                {"title":"品牌","code":"brandName"},
                {"title":"所属店铺","code":"storeName"},
                {"title":"序号","code":"sequenceId"},
				{"title":"站点","code":"webSiteName"},
                {"title":"操作","code":"option",
                    "handle":function(td,record){
                        var btns = "<div class='btn-group'>"+
                                <!-- 是否都有权限-->
                                <#if security.hasEntityPermission("PRODRECOMEND_LIST", "_UPDATE", session)>
                                "<button type='button' class='btn btn-danger btn-sm'  onclick='javascript:editProdRecommend("+record.recommendId+")'>编辑</button>"+
                                </#if>
                                "<button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>"+
                                "<span class='caret'></span>"+
                                "<span class='sr-only'>Toggle Dropdown</span>"+
                                "</button>"+
                                "<ul class='dropdown-menu' role='menu'>"+
                                "<li class='edit_li'>" +
                                <#if security.hasEntityPermission("PRODRECOMEND_LIST", "_DELETE", session)>
                                "<a href='javascript:setDelete("+record.recommendId+")'>删除</a> </li>"+
                                </#if>
                                "</ul>"+

                                "</div>";
                        td.append(btns);
                    }
                }
            ],
            listName: "firstPageList",
            paginateEL: "paginateDiv",
            viewSizeEL: "view_size"
        });


        //查询按钮点击事件
        $('#QueryForm #searchBtn').on('click',function(){
            var productName = $('#QueryForm #productName').val();
            ajaxUrl = changeURLArg(ajaxUrl,"productName",productName);
            data_tbl.reload(ajaxUrl);
            return false;
        });


        //添加按钮点击事件
        $('#btn_add').click(function(){
            window.location = '<@ofbizUrl>prodRecommendAddPage</@ofbizUrl>';
        });

        //删除按钮点击事件
        $('#btn_del').click(function(){
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if(checks.size() > 0 ){
                del_ids = "";
                //编辑id字符串
                checks.each(function(){
                    del_ids += $(this).val() + ",";
                });

                //设置删除弹出框内容
                $('#modal_confirm #modal_confirm_body').html("确认删除勾选的记录吗？");
                $('#modal_confirm').modal('show');
            }else{
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("请至少选择一条记录！");
                $('#modal_msg').modal();
            }
        });

        //删除弹出框删除按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用删除方法
            $.ajax({
                url: "prodRecommendDel",
                type: "GET",
                data: {ids : del_ids},
                dataType : "json",
                success: function(data){
                    <#--//设置提示弹出框内容-->
                    <#--$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");-->
                    <#--$('#modal_msg').modal();-->
                    <#--//提示弹出框隐藏事件，隐藏后重新加载当前页面-->
                    <#--$('#modal_msg').off('hide.bs.modal');-->
                    <#--$('#modal_msg').on('hide.bs.modal', function () {-->
                        <#--data_tbl.reload(ajaxUrl);-->
                    <#--})-->
                    window.location = '<@ofbizUrl>prodRecommendPage</@ofbizUrl>';
                },
                error: function(data){
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        });

        //所有站点的选中事件
        $('#modal_edit #isAllWebSite').change(function(){
            if($(this).prop("checked")){
                $("#modal_edit #webSite").val(null).trigger("change");
                $("#modal_edit #webSite").prop("disabled", true);
                $("#modal_edit #webSite").removeClass('dp-vd');
            }else{
                $("#modal_edit #webSite").prop("disabled", false);
                $("#modal_edit #webSite").addClass('dp-vd');
            }
        });


        $('#modal_edit #save').click(function(){
            $("#editForm").dpValidate({
                clear: true
            });
            $('#editForm').submit();
        });

        $("#editForm").dpValidate({
            validate: true,
            callback: function(){
                var recommendId=$("#editForm #recommendId").val();
                var sequenceId=$("#editForm #sequenceId").val();
                var isAllWebSite = $('#editForm #isAllWebSite').is(':checked') ? 0 : 1;
                var webSite = $('#editForm #webSite').val() !=null ? $('#editForm #webSite').val().join(',') : '';
                updateProductRecommendInfo(recommendId,sequenceId,isAllWebSite,webSite);
            }
        });
    });

    //行删除操作
    function setDelete(id){
        del_ids = id;
        //设置提示弹出框内容
        $('#modal_confirm #modal_confirm_title').html("删除提示");
        $('#modal_confirm #modal_confirm_body').html("该推荐商品信息删除后将无法使用，是否继续？");
        $('#modal_confirm').modal('show');
    }

    // 编辑推荐商品
    function editProdRecommend(id){
        //清空序号
        $('#modal_edit #sequenceId').val('');
        $('#modal_edit #recommendId').val('');
        $('#modal_edit #isAllWebSite').attr("checked",false);
        $.ajax({
            url: "prodRecommendEditInit",
            type: "POST",
            data : {recommendId:id
            },
            dataType : "json",
            success: function(data){
                if(data){
                    var sequenceId=data.sequenceId;
                    var isAllWebSite=data.isAllWebSite;
                    var webSite=data.webSite;
                    var recommendId=data.recommendId;
                    console.log(webSite);

                    $('#modal_edit #sequenceId').val(sequenceId);
                    $('#modal_edit #recommendId').val(recommendId);
                    //初始化站点选择框
                    if(isAllWebSite == '0'){
                        $('#editForm #isAllWebSite').prop("checked","true");
                        $("#editForm #webSite").prop("disabled", true);
                        $("#editForm #webSite").removeClass('dp-vd');
                    }else{
                        $("#editForm #webSite").val(webSite.split(',')).trigger("change");
                    }
                }

            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
        //设置提示弹出框内容
        $('#modal_edit').modal();
    }
    /**
     * 更新推荐商品
     * @param recommendId
     * @param sequenceId
     * @param isAllWebSite
     * @param webSite
     */
    function updateProductRecommendInfo(recommendId,sequenceId,isAllWebSite,webSite){
        //异步调用编辑序号方法
        $.ajax({
            url: "prodRecommendEdit",
            type: "POST",
            data: {recommendId : recommendId,
                sequenceId:sequenceId,
                isAllWebSite:isAllWebSite,
                webSite:webSite
            },
            dataType : "json",
            success: function(data){
                $.tipLayer("操作成功！");
                window.location = '<@ofbizUrl>prodRecommendPage</@ofbizUrl>';
            },
            error: function(data){
                $.tipLayer("操作失败！");
            }
        });
    }

</script><!-- script区域end -->
