<!-- Date Picker -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.css</@ofbizContentUrl>">
<!-- Daterange picker -->
<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker-bs3.css</@ofbizContentUrl>">
<!-- daterangepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/moment.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/daterangepicker/daterangepicker.js</@ofbizContentUrl>"></script>
<!-- datetimepicker -->
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.min.js</@ofbizContentUrl>"></script>
<script src="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/datetimepicker/bootstrap-datetimepicker.zh-CN.js</@ofbizContentUrl>"></script>

<form class="form-horizontal" role="form" method="post" name="editSecKill" id="editSecKill" action="<@ofbizUrl>editSecKill</@ofbizUrl>">
    <input type="hidden" name="activityId" id="activityId" value="${request.getParameter('activityId')}"/>
<#--秒杀支付条件为全部-->
    <input type="hidden" name="activityPayType" value="FULL_PAY" id="activityPayType"/>
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">秒杀活动基本信息</h3>
            <#--Del by zhajh at 20160317 bug 2033 Begin--->
            <#--
            <div class=" pull-right">
            <#if security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session)>
                <a class="btn btn-danger btn-flat" href="findSecKill">返回</a>
                <button class="btn btn-success btn-flat" onclick="updateSecKill()">保存</button></#if>
            </div>
            -->
            <#--Del by zhajh at 20160317 bug 2033 End--->
        </div>
        <div class="box-body">
            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="title" class="col-sm-3 control-label">使用店铺</label>

                    <div class="col-sm-9">
                        <select class="form-control" id="productStoreId"  name="productStoreId">
                        <#list productStores as productStore>
                            <option value="${(productStore.productStoreId)?if_exists}">${(productStore.storeName)?if_exists}</option>
                        </#list>
                        </select>

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="title" class="col-sm-3 control-label">活动类型</label>

                    <div class="col-sm-9">
                        <select class="form-control" id="activityType" name="activityType" disabled>
                        <#list activityTypeEnums as activityTypeEnum>

                            <option value="${(activityTypeEnum.enumId)?if_exists}"
                                    <#if activityTypeEnum.enumId == 'SEC_KILL'>selected</#if>>${(activityTypeEnum.get("description",locale))?if_exists}</option>
                        </#list>
                        </select>

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6">
                    <label for="subTitle" class="col-sm-3 control-label">活动状态</label>

                    <div class="col-sm-9">
                        <select class="form-control" id="activityAuditStatus" name="activityAuditStatus">
                            <option value="">全部</option>
                        <#list activityStatusEnums as activityStatusEnum>
                            <option value="${(activityStatusEnum.enumId)?if_exists}" <#if activityAuditStatus?default("")==activityStatusEnum.enumId>selected</#if>>${(activityStatusEnum.get("description",locale))?if_exists}</option>
                        </#list>
                        </select>

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="活动编码">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>活动编码</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="activityCode" name="activityCode">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6" data-type="required" data-mark="活动名称">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>活动名称</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="activityName" name="activityName">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-sm-6" data-type="linkLt" id="publishDateGroup" data-compare-link="startTimeGroup" data-mark="发布时间" data-compare-mark="销售开始时间">
                    <label for="publishDate" class="col-sm-3 control-label"><i class="required-mark">*</i>发布时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="startTime">
                        <input class="form-control" size="16" id="d_publishDate" type="text" readonly>
                        <input id="publishDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>

                <div id="endGroup" class="form-group col-sm-6">
                    <label for="endDate" class="col-sm-3 control-label"><i class="required-mark">*</i>下架时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="endTime">
                        <input class="form-control" size="16" id="d_endDate" type="text" readonly>
                        <input id="endDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-sm-6" data-type="linkLt" id="startTimeGroup" data-compare-link="endTimeGroup" data-mark="销售开始时间" data-compare-mark="销售结束时间">
                    <label for="startTime" class="col-sm-3 control-label"><i class="required-mark">*</i>销售开始时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="startTime">
                        <input class="form-control" size="16" type="text" id="d_activityStartDate" readonly>
                        <input id="activityStartDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
                <div id="endTimeGroup" class="form-group col-sm-6" data-type="linkLt" data-compare-link="endGroup" data-mark="销售结束时间" data-compare-mark="下架时间">
                    <label for="endTime" class="col-sm-3 control-label"><i class="required-mark">*</i>销售结束时间</label>

                    <div class="input-group date form_datetime col-sm-9 p-l-15 p-r-15" data-link-field="endTime">
                        <input class="form-control" size="16" type="text" id="d_activityEndDate" readonly>
                        <input id="activityEndDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="单个ID限购数量">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>单个ID限购数量</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="limitQuantity" name="limitQuantity">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
                <div class="form-group col-sm-6" data-type="required" data-mark="活动总数量">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>活动总数量</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="activityQuantity" name="activityQuantity">

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="title" class="col-sm-3 control-label"><i class="required-mark">*</i>秒杀商品</label>

                    <div class="col-sm-9">
                    <@htmlTemplate.lookupField formName="editSecKill" position="center" name="productId" id="productId" fieldFormName="LookupProduct"/>
                        <p class="dp-error-msg"></p>
                    </div>

                </div>

                <div class="form-group col-sm-6" id="shipmentTypeDiv">
                    <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>配送方式</label>

                    <div class="col-sm-9">
                        <select class="form-control" id="shipmentType" name="shipmentType">
                        <#list activityShipmentEnums as activityShipmentEnum>
                            <option value="${(activityShipmentEnum.enumId)?if_exists}">${(activityShipmentEnum.get("description",locale))?if_exists}</option>
                        </#list>
                        </select>

                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="row">
                    <div class="form-group col-sm-6">
                        <label for="subTitle" class="col-sm-3 control-label"><i class="required-mark">*</i>秒杀价格</label>

                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="productPrice" id="productPrice">
                            <p class="dp-error-msg"></p>
                        </div>

                    </div>
                    <div class="form-group col-sm-6">
                        <label for="title" class="col-sm-3 control-label">最多可抵扣的积分</label>
                        <div class="col-sm-9">
                            <input type="text" class="form-control dp-vd" name="scoreValue" id="scoreValue">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                <#--秒杀支付条件为全部-->
                </div>
            </div>

            <div class="row" id="virtualProductDate">
                <div class="form-group col-sm-6">
                    <label for="virtualStartTime" class="col-sm-3 control-label"><i class="required-mark">*</i>虚拟商品有效期自</label>

                    <div class="input-group date form_date col-sm-9 p-l-15 p-r-15" data-date-format="yyyy-mm-dd" data-link-field="startTime">
                        <input class="form-control" size="16" type="text" id="d_virtualProductStartDate" readonly>
                        <input id="virtualProductStartDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
                <div id="virtualEndTimeGroup" class="form-group col-sm-6">
                    <label for="endTime" class="col-sm-3 control-label" ><i class="required-mark">*</i>虚拟商品有效期至</label>

                    <div class="input-group date form_date col-sm-9 p-l-15 p-r-15" data-date-format="yyyy-mm-dd" data-link-field="endTime">
                        <input class="form-control" size="16" type="text" id="d_virtualProductEndDate" readonly>
                        <input id="virtualProductEndDate" class="dp-vd" type="hidden">
                        <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
                        <span class="input-group-addon"><span class="fa fa-calendar"></span></span>
                    </div>
                    <div class="dp-error-msg col-sm-offset-2 col-sm-10"></div>
                </div>
            </div>


            <div class="row">
                <div class="form-group col-sm-12">
                    <label class="col-sm-2 control-label">团购选项</label>

                    <div class="col-sm-10">
                        <div class="checkbox clearfix">
                            <#--<label class="col-sm-3" title="随时退"><input name="isAnyReturn" id="isAnyReturn" type="checkbox" value="Y">随时退</label>-->
                                <input type="hidden" name="isAnyReturn" value="N"/>
                            <label class="col-sm-3" title="支持过期退"><input name="isSupportOverTimeReturn" id="isSupportOverTimeReturn" value="Y" type="checkbox">支持过期退</label>
                            <label class="col-sm-3" title="动可积分"><input name="isSupportScore" id="isSupportScore" type="checkbox" value="Y">活动可积分</label>
                            <label class="col-sm-3" title="退货返回积分"><input name="isSupportReturnScore" id="isSupportReturnScore" value="Y" type="checkbox">退货返回积分</label>
                            <label class="col-sm-3" title="推荐到首页"><input name="isShowIndex" id="isShowIndex" type="checkbox" value="Y">推荐到首页</label>
                            <label class="col-sm-3" title="包邮"><input name="isPostageFree" id="isPostageFree" type="checkbox" value="Y">包邮</label>
                        </div>
                        <div class="dp-error-msg"></div>
                    </div>
                </div>
            </div>

            <div class="row p-l-10 p-r-10">
                <div class="form-group">
                    <label for="seo" class="col-sm-2 control-label">活动描述</label>

                    <div class="col-sm-8">
                        <#--<textarea class="form-control" name="activityDesc" id="activityDesc" rows="6"></textarea>-->
                   <#-- <@htmlTemplate.renderTextareaField name="activityDesc" className="dojo-ResizableTextArea" alert="false" value=""
                      cols="60" rows="7" id="textData" \ visualEditorEnable="true" language="zh_CN" buttons="maxi" />-->
                        <@htmlTemplate.renderTextareaField name="activityDesc" className="dojo-ResizableTextArea" alert="false"
                        value="" cols="60" rows="15" id="textData" readonly="" visualEditorEnable="true" language="zh_CN" buttons="maxi" />
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>
        </div>
    </div>
   <#-- <div class="box box-info">
        <div class="box-header">
            <h3 class="box-title">团购规则设置</h3>
            <button class="btn-default pull-right" onclick="addSecKillRule();">新增</button>
        </div>


        <div class="box-body table-responsive no-padding">
            <table class="table table-hover" id="secKillRuleTable">
                <tr>
                    <th>序号</th>
                    <th>阶梯编号</th>
                    <th>团购人数</th>
                    <th>团购价格</th>
                    <th>操作</th>
                </tr>
            </table>
        </div>
    </div>-->
    <div class="box box-info js-party-level">
        <div class="box-header">
            <h3 class="box-title">参加的会员等级</h3>

        </div>


        <div class="box-body table-responsive no-padding">
            <table class="table table-hover js-checkparent js-sort-list " id="partyLevelTable">
                <tr>
                    <th>序号</th>
                    <th>会员等级</th>
                    <th>参加标志<input class="js-allcheck" type="checkbox" id="checkAll"/></th>
                </tr>
            <#list partyLevels as partyLevel>
                <tr>
                    <td>${partyLevel_index}</td>
                    <td>${partyLevel.levelName}</td>
                    <td><input class="js-checkchild" type="checkbox" name="partyLevels" value="${partyLevel.levelId}:${partyLevel.levelName}"></td>
                </tr>
            </#list>

            </table>
        </div>
    </div>
    <div class="box box-info">
        <div class="box-header">
            <h3 class="box-title">参加的社区</h3>

        </div>
        <div class="box-body table-responsive no-padding">
            <table class="table table-hover js-checkparent js-sort-list" id="communitTable">
                <tr>
                    <th>序号</th>
                    <th>社区编号</th>
                    <th>社区名称</th>
                    <th>操作<input class="js-allcheck" type="checkbox" id="checkAll"></th>
                </tr>
            <#list communities as communit>
                <tr>
                    <td>${communit_index}</td>
                    <td>${communit.code}</td>
                    <td>${communit.name}</td>
                    <td><input class="js-checkchild" type="checkbox" name="areas" value="${communit.communityId}:${communit.name}"></td>
                </tr>
            </#list>
            </table>
        </div>
    </div>
    
    <#--Add by zhajh at 20160317 bug 2033 Begin--->
    <div class=" pull-right">
    	<#if security.hasEntityPermission("PRODPROMO_SECKILL", "_UPDATE", session)>
        <a class="btn btn-danger btn-flat" href="findSecKill">返回</a>
        <button class="btn btn-success btn-flat" onclick="updateSecKill()">保存</button></#if>
    </div>
    <#--Add by zhajh at 20160317 bug 2033 End--->
</form>
<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_msg_title">操作提示</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_msg_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 提示弹出框end -->

<!-- add user Modal -->
<div id="modal_add" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">>
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">新增团购规则</h4>
            </div>
            <div class="modal-body">


                <div class="form-group" data-type="required" data-mark="阶梯编号">
                    <label for="orderGouprRuleId" class="control-label col-sm-3"><span class="text-danger">*</span>阶梯编号</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control required" name="orderGouprRuleId" id="orderGouprRuleId">
                    </div>
                    <span id="usertip"></span>
                </div>
                <div class="form-group" data-type="required" data-mark="团购人数">
                    <label for="orderGouprRulePersonNum" class="control-label col-sm-3"><span class="text-danger">*</span>团购人数</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control required" name="orderGouprRulePersonNum" id="orderGouprRulePersonNum">
                    </div>
                </div>

                <div class="form-group" data-type="required" data-mark="团购价格">
                    <label for="orderGouprRulePersonAmount" class="control-label col-sm-3"><span class="text-danger">*</span>团购价格</label>

                    <div class="col-sm-9">
                        <input type="text" class="form-control required" name="orderGouprRulePersonAmount" id="orderGouprRulePersonAmount">
                    </div>
                </div>

            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="saveSecKillRule();">确定</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div>

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
      	<button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">修改</button>
        <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">继续</button>
      </div>
    </div>
  </div>
</div><!--确认弹出框end -->

<script type="text/javascript">


    $(function () {
    
        // Add by zhajh at 20160315 隐藏参加会员等级内容 Begin
        $('.js-party-level').hide();
        // Add by zhajh at 20160315 隐藏参加会员等级内容 End
        
        $('.form_datetime').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            forceParse: 0,
            showMeridian: 1
        });

        $('.form_date').datetimepicker({
            language: 'zh-CN',
            todayBtn: 1,
            autoclose: 1,
            todayHighlight: 1,
            startView: 2,
            minView: 2,
            forceParse: 0
        });

        $('.form_time').datetimepicker({
            language: 'zh-CN',
            autoclose: 1,
            startView: 1,
            forceParse: 0
        });


        //加载团购信息
        $.ajax(
                {
                    url: "productActivityDetail",
                    type: "POST",
                    data: {activityId: ${request.getParameter('activityId')}},
                    dataType: "json",
                    async:false,
                    success: function (data) {
                        console.log(data);
                        if (data) {
                            if (data.productStoreIds) {
                                for (var i = 0; i < data.productStoreIds.length; i++) {
                                    var productStoreId = data.productStoreIds[i];
                                    $('#productStoreId').val(productStoreId);
                                }
                            }
                            $('#activityTypee').val(data.activityTypee);
                            $('#activityAuditStatus').val(data.activityAuditStatus);
                            $('#activityCode').val(data.d_activityCode);
                            $('#activityName').val(data.d_activityName);
                            $('#activityDesc').html(data.d_activityDesc);
                            // Mod by zhajh at 20160316  Bug[2037 秒杀修改页面中，活动描述有时不能显示] Begin
                            //CKEDITOR.instances['textData'].setData(data.d_activityDesc);
                            //CKEDITOR.replace(textData);
                            CKEDITOR.instances.textData.setData(data.d_activityDesc);
                            // Mod by zhajh at 20160316  Bug[2037 秒杀修改页面中，活动描述有时不能显示] End
                            $('#d_publishDate').val(data.d_publishDate);
                            $('#d_endDate').val(data.d_endDate);
                            $('#d_activityStartDate').val(data.d_activityStartDate);
                            $('#d_activityEndDate').val(data.d_activityEndDate);
                            $('#publishDate').val(data.d_publishDate);
                            $('#endDate').val(data.d_endDate);
                            $('#activityStartDate').val(data.d_activityStartDate);
                            $('#activityEndDate').val(data.d_activityEndDate);
                            $('#limitQuantity').val(data.d_limitQuantity);
                            $('#activityQuantity').val(data.d_activityQuantity);
                            $("input[name='productId']").val(data.productId);
                            $('#shipmentType').val(data.shipmentType);
                            $('#scoreValue').val(data.d_scoreValue);
                            $('#activityPayType').val(data.activityPayType);

                            $('#productPrice').val(data.d_productPrice);
                            $('#d_virtualProductStartDate').val(data.d_virtualProductStartDate);
                            $('#d_virtualProductEndDate').val(data.d_virtualProductEndDate);
                            $('#virtualProductStartDate').val(data.d_virtualProductStartDate);
                            $('#virtualProductEndDate').val(data.d_virtualProductEndDate);

                            if(data.d_productType){
                                if(data.d_productType=='VIRTUAL_GOOD'){
                                    $('#shipmentTypeDiv').hide();
                                    $('#virtualProductDate').show();
                                }
                                else if(data.d_productType=='FINISHED_GOOD'){
                                    $('#virtualProductDate').hide();
                                    $('#shipmentTypeDiv').show();
                                    $('#virtualProductStartDate').val('');
                                    $('#virtualProductEndDate').val();
                                }
                            }
                            if (data.d_isAnyReturn == 'Y') {
                                $('#isAnyReturn').eq(0).attr("checked", true);
                            }
                            if (data.d_isSupportOverTimeReturn == 'Y') {
                                $('#isSupportOverTimeReturn').eq(0).attr("checked", true);
                            }
                            if (data.d_isSupportScore == 'Y') {
                                $('#isSupportScore').eq(0).attr("checked", true);
                            }
                            if (data.d_isSupportReturnScore == 'Y') {
                                $('#isSupportReturnScore').eq(0).attr("checked", true);
                            }
                            if (data.d_isShowIndex == 'Y') {
                                $('#isShowIndex').eq(0).attr("checked", true);
                            }
                            if (data.d_isPostageFree == 'Y') {
                                $('#isPostageFree').eq(0).attr("checked", true);
                            }
                            if (data.d_productActivityPartyLevels) {
                                for (var i = 0; i < data.d_productActivityPartyLevels.length; i++) {
                                    var obj = data.d_productActivityPartyLevels[i];
                                    var levelId = obj.levelId;
                                    var levelName = obj.levelName;
                                    $("input[name='partyLevels']").each(function (i, obj) {
                                        console.log(obj);
                                        console.log(levelId + ":" + levelName);
                                        var val = $(obj).val();
                                        if (val == levelId + ":" + levelName) {
                                            $(obj).attr("checked", true);
                                        }
                                    })
                                }

                            }


                            if (data.d_productActivityAreas) {
                                for (var i = 0; i < data.d_productActivityAreas.length; i++) {
                                    var obj = data.d_productActivityAreas[i];
                                    var communityId = obj.communityId;
                                    var communityName = obj.communityName;
                                    $("input[name='areas']").each(function (i, obj) {
                                        var val = $(obj).val();
                                        if (val == communityId + ":" + communityName) {
                                            $(obj).attr("checked", true);
                                        }
                                    })
                                }
                            }
                        }
                    },
                    error: function (data) {
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("操作失败！");
                        $('#modal_msg').modal();
                    }
                }
        )

        //保存秒杀活动的内容
	    $('#modal_confirm #ok').click(function(e){
	       	editSeckillSave();
	    });
        
        $('#editSecKill').dpValidate({
            validate: true,
            clear: true,
            callback: function () {

                $.ajax({
					url: '<@ofbizUrl>chkActivityDateForKill</@ofbizUrl>',
					type: "POST",
					async: false,
					data: {
					    activityStartDate: $('#activityStartDate').val(),
					    activityEndDate: $('#activityEndDate').val(),
					},
					dataType: "json",
					success: function (data) {
		                if(data.chkFlg=='N'){
					       //alert("失败");
					       if(data.activityEndDateChked !=""){
					           $('#activityEndDate').val(data.activityEndDateChked);
					       }
					       $('#modal_confirm #modal_confirm_body').html("秒杀的销售结束时间不正确(秒杀时间不能超过销售开始时间后的99小时59分),是否继续?");
			               $('#modal_confirm').modal('show');
					    }else{
					       //alert("成功");
					       editSeckillSave();
					    }
					},
					error: function (data) {
					    //设置提示弹出框内容
					    $('#modal_msg #modal_msg_body').html("操作失败！");
					    $('#modal_msg').modal();
					    $('#modal_msg').on('hide.bs.modal', function () {
							window.location.reload();
					    })
					}
				});
            }
        })

    });
    function beforeSend(XMLHttpRequest){
        for ( instance in CKEDITOR.instances ) {
            CKEDITOR.instances[instance].updateElement();
            return true;
        }
    }


    function updateSecKill() {
        $('#editSecKill').dpValidate({
            clear: true
        });
    }

    function deleteSecKill(id) {
        $.ajax({
            url: 'deleteSecKill',
            data: {activityId: id},
            type: "POST",
            async: false,
            dataType: "json",
            success: function (data) {
                if (data) {
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("操作成功！");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').on('hide.bs.modal', function () {
                        window.location.href='<@ofbizUrl>findSecKill</@ofbizUrl>';
                    })
                }
            },
            error: function (data) {

                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
                $('#modal_msg').on('hide.bs.modal', function () {
                    window.location.reload();
                })
            }

        })
    }


    function set_product_values (value, value2) {
        if(GLOBAL_LOOKUP_REF.getReference(ACTIVATED_LOOKUP)){
            obj_caller.target = GLOBAL_LOOKUP_REF.getReference(ACTIVATED_LOOKUP).target;
        }
        else {
            obj_caller.target = jQuery(obj_caller.targetW);
        }
        var target = obj_caller.target;

        write_value(value, target);

        if (SHOW_DESCRIPTION) setLookDescription(target.attr("id"), value + " " + value2, "", "", SHOW_DESCRIPTION);

        if(value2=='VIRTUAL_GOOD'){
            $('#shipmentTypeDiv').hide();
            $('#virtualProductDate').show();
//            $('#virtualStartTimeDiv').attr("data-type","linkLt");
        }
        if(value2=='FINISHED_GOOD'){
            $('#virtualProductDate').hide();
            $('#shipmentTypeDiv').show();
            $('#virtualProductStartDate').val('');
            $('#virtualProductEndDate').val();
//            $('#virtualStartTimeDiv').removeAttr("data-type");
//            $('#virtualStartTimeDiv').removeAttr("data-compare-link");
        }


        closeLookup();
    }
    
    function editSeckillSave(){
        //检查虚拟产品时间
        if($('#virtualProductDate').is(":visible")) {
            var virProductStartDate = $('#virtualProductStartDate').val();
            var virProductEndDate = $('#virtualProductEndDate').val();
            if ($('#virtualProductStartDate').val() == '') {
                alert('请输入虚拟产品有效开始时间');
                return;
            }else{
               // Add by zhajh at 20160317 设置默认时分秒 Begin
               //virProductStartDate = virProductStartDate+' 00:00:00';
               virProductStartDate = virProductStartDate;
               // Add by zhajh at 20160317 设置默认时分秒 End
            }

            if ($('#virtualProductEndDate').val() == '') {
                alert('请输入虚拟产品有效结束时间')
                return;
            }else{
               // Add by zhajh at 20160317 设置默认时分秒 Begin
               //virProductEndDate = virProductEndDate+' 00:00:00';
               virProductEndDate = virProductEndDate;
               // Add by zhajh at 20160317 设置默认时分秒 End
            }
            
            if (virProductStartDate > virProductEndDate) {
                alert('输入虚拟产品有效开始时间必须小于虚拟产品有效结束时间');
                return;
            }
        }else{
            $('#virtualProductStartDate').val('');
            $('#virtualProductEndDate').val();
        }


        var levels = "";
        // Del by zhajh at 20160315  2023 新增团购或秒杀活动中，能参加的会员等级模块直接隐藏  Begin
        <#--
        $("input[name='partyLevels']").each(function () {
            if (this.checked) {
                console.log($(this).val());
                levels += $(this).val() + ","
            }
        });

        if (levels == '') {
            $('#modal_msg #modal_msg_body').html("请设置参加的会员等级！");
            $('#modal_msg').modal();
            return;
        }
        if (levels.substring(levels.length - 1) == ',') {
            levels = levels.substring(0, levels.length - 1);
        }

        console.log("levels=" + levels);
        -->
        // Del by zhajh at 20160315  2023 新增团购或秒杀活动中，能参加的会员等级模块直接隐藏  End
        var areas = "";
        $("input[name='areas']").each(function () {
            if (this.checked) {
                areas += $(this).val() + ","
            }
        });

        if (areas == '') {
            $('#modal_msg #modal_msg_body').html("请设置参加的社区！");
            $('#modal_msg').modal();
            return;
        }
        if (areas.substring(areas.length - 1) == ',') {
            areas = areas.substring(0, areas.length - 1);
        }

        console.log("areas=" + areas);
        var isAnyReturn = 'N';
        if ($('#isAnyReturn').is(':checked')) {
            isAnyReturn = 'Y';
        }
        var isSupportOverTimeReturn = 'N';
        if ($('#isSupportOverTimeReturn').is(':checked')) {
            isSupportOverTimeReturn = 'Y';
        }
        var isSupportScore = 'N';
        if ($('#isSupportScore').is(':checked')) {
            isSupportScore = 'Y';
        }
        var isSupportReturnScore = 'N';
        if ($('#isSupportReturnScore').is(':checked')) {
            isSupportReturnScore = 'Y';
        }
        var isShowIndex = 'N';
        if ($('#isShowIndex').is(':checked')) {
            isShowIndex = 'Y';
        }
        
        var isPostageFree = 'N';
        if ($('#isPostageFree').is(':checked')) {
            isPostageFree = 'Y';
        }
        console.log($('#isAnyReturn'));
        console.log($('#isSupportOverTimeReturn'));
        console.log($('#isSupportScore'));
        console.log($('#isSupportReturnScore').attr('checked'));
        console.log($('#isShowIndex').attr('checked'));
        console.log($('#activityPayType').val());

        if ($('#activityPayType').val() == 'PART_PAY') {

            if ($('#productPrice').val() == '') {
                alert('请输入定金价格');
                $('#productPrice').focus();
                return ''
            }
        }

        console.log(CKEDITOR.instances['textData'].getData())

        $.ajax({
            url: "updateSecKill",
            type: "POST",
            async: false,
            beforeSend:beforeSend,
            data: {
                activityId: $('#activityId').val(),
                productStoreIds: $('#productStoreId').val(),
                activityCode: $('#activityCode').val(),
                activityType: $('#activityType').val(),
                activityAuditStatus: $('#activityAuditStatus').val(),
                activityStatus: $('#activityStatus').val(),
                activityName: $('#activityName').val(),
                publishDate: $('#publishDate').val(),
                endDate: $('#endDate').val(),
                activityStartDate: $('#activityStartDate').val(),
                activityEndDate: $('#activityEndDate').val(),
                limitQuantity: $('#limitQuantity').val(),
                activityQuantity: $('#activityQuantity').val(),
                productId: $("input[name='productId']").val(),
                shipmentType: $('#shipmentType').val(),
                activityPayType: $('#activityPayType').val(),
                scoreValue: $('#scoreValue').val(),
                productPrice: $('#productPrice').val(),
                //virtualProductStartDate: $('#virtualProductStartDate').val(),
                //virtualProductEndDate: $('#virtualProductEndDate').val(),
                virtualProductStartDate: virProductStartDate,
                virtualProductEndDate: virProductEndDate,
                isAnyReturn: isAnyReturn,
                isSupportOverTimeReturn: isSupportOverTimeReturn,
                isSupportScore: isSupportScore,
                isSupportReturnScore: isSupportReturnScore,
                isShowIndex: isShowIndex,
                isPostageFree:isPostageFree,
                activityDesc: CKEDITOR.instances['textData'].getData(),
                productActivityPartyLevels: levels,
                productActivityAreas: areas

            },
            dataType: "json",
            success: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作成功！");
                $('#modal_msg').modal();
                //提示弹出框隐藏事件，隐藏后重新加载当前页面
                $('#modal_msg').on('hide.bs.modal', function () {
                    window.location.href = '<@ofbizUrl>findSecKill</@ofbizUrl>';
                })
            },
            error: function (data) {

                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("操作失败！");
                $('#modal_msg').modal();
                $('#modal_msg').on('hide.bs.modal', function () {
                    window.location.reload();
                })
            }
        })
    
    }

</script>
