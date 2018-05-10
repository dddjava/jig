package org.dddjava.jig.presentation.controller.classlist;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.angle.*;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.japanese.JapaneseName;
import org.dddjava.jig.domain.model.report.*;
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
                typeReportOn(Characteristic.IDENTIFIER),
                enumReport(),
                typeReportOn(Characteristic.NUMBER),
                typeReportOn(Characteristic.COLLECTION),
                typeReportOn(Characteristic.DATE),
                typeReportOn(Characteristic.TERM),
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
        DesignSmellAngle designSmellAngle = angleService.stringComparing();
        return new StringComparingReport(designSmellAngle).toReport();
    }

    Report<?> typeReportOn(Characteristic characteristic) {
        GenericModelAngles genericModelAngles = angleService.genericModelAngles(characteristic);
        List<GenericModelReport.Row> list = genericModelAngles.list().stream().map(enumAngle -> {
            JapaneseName japaneseName = glossaryService.japaneseNameFrom(enumAngle.typeIdentifier());
            return new GenericModelReport.Row(enumAngle, japaneseName, typeIdentifierFormatter);
        }).collect(Collectors.toList());
        return new GenericModelReport(characteristic, list).toReport();
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
