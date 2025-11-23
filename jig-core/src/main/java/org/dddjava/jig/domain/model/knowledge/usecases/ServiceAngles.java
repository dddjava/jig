package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * サービスの切り口一覧
 */
public record ServiceAngles(Collection<Entry> entries) {
    private record Entry(JigType jigType, Collection<Usecase> usecases) {
    }

    public static ServiceAngles from(ServiceMethods serviceMethods, InputAdapters inputAdapters, OutputImplementations outputImplementations) {

        return new ServiceAngles(serviceMethods
                .streamAndMap((jigType, serviceMethodList) -> {
                    var serviceAngleList = serviceMethodList.stream()
                            .map(serviceMethod -> Usecase.from(serviceMethod, serviceMethods, inputAdapters, outputImplementations))
                            .toList();
                    return new Entry(jigType, serviceAngleList);
                }).toList());
    }

    public List<Usecase> list() {
        return entries.stream()
                .map(Entry::usecases)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(usecase -> usecase.jigMethodId()))
                .toList();
    }

    public <T> Stream<T> streamAndMap(BiFunction<JigType, Collection<Usecase>, T> biFunction) {
        return entries.stream().map(entry -> biFunction.apply(entry.jigType, entry.usecases));
    }
}
