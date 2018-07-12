package eu.stork.ss.specific.ws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.stork.peps.auth.commons.AttributeUtil;
import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSValues;
import eu.stork.peps.auth.commons.PersonalAttribute;
import eu.stork.peps.auth.commons.PersonalAttributeList;
import eu.stork.ss.Constants;
import eu.stork.ss.SPUtil;
import eu.stork.ss.SavePersonalAttributeList;

public class SaveDummySP //extends SavePersonalAttributeList 
{
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = 1588295800440516440L;

	//Logger
	static final Logger logger = LoggerFactory.getLogger(SaveDummySP.class.getName());

	private static String concatListVals(List<String> l)
	{
		String r = "";
		for (String s: l)
			r+=s;
		return r.trim();
	}
	
	private static String createXMLReply(String token, IPersonalAttributeList pal)
	{
		String xml = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soap=\"http://schemas.microsoft.com/sharepoint/soap/\">"
				+ "<soapenv:Header/><soapenv:Body><soap:UpdateListItems><soap:listName>PersonalDetails</soap:listName>"
				+ "<soap:updates><Batch ListVersion='1' OnError='Continue'>"
				+ "<Method ID='1' Cmd='New'>"
			//	+ "<Field Name=\"ID\">val</Field>"
				+ "<Field Name='Token'>"+token+"</Field>";
		
		for( PersonalAttribute pa: pal ) 
		{
			if (!pa.getName().contains("canonicalResidenceAddress"))
			{
				String value = concatListVals(pa.getValue());
				if (pa.getName().contains("dateOfBirth"))
					value = value.substring(0, 4)+"-"+value.substring(4, 6)+"-"+value.substring(6, 8);
				xml += "<Field Name='"+pa.getName()+"'>"+value+"</Field>";
			}
			else
			{
				Map<String, String> map = pa.getComplexValue();
				Set<String> keys = map.keySet();
				for (String key: keys)
				{
					String value = map.get(key);
					xml += "<Field Name='"+key+"'>"+value+"</Field>";
				}
				
				/*
				String []t = a.split("<element name=\"");
				for (int i = 1; i < t.length; i++)
				{
					xml += "<Field Name='"+t[i].substring(0, t[i].indexOf("\""))+"'>"+concatListVals(pa.getValue())+"</Field>";
				}
				*/
			}
		}
			
		
		xml += "</Method></Batch></soap:updates></soap:UpdateListItems></soapenv:Body></soapenv:Envelope>";
		return xml;
	}
	
	//@Override
	//protected String savePersonalAttributeList(String token, IPersonalAttributeList pal) {
	protected static String savePersonalAttributeList(String token, IPersonalAttributeList pal, String sp) {//ptk
		if (token.contains("?")) token = token.substring(0, token.indexOf("?"));
		String returnUrl = "http://localhost/stork2-login.php?t=";
		String failUrl = "";
		try {
			Properties configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
			//returnUrl = configs.getProperty(Constants.SR_URL);
			//failUrl = configs.getProperty(Constants.SF_URL);
			returnUrl=configs.getProperty(sp+Constants.SR_URL);//ptk
			failUrl=configs.getProperty(sp+Constants.SF_URL);//ptk
		} catch (IOException e) {
		}
		
		pal = mergePalAttribs(pal);
		
		String xml = createXMLReply(token, pal);
		SOAPMessage response = WSTools.execWSReq(xml, "UpdateListItems");
		if (response == null) return failUrl;
		String respString = "ErrorText";
		try
		{
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
		    response.writeTo(stream);
		    respString = new String(stream.toByteArray(), "utf-8");
		} catch(Exception e) {
			//All other Exceptions
			logger.error("Exception: [" + e + "]");
		}      
	    if (respString.contains("ErrorText")) return failUrl;	
	    String returnID = respString.substring(respString.indexOf("ows_ID="));
	    returnID = returnID.substring(returnID.indexOf("\"")+1);
	    returnID = returnID.substring(0, returnID.indexOf("\""));
	    return returnUrl+returnID;
	}
	
	private static IPersonalAttributeList mergePalAttribs(IPersonalAttributeList pal)
	{
		PersonalAttributeList nPal = new PersonalAttributeList();
		HashMap<String, PersonalAttribute> mm = new HashMap<String, PersonalAttribute>();
		
		for (PersonalAttribute pa : pal)
		{
			String name = pa.getName();
			if (name.indexOf("_") != -1) name = name.substring(0, name.indexOf("_"));
			if (!mm.containsKey(name))
			{
				pa.setName(name);
				nPal.add(pa);
				continue;
			}
			PersonalAttribute o = mm.get(name);
			if (o.isEmptyComplexValue())
			{
				List<String> val = o.getValue();
				val.addAll(pa.getValue());
				o.setValue(val);
			}
			else
			{
				List<Map<String, String>> val = o.getComplexValues();
				val.addAll(pa.getComplexValues());
			}
		}
		return nPal;
	}

	/**
	 * Wrap complex value in a String
	 * 
	 * @param map The complex value Map
	 * 
	 * @return The wrapped complex value
	 */
	private static String wrapComplexValue(Map<String, String> map) {
		return AttributeUtil.mapToString(map,
				PEPSValues.ATTRIBUTE_VALUE_SEP.toString());
	}

	/**
	 * Wrap value in a String
	 * 
	 * @param value The value List
	 * 
	 * @return The wrapped value
	 */
	private static String wrapValue(List<String> value) {
		return AttributeUtil.listToString(value,
		        PEPSValues.ATTRIBUTE_VALUE_SEP.toString());
	}
}