import constant.RestResult;
import constant.RestType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by q00420768 on 2017/6/28.
 */
public class ResetPwdFlow {
    private static Logger logger = LogManager.getLogger(ResetPwdFlow.class);

    //flag的请求地址
    private static final String RESET_PWD_FLAG_URL = "http://169.254.169.254/openstack/latest/resetpwd_flag";
    //rest pwd的请求地址
    private static final String RESET_PASSWORD_URL = "http://169.254.169.254/openstack/latest/reset_password";
    //mata_data 请求地址
    private static final String MATA_DAYA_URL = "http://169.254.169.254/openstack/latest/meta_data.json";

    //mata_date key
    private static final String UUID = "uuid";
    private static final String RESET_PASSWORD = "reset_password";
    private static final String RESET_PWD_FLAG = "resetpwd_flag";
    //请求flag的时间间隔，2000ms
    private static final long FLAG_REQUEST_INTERVAL_TIMES = 2000;
    //请求reset_password的时间间隔，2000ms
    private static final long PASSWORD_REQUEST_INTERVAL_TIMES = 2000;

    //算法名
    public static final String KEY_ALGORITHM = "AES";
    //加解密算法/模式/填充方式
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    //重置密码指令 windows
    private static final String RESET_PWD_CMD_WINDOWS = "net user Administrator ";
    //重置密码指令 linux
    private static final String RESET_PWD_CMD_LINUX_FRONT = "echo 'root:";
    private static final String RESET_PWD_CMD_LINUX_TAIL = "' | chpasswd";

    public void run() {


        //get resetpwd_flag请求
        String flag = getFlag();
        if (flag == null || !RestResult.TRUE.equals(flag)) {
            setFlag();
        }


        //获取 rest_password
        String resetPassword = getPWD();
        if (resetPassword == null || RestResult.NONE.equals(resetPassword) || resetPassword.length() == 0) {
            return;
        }


        //get mata_data  请求
        String uuid = getUuid();
        logger.info("...........start to getUuid ...........");
        if (uuid == null) {
            return;
        }

        // 获得key
        byte[] key = getAesKey(uuid, resetPassword);
        if (key == null) {
            return;
        }

        //获得明文密码
        String decryptedPassword = resetPassword.substring(12, resetPassword.length());
        String password = aesCbcDecrypt(decryptedPassword, key, key);
        //logger.info("password is "+password);
        //密码重置
        resetVMPwd(password);

        //删除密码
        deleteResetPwd();


    }

    //删除reset_password
    private void deleteResetPwd() {

        for (int i = 0; i < 10; i++) {
            try {
                curl(RestType.DELETE, RESET_PASSWORD_URL);
                Thread.sleep(PASSWORD_REQUEST_INTERVAL_TIMES);
                String resetPwd = getResetPwdAlone();
                if (resetPwd == null || resetPwd.length() == 0) {
                    return;
                }


            } catch (InterruptedException e) {
                logger.error(e);
            }
        }

    }

    public String getUuid(){
        //get mata_data  多次请求,
        String result = null;
        String resultUuid = getUuidAlone();
        if (resultUuid == null || RestResult.FAIL.equals(resultUuid)) {
            for (int i = 0; i < 10; i++) {
                //间隔2s
                try {
                    Thread.sleep(PASSWORD_REQUEST_INTERVAL_TIMES);
                    resultUuid = getUuidAlone();
                    if (resultUuid != null && !RestResult.FAIL.equals(resultUuid)) {
                        result = resultUuid;
                        return result;
                    }
                } catch (InterruptedException e) {
                    logger.error("getUuid error", e);
                }
            }
            return null;
        } else {
            result = resultUuid;
            return result;
        }
    }

    public String getUuidAlone() {
        //get mata_data  请求
        logger.info("start the func of getUuidAlone() .........");
        String mata_data = curl(RestType.GET, MATA_DAYA_URL);
        if (mata_data == null) {
            return null;
        }
        String uuid = null;
        try {
            JSONObject mataDataJson = new JSONObject(mata_data);
            uuid = mataDataJson.getString(UUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return uuid;
    }

    private void resetVMPwd(String password) {
        Runtime rt = Runtime.getRuntime();
        //获取进程
        Process p = null;//或者 Process p = rt.exec(String cmd);
        try {

            String cmds = "cmd /c net user Administrator " + password;

            p = rt.exec(cmds);

            // 记录dos命令的返回信息
//            StringBuffer resStr = new StringBuffer();
//            // 获取返回信息的流
//            InputStream in = p.getInputStream();

//            BufferedReader bReader =new  BufferedReader( new InputStreamReader(in, Charset.forName("GBK")));
//            for (String res = ""; (res = bReader.readLine()) != null;)
//            {
//                resStr.append(res + "\n");
//            }
//            logger.info("cmd result is "+resStr);
//            bReader.close();
            //如果p不为空，那么要清空

            if (null != p) {
                try {
                    p.waitFor();

                } catch (InterruptedException e) {
                    logger.error(e);
                }
                p.destroy();
                p = null;
            }
        } catch (IOException e) {
            logger.error(e);
        }

    }

    //得到key
    public byte[] getAesKey(String uuid, String encryptedData) {
        int iterations = 5000;
        String salt = getSault(encryptedData);
        System.out.println(salt);
        BASE64Decoder decoder = new BASE64Decoder();

        try {
            byte[] saltBytes = decoder.decodeBuffer(salt);
            PBEKeySpec spec = new PBEKeySpec(uuid.toCharArray(), saltBytes, iterations, 128);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return hash;
        } catch (IOException e) {
            logger.error(e);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
        } catch (InvalidKeySpecException e) {
            logger.error(e);
        }

        return null;
    }


    private String getSault(String encryptedData) {
        if (encryptedData == null || encryptedData.length() < 12)
            return null;

        return encryptedData.substring(0, 12);
    }

    //密码解密
    public String aesCbcDecrypt(String encryptedData, byte[] key, byte[] iv) {

        BASE64Decoder decoder = new BASE64Decoder();

        try {
            byte[] passData = decoder.decodeBuffer(encryptedData);
            Key scretkey = convertToKey(key);
            // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, scretkey, new IvParameterSpec(iv));
            byte[] result = cipher.doFinal(passData);
            return new String(result);
        } catch (IOException e) {
            logger.error(e);
            return null;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error(e);
            return null;
        }

    }

    //转化成JAVA的密钥格式
    public static Key convertToKey(byte[] keyBytes) throws Exception {
        SecretKey secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        return secretKey;
    }

    private String getPWD() {
        String result = null;
        String resetPwd = getResetPwdAlone();

        if (resetPwd == null || RestResult.FAIL.equals(resetPwd)) {
            for (int i = 0; i < 10; i++) {
                //间隔2s
                try {
                    Thread.sleep(PASSWORD_REQUEST_INTERVAL_TIMES);
                    resetPwd = getResetPwdAlone();
                    if (resetPwd != null && !RestResult.FAIL.equals(resetPwd)) {
                        result = resetPwd;
                        return result;
                    }
                } catch (InterruptedException e) {
                    logger.error("getPWD error", e);
                }
            }
            return null;
        } else {
            result = resetPwd;
            return result;
        }


    }

    public String getResetPwdAlone() {
        String resetPwdJsonStr = curl(RestType.GET, RESET_PASSWORD_URL);
        if (resetPwdJsonStr == null) {
            return null;
        }
        String resetPassword = null;
        try {
            JSONObject resetPwdJson = new JSONObject(resetPwdJsonStr);
            resetPassword = resetPwdJson.getString(RESET_PASSWORD);
            if (resetPassword == null) {
                return null;
            }
            return resetPassword;
        } catch (JSONException e) {
            logger.error("getResetPwdAlone error,", e);
            return null;
        }
    }

    //post 设置flag
    public void setFlag() {
        String flag = null;
        for (int i = 0; i < 10; i++) {
            curl(RestType.POST, RESET_PWD_FLAG_URL);
            try {
                //间隔2s
                Thread.sleep(FLAG_REQUEST_INTERVAL_TIMES);
                flag = getFlagAlone();

                if (RestResult.TRUE.equals(flag)) {
                    return;
                }
            } catch (InterruptedException e) {
                logger.error("set flag error,", e);
            }
        }

    }

    public String getFlag(){
        String result = null;
        String resultFlag = getFlagAlone();

        if (resultFlag == null || RestResult.FAIL.equals(resultFlag)) {
            for (int i = 0; i < 10; i++) {
                //间隔2s
                try {
                    Thread.sleep(PASSWORD_REQUEST_INTERVAL_TIMES);
                    resultFlag = getFlagAlone();
                    if (resultFlag != null && !RestResult.FAIL.equals(resultFlag)) {
                        result = resultFlag;
                        return result;
                    }
                } catch (InterruptedException e) {
                    logger.error("getFlag error", e);
                }
            }
            return null;
        } else {
            result = resultFlag;
            return result;
        }

    }

    public String getFlagAlone() {

        String flgJsonStr = curl(RestType.GET, RESET_PWD_FLAG_URL);
        if (flgJsonStr == null) {
            return null;
        }
        String flag = null;
        try {
            JSONObject flagJson = new JSONObject(flgJsonStr);
            flag = flagJson.getString(RESET_PWD_FLAG);

            if (flag == null) {
                return null;
            }
            return flag;
        } catch (JSONException e) {
            logger.error("getFlagAlone error:", e);
            return null;
        }

    }


    //用http发起curl请求
    public String curl(String type, String url) {

        String result;
        URL urlOb = null;
        try {
            urlOb = new URL(url);
        } catch (MalformedURLException e) {
            logger.error("curl new URL() exception", e);
            return null;
        }
        try {
            if (urlOb == null) {
                return null;
            }
            HttpURLConnection conn = (HttpURLConnection) urlOb.openConnection();
            conn.setRequestMethod(type);
            if (RestType.GET.equals(type)) {
                conn.setConnectTimeout(3000);//连接超时 单位毫秒
                conn.setReadTimeout(2000);//读取超时 单位毫秒
                InputStream inStream = conn.getInputStream();
                int count = 0;
                while (count == 0) {
                    count = inStream.available();
                }
                byte[] bytes = new byte[count];
                inStream.read(bytes, 0, inStream.available());
                result = new String(bytes);

                return result;
            }
            if (RestType.POST.equals(type)) {
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.connect();

                PrintWriter out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.write("True");//post的参数 xx=xx&yy=yy
                // flush输出流的缓冲
                out.flush();
                int responseCode = conn.getResponseCode();
                logger.info("responseCode:." + responseCode + "..............................");
                logger.info("post...............................");
                return null;
            }
            if (RestType.DELETE.equals(type)) {
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();
                int responseCode = conn.getResponseCode();
                logger.info("responseCode:." + responseCode + "..............................");
                logger.info("delete...............................");
                return null;
            }

        } catch (IOException e) {
            logger.error("curl exception", e);
            return null;
        }
        return null;

    }
}
