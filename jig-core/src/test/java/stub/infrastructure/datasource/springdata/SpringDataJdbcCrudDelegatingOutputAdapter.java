package stub.infrastructure.datasource.springdata;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SpringDataJdbcCrudDelegatingOutputAdapter implements SpringDataJdbcCrudDelegatingOutputPort {
    private final SpringDataJdbcNameRepository repository;

    public SpringDataJdbcCrudDelegatingOutputAdapter(SpringDataJdbcNameRepository repository) {
        this.repository = repository;
    }

    @Override
    public SpringDataJdbcNameEntity save(SpringDataJdbcNameEntity entity) {
        CrudRepository<SpringDataJdbcNameEntity, Long> crudRepository = repository;
        return crudRepository.save(entity);
    }
}
