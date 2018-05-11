package org.dddjava.jig.domain.model.datasources;

import java.util.List;

public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(List<DatasourceAngle> list) {
        this.list = list;
    }

    public List<DatasourceAngle> list() {
        return list;
    }
}
