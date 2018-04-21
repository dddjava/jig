package jig.infrastructure.javaparser;

import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.japanese.JapaneseReader;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.JigPaths;
import org.springframework.stereotype.Component;

@Component
public class JavaparserJapaneseReader implements JapaneseReader {

    JapaneseNameRepository repository;
    JigPaths jigPaths;

    public JavaparserJapaneseReader(JapaneseNameRepository repository, JigPaths jigPaths) {
        this.repository = repository;
        this.jigPaths = jigPaths;
    }

    @Override
    public void readFrom(ProjectLocation projectLocation) {
        ClassCommentReader classCommentReader = new ClassCommentReader();
        jigPaths.sourcePaths(projectLocation).forEach(path -> {
            classCommentReader.execute(path).ifPresent(repository::register);
        });

        PackageInfoReader packageInfoReader = new PackageInfoReader();
        jigPaths.packageInfoPaths(projectLocation).forEach(path -> {
            packageInfoReader.execute(path).ifPresent(repository::register);
        });
    }
}