package fixture.minimal.application;

import fixture.minimal.domain.MinimalValue;

/**
 * 出力インタフェース
 */
public interface MinimalRepository {

    MinimalValue get(MinimalValue key);
}
