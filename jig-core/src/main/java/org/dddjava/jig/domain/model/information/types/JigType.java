package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.members.JigFields;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethods;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * JIGが識別する型
 */
public record JigType(JigTypeHeader jigTypeHeader, JigTypeMembers jigTypeMembers, JigTypeGlossary jigTypeGlossary) {

    public TypeId id() {
        return jigTypeHeader.id();
    }

    public PackageId packageId() {
        return id().packageId();
    }

    public String fqn() {
        return jigTypeHeader.fqn();
    }

    public JigTypeVisibility visibility() {
        return jigTypeHeader.jigTypeAttributes().jigTypeVisibility();
    }

    public Term term() {
        return jigTypeGlossary.typeTerm();
    }

    public String label() {
        return term().title();
    }

    public TypeIds usingTypes() {
        var collect = Stream.concat(
                        jigTypeHeader.containedIds().stream(),
                        jigTypeMembers.toTypeIdStream()
                )
                .distinct()
                // [L からはじまるarrayが別になるのは嬉しくないので。水際的にここで処置しておくが、源泉近くで対応したい。
                .map(TypeId::unarray)
                // java標準型は usingTypes で出てきて嬉しいことはないので取り除く。水際的にここで処置しておくが、源泉近くで対応したい。
                .filter(typeId -> !typeId.isJavaLanguageType())
                .collect(toSet());
        return new TypeIds(collect);
    }

    public boolean hasAnnotation(TypeId typeId) {
        return jigTypeHeader.jigTypeAttributes().declaredAnnotation(typeId);
    }

    public boolean isDeprecated() {
        return hasAnnotation(TypeId.from(Deprecated.class));
    }

    public Optional<String> annotationValueOf(TypeId typeId, String... elementNames) {
        return jigTypeHeader.jigTypeAttributes().declarationAnnotationInstances().stream()
                .filter(annotation -> annotation.id().equals(typeId))
                .flatMap(annotation -> annotation.elements().stream())
                .filter(element -> element.matchName(elementNames))
                .map(element -> element.valueAsString())
                .findFirst();
    }

    public TypeKind typeKind() {
        // 互換のためにTypeKindを返す形を維持するための実装。TypeKindはあまり活用できていないので、別の何かで再定義したい
        return switch (jigTypeHeader.javaTypeDeclarationKind()) {
            case RECORD -> TypeKind.レコード型;
            case ENUM -> jigTypeHeader.jigTypeAttributes().jigTypeModifiers().contains(JigTypeModifier.ABSTRACT)
                    ? TypeKind.抽象列挙型 : TypeKind.列挙型;
            default -> TypeKind.通常型;
        };
    }

    public JigTypeValueKind toValueKind() {
        return JigTypeValueKind.from(this);
    }

    public TypeCategory typeCategory() {
        // TODO カスタムアノテーション対応 https://github.com/dddjava/jig/issues/343
        if (hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Service"))
                || hasAnnotation(TypeId.from(org.dddjava.jig.annotation.Service.class))) {
            return TypeCategory.InputPort;
        }
        if (hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Controller"))
                || hasAnnotation(TypeId.valueOf("org.springframework.web.bind.annotation.RestController"))
                || hasAnnotation(TypeId.valueOf("org.springframework.web.bind.annotation.ControllerAdvice"))
                || hasAnnotation(TypeId.valueOf("org.dddjava.jig.adapter.HandleDocument"))) {
            return TypeCategory.InputAdapter;
        }
        if (hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Repository"))
                || hasAnnotation(TypeId.from(org.dddjava.jig.annotation.Repository.class))) {
            return TypeCategory.OutputAdapter;
        }
        if (hasAnnotation(TypeId.valueOf("org.springframework.stereotype.Component"))) {
            return TypeCategory.OtherApplicationComponent;
        }

        return TypeCategory.Others;
    }

    public Stream<JigMethod> allJigMethodStream() {
        return jigTypeMembers.allJigMethodStream();
    }

    public JigFields instanceJigFields() {
        return new JigFields(jigTypeMembers.instanceFields());
    }

    public boolean hasInstanceField() {
        return instanceJigFields().empty() == false;
    }

    public JigMethods instanceJigMethods() {
        return new JigMethods(jigTypeMembers.instanceMethods());
    }

    public Stream<JigMethod> instanceJigMethodStream() {
        return instanceJigMethods().stream();
    }

    public boolean hasInstanceMethod() {
        return !instanceJigMethods().empty();
    }

    public JigMethods staticJigMethods() {
        return new JigMethods(jigTypeMembers.staticMethods());
    }
}
