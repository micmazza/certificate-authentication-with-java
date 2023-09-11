# Client

## Build the client on Windows

**Java 8**

    set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_241

**Java 11**

    set JAVA_HOME=C:\Program Files\Java\jdk-11.0.17

    set PATH=%JAVA_HOME%\bin;%PATH%
    set MAVEN_OPTS=-Xms256m -Xmx1024m
    set M3_HOME=%USERPROFILE%\work\programs\apache-maven-3.3.9
    set MAVEN_HOME=%M3_HOME%
    set M3=%M3_HOME%\bin
    set PATH=%M3%;%PATH%

    set WORK=%USERPROFILE%\work\eclipse-workspace
    cd %WORK%\certificate-authentication-with-java\client
    mvn clean package


## Setup to run the client on Linux

**Java 8**

    export JAVA_HOME=/etc/alternatives/jre_1.8.0

**Java 11**

    export JAVA_HOME=/etc/alternatives/jre_11_openjdk

    export PATH=$JAVA_HOME/bin:$PATH
    export SERVER_IP=172.24.31.22
    export SERVER_PORT=10443
    export WORK=/root/mazzami/certificates
    mkdir -p $WORK/root-ca

    cd $WORK

**Tranfer client-0.0.1-SNAPSHOT.jar under $WORK**
