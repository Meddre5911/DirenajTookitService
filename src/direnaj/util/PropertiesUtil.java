package direnaj.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesUtil {

    private Properties prop;
    private static PropertiesUtil propertiesUtil;

    private PropertiesUtil() {
        prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("/var/lib/tomcat7/toolkitConfig/toolkitConfig.properties");
            //            input = new FileInputStream("/data/direnaj/toolkitConfig.properties");
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            Logger.getLogger(PropertiesUtil.class).debug(prop.getProperty("direnajUserId"));
            Logger.getLogger(PropertiesUtil.class).debug(prop.getProperty("direnajPassword"));
            Logger.getLogger(PropertiesUtil.class).debug(prop.getProperty("neo4j.server.rootUri"));
            Logger.getLogger(PropertiesUtil.class).debug(prop.getProperty("mongo.server.address"));
            Logger.getLogger(PropertiesUtil.class).debug(prop.getProperty("mongo.server.port"));
            Logger.getLogger(PropertiesUtil.class).debug(prop.getProperty("mongo.bulk.insert.size"));
            Logger.getLogger(PropertiesUtil.class).debug(prop.getProperty("mongo.usedDB"));
            System.out.println(prop.getProperty("graphDb.closenessCentrality.calculation.hopNode"));
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

}
