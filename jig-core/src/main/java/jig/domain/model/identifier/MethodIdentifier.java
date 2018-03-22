package jig.domain.model.identifier;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodIdentifier that = (MethodIdentifier) o;
        return Objects.equals(classIdentifier, that.classIdentifier) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(args, that.args);
    }

    @Override
    public int hashCode() {

        return Objects.hash(classIdentifier, methodName, args);
    }
}
