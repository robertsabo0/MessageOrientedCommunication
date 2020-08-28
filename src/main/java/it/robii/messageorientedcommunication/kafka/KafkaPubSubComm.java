package it.robii.messageorientedcommunication.kafka;

import it.robii.messageorientedcommunication.PubSubComm;
import it.robii.messageorientedcommunication.config.AppYamlConfig;
import it.robii.messageorientedcommunication.config.ConfigManager;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.rmi.server.ExportException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Log4j2
public class KafkaPubSubComm implements PubSubComm {

    public static KafkaPubSubComm buildAndConnect(){
        KafkaPubSubComm kafka = new KafkaPubSubComm();
        kafka.connect();
        return kafka;
    }

    public static String KAFKA_BROKERS = ConfigManager.appYamlConfig().getKafkaAddress();
    public static String CLIENT_ID="client1";
    public static String GROUP_ID_CONFIG="consumerGroup1";
    public static Integer MAX_NO_MESSAGE_FOUND_COUNT=100;
    public static String OFFSET_RESET_LATEST="latest";
    public static String OFFSET_RESET_EARLIER="earliest";
    public static Integer MAX_POLL_RECORDS=1;


    Properties properties;
    KafkaProducer<Long, String> producer;
    KafkaConsumer<Long, String> consumer;
    public KafkaPubSubComm(){
        properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID_CONFIG);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  LongSerializer.class.getName());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET_EARLIER);
    }

    @Override
    public boolean isConnected() {
        return consumer != null && producer != null;
    }

    @Override
    public boolean connect() {
        consumer = new KafkaConsumer<>(properties);
        producer = new KafkaProducer<>(properties);
        return true;
    }

    private long generateRandomKey(){
        return (long)(Math.random() *1000);
    }

    @Override
    public void publish(String topic, String message) {
        ProducerRecord<Long, String> record = new ProducerRecord<>
                (topic,generateRandomKey(),  message);
        try {
            producer.send(record).get();
        } catch (Exception e) {
            log.error(e+"");
            e.printStackTrace();
        }
    }

    Thread subscribeThread;
    @Override
    public void subscribe(String topic, Consumer<String> onMessage) {
        consumer.subscribe(Collections.singletonList(topic));
        subscribeThread = new Thread(() -> {
            while (true) {
                ConsumerRecords<Long, String> consumerRecords;
                try {
                    consumerRecords = consumer.poll(Duration.ofMillis(1000));
                } catch (Exception ex){ break; }
                if(Thread.currentThread().isInterrupted()) break;
                // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
                if (consumerRecords.count() == 0)  continue;
                //print each record.
                consumerRecords.forEach(record -> {
                    onMessage.accept(record.value());
                });
                // commits the offset of record to broker.
                consumer.commitAsync();
            }
        });
        subscribeThread.setName("KafkaSubscribeThread");
        subscribeThread.setDaemon(true);
        subscribeThread.start();
    }

    @Override
    public void close() throws Exception {
        subscribeThread.interrupt();
    }
}
