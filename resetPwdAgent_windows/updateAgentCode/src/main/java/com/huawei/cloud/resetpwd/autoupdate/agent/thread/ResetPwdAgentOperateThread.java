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

        String path = obsUrl;
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
        logger.info("begin windows update");
        Runtime rt = Runtime.getRuntime();
        try {
            boolean isPending = checkStatus("cloudResetPwdAgent", "PENDING");
            if (isPending) {
                int dalayTime = 5000;
                String dalayTimeStr = ConfigInfo.UPDATE_DALAY_START_TIME;
                if (null != dalayTimeStr) {
                    dalayTime = Integer.parseInt(dalayTimeStr);
                }
                Thread.currentThread().sleep(dalayTime);
            }
            int isStop = rt.exec(new String[]{"cmd", "/c", "net stop cloudResetPwdAgent"})
                    .waitFor();
            String[] urlName = ConfigInfo.UPDATE_FILE_PATH.split("/");
            String uName = urlName[urlName.length - 1];
            unzipFiles(ConfigInfo.DOWNLOAD_FILE_PATH + "\\" + uName,
                    ConfigInfo.DOWNLOAD_FILE_PATH);
            boolean isCover = coverDirectory(ConfigInfo.DOWNLOAD_FILE_PATH + ConfigInfo.COVER_FILE_PATH,
                    ConfigInfo.INSTALL_PATH);

            int isStart = rt.exec(new String[]{"cmd", "/c", "net start cloudResetPwdAgent"}).waitFor();

            if (!isCover) {
                logger.error(COVER_FILE_ERROR);
                return false;
            }
            logger.info("end windows update success");
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    private boolean checkStatus(String serviceName, String status) throws IOException {
        Process pro = Runtime.getRuntime().exec("sc query " + serviceName);
        InputStream inputStream = pro.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);
        StringBuffer stringBuffer = new StringBuffer();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            stringBuffer.append(line);
        }
        String cmdResult = stringBuffer.toString();
        boolean isMatchStatus = false;
        if (null != cmdResult && cmdResult.contains(status)) {
            isMatchStatus = true;
        }
        return isMatchStatus;
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

    private boolean coverDirectory(String srcDirectoryPath, String desDirectoryPath) {
        try {
            copyDirectory(srcDirectoryPath, desDirectoryPath);
            return true;
        } catch (IOException e) {
            logger.error(e);
            return false;
        }
    }

    private boolean copyDirectory(String srcDirectoryPath, String desDirectoryPath) throws IOException {
        File src = new File(srcDirectoryPath);
        File dest = new File(desDirectoryPath);
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdir();
            }
            String files[] = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                // 递归复制
                copyDirectory(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            }
        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
        }
        return true;
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
