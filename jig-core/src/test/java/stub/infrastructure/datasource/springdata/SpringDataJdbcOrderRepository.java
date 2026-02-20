package stub.infrastructure.datasource.springdata;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJdbcOrderRepository extends CrudRepository<SpringDataJdbcOrder, Long> {

    @Override
    SpringDataJdbcOrder save(SpringDataJdbcOrder entity);

    @Override
    SpringDataJdbcOrder findById(Long id);

    @Override
    void deleteById(Long id);

    @Query("update spring_data_jdbc_orders set id = :id where id = :id")
    void updateById(Long id);
}
