package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.domain.model.report.ReportItemFor;
import org.dddjava.jig.domain.model.report.ReportItemsFor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AngleReporter {

    String title;
    Class<?> angleClass;
    List<?> angles;

    public <T> AngleReporter(String title, Class<T> angleClass, List<T> angles) {
        this.title = title;
        this.angleClass = angleClass;
        this.angles = angles;
    }

    public Report toReport(ConvertContext convertContext) {
        ItemConverter[] itemConverters = Arrays.stream(angleClass.getMethods())
                .filter(method -> method.isAnnotationPresent(ReportItemFor.class) || method.isAnnotationPresent(ReportItemsFor.class))
                .flatMap(method -> {
                    // 複数アノテーションがついていたら展開
                    if (method.isAnnotationPresent(ReportItemsFor.class)) {
                        return Arrays.stream(method.getAnnotation(ReportItemsFor.class).value())
                                .map(reportItemFor -> new ReportItemMethod(reportItemFor, method, convertContext));
                    }

                    // 1つだけのはそのまま
                    ReportItemFor reportItemFor = method.getAnnotation(ReportItemFor.class);
                    return Stream.of(new ReportItemMethod(reportItemFor, method, convertContext));
                })
                .sorted()
                .toArray(ItemConverter[]::new);

        return new Report(title, angles, itemConverters);
    }
}
