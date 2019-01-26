
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="ProductFM_QueryForm" class="form-inline clearfix">
            <input type="hidden" id="productTypeId" name="productTypeId" value="${parameters.productTypeId?if_exists}">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品编码</span>
                    <input type="text" class="form-control" id="productId">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" class="form-control" id="productName">
                </div>
            </div>
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品价格</span>
                    <input type="text" name="productPriceStart" id = "productPriceStart" onkeyup="value=this.value.replace(/\D+/g,'')">
                    <span>-</span>
                    <input type="text" name="productPriceEnd" id = "productPriceEnd" onkeyup="value=this.value.replace(/\D+/g,'')">
                </div>
            </div>

            <div class="input-group pull-right m-l-10">
                <button id="searchBtn" class="btn btn-success btn-flat">${uiLabelMap.BrandSearch}</button>
            </div>
    </div>
    </form><!-- 条件查询end -->

    <!-- 分割线start -->
    <div class="cut-off-rule bg-gray"></div>
    <!-- 分割线end -->

    <!-- 表格区域start -->
    <div class="row">
        <div class="col-sm-12">
            <table id="ProductFM_data_tbl" class="table table-bordered table-hover js-checkparent"></table>
        </div>
    </div><!-- 表格区域end -->

    <!-- 分页条start -->
    <div class="row" id="ProductFM_paginateDiv">
    </div><!-- 分页条end -->
</div><!-- /.box-body -->
</div><!-- 内容end -->

<!-- script区域start -->
<script>
    var ProductFM_data_tbl;
    var ProductFM_ajaxUrl = "/catalog/control/ProductListForJson?VIEW_SIZE=5&productTypeId=FINISHED_GOOD";
    $(function(){
        ProductFM_data_tbl = $('#ProductFM_data_tbl').dataTable({
            ajaxUrl: ProductFM_ajaxUrl,
            columns:[
                {"title":"商品编码","code":"productId"},
                {"title":"商品名称","code":"productName"},
                {"title":"商品类型","code":"productTypeName"},
                {"title":"商品分类","code":"categoryName"},
                {"title":"商品价格","code":"price"},
                {"title":"商品图片","code":"imgUrl",
                    "handle":function(td,record){
                        var img="";
                        if(record.imgUrl){
                            img = "<img class='img-responsive' style='max-height: 70px;max-width: 70px;' src='"+record.imgUrl+"'>";
                        }
                        var div =$("<div class='col-sm-12'  align='center'>"+img+"</div>");
                        td.html(div);
                    }
                },
                {"title":"操作","code":"option",
                    "handle":function(td,record){
                        var btn = $("<div class='btn-group'>"+
                                "<button type='button' class='btn btn-danger btn-sm btn-select' data-price='"+ record.price +"' data-id='"+record.productId+"' data-typeName='"+record.productTypeName+"' data-name='"+record.productName+"' data-imgUrl='"+record.imgUrl+"'>选择</button>"+
                                "</div>");
                        td.append(btn);
                    }
                }
            ],
            listName: "recordsList",
            paginateEL: "ProductFM_paginateDiv",
            headNotShow: true,
            midShowNum: 3
        });

        //查询按钮点击事件
        $('#ProductFM_QueryForm #searchBtn').on('click',function(){
            var productTypeId = $('#ProductFM_QueryForm #productTypeId').val();
            var productId = $('#ProductFM_QueryForm #productId').val();
            var productName = $('#ProductFM_QueryForm #productName').val();

            var productPriceStart = $.trim($('#ProductFM_QueryForm #productPriceStart').val());
            var productPriceEnd = $.trim($('#ProductFM_QueryForm #productPriceEnd').val());

            ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productTypeId",productTypeId);
            ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productId",productId);
            ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productName",productName);
            ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productPriceStart",productPriceStart);
            ProductFM_ajaxUrl = changeURLArg(ProductFM_ajaxUrl,"productPriceEnd",productPriceEnd);

            ProductFM_data_tbl.reload(ProductFM_ajaxUrl);
            return false;
        });
    });


</script><!-- script区域end -->
