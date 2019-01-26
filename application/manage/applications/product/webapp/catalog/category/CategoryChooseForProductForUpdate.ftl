<script src="<@ofbizContentUrl>/images/themes/coloradmin/plugins/select2/select2.min.js</@ofbizContentUrl>"></script>
<link href="<@ofbizContentUrl>/images/themes/coloradmin/plugins/select2/select2.css</@ofbizContentUrl>" rel="stylesheet"/>
<#include "component://common/webcommon/includes/htmlScreenTemplate.ftl"/>

<#assign cataResult = dispatcher.runSync("getInitProductCategoryByLevel", Static["org.ofbiz.base.util.UtilMisc"].toMap("productStoreId", requestAttributes.ownerPartyId))/>
<#assign thirdCate = delegator.findByPrimaryKey("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", product.primaryProductCategoryId))/>
<#assign secondCate = delegator.findByPrimaryKey("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", thirdCate.primaryParentCategoryId))/>
<#assign firstCate = delegator.findByPrimaryKey("ProductCategory",Static["org.ofbiz.base.util.UtilMisc"].toMap("productCategoryId", secondCate.primaryParentCategoryId))/>

<form id="productForm">
    <div class="tab-content">
        <div class="tab-pane fade active in" id="default-tab-1">
            <div class="${panelStyle}">
                <div class="${panelBodyStyle}">
                <#--${catalogs},${topCategories},${categories}-->
                    <div class="row">
                        <div class="col-xs-4 cate_set_column">
                            <div class="cate_set_item">
                                <div class="cate_set_cont">
                                    <div class="cate_search">
                                        <input type="hidden" id="parentId0" value="0">
                                    <#if cataResult.productCategoryLevel1Info??>
                                        <input type="hidden" id="parentId1" value="${cataResult.productCategoryLevel1Info.get('productCategoryId')}">
                                    <#else >
                                        <input type="hidden" id="parentId1" value="">
                                    </#if>
                                        <input type="text" id="search_name1" class="cate_search_box" placeholder="输入名称查找">
                                        <a href="javascript:;" onclick="findFirstGoodsCate()"><span class="glyphicon glyphicon-search"></span></a>
                                    </div>
                                    <div class="cate_set_list" id="cate_list1">
                                    <#if cataResult.productCategoryLevel1List??>
                                        <#list cataResult.productCategoryLevel1List as category1>
                                            <div class="cate_item <#if category1.get('productCategoryId') == firstCate.get('productCategoryId')>active</#if>" id="cate_item${category1.get('productCategoryId')}"
                                                 onclick="loadSecondCate(this,${category1.get('productCategoryId')})">
                                                <h4>${category1.get("categoryName")}</h4>
                                            </div>
                                        </#list>
                                    </#if>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-xs-4 cate_set_column">
                            <div class="cate_set_item">
                                <div class="cate_set_cont">
                                    <div class="cate_search">
                                    <#if cataResult.productCategoryLevel2Info??>
                                        <input type="hidden" id="parentId2" value="${cataResult.productCategoryLevel2Info.get('productCategoryId')?if_exists}">
                                    <#else>
                                        <input type="hidden" id="parentId2" value="">
                                    </#if>

                                        <input type="text" id="search_name2" class="cate_search_box" placeholder="输入名称查找">
                                        <a href="javascript:;" onclick="findSecondGoodsCate()"><span class="glyphicon glyphicon-search"></span></a>
                                    </div>
                                    <div class="cate_set_list" id="cate_list2">
                                    <#if cataResult.productCategoryLevel2List??>
                                        <#list cataResult.productCategoryLevel2List as secondCategory>
                                            <div class="cate_item cate <#if secondCategory.get('productCategoryId') == secondCate.get('productCategoryId')>active</#if>" id="cate_item${secondCategory.get('productCategoryId')}"
                                                 onclick="loadThirdCate(this,${secondCategory.get('productCategoryId')})"><h4>${secondCategory.get('categoryName')}</h4>
                                            </div>
                                        </#list>
                                    </#if>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-xs-4 cate_set_column">
                            <div class="cate_set_item">
                                <div class="cate_set_cont">
                                    <div class="cate_search">
                                    <#if cataResult.productCategoryLevel3Info??>
                                    <input type="hidden" id="parentId3" value="${cataResult.productCategoryLevel3Info.get('productCategoryId')}">

                                    <#else>
                                         <input type="hidden" id="parentId3" value="">
                                    </#if>
                                        <input type="text" id="search_name3" class="cate_search_box" placeholder="输入名称查找">
                                        <a href="javascript:;" onclick="findThirdGoodsCate()"><span class="glyphicon glyphicon-search"></span></a>
                                    </div>
                                    <div class="cate_set_list" id="cate_list3">
                                    <#if cataResult.productCategoryLevel3List??>
                                        <#list cataResult.productCategoryLevel3List as thirdCategory>
                                            <div class="cate_item cate3 <#if thirdCategory.get('productCategoryId') == thirdCate.get('productCategoryId')>active</#if>" id="cate_item${thirdCategory.get('productCategoryId')}"
                                                 onclick="clickItem(this)">
                                                <h4>${thirdCategory.get('categoryName')}</h4>
                                            </div>
                                        </#list>
                                    </#if>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>


                </div>
                <div class="text-center">
                    <button type="button" class="btn btn-lg btn-primary" onclick="panelNext('second');">确认选择类目并进入下一步</button>
                    <button type="button" class="btn btn-lg btn-default" onclick="window.location.href='findProductGoods?externalLoginKey=${externalLoginKey?default("")}'">返回列表</button>
                </div>
            </div>
        </div>
        <script type="text/javascript">

            /**
             * 点击按钮时，改变按钮状态。
             * @param obj 按钮对象
             */
            function clickItem(obj) {
                if (obj == null) return;
                $(obj).parent().find("div.cate_item").each(function () {
                    $(this).removeClass("active");
                });
                $(obj).addClass("active");
            }

            /**
             * 加载一级分类
             * @param parentId 父类id
             */
            function findFirstGoodsCate() {
                var cateName = $("#search_name1").val();
                var url = 'searchCatalog.htm';
                $("#cate_list1").html('');
                $.ajax({
                    url: url,
                    data: 'productStoreId=${requestAttributes.productStoreId}&catalogName=' + cateName,
                    method: 'post',
                    success: function (data) {
                        data = data.catalogs;
                        var html = '';
                        for (var i = 0; i < data.length; i++) {
                            var cate = data[i];
                            html += '<div class="cate_item" id="cate_item' + cate.prodCatalogId + '" onclick="loadSecondCate(this,' + cate.prodCatalogId + ')">' +
                                    '<h4>' + cate.catalogName + '</h4>' +
                                    '</div>';
                        }

                        $("#cate_list1").html(html);
                        if ($("#cate_list1").find("div.cate_item").length > 0) {
                            $("#cate_list1").find("div.cate_item")[0].click();
                        }
                    }
                });
            }

            /**
             * 加载二级分类
             * @param obj 按钮对象
             * @param parentId 父类id
             */
            function loadSecondCate(obj, parentId, cateName) {
                clickItem(obj);
                $("#parentId1").val(parentId);
                $("#cate_list2").html('');
                $("#cate_list3").html('');
                var url = 'searchCategory.htm';
                if (cateName == undefined) {
                    cateName = '';
                }
                $.ajax({
                    url: url,
                    data: 'categoryId=' + parentId + '&categoryName=' + cateName,
                    method: 'post',
                    success: function (data) {
                        data = data.categories;
                        var html = '';
                        if (data) {
                            for (var i = 0; i < data.length; i++) {
                                var cate = data[i];
                                html += '<div class="cate_item" id="cate_item' + cate.productCategoryId + '" onclick="loadThirdCate(this,' + cate.productCategoryId + ')">' +
                                        '<h4>' + cate.categoryName + '</h4>' +
                                        '</div>';
                            }
                        }
                        $("#cate_list2").html(html);
                        if ($("#cate_list2").find("div.cate_item").length > 0) {
                            $("#cate_list2").find("div.cate_item")[0].click();
                        } else {

                        }

                    }
                });
            }

            /**
             * 加载三级分类
             * @param obj 按钮对象
             * @param parentId 父类id
             */
            function loadThirdCate(obj, parentId, cateName) {
                clickItem(obj);
                $("#parentId2").val(parentId);
                $("#cate_list3").html('');
                var url = 'searchCategory.htm';
                if (cateName == undefined) {
                    cateName = '';
                }
                $.ajax({
                    url: url,
                    method: 'post',
                    data: 'categoryId=' + parentId + '&categoryName=' + cateName,
                    success: function (data) {
                        data = data.categories;
                        var html = '';
                        for (var i = 0; i < data.length; i++) {
                            var cate = data[i];
                            html += '<div class="cate_item cate3" id="cate_item' + cate.productCategoryId + '" onclick="clickItem(this)">' +
                                    '<h4>' + cate.categoryName + ' </h4>' +

                                    '</div>';
                        }
                        $("#cate_list3").html(html);
                        if ($("#cate_list3").find("div.cate_item").length > 0) {
                            $("#cate_list3").find("div.cate_item")[0].click();
                        } else {

                        }
                    }
                });
            }

            /**
             * 查找二级分类
             */
            function findSecondGoodsCate() {
                var cateName = $("#search_name2").val();
                var parentId = $("#parentId1").val();
                loadSecondCate(null, parentId, cateName);
            }

            function panelNext(setup) {
                console.log($('#productBrandId').val())

                if (setup === 'first') {
                    $('.nav-tabs a[href="#default-tab-1"]').tab('show');
                }
                if (setup === 'second') {
                    if ($('.cate3.active').length > 0) {
                        var categoryId = $('.cate3.active').attr('id');
                        if (categoryId) {
                            categoryId = (categoryId.substring('cate_item'.length));
                            $('input[name="productCategoryId"]').val(categoryId);
                            $('.nav-tabs a[href="#default-tab-2"]').tab('show');
                        }
                    } else {
                        //设置提示弹出框内容
                        tipLayer({msg: "请选择商品对应的分类设置（在第三级分类下选择）！"});
                        return;
                    }
                } else if (setup === 'third') {
                    $('.nav-tabs a[href="#default-tab-3"]').tab('show');
                }
            }


            $('a[href="#default-tab-2"]').on('show.bs.tab', function (e) {
                if ($('.cate3.active').length > 0) {
                    var categoryId = $('.cate3.active').attr('id');
                    if (categoryId) {
                        categoryId = (categoryId.substring('cate_item'.length));
                        $('input[name="productCategoryId"]').val(categoryId);
                    }
                } else {
                    e.preventDefault();
                    //设置提示弹出框内容
                    tipLayer({msg: "请选择商品对应的分类设置（在第三级分类下选择）！"});
                }

            });
            $('a[href="#default-tab-3"]').on('show.bs.tab', function (e) {
                if ($('.cate3.active').length > 0) {
                    var categoryId = $('.cate3.active').attr('id');
                    if (categoryId) {
                        categoryId = (categoryId.substring('cate_item'.length));
                        $('input[name="productCategoryId"]').val(categoryId);
                    }
                } else {
                    e.preventDefault();
                    //设置提示弹出框内容
                    tipLayer({msg: "请选择商品对应的分类设置（在第三级分类下选择）！"});
                }

            });

        </script>
        <style>

            .nav-tabs > li {
                float: left;
                margin-bottom: -1px;
                text-align: center;
                width: calc(100% / 3 - 5px);
            }

            .cate_set_list {
                height: 450px;

            }

            .panel .panel-default {
                background: #eee;
            }

            .cate_set_item {
                background: white;
            }

            #productForm .box {
                border-top: 0px solid #d2d6de;
            }

        </style>