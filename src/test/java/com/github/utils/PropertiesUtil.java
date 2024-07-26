package com.github.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
	private static Properties properties = new Properties();

    static {
        try (InputStream input = PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("IOException occurred while loading properties file", ex);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static String getOwner() {
        return properties.getProperty("owner");
    }

    public static String getRepo() {
        return properties.getProperty("repo");
    }
}
