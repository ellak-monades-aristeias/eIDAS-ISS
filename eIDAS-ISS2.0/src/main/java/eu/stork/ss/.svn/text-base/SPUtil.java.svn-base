package eu.stork.ss;

import java.io.IOException;
import java.util.Properties;

public class SPUtil {

	public static Properties loadConfigs(String path) throws IOException
	{
		Properties properties = new Properties();
		properties.load(SPUtil.class.getClassLoader().getResourceAsStream(path));
		return properties;
	}
}
