package jig.classlist.report;

import jig.classlist.report.method.MethodDetail;
import jig.classlist.report.type.TypeDetail;
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

    public List<MethodDetail> methodDetails(Tag tag) {
        List<MethodDetail> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        Relations methods = relationRepository.methodsOf(names);
        for (Relation methodRelation : methods.list()) {
            MethodDetail condition = new MethodDetail(methodRelation, relationRepository, japaneseNameRepository);
            list.add(condition);
        }
        return list;
    }

    public List<TypeDetail> typeDetails(Tag tag) {
        List<TypeDetail> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        for (Name name : names.list()) {
            list.add(new TypeDetail(new ThingTag(name, tag), relationRepository, japaneseNameRepository));
        }
        return list;
    }
}
