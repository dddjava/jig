package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodDerivation;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.members.JigMethodDeclaration;
import org.dddjava.jig.domain.model.data.members.instruction.DecisionNumber;
import org.dddjava.jig.domain.model.data.members.instruction.Instructions;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;

import java.util.List;
import java.util.stream.Stream;

/**
 * メソッド
 */
public class JigMethod {

    private final JigMethodDeclaration jigMethodDeclaration;
    MethodDeclaration methodDeclaration;

    MethodDerivation methodDerivation;
    private final Term term;

    public JigMethod(JigMethodDeclaration jigMethodDeclaration, MethodDeclaration methodDeclaration, MethodDerivation methodDerivation, Term term) {
        this.jigMethodDeclaration = jigMethodDeclaration;
        this.methodDeclaration = methodDeclaration;
        this.methodDerivation = methodDerivation;
        this.term = term;
    }

    public MethodDeclaration declaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return instructions().decisionNumber();
    }

    public Stream<JigAnnotationReference> declarationAnnotationStream() {
        return jigMethodDeclaration.header().jigMethodAttribute().declarationAnnotations().stream();
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

    public boolean conditionalNull() {
        return instructions().hasNullDecision();
    }

    public boolean referenceNull() {
        return instructions().hasNullReference();
    }

    public boolean useNull() {
        return referenceNull() || conditionalNull();
    }

    public TypeIdentifiers usingTypes() {
        return new TypeIdentifiers(jigMethodDeclaration.associatedTypes());
    }

    public String aliasTextOrBlank() {
        var title = term.title();
        return declaration().methodSignature().methodName().equals(title) ? "" : title;
    }

    public String aliasText() {
        if (aliasTextOrBlank().isEmpty()) {
            return declaration().declaringType().asSimpleText() + "\\n" + declaration().methodSignature().methodName();
        }
        return aliasTextOrBlank();
    }

    public Term term() {
        return term;
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

    public String fqn() {
        return declaration().identifier().asText();
    }

    public String htmlIdText() {
        return declaration().htmlIdText();
    }

    public String labelTextOrLambda() {
        if (declaration().isLambda()) {
            return "lambda";
        }
        return labelText();
    }

    public List<ParameterizedType> argumentTypes() {
        return declaration().methodSignature().arguments();
    }

    public MethodDerivation derivation() {
        return methodDerivation;
    }

    public boolean objectMethod() {
        return declaration().methodSignature().isObjectMethod();
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

    public String asNameArgumentsReturnText() {
        return jigMethodDeclaration.header().asSignatureAndReturnTypeSimpleText();
    }
}
