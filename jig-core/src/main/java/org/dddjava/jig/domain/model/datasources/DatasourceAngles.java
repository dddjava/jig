package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(List<DatasourceAngle> list) {
        this.list = list;
    }

    public static DatasourceAngles of(DatasourceAngleSource datasourceAngleSource) {
        List<DatasourceAngle> list = new ArrayList<>();
        for (MethodDeclaration methodDeclaration : datasourceAngleSource.getRepositoryMethods().list()) {
            list.add(DatasourceAngle.of(methodDeclaration, datasourceAngleSource.getMapperMethods(), datasourceAngleSource.getImplementationMethods(), datasourceAngleSource.getMethodRelations(), datasourceAngleSource.getAllSqls()));
        }
        return new DatasourceAngles(list);
    }

    public List<DatasourceAngle> list() {
        return list;
    }
}
