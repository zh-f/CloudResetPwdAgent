@echo off
echo "begin install agent and update-agent"

set CUR_DIR=%cd%
cd ..
set LAST_DIR=%cd%

set XCOPY=C:\Windows\System32\xcopy.exe
set SYS_DIR=C:\Windows\System32\
set UPDATEAGENT=C:\CloudResetPwdUpdateAgent
set CLOUDUPDATEAGENT=C:\CloudResetPwdUpdateAgent\

echo %CUR_DIR%
call :installupdateAgent
if %errorlevel% neq 0
(
	echo "install updateAgent failed"
	goto :error
)
else
(
	echo "install updateAgent success"
	goto :done
)

:installupdateAgent
echo "begin install updateAgent"
%XCOPY% /E /C /Y %CUR_DIR%\CloudResetPwdUpdateAgent\* %CLOUDUPDATEAGENT%
call %UPDATEAGENT%\bin\InstallApp-NT.bat install
net start cloudResetPwdUpdateAgent

cd CloudResetPwdAgent.Windows\
call setup.bat

:done
echo SUCCESS: Congratulations! CloudResetPwdAgentUpdate installed successfully. 

pause
exit 0

:error
echo FAILED: Sorry! CloudResetPwdAgentUpdate install failed, check for detail.

pause
exit 1
