package org.ofbiz.product.product;

import com.qihua.ofbiz.excel.ParseConstans;
import com.qihua.ofbiz.excel.ParseXMLUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 单元格校验service
 * @author 查俊虎 2016/02/06
 *
 */
public class GoodValidateCell {
    public static final String module = GoodValidateCell.class.getName();
    public static final String resource = "ProductUiLabels";
    
    /**
     * 单元格校验
     * @param dctx
     * @param context
     * @return
     */
    /** 验证单元格数据 **/
	@SuppressWarnings("static-access")
	public static Map<String, Object> validateCellData(DispatchContext dctx, Map<String, ? extends Object> context) {
		Map<String,Object> result = FastMap.newInstance();
		Delegator delegator = dctx.getDelegator();
		//错误信息
		StringBuffer errorString = new StringBuffer();
		
		int curRow = (Integer)context.get("curRow");
		int curCol = (Integer)context.get("curCol");
		Cell colCell = (Cell)context.get("colCell");
		String entityName = (String)context.get("entityName");
		String headName = (String)context.get("headName");
		String curColType = (String)context.get("curColType");
		String curColCode = (String)context.get("curColCode");
		Row columnRow = (Row)context.get("columnRow");
		String curColMaxlength = (String)context.get("curColMaxlength");
		ParseXMLUtil parseXmlUtil = (ParseXMLUtil)context.get("parseXmlUtil");
		
		List<GenericValue> productCategoryLevelFirstList=FastList.newInstance();
		List<GenericValue> productCategoryLevelSecondAllList=FastList.newInstance();
		List<GenericValue> productCategoryLevelThirdAllList=FastList.newInstance();
		List<GenericValue> productCategoryLevelSecondList=FastList.newInstance();
		List<GenericValue> productCategoryLevelThirdList=FastList.newInstance();
		List<GenericValue> mrchantNameList=FastList.newInstance();


        //获取request
        HttpServletRequest request = (HttpServletRequest) parseXmlUtil.getRequest();
        HttpSession session = request.getSession();
		
		List rulList = (List) parseXmlUtil.getColumnRulesMap().get(
				entityName + "_" + headName);
		if (rulList != null && rulList.size() > 0) {
			for (int i = 0; i < rulList.size(); i++) {
				Map rulM = (Map) rulList.get(i);
				String rulName = (String) rulM.get("name");
				String rulMsg = (String) rulM.get("message");
				String cellValue = getStringCellValue(colCell);
				if (rulName.equals(ParseConstans.RULE_NAME_NULLABLE)) { //非空校验
					if (cellValue == null || "".equals(cellValue.trim())) {
						errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
								+ rulMsg + "\"},");
					}
				}else if(rulName.equals(ParseConstans.RULE_NAME_MAX_LENGTH)){//长度校验
					if (cellValue != null && !"".equals(cellValue.trim())) {
						int maxlength = 0;
						if(UtilValidate.isNotEmpty(curColMaxlength)){
							maxlength = Integer.valueOf(curColMaxlength);
						}
						if(cellValue.length() > maxlength){
							errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
									+ rulMsg + "\"},");
						}
					}
				}else if(rulName.equals(ParseConstans.RULE_NAME_VALUE_CHECK)){ //输入值验证
					productCategoryLevelSecondList=FastList.newInstance();
					productCategoryLevelThirdList=FastList.newInstance();
					mrchantNameList=FastList.newInstance();
					if ((cellValue != null && !"".equals(cellValue.trim())) || "integralDeductionUpper".equals(curColCode) || "accountingQuantityTotal".equals(curColCode)) {

						if("businessPartyId".equals(curColCode)){ //所属商家
							try {
								// mrchantNameList = delegator.findByAnd("GetMrchantNameList",UtilMisc.toMap("businessName",cellValue,"auditStatus","1","statusId","PARTY_ENABLED"));
								mrchantNameList = delegator.findByAnd("PartyGroup",UtilMisc.toMap("partyId",cellValue));
								if(mrchantNameList.size()==0){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							} catch (GenericEntityException e) {
								e.printStackTrace();
							}
						}else if("primaryProductCategoryId".equals(curColCode)){ // 商品分类
							if(UtilValidate.isNotEmpty(cellValue)){
								try {
									String[] attrPc = cellValue.split(",");
									if (UtilValidate.isNotEmpty(attrPc) && attrPc.length == 3) {
										productCategoryLevelFirstList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("categoryName", attrPc[0], "productCategoryLevel", new Long(1)));
										if(productCategoryLevelFirstList.size()!=0){
											for (GenericValue genericValue : productCategoryLevelFirstList) {
												productCategoryLevelSecondList=FastList.newInstance();
												productCategoryLevelSecondList = delegator.findByAnd("ProductCategory",UtilMisc.toMap("categoryName",attrPc[1],"productCategoryLevel",new Long(2),"primaryParentCategoryId",genericValue.getString("productCategoryId")));
												if(productCategoryLevelSecondList.size()>0){
													productCategoryLevelSecondAllList.addAll(productCategoryLevelSecondList);
												}
											}
											if(productCategoryLevelSecondAllList.size()!=0){
												for (GenericValue genericValue : productCategoryLevelSecondAllList) {
													productCategoryLevelThirdList=FastList.newInstance();
													productCategoryLevelThirdList = delegator.findByAnd("ProductCategory",UtilMisc.toMap("categoryName",attrPc[2],"productCategoryLevel",new Long(3),"primaryParentCategoryId",genericValue.getString("productCategoryId")));
													if(productCategoryLevelThirdList.size()>0){
														productCategoryLevelThirdAllList.addAll(productCategoryLevelThirdList);
													}
												}
												if(productCategoryLevelThirdAllList.size()==0){
													errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
															+ rulMsg + "\"},");
												}

											}else{
												errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
														+ rulMsg + "\"},");
											}
										}else{
											errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
													+ rulMsg + "\"},");
										}

									} else {
										errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
												+ rulMsg + "\"},");
									}
								}catch (GenericEntityException e){
									e.printStackTrace();
								}
							}
						}else if("tagNames".equals(curColCode)) {//商品标签
							try {
								if(cellValue != null && !"".equals(cellValue.trim())){
									String[] arrTags=cellValue.toString().trim().split(",");
									String chkFlg="Y";
									if(UtilValidate.isNotEmpty(arrTags)){
										for(String tagInfo:arrTags){
											chkFlg="Y";
											List<GenericValue> tagInfos1 = delegator.findByAnd("Tag",UtilMisc.toMap("tagName",tagInfo,"tagTypeId","ProdutTypeTag_1"));
											if(UtilValidate.isEmpty(tagInfos1)){
												chkFlg="N";
												break;
											}
										}
									}
									if(chkFlg=="N"){
										errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
												+ rulMsg + "\"},");
									}
								}
							} catch (GenericEntityException e) {
								e.printStackTrace();
							}
						}else if("isOnline".equals(curColCode)) {//是否申请上架
							if(!("是".equals(cellValue)||"否".equals(cellValue))){
								errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
										+ rulMsg + "\"},");
							}
						}else if("isBondedGoods".equals(curColCode)) {//是否保税商品
							if(!("是".equals(cellValue)||"否".equals(cellValue))){
								errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
										+ rulMsg + "\"},");
							}
						}else if("supportServiceType".equals(curColCode)) {//服务支持
							if(UtilValidate.isNotEmpty(cellValue)){
								String[] attrSeType=cellValue.split(",");
								if(UtilValidate.isNotEmpty(attrSeType)){
									if(attrSeType.length>2){
										errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
												+ rulMsg + "\"},");
									}else{
										for(String strSeType:attrSeType){
											if(!("七日无理由退货".equals(strSeType)||"包邮".equals(strSeType))){
												errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
														+ rulMsg + "\"},");
												break;
											}
										}
									}
								}
							}
						}else if("saleEndTime".equals(curColCode)){// 销售结束时间
							if(cellValue != null && !"".equals(cellValue.trim())){
								SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
								try {
									if(formatter.parse(cellValue).before(formatter.parse(cellValue))){
										errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
												+ rulMsg + "\"},");
									}
								} catch (ParseException e) {
									e.printStackTrace();
								}
							}
						}else if("platformClassId".equals(curColCode)){ // 主营分类
							if(UtilValidate.isNotEmpty(cellValue)){
								try {
									String[] attrPc = cellValue.split(",");
									if (UtilValidate.isNotEmpty(attrPc) && attrPc.length == 3) {
										productCategoryLevelFirstList = delegator.findByAnd("ProductCategory", UtilMisc.toMap("categoryName", attrPc[0], "productCategoryLevel", new Long(1)));
										if(productCategoryLevelFirstList.size()!=0){
											for (GenericValue genericValue : productCategoryLevelFirstList) {
												productCategoryLevelSecondList=FastList.newInstance();
												productCategoryLevelSecondList = delegator.findByAnd("ProductCategory",UtilMisc.toMap("categoryName",attrPc[1],"productCategoryLevel",new Long(2),"primaryParentCategoryId",genericValue.getString("productCategoryId")));
												if(productCategoryLevelSecondList.size()>0){
													productCategoryLevelSecondAllList.addAll(productCategoryLevelSecondList);
												}
											}
											if(productCategoryLevelSecondAllList.size()!=0){
												for (GenericValue genericValue : productCategoryLevelSecondAllList) {
													productCategoryLevelThirdList=FastList.newInstance();
													productCategoryLevelThirdList = delegator.findByAnd("ProductCategory",UtilMisc.toMap("categoryName",attrPc[2],"productCategoryLevel",new Long(3),"primaryParentCategoryId",genericValue.getString("productCategoryId")));
													if(productCategoryLevelThirdList.size()>0){
														productCategoryLevelThirdAllList.addAll(productCategoryLevelThirdList);
													}
												}
												if(productCategoryLevelThirdAllList.size()==0){
													errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
															+ rulMsg + "\"},");
												}

											}else{
												errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
														+ rulMsg + "\"},");
											}
										}else{
											errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
													+ rulMsg + "\"},");
										}

									} else {
										errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
												+ rulMsg + "\"},");
									}
								}catch (GenericEntityException e){
									e.printStackTrace();
								}
							}
						}else if("brandName".equals(curColCode)){// 商品品牌
							try {
								List<GenericValue> brandInfos=FastList.newInstance();
								if(UtilValidate.isNotEmpty(cellValue)){
									brandInfos=delegator.findByAnd("ProductBrand",UtilMisc.toMap("brandName",cellValue));
								}
								if(brandInfos.size()==0){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							} catch (GenericEntityException e) {
								e.printStackTrace();
							}
						}else if("salePrice".equals(curColCode)){ //销售价格
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}

						}else if("integralDeductionType".equals(curColCode)){ //积分抵扣
							if(UtilValidate.isEmpty(session.getAttribute("integralDeductionType"))){
								session.removeAttribute("integralDeductionType");
							}
							if (cellValue != null && !"".equals(cellValue.trim())) {
								if(!("不使用积分抵扣".equals(cellValue.toString().trim())||
										"百分比抵扣".equals(cellValue.toString().trim())||
										"固定金额抵扣".equals(cellValue.toString().trim()))){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}else{
                                    session.setAttribute("integralDeductionType",cellValue);
                                }
							}
						}else if("integralDeductionUpper".equals(curColCode)){ //积分抵扣上限
                            if(UtilValidate.isNotEmpty(session.getAttribute("integralDeductionType"))){
                                String curIntegralDeductionType=(String)session.getAttribute("integralDeductionType");
                                if("百分比抵扣".equals(curIntegralDeductionType)||"固定金额抵扣".equals(curIntegralDeductionType)){

                                    if(UtilValidate.isEmpty(cellValue)){
                                    	String curMsg="积分折扣为百分比抵扣或固定金额抵扣的场合,积分抵扣上限为必输项目";
                                        errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
                                                + curMsg + "\"},");
                                    }else{
                                        String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
                                        if(!match(regex, cellValue)){
                                            errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
                                                    + rulMsg + "\"},");
                                        }
                                    }
                                }
                            }
						}else if("purchaseLimitationQuantity".equals(curColCode)){ //每人限购数量
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}
						}else if("isBondedGoods".equals(curColCode)) {//列表展示
							if(!("是".equals(cellValue)||"否".equals(cellValue))){
								errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
										+ rulMsg + "\"},");
							}
						}else if("marketPrice".equals(curColCode)){ //市场价格(元)
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}
						}else if("costPrice".equals(curColCode)){ // 成本价格(元)
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}
						}else if("volume".equals(curColCode)){ // 体积(m3)
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}
						}else if("weight".equals(curColCode)){ // 重量(kg)
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}
						}else if("isUsedFeature".equals(curColCode)) {//是否使用规格
							if(UtilValidate.isEmpty(session.getAttribute("curIsUsedFeature"))){
								session.removeAttribute("curIsUsedFeature");
							}
							if(!("是".equals(cellValue)||"否".equals(cellValue))){
								errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
										+ rulMsg + "\"},");
							}else{
                                session.setAttribute("curIsUsedFeature",cellValue);
                            }
						}else if("accountingQuantityTotal".equals(curColCode)) {//可用库存
                            if(UtilValidate.isNotEmpty(session.getAttribute("curIsUsedFeature"))){
                                String curIsUsedFeature=(String)session.getAttribute("curIsUsedFeature");
                                if("否".equals(curIsUsedFeature)){
									if(UtilValidate.isEmpty(cellValue)){
                                    	String curMsg="是否使用规格为否的场合,可用库存不能为空";
                                        errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
                                                + curMsg + "\"},");
                                    }
                                }

                            }
							if(UtilValidate.isNotEmpty(cellValue)) {
								if (cellValue != null && !"".equals(cellValue.trim())) {
									String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
									if (!match(regex, cellValue)) {
										errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
												+ rulMsg + "\"},");
									}
								}
							}

						}else if("warningQuantity".equals(curColCode)) {//库存预警数量
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "^([1-9]\\d*(\\.\\d+)?|0)$";
								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}
						}else if("warningMail".equals(curColCode)) {//预警提示人邮箱
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";

								if(!match(regex, cellValue)){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							}
						}else if("providerName".equals(curColCode)) {//供应商
							if (cellValue != null && !"".equals(cellValue.trim())) {
								String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";

								try {
									List<GenericValue> providerInfos = FastList.newInstance();
									if (UtilValidate.isNotEmpty(cellValue)) {
										providerInfos = delegator.findByAnd("Provider", UtilMisc.toMap("providerName", cellValue));
									}
									if (providerInfos.size() == 0) {
										errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
												+ rulMsg + "\"},");
									}
								}catch (GenericEntityException e) {
									e.printStackTrace();
								}

							}
						}
					}
					
				}else if (rulName.equals(ParseConstans.RULE_NAME_DATE_CHECK)){ //时间校验
					if (cellValue != null && !"".equals(cellValue.trim())) {
						if(!dateCheck(cellValue)){
							errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
									+ rulMsg + "\"},");
						}else{
							if("saleEndTime".equals(curColCode)){
								if(cellValue != null && !"".equals(cellValue.trim())){
									SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
									try {
										if(formatter.parse(cellValue).before(formatter.parse(cellValue))){
											errorString.append("第" + curRow + "行,第" + curCol + "列 :优惠结束时间不能大于优惠开始时间!<br>");
										}
									} catch (ParseException e) {
										e.printStackTrace();
									}
								}
							}
							
							
						}
					}
				}
			}
		}
		result.put("errorString",errorString.toString());
		return result;
	}
	
	/**
	 * 获得单元格字符串
	 * 
	 * @throws UnSupportedCellTypeException
	 */
	public static String getStringCellValue(Cell cell) {
		if (cell == null) {
			return null;
		}

		String result = "";
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN:
			result = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				java.text.SimpleDateFormat TIME_FORMATTER = new java.text.SimpleDateFormat(
						"yyyy-MM-dd");
				result = TIME_FORMATTER.format(cell.getDateCellValue());
			} else {
				double doubleValue = cell.getNumericCellValue();
				DecimalFormat df = new DecimalFormat("#.##");
				result = "" + df.format(doubleValue);
			}
			break;
		case Cell.CELL_TYPE_STRING:
			if (cell.getRichStringCellValue() == null) {
				result = null;
			} else {
				result = cell.getRichStringCellValue().getString();
			}
			break;
		case Cell.CELL_TYPE_BLANK:
			result = null;
			break;
		case Cell.CELL_TYPE_FORMULA:
			try {
				result = String.valueOf(cell.getNumericCellValue());
			} catch (Exception e) {
				result = cell.getRichStringCellValue().getString();
			}
			break;
		default:
			result = "";
		}

		return result;
	}
	
	/**
	* @param regex
	* 正则表达式字符串
	* @param str
	* 要匹配的字符串
	* @return 如果str 符合 regex的正则表达式格式,返回true, 否则返回 false;
	*/
	private static boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}	
	
	/**
	 * 时间校验
	 * @param dateStr
	 * @return
	 */
	private static boolean dateCheck(String dateStr){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try{
            Date date = formatter.parse(dateStr);
            return   true;
        }catch(Exception   e){
              return   false;
        }
    }
}
