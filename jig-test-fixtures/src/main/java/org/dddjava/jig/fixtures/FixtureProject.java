package org.dddjava.jig.fixtures;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 配置済みの代表プロジェクト。
 *
 * JIGの入力はクラスファイルとソースの対なので、その両方の在り処を返す。
 */
public final class FixtureProject {

    private final String name;
    private final Path directory;

    FixtureProject(String name, Path directory) {
        this.name = name;
        this.directory = directory;
    }

    /**
     * 指定したJavaバージョンでコンパイルしたクラスファイルの配置先。
     *
     * @param release ビルドで {@code --release} に指定した値
     */
    public Path classes(int release) {
        return existing(directory.resolve("classes-" + release),
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
