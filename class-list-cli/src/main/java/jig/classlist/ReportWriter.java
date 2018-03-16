package jig.classlist;

import jig.domain.model.report.Report;

import java.nio.file.Path;

public interface ReportWriter {
    void writeTo(Report report, Path output);
}
