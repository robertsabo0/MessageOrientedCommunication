package it.robii.messageorientedcommunication;

import java.util.function.Consumer;

public interface PubSubComm extends AutoCloseable {
    public boolean isConnected();
    public boolean connect();
    public void publish(String topic, String message);
    public void subscribe(String topic, Consumer<String> onMessage);
}
