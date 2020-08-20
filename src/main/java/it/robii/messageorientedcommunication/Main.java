package it.robii.messageorientedcommunication;


import it.robii.messageorientedcommunication.config.ConfigManager;
import it.robii.messageorientedcommunication.kafka.KafkaPubSubComm;
import it.robii.messageorientedcommunication.mqtt.MQTTPubSubComm;
import it.robii.messageorientedcommunication.redis.RedisPubSubComm;
import it.robii.messageorientedcommunication.test.PerfTester;
import it.robii.messageorientedcommunication.test.results.JSONResultSaver;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;

@Log4j2
public class Main {

    static String topic;
    static String message;

    public static void main(String[] args) {

        log.debug(ConfigManager.appYamlConfig().getHellostring());
        topic = ConfigManager.appYamlConfig().getPubSubTopic();
        message = "EvetiKur be: "+ Instant.now();

        CommType commType = CommType.MQTT;

        switch (commType){
            case KAFKA: break; // TestKafka(); break; neki problemi. ne mogu 2 puta subscribe. Zabaviti se s ovim...
            case MQTT: TestMqtt(); break;
            case REDIS: TestRedis(); break;
        }
        PerfTester.InitTest(10, 1000, 10, 10, 1, commType, new JSONResultSaver())
                .start();
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

    static void TestKafka(){
        try(KafkaPubSubComm mqttPubSubComm = KafkaPubSubComm.buildAndConnect()){
            testPubSub(mqttPubSubComm);
            Thread.sleep(5000);
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
