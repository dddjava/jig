package stub.infrastructure.datasource.springdata;

import org.springframework.data.relational.core.mapping.Table;

@Table("spring_data_jdbc_orders")
public class SpringDataJdbcOrder {

    Long id;
}
