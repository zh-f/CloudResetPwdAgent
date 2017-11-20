#!/bin/sh
CUR_DIR=`pwd`
OS_BIT=0
IS_SUSE=1
SUSE_DIR='/etc/SuSE-release'

checkSuse()
{
    if [ -f "$SUSE_DIR" ]; then 
        IS_SUSE=0
    fi
}

install()
{
    echo "begin install CloudResetPwdUpdateAgent"
    echo "===============cp LinuxupdateAgent==============="

    cp -rf $CUR_DIR/CloudResetPwdUpdateAgent /

    echo "=================vi /etc/profile================="
    cd /CloudResetPwdUpdateAgent/depend/jre1.8.0_131/bin/
    chmod +x *

	echo "============begin install CloudResetPwdUpdateAgent============"
    cd /CloudResetPwdUpdateAgent/bin
    chmod +x cloudResetPwdUpdateAgent.script
    ./cloudResetPwdUpdateAgent.script install
    ./cloudResetPwdUpdateAgent.script start
    cd -

    if [ $? -eq 0 ]; then
        echo "CloudResetPwdUpdateAgent install successfully."
        return 0
    else
        echo "CloudResetPwdUpdateAgent install failed."
        return 1
    fi
}



cd $CUR_DIR

checkSuse || { exit 1; }
install || { exit 1; }
cd $CUR_DIR
cd ../CloudResetPwdAgent.Linux/
sh setup.sh || { exit 1; }
exit 0

