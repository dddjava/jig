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

    final ConvertContext convertContext;
    final ApplicationService applicationService;
    final BusinessRuleService businessRuleService;

    public BusinessRuleListController(AliasService aliasService,
                                      ApplicationService applicationService,
                                      BusinessRuleService businessRuleService) {
        this.convertContext = new ConvertContext(aliasService);
        this.applicationService = applicationService;
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.BusinessRuleList)
    public JigModelAndView<ModelReports> domainList() {
        ModelReports modelReports = new ModelReports(
                packageReport(),
                businessRulesReport(),
                valuesReport(ValueKind.IDENTIFIER),
                categoriesReport(),
                valuesReport(ValueKind.NUMBER),
                collectionsReport(),
                valuesReport(ValueKind.DATE),
                valuesReport(ValueKind.TERM),
                validateAnnotationReport(),
                smellReport()
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    ModelReport<?> packageReport() {
        BusinessRulePackages businessRulePackages = businessRuleService.businessRules().businessRulePackages();
        return new ModelReport<>(businessRulePackages.list(), PackageReport::new, PackageReport.class);
    }

    ModelReport<?> businessRulesReport() {
        BusinessRules businessRules = businessRuleService.businessRules();
        return new ModelReport<>(businessRules.list(), BusinessRuleReport::new, BusinessRuleReport.class);
    }

    ModelReport<?> valuesReport(ValueKind valueKind) {
        ValueAngles valueAngles = businessRuleService.values(valueKind);
        return new ModelReport<>(valueKind.name(), valueAngles.list(), ValueReport::new, ValueReport.class);
    }

    ModelReport<?> collectionsReport() {
        CollectionAngles collectionAngles = businessRuleService.collections();
        return new ModelReport<>(collectionAngles.list(), CollectionReport::new, CollectionReport.class);
    }

    ModelReport<?> categoriesReport() {
        Categories categories = businessRuleService.categories();
        return new ModelReport<>(categories.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport() {
        ValidationAnnotatedMembers validationAnnotatedMembers = businessRuleService.validationAnnotatedMembers();

        ValidationAngles validationAngles = ValidationAngles.validationAngles(validationAnnotatedMembers);
        return new ModelReport<>(validationAngles.list(), ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> smellReport() {
        MethodSmellList angles = businessRuleService.methodSmells();
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
