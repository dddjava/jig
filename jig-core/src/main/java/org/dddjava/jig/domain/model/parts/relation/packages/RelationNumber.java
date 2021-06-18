package org.dddjava.jig.domain.model.parts.relation.packages;

import java.util.Locale;

/**
 * 依存関係の数
 */
public class RelationNumber {
    int value;

    public RelationNumber(int value) {
        this.value = value;
    }

    public String asText() {
        return Integer.toString(value);
    }

    public String localizedLabel() {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        return (isEnglish ? "Relations: " : "関連数: ") + value;
    }
}
