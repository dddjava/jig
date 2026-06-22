package jig;

import org.gradle.util.GradleVersion;

/**
 * サポートするGradleのバージョン
 */
public enum SupportGradleVersion {
    /**
     * JIGが使用しているGradleのバージョン（最新）
     */
    CURRENT(GradleVersion.current().getVersion(), Integer.MAX_VALUE),
    /**
     * 一つ前のメジャーバージョンの最終。Gradle 8.x は Java 24 まで対応。
     */
    PREVIOUS_MAJOR_LATEST("8.14.3", 24);

    private final String version;
    private final int maxJavaVersion;

    SupportGradleVersion(String version, int maxJavaVersion) {
        this.version = version;
        this.maxJavaVersion = maxJavaVersion;
    }

    public String getVersion() {
        return version;
    }

    public boolean isSupportedOnCurrentJre() {
        return Runtime.version().feature() <= maxJavaVersion;
    }
}
