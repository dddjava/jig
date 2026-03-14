package stub.infrastructure.datasource.springdata;

public interface SpringDataJdbcOrderPort {
    void save();

    void findById();

    void deleteById();

    void updateById();

    void updateByIdWithComment();
}
