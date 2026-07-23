package fixture.minimal.presentation;

import fixture.minimal.application.MinimalService;
import fixture.minimal.domain.MinimalValue;

/**
 * 入力インタフェース
 */
public class MinimalController {

    private final MinimalService service;

    public MinimalController(MinimalService service) {
        this.service = service;
    }

    public String show(String name) {
        MinimalValue value = service.find(name);
        return value.label();
    }
}
