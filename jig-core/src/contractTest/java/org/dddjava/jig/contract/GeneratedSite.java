package org.dddjava.jig.contract;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 生成済みサイトの内容を、実行のたびに変わる値を伏せて読み出す。
 */
final class GeneratedSite {

    /**
     * 計測値そのものであり、内容の一致を求める対象ではない成果物。
     */
    static final Set<String> MEASUREMENTS = Set.of("metrics-data.js", "jig-metrics.txt");

    private GeneratedSite() {
    }

    /**
     * 相対パスごとの内容を並べる。
     *
     * @param excludedFileNames 比較対象から外すファイル名
     */
    static String normalizedContents(Path siteDirectory, Set<String> excludedFileNames) {
        try (var paths = Files.walk(siteDirectory)) {
            List<Path> files = paths.filter(Files::isRegularFile)
                    .filter(file -> !excludedFileNames.contains(file.getFileName().toString()))
                    .sorted()
                    .collect(Collectors.toList());
            if (files.isEmpty()) {
                throw new IllegalStateException("成果物がありません: " + siteDirectory);
            }

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

    static String read(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
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
