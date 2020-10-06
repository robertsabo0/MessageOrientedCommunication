package it.robii.messageorientedcommunication.test.results.dbenities;

import it.robii.messageorientedcommunication.CommType;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "test_run", schema = "perf_test", catalog = "")
public class DbTestRun {
    private int id;
    private String commType;
    private Collection<DbTestResult> testResultsById;
    private DbTestParams testParamsByTestParamsId;

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
    @Column(name = "commType")
    public String getCommType() {
        return commType;
    }

    public void setCommType(String commType) {
        this.commType = commType;
    }

    public void setCommType(CommType commType){
        setCommType(commType.name());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DbTestRun dbTestRun = (DbTestRun) o;

        if (id != dbTestRun.id) return false;
        if (commType != null ? !commType.equals(dbTestRun.commType) : dbTestRun.commType != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (commType != null ? commType.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "testRunByTestRunId")
    public Collection<DbTestResult> getTestResultsById() {
        return testResultsById;
    }

    public void setTestResultsById(Collection<DbTestResult> testResultsById) {
        this.testResultsById = testResultsById;
    }

    @ManyToOne
    @JoinColumn(name = "testParamsId", referencedColumnName = "testNo", nullable = false, table = "test_run")
    public DbTestParams getTestParamsByTestParamsId() {
        return testParamsByTestParamsId;
    }

    public void setTestParamsByTestParamsId(DbTestParams testParamsByTestParamsId) {
        this.testParamsByTestParamsId = testParamsByTestParamsId;
    }
}
