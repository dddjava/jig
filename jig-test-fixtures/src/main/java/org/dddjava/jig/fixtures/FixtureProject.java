package org.dddjava.jig.fixtures;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private Path existing(Path path, String description) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException(
                    "代表プロジェクト '" + name + "' に" + description + "がありません: " + path);
        }
        return path;
    }
}
