<style type="text/css">
    #BusinessInfo_Form .form-group span {
        padding-top: 7px;
        padding-left: 0px;
    }
</style>

<!-- 内容start -->
<form id="BusinessInfo_Form" class="form-horizontal">
    <div class="box box-info">

        <div class="box-header with-border">
            <h3 class="box-title">基本信息</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-6">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">商家编号：</label>
                        <span id="partyId" class="col-sm-8"></span>
                    </div>
                </div>
                <div class="col-sm-6">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">商家名称：</label>
                        <span id="partyName" class="col-sm-8"></span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-6">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">商家地址：</label>
                        <span id="businessAddress" class="col-sm-8"></span>
                    </div>
                </div>

                <div class="col-sm-6">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">是否启用：</label>
                        <span id="isUse" class="col-sm-8"></span>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-6">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">会员编号：</label>
                        <span id="partyIdTo" class="col-sm-8"></span>
                    </div>
                </div>
                <div class="col-sm-6">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">商家描述：</label>
                        <span id="description" class="col-sm-8"></span>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-6">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">店铺logo：</label>
                        <div class="col-sm-3">
                            <a href="#" target="_blank"><img alt="" src="" id="img_logoImgContentId" style="height: 100px;width: 100px;"></a>
                        </div>
                        <div class="col-sm-5">
                            <div class="col-sm-12 dp-form-remarks">注：推荐尺寸为 300*200px</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div><!-- 内容end -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">公司营业执照信息</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">公司名称：</label>
                        <span id="companyName" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">成立时间：</label>
                        <span id="companyCreateDate" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">营业期限：</label>
                        <span id="businessEndDate" class="col-sm-7"></span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">法人姓名：</label>
                        <span id="leageName" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">联系电话：</label>
                        <span id="leageTel" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">电子邮箱：</label>
                        <span id="leageEmail" class="col-sm-7"></span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">身份证号：</label>
                        <span id="leageCardNo" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">营业执照所在地：</label>
                        <span id="busiAddress" class="col-sm-7"></span>
                    </div>
                </div>

                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">统一社会信用代码：</label>
                        <span id="socialCardNo" class="col-sm-7"></span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">营业执照：</label>
                        <div class="col-sm-7">
                            <a href="#" target="_blank"><img alt="" src="" id="img_busiImgContentId" style="height: 100px;width: 100px;"></a>
                        </div>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">身份证电子版正面：</label>
                        <div class="col-sm-7">
                            <a href="#" target="_blank"><img alt="" src="" id="idCardProsImgContentId" style="height: 100px;width: 100px;"></a>
                        </div>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">身份证电子版反面：</label>
                        <div class="col-sm-7">
                            <a href="#" target="_blank"><img alt="" src="" id="idCardConsImgContentId" style="height: 100px;width: 100px;"></a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">相关资质(餐饮酒水类)：</label>
                        <div class="col-sm-7">
                            <a href="#" target="_blank"><img alt="" src="" id="qualifImgContentId" style="height: 100px;width: 100px;"></a>
                        </div>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">合同文件：</label>
                        <div class="col-sm-7">
                            <a href="#" target="_blank"><img alt="" src="" id="qualifImgContentId" style="height: 100px;width: 100px;"></a>
                        </div>
                    </div>
                </div>
            </div>


        </div>
    </div><!-- 内容end -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">结算银行登记</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">银行开户名：</label>
                        <span id="bankAcountName" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">公司银行账号：</label>
                        <span id="bankAccount" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">开户支行名称：</label>
                        <span id="bankBranchName" class="col-sm-7"></span>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-sm-4">
                    <div class="form-group">
                        <label class="col-sm-5 control-label">开户行支行联行号：</label>
                        <span id="bankBranchAcount" class="col-sm-7"></span>
                    </div>
                </div>
                <div class="col-sm-8">
                    <div class="form-group">
                        <label class="col-sm-4 control-label">开户行支行所在地：</label>
                        <span id="bankAddress" class="col-sm-8"></span>
                    </div>
                </div>
            </div>
        </div><!-- /.box-body -->
    </div><!-- 内容end -->
    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">主营分类</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <span class="col-sm-12" id="partyCategory" ></span>
            </div>

        </div><!-- /.box-body -->
    </div><!-- 内容end -->

    <div class="box box-info">
        <div class="box-header with-border">
            <h3 class="box-title">签约品牌</h3>
        </div>
        <div class="box-body">
            <div class="row">
                <span class="col-sm-12" id="partyBrand" ></span>
            </div>
        </div><!-- /.box-body -->
    </div><!-- 内容end -->
</form>
<!-- script区域start -->
<script>
    $(document).ready(function () {
        $.ajax({
            url: "getBusinessInfoById?partyId=" + '${parameters.partyId}',
            type: "GET",
            dataType: "json",
            success: function (data) {
                var party = data.party;
                var partyGroup =data.partyGroup;
                var partyBusiness=data.partyBusiness;
                var partyRelationShip=data.partyRelationShip;
                var partyProductCategory=data.partyProductCategory;
                var partyBusinessBrand=data.partyBusinessBrand;


                $('#BusinessInfo_Form #partyIdTo').text(partyRelationShip.partyIdTo);
                $('#BusinessInfo_Form #partyId').text(party.partyId);
                $('#BusinessInfo_Form #partyName').text(partyGroup.partyName);
                $('#BusinessInfo_Form #businessAddress').text(partyBusiness.province+","+partyBusiness.city+","+partyBusiness.county+","+partyBusiness.address);
                $('#BusinessInfo_Form #partyId').text(party.partyId);
                var isUse ="";
                if(party.statusId=="PARTY_ENABLED"){
                    isUse="启用";
                }else{
                    isUse="未启用";
                }
                $('#BusinessInfo_Form #isUse').text(isUse);
                $('#BusinessInfo_Form #description').text(partyBusiness.description);
                $('#BusinessInfo_Form #companyName').text(partyBusiness.companyName);
                if(partyBusiness.companyCreateDate){
                    $('#BusinessInfo_Form #companyCreateDate').text(timeStamp2String2(partyBusiness.companyCreateDate));
                }
                if(partyBusiness.businessEndDate){
                    $('#BusinessInfo_Form #businessEndDate').text(timeStamp2String2(partyBusiness.businessEndDate));
                }
                $('#BusinessInfo_Form #leageName').text(partyBusiness.leageName);
                $('#BusinessInfo_Form #leageTel').text(partyBusiness.leageTel);
                $('#BusinessInfo_Form #leageEmail').text(partyBusiness.leageEmail);
                $('#BusinessInfo_Form #leageCardNo').text(partyBusiness.leageCardNo);
//                $('#BusinessInfo_Form #img_leageImgContentId').attr({"src": "/content/control/getImage?contentId=" + partyBusiness.leageImgContentId});
                $('#BusinessInfo_Form #idCardConsImgContentId').attr({"src": "/content/control/getImage?contentId=" + partyBusiness.idCardConsImg});
                $('#BusinessInfo_Form #idCardConsImgContentId').parent().eq(0).attr({"href": "/content/control/getImage?contentId=" + partyBusiness.idCardConsImg});

                $('#BusinessInfo_Form #idCardProsImgContentId').attr({"src": "/content/control/getImage?contentId=" + partyBusiness.idCardProsImg});
                $('#BusinessInfo_Form #idCardProsImgContentId').parent().eq(0).attr({"href": "/content/control/getImage?contentId=" + partyBusiness.idCardProsImg});
                if( partyBusiness.qualifImg){
                    $('#BusinessInfo_Form #qualifImgContentId').attr({"src": "/content/control/getImage?contentId=" + partyBusiness.qualifImg});
                    $('#BusinessInfo_Form #qualifImgContentId').parent().eq(0).attr({"href": "/content/control/getImage?contentId=" + partyBusiness.qualifImg});
                }
                if(partyBusiness.logoImg){
                    $('#BusinessInfo_Form #img_logoImgContentId').attr({"src": "/content/control/getImage?contentId=" + partyBusiness.logoImg});
                    $('#BusinessInfo_Form #img_logoImgContentId').parent().eq(0).attr({"href": "/content/control/getImage?contentId=" + partyBusiness.logoImg});
                }
                $('#BusinessInfo_Form #socialCardNo').text(partyBusiness.socialCardNo);
                $('#BusinessInfo_Form #img_busiImgContentId').attr({"src": "/content/control/getImage?contentId=" + partyBusiness.busiImgContentId});
                $('#BusinessInfo_Form #img_busiImgContentId').parent().eq(0).attr({"href": "/content/control/getImage?contentId=" + partyBusiness.busiImgContentId});

                $('#BusinessInfo_Form #busiAddress').text(partyBusiness.busiProvince+","+partyBusiness.busiCity+","+partyBusiness.busiCounty+","+partyBusiness.busiAddress);
                $('#BusinessInfo_Form #leageTel').text(partyBusiness.leageTel);

                $('#BusinessInfo_Form #bankAcountName').text(partyBusiness.bankAcountName);
                $('#BusinessInfo_Form #bankAccount').text(partyBusiness.bankAccount);
                $('#BusinessInfo_Form #bankBranchName').text(partyBusiness.bankBranchName);
                $('#BusinessInfo_Form #bankBranchAcount').text(partyBusiness.bankBranchAcount);
                $('#BusinessInfo_Form #bankAddress').text(partyBusiness.branchProvince+","+partyBusiness.branchCity+","+partyBusiness.branchCounty);

                var partyCategory="";
                var partyCategorys = data.partyProductCategory;
                for(var i=0;i<partyCategorys.length;i++){
                    var categoryName = partyCategorys[i].categoryName;
                    partyCategory=partyCategory+"        "+categoryName;
                }
                $("#BusinessInfo_Form #partyCategory").text(partyCategory)

                var partyBrand="";
                var partyBrands = data.partyBusinessBrand;
                for(var i=0;i<partyBrands.length;i++){
                    var brandName = partyBrands[i].brandName;
                    partyBrand=partyBrand+"        "+brandName;
                }
                $("#BusinessInfo_Form #partyBrand").text(partyBrand)

                /*console.log(data)
                var record = data.record;
                $('#BusinessInfo_Form #businessName').html(record.businessName);
                $('#BusinessInfo_Form #partyId').html(record.partyId);
                $('#BusinessInfo_Form #partyIdTo').html(record.partyIdTo);
                $('#BusinessInfo_Form #brands').html(record.brands);
                var address = record.provinceName + record.cityName + record.countyName + record.address;
                $('#BusinessInfo_Form #address').html(address);
                $('#BusinessInfo_Form #tel').html(record.tel);
                $('#BusinessInfo_Form #legalPersonName').html(record.legalPersonName);
                $('#BusinessInfo_Form #idCard').html(record.idCard);
                $('#BusinessInfo_Form #businessLicense').html(record.businessLicense);
                $('#BusinessInfo_Form #auditStatus').html(record.auditStatus);
                $('#BusinessInfo_Form #statusId').html(record.statusId);
                $('#BusinessInfo_Form #businessTypeName').html(record.businessTypeName);
                if (record.description) {
                    $('#BusinessInfo_Form #description').html(record.description.replace(new RegExp(" ", "gm"), "&nbsp;").replace(new RegExp("\n", "gm"), "<br>"));
                }
                if (record.idCardProsImg) {
                    $('#BusinessInfo_Form #img_idCardProsImg').attr({"src": "/content/control/getImage?contentId=" + record.idCardProsImg});
                }
                if (record.idCardConsImg) {
                    $('#BusinessInfo_Form #img_idCardConsImg').attr({"src": "/content/control/getImage?contentId=" + record.idCardConsImg});
                }
                if (record.businessLicenseImg) {
                    $('#BusinessInfo_Form #img_businessLicenseImg').attr({"src": "/content/control/getImage?contentId=" + record.businessLicenseImg});
                }
                if (record.logoImg) {
                    $('#BusinessInfo_Form #img_logoImg').attr({"src": "/content/control/getImage?contentId=" + record.logoImg});
                }*/
            },
            error: function (data) {
                //设置提示弹出框内容
                $('#modal_msg #modal_msg_body').html("${uiLabelMap.InterError}");
                $('#modal_msg').modal();
            }
        });
    });

    //时间格式化2
    function timeStamp2String2(datetime){
        var year = datetime.year+1900;
        var month = datetime.month + 1 < 10 ? "0" + (datetime.month + 1) : datetime.month + 1;
        /*var date = datetime.day < 10 ? "0" + datetime.day : datetime.day;*/  //这是取周几
        var date = datetime.date < 10 ? "0" + datetime.date : datetime.date;
        var hour = datetime.hours< 10 ? "0" + datetime.hours : datetime.hours;
        var minute = datetime.minutes< 10 ? "0" + datetime.minutes : datetime.minutes;
        var second = datetime.seconds < 10 ? "0" + datetime.seconds : datetime.seconds;
        return year + "-" + month + "-" + date+" "+hour+":"+minute+":"+second;
    };
</script><!-- script区域end -->
