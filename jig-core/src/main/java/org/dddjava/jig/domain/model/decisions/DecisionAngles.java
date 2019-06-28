package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.richmethod.Method;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 判断の切り口一覧
 */
public class DecisionAngles {

    TypeByteCodes typeByteCodes;

    public DecisionAngles(TypeByteCodes typeByteCodes, Architecture architecture) {
        this.typeByteCodes = typeByteCodes;
    }

    List<DecisionAngle> toDecisionAngleList(TypeByteCodes list) {
        return list.list().stream()
                .flatMap(typeByteCode -> typeByteCode.instanceMethodByteCodes().stream())
                .filter(methodByteCode -> methodByteCode.decisionNumber().notZero())
                .map(Method::new)
                .map(DecisionAngle::new)
                .collect(Collectors.toList());
    }

    public List<DecisionAngle> listApplications() {
        return toDecisionAngleList(ApplicationLayer.APPLICATION.filter(typeByteCodes));
    }

    public List<DecisionAngle> listPresentations() {
        return toDecisionAngleList(ApplicationLayer.PRESENTATION.filter(typeByteCodes));
    }

    public List<DecisionAngle> listInfrastructures() {
        return toDecisionAngleList(ApplicationLayer.INFRASTRUCTURE.filter(typeByteCodes));
    }
}
