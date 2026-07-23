package fixture.minimal.application;

import fixture.minimal.domain.MinimalValue;

/**
 * 業務機能
 */
public class MinimalService {

    private final MinimalRepository repository;

    public MinimalService(MinimalRepository repository) {
        this.repository = repository;
    }

    public MinimalValue find(String name) {
        return repository.get(new MinimalValue(name));
    }
}
