package jig.classlist.classlist;

import jig.classlist.ReportFactory;

import java.util.List;

public class TypeReportFactory implements ReportFactory<TypeListNavigator> {

    private final TypeListType methodListType;
    private final List<TypeListNavigator> list;

    public TypeReportFactory(TypeListType methodListType, List<TypeListNavigator> list) {
        this.methodListType = methodListType;
        this.list = list;
    }

    @Override
    public List<String> headerLabel() {
        return methodListType.headerLabel();
    }

    @Override
    public List<String> row(TypeListNavigator param) {
        return methodListType.row(param);
    }

    @Override
    public List<TypeListNavigator> list() {
        return list;
    }
}
