package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.booleans.model.BoolQueryAngle;
import org.dddjava.jig.domain.model.booleans.model.BoolQueryAngles;
import org.dddjava.jig.domain.model.categories.CategoryAngle;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.collections.CollectionAngle;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngle;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngle;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.Layer;
import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.validations.ValidationAngle;
import org.dddjava.jig.domain.model.values.ValueAngle;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.poi.StringComparingReport;
import org.dddjava.jig.presentation.view.poi.report.Report;
import org.dddjava.jig.presentation.view.poi.report.Reports;
import org.dddjava.jig.presentation.view.poi.reporter.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClassListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListController.class);

    ViewResolver viewResolver;

    TypeIdentifierFormatter typeIdentifierFormatter;
    GlossaryService glossaryService;
    AngleService angleService;

    public ClassListController(ViewResolver viewResolver,
                               TypeIdentifierFormatter typeIdentifierFormatter,
                               GlossaryService glossaryService,
                               AngleService angleService) {
        this.viewResolver = viewResolver;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
        this.glossaryService = glossaryService;
        this.angleService = angleService;
    }

    public JigModelAndView<Reports> applicationList(ProjectData projectData) {
        LOGGER.info("入出力リストを出力します");
        Reports reports = new Reports(Arrays.asList(
                serviceReport(projectData),
                datasourceReport(projectData)
        ));

        return new JigModelAndView<>(reports, viewResolver.applicationList());
    }

    public JigModelAndView<Reports> domainList(ProjectData projectData) {
        LOGGER.info("ビジネスルールリストを出力します");
        Reports reports = new Reports(Arrays.asList(
                valueObjectReport(ValueKind.IDENTIFIER, projectData),
                categoryReport(projectData),
                valueObjectReport(ValueKind.NUMBER, projectData),
                collectionReport(projectData),
                valueObjectReport(ValueKind.DATE, projectData),
                valueObjectReport(ValueKind.TERM, projectData),
                validateAnnotationReport(projectData),
                stringComparingReport(projectData),
                booleanReport(projectData)
        ));

        return new JigModelAndView<>(reports, viewResolver.domainList());
    }

    public JigModelAndView<Reports> branchList(ProjectData projectData) {
        LOGGER.info("条件分岐リストを出力します");
        Reports reports = new Reports(Arrays.asList(
                decisionReport(projectData, Layer.PRESENTATION),
                decisionReport(projectData, Layer.APPLICATION),
                decisionReport(projectData, Layer.DATASOURCE)
        ));

        return new JigModelAndView<>(reports, viewResolver.branchList());
    }

    Report<?> serviceReport(ProjectData projectData) {
        ServiceAngles serviceAngles = angleService.serviceAngles(projectData);
        return new Reporter<>("SERVICE", ServiceAngle.class, serviceAngles.list()).toReport(glossaryService, typeIdentifierFormatter);
    }

    Report<?> datasourceReport(ProjectData projectData) {
        DatasourceAngles datasourceAngles = angleService.datasourceAngles(projectData);
        return new Reporter<>("REPOSITORY", DatasourceAngle.class, datasourceAngles.list()).toReport(glossaryService, typeIdentifierFormatter);
    }

    Report<?> stringComparingReport(ProjectData projectData) {
        StringComparingAngle stringComparingAngle = angleService.stringComparing(projectData);
        return new StringComparingReport(stringComparingAngle).toReport();
    }

    Report<?> valueObjectReport(ValueKind valueKind, ProjectData projectData) {
        ValueAngles valueAngles = angleService.valueAngles(valueKind, projectData);
        return new Reporter<>(valueKind.name(), ValueAngle.class, valueAngles.list()).toReport(glossaryService, typeIdentifierFormatter);
    }

    Report<?> collectionReport(ProjectData projectData) {
        CollectionAngles collectionAngles = angleService.collectionAngles(projectData);
        return new Reporter<>("COLLECTION", CollectionAngle.class, collectionAngles.list()).toReport(glossaryService, typeIdentifierFormatter);
    }

    Report<?> categoryReport(ProjectData projectData) {
        CategoryAngles categoryAngles = angleService.enumAngles(projectData);
        return new Reporter<>("ENUM", CategoryAngle.class, categoryAngles.list()).toReport(glossaryService, typeIdentifierFormatter);
    }

    Report<?> validateAnnotationReport(ProjectData projectData) {
        ValidationAnnotatedMembers validationAnnotatedMembers = new ValidationAnnotatedMembers(projectData.annotatedFields(), projectData.annotatedMethods());
        List<ValidationAngle> list = validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList());
        return new Reporter<>("VALIDATION", ValidationAngle.class, list).toReport(glossaryService, typeIdentifierFormatter);
    }

    Report<?> decisionReport(ProjectData projectData, Layer layer) {
        DecisionAngles decisionAngles = angleService.decision(projectData);
        return new Reporter<>(layer.asText(), DecisionAngle.class, decisionAngles.filter(layer)).toReport(glossaryService, typeIdentifierFormatter);
    }

    Report<?> booleanReport(ProjectData projectData) {
        BoolQueryAngles angles = angleService.boolQueryModelMethodAngle(projectData);
        return new Reporter<>("真偽値を返すメソッド", BoolQueryAngle.class, angles.list()).toReport(glossaryService, typeIdentifierFormatter);
    }
}
