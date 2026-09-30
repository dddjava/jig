package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inbound.InboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;

import java.util.Comparator;
import java.util.List;

/**
 * サービスの切り口一覧
 */
public record ServiceAngles(List<Usecase> list) {

    public static ServiceAngles from(ServiceMethods serviceMethods, InboundAdapters inboundAdapters, OutboundAdapters outboundAdapters) {
        return new ServiceAngles(serviceMethods.list().stream()
                .map(serviceMethod -> Usecase.from(serviceMethod, serviceMethods, inboundAdapters, outboundAdapters))
                .sorted(Comparator.comparing(Usecase::jigMethodId))
                .toList());
    }
}
