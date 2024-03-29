package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void ビジネスルールの可視性(BusinessRuleService businessRuleService, Sources sources, JigSourceReadService jigSourceReadService) throws Exception {
        TypeFacts typeFacts = jigSourceReadService.readProjectData(sources);
        List<JigType> jigTypes = typeFacts.jigTypes().list();

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

    @Test
    void アノテーションつきのpackage_infoをビジネスルールとして扱わない(BusinessRuleService businessRuleService, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
        assertFalse(businessRuleService.businessRules().contains(new TypeIdentifier("stub.domain.model.annotation.package-info")));
    }
}
