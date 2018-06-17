package org.dddjava.jig.presentation.view.poi.reporter;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.basic.ReportContext;
import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.domain.basic.ReportItemsFor;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.presentation.view.poi.report.ConvertibleItem;
import org.dddjava.jig.presentation.view.poi.report.Report;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Reporter<T> {

    String title;
    Class<T> angleClass;
    List<T> angles;

    public Reporter(String title, Class<T> angleClass, List<T> angles) {
        this.title = title;
        this.angleClass = angleClass;
        this.angles = angles;
    }

    public Report<?> toReport(GlossaryService glossaryService, TypeIdentifierFormatter typeIdentifierFormatter) {

        class ItemMethod {
            ReportItemFor reportItemFor;
            java.lang.reflect.Method method;

            public ItemMethod(ReportItemFor reportItemFor, Method method) {
                this.reportItemFor = reportItemFor;
                this.method = method;
            }
        }

        ConvertibleItem[] convertibleItems = Arrays.stream(angleClass.getMethods())
                .filter(method -> method.isAnnotationPresent(ReportItemFor.class) || method.isAnnotationPresent(ReportItemsFor.class))
                .flatMap(method -> {
                    // 複数アノテーションを展開
                    if (method.isAnnotationPresent(ReportItemsFor.class)) {
                        return Arrays.stream(method.getAnnotation(ReportItemsFor.class).value())
                                .map(reportItemFor -> new ItemMethod(reportItemFor, method));
                    }
                    // 1つだけのはそのまま
                    return Stream.of(new ItemMethod(method.getAnnotation(ReportItemFor.class), method));
                })
                .sorted(Comparator.comparing(itemMethod -> itemMethod.reportItemFor.order()))
                .map(itemMethod -> {
                    ReportItem item = itemMethod.reportItemFor.item();
                    return new ConvertibleItem() {
                        @Override
                        public String name() {
                            return item.name();
                        }

                        @Override
                        public String convert(Object row) {
                            try {
                                Object value = itemMethod.method.invoke(row);
                                ReportContext reportContext = new ReportContext(value, glossaryService, typeIdentifierFormatter);
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
