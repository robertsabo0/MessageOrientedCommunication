package it.robii.messageorientedcommunication.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;

@Log4j2
public class ConfigManager {

    static String appYamlFile = "app.yaml";

    static{
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream stream = classLoader.getResourceAsStream(appYamlFile);
            // File file = new File(stream);
            // if(!file.exists())throw new FileNotFoundException("Config file "+appYamlFile+" not found");
            ObjectMapper om = new ObjectMapper(new YAMLFactory());
            appYamlConfig = om.readValue(stream, AppYamlConfig.class);

            log.debug("All config initializes successfully!");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        initObjectMapper();
    }

    private static AppYamlConfig appYamlConfig;
    private static ObjectMapper objectMapper;
    public static AppYamlConfig appYamlConfig() {
        return appYamlConfig;
    }
    public static ObjectMapper getObjectMapper(){return objectMapper; }

    static void initObjectMapper(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
