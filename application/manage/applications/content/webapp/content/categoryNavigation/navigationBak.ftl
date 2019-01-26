<style>
    #example2>div{
        border-top: 1px solid #f4f4f4;
    }
    #example2>div.xl_head{
        border-top: none;
    }
    .xl_head span{
        padding: 8px;
    }
    #example2 span{
        display: inline-block;
        width: 45%;
    }
    .xl_head .xl_mod{
        padding-left: 33px;
    }
    .xl_write{
        padding: 8px;
    }

    .classfy_name{
        padding-left: 8px;
    }
    .first_tr .second_tr,.third_tr{
        padding:8px 0px 8px 8px ;
        border-top: 1px solid #f4f4f4;
    }
    .second_td{
        padding-left: 10px;
        padding-bottom: 8px;
    }
    .third_td{
        padding-left: 50px;

    }
    .form-group .col-sm-2{
        width: 18.666%;
    }
    .form-group .col-sm-10{
        width: 80.666%;
    }
    .p_name,.p_name1{
        padding-left: 20px;
    }
    .xl_icon{
        display: inline-block;
        color: black;
        margin-left: 10px;
        cursor: pointer;
    }
    .xl_icon,.name1{
        vertical-align: middle;
    }
    .second_tr .xl_icon{
        padding-left: 10px;
    }
    .second_tr,.third_tr{
        display: none;
    }
    .second_btn_group{
        margin-left: -16px;
    }
    .third_btn_group{
        margin-left: -26px;
    }
</style>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<!-- Main content -->
<section class="content">
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">分类导航</h3>
        </div>
        <div class="box-body">
            <div class="btn-box m-b-10">
                <!--是否有新增的权限-->
            <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_CREATE", session)>
                <button class="btn btn-info" data-toggle="modal" data-num="1">${uiLabelMap.Add}</button>
            </#if>
            </div>
        </div>
    </div>
</section><!-- /.content -->

<!-- 新增分类导航 start -->
<div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">分类导航编辑</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="addFirstForm" action="<@ofbizUrl>updateNavigation</@ofbizUrl>" method="post" >
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="分类导航名称">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>分类导航名称:</label>
                            <div class="col-sm-9">
                                <input type="text" class="form-control dp-vd w-p50" id="navigationName" name="navigationName">
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="商品分类">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品分类:</label>
                            <div class="col-sm-9">
                                <select id="deliveryCompany" name="deliveryCompany" class="form-control dp-vd w-p50">
                                    <option value="" >==请选择==</option>
                                    <#list productCategorys as pc>
                                        <option value="${pc.productCategoryId}" >${pc.categoryName}</option>
                                    </#list>
                                </select>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="required">
                            <label for="title" class="col-sm-3 control-label">分类图片</label>
                            <div class="col-sm-9">
                                <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                                <input style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile" value="选择图片"/>
                                <input type="hidden" id="contentId" class="dp-vd" />
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="是否启用">
                            <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>是否启用:</label>
                            <div class="col-sm-9  dp-vd " style="margin-top: 5px">
                                <input type="radio" name="isEnabled" value="1"  >是&nbsp;&nbsp;&nbsp;&nbsp;
                                <input type="radio" name="isEnabled" value="0" >否
                            </div>
                            <p class="dp-error-msg"></p>
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
<!-- 新增分类导航 model结束-->


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
<script>
    $(function() {

        // 初始化图片选择
        $.chooseImage.int({
            userId: '',
            serverChooseNum: 1,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });

        //图片保存按钮事件
        $('body').on('click','.img-submit-btn',function(){
            var obj = $.chooseImage.getImgData();
            $.chooseImage.choose(obj,function(data){
                $('#contentId').val(data.uploadedFile0);
                $('#img').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
            })
        });

        // 图片选择控件显示
        $('#uploadedFile').click(function(){
            $.chooseImage.show();
        });

        $(".btn-info").click(function () {
            $("#description").val("");
            var a = $(this).attr("data-num");
            $(".number").val(a);
            $('#exampleModal').modal('show');
        });

        //添加一级分类弹框关闭事件
        $('#exampleModal').on('hide.bs.modal', function () {
            $('#addFirstForm').dpValidate({
                clear: true
            });
        })
        //添加一级分类提交按钮点击事件
        $('.btn_save1').click(function(){
            $('#addFirstForm').dpValidate({
                clear: true
            });
            $('#addFirstForm').submit();
        });
        //表单校验
        $('#addFirstForm').dpValidate({
            validate: true,
            callback: function(){
                $.ajax({
                    url: "createArticleType",
                    type: "POST",
                    data: $('#addFirstForm').serialize(),
                    dataType : "json",
                    success: function(data){
                        if(data.success){
                            //隐藏新增弹出窗口
                            $('#exampleModal').modal('toggle');
                            //设置提示弹出框内容
                            $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                            $('#modal_msg').modal();
                            //提示弹出框隐藏事件，隐藏后重新加载当前页面
                            $('#modal_msg').off('hide.bs.modal');
                            $('#modal_msg').on('hide.bs.modal', function () {
                                window.location.href='<@ofbizUrl>articleType</@ofbizUrl>';
                            })
                        }
                    },
                    error: function(data){
                        //隐藏新增弹出窗口
                        $('#exampleModal').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                        $('#modal_msg').modal();
                    }
                });
            }
        });
    });


</script>