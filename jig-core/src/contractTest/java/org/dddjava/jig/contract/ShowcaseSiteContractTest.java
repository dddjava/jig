package org.dddjava.jig.contract;

import org.dddjava.jig.JigExecutor;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.fixtures.FixtureProject;
import org.dddjava.jig.fixtures.JigFixtures;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 代表プロジェクトからサイトを生成できることの契約。
 *
 * 生成物は Web 側の Contract テスト（jig-core/src/test/js/contract）も読むため、
 * 作業ディレクトリからの固定の場所へ出力する。
 */
class ShowcaseSiteContractTest {

    static final String FIXTURE = "showcase";
    static final int FIXTURE_RELEASE = 21;

    /**
     * jig-core をカレントとする相対パス。Web 側からは jig-core/build/contract-site/showcase として参照する。
     */
    static final Path OUTPUT_DIRECTORY = Paths.get("build", "contract-site", "showcase");

    @BeforeAll
    static void サイトを生成する() {
        generateSiteTo(OUTPUT_DIRECTORY);
    }

    static void generateSiteTo(Path outputDirectory) {
        JigSettings settings = new JigSettings(
                outputDirectory,
                Optional.of("showcase.domain.+"),
                JigDocument.canonical(),
                Locale.JAPANESE);

        FixtureProject project = JigFixtures.project(FIXTURE);
        SourceBasePaths sourceBasePaths = new SourceBasePaths(
                new SourceBasePath(List.of(project.classes(FIXTURE_RELEASE))),
                new SourceBasePath(List.of(project.sources())));

        var results = JigExecutor.standard(Configuration.from(settings), sourceBasePaths).listResult();
        assertTrue(results.stream().map(result -> result.jigDocument()).toList()
                        .containsAll(JigDocument.canonical()),
                () -> "生成されなかったドキュメントがあります: " + results);
    }

    @Test
    void 入口となるページとデータが出力される() {
        assertTrue(Files.isRegularFile(OUTPUT_DIRECTORY.resolve("index.html")));
        assertTrue(Files.isDirectory(OUTPUT_DIRECTORY.resolve("data")));
        assertTrue(Files.isDirectory(OUTPUT_DIRECTORY.resolve("assets")));
    }

    @Test
    void データJSはグローバルへの代入形式で出力される() {
        // ページのJSはこの形式を前提に読み込む。個々の内容の構造は Web 側の Contract で見る
        try (var paths = Files.list(OUTPUT_DIRECTORY.resolve("data"))) {
            List<Path> dataFiles = paths.filter(path -> path.getFileName().toString().endsWith(".js")).toList();

            assertFalse(dataFiles.isEmpty(), "データJSが出力されていません");
            for (Path dataFile : dataFiles) {
                String content = Files.readString(dataFile, StandardCharsets.UTF_8);
                assertTrue(content.startsWith("globalThis."),
                        () -> "グローバルへの代入形式ではありません: " + dataFile);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void 解析結果が成果物へ届く() {
        String data = readAll(OUTPUT_DIRECTORY.resolve("data"));

        assertTrue(data.contains("showcase.domain.order.Order"), "解析した型が出力にありません");
    }

    @Test
    void 生成したHTMLが空でない() {
        try (var paths = Files.list(OUTPUT_DIRECTORY)) {
            List<Path> htmlFiles = paths.filter(path -> path.getFileName().toString().endsWith(".html")).toList();

            assertFalse(htmlFiles.isEmpty(), "HTMLが出力されていません");
            for (Path htmlFile : htmlFiles) {
                assertTrue(Files.size(htmlFile) > 0, () -> "空のHTMLがあります: " + htmlFile);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readAll(Path directory) {
        try (var paths = Files.walk(directory)) {
            StringBuilder sb = new StringBuilder();
            for (Path file : paths.filter(Files::isRegularFile).toList()) {
                sb.append(Files.readString(file, StandardCharsets.UTF_8));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
