package it.robii.messageorientedcommunication.config;

import lombok.Data;

@Data
public class AppYamlConfig {
    private String hellostring;
    private String pubSubTopic;

    private String mqttAddress;
    private String mqttUsername;
    private String mqttPassword;

    private String redisAddress;
    private String kafkaAddress;

    private Object log4j;
}
