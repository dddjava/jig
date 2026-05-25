package org.dddjava.jig.infrastructure.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

/**
 * プロパティキー名で引ける生の設定値マップを {@link PartialJigSettings} に変換する共通処理。
 *
 * すべての設定ソース（{@link PropertiesFileSource} / {@link SystemPropertySource} /
 * {@link EnvironmentVariableSource} / CLI 引数）はここを通すことで、入り口によらず
 * 同一のパース・検証（fail-fast）と未知キー警告が適用される。
 *
 * <ul>
 *   <li>値が空文字・空白のキーは「未指定」としてスキップする。</li>
 *   <li>不正な値は {@link JigSettingKey#apply} が例外を投げる（fail-fast）。</li>
 *   <li>{@link JigSettingKey} に存在しないキーは WARN ログを出して無視する。</li>
 * </ul>
 */
public class JigSettingsRawSource {
    private static final Logger logger = LoggerFactory.getLogger(JigSettingsRawSource.class);

    /**
     * @param sourceLabel       警告・例外メッセージに含める設定ソースの表示名
     * @param rawByPropertyKey  プロパティキー名（{@code jig.*}）→ 生文字列 のマップ
     */
    public static PartialJigSettings parse(String sourceLabel, Map<String, String> rawByPropertyKey) {
        PartialJigSettings.Builder builder = PartialJigSettings.builder();
        // 警告順を安定させるためキー昇順で処理する
        new TreeMap<>(rawByPropertyKey).forEach((key, value) -> {
            JigSettingKey settingKey = JigSettingKey.fromPropertyKey(key).orElse(null);
            if (settingKey == null) {
                logger.warn("{}: 未知のキー \"{}\" を無視します（値: {}）。", sourceLabel, key, value);
                return;
            }
            if (value == null || value.isBlank()) {
                return; // 未指定扱い
            }
            try {
                settingKey.apply(builder, value.trim()); // 不正値はここで例外（fail-fast）
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        "%s: 設定 \"%s\" の値 \"%s\" が不正です。".formatted(sourceLabel, key, value), e);
            }
        });
        return builder.build();
    }
}
