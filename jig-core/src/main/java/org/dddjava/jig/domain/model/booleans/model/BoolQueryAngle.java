package org.dddjava.jig.domain.model.booleans.model;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

/**
 * 真偽値を返すモデルの切り口
 */
public class BoolQueryAngle {
    MethodDeclaration method;
    MethodDeclarations usages;

    public BoolQueryAngle(MethodDeclaration method, MethodDeclarations usages) {
        this.method = method;
        this.usages = usages;
    }

    @ReportItemFor(item = ReportItem.クラス名, order = 1)
    @ReportItemFor(item = ReportItem.クラス和名, order = 2)
    public TypeIdentifier declaringTypeIdentifier() {
        return method.declaringType();
    }

    @ReportItemFor(item = ReportItem.メソッド名, order = 3)
    @ReportItemFor(item = ReportItem.メソッド和名, order = 4)
    public MethodDeclaration method() {
        return method;
    }

    @ReportItemFor(item = ReportItem.使用箇所数, order = 5)
    public UserNumber userNumber() {
        // TODO typeIdentifiersに移動？
        return new UserNumber(userTypeIdentifiers().list().size());
    }

    @ReportItemFor(item = ReportItem.使用箇所, order = 6)
    public TypeIdentifiers userTypeIdentifiers() {
        return usages.declaringTypes();
    }
}
