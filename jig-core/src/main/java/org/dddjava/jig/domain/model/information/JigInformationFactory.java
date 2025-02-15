package org.dddjava.jig.domain.model.information;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.unit.ClassDeclaration;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.stream.Collectors;

public class JigInformationFactory {

    public static JigTypes createJigTypes(Collection<ClassDeclaration> classDeclarations, GlossaryRepository glossaryRepository) {
        return classDeclarations
                .stream()
                .map(classDeclaration -> {
                    return JigType.from(
                            classDeclaration.jigTypeHeader(),
                            createJigTypeMembers(glossaryRepository, classDeclaration),
                            glossaryRepository.collectJigTypeTerms(classDeclaration.jigTypeHeader().id())
                    );
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }

    private static JigTypeMembers createJigTypeMembers(GlossaryRepository glossaryRepository, ClassDeclaration classDeclaration) {
        Collection<JigMethod> jigMethods = classDeclaration.jigMethodDeclarations().stream()
                .map(jigMethodDeclaration -> new JigMethod(jigMethodDeclaration,
                        glossaryRepository.getMethodTermPossiblyMatches(jigMethodDeclaration.header().id())))
                .toList();
        return new JigTypeMembers(classDeclaration.jigFieldHeaders(), jigMethods);
    }
}
