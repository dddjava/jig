package jig.classlist.report.method;

import jig.classlist.report.Report;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class MethodReport implements Report {

    private final MethodPerspective perspective;
    private final List<MethodDetail> list;

    public MethodReport(MethodPerspective perspective, List<MethodDetail> list) {
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
