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
    void 未知のパッケージは深さ2で自動集約される() {
        ExternalGroupingRule.Group group = rule.groupOf(PackageId.valueOf("com.example.foo.bar.baz"));
        assertEquals("com.example", group.id());
        assertFalse(group.isJdk());
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
