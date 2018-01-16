package eu.stork.ss.specific.general;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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


public class SaveDummySP extends SavePersonalAttributeList {
	/**
	 * Unique identifier.
	 */
	private static final long serialVersionUID = 1588295800440516440L;

	//Logger
	static final Logger logger = LoggerFactory.getLogger(SaveDummySP.class.getName());
	
	@Override
	protected String savePersonalAttributeList(String token, IPersonalAttributeList pal, String sp) {
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
			cls = Class.forName("eu.stork.ss.specific."+comm_type+".SaveDummySP");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Class<?> args[] = new Class[3];
		args[0] = String.class;
		args[1] = IPersonalAttributeList.class;
		args[2] = String.class;
		
		Object params[] =new Object[3];
		params[0]=token;
		params[1]=pal;
		params[2]=sp;
		try {
			String s =  (String)(cls.getDeclaredMethod("savePersonalAttributeList", args).invoke(null, params));
			System.out.println(">>> "+s);
			return s;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
