package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.members.instruction.InvokedMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.outputs.RepositoryMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public static ServiceAngles from(ServiceMethods serviceMethods, Entrypoints entrypoints, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            List<InvokedMethod> usingMethods = serviceMethod.usingMethods().invokedMethods();

            Collection<JigMethodIdentifier> userServiceMethods = serviceMethod.callerMethods().jigMethodIdentifiers(jigMethodIdentifier -> serviceMethods.contains(jigMethodIdentifier));
            Collection<InvokedMethod> usingServiceMethods = serviceMethod.usingMethods().invokedMethodStream()
                    .filter(invokedMethod-> serviceMethods.contains(invokedMethod.jigMethodIdentifier()))
                    .toList();
            RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
            ServiceAngle serviceAngle = new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, entrypoints.collectEntrypointMethodOf(serviceMethod.callerMethods()), userServiceMethods);
            list.add(serviceAngle);
        }
        return new ServiceAngles(list);
    }

    public List<ServiceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(serviceAngle -> serviceAngle.serviceMethod().method().jigMethodIdentifier().value()))
                .collect(Collectors.toList());
    }

    private ServiceAngles(List<ServiceAngle> list) {
        this.list = list;
    }

    public boolean none() {
        return list.isEmpty();
    }
}
