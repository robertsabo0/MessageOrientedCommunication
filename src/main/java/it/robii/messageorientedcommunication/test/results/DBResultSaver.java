package it.robii.messageorientedcommunication.test.results;

import it.robii.messageorientedcommunication.CommType;
import it.robii.messageorientedcommunication.test.TestParams;
import it.robii.messageorientedcommunication.test.results.dbenities.DbTestParams;
import it.robii.messageorientedcommunication.test.results.dbenities.DbTestResult;
import it.robii.messageorientedcommunication.test.results.dbenities.DbTestRun;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DBResultSaver extends ResultSaver{

    static DbTestParams dbTestParams;
    DbTestRun testRun;
    ConcurrentLinkedQueue<DbTestResult> testResultList;

    @Override
    public void initTestConcrete(TestParams params) {
        saveTestParams(params);

        testResultList = new ConcurrentLinkedQueue<>();
        testRun = new DbTestRun();
        testRun.setCommType(params.getCommType());
        testRun.setTestParamsByTestParamsId(dbTestParams);
        Session session = buildSessionFactory().openSession();
        session.save(testRun);
        session.close();
    }

    @Override
    public void addResult(long msFromStart, long receiveTSMinusSentTSInMS) {
        DbTestResult res = new DbTestResult();
        res.setResponseTime(receiveTSMinusSentTSInMS);
        res.setTsFromStart(msFromStart);
        res.setTestRunByTestRunId(testRun);
        testResultList.add(res);
    }

    @Override
    public void done() {
        Session session = buildSessionFactory().openSession();
        HashMap<CommType, Integer> typeToCount = new HashMap<>();
        typeToCount.put(CommType.KAFKA, 0);
        typeToCount.put(CommType.MQTT, 0);
        typeToCount.put(CommType.REDIS, 0);

        for(DbTestResult res : testResultList) {
            session.save(res);
            CommType type = CommType.valueOf(res.getTestRunByTestRunId().getCommType());
            typeToCount.put(type, typeToCount.get(type)+1);
        }
        session.close();
    }


    static void saveTestParams(TestParams params){
        if(dbTestParams != null) return;
        dbTestParams = new DbTestParams(params);

        Session session = buildSessionFactory().openSession();
        session.save(dbTestParams);
        session.close();
    }

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory()
    {
        try
        {
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // Close caches and connection pools
        getSessionFactory().close();
    }
}
