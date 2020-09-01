package it.robii.messageorientedcommunication.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.robii.messageorientedcommunication.config.ConfigManager;
import lombok.Data;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


public class SendingMessage{
    public String guid;
    public String data;
    public SendingMessage(){}
    public SendingMessage(int length) {
        guid = UUID.randomUUID().toString();
        data = radnomMessageWithLength(length);
    }
    private String radnomMessageWithLength(int length){
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        String generatedString = buffer.toString();
        return generatedString;
    }

    @Override
    public String toString(){
        try {
            return ConfigManager.getObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<SendingMessage> makeMessages(int sendYmessages, int ofZsize) {
        List<SendingMessage> messages = new ArrayList<>(ofZsize);
        for(int i = 0; i<sendYmessages; i++) {
            messages.add(new SendingMessage(ofZsize));
        }
        return messages;
    }

    public static SendingMessage fromJson(String json) {
        try {
            return ConfigManager.getObjectMapper().readValue(json, SendingMessage.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}