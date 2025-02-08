package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * JIGが識別する型
 */
public class JigType {
    private final JigTypeHeader jigTypeHeader;
    private final JigTypeTerms jigTypeTerms;
    private final JigTypeMembers jigTypeMembers;

    private final JigStaticMember jigStaticMember;
    private final JigInstanceMember jigInstanceMember;

    private JigType(JigTypeHeader jigTypeHeader, JigTypeTerms jigTypeTerms, JigTypeMembers jigTypeMembers) {
        this.jigTypeHeader = jigTypeHeader;
        this.jigTypeTerms = jigTypeTerms;
        this.jigTypeMembers = jigTypeMembers;
        this.jigStaticMember = jigTypeMembers.jigStaticMember();
        this.jigInstanceMember = jigTypeMembers.jigInstanceMember();
    }

    public static JigType from(JigTypeHeader jigTypeHeader, JigTypeMembers jigTypeMembers, JigTypeTerms jigTypeTerms) {
        return new JigType(jigTypeHeader, jigTypeTerms, jigTypeMembers);
    }

    public TypeIdentifier identifier() {
        return TypeIdentifier.valueOf(jigTypeHeader.id().value());
    }

    // HeaderのほうもId<JigType>にする？
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

    public JigInstanceMember instanceMember() {
        return jigInstanceMember;
    }

    public JigStaticMember staticMember() {
        return jigStaticMember;
    }

    public TypeIdentifiers usingTypes() {
        Set<TypeIdentifier> set = new HashSet<>();
        set.addAll(jigTypeHeader.containedIds());
        set.addAll(jigStaticMember.listUsingTypes());
        set.addAll(jigInstanceMember.listUsingTypes());
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
        return jigTypeTerms.typeTerm().title();
    }

    public Term term() {
        return jigTypeTerms.typeTerm();
    }

    public JigTypeMembers jigTypeMembers() {
        return jigTypeMembers;
    }

    public JigFields instanceJigFields() {
        return jigTypeMembers().instanceFields();
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
        return jigTypeTerms.markedCore();
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
                || hasAnnotation(TypeIdentifier.from(org.dddjava.jig.adapter.HandleDocument.class))) {
            return TypeCategory.InputAdapter;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Repository"))
                || hasAnnotation(TypeIdentifier.from(org.dddjava.jig.annotation.Repository.class))) {
            return TypeCategory.OutputAdapter;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Component"))) {
            return TypeCategory.BoundaryComponent;
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
        return jigTypeTerms.typeTerm().textWithDelimiter(delimiter);
    }

    public boolean hasInstanceField() {
        return jigTypeMembers().instanceFields().empty() == false;
    }

    public boolean hasInstanceMethod() {
        return jigTypeMembers().jigInstanceMember().hasMethod();
    }

    public Stream<JigMethod> instanceJigMethodStream() {
        return instanceMember().jigMethodStream();
    }

    public JigMethods instanceJigMethods() {
        return instanceMember().instanceMethods();
    }

    // htmlでの出力のためにフィルタリングしている
    public JigMethods instanceMethods() {
        return instanceMember().instanceMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod();
    }
}
