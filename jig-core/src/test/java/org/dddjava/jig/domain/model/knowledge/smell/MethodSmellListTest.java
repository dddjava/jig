package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.JigDataProvider;
import org.junit.jupiter.api.Test;
import stub.domain.model.smell.SmelledClass;
import stub.domain.model.smell.SmelledRecord;
import testing.JigServiceTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@JigServiceTest
class MethodSmellListTest {

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

}