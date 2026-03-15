package sample.data.domain.order;

/**
 * 数量
 */
public class Quantity {
    int value;

    public Quantity(int value) {
        if (value < 0) throw new IllegalArgumentException("数量は0以上である必要があります。");
        this.value = value;
    }

    public int value() {
        return value;
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }
}
