package org.dddjava.jig.adapter.excel;

import org.dddjava.jig.adapter.Adapter;
import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSource;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.classes.type.TypeVisibility;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.information.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.package_.PackageJigTypes;
import org.dddjava.jig.domain.model.information.validations.Validations;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListAdapter implements Adapter<ReportBook> {
    private static final Logger logger = LoggerFactory.getLogger(ListAdapter.class);

    private final JigDocumentContext jigDocumentContext;
    private final JigService jigService;

    public ListAdapter(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocumentContext = jigDocumentContext;
        this.jigService = jigService;
    }

    @HandleDocument(JigDocument.BusinessRuleList)
    public ReportBook businessRuleReports(JigSource jigSource) {
        TypeFacts typeFacts = jigSource.typeFacts();
        var allClassRelations = typeFacts.toClassRelations();

        MethodSmellList methodSmellList = jigService.methodSmells(jigSource);
        JigTypes jigTypes = jigService.jigTypes(jigSource);
        BusinessRules businessRules = jigService.businessRules(jigSource);
        CategoryTypes categoryTypes = jigService.categoryTypes(jigSource);
        List<PackageJigTypes> packageJigTypes = businessRules.listPackages();
        return new ReportBook(
                new ReportSheet<>("PACKAGE", List.of(
                        Map.entry("パッケージ名", item -> item.packageIdentifier().asText()),
                        Map.entry("パッケージ別名", item -> jigDocumentContext.packageComment(item.packageIdentifier()).asText()),
                        Map.entry("クラス数", item -> item.jigTypes().size())
                ), packageJigTypes),
                new ReportSheet<>("ALL", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
                        Map.entry("ビジネスルールの種類", item -> item.toValueKind().toString()),
                        Map.entry("関連元ビジネスルール数", item -> businessRules.businessRuleRelations().filterTo(item.typeIdentifier()).fromTypeIdentifiers().size()),
                        Map.entry("関連先ビジネスルール数", item -> businessRules.businessRuleRelations().filterFrom(item.typeIdentifier()).toTypeIdentifiers().size()),
                        Map.entry("関連元クラス数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).list().size()),
                        Map.entry("非PUBLIC", item -> item.visibility() != TypeVisibility.PUBLIC ? "◯" : ""),
                        Map.entry("同パッケージからのみ参照", item -> {
                            var list = allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).packageIdentifiers().list();
                            return list.size() == 1 && list.get(0).equals(item.typeIdentifier().packageIdentifier()) ? "◯" : "";
                        }),
                        Map.entry("関連元クラス", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).asSimpleText())
                ), businessRules.list()),
                new ReportSheet<>("ENUM", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
                        Map.entry("定数宣言", item -> item.constantsDeclarationsName()),
                        Map.entry("フィールド", item -> item.fieldDeclarations().toSignatureText()),
                        Map.entry("使用箇所数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).list().size()),
                        Map.entry("使用箇所", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.typeIdentifier()).asSimpleText()),
                        Map.entry("パラメーター有り", item -> item.hasParameter() ? "◯" : ""),
                        Map.entry("振る舞い有り", item -> item.hasBehaviour() ? "◯" : ""),
                        Map.entry("多態", item -> item.isPolymorphism() ? "◯" : "")
                ), categoryTypes.list()),
                new ReportSheet<>("COLLECTION", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
                        Map.entry("フィールドの型", item -> item.instanceMember().fieldDeclarations().onlyOneField().fieldType().asSimpleText()), // TODO: onlyOne複数に対応する。型引数を出力したいのでFieldTypeを使用している。
                        Map.entry("使用箇所数", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.identifier()).size()),
                        Map.entry("使用箇所", item -> allClassRelations.collectTypeIdentifierWhichRelationTo(item.identifier()).asSimpleText()),
                        Map.entry("メソッド数", item -> item.instanceMember().instanceMethods().list().size()),
                        Map.entry("メソッド一覧", item -> item.instanceMember().instanceMethods().declarations().asSignatureAndReturnTypeSimpleText())
                ), businessRules.jigTypes().listCollectionType()),
                new ReportSheet<>("VALIDATION", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.typeIdentifier()).asText()),
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
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.methodDeclaration().declaringType()).asText()),
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
    public ReportBook applicationReports(JigSource jigSource) {
        ServiceAngles serviceAngles = jigService.serviceAngles(jigSource);
        DatasourceAngles datasourceAngles = jigService.datasourceAngles(jigSource);
        StringComparingMethodList stringComparingMethodList = jigService.stringComparing(jigSource);
        Entrypoint entrypoint = jigService.entrypoint(jigSource);

        if (entrypoint.isEmpty()) {
            logger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知.localizedMessage());
        }

        return new ReportBook(
                new ReportSheet<>("CONTROLLER", List.of(
                        Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.method().declaration().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.method().declaration().methodReturn().asSimpleText()),
                        Map.entry("クラス別名", item -> item.jigType().typeAlias().asText()),
                        Map.entry("使用しているフィールドの型", item -> item.method().usingFields().typeIdentifiers().asSimpleText()),
                        Map.entry("分岐数", item -> item.method().decisionNumber().intValue()),
                        Map.entry("パス", item -> item.pathText())
                ), entrypoint.listRequestHandlerMethods()),
                new ReportSheet<>("SERVICE", List.of(
                        Map.entry("パッケージ名", item -> item.serviceMethod().declaringType().packageIdentifier().asText()),
                        Map.entry("クラス名", item -> item.serviceMethod().declaringType().asSimpleText()),
                        Map.entry("メソッドシグネチャ", item -> item.method().asSignatureSimpleText()),
                        Map.entry("メソッド戻り値の型", item -> item.method().methodReturn().asSimpleText()),
                        Map.entry("イベントハンドラ", item -> item.usingFromController() ? "◯" : ""),
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.serviceMethod().declaringType()).asText()),
                        Map.entry("メソッド別名", item -> item.serviceMethod().method().aliasTextOrBlank()),
                        Map.entry("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.classComment(item.serviceMethod().method().declaration().methodReturn().typeIdentifier()).asText()
                        ),
                        Map.entry("メソッド引数の型の別名", item ->
                                item.serviceMethod().method().declaration().methodSignature().arguments().stream()
                                        .map(ParameterizedType::typeIdentifier)
                                        .map(jigDocumentContext::classComment)
                                        .map(ClassComment::asText)
                                        .collect(Collectors.joining(", ", "[", "]"))
                        ),
                        Map.entry("使用しているフィールドの型", item -> item.usingFields().typeIdentifiers().asSimpleText()),
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
                        Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.method().declaringType()).asText()),
                        Map.entry("メソッド戻り値の型の別名", item ->
                                jigDocumentContext.classComment(item.method().methodReturn().typeIdentifier()).asText()
                        ),
                        Map.entry("メソッド引数の型の別名", item ->
                                item.method().methodSignature().arguments().stream()
                                        .map(ParameterizedType::typeIdentifier)
                                        .map(jigDocumentContext::classComment)
                                        .map(ClassComment::asText)
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
