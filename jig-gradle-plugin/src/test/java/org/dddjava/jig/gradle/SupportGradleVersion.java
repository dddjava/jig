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
    _6 {
        @Override
        public String version() {
            return "6.9.3";
        }
    },
    _5 {
        @Override
        public String version() {
            return "5.6.4";
        }
    };

    public abstract String version();

    GradleTaskRunner runner(Path path) {
        return new GradleTaskRunner(this, path);
    }
}
