package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.architecture.Layer;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.validations.ValidationAngle;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.branch.DecisionReport;
import org.dddjava.jig.presentation.view.report.business_rule.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClassListController {

    ConvertContext convertContext;
    ApplicationService applicationService;
    BusinessRuleService businessRuleService;

    public ClassListController(TypeIdentifierFormatter typeIdentifierFormatter,
                               GlossaryService glossaryService,
                               ApplicationService applicationService,
                               BusinessRuleService businessRuleService) {
        this.convertContext = new ConvertContext(glossaryService, typeIdentifierFormatter);
        this.applicationService = applicationService;
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.ApplicationList)
    public JigModelAndView<ModelReports> applicationList(AnalyzedImplementation implementations) {
        ModelReports modelReports = new ModelReports(
                controllerReport(implementations),
                serviceReport(implementations),
                datasourceReport(implementations)
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    @DocumentMapping(JigDocument.BusinessRuleList)
    public JigModelAndView<ModelReports> domainList(AnalyzedImplementation implementations) {
        ModelReports modelReports = new ModelReports(
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

    @DocumentMapping(JigDocument.BranchList)
    public JigModelAndView<ModelReports> branchList(AnalyzedImplementation implementations) {
        ModelReports modelReports = new ModelReports(
                decisionReport(implementations, Layer.PRESENTATION),
                decisionReport(implementations, Layer.APPLICATION),
                decisionReport(implementations, Layer.DATASOURCE)
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    ModelReport<?> controllerReport(AnalyzedImplementation implementations) {
        ControllerAngles controllerAngles = applicationService.controllerAngles(implementations.typeByteCodes());
        ProgressAngles progressAngles = applicationService.progressAngles(implementations.typeByteCodes());

        return new ModelReport<>(controllerAngles.list(),
                controllerAngle -> new ControllerReport(controllerAngle, progressAngles.progressOf(controllerAngle.method().declaration())),
                ControllerReport.class);
    }

    ModelReport<?> serviceReport(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations.typeByteCodes());
        ProgressAngles progressAngles = applicationService.progressAngles(implementations.typeByteCodes());

        return new ModelReport<>(serviceAngles.list(),
                serviceAngle -> new ServiceReport(serviceAngle, progressAngles.progressOf(serviceAngle.method())),
                ServiceReport.class);
    }

    ModelReport<?> datasourceReport(AnalyzedImplementation implementations) {
        DatasourceAngles datasourceAngles = applicationService.datasourceAngles(implementations.typeByteCodes(), implementations.sqls());
        return new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class);
    }

    ModelReport<?> stringComparingReport(AnalyzedImplementation implementations) {
        StringComparingAngles stringComparingAngles = applicationService.stringComparing(implementations.typeByteCodes());
        return new ModelReport<>(stringComparingAngles.list(), StringComparingReport::new, StringComparingReport.class);
    }

    ModelReport<?> businessRulesReport(AnalyzedImplementation implementations) {
        BusinessRules businessRules = businessRuleService.businessRules(implementations.typeByteCodes());
        return new ModelReport<>(businessRules.list(), BusinessRuleReport::new, BusinessRuleReport.class);
    }

    ModelReport<?> valuesReport(ValueKind valueKind, AnalyzedImplementation implementations) {
        ValueAngles valueAngles = businessRuleService.values(valueKind, implementations.typeByteCodes());
        return new ModelReport<>(valueKind.name(), valueAngles.list(), ValueReport::new, ValueReport.class);
    }

    ModelReport<?> collectionsReport(AnalyzedImplementation implementations) {
        CollectionAngles collectionAngles = businessRuleService.collections(implementations.typeByteCodes());
        return new ModelReport<>(collectionAngles.list(), CollectionReport::new, CollectionReport.class);
    }

    ModelReport<?> categoriesReport(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations.typeByteCodes());
        return new ModelReport<>(categoryAngles.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport(AnalyzedImplementation implementations) {
        TypeByteCodes typeByteCodes = implementations.typeByteCodes();
        ValidationAnnotatedMembers validationAnnotatedMembers = new ValidationAnnotatedMembers(typeByteCodes.annotatedFields(), typeByteCodes.annotatedMethods());
        List<ValidationAngle> list = validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList());
        return new ModelReport<>(list, ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> decisionReport(AnalyzedImplementation implementations, Layer layer) {
        DecisionAngles decisionAngles = applicationService.decision(implementations.typeByteCodes());
        return new ModelReport<>(layer.asText(), decisionAngles.filter(layer), DecisionReport::new, DecisionReport.class);
    }

    ModelReport<?> smellReport(AnalyzedImplementation implementations) {
        MethodSmellAngles angles = businessRuleService.methodSmells(implementations.typeByteCodes());
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
