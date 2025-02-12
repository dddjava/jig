package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;

import java.util.Iterator;
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

        return jigType.jigTypeMembers().enumConstantNames();
    }

    public String fieldRawText(JigField jigField) {
        return linkText(jigField.jigTypeReference());
    }

    public String nameAndArgumentsAndReturnSimpleText(JigMethod jigMethod) {
        return jigMethod.nameArgumentsReturnSimpleText();
    }

    public String methodReturnLinkText(JigMethod jigMethod) {
        return linkText(jigMethod.jigMethodDeclaration().header().jigMethodAttribute().returnType());
    }

    public Iterator<String> methodArgumentLinkTexts(JigMethod jigMethod) {
        return jigMethod.jigMethodDeclaration().header().jigMethodAttribute().argumentList().stream()
                .map(this::linkText)
                .iterator();
    }

    private String linkText(JigTypeReference jigTypeReference) {
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

    private String unlinkText(TypeIdentifier typeIdentifier) {
        return String.format("<span class=\"weak\">%s</span>", typeIdentifier.asSimpleText());
    }

    private String linkTypeText(TypeIdentifier typeIdentifier) {
        return String.format("<a href=\"./domain.html#%s\">%s</a>", typeIdentifier.fullQualifiedName(), labelText(typeIdentifier));
    }

    public List<JigMethod> listRemarkableInstanceMethods(JigType jigType) {
        return jigType.instanceJigMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod()
                .listRemarkable();
    }

    public static String htmlIdText(JigMethod jigMethod) {
        return htmlIdText(jigMethod.jigMethodIdentifier());
    }

    public static String htmlIdText(JigMethodIdentifier jigMethodIdentifier) {
        var tuple = jigMethodIdentifier.tuple();

        var typeText = tuple.declaringTypeIdentifier().packageAbbreviationText();
        var parameterText = tuple.parameterTypeIdentifiers().stream()
                .map(TypeIdentifier::packageAbbreviationText)
                .collect(Collectors.joining(", ", "(", ")"));
        return (typeText + '.' + tuple.name() + parameterText).replaceAll("[^a-zA-Z0-9]", "_");
    }
}
