package jig.infrastructure.poi;

import jig.domain.model.report.template.Reports;

import java.nio.file.Path;

public interface ReportWriter {

    void writeTo(Reports reports, Path output);
}
