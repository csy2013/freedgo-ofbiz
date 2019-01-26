<#assign commonUrl = "productPromoCodeParty?"+ paramList+"&" />
<!-- begin 基本信息 -->
<div class="box box-info">
    <div class="box-header with-border">
       <ul class="nav nav-tabs">
                <li role="presentation"><a href="<@ofbizUrl>member_Detail</@ofbizUrl>?partyId=${request.getParameter("partyId")}">基本信息</a></li>
                <li role="presentation"  class="active"><a href="<@ofbizUrl>productPromoCodeParty</@ofbizUrl>?partyId=${request.getParameter("partyId")}" >已领取优惠券</a></li>
                <li role="presentation"  ><a href="<@ofbizUrl>CustomJuice</@ofbizUrl>?partyId=${request.getParameter("partyId")}" >定制果汁</a></li>
       </ul>
    </div>
    <div class="box-body">
            <form class="form-inline clearfix"  id="searchForm" role="form" action="<@ofbizUrl>findTagList</@ofbizUrl>">
            
            <div class="form-group col-sm-6">
                   <label for="title" class="col-sm-2 control-label">已领取:</label>
                   <div class="col-sm-10">
                   <span>${promoListSize?default('0')}</span>
                   </div>
            </div>
            <div class="form-group col-sm-6">
                   <label for="title" class="col-sm-2 control-label">已使用:</label>
                   <div class="col-sm-10">
                   <span>${usedCouponSize?default('0')}</span>
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
                            <th>券号</th>
                            <th>优惠券名称</th>
                            <th>优惠券类型</th>
                            <th>优惠券状态</th>
                            <th>领取时间</th>
                            <th>使用时间</th>
                        </tr>
                        </thead>
                        <tbody>
                         <#list promoList as promo_List>
                        <tr>
                            <td>${promo_List.productPromoCodeId?if_exists}</td>
                            <td>${promo_List.couponName?if_exists}</td>
                            
                            <td>
                            <#if promo_List.couponType=='COUPON_TYPE_REDUCE'>
                        满减类型    
                            <#elseif promo_List.couponType=='COUPON_TYPE_CASH'>
                           现金抵用
                            </#if>
                            </td>
                            
                            <td>
                            <#if promo_List.isUsed>
                           已使用
                            <#else>
                            未使用
                            </#if>
                            </td>
                            <td>${promo_List.useDate?if_exists}</td>
                            <td>${promo_List.usedTime?if_exists}</td>
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
            <h3>没有优惠券信息！</h3>
        </div>
	  </#if>
        </div>
        <!-- /.box-body -->
    </div>
    
<script>
   
     
</script>