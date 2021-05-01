package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocument.implementation.MethodSmellList;
import org.dddjava.jig.domain.model.jigmodel.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.dddjava.jig.domain.model.jigsource.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.parts.class_.method.Visibility;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void ビジネスルールの可視性(BusinessRuleService businessRuleService, Sources sources, JigSourceReadService jigSourceReadService) throws Exception {
        TypeFacts typeFacts = jigSourceReadService.readProjectData(sources);
        List<JigType> jigTypes = typeFacts.listJigTypes();

        JigType publicType = jigTypes.stream()
                .filter(businessRule -> businessRule.identifier().fullQualifiedName().endsWith("PublicType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(Visibility.PUBLIC, publicType.visibility());

        JigType protectedType = jigTypes.stream()
                .filter(businessRule -> businessRule.identifier().fullQualifiedName().endsWith("ProtectedType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(Visibility.PUBLIC, protectedType.visibility());

        JigType defaultType = jigTypes.stream()
                .filter(businessRule -> businessRule.identifier().fullQualifiedName().endsWith("DefaultType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(Visibility.NOT_PUBLIC, defaultType.visibility());

        JigType privateType = jigTypes.stream()
                .filter(businessRule -> businessRule.identifier().fullQualifiedName().endsWith("PrivateType"))
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