package stub.infrastructure.datasource.springdata;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.RawSpringDataRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJdbcMixedOrderRepository extends RawSpringDataRepository, CrudRepository<SpringDataJdbcOrder, Long> {

    @Override
    SpringDataJdbcOrder save(SpringDataJdbcOrder entity);

    @Override
    SpringDataJdbcOrder findById(Long id);

    @Override
    void deleteById(Long id);
}
