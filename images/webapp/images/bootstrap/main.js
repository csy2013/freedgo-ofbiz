
// ICO 全局事件
;(function($,window,document,undefined){
  // 表单校验
  $.fn.dpValidate = function(option) {
    var errorNum = 0;

    if (option.validate) {
      $(this).on('submit',function(){
        errorNum = 0;
        $(this).find('.form-group').each(function(){
          var type = $(this).data('type'),
            mark = $(this).data('mark') || '',
            msg = $(this).data('msg') || '',
            relation = $(this).data('relation'),
            isRelation;

          if (relation) isRelation = relationValidata(relation);

          if (type && !isRelation) {
            if (type.indexOf(',')>-1) {
              var typeArray = type.split(',');
              for (var i=0; i < typeArray.length; i++) {
                var hasError = validateType($(this),typeArray[i],mark,msg);
                if(hasError){
                  break;
                }
              }
            } else {
              validateType($(this),type,mark,msg);
            }
          }
        });
        if (option.console) console.log('共有'+errorNum+'个错误');
        if (!errorNum && typeof option.callback === 'function') option.callback();
        return false;
      });
    }

    if (option.clear) $(this).find('.form-group').removeClass('has-error').find('.dp-error-msg').empty();

    function validateType ($el,type,mark,msg) {
      var $vd = $el.find('.dp-vd'),
        $error = $el.find('.dp-error-msg'),
        errorTxt = $error.text();

      var hasError=false;
      if ($vd.size()) {
        switch (type) {
          case 'required':
          {
            var value = $vd.val();
            if (!value) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'不能为空');
              hasError=true;
            }
          }
            break;

          case 'min':
          {
            var value = $vd.val(),
              number = +$el.data('number');

            if (value.length < number) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'不能少于'+number+'位');
              hasError=true;
            }
          }
            break;

          case 'max':
          {
            var value = $vd.val(),
              number = +$el.data('number');

            if (value.toString().length > number) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'不能多于'+number+'位');
              hasError=true;
            } else {
              $el.removeClass('has-error');
              $error.empty();
            }
          }
            break;

          case 'range':
          {
            var value = $vd.val(),
              number = $el.data('number') || '0,0',
              numberArray = number.split(',');

            if (value.length < +numberArray[1] && value.length > +numberArray[0]) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+'请输入'+numberArray[0]+'位到'+numberArray[1]+'位的'+mark);
              hasError=true;
            }
          }
            break;

          case 'format':
          {
            var value = $vd.val() || '',
              reg = $el.data('reg') || '';
            reg = eval(reg);
            if (!reg.test(value)) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'格式不正确');
                hasError=true;
            }
          }
            break;

          case 'minCheck':
          {
            var checked = $el.find(':checked').size(),
              number = +$el.data('number');

            if (checked < number) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+'请至少选择'+number+'个'+mark);
                hasError=true;
            }
          }
            break;

          case 'maxCheck':
          {
            var checked = $el.find(':checked').size(),
              number = +$el.data('number');

            if (checked > number) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'不能多于'+number+'个');
                hasError=true;
            }
          }
            break;

          case 'linkGt':
          {
            var value = $vd.val(),
              compare = $el.data('compare-link') || '',
              $compare = $('#'+compare),
              compareMark = $el.data('compare-mark') || $compare.data('mark') || '',
              compareValue = $compare.find('.dp-vd').val();

            if (value < compareValue) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'必须大于'+compareMark);
              hasError=true;
            }
          }
            break;

          case 'linkLt':
          {
            var value = $vd.val(),
              compare = $el.data('compare-link') || '',
              $compare = $('#'+compare),
              compareMark = $el.data('compare-mark') || $compare.data('mark') || '',
              compareValue = $compare.find('.dp-vd').val();

            if (value > compareValue) {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(msg);
              else $error.text(mark+'必须小于'+compareMark);
              hasError=true;

            }
          }
            break;

          case 'linkEq':
          {
            var value = $vd.val(),
              compare = $el.data('compare-link') || '',
              $compare = $('#'+compare),
              compareMark = $el.data('compare-mark') || $compare.data('mark') || '',
              compareValue = $compare.find('.dp-vd').val();

            if (value === compareValue) {
              $el.removeClass('has-error');
              $error.empty();
            } else {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'必须等于'+compareMark);
              hasError=true;
            }
          }
            break;

          case 'linkLe':
          {
            var value = +$vd.val(),
              compare = $el.data('compare-link') || '',
              $compare = $('#'+compare),
              compareMark = $el.data('compare-mark') || $compare.data('mark') || '',
              compareValue = +$compare.find('.dp-vd').val();


            if (value <= compareValue) {
              $el.removeClass('has-error');
              $error.empty();
            } else {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'必须小于等于'+compareMark);
                hasError=true;

            }
          }
            break;

          case 'linkGe':
          {
            var value = +$vd.val(),
              compare = $el.data('compare-link') || '',
              $compare = $('#'+compare),
              compareMark = $el.data('compare-mark') || $compare.data('mark') || '',
              compareValue = +$compare.find('.dp-vd').val();

            if (value >= compareValue) {
              $el.removeClass('has-error');
              $error.empty();
            } else {
              errorNum++;
              $el.addClass('has-error');
              if (msg) $error.text(errorTxt+msg);
              else $error.text(errorTxt+mark+'必须大于等于'+compareMark);
                hasError=true;

            }
          }
            break;
        }
      }
      return hasError;
    };

    function relationValidata (relation) {
      var $relation,
        relationFailNum = 0;

      if (relation.indexOf(',')>-1) {
        var relationArray = relation.split(',');

        for (var i=0; i < relationArray.length; i++) {
          $relation = $('#'+relationArray[i]);
          if ($relation.hasClass('has-error')) relationFailNum++;
        }
      } else {
        $relation = $('#'+relation);
        if ($relation.hasClass('has-error')) relationFailNum++;
      }

      return relationFailNum;
    }
  };

  $.extend({
    // 提示弹窗
    tipLayer: function(msg) {
      var message = msg || '';

      if (!$('#tipLayer').size()) {
        var tipLayerContent = '<div id="tipLayer" class="modal fade bs-example-modal-sm" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">'
          + '<div class="modal-dialog modal-sm">'
          + '<div class="modal-content">'
          + '<div class="modal-header">'
          + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>'
          + '<h4 class="modal-title" id="mySmallModalLabel">提示</h4></div>'
          + '<div class="modal-body tipLayerMsg"></div>'
          + '</div></div></div>';

        $('body').append(tipLayerContent);
      }

      $('#tipLayer').modal('show').find('.tipLayerMsg').text(message);
    },
    // 确认弹窗
    confirmLayer: function(option) {
      var defaultOption = option || { msg: ''},
        msg = defaultOption.msg || '';

      if (!$('#confirmLayer').size()) {
        var confirmLayerContent = '<div id="confirmLayer" class="modal fade">'
          + '<div class="modal-dialog">'
          + '<div class="modal-content">'
          + '<div class="modal-header">'
          + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
          + '<h4 class="modal-title">确认</h4></div>'
          + '<div class="modal-body confirmLayerMsg"></div>'
          + '<div class="modal-footer">'
          + '<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>'
          + '<button type="button" class="btn btn-primary confirm-btn">确认</button></div></div></div></div>';

        $('body').append(confirmLayerContent);
      }

      $('#confirmLayer').modal('show').find('.confirmLayerMsg').text(msg);

      if (typeof defaultOption.confirm === 'function') $('.confirm-btn','#confirmLayer').click(option.confirm);
    },
    // 其他弹窗
    otherLayer: function (option) {
      if (typeof option !== 'object') option = {};
      var title = option.title || '其他弹窗',
        content = option.content || '';

      if (content instanceof $) content = content.html();

      if (!$('#otherLayer').size()) {
        var otherLayerContent = '<div id="otherLayer" class="otherLayer-bg">'
          + '<div class="otherLayer-box">'
          + '<div class="otherLayer-hd">'
          + '<h4 class="otherLayer-title">'+title+'</h4>'
          + '<span class="otherLayer-close">×</span></div>'
          + '<div class="otherLayer-bd"></div></div></div>';

        $('body').append(otherLayerContent);
      }

      $('#otherLayer').show().find('.otherLayer-bd').html(content);
    },
    // 获取IE版本，若非IE则返回0
    getIeVersion: function() {
      var agent = navigator.userAgent.toLowerCase(),
        regStr_ie = /msie [\d.]+;/gi,
        browser = '';

      //IE
      if(agent.indexOf("msie") > 0) browser = agent.match(regStr_ie);

      return +(browser).replace(/[^0-9.]/ig,'');
    }
  });

  $(function(){
    // 全选 && 反选
    (function(){
      $(document).on('change','.js-allcheck',function(){
        var $parent = $(this).closest('.js-checkparent');

        if ($(this).prop('checked')) $parent.find('.js-checkchild').prop('checked',true);
        else $parent.find('.js-checkchild').prop('checked',false);
      });

      $(document).on('change','.js-checkchild',function(){
        var $parent = $(this).closest('.js-checkparent');

        if ($parent.find('.js-checkchild').size() === $parent.find('.js-checkchild:checked').size()) $parent.find('.js-allcheck').prop('checked',true);
        else $parent.find('.js-allcheck').prop('checked',false);
      });
    })();

    // 列表相关
    (function(){
      // 排序
      $('.js-sort-list').on('click','.js-sort',function(){
        var key = $(this).data('key'),
          url = window.location.href,
          type = !$(this).hasClass('text-muted') && $(this).hasClass('fa-sort-amount-desc') ? 'asc' : 'desc';

        window.location.href = buildUrl(buildUrl(url,'sortField',key),'sortType',type);
        return false;
      });

      function buildUrl(url,key,value) {
        if (url.indexOf(key) > -1) {
          var urlArray =  url.split(key),
            afterUrl = urlArray[1].indexOf('&') > -1
              ? urlArray[1].substring(urlArray[1].indexOf('&'))
              : '';

          url = urlArray[0]+key+'='+value+afterUrl;
        } else {
          if (url.indexOf('?') === -1) url += '?'+key+'='+value;
          else url += '&'+key+'='+value;
        }

        return url;
      }
    })();

    // 图片选择上传
    (function(){
      var chooseImgfn = function (serverImgId,imgFileArray,imgNameArray,serverImgClassify,serverImgPage) {
        this.serverImgId = serverImgId;
        this.imgFileArray = imgFileArray;
        this.imgNameArray = imgNameArray;
        this.serverImgClassify = serverImgClassify;
        this.serverImgPage = serverImgPage;
      };

      var chooseImgObj;

      $.extend({
        // 图片选择上传
        chooseImage: {
          // 初始化
          int: function(option) {
            var $imgLay = $('#img-layer'),
              defaultOption = {
                serverChooseNum: 5,
                getServerImgUrl: '',
                submitLocalImgUrl: '',
                submitNetworkImgUrl: '',
                otherObj: null
              };

            option = option || defaultOption;

            var imgindex = option.imgIndex ? option.imgIndex: '';
            if (!$imgLay.size()) {
              var ieVersion = $.getIeVersion(),
                localTab,
                chooseImageLayer,
                userId = option.userId ? 'data-userid="'+option.userId+'"' : '',

                chooseNum = option.serverChooseNum || 5;

              if (!ieVersion || ieVersion > 9) {
                localTab = '<div id="chooseOriginalBtn" class="btn btn-primary">选择图片</div>'
                  + '<div class="checkbox dp-localimg-check"><label>'
                  + '<input id="dp-isOriginal" type="checkbox" checked>'
                  + '是否上传原图</label></div>'
                  + '<input id="chooseOriginalInput" class="dp-localimg-input" type="file" data-num="'+chooseNum+'" accept="image/png,image/jpg,image/jpeg,image/bmp" multiple>';
              } else {
                localTab = '<p>您的浏览器不支持本地上传</p>';
              }

              chooseImageLayer = '<div id="img-layer" class="img-layer-bg" '+userId+' data-url="'+option.getServerImgUrl+'">'
                + '<div class="img-layer">'
                + '<div class="layer-hd">选择图片</div>'
                + '<div class="layer-bd">'
                + '<div class="nav-tabs-custom m-0">'
                + '<ul class="nav nav-tabs">'
                + '<li class="active">'
                + '<a href="#local" data-toggle="tab" aria-expanded="false">上传本地图片</a></li>'
                + '<li><a href="#server" data-toggle="tab" aria-expanded="false">选择图库图片</a></li>'

                //                                             允许网络图片上传是恢复此处注释下面的部分
                //                                             + '<li><a href="#network" data-toggle="tab" aria-expanded="false">添加网络图片</a></li>'

                + '</ul><div class="tab-content">'
                + '<div id="local" class="tab-pane active" data-url="'+option.submitLocalImgUrl+'">'
                + '<div class="chooseImg-btn-box">'
                + localTab + '</div>'
                + '<div class="chooseImg-show-box row"></div></div>'
                + '<div id="server" class="tab-pane form-horizontal">'
                + '<div class="form-group">'
                + '<label class="col-sm-2 control-label">图片分类</label>'
                + '<div class="col-sm-10">'
                + '<select class="form-control imgClassify" ></select></div></div>'
                + '<div class="server-img-list row" data-num="'+chooseNum+'"></div>'
                + '<div class="js-imgindex" data-picindex="'+imgindex+'"></div>'

                //                                             不允许网络图片上传是恢复此处注释其他的部分
                + '<div class="server-img-paging"></div></div></div></div></div>'

                //                                             允许网络图片上传时恢复此处注释上面的部分
                //                                             + '<div class="server-img-paging"></div></div>'
                //                                             + '<div id="network" class="tab-pane form-horizontal" data-url="'+option.submitNetworkImgUrl+'">'
                //                                             + '<div class="form-group">'
                //                                             + '<label for="networkImage" class="col-sm-2 control-label">图片地址</label>'
                //                                             + '<div class="col-sm-10">'
                //                                             + '<input id="networkImage" name="networkImage" type="text" class="form-control"></div></div>'
                //                                             + '<div class="network-imgshow">'
                //                                             + '<p>图片预览</p></div></div></div></div></div>'

                + '<div class="layer-ft">'
                + '<span class="imgLayer-errormsg"></span>'
                + '<button type="button" class="btn btn-default m-r-5 img-cancel-btn">取消</button>'
                + '<button type="button" class="btn btn-primary img-submit-btn">保存</button></div></div></div>';

              $('body').append(chooseImageLayer);
            } else {
              $('#img-layer').find('.js-imgindex').data('picindex',imgindex);
              $imgLay.find('#chooseOriginalInput').val('');
              $imgLay.find('.imgLayer-errormsg').empty();
              $imgLay.find('.chooseImg-show-box').empty();

//                            允许网络图片上传时恢复此处注释上面的部分
//                            $imgLay.find('.network-imgshow').empty();

              $imgLay.find('.chooseImg-btn-box').show();
            }

            chooseImgObj = new chooseImgfn([],[],[],1,1);

            if (option.otherObj) chooseImgObj.otherObj = option.otherObj;
          },
          // 显示
          show: function() {

            var $imgLay = $('#img-layer'),
              curImgindex= $('#img-layer').find('.js-imgindex').data('picindex')?$('#img-layer').find('.js-imgindex').data('picindex'):'',

              url = $imgLay.data('url');


            if (url) {
              var getServerImg = $.ajax({
                url: url,
                type: 'post',
                dataType: 'json',
                data: {
                  page: 1,
                  classify: 1,
                  imgindex: curImgindex
                },
                timeout: 5000,
                beforeSend: function() {

                }
              })
                .done(function(data){
                  if (data.status) {
                    var imglist = '',
                      pagelist = '',
                      classifylist = '<option value="1">未分类</option>';

                    if (data.classifylist) {
                      for (var c = 0; c<data.classifylist.length; c++) {
                        classifylist += '<option value="'+data.classifylist[c].id+'">'+data.classifylist[c].title+'</option>';
                      }
                    }

                    if (data.imglist) {
                      for (var i = 0; i<data.imglist.length; i++) {
                        imglist += '<label class="server-img-item col-sm-4">'
                          + '<input type="checkbox" value="'+data.imglist[i].id+'">'
                          + '<img src="'+data.imglist[i].url+'"></label>';
                      }
                    }

                    if (data.maxPageNum) {
                      var prev = data.curPageNum === 1 ? '<a class="prev disabled" href="javascript:void(0);">&lt; 上一页</a>' : '<a class="prev" data-page="'+(data.curPageNum-1)+'" href="javascript:void(0);">&lt; 上一页</a>',
                        next = data.curPageNum === data.maxPageNum ? '<a class="next disabled" href="#">下一页 &gt;</a>' : '<a class="next" data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">下一页 &gt;</a>';

                      pagelist += prev;

                      if (data.maxPageNum > 6) {

                        if (data.curPageNum >=1 && data.curPageNum <= 4) {
                          for (var p = 1; p <= 4; p++) {
                            if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                            else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                          }

                          pagelist += '<span>……</span>'
                            + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                        } else if (data.curPageNum >= data.maxPageNum-4 && data.curPageNum <= data.maxPageNum) {
                          pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                            + '<span>……</span>';

                          for (var p = data.maxPageNum-4; p <= data.maxPageNum; p++) {
                            if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                            else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                          }
                        } else if (data.curPageNum > 4) {
                          pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                            + '<span>……</span>'
                            + '<a class="active" href="javascript:void(0);">'+data.curPageNum+'</a>'
                            + '<a data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">'+(data.curPageNum+1)+'</a>'
                            + '<span>……</span>'
                            + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                        }
                      } else {
                        for (var p = 1; p <= data.maxPageNum; p++) {
                          if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                          else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                        }
                      }

                      pagelist +=  next + '<form class="server-img-paging-form" action="">向第'
                        + '<input type="text">页'
                        + '<button type="submit">跳转</button></form>';
                      $('#img-layer').data("maxPageNum",data.maxPageNum);
                    }
                    if(data.imgindex){
                      $('#img-layer').find('.js-imgindex').data('picindex',data.imgindex);
                    }
                    $imgLay.find('.server-img-list').html(imglist).siblings('.server-img-paging').html(pagelist);
                    $imgLay.find('.imgClassify').html(classifylist);

                    $imgLay.show();
                  } else {
                    $imgLay.show();
                  }
                })
                .fail(function(){
                  $imgLay.show();
                })
                .always(function(XMLHttpRequest,status){
                  if (status === 'timeout') {
                    getServerImg.abort();
                    $imgLay.show();
                  }
                });
            } else {
              $imgLay.show();
            }
          },
          // 隐藏
          hide: function() {
            var $imgLay = $('#img-layer');

            $imgLay.hide();
            $imgLay.find('.nav-tabs li:eq(0)').addClass('active').siblings().removeClass('active');
            $imgLay.find('.tab-pane').eq(0).addClass('active').siblings().removeClass('active');
            $.chooseImage.int();
          },
          // 预览
          preview: function(option) {
            // 定义图片最大宽度
            var max_Width = option.max_Width || 1200;
            // 定义图片最大高度
            var max_Height = option.max_Height || 960;
            // 定义压缩方式
            var compressType = option.type || 'none';
            // 定义压缩后图片格式
            var imgType = option.url.substring(option.url.indexOf(':')+1,option.url.indexOf(';'));
            // 定义开始事件
            var beforeMethod = option.beforeMethod || function () {};
            // 定义结束事件
            var endMethod = option.endMethod || function () {};
            // 创建Image对象
            var image = new Image();

            beforeMethod();

            image.onload = function () {
              // 创建 canvas DOM
              var canvas = document.createElement('canvas');
              var ctx = canvas.getContext("2d");

              // 图片尺寸压缩
              switch (compressType) {
                case 'height_Auto':
                {
                  //如果高度超标，则等比缩放
                  if (image.width > max_Width) {
                    image.width *= max_Height / image.height;
                    image.height = max_Height;
                  }
                }
                  break;

                case 'width_Auto':
                {
                  //如果宽度超标，则等比缩放
                  if (image.width > max_Width) {
                    image.height *= max_Width / image.width;
                    image.width = max_Width;
                  }
                }
                  break;

                case 'clip':
                {
                  // 如果宽度或高度超标，则裁剪
                  if (image.width > max_Width) image.width = max_Width;
                  if (image.height > max_Height) image.height = max_Height;
                }
                  break;

                default:
                  break;
              }

              canvas.width = image.width;
              canvas.height = image.height;
              ctx.drawImage(image, 0, 0, image.width, image.height);

              // 获取压缩后图片URL方便回传
              var url = canvas.toDataURL(imgType);
              var blob = dataURLtoBlob(url);

              endMethod(url,blob);
            }

            image.src = option.url;

            function dataURLtoBlob(dataurl) {
              var arr = dataurl.split(','),
                mime = arr[0].match(/:(.*?);/)[1],
                bstr = atob(arr[1]),
                n = bstr.length,
                u8arr = new Uint8Array(n);

              while(n--){
                u8arr[n] = bstr.charCodeAt(n);
              }
              return new Blob([u8arr], {type:mime});
            }
          },
          // 提交
          choose: function(obj,callback){
            var $imgLay = $('#img-layer'),
              userId = $imgLay.data('userid'),
              type = $imgLay.find('.tab-pane.active').attr('id'),
              url = $imgLay.find('.tab-pane.active').data('url'),
              networkImg = $('#networkImage').val(),
              dataObj = new FormData();

            switch (type) {
              case 'local':
              {
                if (!obj.imgFileArray.length) {
                  $imgLay.find('.imgLayer-errormsg').text('请至少选择一张图片');
                  return false;
                }

                for (var x = 0; x < obj.imgFileArray.length; x++) {
                  dataObj.append('uploadedFile'+x,obj.imgFileArray[x],obj.imgNameArray[x]);
                }
              }
                break;

              case 'server':
              {
                if (!obj.serverImgId.length) {
                  $imgLay.find('.imgLayer-errormsg').text('请至少选择一张图片');
                  return false;
                }
              }
                break;

              case 'network':
              {
                if (!networkImg) {
                  $imgLay.find('.imgLayer-errormsg').text('请填写图片路径');
                  return false;
                }

                dataObj.append('networkImgUrl',networkImg);
              }
                break;

              default:
                break;
            }

            if (userId) dataObj.append('userId',userId);

            if (obj.otherObj) {
              for (x in obj.otherObj) {
                dataObj.append(x,obj.otherObj[x]);
              }
            }

            if (obj.imgGroupId){
              dataObj.append("imgGroupId",obj.imgGroupId);
            }

            if (userId == "imgChoose") {
              if (obj.imgNameArray.length > 6) {
                return false;
              }
            }

            if (url) {
              $.ajax({
                url: url,
                type: 'post',
                dataType: 'json',
                data: dataObj,
                processData: false,
                contentType: false,
                beforeSend: function(){

                }
              })
                .done(function(data){
                  if (data.status) {
                    if (typeof callback === 'function') callback(data.contentIds);
                    $.chooseImage.hide();
                  } else {
                    $imgLay.find('.imgLayer-errormsg').text(data.info);
                  }
                })
                .fail(function(){
                  $imgLay.find('.imgLayer-errormsg').text('传输失败');
                });
            } else {
              if (typeof callback === 'function') {
                var backObj = {}
                // Mod by zhajh at 20170619 修改单个图形库的文件 Begin
                // for (var z=0; z < obj.serverImgId.length; z++ ) {
                //     backObj['uploadedFile'+z] = obj.serverImgId[z];
                // }
                // 当前商品图片的index
                var imgindex=$('#img-layer').find('.js-imgindex').data('picindex');

                if(imgindex){
                  // 是选择图形库的图片
                  var imgtype = $imgLay.find('.tab-pane.active').attr('id');
                  if(imgtype=='server'){
                    backObj['uploadedFile'+imgindex] = obj.serverImgId[0];
                  }
                }else{
                  for (var z=0; z < obj.serverImgId.length; z++ ) {
                    backObj['uploadedFile'+z] = obj.serverImgId[z];
                  }
                }
                // Mod by zhajh at 20170619 修改单个图形库的文件 End
                $.chooseImage.hide();
              }

              callback(backObj);
            }

          },
          // 获取数据
          getImgData: function(obj) {
            chooseImgObj = obj || chooseImgObj;
            return chooseImgObj;
          }
        }
      });

      // 打开本地文件选择组件
      $('body').on('click','#chooseOriginalBtn',function(){
        $('#chooseOriginalInput').click();
      });

      // 选择本地图片预览
      $('body').on('change','#chooseOriginalInput',function(e){
        var $error = $('#img-layer').find('.imgLayer-errormsg'),
          num = $(this).data('num');

        if (this.value) {
          if (e.target.files.length > num) {
            $error.text('最多只能同时选择'+num+'张图片');
            return false;
          } else {
            $error.text('');
            for (var i=0; i < e.target.files.length;i++) {
              if (e.target.files[i].size > 5242880) {
                $error.text('所选图片不能大于5M');
                return false;
              }

              var imgName = e.target.files[i].name;
              var freader = new FileReader();

              chooseImgObj.imgNameArray.push(imgName);
              freader.readAsDataURL(e.target.files[i]);
              freader.onload = function (e) {
                var compressType = $('#dp-isOriginal').prop('checked') ? 'none' : 'width_Auto';
                $.chooseImage.preview({
                  url: e.target.result,
                  compressType: compressType,
                  endMethod: function(url,blob){
                    var imgcontent = '<div class="col-sm-4"><img src="'+url+'"></div>'
                    $('#img-layer').find('.chooseImg-show-box').append(imgcontent);
                    $('#img-layer').find('.chooseImg-btn-box').hide();

                    chooseImgObj.imgFileArray.push(blob);
                  }
                });
              }
            }
          }
        }
      });

      // 点击分页跳转
      $('body').on('click','.server-img-paging a',function(){
        var $imgLay = $('#img-layer'),
          $error = $imgLay.find('.imgLayer-errormsg'),
          url = $imgLay.data('url'),
          page = $(this).data('page');

        chooseImgObj.serverImgPage = page;

        if (page) {
          $.ajax({
            url: url,
            type: 'post',
            dataType: 'json',
            data: {
              page: chooseImgObj.serverImgPage,
              classify: chooseImgObj.serverImgClassify
            },
            beforeSend: function() {

            }
          })
            .done(function(data){
              if (data.status) {
                var imglist = '',
                  pagelist = '';

                if (data.imglist) {
                  for (var i = 0; i<data.imglist.length; i++) {
                    var boxChecked = '';
                    for (var x = 0; x<chooseImgObj.serverImgId.length; x++) {
                      if (chooseImgObj.serverImgId[x] === data.imglist[i].id) {
                        boxChecked = 'checked';
                        break;
                      }
                    }

                    imglist += '<label class="server-img-item col-sm-4">'
                      + '<input type="checkbox" value="'+data.imglist[i].id+'" '+boxChecked+'>'
                      + '<img src="'+data.imglist[i].url+'"></label>';
                  }
                }

                if (data.maxPageNum) {
                  var prev = data.curPageNum === 1 ? '<a class="prev disabled" href="javascript:void(0);">&lt; 上一页</a>' : '<a class="prev" data-page="'+(data.curPageNum-1)+'" href="javascript:void(0);">&lt; 上一页</a>',
                    next = data.curPageNum === data.maxPageNum ? '<a class="next disabled" href="#">下一页 &gt;</a>' : '<a class="next" data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">下一页 &gt;</a>';
                  pagelist += prev;

                  if (data.maxPageNum > 6) {

                    if (data.curPageNum >=1 && data.curPageNum <= 4) {
                      for (var p = 1; p <= 4; p++) {
                        if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                        else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                      }

                      pagelist += '<span>……</span>'
                        + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                    } else if (data.curPageNum >= data.maxPageNum-4 && data.curPageNum <= data.maxPageNum) {
                      pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                        + '<span>……</span>';

                      for (var p = data.maxPageNum-4; p <= data.maxPageNum; p++) {
                        if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                        else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                      }
                    } else if (data.curPageNum > 4) {
                      pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                        + '<span>……</span>'
                        + '<a class="active" href="javascript:void(0);">'+data.curPageNum+'</a>'
                        + '<a data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">'+(data.curPageNum+1)+'</a>'
                        + '<span>……</span>'
                        + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                    }
                  } else {
                    for (var p = 1; p <= data.maxPageNum; p++) {
                      if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                      else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                    }
                  }

                  pagelist +=  next + '<form class="server-img-paging-form" action="">向第'
                    + '<input type="text">页'
                    + '<button type="submit">跳转</button></form>';
                  $('#img-layer').data("maxPageNum",data.maxPageNum);
                }

                $imgLay.find('.server-img-list').html(imglist).siblings('.server-img-paging').html(pagelist);
              } else {
                $error.text('查询失败');
              }
            })
            .fail(function(){
              $error.text('查询失败');
            });
        }
      });

      // 跳转到指定页
      $('body').on('submit','.server-img-paging-form',function(){
        var $imgLay = $('#img-layer'),
          $error = $imgLay.find('.imgLayer-errormsg'),
          url = $imgLay.data('url'),
          page = $(this).find('input').val(),
          reg = /^[1-9]\d*$/;
        var maxPageNum=$imgLay.data('maxPageNum');


        if (!reg.test(page)) $error.text('页数只能为正整数');
        else {
          if(page>maxPageNum){
            page=maxPageNum;
          }
          $error.text('');
          chooseImgObj.serverImgPage = page;

          $.ajax({
            url: url,
            type: 'post',
            dataType: 'json',
            data: {
              page: chooseImgObj.serverImgPage,
              classify: chooseImgObj.serverImgClassify
            },
            beforeSend: function() {

            }
          })
            .done(function(data){
              if (data.status) {
                var imglist = '',
                  pagelist = '';

                if (data.imglist) {
                  for (var i = 0; i<data.imglist.length; i++) {
                    var boxChecked = '';
                    for (var x = 0; x<chooseImgObj.serverImgId.length; x++) {
                      if (chooseImgObj.serverImgId[x] === data.imglist[i].id) {
                        boxChecked = 'checked';
                        break;
                      }
                    }

                    imglist += '<label class="server-img-item col-sm-4">'
                      + '<input type="checkbox" value="'+data.imglist[i].id+'" '+boxChecked+'>'
                      + '<img src="'+data.imglist[i].url+'"></label>';
                  }
                }

                if (data.maxPageNum) {
                  var prev = data.curPageNum === 1 ? '<a class="prev disabled" href="javascript:void(0);">&lt; 上一页</a>' : '<a class="prev" data-page="'+(data.curPageNum-1)+'" href="javascript:void(0);">&lt; 上一页</a>',
                    next = data.curPageNum === data.maxPageNum ? '<a class="next disabled" href="#">下一页 &gt;</a>' : '<a class="next" data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">下一页 &gt;</a>';

                  pagelist += prev;

                  if (data.maxPageNum > 6) {

                    if (data.curPageNum >=1 && data.curPageNum <= 4) {
                      for (var p = 1; p <= 4; p++) {
                        if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                        else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                      }

                      pagelist += '<span>……</span>'
                        + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                    } else if (data.curPageNum >= data.maxPageNum-4 && data.curPageNum <= data.maxPageNum) {
                      pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                        + '<span>……</span>';

                      for (var p = data.maxPageNum-4; p <= data.maxPageNum; p++) {
                        if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                        else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                      }
                    } else if (data.curPageNum > 4) {
                      pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                        + '<span>……</span>'
                        + '<a class="active" href="javascript:void(0);">'+data.curPageNum+'</a>'
                        + '<a data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">'+(data.curPageNum+1)+'</a>'
                        + '<span>……</span>'
                        + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                    }
                  } else {
                    for (var p = 1; p <= data.maxPageNum; p++) {
                      if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                      else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                    }
                  }

                  pagelist +=  next + '<form class="server-img-paging-form" action="">向第'
                    + '<input type="text">页'
                    + '<button type="submit">跳转</button></form>';
                }

                $imgLay.find('.server-img-list').html(imglist).siblings('.server-img-paging').html(pagelist);
              } else {
                $error.text('查询失败');
              }
            })
            .fail(function(){
              $error.text('查询失败');
            });
        }

        return false;
      });

      // 切换图库分类
      $('body').on('change','.imgClassify',function(){
        var classify = this.value,
          $imgLay = $('#img-layer'),
          $error = $imgLay.find('.imgLayer-errormsg'),
          url = $imgLay.data('url');

        chooseImgObj.serverImgClassify = classify;
        chooseImgObj.serverImgPage = 1;

        $.ajax({
          url: url,
          type: 'post',
          dataType: 'json',
          data: {
            page: chooseImgObj.serverImgPage,
            classify: chooseImgObj.serverImgClassify
          },
          beforeSend: function() {

          }
        })
          .done(function(data){
            if (data.status) {
              var imglist = '',
                pagelist = '';

              if (data.imglist) {
                for (var i = 0; i<data.imglist.length; i++) {
                  var boxChecked = '';
                  for (var x = 0; x<chooseImgObj.serverImgId.length; x++) {
                    if (chooseImgObj.serverImgId[x] === data.imglist[i].id) {
                      boxChecked = 'checked';
                      break;
                    }
                  }

                  imglist += '<label class="server-img-item col-sm-4">'
                    + '<input type="checkbox" value="'+data.imglist[i].id+'" '+boxChecked+'>'
                    + '<img src="'+data.imglist[i].url+'"></label>';
                }
              }

              if (data.maxPageNum) {
                var prev = data.curPageNum === 1 ? '<a class="prev disabled" href="javascript:void(0);">&lt; 上一页</a>' : '<a class="prev" data-page="'+(data.curPageNum-1)+'" href="javascript:void(0);">&lt; 上一页</a>',
                  next = data.curPageNum === data.maxPageNum ? '<a class="next disabled" href="#">下一页 &gt;</a>' : '<a class="next" data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">下一页 &gt;</a>';

                pagelist += prev;

                if (data.maxPageNum > 6) {

                  if (data.curPageNum >=1 && data.curPageNum <= 4) {
                    for (var p = 1; p <= 4; p++) {
                      if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                      else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                    }

                    pagelist += '<span>……</span>'
                      + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                  } else if (data.curPageNum >= data.maxPageNum-4 && data.curPageNum <= data.maxPageNum) {
                    pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                      + '<span>……</span>';

                    for (var p = data.maxPageNum-4; p <= data.maxPageNum; p++) {
                      if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                      else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                    }
                  } else if (data.curPageNum > 4) {
                    pagelist += '<a data-page="1" href="javascript:void(0);">1</a>'
                      + '<span>……</span>'
                      + '<a class="active" href="javascript:void(0);">'+data.curPageNum+'</a>'
                      + '<a data-page="'+(data.curPageNum+1)+'" href="javascript:void(0);">'+(data.curPageNum+1)+'</a>'
                      + '<span>……</span>'
                      + '<a data-page="'+data.maxPageNum+'" href="javascript:void(0);">'+data.maxPageNum+'</a>';
                  }
                } else {
                  for (var p = 1; p <= data.maxPageNum; p++) {
                    if (p === data.curPageNum) pagelist += '<a class="active" href="javascript:void(0);">'+p+'</a>';
                    else pagelist += '<a data-page="'+p+'" href="javascript:void(0);">'+p+'</a>';
                  }
                }

                pagelist +=  next + '<form class="server-img-paging-form" action="">向第'
                  + '<input type="text">页'
                  + '<button type="submit">跳转</button></form>';
              }

              $imgLay.find('.server-img-list').html(imglist).siblings('.server-img-paging').html(pagelist);
            } else {
              $error.text('查询失败');
            }
          })
          .fail(function(){
            $error.text('查询失败');
          });
      });

      // 选择图库图片
      $('body').on('change','.server-img-item :checkbox',function(){
        var $error = $('#img-layer').find('.imgLayer-errormsg'),
          num = $('#img-layer').find('.server-img-list').data('num'),
          id = $(this).val();

        if ($(this).prop('checked')) {
          if (chooseImgObj.serverImgId.length == num ) {
            $error.text('最多只能选择'+num+'张图片');
            $(this).prop('checked',false);
          } else {
            chooseImgObj.serverImgId.push(id);
          }
        } else {
          chooseImgObj.serverImgId = chooseImgObj.serverImgId.filter(function(item){ return item != id; });
          $error.text('');
        }
      });

      // 网络图片预览
      $('body').on('propertychange input','#networkImage',function(){
        var url = this.value,
          $networkbox = $('#img-layer').find('.network-imgshow');

        if ($networkbox.find('img').size()) $networkbox.find('img').attr('src',url);
        else $networkbox.append('<img src="'+url+'">');
      });

      // 关闭组件弹窗
      $('body').on('click','.img-cancel-btn',function(){
        $.chooseImage.hide();
      });
    })();

    // 其他弹窗
    (function(){
      $('body').on('click','.otherLayer-close',function(){
        $('#otherLayer').hide();
      });
    })();
  });
})(jQuery,window,document);

// ICO 全局事件
;(function($,window,document,undefined){
  //数据表格
  $.fn.dataTable = function(option) {
    var ajaxUrl = option.ajaxUrl,
      columns = option.columns;
    listName = option.listName,
      paginateEL = option.paginateEL,
      viewSizeArr = option.viewSizeArr,
      viewSizeEL = option.viewSizeEL,
      headNotShow = option.headNotShow,
      midShowNum = option.midShowNum ? option.midShowNum : 5,
      _this = $(this);
    _this.createThead = function(){
      if(columns && columns.length){
        var thead = $("<thead></thead>");
        var thead_tr = $("<tr class='js-sort-tr'></tr>");
        $.each(columns,function(i,v){
          var title = v.title,
            code = v.code,
            sort = v.sort,
            hidden = v.hidden,
            checked = v.checked,
            width = v.width;
          //创建表头
          var th = $("<th nowrap='nowrap'></th>");
          //创建复选框
          if(checked){
            th.append("<input class='js-allcheck' type='checkbox'>");
            if(hidden){
              th.css({"display":"none"});
            }
          }else{
            th.append(title);
            //创建隐藏列
            if(hidden){
              th.css({"display":"none"});
            }
            //创建排序按钮
            if(sort){
              var sort_a = $("<a class='js-sort  fa fa-sort-amount-desc text-muted' data-key='"+code+"' href='javascript:void(0)'></a>");
              sort_a.bind('click',function(){
                var key = $(this).data('key'),
                  type = '';
                _this.find('a.js_sort').removeClass('text-muted');

                if($(this).hasClass('fa-sort-amount-desc')){
                  $(this).removeClass('fa-sort-amount-desc').addClass('fa-sort-amount-asc');
                }else{
                  $(this).removeClass('fa-sort-amount-asc').addClass('fa-sort-amount-desc');
                  type='-';
                }
                ajaxUrl = changeURLArg(ajaxUrl,"sortField",key);
                ajaxUrl = changeURLArg(ajaxUrl,"sortType",type);
                _this.reload(ajaxUrl);
              });
              th.append(sort_a);
            }
          }
          thead_tr.append(th);
        });
        thead.append(thead_tr);
        _this.append(thead);
      }
    };

    _this.createTBody = function(paramUrl,callback){
      if(paramUrl){
        ajaxUrl = paramUrl;
      }
      //异步加载列表
      $.ajax({
        url: ajaxUrl,
        type: "GET",
        dataType : "json",
        success: function(data){
          var tbody = $("<tbody></tbody>");
          if(_this.find("tbody").length){
            tbody = _this.find("tbody");
          }
          //清空table
          tbody.empty();
          //清空paginateEL
          if(paginateEL){
            $("#"+paginateEL).empty();
          }
          //清空viewSizeEL
          if(viewSizeEL){
            $("#"+viewSizeEL).empty();
          }
          var datas = data[listName];
          //是否有数据
          if(datas.length){
            if(_this.siblings('div#results').length){
              _this.siblings('div#results').hide();
            }

            //循环数据集
            $.each(datas,function(i,record){
              var tr = $("<tr></tr>");
              //创建tr中的td
              $.each(columns,function(j,key){
                var val = record[key.code]!=null?record[key.code]:"";
                var td = $("<td></td>");
                //创建复选框
                if(key.checked){
                  td.append("<input value='"+val+"' class='js-checkchild' type='checkbox'>");
                }else{
                  //自定义样式
                  if(key.custom){
                    var paramArr = key.paramField.split(",");
                    var custom = key.custom;
                    //替换参数
                    for(var i in paramArr){
                      custom = custom.replace(new RegExp("{"+paramArr[i]+"}","gm"),record[paramArr[i]]);
                    }
                    td.append(custom);
                  }else{
                    td.append(val);
                  }
                }
                //创建隐藏列
                if(key.hidden){
                  td.css({"display":"none"});
                }
                //执行方法
                if(key.handle && $.isFunction(key.handle)){
                  key.handle(td,record);
                }
                tr.append(td);
              });
              tbody.append(tr);
            });
            _this.append(tbody);

            if(!headNotShow){
              //设置标题栏的显示条数
              if($('.content-header h1 small').length > 0){
                var span = $('.content-header h1 small span');
                if(span.length == 0){
                  $('.content-header h1 small').append("<span>（共"+data.totalSize+"条）</span>");
                }else{
                  span.html("（共"+data.totalSize+"条）");
                }

              }
            }
            //设置分页工具条
            if(paginateEL){
              _this.createPaginate(data.lowIndex,data.highIndex,data.viewIndex,data.viewSize,data.totalSize);
            }
            //设置分页下拉框
            if(viewSizeEL){
              _this.createViewSize(data.viewSize);
            }
          }else{
            //无数据展示
            if(_this.siblings('div#results').length){
              _this.siblings('div#results').show();
            }else{
              var resultDiv = "<div id='results' class='col-sm-12' style='text-align: center;'><h3>无数据</h3></div>";
              _this.append(tbody);
              $(resultDiv).insertAfter(_this);
            }
          }

          //执行方法
          if(callback && $.isFunction(callback)){
            callback();
          }
        }
      });
    };

    _this.createTable = function(){
      if(columns){
        _this.createThead();
      }
      if(ajaxUrl){
        _this.createTBody();
      }
    };

    _this.reload = function(ajaxUrl,callback){
      _this.createTBody(ajaxUrl,callback);
    };

    _this.createViewSize = function(curSize){
      if(!viewSizeArr){
        viewSizeArr = [10,20,30,40];
      }
      var _viewSizeEL = $("#"+viewSizeEL);
      if(_viewSizeEL.length > 0){
        _viewSizeEL.empty();
      }
      _viewSizeEL.append("<label>每页显示</label>");
      var select = $("<select id='dp-tables_length' class='form-control input-sm'></select>");
      for(var i=0;i<viewSizeArr.length;i++){
        var option;
        if(viewSizeArr[i] == curSize){
          option = "<option value="+viewSizeArr[i]+" selected>"+viewSizeArr[i]+"</option>";
        }else{
          option = "<option value="+viewSizeArr[i]+">"+viewSizeArr[i]+"</option>";
        }
        select.append(option);
      }
      select.bind("change",function(){
        var viewSize = $(this).children('option:selected').val();
        ajaxUrl = changeURLArg(ajaxUrl,"VIEW_INDEX",0);
        ajaxUrl = changeURLArg(ajaxUrl,"VIEW_SIZE",viewSize);
        _this.reload(ajaxUrl);
      });
      _viewSizeEL.append(select);
      _viewSizeEL.append("<label>条</label>");
    };

    _this.createPaginate = function(lowIndex,highIndex,viewIndex,viewSize,totalSize){
      var _paginate = $("#"+paginateEL);
      if(_paginate.length > 0){
        _paginate.empty();
      }
      var viewDiv = "<div class='col-sm-3 dp-tables_info'>当前"+lowIndex+"至"+highIndex+"条&nbsp;共"+totalSize+"条&nbsp;</div>";
      //添加记录数显示div
      _paginate = _paginate.append(viewDiv);

      var pageBtnDiv = "<div class='dp-tables_paginate col-sm-9'></div>";
      var pageUl = "<ul class='pagination'></ul>";
      var prePage = "<li class='paginate_button previous'><a href='#'>上页</a></li>";
      var nextPage = "<li class='paginate_button next'><a href='#'>下页</a></li>";
      if(viewIndex < 1){
        prePage = $(prePage).addClass('disabled');
      }else{
        prePage = $(prePage).bind("click",function(){
          ajaxUrl = changeURLArg(ajaxUrl,"VIEW_INDEX",viewIndex-1);
          ajaxUrl = changeURLArg(ajaxUrl,"VIEW_SIZE",viewSize);
          _this.reload(ajaxUrl);

        });
      }
      pageUl = $(pageUl).append(prePage);
      //total_page:总页数
      //boundary：页数临界值
      //front_range:前段显示页码数
      //mid_range:中段显示页码数
      //rear_range后段显示页码数
      //page_size：每页记录数
      var total_page = totalSize % viewSize ==0 ? totalSize / viewSize : Math.floor(totalSize / viewSize)+1;
      var pagination_config = {total_page : total_page,current_page:(viewIndex+1), boundary : 7, front_range : 1, mid_range : midShowNum,rear_range : 1,page_size:viewSize};
      //创建分页项
      var pagintion = _this.pagintion_array(pagination_config);
      for(var i=0;i<pagintion.length;i++){
        var page_id =  pagintion[i];
        var li = "<li class='paginate_button' data-page-index="+(page_id-1)+"><a href='javascript:void(0);'>"+page_id+"</a></li>";
        //激活当前页
        if (page_id == (viewIndex + 1)) {
          li = $(li).addClass('active');
        }
        //添加跳转链接
        if(page_id != '...'){
          li = $(li).bind("click",function(){
            var curPage = $(this).data('page-index');
            ajaxUrl = changeURLArg(ajaxUrl,"VIEW_INDEX",curPage);
            ajaxUrl = changeURLArg(ajaxUrl,"VIEW_SIZE",viewSize);
            _this.reload(ajaxUrl);

          });
        }
        pageUl = $(pageUl).append(li);
      }
      if((viewIndex + 1) >= total_page){
        nextPage = $(nextPage).addClass('disabled');
      }else{
        nextPage = $(nextPage).bind("click",function(){
          ajaxUrl = changeURLArg(ajaxUrl,"VIEW_INDEX",viewIndex+1);
          ajaxUrl = changeURLArg(ajaxUrl,"VIEW_SIZE",viewSize);
          _this.reload(ajaxUrl);

        });
      }
      pageUl = $(pageUl).append(nextPage);
      pageBtnDiv = $(pageBtnDiv).append(pageUl);

      //添加跳转项
      var jumpDiv=$("<div class='dp-jumpForm'></div>");
      var jumpA = $("<a class='btn btn-danger' href='javascript:void(0)'>跳转</a>");
      jumpA.bind('click',function(){
        var gotoPage = $(this).siblings('input#jumpPage').val();
        if(gotoPage > total_page){
          gotoPage = total_page;
        }else if(gotoPage < 1){
          gotoPage = 1;
        }
        ajaxUrl = changeURLArg(ajaxUrl,"VIEW_INDEX",gotoPage-1);
        ajaxUrl = changeURLArg(ajaxUrl,"VIEW_SIZE",viewSize);
        _this.reload(ajaxUrl);
      });
      jumpDiv.append("<label>向</label>");
      jumpDiv.append("<input id='jumpPage' type='text' value="+(viewIndex+1)+" />");
      jumpDiv.append("<label>页</label>");
      jumpDiv.append(jumpA);
      pageBtnDiv = $(pageBtnDiv).append(jumpDiv);

      //添加分页按钮组
      _paginate = _paginate.append(pageBtnDiv);
    };

    _this.pagintion_array = function pagintion_array(pagination_config){
      var total_page = pagination_config.total_page,
        boundary = pagination_config.boundary,
        front_range =  pagination_config.front_range,
        mid_range = pagination_config.mid_range,
        rear_range = pagination_config.rear_range,
        current_page = pagination_config.current_page;
      var pagintion = [];

      current_page = (current_page > total_page) ? total_page : current_page;

      //总页数小于页数临界值，则显示所有页码
      if (total_page <= boundary) {
        for (i = 1; i <= total_page; i++) {
          pagintion.push(i);
        }
      } else {
        var front_end = front_range; // 前段最后一个页码
        var mid_start     = current_page - Math.ceil(parseFloat(mid_range - 1) / 2); // 中段第一个页码
        var mid_end     = current_page + ((mid_range - 1) - Math.ceil((mid_range - 1) / 2)); // 中段最后一个页码
        var rear_start     = total_page - rear_range + 1; // 后端最后一个页码
        //中段第一个页码小于等于1，中段页码往左位移
        while (mid_start <= 1) {
          if (mid_start < 1)
            mid_end += 1;
          mid_start += 1;
        }

        //中段第一个页码大于等于总页码数，中段页码往右位移
        while (mid_end >= total_page) {
          if (mid_end > total_page)
            mid_start -= 1;
          mid_end -= 1;
        }

        var first_flag=0;
        var last_flag=0;
        //取出需要显示的页码数
        for (var i = 1; i <= total_page; i++) {
          if(i <= front_end || (i >= mid_start && i <= mid_end) || i >= rear_start){
            pagintion.push(i);
          }else if((i - front_end) >=1 && i < mid_start){
            if(first_flag == 0){
              pagintion.push('...');
              first_flag = 1;
            }
          }else if(i > mid_end && (rear_start-i) >=1){
            if(last_flag == 0){
              pagintion.push('...');
              last_flag = 1;
            }
          }
        }
      }
      return pagintion;
    };

    _this.createTable();

    return _this;
  };
})(jQuery,window,document);

//自定义JS，开发人员用
;(function($,window,document,undefined){
  //数据查询弹出框
  $.extend({
    dataSelectModal : function(option) {
      var _selectId= option.selectId,
        _selectName= option.selectName,
        _title = option.title,
        _url = option.url,
        _selectCallBack = option.selectCallBack,
        _width = option.width,
        _height = option.height,
        _multi = option.multi,
        _this;
      if($('body').find('#modal_data_select.modal').length > 0){
        _this = $('body').find('#modal_data_select.modal');
        _this.find('#modal_edit_title').html(_title);
      }else{
        _this =  $("<div id='modal_data_select'  class='modal fade' tabindex='-1' role='dialog' aria-labelledby='modal_data_select_title'>"+
          "<div class='modal-dialog' role='document'>"+
          "<div class='modal-content'>"+
          "<div class='modal-header'>"+
          "<button type='button' class='close' data-dismiss='modal' aria-label='Close'><span aria-hidden='true'>&times;</span></button>"+
          "<h4 class='modal-title' id='modal_edit_title'>"+_title+"</h4>"+
          "</div>"+
          "<div class='modal-body'>"+
          "</div>"+
          "</div>"+
          "</div>"+
          "</div>");
      }
      //设置宽度
      if(_width){
        _this.find('.modal-dialog').css({"width":_width+"px"});
      }
      //设置高度
      if(_height){
        _this.find('.modal-dialog').css({"height":_height+"px"});
      }

      //加载数据页面，打开弹出框
      $.get(_url,'', function(data){
        _this.find('.modal-body').html(data);
        if(_multi){
          //初始化选中结果集的id和name
          if(_selectId && _selectName){
            var id_arr = $("#"+_selectId).val().split(','),
              name_arr = $("#"+_selectName).val().split(',');
            $.each(id_arr,function(i,v){
              var id = v,
                name = name_arr[i];
              if(id){
                addResultsItem(id,name);
              }
            });
          }
        }
        _this.modal();
      });
      //选中的id结果集
      var select_ids = new Array();
      //选中的name结果集
      var select_names = new Array();
      //查询选择框打开后的事件
      _this.on('shown.bs.modal', function () {
        if(_multi){
          //先解绑点击事件，保持只绑定一次
          $(document).off("click",'#modal_data_select .js-allcheck');
          //表格的复选框全选点击事件
          $(document).on("click",'#modal_data_select .js-allcheck',function(e){
            var is_checked = $(this).prop('checked');
            var $parent = $(this).closest('.js-checkparent');
            $.each($parent.find('.js-checkchild'),function(i,v){
              var id = $(v).val(),
                name = $(v).data("name");
              //根据全选框选中状态，添加或删除选中结果集
              if(is_checked){
                addResultsItem(id,name);
              }else{
                removeResultsItem(id);
              }
            });
          });

          //先解绑点击事件，保持只绑定一次
          $(document).off("click",'#modal_data_select .js-checkchild');
          //表格的复选框点击事件
          $(document).on("click",'#modal_data_select .js-checkchild',function(e){
            var id = $(e.target).val(),
              name = $(e.target).data("name"),
              is_checked = $(e.target).prop("checked");
            //根据复选框选中状态，添加或删除选中结果集
            if(is_checked){
              addResultsItem(id,name);
            }else{
              removeResultsItem(id);
            }
          });

          //先解绑点击事件，保持只绑定一次
          $(document).off("click",'#modal_data_select .select-info .icon-del');
          //表格的复选框点击事件
          $(document).on("click",'#modal_data_select .select-info .icon-del',function(e){
            var id = $(e.target).closest('li').attr("id");
            removeResultsItem(id);
            _this.find('.js-checkchild:checkbox[value="'+id+'"]').prop('checked',false);
          });
        }
        //先解绑点击事件，保持只绑定一次
        $(document).off("click",'#modal_data_select .btn-select');
        //再给内容页的选择按钮添加点击事件
        $(document).on("click",'#modal_data_select .btn-select',function(e){
          //当前弹出框隐藏
          _this.modal('hide');
          if(_multi){
            $(this).attr("data-id",select_ids.join(","));
            $(this).attr("data-name",select_names.join(","));
          }else{
            select_ids.add($(this).data('id'));
            select_names.add($(this).data('name'));
          }
          //选择项ID
          if(_selectId){
            $("#"+_selectId).val(select_ids.join(","));
          }
          //选择项名称
          if(_selectName){
            $("#"+_selectName).val(select_names.join(","));
            $("#"+_selectName).html(select_names.join(","));
          }
          //调用点击回调方法
          if(_selectCallBack && $.isFunction(_selectCallBack)){
            _selectCallBack($(this));
          }
        });
      });

      //查询选择框关闭事件
      _this.on('hide.bs.modal', function () {
        _this.find('.modal-body').empty();
      });

      //添加选中结果
      function addResultsItem(id,name){
        var select_results = _this.find('.select-info');
        if(!select_ids.contains(id)){
          select_ids.add(id);
          select_names.add(name);
        }
        if(select_results.find('li#'+id).length==0){
          var item_li = "<li id="+id+" class='current' title='"+name+"'><span class='icon-del'>×</span>"+name+"</li>";
          select_results.find('ul').append(item_li);
        }
        //结果展示区域展示或隐藏
        if(select_ids.isEmpty()){
          select_results.hide();
        }else{
          select_results.show();
        }
      }

      //删除选中结果
      function  removeResultsItem(id){
        var select_results = _this.find('.select-info');
        if(!select_ids.isEmpty() && !select_names.isEmpty()){
          var index = select_ids.indexOf(id);
          if(index >= 0){
            select_ids.removeAt(index);
            select_names.removeAt(index);
          }
        }
        if(select_results.find('li#'+id).length > 0){
          select_results.find('ul').find('li#'+id).remove();
        }

        //结果展示区域展示或隐藏
        if(select_ids.isEmpty()){
          select_results.hide();
        }else{
          select_results.show();
        }
      }
    }
  });


    $.extend({
        dataSelectModal2 : function(option) {
            var _selectId= option.selectId,
                _selectName= option.selectName,
                _title = option.title,
                _url = option.url,
                _selectCallBack = option.selectCallBack,
                _width = option.width,
                _height = option.height,
                _multi = option.multi,
                _this;
            if($('body').find('#modal_data_select.modal').length > 0){
                _this = $('body').find('#modal_data_select.modal');
                _this.find('#modal_edit_title').html(_title);
            }else{
                _this =  $("<div id='modal_data_select'  class='modal fade' tabindex='-1' role='dialog' aria-labelledby='modal_data_select_title'>"+
                    "<div class='modal-dialog' role='document'>"+
                    "<div class='modal-content'>"+
                    "<div class='modal-header'>"+
                    "<button type='button' class='close' data-dismiss='modal' aria-label='Close'><span aria-hidden='true'>&times;</span></button>"+
                    "<h4 class='modal-title' id='modal_edit_title'>"+_title+"</h4>"+
                    "</div>"+
                    "<div class='modal-body'>"+
                    "</div>"+
                    "</div>"+
                    "</div>"+
                    "</div>");
            }
            //设置宽度
            if(_width){
                _this.find('.modal-dialog').css({"width":_width+"px"});
            }
            //设置高度
            if(_height){
                _this.find('.modal-dialog').css({"height":_height+"px"});
            }

            //加载数据页面，打开弹出框
            $.get(_url,'', function(data){
                _this.find('.modal-body').html(data);
                if(_multi){
                    //初始化选中结果集的id和name
                    if(_selectId && _selectName){
                        var id_arr = $("#"+_selectId).val().split(','),
                            name_arr = $("#"+_selectName).val().split(',');
                        $.each(id_arr,function(i,v){
                            var id = v;
                            var name = name_arr[i];
                            if(id){
                                addResultsItem(id,name);
                            }
                        });
                    }
                }

                _this.modal();

                //
                // _this.on('shown.bs.modal', function () {
                //     var $childs = $(".js-checkchild");
                //     $(".js-checkchild").each(function(){
                //         var id = $(this).val(),
                //             name = $(this).data("name");
                //         $.ajax({
                //             url: "/prodPromo/control/checkGoodIsUsed",
                //             type: "POST",
                //             data:{
                //                 id:id
                //             },
                //             dataType: "json",
                //             success: function (data) {
                //                 if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                //                     $.tipLayer(data._ERROR_MESSAGE_);
                //                 }else{
                //                     if(data.isExisted=="Y"){
                //                         //说明该商品正在被其他团购秒杀直降在使用
                //                         console.log("111")
                //                         $(this).attr("disabled","disabled");
                //                         $(this).attr("checked", false);
                //                     }
                //                 }
                //             },
                //             error: function (data) {
                //                 $.tipLayer("操作失败！");
                //             }
                //         });
                //     });
                // })

            });
            //选中的id结果集
            var select_ids = new Array();
            //选中的name结果集
            var select_names = new Array();
            //查询选择框打开后的事件
            _this.on('shown.bs.modal', function () {
                if(_multi){
                    //先解绑点击事件，保持只绑定一次
                    $(document).off("click",'#modal_data_select .js-allcheck');
                    //表格的复选框全选点击事件
                    $(document).on("click",'#modal_data_select .js-allcheck',function(e){
                        var is_checked = $(this).prop('checked');
                        var $parent = $(this).closest('.js-checkparent');
                        $.each($parent.find('.js-checkchild'),function(i,v){
                            var id = $(v).val(),
                                name = $(v).data("name");
                            //根据全选框选中状态，添加或删除选中结果集
                            //校验是否已经被使用
                            if(is_checked){
                                $.ajax({
                                    url: "/prodPromo/control/checkGoodIsUsed",
                                    type: "POST",
                                    data:{
                                        id:id
                                    },
                                    dataType: "json",
                                    success: function (data) {
                                        if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                                            $.tipLayer(data._ERROR_MESSAGE_);
                                        }else{
                                            if(data.isExisted=="Y"){
                                                //说明该商品正在被其他团购秒杀直降在使用
                                                $(v).attr("disabled","disabled");
                                                $(v).attr("checked", false);
                                            }else{
                                                addResultsItem(id,name);

                                            }
                                        }
                                    },
                                    error: function (data) {
                                        $.tipLayer("操作失败！");
                                    }
                                });
                            }else{
                                removeResultsItem(id);
                            }
                        });
                    });

                    //先解绑点击事件，保持只绑定一次
                    $(document).off("click",'#modal_data_select .js-checkchild');
                    //表格的复选框点击事件
                    $(document).on("click",'#modal_data_select .js-checkchild',function(e){
                        var id = $(e.target).val(),
                            name = $(e.target).data("name"),
                            is_checked = $(e.target).prop("checked");
                        //根据复选框选中状态，添加或删除选中结果集
                        //校验同一件商品只能参加直降、秒杀、拼团其中一种促销活动，之前如果参加过就不能参加了。
                        if(is_checked){
                            $.ajax({
                                url: "/prodPromo/control/checkGoodIsUsed",
                                type: "POST",
                                data:{
                                    id:id
                                },
                                dataType: "json",
                                success: function (data) {
                                    if(data.hasOwnProperty("_ERROR_MESSAGE_")){
                                        $.tipLayer(data._ERROR_MESSAGE_);
                                    }else{
                                        if(data.isExisted=="Y"){
                                            //说明该商品正在被其他团购秒杀直降在使用
                                            $(e.target).attr("disabled","disabled");
                                            $(e.target).attr("checked", false);
                                        }else{
                                            addResultsItem(id,name);

                                        }
                                    }
                                },
                                error: function (data) {
                                    $.tipLayer("操作失败！");
                                }
                            });
                        }else{
                            removeResultsItem(id);
                        }



                    });

                    //先解绑点击事件，保持只绑定一次
                    $(document).off("click",'#modal_data_select .select-info .icon-del');
                    //表格的复选框点击事件
                    $(document).on("click",'#modal_data_select .select-info .icon-del',function(e){
                        var id = $(e.target).closest('li').attr("id");
                        removeResultsItem(id);
                        _this.find('.js-checkchild:checkbox[value="'+id+'"]').prop('checked',false);
                    });
                }
                //先解绑点击事件，保持只绑定一次
                $(document).off("click",'#modal_data_select .btn-select');
                //再给内容页的选择按钮添加点击事件
                $(document).on("click",'#modal_data_select .btn-select',function(e){
                    //当前弹出框隐藏
                    _this.modal('hide');
                    if(_multi){
                        $(this).attr("data-id",select_ids.join(","));
                        $(this).attr("data-name",select_names.join(","));
                    }else{
                        select_ids.add($(this).data('id'));
                        select_names.add($(this).data('name'));
                    }
                    //选择项ID
                    if(_selectId){
                        $("#"+_selectId).val(select_ids.join(","));
                    }
                    //选择项名称
                    if(_selectName){
                        $("#"+_selectName).val(select_names.join(","));
                        $("#"+_selectName).html(select_names.join(","));
                    }
                    //调用点击回调方法
                    if(_selectCallBack && $.isFunction(_selectCallBack)){
                        _selectCallBack($(this));
                    }
                });
            });

            //查询选择框关闭事件
            _this.on('hide.bs.modal', function () {
                _this.find('.modal-body').empty();
            });

            //添加选中结果
            function addResultsItem(id,name){
                var select_results = _this.find('.select-info');
                if(!select_ids.contains(id)){
                    select_ids.add(id);
                    select_names.add(name);
                }
                if(select_results.find('li#'+id).length==0){
                    var item_li = "<li id="+id+" class='current' title='"+name+"'><span class='icon-del'>×</span>"+name+"</li>";
                    select_results.find('ul').append(item_li);
                }
                //结果展示区域展示或隐藏
                if(select_ids.isEmpty()){
                    select_results.hide();
                }else{
                    select_results.show();
                }
            }

            //删除选中结果
            function  removeResultsItem(id){
                var select_results = _this.find('.select-info');
                if(!select_ids.isEmpty() && !select_names.isEmpty()){
                    var index = select_ids.indexOf(id);
                    if(index >= 0){
                        select_ids.removeAt(index);
                        select_names.removeAt(index);
                    }
                }
                if(select_results.find('li#'+id).length > 0){
                    select_results.find('ul').find('li#'+id).remove();
                }

                //结果展示区域展示或隐藏
                if(select_ids.isEmpty()){
                    select_results.hide();
                }else{
                    select_results.show();
                }
            }
        }
    });

  /**************数组扩展方法***************/
  //添加
  Array.prototype.add = function(item) {
    this.push(item);
  };
  //批量添加
  Array.prototype.addRange = function(items) {
    var length = items.length;

    if (length != 0) {
      for (var index = 0; index < length; index++) {
        this.push(items[index]);
      }
    }
  };
  //清空
  Array.prototype.clear = function() {
    if (this.length > 0) {
      this.splice(0, this.length);
    }
  };
  //是否为空
  Array.prototype.isEmpty = function() {
    if (this.length == 0)
      return true;
    else
      return false;
  };
  //复制
  Array.prototype.clone = function() {
    var clonedArray = [];
    var length = this.length;

    for (var index = 0; index < length; index++) {
      clonedArray[index] = this[index];
    }

    return clonedArray;
  };
  //是否包含某项
  Array.prototype.contains = function(item) {
    var index = this.indexOf(item);
    return (index >= 0);
  };
  //移除第一个元素
  Array.prototype.dequeue = function() {
    return this.shift();
  };
  //数组项索引
  Array.prototype.indexOf = function(item) {
    var length = this.length;

    if (length != 0) {
      for (var index = 0; index < length; index++) {
        if (this[index] == item) {
          return index;
        }
      }
    }

    return -1;
  };
  //插入指定位置
  Array.prototype.insert = function(index, item) {
    this.splice(index, 0, item);
  };
  //加入分隔符,返回字符串
  Array.prototype.joinstr = function(str) {
    var new_arr = new Array(this.length);
    for (var i = 0; i < this.length; i++) {
      new_arr[i] = this[i] + str;
    }
    return new_arr;
  };

  Array.prototype.queue = function(item) {
    this.push(item);
  };
  //指定删除某项
  Array.prototype.remove = function(item) {
    var index = this.indexOf(item);

    if (index >= 0) {
      this.splice(index, 1);
    }
  };
  //根据索引删除
  Array.prototype.removeAt = function(index) {
    this.splice(index, 1);
  };
})(jQuery,window,document);
//URL参数编辑
function changeURLArg(url,arg,arg_val){
  var pattern=arg+'=([^&]*)';
  var replaceText=arg+'='+arg_val;
  if(url.match(pattern)){
    var tmp='/('+ arg+'=)([^&]*)/gi';
    tmp=url.replace(eval(tmp),replaceText);
    return tmp;
  }else{
    if(url.match('[\?]')){
      return url+'&'+replaceText;
    }else{
      return url+'?'+replaceText;
    }
  }
  return url+'\n'+arg+'\n'+arg_val;
};

//form表单文本清空
function clearForm(form) {
  $(':input', form).each(function() {
    var type = this.type;
    var tag = this.tagName.toLowerCase();
    if (type == 'text' || type == 'password' || tag == 'textarea')
      this.value = "";
    else if (type == 'checkbox' || type == 'radio')
      this.checked = false;
    else if (tag == 'select')
      this.selectedIndex = -1;
  });
};

//时间格式化
function timeStamp2String(time){
  var datetime = new Date();
  datetime.setTime(time);
  var year = datetime.getFullYear();
  var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
  var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
  var hour = datetime.getHours()< 10 ? "0" + datetime.getHours() : datetime.getHours();
  var minute = datetime.getMinutes()< 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
  var second = datetime.getSeconds()< 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
  return year + "-" + month + "-" + date+" "+hour+":"+minute+":"+second;
};
function timeStamp2String2(time){
    var datetime = new Date();
    datetime.setTime(time);
    var year = datetime.getFullYear();
    var month = datetime.getMonth() + 1 < 10 ? "0" + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
    var date = datetime.getDate() < 10 ? "0" + datetime.getDate() : datetime.getDate();
    var hour = datetime.getHours()< 10 ? "0" + datetime.getHours() : datetime.getHours();
    var minute = datetime.getMinutes()< 10 ? "0" + datetime.getMinutes() : datetime.getMinutes();
    var second = datetime.getSeconds()< 10 ? "0" + datetime.getSeconds() : datetime.getSeconds();
    return year + "-" + month + "-" + date+" "+hour+":"+minute;
};

// 提示弹窗
function tipLayer(option) {
    var defaultOption = option || {msg: ''},
        message = defaultOption.msg || '';
    target = option.target;
    if (!$('#tipLayer').size()) {
        var tipLayerContent = '<div id="tipLayer" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">'
            + '<div class="modal-dialog modal-sm">'
            + '<div class="modal-content">'
            + '<div class="modal-header">'
            + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">×</span></button>'
            + '<h4 class="modal-title" id="mySmallModalLabel">提示</h4></div>'
            + '<div class="modal-body tipLayerMsg"></div>'
            + '</div></div></div>';

        /*if ($(target).parents(".box")) {
         if ($(target).parents(".box").children('.box-body')) {
         $(target).parents(".box").children('.box-body').append(tipLayerContent);
         }
         }else if($(target).parents(".ibox")) {
         if ($(target).parents(".ibox").children('.ibox-content')) {
         $(target).parents(".ibox").children('.ibox-content').append(tipLayerContent);
         }
         }
         else if ($(target).parents(".panel").children('.panel-body')) {
         $(target).parents(".panel").children('.panel-body').append(tipLayerContent);
         } else {
         $('body').append(tipLayerContent);
         }*/
        $('body').append(tipLayerContent);
    }
    $('#tipLayer').modal('show').find('.tipLayerMsg').html(message);
}
// 确认弹窗
function confirmLayer(option) {
    var defaultOption = option || {msg: '', title: ''},
        msg = defaultOption.msg || '';
    title = defaultOption.title || '';
    target = option.target;
    if (!$('#confirmLayer').size()) {
        var confirmLayerContent = '<div id="confirmLayer" name="confirmLayer" class="modal fade">'
            + '<div class="modal-dialog">'
            + '<div class="modal-content">'
            + '<div class="modal-header">'
            + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
            + '<h4 class="modal-title confirmLayerTitle">确认</h4></div>'
            + '<div class="modal-body confirmLayerMsg"></div>'
            + '<div class="modal-footer">'
            + '<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>'
            + '<button type="button" class="btn btn-primary confirm-btn">确认</button></div></div></div></div>';
        $(target).after(confirmLayerContent);
        /*if ($(target).parents(".box")) {
         if ($(target).parents(".box").children('.box-body')) {
         $(target).parents(".box").children('.box-body').append(confirmLayerContent);
         }
         }else if($(target).parents(".ibox")) {
         if ($(target).parents(".ibox").children('.ibox-content')) {
         $(target).parents(".ibox").children('.ibox-content').append(confirmLayerContent);
         }
         }
         else if ($(target).parents(".panel").children('.panel-body')) {
         $(target).parents(".panel").children('.panel-body').append(confirmLayerContent);
         } else {
         $('body').append(confirmLayerContent);
         }*/


        /* if ($(target).parents(".panel").children('.panel-body')) {
         $(target).parents(".panel").children('.panel-body').append(confirmLayerContent);
         } else {
         $('body').append(confirmLayerContent);
         }*/
        $('body').append(confirmLayerContent);
    }

    $('#confirmLayer').find('.confirmLayerTitle').html(title);
    $('#confirmLayer').modal('show').find('.confirmLayerMsg').html(msg);

    if (typeof defaultOption.confirm === 'function') {
        var obj = $('.confirm-btn', '#confirmLayer');
        $(obj).click(option.confirm);
    }
}

function modalLayer(option) {
    var defaultOption = option || {title: '', msg: '', bodyUrl: '', queryArgs: ''},
        msg = defaultOption.msg || '';
    title = defaultOption.title || '';
    bodyUrl = defaultOption.bodyUrl || '';
    queryArgs = defaultOption.queryArgs || '';
    target = option.target;
    modalType = option.modalType;
    modalStyle = option.modalStyle;
    var modalLayerContent = '<div id="modalLayer" class="modal inmodal fade" name="modalLayer">'
        + '<div class="modal-dialog ' + modalStyle + '">'
        + '<div class="modal-content animated">'
        + '<div class="modal-header">'
        + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
        + '<h4 class="modal-title modalLayerTitle">确认</h4></div>'
        + '<div class="modal-body"></div>'
        + '<div class="modal-footer">';

    if (modalType === 'form-submit') {
        modalLayerContent += '<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>'
            + '<button type="button" id="submitBtn" class="btn btn-primary modal-btn">确认</button></div></div></div></div>';
    } else if (modalType === "view") {
        modalLayerContent += '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>';
    }
    //$(target).after(modalLayerContent);
    /* if ($(target).parents(".panel").children('.panel-body')) {
     $(target).parents(".panel").children('.panel-body').find('#modalLayer').remove().end().append(modalLayerContent);
     } else {
     $('body').find('#modalLayer').remove().end().append(modalLayerContent);
     }*/

    /*if ($(target).parents(".box")) {
     if ($(target).parents(".box").children('.box-body')) {
     $(target).parents(".box").children('.box-body').append(modalLayerContent);
     }
     }else if($(target).parents(".ibox")) {
     if ($(target).parents(".ibox").children('.ibox-content')) {
     $(target).parents(".ibox").children('.ibox-content').append(modalLayerContent);
     }
     }
     else if ($(target).parents(".panel").children('.panel-body')) {
     $(target).parents(".panel").children('.panel-body').append(modalLayerContent);
     } else {
     $('body').append(modalLayerContent);
     }*/
    $('body').append(modalLayerContent);

    $('#modalLayer').find('.modalLayerTitle').text(title);
    if (bodyUrl !== '') {
        $.ajax({
            type: "post",
            url: bodyUrl,
            data: queryArgs,
            timeout: AJAX_REQUEST_TIMEOUT,
            cache: false,
            dataFilter: function (data, dataType) {
                waitSpinnerHide();
                return data;
            },
            success: function (data) {
                $('#modalLayer').modal('show').find('.modal-body').html(data);
                //bootstrap modal-btn 增加form 提交disabled事件
                if ($('#modalLayer form')) {
                    var modalForm = $('#modalLayer form');
                    //取消form 提交 onsubmit 事件
                    $(modalForm).unbind("onsubmit");
                    //删除from中的submit元素
                    if ((modalType === 'form-submit')) {
                        modalRemoveSubmit(modalForm);
                    }
                }
                if (typeof defaultOption.confirm === 'function' && modalType === 'form-submit') {
                    var obj = $('#submitBtn').on('click', option.confirm);
                }

            },
            error: function (xhr, reason, exception) {
                if (exception != 'abort') {
                    alert("An error occurred while communicating with the server:\n\n\nreason=" + reason + "\n\nexception=" + exception);
                }
                location.reload(true);
            }
        });
    }
    $('#modalLayer').on('shown.bs.modal', function () {
        /*var form = $("#modalLayer form:first");
         if ($(form).attr('data-parsley-validate')) {
         $(form).parsley().on("form:success", function () {
         doSubmit(form, target);
         })
         }*/
    });

    $('#modalLayer').on('hide.bs.modal', function () {
        if ($('#modalLayer')) {
            $('#modalLayer').remove();
        }
    });

    $("#modalLayer").on("hidden.bs.modal", function () {
        if (modalType === 'form-submit') {
            var form = $("#modalLayer form:first");
            if ($(form).attr('data-parsley-validate')) {
                $(form).parsley().off("form:success")
            }
            $(this).removeData();
            $('#submitBtn').off('click');
        }
    });
}

function lookupModalLayer(option) {
    var defaultOption = option || {title: '', msg: '', id: ''},
        msg = defaultOption.msg || '';
    title = defaultOption.title || '';
    id = defaultOption.id || 'lookupModal';
    target = option.target;
    if (!$('#' + id).size()) {
        var modalLayerContent = '<div id=' + id + ' class="modal fade" name="lookupModal">'
            + '<div class="modal-dialog">'
            + '<div class="modal-content">'
            + '<div class="modal-header">'
            + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
            + '<h4 class="modal-title modalLayerTitle">查找页面</h4></div>'
            + '<div class="modal-body"></div>'
            + '<div class="modal-footer">'
            + '</div></div></div></div>';
        //if($(target)) {
        //    $(target).append(modalLayerContent);
        //}else{
        //$(target).append(modalLayerContent);
        $('body').append(modalLayerContent);
        //}
        $('#lookupModal').find('.modalLayerTitle').text(title);
    }
}
// 获取IE版本，若非IE则返回0
function getIeVersion() {
    var agent = navigator.userAgent.toLowerCase(),
        regStr_ie = /msie [\d.]+;/gi,
        browser = '';
    //IE
    if (agent.indexOf("msie") > 0) browser = agent.match(regStr_ie);

    return +(browser).replace(/[^0-9.]/ig, '');
}
