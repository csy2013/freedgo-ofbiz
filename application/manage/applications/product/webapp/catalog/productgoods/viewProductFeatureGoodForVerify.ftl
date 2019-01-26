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
	            <button type="button" class="btn btn-primary" id="btnReturn">返回</button>
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



<script language="JavaScript" type="text/javascript">
    $(function(){
       var productId="${parameters.productId?if_exists}"
       getProductFeatureNames(productId);
    
    
        
        $('.js-btn-group').on('click','.btn',function(){
			//var i = $(this).index();
			//var dc= '.con'+i;
			//alert(i);
			//$('.content-feature').find(dc).show().siblings().hide();
			//return false;
			
			var i = $(this).index();
			$('.content-feature').find('.con').eq(i).show().siblings().hide();
			return false;
		});
		
		
		
		 // 返回按钮的操作
	     $("#btnReturn").click(function(){
	         document.location.href="<@ofbizUrl>viewProductGoodForVerify?productId="+${parameters.productId?if_exists}+"&operateType=view</@ofbizUrl>";
	         
	     })
	     
	     
	     $("#btnProductIsOnline").click(function(){
	         <#list productList as productInfo>
		         <#if prod.isOnline=='Y'>
		            editProductOnlineStatus('${productInfo.productId?if_exists}','N');
		            editProductOnlineStatus('${parameters.productId?if_exists}','N');
		         <#else>
		            editProductOnlineStatus('${productInfo.productId?if_exists}','Y');
		            editProductOnlineStatus('${parameters.productId?if_exists}','Y');
		         </#if>
	         </#list>
	         document.location.href="<@ofbizUrl>findProductGoods?lookupFlag=Y</@ofbizUrl>"; 
	     })
	     
	     // 修改按钮的处理
	     $("#btnModify").click(function(){
	          document.location.href="<@ofbizUrl>editProductGood?productId="+${parameters.productId?if_exists}+"&productCategoryId="+$("#productCategoryId").val()+"&productTypeId="+$("#productTypeId").val()+"&operateType=update&gobackPage=viewPage</@ofbizUrl>";
	     })
    })
    
    
    // 取得选择的特征值
   function getProductFeatureNames(productId){
       
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
	           
	               //var name=productFeatureNameList[i].productFeatureName;
	               //var productGoodInfo=productFeatureGoodList[i];
	               //var priceList=productGoodInfo.productPriceList;
	               //var salePrice=priceList[2].price;
	               //var costPrice=priceList[0].price;
	               //var marketPrice=priceList[1].price;
	               
	               
	               var name=productFeatureNameList[i].productFeatureName;
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
	               
	               
	               //alert(name);
	               var btn='<button id="" class="btn btn-primary js-btn-featureName">'+name+'</button>';
	               $('.js-btn-group').append(btn);
	               
	               var conId="con";
	               var goodContent='';
	               goodContent=goodContent+
	               '<div class="'+conId+'">'+
                       '<div class="row">'+
						    '<div class="form-group col-sm-6">'+
				                '<label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>商品编号:</label>'+
				                '<div class="col-sm-9">'+
				                     '<input type="text" class="form-control" id="productId" name="productId" value="'+productGoodInfo.productInfo.productId+'" readonly>'+
				                '</div>'+
				            '</div>'+
				            '<div class="form-group col-sm-6">'+
				                '<div class="col-sm-3">'+
//				                     '<button type="button" class="btn btn-default js-feature-del" id="productFeatureDel" name="productFeatureDel">删除</button>'+
				                '</div>'+
				            '</div>'+
			           '</div>'+
	                
	                
	                   '<div class="row">'+
			                '<div class="form-group col-sm-6" data-type="required" data-mark="商品名称">'+
			                    '<label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>商品名称:</label>'+
			                    '<div class="col-sm-9" >'+
			                        '<input type="text" class="form-control dp-vd" id="productName" name="productName" value="'+productGoodInfo.productInfo.productName+'">'+
			                        '<p class="dp-error-msg"></p>'+
			                    '</div>'+
			                '</div>'+
	                   '</div>'+
	                   
	                   '<div class="row">'+
			                '<div class="form-group col-sm-6">'+
				                '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品库存:</label>'+
				                '<div class="col-sm-9">'+
				                    '<button type="button" class="btn btn-primary js-facility">选择仓库</button>'+
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
								               '<th>仓库名称</th><th>库存总量</th> <th>已锁定仓库</th> <th>可用库存</th> <th>预警数量</th><th>${uiLabelMap.BrandOption}</th>'+
								           '</tr>'+
								      '</thead>'+
								      '<tbody>'+
								      '</tbody>'+
					    		  '</table>'+
			                   '</div>'+
			                '</div>'+
			           '</div>'+
			           '<div class="row">'+
				           '<div class="form-group col-sm-6">'+
				                '<label  class="col-sm-3 control-label">销售价格(元):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control" id="salePrice" name="salePrice" value="'+salePrice+'" placeholder="" />'+
				                '</div>'+
				           '</div>'+
				           
				           '<div class="form-group col-sm-6">'+
				                '<label  class="col-sm-3 control-label">成本价格(元):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control" id="costPrice" name="costPrice" value="'+costPrice+'" placeholder="" />'+
				                '</div>'+
				           '</div>'+
			            '</div>'+
			           '<div class="row">'+
				           '<div class="form-group col-sm-6">'+
				                '<label  class="col-sm-3 control-label">市场价格(元):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control" id="marketPrice" name="marketPrice" value="'+marketPrice+'" placeholder="" />'+
				                '</div>'+
				                
				            '</div>'+
				            '<div class="form-group col-sm-6">'+
				                
				                '<label  class="col-sm-3 control-label">重量(kg):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control" id="weight" name="weight" value="'+weight+'" placeholder="" />'+
				                '</div>'+
				            '</div>'+
			            '</div>'+
			             '<div class="row">'+
				            '<div class="form-group col-sm-6">'+
				                '<label  class="col-sm-3 control-label">体积(m³):</label>'+
				                '<div class="col-sm-9">'+
				                    '<input type="text" class="form-control" id="volume" name="volume" value="'+volume+'" placeholder="" />'+
				                '</div>'+
				            '</div>'+
	                    '</div>'+
	                
	                    '<div class="row">'+
				            '<div class="form-group col-sm-6">'+
				               '<label  class="col-sm-3 control-label"><i class="required-mark">*</i>商品图片:</label>'+
				                '<div class="col-sm-9">'+
				                    '<button type="button" class="btn btn-primary" id="btnAddProductContent">添加图片</button>'+
				               '</div>'+
				            '</div>'+
				            <#--
				            '<div class="form-group col-sm-6">'+
				                '<div class="col-sm-6"><div class="col-sm-12 dp-form-remarks">图片应用到其他sku</div></div>'+
				            '</div>'+
				            -->
			            '</div>'+
			            '<div class="row">'+
			                '<div class="form-group col-sm-6">'+
			                    '<label  class="col-sm-2 control-label"></label>'+
							    '<div class="col-sm-2 js-img1">'+
								     '<img height="150"  alt="" src="'+imageUrl+'" id="img1" style="height:100px;width:100px;">'+
							    '</div>'+
							    '<div class="col-sm-2 js-img2">'+
								     '<img height="150"  alt="" src="'+imageUrl2+'" id="img2" style="height:100px;width:100px;">'+
							    '</div>'+
							    '<div class="col-sm-2 js-img3">'+
								     '<img height="150"  alt="" src="'+imageUrl3+'" id="img3" style="height:100px;width:100px;">'+
							    '</div>'+
							    '<div class="col-sm-2 js-img4">'+
								     '<img height="150"  alt="" src="'+imageUrl4+'" id="img4" style="height:100px;width:100px;">'+
							    '</div>'+
							    '<div class="col-sm-2 js-img5">'+
								     '<img height="150"  alt="" src="'+imageUrl5+'" id="img5" style="height:100px;width:100px;">'+
							    '</div>'+
			                '</div>'+
			            '</div>'+
	                '</div>';
	                
	                //$('.content-feature').append(goodContent);
	                //$('.content-feature').find(".con0").show().siblings().hide();
	                //var curCls="."+conId
	                //var $model = $(curCls);
	                //getFacilityByProductId(productGoodInfo.productInfo.productId,$model); //仓库信息初始化
	                
	                $('.content-feature').append(goodContent);
	                var j=$('.js-btn-group').find('.btn').first().index();
	                $('.content-feature').find(".con").eq(j).show().siblings().hide();
	                var $model = $('.content-feature').find(".con").eq(i);
	                getFacilityByProductId(productGoodInfo.productInfo.productId,$model); //仓库信息初始化
	                
	                
	                $("button").attr("disabled","disabled");
	                $(".js-btn-featureName").removeAttr("disabled");
	 			    $("#btnProductIsOnline").removeAttr("disabled");
	 			    $("#btnModify").removeAttr("disabled");
	 			    $("#btnReturn").removeAttr("disabled");
	 			    
	 			    $(".js-img1").hide();
			        $(".js-img2").hide();
			        $(".js-img3").hide();
			        $(".js-img4").hide();
			        $(".js-img5").hide();
			        if(imageUrl!=""){
			            $(".js-img1").show();
			        }
			        if(imageUrl2!=""){
			            $(".js-img2").show();
			        }
			        if(imageUrl3!=""){
			            $(".js-img3").show();
			        }
			        if(imageUrl4!=""){
			            $(".js-img4").show();
			        }
			        if(imageUrl5!=""){
			            $(".js-img5").show();
			        }
	           }
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
	                                productFacilityInfos;
	       
           tproductFeatureGoodsInfos = tproductFeatureGoodsInfos + "*" + curFeatureGoodsInfosInfo;
	    }); 
	    var productFeatureGoodsInfos=tproductFeatureGoodsInfos.substr(1,tproductFeatureGoodsInfos.length);
	    //alert(productFeatureGoodsInfos);
	    $("#productFeatureGoodsInfos").val(productFeatureGoodsInfos);
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
	              var totalNum=facilityInfoList[i].totalNum;
	              var facilityId=facilityInfoList[i].facilityId;
	              
	              //alert(facilityId);
	              
	              //var tr='<tr><td>'+facilityName+'</td><tr>';
	              
	              var tr1=' <tr><td><input type="hidden" name="" id="" value="'+facilityId+'">'+facilityName+'</td> <td>'+totalNum+'</td><td>'+alreadyLockQuantitySum+'</td><td>'+accountingQuantityTotal+'</td><td>'+warningQuantity+'</td> <td class="fc_td"><button type="button" class="js-button btn btn-danger btn-sm">${uiLabelMap.BrandDel}</button></td></tr>';
	              
	             // var tr2='<tr><td>'+facilityname+'</td></tr>';
	 			  //alert(tr1);
	 			  model.find('.js-table_3>tbody').append(tr1);
	 			  $("button").attr("disabled","disabled");
	 			  
	 			  $(".js-btn-featureName").removeAttr("disabled");
	 			  $("#btnProductIsOnline").removeAttr("disabled");
	 			  $("#btnModify").removeAttr("disabled");
	 			  $("#btnReturn").removeAttr("disabled");
	           }
	        }
	    });
	  } 
   }
   
   // 上/下架
	function editProductOnlineStatus(checkedIds,onlineStatus){
	  if (checkedIds != ""){
	        // 选中的项目
           	jQuery.ajax({
                 url: '<@ofbizUrl>updateProductIsOnlineStatus</@ofbizUrl>',
                type: 'POST',
                data: {
                   'checkedIds' : checkedIds,
                   'onlineStatus': onlineStatus
                },
                success: function(data) {
                
                }
            });
	   }
	}
	
  
    
    
</script>

		
