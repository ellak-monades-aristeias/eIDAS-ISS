package eu.stork.ss.specific.general;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eu.stork.peps.auth.commons.IPersonalAttributeList;
import eu.stork.peps.auth.commons.PEPSValues;
import eu.stork.peps.auth.commons.PersonalAttribute;
import eu.stork.peps.auth.commons.PersonalAttributeList;
import eu.stork.ss.Constants;
import eu.stork.ss.SPUtil;
import eu.stork.ss.RetrievePersonalAttributeList;
//import eu.stork.ss.specific.json.*;

public class RetrieveDummySP extends RetrievePersonalAttributeList  {

	private static final long serialVersionUID = -5984353371825874179L;

	//Logger
	static final Logger logger = LoggerFactory.getLogger(RetrieveDummySP.class.getName());
	
	@Override
	public IPersonalAttributeList retrievePersonalAttributeList(String token, String sp) {
		Properties configs=null;
		try {
			configs = SPUtil.loadConfigs(Constants.SP_PROPERTIES);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//ptk
		String comm_type= configs.getProperty(sp+".mode");//ptk
		Class<?> cls=null;
		
		try {
			cls = Class.forName("eu.stork.ss.specific."+comm_type+".RetrieveDummySP");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class<?> args[] = new Class[2];
		args[0] = String.class;
		args[1] = String.class;
		Object params[] =new Object[2];
		params[0]=token;
		params[1]=sp;
		//eu.stork.ss.specific.json.RetrieveDummySP.
		try {
			return (IPersonalAttributeList)cls.getDeclaredMethod("retrievePersonalAttributeList", args).invoke(null, params);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
