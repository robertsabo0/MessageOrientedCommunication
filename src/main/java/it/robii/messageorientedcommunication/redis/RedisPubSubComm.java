package it.robii.messageorientedcommunication.redis;

import it.robii.messageorientedcommunication.PubSubComm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import it.robii.messageorientedcommunication.config.ConfigManager;
import lombok.extern.log4j.Log4j2;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

@Log4j2
public class RedisPubSubComm implements PubSubComm {

    URI uri;
    public static RedisPubSubComm buildAndConnect(){
        String address = ConfigManager.appYamlConfig().getRedisAddress();
        RedisPubSubComm comm = null;
        try {
            comm = new RedisPubSubComm(new URI(address));
            comm.connect();
        } catch (URISyntaxException e) {
            log.error(e);
        }
        return comm;
    }
    public RedisPubSubComm(URI uri){
        this.uri = uri;
    }
    Jedis getJedis() { return  new Jedis(uri);}
    @Override
    public boolean isConnected() {
        return getJedis().isConnected();
    }

    @Override
    public boolean connect() {
        // nothing to do
        return true;
    }

    @Override
    public void publish(String topic, String message) {
        log.debug("message publishing..");
        getJedis().publish(topic, message);
        log.debug("message published!");
    }

    @Override
    public void subscribe(String topic, Consumer<String> onMessageAction) {
        final JedisPubSub jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {

                log.debug("Message received");
                onMessageAction.accept(message);
            }
        };
        new Thread(() -> {
            getJedis().subscribe(jedisPubSub, topic);
        }).start();
        log.debug("subscribed to topic!");
    }

    @Override
    public void close() throws Exception {
        getJedis().disconnect();
    }
}
