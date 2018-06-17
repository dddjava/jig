package org.dddjava.jig.presentation.view.poi.report;

import java.util.Arrays;
import java.util.List;

public class AngleReporters {

    List<AngleReporter> list;

    public AngleReporters(AngleReporter... reporters) {
        this.list = Arrays.asList(reporters);
    }

    public List<AngleReporter> list() {
        return list;
    }
}
