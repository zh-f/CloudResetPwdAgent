CUR_DIR=`pwd`
OS_BIT=0
IS_SUSE=1
SUSE_DIR='/etc/SuSE-release'
#!/bin/sh
checkSuse()
{
    if [ -f "$SUSE_DIR" ]; then 
        IS_SUSE=0
    fi
}
install()
{
echo "begin install CloudResetPwdAgent"
echo "===============cp LinuxCloudResetPwdAgent======================"

#if [ ! -d "../depend_linux" ]; then
#   echo "depend_linux is not exit,install failed"
#  exit 1
#fi
#cp -rf ../depend_linux/jre1.8.0_131 $CUR_DIR/CloudrResetPwdAgent/depend
#cp ../depend_linux/libwrapper.so  $CUR_DIR/CloudrResetPwdAgent/lib
#cp ../depend_linux/wrapper.jar  $CUR_DIR/CloudrResetPwdAgent/lib

cp -rf $CUR_DIR/CloudrResetPwdAgent /

echo "===============vi /etc/profile======================"

echo "===============begin install CloudrResetPwdAgent==================="
cd /CloudrResetPwdAgent/bin
chmod +x cloudResetPwdAgent.script
./cloudResetPwdAgent.script install
./cloudResetPwdAgent.script start
cd -


if [ $? -eq 0 ]; then
        echo "cloudResetPwdAgent install successfully."
            return 0
    else
        echo "cloudResetPwdAgent install failed."
        return 1
    fi
}
checkSuse || { exit 1; }
install || { exit 1; }
exit 0
