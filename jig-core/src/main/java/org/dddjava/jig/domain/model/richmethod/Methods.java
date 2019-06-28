package org.dddjava.jig.domain.model.richmethod;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCodes;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;

/**
 * メソッド一覧
 */
public class Methods {
    List<Method> list;

    public Methods(TypeByteCodes typeByteCodes) {
        List<Method> list = typeByteCodes.instanceMethodByteCodes().stream()
                .map(methodByteCode -> new Method(methodByteCode))
                .collect(toList());
        this.list = list;
    }

    public MethodDeclarations declarations() {
        return list.stream().map(Method::declaration).collect(MethodDeclarations.collector());
    }

    public List<Method> list() {
        list.sort(Comparator.comparing(method -> method.declaration().asFullNameText()));
        return list;
    }

    public Method get(MethodDeclaration methodDeclaration) {
        for (Method method : list) {
            if (method.declaration().sameIdentifier(methodDeclaration)) {
                return method;
            }
        }
        throw new NoSuchElementException(methodDeclaration.asFullNameText());
    }
}
