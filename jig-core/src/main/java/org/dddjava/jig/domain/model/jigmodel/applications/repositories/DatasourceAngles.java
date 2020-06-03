package org.dddjava.jig.domain.model.jigmodel.applications.repositories;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.rdbaccess.Sqls;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * データソースの切り口一覧
 */
public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(DatasourceMethods datasourceMethods, Sqls sqls) {
        List<DatasourceAngle> list = new ArrayList<>();
        for (DatasourceMethod datasourceMethod : datasourceMethods.list()) {
            list.add(new DatasourceAngle(datasourceMethod, sqls));
        }
        this.list = list;
    }

    public List<DatasourceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.method().asFullNameText()))
                .collect(Collectors.toList());
    }
}
