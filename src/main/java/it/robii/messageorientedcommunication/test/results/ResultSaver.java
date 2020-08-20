package it.robii.messageorientedcommunication.test.results;

import it.robii.messageorientedcommunication.test.PerfTester;
import it.robii.messageorientedcommunication.test.TestParams;

import java.time.Instant;
import java.time.ZonedDateTime;

public interface ResultSaver {
    void initTest(TestParams params);
    void addResult(long receiveTSMinusSentTSInMS);
    void done();
}
