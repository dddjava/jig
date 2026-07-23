package fixture.compat;

import java.util.ArrayList;
import java.util.List;

/**
 * サービス
 */
public class CompatService implements CompatBehavior {

    private final List<CompatValue> values = new ArrayList<CompatValue>();

    @Override
    public CompatValue apply(CompatValue value) {
        values.add(value);
        return value.add(new CompatValue(value.name(), 1));
    }

    public CompatKind kindOf(CompatValue value) {
        return value.number() > 0 ? CompatKind.FIRST : CompatKind.SECOND;
    }
}
