package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JigSettingsLoaderTest {

    @TempDir
    Path userHomeDir;
    @TempDir
    Path userDirDir;

    private JigSettings loadFromLayers(PartialJigSettings explicit) {
        return loadFromLayers(explicit, PartialJigSettings.EMPTY);
    }

    private JigSettings loadFromLayers(PartialJigSettings explicit, PartialJigSettings fallback) {
        return JigSettingsLoader.load(List.of(
                explicit,
                new PropertiesFileSource(userDirDir).read(),
                new PropertiesFileSource(userHomeDir.resolve(".jig")).read(),
                fallback
        ));
    }

    private PartialJigSettings outputFallback() {
        return PartialJigSettings.builder()
                .outputDirectory(userDirDir.resolve("fallback_out"))
                .build();
    }

    @Test
    void 出力先未指定ならば例外() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> loadFromLayers(PartialJigSettings.EMPTY));
        assertTrue(e.getMessage().contains("出力先ディレクトリ"));
    }

    @Test
    void 出力先のみexplicit指定で他はjig_coreデフォルト() {
        PartialJigSettings explicit = PartialJigSettings.builder()
                .outputDirectory(userDirDir.resolve("out"))
                .build();

        JigSettings loaded = loadFromLayers(explicit);

        assertEquals(userDirDir.resolve("out"), loaded.outputDirectory());
        assertTrue(loaded.domainPattern().isEmpty());
        assertEquals(JigDocument.canonical(), loaded.jigDocuments());
        assertEquals(Locale.JAPANESE, loaded.locale());
    }

    @Test
    void explicitのみ指定すれば反映される() {
        PartialJigSettings explicit = PartialJigSettings.builder()
                .outputDirectory(userDirDir.resolve("primary_output"))
                .domainPattern("com.example.primary.+")
                .jigDocuments(List.of(JigDocument.DomainModel, JigDocument.ListOutput))
                .locale(Locale.ENGLISH)
                .build();

        JigSettings loaded = loadFromLayers(explicit);

        assertEquals(userDirDir.resolve("primary_output"), loaded.outputDirectory());
        assertEquals("com.example.primary.+", loaded.domainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.DomainModel, JigDocument.ListOutput), loaded.jigDocuments());
        assertEquals(Locale.ENGLISH, loaded.locale());
    }

    @Test
    void explicitがuserDirとuserHomeより優先される() throws IOException {
        writeJigProperties(userHomeDir.resolve(".jig"), """
                jig.pattern.domain=com.example.home.+
                jig.document.types=ListOutput
                jig.output.directory=/home/jig
                jig.locale=en-US
                """);
        writeJigProperties(userDirDir, """
                jig.pattern.domain=com.example.userdir.+
                jig.document.types=DomainModel
                jig.output.directory=/userdir/jig
                jig.locale=fr-FR
                """);

        PartialJigSettings explicit = PartialJigSettings.builder()
                .outputDirectory(Path.of("/primary/jig"))
                .domainPattern("com.example.primary.+")
                .jigDocuments(List.of(JigDocument.DomainModel, JigDocument.ListOutput))
                .locale(Locale.JAPANESE)
                .build();

        JigSettings loaded = loadFromLayers(explicit);

        assertEquals(Path.of("/primary/jig"), loaded.outputDirectory());
        assertEquals("com.example.primary.+", loaded.domainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.DomainModel, JigDocument.ListOutput), loaded.jigDocuments());
        assertEquals(Locale.JAPANESE, loaded.locale());
    }

    @Test
    void userDirがuserHomeより優先される() throws IOException {
        writeJigProperties(userHomeDir.resolve(".jig"), """
                jig.pattern.domain=com.example.home.+
                jig.output.directory=/home/jig
                """);
        writeJigProperties(userDirDir, """
                jig.pattern.domain=com.example.userdir.+
                """);

        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);

        // user.dir が指定したフィールドは user.dir が勝つ
        assertEquals("com.example.userdir.+", loaded.domainPattern().orElseThrow());
        // user.dir が指定しなかったフィールドは user.home が埋める
        assertEquals(Path.of("/home/jig"), loaded.outputDirectory());
    }

    @Test
    void explicit未指定の場合user_dirのjig_propertiesが反映される() throws IOException {
        writeJigProperties(userDirDir, """
                jig.pattern.domain=com.example.userdir.+
                jig.document.types=ListOutput
                jig.output.directory=/userdir/jig
                """);

        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);

        assertEquals("com.example.userdir.+", loaded.domainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.ListOutput), loaded.jigDocuments());
        assertEquals(Path.of("/userdir/jig"), loaded.outputDirectory());
        assertEquals(Locale.JAPANESE, loaded.locale());
    }

    @Test
    void explicit未指定の場合user_homeのjig_propertiesが反映される() throws IOException {
        writeJigProperties(userHomeDir.resolve(".jig"), """
                jig.pattern.domain=com.example.home.+
                jig.output.directory=/home/jig
                """);

        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);

        assertEquals("com.example.home.+", loaded.domainPattern().orElseThrow());
        assertEquals(Path.of("/home/jig"), loaded.outputDirectory());
    }

    @Nested
    class locale指定 {
        @Test
        void 有効な言語タグは反映される() throws IOException {
            writeJigProperties(userDirDir, "jig.locale=en-US\n");
            JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY, outputFallback());
            assertEquals(Locale.forLanguageTag("en-US"), loaded.locale());
        }

        @Test
        void 空文字は無視されデフォルトが残る() throws IOException {
            writeJigProperties(userDirDir, "jig.locale=\n");
            JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY, outputFallback());
            assertEquals(Locale.JAPANESE, loaded.locale());
        }

        @Test
        void 不正タグは例外() throws IOException {
            writeJigProperties(userDirDir, "jig.locale=!!!\n");
            assertThrows(IllegalArgumentException.class,
                    () -> loadFromLayers(PartialJigSettings.EMPTY, outputFallback()));
        }

        @Test
        void 下位層に有効値があっても不正タグは例外() throws IOException {
            // fail-fast: 不正値はどの層にあっても読み込み時点で例外（下位層では救済しない）
            writeJigProperties(userDirDir, "jig.locale=!!!\n");
            writeJigProperties(userHomeDir.resolve(".jig"), "jig.locale=en-US\n");
            assertThrows(IllegalArgumentException.class,
                    () -> loadFromLayers(PartialJigSettings.EMPTY, outputFallback()));
        }
    }

    @Test
    void 未知のjigキーが含まれていても既知キーは正常に読まれる() throws IOException {
        writeJigProperties(userDirDir, """
                jig.outputDir=/typo/path
                jig.pattern.domain=com.example.userdir.+
                jig.output.directory=/correct/path
                """);

        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);

        assertEquals(Path.of("/correct/path"), loaded.outputDirectory());
        assertEquals("com.example.userdir.+", loaded.domainPattern().orElseThrow());
    }

    @Test
    void documentTypesに未知の値が含まれていれば例外() throws IOException {
        writeJigProperties(userDirDir, "jig.document.types=UnknownDoc,DomainModel\n");
        assertThrows(IllegalArgumentException.class,
                () -> loadFromLayers(PartialJigSettings.EMPTY, outputFallback()));
    }

    @Test
    void documentTypesはカンマ前後の空白を許容する() throws IOException {
        writeJigProperties(userDirDir, "jig.document.types=DomainModel, ListOutput\n");
        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY, outputFallback());
        assertEquals(List.of(JigDocument.DomainModel, JigDocument.ListOutput), loaded.jigDocuments());
    }

    @Test
    void fallback層は最低優先度として出力先を埋める() {
        // jig.properties も explicit も outputDirectory を指定していない場合に fallback が効く
        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY, outputFallback());
        assertEquals(userDirDir.resolve("fallback_out"), loaded.outputDirectory());
    }

    @Test
    void fallback層はjig_propertiesより優先度が低い() throws IOException {
        writeJigProperties(userDirDir, "jig.output.directory=/from/properties\n");
        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY, outputFallback());
        assertEquals(Path.of("/from/properties"), loaded.outputDirectory());
    }

    @Nested
    class システムプロパティと環境変数 {
        @Test
        void systemPropertyが反映される() {
            PartialJigSettings systemProperties = new SystemPropertySource(
                    Map.of("jig.pattern.domain", "com.example.sysprop.+")).read();
            JigSettings loaded = JigSettingsLoader.load(List.of(systemProperties, outputFallback()));
            assertEquals("com.example.sysprop.+", loaded.domainPattern().orElseThrow());
        }

        @Test
        void 環境変数が大文字名から逆マッピングされ反映される() {
            PartialJigSettings env = new EnvironmentVariableSource(
                    Map.of("JIG_PATTERN_DOMAIN", "com.example.env.+")).read();
            JigSettings loaded = JigSettingsLoader.load(List.of(env, outputFallback()));
            assertEquals("com.example.env.+", loaded.domainPattern().orElseThrow());
        }

        @Test
        void 未知の環境変数は無視される() {
            PartialJigSettings env = new EnvironmentVariableSource(
                    Map.of("JIG_UNKNOWN", "x", "JIG_PATTERN_DOMAIN", "com.example.env.+")).read();
            JigSettings loaded = JigSettingsLoader.load(List.of(env, outputFallback()));
            assertEquals("com.example.env.+", loaded.domainPattern().orElseThrow());
        }

        @Test
        void systemPropertyの不正値は例外() {
            SystemPropertySource source = new SystemPropertySource(Map.of("jig.locale", "!!!"));
            assertThrows(IllegalArgumentException.class, source::read);
        }

        @Test
        void 優先順位は明示_systemProperty_環境変数_の順() {
            // 明示 > -D > 環境変数 を1フィールドで検証する
            PartialJigSettings explicit = PartialJigSettings.builder()
                    .domainPattern("com.example.explicit.+").build();
            PartialJigSettings systemProperties = new SystemPropertySource(
                    Map.of("jig.pattern.domain", "com.example.sysprop.+")).read();
            PartialJigSettings env = new EnvironmentVariableSource(
                    Map.of("JIG_PATTERN_DOMAIN", "com.example.env.+")).read();

            JigSettings withExplicit = JigSettingsLoader.load(List.of(explicit, systemProperties, env, outputFallback()));
            assertEquals("com.example.explicit.+", withExplicit.domainPattern().orElseThrow());

            JigSettings withoutExplicit = JigSettingsLoader.load(List.of(systemProperties, env, outputFallback()));
            assertEquals("com.example.sysprop.+", withoutExplicit.domainPattern().orElseThrow());

            JigSettings onlyEnv = JigSettingsLoader.load(List.of(env, outputFallback()));
            assertEquals("com.example.env.+", onlyEnv.domainPattern().orElseThrow());
        }
    }

    private static void writeJigProperties(Path dir, String content) throws IOException {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("jig.properties"), content);
    }
}
