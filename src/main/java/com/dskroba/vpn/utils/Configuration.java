package com.dskroba.vpn.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class Configuration {
    private static final Logger log = LogManager.getLogger(Configuration.class);

    private static final String DEFAULT_CONFIG_FILE_PROP = "instance.conf";
    private static volatile Properties globalProperties;

    public static Properties loadGlobalProperties() {
        if (globalProperties == null) {
            synchronized (Configuration.class) {
                if (globalProperties == null) {
                    globalProperties = readProperties();
                }
            }
        }
        return globalProperties;
    }

    private static Properties readProperties() {
        String path = System.getProperty(DEFAULT_CONFIG_FILE_PROP);
        if (path == null) {
            log.warn("Config file not set. Using default properties.");
            path = "";
        }
        log.info("Read properties from files (-D" + DEFAULT_CONFIG_FILE_PROP + "={}).", path);
        Properties propertiesFromPaths = getPropertiesFromPaths(path);

        addPropertiesToSystem(propertiesFromPaths);

        return propertiesFromPaths;
    }

    private static void addPropertiesToSystem(Properties properties) {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            System.setProperty(key, value);
            log.debug("Set system property: {}={}", key, value);
        }
    }

    private static Properties getPropertiesFromPaths(String allPaths) {
        String[] paths = allPaths.split(";");

        Properties properties = new Properties();
        for (String path : paths) {
            if (path.isBlank()) {
                continue;
            }
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(path))) {
                properties.load(reader);
                log.info("Loaded {} properties from: {}", properties.size(), path);
            } catch (FileNotFoundException e) {
                log.error("Can't read properties from non-existing config file: {}", path);
                throw new IllegalArgumentException("Can't read properties from non-existing config file: " + path, e);
            } catch (IOException e) {
                log.error("Can't read properties from file: {}, {}", path, e);
                throw new RuntimeException("Can't read properties from file: " + path, e);
            }
        }

        return properties;
    }

    private Configuration() {
    }
}
