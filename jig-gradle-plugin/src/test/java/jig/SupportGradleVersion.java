package jig;

import org.gradle.util.GradleVersion;

/**
 * サポートするGradleのバージョン
 */
public enum SupportGradleVersion {
    /**
     * JIGが使用しているGradleのバージョン（最新）
     */
    CURRENT(GradleVersion.current().getVersion()),
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
