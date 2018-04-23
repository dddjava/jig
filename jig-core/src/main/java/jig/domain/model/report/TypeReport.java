package jig.domain.model.report;

import jig.domain.model.angle.type.TypeDetail;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.ReportRow;
import jig.domain.model.report.template.Title;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class TypeReport implements Report {

    private final TypePerspective perspective;
    private final List<TypeDetail> list;

    public TypeReport(TypePerspective perspective, List<TypeDetail> list) {
        this.perspective = perspective;
        this.list = list;
    }

    @Override
    public Title title() {
        return new Title(perspective.name());
    }

    @Override
    public ReportRow headerRow() {
        return perspective.headerLabel();
    }

    @Override
    public List<ReportRow> rows() {
        return list.stream().map(perspective::row).collect(toList());
    }
}
