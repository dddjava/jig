package org.dddjava.jig.adapter.excel;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigTypesRepository;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigDataProvider;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.HttpEndpoint;
import org.dddjava.jig.domain.model.information.module.JigTypesPackage;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.type.JigTypes;
import org.dddjava.jig.domain.model.information.type.TypeKind;
import org.dddjava.jig.domain.model.information.validations.Validations;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@HandleDocument
public class ListAdapter implements Adapter<ReportBook> {

    private final JigDocumentContext jigDocumentContext;
    private final JigService jigService;

    public ListAdapter(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocumentContext = jigDocumentContext;
        this.jigService = jigService;
    }

    @HandleDocument(JigDocument.BusinessRuleList)
    public ReportBook businessRuleReports(JigTypesRepository jigTypesRepository) {

        MethodSmellList methodSmellList = jigService.methodSmells(jigTypesRepository);
        JigTypes jigTypes = jigService.jigTypes(jigTypesRepository);
        var allClassRelations = ClassRelations.from(jigTypes);

        JigTypes coreDomainJigTypes = jigService.coreDomainJigTypes(jigTypesRepository);
        JigTypes categoryTypes = jigService.categoryTypes(jigTypesRepository);
        List<JigTypesPackage> jigTypePackages = JigTypesPackage.from(coreDomainJigTypes);
        return new ReportBook(
                new ReportSheet<>("PACKAGE", List.of(
                        Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                        Map.entry("パッケージ別名", item -> jigDocumentContext.packageTerm(item.packageIdentifier()).title()),
                        Map.entry("クラス数", item -> item.jigTypes().size())
                ), jigTypePackages),
                new ReportSheet<>("ALL", List.of(
                        Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> item.label()),
                        Map.entry("ビジネスルールの種類", item -> item.toValueKind().toString()),
                        Map.entry("関連元ビジネスルール数", item -> ClassRelations.internalTypeRelationsTo(coreDomainJigTypes, item).size()),
                        Map.entry("関連先ビジネスルール数", item -> ClassRelations.internalTypeRelationsFrom(coreDomainJigTypes, item).size()),
                        Map.entry("関連元クラス数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).list().size()),
                        Map.entry("非PUBLIC", item -> item.visibility() != JigTypeVisibility.PUBLIC ? "◯" : ""),
                        Map.entry("同パッケージからのみ参照", item -> {
                            var list = allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).packageIdentifiers().list();
                            return list.size() == 1 && list.get(0).equals(item.packageIdentifier()) ? "◯" : "";
                        }),
                        Map.entry("関連元クラス", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).asSimpleText())
                ), coreDomainJigTypes.list()),
                new ReportSheet<>("ENUM", List.of(
                        Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> item.label()),
                        Map.entry("定数宣言", item -> item.jigTypeMembers().enumConstantNames().stream().collect(Collectors.joining(", ", "[", "]"))),
                        Map.entry("フィールド", item -> item.jigTypeMembers().instanceFieldsSimpleText()),
                        Map.entry("使用箇所数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).list().size()),
                        Map.entry("使用箇所", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).asSimpleText()),
                        // TODO: パラメータあり＝フィールドありは直接はつながらない
                        Map.entry("パラメーター有り", item -> item.hasInstanceField() ? "◯" : ""),
                        Map.entry("振る舞い有り", item -> item.hasInstanceMethod() ? "◯" : ""),
                        // 抽象列挙型は継承クラスがコンパイラに作成されているもので、多態とみなすことにする
                        Map.entry("多態", item -> item.typeKind() == TypeKind.抽象列挙型 ? "◯" : "")
                ), categoryTypes.list()),
                new ReportSheet<>("COLLECTION", List.of(
                        Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> item.label()),
                        Map.entry("フィールドの型", item -> item.jigTypeMembers().instanceFieldsSimpleTextWithGenerics()),
                        Map.entry("使用箇所数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.identifier()).size()),
                        Map.entry("使用箇所", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.identifier()).asSimpleText()),
                        Map.entry("メソッド数", item -> item.instanceJigMethods().list().size()),
                        Map.entry("メソッド一覧", item -> item.instanceJigMethods().declarations().asSignatureAndReturnTypeSimpleText())
                ), coreDomainJigTypes.listCollectionType()),
                new ReportSheet<>("VALIDATION", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.typeTerm(item.typeIdentifier()).title()),
                        Map.entry("メンバ名", item -> item.memberName()),
                        Map.entry("メンバクラス名", item -> item.memberType().asSimpleText()),
                        Map.entry("アノテーションクラス名", item -> item.annotationType().asSimpleText()),
                        Map.entry("アノテーション記述", item -> item.annotationDescription())
                ), Validations.from(jigTypes).list()),
                new ReportSheet<>("注意メソッド", List.of(
                        Map.entry("パッケージ名", item -> item.methodDeclaration().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.methodDeclaration().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.methodDeclaration().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.methodDeclaration().methodReturn().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.typeTerm(item.methodDeclaration().declaringType()).title()),
                        Map.entry("メンバを使用していない", item -> item.notUseMember() ? "◯" : ""),
                        Map.entry("基本型の授受を行なっている", item -> item.primitiveInterface() ? "◯" : ""),
                        Map.entry("NULLリテラルを使用している", item -> item.referenceNull() ? "◯" : ""),
                        Map.entry("NULL判定をしている", item -> item.nullDecision() ? "◯" : ""),
                        Map.entry("真偽値を返している", item -> item.returnsBoolean() ? "◯" : ""),
                        Map.entry("voidを返している", item -> item.returnsVoid() ? "◯" : "")
                ), methodSmellList.list())
        );
    }

    @HandleDocument(JigDocument.ApplicationList)
    public ReportBook applicationReports(JigDataProvider jigDataProvider) {
        ServiceAngles serviceAngles = jigService.serviceAngles(jigDataProvider);
        DatasourceAngles datasourceAngles = jigService.datasourceAngles(jigDataProvider);
        StringComparingMethodList stringComparingMethodList = jigService.stringComparing(jigDataProvider);
        Entrypoint entrypoint = jigService.entrypoint(jigDataProvider);

        return new ReportBook(
                new ReportSheet<>("CONTROLLER", List.of(
                        Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.jigMethod().declaration().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.jigMethod().declaration().methodReturn().asSimpleText()),
                        Map.entry("クラス別名", item -> item.jigType().label()),
                        Map.entry("使用しているフィールドの型", item -> item.jigMethod().usingFields().typeNames()),
                        Map.entry("分岐数", item -> item.jigMethod().decisionNumber().intValue()),
                        Map.entry("パス", item -> HttpEndpoint.from(item).pathText())
                ), entrypoint.listRequestHandlerMethods()),
                new ReportSheet<>("SERVICE", List.of(
                        Map.entry("パッケージ名", item -> item.serviceMethod().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.serviceMethod().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.method().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.method().methodReturn().asSimpleText()),
                        Map.entry("イベントハンドラ", item -> item.usingFromController() ? "◯" : ""),
                        Map.entry("クラス別名", item -> jigDocumentContext.typeTerm(item.serviceMethod().declaringType()).title()),
                        Map.entry("メソッド別名", item -> item.serviceMethod().method().aliasTextOrBlank()),
                        Map.entry("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.typeTerm(item.serviceMethod().method().declaration().methodReturn().typeIdentifier()).title()
                        ),
                        Map.entry("メソッド引数の型の別名", item ->
                                item.serviceMethod().method().declaration().methodSignature().arguments().stream()
                                        .map(ParameterizedType::typeIdentifier)
                                        .map(jigDocumentContext::typeTerm)
                                        .map(Term::title)
                                        .collect(Collectors.joining(", ", "[", "]"))
                        ),
                        Map.entry("使用しているフィールドの型", item -> item.usingFields().typeNames()),
                        Map.entry("分岐数", item -> item.serviceMethod().method().decisionNumber().intValue()),
                        Map.entry("使用しているサービスのメソッド", item -> item.usingServiceMethods().asSignatureAndReturnTypeSimpleText()),
                        Map.entry("使用しているリポジトリのメソッド", item -> item.usingRepositoryMethods().asSimpleText()),
                        Map.entry("null使用", item -> item.useNull() ? "◯" : ""),
                        Map.entry("stream使用", item -> item.useStream() ? "◯" : "")
                ), serviceAngles.list()),
                new ReportSheet<>("REPOSITORY", List.of(
                        Map.entry("パッケージ名", item -> item.method().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.method().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.method().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.method().methodReturn().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.typeTerm(item.method().declaringType()).title()),
                        Map.entry("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.typeTerm(item.method().methodReturn().typeIdentifier()).title()
                        ),
                        Map.entry("メソッド引数の型の別名", item ->
                                item.method().methodSignature().arguments().stream()
                                        .map(ParameterizedType::typeIdentifier)
                                        .map(jigDocumentContext::typeTerm)
                                        .map(Term::title)
                                        .collect(Collectors.joining(", ", "[", "]"))
                        ),
                        Map.entry("分岐数", item -> item.concreteMethod().decisionNumber().intValue()),
                        Map.entry("INSERT", item -> item.insertTables()),
                        Map.entry("SELECT", item -> item.selectTables()),
                        Map.entry("UPDATE", item -> item.updateTables()),
                        Map.entry("DELETE", item -> item.deleteTables()),
                        Map.entry("関連元クラス数", item -> item.callerMethods().toDeclareTypes().size()),
                        Map.entry("関連元メソッド数", item -> item.callerMethods().size())
                ), datasourceAngles.list()),
                new ReportSheet<>("文字列比較箇所", List.of(
                        Map.entry("パッケージ名", item2 -> item2.declaration().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item2 -> item2.declaration().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item2 -> item2.declaration().asSignatureSimpleText())
                ), stringComparingMethodList.list())
        );
    }

    @Override
    public List<Path> write(ReportBook result, JigDocument jigDocument) {
        return result.writeXlsx(jigDocument, jigDocumentContext.outputDirectory());
    }
}
