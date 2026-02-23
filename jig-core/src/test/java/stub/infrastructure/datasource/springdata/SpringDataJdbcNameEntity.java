package stub.infrastructure.datasource.springdata;

import org.springframework.data.relational.core.mapping.Table;

@Table(name = "spring_data_table_name")
public record SpringDataJdbcNameEntity(long id, String hoge) {
}
