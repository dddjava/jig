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
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
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

        Sql sql = repository.get(new Name("jig.infrastructure.mybatis.SampleMapper.exists"));
        assertThat(sql.tableName()).isEqualTo("dual");
    }

    @Test
    void SQLのテーブル名とCRUDを取得する() throws Exception {
        sut.resolve(Paths.get("../sut"));

        assertThat(repository.get(new Name("sut.infrastructure.datasource.FugaMapper.get")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("fuga", SqlType.SELECT);

        assertThat(repository.get(new Name("sut.infrastructure.datasource.AnnotationMapper.select")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.SELECT);
        assertThat(repository.get(new Name("sut.infrastructure.datasource.AnnotationMapper.insert")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.INSERT);
        assertThat(repository.get(new Name("sut.infrastructure.datasource.AnnotationMapper.update")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.UPDATE);
        assertThat(repository.get(new Name("sut.infrastructure.datasource.AnnotationMapper.delete")))
                .extracting(Sql::tableName, Sql::sqlType)
                .containsExactly("sut.piyo", SqlType.DELETE);
    }

    @Test
    void TAGが登録されている() throws Exception {
        sut.resolve(Paths.get("../sut"));

        Names names = characteristicRepository.find(Characteristic.MAPPER_METHOD);

        assertThat(names.list())
                .extracting(Name::value)
                .contains(
                        "sut.infrastructure.datasource.FugaMapper.get",
                        "sut.infrastructure.datasource.AnnotationMapper.select");
    }
}