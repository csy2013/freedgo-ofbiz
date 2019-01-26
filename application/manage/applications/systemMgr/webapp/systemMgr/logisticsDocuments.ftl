<div class="box box-info">
    <div class="box-body">
        <h3 class="">物流单据设置</h3>
        <div class="cut-off-rule bg-gray"></div>

        <!--工具栏start -->
        <div class="row m-b-10">
            <!-- 操作按钮组start -->
            <div class="col-sm-6">
                <div class="dp-tables_btn">
                    <#if security.hasEntityPermission("LOGISTICS_DOCUMENTS", "_CREATE", session)>
                    <button class="btn btn-primary glyphicon glyphicon-plus" onclick="location.href='<@ofbizUrl>addLogisticsDocuments</@ofbizUrl>'">添加</button>
                    </#if>
                </div>
            </div><!-- 操作按钮组end -->
        </div><!-- 工具栏end -->

        <table class="table table-striped table-hover">
            <thead>
            <tr>
                <th width="50"><!-- <input type="checkbox" onclick="allunchecked(this,'logisticsSingleId');" > -->序号</th>
                <th>图片</th>
                <th>物流公司名称</th>
                <th>宽度</th>
                <th>高度</th>
                <th class="w100">创建时间</th>
                <th width="200">操作</th>
            </tr>
            </thead>
            <tbody>
            <#if logisticsDocumentsList?has_content>
            <#list logisticsDocumentsList as logisticsDocuments>
            <tr>
                <td width="50">${logisticsDocuments_index+1}</td>
                <td>
                    <img alt="" src="/content/control/getImage?contentId=${logisticsDocuments.contentId?if_exists}"  height="50">
                </td>
                <#assign logisticsCompany = delegator.findByPrimaryKey("LogisticsCompany", {"id": logisticsDocuments.logisticsCompanyId})>
                <td><#if logisticsCompany?has_content>${logisticsCompany.companyName?if_exists}</#if></td>
                <td>${logisticsDocuments.width?if_exists}</td>
                <td>${logisticsDocuments.height?if_exists}</td>
                <td>${logisticsDocuments.createdStamp?string("yyyy-MM-dd HH:mm:ss")}</td>
                <td>
                    <div class="btn-group">
                    <#if security.hasEntityPermission("LOGISTICS_DOCUMENTS", "_UPDATE", session)>
                        <button type="button" class="btn btn-default" onclick="location.href='<@ofbizUrl>editLogisticsDocuments</@ofbizUrl>?logisticsDocumentsId=${logisticsDocuments.logisticsDocumentsId}'">编辑</button>
                    </#if>
                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                            <span class="caret"></span>
                            <span class="sr-only">Toggle Dropdown</span>
                        </button>
                        <ul class="dropdown-menu" role="menu">
                    <#if security.hasEntityPermission("LOGISTICS_DOCUMENTS", "_DELETE", session)>
                            <li><a href="javascript:void(0);" onclick="delonelogistics('${logisticsDocuments.logisticsDocumentsId}');" >删除</a></li>
                    </#if>
                        </ul>
                    </div>
                </td>
            </tr>
            </#list>
            </#if>
            </tbody>
        </table>
    </div>
</div>
<!-- 显示或隐藏确认弹出框start -->
<div id="modal_confirm"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title"></h4>
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
</div><!-- 显示或隐藏确认弹出框end -->
<script>
    var logisticsDocumentsId;
    $(function (){
        //删除确定按钮点击事件
        $('#modal_confirm #ok').click(function(e){
            //异步调用显示方法
            $.ajax({
                url: "deleteDocuments",
                type: "GET",
                data: {logisticsDocumentsId : logisticsDocumentsId},
                dataType : "json",
                success: function(data){
                    $.tipLayer("删除成功！");
                    window.location.href = "<@ofbizUrl>logisticsDocuments</@ofbizUrl>";
                },
                error: function(data){
                    $.tipLayer("删除失败！");
                }
            });
        });
    });
    //删除
    function delonelogistics(id){
        logisticsDocumentsId = id;
        //设置提示弹出框内容
        $('#modal_confirm #modal_confirm_title').html("删除单据");
        $('#modal_confirm #modal_confirm_body').html("确定要删除此单据吗？");
        $('#modal_confirm').modal('show');
    }
</script>