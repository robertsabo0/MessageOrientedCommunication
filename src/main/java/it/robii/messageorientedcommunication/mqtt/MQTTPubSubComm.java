package it.robii.messageorientedcommunication.mqtt;

import it.robii.messageorientedcommunication.PubSubComm;
import it.robii.messageorientedcommunication.config.ConfigManager;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
            // connectOptions.setMaxInflight(Short.MAX_VALUE);

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
            msg.setQos(0);
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
                String message = new String(mqttMessage.getPayload());
                log.debug("Got on topic:"+topic+" message: "+message);
                if(onMessageReceivedTable.containsKey(topic))
                    onMessageReceivedTable.get(topic).accept(message);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        try {
            mqttClient.subscribe(topic);
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
