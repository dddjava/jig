package org.dddjava.jig.infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * {@code <configDirectory>/jig.properties} を {@link PartialJigSettings} に変換するソース。
 *
 * ファイルが存在しない場合や読み込みに失敗した場合は {@link PartialJigSettings#EMPTY} を返す。
 * 各キーのパース・検証と未知キー警告は {@link JigSettingsRawSource} に委譲する（不正値は例外）。
 */
public class PropertiesFileSource {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileSource.class);

    private final Path configDirectory;

    public PropertiesFileSource(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    public PartialJigSettings read() {
        Path jigPropertiesPath = configDirectory.resolve("jig.properties");
        logger.debug("try to load {} ...", jigPropertiesPath.toAbsolutePath());
        if (!Files.exists(jigPropertiesPath)) {
            return PartialJigSettings.EMPTY;
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(jigPropertiesPath)) {
            properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.warn("fail to load {}", jigPropertiesPath, e);
            return PartialJigSettings.EMPTY;
        }
        logger.debug("configuration loaded from {}", jigPropertiesPath.toAbsolutePath());

        Map<String, String> jigProperties = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("jig.")) {
                jigProperties.put(key, properties.getProperty(key));
            }
        }
        return JigSettingsRawSource.parse(jigPropertiesPath.toString(), jigProperties);
    }
}
