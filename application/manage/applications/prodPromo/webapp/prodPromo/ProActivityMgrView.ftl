<script type="text/javascript" src="<@ofbizContentUrl>/images/ckeditor/ckeditor.js</@ofbizContentUrl>"></script>

<style>
	.table-bordered{
		border: 1px solid #d2d6de;
	}
	.table-bordered>thead>tr>th, 
	.table-bordered>tbody>tr>th, 
	.table-bordered>tfoot>tr>th, 
	.table-bordered>thead>tr>td, 
	.table-bordered>tbody>tr>td, 
	.table-bordered>tfoot>tr>td {
		border: 1px solid #d2d6de;
	}
	
	a.empty {
		position: relative;
		display: block;
		background: #ededed;
		text-align: center;
		height: 100px;
		margin-right: 4px;
		overflow: hidden;
		zoom: 1;
		text-align: center;
		opacity: 0.7;
		border: 1px dashed #b4b4b4;
		height: 98px;
	}
	
	a.empty:hover{
		text-decoration: none;
		opacity: 1;
	}
	
	.btn_uploadFile {
		position: absolute;
		display: block;
		left: 0;
		top: 0;
		z-index: 10;
		cursor: pointer;
		opacity: 0;
		filter: alpha(opacity=0);
		overflow: hidden;
		zoom: 1;
	}
	
	.reselect{
		display:none;
		width: 100%;
		height: 100%;
		text-align: center;
		border: 1px dashed #b4b4b4;
		background: #ededed;
		z-index: 999;
		position: absolute;
		top: 0px;
		opacity: 0.5;
		cursor: pointer;
	}
	
	.reselect-text{
		position: absolute;
		bottom: 0px;
		left: 60px;
		color: #7E7E7E;
		font-size: 20px;
		font-weight: 700;
	}
</style>

<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<!-- 内容start -->
<div class="box box-info">
	<form id="ProActivityMgrEditForm" class="form-horizontal">
        <!--店铺编码-->
        <input type="hidden" id="productStoreId" name="productStoreId" value="${requestAttributes.productStoreId}"/>
        <input type="hidden" id="linkId"/>
        <input type="hidden" id="selectName"/>
		<input type="hidden" id="activityManagerId" />
		<div class="box-body">
			<div class="row">
				<div class="form-group col-sm-12" data-type="required" data-mark="专题名称">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>专题名称:</label>
                    <div class="col-sm-10" >
                    	<div class="col-sm-5" style="padding-left: 0px;">
							<input type="text" class="form-control dp-vd" id="activityManagerName" />
							<p class="dp-error-msg"></p>
						</div>
                    </div>
                </div>
			</div>
            <div class="row">
                <div class="form-group col-sm-12" data-type="format" data-reg="/^[1-9]\d*$/" data-mark="序号">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>序号:</label>
                    <div class="col-sm-10" >
                        <div class="col-sm-5" style="padding-left: 0px;">
                            <input type="text" class="form-control dp-vd" id="sequenceId" />
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="专题图片">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>专题图片:</label>
                    <div class="col-sm-10">
                        <img alt="" src="" id="img" style="max-height: 100px;max-width: 200px;">
                        <#--<input style="margin-left:5px;" type="button" id="uploadedFile" name="uploadedFile" value="选择图片"/>-->
                        <input type="hidden" id="contentId" class="dp-vd" />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="专题栏目">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>专题栏目:</label>
                    <div class="col-sm-10">
                        <#--<input style="margin-left:5px;" type="button" id="addSubjectColumn" name="addSubjectColumn" value="新增"/>-->
					<#--<input type="hidden" id="subjectColumnInfo" class="dp-vd" />-->
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-12" data-type="required" data-mark="关联商品">
                    <label class="col-sm-2 control-label"><i class="required-mark">*</i>关联商品:</label>
                    <div class="col-sm-10">
                        <div class="box-header with-border">
                            <ul class="nav nav-tabs js-ul">
                            </ul>
                        </div>

                        <div class="con">

                        </div>
                    </div>
                </div>
			</div>
		</div><!-- box-body end -->
		
		<!-- 按钮组 -->
		<div class="box-footer text-center">
			<!-- <button id="preview" type="button" class="btn btn-primary m-r-10">预览</button> -->
            <button id="save" type="button" class="btn btn-primary m-r-10">返回</button>
        </div>
  	</form>
</div><!-- 内容end -->

<!-- 新增栏目弹出框start -->
<div id="modal_add"  class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_add_title">新增栏目</h4>
            </div>
            <div class="modal-body">
                <form id="addForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl></@ofbizUrl>">
                    <div class="row">
                        <div class="form-group" data-type="required" data-mark="栏目">
                            <label class="control-label col-sm-2"><i class="required-mark">*</i>栏目名称:</label>
                            <div class="col-sm-8">
                                <input type="text" class="form-control dp-vd" id="columnName" name="columnName">
							<#--<textarea class="form-control dp-vd js-question" rows="6" value=""></textarea>-->
                                <p class="dp-error-msg"></p>
                            </div>
                        </div>
                    </div>

                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">${uiLabelMap.BrandSave}</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">${uiLabelMap.BrandCancel}</button>
            </div>
        </div>
    </div>
</div><!-- 新增栏目弹出框end -->
<!-- script区域start -->
<script>
$(function(){
	//初始化数据
    ///////////////////////////////////////////////
	$.ajax({
		url: "/prodPromo/control/getActivityMgrById${externalKeyParam}",
		type: "POST",
		data : {activityManagerId:'${request.getParameter('activityManagerId')}'},
		dataType : "json",
		async : false,
		success: function(data){
			$('#activityManagerId').val(data.activityMgrInfo.productActivityManagerId);
			$('#activityManagerName').val(data.activityMgrInfo.activityManagerName);
            $('#sequenceId').val(data.activityMgrInfo.sequenceId);
			$('#contentId').val(data.activityMgrInfo.contentId);
            var imgUrl='/content/control/getImage?contentId='+data.activityMgrInfo.contentId;
			$("img").attr("src",imgUrl);
			var columnMapList=data.columnMapList;
			for(var i=0;i<columnMapList.length;i++){
				var curColInfo=columnMapList[i];
				var curColumnName=curColInfo.columnName;
				console.log(curColInfo);
				if(curColumnName){
                    getSubjectColumnInfo(i,curColumnName);
				}
				var curProductList=curColInfo.productList;
                console.log(curProductList);
				for(var j=0;j<curProductList.length;j++){
					var curProductInfo=curProductList[j];
					// 商品图片
					var imgUrl=curProductInfo.imgUrl;
					// 商品名称
                    var productName=curProductInfo.productName;
					// 商品规格
                    var productGoodFeature=curProductInfo.featureInfo;
					// 商品编号
                    var productId=curProductInfo.productId;
                    addColumnProductInfo(productId,productName,productGoodFeature,imgUrl,i);
				}
			}
		},
		error: function(data){
			$.tipLayer("操作失败！");
		}
	});


    var liSize= $(".js-col-title").length;
    if(liSize){
        $(".js-col-title li:first").addClass("active").siblings().removeClass("active");

        $(".con .js-col"+0).show().siblings().hide();
    }


    $(document).on('click','.js-col-title',function(){
        var curIndex=$(this).index();
        $(this).addClass("active").siblings().removeClass("active");
        $(".con .js-col"+curIndex).show().siblings().hide();
    })




    ///////////////////////////////////////////////////


	//返回按钮点击事件
	$('#save').click(function(){
        window.location = '<@ofbizUrl>activityMgrPage</@ofbizUrl>';
	});


    $('input').attr("disabled","disabled");

});




// 取得栏目信息
function getSubjectColumnInfo(i,colName){

    var strli='<li class="js-recommend-product js-col-title" data-id='+i+'><a href="javascript:void(0)" >'+colName+'</a></li>';
    var strCol= '<div class="box-body js-product js-col'+i+'">'+
            '    <div class="row">'+
            '        <div class="form-group col-sm-12" >'+
            '            <div class="col-sm-12">'+
//            '                <input type="button" id="btn_prod_add'+1+'" data-id="'+i+'" class="btn btn-primary js-addProducts" value="添加"/>'+
            '                <input type="hidden" id="prodIds'+i+'"/>'+
            '                <input type="hidden" id="prodNames'+i+'"/>'+
            '                <table id="prodtbl'+i+'" class="table table-bordered table-hover m-t-10">'+
            '                    <thead>'+
            '                    <tr>'+
            '                        <th>商品图片</th>'+
            '                        <th>商品名称</th>'+
            '                        <th>规格</th>'+
            '                        <th>商品编号</th>'+
//            '                        <th>操作</th>'+
            '                    </tr>'+
            '                    </thead>'+
            '                    <tbody>'+
            '                    </tbody>'+
            '                </table>'+
            '            </div>'+
            '        </div>'+
            '    </div>'+
            '</div>';
    $('.con').append(strCol);
    $('.js-ul').append(strli);
    var liSize= $(".js-col-title").length;
    $(".js-col-title").each(function(){
        var curIndex=$(this).index();
        if(curIndex=='0'){
            $(this).addClass("active");
            $(".con .js-col"+curIndex).show();
        }else{
            $(this).removeClass("active");
            $(".con .js-col"+curIndex).hide();
        }
        console.log(curIndex);
    })
}


/**
 * 根据条件添加栏目商品
 * @param productId
 * @param productName
 * @param productGoodFeature
 * @param imgUrl
 * @param curIndex
 */
function addColumnProductInfo(productId,productName,productGoodFeature,imgUrl,curIndex){
    var tr = '<tr id="' + productId + '">'
            + '<td><img height="100" src="'+imgUrl+'" class="cssImgSmall" alt="" /></td>'
            + '<td>' + productName + '</td>'
            + '<td>' + productGoodFeature + '</td>'
            + '<td>' + productId + '</td>'
//            + '<td class="fc_td"><button type="button" data-id="'+productId+'" data-col="'+curIndex+'" class="js-button-assocgood btn btn-danger btn-sm">删除</button></td>'
            + '</tr>';
    $('#prodtbl'+curIndex+'>tbody').append(tr);
    var curProductIds= $('.js-col'+curIndex).find('#prodIds'+curIndex).val();
    if(curProductIds==""){
        curProductIds=productId;
    }else{
        curProductIds+=','+productId;
    }
    $('.js-col'+curIndex).find('#prodIds'+curIndex).val(curProductIds);
}
/////////////////////////////////////////////////////
</script><!-- script区域end -->
