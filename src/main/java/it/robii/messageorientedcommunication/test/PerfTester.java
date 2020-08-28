package it.robii.messageorientedcommunication.test;

import it.robii.messageorientedcommunication.CommType;
import it.robii.messageorientedcommunication.Main;
import it.robii.messageorientedcommunication.PubSubComm;
import it.robii.messageorientedcommunication.PubSubCommFactory;
import it.robii.messageorientedcommunication.config.ConfigManager;
import it.robii.messageorientedcommunication.kafka.KafkaPubSubComm;
import it.robii.messageorientedcommunication.mqtt.MQTTPubSubComm;
import it.robii.messageorientedcommunication.redis.RedisPubSubComm;
import it.robii.messageorientedcommunication.test.results.ResultSaver;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.common.network.Send;

import javax.xml.transform.Result;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static it.robii.messageorientedcommunication.Main.sleep;

@Log4j2
public class PerfTester {

    // region Factory
    public static PerfTester InitTest(int testDuration, int everyXms, int sendYmessages, int ofZsize, int paralelOnTThreads, CommType commType, ResultSaver resultSaver){
        return InitTest(new TestParams(testDuration, everyXms, sendYmessages, ofZsize, paralelOnTThreads, commType),
                resultSaver);
    }
    public static PerfTester InitTest(TestParams testParams, ResultSaver resultSaver){
        PubSubComm communication = PubSubCommFactory.createPubSubComm(testParams.getCommType());
        String topic = topic = ConfigManager.appYamlConfig().getPubSubTopic();
        PerfTester perfTester = new PerfTester(testParams, communication, resultSaver, topic);
        return perfTester;
    }
    // endregion

    // region variables
    // region dependencies and params
    TestParams testParams;
    PubSubComm communication;
    ResultSaver resultSaver;
    String topic;
    // endregion
    // region Runtime using vars
    Map<String, Instant> messagesSent;
    Queue<SendingMessage> messageSendingQueue;
    boolean testRunning;
    // endregion
    // endregion

    private PerfTester(TestParams testParams, PubSubComm communication, ResultSaver resultSaver, String topic){
        this.testParams = testParams;
        this.communication = communication;
        this.resultSaver = resultSaver;
        this.topic = topic;
        messagesSent = new ConcurrentHashMap<>();
        messageSendingQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Do the test!
     */
    public void smashIt(){
        testRunning = true;
        subscribeCommunication();
        startSendingThreads(testParams.paralelOnTThreads);
        resultSaver.initTest(testParams);
        Instant endTestAt = Instant.now().plusSeconds(testParams.testDurationSeconds);
        log.debug("Starting test...");
        while(Instant.now().isBefore(endTestAt)){
            sleep(testParams.everyXms);
            List<SendingMessage> messages = SendingMessage.makeMessages(testParams.sendYmessages, testParams.ofZsize);
            log.debug("Made messages, let's send them!");
            messageSendingQueue.addAll(messages);
            /*
            for (SendingMessage msg: messages) {

            }*/
            log.debug("all messages added to queue!");
        }
        waitForEmptyTheQueue();
        testRunning = false;
        waitForReceiveAllMessages();
        log.debug("Ok, all done for now... yea");
        resultSaver.done();

        try{ communication.close();} catch (Exception e){log.error("Error on close: "+e.getMessage());}
    }

    /**
     * Subscribe to topic and onMessage implementation
     */
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

    /**
     * Wait with current thread to empty the sending queue
     */
    private void waitForEmptyTheQueue(){
        while(!messageSendingQueue.isEmpty()){
            log.debug("waiting empty the queue. Still have "+messageSendingQueue.size()+" not sent");
            sleep(100);
        }
    }

    /**
     * Wait with current thread to handle all sent messages
     * WARNING: in case of message loss, this could block... TODO: fix the warning
     */
    private void waitForReceiveAllMessages(){
        while(!messagesSent.isEmpty()){
            log.debug("waiting to receive all messages. Stil have "+messagesSent.size()+" not received");
            sleep(100);
        }
    }


    private void startSendingThreads(int count){
        Thread[] threads = new Thread[count];
        for(int i = 0; i<count; i++){
            threads[i] = new Thread(this::messageSendingFromQueue);
            threads[i].setName("Sender_no:"+i);
            threads[i].start();
        }
    }

    private void messageSendingFromQueue(){
        try{
            while(testRunning){
                if(messageSendingQueue.isEmpty())
                    sleep(10);
                SendingMessage msg;
                while((msg = messageSendingQueue.poll()) != null){
                    log.debug("seding message from "+Thread.currentThread().getName());
                    String toSend = msg.toString();
                    messagesSent.put(msg.guid, Instant.now());
                    communication.publish(this.topic, toSend);
                }
            }
        } catch (Exception ex){
            log.error("FATAL ERROR IN DEQUEUE thread! : "+ex.getMessage());
            ex.printStackTrace();
        }
    }
}
