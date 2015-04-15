/home/ann/aispy/readme.txt

The files in this repo are part of an effort to use h2o to predict s&p500.

Installation:

- Install Linux Ubuntu 14.04.2 on your laptop or desktop.
  This demo cannot run in the cloud.
  http://releases.ubuntu.com/14.04/ubuntu-14.04.2-desktop-amd64.iso

- Login as root

- apt-get install gitk wget

- useradd -m -s /bin/bash ann

- passwd ann

- ssh ann@yourhost

- git clone https://github.com/danbikle/aispy.git

H2O fails to work with many versions of Java.
Today, the H2O behind this demo, depends on Oracle-Java-JDK-7.

- Download and install Java from Oracle.com:

http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html

Look for a link named JDK-7

Click the accept-license-radio-button on the web page,
then a link like this should appear:

http://download.oracle.com/otn-pub/java/jdk/7u75-b13/jdk-7u75-linux-x64.tar.gz

- Download it

- tar zxf ~/Downloads/jdk-7u75-linux-x64.tar.gz

- ln -s jdk1.7.0_75 jdk

- jdk/bin/javac -help

- echo 'export JAVA_HOME=/home/ann/jdk'     >> ~ann/.bashrc

- echo 'export PATH=${JAVA_HOME}/bin:$PATH' >> ~ann/.bashrc

- bash

- which javac

- javac -version

- I should install IntelliJ. Browse this URL:

- https://www.jetbrains.com/idea/download/

- Download Community Edition

- tar zxf ~/Downloads/ideaIC-14.1.1.tar.gz

- ln -s idea-IC-141.178.9 idea

- echo 'export PATH=~ann/idea/bin:$PATH' >> ~ann/.bashrc

- ~ann/idea/bin/idea.sh

- During idea startup, install scala plugin

- I should click the open icon if it is presented

- With IntelliJ, open this file: ~ann/aispy/swd/build.gradle

- I should 'click-through' the forms

- I should 'define' Project JDK (which is /home/ann/jdk)

- I should ignore popup about remote repositories

- I should right-click-run this node on LHS: SparklingWaterDroplet

- It should start H2O and calculate some predictions


