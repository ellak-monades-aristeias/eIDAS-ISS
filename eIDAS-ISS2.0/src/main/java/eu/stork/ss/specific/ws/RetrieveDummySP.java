package eu.stork.ss.specific.ws;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSValues;
import eu.stork.peps.auth.commons.PersonalAttribute;
import eu.stork.peps.auth.commons.PersonalAttributeList;
import eu.stork.ss.Constants;
import eu.stork.ss.RetrievePersonalAttributeList;
import eu.stork.ss.SPUtil;
import eu.stork.ss.specific.json.Attribute;


public class RetrieveDummySP //extends RetrievePersonalAttributeList 
{
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = -5984353371825874179L;

	//Logger
	static final Logger logger = LoggerFactory.getLogger(RetrieveDummySP.class.getName());
	static String global_sp;
	//@Override
	//protected IPersonalAttributeList retrievePersonalAttributeList(String token) {
	public static IPersonalAttributeList retrievePersonalAttributeList(String token,String sp) {//ptk
		global_sp=sp;//ptk
if (token.contains("?")) token = token.substring(0, token.indexOf("?"));
		IPersonalAttributeList pal;

		logger.debug("Trying to retrieve the PAL with WS.");
		
		pal = null;
	
		try
		{
		      SOAPMessage response = WSTools.execWSReq(initSOAPBody(), "GetListItems");
		      if (response == null) return null;
		      ByteArrayOutputStream stream = new ByteArrayOutputStream();
		      response.writeTo(stream);
		      String respString = new String(stream.toByteArray(), "utf-8");
		      
		      if (respString.contains("Fault")) return null;
		      
		      respString = respString.substring(respString.indexOf("ows_ID"));
		      respString = respString.substring(0, respString.indexOf("</rs:data>"));

		      HashMap<String, Attribute> list = new HashMap<String, Attribute>();
		      String []rvals = respString.split("<z:row ");
		      for (int i = 0; i < rvals.length; i++)
		      {
		    	  String t[] = rvals[i].split(" ");
		    	  String key = t[1].substring(t[1].indexOf("\'")+1, t[1].length()-1);
		    	  String isRequired = t[2].substring(t[2].indexOf("\'")+1, t[2].length()-1);
		    	  String isComplex = t[3].substring(t[3].indexOf("\'")+1, t[3].length()-1);
		    	  String value = null;
		    	  Attribute att = new Attribute();
		    	  att.setComplex(isComplex);
		    	  att.setRequired(isRequired);
		    	  att.setValue(value);
		    	  list.put(key, att);
		      }
		      
		      PersonalAttribute pa;
		      pal = new PersonalAttributeList();
		      
		      pa = new PersonalAttribute();
			  pa.setName("eIdentifier");
			  pa.setIsRequired(true);
			  pal.add(pa);
			  
		      for (Map.Entry<String, Attribute> entry : list.entrySet()) 
		      {
		    	  String key = entry.getKey();
		    	  Attribute value = entry.getValue();

		    	  pa = new PersonalAttribute();
		    	  //Set name
		    	  //if (key.startsWith("isStudent")) continue;
		    	  pa.setName(key);
		    	  //Set is required
		    	  if ( value.getRequired().equals("1") )
		    		  pa.setIsRequired(true);
		    	  else
		    		  pa.setIsRequired(false);

		    	  if ( value.getValue()!=null ) 
		    	  {
		    		  final String[] vals =
			                value.getValue().split(PEPSValues.ATTRIBUTE_VALUE_SEP.toString());

		    		  //Set complex or plain values
		    		  if ( value.getComplex().equals("1") )
		    			  pa.setComplexValue(createComplexValue(vals));
		    		  else
		    			  pa.setValue(createValues(vals));
		    	  }
		    	  pal.add(pa);
			}
			
			logger.debug("The constructed PAL: " + pal.toString());
				
				
			} catch(Exception e) {
				//All other Exceptions
				logger.error("Exception: [" + e + "]");
			}

		return pal;
	}
	
	private static String execSOAPRequest(SOAPMessage message)
	{
		//String server = "http://cosign.ddns.net/_vti_bin/Lists.asmx";
		Properties configs=null;
		try {
			configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//ptk
		String server=configs.getProperty(global_sp+Constants.DS_URL);//ptk
		//String server = "http://cosign.ddns.net/_vti_bin/Lists.asmx";
        String response = "";
 
        try 
        {  
        	URL u = new URL(server);
            URLConnection uc = u.openConnection();
            HttpURLConnection urlconnection = (HttpURLConnection) uc;
            urlconnection.setDoOutput(true);
            urlconnection.setDoInput(true);
            urlconnection.setRequestMethod("POST");
            //urlconnection.setRequestProperty("SOAPAction", "http://cosign.ddns.net/_vti_bin/Lists.asmx");
            urlconnection.setRequestProperty("SOAPAction", server);//ptk
                
            OutputStream out = urlconnection.getOutputStream();
 
            OutputStreamWriter wout = new OutputStreamWriter(out);
 
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
		    message.writeTo(stream);
		    String soapmessage = new String(stream.toByteArray(), "utf-8");
            wout.write(soapmessage); 
            wout.flush();

            InputStream is = urlconnection.getInputStream();
            response = readServerResponseMessage(is);
            is.close();
            wout.close();
        }
        catch (IOException e) 
        {
            System.err.println(e); 
        }
        catch(Exception eb)
        {
            System.err.println(eb); 
        }  
        return response;
	}

	private static String readServerResponseMessage(InputStream is)
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is)); 
		char[] readChars = new char[65000];
		String response = "";
		try
		{
			while (br.read(readChars) != -1)
			{       
				response = new String(readChars);
				break;
			}
	 
		}
		catch (IOException e)
		{   
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	 
		return response;
	}      
	
	private static String initSOAPBody()
	{
		
		String req = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:soap=\"http://schemas.microsoft.com/sharepoint/soap/\">"
				+ "<soapenv:Header/>"
				+ "<soapenv:Body>"
				+ "<soap:GetListItems>"
				+ "<!--Optional:-->"
				+ "<soap:listName>Stork2Attributes</soap:listName>"
				+ "<!--Optional:-->"
				+ "<soap:viewName>{CF59C1C5-E73E-45AC-BC15-DD1712E93EFD}</soap:viewName>"
				+ "<!--Optional:-->"
				+ "<soap:query>"
				+ "<Query>"
				+ "</Query>"
				+ "</soap:query>"
				+ "<!--Optional:-->"
				+ "<soap:viewFields>"
				+ "<ViewFields>"
				+ "<FieldRef Name='ID'/>"
				+ "<FieldRef Name='Title'/>" 
				+ "<FieldRef Name='Mandatory'/>"
				+ "<FieldRef Name='Complex'/>"
				+ "</ViewFields>"
				+ "</soap:viewFields>"
				+ "<!--Optional:-->"
				+ "<soap:rowLimit>100</soap:rowLimit>"
				+ "<!--Optional:-->"
				+ "<soap:queryOptions>"
				+ "<QueryOptions>"
				+ "<ViewAttributes Scope=\"RecursiveAll\" />"
				+ "</QueryOptions>"
				+ "</soap:queryOptions>"
				+ "<!--Optional:-->"
				+ "</soap:GetListItems>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";
		return req;
	}
	
	/**
	 * Validates and creates the attribute values.
	 * 
	 * @param vals The attribute values.
	 * 
	 * @return The {@link List} with the attribute values.
	 * 
	 * @see List
	 */
	private static List<String> createValues(final String[] vals) {
		final List<String> values = new ArrayList<String>();
		for (final String val : vals) {
			if (StringUtils.isNotEmpty(val)) {
				values.add(val);
			}
		}
		return values;
	}

	/**
	 * Validates and creates the attribute's complex values.
	 * 
	 * @param values The complex values.
	 * 
	 * @return The {@link Map} with the complex values.
	 * 
	 * @see Map
	 */
	private static HashMap<String, String> createComplexValue(final String[] values) {
		final HashMap<String, String> complexValue = new HashMap<String, String>();
		for (final String val : values) {
			final String[] tVal = val.split("=");
			if (StringUtils.isNotEmpty(val) && tVal.length == 2) {
				complexValue.put(tVal[0], tVal[1]);
			}
		}
		return complexValue;
	}
}