# eIDAS Interconnection Supporting Service Plus
Web-App tool for facilitating SP - eIDAS infrastructure interconnection

## Introduction
The Interconnection Supporting Service Plus allows for several Service Providers (SPs) to connect and use the eIDAS infrastructure without having to implement SAML mechanisms in their infrastructure, or when a Java-capable web browser is not available.
Current ISS+ functionality:
1.	Allows SPs to use the eIDAS infrastructure using non-encrypted (but over https) Json messages and Web Services instead of SAML.
2.	Only one ISS+ instance can support multiple SPs using different communication formats
3.	Simple public web access to the log file of all requests served by the ISS+

It was developed by the "Information Management Lab (i4M Lab)", participant of the Atlantis Group (http://www.atlantis-group.gr/) of the University of the Aegean (https://www.aegean.gr/). 

## Project Purpose

The purpose of this project is to facilitate the development of SPs, by 
1) simplifing the process of the SP interacting with the Greek eIDAS Node. 
2) Enable non-Java capable SPs to connect with the eIDAS infrastructure

## Repository contents
  - eIDAS-ISS1.0/src folder: contains the source code of the ISS 1.0
  - eIDAS-ISS1.0/pom.xml: the maven pom.xml file of the project ISS 1.0
  - eIDAS-ISS1.0/maven-libs folder: Required local libraries for ISS 1.0
  - eIDAS-ISS2.0/src folder: contains the source code of the ISS 2.0
  - eIDAS-ISS2.0/pom.xml: the maven pom.xml file of the project ISS 1.0
  - eIDAS-ISS2.0/maven-libs folder: Required local libraries for ISS 2.0
  - README.md: this file

## Setting up

### Setting up Java
Perform the following steps: 
If Oracle provided JVM is going to be used, then it is necessary to apply the JCE  Unlimited Strength Jurisdiction Policy Files, which contain no restriction on cryptographic strengths: 
a.  Download the Java Cryptography Extension (JCE) Unlimited Strength Policy  Files from Oracle: 
  - For Java 7: http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html
  - For Java 8: http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
b.  Uncompress and extract the downloaded zip file (it contains README.txt and two jar files). 
c.  For the installation, please follow the instructions in the README.txt file. 

### Setting up Tomcat
Some already provided jars need to be added to the libraries of the Tomcat web-server. These jars may be found under AdditionalFiles directory in the binary for your application server, in the eIDAS release bundle. 
If you are using Tomcat 7: 
1.  Create a folder named shared in $TOMCAT_HOME
2.  Create a subfolder named  lib in $TOMCAT_HOME/shared.
3.  Edit the file $TOMCAT_HOME/conf/catalina.properties and change the property shared.loader so that it reads: 
shared.loader=${catalina.home}/shared/lib/*.jar 
4.  Copy the files below to the new shared/lib directory: 
xml-apis-1.4.01.jar 
resolver-2.9.1.jar 
serializer-2.7.2.jar 
xalan-2.7.2.jar 
endorsed/xercesImpl-2.11.0.jar 

If you are using Tomcat 8:  
1.  Copy the files below to the existing lib directory on the application server. 
xml-apis-1.4.01.jar 
resolver-2.9.1.jar 
serializer-2.7.2.jar (rename this file to serializer.jar) 
xalan-2.7.2.jar 
xercesImpl-2.11.0.jar


## Connecting using the eIDAS Interconnection Supporting Service Plus (ISS+)

Connecting to the Greek eIDAS Infrastructure consists of two main steps. The first one is setting up and deploying the ISS+ app on a separate server. The second step is setting up the SP (which can reside on another non-Java capable web server) to interact with ISS+.
The ISS+ package contains 1 file to facilitate the integration of a new SP to the Greek eIDAS Infrastructure:

1. ISSPlus.war:	The Java Web application package containing the ISS+ executables as well as the configuration files.

### Deploying the ISS+ app 	
 
After deployment is complete, the following environmental variable needs to be set in the tomcat execution environment (either as OS/AS environment variable or command-line parameter): SP_CONFIG_REPOSITORY. The variable must point to the location of the file system where the ISS+ was deployed, followed by the subdirectories WEB-INF/classes

### Setting up the keystore 

The aforementioned directory contains the file eidasKeystore.jks, which must contain all the necessary certificates for secure and trusted communication with the eIDAS Node. The following steps need to be executed in order to prepare the keystore for operation.
1.	Change the keystore password (current password: “local-demo”)
2.	Obtain a certificate which identifies the SP (ie: the ISS+). The certificate must satisfy the criteria described in the eIDAS - Cryptographic requirements for the Interoperability Framework document , regarding SAML signing certificates.
3.	Import the certificate in the keystore as a PrivateKeyEntry
4.	Provide the Greek eIDAS Node team with the public certificate of the SP, to be added to the Greek eIDAS Node list of trusted SPs.

### Configuring the ISS+ app

In addition, the following information needs to be modified in the following configuration files:

SignModule_SP.xml

        <entry key="response.sign.assertions">true</entry>
        <entry key="keyStorePath">eidasKeystore.jks</entry>
        <entry key="keyStorePassword">keystore_password</entry>
        <entry key="keyPassword">SP_certificate_password</entry>
        <entry key="issuer">SP_certificate_issuer</entry>
        <entry key="serialNumber">SP_certificate_serial_number</entry>
        <entry key="keyStoreType">JKS</entry>
  
        <entry key="metadata.keyStorePath"> eidasKeystore.jks</entry>
        <entry key="metadata.keyStorePassword">keystore_password</entry>
        <entry key="metadata.keyPassword">SP_certificate_password</entry>
        <entry key="metadata.issuer">SP_certificate_issuer</entry>
        <entry key="metadata.serialNumber">SP_certificate_serial_number</entry>
        <entry key="metadata.keyStoreType">JKS</entry>


EncryptModule_SP.xml

    <!-- Key Encryption algorithm -->
    <entry key="key.encryption.algorithm">http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p</entry>
    <entry key="keyStorePath"> eidasKeystore.jks</entry>
    <entry key="keyStorePassword">keystore_password</entry>
    <entry key="keyPassword">SP_certificate_password</entry>
  
    ...
  
    <!--  If not present then no decryption will be applied on response -->
    <!-- Certificate containing instance private key-->
    <entry key="responseDecryptionIssuer">SP_certificate_issuer</entry>
    <entry key="serialNumber">SP_certificate_serial_number</entry>
    <entry key="keyStoreType">JKS</entry>

sp.properties

    #Sp Country
    sp.country=Code of the SP’s Country (GR)

    #Sp return url
    sp.return= Return URL of the SP (i.e: https://ip:port/ISSPlus/Service Redirect)
    sp.metadata.url= Metadata URL of the SP (i.e: https://ip:port/ISSPlus/metadata)
    ….
    sp.qaalevel=# 
    The level of Assurance required by this SP for the provided authentication data.
    1=Non-existent
    2=Low
    3=Substantial
    4=High
    ….

    #The Service URLs (that we provide support)
    sp1.ds.url=SP Retrieve Requested Attributes Endpoint URL
    sp1.ss.url=SP Receive Requested Values Endpoint URL
    sp1.sr.url=SP Authentication success redirect URL
    sp1.sf.url=SP Authentication failure/error redirect URL
    sp1.mode=(json|ws) communication mode. 

### Interacting with ISS+

The ISS+ has one entry point, one exit point and requires two API calls on the SP side:
1. one URL in order to retrieve the list of Attributes to request from STORK,
2. and a second URL to store the values of the Attribute that were returned from STORK.

Both those URLs provide a Json formatted document. The format of the document is explained in Appendix I.
For the remaining section we will assume that the ISS+ is installed and running in the following
location: http://localhost/ISSPlus/ and the SP is installed and running in: http://localhost/da/. The first step for the SP is to create a unique random identifier. This identifier will be unique and different for each user request, much like a session ID. We call this unique identifier token and it will be used by the ISS+ and the SP in order to identify a specific user request. After the creation of the token the SP will redirect the user to the ISS+ entry point. For demonstration purposes we will assume that the unique token that was generated is: 1234567890, so the entry point of the ISS+ is:
https://localhost/da/ValidateToken?t=1234567890

The following table enumerates the parameters of the HTTP request which redirects the user to the ISS+:

Paramater Name	Parameter Purpose	Mandatory
t	Sp-generated random Token. Used to identify authentication requests	yes
sp	ID of the requesting SP as defined in the ISS+ sp.properties configuration file	yes
cc	Citizen Country Code. If not provided, the user will be requested to choose between the supported countries	no
saml	Generated Saml version definition. While communicating with the Greek eIDAS infrastructure, the value of this parameter should always be “eIDAS”.	yes
qaa	The level of Assurance required for this request for the provided authentication data (1=Non-existent,2=Low,3=Substantial,4=High)
If not provided, the qaa defined in sp.properties will be used.	No

(Entry point example: https://localhost/da/ValidateToken?t=0f985470-3adb-4b33-8bb6-d6f515ab8bce&sp=sp1&cc=GR&saml=eIDAS)
Both aforementioned SP endpoints (retrieving the requested attribute list and pushing the authentication results) also require the token parameter in order to identify the specific authentication request. In addition, the ISS+ HTTP request to the SP’s second endpoint (pushing authentication results) also contains a second parameter “r”, which contains the authentication results, as described in Appendix II.
This SP endpoint must either return a Json “OK” message, which means the user is authenticated in the SP, or “NOK” in case the user is not accepted. 
The exit point is the URL to redirect the user once the ISS+ has finished all the work. This URL is part of the SP and is used to map the SP specific logic (role assignment) in terms of attributes and attribute values that are returned from the ISS+. This URL is outside the scope of this document but for demonstration purposes we will assume that it is the following:
https://localhost/da/eidas-login.php?t=<random token>

Please note that the random token is the same token that was used in the entry point. This way the SP knows how to identify the user that created the original request.

## Appendix I: ISS+ Json API

### Attribute List Retrieval API

The most important part when interfacing with the ISS+ are the two API calls that are used by the ISS+ and the SP in order to exchange information securely (out-of-band communication). As we mentioned earlier the ISS+ has an open design and can be extended to support custom SP needs. The current version of the ISS+ supports JSON formatted communication.
The first step for the ISS+ is to retrieve the list of Attributes. So the ISS+ will communicate with a URL provided by the SP presenting the token. If everything is correct (i.e. valid token) the data returned by the URL must be in the following format:

    {
      "status":"OK",
      "list":{
        "attribute name ":{
          "value":attribute value or null,
          "complex":0 or 1 depending on the value above,
          "required":0 if optional or 1 if mandatory
        },
       ...
      }
    }

Each field is described in the table below. Please note that the list may contain one or more attributes and in some cases the value field may contain information, but in most cases it will be null.
Field Name Details:
status: Informs us if the outcome of the request was successful (OK literal) or not (NOT literal) list: Contains one or more Attributes attribute name The attribute name is the name that identifies this attribute.
value: Contains the attribute value that is a string or null. The attribute value may be normal or complex. In the normal case it contains just one or a list of values separated by comma. So we may have: <value> or we may have: <value #1>,<value #2>...In the complex case we have a single or multiple key-value pairs. So we may have: <key>=<value> or we may have: <key# 1>=<value#1>,<key #2>=<value #2>... The key-value pairs are separated with the equals sign (=).
complex: In order to identify the contents of the value above we set this variable to 1 if the value is complex and to 0 in any other case.
required: Identifies if the attribute is required in order to provide access to the SP service or not (0 if it is optional and 1 if mandatory).
For clarity we also provide a sample output that the DA Attribute List Retrieval API must return:

    {
      "status":"OK",
        "list":{ 
          "http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier":{
          "value":null,
          "complex":"0",
          "required":"1"
        },
    ...
      }
    }

### Attribute Values Storage API

Once the ISS+ has received the populated Attribute List it must deliver these values to the SP. This is performed via JSON again. The only difference is that the ISS+ is not retrieving data from the SP, but sending data. Using the HTTP POST method the SS send a JSON object with the following format:

    {
      "attribute name ":{
        "value":attribute value or null,
        "complex":0 or 1 depending on the value above,
        "required":0 if optional or 1 if mandatory
      },
    ...
    }

As we can see this object is identical with the list object we described in the previous section. The SP will respond with a JSON message stating if the save procedure was completed successful or not:

    {
      "status":"OK"
    }

If the response is OK, then the ISS can redirect the user back to the SP using the appropriate (success) endpoint. If it is "NOK" the user will be redirected to the failure endpoint.