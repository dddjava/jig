package jig.domain.model.report;

import java.util.Arrays;
import java.util.List;

public class ReportRow {

    List<String> list;

    private ReportRow(List<String> list) {
        this.list = list;
    }

    public List<String> list() {
        return list;
    }

    public static ReportRow of(String... cols) {
        return new ReportRow(Arrays.asList(cols));
    }
}
