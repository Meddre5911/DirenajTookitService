package direnaj.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

public class PropertiesUtil {

	private Properties prop;
	private static PropertiesUtil propertiesUtil;

	private PropertiesUtil() {
		prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("/home/direnaj/toolkit/toolkitConfig/toolkitConfig.properties");
			// load a properties file
			prop.load(input);
			// write all properties
			Logger.getLogger(PropertiesUtil.class).debug("All properties");
			Set<Object> propertyKeySet = prop.keySet();
			for (Object propertyKey : propertyKeySet) {
				String keyStr = propertyKey.toString();
				// get the property value and print it out
				Logger.getLogger(PropertiesUtil.class).debug(keyStr + " - " + prop.getProperty(keyStr));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static PropertiesUtil getInstance() {
		if (propertiesUtil == null) {
			propertiesUtil = new PropertiesUtil();
		}
		return propertiesUtil;
	}

	public String getProperty(String property, String defaultValue) {
		try {
			String foundProperty = prop.getProperty(property);
			if (TextUtils.isEmpty(foundProperty)) {
				foundProperty = defaultValue;
			}
			return foundProperty;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public Integer getIntProperty(String property, Integer defaultValue) {
		try {
			return Integer.valueOf(prop.getProperty(property));
		} catch (Exception e) {
			return defaultValue;
		}
	}

	public Boolean getBooleanProperty(String property, Boolean defaultValue) {
		try {
			return Boolean.valueOf(prop.getProperty(property));
		} catch (Exception e) {
			return defaultValue;
		}
	}

}
