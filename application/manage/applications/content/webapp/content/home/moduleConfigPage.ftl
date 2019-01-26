<script type="text/javascript" src="<@ofbizContentUrl>/images/vue/vue.min.js</@ofbizContentUrl>"></script>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<style type="text/css">
    .list-group {
        width: 60%;
    }

    .imgul .list-group-item {
        padding: 0;
        margin: 0px 0px 3px 0px;
        border: 0;
        min-height: 36px;
        background-color: #BCCFDC;
    }

    .list-group-item {
        border: 0;
    }

    .hidespan {
        position: absolute;
        top: 1px;
        right: 1px;
        cursor: pointer;
        background-color: grey;
        font-size: 18px;
        padding: 5px;
        color: white;
    }

    .glyphicon-arrow-up {
        background-color: grey;
        position: absolute;
        top: 1px;
        right: 55px;
        font-size: 25px;
        padding: 5px;
        cursor: pointer;
        color: white;
    }

    .glyphicon-arrow-down {
        background-color: grey;
        position: absolute;
        top: 1px;
        right: 100px;
        font-size: 25px;
        padding: 5px;
        cursor: pointer;
        color: white;
    }

    .list-group-item img {
        width: 100%;
    }

    .templateNameClass {
        position: absolute;
        top: 1px;
        left: 1px;
        cursor: pointer;
        font-size: 18px;
        padding: 5px;
        color: black;
    }
    .templateNameClass1{
        position: absolute;
        top: 1px;
        left: 1px;
        height: 28px;
        margin-top: 4px;
        margin-left: 2px;
        width: 150px;
    }
</style>

<div id="app" class="box box-info">

    <div class="text-center">
        <button onclick="saveTemplateContent()" class="btn btn-primary">保存并发布</button>
        <button type="button" onclick="javascript:history.go(-1)" class="btn">返回</button>
    </div>
    <hr/>

    <div class="row">
        <div class="col-sm-6">
            <ul class="list-group center-block imgul">
                <li class="list-group-item" v-for="item in templateContents">
                    <div  v-bind:sequenceNum="item.sequenceNum"  v-bind:isInEdit="item.isInEdit"></div>

                    <span v-show="item.isInEdit=='N'" v-bind:widgetName="item.widgetName"   class="templateNameClass"  v-bind:templateContentId="item.templateContentId" >
                        {{item.widgetName}}
                    </span>
                    <input autofocus="autofocus" v-show="item.isInEdit=='Y'"  class="templateNameClass1" v-bind:templateContentId="item.templateContentId"/>

                    <span v-if="item.isShow=='Y'"  class="hidespan" v-bind:templateContentId="item.templateContentId">
                        隐藏
                    </span>
                    <span v-else class="hidespan" v-bind:templateContentId="item.templateContentId">
                        使用
                    </span>
                    <span class="glyphicon glyphicon-arrow-up" aria-hidden="true" v-bind:templateContentId="item.templateContentId"></span>
                    <span class="glyphicon glyphicon-arrow-down" aria-hidden="true" v-bind:templateContentId="item.templateContentId"></span>
                    <img v-if="item.isShow=='Y'" :src="'/images/homemodule/'+item.widgetType+'.png'"/>
                </li>
            </ul>
        </div>
        <div class="col-sm-6">
            <ul class="list-group">
                <li class="list-group-item" v-for="item in templateContents">
                    <h3>{{item.widgetName}}</h3>
                    <h5>{{item.remark}}</h5>
                </li>
            </ul>
        </div>
    </div>

</div>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_add_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
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
</div>
<!-- 提示弹出框end -->

<script>
    var app = new Vue({
        el: '#app',
        data: {
            templateContents: []
        },
        // 在 `methods` 对象中定义方法
        created: function () {
            var templateId = ${parameters.templateId};
            $.ajax({
                url: "getTempleteContent",
                type: "POST",
                data: {
                    templateId: templateId
                },
                dataType: "json",
                success: function (data) {
                    if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                        $.tipLayer(data._ERROR_MESSAGE_);
                    } else {
                        //获取所有明细信息，进行排序
                        var recordList = data.recordList;
                        sortTemplateContent(recordList);
                        //所有的都是默认没有编辑的
                        for (var i = 0; i < recordList.length; i++) {
                            recordList[i].isInEdit = "N";
                        }
                        app.templateContents = recordList;
                    }
                },
                error: function (data) {
                    $.tipLayer("操作失败！");
                }
            });
        },
        methods:{

        }
    })

    //遍历排序模块
    function sortTemplateContent(recordList) {
        var recordLenth = recordList.length;
        for (var i = 0; i < recordLenth; i++) {
            for (var j = i + 1; j < recordLenth; j++) {
                if (parseInt(recordList[i].sequenceNum) > parseInt(recordList[j].sequenceNum)) {
                    //互换
                    var temp = recordList[i];
                    recordList[i] = recordList[j];
                    recordList[j] = temp;
                }
            }
        }
    }

    //点击修改模块名称
    $(document).on("click", '.templateNameClass', function (e) {
        var target = e.target;
        var templateContentId = $(target).attr("templateContentId");
        var widgetName= $(target).attr("widgetName");
        var recordList = app.templateContents;
        for (var i = 0; i < recordList.length; i++) {
            if (templateContentId == recordList[i].templateContentId) {
                recordList[i].isInEdit = "Y";
            }
        }
        //数据加载完成后获取了新的dom
        Vue.nextTick(function () {
            $("input[templatecontentid='"+templateContentId+"']").eq(0).val(widgetName)
            $("input[templatecontentid='"+templateContentId+"']").eq(0).trigger("select")
        })
    });

    $(document).on("blur", '.templateNameClass1', function (e) {
        var target = e.target;
        var templateContentId = $(target).attr("templateContentId");
        var editName = $("input[templatecontentid='"+templateContentId+"']").eq(0).val();
        var recordList = app.templateContents;
        for (var i = 0; i < recordList.length; i++) {
            if (templateContentId == recordList[i].templateContentId) {
                recordList[i].isInEdit = "N";
                recordList[i].widgetName= editName;
            }
        }
    });


    //上移
    $(document).on("click", '.glyphicon-arrow-up', function (e) {
        var span = e.target;
        var templateContentId = $(span).attr("templateContentId");
        var recordList = app.templateContents;
        //如果是第一个就不能上移动了。
        if (templateContentId == recordList[0].templateContentId) {
            $.tipLayer("不能上移了！");
            return;
        }
        for (var i = 0; i < recordList.length; i++) {
            if (templateContentId == recordList[i].templateContentId) {
                //互换两个的sequenceNum
                var temp = recordList[i].sequenceNum;
                recordList[i].sequenceNum = recordList[i - 1].sequenceNum
                recordList[i - 1].sequenceNum = temp;
            }
        }
        sortTemplateContent(recordList)
    });
    //下移
    $(document).on("click", '.glyphicon-arrow-down', function (e) {

        var span = e.target;
        var templateContentId = $(span).attr("templateContentId");
        var recordList = app.templateContents;
        var recordLength = recordList.length;
        //如果是第一个就不能上移动了。
        if (templateContentId == recordList[recordLength - 1].templateContentId) {
            $.tipLayer("不能下移了！");
            return;
        }
        for (var i = 0; i < recordList.length; i++) {
            if (templateContentId == recordList[i].templateContentId) {
                //互换两个的sequenceNum
                var temp = recordList[i].sequenceNum;
                recordList[i].sequenceNum = recordList[i + 1].sequenceNum
                recordList[i + 1].sequenceNum = temp;
            }
        }
        sortTemplateContent(recordList)
    });

    //点击隐藏或者显示
    $(document).on("click", '.hidespan', function (e) {
        var span = e.target;
        var spantext = $(span).text().trim();
        var templateContentId = $(span).attr("templateContentId")
        if (spantext == "隐藏") {
            //隐藏图片，并且发送后台
            //修改vuedata
            changeContentStatus("N", templateContentId);
        } else {
            changeContentStatus("Y", templateContentId);
        }
    });


    function changeContentStatus(isUsed, templateContentId) {
        var recordList = app.templateContents;
        for (var i = 0; i < recordList.length; i++) {
            if (recordList[i].templateContentId == templateContentId) {
                recordList[i].isShow = isUsed;
            }
        }
    }

    //保存
    function saveTemplateContent() {
        var recordList = app.templateContents;
        $.ajax({
            url: "saveTemplateContent",
            type: "POST",
            data: {
                recordList: JSON.stringify(recordList)
            },
            dataType: "json",
            success: function (data) {
                if (data.hasOwnProperty("_ERROR_MESSAGE_")) {
                    $.tipLayer(data._ERROR_MESSAGE_);
                } else {
                    $('#modal_msg #modal_msg_body').html("保存成功！");
                    $('#modal_msg').modal();
                }
            },
            error: function (data) {
                $.tipLayer("操作失败！");
            }
        });

    }


</script>
