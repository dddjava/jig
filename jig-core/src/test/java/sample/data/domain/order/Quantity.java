package sample.data.domain.order;

/**
 * 数量
 */
public record Quantity(int value) {
    public Quantity {
        if (value < 0) throw new IllegalArgumentException("数量は0以上である必要があります。");
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }
}
