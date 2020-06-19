package org.dddjava.jig.presentation.view.poi.report;

import org.apache.poi.ss.usermodel.Sheet;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.MethodWorry;
import org.dddjava.jig.presentation.view.poi.report.formatter.ReportItemFormatters;
import org.dddjava.jig.presentation.view.report.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        this(title, pivotModels, modelReporter, collectReportItemMethods(reportClass)
        );
    }

    private static <REPORT> List<ReportItemMethod> collectReportItemMethods(Class<REPORT> reportClass) {
        Stream<ReportItemMethod> items = Arrays.stream(reportClass.getMethods())
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
                });
        Stream<ReportItemMethod> methodWorries = Arrays.stream(reportClass.getMethods())
                .filter(method -> method.isAnnotationPresent(ReportMethodWorryOf.class))
                .map(method -> new ReportItemMethod(method, generateReportItemForInstance(method)));
        return Stream.concat(items, methodWorries)
                .sorted()
                .collect(toList());
    }

    private static ReportItemFor generateReportItemForInstance(Method method) {
        ReportMethodWorryOf reportMethodWorryOf = method.getAnnotation(ReportMethodWorryOf.class);
        MethodWorry value = reportMethodWorryOf.value();
        return new ReportItemFor() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ReportItemFor.class;
            }

            @Override
            public ReportItem value() {
                return ReportItem.汎用真偽値;
            }

            @Override
            public int order() {
                // とりあえず後ろにしておく
                return 100 + value.ordinal();
            }

            @Override
            public String label() {
                return value.toString();
            }
        };
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

    private String convert(ReportItemMethod reportItemMethod, MODEL angle, ReportItemFormatters reportItemFormatters) {
        try {
            Object report = modelReporter.report(angle);

            Object item = reportItemMethod.method.invoke(report);
            return reportItemFormatters.format(reportItemMethod.reportItemFor.value(), item);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("実装ミス", e);
        }
    }

    public void apply(Sheet sheet, ReportItemFormatters reportItemFormatters) {
        List<ReportRow> rows = pivotModels.stream()
                .map(row1 -> {
                    List<String> convertedRow = reportItemMethods.stream()
                            .map(reportItemMethod -> convert(reportItemMethod, row1, reportItemFormatters))
                            .collect(toList());
                    return new ReportRow(convertedRow);
                })
                .collect(toList());

        for (ReportRow row : rows) {
            row.writeRow(sheet.createRow(sheet.getLastRowNum() + 1));
        }
    }
}
