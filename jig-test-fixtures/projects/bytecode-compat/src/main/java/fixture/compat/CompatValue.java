package fixture.compat;

/**
 * 値
 */
public class CompatValue {

    private final String name;
    private final int number;

    public CompatValue(String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String name() {
        return name;
    }

    public int number() {
        return number;
    }

    public CompatValue add(CompatValue other) {
        return new CompatValue(name, number + other.number);
    }
}
