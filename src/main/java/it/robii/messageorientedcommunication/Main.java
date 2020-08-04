package it.robii.messageorientedcommunication;


import it.robii.messageorientedcommunication.config.ConfigManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

    public static void main(String[] args) {
        log.debug(ConfigManager.appYamlConfig().getHellostring());
    }

}
