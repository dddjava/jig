package jig.infrastructure.poi;

import jig.domain.model.report.Reports;

import java.nio.file.Path;

public interface ReportWriter {

    void writeTo(Reports reports, Path output);
}
