
<!-- begin 基本信息 -->
<div class="box box-info">
    <div class="box-header with-border">
       <ul class="nav nav-tabs">
                <li role="presentation"><a href="<@ofbizUrl>member_Detail</@ofbizUrl>?partyId=${request.getParameter("partyId")}">基本信息</a></li>
                <li role="presentation"  ><a href="<@ofbizUrl>productPromoCodeParty</@ofbizUrl>?partyId=${request.getParameter("partyId")}" >已领取优惠券</a></li>
                <li role="presentation"  class="active"><a href="<@ofbizUrl>CustomJuice</@ofbizUrl>?partyId=${request.getParameter("partyId")}" >定制果汁</a></li>
       </ul>
    </div>
    <div class="box-body">
            <form class="form-inline clearfix"  id="searchForm" role="form" action="<@ofbizUrl>findTagList</@ofbizUrl>">
            
            <div class="form-group col-sm-6">
                   <label for="title" class="col-sm-3 control-label">已定制个数:</label>
                   <div class="col-sm-6">
                   <span>${promoListSize?default('0')}</span>
                   </div>
            </div>
            
            </form>
            <div class="cut-off-rule bg-gray"></div>
            <div class="btn-box m-b-10">
             <div class="col-sm-6">
             <div class="dp-tables_btn">
             </div>
            </div><!-- 操作按钮组end -->
            <#if promoList?has_content>
                <!-- 列表当前分页条数start -->
                <div class="col-sm-6">
                    <div class="dp-tables_length">
                        <label>
                        ${uiLabelMap.displayPage}
                            <select id="dp-tables_length" name="tables_length" class="form-control input-sm"
                                    onchange="location.href='${commonUrl}&amp;VIEW_SIZE='+this.value+'&amp;VIEW_INDEX=0'">
                                <option value="10" <#if viewSize==10>selected</#if>>10</option>
                                <option value="20" <#if viewSize==20>selected</#if>>20</option>
                                <option value="30" <#if viewSize==30>selected</#if>>30</option>
                                <option value="40" <#if viewSize==40>selected</#if>>40</option>
                            </select>
                          ${uiLabelMap.brandItem}
                        </label>
                    </div>
                </div><!-- 列表当前分页条数end -->
            </div><!-- 工具栏end -->
            
            <div class="row">
                <div class="col-sm-12">
                    <table id="example2" class="table table-bordered table-hover js-checkparent">
                        <thead>
                        <tr>
                            <th>商品名称</th>
                            <th>容积(ml/瓶)</th>
                            <th>商品配置</th>
                            <th>价格(元)</th>
                            <th>定制时间</th>
                        </tr>
                        </thead>
                        <tbody>
                         <#list promoList as promo_List>
                        <tr>
                            <td>${promo_List.itemDescription?if_exists}</td>
                            <td>${promo_List.volume?if_exists}</td>
                            <td>
                            ${promo_List.configName?if_exists}
                            </td>
                            <td>
                            ${promo_List.unitPrice?if_exists}
                            </td>
                            <td>${promo_List.createdStamp?if_exists}</td>
                        </tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div><!-- 表格区域end -->
            <!-- 分页条start -->
        <#include "component://common/webcommon/includes/htmlTemplate.ftl"/>
        <#assign viewIndexFirst = 0/>
        <#assign viewIndexPrevious = viewIndex - 1/>
        <#assign viewIndexNext = viewIndex + 1/>
        <#assign viewIndexLast = Static["org.ofbiz.base.util.UtilMisc"].getViewLastIndex(promoListSize, viewSize) />
        <#assign messageMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("lowCount", lowIndex, "highCount", highIndex, "total", promoListSize)/>
        <#assign commonDisplaying = Static["org.ofbiz.base.util.UtilProperties"].getMessage("CommonUiLabels", "CommonDisplaying", messageMap, locale)/>
        <@nextPrev commonUrl=commonUrl ajaxEnabled=false javaScriptEnabled=false paginateStyle="nav-pager" paginateFirstStyle="nav-first" viewIndex=viewIndex highIndex=highIndex
        listSize=promoListSize viewSize=viewSize ajaxFirstUrl="" firstUrl="" paginateFirstLabel="" paginatePreviousStyle="nav-previous" ajaxPreviousUrl="" previousUrl="" paginatePreviousLabel=""
        pageLabel="" ajaxSelectUrl="" selectUrl="" ajaxSelectSizeUrl="" selectSizeUrl="" commonDisplaying=commonDisplaying paginateNextStyle="nav-next" ajaxNextUrl="" nextUrl=""
        paginateNextLabel="" paginateLastStyle="nav-last" ajaxLastUrl="" lastUrl="" paginateLastLabel="" paginateViewSizeLabel="" />
       <!-- 分页条end -->
       <#else>
        <div id="noData" class="col-sm-12">
            <h3>没有定制果汁</h3>
        </div>
	  </#if>
        </div>
        <!-- /.box-body -->
    </div>
    
<script>
   
     
</script>