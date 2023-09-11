# Java certificates verification

Simple client-server Java implementation that uses keytool generated certificates to do TLS authentication.  

References:  

[Java HTTPS Client Certificate Authentication](https://www.baeldung.com/java-https-client-certificate-authentication/)

[How to Easily Set Up Mutual TLS SSL](https://dzone.com/articles/hakky54mutual-tls-1)


# Java 11


## Environment set up for Java 11

Build the client source on Windows and transfer the executable jar on the client Linux server as described in client/README.md.  

Build the server source on Windows and transfer the executable jar on the serve Linux server server/README.md.  


## Java 11 one-way self-signed certificates verification (TLS 1.3)

Creating a server keystore with a public and private key (on the server)  

    keytool -v -genkeypair -dname "CN=Server,OU=Dev,O=MyLab,C=IT" -keystore server-keystore.p12 -storepass secret -keypass secret -keyalg RSA -keysize 2048 -alias server -validity 3650 -deststoretype pkcs12 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,DNS:p02v01a-rgsi2,IP:$SERVER_IP


Exporting certificate of the server (on the server)  

    keytool -v -exportcert -file server.cer -alias server -keystore server-keystore.p12 -storepass secret -rfc


Creating the truststore for the client and import the certificate of the server (on the client)  

    keytool -v -importcert -file server.cer -alias server -keystore client-truststore.jks -storepass secret -noprompt


Run the server (first)  

    java -Djavax.net.ssl.keyStore=$WORK/server-keystore.p12 -Djavax.net.ssl.keyStorePassword=secret -jar server-0.0.1-SNAPSHOT.jar $SERVER_PORT false

Run the client (after the server)  

    java -Djavax.net.ssl.trustStore=$WORK/client-truststore.jks -Djavax.net.ssl.trustStorePassword=secret -jar client-0.0.1-SNAPSHOT.jar $SERVER_IP $SERVER_PORT


In the server had requested the authentication of the client (running it with ths second argument false), the following exceptions would have been raised:  

**Client**

    javax.net.ssl.SSLHandshakeException: Received fatal alert: bad_certificate  
  
**Server**

    javax.net.ssl.SSLHandshakeException: Empty client certificate chain  


# Java 11 two-way self-signed certificates verification (TLS 1.3)


In addition to what was done in the previous paragraph:  

- Creating a server keystore with a public and private key
- Exporting Certificate of the Server
- Creating the truststore for the client and import the certificate of the server

Creating client certificate (on the client)  

    keytool -v -genkeypair -dname "CN=Client,OU=Dev,O=MyLab,C=IT" -keystore client-keystore.p12 -storepass secret -keypass secret -keyalg RSA -keysize 2048 -alias client -validity 3650 -deststoretype pkcs12 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth

Export certificate of the client (on the client)  

    keytool -v -exportcert -file client.cer -alias client -keystore client-keystore.p12 -storepass secret -rfc

Create server truststore with certificate of the client (on the server)  

    keytool -v -importcert -file client.cer -alias client -keystore server-truststore.jks -storepass secret -noprompt


Run the server (first)  

    java -Djavax.net.ssl.keyStore=$WORK/server-keystore.p12 -Djavax.net.ssl.keyStorePassword=secret -Djavax.net.ssl.trustStore=$WORK/server-truststore.jks -Djavax.net.ssl.trustStorePassword=secret -jar server-0.0.1-SNAPSHOT.jar $SERVER_PORT true


Run the client (after the server)  

    java -Djavax.net.ssl.keyStore=$WORK/client-keystore.p12 -Djavax.net.ssl.keyStorePassword=secret -Djavax.net.ssl.trustStore=$WORK/client-truststore.jks -Djavax.net.ssl.trustStorePassword=secret -jar client-0.0.1-SNAPSHOT.jar $SERVER_IP $SERVER_PORT


# Java 11 two-way certificates verification based on trusting the Certificate Authority (TLS 1.3)

**Pros**
  - Clients do not need to add the certificate of the server
  - Server does not need to add all the certificates of the clients
  - Maintenance will be less because only the Certificate Authority's certificate validity can expire
  
**Cons**
  - You don't have control anymore for which applications are allowed to call your application. You give permission to any application who has a signed certificate by the Certificate Authority.


In addition to what was done in the previous paragraphs.  


Creating a Certificate Authority (on the server)  

    keytool -v -genkeypair -dname "CN=Root-CA,OU=Certificate Authority,O=MyLab,C=IT" -keystore root-ca/root-ca-identity.p12 -storepass secret -keypass secret -keyalg RSA -keysize 2048 -alias root-ca -validity 3650 -deststoretype pkcs12 -ext KeyUsage=digitalSignature,keyCertSign -ext BasicConstraints=ca:true,PathLen:3


Creating a Certificate Signing Request  

**Certificate Signing Request for the server**

    keytool -v -certreq -file server.csr -keystore server-keystore.p12 -alias server -keypass secret -storepass secret -keyalg rsa

**Certificate Signing Request for the client**

    keytool -v -certreq -file client.csr -keystore client-keystore.p12 -alias client -keypass secret -storepass secret -keyalg rsa


Signing the certificate with the Certificate Signing Request (on the server)  

**Signing the client certificate and copy it on the client**

    keytool -v -gencert -infile client.csr -outfile client-signed.cer -keystore root-ca/root-ca-identity.p12 -storepass secret -alias root-ca -validity 3650 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -rfc

**Signing the server certificate**

    keytool -v -gencert -infile server.csr -outfile server-signed.cer -keystore root-ca/root-ca-identity.p12 -storepass secret -alias root-ca -validity 3650 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:$SERVER_IP -rfc


Export CA certificate on the server and copy it on the client  

    keytool -v -exportcert -file root-ca/root-ca.pem -alias root-ca -keystore root-ca/root-ca-identity.p12 -storepass secret -rfc


Replace unsigned certificate with a signed one into the keystore of the client and server  

The keytool has a strange limitation/design.  
It won't allow you to directly import the signed certificate, and it will give you an error if you try it.  
The certificate of the Certificate Authority must be present within the identity.jks.  

**Client**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore client-keystore.p12 -storepass secret -noprompt
    keytool -v -importcert -file client-signed.cer -alias client -keystore client-keystore.p12 -storepass secret
    keytool -v -delete -alias root-ca -keystore client-keystore.p12 -storepass secret


**Server**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore server-keystore.p12 -storepass secret -noprompt
    keytool -v -importcert -file server-signed.cer -alias server -keystore server-keystore.p12 -storepass secret
    keytool -v -delete -alias root-ca -keystore server-keystore.p12 -storepass secret


Trusting the Certificate Authority only importing the certificate of the Certificate Authority into the truststores of the client and server  

**Client**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore client-truststore.jks -storepass secret -noprompt

**Server**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore server-truststore.jks -storepass secret -noprompt


Remove the client and server specific certificates still present in the truststores  

**Client**

    keytool -v -delete -alias server -keystore client-truststore.jks -storepass secret


**Server**

    keytool -v -delete -alias client -keystore server-truststore.jks -storepass secret


Run the server (first)  

    java -Djavax.net.ssl.keyStore=$WORK/server-keystore.p12 -Djavax.net.ssl.keyStorePassword=secret -Djavax.net.ssl.trustStore=$WORK/server-truststore.jks -Djavax.net.ssl.trustStorePassword=secret -jar server-0.0.1-SNAPSHOT.jar $SERVER_PORT true


Run the client (after the server)  

    java -Djavax.net.ssl.keyStore=$WORK/client-keystore.p12 -Djavax.net.ssl.keyStorePassword=secret -Djavax.net.ssl.trustStore=$WORK/client-truststore.jks -Djavax.net.ssl.trustStorePassword=secret -jar client-0.0.1-SNAPSHOT.jar $SERVER_IP $SERVER_PORT


# Java 8


## Environment set up for Java 8

Build the client source on Windows and transfer the executable jar on the client Linux server as described in client/README.md.  

Build the server source on Windows and transfer the executable jar on the serve Linux server server/README.md.  


# Java 8 two-way certificates verification based on trusting the Certificate Authority

Creating a server keystore with a public and private key (on the server)  

    keytool -v -genkeypair -dname "CN=Server,OU=Dev,O=MyLab,C=IT" -keystore server-keystore.jks -storepass secret -keypass secret -keyalg RSA -keysize 2048 -alias server -validity 3650 -deststoretype pkcs12 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:$SERVER_IP

<!---
Exporting certificate of the server (on the server)  
keytool -v -exportcert -file server.cer -alias server -keystore server-keystore.jks -storepass secret -rfc
Creating the truststore for the client and import the certificate of the server (on the client)  
keytool -v -importcert -file server.cer -alias server -keystore client-truststore.jks -storepass secret -noprompt
-->

Creating client certificate (on the client)  

    keytool -v -genkeypair -dname "CN=Client,OU=Dev,O=MyLab,C=IT" -keystore client-keystore.jks -storepass secret -keypass secret -keyalg RSA -keysize 2048 -alias client -validity 3650 -deststoretype pkcs12 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth

<!---
Export certificate of the client (on the client)  
keytool -v -exportcert -file client.cer -alias client -keystore client-keystore.jks -storepass secret -rfc
Create server truststore with certificate of the client (on the server)  
keytool -v -importcert -file client.cer -alias client -keystore server-truststore.jks -storepass secret -noprompt
-->

Creating a Certificate Authority (on the server)  

    keytool -v -genkeypair -dname "CN=Root-CA,OU=Certificate Authority,O=MyLab,C=IT" -keystore root-ca/root-ca-identity.jks -storepass secret -keypass secret -keyalg RSA -keysize 2048 -alias root-ca -validity 3650 -deststoretype pkcs12 -ext KeyUsage=digitalSignature,keyCertSign -ext BasicConstraints=ca:true,PathLen:3


Creating a Certificate Signing Request  

**Certificate Signing Request for the server**

    keytool -v -certreq -file server.csr -keystore server-keystore.jks -alias server -keypass secret -storepass secret -keyalg rsa


**Certificate Signing Request for the client**

    keytool -v -certreq -file client.csr -keystore client-keystore.jks -alias client -keypass secret -storepass secret -keyalg rsa


Signing the certificate with the Certificate Signing Request (on the server)  

**Signing the client certificate and copy it on the client**

    keytool -v -gencert -infile client.csr -outfile client-signed.cer -keystore root-ca/root-ca-identity.jks -storepass secret -alias root-ca -validity 3650 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -rfc

**Signing the server certificate**

    keytool -v -gencert -infile server.csr -outfile server-signed.cer -keystore root-ca/root-ca-identity.jks -storepass secret -alias root-ca -validity 3650 -ext KeyUsage=digitalSignature,dataEncipherment,keyEncipherment,keyAgreement -ext ExtendedKeyUsage=serverAuth,clientAuth -ext SubjectAlternativeName:c=DNS:localhost,IP:$SERVER_IP -rfc


Export CA certificate on the server and copy it on the client  

    keytool -v -exportcert -file root-ca/root-ca.pem -alias root-ca -keystore root-ca/root-ca-identity.jks -storepass secret -rfc


Replace unsigned certificate with a signed one into the keystore of the client and server  

The keytool has a strange limitation/design.  
It won't allow you to directly import the signed certificate, and it will give you an error if you try it.  
The certificate of the Certificate Authority must be present within the identity.jks.  

**Client**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore client-keystore.jks -storepass secret -noprompt
    keytool -v -importcert -file client-signed.cer -alias client -keystore client-keystore.jks -storepass secret
    keytool -v -delete -alias root-ca -keystore client-keystore.jks -storepass secret

**Server**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore server-keystore.jks -storepass secret -noprompt
    keytool -v -importcert -file server-signed.cer -alias server -keystore server-keystore.jks -storepass secret
    keytool -v -delete -alias root-ca -keystore server-keystore.jks -storepass secret

Trusting the Certificate Authority only importing the certificate of the Certificate Authority into the truststores of the client and server  

**Client**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore client-truststore.jks -storepass secret -noprompt

**Server**

    keytool -v -importcert -file root-ca/root-ca.pem -alias root-ca -keystore server-truststore.jks -storepass secret -noprompt


<!--
Remove the client and server specific certificates still present in the truststores  
**Client**
keytool -v -delete -alias server -keystore client-truststore.jks -storepass secret
**Server**
keytool -v -delete -alias client -keystore server-truststore.jks -storepass secret
-->


Run the server (first)  

    java -Djavax.net.ssl.keyStore=$WORK/server-keystore.jks -Djavax.net.ssl.keyStorePassword=secret -Djavax.net.ssl.trustStore=$WORK/server-truststore.jks -Djavax.net.ssl.trustStorePassword=secret -jar server-0.0.1-SNAPSHOT.jar $SERVER_PORT true


Run the client (after the server)  

    java -Djavax.net.ssl.keyStore=$WORK/client-keystore.jks -Djavax.net.ssl.keyStorePassword=secret -Djavax.net.ssl.trustStore=$WORK/client-truststore.jks -Djavax.net.ssl.trustStorePassword=secret -jar client-0.0.1-SNAPSHOT.jar $SERVER_IP $SERVER_PORT

