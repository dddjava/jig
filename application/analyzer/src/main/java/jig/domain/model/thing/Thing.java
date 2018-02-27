package jig.domain.model.thing;

public class Thing {

    private final Name name;
    private final Relationship relationship;

    public Thing(Name name) {
        this.name = name;
        this.relationship = new Relationship();
    }

    boolean matches(Name name) {
        return this.name.equals(name);
    }

    public void dependsOn(Thing thing) {
        relationship.add(thing);
    }

    public boolean hasDependency() {
        return !relationship.empty();
    }

    public Relationship dependency() {
        return relationship;
    }

    public String format(NameFormatter formatter) {
        return formatter.format(name);
    }
}
