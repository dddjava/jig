package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JigPropertyLoaderTest {

    @TempDir
    Path tempDir;

    Path userHomeBackup;
    Path userDirBackup;

    @BeforeEach
    void setUp() {
        // テストが終わったら戻す用
        userHomeBackup = Paths.get(System.getProperty("user.home"));
        userDirBackup = Paths.get(System.getProperty("user.dir"));
        // テストのために上書き
        System.setProperty("user.home", tempDir.toAbsolutePath().toString());
        System.setProperty("user.dir", tempDir.toAbsolutePath().toString());
    }

    @AfterEach
    void tearDown() {
        // 終わったので戻す
        System.setProperty("user.home", userHomeBackup.toAbsolutePath().toString());
        System.setProperty("user.dir", userDirBackup.toAbsolutePath().toString());
    }

    @Test
    void デフォルト値の確認() {
        JigProperties defaultProps = JigProperties.defaultInstance();

        assertTrue(defaultProps.getDomainPattern().isEmpty());
        assertEquals(JigDocument.values().length, defaultProps.jigDocuments.size());
        assertEquals(JigProperty.defaultOutputDirectory(), defaultProps.outputDirectory.toString());
    }

    @Test
    void 指定あり_設定ファイルなし__指定() {
        // given
        JigProperties primaryProperties = new JigProperties(
                List.of(JigDocument.ApplicationList, JigDocument.ListOutput),
                Optional.of("com.example.primary.+"),
                tempDir.resolve("primary_output")
        );
        JigPropertyLoader loader = new JigPropertyLoader(primaryProperties);

        // when
        JigProperties loadedProperties = loader.load();

        // then
        assertEquals("com.example.primary.+", loadedProperties.getDomainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.ApplicationList, JigDocument.ListOutput), loadedProperties.jigDocuments);
        assertEquals(tempDir.resolve("primary_output"), loadedProperties.outputDirectory);
    }

    @Test
    void 指定あり_設定ファイルあり__指定() throws IOException {
        // given
        Path homeConfigDir = tempDir.resolve(".jig");
        Files.createDirectory(homeConfigDir);

        Files.writeString(homeConfigDir.resolve("jig.properties"), """
                jig.pattern.domain=com.example.home.+
                jig.document.types=BusinessRuleList
                """);

        JigProperties primaryProperties = new JigProperties(
                List.of(JigDocument.ApplicationList, JigDocument.ListOutput),
                Optional.of("com.example.primary.+"),
                tempDir.resolve("primary_output")
        );
        JigPropertyLoader loader = new JigPropertyLoader(primaryProperties);

        // when
        JigProperties loadedProperties = loader.load();

        // then
        assertEquals("com.example.primary.+", loadedProperties.getDomainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.ApplicationList, JigDocument.ListOutput), loadedProperties.jigDocuments);
        assertEquals(tempDir.resolve("primary_output"), loadedProperties.outputDirectory);
    }

    @Test
    void 指定なし_設定ファイルなし__デフォルト() {
        // given
        JigProperties 何も指定しない = new JigProperties(List.of(), Optional.empty(), Path.of(""));
        JigPropertyLoader loader = new JigPropertyLoader(何も指定しない);

        // when
        JigProperties loadedProperties = loader.load();

        // then
        var defaultProperties = JigProperties.defaultInstance();
        assertEquals(defaultProperties.getDomainPattern(), loadedProperties.getDomainPattern());
        assertEquals(defaultProperties.jigDocuments, loadedProperties.jigDocuments);
        assertEquals(defaultProperties.outputDirectory, loadedProperties.outputDirectory);
    }

    @Test
    void 指定なし_設定ファイルあり__設定ファイル() throws IOException {
        // given
        Path homeConfigDir = tempDir.resolve(".jig");
        Files.createDirectory(homeConfigDir);

        Files.writeString(homeConfigDir.resolve("jig.properties"), """
                jig.pattern.domain=com.example.home.+
                jig.document.types=BusinessRuleList
                jig.output.directory=/hoge/fuga
                """);

        var 何も指定しない = new JigProperties(List.of(), Optional.empty(), Path.of(""));
        JigPropertyLoader loader = new JigPropertyLoader(何も指定しない);

        // when
        JigProperties loadedProperties = loader.load();

        // then
        assertEquals("com.example.home.+", loadedProperties.getDomainPattern().orElseThrow());
        assertEquals(List.of(JigDocument.BusinessRuleList), loadedProperties.jigDocuments);
        assertEquals(Path.of("/hoge/fuga"), loadedProperties.outputDirectory);
    }

}
