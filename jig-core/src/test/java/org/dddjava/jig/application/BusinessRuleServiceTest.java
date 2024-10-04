package org.dddjava.jig.application;

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
    void ビジネスルールの可視性(JigService businessRuleService, Sources sources, JigSourceReader jigSourceReader) throws Exception {
        TypeFacts typeFacts = jigSourceReader.readProjectData(sources).typeFacts();
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
    void 注意メソッドの抽出(JigService businessRuleService, Sources sources, JigSourceReader jigSourceReader) {
        var jigSource = jigSourceReader.readProjectData(sources);
        MethodSmellList methodSmellList = businessRuleService.methodSmells(jigSource);

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
    void アノテーションつきのpackage_infoをビジネスルールとして扱わない(JigService businessRuleService, Sources sources, JigSourceReader jigSourceReader) {
        var jigSource = jigSourceReader.readProjectData(sources);
        assertFalse(businessRuleService.businessRules(jigSource).contains(new TypeIdentifier("stub.domain.model.annotation.package-info")));
    }
}