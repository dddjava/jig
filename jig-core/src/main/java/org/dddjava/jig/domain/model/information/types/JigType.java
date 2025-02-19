package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.members.JigFields;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethods;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JIGが識別する型
 */
public class JigType {
    private final JigTypeHeader jigTypeHeader;
    private final JigTypeMembers jigTypeMembers;
    private final JigTypeGlossary jigTypeGlossary;

    public JigType(JigTypeHeader jigTypeHeader, JigTypeMembers jigTypeMembers, JigTypeGlossary jigTypeGlossary) {
        this.jigTypeGlossary = jigTypeGlossary;
        this.jigTypeHeader = jigTypeHeader;
        this.jigTypeMembers = jigTypeMembers;
    }

    public TypeIdentifier id() {
        return jigTypeHeader.id();
    }

    public PackageIdentifier packageIdentifier() {
        return id().packageIdentifier();
    }

    public String fqn() {
        return jigTypeHeader.fqn();
    }

    public JigTypeHeader jigTypeHeader() {
        return jigTypeHeader;
    }

    public JigTypeVisibility visibility() {
        return jigTypeHeader.jigTypeAttributeData().jigTypeVisibility();
    }

    public Term term() {
        return jigTypeGlossary.typeTerm();
    }

    public String label() {
        return term().title();
    }

    public TypeIdentifiers usingTypes() {
        var collect = Stream.concat(
                        jigTypeHeader.containedIds().stream(),
                        jigTypeMembers.allTypeIdentifierSet().stream()
                )
                // [L からはじまるarrayが別になるのは嬉しくないので。水際的にここで処置しておくが、源泉近くで対応したい。
                .map(TypeIdentifier::unarray)
                // java標準型は usingTypes で出てきて嬉しいことはないので取り除く。水際的にここで処置しておくが、源泉近くで対応したい。
                .filter(typeIdentifier -> !typeIdentifier.isJavaLanguageType())
                .collect(Collectors.toSet());
        return new TypeIdentifiers(collect);
    }

    public boolean hasAnnotation(TypeIdentifier typeIdentifier) {
        return jigTypeHeader.jigTypeAttributeData().declaredAnnotation(typeIdentifier);
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

    public TypeKind typeKind() {
        // 互換のためにTypeKindを返す形を維持するための実装。TypeKindはあまり活用できていないので、別の何かで再定義したい
        return switch (jigTypeHeader.jigTypeKind()) {
            case RECORD -> TypeKind.レコード型;
            case ENUM -> jigTypeHeader.jigTypeAttributeData().jigTypeModifiers().contains(JigTypeModifier.ABSTRACT)
                    ? TypeKind.抽象列挙型 : TypeKind.列挙型;
            default -> TypeKind.通常型;
        };
    }

    public JigTypeValueKind toValueKind() {
        return JigTypeValueKind.from(this);
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

    public Stream<JigMethod> allJigMethodStream() {
        return jigTypeMembers.jigMethods().stream();
    }

    public JigTypeMembers jigTypeMembers() {
        return jigTypeMembers;
    }

    public JigFields instanceJigFields() {
        return jigTypeMembers.instanceFields();
    }

    public boolean hasInstanceField() {
        return jigTypeMembers.instanceFields().empty() == false;
    }

    public JigMethods instanceJigMethods() {
        return jigTypeMembers.instanceMethods();
    }

    public Stream<JigMethod> instanceJigMethodStream() {
        return instanceJigMethods().stream();
    }

    public boolean hasInstanceMethod() {
        return !instanceJigMethods().empty();
    }

    public JigMethods staticJigMethods() {
        return jigTypeMembers.staticMethods();
    }
}
