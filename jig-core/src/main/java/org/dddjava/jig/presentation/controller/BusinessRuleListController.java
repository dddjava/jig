package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.validations.ValidationAngles;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.business_rule.*;
import org.springframework.stereotype.Controller;

@Controller
public class BusinessRuleListController {

    ConvertContext convertContext;
    ApplicationService applicationService;
    BusinessRuleService businessRuleService;

    public BusinessRuleListController(TypeIdentifierFormatter typeIdentifierFormatter,
                                      AliasService aliasService,
                                      ApplicationService applicationService,
                                      BusinessRuleService businessRuleService) {
        this.convertContext = new ConvertContext(aliasService, typeIdentifierFormatter);
        this.applicationService = applicationService;
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.BusinessRuleList)
    public JigModelAndView<ModelReports> domainList(AnalyzedImplementation implementations) {
        ModelReports modelReports = new ModelReports(
                packageReport(implementations),
                businessRulesReport(implementations),
                valuesReport(ValueKind.IDENTIFIER, implementations),
                categoriesReport(implementations),
                valuesReport(ValueKind.NUMBER, implementations),
                collectionsReport(implementations),
                valuesReport(ValueKind.DATE, implementations),
                valuesReport(ValueKind.TERM, implementations),
                validateAnnotationReport(implementations),
                stringComparingReport(implementations),
                smellReport(implementations)
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    ModelReport<?> stringComparingReport(AnalyzedImplementation implementations) {
        StringComparingAngles stringComparingAngles = applicationService.stringComparing(implementations);
        return new ModelReport<>(stringComparingAngles.list(), StringComparingReport::new, StringComparingReport.class);
    }

    ModelReport<?> packageReport(AnalyzedImplementation implementations) {
        PackageIdentifiers packageIdentifiers = businessRuleService.businessRules(implementations).identifiers().packageIdentifiers();
        return new ModelReport<>(packageIdentifiers.list(), PackageReport::new, PackageReport.class);
    }

    ModelReport<?> businessRulesReport(AnalyzedImplementation implementations) {
        BusinessRules businessRules = businessRuleService.businessRules(implementations);
        return new ModelReport<>(businessRules.list(), BusinessRuleReport::new, BusinessRuleReport.class);
    }

    ModelReport<?> valuesReport(ValueKind valueKind, AnalyzedImplementation implementations) {
        ValueAngles valueAngles = businessRuleService.values(valueKind, implementations);
        return new ModelReport<>(valueKind.name(), valueAngles.list(), ValueReport::new, ValueReport.class);
    }

    ModelReport<?> collectionsReport(AnalyzedImplementation implementations) {
        CollectionAngles collectionAngles = businessRuleService.collections(implementations);
        return new ModelReport<>(collectionAngles.list(), CollectionReport::new, CollectionReport.class);
    }

    ModelReport<?> categoriesReport(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations);
        return new ModelReport<>(categoryAngles.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport(AnalyzedImplementation implementations) {
        ValidationAngles validationAngles = new ValidationAngles(implementations);
        return new ModelReport<>(validationAngles.list(), ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> smellReport(AnalyzedImplementation implementations) {
        MethodSmellAngles angles = businessRuleService.methodSmells(implementations);
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
