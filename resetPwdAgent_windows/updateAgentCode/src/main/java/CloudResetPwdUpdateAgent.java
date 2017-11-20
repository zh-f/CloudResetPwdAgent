import com.huawei.cloud.resetpwd.autoupdate.agent.thread.ResetPwdUpdateFlowThread;
import com.huawei.cloud.resetpwd.autoupdate.agent.util.PropertiesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hWX467787 on 2017/8/10.
 */
public class CloudResetPwdUpdateAgent {

    private static Logger logger = LogManager.getLogger(CloudResetPwdUpdateAgent.class);
    private static final int DEFAULT_INTERVAL_TIME = 21600;

    public static void main(String[] args) {
        logger.info("CloudResetPwdUpdateAgent is start...");


        String intervalTimeStr = PropertiesUtil.getValue("intervalTime");
        //默认间隔6个小时
        int intervalTime = DEFAULT_INTERVAL_TIME;
        try {
            intervalTime = Integer.parseInt(intervalTimeStr);
        } catch (Exception e) {
            logger.error(e);
            intervalTime = DEFAULT_INTERVAL_TIME;
        }

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                logger.info("UpdateTask is start...");
                ResetPwdUpdateFlowThread agent = new ResetPwdUpdateFlowThread();
                agent.start();
            }

        }, 1000, intervalTime * 1000);
    }
}