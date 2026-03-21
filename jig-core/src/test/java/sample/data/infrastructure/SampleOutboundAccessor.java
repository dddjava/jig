package sample.data.infrastructure;

import org.springframework.data.repository.CrudRepository;

public interface SampleOutboundAccessor extends CrudRepository<SampleEntity, Long> {
}
