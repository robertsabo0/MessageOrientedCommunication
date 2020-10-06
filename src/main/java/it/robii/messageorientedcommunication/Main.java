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
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

@Log4j2
public class Main {

    static String topic;
    static String message;

    public static void main(String[] args) {
        log.debug(ConfigManager.appYamlConfig().getHellostring());
        topic = ConfigManager.appYamlConfig().getPubSubTopic();
        message = "EvetiKur be: "+ Instant.now();
        int testDuration        = 10;
        int everyXms            = 100;
        int sendYmessages       = 20;
        int msgSize             = 10_000;
        int paralelOnThreads    = 1;

        if(args!= null && args.length > 0 &&
                ((args[0]+"").trim().equals("-h") ||
                        (args[0]+"").trim().equals("--help"))){
            printUsage();
            return;
        }
        testDuration = tryToReadAndParse(args,0, testDuration, "testDuration");
        everyXms = tryToReadAndParse(args,1, everyXms, "everyXms");
        sendYmessages = tryToReadAndParse(args,2, sendYmessages, "sendYmessages");
        msgSize = tryToReadAndParse(args,3, msgSize, "msgSize");
        paralelOnThreads = tryToReadAndParse(args,4, paralelOnThreads, "paralelOnThreads");

        log.debug("testDuration="+testDuration);
        log.debug("everyXms="+everyXms);
        log.debug("sendYmessages="+sendYmessages);
        log.debug("msgSize="+msgSize);
        log.debug("paralelOnThreads="+paralelOnThreads);
//        if(true) {
//            TestMqtt();
//            TestKafka();
//            return;
//        }
       //TestDBResultSaver();

        // ResultSaver resultSaver = new JSONResultSaver();
        ResultSaver resultSaver = new DBResultSaver();

        CommType[] commTypesOrdered = {
          CommType.KAFKA,
          CommType.REDIS,
          CommType.MQTT
        };
        for(CommType commType : commTypesOrdered) {
            log.info("Starting test with "+commType);
            sleep(1000);
            PerfTester.InitTest(testDuration, everyXms, sendYmessages, msgSize, paralelOnThreads, commType, resultSaver)
                     .smashIt();
            log.info("done test with "+commType+" cooldown the resources now with 10s sleep...");
            sleep(10*1000);
        }
        log.info("Done with all testes!");
        sleep(2000);
        System.exit(0);
    }

    private static int tryToReadAndParse(String[] args, int i, int defVal, String paramName) {
        String toParse = null;
        if(args != null && args.length > i)
            toParse = args[i];
        toParse = (toParse +"").trim();
        try {
            int parsed = Integer.parseInt(toParse);

            return  parsed;
        } catch (Exception ex){
            System.out.println("Not parsed "+paramName+" from index "+i+". Set default "+defVal);
            return defVal;
        }
    }

    private static void printUsage() {
        System.out.println("commands on run are: 1 2 3 4 5");
        System.out.println("Where: ");
        System.out.println("1: Test duration in seconds");
        System.out.println("2: Cycle interval of sending messages");
        System.out.println("3: Number of messages to send");
        System.out.println("4: Message size");
        System.out.println("5: Number of threads sending from");
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

    final static Semaphore semaphore = new Semaphore(0);
    static void testPubSub(PubSubComm comm){
        log.debug("let's subscribe!");
        comm.subscribe(topic, t -> {
            log.debug("Hey, man, i got a message:"+t);
            semaphore.release();
        });
        log.debug("Subscribed!");
        log.debug("Let's publish...");
        comm.publish(topic, message);
        log.debug("Published...");
        log.debug("Waiting response...");
        try { semaphore.acquire(); } catch (InterruptedException e) {}
        log.debug("Got it. all good!");
    }
}
