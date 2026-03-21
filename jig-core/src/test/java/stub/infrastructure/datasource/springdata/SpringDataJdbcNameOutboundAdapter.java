package stub.infrastructure.datasource.springdata;

import org.springframework.stereotype.Repository;

@Repository
public class SpringDataJdbcNameOutboundAdapter implements SpringDataJdbcNameOutboundPort {
    private final SpringDataJdbcNameRepository repository;

    public SpringDataJdbcNameOutboundAdapter(SpringDataJdbcNameRepository repository) {
        this.repository = repository;
    }

    @Override
    public SpringDataJdbcNameEntity save(SpringDataJdbcNameEntity entity) {
        return repository.save(entity);
    }
}
