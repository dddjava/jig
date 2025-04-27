package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.adapter.html.mermaid.SequenceMermaidDiagram;
import org.dddjava.jig.adapter.html.mermaid.TypeMermaidDiagram;
import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.module.JigPackage;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IExpressionContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * `#jig` で使用するExpressionObject
 *
 * Thymeleafではメソッド呼び出しを直接できるが、domainのメソッドを直接呼び出すとコンパイルチェックが効かず、
 * リファクタリングや未使用の削除をする際のブレーキになってしまうため、極力このオブジェクト経由でアクセスするようにする。
 */
class JigExpressionObject {
    public static final String NAME = "jig";
    private static final Logger logger = LoggerFactory.getLogger(JigExpressionObject.class);

    private final IExpressionContext context;
    private final JigDocumentContext jigDocumentContext;

    public JigExpressionObject(IExpressionContext context, JigDocumentContext jigDocumentContext) {
        this.context = context;
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

    public boolean hasArgument(JigMethod jigMethod) {
        return jigMethod.jigMethodDeclaration().argumentStream().findAny().isPresent();
    }

    public String nameAndArgumentsAndReturnSimpleText(JigMethod jigMethod) {
        return jigMethod.nameArgumentsReturnSimpleText();
    }

    public String methodReturnLinkText(JigMethod jigMethod) {
        return linkText(jigMethod.jigMethodDeclaration().header().returnType());
    }

    public Iterator<String> methodArgumentLinkTexts(JigMethod jigMethod) {
        return jigMethod.jigMethodDeclaration().header().argumentList().stream()
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

    public List<JigMethod> listRemarkableStaticMethods(JigType jigType) {
        return jigType.staticJigMethods()
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

    public Optional<String> descriptionText(JigType jigType) {
        return Optional.of(jigType.term().description())
                .filter(Predicate.not(String::isEmpty));
    }

    public EnumModel selectEnumModel(JigType jigType) {
        var typeIdentifier = jigType.id();
        // これを使用するテンプレートは "enumModelMap" をcontextにputしておく
        // ・・・ならexpressionにしなくてもいいのでは？？？？
        Object variable = context.getVariable("enumModelMap");
        if (variable instanceof Map<?, ?> map) {
            if (map.get(typeIdentifier) instanceof EnumModel enumModel) {
                return enumModel;
            }
        }
        logger.warn("cannot find enum model for {}. Try to create empty model.", typeIdentifier.fullQualifiedName());
        // 落ちないように
        return new EnumModel(typeIdentifier, List.of());
    }

    public Optional<String> relationDiagram(JigPackage jigPackage) {
        if (context.getVariable("relationships") instanceof JigTypesWithRelationships jigTypesWithRelationships) {
            return new TypeMermaidDiagram().write(jigPackage, jigTypesWithRelationships);
        }
        return Optional.empty();
    }

    public List<Term> termList(Glossary glossary) {
        return glossary.list();
    }

    public String nearLetter(List<String> letters, Term term) {
        String termLetter = term.title().substring(0, 1);

        for (String letter : letters) {
            int compared = letter.compareTo(termLetter);
            // 同じかletterの方が大きい場合
            if (compared >= 0) return letter;
        }
        return letters.get(letters.size() - 1);
    }

    public String sequenceFor(JigMethod jigMethod) {
        return SequenceMermaidDiagram.textFor(jigMethod);
    }
}
