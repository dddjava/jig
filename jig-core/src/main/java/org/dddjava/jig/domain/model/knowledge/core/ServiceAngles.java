package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.outputs.RepositoryMethods;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public static ServiceAngles from(ServiceMethods serviceMethods, Entrypoint entrypoint, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            MethodDeclarations usingMethods = serviceMethod.usingMethods().methodDeclarations();

            MethodDeclarations userServiceMethods = serviceMethod.callerMethods().methodDeclarations().filter(methodDeclaration -> serviceMethods.contains(methodDeclaration));
            MethodDeclarations usingServiceMethods = usingMethods.filter(methodDeclaration -> serviceMethods.contains(methodDeclaration));
            RepositoryMethods usingRepositoryMethods = datasourceMethods.repositoryMethods().filter(usingMethods);
            ServiceAngle serviceAngle = new ServiceAngle(serviceMethod, usingRepositoryMethods, usingServiceMethods, entrypoint.collectEntrypointMethodOf(serviceMethod.callerMethods()), userServiceMethods);
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
