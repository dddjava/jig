package jig.infrastructure;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;
import jig.domain.model.thing.ThingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class OnMemoryThingRepository implements ThingRepository {

    List<Thing> list = new ArrayList<>();

    @Override
    public boolean exists(Name name) {
        return list.stream().anyMatch(thing -> thing.matches(name));
    }

    @Override
    public void register(Thing thing) {
        if (exists(thing.name())) return;
        list.add(thing);
    }

    @Override
    public Thing get(Name name) {
        return list.stream()
                .filter(thing -> thing.matches(name))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
