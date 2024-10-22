package jig;

import org.gradle.util.GradleVersion;

public enum SupportGradleVersion {
    CURRENT(GradleVersion.current().getVersion()),
    _8("8.10.2");

    private final String version;

    SupportGradleVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
