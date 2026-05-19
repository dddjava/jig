package org.dddjava.jig.domain.model.documents;

import java.util.Locale;

/**
 * サーバ側でログや警告として出力する文字列の i18n を保持する小さなレコード。
 * <p>
 * クライアントサイドの翻訳辞書（{@code jig-i18n.js} の builtinDictionaries）には載らない、
 * Java から直接 stdout/stderr/ログに書く文言を対象とする。
 */
public record LocalizedMessage(String japanese, String english) {

    public String forLocale(Locale locale) {
        return locale.getLanguage().equals("en") ? english : japanese;
    }
}
