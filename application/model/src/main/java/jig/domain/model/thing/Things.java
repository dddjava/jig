package jig.domain.model.thing;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

public class Things {

    Set<Thing> things = new HashSet<>();

    public void register(Thing thing) {
        things.add(thing);
    }

    public boolean notExists(Name name) {
        return things.stream().noneMatch(model -> model.matches(name));
    }

    public Thing get(Name name) {
        return things.stream()
                .filter(model -> model.matches(name))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
