package jig.model.thing;

public class Thing {

    private final Name name;

    public Thing(Name name) {
        this.name = name;
    }

    public boolean matches(Name name) {
        return this.name.equals(name);
    }

    public String format(NameFormatter formatter) {
        return formatter.format(name);
    }

    public Name name() {
        return name;
    }
}
