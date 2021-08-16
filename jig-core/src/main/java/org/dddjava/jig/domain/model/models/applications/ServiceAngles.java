package org.dddjava.jig.domain.model.models.applications;

import org.dddjava.jig.domain.model.models.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.backends.RepositoryMethods;
import org.dddjava.jig.domain.model.models.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public static ServiceAngles from(ServiceMethods serviceMethods, HandlerMethods handlerMethods, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            MethodDeclarations usingMethods = serviceMethod.usingMethods().methodDeclarations();

            HandlerMethods userHandlerMethods = handlerMethods.filter(serviceMethod.callerMethods());
            ServiceMethods userServiceMethods = serviceMethods.filter(serviceMethod.callerMethods());
            MethodDeclarations usingServiceMethods = usingMethods.filter(methodDeclaration -> serviceMethods.contains(methodDeclaration));
            RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
            ServiceAngle serviceAngle = new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, userHandlerMethods, userServiceMethods);
            list.add(serviceAngle);
        }
        return new ServiceAngles(list);
    }

    public List<ServiceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(serviceAngle -> serviceAngle.method().asFullNameText()))
                .collect(Collectors.toList());
    }

    private ServiceAngles(List<ServiceAngle> list) {
        this.list = list;
    }

    public boolean none() {
        return list.isEmpty();
    }
}
