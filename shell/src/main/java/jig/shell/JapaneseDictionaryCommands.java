package jig.shell;

import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.infrastructure.RecursiveFileVisitor;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.javaparser.PackageInfoReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Paths;

@ShellComponent
public class JapaneseDictionaryCommands {

    @Autowired
    JapaneseNameRepository repository;
    @Autowired
    PackageInfoReader packageInfoReader;
    @Autowired
    ClassCommentReader classCommentReader;

    @ShellMethod("package-info.javaのJavadocコメントを読み込む")
    public void importPackageInfoJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {

        RecursiveFileVisitor fileVisitor = new RecursiveFileVisitor(packageInfoReader::execute);
        fileVisitor.visitAllDirectories(Paths.get(sourceDirectory));
    }

    @ShellMethod("classのJavadocコメントを読み込む")
    public void importClassJavadoc(@ShellOption(defaultValue = "./src/main/java") String sourceDirectory) {

        RecursiveFileVisitor fileVisitor = new RecursiveFileVisitor(classCommentReader::execute);
        fileVisitor.visitAllDirectories(Paths.get(sourceDirectory));
    }

    @ShellMethod("取り込まれた一覧を表示")
    public String showJapaneseDictionary() {
        return repository.all().asText();
    }
}
