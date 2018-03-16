package jig.domain.model.tag;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

public interface TagRepository {

    void register(ThingTag thingTag);

    Names find(Tag tag);

    default void register(Tag tag, Name name) {
        register(new ThingTag(name, tag));
    }
}
