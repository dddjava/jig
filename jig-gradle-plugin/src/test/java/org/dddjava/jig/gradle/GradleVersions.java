package org.dddjava.jig.gradle;

import org.gradle.util.GradleVersion;

public enum GradleVersions {
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
}
