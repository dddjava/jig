package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;

import java.util.stream.Stream;

/**
 * メソッド
 */
public record JigMethod(JigMethodDeclaration jigMethodDeclaration, Term term) {

    public JigMethodId jigMethodIdentifier() {
        return header().id();
    }

    public String simpleText() {
        return jigMethodIdentifier().simpleText();
    }

    public String fqn() {
        return jigMethodIdentifier().value();
    }

    public Stream<JigAnnotationReference> declarationAnnotationStream() {
        return header().declarationAnnotationStream();
    }

    public JigMemberVisibility visibility() {
        return header().jigMemberVisibility();
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
        return name().equals(title) ? "" : title;
    }

    public String aliasText() {
        if (aliasTextOrBlank().isEmpty()) {
            return declaringType().asSimpleText() + "\\n" + name();
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
        return header().isObjectMethod();
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
        return header().name();
    }

    public boolean hasAnnotation(TypeId annotation) {
        return declarationAnnotationStream().anyMatch(it -> it.id().equals(annotation));
    }

    public Instructions instructions() {
        return jigMethodDeclaration.instructions();
    }

    public boolean isAbstract() {
        return header().isAbstract();
    }

    public String nameArgumentsReturnSimpleText() {
        return header().nameArgumentsReturnSimpleText();
    }

    public boolean isCall(JigMethodId jigMethodId) {
        return usingMethods().contains(jigMethodId);
    }

    public JigTypeReference methodReturnTypeReference() {
        return header().returnType();
    }

    public String nameAndArgumentSimpleText() {
        return header().nameAndArgumentSimpleText();
    }

    public Stream<JigTypeReference> methodArgumentTypeReferenceStream() {
        return header().argumentList().stream();
    }

    public TypeId declaringType() {
        return header().id().tuple().declaringTypeIdentifier();
    }

    public boolean isProgrammerDefined() {
        return header().isProgrammerDefined();
    }

    public boolean isRecordComponent() {
        return header().isRecordComponentAccessor();
    }

    /**
     * メソッド定義のヘッダにアクセスするヘルパーメソッド
     *
     * JigMethodに対する関心のほとんどはヘッダに由来する。
     * すべての箇所でチェーンすると冗長なコードになるので、それを改善する。
     */
    private JigMethodHeader header() {
        return jigMethodDeclaration.header();
    }
}
