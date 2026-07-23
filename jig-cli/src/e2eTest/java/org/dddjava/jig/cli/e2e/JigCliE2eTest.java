package org.dddjava.jig.cli.e2e;

import org.dddjava.jig.fixtures.JigFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 配布する CLI から JIG を起動できることの契約。
 *
 * 利用者と同じ経路を確認するため、ビルド済みの jar を別プロセスで実行する。
 * Spring の内部構成には触れない。
 */
class JigCliE2eTest {

    static final int FIXTURE_RELEASE = 21;

    @Test
    void 最小のプロジェクトを解析してサイトを出力する(@TempDir Path workDirectory) throws Exception {
        Path projectDirectory = JigFixtures.project("minimal-java")
                .deployTo(workDirectory.resolve("project"), FIXTURE_RELEASE);
        Path outputDirectory = workDirectory.resolve("out");

        Result result = runCli(projectDirectory, outputDirectory);

        assertEquals(0, result.exitCode, result.output);
        assertTrue(Files.isRegularFile(outputDirectory.resolve("index.html")), result.output);
    }

    @Test
    void 解析した型が出力に現れる(@TempDir Path workDirectory) throws Exception {
        Path projectDirectory = JigFixtures.project("minimal-java")
                .deployTo(workDirectory.resolve("project"), FIXTURE_RELEASE);
        Path outputDirectory = workDirectory.resolve("out");

        Result result = runCli(projectDirectory, outputDirectory);
        assertEquals(0, result.exitCode, result.output);

        // 出力形式ではなく「解析結果が成果物へ届いていること」を見る
        String data = readAll(outputDirectory.resolve("data"));
        assertTrue(data.contains("MinimalValue"), () -> "解析した型が出力にありません: " + result.output);
        assertTrue(data.contains("fixture.minimal.domain"), () -> "解析したパッケージが出力にありません: " + result.output);
    }

    @Test
    void 解析対象がなくてもエラー終了しない(@TempDir Path workDirectory) throws Exception {
        Path emptyProject = Files.createDirectories(workDirectory.resolve("empty"));
        Path outputDirectory = workDirectory.resolve("out");

        Result result = runCli(emptyProject, outputDirectory);

        assertEquals(0, result.exitCode, result.output);
    }

    @Test
    void 複数モジュールが並ぶ構成でも全てのモジュールを収集する(@TempDir Path workDirectory) throws Exception {
        Path projectDirectory = workDirectory.resolve("project");
        JigFixtures.project("minimal-java").deployTo(projectDirectory.resolve("module-a"), FIXTURE_RELEASE);
        JigFixtures.project("showcase").deployTo(projectDirectory.resolve("module-b"), FIXTURE_RELEASE);
        Path outputDirectory = workDirectory.resolve("out");

        Result result = runCli(projectDirectory, outputDirectory);
        assertEquals(0, result.exitCode, result.output);

        String data = readAll(outputDirectory.resolve("data"));
        assertTrue(data.contains("fixture.minimal.domain"), () -> "module-a を収集していません: " + result.output);
        assertTrue(data.contains("showcase.domain.order"), () -> "module-b を収集していません: " + result.output);
    }

    @Test
    void 読めないクラスファイルがあっても解析を継続する(@TempDir Path workDirectory) throws Exception {
        Path projectDirectory = JigFixtures.project("minimal-java")
                .deployTo(workDirectory.resolve("project"), FIXTURE_RELEASE);
        Files.writeString(
                projectDirectory.resolve("build/classes/java/main/Broken.class"),
                "これはクラスファイルではない");
        Path outputDirectory = workDirectory.resolve("out");

        Result result = runCli(projectDirectory, outputDirectory);

        assertEquals(0, result.exitCode, result.output);
        assertTrue(readAll(outputDirectory.resolve("data")).contains("MinimalValue"),
                () -> "読めた分の解析結果が出ていません: " + result.output);
    }

    @Test
    void 不正な設定値は原因を示してエラー終了する(@TempDir Path workDirectory) throws Exception {
        Path projectDirectory = Files.createDirectories(workDirectory.resolve("project"));

        Result result = runCli(projectDirectory, workDirectory.resolve("out"),
                "--jig.document.types=NoSuchDocument");

        assertNotEquals(0, result.exitCode, result.output);
        assertTrue(result.output.contains("jig.document.types"), () -> "設定キーが示されていません: " + result.output);
        assertTrue(result.output.contains("NoSuchDocument"), () -> "不正な値が示されていません: " + result.output);
    }

    private static Result runCli(Path projectDirectory, Path outputDirectory, String... additionalArguments)
            throws Exception {
        List<String> command = new ArrayList<>(List.of(
                javaCommand(),
                "-jar", System.getProperty("jig.cli.jar"),
                "--project.path=" + projectDirectory,
                "--jig.output.directory=" + outputDirectory));
        command.addAll(List.of(additionalArguments));

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(3, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            throw new AssertionError("CLIが終了しません: " + output);
        }
        return new Result(process.exitValue(), output);
    }

    private static String javaCommand() {
        return Paths.get(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static String readAll(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) return "";
        try (var paths = Files.walk(directory)) {
            List<Path> files = paths.filter(Files::isRegularFile).toList();
            StringBuilder sb = new StringBuilder();
            for (Path file : files) {
                sb.append(Files.readString(file, StandardCharsets.UTF_8));
            }
            return sb.toString();
        }
    }

    private record Result(int exitCode, String output) {
    }
}
