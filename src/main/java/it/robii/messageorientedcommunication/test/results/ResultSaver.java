package it.robii.messageorientedcommunication.test.results;

import it.robii.messageorientedcommunication.test.PerfTester;
import it.robii.messageorientedcommunication.test.TestParams;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

public abstract class ResultSaver {

    Instant start;
    public void initTest(TestParams params){
        start = Instant.now();
        initTestConcrete(params);
    }
    public void addResult(long receiveTSMinusSentTSInMS){
        Duration diff = Duration.between(start, Instant.now());
        addResult(diff.toMillis(), receiveTSMinusSentTSInMS);
    }

    protected abstract void initTestConcrete(TestParams params);
    protected abstract void addResult(long msFromStart, long receiveTSMinusSentTSInMS);
    public abstract void done();
}
