package jig.classlist.report.type;

import jig.classlist.report.Report;

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
    public List<String> headerLabel() {
        return perspective.headerLabel();
    }

    @Override
    public List<List<String>> rowList() {
        return list.stream().map(perspective::row).collect(toList());
    }
}
