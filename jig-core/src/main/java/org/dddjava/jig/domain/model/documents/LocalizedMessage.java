package org.dddjava.jig.domain.model.documents;

import java.util.Locale;

/**
 * 解析中に検出した事象を利用者に伝えるメッセージ
 */
public record LocalizedMessage(String japanese, String english) {

    public String forLocale(Locale locale) {
        return locale.getLanguage().equals("en") ? english : japanese;
    }
}
