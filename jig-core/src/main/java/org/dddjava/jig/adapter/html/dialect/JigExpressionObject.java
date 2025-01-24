package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.data.classes.type.*;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thymeleafで使用するカスタム
 */
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
        return parameterizedTypeLinkText(jigField.fieldDeclaration().fieldType().parameterizedType());
    }

    public String fieldRawText(FieldDeclaration fieldDeclaration) {
        return parameterizedTypeLinkText(fieldDeclaration.fieldType().parameterizedType());
    }

    public String methodArgumentRawText(ParameterizedType parameterizedType) {
        return parameterizedTypeLinkText(parameterizedType);
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
