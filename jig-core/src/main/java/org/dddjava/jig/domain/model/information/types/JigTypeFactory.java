package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.unit.ClassDeclaration;
import org.dddjava.jig.domain.model.data.unit.JigMethodDeclaration;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * {@link JigType} の生成処理
 */
public class JigTypeFactory {

    public static JigTypes createJigTypes(Collection<ClassDeclaration> classDeclarations, Glossary glossary) {
        return classDeclarations.stream()
                .map(classDeclaration -> createJigType(glossary, classDeclaration))
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }

    private static JigType createJigType(Glossary glossary, ClassDeclaration classDeclaration) {
        JigTypeGlossary jigTypeGlossary = JigTypeGlossary.from(glossary, classDeclaration.jigTypeHeader().id());
        JigTypeMembers jigTypeMembers = createJigMember(classDeclaration, jigTypeGlossary);
        return new JigType(classDeclaration.jigTypeHeader(), jigTypeMembers, jigTypeGlossary);
    }

    private static JigTypeMembers createJigMember(ClassDeclaration classDeclaration, JigTypeGlossary jigTypeGlossary) {
        Collection<JigFieldHeader> jigFields = classDeclaration.jigFieldHeaders();
        Collection<JigMethod> jigMethods = classDeclaration.jigMethodDeclarations().stream()
                .map(jigMethodDeclaration -> createJigMethod(jigMethodDeclaration, jigTypeGlossary))
                .toList();
        return new JigTypeMembers(jigFields, jigMethods);
    }

    private static JigMethod createJigMethod(JigMethodDeclaration jigMethodDeclaration, JigTypeGlossary jigTypeGlossary) {
        Term methodTerm = jigTypeGlossary.getMethodTermPossiblyMatches(jigMethodDeclaration.header().id());
        return new JigMethod(jigMethodDeclaration, methodTerm);
    }
}
