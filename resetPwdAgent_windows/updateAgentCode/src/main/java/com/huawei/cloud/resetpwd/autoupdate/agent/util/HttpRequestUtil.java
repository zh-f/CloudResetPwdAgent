package com.huawei.cloud.resetpwd.autoupdate.agent.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by hWX467787 on 2017/8/10.
 */
public class HttpRequestUtil {
    private static Logger logger = LogManager.getLogger(HttpRequestUtil.class);
    private static String charset = "utf-8";

    public static String sendGetMethod(String url) throws Exception {

        URL remoteUrl = new URL(url);
        URLConnection connection = remoteUrl.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
        httpURLConnection.setRequestProperty("Accept-Charset", charset);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestMethod("GET");

        InputStream inputStream = null;
        String result = null;
        try {
            inputStream = httpURLConnection.getInputStream();
            int count = 0;
            while (count == 0) {
                count = inputStream.available();
            }
            byte[] bytes = new byte[count];
            inputStream.read(bytes, 0, inputStream.available());
            result = new String(bytes);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
        }

        return result;
    }
}
