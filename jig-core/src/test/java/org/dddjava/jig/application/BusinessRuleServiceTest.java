package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeVisibility;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmell;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;
import org.junit.jupiter.api.Test;
import stub.domain.model.relation.ClassDefinition;
import stub.domain.model.smell.SmelledClass;
import stub.domain.model.smell.SmelledRecord;
import testing.JigServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JigServiceTest
class BusinessRuleServiceTest {

    @Test
    void クラス可視性の判定(JigDataProvider jigsource) throws Exception {
        List<JigType> jigTypes = jigsource.fetchJigTypes().list();

        JigType publicType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("PublicType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(TypeVisibility.PUBLIC, publicType.visibility());

        JigType protectedType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("ProtectedType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(TypeVisibility.PUBLIC, protectedType.visibility());

        JigType defaultType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("DefaultType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(TypeVisibility.NOT_PUBLIC, defaultType.visibility());

        JigType privateType = jigTypes.stream()
                .filter(jigType -> jigType.identifier().fullQualifiedName().endsWith("PrivateType"))
                .findAny().orElseThrow(AssertionError::new);
        assertEquals(TypeVisibility.NOT_PUBLIC, privateType.visibility());
    }

    @Test
    void 注意メソッドの抽出(JigService jigService, JigDataProvider jigDataProvider) {
        MethodSmellList methodSmellList = jigService.methodSmells(jigDataProvider);

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
    void 注意メソッドの抽出_record(JigService jigService, JigDataProvider jigDataProvider) {
        MethodSmellList methodSmellList = jigService.methodSmells(jigDataProvider);

        var detectedSmells = methodSmellList.collectBy(TypeIdentifier.from(SmelledRecord.class));

        assertEquals(1, detectedSmells.size(), () -> detectedSmells.stream().map(methodSmell -> methodSmell.methodDeclaration().identifier()).toList().toString());

        assertTrue(extractMethod(detectedSmells, "returnsVoid").returnsVoid());
    }

    /**
     * @see stub.domain.model.annotation の package-info.java にはアノテーションをつけている
     */
    @Test
    void アノテーションつきのpackage_infoをドメインとして扱わない(JigService jigService, JigDataProvider jigDataProvider) {
        var typeIdentifier = TypeIdentifier.valueOf("stub.domain.model.annotation.package-info");

        var jigTypes = jigService.jigTypes(jigDataProvider);

        assertTrue(jigTypes.resolveJigType(typeIdentifier).isPresent(), "JigTypeには存在する");

        var coreDomainJigTypes = jigService.coreDomainJigTypes(jigDataProvider);
        assertFalse(coreDomainJigTypes.contains(typeIdentifier), "domain coreには存在しない");
    }

    @Test
    void 関連(JigService jigService, JigDataProvider jigDataProvider) {
        var jigTypes = jigService.jigTypes(jigDataProvider);

        var targetJigType = jigTypes.resolveJigType(TypeIdentifier.from(ClassDefinition.class)).orElseThrow();
        var classRelations = jigTypes.internalTypeRelationsFrom(targetJigType);

        assertEquals(classRelations.dotText(), """
                "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.ClassAnnotation";
                "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.GenericsParameter";
                "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.ImplementA";
                "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.ImplementB";
                "stub.domain.model.relation.ClassDefinition" -> "stub.domain.model.relation.clz.SuperClass";""");
    }
}
