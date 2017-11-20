import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by q00420768 on 2017/7/1.
 */
public class CloudResetPwdAgent {

    private static Logger logger = LogManager.getLogger(CloudResetPwdAgent.class);

    public static void main(String[] args) {
        //start a new thread to reset the pwd
        logger.info("CloudResetPwdAgent start..........");
        ResetPwdFlow agent = new ResetPwdFlow();
        try {
            agent.run();
        } catch (Exception e) {
            logger.error(e);
        }
        logger.info("CloudResetPwdAgent finish..........");
        return;

    }

}
