package com.regnosys.rosetta.common.serialisation.json.reportdata;

import com.regnosys.rosetta.common.reports.RegReportIdentifier;

import java.util.Objects;

public class ReportIdentifierDataSet {

    private RegReportIdentifier reportIdentifier;
    private ReportDataSet dataSet;

    public ReportIdentifierDataSet(RegReportIdentifier reportIdentifier, ReportDataSet dataSet) {
        this.reportIdentifier = reportIdentifier;
        this.dataSet = dataSet;
    }

    public ReportIdentifierDataSet() {
    }

    public RegReportIdentifier getReportIdentifier() {
        return reportIdentifier;
    }

    public ReportDataSet getDataSet() {
        return dataSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportIdentifierDataSet that = (ReportIdentifierDataSet) o;
        return Objects.equals(reportIdentifier, that.reportIdentifier) && Objects.equals(dataSet, that.dataSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportIdentifier, dataSet);
    }

    @Override
    public String toString() {
        return "ReportIdentifierDataSet{" +
                "reportIdentifier=" + reportIdentifier +
                ", dataSet=" + dataSet +
                '}';
    }
}