package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportItemsFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class AngleReporter {

    String title;
    List<?> angles;
    Class<?> adapterClass;

    public AngleReporter(String title, Class<?> adapterClass, List<?> angles) {
        this.title = title;
        this.adapterClass = adapterClass;
        this.angles = angles;
    }

    public AngleReporter(Class<?> adapterClass, List<?> angles) {
        this(adapterClass.getAnnotation(ReportTitle.class).value(), adapterClass, angles);
    }

    public Report toReport(ConvertContext convertContext) {
        List<ReportItemMethod> reportItemMethods = Arrays.stream(adapterClass.getMethods())
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

        return new Report(title, angles, reportItemMethods, adapterClass, convertContext);
    }
}
