package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.information.domains.businessrules.MethodSmell;
import org.dddjava.jig.domain.model.information.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.junit.jupiter.api.Test;
import stub.domain.model.smell.SmelledClass;
import stub.domain.model.smell.SmelledRecord;
import testing.JigServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

        var detectedSmells = methodSmellList.collectBy(TypeIdentifier.from(SmelledClass.class));

        assertEquals(9, detectedSmells.size(), () -> detectedSmells.stream().map(methodSmell -> methodSmell.methodDeclaration().identifier()).toList().toString());

        assertTrue(extractMethod(detectedSmells, "returnVoid").returnsVoid());

        assertTrue(extractMethod(detectedSmells, "returnInt").primitiveInterface());
        assertTrue(extractMethod(detectedSmells, "returnLong").primitiveInterface());

        assertTrue(extractMethod(detectedSmells, "returnBoolean").returnsBoolean());

        assertTrue(extractMethod(detectedSmells, "intParameter").primitiveInterface());
        assertTrue(extractMethod(detectedSmells, "longParameter").primitiveInterface());
        assertTrue(extractMethod(detectedSmells, "booleanParameter").primitiveInterface());

        assertTrue(extractMethod(detectedSmells, "useNullLiteral").referenceNull());
        assertTrue(extractMethod(detectedSmells, "judgeNull").nullDecision());
    }

    private static MethodSmell extractMethod(List<MethodSmell> detectedSmells, String methodName) {
        return detectedSmells.stream().filter(methodSmell -> methodSmell.methodDeclaration().identifier().methodSignature().methodName().equals(methodName)).findAny().orElseThrow();
    }

    /**
     * record componentの判別によりrecordで生成されるaccessorが注意メソッドから除外できている。
     */
    @Test
    void 注意メソッドの抽出_record(JigService businessRuleService, Sources sources, JigSourceReader jigSourceReader) {
        var jigSource = jigSourceReader.readProjectData(sources);
        MethodSmellList methodSmellList = businessRuleService.methodSmells(jigSource);

        var detectedSmells = methodSmellList.collectBy(TypeIdentifier.from(SmelledRecord.class));

        assertEquals(1, detectedSmells.size(), () -> detectedSmells.stream().map(methodSmell -> methodSmell.methodDeclaration().identifier()).toList().toString());

        assertTrue(extractMethod(detectedSmells, "returnsVoid").returnsVoid());
    }

    @Test
    void アノテーションつきのpackage_infoをビジネスルールとして扱わない(JigService businessRuleService, Sources sources, JigSourceReader jigSourceReader) {
        var jigSource = jigSourceReader.readProjectData(sources);
        assertFalse(businessRuleService.businessRules(jigSource).contains(TypeIdentifier.valueOf("stub.domain.model.annotation.package-info")));
    }
}
