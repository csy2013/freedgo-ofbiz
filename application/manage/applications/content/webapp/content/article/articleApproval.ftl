<link rel="stylesheet"
      href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>"
      type="text/css"/>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript"
        src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<!-- Main content -->
<#assign commonUrl = "articleApproval?"+ paramList+"&" />
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title m-t-10">${uiLabelMap.articleApproval}</h3>
    </div>
    <div class="box-body">
        <form class="form-inline clearfix" id="searchForm" role="form" action="<@ofbizUrl>articleApproval</@ofbizUrl>">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.articleType}</span>
                    <input type="text" class="form-control" id="treeName" placeholder="全部" <#if description?has_content>
                           value="${description?if_exists}"</#if> onclick="showMenu()" readonly="readonly"/ >
                    <input type="hidden" class="form-control" id="articleTypeId"
                           name="articleTypeId"  <#if articleTypeId?has_content>
                           value="${articleTypeId?if_exists}"</#if>/>
                    <!-- zTree start-->
                    <div id="menuContent" class="menuContent"
                         style="display:none; position: absolute;top:33px;left:81px;border:1px solid #ccc;background:white;z-index:1000;width:196px;">
                        <ul id="treeDemo" class="ztree" style="margin-top: 0; width: 110px;">
                        </ul>
                    </div>
                    <!-- zTree end-->
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">${uiLabelMap.articleTitle}</span>
                    <input type="text" class="form-control" name="articleTitle" placeholder="${uiLabelMap.articleTitle}"
                           value="${articleTitle?if_exists}">
                </div>
                <input type="hidden" class="form-control" name="articleStatus" value="1"/>
            </div>
            <div class="input-group pull-right">
                <!-- 是否有查看权限-->
            <#if security.hasEntityPermission("CONTENT_ARTICLEAPPROVAL", "_VIEW", session)>
                <button class="btn btn-success btn-flat">${uiLabelMap.Search}</button>
            </#if>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
        <!-- 操作按钮组start -->
    <div class="row m-b-10">
        <div class="col-sm-6">
            <div class="btn-box m-b-10">
                <!-- 是否有更新权限-->
            <#if security.hasEntityPermission("CONTENT_ARTICLEAPPROVAL", "_UPDATE", session)>
                <button id="btn_approval" class="btn btn-primary">
                    批量通过
                </button>
            </#if>
                <!-- 是否有更新权限-->
            <#if security.hasEntityPermission("CONTENT_ARTICLEAPPROVAL", "_UPDATE", session)>
                <button class="btn btn-primary" id="btn_resolute">
                    批量拒绝
                </button>
            </#if>
            </div>
        </div> <!-- 操作按钮组end -->
    <#if articleList?has_content >
        <!-- 列表当前分页条数start -->
        <div class="col-sm-6">
            <div class="dp-tables_length">
                <label>
                ${uiLabelMap.displayPage}
                    <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                            onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                        <option value="10" <#if viewSize==10>selected</#if>>10</option>
                        <option value="20" <#if viewSize==20>selected</#if>>20</option>
                        <option value="30" <#if viewSize==30>selected</#if>>30</option>
                        <option value="40" <#if viewSize==40>selected</#if>>40</option>
                    </select>
                ${uiLabelMap.brandItem}
                </label>
            </div>
        </div> <!-- 列表当前分页条数end -->
    </div><!-- 工具栏end -->

        <div class="row">
            <div class="col-sm-12">
                <table id="example2" class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr>
                        <!-- 是否有更新权限-->
                        <#if security.hasEntityPermission("CONTENT_ARTICLEAPPROVAL", "_UPDATE", session)>
                            <th><input type="checkbox" class="js-allcheck"></th>
                        </#if>
                        <th>${uiLabelMap.articleTitle}</th>
                        <th>${uiLabelMap.articleType}</th>
                        <th>${uiLabelMap.articleAuthor}</th>
                        <th>${uiLabelMap.updateTime}</th>
                        <th>${uiLabelMap.Status}</th>
                        <!-- 是否有更新权限-->
                        <#if security.hasEntityPermission("CONTENT_ARTICLEAPPROVAL", "_UPDATE", session)>
                            <th>${uiLabelMap.Operation}</th>
                        </#if>
                    </tr>
                    </thead>
                    <tbody>
                        <#list articleList as articlelist>
                        <tr>
                            <!-- 是否有更新权限-->
                            <#if security.hasEntityPermission("CONTENT_ARTICLEAPPROVAL", "_UPDATE", session)>
                                <td><input type="checkbox" class="js-checkchild" value="${articlelist.articleId}"></td>
                            </#if>
                            <td>${articlelist.articleTitle?if_exists}</td>
                            <td>${articlelist.description?if_exists}</td>
                            <td>${articlelist.articleAuthor?if_exists}</td>
                            <td>${articlelist.createdStamp?string("yyyy-MM-dd HH:mm:ss")?if_exists}</td>
                            <td>
                                <#if articlelist.articleStatus=='1'>
                                    待审核
                                <#elseif articlelist.articleStatus=='0'>
                                    已保存
                                <#elseif articlelist.articleStatus=='2'>
                                    已审核
                                <#elseif articlelist.articleStatus=='3'>
                                    已拒绝
                                </#if>
                            </td>
                            <!-- 是否有更新权限-->
                            <#if security.hasEntityPermission("CONTENT_ARTICLEAPPROVAL", "_UPDATE", session)>
                                <td>
                                    <div class="btn-group">
                                        <input type="hidden" name="articleId" value="${articlelist.articleId}"/>
                                        <button type="button"
                                                class=" gss_btn btn btn-danger btn-sm">${uiLabelMap.Approval}</button>
                                        <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                                data-toggle="dropdown">
                                            <span class="caret"></span>
                                            <span class="sr-only">Toggle Dropdown</span>
                                        </button>
                                        <ul class="dropdown-menu" role="menu">
                                            <li><a href="javascript:detail(${articlelist.articleId})">查看详情</a></li>
                                        </ul>
                                    </div>
                                </td>
                            </#if>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(articleListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", articleListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=articleListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
        <!-- 分页条end -->
    <#else>
        <div id="noData" class="col-sm-12">
            <h3>${uiLabelMap.ArticleNoData}</h3>
        </div>
    </#if>
    </div>
    <!-- /.box-body -->
</div>


<!-- 审核弹出框start -->
<div id="modal_add" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">文章审核</h4>
            </div>
            <div class="modal-body">
                <form id="ApprovalForm" method="post" class="form-horizontal"
                      action="<@ofbizUrl>approvalArticle</@ofbizUrl>">
                    <div class="form-group">
                        <label class="control-label col-sm-2">文章标题:</label>
                        <div class="col-sm-8">
                            <input type="text" class="form-control" id="articleTitle" name="articleTitle" readonly>
                            <input type="hidden" class="form-control" id="articleId" name="articleId">
                        </div>
                    </div>

                    <div class="row">
                        <div class="form-group" data-type="minCheck" data-number="1" data-mark="操作">
                            <label class="control-label col-sm-2">操作:</label>
                            <div class="col-sm-10">
                                <div class="radio">
                                    <label class="col-sm-4"><input name="articleStatus" type="radio" value="2" checked
                                                                   class="radioItem">通过</label>
                                    <label class="col-sm-4"><input name="articleStatus" type="radio" value="3"
                                                                   class="radioItem">拒绝</label>
                                    <div class="dp-error-msg"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!--审核意见Start-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="审核意见">
                            <label for="title" class="col-sm-2 control-label" id="label_gss">审核意见:</label>
                            <div class="col-sm-8">
                                <textarea id="articleOpinion" class="form-control " name="articleOpinion"
                                          class="form-control" rows="3" style="resize: none;"></textarea>
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>
                    <!--审核意见end-->
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default"
                        data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
        </form>
    </div>
</div><!--审核弹出框start -->

<!-- 批量通过弹出框start -->
<div id="modal_approval" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">文章审核-批量通过</h4>
            </div>
            <div class="modal-body">
                <form id="Approval_Form" method="post" class="form-horizontal" role="form" action="">

                </form>
            </div>
            <div class="modal-footer">
                <button id="save_approval" type="button" class="btn btn-primary">确定</button>
                <button id="cancel" type="button" class="btn btn-default"
                        data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 批量通过弹出框end -->

<!-- 批量拒绝弹出框start  -->
<div id="modal_resolute" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">文章审核-批量拒绝</h4>
            </div>
            <div class="modal-body">
                <form id="ResoluteForm" method="post" class="form-horizontal" role="form" action="">

                </form>
            </div>
            <div class="modal-footer">
                <button id="save_resolute" type="button" class="btn btn-primary">确定</button>
                <button id="cancel" type="button" class="btn btn-default"
                        data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 批量通过弹出框end -->

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog"
     aria-labelledby="modal_msg_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">${uiLabelMap.FacilityOptionMsg}</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary"
                        data-dismiss="modal">${uiLabelMap.FacilityOk}</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->


<!-- 查看详情 -->
<div class="modal fade" id="detail_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title" id="exampleModalLabel">文章详情</h4>
            </div>
            <div class="modal-body">
                <form class="form-horizontal" id="detailForm" method="" action="">
                    <div class="box-body">
                        <!--文章标题 start-->
                        <div class="row">
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.articleTitle}">
                                <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleTitle}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" id="articleTitle" name="articleTitle"
                                           value="">
                                </div>
                            </div>
                        </div>
                        <!--文章标题 end-->
                        <!--文章标签 start-->
                        <div class="row">
                            <div class="form-group" data-number="1">
                                <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleTag}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" id="articleTag" name="articleTag"
                                           value="">
                                </div>
                            </div>
                        </div>
                        <!--文章标签 end-->
                        <!--文章配图 start-->
                        <div class="row">
                            <div class="form-group">
                                <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleFigure}:</label>
                                <div class="col-sm-10">
                                    <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                                    <!-- <#if articleContentImg?has_content> <#assign
                                    src='/content/control/getImage?contentId='> <#if
                                    articleContentImg.contentId?has_content> <#assign imgsrc = src
                                        +articleContentImg.contentId/> <img height="50" alt=""
									src="${imgsrc}" id="img" style="height:100px;width:100px;">
								<#else> <img height="50" alt="" src="" id="img"
									style="height:100px;width:100px;"> </#if> <input
									type="hidden" class="form-control dp-vd w-p50" id="contentId"
									name="contentId"
									value="${articleContentImg.contentId?if_exists}">
								<#else> <img height="50" alt="" src="" id="img"
									style="height:100px;width:100px;"> <input type="hidden"
									class="form-control dp-vd w-p50" id="contentId"
									name="contentId"> </#if> <input
									style="margin-left:5px;" type="button" id=""
									name="uploadedFile" onclick="imageManage()" value="选择图片" /> -->
                                </div>
                            </div>
                        </div>
                        <!--文章配图 end-->

                        <!--原文链接Start-->
                        <div class="row">
                            <div class="form-group">
                                <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleLink}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" id="articleLink" name="articleLink"
                                           value="">
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                        <!--原文链接end-->
                        <!--作者Start-->
                        <div class="row">
                            <div class="form-group">
                                <label for="title" class="col-sm-2 control-label">${uiLabelMap.Author}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" id="articleAuthor"
                                           name="articleAuthor" value="">
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                        <!--作者end-->

                        <!--所属分类 Start-->
                        <div class="row">
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.allArticleType}">
                                <label for="title" class="col-sm-2 control-label">${uiLabelMap.allArticleType}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control dp-vd" id="treeName" name="treeName" value=""
                                           onclick="showEditMenu();" readonly/>
                                    <input type="hidden" class="form-control dp-vd" name="articleTypeId"
                                           id="articleTypeId" value="">
                                    <div id="EditContent" class="menuContent"
                                         style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:196px;">
                                        <ul id="editTree" class="ztree" style="margin-top: 0; width: 110px;">
                                        </ul>
                                    </div>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                        <!--所属分类 end-->
                        <!--关键字start-->
                        <#--<div class="row">-->
                            <#--<div class="form-group">-->
                                <#--<label for="title" class="col-sm-2 control-label">${uiLabelMap.keyWord}:</label>-->
                                <#--<div class="col-sm-10">-->
                                    <#--<input type="text" class="form-control dp-vd" id="articleKeyword"-->
                                           <#--name="articleKeyword" value="">-->
                                    <#--<p class="dp-error-msg"></p>-->
                                <#--</div>-->
                            <#--</div>-->
                        <#--</div>-->
                        <!--关键字end-->
                        <!--文章内容 Start-->
                        <div class="row">
                            <div class="form-group">
                                <label for="recipient-name" class="control-label col-sm-2">${uiLabelMap.articleContent}
                                    :</label>
                                <div class="col-sm-10">
									<textarea id="articleCentent" name="articleCentent" value="">
				                    </textarea>
                                    <p class="dp-error-msg"></p>
                                </div>
                            </div>
                        </div>
                        <!--文章内容 end-->
                        <div class="form-grou " style="TEXT-ALIGN: center;">
                            <button type="button" class="btn btn-primary m-l-20" data-dismiss="modal">取消</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<script>

    $(function () {
        $.ajax({
            url: "getAllArticleType",
            type: "GET",
            dataType: "json",
            success: function (data) {
                $.fn.zTree.init($("#treeDemo"), setting, data.articleTypeList);
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    });

    var setting = {
        view: {
            selectedMulti: false //是否允许多选
        },
        data: {
            simpleData: {
                enable: true
            }
        },
        callback: {
            //zTree节点的点击事件
            onClick: onClick,
        }
    };

    //显示树
    function showMenu() {
        $("#menuContent").toggle();
        return false;
    }

    //隐藏树
    $(document).on('click', function (e) {
        if ($(e.target).is('#searchForm #treeName')) return;
        if ($(e.target).closest('div').is('#menuContent')) {
            if ($(e.target).closest('a').is("[id$='_a']")) {
                $("#menuContent").hide();
                return false;
            }
            else return;
        }
        else {
            $("#menuContent").hide();
        }
    })
    //点击某个节点 然后将该节点的名称赋值值文本框
    function onClick(e, treeId, treeNode) {
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        //获得选中的节点
        var nodes = zTree.getSelectedNodes(),
                v = "";
        id = "";
        //根据id排序
        nodes.sort(function compare(a, b) {
            return a.id - b.id;
        });
        for (var i = 0, l = nodes.length; i < l; i++) {
            v += nodes[i].name + ",";
            id += nodes[i].id + ",";
        }
        //将选中节点的名称显示在文本框内
        if (v.length > 0) v = v.substring(0, v.length - 1);
        if (id.length > 0) id = id.substring(0, id.length - 1);
        $("#searchForm #treeName").attr("value", v);
        $("#searchForm #articleTypeId").attr("value", id);
        //隐藏zTree
        return false;
    }

    // 查看文章详情
    function detail(articleId) {
        // 获取文章
        $.ajax({
            url: "<@ofbizUrl>getArticleDetail</@ofbizUrl>",
            type: "post",
            data: {articleId: articleId},
            dataType: "JSON",
            success: function (data) {
                console.log(JSON.stringify(data));
                $("#detail_Modal input[name=articleTitle]").val(data.article.articleTitle);
                // 遍历文章标签
                var tag = "";
                if (data.articleTagList != "" && data.articleTagList != null && data.articleTagList != undefined) {
                    $.each(data.articleTagList, function (index, item) {
                        tag += item.tagName + "  ";
                    });
                }
                $("#detail_Modal input[name=articleTag]").val(tag);
                var src = "";
                if(data.articleContentImg) {
                	src = "/content/control/getImage?contentId=" + data.articleContentImg;
                } else {
                	src = "/images/datasource/default/default_img.png";
                }
                $("#img").attr("src", src);
                $("#detail_Modal input[name=articleLink]").val(data.article.articleLink);
                $("#detail_Modal input[name=articleAuthor]").val(data.article.articleAuthor);
                $("#detail_Modal input[name=treeName]").val(data.articleTypeName);
                $("#detail_Modal input[name=articleKeyword]").val(data.articleKeyword);
                CKEDITOR.replace("articleCentent");
                var content = data.articleContents;
                if (content) {
                    CKEDITOR.instances.articleCentent.setData(content);
                }

            }, error: function (data) {
            }
        });
        $("#detail_Modal").modal("show");
    }

    //单个审核点击事件
    $('.gss_btn').click(function () {
        var tr = $(this).closest('tr');
        var articleTitle = tr.find('td').eq(1).text();//
        var articleId = $(this).parent().find('input[name=articleId]').val();
        $('#modal_add').find('input[name=articleId]').val(articleId)
        $('#modal_add').find('input[name=articleTitle]').val(articleTitle)
        //设置提示弹出框内容
        $('#modal_add').modal();
    });

    //单个保存审核按钮点击事件
    $('#save').click(function () {
        var chkOpinionInfo=$('#ApprovalForm #articleOpinion').val();

        if(chkOpinionInfo.length>100){
            $.tipLayer("审核意见不能大于100位");
            return false;
        }

        $('#ApprovalForm').submit();
    });

    //编辑表单校验
    $('#ApprovalForm').dpValidate({
        validate: true,
        callback: function () {
            $.ajax({
                url: "approvalArticle",
                type: "POST",
                data: $('#ApprovalForm').serialize(),
                dataType: "json",
                success: function (data) {
                    //隐藏新增弹出窗口
                    $('#modal_add').modal('toggle');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.reload();
                    })
                },
                error: function (data) {
                    //隐藏新增弹出窗口
                    $('#modal_add').modal('toggle');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
                    $('#modal_msg').modal();
                }
            });
        }
    });

    //批量通过按钮点击事件
    $('#btn_approval').click(function () {
        var checks = $('.js-checkparent .js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            article_ids = "";
            //编辑id字符串
            checks.each(function () {
                article_ids += $(this).val() + ",";
            });
            var c = '';
            $.ajax({
                url: "findArticlebyID",
                type: "POST",
                data: {article_ids: article_ids},
                dataType: "json",
                success: function (data) {
                    for (var i = 0; i < data.articleList.length; i++) {
                        c += '<div class="form-group">'
                                + '<label class="control-label col-sm-2">文章标题:</label>'
                                + '<div class="col-sm-10">'
                                + ' <input type="text" class="form-control" id="articleTitle_' + data.articleList[i].articleId + '" name="articleTitle_' + data.articleList[i].articleId + '" value="' + data.articleList[i].articleTitle + '" readonly />'
                                + '</div>'
                                + '</div>'
                                + '<div class="form-group">'
                                + '<label class="control-label col-sm-2">审核意见:</label>'
                                + '<div class="col-sm-10">'
                                + '<textarea id="articleOpinion_' + data.articleList[i].articleId + '"  data-id="' + data.articleList[i].articleId + '" name="articleOpinion_' + data.articleList[i].articleId + '" class="form-control" rows="3" style="resize: none;"></textarea>'
                                + '</div>'
                                + '</div>'
                                + '<div class="cut-off-rule bg-gray">'
                                + '</div>'
                    }
                    $('#modal_approval .modal-body .form-horizontal').html(c);
                    $('#modal_approval').modal();
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                    $('#modal_msg').modal();
                }
            });
        } else {
            //设置提示弹出框内容
            $('#modal_msg #modal_msg_body').html
            ("${uiLabelMap.MustSelectOne}");
            $('#modal_msg').modal();
        }
    });

    //批量通过提交按钮点击事件
    $('#save_approval').click(function () {
        var textarea = $('#modal_approval').find('textarea');
        var obj = '';
        var chkFlg="OK";
        textarea.each(function () {
            var id = $(this).data("id"),
                    value = $(this).val();

            if(value.length>100){
                chkFlg="NG";
                return false
            }
            obj += id + ':' + value + ',';
        });
        if(chkFlg=="NG"){
            $.tipLayer("审核意见不能大于100位");
            return false;
        }

        console.log(obj);
        $.ajax({
            url: "batchArticle",
            type: "POST",
            data: {
                obj: obj,
                articleStatus: '2'
            },
            dataType: "json",
            success: function (data) {
                //隐藏新增弹出窗口
                $('#modal_approval').modal('toggle');
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                $('#modal_msg').modal();
                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                $('#modal_msg').off('hide.bs.modal');
                $('#modal_msg').on('hide.bs.modal', function () {
                    window.location.reload();
                })
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    });


    //批量拒绝按钮点击事件
    $('#btn_resolute').click(function () {
        var checks = $('.js-checkparent .js-checkchild:checked');
        //判断是否选中记录
        if (checks.size() > 0) {
            article_ids = "";
            //编辑id字符串
            checks.each(function () {
                article_ids += $(this).val() + ",";
            });
            var c = '';
            $.ajax({
                url: "findArticlebyID",
                type: "POST",
                data: {article_ids: article_ids},
                dataType: "json",
                success: function (data) {
                    for (var i = 0; i < data.articleList.length; i++) {
                        c += '<div class="row">'
                                + '<div class="form-group">'
                                + '<label class="control-label col-sm-2">文章标题:</label>'
                                + '<div class="col-sm-8">'
                                + '<input type="text" class="form-control" id="articleTitle_' + data.articleList[i].articleId + '" name="articleTitle_' + data.articleList[i].articleId + '" value="' + data.articleList[i].articleTitle + '" readonly />'
                                + '</div>'
                                + '</div>'
                                + '</div>'
                                + '<div class="row">'
                                + '<div class="form-group" data-type="required" data-mark="审核意见">'
                                + '<label class="control-label col-sm-2"><i class="required-mark">*</i>审核意见:</label>'
                                + '<div class="col-sm-8">'
                                + '<textarea id="articleOpinion_' + data.articleList[i].articleId + '"  data-id="' + data.articleList[i].articleId + '" name="articleOpinion_' + data.articleList[i].articleId + '" class="form-control dp-vd" rows="3" style="resize: none;"></textarea>'
                                + '<p class="dp-error-msg"></p>'
                                + '</div>'
                                + '</div>'
                                + '</div>'
                                + '<div class="cut-off-rule bg-gray">'
                                + '</div>'
                    }
                    $('#modal_resolute .modal-body .form-horizontal').html(c);
                    $('#modal_resolute').modal();
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                    $('#modal_msg').modal();
                }
            });

        } else {
            //设置提示弹出框内容
            $('#modal_msg #modal_msg_body').html
            ("${uiLabelMap.MustSelectOne}");
            $('#modal_msg').modal();
        }
    });

    //批量拒绝提交按钮点击事件
    $('#save_resolute').click(function () {
        $('#ResoluteForm').dpValidate({
            clear: true
        });
        $('#ResoluteForm').submit();
    });
    //表单校验
    $('#ResoluteForm').dpValidate({
        validate: true,
        callback: function () {
            var textarea = $('#modal_resolute').find('textarea');
            var obj = '';
            textarea.each(function () {
                var id = $(this).data("id"),
                        value = $(this).val();
                obj += id + ':' + value + ',';
            });
            console.log(obj);
            $.ajax({
                url: "batchArticle",
                type: "POST",
                data: {
                    obj: obj,
                    articleStatus: '3'
                },
                dataType: "json",
                success: function (data) {
                    //隐藏新增弹出窗口
                    $('#modal_resolute').modal('toggle');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.reload();
                    })
                },
                error: function (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                    $('#modal_msg').modal();
                }
            });
        }
    });

    $(".radioItem").change(function () {
        var articleStatus = $("input[name='articleStatus']:checked").val();
        if (articleStatus == 3) {
            $('#label_gss').html('<i class="required-mark">*</i>审核意见:');
            $('#modal_add #articleOpinion').addClass('dp-vd');
        } else {
            $('#label_gss').html('审核意见:');
            $('#modal_add #articleOpinion').removeClass('dp-vd');
        }
    })
</script>