package org.dddjava.jig.domain.model.information.outbound.springdata.ut;

import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Table("orders_with_items")
public class OrderWithItems {
    Long id;

    @MappedCollection
    List<OrderItem> items;
}
