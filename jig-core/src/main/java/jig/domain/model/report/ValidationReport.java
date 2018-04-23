package jig.domain.model.report;

import jig.domain.model.angle.validation.AnnotationDetail;
import jig.domain.model.angle.validation.ValidationConcern;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportRow;
import jig.domain.model.report.template.Title;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ValidationReport implements Report {

    private final List<AnnotationDetail> list;

    public ValidationReport(List<AnnotationDetail> list) {
        this.list = list;
    }

    @Override
    public Title title() {
        return new Title("VALIDATION");
    }

    @Override
    public ReportRow headerRow() {
        return Arrays.stream(ValidationConcern.values())
                .map(Enum::name)
                .collect(ReportRow.collector());
    }

    @Override
    public List<ReportRow> rows() {
        return list.stream()
                .map(detail -> Arrays.stream(ValidationConcern.values()).map(concern -> concern.apply(detail))
                        .collect(ReportRow.collector())).collect(toList());
    }
}
