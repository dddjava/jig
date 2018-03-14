package jig.classlist.report;

import java.nio.file.Path;

public interface ReportWriter {
    void writeTo(Report report, Path output);
}
