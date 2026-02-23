package stub.infrastructure.datasource.springdata;

import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("spring_data_jdbc_orders_with_items")
public class SpringDataJdbcOrderWithItems {

    Long id;

    @MappedCollection
    List<SpringDataJdbcOrderItem> items;
}
