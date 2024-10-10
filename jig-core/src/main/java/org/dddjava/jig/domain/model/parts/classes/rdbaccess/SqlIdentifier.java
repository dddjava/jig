package org.dddjava.jig.domain.model.parts.classes.rdbaccess;

import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.Objects;

/**
 * SQL識別子
 */
public class SqlIdentifier {

    String value;

    public SqlIdentifier(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlIdentifier that = (SqlIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public boolean matches(MethodDeclarations methodDeclarations) {
        if (value.contains(".")) {
            for (MethodDeclaration methodDeclaration : methodDeclarations.list()) {
                boolean matches = methodDeclaration.matches(
                        TypeIdentifier.valueOf(value.substring(0, value.lastIndexOf('.'))),
                        value.substring(value.lastIndexOf('.') + 1)
                );
                if (matches) return true;
            }
        }
        return false;
    }
}
