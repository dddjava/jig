package org.dddjava.jig.domain.model.data.packages;

import java.util.Locale;

/**
 * パッケージ数
 */
public record PackageNumber(long value) {

    public String asText() {
        return Long.toString(value);
    }

    public String localizedLabel() {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        return (isEnglish ? "Packages: " : "パッケージ数: ") + value;
    }
}
