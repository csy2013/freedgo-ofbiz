<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/app.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/drag/drag.jquery.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">物流单据设置</h3>
    </div>
    <div class="box-body">
        <form id="express-form" class="form-inline clearfix" role="form" action="<@ofbizUrl>addDocuments</@ofbizUrl>" method="post">
            <div class="form-group m-b-10">
                <label class="control-label">单据名称：</label>
                <select id="billName" name="billName" class="form-control">
                    <#list logisticsCompanys as logisticsCompany>
                        <#assign logisticsDocuments = delegator.findByAnd("LogisticsDocuments",{"logisticsCompanyId":logisticsCompany.id})>
                         <#--<#if !logisticsDocuments?has_content>-->
                            <#if logisticsDocuments?has_content>
                            <option value="${logisticsCompany.id}">${logisticsCompany.companyName?if_exists}</option>
                        </#if>
                    </#list>
                </select>
            </div>
            <div class="form-group m-b-10">
                <label class="control-label">单据尺寸：</label>
                <input id="billWidth" name="billWidth" type="text" class="form-control" style="width:75px;">
                X
                <input id="billHeight" name="billHeight" type="text" class="form-control" style="width:75px;">
            </div>
            <div class="form-group m-b-10">
                <label class="control-label">单据背景图：</label>
                <button id="chooseBtn" class="btn btn-primary">选择图片</button>
            </div>
            <div class="form-group m-b-10">
                <label class="control-label">添加打印项：</label>
                <select id="printItem" class="form-control select2 select2-hidden-accessible" tabindex="-1" aria-hidden="true" style="width:175px;">
                    <option value="" selected disabled>添加打印项</option>
                    <option value="sendName">发件人-姓名</option>
                    <option value="sendAddress">发件人-地址</option>
                    <option value="sendTelphone">发件人-联系电话</option>
                    <option value="getName">收件人-姓名</option>
                    <option value="getProvince">收件人-省</option>
                    <option value="getCity">收件人-市</option>
                    <option value="getCountry">收件人-区</option>
                    <option value="getAddress">收件人-地址</option>
                    <option value="getPhone">收件人-手机号码</option>
                    <option value="getPostalcode">收件人-邮政编码</option>
                    <option value="goodsName">货品名称</option>
                </select>
            </div>
            <div class="form-group m-b-10">
                <button id="subimit-express" class="btn btn-primary">保存快递单</button>
                <a class="btn btn-primary" href="javascript:history.go(-1);">返回快递单</a>
            </div>
            <input id="express-bg" name="express-bg" type="hidden">
            <input id="express-content" name="express-content" type="hidden">
            <div class="express-box">
                <img class="express-bg" src="">
                <div id="express-field-box" class="express-field-box"></div>
            </div>
        </form>
    </div>
</div>
<script>
    $(function () {
        $(".select2").select2();

        $.chooseImage.int({
            userId: '',
            serverChooseNum: 1,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });

        $('#chooseBtn').click(function(){
            $.chooseImage.show();
            return false;
        });

        $('body').on('click','.img-submit-btn',function(){
            var obj = $.chooseImage.getImgData();

            $.chooseImage.choose(obj,function(data){
                $('#express-bg').val(data.uploadedFile0);
                $('.express-bg').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
            });
        });

        // 添加字段
        $('#printItem').on('select2:select',function(){
            var key = this.value,
                    text = $(':selected',this).text();

            if ($('#mark-'+key).size()) {
                alert('请不要重复添加字段')
            } else {
                var mark = '<span id="mark-'+key+'" class="express-field">'+text+'<i class="del"></i><i class="resize_btn"></i></span>';
                $('#express-field-box').append(mark);

                $('#mark-'+key).dragDrop({
                    fixarea:[0,$('.express-field-box').width()-100,0,$('.express-field-box').height()-50]
                });
            }
        });

        // 删除字段
        $('#express-field-box').on('click','.del',function(){
            $(this).parent().remove();
        });

        // 改变单据宽高
        $('#billWidth').blur(function(){
            var bwidth = parseInt(this.value),
                    maxWidth = $('.express-box').width();

            if (bwidth <= 0 || isNaN(bwidth)) alert('宽度不能为空且必须大于0');
            else if (bwidth > maxWidth) alert('宽度不能大于'+maxWidth);
            else {
                $('.express-field-box,.express-bg').width(bwidth);
            }
        });

        $('#billHeight').blur(function(){
            var bheight = parseInt(this.value),
                    maxHeight = $('.express-box').height();

            if (bheight <= 0 || isNaN(bheight)) alert('高度不能为空且必须大于0');
            else if (bheight > maxHeight) alert('宽度不能大于'+maxHeight);
            else {
                $('.express-field-box,.express-bg').height(bheight);
            }
        });

        var canMove = true;

        // 拖拽字段区块
        $('#express-field-box').on('click','.resize_btn',function(){
            var _field = $(this).parent();
            if(canMove) {
                $(this).on('mousemove',function (e) {
                    _field.width(e.pageX - _field.offset().left - 15);
                    _field.height(e.pageY - _field.offset().top + 5);
                });
            }
            else{
                $(this).off('mousemove');
            }
            canMove = !canMove;
        });

        // 表单提交
        $('#subimit-express').click(function(){
            $('#express-content').val($('#express-field-box').html());
            if(!$("#billName").val()) {
                alert('单据名称不能为空');
                return false;
            }
            if(!$("#billWidth").val()) {
                alert('高度不能为空');
                return false;
            }
            if(!$("#billHeight").val()) {
                alert('宽度不能为空');
                return false;
            }
            if(!$("#express-bg").val()) {
                alert('请选择图片');
                return false;
            }
            //异步调用显示方法
            $.ajax({
                url: "addDocuments",
                type: "GET",
                data: $('#express-form').serialize(),
                dataType : "json",
                success: function(data){
                    $.tipLayer("新增成功！");
                    window.location.href = "<@ofbizUrl>logisticsDocuments</@ofbizUrl>";
                },
                error: function(data){
                    $.tipLayer("新增失败！");
                }
            });
            return false;
        });
    });
</script>