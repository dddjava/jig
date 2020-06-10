package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocument.implementation.MethodSmellList;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void 注意メソッドの抽出(BusinessRuleService businessRuleService, Sources sources, ImplementationService implementationService) {
        implementationService.readProjectData(sources);
        MethodSmellList methodSmellList = businessRuleService.methodSmells();

        assertThat(methodSmellList.list())
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