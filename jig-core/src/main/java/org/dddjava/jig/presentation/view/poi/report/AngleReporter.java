package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.poi.report.handler.Handlers;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportItemsFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class AngleReporter {

    String title;
    List<?> angles;
    Class<?> adapterClass;
    private List<ReportItemMethod> reportItemMethods;

    public AngleReporter(String title, Class<?> adapterClass, List<?> angles) {
        this.title = title;
        this.adapterClass = adapterClass;
        this.angles = angles;

        reportItemMethods = Arrays.stream(adapterClass.getMethods())
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
                .collect(toList());
    }

    public AngleReporter(Class<?> adapterClass, List<?> angles) {
        this(adapterClass.getAnnotation(ReportTitle.class).value(), adapterClass, angles);
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

    private String convert(ReportItemMethod reportItemMethod, Object angle, Handlers handlers) {
        try {
            // TODO angleを受け取るコンストラクタを識別する
            Constructor<?> constructor = adapterClass.getDeclaredConstructor();
            Object adapter = constructor.newInstance();


            Object item = reportItemMethod.method.invoke(adapter, angle);

            return handlers.handle(reportItemMethod.reportItemFor.value(), item);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            throw new RuntimeException("実装ミス", e);
        }
    }
}
