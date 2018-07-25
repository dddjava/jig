package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.booleans.model.BoolQueryAngles;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.Layer;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.presentation.view.report.JigDocument;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.smells.MethodSmellAngle;
import org.dddjava.jig.domain.model.validations.ValidationAngle;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.PoiView;
import org.dddjava.jig.presentation.view.poi.report.AngleReporter;
import org.dddjava.jig.presentation.view.poi.report.AngleReporters;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.*;
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

    public ClassListController(TypeIdentifierFormatter typeIdentifierFormatter,
                               GlossaryService glossaryService,
                               AngleService angleService) {
        this.convertContext = new ConvertContext(glossaryService, typeIdentifierFormatter);
        this.angleService = angleService;
    }

    @DocumentMapping(JigDocument.ApplicationList)
    public JigModelAndView<AngleReporters> applicationList(ProjectData projectData) {
        LOGGER.info("入出力リストを出力します");
        AngleReporters angleReporters = new AngleReporters(
                controllerReport(projectData),
                serviceReport(projectData),
                datasourceReport(projectData)
        );

        return new JigModelAndView<>(angleReporters, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.DomainList)
    public JigModelAndView<AngleReporters> domainList(ProjectData projectData) {
        LOGGER.info("ビジネスルールリストを出力します");
        AngleReporters angleReporters = new AngleReporters(
                valueObjectReport(ValueKind.IDENTIFIER, projectData),
                categoryReport(projectData),
                valueObjectReport(ValueKind.NUMBER, projectData),
                collectionReport(projectData),
                valueObjectReport(ValueKind.DATE, projectData),
                valueObjectReport(ValueKind.TERM, projectData),
                validateAnnotationReport(projectData),
                stringComparingReport(projectData),
                booleanReport(projectData),
                smellReport(projectData)
        );

        return new JigModelAndView<>(angleReporters, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.BranchList)
    public JigModelAndView<AngleReporters> branchList(ProjectData projectData) {
        LOGGER.info("条件分岐リストを出力します");
        AngleReporters angleReporters = new AngleReporters(
                decisionReport(projectData, Layer.PRESENTATION),
                decisionReport(projectData, Layer.APPLICATION),
                decisionReport(projectData, Layer.DATASOURCE)
        );

        return new JigModelAndView<>(angleReporters, new PoiView(convertContext));
    }

    AngleReporter controllerReport(ProjectData projectData) {
        ControllerAngles controllerAngles = angleService.controllerAngles(projectData);
        return new AngleReporter(ControllerReportAdapter.class, controllerAngles.list());
    }

    AngleReporter serviceReport(ProjectData projectData) {
        ServiceAngles serviceAngles = angleService.serviceAngles(projectData);
        return new AngleReporter(ServiceReportAdapter.class, serviceAngles.list());
    }

    AngleReporter datasourceReport(ProjectData projectData) {
        DatasourceAngles datasourceAngles = angleService.datasourceAngles(projectData);
        return new AngleReporter(RepositoryReportAdapter.class, datasourceAngles.list());
    }

    AngleReporter stringComparingReport(ProjectData projectData) {
        StringComparingAngles stringComparingAngles = angleService.stringComparing(projectData);
        return new AngleReporter(StringComparingAngleAdapter.class, stringComparingAngles.list());
    }

    AngleReporter valueObjectReport(ValueKind valueKind, ProjectData projectData) {
        ValueAngles valueAngles = angleService.valueAngles(valueKind, projectData);
        return new AngleReporter(valueKind.name(), ValueReportAdapter.class, valueAngles.list());
    }

    AngleReporter collectionReport(ProjectData projectData) {
        CollectionAngles collectionAngles = angleService.collectionAngles(projectData);
        return new AngleReporter(CollectionReportAdapter.class, collectionAngles.list());
    }

    AngleReporter categoryReport(ProjectData projectData) {
        CategoryAngles categoryAngles = angleService.enumAngles(projectData);
        return new AngleReporter(CategoryReportAdapter.class, categoryAngles.list());
    }

    AngleReporter validateAnnotationReport(ProjectData projectData) {
        ValidationAnnotatedMembers validationAnnotatedMembers = new ValidationAnnotatedMembers(projectData.annotatedFields(), projectData.annotatedMethods());
        List<ValidationAngle> list = validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList());
        return new AngleReporter(ValidationReportAdapter.class, list);
    }

    AngleReporter decisionReport(ProjectData projectData, Layer layer) {
        DecisionAngles decisionAngles = angleService.decision(projectData);
        return new AngleReporter(layer.asText(), DecisionReportAdapter.class, decisionAngles.filter(layer));
    }

    AngleReporter booleanReport(ProjectData projectData) {
        BoolQueryAngles angles = angleService.boolQueryModelMethodAngle(projectData);
        return new AngleReporter(BoolQueryReportAdapter.class, angles.list());
    }

    AngleReporter smellReport(ProjectData projectData) {
        List<MethodSmellAngle> list = projectData.methods().list().stream()
                .filter(method -> projectData.characterizedTypes().stream().pickup(method.declaration().declaringType()).has(Characteristic.MODEL))
                .map(method -> new MethodSmellAngle(method, projectData.methodUsingFields()))
                .filter(MethodSmellAngle::hasSmell)
                .collect(Collectors.toList());
        return new AngleReporter(MethodSmellReportAdapter.class, list);
    }
}
