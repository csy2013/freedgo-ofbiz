
<link rel="stylesheet"  href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>"     type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>"      type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>

<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/main.js</@ofbizContentUrl>"></script>

<script type="text/javascript"  src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<div class="box box-info">
    <div class="box-body">

        <div class="box-header">
            <h3 class="box-title m-t-10">商品维护</h3>
        </div>
        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->

        <div class="row m-b-12" style="margin-bottom:15px;">
            <div class="col-sm-6">
                <div class="dp-tables_btn" >
                    <button id="btn_add" class="btn btn-primary"  data-toggle="modal" data-target="#exampleModal">
                        <i class="fa fa-plus" >添加</i>
                    </button>
                    <button id="btn_del" class="btn btn-primary">
                        <i class="fa fa-plus">删除</i>
                    </button>
                    <button id="return" class="btn btn-primary">
                        <i class="fa " href="navigation?navigationGroupId=${requestParameters.navigationGroupId}">返回分类导航</i>
                    </button>
                </div>
            </div>
        </div>



        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>商品编号</th>
                        <th>商品名称</th>
                        <th>是否启用</th>
                        <th>序号</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <#if navigationProducts?has_content>
                        <#list navigationProducts as np>
                            <#assign product = delegator.findByPrimaryKey("Product",{"productId" : np.productId}) />
                            <tr>
                                <td><input value="${np.id}" class="js-checkchild" type="checkbox"></td>
                                <td>${np.productId}</td>
                                <td>${product.productName}</td>
                                <td>
                                    <#if (np.isEnable)?default("0") == "0">
                                        <button type='button' class='btn btn-primary btn-sm btn-sm' value="0" id="${np.id}" onclick="change(this,'isEnable')">否</button>
                                    <#else>
                                        <button type='button' class='btn btn-primary btn-sm btn-sm' value="1" id="${np.id}" onclick="change(this,'isEnable')">是</button>
                                    </#if>
                                </td>
                                <td>

                                    <input type="text" name = "${np.id}" value = "${np.seq}" onkeyup="value=this.value.replace(/\D+/g,'')" size="5" />
                                </td>
                                <td>
                                    <div class='btn-group'>
                                        <button type='button' class='btn btn-danger btn-sm' onclick='baseSet("${np.id}")'>保存</button>
                                        <button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'>
                                            <span class='caret'></span>
                                            <span class='sr-only'>Toggle Dropdown</span>
                                        </button>
                                        <ul class='dropdown-menu' role='menu'>
                                            <li><a href='delNavigationProduct?ids=${np.id}&navigationId=${navigationId}&entityName=NavigationProduct&navigationGroupId=${requestParameters.navigationGroupId}'>删除</a></li>
                                        </ul>
                                    </div>
                                </td>
                        </tr>
                        </#list>
                    <#else>
                    <tr>
                        <td colspan="5">没有任何数据</td>
                    </tr>
                    </#if>


                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">编辑</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="addFirstForm" action="<@ofbizUrl>updateNavigationProduct</@ofbizUrl>" method="post" >
                    <input type = "hidden" name = "id" id="id" />
                    <input type = "hidden" name = "navigationId" id="navigationId" value="${navigationId}" />
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="商品">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品:</label>
                            <div class="col-sm-9">
                                <select id="productId" name="productId" class="form-control select2WebSite dp-vd w-p50" data-placeholder="请选择商品">
                                    <option value="">请选择</option>
                                    <#assign productList = delegator.findByAnd("Product",{"isDel" : "N"}) >
                                    <#if productList?has_content>
                                        <#list productList as p>
                                            <option value="${p.productId}">${p.productName}</option>
                                        </#list>
                                    </#if>
                                </select>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="minCheck" data-mark="是否启用">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>是否启用:</label>
                            <div class="col-sm-9  dp-vd" style="margin-top: 5px">
                                <input type="radio" name="isEnabled" value="1" checked>是&nbsp;&nbsp;&nbsp;&nbsp;
                                <input type="radio" name="isEnabled" value="0">否
                            </div>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="序号">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>序号:</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control dp-vd w-p50" id="seq" name="seq" onkeyup="value=this.value.replace(/\D+/g,'')">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary btn_save1">${uiLabelMap.Save}</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>
    $(function() {
        $('#exampleModal').on('shown.bs.modal',function(){
            $(".select2WebSite").select2();
        });

        //添加一级分类弹框关闭事件
        $('#exampleModal').on('hide.bs.modal', function () {
            $('#addFirstForm').dpValidate({
                clear: true
            });
        });
        //添加一级分类提交按钮点击事件
        $('.btn_save1').click(function(){
            $('#addFirstForm').dpValidate({
                clear: true
            });
            $('#addFirstForm').submit();
        });
        $('#addFirstForm').dpValidate({
            validate: true,
            callback: function(){
                document.getElementById('addFirstForm').submit();
            }
        });
        $('#btn_del').click(function() {
            if (getSelectedIds() != "") {
                window.location.href = "delNavigationProduct?ids=" + getSelectedIds() + "&navigationId=${navigationId}&entityName=NavigationProduct&navigationGroupId=${requestParameters.navigationGroupId}";
            } else {
                alert("请勾选一条记录");
            }
        });
        $('#return').click(function() {
            window.location = '<@ofbizUrl>navigation?navigationGroupId=${requestParameters.navigationGroupId}</@ofbizUrl>';
        });
    });

    function getSelectedIds(){
        var ids = "";
        var checks = $('.js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            //编辑id字符串
            checks.each(function () {
                ids += $(this).val() + ",";
            });
            ids = ids.substring(0,ids.length  -1);
        }
        return ids;
    }

    function change(obj,type){
        var obj = $(obj);
        var newStatus = "";
        var id =  obj.attr("id");
        if (obj.attr("value") == "0"){
            obj.html("是");
            newStatus = "1";
            obj.attr("value","1");
        }else{
            obj.html("否");
            newStatus = "0";
            obj.attr("value","0");
        }
        $.post("changeNavigationProduct",{newStatus : newStatus,id : id,type : type,entityName : "NavigationProduct"},function(data){});
    }

    function baseSet(id){
        var seq = $("input[name=" + id + "]").val();
        window.location.href = "saveNavigationProductSeq?id=" + id + "&seq=" + seq + "&entityName=NavigationProduct&navigationId=${navigationId}&navigationGroupId=${requestParameters.navigationGroupId}";
    }
</script>

