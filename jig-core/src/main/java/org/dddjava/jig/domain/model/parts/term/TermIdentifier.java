package org.dddjava.jig.domain.model.parts.term;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用語の識別子
 */
public class TermIdentifier {

    final String value;

    public TermIdentifier(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermIdentifier that = (TermIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String asText() {
        return value;
    }

    public String simpleText() {
        Pattern pattern = Pattern.compile("([^.]+\\.)*([^.()]+)(\\(.*\\))?");
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            String name = matcher.group(2);
            return name;
        }
        return "";
    }
}
