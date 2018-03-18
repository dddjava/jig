package jig.application.service;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.report.Report;
import jig.domain.model.report.method.MethodDetail;
import jig.domain.model.report.method.MethodPerspective;
import jig.domain.model.report.method.MethodReport;
import jig.domain.model.report.type.TypeDetail;
import jig.domain.model.report.type.TypePerspective;
import jig.domain.model.report.type.TypeReport;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public Report getReport(Characteristic characteristic) {
        if (characteristic.architecture()) {
            return getMethodReport(characteristic);
        } else {
            return getTypeReport(characteristic);
        }
    }

    private Report getMethodReport(Characteristic characteristic) {
        List<MethodDetail> list = new ArrayList<>();
        Names names = characteristicRepository.find(characteristic);
        Relations methods = relationRepository.methodsOf(names);
        for (Relation methodRelation : methods.list()) {
            MethodDetail condition = new MethodDetail(methodRelation, relationRepository, characteristicRepository, sqlRepository, japaneseNameRepository);
            list.add(condition);
        }
        return new MethodReport(MethodPerspective.from(characteristic), list);
    }

    private Report getTypeReport(Characteristic characteristic) {
        List<TypeDetail> list = new ArrayList<>();
        Names names = characteristicRepository.find(characteristic);
        for (Name name : names.list()) {
            TypeDetail detail = new TypeDetail(name, characteristic, relationRepository, japaneseNameRepository);
            list.add(detail);
        }
        return new TypeReport(TypePerspective.from(characteristic), list);
    }
}
