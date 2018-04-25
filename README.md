# CloudResetPwdAgent README

CloudResetPwdAgent is used for CloudOS (Windows and Linux) password resetting under OpenStack architecture, supporting a wide variety of hosting and virtualization technologies, including Ironic, KVM, Xen, LXC, VMware and more. 

Encrypt and decrypt
-------------------

AES-256-CBC

Requirements
------------

JRE (Java Runtime Environment) and Java Service Wrapper

http://www.oracle.com/technetwork/java/javase/downloads/server-jre8-downloads-2133154.html

https://wrapper.tanukisoftware.com/doc/english/download.jsp


Grabbing the latest source code
-------------------------------

https://github.com/Huawei/CloudResetPwdAgent

Downloading latest versions
---------------------------

https://github.com/Huawei/CloudResetPwdAgent/archive/master.zip

How to build the source code
----------------------------

You can build it into jar and run it with JRE (Java Runtime Environment), also you can use the Java Service Wrapper makes it possible to install a Java Application as a Windows and Linux Service. Likewise, the scripts shipped with the Wrapper also make it very easy to install a Java Application as a Daemon process on Windows and Linux systems.

Ps: Donot forget to modify the $OBS_Domain_URL.com to your owner OBS Domain URL address if want to build it by yourself.

How to download the binary file
-------------------------------

http://cn-south-1-cloud-reset-pwd.obs.myhwclouds.com/windows/reset_pwd_agent/CloudResetPwdAgent.zip

http://cn-south-1-cloud-reset-pwd.obs.myhwclouds.com/linux/32/reset_pwd_agent/CloudResetPwdAgent.zip

http://cn-south-1-cloud-reset-pwd.obs.myhwclouds.com/linux/64/reset_pwd_agent/CloudResetPwdAgent.zip

Distributed license
-------------------

CloudResetPwdAgent is distributed under the terms of the GNU General Public License v2.0. The full terms and conditions of this license are detailed in the LICENSE file.

Contact & Contribute
--------------------

For information on how to contribute to CloudResetPwdAgent, please feel free to contact me with email or send pull requests directly.

-- End of broadcast
