<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/ztree/css/zTreeStyle/zTreeStyle.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.core-3.5.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/ztree/js/jquery.ztree.excheck-3.5.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<script type="text/javascript" src="<@ofbizContentUrl>/images/jquery/plugins/upload/ajaxupload.js</@ofbizContentUrl>"></script>
<script language="JavaScript" type="text/javascript">
    var setting = {
		check: {
		    enable: true
		    <#--
			enable: true,
			chkStyle:"radio",
			radioType:"all"
			-->
		},
		data: {
			simpleData: {
				enable: true
			}
		},
		view: {
	    showIcon: false
	  }
	};
    
    var isCommitted = false;//表单是否已经提交标识，默认为false
    
    
     // 创建一个上传参数
	var uploadOption =
	{
	    action: "brandImport",    // 提交目标
	    name: "file",            // 服务端接收的名称
	    autoSubmit: false,        // 是否自动提交
	    // 选择文件之后…
	    onChange: function (file, extension) 
	    {
	        $('#doc').val(file);
	    },
	    // 开始上传文件
	    onSubmit: function (file, extension)
	    {
	        if (!(extension && /^(xls|XLS|xlsx|XLSX)$/.test(extension))) {  
                alert("只支持xls或xlsx格式文件！");  
                return false;  
            }
	    },
	    // 上传完成之后
	    onComplete: function (file, response) 
	    {
	    	var data = eval('(' + response + ')')
	    	if(data.success){
	    		$('#modal_import').modal('hide');
	    		//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html(data.successMsg);
				$('#modal_msg').modal();
				window.location.href="<@ofbizUrl>ProductBrand</@ofbizUrl>";
	    	}else{
	    		$('#error_list table tbody').empty();
	    		$('#error_list .box-header .box-title small').empty();
				$.each(data.errorMsg, function(){     
					$('#error_list table tbody').append("<tr><td style='text-align:left;color:red'>"+this.msg+"</td></tr>");    
				});
				$('#error_list .box-header .box-title').append("<small style='color:red'>（共"+data.errorMsg.length+"条错误）</small>");
				$('#modal_import').modal('hide');
				$('#modal_error').modal();  
	    	}
	    }
	}
	
    $(function() {
    
        //初始化排序字段
    	var sort_field = "${requestParameters.sortField?if_exists}",
    		sort_type = "${requestParameters.sortType?if_exists}";
    	if(sort_field){
    		if(sort_type=='asc'){
	    		$('#a_'+sort_field).removeClass('fa-sort-amount-desc').addClass('fa-sort-amount-asc').removeClass('text-muted');
	    	}else{
	    		$('#a_'+sort_field).removeClass('text-muted');
	    	}
    	}	
        
        //新增按钮点击事件
	    $('#btn_add').click(function(){
	    	//异步加载分类数据
			$.ajax({
				url: "<@ofbizUrl>getProductCategoryList</@ofbizUrl>",
				type: "POST",
				dataType : "json",
				success: function(data){
					$.fn.zTree.init($("#addProductCategoryArea"), setting, data.productCategoryList);
					//设置提示弹出框内容
					var $model = $('#addBrand');
					$model.find('#exampleModalLabel').text("添加品牌");
					$model.find('input[name=brandName]').val("");//品牌名称
					$model.find('input[name=brandNameAlias]').val("");//品牌别名
					
					$model.find('input[name=operateType]').val("create");//操作
					$model.find('input[name=productBrandId]').val("");//品牌Id
					$model.find('input[name=contentId]').val("");//图片Logo ContentId
					$('#addBrand #img').attr('src',"");
					$('#addBrand').modal();
				},
				error: function(data){
					//设置提示弹出框内容
					$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
    				$('#modal_msg').modal();
				}
			});
	    });
        
        $("#btnBrandSave").click(function(){
           $("#updateProductBrand").submit();
        });
        
	    $("#updateProductBrand").dpValidate({
	        validate:true,
	        callback:function(){
	        	var treeObj=$.fn.zTree.getZTreeObj("addProductCategoryArea"),
	    		nodes=treeObj.getCheckedNodes(true),
	            ids='';
		    	for(var i=0;i<nodes.length;i++){
	            	ids += nodes[i].id + ",";
	            }
	            var tempIds=ids.substr(0,ids.length-1);
	            $('#addBrand #productCategoryIds').val(tempIds);
				if(dosubmit()){
		    		var $model = $('#addBrand');
			        var operateType =$model.find('input[name=operateType]').val();
			        if(operateType=="update"){
			           $model.find('input[name=operateType]').val("update");//操作
			        }else{
			           $model.find('input[name=operateType]').val("create");//操作
			        }
			        document.updateProductBrand.action="<@ofbizUrl>updateProductBrand</@ofbizUrl>";
			        document.updateProductBrand.submit();
		      	}
	        }
	    })
	    
	    $('.js-button').on('click',function(){
			var $model = $('#addBrand');
			var tr=$(this).closest('tr');
			
			var productBrandId=tr.find('input[name=curProductBrandId]').val();//品牌Id
            var contentId= tr.find('input[name=contentId]').val();//图片Logo ContentId
			var brandName=tr.find('td').eq(2);//品牌名称
		    var brandNameAlias=tr.find('td').eq(3);//品牌别名
		    
			$model.find('input[name=brandName]').val(brandName.text());//品牌名称
			$model.find('input[name=brandNameAlias]').val(brandNameAlias.text());//品牌别名
			
			$model.find('input[name=operateType]').val("update");//操作
			$model.find('input[name=productBrandId]').val(productBrandId);//品牌Id
			$model.find('input[name=contentId]').val(contentId);//图片Logo ContentId
			$model.find('#exampleModalLabel').text("编辑品牌");
			
			var contentId="/content/control/stream?contentId="+contentId;
            $('#addBrand #img').attr('src',contentId);
			//加载商品分类
			getProductBrandCategoryList(productBrandId);
			
		})
		
		$('.js-btn-isused').on('click',function(){
		   
			var $model = $('#addBrand');
			var tr=$(this).closest('tr');
			
			var productBrandId=tr.find('input[name=curProductBrandId]').val();//品牌Id
		    var isUsed= tr.find('input[name=isUsed]').val();//是否启用
		    //alert(productBrandId);
		    $("#productBrandId").val(productBrandId);
		    if(isUsed=="Y"){
		       $("#operateType").val("disable");
		    } else{
		       $("#operateType").val("enabled");
		    }
		    document.editProductBrand.action="<@ofbizUrl>updateProductBrand</@ofbizUrl>";
			document.editProductBrand.submit();
		})
		
		
		$("#btnBrandDel").click(function(){
		    var ids = "";
		    var checks = $('.js-checkparent .js-checkchild:checked');
		    
		     //判断是否选中记录
	    	if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				}); 
				
				var checkedIds =$("#ids").val(); 
		        if (checkedIds != ""){
		           	isBrandForProduct();
		        }
	        }else{
	           //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	  $('#modal_msg').modal();
	        }
		})
		
		//删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
	        delBrandDataById();
	    });
		
		
		
		$("#exportAll").click(function(){
		    var checks = $('.js-checkparent .js-checkchild');
		    
		    var ids ="";
			if(checks.size() > 0 ){
			   <#--document.location.href="<@ofbizUrl>productBrandListReport.xls?ids="+ids+"</@ofbizUrl>";-->
                var url = "exportProductBrandsByIds?ids="+ids;
                window.location.href = url;
			}else{
			   //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("没有可以导出的记录！");
    	  	  $('#modal_msg').modal();
			}
		})
		
		
		
		$("#curPageExport").click(function(){
		    var ids ="";
		    var checks = $('.js-checkparent .js-checkchild');
		    if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				}); 
				var idsView =$("#ids").val();
				if(idsView!=""){
					<#--document.location.href="<@ofbizUrl>productBrandListReport.xls?ids="+idsView+"</@ofbizUrl>";-->
                    var url = "exportProductBrandsByIds?ids="+ids;
                    window.location.href = url;
				}
			}else{
			  //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("没有可以导出的记录！");
    	  	  $('#modal_msg').modal();
			}
		})
		
		
		
		$("#selectedItemExport").click(function(){
		    var ids ="";
		    var checks = $('.js-checkparent .js-checkchild:checked');
		    if(checks.size() > 0 ){
			    $('input[class="js-checkchild"]:checked').each(function(){ 
		            ids = ids + "," + $(this).val();
		            $("#ids").val(ids.substr(1,ids.length));
				}); 
				var idsView =$("#ids").val();
				if(idsView!=""){
				  <#--document.location.href="<@ofbizUrl>productBrandListReport.xls?ids="+idsView+"</@ofbizUrl>";-->
                    var url = "exportProductBrandsByIds?ids="+ids;
                    window.location.href = url;
			    }
		    }else{
		      //设置提示弹出框内容
    	  	  $('#modal_msg #modal_msg_body').html("${uiLabelMap.MustSelectOne}");
    	  	  $('#modal_msg').modal();
		    }
		})
		
		$("#importBrandTemplate").click(function(){
		   <#--
		   document.location.href="<@ofbizUrl>productBrandImportTemplate.xls</@ofbizUrl>";
		   -->
		   window.location="<@ofbizContentUrl>/images/importTemplate/Brand/brandTemplate.xlsx</@ofbizContentUrl>";
		
		})
		
		/*
		$("#btnBrandImp").click(function(){
			var path = $('#uploadedFile').val(); 
		    
		    alert(path);
		    var $model = $('#impBrandByExcel');
		    $model.find('input[name=filePath]').val(path);//路径
		    document.importBrandForm.action="<@ofbizUrl>importProdcutBrandByExcel</@ofbizUrl>";
		    document.importBrandForm.submit();
		})
		*/
		// 上传图片
		$('body').on('click','.img-submit-btn',function(){
           var obj = $.chooseImage.getImgData();
           $.chooseImage.choose(obj,function(data){
           var contentId="/content/control/stream?contentId="+data.uploadedFile0;
              $('#addBrand #img').attr('src',contentId);
              $('#addBrand #contentId').val(data.uploadedFile0)
		      $('#addBrand').modal();
           })
         });
         
         
        // 品牌导入按钮点击事件
		$('#brandImport').click(function(){
			$('#doc').val("");
			$('#modal_import').modal();
		});

		// 初始化图片上传框
		var au = new AjaxUpload($('#btnUpload'), uploadOption);

		// 导入按钮点击事件
		$('#upload').click(function(){
		   //alert("ddd");
		   au.submit();
		}); 
		
    });
    
    // 将选择的记录id删除
    function delBrandDataById(){
	  // 选中的项目
	  var checkedIds =$("#ids").val(); 
	  if (checkedIds != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>delProductBrandByIds</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'checkedIds' : checkedIds
	        },
	        success: function(data) {
	           document.location.href="<@ofbizUrl>ProductBrand?lookupFlag=Y</@ofbizUrl>"; 
	        }
	    });
	  } 
	}
	
	// 取得品牌对应的分类
    function getProductBrandCategoryList(productBrandId){
	    // 选中的项目
		$.ajax({
			url: "<@ofbizUrl>getProductBrandCategoryList</@ofbizUrl>",
			type: "GET",
			data : {productBrandId:productBrandId},
			dataType : "json",
			success: function(cateGoryData){
				//异步加载所有分类数据
				$.ajax({
					url: "<@ofbizUrl>getProductCategoryList</@ofbizUrl>",
					type: "POST",
					dataType : "json",
					success: function(data){
						var treeObj = $.fn.zTree.init($("#addProductCategoryArea"), setting, data.productCategoryList);
						//自动勾选复选框
						$.each(cateGoryData.paramMap.productBrandCategoryList,function(){
							var node = treeObj.getNodeByParam("id", this.productCategoryId);
							if(node){
								treeObj.checkNode(node);
								//自动展开子节点
								if(!node.isParent){
									treeObj.expandNode(node.getParentNode(), true, true, true);
								}
							}else{
							   
							}
							
							
						})
						$('#addBrand').modal();
					},
					error: function(data){
						//设置提示弹出框内容
						$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
	    				$('#modal_msg').modal();
					}
				});
			},
			error: function(data){
				//设置提示弹出框内容
				$('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
				$('#modal_msg').modal();
			}
		});
	}  
	
	
	
   
    
   function dosubmit(){
     if(isCommitted==false){
        isCommitted = true;//提交表单后，将表单是否已经提交标识设置为true
        return true;//返回true让表单正常提交
     }else{
        return false;//返回false那么表单将不提交
     }
  
  }
  //行删除按钮事件
   function del(id){
	    $("#ids").val(id);
	    isBrandForProduct();
	    
	   	//设置删除弹出框内容
		//$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
		//$('#modal_confirm').modal('show');
   } 
   
  function imageManage() {
  	$.chooseImage.int({
	    userId: 3446,
	    serverChooseNum: 5,
	    getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
	    submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
	    submitServerImgUrl: '',
	    submitNetworkImgUrl: ''
	});
	$.chooseImage.show();
    $('#addBrand').modal('hide');

  }
  
  
  // 判断商品品牌是否是被商品使用
  function isBrandForProduct(){
      var ids=$("#ids").val();
	  if (ids != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>isBrandForProduct</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'ids' : ids
	        },
	        success: function(data) {
	           var isUsedFlg=data.isUsedFlg;
	           if(isUsedFlg=="Y"){
	              //设置提示弹出框内容
	    	  	  $('#modal_msg #modal_msg_body').html("该商品品牌已使用，不能删除");
	    	  	  $('#modal_msg').modal();
	           }else{
		       	//设置删除弹出框内容
				$('#modal_confirm #modal_confirm_body').html("${uiLabelMap.IsDel}");
				$('#modal_confirm').modal('show');
	           }
	        }
	    });
	  } 
   }  	
  
</script>

<#assign commonUrl = "ProductBrand?lookupFlag=Y"+ paramList +"&">

<div class="box box-info">
    <#--
	<div class="box-header with-border">
	  <h3 class="box-title">${uiLabelMap.ProductBrand}</h3>
	</div>
	-->
	<div class="box-body">
    	<form class="form-inline clearfix" role="form" action="<@ofbizUrl>ProductBrand</@ofbizUrl>">
        	<div class="form-group">
              <div class="input-group m-b-10">
                <span class="input-group-addon">${uiLabelMap.BrandName}</span>
                <input type="text" class="form-control" value="${requestParameters.brandName?if_exists}" id="brandName" name="brandName"/>
              </div>
              
              <div class="input-group m-b-10">
                <span class="input-group-addon">${uiLabelMap.BrandNameAlias}</span>
                <input type="text" class="form-control" value="${requestParameters.brandNameAlias?if_exists}" id="brandNameAlias" name="brandNameAlias"/>
              </div>
            </div>
            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
            </div>
        </form>
        <div class="cut-off-rule bg-gray"></div>
            <div class="row m-b-10">
                <div class="col-sm-6">
                    <div class="dp-tables_btn">
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_BRAND", "_CREATE", session)> 
	                        <button id="btn_add" class="btn btn-primary"  data-toggle="modal" data-target="#addBrand" >
					            <i class="fa fa-plus"></i>${uiLabelMap.BrandCreate}
				        	</button>
				        </#if>
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_BRAND", "_DEL", session)>
                        <button class="btn btn-primary" id="btnBrandDel"> 
                            <i class="fa fa-trash"></i>
	                        ${uiLabelMap.BrandDel}
	                    </button>
	                    </#if>
                        <#if security.hasEntityPermission("PRODUCTGOODMGR_BRAND", "_IMP",session)>
                        <button class="btn btn-primary" id="importBrandTemplate">${uiLabelMap.BrandTemplate}</button>
                        <button class="btn btn-primary" id="brandImport">${uiLabelMap.BrandImport}</button>
                        </#if>
                        <div class="btn-group">
                            <#if security.hasEntityPermission("PRODUCTGOODMGR_BRAND", "_EXP",session)>
	                        <button type="button" class="btn btn-primary" id="exportAll">${uiLabelMap.ExportAll}</button>
	                        <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
	                            <span class="caret"></span>
	                            <span class="sr-only">Toggle Dropdown</span>
	                        </button>
	                        <ul class="dropdown-menu" role="menu">
	                            <li><a href="#" id="curPageExport" >${uiLabelMap.ExprotCurPage}</a></li>
	                            <li><a href="#" id="selectedItemExport">${uiLabelMap.ExportSelectedItem}</a></li>
	                        </ul>
	                        </#if>
	                    </div>
               	    </div>
                </div>
                
            </div>
        </div>
        
        <#if productBrandList?has_content>
		   <input id="ids" type="hidden"/>
           <div class="row m-b-10">
                <div class="col-sm-12">
                    <div class="dp-tables_length">
                        <label>
                            ${uiLabelMap.DisplayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="5" <#if viewSize ==5>selected</#if>>5</option>
                                <option value="15" <#if viewSize==15>selected</#if>>15</option>
                                <option value="20" <#if viewSize==20>selected</#if>>20</option>
                                <option value="25" <#if viewSize==25>selected</#if>>25</option>
                            </select>
                            ${uiLabelMap.BrandItem}
                        </label>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-12">
                    <table class="table table-bordered table-hover js-checkparent js-sort-list">
                        <thead>
                        <tr>
                            <th><input class="js-allcheck" type="checkbox" id="checkAll"></th>
                            <th>${uiLabelMap.BrandLogo}
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="contentId" id="a_contentId"></a>
                                <#--
                                <#if orderFiled == 'contentId'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=contentId&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=contentId&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=contentId&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>${uiLabelMap.BrandName}
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="brandName" id="a_brandName"></a>
                                <#--
                                <#if orderFiled == 'brandName'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=brandName&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=brandName&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=brandName&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>${uiLabelMap.BrandNameAlias}
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="brandNameAlias" id="a_brandNameAlias"></a>
                                <#--
                                <#if orderFiled == 'brandNameAlias'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=brandNameAlias&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=brandNameAlias&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=brandNameAlias&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>${uiLabelMap.BrandIsUsed}
                                <a class="fa fa-sort-amount-desc text-muted js-sort" data-key="isUsed" id="a_isUsed"></a>
                                <#--
                                <#if orderFiled == 'isUsed'>
                                    <#if orderBy == 'DESC'>
                                        <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=isUsed&amp;ORDER_BY=ASC"></a>
                                    <#else>
                                        <a class="fa fa-sort-amount-asc" href="${commonUrl}ORDER_FILED=isUsed&amp;ORDER_BY=DESC"></a>
                                    </#if>
                                <#else>
                                    <a class="fa fa-sort-amount-desc" href="${commonUrl}ORDER_FILED=isUsed&amp;ORDER_BY=ASC"></a>
                                </#if>
                                -->
                            </th>
                            <th>${uiLabelMap.BrandOption}</th>
                        </tr>
                        </thead>
                        <tbody>
                            <form method="post" action="" name="editProductBrand" id="editProductBrand">
	                            <#list productBrandList as productBrandRow>
		                            <tr>
				                        <td><input class="js-checkchild" type="checkbox" id="${productBrandRow.productBrandId?if_exists}" value="${productBrandRow.productBrandId?if_exists}"/>
				                            <input type="hidden" name="curProductBrandId" id="curProductBrandId" value="${productBrandRow.productBrandId?if_exists}"/>
				                        </td>
				                        <td>
			                               <#if productBrandRow.contentId?has_content>
			                               
			                                   <#assign src='/content/control/stream?contentId='>
                            				   <#assign imgsrc = src +productBrandRow.contentId/>

			                                  <#---
					                          <#assign contents = delegator.findByAnd("Content", {"contentId" : "${productBrandRow.contentId}"})/>
					                          <#if contents?has_content>
					                               <input type="hidden" name="contentId" id="contentId" value="${productBrandRow.contentId?if_exists}"/>
					                               <#assign content = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(contents) />
					                               <#assign dataResource = delegator.findByPrimaryKey("DataResource",Static["org.ofbiz.base.util.UtilMisc"].toMap("dataResourceId","${content.dataResourceId}"))/>
					                          </#if>
					                          -->
					                          <input type="hidden" name="contentId" id="contentId" value="${productBrandRow.contentId?if_exists}"/>
					                          <img height="100" src="${imgsrc}" class="cssImgSmall" alt="" />
					                       <#else>
					                          <img height="100" src="<@ofbizContentUrl>/images/products/10000/original.jpg</@ofbizContentUrl>" class="cssImgSmall" alt="" />
					                       </#if>
				                        </td>
				                        <td>${productBrandRow.brandName?if_exists}</td>
				                        <td>${productBrandRow.brandNameAlias?if_exists}</td>
				                        <td>
				                            <input type="hidden" name="isUsed" id="isUsed" value="${productBrandRow.isUsed?if_exists}"/>
				                            <#if security.hasEntityPermission("PRODUCTGOODMGR_BRAND", "_UPDATE", session)>
	                                             <#if productBrandRow.isUsed=="N">
					                               <button type="button" class="js-btn-isused btn btn-primary">${uiLabelMap.BrandUnUsed}</button>
					                             <#else>
					                               <button type="button" class="js-btn-isused btn btn-primary">${uiLabelMap.BrandUsed}</button> 
					                             </#if>
                                             <#else>
	                                             <#if productBrandRow.isUsed=="N">
					                               <button type="button" class="js-btn-isused btn btn-primary" disabled="disabled">${uiLabelMap.BrandUnUsed}</button>
					                             <#else>
					                               <button type="button" class="js-btn-isused btn btn-primary" disabled="disabled">${uiLabelMap.BrandUsed}</button> 
					                             </#if>
                                             </#if>
				                        </td>
				                        <td>
				                        	<div class="btn-group">
				                        	    <#if security.hasEntityPermission("PRODUCTGOODMGR_BRAND", "_UPDATE", session)>
					                            <button type="button" class="js-button btn btn-danger btn-sm" >${uiLabelMap.BrandEdit}</button>
					                            </#if>
                                                <button type="button" class="btn btn-danger btn-sm dropdown-toggle" data-toggle="dropdown">
					                              <span class="caret"></span>
					                              <span class="sr-only">Toggle Dropdown</span>
					                            </button>
					                            <ul class="dropdown-menu" role="menu">
					                              <#--
					                              <li><a href="<@ofbizUrl>updateProductBrand?productBrandId=${productBrandRow.productBrandId?if_exists}&operateType=delete</@ofbizUrl>">${uiLabelMap.BrandDel}</a></li>
					                              -->
                                                  <#if security.hasEntityPermission("PRODUCTGOODMGR_BRAND", "_DEL", session)>
					                              <li><a href="javascript:del(${productBrandRow.productBrandId?if_exists})">${uiLabelMap.BrandDel}</a></li>

                                                  </#if>
					                            </ul>
				                        	</div>
				                        </td>
			                        </tr>
	                            </#list>
	                            <input type="hidden" name="productBrandId" id="productBrandId" value=""/>
		                        <input type="hidden" name="operateType" id="operateType"/>
                            </form>
                        </tbody>
                    </table>
                </div>
            </div>
            <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
            <#assign viewIndexFirst = 0/>
            <#assign viewIndexPrevious = viewIndex - 1/>
            <#assign viewIndexNext = viewIndex + 1/>
            <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(productBrandListSize, viewSize) />
            <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", productBrandListSize)/>
            <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
            <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
            listSize=productBrandListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
            pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
            paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />    
        <#else>
            <div id="">
                <h3>没有商品品牌数据</h3>
            </div>
        </#if>
        </div><!-- /.box-body -->
    </div>



    
   <div class="modal fade" id="addBrand" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
   		<div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">${uiLabelMap.CreateBrand}</h4>
            </div>
            <div class="modal-body">
                 <form class="form-horizontal"  id="updateProductBrand" method="post" action="" name="updateProductBrand" enctype="multipart/form-data">
                 
				    <div class="form-group" data-type="required" data-mark="品牌名称">
			            <label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.BrandName}:</label>
			            <div class="col-sm-10">
			               <input type="text" class="form-control dp-vd" name="brandName" id="brandName" value="${parameters.brandName?if_exists}">
			               <p class="dp-error-msg"></p>
			            </div>                
			        </div>
				    
                    <div class="form-group">
                		<label for="message-text" class="control-label col-sm-2">${uiLabelMap.BrandNameAlias}:</label>
		                <div class="col-sm-10">
		                  <input type="text" class="form-control" name="brandNameAlias" id="brandNameAlias">
		                </div>
              		</div>
              		  
              		<!-- Log图片 start-->
              		<div class="form-group">
                		<label class="control-label col-sm-2">${uiLabelMap.LogoPic}</label>
		                <div class="col-sm-3">
		                  <button type="button" class="btn btn-primary" id="btnAddPic"  onclick="imageManage()">${uiLabelMap.AddPic}</button>
		                </div>
                        <div class="col-sm-7">
                            <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 300*200px</div>
                        </div>
              		</div>
              		
	                <div class="form-group" >
	                     <div class="col-sm-2">
	                      </div>
	                     <div class="col-sm-10">
	                     <img height="50" alt="" src="" id="img" style="height:100px;width:100px;">
	                     </div>
	                </div>
                    <!-- Log图片 end-->
              		
              		<#--
              		<div class="form-group">
			            <label class="control-label col-sm-2"><span style="color: red">*</span>${uiLabelMap.LogoPic}</label>
			            <div class="col-sm-10">
			            </div>                
			        </div>
			        <div class="form-group">
			            <label class="control-label col-sm-2">&nbsp;</label>
			            <div class="col-sm-10">
			                <input type="file" class="form-control" id="uploadedFile" name="uploadedFile">
			            </div>                
			        </div>
			        -->
              		<div class="form-group">
			          	<input type="hidden" id="productCategoryIds" name="productCategoryIds"/>
			            <label class="control-label col-sm-2">商品分类:</label>
			            <div class="col-sm-10">
			              <div class="zTreeDemoBackground left">
			                <ul id="addProductCategoryArea" class="ztree"></ul>
			              </div>
			            </div>                
			        </div>
              		
              		<input id="productId" type="hidden" name="productId" value="GoodsBrand" />
                    <input id="productContentTypeId" type="hidden" name="productContentTypeId" value="GOODSBRAND_IMG"/>
              		<input type="hidden" name="operateType" id="operateType" value=""/>
					<input type="hidden" name="productBrandId" value=""/>
                    <input type="hidden" name="contentId" id="contentId" value=""/>
	            </form>
	          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            <button type="button" class="btn btn-primary" id="btnBrandSave">${uiLabelMap.BrandSave}</button>
          </div>
        </div>
      </div>
    </div>
    
    
    
    
    
    <div class="modal fade" id="impBrandByExcel" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
   		<div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">添加商品品牌信息</h4>
            </div>
            <div class="modal-body">
                  <form id="importBrandForm" class="form-horizontal" method="post" action="<@ofbizUrl>importProdcutBrandByExcel</@ofbizUrl>" name="importBrandForm" enctype="multipart/form-data">
                    <div class="form-group">
	                    <label for="recipient-name" class="control-label col-sm-1"></label>
		                <div class="col-sm-10">
		                  <input type="file" class="form-control" id="uploadedFile" name="uploadedFile">
		                  <p class="help-block">(支持xls和xlsx格式的Excel文件)</p>
		                </div>  
		                <input type="hidden" name="filePath" id="filePath" value=""/>
		               <div class="modal-footer">
			            <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
			            <button type="submit" class="btn btn-primary" id="btnBrandImp">上传</button>
			          </div>           
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
	
	<!-- 导入弹出框start -->
	<div id="modal_import"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_import_title">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
					<h4 class="modal-title" id="modal_import_title">${uiLabelMap.InventoryImport}</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal">
						<div class="form-group">
							<label class="control-label col-sm-2">${uiLabelMap.SelectFile}:</label>
							<div class="col-sm-10 uploadFile">
								<input type="text" class="form-control w-p80" style="float: left" disabled id="doc">
								<input type="hidden" id="hidFileName" />
								<input type="button" class="btn btn-default m-l-5" id="btnUpload" value="${uiLabelMap.Upload}" />
							</div>                
						</div>
			        </form>
				</div>
				<div class="modal-footer">
					<button id="upload" type="button" class="btn btn-primary">${uiLabelMap.Import}</button>
					<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
				</div>
			</div>
		</div>
	</div><!-- 导入弹出框end -->
	
	<!-- 导入错误提示框start -->
	<div id="modal_error"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_error_title">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
					<h4 class="modal-title" id="modal_error_title">${uiLabelMap.Error}</h4>
				</div>
				<div class="modal-body">
					<form class="form-horizontal">
						<!-- 错误提示start -->
						<div class="row" id="error_list">
							<div class="col-xs-12">
								<div class="box" style="border-top:0px">
						            <div class="box-header">
						              <h3 class="box-title" style="color: red">
						              	<icon class="fa fa-times-circle"></icon>${uiLabelMap.ImportFail}
						              </h3>
						            </div><!-- /.box-header -->
						            <div class="box-body no-padding" style="border: 1px solid #ddd;overflow-y: auto;height: 350px;" align="left">
						              <table class="table table-condensed" >
						                <tbody>
						              	</tbody>
						              </table>
						            </div><!-- /.box-body -->
						        </div>
							</div>
						</div><!-- 错误提示end -->
			        </form>
				</div>
				<div class="modal-footer">
					<button id="importAgain" type="button" class="btn btn-primary">${uiLabelMap.ImportAgain}</button>
					<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
				</div>
			</div>
		</div>
	</div><!-- 导入错误提示框end -->	
