<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<!-- Main content -->
<!-- 内容start -->
<#assign commonUrl = "articleList?"+ paramList+"&" />
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">${uiLabelMap.articleList}</h3>
        </div>
        <div class="box-body">
          <!-- 条件查询start -->
            <form class="form-inline clearfix" id="searchForm" role="form" action="<@ofbizUrl>articleList</@ofbizUrl>">
                <div class="form-group">
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">${uiLabelMap.articleType}</span>
                        <input type="text" class="form-control" id="treeName" placeholder="全部" <#if description?has_content> value="${description?if_exists}"</#if> onclick="showMenu()"   readonly="readonly"/ >
                        <input type="hidden" class="form-control" id="articleTypeId"  name="articleTypeId"  <#if articleTypeId?has_content> value="${articleTypeId?if_exists}"</#if>/>
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
                        <input type="text" class="form-control" name="articleTitle"  <#if articleTitle?has_content> value="${articleTitle?if_exists}"</#if>placeholder="${uiLabelMap.articleTitle}">
                    </div>
                      <div class="input-group m-b-10">
                                    <span class="input-group-addon">${uiLabelMap.Status}</span>
                                    <select name="articleStatus" id="" class="form-control">
                                        <option value="-1" <#if articleStatus?has_content&&articleStatus=='-1'>selected</#if> selected>请选择</option>
                                        <option value="1" <#if articleStatus?has_content&&articleStatus=='1'>selected</#if>>待审核</option>
                                        <option value="2" <#if articleStatus?has_content&&articleStatus=='2'>selected</#if>>已审核</option>
                                        <option value="3" <#if articleStatus?has_content&&articleStatus=='3'>selected</#if>>已拒绝</option>
                                        <option value="0" <#if articleStatus?has_content&&articleStatus=='0'>selected</#if>>已保存</option>
                                    </select>
                       </div>
                </div>
                <div class="input-group pull-right">
                <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_VIEW", session)>
                    <button class="btn btn-success btn-flat">${uiLabelMap.Search}</button>
                </#if>
                </div>
            </form><!-- 条件查询end -->
            <!-- 分割线start -->
            <div class="cut-off-rule bg-gray"></div>
            <!-- 分割线end -->

            <!--工具栏start -->
            <!-- 操作按钮组start -->
            <div class="row m-b-10">
             <div class="col-sm-6">
            <div class="btn-box m-b-10">
                <!-- 是否有新增权限-->
                <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary" data-toggle="modal" data-target="#exampleModal">
                    <i class="fa fa-plus"></i>${uiLabelMap.Add}
                </button>
                </#if>
                 <!-- 是否有删除权限-->
                <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_DELETE", session)>
                <button class="btn btn-primary" id="btn_del">
                    <i class="fa fa-trash"></i> ${uiLabelMap.Delete}
                </button>
                </#if>
            </div>
            </div>
            <!-- 操作按钮组end -->
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
                </div><!-- 列表当前分页条数end -->
             </div><!-- 工具栏end -->
            <div class="row">
                <div class="col-sm-12">
                    <table id="example2" class="table table-bordered table-hover js-checkparent">
                        <thead>
                        <tr>
                         <!-- 是否有删除权限-->
                        <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_DELETE", session)>
                            <th><input type="checkbox" class="js-allcheck"></th>
                        </#if>
                            <th>${uiLabelMap.articleTitle}</th>
                            <th>${uiLabelMap.articleType}</th>
                            <th>${uiLabelMap.articleAuthor}</th>
                            <th>${uiLabelMap.updateTime}</th>
                            <th>${uiLabelMap.Status}</th>
                        <!-- 是否有更新权限-->
                        <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_UPDATE", session)>
                            <th>${uiLabelMap.Operation}</th>
                        </#if>

                        </tr>
                        </thead>
                        <tbody>
                        <#list articleList as articlelist>
                        <tr>
                         <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_DELETE", session)>
                            <td><input type="checkbox" class="js-checkchild"  value="${articlelist.articleId?if_exists}"></td>
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
                            <td>
                                <div class="btn-group">
                                <input type="hidden" name="articleId"  value="${articlelist.articleId?if_exists}">
                                    <button type="button" class="  btn btn-danger btn-sm " onclick="javascript:editArticle(${articlelist.articleId?if_exists})">编辑</button>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <ul class="dropdown-menu" role="menu">
                                    <!-- 是否有更新权限
                                     <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_UPDATE", session)>
                                     <li><a href="javascript:editArticle(${articlelist.articleId?if_exists})">编辑</a></li>
                                     </#if>-->
                                     <!-- 是否有更新权限-->
                                     <#if articlelist.articleStatus=='0'&&security.hasEntityPermission("CONTENT_ARTICLELIST", "_UPDATE", session)>
                                        <li><a href="javascript:editState(${articlelist.articleId?if_exists})">提交</a></li>
                                     </#if>

                                     <#if articlelist.articleStatus=='3'>
                                        <li><a href="javascript:reason(${articlelist.articleId?if_exists})">查看拒绝原因</a></li>
                                     </#if>
                                     <!-- 是否有删除权限-->
                                      <#if security.hasEntityPermission("CONTENT_ARTICLELIST", "_DELETE", session)>
                                        <li><a href="javascript:del(${articlelist.articleId?if_exists})">${uiLabelMap.Delete}</a></li>
                                      </#if>
                                    </ul>
                                </div>
                            </td>
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
       //添加按钮点击事件
	    $('#btn_add').click(function(){
			window.location.href='<@ofbizUrl>articleAdd</@ofbizUrl>';
	    });

       //编辑按钮点击事件
        function  editArticle(id){
        window.location.href='<@ofbizUrl>articleAdd</@ofbizUrl>?articleId='+id ;
        }
      //删除按钮点击事件
	    $('#btn_del').click(function(){
	    	var checks = $('.js-checkparent .js-checkchild:checked');
	    	//判断是否选中记录
	    	if(checks.size() > 0 ){
	    		del_ids = "";
		    	//编辑id字符串
	    		checks.each(function(){
					del_ids += $(this).val() + ",";
				});
				//设置删除弹出框内容
				$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.DeleteArticle}");
				$('#modal_confirm').modal('show');
	    	}else{
	    		//设置提示弹出框内容
	    		$('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
	    		$('#modal_msg').modal();
	    	}
	    });

	    //删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
			//异步调用删除方法
			$.ajax({
				url: "deleteArticle",
				type: "GET",
				data: {deleteId : del_ids},
				dataType : "json",
				success: function(data){
				if(data.status){
				//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
    				$('#modal_msg').modal();
    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
    				$('#modal_msg').off('hide.bs.modal');
    				$('#modal_msg').on('hide.bs.modal', function () {
					  window.location.reload();
					})
				  }
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
    				$('#modal_msg').modal();
				}
			});
	    });

	    	//行删除按钮事件
	    function del(id){
		del_ids = id;
		//设置删除弹出框内容
				$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.DeleteThisArticle}");
				$('#modal_confirm').modal('show');
		}

	    //保存状态变为待审核
	    function editState(id){
		 $.ajax({
				url: "approvalArticle",
				type: "GET",
				data: {
				      articleId : id,
				      articleStatus:'1'
				      },
				dataType : "json",
				success: function(data){
				if(data){
				//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
    				$('#modal_msg').modal();
    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
    				$('#modal_msg').off('hide.bs.modal');
    				$('#modal_msg').on('hide.bs.modal', function () {
					  window.location.reload();
					})
				  }
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
    				$('#modal_msg').modal();
				}
			});
		}
	    //保存状态变为待审核
	    function reason(id){
		 $.ajax({
				url: "findArticlebyID",
				type: "POST",
				data: {
				      article_ids : id
				      },
				dataType : "json",
				success: function(data){
				console.log(data.articleList[0].articleOpinion);
				if(data){
				    //设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html(data.articleList[0].articleOpinion);
    				$('#modal_msg #modal_msg_title').text('拒绝原因');
    				$('#modal_msg').modal();
				  }
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
    				$('#modal_msg').modal();
				}
			});
		}


		$(function(){
           $.ajax({
				url: "getAllArticleType",
				type: "GET",
				dataType : "json",
				success: function(data){
					$.fn.zTree.init($("#treeDemo"), setting, data.articleTypeList);
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
    				$('#modal_msg').modal();
				}
			});
         })

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
		$(document).on('click',function(e){
		    if($(e.target).is('#searchForm #treeName')) return;
		    if($(e.target).closest('div').is('#menuContent')) {
		    	if($(e.target).closest('a').is("[id$='_a']"))
		    	{
		    		$("#menuContent").hide();return false;}
		    	else return;
		    }
		    else{$("#menuContent").hide();}
		})
		//点击某个节点 然后将该节点的名称赋值值文本框
		function onClick(e, treeId, treeNode) {
		var zTree = $.fn.zTree.getZTreeObj("treeDemo");
		//获得选中的节点
		var nodes = zTree.getSelectedNodes(),
		v = "";
		id = "";
		//根据id排序
		nodes.sort(function compare(a, b) { return a.id - b.id; });
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
</script><!-- script区域end -->
