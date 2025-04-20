package org.dddjava.jig.infrastructure.javaproductreader;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeGlossary;
import org.dddjava.jig.domain.model.information.types.JigTypeMembers;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.infrastructure.asm.ClassDeclaration;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

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
        var fields = classDeclaration.jigFieldHeaders().stream()
                .map(jigFieldHeader -> JigField.from(jigFieldHeader, jigTypeGlossary.fieldTerm(jigFieldHeader.id())))
                .collect(groupingBy(jigField -> jigField.jigFieldHeader().ownership()));

        enum MethodGrouping {
            INITIALIZER,
            INSTANCE,
            CLASS;
        }
        var methods = classDeclaration.jigMethodDeclarations().stream()
                .map(jigMethodDeclaration -> createJigMethod(jigMethodDeclaration, jigTypeGlossary))
                .collect(groupingBy(jigMethod -> {
                    JigMethodHeader header = jigMethod.jigMethodDeclaration().header();
                    if (header.isStaticOrInstanceInitializer()) {
                        return MethodGrouping.INITIALIZER;
                    }
                    return header.ownership() == JigMemberOwnership.INSTANCE ? MethodGrouping.INSTANCE : MethodGrouping.CLASS;
                }));
        return new JigTypeMembers(
                fields.getOrDefault(JigMemberOwnership.CLASS, List.of()),
                fields.getOrDefault(JigMemberOwnership.INSTANCE, List.of()),
                methods.getOrDefault(MethodGrouping.INITIALIZER, List.of()),
                methods.getOrDefault(MethodGrouping.CLASS, List.of()),
                methods.getOrDefault(MethodGrouping.INSTANCE, List.of())
        );
    }

    private static JigMethod createJigMethod(JigMethodDeclaration jigMethodDeclaration, JigTypeGlossary jigTypeGlossary) {
        Term methodTerm = jigTypeGlossary.getMethodTermPossiblyMatches(jigMethodDeclaration.header().id());
        return new JigMethod(jigMethodDeclaration, methodTerm);
    }
}
