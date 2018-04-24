package jig.domain.model.report;

import java.util.List;
import java.util.function.Consumer;

public class Reports {

    List<Report<?>> list;

    public Reports(List<Report<?>> list) {
        this.list = list;
    }

    public void each(Consumer<Report<?>> consumer) {
        list.forEach(consumer);
    }
}
