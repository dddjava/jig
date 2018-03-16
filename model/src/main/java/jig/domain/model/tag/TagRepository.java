package jig.domain.model.tag;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

public interface TagRepository {

    Names find(Tag tag);

    void register(Name name, Tag tag);
}
