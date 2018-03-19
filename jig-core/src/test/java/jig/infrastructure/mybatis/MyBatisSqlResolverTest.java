package jig.infrastructure.mybatis;

import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.thing.Name;
import jig.infrastructure.JigPaths;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverTest {

    SqlRepository repository = new OnMemorySqlRepository();
    CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    MyBatisSqlResolver sut = new MyBatisSqlResolver(repository, characteristicRepository, new JigPaths());

    @Test
    void test() throws Exception {
        ArrayList<URL> list = Collections.list(this.getClass().getClassLoader().getResources(""));
        URL[] urls = list.toArray(new URL[list.size()]);
        sut.resolve(urls);

        Sql sql = repository.get(new Name("jig.infrastructure.mybatis.SampleMapper.simple"));
        assertThat(sql.tableName()).isEqualTo("hoge");
    }

    @Test
    void 解析に失敗してもテーブル不明として出力できる() throws Exception {
        ArrayList<URL> list = Collections.list(this.getClass().getClassLoader().getResources(""));
        URL[] urls = list.toArray(new URL[list.size()]);
        sut.resolve(urls);

        Sql sql = repository.get(new Name("jig.infrastructure.mybatis.SampleMapper.binding"));
        assertThat(sql.tableName()).isEqualTo("（解析失敗）");
    }
}