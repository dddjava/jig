package org.dddjava.jig.domain.model.models.domains.businessrules;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellList {
    List<MethodSmell> list;

    public MethodSmellList(BusinessRules businessRules, MethodRelations methodRelations) {
        this.list = new ArrayList<>();
        for (JigType jigType : businessRules.list()) {
            for (JigMethod method : jigType.instanceMember().instanceMethods().list()) {
                MethodSmell methodSmell = new MethodSmell(
                        method,
                        jigType.instanceMember().fieldDeclarations(),
                        methodRelations
                );
                if (methodSmell.hasSmell()) {
                    list.add(methodSmell);
                }
            }
        }
    }

    public static List<Map.Entry<String, Function<MethodSmell, Object>>> reporter(JigDocumentContext jigDocumentContext) {
        Function<Boolean, String> ox = b -> b ? "◯" : "";
        return List.of(
                Map.entry("パッケージ名", item -> item.methodDeclaration().declaringType().packageIdentifier().asText()),
                Map.entry("クラス名", item -> item.methodDeclaration().declaringType().asSimpleText()),
                Map.entry("メソッドシグネチャ", item -> item.methodDeclaration().asSignatureSimpleText()),
                Map.entry("メソッド戻り値の型", item -> item.methodDeclaration().methodReturn().asSimpleText()),
                Map.entry("クラス別名", item -> jigDocumentContext.classComment(item.methodDeclaration().declaringType()).asText()),
                Map.entry("使用箇所数", item -> item.callerMethods().size()),
                Map.entry("メンバを使用していない", item -> ox.apply(item.notUseMember())),
                Map.entry("基本型の授受を行なっている", item -> ox.apply(item.primitiveInterface())),
                Map.entry("NULLリテラルを使用している", item -> ox.apply(item.referenceNull())),
                Map.entry("NULL判定をしている", item -> ox.apply(item.nullDecision())),
                Map.entry("真偽値を返している", item -> ox.apply(item.returnsBoolean())),
                Map.entry("voidを返している", item -> ox.apply(item.returnsVoid()))
        );
    }

    public List<MethodSmell> list() {
        return list.stream()
                .sorted(Comparator.comparing(methodSmell -> methodSmell.methodDeclaration().asFullNameText()))
                .collect(Collectors.toList());
    }

    public List<MethodSmell> collectBy(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(methodSmell -> methodSmell.methodDeclaration().declaringType().equals(typeIdentifier))
                .toList();
    }
}
