package org.dddjava.jig.domain.model.documents;

import java.util.Locale;
import java.util.Map;

/**
 * JigDocument の表示ラベル。サポート言語ごとの文字列を保持する。
 * <p>
 * 言語数を増やすときは {@link #of(String, String)} の引数を増やすか、
 * 直接 {@link Map} 形式のコンストラクタを呼ぶ。{@code labelFor} は日本語をフォールバックとする。
 */
public record JigDocumentLabel(Map<String, String> labelsByLanguage) {

    private static final String FALLBACK_LANGUAGE = "ja";

    public JigDocumentLabel {
        if (!labelsByLanguage.containsKey(FALLBACK_LANGUAGE)) {
            throw new IllegalArgumentException("日本語ラベルは必須です: " + labelsByLanguage);
        }
    }

    public static JigDocumentLabel of(String japanese, String english) {
        return new JigDocumentLabel(Map.of("ja", japanese, "en", english));
    }

    public String labelFor(Locale locale) {
        return labelsByLanguage.getOrDefault(locale.getLanguage(), labelsByLanguage.get(FALLBACK_LANGUAGE));
    }
}
