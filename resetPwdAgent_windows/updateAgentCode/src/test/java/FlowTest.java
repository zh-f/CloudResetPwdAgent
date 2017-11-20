import com.huawei.cloud.resetpwd.autoupdate.agent.util.ObsUrlUtil;
import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

/**
 * Created by q00420768 on 2017/6/29.
 */
public class FlowTest extends TestCase {


    private static Logger logger = LogManager.getLogger("FlowTest");

    //    ResetPwdFlow resetPwdFlow = new ResetPwdFlow();
//
    @Test
    public void testOBSutil() {
        ObsUrlUtil.getObsUrl();

    }


//
//    @Test
//    public void testAes() {
//
//
//        String uuid = "8ae2549a-7b09-4787-8f25-4487d61be128";
//        String decriptPwd = "0Rqv2zwGpvA=brvpQE1/AAkxrniFf9LgkOanJuSq3uPClZ5eCM6JDpRXqGcHACe6ZBWOdqiKRQTM";
//        int uuidLength = uuid.length();
//        int pwdLength = decriptPwd.length();
//        byte[] key = resetPwdFlow.getAesKey(uuid, decriptPwd);
//        String pass = resetPwdFlow.aesCbcDecrypt(decriptPwd.substring(12, decriptPwd.length()), key, key);
//        System.out.println(pass);
//    }
//
//    @Test
//    public void testPost() throws IOException {
//
//        Runtime rt = Runtime.getRuntime();
//        //获取进程
//        Process p = null;//或者 Process p = rt.exec(String cmd);
//        try {
//            // String cmd=RESET_PWD_CMD_LINUX_FRONT + password + RESET_PWD_CMD_LINUX_TAIL;
//            String cmds = "cmd /c  net user administrator qiqin1992!";
//            logger.info("cmd is " + cmds + "...................");
//            p = rt.exec(cmds);
//            logger.info("cmd exec end ...................");
//            // 记录dos命令的返回信息
//            StringBuffer resStr = new StringBuffer();
//            // 获取返回信息的流
//            InputStream in = p.getInputStream();
//
//            BufferedReader bReader =new  BufferedReader( new InputStreamReader(in, Charset.forName("GBK")));
//            for (String res = ""; (res = bReader.readLine()) != null;)
//            {
//                resStr.append(res + "\n");
//            }
//            bReader.close();
//            //reader.close();
//            System.out.println(resStr.toString());
//            //如果p不为空，那么要清空
//            if (null != p) {
//                logger.info(p);
//                p.destroy();
//                p = null;
//                logger.info("cmd process destroy..................");
//            }
//        } catch (IOException e) {
//            logger.error(e);
//        }
//    }


}
