package it.robii.messageorientedcommunication;

import it.robii.messageorientedcommunication.kafka.KafkaPubSubComm;
import it.robii.messageorientedcommunication.mqtt.MQTTPubSubComm;
import it.robii.messageorientedcommunication.redis.RedisPubSubComm;

public class PubSubCommFactory {


    public static PubSubComm createPubSubComm(CommType commType){
        switch (commType){
            case MQTT: return MQTTPubSubComm.buildAndConnect();
            case KAFKA: return KafkaPubSubComm.buildAndConnect();
            case REDIS: return RedisPubSubComm.buildAndConnect();
            default:
                throw new IllegalArgumentException("CommType "+commType+" is not supported");
        }
    }
}
