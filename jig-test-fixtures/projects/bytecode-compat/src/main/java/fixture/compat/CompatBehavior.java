package fixture.compat;

/**
 * ふるまい
 */
public interface CompatBehavior {

    CompatValue apply(CompatValue value);

    default CompatKind kind() {
        return CompatKind.FIRST;
    }
}
