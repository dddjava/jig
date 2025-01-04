package org.dddjava.jig;

import java.nio.file.Path;

public record JigOptions(
        String domainPattern,
        BuildTool buildTool,
        Path workingDirectory,
        Path outputDirectory
) {
    public JigOptions() {
        this(".+(\\.domain\\.).+", null, Path.of(""), Path.of(""));
    }

    BuildTool resolveBuildTool() {
        if (buildTool != null) return buildTool;

        for (var buildTool : BuildTool.values()) {
            if (buildTool.isUsing(workingDirectory)) {
                return buildTool;
            }
        }

        return BuildTool.GRADLE;
    }

    enum BuildTool {
        GRADLE {
            @Override
            boolean isUsing(Path workingDirectory) {
                return workingDirectory.resolve("build.gradle").toFile().exists()
                        || workingDirectory.resolve("build.gradle.kts").toFile().exists();
            }
        },
        MAVEN {
            @Override
            boolean isUsing(Path workingDirectory) {
                return workingDirectory.resolve("pom.xml").toFile().exists();
            }
        };

        abstract boolean isUsing(Path workingDirectory);
    }

}
