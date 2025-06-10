package org.dddjava.jig.adapter.excel;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldIdentifier;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.inputs.HttpEndpoint;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.module.JigPackageWithJigTypes;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeKind;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;
import org.dddjava.jig.domain.model.knowledge.validations.Validations;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@HandleDocument
public class ListAdapter implements Adapter<ReportBook> {

    /**
     * 一覧出力で複数要素を文字列連結する際のコレクター
     */
    private static final Collector<CharSequence, ?, String> STREAM_COLLECTOR = Collectors.joining(", ", "[", "]");

    private final JigDocumentContext jigDocumentContext;
    private final JigService jigService;

    public ListAdapter(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocumentContext = jigDocumentContext;
        this.jigService = jigService;
    }

    @HandleDocument(JigDocument.BusinessRuleList)
    public ReportBook businessRuleReports(JigRepository jigRepository) {

        MethodSmellList methodSmellList = jigService.methodSmells(jigRepository);
        JigTypes jigTypes = jigService.jigTypes(jigRepository);
        var allClassRelations = TypeRelationships.from(jigTypes);

        JigTypesWithRelationships jigTypesWithRelationships = jigService.coreDomainJigTypesWithRelationships(jigRepository);
        JigTypes coreDomainJigTypes = jigTypesWithRelationships.jigTypes();
        JigTypes categoryTypes = jigService.categoryTypes(jigRepository);
        List<JigPackageWithJigTypes> jigTypePackages = JigPackageWithJigTypes.from(coreDomainJigTypes);
        return new ReportBook(
                new ReportSheet<>("PACKAGE", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageIdentifier().asText()),
                        ReportItem.ofString("パッケージ別名", item -> jigDocumentContext.packageTerm(item.packageIdentifier()).title()),
                        ReportItem.ofNumber("クラス数", item -> item.jigTypes().size())
                ), jigTypePackages),
                new ReportSheet<>("ALL", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.id().asSimpleText()),
                        ReportItem.ofString("クラス別名", item -> item.label()),
                        ReportItem.ofString("ビジネスルールの種類", item -> item.toValueKind().toString()),
                        ReportItem.ofNumber("関連元ビジネスルール数", item -> jigTypesWithRelationships.typeRelationships().filterTo(item.id()).size()),
                        ReportItem.ofNumber("関連先ビジネスルール数", item -> jigTypesWithRelationships.typeRelationships().filterFrom(item.id()).size()),
                        ReportItem.ofNumber("関連元クラス数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.id()).list().size()),
                        ReportItem.ofString("非PUBLIC", item -> item.visibility() != JigTypeVisibility.PUBLIC ? "◯" : ""),
                        ReportItem.ofString("同パッケージからのみ参照", item -> {
                            var identifiers = allClassRelations.collectTypeIdentifierWhichRelationTo(item.id()).packageIdentifiers().identifiers();
                            return identifiers.equals(Set.of(item.packageIdentifier())) ? "◯" : "";
                        }),
                        ReportItem.ofString("関連元クラス", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.id()).asSimpleText())
                ), coreDomainJigTypes.list()),
                new ReportSheet<>("ENUM", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.id().asSimpleText()),
                        ReportItem.ofString("クラス別名", item -> item.label()),
                        ReportItem.ofString("定数宣言", item -> item.jigTypeMembers().enumConstantNames().stream().collect(STREAM_COLLECTOR)),
                        ReportItem.ofString("フィールド", item -> item.jigTypeMembers().instanceFields().stream()
                                .map(jigField -> jigField.jigFieldHeader().simpleText())
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofNumber("使用箇所数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.id()).list().size()),
                        ReportItem.ofString("使用箇所", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.id()).asSimpleText()),
                        // TODO: パラメータあり＝フィールドありは直接はつながらない
                        ReportItem.ofString("パラメーター有り", item -> item.hasInstanceField() ? "◯" : ""),
                        ReportItem.ofString("振る舞い有り", item -> item.hasInstanceMethod() ? "◯" : ""),
                        // 抽象列挙型は継承クラスがコンパイラに作成されているもので、多態とみなすことにする
                        ReportItem.ofString("多態", item -> item.typeKind() == TypeKind.抽象列挙型 ? "◯" : "")
                ), categoryTypes.list()),
                new ReportSheet<>("COLLECTION", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.id().asSimpleText()),
                        ReportItem.ofString("クラス別名", item -> item.label()),
                        ReportItem.ofString("フィールドの型", item -> {
                            List<String> list = item.jigTypeMembers().instanceFields().stream()
                                    .map(jigField -> jigField.jigTypeReference().simpleNameWithGenerics())
                                    .toList();
                            return list.size() == 1 ? list.get(0) : list.stream().collect(STREAM_COLLECTOR);
                        }),
                        ReportItem.ofNumber("使用箇所数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.id()).size()),
                        ReportItem.ofString("使用箇所", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.id()).asSimpleText()),
                        ReportItem.ofNumber("メソッド数", item -> item.instanceJigMethods().list().size()),
                        ReportItem.ofString("メソッド一覧", item -> item.instanceJigMethods().stream().map(JigMethod::nameArgumentsReturnSimpleText).sorted().collect(STREAM_COLLECTOR))
                ), coreDomainJigTypes.listCollectionType()),
                new ReportSheet<>("VALIDATION", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        ReportItem.ofString("クラス別名", item -> jigDocumentContext.typeTerm(item.typeIdentifier()).title()),
                        ReportItem.ofString("メンバ名", item -> item.memberName()),
                        ReportItem.ofString("メンバクラス名", item -> item.memberType().asSimpleText()),
                        ReportItem.ofString("アノテーションクラス名", item -> item.annotationType().asSimpleText()),
                        ReportItem.ofString("アノテーション記述", item -> item.annotationDescription())
                ), Validations.from(jigTypes).list()),
                new ReportSheet<>("注意メソッド", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.method().declaringType().packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.method().declaringType().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.method().nameAndArgumentSimpleText()),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.methodReturnType().asSimpleText()),
                        ReportItem.ofString("クラス別名", item -> item.declaringJigType().label()),
                        ReportItem.ofString("メンバを使用していない", item -> item.notUseMember() ? "◯" : ""),
                        ReportItem.ofString("基本型の授受を行なっている", item -> item.primitiveInterface() ? "◯" : ""),
                        ReportItem.ofString("NULLリテラルを使用している", item -> item.referenceNull() ? "◯" : ""),
                        ReportItem.ofString("NULL判定をしている", item -> item.nullDecision() ? "◯" : ""),
                        ReportItem.ofString("真偽値を返している", item -> item.returnsBoolean() ? "◯" : ""),
                        ReportItem.ofString("voidを返している", item -> item.returnsVoid() ? "◯" : "")
                ), methodSmellList.list())
        );
    }

    @HandleDocument(JigDocument.ApplicationList)
    public ReportBook applicationReports(JigRepository jigRepository) {
        ServiceAngles serviceAngles = jigService.serviceAngles(jigRepository);
        DatasourceAngles datasourceAngles = jigService.datasourceAngles(jigRepository);
        StringComparingMethodList stringComparingMethodList = jigService.stringComparing(jigRepository);
        Entrypoints entrypoints = jigService.entrypoint(jigRepository);

        return new ReportBook(
                new ReportSheet<>("CONTROLLER", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.jigMethod().nameAndArgumentSimpleText()),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.jigMethod().methodReturnTypeReference().simpleName()),
                        ReportItem.ofString("クラス別名", item -> item.jigType().label()),
                        ReportItem.ofString("使用しているフィールドの型", item -> item.jigMethod().usingFields().fieldIds().stream()
                                .map(JigFieldIdentifier::declaringTypeIdentifier)
                                .map(TypeIdentifier::asSimpleText)
                                .sorted()
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofNumber("分岐数", item -> item.jigMethod().instructions().decisionCount()),
                        ReportItem.ofString("パス", item -> HttpEndpoint.from(item).pathText())
                ), entrypoints.listRequestHandlerMethods()),
                new ReportSheet<>("SERVICE", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.serviceMethod().declaringType().packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.serviceMethod().declaringType().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.serviceMethod().method().nameAndArgumentSimpleText()),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.serviceMethod().method().methodReturnTypeReference().simpleName()),
                        ReportItem.ofString("イベントハンドラ", item -> item.usingFromController() ? "◯" : ""),
                        ReportItem.ofString("クラス別名", item -> jigDocumentContext.typeTerm(item.serviceMethod().declaringType()).title()),
                        ReportItem.ofString("メソッド別名", item -> item.serviceMethod().method().aliasTextOrBlank()),
                        ReportItem.ofString("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.typeTerm(item.serviceMethod().method().methodReturnTypeReference().id()).title()
                        ),
                        ReportItem.ofString("メソッド引数の型の別名", item ->
                                item.serviceMethod().method().methodArgumentTypeReferenceStream()
                                        .map(JigTypeReference::id)
                                        .map(jigDocumentContext::typeTerm)
                                        .map(Term::title)
                                        .collect(STREAM_COLLECTOR)
                        ),
                        ReportItem.ofString("使用しているフィールドの型", item -> item.usingFields().fieldIds().stream()
                                .map(JigFieldIdentifier::declaringTypeIdentifier)
                                .map(TypeIdentifier::asSimpleText)
                                .sorted()
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofNumber("分岐数", item -> item.serviceMethod().method().instructions().decisionCount()),
                        ReportItem.ofString("使用しているサービスのメソッド", item -> item.usingServiceMethods().stream().map(invokedMethod -> invokedMethod.asSignatureAndReturnTypeSimpleText()).collect(STREAM_COLLECTOR)),
                        ReportItem.ofString("使用しているリポジトリのメソッド", item -> item.usingRepositoryMethods().list().stream()
                                .map(JigMethod::nameAndArgumentSimpleText)
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofString("null使用", item -> item.useNull() ? "◯" : ""),
                        ReportItem.ofString("stream使用", item -> item.useStream() ? "◯" : "")
                ), serviceAngles.list()),
                new ReportSheet<>("REPOSITORY", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageText()),
                        ReportItem.ofString("クラス名", item -> item.typeSimpleName()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.nameAndArgumentSimpleText()),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.methodReturnTypeReference().simpleNameWithGenerics()),
                        ReportItem.ofString("クラス別名", item -> item.typeLabel()),
                        ReportItem.ofString("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.typeTerm(item.methodReturnTypeReference().id()).title()
                        ),
                        ReportItem.ofString("メソッド引数の型の別名", item ->
                                item.methodArgumentTypeReferenceStream()
                                        .map(JigTypeReference::id)
                                        .map(jigDocumentContext::typeTerm)
                                        .map(Term::title)
                                        .collect(STREAM_COLLECTOR)
                        ),
                        ReportItem.ofNumber("分岐数", item -> item.concreteMethod().instructions().decisionCount()),
                        ReportItem.ofString("INSERT", item -> item.insertTables()),
                        ReportItem.ofString("SELECT", item -> item.selectTables()),
                        ReportItem.ofString("UPDATE", item -> item.updateTables()),
                        ReportItem.ofString("DELETE", item -> item.deleteTables()),
                        ReportItem.ofNumber("関連元クラス数", item -> item.callerMethods().typeCount()),
                        ReportItem.ofNumber("関連元メソッド数", item -> item.callerMethods().size())
                ), datasourceAngles.list()),
                new ReportSheet<>("文字列比較箇所", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.jigMethodDeclaration().declaringTypeIdentifier().packageIdentifier().asText()),
                        ReportItem.ofString("クラス名", item -> item.jigMethodDeclaration().declaringTypeIdentifier().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.nameAndArgumentSimpleText())
                ), stringComparingMethodList.list())
        );
    }

    @Override
    public List<Path> write(ReportBook result, JigDocument jigDocument) {
        return result.writeXlsx(jigDocument, jigDocumentContext.outputDirectory());
    }
}
