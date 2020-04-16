package org.dddjava.jig.domain.model.jigmodel.applications.services;

import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigmodel.applications.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.repositories.DatasourceMethods;

import java.util.ArrayList;
import java.util.List;

/**
 * サービスの切り口一覧
 */
public class ServiceAngles {

    List<ServiceAngle> list;

    public List<ServiceAngle> list() {
        return list;
    }

    public ServiceAngles(ServiceMethods serviceMethods, MethodRelations methodRelations, ControllerMethods controllerMethods, DatasourceMethods datasourceMethods) {
        List<ServiceAngle> list = new ArrayList<>();
        for (ServiceMethod serviceMethod : serviceMethods.list()) {
            list.add(new ServiceAngle(serviceMethod, methodRelations, controllerMethods, serviceMethods, datasourceMethods));
        }
        this.list = list;
    }
}
