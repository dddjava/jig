package stub.infrastructure.datasource.springdata;

import org.springframework.stereotype.Repository;

@Repository
public class SpringDataJdbcNameOutputAdapter implements SpringDataJdbcNameOutputPort {
    private final SpringDataJdbcNameRepository repository;

    public SpringDataJdbcNameOutputAdapter(SpringDataJdbcNameRepository repository) {
        this.repository = repository;
    }

    @Override
    public SpringDataJdbcNameEntity save(SpringDataJdbcNameEntity entity) {
        return repository.save(entity);
    }
}
