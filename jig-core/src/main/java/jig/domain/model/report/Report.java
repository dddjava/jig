package jig.domain.model.report;

import java.util.List;

public interface Report {

    Title title();

    ReportRow headerRow();

    List<ReportRow> rows();
}
