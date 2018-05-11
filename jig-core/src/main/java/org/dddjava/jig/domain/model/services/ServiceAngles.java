package org.dddjava.jig.domain.model.services;

import java.util.List;

public class ServiceAngles {

    List<ServiceAngle> list;

    public ServiceAngles(List<ServiceAngle> list) {
        this.list = list;
    }

    public List<ServiceAngle> list() {
        return list;
    }
}
