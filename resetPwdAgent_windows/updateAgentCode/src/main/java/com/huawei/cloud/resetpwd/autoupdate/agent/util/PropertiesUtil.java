package com.huawei.cloud.resetpwd.autoupdate.agent.util;

import com.huawei.cloud.resetpwd.autoupdate.agent.constant.ConfigInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by hWX467787 on 2017/8/10.
 */
public class PropertiesUtil {
    private static Logger logger = LogManager.getLogger(PropertiesUtil.class);

    private static Properties getProperties() {
        InputStream inStream = null;
        Properties properties = null;
        String templatePath = ConfigInfo.RESOURCE_PATH;
        try {
            inStream = new FileInputStream(templatePath);
            properties = new Properties();
            properties.load(inStream);
        } catch (Exception e) {
            logger.error("load properties fail:" + e);
        } finally {
            if (null != inStream) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
        }
        return properties;
    }

    public static String getValue(String key) {
        String value = getProperties().getProperty(key);
        return value;
    }

    public static void setValue(String key, String value) {
        Properties prop = getProperties();
        prop.setProperty(key, value);
        FileOutputStream outputStream = null;
        String filePath = ConfigInfo.RESOURCE_PATH;
        try {
            outputStream = new FileOutputStream(filePath);
            prop.store(outputStream, "update message");
            logger.info("update version information end.");
        } catch (Exception e) {
            logger.error(e);
        } finally {
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }
}
