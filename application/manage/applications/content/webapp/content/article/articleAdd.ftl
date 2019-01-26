        <link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
        <script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
        <script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
        <script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
        <script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>
       <#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

        <#if article?has_content >
        <!-- Main content -->
          <div class="box box-info">
              <input type="hidden" id="linkId"/>
              <input type="hidden" id="selectName"/>
              <input type="hidden" id="isInner" name="isInner" value="${requestAttributes.isInner}"/>
            <div class="box-header with-border">
              <h3 class="box-title m-t-10">${uiLabelMap.AddOrUpdateArticle}</h3>
            </div>
           <form class="form-horizontal" id="editForm"   method="POST" action="<@ofbizUrl>updateArticle</@ofbizUrl>"  >
            <div class="box-body">
            <!--店铺编码-->
            <#--<input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>-->
            <input type="hidden" id="productIds" name="productIds" value=""/>
            <!--文章标题 start-->
              	<div class="row">
                     <div class="form-group col-sm-6" data-type="required,max" data-mark="${uiLabelMap.articleTitle}" data-number="100">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.articleTitle}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd" id="articleTitle" name="articleTitle" value="${article.articleTitle?if_exists}">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
             <!--文章标题 end--> 
             <!--文章标签 start-->
      	      <div class="row p-l-10 p-r-10">
                        <div class="form-group"  data-number="1">
                            <label class="col-sm-1 control-label">${uiLabelMap.articleTag}:</label>
                            <div class="col-sm-11">
                                <div class="checkbox clearfix">
                                  <input type="hidden"  name="tag" id="tag"/>
                                   <#if articleTagList?has_content>
                                   <#list articleTagList as taglist>
                                   <label>
                                   <input type="checkbox" name="tagId"  <#if typeIds?has_content> <#list typeIds as typeid>  <#if typeid==(taglist.tagId)>checked="checked"</#if></#list></#if> value="${taglist.tagId?if_exists}">
                                   ${taglist.tagName?if_exists}
                                    </label>
                                   </#list>
                                   </#if>
                                </div>
                                <div class="dp-error-msg"></div>
                            </div>
                        </div>
                   </div>
                <!--文章标签 end-->
                <!--文章配图 start-->
              <div class="row">
                <div class="form-group col-sm-6" >
                     <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleFigure}:</label>
                     <div class="col-sm-4">
                     <#if articleContentImg?has_content>
                      <#assign src='/content/control/getImage?contentId='>
	                    <#if articleContentImg.contentId?has_content>
	                    <#assign imgsrc = src +articleContentImg.contentId/>
	                    <img height="50" alt="" src="${imgsrc}" id="img" style="height:100px;width:100px;">
	                    <#else>
	                    <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
	                    </#if>
	                    <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId" value="${articleContentImg.contentId?if_exists}">
	                 <#else>
	                 <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                     <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId">
                     </#if>
                     
                     
                     <input style="margin-left:5px;" type="button" id="" name="uploadedFile"  onclick="imageManage()" value="选择图片"/>
                     </div>

                    <div class="col-sm-6">
                        <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 710*329px</div>
                    </div>
                </div>
             </div>
              <!--文章配图 end-->
              
              <!--原文链接Start-->
                <div class="row">
                     <div class="form-group col-sm-6" >
                           <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleLink}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd" id="articleLink" name="articleLink" value="${article.articleLink?if_exists}"> 
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
               <!--原文链接end-->
               <!--作者Start-->
                <div class="row">
                     <div class="form-group col-sm-6" >
                           <label for="title" class="col-sm-2 control-label">${uiLabelMap.Author}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd" id="articleAuthor" name="articleAuthor" value="${article.articleAuthor?if_exists}">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
                 <!--作者end-->
                 
                 <!--所属分类 Start-->
                <div class="row">
                     <div class="form-group col-sm-6" data-type="required" data-mark="${uiLabelMap.allArticleType}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.allArticleType}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd"  id="treeName" name="treeName" value="${articleTypeName?if_exists}" onclick="showEditMenu();" readonly/>
                               <input type="hidden" class="form-control dp-vd"  name="articleTypeId"  id="articleTypeId" value="${article.articleTypeId?if_exists}">
                                  <div id="EditContent" class="menuContent" style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:196px;">
                               <ul id="editTree" class="ztree" style="margin-top: 0; width: 110px;">
                               </ul>
                               </div>
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
                 <!--所属分类 end-->

                <!--选择商品 start-->
                <div class="row">
                    <div class="form-group col-sm-6" >
                        <label for="title" class="col-sm-2 control-label">选择商品:</label>
                        <div class="col-sm-10">
                            <button id="addProducts" type="button" class="btn btn-primary">
                                <i class="fa fa-plus">选择商品</i>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="form-group col-sm-12" >
                        <label for="title" class="col-sm-2 control-label"></label>
                        <div class="col-sm-10">
                            <div class="table-responsive no-padding">
                                <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                                    <thead>
                                    <tr>
                                        <th  width="10%">商品图片</th>
                                        <th  width="15%">商品规格</th>
                                        <th  width="10%">商品编码</th>
                                        <th  width="50%">商品名称</th>
                                        <th  width="15%">商品价格</th>
                                        <th width="10%"></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                <!--选择商品 end-->
                 <!--关键字start-->
                <#--<div class="row">-->
                     <#--<div class="form-group col-sm-6" >-->
                           <#--<label for="title" class="col-sm-2 control-label">${uiLabelMap.keyWord}:</label>-->
                            <#--<div class="col-sm-10">-->
                               <#--<input type="text" class="form-control dp-vd" id="articleKeyword" name="articleKeyword" value="${articleKeyword?if_exists}">-->
                                  <#--<p class="dp-error-msg"></p>-->
                            <#--</div>-->
                     <#--</div>-->
                 <#--</div>-->
                 <!--关键字end-->
                 
                 <!--文章状态-->
                <input type="hidden" class="form-control dp-vd"  name="articleStatus" value="${article.articleStatus?if_exists}">
                <!--文章ID-->
                <input type="hidden" class="form-control dp-vd"  name="articleId"  id="articleId" value="${article.articleId?if_exists}">
			  <!--文章内容 Start-->
			  <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="control-label col-sm-1">${uiLabelMap.articleContent}:</label>
                <div class="col-sm-10">
                 <div class="box-body pad w-p60" >
                           <#if articleContents?has_content>
                            <input type="hidden" id="content"  value="${articleContents.textData?if_exists}">
                              </#if>
                             <textarea id="articleCentent" name="articleCentent"  value="" >
                             </textarea>
                    <p class="dp-error-msg"></p>
                </div>
                </div>     
              </div>
              </div>
               <!--文章内容 end-->
                <div class="form-grou " style="TEXT-ALIGN: center;">
                <!--
        <button id="" type="button" class="btn btn-primary m-l-20">预览</button>-->
        <button id="edit_submit" type="button" class="btn btn-primary m-l-20" data-dismiss="modal">提交</button>
      </div>
      </form>
        </div><!-- /.box-body -->
          </div>
        <#else>
              <!-- Main content -->
          <div class="box box-info">
              <input type="hidden" id="linkId"/>
              <input type="hidden" id="selectName"/>
              <input type="hidden" id="isInner" name="isInner" value="${requestAttributes.isInner}"/>
            <div class="box-header with-border">
              <h3 class="box-title m-t-10">${uiLabelMap.AddOrUpdateArticle}</h3>
            </div>
           <form class="form-horizontal" id="addForm"   method="POST" action="<@ofbizUrl>createArticle</@ofbizUrl>" enctype="multipart/form-data" >

            <div class="box-body">
                <!--店铺编码-->
                <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
                <input type="hidden" id="productIds" name="productIds" value=""/>

             <!--文章标题 Start-->   
              	<div class="row">
                     <div class="form-group col-sm-6" data-type="required,max" data-mark="${uiLabelMap.articleTitle}" data-number="100">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.articleTitle}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd" id="articleTitle" name="articleTitle">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
                 <!--文章标题 end-->
                 <!--文章标签 start--> 
	  	      <div class="row p-l-10 p-r-10">
	                    <div class="form-group"  data-number="1">
	                        <label class="col-sm-1 control-label">${uiLabelMap.articleTag}:</label>
	                        <div class="col-sm-11">
	                            <div class="checkbox clearfix">
	                              <input type="hidden"  name="tag" id="tag"/>
	                               <#if articleTagList?has_content>
	                               <#list articleTagList as taglist>
	                               <label>
	                               <input type="checkbox" name="tagId"  value="${taglist.tagId?if_exists}">
	                               ${taglist.tagName?if_exists}
	                                </label>
	                               </#list>
	                               </#if>
	                            </div>
	                            <div class="dp-error-msg"></div>
	                        </div>
	                    </div>
	               </div>
	                <!--文章标签 end--> 
	             <!--文章配图 start--> 
                <div class="row">
                <div class="form-group col-sm-6" >
                     <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleFigure}:</label>
                     <div class="col-sm-4">
                     <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
                     <input type="hidden" class="form-control dp-vd w-p50" id="contentId" name="contentId">
                     <input style="margin-left:5px;" type="button" id="" name="uploadedFile"  onclick="imageManage()" value="选择图片"/>
                     </div>
                    <div class="col-sm-6">
                        <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 710*329px</div>
                    </div>
                </div>
             </div>
              <!--文章配图 end-->
              <!--原文连接 start-->
                <div class="row">
                     <div class="form-group col-sm-6" >
                           <label for="title" class="col-sm-2 control-label">${uiLabelMap.articleLink}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd" id="articleLink" name="articleLink"> 
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
                 <!--原文连接 end-->
                 <!--作者 start-->
                <div class="row">
                     <div class="form-group col-sm-6" >
                           <label for="title" class="col-sm-2 control-label">${uiLabelMap.Author}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd" id="articleAuthor" name="articleAuthor">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
                <!--作者 end-->
                <!--文章类型 start-->
                  <div class="row">
                  <div class="form-group col-sm-6" data-type="required" data-mark="${uiLabelMap.allArticleType}">
                   <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.allArticleType}:</label>
                   <div class="col-sm-10">
                      <input type="text" class="form-control dp-vd " id="treeName" name="treeName" onclick="showMenu()" readonly/>
                      <input type="hidden" class="form-control dp-vd w-p50" id="articleTypeId" name="articleTypeId">
                         <!-- zTree start-->
                               <div id="menuContent" class="menuContent" style="display:none; position: absolute;top:33px;left:15px;border:1px solid #ccc;background:white;z-index:1000;width:196px;">
                               <ul id="treeDemo" class="ztree" style="margin-top: 0; width: 110px;">
                               </ul>
                               </div>
                                <!-- zTree end-->
                         <p class="dp-error-msg"></p>
                   </div>
                </div>
          </div>
            <!--选择商品 start-->
            <div class="row">
                <div class="form-group col-sm-6" >
                    <label for="title" class="col-sm-2 control-label">选择商品:</label>
                    <div class="col-sm-10">
                        <button id="addProducts" type="button" class="btn btn-primary">
                            <i class="fa fa-plus">选择商品</i>
                        </button>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6" >
                    <label for="title" class="col-sm-2 control-label"></label>
                    <div class="col-sm-10">
                        <div class="table-responsive no-padding">
                            <table class="table table-hover js-checkparent js-sort-list addProducts" id="productTable">
                                <thead>
                                <tr>
                                    <th>商品图片</th>
                                    <th>商品规格</th>
                                    <th>商品编码</th>
                                    <th>商品名称</th>
                                    <th>商品价格</th>
                                    <th></th>
                                </tr>
                                </thead>
                                <tbody>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <!--选择商品 end-->
            <!--文章类型end-->
            <!--文章关键字start-->
                <#--<div class="row">-->
                     <#--<div class="form-group col-sm-6" >-->
                           <#--<label for="title" class="col-sm-2 control-label">${uiLabelMap.keyWord}:</label>-->
                            <#--<div class="col-sm-10">-->
                               <#--<input type="text" class="form-control dp-vd" id="articleKeyword" name="articleKeyword">-->
                                  <#--<p class="dp-error-msg"></p>-->
                            <#--</div>-->
                     <#--</div>-->
                 <#--</div>-->
                 <!--文章关键字end-->
                 <!--   文章状态start-->
                <input type="hidden" class="form-control dp-vd" id="articleStatus" name="articleStatus" value="0">
			  <!--   文章内容start-->
			  <div class="row">
			  <div class="form-group">
                <label for="recipient-name" class="control-label col-sm-1">${uiLabelMap.articleContent}:</label>
                <div class="col-sm-10">
                 <div class="box-body pad w-p60" >
                    <textarea id="articleCentent" name="articleCentent"  value="2" >
                    </textarea>
                    <p class="dp-error-msg"></p>
                </div>
                </div>     
              </div>
              </div>
              <!--文章内容end-->
                <div class="form-group" style="TEXT-ALIGN: center;">
                <!--
        <button id="article_save1" type="button" class="btn btn-primary m-l-20">预览</button>-->
        <button id="article_save" type="button" class="btn btn-primary m-l-20">保存</button>
        <button id="article_submit" type="button" class="btn btn-primary m-l-20" data-dismiss="modal">提交</button>
      </div>
      </form>
        </div><!-- /.box-body -->
          </div>
        </#if>
        
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



<script>
      //判断是修改还是新增
      var form_operation='';
      var curProductIds="";
      $(function () {
       CKEDITOR.replace("articleCentent");

       var isInner=$('#isInner').val();
          if(isInner=="N"){

              $("#addForm #treeName").attr("value", "其他文章");
              $("#addForm #articleTypeId").attr("value", "OTHER_ARTICLE");
              $("#addForm #treeName").attr("onclick", "");

              $("#editForm #treeName").attr("value", "其他文章");
              $("#editForm #articleTypeId").attr("value", "OTHER_ARTICLE");
              $("#editForm #treeName").attr("onclick", "");

          }
       var content = $("#content").val();
       if(content){
       CKEDITOR.instances.articleCentent.setData(content);
        }
        //保存按钮点击事件
	    $('#article_save').click(function(){
	    $("#articleCentent").val(CKEDITOR.instances.articleCentent.getData());
			$('#addForm').dpValidate({
			  clear: true
			});
			var tagIds=""
			$('input[name="tagId"]:checked').each(function(){ 
			    tagIds = tagIds + "," + $(this).val();
			}); 
			var Tags=tagIds.substr(1,tagIds.length);
			$('#addForm #tag').val(Tags);
			$('#addForm').submit();
	      
	    });
        //提交按钮点击事件
	    $('#article_submit').click(function(){
	        $('#articleStatus').val('1');
	        $("#articleCentent").val(CKEDITOR.instances.articleCentent.getData());
			$('#addForm').dpValidate({
			  clear: true
			});
			$('#addForm').submit();
	    });
	   //表单校验
       $('#addForm').dpValidate({
        validate: true,
        callback: function(){
       //异步调用新增方法
				$.ajax({
					url: "createArticle",
					type: "POST",
					data: $('#addForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal();
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>articleList</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
       });
       
        //编辑按钮点击事件
	    $('#edit_submit').click(function(){
	        $("#articleCentent").val(CKEDITOR.instances.articleCentent.getData());
			$('#editForm').dpValidate({
			  clear: true
			});
			var tagIds=""
			$('input[name="tagId"]:checked').each(function(){ 
			    tagIds = tagIds + "," + $(this).val();
			}); 
			var Tags=tagIds.substr(1,tagIds.length);
		
			$('#editForm #tag').val(Tags);
			$('#editForm').submit();
	    });
	   //编辑表单校验
       $('#editForm').dpValidate({
        validate: true,
        callback: function(){
       //异步调用新增方法
				$.ajax({
					url: "editArticle",
					type: "POST",
					data: $('#editForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal();
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>articleList</@ofbizUrl>';
						})
					},
					error: function(data){
						//隐藏新增弹出窗口
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
       });


          // 添加商品
          $('#addProducts').click(function () {
              $.dataSelectModal({
                  url: "/catalog/control/ProductListMultiModalPage${externalKeyParam}",
                  width: "800",
                  title: "选择商品",
                  selectId: "linkId",
                  selectName: "selectName",
                  multi: true,
                  selectCallBack: function (el) {
                      var productIds = el.data('id');
                      getProductGoodsInfoListByIds(productIds);
                  }
              });
          })
          //商品删除按钮事件
          $(document).on('click', '.js-button-assocgood', function () {
              var id=$(this).data("id");
              $(this).parent().parent().remove();
              curProductIds=updateProductIdsInfo(id);
              $("#productIds").val(curProductIds);
          })

          <#if article?has_content >
             <#--alert('${article.articleId}');-->
             var curArticleId='${article.articleId}';
              getProductInfo(curArticleId);
//             alert(curArticleId);
          </#if>
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

           var editsetting = {
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
                   onClick: EditClick,
                        }
                        };

          $(function(){
           $.ajax({
				url: "getArticleTypeList",
				type: "GET",
				dataType : "json",
				success: function(data){
					$.fn.zTree.init($("#treeDemo"), setting, data.articleTypeList);
					$.fn.zTree.init($("#editTree"), editsetting, data.articleTypeList);
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
		    if($(e.target).is('#addForm #treeName')) return;
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
		$("#addForm #treeName").attr("value", v);
		$("#addForm #articleTypeId").attr("value", id);
		//隐藏zTree
		return false;
		}
		         
		//显示树
		function showEditMenu() {
		$("#EditContent").toggle();
		  return false;
		}
		//隐藏树
		$(document).on('click',function(e){
		    if($(e.target).is('#editForm #treeName')) return;
		    if($(e.target).closest('div').is('#EditContent')) {
		    	if($(e.target).closest('a').is("[id$='_a']"))
		    	{
		    		$("#EditContent").hide();return false;}
		    	else return;
		    }
		    else{$("#EditContent").hide();}
		})
		
		//点击某个节点 然后将该节点的名称赋值值文本框
		function EditClick(e, treeId, treeNode) {
		var zTree = $.fn.zTree.getZTreeObj("editTree");
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
		$("#editForm #treeName").attr("value", v);
		$("#editForm #articleTypeId").attr("value", id);
		//隐藏zTree
		return false;
		}
		
		//图片上传控件
		$(function(){
			  $.chooseImage.int({
                serverChooseNum: 5,
                getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
                submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
                submitServerImgUrl: '',
                submitNetworkImgUrl: ''
                });
           //图片控件保存按钮
           $('body').on('click','.img-submit-btn',function(){
               var obj = $.chooseImage.getImgData();
               $.chooseImage.choose(obj,function(data){
               var contentId="/content/control/getImage?contentId="+data.uploadedFile0;
                 if(form_operation=='add'){
                  $('#addForm #img').attr('src',contentId);
                  $('#addForm #contentId').val(data.uploadedFile0);
                 }else{
                  $('#editForm #img').attr('src',contentId);
                  $('#editForm #contentId').val(data.uploadedFile0);
                 }
               })
             });
          })
           function imageManage() {
                  var  articleId =$('#editForm #articleId').val(); 
                  if(typeof(articleId) == "undefined"){
                  form_operation='add';
                  } 
                  $.chooseImage.show()
               }
      /**
       * 根据商品编码取得商品信息列表
       * @param ids
       */
      function getProductGoodsInfoListByIds(ids){
          $.ajax({
              url: "/catalog/control/getProductGoodsListByIds?externalLoginKey=${externalLoginKey}",
              type: "POST",
              data: {ids: ids},
              dataType: "json",
              success: function (data) {
                  var productGoodInfoList = data.productGoodInfoList;
                  for (var i=0;i<productGoodInfoList.length;i++) {
                      var productGoodInfo = productGoodInfoList[i];
                      var productInfo = productGoodInfo.productInfo;
                      var salesPrice = productGoodInfo.salesPrice;
                      var imgUrl=productGoodInfo.imgUrl;
                      var productGoodFeature = productGoodInfo.productGoodFeature;
                      var productId = productInfo.productId;
                      var productName = productInfo.productName;

                      if(chkProductIdIsSelected(productInfo.productId)){
                          var tr = '<tr id="' + productId + '">'
                                  + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                  + '<td>' + productGoodFeature + '</td>'
                                  + '<td>' + productId + '</td>'
                                  + '<td>' + productName + '</td>'
                                  + '<td>' +salesPrice+ '</td>'
                                  + '<td class="fc_td"><button type="button" data-id="'+productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                  + '</tr>';
                          $('#productTable>tbody').append(tr);

                          if(curProductIds==""){
                              curProductIds=productId;
                          }else{
                              curProductIds+=','+productId;
                          }
                          $("#productIds").val(curProductIds);
                      }

                  };
              },
              error: function (data) {
                  $.tipLayer("操作失败！");
              }
          });
      }

      /**
       * 验证商品编码是否使用
       * @param productId
       * @returns {number}
       */
      function chkProductIdIsSelected(productId){
          var chkFlg=1;
          var ids=curProductIds.split(",");
          for(var i=0;i<ids.length;i++){
              var curProductId=ids[i];
              if(curProductId==productId){
                  chkFlg=0;
                  return chkFlg;
              }
          }
          return chkFlg;
      }

      /**
       * 更新商品编码集合
       * @param productId
       * @returns {string}
       */
      function updateProductIdsInfo(productId){
          var ids=curProductIds.split(",");
          var newIds="";
          for(var i=0;i<ids.length;i++){
              var curProductId=ids[i];
              if(curProductId!=productId){
                  if(newIds==""){
                      newIds= curProductId
                  }else{
                      newIds=newIds+','+curProductId  ;
                  }
              }
          }
          return newIds
      }

      /**
       * 根据编码取得文章商品信息
       * @param productGrpId
       */
      function getProductInfo(articleId) {
          //文章关联商品信息的取得
          jQuery.ajax({
              url: '<@ofbizUrl>getArticleProduct</@ofbizUrl>',
              type: 'POST',
              data: {
                  'articleId':articleId
              },
              success: function(data){
                  if (data.productList) {
                      var product_List = data.productList;
                      $('#productTable>tbody').empty();
                      var tr1 = "";
                      for (var i = 0; i < product_List.length; i++) {
                          var curId=product_List[i].productId;
                          var tr = '<tr id="' + product_List[i].productId + '">'
                                  + '<td><img height="100" src="'+product_List[i].imgUrl+'" class="cssImgSmall" alt="" /></td>'
                                  + '<td>' + product_List[i].featureInfo+'</td>'
                                  + '<td>' + product_List[i].productId + '</td>'
                                  + '<td>' + product_List[i].productName + '</td>'
                                  + '<td>' + product_List[i].defaultprice+'</td>'
                                  + '<td class="fc_td"><button type="button" data-id="'+product_List[i].productId+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
                                  + '</tr>';
                          $('#productTable>tbody').append(tr);
                          if(i==0){
                              curProductIds=curId;
                          }else{
                              curProductIds+=','+curId;
                          }
                          $("#productIds").val(curProductIds);
                      }

                  }
              },
              error: function (data) {
                  //设置提示弹出框内容
                  $('#modal_msg #modal_msg_body').html("文章商品取得失败");
                  $('#modal_msg').modal();
              }
          });
      }

</script>