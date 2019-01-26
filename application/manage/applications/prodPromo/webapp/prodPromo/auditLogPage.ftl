<style type="text/css">
    #auditLogTable .form-group span {
        padding-top: 7px;
        padding-left: 0px;
    }
</style>

<!-- 内容start -->
<form id="auditLogTable" class="form-horizontal">
    <div class="modal-body">
        <table class="table table-bordered table-hover js-checkparent xl_table" id="logTable">
            <thead>
            <tr>
                <th>操作类型</th>
                <th>操作人</th>
                <th>操作时间</th>
                <th>操作原因</th>
            </tr>
            </thead>
            <tbody>

            </tbody>
        </table>

    </div>
<#--<div id="showOperateLog"  >
    <div >
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="showOperateLog_title">操作日志</h4>
            </div>

        &lt;#&ndash;<div class="modal-footer">
            <button id="save" type="button" class="btn btn-primary">确认</button>
            <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
        </div>&ndash;&gt;
        </div>
    </div>
</div>-->


</form>


<script>

    $(function () {
        var businessId = '${parameters.businessId}';
        $.ajax({
            url: "getAuditLog?businessId=" + businessId,
            type: "POST",
            dataType: "json",
            success: function (data) {
                if(data.auditLogList.length==0){
                    $("#logTable tbody").append("<tr><td colspan='4'>无操作记录</td></tr>");
                }else{
                    var h= "";
                    for (var i = 0; i < data.auditLogList.length; i++){
                        var auditLog = data.auditLogList[i];
                        h += "<tr><td>" + auditLog.auditType + "</td><td>" + auditLog.auditPersonName + "</td><td>" + auditLog.auditTime + "</td><td>"+ auditLog.auditMessage +"</td></tr>";
                    }
                    $("#logTable tbody").append(h);
                }

            }
        });
    });
</script>