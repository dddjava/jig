package jig.infrastructure.mybatis;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.SqlType;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import jig.infrastructure.JigPaths;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverParameterizedTest {

    @ParameterizedTest
    @MethodSource
    void test(String methodName, String tableName, SqlType sqlType) throws Exception {
        SqlRepository repository = new OnMemorySqlRepository();
        CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
        MyBatisSqlResolver sut = new MyBatisSqlResolver(repository, characteristicRepository, new JigPaths());

        ArrayList<URL> list = Collections.list(this.getClass().getClassLoader().getResources(""));
        URL[] urls = list.toArray(new URL[list.size()]);
        sut.resolve(urls);

        Name sqlName = new Name("jig.infrastructure.mybatis.CanonicalMapper." + methodName);
        Sql sql = repository.get(sqlName);
        assertThat(sql.tableName()).isEqualTo(tableName);
        assertThat(sql.sqlType()).isEqualTo(sqlType);


        Names names = characteristicRepository.find(Characteristic.MAPPER_METHOD);
        assertThat(names.contains(sqlName)).isTrue();
    }

    static Stream<Arguments> test() {
        return Stream.of(
                Arguments.of("insert", "crud_test", SqlType.INSERT),
                Arguments.of("select", "crud_test", SqlType.SELECT),
                Arguments.of("update", "crud_test", SqlType.UPDATE),
                Arguments.of("delete", "crud_test", SqlType.DELETE),
                Arguments.of("annotationInsert", "crud_test", SqlType.INSERT),
                Arguments.of("annotationSelect", "crud_test", SqlType.SELECT),
                Arguments.of("annotationUpdate", "crud_test", SqlType.UPDATE),
                Arguments.of("annotationDelete", "crud_test", SqlType.DELETE),
                Arguments.of("japanese", "あのスキーマ.このテーブル", SqlType.SELECT),
                Arguments.of("illegal", "（解析失敗）", SqlType.SELECT)
        );
    }
}