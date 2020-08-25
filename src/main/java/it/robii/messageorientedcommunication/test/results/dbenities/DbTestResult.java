package it.robii.messageorientedcommunication.test.results.dbenities;

import javax.persistence.*;

@Entity
@Table(name = "test_result", schema = "perf_test", catalog = "")
public class DbTestResult {
    private int id;
    private long tsFromStart;
    private long responseTime;
    private DbTestRun testRunByTestRunId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "tsFromStart")
    public long getTsFromStart() {
        return tsFromStart;
    }

    public void setTsFromStart(long tsFromStart) {
        this.tsFromStart = tsFromStart;
    }

    @Basic
    @Column(name = "responseTime")
    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbTestResult that = (DbTestResult) o;

        if (id != that.id) return false;
        if (tsFromStart != that.tsFromStart) return false;
        if (responseTime != that.responseTime) return false;

        return true;
    }

    @Override
    public int hashCode() {
        long result = id;
        result = 31 * result + tsFromStart;
        result = 31 * result + responseTime;
        return (int)result;
    }

    @ManyToOne
    @JoinColumn(name = "testRunId", referencedColumnName = "id", nullable = false, table = "test_result")
    public DbTestRun getTestRunByTestRunId() {
        return testRunByTestRunId;
    }

    public void setTestRunByTestRunId(DbTestRun testRunByTestRunId) {
        this.testRunByTestRunId = testRunByTestRunId;
    }
}
