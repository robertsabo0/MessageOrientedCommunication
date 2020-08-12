package it.robii.messageorientedcommunication.kafka;

import it.robii.messageorientedcommunication.PubSubComm;
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

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class KafkaPubSubComm implements PubSubComm {

    public static KafkaPubSubComm buildAndConnect(){
        KafkaPubSubComm kafka = new KafkaPubSubComm();
        kafka.connect();
        return kafka;
    }

    public static String KAFKA_BROKERS = "localhost:9092";
    public static Integer MESSAGE_COUNT=1000;
    public static String CLIENT_ID="client1";
    public static String TOPIC_NAME="demo";
    public static String GROUP_ID_CONFIG="consumerGroup1";
    public static Integer MAX_NO_MESSAGE_FOUND_COUNT=100;
    public static String OFFSET_RESET_LATEST="latest";
    public static String OFFSET_RESET_EARLIER="earliest";
    public static Integer MAX_POLL_RECORDS=1;


    Properties producerProperties;
    Properties consumerProperties;
    KafkaProducer<Long, String> producer;
    KafkaConsumer<Long, String> consumer;
    public KafkaPubSubComm(){
        producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        producerProperties.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID_CONFIG);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLL_RECORDS);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET_EARLIER);

    }

    @Override
    public boolean isConnected() {
        return consumer != null && producer != null;
    }

    @Override
    public boolean connect() {
        consumer = new KafkaConsumer<>(consumerProperties);
        producer = new KafkaProducer<>(producerProperties);
        return true;
    }

    @Override
    public void publish(String topic, String message) {
        ProducerRecord<Long, String> record = new ProducerRecord<>
                (topic,(long)(Math.random() *1000),  message);
        try {
            producer.send(record).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(String topic, Consumer<String> onMessage) {
        consumer.subscribe(Collections.singletonList(topic));
        new Thread(() -> {
            while (true) {
                ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));
                // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
                if (consumerRecords.count() == 0) {
                    continue;
                }
                //print each record.
                consumerRecords.forEach(record -> {
                    System.out.println("Record Key " + record.key());
                    System.out.println("Record value " + record.value());
                    System.out.println("Record partition " + record.partition());
                    System.out.println("Record offset " + record.offset());
                    onMessage.accept(record.value());
                });
                // commits the offset of record to broker.
                consumer.commitAsync();
            }
        }).start();
    }

    @Override
    public void close() throws Exception {

    }
}
