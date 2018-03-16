package jig.infrastructure;

import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.tag.ThingTag;
import jig.domain.model.thing.Names;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OnMemoryTagRepository implements TagRepository {

    List<ThingTag> list = new ArrayList<>();

    @Override
    public void register(ThingTag thingTag) {
        list.add(thingTag);
    }

    @Override
    public Names find(Tag tag) {
        return list.stream()
                .filter(thingTag -> thingTag.matches(tag))
                .map(ThingTag::name)
                .collect(Names.collector());
    }
}
