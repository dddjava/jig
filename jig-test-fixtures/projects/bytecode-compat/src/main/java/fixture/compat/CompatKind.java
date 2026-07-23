package fixture.compat;

/**
 * 種別
 */
public enum CompatKind {
    FIRST,
    SECOND;

    public boolean isFirst() {
        return this == FIRST;
    }
}
