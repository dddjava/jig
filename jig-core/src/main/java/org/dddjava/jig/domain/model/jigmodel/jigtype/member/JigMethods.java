package org.dddjava.jig.domain.model.jigmodel.jigtype.member;

import org.dddjava.jig.domain.model.parts.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.method.MethodDerivation;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * メソッド一覧
 */
public class JigMethods {
    List<JigMethod> list;

    public JigMethods(List<JigMethod> list) {
        this.list = list;
    }

    public MethodDeclarations declarations() {
        return list.stream().map(JigMethod::declaration).collect(MethodDeclarations.collector());
    }

    public List<JigMethod> listRemarkable() {
        return list.stream()
                .filter(JigMethod::remarkable)
                .sorted(Comparator
                        .comparing(JigMethod::visibility)
                        .thenComparing(jigMethod -> jigMethod.declaration().asFullNameText()))
                .collect(Collectors.toList());
    }

    public List<JigMethod> list() {
        return list.stream()
                .sorted(Comparator
                        .comparing(JigMethod::visibility)
                        .thenComparing(jigMethod -> jigMethod.declaration().asFullNameText()))
                .collect(Collectors.toList());
    }

    public JigMethod get(MethodDeclaration methodDeclaration) {
        for (JigMethod method : list) {
            if (method.declaration().sameIdentifier(methodDeclaration)) {
                return method;
            }
        }
        throw new NoSuchElementException(methodDeclaration.asFullNameText());
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public List<TypeIdentifier> listUsingTypes() {
        return list.stream()
                .flatMap(jigMethod -> jigMethod.usingTypes().list().stream())
                .collect(Collectors.toList());
    }

    public JigMethods filterProgrammerDefined() {
        return new JigMethods(list.stream()
                .filter(jigMethod -> MethodDerivation.PROGRAMMER == jigMethod.derivation())
                .collect(Collectors.toList()));
    }

    public JigMethods excludeNotNoteworthyObjectMethod() {
        return new JigMethods(list.stream()
                .filter(jigMethod -> !jigMethod.objectMethod() || jigMethod.documented())
                .collect(Collectors.toList()));
    }
}
