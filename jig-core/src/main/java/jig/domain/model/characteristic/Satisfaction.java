package jig.domain.model.characteristic;

public enum Satisfaction {
    SATISFY("◯"),
    NOT_SATISFY("");

    final String symbol;

    Satisfaction(String symbol) {
        this.symbol = symbol;
    }

    public static Satisfaction of(boolean satisfied) {
        return satisfied ? SATISFY : NOT_SATISFY;
    }

    public String toSymbolText() {
        return symbol;
    }
}
