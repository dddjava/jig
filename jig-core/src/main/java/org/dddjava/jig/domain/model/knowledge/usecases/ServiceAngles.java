package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inbound.InputAdapters;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * サービスの切り口一覧
 */
public record ServiceAngles(Collection<Entry> entries) {
    private record Entry(JigType jigType, Collection<Usecase> usecases) {
    }

    public static ServiceAngles from(ServiceMethods serviceMethods, InputAdapters inputAdapters, OutboundAdapters outboundAdapters) {

        return new ServiceAngles(serviceMethods
                .streamAndMap((jigType, serviceMethodList) -> {
                    var serviceAngleList = serviceMethodList.stream()
                            .map(serviceMethod -> Usecase.from(serviceMethod, serviceMethods, inputAdapters, outboundAdapters))
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
}
