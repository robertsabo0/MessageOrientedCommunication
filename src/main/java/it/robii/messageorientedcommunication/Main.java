package it.robii.messageorientedcommunication;


import it.robii.messageorientedcommunication.config.ConfigManager;
import it.robii.messageorientedcommunication.mqtt.MQTTPubSubComm;
import it.robii.messageorientedcommunication.redis.RedisPubSubComm;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;

@Log4j2
public class Main {

    static String topic;
    static String message;
    public static void main(String[] args) {
        log.debug(ConfigManager.appYamlConfig().getHellostring());
        topic = ConfigManager.appYamlConfig().getPubSubTopic();
        message = "EvetiKur be: "+ Instant.now();
        // TestMqtt();
        TestRedis();
    }

    static void TestMqtt(){
        try(MQTTPubSubComm mqttPubSubComm = MQTTPubSubComm.buildAndConnect()){
            testPubSub(mqttPubSubComm);
        } catch (Exception e) {
            log.error(e);
        }
    }

    static void TestRedis(){
        try(RedisPubSubComm mqttPubSubComm = RedisPubSubComm.buildAndConnect()){
            testPubSub(mqttPubSubComm);
        } catch (Exception e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    static void testPubSub(PubSubComm comm){
        comm.subscribe(topic, t -> log.debug("Hey, man, i got a message:"+t));
        comm.publish(topic, message);
    }
}
