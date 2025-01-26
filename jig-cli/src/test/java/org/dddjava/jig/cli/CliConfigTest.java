package org.dddjava.jig.cli;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CliConfigTest {

    @Nested
    class SourceBasePathsの解析 {
        @TempDir
        Path tempDir;

        @Test
        void 該当するディレクトリが一つもなくてもエラーにならない() {
            var sut = new CliConfig();
            sut.projectPath = tempDir.toAbsolutePath().toString();
            sut.directoryClasses = "invalid-directory-for-test";
            sut.directoryResources = "invalid-directory-for-test";
            sut.directorySources = "invalid-directory-for-test";

            var sourcePaths = sut.rawSourceLocations();

            assertTrue(sourcePaths.classSourceBasePaths().isEmpty());
            assertTrue(sourcePaths.javaSourceBasePaths().isEmpty());
        }

        @Test
        void Gradle標準ディレクトリ構成でソースパスが解決できる() throws IOException {
            // 準備。採用されないものとしてtestとkotlinを作成しておく。
            Files.createDirectories(tempDir.resolve("build/classes/java/main"));
            Files.createDirectories(tempDir.resolve("build/classes/java/test"));
            Files.createDirectories(tempDir.resolve("build/classes/kotlin/main"));
            Files.createDirectories(tempDir.resolve("build/resources/main"));
            Files.createDirectories(tempDir.resolve("src/main/java"));
            Files.createDirectories(tempDir.resolve("src/main/kotlin"));
            Files.createDirectories(tempDir.resolve("src/main/resources"));
            Files.createDirectories(tempDir.resolve("src/test/java"));

            var sut = new CliConfig();
            sut.projectPath = tempDir.toAbsolutePath().toString();
            sut.directoryClasses = "build/classes/java/main/";
            sut.directoryResources = "build/resources/main/";
            sut.directorySources = "src/main/java/";

            var sourcePaths = sut.rawSourceLocations();

            assertPaths(sourcePaths.classSourceBasePaths(),
                    tempDir.resolve("build/classes/java/main"),
                    tempDir.resolve("build/resources/main"));
            assertPaths(sourcePaths.javaSourceBasePaths(),
                    tempDir.resolve("src/main/java"));
        }

        @Test
        void Gradleマルチプロジェクト構成でソースパスが解決できる() throws IOException {
            // 準備
            Files.createDirectories(tempDir.resolve("a/build/classes/java/main"));
            Files.createDirectories(tempDir.resolve("a/build/resources/main"));
            Files.createDirectories(tempDir.resolve("a/src/main/java"));
            Files.createDirectories(tempDir.resolve("a/src/main/resources"));
            Files.createDirectories(tempDir.resolve("b/build/classes/java/main"));
            Files.createDirectories(tempDir.resolve("b/build/resources/main"));
            Files.createDirectories(tempDir.resolve("b/src/main/java"));
            Files.createDirectories(tempDir.resolve("b/src/main/resources"));

            var sut = new CliConfig();
            sut.projectPath = tempDir.toAbsolutePath().toString();
            // ここではaやbなどを指定しない
            sut.directoryClasses = "build/classes/java/main/";
            sut.directoryResources = "build/resources/main/";
            sut.directorySources = "src/main/java/";

            var sourcePaths = sut.rawSourceLocations();

            assertPaths(sourcePaths.classSourceBasePaths(),
                    tempDir.resolve("a/build/classes/java/main"),
                    tempDir.resolve("a/build/resources/main"),
                    tempDir.resolve("b/build/classes/java/main"),
                    tempDir.resolve("b/build/resources/main"));
            assertPaths(sourcePaths.javaSourceBasePaths(),
                    tempDir.resolve("a/src/main/java"),
                    tempDir.resolve("b/src/main/java"));
        }


        private void assertPaths(List<Path> actual, Path... expectedAnyOrder) {
            assertEquals(expectedAnyOrder.length, actual.size());

            assertAll(Arrays.stream(expectedAnyOrder).map(expected -> () ->
                    assertTrue(actual.contains(expected),
                            () -> "[%s] がリスト [%s] に含まれていませんでした。".formatted(expected, actual)))
            );
        }

        @Test
        void ドット始まりを無視する() throws IOException {
            // 準備
            Files.createDirectories(tempDir.resolve("build/classes/java/main"));
            Files.createDirectories(tempDir.resolve("build/resources/main"));
            Files.createDirectories(tempDir.resolve("src/main/java"));
            Files.createDirectories(tempDir.resolve(".jig/build/classes/java/main"));
            Files.createDirectories(tempDir.resolve(".jig/build/resources/main"));
            Files.createDirectories(tempDir.resolve(".jig/src/main/java"));

            var sut = new CliConfig();
            // . や .. などの相対記述を使用している場合にドット始まりとして無視されてしまう不具合の検出のため resolve(.) を入れる
            sut.projectPath = tempDir.toAbsolutePath().resolve(".").toString();
            sut.directoryClasses = "classes";
            sut.directoryResources = "resources";
            sut.directorySources = "src";

            var sourcePaths = sut.rawSourceLocations();

            assertPaths(sourcePaths.classSourceBasePaths(),
                    tempDir.resolve("build/classes"),
                    tempDir.resolve("build/resources"));
            assertPaths(sourcePaths.javaSourceBasePaths(),
                    tempDir.resolve("src"));
        }
    }
}