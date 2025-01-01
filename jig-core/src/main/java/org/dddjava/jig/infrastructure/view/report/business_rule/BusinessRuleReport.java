package org.dddjava.jig.infrastructure.view.report.business_rule;

import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeDeclaration;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.infrastructure.view.report.ReportItem;
import org.dddjava.jig.infrastructure.view.report.ReportItemFor;
import org.dddjava.jig.infrastructure.view.report.ReportTitle;

import java.util.List;

@ReportTitle("ALL")
public class BusinessRuleReport {

    JigType jigType;
    BusinessRules businessRules;

    public BusinessRuleReport(JigType jigType, BusinessRules businessRules) {
        this.jigType = jigType;
        this.businessRules = businessRules;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public TypeDeclaration type() {
        return jigType.jigType().typeDeclaration();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "ビジネスルールの種類", order = 11)
    public String valueKind() {
        return jigType.toValueKind().toString();
    }

    @ReportItemFor(value = ReportItem.使用箇所数, label = "関連元クラス数", order = 23)
    @ReportItemFor(value = ReportItem.使用箇所, label = "関連元クラス", order = 100)
    public TypeIdentifiers userTypeIdentifiers() {
        return businessRules.allTypesRelatedTo(jigType);
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "関連元ビジネスルール数", order = 21)
    public int ビジネスルール使用箇所数() {
        ClassRelations classRelations = businessRules.businessRuleRelations().filterTo(jigType.typeIdentifier());
        TypeIdentifiers typeIdentifiers = classRelations.fromTypeIdentifiers();
        return typeIdentifiers.size();
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "関連先ビジネスルール数", order = 22)
    public int 参照ビジネスルール数() {
        ClassRelations classRelations = businessRules.businessRuleRelations().filterFrom(jigType.typeIdentifier());
        TypeIdentifiers typeIdentifiers = classRelations.toTypeIdentifiers();
        return typeIdentifiers.size();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "非PUBLIC", order = 31)
    public boolean isNotPublic() {
        return jigType.jigType().visibility() != Visibility.PUBLIC;
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "同パッケージからのみ参照", order = 32)
    public boolean useFromSamePackage() {
        List<PackageIdentifier> list = userTypeIdentifiers().packageIdentifiers().list();
        return list.size() == 1 && list.get(0).equals(jigType.typeIdentifier().packageIdentifier());
    }
}
