package org.dddjava.jig.domain.model.information.method;


import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;

import java.util.stream.Stream;

/**
 * メソッドが使用しているメソッド一覧
 */
public class UsingMethods {
    MethodDeclarations list;

    private UsingMethods(MethodDeclarations list) {
        this.list = list;
    }

    static UsingMethods from(Instructions instructions) {
        return new UsingMethods(instructions.instructMethods());
    }

    public boolean containsStream() {
        return list.list().stream()
                .map(MethodDeclaration::methodReturn)
                .anyMatch(MethodReturn::isStream);
    }

    public MethodDeclarations methodDeclarations() {
        return list;
    }

    public Stream<MethodDeclaration> stream() {
        return list.list().stream();
    }
}
