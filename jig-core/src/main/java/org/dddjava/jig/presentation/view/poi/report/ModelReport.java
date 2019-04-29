package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.poi.report.formatter.ReportItemFormatters;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportItemsFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelReport<MODEL> {

    String title;
    List<MODEL> pivotModels;
    ModelReporter<?, MODEL> modelReporter;
    List<ReportItemMethod> reportItemMethods;

    public <REPORT> ModelReport(List<MODEL> pivotModels, ModelReporter<REPORT, MODEL> modelReporter, Class<REPORT> reportClass) {
        this(reportClass.getAnnotation(ReportTitle.class).value(), pivotModels, modelReporter, reportClass);
    }

    public <REPORT> ModelReport(String title, List<MODEL> pivotModels, ModelReporter<REPORT, MODEL> modelReporter, Class<REPORT> reportClass) {
        this(title, pivotModels, modelReporter,
                Arrays.stream(reportClass.getMethods())
                        .filter(method -> method.isAnnotationPresent(ReportItemFor.class) || method.isAnnotationPresent(ReportItemsFor.class))
                        .flatMap(method -> {
                            // 複数アノテーションがついていたら展開
                            if (method.isAnnotationPresent(ReportItemsFor.class)) {
                                return Arrays.stream(method.getAnnotation(ReportItemsFor.class).value())
                                        .map(reportItemFor -> new ReportItemMethod(method, reportItemFor));
                            }

                            // 1つだけのはそのまま
                            ReportItemFor reportItemFor = method.getAnnotation(ReportItemFor.class);
                            return Stream.of(new ReportItemMethod(method, reportItemFor));
                        })
                        .sorted()
                        .collect(toList())
        );
    }

    private ModelReport(String title, List<MODEL> pivotModels, ModelReporter<?, MODEL> modelReporter, List<ReportItemMethod> reportItemMethods) {
        this.title = title;
        this.pivotModels = pivotModels;
        this.modelReporter = modelReporter;
        this.reportItemMethods = reportItemMethods;
    }

    public String title() {
        return title;
    }

    public Header header() {
        return new Header(reportItemMethods);
    }

    public List<ReportRow> rows(ConvertContext convertContext) {
        ReportItemFormatters reportItemFormatters = new ReportItemFormatters(convertContext);
        return pivotModels.stream()
                .map(row -> {
                    List<String> convertedRow = reportItemMethods.stream()
                            .map(reportItemMethod -> convert(reportItemMethod, row, reportItemFormatters))
                            .collect(toList());
                    return new ReportRow(convertedRow);
                })
                .collect(toList());
    }

    private String convert(ReportItemMethod reportItemMethod, MODEL angle, ReportItemFormatters reportItemFormatters) {
        try {
            Object report = modelReporter.report(angle);

            Object item = reportItemMethod.method.invoke(report);
            return reportItemFormatters.format(reportItemMethod.reportItemFor.value(), item);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("実装ミス", e);
        }
    }
}
