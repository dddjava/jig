package org.dddjava.jig.domain.model.repositories;

import org.dddjava.jig.domain.model.fact.datasource.Sqls;

import java.util.ArrayList;
import java.util.List;

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
        return list;
    }
}
