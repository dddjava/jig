package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * issue #1099 の再現テスト
 *
 * AsmMethodVisitor#jigMethodHeader の判定が {@code name.equals("$values()")}
 * （括弧付き）になっているため、バイトコード上の実際のメソッド名 {@code $values}
 * （括弧なし）と一致せず、ENUM_SUPPORT フラグが付与されない。
 */
class JigMethodEnumSupportFlagTest {

    private enum SampleEnum {
        A, B
    }

    @Test
    void enumのvaluesメソッドにENUM_SUPPORTフラグが付与される() {
        JigMethod method = TestSupport.buildJigMethod(SampleEnum.class, "values");
        var flags = method.jigMethodDeclaration().header().jigMethodAttribute().flags();

        assertTrue(flags.contains(JigMethodFlag.ENUM_SUPPORT));
    }

    @Test
    void enumのvalueOfメソッドにENUM_SUPPORTフラグが付与される() {
        JigMethod method = TestSupport.buildJigMethod(SampleEnum.class, "valueOf");
        var flags = method.jigMethodDeclaration().header().jigMethodAttribute().flags();

        assertTrue(flags.contains(JigMethodFlag.ENUM_SUPPORT));
    }

    @Test
    void enumのコンパイラ生成メソッドdollarValuesにENUM_SUPPORTフラグが付与される() {
        JigMethod method = TestSupport.buildJigMethod(SampleEnum.class, "$values");
        var flags = method.jigMethodDeclaration().header().jigMethodAttribute().flags();

        assertTrue(flags.contains(JigMethodFlag.ENUM_SUPPORT));
    }
}
