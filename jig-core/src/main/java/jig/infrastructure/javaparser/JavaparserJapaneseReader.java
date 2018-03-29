package jig.infrastructure.javaparser;

import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.japanasename.JapaneseReader;
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
        ClassCommentReader classCommentReader = new ClassCommentReader(repository);
        jigPaths.sourcePaths(projectLocation)
                .forEach(classCommentReader::execute);

        PackageInfoReader packageInfoReader = new PackageInfoReader(repository);
        jigPaths.packageInfoPaths(projectLocation)
                .forEach(packageInfoReader::execute);
    }
}