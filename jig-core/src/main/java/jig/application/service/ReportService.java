package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.method.MethodIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifierFormatter;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.report.Perspective;
import jig.domain.model.report.method.MethodDetail;
import jig.domain.model.report.method.MethodPerspective;
import jig.domain.model.report.method.MethodReport;
import jig.domain.model.report.template.Report;
import jig.domain.model.report.template.Reports;
import jig.domain.model.report.type.TypeDetail;
import jig.domain.model.report.type.TypePerspective;
import jig.domain.model.report.type.TypeReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    CharacteristicRepository characteristicRepository;
    @Autowired
    RelationRepository relationRepository;
    @Autowired
    SqlRepository sqlRepository;
    @Autowired
    JapaneseNameRepository japaneseNameRepository;
    @Autowired
    TypeIdentifierFormatter typeIdentifierFormatter;

    public Reports reports() {
        List<Report> list = Arrays.stream(Perspective.values())
                .map(perspective -> {
                    if (perspective.isMethod()) return getMethodReport(perspective.getMethodPerspective());
                    return getTypeReport(perspective.getTypePerspective());
                })
                .collect(Collectors.toList());
        return new Reports(list);
    }

    private Report getMethodReport(MethodPerspective perspective) {
        Characteristic characteristic = perspective.characteristic();
        List<MethodDetail> list = new ArrayList<>();
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(characteristic);
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            MethodIdentifiers methods = relationRepository.methodsOf(typeIdentifier);
            for (MethodIdentifier methodIdentifier : methods.list()) {
                MethodDetail detail = new MethodDetail(typeIdentifier, methodIdentifier, relationRepository, characteristicRepository, sqlRepository, japaneseNameRepository, typeIdentifierFormatter);
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
            Characteristics characteristics = characteristicRepository.characteristicsOf(typeIdentifier);
            TypeDetail detail = new TypeDetail(typeIdentifier, characteristics, relationRepository, japaneseNameRepository, typeIdentifierFormatter);
            list.add(detail);
        }
        return new TypeReport(perspective, list);
    }
}
