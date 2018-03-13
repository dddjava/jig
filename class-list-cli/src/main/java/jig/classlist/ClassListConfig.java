package jig.classlist;

import jig.domain.model.relation.RelationRepository;
import jig.domain.model.tag.JapaneseNameRepository;
import jig.domain.model.thing.ThingRepository;
import jig.infrastructure.OnMemoryJapanaseNameRepository;
import jig.infrastructure.OnMemoryRelationRepository;
import jig.infrastructure.OnMemoryThingRepository;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.reflection.ModelTypeClassLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class ClassListConfig {

    @Bean
    ThingRepository thingRepository() {
        return new OnMemoryThingRepository();
    }

    @Bean
    RelationRepository relationRepository() {
        return new OnMemoryRelationRepository();
    }

    @Bean
    JapaneseNameRepository japaneseNameRepository(@Value("${target.source}") String sourcePath) {
        JapaneseNameRepository repository = new OnMemoryJapanaseNameRepository();
        ClassCommentReader classCommentReader = new ClassCommentReader(Paths.get(sourcePath));
        classCommentReader.registerTo(repository);
        return repository;
    }

    @Bean
    ModelTypeClassLoader modelTypeClassLoader(@Value("${target.class}") String targetClasspath) {
        return new ModelTypeClassLoader(targetClasspath, thingRepository(), relationRepository());
    }
}
