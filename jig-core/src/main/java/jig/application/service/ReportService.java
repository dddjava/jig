package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.TypeCharacteristics;
import jig.domain.model.datasource.SqlRepository;
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

    public ReportService(CharacteristicRepository characteristicRepository,
                         RelationRepository relationRepository,
                         SqlRepository sqlRepository,
                         JapaneseNameRepository japaneseNameRepository,
                         TypeIdentifierFormatter typeIdentifierFormatter) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
        this.sqlRepository = sqlRepository;
        this.japaneseNameRepository = japaneseNameRepository;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }

    public Reports reports() {
        return new Reports(Arrays.asList(
                getMethodReport(MethodPerspective.SERVICE),
                getMethodReport(MethodPerspective.REPOSITORY),
                getTypeReport(TypePerspective.IDENTIFIER),
                getTypeReport(TypePerspective.ENUM),
                getTypeReport(TypePerspective.NUMBER),
                getTypeReport(TypePerspective.COLLECTION),
                getTypeReport(TypePerspective.DATE),
                getTypeReport(TypePerspective.TERM)
        ));
    }

    private Report getMethodReport(MethodPerspective perspective) {
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

    private Report getTypeReport(TypePerspective perspective) {
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
