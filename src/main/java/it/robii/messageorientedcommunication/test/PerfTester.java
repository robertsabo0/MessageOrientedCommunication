package it.robii.messageorientedcommunication.test;

import it.robii.messageorientedcommunication.CommType;
import it.robii.messageorientedcommunication.PubSubComm;
import it.robii.messageorientedcommunication.config.ConfigManager;
import it.robii.messageorientedcommunication.kafka.KafkaPubSubComm;
import it.robii.messageorientedcommunication.mqtt.MQTTPubSubComm;
import it.robii.messageorientedcommunication.redis.RedisPubSubComm;
import it.robii.messageorientedcommunication.test.results.ResultSaver;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Log4j2
public class PerfTester {

    public static PerfTester InitTest(int testDuration, int everyXms, int sendYmessages, int ofZsize, int paralelOnTThreads, CommType commType, ResultSaver resultSaver){
        TestParams testParams = new TestParams(testDuration, everyXms, sendYmessages, ofZsize, paralelOnTThreads, commType);
        PerfTester perfTester = new PerfTester(testParams);
        perfTester.setCommunication(initCommunication(commType));
        perfTester.setResultSaver(resultSaver);
        perfTester.topic = ConfigManager.appYamlConfig().getPubSubTopic();
        return perfTester;
    }

    static PubSubComm initCommunication(CommType commType){
        switch (commType){
            case MQTT: return MQTTPubSubComm.buildAndConnect();
            case KAFKA: return KafkaPubSubComm.buildAndConnect();
            case REDIS: return RedisPubSubComm.buildAndConnect();
            default:
                throw new IllegalArgumentException("CommType "+commType+" is not supported");
        }
    }

    TestParams testParams;
    PubSubComm communication;
    ResultSaver resultSaver;
    String topic;


    private PerfTester(TestParams testParams){
        this.testParams = testParams;
    }
    private void setCommunication(PubSubComm communication){
        this.communication = communication;
    }
    private void setResultSaver(ResultSaver resultSaver){
        this.resultSaver = resultSaver;
    }


    Map<String, Instant> messagesSent;
    public void start(){
        subscribeCommunication();
        messagesSent = new HashMap<>();
        resultSaver.initTest(testParams);
        Instant endTestAt = Instant.now().plusSeconds(testParams.testDurationSeconds);
        log.debug("Starting test...");
        while(Instant.now().isBefore(endTestAt)){
            try { Thread.sleep(testParams.everyXms); } catch (InterruptedException e) {}

            List<SendingMessage> messages = SendingMessage.makeMessages(testParams.sendYmessages, testParams.ofZsize);
            log.debug("Made messages, let's send them!");
            for (SendingMessage msg: messages) {
                String toSend = msg.toString();
                messagesSent.put(msg.guid, Instant.now());
                communication.publish(this.topic, toSend);
            }
            log.debug("all messages sent!");
        }
        while(!messagesSent.isEmpty()){
            log.debug("waiting to receive all messages. Stil have "+messagesSent.size()+" not received");
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        log.debug("Ok, all done for now... yea");
        resultSaver.done();
    }

    private void subscribeCommunication() {
        communication.subscribe(this.topic, msg ->{
            log.debug("ok, got message, let's hanlde it");
            Instant gotMessageOn = Instant.now();
            SendingMessage message = SendingMessage.fromJson(msg);
            long diff = -1;
            if(messagesSent.containsKey(message.guid)) {
                Instant sentOn = messagesSent.remove(message.guid);
                diff = Duration.between(sentOn, gotMessageOn).toMillis();
            }
            resultSaver.addResult(diff);
            log.debug("this one handled!");
        });
    }


}
