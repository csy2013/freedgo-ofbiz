<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/bootcss/css/order.css</@ofbizContentUrl>" type="text/css"/>
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/bootcss/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>" type="text/css"/>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/bootcss/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/bootcss/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/bootcss/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/bootcss/js/plugins/jQuery/jquery-migrate-1.2.1.js</@ofbizContentUrl>"></script>
<script type="text/javascript" src="<@ofbizContentUrl>/images/themes/bootcss/dist/js/main.js</@ofbizContentUrl>"></script>
<#assign commonUrl = "productCollection?lookupFlag=Y&"+ paramList +"&">
<!-- 内容start -->
<div class="box box-info">
    <div class="box-body">
        <!-- 条件查询start -->
        <form id="QueryForm" method="post" class="form-inline clearfix" role="form" action="<@ofbizUrl>productCollection</@ofbizUrl>">
            <div class="form-group">
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品编码</span>
                    <input type="text" name="productId" class="form-control" value="${(paramMap.productId)!''}">
                </div>

                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品名称</span>
                    <input type="text" name="productName" class="form-control" value="${(paramMap.productName)!''}">
                </div>
                <div class="input-group m-b-10">
                    <span class="input-group-addon">商品状态</span>
                    <select name = "productType" class="form-control">
                        <option value="" <#if (paramMap.productType)?default("") == ""> selected="selected" </#if> >=======全部=======</option>
                        <option value="01" <#if (paramMap.productType)?default("") == "01"> selected="selected" </#if>>未上架</option>
                        <option value="02" <#if (paramMap.productType)?default("") == "02"> selected="selected" </#if>>已上架</option>
                        <option value="03" <#if (paramMap.productType)?default("") == "03"> selected="selected" </#if>>已下架</option>
                    </select>
                </div>
            </div>
            <div class="input-group pull-right">
                <button class="btn btn-success btn-flat">查询</button>
            </div>
        </form><!-- 条件查询end -->

        <!-- 分割线start -->
        <div class="cut-off-rule bg-gray"></div>
        <!-- 分割线end -->
        <!-- 表格区域start -->
        
    <#if lookupFlag == "N">
        <!-- 列表当前分页条数start -->
        <div class="row m-b-12">
            <div class="col-sm-6">
            </div>
            <div class="col-sm-6">
                <div class="dp-tables_length">
                    <label>
                        每页显示
                        <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                            <option value="10" <#if viewSize==10>selected</#if>>10</option>
                            <option value="20" <#if viewSize==20>selected</#if>>20</option>
                            <option value="30" <#if viewSize==30>selected</#if>>30</option>
                            <option value="40" <#if viewSize==40>selected</#if>>40</option>
                        </select>
                        条
                    </label>
                </div>
            </div><!-- 列表当前分页条数end -->
        </div><!-- 工具栏end -->

        <div class="row">
            <div class="col-sm-12">
                <table class="table table-bordered table-hover js-checkparent">
                    <thead>
                    <tr class="js-sort-list">
                        <th><input class="js-allcheck" type="checkbox"></th>
                        <th>商品编码</th>
                        <th>商品名称</th>
                        <th>商品状态</th>
                        <th>收藏次数</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list productCollectionList as productCollection>
                        	<#if productCollection.contentId?has_content>
                        		<#assign productImage="/content/control/getImage?contentId=${productCollection.contentId}" />
                        	<#else>
								<#assign productImage ="/images/datasource/default/default_img.png"/>
                        	</#if>
	                        <tr class="xl_meg">
	                            <td><input value="${(productCollection.productId)!''}" class="js-checkchild" type="checkbox"></td>
	                            <td>${(productCollection.productId)!''}</td>
	                            <td>
	                            	<div class="form-group">
	                            		<span class="col-sm-6" style="display:inline-block;">
                                       		<img height="100"  src="<#if productImage?has_content><@ofbizContentUrl>${productImage}</@ofbizContentUrl></#if>" class="cssImgSmall" alt="" />
                                     	</span>
                                      	<span class="col-sm-6" style="display:inline-block;">
                                        	${(productCollection.productName)!''}
                                      	</span>
	                            	</div>
	                            </td>
	                            <td>${(productCollection.productType)!''}</td>
	                            <#assign productCollections = delegator.findByAnd("ProductCollection",{"productId":productCollection.productId}).size()>
	                            <td>${(productCollections)!''}</td>
	                            <td><button type='button' class='btn btn-danger btn-sm btn-sm' data-name="${(productCollection.productId)!''}" data-toggle="modal" data-target="#partyModal">查看</button></td>
	                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div><!-- 表格区域end -->
        <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign commonUrl = "productCollection?lookupFlag=Y&"+ paramList + "&"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex?if_exists - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(productCollectionListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", productCollectionListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=returnListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
        <!-- 分页条end -->
    <#else>
        <div id="findPartyResults_2" class="col-sm-12">
            <h3>没有数据</h3>
        </div>
    </#if>
    
    <!-- 退款 -->
    <div class="modal fade" id="partyModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel">
   		<div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="exampleModalLabel">收藏人员列表</h4>
            </div>
            <div class="modal-body">
		        <div class="form-group" data-type="required">
		            <table id="CPartyFM_data_tbl" class="table table-bordered table-hover js-checkparent"></table>               
		        </div>
		        <div class="row" id="CPartyFM_paginateDiv">
		        <div class="col-sm-6">
        <div id="view_size" class="dp-tables_length">
        </div>
      </div>
	         </div>
        </div>
      </div>
    </div>
    


<script>
    $(function(){
    	var CPartyFM_data_tbl;
		
		// 查看详情
		$(".btn-danger").on("click", function(){
			$('#CPartyFM_data_tbl').empty();
			$('#CPartyFM_paginateDiv').empty();
			var productId = $(this).data("name");
			CPartyFM_data_tbl = $('#CPartyFM_data_tbl').dataTable({
				ajaxUrl: "<@ofbizUrl>collectionPartyList?productId="+productId+"&VIEW_SIZE=5</@ofbizUrl>",
				columns:[
					{"title":"关注会员ID","code":"partyId"},
					{"title":"会员等级","code":"levelName"},
					{"title":"手机号","code":"mobile"},
					{"title":"Email","code":"email"},
					{"title":"收藏时间","code":"createdStamp",
					 "handle":function(td,record){
					 	var date = timeStamp2String(record.createdStamp.time);
					 	td.append(date);
					 }}
				],
				listName: "partyList",
				paginateEL: "CPartyFM_paginateDiv",
				headNotShow: true,
				midShowNum: 3,
				viewSizeEL: "view_size"
			});
		});
	});
</script>


