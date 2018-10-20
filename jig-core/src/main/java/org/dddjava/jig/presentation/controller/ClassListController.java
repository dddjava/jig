package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.architecture.Layer;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
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
    public JigModelAndView<ModelReports> applicationList(TypeByteCodes typeByteCodes, Sqls sqls) {
        LOGGER.info("入出力リストを出力します");
        ModelReports modelReports = new ModelReports(
                controllerReport(typeByteCodes),
                serviceReport(typeByteCodes),
                datasourceReport(typeByteCodes, sqls)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.DomainList)
    public JigModelAndView<ModelReports> domainList(TypeByteCodes typeByteCodes) {
        LOGGER.info("ビジネスルールリストを出力します");
        ModelReports modelReports = new ModelReports(
                businessRulesReport(typeByteCodes),
                valuesReport(ValueKind.IDENTIFIER, typeByteCodes),
                categoriesReport(typeByteCodes),
                valuesReport(ValueKind.NUMBER, typeByteCodes),
                collectionsReport(typeByteCodes),
                valuesReport(ValueKind.DATE, typeByteCodes),
                valuesReport(ValueKind.TERM, typeByteCodes),
                validateAnnotationReport(typeByteCodes),
                stringComparingReport(typeByteCodes),
                smellReport(typeByteCodes)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    @DocumentMapping(JigDocument.BranchList)
    public JigModelAndView<ModelReports> branchList(TypeByteCodes typeByteCodes) {
        LOGGER.info("条件分岐リストを出力します");
        ModelReports modelReports = new ModelReports(
                decisionReport(typeByteCodes, Layer.PRESENTATION),
                decisionReport(typeByteCodes, Layer.APPLICATION),
                decisionReport(typeByteCodes, Layer.DATASOURCE)
        );

        return new JigModelAndView<>(modelReports, new PoiView(convertContext));
    }

    ModelReport<?> controllerReport(TypeByteCodes typeByteCodes) {
        ControllerAngles controllerAngles = applicationService.controllerAngles(typeByteCodes);
        ProgressAngles progressAngles = applicationService.progressAngles(typeByteCodes);

        return new ModelReport<>(controllerAngles.list(),
                controllerAngle -> new ControllerReport(controllerAngle, progressAngles.progressOf(controllerAngle.method().declaration())),
                ControllerReport.class);
    }

    ModelReport<?> serviceReport(TypeByteCodes typeByteCodes) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(typeByteCodes);
        ProgressAngles progressAngles = applicationService.progressAngles(typeByteCodes);

        return new ModelReport<>(serviceAngles.list(),
                serviceAngle -> new ServiceReport(serviceAngle, progressAngles.progressOf(serviceAngle.method())),
                ServiceReport.class);
    }

    ModelReport<?> datasourceReport(TypeByteCodes typeByteCodes, Sqls sqls) {
        DatasourceAngles datasourceAngles = applicationService.datasourceAngles(typeByteCodes, sqls);
        return new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class);
    }

    ModelReport<?> stringComparingReport(TypeByteCodes typeByteCodes) {
        StringComparingAngles stringComparingAngles = applicationService.stringComparing(typeByteCodes);
        return new ModelReport<>(stringComparingAngles.list(), StringComparingReport::new, StringComparingReport.class);
    }

    ModelReport<?> businessRulesReport(TypeByteCodes typeByteCodes) {
        BusinessRules businessRules = businessRuleService.businessRules(typeByteCodes.types());
        return new ModelReport<>(businessRules.list(), BusinessRuleReport::new, BusinessRuleReport.class);
    }

    ModelReport<?> valuesReport(ValueKind valueKind, TypeByteCodes typeByteCodes) {
        ValueAngles valueAngles = businessRuleService.values(valueKind, typeByteCodes);
        return new ModelReport<>(valueKind.name(), valueAngles.list(), ValueReport::new, ValueReport.class);
    }

    ModelReport<?> collectionsReport(TypeByteCodes typeByteCodes) {
        CollectionAngles collectionAngles = businessRuleService.collections(typeByteCodes);
        return new ModelReport<>(collectionAngles.list(), CollectionReport::new, CollectionReport.class);
    }

    ModelReport<?> categoriesReport(TypeByteCodes typeByteCodes) {
        CategoryAngles categoryAngles = businessRuleService.categories(typeByteCodes);
        return new ModelReport<>(categoryAngles.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport(TypeByteCodes typeByteCodes) {
        ValidationAnnotatedMembers validationAnnotatedMembers = new ValidationAnnotatedMembers(typeByteCodes.annotatedFields(), typeByteCodes.annotatedMethods());
        List<ValidationAngle> list = validationAnnotatedMembers.list().stream()
                .map(ValidationAngle::new)
                .collect(Collectors.toList());
        return new ModelReport<>(list, ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> decisionReport(TypeByteCodes typeByteCodes, Layer layer) {
        DecisionAngles decisionAngles = applicationService.decision(typeByteCodes);
        return new ModelReport<>(layer.asText(), decisionAngles.filter(layer), DecisionReport::new, DecisionReport.class);
    }

    ModelReport<?> smellReport(TypeByteCodes typeByteCodes) {
        MethodSmellAngles angles = businessRuleService.methodSmells(typeByteCodes);
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }
}
