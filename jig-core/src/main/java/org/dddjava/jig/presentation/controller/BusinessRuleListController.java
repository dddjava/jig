package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackages;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.domains.collections.JigCollectionTypes;
import org.dddjava.jig.domain.model.models.domains.validations.Validations;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.business_rule.*;
import org.springframework.stereotype.Controller;

@Controller
public class BusinessRuleListController {

    final BusinessRuleService businessRuleService;

    public BusinessRuleListController(BusinessRuleService businessRuleService) {
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.TermList)
    public ModelReports termList() {
        Terms terms = businessRuleService.terms();
        return new ModelReports(new ModelReport<>(terms.list(), TermReport::new, TermReport.class));
    }

    @DocumentMapping(JigDocument.DomainSummary)
    public SummaryModel domainListHtml() {
        return SummaryModel.from(businessRuleService.businessRules());
    }

    @DocumentMapping(JigDocument.EnumSummary)
    public SummaryModel enumListHtml() {
        return SummaryModel.from(businessRuleService.categoryTypes(), businessRuleService.enumModels());
    }

    @DocumentMapping(JigDocument.SchemaSummary)
    public SummaryModel schemaHtml() {
        return SummaryModel.from(businessRuleService.businessRules());
    }

    @DocumentMapping(JigDocument.BusinessRuleList)
    public ModelReports domainList() {
        return new ModelReports(
                packageReport(),
                businessRulesReport(),
                categoriesReport(),
                collectionsReport(),
                validateAnnotationReport(),
                smellReport()
        );
    }

    ModelReport<?> packageReport() {
        BusinessRulePackages businessRulePackages = businessRuleService.businessRules().businessRulePackages();
        return new ModelReport<>(businessRulePackages.list(), PackageReport::new, PackageReport.class);
    }

    ModelReport<?> businessRulesReport() {
        BusinessRules businessRules = businessRuleService.businessRules();
        return new ModelReport<>(businessRules.list(), businessRule -> new BusinessRuleReport(businessRule, businessRules), BusinessRuleReport.class);
    }

    ModelReport<?> collectionsReport() {
        JigCollectionTypes jigCollectionTypes = businessRuleService.collections();
        return new ModelReport<>(jigCollectionTypes.listJigType(),
                jigType -> new CollectionReport(jigType, jigCollectionTypes.classRelations()),
                CollectionReport.class);
    }

    ModelReport<?> categoriesReport() {
        CategoryDiagram categoryDiagram = businessRuleService.categories();
        return new ModelReport<>(categoryDiagram.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport() {
        JigTypes jigTypes = businessRuleService.jigTypes();

        return new ModelReport<>(Validations.from(jigTypes).list(), ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> smellReport() {
        MethodSmellList angles = businessRuleService.methodSmells();
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
