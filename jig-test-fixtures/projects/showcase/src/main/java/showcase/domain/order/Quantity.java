package showcase.domain.order;

/**
 * 数量 &lt;span&gt;単位は"個"&lt;/span&gt;
 *
 * データJSのエスケープ規則を確認するため、引用符と山括弧を含む。
 */
public record Quantity(int value) {

    public Quantity {
        if (value < 0) throw new IllegalArgumentException("数量は0以上である必要があります");
    }

    /**
     * 数量を加算する
     */
    public Quantity add(Quantity other) {
        return new Quantity(value + other.value);
    }

    public boolean isEmpty() {
        return value == 0;
    }
}
