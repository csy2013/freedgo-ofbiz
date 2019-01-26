<!-- Select2 -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/dist/css/AdminLTE.min.css</@ofbizContentUrl>">
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>


    <div class="form-group">
        <label class="control-label col-sm-3"><i class="required-mark">*</i>服务支持编号:</label>
        <div class="col-sm-8">
            <input type="text" class="form-control" id="serviceSupportId" name="serviceSupportId" readonly>
        </div>
    </div>
    <div class="form-group" data-type="required" data-mark="服务支持名称">
        <label class="control-label col-sm-3"><i class="required-mark">*</i>服务支持名称:</label>
        <div class="col-sm-8">
            <input type="text" class="form-control dp-vd" id="serviceSupportName" name="serviceSupportName">
            <p class="dp-error-msg"></p>
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">服务支持图标:</label>
        <div class="col-sm-9">
            <input style="margin-left:5px;" type="button" id="imgUrl" value="选择图片"/>
            <input type="hidden" name="imgUrl" value="">
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">预览图片:</label>
        <div class="col-sm-9">
            <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
        </div>
    </div>

    <div class="form-group" data-type="required" data-mark="帮助">
    <#assign helpInfoList = delegator.findByAnd("HelpInfo",{})/>
        <label class="control-label col-sm-3"><i class="required-mark">*</i>帮助:</label>
        <div class="col-sm-8">
            <select class="form-control select2HelpInfoName dp-vd" id="helpInfoId" name="helpInfoId">
            <#list categoryList as categoryVO>
                <option value="${categoryVO.helpCategoryId}">${categoryVO.categoryName}</option>
            </#list>
            </select>
            <p class="dp-error-msg"></p>
        </div>
    </div>

<!-- script区域start -->
<script>

    $(function(){
        $(".select2HelpInfoName").select2();
    });
</script><!-- script区域end -->
