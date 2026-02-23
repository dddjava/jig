package stub.infrastructure.datasource.springdata;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJdbcOrderWithItemsRepository extends CrudRepository<SpringDataJdbcOrderWithItems, Long> {

    @Override
    SpringDataJdbcOrderWithItems save(SpringDataJdbcOrderWithItems entity);

    @Override
    SpringDataJdbcOrderWithItems findById(Long id);

    @Override
    void deleteById(Long id);
}
