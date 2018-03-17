package jig.application.service.report;

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
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    TagRepository tagRepository;
    @Autowired
    RelationRepository relationRepository;
    @Autowired
    SqlRepository sqlRepository;
    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    public Report getReport(Tag tag) {
        if (tag.architecture()) {
            return getMethodReport(tag);
        } else {
            return getTypeReport(tag);
        }
    }

    private Report getMethodReport(Tag tag) {
        List<MethodDetail> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        Relations methods = relationRepository.methodsOf(names);
        for (Relation methodRelation : methods.list()) {
            MethodDetail condition = new MethodDetail(methodRelation, relationRepository, tagRepository, sqlRepository, japaneseNameRepository);
            list.add(condition);
        }
        return new MethodReport(MethodPerspective.from(tag), list);
    }

    private Report getTypeReport(Tag tag) {
        List<TypeDetail> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        for (Name name : names.list()) {
            TypeDetail detail = new TypeDetail(name, tag, relationRepository, japaneseNameRepository);
            list.add(detail);
        }
        return new TypeReport(TypePerspective.from(tag), list);
    }
}
