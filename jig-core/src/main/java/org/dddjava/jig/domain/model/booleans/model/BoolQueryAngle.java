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

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier declaringTypeIdentifier() {
        return method.declaringType();
    }

    @ReportItemFor(ReportItem.メソッド名)
    @ReportItemFor(ReportItem.メソッド和名)
    public MethodDeclaration method() {
        return method;
    }

    @ReportItemFor(ReportItem.使用箇所数)
    public UserNumber userNumber() {
        // TODO typeIdentifiersに移動？
        return new UserNumber(userTypeIdentifiers().list().size());
    }

    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return usages.declaringTypes();
    }
}
