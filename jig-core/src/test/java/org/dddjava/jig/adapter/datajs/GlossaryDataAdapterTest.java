package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.*;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.sources.javasources.TypeSourcePaths;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlossaryDataAdapterTest {

    @Test
    void 用語集JSONはHTMLではなくglossaryDataとしてJSに書き出す() {
        var glossary = new Glossary(List.of(
                new Term(new TermId("app.Account"), "Account", "desc", TermKind.クラス, TermOrigin.その他)
        ));

        String js = "globalThis.glossaryData = " + GlossaryDataAdapter.buildGlossaryJson(glossary, List.of());

        assertTrue(js.contains("globalThis.glossaryData = "));
        assertTrue(js.contains("\"app.Account\""));
        assertTrue(js.contains("Account"));
        assertFalse(js.contains("id=\"glossary-data\""));
    }

    @Test
    void 用語の有無に関わらず全型のソースパスをsourcePathsとして書き出す() {
        var glossary = new Glossary(List.of(
                new Term(new TermId("app.Account"), "Account", "desc", TermKind.クラス, TermOrigin.その他)
        ));

        String json = GlossaryDataAdapter.buildGlossaryJson(glossary, List.of(), Map.of(
                "app.Account", "src/main/java/app/Account.java",
                "app.NoJavadoc", "src/main/java/app/NoJavadoc.java"
        ));

        assertTrue(json.contains("\"sourcePaths\":"));
        assertTrue(json.contains("\"app.Account\":\"src/main/java/app/Account.java\""));
        assertTrue(json.contains("\"app.NoJavadoc\":\"src/main/java/app/NoJavadoc.java\""));
    }

    @Test
    void リポジトリ配下のパスをルート相対に変換する() {
        var root = Path.of("/repo").toAbsolutePath();
        var paths = new TypeSourcePaths(
                Map.of(TypeId.valueOf("example.Foo"), root.resolve("src/main/java/example/Foo.java")),
                Map.of(PackageId.valueOf("example"), root.resolve("src/main/java/example/package-info.java")));

        var actual = GlossaryDataAdapter.sourcePaths(paths, Optional.of(root));

        assertEquals(Map.of(
                "example.Foo", "src/main/java/example/Foo.java",
                "example", "src/main/java/example/package-info.java"), actual);
    }

    @Test
    void リポジトリ外のパスは除外する() {
        var root = Path.of("/repo").toAbsolutePath();
        var paths = new TypeSourcePaths(
                Map.of(TypeId.valueOf("example.Outside"), Path.of("/elsewhere/src/example/Outside.java"),
                        TypeId.valueOf("example.Inside"), root.resolve("src/example/Inside.java")),
                Map.of());

        var actual = GlossaryDataAdapter.sourcePaths(paths, Optional.of(root));

        assertEquals(Map.of("example.Inside", "src/example/Inside.java"), actual);
    }

    @Test
    void 相対パスや正規化前のパスもリポジトリ配下なら変換できる() {
        var root = Path.of("").toAbsolutePath();
        var paths = new TypeSourcePaths(
                Map.of(TypeId.valueOf("example.Relative"), Path.of("src/example/Relative.java"),
                        TypeId.valueOf("example.Dotted"), root.resolve("src/../src/example/Dotted.java")),
                Map.of());

        var actual = GlossaryDataAdapter.sourcePaths(paths, Optional.of(root));

        assertEquals(Map.of(
                "example.Relative", "src/example/Relative.java",
                "example.Dotted", "src/example/Dotted.java"), actual);
    }

    @Test
    void リポジトリルートがなければ空マップを返す() {
        var paths = new TypeSourcePaths(
                Map.of(TypeId.valueOf("example.Foo"), Path.of("/repo/src/example/Foo.java")),
                Map.of());

        assertEquals(Map.of(), GlossaryDataAdapter.sourcePaths(paths, Optional.empty()));
    }
}
