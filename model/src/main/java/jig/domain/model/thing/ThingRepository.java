package jig.domain.model.thing;

public interface ThingRepository {

    boolean exists(Name name);

    void register(Thing thing);

    Thing get(Name name);
}
