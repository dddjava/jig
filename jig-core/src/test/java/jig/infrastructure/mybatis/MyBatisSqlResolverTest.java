package jig.infrastructure.mybatis;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlIdentifier;
import jig.domain.model.datasource.SqlRepository;
import jig.infrastructure.JigPaths;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverTest {

    @Test
    void bindを使ってても解析できる() throws Exception {
        SqlRepository repository = new OnMemorySqlRepository();
        JigPaths jigPaths = new JigPaths();

        MyBatisSqlResolver sut = new MyBatisSqlResolver(repository, jigPaths);
        URL[] urls = Collections.list(this.getClass().getClassLoader().getResources("")).toArray(new URL[0]);

        sut.resolve(urls);

        Sql sql = repository.get(new SqlIdentifier("jig.infrastructure.mybatis.SampleMapper.binding"));
        assertThat(sql.tableName()).isEqualTo("fuga");
    }
}