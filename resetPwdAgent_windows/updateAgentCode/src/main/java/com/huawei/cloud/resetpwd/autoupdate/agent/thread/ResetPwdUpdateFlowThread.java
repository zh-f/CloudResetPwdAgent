package com.huawei.cloud.resetpwd.autoupdate.agent.thread;

import com.huawei.cloud.resetpwd.autoupdate.agent.constant.ConfigInfo;
import com.huawei.cloud.resetpwd.autoupdate.agent.util.HttpRequestUtil;
import com.huawei.cloud.resetpwd.autoupdate.agent.util.ObsUrlUtil;
import com.huawei.cloud.resetpwd.autoupdate.agent.util.PropertiesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class ResetPwdUpdateFlowThread extends Thread {
    private static Logger logger = LogManager.getLogger(ResetPwdUpdateFlowThread.class);

    public void run() {
        String localAgentVersion = PropertiesUtil.getValue("resetPwdAgentVersion");
        String remoteAgentVersion = getRemoteAgentVersion(ObsUrlUtil.getObsUrl(),
                ConfigInfo.REMOTE_VERSION_FILE, ConfigInfo.RESET_PWD_AGENT_VERSION_KEY);
        logger.info("localAgentVersion is: " + localAgentVersion);
        logger.info("remoteAgentVersion is: " + remoteAgentVersion);

        if (isNeedUpdate(localAgentVersion, remoteAgentVersion)) {
            try {
                ResetPwdAgentOperateThread operate = new ResetPwdAgentOperateThread();
                operate.start();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

    //读取配置文件的UDS桶的URL地址获取远程Agent版本号
    private String getRemoteAgentVersion(String obsUrl, String remoteVersionFile, String agentVersionKey) {
        String remoteAgentVersion = "";

        try {
            String body = HttpRequestUtil.sendGetMethod(obsUrl + remoteVersionFile);
            logger.info("version:" + body);

            String[] tempLine = body.split("\n");
            for (int i = 0; i < tempLine.length; i++) {
                if(tempLine[i].indexOf(agentVersionKey) >= 0) {
                    remoteAgentVersion = tempLine[i].split("=")[1];
                }
                i++;
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return remoteAgentVersion;
    }

    //对比Agent版本号
    private boolean isNeedUpdate(String localAgentVersion, String remoteAgentVersion) {
        if ("".equals(remoteAgentVersion) || localAgentVersion.equals(remoteAgentVersion)) {
            return false;
        }

        String[] localArr = localAgentVersion.replace("\r", "").split("\\.");
        String[] remoteArr = remoteAgentVersion.replace("\r", "").split("\\.");
        int versionNumber = Math.min(localArr.length, remoteArr.length);

        for (int i = 0; i < versionNumber; i++) {
            int local = Integer.parseInt(localArr[i]);
            int remote = Integer.parseInt(remoteArr[i]);

            if (local < remote) {
                return true;
            } else if (local > remote) {
                return false;
            }
        }

        if (versionNumber == localArr.length) {
            return true;
        }
        return false;
    }
}