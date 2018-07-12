package eu.stork.ss.specific.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import eu.stork.ss.Monitoring;
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

//	@Override
	//protected String savePersonalAttributeList(String token, IPersonalAttributeList pal) {
	public static String savePersonalAttributeList(String token, IPersonalAttributeList pal, String sp) {//ptk
		boolean outcome = false;
		HashMap<String, Attribute> list;
		String serviceUrl = "http://localhost/stork2-attributes.php?t=";
		String returnUrl = "http://localhost/stork2-login.php?t=";
		String failUrl = "";
		logger.debug("Trying to save the PAL with JSON.");
		try {
			//Load the configuration (URL)
			try {
				Properties configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
				serviceUrl = configs.getProperty(sp+"."+Constants.SS_URL);//ptk
				returnUrl=configs.getProperty(sp+"."+Constants.SR_URL);//ptk
				failUrl=configs.getProperty(sp+"."+Constants.SF_URL);//ptk
				//serviceUrl = configs.getProperty(Constants.SS_URL);
				//returnUrl = configs.getProperty(Constants.SR_URL);
				//failUrl = configs.getProperty(Constants.SF_URL);
			} catch (IOException e) {
				Monitoring.monitoringLog( "<span class='error'>Step 5: Error!</span>");
			}

			logger.debug("The ServiceURL: [" + serviceUrl + "]");
			logger.debug("The ReturnURL: [" + returnUrl + "]");
			logger.debug("The PAL from STORK: [" + pal.toString() + "]");
			Monitoring.monitoringLog( "Pal from stork "+pal.toString());
			pal = mergePalAttribs(pal);	
			list = new HashMap<String, Attribute>();
			for( PersonalAttribute pa: pal ) {
				final Attribute att = new Attribute();
	
				boolean eClassFix = true;
				//Fix for eClass, transform complex attribute to plain for isStudent
				if ( eClassFix )
				{
					if ( !pa.isEmptyComplexValue() ) 
					{					
						if (pa.getName().equals("isStudent") || pa.getName().equals("isTeacherOf"))
						{
							Map<String, String> map = pa.getComplexValue();
							String xml;
		
							xml = "";
							for (Map.Entry<String, String> entry : map.entrySet())	
							{
								String key, value;
								
								key = entry.getKey();
								value = entry.getValue();
								
								xml = xml + "<" + key + ">" + value + "</" + key + ">";
							}
							
							att.setComplex("0");
							att.setValue(xml);
						}
						else 
						{
							att.setComplex("1");
							att.setValue(wrapComplexValue(pa.getComplexValue()));
						}
					}
					else 
					{
						att.setComplex("0");
						if ( !pa.isEmptyValue() )
							att.setValue(wrapValue(pa.getValue()));
					}
				}
				else
				{
					if ( !pa.isEmptyComplexValue() ) 
					{	
						att.setComplex("1");
						att.setValue(wrapComplexValue(pa.getComplexValues()));
					}
					else 
					{
						att.setComplex("0");
						if ( !pa.isEmptyValue() )
							att.setValue(wrapValue(pa.getValue()));
					}
				}

				if ( pa.isRequired() )
					att.setRequired("1");
				else
					att.setRequired("0");
				list.put(pa.getName(), att);
			}

			CloseableHttpClient httpclient = HttpClients.createDefault();
			try {
				Gson gson = new Gson();
				List <NameValuePair> nvps = new ArrayList <NameValuePair>();
				String param, httpJson;
				HttpPost httpost;
				CloseableHttpResponse response;
				HttpEntity entity;
				SaveAttributes status;

				param = gson.toJson(list);

				logger.trace("The JSON to send: [" + param + "]");
				System.out.println("The JSON to send: [" + param + "]");
				System.out.println("Save UR: "+serviceUrl + token);

				httpost = new HttpPost(serviceUrl + token);
				nvps.add(new BasicNameValuePair("r", param));
				httpost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

				response = httpclient.execute(httpost);

				try {
					entity = response.getEntity();
				    httpJson = EntityUtils.toString(entity);
				    status = gson.fromJson(httpJson, SaveAttributes.class);
				    
					logger.trace("The JSON response: [" + status + "]");
					System.out.println("The JSON response: [" + status.getStatus() + "]");
					
				    if ( status.getStatus().equals("OK") )
				    	outcome = true;
				} finally {
				    response.close();
				}
			} finally {
			    httpclient.close();
			}
		} catch(Exception ex) {
			Monitoring.monitoringLog( "<span class='error'>Step 5: Error!</span>");
			System.out.println("IOException: [" + ex + "]");
			logger.error("IOException: [" + ex + "]");
		}

		if ( outcome ){
			Monitoring.monitoringLog( "<span class='success'>Step 5: Success!</span>");
			Monitoring.monitoringLog( "<span class='title'>Session End</span>");
			return returnUrl + token;
		}
		else
		{
			Monitoring.monitoringLog( "<span class='error'>Step 5: Error!</span>");
			return failUrl;
		}
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

	private static String wrapComplexValue(List<Map<String, String>> lmap) {
		StringBuffer s = new StringBuffer("");
		for (Map<String, String> map : lmap) 
			s.append(AttributeUtil.mapToString(map,	PEPSValues.ATTRIBUTE_VALUE_SEP.toString())+"\n");
		return s.toString().trim();
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
	
}