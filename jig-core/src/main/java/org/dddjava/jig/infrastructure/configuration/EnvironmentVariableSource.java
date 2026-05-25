package org.dddjava.jig.infrastructure.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 環境変数（{@code JIG_*}）を {@link PartialJigSettings} に変換するソース。
 *
 * {@code JIG_OUTPUT_DIRECTORY} のような名前を {@link JigSettingKey#environmentVariableName()} で
 * プロパティキーへ逆マッピングしてからパースする。既知のキーに対応しない {@code JIG_*} は
 * 生の環境変数名のまま渡し、{@link JigSettingsRawSource} が未知キーとして警告する。
 *
 * テスト容易性のため、対象とする環境変数集合をコンストラクタで受け取る。
 */
public class EnvironmentVariableSource {

    private final Map<String, String> environmentVariables;

    public EnvironmentVariableSource(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public static EnvironmentVariableSource fromSystem() {
        return new EnvironmentVariableSource(System.getenv());
    }

    public PartialJigSettings read() {
        Map<String, String> rawByPropertyKey = new LinkedHashMap<>();
        environmentVariables.forEach((name, value) -> {
            if (!name.startsWith("JIG_")) {
                return;
            }
            String key = JigSettingKey.fromEnvironmentVariableName(name)
                    .map(JigSettingKey::propertyKey)
                    .orElse(name); // 未知の JIG_* は生名のまま渡し、パーサ側で警告させる
            rawByPropertyKey.put(key, value);
        });
        return JigSettingsRawSource.parse("環境変数", rawByPropertyKey);
    }
}
