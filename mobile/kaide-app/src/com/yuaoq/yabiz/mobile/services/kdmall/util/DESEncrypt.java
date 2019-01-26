package com.yuaoq.yabiz.mobile.services.kdmall.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by changsy on 2018/4/17.
 */
public class DESEncrypt {
    
    /**
     * 加密方法
     *
     * @param encryptString 需要加密的字符串
     *                      加密的key，字符长度只能为8 位
     */
    public static String encryptDES(String encryptString, String encryptKey) throws Exception {
        IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
        
        return new BASE64Encoder().encode(encryptedData);
    }
    
    /**
     * 解密方法
     *
     * @param decryptString 需要解密的字符串
     * @param decryptKey    解密的key，字符长度只能为8 位
     */
    public static String decryptDES(String decryptString, String decryptKey) throws Exception {
        byte[] byteMi = new BASE64Decoder().decodeBuffer(decryptString);
        IvParameterSpec zeroIv = new IvParameterSpec(new byte[8]);
        SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
        byte decryptedData[] = cipher.doFinal(byteMi);
        
        return new String(decryptedData);
    }
}
