package jig.domain.model.report;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ReportRow {

    List<String> list;

    public ReportRow(List<String> list) {
        this.list = list;
    }

    public static Collector<String, ?, ReportRow> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), ReportRow::new);
    }

    public List<String> list() {
        return list;
    }
}
