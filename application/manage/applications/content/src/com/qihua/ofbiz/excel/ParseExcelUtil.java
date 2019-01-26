package com.qihua.ofbiz.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * 解析excel 工具类
 * 
 * @author PCCW
 * 
 */
@SuppressWarnings("rawtypes")
public class ParseExcelUtil {
	public Workbook  workBook;
	public Sheet sheet;
	public ParseXMLUtil parseXmlUtil;
	public StringBuffer errorString;

	/** 当前实体类的code **/
	public String curEntityCode;
	/** 表头map对象：key:entityCode, value:headMap(index,headTitle) **/
	public Map curEntityHeadMap;

	/** 字段的必填：key:entityCode+headTitle, value:true(必填),false(不必填) **/
	public Map curEntityColRequired;

	/** 存放每一行的数据 **/
	public List listDatas;
	
	public LocalDispatcher dispatcher;
	
	public String validateCellData;
	public HttpServletRequest request;
	
	public ParseExcelUtil(FileInputStream fis, File xmlFile, String validateCellData, LocalDispatcher dispatcher) {
		this.validateCellData = validateCellData;
		this.dispatcher = dispatcher;
		try {
			workBook = WorkbookFactory.create(fis);
			parseXmlUtil = new ParseXMLUtil(xmlFile);
			errorString = new StringBuffer();
			readExcelData();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
	}

	public ParseExcelUtil(FileInputStream fis, File xmlFile, String validateCellData, LocalDispatcher dispatcher,HttpServletRequest request) {
		this.validateCellData = validateCellData;
		this.dispatcher = dispatcher;
		this.request=request;
		try {
			workBook = WorkbookFactory.create(fis);
			parseXmlUtil = new ParseXMLUtil(xmlFile,request);
			errorString = new StringBuffer();
			readExcelData();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
	}

	/** 开始从excel读取数据 **/
	public void readExcelData() {
		int sheetSize = workBook.getNumberOfSheets();
		if(sheetSize > 0){
			//获取sheet
			sheet = workBook.getSheetAt(0);   
			String entityName = workBook.getSheetName(0);
			readSheetData(sheet, entityName);
		}else{
			errorString.append("{\"msg\":\"请使用指定Excel模板！\"},");
		}
	}

	/** 读每个sheet页的数据 **/
	public void readSheetData(Sheet sheet, String entityName) {
		int rowNumbers = sheet.getPhysicalNumberOfRows();
		Map ent = (Map) parseXmlUtil.getEntityMap().get(entityName);
		if(ent == null){
			System.out.println("================请使用指定Excel模板！");
			errorString.append("{\"msg\":\"请使用指定Excel模板！\"},");
		}else{
			this.setCurEntityCode((String) ent.get("code"));
			if (rowNumbers == 0) {
				System.out.println("================excel中数据为空！");
				errorString.append("{\"msg\":\""+ParseConstans.ERROR_EXCEL_NULL+"\"},");
			}else{
				List colList = (List) parseXmlUtil.getColumnListMap().get(entityName);
				int xmlRowNum = colList.size();
				Row excelRow = sheet.getRow(0);
				int excelFirstRow = excelRow.getFirstCellNum();
				int excelLastRow = excelRow.getLastCellNum();
				if (xmlRowNum != (excelLastRow - excelFirstRow)) {
					System.out.println("==================excel列数不相符，请检查");
					errorString.append("{\"msg\":\""+ParseConstans.ERROR_EXCEL_COLUMN_NOT_EQUAL+"\"},");
				}else{
					readSheetHeadData(sheet);
					readSheetColumnData(sheet, entityName);
				}
			}
		}
	}

	/** 读取sheet页中的表头信息 **/
	@SuppressWarnings({ "unchecked", "static-access" })
	public void readSheetHeadData(Sheet sheet) {
		Map headMap = new HashMap();
		curEntityHeadMap = new HashMap();
		curEntityColRequired = new HashMap();
		Row excelheadRow = sheet.getRow(0);
		int excelLastRow = excelheadRow.getLastCellNum();
		String headTitle = "";
		for (int i = 0; i < excelLastRow; i++) {
			Cell cell = excelheadRow.getCell(i);
			headTitle = getStringCellValue(cell).trim();
			if (headTitle.endsWith("*")) {
				curEntityColRequired.put(this.getCurEntityCode() + "_"
						+ headTitle, true);
			} else {
				curEntityColRequired.put(this.getCurEntityCode() + "_"
						+ headTitle, false);
			}
			headMap.put(i, headTitle);
		}
		curEntityHeadMap.put(this.getCurEntityCode(), headMap);
	}

	/** 读取sheet页里面的数据 **/
	@SuppressWarnings({ "unchecked", "static-access" })
	public void readSheetColumnData(Sheet sheet, String entityName) {

		Row excelheadRow = sheet.getRow(0);
		int excelLastcell = excelheadRow.getLastCellNum(); // excel总列数
		int excelRowNum = sheet.getLastRowNum(); // excel总行数
		Map headMap = (Map) this.getCurEntityHeadMap().get(
				this.getCurEntityCode());
		Map colMap = parseXmlUtil.getColumnMap();
		listDatas = new ArrayList();

		for (int i = 1; i <= excelRowNum; i++) {// 行循环
			Row columnRow = sheet.getRow(i);
			if (columnRow != null && columnRow.getLastCellNum() > 0) {
				Map curRowCellMap = new HashMap();
				for (int j = 0; j < excelLastcell; j++) { // 列循环
					int cout = headMap.get(j).toString().indexOf("*");
					String headTitle = "";
					if (cout == -1) {
						headTitle = headMap.get(j).toString();
					} else {
						headTitle = headMap.get(j).toString()
								.substring(0, cout);
					}
					Map curColMap = (Map) colMap.get(entityName + "_"
							+ headTitle);
					if(UtilValidate.isEmpty(curColMap)){
						errorString.append("{\"msg\":\"列标题找不到："+headTitle+"\"},");
					}else{
						String curColCode = (String) curColMap.get("code");
						String curColType = (String) curColMap.get("type");
						String curColName = (String) curColMap.get("name");
						String curColMaxlength = (String) curColMap.get("maxlength");
						Cell colCell = columnRow.getCell(j);
						String value = getStringCellValue(colCell);
						if (value != null) {
							value = value.trim();
						}
						String xmlColType = (String) curColMap.get("type");
						curRowCellMap.put(curColCode, value);
						try {
							Map rs = dispatcher.runSync(
										this.validateCellData,
										UtilMisc.toMap(
												"curRow",i + 1,
												"curCol",j + 1,
												"colCell",colCell,
												"entityName",entityName,
												"headName",headTitle,
												"curColType",curColType,
												"curColCode",curColCode,
												"columnRow",columnRow,
												"curColMaxlength",curColMaxlength,
												"parseXmlUtil",parseXmlUtil
												));
							errorString.append(rs.get("errorString"));
						} catch (GenericServiceException e) {
						}
					}
				}
				listDatas.add(curRowCellMap);
			}
		}
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
	
	public String getCurEntityCode() {
		return curEntityCode;
	}

	public void setCurEntityCode(String curEntityCode) {
		this.curEntityCode = curEntityCode;
	}

	public Map getCurEntityHeadMap() {
		return curEntityHeadMap;
	}

	public void setCurEntityHeadMap(Map curEntityHeadMap) {
		this.curEntityHeadMap = curEntityHeadMap;
	}

	public ParseXMLUtil getParseXmlUtil() {
		return parseXmlUtil;
	}

	public void setParseXmlUtil(ParseXMLUtil parseXmlUtil) {
		this.parseXmlUtil = parseXmlUtil;
	}

	public Map getCurEntityColRequired() {
		return curEntityColRequired;
	}

	public void setCurEntityColRequired(Map curEntityColRequired) {
		this.curEntityColRequired = curEntityColRequired;
	}

	public List getListDatas() {
		return listDatas;
	}

	public void setListDatas(List listDatas) {
		this.listDatas = listDatas;
	}

	public StringBuffer getErrorString() {
		return errorString;
	}

	public void setErrorString(StringBuffer errorString) {
		this.errorString = errorString;
	}
	
	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
