package it.robii.messageorientedcommunication.test.results.dbenities;

import it.robii.messageorientedcommunication.test.TestParams;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "test_params", schema = "perf_test", catalog = "")
public class DbTestParams {
    private int testNo;
    private int testDurationSeconds;
    private int everyXms;
    private int sendYmessages;
    private int ofZsize;
    private int paralelOnTThreads;
    private Collection<DbTestRun> testRunsByTestNo;

    public DbTestParams(){
        testRunsByTestNo = new ArrayList<DbTestRun>(3);
    }

    public DbTestParams(TestParams params) {
        this();
        this.testDurationSeconds = params.getTestDurationSeconds();
        this.everyXms = params.getEveryXms();
        this.sendYmessages = params.getSendYmessages();
        this.ofZsize = params.getOfZsize();
        this.paralelOnTThreads = params.getParalelOnTThreads();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testNo")
    public int getTestNo() {
        return testNo;
    }

    public void setTestNo(int testNo) {
        this.testNo = testNo;
    }

    @Basic
    @Column(name = "testDurationSeconds")
    public int getTestDurationSeconds() {
        return testDurationSeconds;
    }

    public void setTestDurationSeconds(int testDurationSeconds) {
        this.testDurationSeconds = testDurationSeconds;
    }

    @Basic
    @Column(name = "everyXms")
    public int getEveryXms() {
        return everyXms;
    }

    public void setEveryXms(int everyXms) {
        this.everyXms = everyXms;
    }

    @Basic
    @Column(name = "sendYmessages")
    public int getSendYmessages() {
        return sendYmessages;
    }

    public void setSendYmessages(int sendYmessages) {
        this.sendYmessages = sendYmessages;
    }

    @Basic
    @Column(name = "ofZsize")
    public int getOfZsize() {
        return ofZsize;
    }

    public void setOfZsize(int ofZsize) {
        this.ofZsize = ofZsize;
    }

    @Basic
    @Column(name = "paralelOnTThreads")
    public int getParalelOnTThreads() {
        return paralelOnTThreads;
    }

    public void setParalelOnTThreads(int paralelOnTThreads) {
        this.paralelOnTThreads = paralelOnTThreads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbTestParams that = (DbTestParams) o;

        if (testNo != that.testNo) return false;
        if (testDurationSeconds != that.testDurationSeconds) return false;
        if (everyXms != that.everyXms) return false;
        if (sendYmessages != that.sendYmessages) return false;
        if (ofZsize != that.ofZsize) return false;
        if (paralelOnTThreads != that.paralelOnTThreads) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = testNo;
        result = 31 * result + testDurationSeconds;
        result = 31 * result + everyXms;
        result = 31 * result + sendYmessages;
        result = 31 * result + ofZsize;
        result = 31 * result + paralelOnTThreads;
        return result;
    }

    @OneToMany(mappedBy = "testParamsByTestParamsId")
    public Collection<DbTestRun> getTestRunsByTestNo() {
        return testRunsByTestNo;
    }

    public void setTestRunsByTestNo(Collection<DbTestRun> testRunsByTestNo) {
        this.testRunsByTestNo = testRunsByTestNo;
    }
}
