package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMethodFlag;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.data.unit.ClassDeclaration;
import org.dddjava.jig.domain.model.information.members.JigFields;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethods;

import java.util.*;
import java.util.stream.Stream;

/**
 * JIGが識別する型
 */
public class JigType {
    private final JigTypeHeader jigTypeHeader;
    private final JigTypeGlossary jigTypeGlossary;
    private final JigTypeMembers jigTypeMembers;

    private final JigStaticMember jigStaticMember;

    private JigType(JigTypeHeader jigTypeHeader, JigTypeMembers jigTypeMembers, JigTypeGlossary jigTypeGlossary) {
        this.jigTypeGlossary = jigTypeGlossary;
        this.jigTypeHeader = jigTypeHeader;
        this.jigTypeMembers = jigTypeMembers;

        this.jigStaticMember = new JigStaticMember(
                // コンストラクタ
                jigTypeMembers.jigMethods().stream()
                        .filter(jigMethod -> jigMethod.jigMethodDeclaration().header().jigMethodAttribute().flags().contains(JigMethodFlag.INITIALIZER))
                        .toList(),
                // staticメソッド、staticイニシャライザ
                jigTypeMembers.jigMethods().stream()
                        .filter(jigMethod -> jigMethod.jigMethodDeclaration().header().ownership() == JigMemberOwnership.CLASS)
                        .toList()
        );
    }

    public static JigType from(ClassDeclaration classDeclaration, Glossary glossary) {
        JigTypeGlossary jigTypeGlossary = JigTypeGlossary.from(glossary, classDeclaration.jigTypeHeader().id());

        Collection<JigMethod> jigMethods = classDeclaration.jigMethodDeclarations().stream()
                .map(jigMethodDeclaration -> new JigMethod(jigMethodDeclaration,
                        jigTypeGlossary.getMethodTermPossiblyMatches(jigMethodDeclaration.header().id())))
                .toList();

        return new JigType(classDeclaration.jigTypeHeader(), new JigTypeMembers(classDeclaration.jigFieldHeaders(), jigMethods), jigTypeGlossary);
    }

    public TypeIdentifier identifier() {
        return TypeIdentifier.valueOf(jigTypeHeader.id().value());
    }

    public TypeIdentifier id() {
        return jigTypeHeader.id();
    }

    public JigTypeHeader jigTypeHeader() {
        return jigTypeHeader;
    }

    public TypeKind typeKind() {
        // 互換のためにTypeKindを返す形を維持するための実装。TypeKindはあまり活用できていないので、別の何かで再定義したい
        return switch (jigTypeHeader.jigTypeKind()) {
            case RECORD -> TypeKind.レコード型;
            case ENUM -> jigTypeHeader.jigTypeAttributeData().jigTypeModifiers().contains(JigTypeModifier.ABSTRACT)
                    ? TypeKind.抽象列挙型 : TypeKind.列挙型;
            default -> TypeKind.通常型;
        };
    }

    public JigTypeVisibility visibility() {
        return jigTypeHeader.jigTypeAttributeData().jigTypeVisibility();
    }

    public JigStaticMember staticMember() {
        return jigStaticMember;
    }

    public TypeIdentifiers usingTypes() {
        Set<TypeIdentifier> set = new HashSet<>();
        set.addAll(jigTypeHeader.containedIds());
        set.addAll(jigTypeMembers.allTypeIdentifierSet());
        return new TypeIdentifiers(new ArrayList<>(set));
    }

    public PackageIdentifier packageIdentifier() {
        return identifier().packageIdentifier();
    }

    public String simpleName() {
        return jigTypeHeader.simpleName();
    }

    public String fqn() {
        return jigTypeHeader.fqn();
    }

    public String label() {
        return jigTypeGlossary.typeTerm().title();
    }

    public Term term() {
        return jigTypeGlossary.typeTerm();
    }

    public JigTypeMembers jigTypeMembers() {
        return jigTypeMembers;
    }

    public JigFields instanceJigFields() {
        return jigTypeMembers.instanceFields();
    }

    public JigMethods staticMethods() {
        return staticMember().staticMethods().filterProgrammerDefined();
    }

    public JigTypeValueKind toValueKind() {
        return JigTypeValueKind.from(this);
    }

    public boolean hasAnnotation(TypeIdentifier typeIdentifier) {
        return jigTypeHeader.jigTypeAttributeData().declaredAnnotation(typeIdentifier);
    }

    public boolean markedCore() {
        return jigTypeGlossary.markedCore();
    }

    public boolean isDeprecated() {
        return hasAnnotation(TypeIdentifier.from(Deprecated.class));
    }

    public Optional<String> annotationValueOf(TypeIdentifier typeIdentifier, String... elementNames) {
        return jigTypeHeader.jigTypeAttributeData().declarationAnnotationInstances().stream()
                .filter(annotation -> annotation.id().equals(typeIdentifier))
                .flatMap(annotation -> annotation.elements().stream())
                .filter(element -> element.matchName(elementNames))
                .map(element -> element.valueAsString())
                .findFirst();
    }

    public Stream<JigMethod> allJigMethodStream() {
        return Stream.concat(
                instanceJigMethodStream(),
                staticMember().jigMethodStream());
    }

    public TypeCategory typeCategory() {
        // TODO カスタムアノテーション対応 https://github.com/dddjava/jig/issues/343
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Service"))
                || hasAnnotation(TypeIdentifier.from(org.dddjava.jig.annotation.Service.class))) {
            return TypeCategory.Usecase;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Controller"))
                || hasAnnotation(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RestController"))
                || hasAnnotation(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.ControllerAdvice"))
                || hasAnnotation(TypeIdentifier.valueOf("org.dddjava.jig.adapter.HandleDocument"))) {
            return TypeCategory.InputAdapter;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Repository"))
                || hasAnnotation(TypeIdentifier.from(org.dddjava.jig.annotation.Repository.class))) {
            return TypeCategory.OutputAdapter;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Component"))) {
            return TypeCategory.OtherApplicationComponent;
        }

        return TypeCategory.Others;
    }

    public TypeIdentifier typeIdentifier() {
        return identifier();
    }

    public String nodeLabel() {
        return nodeLabel("\\n");
    }

    public String nodeLabel(String delimiter) {
        return jigTypeGlossary.typeTerm().textWithDelimiter(delimiter);
    }

    public boolean hasInstanceField() {
        return jigTypeMembers.instanceFields().empty() == false;
    }

    public boolean hasInstanceMethod() {
        return !instanceJigMethods().empty();
    }

    public Stream<JigMethod> instanceJigMethodStream() {
        return instanceJigMethods().stream();
    }

    public JigMethods instanceJigMethods() {
        return jigTypeMembers.instanceMethods();
    }

    public JigMethods staticJigMethods() {
        return jigTypeMembers.staticMethods();
    }
}
