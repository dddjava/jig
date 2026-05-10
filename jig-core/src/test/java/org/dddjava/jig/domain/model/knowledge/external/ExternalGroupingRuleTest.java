package org.dddjava.jig.domain.model.knowledge.external;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalGroupingRuleTest {

    private final ExternalGroupingRule rule = ExternalGroupingRule.defaultRule();

    @Test
    void 既知マップは長一致が優先される() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("org.springframework.web.servlet"));
        assertEquals("spring-web", group.id());
        assertEquals("spring-web", group.displayName());
        assertFalse(group.isJavaStandard());
    }

    @Test
    void 既知マップ未満の親パッケージはより短い既知prefixで集約される() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("org.springframework.context.annotation"));
        assertEquals("spring", group.id());
    }

    @Test
    void 既知ライブラリprefixそのものもマッチする() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("org.junit"));
        assertEquals("junit", group.id());
    }

    @Test
    void 未知のパッケージは深さ2で自動集約され表示名はTLDが除外される() {
        // 集約のキー（id）は深さ2のまま、表示名は先頭TLDを除く
        ExternalGroupingRule.Group g1 = rule.groupOf(PackageId.valueOf("com.example.foo.bar.baz"));
        assertEquals("com.example", g1.id());
        assertEquals("example", g1.displayName());
        assertFalse(g1.isJavaStandard());

        ExternalGroupingRule.Group g2 = rule.groupOf(PackageId.valueOf("io.netty.handler.codec"));
        assertEquals("io.netty", g2.id());
        assertEquals("netty", g2.displayName());

        ExternalGroupingRule.Group g3 = rule.groupOf(PackageId.valueOf("org.unknownlib.module.sub"));
        assertEquals("org.unknownlib", g3.id());
        assertEquals("unknownlib", g3.displayName());

        // TLDで始まらないものは集約も表示名もそのまま
        ExternalGroupingRule.Group g4 = rule.groupOf(PackageId.valueOf("jakarta.foobar.baz"));
        assertEquals("jakarta.foobar", g4.id());
        assertEquals("jakarta.foobar", g4.displayName());
    }

    @Test
    void Java標準は単一のjavaグループに集約されisJavaStandardがtrueになる() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("java.util.concurrent"));
        assertEquals("java", group.id());
        assertTrue(group.isJavaStandard());
    }

    @Test
    void javaxもJava標準として扱う() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("javax.annotation"));
        assertEquals("java", group.id());
        assertTrue(group.isJavaStandard());
    }
}
