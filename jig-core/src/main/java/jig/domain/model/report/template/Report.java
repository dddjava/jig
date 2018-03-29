package jig.domain.model.report.template;

import java.util.List;

public interface Report {

    Title title();

    ReportRow headerRow();

    List<ReportRow> rows();
}
