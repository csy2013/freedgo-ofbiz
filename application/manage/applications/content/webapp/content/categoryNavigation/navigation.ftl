<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/admin.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/dist/js/index.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<section class="content cont" style="background: #fff">
    <input id="navigationGroupId" name="navigationGroupId" type="hidden" value="${requestParameters.navigationGroupId}">
    <div class="btn-box m-b-10">
        <button data-toggle="modal" class="btn btn-primary" id="add" data-target="#exampleModal">添加</button>
        <button  id="return"  class="btn btn-primary"  >返回导航分组</button>
    </div>

    <div class="main w-p100">
        <ul class="title">
            <i class="fold glyphicon glyphicon-minus-sign"></i>
            <span class="name">名称</span>
            <span class="img">分类图片</span>
            <span class="enable">是否启用</span>
            <span class="number">序号</span>
            <span class="handle">操作</span>
        </ul>

        <#list navigationRoots as nr>
        <ul class="first root" id="${nr.id}" navigationName="${nr.navigationName}" productCategory="${nr.productCategoryId}" contentId="${(nr.navigationImg)!''}" isEnabled="${nr.isEnable}">
            <i class="fold glyphicon glyphicon-minus-sign"></i>
            <span class="name">${nr.navigationName}</span>
            <span class="img">
            <#if (nr.navigationImg)?has_content>
            	<img alt="" src="/content/control/getImage?contentId=${(nr.navigationImg)!''}" class="cssImgSmall">
            <#else>
            	<img alt="" src="/images/datasource/default/default_img.png" class="cssImgSmall">
            </#if>
            </span>
            <span class="enable">
                <#if (nr.isEnable)?default("0") == "0">
                    <button type='button' class='btn btn-default' value="0" onclick="changeNavigation(this,'isEnable')">否</button>
                <#else>
                    <button type='button' class='btn btn-primary' value="1" onclick="changeNavigation(this,'isEnable')">是</button>
                </#if>
            </span>
            <span class="number">${nr.sequence}</span>
            <span class="handle">
                <div class="btn-group">
                    <button type="button" class=" js-button btn btn-danger btn-sm xl_bj edit" data-toggle="modal" data-target="#exampleModal">编辑</button>
                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
                        <span class="caret"></span>
                        <span class="sr-only">Toggle Dropdown</span>
                    </button>
                    <ul class="dropdown-menu" role="menu">
                        <li><a class="gss_delete first_delete " href="navigationBrand?navigationId=${nr.id}&navigationGroupId=${requestParameters.navigationGroupId}">设置品牌</a></li>
                        <li><a class="gss_delete first_delete " href="navigationBannerEditPage?navigationId=${nr.id}&navigationGroupId=${requestParameters.navigationGroupId}">设置广告</a></li>
                        <li class="del"><a class="gss_delete first_delete">删除</a></li>
                        <li class="add-child"><a class="gss-button" data-toggle="modal" data-target="#exampleModal">添加子分类</a></li>
                        <li class="up"><a class="up-button">上移</a></li>
                        <li class="down"><a class="down-button">下移</a></li>
                    </ul>
                </div>
            </span>
            <#assign navigationSeconds = (delegator.findByAnd("Navigation",{"parentId" : nr.id},Static["org.ofbiz.base.util.UtilMisc"].toList("sequence")))!'' >
            <#if navigationSeconds != "">
            <#list navigationSeconds as ns>
                <ul class="second root" id="${ns.id}" navigationName="${ns.navigationName}" productCategory="${ns.productCategoryId}" contentId="${(ns.navigationImg)!''}" isEnabled="${ns.isEnable}">
                    <i class="fold glyphicon glyphicon-minus-sign"></i>
                    <span class="name">${ns.navigationName}</span>
                    <span class="img">
                    <#if (ns.navigationImg)?has_content>
		            	<img alt="" src="/content/control/getImage?contentId=${(ns.navigationImg)!''}" class="cssImgSmall">
		            <#else>
		            	<img alt="" src="/images/datasource/default/default_img.png" class="cssImgSmall">
		            </#if>
                    </span>
                    <span class="enable">
                        <#if (ns.isEnable)?default("0") == "0">
                            <button type='button' class='btn btn-default' value="0" onclick="changeNavigation(this,'isEnable')">否</button>
                        <#else>
                            <button type='button' class='btn btn-primary' value="1" onclick="changeNavigation(this,'isEnable')">是</button>
                        </#if>
                    </span>
                    <span class="number">${ns.sequence}</span>
                    <span class="handle">
                        <div class="btn-group">
                            <button type="button" class=" js-button btn btn-danger btn-sm xl_bj edit" data-toggle="modal" data-target="#exampleModal">编辑</button>
                            <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
                                <span class="caret"></span>
                                <span class="sr-only">Toggle Dropdown</span>
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <li class="del"><a class="gss_delete first_delete">删除</a></li>
                                <li class="add-child"><a class="gss-button" data-toggle="modal" data-target="#exampleModal">添加子分类</a></li>
                                <li class="up"><a class="up-button">上移</a></li>
                                <li class="down"><a class="down-button">下移</a></li>
                            </ul>
                        </div>
                    </span>
                    <#assign navigationThirds = (delegator.findByAnd("Navigation",{"parentId" : ns.id},Static["org.ofbiz.base.util.UtilMisc"].toList("sequence")))!'' >
                    <#if navigationThirds != "">
                        <#list navigationThirds as nt>
                            <ul class="third root" id="${nt.id}" navigationName="${nt.navigationName}" productCategory="${nt.productCategoryId}" contentId="${(nt.navigationImg)!''}" isEnabled="${nt.isEnable}">
                                <i class="fold glyphicon glyphicon-minus-sign"></i>
                                <span class="name">${nt.navigationName}</span>
                                <span class="img">
                                    <img alt="" src="/content/control/getImage?contentId=${(nt.navigationImg)!''}" class="cssImgSmall">
                                </span>
                                <span class="enable">
                                    <#if (nt.isEnable)?default("0") == "0">
                                        <button type='button' class='btn btn-default' value="0" onclick="changeNavigation(this,'isEnable')">否</button>
                                    <#else>
                                        <button type='button' class='btn btn-primary' value="1" onclick="changeNavigation(this,'isEnable')">是</button>
                                    </#if>
                                </span>
                                <span class="number">${nt.sequence}</span>
                                <span class="handle">
                                    <div class="btn-group">
                                        <button type="button" class=" js-button btn btn-danger btn-sm xl_bj edit" data-toggle="modal" data-target="#exampleModal">编辑</button>
                                        <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
                                            <span class="caret"></span>
                                            <span class="sr-only">Toggle Dropdown</span>
                                        </button>
                                        <ul class="dropdown-menu" role="menu">
                                            <li><a class="gss_delete first_delete" href="navigationProducts?navigationId=${nt.id}&navigationGroupId=${requestParameters.navigationGroupId}">商品维护</a></li>
                                            <li class="del"><a class="gss_delete first_delete">删除</a></li>
                                            <li class="up"><a class="up-button">上移</a></li>
                                            <li class="down"><a class="down-button">下移</a></li>
                                        </ul>
                                    </div>
                                </span>
                            </ul>
                        </#list>
                    </#if>
                </ul>
            </#list>
            </#if>
        </ul>
        </#list>
    </div>


    <!-- 新增分类 start -->
    <div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title" id="exampleModalLabel">编辑</h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" id="addFirstForm" action="<@ofbizUrl>updateNavigation</@ofbizUrl>" method="post" >
                        <input type = "hidden" name = "id" id="idd" />
                        <input type = "hidden" name = "parentId" id="parentId"/>
                        <input type = "hidden" name = "moveId" id="moveId"/>
                        <input type = "hidden" name = "navigationGroupId" value="${requestParameters.navigationGroupId}" />
                        <input type = "hidden" name = "zIndex" id="zIndex" />
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
                                    <select id="productCategory" name="productCategory" class="form-control dp-vd w-p50">
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
                            <div class="form-group">
                                <label for="title" class="col-sm-3 control-label">分类图片:</label>
                                <div class="col-sm-9">
                                    <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                                    <input style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile" value="选择图片"/>
                                    <input type="hidden" id="contentId" name="contentId" class="dp-vd" />
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
                        <div class="modal-footer">
                            <button type="button" class="btn btn-primary btn_save1">${uiLabelMap.Save}</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <!-- 新增分类 model结束-->

</section>

<script>
    $(function() {
        $('#return').click(function() {
            window.location = 'categoryNavigation';
        });
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
        $('body').on('click', '.img-submit-btn', function () {
            var obj = $.chooseImage.getImgData();
            $.chooseImage.choose(obj, function (data) {
                $('#contentId').val(data.uploadedFile0);
                $('#img').attr({"src": "/content/control/getImage?contentId=" + data.uploadedFile0});
            })
        });

        // 图片选择控件显示
        $('#uploadedFile').click(function () {
            $.chooseImage.show();
        });

		// 清空图片控件内容
		$(".btn_save1").on("click", function(){
			$("#exampleModal #img").attr("src","");
			$("#exampleModal #contentId").val("");
			window.location = 'navigation?navigationGroupId=${requestParameters.navigationGroupId}';
		});

    });

    function changeNavigation(obj,type){
        var obj = $(obj);
        var newStatus = "";
        var id = obj.closest(".root").attr('id');
        if (obj.attr("value") == "0"){
            obj.html("是");
            newStatus = "1";
            obj.attr("value","1");
            obj.attr("class","btn btn-primary");
        }else{
            obj.html("否");
            newStatus = "0";
            obj.attr("value","0");
            obj.attr("class","btn btn-default");
        }
        $.post("changeNavigation",{newStatus : newStatus,id : id,type : type},function(data){
            obj.closest(".root").attr('isEnabled',newStatus);
        });
    }
</script>