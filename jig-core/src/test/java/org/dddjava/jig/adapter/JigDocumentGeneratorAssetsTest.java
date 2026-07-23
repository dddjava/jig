package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JigDocumentGeneratorAssetsTest {

    @Test
    void templatesAssets配下の全ファイルをコピーする(@TempDir Path outputDirectory)
            throws IOException, URISyntaxException {
        // この検証は解析結果を必要としないため、解析入力（fixture）を用意せずConfigurationだけで組み立てる
        var settings = new JigSettings(outputDirectory, Optional.empty(), JigDocument.canonical(), Locale.JAPANESE);
        var sut = Configuration.from(settings).jigDocumentGenerator();

        sut.generateSharedAssets();

        Path copiedAssetsDirectory = outputDirectory.resolve("assets");
        Set<String> copied = collectRelativeFilePaths(copiedAssetsDirectory);
        Set<String> expected = collectTemplateAssetsRelativePaths();
        // 型定義はコピーしない
        expected.remove("types.js");
        // バンドル元のファイルはコピー対象外（jig-bundle.js に集約されるため）
        expected.remove("jig-dom.js");
        expected.remove("jig-data.js");
        expected.remove("jig-glossary.js");
        expected.remove("jig-mermaid.js");
        expected.remove("jig-util.js");
        expected.remove("jig-bootstrap.js");
        expected.remove("jig-i18n.js");

        // 各ドキュメント用のファイルは generateDocuments でコピーされるため、generateSharedAssets のテストでは除外する
        expected.remove("domain.js");
        expected.remove("glossary.js");
        expected.remove("inbound.js");
        expected.remove("insight.js");
        expected.remove("list-output.js");
        expected.remove("outbound.js");
        expected.remove("package.js");
        expected.remove("usecase.js");
        expected.remove("library-dependency.js");

        assertEquals(expected, copied);
    }

    private Set<String> collectTemplateAssetsRelativePaths() throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource("templates/assets");
        if (resource == null) {
            throw new IOException("templates/assets が見つかりませんでした。");
        }
        URI uri = resource.toURI();
        if ("jar".equalsIgnoreCase(uri.getScheme())) {
            try (FileSystem fs = openJarFileSystem(uri)) {
                return collectRelativeFilePaths(fs.getPath("/templates/assets"));
            }
        }
        return collectRelativeFilePaths(Paths.get(uri));
    }

    private FileSystem openJarFileSystem(URI uri) throws IOException {
        try {
            return FileSystems.newFileSystem(uri, Map.of());
        } catch (FileSystemAlreadyExistsException ignored) {
            return FileSystems.getFileSystem(uri);
        }
    }

    private Set<String> collectRelativeFilePaths(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .map(root::relativize)
                    .map(path -> path.toString().replace('\\', '/'))
                    .collect(Collectors.toSet());
        }
    }
}
