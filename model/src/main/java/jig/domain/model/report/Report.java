package jig.domain.model.report;

import java.util.List;

public interface Report {

    ReportRow headerRow();

    List<ReportRow> rows();
}
