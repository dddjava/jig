package org.dddjava.jig.domain.model.information.method;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.data.classes.method.MethodDerivation;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /**
     * 注目に値するメソッド一覧
     *
     * 「注目に値する」メソッドを可視性順で並べたもの。
     * 主に概要での出力に使用する。
     */
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

    public Stream<JigMethod> stream() {
        return list.stream();
    }
}
