package showcase.domain.order;

/**
 * 注文番号
 */
public record OrderId(int value) {

    public String label() {
        return "No." + value;
    }
}
