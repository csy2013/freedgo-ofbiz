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
 <!-- Main content -->
        <section class="content">
            <div class="box box-info">
                <div class="box-header with-border">
                    <h3 class="box-title m-t-10">${uiLabelMap.articleType}</h3>
                </div>
                <div class="box-body">
                    <div class="btn-box m-b-10">
                      <#--<!--是否有新增的权限&ndash;&gt;-->
                      <#--<#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_CREATE", session)>-->
                        <#--<button class="btn btn-primary" data-toggle="modal" data-num="1">${uiLabelMap.Add}</button>-->
                      <#--</#if>-->
                    </div>
                    <div class="row">
                        <div class="col-sm-12">
                            <input type="hidden" class="add_name">
                            <div id="example2" class="table table-bordered table-hover w-p50">
                                <div class="xl_head">
                                    <span>${uiLabelMap.TypeName}</span>
                                     <!--是否有的权限-->
                                    <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_UPDATE", session)>
                                    <span class="xl_mod">${uiLabelMap.Operation}</span>
                                     </#if>
                                </div>
                                <#if articleTypeList?has_content>
                                <#list articleTypeList as typelist>
                               <!--一级分类  start-->
                                        <div class="first_tr" data-first="1">
                                            <i class="glyphicon glyphicon-plus-sign xl_icon first_icon"></i><span class="classfy_name name1">${typelist.description?if_exists}</span>
                                            
                                            <span class="xl_write">
                                                <div class="btn-group">
                                                <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_UPDATE", session)>
                                                    <button type="button" class=" js-button btn btn-danger btn-sm xl_bj" data-toggle="modal">编辑</button>
                                                    <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
                                                        <span class="caret"></span>
                                                        <span class="sr-only">Toggle Dropdown</span>
                                                    </button>
                                                    <ul class="dropdown-menu" role="menu">
                                                    <#--<#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_DELETE", session) && typelist.articleTypeId != "NEWS" && typelist.articleTypeId != "ABOUT_US">-->
                                                        <#--<li><a href="javascript:del(${typelist.articleTypeId?if_exists})" class="gss_delete first_delete">删除</a></li>-->
                                                    <#--</#if>-->
                                                    <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_CREATE", session)>
                                                        <li><a href="" class="gss-button" data-toggle="modal">添加子分类</a></li>
                                                    </#if>
                                                    </ul>
                                                 </#if>
                                                </div>
                                                <input type="hidden" name="articleTypeId" value="${typelist.articleTypeId?if_exists}">
                                                <input type="hidden" name="typeLevel" value="${typelist.typeLevel?if_exists}">
                                            </span>
                                            <#assign articleTypelevel2 = delegator.findByAnd("ArticleType", {"parentTypeId" : typelist.articleTypeId?if_exists})>
                                            <#if articleTypelevel2?has_content>
                                            <#list articleTypelevel2 as articleType2>
                                            <!--子分类-->
                                            <div class="second_tr" data-second="2">
                                                <i class="glyphicon glyphicon-plus-sign xl_icon second_icon"></i><span class="second_td name1">${articleType2.description?if_exists}</span>
                                            <span class="second_td">
                                            <div class="btn-group second_btn_group">
                                            <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_UPDATE", session)>
                                            <button type="button" class=" js-button btn btn-danger btn-sm xl_bj" data-toggle="modal" >编辑</button>
                                            <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
                                                <span class="caret"></span>
                                                <span class="sr-only">Toggle Dropdown</span>
                                            </button>
                                            <input type="hidden" name="articleTypeId" value="${articleType2.articleTypeId?if_exists}">
                                            <input type="hidden" name="typeLevel" value="${articleType2.typeLevel?if_exists}">
                                            <ul class="dropdown-menu" role="menu">
                                            <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_DELETE", session)>
                                                <li><a href="javascript:del(${articleType2.articleTypeId?if_exists});" class="gss_delete second_delete">删除</a></li>
                                            </#if>
                                            <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_CREATE", session)>
                                                <li><a href="" class="gss-button1" data-toggle="modal">添加子分类</a></li>
                                            </#if>
                                            </ul>
                                            </#if>
                                            </div>
                                            </span>
                                            
                                             <#assign articleTypelevel3 = delegator.findByAnd("ArticleType", {"parentTypeId" : articleType2.articleTypeId?if_exists})>
                                             <#if articleTypelevel3?has_content>
                                            <#list articleTypelevel3 as articleType3>
                                               <!--三级分类-->
                                           <div class="third_tr"  data-third="3">
                                           <span class="third_td name1">${articleType3.description}</span>
                                            <span class="third_td">
                                            <div class="btn-group third_btn_group">
                                            <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_UPDATE", session)>
                                            <button type="button" class=" js-button btn btn-danger btn-sm xl_bj"  >编辑</button>
                                            <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
                                                <span class="caret"></span>
                                                <span class="sr-only">Toggle Dropdown</span>
                                            </button>
                                            <input type="hidden" name="articleTypeId" value="${articleType3.articleTypeId?if_exists}">
                                            <input type="hidden" name="typeLevel" value="${articleType3.typeLevel?if_exists}">
                                            <ul class="dropdown-menu" role="menu">
                                            <#if security.hasEntityPermission("CONTENT_ARTICLETYPE", "_DELETE", session)>
                                                <li><a href="javascript:del(${articleType3.articleTypeId?if_exists});" class="gss_delete third_delete">删除</a></li>
                                            </#if>
                                            </ul>
                                            </#if>
                                           </div>
                                          </span>
                                                </div>
                                            </#list>
                                            </#if>
                                            </div>
                                            </#list>
                                            </#if>
                                        </div>
                                         </#list>
                                </#if>
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </section><!-- /.content -->

<!-- 新增文章分类 start -->
        <div class="modal fade" id="exampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.TypeEdit}</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="addFirstForm" action="<@ofbizUrl>createArticleType</@ofbizUrl>" method="post" >
                     <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.TypeName}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TypeName}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50"  name="description">
                               <#--<input type="hidden" class="form-control dp-vd w-p50" id="articleTypeId" name="articleTypeId">-->
                                  <p class="dp-error-msg"></p>
                            </div>
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
       <!-- 新增文章分类 model结束-->
        
        <!-- 编辑文章分类 start -->
        <div class="modal fade" id="EditexampleModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.TypeEdit}</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal" id="EditForm" action="<@ofbizUrl>updateArticleType</@ofbizUrl>" method="post" >
                     
                     <div class="row">
                     <div class="form-group" data-type="required" data-mark="${uiLabelMap.TypeName}">
                           <label for="title" class="col-sm-2 control-label"><i class="required-mark">*</i>${uiLabelMap.TypeName}:</label>
                            <div class="col-sm-10">
                               <input type="text" class="form-control dp-vd w-p50"  name="description">
                               <input type="hidden" class="form-control dp-vd w-p50" id="articleTypeId" name="articleTypeId">
                                  <p class="dp-error-msg"></p>
                            </div>
                     </div>
                 </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary edit_btn_save">${uiLabelMap.Save}</button>
                        <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                    </div>
                    </form>
                </div>
            </div>
        </div>
            </div>
       <!-- 编辑文章分类 model结束-->
       
        <!--添加子分类的model框-->
        <div class="modal fade" id="example" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document" class="xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel1">${uiLabelMap.AddSubType}</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal"  id="addFirstForm1" action="<@ofbizUrl>createArticleType</@ofbizUrl>" method="post">
                            <div class="row">
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.SubTypeName}">
                                <label  class="control-label col-sm-2">上级分类:</label>
                                <div class="col-sm-10">
                                    <span class="p_name"></span>
                                </div>
                            </div>
                            </div>
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.SubTypeName}">
                                <label  class="control-label col-sm-2">${uiLabelMap.SubTypeName}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control w-p50" name="description"/>
                                    <p class="dp-error-msg"></p>
                                    <input type="hidden" class="form-control w-p50"  name="parentTypeId"/>
                                    <input type="hidden" class="form-control w-p50"  name="typeLevel"/>
                                </div>
                            </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-primary btn_save2 ">${uiLabelMap.Save}</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div><!--添加子分类的model框 end-->
        
        <!--三级子分类的模态框-->
        <div class="modal fade" id="example3" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
            <div class="modal-dialog" role="document" class="xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                        <h4 class="modal-title" id="exampleModalLabel2">${uiLabelMap.AddSubType}</h4>
                    </div>
                    <div class="modal-body">
                        <form class="form-horizontal"  id="addFirstForm2" action="<@ofbizUrl>createArticleType</@ofbizUrl>" method="post">
                            <div class="row">
                            <div class="form-group" data-type="required" data-mark="${uiLabelMap.SubTypeName}">
                                <p class="p_name1"><span class="span1">11</span>&nbsp;&gt;&nbsp;<span class="span2">22</span></p>
                                <label  class="control-label col-sm-2">${uiLabelMap.SubTypeName}:</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control w-p50" name="description"/>
                                     <p class="dp-error-msg"></p>
                                    <input type="hidden" class="form-control w-p50"  name="parentTypeId"/>
                                    <input type="hidden" class="form-control w-p50"  name="typeLevel"/>
                                </div>
                            </div>
                            </div>
                            
                            <div class="modal-footer">
                                <button type="button" class="btn btn-primary btn_save3 ">${uiLabelMap.Save}</button>
                                <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.Cancel}</button>
                            </div>

                        </form>
                    </div>
                </div>
            </div>
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
    $(function(){
        $(".btn-primary").click(function(){
            $("#description").val("");
          var a=$(this).attr("data-num");
            $(".number").val(a);
            // $('#exampleModal').modal('show');
        });
        
        //编辑弹出框
        $("#example2").on("click",".xl_bj",function(){
           var a=$(this).parent().parent().prev().html();
           var articleTypeId=$(this).parent().parent().find('input[name=articleTypeId]').val();
            $(this).parent().parent().prev().addClass("new_name");
            $('#EditexampleModal').modal('show');
            $("#EditexampleModal").find('input[name=description]').val(a);
            $("#EditexampleModal").find('input[name=articleTypeId]').val(articleTypeId);
        });
        
        //编辑提交按钮点击事件
	    $('.edit_btn_save').click(function(){
			$('#EditForm').submit();
	    });
       //编辑表单校验
       $('#EditForm').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "updateArticleType",
					type: "POST",
					data: $('#EditForm').serialize(),
					dataType : "json",
					success: function(data){
						//隐藏新增弹出窗口
						$('#EditexampleModal').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionSuccess}");
	    				$('#modal_msg').modal();
	    				//提示弹出框隐藏事件，隐藏后重新加载当前页面
	    				$('#modal_msg').off('hide.bs.modal');
	    				$('#modal_msg').on('hide.bs.modal', function () {
						  window.location.href='<@ofbizUrl>articleType</@ofbizUrl>';
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
       
       
        //添加二级子分类的弹框
        $("#example2").on('click','.gss-button',function(){
            $(this).closest(".first_tr").addClass("current");
            var name=$(this).closest(".first_tr").find(".classfy_name").html();
            var id=$(this).closest(".first_tr").find('input[name=articleTypeId]').val();
            var typelevel=$(this).closest(".first_tr").find('input[name=typeLevel]').val();
            $(".p_name").html(name);
            $('#example').find('input[name=parentTypeId]').val(id);
            $('#example').find('input[name=typeLevel]').val(typelevel);
            $('#example').modal('show');
        });
        
        
         //添加二级级分类弹框关闭事件
		$('#example').on('hide.bs.modal', function () {
		  $('#addFirstForm1').dpValidate({
		  	clear: true
		  });
		})
        //二级分类提交按钮点击事件
	    $('.btn_save2').click(function(){
	        $('#addFirstForm1').dpValidate({
			  clear: true
			});
			$('#addFirstForm1').submit();
	    });
	    
        //表单校验
       $('#addFirstForm1').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "createArticleType",
					type: "POST",
					data: $('#addFirstForm1').serialize(),
					dataType : "json",
					success: function(data){
					 if(data.success){
						//隐藏新增弹出窗口
						$('#example').modal('toggle');
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
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
        
        //添加三级子分类
        $("#example2").on('click','.gss-button1',function(){
            $(this).closest(".second_tr").addClass("current1");
            var name2=$(this).closest(".second_tr").find(".second_td").html();
            var name1=$(this).closest(".first_tr").find(".classfy_name").html();
            var id=$(this).closest(".second_tr").find('input[name=articleTypeId]').val();
            var typelevel=$(this).closest(".second_tr").find('input[name=typeLevel]').val();
            $('#example3').find('input[name=parentTypeId]').val(id);
            $('#example3').find('input[name=typeLevel]').val(typelevel);
            $(".p_name1>.span1").html(name1);
            $(".p_name1>.span2").html(name2);
            $('#example3').modal('show');
        });
        
        
          //添加二级级分类弹框关闭事件
		$('#example3').on('hide.bs.modal', function () {
		  $('#addFirstForm2').dpValidate({
		  	clear: true
		  });
		})
		
        //提交按钮点击事件
	    $('.btn_save3').click(function(){
	        $('#addFirstForm2').dpValidate({
			  clear: true
			});
			$('#addFirstForm2').submit();
	    });
        //表单校验
       $('#addFirstForm2').dpValidate({
        validate: true,
        callback: function(){
           $.ajax({
					url: "createArticleType",
					type: "POST",
					data: $('#addFirstForm2').serialize(),
					dataType : "json",
					success: function(data){
					 if(data.success){
						//隐藏新增弹出窗口
						$('#example3').modal('toggle');
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
						$('#modal_add').modal('toggle');
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.OptionFail}");
	    				$('#modal_msg').modal();
					}
				});
          }
        });
        
//        点击小图标展开下一级菜单
        $("#example2").on("click",".first_icon",function(){
            if($(this).hasClass("glyphicon-plus-sign")){
                $(this).removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
                $(this).parent().find(".second_tr").show();
            }else{
                $(this).removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
                $(this).parent().find(".second_tr").hide();
            }

        });
//        二级展开三级的图标
        $("#example2").on("click",".second_icon",function(){
            if($(this).hasClass("glyphicon-plus-sign")){
                $(this).removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
                $(this).parent().find(".third_tr").show();
            }else{
                $(this).removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
                $(this).parent().find(".third_tr").hide();
            }

        });


	    //删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
			//异步调用删除方法
			$.ajax({
				url: "delArticleType",
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
    })
    	//删除按钮事件
	    function del(id){
		 del_ids = id;
		 $.ajax({
					url: "getArticleTypeById",
					type: "POST",
					data: {deleteId : del_ids},
					dataType : "json",
					success: function(data){
						if(data.thisarticle>0||data.article>0){
							//设置提示弹出框内容
							$('#modal_msg #modal_msg_body').html("${uiLabelMap.DeleteNotArticleType}");
							$('#modal_msg').modal();
						}else{
							//设置删除弹出框内容
				    		$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.DeleteArticleType}");
				    		$('#modal_confirm').modal('show');
						}
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
						$('#modal_msg').modal();
					}
				});
		}
</script>