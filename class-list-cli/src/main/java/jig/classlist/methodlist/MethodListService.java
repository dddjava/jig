package jig.classlist.methodlist;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.Relations;
import jig.domain.model.tag.JapaneseNameRepository;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.thing.Names;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MethodListService {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    RelationRepository relationRepository;

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    public List<MethodRelationNavigator> list(Tag tag) {

        List<MethodRelationNavigator> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        Relations methods = relationRepository.methodsOf(names);
        for (Relation methodRelation : methods.list()) {
            MethodRelationNavigator condition = new MethodRelationNavigator(methodRelation, relationRepository, japaneseNameRepository);
            list.add(condition);
        }

        return list;
    }
}
