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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JigSettingsLoaderTest {

    @TempDir
    Path userHomeDir;
    @TempDir
    Path userDirDir;

    private JigSettings loadFromLayers(PartialJigSettings explicit) {
        return JigSettingsLoader.load(List.of(
                explicit,
                new PropertiesFileSource(userDirDir).read(),
                new PropertiesFileSource(userHomeDir.resolve(".jig")).read()
        ));
    }

    @Test
    void デフォルト値() {
        JigSettings defaults = JigSettings.defaults();

        assertTrue(defaults.domainPattern().isEmpty());
        assertEquals(JigDocument.canonical(), defaults.documentTypes());
        assertEquals(Locale.JAPANESE, defaults.locale());
        // outputDirectory は user.dir/.jig（システムプロパティ依存）
        assertEquals(Path.of(System.getProperty("user.dir")).resolve(".jig").toAbsolutePath(),
                defaults.outputDirectory());
    }

    @Test
    void 全層emptyならデフォルトが採用される() {
        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);

        JigSettings defaults = JigSettings.defaults();
        assertEquals(defaults.outputDirectory(), loaded.outputDirectory());
        assertEquals(defaults.domainPattern(), loaded.domainPattern());
        assertEquals(defaults.documentTypes(), loaded.documentTypes());
        assertEquals(defaults.locale(), loaded.locale());
    }

    @Test
    void explicitのみ指定すれば反映される() {
        PartialJigSettings explicit = PartialJigSettings.builder()
                .outputDirectory(userDirDir.resolve("primary_output"))
                .domainPattern("com.example.primary.+")
                .documentTypes(List.of(JigDocument.DomainModel, JigDocument.ListOutput))
                .locale(Locale.ENGLISH)
                .build();

        JigSettings loaded = loadFromLayers(explicit);

        assertEquals(userDirDir.resolve("primary_output"), loaded.outputDirectory());
        assertEquals("com.example.primary.+", loaded.domainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.DomainModel, JigDocument.ListOutput), loaded.documentTypes());
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
                .documentTypes(List.of(JigDocument.DomainModel, JigDocument.ListOutput))
                .locale(Locale.JAPANESE)
                .build();

        JigSettings loaded = loadFromLayers(explicit);

        assertEquals(Path.of("/primary/jig"), loaded.outputDirectory());
        assertEquals("com.example.primary.+", loaded.domainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.DomainModel, JigDocument.ListOutput), loaded.documentTypes());
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
        assertEquals(List.of(JigDocument.ListOutput), loaded.documentTypes());
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
            JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);
            assertEquals(Locale.forLanguageTag("en-US"), loaded.locale());
        }

        @Test
        void 空文字は無視されデフォルトが残る() throws IOException {
            writeJigProperties(userDirDir, "jig.locale=\n");
            JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);
            assertEquals(Locale.JAPANESE, loaded.locale());
        }

        @Test
        void 不正タグは無視されデフォルトが残る() throws IOException {
            writeJigProperties(userDirDir, "jig.locale=!!!\n");
            JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);
            assertEquals(Locale.JAPANESE, loaded.locale());
        }

        @Test
        void 不正タグでも下位層の値があれば下位層が活きる() throws IOException {
            writeJigProperties(userDirDir, "jig.locale=!!!\n");
            writeJigProperties(userHomeDir.resolve(".jig"), "jig.locale=en-US\n");
            JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);
            assertEquals(Locale.forLanguageTag("en-US"), loaded.locale());
        }
    }

    @Test
    void documentTypesに未知の値が含まれていれば当該キーは無視される() throws IOException {
        writeJigProperties(userDirDir, "jig.document.types=UnknownDoc,DomainModel\n");
        JigSettings loaded = loadFromLayers(PartialJigSettings.EMPTY);
        assertEquals(JigDocument.canonical(), loaded.documentTypes());
    }

    private static void writeJigProperties(Path dir, String content) throws IOException {
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("jig.properties"), content);
    }
}
