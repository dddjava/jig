package org.dddjava.jig.domain.model.models.applications;

import org.dddjava.jig.domain.model.models.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.backends.RepositoryMethods;
import org.dddjava.jig.domain.model.models.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public List<ServiceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(serviceAngle -> serviceAngle.method().asFullNameText()))
                .collect(Collectors.toList());
    }

    public ServiceAngles(ServiceMethods serviceMethods, MethodRelations methodRelations, HandlerMethods handlerMethods, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            list.add(createServiceAngle(serviceMethod, methodRelations, handlerMethods, serviceMethods, datasourceMethods));
        }
        this.list = list;
    }

    private static ServiceAngle createServiceAngle(ServiceMethod serviceMethod, MethodRelations methodRelations, HandlerMethods handlerMethods, ServiceMethods serviceMethods, DatasourceMethods datasourceMethods) {
        MethodDeclarations usingMethods = serviceMethod.usingMethods().methodDeclarations();
        CallerMethods callerMethods = methodRelations.callerMethodsOf(serviceMethod.methodDeclaration());

        HandlerMethods userHandlerMethods = handlerMethods.filter(callerMethods);
        ServiceMethods userServiceMethods = serviceMethods.filter(callerMethods);
        ServiceMethods usingServiceMethods = serviceMethods.intersect(usingMethods);
        RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
        return new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, userHandlerMethods, userServiceMethods);
    }

    public boolean none() {
        return list.isEmpty();
    }
}
