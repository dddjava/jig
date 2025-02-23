package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.enums.EnumModel;
import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.JigTypeArgument;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.module.JigPackage;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.relations.TypeRelationship;
import org.dddjava.jig.domain.model.information.types.relations.TypeRelationships;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.IExpressionContext;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class JigExpressionObject {
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
            PackageIdentifier packageIdentifier = jigPackage.packageIdentifier();
            TypeRelationships typeRelationships = jigTypesWithRelationships.typeRelationships();

            Map<Boolean, List<TypeRelationship>> partitioningRelations = typeRelationships.list().stream()
                    // fromがこのパッケージを対象とし、このパッケージのクラスから外のクラスへの関連を出力する。
                    // toを対象にすると広く使われるクラス（たとえばIDなど）があるパッケージは見れたものではなくなるので出さない。
                    .filter(typeRelationship -> typeRelationship.from().packageIdentifier().equals(packageIdentifier))
                    .collect(Collectors.partitioningBy(typeRelationship -> typeRelationship.to().packageIdentifier().equals(packageIdentifier)));
            if (partitioningRelations.get(true).isEmpty()) {
                return Optional.empty();
            }

            // 外部関連を表示する閾値
            int threshold = 20;
            boolean omitExternalRelations = partitioningRelations.get(true).size() + partitioningRelations.get(false).size() > threshold;
            List<TypeRelationship> targetRelationships = omitExternalRelations
                    ? partitioningRelations.get(true) // パッケージ外への関連の方が多い場合はパッケージ内のみにする
                    : partitioningRelations.values().stream().flatMap(Collection::stream).toList();

            // 関連に含まれるnodeをパッケージの内側と外側に仕分け＆ラベル付け
            Set<TypeIdentifier> targetTypes = targetRelationships.stream()
                    .flatMap(typeRelationship -> Stream.of(typeRelationship.from(), typeRelationship.to()))
                    .collect(Collectors.toSet());
            Map<Boolean, List<String>> nodeMap = targetTypes.stream()
                    .collect(Collectors.partitioningBy(typeIdentifier -> typeIdentifier.packageIdentifier().equals(packageIdentifier),
                            Collectors.mapping(typeIdentifier -> {
                                        String label = jigTypesWithRelationships.jigTypes()
                                                .resolveJigType(typeIdentifier).map(JigType::label)
                                                .orElseGet(typeIdentifier::asSimpleName);
                                        return "%s[%s]".formatted(typeIdentifier.htmlIdText(), label);
                                    },
                                    Collectors.toList())));

            StringJoiner diagramText = new StringJoiner("\n    ", "\ngraph TB\n    ", "");
            if (nodeMap.containsKey(true)) {
                diagramText.add("subgraph %s[%s]".formatted(jigPackage.packageIdentifier().htmlIdText(), jigPackage.label()));
                diagramText.add("direction TB");
                nodeMap.get(true).forEach(diagramText::add);
                diagramText.add("end");
            }
            if (nodeMap.containsKey(false)) {
                nodeMap.get(false).forEach(diagramText::add);
            }
            // クリックでジャンプ
            targetTypes.stream().map(id -> "click %s \"#%s\"".formatted(id.htmlIdText(), id.fullQualifiedName())).forEach(diagramText::add);

            targetRelationships.stream()
                    .map(relationship -> "%s --> %s".formatted(relationship.from().htmlIdText(), relationship.to().htmlIdText()))
                    .forEach(diagramText::add);
            if (omitExternalRelations) {
                diagramText.add("A@{ shape: braces, label: \"関連数が%dを超えるため、外部への関連は省略されました。\" }".formatted(threshold));
            }

            return Optional.of(diagramText.toString());
        }
        return Optional.empty();
    }
}
