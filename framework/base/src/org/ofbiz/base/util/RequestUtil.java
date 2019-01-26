package org.ofbiz.base.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Alex on 2016/5/6.
 */
public class RequestUtil {

    public static String convertStreamToString(InputStream is) {
        StringBuilder sb = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));

            String line = null;
            while ((line = reader.readLine()) != null) {

                sb.append(line);

            }

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

            try {

                is.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

        }



        return sb.toString();

    }

}
