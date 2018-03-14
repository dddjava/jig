package jig.domain.model.list.kind;

import jig.domain.model.thing.Names;

public interface TagRepository {

    void register(ThingTag thingTag);

    Names find(Tag anEnum);
}
