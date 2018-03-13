package jig.classlist;

import jig.domain.model.list.ModelTypeRepository;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.infrastructure.OnMemoryModelTypeRepository;
import jig.infrastructure.OnMemoryRelationRepository;
import jig.infrastructure.javaparser.ClassCommentLibrary;
import jig.infrastructure.reflection.ModelTypeClassLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

@Configuration
public class ClassListConfig {

    @Bean
    ModelTypeRepository modelTypeRepository() {
        return new OnMemoryModelTypeRepository();
    }

    @Bean
    RelationRepository relationRepository() {
        return new OnMemoryRelationRepository();
    }

    @Bean
    JapaneseNameDictionary japaneseNameRepository(@Value("${target.source}") String sourcePath) {
        return new ClassCommentLibrary(Paths.get(sourcePath)).borrow();
    }

    @Bean
    ModelTypeClassLoader modelTypeClassLoader(@Value("${target.class}") String targetClasspath) {
        return new ModelTypeClassLoader(targetClasspath, relationRepository(), modelTypeRepository());
    }
}
