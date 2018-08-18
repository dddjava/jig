package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.poi.report.handler.Handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Report {

    String title;
    List<?> angles;

    private final List<ReportItemMethod> reportItemMethods;
    private final Object adapter;
    private final ConvertContext convertContext;

    public Report(String title, List<?> angles, List<ReportItemMethod> reportItemMethods, Object adapter, ConvertContext convertContext) {
        this.title = title;
        this.angles = angles;
        this.reportItemMethods = reportItemMethods;
        this.adapter = adapter;
        this.convertContext = convertContext;
    }

    public String title() {
        return title;
    }

    public ReportRow headerRow() {
        return new ReportRow(reportItemMethods.stream().map(ReportItemMethod::label).collect(toList()));
    }

    public List<ReportRow> rows() {
        return angles.stream()
                .map(row -> {
                    List<String> convertedRow = reportItemMethods.stream()
                            .map(reportItemMethod -> convert(reportItemMethod, row))
                            .collect(toList());
                    return new ReportRow(convertedRow);
                })
                .collect(toList());
    }

    private String convert(ReportItemMethod reportItemMethod, Object angle) {
        Handlers handlers = new Handlers(convertContext);
        try {
            Object item = reportItemMethod.method.invoke(adapter, angle);

            return handlers.handle(reportItemMethod.reportItemFor.value(), item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("実装ミス", e);
        }
    }
}
