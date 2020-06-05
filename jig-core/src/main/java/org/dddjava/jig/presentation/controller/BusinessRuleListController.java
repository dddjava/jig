package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.implementation.MethodSmellList;
import org.dddjava.jig.domain.model.jigdocument.specification.Categories;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRulePackages;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.businessrules.ValueKind;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionAngles;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.jigmodel.validations.ValidationAngles;
import org.dddjava.jig.domain.model.jigmodel.values.ValueAngles;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
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

    public BusinessRuleListController(AliasService aliasService,
                                      ApplicationService applicationService,
                                      BusinessRuleService businessRuleService) {
        this.convertContext = new ConvertContext(aliasService);
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
                smellReport(implementations)
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    ModelReport<?> packageReport(AnalyzedImplementation implementations) {
        BusinessRulePackages businessRulePackages = businessRuleService.businessRules(implementations).businessRulePackages();
        return new ModelReport<>(businessRulePackages.list(), PackageReport::new, PackageReport.class);
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
        Categories categories = businessRuleService.categories(implementations);
        return new ModelReport<>(categories.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport(AnalyzedImplementation implementations) {
        ValidationAnnotatedMembers validationAnnotatedMembers = implementations.typeFacts().validationAnnotatedMembers();

        ValidationAngles validationAngles = ValidationAngles.validationAngles(validationAnnotatedMembers);
        return new ModelReport<>(validationAngles.list(), ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> smellReport(AnalyzedImplementation implementations) {
        MethodSmellList angles = businessRuleService.methodSmells(implementations);
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
