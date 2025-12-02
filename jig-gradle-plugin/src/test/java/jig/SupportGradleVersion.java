package jig;

import org.gradle.util.GradleVersion;

/**
 * サポートするGradleのバージョン
 */
public enum SupportGradleVersion {
    /**
     * JIGが使用しているGradleのバージョン　
     */
    CURRENT(GradleVersion.current().getVersion()),
    /**
     * 最新
     */
    LATEST("9.2.1"),
    /**
     * 一つ前のメジャーバージョンの最終
     */
    PREVIOUS_MAJOR_LATEST("8.14.3");

    private final String version;

    SupportGradleVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
