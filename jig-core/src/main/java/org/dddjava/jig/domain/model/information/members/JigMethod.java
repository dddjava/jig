package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;

import java.util.stream.Stream;

/**
 * メソッド
 */
public record JigMethod(JigMethodDeclaration jigMethodDeclaration, Term term) {

    public JigMethodIdentifier jigMethodIdentifier() {
        return jigMethodDeclaration.header().id();
    }

    public String simpleText() {
        return jigMethodIdentifier().simpleText();
    }

    public String fqn() {
        return jigMethodIdentifier().value();
    }

    public Stream<JigAnnotationReference> declarationAnnotationStream() {
        return jigMethodDeclaration.header().declarationAnnotationStream();
    }

    public JigMemberVisibility visibility() {
        return jigMethodDeclaration.jigMemberVisibility();
    }

    public boolean isPublic() {
        return visibility().isPublic();
    }

    public UsingFields usingFields() {
        return UsingFields.from(instructions());
    }

    public UsingMethods usingMethods() {
        return UsingMethods.from(instructions());
    }

    public TypeIdentifiers usingTypes() {
        return new TypeIdentifiers(jigMethodDeclaration.associatedTypes());
    }

    public String aliasTextOrBlank() {
        var title = term.title();
        return jigMethodDeclaration.name().equals(title) ? "" : title;
    }

    public String aliasText() {
        if (aliasTextOrBlank().isEmpty()) {
            return jigMethodDeclaration.declaringTypeIdentifier().asSimpleText() + "\\n" + name();
        }
        return aliasTextOrBlank();
    }

    /**
     * 出力時に使用する名称
     */
    public String labelTextWithSymbol() {
        return visibility().symbol() + ' ' + labelText();
    }

    public String labelText() {
        return term.title();
    }

    public String labelTextOrLambda() {
        if (jigMethodIdentifier().isLambda()) {
            return "lambda";
        }
        return labelText();
    }


    public boolean isObjectMethod() {
        return jigMethodDeclaration.header().isObjectMethod();
    }

    public boolean documented() {
        return !aliasTextOrBlank().isEmpty();
    }

    /**
     * 注目に値するかの判定
     *
     * publicもしくはドキュメントコメントが記述されているものを「注目に値する」と識別する。
     * privateでもドキュメントコメントが書かれているものは注目する。
     */
    public boolean remarkable() {
        return isPublic() || documented();
    }

    public String name() {
        return jigMethodDeclaration.name();
    }

    public boolean hasAnnotation(TypeIdentifier annotation) {
        return declarationAnnotationStream().anyMatch(it -> it.id().equals(annotation));
    }

    public Instructions instructions() {
        return jigMethodDeclaration.instructions();
    }

    public boolean isAbstract() {
        return jigMethodDeclaration.isAbstract();
    }

    public String nameArgumentsReturnSimpleText() {
        return jigMethodDeclaration.header().nameArgumentsReturnSimpleText();
    }

    public boolean isCall(JigMethodIdentifier jigMethodIdentifier) {
        return usingMethods().contains(jigMethodIdentifier);
    }

    public JigTypeReference methodReturnTypeReference() {
        return jigMethodDeclaration.header().returnType();
    }

    public String nameAndArgumentSimpleText() {
        return jigMethodDeclaration.nameAndArgumentSimpleText();
    }

    public Stream<JigTypeReference> methodArgumentTypeReferenceStream() {
        return jigMethodDeclaration.argumentStream();
    }

    public TypeIdentifier declaringType() {
        return jigMethodDeclaration.declaringTypeIdentifier();
    }

    public boolean isProgrammerDefined() {
        return jigMethodDeclaration.header().isProgrammerDefined();
    }

    public boolean isRecordComponent() {
        return jigMethodDeclaration.header().isRecordComponentAccessor();
    }
}
