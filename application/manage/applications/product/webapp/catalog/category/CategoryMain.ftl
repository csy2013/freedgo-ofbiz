<script src="<@ofbizContentUrl>/images/themes/coloradmin/plugins/select2/select2.min.js</@ofbizContentUrl>"></script>
<link href="<@ofbizContentUrl>/images/themes/coloradmin/plugins/select2/select2.css</@ofbizContentUrl>" rel="stylesheet"/>
<#--<service service-name="findCatalog">
    <field-map field-name="productStoreId" from-field="parameters.productStoreId"/>
</service>
<set field="catalogId" from-field="catalogs[0].prodCatalogId"/>
<service service-name="findTopCategories" result-map="topCategories">
    <field-map field-name="catalogId" from-field="catalogId"/>
</service>
<service service-name="findCategoryMembers" >
    <field-map field-name="categoryId" from-field="topCategories.categories[0].productCategoryId"/>
</service>-->
<#assign cataResult = dispatcher.runSync("findCatalog", Static["org.ofbiz.base.util.UtilMisc"].toMap("productStoreId", requestAttributes.productStoreId,"userLogin",requestAttributes.userLogin))/>

<#if cataResult.catalogs?exists>
    <#assign catalogs = cataResult.catalogs >
    <#if catalogs[0]?exists>
        <#assign topCategoriesRet = dispatcher.runSync("findTopCategories", Static["org.ofbiz.base.util.UtilMisc"].toMap("catalogId", catalogs[0].prodCatalogId,"userLogin",requestAttributes.userLogin))/>
        <#if topCategoriesRet.categories?exists>
            <#assign topCategories = topCategoriesRet.categories>
            <#assign  categoryId = topCategories[0].productCategoryId>
            <#assign categoriesRet = dispatcher.runSync("findCategoryMembers", Static["org.ofbiz.base.util.UtilMisc"].toMap("categoryId", categoryId,"userLogin",requestAttributes.userLogin))/>
            <#assign categories = categoriesRet.categories>
        </#if>
    </#if>
</#if>
<table class="table gtreetable" id="gtreetable">
    <thead>
    <tr>
        <th>浏览分类</th>
    </tr>
    </thead>
</table>
<#--${catalogs},${topCategories},${categories}-->
<div class="row">

    <div class="col-xs-4 cate_set_column">
        <div class="cate_set_item">
            <div class="cate_set_cont">
                <div class="cate_search">
                    <input type="hidden" id="parentId0" value="0">
                <#if catalogs??>
                    <input type="hidden" id="parentId1" value="${catalogs.get(0).get('prodCatalogId')}">
                <#else >
                    <input type="hidden" id="parentId1" value="">
                </#if>
                    <input type="text" id="search_name1" class="cate_search_box" placeholder="输入名称查找">
                    <a href="javascript:;" onclick="findFirstGoodsCate()"><span class="glyphicon glyphicon-search"></span></a>
                </div>
                <div class="cate_set_list" id="cate_list1"><a class="add_cart_btn" href="javascript:;" onclick="showAddCatalog()"><i class="glyphicon glyphicon-plus"></i>
                    添加一级分类</a>
                <#if catalogs??>
                    <#list catalogs as catalog>
                        <div class="cate_item <#if catalog_index ==0>active</#if>" id="cate_item${catalog.get('prodCatalogId')}"
                             onclick="loadSecondCate(this,${catalog.get('prodCatalogId')})">
                            <h4>${catalog.get("catalogName")}</h4>
                            <div class="c_btns">
                                <a href="javascript:;"><span class="glyphicon glyphicon-edit" onclick="showEditCatalog(1,${catalog.get('prodCatalogId')})"></span></a>
                                <a href="javascript:;" onclick="deleteCatalog(${catalog.get('prodCatalogId')})"><span class="glyphicon glyphicon-trash"></span></a>
                            </div>
                        </div>
                    </#list></#if>
                </div>
            </div>
        </div>
    </div>
    <div class="col-xs-4 cate_set_column">
        <div class="cate_set_item">
            <div class="cate_set_cont">
                <div class="cate_search">
                <#if topCategories??>
                    <input type="hidden" id="parentId2" value="${topCategories.get(0).get('productCategoryId')?if_exists}">
                <#else>
                    <input type="hidden" id="parentId2" value="">
                </#if>

                    <input type="text" id="search_name2" class="cate_search_box" placeholder="输入名称查找">
                    <a href="javascript:;" onclick="findSecondGoodsCate()"><span class="glyphicon glyphicon-search"></span></a>
                </div>
                <div class="cate_set_list" id="cate_list2"><a class="add_cart_btn" href="javascript:;" onclick="showAddCate(0)"><i class="glyphicon glyphicon-plus"></i> 添加二级分类</a>
                <#if topCategories??>
                    <#list topCategories  as secondCategory>
                        <div class="cate_item <#if secondCategory_index == 0>active</#if>" id="cate_item${secondCategory.get('productCategoryId')}"
                             onclick="loadThirdCate(this,${secondCategory.get('productCategoryId')})"><h4>${secondCategory.get('categoryName')}</h4>
                            <div class="c_btns"><a href="javascript:;"><span class="glyphicon glyphicon-edit"
                                                                             onclick="showEditCate(2,${secondCategory.get('productCategoryId')})"></span></a>
                                <a href="javascript:;" onclick="deleteCategory(${secondCategory.get('productCategoryId')})"><span class="glyphicon glyphicon-trash"></span></a>
                            </div>
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
                <#if categories??>
                    <input type="hidden" id="parentId3" value="">
                <#else>
                    <input type="hidden" id="parentId3" value="${categories.get(0).get('productCategoryId')}">
                </#if>
                    <input type="text" id="search_name3" class="cate_search_box" placeholder="输入名称查找">
                    <a href="javascript:;" onclick="findThirdGoodsCate()"><span class="glyphicon glyphicon-search"></span></a>
                </div>
                <div class="cate_set_list" id="cate_list3"><a class="add_cart_btn" href="javascript:;" onclick="showAddThreeCate(1)"><i class="glyphicon glyphicon-plus"></i> 添加三级分类</a>
                <#if categories??><#list categories as thirdCategory>
                    <div class="cate_item <#if thirdCategory_index == 0>active</#if>" id="cate_item${thirdCategory.get('productCategoryId')}" onclick="clickItem(this   )">
                        <h4>${thirdCategory.get('categoryName')}</h4>
                        <div class="c_btns"><a href="javascript:;"><span class="glyphicon glyphicon-edit"
                                                                         onclick="showEditThreeCate(1,thirdCategory.get('productCategoryId'))"></span></a>
                            <a href="javascript:;" onclick="deleteThreeCategory(thirdCategory.get('productCategoryId'))"><span class="glyphicon glyphicon-trash"></span></a></div>
                    </div>
                </#list></#if>
                </div>
            </div>
        </div>
    </div>


    <div class="modal fade" id="catalogAdd" role="dialog" aria-hidden="true" style="display: none;">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title">添加分类</h4>
                </div>
                <div class="modal-body">
                    <div class="modal_form">
                        <form role="form" class="form-horizontal" action="addCatalog.htm" method="post" id="saveCatalogForm">
                            <input type="hidden" id="cur_level" value="1">
                            <input type="hidden" name="productStoreId" value="${requestAttributes.productStoreId}"/>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类名称：
                                </label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control required specstr" maxlength="16" name="catalogName" onblur="checkCatNameExist(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类排序：
                                </label>
                                <div class="col-sm-6 catSort">
                                    <input type="text" class="form-control w100 required integer" name="sequenceNum" id="add_catalog_sort" onblur="checkNumber(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" onclick="submitSaveCatalogForm()">确定</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="catalogEdit" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title">编辑分类</h4>
                </div>
                <div class="modal-body">
                    <div class="modal_form">
                        <form role="form" class="form-horizontal" id="updateCatalogForm">
                            <input type="hidden" name="prodCatalogId" id="update_catId">
                            <input type="hidden" id="oldCatalogName">
                            <input type="hidden" name="productStoreId" value="${requestAttributes.productStoreId}"/>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类名称：
                                </label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control required specstr" maxlength="16" name="catalogName" id="update_cate_name"
                                           onblur="checkUpdateCatalogNameExist(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类排序：
                                </label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control w100 required number" name="sequenceNum" id="update_catalog_sort" onblur="checkNumber(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <!-- 下面这个商品类型最后一级分类才需要填写  -->
                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" onclick="submitUpdateCatalogForm()">确定</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="cateAdd" role="dialog" aria-hidden="true" style="display: none;">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title">添加分类</h4>
                </div>
                <div class="modal-body">
                    <div class="modal_form">
                        <form role="form" class="form-horizontal" action="addCategory.htm" method="post" id="saveCategoryForm">
                            <input type="hidden" id="cur_level" value="0">
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类名称：
                                </label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control required specstr" maxlength="16" name="categoryName" id="add_category_name"
                                           onblur="checkCatNameExist(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>上级分类：
                                </label>
                                <div class="col-sm-6">
                                    <select class="w100 select2-hidden-accessible" style="width: 100%" data-live-search="true" id="add_parent_category_id" name="prodCatalogId"  >
                                    </select>
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类排序：
                                </label>
                                <div class="col-sm-6 catSort">
                                    <input type="text" class="form-control w100 required integer" name="sequenceNum" id="add_category_sort" onblur="checkNumber(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>

                            <!-- 下面这个商品类型最后一级分类才需要填写  -->


                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" onclick="submitSaveCategoryForm()">确定</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="cateEdit" role="dialog" aria-hidden="true" style="display: none;">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title">修改分类</h4>
                </div>
                <div class="modal-body">
                    <div class="modal_form">
                        <form role="form" class="form-horizontal" action="editCategory.htm" method="post" id="updateCategoryForm">
                            <input type="hidden" id="cur_level" value="0">
                            <input type="hidden" id="update_category_id" name="productCategoryId" value=""/>
                            <input type="hidden" id="oldCategoryName">
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类名称：
                                </label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control required specstr" maxlength="16" name="categoryName" id="update_category_name"
                                           onblur="checkUpdateCatNameExist(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>上级分类：
                                </label>
                                <div class="col-sm-6">
                                    <select class="w100 select2-hidden-accessible" style="width: 100%" data-live-search="true" id="update_parent_category_id" name="prodCatalogId" tabindex="-1"
                                            aria-hidden="true">
                                    </select>
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类排序：
                                </label>
                                <div class="col-sm-6 catSort">
                                    <input type="text" class="form-control w100 required integer" name="sequenceNum" id="update_category_sort" onblur="checkNumber(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>

                            <!-- 下面这个商品类型最后一级分类才需要填写  -->
                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" onclick="submitUpdateCategoryForm()">确定</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="threeCateAdd" role="dialog" aria-hidden="true" style="display: none;">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title">添加三级分类</h4>
                </div>
                <div class="modal-body">
                    <div class="modal_form">
                        <form role="form" class="form-horizontal" action="addThreeCategory.htm" method="post" id="saveThreeCategoryForm">
                            <input type="hidden" id="cur_level" value="0">
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类名称：
                                </label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control required specstr" maxlength="16" name="categoryName" id="add_three_category_name"
                                           onblur="checkCatNameExist(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>上级分类：
                                </label>
                                <div class="col-sm-6">
                                    <select class="w100 select2-hidden-accessible" style="width: 100%" data-live-search="true" id="add_three_parent_category_id" name="parentProductCategoryId"
                                            tabindex="-1"
                                            aria-hidden="true">
                                    </select>
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类排序：
                                </label>
                                <div class="col-sm-6 catSort">
                                    <input type="text" class="form-control w100 required integer" name="sequenceNum" id="add_three_category_sort" onblur="checkNumber(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>

                            <!-- 下面这个商品类型最后一级分类才需要填写  -->


                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" onclick="submitSaveThreeCategoryForm()">确定</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
    <div class="modal fade" id="threeCateEdit" role="dialog" aria-hidden="true" style="display: none;">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>
                    <h4 class="modal-title">修改三级分类</h4>
                </div>
                <div class="modal-body">
                    <div class="modal_form">
                        <form role="form" class="form-horizontal" action="editThreeCategory.htm" method="post" id="updateThreeCategoryForm">
                            <input type="hidden" id="cur_level" value="0">
                            <input type="hidden" id="update_three_category_id" name="productCategoryId" value=""/>
                            <input type="hidden" id="oldThreeCategoryName">
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类名称：
                                </label>
                                <div class="col-sm-6">
                                    <input type="text" class="form-control required specstr" maxlength="16" name="categoryName" id="update_three_category_name"
                                           onblur="checkUpdateThreeCatNameExist(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>上级分类：
                                </label>
                                <div class="col-sm-6">
                                    <select class="w100 select2-hidden-accessible" style="width: 100%" data-live-search="true" id="update_three_parent_category_id" name="parentProductCategoryId"
                                            tabindex="-1"
                                            aria-hidden="true">
                                    </select>
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="col-sm-4 control-label">
                                    <span class="text-danger">*</span>分类排序：
                                </label>
                                <div class="col-sm-6 catSort">
                                    <input type="text" class="form-control w100 required integer" name="sequenceNum" id="update_three_category_sort" onblur="checkNumber(this)">
                                </div>
                                <div class="col-sm-2">
                                </div>
                            </div>

                            <!-- 下面这个商品类型最后一级分类才需要填写  -->
                        </form>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" onclick="submitUpdateThreeCategoryForm()">确定</button>
                    <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
    <script type="text/javascript">
        // 缓存的编辑框的值
        var mythml = $("#catalogEdit").html();
        var myhtml1 = $("#cateEdit").html();
        var myhtml2 = $("#threeCateEdit").html();

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
                    html += '<a class="add_cart_btn" href="javascript:;" onclick="showAddCatalog()"><i class="glyphicon glyphicon-plus"></i> 添加一级分类</a>';
                    for (var i = 0; i < data.length; i++) {
                        var cate = data[i];
                        html += '<div class="cate_item" id="cate_item' + cate.prodCatalogId + '" onclick="loadSecondCate(this,' + cate.prodCatalogId + ')">' +
                                '<h4>' + cate.catalogName + '</h4>' +
                                '<div class="c_btns">' +
                                '<a href="javascript:;"><span class="glyphicon glyphicon-edit" onclick="showEditCatalog(1,' + cate.prodCatalogId + ')"></span></a>' +
                                '<a href="javascript:;" onclick="deleteCatalog(' + cate.prodCatalogId + ')"><span class="glyphicon glyphicon-trash"></span></a>' +
                                '</div>' +
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
            var url = 'searchCategoryByCatalog.htm';
            if (cateName == undefined) {
                cateName = '';
            }
            $.ajax({
                url: url,
                data: 'catalogId=' + parentId + '&categoryName=' + cateName,
                method: 'post',
                success: function (data) {
                    data = data.categories;
                    var html = '';
                    html += '<a class="add_cart_btn" href="javascript:;" onclick="showAddCate(0)"><i class="glyphicon glyphicon-plus"></i> 添加二级分类</a>';
                    if (data) {
                        for (var i = 0; i < data.length; i++) {
                            var cate = data[i];
                            html += '<div class="cate_item" id="cate_item' + cate.productCategoryId + '" onclick="loadThirdCate(this,' + cate.productCategoryId + ')">' +
                                    '<h4>' + cate.categoryName + '</h4>' +
                                    '<div class="c_btns">' +
                                    '<a href="javascript:;"><span class="glyphicon glyphicon-edit" onclick="showEditCate(2,' + cate.productCategoryId + ')"></span></a>' +
                                    '<a href="javascript:;" onclick="deleteCategory(' + cate.productCategoryId + ')"><span class="glyphicon glyphicon-trash"></span></a>' +
                                    '</div>' +
                                    '</div>';
                        }
                    }
                    $("#cate_list2").html(html);
                    if ($("#cate_list2").find("div.cate_item").length > 0) {
                        $("#cate_list2").find("div.cate_item")[0].click();
                    } else {
                        $("#cate_list3").html('<a class="add_cart_btn" href="javascript:;" onclick="showAddCate(1)"><i class="glyphicon glyphicon-plus"></i> 添加三级分类</a>');
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
                    html += '<a class="add_cart_btn" href="javascript:;" onclick="showAddThreeCate(1)"><i class="glyphicon glyphicon-plus"></i> 添加三级分类</a>';
                    for (var i = 0; i < data.length; i++) {
                        var cate = data[i];
                        html += '<div class="cate_item" id="cate_item' + cate.productCategoryId + '"onclick="clickItem(this)">' +
                                '<h4>' + cate.categoryName + ' </h4>' +
                                '<div class="c_btns">' +
                                '<a href="javascript:;"><span class="glyphicon glyphicon-edit" onclick="showEditThreeCate(1,' + cate.productCategoryId + ')"></span></a>' +
                                '<a href="javascript:;" onclick="deleteThreeCategory(' + cate.productCategoryId + ')"><span class="glyphicon glyphicon-trash"></span></a>' +
                                '</div>' +
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

        function findThirdGoodsCate() {
            var cateName = $("#search_name3").val();
            var parentId = $("#parentId2").val();
            loadThirdCate(null, parentId, cateName);
        }

        /**
         * 查找二级分类
         */


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
         * 添加分类时，分类名称onblur，检查分类名是否重复
         * @param obj
         */
        function checkCatNameExist(obj) {
            $(obj).removeClass("error");
            $("#cateName_exist_tip").remove();
            var cateName = $(obj).val();
            if ($("#cur_level").val()) {
                $.post("checkCategoryNameExist.htm", "categoryName=" + cateName, function (data) {
                    if (data.isExist && data.isExist == 1) {
                        $(obj).addClass("error");
                        $(obj).after('<label generated="true" class="error value_tip" id="cateName_exist_tip">分类名称已存在</label>');
                    }
                });
            } else {
                $.post("checkCatalogNameExist.htm", "catalogName=" + cateName + "&productStoreId=${requestAttributes.productStoreId}", function (data) {
                    if (data.isExist && data.isExist == 1) {
                        $(obj).addClass("error");
                        $(obj).after('<label generated="true" class="error value_tip" id="cateName_exist_tip">分类名称已存在</label>');
                    }
                });
            }
        }

        /**
         * 弹框显示添加分类
         * @param level 商品是属于几级分类（一级，二级，三级）
         */
        var num = 0;

        function showAddCatalog(level) {
            num = 0;
            $("#saveCatalogForm")[0].reset();
            var parentId = $("#parentId" + level).val();
            $("#cur_level").val(level);

            $("#saveCatalogForm")[0].action = 'addCatalog.htm';

            $('#catalogAdd').modal("show");
            $('input[name=sequenceNum]').removeClass("error");
            $('input[name=sequenceNum]').next().hide();
            $('#add_catalog_sort]').removeClass("error");
            $('#add_catalog_sort]').next().hide();

        }

        //    add catalog category
        function showAddCate(level) {
            num = 0;
            $("#saveCategoryForm")[0].reset();
            var parentId = $("#parentId" + level).val();
            $("#cur_level").val(level);

            $.ajax({
                url: 'queryCatalogs.htm',
                method: 'post',
                data: 'productStoreId=${requestAttributes.productStoreId}',
                success: function (data) {
                    var html = '';
                    data = data.catalogs;
                    if (data) {

                        for (var i = 0; i < data.length; i++) {
                            var cate = data[i];
                            if (cate.prodCatalogId == parentId) {
                                html += '<option value="' + cate.prodCatalogId + '" selected>' + cate.catalogName + '</option>';
                            } else {
                                html += '<option value="' + cate.prodCatalogId + '">' + cate.catalogName + '</option>';
                            }
                        }

                        $('#add_parent_category_id').html(html);
                        $("#add_parent_category_id option[value=" + $("#parentId" + parseInt(level + 1)).val() + "]").attr("selected", true);
                        $('select[data-live-search="true"]').select2();
                    }
                }
            });

            $('#cateAdd').modal("show");
            $('#add_category_name').removeClass("error");
            $('#add_category_name').next().hide();
            $('#add_catalog_sort').removeClass("error");
            $('#add_catalog_sort').next().hide();

        }


        //    add three catalog category
        function showAddThreeCate(level) {
            num = 0;
            $("#saveThreeCategoryForm")[0].reset();
            var parentId = $("#parentId" + level).val();
            $("#cur_level").val(level);

            $.ajax({
                url: 'findTopCategories.htm',
                method: 'post',
                data: 'catalogId=' + parentId,
                success: function (data) {
                    var html = '';
                    data = data.categories;
                    if (data) {
                        for (var i = 0; i < data.length; i++) {
                            var cate = data[i];
                            if (cate.productCategoryId == parentId) {
                                html += '<option value="' + cate.productCategoryId + '" selected>' + cate.categoryName + '</option>';
                            } else {
                                html += '<option value="' + cate.productCategoryId + '">' + cate.categoryName + '</option>';
                            }
                        }
                        $('#add_three_parent_category_id').html(html);
                        $("#add_three_parent_category_id option[value=" + $("#parentId" + parseInt(level + 1)).val() + "]").attr("selected", true);
                        $('#add_three_parent_category_id').select2();
                    }
                }
            });

            $('#threeCateAdd').modal("show");
            $('#add_three_category_name').removeClass("error");
            $('#add_three_category_name').next().hide();
            $('#add_three_catalog_sort').removeClass("error");
            $('#add_three_catalog_sort').next().hide();

        }

        /**
         * 确定提交添加
         */
        function submitSaveCatalogForm() {
            //if($("#saveGoodsCateForm").find(".error").length>0) return;
            var hasNotError = true;
            $("#saveCatalogForm").find(".error").each(function () {
                if ($(this).css("display") != 'none') {
                    hasNotError = false;
                }
            });
            var r = /^\+?[1-9][0-9]*$/;
            if (!r.test($('#add_catalog_sort').val())) {
                $("#add_catalog_sort").parent("div").append("<label for='add_catalog_sort' generated='true' class='error'>请输入整数！</label>");
                $("#add_catalog_sort").addClass("error");
                return;
            }
            var level = $("#cur_level").val();
            var parentId = $("#parentId" + (parseInt(level) + 1)).val();

            if (num == 0) {
                $.ajax({
                    url: 'addCatalog.htm',
                    method: 'post',
                    data: $("#saveCatalogForm").serialize(),
                    success: function (data) {
                        location.reload();
                    }
                });
            }

            $('#catalogAdd').modal("hide");
        }

        function submitSaveCategoryForm() {
            //if($("#saveGoodsCateForm").find(".error").length>0) return;
            var hasNotError = true;
            $("#saveCategoryForm").find(".error").each(function () {
                if ($(this).css("display") != 'none') {
                    hasNotError = false;
                }
            });
            var r = /^\+?[1-9][0-9]*$/;
            if (!r.test($('#add_category_sort').val())) {
                $("#add_category_sort").parent("div").append("<label for='add_category_sort' generated='true' class='error'>请输入整数！</label>");
                $("#add_category_sort").addClass("error");
                return;
            }
            var level = $("#cur_level").val();
            var parentId = $("#parentId" + (parseInt(level) + 1)).val();

            if (num == 0) {
                $.ajax({
                    url: 'addCategory.htm',
                    method: 'post',
                    data: $("#saveCategoryForm").serialize(),
                    success: function (data) {
                        location.reload();
                    }
                });
            }

            $('#cateAdd').modal("hide");
        }

        function submitSaveThreeCategoryForm() {
            //if($("#saveGoodsCateForm").find(".error").length>0) return;
            var hasNotError = true;
            $("#saveThreeCategoryForm").find(".error").each(function () {
                if ($(this).css("display") != 'none') {
                    hasNotError = false;
                }
            });
            var r = /^\+?[1-9][0-9]*$/;
            if (!r.test($('#add_three_category_sort').val())) {
                $("#add_three_category_sort").parent("div").append("<label for='add_three_category_sort' generated='true' class='error'>请输入整数！</label>");
                $("#add_three_category_sort").addClass("error");
                return;
            }
            var level = $("#cur_level").val();
            var parentId = $("#parentId" + (parseInt(level) + 1)).val();

            if (num == 0) {
                $.ajax({
                    url: 'addThreeCategory.htm',
                    method: 'post',
                    data: $("#saveThreeCategoryForm").serialize(),
                    success: function (data) {
                        location.reload();
                    }
                });
            }

            $('#threeCateAdd').modal("hide");
        }

        /**
         * 弹框显示编辑商品分类
         * @param level 商品是属于几级分类
         * @param cateId 分类Id
         */
        function showEditCatalog(level, cateId) {

            $("#catalogEdit").html(mythml);
            $("#cur_level").val(level);
            var parentId = $("#parentId" + (parseInt(level) - 2)).val();
            $.post("queryCatalogById.htm", "&prodCatalogId=" + cateId, function (data) {
                data = data.prodCatalog;
                $("#update_catId").val(data.prodCatalogId);
                $("#update_cate_name").val(data.catalogName);
                $("#oldCatName").val(data.catalogName);
                $("#update_catalog_sort").val(data.sequenceNum)
                $("#update_cate_sort").val(data.catSort);
                $("#catalogEdit").modal("show");

            });
        }

        /**
         * 弹框显示编辑商品分类
         * @param level 商品是属于几级分类
         * @param cateId 分类Id
         */
        function showEditCate(level, cateId) {
            $("#cateEdit").html(myhtml1);
            $("#cur_level").val(level);
            var parentId = $("#parentId" + (parseInt(level) - 1)).val();
            $.post("queryCategoryById.htm", "productCategoryId=" + cateId, function (data) {
                data = data.category;

                $("#update_category_id").val(data.productCategoryId);
                $("#update_category_name").val(data.categoryName);
                $("#oldUpdateCatName").val(data.categoryName);
                $("#update_category_sort").val(data.sequenceNum);

                $.ajax({
                    url: 'queryCatalogs.htm',
                    method: 'post',
                    data: 'productStoreId=${requestAttributes.productStoreId}',
                    success: function (data) {
                        var html = '';
                        data = data.catalogs;
                        if (data) {
                            for (var i = 0; i < data.length; i++) {
                                var cate = data[i];
                                if (cate.prodCatalogId == parentId) {
                                    html += '<option value="' + cate.prodCatalogId + '" selected>' + cate.catalogName + '</option>';
                                } else {
                                    html += '<option value="' + cate.prodCatalogId + '">' + cate.catalogName + '</option>';
                                }
                            }
                            $('#update_parent_category_id').html(html);
                            $("#update_parent_category_id option[value=" + $("#parentId" + parseInt(level + 1)).val() + "]").attr("selected", true);
                            $('select[data-live-search="true"]').select2();
                        }
                    }
                });
                $("#cateEdit").modal("show");
            });
        }

        function showEditThreeCate(level, cateId) {
            $("#threeCateEdit").html(myhtml2);
            $("#cur_level").val(level);
            var parentId = $("#parentId" + (parseInt(level))).val();
            $.post("queryCategoryById.htm", "productCategoryId=" + cateId, function (data) {
                data = data.category;

                $("#update_three_category_id").val(data.productCategoryId);
                $("#update_three_category_name").val(data.categoryName);
                $("#oldUpdateThreeCatName").val(data.categoryName);
                $("#update_three_category_sort").val(data.sequenceNum);
                $.ajax({
                    url: 'findTopCategories.htm',
                    method: 'post',
                    data: 'catalogId=' + parentId,
                    success: function (data) {
                        var html = '';
                        data = data.categories;
                        if (data) {
                            for (var i = 0; i < data.length; i++) {
                                var cate = data[i];
                                if (cate.productCategoryId == parentId) {
                                    html += '<option value="' + cate.productCategoryId + '" selected>' + cate.categoryName + '</option>';
                                } else {
                                    html += '<option value="' + cate.productCategoryId + '">' + cate.categoryName + '</option>';
                                }
                            }

                            $('#update_three_parent_category_id').html(html);
                            $("#update_three_parent_category_id option[value=" + $("#parentId" + parseInt(level + 1)).val() + "]").attr("selected", true);
                            $('select[data-live-search="true"]').select2();
                        }
                    }
                });
                $("#threeCateEdit").modal("show");
            });
        }


        /**
         * 编辑分类时，分类名称onblur，检查分类名是否重复
         * @param obj
         */
        function checkUpdateCatNameExist(obj) {
            $(obj).removeClass("error");
            $("#cateName_exist_tip").remove();
            var oldCategoryName = $("#oldCategoryName").val();
            var cateName = $(obj).val();
            if (cateName != oldCategoryName) {
                $.post("checkCatalogNameExist.htm", "catalogName=" + cateName + "&productStoreId=${requestAttributes.productStoreId}", function (data) {
                    if (data.isExist && data.isExist == 1) {
                        $(obj).addClass("error");
                        $(obj).after('<label generated="true" class="error value_tip" id="cateName_exist_tip">分类名称已存在</label>');
                    }
                });
            }
        }


        function checkUpdateCatalogNameExist(obj) {
            $(obj).removeClass("error");
            $("#cateName_exist_tip").remove();
            var oldCatName = $("#oldCatalogName").val();
            var cateName = $(obj).val();
            if (cateName != oldCatName) {
                $.post("checkCatalogNameExist.htm", "catalogName=" + cateName + "&productStoreId=${requestAttributes.productStoreId}", function (data) {
                    if (data.isExist && data.isExist == 1) {
                        $(obj).addClass("error");
                        $(obj).after('<label generated="true" class="error value_tip" id="cateName_exist_tip">分类名称已存在</label>');
                    }
                });
            }
        }

        function checkUpdateThreeCatNameExist(obj) {
            $(obj).removeClass("error");
            $("#cateName_exist_tip").remove();
            var oldCategoryName = $("#oldThreeCategoryName").val();
            var cateName = $(obj).val();
            if (cateName != oldCategoryName) {
                $.post("checkCatalogNameExist.htm", "catalogName=" + cateName + "&productStoreId=${requestAttributes.productStoreId}", function (data) {
                    if (data.isExist && data.isExist == 1) {
                        $(obj).addClass("error");
                        $(obj).after('<label generated="true" class="error value_tip" id="cateName_exist_tip">分类名称已存在</label>');
                    }
                });
            }
        }

        function checkNumber(obj) {

            var r = /^\+?[1-9][0-9]*$/;
            if (!r.test($(obj).val())) {
                $(obj).parent("div").append("<label for='update_catalog_sort' generated='true' class='error'>请输入整数！</label>");
                $(obj).addClass("error");
                return;
            } else {
                $(obj).removeClass('error');
                $(obj).next().remove();
            }
        }

        /**
         * 编辑商品分类
         */
        function submitUpdateCatalogForm() {
            var hasNotError = true;
            $("#updateCatalogForm").find(".error").each(function () {
                if ($(this).css("display") != 'none') {
                    hasNotError = false;
                }
            });
            if (!hasNotError) return;
            var r = /^\+?[1-9][0-9]*$/;
            if (!r.test($('#update_catalog_sort').val())) {
                $("#update_catalog_sort").parent("div").append("<label for='update_catalog_sort' generated='true' class='error'>请输入整数！</label>");
                $("#update_catalog_sort").addClass("error");
                return;
            }
            var level = $("#cur_level").val();
            var parentId = $("#parentId" + (parseInt(level) - 1)).val();
            $.ajax({
                url: 'updateCatalog.htm',
                method: 'post',
                data: $("#updateCatalogForm").serialize(),
                success: function (data) {
                    location.reload();
                }
            });
            $('#catelogEdit').modal("hide");
        }

        /**
         * 编辑商品分类
         */
        function submitUpdateCategoryForm() {
            var hasNotError = true;
            $("#updateCategoryForm").find(".error").each(function () {
                if ($(this).css("display") != 'none') {
                    hasNotError = false;
                }
            });
            if (!hasNotError) return;
            var r = /^\+?[1-9][0-9]*$/;
            if (!r.test($('#update_category_sort').val())) {
                $("#update_category_sort").parent("div").append("<label for='update_category_sort' generated='true' class='error'>请输入整数！</label>");
                $("#update_category_sort").addClass("error");
                return;
            }
            var level = $("#cur_level").val();
            var parentId = $("#parentId" + (parseInt(level) - 1)).val();

            $.ajax({
                url: 'updateCategory.htm',
                method: 'post',
                data: $("#updateCategoryForm").serialize(),
                success: function (data) {
                    location.reload();
                }
            });
            $('#cateEdit').modal("hide");
        }

        function submitUpdateThreeCategoryForm() {
            var hasNotError = true;
            $("#updateThreeCategoryForm").find(".error").each(function () {
                if ($(this).css("display") != 'none') {
                    hasNotError = false;
                }
            });
            if (!hasNotError) return;
            var r = /^\+?[1-9][0-9]*$/;
            if (!r.test($('#update_three_category_sort').val())) {
                $("#update_three_category_sort").parent("div").append("<label for='update_three_category_sort' generated='true' class='error'>请输入整数！</label>");
                $("#update_three_category_sort").addClass("error");
                return;
            }
            var level = $("#cur_level").val();
            var parentId = $("#parentId" + (parseInt(level) - 1)).val();

            $.ajax({
                url: 'updateThreeCategory.htm',
                method: 'post',
                data: $("#updateThreeCategoryForm").serialize(),
                success: function (data) {
//                location.reload();
                }
            });
            $('#threeCateEdit').modal("hide");
        }

        /**
         * 删除商品分类
         * @param cateId 分类Id
         */
        function deleteCatalog(cateId) {
            $.post("checkCatalogDel.htm", "prodCatalogId=" + cateId, function (data) {
                if (data && data.canDel == '1') {
                    showDeleteOneConfirmAlert("delCatalog.htm", "prodCatalogId=" + cateId);
                } else {
                    //输入错误
                    showTipAlert("该分类下包含子分类，不能删除！");
                }
            });
        }

        /**
         * 删除商品分类
         * @param cateId 分类Id
         */
        function deleteCategory(cateId) {
            $.post("checkCategoryDel.htm", "productCategoryId=" + cateId, function (data) {
                if (data && data.canDel == '1') {
                    showDeleteOneConfirmAlert("delCategory.htm", "productCategoryId=" + cateId);
                } else {
                    //输入错误
                    showTipAlert("该分类下包含子分类，不能删除！");
                }
            });
        }

        /**
         * 删除商品分类
         * @param cateId 分类Id
         */
        function deleteThreeCategory(cateId) {
            $.post("checkCategoryDel.htm", "productCategoryId=" + cateId, function (data) {
                if (data && data.canDel == '1') {
                    showDeleteOneConfirmAlert("delCategory.htm", "productCategoryId=" + cateId);
                } else {
                    //输入错误
                    showTipAlert("该分类下包含子分类，不能删除！");
                }
            });
        }


        /**
         * 删除单个记录的确认框
         * @param deleteUrl 删除链接。
         */
        function showDeleteOneConfirmAlert(deleteUrl, params, tips) {
            $("#modalDialog").remove();
            var confirmDialog =
                    '<div class="modal fade" id="modalDialog" tabindex="-1" role="dialog">' +
                    '    <div class="modal-dialog">' +
                    '        <div class="modal-content">' +
                    '            <div class="modal-header">' +
                    '                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                    '               <h4 class="modal-title">系统提示</h4>' +
                    '           </div>' +
                    '           <div class="modal-body">';
            if (tips != '' && tips != undefined) {
                confirmDialog += tips;
            } else {
                confirmDialog += '确认要删除这条记录吗？';
            }
            confirmDialog += '           </div>' +
                    '           <div class="modal-footer">' +
                    '               <button type="button" class="btn btn-primary" onclick="doAjaxDeleteOne(\'' + deleteUrl + '\',\'' + params + '\',);">确定</button>' +
                    '               <button type="button" class="btn btn-default" data-dismiss="modal" onclick="$(\'#modalDialog\').modal(\'hide\');">取消</button>' +
                    '           </div>' +
                    '       </div>' +
                    '   </div>' +
                    '</div>';
            $(document.body).append(confirmDialog);
            $('#modalDialog').modal('show');
        }

        function doAjaxDeleteOne(deleteUrl, params) {
            $.ajax({
                url: deleteUrl,
                method: 'post',
                data: params,
                success: function (data) {
                    location.reload();
                }
            });
        }

        /**
         * 提示框
         * @param tip 提示内容
         */
        function showTipAlert(tip, callback) {
            $("#modalDialog").remove();
            var dialogHtml =
                    '<div class="modal fade" id="modalDialog" tabindex="-1" role="dialog" style="z-index:99999;">' +
                    '    <div class="modal-dialog">' +
                    '        <div class="modal-content">' +
                    '            <div class="modal-header">' +
                    '                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                    '               <h4 class="modal-title">操作提示</h4>' +
                    '           </div>' +
                    '           <div class="modal-body" style="text-align: center;">' +
                    tip +
                    '           </div>' +
                    '           <div class="modal-footer">' +
                    '               <button type="button" class="btn btn-primary" data-dismiss="modal">确定</button>' +
                    '           </div>' +
                    '       </div>' +
                    '   </div>' +
                    '</div>';
            $(document.body).append(dialogHtml);

            var dialog = $('#modalDialog');
            dialog.off('click', '.btn-primary');
            dialog.on('click', '.btn-primary', function () {
                if (typeof callback == 'function') {
                    callback();
                }
                $('#modalDialog').modal('hide');
            });
            $('#modalDialog').modal('show');
        }
    </script>
    <style>
        label.error {
            color: #a94442;
            margin-top: 5px;
        }

        label {
            display: inline-block;
            max-width: 100%;
            margin-bottom: 5px;
            font-weight: bold;
        }

        input.error {
            color: #a94442;
            border-color: #a94442;
            text-align: left;
        }

        input[type="text"] {
            border: 1px solid #ccc;
        }
    </style>