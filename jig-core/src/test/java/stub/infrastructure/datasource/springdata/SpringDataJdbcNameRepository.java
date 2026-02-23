package stub.infrastructure.datasource.springdata;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJdbcNameRepository extends CrudRepository<SpringDataJdbcNameEntity, Long> {

    SpringDataJdbcNameEntity findByHoge(Long hoge);

}
