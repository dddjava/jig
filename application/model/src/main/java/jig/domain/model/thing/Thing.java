package jig.domain.model.thing;

public class Thing {

    private final Name name;

    public Thing(Name name) {
        this.name = name;
    }

    public boolean matches(Name name) {
        return this.name.equals(name);
    }

    public Name name() {
        return name;
    }
}
