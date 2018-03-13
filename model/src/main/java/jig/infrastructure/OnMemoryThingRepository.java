package jig.infrastructure;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;
import jig.domain.model.thing.ThingRepository;
import jig.domain.model.thing.ThingType;

import java.util.ArrayList;
import java.util.List;

public class OnMemoryThingRepository implements ThingRepository {

    List<Thing> list = new ArrayList<>();

    @Override
    public void persist(Thing thing) {
        list.add(thing);
    }

    @Override
    public Thing resolve(Name name, ThingType type) {
        return list.stream()
                .filter(thing -> thing.matches(name))
                .findFirst()
                .orElseGet(() -> new Thing(name, type));
    }
}
