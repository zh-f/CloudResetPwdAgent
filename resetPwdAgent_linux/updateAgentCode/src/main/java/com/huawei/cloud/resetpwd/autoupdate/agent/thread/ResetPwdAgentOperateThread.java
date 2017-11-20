package com.huawei.cloud.resetpwd.autoupdate.agent.thread;

import com.huawei.cloud.resetpwd.autoupdate.agent.constant.ConfigInfo;
import com.huawei.cloud.resetpwd.autoupdate.agent.util.HttpRequestUtil;
import com.huawei.cloud.resetpwd.autoupdate.agent.util.ObsUrlUtil;
import com.huawei.cloud.resetpwd.autoupdate.agent.util.PropertiesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by hWX467787 on 2017/8/10.
 */
public class ResetPwdAgentOperateThread extends Thread {
    private static Logger logger = LogManager.getLogger(ResetPwdAgentOperateThread.class);

    public static final String DOWNLOAD_ERROR = "download agent fail";
    public static final String COVER_FILE_ERROR = "cover agent files fail";
    public static final String REINSTALL_ERROR = "reinstall agent fail";
    public static final String COMPLETED = "update agent completed";

    public void run() {
        logger.info("ResetPwdAgentOperateThread is start...");
        try {
            String obsUrl = ObsUrlUtil.getObsUrl();
            String remoteVersion = getRemoteVersion(obsUrl, ConfigInfo.REMOTE_VERSION_FILE,
                    ConfigInfo.RESET_PWD_AGENT_VERSION_KEY);

            boolean isDownload = downloadResetPwdNewAgent(obsUrl);
            if (!isDownload) {
                logger.error(DOWNLOAD_ERROR);
                return;
            }

            boolean isReinstall = reinstallResetPwdAgent();
            if (!isReinstall) {
                logger.error(REINSTALL_ERROR);
                return;
            }

            updateVesionInfo(ConfigInfo.RESET_PWD_AGENT_VERSION_KEY, remoteVersion);

            logger.info(COMPLETED);
        } catch (Exception e) {
            logger.error(e);
        }
        logger.info("ResetPwdAgentOperateThread is end...");
    }

    private String getRemoteVersion(String obsUrl, String remoteVersionFile, String agentVersionKey) {
        String remoteVersion = "";
        try {
            String body = HttpRequestUtil.sendGetMethod(obsUrl + remoteVersionFile);
            logger.info("version:" + body);

            String[] tempLine = body.split("\n");
            for (int i = 0; i < tempLine.length; i++) {
                if (tempLine[i].indexOf(agentVersionKey) >= 0) {
                    remoteVersion = tempLine[i].split("=")[1];
                }
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return remoteVersion;
    }

    private boolean downloadResetPwdNewAgent(String obsUrl) throws Exception {
        String dir = ConfigInfo.DOWNLOAD_FILE_PATH;
        File savePath = new File(dir);
        if (!savePath.exists()) {
            savePath.mkdir();
        }

        String path =obsUrl;
        String updateURL = ConfigInfo.UPDATE_FILE_PATH;
        String[] urlName = ConfigInfo.UPDATE_FILE_PATH.split("/");
        String uName = urlName[urlName.length - 1];
        File file = new File(savePath + "/" + uName);
        InputStream ips = null;
        OutputStream ops = null;
        HttpURLConnection httpURLConnection = null;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            ops = new FileOutputStream(file);
            URL url = new URL(path + updateURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            ips = httpURLConnection.getInputStream();
            logger.info("file size is: " + httpURLConnection.getContentLength());

            byte[] buffer = new byte[1024];
            int byteRead = -1;
            while ((byteRead = (ips.read(buffer))) != -1) {
                ops.write(buffer, 0, byteRead);
            }
            ops.flush();
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        } finally {
            if (null != ips) {
                ips.close();
            }
            if (null != ops) {
                ops.close();
            }
            if (null != httpURLConnection) {
                httpURLConnection.disconnect();
            }
        }
    }

    private boolean reinstallResetPwdAgent() {
        logger.info("linux begin update");
        Runtime rt = Runtime.getRuntime();
        try {
            rt.exec(new String[]{ConfigInfo.INSTALL_PATH +
                    "/bin/cloudResetPwdAgent.script", "stop"}).waitFor();

            String[] urlName = ConfigInfo.UPDATE_FILE_PATH.split("/");
            String uName = urlName[urlName.length - 1];
            unzipFiles(ConfigInfo.DOWNLOAD_FILE_PATH + "/" + uName,
                    ConfigInfo.DOWNLOAD_FILE_PATH);
            int isCover = rt.exec(new String[]{ "cp", "-R",
                    ConfigInfo.COVER_FILE_PATH, "/"}).waitFor();
            if (0 != isCover) {
                logger.error(COVER_FILE_ERROR);
                return false;
            }
            logger.info("cover files success");

            rt.exec(new String[]{ ConfigInfo.INSTALL_PATH + "/bin/cloudResetPwdAgent.script",
                    "start"}).waitFor();
            logger.info("restart success");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    private void unzipFiles(String zipFile, String descDir) throws IOException {
        logger.info("unzipFiles is start...");
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        ZipFile zip = new ZipFile(zipFile);
        for (Enumeration entries = zip.entries();
             entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = zip.getInputStream(entry);
            String outPath = (descDir + "/" + zipEntryName).replaceAll("\\*", "/");
            //判断路径是否存在，不存在则创建文件路径
            File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
            if (!file.exists()) {
                file.mkdirs();
            }
            //判断文件全路径是否为文件夹，如果是上面已经上传，不需要解压
            if (new File(outPath).isDirectory()) {
                continue;
            }

            OutputStream out = new FileOutputStream(outPath);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        logger.info("unzipFiles is end...");
    }

    private void updateVesionInfo(String agentVersionKey, String value) {
        try {
            if (null != value && !"".equals(value)) {
                PropertiesUtil.setValue(agentVersionKey, value);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
