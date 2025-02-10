package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigTypesRepository;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import stub.domain.model.smell.SmelledClass;
import stub.domain.model.smell.SmelledRecord;
import testing.JigServiceTest;
import testing.TestSupport;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;


@JigServiceTest
class MethodSmellListTest {

    @Test
    void 注意メソッドの抽出(JigService jigService, JigTypesRepository jigTypesRepository) {
        MethodSmellList methodSmellList = jigService.methodSmells(jigTypesRepository);

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


    @MethodSource
    @ParameterizedTest(name = "{index} {1} :: {0}")
    void メンバ未使用の判定(Class<?> clz, String name, boolean expected) {
        var jigType = TestSupport.buildJigType(clz);
        MethodSmellList methodSmellList = new MethodSmellList(new JigTypes(List.of(jigType)));

        // smellListに入っていないものは警告なしの判定になるのでfilterとanyMatchで検証
        assertEquals(expected, methodSmellList.list().stream()
                .filter(methodSmell -> methodSmell.method().name().equals(name))
                .anyMatch(MethodSmell::notUseMember)
        );
    }

    static Stream<Arguments> メンバ未使用の判定() {
        return Stream.of(
                arguments(MemberUsageCheckNormalClass.class, "インスタンスフィールドを使用しているインスタンスメソッド", false),
                arguments(MemberUsageCheckNormalClass.class, "staticフィールドを使用しているインスタンスメソッド", false),
                arguments(MemberUsageCheckNormalClass.class, "何も使用していないインスタンスメソッド", true),
                arguments(MemberUsageCheckNormalClass.class, "他クラスのメソッドを使用しているがメンバを使用していないメソッド", true),
                arguments(MemberUsageCheckNormalClass.class, "インスタンスメソッドを使用しているインスタンスメソッド", false),
                arguments(MemberUsageCheckNormalClass.class, "staticメソッドを使用しているインスタンスメソッド", false),
                // staticメソッドは現在対象にしていない
                //arguments(MySut.class, "staticフィールドを使用しているstaticメソッド", true),
                //arguments(MySut.class, "何も使用していないstaticメソッド", true)
                // -- interface
                arguments(MemberUsageCheckInterface.class, "インタフェースのメソッド", false),
                arguments(MemberUsageCheckInterface.class, "インタフェースのdefaultメソッドでメンバを使用していない", true),
                arguments(MemberUsageCheckInterface.class, "インタフェースのdefaultメソッドでインタフェースのメソッドを使用している", false),
                // -- enum
                arguments(MemberUsageCheckEnum.class, "何も使用していないメソッド", true),
                arguments(MemberUsageCheckEnum.class, "列挙定数を使用しているメソッド", false),
                arguments(MemberUsageCheckEnum.class, "インスタンスフィールドを参照しているメソッド", false),
                // staticメソッドは現在対象にしていない
                //arguments(MemberUsageCheckEnum.class, "ファクトリメソッド1", false),
                //arguments(MemberUsageCheckEnum.class, "ファクトリメソッド2", false),
                // -- record
                arguments(MemberUsageCheckRecord.class, "componentA", false),
                arguments(MemberUsageCheckRecord.class, "何も使用していないメソッド", true),
                arguments(MemberUsageCheckRecord.class, "コンポーネントフィールドを呼び出しているメソッド", false),
                arguments(MemberUsageCheckRecord.class, "コンポーネントメソッドを呼び出しているメソッド", false),
                arguments(MemberUsageCheckRecord.class, "componentB", true)
        );
    }

    /**
     * record componentの判別によりrecordで生成されるaccessorが注意メソッドから除外できている。
     */
    @Test
    void 注意メソッドの抽出_record(JigService jigService, JigTypesRepository jigTypesRepository) {
        MethodSmellList methodSmellList = jigService.methodSmells(jigTypesRepository);

        var detectedSmells = methodSmellList.collectBy(TypeIdentifier.from(SmelledRecord.class));

        assertEquals(1, detectedSmells.size(), () -> detectedSmells.stream().map(methodSmell -> methodSmell.methodDeclaration().identifier()).toList().toString());

        assertTrue(extractMethod(detectedSmells, "returnsVoid").returnsVoid());
    }

}