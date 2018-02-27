package jig.model.thing;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

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

    public String format(ThingFormatter formatter) {
        return formatter.header() +
                things.stream()
                        .filter(Thing::hasDependency)
                        .map(formatter::format)
                        .collect(Collectors.joining(System.lineSeparator())) +
                formatter.footer();
    }
}
