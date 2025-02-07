package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.classes.type.TypeArgumentList;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.JigTypeValueKind;

import java.util.List;
import java.util.stream.Collectors;

class JigExpressionObject {
    private final JigDocumentContext jigDocumentContext;

    public JigExpressionObject(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    public String labelText(TypeIdentifier typeIdentifier) {
        return jigDocumentContext.typeTerm(typeIdentifier).title();
    }

    public String fieldLinkType(TypeIdentifier typeIdentifier) {
        if (typeIdentifier.isJavaLanguageType()) {
            return "none";
        }
        return "other";
    }

    public boolean isEnum(JigType jigType) {
        return jigType.toValueKind() == JigTypeValueKind.区分;
    }

    /**
     * enumの定数名リストを作成する。
     */
    public List<String> enumConstantIdentifiers(JigType jigType) {
        if (jigType.toValueKind() != JigTypeValueKind.区分) {
            return List.of();
        }

        return jigType.staticMember().staticFieldDeclarations().selfDefineOnly().list().stream()
                .map(StaticFieldDeclaration::nameText)
                .toList();
    }

    public String methodReturnRawText(MethodReturn methodReturn) {
        return parameterizedTypeLinkText(methodReturn.parameterizedType());
    }

    public String fieldRawText(JigField jigField) {
        return parameterizedTypeLinkText(jigField.jigTypeReference());
    }

    public String fieldRawText(FieldDeclaration fieldDeclaration) {
        return parameterizedTypeLinkText(fieldDeclaration.fieldType().parameterizedType());
    }

    public String methodArgumentRawText(ParameterizedType parameterizedType) {
        return parameterizedTypeLinkText(parameterizedType);
    }

    private String parameterizedTypeLinkText(JigTypeReference jigTypeReference) {
        TypeIdentifier typeIdentifier = jigTypeReference.id();
        var typeArgumentList = jigTypeReference.typeArgumentList();
        if (typeArgumentList.isEmpty()) {
            if (typeIdentifier.isJavaLanguageType()) {
                return unlinkText(typeIdentifier);
            }
            return linkTypeText(typeIdentifier);
        }

        // 型パラメータあり
        String typeParameterText = typeArgumentList.stream()
                .map(JigTypeArgument::typeIdentifier)
                .map(argumentTypeIdentifier -> {
                    if (argumentTypeIdentifier.isJavaLanguageType()) {
                        return unlinkText(argumentTypeIdentifier);
                    }
                    return linkTypeText(argumentTypeIdentifier);
                })
                .collect(Collectors.joining(", ", "&lt;", "&gt;"));

        if (typeIdentifier.isJavaLanguageType()) {
            return unlinkText(typeIdentifier) + typeParameterText;
        }
        return linkTypeText(typeIdentifier) + typeParameterText;
    }

    private String parameterizedTypeLinkText(ParameterizedType parameterizedType) {
        TypeIdentifier typeIdentifier = parameterizedType.typeIdentifier();
        TypeArgumentList typeArgumentList = parameterizedType.typeParameters();
        if (typeArgumentList.empty()) {
            if (typeIdentifier.isJavaLanguageType()) {
                return unlinkText(typeIdentifier);
            }
            return linkTypeText(typeIdentifier);
        }

        // 型パラメータあり
        String typeParameterText = typeArgumentList.list().stream()
                .map(parameterTypeIdentifier -> {
                    if (parameterTypeIdentifier.isJavaLanguageType()) {
                        return unlinkText(parameterTypeIdentifier);
                    }
                    return linkTypeText(parameterTypeIdentifier);
                })
                .collect(Collectors.joining(", ", "&lt;", "&gt;"));

        if (typeIdentifier.isJavaLanguageType()) {
            return unlinkText(typeIdentifier) + typeParameterText;
        }
        return linkTypeText(typeIdentifier) + typeParameterText;
    }

    private String unlinkText(TypeIdentifier typeIdentifier) {
        return String.format("<span class=\"weak\">%s</span>", typeIdentifier.asSimpleText());
    }

    private String linkTypeText(TypeIdentifier typeIdentifier) {
        return String.format("<a href=\"./domain.html#%s\">%s</a>", typeIdentifier.fullQualifiedName(), labelText(typeIdentifier));
    }
}
