package sample.data.infrastructure;

import org.springframework.data.repository.CrudRepository;

public interface SampleOutputAccessor extends CrudRepository<SampleEntity, Long> {
}
