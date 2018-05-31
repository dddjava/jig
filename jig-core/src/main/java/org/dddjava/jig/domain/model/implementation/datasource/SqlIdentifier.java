package org.dddjava.jig.domain.model.implementation.datasource;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;

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

    public boolean matches(MethodDeclaration identifier) {
        // メソッド名から引数部分を除去してマッチングする
        String nameString = identifier.asFullText();
        String substring = nameString.substring(0, nameString.indexOf('('));
        return substring.equals(value);
    }

    public boolean matches(MethodDeclarations methodDeclarations) {
        for (MethodDeclaration methodDeclaration : methodDeclarations.list()) {
            boolean matches = matches(methodDeclaration);
            if (matches) return true;
        }
        return false;
    }
}
