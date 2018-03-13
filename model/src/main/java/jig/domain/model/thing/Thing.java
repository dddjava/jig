package jig.domain.model.thing;

public class Thing {

    private final Name name;
    private final ThingType type;

    public Thing(Name name, ThingType type) {
        this.name = name;
        this.type = type;
    }

    public boolean matches(Name name) {
        return this.name.equals(name);
    }

    public Name name() {
        return name;
    }
}
