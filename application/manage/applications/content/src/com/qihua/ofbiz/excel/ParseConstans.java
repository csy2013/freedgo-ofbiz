package com.qihua.ofbiz.excel;

public class ParseConstans {
	
	/**xml中验证规则的名称name**/
	public static String RULE_NAME_NULLABLE = "nullable";		//不能为空
	public static String RULE_NAME_NOT_EQUAL = "not_equal";		//不匹配
	public static String RULE_NAME_NOT_EXIST = "not_exist";		//不存在
	public static String RULE_NAME_NUMERIC_CHECK = "numeric_check"; //2位小数
	public static String RULE_NAME_INT_CHECK = "int_check"; 	//整数
	public static String RULE_NAME_DATE_CHECK = "date_check";	//日期校验
	public static String RULE_NAME_MAX_LENGTH = "max_length";	//最大长度
	public static String RULE_NAME_REPEAT_CHECK = "name_check";	//名称重复
	public static String RULE_NAME_VALUE_CHECK = "value_check";	//输入值
	public static String RULE_NAME_TEL_VALIDATE = "tel_validate";//手机号校验
	
	public static String RULE_NAME_UNIQUE = "checkUnique";
	
	/**excel 中的模板数据错误**/
	public static String ERROR_EXCEL_NULL="excel中数据为空!<br>";
	public static String ERROR_EXCEL_COLUMN_NOT_EQUAL="excel列数不相符，请检查!<br>";
	public static String ERROR_EXCEL_DATA_TYPE = "数据类型错误";

}
