package org.dddjava.jig.domain.model.data.classes.field;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

public class JigField {
    private final JigFieldHeader jigFieldHeader;
    FieldDeclaration fieldDeclaration;

    public JigField(JigFieldHeader jigFieldHeader, FieldDeclaration fieldDeclaration) {
        this.jigFieldHeader = jigFieldHeader;
        this.fieldDeclaration = fieldDeclaration;
    }

    public JigTypeReference jigTypeReference() {
        return jigFieldHeader.jigTypeReference();
    }

    static ParameterizedType jigReferenceToParameterizedType(JigTypeReference jigTypeReference) {
        return new ParameterizedType(jigTypeReference.id(),
                jigTypeReference.typeArgumentList().stream()
                        .map(jigTypeArgument -> jigReferenceToParameterizedType(jigTypeArgument.jigTypeReference()))
                        .toList());
    }

    public static JigField from(JigFieldHeader jigFieldHeader) {
        // 互換のため無理矢理JigFieldHeaderから生成している状態。FieldDeclarationの使用箇所を直せばもっと素直にできるはず
        var fieldDeclaration = new FieldDeclaration(
                jigFieldHeader.id().declaringTypeIdentifier(),
                new FieldType(jigReferenceToParameterizedType(jigFieldHeader.jigTypeReference())),
                jigFieldHeader.id().name()
        );
        return new JigField(
                jigFieldHeader,
                fieldDeclaration
        );
    }

    public TypeIdentifier typeIdentifier() {
        return jigFieldHeader.jigTypeReference().id();
    }

    public String nameText() {
        return jigFieldHeader.name();
    }

    public boolean isDeprecated() {
        return jigFieldHeader.isDeprecated();
    }
}
