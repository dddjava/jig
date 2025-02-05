package org.dddjava.jig.domain.model.data.classes.type;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeModifier;
import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.javasources.comment.ClassComment;
import org.dddjava.jig.domain.model.sources.javasources.comment.Comment;

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

    private final JigStaticMember jigStaticMember;
    private final JigInstanceMember jigInstanceMember;

    public JigType(JigTypeHeader jigTypeHeader, JigTypeTerms jigTypeTerms, JigStaticMember jigStaticMember, JigInstanceMember jigInstanceMember) {
        this.jigTypeHeader = jigTypeHeader;
        this.jigTypeTerms = jigTypeTerms;
        this.jigStaticMember = jigStaticMember;
        this.jigInstanceMember = jigInstanceMember;
    }

    public static JigType from(JigTypeHeader jigTypeHeader, JigStaticMember jigStaticMember, JigInstanceMember jigInstanceMember, JigTypeTerms jigTypeTerms) {
        return new JigType(jigTypeHeader, jigTypeTerms, jigStaticMember, jigInstanceMember);
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
        return classComment().asTextOrIdentifierSimpleText();
    }

    public JigTypeDescription description() {
        return jigTypeTerms.jigTypeDescription();
    }

    public JigMethods instanceMethods() {
        return instanceMember().instanceMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod();
    }

    @Deprecated // ドキュメントでフィールドのアノテーションを参照するためにinstanceJigFieldsに乗り換える
    public FieldDeclarations instanceFields() {
        return instanceJigFields().fieldDeclarations();
    }

    public JigFields instanceJigFields() {
        return instanceMember().instanceFields();
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
                .flatMap(annotation -> annotation.elementValueData().stream())
                .filter(element -> element.matchName(elementNames))
                .map(element -> element.valueAsString())
                .findFirst();
    }

    public Stream<JigMethod> allJigMethodStream() {
        return Stream.concat(
                instanceMember().jigMethodStream(),
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

    public ClassComment classComment() {
        // ClassCommentはなくしたいが一旦Termから再構築する
        Optional<Term> typeTerm = jigTypeTerms.typeTerm();

        return new ClassComment(
                jigTypeHeader.id(),
                typeTerm.map(term -> Comment.from(term.title(), term.description()))
                        .orElse(Comment.empty())
        );
    }

    public String nodeLabel() {
        return classComment().nodeLabel();
    }

    public String nodeLabel(String delimiter) {
        return classComment().nodeLabel(delimiter);
    }
}
