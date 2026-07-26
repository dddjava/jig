package org.dddjava.jig.contract;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
 * ここで見るのは成果物の構成（どのファイルが対で揃うか、データJSの読み込み形式）まで。
 * 個々のページのDOM構造とデータの内容は Web 側の Contract テスト（jig-core/src/test/js/contract）が見る。
 *
 * 生成物はその Web 側も読むため、ビルドが指定する場所へ出力する。
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
    void 入口となるページと資産が出力される() {
        assertTrue(Files.isRegularFile(OUTPUT_DIRECTORY.resolve("index.html")));
        assertTrue(Files.isRegularFile(OUTPUT_DIRECTORY.resolve("data").resolve("navigation-data.js")));
        assertTrue(Files.isDirectory(OUTPUT_DIRECTORY.resolve("assets")));
    }

    /**
     * ドキュメントを追加したときに、ページかデータJSの片方だけを出す状態で通らないようにする。
     */
    @ParameterizedTest
    @MethodSource("標準ドキュメント")
    void 標準ドキュメントはページとデータJSの対で出力される(JigDocument jigDocument) throws IOException {
        Path page = OUTPUT_DIRECTORY.resolve(jigDocument.fileName() + ".html");
        Path data = OUTPUT_DIRECTORY.resolve("data").resolve(jigDocument.fileName() + "-data.js");

        assertTrue(Files.isRegularFile(page), () -> "ページがありません: " + page);
        assertTrue(Files.isRegularFile(data), () -> "データJSがありません: " + data);
        assertTrue(Files.size(page) > 0, () -> "ページが空です: " + page);
        assertTrue(Files.size(data) > 0, () -> "データJSが空です: " + data);
    }

    static List<JigDocument> 標準ドキュメント() {
        return JigDocument.canonical();
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
    void 解析結果がドメインモデルのデータへ届く() {
        String data = GeneratedSite.read(OUTPUT_DIRECTORY.resolve("data").resolve("domain-data.js"));

        assertTrue(data.contains("showcase.domain.order.Order"), () -> "解析した型が出力にありません: " + data);
    }
}
