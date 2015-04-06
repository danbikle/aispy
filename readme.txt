/home/ann/aispy/readme.txt

The files in this repo are part of an effort to use h2o to predict s&p500.

Installation:

H2O depends on Oracle-Java-JDK:
- Download and install Java from Oracle.com:
http://www.oracle.com/technetwork/java/javase/downloads/index.html
Look for a link named JDK
The link I see today (2015-04-06):
http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

Click the accept-license-radio-button on the web page,
then a link like this should appear:
http://download.oracle.com/otn-pub/java/jdk/8u40-b26/jdk-8u40-linux-x64.tar.gz

- cd ~ann/

- tar zxf ~/Downloads/jdk-8u40-linux-x64.tar.gz 

- ln -s jdk1.8.0_40 jdk

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

- I shold click the open icon if it is presented

- Navigate to this readme.txt and open it





