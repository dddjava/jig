package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocument.implementation.MethodSmellList;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void ビジネスルールの可視性(BusinessRuleService businessRuleService, Sources sources, JigSourceReadService jigSourceReadService) throws Exception {
        jigSourceReadService.readProjectData(sources);
        BusinessRules businessRules = businessRuleService.businessRules();

        BusinessRule publicType = businessRules.list().stream()
                .filter(businessRule -> businessRule.typeIdentifier().fullQualifiedName().endsWith("PublicType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(Visibility.PUBLIC, publicType.visibility());

        BusinessRule protectedType = businessRules.list().stream()
                .filter(businessRule -> businessRule.typeIdentifier().fullQualifiedName().endsWith("ProtectedType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(Visibility.PUBLIC, protectedType.visibility());

        BusinessRule defaultType = businessRules.list().stream()
                .filter(businessRule -> businessRule.typeIdentifier().fullQualifiedName().endsWith("DefaultType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(Visibility.NOT_PUBLIC, defaultType.visibility());

        BusinessRule privateType = businessRules.list().stream()
                .filter(businessRule -> businessRule.typeIdentifier().fullQualifiedName().endsWith("PrivateType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(Visibility.NOT_PUBLIC, privateType.visibility());
    }

    @Test
    void 注意メソッドの抽出(BusinessRuleService businessRuleService, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
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