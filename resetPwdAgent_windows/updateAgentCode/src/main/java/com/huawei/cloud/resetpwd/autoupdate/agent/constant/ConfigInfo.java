package com.huawei.cloud.resetpwd.autoupdate.agent.constant;

import com.huawei.cloud.resetpwd.autoupdate.agent.util.PropertiesUtil;

/**
 * Created by hWX467787 on 2017/8/11.
 */
public class ConfigInfo {
    public static final String RESOURCE_PATH = "C:\\CloudResetPwdUpdateAgent\\conf\\resource.properties";
    public static final String RESET_PWD_AGENT_VERSION_KEY = "resetPwdAgentVersion";

    public static final String OBS_URL = PropertiesUtil.getValue("obsUrl");
    public static final String REMOTE_VERSION_FILE = PropertiesUtil.getValue("remoteVersionFile");
    public static final String DOWNLOAD_FILE_PATH = PropertiesUtil.getValue("downLoadFilePath");
    public static final String UPDATE_FILE_PATH = PropertiesUtil.getValue("updateFilePath");
    public static final String INSTALL_PATH = PropertiesUtil.getValue("installPath");
    public static final String COVER_FILE_PATH = PropertiesUtil.getValue("coverFilePath");
    public static final String UPDATE_DALAY_START_TIME = PropertiesUtil.getValue("updateDelayStartTime");
}
