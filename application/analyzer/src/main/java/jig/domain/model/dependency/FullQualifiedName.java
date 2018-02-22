package jig.domain.model.dependency;

import java.util.Objects;

public class FullQualifiedName {

    String value;

    public FullQualifiedName(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullQualifiedName that = (FullQualifiedName) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
