package showcase.domain.order;

/**
 * 注文の状態
 */
public enum OrderStatus {
    /** 受付済 */
    ACCEPTED,
    /** 出荷済 */
    SHIPPED;

    public boolean shippable() {
        return this == ACCEPTED;
    }
}
