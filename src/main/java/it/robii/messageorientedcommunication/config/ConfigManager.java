package it.robii.messageorientedcommunication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

@Log4j2
public class ConfigManager {

    static String appYamlFile = "app.yaml";

    static{
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resUrl = classLoader.getResource(appYamlFile);
            if(resUrl == null )throw new FileNotFoundException("Config file "+appYamlFile+" not found");
            File file = new File(resUrl.getFile());
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            appYamlConfig = om.readValue(file, AppYamlConfig.class);

            log.debug("All config initializes successfully!");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private static AppYamlConfig appYamlConfig;
    public static AppYamlConfig appYamlConfig() {
        return appYamlConfig;
    }

}
