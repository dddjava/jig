package org.dddjava.jig.presentation.controller.classlist;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.basic.report.Report;
import org.dddjava.jig.domain.basic.report.Reports;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.categories.EnumReport;
import org.dddjava.jig.domain.model.characteristic.ValueObjectType;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceReport;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.DecisionReport;
import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.decisions.StringComparingReport;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.services.ServiceReport;
import org.dddjava.jig.domain.model.validations.ValidationReport;
import org.dddjava.jig.domain.model.valueobjects.ValueObjectAngles;
import org.dddjava.jig.domain.model.valueobjects.ValueObjectReport;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClassListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListController.class);

    ViewResolver viewResolver;

    TypeIdentifierFormatter typeIdentifierFormatter;
    AnnotationDeclarationRepository annotationDeclarationRepository;
    GlossaryService glossaryService;
    AngleService angleService;

    public ClassListController(ViewResolver viewResolver,
                               TypeIdentifierFormatter typeIdentifierFormatter,
                               AnnotationDeclarationRepository annotationDeclarationRepository,
                               GlossaryService glossaryService,
                               AngleService angleService) {
        this.viewResolver = viewResolver;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
        this.glossaryService = glossaryService;
        this.angleService = angleService;
    }

    public JigModelAndView<Reports> applicationList() {
        LOGGER.info("入出力リストを出力します");
        Reports reports = new Reports(Arrays.asList(
                serviceReport(),
                datasourceReport()
        ));

        return new JigModelAndView<>(reports, viewResolver.applicationList());
    }

    public JigModelAndView<Reports> domainList() {
        LOGGER.info("ビジネスルールリストを出力します");
        Reports reports = new Reports(Arrays.asList(
                typeReportOn(ValueObjectType.IDENTIFIER),
                enumReport(),
                typeReportOn(ValueObjectType.NUMBER),
                typeReportOn(ValueObjectType.COLLECTION),
                typeReportOn(ValueObjectType.DATE),
                typeReportOn(ValueObjectType.TERM),
                validateAnnotationReport(),
                stringComparingReport(),
                decisionReport()
        ));

        return new JigModelAndView<>(reports, viewResolver.domainList());
    }

    Report<?> serviceReport() {
        ServiceAngles serviceAngles = angleService.serviceAngles();
        List<ServiceReport.Row> list = serviceAngles.list().stream().map(angle -> {
            JapaneseName japaneseName = glossaryService.japaneseNameFrom(angle.method().declaringType());
            return new ServiceReport.Row(angle, japaneseName, typeIdentifierFormatter);
        }).collect(Collectors.toList());
        return new ServiceReport(list).toReport();
    }

    Report<?> datasourceReport() {
        DatasourceAngles datasourceAngles = angleService.datasourceAngles();
        List<DatasourceReport.Row> list = datasourceAngles.list().stream().map(angle -> {
            JapaneseName japaneseName = glossaryService.japaneseNameFrom(angle.method().declaringType());
            return new DatasourceReport.Row(angle, japaneseName, typeIdentifierFormatter);
        }).collect(Collectors.toList());
        return new DatasourceReport(list).toReport();
    }

    Report<?> stringComparingReport() {
        StringComparingAngle stringComparingAngle = angleService.stringComparing();
        return new StringComparingReport(stringComparingAngle).toReport();
    }

    Report<?> typeReportOn(ValueObjectType valueObjectType) {
        ValueObjectAngles valueObjectAngles = angleService.genericModelAngles(valueObjectType);
        List<ValueObjectReport.Row> list = valueObjectAngles.list().stream().map(enumAngle -> {
            JapaneseName japaneseName = glossaryService.japaneseNameFrom(enumAngle.typeIdentifier());
            return new ValueObjectReport.Row(enumAngle, japaneseName, typeIdentifierFormatter);
        }).collect(Collectors.toList());
        return new ValueObjectReport(valueObjectType, list).toReport();
    }

    Report<?> enumReport() {
        EnumAngles enumAngles = angleService.enumAngles();
        List<EnumReport.Row> list = enumAngles.list().stream().map(enumAngle -> {
            JapaneseName japaneseName = glossaryService.japaneseNameFrom(enumAngle.typeIdentifier());
            return new EnumReport.Row(enumAngle, japaneseName, typeIdentifierFormatter);
        }).collect(Collectors.toList());
        return new EnumReport(list).toReport();
    }

    Report<?> validateAnnotationReport() {
        List<ValidationReport.Row> list = new ArrayList<>();
        for (ValidationAnnotationDeclaration annotationDeclaration : annotationDeclarationRepository.findValidationAnnotation()) {
            JapaneseName japaneseName = glossaryService.japaneseNameFrom(annotationDeclaration.declaringType());
            list.add(new ValidationReport.Row(annotationDeclaration, japaneseName, typeIdentifierFormatter));
        }
        return new ValidationReport(list).toReport();
    }

    Report<?> decisionReport() {
        DecisionAngles decisionAngles = angleService.decision();
        return new DecisionReport(decisionAngles).toReport();
    }
}
