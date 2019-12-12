package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.jigloaded.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.jigloaded.architecture.Architecture;
import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 判断の切り口一覧
 */
public class DecisionAngles {

    TypeByteCodes typeByteCodes;
    Architecture architecture;

    public DecisionAngles(TypeByteCodes typeByteCodes, Architecture architecture) {
        this.typeByteCodes = typeByteCodes;
        this.architecture = architecture;
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
        return toDecisionAngleList(ApplicationLayer.APPLICATION.filter(typeByteCodes, architecture));
    }

    public List<DecisionAngle> listPresentations() {
        return toDecisionAngleList(ApplicationLayer.PRESENTATION.filter(typeByteCodes, architecture));
    }

    public List<DecisionAngle> listInfrastructures() {
        return toDecisionAngleList(ApplicationLayer.INFRASTRUCTURE.filter(typeByteCodes, architecture));
    }
}
