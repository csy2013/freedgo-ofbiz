
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<!-- 进入页面自动展示列表，根据社区店编号排序 -->
<!-- 根据社区店名称做模糊检索 -->
<div class="box box-info">
	<div class="box-body">
		<!-- 条件查询表单start -->
        <form class="form-inline clearfix">
               <div class="form-group w-p100">
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">社区店名称</span>
                        <input type="text" class="form-control"  id="storeNameCond" placeholder="社区店名称">
                    </div>
                    <div class="input-group m-b-10">
                        <span class="input-group-addon">社区店编号</span>
                        <input type="text" class="form-control"  id="commStoreIdCond" placeholder="社区店编号" >
                    </div>
                      <div class="input-group m-b-10">
                        <span class="input-group-addon">社区店类型</span>
                        <select class="form-control" style="min-width: 196px;" id="storeTypeCond">
            				<option value="">======请选择======</option>
            				<#list communityStoreTypes as type>
		            		<option value="${type.enumId}">${type.description}</option>
		            		</#list>
          				</select>
                    </div>
                    <div class="input-group pull-right">
	                	<button id="btnQuery" class="btn btn-success btn-flat">搜索</button>
	                </div>
                </div>
            </form><!-- 条件查询表单end -->
            
            <!-- 分割线start -->
            <div class="cut-off-rule bg-gray"></div>
            <!-- 分割线end -->
            
    <!--工具栏start -->
    <div class="row m-b-10">
      <!-- 操作按钮组start -->
      <div class="col-sm-6">
        <div class="dp-tables_btn">
        	<#if security.hasEntityPermission("COMMUNITY_STORE", "_CREATE", session)>
	        <button id="btnAdd" class="btn btn-primary">
	            <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
	        </button>
	        </#if>
	        <#if security.hasEntityPermission("COMMUNITY_STORE", "_DELETE", session)>
	        <button id="btnDel" class="btn btn-primary">
	            <i class="fa fa-trash"></i>删除
	        </button>
	        </#if>
        </div>
      </div><!-- 操作按钮组end -->
      <!-- 列表当前分页条数start -->
      <div class="col-sm-6">
	      <div id="pageSize" class="dp-tables_length">
	      </div>
      </div><!-- 列表当前分页条数end -->
    </div><!-- 工具栏end -->
	<!-- 表格区域start -->
    <div class="row">
      <div class="col-sm-12">
        <table id="storeTable" class="table table-bordered table-hover js-checkparent">
        </table>
      </div>
    </div><!-- 表格区域end -->
    <!-- 分页条start -->
    <div class="row" id="paginateDiv">
	</div><!-- 分页条end -->
  </div><!-- /.box-body -->
</div><!-- 内容end -->

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

<!-- 编辑，新增弹出框start -->
<div id="editStore" class="modal fade" tabindex="-1">
    <div class="modal-dialog" >
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" ></h4>
            </div>
            <div class="modal-body">
            	<form id="storeForm" class="form-horizontal">
            		<input type="hidden" name="commStoreId" />
            		<div class="row">
	                	<div class="form-group" data-type="required">
							<label class="col-sm-3 control-label"><i class="required-mark">*</i>社区店编号</label>
							<div class="col-sm-9">
								<input type="text" disabled name="commStoreId" class="form-control w-p60" >
							</div>
						</div>
	                </div>
	                <div class="row">
	                	<div class="form-group" data-type="required">
							<label class="col-sm-3 control-label"><i class="required-mark">*</i>社区店名称</label>
							<div class="col-sm-9">
								<input type="text" class="form-control dp-vd w-p60" name="storeName">
								<p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
	                <div class="row">
                    	<div class="form-group" data-type="required">
	                        <label class="col-sm-3 control-label"><i class="required-mark">*</i>是否启用</label>
	                        <div class="col-sm-9">
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
	                	<div class="form-group" data-type="required">
							<label class="col-sm-3 control-label"><i class="required-mark">*</i>社区店图标</label>
							<div class="col-sm-9">
								<img id="urlIconImg" style="max-height: 100px;max-width: 200px;">
			                    <input style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile" value="选择图片"/>
			                    <input type="hidden" class="dp-vd" name="iconUrl" />
			                    <p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
                    <div class="row">
	                	<div class="form-group" data-type="required">
							<label class="col-sm-3 control-label"><i class="required-mark">*</i>门店类型</label>
							<div class="col-sm-9">
								<select class="form-control dp-vd w-p60" name="storeType">
									<option value="">======请选择======</option>
									<#list communityStoreTypes as type>
		            				<option value="${type.enumId}">${type.description}</option>
		            				</#list>
		          				</select>
								<p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
	                <div class="row">
                    	<div class="form-group">
	                        <label class="col-sm-3 control-label">支付方式</label>
	                        <div class="col-sm-9">
	                        	<div class="checkbox">
		                        	<label >
		                        		<input type="checkbox" name="payType" class="dp-vd" value="EXT_ALIPAY" />支付宝
		                        	</label>
		                        	<label>
		                        		<input type="checkbox" name="payType" class="dp-vd" value="EXT_WEIXIN"/>微信
		                        	</label>
		                        	<label>
		                        		<input type="checkbox" name="payType" class="dp-vd" value="EXT_UNIONPAY"/>银联
		                        	</label>
		                        	<p class="dp-error-msg"></p>
	                        	</div>	
	                        </div>
                    	</div>
                    </div>
                    
                    <div class="row">
		                <div class="form-group" data-type="required" data-mark="地址">
		                    <label class="col-sm-3 control-label"><i class="required-mark">*</i>地址:</label>
		                    <div class="col-sm-9">
		                    	<div style="display:none;">
			                    	<select name="countryGeoId" id="storeForm_countryGeoId" >
				                    	${screens.render("component://common/widget/CommonScreens.xml#countries")}
					                    <#assign defaultCountryGeoId = Static["org.ofbiz.base.util.UtilProperties"].getPropertyValue("general.properties", "country.geo.id.default")>
				                        <option selected="selected" value="${defaultCountryGeoId}">
				                        <#assign countryGeo = delegator.findByPrimaryKey("Geo",Static["org.ofbiz.base.util.UtilMisc"].toMap("geoId",defaultCountryGeoId))>
				                        ${countryGeo.get("geoName",locale)}
				                        </option>
				                    </select>
			                    </div>
			                    
					            <div class="col-sm-3" style="padding-left: 0px;">
		                            <select class="form-control" name="province" id="storeForm_stateProvinceGeoId">
		                                <option value=""></option>
		                            </select>
		                        </div>
		                        <div class="col-sm-3" style="padding-left: 0px;">
		                            <select class="form-control" name="city" id="storeForm_cityGeoId">
		                                <option value=""></option>
		                            </select>
		                        </div>
		                        <div class="col-sm-3" style="padding-left: 0px;">
		                            <select class="form-control" name="county" id="storeForm_countyGeoId">
		                                <option value=""></option>
		                            </select>
		                        </div>
		                        
		                    	<div class="col-sm-9" style="margin-top: 10px;padding-left: 0px;">
									<input type="text" class="form-control dp-vd" name="address" />
									<p class="dp-error-msg"></p>
								</div>
		                    </div>
		                </div>
		            </div>
                    <div class="row">
	                	<div class="form-group">
							<label class="col-sm-3 control-label">营业时间</label>
							<div class="col-sm-9">
								<input type="text" class="form-control w-p60" name="businessHours">
								<p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
	                <div class="row">
	                	<div class="form-group">
							<label class="col-sm-3 control-label">联系电话</label>
							<div class="col-sm-9">
								<input type="text" class="form-control w-p60" name="contactPhone">
								<p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
	                <div class="row">
	                	<div class="form-group">
							<label class="col-sm-3 control-label">联系人</label>
							<div class="col-sm-9">
								<input type="text" class="form-control w-p60" name="contactMan">
								<p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
	                <div class="row">
	                	<div class="form-group">
							<label class="col-sm-3 control-label">备注</label>
							<div class="col-sm-9">
								<input type="text" class="form-control w-p60" name="remark">
								<p class="dp-error-msg"></p>
							</div>
						</div>
	                </div>
            	</form>
            </div>
            <div class="modal-footer">
             <!-- 是否有修改权限-->
                <button id="btnSave" type="button" class="btn btn-primary" >保存</button>
                <button id="btnCancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
	</div>
</div>
<!-- 编辑，新增弹出框end -->

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

<script>
	// 支付方式
	var payType = {'EXT_ALIPAY':'支付宝','EXT_WEIXIN':'微信支付','EXT_UNIONPAY':'银联支付'};
	// 店铺类型
	var storeType = {
				<#list communityStoreTypes as type>
					'${type.enumId}':'${type.description}',
				</#list>
					} ;
	
	var queryUrl = 'queryCommunityStore';
	
	var dataTable;
	
	// 修改社区店状态
	function editIsShowData (commStoreId, obj) {
		var isEnabled = $(obj).val();
		if (isEnabled == "Y") {
			isEnabled = "N";
		} else {
			isEnabled = "Y";
		}
		$.ajax({
			url: "updateCommunityStoreStatus",
			type: "POST",
			data: {commStoreId:commStoreId, isEnabled:isEnabled},
			dataType: "json",
			success: function(data) {
				console.log(data);
			    if (data.status !== 'success') {
			    	showMsg('保存失败');
			      	return;
			    }
			    if(data.isEnabled=='Y'){
			    	$(obj).attr("class", "btn btn-primary");
			    	$(obj).text("是");
			    	$(obj).val(isEnabled);
		   		}
				if(data.isEnabled=='N'){
			    	$(obj).attr("class", "btn btn-default");
			    	$(obj).text("否");
			    	$(obj).val(isEnabled);
				}
			},
			error: function(data) {
			    //设置提示弹出框内容
			    showMsg('保存失败');
			}
		});
	}
	function showMsg (msg,title,callback){
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
	
	$(function(){
		var $editForm = $("#storeForm")
		var $editModal = $('#editStore')
		
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
		
		//获取查询url
		var getQueryUrl = function(){
			var url = queryUrl;
			var storeName = $.trim($('#storeNameCond').val());
			var storeId = $.trim($('#commStoreIdCond').val());
			var type = $('#storeTypeCond').val();
			var storeType = type===undefined?'':type;
			
			url = changeURLArg(url,"storeName",storeName);
			url = changeURLArg(url,"storeId",storeId);
			url = changeURLArg(url,"storeType",storeType);
			
			return url;
		}
		
		//表格初始化
		dataTable = $('#storeTable').dataTable({
			ajaxUrl: getQueryUrl(),
			columns:[
				{"title":"id","code":"commStoreId","checked":true},
				{"title":"社区店名称","code":"storeName"},
				{"title":"社区店编号","code":"commStoreId"},
				{"title":"社区店地址","code":"address",handle:function(td,record){
					td.html(record['provinceName'] + record['cityName'] + record['countyName'] + record['address'])
				}},
				{"title":"门店类型","code":"storeType",handle:function(td,record){
					td.html(storeType[record['storeType']]);
				}},
				{"title":"是否启用","code":"isEnabled",handle:function(td,record){
					switch(record['isEnabled']){
						case 'Y':
						  td.html("<button class='btn btn-primary' onclick='javascript:editIsShowData("+record.commStoreId+", this)' value='"+record.isEnabled+"'>${uiLabelMap.Y}</button>");
						  break;
						case "N":
						  	td.html("<button class='btn btn-default' onclick='javascript:editIsShowData("+record.commStoreId+", this)' value='"+record.isEnabled+"'>${uiLabelMap.N}</button>");
						  break;
						default:
						  td.html('');
					}
				}},
				<#if security.hasEntityPermission("COMMUNITY_STORE", "_DELETE", session)>
				{"title":"操作","code":"option",
                    "handle":function(td,record){
                        var $btn =$("<div class='btn-group'>"+
                                "<button type='button' class='btn btn-danger btn-sm'>编辑</button>"+
                                "</div>") ;
                        td.append($btn);
                        //注册点击编辑事件
                        $btn.on('click',function(e){
                        	updateStore(record.commStoreId);
                        })
                    }
                }
                </#if>
			],
			listName: "storeList",
			paginateEL: "paginateDiv",
			viewSizeEL: "pageSize"
		});
		
		var updateStore = function(commStoreId){
			$.ajax({
				url : "queryCommunityStore",
				type : "POST",
				data : {storeId : commStoreId},
				dataType : "JSON",
				success : function(data) {
					var record = data.storeList[0];
					$editModal.find('.modal-header h4').html('社区店编辑')	;
					$editModal.modal();
					//界面数据初始化
					$editForm.find('input[name=commStoreId]').val(record['commStoreId'])
					$editForm.find('input[name=commStoreId]').val(record['commStoreId'])
					$editForm.find('input[name=storeName]').val(record['storeName'])
					$editForm.find('select[name=storeType]').val(record['storeType'])
					$editForm.find('input[name=storeName]').val(record['storeName'])
					$editForm.find("input[type=radio][name=isEnabled][value="+ record['isEnabled'] +"]").attr("checked",true);
					//storeIcon
					$('#urlIconImg').attr({"src":"/content/control/getImage?contentId="+record['iconUrl']}) ;
					
					$editForm.find('input[name=iconUrl]').val(record['iconUrl']);
					
					if(record['payType']){
						setCheckbox('payType',record['payType'].split(','),$editForm);
					}
					
					$editForm.find('input[name=address]').val(record['address'])
					
					$('#storeForm_stateProvinceGeoId').val(record['province'])
					$("#storeForm_stateProvinceGeoId").trigger("change");
					$('#storeForm_cityGeoId').val(record['city'])
					$("#storeForm_cityGeoId").trigger("change");
					$('#storeForm_countyGeoId').val(record['county'])
					
					$editForm.find('input[name=businessHours]').val(record['businessHours'])
					$editForm.find('input[name=contactMan]').val(record['contactMan'])
					$editForm.find('input[name=contactPhone]').val(record['contactPhone'])
					$editForm.find('input[name=remark]').val(record['remark'])
					//表单校验
				   $editForm.dpValidate({
				       validate: true,
				       callback: updateHandler
				   });
				},
				error : function(data) {
				}
			});
		}
		//更新回调
		var updateHandler = function(){
			$.ajax({
			  url: "updateCommunityStore",
			  type: "POST",
			  data: $editForm.serialize(),
			  dataType: "json",
			  success: function(data) {
			    $editModal.modal('hide');
			    
			    if (data.status !== 'success') {
			      showMsg('保存失败');
			      return;
			    }
			    showMsg('保存成功');
			    dataTable.reload(getQueryUrl());
			  },
			  error: function(data) {
			    //设置提示弹出框内容
			    showMsg('保存失败');
			  }
			});
		}
		//查询事件注册
		$('#btnQuery').on('click',function(e){
			e.preventDefault();
			dataTable.reload(getQueryUrl());
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
	    $('body').on('click','.img-submit-btn',function(){
	       var obj = $.chooseImage.getImgData();
	       $.chooseImage.choose(obj,function(data){
	       		$editForm.find('input[name=iconUrl]').val(data.uploadedFile0);
	       		$('#urlIconImg').attr({"src":"/content/control/getImage?contentId="+data.uploadedFile0});
	       })
		});
	    
	    // 图片选择控件显示
	    $('#uploadedFile').click(function(){
	        $.chooseImage.show();
	    });
		
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
		
		//页面隐藏的时候清除保存事件
		$editModal.on('hidden.bs.modal',function(e){
			$editForm.unbind('submit');
			clearForm();
		});
		//表单清空
		var clearForm = function(){
			$editModal.find("input[type=text]").val('');
			$editModal.find("input[type=hidden]").val('');
			//表单页面清理
			$editForm.dpValidate({clear: true});
			$('#urlIconImg').attr({"src":""});
			//门店类型重置
			$editForm.find('select[name=storeType] option:first').prop("selected", 'selected');
			$editForm.find("input[type=radio][value=Y]").prop("checked","checked");
			//省市区选择
			$('#storeForm_stateProvinceGeoId option:first').prop("selected", 'selected');
			$("#storeForm_stateProvinceGeoId").trigger("change");
			$('#storeForm_cityGeoId option:first').prop("selected", 'selected');
			$("#storeForm_cityGeoId").trigger("change");
			$('#storeForm_countyGeoId option:first').prop("selected", 'selected');
			//支付方式重置
			$editForm.find('input[name=payType]').removeAttr('checked');
		}
		
		// 创建社区店
		var createComunityStore = function(e){
			$editModal.find('.modal-header h4').html('社区店添加')	
			$editModal.modal()
			//表单校验
		   $editForm.dpValidate({
		       validate: true,
		       callback: saveStoreHandler
		   });
		}
		$('#btnAdd').on('click',createComunityStore)
		
		$("#btnSave").on('click', function() {
            if($("#storeForm_stateProvinceGeoId").val()=='undefined'){
                showMsg('省不能为空');
                return;
            }
			if($("#storeForm_cityGeoId").val()=='undefined'){
                showMsg('市不能为空');
                return;
			}
			if($("#storeForm_countyGeoId").val()=='undefined'){
                showMsg('区不能为空');
                return;
			}
		   $editForm.dpValidate({
		          clear: true
		      });
		   $editForm.submit();
		});
		//店铺保存回调
		var saveStoreHandler = function(){
			$.ajax({
			  url: "saveCommunityStore",
			  type: "POST",
			  data: $editForm.serialize(),
			  dataType: "json",
			  success: function(data) {
			    $editModal.modal('hide');
			    //clearForm();
			    if (data.status !== 'success') {
			      showMsg('保存失败');
			      return;
			    }
			    showMsg('保存成功');
			    dataTable.reload(getQueryUrl());
			  },
			  error: function(data) {
			    //设置提示弹出框内容
			    showMsg('保存失败');
			  }
			});
		}
		//删除确认按钮
		var $btnConfirmOk = $('#confirmOk');
		//社区店禁用
		$('#btnDel').on('click',function(e){
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
                $('#modal_confirm #modal_confirm_title').html("删除社区店");
                $('#modal_confirm #modal_confirm_body').html("确定删除这些信息？");
                $('#modal_confirm').modal('show');
            	$btnConfirmOk.unbind('click');
            	//确定按钮的点击事件，点击时删除已经选中的菜单导航信息
				$btnConfirmOk.on('click',function(){
					//删除选中信息
		            $.ajax({
		                url: "delCommunityStore",
		                type: "POST",
		                data: {commStoreIds:ids.join(',')},
		                dataType : "json",
		                success: function(data){
		                	if(data.status!=='success'){
		                		showMsg('删除失败','系统提示');
		                		return;
		                	}
		                	showMsg('删除成功','系统提示',function(){
		                		dataTable.reload(getQueryUrl());
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
		})
		  
	});
		
</script>