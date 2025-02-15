package org.dddjava.jig.domain.model.information;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.unit.ClassDeclaration;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.stream.Collectors;

public class JigInformationFactory {

    public static JigTypes createJigTypes(Collection<ClassDeclaration> classDeclarations, GlossaryRepository glossaryRepository) {
        return classDeclarations.stream()
                .map(classDeclaration -> {
                    return JigType.from(classDeclaration, glossaryRepository.collectJigTypeTerms(classDeclaration.jigTypeHeader().id()));
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }
}
