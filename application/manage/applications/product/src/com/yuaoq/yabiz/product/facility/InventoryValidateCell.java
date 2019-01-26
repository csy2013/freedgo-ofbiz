package com.yuaoq.yabiz.product.facility;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.service.DispatchContext;

import com.qihua.ofbiz.excel.ParseConstans;
import com.qihua.ofbiz.excel.ParseXMLUtil;


/**
 * 单元格校验service
 * @author 钱进 2016/01/04
 *
 */
public class InventoryValidateCell {
    public static final String module = InventoryValidateCell.class.getName();
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
				} else if (rulName.equals(ParseConstans.RULE_NAME_NUMERIC_CHECK)){ //数字校验，2位小数
					if (cellValue != null && !"".equals(cellValue.trim())) {
						String regex = "^[0-9]+(.[0-9]{1,2})?$";
						if(!match(regex, cellValue)){
							errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
									+ rulMsg + "\"},");
						}
					}
				}else if (rulName.equals(ParseConstans.RULE_NAME_INT_CHECK)){ //整数校验
					if (cellValue != null && !"".equals(cellValue.trim())) {
						String regex = "^[0-9]+?$";
						if(!match(regex, cellValue)){
							errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
									+ rulMsg + "\"},");
						}
					}
				}else if (rulName.equals(ParseConstans.RULE_NAME_DATE_CHECK)){ //时间校验
					if (cellValue != null && !"".equals(cellValue.trim())) {
						if(!dateCheck(cellValue)){
							errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
									+ rulMsg + "\"},");
						}
					}
				}else if (rulName.equals(ParseConstans.RULE_NAME_NOT_EXIST)){ //是否存在
					if (cellValue != null && !"".equals(cellValue.trim())) {
						if("product_id".equals(curColCode)){
							try {
								List<EntityCondition> filedExprs = FastList.newInstance();
								filedExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS,cellValue));
								filedExprs.add(EntityCondition.makeCondition(UtilMisc.toList(
																				EntityCondition.makeCondition("isDel", EntityOperator.NOT_EQUAL,"Y"), 
																				EntityCondition.makeCondition("isDel", EntityOperator.EQUALS,null)),
																			EntityOperator.OR));
								
								List<GenericValue> gv = delegator.findList("Product", EntityCondition.makeCondition(filedExprs,EntityOperator.AND), null, null, null, false);
								if(gv == null || gv.size() == 0){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							} catch (GenericEntityException e) {
								e.printStackTrace();
							}
						}else if("facility_id".equals(curColCode)){
							try {
								GenericValue gv = delegator.findByPrimaryKey("Facility",UtilMisc.toMap("facilityId",cellValue));
								if(gv == null || gv.isEmpty()){
									errorString.append("{\"msg\":\"第" + curRow + "行,第" + curCol + "列 :"
											+ rulMsg + "\"},");
								}
							} catch (GenericEntityException e) {
								e.printStackTrace();
							}
						}
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
				SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat(
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
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try{
            Date date = formatter.parse(dateStr);
            return   true;
        }catch(Exception   e){
              return   false;
        }
    }
}
