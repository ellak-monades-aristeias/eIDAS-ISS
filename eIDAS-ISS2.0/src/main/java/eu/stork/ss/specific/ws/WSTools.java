package eu.stork.ss.specific.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stork.ss.Constants;
import eu.stork.ss.SPUtil;

public class WSTools
{
	//Logger
	static final Logger logger = LoggerFactory.getLogger(RetrieveDummySP.class.getName());
	
	public static SOAPMessage execWSReq(String xml, String method)
	{
		SOAPMessage response = null;
		try
		{
		      InputStream is = new ByteArrayInputStream(xml.getBytes("utf-8"));
		      SOAPMessage message = MessageFactory.newInstance(/*SOAPConstants.SOAP_1_2_PROTOCOL*/).createMessage(new MimeHeaders(), is);
		      message.saveChanges();
		      //message.getSOAPPart().getEnvelope().addNamespaceDeclaration("soap", "http://schemas.microsoft.com/sharepoint/soap/");
		      String authorization = new sun.misc.BASE64Encoder().encode(("corp\\user2:qwe123!").getBytes());
		      message.getMimeHeaders().addHeader("Authorization", "Basic " + authorization);
		      message.getMimeHeaders().addHeader("SOAPAction", "http://schemas.microsoft.com/sharepoint/soap/"+method);
		      message.saveChanges();
		      ByteArrayOutputStream out = new ByteArrayOutputStream();
		      message.writeTo(out);
		      String strMsg = new String(out.toByteArray());
		      String sss = message.getSOAPPart().getMimeHeader("Content-Type")[0];
		      
		      /*
		      SOAPMessage message = mf.createMessage();

		      SOAPHeader shead = message.getSOAPHeader();
		      SOAPBody body = message.getSOAPBody();
		      QName bodyName = new QName("http://cosign.ddns.net/_vti_bin/Lists.asmx", "GetListItems");
		      SOAPBodyElement bodyElement = body.addBodyElement(bodyName);
		      
		      
		      //SOAPElement symbol = bodyElement.addChildElement("MyMetal");
		      //symbol.addTextNode("iron");
		      
		      shead.detachNode();
		      body.addDocument(initSOAPBody());
		      message.saveChanges();
		      */
		      
		      
		      String endpoint = "http://cosign.ddns.net/OBA/_vti_bin/Lists.asmx";
		      SOAPConnection connection = SOAPConnectionFactory.newInstance().createConnection();
		      response = connection.call(message, endpoint);
		      connection.close();
		      } catch(Exception e) {
		    	  //All other Exceptions
		    	  logger.error("Exception: [" + e + "]");
		      }
		      return response;
	}
}