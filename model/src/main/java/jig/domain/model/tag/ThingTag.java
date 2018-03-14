package jig.domain.model.tag;

import jig.domain.model.thing.Name;

public class ThingTag {
    Name name;
    Tag tag;

    public ThingTag(Name name, Tag tag) {
        this.name = name;
        this.tag = tag;
    }

    public Name name() {
        return name;
    }

    @Override
    public String toString() {
        return name.value() + ":" + tag;
    }

    public boolean matches(Tag tag) {
        return this.tag.matches(tag);
    }
}
