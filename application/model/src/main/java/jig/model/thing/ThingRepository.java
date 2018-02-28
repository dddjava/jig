package jig.model.thing;

public interface ThingRepository {

    void persist(Thing thing);

    Thing resolve(Name name);
}
