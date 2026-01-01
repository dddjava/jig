package org.dddjava.jig.adapter.thymeleaf.dialect;

import org.dddjava.jig.adapter.mermaid.SequenceMermaidDiagram;
import org.dddjava.jig.adapter.mermaid.TypeRelationMermaidDiagram;
import org.dddjava.jig.adapter.thymeleaf.HtmlSupport;
import org.dddjava.jig.adapter.thymeleaf.SummaryModel;
import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IExpressionContext;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

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

    public String labelText(TypeId typeId) {
        return jigDocumentContext.typeTerm(typeId).title();
    }

    public String fieldLinkType(TypeId typeId) {
        if (typeId.isJavaLanguageType()) {
            return "none";
        }
        return "other";
    }

    public boolean isEnum(JigType jigType) {
        return jigType.toValueKind() == JigTypeValueKind.区分;
    }

    /**
     * enumの列挙定数名リスト
     */
    public List<String> enumConstantIdentifiers(JigType jigType) {
        return jigType.jigTypeMembers().enumConstantStream()
                .map(jigField -> jigField.nameText())
                .toList();
    }

    /**
     * enumの列挙定数用語リスト
     */
    public List<Term> enumConstantTerms(JigType jigType) {
        return jigType.jigTypeMembers().enumConstantStream()
                .map(jigField -> jigField.term())
                .toList();
    }

    public String fieldRawText(JigField jigField) {
        return linkText(jigField.jigTypeReference());
    }

    public boolean hasArgument(JigMethod jigMethod) {
        return jigMethod.jigMethodDeclaration().argumentStream().findAny().isPresent();
    }

    public String simpleMethodDeclarationText(JigMethod jigMethod) {
        return jigMethod.simpleMethodDeclarationText();
    }

    public String methodReturnLinkText(JigMethod jigMethod) {
        return linkText(jigMethod.jigMethodDeclaration().header().returnType());
    }

    public Iterator<String> methodParameterLinkTexts(JigMethod jigMethod) {
        return jigMethod.jigMethodDeclaration().header().parameterTypeList().stream()
                .map(this::linkText)
                .iterator();
    }

    private String linkText(JigTypeReference jigTypeReference) {
        var typeArgumentList = jigTypeReference.typeArgumentList();
        // 型引数がなければ空文字列、ある場合は <Class> のような文字列にする
        String typeArgumentText = typeArgumentList.isEmpty() ? "" :
                typeArgumentList.stream()
                        .map(JigTypeArgument::typeId)
                        .map(this::typeIdToLinkText)
                        .collect(joining(", ", "&lt;", "&gt;"));

        return typeIdToLinkText(jigTypeReference.id()) + typeArgumentText;
    }

    private String typeIdToLinkText(TypeId typeId) {
        if (typeId.isJavaLanguageType()) {
            return plainText(typeId);
        }
        return linkText(typeId);
    }

    private String plainText(TypeId typeId) {
        return String.format("<span class=\"weak\">%s</span>", typeId.asSimpleText());
    }

    private String linkText(TypeId typeId) {
        return String.format("<a href=\"./domain.html#%s\">%s</a>", typeId.fqn(), labelText(typeId));
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
        return HtmlSupport.htmlMethodIdText(jigMethod.jigMethodId());
    }

    public Optional<String> descriptionText(JigType jigType) {
        return Optional.of(jigType.term().description())
                .filter(Predicate.not(String::isEmpty));
    }

    public EnumModel selectEnumModel(JigType jigType) {
        var typeId = jigType.id();
        // これを使用するテンプレートは "enumModelMap" をcontextにputしておく
        // ・・・ならexpressionにしなくてもいいのでは？？？？
        Object variable = context.getVariable(SummaryModel.ENUM_MODEL_MAP_KEY);
        if (variable instanceof Map<?, ?> map) {
            if (map.get(typeId) instanceof EnumModel enumModel) {
                return enumModel;
            }
        }
        logger.warn("cannot find enum model for {}. Try to create empty model.", typeId.fqn());
        // 落ちないように
        return new EnumModel(typeId, List.of(), List.of());
    }

    public Optional<String> relationDiagram(JigPackage jigPackage) {
        if (context.getVariable(SummaryModel.RELATIONSHIPS_KEY) instanceof CoreTypesAndRelations coreTypesAndRelations) {
            return new TypeRelationMermaidDiagram().write(jigPackage, coreTypesAndRelations);
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
