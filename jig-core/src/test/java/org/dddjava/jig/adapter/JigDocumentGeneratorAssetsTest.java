package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.junit.jupiter.api.Test;
import testing.JigTest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JigTest
class JigDocumentGeneratorAssetsTest {

    @Test
    void templatesAssets配下の全ファイルをコピーする(JigDocumentGenerator sut, JigDocumentContext jigDocumentContext)
            throws Exception {
        invokeGenerateAssets(sut);

        Path copiedAssetsDirectory = jigDocumentContext.outputDirectory().resolve("assets");
        Set<String> copied = collectRelativeFilePaths(copiedAssetsDirectory);
        Set<String> expected = collectTemplateAssetsRelativePaths();
        // 型定義はコピーしない
        expected.remove("types.js");
        // バンドル元のファイルはコピー対象外（jig-bundle.js に集約されるため）
        expected.remove("jig-dom.js");
        expected.remove("jig-glossary.js");
        expected.remove("jig-util.js");
        expected.remove("jig-mermaid.js");

        assertEquals(expected, copied);
    }

    private void invokeGenerateAssets(JigDocumentGenerator sut)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var method = JigDocumentGenerator.class.getDeclaredMethod("generateAssets");
        method.setAccessible(true);
        method.invoke(sut);
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
