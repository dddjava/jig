package org.dddjava.jig.fixtures;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 代表プロジェクトの配置を解決する。
 *
 * 利用側がビルドレイアウトを前提にパスを組み立てないよう、入力の解決はこのAPIへ集約する。
 * 配置先はビルドがシステムプロパティ {@value #ROOT_PROPERTY} で渡す。
 */
public final class JigFixtures {

    static final String ROOT_PROPERTY = "jig.fixtures.root";

    private JigFixtures() {
    }

    /**
     * @param name projects/ 配下のディレクトリ名
     */
    public static FixtureProject project(String name) {
        Path directory = root().resolve(name);
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException(
                    "代表プロジェクト '" + name + "' が配置されていません: " + directory
                            + " / jig-test-fixtures の fixtures タスクに依存していますか？");
        }
        return new FixtureProject(name, directory);
    }

    private static Path root() {
        String configured = System.getProperty(ROOT_PROPERTY);
        if (configured == null) {
            throw new IllegalStateException(
                    "システムプロパティ " + ROOT_PROPERTY + " が未設定です。"
                            + "fixtureを使うテストタスクへ jig-test-fixtures の配置先を渡してください。");
        }
        return Paths.get(configured);
    }
}
