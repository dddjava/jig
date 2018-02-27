package jig.domain.model.thing;

import java.util.ArrayList;
import java.util.List;

public class Relationship {

    final List<Thing> things = new ArrayList<>();

    void add(Thing thing) {
        things.add(thing);
    }

    public boolean empty() {
        return things.isEmpty();
    }

    public List<Thing> list() {
        return things;
    }
}
