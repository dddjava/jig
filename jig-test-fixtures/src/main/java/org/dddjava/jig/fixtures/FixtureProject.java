package org.dddjava.jig.fixtures;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 配置済みの代表プロジェクト。
 *
 * JIGの入力はクラスファイルとソースの対なので、その両方の在り処を返す。
 */
public final class FixtureProject {

    private static final String CLASSES_PREFIX = "classes-";

    private final String name;
    private final Path directory;

    FixtureProject(String name, Path directory) {
        this.name = name;
        this.directory = directory;
    }

    /**
     * 配置済みのクラスファイルのバージョン。
     *
     * 生成するバージョンはビルド環境で変わりうるため、利用側は想定を持たずにここから得る。
     */
    public List<Integer> availableReleases() {
        Path releasesFile = directory.resolve("releases.txt");
        if (!Files.isRegularFile(releasesFile)) {
            throw new IllegalStateException(
                    "代表プロジェクト '" + name + "' の配置が完了していません: " + releasesFile);
        }
        try (Stream<String> lines = Files.lines(releasesFile)) {
            return lines
                    .filter(line -> !line.isBlank())
                    .map(line -> Integer.valueOf(line.trim()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("代表プロジェクト '" + name + "' を読み取れません", e);
        }
    }

    /**
     * 指定したJavaバージョンでコンパイルしたクラスファイルの配置先。
     *
     * @param release ビルドで {@code --release} に指定した値
     */
    public Path classes(int release) {
        return existing(directory.resolve(CLASSES_PREFIX + release),
                "Java " + release + " のクラスファイル");
    }

    /**
     * ソースの配置先。JIGはJavadocをここから読む。
     */
    public Path sources() {
        return existing(directory.resolve("sources"), "ソース");
    }

    /**
     * 利用者のプロジェクトと同じレイアウトへ展開する。
     *
     * CLI と Gradle プラグインは `build/classes/java/main` と `src/main/java` を既定で探すため、
     * 別プロセスから起動する検証はこの形の入力を必要とする。
     *
     * @return 展開先ディレクトリ
     */
    public Path deployTo(Path targetDirectory, int release) {
        copyRecursively(classes(release), targetDirectory.resolve("build/classes/java/main"));
        copyRecursively(sources(), targetDirectory.resolve("src/main/java"));
        return targetDirectory;
    }

    private void copyRecursively(Path from, Path to) {
        try (Stream<Path> paths = Files.walk(from)) {
            for (Path path : paths.collect(Collectors.toList())) {
                Path destination = to.resolve(from.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.createDirectories(destination.getParent());
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("代表プロジェクト '" + name + "' を展開できません: " + to, e);
        }
    }

    private Path existing(Path path, String description) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(
                    "代表プロジェクト '" + name + "' に" + description + "がありません: " + path);
        }
        return path;
    }
}
