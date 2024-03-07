package org.dddjava.jig.infrastructure.configuration;

import java.util.Objects;

/**
 * @deprecated 廃止しました。設定しても使用せず、このクラスは 2024.4.1 以降に削除されます。
 */
@Deprecated(since = "2024.3.1")
public class OutputOmitPrefix {
    String pattern;

    public OutputOmitPrefix(String pattern) {
        this.pattern = pattern;
    }

    public String format(String fullQualifiedName) {
        return fullQualifiedName.replaceFirst(pattern, "");
    }

    @Override
    public String toString() {
        return pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputOmitPrefix that = (OutputOmitPrefix) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }
}
