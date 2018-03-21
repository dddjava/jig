package jig.domain.model.identifier;

public class MethodIdentifier {

    private final Identifier classIdentifier;
    private final String methodName;
    private final String args;

    public MethodIdentifier(Identifier classIdentifier, String methodName, String args) {
        this.classIdentifier = classIdentifier;
        this.methodName = methodName;
        this.args = args;
    }

    // TODO このままIdentifierを残すかは検討の余地あり
    public Identifier toIdentifier() {
        return new Identifier(classIdentifier.value() + "." + methodName + args);
    }
}
