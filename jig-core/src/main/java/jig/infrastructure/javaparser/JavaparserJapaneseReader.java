package jig.infrastructure.javaparser;

import jig.application.service.JapaneseReader;
import jig.domain.model.project.ProjectLocation;
import org.springframework.stereotype.Component;

@Component
public class JavaparserJapaneseReader implements JapaneseReader {
    ClassCommentReader classCommentReader;
    PackageInfoReader packageInfoReader;

    public JavaparserJapaneseReader(ClassCommentReader classCommentReader, PackageInfoReader packageInfoReader) {
        this.classCommentReader = classCommentReader;
        this.packageInfoReader = packageInfoReader;
    }

    @Override
    public void readFrom(ProjectLocation projectLocation) {
        classCommentReader.execute(projectLocation.getValue());
        packageInfoReader.execute(projectLocation.getValue());
    }
}