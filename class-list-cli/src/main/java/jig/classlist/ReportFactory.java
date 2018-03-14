package jig.classlist;

import java.util.List;

import static java.util.stream.Collectors.toList;

public interface ReportFactory<T> {

    List<String> headerLabel();

    List<String> row(T param);

    default List<List<String>> rowList() {
        return list().stream().map(this::row).collect(toList());
    }

    List<T> list();
}
