package org.dddjava.jig.domain.model.documents;

import java.util.Locale;

/**
 * 解析中に検出した事象を利用者に伝える文字列の i18n を保持する小さなレコード。
 * <p>
 * ビルド時に決まらないため {@code jig-i18n.js} の builtinDictionaries には持てず、
 * ログ出力と、{@link Diagnostic} として生成物に埋め込む出力の両方で使う。
 */
public record LocalizedMessage(String japanese, String english) {

    public String forLocale(Locale locale) {
        return locale.getLanguage().equals("en") ? english : japanese;
    }
}
