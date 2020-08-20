package it.robii.messageorientedcommunication.test;

import it.robii.messageorientedcommunication.CommType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TestParams {
    int testDurationSeconds;
    int everyXms;
    int sendYmessages;
    int ofZsize;
    int paralelOnTThreads;
    CommType commType;

    public TestParams(){}
}
