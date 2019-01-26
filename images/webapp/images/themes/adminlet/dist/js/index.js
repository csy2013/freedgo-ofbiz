$(function(){
	$(document).on("click","#add, .add-child, .del",function(){
		no=$(this).closest(".root").children(".number").text();
		
		if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('first')){
			 a=1; //1表示第一级添加子分类
			 $(this).closest(".root").addClass("cur")
		}else if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('second')){
			 a=2; //2表示第二级添加子分类
			 $(this).closest(".root").addClass("cur")
		}else if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('third')){
			 a=3; //3表示第三级
		}else{
			 a=0 //0表示添加
		}
	})
	
	$(document).on("click",".del, .up, .down",function(){
		
		if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('first')){
			 a=1; //1表示第一级添加子分类
		}else if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('second')){
			 a=2; //2表示第二级添加子分类
		}else if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('third')){
			 a=3; //3表示第三级
		}else{
			 a=0 //0表示添加
		}
	});


	//添加一级分类弹框关闭事件
	$('#exampleModal').on('hide.bs.modal', function () {
		$(".name").removeClass('cur');
		$('#addFirstForm').dpValidate({
			clear: true
		});
	})
	//添加一级分类提交按钮点击事件
	$('.btn_save1').click(function(){
		$('#addFirstForm').dpValidate({
			clear: true
		});
		$('#addFirstForm').submit();
	});
	//表单校验
	$('#addFirstForm').dpValidate({
		validate:true,
		console:true,
		callback: function(){
			$("#zIndex").val(a);
			if(a==0){
				var para_num=parseInt($("ul.first").length+1);
			}else if(a==1){
				var para_num=no+"."+parseInt($(".cur").children('ul').length+1);
			}else if(a==2){
				var para_num=no+"."+parseInt($(".cur").children('ul').length+1);
			}
			var isEnable = $("#exampleModal input[name='isEnabled'][checked]").attr("value");
			var buttonstr = "";
			if (isEnable == "0"){
				buttonstr = '<button type="button" class="btn btn-primary btn-sm btn-sm" value="0" onclick="changeNavigation(this,\'isEnable\')">否</button>';
			}else{
				buttonstr = '<button type="button" class="btn btn-primary btn-sm btn-sm" value="1" onclick="changeNavigation(this,\'isEnable\')">是</button>';
			}
			html="<i class='fold  glyphicon glyphicon-minus-sign' style='visibility:hidden'></i><span class='name'>"+$("#exampleModal").find('input[name=navigationName]').val()+"</span>" +
			'<span class="img"><img alt="" src="/content/control/getImage?contentId=' + $("#exampleModal").find('input[name=contentId]').val() +'" class="cssImgSmall"></span>' +
			"<span class='enable'>" + buttonstr + "</span>" +
			"<span class='number'>"+para_num+"</span>" +
			"<span class='handle'><div class='btn-group'><button type='button' class='js-button btn btn-danger btn-sm xl_bj edit' data-toggle='modal' data-target='#exampleModal'>编辑</button><button type='button' class='btn btn-danger btn-sm dropdown-toggle' data-toggle='dropdown'><span class='caret'></span><span class='sr-only'>Toggle Dropdown</span></button><ul class='dropdown-menu' role='menu'><li class='del'><a class='gss_delete first_delete'>删除</a></li><li class='add-child'><a class='gss-button' data-toggle='modal' data-target='#exampleModal'>添加子分类</a></li><li class='up'><a class='up-button'>上移</a></li><li class='down'><a class='down-button'>下移</a></li></ul></div></span>"

			if(a == 4){
				//编辑
				$.ajax({
					url: "updateNavigation",
					type: "POST",
					data: $('#addFirstForm').serialize(),
					success: function(data){
						/*$(".cur").text($("#navigationName").val());
						 $(".name").removeClass('cur');*/
						if (data.flag == "S"){
							var obj = $("#"+data.id);
							obj.attr("navigationName",data.navigationName);
							obj.attr("productCategory",data.productCategory);
							obj.attr("contentId",data.contentId);
							obj.attr("isEnabled",data.isEnabled);
							$(".cur").text(data.navigationName);
							$(".cur").next().find('img').attr("src","/content/control/getImage?contentId=" + data.contentId);
							var str = "";
							if (data.isEnabled == "1"){
								str = "是";
							}else{
								str = "否";
							}
							$(".cur").next().next().find('button').text(str);
							$(".cur").next().next().find('button').attr("value",data.isEnabled);
							$(".name").removeClass('cur');
							$("#idd").val("");
						}
						$('#exampleModal').modal('toggle');
					},
					 error: function(data){
						 //隐藏弹出窗口
						 $('#exampleModal').modal('toggle');
						 update = 0;
					 }
				 });
				/*$(".cur").text($("#navigationName").val());
				$(".name").removeClass('cur');*/

			}else if(a==0){
				$("#parentId").val("");
				$("#idd").val("");
				//添加按钮
				$.ajax({
				 url: "updateNavigation",
				 type: "POST",
				 data:  $('#addFirstForm').serialize(),
				 success: function(data){
					 if (data.flag == "S"){
						 $(".main").append("<ul class='first root' id='" + data.id + "'navigationName='" + data.navigationName + "' contentId='" +data.contentId  + "' productCategory='" +data.productCategory+ "' isEnabled='"+data.isEnabled +"' >" + html + "</ul>");
						 var link = '<li><a class="gss_delete first_delete " href="navigationBrand?navigationId=' + data.id + '&navigationGroupId=' + $("#navigationGroupId").val() + '">设置品牌</a></li>' +
						 '<li><a class="gss_delete first_delete " href="navigationBannerEditPage?navigationId=' + data.id + '&navigationGroupId=' + $("#navigationGroupId").val() + '">设置广告</a></li>';
						 $("#" + data.id ).closest("ul").find(".dropdown-menu").prepend(link);
					 }
					 $('#exampleModal').modal('toggle');
				 },
				 error: function(data){
				 	//隐藏弹出窗口
				 	$('#exampleModal').modal('toggle');
				 }
				 });
				//$(".main").append("<ul class='first root'>"+html+"</ul>");
			}else if(a==1){
				$("#parentId").val($(".cur").closest(".root").attr('id'));
				$.ajax({
				 url: "updateNavigation",
				 type: "POST",
				 data: $('#addFirstForm').serialize(),
				 success: function(data){
					 if (data.flag == "S") {
						 $(".cur").append("<ul class='second root'id='" + data.id + "'navigationName='" + data.navigationName + "' contentId='" +data.contentId  + "' productCategory='" +data.productCategory+ "' isEnabled='"+data.isEnabled +"' >" + html + "</ul>");
						 if ($(".cur").children("ul").length > 0) {
							 $(".cur").children(".fold").css("visibility", "visible")
						 }
						 $("ul").removeClass('cur')
					 }
					 $('#exampleModal').modal('toggle');
				 },
				 error: function(data){
					 //隐藏弹出窗口
					 $('#exampleModal').modal('toggle');
				 }
				 });
			}else if(a==2){
				$("#parentId").val($(".cur").closest(".root").attr('id'));
				$("#idd").val("");
				$.ajax({
				 url: "updateNavigation",
				 type: "POST",
				 data: $('#addFirstForm').serialize(),
				 success: function(data){
					 if (data.flag == "S") {
						 $(".cur").append("<ul class='third root'id='" + data.id + "'navigationName='" + data.navigationName + "' contentId='" +data.contentId  + "' productCategory='" +data.productCategory+ "' isEnabled='"+data.isEnabled +"' >" + html + "</ul>");
						 if ($(".cur").children("ul").length > 0) {
							 $(".cur").children(".fold").css("visibility", "visible");
						 }
						 $(".cur").children("ul").find(".add-child").remove();
						 var link = '<li><a class="gss_delete first_delete " href="navigationProducts?navigationId=' + data.id + '&navigationGroupId=' + $("#navigationGroupId").val() + '">商品维护</a></li>';
						 $("#" + data.id ).closest("ul").find(".dropdown-menu").append(link);
						 $("ul").removeClass('cur');
					 }
					 $('#exampleModal').modal('toggle');
				 },
				 error: function(data){
				 	//隐藏弹出窗口
				 	$('#exampleModal').modal('toggle');
				 }
				 });
			}


			$("#navigationName").val("")
		}
	});


		
	//删除
	$(document).on("click",".del",function(){
		var obj = $(this).closest(".root");
		$("#zIndex").val(a);
		$("#idd").val($(this).closest(".root").attr('id'));
		$.ajax({
				url: "delNavigation",
				type: "POST",
				data: $('#addFirstForm').serialize(),
				success: function(data){
					//$(this).closest(".root").remove();
					if (data.flag == "S"){
						obj.remove();
						//重新排序
						for(var i=0;i<=$(".main").children(".first").length;i++){
							$(".first").eq(i).children(".number").html(parseInt(i+1));

							for(var t=0;t<=$(".first").eq(i).children(".second").length;t++){
								$(".first").eq(i).children(".second").eq(t).children(".number").html(parseInt(i+1)+"."+parseInt(t+1));

								for(var q=0;q<=$(".first").eq(i).children(".second").length;q++){
									$(".first").eq(i).children(".second").eq(t).children(".third").eq(q).children(".number").html(parseInt(i+1)+"."+parseInt(t+1)+"."+parseInt(q+1));
								}
							}
						}
					}
				},
				error: function(data){
					//隐藏弹出窗口
					$('#exampleModal').modal('toggle');
				}
			});

		
	})
	
	//编辑
	$(document).on("click",".edit",function(){
		a = 4;
		var level ;
		if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('first')){
			level=1; //1表示第一级添加子分类
		}else if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('second')){
			level=2; //2表示第二级添加子分类
		}else if($(this).parentsUntil('.root').parent().attr('class') && $(this).parentsUntil('.root').parent().hasClass('third')){
			level=3; //3表示第三级
		}
		$(this).parent().parent().siblings(".name").addClass('cur');
		$("#exampleModal").find('input[name=id]').val($(".cur").closest(".root").attr('id'));
		$("#exampleModal").find('input[name=navigationName]').val($(".cur").closest(".root").attr('navigationName'));
		if ($(".cur").closest(".root").attr('contentId') != "" && $(".cur").closest(".root").attr('contentId') != "undefined"){
			$('#img').attr({"src": "/content/control/getImage?contentId=" + $(".cur").closest(".root").attr('contentId')});
			$("#exampleModal").find('input[name=contentId]').val($(".cur").closest(".root").attr('contentId'));
		}
		$("#exampleModal input[name='isEnabled']").each(function(){
			if ($(this).val() == $(".cur").closest(".root").attr('isEnabled')){
				$(this).attr("checked","checked");
			}
		});
		$("#exampleModal").find('select[name=productCategory]').val($(".cur").closest(".root").attr('productCategory'));

	})
	
	//上移
	$(document).on("click",".up",function(){
		$("#zIndex").val(a);
		$("#idd").val($(this).closest(".root").attr('id'));
		$("#moveId").val($(this).closest(".root").prev("ul").attr('id'));
		var _old = $(this).closest(".root");
		var _new = $(this).closest(".root").prev("ul");
		$.ajax({
			url: "navigationMove",
			type: "POST",
			data: $('#addFirstForm').serialize(),
			success: function(data){
				if (data.flag == "S"){
					if (_new.length > 0 &&  _new.attr("class")!="title") {
						//重新排序
						if(a==1){
							_old.find(".number").each(function(){
								var str=$(this).text();
								var changeStr=parseInt(str.substring(0,1))-1;
								$(this).html(changeStr+str.substring(1,str.length))
							})

							_new.find(".number").each(function(){
								var str=$(this).text();
								var changeStr=parseInt(str.substring(0,1))+1;
								$(this).html(changeStr+str.substring(1,str.length))
							})
						}else if(a==2){
							_old.find(".number").each(function(){
								var str=$(this).text();
								var changeStr=parseInt(str.substring(2,3))-1;
								$(this).html(str.substring(0,2)+changeStr+str.substring(3,str.length))
							})

							_new.find(".number").each(function(){
								var str=$(this).text();
								var changeStr=parseInt(str.substring(2,3))+1;
								$(this).html(str.substring(0,2)+changeStr+str.substring(3,str.length))
							})
						}else if(a==3){
							_old.find(".number").each(function(){
								var str=$(this).text();
								var changeStr=parseInt(str.substring(str.length-1,str.length))-1;
								$(this).html(str.substring(0,str.length-1)+changeStr)
							})

							_new.find(".number").each(function(){
								var str=$(this).text();
								var changeStr=parseInt(str.substring(str.length-1,str.length))+1;
								$(this).html(str.substring(0,str.length-1)+changeStr)
							})
						}

						var _temp = _old.html();
						_old.empty().append(_new.html());
						_new.empty().append(_temp);

					}else{
						alert('此分类已在当层等级下排第一')
					}
				}
			},
			error: function(data){
				//隐藏弹出窗口
				$('#exampleModal').modal('toggle');
			}
		});
	})
	
	//下移
	$(document).on("click",".down",function(){
		$("#zIndex").val(a);
		$("#idd").val($(this).closest(".root").attr('id'));
		$("#moveId").val($(this).closest(".root").next("ul").attr('id'));
		var _old = $(this).closest(".root");
		var _new = $(this).closest(".root").next("ul");
		$.ajax({
			url: "navigationMove",
			type: "POST",
			data: $('#addFirstForm').serialize(),
			success: function(data){
				if (_new.length > 0) {
					//重新排序
					if(a==1){
						_old.find(".number").each(function(){
							var str=$(this).text();
							var changeStr=parseInt(str.substring(0,1))+1;
							$(this).html(changeStr+str.substring(1,str.length))
						})
						
						_new.find(".number").each(function(){
							var str=$(this).text();
							var changeStr=parseInt(str.substring(0,1))-1;
							$(this).html(changeStr+str.substring(1,str.length))
						})
					}else if(a==2){
						_old.find(".number").each(function(){
							var str=$(this).text();
							var changeStr=parseInt(str.substring(2,3))+1;
							$(this).html(str.substring(0,2)+changeStr+str.substring(3,str.length))
						})
						
						_new.find(".number").each(function(){
							var str=$(this).text();
							var changeStr=parseInt(str.substring(2,3))-1;
							$(this).html(str.substring(0,2)+changeStr+str.substring(3,str.length))
						})
					}else if(a==3){
						_old.find(".number").each(function(){
							var str=$(this).text();
							var changeStr=parseInt(str.substring(str.length-1,str.length))+1;
							$(this).html(str.substring(0,str.length-1)+changeStr)
						})
						
						_new.find(".number").each(function(){
							var str=$(this).text();
							var changeStr=parseInt(str.substring(str.length-1,str.length))-1;
							$(this).html(str.substring(0,str.length-1)+changeStr)
						})
					}
					
					var _temp = _old.html();
					_old.empty().append(_new.html());
					_new.empty().append(_temp);
				}else{
					alert('此分类已在当层等级下排最后一位')
				}
			},
			error: function(data){
				//隐藏弹出窗口
				$('#exampleModal').modal('toggle');
			}
		});
	})
	
	//展开缩起
	$(document).on("click",".fold",function(){
		if($(this).hasClass("glyphicon-plus-sign")){
			$(this).removeClass("glyphicon-plus-sign")
			$(this).addClass("glyphicon-minus-sign")
		}else{
			$(this).removeClass("glyphicon-minus-sign")
			$(this).addClass("glyphicon-plus-sign")
		}
		$(this).siblings('ul').toggle()
	})
	

	$("form").submit(function(e){
		e.preventDefault();
   	});
	
})
