/******************************************************************图片上传 start*****************************************************/

var chars = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

function generateMixed(n) {
  var res = "";
  for (var i = 0; i < n; i++) {
    var id = Math.ceil(Math.random() * 35);
    res += chars[id];
  }
  return res;
}

/*************************************************商品主图上传START*******************************************************/
var mainFlag;

function mainImg_uploadFile(obj) {
  mainFlag = $(obj).attr("id");
  $(obj).prev("span").addClass("_loading");
  var paramo = {"bid": bid, "upload_type": type_product, "num": "0", "file_type": "jpg", "file_size": "5"};
  _uploadFile(obj, paramo, "_mainImg_uploadFileAfter");
}

function _mainImg_uploadFileAfter(data) {
  mainImg_uploadFileAfter(data, "mainImg_uploadBtn");
}

function mainImg_uploadFileAfter(data, htmlflag) {
  if (data != "") {
    if ("0" == data.fileState) {
      $("#" + mainFlag).parents("li:first").find("img").attr("src", data.fileUrl + "?" + generateMixed(6));
      $("#" + mainFlag).parents("li:first").find("a").attr("href", data.fileUrl + "?" + generateMixed(6));
      $("#" + mainFlag).parents("div:first").addClass("hidden");
      $("#" + mainFlag).parents("li:first").find("div:last").removeClass("hidden");
      var _imgsku = $("#" + mainFlag).parents("ul:first").attr("imgsku");
      $("#sku_body tr").each(function () {
        var property_id = $(this).find("input[id^='property_id_']").val();
        if (_imgsku == 'all') {
          $(this).find("input[id^='mainImg_']").val(data.fileName);
        } else if (property_id.indexOf(_imgsku) >= 0) {
          $(this).find("input[id^='mainImg_']").val(data.fileName);
        }
      })

    } else {
      alert(data.fileMessage);
    }
  }
  $("#" + mainFlag).prev("span").removeClass("_loading");
}

/*************************************************商品主图上传END*******************************************************/

/*************************************************商品SKU图上传START*******************************************************/
var up_skuimg_num = 0;
var up_skuimg_id;

function skuImg_uploadFile(obj, num) {
  $(obj).prev("span").addClass("_loading");
  up_skuimg_num = num;
  up_skuimg_id = $(obj).attr("id");
  var paramo = {"bid": bid, "upload_type": type_product, "num": num, "file_type": "jpg", "file_size": "5"};
  _uploadFile(obj, paramo, "_skuImg_uploadFileAfter");
}

function _skuImg_uploadFileAfter(data) {
  skuImg_uploadFileAfter(data, "sku_img_");
}

function skuImg_uploadFileAfter(data, htmlflag) {
  if (data != "") {
    if ("0" == data.fileState) {
      $("#" + up_skuimg_id).parents("li:first").find("img").attr("src", data.fileUrl + "?" + generateMixed(6));
      $("#" + up_skuimg_id).parents("li:first").find("a").attr("href", data.fileUrl + "?" + generateMixed(6));
      $("#" + up_skuimg_id).parents("li:first").find("div:first").addClass("hidden");
      $("#" + up_skuimg_id).parents("li:first").find("div:last").removeClass("hidden");
      var _imgsku = $("#" + up_skuimg_id).parents("ul:first").attr("imgsku");
      $("#sku_body tr").each(function () {
        var property_id = $(this).find("input[id^='property_id_']").val();
        if (_imgsku == 'all') {
          $(this).find("input[id^='skuImg_" + up_skuimg_num + "_']").val(data.fileName);
        } else if (property_id.indexOf(_imgsku) >= 0) {
          $(this).find("input[id^='skuImg_" + up_skuimg_num + "_']").val(data.fileName);
        }
      })
    } else {
      alert(data.fileMessage);
    }
  }
  $("#" + up_skuimg_id).prev("span").removeClass("_loading");
}

/*************************************************商品SKU图上传END*******************************************************/