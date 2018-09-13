package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.poi.report.handler.Handlers;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportItemsFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class AngleReporter<MODEL> {

    String title;
    List<MODEL> angles;
    Class<?> adapterClass;
    private List<ReportItemMethod> reportItemMethods;

    ReporterProvider<?, MODEL> reporterProvider;

    public AngleReporter(String title, Class<?> adapterClass, List<MODEL> angles) {
        this(title, angles, adapterClass,
                Arrays.stream(adapterClass.getMethods())
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
                        .collect(toList()),
                model -> {
                    // TODO 後方互換
                    try {
                        return Arrays.stream(adapterClass.getDeclaredConstructors())
                                .filter(constructor -> constructor.getParameterCount() == 1)
                                .filter(constructor -> constructor.getParameterTypes()[0] == model.getClass())
                                .findAny()
                                .orElseThrow(() -> new RuntimeException("angleを引数にとるコンストラクタが必要です"))
                                .newInstance(model);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    public AngleReporter(String title, List<MODEL> angles, Class<?> adapterClass, List<ReportItemMethod> reportItemMethods, ReporterProvider<?, MODEL> reporterProvider) {
        this.title = title;
        this.angles = angles;
        this.adapterClass = adapterClass;
        this.reportItemMethods = reportItemMethods;
        this.reporterProvider = reporterProvider;
    }

    public AngleReporter(Class<?> adapterClass, List<MODEL> angles) {
        this(adapterClass.getAnnotation(ReportTitle.class).value(), adapterClass, angles);
    }

    public <REPORT> AngleReporter(List<MODEL> angles, ReporterProvider<REPORT, MODEL> reporterProvider, Class<REPORT> reportClass) {
        this(reportClass.getAnnotation(ReportTitle.class).value(), angles, reportClass,
                // FIXME WET
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
                        .collect(toList()),
                reporterProvider);
    }

    public String title() {
        return title;
    }

    public ReportRow headerRow() {
        return new ReportRow(reportItemMethods.stream().map(ReportItemMethod::label).collect(toList()));
    }

    public List<ReportRow> rows(ConvertContext convertContext) {
        Handlers handlers = new Handlers(convertContext);
        return angles.stream()
                .map(row -> {
                    List<String> convertedRow = reportItemMethods.stream()
                            .map(reportItemMethod -> convert(reportItemMethod, row, handlers))
                            .collect(toList());
                    return new ReportRow(convertedRow);
                })
                .collect(toList());
    }

    private String convert(ReportItemMethod reportItemMethod, MODEL angle, Handlers handlers) {
        try {
            Object report = reporterProvider.report(angle);

            Object item = reportItemMethod.method.invoke(report);
            return handlers.handle(reportItemMethod.reportItemFor.value(), item);

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("実装ミス", e);
        }
    }
}
