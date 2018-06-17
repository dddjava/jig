package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.basic.ReportContext;
import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.domain.basic.ReportItemsFor;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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

        class ItemMethod implements Comparable<ItemMethod> {
            ReportItemFor reportItemFor;
            java.lang.reflect.Method method;

            public ItemMethod(ReportItemFor reportItemFor, Method method) {
                this.reportItemFor = reportItemFor;
                this.method = method;
            }

            @Override
            public int compareTo(ItemMethod o) {
                int compare = Integer.compare(this.reportItemFor.order(), o.reportItemFor.order());
                if (compare != 0) {
                    return compare;
                }
                return reportItemFor.value().compareTo(o.reportItemFor.value());
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
                .sorted()
                .map(itemMethod -> {
                    ReportItem item = itemMethod.reportItemFor.value();
                    return new ConvertibleItem() {
                        @Override
                        public String name() {
                            String label = itemMethod.reportItemFor.label();
                            return label.isEmpty() ? item.name() : label;
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
