package org.dddjava.jig.domain.model.knowledge.library;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryGroupingRuleTest {

    private final LibraryGroupingRule rule = LibraryGroupingRule.defaultRule();

    @Test
    void 既知マップは長一致が優先される() {
        LibraryGroupingRule.Library group = rule.libraryOf(PackageId.valueOf("org.springframework.web.servlet"));
        assertEquals("spring-web", group.id());
        assertEquals("spring-web", group.displayName());
        assertFalse(group.isJavaStandard());
    }

    @Test
    void 既知マップ未満の親パッケージはより短い既知prefixで集約される() {
        LibraryGroupingRule.Library group = rule.libraryOf(PackageId.valueOf("org.springframework.context.annotation"));
        assertEquals("spring", group.id());
    }

    @Test
    void 既知ライブラリprefixそのものもマッチする() {
        LibraryGroupingRule.Library group = rule.libraryOf(PackageId.valueOf("org.junit"));
        assertEquals("junit", group.id());
    }

    @Test
    void 未知のパッケージは深さ2で自動集約され表示名はTLDが除外される() {
        // 集約のキー（id）は深さ2のまま、表示名は先頭TLDを除く
        LibraryGroupingRule.Library g1 = rule.libraryOf(PackageId.valueOf("com.example.foo.bar.baz"));
        assertEquals("com.example", g1.id());
        assertEquals("example", g1.displayName());
        assertFalse(g1.isJavaStandard());

        LibraryGroupingRule.Library g2 = rule.libraryOf(PackageId.valueOf("io.netty.handler.codec"));
        assertEquals("io.netty", g2.id());
        assertEquals("netty", g2.displayName());

        LibraryGroupingRule.Library g3 = rule.libraryOf(PackageId.valueOf("org.unknownlib.module.sub"));
        assertEquals("org.unknownlib", g3.id());
        assertEquals("unknownlib", g3.displayName());

        // TLDで始まらないものは集約も表示名もそのまま
        LibraryGroupingRule.Library g4 = rule.libraryOf(PackageId.valueOf("jakarta.foobar.baz"));
        assertEquals("jakarta.foobar", g4.id());
        assertEquals("jakarta.foobar", g4.displayName());
    }

    @Test
    void Java標準は単一のjavaに集約されisJavaStandardがtrueになる() {
        LibraryGroupingRule.Library group = rule.libraryOf(PackageId.valueOf("java.util.concurrent"));
        assertEquals("java", group.id());
        assertTrue(group.isJavaStandard());
    }

    @Test
    void javaxもJava標準として扱う() {
        LibraryGroupingRule.Library group = rule.libraryOf(PackageId.valueOf("javax.annotation"));
        assertEquals("java", group.id());
        assertTrue(group.isJavaStandard());
    }
}
