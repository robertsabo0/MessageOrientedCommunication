package it.robii.messageorientedcommunication;


import it.robii.messageorientedcommunication.config.ConfigManager;
import it.robii.messageorientedcommunication.kafka.KafkaPubSubComm;
import it.robii.messageorientedcommunication.mqtt.MQTTPubSubComm;
import it.robii.messageorientedcommunication.redis.RedisPubSubComm;
import it.robii.messageorientedcommunication.test.PerfTester;
import it.robii.messageorientedcommunication.test.TestParams;
import it.robii.messageorientedcommunication.test.results.DBResultSaver;
import it.robii.messageorientedcommunication.test.results.JSONResultSaver;
import it.robii.messageorientedcommunication.test.results.ResultSaver;
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

        if(true) {
            // TestMqtt();
            TestKafka();
            return;
        }
       // TestDBResultSaver();

        int testDuration = 10;
        int everyXms = 50;
        int sendYmessages = 10;
        int msgSize = 1000;
        int paralelOnThreads = 5;
        // ResultSaver resultSaver = new JSONResultSaver();
        ResultSaver resultSaver = new DBResultSaver();

        /*
        switch (commType){
            case KAFKA: break; // TestKafka(); break; neki problemi. ne mogu 2 puta subscribe. Zabaviti se s ovim...
            case MQTT: TestMqtt(); break;
            case REDIS: TestRedis(); break;
        }*/
        CommType[] commTypesOrdered = {
          // CommType.KAFKA,
          // CommType.REDIS,
          CommType.MQTT
        };
        for(CommType commType : commTypesOrdered) {
            log.info("Starting test with "+commType);
            sleep(1000);
            PerfTester.InitTest(testDuration, everyXms, sendYmessages, msgSize, paralelOnThreads, commType, resultSaver)
                    .smashIt();
            log.info("done test with "+commType);
            sleep(1000);
        }
        log.info("Done with all testes!");
        sleep(2000);
        System.exit(0);
    }

    private static void TestDBResultSaver() {
        ResultSaver res = new DBResultSaver();
        res.initTest(new TestParams(1,2,3,4,5,CommType.MQTT));
        res.addResult(12);
        res.addResult(12);
        res.addResult(12);
        res.done();
    }

    public static void sleep(int ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
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
