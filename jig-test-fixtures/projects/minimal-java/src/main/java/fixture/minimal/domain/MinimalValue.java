package fixture.minimal.domain;

/**
 * ドメインモデル
 */
public class MinimalValue {

    private final String name;

    public MinimalValue(String name) {
        this.name = name;
    }

    public String label() {
        return name;
    }
}
