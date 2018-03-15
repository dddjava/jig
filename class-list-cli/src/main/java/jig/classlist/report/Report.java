package jig.classlist.report;

import java.util.List;

public interface Report {

    ReportRow headerRow();

    List<ReportRow> rows();
}
