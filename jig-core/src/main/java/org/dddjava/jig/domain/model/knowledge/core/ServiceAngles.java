package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * サービスの切り口一覧
 */
public record ServiceAngles(Collection<Entry> entries) {
    private record Entry(JigType jigType, List<ServiceAngle> serviceAngleList) {
    }

    public static ServiceAngles from(ServiceMethods serviceMethods, Entrypoints entrypoints, DatasourceMethods datasourceMethods) {

        return new ServiceAngles(serviceMethods
                .streamAndMap((jigType, serviceMethodList) -> {
                    var serviceAngleList = serviceMethodList.stream()
                            .map(serviceMethod -> ServiceAngle.from(serviceMethod, serviceMethods, entrypoints, datasourceMethods))
                            .toList();
                    return new Entry(jigType, serviceAngleList);
                }).toList());
    }

    public List<ServiceAngle> list() {
        return entries.stream().map(Entry::serviceAngleList).flatMap(List::stream).toList();
    }

    public <T> Stream<T> streamAndMap(BiFunction<JigType, List<ServiceAngle>, T> biFunction) {
        return entries.stream().map(entry -> biFunction.apply(entry.jigType, entry.serviceAngleList));
    }
}
