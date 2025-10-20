package org.dddjava.jig.domain.model.knowledge.datasource;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.Comparator;
import java.util.List;

/**
 * データソースの切り口一覧
 */
public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(OutputImplementations outputImplementations, MyBatisStatements myBatisStatements, CallerMethodsFactory callerMethodsFactory) {
        this.list = outputImplementations.stream()
                .map(outputImplementation -> {
                    CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortGateway().jigMethodId());
                    return new DatasourceAngle(outputImplementation, myBatisStatements, callerMethods);
                })
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList();
    }

    public List<DatasourceAngle> list() {
        return list;
    }
}
