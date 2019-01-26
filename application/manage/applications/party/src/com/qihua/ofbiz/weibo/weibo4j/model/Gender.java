package com.qihua.ofbiz.weibo.weibo4j.model;

/**
 * @author SinaWeibo
 * 
 */
public enum Gender {
	/**
	 * 男
	 */
	MALE,
	/**
	 * 女
	 */
	FEMALE;
	public static String valueOf(Gender gender) {
		int ordinal= gender.ordinal();
		if(ordinal==0) {
            return "m";
        }
		return "f";
	}
}
