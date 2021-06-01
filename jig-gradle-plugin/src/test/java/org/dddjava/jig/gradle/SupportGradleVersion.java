package org.dddjava.jig.gradle;

import org.gradle.util.GradleVersion;

import java.nio.file.Path;

/**
 * テストするGradleのバージョンを列挙
 *
 * 数の分だけGradleを動かすJavaプロセスが立ち上がってしまい、CIでメモリ確保できなくなるため少なくしている。
 * ちゃんとJavaプロセス殺すようにして増やしたい。
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
            return "5.0";
        }
    };

    public abstract String version();

    GradleTaskRunner runner(Path path) {
        return new GradleTaskRunner(this, path);
    }
}
