<!-- Select2 -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/dist/css/AdminLTE.min.css</@ofbizContentUrl>">
<style>.content-wrapper{padding-top: 0px;}</style>
<!-- Select2 -->
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">基本信息</h3>
    </div>
    <div class="box-body">
        <form class="form-horizontal" method="post"  role="form" action="saveHelpInfo" name="editDataForm"  id="editDataForm"  class="" >
            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>帮助编号:</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control" id="helpInfoId" name="helpInfoId" readonly>
                    </div>
                </div>
            </div>

            <div class="row title">
                <div class="form-group col-sm-6" data-type="required" data-mark="所属分类">
                <#assign categoryList = delegator.findByAnd("HelpCategory",{})/>
                    <label class="col-sm-3 control-label"><i class="required-mark">*</i>所属分类:</label>
                    <div class="col-sm-9">
                        <select class="form-control select2CategoryName dp-vd" id="helpCategoryId" name="helpCategoryId">
                        <#list categoryList as categoryVO>
                            <option value="${categoryVO.helpCategoryId}">${categoryVO.categoryName}</option>
                        </#list>
                        </select>
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6"  data-type="required" data-mark="帮助标题">
                    <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>标题名称:</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="helpTitle" name="helpTitle" >
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label class="col-sm-3 control-label">分类图片:</label>
                    <div class="col-sm-9">
                        <input style="margin-left:5px;" type="button" id="imgUrl" value="选择图片"/>
                        <input type="hidden" name="helpIcon" value="">
                    </div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-sm-6">
                    <label class="col-sm-3 control-label">预览图片:</label>
                    <div class="col-sm-9">
                        <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="作者">
                    <label class="col-sm-3 control-label"><i class="required-mark">*</i>作者:</label>
                    <div class="col-sm-9" >
                            <input type="text" name="helpAuthor" class="form-control dp-vd">
                            <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row"><#--data-type="required" data-mark="帮助排序"-->
                <div class="form-group col-sm-6"  data-type="format" data-reg="/^\+?[1-9][0-9]*$/" data-msg="排序请填写数字">
                    <label class="control-label col-sm-3"><i class="required-mark">*</i>帮助排序:</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="sequenceNum" name="sequenceNum">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label class="control-label col-sm-3">是否底部显示:</label>
                    <div class="col-sm-9 radio">
                        <label class="col-sm-3"><input name="isShow" type="radio" checked value="Y">是</label>
                        <label class="col-sm-3"><input name="isShow" type="radio" value="N">否</label>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label class="control-label col-sm-3">展示渠道:</label>
                    <div class="col-sm-9 checkbox">
                        <label class="col-sm-3"><input type="checkbox" name="showChannel" checked value="P">PC端</label>
                        <label class="col-sm-3"><input type="checkbox" name="showChannel" checked value="M">移动端</label>
                    </div>
                </div>
            </div>

            <div class="row js-pcdetails">
                <div class="form-group col-sm-6">
                    <label for="seo" class="col-sm-3 control-label">PC端详情:</label>
                </div>
            </div>
            <div class="row js-pcdetails">
                <div class="form-group">
                    <label class="col-sm-1 control-label"></label>
                    <div class="col-sm-8">
                        <textarea id="helpContent" name="helpContent" rows="6" cols="80" value="" >
                        </textarea>
                    </div>
                </div>
            </div>


            <!-- 按钮组 -->
            <div class="box-footer text-center">
                <button id="save" type="button" class="btn btn-primary m-r-10">保存</button>
            </div>
        </form>
    </div><!-- /.box-body -->
</div>

<script type="text/javascript">

    $(function(){
        $('#editDataForm #save').unbind("click") ;
        $('#editDataForm #save').bind('click',function(){
            // 取得pc详情信息
            $("#helpContent").val(CKEDITOR.instances.helpContent.getData());
            $("#editDataForm").dpValidate({
                clear: true
            });
            $("#editDataForm").submit();
        });
        // 数据校验
        $("#editDataForm").dpValidate({
            validate:true,
            callback:function(){
                $("#linkName").val($("#selectName").html());
                $.ajax({
                    url: "saveHelpInfo",
                    type: "POST",
                    data :$("#editDataForm").serialize() ,
                    dataType : "json",
                    success: function(data){
                        if(!data._ERROR_MESSAGE_){
                            $.tipLayer("操作成功！");
                            window.location = '<@ofbizUrl>helpInfoList</@ofbizUrl>';
                        }else{
                            $.tipLayer("操作失败！");
                        }
                    },
                    error: function(data){
                        $.tipLayer("操作失败！");
                    }
                });
            }
        }) ;


        $(".select2CategoryName").select2();
        // 富文本编辑器
        CKEDITOR.replace("helpContent");
        /***************************************************************************************************/
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
                $('#img').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
                $("input[name='helpIcon']").val("/content/control/getImage?contentId="+data.uploadedFile0) ;
            })
        });

        // 图片选择控件显示
        $('#imgUrl').click(function(){
            $.chooseImage.show();
        });

    });
</script>
