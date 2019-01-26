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
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>  
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
            <input type="hidden" name="isRecommendHomePage"   id="isRecommendHomePage" value="${parameters.isRecommendHomePage?if_exists}"/>
            <input type="hidden" name="supportServiceType"   id="supportServiceType" value="${parameters.supportServiceType?if_exists}"/>
            <input type="hidden" name="integralDeductionType"   id="integralDeductionType" value="${parameters.integralDeductionType?if_exists}"/>
            <input type="hidden" name="integralDeductionUpper"   id="integralDeductionUpper" value="${parameters.integralDeductionUpper?if_exists}"/>
            <input type="hidden" name="purchaseLimitationQuantity"   id="purchaseLimitationQuantity" value="${parameters.purchaseLimitationQuantity?if_exists}"/>
            <input type="hidden" name="isListShow"   id="isListShow" value="${parameters.isListShow?if_exists}"/>
            <input type="hidden" name="voucherAmount"   id="voucherAmount" value="${parameters.voucherAmount?if_exists}"/>
            <input type="hidden" name="useLimit"   id="useLimit" value="${parameters.useLimit?if_exists}"/>
            <input type="hidden" name="useStartTime"   id="useStartTime" value="${parameters.useStartTime?if_exists}"/>
            <input type="hidden" name="useEndTime"   id="useEndTime" value="${parameters.useEndTime?if_exists}"/>
            <input type="hidden" name="isBondedGoods"   id="isBondedGoods" value="${parameters.isBondedGoods?if_exists}"/>
            <input type="hidden" name="productStoreId"   id="productStoreId" value="${parameters.productStoreId?if_exists}"/>
            <input type="hidden" name="isInner"   id="isInner" value="${parameters.isInner?if_exists}"/>
            <input type="hidden" name="platformClassId"   id="platformClassId" value="${parameters.platformClassId?if_exists}"/>
            <input type="hidden" name="providerId"   id="providerId" value="${parameters.providerId?if_exists}"/>
            
            <#---
            <input type="hidden" name="productFacilityInfosForFeatureGoods" id="productFacilityInfosForFeatureGoods" value=""/>
            -->
            <input type="hidden" name="productFeatureGoodsInfos" id="productFeatureGoodsInfos" value=""/>
            
            <input type="hidden" name="productFeatureGoodsDelIds" id="productFeatureGoodsDelIds" value=""/>
            
			
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
	            <#if parameters.operateType?if_exists =='update'>
	            <button type="button" class="btn btn-primary" id="btnAddSku">新增sku</button>
	            </#if>
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
                
                <#--<#assign productFacilityList = delegator.findByAnd("Facility")/>-->
                <#assign productFacilityList = delegator.findByAnd("FacilityByPsId",{"productStoreId" : "${requestAttributes.productStoreId?if_exists}"})/>
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

				<div class="form-group">
					<label for="message-text" class="control-label col-sm-2">预警人邮箱:</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" name="warningMail" id="warningMail">
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
		                          <#if productFeatureType.productFeatureTypeName?if_exists != "">
		                          <option value="${productFeatureType.productFeatureTypeId?if_exists}">${productFeatureType.productFeatureTypeName?if_exists}</option>
		                          </#if>
		                      </#list>
		                    </select>
		                </div>
		            </div>
	            </div>
	            
	            <#--特征值-->
	            <div class="row">
	               <div class="form-group">
	             
	                <div class="col-sm-12 js-feature">
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


<!-- 确认弹出框start -->
<div id="modal_confirm_save"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
        <button id="createPage" type="button" class="btn btn-primary" data-dismiss="modal">继续添加</button>
        <button id="rtnList" type="button" class="btn btn-primary" data-dismiss="modal">跳转到列表</button>
      </div>
    </div>
  </div>
</div><!--确认弹出框end -->


<!-- 确认弹出框start -->
<div id="modal_confirm_img"  class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
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
</div><!--确认弹出框end -->


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

<!-- 确认弹出框start -->
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
</div><!--确认弹出框end -->

<script language="JavaScript" type="text/javascript">

    var featureNamelistSize=0;
    var $conMod="";
    var delIds="";
    var $conDelMod="";// 该sku商品的con信息
    var imgContentTypeId="";// 商品图片类型ID
    var facilityItem ="";//待删除仓库ID
    var imgContentIndex="";// 商品图片Index
    $(function(){
       var ids="${parameters.ids?if_exists}";
       var featureIds="${parameters.featureIds?if_exists}";
       var productId="${parameters.productId?if_exists}";
       var optypeTmp="${parameters.operateType?if_exists}";
        $(".select2Feature").select2({
	        tags: true,
	        maximumSelectionLength: 3  //最多能够选择的个数
	    });
	    if(optypeTmp=="update"){
	       isSkuProduct(productId,ids,featureIds);
	    }else{
           getProductFeatureNamesForCreate1(ids,featureIds);
        }
        if($("#mobileDetails").val()=="") {
            getMobileDetailsInfo();
        }
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
			
			$('.content-feature').find('.con').eq(i).show().siblings().hide();
			return false;
		});
		
		
		//添加库存
		$(document).on('click','.js-facility',function(){
			var curClass=$(this).parent().parent().parent().parent().attr("class");
			//alert(curClass);
			var $con = $(this).closest('.con'),
			i = $con.index();
			
			var $model = $('#addFacility');
			$model.find('input[name=conId]').val(i);//品牌名称
			$model.find('input[name=accountingQuantityTotal]').val('');//可用数量
			$model.find('input[name=warningQuantity]').val('');//预警数量
            $model.find('input[name=warningMail]').val('');//预警人邮箱
			
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
				var warningQuantity="0";
                var warningMail="";
				if($model.find('input[name=warningQuantity]').val()){
				    warningQuantity=$model.find('input[name=warningQuantity]').val();//预警库存
				}
                if($model.find('input[name=warningMail]').val()){
                    warningMail=$model.find('input[name=warningMail]').val();//预警人邮箱
                }
				//alert(facilityname);
				//alert(accountingQuantityTotal);
				//alert(warningQuantity);
	             
	            var tr=' <tr><td><input type="hidden" name="" id="" value="'+conId+'"><input type="hidden" name="facilitynameId" id="" value="'+facilitynameId+'">'+facilityname+'</td><td><input type="hidden" name="curInventoryItemId" id="curInventoryItemId" value="">'+accountingQuantityTotal+'</td><td>0</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td><td>'+warningMail+'</td><td class="fc_td"><button type="button" class="js-button-facility btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	 			var className='.'+conId;
	 		
	 			//$(className).find('.js-table_3>tbody').append(tr);
	 			$('.content-feature').find('.con').eq(conId).find('.js-table_3>tbody').append(tr);
	 			//$('.js-table_3>tbody').append(tr);
	 			$('#addFacility').modal('hide');
		    }
	    })
	    
        //删除按钮事件
  	    $(document).on('click','.js-button-facility',function(){
  	       //$(this).parent().parent().remove();
  	       var facilityId=$(this).parent().parent().find("input").val();//仓库ID
  	       var productForFacliityId=$("#productId").val();
  	       facilityItem=$(this).parent().parent();
  	       $conMod = $(this).closest('.con');
  	       isFacilityUsed(facilityId,productForFacliityId);
        })
        
        //删除弹出框删除按钮点击事件
	    $('#modal_confirm #ok').click(function(e){
	        $conMod.find("#productFacilityInfosForFeatureGoods").val("");
	       	facilityItem.remove();
	    });
	    
        // 保存处理
        $("#btnProductGoodSave").click(function(){
	       $('#updateProductFeatureGoods').dpValidate({
			  clear: true
		   });
		   // 取得特征商品登录信息
           productFeatureGoodsInfos();
	       $('#updateProductFeatureGoods').submit();
        });
        
        
        $('#updateProductFeatureGoods').dpValidate({
      		validate: true,
      		callback: function(){
               // 取得特征商品登录信息
               //productFeatureGoodsInfos();
               //document.updateProductFeatureGoods.action="<@ofbizUrl>updateProductFeatureGoods</@ofbizUrl>";
			   //document.updateProductFeatureGoods.submit();
			   
			    var productIdMain= $("#productIdMain").val();
		 	    var productTypeId= $("#productTypeId").val();
		 	    var productCategoryId= $("#productCategoryId").val();
			    // 商品编号	  
				var productNameMain=$("#productNameMain").val();
				// 商品副标题	
				var productSubheadName=$("#productSubheadName").val();
				// 是否上架
				var isOnline=$("#isOnline").val();
				// 销售开始时间
				var startTime=$("#startTime").val();
				// 销售结束时间
				var endTime=$("#endTime").val();
				// 商家名称	
				var businessPartyId=$("#businessPartyId").val();
				// 商品品牌	
				var brandId=$("#brandId").val();
				// 体积
				var volumeMain=$("#volumeMain").val();
				// 重量	
				var weightMain=$("#weightMain").val();
				// 是否使用特征	
				var isUsedFeature=$("#isUsedFeature").val();
				// seo关键字
				var seoKeyword=$("#seoKeyword").val();
				// 取得pc详情信息
				var pcDetails=$("#pcDetails").val();
				// 取得移动详情信息
				var mobileDetails=$("#mobileDetails").val();
				// 取得商品标签
				var productTags=$("#productTags").val();
				// 销售价格
				var salePriceMain=$("#salePriceMain").val();
				// 市场价格
				var marketPriceMain=$("#marketPriceMain").val();
				// 成本价格	
				var costPriceMain=$("#costPriceMain").val();
				// 取得商品分类属性信息
				var productAttrInfos=$("#productAttrInfos").val();
				// 取得参数信息
				var productParameterInfos=$("#productParameterInfos").val();
				// 取得商品库存信息
				var productFacilityInfos=$("#productFacilityInfos").val();
				// 商品特征信息
				var productFeatureInfos=$("#productFeatureInfos").val();
				// 图片信息
				var productContentInfos=$("#productContentInfos").val();
				// 商品关系信息
				var productAssocInfos=$("#productAssocInfos").val();

                // Add by zhajh at 20180305 yabiz相关内容 Begin
                // 推荐到首页
                var isRecommendHomePage=$('#isRecommendHomePage').val();
                // 支持服务类型
                var supportServiceType=$('#supportServiceType').val();
                // 积分抵扣
                var integralDeductionType=$("#integralDeductionType").val();
                // 积分抵扣上限
                var integralDeductionUpper=$("#integralDeductionUpper").val();
                // 每人限购数量
                var purchaseLimitationQuantity=$("#purchaseLimitationQuantity").val();
                // 列表展示
                var isListShow=$('#isListShow').val();
                // 代金券面额
                var voucherAmount=$("#voucherAmount").val();
                // 使用限制
                var useLimit=$('#useLimit').val();
                // 使用开始时间
                var useStartTime=$("#useStartTime").val();
                // 使用结束时间
                var useEndTime=$("#useEndTime").val();
                // 是否保税商品
                var isBondedGoods=$('#isBondedGoods').val();
                // 店铺
                var productStoreId=$('#productStoreId').val();
                // 是否自营
                var isInner=$('#isInner').val();
                // 平台分类
                var platformClassId=$('#platformClassId').val();
                // 供应商编码
                var providerId=$('#providerId').val();
                // Add by zhajh at 20180305 yabiz相关内容 End




                // 特征商品信息
				var productFeatureGoodsInfosP=$("#productFeatureGoodsInfos").val();
				// 操作类型	
				var operateType=$("#operateType").val();
				
				// 删除特征商品信息
				if(delIds!=""){
				   $("#productFeatureGoodsDelIds").val(delIds.substr(1,delIds.length-1));
				}
				var productFeatureGoodsDelIds=$("#productFeatureGoodsDelIds").val();
				//alert(productFeatureGoodsDelIds);
			    jQuery.ajax({
			        url: '<@ofbizUrl>updateProductFeatureGoodsForAjax</@ofbizUrl>',
			        type: 'POST',
			        data: {
			            'productIdMain':productIdMain,  
						'productTypeId':productTypeId,
						'productCategoryId':productCategoryId,
						'operateType':operateType,  
						'productNameMain':productNameMain,
						'productSubheadName':productSubheadName,
						'isOnline':isOnline,
						'startTime':startTime,
						'endTime':endTime,
						'businessPartyId':businessPartyId,
						'brandId':brandId,
						'volumeMain':volumeMain,
						'weightMain':weightMain,
						'isUsedFeature':isUsedFeature,
						'seoKeyword':seoKeyword,
						'pcDetails':pcDetails,
						'mobileDetails':mobileDetails,
						'productTags':productTags,
						'salePriceMain':salePriceMain,
						'marketPriceMain':marketPriceMain,
						'costPriceMain':costPriceMain,
						'productAttrInfos':productAttrInfos,
						'productParameterInfos':productParameterInfos,
						'productFacilityInfos':productFacilityInfos,
						'productFeatureInfos':productFeatureInfos,
						'productContentInfos':productContentInfos,
						'productAssocInfos':productAssocInfos,
						'productFeatureGoodsDelIds':productFeatureGoodsDelIds,
						'productFeatureGoodsInfos':productFeatureGoodsInfosP,
                        'isRecommendHomePage':isRecommendHomePage,
                        'supportServiceType':supportServiceType,
                        'integralDeductionType':integralDeductionType,
                        'integralDeductionUpper':integralDeductionUpper,
                        'purchaseLimitationQuantity':purchaseLimitationQuantity,
                        'isListShow':isListShow,
                        'voucherAmount':voucherAmount,
                        'useLimit':useLimit,
                        'useStartTime':useStartTime,
                        'useEndTime':useEndTime,
                        'isBondedGoods':isBondedGoods,
                        'productStoreId':productStoreId,
                        'isInner':isInner,
                        'platformClassId':platformClassId,
						'providerId':providerId
			        },
			        success: function(data) {
			            if(operateType=="create"){
				            //设置删除弹出框内容
			   				$('#modal_confirm_save #modal_confirm_body').html("商品添加成功");
			   				$('#modal_confirm_save').modal('show');
		   				}else{
		   				    document.location.href="<@ofbizUrl>findProductGoods</@ofbizUrl>";
		   				}
			        }
			    });
			    
      		}
        });
       
        
        
        
        
        
        // 添加商品sku
        $(document).on('click','#btnAddSku',function(){
          
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
			//$(curClassName).find('input[name=operateTypeSub]').val("delete");
			//$('.content-feature').find(".con0").show().siblings().hide();
			var $con = $(this).closest('.con');
			var	i = $con.index();
			
			
			if($con.find('input[name=operateTypeSub]').val()=="update"){
			    var delProductId=$con.find('input[name=productId]').val();
			    delIds=delIds+","+delProductId;
			}
			
			$con.find('input[name=operateTypeSub]').val("delete");
			$con.hide();
			$('.js-btn-group').find('.btn').eq(i).hide();
			if ($('.js-btn-group').find('.btn:visible').length) $con.siblings('.con').first().show();
		});
		
		
		 // 返回按钮的操作
	     $("#btnGoBack").click(function(){
	         <#if parameters.operateType?if_exists =='update'>
	         document.location.href="<@ofbizUrl>editProductGood?productId="+${parameters.productId?if_exists}+"&productCategoryId="+$("#productCategoryId").val()+"&productTypeId="+$("#productTypeId").val()+"&operateType=update&gobackPage=viewPage</@ofbizUrl>";
	         <#else>
	         document.location.href="<@ofbizUrl>editProductGood?productCategoryId="+$("#productCategoryId").val()+"&productTypeId="+$("#productTypeId").val()+"&operateType=create&gobackPage=createPage</@ofbizUrl>";
	         </#if>
	     })
	     
	     //创建成功后返回列表按钮点击事件
	     $('#modal_confirm_save #rtnList').click(function(e){
	        document.location.href="<@ofbizUrl>findProductGoods</@ofbizUrl>";
	     });
        
         //创建成功后继续添加按钮点击事件
	     $('#modal_confirm_save #createPage').click(function(e){
	       	document.location.href="<@ofbizUrl>createProductGood</@ofbizUrl>";
	     });
	     
         //添加图片按钮事件
  	     $(document).on('click','.js-productcontent',function(){
  	       var $con = $(this).closest('.con'),
		   i = $con.index();
		   $conMod=$('.content-feature').find('.con').eq(i);
		   //var productId=$('.content-feature').find('.con').eq(i).find("#productId").val();
		   var productId =$conMod.find("#productId").val();
           imageManage();
         })


        // 图片的编辑
        $(document).on('click','.js-imgedit',function(){
            var curId=$(this).attr("id");
            if(curId=="editProductContentImg1"){
                imgContentTypeId="ADDITIONAL_IMAGE_1";
                imgContentIndex="0";
            }else if(curId=="editProductContentImg2"){
                imgContentTypeId="ADDITIONAL_IMAGE_2";
                imgContentIndex="1";
            }else if(curId=="editProductContentImg3"){
                imgContentTypeId="ADDITIONAL_IMAGE_3";
                imgContentIndex="2";
            }else if(curId=="editProductContentImg4"){
                imgContentTypeId="ADDITIONAL_IMAGE_4";
                imgContentIndex="3";
            }else if(curId=="editProductContentImg5"){
                imgContentTypeId="ADDITIONAL_IMAGE_5";
                imgContentIndex="4";
            }
//            alert(imgContentTypeId);
//            $conDelMod = $(this).closest('.con');
            $conMod=$(this).closest('.con');
            imageManageEdit();
        });
        
//        // 上传图片
//	    $('body').on('click','.img-submit-btn',function(){
//           var obj = $.chooseImage.getImgData();
//           $.chooseImage.choose(obj,function(data){
//             //var contentId="/content/control/stream?contentId="+data.uploadedFile0;
//             // $('#img').attr('src',contentId);
//             //$('#contentId').val(data.uploadedFile0);
//             var productId=$conMod.find("#productId").val();
//             getProductPicById(productId);
//           })
//        });

        // 上传图片
        $('body').on('click','.img-submit-btn',function(){
            var obj = $.chooseImage.getImgData();
//            var productId=$conMod.find("#productId").val();
            var imgProductId=$conMod.find("#productId").val();
            $.chooseImage.choose(obj,function(data){
                imgUrl1="";
                imgUrl2="";
                imgUrl3="";
                imgUrl4="";
                imgUrl5="";
				;
                if(data.uploadedFile0){
                    imgUrl1="/content/control/getImage?contentId="+data.uploadedFile0;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile0,"1");
                    //alert(imgUrl1);
                }
                if(data.uploadedFile1){
                    imgUrl2="/content/control/getImage?contentId="+data.uploadedFile1;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile1,"2");
                    //alert(imgUrl2);
                }
                if(data.uploadedFile2){
                    imgUrl3="/content/control/getImage?contentId="+data.uploadedFile2;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile2,"3");
                    //alert(imgUrl3);
                }
                if(data.uploadedFile3){
                    imgUrl4="/content/control/getImage?contentId="+data.uploadedFile3;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile3,"4");
                    //alert(imgUrl4);
                }
                if(data.uploadedFile4){
                    imgUrl5="/content/control/getImage?contentId="+data.uploadedFile4;
                    creatProductContentByIdForProduct(imgProductId,data.uploadedFile4,"5");
                    //alert(imgUrl5);
                }
                getProductPicByContentId(imgUrl1,imgUrl2,imgUrl3,imgUrl4,imgUrl5);

//                var productId=$("#productId").val();
//                getProductPicById(productId);
            })
        });

        // 判断/创建商品图片信息
        function creatProductContentByIdForProduct(productId,contentId,picIndex){
            if (productId != ""&& contentId !="" && picIndex !=""){
                jQuery.ajax({
                    url: '<@ofbizUrl>creatProductContentByIdForProduct</@ofbizUrl>',
                    type: 'POST',
                    data: {
                        'productId' : productId,
                        'contentId' : contentId,
                        'picIndex' : picIndex
                    },
                    success: function(data) {
                        var productId=data.productId;
                        var contentId=data.contentId;
                        var picIndex=data.picIndex;

                        //alert(contentId);
                    }
                });
            }
        }
        
        // 图片的删除
        $(document).on('click','.js-imgdel',function(){
             var curId=$(this).attr("id");
             if(curId=="delProductContentImg1"){
                 imgContentTypeId="ADDITIONAL_IMAGE_1";
             }else if(curId=="delProductContentImg2"){
                 imgContentTypeId="ADDITIONAL_IMAGE_2";
             }else if(curId=="delProductContentImg3"){
                 imgContentTypeId="ADDITIONAL_IMAGE_3";
             }else if(curId=="delProductContentImg4"){
                 imgContentTypeId="ADDITIONAL_IMAGE_4";
             }else if(curId=="delProductContentImg5"){
                 imgContentTypeId="ADDITIONAL_IMAGE_5";
             }
             $conDelMod = $(this).closest('.con');
             $('#modal_confirm_img #modal_confirm_body').html("确定要删除该商品图片信息，是否继续删除");
			 $('#modal_confirm_img').modal('show');
        });
        
        //删除弹出框删除按钮点击事件
	    $('#modal_confirm_img #ok').click(function(e){
	       	delProductContentImg();
	    });
        
    })

    // 取得商品图片信息
    function getProductPicByContentId(imgUrl1,imgUrl2,imgUrl3,imgUrl4,imgUrl5){
        var time = (new Date()).getTime();
		console.log($conMod);
        if(imgUrl1!=""){
            $conMod.find('#img1').attr('src',imgUrl1+'&v='+time);
            $conMod.find('#mainImg').attr('value',imgUrl1+'&v='+time);
            $conMod.find('#editProductContentImg1').text("编辑");
            $conMod.find("#delProductContentImg1").show();

            $conMod.find(".js-img1").show();
        }
        if(imgUrl2!=""){
            $conMod.find('#img2').attr('src',imgUrl2+'&='+time);
            $conMod.find('#editProductContentImg2').text("编辑");
            $conMod.find("#delProductContentImg2").show();

            $conMod.find(".js-img2").show();
        }
        if(imgUrl3!=""){
            $conMod.find('#img3').attr('src',imgUrl3+'&='+time);
            $conMod.find('#editProductContentImg3').text("编辑");
            $conMod.find("#delProductContentImg3").show();

            $conMod.find(".js-img3").show();
        }
        if(imgUrl4!=""){
            $conMod.find('#img4').attr('src',imgUrl4+'&='+time);
            $conMod.find('#editProductContentImg4').text("编辑");
            $conMod.find("#delProductContentImg4").show();

            $conMod.find(".js-img4").show();
        }
        if(imgUrl5!=""){
            $conMod.find('#img5').attr('src',imgUrl5+'&='+time);
            $conMod.find('#editProductContentImg5').text("编辑");
            $conMod.find("#delProductContentImg5").show();
            $conMod.find(".js-img5").show();
        }
    }
    
   // 取得商品库存信息
   function getFacilityByProductId(productId,model){
	  if (productId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getFacilityByProductId</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId
	        },
	        success: function(data) {
	           var facilityInfoList=data.facilityInfoList;
	           //alert(facilityInfoList);
	           for (var i=0;i<facilityInfoList.length;i++){
	              var facilityName=facilityInfoList[i].facilityName;
	              var alreadyLockQuantitySum=facilityInfoList[i].alreadyLockQuantitySum;
	              var accountingQuantityTotal=facilityInfoList[i].accountingQuantityTotal;
	              var warningQuantity=facilityInfoList[i].warningQuantity;
			      var warningMail="";
                   if(facilityInfoList[i].warningMail){
                       warningMail=facilityInfoList[i].warningMail;
                   }

	              var totalNum=facilityInfoList[i].totalNum;
	              var facilityId=facilityInfoList[i].facilityId;
	              var inventoryItemId=facilityInfoList[i].inventoryItemId;
	              //alert(facilityId);
	              
	              //var tr='<tr><td>'+facilityName+'</td><tr>';
	              
	              var tr1=' <tr><td><input type="hidden" name="facilitynameId" id="" value="'+facilityId+'">'+facilityName+'</td> <td><input type="hidden" name="curInventoryItemId" id="curInventoryItemId" value="'+inventoryItemId+'">'+totalNum+'</td><td>'+alreadyLockQuantitySum+'</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td><td>'+warningMail+'</td> <td class="fc_td"><button type="button" class="js-button-facility btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	              
	             // var tr2='<tr><td>'+facilityname+'</td></tr>';
	 			  //alert(tr1);
	 			  model.find('.js-table_3>tbody').append(tr1);
	 			
	           }
	        }
	    });
	  } 
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
	       var warningQuantity= "0";//预警数量
		   var warningMail= "";//预警人邮箱
	       if(tdArr.eq(4).text()){
	          warningQuantity= tdArr.eq(4).text();
	       }
	       if(tdArr.eq(5).text()){
               warningMail= tdArr.eq(5).text();
		   }
	       var inventoryItemId = tdArr.eq(1).find("input").val();//库存明细ID
	       curFacilityInfo=facilityId+"|"+inventoryItemId+"|"+accountingQuantityTotal+"|"+warningQuantity+"|"+warningMail;
           tFacilityInfos = tFacilityInfos + "," + curFacilityInfo;
	    }); 
	    var facilityInfos=tFacilityInfos.substr(1,tFacilityInfos.length);
	    model.find("#productFacilityInfosForFeatureGoods").val(facilityInfos);
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
		   var featureProductId =$model.find('input[name=featureProductId]').val();//商品特Id
		   var operateTypeSub =$model.find('input[name=operateTypeSub]').val();//特征商品操作类型
		   facilityInfos($model);
		   
		   
		   //var className='.'+conId;
	 	   //$(className).find('.js-table_3>tbody').append(tr);
		   var productFacilityInfos =$model.find("#productFacilityInfosForFeatureGoods").val();//仓库信息
		   if(!productFacilityInfos){
		      productFacilityInfos="none";
		   }
		   //var productContentInfos =$model.find('input[name=productContentInfos]').val();//图片信息
	       
	       curFeatureGoodsInfosInfo=productId+"^"+
	                                productName+"^"+
	                                volume+"^"+
	                                weight+"^"+
	                                salePrice+"^"+
	                                marketPrice+"^"+
	                                costPrice+"^"+
	                                featureProductName+"^"+
	                                featureProductId+"^"+
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
         getProductFeatureListById2(featureId,featureName)
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
  
   // 取得选择的特征值
   function getSelectedFeatures(){
	   var curItem="";
	   var curId="";
	   $('.js-feature').children().each(function(){
	     var txt=$(this).find('input[type=radio]:checked').val();
	     var id=$(this).find('input[type=hidden]').val();
	     curItem=curItem+txt;
	     curId=curId+"|"+id;
	   })
	   
	   //alert(curId.substr(1,curId.length-1));
	   var productTypeIdForPrepare='${parameters.productTypeId?if_exists}';
	   var productCategoryIdForPrepare ='${parameters.productCategoryId?if_exists}';
	   
	   //addProductFeatureName(curItem);
	   addProductFeatureInfo(productTypeIdForPrepare,productCategoryIdForPrepare,curItem,curId.substr(1,curId.length-1));
   }
   
   <#--
   // 取得选择的特征值
   function addProductFeatureName(curItem,productId){
       var name=curItem;
       //alert(name);
       var i=featureNamelistSize;
       var conId="con";
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
	                     '<input type="text" class="form-control" id="productId" name="productId" value="'+productId+'" readonly>'+
	                '</div>'+
	                '<div class="col-sm-3">'+
	                '</div>'+
	                '<div class="col-sm-3">'+
	                     '<button type="button" class="btn btn-default js-feature-del">删除</button>'+
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
	                    '<button type="button" class="btn btn-primary js-facility">选择仓库</button>'+
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
	                    '<button type="button" class="btn btn-primary js-productcontent">添加图片</button>'+
	               '</div>'+
	                '<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+
	            '</div>'+
            '</div>'+
            '<div class="row">'+
                '<div class="form-group col-sm-6">'+
                    '<label  class="col-sm-2 control-label"></label>'+
				    '<div class="col-sm-2">'+
					     '<img height="150"  alt="" src="" id="img1" style="height:100px;width:100px;">'+
				    '</div>'+
                '</div>'+
            '</div>'+
            
        '</div>';
        $('.content-feature').append(goodContent);
        $('#addProductSku').modal('hide')
        var j=$('.js-btn-group').find('.btn').last().index();
	    $('.content-feature').find(".con").eq(j).show().siblings().hide();
   }
   
   -->
   
   
   // 取得商品特征信息
   function getProductFeatureListById2(productFeatureTypeId,featureName,featureIds){
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
                ' <div class="col-sm-12" style="text-align:left">'+featureName+
				' </div>';
				
				var group=	''; 
				if(listSize>0){
				    for (var i=0;i<trCount;i++){
				       group=group+' <div class="form-group col-sm-12">'; 
				       for(var j=i*3;j<(i+1)*3;j++){
				           if(j<listSize){
				           var curFeatureName= "featureNameGood"+productFeatureTypeId;
				           var productFeature=productFeatureList[j];
							group +='<div class="col-sm-4">'+
										   '<div class="form-group">'+
										        '<div class="col-sm-2">'+
                                                     '<input class="js-productFeature" type="radio" name="'+curFeatureName+'" value="'+productFeature.productFeatureName+'"/><input type="hidden" name="" id="" value="'+productFeature.productFeatureId+'">'+
												'</div>'+
												'<div class="col-sm-10">'+
                                                     '<input type="text" name="productFeatureName" id="" value="'+productFeature.productFeatureName+'"  disabled="true" size=15>'+
												'</div>'+
										   '</div>'+
							           '</div>';
				           }
				       }
				       group=group+'</div>';
				    }   
				}  
				var tbBodyE	=' </div>';
				var all=tbDiv+group+tbBodyE;	
		        $eventLog.append(all);
		      
	        }
	    });
	  } 
   }
   
   
  // 判断商品是否是Sku
  function isSkuProduct(productId,ids,featureIds){
	  if (productId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>isSkuProduct</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId
	        },
	        success: function(data) {
	           var skuFlg=data.skuFlg;
	           if(skuFlg=="Y"){
	              getProductFeatureNamesForUpdate1(productId);
	           }else{
		       	  getProductFeatureNamesForCreate1(ids,featureIds);
	           }
	        }
	    });
	  } 
   }
   //////////////////////////////////////////////////////////////////////////////////////////////////
   // 添加准备商品实体
   function setPrepareProductEntity(productTypeId,productCategoryId,listSize,productFeatureNameList){
	  if (productTypeId!="" && productCategoryId!=""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>setPrepareProductEntityForCreate</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productTypeId' : productTypeId,
	             'productCategoryId' : productCategoryId,
	             'isFeatureGoods' : "Y",
	             'listSize':listSize
	        },
	        success: function(data) {
	           var featureGoodCreateInfoList=data.featureGoodCreateInfoList;
	           for(var i=0;i<listSize;i++){
	               var  featureGoodCreateInfo=featureGoodCreateInfoList[i];
	               var  PrepareProductId= featureGoodCreateInfo.productId;
	               var conId="con";
	               var name=productFeatureNameList[i].productFeatureName;
	               var featureId=productFeatureNameList[i].productFeatureId;
	               var btn='<button id="'+conId+'" class="btn btn-primary">'+name+'</button>';
	               $('.js-btn-group').append(btn);
	               
	               
	               var productNameMain =$("#productNameMain").val();
	               var goodContent='';
	               goodContent=goodContent+
	               '<div class="'+conId+'">'+
	                   '<input type="hidden" id="featureProductName" name="featureProductName" value="'+name+'">'+
	                   '<input type="hidden" id="featureProductId" name="featureProductId" value="'+featureId+'">'+
	                   '<input type="hidden" id="operateTypeSub" name="operateTypeSub" value="create">'+
                       '<div class="row">'+
						    '<div class="form-group col-sm-6">'+
				                '<label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品编号:</label>'+
				                '<div class="col-sm-9">'+
				                     '<input type="hidden" id="productId" name="productId" value="'+PrepareProductId+'">'+
				                     '<input type="text" class="form-control" id="" name="" value="" readonly>'+
				                '</div>'+
				                <#--
					                '<div class="col-sm-3">'+
					                '</div>'+
					                '<div class="col-sm-3">'+
					                     '<button type="button" class="btn btn-default js-feature-del" id="productFeatureDel" name="productFeatureDel">删除</button>'+
					                '</div>'+
				                 -->
				            '</div>'+
			           '</div>'+
	                
	                
	                   '<div class="row">'+
			                '<div class="form-group col-sm-6" data-type="required" data-mark="商品名称">'+
			                    '<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品名称:</label>'+
			                    '<div class="col-sm-9">'+
                                    '<input type="text" class="form-control dp-vd" id="productName" name="productName" value="'+productNameMain+'('+name+')" readonly>'+
			                        '<p class="dp-error-msg"></p>'+
			                    '</div>'+
			                '</div>'+
	                   '</div>'+
	                   
	                   '<div class="row">'+
	                      
			                '<div class="form-group col-sm-6" data-type="required" data-mark="商品库存">'+
				                '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>'+
				                '<div class="col-sm-9">'+
				                    '<button type="button" class="btn btn-primary js-facility">选择仓库</button>'+
				                    '<input type="hidden" class="form-control dp-vd" name="productFacilityInfosForFeatureGoods" id="productFacilityInfosForFeatureGoods" value=""/>'+
		                            '<div class="dp-error-msg"></div>'+
				                '</div>'+
				            '</div>'+
			           '</div>'+
			           
			           
			           '<div class="row">'+
			                '<div class="form-group">'+
			                   '<div class="col-sm-1">'+
			                   '</div>'+
			                   '<div class="col-sm-10">'+
			                      '<table class="table table-bordered table_b_c js-table_3">'+
								      '<thead>'+
								           '<tr>'+
								               '<th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>预警人邮箱</th><th>${uiLabelMap.BrandOption}</th>'+
								           '</tr>'+
								      '</thead>'+
								      '<tbody>'+
								      '</tbody>'+
					    		  '</table>'+
			                   '</div>'+
			                '</div>'+
			           '</div>'+
			           '<div class="row">'+
				           '<div class="form-group col-sm-6" data-type="required,format" data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/" data-mark="销售价格">'+
				                 '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>销售价格(元):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="" placeholder="" />'+
				                '</div>'+
				                '<p class="dp-error-msg"></p>'+
				            '</div>'+
				            
				             '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="成本价格">'+
				                '<label  class="col-sm-3 control-label">成本价格(元):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control dp-vd" id="costPrice" name="costPrice" value="" placeholder="" />'+
				                '</div>'+
				                '<p class="dp-error-msg"></p>'+
				            '</div>'+
			           '</div>'+
			           '<div class="row">'+
				             '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="市场价格">'+
				                '<label  class="col-sm-3 control-label">市场价格(元):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control dp-vd" id="marketPrice" name="marketPrice" value="" placeholder="" />'+
				                '</div>'+
				                '<p class="dp-error-msg"></p>'+
				            '</div>'+
				             '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="重量">'+
				                '<label  class="col-sm-3 control-label">重量(kg):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control dp-vd" id="weight" name="weight" value="" placeholder="" />'+
				                '</div>'+
				                '<p class="dp-error-msg"></p>'+
				            '</div>'+
			            '</div>'+
			            
			            '<div class="row">'+
				            '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="体积">'+
				                '<label  class="col-sm-3 control-label">体积(m³):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control dp-vd" id="volume" name="volume" value="" placeholder="" />'+
				                '</div>'+
				                '<p class="dp-error-msg"></p>'+
				            '</div>'+
	                    '</div>'+
	                
	                    '<div class="row">'+
				            '<div class="form-group col-sm-6" data-type="required" data-mark="商品主图片">'+
				               '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>'+
				                '<div class="col-sm-9">'+
				                    '<button type="button" class="btn btn-primary js-productcontent">添加图片</button>'+
				                    '<input id="mainImg" name="mainImg" class="dp-vd" type="hidden">'+ 
					                '<p class="dp-error-msg"></p>'+
				               '</div>'+
				            '</div>'+
				            <#--
				            '<div class="form-group col-sm-6">'+
				                '<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+
				            '</div>'+
				            -->
			            '</div>'+
			            
			            <#--'<div class="row">'+-->
			                <#--'<div class="form-group col-sm-6">'+-->
			                    <#--'<label  class="col-sm-2 control-label"></label>'+-->
							    <#--'<div class="col-sm-2 js-img1">'+-->
								     <#--'<img height="150"  alt="" src="" id="img1" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img2">'+-->
								     <#--'<img height="150"  alt="" src="" id="img2" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img3">'+-->
								     <#--'<img height="150"  alt="" src="" id="img3" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img4">'+-->
								     <#--'<img height="150"  alt="" src="" id="img4" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img5">'+-->
								     <#--'<img height="150"  alt="" src="" id="img5" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
			                <#--'</div>'+-->
			            <#--'</div>'+-->

                           '<div class="row">'+
                           '<div class="form-group col-sm-8">'+
                           '<label  class="col-sm-2 control-label"></label>'+
                           '<div class="col-sm-2 js-img1">'+
                           '<img height="150"  alt="" src="" id="img1" style="width:100%;">'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px"  id="editProductContentImg1">添加</button>'+
                           '</div>'+
                           '<div class="col-sm-2 js-img2">'+
                           '<img height="150"  alt="" src="" id="img2" style="width:100%;">'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px" id="editProductContentImg2">添加</button>'+
                           '</div>'+
                           '<div class="col-sm-2 js-img3">'+
                           '<img height="150"  alt="" src="" id="img3" style="width:100%;">'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px" id="editProductContentImg3">添加</button>'+
                           '</div>'+
                           '<div class="col-sm-2 js-img4">'+
                           '<img height="150"  alt="" src="" id="img4" style="width:100%;">'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px" id="editProductContentImg4">添加</button>'+
                           '</div>'+
                           '<div class="col-sm-2 js-img5">'+
                           '<img height="150"  alt="" src="" id="img5" style="width:100%;">'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>'+
                           '<button type="button"class="btn btn-danger btn-sm js-imgedit"  style="margin-left:3px" id="editProductContentImg5">添加</button>'+
                           '</div>'+
                           '</div>'+
                           '</div>'+
	                '</div>';
	                $('.content-feature').append(goodContent);
	                //$('.content-feature').find(".con0").show().siblings().hide();
	                var j=$('.js-btn-group').find('.btn').first().index();
	                $('.content-feature').find(".con").eq(j).show().siblings().hide();
	                $conMod=$('.content-feature').find('.con').eq(i);
//	                $conMod.find(".js-img1").hide();
//			        $conMod.find(".js-img2").hide();
//			        $conMod.find(".js-img3").hide();
//			        $conMod.find(".js-img4").hide();
//			        $conMod.find(".js-img5").hide();

                   $conMod.find("#delProductContentImg1").hide();
                   $conMod.find("#delProductContentImg2").hide();
                   $conMod.find("#delProductContentImg3").hide();
                   $conMod.find("#delProductContentImg4").hide();
                   $conMod.find("#delProductContentImg5").hide();
	               
	           }
	        }
	    });
	  } 
   }
   
   
   
   //////////////////////////////////////////////////////////////////////////////////////////////////
   // 取得选择的特征值
   function getProductFeatureNamesForCreate1(ids,featureIds){
		jQuery.ajax({
	        url: '<@ofbizUrl>getProductFeatureGoodsByFeature</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'ids' : ids,
	             'featureIds' : featureIds
	        },
	        success: function(data) {
	           var map=data.productFeatureInfo;
	           var productFeatureNameList=map.productFeatureNameList;
	           var listSize=map.listSize;
	           featureNamelistSize=map.listSize;
	           var productTypeIdForPrepare='${parameters.productTypeId?if_exists}';
	           var productCategoryIdForPrepare ='${parameters.productCategoryId?if_exists}';
	           $('.js-btn-group').find("button").remove();
	           $('.content-feature').find("div").remove();
	           //添加准备商品实体
	           setPrepareProductEntity(productTypeIdForPrepare,productCategoryIdForPrepare,listSize,productFeatureNameList);
	        }
	    });
   }
   
   function imageManage() {
     var imgType="product";
     var imgProductId=$conMod.find("#productId").val();
       var curPartyId='${requestAttributes.userLogin.partyId?if_exists}';
       var curIsInner='${requestAttributes.isInner?if_exists}';
  	 $.chooseImage.int({
	    userId: 3446,
	    serverChooseNum: 5,
	    getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
	    submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
	    submitServerImgUrl: '',
	    submitNetworkImgUrl: '',
	    otherObj:{
	       type:imgType,
	       productId:imgProductId,
		   ownerPartyId:curPartyId,
		   isInner:curIsInner
	    }
	});
	$.chooseImage.show()
  }
  // 取得商品图片信息
  function getProductPicById(productId){
	  if (productId != ""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>getProductPicById</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId
	        },
	        success: function(data) {
	       	   var time = (new Date()).getTime();
	           var productAdditionalImage1=data.productAdditionalImage1;
	           var productAdditionalImage2=data.productAdditionalImage2;
	           var productAdditionalImage3=data.productAdditionalImage3;
	           var productAdditionalImage4=data.productAdditionalImage4;
	           var productAdditionalImage5=data.productAdditionalImage5;
	           
	           if(productAdditionalImage1!=""){
	               $conMod.find('#img1').attr('src',productAdditionalImage1+'?v='+time);
	               $conMod.find('#mainImg').attr('value',productAdditionalImage1+'?v='+time);
	               $conMod.find(".js-img1").show();
	           }
	           if(productAdditionalImage2!=""){
	               $conMod.find('#img2').attr('src',productAdditionalImage2+'?v='+time);
	               $conMod.find(".js-img2").show();
	           }
	           if(productAdditionalImage3!=""){
	               $conMod.find('#img3').attr('src',productAdditionalImage3+'?v='+time);
	               $conMod.find(".js-img3").show();
	           }
	           if(productAdditionalImage4!=""){
	               $conMod.find('#img4').attr('src',productAdditionalImage4+'?v='+time);
	               $conMod.find(".js-img4").show();
	           }
	           if(productAdditionalImage5!=""){
	               $conMod.find('#img5').attr('src',productAdditionalImage5+'?v='+time);
	               $conMod.find(".js-img5").show();
	           }
	        }
	    });
	  } 
   }	 
   
   
    <#--// 取得选择的特征值-->
   <#--function getProductFeatureNamesForUpdate1(productId){-->
       <#---->
		<#--jQuery.ajax({-->
	        <#--url: '<@ofbizUrl>getProductFeatureGoodsByFeature</@ofbizUrl>',-->
	        <#--type: 'POST',-->
	        <#--data: {-->
	             <#--'productId' :productId-->
	        <#--},-->
	        <#--success: function(data) {-->
	           <#--var map=data.productFeatureInfo;-->
	           <#--var productFeatureNameList=map.productFeatureNameList;-->
	           <#--var productFeatureGoodList=map.productFeatureGoodList;-->
	           <#--var listSize=map.listSize;-->
	           <#--$('.js-btn-group').find("button").remove();-->
	           <#--$('.content-feature').find("div").remove();-->
	          <#---->
	           <#---->
	           <#---->
	           <#--for(var i=0;i<listSize;i++){-->
	           <#---->
	               <#--var name=productFeatureNameList[i].productFeatureName;-->
	               <#--var featureId=productFeatureNameList[i].productFeatureId;-->
	               <#--var productGoodInfo=productFeatureGoodList[i];-->
	               <#--var priceList=productGoodInfo.productPriceList;-->
	               <#--var priceMap=productGoodInfo.priceMap;-->
	               <#--var salePrice="";-->
	               <#--if(priceMap.DEFAULT_PRICE){-->
	               		<#--salePrice=priceMap.DEFAULT_PRICE;-->
	               <#--}-->
	               <#--var costPrice="";-->
	               <#--if(priceMap.COST_PRICE){-->
	                 <#--costPrice=priceMap.COST_PRICE;-->
	               <#--}-->
	               <#--var marketPrice="";-->
	               <#--if(priceMap.MARKET_PRICE){-->
	                 <#--marketPrice=priceMap.MARKET_PRICE;-->
	               <#--}-->
	               <#--var weight="";-->
	               <#--if(productGoodInfo.productInfo.weight){-->
	                  <#--weight=productGoodInfo.productInfo.weight;-->
	               <#--}   -->
	               <#--var volume="";-->
	               <#--if(productGoodInfo.productInfo.volume){-->
	                  <#--volume =productGoodInfo.productInfo.volume;-->
	               <#--}-->
	               <#---->
	               <#--var imageUrl="";-->
	               <#--if(productGoodInfo.imageUrl){-->
	                  <#--imageUrl =productGoodInfo.imageUrl;-->
	               <#--}-->
	               <#---->
	               <#--var imageUrl2="";-->
	               <#--if(productGoodInfo.imageUrl2){-->
	                  <#--imageUrl2 =productGoodInfo.imageUrl2;-->
	               <#--}-->
	               <#---->
	               <#--var imageUrl3="";-->
	               <#--if(productGoodInfo.imageUrl3){-->
	                  <#--imageUrl3 =productGoodInfo.imageUrl3;-->
	               <#--}-->
	               <#---->
	               <#--var imageUrl4="";-->
	               <#--if(productGoodInfo.imageUrl4){-->
	                  <#--imageUrl4 =productGoodInfo.imageUrl4;-->
	               <#--}-->
	               <#---->
	               <#--var imageUrl5="";-->
	               <#--if(productGoodInfo.imageUrl5){-->
	                  <#--imageUrl5 =productGoodInfo.imageUrl5;-->
	               <#--}-->
	               <#---->
	               <#---->
	               <#---->
	               <#--var btn='<button id=""  class="btn btn-primary js-btn-featureName">'+name+'</button>';-->
	               <#--$('.js-btn-group').append(btn);-->
	               <#---->
	               <#--var conId="con";-->
	               <#--var goodContent='';-->
	               <#--goodContent=goodContent+-->
	               <#--'<div class="'+conId+'">'+-->
	                   <#--'<input type="hidden" id="featureProductName" name="featureProductName" value="'+name+'">'+-->
	                   <#--'<input type="hidden" id="featureProductId" name="featureProductId" value="'+featureId+'">'+-->
	                   <#--'<input type="hidden" id="operateTypeSub" name="operateTypeSub" value="update">'+-->
                       <#--'<div class="row">'+-->
						    <#--'<div class="form-group col-sm-6">'+-->
				                <#--'<label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品编号:</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                     <#--'<input type="text" class="form-control" id="productId" name="productId" value="'+productGoodInfo.productInfo.productId+'" readonly>'+-->
				                <#--'</div>'+-->
				            <#--'</div>'+-->
				            <#--'<div class="form-group col-sm-6">'+-->
				                <#--'<div class="col-sm-3">'+-->
				                     <#--'<button type="button" class="btn btn-default js-feature-del" id="productFeatureDel" name="productFeatureDel">删除</button>'+-->
				                <#--'</div>'+-->
				            <#--'</div>'+-->
			           <#--'</div>'+-->
	                <#---->
	                <#---->
	                   <#--'<div class="row">'+-->
			                <#--'<div class="form-group col-sm-6" data-type="required" data-mark="商品名称">'+-->
			                    <#--'<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品名称:</label>'+-->
			                    <#--'<div class="col-sm-9" >'+-->
			                        <#--'<input type="text" class="form-control dp-vd" id="productName" name="productName" value="'+productGoodInfo.productInfo.productName+'" readonly>'+-->
			                        <#--'<p class="dp-error-msg"></p>'+-->
			                    <#--'</div>'+-->
			                <#--'</div>'+-->
	                   <#--'</div>'+-->
	                   <#---->
			           <#--'<div class="row">'+-->
			                <#--'<div class="form-group col-sm-6" data-type="required" data-mark="商品库存">'+-->
				                <#--'<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                    <#--'<button type="button" class="btn btn-primary js-facility">选择仓库</button>'+-->
				                    <#--'<input type="hidden" class="form-control dp-vd" name="productFacilityInfosForFeatureGoods" id="productFacilityInfosForFeatureGoods" value=""/>'+-->
		                            <#--'<div class="dp-error-msg"></div>'+-->
				                <#--'</div>'+-->
				            <#--'</div>'+-->
			           <#--'</div>'+-->
			           <#---->
			           <#--'<div class="row">'+-->
			                <#--'<div class="form-group ">'+-->
			                   <#--'<label  class="col-sm-2 control-label"></label>'+-->
			                   <#--'<div class="col-sm-8">'+-->
			                      <#--'<table class="table table-bordered table_b_c js-table_3">'+-->
								      <#--'<thead>'+-->
								           <#--'<tr>'+-->
								               <#--'<th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>预警人邮箱</th><th>${uiLabelMap.BrandOption}</th>'+-->
								           <#--'</tr>'+-->
								      <#--'</thead>'+-->
								      <#--'<tbody>'+-->
								      <#--'</tbody>'+-->
					    		  <#--'</table>'+-->
			                   <#--'</div>'+-->
			                <#--'</div>'+-->
			           <#--'</div>'+-->
			           <#---->
			           <#---->
			            <#--'<div class="row">'+-->
				           <#--'<div class="form-group col-sm-6" data-type="required,format" data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/"  data-mark="销售价格">'+-->
				                <#--'<label  class="col-sm-3 control-label"><i class="required-mark">*</i>销售价格(元):</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                    <#--'<input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="'+salePrice+'" placeholder="" />'+-->
				                <#--'</div>'+-->
				                <#--'<p class="dp-error-msg"></p>'+-->
				           <#--'</div>'+-->
				           <#---->
				           <#--'<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="成本价格">'+-->
				                <#--'<label  class="col-sm-3 control-label">成本价格(元):</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                    <#--'<input type="text" class="form-control dp-vd" id="costPrice" name="costPrice" value="'+costPrice+'" placeholder="" />'+-->
				                <#--'</div>'+-->
				                <#--'<p class="dp-error-msg"></p>'+-->
				           <#--'</div>'+-->
			            <#--'</div>'+-->
			           <#---->
			            <#--'<div class="row">'+-->
				           <#--'<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="市场价格">'+-->
				                <#--'<label  class="col-sm-3 control-label">市场价格(元):</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                    <#--'<input type="text" class="form-control dp-vd" id="marketPrice" name="marketPrice" value="'+marketPrice+'" placeholder="" />'+-->
				                <#--'</div>'+-->
				                <#--'<p class="dp-error-msg"></p>'+-->
				            <#--'</div>'+-->
				            <#--'<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="重量">'+-->
				                <#---->
				                <#--'<label  class="col-sm-3 control-label">重量(kg):</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                    <#--'<input type="text" class="form-control dp-vd" id="weight" name="weight" value="'+weight+'" placeholder="" />'+-->
				                <#--'</div>'+-->
				                <#--'<p class="dp-error-msg"></p>'+-->
				            <#--'</div>'+-->
			            <#--'</div>'+-->
			          <#---->
			             <#--'<div class="row">'+-->
				            <#--'<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="体积">'+-->
				                <#--'<label  class="col-sm-3 control-label">体积(m³):</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                    <#--'<input type="text" class="form-control dp-vd" id="volume" name="volume" value="'+volume+'" placeholder="" />'+-->
				                <#--'</div>'+-->
				                <#--'<p class="dp-error-msg"></p>'+-->
				            <#--'</div>'+-->
	                    <#--'</div>'+-->
			            <#---->
	                <#---->
	                     <#--'<div class="row">'+-->
				            <#--'<div class="form-group col-sm-6" data-type="required" data-mark="商品主图片">'+-->
				               <#--'<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>'+-->
				                <#--'<div class="col-sm-9">'+-->
				                    <#--'<button type="button" class="btn btn-primary js-productcontent">添加图片</button>'+-->
				                    <#--'<input id="mainImg" name="mainImg" class="dp-vd" type="hidden">'+ -->
					                <#--'<p class="dp-error-msg"></p>'+-->
				               <#--'</div>'+-->
				            <#--'</div>'+-->
				            <#--&lt;#&ndash;-->
				            <#--'<div class="form-group col-sm-6">'+-->
				                <#--'<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+-->
				            <#--'</div>'+-->
				             <#--&ndash;&gt;-->
			            <#--'</div>'+-->
			            <#--'<div class="row">'+-->
			                <#--'<div class="form-group col-sm-6">'+-->
			                    <#--'<label  class="col-sm-2 control-label"></label>'+-->
							    <#--'<div class="col-sm-2 js-img1">'+-->
								     <#--'<img height="150"  alt="" src="'+imageUrl+'" id="img1" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img2">'+-->
								     <#--'<img height="150"  alt="" src="'+imageUrl2+'" id="img2" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img3">'+-->
								     <#--'<img height="150"  alt="" src="'+imageUrl3+'" id="img3" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img4">'+-->
								     <#--'<img height="150"  alt="" src="'+imageUrl4+'" id="img4" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
							    <#--'<div class="col-sm-2 js-img5">'+-->
								     <#--'<img height="150"  alt="" src="'+imageUrl5+'" id="img5" style="height:100px;width:100px;">'+-->
								     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>'+-->
							    <#--'</div>'+-->
			                <#--'</div>'+-->
			            <#--'</div>'+-->
	                <#--'</div>';-->
	                     <#---->
	                <#---->
	                <#--$('.content-feature').append(goodContent);-->
	                <#--var j=$('.js-btn-group').find('.btn').first().index();-->
	                <#--//alert(j);-->
	                <#--$('.content-feature').find(".con").eq(j).show().siblings().hide();-->
	                <#--//var curCls="."+conId-->
	                <#--var $model = $('.content-feature').find(".con").eq(i);-->
	                <#--getFacilityByProductId(productGoodInfo.productInfo.productId,$model); //仓库信息初始化-->
	                <#--$conMod=$('.content-feature').find('.con').eq(i);-->
	                <#---->
	                <#--$conMod.find(".js-img1").hide();-->
			        <#--$conMod.find(".js-img2").hide();-->
			        <#--$conMod.find(".js-img3").hide();-->
			        <#--$conMod.find(".js-img4").hide();-->
			        <#--$conMod.find(".js-img5").hide();-->
			        <#---->
			        <#--if(imageUrl!=""){-->
			           <#--$conMod.find(".js-img1").show();-->
		               <#--$conMod.find('#mainImg').attr('value',imageUrl);-->
			        <#--}-->
			        <#--if(imageUrl2!=""){-->
			            <#--$conMod.find(".js-img2").show();-->
			        <#--}-->
			        <#--if(imageUrl3!=""){-->
			            <#--$conMod.find(".js-img3").show();-->
			        <#--}-->
			        <#--if(imageUrl4!=""){-->
			            <#--$conMod.find(".js-img4").show();-->
			        <#--}-->
			        <#--if(imageUrl5!=""){-->
			            <#--$conMod.find(".js-img5").show();-->
			        <#--}-->
	           <#--}-->
	           <#---->
	        <#--}-->
	    <#--});-->
   <#--}-->
    function getProductFeatureNamesForUpdate1(productId){

        jQuery.ajax({
            url: '<@ofbizUrl>getProductFeatureGoodsByFeature</@ofbizUrl>',
            type: 'POST',
            data: {
                'productId' :productId
            },
            success: function(data) {
                var map=data.productFeatureInfo;
                var productFeatureNameList=map.productFeatureNameList;
                var productFeatureGoodList=map.productFeatureGoodList;
                var listSize=map.listSize;
                $('.js-btn-group').find("button").remove();
                $('.content-feature').find("div").remove();



                for(var i=0;i<listSize;i++){
                    console.log(i);

                    var name=productFeatureNameList[i].productFeatureName;
                    var featureId=productFeatureNameList[i].productFeatureId;
                    var productGoodInfo=productFeatureGoodList[i];
                    var priceList=productGoodInfo.productPriceList;
                    var priceMap=productGoodInfo.priceMap;
                    var salePrice="";
                    if(priceMap.DEFAULT_PRICE){
                        salePrice=priceMap.DEFAULT_PRICE;
                    }
                    var costPrice="";
                    if(priceMap.COST_PRICE){
                        costPrice=priceMap.COST_PRICE;
                    }
                    var marketPrice="";
                    if(priceMap.MARKET_PRICE){
                        marketPrice=priceMap.MARKET_PRICE;
                    }
                    var weight="";
                    if(productGoodInfo.productInfo.weight){
                        weight=productGoodInfo.productInfo.weight;
                    }
                    var volume="";
                    if(productGoodInfo.productInfo.volume){
                        volume =productGoodInfo.productInfo.volume;
                    }

                    var imageUrl="";
                    if(productGoodInfo.imageUrl){
                        imageUrl =productGoodInfo.imageUrl;
                    }

                    var imageUrl2="";
                    if(productGoodInfo.imageUrl2){
                        imageUrl2 =productGoodInfo.imageUrl2;
                    }

                    var imageUrl3="";
                    if(productGoodInfo.imageUrl3){
                        imageUrl3 =productGoodInfo.imageUrl3;
                    }

                    var imageUrl4="";
                    if(productGoodInfo.imageUrl4){
                        imageUrl4 =productGoodInfo.imageUrl4;
                    }

                    var imageUrl5="";
                    if(productGoodInfo.imageUrl5){
                        imageUrl5 =productGoodInfo.imageUrl5;
                    }

                    var houseTypeDescription="";
                    if(productGoodInfo.productInfo.houseTypeDescription){
                        houseTypeDescription =productGoodInfo.productInfo.houseTypeDescription;
                    }

                    var bedDescription="";
                    if(productGoodInfo.productInfo.bedDescription){
                        bedDescription =productGoodInfo.productInfo.bedDescription;
                    }

                    var bedType="";
                    if(productGoodInfo.productInfo.bedType){
                        bedType =productGoodInfo.productInfo.bedType;
                    }

                    var btn='<button id=""  class="btn btn-primary js-btn-featureName">'+name+'</button>';
                    $('.js-btn-group').append(btn);

                    var bedTypeList=["大床","双床","单人床","多床"];
                    // 娱乐商品用sku项目
                    var recreationGoodContentForUpdate=
                            '<div class="row">'+
                            '<div class="form-group col-sm-6" >'+
                            '<label  class="col-sm-3 control-label">房型描述:</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control dp-vd" id="houseTypeDescription" name="houseTypeDescription" value="'+houseTypeDescription+'" placeholder="" />'+
                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '<div class="form-group col-sm-6">'+
                            '<label  class="col-sm-3 control-label">床位描述:</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control dp-vd" id="bedDescription" name="bedDescription" value="'+bedDescription+'" placeholder="" />'+
                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '</div>'+
                            '<div class="row">'+
                            '<div class="form-group col-sm-6" >'+
                            '<label  class="col-sm-3 control-label">床型:</label>'+
                            '<div class="col-sm-9">'+
                            ' <select class="form-control dp-vd" id="bedType" name="bedType">';

                    if(bedTypeList){
                        for(var k=0;k<bedTypeList.length;k++){
                            var tempContent="";
                            if(bedType==bedTypeList[k]){
                                tempContent='<option value="'+bedTypeList[k]+'" selected="selected">'+bedTypeList[k]+'</option>';
                            }else{
                                tempContent='<option value="'+bedTypeList[k]+'">'+bedTypeList[k]+'</option>';
                            }

                            recreationGoodContentForUpdate=recreationGoodContentForUpdate+tempContent;

                        }
                    }
                    recreationGoodContentForUpdate=recreationGoodContentForUpdate+

                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+

                            '</div>';
                    console.log(recreationGoodContentForUpdate);

                    var conId="con";
                    var goodContent='';
                    goodContent=goodContent+
                            '<div class="'+conId+'">'+
                            '<input type="hidden" id="featureProductName" name="featureProductName" value="'+name+'">'+
                            '<input type="hidden" id="featureProductId" name="featureProductId" value="'+featureId+'">'+
                            '<input type="hidden" id="operateTypeSub" name="operateTypeSub" value="update">'+
                            '<div class="row">'+
                            '<div class="form-group col-sm-6">'+
                            '<label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品编号:</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control" id="productId" name="productId" value="'+productGoodInfo.productInfo.productId+'" readonly>'+
                            '</div>'+
                            '</div>'+
                            '<div class="form-group col-sm-6">'+
                            '<div class="col-sm-3">'+
//                            '<button type="button" class="btn btn-default js-feature-del" id="productFeatureDel" name="productFeatureDel">删除</button>'+
                            '</div>'+
                            '</div>'+
                            '</div>'+


                            '<div class="row">'+
                            '<div class="form-group col-sm-6" data-type="required" data-mark="商品名称">'+
                            '<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品名称:</label>'+
                            '<div class="col-sm-9" >'+
                            '<input type="text" class="form-control dp-vd" id="productName" name="productName" value="'+productGoodInfo.productInfo.productName+'" readonly>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '</div>'+
                            '</div>'+

                            '<div class="row">'+
                            '<div class="form-group col-sm-6" data-type="required" data-mark="商品库存">'+
                            '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>'+
                            '<div class="col-sm-9">'+
                            '<button type="button" class="btn btn-primary js-facility">选择仓库</button>'+
                            '<input type="hidden" class="form-control dp-vd" name="productFacilityInfosForFeatureGoods" id="productFacilityInfosForFeatureGoods" value=""/>'+
                            '<div class="dp-error-msg"></div>'+
                            '</div>'+
                            '</div>'+
                            '</div>'+

                            '<div class="row">'+
                            '<div class="form-group ">'+
                            '<label  class="col-sm-2 control-label"></label>'+
                            '<div class="col-sm-8">'+
                            '<table class="table table-bordered table_b_c js-table_3">'+
                            '<thead>'+
                            '<tr>'+
                            '<th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>预警人邮箱</th><th>${uiLabelMap.BrandOption}</th>'+
                            '</tr>'+
                            '</thead>'+
                            '<tbody>'+
                            '</tbody>'+
                            '</table>'+
                            '</div>'+
                            '</div>'+
                            '</div>'+


                            '<div class="row">'+
                            '<div class="form-group col-sm-6" data-type="required,format" data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/"  data-mark="销售价格">'+
                            '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>销售价格(元):</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="'+salePrice+'" placeholder="" />'+
                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+

                            '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="成本价格">'+
                            '<label  class="col-sm-3 control-label">成本价格(元):</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control dp-vd" id="costPrice" name="costPrice" value="'+costPrice+'" placeholder="" />'+
                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '</div>'+

                            '<div class="row">'+
                            '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="市场价格">'+
                            '<label  class="col-sm-3 control-label">市场价格(元):</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control dp-vd" id="marketPrice" name="marketPrice" value="'+marketPrice+'" placeholder="" />'+
                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="重量">'+

                            '<label  class="col-sm-3 control-label">重量(kg):</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control dp-vd" id="weight" name="weight" value="'+weight+'" placeholder="" />'+
                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '</div>'+

                            '<div class="row">'+
                            '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="体积">'+
                            '<label  class="col-sm-3 control-label">体积(m³):</label>'+
                            '<div class="col-sm-9">'+
                            '<input type="text" class="form-control dp-vd" id="volume" name="volume" value="'+volume+'" placeholder="" />'+
                            '</div>'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '</div>';

                    if('${parameters.productTypeId?if_exists}'=='RECREATION_GOOD'){
                        if($("#isHotel").val()=='Y') {
                            goodContent=goodContent+recreationGoodContentForUpdate;
                        }

                    }
                    goodContent= goodContent+

                            '<div class="row">'+
                            '<div class="form-group col-sm-6" data-type="required" data-mark="商品主图片">'+
                            '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>'+
                            '<div class="col-sm-9">'+
                            '<button type="button" class="btn btn-primary js-productcontent">添加图片</button>'+
                            '<input id="mainImg" name="mainImg" class="dp-vd" type="hidden">'+
                            '<p class="dp-error-msg"></p>'+
                            '</div>'+
                            '</div>'+
							<#--
                            '<div class="form-group col-sm-6">'+
                                '<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+
                            '</div>'+
                             -->
                            '</div>'+
                            '<div class="row">'+
                            '<div class="form-group col-sm-8">'+
                            '<label  class="col-sm-2 control-label"></label>'+
                            '<div class="col-sm-2 js-img1">'+
                            '<img height="150"  alt="" src="'+imageUrl+'" id="img1" style="width:100%;">'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgedit"  style="margin-left:3px" id="editProductContentImg1">添加</button>'+
                            '</div>'+
                            '<div class="col-sm-2 js-img2">'+
                            '<img height="150"  alt="" src="'+imageUrl2+'" id="img2" style="width:100%;">'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgedit"  style="margin-left:3px" id="editProductContentImg2">添加</button>'+
                            '</div>'+
                            '<div class="col-sm-2 js-img3">'+
                            '<img height="150"  alt="" src="'+imageUrl3+'" id="img3" style="width:100%;">'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px"  id="editProductContentImg3">添加</button>'+
                            '</div>'+
                            '<div class="col-sm-2 js-img4">'+
                            '<img height="150"  alt="" src="'+imageUrl4+'" id="img4" style="width:100%;">'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgedit"  style="margin-left:3px" id="editProductContentImg4">添加</button>'+
                            '</div>'+
                            '<div class="col-sm-2 js-img5">'+
                            '<img height="150"  alt="" src="'+imageUrl5+'" id="img5" style="width:100%;">'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>'+
                            '<button type="button"class="btn btn-danger btn-sm js-imgedit"  style="margin-left:3px" id="editProductContentImg5">添加</button>'+
                            '</div>'+
                            '</div>'+
                            '</div>'+
                            '</div>';


                    $('.content-feature').append(goodContent);
                    var j=$('.js-btn-group').find('.btn').first().index();
                    //alert(j);
                    $('.content-feature').find(".con").eq(j).show().siblings().hide();
                    //var curCls="."+conId
                    var $model = $('.content-feature').find(".con").eq(i);
                    getFacilityByProductId(productGoodInfo.productInfo.productId,$model); //仓库信息初始化
                    $conMod=$('.content-feature').find('.con').eq(i);

//	                $conMod.find(".js-img1").hide();
//			        $conMod.find(".js-img2").hide();
//			        $conMod.find(".js-img3").hide();
//			        $conMod.find(".js-img4").hide();
//			        $conMod.find(".js-img5").hide();


                    var curIsUsedFacility=$('#isUsedFacility').val();
                    if(curIsUsedFacility=='N'){
                        $conMod.find("#productFacilityInfosForFeatureGoods").removeClass("dp-vd");
                    }

                    if(imageUrl!=""){
                        $conMod.find(".js-img1").show();
                        $conMod.find('#mainImg').attr('value',imageUrl);
                    }else{
                        $conMod.find("#delProductContentImg1").hide();
                    }
                    if(imageUrl2!=""){
                        $conMod.find(".js-img2").show();
                    }else{
                        $conMod.find("#delProductContentImg2").hide();
                    }
                    if(imageUrl3!=""){
                        $conMod.find(".js-img3").show();
                    }else{
                        $conMod.find("#delProductContentImg3").hide();
                    }
                    if(imageUrl4!=""){
                        $conMod.find(".js-img4").show();
                    }else{
                        $conMod.find("#delProductContentImg4").hide();
                    }
                    if(imageUrl5!=""){
                        $conMod.find(".js-img5").show();
                    }else{
                        $conMod.find("#delProductContentImg5").hide();
                    }
                }

            }
        });
    }




    //////////////////////////////////////////////////////////////////////////////////////////////////
   
   
   // 修改的场合添加SKU信息
   function addProductFeatureInfo(productTypeId,productCategoryId,curItemName,curId){
	  if (productTypeId!="" && productCategoryId!=""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>setPrepareProductEntity</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productTypeId' : productTypeId,
	             'productCategoryId' : productCategoryId,
	             'isFeatureGoods' : "Y"
	        },
	        success: function(data) {
	           var prepareProductId=data.productId;
	           //alert(productTypeId);
	           //alert(productCategoryId);
	           //alert(curItemName);
	           //alert(prepareProductId);
	           addProductFeatureName1(curItemName,prepareProductId,curId);
	        }
	    });
	  } 
   }
   
   
     // 取得选择的特征值
   function addProductFeatureName1(curItem,productId,curId){
	   var name=curItem;
	   var featureId=curId;
       //alert(name);
       //var i=featureNamelistSize;
       var conId="con";
       var btn='<button id="'+conId+'" class="btn btn-primary">'+name+'</button>';
       $('.js-btn-group').append(btn);
       var productName ='${parameters.productName?if_exists}';
       var goodContent='';
       goodContent=goodContent+
      '<div class="'+conId+'">'+
	       '<input type="hidden" id="featureProductName" name="featureProductName" value="'+name+'">'+
	       '<input type="hidden" id="featureProductId" name="featureProductId" value="'+featureId+'">'+
	       '<input type="hidden" id="operateTypeSub" name="operateTypeSub" value="create">'+
	       '<div class="row">'+
			    '<div class="form-group col-sm-6">'+
	                '<label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品编号:</label>'+
	                '<div class="col-sm-9">'+
	                     '<input type="hidden" id="productId" name="productId" value="'+productId+'">'+
	                     '<input type="text" class="form-control" id="" name="" value="" readonly>'+
	                '</div>'+
	            '</div>'+
	            '<div class="form-group col-sm-6">'+
	                '<div class="col-sm-3">'+
//	                     '<button type="button" class="btn btn-default js-feature-del" id="productFeatureDel" name="productFeatureDel">删除</button>'+
	                '</div>'+
	            '</div>'+
	       '</div>'+
	    
	    
	       '<div class="row">'+
	            '<div class="form-group col-sm-6" data-type="required" data-mark="商品名称">'+
	                '<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品名称:</label>'+
	                '<div class="col-sm-9" >'+
	                    '<input type="text" class="form-control dp-vd" id="productName" name="productName" value="'+productName+'('+name+')" readonly>'+
	                    '<p class="dp-error-msg"></p>'+
	                '</div>'+
	            '</div>'+
	       '</div>'+
	       
	       '<div class="row">'+
	            '<div class="form-group col-sm-6" data-type="required" data-mark="商品库存">'+
	                '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>'+
	                '<div class="col-sm-9">'+
	                    '<button type="button" class="btn btn-primary js-facility">选择仓库</button>'+
	                    '<input type="hidden" class="form-control dp-vd" name="productFacilityInfosForFeatureGoods" id="productFacilityInfosForFeatureGoods" value=""/>'+
		                '<div class="dp-error-msg"></div>'+
	                '</div>'+
	            '</div>'+
	       '</div>'+
	       
	       '<div class="row">'+
	            '<div class="form-group ">'+
	               '<label  class="col-sm-2 control-label"></label>'+
	               '<div class="col-sm-8">'+
	                  '<table class="table table-bordered table_b_c js-table_3">'+
					      '<thead>'+
					           '<tr>'+
					               '<th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>预警人邮箱</th><th>${uiLabelMap.BrandOption}</th>'+
					           '</tr>'+
					      '</thead>'+
					      '<tbody>'+
					      '</tbody>'+
		    		  '</table>'+
	               '</div>'+
	            '</div>'+
	       '</div>'+
	       
	       
	        '<div class="row">'+
	           '<div class="form-group col-sm-6" data-type="required,format" data-reg="/^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$/"  data-mark="销售价格">'+
	                '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>销售价格(元):</label>'+
	                '<div class="col-sm-9">'+
	                    '<input type="text" class="form-control dp-vd" id="salePrice" name="salePrice" value="" placeholder="" />'+
	                '</div>'+
	                '<p class="dp-error-msg"></p>'+
	           '</div>'+
	           
	           '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="成本价格">'+
	                '<label  class="col-sm-3 control-label">成本价格(元):</label>'+
	                '<div class="col-sm-9">'+
	                    '<input type="text" class="form-control dp-vd" id="costPrice" name="costPrice" value="" placeholder="" />'+
	                '</div>'+
	                '<p class="dp-error-msg"></p>'+
	           '</div>'+
	        '</div>'+
	       
	        '<div class="row">'+
	           '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="市场价格">'+
	                '<label  class="col-sm-3 control-label">市场价格(元):</label>'+
	                '<div class="col-sm-9">'+
	                    '<input type="text" class="form-control dp-vd" id="marketPrice" name="marketPrice" value="" placeholder="" />'+
	                '</div>'+
	                '<p class="dp-error-msg"></p>'+
	            '</div>'+
	            '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="重量">'+
	                
	                '<label  class="col-sm-3 control-label">重量(kg):</label>'+
	                '<div class="col-sm-9">'+
	                    '<input type="text" class="form-control dp-vd" id="weight" name="weight" value="" placeholder="" />'+
	                '</div>'+
	                '<p class="dp-error-msg"></p>'+
	            '</div>'+
	        '</div>'+
	      
	         '<div class="row">'+
	            '<div class="form-group col-sm-6"  data-type="format" data-reg="/(^-?([0-9]+(?:[\.][0-9]*)?|\.[0-9]+)$)|(^\s*$)/"  data-mark="体积">'+
	                '<label  class="col-sm-3 control-label">体积(m³):</label>'+
	                '<div class="col-sm-9">'+
	                    '<input type="text" class="form-control dp-vd" id="volume" name="volume" value="" placeholder="" />'+
	                '</div>'+
	                '<p class="dp-error-msg"></p>'+
	            '</div>'+
	        '</div>'+
	        
	    
	         '<div class="row">'+
	            '<div class="form-group col-sm-6" data-type="required" data-mark="商品主图片">'+
	               '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>'+
	                '<div class="col-sm-9">'+
	                    '<button type="button" class="btn btn-primary js-productcontent">添加图片</button>'+
	                    '<input id="mainImg" name="mainImg" class="dp-vd" type="hidden">'+ 
					    '<p class="dp-error-msg"></p>'+
	               '</div>'+
	            '</div>'+
	            <#--
	            '<div class="form-group col-sm-6">'+
	                '<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+
	            '</div>'+
	            -->
	        '</div>'+
	        '<div class="row">'+
	            <#--'<div class="form-group col-sm-6">'+-->
	                <#--'<label  class="col-sm-2 control-label"></label>'+-->
				    <#--'<div class="col-sm-2 js-img1">'+-->
					     <#--'<img height="150"  alt="" src="" id="img1" style="height:100px;width:100px;">'+-->
					     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>'+-->
				    <#--'</div>'+-->
				    <#--'<div class="col-sm-2 js-img2">'+-->
					     <#--'<img height="150"  alt="" src="" id="img2" style="height:100px;width:100px;">'+-->
					     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>'+-->
				    <#--'</div>'+-->
				    <#--'<div class="col-sm-2 js-img3">'+-->
					     <#--'<img height="150"  alt="" src="" id="img3" style="height:100px;width:100px;">'+-->
					     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>'+-->
				    <#--'</div>'+-->
				    <#--'<div class="col-sm-2 js-img4">'+-->
					     <#--'<img height="150"  alt="" src="" id="img4" style="height:100px;width:100px;">'+-->
					     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>'+-->
				    <#--'</div>'+-->
				    <#--'<div class="col-sm-2 js-img5">'+-->
					     <#--'<img height="150"  alt="" src="" id="img5" style="height:100px;width:100px;">'+-->
					     <#--'<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>'+-->
				    <#--'</div>'+-->
	            <#--'</div>'+-->
               '<div class="form-group col-sm-8">'+
               '<label  class="col-sm-2 control-label"></label>'+
               '<div class="col-sm-2 js-img1">'+
               '<img height="150"  alt="" src="" id="img1" style="width:100%;">'+
               '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg1">${uiLabelMap.CommonDelete}</button>'+
               '<button type="button"class="btn btn-danger btn-sm js-imgedit"  style="margin-left:3px" id="editProductContentImg1">添加</button>'+
               '</div>'+
               '<div class="col-sm-2 js-img2">'+
               '<img height="150"  alt="" src="" id="img2" style="width:100%;">'+
               '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg2">${uiLabelMap.CommonDelete}</button>'+
               '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px" id="editProductContentImg2">添加</button>'+
               '</div>'+
               '<div class="col-sm-2 js-img3">'+
               '<img height="150"  alt="" src="" id="img3" style="width:100%;">'+
               '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg3">${uiLabelMap.CommonDelete}</button>'+
               '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px" id="editProductContentImg3">添加</button>'+
               '</div>'+
               '<div class="col-sm-2 js-img4">'+
               '<img height="150"  alt="" src="" id="img4" style="width:100%;">'+
               '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg4">${uiLabelMap.CommonDelete}</button>'+
               '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px" id="editProductContentImg4">添加</button>'+
               '</div>'+
               '<div class="col-sm-2 js-img5">'+
               '<img height="150"  alt="" src="" id="img5" style="width:100%;">'+
               '<button type="button"class="btn btn-danger btn-sm js-imgdel"  id="delProductContentImg5">${uiLabelMap.CommonDelete}</button>'+
               '<button type="button"class="btn btn-danger btn-sm js-imgedit" style="margin-left:3px" id="editProductContentImg5">添加e</button>'+
               '</div>'+
               '</div>'+
	        '</div>'+
	    '</div>';
        $('.content-feature').append(goodContent);
        $('#addProductSku').modal('hide')
        var j=$('.js-btn-group').find('.btn').last().index();
	    $('.content-feature').find(".con").eq(j).show().siblings().hide();
	    $conMod=$('.content-feature').find('.con').eq(j);
//	    $conMod.find(".js-img1").hide();
//        $conMod.find(".js-img2").hide();
//        $conMod.find(".js-img3").hide();
//        $conMod.find(".js-img4").hide();
//        $conMod.find(".js-img5").hide();


       $conMod.find("#delProductContentImg1").hide();
       $conMod.find("#delProductContentImg2").hide();
       $conMod.find("#delProductContentImg3").hide();
       $conMod.find("#delProductContentImg4").hide();
       $conMod.find("#delProductContentImg5").hide();
	    
   }
   
   
   <#--// 删除图片-->
   <#--function delProductContentImg(){-->
       <#--var productContentTypeId=imgContentTypeId;-->
       <#--var productId=$conDelMod.find("#productId").val();-->
       <#--jQuery.ajax({-->
            <#--url: '<@ofbizUrl>delProductContentImg</@ofbizUrl>',-->
            <#--type: 'POST',-->
            <#--data: {-->
                <#--'productId' : productId,-->
                <#--'productContentTypeId' : productContentTypeId-->
            <#--},-->
            <#--success: function(data) {-->
                <#--var productFeatureGoodItem=data.productFeatureGoodItem;-->
                <#--if(productFeatureGoodItem.typeNo=='1'){-->
                   <#--$conDelMod.find('#img1').attr('src','');-->
                   <#--$('#mainImg').attr('value','');-->
                   <#--$conDelMod.find(".js-img1").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='2'){-->
                   <#--$conDelMod.find('#img2').attr('src','');-->
                   <#--$conDelMod.find(".js-img2").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='3'){-->
                   <#--$conDelMod.find('#img3').attr('src','');-->
                   <#--$conDelMod.find(".js-img3").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='4'){-->
                   <#--$conDelMod.find('#img4').attr('src','');-->
                   <#--$conDelMod.find(".js-img4").hide();-->
                <#--}else if(productFeatureGoodItem.typeNo=='5'){-->
                   <#--$conDelMod.find('#img5').attr('src','');-->
                   <#--$conDelMod.find(".js-img5").hide();-->
                <#--}-->
            <#--}-->
        <#--});-->
    <#--}-->


    // 删除图片
    function delProductContentImg(){
        var productContentTypeId=imgContentTypeId;
        var productId=$conDelMod.find("#productId").val();
        jQuery.ajax({
            url: '<@ofbizUrl>delProductContentImg</@ofbizUrl>',
            type: 'POST',
            data: {
                'productId' : productId,
                'productContentTypeId' : productContentTypeId
            },
            success: function(data) {
                var productFeatureGoodItem=data.productFeatureGoodItem;
                if(productFeatureGoodItem.typeNo=='1'){
                    $conDelMod.find('#img1').attr('src','');
                    $('#mainImg').attr('value','');
                    $conDelMod.find('#editProductContentImg1').text("添加");
                    $conDelMod.find('#delProductContentImg1').hide();
//                   $conDelMod.find(".js-img1").hide();
//                    $('#addImg1').attr('value','');
                }else if(productFeatureGoodItem.typeNo=='2'){
                    $conDelMod.find('#img2').attr('src','');
                    $conDelMod.find('#editProductContentImg2').text("添加");
                    $conDelMod.find('#delProductContentImg2').hide();
//                   $conDelMod.find(".js-img2").hide();
                }else if(productFeatureGoodItem.typeNo=='3'){
                    $conDelMod.find('#img3').attr('src','');
                    $conDelMod.find('#editProductContentImg3').text("添加");
                    $conDelMod.find('#delProductContentImg3').hide();
//                   $conDelMod.find(".js-img3").hide();
                }else if(productFeatureGoodItem.typeNo=='4'){
                    $conDelMod.find('#img4').attr('src','');
                    $conDelMod.find('#editProductContentImg4').text("添加");
                    $conDelMod.find('#delProductContentImg4').hide();
//                   $conDelMod.find(".js-img4").hide();
                }else if(productFeatureGoodItem.typeNo=='5'){
                    $conDelMod.find('#img5').attr('src','');
                    $conDelMod.find('#editProductContentImg5').text("添加");
                    $conDelMod.find('#delProductContentImg5').hide();
//                   $conDelMod.find(".js-img5").hide();
                }
            }
        });
    }



    // 判断库存是否是使用
  function isFacilityUsed(facilityId,productId){
	  if (facilityId!=""){
	    jQuery.ajax({
	        url: '<@ofbizUrl>isFacilityForProduct</@ofbizUrl>',
	        type: 'POST',
	        data: {
	             'productId' : productId,
	             'facilityId' : facilityId
	        },
	        success: function(data) {
	           var isUsedFlg=data.isUsedFlg;
	           if(isUsedFlg=="Y"){
	               //设置提示弹出框内容
	    	  	  $('#modal_msg #modal_msg_body').html("该商品的库存正在使用中，不能删除");
	    	  	  $('#modal_msg').modal();
	           }else{
		       	   //设置删除弹出框内容
				   $('#modal_confirm #modal_confirm_body').html("确定要删除该商品库存信息，是否继续删除");
				   $('#modal_confirm').modal('show');
		       	  
	           }
	        }
	    });
	  } 
   }

    // 编辑图片
    function imageManageEdit() {
        var imgType="product";
        var imgProductId=$conMod.find("#productId").val();
		console.log(imgProductId);
        var curPartyId='${requestAttributes.userLogin.partyId?if_exists}';
        var curIsInner='${requestAttributes.isInner?if_exists}';
        $.chooseImage.int({
            userId: 3446,
            serverChooseNum: 1,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: '',
            imgIndex:imgContentIndex,
            otherObj:{
                type:imgType,
                productId:imgProductId,
                curProductContentTypeId:imgContentTypeId,
                ownerPartyId:curPartyId,
                isInner:curIsInner
            }
        });
        $.chooseImage.show()
    }





    //取得mobileDetails信息
    function getMobileDetailsInfo(){

		$.ajax({
			url:'<@ofbizUrl>getSessionByParam</@ofbizUrl>',
			dataType:'json',
			type:'post',
			data:{
				'attrName' : "mobileDetails"
			},
			success:function(data){
                if(data.attrVal){
                    $("#mobileDetails").val(data.attrVal);
                }
			},
			error:function(){
				alert("操作失败")
			}
		})
    }

</script>

		
