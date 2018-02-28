package jig.model.thing;

public class Thing {

    private final Name name;
    private final Dependency dependency;

    public Thing(Name name) {
        this.name = name;
        this.dependency = new Dependency();
    }

    boolean matches(Name name) {
        return this.name.equals(name);
    }

    public void dependsOn(Thing thing) {
        dependency.add(thing);
    }

    public boolean hasDependency() {
        return !dependency.empty();
    }

    public Dependency dependency() {
        return dependency;
    }

    public String format(NameFormatter formatter) {
        return formatter.format(name);
    }
}
