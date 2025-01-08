package org.dddjava.jig.domain.model.data.packages;

import java.util.Locale;

/**
 * パッケージ数
 */
public class PackageNumber {
    long value;

    public PackageNumber(long value) {
        this.value = value;
    }

    public String asText() {
        return Long.toString(value);
    }

    public String localizedLabel() {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        return (isEnglish ? "Packages: " : "パッケージ数: ") + value;
    }
}
