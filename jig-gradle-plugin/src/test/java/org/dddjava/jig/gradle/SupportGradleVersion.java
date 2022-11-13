package org.dddjava.jig.gradle;

import org.gradle.util.GradleVersion;

import java.nio.file.Path;

/**
 * テストするGradleのバージョンを列挙
 *
 * 各メジャーバージョンのMAXを使用する。
 */
public enum SupportGradleVersion {
    CURRENT {
        @Override
        public String version() {
            return GradleVersion.current().getVersion();
        }
    },
    MIN {
        @Override
        public String version() {
            return "6.9.3";
        }
    };

    public abstract String version();

    GradleTaskRunner runner(Path path) {
        return new GradleTaskRunner(this, path);
    }
}
