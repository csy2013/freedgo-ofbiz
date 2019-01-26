<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/css/order.css</@ofbizContentUrl>" type="text/css"/>
<!-- 内容start -->
<div class="box box-info">
    <form id="form" class="form-horizontal" action="<@ofbizUrl>orderNext</@ofbizUrl>" method="post">
        <div class="box-body">
        <#--<div class="row">
            <div class="form-group col-sm-6" data-type="required" data-mark="会员用户名">
                <label for="currenPartyId" class="col-sm-3 control-label"><i class="required-mark">*</i>会员用户名</label>
                <div class="col-sm-9">
                    <input type="text" class="form-control dp-vd" id="currenPartyId" name="currenPartyId" value="${currenPartyId!''}">
                    <p class="dp-error-msg"></p>
                </div>
            </div>
        </div>-->
            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="会员编码">
                    <label class="col-sm-3 control-label"><i class="required-mark">*</i>会员编码</label>
                    <div class="col-sm-9" >
                        <div class="col-sm-5 input-group input-group-sm" style="padding-left:0px">
                            <input type="text" id="partyId" name="currenPartyId" class="form-control dp-vd" value="${currenPartyId!''}" readonly style="height:34px;border-radius:0px;">
		                    <span class="input-group-btn">
		                      <button class="btn btn-default btn-flat" type="button" id="party_search" style="height:34px;">
                                  <i class="fa fa-search"></i>
                              </button>
		                    </span>
                        </div>
                        <p id="party_msg" class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
        <div class="row">
            <div class="form-group col-sm-6" data-type="required" data-msg="请至少选择一个商品">
                <label  class="col-sm-3 control-label"><i class="required-mark">*</i>订单商品</label>
                <div class="col-sm-9">
                    <div class="dp-tables_btn">
                        <button id="addProducts" type="button" class="btn btn-primary">
                            <i class="fa fa-plus">添加商品</i>
                        </button>
                    </div>
                    <input type="hidden" class="form-control dp-vd" id="productSize" name="productSize" value="${productSize!''}">
                    <p class="dp-error-msg"></p>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="form-group col-sm-12">
                <label  class="col-sm-3 control-label">已选择货品</label>
                <div class="col-sm-9">
                    <table class="table table-bordered table-hover js-checkparent" id = "seletedProductTable">
                        <thead>
                            <tr class="js-sort-list">
                                <th>货品图片</th>
                                <th>货品编号</th>
                                <th>货品名称</th>
                                <th>原始价格</th>
                                <th>惠后价格</th>
                                <th>数量</th>
                                <th>商家</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                        <#list shoppingCart.items() as cartLine>
                            <#assign originalPrice = Static["org.ofbiz.order.order.OrderReadHelper"].getProductPrice(delegator,cartLine.getProductId()) />
                            <#assign cartLineIndex = shoppingCart.getItemIndex(cartLine)>
                            <#assign bussinessPartyId = (delegator.findByPrimaryKey("Product",{"productId":cartLine.getProductId()}).businessPartyId)!''>
                            <#assign bussinessName = (delegator.findByPrimaryKey("PartyBusiness",{"partyId":bussinessPartyId}).businessName)!''>
                        <tr>
                            <td>货品图片</td>
                            <td>${cartLine.getProductId()}</td>
                            <td>${cartLine.getName()?if_exists}</td>
                            <td>${originalPrice}</td>
                            <td>${cartLine.getBasePrice()}</td>
                            <td>
                                <input type="text" name = "${cartLine.getProductId()}" class="product" value = "${cartLine.getQuantity()?string.number}" onkeyup="value=this.value.replace(/\D+/g,'')" size="5" />
                            </td>
                            <td>${bussinessName}</td>
                            <td>
                                <button id="addProducts" type="button" class="btn btn-primary" onclick="removeItem(${cartLineIndex})">移除</button>
                            </td>
                        </tr>
                        </#list>
                        </tbody>
                     </table>
                </div>
            </div>
        </div>


        <!-- 表格区域start -->

        </div><!-- /.box-body -->
    </form>
    <div class="row">
        <div class="form-group col-sm-6">
            <label  class="col-sm-2 control-label"></label>
            <div class="col-sm-10">
                <div class="dp-tables_btn">
                    <button id="next" class="btn btn-primary">
                        <i class="fa">下一步</i>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>



<div class="modal fade" id="myModal" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog" style="width: 900px">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close"
                        data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="myModalLabel">
                    选择商品
                </h4>
            </div>
            <div class="modal-body">
                <form class="xl_produce">
                    <input type="hidden" value="" name="VIEW_INDEX" id = "VIEW_INDEX"/>
                    <p>
                        <span>商品名称:　</span><input type="text" name="productName" id = "productName">
                        　<span>商品编码:　</span><input type="text" name="productId" id = "productId">
                        <button type="button" class="btn btn-primary xl_search">搜索</button>
                    </p>
                    <p>
                        <span>商品价格:　</span>
                            <input type="text" name="productPriceStart" id = "productPriceStart" onkeyup="value=this.value.replace(/\D+/g,'')">　
                        <span>-</span>
                        　<input type="text" name="productPriceEnd" id = "productPriceEnd" onkeyup="value=this.value.replace(/\D+/g,'')">
                    </p>
                </form>
                <table class="table table-bordered table-hover js-checkparent xl_table" >
                    <thead>
                    <tr>
                        <th><input class="js-allcheck" type="checkbox"></th>
                       <#-- <th>商品图片</th>
                        <th>商品规格</th>-->
                        <th>商品编码</th>
                        <th>商品名称</th>
                        <th>商家</th>
                        <th>价格</th>
                    </tr>
                    </thead>
                    <tbody>

                    </tbody>
                </table>
                <ul class="pagination xl_pages">

                </ul>
            </div>


            <div class="modal-footer xl_footer">
                <button type="button" class="btn btn-default"
                        data-dismiss="modal">取消
                </button>
                <button type="button" class="btn btn-primary xl_confirm">
                    确认
                </button>
            </div>
        </div><!-- /.modal-content -->
    </div><!-- /.modal -->
</div>
<!--modal结束-->
<!--另一个弹出框-->
<div class="modal fade" id="myActive" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog" style="width: 750px;">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close"
                        data-dismiss="modal" aria-hidden="true">
                    &times;
                </button>
                <h4 class="modal-title" id="myActiveLabel">
                    商品活动
                </h4>
            </div>
            <div class="modal-body xl_promotion">
                <table class="table table-bordered table-hover js-checkparent xl_tableActive" id = "myActiveTable">
                    <thead>
                    <tr>
                        <th></th>
                        <th>商品编码</th>
                        <th>商品名称</th>
                        <th>可用活动</th>
                        <th>活动后价格</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td><input type="radio" name="p_active1" gd-id="112223343444"></td>
                        <td>112223343444</td>
                        <td>苹果(4G)苹果(4G)苹果(4G)苹果(4G)苹果(4G)苹果(4G)</td>
                        <td>促销</td>
                        <td>1499</td>
                    </tr>
                    <tr>
                        <td><input type="radio" name="p_active1" gd-id="112223343444"></td>
                        <td>112223343444</td>
                        <td>苹果(4G)苹果(4G)苹果(4G)苹果(4G)苹果(4G)苹果(4G)</td>
                        <td>促销</td>
                        <td>1499</td>
                    </tr>
                    </tbody>
                </table>



            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default"
                        data-dismiss="modal">取消
                </button>
                <button type="button" class="btn btn-primary xl_btn_promote">
                    确定
                </button>
            </div>
        </div>
    </div>
</div>


<script>
    $(function(){
        $('#party_search').click(function(e){
            $.dataSelectModal({
                url: "/membermgr/control/personListModalPage?externalLoginKey=${externalLoginKey}",
                width:	"800",
                title:	"选择会员",
                selectId: "partyId"
            });
        });

//        点击弹出框
        $("#addProducts").click(function(){
            $("#myModal").modal("show");
        });
//        点击搜索按钮向后台请求
        $(".xl_search").click(function(){
            var productName = $("#productName").val();
            var productId = $("#productId").val();
            var productPriceStart = $("#productPriceStart").val();
            var productPriceEnd = $("#productPriceEnd").val();
            var params = {productName : productName, productId : productId,
                           productPriceStart:productPriceStart, productPriceEnd : productPriceEnd};
            $.ajax({
                url:'<@ofbizUrl>findOrderProducts</@ofbizUrl>',
                dataType:'json',
                type:'post',
                data:params,
                beforeSend:function(){
                    $(".xl_search").prop("disabled",false)
                    },
                    success:function(data){
                        $("#productName").val(data.productName);
                        $("#productId").val(data.productId);
                        $("#productPriceStart").val(data.productPriceStart);
                        $("#productPriceEnd").val(data.productPriceEnd);
                        $("#VIEW_INDEX").val(data.VIEW_INDEX);
                        $(".xl_table tbody").html("");
                        $(".xl_search").prop("disabled",false);
                        //请求成功之后遍历里面的数据
                        var b = '';
                        var list = data.productPriceViewList;
                        for(var i=0; i < list.length; i++){
                            var pp = list[i];
                            b+='<tr>'
                            +'<td><input class="js-checkchild" type="checkbox" value="'+ pp.productId +'"price="'+ pp.price+'"></td>'
                           /* +'<td></td>'
                            +'<td></td>'*/
                            +'<td>'+ pp.productId +'</td>'
                            +'<td>'+ pp.productName +'</td>'
                            +'<td>'+ pp.bussinessName +'</td>'
                            +'<td>'+ pp.price +'</td>'
                            +'</tr>';
                        }
                    $(".xl_table tbody").append(b);
                    // 请求成功之后，遍历分页
                    var a = '';
                    if (data.VIEW_INDEX > 0){
                       a = '<li><a href="javascript:;"  index=' + (data.VIEW_INDEX - 1) + '>上一页</a></li>';
                    }
                    for(var i=1;i <= data.pageCount; i++){
                        a += '<li ><a href="javascript:;" index='+ (i-1) +'>' + i + '</a></li>' ;
                    }

                    if (data.VIEW_INDEX + 1 < data.pageCount){
                        a += '<li><a href="javascript:;" index=' + (data.VIEW_INDEX + 1) + '>下一页</a></li>';
                    }

                    $(".xl_pages").html(a);

                    if (data.VIEW_INDEX == 0){
                        $(".xl_pages a").eq(data.VIEW_INDEX).addClass("active");
                    }else{
                        $(".xl_pages a").eq(data.VIEW_INDEX + 1 ).addClass("active");
                    }
                    $(".xl_search").prop("disabled",false);

                },
                error:function(){
                    $(".xl_search").prop("disabled",false);
                    alert("操作失败！")}

            });
            return false;
        });
//        上一页 下一页
        $(".xl_pages").on("click","a",function(){
            var productName = $("#productName").val();
            var productId = $("#productId").val();
            var productPriceStart = $("#productPriceStart").val();
            var productPriceEnd = $("#productPriceEnd").val();
            var VIEW_INDEX = $(this).attr("index");
            var params = {productName : productName, productId : productId,
                productPriceStart:productPriceStart, productPriceEnd : productPriceEnd, VIEW_INDEX : VIEW_INDEX};
            $.ajax({
                url:'<@ofbizUrl>findOrderProducts</@ofbizUrl>',
                dataType:'json',
                type:'post',
                data:params,
                beforeSend:function(){
                    $(".xl_pages>li>a").off("click");
                },
                success:function(data){
                    $(".xl_pages>li>a").on("click");
                    $(".xl_table tbody").html("");
                    //请求成功之后遍历里面的数据
                    var b = '';
                    var list = data.productPriceViewList;
                    for(var i=0; i < list.length; i++){
                        var pp = list[i];
                        b+='<tr>'
                        +'<td><input class="js-checkchild" type="checkbox" value="'+ pp.productId+'"price="'+ pp.price+'"></td>'
                        /*+'<td></td>'
                        +'<td></td>'*/
                        +'<td>'+ pp.productId +'</td>'
                        +'<td>'+ pp.productName +'</td>'
                        +'<td>'+ pp.bussinessName +'</td>'
                        +'<td>'+ pp.price +'</td>'
                        +'</tr>';
                    }
                    $(".xl_table tbody").append(b);
                    // 请求成功之后，遍历分页
                    var a = '';
                    if (data.VIEW_INDEX > 0){
                        a = '<li><a href="javascript:;" aria-disabled="true" index=' + (data.VIEW_INDEX - 1) + '>上一页</a></li>';
                    }
                    for(var i=1;i <= data.pageCount; i++){
                        a += '<li ><a href="javascript:;" index='+ (i-1) +'>' + i + '</a></li>' ;
                    }

                    if (data.VIEW_INDEX + 1 < data.pageCount){
                        a += '<li><a href="javascript:;" index=' + (data.VIEW_INDEX + 1) + '>下一页</a></li>';
                    }

                    $(".xl_pages").html(a);

                    if (data.VIEW_INDEX == 0){
                        $(".xl_pages a").eq(data.VIEW_INDEX).addClass("active");
                    }else{
                        $(".xl_pages a").eq(data.VIEW_INDEX + 1 ).addClass("active");
                    }
                },
                error:function(){
                    $(".xl_pages>li>a").on("click")
                    alert("操作失败！")
                }
            });

        });
        //第一个弹框点击确认向后台发送请求
        var arry_id=[];
        var arry_price=[];
        $(".xl_confirm").click(function(){
            var productIds = "";
            var checks = $('.js-checkparent .js-checkchild:checked');
            //判断是否选中记录
            if (checks.size() > 0) {
                //编辑id字符串
                checks.each(function () {
                    productIds += $(this).val() + ",";
                });
                productIds = productIds.substring(0,productIds.length  -1);
                $.ajax({
                    url:'<@ofbizUrl>isHaveActive</@ofbizUrl>',
                    dataType:'json',
                    type:'post',
                    data:{
                        productIds:productIds
                    },
                    beforeSend:function(){
                        $(".xl_confirm").prop("disabled",true);
                    },
                    success:function(data){
                        // 判断有活动，跳转到下一个弹出框
                        if(data.success==1){
                            $(".xl_confirm").prop("disabled",false);
                            $("#myModal").modal("hide");
                            $("#myActive").modal("show");
                            var trs = '';
                            for(var i=0;i<data.table.length;i++){
                                var activity = data.table[i];
                                trs +=  ' <tr>'
                                +'<td><input type="radio" name="activity" activityId="' + activity.activityId + '" productId="' + activity.productId + '" productPrice="'+ activity.productPrice +'" ></td>'
                                +'<td>' + activity.productId + '</td>'
                                +'<td>' + activity.productName + '</td>'
                                +'<td>' + activity.activityName + '</td>'
                                +'<td>' + activity.productPrice + '</td>'
                                +'</tr>';
                            }
                            $("#myActiveTable>tbody").html(trs);
                        }else  if(data.success == 0){
                            window.location.href = "<@ofbizUrl>addSomeOrderProducts?productIds=" + productIds +
                            "&currenPartyId=" + $("#partyId").val() +
                            "</@ofbizUrl>";
                        }
                    },
                    error:function(){
                        $(".xl_confirm").prop("disabled",false);
                        alert("操作失败！")
                    }
                });
            }else{
                alert("请至少勾选一条记录");
            }
            return false;
        });
        //点击第二个弹框的确认按钮
        $(".xl_btn_promote").click(function(){
            var productIds = "";
            var activityIds = "";
            var productPrices = "";
            var checks = $('input[name="activity"]:checked');
            if (checks.size() > 0) {
                //编辑id字符串
                checks.each(function () {
                    productIds += $(this).attr("productId") + ",";
                    activityIds += $(this).attr("activityId") + ",";
                    productPrices += $(this).attr("productPrice") + ",";
                });
                productIds = productIds.substring(0,productIds.length -1);
                activityIds = activityIds.substring(0,activityIds.length -1);
                productPrices = productPrices.substring(0,productPrices.length -1);
                window.location.href = "<@ofbizUrl>addSomeOrderProducts?productIds=" + productIds +
                                                   "&activityIds=" + activityIds + "&productPrices=" + productPrices +
                                                   "&currenPartyId=" + $("#partyId").val() + "</@ofbizUrl>";
            }else{
               alert("请至少勾选一条记录");
            }
            return false;
        });
    });
    $('#next').click(function(){
        $('#form').dpValidate({
            clear: true
        });
        var flag = true;
        var productId = "";
        var num = "";
        $(".product").each(function(){
            productId = $(this).attr("name");
            num = $(this).val();
        });
        if (productId != ""){
            if (num === '0'){
                alert("数量不能为0");
            }else{
                $.post("<@ofbizUrl>checkOrderProductNum</@ofbizUrl>",{productId : productId,num : num},function(data){
                    var resultData = data.resultData;
                    if (resultData.status == false){
                        alert("可购数量不足");
                        return;
                    }else{
                        $('#form').submit();
                    }
                });
            }
        }else{
            $('#form').submit();
        }

    });

    $('#form').dpValidate({
        validate: true,
        callback: function(){
            document.getElementById('form').submit();
        }
    });

    function removeItem(index){
        window.location.href = "<@ofbizUrl>salesmodifycart?removeSelected=true"+ "&selectedItem=" + index +"</@ofbizUrl>";
    }
</script>
