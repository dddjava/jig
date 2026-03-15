package sample.data.domain.product;

/**
 * 価格
 */
public class Price {
    long value;

    public Price(long value) {
        if (value < 0) throw new IllegalArgumentException("価格は0以上である必要があります。");
        this.value = value;
    }

    public long value() {
        return value;
    }

    public Price multiply(int multiplier) {
        return new Price(this.value * multiplier);
    }
}
