package com.huawei.cloud.resetpwd.autoupdate.agent.util;


import com.huawei.cloud.resetpwd.autoupdate.agent.constant.ConfigInfo;
import com.huawei.cloud.resetpwd.autoupdate.agent.constant.RestResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class ObsUrlUtil {

    private static Logger logger = LogManager.getLogger(ObsUrlUtil.class);
    private static String charset = "utf-8";
    private static final String MATA_DATA_URL = "http://169.254.169.254/openstack/latest/meta_data.json";
    private static final String AZ = "availability_zone";
    private static final int PASSWORD_REQUEST_INTERVAL_TIMES = 2;


    public static String getObsUrl() {

        logger.info("start to get ObsUrl ...");
        String region = getRegion();
        String obsUrl = null;

        String defualtRegion = PropertiesUtil.getValue("defualtRegion");
        String isHEC = PropertiesUtil.getValue("isHEC");
        String isCTC = PropertiesUtil.getValue("isCTC");
        String bucket = PropertiesUtil.getValue("bucket");


        if ("True".equals(isHEC)) {
            logger.info("start to get HEC ObsUrl ...");
            String regionObsUrl = "http://" + region + "-" + bucket + ".obs." + region + ".$OBS_Domain_URL.com";
            if (isOBSConnetion(regionObsUrl + ConfigInfo.REMOTE_VERSION_FILE)) {
                logger.info("The accessed obsUrl is [{}]..........", obsUrl);
                obsUrl = regionObsUrl;
                return obsUrl;
            } else {
                String defualtObsUrl = "http://" + defualtRegion + "-" + bucket + ".obs." + defualtRegion + ".$OBS_Domain_URL.com";
                if (isOBSConnetion(defualtObsUrl + ConfigInfo.REMOTE_VERSION_FILE)) {
                    obsUrl = defualtObsUrl;
                    logger.info("The accessed obsUrl is [{}]..........", obsUrl);
                    return obsUrl;
                } else {
                    logger.info("URL access failed ...");
                }
            }
        }

        if (obsUrl == null || "".equals(obsUrl)) {
            obsUrl = PropertiesUtil.getValue("defualtUrl");
        }

        logger.info("The accessed obsUrl is " + obsUrl);
        return obsUrl;

    }

    private static String getRegion() {

        logger.info("start to get RegionName ...");
        String result = null;
        String resultRegionname = getRegionNameAlone();

        if (resultRegionname == null) {
            for (int i = 0; i < 10; i++) {
                //间隔2s
                try {
                    Thread.sleep(PASSWORD_REQUEST_INTERVAL_TIMES);
                    resultRegionname = getRegionNameAlone();
                    if (resultRegionname != null) {
                        result = resultRegionname;
                        return result;
                    }
                } catch (InterruptedException e) {
                    logger.info("getRegionName error", e);
                }
            }
            logger.info("get RegionName failed!!!!!!!!!!!!!!!...");
            return null;
        } else {
            result = resultRegionname;
            return result;
        }

    }

    private static String getRegionNameAlone() {

        String mata_data = null;
        try {
            mata_data = HttpRequestUtil.sendGetMethod(MATA_DATA_URL);
        } catch (Exception e) {
            logger.error("get metadata error", e);
            return null;
        }
        if(mata_data == null){
            return null;
        }
        String az = null;
        try {
            JSONObject mataDataJson = new JSONObject(mata_data);
            az = mataDataJson.getString(AZ);
        } catch (Exception e) {
            logger.error("get az from metadata error", e);
            return null;
        }
        if (az != null && az.length() > 0) {
            String regionName = az.substring(0, az.length() - 1);
            return regionName;
        } else {
            return null;
        }

    }

    private static boolean isOBSConnetion(String obsUrl) {

        URL remoteUrl = null;
        try {
            remoteUrl = new URL(obsUrl);
        } catch (MalformedURLException e) {
            logger.error(e);
        }
        try {
            URLConnection connection = remoteUrl.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            httpURLConnection.setRequestProperty("Accept-Charset", charset);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestMethod("GET");

            int state = httpURLConnection.getResponseCode();
            if (state == 200) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return false;
    }
}
