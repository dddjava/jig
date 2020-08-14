package org.dddjava.jig.presentation.view.report.business_rule;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleTendency;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
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
        return businessRule.typeDeclaration();
    }

    @ReportItemFor(ReportItem.使用箇所数)
    public TypeIdentifiers userTypeIdentifiers() {
        return businessRules.allTypesRelatedTo(businessRule);
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "ビジネスルール使用箇所数")
    public int ビジネスルール使用箇所数() {
        ClassRelations classRelations = businessRules.businessRuleRelations().filterTo(businessRule.typeIdentifier());
        TypeIdentifiers typeIdentifiers = classRelations.fromTypeIdentifiers();
        return typeIdentifiers.size();
    }

    @ReportItemFor(value = ReportItem.汎用数値, label = "参照ビジネスルール数")
    public int 参照ビジネスルール数() {
        ClassRelations classRelations = businessRules.businessRuleRelations().filterFrom(businessRule.typeIdentifier());
        TypeIdentifiers typeIdentifiers = classRelations.toTypeIdentifiers();
        return typeIdentifiers.size();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "非PUBLIC")
    public boolean isNotPublic() {
        return !businessRule.visibility().isPublic();
    }

    @ReportItemFor(value = ReportItem.汎用真偽値, label = "同パッケージからのみ参照")
    public boolean useFromSamePackage() {
        List<PackageIdentifier> list = userTypeIdentifiers().packageIdentifiers().list();
        return list.size() == 1 && list.get(0).equals(businessRule.typeIdentifier().packageIdentifier());
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "ビジネスルールの種類")
    public String valueKind() {
        return businessRule.businessRuleCategory().toString();
    }

    @ReportItemFor(value = ReportItem.汎用文字列, label = "型の傾向")
    public String tendency() {
        return BusinessRuleTendency.from(businessRule, businessRules).toString();
    }
}
