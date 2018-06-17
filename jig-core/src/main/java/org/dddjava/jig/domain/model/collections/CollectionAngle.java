package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.domain.model.report.ReportItemFor;
import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodNumber;
import org.dddjava.jig.domain.model.declaration.method.Methods;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

/**
 * コレクションの切り口
 *
 * 以下を推測したい
 * ・ロジックがいろいろ書かれていそう --> そのクラスのロジックの書き方を重点レビュー
 * ・ロジックがほとんどなさそう --> そのクラスを使っているクラス側にロジックが書かれていないか、レビュー
 */
public class CollectionAngle {

    TypeIdentifier typeIdentifier;
    FieldDeclarations fields;
    MethodDeclarations methods;
    TypeIdentifiers userTypeIdentifiers;

    public CollectionAngle(TypeIdentifier typeIdentifier, FieldDeclarations fieldDeclarations, Methods methods, TypeDependencies allTypeDependencies) {
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = allTypeDependencies.stream()
                .filterTo(typeIdentifier)
                .removeSelf()
                .fromTypeIdentifiers();
        this.fields = fieldDeclarations.filterDeclareTypeIs(typeIdentifier);
        this.methods = methods.declarations().filterDeclareTypeIs(typeIdentifier);
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    @ReportItemFor(ReportItem.使用箇所数)
    public UserNumber userNumber() {
        return new UserNumber(userTypeIdentifiers().list().size());
    }

    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }

    @ReportItemFor(ReportItem.メソッド数)
    public MethodNumber methodNumber() {
        return methods.number();
    }

    @ReportItemFor(ReportItem.メソッド一覧)
    public MethodDeclarations methods() {
        return methods;
    }

}
