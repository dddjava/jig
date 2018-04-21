package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.TypeCharacteristics;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import jig.domain.model.declaration.annotation.ValidationAnnotationDeclaration;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.report.method.MethodDetail;
import jig.domain.model.report.method.MethodPerspective;
import jig.domain.model.report.method.MethodReport;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.Reports;
import jig.domain.model.report.type.TypeDetail;
import jig.domain.model.report.type.TypePerspective;
import jig.domain.model.report.type.TypeReport;
import jig.domain.model.report.validation.AnnotationDetail;
import jig.domain.model.report.validation.ValidationAnnotateReport;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ReportService {

    final CharacteristicRepository characteristicRepository;
    final RelationRepository relationRepository;
    final SqlRepository sqlRepository;
    final JapaneseNameRepository japaneseNameRepository;
    final TypeIdentifierFormatter typeIdentifierFormatter;
    private AnnotationDeclarationRepository annotationDeclarationRepository;
    private GlossaryService glossaryService;

    public ReportService(CharacteristicRepository characteristicRepository,
                         RelationRepository relationRepository,
                         SqlRepository sqlRepository,
                         JapaneseNameRepository japaneseNameRepository,
                         TypeIdentifierFormatter typeIdentifierFormatter, AnnotationDeclarationRepository annotationDeclarationRepository, GlossaryService glossaryService) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
        this.sqlRepository = sqlRepository;
        this.japaneseNameRepository = japaneseNameRepository;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
        this.annotationDeclarationRepository = annotationDeclarationRepository;
        this.glossaryService = glossaryService;
    }

    public Reports reports() {
        return new Reports(Arrays.asList(
                methodReportOn(MethodPerspective.SERVICE),
                methodReportOn(MethodPerspective.REPOSITORY),
                typeReportOn(TypePerspective.IDENTIFIER),
                typeReportOn(TypePerspective.ENUM),
                typeReportOn(TypePerspective.NUMBER),
                typeReportOn(TypePerspective.COLLECTION),
                typeReportOn(TypePerspective.DATE),
                typeReportOn(TypePerspective.TERM),
                validateAnnotationReport()
        ));
    }

    private Report validateAnnotationReport() {
        List<AnnotationDetail> list = new ArrayList<>();
        for (ValidationAnnotationDeclaration annotationDeclaration : annotationDeclarationRepository.findValidationAnnotation()) {
            list.add(new AnnotationDetail(annotationDeclaration, glossaryService, typeIdentifierFormatter));
        }
        return new ValidationAnnotateReport(list);
    }

    Report methodReportOn(MethodPerspective perspective) {
        Characteristic characteristic = perspective.characteristic();
        List<MethodDetail> list = new ArrayList<>();
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(characteristic);
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            MethodDeclarations methods = relationRepository.methodsOf(typeIdentifier);
            for (MethodDeclaration methodDeclaration : methods.list()) {
                MethodDetail detail = new MethodDetail(typeIdentifier, methodDeclaration, relationRepository, characteristicRepository, sqlRepository, japaneseNameRepository, typeIdentifierFormatter);
                list.add(detail);
            }
        }
        return new MethodReport(perspective, list);
    }

    Report typeReportOn(TypePerspective perspective) {
        Characteristic characteristic = perspective.characteristic();
        List<TypeDetail> list = new ArrayList<>();
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(characteristic);
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            TypeCharacteristics typeCharacteristics = characteristicRepository.characteristicsOf(typeIdentifier);
            TypeDetail detail = new TypeDetail(typeIdentifier, typeCharacteristics, relationRepository, japaneseNameRepository, typeIdentifierFormatter);
            list.add(detail);
        }
        return new TypeReport(perspective, list);
    }
}
