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

    public MethodIdentifier(Identifier identifier) {
        String identifierText = identifier.value();
        int argIndex = identifierText.lastIndexOf("(");
        int index = identifierText.lastIndexOf(".", argIndex);
        this.classIdentifier = new Identifier(identifierText.substring(0, index));
        this.methodName = identifierText.substring(index + 1, argIndex);
        this.args = identifierText.substring(argIndex);
    }

    // TODO このままIdentifierを残すかは検討の余地あり
    public Identifier toIdentifier() {
        return new Identifier(classIdentifier.value() + "." + methodName + args);
    }

    public String asFullText() {
        return toIdentifier().value();
    }

    public String asSimpleText() {
        return toIdentifier().asSimpleText();
    }

    public Identifier typeIdentifier() {
        return classIdentifier;
    }
}
