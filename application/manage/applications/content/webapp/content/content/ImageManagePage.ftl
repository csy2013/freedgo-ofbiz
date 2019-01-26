<link rel="stylesheet" href="<@ofbizContentUrl>/images/themes/adminlet/js/plugins/iCheck/all.css</@ofbizContentUrl>" type="text/css"/>
<#assign externalKeyParam = "?externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>
<style>
    .cell > p > a {
        color: rgb(102, 102, 102);
        text-decoration: none;
    }

    .cell > p > a:hover {
        color: rgb(0, 0, 0);
        text-decoration: none;
    }

    img {
        border: 0px currentColor;
    }

    #waterfall {
        margin: 0px auto;
        text-align: center;
    }

    #waterfall img {
        width: 203px;
    }

    #waterfall .cell {
        background: rgb(255, 255, 255);
        padding: 10px 0px 5px;
        border: 1px solid rgb(227, 227, 227);
        width: 223px;
        margin-top: 20px;
        box-shadow: 0px 2px 3px 1px #ccc;
    }

    #waterfall .cell:hover {
        box-shadow: 0px 2px 3px 1px #999;
    }

    .box-left {
        width: 150px;
        height: 520px;
        background-color: #fff;
        border: 1px solid #ccc;
        float: left;
    }

    .first-menu {
        padding-left: 10px;
        padding-top: 10px;
        list-style: none;
    }

    .first-menu > li > a {
        display: block;
        color: #666;
        cursor: pointer;
        border-right: 3px solid transparent;
    }

    .first-menu > li > a:hover {
        color: #3c8dbc;
        border-right-color: #3c8dbc;
    }

    .first-menu > li.active > a {
        color: #3c8dbc;
        border-right-color: #3c8dbc;
    }

    .second-menu {
        padding-left: 0px;
        list-style: none;
    }

    .second-menu > li {
        position: relative;
        display: block;
    }

    .second-menu > li > a {
        position: relative;
        display: block;
        padding-left: 10px;
        padding-top: 5px;
        color: #666;
        cursor: pointer;
        border-right: 3px solid transparent;
    }

    .second-menu > li.active > a {
        color: #3c8dbc;
        border-right-color: #3c8dbc;
    }

    .second-menu > li > a:hover {
        color: #3c8dbc;
        border-right-color: #3c8dbc;
    }

    .box-right {
        height: 520px;
        background-color: #fff;
        border-top: 1px solid #ccc;
        border-right: 1px solid #ccc;
        border-bottom: 1px solid #ccc;
    }

    .third-menu {
        padding-left: 0px;
        list-style: none;
        display: none;
    }

    .third-menu > li {
        position: relative;
        display: block;
    }

    .third-menu > li > a {
        position: relative;
        display: block;
        padding-left: 20px;
        padding-top: 5px;
        color: #666;
        cursor: pointer;
        border-right: 3px solid transparent;
    }
</style>

<SCRIPT>
    ;(function ($) {
        var
                //参数
                setting = {
                    column_width: 240,//列宽
                    column_className: 'waterfall_column',//列的类名
                    column_space: 10,//列间距
                    cell_selector: '.cell',//要排列的砖块的选择器，context为整个外部容器
                    img_selector: 'img',//要加载的图片的选择器
                    auto_imgHeight: true,//是否需要自动计算图片的高度
                    fadein: true,//是否渐显载入
                    fadein_speed: 600,//渐显速率，单位毫秒
                    insert_type: 2, //单元格插入方式，1为插入最短那列，2为按序轮流插入
                    getResource: function (index) {
                    }  //获取动态资源函数,必须返回一个砖块元素集合,传入参数为加载的次数
                },
                //
                waterfall = $.waterfall = {},//对外信息对象
                $waterfall = null;//容器
        waterfall.load_index = 0, //加载次数
                $.fn.extend({
                    waterfall: function (opt) {
                        opt = opt || {};
                        setting = $.extend(setting, opt);
                        $waterfall = waterfall.$waterfall = $(this);
                        waterfall.$columns = creatColumn();
                        render($(this).find(setting.cell_selector).detach(), false); //重排已存在元素时强制不渐显
                        waterfall._scrollTimer2 = null;
                        $('.img-area').bind('scroll', function () {
                            clearTimeout(waterfall._scrollTimer2);
                            waterfall._scrollTimer2 = setTimeout(onScroll, 300);
                        });
                        waterfall._scrollTimer3 = null;
                        $(window).bind('resize', function () {
                            clearTimeout(waterfall._scrollTimer3);
                            waterfall._scrollTimer3 = setTimeout(onResize, 300);
                        });
                    }
                });

        function creatColumn() {//创建列
            waterfall.column_num = calculateColumns();//列数
            //循环创建列
            var html = '';
            for (var i = 0; i < waterfall.column_num; i++) {
                html += '<div class="' + setting.column_className + '" style="width:' + setting.column_width + 'px; display:inline-block; *display:inline;zoom:1; margin-left:' + setting.column_space / 2 + 'px;margin-right:' + setting.column_space / 2 + 'px; vertical-align:top; overflow:hidden"></div>';
            }
            $waterfall.prepend(html);//插入列
            return $('.' + setting.column_className, $waterfall);//列集合
        }

        function calculateColumns() {//计算需要的列数
            var num = Math.floor(($waterfall.innerWidth()) / (setting.column_width + setting.column_space));
            if (num < 1) {
                num = 1;
            } //保证至少有一列
            return num;
        }

        function render(elements, fadein) {//渲染元素
            if (!$(elements).length) return;//没有元素
            var $columns = waterfall.$columns;
            $(elements).each(function (i) {
                if (!setting.auto_imgHeight || setting.insert_type == 2) {//如果给出了图片高度，或者是按顺序插入，则不必等图片加载完就能计算列的高度了
                    if (setting.insert_type == 1) {
                        insert($(elements).eq(i), setting.fadein && fadein);//插入元素
                    } else if (setting.insert_type == 2) {
                        insert2($(elements).eq(i), i, setting.fadein && fadein);//插入元素
                    }
                    return true;//continue
                }
                if ($(this)[0].nodeName.toLowerCase() == 'img' || $(this).find(setting.img_selector).length > 0) {//本身是图片或含有图片
                    var image = new Image;
                    var src = $(this)[0].nodeName.toLowerCase() == 'img' ? $(this).attr('src') : $(this).find(setting.img_selector).attr('src');
                    image.onload = function () {//图片加载后才能自动计算出尺寸
                        image.onreadystatechange = null;
                        if (setting.insert_type == 1) {
                            insert($(elements).eq(i), setting.fadein && fadein);//插入元素
                        } else if (setting.insert_type == 2) {
                            insert2($(elements).eq(i), i, setting.fadein && fadein);//插入元素
                        }
                        image = null;
                    }
                    image.onreadystatechange = function () {//处理IE等浏览器的缓存问题：图片缓存后不会再触发onload事件
                        if (image.readyState == "complete") {
                            image.onload = null;
                            if (setting.insert_type == 1) {
                                insert($(elements).eq(i), setting.fadein && fadein);//插入元素
                            } else if (setting.insert_type == 2) {
                                insert2($(elements).eq(i), i, setting.fadein && fadein);//插入元素
                            }
                            image = null;
                        }
                    }
                    image.src = src;
                } else {//不用考虑图片加载
                    if (setting.insert_type == 1) {
                        insert($(elements).eq(i), setting.fadein && fadein);//插入元素
                    } else if (setting.insert_type == 2) {
                        insert2($(elements).eq(i), i, setting.fadein && fadein);//插入元素
                    }
                }
            });
        }

        function public_render(elems) {//ajax得到元素的渲染接口
            render(elems, true);
        }

        function insert($element, fadein) {//把元素插入最短列
            if (fadein) {//渐显
                $element.css('opacity', 0).appendTo(waterfall.$columns.eq(calculateLowest())).fadeTo(setting.fadein_speed, 1);
            } else {//不渐显
                $element.appendTo(waterfall.$columns.eq(calculateLowest()));
            }
        }

        function insert2($element, i, fadein) {//按序轮流插入元素
            if (fadein) {//渐显
                $element.css('opacity', 0).appendTo(waterfall.$columns.eq(i % waterfall.column_num)).fadeTo(setting.fadein_speed, 1);
            } else {//不渐显
                $element.appendTo(waterfall.$columns.eq(i % waterfall.column_num));
            }
        }

        function calculateLowest() {//计算最短的那列的索引
            var min = waterfall.$columns.eq(0).outerHeight(), min_key = 0;
            waterfall.$columns.each(function (i) {
                if ($(this).outerHeight() < min) {
                    min = $(this).outerHeight();
                    min_key = i;
                }
            });
            return min_key;
        }

        function getElements() {//获取资源
            $.waterfall.load_index++;
            return setting.getResource($.waterfall.load_index, public_render);
        }

        waterfall._scrollTimer = null;//延迟滚动加载计时器
        function onScroll() {//滚动加载
            clearTimeout(waterfall._scrollTimer);
            waterfall._scrollTimer = setTimeout(function () {
                var $lowest_column = waterfall.$columns.eq(calculateLowest());//最短列
                var bottom = $lowest_column.offset().top + $lowest_column.outerHeight();//最短列底部距离浏览器窗口顶部的距离
                var scrollTop = document.documentElement.scrollTop || document.body.scrollTop || 0;//滚动条距离
                var windowHeight = document.documentElement.clientHeight || document.body.clientHeight || 0;//窗口高度
                if (scrollTop >= bottom - windowHeight) {
                    render(getElements(), true);
                }
            }, 100);
        }

        function onResize() {//窗口缩放时重新排列
            if (calculateColumns() == waterfall.column_num) return; //列数未改变，不需要重排
            var $cells = waterfall.$waterfall.find(setting.cell_selector);
            waterfall.$columns.remove();
            waterfall.$columns = creatColumn();
            render($cells, false); //重排已有元素时强制不渐显
        }
    })(jQuery);
</SCRIPT>

<div class="box box-info">
    <div class="box-body">
        <div class="box-left">
            <div style="height:450px;overflow-y: auto;overflow-x: hidden;">
                <ul class="first-menu">
                    <li class="active">
                        <a href="javascript:;">
                            全部
                            <i class="glyphicon glyphicon-chevron-down"></i>
                        </a>
                        <ul class="second-menu">

                        </ul>
                    </li>
                </ul>
            </div>
            <div style="text-align: center;border-top: 1px solid #ccc;padding: 15px;height: 70px;">
                <button id="btn_addImgGroup" class="btn btn-primary">
                    <i class="fa fa-plus"></i> 新建分组
                </button>

            </div>
        </div>

        <div class="box-right js-checkparent">
            <div class="row" style="overflow-x: hidden;">
                <div class="col-sm-12 m-5">
                    <div class="dp-tables_btn">
                        <div class="icheckbox_minimal-blue js-allcheck" style="position: relative;"></div>
                        全选

                        <!--是否有新增的权限-->
                        <button id="btn_add" class="btn btn-primary">
                            <i class="fa fa-plus"></i> 添加图片
                        </button>

                        <!--是否有移动分组的权限-->
                        <button id="btn_change" class="btn btn-primary">
                            <i class="fa fa-exchange"></i> 移动分组
                        </button>

                        <!--是否有删除的权限-->
                        <button id="btn_del" class="btn btn-primary btn_del">
                            <i class="fa fa-trash"></i> ${uiLabelMap.Delete}
                        </button>
                    </div>
                </div><!-- 操作按钮组end -->
            </div><!-- 工具栏end -->


            <!-- 图片区域start -->
            <div class="row img-area" style="border-top: 1px solid #ccc;overflow-x: hidden;height:473px">
                <div class="col-sm-12">
                    <DIV id="waterfall">
                    </DIV>
                </div>
            </div><!-- 表格区域end -->
        </div>
    </div>
</div>

<!-- 提示弹出框start -->
<div id="modal_msg" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_msg_title">
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

<!-- 新建图片分组弹出框start -->
<div id="modal_addImgGroup" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_addImgGroup_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">新建图片分组</h4>
            </div>
            <div class="modal-body">
                <form id="AddImgGroupForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>AddImgGroup</@ofbizUrl>">
                    <div class="form-group" data-type="required" data-mark="分组名称">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>分组名称:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="imgGroupName" name="imgGroupName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">保存</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 新建图片分组弹出框 end -->

<!-- 修改图片分组弹出框start -->
<div id="modal_editImgGroup" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_editImgGroup_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">图片分组重命名</h4>
            </div>
            <div class="modal-body">
                <form id="EditImgGroupForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>EditImgGroup</@ofbizUrl>">
                    <input type="hidden" id="imgGroupId" name="imgGroupId"/>
                    <div class="form-group" data-type="required" data-mark="分组名称">
                        <label class="control-label col-sm-2"><i class="required-mark">*</i>分组名称:</label>
                        <div class="col-sm-10">
                            <input type="text" class="form-control dp-vd" id="imgGroupName" name="imgGroupName">
                            <p class="dp-error-msg"></p>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">保存</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 修改图片分组弹出框 end -->

<!-- 分组删除确认弹出框start -->
<div id="modal_confirm_imgGroup" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_title">删除提示</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 分组删除确认弹出框end -->

<!-- 图片删除确认弹出框start -->
<div id="modal_confirm_img" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="modal_confirm_img_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="modal_confirm_img_title">删除提示</h4>
            </div>
            <div class="modal-body">
                <h4 id="modal_confirm_body"></h4>
            </div>
            <div class="modal-footer">
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
                <button id="ok" type="button" class="btn btn-primary" data-dismiss="modal">确定</button>
            </div>
        </div>
    </div>
</div><!-- 图片删除确认弹出框end -->

<!-- 移动分组弹出框start -->
<div id="modal_changeGroup" class="modal fade " tabindex="-1" role="dialog" aria-labelledby="modal_changeGroup_title">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title">移动分组</h4>
            </div>
            <div class="modal-body">
                <form id="ChangeGroupForm" method="post" class="form-horizontal" role="form" action="<@ofbizUrl>ChangeGroupForm</@ofbizUrl>">
                    <div class="form-group" data-type="required" data-mark="分组名称">
                        <label class="control-label col-sm-4"><i class="required-mark">*</i>选择图片分组:</label>
                        <div class="col-sm-6">
                            <select id="groupId" class="form-control">
                            </select>
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="save" type="button" class="btn btn-primary">保存</button>
                <button id="cancel" type="button" class="btn btn-default" data-dismiss="modal">取消</button>
            </div>
        </div>
    </div>
</div><!-- 移动分组弹出框 end -->

<SCRIPT>
    var opt = {
        getResource: function (index, render) {//index为已加载次数,render为渲染接口函数,接受一个dom集合或jquery对象作为参数。通过ajax等异步方法得到的数据可以传入该接口进行渲染，如 render(elem)
            var html = '';
            var groupId = "";
            if (cur_group_id != "not_group") {
                groupId = cur_group_id;
            }
            //异步加载未分组的图片数据
            $.ajax({
                url: _url,
                type: "GET",
                dataType: "json",
                async: false,
                data: {
                    VIEW_INDEX: index,
                    VIEW_SIZE: 20,
                    groupId: groupId
                },
                success: function (data) {
                    //已有的图片分组
                    $.each(data.recordsList, function (i, v) {
                        var conentId = v.CONTENT_ID,
                                createDate = timeStamp2String(v.CREATED_STAMP.time),
                                objectInfo = v.OBJECT_INFO;

                        var img_div = "<DIV id='" + conentId + "' class='cell'>" +
                                "<A href='javasript:void(0)'>" +
                                "<IMG src='" + objectInfo + "'>" +
                                "</A>" +
                                "<P>" +
                                "<div class='icheckbox_minimal-blue js-checkchild' style='position: relative;'></div>" +
                                "<A href='javasript:void(0)'>" + createDate + "</A>" +
                                "</P>" +
                                "<p class='img-btn' style='text-align: left;'>" +
                                "<span style='padding-left: 50px;'>" +
                                "<i class='fa fa-exchange' style='font-size: 20px;color:#C1C1C1;cursor: pointer;' title='移动分组' onclick='changeGroup(\"" + conentId + "\")'></i>" +
                                "</span>" +
                                "<span style='padding-left: 90px;'>" +
                                "<i class='fa fa-trash' style='font-size: 20px;color: #C1C1C1;cursor: pointer;' title='删除' onclick='delImg(\"" + conentId + "\")'></i>" +
                                "</span>" +
                                "</p>" +
                                "</DIV>";
                        html += img_div;
                    });
                },
                error: function (data) {
                    $.tipLayer("网络异常！");
                }
            });
            return $(html);
        },
        auto_imgHeight: true,
        insert_type: 2
    }
</SCRIPT>

<script>
    var ids;
    var cur_group_id;	//当前选择的分组ID
    $(function () {
        // 初始化图片选择
        $.chooseImage.int({
            userId: 'imgChoose',
            serverChooseNum: 1,
            getServerImgUrl: '/content/control/imagesmanage${externalKeyParam}',
            submitLocalImgUrl: '/content/control/uploadFile${externalKeyParam}',
            submitServerImgUrl: '',
            submitNetworkImgUrl: ''
        });

        //图片保存按钮事件
        $('body').on('click', '.img-submit-btn', function () {
            var obj = $.chooseImage.getImgData();
            if (obj.imgNameArray.length > 6) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("一次最多只能上传6张图片，请重新选择");
                $('#modal_msg').modal();
                $.chooseImage.hide();
                return false;
            }
            $.chooseImage.choose(obj, function (data) {
                // 获取选择图片的集合
                $.each(obj.imgNameArray, function (index, value) {
                    var contentId = "";
                    if (index == 0) {
                        contentId = data.uploadedFile0;
                    } else if (index == 1) {
                        contentId = data.uploadedFile1;
                    } else if (index == 2) {
                        contentId = data.uploadedFile2;
                    } else if (index == 3) {
                        contentId = data.uploadedFile3;
                    } else if (index == 4) {
                        contentId = data.uploadedFile4;
                    } else if (index == 5) {
                        contentId = data.uploadedFile5;
                    }
                    var group_id = "";
                    if (cur_group_id != "not_group") {
                        group_id = cur_group_id;
                        //异步保存图片数据
                        $.ajax({
                            url: "addImgToGroup",
                            type: "POST",
                            dataType: "json",
                            data: {
                                contentId: contentId,
                                groupId: group_id
                            },
                            async: false,
                            success: function (data) {
                                getImgList(_url, group_id);
                            },
                            error: function (data) {
                                $.tipLayer("网络异常！");
                            }
                        });
                    } else {
                        getImgList(_url, group_id);
                    }
                });
                // 移除全选状态
                $(".icheckbox_minimal-blue.js-allcheck").removeClass('checked');
            })
        });

        // 图片选择控件显示
        $('#btn_add').click(function () {
            $.chooseImage.show();
        });

        //异步加载图片分组数据
        $.ajax({
            url: "GetImgGroupList",
            type: "POST",
            dataType: "json",
            async: false,
            success: function (data) {
                //已有的图片分组
                $.each(data.imgGroupList, function (i, v) {
                    //左侧菜单
                    newImgGroupItem(v.imgGroupId, v.imgGroupName, v.imgCount);
                });

                //添加未分组项
                $('.second-menu').append("<li id='not_group' class='active'><a href='javascript:getImgList(\"getImgListNotInGroup\");'>未分组(<span class='imgCount'></span>)</a></li>");
                getImgList("getImgListNotInGroup");
            },
            error: function (data) {
                $.tipLayer("网络异常！");
            }
        });

        //复选框hover事件
        $(document).on('mouseover mouseout', '.icheckbox_minimal-blue', function (event) {
            if (event.type == "mouseover") {
                $(this).addClass('hover');
            } else if (event.type == "mouseout") {
                $(this).removeClass('hover');
            }
        });

        //复选框点击事情
        $(document).on('click', '.icheckbox_minimal-blue', function () {
            if ($(this).hasClass('checked')) {
                $(this).removeClass('checked');
                //全选/反选切换
                if ($(this).hasClass('js-allcheck')) {
                    var $parent = $(this).closest('.js-checkparent');
                    $parent.find('.js-checkchild').removeClass('checked');
                }
            } else {
                $(this).addClass('checked');
                //全选/反选切换
                if ($(this).hasClass('js-allcheck')) {
                    var $parent = $(this).closest('.js-checkparent');
                    $parent.find('.js-checkchild').addClass('checked');
                }
            }
        });

        //图片按钮组hover事件
        $(document).on('mouseover mouseout', '.img-btn i', function (event) {
            $(this).finish();
            if (event.type == "mouseover") {
                $(this).css({'color': '#777'}).animate({fontSize: "30px", "margin-left": "-5px"}, 250);
            } else if (event.type == "mouseout") {
                $(this).css({'color': '#C1C1C1'}).animate({fontSize: "20px", "margin-left": "-0px"}, 250);
            }
        });

        //左侧一级菜单的下拉图标点击事件
        $(".first-menu i").click(function (e) {
            if ($(this).hasClass('glyphicon-chevron-down')) {
                $(this).closest('a').siblings('ul').slideUp('normal');
                $(this).removeClass("glyphicon-chevron-down").addClass("glyphicon-chevron-up");
            } else {
                $(this).closest('a').siblings('ul').slideDown('normal');
                $(this).removeClass("glyphicon-chevron-up").addClass("glyphicon-chevron-down");
            }
        });

        //左侧一级菜单的文字点击事件
        $(".first-menu li a").click(function (e) {
            $('.first-menu').find('.active').removeClass('active');
            $(this).closest('li').addClass('active');
        });

        //左侧二级菜单的点击事件
        $(document).on("click", ".second-menu li", function (e) {
            var li_id = $(this).attr("id");
            cur_group_id = li_id;
            $('.first-menu').find('li.active').removeClass('active');
            $(this).addClass("active");
            $(this).siblings('li').find('.third-menu').slideUp('normal');
            $(this).find('.third-menu').slideDown('normal');
            // 移除全选状态
            $(".icheckbox_minimal-blue.js-allcheck").removeClass('checked');
        });

        //左侧三级菜单的hover事件
        $(document).on('mouseover mouseout', '.third-menu li', function () {
            $(this).find('i').finish();
            if (event.type == "mouseover") {
                for (i = 0; i < 99; i++) {
                    $(this).find('i').animate({'margin-left': '-10', 'margin-right': '10'}, 250);
                    $(this).find('i').animate({'margin-left': '0', 'margin-right': '0'}, 250);
                }
            }
        });

        //新建分组按钮点击事件
        $("#btn_addImgGroup").click(function () {
            //清空form
            clearForm($("#AddImgGroupForm"));

            $("#AddImgGroupForm").dpValidate({
                clear: true
            });

            $('#modal_addImgGroup').modal();
        });

        //新建分组弹出框保存按钮点击事件
        $('#modal_addImgGroup #save').click(function () {
            $("#AddImgGroupForm").dpValidate({
                clear: true
            });
            $('#AddImgGroupForm').submit();
        });

        //图片分组保存方法
        $('#AddImgGroupForm').dpValidate({
            validate: true,
            callback: function () {
                //异步调用修改方法
                $.ajax({
                    url: "AddImgGroup",
                    type: "POST",
                    data: $('#AddImgGroupForm').serialize(),
                    dataType: "json",
                    success: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_addImgGroup').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("保存成功！");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            newImgGroupItem(data.imgGroupId, data.imgGroupName, 0);
                        })
                    },
                    error: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_addImgGroup').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("保存失败！");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

        //修改图片分组弹出框保存按钮点击事件
        $('#modal_editImgGroup #save').click(function () {
            $("#EditImgGroupForm").dpValidate({
                clear: true
            });

            $('#EditImgGroupForm').submit();
        });

        //图片分组修改方法
        $('#EditImgGroupForm').dpValidate({
            validate: true,
            callback: function () {
                //异步调用修改方法
                $.ajax({
                    url: "EditImgGroup",
                    type: "POST",
                    data: $('#EditImgGroupForm').serialize(),
                    dataType: "json",
                    success: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_editImgGroup').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("保存成功！");
                        $('#modal_msg').modal();
                        //提示弹出框隐藏事件，隐藏后重新加载当前页面
                        $('#modal_msg').off('hide.bs.modal');
                        $('#modal_msg').on('hide.bs.modal', function () {
                            $('.second-menu').find('li#' + data.imgGroupId).find('span.imgGroupName').text(data.imgGroupName);
                        })
                    },
                    error: function (data) {
                        //隐藏修改弹出窗口
                        $('#modal_editImgGroup').modal('toggle');
                        //设置提示弹出框内容
                        $('#modal_msg #modal_msg_body').html("保存失败！");
                        $('#modal_msg').modal();
                    }
                });
            }
        });

        //图片分组删除弹出框删除按钮点击事件
        $('#modal_confirm_imgGroup #ok').click(function (e) {
            //异步调用删除方法
            $.ajax({
                url: "DelImgGroup",
                type: "POST",
                data: {imgGroupId: del_imgGroupId},
                dataType: "json",
                success: function (data) {
                    //隐藏修改弹出窗口
                    $('#modal_confirm_imgGroup').modal('hide');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("删除成功！");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        var img_count = $('.img-area').find(".cell").length;
                        $('.second-menu').find('li#' + data.imgGroupId).remove();
                        var new_li = $('.second-menu').find('li#not_group').find('span.imgCount');
                        new_li.text(parseInt(new_li.text()) + img_count);
                        $('.second-menu').find('li#not_group').trigger("click");
                        getImgList("getImgListNotInGroup");
                    })
                },
                error: function (data) {
                    //隐藏修改弹出窗口
                    $('#modal_confirm_imgGroup').modal('hide');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("删除失败！");
                    $('#modal_msg').modal();
                }
            });
        });

        //移动分组弹出框保存事件
        $('#modal_changeGroup #save').click(function (e) {
            var groupId = $("#modal_changeGroup #groupId").val();
            //异步调用移动分组方法
            $.ajax({
                url: "changeGroup",
                type: "POST",
                data: {
                    ids: ids,
                    groupId: groupId
                },
                dataType: "json",
                success: function (data) {
                    //隐藏弹出窗口
                    $('#modal_changeGroup').modal('hide');
                    var ids_arr = ids.split(",");
                    $.each(ids_arr, function (i, v) {
                        $(".img-area").find(".cell#" + v).remove();
                    });
                    var old_li = $('.second-menu').find('li.active').find('span.imgCount');
                    old_li.text(parseInt(old_li.text()) - ids_arr.length);
                    var new_li = $('.second-menu').find('li#' + groupId).find('span.imgCount');
                    new_li.text(parseInt(new_li.text()) + ids_arr.length);
                    if ($(".img-area").find(".cell").length < 20) {
                        $(".img-area #waterfall").empty();
                        $.waterfall.load_index = -1;
                        $(".img-area").trigger("scroll");
                        $('#waterfall').waterfall(opt);
                    }
                    // 移除全选状态
                    $(".icheckbox_minimal-blue.js-allcheck").removeClass('checked');
                },
                error: function (data) {
                    //隐藏弹出窗口
                    $('#modal_changeGroup').modal('hide');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("移动分组失败！");
                    $('#modal_msg').modal();
                }
            });
        });

        //移动分组按钮点击事件
        $("#btn_change").click(function () {
            var content_ids = "";
            var checks_count = $(".img-area").find(".js-checkchild.checked").length;
            //判断选择的图片数量
            if (checks_count == 0) {
                $.tipLayer("至少选择一条记录！");
                return;
            } else {
                $.each($(".img-area").find(".js-checkchild.checked"), function (i, v) {
                    content_ids += $(v).closest(".cell").attr("id") + ",";
                });
            }
            changeGroup(content_ids.substring(0, content_ids.length - 1));
        });

        //图片删除弹出框删除按钮点击事件
        $('#modal_confirm_img #ok').click(function (e) {
            //异步调用删除方法
            $.ajax({
                url: "delImgs",
                type: "POST",
                data: {ids: ids},
                dataType: "json",
                success: function (data) {
                    //隐藏修改弹出窗口
                    $('#modal_confirm_img').modal('hide');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("删除成功！");
                    $('#modal_msg').modal();
                    //提示弹出框隐藏事件，隐藏后重新加载当前页面
                    $('#modal_msg').off('hide.bs.modal');
                    $('#modal_msg').on('hide.bs.modal', function () {
                        var ids_arr = ids.split(",");
                        $.each(ids_arr, function (i, v) {
                            $(".img-area").find(".cell#" + v).remove();
                        });
                        var old_li = $('.second-menu').find('li.active').find('span.imgCount');
                        old_li.text(parseInt(old_li.text()) - ids_arr.length);

                        if ($(".img-area").find(".cell").length < 20) {
                            $(".img-area #waterfall").empty();
                            $.waterfall.load_index = -1;
                            $(".img-area").trigger("scroll");
                            $('#waterfall').waterfall(opt);
                        }
                    });
                    // 移除全选状态
                    $(".icheckbox_minimal-blue.js-allcheck").removeClass('checked');
                },
                error: function (data) {
                    //隐藏修改弹出窗口
                    $('#modal_confirm_imgGroup').modal('hide');
                    //设置提示弹出框内容
                    $('#modal_msg #modal_msg_body').html("删除失败！");
                    $('#modal_msg').modal();
                }
            });
        });

        //删除按钮点击事件
        $("#btn_del").click(function () {
            var content_ids = "";
            var checks_count = $(".img-area").find(".js-checkchild.checked").length;
            //判断选择的图片数量
            if (checks_count == 0) {
                $.tipLayer("至少选择一条记录！");
                return;
            } else {
                $.each($(".img-area").find(".js-checkchild.checked"), function (i, v) {
                    content_ids += $(v).closest(".cell").attr("id") + ",";
                });
            }
            delImg(content_ids.substring(0, content_ids.length - 1));
        });
    });

    //新建图片分组项
    function newImgGroupItem(imgGroupId, imgGroupName, imgCount) {
        var li_item = "<li id='" + imgGroupId + "'><a href='javascript:getImgList(\"getImgsByGroupId\"," + imgGroupId + ");'><span class='imgGroupName'>" + imgGroupName + "</span>(<span class='imgCount'>" + imgCount + "</span>)</a>" +
                "<ul class='third-menu'>" +
                "<li><a href='javascript:updateImgGroup(" + imgGroupId + ");' ><i class='fa fa-hand-o-right p-r-5'></i>重命名</a></li>" +
                "<li><a href='javascript:delImgGroup(" + imgGroupId + ");' ><i class='fa fa-hand-o-right p-r-5'></i>删除分组</a></li>" +
                "</ul></li>";

        if ($('.second-menu').find('li#not_group').length > 0) {
            $(li_item).insertBefore($('.second-menu').find('li#not_group'));
        } else {
            $('.second-menu').append(li_item);
        }
    }

    //修改图片分组
    function updateImgGroup(imgGroupId) {
        //清空form
        clearForm($("#EditImgGroupForm"));

        $("#EditImgGroupForm").dpValidate({
            clear: true
        });
        var imgGroupName = $('.second-menu li#' + imgGroupId).find('span.imgGroupName').text();
        $("#EditImgGroupForm").find("input#imgGroupId").val(imgGroupId);
        $("#EditImgGroupForm").find("input#imgGroupName").val(imgGroupName);

        $('#modal_editImgGroup').modal();
    }

    var del_imgGroupId;

    //删除图片分组
    function delImgGroup(imgGroupId) {
        del_imgGroupId = imgGroupId;
        //设置删除弹出框内容
        $('#modal_confirm_imgGroup #modal_confirm_body').html("删除该图片分组后，分组下的图片将被移致未分组中，确认删除吗？");
        $('#modal_confirm_imgGroup').modal('show');
    }

    var _url;

    //未分组图片数据
    function getImgList(url, groupId) {
        _url = url;
        //异步加载未分组的图片数据
        $.ajax({
            url: url,
            type: "GET",
            dataType: "json",
            data: {
                VIEW_INDEX: 0,
                VIEW_SIZE: 20,
                groupId: groupId
            },
            success: function (data) {
                //设置菜单上的图片总数
                if (!groupId) {
                    $('.second-menu li#not_group').find('span.imgCount').text(data.totalSize);
                } else {
                    $('.second-menu li#' + groupId).find('span.imgCount').text(data.totalSize);
                }

                $('.img-area #waterfall').empty();
                //已有的图片分组
                $.each(data.recordsList, function (i, v) {
                    var conentId = v.CONTENT_ID,
                            createDate = timeStamp2String(v.CREATED_STAMP.time),
                            objectInfo = v.OBJECT_INFO;

                    var img_div = "<DIV id='" + conentId + "' class='cell'>" +
                            "<A href='javasript:void(0)'>" +
                            "<IMG src='" + objectInfo + "'>" +
                            "</A>" +
                            "<P>" +
                            "<div class='icheckbox_minimal-blue js-checkchild' style='position: relative;'></div>" +
                            "<A href='javasript:void(0)'>" + createDate + "</A>" +
                            "</P>" +
                            "<p class='img-btn' style='text-align: left;'>" +
                            "<span style='padding-left: 50px;'>" +
                            "<i class='fa fa-exchange' style='font-size: 20px;color:#C1C1C1;cursor: pointer;' title='移动分组' onclick='changeGroup(\"" + conentId + "\")'></i>" +
                            "</span>" +
                            "<span style='padding-left: 90px;'>" +
                            "<i class='fa fa-trash' style='font-size: 20px;color: #C1C1C1;cursor: pointer;' title='删除' onclick='delImg(\"" + conentId + "\")'></i>" +
                            "</span>" +
                            "</p>" +
                            "</DIV>";
                    $('.img-area #waterfall').append(img_div);
                });
                $.waterfall.load_index = 0;
                $('#waterfall').waterfall(opt);
            },
            error: function (data) {
                $.tipLayer("网络异常！");
            }
        });
    }

    //移动分组
    function changeGroup(contentId) {
        ids = contentId;
        //异步加载图片分组数据
        $.ajax({
            url: "GetImgGroupList",
            type: "POST",
            dataType: "json",
            async: false,
            success: function (data) {
                $("#modal_changeGroup #groupId").empty();
                //已有的图片分组
                $.each(data.imgGroupList, function (i, v) {
                    //移动分组弹出框的下拉选项
                    if (cur_group_id != v.imgGroupId) {
                        $("#modal_changeGroup #groupId").append("<option value='" + v.imgGroupId + "'>" + v.imgGroupName + "</option>");
                    }
                });
            },
            error: function (data) {
                $.tipLayer("网络异常！");
            }
        });

        if ($("#modal_changeGroup #groupId").find("option").length == 0) {
            $.tipLayer("无其他图片分组！");
            return;
        }

        $("#modal_changeGroup").modal("show");
    }

    //删除图片
    function delImg(contentId) {
        ids = contentId;
        //设置删除弹出框内容
        $('#modal_confirm_img #modal_confirm_body').html("删除图片后，相关页面讲无法展示图片，确认删除吗？");
        $('#modal_confirm_img').modal('show');
    }

    //重载当前分组的图片数据
    function reloadImgs() {
        $(".img-area #waterfall").empty();
        var load_index = $.waterfall.load_index;
        for (var i = -1; i < load_index; i++) {
            $.waterfall.load_index = i;
            $(".img-area").trigger("scroll");
        }
        $('#waterfall').waterfall(opt);
    }
</script>

