package org.dddjava.jig.infrastructure.springdatajdbc;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatementId;
import org.dddjava.jig.domain.model.data.rdbaccess.SqlType;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.junit.jupiter.api.Test;
import stub.infrastructure.datasource.springdata.SpringDataJdbcOrderRepository;
import testing.JigTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@JigTest
class SpringDataJdbcStatementReaderTest {

    @Test
    void SpringDataJdbcのRepositoryメソッドをSQLとして取得できる(JigRepository jigRepository) {
        var statements = jigRepository.jigDataProvider().fetchMybatisStatements();
        var namespace = SpringDataJdbcOrderRepository.class.getCanonicalName();

        var save = statements.findById(MyBatisStatementId.from(namespace + ".save")).orElseThrow();
        assertEquals("[spring_data_jdbc_orders]", save.tables().asText());
        assertEquals(SqlType.INSERT, save.sqlType());

        var findById = statements.findById(MyBatisStatementId.from(namespace + ".findById")).orElseThrow();
        assertEquals("[spring_data_jdbc_orders]", findById.tables().asText());
        assertEquals(SqlType.SELECT, findById.sqlType());

        var deleteById = statements.findById(MyBatisStatementId.from(namespace + ".deleteById")).orElseThrow();
        assertEquals("[spring_data_jdbc_orders]", deleteById.tables().asText());
        assertEquals(SqlType.DELETE, deleteById.sqlType());
    }

    @Test
    void DatasourceAnglesにSpringDataJdbcのCRUDが反映される(JigService jigService, JigRepository jigRepository) {
        var datasourceAngles = jigService.datasourceAngles(jigRepository).list().stream()
                .filter(angle -> angle.declaringType().fqn().equals(SpringDataJdbcOrderRepository.class.getCanonicalName()))
                .toList();

        assertEquals(4, datasourceAngles.size());
        assertEquals("[spring_data_jdbc_orders]", datasourceAngles.stream()
                .filter(angle -> angle.interfaceMethod().name().equals("save"))
                .findFirst()
                .orElseThrow()
                .insertTables());
        assertEquals("[spring_data_jdbc_orders]", datasourceAngles.stream()
                .filter(angle -> angle.interfaceMethod().name().equals("findById"))
                .findFirst()
                .orElseThrow()
                .selectTables());
        assertEquals("[spring_data_jdbc_orders]", datasourceAngles.stream()
                .filter(angle -> angle.interfaceMethod().name().equals("updateById"))
                .findFirst()
                .orElseThrow()
                .updateTables());
        assertEquals("[spring_data_jdbc_orders]", datasourceAngles.stream()
                .filter(angle -> angle.interfaceMethod().name().equals("deleteById"))
                .findFirst()
                .orElseThrow()
                .deleteTables());
    }
}
