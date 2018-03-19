package jig.domain.model.report;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Reports {

    List<Report> list;

    public Reports(Report... reports) {
        this.list = Arrays.asList(reports);
    }

    public void each(Consumer<Report> consumer) {
        list.forEach(consumer);
    }
}
