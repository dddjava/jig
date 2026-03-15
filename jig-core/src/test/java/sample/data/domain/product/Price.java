package sample.data.domain.product;

/**
 * 価格
 */
public record Price(long value) {
    public Price {
        if (value < 0) throw new IllegalArgumentException("価格は0以上である必要があります。");
    }

    public Price multiply(int multiplier) {
        return new Price(this.value * multiplier);
    }
}
