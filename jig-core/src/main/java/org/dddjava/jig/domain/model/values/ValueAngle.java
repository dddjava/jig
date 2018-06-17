package org.dddjava.jig.domain.model.values;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.ReportItemFor;
import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;

/**
 * 値の切り口
 */
public class ValueAngle {

    ValueKind valueKind;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public ValueAngle(ValueKind valueKind, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers) {
        this.valueKind = valueKind;
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = userTypeIdentifiers;
    }

    public static ValueAngle of(ValueKind valueKind, TypeDependencies allTypeDependencies, TypeIdentifier typeIdentifier) {
        TypeIdentifiers userTypeIdentifiers = allTypeDependencies.stream()
                .filterTo(typeIdentifier)
                .removeSelf()
                .fromTypeIdentifiers();
        return new ValueAngle(valueKind, typeIdentifier, userTypeIdentifiers);
    }

    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス和名)
    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    @ReportItemFor(ReportItem.使用箇所)
    public TypeIdentifiers userTypeIdentifiers() {
        return userTypeIdentifiers;
    }

    @ReportItemFor(ReportItem.使用箇所数)
    public UserNumber userNumber() {
        return new UserNumber(userTypeIdentifiers().list().size());
    }
}
