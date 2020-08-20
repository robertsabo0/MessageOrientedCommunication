package it.robii.messageorientedcommunication.test.results;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.robii.messageorientedcommunication.config.ConfigManager;
import it.robii.messageorientedcommunication.test.TestParams;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.rmi.server.ExportException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.*;

public class JSONResultSaver implements ResultSaver {

    JSONResult result;
    Instant start;
    @Override
    public void initTest(TestParams params) {
        start = Instant.now();
        result = new JSONResult(params);
    }

    @Override
    public void addResult(long receiveTSMinusSentTS) {
        Duration diff = Duration.between(start, Instant.now());
        result.results.add(new JSONSingleResult(diff.toMillis(),receiveTSMinusSentTS));
    }
    @Override
    public void done(){
        try {
            result.resultsCount = result.results.size();
            Collections.sort(result.results, (o1, o2) ->
                    (int)(o1.tsFromStart - o2.tsFromStart)
                );
            String json = ConfigManager.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result);
            BufferedWriter bf = new BufferedWriter(new FileWriter(getCurrentFileResultName()));
            bf.write(json);
            bf.flush();
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    String getCurrentFileResultName(){
        return "jsonResults/"+("result_"+Instant.now()+".json").replaceAll(":","-");
    }

    @Data
    static class JSONResult {

        public JSONResult(TestParams testParams){
            this.testParams = testParams;
            this.results = new LinkedList<>();
        }
        TestParams testParams;
        int resultsCount;
        List<JSONSingleResult> results;
    }
    @Data
    @AllArgsConstructor
    static class JSONSingleResult{
        public long tsFromStart;
        public long responseTime;
    }
}
