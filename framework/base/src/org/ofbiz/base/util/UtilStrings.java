package org.ofbiz.base.util;

/**
 * Created by Administrator on 2015/1/14.
 */
public class UtilStrings {

    public static boolean contains(String[] strs,String str){

        if(strs==null || strs.length ==0 ) return false;
        if(str==null || str.equals("")) return false;
        for (int i = 0; i < strs.length; i++) {
            String s = strs[i];
            if(s.equalsIgnoreCase(str)){
                return true;
            }
        }
        return false;

    }

    public static String firstUpperCase(String extend){
        if(extend==null || extend.equals("")) return extend;
        return extend.substring(0,1).toUpperCase()+ extend.substring(1);
    }


    public static String replace(String s, String oldSub, String newSub) {
        if ((s == null) || (oldSub == null) || (newSub == null)) {
            return null;
        }

        int y = s.indexOf(oldSub);

        if (y >= 0) {

            // The number 5 is arbitrary and is used as extra padding to reduce
            // buffer expansion

            StringBuilder sb = new StringBuilder(
                    s.length() + 5 * newSub.length());

            int length = oldSub.length();
            int x = 0;

            while (x <= y) {
                sb.append(s.substring(x, y));
                sb.append(newSub);

                x = y + length;
                y = s.indexOf(oldSub, x);
            }

            sb.append(s.substring(x));

            return sb.toString();
        }
        else {
            return s;
        }
    }

}
