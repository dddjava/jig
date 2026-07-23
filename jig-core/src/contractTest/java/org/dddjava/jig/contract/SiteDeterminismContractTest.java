package org.dddjava.jig.contract;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 同じ入力からは同じ成果物が得られることの契約。
 *
 * 生成順序や並列処理に依存した揺れが入ると、利用者は差分レビューで無関係な変更を読むことになる。
 */
class SiteDeterminismContractTest {

    @Test
    void 同じ入力を二度解析しても成果物が変わらない(@TempDir Path workDirectory) {
        Path first = workDirectory.resolve("first");
        Path second = workDirectory.resolve("second");

        ShowcaseSiteContractTest.generateSiteTo(first);
        ShowcaseSiteContractTest.generateSiteTo(second);

        assertEquals(normalizedContents(first), normalizedContents(second));
    }

    /**
     * 計測値そのものであり、決定性を求める対象ではない成果物。
     */
    private static final Set<String> MEASUREMENTS = Set.of("metrics-data.js", "jig-metrics.txt");

    /**
     * 実行のたびに変わる値を伏せたうえで、相対パスごとの内容を並べる。
     */
    private static String normalizedContents(Path siteDirectory) {
        try (var paths = Files.walk(siteDirectory)) {
            List<Path> files = paths.filter(Files::isRegularFile)
                    .filter(file -> !MEASUREMENTS.contains(file.getFileName().toString()))
                    .sorted()
                    .collect(Collectors.toList());
            assertFalse(files.isEmpty(), "成果物がありません: " + siteDirectory);

            StringBuilder sb = new StringBuilder();
            for (Path file : files) {
                sb.append("--- ").append(siteDirectory.relativize(file)).append('\n');
                sb.append(normalize(readIfText(file))).append('\n');
            }
            return sb.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String normalize(String content) {
        return content
                // キャッシュバスティングは実行時刻から作られる
                .replaceAll("\\?v=\\d+", "?v=NORMALIZED")
                // 出力日時（index.html の jig-timestamp）も実行時刻から作られ、二度の実行の間で秒が変わりうる
                .replaceAll("data-jig-timestamp=\"[^\"]*\">[^<]*", "data-jig-timestamp=\"NORMALIZED\">NORMALIZED");
    }

    private static String readIfText(Path file) throws IOException {
        String fileName = file.getFileName().toString();
        // 画像やアイコンは内容を持ち出さず、サイズだけ比べる
        if (fileName.endsWith(".ico") || fileName.endsWith(".png")) {
            return "binary:" + Files.size(file);
        }
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
