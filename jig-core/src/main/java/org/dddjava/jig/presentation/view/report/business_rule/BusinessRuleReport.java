package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.parts.method.Visibility;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.type.TypeDeclaration;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.dddjava.jig.presentation.view.report.ReportItemFor;
import org.dddjava.jig.presentation.view.report.ReportTitle;

import java.util.List;

@ReportTitle("ALL")
public class BusinessRuleReport {

    BusinessRule businessRule;
    BusinessRules businessRules;

    public BusinessRuleReport(BusinessRule businessRule, BusinessRules businessRules) {
        this.businessRule = businessRule;
        this.businessRules = businessRules;
    }

    @ReportItemFor(ReportItem.パッケージ名)
    @ReportItemFor(ReportItem.クラス名)
    @ReportItemFor(ReportItem.クラス別名)
    public TypeDeclaration type() {
        return businessRule.jigType().typeDeclaration();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "ビジネスルールの種類", order = 11)
    public String valueKind() {
        return businessRule.toValueKind().toString();
    }

    @ReportItemFor(value = ReportItem.使用箇所数, label = "関連元クラス数", order = 23)
    @ReportItemFor(value = ReportItem.使用箇所, label = "関連元クラス", order = 100)
    public TypeIdentifiers userTypeIdentifiers() {
        return businessRules.allTypesRelatedTo(businessRule);
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "関連元ビジネスルール数", order = 21)
    public int ビジネスルール使用箇所数() {
        ClassRelations classRelations = businessRules.businessRuleRelations().filterTo(businessRule.typeIdentifier());
        TypeIdentifiers typeIdentifiers = classRelations.fromTypeIdentifiers();
        return typeIdentifiers.size();
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "関連先ビジネスルール数", order = 22)
    public int 参照ビジネスルール数() {
        ClassRelations classRelations = businessRules.businessRuleRelations().filterFrom(businessRule.typeIdentifier());
        TypeIdentifiers typeIdentifiers = classRelations.toTypeIdentifiers();
        return typeIdentifiers.size();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "非PUBLIC", order = 31)
    public boolean isNotPublic() {
        return businessRule.jigType().visibility() != Visibility.PUBLIC;
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "同パッケージからのみ参照", order = 32)
    public boolean useFromSamePackage() {
        List<PackageIdentifier> list = userTypeIdentifiers().packageIdentifiers().list();
        return list.size() == 1 && list.get(0).equals(businessRule.typeIdentifier().packageIdentifier());
    }
}
