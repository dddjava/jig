package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public static ServiceAngles from(ServiceMethods serviceMethods, Entrypoints entrypoints, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            ServiceAngle serviceAngle = ServiceAngle.from(serviceMethods, entrypoints, datasourceMethods, serviceMethod);
            list.add(serviceAngle);
        }
        return new ServiceAngles(list);
    }

    public List<ServiceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(serviceAngle -> serviceAngle.serviceMethod().method().jigMethodId().value()))
                .toList();
    }

    private ServiceAngles(List<ServiceAngle> list) {
        this.list = list;
    }

    public boolean none() {
        return list.isEmpty();
    }
}
