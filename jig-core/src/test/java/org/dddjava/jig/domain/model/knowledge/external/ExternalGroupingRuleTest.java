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
        assertFalse(group.isJdk());
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
    void 未知のパッケージは深さ2で自動集約されTLDは除外される() {
        // com で始まるものは com を除外して以降の2階層で集約
        ExternalGroupingRule.Group g1 = rule.groupOf(PackageId.valueOf("com.example.foo.bar.baz"));
        assertEquals("example.foo", g1.id());
        assertFalse(g1.isJdk());

        // io.* / org.* / net.* も同様
        assertEquals("netty.handler", rule.groupOf(PackageId.valueOf("io.netty.handler.codec")).id());
        assertEquals("unknownlib.module", rule.groupOf(PackageId.valueOf("org.unknownlib.module.sub")).id());

        // TLDで始まらないものはそのまま深さ2
        assertEquals("jakarta.foobar", rule.groupOf(PackageId.valueOf("jakarta.foobar.baz")).id());
    }

    @Test
    void JDKは単一のjdkグループに集約されisJdkがtrueになる() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("java.util.concurrent"));
        assertEquals("jdk", group.id());
        assertTrue(group.isJdk());
    }

    @Test
    void javaxもJDKとして扱う() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("javax.annotation"));
        assertEquals("jdk", group.id());
        assertTrue(group.isJdk());
    }
}
