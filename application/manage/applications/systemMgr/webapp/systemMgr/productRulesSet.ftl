<#assign commonUrl = "ProductRulesSet?" />
<div class="box box-info" xmlns="http://www.w3.org/1999/html">
    <div class="box-header with-border">
        <h3 class="box-title m-t-10">审核开关设置</h3>
    </div>
    <div class="box-body">
        <!--分割线 start-->
        <div class="cut-off-rule bg-gray"></div>

<!--是否有查看的权限-->
    <#if security.hasEntityPermission("PRODUCT_RULESSET", "_VIEW", session) || security.hasEntityPermission("PRODUCT_RULESSET", "_UPDATE", session)>
        <form class="form-horizontal" id="AddForm" action="<@ofbizUrl>updateProductRulesSet</@ofbizUrl>" method="post">
            <#if productRules??>
            <div class="col-sm-12 col-md-12">
                    <div class="box-header">
                        <h3 class="box-title">自营商品审核开关设置</h3>
                    </div>
                    <input type="hidden" name="rulesId" value="${productRules.rulesId}"/>
                    <div class="col-sm-8">
                        <label for="subTitle" class="col-sm-3 control-label">是否启用:</label>
                        <div class="from group">
                            <label class="radio-inline">
                                <input type="radio" value="Y" name="physicalProductStatus"
                                <#if productRules.physicalProductStatus="Y">checked</#if> >开启
                            </label>
                            <label class="radio-inline">
                                <input type="radio" value="N" name="physicalProductStatus"
                                       <#if productRules.physicalProductStatus="N">checked</#if> >关闭
                            </label>
                        </div>
                    </div>
            </div>

            <div  class="col-sm-12 col-md-12">
                <div class="box-header">
                    <h3 class="box-title">商家商品审核开关设置</h3>
                </div>
                <div class="col-sm-8">
                    <label for="subTitle" class="col-sm-3 control-label">是否启用:</label>
                    <div class="from group">
                        <label class="radio-inline">
                            <input type="radio" value="Y" name="virtualProductStatus"
                                   <#if productRules.virtualProductStatus="Y">checked</#if> >开启
                        </label>
                        <label class="radio-inline">
                            <input type="radio" value="N" name="virtualProductStatus"
                                   <#if productRules.virtualProductStatus="N">checked</#if> >关闭
                        </label>
                    </div>
                </div>
            </div>
            <#else>
            <div class="col-sm-12 col-md-12">
                <div class="box-header">
                    <h3 class="box-title">自营商品审核开关设置</h3>
                </div>
                <div class="col-sm-8">
                    <label for="subTitle" class="col-sm-3 control-label">是否启用:</label>
                    <div class="from group">
                        <label class="radio-inline">
                            <input type="radio" value="Y" name="physicalProductStatus" checked>开启
                        </label>
                        <label class="radio-inline">
                            <input type="radio" value="N" name="physicalProductStatus" >关闭
                        </label>
                    </div>
                </div>
            </div>

            <div  class="col-sm-12 col-md-12">
                <div class="box-header">
                    <h3 class="box-title">商家商品审核开关设置</h3>
                </div>
                <div class="col-sm-8">
                    <label for="subTitle" class="col-sm-3 control-label">是否启用:</label>
                    <div class="from group">
                        <label class="radio-inline">
                            <input type="radio" value="Y" name="virtualProductStatus" checked>开启
                        </label>
                        <label class="radio-inline">
                            <input type="radio" value="N" name="virtualProductStatus">关闭
                        </label>
                    </div>
                </div>
            </div>
            </#if>

     <!--是否有修改的权限-->
        <#if security.hasEntityPermission("PRODUCT_RULESSET", "_UPDATE", session)>
                    <div class="col-md-2 col-sm-offset-1 text-center">
              <button  type="button" class="btn btn-primary m-r-10"  id="btn_save">${uiLabelMap.Save}</button>
                    </div>
       </#if>
        </form>
    </#if>
    </div>


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
    </div> <!-- 提示弹出框end -->


   <script>
        //添加提交按钮点击事件
        $(function(){
        $('#btn_save').click(function(){
            /*console.log($('#AddForm').serialize());*/
            $('#AddForm').dpValidate({
                clear: true
            });
            $('#AddForm').submit();
        })

       //验证
            $('#AddForm').dpValidate({
                validate: true,
                callback:function(){
                    $.ajax({
                        url: "updateProductRulesSet",
                        type: "POST",
                        data: $('#AddForm').serialize(),
                        dataType : "json",
                        success:function(data){
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作成功！");
                            $('#modal_msg').modal();
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.href='<@ofbizUrl>ProductRulesSet</@ofbizUrl>';
                            })
                        },
                        error:function(data){
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("操作失败！");
                            $('#modal_msg').modal();
                        }
                    })
                }
            })

        })
    </script>