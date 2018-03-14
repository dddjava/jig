package jig.infrastructure;

import jig.domain.model.list.kind.Tag;
import jig.domain.model.list.kind.TagRepository;
import jig.domain.model.list.kind.ThingTag;
import jig.domain.model.thing.Names;

import java.util.ArrayList;
import java.util.List;

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
