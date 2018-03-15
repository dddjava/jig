package jig.classlist.report.type;

import jig.classlist.report.Report;
import jig.classlist.report.ReportRow;

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
    public ReportRow headerRow() {
        return perspective.headerLabel();
    }

    @Override
    public List<ReportRow> rows() {
        return list.stream().map(perspective::row).collect(toList());
    }
}
