package direnaj.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private Properties prop;
    private static PropertiesUtil propertiesUtil;

    private PropertiesUtil() {
        prop = new Properties();
        InputStream input = null;
        try {
//            input = new FileInputStream("/var/lib/tomcat7/toolkitConfig/toolkitConfig.properties");´
            input = new FileInputStream("/data/direnaj/toolkitConfig.properties");
            // load a properties file
            prop.load(input);
            // get the property value and print it out
            System.out.println(prop.getProperty("direnajUserId"));
            System.out.println(prop.getProperty("direnajPassword"));
            System.out.println(prop.getProperty("neo4j.server.rootUri"));
            System.out.println(prop.getProperty("mongo.server.address"));
            System.out.println(prop.getProperty("mongo.server.port"));
            System.out.println(prop.getProperty("mongo.bulk.insert.size"));
            System.out.println(prop.getProperty("mongo.usedDB"));
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

    public String getProperty(String property) {
        try {
            return prop.getProperty(property);
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getIntProperty(String property) {
        try {
            return Integer.valueOf(prop.getProperty(property));
        } catch (Exception e) {
            return null;
        }
    }

}
