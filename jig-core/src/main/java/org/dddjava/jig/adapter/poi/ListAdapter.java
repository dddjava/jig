package org.dddjava.jig.adapter.poi;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.JigTypeVisibility;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeKind;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngle;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.module.JigPackageWithJigTypes;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmells;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.validations.Validations;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

import static java.util.stream.Collectors.joining;

/**
 * 一覧のAdapter
 */
@HandleDocument
public class ListAdapter {

    /**
     * 一覧出力で複数要素を文字列連結する際のコレクター
     */
    private static final Collector<CharSequence, ?, String> STREAM_COLLECTOR = joining(", ", "[", "]");

    private final JigDocumentContext jigDocumentContext;
    private final JigService jigService;

    public ListAdapter(JigDocumentContext jigDocumentContext, JigService jigService) {
        this.jigDocumentContext = jigDocumentContext;
        this.jigService = jigService;
    }

    @HandleDocument(JigDocument.BusinessRuleList)
    public List<Path> businessRuleReports(JigRepository jigRepository, JigDocument jigDocument) {

        MethodSmells methodSmells = jigService.methodSmells(jigRepository);
        JigTypes jigTypes = jigService.jigTypes(jigRepository);
        var allClassRelations = TypeRelationships.from(jigTypes);

        CoreTypesAndRelations coreTypesAndRelations = jigService.coreTypesAndRelations(jigRepository);
        JigTypes coreDomainJigTypes = coreTypesAndRelations.coreJigTypes();
        JigTypes categoryTypes = jigService.categoryTypes(jigRepository);
        List<JigPackageWithJigTypes> jigTypePackages = JigPackageWithJigTypes.from(coreDomainJigTypes);
        var result = new ReportBook(
                new ReportSheet<>("PACKAGE", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageId().asText()),
                        ReportItem.ofString("パッケージ別名", item -> jigDocumentContext.packageTerm(item.packageId()).title()),
                        ReportItem.ofNumber("クラス数", item -> item.jigTypes().size())
                ), jigTypePackages),
                new ReportSheet<>("ALL", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.id().asSimpleText()),
                        ReportItem.ofString("クラス別名", JigType::label),
                        ReportItem.ofString("ビジネスルールの種類", item -> item.toValueKind().toString()),
                        ReportItem.ofNumber("関連元ビジネスルール数", item -> coreTypesAndRelations.internalTypeRelationships().filterTo(item.id()).size()),
                        ReportItem.ofNumber("関連先ビジネスルール数", item -> coreTypesAndRelations.internalTypeRelationships().filterFrom(item.id()).size()),
                        ReportItem.ofNumber("関連元クラス数", item -> allClassRelations.collectTypeIdWhichRelationTo(item.id()).list().size()),
                        ReportItem.ofString("非PUBLIC", item -> markIfTrue(item.visibility() != JigTypeVisibility.PUBLIC)),
                        ReportItem.ofString("同パッケージからのみ参照", item -> {
                            var identifiers = allClassRelations.collectTypeIdWhichRelationTo(item.id()).packageIds().values();
                            return markIfTrue(identifiers.equals(Set.of(item.packageId())));
                        }),
                        ReportItem.ofString("関連元クラス", item -> allClassRelations.collectTypeIdWhichRelationTo(item.id()).asSimpleText())
                ), coreDomainJigTypes.list()),
                new ReportSheet<>("ENUM", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.id().asSimpleText()),
                        ReportItem.ofString("クラス別名", JigType::label),
                        ReportItem.ofString("定数宣言", item -> item.jigTypeMembers().enumConstantStream()
                                .map(jigField -> jigField.jigFieldHeader().name()).collect(STREAM_COLLECTOR)),
                        ReportItem.ofString("フィールド", item -> item.jigTypeMembers().instanceFields().stream()
                                .map(jigField -> jigField.jigFieldHeader().simpleText())
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofNumber("使用箇所数", item -> allClassRelations.collectTypeIdWhichRelationTo(item.id()).list().size()),
                        ReportItem.ofString("使用箇所", item -> allClassRelations.collectTypeIdWhichRelationTo(item.id()).asSimpleText()),
                        // TODO: パラメータあり＝フィールドありは直接はつながらない
                        ReportItem.ofString("パラメーター有り", item -> markIfTrue(item.hasInstanceField())),
                        ReportItem.ofString("振る舞い有り", item -> markIfTrue(item.hasInstanceMethod())),
                        // 抽象列挙型は継承クラスがコンパイラに作成されているもので、多態とみなすことにする
                        ReportItem.ofString("多態", item -> markIfTrue(item.typeKind() == TypeKind.抽象列挙型))
                ), categoryTypes.list()),
                new ReportSheet<>("COLLECTION", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.id().asSimpleText()),
                        ReportItem.ofString("クラス別名", JigType::label),
                        ReportItem.ofString("フィールドの型", item -> {
                            List<String> list = item.jigTypeMembers().instanceFields().stream()
                                    .map(jigField -> jigField.jigTypeReference().simpleNameWithGenerics())
                                    .toList();
                            return list.size() == 1 ? list.get(0) : list.stream().collect(STREAM_COLLECTOR);
                        }),
                        ReportItem.ofNumber("使用箇所数", item -> allClassRelations.collectTypeIdWhichRelationTo(item.id()).size()),
                        ReportItem.ofString("使用箇所", item -> allClassRelations.collectTypeIdWhichRelationTo(item.id()).asSimpleText()),
                        ReportItem.ofNumber("メソッド数", item -> item.instanceJigMethods().list().size()),
                        ReportItem.ofString("メソッド一覧", item -> item.instanceJigMethods().stream().map(JigMethod::nameArgumentsReturnSimpleText).sorted().collect(STREAM_COLLECTOR))
                ), coreDomainJigTypes.listCollectionType()),
                new ReportSheet<>("VALIDATION", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.typeId().packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.typeId().asSimpleText()),
                        ReportItem.ofString("クラス別名", item -> jigDocumentContext.typeTerm(item.typeId()).title()),
                        ReportItem.ofString("メンバ名", item -> item.memberName()),
                        ReportItem.ofString("メンバクラス名", item -> item.memberType().asSimpleText()),
                        ReportItem.ofString("アノテーションクラス名", item -> item.annotationType().asSimpleText()),
                        ReportItem.ofString("アノテーション記述", item -> item.annotationDescription())
                ), Validations.from(jigTypes).list()),
                new ReportSheet<>("注意メソッド", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.method().declaringType().packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.method().declaringType().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.method().nameAndArgumentSimpleText()),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.methodReturnType().asSimpleText()),
                        ReportItem.ofString("クラス別名", item -> item.declaringJigType().label()),
                        ReportItem.ofString("メンバを使用していない", item -> markIfTrue(item.notUseMember())),
                        ReportItem.ofString("基本型の授受を行なっている", item -> markIfTrue(item.primitiveInterface())),
                        ReportItem.ofString("NULLリテラルを使用している", item -> markIfTrue(item.referenceNull())),
                        ReportItem.ofString("NULL判定をしている", item -> markIfTrue(item.nullDecision())),
                        ReportItem.ofString("真偽値を返している", item -> markIfTrue(item.returnsBoolean())),
                        ReportItem.ofString("voidを返している", item -> markIfTrue(item.returnsVoid()))
                ), methodSmells.list())
        );
        return result.writeXlsx(jigDocument, jigDocumentContext.outputDirectory());
    }

    @HandleDocument(JigDocument.ApplicationList)
    public List<Path> applicationReports(JigRepository jigRepository, JigDocument jigDocument) {
        ServiceAngles serviceAngles = jigService.serviceAngles(jigRepository);
        DatasourceAngles datasourceAngles = jigService.datasourceAngles(jigRepository);
        StringComparingMethodList stringComparingMethodList = jigService.stringComparing(jigRepository);
        InputAdapters inputAdapters = jigService.inputAdapters(jigRepository);

        var result = new ReportBook(
                new ReportSheet<>("CONTROLLER", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.typeId().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.jigMethod().nameAndArgumentSimpleText()),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.jigMethod().methodReturnTypeReference().simpleName()),
                        ReportItem.ofString("クラス別名", item -> item.jigType().label()),
                        ReportItem.ofString("使用しているフィールドの型", item -> item.jigMethod().usingFields().jigFieldIds().stream()
                                .map(JigFieldId::declaringTypeId)
                                .map(TypeId::asSimpleText)
                                .sorted()
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofNumber("循環的複雑度", item -> item.jigMethod().instructions().cyclomaticComplexity()),
                        ReportItem.ofString("パス", item -> item.fullPathText())
                ), inputAdapters.listEntrypoint()),
                new ReportSheet<>("SERVICE", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.serviceMethod().declaringType().packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.serviceMethod().declaringType().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.serviceMethod().method().nameAndArgumentSimpleText()),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.serviceMethod().method().methodReturnTypeReference().simpleName()),
                        ReportItem.ofString("イベントハンドラ", item -> markIfTrue(item.usingFromController())),
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
                        ReportItem.ofString("使用しているフィールドの型", item -> item.usingFields().jigFieldIds().stream()
                                .map(JigFieldId::declaringTypeId)
                                .map(TypeId::asSimpleText)
                                .sorted()
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofNumber("循環的複雑度", item -> item.serviceMethod().method().instructions().cyclomaticComplexity()),
                        ReportItem.ofString("使用しているサービスのメソッド", item -> item.usingServiceMethods().stream().map(invokedMethod -> invokedMethod.asSignatureAndReturnTypeSimpleText()).collect(STREAM_COLLECTOR)),
                        ReportItem.ofString("使用しているリポジトリのメソッド", item -> item.usingRepositoryMethods().list().stream()
                                .map(JigMethod::nameAndArgumentSimpleText)
                                .collect(STREAM_COLLECTOR)),
                        ReportItem.ofString("null使用", item -> markIfTrue(item.useNull())),
                        ReportItem.ofString("stream使用", item -> markIfTrue(item.useStream()))
                ), serviceAngles.list()),
                new ReportSheet<>("REPOSITORY", List.of(
                        ReportItem.ofString("パッケージ名", DatasourceAngle::packageText),
                        ReportItem.ofString("クラス名", DatasourceAngle::typeSimpleName),
                        ReportItem.ofString("メソッドシグネチャ", DatasourceAngle::nameAndArgumentSimpleText),
                        ReportItem.ofString("メソッド戻り値の型", item -> item.methodReturnTypeReference().simpleNameWithGenerics()),
                        ReportItem.ofString("クラス別名", DatasourceAngle::typeLabel),
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
                        ReportItem.ofNumber("循環的複雑度", DatasourceAngle::cyclomaticComplexity),
                        ReportItem.ofString("INSERT", DatasourceAngle::insertTables),
                        ReportItem.ofString("SELECT", DatasourceAngle::selectTables),
                        ReportItem.ofString("UPDATE", DatasourceAngle::updateTables),
                        ReportItem.ofString("DELETE", DatasourceAngle::deleteTables),
                        ReportItem.ofNumber("関連元クラス数", item -> item.callerMethods().typeCount()),
                        ReportItem.ofNumber("関連元メソッド数", item -> item.callerMethods().size())
                ), datasourceAngles.list()),
                new ReportSheet<>("文字列比較箇所", List.of(
                        ReportItem.ofString("パッケージ名", item -> item.jigMethodDeclaration().declaringTypeId().packageId().asText()),
                        ReportItem.ofString("クラス名", item -> item.jigMethodDeclaration().declaringTypeId().asSimpleText()),
                        ReportItem.ofString("メソッドシグネチャ", item -> item.nameAndArgumentSimpleText())
                ), stringComparingMethodList.list())
        );
        return result.writeXlsx(jigDocument, jigDocumentContext.outputDirectory());
    }

    private static String markIfTrue(boolean b) {
        return b ? "◯" : "";
    }
}
