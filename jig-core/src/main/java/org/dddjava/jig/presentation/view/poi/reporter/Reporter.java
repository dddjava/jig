package org.dddjava.jig.presentation.view.poi.reporter;

import org.dddjava.jig.domain.basic.ReportContext;
import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.presentation.view.poi.report.ConvertibleItem;
import org.dddjava.jig.presentation.view.poi.report.Report;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Reporter<T> {

    String title;
    Class<T> angleClass;
    List<T> angles;

    public Reporter(String title, Class<T> angleClass, List<T> angles) {
        this.title = title;
        this.angleClass = angleClass;
        this.angles = angles;
    }

    public Report<?> toReport() {
        ConvertibleItem[] convertibleItems = Arrays.stream(angleClass.getMethods())
                .filter(method -> method.isAnnotationPresent(ReportItemFor.class))
                .sorted(Comparator.comparing(method -> {
                    ReportItemFor annotation = method.getAnnotation(ReportItemFor.class);
                    return annotation.order();
                }))
                .map(itemMethod -> {
                    ReportItemFor annotation = itemMethod.getAnnotation(ReportItemFor.class);
                    ReportItem item = annotation.item();
                    return new ConvertibleItem() {
                        @Override
                        public String name() {
                            return item.name();
                        }

                        @Override
                        public String convert(Object row) {
                            try {
                                Object value = itemMethod.invoke(row);
                                ReportContext reportContext = new ReportContext(value);
                                return item.convert(reportContext);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException("実装ミス");
                            }
                        }
                    };
                })
                .toArray(ConvertibleItem[]::new);

        return new Report<>(title, angles, convertibleItems);
    }
}
