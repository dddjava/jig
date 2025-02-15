package org.dddjava.jig.domain.model.information;

import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.unit.ClassDeclaration;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.stream.Collectors;

public class JigInformationFactory {

    public static JigTypes createJigTypes(Collection<ClassDeclaration> classDeclarations, Glossary glossary) {
        return classDeclarations.stream()
                .map(classDeclaration -> {
                    return JigType.from(classDeclaration, glossary);
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }
}
