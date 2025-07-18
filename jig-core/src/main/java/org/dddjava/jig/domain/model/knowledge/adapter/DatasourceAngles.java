package org.dddjava.jig.domain.model.knowledge.adapter;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethod;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * データソースの切り口一覧
 */
public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(DatasourceMethods datasourceMethods, MyBatisStatements myBatisStatements, CallerMethodsFactory callerMethodsFactory) {
        List<DatasourceAngle> list = new ArrayList<>();
        for (DatasourceMethod datasourceMethod : datasourceMethods.list()) {
            CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(datasourceMethod.repositoryMethod().jigMethodId());
            list.add(new DatasourceAngle(datasourceMethod, myBatisStatements, callerMethods));
        }
        this.list = list;
    }

    public List<DatasourceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList();
    }
}
