@echo off
echo "begin install cloud-reset-pwd-agent"

set CUR_DIR=%cd%
cd..
set LAST_DIR=%cd%
set DEPENDENT=%CUR_DIR%\depend

set XCOPY=C:\Windows\System32\xcopy.exe
set SYS_DIR=C:\Windows\System32\
set RESETPWDAGENT=C:\CloudResetPwdAgent
set CLOUDRESETPWDAGENT=C:\CloudResetPwdAgent\
set /a OS_BIT=0

echo %CUR_DIR%
call :installDependence
if %errorlevel% neq 0 (

    echo "install dependences failed"

    goto :errorlevel

) else (

    echo "install dependences success"

)

call :installResetPwdAgent
if %errorlevel% neq 0 (

    echo "install CloudResetPwdAgent failed"

    goto :error

) else (

    echo "install CloudResetPwdAgent success"

	goto :done
)

:: ============Function: installDependence===========

:: instasll dependence

:: ===============================================
:installDependence
echo "begin install dependece"
%XCOPY% /E /C /Y %CUR_DIR%\cloudResetPwdAgent\* %CLOUDRESETPWDAGENT%
goto :eof

:: ============Function: installResetPwdAgent===========

:: insallt installResetPwdAgent

:: ===============================================
:installResetPwdAgent
echo "begin install installResetPwdAgent"
call %RESETPWDAGENT%\bin\InstallApp-NT.bat install
call %RESETPWDAGENT%\bin\App.bat start
goto :eof


:done

echo SUCCESS: Congratulations! CloudResetPwdAgent installed successfully. 

pause

exit /B 0


:: if failed

:error

echo FAILED: Sorry! CloudResetPwdAgent install failed, check for detail

pause

exit /B 1