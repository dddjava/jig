package jig.infrastructure.mybatis;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlIdentifier;
import jig.domain.model.datasource.SqlRepository;
import jig.infrastructure.JigPaths;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

class MyBatisSqlResolverTest {

    private static MyBatisSqlResolver sut;
    private static SqlRepository repository;
    private static URL[] urls;

    @BeforeAll
    static void setup() throws Exception {
        repository = new OnMemorySqlRepository();
        JigPaths jigPaths = new JigPaths();

        sut = new MyBatisSqlResolver(repository, jigPaths);
        urls = TestSupport.getTestResourceRootURLs();
    }

    @Test
    void bindを使ってても解析できる() throws Exception {
        sut.resolve(urls);

        Sql sql = repository.get(new SqlIdentifier("jig.infrastructure.mybatis.SampleMapper.binding"));
        assertThat(sql.tableName()).isEqualTo("fuga");
    }
}