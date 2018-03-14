package jig.classlist.classlist;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.tag.JapaneseNameRepository;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.tag.ThingTag;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TypeListService {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    RelationRepository relationRepository;

    @Autowired
    JapaneseNameRepository japaneseNameRepository;

    public List<TypeListNavigator> list(Tag tag) {

        List<TypeListNavigator> list = new ArrayList<>();
        Names names = tagRepository.find(tag);
        for (Name name : names.list()) {
            list.add(new TypeListNavigator(new ThingTag(name, tag), relationRepository, japaneseNameRepository));
        }

        return list;
    }
}
