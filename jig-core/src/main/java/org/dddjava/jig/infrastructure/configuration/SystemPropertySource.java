package org.dddjava.jig.infrastructure.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * システムプロパティ（{@code -Djig.*}）を {@link PartialJigSettings} に変換するソース。
 *
 * {@code jig.} で始まるキーのみを対象とし、パース・検証は {@link JigSettingsRawSource} に委譲する。
 * テスト容易性のため、対象とするプロパティ集合をコンストラクタで受け取る。
 */
public class SystemPropertySource {

    private final Map<String, String> systemProperties;

    public SystemPropertySource(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public static SystemPropertySource fromSystem() {
        Properties properties = System.getProperties();
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
        return new SystemPropertySource(map);
    }

    public PartialJigSettings read() {
        Map<String, String> jigProperties = new LinkedHashMap<>();
        systemProperties.forEach((key, value) -> {
            if (key.startsWith("jig.")) {
                jigProperties.put(key, value);
            }
        });
        return JigSettingsRawSource.parse("-D（システムプロパティ）", jigProperties);
    }
}
