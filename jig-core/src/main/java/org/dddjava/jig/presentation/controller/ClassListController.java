package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.Layer;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.validations.ValidationAngle;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.PoiView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.branch.DecisionReport;
import org.dddjava.jig.presentation.view.report.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClassListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListController.class);

    ConvertContext convertContext;
    AngleService angleService;
    BusinessRuleService businessRuleService;

    public ClassListController(TypeIdentifierFormatter typeIdentifierFormatter,
                               GlossaryService glossaryService,
                               AngleService angleService,
                               BusinessRuleService businessRuleService) {
        this.convertContext = new ConvertContext(glossaryService, typeIdentifierFormatter);
        this.angleService = angleService;
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.ApplicationList)
    public JigModelAndView<ModelReports> applicationList(ProjectData projectData) {
        LOGGER.info("入出力リストを出力します");
        ModelReports modelReports = new ModelReports(
                controllerReport(projectData),
                serviceReport(projectData),
                datasourceReport(projectData)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.DomainList)
    public JigModelAndView<ModelReports> domainList(ProjectData projectData) {
        LOGGER.info("ビジネスルールリストを出力します");
        ModelReports modelReports = new ModelReports(
                businessRulesReport(projectData),
                valuesReport(ValueKind.IDENTIFIER, projectData),
                categoriesReport(projectData),
                valuesReport(ValueKind.NUMBER, projectData),
                collectionsReport(projectData),
                valuesReport(ValueKind.DATE, projectData),
                valuesReport(ValueKind.TERM, projectData),
                validateAnnotationReport(projectData),
                stringComparingReport(projectData),
                smellReport(projectData)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.BranchList)
    public JigModelAndView<ModelReports> branchList(ProjectData projectData) {
        LOGGER.info("条件分岐リストを出力します");
        ModelReports modelReports = new ModelReports(
                decisionReport(projectData, Layer.PRESENTATION),
                decisionReport(projectData, Layer.APPLICATION),
                decisionReport(projectData, Layer.DATASOURCE)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    ModelReport<?> controllerReport(ProjectData projectData) {
        ControllerAngles controllerAngles = angleService.controllerAngles(projectData);
        ProgressAngles progressAngles = angleService.progressAngles(projectData);

        return new ModelReport<>(controllerAngles.list(),
                controllerAngle -> new ControllerReport(controllerAngle, progressAngles.progressOf(controllerAngle.method().declaration())),
                ControllerReport.class);
    }

    ModelReport<?> serviceReport(ProjectData projectData) {
        ServiceAngles serviceAngles = angleService.serviceAngles(projectData);
        ProgressAngles progressAngles = angleService.progressAngles(projectData);

        return new ModelReport<>(serviceAngles.list(),
                serviceAngle -> new ServiceReport(serviceAngle, progressAngles.progressOf(serviceAngle.method())),
                ServiceReport.class);
    }

    ModelReport<?> datasourceReport(ProjectData projectData) {
        DatasourceAngles datasourceAngles = angleService.datasourceAngles(projectData);
        return new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class);
    }

    ModelReport<?> stringComparingReport(ProjectData projectData) {
        StringComparingAngles stringComparingAngles = angleService.stringComparing(projectData);
        return new ModelReport<>(stringComparingAngles.list(), StringComparingReport::new, StringComparingReport.class);
    }

    ModelReport<?> businessRulesReport(ProjectData projectData) {
        BusinessRules businessRules = businessRuleService.businessRules(projectData.types());
        return new ModelReport<>(businessRules.list(), BusinessRuleReport::new, BusinessRuleReport.class);
    }

    ModelReport<?> valuesReport(ValueKind valueKind, ProjectData projectData) {
        ValueAngles valueAngles = businessRuleService.values(valueKind, projectData);
        return new ModelReport<>(valueKind.name(), valueAngles.list(), ValueReport::new, ValueReport.class);
    }

    ModelReport<?> collectionsReport(ProjectData projectData) {
        CollectionAngles collectionAngles = businessRuleService.collections(projectData);
        return new ModelReport<>(collectionAngles.list(), CollectionReport::new, CollectionReport.class);
    }

    ModelReport<?> categoriesReport(ProjectData projectData) {
        CategoryAngles categoryAngles = businessRuleService.categories(projectData);
        return new ModelReport<>(categoryAngles.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport(ProjectData projectData) {
        ValidationAnnotatedMembers validationAnnotatedMembers = new ValidationAnnotatedMembers(projectData.fieldAnnotations(), projectData.methodAnnotations());
        List<ValidationAngle> list = validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList());
        return new ModelReport<>(list, ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> decisionReport(ProjectData projectData, Layer layer) {
        DecisionAngles decisionAngles = angleService.decision(projectData);
        return new ModelReport<>(layer.asText(), decisionAngles.filter(layer), DecisionReport::new, DecisionReport.class);
    }

    ModelReport<?> smellReport(ProjectData projectData) {
        MethodSmellAngles angles = businessRuleService.methodSmells(projectData);
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
