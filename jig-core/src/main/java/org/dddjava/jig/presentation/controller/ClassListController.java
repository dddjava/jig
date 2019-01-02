package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.angle.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.angle.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.angle.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.angle.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.architecture.Layer;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.businessrules.categories.CategoryAngles;
import org.dddjava.jig.domain.model.businessrules.collections.CollectionAngles;
import org.dddjava.jig.domain.model.businessrules.validations.ValidationAngle;
import org.dddjava.jig.domain.model.businessrules.values.ValueAngles;
import org.dddjava.jig.domain.model.businessrules.values.ValueKind;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.Implementations;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.threelayer.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.threelayer.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.threelayer.services.ServiceAngles;
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
    public JigModelAndView<ModelReports> applicationList(Implementations implementations) {
        ModelReports modelReports = new ModelReports(
                controllerReport(implementations),
                serviceReport(implementations),
                datasourceReport(implementations)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.BusinessRuleList)
    public JigModelAndView<ModelReports> domainList(Implementations implementations) {
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

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.BranchList)
    public JigModelAndView<ModelReports> branchList(Implementations implementations) {
        ModelReports modelReports = new ModelReports(
                decisionReport(implementations, Layer.PRESENTATION),
                decisionReport(implementations, Layer.APPLICATION),
                decisionReport(implementations, Layer.DATASOURCE)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    ModelReport<?> controllerReport(Implementations implementations) {
        ControllerAngles controllerAngles = applicationService.controllerAngles(implementations.typeByteCodes());
        ProgressAngles progressAngles = applicationService.progressAngles(implementations.typeByteCodes());

        return new ModelReport<>(controllerAngles.list(),
                controllerAngle -> new ControllerReport(controllerAngle, progressAngles.progressOf(controllerAngle.method().declaration())),
                ControllerReport.class);
    }

    ModelReport<?> serviceReport(Implementations implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations.typeByteCodes());
        ProgressAngles progressAngles = applicationService.progressAngles(implementations.typeByteCodes());

        return new ModelReport<>(serviceAngles.list(),
                serviceAngle -> new ServiceReport(serviceAngle, progressAngles.progressOf(serviceAngle.method())),
                ServiceReport.class);
    }

    ModelReport<?> datasourceReport(Implementations implementations) {
        DatasourceAngles datasourceAngles = applicationService.datasourceAngles(implementations.typeByteCodes(), implementations.sqls());
        return new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class);
    }

    ModelReport<?> stringComparingReport(Implementations implementations) {
        StringComparingAngles stringComparingAngles = applicationService.stringComparing(implementations.typeByteCodes());
        return new ModelReport<>(stringComparingAngles.list(), StringComparingReport::new, StringComparingReport.class);
    }

    ModelReport<?> businessRulesReport(Implementations implementations) {
        BusinessRules businessRules = businessRuleService.businessRules(implementations.typeByteCodes().types());
        return new ModelReport<>(businessRules.list(), BusinessRuleReport::new, BusinessRuleReport.class);
    }

    ModelReport<?> valuesReport(ValueKind valueKind, Implementations implementations) {
        ValueAngles valueAngles = businessRuleService.values(valueKind, implementations.typeByteCodes());
        return new ModelReport<>(valueKind.name(), valueAngles.list(), ValueReport::new, ValueReport.class);
    }

    ModelReport<?> collectionsReport(Implementations implementations) {
        CollectionAngles collectionAngles = businessRuleService.collections(implementations.typeByteCodes());
        return new ModelReport<>(collectionAngles.list(), CollectionReport::new, CollectionReport.class);
    }

    ModelReport<?> categoriesReport(Implementations implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations.typeByteCodes());
        return new ModelReport<>(categoryAngles.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport(Implementations implementations) {
        TypeByteCodes typeByteCodes = implementations.typeByteCodes();
        ValidationAnnotatedMembers validationAnnotatedMembers = new ValidationAnnotatedMembers(typeByteCodes.annotatedFields(), typeByteCodes.annotatedMethods());
        List<ValidationAngle> list = validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList());
        return new ModelReport<>(list, ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> decisionReport(Implementations implementations, Layer layer) {
        DecisionAngles decisionAngles = applicationService.decision(implementations.typeByteCodes());
        return new ModelReport<>(layer.asText(), decisionAngles.filter(layer), DecisionReport::new, DecisionReport.class);
    }

    ModelReport<?> smellReport(Implementations implementations) {
        MethodSmellAngles angles = businessRuleService.methodSmells(implementations.typeByteCodes());
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
