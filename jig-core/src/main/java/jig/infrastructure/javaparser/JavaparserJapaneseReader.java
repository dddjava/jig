package jig.infrastructure.javaparser;

import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.japanasename.JapaneseReader;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.JigPaths;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class JavaparserJapaneseReader implements JapaneseReader {

    ClassCommentReader classCommentReader;
    PackageInfoReader packageInfoReader;

    public JavaparserJapaneseReader(JapaneseNameRepository repository, JigPaths jigPaths) {
        this.classCommentReader = new ClassCommentReader(repository, jigPaths);
        this.packageInfoReader = new PackageInfoReader(repository, jigPaths);
    }

    @Override
    public void readFrom(ProjectLocation projectLocation) {
        Path path = projectLocation.getValue();

        classCommentReader.execute(path);
        packageInfoReader.execute(path);
    }
}