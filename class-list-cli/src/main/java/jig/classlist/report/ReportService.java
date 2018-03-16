package jig.classlist.report;

import jig.classlist.report.method.MethodDetail;
import jig.classlist.report.method.MethodPerspective;
import jig.classlist.report.method.MethodReport;
import jig.classlist.report.type.TypeDetail;
import jig.classlist.report.type.TypePerspective;
import jig.classlist.report.type.TypeReport;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.tag.ThingTag;
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
            MethodDetail condition = new MethodDetail(methodRelation, relationRepository, japaneseNameRepository);
            list.add(condition);
        }
        return new MethodReport(MethodPerspective.from(tag), list);
    }

    private Report getTypeReport(Tag tag) {
        List<TypeDetail> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        for (Name name : names.list()) {
            TypeDetail detail = new TypeDetail(new ThingTag(name, tag), relationRepository, japaneseNameRepository);
            list.add(detail);
        }
        return new TypeReport(TypePerspective.from(tag), list);
    }
}
