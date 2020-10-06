package it.robii.messageorientedcommunication.mqtt;

import it.robii.messageorientedcommunication.PubSubComm;
import it.robii.messageorientedcommunication.config.ConfigManager;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.function.Consumer;

@Log4j2
public class MQTTPubSubComm implements PubSubComm {

    public static MQTTPubSubComm buildAndConnect(){
        MQTTPubSubComm comm = null;
        try {
            comm = new MQTTPubSubComm(ConfigManager.appYamlConfig().getMqttAddress());
            comm.notAnonymous(ConfigManager.appYamlConfig().getMqttUsername(), ConfigManager.appYamlConfig().getMqttPassword());
            comm.connect();
        } catch (Throwable e) {
            log.error(e);
        }
        return comm;
    }

    Map<String, Consumer<String>> onMessageReceivedTable = new HashMap<>();
    public MQTTPubSubComm(String address, String uuid) throws MqttException {
        mqttClient = new MqttClient(address, uuid);
    }
    public MQTTPubSubComm(String address) throws MqttException {
        this(address, UUID.randomUUID().toString());
    }
    
    MqttClient mqttClient;
    boolean isAnonymous;
    String username;
    String pass;
    int qos = 0;
    ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(16);

    private void notAnonymous(String mqttUsername, String mqttPassword) {
        this.username = mqttUsername;
        this.pass = mqttPassword;
        this.isAnonymous = false;
    }


    @Override
    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    @Override
    public boolean connect() {
        try {
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setMaxInflight(999);

            if(!isAnonymous) {
                connectOptions.setUserName(username);
                connectOptions.setPassword(pass.toCharArray());
            }

            mqttClient.connect(connectOptions);
            return isConnected();
        } catch (MqttException e) {
            log.error(e);
            return false;
        }
    }

    @Override
    public void publish(String topic, String message) {
        try {
            MqttMessage msg = new MqttMessage(message.getBytes());
            msg.setQos(qos);
            mqttClient.publish(topic, msg);
        } catch (MqttException e) {
            log.error(e);
        }
    }


    @Override
    public void subscribe(String topic, final Consumer<String> onMessage) {
        onMessageReceivedTable.put(topic, onMessage);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                log.error("Connection lost");
                log.error(throwable);
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                executorService.execute(() ->{
                    String message = new String(mqttMessage.getPayload());
                    log.debug("Got on topic:"+topic+" message");
                    if(onMessageReceivedTable.containsKey(topic))
                        onMessageReceivedTable.get(topic).accept(message);
                    else
                        log.error("PROBLEM!");
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        try {
            mqttClient.subscribe(topic, qos);
        } catch (MqttException e) {
            log.error(e);
        }
    }

    @Override
    public void close() throws Exception {
        mqttClient.disconnect();
        mqttClient.close();
    }
}
