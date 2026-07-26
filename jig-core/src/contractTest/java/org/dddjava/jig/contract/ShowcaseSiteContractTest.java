package org.dddjava.jig.contract;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 代表プロジェクトからサイトを生成できることの契約。
 *
 * 生成物は Web 側の Contract テスト（jig-core/src/test/js/contract）も読むため、
 * ビルドが指定する場所へ出力する。
 */
class ShowcaseSiteContractTest {

    /**
     * 生成サイトの出力先。Web 側の Contract テストも同じ場所を読む。
     */
    static final Path OUTPUT_DIRECTORY = siteRoot().resolve("showcase");

    /**
     * 出力先はビルドがシステムプロパティ {@code jig.contract.siteRoot} で渡す。
     */
    private static Path siteRoot() {
        String configured = System.getProperty("jig.contract.siteRoot");
        if (configured == null) {
            throw new IllegalStateException(
                    "システムプロパティ jig.contract.siteRoot が未設定です。jig-core の contractTest タスクから実行してください。");
        }
        return Paths.get(configured);
    }

    @BeforeAll
    static void サイトを生成する() {
        List<JigDocument> documents = ShowcaseSite.generateTo(OUTPUT_DIRECTORY);

        assertTrue(documents.containsAll(JigDocument.canonical()),
                () -> "生成されなかったドキュメントがあります: " + documents);
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
