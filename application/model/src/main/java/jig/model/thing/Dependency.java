package jig.model.thing;

import java.util.ArrayList;
import java.util.List;

public class Dependency {

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
