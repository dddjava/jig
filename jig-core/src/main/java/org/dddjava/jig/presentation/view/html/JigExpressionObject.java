package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.field.FieldType;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeParameters;

import java.util.stream.Collectors;

class JigExpressionObject {
    private final JigDocumentContext jigDocumentContext;

    public JigExpressionObject(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    public String labelText(TypeIdentifier typeIdentifier) {
        ClassComment classComment = jigDocumentContext.classComment(typeIdentifier);
        return classComment.asTextOrIdentifierSimpleText();
    }

    public String fieldLinkType(FieldDeclaration fieldDeclaration) {
        TypeIdentifier typeIdentifier = fieldDeclaration.typeIdentifier();
        if (typeIdentifier.isJavaLanguageType()) {
            return "none";
        }
        return "other";
    }

    public String fieldRawText(FieldDeclaration fieldDeclaration) {
        FieldType fieldType = fieldDeclaration.fieldType();
        TypeParameters typeParameters = fieldType.typeParameterTypeIdentifiers();
        if (typeParameters.empty()) {
            TypeIdentifier typeIdentifier = fieldDeclaration.typeIdentifier();
            if (typeIdentifier.isJavaLanguageType()) {
                return unlinkText(typeIdentifier);
            }
            return linkTypeText(typeIdentifier);
        }

        // 型パラメータあり
        String typeParameterText = typeParameters.list().stream()
                .map(typeIdentifier -> {
                    if (typeIdentifier.isJavaLanguageType()) {
                        return unlinkText(typeIdentifier);
                    }
                    return linkTypeText(typeIdentifier);
                })
                .collect(Collectors.joining(", ", "&lt;", "&gt;"));

        TypeIdentifier typeIdentifier = fieldDeclaration.typeIdentifier();
        if (typeIdentifier.isJavaLanguageType()) {
            return unlinkText(typeIdentifier) + typeParameterText;
        }
        return linkTypeText(typeIdentifier) + typeParameterText;
    }

    private String unlinkText(TypeIdentifier typeIdentifier) {
        return String.format("<span class=\"weak\">%s</span>", typeIdentifier.asSimpleText());
    }

    private String linkTypeText(TypeIdentifier typeIdentifier) {
        return String.format("<a href=\"#%s\">%s</a>", typeIdentifier.fullQualifiedName(), labelText(typeIdentifier));
    }
}
