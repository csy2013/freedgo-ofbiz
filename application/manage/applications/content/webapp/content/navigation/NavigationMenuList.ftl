<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/AdminLTE.min.css</@ofbizContentUrl>" type="text/css"/>

<div class="box box-info">
	<div class="box-body">
		  <div class="row m-b-10">
	      <!-- 操作按钮组start -->
	      <div class="col-sm-6">
	        <div class="dp-tables_btn">
	        		<#if security.hasEntityPermission("NAVIGATION_MENU", "_CREATE", session)>
                    <a class="btn btn-primary" href="javascript:createNavMenu();">
                    	<i class="fa fa-plus"></i>${uiLabelMap.CommonAdd}
                    </a>
                    </#if>
                    <#if security.hasEntityPermission("NAVIGATION_MENU", "_DELETE", session)>
                   	<a class="btn btn-primary" href="javascript:delNav();">
                    	<i class="fa fa-trash"></i>删除
                    </a>
                    </#if>
            </div>
	      </div><!-- 操作按钮组end -->
	      
	      <!-- 表格区域start -->
	    <div class="row">
	      <div class="col-sm-12">
	      	<table id="navMenuTable" class="table table-bordered table-hover js-checkparent">
	        </table>
	      </div>
	    </div><!-- 表格区域end -->
	</div>

</div>

<!-- 显示或隐藏确认弹出框start -->
<div id="modal_confirm"  class="modal fade" tabindex="-1">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button id="confirmClose" type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title"></h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="confirmCancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="confirmOk" type="button" class="btn btn-primary">确定</button>
            </div>
        </div>
    </div>
</div><!-- 显示或隐藏确认弹出框end -->
<!-- 参考页面：FindWebSite -->
<!-- 修改弹出框start -->
<div id="editexampleModal" class="modal fade" tabindex="-1">
    <div class="modal-dialog" >
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" ></h4>
            </div>
            <div class="modal-body">
            	<!-- 隐藏输入框 -->
                <form id="mainForm" class="form-horizontal">
                	<input type="hidden" name="navId" />
	                <div class="row">
	                	<div class="form-group" data-type="required">
							<label class="col-sm-2 control-label"><i class="required-mark">*</i>导航名称</label>
							<div class="col-sm-10">
								<input type="text" name="navName" placeholder="请输入导航名称" class="form-control dp-vd w-p60">
								<p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
                    <!-- 
                    <div class="row">
						<div class="form-group" data-type="required">
	                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>导航地址</label>
	                        <div class="col-sm-10">
	                        	 <input type="text" name="navUrl" placeholder="请输入导航地址" class="form-control dp-vd w-p60"/>
	                        	 <p class="dp-error-msg"></p>
	                        </div>
                    	</div>
					</div>
                      -->
					<#--<div class="row">-->
						<#--<div class="form-group" data-type="required">-->
	                        <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>导航类型</label>-->
          					<#--<div class="col-sm-10">-->
								<#--<select class="form-control dp-vd w-p60" name="navType">-->
									<#--<option value="">======请选择======</option>-->
									<#--<#list navTypes as type>-->
		            				<#--<option value="${type.enumId}">${type.description}</option>-->
		            				<#--</#list>-->
		          				<#--</select>-->
								<#--<p class="dp-error-msg"></p>-->
							<#--</div>-->
                    	<#--</div>-->
					<#--</div>-->


                    <div class="row">
                        <div id="linkDiv" class="form-group col-sm-8" style="display:none;">
                            <label class="col-sm-2 control-label">链接地址:</label>
                            <div class="col-sm-10">
                                <div class="col-sm-5" style="padding-left: 0px;">
                                    <input type="text" class="form-control" id="linkUrl" name="linkUrl"/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="form-group" >
                            <label class="col-sm-2 control-label">导航链接</label>
                            <div class="col-sm-10">
                                <div style="overflow:hidden;margin-bottom: 10px;">
                                    <select id="firstLinkType"  name="firstLinkType" class="form-control" style="width:200px;float: left;margin-right: 20px;">
                                        <option value="">===请选择链接类型===</option>
									<#if linkTypes?has_content && (linkTypes?size > 0)>
										<#list linkTypes as linkType>
                                            <option value="${linkType.enumId}">${linkType.description}</option>
										</#list>
                                        <option value="FLT_CHB">拆红包</option>
                                        <#--<option value="FLT_ZSLP">赠送礼品</option>-->
                                        <option value="FLT_XYD">心愿单</option>
                                        <option value="FLT_DTYJF">答题赢积分</option>
                                        <option value="FLT_ZZK">找折扣</option>
                                        <option value="FLT_PT">拼团</option>
									</#if>
                                    </select>
                                </div>
                                <div id="selectViewDiv" style="margin-top: 10px;display:none;"  >
                                    <span>已选择:</span>
                                    <span id="selectName"  name="selectName" style="margin-left: 10px;color: blue;cursor: pointer;"></span>
									<input type="hidden" name="linkName" id="linkName"/>
                                    <input type="hidden" id="linkId"  name="linkId"/>
                                </div>
                            </div>
						</div>

                    </div>
					
					<div class="row">
	                	<div class="form-group" data-type="required">
							<label class="col-sm-2 control-label"><i class="required-mark">*</i>导航图标</label>
							<div class="col-sm-4">
								<img id="urlIconImg" style="max-height: 100px;max-width: 200px;">
			                    <input style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile" value="选择图片"/>
			                    <input type="hidden" class="dp-vd" name="contentId" />
			                    <p class="dp-error-msg"></p>
							</div>
                            <div class="col-sm-6">
                                <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 60*60px</div>
                            </div>
						</div>
	                </div>
					
                    <div class="row">
                    	<div class="form-group">
	                        <label class="col-sm-2 control-label">描述信息</label>
	                        <div class="col-sm-10">
	                        	 <input type="text" name="navDesc" placeholder="请输入描述信息" class="form-control w-p60"/>
	                        	 <p class="dp-error-msg"></p>
	                        </div>
                    	</div>
                    </div>
                    <div class="row">
                    	<div class="form-group" data-type="required">
	                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>是否启用</label>
	                        
	                        <div class="col-sm-10">
	                        	<div class="radio">
		                        	<label >
		                        		<input type="radio" name="isEnabled" value="Y" class="js-Y" checked/>${uiLabelMap.Y}
		                        	</label>
		                        	<label>
		                        		<input type="radio" name="isEnabled" value="N" class="js-N"/>${uiLabelMap.N}
		                        	</label>
		                        	<p class="dp-error-msg"></p>
	                        	</div>	
	                        </div>
                    	</div>
                    </div>
                    <div class="row">
                    	<div class="form-group" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">
	                        <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号</label>
	                         <div class="col-sm-10">
	                        	<input type="text" name="seqNo" placeholder="请输入导航序号" class="form-control dp-vd w-p60"/>
	                        	<p class="dp-error-msg"></p>
	                        </div>
                   		</div>
                    </div>
                    <#--<div class="row">-->
                    	<#--<div class="form-group" data-type="minCheck" data-number="1" data-mark="展示前台">-->
	                        <#--<label class="col-sm-2 control-label"><i class="required-mark">*</i>展示前台</label>-->
	                         <#--<div class="col-sm-10">-->
	                        	<#--&lt;#&ndash;<input type="checkbox" name="exPlat" value="P" class="dp-vd"/>PC端&ndash;&gt;-->
	                        	<#--&lt;#&ndash;<input type="checkbox" name="exPlat" value="M" class="dp-vd"/>移动端&ndash;&gt;-->
							    <#--<input type="checkbox" name="exPlat" value="W" class="dp-vd"/>小程序端-->
	                        	<#--<p class="dp-error-msg"></p>-->
	                        <#--</div>-->
                    	<#--</div>-->
                    <#--</div>-->
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="站点">
                            <label class="col-sm-2 control-label"><i class="required-mark">*</i>站点:</label>
                            <div class="col-sm-10">
                                <div class="col-sm-5" style="padding-left: 0px;">
                                    <select id="webSite" class="form-control select2" multiple="multiple" name="webSite" data-placeholder="请选择站点" style="width: 200px;">
									<#assign webSiteList = delegator.findByAnd("WebSite",{"isEnabled":"Y"}) >
									<#if webSiteList?has_content>
										<#list webSiteList as webSite>
                                            <option value="${webSite.webSiteId}">${webSite.siteName}</option>
										</#list>
									</#if>
                                    </select>
                                    <p class="dp-error-msg"></p>
                                </div>
                                <div class="col-sm-3" style="padding-left: 0px;">
                                    <div class="checkbox">
                                        <label>
                                            <input id="isAllWebSite" type="checkbox" value="0" checked name="isAllWebSite">所有站点
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>
					</div>
                   
                </form>
            </div>
            <div class="modal-footer">
             <!-- 是否有修改权限-->
                <button id="btnSave" type="button" class="btn btn-primary" >保存</button>
                <button id="btnCancel" type="button" class="btn btn-default">取消</button>
            </div>
        </div>
	</div>
</div>
<!-- 修改弹出框end -->

<!-- 提示弹出框start -->
<div id="showMsg"  class="modal fade" tabindex="-1" >
    <div class="modal-dialog" >
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" ><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" >系统提示</h4>
            </div>
            <div class="modal-body">
            	<!-- 提示信息 -->
                <h4></h4>
            </div>
            <div class="modal-footer">
                <button  class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<script>
	var navMenuTable;
	var queryUrl = 'queryNavMenu'
	var $navForm = $('#mainForm')
	var $editModal = $('#editexampleModal')
	
	$(function(){
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
	       		$navForm.find('input[name=contentId]').val(data.uploadedFile0);
	       		$('#urlIconImg').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
	       })
		});
	    
	    // 图片选择控件显示
	    $('#uploadedFile').click(function(){
	        $.chooseImage.show();
	    });


        //链接地址一的选项切换事件
        $('#firstLinkType').on('change',function(){
            var type = $(this).val()
            selectLinkHandler(type)
        });

        //已选的名称点击事件
        $('#selectName').on('click',function(){
            var flt =  $('#firstLinkType').val()
            selectLinkHandler(flt)
        });
		
	})
	
	;(function(win,doc){
		 //初始化select2
		$(".select2").select2({
			closeOnSelect:false
		});
		
		 //所有站点的选中事件
	    $('#isAllWebSite').change(function(){
	    	if($(this).prop("checked")){
	    		$("#webSite").val(null).trigger("change");
	    		$("#webSite").prop("disabled", true);
	    		$("#webSite").removeClass('dp-vd');
	    	}else{
	    		$("#webSite").prop("disabled", false);
	    		$("#webSite").addClass('dp-vd');
	    	}
	    });
		
		
		//多选框设置
		var setCheckbox = function(name,arr,$form){
			$form.find("[name = "+ name +"]:checkbox").each(function(){
				var $self = $(this)
				arr.forEach(function(el){
                    	if(el===$self.val()){
                    		 $self.attr("checked",'checked');
                    	}
                });
			})
		}
		
		//站点设置
		var setWebSite = function(record){
			 var isAll = record['isAllWebSite'];
			 if(isAll==='1'){
			 	$('#isAllWebSite').attr("checked",false);
			 	$("#webSite").addClass('dp-vd');
			 	$.ajax({
			 		url: 'queryNavWebSites',
			 		async: true,
			 		type: 'POST',
			 		data: {navId:record['navId']},
			 		dataType: 'json',
			 		success: function(data){
			 			if(data.status!=='success'){
			 				$editModal.modal('hide');
			 				showMsg('导航菜单站点信息查询失败')
			 				return
			 			}
			 			var webSiteIdArr = [];
			 			data.webSites.forEach(function(el){
			 				webSiteIdArr.push(el['webSiteId'])
			 			})
			 			$("#webSite").prop("disabled", false);
			 			$("#webSite").val(webSiteIdArr).trigger("change");
			 			
			 		},
			 		error: function(data){
			 			$editModal.modal('hide');
			 			showMsg('导航菜单站点信息查询失败')
			 		}
			 	})
			 }else{
			 	$('#isAllWebSite').prop("checked","true");
			 	$("#webSite").removeClass('dp-vd');
			 	$("#webSite").prop("disabled", true);
			 }
		}
		
		//数据编辑
		var updateNavMenu = function(record){
			//标题修改
			$editModal.find('.modal-header h4').html('导航菜单编辑')			
			//页面数据渲染
			$navForm.find('input[name=navId]').val(record['navId'])
			$navForm.find('input[name=navName]').val(record['navName'])
			$navForm.find('input[name=navDesc]').val(record['navDesc'])
			//$navForm.find('input[name=navUrl]').val(record['navUrl'])
			$navForm.find("input[type=radio][name=isEnabled][value="+ record['isEnabled'] +"]").prop("checked",true);
//			$navForm.find('select[name=navType]').val(record['navType'])
//			alert(record['firstLinkType']);
            var linkType = record['firstLinkType'];
            var linkUrl = record['linkUrl'];
            var linkId = record['linkId'];
            var linkName = record['linkName'];
//			alert(linkType+"****"+linkId+'*****'+linkName);
            $navForm.find('select[name=firstLinkType]').val(linkType);
            $navForm.find('input[name=linkId]').val(linkId);
            $navForm.find('input[name=linkName]').val(linkName);
            $navForm.find('span[name=selectName]').html(linkName);
            $navForm.find('input[name=linkUrl]').val(linkUrl);
            switch (linkType)
            {
                    // 自定义链接
                case 'FLT_ZDYLJ':
                    $('#linkDiv').show();
                    $('#selectViewDiv').hide();
                    break;
                default:
                    if(linkUrl){
                        $('#selectViewDiv').show();
                        $('#linkDiv').hide();
                    }
            };
			$('#urlIconImg').attr({"src":"/content/control/getImage?contentId="+record['contentId']}) ;
			$navForm.find('input[name=contentId]').val(record['contentId'])
			
			$navForm.find('input[name=seqNo]').val(record['seqNo'])
//			//展示前台选择框处理
//			var exPlatArr = record['exPlat'].split(',');
//			setCheckbox('exPlat',exPlatArr,$navForm);
            //站点选择,是否选择全部站点
            setWebSite(record)
		    $editModal.modal();
			 //表单校验
		   $navForm.dpValidate({
		       validate: true,
		       callback: editNavHandler
		   });
		}
		
		
		//编辑的保存回调
		var editNavHandler = function(){
			$.ajax({
					url: 'updateNavMenu',
					type: "POST",
					data: $navForm.serialize(),
					dataType: "json",
					success: function(data) {
						$editModal.modal('hide');
						//clearForm();
						if(data.status!=='success'){
							showMsg('保存失败');
							return;
						}
						showMsg('保存成功', '系统提示', function() {
//							navMenuTable.reload(queryUrl);
                            window.location = '<@ofbizUrl>listNavMenu</@ofbizUrl>';
						});
					},
					error: function(data) {
						//设置提示弹出框内容
						showMsg('保存失败');
					}
				});
		}
		//页面隐藏的时候清除保存事件
		$editModal.on('hidden.bs.modal',function(e){
			$navForm.unbind('submit');
			clearForm();
		});
		
		//表单数据清理
		 win.clearForm = function(){
			$editModal.find("input[type=text]").val('');
			$editModal.find("input[type=hidden]").val('');
			$editModal.find("input[type=checkbox]").removeAttr("checked");
			$editModal.find("input[type=radio]").removeAttr("checked");
			$editModal.find("input[type=radio][value=Y]").attr("checked","true");
			
//			$navForm.find('select[name=navType]').val('');

			 $navForm.find('select[name=firstLinkType]').val('');
			 $navForm.find('#selectViewDiv').hide();
			 $navForm.find('span[name=selectName]').html('');
			 $navForm.find('input[name=linkName]').val('');
			 $navForm.find('input[name=linkId]').val('');

			
			$('#isAllWebSite').prop("checked","true");
			$("#webSite").val(null).trigger("change");
	    	$("#webSite").prop("disabled", true);
	    	$("#webSite").removeClass('dp-vd');
	    	
	    	$('#urlIconImg').attr({"src":""});
			//表单页面清理
			$navForm.dpValidate({clear: true});
			
		}
		//仅显示提示信息，回调隐藏时执行
		var showMsg = function(msg,title,callback){
			var $msgDialog = $('#showMsg');
			$msgDialog.modal();
			if(title){
				$msgDialog.find('.modal-header h4').html(title);
			}
			$msgDialog.find('.modal-body h4').html(msg);
			
			$msgDialog.one('hide.bs.modal',function(e){
				if(callback){
					callback();
				}
			});
			
		}
		
		$('#btnCancel').on('click',function(){
			$editModal.modal('hide');
		});
		
	  //新建菜单回调
	  var createNavHandler = function(){
	  		 $.ajax({
	                   url: "saveNavMenu",
	                   type: "POST",
	                   data: $navForm.serialize(),
	                   dataType: "json",
	                   success: function(data) {
	                       $editModal.modal('hide');
	                       clearForm();
	                       if(data.status!=='success'){
	                       		showMsg('保存失败');
	                       		return;
	                       }
	                       showMsg('保存成功', '系统提示', function() {
//	                           navMenuTable.reload(queryUrl);
                               window.location = '<@ofbizUrl>listNavMenu</@ofbizUrl>';
	                       });
	                   },
	                   error: function(data) {
	                       //设置提示弹出框内容
	                       showMsg('保存失败');
	                   }
	               });
	  
	  }
	 
	   win.createNavMenu = function() {
	   	   $editModal.find('.modal-header h4').html('导航菜单添加')
           $("#webSite").val(null).trigger("change");
           $("#webSite").prop("disabled", true);
           $("#webSite").removeClass('dp-vd');
	       $('#editexampleModal').modal();
	       //表单校验
		   $navForm.dpValidate({
		       validate: true,
		       callback: createNavHandler
		   });
		 
	   }
	    //保存按钮的点击事件
	  
	   	$("#btnSave").on('click', function() {
		      //校验表单，保存数据，刷新列表
		      $navForm.dpValidate({
		          clear: true
		      });
		      $navForm.submit();
		});
	  
		//更新导航菜单状态
		var updatNavStatus = function(record,navId,$btn){
			var isEnabled = record['isEnabled']
			$btn.on('click',function(){
				//注册事件，修改启用状态
				$.ajax({
					url: 'updateNavStatus',
					type: "POST",
					data: {
						navId: navId,
						isEnabled: isEnabled
					},
					dataType: "json",
					success: function(data) {
						if(data.status!=='success'){
							showMsg('状态修改失败!'+(data.msg===undefined?'':data.msg));
							return;
						}
						//刷新数据
						navMenuTable.reload(queryUrl);
					},
					error: function(data) {
						showMsg('状态修改失败！');
					}
				});		
			});
		}
		
		navMenuTable = $('#navMenuTable').dataTable({
			ajaxUrl: queryUrl,
			columns: [
				{"title":"id","code":"navId","checked":true},
				{"title":"导航名称","code":"navName"},
				//{"title":"导航地址","code":"navUrl"},
//				{"title":"导航类型","code":"navTypeName"},
                {"title":"链接地址","code":"linkUrl"},

				{"title":"是否启用","code":"isEnabled","handle":function(td,record){
					var btnClass ;
					//列表页点击可调整是否,修改是否启用的状态
					switch(record['isEnabled'])
						{
						<#if security.hasEntityPermission("NAVIGATION_MENU", "_UPDATE", session)>
						case 'Y':
						  td.html('<button class="gss_btn btn btn-primary">${uiLabelMap.Y}</button>');
						  break;
						case "N":
						  	td.html('<button class="gss_btn btn btn-default">${uiLabelMap.N}</button>');
						  break;
						default:
						  td.html('<button class="gss_btn btn btn-default">${uiLabelMap.N}</button>');
						  <#else>
						  	case 'Y':
						  		td.html('${uiLabelMap.Y}');
						  	 break;
							case "N":
						  		td.html('${uiLabelMap.N}');
						  	 break;
							default:
						  		td.html('${uiLabelMap.N}');
						  </#if>
						}
						<#if security.hasEntityPermission("NAVIGATION_MENU", "_UPDATE", session)>
						updatNavStatus(record,record['navId'],td.find('button'))
						</#if>
				}},
				{"title":"序号","code":"seqNo"},
//				{"title":"展示前台","code":"exPlat","handle":function(td,record){
//					//console.log(record);
//					var arr = record['exPlat'].split(',');
//					td.html(arr.map(function(el){
//						if(el==='P'){
//							return 'PC端';
//						}else if(el === 'M'){
//							return '移动端';
//						}else if(el==='W'){
//							return '小程序端';
//						}
//						return '';
//					}).join(','));
//				}},
				<#if security.hasEntityPermission("NAVIGATION_MENU", "_UPDATE", session)>
				 {"title":"操作","code":"option",
                    "handle":function(td,record){
                    	
                        var $btn =$("<div class='btn-group'>"+
                                "<button type='button' class='btn btn-danger btn-sm' onclick=''>编辑</button>"+
                                "</div>") ;
                        td.append($btn);
                        //注册点击编辑事件
                        $btn.on('click',function(e){
                        	updateNavMenu(record);
                        })
                       
                    }
                }
                 </#if>
			],
			listName:'navMenus'}
		);
		//删除确认按钮
		var $btnConfirmOk = $('#confirmOk');
		
		win.delNav = function(){
			//获取被选中的对象
			var checkedNavs = $('.js-checkparent .js-checkchild:checked');
			if(checkedNavs.size()<=0){
				 //设置提示弹出框内容
                showMsg('请至少选择一条记录！');
			}else{
				var ids = [];
				checkedNavs.each(function(){
					ids.push($(this).val());
				});
				 //设置提示弹出框内容
                $('#modal_confirm #modal_confirm_title').html("删除导航菜单");
                $('#modal_confirm #modal_confirm_body').html("确定删除这些信息？");
                $('#modal_confirm').modal('show');
            	$btnConfirmOk.unbind('click');
            	//确定按钮的点击事件，点击时删除已经选中的菜单导航信息
				$btnConfirmOk.on('click',function(){
					//删除选中信息
		            $.ajax({
		                url: "delNavMenus",
		                type: "POST",
		                data: {navIds:ids.join(',')},
		                dataType : "json",
		                success: function(data){
		                	if(data.status!=='success'){
		                		showMsg('删除失败','系统提示');
		                		return;
		                	}
		                	showMsg('删除成功','系统提示',function(){
		                		navMenuTable.reload(queryUrl);
		                	});
		                   
		                },
		                error: function(data){
		                    showMsg('删除失败','系统提示');
		                }
		            });
					//隐藏确认框
					$('#modal_confirm').modal('hide');
				});
				
			}
		}
		
	})(window,document)


    // 链接类型选择链接回调处理
    var selectLinkHandler = function(type){
        switch (type) {
            case 'FLT_WZLJ':
            {
                $('#linkDiv').hide();
                $.dataSelectModal({
                    url: "ArticleListModalPage",
                    width:	"800",
                    title:	"选择文章",
                    selectId: "linkId",
                    selectName:	"selectName",
                    selectCallBack: function(el){
                        $('#selectViewDiv').show();
                        $('#linkUrl').val('modalName=WZ&id;='+el.data('id'));
                        var linkName = $('#selectName').html();
                        $('#linkName').val(linkName);
                    }
                });
            }
                break;
            case 'FLT_HDLJ':
            {
                $('#linkDiv').hide();
                $.dataSelectModal({
                    url: "/prodPromo/control/ProActivityMgrListModalPage?externalLoginKey=${externalLoginKey}",
                    width:	"800",
                    title:	"选择活动",
                    selectId: "linkId",
                    selectName:	"selectName",
                    selectCallBack: function(el){
                        $('#selectViewDiv').show();
                        $('#linkUrl').val('modalName=HD&id='+el.data('id'));
                        var linkName = $('#selectName').html();
                        $('#linkName').val(linkName);

                    }
                });
            }
                break;
		<#-- 自定义链接  -->
            case 'FLT_ZDYLJ':
            {
                $('#selectViewDiv').hide();
                $('#linkId').val('');
                $('#selectName').html('');
                $('#linkUrl').val('');
                $('#linkDiv').show();
            }
                break;
		<#-- 商品选择添加 -->
            case 'FLT_SPLJ':
            {
                $('#linkDiv').hide();
                $.dataSelectModal({
                    url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
                    width:	"800",
                    title:	"选择商品",
                    selectId: "linkId",
                    selectName:	"selectName",
                    selectCallBack: function(el){
                        $('#selectViewDiv').show();
                        $('#linkUrl').val('modalName=SP&id='+el.data('id'));
                        var linkName = $('#selectName').html();
                        $('#linkName').val(linkName);
                    }
                });
            }
                break;
                // 促销类型
            case 'FLT_CXLJ':
            {
                $('#linkDiv').hide();
                $.dataSelectModal({
                    url: "/prodPromo/control/PromoListModalPage?externalLoginKey=${externalLoginKey}",
                    width:	"800",
                    title:	"选择促销",
                    selectId: "linkId",
                    selectName:	"selectName",
                    selectCallBack: function(el){
                        $('#selectViewDiv').show();
                        var modalName = el.data('record').activityType;
                        $('#linkUrl').val('modalName='+modalName+'&id='+el.data('id'));
                        var linkName = $('#selectName').html();
                        $('#linkName').val(linkName);

                    }
                });
            }
            default:
            {
                $('#selectViewDiv').hide();
                $('#linkId').val('');
                $('#selectName').html('');
                $('#linkUrl').val('');
                $('#linkDiv').hide();
            }
        }
    }
</script>
	