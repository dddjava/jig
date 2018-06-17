package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.poi.reporter.Reporter;

import java.util.List;

public class Reports {

    List<Reporter<?>> list;

    public Reports(List<Reporter<?>> list) {
        this.list = list;
    }

    public List<Reporter<?>> list() {
        return list;
    }
}
