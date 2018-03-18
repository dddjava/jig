package jig.infrastructure;

import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Repository
public class OnMemoryTagRepository implements TagRepository {

    EnumMap<Tag, List<Name>> map = new EnumMap<>(Tag.class);

    @Override
    public void register(Name name, Tag tag) {
        map.computeIfAbsent(tag, t -> new ArrayList<>());
        map.get(tag).add(name);
    }

    @Override
    public Names find(Tag tag) {
        return map.entrySet().stream()
                .filter(e -> e.getKey().matches(tag))
                .flatMap(e -> e.getValue().stream())
                .collect(Names.collector());
    }
}
