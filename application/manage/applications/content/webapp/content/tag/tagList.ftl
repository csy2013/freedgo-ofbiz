<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<!-- Main content -->
<#assign commonUrl = "TagList?"+ paramList+"&" />
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title m-t-10">${uiLabelMap.TagList}</h3>
        </div>

        <div class="box-body">
            <form class="form-inline clearfix"  id="searchForm" role="form" action="<@ofbizUrl>findTagList</@ofbizUrl>">
                <div class="form-group" style="position:relative;">
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">${uiLabelMap.TagType}</span>
                        <input type="text" class="form-control" id="treeName" placeholder="全部" <#if tagTypeName?has_content> value="${tagTypeName?if_exists}"</#if>onclick="showMenu()"   readonly="readonly"/ >
                        <input type="hidden" class="form-control" id="tagTypeId"  name="tagTypeId"  <#if tagTypeId?has_content> value="${tagTypeId?if_exists}"</#if>/>
                   <div id="menuContent" class="menuContent" style="display:none; position: absolute;top:33px;left:81px;border:1px solid #ccc;background:white;z-index:1000;width:196px;">
                 <ul id="treeDemo" class="ztree" style="margin-top: 0; width: 110px;">
                 </ul>
                    </div>
                  </div>
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">${uiLabelMap.TagName}</span>
                        <input type="text" class="form-control" placeholder="${uiLabelMap.TagName}" name="tagName" <#if tagName?has_content> value="${tagName?if_exists}"</#if>/>
                    </div>
                </div>
                <div class="input-group pull-right">
                <!--是否有查询的权限-->
                <#if security.hasEntityPermission("CONTENT_TAGLIST", "_VIEW", session)>
                    <button class="btn btn-success btn-flat">${uiLabelMap.Search}</button>
                </#if>
                </div>
            </form>
            
            <div class="cut-off-rule bg-gray"></div>
            <div class="btn-box m-b-10">
             <div class="col-sm-6">
        <div class="dp-tables_btn">
          <!--是否有新增的权限-->
          <#if security.hasEntityPermission("CONTENT_TAGLIST", "_CREATE", session)>
                <button id="btn_add" class="btn btn-primary" >
                    <i class="fa fa-plus"></i>${uiLabelMap.Add}
                </button>
          </#if>
          <!--是否有删除的权限-->
          <#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                <button class="btn btn-primary" id="btn_del">
                    <i class="fa fa-trash"></i> ${uiLabelMap.Delete}
                </button>
          </#if>   
                </div>
            </div><!-- 操作按钮组end -->
            <#if tagList?has_content>
                       
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
                           <!--是否有删除的权限-->
                           <#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                            <th><input type="checkbox" class="js-allcheck"></th>
                           </#if>
                            <th>${uiLabelMap.TagImg}</th>
                            <th>${uiLabelMap.TagType}</th>
                            <th>${uiLabelMap.TagName}</th>
                            <th>${uiLabelMap.Remark}</th>
                            <!--是否有更新的权限-->
                            <#if security.hasEntityPermission("CONTENT_TAGLIST", "_UPDATE", session)>
                            <th>${uiLabelMap.Operation}</th>
                            </#if>
                        </tr>
                        </thead>
                        <tbody>
                         <#list tagList as taglist>
                        <tr>
                            <!--是否有删除的权限-->
                           <#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                           <td><input type="checkbox" class="js-checkchild" value="${taglist.tagId?if_exists}"></td>
                           </#if>
                           
                            <td>
                            <#assign src='/content/control/getImage?contentId='>
                            <#if taglist.contentId?has_content>
                            <#assign imgsrc = src +taglist.contentId/>
                            <img height="50" alt="" src="${imgsrc}" id="img" style="height:100px;width:100px;">
                            </#if>
                            </td>
                            <td>${taglist.tagTypeName?if_exists}</td>
                            <td>${taglist.tagName?if_exists}</td>
                            <td>${taglist.tagRemark?if_exists}</td>
                            <!--是否有更新的权限-->
                            <#if security.hasEntityPermission("CONTENT_TAGLIST", "_UPDATE", session)>
                            <td>
                                <div class="btn-group">
                                    <button type="button" class=" btn-info btn btn-danger btn-sm">${uiLabelMap.Edit}</button>
                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle"
                                            data-toggle="dropdown">
                                        <span class="caret"></span>
                                        <span class="sr-only">Toggle Dropdown</span>
                                    </button>
                                    <!--是否有删除的权限-->
                                    <#if security.hasEntityPermission("CONTENT_TAGLIST", "_DELETE", session)>
                                    <ul class="dropdown-menu" role="menu">
                                        <li><a href="javascript:del(${taglist.tagId?if_exists})">${uiLabelMap.Delete}</a></li>
                                    </ul>
                                    </#if>
                                </div>
                                 <input type="hidden" name="tagId" value="${taglist.tagId?if_exists}">
                            </td>
                            </#if>
                        </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div><!-- 表格区域end -->
            <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(tagListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", tagListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=tagListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
       <!-- 分页条end -->
       <#else>
        <div id="noData" class="col-sm-12">
            <h3>${uiLabelMap.tagNoData}</h3>
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

       

 <!--添加弹出框 start-->
<div class="modal fade" id="add_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.TagEdit}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="AddForm" action="<@ofbizUrl>createTag</@ofbizUrl>" method="post">
             <div class="row">
                <div class="form-group" data-type="required" data-mark="${uiLabelMap.TagType}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TagType}:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd w-p50" id="treeName" name="treeName" onclick="addshowMenu()" readonly/>
                      <input type="hidden" class="form-control dp-vd w-p50" id="tagTypeId" name="tagTypeId">
                         <div id="Addmenu" style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:248px;">
                          <ul id="addtree" class="ztree" style="margin-top: 0; width: 110px;">
                          </ul>
                         </div>
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
          <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.TagName}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TagName}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="tagName" name="tagName">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
             <!-- 标签图片 start-->
           <div class="row">
                <div class="form-group" >
                     <label for="title" class="col-sm-2 control-label">${uiLabelMap.TagImg}:</label>
                     <div class="col-sm-10">
                     <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                     <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId">
                     <input style="margin-left:5px;" type="button" id="" name="uploadedFile"  onclick="imageManage()" value="选择图片"/>
                     </div>
                </div>
             </div>
              <!-- 标签图片 end-->
              <!-- 标签备注start-->
              <div class="row">
              <div class="form-group">
                        <label for="title" class="control-label col-sm-2">${uiLabelMap.Remark}:</label>
                        <div class="col-sm-10">
                            <textarea class="form-control w-p80" id="tagRemark" name="tagRemark"></textarea>
                        </div>
             </div>
             </div>
             <input type="hidden" class="form-control dp-vd w-p50" id="isDel" name="isDel" value="N">
             <!-- 标签备注 end-->
          <div class="modal-footer">
		    <button type="button"  id="btn_save" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--添加弹出框 end-->

 <!--编辑弹出框 start-->
<div class="modal fade" id="edit_Modal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel1">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.TagEdit}</h4>
          </div>
          <div class="modal-body">
            <form class="form-horizontal" id="EditForm" action="<@ofbizUrl>updateTag</@ofbizUrl>" method="post">
             <div class="row">
                <div class="form-group" data-type="required" data-mark="${uiLabelMap.TagType}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TagType}:</label>
                   <div class="col-sm-10">
                       <input type="text" class="form-control dp-vd w-p50" id="treeName" name="treeName" onclick="EditshowMenu()" readonly/>
                      <input type="hidden" class="form-control dp-vd w-p50" id="tagTypeId" name="tagTypeId">
                      <input type="hidden" class="form-control dp-vd w-p50" id="tagId" name="tagId">
                         <div id="Editmenu" style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:248px;">
                          <ul id="edittree" class="ztree" style="margin-top: 0; width: 110px;">
                          </ul>
                         </div>
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
         
         <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.TagName}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TagName}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50" id="tagName" name="tagName">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
           </div>
             <!-- 标签图片 start-->
           <div class="row">
                <div class="form-group" >
                     <label for="title" class="col-sm-2 control-label">${uiLabelMap.TagImg}:</label>
                     <div class="col-sm-10">
                     <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                     <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId">
                     <input style="margin-left:5px;" type="button" id="" name="uploadedFile"  onclick="imageManage()" value="选择图片"/>
                     </div>
                </div>
             </div>
              <!-- 标签图片 end-->
              <!-- 标签备注start-->
              <div class="row">
              <div class="form-group">
                        <label for="title" class="control-label col-sm-2">${uiLabelMap.Remark}:</label>
                        <div class="col-sm-10">
                            <textarea class="form-control w-p80" id="tagRemark" name="tagRemark"></textarea>
                        </div>
             </div>
             </div>
             <!-- 标签备注 end-->
          <div class="modal-footer">
		    <button type="button"  id="btn_edit" class="btn btn-primary">保存</button>
            <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>          
          </div>
          </form>
        </div>
      </div>
    </div>
    </div>
   <!--编辑弹出框 end-->




<script>
        //判断是新增还是修改
        var form_operation="";
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
                        
		var setting1 = {
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
                   onClick: onAddClick,
                        }
                        };
		var setting2 = {
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
                        onClick: onEditClick
                        }
                        };
		var del_ids = "";
		$(function(){
       //添加按钮点击事件
        $('#btn_add').click(function(){
        form_operation='add';
        $("#add_Modal #tagTypeId").val('');
        $("#add_Modal #tagName").val('');
        $("#add_Modal #uploadedFile").attr("src", '');
        $("#add_Modal #tagRemark").val('');
        $("#add_Modal #treeName").val('');
	    $('#add_Modal').modal('show');
	    });
	    
	     //编辑按钮点击事件
	    $(".btn-info").click(function(){
	    var tagId=$(this).parent().parent().find('input[name=tagId]').val();
        doSearchTag(tagId);
        form_operation='edit';
        $('#edit_Modal').modal('show');
        });
	    
	    function doSearchTag(id) {
        $.ajax({
            type: 'post',
            url: '<@ofbizUrl>queryTag</@ofbizUrl>',
            data: {tagId: id},
            success: function (data) {
                $("#edit_Modal #tagId").val(data.tag.tagId);
                $("#edit_Modal #treeName").val(data.treeName);
                $("#edit_Modal #tagName").val(data.tag.tagName);
                $("#edit_Modal #tagTypeId").val(data.tag.tagTypeId);
                $("#edit_Modal #tagRemark").val(data.tag.tagRemark);
                $("#edit_Modal #contentId").val(data.tag.contentId);
                if(data.tag.contentId!==null){
                var contentId="/content/control/getImage?contentId="+data.tag.contentId;
                $('#edit_Modal #img').attr('src',contentId);
                }
            }
        });
      }
	   
	    //添加关闭事件
		$('#add_Modal').on('hide.bs.modal', function () {
		  $('#AddForm').dpValidate({
		  	clear: true
		  });
		})
      //添加提交按钮点击事件
	    $('#btn_save').click(function(){
	        $('#AddForm').dpValidate({
			  clear: true
			});
			$('#AddForm').submit();
	    });
       //编辑表单校验
       $('#AddForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "createTag",
					type: "POST",
					data: $('#AddForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#add_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>TagList</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#add_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
        
         //编辑弹框关闭事件
		$('#edit_Modal').on('hide.bs.modal', function () {
		  $('#EditForm').dpValidate({
		  	clear: true
		  });
		})
      //编辑提交按钮点击事件
	    $('#btn_edit').click(function(){
	        $('#EditForm').dpValidate({
			  clear: true
			});
			$('#EditForm').submit();
	    });
       //编辑表单校验
       $('#EditForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "updateTag",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal('show');
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>TagList</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#edit_Modal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
})

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
				$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.DeleteTag}");
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
				url: "deleteTag",
				type: "GET",
				data: {deleteId : del_ids},
				dataType : "json",
				success: function(data){
				//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
    				$('#modal_msg').modal();
    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
    				$('#modal_msg').off('hide.bs.modal');
    				$('#modal_msg').on('hide.bs.modal', function () {
					  window.location.reload();
					})
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
				$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.DeleteThisTag}");
				$('#modal_confirm').modal('show');
		}

		$(function(){
		       $.ajax({
						url: "getAllTagType",
						type: "GET",
						dataType : "json",
						success: function(data){
							$.fn.zTree.init($("#treeDemo"), setting, data.tagTypeList);
						},
						error: function(data){
							//设置提示弹出框内容
							$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
		    				$('#modal_msg').modal();
						}
					});
		})
		
			$(function(){
			  $.chooseImage.int({
                serverChooseNum: 5,
                getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
                submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
                submitServerImgUrl: '',
                submitNetworkImgUrl: ''
            });
            
           $('body').on('click','.img-submit-btn',function(){
               var obj = $.chooseImage.getImgData();
               $.chooseImage.choose(obj,function(data){
               var contentId="/content/control/getImage?contentId="+data.uploadedFile0;
                 if(form_operation=='add'){
                  $('#add_Modal #img').attr('src',contentId);
                  $('#add_Modal #contentId').val(data.uploadedFile0);
                 }else{
                  $('#edit_Modal #img').attr('src',contentId);
                  $('#edit_Modal #contentId').val(data.uploadedFile0);
                 }
               })
             });
  
  $.ajax({
				url: "getTagTypeList",
				type: "GET",
				dataType : "json",
				success: function(data){
					$.fn.zTree.init($("#addtree"), setting1, data.tagTypeList);
					$.fn.zTree.init($("#edittree"), setting2, data.tagTypeList);
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
    				$('#modal_msg').modal();
				}
			});
})


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
		$("#searchForm #tagTypeId").attr("value", id);
		//隐藏zTree
		return false;
		}
		//添加显示树
		function addshowMenu() {
		$("#Addmenu").toggle();
		   return false;
		}
		
		//添加隐藏树
		$(document).on('click',function(e){
		    if($(e.target).is('#AddForm #treeName')) return;
		    if($(e.target).closest('div').is('#Addmenu')) {
		    	if($(e.target).closest('a').is("[id$='_a']"))
		    	{
		    		$("#Addmenu").hide();return false;}
		    	else return;
		    }
		    else{$("#Addmenu").hide();}
		})
		
		//点击某个节点 然后将该节点的名称赋值值文本框
		function onAddClick(e, treeId, treeNode) {
		var zTree = $.fn.zTree.getZTreeObj("addtree");
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
		$("#AddForm #treeName").attr("value", v);
		$("#AddForm #tagTypeId").attr("value", id);
		return false;
		}
		//编辑显示树
		function EditshowMenu() {
		$("#Editmenu").toggle();
		   return false;
		}
		//编辑隐藏树
		$(document).on('click',function(e){
		    if($(e.target).is('#EditForm #treeName')) return;
		    if($(e.target).closest('div').is('#Editmenu')) {
		    	if($(e.target).closest('a').is("[id$='_a']"))
		    	{
		    		$("#Editmenu").hide();return false;}
		    	else return;
		    }
		    else{$("#Editmenu").hide();}
		})
		//点击某个节点 然后将该节点的名称赋值值文本框
		function onEditClick(e, treeId, treeNode) {
		var zTree = $.fn.zTree.getZTreeObj("edittree");
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
		$("#EditForm #treeName").attr("value", v);
		$("#EditForm #tagTypeId").attr("value", id);
		return false;
		}
		 function imageManage() {
                    $.chooseImage.show()
                }
</script>