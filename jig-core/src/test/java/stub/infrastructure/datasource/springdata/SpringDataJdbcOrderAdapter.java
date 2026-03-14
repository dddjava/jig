package stub.infrastructure.datasource.springdata;

import org.springframework.stereotype.Repository;

@Repository
public class SpringDataJdbcOrderAdapter implements SpringDataJdbcOrderPort {

    private final SpringDataJdbcOrderAccessor accessor;

    public SpringDataJdbcOrderAdapter(SpringDataJdbcOrderAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public void save() {
        accessor.save(new SpringDataJdbcOrder());
    }

    @Override
    public void findById() {
        accessor.findById(1L);
    }

    @Override
    public void deleteById() {
        accessor.deleteById(1L);
    }

    @Override
    public void updateById() {
        accessor.updateById(1L);
    }

    @Override
    public void updateByIdWithComment() {
        accessor.updateById(1L);
    }
}
