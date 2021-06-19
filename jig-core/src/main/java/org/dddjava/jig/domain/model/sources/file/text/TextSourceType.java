package org.dddjava.jig.domain.model.sources.file.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TextSourceType {
    JAVA_PACKAGE_INFO("package-info\\.java"),
    JAVA(".+\\.java"),
    KOTLIN(".+\\.kt"),
    SCALA(".+\\.scala"),
    UNSUPPORTED("");

    private final Pattern pattern;

    TextSourceType(String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public static TextSourceType from(String fileName) {
        for (TextSourceType value : values()) {
            if (value == UNSUPPORTED) return value;
            Matcher matcher = value.pattern.matcher(fileName);
            if (matcher.matches()) {
                return value;
            }
        }
        return UNSUPPORTED;
    }
}
