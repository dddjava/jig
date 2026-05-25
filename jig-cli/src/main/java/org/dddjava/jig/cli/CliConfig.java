package org.dddjava.jig.cli;

import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.dddjava.jig.infrastructure.configuration.Configuration;
import org.dddjava.jig.infrastructure.configuration.JigSettingKey;
import org.dddjava.jig.infrastructure.configuration.JigSettingsLoader;
import org.dddjava.jig.infrastructure.configuration.JigSettingsRawSource;
import org.dddjava.jig.infrastructure.configuration.PartialJigSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
class CliConfig {
    private static final Logger logger = LoggerFactory.getLogger(CliConfig.class);

    private final ApplicationArguments applicationArguments;

    @Value("${project.path}")
    String projectPath;
    @Value("${directory.classes:}")
    String directoryClasses;
    @Value("${directory.resources:}")
    String directoryResources;
    @Value("${directory.sources:}")
    String directorySources;

    CliConfig(ApplicationArguments applicationArguments) {
        this.applicationArguments = applicationArguments;
    }

    Configuration configuration() {
        // 明示層は真の CLI 引数（--jig.*）のみ。-D / 環境変数はコアの設定ソースが担う。
        PartialJigSettings explicit = JigSettingsRawSource.parse("CLI 引数", explicitFromArgs());
        // jig.properties 等で上書きされない場合の最終的な既定値（CLI ランタイム固有）。
        PartialJigSettings cliDefaults = PartialJigSettings.builder()
                .outputDirectory(Path.of("./build/jig"))
                .build();
        return Configuration.from(JigSettingsLoader.loadStandard(explicit, cliDefaults));
    }

    private Map<String, String> explicitFromArgs() {
        Map<String, String> raw = new LinkedHashMap<>();
        for (JigSettingKey key : JigSettingKey.values()) {
            List<String> values = applicationArguments.getOptionValues(key.propertyKey());
            if (values != null && !values.isEmpty()) {
                raw.put(key.propertyKey(), values.get(values.size() - 1)); // 複数指定時は最後を採用
            }
        }
        return raw;
    }

    SourceBasePaths rawSourceLocations() {
        try {
            Path projectRoot = Paths.get(projectPath).toAbsolutePath().normalize();

            if (directoryClasses.isEmpty() && directoryResources.isEmpty() && directorySources.isEmpty()) {
                if (Files.exists(projectRoot.resolve("pom.xml"))) {
                    logger.info("pom.xml が検出されたため、Maven構成で読み取ります。");
                    directoryClasses = "target/classes";
                    directoryResources = "target/classes";
                    directorySources = "src/main/java";
                }
            }
            // デフォルトの設定
            directoryClasses = getOrDefault(directoryClasses, "build/classes/java/main");
            directoryResources = getOrDefault(directoryResources, "build/resources/main");
            directorySources = getOrDefault(directorySources, "src/main/java");

            DirectoryCollector sourcesCollector = new DirectoryCollector(
                    Paths.get(directoryClasses),
                    Paths.get(directoryResources),
                    Paths.get(directorySources));
            Files.walkFileTree(projectRoot, sourcesCollector);

            return sourcesCollector.toSourcePaths();
        } catch (IOException e) {
            // TODO エラーメッセージ。たとえばルートパスの指定が変な時とかはここにくる。
            throw new UncheckedIOException(e);
        }
    }

    private String getOrDefault(String currentValue, String defaultValue) {
        if (currentValue.isEmpty()) {
            return defaultValue;
        }
        return currentValue;
    }
}
