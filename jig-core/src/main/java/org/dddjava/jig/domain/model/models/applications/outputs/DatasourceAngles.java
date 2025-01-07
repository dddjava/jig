package org.dddjava.jig.domain.model.models.applications.outputs;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * データソースの切り口一覧
 */
public class DatasourceAngles {

    List<DatasourceAngle> list;

    public DatasourceAngles(DatasourceMethods datasourceMethods, Sqls sqls, MethodRelations methodRelations) {
        List<DatasourceAngle> list = new ArrayList<>();
        for (DatasourceMethod datasourceMethod : datasourceMethods.list()) {
            CallerMethods callerMethods = methodRelations.callerMethodsOf(datasourceMethod.repositoryMethod().declaration());
            list.add(new DatasourceAngle(datasourceMethod, sqls, callerMethods));
        }
        this.list = list;
    }

    public List<DatasourceAngle> list() {
        return list.stream()
                .sorted(Comparator.comparing(datasourceAngle -> datasourceAngle.method().asFullNameText()))
                .collect(Collectors.toList());
    }


    public static List<Map.Entry<String, Function<DatasourceAngle, Object>>> reporter(JigDocumentContext jigDocumentContext) {
        // TODO jigDocumentContext を使わずに名前解決をできるようにしたい
        return List.of(
                Map.entry("パッケージ名", item -> item.method().declaringType().packageIdentifier().asText()),
                Map.entry("クラス名", item -> item.method().declaringType().asSimpleText()),
                Map.entry("メソッドシグネチャ", item -> item.method().asSignatureSimpleText()),
                Map.entry("メソッド戻り値の型", item -> item.method().methodReturn().asSimpleText()),
                Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.method().declaringType()).asText()),
                Map.entry("メソッド戻り値の型の別名", item ->
                        jigDocumentContext.classComment(item.method().methodReturn().typeIdentifier()).asText()
                ),
                Map.entry("メソッド引数の型の別名", item ->
                        item.method().methodSignature().listArgumentTypeIdentifiers().stream()
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
        );
    }
}
