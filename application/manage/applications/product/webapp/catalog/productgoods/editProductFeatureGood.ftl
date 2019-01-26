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

<!-- Select2 -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.min.css</@ofbizContentUrl>">
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/dist/css/AdminLTE.min.css</@ofbizContentUrl>">
<!-- Select2 -->
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/select2/select2.full.min.js</@ofbizContentUrl>"></script>

<div class="box box-info">
	<div class="box-header with-border">
	  <h3 class="box-title">商品信息</h3>
	</div>
	<div class="box-body">
	    
		
	    <form class="form-horizontal" method="post"  role="form" action="" name="updateProductFeatureGoods"  id="updateProductFeatureGoods"  class="" enctype="multipart/form-data">
            <input type="hidden" name="operateType" id="operateType"  value="${parameters.operateType?if_exists}"/>
		 	<input type="hidden" name="productIdMain"   id="productIdMain"  value="${parameters.productId?if_exists}"/>
			<input type="hidden" name="productTypeId"   id="productTypeId"  value="${parameters.productTypeId?if_exists}"/>
		 	<input type="hidden" name="productCategoryId"   id="productCategoryId"  value="${parameters.productCategoryId?if_exists}"/>
			<input type="hidden" name="productNameMain"   id="productNameMain"  value="${parameters.productName?if_exists}"/>
			<input type="hidden" name="productSubheadName"   id="productSubheadName"  value="${parameters.productSubheadName?if_exists}"/>
			<input type="hidden" name="isOnline"   id="isOnline"  value="${parameters.isOnline?if_exists}"/>
			<input type="hidden" name="startTime"   id="startTime"  value="${parameters.startTime?if_exists}"/>
			<input type="hidden" name="endTime"   id="endTime"  value="${parameters.endTime?if_exists}"/>
			<input type="hidden" name="businessPartyId"   id="businessPartyId"  value="${parameters.businessPartyId?if_exists}"/>
			<input type="hidden" name="brandId"   id="brandId"  value="${parameters.brandId?if_exists}"/>
			<input type="hidden" name="volumeMain"   id="volumeMain"  value="${parameters.volume?if_exists}"/>
			<input type="hidden" name="weightMain"   id="weightMain"  value="${parameters.weight?if_exists}"/>
			<input type="hidden" name="isUsedFeature"   id="isUsedFeature"  value="${parameters.isUsedFeature?if_exists}"/>
			<input type="hidden" name="seoKeyword"   id="seoKeyword"  value="${parameters.seoKeyword?if_exists}"/>
			<input type="hidden" name="salePriceMain"   id="salePriceMain"  value="${parameters.salePrice?if_exists}"/>
			<input type="hidden" name="marketPriceMain"   id="marketPriceMain"  value="${parameters.marketPrice?if_exists}"/>
			<input type="hidden" name="costPriceMain"   id="costPriceMain"  value="${parameters.costPrice?if_exists}"/>
			<input type="hidden" name="productTags"   id="productTags"  value="${parameters.productTags?if_exists}"/>
			<input type="hidden" name="productParameterInfos"   id="productParameterInfos"  value="${parameters.productParameterInfos1?if_exists}"/>
			<input type="hidden" name="productAttrInfos"   id="productAttrInfos"  value="${parameters.productAttrInfos?if_exists}"/>
			<input type="hidden" name="productFacilityInfos"   id="productFacilityInfos"  value="${parameters.productFacilityInfos?if_exists}"/>
			<input type="hidden" name="pcDetails"   id="pcDetails"  value="${parameters.pcDetails?if_exists}"/>
			<input type="hidden" name="mobileDetails"   id="mobileDetails"  value="${parameters.mobileDetails?if_exists}"/>
			<input type="hidden" name="productFeatureInfos"   id="productFeatureInfos"  value="${parameters.productFeatureInfos?if_exists}"/>
			<input type="hidden" name="productContentInfos"   id="productContentInfos"  value="${parameters.productContentInfos?if_exists}"/>
			<input type="hidden" name="productAssocInfos"   id="productAssocInfos" value="${parameters.productAssocInfos?if_exists}"/>

            <input type="hidden" name="productFacilityInfosForFeatureGoods" id="productFacilityInfosForFeatureGoods" value=""/>
            <input type="hidden" name="productFeatureGoodsInfos" id="productFeatureGoodsInfos" value=""/>
			
            <div class="row m-b-10">
                <div class="col-sm-6">
                    <div class="dp-tables_btn js-btn-group">
                    
               	    </div>
                </div>
	        </div>
            <!-- 分割线start -->
		    <div class="cut-off-rule bg-gray"></div>
		    <!-- 分割线end -->
            
            <div class="content-feature">
            
            </div>
            
	       <div class="modal-footer">
	            <button type="button" class="btn btn-primary" id="btnGoBack">上一步</button>
	            <button type="button" class="btn btn-primary" id="btnAddSku">新增sku</button>
	            <button type="button" class="btn btn-primary" id="btnProductGoodSave">保存</button>
	       </div>
	   </form>
    </div><!-- /.box-body -->
</div>

<div class="modal fade" id="addFacility" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">选择仓库</h4>
        </div>
        <div class="modal-body">
             <input type="hidden" name="conId" id="conId" value=""/>
             
             
             <form class="form-horizontal"  id="updateProductFacility" method="post" action="" name="updateProductFacility" enctype="multipart/form-data">
                
                <#assign productFacilityList = delegator.findByAnd("Facility")/>
			    <div class="form-group" data-type="required" data-mark="选择仓库">
		            <label class="control-label col-sm-2"><span style="color: red">*</span>选择仓库:</label>
		            <div class="col-sm-10">
		                <select class="form-control dp-vd" id="facilityname" name="facilityname">
	                        <#list productFacilityList as productFacility >
	                        <option value="${productFacility.facilityId?if_exists}">${productFacility.facilityName?if_exists}</option>
	                        </#list>
	                    </select>
	                    <p class="dp-error-msg"></p>
		            </div>                
		        </div>
			    
                <div class="form-group" data-type="required" data-mark="可用库存">
            		<label for="message-text" class="control-label col-sm-2"><span style="color: red">*</span>可用库存:</label>
	                <div class="col-sm-10">
	                  <input type="text" class="form-control dp-vd" name="accountingQuantityTotal" id="accountingQuantityTotal">
	                  <p class="dp-error-msg"></p>
	                </div>
          		</div>
          		
          		<div class="form-group">
            		<label for="message-text" class="control-label col-sm-2">库存预警数量:</label>
	                <div class="col-sm-10">
	                  <input type="text" class="form-control" name="warningQuantity" id="warningQuantity">
	                </div>
          		</div>
          		
          		<input type="hidden" name="operateType" id="operateType" value=""/>
            </form>
          </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
        <button type="button" class="btn btn-primary" id="btnFacilitySave">${uiLabelMap.BrandSave}</button>
      </div>
    </div>
  </div>
</div>


<div class="modal fade" id="addProductSku" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
	<div class="modal-dialog" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title" id="exampleModalLabel">新增Sku</h4>
        </div>
        <div class="modal-body">
             <input type="hidden" name="conId" id="conId" value=""/>
             
             
             <form class="form-horizontal"  id="updateProductSku" method="post" action="" name="updateProductSku" enctype="multipart/form-data">
                
          		 <div class="row">
		            <div class="form-group">
		                <label  class="col-sm-2 control-label"><i class="required-mark">*</i>特征名称:</label>
		                <div class="col-sm-4">
		                    <#assign productFeatureTypeList = delegator.findByAnd("ProductFeatureType")/>
		                    <select class="form-control select2Feature" multiple="multiple" data-placeholder="Select a State">
		                      <#list productFeatureTypeList as productFeatureType>
		                          <option value="${productFeatureType.productFeatureTypeId?if_exists}">${productFeatureType.productFeatureTypeName?if_exists}</option>
		                      </#list>
		                    </select>
		                </div>
		            </div>
	            </div>
	            
	            <#--特征值-->
	            <div class="row">
	               <div class="form-group">
	               <label  class="col-sm-2 control-label"></label>
	                <div class="col-sm-8 js-feature">
	                 
	                </div>
	               </div>
	            </div>
            </form>
          </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
        <button type="button" class="btn btn-primary" id="btnProductSku">新增</button>
      </div>
    </div>
  </div>
</div>



<script language="JavaScript" type="text/javascript">

    var featureNamelistSize=0;
    $(function(){
       var ids="${parameters.ids?if_exists}";
        $(".select2Feature").select2({
	        tags: true,
	        maximumSelectionLength: 3  //最多能够选择的个数
	    });
        getProductFeatureNames(ids);
    
        //商品特征事件
        var $eventSelect = $(".select2Feature");
        //商品特征的选择
        $eventSelect.on("select2:select", function (e) {
         addFeature("select2:select", e);
        });
        // 商品特征的删除
        $eventSelect.on("select2:unselect", function (e) { 
           delFeature("select2:unselect", e); 
        });
        
        
        $('.js-btn-group').on('click','.btn',function(){
			var i = $(this).index();
			var dc= '.con'+i;
			//alert(i);
			$('.content-feature').find(dc).show().siblings().hide();
			return false;
		});
		
		
		//添加库存
		$('.content').on('click','.js-facility',function(){
			//alert("dd");
			var curClass=$(this).parent().parent().parent().parent().attr("class");
			//alert(curClass);
			var $model = $('#addFacility');
			$model.find('input[name=conId]').val(curClass);//品牌名称
			
            //设置弹出框内容
			$('#addFacility').modal();
		});
        
        $("#btnFacilitySave").click(function(){
           $("#updateProductFacility").submit();
        });
        
	    $("#updateProductFacility").dpValidate({
	        validate:true,
	        callback:function(){
	        	var $model = $('#addFacility');
	        	var conId=$model.find('input[name=conId]').val();
				var facilitynameId =$model.find('select[name=facilityname]').val();//仓库Id
				var facilityname =$model.find('select[name=facilityname] option:selected').text();//仓库名称
				var accountingQuantityTotal=$model.find('input[name=accountingQuantityTotal]').val();//可用库存
				var warningQuantity=$model.find('input[name=warningQuantity]').val();//预警库存
				
				//alert(facilityname);
				//alert(accountingQuantityTotal);
				//alert(warningQuantity);
	             
	            var tr=' <tr><td><input type="hidden" name="" id="" value="'+conId+'"><input type="hidden" name="facilitynameId" id="" value="'+facilitynameId+'">'+facilityname+'</td><td>'+accountingQuantityTotal+'</td><td>0</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td><td class="fc_td"><button type="button" class="js-button-facility btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	 			var className='.'+conId;
	 			$(className).find('.js-table_3>tbody').append(tr);
	 			//$('.js-table_3>tbody').append(tr);
	 			$('#addFacility').modal('hide');
		    }
	    })
	    
        //删除按钮事件
  	    $(document).on('click','.js-button-facility',function(){
  	       $(this).parent().parent().remove();
        })
        
        $("#btnProductGoodSave").click(function(){
        
            //alert("ddd");
            // 取得特征商品登录信息
            productFeatureGoodsInfos();
            
            document.updateProductFeatureGoods.action="<@ofbizUrl>updateProductFeatureGoods</@ofbizUrl>";
			document.updateProductFeatureGoods.submit();
        })
        // 添加商品sku
        $('.content').on('click','#btnAddSku',function(){
          
			$('#addProductSku').modal();
        })
        
        
        $("#btnProductSku").click(function(){
           getSelectedFeatures();
        })
        
        $('#addProductSku').on('shown.bs.modal', function (e) {
		    //设置弹出框内容
            $(".select2Feature").select2({
		        tags: true,
		        maximumSelectionLength: 3  //最多能够选择的个数
		    });
		})
		
		
		
		
		
		//删除特殊商品
		$('.content').on('click','.js-feature-del',function(){
			//var curClass=$(this).parent().parent().parent().parent().attr("class");
			//alert(curClass);
			//var curClassName="."+curClass;
			//var curId="#"+curClass;
			//$(curClassName).hide();
			//$(curId).hide();
			//$(curClassName).find('input[name=operateType]').val("delete");
			//$('.content-feature').find(".con0").show().siblings().hide();
			
		});
    })
    
    
    // 取得选择的特征值
   function getProductFeatureNames(ids){
       
		jQuery.ajax({
	        url: '<@ofbizUrl>getProductFeatureGoodsByFeature</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'ids' : ids
	        },
	        success: function(data) {
	           var map=data.productFeatureInfo;
	           var productFeatureNameList=map.productFeatureNameList;
	           var listSize=map.listSize;
	           featureNamelistSize=map.listSize;
	           $('.js-btn-group').find("button").remove();
	           $('.content-feature').find("div").remove();
	           for(var i=0;i<listSize;i++){
	               var conId="con"+i;
	               var name=productFeatureNameList[i].productFeatureName;
	               //alert(name);
	               var btn='<button id="'+conId+'" class="btn btn-primary">'+name+'</button>';
	               $('.js-btn-group').append(btn);
	               
	               
	               
	               var goodContent='';
	               goodContent=goodContent+
	               '<div class="'+conId+'">'+
	                   '<input type="hidden" id="featureProductName" name="featureProductName" value="'+name+'">'+
	                   '<input type="hidden" id="operateTypeSub" name="operateTypeSub" value="create">'+
                       '<div class="row">'+
						    '<div class="form-group">'+
				                '<label for="number" class="col-sm-2 control-label"><i class="required-mark">*</i>商品编号:</label>'+
				                '<div class="col-sm-3">'+
				                     '<input type="text" class="form-control" id="productId" name="productId" value="" readonly>'+
				                '</div>'+
				                '<div class="col-sm-3">'+
				                '</div>'+
				                '<div class="col-sm-3">'+
				                     '<button type="button" class="btn btn-default js-feature-del" id="productFeatureDel" name="productFeatureDel">删除</button>'+
				                '</div>'+
				                
				            '</div>'+
			           '</div>'+
	                
	                
	                   '<div class="row">'+
			                '<div class="form-group col-sm-6" data-type="required" data-mark="商品名称">'+
			                    '<label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>商品名称:</label>'+
			                    '<div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:15px;width:48.5%;">'+
			                        '<input type="text" class="form-control dp-vd" id="productName" name="productName" value="">'+
			                        '<p class="dp-error-msg"></p>'+
			                    '</div>'+
			                '</div>'+
	                   '</div>'+
	                   
	                   '<div class="row">'+
			                '<div class="form-group">'+
				                '<label  class="col-sm-2 control-label">商品库存:</label>'+
				                '<div class="col-sm-3">'+
				                    '<button type="button" class="btn btn-primary js-facility">选择仓库1'+name+'</button>'+
				                '</div>'+
				            '</div>'+
			           '</div>'+
			           
			           
			           '<div class="row">'+
			                '<div class="form-group">'+
			                   '<label  class="col-sm-2 control-label"></label>'+
			                   '<div class="col-sm-8">'+
			                      '<table class="table table-bordered table_b_c js-table_3">'+
								      '<thead>'+
								           '<tr>'+
								               '<th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>${uiLabelMap.BrandOption}</th>'+
								           '</tr>'+
								      '</thead>'+
								      '<tbody>'+
								      '</tbody>'+
					    		  '</table>'+
			                   '</div>'+
			                '</div>'+
			           '</div>'+
			           
			           '<div class="form-group">'+
			                '<label  class="col-sm-2 control-label">销售价格(元):</label>'+
			                '<div class="col-sm-3">'+
			                    '<input type="text" class="form-control" id="salePrice" name="salePrice" value="" placeholder="" />'+
			                '</div>'+
			                
			                '<label  class="col-sm-2 control-label">成本价格(元):</label>'+
			                '<div class="col-sm-3">'+
			                    '<input type="text" class="form-control" id="costPrice" name="costPrice" value="" placeholder="" />'+
			                '</div>'+
			           '</div>'+
			           
			           '<div class="form-group">'+
			                '<label  class="col-sm-2 control-label">市场价格(元):</label>'+
			                '<div class="col-sm-3">'+
			                    '<input type="text" class="form-control" id="marketPrice" name="marketPrice" value="" placeholder="" />'+
			                '</div>'+
			                
			                '<label  class="col-sm-2 control-label">重量(kg):</label>'+
			                '<div class="col-sm-3">'+
			                    '<input type="text" class="form-control" id="weight" name="weight" value="" placeholder="" />'+
			                '</div>'+
			            '</div>'+
			            
			            '<div class="form-group">'+
			                '<label  class="col-sm-2 control-label">体积(m³):</label>'+
			                '<div class="col-sm-3">'+
			                    '<input type="text" class="form-control" id="volume" name="volume" value="" placeholder="" />'+
			                '</div>'+
			            '</div>'+
	                
	                
	                    '<div class="row">'+
				            '<div class="form-group">'+
				               '<label  class="col-sm-2 control-label"><i class="required-mark">*</i>商品图片:</label>'+
				                '<div class="col-sm-3">'+
				                    '<button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>'+
				               '</div>'+
				                '<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+
				            '</div>'+
			            '</div>'+
	                '</div>';
	                
	                $('.content-feature').append(goodContent);
	                $('.content-feature').find(".con0").show().siblings().hide();
	               
	           
	           }
	           
	           //设置弹出框内容
			   //$('#selectSku').modal();
			   
	        }
	    });
   }


    // 取得仓库分信息
	function facilityInfos(model){
	    var tFacilityInfos = "";
        var curFacilityInfo="";
        model.find('.js-table_3>tbody').find("tr").each(function(){
           // 读取tbody中tr的内容
           curFacilityInfo="";
	       var tdArr = $(this).children();
	       var facilityId = tdArr.eq(0).find("input[name=facilitynameId]").val();//仓库ID
	       var accountingQuantityTotal= tdArr.eq(3).text();//可用数量
	       var warningQuantity= tdArr.eq(4).text();//预警数量
	       curFacilityInfo=facilityId+"|"+accountingQuantityTotal+"|"+warningQuantity;
           tFacilityInfos = tFacilityInfos + "," + curFacilityInfo;
	    }); 
	    var facilityInfos=tFacilityInfos.substr(1,tFacilityInfos.length);
	    $("#productFacilityInfosForFeatureGoods").val(facilityInfos);
	}


   
    // 取得特征商品信息信息
	function productFeatureGoodsInfos(){
	    var tproductFeatureGoodsInfos = "";
        var curFeatureGoodsInfosInfo="";
        $('.content-feature>div').each(function(){
           // 读取div的内容
           curFeatureGoodsInfosInfo="";
	       var $model = $(this);
	       
	    
	       var productId =$model.find('input[name=productId]').val();//商品编号
		   var productName =$model.find('input[name=productName]').val();//商品名称
		   var volume =$model.find('input[name=volume]').val();//体积
		   var weight =$model.find('input[name=weight]').val();//重量
		   var salePrice =$model.find('input[name=salePrice]').val();//销售价格
		   var marketPrice =$model.find('input[name=marketPrice]').val();//市场价格
		   var costPrice =$model.find('input[name=costPrice]').val();//成本价格
		   var featureProductName =$model.find('input[name=featureProductName]').val();//商品特征名
		   var operateTypeSub =$model.find('input[name=operateTypeSub]').val();//特征商品操作类型
		   facilityInfos($model);
		   
		   //var className='.'+conId;
	 	   //$(className).find('.js-table_3>tbody').append(tr);
		   var productFacilityInfos =$("#productFacilityInfosForFeatureGoods").val();//仓库信息
		   //var productContentInfos =$model.find('input[name=productContentInfos]').val();//图片信息
	       
	       curFeatureGoodsInfosInfo=productId+"^"+
	                                productName+"^"+
	                                volume+"^"+
	                                weight+"^"+
	                                salePrice+"^"+
	                                marketPrice+"^"+
	                                costPrice+"^"+
	                                featureProductName+"^"+
	                                operateTypeSub+"^"+
	                                productFacilityInfos;
	       
           tproductFeatureGoodsInfos = tproductFeatureGoodsInfos + "*" + curFeatureGoodsInfosInfo;
	    }); 
	    var productFeatureGoodsInfos=tproductFeatureGoodsInfos.substr(1,tproductFeatureGoodsInfos.length);
	    //alert(productFeatureGoodsInfos);
	    $("#productFeatureGoodsInfos").val(productFeatureGoodsInfos);
	}
    
    
    // 商品特征的添加 
   function addFeature(name, evt) {
      if (evt) {
         var featureName=evt.params.data.text;
         var featureId=evt.params.data.id;
         getProductFeatureListById(featureId,featureName)
      }
   }
   // 商品特征的删除
   function delFeature(name, evt) {
      if (evt) {
         var featureName=evt.params.data.text;
         var featureId=evt.params.data.id;
         var aad="#"+featureId;
         $(aad).remove();
      }
  }
  
   // 取得商品特征信息
   function getProductFeatureListById(productFeatureTypeId,featureName,featureIds){
	  if (productFeatureTypeId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductFeatureListById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productFeatureTypeId' : productFeatureTypeId,
	             'featureIds' :featureIds
	        },
	        success: function(data) {
	           var map=data.productFeatureInfo;
	           var productFeatureList=map.productFeatureList;
	           var trCount=map.trCount;
	           var listSize=map.listSize;
	           var featureIdList=map.featureIdList;
	           
	           var $eventLog = $(".js-feature");
	           var tbDiv=' <div id="'+productFeatureTypeId+'">'+
                ' <table class="table table-bordered table_b_c">'+
                ' <thead>'+
                ' <tr>'+
                ' <th style="text-align:left;">'+featureName+'</th> '
				' </tr>'+
				' </thead>';
				var tbBodyS =' <tbody>';
				var tr=	''; 
				if(listSize>0){
				    for (var i=0;i<trCount;i++){
				       tr=tr+'<tr>'; 
				       for(var j=i*3;j<(i+1)*3;j++){
				           if(j<listSize){
				           var curFeatureName= "featureNameGood"+productFeatureTypeId;
				           var productFeature=productFeatureList[j];
                              tr+='<td><input class="js-productFeature" type="radio" name="'+curFeatureName+'" value="'+productFeature.productFeatureName+'"/><input type="hidden" name="" id="" value="'+productFeature.productFeatureId+'"></td>'+
					              '<td><input type="text" name="productFeatureName" id="" value="'+productFeature.productFeatureName+'"  disabled="true"></td>';
				           }
				       }
				       tr=tr+'</tr>';
				    }   
				}  
				var tbBodyE	=' </tbody>'+
						' </table>'+
						' </div>'; 
				var all=tbDiv+tbBodyS+tr+tbBodyE;		              
		        $eventLog.append(all);
		        // 勾选选中项目
		        $eventLog.find('input[type=checkbox]').each(function(){
		            var ckVal=$(this).val();
		            for (var i=0;i<featureIdList.length;i++){
		                var curFeatureId=featureIdList[i];
		                if(ckVal==curFeatureId){
		                   $(this).attr("checked",true);
		                }
		            }
		        });
	        }
	    });
	  } 
   }
   
   
   // 取得选择的特征值
   function getSelectedFeatures(){
	   var curItem="";
	   $('.js-feature').find("div").each(function(){
	       trItem=""
	       trIdItem=""
	       var txt=$(this).find("table>tbody").find('input[type=radio]:checked').val();
	       curItem=curItem+txt;
	   })
	   addProductFeatureName(curItem)
   }
   
   
   // 取得选择的特征值
   function addProductFeatureName(curItem){
       var name=curItem;
       //alert(name);
       var i=featureNamelistSize;
       var conId="con"+i;
       var btn='<button id="'+conId+'" class="btn btn-primary">'+name+'</button>';
       $('.js-btn-group').append(btn);
      
       
       var goodContent='';
       goodContent=goodContent+
       '<div class="'+conId+'">'+
           '<input type="hidden" id="featureProductName" name="featureProductName" value="'+name+'">'+
           '<input type="hidden" id="operateTypeSub" name="operateTypeSub" value="create">'+
           '<div class="row">'+
			    '<div class="form-group">'+
	                '<label for="number" class="col-sm-2 control-label"><i class="required-mark">*</i>商品编号:</label>'+
	                '<div class="col-sm-3">'+
	                     '<input type="text" class="form-control" id="productId" name="productId" value="" readonly>'+
	                '</div>'+
	                '<div class="col-sm-3">'+
	                '</div>'+
	                '<div class="col-sm-3">'+
	                     '<button type="button" class="btn btn-default js-feature-del" id="productFeatureDel" name="productFeatureDel">删除</button>'+
	                '</div>'+
	                
	            '</div>'+
           '</div>'+
        
        
           '<div class="row">'+
                '<div class="form-group col-sm-6" data-type="required" data-mark="商品名称">'+
                    '<label for="title" class="col-sm-4 control-label"><i class="required-mark">*</i>商品名称:</label>'+
                    '<div class="col-sm-6" style="padding-left:0;padding-right:0;margin-left:15px;width:48.5%;">'+
                        '<input type="text" class="form-control dp-vd" id="productName" name="productName" value="">'+
                        '<p class="dp-error-msg"></p>'+
                    '</div>'+
                '</div>'+
           '</div>'+
           
           '<div class="row">'+
                '<div class="form-group">'+
	                '<label  class="col-sm-2 control-label">商品库存:</label>'+
	                '<div class="col-sm-3">'+
	                    '<button type="button" class="btn btn-primary js-facility">选择仓库1'+name+'</button>'+
	                '</div>'+
	            '</div>'+
           '</div>'+
           
           
           '<div class="row">'+
                '<div class="form-group">'+
                   '<label  class="col-sm-2 control-label"></label>'+
                   '<div class="col-sm-8">'+
                      '<table class="table table-bordered table_b_c js-table_3">'+
					      '<thead>'+
					           '<tr>'+
					               '<th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>${uiLabelMap.BrandOption}</th>'+
					           '</tr>'+
					      '</thead>'+
					      '<tbody>'+
					      '</tbody>'+
		    		  '</table>'+
                   '</div>'+
                '</div>'+
           '</div>'+
           
           '<div class="form-group">'+
                '<label  class="col-sm-2 control-label">销售价格(元):</label>'+
                '<div class="col-sm-3">'+
                    '<input type="text" class="form-control" id="salePrice" name="salePrice" value="" placeholder="" />'+
                '</div>'+
                
                '<label  class="col-sm-2 control-label">成本价格(元):</label>'+
                '<div class="col-sm-3">'+
                    '<input type="text" class="form-control" id="costPrice" name="costPrice" value="" placeholder="" />'+
                '</div>'+
           '</div>'+
           
           '<div class="form-group">'+
                '<label  class="col-sm-2 control-label">市场价格(元):</label>'+
                '<div class="col-sm-3">'+
                    '<input type="text" class="form-control" id="marketPrice" name="marketPrice" value="" placeholder="" />'+
                '</div>'+
                
                '<label  class="col-sm-2 control-label">重量(kg):</label>'+
                '<div class="col-sm-3">'+
                    '<input type="text" class="form-control" id="weight" name="weight" value="" placeholder="" />'+
                '</div>'+
            '</div>'+
            
            '<div class="form-group">'+
                '<label  class="col-sm-2 control-label">体积(m³):</label>'+
                '<div class="col-sm-3">'+
                    '<input type="text" class="form-control" id="volume" name="volume" value="" placeholder="" />'+
                '</div>'+
            '</div>'+
        
        
            '<div class="row">'+
	            '<div class="form-group">'+
	               '<label  class="col-sm-2 control-label"><i class="required-mark">*</i>商品图片:</label>'+
	                '<div class="col-sm-3">'+
	                    '<button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>'+
	               '</div>'+
	                '<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+
	            '</div>'+
            '</div>'+
        '</div>';
        $('.content-feature').append(goodContent);
        $('#addProductSku').modal('hide')
        var className="."+conId;
        $('.content-feature').find(className).show().siblings().hide();
   }
    	
    
</script>

		
