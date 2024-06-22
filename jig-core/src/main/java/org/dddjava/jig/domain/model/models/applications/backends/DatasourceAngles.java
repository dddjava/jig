package org.dddjava.jig.domain.model.models.applications.backends;

import org.dddjava.jig.domain.model.parts.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.rdbaccess.Sqls;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * データソースの切り口一覧
 */
public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(DatasourceMethods datasourceMethods, Sqls sqls, MethodRelations methodRelations) {
        List<DatasourceAngle> list = new ArrayList<>();
        for (DatasourceMethod datasourceMethod : datasourceMethods.list()) {
            CallerMethods callerMethods = methodRelations.callerMethodsOf(datasourceMethod.repositoryMethod().declaration());
            list.add(new DatasourceAngle(datasourceMethod, sqls, callerMethods));
        }
        this.list = list;
    }

    public List<DatasourceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.method().asFullNameText()))
                .collect(Collectors.toList());
    }
}
