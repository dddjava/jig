package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.architecture.ArchitectureBlock;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 判断の切り口一覧
 */
public class DecisionAngles {

    List<DecisionAngle> list;

    public DecisionAngles(TypeByteCodes typeByteCodes, Architecture architecture) {
        list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            ArchitectureBlock architectureBlock = architecture.layer(typeByteCode);

            for (MethodByteCode instanceMethodByteCode : typeByteCode.instanceMethodByteCodes()) {
                if (instanceMethodByteCode.decisionNumber().notZero()) {
                    list.add(new DecisionAngle(new Method(instanceMethodByteCode), architectureBlock));
                }
            }
        }
    }

    public List<DecisionAngle> filter(ArchitectureBlock architectureBlock) {
        return list.stream()
                .filter(decisionAngle -> decisionAngle.typeLayer() == architectureBlock)
                .collect(Collectors.toList());
    }
}
