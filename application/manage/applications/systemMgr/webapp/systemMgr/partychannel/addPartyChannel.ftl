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

<form class="form-horizontal" role="form" method="post" name="addPartyChannel" id="addPartyChannel"
      action="<@ofbizUrl>addPartyChannel</@ofbizUrl>">
    <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">渠道基本信息</h3>
        <#---
        <div class=" pull-right">
        <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_ADD", session)>
            <button class="btn btn-success btn-flat" onclick="saveSecKill()">新增</button></#if>
        </div>
        -->
        </div>

        <div class="box-body">
            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="渠道编码">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>渠道编码</label>
                    <div class="col-sm-9">
                        <input type="text"  class="form-control dp-vd" id="channelCode" name="channelCode">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="渠道名称">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>渠道名称</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="channelName" name="channelName">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="说明">
                    <label for="subTitle" class="col-sm-3 control-label">说明</label>
                    <div class=" col-sm-9">
                        <textarea rows="4" class="form-control"  id="remark"  style="resize: none;">

                        </textarea>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="是否启用">
                    <label for="subTitle" class="col-sm-3 control-label">是否启用</label>
                    <div class="col-sm-9 p-t-5">
                        <label class="col-sm-3" title="启用"><input type="radio" name="status" value="1" checked></input>启用</label>
                        <label class="col-sm-3" title="不启用"><input type="radio" name="status" value="0"></input>不启用</label>
                    </div>
                </div>
            </div>

        </div>
    </div>


    <div class="box-footer text-center">
        <#if security.hasEntityPermission("CHANNEL_MANAGE", "_CREATE", session)>
            <button id="save" class="btn btn-primary m-r-10" onclick="savePartyChannel()">保存</button>
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
<script>
    $(function () {

    });
</script>


<script type="text/javascript">

    var flag = true;

    function savePartyChannel() {
        $('#addPromoReduce').dpValidate({
            clear: true
        });
    }


    $("#addPartyChannel").dpValidate({
        validate: true,
        clear: true,
        callback: function () {
            if (flag) {
                addPartyChannel();
            }
        }
    });

    function addPartyChannel() {

        var data ={
            channelCode:$("#channelCode").val(),
            channelName:$("#channelName").val(),
            remark:$("#remark").val(),
            status:$("input[name='status']:checked").val()
        }
        $.ajax({
            url: "addPartyChannel",
            type: "POST",
            async: false,
            data: data,
            dataType: "json",
            success: function (data) {
                if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                    $.tipLayer(data._ERROR_MESSAGE_);
                }else{
                    $('#modal_msg #modal_msg_body').html("保存成功！");
                    $('#modal_msg').modal();
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location = '<@ofbizUrl>partyChannelList</@ofbizUrl>';
                    })
                }
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
            }
        });
    }




    function back() {
        history.back(-1);
    }
</script>
