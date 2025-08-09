package org.dddjava.jig.domain.model.knowledge.adapter;

import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementation;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * データソースの切り口一覧
 */
public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(OutputImplementations outputImplementations, MyBatisStatements myBatisStatements, CallerMethodsFactory callerMethodsFactory) {
        List<DatasourceAngle> list = new ArrayList<>();
        for (OutputImplementation outputImplementation : outputImplementations.values()) {
            CallerMethods callerMethods = callerMethodsFactory.callerMethodsOf(outputImplementation.outputPortGateway().jigMethodId());
            list.add(new DatasourceAngle(outputImplementation, myBatisStatements, callerMethods));
        }
        this.list = list;
    }

    public List<DatasourceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.interfaceMethod().jigMethodId().value()))
                .toList();
    }
}
