package org.dddjava.jig;

import java.nio.file.Path;
import java.util.Arrays;

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

        return Arrays.stream(BuildTool.values())
                .filter(buildTool -> buildTool.isUsing(workingDirectory))
                .findAny()
                // 該当しない場合はGradleとして扱う
                .orElse(BuildTool.GRADLE);
    }

    enum BuildTool {
        GRADLE {
            @Override
            boolean isUsing(Path rootPath) {
                return rootPath.resolve("build.gradle").toFile().exists()
                        || rootPath.resolve("build.gradle.kts").toFile().exists();
            }
        },
        MAVEN {
            @Override
            boolean isUsing(Path rootPath) {
                return rootPath.resolve("pom.xml").toFile().exists();
            }
        };

        /**
         * 渡されたパスをもとにこのビルドツールを使用しているか判別する
         */
        abstract boolean isUsing(Path rootPath);
    }

}
