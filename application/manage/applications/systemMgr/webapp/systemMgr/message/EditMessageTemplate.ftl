<#if messageTemplateDTO?exists>
<div class="box box-info">
    <div class="box-header with-border">
        <h3 class="box-title">基本信息</h3>
    </div>
    <div class="box-body">
        <form class="form-horizontal" method="post"  role="form" action="editMessageTemplate" name="editDataForm"  id="editDataForm"  class="" >
            <input type="hidden" name="operateType" value="${operateType}">
            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>模板编号:</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control" id="messageTemplateId" name="messageTemplateId" value="${messageTemplateDTO.messageTemplateId?if_exists}" readonly>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>是否启用:</label>
                    <div class="col-sm-9">
                        <label class="col-sm-4"><input name="isUse" type="radio" ${(messageTemplateDTO.isUse=='0')?string('checked','')} value="0">是</label>
                        <label class="col-sm-4"><input name="isUse" type="radio" ${(messageTemplateDTO.isUse=='1')?string('checked','')} value="1">否</label>
                        <label class="col-sm-4"></label>
                    </div>
                </div>
            </div>

            <div class="row title">
                <div class="form-group col-sm-6" data-type="required" data-mark="模板类型">
                    <label class="col-sm-3 control-label"><i class="required-mark">*</i>模板类型:</label>
                    <div class="col-sm-9">
                    <#--<div class="col-sm-3" style="padding-left: 0px;">
                        <select class="form-control" name="stateProvinceGeoId" id="BusinessAddForm_stateProvinceGeoId">
                            <option value=""></option>
                        </select>
                    </div>-->
                        <select class="form-control" name="templateType" id="dataForm_templateType" >
                            <option ${(messageTemplateDTO.templateType=='0')?string('selected','')} value="0">订单类</option>
                            <option ${(messageTemplateDTO.templateType=='1')?string('selected','')} value="1">商品类</option>
                            <option ${(messageTemplateDTO.templateType=='2')?string('selected','')} value="2">活动类</option>
                        </select>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6"  data-type="required" data-mark="模板名称">
                    <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>模板名称:</label>
                    <div class="col-sm-9">
                        <input type="text" class="form-control dp-vd" id="templateName" name="templateName" value="${messageTemplateDTO.templateName?if_exists}" >
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label for="number" class="col-sm-3 control-label"><i class="required-mark">*</i>请选择发送方式:</label>
                    <div class="col-sm-9">
                        <div class="radio col-sm-9">
                            <label class="col-sm-4"><input name="sendMode" type="radio"  ${(messageTemplateDTO.sendMode=='0')?string('checked','')} value="0">短信</label>
                            <label class="col-sm-4"><input name="sendMode" type="radio"  ${(messageTemplateDTO.sendMode=='1')?string('checked','')} value="1">app消息</label>
                            <label class="col-sm-4"><input name="sendMode" type="radio"  ${(messageTemplateDTO.sendMode=='2')?string('checked','')} value="2">pc消息</label>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6" data-type="required" data-mark="消息模板内容">
                    <label class="col-sm-3 control-label">消息模板内容:</label>
                    <div class="col-sm-9" >
                        <div style="padding-left: 0px;">
                            <textarea id="templateContent" name="templateContent" class="form-control  dp-vd" rows="3" style="resize: none;">${messageTemplateDTO.templateContent!''}</textarea>
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-9">
                    <label class="col-sm-2 control-label">插入变量:</label>
                    <div class="col-sm-10 sm_check_show" >
                        <button type="button" onclick="inserVar('{会员名称}')" class="btn  btn-success m-r-6">会员名称</button>
                        <button id="bt1" type="button" onclick="inserVar('{订单编号}')" class="btn btn-success m-r-6">订单编号</button>
                        <button id="bt3" type="button" onclick="inserVar('{商品名称}')" class="btn btn-success m-r-6">商品名称</button>
                        <button id="bt4" type="button" onclick="inserVar('{活动名称}')" class="btn btn-success m-r-6">活动名称</button>
                        <button  type="button" onclick="inserVar('{订单日期}')" class="btn btn-success m-r-6">订单日期</button>
                        <button  type="button" onclick="inserVar('{爱车名称}')" class="btn btn-success m-r-6">爱车名称</button>
                    </div>
                    <div class="col-sm-9 pcapp_check_show hidden">
                        <button  type="button" onclick="inserVar('{爱车名称}')" class="btn btn-success m-r-8">爱车名称</button>
                        <button  type="button" onclick="inserVar('{订单编号}')" class="btn btn-success m-r-8">订单编号</button>
                        <button  type="button" onclick="inserVar('{商品名称}')" class="btn btn-success m-r-8">商品名称</button>
                        <button  type="button" onclick="inserVar('{活动名称}')" class="btn btn-success m-r-8">活动名称</button>

                    </div>
                </div>
            </div>

            <div class="row">
                <div class="form-group col-sm-6">
                    <label class="col-sm-3 control-label">链接地址:</label>
                    <div class="col-sm-9">
                        <div style="overflow:hidden;margin-bottom: 10px;">
                            <#assign fltList = Static["com.qihua.ofbiz.systemMgr.MessageTemplateServices"].getEnumByTypeId(delegator,"FIRST_LINK_TYPE")>
                            <select id="firstLinkType" name="firstLinkType" class="form-control" style="width:150px;float: left;margin-right: 20px;">
                                <option value="">====请选择====</option>
                                <#if fltList?has_content && (fltList?size > 0)>
                                    <#list fltList as flt>
                                        <#if messageTemplateDTO.firstLinkType?if_exists ==flt.enumId>
                                            <option value="${flt.enumId}" selected>${flt.description}</option>
                                        <#else>
                                            <#if flt.enumId != 'FLT_GNLJ'>
                                                <option value="${flt.enumId}">${flt.description}</option>
                                            </#if>
                                        </#if>
                                    </#list>
                                </#if>
                            </select>

                            <#--<#assign sltList = Static["com.qihua.ofbiz.banner.BannerServices"].getEnumByTypeId(delegator,"SECOND_LINK_TYPE")>
                            <select id="secondLinkType" class="form-control" style="width:150px">
                                <option value="">====请选择====</option>
                                <#if sltList?has_content && (sltList?size > 0)>
                                    <#list sltList as slt>
                                        <#if parameters.secondLinkType?if_exists == slt.enumId>
                                            <option value="${slt.enumId}" selected>${slt.description}</option>
                                        <#else>
                                            <option value="${slt.enumId}">${slt.description}</option>
                                        </#if>
                                    </#list>
                                </#if>
                            </select>-->
                        </div>

                        <div id="selectViewDiv" style="margin-top: 10px;display:none;"  >
                            <span>已选择:</span>
                            <span id="selectName" style="margin-left: 10px;color: blue;cursor: pointer;">${messageTemplateDTO.linkName?if_exists}</span>
                            <input name="linkName" value="" id="linkName">
                            <input type="hidden" id="linkId" name="linkId" value="${messageTemplateDTO.linkId?if_exists}" />
                        </div>
                        <div id="linkDiv" class="col-sm-5" style="margin-top: 10px;padding-left: 0px;display:none;">
                            <input type="text" class="form-control" id="linkUrl" name="linkUrl" value="${messageTemplateDTO.linkUrl?if_exists}"/>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row sm_check_show">
                <div class="form-group col-sm-6" data-type="max" data-mark="短信签名" data-number="4">
                    <label class="col-sm-3 control-label">短信签名:</label>
                    <div class="col-sm-9" >
                        <input type="text" class="form-control dp-vd" id="messageSignature" name="messageSignature" value="${messageTemplateDTO.messageSignature!''}">
                        <p class="dp-error-msg"></p>
                    </div>
                </div>
            </div>

            <div class="row sm_check_show">
                <div class="form-group col-sm-6">
                    <label class="col-sm-3 control-label"></label>
                    <div class="col-sm-9" >
                        <label>1条短信67字，超过按多条计算，您已输入9个字（含签名退订N),预扣1条短信，以实际数字为准</label>
                    </div>
                </div>
            </div>
            <div class="row sm_check_show">
                <div class="form-group col-sm-6">
                    <label class="col-sm-3 control-label">发送时间:</label>
                    <div class="col-sm-9" >
                        <div class="radio">
                            <label class="col-sm-6"><input name="" type="radio" checked value="0">实时发送</label>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 按钮组 -->
            <div class="box-footer text-center">
                <button id="save" type="button" class="btn btn-primary m-r-10">保存</button>
            </div>
        </form>
    </div><!-- /.box-body -->
</div>
</#if>

<script type="text/javascript">
    function messageTemplate(){
        this.init= function(){
            $('#save').click(function(){
                $("#editDataForm").dpValidate({
                    clear: true
                });
                $("#editDataForm").submit();
            });
            // 数据校验
            $("#editDataForm").dpValidate({
                validate:true,
                callback:function(){
                    $("#linkName").val($("#selectName").html());
                    $.ajax({
                        url: "editMessageTemplate",
                        type: "POST",
                        data :$("#editDataForm").serialize() ,
                        dataType : "json",
                        success: function(data){
                            if(!data._ERROR_MESSAGE_){
                                $.tipLayer("操作成功！");
                                window.location = '<@ofbizUrl>messageTemplateList</@ofbizUrl>';
                            }else{
                                $.tipLayer("操作失败！");
                            }
                        },
                        error: function(data){
                            $.tipLayer("操作失败！");
                        }
                    });
                }
            }) ;

            //链接地址一的选项切换事件
            $('#firstLinkType').on('change',function(){
                switch ($(this).val()) {
                    case 'FLT_GNLJ': // 功能链接
                    {
                        $('#linkId').val('');
                        $('#selectName').html('');
                        $('#linkUrl').val('');
                        $('#secondLinkType').val('');
                        $('#secondLinkType').show();
                        $('#linkDiv').hide();
                    }
                        break;
                    case 'FLT_SPLJ':  // 商品链接
                    {
                        $('#secondLinkType').hide();
                        $('#linkDiv').hide();
                        $.dataSelectModal({
                            url: "/catalog/control/ProductListModalPage?externalLoginKey=${externalLoginKey}",
                            width:	"800",
                            title:	"选择商品",
                            selectId: "linkId",
                            selectName:	"selectName",
                            selectCallBack: function(el){
                                $('#selectViewDiv').show();
                                $('#linkUrl').val('modalName=SP&id='+el.data('id'));
                            }
                        });
                    }
                        break;
                    case 'FLT_WZLJ':  // 文章链接
	                {
	                    $('#secondLinkType').hide();
	                    $('#linkDiv').hide();
	                    $.dataSelectModal({
		    				url: "/content/control/ArticleListModalPage?externalLoginKey=${externalLoginKey}",
		    				width:	"800",
		    				title:	"选择文章",
		    				selectId: "linkId",
		    				selectName:	"selectName",
		    				selectCallBack: function(el){
		    					$('#selectViewDiv').show();
		    					$('#linkUrl').val('modalName=WZ&id='+el.data('id'));
		    				}
		    			});
	                }
	                    break;
                    case 'FLT_HDLJ': // 活动链接
                    {
                        $('#secondLinkType').hide();
                        $('#linkDiv').hide();
                        $.dataSelectModal({
                            url: "/prodPromo/control/ProActivityMgrListModalPage?externalLoginKey=${externalLoginKey}",
                            width:	"800",
                            title:	"选择活动",
                            selectId: "linkId",
                            selectName:	"selectName",
                            selectCallBack: function(el){
                                $('#selectViewDiv').show();
                                $('#linkUrl').val('modalName=HD&id='+el.data('id'));
                            }
                        });
                    }
                        break;
                    case 'FLT_CXLJ': // 促销链接
	                {
	                    $('#secondLinkType').hide();
	                    $('#linkDiv').hide();
	                    $.dataSelectModal({
	                        url: "/prodPromo/control/PromoListModalPage?externalLoginKey=${externalLoginKey}",
	                        width:	"800",
	                        title:	"选择促销",
	                        selectId: "linkId",
	                        selectName:	"selectName",
	                        selectCallBack: function(el){
	                            $('#selectViewDiv').show();
	                            $('#linkUrl').val('modalName=HD&id='+el.data('id'));
	                        }
	                    });
	                }
	                    break;
                    case 'FLT_DDLJ':
                    {
                        $('#secondLinkType').hide();
                        $('#linkDiv').hide();
                        $('#linkUrl').val('modalName=HD&orderId={orderId}');
                        inserVar("<@ofbizContentUrl>{DD_RUL}?orderId={orderId}&amp;modalName=HD</@ofbizContentUrl>") ;
                    }
                        break;
                    case 'FLT_DDLJ':
                    {
                        $('#secondLinkType').hide();
                        $('#linkDiv').hide();
                    }
                        break;
                    case 'FLT_ZDYLJ':
                    {
                        $('#secondLinkType').hide();
                        $('#selectViewDiv').hide();
                        $('#linkId').val('');
                        $('#selectName').html('');
                        $('#linkUrl').val('');
                        $('#linkDiv').show();
                    }
                        break;
                }
            });

            //链接地址二的选项切换事件
            $('#secondLinkType').on('change',function(){
                switch ($(this).val()) {
                    case 'SLT_CX':
                    {
                        $.dataSelectModal({
                            url: "/prodPromo/control/PromoListModalPage?externalLoginKey=${externalLoginKey}",
                            width:	"800",
                            title:	"选择促销",
                            selectId: "linkId",
                            selectName:	"selectName",
                            selectCallBack: function(el){
                                $('#selectViewDiv').show();
                                var modalName = el.data('record').activityType;
                                $('#linkUrl').val('modalName='+modalName+'&id='+el.data('id'));
                            }
                        });
                    }
                        break;
                    case 'SLT_YHQ':
                    {
                        $('#linkId').val('');
                        $('#selectName').html('');
                        $('#linkUrl').val('');
                        alert("暂未无此功能！");
                    }
                        break;
                    default:
                        $('#linkId').val('');
                        $('#selectName').html('');
                        $('#linkUrl').val('');
                        break;
                }
            });
            this.setSendMode();
        }
        this.setSendMode=function(){
            // 发送方式选择短信时显示 短信签名
            $("input[name='sendMode']").on('change',function(){
                if(0==$(this).val()){
                    $(".pcapp_check_show").addClass("hidden");
                    $(".sm_check_show").show();
                    $("#messageSignature").addClass("dp-vd") ;
                }else{
                    $("#messageSignature").removeClass("dp-vd") ;
                    $(".pcapp_check_show").removeClass("hidden");
                    $(".sm_check_show").hide();
                }
            });
            if($("input[name='sendMode']:checked").val()!=0){
                $("#messageSignature").removeClass("dp-vd") ;
                $(".pcapp_check_show").removeClass("hidden");
                $(".sm_check_show").hide();
            }
        }
    }
    function insertText(obj,str) {
        if (typeof obj.selectionStart === 'number' && typeof obj.selectionEnd === 'number') {
            var startPos = obj.selectionStart,
                    endPos = obj.selectionEnd,
                    cursorPos = startPos,
                    tmpStr = obj.value;
            obj.value = tmpStr.substring(0, startPos) + str + tmpStr.substring(endPos, tmpStr.length);
            cursorPos += str.length;
            obj.selectionStart = obj.selectionEnd = cursorPos;
        } else {
            obj.value += str;
        }
    }
    function inserVar(content){
        var t = document.getElementById("templateContent");
        insertText(t,content);
    }

    $(function(){
        var message_ = new messageTemplate() ;
        message_.init() ;
    })
</script>
