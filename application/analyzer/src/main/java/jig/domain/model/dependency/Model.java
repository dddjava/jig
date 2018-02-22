package jig.domain.model.dependency;

public class Model {

    private final FullQualifiedName name;
    private final Dependency dependency;

    public Model(FullQualifiedName name) {
        this.name = name;
        this.dependency = new Dependency();
    }

    boolean matches(FullQualifiedName name) {
        return this.name.equals(name);
    }

    public void dependsOn(Model model) {
        dependency.add(model);
    }

    public boolean hasDependency() {
        return !dependency.empty();
    }

    public Dependency dependency() {
        return dependency;
    }

    public String format(ModelNameFormatter formatter) {
        return formatter.format(name);
    }
}
