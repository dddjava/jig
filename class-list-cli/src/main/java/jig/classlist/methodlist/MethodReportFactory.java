package jig.classlist.methodlist;

import jig.classlist.ReportFactory;

import java.util.List;

public class MethodReportFactory implements ReportFactory<MethodRelationNavigator> {

    private final MethodListType methodListType;
    private final List<MethodRelationNavigator> list;

    public MethodReportFactory(MethodListType methodListType, List<MethodRelationNavigator> list) {
        this.methodListType = methodListType;
        this.list = list;
    }

    @Override
    public List<String> headerLabel() {
        return methodListType.headerLabel();
    }

    @Override
    public List<String> row(MethodRelationNavigator param) {
        return methodListType.row(param);
    }

    @Override
    public List<MethodRelationNavigator> list() {
        return list;
    }
}
