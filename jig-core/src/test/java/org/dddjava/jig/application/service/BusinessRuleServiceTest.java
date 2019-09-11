package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.smells.MethodSmellAngles;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void 注意メソッドの抽出(BusinessRuleService businessRuleService, AnalyzedImplementation analyzedImplementation) {
        MethodSmellAngles methodSmellAngles = businessRuleService.methodSmells(analyzedImplementation);

        assertThat(methodSmellAngles.list())
                .filteredOn(methodSmellAngle -> methodSmellAngle.methodDeclaration().declaringType()
                        .fullQualifiedName().equals("stub.domain.model.smell.SmellMethods"))
                .extracting(methodSmellAngle -> methodSmellAngle.methodDeclaration().identifier().methodSignature().methodName())
                .contains(
                        "returnVoid",
                        "returnInt",
                        "longParameter",
                        "judgeNull"
                );
    }
}