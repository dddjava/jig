package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.GenericRelation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.report.Perspective;
import jig.domain.model.report.Report;
import jig.domain.model.report.Reports;
import jig.domain.model.report.method.MethodDetail;
import jig.domain.model.report.method.MethodPerspective;
import jig.domain.model.report.method.MethodReport;
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
        Identifiers identifiers = characteristicRepository.find(characteristic);
        Relations methods = relationRepository.methodsOf(identifiers);
        for (GenericRelation<Identifier, MethodIdentifier> methodRelation : methods.list2()) {
            MethodDetail condition = new MethodDetail(methodRelation, relationRepository, characteristicRepository, sqlRepository, japaneseNameRepository);
            list.add(condition);
        }
        return new MethodReport(perspective, list);
    }

    private Report getTypeReport(TypePerspective perspective) {
        Characteristic characteristic = perspective.characteristic();
        List<TypeDetail> list = new ArrayList<>();
        Identifiers identifiers = characteristicRepository.find(characteristic);
        for (Identifier identifier : identifiers.list()) {
            Characteristics characteristics = characteristicRepository.characteristicsOf(identifier);
            TypeDetail detail = new TypeDetail(identifier, characteristics, relationRepository, japaneseNameRepository);
            list.add(detail);
        }
        return new TypeReport(perspective, list);
    }
}
